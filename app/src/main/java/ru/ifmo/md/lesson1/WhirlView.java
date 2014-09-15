package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    byte [][][] field = null;
    Bitmap bitmap = null;
    int[] bitmapArray = null;
    Rect imgRect = null;
    int curBuffer = 0;
    int width = 240;
    int height = 320;
    int scale = 2;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

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
        } catch (InterruptedException ignore) {}
    }

    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
/*                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}//*/
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        imgRect = new Rect(0, 0, w, h);
        initField();
    }

    void initField() {
        field = new byte[2][height + 2][width + 2];
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmapArray = new int[width * height];
        Random rand = new Random();
        curBuffer = 0;
        for (int y = 1; y <= height; y++) {
            for (int x = 1; x <= width; x++) {
                field[0][y][x] = (byte)rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        byte[][] cur = field[curBuffer], other = field[1 - curBuffer];
        for (int x = 1; x <= width; x++) {
            cur[0][x] = cur[height][x];
            cur[height + 1][x] = cur[1][x];
        }

        for (int y = 0; y <= height + 1; y++) {
            cur[y][0] = cur[y][width];
            cur[y][width + 1] = cur[y][1];
        }
        for (int y = 1; y <= height; y++) {
            for (int x = 1; x <= width; x++) {
                byte oldV = cur[y][x], newV = (byte)((oldV + 1) % MAX_COLOR);
                other[y][x] = oldV;

                if (cur[y - 1][x - 1] == newV || cur[y][x - 1] == newV || cur[y + 1][x - 1] == newV ||
                    cur[y - 1][x]     == newV ||                          cur[y + 1][x]     == newV ||
                    cur[y - 1][x + 1] == newV || cur[y][x + 1] == newV || cur[y + 1][x + 1] == newV) {
                    other[y][x] = newV;
                }
            }
        }
        curBuffer = 1 - curBuffer;
    }

    @Override
    public void onDraw(Canvas canvas) {
        byte[][] cur = field[curBuffer];
        int curPos = 0;
        for (int y = 1; y <= height; y++) {
            for (int x = 1; x <= width; x++) {
                bitmapArray[curPos] = palette[cur[y][x]];
                curPos++;
            }
        }
        bitmap.setPixels(bitmapArray, 0, width, 0, 0, width, height);
        canvas.drawBitmap(bitmap, null, imgRect, null);
    }
}
