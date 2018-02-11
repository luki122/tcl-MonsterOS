/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import cn.tcl.meetingassistant.R;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * The import list title
 */
public class ImportPointListTitleView extends RelativeLayout{

    private View mAddBtn;

    public ImportPointListTitleView(Context context) {
        this(context, null);
    }

    public ImportPointListTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater layoutInflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.import_point_list_title,this,true);
        mAddBtn = view.findViewById(R.id.import_point_add_btn);
    }

    public ImportPointListTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public ImportPointListTitleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs);
    }

    public void setAddBtnClickListener(OnClickListener onClickListener){
        mAddBtn.setOnClickListener(onClickListener);
    }


}
