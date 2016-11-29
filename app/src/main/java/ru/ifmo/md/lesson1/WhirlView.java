package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

// Loskutov Ignat (2538)
/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    final int width = 240;
    final int height = 320;
    int [] field = new int[width * height];
    int [] field2 = new int[width * height];
    int [] colors = new int[width * height];
    int wScale;
    int hScale;
    final int MAX_COLOR = 10;
    final int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    final Paint paint = new Paint();
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
        Canvas canvas;
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                canvas = holder.lockCanvas();
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        //width = w/scale;
        //height = h/scale;
        wScale = w/width;
        hScale = h/height;
        Log.i("w", w + " " + width + " " + wScale);
        Log.i("h", h + " " + height + " " + hScale);
        initField();
    }

    void initField() {
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x + y*width] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        System.arraycopy(field, 0, field2, 0, width * height);

        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                for (int dx=-1; dx<=1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2<0) x2 += width;
                        if (y2<0) y2 += height;
                        if (x2>=width) x2 -= width;
                        if (y2>=height) y2 -= height;
                        if ( (field2[x + y*width]+1) % MAX_COLOR == field2[x2 + y2*width]) {
                            field[x + y*width] = field2[x2 + y2*width];
                        }
                    }
                }
                colors[x + y*width] = palette[field[x + y*width]];
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(wScale, hScale);
        canvas.drawBitmap(colors, 0, width, 0, 0, width, height, false, paint);
    }
}
