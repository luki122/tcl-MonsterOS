package com.monster.launcher.unread;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

import com.monster.launcher.compat.UserHandleCompat;

/**
 * Created by lj on 16-7-18.
 */
public class UnreadSupportShortcut {
    public ComponentName component;
    public int mShortcutType;
    public int mUnreadNum;
    public Bitmap icon;
    public UserHandle user;
    public boolean hadDraw = false;

    public UnreadSupportShortcut(String pkgName, String clsName, Bitmap icon, int type) {
        component = new ComponentName(pkgName, clsName);
        mShortcutType = type;
        mUnreadNum = 0;
        this.icon = icon;
        user = UserHandleCompat.myUserHandle().getUser();
    }
    public UnreadSupportShortcut(String pkgName, String clsName, Bitmap icon, int type,UserHandle us) {
        component = new ComponentName(pkgName, clsName);
        mShortcutType = type;
        mUnreadNum = 0;
        this.icon = icon;
        user = us;
    }
    public UnreadSupportShortcut(ComponentName cpName, Bitmap icon, int type) {
        component = cpName;
        mShortcutType = type;
        mUnreadNum = 0;
        this.icon = icon;
        user = UserHandleCompat.myUserHandle().getUser();
    }

    public UnreadSupportShortcut(ComponentName cpName, Bitmap icon, int type,UserHandle us) {
        component = cpName;
        mShortcutType = type;
        mUnreadNum = 0;
        this.icon = icon;
        user = us;
    }

    @Override
    public String toString() {
        return "{UnreadSupportShortcut[" + component + ",type = "
                + mShortcutType + ",unreadNum = " + mUnreadNum + "}";
    }
}
