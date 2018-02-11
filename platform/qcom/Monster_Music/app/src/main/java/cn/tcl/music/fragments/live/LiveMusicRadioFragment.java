package cn.tcl.music.fragments.live;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.FragmentArgs;
import cn.tcl.music.activities.SearchActivity;
import cn.tcl.music.activities.SettingsActivity;
import cn.tcl.music.activities.live.FragmentContainerActivityV2;
import cn.tcl.music.adapter.live.RadioListAdapter;
import cn.tcl.music.fragments.NetWorkBaseFragment;
import cn.tcl.music.model.live.LiveMusicRadio;
import cn.tcl.music.model.live.LiveMusicRankItem;
import cn.tcl.music.model.live.RadioCategoryBean;
import cn.tcl.music.network.DataRequest;
import cn.tcl.music.network.LiveMusicRadioCategoriesTask;
import cn.tcl.music.network.LiveMusicRadioGuessTask;
import cn.tcl.music.network.LiveMusicRadioTask;
import cn.tcl.music.network.LiveMusicRankTask;
import cn.tcl.music.view.LiveMusicItemRadioLayout;
import cn.tcl.music.view.OnDetailItemClickListener;
import mst.widget.toolbar.Toolbar;

public class LiveMusicRadioFragment extends NetWorkBaseFragment implements OnDetailItemClickListener, View.OnClickListener {
    private static String TAG = LiveMusicRadioFragment.class.getSimpleName();
    private static final int RADIO_LIST_SIZE = 50;

    private static final int ORIGIN_TYPE = 0;
    private static final int SENCE_TYPE = 1;
    private static final int STYLE_TYPE = 2;
    private static final int MOOD_TYPE = 3;

    private static final String ORIGIN_ID = "113597100";
    private static final String SENCE_ID = "2";
    private static final String STYLE_ID = "3";
    private static final String MOOD_ID = "4";

    private static final String DEFAULT_PAGE = "1";

    private LiveMusicRadioCategoriesTask mRadioCategoriesTask;
    private LiveMusicRadioTask mLiveMusicRadioTask;
    private LiveMusicRadioGuessTask mLiveMusicRadioGuessTask;

    private LiveMusicItemRadioLayout mLiveMusicItemRadioLayout;
    private RelativeLayout mGuessRadio;
    private RelativeLayout mPrivateRadio;
    private RecyclerView mRecyclerView;

    private ArrayList<RadioCategoryBean> mCategoryBeans = new ArrayList<RadioCategoryBean>();
    private ArrayList<RadioCategoryBean> mTempData = new ArrayList<RadioCategoryBean>();
    private List<LiveMusicRadio> mRadioDatas = new ArrayList<LiveMusicRadio>();

    List<LiveMusicItemRadioLayout.LiveItem> datas = new ArrayList<LiveMusicItemRadioLayout.LiveItem>();
    LiveMusicItemRadioLayout.LiveItem mItem = null;

    public static void launch(Activity activity, List<LiveMusicRankItem> ranks) {
        FragmentArgs args = new FragmentArgs();
        FragmentContainerActivityV2.launch(activity, LiveMusicRadioFragment.class, args);
    }

    public static void launch(Activity activity) {
        FragmentArgs args = new FragmentArgs();
        FragmentContainerActivityV2.launch(activity, LiveMusicRadioFragment.class, args);
    }

    @Override
    protected int getSubContentLayout() {
        return R.layout.fragment_radio;
    }

