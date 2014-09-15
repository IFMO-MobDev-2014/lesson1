package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 * Modified by vlad107 on 15/09/14.
 */

class WhirlView extends SurfaceView implements Runnable {
    static final int WIDTH = 240;
    static final int HEIGHT = 320;
    static final Random RAND = new Random();
    int[][] field = new int[WIDTH][HEIGHT];
    int[][] field2 = new int[WIDTH][HEIGHT];
    int[] colors = new int[WIDTH * HEIGHT];
    static final int MAX_COLOR = 10;
    Canvas canvas;
    float scaleX = 1;
    float scaleY = 1;
    int[] PALETTE = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
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
        double average = 0;
        int cnt = 0;
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                canvas = holder.lockCanvas();
                updateField();
                onDraw();
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                double fps = 1000.0 / ((finishTime - startTime) / 1000000);
                String res = String.format("%.1f", fps);
                average += fps;
                cnt++;
                Log.i("TIME", "Circle: " + res + " FPS");
            }
        }
        average /= cnt;
        Log.i("TIME", "Average result: " + average + " FPS");
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scaleX = ((float) (w)) / WIDTH;
        scaleY = ((float) (h)) / HEIGHT;
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
                found:
                for (int  dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if ((dx == 0) && (dy == 0)) {
                            continue;
                        }
                        int x2 = (x + dx + WIDTH) % WIDTH;
                        int y2 = (y + dy + HEIGHT) % HEIGHT;
                        if ((field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                            break found;
                        }
                    }
                }
            }
        }
        for (int x = 0; x < WIDTH; x++) {
            System.arraycopy(field2[x], 0, field[x], 0, HEIGHT);
        }
    }

    public void onDraw() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                colors[y * WIDTH + x] = PALETTE[field[x][y]];
            }
        }
        canvas.scale(scaleX, scaleY);
        canvas.drawBitmap(colors, 0, WIDTH, 0, 0, WIDTH, HEIGHT, false, null);
    }
}
