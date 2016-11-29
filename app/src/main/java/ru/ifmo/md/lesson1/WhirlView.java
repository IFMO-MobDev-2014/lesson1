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
    int[] field = null;
    int[] field2 = null;
    int[] field_old = null;
    int[] colors;
    final int width = 240;
    final int height = 320;
    int newColor;
    int x;
    int y;
    int widthMulHeight = width * height;
    float scalex;
    float scaley;
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
        } catch (InterruptedException ignore) {
        }
    }

    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.currentTimeMillis();
                Canvas canvas = holder.lockCanvas();
                updateField();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.currentTimeMillis();
                Log.i("TIME", "Circle: " + (finishTime - startTime));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        //width = 240;//w / scale;
        //height = 320;//h / scale;
        scalex = (float) w / width;
        scaley = (float) h / height;
        initField();
    }

    void initField() {
        field = new int[widthMulHeight];
        field2 = new int[widthMulHeight];
        colors = new int[widthMulHeight];
        Random rand = new Random();
        for (int i = 0; i < widthMulHeight; i++) {
            field[i] = rand.nextInt(MAX_COLOR);
        }
    }

    void updateField() {
        for (x = 1; x < width - 1; x++) {
            for (y = 1; y < height - 1; y++) {
                field2[x + width * y] = field[x + width * y];
                newColor = field[x + width * y] + 1;
                if (newColor >= MAX_COLOR) {
                    newColor -= MAX_COLOR;
                }
                if (newColor == field[(x - 1) + width * (y - 1)]) {
                    field2[x + width * y] = newColor;
                    continue;
                }
                if (newColor == field[(x - 1) + width * y]) {
                    field2[x + width * y] = newColor;
                    continue;
                }
                if (newColor == field[(x - 1) + width * (y + 1)]) {
                    field2[x + width * y] = newColor;
                    continue;
                }
                if (newColor == field[x + width * (y - 1)]) {
                    field2[x + width * y] = newColor;
                    continue;
                }
                if (newColor == field[x + width * (y + 1)]) {
                    field2[x + width * y] = newColor;
                    continue;
                }
                if (newColor == field[(x + 1) + width * (y - 1)]) {
                    field2[x + width * y] = newColor;
                    continue;
                }
                if (newColor == field[(x + 1) + width * y]) {
                    field2[x + width * y] = newColor;
                    continue;
                }
                if (newColor == field[(x + 1) + width * (y + 1)]) {
                    field2[x + width * y] = newColor;
                    continue;
                }
            }
        }
        x = 0;
        for (y = 1; y < height - 1; y++) {
            field2[width * y] = field[width * y];
            newColor = field[width * y] + 1;
            if (newColor >= MAX_COLOR) {
                newColor -= MAX_COLOR;
            }
            if (newColor == field[width - 1 + width * (y - 1)]) {
                field2[width * y] = newColor;
                continue;
            }
            if (newColor == field[(width - 1) + width * y]) {
                field2[width * y] = newColor;
                continue;
            }
            if (newColor == field[(width - 1) + width * (y + 1)]) {
                field2[width * y] = newColor;
                continue;
            }
            if (newColor == field[width * (y - 1)]) {
                field2[width * y] = newColor;
                continue;
            }
            if (newColor == field[width * (y + 1)]) {
                field2[width * y] = newColor;
                continue;
            }
            if (newColor == field[(1) + width * (y - 1)]) {
                field2[width * y] = newColor;
                continue;
            }
            if (newColor == field[(1) + width * y]) {
                field2[width * y] = newColor;
                continue;
            }
            if (newColor == field[(1) + width * (y + 1)]) {
                field2[width * y] = newColor;
                continue;
            }
        }
        x = width - 1;
        for (y = 1; y < height - 1; y++) {
            field2[x + width * y] = field[x + width * y];
            newColor = field[x + width * y] + 1;
            if (newColor >= MAX_COLOR) {
                newColor -= MAX_COLOR;
            }
            if (newColor == field[(x - 1) + width * (y - 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[(x - 1) + width * y]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[(x - 1) + width * (y + 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[x + width * (y - 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[x + width * (y + 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[(x + 1) + width * (y - 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[0 + width * y]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[0 + width * (y + 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
        }
        y = 0;
        for (x = 1; x < width - 1; x++) {
            field2[x + width * y] = field[x + width * y];
            newColor = field[x + width * y] + 1;
            if (newColor >= MAX_COLOR) {
                newColor -= MAX_COLOR;
            }
            if (newColor == field[(x - 1) + width * (height - 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[(x - 1) + width * y]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[(x - 1) + width * (y + 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[x + width * (height - 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[x + width * (y + 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[(x + 1) + width * (height - 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[(x + 1) + width * y]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[(x + 1) + width * (y + 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
        }
        y = height - 1;
        for (x = 1; x < width - 1; x++) {
            field2[x + width * y] = field[x + width * y];
            newColor = field[x + width * y] + 1;
            if (newColor >= MAX_COLOR) {
                newColor -= MAX_COLOR;
            }
            if (newColor == field[(x - 1) + width * (y - 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[(x - 1) + width * y]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[(x - 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[x + width * (y - 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[x]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[(x + 1) + width * (y - 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[(x + 1) + width * y]) {
                field2[x + width * y] = newColor;
                continue;
            }
            if (newColor == field[(x + 1)]) {
                field2[x + width * y] = newColor;
                continue;
            }
        }
        for (x = 0; x < 1; x++) {  //better it use "goto" to go to next step. but "goto" illegal
            y = 0;
            field2[x + width * y] = field[x + width * y];
            newColor = field[x + width * y] + 1;
            if (newColor >= MAX_COLOR) {
                newColor -= MAX_COLOR;
            }
            if (newColor == field[(width - 1) + width * (height - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(width - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(width - 1) + width]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[x + width * (height - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[x + width]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(x + 1) + width * (height - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(x + 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(x + 1) + width]) {
                field2[x + width * y] = newColor;
                break;
            }
        }
        for (x = width - 1; x < width; x++) {
            y = 0;
            field2[x + width * y] = field[x + width * y];
            newColor = field[x + width * y] + 1;
            if (newColor >= MAX_COLOR) {
                newColor -= MAX_COLOR;
            }
            if (newColor == field[(x - 1) + width * (height - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(x - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(x - 1) + width]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[x + width * (height - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[x + width]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(0) + width * (height - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(0)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(0) + width]) {
                field2[x + width * y] = newColor;
                break;
            }
        }
        for (x = 0 ; x < 1; x++) {
            y = height-1;
            field2[x + width * y] = field[x + width * y];
            newColor = field[x + width * y] + 1;
            if (newColor >= MAX_COLOR) {
                newColor -= MAX_COLOR;
            }
            if (newColor == field[(width - 1) + width * (y - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(width - 1) + width * y]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(width - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[x + width * (y - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[x]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(x + 1) + width * (y - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(x + 1) + width * y]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(x + 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
        }
        for (x = width - 1; x < width; x++) {
            y = height-1;
            field2[x + width * y] = field[x + width * y];
            newColor = field[x + width * y] + 1;
            if (newColor >= MAX_COLOR) {
                newColor -= MAX_COLOR;
            }
            if (newColor == field[(x - 1) + width * (y - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(x - 1) + width * y]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[(x-1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[x + width * (y - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[x]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[ width * (y - 1)]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[ width * y]) {
                field2[x + width * y] = newColor;
                break;
            }
            if (newColor == field[0]) {
                field2[x + width * y] = newColor;
                break;
            }
        }


        field_old = field;
        field = field2;
        field2 = field_old;
    }

    @Override
    public void onDraw(Canvas canvas) {


        for (int i = 0; i < widthMulHeight; i++) {
            colors[i] = palette[field[i]];
        }
        canvas.scale(scalex, scaley);
        canvas.drawBitmap(colors, 0, width, 0, 0, width, height, false, null);
    }
}
