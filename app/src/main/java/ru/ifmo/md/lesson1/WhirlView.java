package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;


import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    int width = 240;
    int height = 320;
    int[][] field = new int[width][height];
//    int[][] field2 = null;
    int[][] updatedCell = new int[width][height];
    int[] colors = new int[width*height];
    Rect destRect = null;
    Rect srcRect = new Rect(0, 0, width, height);
//    Bitmap place = null;
    Bitmap scaledPlace = null;
    Bitmap place = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    Paint paint = new Paint();
//    int scale = 4;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
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
                updateField();
                Canvas canvas = holder.lockCanvas();
                onDraw(canvas);
//                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
//                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                Log.i("FPSe", String.valueOf(1000.0/((finishTime-startTime)/1000000.0)));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
//        width = w/scale;
//        height = h/scale;
        destRect = new Rect(0, 0, w, h);
        initField();
    }

    void initField() {
//        field = new int[width][height];
        Random rand = new Random();
        int colorsCell = 0;
        for (int x=0; x<width; x++) {
            for (int y = 0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
                colors[colorsCell++] = palette[field[x][y]];
//                place.setPixel(x, y, palette[field[x*width + y]]);
            }
        }
//        place = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_4444);
        place.setPixels(colors, 0, width, 0, 0, width, height);
//        paint.setAntiAlias(true);
//        paint.setFilterBitmap(true);
//        paint.setDither(true);
    }

    void updateField() {
//        field2 = new int[width][height];
//        boolean updateCell = false;
        int colorsCell = 0;
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {

//                field2[x][y] = field[x][y];
                updatedCell[x][y] = 0;
//                updateCell = false;

                for (int dx=-1; dx<=1 && updatedCell[x][y]==0; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2<0) x2 += width;
                        if (y2<0) y2 += height;
                        if (x2>=width) x2 -= width;
                        if (y2>=height) y2 -= height;
                        int colorField = field[x2][y2];
                        if ((x2<x || (x2==x && y2<y)) && updatedCell[x2][y2]==1) {
                            colorField--;
                            if (colorField < 0) {
                                colorField = MAX_COLOR - 1;
                            }
                        }
//                        if ( (field[x][y]+1) % MAX_COLOR == field[x2][y2]) {
                        if ((field[x][y]+1) % MAX_COLOR == colorField) {
//                            field2[x][y] = field[x2][y2];
//                            updateCell = true;
                            field[x][y] = colorField;
                            updatedCell[x][y] = 1;
                            break;
                        }
                    }
                }

//                colors[x*height+y] = palette[field2[x][y]];
                colors[colorsCell++] = palette[field[x][y]];
            }
        }
//        field = field2;
    }

    public void reDraw(Canvas canvas)
    {
/*        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
//                field2[x][y] = palette[field[x][y]];
//                place.setPixel(x, y, palette[field[x][y]]);
            }
        }*/
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        place.prepareToDraw();
        place.setPixels(colors, 0, width, 0, 0, width, height);
//        place.prepareToDraw();
//        scaledPlace = Bitmap.createScaledBitmap(place, destRect.width(), destRect.height(), true);
        canvas.drawBitmap(place, null, destRect, null);
//        canvas.drawBitmap(scaledPlace, 0, 0, null);
//        canvas.setBitmap(place);
    }
}
