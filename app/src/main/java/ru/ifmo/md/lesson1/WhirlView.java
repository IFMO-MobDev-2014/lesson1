package ru.ifmo.md.lesson1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by thevery on 11/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    private final FpsLogger fpsLogger = new FpsLogger();
    private final SurfaceHolder holder;
    private final Updater updater = new Updater();

    private Thread thread = null;
    private volatile boolean running = false;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
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
        } catch (InterruptedException ignore) {
        }
    }

    @SuppressLint("WrongCall") // We really should call onDraw here
    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                updater.updateAll();
                Canvas canvas = holder.lockCanvas();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                fpsLogger.update();
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        updater.setScaleX(1F * w / Updater.WIDTH);
        updater.setScaleY(1F * h / Updater.HEIGHT);
        updater.initField();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(updater.getScaleX(), updater.getScaleY());
        canvas.drawBitmap(updater.getColors(), 0, Updater.WIDTH, 0, 0,
                Updater.WIDTH, Updater.HEIGHT, false, null);
    }
}
