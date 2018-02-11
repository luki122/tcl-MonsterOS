package cn.tcl.music.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.model.QualityBean;

public class QualityAdapter extends BaseAdapter {
    private static final String TAG = QualityAdapter.class.getSimpleName();
    private static final int AUDITION_QUALITY_HEADER = 0;
    private static final int DOWNLOAD_QUALITY_HEADER = 5;
    private List<QualityBean> mQualityList;
    private Context mContext;
    private LayoutInflater mInflater;

    public QualityAdapter(Context context, List<QualityBean> list) {
        this.mContext = context;
        this.mQualityList = list;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mQualityList.size();
    }

    @Override
    public Object getItem(int position) {
        return mQualityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        if (CommonConstants.ITEM_TYPE_HEADER == getItemViewType(position)) {
            View view = mInflater.inflate(R.layout.item_quality_header, null);
            holder.mHeaderTitleText = (TextView) view.findViewById(R.id.quality_header_title);
            holder.mHeaderTitleText.setText(mQualityList.get(position).getItemTitle());
            return view;
        } else {
            View view = mInflater.inflate(R.layout.item_quality_content, null);
            holder.mRightImage = (ImageView) view.findViewById(R.id.item_right_image);
            holder.mSVIPImage = (ImageView) view.findViewById(R.id.svip_pic);
            holder.mTitleText = (TextView) view.findViewById(R.id.quantify_title);
            holder.mSubTitleText = (TextView) view.findViewById(R.id.quality_sub_title);
            holder.mTitleText.setText(mQualityList.get(position).getItemTitle());
            holder.mSubTitleText.setText(mQualityList.get(position).getItemSubTitle());
            if (mQualityList.get(position).ismSVIPItem()) {
                holder.mSVIPImage.setVisibility(View.VISIBLE);
            }
            if (mQualityList.get(position).isSelectedItem()) {
                holder.mRightImage.setVisibility(View.VISIBLE);
            } else {
                holder.mRightImage.setVisibility(View.GONE);
            }
            return view;
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == AUDITION_QUALITY_HEADER || position == DOWNLOAD_QUALITY_HEADER) {
            return CommonConstants.ITEM_TYPE_HEADER;
        } else {
            return CommonConstants.ITEM_TYPE_CONTENT;
        }
    }

    private static class ViewHolder {
        private ImageView mRightImage;
        private ImageView mSVIPImage;
        private TextView mTitleText;
        private TextView mSubTitleText;
        private TextView mHeaderTitleText;
    }
}
