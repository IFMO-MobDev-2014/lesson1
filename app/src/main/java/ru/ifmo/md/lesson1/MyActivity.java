package ru.ifmo.md.lesson1;

import android.app.Activity;
import android.os.Bundle;


public class MyActivity extends Activity {

    private MainThreadWrapper mainThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WhirlView whirlView = new WhirlView(this);
        mainThread = new MainThreadWrapper(whirlView);
        setContentView(whirlView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mainThread.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mainThread.pause();
    }
}
