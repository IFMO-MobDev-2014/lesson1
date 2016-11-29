package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
    int[][] field = null;
    int width = 0;
    int height = 0;
    int[][] field2 = null;
    int[] colors = null;
    int pos = 0;
    final int MAX_COLOR = 10;
    final int STD_WIDTH = 240;
    final int STD_HEIGHT = 320;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    boolean ok = false;

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
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w;
        height = h;
        initField();
    }

    void initField() {
        field = new int[STD_WIDTH][STD_HEIGHT];
        field2 = new int[STD_WIDTH][STD_HEIGHT];
        colors = new int[STD_WIDTH * STD_HEIGHT];
        Random rand = new Random();
        for (int x = 0; x < STD_WIDTH; x++) {
            for (int y = 0; y < STD_HEIGHT; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x = 0; x < STD_WIDTH; x++) {
            for (int y = 0; y < STD_HEIGHT; y++) {

                field2[x][y] = field[x][y];
                ok = false;
                for (int dx = 1; dx >= -1; dx--) {
                    if (ok) break;
                    for (int dy = 1; dy >= -1; dy--) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += STD_WIDTH;
                        if (y2 < 0) y2 +=  STD_HEIGHT;
                        if (x2 >= STD_WIDTH) x2 -= STD_WIDTH;
                        if (y2 >= STD_HEIGHT) y2 -=  STD_HEIGHT;
                        int tmp = field[x][y] + 1;
                        if (tmp >= MAX_COLOR) tmp -= MAX_COLOR;
                        if (tmp == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                            ok = true;
                            break;
                        }
                    }
                }
            }
        }
        //field = field2;
        int[][] tmp = field;
        field = field2;
        field2 = tmp;
//        for (int x = 0; x < width; x++)
//            System.arraycopy(field2[x], 0, field[x], 0, height);
    }

    Rect dst;
    Bitmap image;

    @Override
    public void onDraw(Canvas canvas) {
        if (dst == null) {
            dst = new Rect(0, 0, width, height);
        }
        if (image == null) {
            image = Bitmap.createBitmap(STD_WIDTH,  STD_HEIGHT, Bitmap.Config.ARGB_8888);
        }
        pos = 0;
        for (int y = 0; y < STD_HEIGHT; y++) {
            for (int x = 0; x < STD_WIDTH; x++) {
                colors[pos] = palette[field[x][y]];
                pos++;
            }
        }
        image.setPixels(colors, 0, STD_WIDTH, 0, 0, STD_WIDTH,  STD_HEIGHT);
        canvas.drawBitmap(image, null, dst, null);
//                canvas.drawRect(x * scale, y * scale, (x + 1) * scale, (y + 1) * scale, paint);
        //canvas.drawBitmap(colors, 0, width, 0, 0, width, height, true, null);

    }
}
