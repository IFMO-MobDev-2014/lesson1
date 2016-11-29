package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
* Created by thevery on 11/09/14.
*/
class WhirlView extends SurfaceView implements Runnable {
    byte [][][] field = null;
	int [] colors = null;
    final int width = 240;
    final int height = 320;
    final int MAX_COLOR = 10;
    int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
    int buffer = 0;
    final int BUFFERS = 2;
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
        initField();
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
                updateField();
                Canvas canvas = holder.lockCanvas();
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

    void initField() {
        field = new byte[BUFFERS][width][height];
		colors = new int[width * height];
		buffer = 0;
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[buffer][x][y] = (byte)rand.nextInt(MAX_COLOR);
                colors[x + y * width] = palette[field[buffer][x][y]];
            }
        }
    }

	void updateField() {
		byte [][] curField = field[buffer];
		buffer = (buffer + 1) % BUFFERS; // select next buffer
		byte [][] nextField = field[buffer];
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				nextField[x][y] = curField[x][y];
				for (int dx = -1; dx <= 1; dx++) {
					for (int dy = -1; dy <= 1; dy++) {
						int x2 = x + dx;
						int y2 = y + dy;
						// simulate torus
						if (x2 < 0)
							x2 += width;
						if (y2 < 0)
							y2 += height;
						if (x2 >= width)
							x2 -= width;
						if (y2 >= height)
							y2 -= height;
						if ((curField[x][y] + 1) % MAX_COLOR == curField[x2][y2]) {
							nextField[x][y] = curField[x2][y2];
							colors[x + y * width] = palette[nextField[x][y]];
						}
					}
				}
				
			}
		}
	}

    @Override
    public void onDraw(Canvas canvas) {
    	canvas.scale((float) canvas.getWidth() / width, (float) canvas.getHeight() / height);
    	canvas.drawBitmap(colors, 0, width, 0, 0, width, height, false, null);
    }
}
