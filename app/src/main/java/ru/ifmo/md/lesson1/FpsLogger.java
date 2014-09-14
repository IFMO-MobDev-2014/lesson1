package ru.ifmo.md.lesson1;

import android.util.Log;

/**
 * Calculates and logs fps.
 *
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
class FpsLogger {
    private float value;
    private int count;

    public FpsLogger(final int period) {
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                //noinspection InfiniteLoopStatement
                while (true) {
                    try {
                        Thread.sleep(period * 1000);
                    } catch (InterruptedException ignored) {

                    }

                    Log.i("FPS", String.format("%.2f", 1000F / value));
                    count = 0;
                }
            }
        }).start();
    }

    public synchronized void update(float value) {
        ++count;
        this.value = (this.value * (count - 1) + value) / count;
    }
}
