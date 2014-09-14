package ru.ifmo.md.lesson1;

import android.graphics.Canvas;
import android.util.Log;

/**
 * Created by Home on 14.09.2014.
 */
public class UpdateThread implements Runnable {

    Thread thread = null;
    volatile boolean running = false;

    private final WhirlView whirlView;

    public UpdateThread(WhirlView view) {
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
        synchronized (whirlView) {
            while (running) {
                whirlView.updateField();
                whirlView.notify();
            }
        }
    }

}
