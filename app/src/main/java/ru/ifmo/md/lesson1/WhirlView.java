package pls.me.kill.whirl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by Nikita on 13.09.2014.
 */

class WhirlView extends SurfaceView implements Runnable {
    int[][][] field;
    int width = 320, height = 240, f;
    float xscale, yscale;
    final int MAX_COLOR = 10;
    int[] palette = { 0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF };
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
        xscale = w * 1f / width;
        yscale = h * 1f / height;
        initField();
    }

    void initField() {
        field = new int[2][width][height];
        Random rnd = new Random();
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                field[f][x][y] = rnd.nextInt(MAX_COLOR);
    }

    void updateField() {
        int s = 1 - f;
        for (int x = 0; x < width; x++) {
            second: for (int y = 0; y < height; y++) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy =- 1; dy <= 1; dy++) {
                        int x2 = (x + dx + width) % width, y2 = (y + dy + height) % height;
                        if ((field[f][x][y] + 1) % MAX_COLOR == field[f][x2][y2]) {
                            field[s][x][y] = field[f][x2][y2];
                            continue second;
                        }
                    }
                }
                field[s][x][y] = field[f][x][y];
            }
        }
        f = s;
    }

    Paint paint = new Paint();

    @Override
    public void onDraw(Canvas canvas) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                paint.setColor(palette[field[f][x][y]]);
                canvas.drawRect(x * xscale, y * yscale, (x + 1) * xscale, (y + 1) * yscale, paint);
            }
        }
    }
}