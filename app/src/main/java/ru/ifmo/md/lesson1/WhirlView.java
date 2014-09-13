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
class WhirlView extends SurfaceView implements Runnable {

    // since most modern phones have at least double-core cpu, why not add another thread to draw
    // and update field in parallel?
    private class UpdaterThread extends Thread {
        @Override
        public void run() {
            while(running) {
                try {
                    barrier.await(); // seems like the most appropriate way to synchronise
                    updateField();
                    barrier.await();
                } catch (InterruptedException ignore) {} catch (BrokenBarrierException ignore) {}
            }
        }
    }

    volatile CyclicBarrier barrier = null;
    int [][] field1 = null;
    int [][] field2 = null;
    int [] pixels = null;
    long lastFrameStart = 0;
    int width = 0;
    int height = 0;
    float scaleX = 0.0f;
    float scaleY = 0.0f;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    UpdaterThread updaterThread = null;
    volatile boolean running = false;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
        pixels = new int[320*240];
    }

    public void resume() {
        running = true;
        barrier = new CyclicBarrier(2);
        thread = new Thread(this);
        thread.start();
        updaterThread = new UpdaterThread();
        updaterThread.start();
    }

    public void pause() {
        running = false;
        CyclicBarrier cb = barrier;
        barrier = new CyclicBarrier(1); // make other threads pass barrier.await transparently
        cb.reset(); // break threads that are already awaiting
        try {
            thread.join();
            updaterThread.join();
        } catch (InterruptedException ignore) {}
        barrier = null;
    }

    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                try {
                    barrier.await();
                    long startTime = System.nanoTime();
                    Log.i("TIME","Since last frame: " + (startTime - lastFrameStart) / 1000000);
                    lastFrameStart = startTime;
                    Canvas canvas = holder.lockCanvas();
                    doDraw(canvas);
                    holder.unlockCanvasAndPost(canvas);
                    barrier.await();
                } catch (InterruptedException ignore) {} catch (BrokenBarrierException ignore) {}
                swapFields();
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        pause();
        if (w > h) {
            width = 320;
            height = 240;
            scaleX = w / (float) width;
            scaleY = h / (float) height;
        } else {
            width = 240;
            height = 320;
            scaleX = w / (float) width;
            scaleY = h / (float) height;
        }
        initField();
        resume();
    }

    void initField() {
        field1 = new int[width][height];
        field2 = new int[width][height];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field1[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateSingleField(int x, int y) {
        field2[x][y] = field1[x][y];

        int nc = field1[x][y] + 1;
        if (nc == MAX_COLOR) { // faster than %, even in java
            nc = 0;
        }

        outer:
        for (int dx=-1; dx<=1; dx++) {
            for (int dy=-1; dy<=1; dy++) {
                int x2 = x + dx;
                int y2 = y + dy;
                if (x2<0) x2 += width;
                if (y2<0) y2 += height;
                if (x2>=width) x2 -= width;
                if (y2>=height) y2 -= height;

                if (nc == field1[x2][y2]) {
                    field2[x][y] = field1[x2][y2];
                    break outer;
                }
            }
        }
    }

    void updateField() {
        int wm1 = width - 1;
        int hm1 = height - 1;

        for (int x=1; x < wm1; x++)
            for (int y=1; y < hm1; y++) {
                field2[x][y] = field1[x][y];

                int nc = field1[x][y] + 1;
                if (nc == MAX_COLOR) {
                    nc = 0;
                }

                outer:
                for (int dx=-1; dx<=1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;

                        if (nc == field1[x2][y2]) {
                            field2[x][y] = field1[x2][y2];
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

    void swapFields() {
        int [][] tf = field1;
        field1 = field2;
        field2 = tf;
    }

    public void doDraw(Canvas canvas) {
        canvas.scale(scaleX, scaleY);
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                pixels[x+y*width] = palette[field1[x][y]];
            }
        }
        canvas.drawBitmap(pixels, 0, width, 0, 0, width, height, false, null);
    }
}
