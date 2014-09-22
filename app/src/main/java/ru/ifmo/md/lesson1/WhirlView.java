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
class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null;
    int [][] field2 = null;
    int width = 240;
    int height = 320;
    int myw = 0;
    int myh = 0;
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
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                long cir = (finishTime - startTime) / 1000000;
                Log.i("TIME: ", "Circle: " + cir);

                Log.i("TIME: ","Fps: " + (1000. / cir));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        myw = w;
        myh = h;
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
        for (int x=0; x<width; x++)
             System.arraycopy(field[x], 0, field2[x], 0, height);
        int xm1 = width - 1;
        for (int x=0; x<width; x++) {
            int ym1 = height - 1;
            for (int y=0; y<height; y++) {
                int yp1 = y + 1;
                if (yp1 == height) yp1 = 0;

                field2[x][y] = field[x][y];
                int next = field[x][y] + 1;
                int prev = field[x][y] - 1;
                if (next == MAX_COLOR) next = 0;
                if (prev == -1) prev = MAX_COLOR - 1;

                if (field[xm1][ym1] == next) {
                    field2[x][y] = next;
                } else
                if (field[xm1][y] == next) {
                    field2[x][y] = next;
                } else
                if (field[xm1][yp1] == next) {
                    field2[x][y] = next;
                } else
                if (field[x][ym1] == next) {
                    field2[x][y] = next;
                }


                if (field[xm1][ym1] == prev) {
                    field2[xm1][ym1] = field[x][y];
                }
                if (field[xm1][y] == prev) {
                    field2[xm1][y] = field[x][y];
                }
                if (field[xm1][yp1] == prev) {
                    field2[xm1][yp1] = field[x][y];
                }
                if (field[x][ym1] == prev) {
                    field2[x][ym1] = field[x][y];
                }

                ym1 = y;
            }
            xm1 = x;
        }
        for (int x=0; x<width; x++)
            System.arraycopy(field2[x], 0, field[x], 0, height);
    }

    @Override
    public void onDraw(Canvas canvas) {
        /*Paint paint = new Paint();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                paint.setColor(palette[field[x][y]]);
                canvas.drawRect(x*myw / width, y*myh / height, (x+1)*myw / width, (y+1)*myh / height, paint);
            }
        }*/
        int [] fl = new int[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) fl[y * width + x] = palette[field[x][y]];
        }

        Bitmap map = Bitmap.createBitmap(fl, width, height, Bitmap.Config.RGB_565);

        canvas.scale(myw / (float) width, myh / (float) height);
        canvas.drawBitmap(map, 0, 0, null);
    }
}
