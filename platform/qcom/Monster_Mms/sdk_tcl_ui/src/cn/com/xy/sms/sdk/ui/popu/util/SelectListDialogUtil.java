package cn.com.xy.sms.sdk.ui.popu.util;

import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.widget.AdapterDataSource;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquDialogSelected;
import cn.com.xy.sms.sdk.ui.popu.widget.ListDialog4NumSwitch;
import cn.com.xy.sms.sdk.ui.popu.widget.SelectDataAdapter;
import cn.com.xy.sms.util.SdkCallBack;

public class SelectListDialogUtil {
    private static final String TAG = "SelectListDialogUtil";
    private static boolean mHavePopupDialog = false;

    /**
     * Show select list dialog click event
     * 
     */
    public static OnClickListener showSelectListDialogClickListener(final Context context, final String dialogTitle,
            final String confirmText, final String caneclText, final AdapterDataSource adapterDataSource,
            final DuoquDialogSelected selected, final SdkCallBack callBack) {
        if (context == null || adapterDataSource == null || adapterDataSource.getDataSrouce() == null
                || adapterDataSource.getDataSrouce().length() == 0) {
            return null;
        }
        return new OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View arg0) {
                if (mHavePopupDialog) {
                    return;
                }
                mHavePopupDialog = true;
                try {
                    selectListDialog_win(context, dialogTitle, confirmText, caneclText,
                            new SelectDataAdapter(context, adapterDataSource, selected), callBack).show();
                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                }
            }
        };
    }

    private static Dialog selectListDialog_win(Context context, String dialogTitle, String confirmText,
            String caneclText, final SelectDataAdapter dataAdapter, final SdkCallBack callBack) {

        ListDialog4NumSwitch dialog = new ListDialog4NumSwitch(dataAdapter, new SdkCallBack() {

            @Override
            public void execute(Object... obj) {
                // TODO Auto-generated method stub
                selectedDataCallBack(dataAdapter, callBack);
            }
        }, context, R.style.ShareDialog, dialogTitle);
        Window win = dialog.getWindow();
        int paddingDp = ViewManger.getIntDimen(context, R.dimen.duoqu_dialo_margin_screen);
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        win.setAttributes(lp);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mHavePopupDialog = false;
            }
        });
        return dialog;
    }

    @SuppressLint("NewApi")
    private static AlertDialog selectListDialog(Context context, String dialogTitle, String confirmText,
            String caneclText, final SelectDataAdapter dataAdapter, final SdkCallBack callBack) {
        return new AlertDialog.Builder(context).setIconAttribute(android.R.attr.alertDialogIcon).setTitle(dialogTitle)
                .setAdapter(dataAdapter, null).setPositiveButton(confirmText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedDataCallBack(dataAdapter, callBack);
                        dialog.dismiss();
                    }
                }).setNegativeButton(caneclText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHavePopupDialog = false;
                    }
                }).create();
    }

    private static void selectedDataCallBack(SelectDataAdapter dataAdapter, SdkCallBack callBack) {
        for (Entry<String, Boolean> entry : dataAdapter.mCheckedStates.entrySet()) {
            if (entry.getValue().equals(true)) {
                try {
                    int index = Integer.parseInt(entry.getKey());
                    ContentUtil.callBackExecute(callBack, dataAdapter.getItem(index), index,
                            dataAdapter.getDisplayValue(index));
                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
                    break;
                }
                break;
            }
        }
    }
}
