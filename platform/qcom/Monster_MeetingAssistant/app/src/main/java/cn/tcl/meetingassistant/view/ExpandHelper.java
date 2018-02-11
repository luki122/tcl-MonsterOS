/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import java.util.HashMap;

/**
 * record the expand status for edit important point page
 */
public class ExpandHelper {

    HashMap<Object,Boolean> mExpandStatus = new HashMap<>();

    public void expand(Object object){
        mExpandStatus.put(object,true);
    }

    public void collapse(Object object){
        if(mExpandStatus.containsKey(object)){
            mExpandStatus.remove(object);
        }
    }

    public boolean isExpand(Object object){
        boolean result = mExpandStatus.containsKey(object) && mExpandStatus.get(object);
        return result;
    }
}
