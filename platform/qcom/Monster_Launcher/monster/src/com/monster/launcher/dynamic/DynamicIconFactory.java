package com.monster.launcher.dynamic;

import android.content.ComponentName;

import com.monster.launcher.AppInfo;
import com.monster.launcher.CalendarDynamic;
import com.monster.launcher.DeskClockDynamic;
import com.monster.launcher.ItemInfo;
import com.monster.launcher.Log;
import com.monster.launcher.NetManageDynamic;
import com.monster.launcher.ShortcutInfo;
import com.monster.launcher.WeatherDynamic;

import java.util.ArrayList;

/**
 * 创建于 cailiuzuo on 16-7-15 下午2:48.
 * 作者
 */
public class DynamicIconFactory {
    private static DynamicIconFactory mInstance = null;
    private final int NOMAL=10;
    private final int CLOCKDYNAMIC=0;
    private final int CALENDARDYNAMIC=1;
    private final int WEATHERDYNAMIC=2;
    private final int NETMANAGEDYNAMIC=3;
    private DeskClockDynamic mDeskClockDynamic;
    private CalendarDynamic mCalendarDynamic;
    private WeatherDynamic mWeatherDynamic;
    private NetManageDynamic mNetManageDynamic;
    private static ArrayList<IDynamicIcon> mList;
    private DynamicIconFactory() {
    }
    String[] mPackageName=new String[]{"com.android.deskclock","com.android.calendar",
            "cn.tcl.weather","com.monster.netmanage"};
    String[] mClassName=new String[]{"com.android.deskclock.DeskClock","com.android.calendar.AllInOneActivity",
            "cn.tcl.weather.EntranceActivity","com.monster.netmanage.activity.MainActivity"};
    public static DynamicIconFactory getInstance() {
        if (mInstance == null) {
            mInstance=new DynamicIconFactory();
            mList= new ArrayList();
        }

        return mInstance;
    }
    public IDynamicIcon createDynamicIcon(ItemInfo info){
        switch (isDynamicIcon(info)){
            case NOMAL:
                return null;
            case CLOCKDYNAMIC:
                if (mDeskClockDynamic==null)
                mDeskClockDynamic= new DeskClockDynamic();

                if(!mList.contains(mDeskClockDynamic))
                mList.add(mDeskClockDynamic);
                return mDeskClockDynamic;
            case CALENDARDYNAMIC:
                if (mCalendarDynamic==null)
                mCalendarDynamic=new CalendarDynamic();

                if(!mList.contains(mCalendarDynamic))
                mList.add(mCalendarDynamic);
                return mCalendarDynamic;
            case WEATHERDYNAMIC:
                Log.d("WeatherDynamic","case weather");
                if (mWeatherDynamic==null)
                    mWeatherDynamic=new WeatherDynamic();

                if(!mList.contains(mWeatherDynamic))
                    mList.add(mWeatherDynamic);
                return mWeatherDynamic;
            case NETMANAGEDYNAMIC:
                Log.d("liuzuo914","case netmanage");
                if (mNetManageDynamic==null)
                    mNetManageDynamic=new NetManageDynamic();

                if(!mList.contains(mNetManageDynamic))
                    mList.add(mNetManageDynamic);
                return mNetManageDynamic;
        }
        return null;
    }
    public int isDynamicIcon(ItemInfo info){

    for (int i=0;i<mPackageName.length;i++){
        if(info instanceof ShortcutInfo ||info instanceof AppInfo){
            if (info != null && info.getIntent() != null) {
                ComponentName cn = info.getIntent().getComponent();
                if (cn != null
                        && mPackageName[i].equals(cn.getPackageName())
                        && mClassName[i].equals(cn.getClassName())) {
                    return i;
                }
            }
        }

    }
        return NOMAL;
    }
    public ArrayList<IDynamicIcon> getAllDynamicIcon(){
        return mList;
    }
}
