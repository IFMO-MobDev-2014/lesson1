package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 * Optimized by vadimsemenov on 14/09/14.
 */

class WhirlView extends SurfaceView implements Runnable {
    private static final int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000,
            0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    private static final int MAX_COLOR = palette.length;

    private static final int MIN_SIDE = 240;
    private static final int MAX_SIDE = 320;

    private int[][] field = null;
    private int[] colors = null;

    private int width = MIN_SIDE;
    private int height = MAX_SIDE;
    private float widthScale;
    private float heightScale;

    private SurfaceHolder holder;
    private Thread thread = null;
    private volatile boolean running = false;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
    }

    public void resume() {
        running = true;
        thread = new Thread(this);
        thread.setPriority(Thread.MAX_PRIORITY); // in Java we trust!
        thread.start();
    }

    public void pause() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException ignore) {
        }
    }

    private long framesCounter, allTime;

    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.currentTimeMillis();
                Canvas canvas = holder.lockCanvas();
                updateField();
                drawIt(canvas); // seems like we must not call on...() methods by android convention
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.currentTimeMillis();
                // Log.i("TIME", "Circle: " + (finishTime - startTime));
                // Log.i("FPS", "_Circle: " + 1000. / (finishTime - startTime));
                framesCounter++;
                allTime += finishTime - startTime;
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {
                }
            }
        }
        Log.i("FPS", "Summary: " + framesCounter * 1000. / allTime);
    }

    public void drawIt(Canvas canvas) {
        canvas.scale(widthScale, heightScale);
        canvas.drawBitmap(colors, 0, width, 0, 0, width, height, false, null);
        // low level method to improve performance
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        if (w < h) {
            width = MIN_SIDE;
            height = MAX_SIDE;
        } else {
            width = MAX_SIDE;
            height = MIN_SIDE;
        }
        widthScale = (float) w / width;
        heightScale = (float) h / height;
        initField();
    }

    private Random random = new Random(7); // todo: remove seed!

    private void initField() {
        field = new int[height][width]; // changed dimensions to improve colors caching
        field2 = new int[height][width];
        colors = new int[height * width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                field[y][x] = random.nextInt(MAX_COLOR);
            }
        }
    }

    private int[][] field2 = null; // avoid creating unnecessary int[]

    private void updateField() {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                field2[y][x] = field[y][x];

                outer:
                for (int dx = -1; dx <= 1; ++dx) {
                    for (int dy = -1; dy <= 1; ++dy) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) {
                            x2 += width;
                        }
                        if (y2 < 0) {
                            y2 += height;
                        }
                        if (x2 >= width) {
                            x2 -= width;
                        }
                        if (y2 >= height) {
                            y2 -= height;
                        }
                        if ((field[y][x] + 1) % MAX_COLOR == field[y2][x2]) {
                            field2[y][x] = field[y2][x2];
                            colors[y * width + x] = palette[field2[y][x]];
                            break outer;
                        }
                    }
                }
            }
        }
        int[][] tmp = field;
        field = field2;
        field2 = tmp;
    }
}