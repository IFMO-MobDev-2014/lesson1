package ru.ifmo.md.lesson1;

import android.annotation.SuppressLint;
import android.content.Context;
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
    private static final int MAX_COLOR = 10;
    private static final int WIDTH = 240;
    private static final int HEIGHT = 320;
    private static final int[] PALETTE = {
            0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080,
            0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};


    private final Random rand = new Random();
    private final Paint paint = new Paint();
    private final SurfaceHolder holder;

    private float scaleWidth;
    private float scaleHeight;
    private int[][] field = new int[WIDTH][HEIGHT];
    private int[][] field2 = new int[WIDTH][HEIGHT];

    private Thread thread = null;
    private volatile boolean running = false;

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

    @SuppressLint("WrongCall") // We really should call onDraw here
    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("WHIRL", "FPS: "
                        + String.format("%.2f", (finishTime - startTime) / 1000000.0));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scaleWidth = 1F * w / WIDTH;
        scaleHeight = 1F * h / HEIGHT;
        initField();
    }

    void initField() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {

                field2[x][y] = field[x][y];

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2 < 0) x2 += WIDTH;
                        if (y2 < 0) y2 += HEIGHT;
                        if (x2 >= WIDTH) x2 -= WIDTH;
                        if (y2 >= HEIGHT) y2 -= HEIGHT;
                        if ((field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }

        int[][] buf = field;
        field = field2;
        field2 = buf;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (field[x][y] != field2[x][y]) {
                    paint.setColor(PALETTE[field[x][y]]);
                    canvas.drawRect(x * scaleWidth, y * scaleHeight,
                            (x + 1) * scaleWidth, (y + 1) * scaleHeight, paint);
                }
            }
        }
    }
}
