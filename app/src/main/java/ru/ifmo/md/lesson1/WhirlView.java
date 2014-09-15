package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.BitSet;
import java.util.Random;

class WhirlView extends SurfaceView implements Runnable {
    private int[][][] field = null;
    private int[] bmpSrc = null;
    private Bitmap bmp = null;
    private int z = 0;
    private final int width = 240;
    private final int height = 320;
    private float scaleX = 4;
    private float scaleY = 8;
    private final int MAX_COLOR = 10;
    private final int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    private final SurfaceHolder holder;
    private Thread thread = null;
    private volatile boolean running = false;

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
        Canvas canvas;
        int count = 0;
        long sum = 0;
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                canvas = holder.lockCanvas();
                updateField();

                draw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "" + (finishTime - startTime) / 1000000);
                count++;
                sum += (finishTime - startTime) / 1000000;
                /*try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {
                }*/
            }
        }
        Log.i("TIME", "Average circle: " + (sum / count) + "FPS: " + (count * 1000 / sum));
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scaleX = ((float)(w + width - 1)) / ((float)width);
        scaleY = ((float)(h + height - 1)) / ((float)height);
        initField();
    }

    void initField() {
        field = new int[2][height][width];
        bmpSrc = new int[width * height];
        z = 0;
        Random rand;
        rand = new Random();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                field[z][y][x] = rand.nextInt(MAX_COLOR);
                field[1 - z][y][x] = MAX_COLOR;
            }
        }
    }

    void updateField() {
        z = 1 - z;
        int x2, y2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                field[z][y][x] = field[1 - z][y][x];

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        x2 = x + dx;
                        y2 = y + dy;
                        if (x2 < 0) x2 = width - 1;
                        if (y2 < 0) y2 = height - 1;
                        if (x2 >= width) x2 = 0;
                        if (y2 >= height) y2 = 0;
                        if ((field[1 - z][y][x] + 1) % MAX_COLOR == field[1 - z][y2][x2]) {
                            field[z][y][x] = field[1 - z][y2][x2];
                        }
                    }
                }
            }
        }
    }

    public void draw(Canvas canvas) {
        /*for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (field[z][x][y] != field[1 - z][x][y]) {
                    paint.setColor(palette[field[z][x][y]]);
                    canvas.drawRect(x * scaleX, y * scaleY, (x + 1) * scaleX, (y + 1) * scaleY, paint);
                }
            }
        }*/


        int count = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bmpSrc[count++] = palette[field[z][y][x]];
            }
        }
        //bmp = Bitmap.createBitmap(bmpSrc, width, height, Bitmap.Config.ARGB_8888);
        canvas.scale(scaleX, scaleY);
        canvas.drawBitmap(bmpSrc, 0, width, 0, 0, width, height, false, null);

    }
}