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
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
                field2[x][y] = field[x][y];
            }
        }
    }

    void updateField() {
        
        for (int x = 0; x < width; x++) {
            if (field[x][0] == MAX_COLOR - 1) {
                if (field[x][1] == 0) {
                    field2[x][0] = field[x][1];
                }
                if (field[x][height - 1] == 0) {
                    field2[x][0] = field[x][height - 1];
                }
            } else {
                if (field[x][0] + 1 == field[x][1]) {
                    field2[x][0] = field[x][1];
                }
                if (field[x][0] + 1 == field[x][height - 1]) {
                    field2[x][0] = field[x][height - 1];
                }
            }
            if (field[x][height - 1] == MAX_COLOR - 1) {
                if (field[x][height - 2] == 0) {
                    field2[x][height - 1] = field[x][height - 2];
                }
                if (field[x][0] == 0) {
                    field2[x][height - 2] = field[x][0];
                }
            } else {
                if (field[x][height - 1] + 1 == field[x][height - 2]) {
                    field2[x][height - 1] = field[x][height - 2];
                }
                if (field[x][height - 1] + 1 == field[x][0]) {
                    field2[x][height - 2] = field[x][0];
                }
            }
        }

        for (int y = 0; y < height; y++) {
            if (field[0][y] == MAX_COLOR - 1) {
                if (field[1][y] == 0) {
                    field2[0][y] = field[1][y];
                }
                if (field[width - 1][y] == 0) {
                    field2[0][y] = field[width - 1][y];
                }
            } else {
                if (field[0][y] + 1 == field[1][y]) {
                    field2[0][y] = field[1][y];
                }
                if (field[0][y] + 1 == field[width - 1][y]) {
                    field2[0][y] = field[width - 1][y];
                }
            }
            if (field[width - 1][y] == MAX_COLOR - 1) {
                if (field[width - 2][y] == 0) {
                    field2[width - 1][y] = field[width - 2][y];
                }
                if (field[0][y] == 0) {
                    field2[width - 1][y] = field[0][y];
                }
            } else {
                if (field[width - 1][y] + 1 == field[width - 2][y]) {
                    field2[width - 1][y] = field[width - 2][y];
                }
                if (field[width - 1][y] + 1 == field[0][y]) {
                    field2[width - 1][y] = field[0][y];
                }
            }
        }
        
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (field[x][y] == MAX_COLOR - 1) {
                    if (field[x - 1][y - 1] == 0) {
                        field2[x][y] = field[x - 1][y - 1];
                    }
                    if (field[x - 1][y] == 0) {
                        field2[x][y] = field[x - 1][y];
                    }
                    if (field[x - 1][y + 1] == 0) {
                        field2[x][y] = field[x - 1][y + 1];
                    }
                    if (field[x][y - 1] == 0) {
                        field2[x][y] = field[x][y - 1];
                    }
                    if (field[x][y + 1] == 0) {
                        field2[x][y] = field[x][y + 1];
                    }
                    if (field[x + 1][y - 1] == 0) {
                        field2[x][y] = field[x + 1][y - 1];
                    }
                    if (field[x + 1][y] == 0) {
                        field2[x][y] = field[x + 1][y];
                    }
                    if (field[x + 1][y + 1] == 0) {
                        field2[x][y] = field[x + 1][y + 1];
                    }
                } else {
                    if (field[x][y] + 1 == field[x - 1][y - 1]) {
                        field2[x][y] = field[x - 1][y - 1];
                    }
                    if (field[x][y] + 1 == field[x - 1][y]) {
                        field2[x][y] = field[x - 1][y];
                    }
                    if (field[x][y] + 1 == field[x - 1][y + 1]) {
                        field2[x][y] = field[x - 1][y + 1];
                    }
                    if (field[x][y] + 1 == field[x][y - 1]) {
                        field2[x][y] = field[x][y - 1];
                    }
                    if (field[x][y] + 1 == field[x][y + 1]) {
                        field2[x][y] = field[x][y + 1];
                    }
                    if (field[x][y] + 1 == field[x + 1][y - 1]) {
                        field2[x][y] = field[x + 1][y - 1];
                    }
                    if (field[x][y] + 1 == field[x + 1][y]) {
                        field2[x][y] = field[x + 1][y];
                    }
                    if (field[x][y] + 1 == field[x + 1][y + 1]) {
                        field2[x][y] = field[x + 1][y + 1];
                    }
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
