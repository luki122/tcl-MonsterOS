package com.monster.launchericon.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.UserHandle;

/**
 * Created by antino on 16-11-9.
 */
public class Utilites {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isOtherUser(UserHandle user){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1){
            if(user==null||android.os.Process.myUserHandle().equals(user)){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }
}
