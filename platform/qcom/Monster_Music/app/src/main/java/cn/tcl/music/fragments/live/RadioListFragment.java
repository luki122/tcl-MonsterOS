package cn.tcl.music.fragments.live;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.adapter.ListBaseAdapter;
import cn.tcl.music.model.live.LiveMusicRadio;
import cn.tcl.music.model.live.RadioBean;
import cn.tcl.music.model.live.RadioGridBean;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.network.LiveMusicRadioDetailTask;
import cn.tcl.music.network.LiveMusicRadioTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.view.AuditionAlertDialog;

public class RadioListFragment extends BaseListFragment<RadioGridBean> {
    private static final String TAG = RadioListFragment.class.getSimpleName();
    private ATabTitlePagerFragment.TabTitlePagerBean titlePagerBean;

    public static RadioListFragment newInstance(ATabTitlePagerFragment.TabTitlePagerBean bean) {
        RadioListFragment fragment = new RadioListFragment();
        Bundle args = new Bundle();
        args.putSerializable("titlePagerBean", bean);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int inflateContentView() {
        return R.layout.fragment_radio_list_pull_refresh_listview;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        if (savedInstanceSate == null) {
            titlePagerBean = (ATabTitlePagerFragment.TabTitlePagerBean) getArguments().getSerializable("titlePagerBean");
        } else {
            titlePagerBean = (ATabTitlePagerFragment.TabTitlePagerBean) savedInstanceSate.getSerializable("titlePagerBean");
        }
        if (titlePagerBean == null) {
            titlePagerBean = new ATabTitlePagerFragment.TabTitlePagerBean();
        }

        super.layoutInit(inflater, savedInstanceSate);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("titlePagerBean", titlePagerBean);
    }

    LiveMusicRadioTask liveMusicRadioTask;

    @Override
    protected void sendRequestData() {
        //虾米sdk获取数据 从第1页开始
        if (mCurrentPage == 0) {
            mCurrentPage = 1;
        }
        LogUtil.e(TAG, "RadioListFragment sendRequestData , mCurrentPage = " + mCurrentPage);
        //电台
        liveMusicRadioTask = new LiveMusicRadioTask(getActivity(), this, getRadioSearchType(), String.valueOf(mCurrentPage));
        liveMusicRadioTask.executeMultiTask();
    }


    private String getRadioSearchType() {
        if (titlePagerBean != null) {
            return titlePagerBean.getType();
        }
        return null;
    }


    @Override
    protected ListBaseAdapter getListAdapter() {
        return new RadioAdapter();
    }

    private boolean haveMoreData = true;

    @Override
    protected boolean haveNoMoreData(List<RadioGridBean> data) {
        return !haveMoreData;
    }

    List<RadioBean> beans; //本次从服务器拉取到的数据

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        super.onLoadSuccess(dataType, datas);
        if (datas != null && datas.size() > 0) {
            LiveMusicRadio radio = (LiveMusicRadio) datas.get(0);
            beans = radio.radios;
            haveMoreData = radio.more;
            List<RadioGridBean> assembleDatas = assemblingData(radio.radios);
            executeOnLoadDataSuccess(assembleDatas);
        }
    }

    @Override
    protected int getDataSize(List<RadioGridBean> data) {
        return beans.size();
    }

    @Override
    protected int getPageSize() {
        return LiveMusicRadioTask.REQUEST_RADIO_PAGE_SIZE;
    }

    private List<RadioGridBean> assemblingData(List<RadioBean> radios) {
        List<RadioGridBean> gridBeanList = new ArrayList<>();
        int columnNumbers = 3;
        if (radios != null && radios.size() > 0) {
            int row = 0;
            int size = radios.size();
            if (size % columnNumbers == 0) {
                row = radios.size() / columnNumbers;
            } else {
                row = radios.size() / columnNumbers + 1;
            }
            int index = 0;
            for (int i = 0; i < row; i++) {
                RadioGridBean gridBean = new RadioGridBean();
                for (int j = 0; j < columnNumbers; j++) {
                    if ((j + index * columnNumbers) < radios.size()) {
                        RadioBean radioBean = radios.get(j + index * columnNumbers);
                        gridBean.radioBeans.add(radioBean);
                    }
                }
                index++;
                gridBeanList.add(gridBean);
            }
        }
        return gridBeanList;
    }


    @Override
    public void onLoadFail(int dataType, String message) {
        super.onLoadFail(dataType, message);
    }


    public static final int IMAGE_QUALITY_SIZE = 300;

    public class RadioAdapter extends ListBaseAdapter<RadioGridBean> {

