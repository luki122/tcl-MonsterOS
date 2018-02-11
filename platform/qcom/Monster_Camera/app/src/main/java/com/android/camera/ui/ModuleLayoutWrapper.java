package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.android.camera.app.AppController;

/**
 * Created by sichao.hu on 10/27/15.
 */
public class ModuleLayoutWrapper extends FrameLayout {

    public ModuleLayoutWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ModuleLayoutWrapper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ModuleLayoutWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public static interface OnAllViewRemovedListener{
        public void onAllViewRemoved(AppController controller);
    }

    private OnAllViewRemovedListener mListener;
    private AppController mAppController;

    public void setOnAllViewRemovedListener(OnAllViewRemovedListener listener,AppController controllerToBeReturned){
        mListener=listener;
        mAppController=controllerToBeReturned;
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
        if(mListener!=null){
            mListener.onAllViewRemoved(mAppController);
        }
    }
}
