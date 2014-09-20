package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/

class Update implements Runnable {
    Update() {

    }
    public void run() {}
}


class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null;
    int width = 240;
    int height = 320;
    int w1 = 0, h1 = 0;
    int[] ps = null;
    boolean flag = false;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread, thread1 = null;
    Bitmap bitmap, bitmap2 = null;
    volatile boolean running = false;

    public class Update implements Runnable {
        public void run() {
            while (running) {
                if (flag) continue;
                int[][] field2 = new int[width][height];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        field2[x][y] = field[x][y];
                        int t = (field[x][y] + 1) % MAX_COLOR;
                        for (int dx = -1; dx <= 1; dx++) {
                            int x2 = x + dx;
                            if (x2 < 0) x2 += width;
                            else if (x2 >= width) x2 -= width;
                            for (int dy = -1; dy <= 1; dy++) {
                                int y2 = y + dy;
                                if (y2 < 0) y2 += height;
                                else if (y2 >= height) y2 -= height;
                                if (t == field[x2][y2]) {
                                    field2[x][y] = field[x2][y2];
                                    dx = 2;
                                    break;
                                }
                            }
                        }
                    }
                }
                field = field2;
                try {
                    Thread.sleep(16);
                } catch(InterruptedException e) {}
                flag = true;
            }
        }
    }

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
    }

    public void resume() {
        initField();
        running = true;
        thread = new Thread(this);
        thread1 = new Thread(new Update());
        thread.start();
        thread1.start();

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
                //updateField();
                MyonDraw(canvas);
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
        initField();
        bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.RGB_565 );

    }

    void initField() {
        field = new int[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
        ps = new int[width * height];
    }

    void updateField() {
        int[][] field2 = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field2[x][y] = field[x][y];
                int t = (field[x][y] + 1) % MAX_COLOR;

                for (int dx = -1; dx <= 1; dx++) {
                    int x2 = x + dx;
                    if (x2 < 0) x2 += width;
                    else if (x2 >= width) x2 -= width;

                    for (int dy = -1; dy <= 1; dy++) {
                        int y2 = y + dy;
                        if (y2 < 0) y2 += height;
                        else if (y2 >= height) y2 -= height;
                        if ( t == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                            dx = 2;
                            break;
                        }
                    }
                }
            }
        }
        field = field2;
    }

    //@Override
    public void MyonDraw(Canvas canvas) {
        bitmap2 = Bitmap.createScaledBitmap(bitmap, canvas.getWidth(), canvas.getHeight(), false);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                ps[x + y*width] = palette[field[x][y]];
            }
        }
        bitmap.setPixels(ps, 0, width, 0,0, width , height);
        canvas.drawBitmap(bitmap2, 0, 0, null);
        flag = false;
    } 
}
