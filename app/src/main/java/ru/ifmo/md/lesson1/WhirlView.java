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
    int[][] field = null;
    int[][] field2;
    int[] color;

    Random rand = new Random();
    Canvas canvas;
    Bitmap bufferBitmap;

    Rect displ;
    Rect bm;

    int width = 0;
    int height = 0;
    long startTime, finishTime;

    int x2, y2;
    int scale = 4;

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
        } catch (InterruptedException ignore) {
        }
    }

    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                startTime = System.nanoTime();

                canvas = holder.lockCanvas();
                updateField();

                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);

                finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w / scale;
        height = h / scale;

        bm = new Rect(0, 0, width, height);
        displ = new Rect(0, 0, w, h);

        initField();
    }

    void initField() {
        field = new int[width][height];
        field2 = new int[width][height];
        color = new int[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                field2[x][y] = field[x][y];

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {

                        x2 = x + dx;
                        y2 = y + dy;

                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ((field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }
        for (int x = 0; x < width; x++) {
            System.arraycopy(field2[x], 0, field[x], 0, height);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                color[x + width * y] = palette[field[x][y]];

        bufferBitmap = Bitmap.createBitmap(color, width, height, Bitmap.Config.RGB_565);
        canvas.drawBitmap(bufferBitmap, bm, displ, null);
    }
}

