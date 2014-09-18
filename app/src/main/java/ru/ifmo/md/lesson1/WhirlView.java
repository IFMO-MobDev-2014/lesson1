package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by thevery on 11/09/14.
 */

// TODO While main thread is parking in run(), it`s a bit slow.
// (But synchronization is painful)

class WhirlView extends SurfaceView implements Runnable {
    private final int width = 240;
    private final int height = 382;

    private float k1;
    private float k2;
    // volatile?
    private int[][] field;
    private int[] paintedField = new int[width * height];
    private int[] tempFieldE = null;
    private int[][][] tempFieldS = null;

    private final int MAX_COLOR = 10;
    private int[] palette;
    private int cyclingConst = MAX_COLOR * MAX_COLOR;

    final int STRIPE_THREADS_NUMBER = 4;  //FIXME (height - 2) % STRIPE_THREADS_NUMBER must be 0
    private int delta = (height - 2) / STRIPE_THREADS_NUMBER;
    private Thread mainThread = null;
    private Runnable[] joiners = new Runnable[STRIPE_THREADS_NUMBER];
    private Runnable[] counters = new Runnable[STRIPE_THREADS_NUMBER + 1];
    private ExecutorService executor = Executors.newFixedThreadPool(STRIPE_THREADS_NUMBER * 2 + 1);
    private Future[] futures = new Future[STRIPE_THREADS_NUMBER * 2 + 1];

    Random rand = new Random();
    private SurfaceHolder holder;
    Canvas canvas;

    volatile boolean running = false;

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean cycling = true;
    private LinkedList<int[]> fieldCache = new LinkedList<int[]>();


    public WhirlView(Context context) {
        super(context);
        holder = getHolder();

    }

    private void initPalette() {
        palette = new int[MAX_COLOR];
        for (int i = 0; i < MAX_COLOR; i++) {
            palette[i] = rand.nextInt(Integer.MAX_VALUE);
        }
    }

    public void resume() {
        running = true;
        mainThread = new Thread(this);
        mainThread.start();
    }

    public void pause() {
        running = false;
        try {
            mainThread.join();
        } catch (InterruptedException ignore) {
        }
    }

    public void run() {
        initPalette();
        int cycles = 0;
        boolean looped = false;
        while (running) {
            if (holder.getSurface().isValid()) {
                canvas = holder.lockCanvas();
                long startTime = System.nanoTime();
                //noinspection ConstantConditions,PointlessBooleanExpression
                if (cycling && cycles > cyclingConst) { // +- magic constant
                    if (!looped) {
                        calculate();
                        if (!fieldCache.isEmpty() && Arrays.equals(fieldCache.getFirst(), paintedField)) {
                            fieldCache.addLast(fieldCache.removeFirst());
                            looped = true;
                        } else {
                            fieldCache.add(paintedField.clone());
                        }
                    } else {
                        paintedField = fieldCache.removeFirst();
//                        bitmap.setPixels(paintedField, 0, width, 0, 0, width, height);
                        fieldCache.addLast(paintedField);
                        onDraw(canvas);
                    }
                } else {
                    cycles++;
                    calculate();
                }
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle/FPS: " + (finishTime - startTime) / 1000000 + "/" + Math.round(1000000000.0 / ((double) (finishTime - startTime))));
            }
        }
    }

