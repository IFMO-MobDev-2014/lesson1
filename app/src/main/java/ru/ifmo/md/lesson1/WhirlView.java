package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
// TODO Parallel it
// TODO Bitmap
class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null;
    int width = 0;
    int height = 0;
    int scale = 4;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
//    int[] palette2 = {0xFFFF00, 0xFF8000, 0xFF8080, 0xFF0080, 0xFF00FF, 0xFF0080, 0xFF0000, 0xFF8000, 0xFF80FF, 0xFFFFFF};
//    Paint paint = new Paint();
    SurfaceHolder holder;
    Thread thread = null;
    Canvas canvas;
    Bitmap bitmap;
    Rect rect;
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
                canvas = holder.lockCanvas();
                long startTime = System.nanoTime();
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle/FPS: " + (finishTime - startTime) / 1000000 + "/" + Math.round(1000000000.0 / ((double) (finishTime - startTime))));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w/scale;
        height = h/scale;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        rect = new Rect(0, 0, w, h);
        canvas = new Canvas(bitmap);
        initField();
    }

    void initField() {
        field = new int[width][height];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int[][] field2 = new int[width][height];
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field2[x][y] = field[x][y];
                for (int dx=-1; dx<=1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2<0) x2 += width;
                        if (y2<0) y2 += height;
                        if (x2>=width) x2 -= width;
                        if (y2>=height) y2 -= height;
                        if ( (field[x][y]+1) % MAX_COLOR == field[x2][y2]) {
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
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                bitmap.setPixel(i, j, palette[field[i][j]]);
            }
        }
        canvas.drawBitmap(bitmap, null, rect, null);
//        for (int x=0; x<width; x++) {
//            for (int y=0; y<height; y++) {
//                paint.setColor(palette[field[x][y]]);
//                canvas.drawRect(x*scale, y*scale, (x+1)*scale, (y+1)*scale, paint);
//            }
//        }
    }
}
