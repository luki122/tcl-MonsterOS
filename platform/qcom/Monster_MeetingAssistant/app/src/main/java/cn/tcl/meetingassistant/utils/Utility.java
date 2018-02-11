/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ts on 9/1/16.
 */
public class Utility {

    private static int getIntFromDimens(Context context,int index) {
        int result = 0;

        if(index != 0) {
            result = context.getResources().getDimensionPixelSize(index);
        }
        return result;
    }

    public static List<String> parseStringByNewLine(String content) {
        if(TextUtils.isEmpty(content))
            return null;

        String[] strings = content.split("\\n");
        List<String> stringList= new ArrayList<>(strings.length);
        for(int i=0;i<strings.length;i++){
            stringList.add(strings[i]);
        }

        return stringList;
    }

    public static LinearLayout.LayoutParams creatLayoutParams(Context context,
                                                        int marginLeftDimenId,
                                                        int marginTopDimenId,
                                                        int marginRightDimenId,
                                                        int marginBottomDimenId){
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(getIntFromDimens(context, marginLeftDimenId),
                getIntFromDimens(context, marginTopDimenId),
                getIntFromDimens(context, marginRightDimenId),
                getIntFromDimens(context, marginBottomDimenId));

        return layoutParams;
    }

}
