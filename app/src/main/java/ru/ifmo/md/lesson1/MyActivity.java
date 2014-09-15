package ru.ifmo.md.lesson1;

import android.app.Activity;
import android.os.Bundle;


public class MyActivity extends Activity {

    private WhirlView whirlView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        whirlView = new WhirlView(this);
        setContentView(whirlView);
    }

    @Override
    public void onResume() {
        super.onResume();
        whirlView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        whirlView.pause();
    }
}
