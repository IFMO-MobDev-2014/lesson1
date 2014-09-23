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
    final Random RAND = new Random();
    Canvas canvas;
    Bitmap bufferBitmap;

    final int WIDTH = 240;
    final int HEIGHT = 320;

    int[][] field = new int[WIDTH][HEIGHT];
    int[][] field2 = new int[WIDTH][HEIGHT];
    int[] color = new int[WIDTH * HEIGHT];

    Rect displ = null;
    final Rect BITMAP = new Rect(0, 0, WIDTH, HEIGHT);

    long startTime, finishTime;
    int x2, y2;

    final int MAX_COLOR = 10;
    final int[] PALETTE = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};

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
        } catch (InterruptedException ignore) {
        }
    }

    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                startTime = System.nanoTime();

                canvas = holder.lockCanvas();
                updateField();

                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);

                finishTime = System.nanoTime();
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
        displ = new Rect(0, 0, w, h);
        initField();
    }

    void initField() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                field[x][y] = RAND.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                field2[x][y] = field[x][y];
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {

                        x2 = x + dx;
                        y2 = y + dy;

                        if (x2 < 0) x2 += WIDTH;
                        if (y2 < 0) y2 += HEIGHT;
                        if (x2 >= WIDTH) x2 -= WIDTH;
                        if (y2 >= HEIGHT) y2 -= HEIGHT;
                        if ((field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }
        for (int x = 0; x < WIDTH; x++)
            System.arraycopy(field2[x], 0, field[x], 0, HEIGHT);
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int x = 0; x < WIDTH; x++)
            for (int y = 0; y < HEIGHT; y++)
                color[x + WIDTH * y] = PALETTE[field[x][y]];

        bufferBitmap = Bitmap.createBitmap(color, WIDTH, HEIGHT, Bitmap.Config.RGB_565);
        canvas.drawBitmap(bufferBitmap, BITMAP, displ, null);
    }
}

