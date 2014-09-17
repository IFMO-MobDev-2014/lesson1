package whirl;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;


public class MyActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Display display = getWindowManager().getDefaultDisplay();

        super.onCreate(savedInstanceState);
        WhirlView view = new WhirlView(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(view);

    }
}
