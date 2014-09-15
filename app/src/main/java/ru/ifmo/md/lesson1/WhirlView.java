package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    final int MAX_COLOR = 10;
    int currentField = 0;
    int[][][] field = null;
    int width = 240;
    int height = 320;
    Rect current = new Rect(0, 0, width, height);
    Rect scaled = current; // I think that onSizeChanged could be never called
    float scaleW = 4;
    float scaleH = 4;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    Bitmap bitmap = null;
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

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
        int i = 0;
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                updateField();
                draw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                Log.i("FPS", ((Double) (1000000000. * (++i) / (finishTime - startTime))).toString());
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scaleW = (float) w / width;
        scaleH = (float) h / height;
        scaled = new Rect(0, 0, w, h);
    }

    void initField() {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        currentField = 0;
        field = new int[2][width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[currentField][x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int newField = 1 ^ currentField;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                field[newField][x][y] = field[currentField][x][y];

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int x2 = (x + dx + width) % width;
                        int y2 = (y + dy + height) % height;
                        if ((field[currentField][x][y] + 1) % MAX_COLOR == field[currentField][x2][y2]) {
                            field[newField][x][y] = field[currentField][x2][y2];
                        }
                    }
                }
            }
        }
        currentField = newField;
    }

    @Override
    public void draw(Canvas canvas) {
        int oldField = 1 ^ currentField;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (field[currentField][x][y] != field[oldField][x][y]) {
                    bitmap.setPixel(x, y, palette[field[currentField][x][y]]);
                }
            }
        }

        canvas.drawBitmap(bitmap, current, scaled, null);
    }
}
