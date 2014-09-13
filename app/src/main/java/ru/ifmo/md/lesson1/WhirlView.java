package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

class WhirlView extends SurfaceView implements Runnable {
    int [][][] field = null;
    int step = 0;
    int width = 240;
    int height = 320;
    int devWidth;
    int devHeight;
    Paint paint = new Paint();
    Bitmap bmap = Bitmap.createBitmap(width,height, Bitmap.Config.RGB_565);
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
        Canvas canvas;
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                canvas = holder.lockCanvas();

                updateField();  // 25ms
                onDraw(canvas); //~45ms

                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();

                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                Log.i("FPS", "FPS:" + 1000.0/((finishTime - startTime) / 1000000));
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        devWidth = w;
        devHeight = h;
        initField();
    }

    void initField() {
        field = new int[width][height][2];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y][step] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int nstep = (step + 1) % 2;
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {

                field[x][y][nstep] = field[x][y][step];

                for (int dx=-1; dx<=1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2<0) x2 += width;
                        if (y2<0) y2 += height;
                        if (x2>=width) x2 -= width;
                        if (y2>=height) y2 -= height;
                        if ( (field[x][y][step]+1) % MAX_COLOR == field[x2][y2][step]) {
                            field[x][y][nstep] = field[x2][y2][step];
                        }
                    }
                }
                bmap.setPixel(x,y,palette[field[x][y][nstep]]);
            }
        }
        step = nstep;

    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(bmap,null,new Rect(0,0,devWidth,devHeight), paint);
    }
}
