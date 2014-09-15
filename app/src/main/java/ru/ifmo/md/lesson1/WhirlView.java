package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    Canvas canvas = null;
    final static int fieldX = 240;
    final static int fieldY = 320;
    final static int MAX_COLOR = 10;
    final static int maxN = 350;

    int q = 5;
    int p = 3;
    long[] pPow = new long[maxN];
    long[] qPow = new long[maxN];
    Map<Long, Bitmap> links = new HashMap<Long, Bitmap>();
    long[] hashs = new long[100];
    Bitmap[] forLinks = null;
    boolean flag = false;

    int[][] field = null;
    int[][] field2 = null;
    float scaleX;
    float scaleY;
    Bitmap bitmap = null;
    Paint paint = null;

    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

    public WhirlView(Context context) {
        super(context);
        if (forLinks == null) {
            pPow[0] = qPow[0] = 1;
            for (int i = 1; i < maxN; i++) {
               pPow[i] = pPow[i - 1] * p;
               qPow[i] = qPow[i - 1] * q;
            }
        }
        holder = getHolder();
    }

    public void resume() {
//        Log.i("id", "resume");
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
//        Log.i("id", "pause");
        running = false;
        try {
            thread.join();
        } catch (InterruptedException ignore) {}
    }

    long getHash() {
        long hash = 0;
        for (int i = 0; i < fieldX; i++) {
            for (int j = 0; j < fieldY; j++) {
               hash += field[i][j] * pPow[i] * qPow[j];
            }
        }
        return hash;
    }

    public void run() {
        int id = 0;
        int it = 0;

        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();

                //initial state
                long hash;
                //
                while (!flag) {
                    hash = getHash();
                    setUpPixels();
                    if (links.containsKey(hash)) {
                        flag = true;
                        for (int i = 1; i <= hashs[0]; i++) {
                            if (hashs[i] == hash) {
                                id = i;
                                break;
                            }
                        }

                        forLinks = new Bitmap[(int) (hashs[0] - id + 1)];
                        for (int i = id; i <= hashs[0]; i++) {
                            forLinks[i - id] = links.get(hashs[i]);
                        }
                        break;
                    } else {
                        hashs[0]++;
                        hashs[(int) hashs[0]] = hash;
                        links.put(new Long(hash), bitmap);

                        canvas = holder.lockCanvas();
                        onDraw(canvas);
                        holder.unlockCanvasAndPost(canvas);
                    }
                    Log.i("TIME", "FPS: " + 1e9 / (System.nanoTime() - startTime));
                    updateField();
                }


//                Log.i("LOL", "-----------");
                bitmap = forLinks[it];
                it = (it + 1) % forLinks.length;
                canvas = holder.lockCanvas();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);

                long finishTime = System.nanoTime();
                Log.i("TIME", "FPS: " + 1e9 / (finishTime - startTime));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scaleX = w * 1.f / fieldX;
        scaleY = h * 1.f / fieldY;
        initField();
    }

    void initField() {
        field = new int[fieldX][fieldY];
        Random rand = new Random();
        for (int x=0; x<fieldX; x++) {
            for (int y=0; y<fieldY; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
        paint = new Paint();
    }

    void updateField() {
        field2 = new int[fieldX][fieldY];
        for (int x=0; x<fieldX; x++) {
            for (int y=0; y<fieldY; y++) {

                field2[x][y] = field[x][y];

                for (int dx=-1; dx<=1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2<0) x2 += fieldX;
                        if (y2<0) y2 += fieldY;
                        if (x2>=fieldX) x2 -= fieldX;
                        if (y2>=fieldY) y2 -= fieldY;
                        if ( (field[x][y]+1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }
        field = field2;
    }

    public void setUpPixels() {
        bitmap = Bitmap.createBitmap(fieldX, fieldY, Bitmap.Config.ARGB_4444);
        for (int x = 0; x < fieldX; x++) {
            for (int y = 0; y < fieldY; y++) {
                bitmap.setPixel(x, y, palette[field[x][y]]);
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(scaleX, scaleY);
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }
}
