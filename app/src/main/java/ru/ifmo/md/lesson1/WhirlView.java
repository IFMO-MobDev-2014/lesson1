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

/**
 * Created by thevery on 11/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    int width = 240;
    int height = 320;
    int[][] field;
    float scaleWidth = 0;
    float scaleHeight = 0;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    int[] colors = new int[width * height];
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    Paint paint = new Paint();
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Rect rect;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
        initField();
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
        } catch (InterruptedException ignore) {
        }
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
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scaleWidth = ((float) w / width);
        scaleHeight = ((float) h / height);
        Log.d("size", w + " x " + h);
        rect = new Rect(0, 0, (int) (width * scaleWidth), (int) (height * scaleHeight));
    }

    void initField() {
        field = new int[height + 2][width + 2];
        Random rand = new Random();
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                field[y][x] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    boolean[][] change = new boolean[height][width];

    void updateField() {
        for (int x = 1; x < width + 1; x++) {
            field[0][x] = field[height][x];
            field[height + 1][x] = field[1][x];
        }
        for (int y = 1; y < height + 1; y++) {
            field[y][0] = field[y][width];
            field[y][width + 1] = field[y][1];
        }
        field[0][0] = field[height][width];
        field[0][width + 1] = field[height][1];
        field[height + 1][0] = field[1][width];
        field[height + 1][width + 1] = field[1][1];
        for (int x = 1; x < width + 1; x++) {
            for (int y = 1; y < height + 1; y++) {
                int mod = (field[y][x] + 1) % MAX_COLOR;
                change[y-1][x-1] = ((mod == field[y - 1][x - 1]) || (mod == field[y - 1][x]) || (mod == field[y - 1][x + 1])
                        || (mod == field[y][x - 1]) || (mod == field[y][x + 1]) ||
                        (mod == field[y + 1][x - 1]) || (mod == field[y + 1][x]) || (mod == field[y + 1][x + 1]));
            }
        }
        for (int x = 1; x < width+1; x++)
            for (int y = 1; y < height+1; y++)
                if (change[y-1][x-1])
                    field[y][x] = (field[y][x] + 1) < MAX_COLOR ? (field[y][x] + 1) : 0;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int y = 1; y < height + 1; y++) {
            for (int x = 1; x < width + 1; x++) {
                colors[(y-1) * width + x-1] = palette[field[y][x]];
            }
        }
        bitmap.setPixels(colors, 0, width, 0, 0, width, height);
        canvas.drawBitmap(bitmap, null, rect, paint);
        // canvas.drawBitmap(colors,0,width,0,0,width,height,true,null);

    }
}