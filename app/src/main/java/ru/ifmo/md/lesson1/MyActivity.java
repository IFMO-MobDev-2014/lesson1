package ru.ifmo.md.lesson1;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class MyActivity extends Activity {

    private WhirlView whirlView;
    TextView fps;

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
