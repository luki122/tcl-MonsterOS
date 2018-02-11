package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.util.Log;

import com.android.systemui.qs.QSTile;
import com.android.systemui.R;

/**
 * Created by chenhl on 16-9-19.
 */
public class NfcTile extends QSTile<QSTile.BooleanState>{

    private NfcAdapter mNfcAdapter;
    private boolean mListening;
    private boolean mNfcEnable;
    public NfcTile(Host host) {
        super(host);
        initAdapter();
    }

    private void initAdapter(){
        if(mNfcAdapter==null){
            mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
            if(mNfcAdapter==null){
                mNfcEnable=false;
            }else{
                mNfcEnable = true;
            }
        }
    }
    @Override
    public boolean isAvailable() {
        return mContext.getPackageManager().hasSystemFeature("android.hardware.nfc");
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
        initAdapter();
        if(!mNfcEnable){
            return;
        }
        if (!mState.value) {
            mNfcAdapter.enable();
        } else {
            mNfcAdapter.disable();
        }
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        initAdapter();
        final int value = arg instanceof Integer ? (Integer)arg :
                (mNfcEnable?mNfcAdapter.getAdapterState():NfcAdapter.STATE_OFF);

        final boolean nfcswitch = value == NfcAdapter.STATE_ON;
        state.value = nfcswitch;
        state.label = mContext.getString(R.string.mst_nfc);
        if(nfcswitch){
            state.icon = ResourceIcon.get(R.drawable.ic_mst_qs_nfc_enable);
        }else{
            state.icon = ResourceIcon.get(R.drawable.ic_mst_qs_nfc_disable);
        }
    }

    @Override
    public int getMetricsCategory() {
        return 4;
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent(Settings.ACTION_NFC_SETTINGS);
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.mst_nfc);
    }

    @Override
    public void setListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        if (listening) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
            mContext.registerReceiver(mReceiver, filter);
        } else {
            mContext.unregisterReceiver(mReceiver);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NfcAdapter.ACTION_ADAPTER_STATE_CHANGED.equals(intent.getAction())) {
                int state=intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE,
                        NfcAdapter.STATE_OFF);
                refreshState(state);
            }
        }
    };
}
