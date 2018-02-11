package cn.tcl.music.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import cn.tcl.music.R;
import cn.tcl.music.adapter.LocalMediaAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.fragments.FolderDetailFragment;
import mst.widget.toolbar.Toolbar;

public class FolderDetailActivity extends BaseMusicActivity implements LocalMediaAdapter.IonSlidingViewClickListener {

    private static final String TAG = FolderDetailActivity.class.getSimpleName();
    private FolderDetailFragment mFolderDetailFragment;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_folder_detail);
    }

    @Override
    protected Activity getMainActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String title = bundle.getString(CommonConstants.BUNDLE_KEY_FOLDER_NAME);
        initView(bundle);
        initToolBar(title);
    }

    private void initView(Bundle bundle) {
        mFolderDetailFragment = new FolderDetailFragment();
        String tag = "FolderDetailFragment";
        mFolderDetailFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.folder_detail_external_container, mFolderDetailFragment, tag).commit();
    }

    private void initToolBar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.folder_detail_toolbar);
        toolbar.inflateMenu(R.menu.memu_other_music);
        toolbar.setTitle(title);
        toolbar.setTitleTextAppearance(FolderDetailActivity.this,R.style.ToolbarTitle);
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
                case R.id.other_setting:
                    Intent intent = new Intent(FolderDetailActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCurrentMusicMetaChanged() {
        if(mFolderDetailFragment != null){
            mFolderDetailFragment.onCurrentMetaChanged();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(View view, int position) {
        mFolderDetailFragment.clickItem(position);
    }

    @Override
    public void onDeleteBtnClick(View view, int position) {
        mFolderDetailFragment.deleteItem(position);
    }
}
