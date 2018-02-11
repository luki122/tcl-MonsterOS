package cn.tcl.music.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.view.FooterView;

/**
 * Created by dongdong.huang on 2015/11/11.
 */
public abstract class BaseRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    protected FooterViewHolder mFooterViewHolder;
    private LayoutInflater mInflater;
    private List mDatas;

    public BaseRecyclerAdapter(Context context, List list){
        mInflater = LayoutInflater.from(context);
        mDatas = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_FOOTER){
            View view = mInflater.inflate(R.layout.item_recycler_view_footer, null);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            mFooterViewHolder = new FooterViewHolder(view);
            return mFooterViewHolder;
        }
        else {
            return onItemHolderCreate();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int count = getItemCount();

        if(count > 1 && position < count - 1){
            onItemHolderBind(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        if(mDatas != null){
            return mDatas.size() + 1;
        }

        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        FooterView progressBar = null;
        public FooterViewHolder(View view) {
            super(view);
            progressBar = (FooterView)view.findViewById(R.id.progress_footer);

        }

    }

    public void hideLoading(){
        if(mFooterViewHolder != null){
            mFooterViewHolder.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void showLoading(){
        if(mFooterViewHolder != null){
            mFooterViewHolder.progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * viewHolder的创建
     */
    public abstract RecyclerView.ViewHolder onItemHolderCreate();

    /**
     * viewHolder的绑定
     */
    public abstract void onItemHolderBind(RecyclerView.ViewHolder holder, int position);
}
