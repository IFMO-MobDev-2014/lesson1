package ru.ifmo.md.lesson1;

    import android.content.Context;
    import android.graphics.Canvas;
    import android.graphics.Paint;
    import android.view.SurfaceHolder;
    import android.view.SurfaceView;
    import java.util.Random;


    /**
     * Created by thevery on 11/09/14.
     */
    class WhirlView extends SurfaceView {

        private final int width = 240;
        private final int height = 320;
        int [][] field = new int[width][height];
        int [][] field2 = new int[width][height];
        int [][] field3 = new int[width][height];
        int [] colors;
        float scale_x = 1;
        float scale_y = 1;
        final int MAX_COLOR = 10;
        int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};
        SurfaceHolder holder;

    private Paint paint = new Paint();

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        scale_x = (float) w / width;
        scale_y = (float) h / height;
        colors = new int[width * height];
        initField();
    }

    void initField() {
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                field[x][y] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        int x2, y2, c;
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {

                field2[x][y] = field[x][y];

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        x2 = x + dx;
                        y2 = y + dy;
                        c = field[x][y] + 1;
                        if (c >= MAX_COLOR)
                            c -= MAX_COLOR;
                        if (c == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y += height - 1) {

                field2[x][y] = field[x][y];

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        x2 = x + dx;
                        y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (y2 < 0) y2 += height;
                        if (x2 >= width) x2 -= width;
                        if (y2 >= height) y2 -= height;
                        c = field[x][y] + 1;
                        if (c >= MAX_COLOR)
                            c -= MAX_COLOR;
                        if (c == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }

        for (int x = 0; x < width; x += width - 1) {
            for (int y = 1; y < height - 1; y++) {

                field2[x][y] = field[x][y];

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        x2 = x + dx;
                        y2 = y + dy;
                        if (x2 < 0) x2 += width;
                        if (x2 >= width) x2 -= width;
                        c = field[x][y] + 1;
                        if (c >= MAX_COLOR)
                            c -= MAX_COLOR;
                        if (c == field[x2][y2]) {
                            field2[x][y] = field[x2][y2];
                        }
                    }
                }
            }
        }

        field3 = field;
        field = field2;
        field2 = field3;
    }

    public void render(Canvas canvas) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                colors[x + y * width] = palette[field[x][y]];
            }
        }
        canvas.scale(scale_x, scale_y);
        canvas.drawBitmap(colors, 0, width, 0, 0, width, height, false, paint);
    }

    @Override
    public void onDraw(Canvas canvas) {
        render(canvas);
    }

}
