package ru.ifmo.md.lesson1;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.SurfaceHolder;

/**
 * Created by Женя on 14.09.2014.
 */
public class DrawThread extends Thread {
    private boolean running = false;
    private SurfaceHolder holder;
    private WhirlView whirlView;

    public DrawThread(SurfaceHolder holder, WhirlView whirlView) {
        this.whirlView = whirlView;
        this.holder = holder;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }


    @Override
    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                Canvas canvas = holder.lockCanvas();
                synchronized (holder) {
                    whirlView.updateField();
                    whirlView.draw(canvas);
                }
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

}


