package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.Console;
import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 */

// NOTE It can lose in performance because of shared field
class WhirlView extends SurfaceView implements Runnable {
    volatile int[][] field = null;
    int[] tempFieldE = null;
    int[][][] tempFieldS = null;
    final int width = 240;
    final int height = 380;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    final int STRIPE_THREADS_NUMBER = 1;  //FIXME (height - 2) % STRIPE_THREADS_NUMBER must be 0
    int delta = (height - 2) / STRIPE_THREADS_NUMBER;
    Thread mainthread = null;
    Thread[] threads = null;
    Canvas canvas;
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    Rect rect;
    volatile boolean running = false;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
    }

    public void resume() {
        running = true;
        mainthread = new Thread(this);
        mainthread.start();
    }

    public void pause() {
        running = false;
        try {
            mainthread.join();
        } catch (InterruptedException ignore) {
        }
    }

    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                canvas = holder.lockCanvas();
                long startTime = System.nanoTime();
                // haha nice try
                for (Thread thread : threads) thread.run();
                try {
                    for (Thread thread : threads) thread.join();
                } catch (InterruptedException e) {
//                    Thread.sleep(100);
                    e.printStackTrace();
                }
                //
                updateField();
                dumpBitmap();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle/FPS: " + (finishTime - startTime) / 1000000 + "/" + Math.round(1000000000.0 / ((double) (finishTime - startTime))));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
//        width = w / scale;
//        height = h / scale;
        rect = new Rect(0, 0, w, h);
        canvas = new Canvas(bitmap);
        tempFieldE = new int[2 * width + 2 * height - 4];
        tempFieldS = new int[STRIPE_THREADS_NUMBER][width - 2][delta];
        initField();
        initThreads();
    }

    void initField() {
        field = new int[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void initThreads() {
        threads = new Thread[STRIPE_THREADS_NUMBER + 1];
        threads[0] = new Thread(new EdgeDrawer(), "Edger");
        for (int i = 0; i < STRIPE_THREADS_NUMBER; i++) threads[i + 1] = new Thread(new StripeDrawer(i), "Striper #" + (i+1));
//        for (int i = 0; i < STRIPE_THREADS_NUMBER; i++) threads[i + 1] = new Thread (new StripeDrawer(STRIPE_THREADS_NUMBER - 1 - i), "Striper #" + (i+1));
    }

        // TODO Construnt field out of tempfields
    void updateField() {
        for (int i = 0; i < width; i++) {
            field[i][0] = tempFieldE[i];
            field[i][height - 1] = tempFieldE[width + 2 * (height - 2) + i];
            bitmap.setPixel(i, 0, palette[field[i][0]]);
            bitmap.setPixel(i, height - 1, palette[field[i][height - 1]]);
        }
        for (int i = 1; i < height - 1; i++) {
            field[0][i] = tempFieldE[width + i - 1];
            field[width - 1][i] = tempFieldE[width + (height - 2) + i - 1];
            bitmap.setPixel(0, i, palette[field[0][i]]);
            bitmap.setPixel(width - 1, i, palette[field[width - 1][i]]);
        }
        for (int num = 0; num < STRIPE_THREADS_NUMBER; num++) {
            for (int x = 1; x < width - 1; x++) {
                System.arraycopy(tempFieldS[num][x - 1], 0, field[x], 1 + num * delta, delta);
            }
        }
    }

    int[][] dumper = new int[width][height];
    void dumpBitmap(){
        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++) {
                dumper[i][j] = bitmap.getPixel(i, j);
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, null, rect, null);
    }

    private class EdgeDrawer implements Runnable {
        int[] adjacent1x = new int[]{width - 1, 0, 1};
        int[] adjacent2x = new int[]{width - 2, width - 1, 0};
        int[] adjacent1y = new int[]{height - 1, 0, 1};
        int[] adjacent2y = new int[]{height - 2, height - 1, 0};

        // (a,b) base, (c,d) new, f place in tempFieldE
        void validate(int a, int b, int c, int d, int f) {
            if ((field[a][b] + 1) % MAX_COLOR == field[c][d]) { tempFieldE[f] = field[c][d]; }
        }

        @Override
        public void run() {
            // corners
            for (int a : adjacent1x){
                for (int b: adjacent1y) { validate(0, 0, a, b, 0); }
                for (int b: adjacent2y) { validate(0, height - 1, a, b, width + (height - 2) * 2); }
            }
            for (int a : adjacent2x){
                for (int b: adjacent1y) { validate(width - 1, 0, a, b, width - 1); }
                for (int b: adjacent2y) {
                    validate(width - 1, height - 1, a, b, 2 * width + 2 * height - 5);
                }
            }
            // horizontal lines
            for (int i = 1; i < width - 1; i++) {
                for (int a : new int[]{i - 1, i, i + 1}){
                    for (int b : adjacent1y){ // upper row
                        validate(i, 0, a, b, i);
                    }
                    for (int b : adjacent2y){ // lower row
                        validate(i, height - 1, a, b, width + (height - 2) * 2 + i);
                    }
                }
            }
            // vertical lines
            for (int i = 1; i < height - 1; i++) {
                for (int b : new int[]{i - 1, i, i + 1}){
                    for (int a : adjacent1x){ // left column
                        validate(0, i, a, b, width + i - 1);
                    }
                    for (int a : adjacent2x){ // right column
                        validate(width - 1, i, a, b, width + (height - 2) + i - 1);
                    }
                }
            }
            // now tempFieldE is ready
        }
    }

    private class StripeDrawer implements Runnable {
        private int num;

        private StripeDrawer(int num) {
            this.num = num;
        }

        @Override
        public void run() {
            // FIXME Indexes bug
            int shift = 1 + num * delta;
            for (int x = 0; x < width - 2; x++) {
                for (int y = 0; y < delta; y++) {
                    tempFieldS[num][x][y] = field[x + 1][y + shift];
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int x2 = x + dx;
                            int y2 = y + dy;
                            if ((field[x + 1][y + shift] + 1) % MAX_COLOR == field[x2 + 1][y2 + shift]) {
                                tempFieldS[num][x][y] = field[x2 + 1][y2 + shift];
                                bitmap.setPixel(x + 1, y + shift, palette[tempFieldS[num][x][y]]);
                            }
                        }
                    }
                }
            }
        }
    }
}
