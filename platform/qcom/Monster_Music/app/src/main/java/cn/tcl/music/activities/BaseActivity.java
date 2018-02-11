package cn.tcl.music.activities;

import android.os.Bundle;
import android.view.MenuItem;

import mst.app.MstActivity;

/**
 * Created by xiangxiangliu on 2015/11/2.
 */
public class BaseActivity extends MstActivity {

    private boolean isDestroyed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            onOptionsItemClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onOptionsItemClicked(){
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }
    public boolean isDestroyed(){
        return isDestroyed;
    }
}
