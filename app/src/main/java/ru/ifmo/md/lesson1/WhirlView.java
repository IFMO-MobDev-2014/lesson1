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
class WhirlView extends SurfaceView implements SurfaceHolder.Callback {

    private DrawThread drawThread;

    int width = 0;
    int height = 0;
    static final int scale = 4;

    public WhirlView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(getHolder(), width, height);
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
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w/scale;
        height = h/scale;
    }


}

class DrawThread extends Thread {
    private boolean runFlag = false;
    private SurfaceHolder surfaceHolder;
    private Bitmap bmp;
    private Bitmap scaled_bmp;
    Bitmap.Config conf = Bitmap.Config.RGB_565;
    static final int MAX_COLOR = 10;
    private int[][] field;
    private int[][] field2;
    static final int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    int width= 0;
    int height = 0;

    public DrawThread(SurfaceHolder surfaceHolder, int w, int h) {
        this.surfaceHolder = surfaceHolder;
        width = w;
        height = h;
        field2 = new int[width][height];
        initField();
        bmp = Bitmap.createBitmap(width, height,conf);
    }

    public void setRunning(boolean run) {
        runFlag = run;
    }

    void initField() {
        field = new int[width][height];
        bmp = Bitmap.createBitmap(width, height, conf);
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    @Override
    public void run() {
        Canvas canvas;
        while (runFlag) {
            canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                long startTime = System.nanoTime();
                updateField();
                synchronized (surfaceHolder) {
                    scaled_bmp = Bitmap.createScaledBitmap(bmp, canvas.getWidth(), canvas.getHeight(), true);
                    canvas.drawBitmap(scaled_bmp, 0, 0, null);
                }
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                Log.i("FPS", "Circle: " + 1000.0 / (double)((finishTime - startTime) / 1000000));
            }
            finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
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
                bmp.setPixel(x, y, palette[field[x][y]]);
            }
        }
        for (int x = 0; x < width; x++) {
            System.arraycopy(field2[x],0,field[x],0,height);
        }
    }

}