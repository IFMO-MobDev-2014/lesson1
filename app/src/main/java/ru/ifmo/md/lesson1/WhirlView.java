package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.Random;

public class WhirlView extends SurfaceView implements SurfaceHolder.Callback {
    private DrawThread drawThread;
    private int SIZE_LONG = 320;
    private  int SIZE_SHORT = 240;

    private TextView view;
    private Bitmap bitmap = null;

    private int[] field = null;
    private int width = 0;
    private int height = 0;
    private float scaleX = 1;
    private float scaleY = 1;
    private int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};


    public WhirlView(Context context, TextView view) {
        super(context);
        this.view = view;
        getHolder().addCallback(this);
    }

    public void stopDrawing() {
        drawThread.setRunning(false);
        try {
            drawThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(this);
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // retry until it stops
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        if (w > h) {
            width = SIZE_LONG;
            height = SIZE_SHORT;
        } else {
            width = SIZE_SHORT;
            height = SIZE_LONG;
        }
        scaleX = w / (float) width;
        scaleY = h / (float) height;
        initField();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(scaleX, scaleY);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    void initField() {
        field = new int[width * height];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[y * width + x] = rand.nextInt(palette.length);
            }
        }
    }

    public int[] getField() {
        return field;
    }

    public void setField(int[] newFieled) {
        field = newFieled;
    }

    public int getH() {
        return height;
    }

    public int getW() {
        return width;
    }

    public TextView getTextView() {
        return view;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int[] getPalette() {
        return palette;
    }

}