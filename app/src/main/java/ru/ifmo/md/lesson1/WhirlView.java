package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    int[] field = null;
    int[] field2 = null;
    int[] pixels = null;
    int width = 0;
    int height = 0;
    int state = 0;
    int incx = 0;
    int decx = 0;
    int incy = 0;
    int decy = 0;
    float wScale = 0;
    float hScale = 0;
    public static final int scale = 4;
    public static final int MAX_COLOR = 10;
    public static final int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread;
    Canvas canvas;
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
                canvas = holder.lockCanvas();
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
        width = w / scale;
        height = h / scale;
        wScale = (float) w / width;
        hScale = (float) h / height;
        initField();
    }

    void initField() {
        field = new int[width*height];
        field2 = new int[width*height];
        pixels = new int[width * height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x+width*y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x = 1; x < width-1; x++) {
            for (int y = 1; y < height-1; y++) {
                field2[x+width*y] = field[x+width*y];
                pixels[x + width * y] = palette[field[x+width*y]];
                state = (field[x+width*y]+1) % MAX_COLOR;
                incx = (x<width) ? x+1 : 0;
                decx = (x>=0) ? x-1 : width-1;
                incy = (y<height) ? y+1 : 0;
                decy = (y>=0) ? y-1 : height-1;
                if (state == field[incx+width*y]) {
                    field2[x+width*y] = field[incx+width*y];
                    pixels[x + width * y] = palette[field[incx+width*y]];
                } else if (state == field[incx+width*incy]) {
                    field2[x+width*y] = field[incx+width*incy];
                    pixels[x + width * y] = palette[field[incx+width*incy]];
                } else if (state == field[incx+width*decy]) {
                    field2[x+width*y] = field[incx+width*decy];
                    pixels[x + width * y] = palette[field[incx+width*decy]];
                } else if (state == field[decx+width*incy]) {
                    field2[x+width*y] = field[decx+width*incy];
                    pixels[x + width * y] = palette[field[decx+width*incy]];
                } else if (state == field[decx+width*y]) {
                    field2[x+width*y] = field[decx+width*y];
                    pixels[x + width * y] = palette[field[decx+width*y]];
                } else if (state == field[decx+width*decy]) {
                    field2[x+width*y] = field[decx+width*decy];
                    pixels[x + width * y] = palette[field[decx+width*decy]];
                } else if (state == field[x+width*incy]) {
                    field2[x+width*y] = field[x+width*incy];
                    pixels[x + width * y] = palette[field[x+width*incy]];
                } else if (state == field[x+width*decy]) {
                    field2[x+width*y] = field[x+width*decy];
                    pixels[x + width * y] = palette[field[x+width*decy]];
                }
            }
        }

        System.arraycopy(field2, 0, field, 0 ,width*height);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.scale(hScale, wScale);
        canvas.drawBitmap(pixels, 0, width, 0, 0, width, height, false, null);
    }
}

