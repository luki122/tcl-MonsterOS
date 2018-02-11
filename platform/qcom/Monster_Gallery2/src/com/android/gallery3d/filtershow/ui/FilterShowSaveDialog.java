package com.android.gallery3d.filtershow.ui;

import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

import com.android.gallery3d.R;

public class FilterShowSaveDialog implements OnClickListener {

    public static final int MSG_SAVE_COPY = 0x1000;
    public static final int MSG_SAVE_OVERWRITE = 0x1001;
    public static final int MSG_SAVE_DONE = 0x1002;
    public static final String DONT_ASK_TIP = "filtershow_save_tip";
    public static final String DONT_ASK_TIP_KEY = "filtershow_save_tip_key";
    public static final String DONT_ASK_TIP_OPTION = "filtershow_save_tip_option";

    private Context mContext;
    private int mTheme;
    private AlertDialog mDialog;
    private CheckBox mCheckBox;
    private Button mBtnCopy;
    private Button mBtnOverWrite;
    private LayoutInflater mInflater;
    private View mContentView;
    private Handler mHandler;
    private SharedPreferences mSp;

    public FilterShowSaveDialog(Context context, int theme, Handler handler) {
        this.mContext = context;
        this.mTheme = theme;
        this.mHandler = handler;
        mInflater = LayoutInflater.from(context);
        mSp = mContext.getSharedPreferences(DONT_ASK_TIP, 1);
        initView();
    }

    public FilterShowSaveDialog(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        mInflater = LayoutInflater.from(context);
        mSp = mContext.getSharedPreferences(DONT_ASK_TIP, 1);
        initView();
    }

    private void initView() {
        mContentView = mInflater.inflate(
                R.layout.filtershow_save_dialog_layout, null);
        mBtnCopy = (Button) mContentView.findViewById(R.id.button_copy);
        mBtnOverWrite = (Button) mContentView
                .findViewById(R.id.button_overwrite);
        mBtnCopy.setOnClickListener(this);
        mBtnOverWrite.setOnClickListener(this);

        AlertDialog.Builder dialog;
        if (mTheme > 0) {
            dialog = new AlertDialog.Builder(mContext, mTheme);
        } else {
            dialog = new AlertDialog.Builder(mContext);
        }
        dialog.setView(mContentView);
        mDialog = dialog.create();
    }

    public void show() {
        if (!dontAskCheck()) {
            if (mDialog != null && !mDialog.isShowing()) {
                mDialog.show();
            }
        } else {
            int key = mSp.getInt(DONT_ASK_TIP_OPTION, MSG_SAVE_COPY);
            switch (key) {
            case MSG_SAVE_COPY:
                mHandler.sendMessage(mHandler.obtainMessage(MSG_SAVE_COPY));
                break;
            case MSG_SAVE_OVERWRITE:
                mHandler.sendMessage(mHandler.obtainMessage(MSG_SAVE_OVERWRITE));
                break;
            default:
                break;
            }
        }
    }

    public void dissmiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button_copy:
/* MODIFIED-BEGIN by caihong.gu-nb, 2016-04-20,BUG-1963060*/
//            dontAskCommit(MSG_SAVE_COPY);
            dissmiss();
            mHandler.sendMessage(mHandler.obtainMessage(MSG_SAVE_COPY));
            break;
        case R.id.button_overwrite:
//            dontAskCommit(MSG_SAVE_OVERWRITE);
/* MODIFIED-END by caihong.gu-nb,BUG-1963060*/
            dissmiss();
            mHandler.sendMessage(mHandler.obtainMessage(MSG_SAVE_OVERWRITE));
            break;

        default:
            break;
        }
    }

/* MODIFIED-BEGIN by caihong.gu-nb, 2016-04-20,BUG-1963060*/
//    private void dontAskCommit(int type) {
//        SharedPreferences.Editor editor = mSp.edit();
//        editor.putBoolean(DONT_ASK_TIP_KEY, mCheckBox.isChecked());
//        editor.putInt(DONT_ASK_TIP_OPTION, type);
//        editor.commit();
//    }
/* MODIFIED-END by caihong.gu-nb,BUG-1963060*/

    public boolean dontAskCheck() {
        boolean dontAsk = mSp.getBoolean(DONT_ASK_TIP_KEY, false);
        return dontAsk;
    }
}
