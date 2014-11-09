package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 *
 */
class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null, field2 = null;
    int [] colors = null;
    final int width = 240, height = 320;
    Bitmap bitmap = null;
    Rect rect = null;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

    public WhirlView(Context context) {
        super(context);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        holder = getHolder();
        initField();
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
        long startTime, finishTime, t1;
        while (running) {
            if (holder.getSurface().isValid()) {
                startTime = System.nanoTime();
                updateField();
                t1 = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                finishTime = System.nanoTime();

                Log.i("TIME", "cycle time: " + (finishTime - startTime) / 1000000 + "ms (" + (t1 - startTime) / 1000000 + "ms for updating field)");
                // 45ms for updating, 10ms for drawing (on device explay alto with screen resolution 480x800)
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        rect = new Rect(0, 0, w, h);
    }

    void initField() {
        colors = new int[width * height];
        field  = new int[width][height];
        field2 = new int[width][height];

        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }
    void updatePoint(int x, int y) {
        int color = field[x][y] + 1;
        if (color == MAX_COLOR) color = 0;
        for (int dx = -1; dx <= 1; dx++) {
            int x2 = x + dx;
            if (x2 < 0) x2 += width;
            if (x2 >= width)  x2 -= width;
            for (int dy = -1; dy <= 1; dy++) {
                int y2 = y + dy;
                if (y2 < 0) y2 += height;
                if (y2 >= height) y2 -= height;

                if (color == field[x2][y2]) {
                    field2[x][y] = field[x2][y2];
                }
            }
        }
    }
    void updateField() {
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int color = field[x][y] + 1;
                if (color == MAX_COLOR) color = 0;

                for (int x2 = x - 1; x2 <= x + 1; x2++) {
                    for (int y2 = y - 1; y2 <= y + 1; y2++) {
                        if (color == field[x2][y2]) {
                            field2[x][y]= field[x2][y2];
                        }
                    }
                }
            }
        }
        for (int x = 0; x < width; x++) {
            updatePoint(x, 0);
            updatePoint(x, height - 1);
        }
        for (int y = 0; y < height; y++) {
            updatePoint(0, y);
            updatePoint(width - 1, y);
        }

        int[][] temp = field;
        field = field2;
        field2 = temp;

        int next = 0;
        for (int y = 0; y < height; y++) {
            for (int x=0; x < width; x++) {
                colors[next++] = palette[field[x][y]];
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        bitmap.setPixels(colors, 0, width, 0, 0, width, height);
        canvas.drawBitmap(bitmap, null, rect, null);
    }
}
