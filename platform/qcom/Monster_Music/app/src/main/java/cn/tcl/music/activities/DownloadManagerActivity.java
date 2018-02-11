package cn.tcl.music.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import cn.tcl.music.R;
import cn.tcl.music.fragments.DownloadFragment;
import mst.widget.toolbar.Toolbar;

public class DownloadManagerActivity extends BaseMusicActivity {

    private static final String TAG = DownloadManagerActivity.class.getSimpleName();
    private DownloadFragment mDownloadFragment;

    public static void launch(Activity from) {
        Intent intent = new Intent(from, DownloadManagerActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected Activity getMainActivity() {
        return this;
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_download_manager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDownloadFragment = new DownloadFragment();
        getFragmentManager().beginTransaction().replace(R.id.sliding_up_external_container, mDownloadFragment).commit();
        initToolBar();
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.download_toolbar);
        toolbar.inflateMenu(R.menu.menu_local_music);
        toolbar.setTitle(getResources().getString(R.string.download));
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitle);
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_search:
                    Intent searchIntent = new Intent(DownloadManagerActivity.this, SearchActivity.class);
                    startActivity(searchIntent);
                    break;
                case R.id.action_setting:
                    Intent intent = new Intent(DownloadManagerActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

//    @Override
//    protected void onResumeFragments() {
//        super.onResumeFragments();
//    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onCurrentMusicMetaChanged() {
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
