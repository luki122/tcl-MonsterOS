package cn.tcl.music.view;

import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import cn.tcl.music.R;

/**
 * Created by renakang on 10/18/16.
 */

public class RemoveSongsDialog {
    private Context mContext;
    private boolean mIsContainLocalFile;
    private boolean mShouldKeepIt;
    private OnRemoveSongsDialogOkClickListener mOnRemoveSongsDialogOkClickListener;

    public interface OnRemoveSongsDialogOkClickListener {
        void onOkClick(boolean shouldKeepIt);
    }

    public RemoveSongsDialog(Context context, boolean isContainLocalFile) {
        mContext = context;
        mIsContainLocalFile = isContainLocalFile;
    }

    public void setOnRemoveSongsDialogOkClickListener(OnRemoveSongsDialogOkClickListener onRemoveSongsDialogOkClickListener){
        mOnRemoveSongsDialogOkClickListener = onRemoveSongsDialogOkClickListener;
    }

    public AlertDialog createRemoveDialog() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_remove_songs, null);
        CheckBox checkToDelete = (CheckBox) dialogView.findViewById(R.id.selected_to_delete);
        checkToDelete.setVisibility(mIsContainLocalFile ? View.VISIBLE : View.GONE);
        mShouldKeepIt = checkToDelete.isChecked();
        AlertDialog removeDialog = new AlertDialog.Builder(mContext, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mOnRemoveSongsDialogOkClickListener.onOkClick(mShouldKeepIt);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        checkToDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mShouldKeepIt = isChecked;
                }
            });
        return removeDialog;
    }
}
