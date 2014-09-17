package whirl;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;

import java.util.Random;

class WhirlView extends View {

    // size
    final static int WIDTH = 240;
    final static int HEIGHT = 320;
    //FPS variables
    private static final long FPS_CALC_INTERVAL = 1000L;
    private long lastFpsCalcUptime;
    private long frameCounter;
    private long fps;
    Paint p_fps = new Paint();
    //colors
    int[] colors = null;
    final static int COUNTOFCOLORS = 32;
    int[] bits = new int[WIDTH*HEIGHT];
    int[][] bitscolors = new int[WIDTH][HEIGHT];

    Random rand = new Random();

    public WhirlView (Context context){

        super(context);

        p_fps.setColor(Color.WHITE);
        p_fps.setTextSize(10.0f);

        colors = new int[COUNTOFCOLORS];

       for(int i = 0;i < COUNTOFCOLORS;i++)
           colors[i] = Color.argb(255 , rand.nextInt(256) ,  rand.nextInt(256) ,  rand.nextInt(256));

        int i = rand.nextInt(WIDTH);
        int j = rand.nextInt(HEIGHT);

        bitscolors[i][j] = rand.nextInt(COUNTOFCOLORS);
        bits[j * WIDTH + i] = colors[bitscolors[i][j]];


        /*for(int i = 0; i < WIDTH; i++)
            for(int j = 0; j < HEIGHT; j++){
                bitscolors[i][j] = rand.nextInt(COUNTOFCOLORS);
                bits[j * WIDTH + i] = colors[bitscolors[i][j]];

            }

            */
    }

    @Override
    public void onDraw(Canvas canvas){

        Fps();
        int D_Height = canvas.getHeight();
        int D_Width = canvas.getWidth();
        MakeAlive();
        Matrix matrix = new Matrix();
        matrix.setScale((float) D_Width / WIDTH, (float) D_Height / HEIGHT);
        canvas.setMatrix(matrix);
        canvas.drawBitmap(bits, 0, WIDTH, 0, 0, WIDTH, HEIGHT, false, null);
        canvas.drawText("FPS: " + fps, 190, 300, p_fps);

        invalidate();

    }

    int transformCell(int northWestCell, int northCell, int northEastCell,int westCell, int centerCell, int eastCell,int southWestCell, int southCell, int southEastCell){

        int sum = northWestCell + northCell + northEastCell +
                westCell + eastCell +
                southWestCell + southCell + southEastCell;
        if (centerCell == 0) {
            if (sum < 5) {
                return 0;
            } else if (sum < 100) {
                return 2;
            } else {
                return 3;
            }
        } else if (centerCell == COUNTOFCOLORS - 1) {
            return 0;
        } else {
            return Math.min(sum / 8 + 5, COUNTOFCOLORS - 1);
        }
    }

    private void MakeAlive(){

        for(int i = 0; i < WIDTH; i++ )
            for(int j = 0; j < HEIGHT; j++){

                int i_left, i_right, j_up, j_down;
                if(i == 0) {
                    i_left = 239;
                }
                else {
                    i_left = i - 1;
                }

                if(i == 239){
                    i_right = 0;
                } else{
                    i_right = i + 1;
                }

                if(j == 0){
                    j_up = 319;
                } else{
                    j_up = j - 1;
                }

                if(j == 319){
                    j_down = 0;
                } else{
                    j_down = j + 1;
                }


               bitscolors[i][j] = transformCell(bitscolors[i_left][j_up],
                       bitscolors[i][j_up],bitscolors[i_right][j_up],
                       bitscolors[i_left][j],
                       bitscolors[i][j],
                       bitscolors[i_right][j],
                       bitscolors[i_left][j_down],
                       bitscolors[i][j_down],
                       bitscolors[i_right][j_down]);
               bits[j * WIDTH + i] = colors[bitscolors[i][j]];


            }



    }

    private void Fps() {

        frameCounter++;
        long now = SystemClock.uptimeMillis();
        long delta = now - lastFpsCalcUptime;
        if (delta > FPS_CALC_INTERVAL) {
            fps = frameCounter * FPS_CALC_INTERVAL / delta;
            frameCounter = 0;
            lastFpsCalcUptime = now;
        }
    }

}
