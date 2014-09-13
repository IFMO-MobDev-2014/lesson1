package ru.ifmo.md.lesson1;

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

    final int width = 240;                          //only need to render 240x320 so made them constant
    final int height = 320;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    Paint paint = new Paint();                      //constructing paint only once
    float xScale = 0;                               //scale for drawing constant size BitMap on any resolution
    float yScale = 0;
    int[] colours = null;                            //BitMap of colours
    int[][] field = new int[width][height];          //new field for single memory allocation
    int[][] field2 = new int[width][height];
    int[][] field3 = new int[width][height];

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
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        xScale = (float)w/width;
        yScale = (float)h/height;
        initField();
    }

    void initField() {

        colours = new int[width*height];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
                field2[x][y] = field[x][y];
                field3[x][y] = field[x][y];
            }
        }
    }

    void updateField() {
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                int colour = (field[x][y]+1) % MAX_COLOR;       //calculating colour needed for match once
                field2[x][y] = field[x][y];
                loop:                                           //mark for exiting inner cycles

                for (int dx=-1; dx<=1; dx++) {                  //checking all squares around current
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2<0) x2 += width;
                        if (y2<0) y2 += height;
                        if (x2>=width) x2 -= width;
                        if (y2>=height) y2 -= height;
                        if (colour  == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                            break loop;                         //found match - no need for further search
                        }
                    }
                }
            }
        }
        field3 = field;                                         //swapping references
        field = field2;
        field2 = field3;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                colours[x + y * width] = palette[field[x][y]];                              //calculating BitMap
            }
        }
        canvas.scale(xScale, yScale);
        canvas.drawBitmap(colours, 0, width, 0, 0, width, height, false, paint);             //drawing scaled bits is far better than drawing squares

    }
}