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
    int[] field = null;
    //int [][] field = null;
    boolean flag = false;
    int counter = 0, num = 0;
    int width = 1920;
    int height = 1080;
    int scale = 4;
    int[][] cash = new int[9][width * height];
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
                //if (flag) {
                    //cashUpdate();
                //}
                //else {
                    updateField();
                //}
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
        width = w/scale;
        height = h/scale;
        initField();
    }

    void initField() {
        field = new int[width * height];
        //field = new int[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //field[x][y] = rand.nextInt(MAX_COLOR);
                field[x * width + y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int[] field2 = new int[width * height];
        //int[][] field2 = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                field2[x * width + y] = field[x * width + y];
                //field2[x][y] = field[x][y];

                for (int dx=-1; dx<=1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2<0) x2 += width;
                        if (y2<0) y2 += height;
                        if (x2>=width) x2 -= width;
                        if (y2>=height) y2 -= height;
                        //if ( (field[x][y]+1) % MAX_COLOR == field[x2][y2]) {
                        if ( (field[x * width + y] + 1) % MAX_COLOR == field[x2 * width + y2]) {
                            field2[x * width + y] = field[x2 * width + y2];
                            //field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }
        field = field2;
        /*cash[counter] = field2;
        if (cash[counter] == cash[0]) {
            flag = true;

        }
        else {
            counter++;
        }*/
    }

    /*void cashUpdate() {
        if(num == counter + 1) {
            num = 0;
        }
        field = cash[num];
        num++;
    }*/

    @Override
    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                //paint.setColor(palette[field[x][y]]);
                paint.setColor(palette[field[x * width + y]]);
                canvas.drawRect(x*scale, y*scale, (x+1)*scale, (y+1)*scale, paint);
            }
        }
    }
}
