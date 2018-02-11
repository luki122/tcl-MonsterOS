/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView; // MODIFIED by fei.hui, 2016-09-29,BUG-2994050

import com.tct.camera.R;

/**
 * Created by administrator on 3/25/16.
 */
public class GridAdapter extends BaseAdapter {
    private static final int POSE_MAX_NUMBER = 8;
    private TypedArray mDrawableIds;
    private LayoutInflater mInflater;
    private Context mContext;

    public GridAdapter(Context context, TypedArray typedArray) {
        mDrawableIds = typedArray;
        mInflater = LayoutInflater.from(context);
        mContext = context;

    }

    public int getCount() {
        return mDrawableIds.length();
    }

    public Object getItem(int position) {
        return mDrawableIds.getResourceId(position, 0);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ItemViewTag viewTag;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pose_item_layout, null);

            /* MODIFIED-BEGIN by fei.hui, 2016-09-29,BUG-2994050*/
            viewTag = new ItemViewTag((ImageView) convertView.findViewById(R.id.pose_item_image),
                    (TextView)convertView.findViewById(R.id.pose_item_text));
            convertView.setTag(viewTag);
        } else {
            viewTag = (ItemViewTag) convertView.getTag();
        }

        int viewposition = position+1;
        viewTag.mTextView.setText(mContext.getString(R.string.mode_pose)+" "+viewposition);
        viewTag.mIcon.setImageResource(mDrawableIds.getResourceId(position, 0));
        if(position == POSE_MAX_NUMBER){
            viewTag.mTextView.setText(mContext.getString(R.string.pose_final_icon));
        }
        return convertView;
    }

    class ItemViewTag {
        protected ImageView mIcon;
        protected TextView mTextView;

        public ItemViewTag(ImageView icon,TextView textView) {
            this.mIcon = icon;
            this.mTextView = textView;
            /* MODIFIED-END by fei.hui,BUG-2994050*/
        }
    }
}
