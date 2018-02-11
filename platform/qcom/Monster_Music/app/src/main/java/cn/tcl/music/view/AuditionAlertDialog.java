package cn.tcl.music.view;

import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import cn.tcl.music.R;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.SystemUtility;

/**
 * Created by guofeng.wu on 2015/12/21.
 */
public class AuditionAlertDialog {
    public static final String TAG = AuditionAlertDialog.class.getSimpleName();
    public static boolean showAgain = true;
    private static OnSelectedListener mCallback;
    private static AuditionAlertDialog mAuditionAlertDialog;
    private static AlertDialog mAlertDialog;

    public boolean shouldShow(boolean local){
        LogUtil.d("jiangyuanxi","showAgain = " +showAgain);
        return SystemUtility.getNetworkType() == SystemUtility.NetWorkType.mobile && showAgain &&
                !local;
    }

    public boolean showWrapper(boolean local){
        LogUtil.d(TAG,"showWrapper shouldShow(local) = " +shouldShow(local));
        if (shouldShow(local)){
            mAlertDialog.show();
            LogUtil.d(TAG,"Dialog have showed");
            return true;
        }
        return false;
    }

    public boolean showWrapper(boolean local, OnSelectedListener mCallback){
        if(mCallback != null){
            this.mCallback = mCallback;
           if (shouldShow(local)){
               mAlertDialog.show();
               return true;
           } else {
               mCallback.onPlay();
           }

        }
        return false;
    }

    public static AuditionAlertDialog getInstance(Context context){

            mAuditionAlertDialog = new AuditionAlertDialog();
            AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);

            builder.setMessage(context.getString(R.string.dialog_audition_message));
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showAgain = false;
                    if (mCallback != null) {
                        mCallback.onPlay();
                    }
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            mAlertDialog = builder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);

        return mAuditionAlertDialog;
    }

    public interface OnSelectedListener{
        void onPlay();
    }
}
