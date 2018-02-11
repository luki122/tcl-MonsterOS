package cn.tcl.music.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.activities.SearchActivity;

/**
 * Created by daisy on 16-10-18.
 */
public class SearchHistoryAdapter extends BaseAdapter {

    private final int VIEW_TYPE = 2;
    private final int TYPE_SEARCH_HISTORY = 0;
    private final int TYPE_SEARCH_CLEAN = 1;

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<String> mHistoryList;

    public SearchHistoryAdapter(Context context, ArrayList<String> arrayList) {
        mHistoryList = arrayList;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mHistoryList.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position == mHistoryList.size()) {
            position = mHistoryList.size() - 1;
        }
        return mHistoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mHistoryList.size()) {
            return TYPE_SEARCH_HISTORY;
        } else {
            return TYPE_SEARCH_CLEAN;
        }
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);

        SearchHistoryViewHolder searchHistoryViewHolder = null;
        SearchCleanViewHolder searchCleanViewHolder = null;

        if (convertView != null) {
            if ((type == TYPE_SEARCH_HISTORY && !(convertView.getTag() instanceof SearchHistoryViewHolder)) ||
                    (type == TYPE_SEARCH_CLEAN && !(convertView.getTag() instanceof SearchCleanViewHolder))) {
                convertView = null;
            }
        }
        if (convertView == null) {
            mInflater = LayoutInflater.from(mContext);
            switch (type) {
                case TYPE_SEARCH_HISTORY:
                    convertView = mInflater.inflate(R.layout.item_search_history, parent, false);
                    searchHistoryViewHolder = new SearchHistoryViewHolder();
                    searchHistoryViewHolder.searchHistoryTv = (TextView) convertView.findViewById(R.id.tv_search_history);
                    convertView.setTag(searchHistoryViewHolder);
                    break;
                case TYPE_SEARCH_CLEAN:
                    convertView = mInflater.inflate(R.layout.item_clean_history, parent, false);
                    searchCleanViewHolder = new SearchCleanViewHolder();
                    searchCleanViewHolder.searchCleanBt = (Button) convertView.findViewById(R.id.search_history_clean);
                    searchCleanViewHolder.searchCleanBt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mHistoryList.clear();
                            notifyDataSetChanged();
                            ((SearchActivity)mContext).clearSearchHistory();
                        }
                    });
                    convertView.setTag(searchCleanViewHolder);
                    break;
                default:
                    break;
            }
        } else {
            switch (type) {
                case TYPE_SEARCH_HISTORY:
                    searchHistoryViewHolder = (SearchHistoryViewHolder) convertView.getTag();
                    break;
                case TYPE_SEARCH_CLEAN:
                    searchCleanViewHolder = (SearchCleanViewHolder) convertView.getTag();
                    break;
            }
        }

        switch (type) {
            case TYPE_SEARCH_HISTORY:
                searchHistoryViewHolder.searchHistoryTv.setText(mHistoryList.get(position));
                break;
            case TYPE_SEARCH_CLEAN:
                break;
        }
        return convertView;
    }

    public class SearchHistoryViewHolder {
        TextView searchHistoryTv;
    }

    public class SearchCleanViewHolder {
        Button searchCleanBt;
    }
}
