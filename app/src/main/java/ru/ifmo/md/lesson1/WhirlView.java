package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    final static String TAG = "WhirlView";

    final static int FIELD_WIDTH = 240;
    final static int FIELD_HEIGHT = 320;

    Bitmap bitmap = null;
    float scaleWidth = 1f;
    float scaleHeight = 1f;
    int width = FIELD_WIDTH;
    int height = FIELD_HEIGHT;

    int [][] field = null;
    int [][] field2 = null;
    Paint paint = null;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
        bitmap = Bitmap.createBitmap(FIELD_WIDTH, FIELD_HEIGHT, Bitmap.Config.ARGB_4444);
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
        int framesDrawn = 0;
        long lastTime = System.currentTimeMillis();
        while (running) {
            if (holder.getSurface().isValid()) {
                Canvas canvas = holder.lockCanvas();
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                framesDrawn++;
                long curTime = System.currentTimeMillis();
                long diffTime = curTime - lastTime;
                if (diffTime > 1000) {
                    Log.i(TAG, "FPS: " + framesDrawn);
                    framesDrawn = 0;
                    lastTime = curTime;
                }
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        Log.i(TAG, "onSizeChanged: " + w + " " + h + " " + oldW + " " + oldH);
        scaleWidth = (float)w / FIELD_WIDTH;
        scaleHeight = (float)h / FIELD_HEIGHT;
        initField();
    }

    void initField() {
        paint = new Paint();
        field = new int[width][height];
        field2 = new int [width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {

                field2[x][y] = field[x][y];

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ( (field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }
        int [][] tmp = field;
        field = field2;
        field2 = tmp;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, palette[field[x][y]]);
            }
        }
        //canvas.drawBitmap(bitmap, rectSrc, rectDst, paint);
        canvas.scale(scaleWidth, scaleHeight);
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }
}
