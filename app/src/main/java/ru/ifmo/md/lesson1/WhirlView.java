package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    final int MAX_COLOR = 10;
    final int P = (int)1e9 + 9;
    int [][] field = null;
    int [][] field2 = null;
    int W, H;
    float scaleX;
    float scaleY;
    boolean flag;

    HashMap< Long, PictureData > q = new HashMap<Long, PictureData >();
    Paint [] paint = new Paint[MAX_COLOR];
    int width = 0;
    int height = 0;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

    public class PictureData {
        int [][] field;
        Bitmap bitmap;
        PictureData(int [][] _field, Bitmap _bitmap) {
            field = _field;
            bitmap = _bitmap;
        }
    }


    public Canvas clone() throws CloneNotSupportedException {
        Canvas obj = (Canvas)super.clone();
        return obj;
    }

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


    public void test() {
        if (flag) return;
        flag = true;
        Log.i("start: ", "tmp");
        Rect dst = new Rect(0, 0, W / 2, H / 2);
        Bitmap r = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = holder.lockCanvas();
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                r.setPixel(i, j, palette[2]);
        canvas.drawBitmap(r, null, dst, null);

        holder.unlockCanvasAndPost(canvas);

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        while (running) {
            if (holder.getSurface().isValid()) {
                Log.i("w h: ", " " + width + " " + height);
                //test();
                Canvas canvas = holder.lockCanvas();
                long startTime = System.nanoTime();

                Long hash = calcHash(field);
                Bitmap bitmap;
                if (q.containsKey(hash)) {
                    field = q.get(hash).field;
                    bitmap = q.get(hash).bitmap.copy(Bitmap.Config.ARGB_4444, true);
//                    try {
//                        Thread.sleep(50);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                } else {
                    Log.i("second case ", " ");
                    updateField();
                    int [][] fieldCopy = new int[width][height];
                    for (int i = 0; i < width; i++)
                        for (int j = 0; j < height; j++)
                            fieldCopy[i][j] = field[i][j];
                    bitmap = onDraw2();
                    q.put(hash, new PictureData(fieldCopy, bitmap));
                }
                Rect dst = new Rect(0, 0, W, H);
                canvas.drawBitmap(bitmap, null, dst, null);

                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("TIME", "Circle: " + (finishTime - startTime) / 1000000);
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        W = w;
        H = h;
        width = 240;
        height = 320;
        if (w > h) {
            int x = width;
            width = height;
            height = x;
        }
        scaleX = w * 1.0f / width;
        scaleY = h * 1.0f / height;
        initField();
    }

    void initField() {
        field = new int[width][height];
        field2 = new int[width][height];
        for (int i = 0; i < MAX_COLOR; i++) {
            paint[i] = new Paint();
            paint[i].setColor(palette[i]);
        }

        Random rand = new Random();
        rand.setSeed(System.nanoTime());
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }
    long calcHash(int [][] field)  {
        long hash = 0;
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                hash = hash * P + field[i][j];
        return hash;
    }
    void updateField() {
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field2[x][y] = field[x][y];
                for (int dx=-1; dx<=1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2<0) x2 += width;
                        if (y2<0) y2 += height;
                        if (x2>=width) x2 -= width;
                        if (y2>=height) y2 -= height;
                        if ((field[x][y]+1) % MAX_COLOR == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }
        int [][] field3 = field;
        field = field2;
        field2 = field3;
    }

    public Bitmap onDraw2() {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                bitmap.setPixel(i, j, palette[field[i][j]]);
        return bitmap;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                canvas.drawRect(x*scaleX, y*scaleY, (x+1)*scaleX, (y+1)*scaleY, paint[field[x][y]]);
            }
        }
    }
}

