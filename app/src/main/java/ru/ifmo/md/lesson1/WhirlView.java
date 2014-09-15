package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null, field2 = null;
    int [] colors = null;
    final int width = 240, height = 320;
    int screenWidth = 0, screenHeight = 0;
    Bitmap bitmap = null;
    Rect rect = null;
    // int scale = 4;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

    public WhirlView(Context context) {
        super(context);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        holder = getHolder();
        initField();
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

    int clockSum = 0, clockCnt = 0;
    long startTime, finishTime;
    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                finishTime = System.nanoTime();
                clockSum += (finishTime - startTime) / 1000000;
                clockCnt++;
                Log.i("TIME", "average time: " + clockSum / clockCnt);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        screenWidth = w;
        screenHeight = h;
        rect = new Rect(0, 0, screenWidth, screenHeight);
    }

    void initField() {
        colors = new int[width * height];
        field  = new int[width][height];
        field2 = new int[width][height];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field2[x][y] = field[x][y];
                for (int dx=-1; dx<=1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2<0) x2 += width;
                        if (y2<0) y2 += height;
                        if (x2>=width) x2 -= width;
                        if (y2>=height) y2 -= height;
                        if ( (field[x][y]+1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }

        int next = 0;
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                colors[next++] = palette[field2[x][y]];
            }
        }
        int[][] temp = field;
        field = field2;
        field2 = temp;
    }

    @Override
    public void onDraw(Canvas canvas) {
        bitmap.setPixels(colors, 0, width, 0, 0, width, height);
        canvas.drawBitmap(bitmap, null, rect, null);
    }
}
