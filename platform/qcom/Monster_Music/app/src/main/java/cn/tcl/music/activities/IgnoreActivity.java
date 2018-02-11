package cn.tcl.music.activities;

import android.os.Bundle;
import android.view.View;

import cn.tcl.music.R;
import cn.tcl.music.adapter.LocalFolderAdapter;
import cn.tcl.music.adapter.LocalMediaAdapter;
import cn.tcl.music.fragments.IgnoredFragment;
import mst.widget.toolbar.Toolbar;

public class IgnoreActivity extends BaseMusicActivity implements LocalMediaAdapter.IonSlidingViewClickListener,
        LocalFolderAdapter.IonSlidingViewClickListener, View.OnClickListener {

    private final static String TAG = IgnoreActivity.class.getSimpleName();
    private boolean isEmpty = true;
    private IgnoredFragment mIgnoredFragment;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_ignored);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIgnoredFragment = new IgnoredFragment();
        getFragmentManager().beginTransaction().replace(R.id.main_container, mIgnoredFragment, TAG).commit();
        initToolBar();
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.folder_ignored_toolbar);
        toolbar.setTitle(getResources().getString(R.string.ignored));
        toolbar.setTitleTextAppearance(IgnoreActivity.this,R.style.ToolbarTitle);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCurrentMusicMetaChanged() {

    }

    @Override
    public void onItemClick(View view, int position) {
        mIgnoredFragment.clickItem(position);
    }

    @Override
    public void onDeleteBtnClick(View view, int position) {

    }

    @Override
    public void onClick(View view) {

    }

}
