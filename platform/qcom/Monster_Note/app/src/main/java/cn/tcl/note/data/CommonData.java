/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.data;

import android.support.v7.widget.RecyclerView;

public abstract class CommonData {
    private RecyclerView.ViewHolder mViewHolder;

    public RecyclerView.ViewHolder getViewHolder() {
        return mViewHolder;
    }

    public void setViewHolder(RecyclerView.ViewHolder mViewHolder) {
        this.mViewHolder = mViewHolder;
    }

    public abstract String toXmlString();
}
