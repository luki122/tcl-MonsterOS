/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.filemanager.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.IActivitytoCategoryListener;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.activity.CategoryActivity;
import cn.tcl.filemanager.activity.FileBrowserActivity;
import cn.tcl.filemanager.activity.FileSafeActivity;
import cn.tcl.filemanager.adapter.ListFileInfoAdapter;
import cn.tcl.filemanager.dialog.PasswordDialog;
import cn.tcl.filemanager.manager.CategoryCountManager;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.IconManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.service.FileManagerService;
import cn.tcl.filemanager.service.ProgressInfo;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;
import cn.tcl.filemanager.utils.StorageQueryUtils;
import cn.tcl.filemanager.utils.ToastHelper;
import cn.tcl.filemanager.view.PathProgressLayout;
import cn.tcl.filemanager.view.PathProgressThirdLayout;
import cn.tcl.filemanager.view.PathProgressTwoFirstLayout;
import cn.tcl.filemanager.view.PathProgressTwoSecondLayout;
import cn.tcl.filemanager.view.RoundProgressBar;
import mst.widget.CycleImageView;

/*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/
/*MODIFIED-END by haifeng.tang,BUG-1910684*/
/* MODIFIED-BEGIN by haifeng.tang, 2016-04-20,BUG-1925055*/
/* MODIFIED-END by haifeng.tang,BUG-1925055*/
/*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/
/*MODIFIED-END by haifeng.tang,BUG-1910684*/

public class CategoryFragment extends Fragment implements IActivitytoCategoryListener, OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = CategoryFragment.class.getSimpleName();

    private LinearLayout progressContent;
    private PathProgressLayout mPhoneSizeParent;
    private PathProgressLayout mSDSizeParent;
    private PathProgressLayout mExternalSizeParent;
    private PathProgressTwoFirstLayout mPhoneSizeParentF;
    private PathProgressTwoSecondLayout mPhoneSizeParentS;
    private PathProgressTwoFirstLayout mSDSizeParentF;
    private PathProgressTwoSecondLayout mSDSizeParentS;
    private PathProgressTwoFirstLayout mExternalSizeParentF;
    private PathProgressTwoSecondLayout mExternalSizeParentS;
    private PathProgressThirdLayout mSdAndExternalSizeParent;

    /* MODIFIED-BEGIN by zibin.wang, 2016-06-29,BUG-2419472*/
    private TextView mSizeFirstPercent;
    private TextView mSizeFirstType;
    private TextView mSizeSecondPercent;
    private TextView mSizeSecondType;
    private TextView mSizeThirdPercent;
    private TextView mSizeThirdType;
    private ProgressBar mProgressBar;
    /* MODIFIED-END by zibin.wang,BUG-2419472*/

    private PathProgressLayout mOnePathProgressLayout;
    private RelativeLayout mSecondPathProgressLayout;
    private RelativeLayout mThirdPathProgressLayout;

    private LinearLayout mRecentLayout;
    private CycleImageView mRecentTopIcon;
    private TextView mRecentTopTextUp;
    private TextView mRecentTopTextDown;
    private CycleImageView mRecentBottomIcon;
    private TextView mRecentBottomTextUp;
    private TextView mRecentBottomTextDown;
    private SimpleDateFormat mDateFormat;
    private LinearLayout mRecentTopLayout;
    private LinearLayout mRecentBottomLayout;

    /** category item */

    private RelativeLayout mCategoryImage;
    private TextView mImageCount;
    private RelativeLayout mCategoryVideo;
    private TextView mVideoCount;
    private RelativeLayout mCategoryMusic;
    private TextView mMusicCount;
    private RelativeLayout mCategoryApk;
    private TextView mApkCount;
    private RelativeLayout mCategorySafe;
    private TextView mSafeCount;
    private RelativeLayout mCategoryRecents;

    private ListView mRecentsList;

    private TextView[] mCountTextView;

    private ToastHelper mToastHelper;

    private final int STORAGE_PHONE = 1;
    private final int STORAGE_SD = 2;
    private final int STORAGE_USB = 4;

    private final int PROGRESSBAR_CATEGORY_PHONE = STORAGE_PHONE;
    private final int PROGRESSBAR_CATEGORY_SD = STORAGE_SD;
    private final int PROGRESSBAR_CATEGORY_USB = STORAGE_USB;
    private final int PROGRESSBAR_CATEGORY_PHONE_SD = STORAGE_PHONE + STORAGE_SD;
    private final int PROGRESSBAR_CATEGORY_SD_USB = STORAGE_SD + STORAGE_USB;
    private final int PROGRESSBAR_CATEGORY_PHONE_USB = STORAGE_PHONE + STORAGE_USB;
    private final int PROGRESSBAR_CATEGORY_ALL = STORAGE_PHONE + STORAGE_SD + STORAGE_USB;

    private CategoryListListener mCategoryListListener;
    private FileInfoManager mRecentsFileInfoManager;
    private ListFileInfoAdapter mRecentsAdapter;
    private Resources mResources;
    private Context mContext;
    protected FileManagerApplication mApplication;
    protected MountManager mMountManager;

    private static final int DATA_UPDATED = 100;

    // private boolean mScannerFinished;
