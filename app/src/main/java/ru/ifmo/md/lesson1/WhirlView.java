package ru.ifmo.md.lesson1;

import android.graphics.Bitmap;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    final Bitmap.Config config = Bitmap.Config.RGB_565;
    int[][] field;
    int[][] field2;
    int[] color;
    final int width = 320;
    final int height = 240;
    //float scalex = 1;
    //float scaley = 1;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    Paint paint;
    Bitmap bitmap;
    RectF rect;
    Canvas canvas;
    Random rand;
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
        rand = new Random();
        paint = new Paint();
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
                canvas = holder.lockCanvas();
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        rect = new RectF(0, 0, w, h);
        initField();
    }

    void initField() {
        field = new int[width][height];
        field2 = new int[width][height];
        color = new int[width * height];
        bitmap = Bitmap.createBitmap(width, height, config);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int x2, y2;
        for (int x = 0; x < width; x++) {
            System.arraycopy(field[x], 0, field2[x], 0, height);
            for (int y = 0; y < height; y++) {
                a:
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        x2 = (x + dx + width) % width;
                        y2 = (y + dy + height) % height;
                        if ( (field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                            break a;
                        }
                    }
                }
            }
        }
        field = field2;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                color[y * width + x] = palette[field[x][y]];
            }
        }
        bitmap.setPixels(color, 0, width, 0, 0, width, height);
        canvas.drawBitmap(bitmap, null, rect, null);
    }
}
