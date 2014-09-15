package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {

    int x, y, tmp;
    int width = 240;
    int height = 320;

    int[][] field = new int[width][height];
    int[][] field2 = new int[width][height];

    boolean flag = true;
    float widthScale, heightScale;
    int[] colors = new int[width * height];

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
                draw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        widthScale = w / (float)width;
        heightScale = h / (float)height;
        initField();
    }

    void initField() {
        Random rand = new Random();
        for (x = 0; x < width; x++) {
            for (y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (x = 0; x < width; x++) {
            for (y = 0; y < height; y++) {

                field2[x][y] = field[x][y];

                flag = true;
                tmp = field[x][y] + 1;
                if (tmp >= MAX_COLOR)
                    tmp -= MAX_COLOR;
                if (x - 1 >= 0 && y - 1 >= 0 && tmp == field[x - 1][y - 1])
                    field2[x][y] = field[x - 1][y - 1];
                if (y - 1 >= 0 && tmp == field[x][y - 1])
                    field2[x][y] = field[x][y - 1];
                if (x + 1 < width && y - 1 >= 0 && tmp == field[x + 1][y - 1])
                    field2[x][y] = field[x + 1][y - 1];
                if (x - 1 >= 0 && tmp == field[x - 1][y])
                    field2[x][y] = field[x - 1][y];
                if (x + 1 < width && tmp == field[x + 1][y])
                    field2[x][y] = field[x + 1][y];
                if (x - 1 >= 0 && y + 1 < height && tmp == field[x - 1][y + 1])
                    field2[x][y] = field[x - 1][y + 1];
                if (y + 1 < height && tmp == field[x][y + 1])
                    field2[x][y] = field[x][y + 1];
                if (x + 1 < width && y + 1 < height && tmp == field[x + 1][y + 1])
                    field2[x][y] = field[x + 1][y + 1];
            }
        }
        int[][] t = field;
        field = field2;
        field2 = t;
    }

    @Override
    public void draw(Canvas canvas)
    {
        for (x = 0; x < width; x++) {
            for (y = 0; y < height; y++) {
                colors[y * width + x] = palette[field[x][y]];
            }
        }

        canvas.scale(widthScale, heightScale);
        canvas.drawBitmap(colors, 0, width, 0, 0, width, height, false, null);
    }
}