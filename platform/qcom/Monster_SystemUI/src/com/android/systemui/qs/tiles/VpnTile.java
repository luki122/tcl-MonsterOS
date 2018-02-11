package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.systemui.qs.QSTile;
import com.android.systemui.R;
import android.net.IConnectivityManager;
import android.os.ServiceManager;
import com.android.internal.net.VpnProfile;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import android.security.KeyStore;
import android.security.Credentials;
import android.util.Log;
import com.google.android.collect.Lists;
import android.net.NetworkRequest;

import java.util.ArrayList;


/**
 * Created by chenhl on 16-9-19.
 */
public class VpnTile extends QSTile<QSTile.BooleanState>{

    private final IConnectivityManager mConnectivityService = IConnectivityManager.Stub
            .asInterface(ServiceManager.getService(Context.CONNECTIVITY_SERVICE));
    private KeyStore mKeyStore = KeyStore.getInstance();
    private ArrayList<VpnProfile> mResult = Lists.newArrayList();
    private ConnectivityManager mConnectivityManager;
    private static final NetworkRequest VPN_REQUEST = new NetworkRequest.Builder()
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
            .build();
    private final static  int  MSG_REFRESH = 100;
    public VpnTile(Host host) {
        super(host);
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {

        if(!checkVpnEnable()){
            handleLongClick();
            return;
        }

        if(mState.value){
            disconnect();
        }else{
            try {
                loadVpnProfiles();
                connect(getFirstProfiles());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mState.icon = ResourceIcon.get(R.drawable.ic_mst_qs_vpn_enable);
            handleStateChanged();
        }
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        loadVpnProfiles();
        boolean value = isConnect();
        int connectState = getConnectState();
        state.label = mContext.getString(R.string.mst_vpn);
        state.value = value;
        state.icon = ResourceIcon.get((isConnectting(connectState)||state.value) ?R.drawable.ic_mst_qs_vpn_enable:
                R.drawable.ic_mst_qs_vpn_disable);
    }

    @Override
    public int getMetricsCategory() {
        return 5;
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent(Settings.ACTION_VPN_SETTINGS);
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.mst_vpn);
    }

    @Override
    public void setListening(boolean listening) {
        if(listening) {
            //mConnectivityManager.registerNetworkCallback(VPN_REQUEST, mNetworkCallback);
            mHandler.sendEmptyMessage(MSG_REFRESH);
        }else {
            //mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
            mHandler.removeMessages(MSG_REFRESH);
        }

    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            removeMessages(MSG_REFRESH);
            refreshState();
            sendEmptyMessageDelayed(MSG_REFRESH,1000);
        }
    };

    private void connect(VpnProfile profile) throws RemoteException {
        try {
            if(profile!=null)
                mConnectivityService.startLegacyVpn(profile);
        } catch (IllegalStateException e) {
        }
    }

    private void disconnect() {
        try {
            LegacyVpnInfo connected = mConnectivityService.getLegacyVpnInfo(UserHandle.myUserId());
            if (connected != null ) {
                clearLockdownVpn(mContext);
                mConnectivityService.prepareVpn(VpnConfig.LEGACY_VPN, VpnConfig.LEGACY_VPN,
                        UserHandle.myUserId());
            }
        } catch (RemoteException e) {
        }
    }

    private int getConnectState(){
        try {
            LegacyVpnInfo connected = mConnectivityService.getLegacyVpnInfo(UserHandle.myUserId());
            if(connected!=null){
                return connected.state;
            }
        } catch (RemoteException e) {
        }
        return  LegacyVpnInfo.STATE_DISCONNECTED;
    }
    public static void clearLockdownVpn(Context context) {
        KeyStore.getInstance().delete(Credentials.LOCKDOWN_VPN);
        // Always notify ConnectivityManager after keystore update
        context.getSystemService(ConnectivityManager.class).updateLockdownVpn();
    }

    private boolean checkVpnEnable(){

        return mResult.size()>0;
    }

    private boolean isConnect(){
        try {
            LegacyVpnInfo connected = mConnectivityService.getLegacyVpnInfo(UserHandle.myUserId());
            return connected != null&&(connected.state==LegacyVpnInfo.STATE_CONNECTED);
        }catch (RemoteException e) {
        }
        return false;
    }

    private boolean isConnectting(int state){

        return state==LegacyVpnInfo.STATE_INITIALIZING
                || state==LegacyVpnInfo.STATE_CONNECTING;
    }

    private void loadVpnProfiles(){
        mResult.clear();
        for (String key : mKeyStore.list(Credentials.VPN)) {
            final VpnProfile profile = VpnProfile.decode(key, mKeyStore.get(Credentials.VPN + key));
            if (profile != null) {
                mResult.add(profile);
            }
        }
    }

    private VpnProfile getFirstProfiles(){

        return mResult.size()>0?mResult.get(0):null;
    }

    private ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            refreshState();
        }

        @Override
        public void onLost(Network network) {
            refreshState();
        }
    };
}
