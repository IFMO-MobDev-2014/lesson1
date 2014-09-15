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
    final int WIDTH = 240, HEIGHT = 320;
    int[][] field = new int[WIDTH][HEIGHT], field2 = new int[WIDTH][HEIGHT];
    final Rect SRC = new Rect(0, 0, WIDTH, HEIGHT);
    Rect dst;
    Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    boolean changeAll = false;
    int currentBitmap = 0;
    Bitmap[] bitmaps = new Bitmap[MAX_COLOR];
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
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000 + ", fps: " + 1E9 / (finishTime - startTime));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        dst = new Rect(0, 0, w-1, h-1);
        initField();
    }

    void initField() {
        Random rand = new Random();
        for (int x=0; x<WIDTH; x++) {
            for (int y=0; y<HEIGHT; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
                field2[x][y] = field[x][y];
            }
        }
    }

    void updateField() {
        if (changeAll) {
            currentBitmap = currentBitmap == MAX_COLOR-1 ? 0 : currentBitmap + 1;
            bitmap = bitmaps[currentBitmap];
        }
        else {
            changeAll = true;
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {

                    boolean found = false;
                    int cur = field[x][y] == MAX_COLOR-1 ? 0 : field[x][y] + 1;

                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int x2 = x + dx;
                            int y2 = y + dy;
                            if (x2 == -1) x2 = WIDTH - 1;
                            else if (x2 == WIDTH) x2 = 0;
                            if (y2 == -1) y2 = HEIGHT - 1;
                            else if (y2 == HEIGHT) y2 = 0;
                            if (cur == field[x2][y2]) {
                                field2[x][y] = cur;
                                bitmap.setPixel(x, y, palette[cur]);
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            break;
                        }
                    }
                    if (!found) {
                        changeAll = false;
                    }
                }
            }
            for (int x = 0; x < WIDTH; x++) {
                System.arraycopy(field2[x], 0, field[x], 0, HEIGHT);
            }
            if (changeAll) {
                for (int i=0; i<MAX_COLOR; i++) {
                    bitmaps[i] = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
                    for (int x=0; x<WIDTH; x++) {
                        for (int y=0; y<HEIGHT; y++) {
                            //field[x][y] = field[x][y] == MAX_COLOR-1 ? 0 : field[x][y]+1;
                            bitmaps[i].setPixel(x, y, palette[(field[x][y] + i) % MAX_COLOR]);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, SRC, dst, null);
    }
}
