package ru.ifmo.md.lesson1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by dimatomp on 12.09.14.
 */
public class FieldDrawer implements Runnable {
    public static final int MAX_COLOR = 10;
    public static final int SCALE = 4;

    static final int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    //static final int[] palette = {0b1111100000000000, 0b1000000000000000, 0b1000010000000000, 0b0000010000000000,
    //                              0b0000011111100000, 0b0000010000010000, 0b0000000000011111, 0b0000000000010000,
    //                              0b1000000000010000, 0b1111111111111111};
    static final int MAX_FRAMES = 10;

    ArrayBlockingQueue<Bitmap> queue = new ArrayBlockingQueue<>(MAX_FRAMES);
    static final String TAG = "FieldDrawer";
    final FieldUpdater updater;
    final Paint paint = new Paint();
    final Bitmap bitmap;
    final Canvas canvas;
    final Rect rect;
    float[][] points;
    int[] ind;

    public FieldDrawer(FieldUpdater updater, int width, int height) {
        this.updater = updater;
        this.bitmap = Bitmap.createBitmap(width / SCALE, height / SCALE, Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bitmap);
        this.rect = new Rect();
        this.points = new float[MAX_COLOR][(width / SCALE) * (height / SCALE) * 2];
        this.ind = new int[MAX_COLOR];
    }

    public Bitmap nextBitmap() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public void run() {
        while (WhirlView.running) {
            int[][] field = updater.nextState();
            for (int i = 0; i < MAX_COLOR; i++)
                ind[i] = 0;
            for (int i = 0; i < field.length; i++)
                for (int j = 0; j < field[i].length; j++) {
                    points[field[i][j]][ind[field[i][j]]] = i;
                    points[field[i][j]][ind[field[i][j]] + 1] = j;
                    ind[field[i][j]] += 2;
                }
            for (int i = 0; i < MAX_COLOR; i++) {
                paint.setColor(palette[i]);
                canvas.drawPoints(points[i], 0, ind[i], paint);
            }
            try {
                queue.put(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * SCALE, bitmap.getHeight() * SCALE, false));
            } catch (InterruptedException ignore) {
                Log.e(TAG, "Could not put the bitmap into the queue");
            }
            Log.v(TAG, "Queue size: " + queue.size());
        }
    }
}
