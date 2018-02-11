package cn.tcl.music.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.adapter.QualityAdapter;
import cn.tcl.music.model.QualityBean;
import cn.tcl.music.util.PreferenceUtil;

public class QualityFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = QualityFragment.class.getSimpleName();
    private static final String HEADER_EMPTY_STRING = "";
    private List<QualityBean> mQualityBeanList;
    private QualityAdapter mQualityAdapter;

    public enum QualityItem {
        AUDITION_QUALITY,
        AUDITION_AUTO,
        AUDITION_STANDARD,
        AUDITION_HIGH,
        AUDITION_LOSSLESS,
        DOWNLOAD_QUALITY,
        DOWNLOAD_STANDARD,
        DOWNLOAD_HIGH,
        DOWNLOAD_LOSSLESS
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audition_and_download_quality, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        ListView listView = (ListView) rootView.findViewById(R.id.quantify_list_view);
        mQualityBeanList = new ArrayList<QualityBean>();
        initQualityBeanList();
        mQualityAdapter = new QualityAdapter(getActivity(), mQualityBeanList);
        listView.setAdapter(mQualityAdapter);
        listView.setOnItemClickListener(this);
    }

    private void initQualityBeanList() {
        String[] titleText = new String[]{getResources().getString(R.string.audition_quality),
                getResources().getString(R.string.auto_recommend), getResources().getString(R.string.standard_download),
                getResources().getString(R.string.audition_high_quality), getResources().getString(R.string.lossless),
                getResources().getString(R.string.download_quality), getResources().getString(R.string.standard_download),
                getResources().getString(R.string.audition_high_quality), getResources().getString(R.string.lossless)};

        String[] subTitleText = new String[]{HEADER_EMPTY_STRING,
                getResources().getString(R.string.auto_sub_text), getResources().getString(R.string.audition_kbps, 128),
                getResources().getString(R.string.audition_kbps, 320), getResources().getString(R.string.audition_kbps, 780),
                HEADER_EMPTY_STRING, getResources().getString(R.string.download_mb, 4), getResources().getString(R.string.download_mb, 10),
                getResources().getString(R.string.download_mb, 30)};
        for (int i = 0; i < titleText.length; i++) {
            QualityBean bean = new QualityBean();
            bean.setItemTitle(titleText[i]);
            bean.setItemSubTitle(subTitleText[i]);
            if (i == QualityItem.AUDITION_HIGH.ordinal() || i == QualityItem.AUDITION_LOSSLESS.ordinal() ||
                    i == QualityItem.DOWNLOAD_HIGH.ordinal() || i == QualityItem.DOWNLOAD_LOSSLESS.ordinal()) {
                bean.setSVIPItem(true);
            } else {
                bean.setSVIPItem(false);
            }
            if (i > QualityItem.AUDITION_QUALITY.ordinal() && i < QualityItem.DOWNLOAD_QUALITY.ordinal()) {
                bean.setSelectedItem(PreferenceUtil.getValue(getActivity(), PreferenceUtil.NODE_AUDITION_QUALITY,
                        PreferenceUtil.KEY_AUDITION_QUALITY, QualityItem.AUDITION_AUTO.ordinal()) == i);
            } else if (i > QualityItem.DOWNLOAD_QUALITY.ordinal()) {
                bean.setSelectedItem(PreferenceUtil.getValue(getActivity(), PreferenceUtil.NODE_DOWNLOAD_QUALITY,
                        PreferenceUtil.KEY_DOWNLOAD_QUALITY, QualityItem.DOWNLOAD_STANDARD.ordinal()) == i);
            }
            mQualityBeanList.add(bean);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (position == QualityItem.AUDITION_QUALITY.ordinal() || position == QualityItem.DOWNLOAD_QUALITY.ordinal()) {
            return;
        }
        if (position < QualityItem.DOWNLOAD_QUALITY.ordinal()) {
            PreferenceUtil.saveValue(getActivity(), PreferenceUtil.NODE_AUDITION_QUALITY, PreferenceUtil.KEY_AUDITION_QUALITY, position);
            for (int i = QualityItem.AUDITION_AUTO.ordinal(); i < QualityItem.DOWNLOAD_QUALITY.ordinal(); i++) {
                QualityBean bean = mQualityBeanList.get(i);
                if (i == position) {
                    bean.setSelectedItem(true);
                } else {
                    bean.setSelectedItem(false);
                }
            }
        } else if (position > QualityItem.DOWNLOAD_QUALITY.ordinal()) {
            PreferenceUtil.saveValue(getActivity(), PreferenceUtil.NODE_DOWNLOAD_QUALITY, PreferenceUtil.KEY_DOWNLOAD_QUALITY, position);
            for (int i = QualityItem.DOWNLOAD_STANDARD.ordinal(); i < mQualityBeanList.size(); i++) {
                QualityBean bean = mQualityBeanList.get(i);
                if (i == position) {
                    bean.setSelectedItem(true);
                } else {
                    bean.setSelectedItem(false);
                }
            }
        }
        mQualityAdapter.notifyDataSetChanged();
    }
}