//    private DataContentObserver mDataContentObserver; //MODIFIED by haifeng.tang, 2016-04-13,BUG-1938740

    // private PercentageBar mChart;
    // private PercentageBarManager mChartManager;

    private Activity mActivity;
    private CategoryFragmentListener mCategoryFragmentListener;

    private static final String ALL = "allSpace";
    private static final String PHONE = "phoneSpace";
    private static final String SDCARD = "sdcardSpace";
    private Typeface tf;

    private boolean isOpenUSB = true;
    private boolean isSettingsEnter = false;
    private PasswordDialog mPasswordDialog;
    private StorageQueryUtils storageQueryUtils;
    private DataContentObserver mDataContentObserver;

    private Timer mTimer;
    private final static int ONCE_TIME = 1000;
    private final static int OPERATION_TIME = 120000;

    private Uri mUri;
    private int numOfFile;

    private boolean isFirstFileItem;

    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/

    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1913721*/

    /**
     * if no safe box ,popup a dialog for enable safe box function
     * // MODIFIED-BEGIN by haifeng.tang, 2016-04-20, BUG-1925055
     *
     * @param context
     * @param mMountManager
     * @return
     */
    public boolean isFristEntry(Context context, MountManager mMountManager,boolean isSettingsEnter) {
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(context);
        infoDialog.setTitle(R.string.welcome_dialog_title);
        if (SafeUtils.isUserFingerPrint(context)) {
            //TODO Verify identity
            infoDialog.setMessage(R.string.welcome_dialog_info);
            infoDialog.setPositiveButton(R.string.welcome_dialog_verify_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mPasswordDialog = new PasswordDialog(getActivity(), SafeUtils.getCurrentSafePath(getActivity()), false);
                    mPasswordDialog.popIdentityVerify();
                }
            });
        } else {
            // TODO set system pwd
            infoDialog.setMessage(R.string.welcome_dialog_verify_pwd);
            infoDialog.setPositiveButton(R.string.safe_set, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent();
                    intent.setClassName("com.android.settings",
                            "com.android.settings.fingerprint.FingerprintEnrollEnrolling");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    intent.putExtra("enroll_fingerprint_tag", 0);
                    intent.putExtra("enroll_feature", 2);
                    startActivity(intent);
                }
            });
            infoDialog.setNeutralButton(R.string.cancel, null);
            infoDialog.show();
        }

        return false;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.space_clear:
