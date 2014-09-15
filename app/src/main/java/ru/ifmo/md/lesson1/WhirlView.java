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
 * Modified by Alexey Katsman on 15/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    final static int width = 240;
    final static int height = 320;
    int[][] field = new int[width][height];
    int[][] field2 = new int[width][height];
    final static short MAX_COLOR = 10;
    final static Rect src = new Rect(0, 0, width, height);
    Rect dst = null;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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
                Log.i("Frames Per Second", "Circle: " + 1e9 / (double) (finishTime - startTime));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        dst = new Rect(0, 0, w, h);
        initField();
    }

    void initField() {
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
                field2[x][y] = field[x][y];
            }
        }
    }

    void updateField() {
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                int current_color = field[x][y] + 1;
                current_color %= MAX_COLOR;

                int x1 = x == 0 ? width - 1 : x - 1;
                int x3 = x == width - 1 ? 0 : x + 1;
                int y1 = y == 0 ? height - 1 : y - 1;
                int y3 = y == height - 1 ? 0 : y + 1;

                if (current_color == field[x3][y3]) {
                    field2[x][y] = current_color;
                    bitmap.setPixel(x, y, palette[current_color]);
                    continue;
                }

                if (current_color == field[x3][y]) {
                    field2[x][y] = current_color;
                    bitmap.setPixel(x, y, palette[current_color]);
                    continue;
                }

                if (current_color == field[x3][y1]) {
                    field2[x][y] = current_color;
                    bitmap.setPixel(x, y, palette[current_color]);
                    continue;
                }

                if (current_color == field[x][y3]) {
                    field2[x][y] = current_color;
                    bitmap.setPixel(x, y, palette[current_color]);
                    continue;
                }

                if (current_color == field[x][y1]) {
                    field2[x][y] = current_color;
                    bitmap.setPixel(x, y, palette[current_color]);
                    continue;
                }

                if (current_color == field[x1][y3]) {
                    field2[x][y] = current_color;
                    bitmap.setPixel(x, y, palette[current_color]);
                    continue;
                }

                if (current_color == field[x1][y]) {
                    field2[x][y] = current_color;
                    bitmap.setPixel(x, y, palette[current_color]);
                    continue;
                }

                if (current_color == field[x1][y1]) {
                    field2[x][y] = current_color;
                    bitmap.setPixel(x, y, palette[current_color]);
                }
            }
        }

        for (int x = 0; x < width; x++) {
            System.arraycopy(field2[x], 0, field[x], 0, height);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, src, dst, null);
    }
}