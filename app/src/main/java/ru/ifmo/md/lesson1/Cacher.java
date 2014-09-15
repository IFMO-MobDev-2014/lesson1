package ru.ifmo.md.lesson1;

import android.util.SparseIntArray;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by zakharvoit on 9/15/14.
 */
class Cacher {
    private static final int CACHE_SIZE = 100; // About 30-32 Mb memory
    private final SparseIntArray cache = new SparseIntArray();
    private final int[][] states = new int[CACHE_SIZE][];
    private int position = 0;

    public Cacher() {
    }

    /**
     * Adds field to cache.
     *
     * @return false if cache size if exceeded, true otherwise.
     */
    boolean add(int[] field, int[] colors) {
        if (position == CACHE_SIZE) {
            return false;
        }
        cache.put(Arrays.hashCode(field), position + 1); // put plus 1 because get return 0 when
        // there are no such key in container
        states[position++] = colors.clone();

        return true;
    }

    Iterator<int[]> searchForCycle(int[] field) {
        final int index = cache.get(Arrays.hashCode(field)) - 1; // get minus 1 because
        // get return 0 when there are
        // no such key in container
        if (index == -1) {
            return null;
        }

        return new Iterator<int[]>() {
            private int iter = 0;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public int[] next() {
                int[] colors = states[index + iter];
                iter++;
                iter %= position - index;
                return colors;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
