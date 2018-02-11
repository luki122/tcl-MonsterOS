package com.android.gallery3d.ui;

import com.android.gallery3d.R;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.PopupWindow;

public class SelectDeletePopupWindow implements OnClickListener {
    
    private View mParent;
    private PopupWindow mPopupWindow;
    private ImageButton mDeleteButton;
    
    private OnSelectDeleteClickedListener mOnSelectDeleteClickedListener;
    
    public SelectDeletePopupWindow(View parent) {
        Context context = parent.getContext();
        mParent = parent;
        mPopupWindow = new PopupWindow(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.select_delete_popup_window, null);
        mPopupWindow .setContentView(contentView);
        mPopupWindow.setAnimationStyle(R.style.SelectDeletePopupWindowAnim);
        mPopupWindow.setWidth(LayoutParams.MATCH_PARENT);
        mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(0x7F000000);  
        mPopupWindow.setBackgroundDrawable(dw);  
        mDeleteButton = (ImageButton)contentView.findViewById(R.id.select_delete_button);
        mDeleteButton.setOnClickListener(this);
    }
    
    public interface OnSelectDeleteClickedListener {
        public void onSelectDeleteClicked();
    }
    
    public void setOnSelectDeleteClickedListener(OnSelectDeleteClickedListener listener) {
        mOnSelectDeleteClickedListener = listener;
    }
    
    public void show() {
        mPopupWindow.showAtLocation(mParent, Gravity.BOTTOM, 0, 0);
    }
    
    public void dismiss() {
        if(mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.select_delete_button) {
            mOnSelectDeleteClickedListener.onSelectDeleteClicked();
        }
    }
}