//                if (CommonUtils.checkApkExist(this, CommonUtils.BOOSTER_PACKAGE_NAME)) {
//                }
                migrateBtnClicked();
                break;
            case R.id.category_image:
                CategoryManager.mCurrentCagegory = CategoryManager.CATEGORY_PICTURES;
                startFileBrowerActivity();
                break;
            case R.id.category_video:
                CategoryManager.mCurrentCagegory = CategoryManager.CATEGORY_VEDIOS;
                startFileBrowerActivity();
                break;
            case R.id.category_music:
                CategoryManager.mCurrentCagegory = CategoryManager.CATEGORY_MUSIC;
                startFileBrowerActivity();
                break;
            case R.id.category_apk:
                CategoryManager.mCurrentCagegory = CategoryManager.CATEGORY_APKS;
                startFileBrowerActivity();
                break;
            case R.id.category_safe:
                mApplication.mIsCategorySafe = true;
                mApplication.mIsSafeMove = false;
                Intent enterEncryptIntent = new Intent();
                mst.app.dialog.AlertDialog.Builder infoDialog = new mst.app.dialog.AlertDialog.Builder(getActivity());
                if (((CategoryActivity)getActivity()).isSystemLock()) {
                    infoDialog.setTitle(R.string.welcome_dialog_title);
                        if (mApplication.mIsVerifySystemPwd) {
                            // TODO to category safe
                            Intent verificationIntent = new Intent();
                            verificationIntent.setAction("com.tct.securitycenter.FingerprintVerify");
                            mApplication.mCurrentPath = SafeUtils.getEncryptRootPath(getActivity());
                            CategoryManager.mCurrentMode = CategoryManager.PATH_MODE;
                            getActivity().startActivityForResult(verificationIntent, 100);
                        } else {
                            //TODO Verify identity
                            infoDialog.setMessage(R.string.welcome_dialog_info);
                            infoDialog.setPositiveButton(R.string.welcome_dialog_verify_btn, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mApplication.mIsVerifySystemPwd = true;
                                    Intent verificationIntent = new Intent();
                                    verificationIntent.setAction("com.tct.securitycenter.FingerprintVerify");
                                    mApplication.mCurrentPath = SafeUtils.getEncryptRootPath(getActivity());
                                    CategoryManager.mCurrentMode = CategoryManager.PATH_MODE;
                                    getActivity().startActivityForResult(verificationIntent, 100);
                                }
                            });
                            infoDialog.show();
                        }
                } else {
                    // TODO set system pwd
                    infoDialog.setMessage(R.string.welcome_dialog_verify_pwd);
                    infoDialog.setPositiveButton(R.string.safe_set, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            ComponentName cn = new ComponentName("com.android.settings",
                                    "com.android.settings.fingerprint.FingerprintSettings");
                            intent.setComponent(cn);
                            startActivity(intent);
                        }
                    });
                    infoDialog.setNeutralButton(R.string.cancel, null);
                    infoDialog.show();
                }
                break;
            case R.id.recents_view_layout:
                CategoryManager.mCurrentCagegory = CategoryManager.CATEGORY_RECENT;
                startFileBrowerActivity();
                break;
        }
    }

    private void migrateBtnClicked() {
        CommonUtils.launchPhoneKeeperActivity(mActivity); // MODIFIED by songlin.qi, 2016-06-06,BUG-2223767
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        openFile(mRecentsAdapter.getItem(position));
    }

    /**
     * open file
     */
    private void openFile(FileInfo fileInfo) {

        if (!fileInfo.getFile().exists()) {
            String error = mActivity.getString(R.string.path_not_exists, fileInfo.getFileName());
            mToastHelper.showToast(error);
            return;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = fileInfo.getMIMEType();
        String path = fileInfo.getFileAbsolutePath();

        mUri = fileInfo.getContentUri(mApplication.mService);

        if (FileInfo.MIMETYPE_EXTENSION_UNKONW.equals(type)) {
            type = "*/*";
        }
        final String mimeType = type;
        LogUtils.d(TAG, "Open uri file: " + mUri + " mimeType=" + type);
        if (mUri.toString().startsWith(FileInfo.FILE_URI_HEAD)) {
            MediaScannerConnection.scanFile(mActivity, new String[]{path}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {

                }

                @Override
                public void onScanCompleted(String s, Uri uri) {
                    LogUtils.d(TAG, "scan completed uri=" + uri);
                    mUri = uri;
                    intent.setDataAndType(mUri, mimeType);
                    startAcitivityOfOpenFile(intent, fileInfo.getFileName());
                }
            });
        } else {
            if (null != mUri && mUri.toString().startsWith(FileInfo.FILE_URI_HEAD) && !mUri.toString().startsWith(FileInfo.FILE_OTG_HEAD) && !mUri.toString().startsWith(FileInfo.FILE_SD_HEAD)) {
                mUri = FileProvider.getUriForFile(mContext, FileInfo.FILE_PROVIDER, fileInfo.getFile());
                LogUtils.d(TAG, "uri turn to fileprovider:" + mUri);
            }
            intent.setDataAndType(mUri, type);
            startAcitivityOfOpenFile(intent, fileInfo.getFileName());
        }
    }

    private void startAcitivityOfOpenFile(Intent intent,String name) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            String error = mActivity.getString(R.string.msg_unable_open_file_in_app, name);
            mToastHelper.showToast(error);
        }
    }

    public interface CategoryFragmentListener {
        public void switchCategoryList();

        public void updateCategoryNormalBarView();

        public void notifyCategoryDone(boolean isDone);

        public void LandShowSize(PathProgressLayout mPhoneSize, PathProgressLayout mSDSize, PathProgressLayout mExternalSize);
    }

    @Override
    public void onAttach(Activity activity) {
        try {
            mCategoryFragmentListener = (CategoryFragmentListener) activity;
            mActivity = activity;
            mApplication = (FileManagerApplication) mActivity
                    .getApplicationContext();
        } catch (Exception e) {
            throw new ClassCastException(activity.toString()
                    + "must implement CategoryFragmentListener");
        }
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogUtils.timerMark(TAG+" start");
        super.onCreate(savedInstanceState);
        LogUtils.timerMark(TAG+" end");
        LogUtils.getAppInfo(mActivity);
        Intent intent = mActivity.getIntent();
        if(intent != null){
            isSettingsEnter = intent.getBooleanExtra("from_settings",false);
        }
        if (null == mRecentsFileInfoManager){
            mRecentsFileInfoManager = new FileInfoManager();
        }
        if (null == mCategoryListListener) {
            mCategoryListListener = new CategoryListListener();
        }


        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = this.getActivity().getApplicationContext();
        mResources = mContext.getResources();
        storageQueryUtils = new StorageQueryUtils(this.getActivity());
        mToastHelper = new ToastHelper(mContext);
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    private class CategoryListListener implements FileManagerService.OperationEventListener, DialogInterface.OnDismissListener {

        @Override
        public void onDismiss(DialogInterface dialog) {
        }

        @Override
        public void onTaskPrepare() {
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
        }

        @Override
        public void onTaskResult(int result) {
            List<FileInfo> list = mRecentsFileInfoManager.getShowFileList();
            FileInfo firstFileInfo;
            FileInfo secondFileInfo;
            if (null != list && list.size() > 0) {
                firstFileInfo = list.get(0);
                setTextViewStatus(mRecentTopTextUp);
                mRecentTopTextUp.setText(firstFileInfo.getFileName());
                mRecentTopTextDown.setText(getModifiedTime(firstFileInfo) + "     " + firstFileInfo.getFileSizeStr());
                isFirstFileItem = false;

                int mIconId = IconManager.getInstance().getIcon(firstFileInfo, IconManager.LIST_ITEM);
                mRecentTopIcon.setImageResource(mIconId);
                loadImage(firstFileInfo, isFirstFileItem);
                if (list.size() > 1) {
                    secondFileInfo = list.get(1);
                    setTextViewStatus(mRecentBottomTextUp);
                    mRecentBottomTextUp.setText(secondFileInfo.getFileName());
                    mRecentBottomTextDown.setText(getModifiedTime(secondFileInfo) + "     " + secondFileInfo.getFileSizeStr());
                    isFirstFileItem = true;

                    mIconId = IconManager.getInstance().getIcon(secondFileInfo, IconManager.LIST_ITEM);
                    mRecentBottomIcon.setImageResource(mIconId);
                    loadImage(secondFileInfo, isFirstFileItem);
                }
            }
            mRecentLayout.postInvalidate();
        }
    }

    private void setTextViewStatus (TextView textView){
        ViewGroup.LayoutParams params = textView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        textView.setLayoutParams(params);
        textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
    }

    private String getModifiedTime(FileInfo fileInfo) {
        long mTime = fileInfo.getFileLastModifiedTime();
        if (mTime == 0) {
            mTime = System.currentTimeMillis();
        }
        if (mDateFormat == null) {
            String strDateFormat = "yyyy-MM-dd   HH:mm";
            mDateFormat = new SimpleDateFormat(strDateFormat);
        }
        String mSecondModifiedTime = mDateFormat.format(new Date(mTime)).toString();
        return mSecondModifiedTime;
    }

    private void loadImage(final FileInfo fileInfo, boolean isFirstFileItem) {
        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (null != mRecentTopIcon && !isFirstFileItem) {
                        mRecentTopIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        mRecentTopIcon.setImageDrawable((Drawable) msg.obj);
                } else if (null != mRecentBottomIcon && isFirstFileItem) {
                        mRecentBottomIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        mRecentBottomIcon.setImageDrawable((Drawable) msg.obj);
                }
            }
        };
        IconManager.getInstance().loadImage(mContext, fileInfo,
                new IconManager.IconCallback() {
                    public void iconLoaded(Drawable iconDrawable) {
                        if (iconDrawable != null) {
                            Message message = mHandler.obtainMessage(0, 1, 1, iconDrawable);
                            mHandler.sendMessage(message);
                        }
                    }
                });
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        LogUtils.i(TAG, "onViewCreated");
        mMountManager = MountManager.getInstance();
        progressContent = (LinearLayout) view.findViewById(R.id.progress_content);

        mCategoryImage = (RelativeLayout) view.findViewById(R.id.category_image);
        mImageCount = (TextView) mCategoryImage.findViewById(R.id.image_count_textview);
        mCategoryVideo = (RelativeLayout) view.findViewById(R.id.category_video);
        mVideoCount = (TextView) mCategoryVideo.findViewById(R.id.video_count_textview);
        mCategoryMusic = (RelativeLayout) view.findViewById(R.id.category_music);
        mMusicCount = (TextView) mCategoryMusic.findViewById(R.id.music_count_textview);
        mCategoryApk = (RelativeLayout) view.findViewById(R.id.category_apk);
        mApkCount = (TextView) mCategoryApk.findViewById(R.id.apk_count_textview);
        mCategorySafe = (RelativeLayout) view.findViewById(R.id.category_safe);
        mSafeCount = (TextView) mCategorySafe.findViewById(R.id.safe_count_textview);
        mCategoryRecents = (RelativeLayout) view.findViewById(R.id.recents_view_layout);

