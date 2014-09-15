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

class WhirlView extends SurfaceView implements Runnable {
    int [][] field=null;
    int width = 240, count=0, sum=0, height=320, scale=4;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    int bitmap[]=null;
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

    public void getFPS(long time){
        Log.i("", "Fps: " + 1000/time);
        sum+=1000/time;
        count++;
    }

    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();

                updateField();

                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        bitmap[width*j+i] = palette[field[i][j]];
                    }
                }

                onDraw(canvas);

                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();

                getFPS((finishTime - startTime) / 1000000);

                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
        Log.i("","Middle fps: "+ sum/count);
    }

    @Override

    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w/scale;
        height = h/scale;
        initField();
    }

    void initField() {
        field = new int[width][height];
        bitmap = new int[width*height];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int[][] field2 = new int[width][height];
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

        field = field2;

    }

    @Override

    public void onDraw(Canvas canvas) {

        canvas.scale(scale, scale);
        canvas.drawBitmap(bitmap, 0, width, 0, 0, width, height, false, null);

    }
}