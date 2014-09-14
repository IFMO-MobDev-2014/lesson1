package ru.ifmo.md.lesson1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    private static final int WIDTH = 240;
    private static final int HEIGHT = 320;
    private static final int SIZE = WIDTH * HEIGHT;
    private static final int[] PALETTE = {
            0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080,
            0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    private static final int MAX_COLOR = PALETTE.length;


    private final FpsLogger fpsLogger = new FpsLogger(2);
    private final SurfaceHolder holder;

    private int[] field = new int[SIZE];
    private int[] field2 = new int[SIZE];
    private final int[] colors = new int[SIZE];

    private float scaleX;
    private float scaleY;

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
                Canvas canvas = holder.lockCanvas();
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                fpsLogger.update();
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scaleX = 1F * w / WIDTH;
        scaleY = 1F * h / HEIGHT;
        initField();
    }

    private void initField() {
        Random rand = new Random();
        for (int i = 0; i < SIZE; i++) {
            field[i] = rand.nextInt(MAX_COLOR);
        }
    }

    private void updateField() {
        for (int i = 0; i < SIZE; i++) {
            field2[i] = field[i];
            int next = (field[i] + 1) % MAX_COLOR;
            all:
            for (int dy = -WIDTH; dy <= WIDTH; dy += WIDTH) {
                for (int dx = -1; dx <= 1; dx++) {
                    int j = (i + dy + dx + SIZE) % SIZE;
                    if (field[j] == next) {
                        field2[i] = next;
                        colors[i] = PALETTE[next];
                        break all;
                    }
                }
            }
        }

        int[] buf = field;
        field = field2;
        field2 = buf;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(scaleX, scaleY);
        canvas.drawBitmap(colors, 0, WIDTH, 0, 0, WIDTH, HEIGHT, false, null);
    }
}
