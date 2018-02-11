package cn.tcl.music.adapter.live;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.SearchActivity;
import cn.tcl.music.model.live.LiveMusicAutoTipsBean;

public class AutoTipsAdapter extends BaseAdapter {

    private static final String TAG = AutoTipsAdapter.class.getSimpleName();
    private Context mContext;
    private LayoutInflater mInflater;
    private List<LiveMusicAutoTipsBean> mAutoTipsList = new ArrayList<>();

    public AutoTipsAdapter(Context context) {
        mContext = context;
    }

    public void setmAutoTipsList(List<LiveMusicAutoTipsBean> mAutoTipsList) {
        this.mAutoTipsList.clear();
        this.mAutoTipsList.addAll(mAutoTipsList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AutoTipsViewHolder autoTipsViewHolder = null;

        if (convertView == null) {
            mInflater = LayoutInflater.from(mContext);
            convertView = mInflater.inflate(R.layout.item_auto_tips, parent, false);
            autoTipsViewHolder = new AutoTipsViewHolder();
            autoTipsViewHolder.mAutoTipsType = (TextView) convertView.findViewById(R.id.auto_tips_type);
            autoTipsViewHolder.mAtuoTipsContent = (TextView) convertView.findViewById(R.id.auto_tips_content);
            autoTipsViewHolder.mAutoTipsImage = (ImageView) convertView.findViewById(R.id.auto_tips_image);
            convertView.setTag(autoTipsViewHolder);
        } else {
            autoTipsViewHolder = (AutoTipsViewHolder) convertView.getTag();
        }

        autoTipsViewHolder.mAutoTipsType.setText(mAutoTipsList.get(position).type);
        autoTipsViewHolder.mAtuoTipsContent.setText(mAutoTipsList.get(position).tip);
        if (!mAutoTipsList.get(position).type.equals(SearchActivity.AUTO_TIPS_SONG)) {
            autoTipsViewHolder.mAutoTipsImage.setImageResource(R.drawable.auto_tips_others);
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return mAutoTipsList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAutoTipsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class AutoTipsViewHolder {
        private TextView mAutoTipsType;
        private TextView mAtuoTipsContent;
        private ImageView mAutoTipsImage;
    }
}
