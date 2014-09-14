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
    Paint paint = new Paint();
    int width = 240;
    int height = 320;
    int[][] field = new int[width][height], field2 = new int[width][height], field3 = new int[width][height];
    float scaleX, scaleY;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    int[] colors = new int[width * height];
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
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scaleX = (float) w / width;
        scaleY = (float) h / height;

        initField();
    }

    void initField() {
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                field2[x][y] = field[x][y];
                int probColor = field[x][y] + 1;
                boolean checker = true;
                if (probColor >= MAX_COLOR) probColor -= MAX_COLOR;
                for (int dx = -1; dx <= 1 && checker; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if (probColor == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                            checker = false;
                            break;
                        }
                    }
                }
            }
        }
        field3 = field;
        field = field2;
        field2 = field3;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int counter = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                colors[counter++] = palette[field[x][y]];
            }
        }
        canvas.scale(scaleX, scaleY);
        canvas.drawBitmap(colors, 0, width, 0, 0, width, height, false, null);
    }
}
