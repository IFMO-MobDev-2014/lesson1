package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by Freemahn on 13/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    final int MAX_COLOR = 10;
    Paint[] paints = new Paint[MAX_COLOR];
    final int width = 240;
    final int height = 320;
    int[][] field = null;
    int[][] list = null;
    int scaleX = 0;
    int scaleY = 0;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    Canvas canvas;
    volatile long time;
    long allTime;
    int frames;
    int[] pixels;

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
                canvas = holder.lockCanvas();
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                time = (finishTime - startTime) / 1000000;
                frames++;
                allTime += time;
                Log.i("FPS", "allFPS: " + (frames * 1000.0 / allTime) + " " + "currentFPS: " + (1000.0 / time));
                //Log.i("FPS", "currentFPS: " + (1000.0 / time));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {
                }
            }
        }

    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scaleX = 4;
        scaleY = 4;
        initField();
    }

    void initField() {
        field = new int[width][height];
        list = new int[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
        for (int i = 0; i < MAX_COLOR; i++) {
            paints[i] = new Paint();
            paints[i].setColor(palette[i]);
        }
        pixels = new int[width * height];


    }

    void updateField() {

        int[][] field2 = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field2[x][y] = field[x][y];
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ((field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];

                        }
                    }
                }
            }
        }
        field = field2;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(scaleX, scaleY);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[x + y * width] = palette[field[x][y]];
            }
        }
        canvas.drawBitmap(pixels, 0, width, 0, 0, width, height, false, null);
        /*for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                canvas.drawRect(x * scale, y * scale, (x + 1) * scale, (y + 1) * scale, paints[field[x][y]]);
            }
        }*/
    }


}
