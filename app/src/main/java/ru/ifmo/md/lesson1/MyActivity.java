package ru.ifmo.md.lesson1;

import android.app.Activity;
import android.os.Bundle;


public class MyActivity extends Activity {

    private MainThread mainThread;
    //private UpdateThread updateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WhirlView whirlView = new WhirlView(this);
        mainThread = new MainThread(whirlView);
        //updateThread = new UpdateThread(whirlView);
        setContentView(whirlView);
    }

    @Override
    public void onResume() {
        super.onResume();
        //updateThread.resume();
        mainThread.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //updateThread.pause();
        mainThread.pause();
    }
}
