package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.android.systemui.qs.QSTile;
import com.android.systemui.R;

/**
 * Created by chenhl on 16-12-14.
 */
public class HideNavigationbarTile extends QSTile<QSTile.BooleanState> {

    private final static String HIDE_NAVIGATIONBAR = "tcl_sys_hide_navigationbar";
    private final SettingObserver mSetting;
    private boolean mListening;
    private long mTime=0;

    public HideNavigationbarTile(Host host) {
        super(host);
        mSetting = new SettingObserver(mHandler);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
        long time = System.currentTimeMillis();
        if(time-mTime>200) {
            mTime = time;
            setHideNavigationbar(!mState.value);
        }
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.value=getHideState();
        Log.d("chenhl","handleUpdateState state.value:"+state.value);
        if(state.value){
            state.icon = ResourceIcon.get(R.drawable.ic_mst_navigationbar_show);
            state.label = mContext.getString(R.string.tcl_hide);
        }else{
            state.icon = ResourceIcon.get(R.drawable.ic_mst_navigationbar_hide);
            state.label = mContext.getString(R.string.tcl_show);
        }
    }

    @Override
    public int getMetricsCategory() {
        return 4;
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    protected void setListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        mSetting.setListening(listening);
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.tcl_show);
    }

    private class SettingObserver extends ContentObserver {


        public SettingObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            refreshState();
        }

        public void setListening(boolean listening) {
            if (listening) {
                mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(HIDE_NAVIGATIONBAR), false, this);
            } else {
                mContext.getContentResolver().unregisterContentObserver(this);
            }
        }
    }

    private boolean getHideState(){
        int hide=Settings.System.getInt(mContext.getContentResolver(),HIDE_NAVIGATIONBAR,0);
        return hide==1;
    }

    private void setHideNavigationbar(boolean is){
        Settings.System.putInt(mContext.getContentResolver(),HIDE_NAVIGATIONBAR,is?1:0);
    }
}
