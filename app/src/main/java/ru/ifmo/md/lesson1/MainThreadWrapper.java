package ru.ifmo.md.lesson1;

import android.graphics.Canvas;
import android.util.Log;

import java.util.Objects;

/**
 * Created by Home on 14.09.2014.
 */
public class MainThreadWrapper {

    volatile boolean running = false;

    private final WhirlView whirlView;
    private final Object monitor = new Object();
    private boolean updated = true;
    private Thread drawThread = null;
    private Thread updateThread = null;

    class DrawWrapper implements Runnable {

        public void run() {
            Canvas canvas;
            float sumFps = 0;
            int frameCount = 0;
            while (running) {
                if (whirlView.holder.getSurface().isValid()) {
                    long startTime = System.nanoTime();
                    canvas = whirlView.holder.lockCanvas();
                    synchronized (monitor) {
                        while (!updated) {
                            try {
                                monitor.wait(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        whirlView.render(canvas);
                        updated = false;
                        monitor.notifyAll();
                    }
                    whirlView.holder.unlockCanvasAndPost(canvas);
                    long finishTime = System.nanoTime();
                    sumFps += 1e9 / (finishTime - startTime);
                    Log.i("TIME", "Circle: " + sumFps / (++frameCount) + " fps");
                }
            }
        }

    }

    class UpdateWrapper implements Runnable {

        public void run() {
            while (running) {
                synchronized (monitor) {
                    while (updated) {
                        try {
                            monitor.wait(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    whirlView.updateField();
                    updated = true;
                    monitor.notifyAll();
                }
            }
        }

    }

    public MainThreadWrapper(WhirlView view) {
        whirlView = view;
    }

    public void resume() {
        running = true;
        updated = true;
        drawThread = new Thread(new DrawWrapper());
        updateThread = new Thread(new UpdateWrapper());
        drawThread.start();
        updateThread.start();
    }

    public void pause() {
        running = false;
        try {
            drawThread.join();
            updateThread.join();
        } catch (InterruptedException ignore) {}
    }

}
