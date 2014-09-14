package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Rect;
import android.graphics.Bitmap;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null, field2 = null;
    int width = 0;
    int height = 0;
    int scale = 4;
    final int MAX_COLOR = 10;
    final int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    int[] pixels;
    final Paint[] colors = new Paint[palette.length];
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

    Rect dst;
    Bitmap bitmap;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();

        for (int i=0; i<palette.length; i++) {
            Paint paint = new Paint();
            paint.setColor(palette[i]);
            colors[i] = paint;
        }
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
                Canvas canvas = holder.lockCanvas();
                updateField();
                long startTime = System.nanoTime();
                draw(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w/scale;
        height = h/scale;

        dst = new Rect(0, 0, w, h);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        pixels = new int[width * height];

        initField();
    }

    void initField() {
        field = new int[width][height];
        field2 = new int[width][height];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x=1; x<width-1; x++) {
            for (int y=1; y<height-1; y++) {
                int curColor = field[x][y] + 1;
                int newColor;
                if (curColor >= MAX_COLOR) {
                    curColor -= MAX_COLOR;
                }
                /*if (x2<0) x2 += width;
                if (y2<0) y2 += height;
                if (x2>=width) x2 -= width;
                if (y2>=height) y2 -= height;*/
                newColor = field[x + 1][y + 1];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x + 1][y];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x + 1][y - 1];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x][y + 1];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x][y - 1];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x - 1][y + 1];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x - 1][y];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x - 1][y - 1];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                field2[x][y] = field[x][y];
            }
        }
        for (int x=0; x<width; x++) {
            System.arraycopy(field2[x], 0, field[x], 0, height);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        /*for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                canvas.drawRect(x*scale, y*scale, (x+1)*scale, (y+1)*scale, colors[field[x][y]]);
            }
        }*/
        int curPixel = 0;
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                pixels[curPixel++] = palette[field[x][y]];
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        canvas.drawBitmap(bitmap, null, dst, null);
    }
}
