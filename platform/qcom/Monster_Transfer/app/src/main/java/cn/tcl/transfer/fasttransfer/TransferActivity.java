/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.fasttransfer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Telephony;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.transfer.sdk.access.ILogicObsv;
import com.tencent.transfer.sdk.access.MessageIdDef;
import com.tencent.transfer.sdk.access.TransferStatusMsg;
import com.tencent.transfer.sdk.access.UTransferRes;
import com.tencent.transfer.sdk.access.UTransferState;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import beebeesdk.core.BeeBeeEngine;
import beebeesdk.core.HoneyComb;
import cn.tcl.transfer.R;
import cn.tcl.transfer.activity.CompleteActivity;
import cn.tcl.transfer.activity.DisconnectActivity;
import cn.tcl.transfer.activity.GearView;
import cn.tcl.transfer.activity.LockableScrollView;
import cn.tcl.transfer.util.AppUtils;
import cn.tcl.transfer.util.CodeUtil;
import cn.tcl.transfer.util.DialogBuilder;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.NotificationUtils;
import cn.tcl.transfer.util.Utils;
import cn.tcl.transfer.util.qrimage.QRImage;
import mst.app.MstActivity;

import android.content.pm.IPackageInstallObserver2;

public class TransferActivity extends MstActivity implements ILogicObsv {
    private HoneyComb mHoneyComb;
    private TextView mStatusText;
    private Button mBtn_bottom;
    private View mContactItem;
    private View mSmsItem;
    private View mCalllogItem;
    private View mBookmarkItem;
    private View mCalendarItem;
    private View mPictureItem;
    private View mVideoItem;
    private View mMusicItem;
    private View mAppItem;
    private View mInstallItem;
    private Dialog mDisconnectDialog;
    private ReceiveBackground mStatusIcon;
    private ImageView mEndIcon;
    private GearView mRestoreIcon;
    private LockableScrollView mScrollView;
    private int mCurrent;
    ArrayList<File> mApks;
    private boolean mHasApp = false;
    private boolean mHasData = false;
    private boolean mIsTransferFinish = false;
    private boolean mIsDisconnect = false;
    private boolean mIsRestoring = false;
    private boolean mIsInstall = false;
    private boolean mIsContactStart = false;
    private boolean mIsSmsStart = false;
    private boolean mIsCalllogStart = false;
    private boolean mIsBookmarkStart = false;
    private boolean mIsCalendarStart = false;
    private boolean mIsPictureStart = false;
    private boolean mIsVideoStart = false;
    private boolean mIsMusicStart = false;
    private boolean mIsAppStart = false;
    public static final String SOFT_PATH= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Transfer/FileTransfer/soft/";
    private static final String TAG = "QQTransfer";
    private static final String ELLIPSIS = "...";
    private final int INSTALL_COMPLETED = 1;
    private static final String ISBACK="isback";
    private static final String CURRENT = "current";
    private static final String HASAPP = "hasapp";
    private static final String HASDATA = "hasdata";
    private static final String DISCONNECT = "isdisconnect";
    private static final String RESTORE = "isrestore";
    private static final String TRANSFERFINISH = "istransferfinish";
    private static final String INSTALL = "isinstall";
    private static final String ISCONTACTSTART = "mIsContactStart";
    private static final String ISSMSSTART = "mIsSmsStart";
    private static final String ISCALLLOGSTART = "mIsCalllogStart";
    private static final String ISBOOKMARKSTART = "mIsBookmarkStart";
    private static final String ISCALENDARSTART = "mIsCalendarStart";
    private static final String ISPICTURESTART = "mIsPictureStart";
    private static final String ISVIDIEOSTART = "mIsVideoStart";
    private static final String ISMUSICSTART = "mIsMusicStart";
    private static final String ISAPPSTART = "mIsAppStart";
    private static final long WAIT_TIME=3*1000L;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        acquireWakeLock();
        setMstContentView(R.layout.activity_qq_receiver);
        initUI(savedInstanceState);
        initData(savedInstanceState);
        Toast.makeText(this, R.string.notify_receiving, Toast.LENGTH_SHORT).show();
    }

    private void initUI(Bundle savedInstanceState) {
        mStatusText = (TextView)findViewById(R.id.status);
        mScrollView = (LockableScrollView)findViewById(R.id.scroll);
        mBtn_bottom= (Button)findViewById(R.id.bottom_btn);
        mBtn_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsTransferFinish) {
                    mHoneyComb.interruptTransferData();
                    mHoneyComb.reset();
                    finish();
                } else {
                    if (mIsRestoring) {
                        moveTaskToBack(true);
                    } else {
                        if (mDisconnectDialog == null) {
                            mDisconnectDialog = DialogBuilder.createConfirmDialog(TransferActivity.this, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mDisconnectDialog != null) {
                                        mDisconnectDialog.dismiss();
                                    }
                                    mHoneyComb.interruptTransferData();
                                    mHoneyComb.reset();
                                    mIsDisconnect = true;
                                    Intent intent = new Intent(TransferActivity.this, DisconnectActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    TransferActivity.this.finish();
                                }
                            }, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mDisconnectDialog.isShowing()) {
                                        mDisconnectDialog.dismiss();
                                    }
                                }
                            }, getResources().getString(R.string.qq_cancel_confirm_info));
                        }
                        mDisconnectDialog.dismiss();
                        mDisconnectDialog.show();
                    }
                }
            }
        });
        mContactItem = findViewById(R.id.contact_item);
        mSmsItem = findViewById(R.id.sms_item);
        mCalllogItem = findViewById(R.id.calllog_item);
        mBookmarkItem = findViewById(R.id.bookmark_item);
        mCalendarItem = findViewById(R.id.calendar_item);
        mPictureItem = findViewById(R.id.picture_item);
        mVideoItem = findViewById(R.id.video_item);
        mMusicItem = findViewById(R.id.music_item);
        mAppItem = findViewById(R.id.app_item);
        mInstallItem = findViewById(R.id.install_item);
        mStatusIcon = (ReceiveBackground)findViewById(R.id.status_icon);
        mStatusIcon.post(new Runnable() {
            @Override
            public void run() {
                mStatusIcon.init(TransferActivity.this);
                mStatusIcon.startRippleAnimation();
                }
            });
        mEndIcon = (ImageView)findViewById(R.id.end_icon);
        mRestoreIcon = (GearView)findViewById(R.id.restore_icon);
        mRestoreIcon.setVisibility(View.GONE);
        mContactItem.setVisibility(View.GONE);
        mSmsItem.setVisibility(View.GONE);
        mCalllogItem.setVisibility(View.GONE);
        mBookmarkItem.setVisibility(View.GONE);
        mCalendarItem.setVisibility(View.GONE);
        mPictureItem.setVisibility(View.GONE);
        mVideoItem.setVisibility(View.GONE);
        mMusicItem.setVisibility(View.GONE);
        mAppItem.setVisibility(View.GONE);
        mInstallItem.setVisibility(View.GONE);
    }

    private void initData(Bundle savedInstanceState) {
        mApks = new ArrayList<File>();
        BeeBeeEngine.getInstance(getApplicationContext()).bindObsv(this);
        mHoneyComb = BeeBeeEngine.getInstance(getApplicationContext()).getHoneyComb();
        mHoneyComb.setSaveDataObvs(new ImportDataProcess());
        if (savedInstanceState != null) {
            mCurrent = savedInstanceState.getInt(CURRENT,0);
            mHasApp = savedInstanceState.getBoolean(HASAPP,false);
            mHasData = savedInstanceState.getBoolean(HASDATA,false);
            mIsTransferFinish = savedInstanceState.getBoolean(TRANSFERFINISH,false);
            mIsDisconnect = savedInstanceState.getBoolean(DISCONNECT,false);
            mIsRestoring = savedInstanceState.getBoolean(RESTORE,false);
            mIsInstall = savedInstanceState.getBoolean(INSTALL,false);
            mIsContactStart = savedInstanceState.getBoolean(ISCONTACTSTART,false);
            mIsSmsStart = savedInstanceState.getBoolean(ISSMSSTART,false);
            mIsCalllogStart = savedInstanceState.getBoolean(ISCALLLOGSTART,false);
            mIsBookmarkStart = savedInstanceState.getBoolean(ISBOOKMARKSTART,false);
            mIsCalendarStart = savedInstanceState.getBoolean(ISCALENDARSTART,false);
            mIsPictureStart = savedInstanceState.getBoolean(ISPICTURESTART,false);
            mIsVideoStart = savedInstanceState.getBoolean(ISVIDIEOSTART,false);
            mIsMusicStart = savedInstanceState.getBoolean(ISMUSICSTART,false);
            mIsAppStart = savedInstanceState.getBoolean(ISAPPSTART,false);
            if (mIsInstall) {
                getApkFiles(mApks,SOFT_PATH);
            }
            restoreUI();
        }
    }

    private void restoreUI() {

        if (mIsRestoring) {
            mRestoreIcon.setVisibility(View.VISIBLE);
            mEndIcon.setVisibility(View.GONE);
            mStatusText.setText(R.string.text_restore);
        }
        if (mIsInstall) {
            mRestoreIcon.setVisibility(View.GONE);
            mInstallItem.setVisibility(View.VISIBLE);
            TextView status = (TextView)mInstallItem.findViewById(R.id.receive_status);
            status.setVisibility(View.INVISIBLE);
            mEndIcon.setVisibility(View.VISIBLE);
            mStatusText.setText(R.string.qq_start_install);
        }
    }

    @Override
    public void notifyMessage(Message message) {
        if (!mIsDisconnect) {
            honeyCombHandler.handleMessage(message);
        }
    }

    Handler honeyCombHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg == null) {
                return;
            }
            LogUtils.d(TAG,"transfer msg.what="+msg.what);
            switch (msg.what) {
                //in transfer
                case MessageIdDef.PROGRESS_CHANGE: {
                    TransferStatusMsg data = (TransferStatusMsg) msg.obj;
                    LogUtils.d(TAG,"transfer data.getDataType()="+data.getDataType());
                    if (data.getStatus() == UTransferState.TRANSFER_DATA_TRANSFERING) {
                        switch (data.getDataType()) {
                            case TRANSFER_BOOKMARK: {
                                mHasData = true;
                                updateUI(mBookmarkItem,data);
                                break;
                            }
                            case TRANSFER_CALENDAR: {
                                mHasData = true;
                                updateUI(mCalendarItem,data);
                                break;
                            }
                            case TRANSFER_CALLLOG: {
                                mHasData = true;
                                updateUI(mCalllogItem,data);
                                break;
                            }
                            case TRANSFER_CONTACT: {
                                mHasData = true;
                                updateUI(mContactItem,data);
                                break;
                            }
                            case TRANSFER_CONTACT_PHOTO: {
                                break;
                            }
                            case TRANSFER_MUSIC: {
                                updateUI(mMusicItem,data);
                                break;
                            }
                            case TRANSFER_PHOTO: {
                                updateUI(mPictureItem,data);
                                break;
                            }
                            case TRANSFER_SMS: {
                                mHasData = true;
                                updateUI(mSmsItem,data);
                                break;
                            }
                            case TRANSFER_SOFTWARE: {
                                mHasApp = true;
                                updateUI(mAppItem,data);
                                break;
                            }
                            case TRANSFER_VIDEO: {
                                updateUI(mVideoItem,data);
                                break;
                            }
                            case TRANSFER_NONE: {
                                break;
                            }
                        }
                    } else if (data.getStatus() == UTransferState.TRANSFER_DATA_END) {
                        updateend(data);
                    } else if (data.getStatus() == UTransferState.TRANSFER_DATA_BEGIN) {
                    } else if (data.getStatus() == UTransferState.TRANSFER_ALL_BEGIN) {
                    } else if (data.getStatus() == UTransferState.TRANSFER_ALL_END) {
                        mStatusIcon.stopRippleAnimation();
                        mStatusIcon.setVisibility(View.GONE);
                        mBtn_bottom.setText(R.string.button_text_complete);
                        LogUtils.d(TAG,"transfer mHasData="+mHasData);
                        LogUtils.d(TAG,"transfer mHasApp="+mHasApp);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                                + ScanActivity.PIC_PATH)));
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                                + ScanActivity.MUSIC_PATH)));
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                                + ScanActivity.VIDEO_PATH)));
                        if (data.getFinalResult() == UTransferRes.TRANSFER_FAILED) {
                            ClearUI();
                            mEndIcon.setVisibility(View.VISIBLE);
                            mStatusText.setText(R.string.qq_text_receive_fail);
                            mHoneyComb.interruptTransferData();
                            mHoneyComb.reset();
                            mIsTransferFinish = true;
                            Intent intent = new Intent(TransferActivity.this, DisconnectActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra(Utils.IS_FAIL, true);
                            startActivity(intent);
                        } else if (data.getFinalResult() == UTransferRes.TRANSFER_CANCEL) {
                            mHoneyComb.interruptTransferData();
                            mHoneyComb.reset();
                            mIsDisconnect = true;
                            Intent intent = new Intent(TransferActivity.this, DisconnectActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            TransferActivity.this.finish();
                        } else {
                            mStatusText.setText(R.string.qq_text_received);
                            if (!mHasData && mHasApp) {
                                ClearUI();
                                mRestoreIcon.setVisibility(View.GONE);
                                mEndIcon.setVisibility(View.VISIBLE);
                                installApp();
                            } else if (!mHasData) {
                                mEndIcon.setVisibility(View.VISIBLE);
                                Intent intent = new Intent(TransferActivity.this, CompleteActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(Utils.IS_SEND, false);
                                startActivity(intent);
                            }
                            if (mHasData) {
                                Toast.makeText(TransferActivity.this, R.string.text_restore, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    break;
                }
            }
        }
    };

    private void ClearUI() {
        mBookmarkItem.setVisibility(View.GONE);
        mCalendarItem.setVisibility(View.GONE);
        mAppItem.setVisibility(View.GONE);
        mCalllogItem.setVisibility(View.GONE);
        mContactItem.setVisibility(View.GONE);
        mMusicItem.setVisibility(View.GONE);
        mPictureItem.setVisibility(View.GONE);
        mSmsItem.setVisibility(View.GONE);
        mVideoItem.setVisibility(View.GONE);
    }

    private class ImportDataProcess implements ILogicObsv {
        @Override
        public void notifyMessage(Message msg) {
            if (!mIsDisconnect && !mIsTransferFinish) {
                showImportProgress((TransferStatusMsg) msg.obj);
            }
        }
    }
    private void showImportProgress(TransferStatusMsg msg) {
        if (msg.getStatus() == UTransferState.TRANSFER_DATA_TRANSFERING) {
            mIsRestoring = true;
            mStatusText.setText(R.string.text_restore);
            mRestoreIcon.setVisibility(View.VISIBLE);
        } else if (msg.getStatus() == UTransferState.TRANSFER_DATA_END) {
        } else if (msg.getStatus() == UTransferState.TRANSFER_DATA_BEGIN) {
        } else if (msg.getStatus() == UTransferState.TRANSFER_ALL_BEGIN) {
            mIsRestoring = true;
            mStatusText.setText(R.string.text_restore);
            mRestoreIcon.setVisibility(View.VISIBLE);
        } else if (msg.getStatus() == UTransferState.TRANSFER_ALL_END) {
            ClearUI();
            mIsRestoring = false;
            mStatusText.setText(R.string.qq_text_received);
            mRestoreIcon.setVisibility(View.GONE);
            mEndIcon.setVisibility(View.VISIBLE);
            if (mHasApp) {
                mEndIcon.setVisibility(View.VISIBLE);
                mRestoreIcon.setVisibility(View.GONE);
                installApp();
            } else {
                mIsTransferFinish = true;
                mBtn_bottom.setText(R.string.button_text_complete);
                Intent intent = new Intent(TransferActivity.this, CompleteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(Utils.IS_SEND, false);
                startActivity(intent);
            }
        }
    }
    @Override
    public void onNavigationClicked(View view) {
        if (mIsTransferFinish) {
            mHoneyComb.interruptTransferData();
            mHoneyComb.reset();
            finish();
        } else {
            moveTaskToBack(true);
        }
    }

    private void updateend(TransferStatusMsg data) {
        TextView receivestatus;
        switch (data.getDataType()) {
            case TRANSFER_BOOKMARK: {
                receivestatus = (TextView)mBookmarkItem.findViewById(R.id.receive_status);
                receivestatus.setText(String.format(getResources().getString(R.string.total_size),getCurrentSize(data.getTotal())));
                break;
            }
            case TRANSFER_CALENDAR: {
                receivestatus = (TextView)mCalendarItem.findViewById(R.id.receive_status);
                receivestatus.setText(String.format(getResources().getString(R.string.total_size),getCurrentSize(data.getTotal())));
                break;
            }
            case TRANSFER_CALLLOG: {
                receivestatus = (TextView)mCalllogItem.findViewById(R.id.receive_status);
                receivestatus.setText(String.format(getResources().getString(R.string.total_size),getCurrentSize(data.getTotal())));
                break;
            }
            case TRANSFER_CONTACT: {
                receivestatus = (TextView)mContactItem.findViewById(R.id.receive_status);
                receivestatus.setText(String.format(getResources().getString(R.string.total_size),getCurrentSize(data.getTotal())));
                break;
            }
            case TRANSFER_CONTACT_PHOTO: {
                break;
            }
            case TRANSFER_MUSIC: {
                receivestatus = (TextView)mMusicItem.findViewById(R.id.receive_status);
                receivestatus.setText(String.format(getResources().getString(R.string.total_size),getCurrentSize(data.getTotal())));
                break;
            }
            case TRANSFER_PHOTO: {
                receivestatus = (TextView)mPictureItem.findViewById(R.id.receive_status);
                receivestatus.setText(String.format(getResources().getString(R.string.total_size),getCurrentSize(data.getTotal())));
                break;
            }
            case TRANSFER_SMS: {
                receivestatus = (TextView)mSmsItem.findViewById(R.id.receive_status);
                receivestatus.setText(String.format(getResources().getString(R.string.total_size),getCurrentSize(data.getTotal())));
                break;
            }
            case TRANSFER_SOFTWARE: {
                receivestatus = (TextView)mAppItem.findViewById(R.id.receive_status);
                receivestatus.setText(String.format(getResources().getString(R.string.total_size),getCurrentSize(data.getTotal())));
                break;
            }
            case TRANSFER_VIDEO: {
                receivestatus = (TextView)mVideoItem.findViewById(R.id.receive_status);
                receivestatus.setText(String.format(getResources().getString(R.string.total_size),getCurrentSize(data.getTotal())));
                break;
            }
            case TRANSFER_NONE: {
                break;
            }
        }
    }
    private void updateUI(View view,TransferStatusMsg data) {
        ProgressBar progress;
        TextView typetext = (TextView) view.findViewById(R.id.type_name);
        ImageView typeimage = (ImageView) view.findViewById(R.id.icon);
        TextView receivestatus = (TextView)view.findViewById(R.id.receive_status);
        TextView receivesize = (TextView)view.findViewById(R.id.receive_size);
        ImageView finishicon = (ImageView)view.findViewById(R.id.finish_mark);
        progress = (ProgressBar)view.findViewById(R.id.progress);
        progress.setProgress(data.getProgress());
        String typename="";
        switch (data.getDataType()) {
            case TRANSFER_BOOKMARK: {
                if (mBookmarkItem.getVisibility() == View.VISIBLE) {
                    if (!mIsBookmarkStart) {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        mIsBookmarkStart = true;
                    }
                }
                receivestatus.setText(R.string.recving);
                typetext.setText(R.string.qq_text_bookmark);
                receivesize.setText(String.format(getResources().getString(R.string.recv_size),data.getProgress()+"%"));
                typeimage.setImageResource(R.drawable.bookmark_icon);
                typename = getResources().getString(R.string.qq_text_bookmark);
                break;
            }
            case TRANSFER_CALENDAR: {
                if (mCalendarItem.getVisibility() == View.VISIBLE) {
                    if (!mIsCalendarStart) {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        mIsCalendarStart = true;
                    }
                }
                receivestatus.setText(R.string.recving);
                typetext.setText(R.string.qq_text_calendar);
                receivesize.setText(String.format(getResources().getString(R.string.recv_size),data.getProgress()+"%"));
                typeimage.setImageResource(R.drawable.calendar_icon);
                typename = getResources().getString(R.string.qq_text_calendar);
                break;
            }
            case TRANSFER_CALLLOG: {
                if (mCalllogItem.getVisibility() == View.VISIBLE) {
                    if (!mIsCalllogStart) {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        mIsCalllogStart = true;
                    }
                }
                receivestatus.setText(R.string.recving);
                typetext.setText(R.string.qq_text_calllog);
                receivesize.setText(String.format(getResources().getString(R.string.recv_size),data.getProgress()+"%"));
                typeimage.setImageResource(R.drawable.calllog_icon);
                typename = getResources().getString(R.string.qq_text_calllog);
                break;
            }
            case TRANSFER_CONTACT: {
                if (mContactItem.getVisibility() == View.VISIBLE) {
                    if (!mIsContactStart) {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        mIsContactStart = true;
                    }
                }
                receivestatus.setText(R.string.recving);
                typetext.setText(R.string.qq_text_contact);
                receivesize.setText(String.format(getResources().getString(R.string.recv_size),data.getProgress()+"%"));
                typeimage.setImageResource(R.drawable.contact_icon);
                typename = getResources().getString(R.string.qq_text_contact);
                break;
            }
            case TRANSFER_CONTACT_PHOTO: {
                break;
            }
            case TRANSFER_MUSIC: {
                if (mMusicItem.getVisibility() == View.VISIBLE) {
                    if (!mIsMusicStart) {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        mIsMusicStart = true;
                    }
                }
                receivestatus.setText(data.getFileName());
                receivesize.setText(String.format(getResources().getString(R.string.recv_size),getCurrentSize(data.getCurrent())));
                typetext.setText(R.string.qq_text_music);
                typeimage.setImageResource(R.drawable.qq_music);
                typename = getResources().getString(R.string.qq_text_music);
                break;
            }
            case TRANSFER_PHOTO: {
                if (mPictureItem.getVisibility() == View.VISIBLE) {
                    if (!mIsPictureStart) {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        mIsPictureStart = true;
                    }
                }
                receivestatus.setText(data.getFileName());
                receivesize.setText(String.format(getResources().getString(R.string.recv_size),getCurrentSize(data.getCurrent())));
                typetext.setText(R.string.qq_text_picture);
                typeimage.setImageResource(R.drawable.qq_picture);
                typename = getResources().getString(R.string.qq_text_picture);
                break;
            }
            case TRANSFER_SMS: {
                if (mSmsItem.getVisibility() == View.VISIBLE) {
                    if (!mIsSmsStart) {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        mIsSmsStart = true;
                    }
                }
                receivestatus.setText(R.string.recving);
                typetext.setText(R.string.qq_text_sms);
                receivesize.setText(String.format(getResources().getString(R.string.recv_size),data.getProgress()+"%"));
                typeimage.setImageResource(R.drawable.sms_icon);
                typename = getResources().getString(R.string.qq_text_sms);
                break;
            }
            case TRANSFER_SOFTWARE: {
                if (mAppItem.getVisibility() == View.VISIBLE) {
                    if (!mIsAppStart) {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        mIsAppStart = true;
                    }
                }
                receivestatus.setText(data.getFileName());
                receivesize.setText(String.format(getResources().getString(R.string.recv_size),getCurrentSize(data.getCurrent())));
                typetext.setText(R.string.qq_text_app);
                typeimage.setImageResource(R.drawable.soft_icon);
                typename = getResources().getString(R.string.qq_text_app);
                break;
            }
            case TRANSFER_VIDEO: {
                if (mVideoItem.getVisibility() == View.VISIBLE) {
                    if (!mIsVideoStart) {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        mIsVideoStart = true;
                    }
                }
                receivestatus.setText(data.getFileName());
                receivesize.setText(String.format(getResources().getString(R.string.recv_size),getCurrentSize(data.getCurrent())));
                typetext.setText(R.string.qq_text_video);
                typeimage.setImageResource(R.drawable.qq_video);
                typename = getResources().getString(R.string.qq_text_video);
                break;
            }
            case TRANSFER_NONE: {
                break;
            }
        }
        if (data.getRemainTime() >= 0) {
            String time = transTime(data.getRemainTime());
            mStatusText.setText(getResources().getString(R.string.text_transfering) + typename+getResources().getString(R.string.text_remain_time) + time);
        } else {
            mStatusText.setText("");
        }
        if (data.getProgress() == 100) {
            finishicon.setVisibility(View.VISIBLE);
            receivesize.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.INVISIBLE);
            receivestatus.setText(R.string.qq_text_received);
        } else {
            finishicon.setVisibility(View.INVISIBLE);
            receivesize.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
        view.setVisibility(View.VISIBLE);
    }

    private String transTime(int time) {
        int day=time/(60*60*24);
        int hour=(time-(60*60*24*day))/3600;
        int minute=(time-60*60*24*day-3600*hour)/60;
        int second=time-60*60*24*day-3600*hour-60*minute;
        if (day > 0) {
            return day + getResources().getString(R.string.text_day) + hour + getResources().getString(R.string.text_hour) + minute
                    + getResources().getString(R.string.text_min) + second + getResources().getString(R.string.text_sec);
        } else if (hour > 0) {
            return hour + getResources().getString(R.string.text_hour) + minute
                    + getResources().getString(R.string.text_min) + second + getResources().getString(R.string.text_sec);
        } else if (minute >0) {
            return minute + getResources().getString(R.string.text_min) + second + getResources().getString(R.string.text_sec);
        } else {
            return second + getResources().getString(R.string.text_sec);
        }
    }

    class DeletePostTask extends AsyncTask<Void,String,Boolean> {
        private Context context;
        DeletePostTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            ImageView image = (ImageView)mInstallItem.findViewById(R.id.restore);
            image.clearAnimation();
            mInstallItem.setVisibility(View.GONE);
            mIsTransferFinish = true;
            mBtn_bottom.setText(R.string.button_text_complete);
            mHoneyComb.reset();
            Intent intent = new Intent(TransferActivity.this, CompleteActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(Utils.IS_SEND, false);
            startActivity(intent);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            deleteAllFiles(new File(SOFT_PATH));
            return true;
        }
    }
    private String getAppName(File file) {
        String apk_path = file.getAbsolutePath();
        LogUtils.d(TAG,"apk_path:"+apk_path);
        PackageManager pm = getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(apk_path, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            appInfo.sourceDir = apk_path;
            appInfo.publicSourceDir = apk_path;
            LogUtils.d(TAG, "app name:" + appInfo.loadLabel(pm).toString());
            return appInfo.loadLabel(pm).toString();
        } else {
            return "";
        }
    }
    private String getPackageName(File file) {
        String apk_path = file.getAbsolutePath();
        LogUtils.d(TAG,"apk_path:"+apk_path);
        PackageManager pm = getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(apk_path, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            return packageInfo.packageName;
        } else {
            return "";
        }
    }
    private void installApp() {
        mIsInstall = true;
        Toast.makeText(this, R.string.notify_installing, Toast.LENGTH_SHORT).show();
        mStatusText.setText(R.string.qq_start_install);
        mInstallItem.setVisibility(View.VISIBLE);
        TextView status = (TextView)mInstallItem.findViewById(R.id.receive_status);
        status.setText(getResources().getString(R.string.text_pre_install));
        status.setVisibility(View.VISIBLE);
        ImageView image = (ImageView)mInstallItem.findViewById(R.id.restore);
        Animation animation= AnimationUtils.loadAnimation(TransferActivity.this, R.anim.tip);
        LinearInterpolator lin = new LinearInterpolator();
        animation.setInterpolator(lin);
        if (animation != null) {
            image.startAnimation(animation);
        }
        if (mBtn_bottom != null) {
            mBtn_bottom.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (mIsTransferFinish) {
                        mHoneyComb.interruptTransferData();
                        mHoneyComb.reset();
                        finish();
                    } else {
                        moveTaskToBack(true);
                    }
                }
            });
        }
        getApkFiles(mApks,SOFT_PATH);
        LogUtils.d(TAG,"transfer mApks.size()="+mApks.size());
        if (mApks.size()>0) {
            mCurrent = 0;
            File file = mApks.get(mCurrent);
            if (getApplicationInfo().packageName == getPackageName(file)) {
                Message installmsg = mInstallHandler.obtainMessage(INSTALL_COMPLETED);
                mInstallHandler.sendMessage(installmsg);
                return;
            }
            if (AppUtils.checkNeedInstallByPackageName(TransferActivity.this,file.getAbsolutePath(),getPackageName(file))) {
                installApk(file.getAbsolutePath());
                LogUtils.d(TAG, "values:" + getAppName(file));
                status.setText(getResources().getString(R.string.text_install) + getAppName(file) + ELLIPSIS);
                status.setVisibility(View.VISIBLE);
            } else {
                Message installmsg = mInstallHandler.obtainMessage(INSTALL_COMPLETED);
                mInstallHandler.sendMessage(installmsg);
                return;
            }
        } else {
            image.clearAnimation();
            mInstallItem.setVisibility(View.GONE);
            mIsTransferFinish = true;
            mBtn_bottom.setText(R.string.button_text_complete);
            mHoneyComb.reset();
            Intent intent = new Intent(TransferActivity.this, CompleteActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(Utils.IS_SEND, false);
            startActivity(intent);
        }
    }
    private void getApkFiles(ArrayList<File> fileList, String path) {
        File[] allFiles = new File(path).listFiles();
        if (allFiles == null) {
            return;
        }
        for (File file:allFiles) {
            if (file.isFile()) {
                if (file.getName().toLowerCase().endsWith(".apk")) {
                    fileList.add(file);
                }
            }
        }
    }

    private void installApk(String apkAbsolutePath) {
        try {
            Class<?> pmService;
            Class<?> activityTherad;
            Method method;

            activityTherad = Class.forName("android.app.ActivityThread");
            Class<?> paramTypes[] = getParamTypes(activityTherad , "getPackageManager");
            method = activityTherad.getMethod("getPackageManager", paramTypes);
            Object PackageManagerService = method.invoke(activityTherad);

            pmService = PackageManagerService.getClass();
            getPackageManager();


            Class<?> paramTypes1[] = getParamTypes(pmService , "installPackageAsUser");
            method = pmService.getMethod("installPackageAsUser", paramTypes1);
            method.invoke(PackageManagerService, apkAbsolutePath, mLocalObserver, PackageManager.INSTALL_REPLACE_EXISTING, null, 0);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Exception when install", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Exception when install", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Exception when install", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Exception when install", e);
        }catch (ClassNotFoundException e1) {
            Log.e(TAG, "Exception when install", e1);
        } catch (Exception e) {
            Log.e(TAG, "Exception when install", e);
        }
    }
    private Class<?>[] getParamTypes(Class<?> cls, String mName) {
        Class<?> cs[] = null;

        Method[] mtd = cls.getMethods();

        for (int i = 0; i < mtd.length; i++) {
            if (!mtd[i].getName().equals(mName)) {
                continue;
            }
            cs = mtd[i].getParameterTypes();
        }
        return cs;
    }

    private Uri getPackageUri(String apkAbsolutePath)
    {
        File file = new File(apkAbsolutePath);
        return Uri.fromFile(file);
    }

    private String getCurrentSize(int size) {
        long mb = 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else {
            return String.format(size > 100 ? "%.0f KB" : "%.1f KB", (float)size);
        }
    }

    @Override
    public void onBackPressed() {
        if (mIsTransferFinish) {
            mHoneyComb.interruptTransferData();
            mHoneyComb.reset();
            finish();
        } else {
            moveTaskToBack(true);
        }
    }

    private void deleteAllFiles(File root) {
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                        Log.e(TAG,"Exception when delete file:", e);
                    }
                } else {
                    if (f.exists()) {
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                            Log.e(TAG,"Exception when delete file:", e);
                        }
                    }
                }
            }
    }

    private IPackageInstallObserver2 mLocalObserver = new IPackageInstallObserver2.Stub() {
        @Override
        public void onUserActionRequired(Intent intent) {
                throw new IllegalStateException();
            }

        @Override
        public void onPackageInstalled(String basePackageName, int returnCode, String msg,
                    Bundle extras) {
            Message installmsg = mInstallHandler.obtainMessage(INSTALL_COMPLETED);
            installmsg.arg1 = returnCode;
            mInstallHandler.sendMessage(installmsg);
        }
    };

    Handler mInstallHandler=new Handler(){
        public void handleMessage(Message msg) {
            switch(msg.what){
                case INSTALL_COMPLETED:
                    if(mCurrent < mApks.size() - 1){
                        mCurrent ++;
                        File file = mApks.get(mCurrent);
                        if (getApplicationInfo().packageName == getPackageName(file)) {
                            Message installmsg = mInstallHandler.obtainMessage(INSTALL_COMPLETED);
                            mInstallHandler.sendMessage(installmsg);
                            break;
                        }
                        if (AppUtils.checkNeedInstallByPackageName(TransferActivity.this,file.getAbsolutePath(),getPackageName(file))) {
                            installApk(file.getAbsolutePath());
                            TextView status = (TextView) mInstallItem.findViewById(R.id.receive_status);
                            LogUtils.d(TAG, "values:" + getAppName(file));
                            status.setText(getResources().getString(R.string.text_install) + getAppName(file) + ELLIPSIS);
                            status.setVisibility(View.VISIBLE);
                        } else {
                            Message installmsg = mInstallHandler.obtainMessage(INSTALL_COMPLETED);
                            mInstallHandler.sendMessage(installmsg);
                            return;
                        }
                    } else if (mCurrent == mApks.size() - 1) {
                        DeletePostTask task = new DeletePostTask(TransferActivity.this);
                        task.execute();
                    }
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT,mCurrent);
        outState.putBoolean(HASAPP,mHasApp);
        outState.putBoolean(HASDATA,mHasData);
        outState.putBoolean(DISCONNECT,mIsDisconnect);
        outState.putBoolean(RESTORE,mIsRestoring);
        outState.putBoolean(TRANSFERFINISH,mIsTransferFinish);
        outState.putBoolean(INSTALL,mIsInstall);
        outState.putBoolean(ISAPPSTART,mIsAppStart);
        outState.putBoolean(ISBOOKMARKSTART,mIsBookmarkStart);
        outState.putBoolean(ISCALENDARSTART,mIsCalendarStart);
        outState.putBoolean(ISCONTACTSTART,mIsContactStart);
        outState.putBoolean(ISCALLLOGSTART,mIsCalllogStart);
        outState.putBoolean(ISMUSICSTART,mIsMusicStart);
        outState.putBoolean(ISPICTURESTART,mIsPictureStart);
        outState.putBoolean(ISSMSSTART,mIsSmsStart);
        outState.putBoolean(ISVIDIEOSTART,mIsVideoStart);
        super.onSaveInstanceState(outState);
    }

    private PowerManager.WakeLock wakeLock;

    private void acquireWakeLock(){
        if (null == wakeLock) {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "keepWifiOn");
            if (null != wakeLock){
                wakeLock.acquire();
            }
        }
    }

    private void releaseWakeLock() {
        if (null != wakeLock){
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
    }
}
