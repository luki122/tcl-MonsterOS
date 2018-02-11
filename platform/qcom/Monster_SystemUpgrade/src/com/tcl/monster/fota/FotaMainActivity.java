package com.tcl.monster.fota;

import android.animation.Animator;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tcl.monster.fota.downloadengine.DownloadTask;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.misc.State;
import com.tcl.monster.fota.model.UpdatePackageInfo;
import com.tcl.monster.fota.ui.GradientTextView;
import com.tcl.monster.fota.ui.anim.FoldingCubeAnimation;
import com.tcl.monster.fota.utils.AlertDialogUtil;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaPref;
import com.tcl.monster.fota.utils.FotaUtil;

import java.lang.ref.WeakReference;

import mst.app.MstActivity;
import mst.app.dialog.ProgressDialog;

/**
 * Main Activity of this application.
 */
public class FotaMainActivity extends MstActivity implements View.OnClickListener {
    /**
     * TAG for Log
     */
    private static final String TAG = "FotaMainActivity";

    /**
     * Intent action extra
     */
    public static final String EXTRA_ACTION = "ACTION";

    /**
     * Download action
     */
    public static final String ACTION_DOWNLOAD = "DOWNLOAD";

    /**
     * Message ID
     */
    private static final int MSG_UPDATE_RESULT = 1001;
    private static final int MSG_UPDATE_DOWNLOAD = 1002;

    /** ContentView */
    private View mContentView;
    private LayoutInflater mInflater;

    /** Check View */
    private ViewGroup mCheckView;
    private ImageView box;
    private ImageView boxLeftTop;
    private ImageView boxRightTop;
    private ImageView boxLeftBottom;
    private ImageView boxRightBottom;

    /** Version View */
    private ViewGroup mVersionView;
    private GradientTextView tvVersionTips;
    private ImageView imgArrow;
    private GradientTextView tvVersionInfo;
    private Button btnUpdate;

    /** Download View */
    private View mDownloadView;
    private TextView tvDownloadProgress;
    private ProgressBar progressDownload;
    private Button btnCancelDownload;
    private Button btnDownload;
    private Button btnInstall;
    private View mDownloadButtonView;
    private View mInstallButtonView;

    /** ProgressDialog for install update package */
    private ProgressDialog mInstallDialog;

    /**
     * Current DownloadTask
     */
    private DownloadTask mTask = null;

    /**
     * Current DownloadTask State
     */
    private State mTaskState = State.UNUSED;

    /**
     * Message Hanlder
     */
    private static MsgHandle mHandler = null;

    /**
     * FotaUIPresenter
     */
    private FotaUIPresenter mFotaUIPresenter;

    /**
     * Flag, Resume from background.
     */
    private boolean mResumeFromBackground = false;

    /**
     * Flag, Download when start activity.
     */
    private boolean mDownloadWhenResume = false;

    /**
     * Version Check Animation
     */
    private FoldingCubeAnimation checkAnimation = null;

