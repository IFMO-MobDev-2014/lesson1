package ru.ifmo.md.lesson1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.widget.TextView;

class DrawThread extends Thread{
    public static final long FPS_UPDATE_INTERVAL = 300; // in ms
    private volatile boolean runFlag = false;

    private WhirlView view;
    private SurfaceHolder holder = null;
    private TextView textView = null;
    private int[] palette = null;
    private int[] field2 = null;
    private int[] colors = null;

    private long prevTime = 0;

    //for FPS count & update every FPS_UPDATE_INTERVAL ms
    private long prevFpsUpdate = 0;
    private long fpsUpdateCnt = 0;
    private double sumFpsUpdates = 0;

    private Looper looper = Looper.getMainLooper();
    private Handler handler = new Handler(looper);

    public DrawThread(WhirlView view){
        this.view = view;
        this.holder = view.getHolder();
        textView = view.getTextView();
        palette = view.getPalette();
        field2 = new int[view.getW() * view.getH()];
        colors = new int[view.getW() * view.getH()];
    }

    public void setRunning(boolean run) {
        runFlag = run;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        while (runFlag) {

            synchronized (holder) {
                canvas = holder.lockCanvas(null);
                updateField();
                updateBitmap();
                view.onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
            }

            long curTime = System.currentTimeMillis();
            long delta = curTime - prevTime;
            prevTime = curTime;

            sumFpsUpdates += 1000.0 / delta;
            fpsUpdateCnt++;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long cur  = System.currentTimeMillis();
                    if (cur - prevFpsUpdate > FPS_UPDATE_INTERVAL) {
                        final Double aproxFps = sumFpsUpdates / fpsUpdateCnt;
                        textView.setText(String.format("%.2f fps", aproxFps));
                        prevFpsUpdate = cur;
                        fpsUpdateCnt = 0;
                        sumFpsUpdates = 0;
                    }
                }
            });
        }
    }

    void updateField() {
        int width = view.getW();
        int height = view.getH();
        int[] field = view.getField();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                int curIndex = y * width + x;
                field2[curIndex] = field[curIndex];
                for (int dx=-1; dx<=1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = x + dx;
                        int y2 = y + dy;
                        if (x2<0) x2 += width;
                        if (y2<0) y2 += height;
                        if (x2>=width) x2 -= width;
                        if (y2>=height) y2 -= height;
                        if ( (field[curIndex]+1) % palette.length == field[y2 * width + x2]) {
                            field2[curIndex] = field[y2 * width + x2];
                        }
                    }
                }
            }
        }
        view.setField(field2);
    }

    private void updateBitmap() {
        int width = view.getW();
        int height = view.getH();
        int[] field = view.getField();
        int[]  palette = view.getPalette();

        for (int i=0; i<width*height; i++) {
            colors[i] = palette[field[i]];
        }

        view.setBitmap(Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888));
    }
}
