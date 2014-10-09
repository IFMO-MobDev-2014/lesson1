package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 */
class WhirlView extends SurfaceView implements Runnable {
    Rect dst;
    final int width = 240;
    final int height = 320;
    final int MAX_COLOR = 10;
    short[][] field = new short[width][height], field2 = new short[width][height];
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    Bitmap bMap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
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
                Log.i("FPS", ": " + 1000000000.0 / (double)(finishTime - startTime));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        dst = new Rect(0, 0, w, h);
        initField();
    }

    void initField() {
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = (short) rand.nextInt(MAX_COLOR);
                field2[x][y] = field[x][y];
            }
        }
    }

    void updateField() {
        int x2, y2;
        for (int x=0; x<width; x++) {
            x2 = x - 1;
            if (x2==-1) x2 = width - 1;
            y2 = height - 1;
            if ((field[x][0]+1) % MAX_COLOR == field[x2][y2]) {
                field2[x][0] = field[x2][y2];
                bMap.setPixel(x, 0, palette[field2[x][0]]);
                //continue;
            }

            if ( (field[x][0]+1) % MAX_COLOR == field[x2][0]) {
                field2[x][0] = field[x2][0];
                bMap.setPixel(x, 0, palette[field2[x][0]]);
                //continue;
            }

            y2 = 1;
            if ( (field[x][0]+1) % MAX_COLOR == field[x2][y2]) {
                field2[x][0] = field[x2][y2];
                bMap.setPixel(x, 0, palette[field2[x][0]]);
                //continue;
            }

            y2 = height - 1;
            if ( (field[x][0]+1) % MAX_COLOR == field[x][y2]) {
                field2[x][0] = field[x][y2];
                bMap.setPixel(x, 0, palette[field2[x][0]]);
                //continue;
            }

            y2 = 1;
            if ( (field[x][0]+1) % MAX_COLOR == field[x][y2]) {
                field2[x][0] = field[x][y2];
                bMap.setPixel(x, 0, palette[field2[x][0]]);
                //continue;
            }

            x2 = x + 1;
            y2 = height - 1;
            if (x2==width) x2 = 0;
            if ( (field[x][0]+1) % MAX_COLOR == field[x2][y2]) {
                field2[x][0] = field[x2][y2];
                bMap.setPixel(x, 0, palette[field2[x][0]]);
                //continue;
            }

            if ( (field[x][0]+1) % MAX_COLOR == field[x2][0]) {
                field2[x][0] = field[x2][0];
                bMap.setPixel(x, 0, palette[field2[x][0]]);
                //continue;
            }

            y2 = 1;
            if ( (field[x][0]+1) % MAX_COLOR == field[x2][y2]) {
                field2[x][0] = field[x2][y2];
                bMap.setPixel(x, 0, palette[field2[x][0]]);
            }
            for (int y=1; y<height-1; y++) {
                x2 = x - 1;
                y2 = y - 1;
                if (x2<0) x2 = width - 1;
                if ((field[x][y]+1) % MAX_COLOR == field[x2][y2]) {
                    field2[x][y] = field[x2][y2];
                    bMap.setPixel(x, y, palette[field2[x][y]]);
                    continue;
                }

                if ( (field[x][y]+1) % MAX_COLOR == field[x2][y]) {
                    field2[x][y] = field[x2][y];
                    bMap.setPixel(x, y, palette[field2[x][y]]);
                    continue;
                }

                y2 = y + 1;
                if ( (field[x][y]+1) % MAX_COLOR == field[x2][y2]) {
                    field2[x][y] = field[x2][y2];
                    bMap.setPixel(x, y, palette[field2[x][y]]);
                    continue;
                }

                y2 = y - 1;
                if ( (field[x][y]+1) % MAX_COLOR == field[x][y2]) {
                    field2[x][y] = field[x][y2];
                    bMap.setPixel(x, y, palette[field2[x][y]]);
                    continue;
                }

                y2 = y + 1;
                if ( (field[x][y]+1) % MAX_COLOR == field[x][y2]) {
                    field2[x][y] = field[x][y2];
                    bMap.setPixel(x, y, palette[field2[x][y]]);
                    continue;
                }

                x2 = x + 1;
                y2 = y - 1;
                if (x2==width) x2 = 0;
                if ( (field[x][y]+1) % MAX_COLOR == field[x2][y2]) {
                    field2[x][y] = field[x2][y2];
                    bMap.setPixel(x, y, palette[field2[x][y]]);
                    continue;
                }

                if ( (field[x][y]+1) % MAX_COLOR == field[x2][y]) {
                    field2[x][y] = field[x2][y];
                    bMap.setPixel(x, y, palette[field2[x][y]]);
                    continue;
                }

                y2 = y + 1;
                if ( (field[x][y]+1) % MAX_COLOR == field[x2][y2]) {
                    field2[x][y] = field[x2][y2];
                    bMap.setPixel(x, y, palette[field2[x][y]]);
                    continue;
                }
            }
            x2 = x - 1;
            y2 = height-2;
            if (x2<0) x2 = width - 1;
            if ((field[x][height-1]+1) % MAX_COLOR == field[x2][y2]) {
                field2[x][height-1] = field[x2][y2];
                bMap.setPixel(x, height-1, palette[field2[x][height-1]]);
                continue;
            }

            if ( (field[x][height-1]+1) % MAX_COLOR == field[x2][height-1]) {
                field2[x][height-1] = field[x2][height-1];
                bMap.setPixel(x, height-1, palette[field2[x][height-1]]);
                continue;
            }

            y2 = 0;
            if ( (field[x][height-1]+1) % MAX_COLOR == field[x2][y2]) {
                field2[x][height-1] = field[x2][y2];
                bMap.setPixel(x, height-1, palette[field2[x][height-1]]);
                continue;
            }

            y2 = height-2;
            if ( (field[x][height-1]+1) % MAX_COLOR == field[x][y2]) {
                field2[x][height-1] = field[x][y2];
                bMap.setPixel(x, height-1, palette[field2[x][height-1]]);
                continue;
            }

            y2 = 0;
            if ( (field[x][height-1]+1) % MAX_COLOR == field[x][y2]) {
                field2[x][height-1] = field[x][y2];
                bMap.setPixel(x, height-1, palette[field2[x][height-1]]);
                continue;
            }

            x2 = x + 1;
            y2 = height-2;
            if (x2==width) x2 = 0;
            if ( (field[x][height-1]+1) % MAX_COLOR == field[x2][y2]) {
                field2[x][height-1] = field[x2][y2];
                bMap.setPixel(x, height-1, palette[field2[x][height-1]]);
                continue;
            }

            if ( (field[x][height-1]+1) % MAX_COLOR == field[x2][height-1]) {
                field2[x][height-1] = field[x2][height-1];
                bMap.setPixel(x, height-1, palette[field2[x][height-1]]);
                continue;
            }

            y2 = 0;
            if ( (field[x][height-1]+1) % MAX_COLOR == field[x2][y2]) {
                field2[x][height-1] = field[x2][y2];
                bMap.setPixel(x, height-1, palette[field2[x][height-1]]);
            }
        }

    }

    @Override
    public void draw(Canvas canvas) {
        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                field[i][j] = field2[i][j];
            }
        }
        canvas.drawBitmap(bMap, null, dst, null);
    }
}