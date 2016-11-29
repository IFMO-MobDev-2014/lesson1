package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by serejke on 15/09/14.
 */

class WhirlView extends SurfaceView implements Runnable {
    static final int width = 240;
    static final int height = 320;
    static final int MAX_COLOR = 10;
    static final int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    static final int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
    static final int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    int field1[] = new int[width * height];
    int field2[] = new int[width * height];

    boolean usingFirst;

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

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        initField();
    }

    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                updateField();
                drawOnCanvas(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("FPS", "current: " + (1e9 / (finishTime - startTime)));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    void initField() {
        Random rand = new Random();
        for (int pos = 0; pos < width * height; pos++) {
            field1[pos] = palette[rand.nextInt(MAX_COLOR)];
        }
        usingFirst = true;
    }

    int getNextColor(int color) {
        for (int i = 0; i < palette.length; i++)
            if (color == palette[i])
                return palette[(i + 1) % palette.length];
        return 0;
    }

    void updateField() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pos1 = x * height + y;
                int nxtColor = getNextColor(usingFirst ? field1[pos1] : field2[pos1]);
                if (usingFirst)
                    field2[pos1] = field1[pos1];
                else
                    field1[pos1] = field2[pos1];
                for (int k = 0; k < dx.length; k++) {
                    int x2 = x + dx[k];
                    int y2 = y + dy[k];
                    if (x2 < 0) x2 += width;
                    if (y2 < 0) y2 += height;
                    if (x2 >= width) x2 -= width;
                    if (y2 >= height) y2 -= height;
                    int pos2 = x2 * height + y2;
                    if (usingFirst) {
                        if (field1[pos2] == nxtColor) {
                            field2[pos1] = field1[pos2];
                            break;
                        }
                    } else {
                        if (field2[pos2] == nxtColor) {
                            field1[pos1] = field2[pos2];
                            break;
                        }
                    }
                }
            }
        }
        usingFirst = !usingFirst;
    }

    public void drawOnCanvas(Canvas canvas) {
        canvas.scale(canvas.getWidth() * 1f / height, canvas.getHeight() * 1f / width);
        if (usingFirst)
            canvas.drawBitmap(field2, 0, height, 0, 0, height, width, false, null);
        else
            canvas.drawBitmap(field1, 0, height, 0, 0, height, width, false, null);
    }
}
