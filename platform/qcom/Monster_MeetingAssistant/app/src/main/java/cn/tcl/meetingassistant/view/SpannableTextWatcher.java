/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.nfc.Tag;
import android.text.Editable;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BulletSpan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.DensityUtil;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-10.
 * SpannableTextWatcher
 */
public class SpannableTextWatcher implements TextWatcher {
    private final String TAG = SpannableTextWatcher.class.getSimpleName();
    // dp unit
    private final int DEFAULT_GAP = 14;
    // dp unit
    private final int DEFAULT_SIZE = 4;
    private Context mContext;
    private List<BlueBulletSpan> list;
    private int mGap = DEFAULT_GAP;
    private int mSize = DEFAULT_SIZE;

    public SpannableTextWatcher(Context context) {
        this.mContext = context;
        list = new LinkedList<>();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int count, int after) {

    }

    public SpannableTextWatcher setGap(int gap){
        mGap = gap;
        return this;
    }

    public SpannableTextWatcher setSize(int size){
        mSize = size;
        return this;
    }

    @Override
    public void afterTextChanged(Editable editable) {
        String s = editable.toString();
        String[] as = s.split("\\n");
        for(BlueBulletSpan span : list){
            editable.removeSpan(span);
        }
        list.clear();
        BulletSpan[] spans = editable.getSpans(0, editable.length(), BulletSpan.class);
        for (BulletSpan iconMarginSpan : spans) {
            editable.removeSpan(iconMarginSpan);
        }


        int index = 0;
        int color = mContext.getResources().getColor(R.color.bullet_color,null);
        if(TextUtils.isEmpty(s)){
            return;
        }
        for (String s1 : as) {
            BlueBulletSpan span = new BlueBulletSpan(mContext,DensityUtil.dip2px(mContext,mGap),color,mSize);
            list.add(span);
            editable.setSpan(span, index, s1.length() + index,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            index = s1.length() + index + 1;
        }

    }

}
