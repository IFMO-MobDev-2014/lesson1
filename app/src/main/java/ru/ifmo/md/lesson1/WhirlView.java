package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    byte [][][] field           = null;
    byte currentBuffer          = 0;
    int scale                   = 0;
    final int MAX_COLOR         = 10;
    final int WIDTH             = 240;
    final int HEIGHT            = 320;
    Bitmap bitmap               = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
    int[] colors                = new int[WIDTH * HEIGHT];
    int[] palette               = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00,
            0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder        = null;
    Thread thread               = null;
    volatile boolean running    = false;
    Rect rect                   = null;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
        field = new byte[2][HEIGHT + 2][WIDTH + 2];
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
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        initField();
        rect = new Rect(0, 0, w, h);
    }

    void updateBounds(int bufferNumber) {
        for (int x = 1; x < HEIGHT + 1; x++) {
            field[bufferNumber][x][0] = field[bufferNumber][x][WIDTH];
            field[bufferNumber][x][WIDTH + 1] = field[bufferNumber][x][1];
        }
        for (int y = 1; y < WIDTH + 1; y++) {
            field[bufferNumber][0][y] = field[bufferNumber][HEIGHT][y];
            field[bufferNumber][HEIGHT + 1][y] = field[bufferNumber][1][y];
        }
        field[bufferNumber][0][0] = field[bufferNumber][HEIGHT][WIDTH];
        field[bufferNumber][HEIGHT + 1][WIDTH + 1] = field[bufferNumber][1][1];
        field[bufferNumber][HEIGHT + 1][0] = field[bufferNumber][1][WIDTH];
        field[bufferNumber][0][WIDTH + 1] = field[bufferNumber][HEIGHT][1];
    }

    void initField() {
        currentBuffer = 1;
        Random rand = new Random();
        for (int x = 1; x < HEIGHT + 1; x++) {
            for (int y = 1; y < WIDTH + 1; y++) {
                field[0][x][y] = (byte)rand.nextInt(MAX_COLOR);
            }
        }
        updateBounds(0);
    }

    void updateField() {
        for (int x = 1; x < HEIGHT + 1; x++) {
            for (int y = 1; y < WIDTH + 1; y++) {

                field[currentBuffer][x][y] = field[currentBuffer ^ 1][x][y];

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if ((field[currentBuffer ^ 1][x][y] + 1) % MAX_COLOR ==
                                field[currentBuffer ^ 1][x2][y2]) {
                            field[currentBuffer][x][y] = field[currentBuffer ^ 1][x2][y2];
                        }
                    }
                }
            }
        }
        updateBounds(currentBuffer);
        currentBuffer ^= 1;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int currentIndex = 0;
        for (int y = 1; y < HEIGHT + 1; y++) {
            for (int x = 1; x < WIDTH + 1; x++) {
                colors[currentIndex++] = palette[field[currentBuffer ^ 1][y][x]];
            }
        }
        bitmap.setPixels(colors, 0, WIDTH, 0, 0, WIDTH, HEIGHT);
        canvas.drawBitmap(bitmap, null, rect, null);
    }
}

