package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 * Modified by lightning95.
 */

class WhirlView extends SurfaceView implements Runnable {
    final int MAX_COLOR = 10;
    final Bitmap.Config CONFIG = Bitmap.Config.RGB_565;
    final int WIDTH = 240;
    final int HEIGHT = 320;
    final int[] PALETTE = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000,
            0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};

    int[][] field;
    int[][] field2;
    int[] color;

    Paint paint;
    Bitmap bitmap;
    RectF rectF;
    Canvas canvas;
    Random rand;

    SurfaceHolder holder;
    Thread thread;
    volatile boolean running;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
        rand = new Random();
        paint = new Paint();
        paint.setTextSize(100);
        paint.setColor(Color.WHITE);
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
        long startTime, finishTime, fps = 0;
        while (running) {
            if (holder.getSurface().isValid()) {
                startTime = System.nanoTime();
                canvas = holder.lockCanvas();
                updateField();
                drawField(canvas);
                canvas.drawText("FPS : " + fps, 100, 100, paint);
                holder.unlockCanvasAndPost(canvas);
                finishTime = System.nanoTime();
                fps = 1000000000 / (finishTime - startTime);
                Log.i("TIME", "FPS: " + fps);
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        rectF = new RectF(0, 0, w, h);
        initField();
    }

    void initField() {
        field = new int[WIDTH][HEIGHT];
        field2 = new int[WIDTH][HEIGHT];
        color = new int[WIDTH * HEIGHT];
        bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, CONFIG);

        for (int x = 0; x < WIDTH; ++x) {
            for (int y = 0; y < HEIGHT; ++y) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x = 0; x < WIDTH; ++x) {
            System.arraycopy(field[x], 0, field2[x], 0, HEIGHT);
            for (int y = 0; y < HEIGHT; ++y) {
                lazy:
                for (int dx = 1; dx >= -1; --dx) {
                    for (int dy = 1; dy >= -1; --dy) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += WIDTH;
                        if (y2 < 0) y2 += HEIGHT;
                        if (x2 >= WIDTH) x2 -= WIDTH;
                        if (y2 >= HEIGHT) y2 -= HEIGHT;
                        if ((field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                            break lazy;
                        }
                    }
                }
            }
        }
        for (int x = 0; x < WIDTH; ++x) {
            System.arraycopy(field2[x], 0, field[x], 0, HEIGHT);
        }
    }

    public void drawField(Canvas canvas) {
        for (int y = 0; y < HEIGHT; ++y) {
            for (int x = 0; x < WIDTH; ++x) {
                color[y * WIDTH + x] = PALETTE[field[x][y]];
            }
        }
        bitmap.setPixels(color, 0, WIDTH, 0, 0, WIDTH, HEIGHT);
        canvas.drawBitmap(bitmap, null, rectF, null);
    }
}
