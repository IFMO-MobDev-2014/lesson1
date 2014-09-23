package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 */

final
class WhirlView extends SurfaceView implements Runnable {
    final int initialWidth = 240;
    final int initialHeight = 320;

    int width = initialWidth;
    int height = initialHeight;

    int[][] field = new int[width][height];
    int[][] field2 = new int[width][height];
    int[] colors = new int[width * height];
    float scaleW = 1, scaleH = 1;

    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

    Random rand = new Random();


    public WhirlView(Context context) {
        super(context);
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
        } catch (InterruptedException ignore) {
        }
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
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scaleW = (float)w / width;
        scaleH = (float)h / height;
    }

    void initField() {
        for (int x = 0;  x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
                colors[y * width + x] = palette[field[x][y]];
            }
        }
    }

    void updateField() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field2[x][y] = field[x][y];
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0)
                            x2 += width;
                        if (y2 < 0)
                            y2 += height;
                        if (x2 >= width)
                            x2 -= width;
                        if (y2 >= height)
                            y2 -= height;
                        if ((field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
                colors[y * width + x] = palette[field2[x][y]];
            }
        }
        int[][] tmp = field;
        field = field2;
        field2 = tmp;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(scaleW, scaleH);
        canvas.drawBitmap(colors, 0, width, 0, 0, width, height, false, null);
    }
}