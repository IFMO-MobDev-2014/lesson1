package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    Paint paint = null;
    int [][] field = null;
    int width = 240;
    int height = 320;
    //float scaleX, scaleY;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    Bitmap bmp, scaledBmp;

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
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                //try {
                //    Thread.sleep(16);
                //} catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        //scaleX = (float)w / (float)width;
        //scaleY = (float)h / (float)height;
        initField();
    }

    void initField() {
        paint = new Paint();
        bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        field = new int[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int[][] field2 = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                field2[x][y] = field[x][y];

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy =- 1; dy <= 1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;

                        int newColor = field[x][y] + 1;
                        if (newColor == MAX_COLOR) {
                            newColor = 0;
                        }

                        if (newColor == field[x2][y2]) {
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
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //paint.setColor(palette[field[x][y]]);
                //canvas.drawRect(x*scale, y*scale, (x+1)*scale, (y+1)*scale, paint);
                //canvas.drawRect(x * scaleX, y * scaleY, (x + 1) * scaleX, (y + 1) * scaleY, paint);

                bmp.setPixel(x, y, palette[field[x][y]]);
            }
        }
        scaledBmp = Bitmap.createScaledBitmap(bmp, canvas.getWidth(), canvas.getHeight(), false);
        canvas.drawBitmap(scaledBmp, 0, 0, paint);
    }
}