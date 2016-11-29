package ru.ifmo.md.lesson1;

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
    int[] drawField;
    int width;
    int height;
    final int MAX_COLOR = 10;
    RectF screenRect;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    volatile boolean fieldStabilized = false;

    public WhirlView(Context context) {
        super(context);
        width = 240;
        height = 320;
        drawField = new int[width * height];
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
        } catch (InterruptedException ignore) {
        }
    }

    public void run() {
        int time = 0, count = 0;
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                updateField();
                draw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                count++;
                time += (finishTime - startTime) / 1000000;
                if (time > 100) {
                    Log.i("TIME", "FPS: " + count / (time / 1000.0));
                    time = 0;
                    count = 0;
                }
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
//        width = w / scale;
//        height = h / scale;
        screenRect = new RectF(0, 0, w, h);
    }

    void initField() {
        field = new int[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
                drawField[y * width + x] = palette[field[x][y]];
            }
        }
    }

    void incCells() {
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                field[i][j]++;
                if (field[i][j] == MAX_COLOR)
                    field[i][j] = 0;
                drawField[j * width + i] = palette[field[i][j]];
            }
    }

    void updateField() {
        if (fieldStabilized) {
            incCells();
            return;
        }

        int[][] field2 = new int[width][height];
        int temp = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                field2[x][y] = field[x][y];
                int newColor = (field[x][y] + 1) % MAX_COLOR;
                boolean found = false;
                for (int dx = -1; !(dx > 1 || found); dx++) {
                    for (int dy = -1; !(dy > 1 || found); dy++) {
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
                    temp++;
                    drawField[y * width + x] = palette[field2[x][y]];
                }
            }
        }
        field = field2;
        fieldStabilized = temp == width * height;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(Bitmap.createBitmap(drawField, width, height, Bitmap.Config.ARGB_8888), null, screenRect, null);
    }
}