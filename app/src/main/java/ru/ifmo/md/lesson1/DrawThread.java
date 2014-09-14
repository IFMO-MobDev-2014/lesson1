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
        int fps = 0, cnt = 0;
        long now = System.nanoTime() / 1000000;
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setTextSize(100);
        while (running) {
            if (System.nanoTime() / 1000000 - now > 5000) {
                fps = cnt / 5;
                cnt = 0;
                now = System.nanoTime() / 1000000;
            }
            if (holder.getSurface().isValid()) {
                Canvas canvas = holder.lockCanvas();
                synchronized (holder) {
                    whirlView.updateField();
                    whirlView.draw(canvas);
                    canvas.drawText("FPS = " + fps, 100, 100, p);

                }
                holder.unlockCanvasAndPost(canvas);
            }
            ++cnt;
        }
    }

}


