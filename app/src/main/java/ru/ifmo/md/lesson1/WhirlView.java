package ru.ifmo.md.lesson1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

class WhirlView extends SurfaceView implements Runnable {
    private int[][][] field = null;
    private int currentState = 0;
    private final int height = 320;
    private final int width = 240;

    private long lastCalculation;
    private int frames;
    private double fps = 0;

    private final int MAX_COLOR = 20;
    private Paint p = new Paint();
    {
        p.setAntiAlias(true);
        p.setTextSize(15);
    }

    private int[] palette;

    private int[] colors;
    private SurfaceHolder holder;
    private Thread thread;
    private volatile boolean running = false;

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

    public WhirlView(Context context) {
        super(context);
        initField();
        genPalette();
        holder = getHolder();
    }

    private void genPalette() {
        palette = new int[MAX_COLOR];
        float maxHue = 360f;
        float[] hsv = new float[3];
        for (int i = 0; i < MAX_COLOR; i++) {
            hsv[0] = (MAX_COLOR - i) * maxHue / MAX_COLOR;
            hsv[1] = 0.85f;
            hsv[2] = 0.85f;
            palette[i] = Color.HSVToColor(hsv);
        }
    }

    public void calculateFPS() {
        frames++;
        long now = SystemClock.uptimeMillis();
        long elapsedTime = now - lastCalculation;
        long FPS_CALC_INTERVAL = 1000;
        if (elapsedTime > FPS_CALC_INTERVAL) {
            fps = frames * FPS_CALC_INTERVAL / elapsedTime;
            frames = 0;
            lastCalculation = now;
        }
    }

    @SuppressLint("WrongCall")
    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                updateField();
                calculateFPS();
                Canvas canvas = holder.lockCanvas();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    void initField() {
        colors = new int[width * height];
        field = new int[2][width][height];
        Random rnd = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[currentState][x][y] = rnd.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int i = 0;
        int newState = 1 - currentState;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int nextColor = (field[currentState][x][y] + 1) % MAX_COLOR;
                if (checkAdjucentCell(x - 1, y - 1, nextColor) ||
                        checkAdjucentCell(x, y - 1, nextColor) ||
                        checkAdjucentCell(x - 1, y, nextColor) ||
                        checkAdjucentCell(x + 1, y - 1, nextColor) ||
                        checkAdjucentCell(x - 1, y + 1, nextColor) ||
                        checkAdjucentCell(x, y + 1, nextColor) ||
                        checkAdjucentCell(x + 1, y, nextColor) ||
                        checkAdjucentCell(x + 1, y + 1, nextColor)) {

                    field[newState][x][y] = nextColor;
                } else {
                    field[newState][x][y] = field[currentState][x][y];
                }
                colors[i] = palette[field[newState][x][y]];
                i++;
            }
        }
        currentState = newState;
    }

    private boolean checkAdjucentCell(int x, int y, int color) {
        if (x == -1) {
            x = width - 1;
        }
        if (x == width) {
            x = 0;
        }
        if (y == -1) {
            y = height - 1;
        }
        if (y == height) {
            y = 0;
        }
        return field[currentState][x][y] == color;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(canvas.getWidth() * 1f / height, canvas.getHeight() * 1f / width);
        canvas.drawBitmap(colors, 0, height, 0, 0, height, width, false, null);
        canvas.drawText(Math.ceil(fps * 10) / 10 + " FPS", 240, 230, p);
    }

}
