package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Arrays;
import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null;
    int width = 0;
    int height = 0;
    int scale = 4;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    int [][] colors = null;
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    Bitmap basic = null, scaled = null;


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
                long a = (finishTime - startTime) / 1000000;
                Log.i("TIME", "Circle: " + a);
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w/scale;
        height = h/scale;
        initBitmapsAndColorMap();
        initField();
    }

    void initBitmapsAndColorMap() {
        basic = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        field = new int[width][height];
        colors = new int[height][width];
    }

    void initField() {
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
                colors[y][x] = palette[field[x][y]];
            }
        }
    }

    void updateField() {
        int [][] field2 = new int[width][height];
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field2[x][y] = field[x][y];

                for (int dx=-1; dx<=1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        x2 = (x2 + width) % width;
                        y2 = (y2 + height) % height;
                        if ( (field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
                colors[y][x] = palette[field2[x][y]];
            }
        }
        field = field2;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for(int i  = 0; i < height; i++) {
            basic.setPixels(colors[i], 0, width, 0, i, width, 1);
        }
        scaled = Bitmap.createScaledBitmap(basic, width * scale, height * scale, false);
        canvas.drawBitmap(scaled, 0, 0, null);
    }
}
