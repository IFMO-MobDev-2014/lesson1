package ru.ifmo.md.lesson1;

import android.graphics.Canvas;
import android.util.Log;

/**
 * Created by Home on 14.09.2014.
 */
public class MainThread implements Runnable{

    Thread thread = null;
    volatile boolean running = false;

    private final WhirlView whirlView;

    public MainThread(WhirlView view) {
        whirlView = view;
    }

    public void resume() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException ignore) {}
    }

    public void run() {
        Canvas canvas;
        float sumFps = 0;
        int frameCount = 0;
        //synchronized (whirlView) {
            while (running) {

                if (whirlView.holder.getSurface().isValid()) {
                    long startTime = System.nanoTime();
                    canvas = whirlView.holder.lockCanvas();
                    whirlView.render(canvas);
                    whirlView.updateField();
                    whirlView.holder.unlockCanvasAndPost(canvas);
                    long finishTime = System.nanoTime();
                    sumFps += 1e9 / (finishTime - startTime);
                    Log.i("TIME", "Circle: " + sumFps / (++frameCount) + " fps");
                }
                //whirlView.notify();
            }
        //}
    }
}
