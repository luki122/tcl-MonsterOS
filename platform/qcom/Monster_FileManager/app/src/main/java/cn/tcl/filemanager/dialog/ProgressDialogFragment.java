/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.NumberFormat;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.fragment.FileBrowserFragment.AbsListViewFragmentListener;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.service.ProgressInfo;
import cn.tcl.filemanager.utils.LogUtils;

public class ProgressDialogFragment extends DialogFragment {
    public static final String TAG = ProgressDialogFragment.class.getSimpleName();
    private static final String STYLE = "style";
    private static final String TITLE = "title";
    private static final String CANCEL = "cancel";
    private static final String TOTAL = "total";
    private static final String MESSAGE = "message";
    private View.OnClickListener mCancelListener;
    private static NumberFormat mProgressPercentFormat;
    private FileManagerApplication mApplication;
    private AbsListViewFragmentListener mAbsListViewFragmentListener;
    private int mTitle;
    private TextView mTextPercent;
    private TextView mTextName;
    private ProgressBar mProgressBar;
    private Button mCancelButton;
    private Dialog mProgressDialog;

    /**
     * This method gets a instance of ProgressDialogFragment
     *
     * @param style   resource ID of style of DialogFragment
     * @param title   resource ID of title shown on DialogFragment
     * @param message resource ID of message shown on DialogFragment
     * @param cancel  resource ID of content on cancel button
     * @return a progressDialogFragment
     */
    public static ProgressDialogFragment newInstance(int style, int title,
                                                     int message, int cancel) {
        ProgressDialogFragment f = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putInt(STYLE, style);
        args.putInt(TITLE, title);
        args.putInt(CANCEL, cancel);
        args.putInt(MESSAGE, message);
        f.setArguments(args);
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
        return f;
    }

    /**
     * This method sets cancel listener to cancel button
     *
     * @param listener clickListener, which will do proper things when touch
     *                 cancel button
     */
    public void setCancelListener(View.OnClickListener listener) {
        mCancelListener = listener;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mProgressDialog = new Dialog(getActivity());
        mApplication = (FileManagerApplication) getActivity().getApplicationContext();
        mAbsListViewFragmentListener = (AbsListViewFragmentListener) getActivity();
        mProgressDialog.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.copying_dialog, null, false);
        mProgressDialog.setContentView(view);
        mTextName = (TextView) view.findViewById(R.id.copying_name);
        mProgressBar = (ProgressBar) view.findViewById(R.id.copying_progress);
        mTextPercent = (TextView) view.findViewById(R.id.copying_percent);
        mCancelButton = (Button) view.findViewById(R.id.copying_cancel);
        mTextName.setText(R.string.loading);
        setProgress(0);
        Bundle args = null;
        if (savedInstanceState == null) {
            args = getArguments();
        } else {
            args = savedInstanceState;
        }
        if (args != null) {
            int title = args.getInt(TITLE, AlertDialogFragment.INVIND_RES_ID);
            mTitle = title;
            if (title != AlertDialogFragment.INVIND_RES_ID) {
                mTextName.setText(title);
                mCancelButton.setText(R.string.cancel);
            }
        }
        mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        View cancel = view.findViewById(R.id.copying_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mCancelListener) {
                    mCancelListener.onClick(v);
                } else {
                    mProgressDialog.dismiss();
                    if (mTitle == R.string.pasting) {
                        mApplication.mFileInfoManager.clearPasteList();
                        LogUtils.e(TAG,"ProgressDialogFragment paste mode cancel enter...");
                        mApplication.mFileInfoManager.setPasteStatus(FileInfoManager.PASTE_MODE_CANCEL);
                    } else if (mTitle == R.string.deleting) {
                        mApplication.mFileInfoManager.clearRemoveList();
                        mApplication.mFileInfoManager.setDeleteStatus(FileInfoManager.DELETE_MODE_CANCEL);
                    }
                }
            }
        });
        mProgressDialog.setContentView(view);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        return mProgressDialog;
    }

    public void setTitle(String name) {
        mTitle = -1;
        if (mTextName != null) {
            mTextName.setText(name);
        }
    }

    public void setTitle(int id) {
        mTitle = -1;
        if (mTextName != null) {
            mTextName.setText(id);
        }
    }

    public boolean isShowing() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            return true;
        }
        return false;
    }

    public void setCancelIsVisiable(int isVisiable) {
        if (null != mCancelButton) {
            mCancelButton.setVisibility(isVisiable);
        }
    }

    public void setProgress(int progress) {
        if (null != mProgressBar) {
            mProgressBar.setMax(100);
            mProgressBar.setProgress(progress);
            String percentInfo = progress + " %";
            mTextPercent.setText(percentInfo);
        }
    }

    /**
     * This method sets progress of progressDialog according to information of
     * received ProgressInfo.
     *
     * @param progressInfo information which need to be updated on
     *                      progressDialog
     */
    public void setProgress(ProgressInfo progressInfo) {
        int progress = progressInfo.getProgeress();
        int max = (int) progressInfo.getTotal();
        long progressSize = progressInfo.getProgressSize();
        long totalSize = progressInfo.getTotalSize();
        mProgressBar.setMax(100);
        if (totalSize != 0) {
            double percent = (progressSize / (double) totalSize) * 100;
            String str = String.format("%.0f", percent);
            String percentInfo = str + " %";
            mProgressBar.setProgress((int) percent);
            mTextPercent.setText(percentInfo);
        } else {
            double percent = (progress / (double) max) * 100;
            String str = String.format("%.0f", percent);
            String percentInfo = str + " %";
            mProgressBar.setProgress((int) percent);
            mTextPercent.setText(percentInfo);
        }
    }

    public void setLoadProgress(ProgressInfo progressInfo) {
        int progress = progressInfo.getProgeress();
        int max = (int) progressInfo.getTotal();
        mProgressBar.setMax(100);
        mCancelButton.setVisibility(View.GONE);
        double percent = (progress / (double) max) * 100;
        String str = String.format("%.0f", percent);
        String percentInfo = str + " %";
        mProgressBar.setProgress((int) percent);
        mTextPercent.setText(percentInfo);
    }
}