    private void calculate() {
        for (int i = 1; i < STRIPE_THREADS_NUMBER + 1; i++) {
            futures[i] = executor.submit(counters[i]);
        }
        onDraw(canvas);
        counters[0].run();
        for (int i = 1; i < STRIPE_THREADS_NUMBER + 1; i++) {
            try {
                futures[i].get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        updateField();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        k1 = (float) w / width;
        k2 = (float) h / height;
        initFields();
        initThreads();
    }

    void initFields() {
        field = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
        tempFieldE = new int[2 * width + 2 * height - 4];
        tempFieldS = new int[STRIPE_THREADS_NUMBER][width - 2][delta];
    }

    void initThreads() {
        counters[0] = new EdgeDrawer();
        for (int i = 0; i < STRIPE_THREADS_NUMBER; i++) {
            counters[i + 1] = new StripeDrawer(i);
            joiners[i] = new Joiner(i);
        }
    }

    void updateField() {
        for (int i = 0; i < STRIPE_THREADS_NUMBER; i++) {
            futures[i + 1 + STRIPE_THREADS_NUMBER] = executor.submit(joiners[i]);
        }
        for (int i = 0; i < width; i++) {
            field[i][0] = tempFieldE[i];
            field[i][height - 1] = tempFieldE[width + 2 * (height - 2) + i];
        }
        for (int i = 1; i < height - 1; i++) {
            field[0][i] = tempFieldE[width + i - 1];
            field[width - 1][i] = tempFieldE[width + (height - 2) + i - 1];
        }
        for (int i = STRIPE_THREADS_NUMBER + 1; i < 2 * STRIPE_THREADS_NUMBER + 1; i++)
            try {
                futures[i].get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                paintedField[i + width * j] = palette[field[i][j]];
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(k1, k2);
        canvas.drawBitmap(paintedField, 0, width, 0, 0, width, height, true, null);
    }

    private class Joiner implements Runnable {
        private final int num;

        private Joiner(int num) {
            this.num = num;
        }

        @Override
        public void run() {
            for (int x = 1; x < width - 1; x++) {
                System.arraycopy(tempFieldS[num][x - 1], 0, field[x], 1 + num * delta, delta);
            }
        }
    }

    private class EdgeDrawer implements Runnable {
        int[] adjacent1x = new int[]{width - 1, 0, 1};
        int[] adjacent2x = new int[]{width - 2, width - 1, 0};
        int[] adjacent1y = new int[]{height - 1, 0, 1};
        int[] adjacent2y = new int[]{height - 2, height - 1, 0};

        @Override
        public void run() {
            // corners
            for (int a : adjacent1x) {
                for (int b : adjacent1y) {
                    if ((field[0][0] + 1) % MAX_COLOR == field[a][b]) {
                        tempFieldE[0] = field[a][b];
                    }
                }
                for (int b : adjacent2y) {
                    if ((field[0][height - 1] + 1) % MAX_COLOR == field[a][b]) {
                        tempFieldE[width + (height - 2) * 2] = field[a][b];
                    }
                }
            }
            for (int a : adjacent2x) {
                for (int b : adjacent1y) {
                    if ((field[width - 1][0] + 1) % MAX_COLOR == field[a][b]) {
                        tempFieldE[width - 1] = field[a][b];
                    }
                }
                for (int b : adjacent2y) {
                    if ((field[width - 1][height - 1] + 1) % MAX_COLOR == field[a][b]) {
                        tempFieldE[2 * width + 2 * height - 5] = field[a][b];
                    }
                }
            }
            // horizontal lines
            for (int i = 1; i < width - 1; i++) {
                for (int a = i - 1; a <= i + 1; a++) {
                    for (int b : adjacent1y) { // upper row
                        if ((field[i][0] + 1) % MAX_COLOR == field[a][b]) {
                            tempFieldE[i] = field[a][b];
                        }
                    }
                    for (int b : adjacent2y) { // lower row
                        if ((field[i][height - 1] + 1) % MAX_COLOR == field[a][b]) {
                            tempFieldE[width + (height - 2) * 2 + i] = field[a][b];
                        }
                    }
                }
            }
            // vertical lines
            for (int i = 1; i < height - 1; i++) {
                for (int b = i - 1; b <= i + 1; b++) {
                    for (int a : adjacent1x) { // left column
                        if ((field[0][i] + 1) % MAX_COLOR == field[a][b]) {
                            tempFieldE[width + i - 1] = field[a][b];
                        }
                    }
                    for (int a : adjacent2x) { // right column
                        if ((field[width - 1][i] + 1) % MAX_COLOR == field[a][b]) {
                            tempFieldE[width + (height - 2) + i - 1] = field[a][b];
                        }
                    }
                }
            }
        }
    }

    private class StripeDrawer implements Runnable {
        private int num, temp, temp2, x2, y2;
        private int shift;

        private StripeDrawer(int num) {
            this.num = num;
            shift = 1 + num * delta;
        }

        @Override
        public void run() {
            for (int x = 0; x < width - 2; x++) {
                for (int y = 0; y < delta; y++) {
                    temp2 = field[x + 1][y + shift];
                    tempFieldS[num][x][y] = temp2;
                    der:
                    for (int dx = -1; dx < 2; dx++) {
                        x2 = x + dx;
                        for (int dy = -1; dy < 2; dy++) {
                            y2 = y + dy;
                            temp = field[x2 + 1][y2 + shift];
                            if ((temp2 + 1) % MAX_COLOR == temp) {
                                tempFieldS[num][x][y] = temp;
                                break der; // That's the right way
                            }
                        }
                    }
                }
            }
        }
    }
}