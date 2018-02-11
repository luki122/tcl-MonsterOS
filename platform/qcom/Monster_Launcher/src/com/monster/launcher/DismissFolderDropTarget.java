package com.monster.launcher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;

import com.monster.launcher.util.Thunk;

/**
 * Created by antino on 16-6-21.
 */
public class DismissFolderDropTarget extends  ButtonDropTarget{
    public DismissFolderDropTarget(Context context){
        this(context,null);
    }

    public DismissFolderDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DismissFolderDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // Get the hover color
        mHoverColor = getResources().getColor(R.color.dismmiss_folder_target_hover_tint);
        //TODO:xiejun, I just apply mdpi picture here,remenber apply other dencenty picture.
        setDrawable(R.drawable.ic_dismiss_folder_drop_target_1);
    }


    @Override
    protected boolean supportsDrop(DragSource source, Object info) {
        Log.i(TAG,"source = "+source);
        return (info instanceof  FolderInfo);
    }

    @Override
    public void onDrop(DragObject d) {
        // Differ item deletion
        if (d.dragSource instanceof UninstallDropTarget.UninstallSource) {
            ((UninstallDropTarget.UninstallSource) d.dragSource).deferCompleteDropAfterUninstallActivity();
        }
        super.onDrop(d);
    }

    @Thunk
    void sendUninstallResult(DragSource target, boolean result) {
        if (target instanceof UninstallDropTarget.UninstallSource) {
            ((UninstallDropTarget.UninstallSource) target).onUninstallActivityReturned(result);
        }
    }

    @Override
    void completeDrop(final DragObject d) {
        //add by xiangzx

        AlertDialog dialog = new AlertDialog.Builder(this.getContext())
                .setTitle(R.string.dismiss_folder_title)
                .setMessage(R.string.dismiss_folder_msg)
                .setNegativeButton(R.string.cancel_action,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                sendUninstallResult(d.dragSource, false);
                            }
                        })
                .setPositiveButton(R.string.dismiss_folder_confirm,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (d.dragInfo instanceof FolderInfo) {
                                    final FolderInfo folderInfo = (FolderInfo) d.dragInfo;
                                    mLauncher.removeFolder(folderInfo);
                                    LauncherModel.deleteFolderContentsFromDatabase(mLauncher, folderInfo);
                                    sendUninstallResult(d.dragSource, true);
                                }else{
                                    sendUninstallResult(d.dragSource, false);
                                }
                            }
                        })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        sendUninstallResult(d.dragSource, false);
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

    }
    @Override
    public void changeImage() {
        super.changeImage();
        setDrawable(R.drawable.droptarget_dismiss_folder_animation);
        AnimationDrawable drawable = (AnimationDrawable) getCompoundDrawables()[0];
        drawable.start();
    }

    @Override
    public void resetImage() {
        super.resetImage();
        setDrawable(R.drawable.ic_dismiss_folder_drop_target_1);
    }
}