    //call when onCreateView();init widget
    @Override
    protected void findViewByIds(View parent) {
        super.findViewByIds(parent);
        Toolbar toolbar = (Toolbar) parent.findViewById(R.id.online_radio_toolbar);
        toolbar.inflateMenu(R.menu.menu_local_music);
        toolbar.setTitle(getResources().getString(R.string.radio));
        toolbar.setTitleTextAppearance(getActivity(), R.style.ToolbarTitle);
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        mLiveMusicItemRadioLayout = (LiveMusicItemRadioLayout) parent.findViewById(R.id.live_music_item_radio);
        mLiveMusicItemRadioLayout.setChildCount(4);
        mGuessRadio = (RelativeLayout) parent.findViewById(R.id.rl_guess_radio);
        mPrivateRadio = (RelativeLayout) parent.findViewById(R.id.rl_private_radio);
        mRecyclerView = (RecyclerView) parent.findViewById(R.id.recycle_view);
        mGuessRadio.setOnClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_search:
                    Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                    startActivity(searchIntent);
                    break;
                case R.id.action_setting:
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    //call when onViewCreated();
    @Override
    protected void initViews() {
        super.initViews();

        mRadioCategoriesTask = new LiveMusicRadioCategoriesTask(getActivity(), this);
        mRadioCategoriesTask.executeMultiTask();

        mLiveMusicRadioTask = new LiveMusicRadioTask(getActivity(), this, RADIO_LIST_SIZE);
        mLiveMusicRadioTask.executeMultiTask();

        mLiveMusicItemRadioLayout.addImgClickListener(new LiveMusicItemRadioLayout.LiveMusicClickListener() {
            @Override
            public void onImgClick(Context context, int position) {
                //click four categories radio
                loadRadioCategoriesWithPosition(position);
            }
        });
    }

    //load radio with categories
    private void loadRadioCategoriesWithPosition(int position) {
        switch (position) {
            case ORIGIN_TYPE:
                mLiveMusicRadioTask = new LiveMusicRadioTask(getActivity(), this, ORIGIN_ID, DEFAULT_PAGE);
                mLiveMusicRadioTask.executeMultiTask();
                break;
            case SENCE_TYPE:
                mLiveMusicRadioTask = new LiveMusicRadioTask(getActivity(), this, SENCE_ID, DEFAULT_PAGE);
                mLiveMusicRadioTask.executeMultiTask();
                break;
            case STYLE_TYPE:
                mLiveMusicRadioTask = new LiveMusicRadioTask(getActivity(), this, STYLE_ID, DEFAULT_PAGE);
                mLiveMusicRadioTask.executeMultiTask();
                break;
            case MOOD_TYPE:
                mLiveMusicRadioTask = new LiveMusicRadioTask(getActivity(), this, MOOD_ID, DEFAULT_PAGE);
                mLiveMusicRadioTask.executeMultiTask();
                break;
            default:
                break;
        }
    }


    @Override
    public void onLoadFail(int dataType, String message) {
        super.onLoadFail(dataType, message);
        showFail();
        //finishBottomLoading();
    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        super.onLoadSuccess(dataType, datas);

        hideLoading();
        showContent();

        if (datas == null || datas.isEmpty()) {
            showNoData();
            return;
        }

        switch (dataType) {
            case DataRequest.Type.TYPE_LIVE_RADIO_CATEGORES:
                mTempData = (ArrayList<RadioCategoryBean>) datas;
                showRecommendedRadio(mTempData);
                break;
            case DataRequest.Type.TYPE_LIVE_RADIO:
                mRadioDatas = datas;
                RadioListAdapter adapter = new RadioListAdapter(getContext(), mRadioDatas);
                adapter.setOnDetailItemClickListener(this);
                mRecyclerView.setAdapter(adapter);
                break;
            default:
                break;
        }

    }

    private void showRecommendedRadio(ArrayList<RadioCategoryBean> list) {
        mCategoryBeans.clear();
        try {
            mCategoryBeans.add(list.get(1));
            mCategoryBeans.add(list.get(2));
            mCategoryBeans.add(list.get(3));
            mCategoryBeans.add(list.get(4));
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        mLiveMusicItemRadioLayout.showData(changeLiveItem(mCategoryBeans), getActivity());

    }

    private List<LiveMusicItemRadioLayout.LiveItem> changeLiveItem(ArrayList<RadioCategoryBean> categoryBeans) {
        for (int i = 0; i < categoryBeans.size(); i++) {
            mItem = new LiveMusicItemRadioLayout.LiveItem();
            mItem.name = categoryBeans.get(i).category_name;
            mItem.imgUrl = categoryBeans.get(i).radio_logo;
            datas.add(mItem);
        }
        return datas;
    }

    @Override
    protected void doReloadData() {
        showLoading();
        new LiveMusicRankTask(getActivity(), this).executeMultiTask();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRadioCategoriesTask != null) {
            mRadioCategoriesTask.cancel(true);
        }
        if (mLiveMusicRadioTask != null) {
            mLiveMusicRadioTask.cancel(true);
        }
    }

    @Override
    public void onClick(View v, Object object, int position) {
        if (!isAdded()) {
            return;
        }
        int id = v.getId();
        if (id == R.id.rl_item_online_radio) {

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_guess_radio:
                /*mLiveMusicRadioGuessTask = new LiveMusicRadioGuessTask(getContext(), this);
                mLiveMusicRadioGuessTask.executeMultiTask();*/
                break;
            default:
                break;
        }
    }
}
