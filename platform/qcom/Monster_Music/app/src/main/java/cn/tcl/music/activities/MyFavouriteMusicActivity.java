package cn.tcl.music.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.adapter.LocalMediaAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.fragments.MyFavouriteMusicFragment;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.widget.ActionModeHandler;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.toolbar.Toolbar;

public class MyFavouriteMusicActivity extends BaseMusicActivity implements View.OnClickListener, LocalMediaAdapter.IonSlidingViewClickListener {
    private final static String TAG = MyFavouriteMusicActivity.class.getSimpleName();
    private MyFavouriteMusicFragment mMyFavouriteMusicFragment;
    private ActionModeHandler mActionModeHandler;
    private TextView mDownloadTextView;
    private TextView mAddTextView;
    private TextView mManagerTextView;
    public static final int REQUEST_CODE_SELECT_FAVORITE_SONGS = 1000;
    public static final int RESULT_CODE_SELECT_FAVORITE_SONGS = REQUEST_CODE_SELECT_FAVORITE_SONGS + 1;
    public static final int RESULT_CODE_SELECT_SONGS_FAVORITE_CANCEL = REQUEST_CODE_SELECT_FAVORITE_SONGS + 2;

    public MyFavouriteMusicFragment.OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener =
            new MyFavouriteMusicFragment.OnMediaFragmentSelectedListener() {
                @Override
                public void onAudioSelectdNum(ArrayList<Integer> songIds) {
                    if (mActionModeHandler == null) {
                        mActionModeHandler = new ActionModeHandler(MyFavouriteMusicActivity.this);
                    }
                    mActionModeHandler.setItemNum(songIds);
                }
            };


    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_my_favourite_music);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMyFavouriteMusicFragment = new MyFavouriteMusicFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, mMyFavouriteMusicFragment, MyFavouriteMusicFragment.class.getSimpleName()).commit();
        initToolBar();
        initView();
        mActionModeHandler = new ActionModeHandler(this);
    }

    @Override
    public void onCurrentMusicMetaChanged() {
        if (mMyFavouriteMusicFragment != null) {
            mMyFavouriteMusicFragment.onCurrentMetaChanged();
        }
    }

    private void initView() {
        mDownloadTextView = (TextView) findViewById(R.id.favorite_download_tv);
        mAddTextView = (TextView) findViewById(R.id.favorite_add_tv);
        mManagerTextView = (TextView) findViewById(R.id.favorite_manager_tv);
        mDownloadTextView.setOnClickListener(this);
        mAddTextView.setOnClickListener(this);
        mManagerTextView.setOnClickListener(this);
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_favourite_music_toolbar);
        toolbar.inflateMenu(R.menu.memu_other_music);
        toolbar.setTitle(getResources().getString(R.string.my_favourite_music));
        toolbar.setTitleTextAppearance(MyFavouriteMusicActivity.this, R.style.ToolbarTitle);
        toolbar.setOnMenuItemClickListener(mOnMenuItemClick);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        setActionModeListener(new ActionModeListener() {
            @Override
            public void onActionItemClicked(ActionMode.Item item) {
                switch (item.getItemId()) {
                    case ActionMode.NAGATIVE_BUTTON:
                        mMyFavouriteMusicFragment.leaveMultiChoose();
                        break;
                    case ActionMode.POSITIVE_BUTTON:
                        if (mMyFavouriteMusicFragment.isSelectAll()) {
                            getActionMode().setPositiveText(getResources().getString(R.string.select_all));
                            mMyFavouriteMusicFragment.selectAll(false);
                        } else {
                            getActionMode().setPositiveText(getResources().getString(R.string.cancel_select_all));
                            mMyFavouriteMusicFragment.selectAll(true);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onActionModeShow(ActionMode actionMode) {
            }

            @Override
            public void onActionModeDismiss(ActionMode actionMode) {
            }
        });
    }

    private Toolbar.OnMenuItemClickListener mOnMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.other_setting:
                    Intent intent = new Intent(MyFavouriteMusicActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    public void onItemClick(View view, int position) {
        if (null != mMyFavouriteMusicFragment) {
            mMyFavouriteMusicFragment.clickItem(position);
        }
    }

    public void onDeleteBtnClick(View view, int position) {
        if (null != mMyFavouriteMusicFragment) {
            mMyFavouriteMusicFragment.deleteItem(position);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.favorite_download_tv:
                //TODO Click to Download page
                break;
            case R.id.favorite_add_tv:
                ArrayList<Long> arrayList = mMyFavouriteMusicFragment.getSongsIds();
                long[] ids = new long[arrayList.size()];
                for (int i = 0; i < arrayList.size(); i++) {
                    ids[i] = arrayList.get(i);
                }
                Intent selectIntent = new Intent(this, SelectSongsActivity.class);
                selectIntent.putExtra(CommonConstants.SELECT_SONGS_ADD, CommonConstants.SELECT_SONGS_TO_FAVORITE);
                selectIntent.putExtra(CommonConstants.BUNDLE_KEY_FAVORITE_ADD_FLAG, ids);
                startActivityForResult(selectIntent, REQUEST_CODE_SELECT_FAVORITE_SONGS);
                break;
            case R.id.favorite_manager_tv:
//                mActionModeHandler.startActionMode(null);
//                mActionModeHandler.setItemNum(DEFAULT_ZERO);
                goToMultiChoose();
                mMyFavouriteMusicFragment.setMultiMode(true);
                setMyFavoriteTitleEnable(false);
                break;

        }
    }

    public void setMyFavoriteTitleEnable(boolean isAvailable) {
        mManagerTextView.setClickable(isAvailable);
        mAddTextView.setClickable(isAvailable);
        mDownloadTextView.setClickable(isAvailable);
        if (isAvailable) {
            mManagerTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_ENABLE_ALPHA);
            mAddTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_ENABLE_ALPHA);
            mDownloadTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_ENABLE_ALPHA);
        } else {
            mManagerTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
            mAddTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
            mDownloadTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.d(TAG, "onActivityResult and result code is " + resultCode);
        if (requestCode == REQUEST_CODE_SELECT_FAVORITE_SONGS) {
            if (resultCode == RESULT_CODE_SELECT_FAVORITE_SONGS && data.getExtras().getBoolean(CommonConstants.ADD_SUCCESS_KEY)) {
                ToastUtil.showToast(MyFavouriteMusicActivity.this, R.string.save_success);
            }
            mMyFavouriteMusicFragment.refreshData();
        }
    }

    public void goToMultiChoose() {
        mMyFavouriteMusicFragment.mBatchOperateLinearLayout.setVisibility(View.VISIBLE);
        mMyFavouriteMusicFragment.noclickableplayall();
        showActionMode(true);
        getActionMode().setPositiveText(getResources().getString(R.string.select_all));
    }

    @Override
    public void onBackPressed() {
        if (getActionMode().isShowing()) {
            mMyFavouriteMusicFragment.leaveMultiChoose();
            return;
        }
        super.onBackPressed();
    }

    public void setSelectedNumber(int selectedNum) {
        String format = getResources().getString(R.string.batch_songs_num);
        getActionMode().setTitle(String.format(format, selectedNum));
    }

}
