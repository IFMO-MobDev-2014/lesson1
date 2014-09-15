package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

class WhirlView extends SurfaceView implements SurfaceHolder.Callback {
    int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080,
            0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    int[][] field;
    int[][] bufferField;
    int[][] tempField;
    int[] colors;
    final int width = 240;
    final int height = 320;
    float scaleX;
    float scaleY;
    final Object locker = new Object();
    boolean updatingField = false;
    DrawThread drawThread;
    FieldUpdater fieldUpdater;

    public WhirlView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public void resume() {
        if (drawThread != null) {
            drawThread.setRunning(true);
        }

        if (fieldUpdater != null) {
            fieldUpdater.setRunning(true);
        }
    }

    public void pause() {
        if (drawThread != null) {
            drawThread.setRunning(false);
        }

        if (fieldUpdater != null) {
            fieldUpdater.setRunning(false);
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scaleX = (float)(w) / width;
        scaleY = (float)(h) / height;
        initField();
    }

    void initField() {
        colors = new int[height * width];
        field = new int[width][height];
        bufferField = new int[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateColors(int beginX, int beginY, int endX, int endY) {
        for (int y = beginY; y < endY; y++) {
            for (int x = beginX; x < endX; x++) {
                bufferField[x][y] = field[x][y];
                int newColor = field[x][y] + 1;

                if (newColor >= MAX_COLOR) {
                    newColor -= MAX_COLOR;
                }

                if (newColor == field[x - 1][y - 1])
                    bufferField[x][y] = field[x - 1][y - 1];
                else if (newColor == field[x - 1][y])
                    bufferField[x][y] = field[x - 1][y];
                else if (newColor == field[x - 1][y + 1])
                    bufferField[x][y] = field[x - 1][y + 1];
                else if (newColor == field[x][y - 1])
                    bufferField[x][y] = field[x][y - 1];
                else if (newColor == field[x][y])
                    bufferField[x][y] = field[x][y];
                else if (newColor == field[x][y + 1])
                    bufferField[x][y] = field[x][y + 1];
                else if (newColor == field[x + 1][y - 1])
                    bufferField[x][y] = field[x + 1][y - 1];
                else if (newColor == field[x + 1][y])
                    bufferField[x][y] = field[x + 1][y];
                else if (newColor == field[x + 1][y + 1])
                    bufferField[x][y] = field[x + 1][y + 1];

                colors[y * width + x] = palette[field[x][y]];
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        drawThread = new DrawThread(getHolder());
        drawThread.setRunning(true);
        drawThread.start();
        fieldUpdater = new FieldUpdater();
        fieldUpdater.setRunning(true);
        fieldUpdater.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        drawThread.setRunning(false);
        boolean trying = true;
        while (trying) {
            try {
                drawThread.join();
                fieldUpdater.join();
                trying = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class FieldUpdater extends Thread {
        private boolean running = false;

        public void processBoard() {
            int x2;
            int y2;

            for (int x = 0; x < width; x++) {
                bufferField[x][0] = field[x][0];
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        x2 = x + dx;
                        y2 = dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ( (field[x][0] + 1) % MAX_COLOR == field[x2][y2]) {
                            bufferField[x][0] = field[x2][y2];
                        }
                    }
                }
            }

            for (int x = 0; x < width; x++) {
                bufferField[x][height - 1] = field[x][height - 1];
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        x2 = x + dx;
                        y2 = height - 1 + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ( (field[x][height - 1] + 1) % MAX_COLOR == field[x2][y2]) {
                            bufferField[x][height - 1] = field[x2][y2];
                        }
                    }
                }
            }

            for (int y = 0; y < height; y++) {
                bufferField[0][y] = field[0][y];
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        x2 = dx;
                        y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ( (field[0][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            bufferField[0][y] = field[x2][y2];
                        }
                    }
                }
            }

            for (int y = 0; y < height; y++) {
                bufferField[width - 1][y] = field[width - 1][y];

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        x2 = width - 1 + dx;
                        y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ( (field[width - 1][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            bufferField[width - 1][y] = field[x2][y2];
                        }
                    }
                }
            }
        }

        public void updateField() {
            Thread top = new Updater(1, 1, width - 1, height / 2);
            Thread bottom = new Updater(1, height / 2, width - 1, height - 1);
            top.start();
            bottom.start();

            processBoard();

            try {
                top.join();
                bottom.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (locker) {
                while (updatingField) {
                    try {
                        locker.wait(1);
                    } catch (InterruptedException ignored) {

                    }
                }
                tempField = field;
                field = bufferField;
                bufferField = tempField;
                updatingField = true;
                locker.notifyAll();
            }
        }

        public void setRunning(boolean run) {
            running = run;
        }

        @Override
        public void run() {
            while (running) {
                updateField();
            }
        }
    }

    class Updater extends Thread {
        private int beginX;
        private int beginY;
        private int endX;
        private int endY;

        Updater(int beginX, int beginY, int endX, int endY) {
            this.beginX = beginX;
            this.beginY = beginY;
            this.endX = endX;
            this.endY = endY;
        }

        @Override
        public void run() {
            updateColors(beginX, beginY, endX, endY);
        }
    }

    class DrawThread extends Thread {
        private FPSCounter fpsCounter = new FPSCounter();
        private boolean running = false;
        private SurfaceHolder holder;

        public DrawThread(SurfaceHolder holder) {
            this.holder = holder;
        }

        public void setRunning(boolean run) {
            running = run;
        }

        @Override
        public void run() {
            while (running) {
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas(null);
                synchronized (locker) {
                    while (!updatingField) {
                        try {
                            locker.wait(1);
                        } catch (InterruptedException ignored) {

                        }
                    }
                    updatingField = false;

                    try {
                        canvas.scale(scaleX, scaleY);
                        canvas.drawBitmap(colors, 0, width, 0, 0, width, height, false, null);
                    } catch (NullPointerException e) {
                        locker.notifyAll();
                        continue;
                    }
                    locker.notifyAll();
                }
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                fpsCounter.logFrame();
            }
        }
    }

    public class FPSCounter {
        long startTime = System.nanoTime();
        int frames = 0;

        public void logFrame() {
            frames++;
            if (System.nanoTime() - startTime >= 1000000000) {
                Log.d("FPS", "fps: " + frames);
                frames = 0;
                startTime = System.nanoTime();
            }
        }
    }
}