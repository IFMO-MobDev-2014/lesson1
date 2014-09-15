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
    int [][] field = null;
    int step = 0;
    final int WIDTH = 240;
    final int HEIGHT = 320;
    int countframes = 0;
    double fps, afps;
    long ptime;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    Canvas canvas;
    Rect screen;
    Paint paint = new Paint();
    Bitmap bmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565);

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
                canvas = holder.lockCanvas();
                ptime = System.currentTimeMillis();
                updateField();
                canvas.drawBitmap(bmap, null, screen, paint);


                canvas.drawText("FPS:" + String.valueOf(Math.round(fps * 10) / 10.0) + "Average: " + String.valueOf(Math.round(fps * 10) / 10.0), 60, 60, paint);

                holder.unlockCanvasAndPost(canvas);

                ptime = System.currentTimeMillis() - ptime;
                fps = 1000.0 /ptime;
                afps = (afps * countframes++ + fps)/countframes;

                Log.i("TIME", "Circle: " + ptime + " AverageFPS: " + afps);
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        screen = new Rect(0,0,w,h);
        initField();
    }

    void initField() {
        paint.setTextSize(50f);
        paint.setColor(0xFFFFFFFF);
        field = new int[3][WIDTH * HEIGHT];
        Random rand = new Random();
        for (int x=0; x< WIDTH; x++) {
            for (int y=0; y< HEIGHT; y++) {
                field[step][x + y * WIDTH] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int nstep = (step + 1) % 2;
        int x2;
        int y2;
        for (int x=0; x< WIDTH; x++) {
             for (int y=0; y< HEIGHT; y++) {

                field[nstep][x + y * WIDTH] = field[step][x + y * WIDTH];
                boolean loop = true;
                for (int dx=-1; dx<=1 && loop; dx++) {
                    for (int dy=-1; dy<=1 && loop; dy++) {
                        x2 = (WIDTH + x + dx) % WIDTH;
                        y2 = (HEIGHT + y + dy) % HEIGHT;
                        if ( (field[step][x + y * WIDTH]+1) % MAX_COLOR == field[step][x2 + y2 * WIDTH]) {
                            field[nstep][x + y * WIDTH] = field[step][x2 + y2 * WIDTH];
                            field[2][x + y * WIDTH] = palette[field[step][x2 + y2 * WIDTH]];
                            loop = false;
                        }
                    }
                }

            }
        }
        bmap.setPixels(field[2], 0, WIDTH,0,0, WIDTH, HEIGHT);
        step = nstep;

    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(bmap,null,screen, paint);
    }
}
