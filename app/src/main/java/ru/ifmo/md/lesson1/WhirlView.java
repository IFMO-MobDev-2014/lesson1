package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 * Modified by heat-wave on 17/11/14.
 */

class WhirlView extends SurfaceView implements Runnable {
    int [] field = null;
    final int WIDTH = 240;
    final int HEIGHT = 320;
    final int MAX_COLOR = 10;
    int [] sc = new int[WIDTH * HEIGHT];
    int [] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    Bitmap screen;
    SurfaceHolder holder;
    Thread thread = null;
    final RectF rect = new RectF(0, 0, 1080, 1920);
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
                double fps = 1000000000.0 / (finishTime - startTime);
                Log.i("TIME", "FPS: "  + fps);
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        screen = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565);
        initField();
    }

    void initField() {
        field = new int[WIDTH * HEIGHT];
        Random rand = new Random();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                field[x + y * WIDTH] = rand.nextInt(MAX_COLOR);
                sc[x + y * WIDTH] = palette[field[x + y * WIDTH]];
            }
        }
        screen.setPixels(sc, 0, WIDTH, 0,0, WIDTH, HEIGHT);
    }

    void updateField() {
        new Thread(new Runnable() {
            public void run() {
                int[] field2 = new int[WIDTH * HEIGHT];
                for (int x = 0; x < WIDTH; x++) {
                    for (int y = 0; y < HEIGHT; y++) {
                        field2[x + y * WIDTH] = field[x + y * WIDTH];
                        for (int dx =- 1; dx <= 1; dx++) {
                            int x2 = x + dx;
                            if (x2 < 0) x2 += WIDTH;
                            if (x2 >= WIDTH) x2 -= WIDTH;
                            for (int dy= -1; dy <= 1; dy++) {
                                int y2 = y + dy;
                                if (y2 < 0) y2 += HEIGHT;
                                if (y2 >= HEIGHT) y2 -= HEIGHT;
                                if ((field[x + y * WIDTH] + 1) % MAX_COLOR == field[x2 + y2 * WIDTH]) {
                                    field2[x + y * WIDTH] = field[x2 + y2 * WIDTH];
                                    sc[x + y * WIDTH] = palette[field2[x + y * WIDTH]];
                                }
                            }
                        }
                    }
                }
                field = field2;
                screen.setPixels(sc, 0, WIDTH, 0,0, WIDTH, HEIGHT);
            }
        }).start();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(screen, null, rect, null);
    }
}