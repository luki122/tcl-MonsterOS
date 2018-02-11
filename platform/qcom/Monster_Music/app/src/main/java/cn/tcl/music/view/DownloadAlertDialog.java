package cn.tcl.music.view;

import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import cn.tcl.music.R;

/**
 * Created by Administrator on 2015/12/22.
 */
public class DownloadAlertDialog {
    public static final String TAG = DownloadAlertDialog.class.getSimpleName();
    private OnDialogButtonClickListener buttonClickListner;
    public static boolean showAgain = true;
    private static AlertDialog mAlertDialog;

    public DownloadAlertDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);

        builder.setMessage(context.getString(R.string.dialog_download_message));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showAgain = false;
                buttonClickListner.okButtonClick();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                buttonClickListner.cancelButtonClick();
                dialog.dismiss();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.setCanceledOnTouchOutside(false);
    }


    public interface OnDialogButtonClickListener {
        void okButtonClick();
        void cancelButtonClick();

    }

    public void setOnButtonClickListener(OnDialogButtonClickListener listener) {
        this.buttonClickListner = listener;
    }

   public void showWrapper(){
       if(showAgain){
           mAlertDialog.show();
       }else{
           if(buttonClickListner!=null){
               buttonClickListner.okButtonClick();
           }
       }
   }
}