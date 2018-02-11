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
import cn.tcl.music.model.ClosedTimeBean;

public class ClosedTimeAdapter extends BaseAdapter {
    private static final String TAG = ClosedTimeAdapter.class.getSimpleName();
    private List<ClosedTimeBean> mClosedTimeList;
    private Context mContext;
    private LayoutInflater mInflater;

    public ClosedTimeAdapter(Context context, List<ClosedTimeBean> list) {
        this.mContext = context;
        this.mClosedTimeList = list;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mClosedTimeList.size();
    }

    @Override
    public Object getItem(int position) {
        return mClosedTimeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.dialog_time_closed_item, null);
            holder.mRightImage = (ImageView) convertView.findViewById(R.id.dialog_item_right_image);
            holder.mTimeText = (TextView) convertView.findViewById(R.id.dialog_item_time_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mTimeText.setText(mClosedTimeList.get(position).getTime());
        if (mClosedTimeList.get(position).isSelect()) {
            holder.mRightImage.setVisibility(View.VISIBLE);
        } else {
            holder.mRightImage.setVisibility(View.GONE);
        }
        return convertView;
    }

    static class ViewHolder {
        private ImageView mRightImage;
        private TextView mTimeText;
    }
}
