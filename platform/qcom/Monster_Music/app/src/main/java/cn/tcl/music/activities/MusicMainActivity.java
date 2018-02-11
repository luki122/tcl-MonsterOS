package cn.tcl.music.activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiami.sdk.XiamiSDK;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DatabaseSyncRegister;
import cn.tcl.music.fragments.LocalMusicFragment;
import cn.tcl.music.fragments.OnlineFragment;
import cn.tcl.music.model.live.XiamiMemberInfo;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveGetMemberInfoTask;
import cn.tcl.music.util.ActivitiesManager;
import cn.tcl.music.util.FixedSpeedScroller;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.PermissionsUtil;
import cn.tcl.music.util.PreferenceUtil;
import cn.tcl.music.util.SDKUtil;
import cn.tcl.music.util.live.UserInfoManager;
import mst.widget.FragmentPagerAdapter;
import mst.widget.ViewPager;
import mst.widget.toolbar.Toolbar;

public class MusicMainActivity extends BaseMusicActivity {
    private static final String TAG = MusicMainActivity.class.getSimpleName();
    private static final int SELECT_LOCAL_PAGE = 0;
    private static final int SELECT_ONLINE_PAGE = 1;
    private static final String SCROLLER = "mScroller";
    private int mPageMode;
    private TextView mLocalTextView;
    private TextView mFindTextView;
    private ImageView mSelectLocalIv;
    private ImageView mSelectOnlineIv;
    private ViewPager mViewPager;
    private ArrayList<Fragment> mFragmentList;
    private DatabaseSyncRegister mRegister;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_music_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PermissionsUtil.shouldRequestPermissions(this)) {
            return;
        }
        initData();
        initToolBar();
        initViewPager();
        mRegister = DatabaseSyncRegister.getInstance(this.getApplicationContext());
        mRegister.startListeningDBSync();
        //register xiami sdk
        XiamiSDK.init(getApplicationContext(), SDKUtil.KEY, SDKUtil.SECRET);
        XiamiSDK.enableLog(true);
        refreshXiamiMemberInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRegister != null) {
            mRegister.stopListeningDBSync();
        }
    }

    private void initData() {
        mPageMode = 0;
        mFragmentList = new ArrayList<Fragment>();
    }

    public void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.music_main_toolbar);
        toolbar.inflateMenu(R.menu.menu_music_main);
        toolbar.setNavigationIcon(R.drawable.local_search);
        toolbar.setTitle(R.string.search_hint);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() != R.id.menu_setting) {
                    Intent intent = new Intent(MusicMainActivity.this, SearchActivity.class);
                    startActivity(intent);
                }
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_setting) {
                    Intent intent = new Intent(MusicMainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        });
        initView();
    }

    private void initView() {
        mLocalTextView = (TextView) findViewById(R.id.header_local);
        mFindTextView = (TextView) findViewById(R.id.hearder_found);
        mSelectLocalIv = (ImageView) findViewById(R.id.heard_select1);
        mSelectOnlineIv = (ImageView) findViewById(R.id.heard_select2);
        mLocalTextView.setOnClickListener(new PageChangeClick());
        mFindTextView.setOnClickListener(new PageChangeClick());
    }

    private class PageChangeClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.header_local:
                    mViewPager.setCurrentItem(SELECT_LOCAL_PAGE);
                    break;
                case R.id.hearder_found:
                    mViewPager.setCurrentItem(SELECT_ONLINE_PAGE);
                    break;
                default:
                    break;
            }
        }
    }

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.base_viewpager);
        Fragment localFragment = new LocalMusicFragment();
        mFragmentList.add(localFragment);
        Fragment foundFragment = new OnlineFragment();
        mFragmentList.add(foundFragment);
        mViewPager.setAdapter(new BaseMusicAdapter(getFragmentManager(), mFragmentList));
        mViewPager.setCurrentItem(mPageMode);
        mViewPager.setOnPageChangeListener(new BaseChangeListner());
        mViewPager.setOffscreenPageLimit(0);
        setViewPagerScrollSpeed();
    }

    private void setViewPagerScrollSpeed() {
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField(SCROLLER);
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(mViewPager.getContext());
            mScroller.set(mViewPager, scroller);
        } catch (NoSuchFieldException e) {

        } catch (IllegalArgumentException e) {

        } catch (IllegalAccessException e) {
        }
    }

    private class BaseMusicAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> mList;

        public BaseMusicAdapter(android.app.FragmentManager fragmentManager, ArrayList<Fragment> list) {
            super(fragmentManager);
            this.mList = list;
        }

        @Override
        public Fragment getItem(int arg0) {
            return mList.get(arg0);
        }

        @Override
        public int getCount() {
            return mList.size();
        }
    }

    private class BaseChangeListner implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPageSelected(int arg0) {
            setTextColor(arg0);
            if (arg0 == SELECT_LOCAL_PAGE) {
                LocalMusicFragment fragment = (LocalMusicFragment) mFragmentList.get(0);
                if (fragment != null) {
                    fragment.refresh();
                }
            }
        }
    }

    private void setTextColor(int id) {
        if (id == SELECT_LOCAL_PAGE) {
            mLocalTextView.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
            mFindTextView.setAlpha(CommonConstants.VIEW_LOCAL_NO_SELECTER_TITLE_ALPHA);
            mSelectLocalIv.setVisibility(View.VISIBLE);
            mSelectOnlineIv.setVisibility(View.INVISIBLE);
        } else if (id == SELECT_ONLINE_PAGE) {
            mLocalTextView.setAlpha(CommonConstants.VIEW_LOCAL_NO_SELECTER_TITLE_ALPHA);
            mFindTextView.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
            mSelectLocalIv.setVisibility(View.INVISIBLE);
            mSelectOnlineIv.setVisibility(View.VISIBLE);
        } else {
            LogUtil.e(TAG, "id number is error");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivitiesManager.clearActivities(true);//[BUGFIX]-Add by peng.tian-nb,Defect 1966917,2016/04/27
    }

    @Override
    public void onCurrentMusicMetaChanged() {

    }

    private void startLocalMusicActivity() {
        Intent intent = new Intent(this, LocalMusicActivity.class);
        startActivity(intent);
    }

    private void startSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    private void startPlayListActivity() {
        Intent intent = new Intent(this, PlaylistDetailActivity.class);
        startActivity(intent);
    }

    private void startMyFavouriteActivity() {
        Intent intent = new Intent(this, MyFavouriteMusicActivity.class);
        startActivity(intent);
    }

    private void startPlayingActivity() {
        Intent intent = new Intent(this, PlayingActivity.class);
        startActivity(intent);
    }

    private void refreshXiamiMemberInfo() {
        XiamiSDK xiamiSDK = new XiamiSDK();
        if (xiamiSDK.isLogin()) {
            long userId = PreferenceUtil.getValue(this,PreferenceUtil.NODE_XIAMI_MEMBER_INFO,PreferenceUtil.KEY_USER_ID,0l);
            LogUtil.d(TAG,"userId is " + userId);
            LiveGetMemberInfoTask getMemberInfoTask = new LiveGetMemberInfoTask(this, new ILoadData() {
                @Override
                public void onLoadSuccess(int dataType, List datas) {
                    if (null != datas && !datas.isEmpty()) {
                        XiamiMemberInfo info = (XiamiMemberInfo) datas.get(0);
                        LogUtil.d(TAG,"info userid is " + info.user_id + " and nickname is " + info.nick_name);
                        UserInfoManager userInfoManager = UserInfoManager.getInstance(MusicMainActivity.this);
                        userInfoManager.setmMemberInfo(info);
                    }
                }

                @Override
                public void onLoadFail(int dataType, String message) {

                }
            },userId);
            getMemberInfoTask.executeMultiTask();
        }
    }

}
