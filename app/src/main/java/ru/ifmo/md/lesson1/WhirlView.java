package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

class WhirlView extends SurfaceView implements Runnable {
    int[][][] field = null;
    int[] myField = null;
    private static final int width = 240;
    private static final int height = 320;
    private float scaleW = 1;
    private float scaleH = 1;
    int z = 0;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Bitmap b;
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
        scaleW = (float) w / width;
        scaleH = (float) h / height;
        initField();
    }

    void initField() {
        field = new int[2][width][height];
        myField = new int[width * height];
        b = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[z][x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        boolean flag;
        int color;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[z ^ 1][x][y] = field[z][x][y];
                myField[x + y * width] = palette[field[z][x][y]];
                color = field[z][x][y] + 1;
                if (color == MAX_COLOR) color -= MAX_COLOR;
                flag = true;
                for (int dx = -1; dx <= 1 && flag; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if (color == field[z][x2][y2]) {
                            field[z ^ 1][x][y] = field[z][x2][y2];
                            myField[x + y * width] = palette[field[z][x2][y2]];
                            flag = false;
                            break;
                        }
                    }
                }
            }
        }
        z ^= 1;
    }

    @Override
    public void draw(Canvas canvas) {
        b.setPixels(myField, 0, width, 0, 0, width, height);
        canvas.scale(scaleW, scaleH);
        canvas.drawBitmap(b, 0, 0, null);
    }
}
