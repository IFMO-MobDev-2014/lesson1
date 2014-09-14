package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.SurfaceView;

import java.util.Timer;
import java.util.TimerTask;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView {
    static final int WIDTH = 240, HEIGHT = 320;
    static final String TAG = "WhirlView";
    static final int div = 1;
    public static volatile boolean running = false;
    Bitmap toDraw;
    FieldDrawer drawer;
    Timer showTimer;
    FPSLogger perfLog;
    Matrix matrix = new Matrix();

    public WhirlView(Context context) {
        super(context);
    }

    public void resume() {
        if (getWidth() == 0 || getHeight() == 0)
            return;
        running = true;
        drawer = new FieldDrawer(WIDTH, HEIGHT);
        showTimer = new Timer();
        showTimer.schedule(new DisplayTask(), 0, 16);
        perfLog = new FPSLogger();
        showTimer.schedule(perfLog, 0, 1000 / div);
    }

    public void pause() {
        running = false;
        try {
            drawer.join();
            showTimer.cancel();
        } catch (InterruptedException ignore) {}
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        matrix.setScale((float) w / WIDTH, (float) h / HEIGHT);
        resume();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (toDraw != null)
            canvas.drawBitmap(toDraw, matrix, null);
    }

    class DisplayTask extends TimerTask {
        @Override
        public void run() {
            if (getHolder().getSurface().isValid()) {
                perfLog.tick();
                toDraw = drawer.nextBitmap();
                Canvas canvas = getHolder().lockCanvas();
                onDraw(canvas);
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }

    class FPSLogger extends TimerTask {
        int frameCount;

        public synchronized void tick() {
            frameCount++;
        }

        @Override
        public synchronized void run() {
            Log.d(TAG, "FPS: " + frameCount * div);
            frameCount = 0;
        }
    }
}
