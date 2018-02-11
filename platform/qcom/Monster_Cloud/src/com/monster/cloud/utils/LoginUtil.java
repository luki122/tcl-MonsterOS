package com.monster.cloud.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageParser;
import android.text.TextUtils;
import android.content.SharedPreferences;
import android.util.Log;

import com.monster.cloud.constants.Constant;
import com.monster.cloud.preferences.FilePreferences;
import com.monster.cloud.preferences.Preferences;
import com.tcl.account.sdkapi.QQLoginListener;
import com.tcl.account.sdkapi.UiAccountHelper;
import com.tcl.account.sdkapi.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yubai on 16-10-31.
 */
public class LoginUtil {

    static FilePreferences preferences;

    public static void getInstance(Context context) {
        if (preferences == null) {
            preferences = (FilePreferences) Preferences.Factory.getInstance(context, Constant.FILE_TYPE);
        }
    }

    public static void updateToken(Context context, String token) {
        if (preferences == null) {
            getInstance(context);
        }

        if (null != token) {
            preferences.setToken(token);
        }
    }

    public static void updateOpenId(Context context, String openId) {
        if (preferences == null) {
            getInstance(context);
        }

        if (null != openId) {
            preferences.setOpenId(openId);
        }
    }

    public static String getToken(Context context) {
        if (preferences == null) {
            getInstance(context);
        }
        return preferences.getToken();
    }

    public static String getOpenId(Context context) {
        if (preferences == null) {
            getInstance(context);
        }
        return preferences.getOpenId();
    }

    public static void updateLoginLabel(Context context, boolean isLogin) {
        if (preferences == null) {
            getInstance(context);
        }

        if (isLogin != false) {
            preferences.setLoginLabel(isLogin);
        }
    }

    public static boolean getLoginLabel(Context context) {
        if (preferences == null) {
            getInstance(context);
        }

        return preferences.getLoginLabel();
    }

    public static boolean isLogInTCLAccount(Context context){//TCL账号是否登陆
        User user = UiAccountHelper.getUserInfo(context);
        if (null == user || TextUtils.isEmpty(user.accountName)) {
            return false;
        }
        return true;
    }

    public static boolean isQQLogIn(Context context){//是否是用QQ登陆

        String openId = LoginUtil.getOpenId(context);
        String accessToken = LoginUtil.getToken(context);

        if (openId != null && accessToken != null){
            return true;
        }

        return false;
    }

    public static void saveTCLUserId(Context context){
        SharedPreferences mySharedPreferences = context.getSharedPreferences("TCLAccountName", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        User user = UiAccountHelper.getUserInfo(context);
        if(user!=null && !TextUtils.isEmpty(user.accountName)){
            editor.putString("TCLAccountName", user.accountName);
        }
        editor.commit();
    }

    public static String getTCLUsrID(Context context){
        SharedPreferences mySharedPreferences = context.getSharedPreferences("TCLAccountName", Activity.MODE_PRIVATE);
        return mySharedPreferences.getString("TCLAccountName","");
    }

}
