package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Objects;
import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    int width = 240;
    int height = 320;
    int [][] field = new int[height][width];
    int [][] field2 = new int[height][width];
    private static Object lock = new Object();
    float scaleX = 0;
    float scaleY = 0;
    volatile boolean ready;
    int [] bitmapArray = new int[width * height];
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
//    final int MAX_COLOR = 21;
  //  int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF, 0xFF86E500, 0xFF40E100, 0xFF00DD03, 0xFF00D944, 0xFF00D583, 0xFF00D2BF, 0xFF00A2CE, 0xFF0062CA, 0xFF0024C6, 0xFF1600C2, 0xFF4E00BF};
    final int MAX_COLOR = palette.length;
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
//                try {
//                    Thread.sleep(115);
//                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
            scaleX = (float)w/width;
            scaleY = (float)h/height;
        initField();
    }

    void initField() {
        Random rand = new Random();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                field[y][x] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int x2, y2, dx, dy;
//        Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
        long startTime = System.nanoTime();
        for (int x = 0; x < width; x++) {
            int y = 0;
            field2[y][x] = field[y][x];

            for (dx = -1; dx <= 1; dx++) {
                for (dy = -1; dy <= 1; dy++) {
                    x2 = x + dx;
                    y2 = y + dy;
                    if (x2 < 0) x2 += width;
                    if (y2 < 0) y2 += height;
                    if (x2 >= width) x2 -= width;
                    if (y2 >= height) y2 -= height;
                    if ( (field[y][x] + 1) % MAX_COLOR == field[y2][x2]) {
                        field2[y][x] = field[y2][x2];
                        bitmapArray[y * width + x] = palette[field2[y][x]];
                    }
                }
            }
        }

        for (int x = 0; x < width; x++) {
            int y = height - 1;
            field2[y][x] = field[y][x];

            for (dx = -1; dx <= 1; dx++) {
                for (dy = -1; dy <= 1; dy++) {
                    x2 = x + dx;
                    y2 = y + dy;
                    if (x2 < 0) x2 += width;
                    if (y2 < 0) y2 += height;
                    if (x2 >= width) x2 -= width;
                    if (y2 >= height) y2 -= height;
                    if ( (field[y][x] + 1) % MAX_COLOR == field[y2][x2]) {
                        field2[y][x] = field[y2][x2];
                        bitmapArray[y * width + x] = palette[field2[y][x]];
                    }
                }
            }
        }

        for (int y = 0; y < height; y++) {
            int x = 0;
            field2[y][x] = field[y][x];
            for (dx = -1; dx <= 1; dx++) {
                for (dy = -1; dy <= 1; dy++) {
                    x2 = x + dx;
                    y2 = y + dy;
                    if (x2 < 0) x2 += width;
                    if (y2 < 0) y2 += height;
                    if (x2 >= width) x2 -= width;
                    if (y2 >= height) y2 -= height;
                    if ( (field[y][x] + 1) % MAX_COLOR == field[y2][x2]) {
                        field2[y][x] = field[y2][x2];
                        bitmapArray[y * width + x] = palette[field2[y][x]];
                    }
                }
            }
        }

        for (int y = 0; y < height; y++) {
            int x = width - 1;
            field2[y][x] = field[y][x];

            for (dx = -1; dx <= 1; dx++) {
                for (dy = -1; dy <= 1; dy++) {
                    x2 = x + dx;
                    y2 = y + dy;
                    if (x2 < 0) x2 += width;
                    if (y2 < 0) y2 += height;
                    if (x2 >= width) x2 -= width;
                    if (y2 >= height) y2 -= height;
                    if ( (field[y][x] + 1) % MAX_COLOR == field[y2][x2]) {
                        field2[y][x] = field[y2][x2];
                        bitmapArray[y * width + x] = palette[field2[y][x]];
                    }
                }
            }
        }
        long finishTime = System.nanoTime();
        Log.i("CYCLE1", "Circle: " + (finishTime - startTime) / 1000000);
        startTime = System.nanoTime();
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                field2[y][x] = field[y][x];
                int nextColor = (field[y][x] + 1);
                if (nextColor >= MAX_COLOR)
                    nextColor -= MAX_COLOR;
                if (nextColor == field[y - 1][x - 1])
                    field2[y][x] = field[y - 1][x - 1];
                else if (nextColor == field[y][x - 1])
                    field2[y][x] = field[y][x - 1];
                else if (nextColor == field[y + 1][x - 1])
                    field2[y][x] = field[y + 1][x - 1];
                else if (nextColor == field[y - 1][x])
                    field2[y][x] = field[y - 1][x];
                else if (nextColor == field[y][x])
                    field2[y][x] = field[y][x];
                else if (nextColor == field[y + 1][x])
                    field2[y][x] = field[y + 1][x];
                else if (nextColor == field[y - 1][x + 1])
                    field2[y][x] = field[y - 1][x + 1];
                else if (nextColor == field[y][x + 1])
                    field2[y][x] = field[y][x + 1];
                else if (nextColor == field[y + 1][x + 1])
                    field2[y][x] = field[y + 1][x + 1];
                bitmapArray[y * width + x] = palette[field2[y][x]];
            }
        }
        finishTime = System.nanoTime();
        Log.i("CYCLE2", "Circle: " + (finishTime - startTime) / 1000000);
        int[][] tmp = field;
        field = field2;
        field2 = tmp;
    }

    @Override
    public void onDraw(Canvas canvas) {
        long startTime = System.nanoTime();
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                bitmapArray[y * width + x] = palette[field[y][x]];
//               // tmpPaint.setColor(palette[field[x][y]]);
//               // canvas.drawRect(x*scaleX, y*scaleY, (x + 1)*scaleX, (y + 1)*scaleY, tmpPaint);
//            }
//        }
        canvas.scale(scaleX, scaleY);
        canvas.drawBitmap(bitmapArray, 0, width, 0, 0, width, height, false, null);
        long finishTime = System.nanoTime();
        Log.i("onDrawCycle", "" + (finishTime - startTime) / 1000000);
    }
}
