package com.monster.autostart.interfaces;

import java.util.List;

import com.monster.autostart.bean.AppInfo;

import android.content.pm.ResolveInfo;

public interface IFilterBehavior {
    public List<AppInfo> filter();
}
