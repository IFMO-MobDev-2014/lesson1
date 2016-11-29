package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView {

    // since most modern phones have at least double-core cpu, why not add another thread to draw
    // and update field in parallel?
    private class UpdaterThread extends Thread {
        @Override
        public void run() {
            while (running) {
                try {
                    barrier.await(); // seems like the most appropriate way to synchronise
                    updateField();
                    barrier.await();
                } catch (InterruptedException ignore) {} catch (BrokenBarrierException ignore) {}
            }
        }
    }

    private class DrawThread extends Thread {
        @Override
        public void run() {
            while (running) {
                if (holder.getSurface().isValid()) {
                    try {
                        barrier.await();

                        framesCounter++; // the only reason why it's here is because drawing is generally faster than updating, so we might as well do it here
                        long currentTime = System.nanoTime() / 1000000;
                        if (currentTime - lastSecondStart >= 1000) {
                            lastSecondStart = currentTime;
                            Log.i("FPS", framesCounter + "");
                            framesCounter = 0;
                        }

                        Canvas canvas = holder.lockCanvas();
                        doDraw(canvas);
                        holder.unlockCanvasAndPost(canvas);
                        barrier.await();
                        swapFields();
                    } catch (InterruptedException ignore) {} catch (BrokenBarrierException ignore) {}
                }
            }
        }
    }

    private volatile CyclicBarrier barrier = null;
    private int [][] fieldFront = null;
    private int [][] fieldBack = null;
    private int [] pixels = null;
    private long lastSecondStart = 0;
    private int framesCounter = 0;
    private int width = 0;
    private int height = 0;
    private float scaleX = 0.0f;
    private float scaleY = 0.0f;
    private final int SIZE_LONG = 320;
    private final int SIZE_SHORT = 240;
    private int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    private final int MAX_COLOR = palette.length; // makes sense to do it this way
    private SurfaceHolder holder;
    private DrawThread drawThread = null;
    private UpdaterThread updaterThread = null;
    private volatile boolean running = false;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
        pixels = new int[SIZE_LONG * SIZE_SHORT];
    }

    public void resume() {
        running = true;
        barrier = new CyclicBarrier(2);
        drawThread = new DrawThread();
        drawThread.start();
        updaterThread = new UpdaterThread();
        updaterThread.start();
    }

    public void pause() {
        running = false;
        CyclicBarrier cb = barrier;
        barrier = new CyclicBarrier(1); // make other threads pass barrier.await transparently
        cb.reset(); // break threads that are already awaiting
        try {
            drawThread.join();
            updaterThread.join();
        } catch (InterruptedException ignore) {}
        barrier = null;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        boolean wasRunning = running;
        if (running)
            pause();

        if (w > h) {
            width = SIZE_LONG;
            height = SIZE_SHORT;
        } else {
            width = SIZE_SHORT;
            height = SIZE_LONG;
        }
        scaleX = w / (float) width;
        scaleY = h / (float) height;
        initField();

        if (wasRunning)
            resume();
    }

    private void initField() {
        fieldFront = new int[width][height];
        fieldBack = new int[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                fieldFront[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    private void updateSingleField(int x, int y) {
        fieldBack[x][y] = fieldFront[x][y];

        int nc = fieldFront[x][y] + 1;
        if (nc == MAX_COLOR) { // faster than %, even in java
            nc = 0;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int x2 = x + dx;
                int y2 = y + dy;
                if (x2<0) x2 += width;
                if (y2<0) y2 += height;
                if (x2>=width) x2 -= width;
                if (y2>=height) y2 -= height;

                if (nc == fieldFront[x2][y2]) {
                    fieldBack[x][y] = fieldFront[x2][y2];
                    return;
                }
            }
        }
    }

    private void updateField() {
        int wm1 = width - 1;
        int hm1 = height - 1;

        for (int x=1; x < wm1; x++)
            for (int y=1; y < hm1; y++) {
                fieldBack[x][y] = fieldFront[x][y];

                int nc = fieldFront[x][y] + 1;
                if (nc == MAX_COLOR) {
                    nc = 0;
                }

                outer:
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;

                        if (nc == fieldFront[x2][y2]) {
                            fieldBack[x][y] = fieldFront[x2][y2];
                            break outer;
                        }
                    }
                }
            }

        for (int x=0; x < width; x++) {
            updateSingleField(x, 0);
            updateSingleField(x, hm1);
        }

        for (int y=0; y < height; y++) {
            updateSingleField(0, y);
            updateSingleField(wm1, y);
        }
    }

    private void swapFields() {
        int[][] tf = fieldFront;
        fieldFront = fieldBack;
        fieldBack = tf;
    }

    private void doDraw(Canvas canvas) {
        canvas.scale(scaleX, scaleY);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[x + y * width] = palette[fieldFront[x][y]];
            }
        }
        canvas.drawBitmap(pixels, 0, width, 0, 0, width, height, false, null);
    }
}
