package cn.tcl.music.fragments.live;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.SingerDetailActivity;
import cn.tcl.music.model.live.AlbumBean;
import cn.tcl.music.model.live.ArtistBean;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.view.AuditionAlertDialog;
import mst.widget.FragmentStatePagerAdapter;
import mst.widget.ViewPager;
import mst.widget.tab.TabLayout;
import mst.widget.toolbar.Toolbar;

/**
 * @author zengtao.kuang
 * @Description: 歌手详情主框架Fragment
 * @date 2015/11/5 20:14
 * @copyright TCL-MIE
 */
public class SingerDetailFrameFragment extends Fragment implements View.OnClickListener{

    public static final String TAG = SingerDetailFrameFragment.class.getSimpleName();
    private SingerDetailActivity associatedActivity;
    private View rootView;
    private Adapter adapter;
    private ImageView logoIV;
    private ImageView logoLloading; //loading时显示的图片
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageButton fab;
    private boolean isFirstClick = true;
    private int singerSongCount = 0;
    private int singerAlbumCount = 0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        associatedActivity = (SingerDetailActivity)getActivity();
        rootView = inflater.inflate(R.layout.fragment_singer_main_frame, container, false);
        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        associatedActivity.setActionBar(toolbar);
        ActionBar actionBar = associatedActivity.getActionBar();
/*MODIFIED-BEGIN by lei.liu2, 2016-04-12,BUG-1919207*/
        /* MODIFIED-BEGIN by beibei.yang, 2016-05-18,BUG-2104905*/
        actionBar.hide();
        //actionBar.show();
        /* MODIFIED-END by beibei.yang,BUG-2104905*/
        /*MODIFIED-END by lei.liu2,BUG-1919207*/
        final TextView TextBar = (TextView) rootView.findViewById(R.id.textbar);
        TextBar.setText(associatedActivity.getArtistName());

        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitleEnabled(false);