//        mRecentsAdapter = new ListFileInfoAdapter(mActivity, mRecentsFileInfoManager, mRecentsList);

        mCountTextView = new TextView[]{mImageCount, mVideoCount, mMusicCount, mApkCount, mSafeCount};

        mOnePathProgressLayout = (PathProgressLayout) progressContent.findViewById(R.id.first_size_parent);
        mSecondPathProgressLayout = (RelativeLayout) progressContent.findViewById(R.id.progress_two_storage_layout);
        mThirdPathProgressLayout = (RelativeLayout) progressContent.findViewById(R.id.progress_all_storage_layout);

        mRecentLayout = (LinearLayout) view.findViewById(R.id.recent_layout);
        mRecentTopIcon = (CycleImageView) mRecentLayout.findViewById(R.id.recent_icon1);
        mRecentTopTextUp = (TextView) mRecentLayout.findViewById(R.id.recent_up_text1);
        mRecentTopTextDown = (TextView) mRecentLayout.findViewById(R.id.recent_down_text1);
        mRecentBottomIcon = (CycleImageView) mRecentLayout.findViewById(R.id.recent_icon2);
        mRecentBottomTextUp = (TextView) mRecentLayout.findViewById(R.id.recent_up_text2);
        mRecentBottomTextDown = (TextView) mRecentLayout.findViewById(R.id.recent_down_text2);
        mRecentTopLayout = (LinearLayout) mRecentLayout.findViewById(R.id.recent_top_layout);
        mRecentBottomLayout = (LinearLayout) mRecentLayout.findViewById(R.id.recent_bottom_layout);

        mRecentTopLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                List<FileInfo> mRecentFirst = mRecentsFileInfoManager.getShowFileList();
                if (null != mRecentFirst && mRecentFirst.size() > 0) {
                    if (null != mRecentFirst.get(0)) {
                        openFile(mRecentFirst.get(0));
                    }
                }
            }
        });

        mRecentBottomLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                List<FileInfo> mRecentSecond = mRecentsFileInfoManager.getShowFileList();
                if (null != mRecentSecond && mRecentSecond.size() > 1) {
                    if (null != mRecentSecond.get(1)) {
                        openFile(mRecentSecond.get(1));
                    }
                }
            }
        });

/*MODIFIED-BEGIN by haifeng.tang, 2016-04-13,BUG-1938740*/
//        mDataContentObserver = new DataContentObserver(mHandler, mContext);
        tf = CommonUtils.getRobotoMedium();

        mCategoryImage.setOnClickListener(this);
        mCategoryVideo.setOnClickListener(this);
        mCategoryMusic.setOnClickListener(this);
        mCategoryApk.setOnClickListener(this);
        mCategorySafe.setOnClickListener(this);
        mCategoryRecents.setOnClickListener(this);

//        mRecentsList.setOnItemClickListener(this);

        mResources = mContext.getResources();
//        registerContentObservers();
/*MODIFIED-END by haifeng.tang,BUG-1938740*/
        refreshSizeView();
