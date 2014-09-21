package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    private static final int MAX_COLOR = 10;
    private static final int[] PALETTE = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    private static final int SCALE = 4;
    private static final Matrix SCALE_MATRIX = new Matrix();
    static {
        SCALE_MATRIX.setScale(SCALE, SCALE);
    }
    private static final Paint BLANK_PAINT = new Paint();

    private int[][] field = null;
    private int[][] tempField = null;
    private int[] colors = null;
    private volatile boolean running = false;

    private int width = 0;
    private int height = 0;
    private boolean looped = false;

    private SurfaceHolder holder;
    private Thread thread = null;

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

    @Override
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
        width = w / SCALE;
        height = h / SCALE;
        initField();
    }

    private void initField() {
        field = new int[height][width];
        tempField = new int[height][width];
        colors = new int[width * height];
        looped = false;
        Random rand = new Random();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                field[y][x] = rand.nextInt(MAX_COLOR);
                colors[y * width + x] = PALETTE[field[y][x]];
            }
        }
    }

    private void updateField() {
        if (looped) {
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    field[y][x] = field[y][x] + 1;
                    if (field[y][x] == MAX_COLOR) field[y][x] = 0;
                    colors[y * width + x] = PALETTE[field[y][x]];
                }
            }
        } else {
            looped = true;
            for (int y = 0; y < height; y++) {
                int y1 = y - 1 < 0 ? height - 1 : y - 1;
                int y2 = y + 1 >= height ? 0 : y + 1;

                for (int x = 0; x < width; x++) {
                    int x1 = x - 1 < 0 ? width - 1 : x - 1;
                    int x2 = x + 1 >= width ? 0 : x + 1;

                    int nextColor = field[y][x] + 1;
                    if (nextColor == MAX_COLOR) nextColor = 0;

                    if (field[y1][x1] == nextColor || field[y1][x] == nextColor || field[y1][x2] == nextColor ||
                            field[y][x1] == nextColor || field[y][x2] == nextColor ||
                            field[y2][x1] == nextColor || field[y2][x] == nextColor || field[y2][x2] == nextColor) {
                        tempField[y][x] = nextColor;
                        colors[y * width + x] = PALETTE[nextColor];
                    } else {
                        tempField[y][x] = field[y][x];
                        looped = false;
                    }
                }
            }
            {
                int[][] t = tempField;
                tempField = field;
                field = t;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.setMatrix(SCALE_MATRIX);
        canvas.drawBitmap(colors, 0, width, 0, 0, width, height, false, BLANK_PAINT);
    }
}
