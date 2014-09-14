package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Rect;
import android.graphics.Bitmap;

import java.util.Random;
import java.util.TreeMap;
import java.util.ArrayList;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null, field2 = null, field3 = null;
    int width = 320;
    int height = 240;
    final int MAX_COLOR = 10;
    final int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    int[] pixels;
    final Paint[] colors = new Paint[palette.length];
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

    Rect dst;
    Bitmap bitmap;

    boolean repetitionMode = false;
    TreeMap<Integer, Integer> historyChecker;
    ArrayList<Bitmap> history;
    int iteration;


    public WhirlView(Context context) {
        super(context);
        holder = getHolder();

        for (int i=0; i<palette.length; i++) {
            Paint paint = new Paint();
            paint.setColor(palette[i]);
            colors[i] = paint;
        }

        historyChecker = new TreeMap<Integer, Integer>();
        history = new ArrayList<Bitmap>();
        iteration = 0;
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

    public void run() {
        int superCounter = 0;
        float averageFPS = 0.0f;
        while (running) {
            while (running && repetitionMode) {
                if (holder.getSurface().isValid()) {
                    long startTime = System.nanoTime();
                    Canvas canvas = holder.lockCanvas();
                    //Log.i("info", "" + iteration + " " + history.size());
                    iteration++;
                    if (iteration >= history.size()) {
                        iteration = 0;
                    }
                    drawFromHistory(canvas, iteration);
                    holder.unlockCanvasAndPost(canvas);
                    long finishTime = System.nanoTime();
                    superCounter++;
                    averageFPS += 1000.0 / ((double)(finishTime - startTime) / 1000000);
                    if (superCounter == 100) {
                        Log.i("AVG FPS", "" + (averageFPS / 100));
                        superCounter = 0;
                        averageFPS = 0.0f;
                    }
                }
            }
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                updateField();
                draw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                superCounter++;
                averageFPS += 1000.0 / ((double)(finishTime - startTime) / 1000000);
                if (superCounter == 100) {
                    Log.i("AVG FPS", "" + (averageFPS / 100));
                    superCounter = 0;
                    averageFPS = 0.0f;
                }
                /*try {
                    Thread.sleep(1);
                } catch (InterruptedException ignore) {}*/
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        dst = new Rect(0, 0, w, h);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        pixels = new int[width * height];

        repetitionMode = false;
        historyChecker.clear();
        history.clear();
        iteration = 0;

        initField();
    }

    void initField() {
        field = new int[width][height];
        field2 = new int[width][height];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                int curColor = field[x][y] + 1;
                int newColor;
                if (curColor >= MAX_COLOR) {
                    curColor -= MAX_COLOR;
                }
                int x1 = x == 0 ? width - 1 : x - 1;
                int y1 = y == 0 ? height - 1 : y - 1;
                int x2 = x == width - 1 ? 0 : x + 1;
                int y2 = y == height - 1 ? 0 : y + 1;
                newColor = field[x2][y2];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x2][y];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x2][y1];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x][y2];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x][y1];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x1][y2];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x1][y];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
                newColor = field[x1][y1];
                if (curColor == newColor) {
                    field2[x][y] = newColor;
                    continue;
                }
            }
        }
        field3 = field;
        field = field2;
        field2 = field3;
        /*for (int x=0; x<width; x++) {
            System.arraycopy(field2[x], 0, field[x], 0, height);
        }*/
    }

    public void draw(Canvas canvas) {
        /*for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                canvas.drawRect(x*scale, y*scale, (x+1)*scale, (y+1)*scale, colors[field[x][y]]);
            }
        }*/
        int curPixel = 0;
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                pixels[curPixel++] = palette[field[x][y]];
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        canvas.drawBitmap(bitmap, null, dst, null);
        iteration++;
        if (iteration > 100) { // some empirical constant, may be tuned
            ArrayList<Integer> pixelList = new ArrayList<Integer>();
            for (int i=0; i<pixels.length; i++) {
                pixelList.add(pixels[i]);
            }
            int k = pixelList.hashCode();
            boolean loopFound = false;
            if (historyChecker.containsKey(k)) {
                Bitmap potentialBitmap = history.get(historyChecker.get(k));
                loopFound = bitmap.sameAs(potentialBitmap);
            } else {
                historyChecker.put(k, history.size());
            }
            Bitmap clonedBitmap = bitmap.createBitmap(pixels, width, height, Bitmap.Config.RGB_565);
            history.add(clonedBitmap);
            if (loopFound) {
                repetitionMode = true;
                historyChecker.clear();
            }
        }
    }

    public void drawFromHistory(Canvas canvas, int iteration) {
        canvas.drawBitmap(history.get(iteration), null, dst, null);
    }
}