    /**
     * Result code for update UI
     */
    public int resultCode = FotaUIPresenter.FOTA_RESULT_TYPE_OK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initAnimation();
    }

    private void initView() {
        getWindow().setStatusBarColor(0x00FFFFFF);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = mInflater.inflate(R.layout.activity_fota_main, null);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(mContentView);

        mCheckView = (ViewGroup) findViewById(R.id.check_view);
        mCheckView.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
        box = (ImageView) findViewById(R.id.img_box);
        boxLeftTop = (ImageView) findViewById(R.id.img_lefttop);
        boxRightTop = (ImageView) findViewById(R.id.img_righttop);
        boxLeftBottom = (ImageView) findViewById(R.id.img_leftbottom);
        boxRightBottom = (ImageView) findViewById(R.id.img_rightbottom);

        mVersionView = (ViewGroup) findViewById(R.id.version_view);
        tvVersionTips = (GradientTextView) findViewById(R.id.tv_version_tips);
        imgArrow = (ImageView) findViewById(R.id.img_arrow);
        tvVersionInfo = (GradientTextView) findViewById(R.id.tv_version_info);
        btnUpdate = (Button) findViewById(R.id.btn_update);
        tvVersionTips.setOnClickListener(this);
        imgArrow.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);

        mDownloadView = findViewById(R.id.download_view);
        tvDownloadProgress = (TextView) findViewById(R.id.tv_download_progress);
        progressDownload = (ProgressBar) findViewById(R.id.download_progressBar);
        btnCancelDownload = (Button) findViewById(R.id.btn_cancel_download);
        btnDownload = (Button) findViewById(R.id.btn_download);
        btnInstall = (Button) findViewById(R.id.btn_install);
        mDownloadButtonView = findViewById(R.id.download_btn_layout);
        mInstallButtonView = findViewById(R.id.install_btn_layout);
        btnCancelDownload.setOnClickListener(this);
        btnDownload.setOnClickListener(this);
        btnInstall.setOnClickListener(this);

        findViewById(R.id.test).setOnClickListener(this);
    }

    private void initData() {
        handleIntent(getIntent());
        mResumeFromBackground = false;
        mFotaUIPresenter = FotaUIPresenter.getInstance(this);
        mHandler = new MsgHandle(new WeakReference<FotaMainActivity>(this));
    }

    private void initAnimation() {
        checkAnimation = new FoldingCubeAnimation(box, boxLeftTop,
                boxRightTop, boxLeftBottom, boxRightBottom);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getStringExtra(EXTRA_ACTION);
        FotaLog.d(TAG, "handleIntent -> action = " + action);
        if (!TextUtils.isEmpty(action) && action.equals(ACTION_DOWNLOAD)) {
            mDownloadWhenResume = true;
        } else {
            mDownloadWhenResume = false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FotaLog.d(TAG, "onResume -> mDownloadWhenResume = " + mDownloadWhenResume
                + ", mResumeFromBackground = " + mResumeFromBackground);
        mFotaUIPresenter.attatchActivity(this);
        if (mDownloadWhenResume) {
            startDownload(true);
        } else if (!mResumeFromBackground) {
            startCheck();
        } else if (mFotaUIPresenter.haveActvieDownloadTask()){
            showDownloadView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumeFromBackground = true;
        mDownloadWhenResume = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFotaUIPresenter.attatchActivity(null);
        mHandler.removeMessages(MSG_UPDATE_RESULT);
        mHandler.removeMessages(MSG_UPDATE_DOWNLOAD);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_version_tips:
            case R.id.img_arrow:
                if (imgArrow.getVisibility() == View.VISIBLE) {
                    startVersionDetailActivity();
                }
                break;
            case R.id.btn_update:
                FotaLog.d(TAG, "onClick -> btn_update resultCode = " + resultCode);
                if (resultCode >= FotaUIPresenter.FOTA_CHECK_RESULT_NO_NETWORK_CONNECTED
                        && resultCode <= FotaUIPresenter.FOTA_CHECK_RESULT_CHECK_ERROR) {
                    startCheck();
                } else {
                    startDownload(true);
                }
                break;
            case R.id.btn_download:
                pauseOrResumeDownload(true);
                break;
            case R.id.btn_cancel_download:
                deleteDownload();
                break;
            case R.id.btn_install:
                startInstall();
                break;
            case R.id.test:
                startActivity(new Intent(FotaMainActivity.this, AdvancedModeActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        mTask = mFotaUIPresenter.getCurrentDownloadTask();
        if (mTask != null && State.valueOf(mTask.getState()) == State.DOWNLOADED) {
            AlertDialogUtil dialogUtil = AlertDialogUtil.getInstance();
            dialogUtil.show(this, R.string.dialog_title_warm,
                    R.string.dialog_msg_exit_install,
                    R.string.dialog_msg_exit_install_cancel, null,
                    R.string.dialog_msg_exit_install_ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    });
        } else {
            super.onBackPressed();
        }
    }

    private void startVersionDetailActivity() {
        String downloadTaskId;
        if (resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_NO_NEW_VERSION) {
            downloadTaskId = FotaVersionDetailActivity.CURRENT_VERSION_ID;
        } else {
            downloadTaskId = FotaPref.getInstance(FotaMainActivity.this)
                    .getString(FotaConstants.DOWNLOAD_ID, "");
        }
        FotaLog.d(TAG, "startVersionDetailActivity -> downloadTaskId = " + downloadTaskId);
        Intent intent = new Intent(FotaMainActivity.this, FotaVersionDetailActivity.class);
        intent.putExtra(FotaVersionDetailActivity.EXTRA_ID, downloadTaskId);
        FotaMainActivity.this.startActivity(intent);
    }

    private void startCheckAnimation() {
        checkAnimation.addListener(new FoldingCubeAnimation.FoldingCubeAnimationListener() {
            @Override
            public void onAnimationEnd() {
                handleCheckResult();
            }
        });
        checkAnimation.start();
    }

    private void showCheckView() {
        mVersionView.setVisibility(View.GONE);
        mDownloadView.setVisibility(View.GONE);
        mCheckView.setVisibility(View.VISIBLE);
        startCheckAnimation();
    }

    private void showVersionView() {
        mCheckView.setVisibility(View.GONE);
        mDownloadView.setVisibility(View.GONE);
        mVersionView.setVisibility(View.VISIBLE);

        AlertDialogUtil dialogUtil = AlertDialogUtil.getInstance();
        if (resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_NO_NEW_VERSION) {
            btnUpdate.setVisibility(View.INVISIBLE);
            imgArrow.setVisibility(View.INVISIBLE);
            tvVersionTips.setText(R.string.find_no_version);
            tvVersionInfo.setText(FotaUtil.getExtVersion());
            tvVersionTips.addListener(new GradientTextView.GradientAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator var1) {
                    imgArrow.setVisibility(View.VISIBLE);
                }
            });
            tvVersionTips.startAnimation();
            tvVersionInfo.startAnimation();
        } else if (resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_GET_NEW_VERSION) {
            mTask = mFotaUIPresenter.getCurrentDownloadTask();
            tvVersionTips.setText(R.string.find_new_version);
            imgArrow.setVisibility(View.INVISIBLE);
            btnUpdate.setText(R.string.start_update);
            btnUpdate.setVisibility(View.VISIBLE);
            UpdatePackageInfo.UpdateFile updateFile = mTask.getUpdateInfo().mFiles.get(0);
            tvVersionInfo.setText(mTask.getUpdateInfo().mSvn + " | "
                    + FotaUtil.formatSize(updateFile.mFileSize));
            tvVersionTips.addListener(new GradientTextView.GradientAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator var1) {
                    imgArrow.setVisibility(View.VISIBLE);
                }
            });
            tvVersionTips.startAnimation();
            tvVersionInfo.startAnimation();
        } else if (resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_DOWNLOAD_VERSION_INVALID) {
            imgArrow.setVisibility(View.INVISIBLE);
            tvVersionTips.setText(R.string.find_no_version);
            tvVersionInfo.setText(FotaUtil.getExtVersion());
            btnUpdate.setVisibility(View.INVISIBLE);
            tvVersionTips.addListener(new GradientTextView.GradientAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator var1) {
                    imgArrow.setVisibility(View.VISIBLE);
                }
            });
            tvVersionTips.startAnimation();
            tvVersionInfo.startAnimation();
            dialogUtil.showPositive(this, R.string.dialog_title_warm,
                    R.string.dialog_msg_version_invalid,
                    R.string.ok, null, false);
        } else if( resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_DOWNLOAD_VERSION_DISCARD
                || resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_DOWNLOAD_VERSION_EXPIRED) {
            mTask = mFotaUIPresenter.getCurrentDownloadTask();
            if (mTask != null) {
                tvVersionTips.setText(R.string.find_new_version);
                btnUpdate.setVisibility(View.VISIBLE);
                UpdatePackageInfo.UpdateFile updateFile = mTask.getUpdateInfo().mFiles.get(0);
                tvVersionInfo.setText(mTask.getUpdateInfo().mSvn + " | "
                        + FotaUtil.formatSize(updateFile.mFileSize));
                tvVersionTips.addListener(new GradientTextView.GradientAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator var1) {
                        imgArrow.setVisibility(View.VISIBLE);
                    }
                });
                tvVersionTips.startAnimation();
                tvVersionInfo.startAnimation();
            } else {
                tvVersionTips.setText(R.string.find_no_version);
                tvVersionInfo.setText(FotaUtil.getExtVersion());
                btnUpdate.setVisibility(View.INVISIBLE);
                tvVersionTips.addListener(new GradientTextView.GradientAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator var1) {
                        imgArrow.setVisibility(View.VISIBLE);
                    }
                });
                tvVersionTips.startAnimation();
                tvVersionInfo.startAnimation();
            }

            if (mTask == null) {
                dialogUtil.showPositive(this, R.string.dialog_title_warm,
                        R.string.dialog_msg_version_invalid,
                        R.string.ok, null, false);
            } else if (resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_DOWNLOAD_VERSION_DISCARD) {
                dialogUtil.showPositive(this, R.string.dialog_title_warm,
                        R.string.dialog_msg_version_discard,
                        R.string.ok, null, false);
            } else if (resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_DOWNLOAD_VERSION_EXPIRED) {
                dialogUtil.show(this, R.string.dialog_title_warm,
                        R.string.dialog_msg_version_expired,
                        R.string.cancel_download, null,
                        R.string.dialog_msg_version_expired_redownload,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                startDownload(true);
                            }
                        });
            }
        } else if (resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_NO_NETWORK_CONNECTED
                || resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_CONNECT_TIMEOUT
                || resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_SERVER_EXCEPTION
                || resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_CHECK_ERROR ) {
            imgArrow.setVisibility(View.GONE);
            if (resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_NO_NETWORK_CONNECTED) {
                tvVersionTips.setText(R.string.no_network);
                tvVersionInfo.setText(R.string.no_network_info);
            } else if (resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_CONNECT_TIMEOUT) {
                tvVersionTips.setText(R.string.connection_failed);
                tvVersionInfo.setText(R.string.connection_failed_info);
            } else if (resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_SERVER_EXCEPTION) {
                tvVersionTips.setText(R.string.server_exception);
                tvVersionInfo.setText(R.string.pls_try_again_late);
            } else if (resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_CHECK_ERROR) {
                tvVersionTips.setText(R.string.check_error);
                tvVersionInfo.setText(R.string.pls_try_again_late);
            }
            btnUpdate.setText(R.string.try_again);
            btnUpdate.setVisibility(View.VISIBLE);
            tvVersionTips.startAnimation();
            tvVersionInfo.startAnimation();
        }
    }

    private void showDownloadView() {
        if (checkAnimation != null && checkAnimation.isPlaying) {
            return;
        }

        mTask = mFotaUIPresenter.getCurrentDownloadTask();
        mTaskState = mFotaUIPresenter.getCurrentTaskState();
        FotaLog.d(TAG, "showDownloadView -> mTask = " + mTask + ", mTaskState = " + mTaskState);
        if (mTaskState == State.IDLE || mTaskState == State.CHECKED || mTaskState == State.UNUSED) {
            return;
        }
        mCheckView.setVisibility(View.GONE);
        mVersionView.setVisibility(View.GONE);
        mDownloadView.setVisibility(View.VISIBLE);

        if (mTask != null) {
            if (State.valueOf(mTask.getState()) == State.DOWNLOADED ||
                    State.valueOf(mTask.getState()) == State.DOWNLOADED) {
                mDownloadButtonView.setVisibility(View.GONE);
                mInstallButtonView.setVisibility(View.VISIBLE);
            } else {
                mDownloadButtonView.setVisibility(View.VISIBLE);
                mInstallButtonView.setVisibility(View.GONE);
            }

            Resources r = getResources();
            int progress = FotaUtil.percentFrom(mTask.getCurrentBytes(), mTask.getTotalBytes());
            String sizeInfo = FotaUtil.formatSize(mTask.getCurrentBytes()) + "/"
                    + FotaUtil.formatSize(mTask.getTotalBytes());
            progressDownload.setProgress(progress);
            if (mTaskState == State.STARTING || mTaskState == State.DOWNLOADING) {
                tvDownloadProgress.setText(r.getString(R.string.downloading)
                        + String.valueOf(progress) + "%" + " - " + sizeInfo);
                btnDownload.setText(r.getString(R.string.pause_download));
                btnDownload.setEnabled(true);
            } else if (mTaskState == State.PAUSING) {
                tvDownloadProgress.setText(r.getString(R.string.downloading)
                        + String.valueOf(progress) + "%" + " - " + sizeInfo);
                btnDownload.setText(r.getString(R.string.pausing_download));
                btnDownload.setEnabled(false);
            } else if (mTaskState == State.PAUSED) {
                tvDownloadProgress.setText(r.getString(R.string.download_paused)
                        + String.valueOf(progress) + "%" + " - " + sizeInfo);
                btnDownload.setText(r.getString(R.string.resume_download));
                btnDownload.setEnabled(true);
            } else if (mTaskState == State.RESUMING) {
                btnDownload.setText(r.getString(R.string.resuming_download));
                btnDownload.setEnabled(false);
                tvDownloadProgress.setText(r.getString(R.string.download_paused)
                        + String.valueOf(progress) + "%" + " - " + sizeInfo);
            } else if (mTaskState == State.DOWNLOADED || mTaskState == State.DOWNLOADED) {
                tvDownloadProgress.setText(r.getString(R.string.downloaded)
                        + String.valueOf(progress) + "%" + " - " + sizeInfo);
            }
        }
    }

    public void startCheck() {
        if (!FotaUtil.isOnline(this)) {
            updateResult(FotaUIPresenter.FOTA_CHECK_RESULT_NO_NETWORK_CONNECTED);
            return;
        }
        showCheckView();
        mFotaUIPresenter.scheduleCheck(FotaConstants.FOTA_CHECK_TYPE_VALUE_MANUAL);
    }

    private void startDownload(boolean onlyWifi) {
        btnUpdate.setText(getResources().getString(R.string.ready_download));
        mFotaUIPresenter.scheduleStartDownload(onlyWifi);
    }

    public void pauseOrResumeDownload(boolean onlyWifi) {
        mTaskState = mFotaUIPresenter.getCurrentTaskState();
        if (mTaskState == State.PAUSED) {
            mFotaUIPresenter.scheduleResumeDownload(onlyWifi);
        } else {
            mFotaUIPresenter.schedulePauseDownload();
        }
    }

    private void deleteDownload() {
        AlertDialogUtil dialogUtil = AlertDialogUtil.getInstance();

        dialogUtil.show(this, R.string.dialog_title_warm,
                R.string.dialog_msg_delete_download,
                R.string.dialog_msg_resume_download, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }, R.string.dialog_msg_cancel_download,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mFotaUIPresenter.scheduleDeleteCurrentTask(false);
                        showCheckView();
                    }
                });
    }

    private void startInstall() {
        AlertDialogUtil dialogUtil = AlertDialogUtil.getInstance();
        dialogUtil.show(this, R.string.dialog_title_warm,
                R.string.dialog_msg_restart_and_install,
                R.string.dialog_msg_restart_and_install_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }, R.string.dialog_msg_restart_and_install_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean online = (FotaUtil.isWifiOnline(FotaMainActivity.this)
                                || FotaUtil.isMobileOnline(FotaMainActivity.this));
                        showInstallProgressDiloag();
                        if (!online) {
                            mFotaUIPresenter.scheduleStartInstall();
                        } else {
                            mFotaUIPresenter.scheduleCheck(FotaConstants.FOTA_CHECK_TYPE_VALUE_INSTALL);
                        }
                    }
                });
    }

    private void showInstallProgressDiloag() {
        mInstallDialog = new ProgressDialog(this);
        mInstallDialog.setMessage(getString(R.string.dialog_msg_install_verify));
        mInstallDialog.setCancelable(false);
        mInstallDialog.show();
    }

    private static class MsgHandle extends Handler {
        WeakReference<FotaMainActivity> activityRef;
        MsgHandle(WeakReference<FotaMainActivity> ref){
            activityRef = ref;
        }
        @Override
        public void handleMessage(Message msg) {
            FotaMainActivity activity = activityRef.get();
            if(activity == null){
                return;
            }

            FotaLog.d(TAG, "handleMessage -> what = " + msg.what + ", arg = " + msg.arg1);
            switch (msg.what) {
                case MSG_UPDATE_DOWNLOAD:
                    activity.handleDownloadStatus();
                    break;
                case MSG_UPDATE_RESULT:
                    activity.resultCode = msg.arg1;
                    if (msg.arg1 >= FotaUIPresenter.FOTA_CHECK_RESULT_BEGIN
                            && msg.arg1 <= FotaUIPresenter.FOTA_CHECK_RESULT_END) {
                        if (activity.checkAnimation.isPlaying) {
                            activity.checkAnimation.stop(activity.resultCode);
                        } else {
                            activity.handleCheckResult();
                        }
                    } else if (msg.arg1 >= FotaUIPresenter.FOTA_DOWNLOAD_RESULT_NO_NETWORK_CONNECTED
                            && msg.arg1 <= FotaUIPresenter.FOTA_DOWNLOAD_RESULT_STORAGE_NOT_AVAILABLE) {
                        activity.handleErrorBeforeDownload(msg.arg1);
                    } else if (msg.arg1 == FotaUIPresenter.FOTA_DOWNLOAD_RESULT_DOWNLOAD_DELETED) {
                        activity.startCheck();
                    } else if (msg.arg1 >= FotaUIPresenter.FOTA_INSTALL_RESULT_LOW_BATTERY
                            && msg.arg1 <= FotaUIPresenter.FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_VALID) {
                        activity.handleInstallResult(msg.arg1);
                    } else {
                        FotaLog.v(TAG, "handleMessage -> unknown result type ???");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void updateResult(int type) {
        FotaLog.v(TAG, "updateResult -> type = " + type);
        mHandler.removeMessages(MSG_UPDATE_RESULT);
        Message message = mHandler.obtainMessage(MSG_UPDATE_RESULT, type, 0);
        mHandler.sendMessage(message);
    }

    public void updateDownloadStatus() {
        FotaLog.v(TAG, "updateDownloadStatus");
        mHandler.removeMessages(MSG_UPDATE_DOWNLOAD);
        mHandler.sendEmptyMessage(MSG_UPDATE_DOWNLOAD);
    }

    private void handleCheckResult() {
        if (resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_DOWNLOAD_VERSION_VALID
                || resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_NO_NETWORK_CONNECTED
                && FotaUIPresenter.getInstance(this).haveActvieDownloadTask()) {
            showDownloadView();
        } else {
            showVersionView();
        }
    }

    private void handleErrorBeforeDownload(int err) {
        btnUpdate.setText(getResources().getString(R.string.start_update));
        AlertDialogUtil dialogUtil = AlertDialogUtil.getInstance();
        switch (err) {
            case FotaUIPresenter.FOTA_DOWNLOAD_RESULT_NO_NETWORK_CONNECTED:
                dialogUtil.show(this, R.string.dialog_title_warm,
                        R.string.dialog_msg_download_network,
                        R.string.cancel_download, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }, R.string.dialog_msg_download_setting_network,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                break;
            case FotaUIPresenter.FOTA_DOWNLOAD_RESULT_WIFI_WARNING:
                dialogUtil.show(this, R.string.dialog_title_warm,
                        R.string.dialog_msg_download_network_mobile,
                        R.string.cancel_download, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }, R.string.dialog_msg_download_network_mobile_download,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (FotaUIPresenter.getInstance(FotaMainActivity.this)
                                        .getCurrentTaskState() == State.CHECKED) {
                                    startDownload(false);
                                } else {
                                    pauseOrResumeDownload(false);
                                }
                            }
                        });
                break;
            case FotaUIPresenter.FOTA_DOWNLOAD_RESULT_STORAGE_NOT_ENOUGH:
                dialogUtil.show(this, R.string.dialog_title_warm,
                        R.string.dialog_msg_download_storage,
                        R.string.cancel_download, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }, R.string.dialog_msg_download_storage_cleanup,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent = new Intent();
                                ComponentName componentName = new ComponentName("cn.tcl.filemanager",
                                        "cn.tcl.filemanager.activity.FileBrowserActivity");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setComponent(componentName);
                                startActivity(intent);
                            }
                        });
                break;
            case FotaUIPresenter.FOTA_DOWNLOAD_RESULT_STORAGE_NOT_AVAILABLE:
                break;
            default:
                break;
        }
    }

    private void handleDownloadStatus() {
        showDownloadView();
    }

    private void handleInstallResult(int resultCode) {
        if (resultCode != FotaUIPresenter.FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_VALID
                && mInstallDialog != null && mInstallDialog.isShowing()) {
            mInstallDialog.dismiss();
        }
        AlertDialogUtil dialogUtil = AlertDialogUtil.getInstance();
        switch (resultCode) {
            case FotaUIPresenter.FOTA_INSTALL_RESULT_LOW_BATTERY:
                dialogUtil.showPositive(this, R.string.dialog_title_warm,
                        R.string.dialog_msg_install_low_battery,
                        R.string.ok, null, false);
                break;
            case FotaUIPresenter.FOTA_INSTALL_RESULT_VERIFY_FAIL:
                dialogUtil.showPositive(this, R.string.dialog_title_warm,
                        R.string.dialog_msg_install_package_error,
                        R.string.ok, null, false);
                break;
            case FotaUIPresenter.FOTA_INSTALL_RESULT_UPDATE_FAIL:
                dialogUtil.showPositive(this, R.string.dialog_title_warm,
                        R.string.dialog_msg_install_failed,
                        R.string.ok, null, false);
                break;
            case FotaUIPresenter.FOTA_INSTALL_RESULT_UPDATE_WAIT:
                dialogUtil.showPositive(this, R.string.dialog_title_warm,
                        R.string.dialog_msg_install_call_interrupt,
                        R.string.ok, null, false);
                break;
            case FotaUIPresenter.FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_INVALID:
                dialogUtil.showPositive(this, R.string.dialog_title_warm,
                        R.string.dialog_msg_version_invalid,
                        R.string.ok, null, false);
                break;
            case FotaUIPresenter.FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_DISCARD:
                dialogUtil.showPositive(this, R.string.dialog_title_warm,
                        R.string.dialog_msg_version_discard,
                        R.string.ok, null, false);
                break;
            case FotaUIPresenter.FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_EXPIRED:
                dialogUtil.show(this, R.string.dialog_title_warm,
                        R.string.dialog_msg_version_expired,
                        R.string.cancel_download, null,
                        R.string.dialog_msg_version_expired_redownload,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                startDownload(true);
                            }
                        });
                break;
            case FotaUIPresenter.FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_VALID:
                mFotaUIPresenter.scheduleStartInstall();
                break;
            default:
                break;
        }
    }
}