package com.android.deskclock;

import android.content.Context;
import android.preference.SeekBarVolumizer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;


import mst.preference.Preference;

public class TCLSettingSeekBarPreference extends Preference{

    
    private SeekBar my_seekbar;
    
    private SeekBarVolumizer  m_sbv;
    
    public TCLSettingSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        setLayoutResource(R.layout.tcl_seekbar_preference);
    }
    
    
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        my_seekbar = (SeekBar)view.findViewById(R.id.my_seekbar);
        if(m_sbv !=null){
            m_sbv.setSeekBar(my_seekbar);
        }
    }
    
    
    public void setSeekBarVolumizer(SeekBarVolumizer sbv){
        m_sbv = sbv;
        notifyChanged();
    }


}
