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
    final int WIDTH ;
    final int HEIGHT;
    RectF screenRect;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    volatile boolean fieldStabilized = false;

    public WhirlView(Context context) {
        super(context);
        WIDTH = 240;
        HEIGHT = 320;
        buffer = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        holder = getHolder();
        initField();
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
                onDraw(canvas);
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
        System.out.println("SIZE CHANGED " + w + " " + h + " " + oldW + " " + oldH);
        screenRect = new RectF(0, 0, w, h);
    }

    void initField() {
        drawingField = new int[WIDTH * HEIGHT];
        field = new int[WIDTH][HEIGHT];
        Random rand = new Random();
        for (int x = 0; x < WIDTH; x++)
            for (int y = 0; y < HEIGHT; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
                drawingField[y * WIDTH + x] = palette[field[x][y]];
            }
    }

    private void incCells() {
        for (int i = 0; i < WIDTH; ++i)
            for (int j = 0; j < HEIGHT; ++j) {
                field[i][j]++;
                if (field[i][j] == MAX_COLOR)
                    field[i][j] = 0;
                drawingField[j * WIDTH + i] = palette[field[i][j]];
            }
    }

    void updateField() {
        if (fieldStabilized) {
            incCells();
            return;
        }

        int[][] field2 = new int[WIDTH][HEIGHT];
        int founds = 0;
        for (int x=0; x<WIDTH; x++) {
            for (int y=0; y<HEIGHT; y++) {
                field2[x][y] = field[x][y];
                int newColor = (field[x][y] + 1) % MAX_COLOR;
                boolean found = false;
                for (int dx = -1; dx <= 1 && !found; dx++) {
                    for (int dy = -1; dy <= 1 && !found; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += WIDTH;
                        if (y2 < 0) y2 += HEIGHT;
                        if (x2 >= WIDTH) x2 -= WIDTH;
                        if (y2 >= HEIGHT) y2 -= HEIGHT;

                        found = newColor == field[x2][y2];
                    }
                }

                if (found) {
                    field2[x][y] = newColor;
                    ++founds;
                    drawingField[y * WIDTH + x] = palette[field2[x][y]];
                }
            }
        }
        field = field2;
        fieldStabilized = founds == WIDTH * HEIGHT;
    }

    @Override
    public void onDraw(Canvas canvas) {
        buffer.setPixels(drawingField, 0, WIDTH, 0, 0, WIDTH, HEIGHT);
        canvas.drawBitmap(buffer, null, screenRect, null);
    }
}
