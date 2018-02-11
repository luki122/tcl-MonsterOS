package cn.tcl.music.adapter.holders;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import cn.tcl.music.R;
import cn.tcl.music.util.LogUtil;

public class ClickableViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static final String TAG = ClickableViewHolder.class.getSimpleName();

    public interface OnViewHolderClickListener{

        void onViewHolderClick(RecyclerView.ViewHolder vh, int position, View v);
    }


    public ClickableViewHolder(View itemView, OnViewHolderClickListener onViewHolderClickListener) {
        super(itemView);
        contextMenuImageButton = (ImageButton) itemView.findViewById(R.id.item_menu_image_button);
        mRecoverScanButton = (Button) itemView.findViewById(cn.tcl.music.R.id.recover_scan_button);
        if (contextMenuImageButton != null)
            contextMenuImageButton.setOnClickListener(this);
        if(mRecoverScanButton != null)
            mRecoverScanButton.setOnClickListener(this);
        mOnViewHolderClickListener = onViewHolderClickListener;
    }

    @Override
    public void onClick(View v) {
        if (mOnViewHolderClickListener != null)
        {
            LogUtil.d(TAG, "ClickableViewHolder onClick  mOnViewHolderClickListener.onViewHolderClick ");
            mOnViewHolderClickListener.onViewHolderClick(this, getPosition(), v);
        }
    }

    public ImageButton contextMenuImageButton;
    public Button mRecoverScanButton;
    OnViewHolderClickListener mOnViewHolderClickListener;
    //Task to add some parameters to this view Holder, if they are needed asynchronously
    public AsyncTask<?,?,?> currentTask;
}