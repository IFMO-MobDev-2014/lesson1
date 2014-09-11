package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    int [][] field1 = null;
    int [][] field2 = null;
    int [] pixels = null;
    int width = 0;
    int height = 0;
    int scale = 4;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    Paint[] paints = null;
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
        paints = new Paint[palette.length];
        for(int i = 0; i < palette.length; i++) {
            paints[i] = new Paint();
            paints[i].setColor(palette[i]);
        }
        pixels = new int[320*240];
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
                long updateTime = System.nanoTime();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "U+D: " + (finishTime - startTime) / 1000000 + " U: " + (updateTime - startTime) / 1000000);
                try {
                    Thread.sleep(16); // why, oh why?
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        if(w > h) {
            width = 320;
            height = 240;
            scale = Math.max(w / width, h / height);
        } else {
            width = 240;
            height = 320;
            scale = Math.max(w / width, h / height);
        }
        Log.i("SIZE","WHS: " + w + " " + h + " " + scale);
        initField();
    }

    void initField() {
        field1 = new int[width][height];
        field2 = new int[width][height];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field1[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {

                field2[x][y] = field1[x][y];

                int nc = field1[x][y] + 1;
                if(nc == MAX_COLOR)
                    nc = 0;

                for (int dx=-1; dx<=1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2<0) x2 += width;
                        if (y2<0) y2 += height;
                        if (x2>=width) x2 -= width;
                        if (y2>=height) y2 -= height;

                        if ( nc == field1[x2][y2]) {
                            field2[x][y] = field1[x2][y2];
                        }
                    }
                }
            }
        }

        int [][] tf = field1;
        field1 = field2;
        field2 = tf;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(scale, scale);
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                pixels[x+y*width] = palette[field1[x][y]];
            }
        }
        canvas.drawBitmap(pixels, 0, width, 0, 0, width, height, false, null);
    }
}
