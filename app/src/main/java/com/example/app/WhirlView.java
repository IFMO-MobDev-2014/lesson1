package com.example.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/

class WhirlView extends SurfaceView implements Runnable {
    int[][] field = null;
    int[] drawingField;
    Bitmap buffer;
    int width, height;
    RectF screenRect;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    volatile boolean fieldStabilized = false;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
        setDrawingCacheQuality(DRAWING_CACHE_QUALITY_HIGH);
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
        int timeSum = 0, tot = 0;
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                updateField();
                draw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                ++tot;
                timeSum += (finishTime - startTime) / 1000000;
                if (timeSum > 5000) {
                    Log.i("TIME", "FPS: " + tot / (timeSum / 1000.0));
                    timeSum = 0;
                    tot = 0;
                }
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        if (w < h) {
            width = 240;
            height = 320;
        } else {
            width = 320;
            height = 240;
        }
        buffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        initField();
        screenRect = new RectF(0, 0, w, h);
    }

    void initField() {
        drawingField = new int[width * height];
        field = new int[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
                drawingField[y * width + x] = palette[field[x][y]];
            }
    }

    private void incCells() {
        for (int i = 0; i < width; ++i)
            for (int j = 0; j < height; ++j) {
                field[i][j]++;
                if (field[i][j] == MAX_COLOR)
                    field[i][j] = 0;
                drawingField[j * width + i] = palette[field[i][j]];
            }
    }

    void updateField() {
        if (fieldStabilized) {
            incCells();
            return;
        }

        int[][] field2 = new int[width][height];
        int founds = 0;
        for (int x=0; x< width; x++) {
            for (int y=0; y< height; y++) {
                field2[x][y] = field[x][y];
                int newColor = (field[x][y] + 1) % MAX_COLOR;
                boolean found = false;
                for (int dx = -1; dx <= 1 && !found; dx++) {
                    for (int dy = -1; dy <= 1 && !found; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;

                        found = newColor == field[x2][y2];
                    }
                }

                if (found) {
                    field2[x][y] = newColor;
                    ++founds;
                    drawingField[y * width + x] = palette[field2[x][y]];
                }
            }
        }
        field = field2;
        fieldStabilized = founds == width * height;
    }

    @Override
    public void draw(Canvas canvas) {
        buffer.setPixels(drawingField, 0, width, 0, 0, width, height);
        canvas.drawBitmap(buffer, null, screenRect, null);
    }
}
