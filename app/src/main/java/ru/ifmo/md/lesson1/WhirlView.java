package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;


import java.util.ArrayList;
import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    int width = 240;
    int height = 320;
    int deviceWidth = 0;
    int deviceHeight = 0;
    int[][] field = null;
    int[][] updatedCell = null;
    int[] colors = null;
    Bitmap place = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    ArrayList<int[]> pixels = new ArrayList<int[]>();
    ArrayList<Bitmap> places = new ArrayList<Bitmap>();
    int countCycle = 0;
    int sizeCycle = 0;
    Rect destRect = null;
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
                Canvas canvas = holder.lockCanvas();
                reDraw(canvas);
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
        deviceWidth = w;
        deviceHeight = h;
        destRect = new Rect(0, 0, w, h);
        initField();
    }

    boolean isMatrixEqual(int[] m1, int[] m2) {
        for (int x = 0; x < width*height; x++) {
            if (m1[x] != m2[x]) {
                return false;
            }
        }
        return true;
    }

    boolean isExistCycle(int[] m) {
        for (int i = 0; i < pixels.size(); i++) {
            if (isMatrixEqual(m, pixels.get(i))) {
                sizeCycle = i;
                return true;
            }
        }
        return false;
    }

    void initField() {
        field = new int[width][height];
        updatedCell = new int[width][height];
        colors = new int[width*height];
        Random rand = new Random();
        int colorsCell = 0;
        for (int x=0; x<width; x++) {
            for (int y = 0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
                colors[x + y*width] = palette[field[x][y]];
            }
        }

        pixels.add(colors);
        updateField();
        while (!isExistCycle(colors)) {
            pixels.add(colors);
            updateField();
        }

        Log.i("SIZE", String.valueOf(pixels.size()));

        field = null;
        colors = null;
        updatedCell = null;

    }

    private void updateCell(int x, int y) {
        updatedCell[x][y] = 0;

        for (int dx=-1; dx<=1; dx++) {
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
                if ((field[x][y]+1) % MAX_COLOR == colorField) {
                    field[x][y] = colorField;
                    updatedCell[x][y] = 1;
                    return;
                }
            }
        }

    }

    private void updateField() {
        int colorsCell = 0;
        colors = new int[width*height];
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                updateCell(x, y);

                colors[x + y*width] = palette[field[x][y]];
            }
        }
    }

    public void reDraw(Canvas canvas) {
        if (!pixels.isEmpty()) {
            place.setPixels(pixels.get(countCycle), 0, width, 0, 0, width, height);
            canvas.drawBitmap(place, null, destRect, null);
            if (countCycle < sizeCycle) {
                sizeCycle--;
            } else {
                places.add(Bitmap.createScaledBitmap(place, deviceWidth, deviceHeight, true));
            }
            pixels.remove(0);
        } else {
            canvas.drawBitmap(places.get(countCycle), 0, 0, null);
            countCycle = (countCycle + 1) % places.size();
        }
    }
}
