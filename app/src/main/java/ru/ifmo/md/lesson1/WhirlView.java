package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

class WhirlView extends SurfaceView implements Runnable {
    int [][] field = null;
    int width = 240;
    int height = 320;
    int dstWidth = 1080;
    int dstHeight = 1920;
    int scale = 4;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    int MC = 10;
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    Bitmap bitmap = null;
    Bitmap bitmap2 = null;
    int[] bc = null;
    int f2 = 0;
    int x = 0;
    int y = 0;
    int fn = 0;

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
        width = w/scale;
        height = h/scale;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bc = new int[width*height];
        initField();
    }

    void initField() {
        field = new int[width][height];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int[][] field2 = new int[width][height];

        for (int x = 1; x < width - 1; x++) {

            int[] col0 = field[x-1];
            int[] col1 = field[x];
            int[] col2 = field[x+1];
            int fn = 0;

            for (int y = 1; y < height - 1; y++) {

                int f2 = (field[x][y]+1) % MC;

                if (col0[y-1]==f2) {
                    fn = f2;
                } else if (col0[y]==f2) {
                    fn = f2;
                } else if (col0[y+1]==f2) {
                    fn = f2;
                } else if (col1[y-1]==f2) {
                    fn = f2;
                } else if (col1[y+1]==f2) {
                    fn = f2;
                } else if (col2[y-1]==f2) {
                    fn = f2;
                } else if (col2[y]==f2) {
                    fn = f2;
                } else if (col2[y+1]==f2) {
                    fn = f2;
                } else {
                    fn = field[x][y];
                }

                field2[x][y] = fn;
            }
        }

        int[] col0 = field[width - 1];
        int[] col1 = field[0];
        int[] col2 = field[1];

        for (int y = 1; y < height-1; y++) {
            int x = 0;
            int f2 = (field[x][y]+1) % MC;

            if (col0[y-1]==f2) {
                fn = f2;
            } else if (col0[y]==f2) {
                fn = f2;
            } else if (col0[y+1]==f2) {
                fn = f2;
            } else if (col1[y-1]==f2) {
                fn = f2;
            } else if (col1[y+1]==f2) {
                fn = f2;
            } else if (col2[y-1]==f2) {
                fn = f2;
            } else if (col2[y]==f2) {
                fn = f2;
            } else if (col2[y+1]==f2) {
                fn = f2;
            } else {
                fn = field[x][y];
            }

            field2[x][y] = fn;
        }

        col0 = field[width - 2];
        col1 = field[width - 1];
        col2 = field[0];
        fn = 0;

        for (int y = 1; y < height-1; y++) {
            int x = width - 1;
            int f2 = (field[x][y]+1) % MC;

            if (col0[y-1]==f2) {
                fn = f2;
            } else if (col0[y]==f2) {
                fn = f2;
            } else if (col0[y+1]==f2) {
                fn = f2;
            } else if (col1[y-1]==f2) {
                fn = f2;
            } else if (col1[y+1]==f2) {
                fn = f2;
            } else if (col2[y-1]==f2) {
                fn = f2;
            } else if (col2[y]==f2) {
                fn = f2;
            } else if (col2[y+1]==f2) {
                fn = f2;
            } else {
                fn = field[x][y];
            }

            field2[x][y] = fn;
        }

        fn = 0;
        y = 0;
        for (int x = 1; x < width - 1; x++) {

            int f2 = (field[x][y] + 1) % MC;

            if (field[x-1][height-1]==f2) {
                fn = f2;
            } else if (field[x-1][y]==f2) {
                fn = f2;
            } else if (field[x-1][y+1]==f2) {
                fn = f2;
            } else if (field[x][height-1]==f2) {
                fn = f2;
            } else if (field[x][y+1]==f2) {
                fn = f2;
            } else if (field[x+1][height-1]==f2) {
                fn = f2;
            } else if (field[x+1][y]==f2) {
                fn = f2;
            } else if (field[x+1][y+1]==f2) {
                fn = f2;
            } else {
                fn = field[x][y];
            }

            field2[x][y] = fn;
        }

        y = height - 1;
        for (int x = 1; x < width - 1; x++) {

            int f2 = (field[x][y]+1) % MC;

            if (field[x-1][y-1]==f2) {
                fn = f2;
            } else if (field[x-1][y]==f2) {
                fn = f2;
            } else if (field[x-1][0]==f2) {
                fn = f2;
            } else if (field[x][y-1]==f2) {
                fn = f2;
            } else if (field[x][0]==f2) {
                fn = f2;
            } else if (field[x+1][y-1]==f2) {
                fn = f2;
            } else if (field[x+1][y]==f2) {
                fn = f2;
            } else if (field[x+1][0]==f2) {
                fn = f2;
            } else {
                fn = field[x][y];
            }

            field2[x][y] = fn;
        }

        f2 = (field[0][0]+1) % MC;
        if (field[width-1][height-1]==f2) {
            fn = f2;
        } else if (field[width-1][0]==f2) {
            fn = f2;
        } else if (field[width-1][1]==f2) {
            fn = f2;
        } else if (field[0][height-1]==f2) {
            fn = f2;
        } else if (field[0][1]==f2) {
            fn = f2;
        } else if (field[1][height-1]==f2) {
            fn = f2;
        } else if (field[1][0]==f2) {
            fn = f2;
        } else if (field[1][1]==f2) {
            fn = f2;
        } else {
            fn = field[0][0];
        }
        field2[0][0] = fn;

        f2 = (field[0][height-1]+1) % MC;
        if (field[width-1][height-2]==f2) {
            fn = f2;
        } else if (field[width-1][height-1]==f2) {
            fn = f2;
        } else if (field[width-1][0]==f2) {
            fn = f2;
        } else if (field[0][height-2]==f2) {
            fn = f2;
        } else if (field[0][0]==f2) {
            fn = f2;
        } else if (field[1][height-2]==f2) {
            fn = f2;
        } else if (field[1][height-1]==f2) {
            fn = f2;
        } else if (field[1][0]==f2) {
            fn = f2;
        } else {
            fn = field[0][height-1];
        }
        field2[0][height-1] = fn;

        f2 = (field[width-1][0]+1) % MC;
        if (field[width-2][height-1]==f2) {
            fn = f2;
        } else if (field[width-2][0]==f2) {
            fn = f2;
        } else if (field[width-2][1]==f2) {
            fn = f2;
        } else if (field[width-1][height-1]==f2) {
            fn = f2;
        } else if (field[width-1][1]==f2) {
            fn = f2;
        } else if (field[0][height-1]==f2) {
            fn = f2;
        } else if (field[0][0]==f2) {
            fn = f2;
        } else if (field[0][1]==f2) {
            fn = f2;
        } else {
            fn = field[width-1][0];
        }
        field2[width-1][0] = fn;

        f2 = (field[width-1][height-1]+1) % MC;
        if (field[width-2][height-2]==f2) {
            fn = f2;
        } else if (field[width-2][height-1]==f2) {
            fn = f2;
        } else if (field[width-2][0]==f2) {
            fn = f2;
        } else if (field[width-1][height-2]==f2) {
            fn = f2;
        } else if (field[width-1][0]==f2) {
            fn = f2;
        } else if (field[0][height-2]==f2) {
            fn = f2;
        } else if (field[0][height-1]==f2) {
            fn = f2;
        } else if (field[0][0]==f2) {
            fn = f2;
        } else {
            fn = field[width-1][height-1];
        }
        field2[width-1][height-1] = fn;



        field = field2;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                bc[x+y*width] = palette[field[x][y]];
            }
        }
        bitmap.setPixels(bc, 0, width, 0, 0, width, height);
        bitmap2 = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
        canvas.drawBitmap(bitmap2, 0, 0, null);
    }
}
