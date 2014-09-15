package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Paint;

import java.util.Random;

class WhirlView extends SurfaceView implements Runnable {
    Paint paint = new Paint();
    Canvas canvas;
    int width = 240;
    int height = 320;
    int [][] field = new int[width][height];
    int [][] field2 = new int[width][height];
    int [][] buf = new int[width][height];
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    int[] colours = new int[width * height];
    float xScale;
    float yScale;
    float fps = 0;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
        paint.setTextSize(20);
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
                render(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                fps = 1000.0f / (float)((finishTime - startTime) / 1000000.0);
                Log.i("FPS", "FPS = " +  fps);
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        xScale = ((float) w) / width;
        yScale = ((float) h) / height;
        initField();
    }

    void initField() {
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field2[x][y] = field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                boolean changed = false;
                field2[x][y] = field[x][y];

                for (int dx=-1; dx<=1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = (x + dx) % width;
                        int y2 = (y + dy) % height;
                        if (x2<0) x2 += width;
                        if (y2<0) y2 += height;
                        if ( (field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                            changed = true;
                            break;
                        }
                    }
                    if (changed) break;
                }
            }
        }
        buf = field;
        field = field2;
        field2 = buf;
    }

    public void render(Canvas canvas) {
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                colours[x + y * width] = palette[field[x][y]];
        canvas.scale(xScale, yScale);
        canvas.drawBitmap(colours, 0, width, 0, 0, width, height, false, null);
        canvas.drawText("FPS = " + String.format("%.1f", fps), 20, 20, paint);
    }
}