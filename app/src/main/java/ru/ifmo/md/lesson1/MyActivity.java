package ru.ifmo.md.lesson1;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;


public class MyActivity extends Activity {

    private WhirlView whirlView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView editBox = new TextView(this);
        editBox.setTextColor(Color.WHITE);
        editBox.setGravity(Gravity.CENTER);
        editBox.setTextSize(50);
        whirlView = new WhirlView(this, editBox);
        setContentView(whirlView);
        addContentView(editBox, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onPause() {
        super.onPause();
        whirlView.stopDrawing();
    }
}
