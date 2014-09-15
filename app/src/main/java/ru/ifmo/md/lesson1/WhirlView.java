package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null;
    int [][] field2 = null;
    int [][] work = null;
    int [] pixels = null;
    final int width = 240;
    final int height = 320;
    int thisPixel;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Bitmap bitmap;
    Rect rect;
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
                draw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "FPS: " + 1000000000 / ((double) finishTime - startTime));
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        rect = new Rect(0, 0, w, h);

        initField();
    }

    void initField() {
        field = new int[width][height];
        field2 = new int[width][height];
        pixels = new int[width * height];
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
                field2[x][y] = field[x][y];
            }
        }
    }

    void updateField() {
        int previousX;
        int nextX;
        int previousY;
        int nextY;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0) {
                    previousX = width - 1;
                } else {
                    previousX = x - 1;
                }
                if (x == width - 1) {
                    nextX = 0;
                } else {
                    nextX = x + 1;
                }
                if (y == 0) {
                    previousY = height - 1;
                } else {
                    previousY = y - 1;
                }
                if (y == height - 1) {
                    nextY = 0;
                } else {
                    nextY = y + 1;
                }

                if ((field[x][y] == MAX_COLOR - 1 && field[previousX][previousY] == 0) || (field[x][y] + 1 == field[previousX][previousY])) {
                    field2[x][y] = field[previousX][previousY];
                }
                if ((field[x][y] == MAX_COLOR - 1 && field[previousX][y] == 0) || (field[x][y] + 1 == field[previousX][y])) {
                    field2[x][y] = field[previousX][y];
                }
                if ((field[x][y] == MAX_COLOR - 1 && field[previousX][nextY] == 0) || (field[x][y] + 1 == field[previousX][nextY])) {
                    field2[x][y] = field[previousX][nextY];
                }
                if ((field[x][y] == MAX_COLOR - 1 && field[x][previousY] == 0) || (field[x][y] + 1 == field[x][previousY])) {
                    field2[x][y] = field[x][previousY];
                }
                if ((field[x][y] == MAX_COLOR - 1 && field[x][nextY] == 0) || (field[x][y] + 1 == field[x][nextY])) {
                    field2[x][y] = field[x][nextY];
                }
                if ((field[x][y] == MAX_COLOR - 1 && field[nextX][previousY] == 0) || (field[x][y] + 1 == field[nextX][previousY])) {
                    field2[x][y] = field[nextX][previousY];
                }
                if ((field[x][y] == MAX_COLOR - 1 && field[nextX][y] == 0) || (field[x][y] + 1 == field[nextX][y])) {
                    field2[x][y] = field[nextX][y];
                }
                if ((field[x][y] == MAX_COLOR - 1 && field[nextX][nextY] == 0) || (field[x][y] + 1 == field[nextX][nextY])) {
                    field2[x][y] = field[nextX][nextY];
                }
            }
        }
        work = field;
        field = field2;
        field2 = work;

        thisPixel = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[thisPixel++] = palette[field[x][y]];
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        canvas.drawBitmap(bitmap, null, rect, null);
    }
}