//        if (tf != null) {
//            mPhoneName.setTypeface(tf);
//            mSDName.setTypeface(tf);
//            mExternalName.setTypeface(tf);
//        }
        if(isSettingsEnter){
            isFristEntry(mActivity,mMountManager,isSettingsEnter);
        }
        isSettingsEnter = false;
    }


    private void showOneProgressBar(int category) {
        if (mOnePathProgressLayout == null ||
                mSecondPathProgressLayout == null ||
                mThirdPathProgressLayout == null) {
            return;
        }
        mOnePathProgressLayout.setVisibility(View.VISIBLE);
        mSecondPathProgressLayout.setVisibility(View.GONE);
        mThirdPathProgressLayout.setVisibility(View.GONE);
        switch (category) {
            case PROGRESSBAR_CATEGORY_PHONE:
                mPhoneSizeParent = mOnePathProgressLayout;
                mSDSizeParent = null;
                mExternalSizeParent = null;
                break;
            case PROGRESSBAR_CATEGORY_SD:
                mSDSizeParent = mOnePathProgressLayout;
                mPhoneSizeParent = null;
                mExternalSizeParent = null;

                break;
            case PROGRESSBAR_CATEGORY_USB:

                mExternalSizeParent = mOnePathProgressLayout;
                mPhoneSizeParent = null;
                mSDSizeParent = null;

                break;
        }
    }

    private void showTwoProgressBar(int category) {
        /* MODIFIED-BEGIN by haifeng.tang, 2016-04-20,BUG-1940832*/
        try {
            mOnePathProgressLayout.setVisibility(View.GONE);
            mSecondPathProgressLayout.setVisibility(View.VISIBLE);
            mThirdPathProgressLayout.setVisibility(View.GONE);

            switch (category) {
                case PROGRESSBAR_CATEGORY_PHONE_SD:
                    mPhoneSizeParentF = (PathProgressTwoFirstLayout) mSecondPathProgressLayout.findViewById(R.id.first_size_parent);
                    mSDSizeParentS = (PathProgressTwoSecondLayout) mSecondPathProgressLayout.findViewById(R.id.second_size_parent);
                    mExternalSizeParentF = null;
                    mExternalSizeParentS = null;
                    break;
                case PROGRESSBAR_CATEGORY_PHONE_USB:
                    mPhoneSizeParentF = (PathProgressTwoFirstLayout) mSecondPathProgressLayout.findViewById(R.id.first_size_parent);
                    mExternalSizeParentS = (PathProgressTwoSecondLayout) mSecondPathProgressLayout.findViewById(R.id.second_size_parent);
                    mSDSizeParentF = null;
                    mSDSizeParentS = null;

                    break;
                case PROGRESSBAR_CATEGORY_SD_USB:

                    mSDSizeParentF = (PathProgressTwoFirstLayout) mSecondPathProgressLayout.findViewById(R.id.first_size_parent);
                    mExternalSizeParentS = (PathProgressTwoSecondLayout) mSecondPathProgressLayout.findViewById(R.id.second_size_parent);
                    mPhoneSizeParentF = null;
                    mPhoneSizeParentS = null;

                    break;
            }
        } catch (Exception e) {

        }
    }

    private void showThreeProgressBar() {
        try {
            mOnePathProgressLayout.setVisibility(View.GONE);
            mSecondPathProgressLayout.setVisibility(View.GONE);
            mThirdPathProgressLayout.setVisibility(View.VISIBLE);

            mPhoneSizeParentF = (PathProgressTwoFirstLayout) mThirdPathProgressLayout.findViewById(R.id.first_size_parent);
            mSdAndExternalSizeParent = (PathProgressThirdLayout) mThirdPathProgressLayout.findViewById(R.id.third_size_parent);
            /* MODIFIED-BEGIN by zibin.wang, 2016-06-29,BUG-2419472*/
            mSizeFirstPercent = (TextView) mPhoneSizeParent.findViewById(R.id.size_percent);
            mSizeFirstType = (TextView) mPhoneSizeParent.findViewById(R.id.size);
            mSizeSecondPercent = (TextView) mSDSizeParent.findViewById(R.id.size_percent);
            mSizeSecondType = (TextView) mSDSizeParent.findViewById(R.id.size);
            mSizeThirdPercent = (TextView) mExternalSizeParent.findViewById(R.id.size_percent);
            mSizeThirdType = (TextView) mExternalSizeParent.findViewById(R.id.size);
            mProgressBar=(ProgressBar)getActivity().findViewById(R.id.pb_progressbar);
            /* MODIFIED-END by zibin.wang,BUG-2419472*/
        } catch (Exception e) {

        }
        /* MODIFIED-END by haifeng.tang,BUG-1940832*/

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtils.i(TAG, "onActivityCreated");
    }

    @Override
    public void onDestroy() {
//        unRegisterContentObservers(); //MODIFIED by haifeng.tang, 2016-04-13,BUG-1938740
        super.onDestroy();
        if (null != mTimer) {
            mTimer.cancel();
        }
    }

    private void refreshPercentageBar(long totalSpace, long usedSpace,
                                      int order, Drawable drawable) {
        // mChartManager.addEntry(order, usedSpace / (float) totalSpace,
        // drawable);
    }


    private void refreshSizeView() {

        LogUtils.e(TAG, "refreshSizeView start->" + System.currentTimeMillis());
        // mChart.setVisibility(View.VISIBLE);
        // ADD START FOR PR980890,978687 BY Wenjing.ni 20151126

//        });
        int category = 0;
        if (!CommonUtils.isPhoneStorageZero()) {
            category += STORAGE_PHONE;
        }

        if (mMountManager != null && mMountManager.isSDCardMounted()) {
            category += STORAGE_SD;
        }

        if (mMountManager != null && mMountManager.isOtgMounted()) {
            category += STORAGE_USB;
        }
        int mode = 0;
        switch (category) {
            case PROGRESSBAR_CATEGORY_PHONE:
            case PROGRESSBAR_CATEGORY_SD:
            case PROGRESSBAR_CATEGORY_USB:
                showOneProgressBar(category);
                mode = RoundProgressBar.BIG;
                break;
            case PROGRESSBAR_CATEGORY_PHONE_SD:
            case PROGRESSBAR_CATEGORY_PHONE_USB:
            case PROGRESSBAR_CATEGORY_SD_USB:
                showTwoProgressBar(category);
                mode = RoundProgressBar.BIG;
                break;
            case PROGRESSBAR_CATEGORY_ALL:
                showThreeProgressBar();
                /* MODIFIED-BEGIN by zibin.wang, 2016-06-29,BUG-2419472*/
                /* MODIFIED-END by zibin.wang,BUG-2419472*/
                mode = RoundProgressBar.BIG;
                break;
        }

        if (mPhoneSizeParent != null) {

            mPhoneSizeParent.setOnClickListener((OnClickListener) mActivity);
            if (mApplication.mService != null) { // MODIFIED by haifeng.tang, 2016-04-20,BUG-1925055
                mApplication.mService.startCountStorageSizeTask(mPhoneSizeParent,
                        mMountManager.getPhonePath(), mContext);
            }

            mPhoneSizeParent.setId(R.id.phone_size_parent);
            mPhoneSizeParent.setPathNameText(R.string.draw_left_phone_storage);
            mPhoneSizeParent.setIcon(R.drawable.ic_phone);
            long mPhoneAvailableSize = storageQueryUtils.getPhoneAvailableSize();
            mPhoneSizeParent.setUsedSize(FileUtils.sizeToString(mActivity, mPhoneAvailableSize));
        }

        if (mPhoneSizeParentF != null) {
            if (mApplication.mService != null) { // MODIFIED by haifeng.tang, 2016-04-20,BUG-1925055
                mApplication.mService.startCountStorageSizeTask(mPhoneSizeParentF,
                        mMountManager.getPhonePath(), mContext);
            }

            mPhoneSizeParentF.setOnClickListener((OnClickListener) mActivity);
            mPhoneSizeParentF.setId(R.id.phone_size_parent);
            long mPhoneAvailableSize = storageQueryUtils.getPhoneAvailableSize();
            int mPhonePercent = (int) (((storageQueryUtils.getPhoneTolSize() - storageQueryUtils.getPhoneAvailableSize()) / (float) storageQueryUtils.getPhoneTolSize()) * 100);
            mPhoneSizeParentF.setPhoneStorage(FileUtils.sizeToString(mActivity, mPhoneAvailableSize));
            mPhoneSizeParentF.setPhoneOrSdName(getString(R.string.phone_storage));
            mPhoneSizeParentF.setProgressNew(mPhonePercent);
        }

        if (mSDSizeParent != null) {
            if (mApplication.mService != null) { // MODIFIED by haifeng.tang, 2016-04-20,BUG-1925055
                mApplication.mService.startCountStorageSizeTask(mSDSizeParent,
                        mMountManager.getSDCardPath(), mContext);
            }

            mSDSizeParent.setOnClickListener((OnClickListener) mActivity);
            mSDSizeParent.setId(R.id.sd_size_parent);
            mSDSizeParent.setIcon(R.drawable.ic_sd);
            mSDSizeParent.setPathNameText(R.string.main_sd_storage);
            long mSdAvailableSize = storageQueryUtils.getSdAvailableSize();
            mSDSizeParent.setUsedSize(FileUtils.sizeToString(mActivity, mSdAvailableSize));
        }

        if (mSDSizeParentF != null) {
            if (mApplication.mService != null) { // MODIFIED by haifeng.tang, 2016-04-20,BUG-1925055
                mApplication.mService.startCountStorageSizeTask(mSDSizeParentF,
                        mMountManager.getSDCardPath(), mContext);
            }

            mSDSizeParentF.setOnClickListener((OnClickListener) mActivity);
            mSDSizeParentF.setId(R.id.sd_size_parent);
            long mSdAvailableSize = storageQueryUtils.getSdAvailableSize();
            int mSdPercent = (int) (((storageQueryUtils.getSdTolSize() - storageQueryUtils.getSdAvailableSize()) / (float) storageQueryUtils.getSdTolSize()) * 100);
            mSDSizeParentF.setPhoneStorage(FileUtils.sizeToString(mActivity, mSdAvailableSize));
            mSDSizeParentF.setPhoneOrSdName(getString(R.string.main_sd_storage));
            mSDSizeParentF.setProgressNew(mSdPercent);
        }

        if (mSDSizeParentS != null) {
            if (mApplication.mService != null) { // MODIFIED by haifeng.tang, 2016-04-20,BUG-1925055
                mApplication.mService.startCountStorageSizeTask(mSDSizeParentS,
                        mMountManager.getSDCardPath(), mContext);
            }

            mSDSizeParentS.setOnClickListener((OnClickListener) mActivity);
            mSDSizeParentS.setId(R.id.sd_size_parent);
            long mSdAvailableSize = storageQueryUtils.getSdAvailableSize();
            int mSdPercent = (int) (((storageQueryUtils.getSdTolSize() - storageQueryUtils.getSdAvailableSize()) / (float) storageQueryUtils.getSdTolSize()) * 100);
            mSDSizeParentS.setSdStorage(FileUtils.sizeToString(mActivity, mSdAvailableSize));
            mSDSizeParentS.setProgressNew(mSdPercent);
        }

        if (mExternalSizeParent != null) {
            mExternalSizeParent.setOnClickListener((OnClickListener) mActivity);
            if (mApplication.mService != null) { // MODIFIED by haifeng.tang, 2016-04-20,BUG-1925055
                mApplication.mService.startCountStorageSizeTask(mExternalSizeParent,
                        mMountManager.getUsbOtgPath(), mContext);
            }

            mExternalSizeParent.setId(R.id.external_size_parent);
            mExternalSizeParent.setIcon(R.drawable.ic_usb);
            mExternalSizeParent.setPathNameText(R.string.main_external_storage);
            long mOtgVailableSize = storageQueryUtils.getOtgAvailableSize();
            mExternalSizeParent.setUsedSize(FileUtils.sizeToString(mActivity,mOtgVailableSize));
        }

        if (mExternalSizeParentS != null) {
            mExternalSizeParentS.setOnClickListener((OnClickListener) mActivity);
            if (mApplication.mService != null) {
                mApplication.mService.startCountStorageSizeTask(mExternalSizeParentS,
                        mMountManager.getUsbOtgPath(), mContext);
            }
            mExternalSizeParentS.setOnClickListener((OnClickListener) mActivity);
            mExternalSizeParentS.setId(R.id.external_size_parent);
            long mOtgVailableSize = storageQueryUtils.getOtgAvailableSize();
            int mOtgPercent = (int) (((storageQueryUtils.getOtgTolSize() - storageQueryUtils.getOtgAvailableSize()) / (float) storageQueryUtils.getOtgTolSize()) * 100);
            mExternalSizeParentS.setSdStorage(FileUtils.sizeToString(mActivity,mOtgVailableSize));
            mExternalSizeParentS.setProgressNew(mOtgPercent);
        }

        if (mSdAndExternalSizeParent != null) {
            mSdAndExternalSizeParent.setOnClickListener((OnClickListener) mActivity);
            if (mApplication.mService != null) {
                mApplication.mService.startCountStorageSizeTask(mSdAndExternalSizeParent,
                        mMountManager.getUsbOtgPath(), mContext);
            }
            mSdAndExternalSizeParent.setOnClickListener((OnClickListener) mActivity);
            mSdAndExternalSizeParent.setId(R.id.sd_external_size_parent);
            long mSdAndExternalAvailableSize = storageQueryUtils.getSdAvailableSize() + storageQueryUtils.getOtgAvailableSize();
            long mSdAndExternalTotalSize = storageQueryUtils.getSdTolSize() + storageQueryUtils.getOtgTolSize();
            float mPercent = (((float) (mSdAndExternalTotalSize - mSdAndExternalAvailableSize) / (float) mSdAndExternalTotalSize)) * 100;
            mSdAndExternalSizeParent.setSdAndExternalStorage(FileUtils.sizeToString(mActivity, mSdAndExternalAvailableSize));
            mSdAndExternalSizeParent.setProgressNew((int) mPercent);
        }

        LogUtils.i(TAG, "refreshSizeView end->" + System.currentTimeMillis());

    }


    protected void registerContentObservers() {
        LogUtils.i(TAG, "registerContentObservers");
        Uri uri = MediaStore.Files.getContentUri("external");
        if (mDataContentObserver == null) {
            mDataContentObserver = new DataContentObserver(new Handler()); // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329
        }
        if (mActivity != null && mActivity.getContentResolver() != null) {
            mActivity.getContentResolver().registerContentObserver(uri, true, mDataContentObserver);
        }
        if (mApplication != null) {
            mApplication.currentOperation = FileManagerApplication.OTHER;
        }
    }

    private class DataContentObserver extends ContentObserver {

        public DataContentObserver(Handler handler) {
            super(handler);

        }

        @Override
        public void onChange(boolean selfChange) {
            LogUtils.i(TAG, "notify onChanged");
            loadCountForCategory();
            loadRecentsFileList();
        }
    }

    public void loadCount() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
