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
    public static final int SCALE = 4;
    static final String TAG = "WhirlView";
    public static volatile boolean running = false;
    Bitmap toDraw;
    FieldDrawer drawer;
    Timer showTimer;
    FPSLogger perfLog;
    Matrix matrix;

    public WhirlView(Context context) {
        super(context);
        matrix = new Matrix();
        matrix.setScale(SCALE, SCALE);
    }

    public void resume() {
        if (getWidth() == 0 || getHeight() == 0)
            return;
        running = true;
        drawer = new FieldDrawer(getWidth() / SCALE, getHeight() / SCALE);
        showTimer = new Timer();
        showTimer.schedule(new DisplayTask(), 0, 16);
        perfLog = new FPSLogger();
        showTimer.schedule(perfLog, 0, 1000);
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
            Log.d(TAG, "FPS: " + frameCount);
            frameCount = 0;
        }
    }
}
