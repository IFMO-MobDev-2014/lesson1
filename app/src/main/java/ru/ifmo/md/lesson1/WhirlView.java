package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 * Edited by Aganov on 15/09/14
 */
class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null;
    int [][] field2 = null;
    int [] colors = null;
    Paint paint = new Paint();
    int width = 0;
    int height = 0;
    int realWidth = 0;
    int realHeight = 0;
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
        Canvas canvas;
        long t1, t2, t3, t4;
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();

                canvas = holder.lockCanvas();
                t1 = System.nanoTime() - startTime;

                updateField();
                t2 = System.nanoTime() - t1  - startTime;

                onDraw(canvas);
                t3 = System.nanoTime() - t1 - t2  - startTime;

                holder.unlockCanvasAndPost(canvas);
                t4 = System.nanoTime() - t1 - t2 - t3  - startTime;

                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + t1 / 1000000 + " + " + t2 / 1000000 + " + " + t3 / 1000000 + " + " + t4 / 1000000 + " = " + (finishTime - startTime) / 1000000);
                try {
                    Thread.sleep(1);// o_0
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = 240;
        height = 320;
        colors = new int[width * height];
        realHeight = h;
        realWidth = w;
        initField();
    }

    void initField() {
        field = new int[width][height];
        field2 = new int[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void slowUpdate(int x3, int y3, int x4, int y4) {
        int x, y, dx, dy, color, x2, y2;
        boolean f;
        for (x = x3; x <= x4; x++) {
            for (y = y3; y <= y4; y++) {
                field2[x][y] = field[x][y];
                color = (field[x][y] + 1) % MAX_COLOR;
                f = false;
                for (dx = -1; dx <= 1 && !f; dx++) {
                    for (dy = -1; dy <= 1 && !f; dy++) {
                        x2 = x + dx;
                        y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if (color == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                            f = true;
                        }
                    }
                }
            }
        }
    }

    void updateField() {

        //int[][] field2 = new int[width][height];
        int color, x, y, x2, y2, dx, dy;
        boolean f = false;
        for (x = 1; x < width - 1; x++) {
            for (y = 1; y < height - 1; y++) {
                field2[x][y] = field[x][y];
                color = (field[x][y] + 1) % MAX_COLOR;
                f = false;
                for (dx = -1; dx <= 1 && !f; dx++) {
                    for (dy = -1; dy <= 1 && !f; dy++) {
                        x2 = x + dx;
                        y2 = y + dy;
                        if (color == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                            f = true;
                        }
                    }
                }
            }
        }
        slowUpdate(0, 0, 1, height - 1);
        slowUpdate(width - 2, 0, width - 1, height - 1);
        slowUpdate(0, 0, width - 1, 1);
        slowUpdate(0, height - 2, width - 1, height - 1);
        int [][] t = field;
        field = field2;
        field2 = t;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                colors[y * width + x] = palette[field[x][y]];
            }
        }
        canvas.scale((float)realWidth / width, (float)realHeight / height);
        canvas.drawBitmap(colors, 0, width, 0, 0, width, height, false, paint);
    }
}