//                mActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        if (null != mTimer) {
            mTimer.cancel();
        }
        mTimer = new Timer();

        TimerTask timerCancel = new TimerTask() {
            @Override
            public void run() {
                mTimer.cancel();
            }
        };
        mTimer.schedule(timerCancel, OPERATION_TIME);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                loadCountForCategory();
            }
        };
        mTimer.schedule(timerTask, 0, ONCE_TIME);
        loadRecentsFileList();
//        }
    }

    private void loadCountForCategory() {
        try {
            CategoryCountManager.getInstance().clearTaskQueue();
            int[] categoryType = {CategoryManager.CATEGORY_PICTURES,
                    CategoryManager.CATEGORY_VEDIOS,
                    CategoryManager.CATEGORY_MUSIC,
                    CategoryManager.CATEGORY_APKS,
                    CategoryManager.CATEGORY_SAFE};
            int categorySize = categoryType.length;
            if(null != mCountTextView && mCountTextView.length > 0) {
                for (int i = 0; i < categorySize; i++) {
                    loadCountText(mCountTextView[i], categoryType[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRecentsFileList() {
        if (null != mApplication && null != mApplication.mService) {
            mApplication.mService.listRecentsFiles(mActivity.getClass()
                    .getName(), mRecentsFileInfoManager, mActivity, mCategoryListListener);
        }
    }

    private Handler mmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            TextView textView = (TextView) msg.obj;
            textView.setText("" + msg.arg1);
        }
    };

    private void loadCountText(final TextView textview, int position) {
        CategoryCountManager.getInstance().loadCategoryCountText(position, mContext,
                new CategoryCountManager.CountTextCallback() {
                    @Override
                    public void countTextCallback(int countText) {
                        Message message = mmHandler.obtainMessage(position, countText, 1, textview);
                        mmHandler.sendMessage(message);
                    }
                });
    }

    private Handler mCountHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            numOfFile = msg.arg1;
        }
    };

    private void loadCountNum(final TextView textview, int position) {
        CategoryCountManager.getInstance().loadCategoryCountText(position, mContext,
                new CategoryCountManager.CountTextCallback() {
                    @Override
                    public void countTextCallback(int countText) {
                        Message message = mCountHandler.obtainMessage(position, countText, 1, textview);
                        mCountHandler.sendMessage(message);
                    }
                });
    }

//    public int isEmptyCategory(){
//        switch(CategoryManager.mCurrentCagegory){
//            case CategoryManager.CATEGORY_PICTURES:
//                loadCountNum(mImageCount, CategoryManager.CATEGORY_PICTURES);
//                break;
//            case CategoryManager.CATEGORY_VEDIOS:
//                loadCountNum(mVideoCount, CategoryManager.CATEGORY_VEDIOS);
//                break;
//            case CategoryManager.CATEGORY_MUSIC:
//                loadCountNum(mMusicCount, CategoryManager.CATEGORY_MUSIC);
//                break;
//            case CategoryManager.CATEGORY_APKS:
//                loadCountNum(mApkCount, CategoryManager.CATEGORY_APKS);
//                break;
//            case CategoryManager.CATEGORY_SAFE:
//                loadCountNum(mSafeCount, CategoryManager.CATEGORY_SAFE);
//                break;
//        }
//        return numOfFile;
//    }

    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/
    class OpenSafeListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int id) {
            /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/
            SharedPreferenceUtils.setFristEnterSafe(mContext, true);
            Intent intent = new Intent(mContext, FileSafeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            /*MODIFIED-END by haifeng.tang,BUG-1910684*/

        }
    }
    /*MODIFIED-END by haifeng.tang,BUG-1910684*/

    private class updateChartListener implements
            FileManagerService.OperationEventListener {
        private long mAllSpace;
        private long mPhoneUsedSpace;
        private long mSDUsedSpace;

        @Override
        public void onTaskResult(int result) {
            if (mPhoneUsedSpace == 0 && mAllSpace == 0) {
                getSpace();
            }
            CategoryCountManager countManager = CategoryCountManager
                    .getInstance();
            countManager.putMap(ALL, mAllSpace);
            countManager.putMap(PHONE, mPhoneUsedSpace);
            // mChartManager.clear();
            refreshPercentageBar(
                    mAllSpace,
                    mPhoneUsedSpace,
                    0,
                    mResources
                            .getDrawable(R.drawable.ic_category_percent_smartphone));
            if (mMountManager.isSDCardMounted()) {
                countManager.putMap(SDCARD, mSDUsedSpace);// add for PR961285 by
                // yane.wang@jrdcom.com
                // 20150511
                refreshPercentageBar(
                        mAllSpace,
                        mSDUsedSpace,
                        1,
                        mResources
                                .getDrawable(R.drawable.ic_category_percent_sd_card));
            }
            // mChartManager.Commit();
            mCategoryFragmentListener.notifyCategoryDone(true);// add for
            // PR917842 by
            // yane.wang@jrdcom.com
            // 20150212
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            if (!progressInfo.isFailInfo()) {
                String updateInfo = progressInfo.getUpdateInfo();
                if ("phoneUsedSpace".equals(updateInfo)) {
                    mPhoneUsedSpace = progressInfo.getTotal();
                } else if ("sdUsedSpace".equals(updateInfo)) {
                    mSDUsedSpace = progressInfo.getTotal();
                } else if ("allSpace".equals(updateInfo)) {
                    mAllSpace = progressInfo.getTotal();
                }
            }
        }

        @Override
        public void onTaskPrepare() {
            // mChartManager.clear();
        }

        public void getSpace() {
            MountManager mMountManager = MountManager.getInstance();
            long totalSpace = 0;
            long blocSize = 0;
            long availaBlock = 0;
            long blockCount = 0;
            long freeSpace = 0;
            long sdfreeSpace = 0;

            try {
                String filePath = mMountManager.getPhonePath();
                StatFs statfs = new StatFs(filePath);
                try {
                    blocSize = statfs.getBlockSizeLong();
                    if (CommonUtils.hasM()) {
                        availaBlock = new File(filePath).getFreeSpace();
                    } else {
                        availaBlock = statfs.getAvailableBlocksLong();
                    }
                    blockCount = statfs.getBlockCountLong();
                } catch (NoSuchMethodError e) {
                    blocSize = statfs.getBlockSizeLong();
                    if (CommonUtils.hasM()) {
                        availaBlock = new File(filePath).getFreeSpace();
                    } else {
                        availaBlock = statfs.getAvailableBlocksLong();
                    }
                    blockCount = statfs.getBlockCountLong();
                }
                if (!CommonUtils.hasM()) {
                    freeSpace = availaBlock * blocSize;
                }
                totalSpace = blocSize * blockCount;
                if (CommonUtils.hasM()) {
                    mPhoneUsedSpace = totalSpace - freeSpace;
                } else {
                    mPhoneUsedSpace = totalSpace - availaBlock;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mAllSpace += totalSpace;

            if (mMountManager.isSDCardMounted()) {
                try {
                    String sdFilePath = mMountManager.getPhonePath();
                    StatFs statfs = new StatFs(sdFilePath);
                    try {
                        blocSize = statfs.getBlockSizeLong();
                        if (CommonUtils.hasM()) {
                            availaBlock = new File(sdFilePath).getFreeSpace();
                        } else {
                            availaBlock = statfs.getAvailableBlocksLong();
                        }
                        blockCount = statfs.getBlockCountLong();
                    } catch (NoSuchMethodError e) {
                        blocSize = statfs.getBlockSizeLong();
                        if (CommonUtils.hasM()) {
                            availaBlock = new File(sdFilePath).getFreeSpace();
                        } else {
                            availaBlock = statfs.getAvailableBlocksLong();
                        }
                        blockCount = statfs.getBlockCountLong();
                    }
                    if (!CommonUtils.hasM()) {
                        sdfreeSpace = availaBlock * blocSize;
                    }
                    totalSpace = blocSize * blockCount;
                    if (CommonUtils.hasM()) {
                        mSDUsedSpace = totalSpace - availaBlock;
                    } else {
                        mSDUsedSpace = totalSpace - sdfreeSpace;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAllSpace += totalSpace;
            }
        }
    }


    @Override
    public void refreshCategory() {
        LogUtils.i(TAG, "refreshCategory");
        refreshSizeView();
    }


    @Override
    public void onScannerStarted() {
        // unRegisterContentObservers();
        // mScannerFinished = false;
    }

    @Override
    public void onScannerFinished() {
        // registerContentObservers();
        // mScannerFinished = true;
        loadCount();
    }

    @Override
    public void disableCategoryEvent(boolean disable) {
    }

    // ADD START FOR PR495247 BY HONGBIN.CHEN 20150807
    @Override
    public void onResume() {
        loadCount();
        registerContentObservers();
        /* MODIFIED-BEGIN by haifeng.tang, 2016-04-20,BUG-1940832*/
        super.onResume();
    }

    /* MODIFIED-END by haifeng.tang,BUG-1940832*/

    public void onHiddenChanged(boolean hidden) {
        if (!hidden)
            loadCount();
        super.onHiddenChanged(hidden);
    }
    // ADD END FOR PR495247 BY HONGBIN.CHEN 20150807

    private void unRegisterContentObservers() {
        LogUtils.i(TAG, "unRegisterContentObservers");
        if (mDataContentObserver != null) {
            mActivity.getContentResolver().unregisterContentObserver(mDataContentObserver);
            mDataContentObserver = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mPasswordDialog !=null && mPasswordDialog.fingerPrintDialog != null && mPasswordDialog.fingerPrintDialog.isShowing()){
            mPasswordDialog.fingerPrintDialog.dismiss();
            mPasswordDialog.stopFingerprint();
        }
        unRegisterContentObservers();
    }


    private  void startFileBrowerActivity() {
        Intent intent = null;
        intent = new Intent(mActivity, FileBrowserActivity.class);
        mActivity.startActivity(intent);
    }
}