        @SuppressLint("InflateParams")
        @Override
        protected View getRealView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null || convertView.getTag() == null) {
                convertView = getLayoutInflater(parent.getContext()).inflate(
                        R.layout.item_radio_v2, null);
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            RadioGridBean data = mDatas.get(position);
            ArrayList<RadioBean> itemBeans = data.radioBeans;

            //推荐
            if ("1".equals(titlePagerBean.getType())) {
                LogUtil.d(TAG, "titlePagerBean = " + titlePagerBean + ", recommand data = " + itemBeans);
            }
            //原创
            if ("113597100".equals(titlePagerBean.getType())) {
                LogUtil.d(TAG, "titlePagerBean = " + titlePagerBean + ", orginal data = " + itemBeans);
            }

            if (data != null && itemBeans != null) {
                if (itemBeans.size() > 0) {
                    vh.layout1.setVisibility(View.VISIBLE);
                    //获取电台列表数据的时候  除了原创会返回小图片 之外  其他的例如推荐 场景都是返回高清的图片
                    //为了统一处理  传入固定的size modify by xiangxiang.liu 2015/11/28 16:04
                    Glide.with(getContext())
                            .load(ImageUtil.transferImgUrl(itemBeans.get(0).radio_logo, IMAGE_QUALITY_SIZE))
                            .into(vh.iv_radio1);
                    vh.tv_radio1_name.setText(itemBeans.get(0).radio_name);
                    vh.tv_radio1_count.setText(itemBeans.get(0).play_count);
                    vh.layout1.setTag(itemBeans.get(0));
                    vh.layout1.setOnClickListener(onClickListener);
                }
                if (itemBeans.size() > 1) {
                    vh.layout2.setVisibility(View.VISIBLE);
                    Glide.with(getContext())
                            .load(ImageUtil.transferImgUrl(itemBeans.get(1).radio_logo, IMAGE_QUALITY_SIZE))
                            .into(vh.iv_radio2);
                    vh.tv_radio2_name.setText(itemBeans.get(1).radio_name);
                    vh.tv_radio2_count.setText(itemBeans.get(1).play_count);
                    vh.layout2.setTag(itemBeans.get(1));
                    vh.layout2.setOnClickListener(onClickListener);
                }
                if (itemBeans.size() > 2) {
                    vh.layout3.setVisibility(View.VISIBLE);
                    Glide.with(getContext())
                            .load(ImageUtil.transferImgUrl(itemBeans.get(2).radio_logo, IMAGE_QUALITY_SIZE))
                            .into(vh.iv_radio3);
                    vh.tv_radio3_name.setText(itemBeans.get(2).radio_name);
                    vh.tv_radio3_count.setText(itemBeans.get(2).play_count);
                    vh.layout3.setTag(itemBeans.get(2));
                    vh.layout3.setOnClickListener(onClickListener);
                }

            }
            return convertView;
        }
    }

    LiveMusicRadioDetailTask liveMusicRadioDetailTask;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            if (isAdded() && !getActivity().isFinishing()) {
//                final MobileNetworkDialog dialog = MobileNetworkDialog.getInstance(getActivity());
//                if (null != dialog && dialog.showWrapper()) {
//                    return;
//                }
//            }
            RadioBean radioBean = (RadioBean) v.getTag();
            liveMusicRadioDetailTask = new LiveMusicRadioDetailTask(getActivity(),
                    new ILoadData() {
                        @Override
                        public void onLoadSuccess(int dataType, List datas) {
                            final List<SongDetailBean> songs = datas;

                            if (songs != null && songs.size() > 0) {
                                if (isAdded()) {
                                    AuditionAlertDialog.getInstance(getActivity()).showWrapper(false, new AuditionAlertDialog.OnSelectedListener() {
                                        @Override
                                        public void onPlay() {
                                            new LiveMusicPlayTask(getActivity()).playNow(songs, 0, true);
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onLoadFail(int dataType, String message) {
                            LogUtil.i(TAG, "--load--fail--" + message);
                        }
                    }
                    , radioBean.radio_id, radioBean.category_type);
            liveMusicRadioDetailTask.executeMultiTask();
        }
    };


    static class ViewHolder {
        private View layout1, layout2, layout3;
        private ImageView iv_radio1, iv_radio2, iv_radio3;
        private TextView tv_radio1_name, tv_radio2_name, tv_radio3_name;
        private TextView tv_radio1_count, tv_radio2_count, tv_radio3_count;

        public ViewHolder(View view) {
            layout1 = view.findViewById(R.id.layout_1);
            layout2 = view.findViewById(R.id.layout_2);
            layout3 = view.findViewById(R.id.layout_3);


            iv_radio1 = (ImageView) view.findViewById(R.id.iv_radio1);
            iv_radio2 = (ImageView) view.findViewById(R.id.iv_radio2);
            iv_radio3 = (ImageView) view.findViewById(R.id.iv_radio3);

            tv_radio1_name = (TextView) view.findViewById(R.id.tv_radio1_name);
            tv_radio2_name = (TextView) view.findViewById(R.id.tv_radio2_name);
            tv_radio3_name = (TextView) view.findViewById(R.id.tv_radio3_name);

            tv_radio1_count = (TextView) view.findViewById(R.id.tv_radio1_count);
            tv_radio2_count = (TextView) view.findViewById(R.id.tv_radio2_count);
            tv_radio3_count = (TextView) view.findViewById(R.id.tv_radio3_count);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelTask(liveMusicRadioDetailTask);
    }
}
