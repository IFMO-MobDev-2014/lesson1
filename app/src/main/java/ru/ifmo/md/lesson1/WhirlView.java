package ru.ifmo.md.lesson1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    int [][] field_a = null;
    int [][] field_b = null;
    int [][] field_c = null;
    int width = 240;
    int height = 320;
    int [] field_colors = null;
    Bitmap picture;
    DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
    int m1 = displaymetrics.widthPixels;
    int m2 = displaymetrics.heightPixels;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    float width_to_draw = 1;
    float height_to_draw = 1;

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

    @SuppressLint("WrongCall")
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
                try {
                    Thread.sleep(0);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width_to_draw = (float) w/width;
        height_to_draw = (float) h/height;

        initField();
    }

    void initField() {
        field_a = new int [width][height];
        field_b = new int [width][height];
        Random rand = new Random();
        picture = Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565);
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field_a [x][y] = rand.nextInt(MAX_COLOR);
            }
        }
        field_colors = new int[width*height];

    }

    void updateField() {
        int choose = 1;
        int now_color;
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field_b[x][y] = field_a[x][y];
                now_color = field_a[x][y]+1;
                if(now_color == MAX_COLOR)
                    now_color -= MAX_COLOR;
                field_colors[y*width+x] = palette[field_a[x][y]];

                choose = 1;
                for (int dx=-1; dx<=1 && choose==1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2<0) x2 += width;
                        if (y2<0) y2 += height;
                        if (x2>=width) x2 -= width;
                        if (y2>=height) y2 -= height;
                        if(field_a[x2][y2] == now_color){
                            field_colors[y*width+x] = palette[field_a[x2][y2]];
                            choose = 0;
                            field_b[x][y] = field_a[x2][y2];
                            break;
                        }
                    }
                }
            }
        }
        field_c = field_a;
        field_a = field_b;
        field_b = field_c;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(width_to_draw,height_to_draw);
        picture.setPixels(field_colors,0,width,0,0,width,height);
        canvas.drawBitmap(picture,0,0,null);
    }


}
