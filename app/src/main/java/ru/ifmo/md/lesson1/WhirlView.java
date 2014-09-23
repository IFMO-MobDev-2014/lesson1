package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/

class WhirlView extends SurfaceView implements Runnable {
    public static final int WIDTH = 320;
    public static final int HEIGHT = 240;
    int [][] field = null;
    int [][] field2 = null;
    int [][] swap_field = null;
    int [] colors = new int [WIDTH * HEIGHT];
    int width;
    int height;
    Rect src = new Rect();
    Rect dst = new Rect();
    Bitmap image;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF3300, 0xFFFF6600, 0xFFFFCC66, 0xFFFF66, 0xFF99FF66, 0xFF99FF99, 0xFF00FFCC, 0xFF003366, 0xFF66CCCC, 0xFFCC3399};
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
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        if (w > h) {
            width = WIDTH;
            height = HEIGHT;
        } else {
            width = HEIGHT;
            height = WIDTH;
        }

        src.set(0, 0, width, height);
        dst.set(0, 0, w, h);
        if (image != null)
            image.recycle();
        image = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        initField();
    }

    void initField() {
        field = new int[width][height];
        field2 = new int[width][height];

        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
                colors[x + y*width] = palette[field[x][y]];
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
                        if ((field[x][y]+1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
                colors[x + y*width] = palette[field2[x][y]];
            }
        }

        swap_field = field;
        field = field2;
        field2 = swap_field;
    }

    @Override
    public void onDraw(Canvas canvas) {
        image.setPixels(colors, 0, width, 0, 0, width, height);
        canvas.drawBitmap(image, src, dst, null);
    }
}
