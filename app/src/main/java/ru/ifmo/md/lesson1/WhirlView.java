package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    int[][] field = null;
    int width = 0;
    int height = 0;
    int scale = 4;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

    Paint paint = new Paint();
    RectF rectf;
    Bitmap bitmap;
    int[] color;
    int fps;
    int t;
    int firstcol[];
    int cur[];
    int cur2[];


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
        } catch (InterruptedException ignore) {
        }
    }

    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                updateField();
                draw(canvas);
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
        width = w / scale;
        height = h / scale;
        rectf = new RectF(0, 0, width * scale, height * scale);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        color = new int[width * height];
        firstcol = new int[height];
        cur = new int[height];
        cur2 = new int[height];
        initField();
    }

    void initField() {
        field = new int[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }

    }

    void updateField() {

        for (int y = 0; y < height; y++) {
            firstcol[y] = field[0][y];
            cycle:
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int x2 = dx;
                    int y2 = y + dy;
                    if (x2 < 0) x2 += width;
                    if (y2 < 0) y2 += height;
                    if (x2 >= width) x2 -= width;
                    if (y2 >= height) y2 -= height;
                    if ((field[0][y] + 1) % MAX_COLOR == field[x2][y2]) {
                        firstcol[y] = field[x2][y2];
                        break cycle;
                    }
                }
            }
        }


        for (int y = 0; y < height; y++) {
            cur[y] = field[0][y];
            cycle:
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int x2 = 1 + dx;
                    int y2 = y + dy;
                    if (x2 < 0) x2 += width;
                    if (y2 < 0) y2 += height;
                    if (x2 >= width) x2 -= width;
                    if (y2 >= height) y2 -= height;
                    if ((field[1][y] + 1) % MAX_COLOR == field[x2][y2]) {
                        cur[y] = field[x2][y2];
                        break cycle;
                    }
                }
            }
        }

        for (int x = 2; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cur2[y] = field[x][y];
                cycle:
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ((field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            cur2[y] = field[x2][y2];
                            break cycle;
                        }
                    }
                }
            }
            for (int y = 0; y < height; y++) {
                field[x - 1][y] = cur[y];
                cur[y] = cur2[y];
            }
        }

        for (int y = 0; y < height; y++) {
            field[width - 1][y] = cur[y];
            field[0][y] = firstcol[y];
        }

    }

    @Override
    public void draw(Canvas canvas) {
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                color[x * width + y] = palette[field[y][x]];
            }
        }
        bitmap.setPixels(color, 0, width, 0, 0, width, height);
        canvas.drawBitmap(bitmap, null, rectf, paint);
    }
}
