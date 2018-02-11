/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.app;

import android.text.TextUtils;

import cn.tcl.transfer.systemApp.CalendarSysApp;
import cn.tcl.transfer.systemApp.ContactsSysApp;
import cn.tcl.transfer.systemApp.DialerSysApp;
import cn.tcl.transfer.systemApp.EmailSysApp;
import cn.tcl.transfer.systemApp.LauncherSysApp;
import cn.tcl.transfer.systemApp.MeetingAssistantSysApp;
import cn.tcl.transfer.systemApp.MmsSysApp;
import cn.tcl.transfer.systemApp.NoteSysApp;
import cn.tcl.transfer.systemApp.OtherSysApp;
import cn.tcl.transfer.systemApp.SettingsSysApp;
import cn.tcl.transfer.systemApp.SoundrecorderSysApp;
import cn.tcl.transfer.systemApp.SysBaseApp;

public class AppSendManager {

    private static AppSendManager mAppSendManager;


    private AppSendManager() {

    }

    public synchronized static AppSendManager getInstance() {
        if(mAppSendManager == null) {
            mAppSendManager = new AppSendManager();
        }
        return mAppSendManager;
    }


    public BaseApp getAppByName(String packageName) {

        if(TextUtils.equals("com.tencent.mm", packageName)) {
            return new WeChatApp();
        } else if(TextUtils.equals(packageName, "com.tencent.mobileqq")) {
            return new QQApp();
        } else if(TextUtils.equals(packageName, "com.netease.cloudmusic")) {
            return new NeteaseMusicApp();
        } else if(TextUtils.equals(packageName, "com.sina.weibo")) {
            return new SinaWeiboApp();
        } else if(TextUtils.equals(packageName, "com.mt.mtxx.mtxx")) {
            return new MeituApp();
        } else if(TextUtils.equals(packageName, "com.autonavi.minimap")) {
            return new AmapApp();
        } else if(TextUtils.equals(packageName, "com.baidu.BaiduMap")) {
            return new BaiduMapApp();
        } else if(TextUtils.equals(packageName, "com.tencent.mtt")) {
            return new QQBrowserApp();
        } else if(TextUtils.equals(packageName, "com.taobao.taobao")) {
            return new TaobaoApp();
        } else if(TextUtils.equals(packageName, "com.tencent.qqmusic")) {
            return new QQMusicApp();
        } else if(TextUtils.equals(packageName, "com.sohu.inputmethod.sogou")) {
            return new SogouInputApp();
        } else if(TextUtils.equals(packageName, " com.sdu.didi.psnger")) {
            return new DidiApp();
        }
        return new OtherApp(packageName) ;
    }

    public SysBaseApp getSysAppByName(String packageName) {

        if(TextUtils.equals(CalendarSysApp.NAME, packageName)) {
            return new CalendarSysApp();
        } else if(TextUtils.equals(ContactsSysApp.NAME, packageName)) {
            return new ContactsSysApp();
        } else if(TextUtils.equals(DialerSysApp.NAME, packageName)) {
            return new DialerSysApp();
        } else if(TextUtils.equals(EmailSysApp.NAME, packageName)) {
            return new EmailSysApp();
        } else if(TextUtils.equals(LauncherSysApp.NAME, packageName)) {
            return new LauncherSysApp();
        } else if(TextUtils.equals(MeetingAssistantSysApp.NAME, packageName)) {
            return new MeetingAssistantSysApp();
        } else if(TextUtils.equals(MmsSysApp.NAME, packageName)) {
            return new MmsSysApp();
        } else if(TextUtils.equals(NoteSysApp.NAME, packageName)) {
            return new NoteSysApp();
        } else if(TextUtils.equals(SettingsSysApp.NAME, packageName)) {
            return new SettingsSysApp();
        } else if(TextUtils.equals(SoundrecorderSysApp.NAME, packageName)) {
            return new SoundrecorderSysApp();
        }
        return new OtherSysApp(packageName) ;
    }

}
