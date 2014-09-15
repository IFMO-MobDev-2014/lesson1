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
    char[][] field, bufferField, tempField;
    int[] colors;
    int width = 0;
    int height = 0;
    float scaleX, scaleY;
    DrawThread drawThread;

    public WhirlView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public void resume() {
        if (drawThread != null) {
            drawThread.setRunning(true);
        }
    }

    public void pause() {
        if (drawThread != null) {
            drawThread.setRunning(false);
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = 240;
        height = 320;
        scaleX = (float)(w) / width;
        scaleY = (float)(h) / height;
        initField();
    }

    void initField() {
        colors = new int[height * width];
        field = new char[width][height];
        bufferField = new char[width][height];
        Random rand = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = (char)rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateColors(int beginX, int beginY, int endX, int endY) {
        int x2, y2;
        for (int y = beginY; y < endY; y++) {
            for (int x = beginX; x < endX; x++) {
                bufferField[x][y] = field[x][y];
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        x2 = x + dx;
                        y2 = y + dy;
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
                        if (field[x][y] + 1 == MAX_COLOR) {
                            if (0 == field[x2][y2]) {
                                bufferField[x][y] = field[x2][y2];
                            }
                        } else {
                            if (field[x][y] + 1 == field[x2][y2]) {
                                bufferField[x][y] = field[x2][y2];
                            }
                        }
                    }
                }
                colors[y * width + x] = palette[field[x][y]];
            }
        }
    }

    class Updater extends Thread {
        private int beginX, beginY, endX, endY;

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

    void updateField() {
        Thread leftTop = new Updater(0, 0, width / 2, height / 2);

        Thread rightTop = new Updater(width / 2, 0, width, height / 2);

        Thread leftBottom = new Updater(0, height / 2, width / 2, height);

        Thread rightBottom = new Updater(width / 2, height / 2, width, height);

        leftTop.start();
        rightTop.start();
        leftBottom.start();
        rightBottom.start();
        try {
            leftTop.join();
            rightTop.join();
            leftBottom.join();
            rightBottom.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        tempField = field;
        field = bufferField;
        bufferField = tempField;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        drawThread = new DrawThread(getHolder());
        drawThread.setRunning(true);
        drawThread.start();
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
                trying = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class DrawThread extends Thread {
        FPSCounter fpsCounter = new FPSCounter();
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
                WhirlView.this.updateField();
                long finishTime = System.nanoTime();
                Log.i("TIME", "Update field: " + (finishTime - startTime) / 1000000);
                startTime = finishTime;
                Canvas canvas = holder.lockCanvas(null);
                try {
                    canvas.scale(scaleX, scaleY);
                    canvas.drawBitmap(colors, 0, width, 0, 0, width, height, false, null);
                } catch (NullPointerException e) {
                    continue;
                }
                holder.unlockCanvasAndPost(canvas);
                finishTime = System.nanoTime();
                Log.i("TIME", "Drawing: " + (finishTime - startTime) / 1000000);
                fpsCounter.logFrame();
            }
        }

        public class FPSCounter {
            long startTime = System.nanoTime();
            int frames = 0;

            public void logFrame() {
                frames++;
                if (System.nanoTime() - startTime >= 1000000000) {
                    Log.d("FPSCounter", "fps: " + frames);
                    frames = 0;
                    startTime = System.nanoTime();
                }
            }
        }
    }
}