package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    int width = 240;
    int height = 320;
    TCount t = new TCount();
    private static final Object lock = new Object();
    float scaleX = 0;
    float scaleY = 0;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
// /*some hypnotic*/    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF, 0xFF86E500, 0xFF40E100, 0xFF00DD03, 0xFF00D944, 0xFF00D583, 0xFF00D2BF, 0xFF00A2CE, 0xFF0062CA, 0xFF0024C6, 0xFF1600C2, 0xFF4E00BF};
//    int[] palette = {0xFF43E4F4, 0xFF44F4DC, 0xFF45F4B5, 0xFF46F58F, 0xFF47F569, 0xFF4DF648, 0xFF75F64A, 0xFF9CF74B, 0xFFC3F74C, 0xFFE9F74D, 0xFFF8E04E, 0xFFF8BB4F, 0xFFF99751, 0xFFF97252, 0xFFFA5358, 0xFFFA547E, 0xFFFA56A3};
    final int MAX_COLOR = palette.length;
    int [][] field3 = new int[height][width];
    boolean ready = false;
    int [] bitmapArrayForDrawing = new int [width * height];
    SurfaceHolder holder;
    Thread thread = null;
    Thread drawThread = null;
    volatile boolean running = false;

    private class TCount implements Runnable{
        int [][] field = new int[height][width];
        int [][] field2 = new int[height][width];
        volatile boolean r = false;
        @Override
        public void run() {
            initField();
            while (r)
                updateField();
        }
        void initField() {
            Random rand = new Random();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    field[y][x] = rand.nextInt(MAX_COLOR);
                }
            }
        }
        void updateField() {
            int x2, y2, dx, dy;
//        Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
            long startTime = System.nanoTime();
            for (int x = 0; x < width; x++) {
                int y = 0;
                field2[y][x] = field[y][x];

                for (dx = -1; dx <= 1; dx++) {
                    for (dy = -1; dy <= 1; dy++) {
                        x2 = x + dx;
                        y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ( (field[y][x] + 1) % MAX_COLOR == field[y2][x2]) {
                            field2[y][x] = field[y2][x2];
                        }
                    }
                }
            }

            for (int x = 0; x < width; x++) {
                int y = height - 1;
                field2[y][x] = field[y][x];

                for (dx = -1; dx <= 1; dx++) {
                    for (dy = -1; dy <= 1; dy++) {
                        x2 = x + dx;
                        y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ( (field[y][x] + 1) % MAX_COLOR == field[y2][x2]) {
                            field2[y][x] = field[y2][x2];
                        }
                    }
                }
            }

            for (int y = 0; y < height; y++) {
                int x = 0;
                field2[y][x] = field[y][x];
                for (dx = -1; dx <= 1; dx++) {
                    for (dy = -1; dy <= 1; dy++) {
                        x2 = x + dx;
                        y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ( (field[y][x] + 1) % MAX_COLOR == field[y2][x2]) {
                            field2[y][x] = field[y2][x2];
                        }
                    }
                }
            }

            for (int y = 0; y < height; y++) {
                int x = width - 1;
                field2[y][x] = field[y][x];

                for (dx = -1; dx <= 1; dx++) {
                    for (dy = -1; dy <= 1; dy++) {
                        x2 = x + dx;
                        y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        if ( (field[y][x] + 1) % MAX_COLOR == field[y2][x2]) {
                            field2[y][x] = field[y2][x2];
                        }
                    }
                }
            }
//            long finishTime = System.nanoTime();
//            Log.i("CYCLE1", "Circle: " + (finishTime - startTime) / 1000000);
            startTime = System.nanoTime();
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    field2[y][x] = field[y][x];
                    int nextColor = (field[y][x] + 1);
                    if (nextColor >= MAX_COLOR)
                        nextColor -= MAX_COLOR;
                    if (nextColor == field[y - 1][x - 1])
                        field2[y][x] = field[y - 1][x - 1];
                    else if (nextColor == field[y][x - 1])
                        field2[y][x] = field[y][x - 1];
                    else if (nextColor == field[y + 1][x - 1])
                        field2[y][x] = field[y + 1][x - 1];
                    else if (nextColor == field[y - 1][x])
                        field2[y][x] = field[y - 1][x];
                    else if (nextColor == field[y][x])
                        field2[y][x] = field[y][x];
                    else if (nextColor == field[y + 1][x])
                        field2[y][x] = field[y + 1][x];
                    else if (nextColor == field[y - 1][x + 1])
                        field2[y][x] = field[y - 1][x + 1];
                    else if (nextColor == field[y][x + 1])
                        field2[y][x] = field[y][x + 1];
                    else if (nextColor == field[y + 1][x + 1])
                        field2[y][x] = field[y + 1][x + 1];
                }
            }
            int [][] tmp;
            synchronized (lock) {
                while (ready) {
                    try {
                        lock.wait(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                tmp = field3;
                field3 = field;
                field = field2;
                field2 = tmp;
                ready = true;
                lock.notifyAll();
            }
//            finishTime = System.nanoTime();
//            Log.i("CYCLE2", "Circle: " + (finishTime - startTime) / 1000000);
        }
    }

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
    }

    public void resume() {
        running = true;
        t.r = true;
        ready = false;
        thread = new Thread(this);
        drawThread = new Thread(t);
        drawThread.setPriority(Thread.MAX_PRIORITY);
        drawThread.start();
        thread.start();
    }

    public void pause() {
        try {
          //  synchronized ()
            t.r = false;
            drawThread.join();
            running = false;
            thread.join();
        } catch (InterruptedException ignore) {}
    }

    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                synchronized (lock) {
                    while (!ready) {
                        try {
                            lock.wait(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    ready = false;
                    draw(canvas);
                    lock.notifyAll();
                }
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("FPS", "" +  1000000000.0f / (finishTime - startTime));
//                try {
//                    Thread.sleep(4);
//                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
            scaleX = (float)w/width;
            scaleY = (float)h/height;
    }

    @Override
    public void draw(Canvas canvas) {
//        long startTime = System.nanoTime();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bitmapArrayForDrawing[y * width + x] = palette[field3[y][x]];
            }
        }
            canvas.scale(scaleX, scaleY);
            canvas.drawBitmap(bitmapArrayForDrawing, 0, width, 0, 0, width, height, false, null);
//        long finishTime = System.nanoTime();
//        Log.i("onDrawCycle", "" + (finishTime - startTime) / 1000000);
    }
}