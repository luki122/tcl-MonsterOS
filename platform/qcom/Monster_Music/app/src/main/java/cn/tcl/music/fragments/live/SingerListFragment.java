package cn.tcl.music.fragments.live;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tcl.framework.log.NLog;
import com.xiami.sdk.utils.ImageUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.SingerDetailActivity;
import cn.tcl.music.adapter.ListBaseAdapter;
import cn.tcl.music.model.live.ArtistBean;
import cn.tcl.music.model.live.LiveMusicSinger;
import cn.tcl.music.network.LiveMusicSingerTask;
import cn.tcl.music.util.CharacterParser;
import cn.tcl.music.util.PinyinComparator;
import cn.tcl.music.view.EmptyLayoutV2;

public class SingerListFragment extends BaseListFragment<ArtistBean> implements AdapterView.OnItemClickListener {
    private static final String TAG = SingerListFragment.class.getSimpleName();
    private ATabTitlePagerFragment.TabTitlePagerBean titlePagerBean;
    private String lastFilterType = SingerPageFragment.DEFAULT_FILTER_TYPE;  //当前Fragment最后选择的过滤条件
    private CharacterParser characterParser;
    private PinyinComparator pinyinComparator;

    public static SingerListFragment newInstance(ATabTitlePagerFragment.TabTitlePagerBean bean) {
        SingerListFragment fragment = new SingerListFragment();
        Bundle args = new Bundle();
        args.putSerializable("titlePagerBean", bean);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int inflateContentView() {
        return R.layout.fragment_singer_list_pull_refresh_listview;
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
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        super.layoutInit(inflater, savedInstanceSate);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("titlePagerBean", titlePagerBean);
    }

    @Override
    protected void sendRequestData() {
        String type = getSingerSearchType(lastFilterType);
        //虾米sdk获取数据 从第1页开始
        if (mCurrentPage == 0) {
            mCurrentPage = 1;
        }
        NLog.d(TAG, "sendRequestData type = " + type + ", mCurrentPage = " + mCurrentPage);
        if (getActivity() != null) {
            new LiveMusicSingerTask(getActivity(), this, type, String.valueOf(mCurrentPage)).executeMultiTask();
        }
    }

    public String getLastFilterType() {
        return lastFilterType;
    }

    /**
     * @param gender 歌手性别类型   男歌手 女歌手 组合
     */
    public void refreshData(String gender) {
        if (lastFilterType.equals(gender)) {
            NLog.d(TAG, "refreshData the same type  return ");
            return;
        } else {
            //type不同 先清除掉之前的数据
            if (mAdapter == null) {//[BUG-FIX] 2015-12-3
                mAdapter = getListAdapter();
            }
            mAdapter.clear();
        }
        lastFilterType = gender;
        NLog.d(TAG, "refreshData gender  = " + gender);
        if (mErrorLayout != null) {
            mErrorLayout.setErrorType(EmptyLayoutV2.NETWORK_LOADING);
        }
        sendRequestData();
    }

    /**
     * 根据过滤条件获得相应的搜索的type
     *
     * @param gender
     * @return
     */
    private String getSingerSearchType(String gender) {
        String result = "";
        //华语
        if (titlePagerBean != null) {
            if ("1".equals(titlePagerBean.getType())) {
                result = "chinese_M";
                if (SingerPageFragment.FITER_TYPE.M.name().equals(gender)) {
                    result = "chinese_M";
                } else if (SingerPageFragment.FITER_TYPE.F.name().equals(gender)) {
                    result = "chinese_F";
                } else if (SingerPageFragment.FITER_TYPE.B.name().equals(gender)) {
                    result = "chinese_B";
                }
            }
            //欧美
            else if ("2".equals(titlePagerBean.getType())) {
                result = "english_M";
                if (SingerPageFragment.FITER_TYPE.M.name().equals(gender)) {
                    result = "english_M";
                } else if (SingerPageFragment.FITER_TYPE.F.name().equals(gender)) {
                    result = "english_F";
                } else if (SingerPageFragment.FITER_TYPE.B.name().equals(gender)) {
                    result = "english_B";
                }
            }
            //日本
            else if ("3".equals(titlePagerBean.getType())) {
                result = "japanese_M";
                if (SingerPageFragment.FITER_TYPE.M.name().equals(gender)) {
                    result = "japanese_M";
                } else if (SingerPageFragment.FITER_TYPE.F.name().equals(gender)) {
                    result = "japanese_F";
                } else if (SingerPageFragment.FITER_TYPE.B.name().equals(gender)) {
                    result = "japanese_B";
                }
            }
            //韩国
            else if ("4".equals(titlePagerBean.getType())) {
                result = "korea_M";
                if (SingerPageFragment.FITER_TYPE.M.name().equals(gender)) {
                    result = "korea_M";
                } else if (SingerPageFragment.FITER_TYPE.F.name().equals(gender)) {
                    result = "korea_F";
                } else if (SingerPageFragment.FITER_TYPE.B.name().equals(gender)) {
                    result = "korea_B";
                }
            }
            //音乐人
            else if ("5".equals(titlePagerBean.getType())) {
                result = "musician";

            }
        }
        return result;
    }

    @Override
    protected ListBaseAdapter getListAdapter() {
        return new ArtistAdapter();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ArtistBean artistBean = mAdapter.getItem(position);
        if (artistBean == null) {
            return;
        }
//        if (isAdded() && !getActivity().isFinishing()) {
//            final MobileNetworkDialog dialog = MobileNetworkDialog.getInstance(getActivity());
//            if (null != dialog && dialog.showWrapper()) {
//                return;
//            }
//        }
        NLog.d(TAG, "onItemClick artistid = " + artistBean.artist_id + ", artistName = " + artistBean.artist_name);
        goToSingerActivity(artistBean);
    }

    private void goToSingerActivity(ArtistBean artistBean) {
        SingerDetailActivity.launch(getActivity(), artistBean.artist_id, artistBean.artist_name, artistBean.songs_count, artistBean.albums_count);
    }


    public class ArtistAdapter extends ListBaseAdapter<ArtistBean> {

        @SuppressLint("InflateParams")
        @Override
        protected View getRealView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null || convertView.getTag() == null) {
                convertView = getLayoutInflater(parent.getContext()).inflate(
                        R.layout.item_singler, null);
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            final ArtistBean data = mDatas.get(position);

            int section = getSectionForPosition(position);

            //不需要显示分组了
            vh.tv_singer_name.setText(data.artist_name);
            Glide.with(getContext())
                    .load(ImageUtil.transferImgUrl(data.artist_logo, 300))
                    .placeholder(R.drawable.default_singer_list)
                    .into(vh.iv_singer);

            //点击歌手列表后面的的箭头，跳转到相应的详情页
            vh.iv_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if (isAdded() && !getActivity().isFinishing()) {
//                        final MobileNetworkDialog dialog = MobileNetworkDialog.getInstance(getActivity());
//                        if (null != dialog && dialog.showWrapper()) {
//                            return;
//                        }
//                    }
                    SingerDetailActivity.launch(getActivity(), data.artist_id, data.artist_name, data.songs_count, data.albums_count);
                }
            });
            return convertView;
        }

        public int getSectionForPosition(int position) {
            return mDatas.get(position).sort_title.charAt(0);
        }

        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mDatas.get(i).sort_title;
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }

            return -1;
        }

    }

    static class ViewHolder {
        private ImageView iv_singer;
        private TextView tv_singer_name;
        private ImageView iv_more;
        private RelativeLayout title;
        private RelativeLayout content;
        private TextView sort_title;

        public ViewHolder(View view) {
            iv_singer = (ImageView) view.findViewById(R.id.iv_singer);
            tv_singer_name = (TextView) view.findViewById(R.id.tv_singer_name);
            iv_more = (ImageView) view.findViewById(R.id.iv_more);
            title = (RelativeLayout) view.findViewById(R.id.singer_sorttitle);
            content = (RelativeLayout) view.findViewById(R.id.singer_realtivelayout);
            sort_title = (TextView) view.findViewById(R.id.sort_title_tv);

        }
    }


    private boolean haveMoreData = true;

    /**
     * 用于下拉刷新时标志是否还有更多数据
     *
     * @param data
     * @return
     */
    @Override
    protected boolean haveNoMoreData(List<ArtistBean> data) {
        return !haveMoreData;
    }


    @Override
    public void onLoadSuccess(int dataType, List datas) {
        if (datas != null && datas.size() > 0) {
            LiveMusicSinger data = (LiveMusicSinger) datas.get(0);
            if (data != null && data.artists != null && data.artists.size() > 0) {
                NLog.e(TAG, " onLoadSuccess more = " + data.more + ", datas[0] = " + data.artists.get(0));
                haveMoreData = data.more;
                executeOnLoadDataSuccess(data.artists);
            } else {
                onLoadFail(-1, "");
            }
        }
    }

    @Override
    public void onLoadFail(int dataType, String message) {
        super.onLoadFail(dataType, message);
        NLog.e(TAG, " onLoadFail dataType = " + dataType + ", message = " + message);
        executeOnLoadDataError(null);

    }

    @Override
    protected int getPageSize() {
        return LiveMusicSingerTask.REQUEST_PAGE_SIZE;
    }

    /**
     * 过滤掉相同的数据
     *
     * @param datas
     * @param data
     * @return
     */
    @Override
    protected boolean filterSameData(List<? extends Serializable> datas, ArtistBean data) {
        int s = datas.size();
        if (data != null) {
            for (int i = 0; i < s; i++) {
                ArtistBean bean = (ArtistBean) datas.get(i);
                if (data.artist_id.equals(bean.artist_id)) {
                    return true;
                }
            }
        }
        return super.filterSameData(datas, data);
    }

    @Override
    protected void dealResult(ArrayList<ArtistBean> datas) {
        int s = datas.size();
        if (datas != null) {
            for (int i = 0; i < s; i++) {
                String pinyin = characterParser.getSelling(datas.get(i).artist_name);
                String sortString = pinyin.substring(0, 1).toUpperCase();
                // 正则表达式，判断首字母是否是英文字母
                if (sortString.matches("[A-Z]")) {
                    datas.get(i).sort_title = sortString.toUpperCase();
                } else {
                    datas.get(i).sort_title = "#";
                }
                NLog.d("SingerListFragment", "name = " + datas.get(i).artist_name + " ,title = " + datas.get(i).sort_title);
            }
        }
        // 根据a-z进行排序源数据
        mAdapter.setData(datas);
    }
}
