package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    final int MAX_COLOR = 10;
    int[][] field = null;
    int[][] field2 = null;
    int width = 360;
    int height = 240;
    int scale = 4;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    Paint paint = new Paint();
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
                String fps = String.format("%.4f", 1000.0 / ((finishTime - startTime) / 1000000));
                Log.i("TIME", "Circle: " + fps + " FPS");
//                try {
//                    Thread.sleep(16);
//                } catch (InterruptedException ignore) {
//                }
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w / scale;
        height = h / scale;
        initField();
    }

    void initField() {
        field = new int[width][height];
        field2 = new int[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x = 0; x < width; x++) {
            System.arraycopy(field[x], 0, field2[x], 0, height);
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if ((dx == 0) && (dy == 0)) {
                    continue;
                }
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int x2 = (x + dx + width) % width;
                        int y2 = (y + dy + height) % height;
                        if ((field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }
        for (int x = 0; x < width; x++) {
            System.arraycopy(field2[x], 0, field[x], 0, height);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                paint.setColor(palette[field[x][y]]);
                int left = x * scale;
                int top = y * scale;
                canvas.drawRect(left, top, left + scale, top + scale, paint);
                paint.setColor(0);
            }
        }
    }
}
