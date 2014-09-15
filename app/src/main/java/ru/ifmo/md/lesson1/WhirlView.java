package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null;
    int width = 240;
    int height = 320;
    float scaleWidth = 1, scaleHeight = 1;
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Rect rect;
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
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        rect = new Rect(0, 0, w, h);
        scaleWidth = (float) w / width;
        scaleHeight = (float) h / height;
        initField();
    }

    void initField() {
        field = new int[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int [][] field2 = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                field2[x][y] = field[x][y];

                cycle: for (int dx = -1; dx <= 1; dx++) {
                    for (int dy =- 1; dy <= 1; dy++) {
                        if (dy == 0 && dx == 0 ) continue;
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (y2 >= height) y2 -= height;
                        if ( (field[x][y]+1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                            break cycle;
                        }
                    }
                }
            }
        }
        field = field2;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, palette[field[x][y]]);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.scale(scaleWidth, scaleHeight);
        canvas.drawBitmap(bitmap, null, rect, null);
    }
}