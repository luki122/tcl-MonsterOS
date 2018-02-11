/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.services;

import android.content.ServiceConnection;

import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Manager for play service
 * Created on 16-9-7.
 */
public class PlayerServiceConnectionManager {

    private final String TAG = PlayerServiceConnectionManager.class.getSimpleName();

    private ServiceConnection mPlayerServiceConnection;

    private String mCurrentPlayFile;

    private PlayerService mService;

    private static PlayerServiceConnectionManager mManager= new PlayerServiceConnectionManager();

    private  PlayerServiceConnectionManager(){
        mCurrentPlayFile = "";
    }

    public synchronized static PlayerServiceConnectionManager getInstance(){
        return mManager;
    }

    public ServiceConnection getPlayerServiceConnection() {
        return mPlayerServiceConnection;
    }

    public void setManagerInfo(ServiceConnection mPlayerServiceConnection, String playFile,
                               PlayerService playerService) {
        MeetingLog.d(TAG,"setManagerInfo : set file :" + playFile);
        this.mPlayerServiceConnection = mPlayerServiceConnection;
        mCurrentPlayFile = playFile;
        mService = playerService;
    }

    public String getCurrentPlayFile() {
        return mCurrentPlayFile;
    }

    public PlayerService getService() {
        return mService;
    }
}
