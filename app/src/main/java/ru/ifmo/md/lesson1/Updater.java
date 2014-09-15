package ru.ifmo.md.lesson1;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Updates whirl field.
 *
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
class Updater {
    static final int WIDTH = 240;
    static final int HEIGHT = 320;
    private static final int SIZE = WIDTH * HEIGHT;
    private static final int[] PALETTE = {
            0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080,
            0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    private static final int MAX_COLOR = PALETTE.length;
    private static final int CPUS = Runtime.getRuntime().availableProcessors();

    private final ExecutorService pool =
            Executors.newFixedThreadPool(CPUS);
    private final CompletionService<Void> service = new ExecutorCompletionService<Void>(pool);
    private final Worker[] workers = new Worker[CPUS];

    private int[] field = new int[SIZE];
    private int[] field2 = new int[SIZE];
    private final int[] colors = new int[SIZE];

    private float scaleX;
    private float scaleY;

    class Worker implements Callable<Void> {
        int a, b;

        Worker(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public Void call() throws Exception {
            update(a, b);

            return null;
        }
    }

    Updater() {
        final int step = SIZE / CPUS;
        for (int i = 0; i < CPUS - 1; i++) {
            workers[i] = new Worker(step * i, step * (i + 1));
        }
        workers[CPUS - 1] = new Worker(step * (CPUS - 1), SIZE);
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public int[] getColors() {
        return colors;
    }

    void initField() {
        Random rand = new Random();
        for (int i = 0; i < SIZE; i++) {
            field[i] = rand.nextInt(MAX_COLOR);
        }
    }

    void update(int start, int end) {
        for (int i = start; i < end; i++) {
            field2[i] = field[i];
            int next = (field[i] + 1) % MAX_COLOR;
            all:
            for (int dy = -WIDTH; dy <= WIDTH; dy += WIDTH) {
                for (int dx = -1; dx <= 1; dx++) {
                    int j = (i + dy + dx + SIZE) % SIZE;
                    if (field[j] == next) {
                        field2[i] = next;
                        colors[i] = PALETTE[next];
                        break all;
                    }
                }
            }
        }
    }


    void updateAll() {
        for (int i = 0; i < CPUS; i++) {
            service.submit(workers[i]);
        }

        for (int i = 0; i < CPUS; i++) {
            try {
                service.take();
            } catch (InterruptedException ignored) {

            }
        }

        int[] buf = field;
        field = field2;
        field2 = buf;
    }
}
