package cn.tcl.music.adapter.holders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.PlaylistInfo;

/**
 * Created by han.lou on 16-9-29.
 */
public class PlayListAdapter extends BaseAdapter {

    private Context mContext;
    private List<PlaylistInfo> mPlayList;

    public PlayListAdapter(Context context, List<PlaylistInfo> playlist) {
        mContext = context;
        mPlayList = playlist;
    }

    @Override
    public int getCount() {
        return mPlayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mPlayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlayListHolder holder;
        if(convertView == null) {
            holder = new PlayListHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.play_list_item, parent, false);
            holder.mPlayListNameTextView = (TextView) convertView.findViewById(R.id.title_text_view);
            convertView.setTag(holder);
        } else {
            holder = (PlayListHolder) convertView.getTag();
        }
        holder.mPlayListNameTextView.setText(mPlayList.get(position).getName());
        return convertView;
    }

    private final class PlayListHolder {
        TextView mPlayListNameTextView;
    }
}
