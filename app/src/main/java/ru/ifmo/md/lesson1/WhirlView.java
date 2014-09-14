package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.media.Image;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements SurfaceHolder.Callback {
    int [][] field = null;
    int width = 0;
    int height = 0;
    int scale = 4;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    private DrawThread thread;
    volatile boolean running = false;


    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
    }

    int[][] field2;

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w/scale;
        height = h/scale;
        field2 = new int[width][height];
        bufferedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bufferedCanvas = new Canvas(bufferedBitmap);
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
        int[][] tmp = field;
        field = field2;
        field2 = field;
    }
    private Bitmap bufferedBitmap;
    private Canvas bufferedCanvas;
    private Paint paint = new Paint();

    @Override
    public void draw(Canvas canvas) {
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                paint.setColor(palette[field[x][y]]);
                bufferedCanvas.drawRect(x*scale, y*scale, (x+1)*scale, (y+1)*scale, paint);
            }
        }

        canvas.drawBitmap(bufferedBitmap, 0, 0, null);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        thread = new DrawThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        thread.setRunning(false);
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
