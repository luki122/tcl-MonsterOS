/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.transfer.File_Exchange;
import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.R;
import cn.tcl.transfer.fasttransfer.SendBackground;
import cn.tcl.transfer.send.ISendInfo;
import cn.tcl.transfer.send.SendBackupDataService;
import cn.tcl.transfer.util.DataManager;
import cn.tcl.transfer.util.DialogBuilder;
import cn.tcl.transfer.util.FilePathUtils;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.NotificationUtils;
import cn.tcl.transfer.util.Utils;

public class SendingActivity extends BaseActivity {

    private static final String TAG = "SendingActivity";

    List<SendItem> mAllList = new ArrayList<SendItem>();

    private static final int OPERATE_PROGRESS = 1000;
    private static final int OPERATE_SUCESS = 10001;
    private static final int OPERATE_CANCEL = 10002;
    private static final int OPERATE_ERROR = 10003;
    private static final int UPDATE_TIME = 10004;
    private static final int ITEMHEIGHT = 64;

    private SendItem mSysSendItem;
    private SendItem mAppSendItem;
    private SendItem mImageSendItem;
    private SendItem mVideoSendItem;
    private SendItem mAudioSendItem;
    private SendItem mDocSendItem;

    private TextView mLeftTimeView;
    private ScrollView mScrollView;

    private SendBackground mStatusIcon;

