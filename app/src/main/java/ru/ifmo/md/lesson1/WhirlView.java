package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;


class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null;
    int width = 240;
    int height = 320;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    float scaleW = 0;
    float scaleH = 0;
    Thread thread = null;
    volatile boolean running = false;
    Paint[] paints = null;
    int[] bitcolor = null;
    Bitmap bitmap = null;
    int[][] field2;
    float fps;
    int frames;
    long allTime;
    volatile long time;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
    }

    public void resume() {
        running = true;
        thread = new Thread(this);
        thread.start();
        paints = new Paint[MAX_COLOR];
        for (int i=0; i<MAX_COLOR; i++){
            paints[i] = new Paint();
            paints[i].setColor(palette[i]);
        }
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
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                time = (finishTime - startTime) / 1000000;
                frames++;
                fps = (float) (frames * 1000.0/ allTime);
                allTime += time;
                Log.i("FPS", "FPS: " + fps + " " + "currentFPS" + (1000.0/time));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scaleW = (float) w / width;
        scaleH = (float) h / height;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitcolor = new int[width*height];
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
        {
            int x = 0;
            {
                int y = 0;
                int f2 = (field[x][y]+1) % MAX_COLOR;
                field2[x][y] = field[x][y];
                for (int dx=-1; dx<=1; dx++) {
                    int x2 = x + dx;
                    if (x2<0) x2 += width;
                    if (x2>=width) x2 -= width;
                    for (int dy=-1; dy<=1; dy++) {
                        int y2 = y + dy;
                        if (y2<0) y2 += height;
                        if (y2>=height) y2 -= height;
                        if ( f2 == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
            for (int y=0; y<height; y++) {
                int f2 = (field[x][y]+1) % MAX_COLOR;
                field2[x][y] = field[x][y];

                for (int dx=-1; dx<=1; dx++) {
                    int x2 = x + dx;
                    if (x2<0) x2 += width;
                    if (x2>=width) x2 -= width;
                    for (int dy=-1; dy<=1; dy++) {
                        int y2 = y + dy;
                        if (y2<0) y2 += height;
                        if (y2>=height) y2 -= height;
                        if ( f2 == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
            {
                int y = height - 1;
                int f2 = (field[x][y]+1) % MAX_COLOR;
                field2[x][y] = field[x][y];

                for (int dx=-1; dx<=1; dx++) {
                    int x2 = x + dx;
                    if (x2<0) x2 += width;
                    if (x2>=width) x2 -= width;
                    for (int dy=-1; dy<=1; dy++) {
                        int y2 = y + dy;
                        if (y2<0) y2 += height;
                        if (y2>=height) y2 -= height;
                        if ( f2 == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }
        for (int x=1; x<width - 1; x++) {
            {
                int y = 0;
                int f2 = (field[x][y]+1) % MAX_COLOR;
                field2[x][y] = field[x][y];
                for (int dx=-1; dx<=1; dx++) {
                    int x2 = x + dx;
                    if (x2<0) x2 += width;
                    if (x2>=width) x2 -= width;
                        for (int dy=-1; dy<=1; dy++) {
                        int y2 = y + dy;
                        if (y2<0) y2 += height;
                        if (y2>=height) y2 -= height;
                        if ( f2 == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
            for (int y=0; y<height; y++) {
                int f2 = (field[x][y]+1) % MAX_COLOR;
                field2[x][y] = field[x][y];

                for (int dx=-1; dx<=1; dx++) {
                    int x2 = x + dx;
                    if (x2<0) x2 += width;
                    if (x2>=width) x2 -= width;
                        for (int dy=-1; dy<=1; dy++) {
                        int y2 = y + dy;
                        if (y2<0) y2 += height;
                        if (y2>=height) y2 -= height;
                        if ( f2 == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
            {
               int y = height - 1;
               int f2 = (field[x][y]+1) % MAX_COLOR;
               field2[x][y] = field[x][y];

               for (int dx=-1; dx<=1; dx++) {
                   int x2 = x + dx;
                   if (x2<0) x2 += width;
                   if (x2>=width) x2 -= width;
                       for (int dy=-1; dy<=1; dy++) {
                       int y2 = y + dy;
                       if (y2<0) y2 += height;
                       if (y2>=height) y2 -= height;
                       if ( f2 == field[x2][y2]) {
                           field2[x][y] = field[x2][y2];
                       }
                   }
               }
            }
        }
        {
            int x = width - 1;
            {
                int y = 0;
                int f2 = (field[x][y]+1) % MAX_COLOR;
                field2[x][y] = field[x][y];
                for (int dx=-1; dx<=1; dx++) {
                    int x2 = x + dx;
                    if (x2<0) x2 += width;
                    if (x2>=width) x2 -= width;
                    for (int dy=-1; dy<=1; dy++) {
                        int y2 = y + dy;
                        if (y2<0) y2 += height;
                        if (y2>=height) y2 -= height;
                        if ( f2 == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
            for (int y=0; y<height; y++) {
                int f2 = (field[x][y]+1) % MAX_COLOR;
                field2[x][y] = field[x][y];

                for (int dx=-1; dx<=1; dx++) {
                    int x2 = x + dx;
                    if (x2<0) x2 += width;
                    if (x2>=width) x2 -= width;
                    for (int dy=-1; dy<=1; dy++) {
                        int y2 = y + dy;
                        if (y2<0) y2 += height;
                        if (y2>=height) y2 -= height;
                        if ( f2 == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
            {
                int y = height - 1;
                int f2 = (field[x][y]+1) % MAX_COLOR;
                field2[x][y] = field[x][y];

                for (int dx=-1; dx<=1; dx++) {
                    int x2 = x + dx;
                    if (x2<0) x2 += width;
                    if (x2>=width) x2 -= width;
                    for (int dy=-1; dy<=1; dy++) {
                        int y2 = y + dy;
                        if (y2<0) y2 += height;
                        if (y2>=height) y2 -= height;
                        if ( f2 == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }
        int[][] temp = field;
        field = field2;
        field2 = temp;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(scaleW,scaleH);
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                bitcolor[x+y*width] = palette[field[x][y]];
            }
        }
        bitmap.setPixels(bitcolor, 0, width, 0, 0, width, height);
        canvas.drawBitmap(bitmap, 0, 0 , null);
    }
}