        fab = (ImageButton)rootView.findViewById(R.id.fab);
        final float fabAlpha=fab.getAlpha();
        fab.setOnClickListener(this);

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.tab_indicator));
        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        if (viewPager != null) {
            adapter = new Adapter(associatedActivity.getFragmentManager(), associatedActivity.getApplicationContext());
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                tab.setCustomView(adapter.getTabView(i));
            }
        }

        logoIV = (ImageView)rootView.findViewById(R.id.logo);
        logoIV.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final List<SongDetailBean> dataList = associatedActivity.getSongDetailBeanList();
                if (dataList != null) {
                    //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_DETAIL_PLAYAll);
                    AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                        @Override
                        public void onPlay() {
                            new LiveMusicPlayTask(getActivity()).playNow(dataList, 0, isFirstClick);
                            isFirstClick = false;
                        }
                    });
                } else {
                    LogUtil.i(TAG, "dataList is null");
                }
            }
        });
        logoLloading = (ImageView)rootView.findViewById(R.id.logo_loading);
        ArtistBean artistBean = associatedActivity.getArtistBean();
        if(artistBean!=null){
            updateSingerDetail(artistBean);
        }else{
            associatedActivity.loadSingerDetailData();
        }
        final View tabshadow = rootView.findViewById(R.id.tabshadow);
        final float tabshadowAlpha = tabshadow.getAlpha();
        collapsingToolbar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                collapsingToolbar.getViewTreeObserver().removeOnPreDrawListener(this);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) collapsingToolbar.getLayoutParams();
                layoutParams.bottomMargin = -tabLayout.getHeight();
                collapsingToolbar.setLayoutParams(layoutParams);
                return false;
            }
        });

        final AppBarLayout appBarLayout = (AppBarLayout)rootView.findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                LogUtil.d(TAG, "onOffsetChanged " + verticalOffset + " getTotalScrollRange " + appBarLayout.getTotalScrollRange());
                int totalScrollRange = appBarLayout.getTotalScrollRange();
                if (totalScrollRange == 0) {
                    return;
                }
                int t = totalScrollRange + verticalOffset;
                if (t <= fab.getHeight() / 2 + 1) {
                    fab.setVisibility(View.GONE);
                } else {
                    fab.setVisibility(View.GONE);
                }

                logoIV.setAlpha((t * 1.0f) / totalScrollRange);
                tabshadow.setAlpha(tabshadowAlpha * ((t * 1.0f) / totalScrollRange));
                fab.setAlpha(fabAlpha*((t * 1.0f) / totalScrollRange));
            }
        });

        Bundle args = getArguments();

        if(args != null){
            singerSongCount = args.getInt(SingerDetailActivity.ARTIST_SONG_COUNT);
            singerAlbumCount = args.getInt(SingerDetailActivity.ARTIST_ALBUM_COUNT);
        }
        else{
            singerSongCount = 0;
            singerAlbumCount = 0;
        }

        return rootView;
    }

    public void updateTitle(){
        if(!isAdded()){
            return;
        }
        if(associatedActivity==null){
            return;
        }
        if(rootView==null){
            return;
        }
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(associatedActivity.getArtistName());

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);

        collapsingToolbar.setTitle(associatedActivity.getArtistName());

        //[BUGFIX]-Add-BEGIN by Peng.Tian,Defect 1940794,2016/04/25
        TextView TextBar = (TextView) rootView.findViewById(R.id.textbar);
        TextBar.setText(associatedActivity.getArtistName());
        //[BUGFIX]-Add-END by Peng.Tian,Defect 1940794,2016/04/25
    }

    public void updateSingerDetail(ArtistBean artistBean){
        if(artistBean==null){
            return;
        }
        if(adapter==null){
            return;
        }
        SingerDetailFragement singerDetailFragement = (SingerDetailFragement)adapter.getFragment(Adapter.SINGER_DETAIL_INDEX);
        if(singerDetailFragement!=null){
            singerDetailFragement.updateSingerDetail(artistBean);
        }
        if(logoIV !=null){
            Glide.with(this)
                    .load(ImageUtil.transferImgUrl(artistBean.artist_logo, 330))
                    .into(logoIV);
            logoIV.setVisibility(View.VISIBLE);
            logoLloading.setVisibility(View.GONE);
        }

    }

    public void updateSingerSong(List<SongDetailBean> songDetailBeanList){

        if(associatedActivity ==null){
            return;
        }
        if(adapter==null){
            return;
        }
        SingerSongFragment singerSongFragment = (SingerSongFragment)adapter.getFragment(Adapter.SONG_INDEX);
        if(singerSongFragment!=null){
            singerSongFragment.updateArtistHotSongs(songDetailBeanList);
        }

        TabLayout.Tab tab = tabLayout.getTabAt(Adapter.SONG_INDEX);
        View customView = tab.getCustomView();
        TextView textView = (TextView)customView.findViewById(R.id.num);
        int artistHotSongCount = associatedActivity.getArtistHotSongCount();
/* MODIFIED-BEGIN by zheng.ding, 2016-06-14,BUG-2226384*/
//        if(artistHotSongCount<10){
//            StringBuilder stringBuilder = new StringBuilder(" ");
//            stringBuilder.append(artistHotSongCount);
//            stringBuilder.append(" ");
//            textView.setText(stringBuilder.toString());
//        }else{
//            textView.setText(String.valueOf(artistHotSongCount));
//        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(getResources().getString(R.string.quantity_songs),artistHotSongCount));
        textView.setText(sb.toString());
        /* MODIFIED-END by zheng.ding,BUG-2226384*/
        textView.setVisibility(View.VISIBLE);
    }

    public void updateSingerAlbum(List<AlbumBean> artistAlbumsDataBeans){

        if(associatedActivity ==null){
            return;
        }
        if(adapter==null){
            return;
        }
        SingerAlbumsFragment singerAlbumsFragment = (SingerAlbumsFragment)adapter.getFragment(Adapter.ALBUM_INDEX);
        if(singerAlbumsFragment!=null){
            singerAlbumsFragment.updateArtistAlbums(artistAlbumsDataBeans);
        }

        TabLayout.Tab tab = tabLayout.getTabAt(Adapter.ALBUM_INDEX);
        View customView = tab.getCustomView();

        TextView textView = (TextView)customView.findViewById(R.id.num);
        int artistAlbumCount = associatedActivity.getArtistAlbumCount();
/* MODIFIED-BEGIN by zheng.ding, 2016-06-14,BUG-2226384*/
//        if(artistAlbumCount<10){
//            StringBuilder stringBuilder = new StringBuilder(" ");
//            stringBuilder.append(artistAlbumCount);
//            stringBuilder.append(" ");
//            textView.setText(stringBuilder.toString());
//        }else{
//            textView.setText(String.valueOf(artistAlbumCount));
//        }
        StringBuilder sb = new StringBuilder();
        if(isAdded()){
            sb.append(String.format(getResources().getString(R.string.quantity_albums),artistAlbumCount));
            textView.setText(sb.toString());
        }
        /* MODIFIED-END by zheng.ding,BUG-2226384*/
        textView.setVisibility(View.VISIBLE);
    }

    public void updateSingerSongStatus(){
        if(associatedActivity ==null){
            return;
        }
        if(adapter==null){
            return;
        }
        SingerSongFragment singerSongFragment = (SingerSongFragment)adapter.getFragment(Adapter.SONG_INDEX);
        if(singerSongFragment==null){
            return;
        }
        singerSongFragment.updateStatus(0, 0);
    }

    public void updateSingerAlbumStatus(){
        if(associatedActivity ==null){
            return;
        }
        if(adapter==null){
            return;
        }
        SingerAlbumsFragment singerAlbumsFragment = (SingerAlbumsFragment)adapter.getFragment(Adapter.ALBUM_INDEX);
        if(singerAlbumsFragment==null){
            return;
        }
        singerAlbumsFragment.updateStatus(0, 0);
    }

    public void updateSingerSongStatus(int totalSongCount, int realSongCount){
        if(associatedActivity ==null){
            return;
        }
        if(adapter==null){
            return;
        }
        SingerSongFragment singerSongFragment = (SingerSongFragment)adapter.getFragment(Adapter.SONG_INDEX);
        if(singerSongFragment==null){
            return;
        }
        singerSongFragment.updateStatus(totalSongCount, realSongCount);
    }

    public void updateSingerAlbumStatus(int totalSongCount, int realSongCount){
        if(associatedActivity ==null){
            return;
        }
        if(adapter==null){
            return;
        }
        SingerAlbumsFragment singerAlbumsFragment = (SingerAlbumsFragment)adapter.getFragment(Adapter.ALBUM_INDEX);
        if(singerAlbumsFragment==null){
            return;
        }
        singerAlbumsFragment.updateStatus(totalSongCount, realSongCount);
    }


    public void updateSingerDetailStatus(){
        if(associatedActivity ==null){
            return;
        }
        if(adapter==null){
            return;
        }
        SingerDetailFragement singerDetailFragement = (SingerDetailFragement)adapter.getFragment(Adapter.SINGER_DETAIL_INDEX);
        if(singerDetailFragement==null){
            return;
        }
        singerDetailFragement.updateStatus(0, 0);
    }

    @Override
    public void onClick(View v) {
        if (!isAdded()){
            return;
        }
        int currentItem = viewPager.getCurrentItem();
        if(Adapter.SONG_INDEX==currentItem){

        }else if(Adapter.ALBUM_INDEX==currentItem){

        }else if(Adapter.SINGER_DETAIL_INDEX==currentItem){

        }
        if (v.getId()== R.id.fab) {
            final List<SongDetailBean> dataList = associatedActivity.getSongDetailBeanList();
            if (dataList != null) {
                //MobclickAgent.onEvent(getActivity(), MobConfig.SINGER_DETAIL_PLAYAll);
                AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                    @Override
                    public void onPlay() {
                        new LiveMusicPlayTask(getActivity()).playNow(dataList, 0, isFirstClick);
                        isFirstClick = false;
                    }
                });
            } else {
                LogUtil.i(TAG, "dataList is null");
            }
        }


    }
    @Override
    public void onResume() {
        super.onResume();
        //MobclickAgent.onPageStart(MobConfig.SINGER_DETAIL_TOTAL_BROWSE_TIME);
    }
    @Override
    public void onPause() {
        super.onPause();
        //MobclickAgent.onPageEnd(MobConfig.SINGER_DETAIL_TOTAL_BROWSE_TIME);
    }

    static class Adapter extends FragmentStatePagerAdapter {
        public static final int SONG_INDEX = 0;
        public static final int ALBUM_INDEX = 1;
        public static final int SINGER_DETAIL_INDEX =2;
        private String[] titles;
        private Context context;

        private HashMap<Integer,WeakReference<Fragment>> mPageReferenceMap = new HashMap<Integer, WeakReference<Fragment>>();

        public Adapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
            Resources res = context.getResources();
            titles = new String[]{res.getString(R.string.songs),res.getString(R.string.album_title),res.getString(R.string.singer_detail)};
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if(SONG_INDEX==position){
                //MobclickAgent.onEvent(context, MobConfig.SINGER_DETAIL_SONG_TAB);
                fragment = SingerSongFragment.newInstance();
            }else if(ALBUM_INDEX==position){
                //MobclickAgent.onEvent(context, MobConfig.SINGER_DETAIL_ALBUM_TAB);
                fragment = SingerAlbumsFragment.newInstance();
            }else if(SINGER_DETAIL_INDEX==position){
                //MobclickAgent.onEvent(context, MobConfig.SINGER_DETAIL_INFORMATION_TAB);
                fragment = SingerDetailFragement.newInstance();
            }

            if(fragment!=null){
                mPageReferenceMap.put(position,new WeakReference<Fragment>(fragment));
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        public View getTabView(int position){
            View view = LayoutInflater.from(context).inflate(R.layout.tab_item, null);
            TextView titleTV= (TextView) view.findViewById(R.id.title);
            titleTV.setText(getPageTitle(position));

            return view;
        }

        @Override
        public void destroyItem(View container, int position, Object object) {

            super.destroyItem(container, position, object);

            mPageReferenceMap.remove(Integer.valueOf(position));
        }

        public Fragment getFragment(int key) {
            WeakReference<Fragment> fragmentSoftReference = mPageReferenceMap.get(key);
            if( null == fragmentSoftReference ){
                return null;
            }
            return fragmentSoftReference.get();
        }
    }
}
