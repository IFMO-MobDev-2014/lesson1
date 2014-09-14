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
    int[][] field = null;
    int deviceWidth = 0;
    int deviceHeight = 0;
    int width = 240;
    int height = 320;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
//    final int MAX_COLOR = 16;
//    int[] palette = {0xFF000000, 0xFF111111, 0xFF222222, 0xFF333333, 0xFF444444, 0xFF555555, 0xFF666666, 0xFF777777, 0xFF888888, 0xFF999999,
//            0xFFAAAAAA, 0xFFBBBBBB, 0xFFCCCCCC, 0xFFDDDDDD, 0xFFEEEEEE, 0xFFFFFFFF};
    Rect initRect;
    Rect deviceRect;
    Bitmap b;
    Bitmap[] bitmaps = new Bitmap[MAX_COLOR];
    int numOfBitmaps = 0;
    int curBitmap = 0;
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    boolean cycle = false;

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
//                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
//                long finishTime = System.nanoTime();
//                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000 + " " + cycle);
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        deviceWidth = w;
        deviceHeight = h;
        if (w < h && width > height || w > h && width < height) {
            int t = width;
            width = height;
            height = t;
        }
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
        initRect = new Rect(0, 0, width - 1, height - 1);
        deviceRect = new Rect(0, 0, deviceWidth - 1, deviceHeight - 1);
        cycle = false;
        numOfBitmaps = 0;
        curBitmap = 0;
    }

    void updateField() {
        if (numOfBitmaps == MAX_COLOR)
            return;
        if (cycle) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    field[x][y] = (field[x][y] + 1) % MAX_COLOR;
                }
            }
            return;
        }
        int[][] field2 = new int[width][height];
        int c = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                field2[x][y] = field[x][y];

                outer: for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ((field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                            c++;
                            break outer;
                        }
                    }
                }
            }
        }
        field = field2;
        if (c == width * height) {
            cycle = true;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (numOfBitmaps == MAX_COLOR) {
            canvas.drawBitmap(bitmaps[curBitmap], initRect, deviceRect, null);
            curBitmap = (curBitmap + 1) % MAX_COLOR;
            return;
        }
        int[] colors = new int[width * height];
        int c = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                colors[c++] = palette[field[x][y]];
            }
        }
        b = Bitmap.createBitmap(colors, width, height, Bitmap.Config.RGB_565);
        if (cycle) {
            bitmaps[numOfBitmaps++] = b;
        }
        canvas.drawBitmap(b, initRect, deviceRect, null);
    }
}
