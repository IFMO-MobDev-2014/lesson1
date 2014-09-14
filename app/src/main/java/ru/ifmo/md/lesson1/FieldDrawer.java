package ru.ifmo.md.lesson1;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by dimatomp on 12.09.14.
 */
public class FieldDrawer extends Thread {
    static final int MAX_FRAMES = 10;

    ArrayBlockingQueue<Bitmap> queue = new ArrayBlockingQueue<>(MAX_FRAMES);
    static final String TAG = "FieldDrawer";
    final FieldUpdater updater;
    final FieldRenderer renderer;
    final ConsequenceFinder finder;
    int animIndex;
    Bitmap animation[];

    public FieldDrawer(int width, int height) {
        setPriority(MAX_PRIORITY - 1);
        this.updater = new FieldUpdater(width, height);
        updater.setPriority(MAX_PRIORITY);
        this.finder = new ConsequenceFinder(this);
        finder.setPriority(MAX_PRIORITY);
        this.renderer = new FieldRenderer(width, height);
        start();
    }

    public Bitmap nextBitmap() {
        if (animation == null) {
            try {
                return queue.take();
            } catch (InterruptedException e) {
                return null;
            }
        } else {
            return animation[animIndex = (animIndex + 1) % animation.length];
        }
    }

    public void replaceWithAnimation(Bitmap[] anim, int from) {
        animation = anim;
        animIndex = from;
        updater.foundConsequence = true;
        try {
            updater.join();
        } catch (InterruptedException ignore) {
        }
    }

    @Override
    public void run() {
        while (WhirlView.running && animation == null) {
            int nextState[][] = updater.nextState();
            finder.addToHistory(nextState);
            try {
                queue.put(renderer.draw(nextState));
            } catch (InterruptedException ignore) {
                Log.e(TAG, "Could not put the bitmap into the queue");
            }
            Log.v(TAG, "Queue size: " + queue.size());
        }
    }
}
