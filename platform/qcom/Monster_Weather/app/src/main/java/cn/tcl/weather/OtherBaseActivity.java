package cn.tcl.weather;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import cn.tcl.weather.utils.CommonUtils;

/**
 * Created on 16-9-26.
 */
public class OtherBaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CommonUtils.setStateBarColor(this, R.color.white);
        changeVirtualKeyToWhite();
        super.onCreate(savedInstanceState);
    }

    private void changeVirtualKeyToWhite() {
        int vi = getWindow().getDecorView().getSystemUiVisibility();
        getWindow().getDecorView().setSystemUiVisibility(vi | 0x00000010);
        getWindow().setNavigationBarColor(0xFFFAFAFA);
    }


    protected void disableStatuBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        int flag=WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setFlags(flag, flag);
    }
}
