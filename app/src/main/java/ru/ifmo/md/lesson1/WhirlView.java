package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null;
    Bitmap bitmap;
    Rect src, dst;
    int width = 0, height = 0;
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
                Canvas canvas = holder.lockCanvas();
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        height = 320;
        width = 240;
        if (w > h) {
            int temp = height;
            width = height;
            height = temp;
        }
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        initField(w, h);
    }

    void initField(int w, int h) {
        Random rand = new Random();
        field = new int [width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
        src = new Rect(0, 0, width, height); //Creating our rect
        dst = new Rect(0, 0, w, h);        //Creating device rect
    }

    void updateField() {
        int[][] field2 = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field2[x][y] = field[x][y];
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy =- 1; dy <= 1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ((field[x][y] + 1) % MAX_COLOR == field[x2][y2])
                            field2[x][y] = field[x2][y2];
                    }
                }
            }
        }
        field = field2;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int count = 0;
        int [] color = new int [width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                color[count++] = palette[field[x][y]];
            }
        }
        bitmap.setPixels(color, 0, width, 0, 0, width, height);
        canvas.drawBitmap(bitmap, src, dst, null); //Draw definite bitmap with translating from our rect to device rect
    }
}