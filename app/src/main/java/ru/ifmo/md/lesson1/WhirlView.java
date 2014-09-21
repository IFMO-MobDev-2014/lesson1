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
    final static int WIDTH = 240;
    final static int HEIGHT = 320;
    final static int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    int[][]field2 = new int[WIDTH][HEIGHT];
    Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565); //I think it's better way to work with
    Rect dst; //the rectangle that the bitmap will be scaled to fit into

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
                draw(canvas);
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
        dst = new Rect(0, 0, w, h);
        initField();
    }

    void initField() {
        field = new int[WIDTH][HEIGHT];
        Random rand = new Random();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                field2[x][y] = field[x][y];

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
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
        for (int i = 0; i < WIDTH; i++) {
            field[i] = field2[i].clone();
        }
    }

    public void draw(Canvas canvas) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                bitmap.setPixel(x, y, palette[field[x][y]]);
            }
        }

        canvas.drawBitmap(bitmap, null, dst, null);
    }
}
