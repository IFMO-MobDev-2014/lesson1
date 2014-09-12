package ru.ifmo.md.lesson1;

import android.util.Log;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by dimatomp on 12.09.14.
 */
public class FieldUpdater implements Runnable {
    static final String TAG = "FieldUpdater";
    static final int MAX_FRAMES = 10;
    final ArrayBlockingQueue<int[][]> queue = new ArrayBlockingQueue<>(MAX_FRAMES);
    int width, height, field[][];

    public FieldUpdater(int width, int height) {
        this.width = width;
        this.height = height;
        initField();
    }

    public int[][] nextState() {
        try {
            return queue.take();
        } catch (InterruptedException ignore) {
            return null;
        }
    }

    void initField() {
        field = new int[width][height];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(FieldDrawer.MAX_COLOR);
            }
        }
        try {
            queue.put(field);
        } catch (InterruptedException ignore) {}
    }

    @Override
    public void run() {
        while (WhirlView.running) {
            int[][] field2 = new int[width][height];
            for (int x=0; x<width; x++) {
                System.arraycopy(field[x], 0, field2[x], 0, field[x].length);
                for (int y=0; y<height; y++) {
                    for (int dx = -1; dx <= 1; dx++)
                        for (int dy = -1; dy <= 1; dy++) {
                            int x2 = x + dx;
                            int y2 = y + dy;
                            if (x2 < 0) x2 += width;
                            if (y2 < 0) y2 += height;
                            if (x2 >= width) x2 -= width;
                            if (y2 >= height) y2 -= height;
                            if ((field[x][y] + 1) % FieldDrawer.MAX_COLOR == field[x2][y2]) {
                                field2[x][y] = field[x2][y2];
                            }
                        }
                }
            }
            try {
                queue.put(field2);
            } catch (InterruptedException ignore) {}
            Log.v(TAG, "Queue size: " + queue.size());
            field = field2;
        }
    }
}
