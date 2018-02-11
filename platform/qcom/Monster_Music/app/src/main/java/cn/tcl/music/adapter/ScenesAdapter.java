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
import cn.tcl.music.model.ScenesBean;

public class ScenesAdapter extends BaseAdapter {

    private static final String TAG = ScenesAdapter.class.getSimpleName();
    private List<ScenesBean> mList;
    private Context mContext;
    private LayoutInflater mInflater;

    public ScenesAdapter(Context context, List<ScenesBean> list) {
        this.mContext = context;
        this.mList = list;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
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
            convertView = mInflater.inflate(R.layout.item_local_scenes, null);
            holder.mImageView = (ImageView) convertView.findViewById(R.id.scenes_img);
            holder.mTextView = (TextView) convertView.findViewById(R.id.scenes_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ScenesBean scenesBean = mList.get(position);
        holder.mImageView.setImageResource(mList.get(position).getScenesIcon());
        holder.mTextView.setText(scenesBean.getScenesText());
        return convertView;
    }

    static class ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;
    }

}
