package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null;
    int [] colors = null;
    int width = 240;
    int height = 320;
    float scalewidth = 0;
    float scaleheight = 0;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

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

    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                updateField();
                draw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                Log.i("FPS", "Circle: " + (double)1000 / ((double)(finishTime - startTime) / (double)1000000));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scalewidth = (float)w/width;
        scaleheight = (float)h/height;
        initField();
    }

    void initField() {
        field = new int[width][height];
        colors = new int [width*height];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int[][] field2 = new int[width][height];


        for (int x=1; x<width-1; x++) {
            for (int y = 1; y < height - 1; y++) {

                field2[x][y] = field[x][y];

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if ((field[x][y] + 1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }

        field2[0][0] = field[0][0];
        if ((field[0][0] + 1) % MAX_COLOR == field[width - 1][height - 1])
            field2[0][0] = field[width - 1][height - 1];

        for (int dy = 0; dy <= 1; dy++) {
            if ((field[0][0] + 1) % MAX_COLOR == field[width - 1][dy])
                field2[0][0] = field[width - 1][dy];
        }

        for (int dx = 0; dx <=1; dx++) {
            if ((field[0][0] + 1) % MAX_COLOR == field[dx][height - 1])
                field2[0][0] = field[dx][height - 1];
            for (int dy = 0; dy <=1; dy++)
                if ((field[0][0] + 1) % MAX_COLOR == field[dx][dy])
                    field2[0][0] = field[dx][dy];
        }

        field2[width - 1][0] = field[width - 1][0];
        if ((field[width - 1][0] + 1) % MAX_COLOR == field[0][height - 1])
            field2[width - 1][0] = field[0][height - 1];

        for (int dy = 0; dy <= 1; dy++)
            if ((field[width - 1][0] + 1) % MAX_COLOR == field[0][dy])
                field2[width - 1][0] = field[0][dy];

        for (int dx = -1; dx <=0; dx++) {
            if ((field[width - 1][0] + 1) % MAX_COLOR == field[width - 1 + dx][height - 1])
                field2[width - 1][0] = field[width - 1 + dx][height - 1];
            for (int dy = 0; dy <=1; dy++)
                if ((field[width - 1][0] + 1) % MAX_COLOR == field[width - 1 + dx][dy])
                    field2[width - 1][0] = field[width - 1 + dx][dy];
        }

        field2[0][height - 1] = field[0][height - 1];
        if ((field[0][height - 1] + 1) % MAX_COLOR == field[width - 1][0])
            field2[0][height - 1] = field[width - 1][0];

        for (int dy = -1; dy <= 0; dy++) {
            if ((field[0][height - 1] + 1) % MAX_COLOR == field[width - 1][height - 1 + dy])
                field2[0][height - 1] = field[width - 1][height - 1 + dy];
        }

        for (int dx = 0; dx <=1; dx++) {
            if ((field[0][height - 1] + 1) % MAX_COLOR == field[dx][0])
                field2[0][height - 1] = field[dx][0];
            for (int dy = -1; dy <= 0; dy++)
                if ((field[0][height - 1] + 1) % MAX_COLOR == field[dx][height - 1 + dy])
                    field2[0][height - 1] = field[dx][height - 1 + dy];
        }

        field2[width - 1][height - 1] = field[width - 1][height - 1];
        if ((field[width - 1][height - 1] + 1) % MAX_COLOR == field[0][0])
            field2[width - 1][height - 1] = field[0][0];

        for (int dy = -1; dy <= 0; dy++)
            if ((field[width - 1][height - 1] + 1) % MAX_COLOR == field[0][height - 1 + dy])
                field2[width - 1][height - 1] = field[0][height - 1 + dy];

        for (int dx = -1; dx <=0; dx++) {
            if ((field[width - 1][height - 1] + 1) % MAX_COLOR == field[width - 1 + dx][0])
                field2[width - 1][height - 1] = field[width - 1 + dx][0];
            for (int dy = -1; dy <=0; dy++)
                if ((field[width - 1][height - 1] + 1) % MAX_COLOR == field[width - 1 + dx][height -  1 + dy])
                    field2[width - 1][height - 1] = field[width - 1 + dx][height - 1 + dy];
        }

        for (int  y = 1; y < height - 1; y++) {
            field2[0][y] = field[0][y];
            for (int dy = -1; dy <= 1; dy++)
                if ((field[0][y] + 1) % MAX_COLOR == field[width - 1][y + dy])
                    field[0][y] = field[width - 1][y + dy];
            for (int dx = 0; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if ((field[0][y] + 1) % MAX_COLOR == field[dx][y + dy])
                        field2[0][y] = field[dx][y + dy];

        }

        for (int  y = 1; y < height - 1; y++) {
            field2[width - 1][y] = field[width - 1][y];
            for (int dy = -1; dy <= 1; dy++)
                if ((field[width - 1][y] + 1) % MAX_COLOR == field[0][y + dy])
                    field[width - 1][y] = field[0][y + dy];
            for (int dx = -1; dx <= 0; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if ((field[width - 1][y] + 1) % MAX_COLOR == field[width - 1 + dx][y + dy])
                        field2[width - 1][y] = field[width - 1 + dx][y + dy];

        }

        for (int  x = 1; x < width - 1; x++) {
            field2[x][0] = field[x][0];
            for (int dx = -1; dx <= 1; dx++) {
                if ((field[x][0] + 1) % MAX_COLOR == field[x + dx][height - 1])
                    field2[x][0] = field[x + dx][height - 1];
                for (int dy = 0; dy <= 1; dy++)
                    if ((field[x][0] + 1) % MAX_COLOR == field[x + dx][dy])
                        field2[x][0] = field[x + dx][dy];
            }
        }

        for (int  x = 1; x < width - 1; x++) {
            field2[x][height - 1] = field[x][height - 1];
            for (int dx = -1; dx <= 1; dx++) {
                if ((field[x][height - 1] + 1) % MAX_COLOR == field[x][0])
                    field2[x][height - 1] = field[x][0];
                for (int dy = -1; dy <= 0; dy++)
                    if ((field[x][height - 1] + 1) % MAX_COLOR == field[x + dx][height - 1 + dy])
                        field2[x][height - 1] = field[x + dx][height - 1 + dy];
            }
        }

        field = field2;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.scale(scalewidth, scaleheight);
        Paint paint = new Paint();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                colors[x + y * width] = palette[field[x][y]];
            }
        }
        canvas.drawBitmap(colors, 0, width, 0, 0, width, height, false, paint);
    }
}