/* MODIFIED-BEGIN by zheng.ding, 2016-06-14, BUG-2226384*/
package cn.tcl.music.fragments.live;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
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
import cn.tcl.music.activities.live.AlbumDetailActivity;
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
 * @Description: 专集详情主框架Fragment
 * @date 2015/11/20 09:30
 * @copyright TCL-MIE
 */
public class AlbumDetailMainFrameFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "AlbumDetailMainFrameFragment";

    private AlbumDetailActivity associatedActivity;
    private Adapter adapter;
    private ImageView logoIV;
    private ImageView logoLloading;  //loading时显示的图片
    private TabLayout tabLayout;
    private boolean isFirstClick = true ;
    private int albumSongCount = 0;
    private View rootView;//[BUGFIX]-Modified by Peng.Tian,Defect 1940794,2016/04/25
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        associatedActivity = (AlbumDetailActivity)getActivity();
        rootView = inflater.inflate(R.layout.fragment_collect_detail_main_frame, container, false);//[BUGFIX]-Modified by Peng.Tian,Defect 1940794,2016/04/25
        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        associatedActivity.setActionBar(toolbar);
        ActionBar actionBar = associatedActivity.getActionBar();
        actionBar.hide();
        final TextView TextBar = (TextView) rootView.findViewById(R.id.textbar);
        TextBar.setText(associatedActivity.getAlbumName());

        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.tab_indicator));
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        if (viewPager != null) {
            adapter = new Adapter(associatedActivity.getFragmentManager(), associatedActivity);
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                tab.setCustomView(adapter.getTabView(i));
            }
        }

        logoLloading = (ImageView)rootView.findViewById(R.id.logo_loading);

        logoIV = (ImageView)rootView.findViewById(R.id.logo);
        logoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<SongDetailBean> dataList = associatedActivity.getSongDetailBeanList();
                if(dataList != null){

                    AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                        @Override
                        public void onPlay() {
                            new LiveMusicPlayTask(getActivity()).playNow(dataList,0, isFirstClick);
                            isFirstClick = false;
                        }
                    });
                }
                else{
                    LogUtil.i(TAG, "dataList is null");
                }
            }
        });

        List<SongDetailBean> songDetailBeanList = associatedActivity.getSongDetailBeanList();
        if(songDetailBeanList!=null){
            updateAlbumDetailSong(songDetailBeanList, albumSongCount);
        }else {
            associatedActivity.loadAlbumDetailData();
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

        final ImageButton fab = (ImageButton)rootView.findViewById(R.id.fab);
        final float fabAlpha=fab.getAlpha();
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
                fab.setAlpha(fabAlpha * ((t * 1.0f) / totalScrollRange));
            }
        });

        //fab.setOnClickListener(this);
        Bundle args = getArguments();

        if(args != null){
            albumSongCount = args.getInt(AlbumDetailActivity.ALBUM_SONG_COUNT, 0);
        }
        else{
            albumSongCount = 0;
        }
        return rootView;
    }
    private android.os.Handler mHandler = new android.os.Handler();

    public void updateAlbumDetailLogo( String logoUrl){
        if(logoIV !=null){
            Glide.with(getContext())
                    .load(ImageUtil.transferImgUrl(logoUrl, 330))
                    .placeholder(R.drawable.default_cover_details_small)
                    .into(logoIV);
            logoIV.setVisibility(View.VISIBLE);
            logoLloading.setVisibility(View.GONE);
        }
    }



    public void updateAlbumDetailSong(List<SongDetailBean> songDetailBeanList, int albumSongCount){
        if(songDetailBeanList==null){
            return;
        }
        if(adapter==null){
            return;
        }
        AlbumDetailSongFragment albumDetailSongFragment = (AlbumDetailSongFragment)adapter.getFragment(Adapter.SONG_INDEX);
        if(albumDetailSongFragment!=null){
            albumDetailSongFragment.updateAlbumDetailSong(songDetailBeanList);
        }

        TabLayout.Tab tab = tabLayout.getTabAt(Adapter.SONG_INDEX);
        View customView = tab.getCustomView();
        TextView textView = (TextView)customView.findViewById(R.id.num);
    /* MODIFIED-BEGIN by zheng.ding, 2016-06-14,BUG-2226384*/
    //    textView.setText(String.valueOf(associatedActivity.getSongDetailBeanListCount()));
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(getResources().getString(R.string.quantity_songs),associatedActivity.getSongDetailBeanListCount()));
        textView.setText(sb.toString());
        /* MODIFIED-END by zheng.ding,BUG-2226384*/
        textView.setVisibility(View.VISIBLE);
    }

    public void updateAlbumDetailDescription(String description){
        if(description==null){
            return;
        }
        if(adapter==null){
            return;
        }
        AlbumDetailDescriptionFragement albumDetailDescriptionFragement = (AlbumDetailDescriptionFragement)adapter.getFragment(Adapter.DETAIL);
        if(albumDetailDescriptionFragement!=null){
            albumDetailDescriptionFragement.updateAlbumDetailDescription(description);
        }
    }

    public void updateAlbumSongStatus(){
        if(associatedActivity ==null){
            return;
        }
        if(adapter==null){
            return;
        }
        AlbumDetailSongFragment albumDetailSongFragment = (AlbumDetailSongFragment)adapter.getFragment(Adapter.SONG_INDEX);
        if(albumDetailSongFragment==null){
            return;
        }
        albumDetailSongFragment.updateStatus(0, 0);
    }

    public void updateAlbumSongStatus(int totalSongCount, int realSongCount){
        if(associatedActivity ==null){
            return;
        }
        if(adapter==null){
            return;
        }
        AlbumDetailSongFragment albumDetailSongFragment = (AlbumDetailSongFragment)adapter.getFragment(Adapter.SONG_INDEX);
        if(albumDetailSongFragment==null){
            return;
        }
        albumDetailSongFragment.updateStatus(totalSongCount, realSongCount);
    }

    public void updateAlbumDescriptionStatus(){
        if(associatedActivity ==null){
            return;
        }
        AlbumDetailDescriptionFragement albumDetailDescriptionFragement = (AlbumDetailDescriptionFragement)adapter.getFragment(Adapter.DETAIL);
        if(albumDetailDescriptionFragement==null){
            return;
        }
        albumDetailDescriptionFragement.updateStatus();
    }


    static class Adapter extends FragmentStatePagerAdapter {
        public static final int SONG_INDEX = 0;
        public static final int DETAIL = 1;
        private String[] titles;
        private Context context;

        private HashMap<Integer,WeakReference<Fragment>> mPageReferenceMap = new HashMap<>();

        public Adapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
            Resources res = context.getResources();
            titles = new String[]{res.getString(R.string.songs),res.getString(R.string.album_detail)};
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if(SONG_INDEX==position){
                fragment = AlbumDetailSongFragment.newInstance();
            }else if(DETAIL ==position){
                fragment = AlbumDetailDescriptionFragement.newInstance();
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

    @Override
    public void onClick(View v) {
        if (!isAdded()){
            return;
        }
        final List<SongDetailBean> dataList = associatedActivity.getSongDetailBeanList();
        if(dataList != null){

            AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                @Override
                public void onPlay() {
                    new LiveMusicPlayTask(getActivity()).playNow(dataList,0, isFirstClick);
                    isFirstClick = false;
                }
            });
        }
        else{
            LogUtil.i(TAG, "dataList is null");
        }
    }

    //[BUGFIX]-Add-BEGIN by Peng.Tian,Defect 1940794,2016/04/25
    public void updateAlbumTitle(String albumName){
        TextView TextBar = (TextView) rootView.findViewById(R.id.textbar);
        TextBar.setText(associatedActivity.getAlbumName());
    }

    //[BUGFIX]-Add-END by Peng.Tian,Defect 1940794,2016/04/25
}
/* MODIFIED-END by zheng.ding,BUG-2226384*/

