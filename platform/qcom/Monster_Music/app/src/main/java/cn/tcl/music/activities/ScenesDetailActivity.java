package cn.tcl.music.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.tcl.music.R;
import cn.tcl.music.adapter.LocalMediaAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.fragments.ScenesDetailFragment;
import cn.tcl.music.model.ScenesBean;
import cn.tcl.music.util.SystemUtility;
import mst.widget.toolbar.Toolbar;

public class ScenesDetailActivity extends BaseMusicActivity implements LocalMediaAdapter.IonSlidingViewClickListener{

    private static final String TAG = ScenesDetailActivity.class.getSimpleName();
    private long mScenesId;
    private ScenesBean mScenesBean;
    private ScenesDetailFragment mScenesDetailFragment;
    private final static long SCENESID_OVER = 10000;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_scenes_detail);
    }

    @Override
    protected Activity getMainActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mScenesDetailFragment = new ScenesDetailFragment();
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            mScenesBean = (ScenesBean) getIntent().getExtras().getSerializable(CommonConstants.BUNDLE_KEY_SCENE);
            mScenesId = mScenesBean.getScenesId();
            mScenesDetailFragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().replace(R.id.detail_up_external_scenes,
                    mScenesDetailFragment).commit();
            mScenesId = mScenesBean.getScenesId();
            initToolBar();
            initView();
        }
    }

    @Override
    public void onCurrentMusicMetaChanged() {
        if(mScenesDetailFragment!=null){
            mScenesDetailFragment.onCurrentMusicChanged();
        }
    }

    private void initView() {
        ImageView imageView = (ImageView) findViewById(R.id.scenes_detail_image);
        int[] imageBimap = new int[]{R.drawable.bigscenes1, R.drawable.bigscenes2,
                R.drawable.bigscenes3, R.drawable.bigscenes4,
                R.drawable.bigscenes5, R.drawable.bigscenes6,
                R.drawable.bigscenes7, R.drawable.bigscenes8,
                R.drawable.bigscenes9, R.drawable.bigscenes10};
        imageView.setImageResource(imageBimap[(int) (mScenesId - SCENESID_OVER)]);
    }

    private void initToolBar() {



        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.transparent));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        Toolbar toolbar = (Toolbar) findViewById(R.id.scenes_detail_toolbar);
        toolbar.inflateMenu(R.menu.memu_other_music);
        toolbar.setTitle(mScenesBean.getScenesText());
        toolbar.setTitleTextAppearance(ScenesDetailActivity.this,R.style.ToolbarTitle);
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, toolbar.getMinimumHeight());
        layoutParams.setMargins(0, SystemUtility.getStatusBarHeight(), 0, 0);
        toolbar.setLayoutParams(layoutParams);
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.other_setting:
                    Intent intent = new Intent(ScenesDetailActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.memu_other_music, menu);
        return true;
    }

    @Override
    public void onItemClick(View view, int position) {
        if (mScenesDetailFragment != null) {
            mScenesDetailFragment.clickItem(position);
        }
    }

    @Override
    public void onDeleteBtnClick(View view, int position) {

    }
}
