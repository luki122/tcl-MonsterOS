package com.android.systemui.qs.tiles;

import android.content.Intent;

import com.android.systemui.qs.QSTile;
import com.android.systemui.R;

/**
 * Created by chenhl on 16-9-19.
 */
public class SecretLetterTile extends QSTile<QSTile.BooleanState>{

    public SecretLetterTile(Host host) {
        super(host);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.label = mContext.getString(R.string.mst_secret_letter);
        state.value = false;
        state.icon = ResourceIcon.get(state.value ?R.drawable.ic_mst_qs_secret_enable:
                R.drawable.ic_mst_qs_secret_disable);
    }

    @Override
    public int getMetricsCategory() {
        return 6;
    }

    @Override
    protected void handleLongClick() {

    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.mst_secret_letter);
    }

    @Override
    public void setListening(boolean listening) {

    }
}
