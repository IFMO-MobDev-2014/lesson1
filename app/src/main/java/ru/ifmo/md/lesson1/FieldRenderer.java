package ru.ifmo.md.lesson1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by dimatomp on 13.09.14.
 */
public class FieldRenderer {
    public static final int MAX_COLOR = 10;
    static final int[] palette = {0xFFFF0000, 0xFF800000, 0xFF808000, 0xFF008000, 0xFF00FF00, 0xFF008080, 0xFF0000FF, 0xFF000080, 0xFF800080, 0xFFFFFFFF};

    final Bitmap bitmap;
    final Canvas canvas;
    final Paint paint = new Paint();
    final int[] ind;
    final float[][] points;

    public FieldRenderer(int width, int height) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        ind = new int[MAX_COLOR];
        points = new float[MAX_COLOR][width * height * 2];
    }

    public Bitmap draw(int[][] field) {
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
        return Bitmap.createBitmap(bitmap);
    }
}
