package com.monster.paymentsecurity.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.monster.paymentsecurity.adapter.ListAppRiskAdapter;

import mst.widget.LinearLayout;

/**
 * Created by logic on 16-12-6.
 */
public class AppRiskList extends LinearLayout {

    private View.OnClickListener mListener;

    public AppRiskList(Context context) {
        super(context);
    }

    public AppRiskList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppRiskList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AppRiskList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setAdapter(ListAppRiskAdapter list) {
        setOrientation(VERTICAL);

        //Popolute list
        if (list != null) {
            for (int i = 0; i < list.getCount(); i++) {
                View view = list.getView(i, null, this);
                this.addView(view);
            }
        }
    }

}