    private volatile float mSendSpeed = Utils.TRANSFER_SPEED;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case OPERATE_SUCESS:
                    Intent intent = new Intent(SendingActivity.this, CompleteActivity.class);
                    intent.putExtra(Utils.IS_SEND, true);
                    startActivity(intent);
                    finish();
                    break;
                case OPERATE_CANCEL:
                    finish();
                    break;
                case OPERATE_ERROR:
                    Intent intent1 = new Intent();
                    intent1.setClass(getApplicationContext(), DisconnectActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent1);
                    finish();
                case UPDATE_TIME:
                    updateLeftTimeView();
                    mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 10 * 1000);
                    break;
            }
        }
    };

    private ICallback.Stub  mCallBack = new ICallback.Stub() {
        @Override
        public void onStart(int type) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAllList.get(type).updateSendStatus(SendItem.SENDING);
                }
            });
        }

        @Override
        public void onProgress(int type, long size, long speed) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAllList.get(type).updateSendSize(size);
                }
            });
        }

        @Override
        public void onFileBeginSend(int type, String fileName) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAllList.get(type).updateSendFileName(fileName);
                }
            });
        }

        @Override
        public void onComplete(int type) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAllList.get(type).sendStatus = SendItem.SEND_OK;
                    mAllList.get(type).updateView();
                    moveScrollToDest(type);
                }
            });
        }

        @Override
        public void onError(int type, String reason) {
            mHandler.sendEmptyMessage(OPERATE_ERROR);
        }

        @Override
        public void onAllComplete() throws RemoteException {
            mHandler.sendEmptyMessage(OPERATE_SUCESS);
        }

        @Override
        public void onCancel() throws RemoteException {
            mHandler.sendEmptyMessage(OPERATE_CANCEL);
        }
    };

    private void moveScrollToDest(int type) {
        float density = getResources().getDisplayMetrics().density;
        int scrollY = (int)(density * ITEMHEIGHT * (type + 1));
        mScrollView.scrollTo(0 , scrollY);
    }


    private long getLeftSize() {
        long leftSize = 0;
        for (SendItem item : mAllList) {
            if(item.sendStatus != SendItem.SEND_OK) {
                leftSize += item.totalSize - item.sendSize;
            }
        }
        return leftSize;
    }

    private void updateLeftTimeView() {

        long leftSize = getLeftSize();
        long currentTime = System.currentTimeMillis();
        if(SendBackupDataService.mCurrentTotalSendSize != 0) {
            mSendSpeed = SendBackupDataService.mCurrentTotalSendSize * 1000 / (currentTime - SendBackupDataService.startSendTime);
        }
        long leftSendTime = (long)(leftSize/mSendSpeed);
        long time = leftSendTime;
        long minutes = 0;
        if(leftSize > 0) {
            minutes = time/60;
            if(time%60 > 0) {
                minutes += 1;
            }
        }
        LogUtils.d(TAG, "updateLeftTimeView" + "  mSendSpeed=" + mSendSpeed + "  leftSendTime:" + leftSendTime );
        mLeftTimeView.setText(getString(R.string.remaining_time,  minutes + " " + getString(R.string.text_min)));
    }

    private ISendInfo mRemoteService;
    ServiceConnection mConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            try {
                mRemoteService = ISendInfo.Stub.asInterface(service);
                mRemoteService.registerCallback(mCallBack);
                mRemoteService.sendData();
                LogUtils.i(TAG, "onServiceConnected");
            } catch (Exception e) {
                Log.e(TAG, "onServiceConnected:", e);
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.sending_layout);
        Toast.makeText(this, R.string.notify_sending, Toast.LENGTH_SHORT).show();
        mLeftTimeView = (TextView)findViewById(R.id.secondary_text);
        mStatusIcon = (SendBackground)findViewById(R.id.status_icon);
        mStatusIcon.post(new Runnable() {
            @Override
            public void run() {
                mStatusIcon.init(SendingActivity.this);
                mStatusIcon.startRippleAnimation();
            }
        });
        initView();

        Intent intent = new Intent(this, SendBackupDataService.class);
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        mHandler.sendEmptyMessage(UPDATE_TIME);
    }

    private void initView() {
        String[] categories = getResources().getStringArray(R.array.receiver_content_item);
        mScrollView = (ScrollView)findViewById(R.id.send_scrollview);
        mSysSendItem = new SendItem(categories[Utils.CATEGORY_SYS], R.drawable.system_icon);
        mAppSendItem = new SendItem(categories[Utils.CATEGORY_APP], R.drawable.app_icon);
        mImageSendItem = new SendItem(categories[Utils.CATEGORY_IMAGE], R.drawable.image_icon);
        mVideoSendItem = new SendItem(categories[Utils.CATEGORY_VIDEO], R.drawable.video_icon);
        mAudioSendItem = new SendItem(categories[Utils.CATEGORY_AUDIO], R.drawable.music_icon);
        mDocSendItem = new SendItem(categories[Utils.CATEGORY_DOCUMENT], R.drawable.doc_icon);

        mAllList.add(mSysSendItem);
        mAllList.add(mAppSendItem);
        mAllList.add(mImageSendItem);
        mAllList.add(mVideoSendItem);
        mAllList.add(mAudioSendItem);
        mAllList.add(mDocSendItem);

        FilePathUtils.getPictureFilePath(this);
        if(DataManager.isSysSelect()) {
            mAllList.get(Utils.CATEGORY_SYS).totalSize = DataManager.getSelectSysSize();
        }
        if(DataManager.isAppSelect()) {
            mAllList.get(Utils.CATEGORY_APP).totalSize = DataManager.getSelectAppSize();
        }
        if(DataManager.isImageSelect()) {
            mAllList.get(Utils.CATEGORY_IMAGE).totalSize = FilePathUtils.getFileSize(File_Exchange.TYPE_IMAGE, getApplicationContext());
        }
        if(DataManager.isVideoSelect()) {
            mAllList.get(Utils.CATEGORY_VIDEO).totalSize = FilePathUtils.getFileSize(File_Exchange.TYPE_VIDEO, getApplicationContext());
        }
        if(DataManager.isAudioSelect()) {
            mAllList.get(Utils.CATEGORY_AUDIO).totalSize = FilePathUtils.getFileSize(File_Exchange.TYPE_AUDIO, getApplicationContext());
        }
        if(DataManager.isDocSelect()) {
            mAllList.get(Utils.CATEGORY_DOCUMENT).totalSize = FilePathUtils.getFileSize(File_Exchange.TYPE_DOCUMENT, getApplicationContext());
        }
        mSysSendItem.initView(findViewById(R.id.sys_view));
        mAppSendItem.initView(findViewById(R.id.app_view));
        mImageSendItem.initView(findViewById(R.id.image_view));
        mVideoSendItem.initView(findViewById(R.id.video_view));
        mAudioSendItem.initView(findViewById(R.id.audio_view));
        mDocSendItem.initView(findViewById(R.id.doc_view));
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCancelDialog();
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mRemoteService.unregisterCallback(mCallBack);
        } catch (RemoteException e) {
            LogUtils.e(TAG, "onDestroy", e);
        }
        unbindService(mConn);
    }

    private void cancelSending() {
        try {
            mRemoteService.cancelSend();
        } catch (RemoteException e) {
            LogUtils.e(TAG, "cancelSending", e);
        }
    }

    private void showCancelDialog() {

        DialogBuilder.createConfirmDialog(this, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                cancelSending();
                Intent intent = new Intent(SendingActivity.this, DisconnectActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                SendingActivity.this.finish();
            }
        }, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }, getString(R.string.cancel_confirm_info)).show();
    }

    private class ViewHolder {
        public TextView title;
        public TextView summary;
        public TextView info;
        public ImageView categoryIcon;
        public ImageView expandIcon;
        public ProgressBar progressBar;
        View paddingView;
    }

    private class SendItem {

        public static final int SEND_NO = 0;
        public static final int SENDING = 1;
        public static final int SEND_OK = 2;

        public long totalSize = 0;
        public long sendSize;
        public String title;
        public int sendStatus;
        public int icon;
        public String currentSendInfo;

        public ViewHolder viewHolder = new ViewHolder();
        public View mItemview = null;

        public String getSummary() {

            switch (sendStatus) {
                case SendItem.SEND_NO:
                case SendItem.SEND_OK:
                    return getString(R.string.total_size, Utils.convertFileSize(totalSize));
                case SendItem.SENDING:
                    return getString(R.string.sending);
                default:
                    return "";
            }
        }

        public String getInfo() {

            switch (sendStatus) {
                case SendItem.SEND_NO:
                case SendItem.SEND_OK:
                    return getString(R.string.total_size, Utils.convertFileSize(totalSize));
                case SendItem.SENDING:
                    return currentSendInfo;
                default:
                    return "";
            }
        }

        public String getSendSize() {
            if(sendStatus == SendItem.SENDING) {
                return getString(R.string.send_size, Utils.convertFileSize(sendSize));
            }
            return "";
        }

        public SendItem(String title1, int ic) {
            title = title1;
            icon = ic;
            sendStatus = SEND_NO;
        }

        public void initView(View view) {
            mItemview = view;
            TextView titleView = (TextView)view.findViewById(R.id.title);
            TextView summary = (TextView)view.findViewById(R.id.summary);
            TextView sendInfo = (TextView)view.findViewById(R.id.send_info);
            ImageView categoryIcon = (ImageView)view.findViewById(R.id.category_icon);
            ImageView expandIcon = (ImageView)view.findViewById(R.id.expand);
            ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.send_progress);
            View paddingView = view.findViewById(R.id.sending_top);

            viewHolder.title = titleView;
            viewHolder.summary = summary;
            viewHolder.info = sendInfo;
            viewHolder.categoryIcon = categoryIcon;
            viewHolder.expandIcon = expandIcon;
            viewHolder.progressBar = progressBar;
            viewHolder.paddingView = paddingView;

            viewHolder.categoryIcon.setImageResource(icon);
            updateView();
        }

        public void updateView() {
            if(totalSize == 0) {
                mItemview.setVisibility(View.GONE);
                return;
            }
            updateSendStatus(sendStatus);
            viewHolder.title.setText(title);
            viewHolder.info.setText(getInfo());
            viewHolder.summary.setText(getSendSize());
            viewHolder.progressBar.setMax(100);
            if(totalSize != 0) {
                viewHolder.progressBar.setProgress((int) (sendSize * 100 / totalSize));
            }
        }

        public void updateSendSize(long size) {
            sendSize = size;
            viewHolder.summary.setText(getSendSize());
            if(totalSize != 0) {
                viewHolder.progressBar.setProgress((int) (sendSize * 100 / totalSize));
            }
        }

        public void updateSendFileName(String name) {
            currentSendInfo = name;
            viewHolder.info.setText(getInfo());
        }

        public void updateSendStatus(int status) {
            sendStatus = status;
            switch (sendStatus) {
                case SendItem.SEND_NO:
                    viewHolder.paddingView.setVisibility(View.VISIBLE);
                    viewHolder.info.setVisibility(View.VISIBLE);
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.expandIcon.setVisibility(View.GONE);
                    break;
                case SendItem.SENDING:
                    viewHolder.paddingView.setVisibility(View.GONE);
                    viewHolder.info.setVisibility(View.VISIBLE);
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    viewHolder.expandIcon.setVisibility(View.GONE);
                    break;
                case SendItem.SEND_OK:
                    viewHolder.paddingView.setVisibility(View.VISIBLE);
                    viewHolder.info.setVisibility(View.VISIBLE);
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.expandIcon.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }

    }

    @Override
    public void onNavigationClicked(View view) {
        moveTaskToBack(true);
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        return;
    }
}
