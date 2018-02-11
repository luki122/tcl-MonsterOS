package cn.tcl.transfer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import cn.tcl.transfer.R;
import cn.tcl.transfer.util.ReceiverItem;

public class ReceiverAdapter extends BaseAdapter{
    private Context mContext;
    private List<ReceiverItem> mList;


    public ReceiverAdapter(Context context,List<ReceiverItem> list) {
        mContext = context;
        mList= list;
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mContext).inflate(R.layout.layout_receiver_item,null);
        TextView type_text = (TextView)view.findViewById(R.id.type_name);
        TextView status_text = (TextView)view.findViewById(R.id.receive_status);
        ImageView mark = (ImageView)view.findViewById(R.id.finish_mark);
        ImageView icon = (ImageView)view.findViewById(R.id.icon);
        ProgressBar progress = (ProgressBar)view.findViewById(R.id.progress);
        String[] textlist= mContext.getResources().getStringArray(R.array.receiver_content_item);
        type_text.setText(textlist[mList.get(i).getType()]);
        progress.setProgress(mList.get(i).getProgress());
        status_text.setText(convertFileSize(mList.get(i).getSize()));
        setIconByType(icon,mList.get(i).getType());
        return view;
    }

    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    private void setIconByType(ImageView view,int type) {
        switch(type) {
            case ReceiverItem.TYPE_SYSTEM:
                view.setImageResource(R.drawable.system_icon);
                break;
            case ReceiverItem.TYPE_APP:
                view.setImageResource(R.drawable.app_icon);
                break;
            case ReceiverItem.TYPE_PICTURE:
                view.setImageResource(R.drawable.picture_icon);
                break;
            case ReceiverItem.TYPE_AUDIO:
                view.setImageResource(R.drawable.music_icon);
                break;
            case ReceiverItem.TYPE_VIDEO:
                view.setImageResource(R.drawable.video_icon);
                break;
            case ReceiverItem.TYPE_DOCUMENT:
                view.setImageResource(R.drawable.doc_icon);
                break;
        }

    }
}