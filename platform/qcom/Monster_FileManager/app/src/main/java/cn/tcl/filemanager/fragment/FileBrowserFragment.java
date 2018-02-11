/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.fragment;

import android.annotation.Nullable;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.IActivityManager;

import cn.tcl.filemanager.activity.CategoryActivity;
import mst.app.dialog.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.ContentObserver;
import android.drm.DrmStore;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.WifiDisplayStatus;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.Downloads;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xdja.sks.IEncDecListener;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.IActivityListener;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.activity.FileBaseActivity;
import cn.tcl.filemanager.activity.FileBrowserActivity;
import cn.tcl.filemanager.activity.FileSafeBrowserActivity;
import cn.tcl.filemanager.adapter.FileInfoAdapter;
import cn.tcl.filemanager.adapter.GridFileInfoAdapter;
import cn.tcl.filemanager.adapter.ListFileInfoAdapter;
import cn.tcl.filemanager.adapter.SimPickerDetailAdapter;
import cn.tcl.filemanager.dialog.AlertDialogFragment;
import cn.tcl.filemanager.dialog.ProgressDialogFragment;
import cn.tcl.filemanager.drm.DrmManager;
import cn.tcl.filemanager.manager.CategoryCountManager;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.FileInfoComparator;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.manager.SafeManager;
import cn.tcl.filemanager.manager.ShortCutManager;
import cn.tcl.filemanager.service.BaseAsyncTask;
import cn.tcl.filemanager.service.CategoryTask;
import cn.tcl.filemanager.service.FileManagerService;
import cn.tcl.filemanager.service.FileSecurityTask;
import cn.tcl.filemanager.service.ListFileTask;
import cn.tcl.filemanager.service.MediaStoreHelper;
import cn.tcl.filemanager.service.MultiMediaStoreHelper;
import cn.tcl.filemanager.service.PrivateFileOperationTask;
import cn.tcl.filemanager.service.ProgressInfo;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;
import cn.tcl.filemanager.utils.ToastHelper;
import cn.tcl.filemanager.utils.TypedObject;

import mst.app.dialog.AlertDialog;

//import FavoriteManager;
/* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
/* MODIFIED-END by haifeng.tang,BUG-1987329*/
//[FEATURE]-Add-BEGIN by TSBJ,shuang.liu1,09/24/2014,FR-791321,


public abstract class FileBrowserFragment extends Fragment implements OnItemClickListener,
        OnItemLongClickListener, IActivityListener {

    private static final String TAG = FileBrowserFragment.class.getSimpleName();
    private static final String CREATE_FOLDER_DIALOG_TAG = "CreateFolderDialogTag";
    public static final String RENAME_DIALOG_TAG = "RenameDialogTag";
    private static final String RENAME_EXTENSION_DIALOG_TAG = "RenameExtensionDialogTag";
    private static final String DETAIL_DIALOG_TAG = "DetailDialogTag";
    private static final String DELETE_DIALOG_TAG = "DeleteDialogTag";
    private static final String NEW_FILE_PATH_KEY = "newFilePathKey";
    public static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    private static boolean isChanageMode = false;
    private static String copyFilePath;
    private static int mCurrentCategory;
    public static String currentFile;

    private boolean mSavedState;
    private boolean mSearchMode;

    private GridFileInfoAdapter mGridAdapter;

    protected LinearLayout mNoSearchView;
    protected LinearLayout mNoFolderView;
    protected TextView mNo_messageView;
    protected TextView noSearchText;
    private FrameLayout mContentView;
    private ListFileInfoAdapter mListFileInfoAdapter;


    private ToastHelper mToastHelper;
    protected Activity mActivity;
    protected FileInfoAdapter mAdapter;
    protected FileManagerApplication mApplication;

    private ProgressDialogFragment mProgressDialog;

    private AlertDialog mCancelDialog;

    private AlertDialog mAlertDialog;

    //[PLATFORM]-Add by qinglian.zhang, 2014/10/11 PR-558780, reason screen share
    private DisplayManager mDisplayManager;
    private static final String TCT_HDCP_DRM_NOTIFY = "hdcp_drm_notify";

    RenameDoneListener renameDoneListener; // MODIFIED by zibin.wang, 2016-05-06,BUG-2019352
    CreateFolderListener createFolderListener;

    private static final int NETWORK_ERROR = 0X11;
    private static final int NON_ENCRYPT_FILE = 0X12;
    private static final int NON_DECRYPT_FILE = 0X13;

    protected class Pos {
        int index = 0;
        int top = 0;
    }

    protected Stack<Pos> mPosStack = new Stack<Pos>();
    protected boolean mIsBack;
    protected MountManager mMountManager;

    //[FEATURE]-Add-BEGIN by TSBJ,shuang.liu1,09/24/2014,FR-791321,
    private WindowManager mWindowManager;
    private ImageView mDragView;
    private FileInfo mDragFile;
    private boolean mDraging;
    private WindowManager.LayoutParams mDragLayoutParams; // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
    private static final String SEND_BROADCAST_MESSAGE = "com.tcl.test";
    private static final int DRAGVIEW_WIDTH = 48;
    private static final int DRAGVIEW_HEIGHT = 48;
    private DataContentObserver mDataContentObserver;
    private int resumeCount;
    private boolean cutSameState = false;
    private boolean deleteFlag = false;
    private ShortCutManager mShortcutManager;
    private String mSearchMessage;
    private boolean mSelectAll = false;
    ProgressDialogFragment listDialogFragment = null;
    List<FileInfo> infos;
    private boolean scrollBottom = false;
    // delete mode for safe box
    static int deleteMode = 0;

    /* MODIFIED-BEGIN by songlin.qi, 2016-05-26,BUG-2202760*/
    // record onChanged count
    private int onChangedCount = 0;
    private int MESSAGE_REFRESH_ADAPTER = 0;
    private int MESSAGE_RELOAD_CONTENT = 1;
    /* MODIFIED-END by songlin.qi,BUG-2202760*/

    private int mFirstPosition;

    private Uri mUri;
    private EncryptIEncDecListener mEncryptIEncDecListener;
    private DecryptIEncDecListener mDecryptIEncDecListener;

    private String mDecryptFilePath;

    private OnClickListener mCancelListner = new OnClickListener() { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433

        public void onClick(DialogInterface dlg, int sumthin) {
            clearAlertDialog();
            mSearchMode = false;
            if (mAdapter != null) {
                if (mAdapter.isMode(FileInfoAdapter.MODE_SEARCH)) {
                    mFromSearchToEdit = true;
                }
                if (deleteMode == SafeManager.DELETE_ALBUM_MODE) {
                    updateActionMode(FileInfoAdapter.MODE_NORMAL);
                    ((FileBrowserActivity) getActivity()).hideEditWindow();
                } else {
                    updateActionMode(FileInfoAdapter.MODE_EDIT);
                    ((FileBrowserActivity) getActivity()).showEditButton();
                }
            }
            updateEditBarByThread();
        }
    };

    private void clearAlertDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        mAlertDialog = null;
    }

    private void setShowAlertDialog(AlertDialog dialog) {
        mAlertDialog = dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        LogUtils.e("niky", "FileBrowserFragment -> onAttach, activity=" + activity);
        super.onAttach(activity);
        try {
            mActivity = activity;
            boolean forceRefresh = false;//PR 1489123 zibin.wang 2016-03-02 add
            if (mApplication == null) {
                forceRefresh = true;
            }
            mApplication = (FileManagerApplication) mActivity.getApplicationContext();
            mAbsListViewFragmentListener = (AbsListViewFragmentListener) activity;
            mAbsUpdateEncryptFilesCount = new UpdateEncryptCount();
            if (forceRefresh) {
                refreshAdapter(mApplication.mCurrentPath);
            }
        } catch (Exception e) {
            throw new ClassCastException(activity.toString() + "must implement AbsListViewFragmentListener");
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_browser, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        LogUtils.timerMark(TAG+" start");
        super.onViewCreated(view, savedInstanceState);
        LogUtils.timerMark(TAG+" end");
        LogUtils.getAppInfo(mActivity);
        mToastHelper = new ToastHelper(mActivity);
        mContentView = (FrameLayout) view.findViewById(R.id.file_browser_content);
        addContentView(getContentLayoutId());
        cancelAllDialog();

    }


    abstract int getContentLayoutId();


    protected void addContentView(int layoutId) {
        LayoutInflater.from(mActivity).inflate(layoutId, mContentView);
    }

    private void showNoSearchResultView(boolean isShow, String args) {
        /* MODIFIED-BEGIN by haifeng.tang, 2016-05-09,BUG-2104433*/
        //if not attached to Activity return
        if (!isAdded()) {
            return;
        }

        if (isShow) {
            if (mNoFolderView != null && mNoFolderView.getVisibility() == View.VISIBLE) {
                mNoFolderView.setVisibility(View.GONE);
            }
            if (mNoSearchView != null) {
                mNoSearchView.setVisibility(View.VISIBLE);
                //String noResultText = getResources().getString(R.string.no_search_result_m);
                //noResultText = String.format(noResultText, args);
                noSearchText.setText(getResources().getString(R.string.no_search_result_cn));
            }
            /* MODIFIED-END by haifeng.tang,BUG-2104433*/
        } else {
            if (mNoSearchView != null) {
                mNoSearchView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void clickDestorySafe(int mode, String mDesFolder) {
        if (mode == SafeManager.DESTORY_DELETE_MODE) {
            if (mApplication.mService != null) {
                LogUtils.d("DELETE", "this is enter delete");
                mApplication.mService.deleteFiles(mActivity
                                .getClass().getName(), SafeManager.SAFE_DESTORY_DELETE_MODE, new ArrayList<FileInfo>(),
                        new HeavyOperationListener(R.string.deleting) {
                            @Override
                            public void onTaskResult(int errorType) {
                                super.onTaskResult(errorType);

                                SafeUtils.deleteSafeRootFolder(SafeUtils.getCurrentSafePath(mActivity));
                                SharedPreferenceUtils.setCurrentSafeRoot(mActivity, null);
                                SharedPreferenceUtils.setCurrentSafeName(mActivity, null);
                                mActivity.finish();
                            }
                        });
                deleteMode = 0;
            }

        } else if (mode == SafeManager.DESTORY_RECOVER_MODE) {
            if (mApplication.mService != null) {
                mApplication.mService.ShiftOutFiles(mActivity.getClass().getName(),
                        new ArrayList<FileInfo>(), mDesFolder, SafeManager.DESTORY_SHIFT_OUT_MODE, new HeavyOperationListener(R.string.move_out) {
                            @Override
                            public void onTaskResult(int errorType) {
                                super.onTaskResult(errorType);
                                mApplication.currentOperation = FileManagerApplication.OTHER;
                                switch (errorType) {
                                    case FileManagerService.OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE:
                                        mToastHelper.showToast(R.string.insufficient_message_cn); // MODIFIED by haifeng.tang, 2016-05-11,BUG-2104433
                                        break;
                                    case FileManagerService.OperationEventListener.ERROR_CODE_DELETE_FAILS:
                                        mToastHelper.showToast(R.string.delete_fail);
                                        break;
                                    case FileManagerService.OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION:
                                        mToastHelper.showToast(R.string.copy_deny);
                                        break;
                                    default:
                                        break;
                                }
                                SafeUtils.deleteSafeRootFolder(SafeUtils.getCurrentSafePath(mActivity));
                                SharedPreferenceUtils.setCurrentSafeRoot(mActivity, null);
                                SharedPreferenceUtils.setCurrentSafeName(mActivity, null);
                                clearFailedFiles(); // MODIFIED by songlin.qi, 2016-06-12,BUG-1989911
                                mActivity.finish();
                            }
                        });
                //showNoFolderResultView(false);//PR-1174270 Nicky Ni -001 20151217
            } else {
                LogUtils.i(TAG, "mApplication.mService  is null");
            }
        }
    }

    public void onGlobalSearchBackPressed() {
        showNoSearchResultView(false, null);
        //switchToNormalView();
        if (mAdapter != null) {
            mAdapter.changeModeFromSearchToNormal();
        }
        updateActionMode(FileInfoAdapter.MODE_NORMAL);
    }

    public void onBackPress() {
        LogUtils.e(TAG, "onBackPress111111111111111");
        showNoSearchResultView(false, null);
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-01,BUG-2231253*/
        if (mApplication != null && mApplication.mFileInfoManager != null
                && mApplication.mFileInfoManager.getShowFileList().size() == 0) {
            showNoFolderResultView(true);
        } else {
            showNoFolderResultView(false);
        }
        /* MODIFIED-END by songlin.qi,BUG-2231253*/
        if (mAdapter != null && mAdapter.isMode(FileInfoAdapter.MODE_EDIT)) {
            if (mFromSearchToEdit) {
                //add for PR902241 by yane.wang@jrdcom.com 20150113 begin
                if (mApplication.mFileInfoManager.getSearchItemsCount() > 0) {
                    showSearchResultView();
                } else {
                    exitSearchResultView();// PR922787 modified by bin.song
                }
                //add for PR902241 by yane.wang@jrdcom.com 20150113 end
            } else {
                switchToNormalView();
            }
            mFromSearchToEdit = false;
            return;
        }
        if (mAdapter != null && mAdapter.isMode(FileInfoAdapter.MODE_SEARCH)) {
            exitSearchResultView();
            return;
        }
        if (mApplication == null || (mApplication.mService != null && mApplication.mService.isBusy(mActivity.getClass().getName()))) { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
            return;
        }

        LogUtils.i(TAG, "onBackPress--MODE:" + mAdapter.getMode());
        mIsBack = true;
        if (mAdapter != null && mAdapter.isMode(FileInfoAdapter.MODE_NORMAL) || mAdapter.isMode(FileInfoAdapter.MODE_COPY)
                || mAdapter.isMode(FileInfoAdapter.MODE_GLOBALSEARCH) || mAdapter.isMode(FileInfoAdapter.MODE_ADD_ENCRYPT_FILE)) {
            if (CategoryManager.mCurrentMode == CategoryManager.PATH_MODE) {
                /** To determine whether the current root directory. */
                if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES &&
                        MountManager.getInstance().isSdOrPhonePath(mApplication.mCurrentPath)) {
                    List<FileInfo> showFileList = mApplication.mFileInfoManager.getShowFileList();
                    if (showFileList != null && showFileList.size() > 0 && !showFileList.get(0).isDirectory()) {
                        showPicDir();
                    } else {
                        showDirectoryContent(mApplication.mCurrentPath);
                    }
                    if (mAbsListViewFragmentListener != null) {
                        mAbsListViewFragmentListener.updateActionbar();
                    }
                } else if (mApplication.mCurrentPath != null
                        && !MountManager.getInstance().isSdOrPhonePath(mApplication.mCurrentPath)
                        || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE
                        && !SafeUtils.getEncryptRootPath(mActivity).equals(mApplication.mCurrentPath)) {
                    String parentDir = new File(mApplication.mCurrentPath).getParent();
                    if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                        showPicDir();
                    } else {
                        showDirectoryContent(parentDir);
                    }
                } else {
                    mActivity.finish();
                }
            }
//            else {
//                if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE){
//                    String parentDir = new File(mApplication.mCurrentPath).getParent();
//                    showDirectoryContent(parentDir);
//                }
//            }
        }
    }

    public void showPicDir() {
        LogUtils.i(TAG, "showPicDir()");
        mApplication.mFileInfoManager.addItemList(mApplication.mFileInfoList);
        mApplication.mCurrentPath = Environment.getExternalStorageDirectory().toString();
        // ADD END FOR PR1050553 BY HONGBIN.CHEN 20150810
        refreshPathBar();
        if (mApplication != null && mApplication.mFileInfoManager != null) {
            mApplication.mFileInfoManager.loadAllFileInfoList();
        }
        refreshAdapter();
        if (mAdapter != null && mAdapter.isMode(FileInfoAdapter.MODE_EDIT)) {
            updateEditBarState();
        }
        restoreListPosition();
    }

    public void refreshAdapter() {
        if (mAdapter != null) {
            /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
            int lastCount = mAdapter.getCount();
            LogUtils.e(TAG,"refreshAdapter lastcount " + lastCount);
            mAdapter.refresh();
            LogUtils.e(TAG,"refreshAdapter lastcount2 " + mAdapter.getCount());
            if (CategoryManager.mCurrentMode != CategoryManager.CATEGORY_MODE) {
                showNoFolderResultView(mAdapter.isEmpty());
            }
            LogUtils.e(TAG,"refreshAdapter enter mode " + SafeManager.mCurrentmode);
            if (SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN) {
                LogUtils.e(TAG,"refreshAdapter enter mode0111");
                mAdapter.changeMode(mAdapter.MODE_EDIT);
            } else if (SafeManager.mCurrentmode == SafeManager.FILE_NORMAL && CategoryManager.mCurrentSafeCategory != -1) {
                LogUtils.e(TAG,"refreshAdapter enter mode00000");
                mAdapter.changeMode(mAdapter.MODE_NORMAL);
            }
            mAdapter.notifyDataSetChanged();
            if (mActivity != null && lastCount != mAdapter.getCount()
                    && mActivity instanceof FileSafeBrowserActivity) {
                LogUtils.e(TAG,"refreshAdapter lastCount != ");
                ((FileBrowserActivity) mActivity).updateOptionMenu();
            }
            /* MODIFIED-END by songlin.qi,BUG-2227088*/
        }
    }

    //[PLATFORM]-Add-BEGIN by qinglian.zhang, 2014/10/11 PR-558780, reason screen share
    private void showDrmWifidisplyDiaog(final Context context) {
        new AlertDialog.Builder(mActivity).setIcon(R.drawable.ic_dialog_alert_holo_light)
                .setTitle(R.string.drm_wifidisplay_title).setMessage(R.string.drm_wifidisplay_message).setPositiveButton(R.string.drm_wifidisplay_cancel_btn, new OnClickListener() { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
            public void onClick(DialogInterface dlg, int sumthin) {
                //add for PR989765 by yane.wang@jrdcom.com 20150430 begin
                DisplayManagerGlobal.getInstance().disconnectWifiDisplay();
                //add for PR989765 by yane.wang@jrdcom.com 20150430 end
                Toast.makeText(context, R.string.tv_link_close_toast, Toast.LENGTH_SHORT).show();
            }
        })
                .setNegativeButton(R.string.drm_wifidisplay_ok, null).show();
    }

    //[PLATFORM]-Add-END by qinglian.zhang

    private void exitSearchResultView() {
        switchToNormalView();
        if (mApplication.mService != null) {
            // ADD START FOR PR1062833 BY HONGBIN.CHEN 20150817
            if (!showContent(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory)) {
                showBeforeSearchContent();
            }
            // ADD END FOR PR1062833 BY HONGBIN.CHEN 20150817
        }
        mFromSearchToEdit = false;
        /* MODIFIED-BEGIN by haifeng.tang, 2016-05-09,BUG-2104433*/
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.changeSearchMode(false);
        }
        /* MODIFIED-END by haifeng.tang,BUG-2104433*/
    }

    protected void showDirectoryContent(String path) {
        LogUtils.i(TAG, "showDirectoryContent->" + path);
        //unRegisterContentObservers();
        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }
        // ADD START FOR PR1059973 BY HONGBIN.CHEN 20150804
        unRegisterContentObservers();
        registerContentObservers(-1);
        // ADD END FOR PR1059973 BY HONGBIN.CHEN 20150804

        if (mApplication != null) {
            mApplication.mCurrentPath = path;
            if (mApplication.mFileInfoManager != null) {
                mApplication.mFileInfoManager.clearShowFiles();
            }

            if (mApplication != null && mApplication.mService != null) { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
                mApplication.mService.listFiles(mActivity.getClass().getName(),
                        path, new ListListener(true), false, null, -1, ListFileTask.LIST_MODE_VIEW); // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329
            }
        }
    }

    private void showCategoryContent(int category) {
        LogUtils.i(TAG, "showCategoryContent category->" + category);
        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }
        unRegisterContentObservers();
        registerContentObservers(category);

        if (mApplication != null) {
            if (mApplication.mFileInfoManager != null) {
                LogUtils.d("niky", ">>>>>>>>.showCategoryContent() -.clearShowFiles ()");
                mApplication.mFileInfoManager.clearShowFiles();
            }

            if (mApplication.mService != null) {
                if (CategoryManager.isSafeCategory) {
                    LogUtils.d(TAG, "this is enter CategoryManager.isSafeCategory" + CategoryManager.isSafeCategory);
                    mApplication.mService.listSafeCategoryFiles(mActivity.getClass()
                            .getName(), category, mActivity, new CategoryListListener(category, true));
                } else {
                    mApplication.mService.listCategoryFiles(mActivity.getClass()
                            .getName(), category, mActivity, new CategoryListListener(category, true), CategoryTask.LIST_MODE_VIEW); // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329
                }
            }
        }
    }

    protected void registerContentObservers(int category) {
        LogUtils.i(TAG, "registerContentObservers");
        Uri uri = MediaStore.Files.getContentUri("external");
        if (mDataContentObserver == null) {
            mDataContentObserver = new DataContentObserver(new Handler()); // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329
        }
        if (category == CategoryManager.CATEGORY_DOWNLOAD) {
            uri = Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI;
        }
        if (mActivity != null && mActivity.getContentResolver() != null) {
            mActivity.getContentResolver().registerContentObserver(uri, true, mDataContentObserver);
        }
        if (mApplication != null) {
            mApplication.currentOperation = FileManagerApplication.OTHER;
        }
    }


    /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
    @Override
    public void onStop() {
        super.onStop();
        /* MODIFIED-BEGIN by zibin.wang, 2016-05-06,BUG-2019352*/
        if (renameDoneListener != null && renameDoneListener.getcheatShowDialog()) {
            renameDoneListener.setShowDialog(true);
            renameDoneListener.setcheatShowDialog(false);
        }
        if (createFolderListener != null && createFolderListener.getcheatShowDialog()) {
            createFolderListener.setShowDialog(true);
            createFolderListener.setcheatShowDialog(false);
        }
        /* MODIFIED-END by zibin.wang,BUG-2019352*/
        unRegisterContentObservers();
    }
    /* MODIFIED-END by haifeng.tang,BUG-1987329*/

    private void unRegisterContentObservers() {
        LogUtils.i(TAG, "unRegisterContentObservers");
        if (mDataContentObserver != null) {
            mActivity.getContentResolver().unregisterContentObserver(mDataContentObserver);
            mDataContentObserver = null;
        }

        /* MODIFIED-BEGIN by songlin.qi, 2016-05-26,BUG-2202760*/
        // reset value of onChangedCount
        onChangedCount = 0;
        /* MODIFIED-END by songlin.qi,BUG-2202760*/
    }

    private class DataContentObserver extends ContentObserver {

        private Handler mHandler;
        private Runnable mRun = new Runnable() {
            @Override
            public void run() {
                onChanged();
            }
        };

        public DataContentObserver(Handler handler) { // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329
            super(handler);
            mHandler = handler;

        }

        @Override
        public void onChange(boolean selfChange) {
            LogUtils.i(TAG, "notify onChanged");
            //add by haifeng.tang for PR:1274836 start 2015-12-31
            //keep paste task
            if (mApplication.currentOperation == FileManagerApplication.PASTE
                    || mApplication.currentOperation == FileManagerApplication.DETETE) {//PR 1551628 zibin.wang 2016/02/19
                return;
            }
            //add by haifeng.tang for PR:1274836 end 2015-12-31
            if (mApplication.currentOperation != FileManagerApplication.OTHER) {
                mApplication.currentOperation = FileManagerApplication.OTHER;
                return;
            }
            if (mSearchMode || (mAdapter != null && mAdapter.isMode(FileInfoAdapter.MODE_EDIT))) {
                return;
            }
            //// TODO: 16-3-9 2000ms is used to avoid large amount of refresh the listview.
            //This can rerproduce the rate that enter the folder while click the more botton in the list item after copying folder which contains over 200files
            //but this can't fix this issue.
            if (mHandler.hasCallbacks(mRun)) {
                mHandler.removeCallbacks(mRun);
            }
            if (onChangedCount > 0) {
                mHandler.postDelayed(mRun, 150);
            } else {
                mHandler.post(mRun);
            }
            onChangedCount++;
        }
    }

    private void listCategoryFiles(boolean showDialog) {
        try {
            if (mApplication != null && mApplication.mService != null && mActivity != null) {
                if (CategoryManager.isSafeCategory) {
                    mApplication.mService.listSafeCategoryFiles(mActivity.getClass()
                                    /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
                                    .getName(), CategoryManager.mCurrentSafeCategory, mActivity,
                            new CategoryListListener(CategoryManager.mCurrentSafeCategory, showDialog));
                            /* MODIFIED-END by songlin.qi,BUG-2227088*/
                } else {
                    mApplication.mService.listCategoryFiles(mActivity.getClass()
                                    .getName(), CategoryManager.mCurrentCagegory, mActivity,
                            new CategoryListListener(CategoryManager.mCurrentCagegory, showDialog), showDialog ? CategoryTask.LIST_MODE_VIEW : CategoryTask.LIST_MODE_ONCHANGE); // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329
                }
            }
        } catch (IllegalArgumentException e) {
            // Just ignore it
        }
    }

    private void showBeforeSearchContent() {
        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        if (mApplication != null && mApplication.mService != null) {
            mApplication.mService.listBeforeSearchFiles(mActivity.getClass().getName(),
                    new BeforeSearchListListener(), mApplication.mFileInfoManager.getBeforeSearchList());
        }
    }

    private boolean showContent(String path, int category) {
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
        // if in safe category show category content by CategoryManager.mCurrentSafeCategory
        if (CategoryManager.isSafeCategory) {
            showCategoryContent(category);
            return true;
        }
        /* MODIFIED-END by songlin.qi,BUG-2227088*/

        if (CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE
                && 0 <= category && category <= 11) {
            LogUtils.d(TAG, "this is enter showContent(String path, int category)");
            showCategoryContent(category);
            return true;
        } else if (CategoryManager.mCurrentMode == CategoryManager.PATH_MODE
                && path != null) {
            showDirectoryContent(path);
            return true;
        }
        return false;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            /* MODIFIED-BEGIN by songlin.qi, 2016-05-26,BUG-2202760*/
            int what = msg.what;
            if (what == MESSAGE_REFRESH_ADAPTER) {
                refreshAdapter();
            } else if (what == MESSAGE_RELOAD_CONTENT) {
                int category = (int) msg.obj;
                if (isVisible() && category == CategoryManager.mCurrentCagegory) {
                    showCategoryContent(category);
                }
            }
            /* MODIFIED-END by songlin.qi,BUG-2202760*/
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        resumeCount++;
        mSavedState = false;//add for PR967486 by yane.wang@jrdcom.com 20150403

        /* MODIFIED-BEGIN by songlin.qi, 2016-05-26,BUG-2202760*/
        if (CategoryManager.mCurrentCagegory != CategoryManager.CATEGORY_PICTURES) {
            if (isVisible() && resumeCount > 1 &&
                    (CategoryManager.mCurrentMode == CategoryManager.PATH_MODE ||
                            (CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE &&
                                    CategoryManager.mCurrentCagegory != -1))) {
                reloadContent();
            }
        }
    }

    private void reloadContent() {
        if (mApplication != null && mApplication.mService != null && mApplication.mFileInfoManager != null
                && !mApplication.mService.isBusy(mActivity.getClass().getName())) {
            if (mApplication.mFileInfoManager.isPathModified(mApplication.mCurrentPath)) {
            /* MODIFIED-END by songlin.qi,BUG-2202760*/
                if (!mSearchMode) {
                    showContent(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory);
                }
            } else if (mApplication.mFileInfoManager != null && mAdapter != null && mApplication.mCurrentPath != null) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int len = mAdapter.getCount();
                            for (int i = 0; i < len; i++) {
                                if (mAdapter.getItem(i).isDrmFile()) {
                                    handler.sendMessage(handler.obtainMessage(MESSAGE_REFRESH_ADAPTER)); // MODIFIED by songlin.qi, 2016-05-26,BUG-2202760
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                new Thread(runnable).start();
            /* MODIFIED-BEGIN by songlin.qi, 2016-05-26,BUG-2202760*/
            } else if ((CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE &&
                    CategoryManager.mCurrentCagegory != -1)) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int adapterCount = mAdapter.getCount();
                            int currrentCount = CategoryCountManager.getInstance().doInBackgroundTMP(CategoryManager.mCurrentCagegory, mActivity);
                            //if the tow count not equal, content may changed, reload it
                            if (adapterCount != currrentCount) {
                                Message msg = new Message();
                                msg.what = MESSAGE_RELOAD_CONTENT;
                                msg.obj = CategoryManager.mCurrentCagegory;
                                handler.sendMessage(msg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                new Thread(runnable).start();
                /* MODIFIED-END by songlin.qi,BUG-2202760*/
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mPosStack.empty()) {
            mPosStack.clear();
        }
    }

    //add for PR959141 by yane.wang@jrdcom.com 20150326 begin
    protected boolean mIsHasDirctory;
    protected boolean mIsHasDrm;
    protected boolean mCanShare;

    private void updateSelectedFilter(List<FileInfo> files) {
        mIsHasDirctory = false;
        mIsHasDrm = false;
        mCanShare = true;
        for (FileInfo info : files) {
            if (!mIsHasDirctory && info.isDirectory()) {
                mIsHasDirctory = true;
                mCanShare = false;
            }
            if ((!mIsHasDrm || mCanShare) && info.isDrmFile()) {
                mIsHasDrm = true;
                if (!DrmManager.getInstance(getActivity().getApplicationContext()).isDrmSDFile(info.getFileAbsolutePath())) {
                    mCanShare = false;
                }
            }
            if (mIsHasDirctory && mIsHasDrm && !mCanShare) {
                break;
            }
        }
    }
    //add for PR959141 by yane.wang@jrdcom.com 20150326 end

    protected void restoreListPosition() {
    }

    /** restore first position when picture category */
    protected void restoreFirstPosition(){
    }

    protected void storeLastListPos() {
        mIsBack = false;
        int index = mAbsListView.getFirstVisiblePosition();
        View v = mAbsListView.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();
        Pos lastPos = new Pos();
        lastPos.index = index;
        lastPos.top = top;
        mPosStack.push(lastPos);
    }

    protected void refreshPathBar() {
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.updateNormalBarView();
        }
    }

    private class BeforeSearchListListener implements
            FileManagerService.OperationEventListener { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433

        @Override
        public void onTaskResult(int result) {
            if (result != FileManagerService.OperationEventListener.ERROR_CODE_BUSY) {
                if (mApplication != null && mApplication.mFileInfoManager != null) {
                    if (CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE) {
                        mApplication.mFileInfoManager.updateCategoryList(mApplication.mSortType);
                    } else {
                        mApplication.mFileInfoManager.loadFileInfoList(mApplication.mCurrentPath, mApplication.mSortType);
                    }
                }
                LogUtils.e(TAG,"OnItemClick onTaskResult5555....");
                refreshAdapter();
            }
        }

        @Override
        public void onTaskPrepare() {
            return;
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            return;
        }
    }

    public void clearAll() {
        mPosStack.clear();
    }

    /* MODIFIED-BEGIN by haifeng.tang, 2016-05-09,BUG-2104433*/
    private class ListListener implements FileManagerService.OperationEventListener, OnDismissListener {

        public static final String LIST_DIALOG_TAG = "ListDialogFragment";//add for PR889409 by yane.wang@jrdcom.com 20150116
        public static final int LIST_DIALOG_MSG = 100; //MODIFIED by jian.xu, 2016-04-18,BUG-1868328
        /* MODIFIED-END by haifeng.tang,BUG-2104433*/
        // ADD START FOR PR511096 BY HONGBIN.CHEN 20150901
        private boolean mmShowLoading;

        public ListListener(boolean showLoading) {
            mmShowLoading = showLoading;
        }

        // ADD END FOR PR511096 BY HONGBIN.CHEN 20150901

        @Override
        public void onTaskResult(int result) {
            // ADD START FOR PR1050553 BY HONGBIN.CHEN 20150810
            if (result == FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS) {

                // ADD END FOR PR1050553 BY HONGBIN.CHEN 20150810
                refreshPathBar();
                if (mApplication != null && mApplication.mFileInfoManager != null) {
                    mApplication.mFileInfoManager.loadFileInfoList(mApplication.mCurrentPath, FileInfoComparator.SORT_BY_NAME);
                }
                LogUtils.e(TAG,"OnItemClick onTaskResult33333....");
                refreshAdapter();
                if (mAdapter != null && mAdapter.isMode(FileInfoAdapter.MODE_EDIT)) {
                    updateEditBarState();
                }
                restoreListPosition();
            }
            // ADD START FOR PR511096 BY HONGBIN.CHEN 20150901
            if (!mmShowLoading) return;
            // ADD END FOR PR511096 BY HONGBIN.CHEN 20150901
            try {
                mHandler.removeMessages(LIST_DIALOG_MSG); //MODIFIED by jian.xu, 2016-04-18,BUG-1868328 // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
                ProgressDialogFragment listDialogFragment =
                        (ProgressDialogFragment) findFragmentByTag(LIST_DIALOG_TAG);
                if (listDialogFragment != null) {
                    listDialogFragment.dismissAllowingStateLoss();
                }
            } catch (NullPointerException e) {
                // Maybe
                // 1¡¢Parent activity is destroyed
                // 2¡¢Parent activity is finished
                // 3¡¢This fragment is detached
                e.printStackTrace();
            }
        }


        @Override
        public void onTaskPrepare() {
        }

        //add for PR889409 by yane.wang@jrdcom.com 20150116 begin
        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            // ADD START FOR PR511096 BY HONGBIN.CHEN 20150901
            if (!mmShowLoading)
                return;
            // ADD END FOR PR511096 BY HONGBIN.CHEN 20150901
            /* MODIFIED-BEGIN by haifeng.tang, 2016-05-09,BUG-2104433*/
            /*MODIFIED-BEGIN by jian.xu, 2016-04-18,BUG-1868328*/
            if (progressInfo != null && progressInfo.getTotal() < 0) {
                mHandler.sendEmptyMessageDelayed(LIST_DIALOG_MSG, BaseAsyncTask.NEED_UPDATE_TIME);
                return;
            }
            /*MODIFIED-END by jian.xu,BUG-1868328*/
            /* MODIFIED-END by haifeng.tang,BUG-2104433*/

            if (progressInfo != null) {
                try {
                    ProgressDialogFragment listDialogFragment = (ProgressDialogFragment) findFragmentByTag(LIST_DIALOG_TAG);
                    if (isResumed()) {
                        FragmentManager fm = getFragmentManager();
                        if (listDialogFragment == null && fm != null) {
                            listDialogFragment = ProgressDialogFragment
                                    .newInstance(ProgressDialog.STYLE_HORIZONTAL, -1,
                                            R.string.loading,
                                            AlertDialogFragment.INVIND_RES_ID);

                            listDialogFragment.show(fm, LIST_DIALOG_TAG);
                            fm.executePendingTransactions();
                        }
                        listDialogFragment.setLoadProgress(progressInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (mApplication != null && mApplication.mService != null) {
                mApplication.mService.cancel(mActivity.getClass().getName());
            }
        }
        //add for PR889409 by yane.wang@jrdcom.com 20150116 end
    }

    /* MODIFIED-BEGIN by haifeng.tang, 2016-05-09,BUG-2104433*/
    private class CategoryListListener implements FileManagerService.OperationEventListener, OnDismissListener {

        public static final String CATEGORY_LIST_DIALOG_TAG = "CategoryListDialogFragment";
        public static final int CATEGORY_LIST_DIALOG_MSG = 101; //MODIFIED by jian.xu, 2016-04-18,BUG-1868328
        /* MODIFIED-END by haifeng.tang,BUG-2104433*/
        // ADD START FOR PR511096 BY HONGBIN.CHEN 20150901
        private boolean mmShowLoading;

//        public CategoryListListener(boolean showLoading) {
//            mmShowLoading = showLoading;
//        }

        // ADD END FOR PR511096 BY HONGBIN.CHEN 20150901

        /**
         * Constructor of CategoryListListener.
         *
         * @param category the search target(String), which will be shown on
         *                 searchResult TextView..
         */
        public CategoryListListener(int category, boolean showLoading) {
            if (category < 0 || category > 11) {
                throw new IllegalArgumentException();
            }
            mmShowLoading = showLoading;
        }

        @Override
        public void onTaskResult(int result) {
            // ADD START FOR PR1050553 BY HONGBIN.CHEN 20150810
            /* MODIFIED-BEGIN by haifeng.tang, 2016-05-09,BUG-2104433*/
            if (result != FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL ||
                    result == FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS) {
                // ADD END FOR PR1050553 BY HONGBIN.CHEN 20150810
                /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
                if (CategoryManager.mCurrentCagegory >= 0 ||
                        CategoryManager.mCurrentSafeCategory >= 0) {
                        /* MODIFIED-END by songlin.qi,BUG-2227088*/
                    if (mApplication != null && mApplication.mFileInfoManager != null &&
                            mApplication.mFileInfoManager.getShowFileList().size() == 0) {
                        try {
//                            if (mNo_messageView != null) {
//                                mNo_messageView.setText(mActivity.getResources().getString(R.string.no_folder_cn)); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1913721 // MODIFIED by wenjing.ni, 2016-05-07,BUG-802835
//                            }
/* MODIFIED-END by haifeng.tang,BUG-2104433*/
                            showNoFolderResultView(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-08,BUG-1912698*/
                        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-16,BUG-1951763*/
                        if (SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN && mActivity instanceof FileSafeBrowserActivity) {
                            FileSafeBrowserActivity fileSafeBrowserActivity = (FileSafeBrowserActivity) mActivity;
                            fileSafeBrowserActivity.setAddSafeFileBtn(false);
                                /*MODIFIED-END by wenjing.ni,BUG-1951763*/
                        }
                        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-15,BUG-1950773*/
                        if (mNo_messageView != null && CategoryManager.isSafeCategory && SafeManager.mCurrentmode == SafeManager.FILE_NORMAL) {
                            mNo_messageView.setText(R.string.no_files_for_add_to_safebox);
                        }
                        /*MODIFIED-END by haifeng.tang,BUG-1950773*/
                        /*MODIFIED-END by haifeng.tang,BUG-1912698*/
                    } else {
                        showNoFolderResultView(false);
                        if (SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN) {
                            FileSafeBrowserActivity fileSafeBrowserActivity = (FileSafeBrowserActivity) mActivity;
                            fileSafeBrowserActivity.setAddSafeFileBtn(true);
                        }
                    }
                }
                LogUtils.e(TAG,"OnItemClick onTaskResult3333....");
                refreshAdapter();
                // add for PR845930 by yane.wang@jrdcom.com 20141127 begin
                FileInfo.mountReceiver = false;
                FileInfo.scanFinishReceiver = false;
                // add for PR845930 by yane.wang@jrdcom.com 20141127 end
                ((FileBrowserActivity)getActivity()).setEditItemIsVisiable();
            }
            // ADD START FOR PR511096 BY HONGBIN.CHEN 20150901
            if (!mmShowLoading)
                return;
            // ADD END FOR PR511096 BY HONGBIN.CHEN 20150901
            try {
                mHandler.removeMessages(CATEGORY_LIST_DIALOG_MSG); //MODIFIED by jian.xu, 2016-04-18,BUG-1868328 // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
                CategoryManager.mLastCagegory = CategoryManager.mCurrentCagegory;
                ProgressDialogFragment listDialogFragment = (ProgressDialogFragment) findFragmentByTag(CATEGORY_LIST_DIALOG_TAG);
                if (listDialogFragment != null) {
                    listDialogFragment.dismissAllowingStateLoss();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onTaskPrepare() {
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            // ADD START FOR PR511096 BY HONGBIN.CHEN 20150901
            if (!mmShowLoading)
                return;
            /* MODIFIED-BEGIN by haifeng.tang, 2016-05-09,BUG-2104433*/
            /*MODIFIED-BEGIN by jian.xu, 2016-04-18,BUG-1868328*/
            if (progressInfo != null && progressInfo.getTotal() < 0) {
                mHandler.sendEmptyMessageDelayed(CATEGORY_LIST_DIALOG_MSG, BaseAsyncTask.NEED_UPDATE_TIME);
                return;
            }
            /*MODIFIED-END by jian.xu,BUG-1868328*/
            /* MODIFIED-END by haifeng.tang,BUG-2104433*/
            // ADD END FOR PR511096 BY HONGBIN.CHEN 20150901
            if (progressInfo != null) {
                try {
                    ProgressDialogFragment listDialogFragment = (ProgressDialogFragment) findFragmentByTag(CATEGORY_LIST_DIALOG_TAG);
                    if (isResumed()) {
                        FragmentManager fm = getFragmentManager();
                        if (listDialogFragment == null && fm != null) {
                            listDialogFragment = ProgressDialogFragment
                                    .newInstance(ProgressDialog.STYLE_HORIZONTAL, -1,
                                            R.string.loading,
                                            AlertDialogFragment.INVIND_RES_ID);

                            listDialogFragment.show(fm, CATEGORY_LIST_DIALOG_TAG);
                            fm.executePendingTransactions();
                        }
                        listDialogFragment.setLoadProgress(progressInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (mApplication != null && mApplication.mService != null) {
                mApplication.mService.cancel(mActivity.getClass().getName());
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        LogUtils.e(TAG,"Enter FileBrowserFragment onItemLongClick....");
        if (mAdapter.isMode(FileInfoAdapter.MODE_NORMAL) || mAdapter.isMode(FileInfoAdapter.MODE_COPY)) {
            ((FileBrowserActivity) getActivity()).showEditButton();
            if (!MountManager.getInstance().isRootPath(mApplication.mCurrentPath)
                    && !mApplication.mService.isBusy(this.getClass().getName())) {
                if (mAbsListViewFragmentListener != null) {
                    mAbsListViewFragmentListener.HideActionbar(false);
                }
//                showHideToolbar();
                // ADD START FOR PR1061220 BY HONGBIN.CHEN 20150805
                if (position < mAdapter.getCount()) {
                    storeLastListPos();
                    int top = view.getTop();
                    switchToEditView(position, top);
                    FileInfo firstCheckedItem = mAdapter.getFirstCheckedFileInfoItem();
                    mFirstPosition = mApplication.mFileInfoManager.getShowFileList().indexOf(firstCheckedItem);
                    return true;
                }
                // ADD END FOR PR1061220 BY HONGBIN.CHEN 20150805
            }
        } else if (mAdapter.isMode(FileInfoAdapter.MODE_SEARCH)) {
            if (!mApplication.mService.isBusy(this.getClass().getName())) {
                int top = view.getTop();
                switchToEditView(position, top);
                return true;
            }
        } else if (mAdapter.isMode(FileInfoAdapter.MODE_GLOBALSEARCH)) {
            /** item long click not enter edit mode */
//            if (!mApplication.mService.isBusy(this.getClass().getName())) {
//                FileInfo selecteItemFileInfo = (FileInfo) mAdapter.getItem(position);
//                //if (selecteItemFileInfo.getFile().isDirectory()) {//PR-985568 Nicky Ni -001 20151128
//                if (CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE
//                        ) {
//                    CategoryManager.mCurrentMode = CategoryManager.PATH_MODE;
//                    // PR-984474 Nicky Ni -001 start
//                    mApplication.mCurrentPath = selecteItemFileInfo.getFileParentPath();
//                    // PR-984474 Nicky Ni -001 end
//                    refreshPathBar();
//                    if (mAbsListViewFragmentListener != null) {
//                        mAbsListViewFragmentListener.updateActionbar();
//                    }
//                    //mAbsListViewFragmentListener.updateDrawerListView();
//                }
//                mApplication.mRecordSearchPath = mApplication.mCurrentPath;
//                //}
//                int top = view.getTop();
//                switchToEditView(position, top);
//                updateActionMode(FileInfoAdapter.MODE_EDIT);
//                if (mAbsListViewFragmentListener != null) {
//                    mAbsListViewFragmentListener.updateActionbar();
//                }
//                ((FileBrowserActivity)getActivity()).setGlobalSearchViewUnable();
//                return true;
//            }
        }
        return false;
    }

    private void showSearchResultView() {
        switchToSearchView();
        //mApplication.mFileInfoManager.showSearchResultView(mApplication.mSortType);
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.reSearch();
        }
        mAdapter.clearChecked();
    }

    private void switchToSearchView() {
        mSearchMode = true;
        updateActionMode(FileInfoAdapter.MODE_SEARCH);
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.updateActionbar();
        }
    }

    private void switchToGlobalSearchView() {
        mSearchMode = true;
        updateActionMode(FileInfoAdapter.MODE_GLOBALSEARCH);
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.updateActionbar();
        }
    }

    private void switchToNormalView() {
        LogUtils.i(TAG, "switchToNormalView");
        mSearchMode = false;
        if (mAdapter != null) {
            mAdapter.changeModeFromSearchToNormal();
        }
        updateActionMode(FileInfoAdapter.MODE_NORMAL);
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.updateActionbar();
        }
    }

    protected void switchToEditView() {
        mSearchMode = false;//add for PR838074 by yane.wang@jrdcom.com 20141120
        if (mAdapter != null) {
            if (mAdapter.isMode(FileInfoAdapter.MODE_SEARCH)) {
                mFromSearchToEdit = true;
            }
            updateActionMode(FileInfoAdapter.MODE_EDIT);

        }
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.updateActionbar();
        }
        /** don,t update edit bar when add encrypt file mode */
        if (!mAdapter.isMode(FileInfoAdapter.MODE_ADD_ENCRYPT_FILE)) {
            updateEditBarByThread();
        }
    }

    public void updateActionMode(int mode) {
        if (mAdapter != null) {
            mAdapter.changeMode(mode);
        }
    }
    public int getActionMode() {
        if (mAdapter != null) {
            return mAdapter.getMode();
        }
        return -1;
    }

    //add for PR967486 by yane.wang@jrdcom.com 20150403 begin
    @Override
    public void onSaveInstanceState(Bundle outState) {
        mSavedState = true;
        super.onSaveInstanceState(outState);
    }
    //add for PR967486 by yane.wang@jrdcom.com 20150403 end

    private void showCreateFolderDialog() {
        //add for PR967486 by yane.wang@jrdcom.com 20150403 begin
        if (mSavedState) {
            return;
        }
        //add for PR967486 by yane.wang@jrdcom.com 20150403 end
        AlertDialogFragment.EditDialogFragmentBuilder builder = new AlertDialogFragment.EditDialogFragmentBuilder();
        String defaultName = getResources().getString(R.string.new_folder);
        builder.setDefault(defaultName, 0, false).setDoneTitle(R.string.create_folder)
                .setCancelTitle(R.string.cancel)
                .setTitle(R.string.add_folder); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1913721
        final AlertDialogFragment.EditTextDialogFragment createFolderDialogFragment = builder.create();
        createFolderListener = new CreateFolderListener(createFolderDialogFragment);
        createFolderDialogFragment.setOnEditTextDoneListener(createFolderListener);
//        createFolderDialogFragment.setOnEditTextDoneListener(new CreateFolderListener(createFolderDialogFragment));
        createFolderDialogFragment.setOnCancelListener(new OnClickListener() { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
            public void onClick(DialogInterface dialog, int id) {
                // cancel
                boolean cheatShowDialog = createFolderListener.getcheatShowDialog();
                if (cheatShowDialog) {
                    createFolderListener.setShowDialog(true);
                    createFolderListener.setcheatShowDialog(false);
                    dialog.dismiss();
                }
                hideGlobalSearchViewInputMethod(createFolderDialogFragment);
            }
        });
        createFolderDialogFragment.setDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (createFolderListener != null) {
                    createFolderListener = null;
                }
                hideGlobalSearchViewInputMethod(createFolderDialogFragment);
            }
        });
        createFolderDialogFragment.show(getFragmentManager(), CREATE_FOLDER_DIALOG_TAG);
    }

    private final class CreateFolderListener implements AlertDialogFragment.EditTextDialogFragment.EditTextDoneListener {
        private DialogFragment mDialogFragment;
        private boolean cheatShowDialog;

        public boolean getcheatShowDialog() {
            return cheatShowDialog;
        }

        public void setcheatShowDialog(boolean value) {
            cheatShowDialog = value;
        }
        public CreateFolderListener(DialogFragment dialogFragment) {
            mDialogFragment = dialogFragment;
        }

        public void onClick(String text) {
            if (TextUtils.isEmpty(text) || text.trim().length() == 0) {
                mToastHelper.showToast(R.string.invalid_empty_name);
                if (!cheatShowDialog) {
                    cheatShowDialog = true;
                    setShowDialog(false);
                }
                return;
            } else {
                setShowDialog(true);
                cheatShowDialog = false;
            }
            if (mApplication.mService != null) {
                if (TextUtils.isEmpty(mApplication.mCurrentPath) && CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
                    mApplication.mCurrentPath = SafeUtils.getEncryptRootPath(mActivity);
                }
                String dstPath = mApplication.mCurrentPath + MountManager.SEPARATOR + text;
                mApplication.mService.createFolder(mActivity.getClass().getName(), dstPath, new LightOperationListener(text));
            }
            hideGlobalSearchViewInputMethod(mDialogFragment);
        }

        private void setShowDialog(boolean value) {
            try {
                Field showingFiled = Dialog.class.getDeclaredField("mShowing");
                showingFiled.setAccessible(true);
                showingFiled.set(mDialogFragment.getDialog(), value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showRenameDialog() {
        FileInfo fileInfo = mAdapter.getFirstCheckedFileInfoItem();
        if (fileInfo == null && mAdapter.getItemEditSelect().size() > 0) {
            fileInfo = mAdapter.getItemEditSelect().get(0);
        }
        int selection = 0;
        if (fileInfo != null) {
            String name = fileInfo.getFileName();
            String fileExtension = FileUtils.getFileExtension(name);
            selection = name.length();
            if (!fileInfo.isDirectory() && fileExtension != null) {
                selection = selection - fileExtension.length() - 1;
            }
            AlertDialogFragment.EditDialogFragmentBuilder builder = new AlertDialogFragment.EditDialogFragmentBuilder();
            builder.setDefault(name, selection, false).setDoneTitle(R.string.save) //MODIFIED by haifeng.tang, 2016-04-09,BUG-1913721
                    .setCancelTitle(R.string.cancel)
                    .setTitle(R.string.rename);
            /* MODIFIED-BEGIN by zibin.wang, 2016-05-06,BUG-2019352*/
            final AlertDialogFragment.EditTextDialogFragment renameDialogFragment = builder.create();
            renameDialogFragment.setEditFile(fileInfo);
            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-21,BUG-1940832*/
            renameDoneListener = new RenameDoneListener(fileInfo, name, renameDialogFragment);
            renameDialogFragment.setOnEditTextDoneListener(renameDoneListener);
            renameDialogFragment.setOnCancelListener(new OnClickListener() { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
                public void onClick(DialogInterface dialog, int id) {
                    // cancel
                    boolean cheatShowDialog = renameDoneListener.getcheatShowDialog();
                    if (cheatShowDialog) {
                        renameDoneListener.setShowDialog(true);
                        renameDoneListener.setcheatShowDialog(false);
                        dialog.dismiss();
                    }
                    hideGlobalSearchViewInputMethod(renameDialogFragment);
                }
            });
            renameDialogFragment.setDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (renameDoneListener != null) {
                        renameDoneListener = null;
                    }
                    hideGlobalSearchViewInputMethod(renameDialogFragment);
                }
            });
            /* MODIFIED-END by zibin.wang,BUG-2019352*/
            renameDialogFragment.show(getFragmentManager(), RENAME_DIALOG_TAG);
        }
    }

    private void hideGlobalSearchViewInputMethod(DialogFragment dialogFragment) {
        InputMethodManager immEdit = (InputMethodManager) mActivity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (immEdit != null && dialogFragment != null && dialogFragment.getDialog() != null && dialogFragment.getDialog().getCurrentFocus() != null) {
            immEdit.hideSoftInputFromWindow(dialogFragment.getDialog().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private class RenameDoneListener implements AlertDialogFragment.EditTextDialogFragment.EditTextDoneListener {
        FileInfo mSrcfileInfo;
        private String mOriginalName;
        private DialogFragment mDialogFragment;
        /* MODIFIED-BEGIN by zibin.wang, 2016-05-06,BUG-2019352*/
        private boolean cheatShowDialog;

        public boolean getcheatShowDialog() {
            return cheatShowDialog;
        }

        public void setcheatShowDialog(boolean value) {
            cheatShowDialog = value;
        }

        /* MODIFIED-END by zibin.wang,BUG-2019352*/
        public RenameDoneListener(FileInfo srcFile, String originalName, DialogFragment dialogFragment) { // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329
            mSrcfileInfo = srcFile;
            mOriginalName = originalName;
            mDialogFragment = dialogFragment;
        }

        @Override
        public void onClick(String text) {

            if (TextUtils.equals(mOriginalName, text)) {
                mToastHelper.showToast(mActivity.getString(R.string.duplicate_file_name_tip));
                /* MODIFIED-BEGIN by zibin.wang, 2016-05-06,BUG-2019352*/
                // try to hold the dialog when click save button
                if (!cheatShowDialog) {
                    cheatShowDialog = true;
                    setShowDialog(false);
                }
                return;
            } else if (TextUtils.isEmpty(text) || text.trim().length() == 0) {
                mToastHelper.showToast(R.string.invalid_empty_name);
                if (!cheatShowDialog) {
                    cheatShowDialog = true;
                    setShowDialog(false);
                }
                return;
            } else {
                setShowDialog(true);
                cheatShowDialog = false;
                /* MODIFIED-END by zibin.wang,BUG-2019352*/
            }
            /* MODIFIED-END by haifeng.tang,BUG-1940832*/
            String newFilePath = null;
            if (mApplication.mCurrentPath == null || mAdapter.isMode(FileInfoAdapter.MODE_SEARCH)
                    || mAdapter.isMode(FileInfoAdapter.MODE_EDIT)) {/*PR 1245371 zibin.wang 12/29/2015 */
                String path = mSrcfileInfo.getFileParentPath();
                newFilePath = path + MountManager.SEPARATOR + text;
            } else {
                newFilePath = mApplication.mCurrentPath + MountManager.SEPARATOR + text;
            }
            if (null == mSrcfileInfo) {
                LogUtils.w(TAG, "mSrcfileInfo is null.");
                return;
            }
            // ADD START FOR PR1074164 BY HONGBIN.CHEN 20150824
            mIsBack = true;
            // ADD END FOR PR1074164 BY HONGBIN.CHEN 20150824

            if (FileUtils.isStartWithDot(newFilePath,
                    mSrcfileInfo.getFileAbsolutePath())) {
                showCheckFileStartDialog(mSrcfileInfo, newFilePath);
            } else {
                if (FileUtils.isExtensionChange(newFilePath,
                        mSrcfileInfo.getFileAbsolutePath())) {
                    showRenameExtensionDialog(mSrcfileInfo, newFilePath);
                } else {
                    ((FileBrowserActivity) getActivity()).hideEditWindow();
                    if (mApplication.mService != null) {
                        if (mAdapter.isMode(FileInfoAdapter.MODE_SEARCH) || mFromSearchToEdit) {
                            showSearchResultView();
                        } else {
                            //add by haifeng.tang@tcl.com for PR:1246475 start 2015-12-30
                            mSearchMessage = null;
                            //add by haifeng.tang@tcl.com for PR:1246475 end 2015-12-30
                            switchToNormalView();
                        }
                        mApplication.mService.rename(mActivity.getClass().getName(),
                                mSrcfileInfo, mSearchMessage,
                                new FileInfo(getActivity(),
                                        newFilePath),
                                new LightOperationListener(
                                        FileUtils.getFileName(newFilePath)), mAdapter.getMode(), mApplication.mCurrentPath);
                    }
                    /*PR 1050893 zibin.wang add Start*/
                    if (mApplication.mFileInfoManager.getSearchItemsCount() == 1) {
                        switchToNormalView();
                        mAdapter.clearList();
                        LogUtils.e(TAG,"Begin to freshAdapter...currentpath " + mApplication.mCurrentPath);
                        refreshAdapter(mApplication.mCurrentPath);
                        mFromSearchToEdit = false;
                        if (mAbsListViewFragmentListener != null) {
                            mAbsListViewFragmentListener.changeSearchMode(false);
                        }
                        isChanageMode = true;
                    }
                    /*PR 1050893 zibin.wang add Start*/
//                    if (mAdapter.isMode(FileInfoAdapter.MODE_SEARCH) ) {
//                        mAbsListViewFragmentListener.reSearch();
//                    }
                }
            }
            hideGlobalSearchViewInputMethod(mDialogFragment);
        }

        /* MODIFIED-BEGIN by haifeng.tang, 2016-04-21,BUG-1940832*/
        private void setShowDialog(boolean value) {
            try {
                Field showingFiled = Dialog.class.getDeclaredField("mShowing");
                showingFiled.setAccessible(true);
               /* MODIFIED-BEGIN by zibin.wang, 2016-05-06,BUG-2019352*/
                //howingFiled. // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
                showingFiled.set(mDialogFragment.getDialog(), value);
            } catch (Exception e) {
            /* MODIFIED-END by zibin.wang,BUG-2019352*/
                e.printStackTrace();
            }
        }
        /* MODIFIED-END by haifeng.tang,BUG-1940832*/
    }

    private class RenameExtensionListener implements OnClickListener {

        private final String mNewFilePath;
        private final FileInfo mSrcFile;

        public RenameExtensionListener(FileInfo fileInfo, String newFilePath) {
            mNewFilePath = newFilePath;
            mSrcFile = fileInfo;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mApplication.mService != null) {
                switchToNormalView();
                mApplication.mService.rename(
                        mActivity.getClass().getName(),
                        mSrcFile, mSearchMessage,
                        new FileInfo(getActivity(), mNewFilePath),
                        new LightOperationListener(FileUtils.getFileName(mNewFilePath)), mAdapter.getMode(), mApplication.mCurrentPath);
                updatePasteBtn();
                ((FileBrowserActivity) getActivity()).hideEditWindow();
            }
        }

    }

    /**
     * The method creates an alert check file name start with "." dialog
     *
     * @param args argument, the boolean value who will indicates whether the
     *             selected files just only one. The prompt message will be
     *             different.
     * @return a dialog
     */
    private void showCheckFileStartDialog(FileInfo srcfileInfo, final String newFilePath) {
        AlertDialogFragment.AlertDialogFragmentBuilder builder = new AlertDialogFragment.AlertDialogFragmentBuilder();
        AlertDialogFragment checkFileStartDialogFragment = builder
                .setTitle(R.string.confirm_rename)
                .setIcon(R.drawable.ic_dialog_alert_holo_light)
                .setMessage(R.string.create_hidden_file)
                .setCancelTitle(R.string.cancel)
                .setDoneTitle(R.string.ok).create();
        checkFileStartDialogFragment.getArguments().putString(NEW_FILE_PATH_KEY, newFilePath);
        checkFileStartDialogFragment.setOnDoneListener(new RenameExtensionListener(srcfileInfo, newFilePath));
        checkFileStartDialogFragment.show(getFragmentManager(), RENAME_EXTENSION_DIALOG_TAG);
    }

    /**
     * The method creates an alert delete dialog
     *
     * @param args argument, the boolean value who will indicates whether the
     *             selected files just only one. The prompt message will be
     *             different.
     * @return a dialog
     */
    private void showRenameExtensionDialog(FileInfo srcfileInfo, final String newFilePath) {
        AlertDialogFragment.AlertDialogFragmentBuilder builder = new AlertDialogFragment.AlertDialogFragmentBuilder();
        AlertDialogFragment renameExtensionDialogFragment = builder
                .setTitle(R.string.confirm_rename)
                .setIcon(R.drawable.ic_dialog_alert_holo_light)
                .setMessage(R.string.msg_rename_ext)
                .setCancelTitle(R.string.cancel)
                .setDoneTitle(R.string.ok).create();
        renameExtensionDialogFragment.getArguments().putString(NEW_FILE_PATH_KEY, newFilePath);
        LogUtils.e(TAG,"RenameExtenseionDialog newFilePath :" + newFilePath + " srcFileInfo Name" + srcfileInfo.getFileName());
        renameExtensionDialogFragment.setOnDoneListener(new RenameExtensionListener(srcfileInfo, newFilePath));
        renameExtensionDialogFragment.show(getFragmentManager(), RENAME_EXTENSION_DIALOG_TAG);
    }

    private class LightOperationListener implements FileManagerService.OperationEventListener { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433

        String mDstName = null;

        LightOperationListener(String dstName) {
            mDstName = dstName;
        }

        @Override
        public void onTaskResult(int errorType) {
            mApplication.currentOperation = FileManagerApplication.OTHER;
            LogUtils.e(TAG,"OnItemClick onTaskResult33333....");
            switch (errorType) {
                case ERROR_CODE_SUCCESS:
                case ERROR_CODE_USER_CANCEL:
                    if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_RECENT) {
                        LogUtils.e(TAG,"Category Type is Recent.");
                        if (mApplication.mService != null) {
                            mApplication.mService.listCategoryFiles(mActivity.getClass()
                                    .getName(), CategoryManager.CATEGORY_RECENT, mActivity, new CategoryListListener(CategoryManager.CATEGORY_RECENT, true), CategoryTask.LIST_MODE_VIEW);
                        }
                    } else {
                        FileInfo fileInfo;
                        LogUtils.e(TAG,"File current Cagegory is " + CategoryManager.mCurrentCagegory);
                        if (mApplication.mCurrentPath == null ||
                                CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES ||
                                CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_MUSIC ||
                                CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_VEDIOS ||
                                CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_APKS ||
                                CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_DOWNLOAD) {    //modify by liaoah
                            fileInfo = mApplication.mFileInfoManager.updateOneCategoryFileInfoList(mApplication.mSortType);
                            LogUtils.e(TAG,"File current path22 is " + mApplication.mCurrentPath);
                        } else {
                            LogUtils.e(TAG,"mApplication.mFileInfoManager.getAddFilesInfoList() " + mApplication.mFileInfoManager.getAddFilesInfoList().size() + " Name" +
                                    mApplication.mFileInfoManager.getAddFilesInfoList().get(0).getFileName());
                            fileInfo = mApplication.mFileInfoManager.updateOneFileInfoList(mApplication.mCurrentPath, mApplication.mSortType);
                            LogUtils.e(TAG,"File current path333 is " + mApplication.mCurrentPath);
                        }
                        LogUtils.e(TAG,"File current path is " + mApplication.mCurrentPath);
                        if (mDstName != null && mDstName.startsWith(".")) {
                            mToastHelper.showToast(R.string.create_hidden_file);
                        }
                        if (mApplication.mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_CUT
                                || mApplication.mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_COPY) {
                            updatePasteBtn();
                        }
                        /*
                        for (int index = 0; index < mApplication.mFileInfoManager.getAddFilesInfoList().size(); index++) {
                            mApplication.mFileInfoManager.getShowFileList().add(mApplication.mFileInfoManager.getAddFilesInfoList().get(index));
                        }*/
                        LogUtils.e(TAG,"mApplication.mFileInfoManager.getAddFilesInfoList() " + mApplication.mFileInfoManager.getAddFilesInfoList().size());
                        //mApplication.mFileInfoManager.getShowFileList().addAll(mApplication.mFileInfoManager.getAddFilesInfoList());
                        refreshAdapter();

                        // NullPointerException, the fragment maybe is not attached or
                        // destroyed
                        if (mAdapter != null && fileInfo != null) {
                            int postion = mApplication.mFileInfoManager.getShowFileList().indexOf(fileInfo);
                            setViewPostion(postion);
                        }
                    }
                    break;
                case ERROR_CODE_FILE_EXIST:
                    LogUtils.e(TAG, "ERROR_CODE_FILE_EXIST  mDstName=" + mDstName);
                    if (mDstName != null) {
                        mToastHelper.showToast(getResources().getString(R.string.already_exists, mDstName));
                    }
                    break;
                case ERROR_CODE_NAME_EMPTY:
                    mToastHelper.showToast(R.string.invalid_empty_name);
                    break;
                case ERROR_CODE_NAME_TOO_LONG:
                    mToastHelper.showToast(R.string.file_name_too_long);
                    break;
                case ERROR_CODE_NOT_ENOUGH_SPACE:
                /* PR 1067293 zibin.wang add Start */
                    //mToastHelper.showToast(R.string.insufficient_memory);
                    mToastHelper.showToast(R.string.insufficient_message_cn); // MODIFIED by haifeng.tang, 2016-05-11,BUG-2104433
                /* PR 1067293 zibin.wang add End */
                    break;
                case ERROR_CODE_UNSUCCESS:
                    mToastHelper.showToast(R.string.operation_fail);
                    break;
                // [BUGFIX]-Mod-BEGIN by TSNJ,qinglian.zhang,09/03/2014,PR-776005
                case ERROR_INVALID_CHAR:
                    mToastHelper.showToast(R.string.invalid_char_prompt);
                    break;
                // [BUGFIX]-Mod-END by TSNJ,qinglian.zhang,
                default:
                    LogUtils.e(TAG, "wrong errorType for LightOperationListener");
                    break;
            }
            clearFailedFiles(); // MODIFIED by songlin.qi, 2016-06-12,BUG-1989911
        }

        public void onTaskPrepare() {
        }

        public void onTaskProgress(ProgressInfo progressInfo) {
        }
    }

    public void setViewPostion(int position) {
    }


    public boolean getDownloadEditStatus() {
        List<FileInfo> fileInfo = mAdapter.getItemEditFileInfoList();
        int status = -1;
        if (fileInfo != null) {
            for (FileInfo info : fileInfo) {
                if (info.getTotalBytes() != info.getCurrentBytes() && info.getTotalBytes() != 0) {
                    status = 1;
                    break;
                }
            }
        }
        if (status == 1) {
            return true;
        } else {
            return false;
        }
    }

    private void share() {
        Intent intent;
        List<FileInfo> files = null;
        ArrayList<Parcelable> sendList = new ArrayList<Parcelable>();
        LogUtils.e(TAG,"Enter share function....");

//        if (!mAdapter.isMode(FileInfoAdapter.MODE_EDIT)) {
//            return;
//        }

        files = mAdapter.getItemEditFileInfoList();
//        if(files.size() == 0){
//            files = mAdapter.getItemEditFileInfoList();
//        }
        if (files.size() > 100) {
            isShareSizeExceed = true;
            showNoShareDialog();
            return;
        } else {
            isShareSizeExceed = false;
        }
        if (files.size() > 1) {
            for (FileInfo info : files) {
                if (info.isDrmFile() && !DrmManager.getInstance(getActivity().getApplicationContext()).isAllowForward(info.getFileAbsolutePath())) {
                    forbidden = true;
                    break;
                }
                LogUtils.e(TAG,"Enter share function2...info uri " + info.getUri());
                sendList.add(info.getUri());
            }
            LogUtils.e(TAG,"Enter share function2...info forbiden " + forbidden);

            if (!forbidden) {
                intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                //add for PR896567 by yane.wang@jrdcom.com 20150109 begin
                intent.setType("*/*");
                //add for PR896567 by yane.wang@jrdcom.com 20150109 end
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, sendList);

                try {
                    startActivity(Intent.createChooser(intent, getString(R.string.send_file)));
                } catch (ActivityNotFoundException e) { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
                    e.printStackTrace();
                }
            }
        } else {
            // send single file
            FileInfo fileInfo = files.get(0);
            //add for PR914390 by yane.wang@jrdcom.com 20150127 begin
            File file = new File(fileInfo.getFileAbsolutePath());
            String mimeType = fileInfo.getShareMimeType();
            //add for PR914390 by yane.wang@jrdcom.com 20150127 end

            if (fileInfo.isDrmFile() && !DrmManager.getInstance(getActivity().getApplicationContext()).isAllowForward(fileInfo.getFileAbsolutePath())) {
                forbidden = true;
            }

            if (TextUtils.isEmpty(mimeType) || mimeType.startsWith("unknown")) {
                mimeType = FileInfo.MIMETYPE_UNRECOGNIZED;
            }

            if (!forbidden) {
                intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType(mimeType);
                Uri uri = Uri.fromFile(fileInfo.getFile());
                intent.putExtra(Intent.EXTRA_STREAM, uri);

                try {
                    startActivity(Intent.createChooser(intent, getString(R.string.send_file)));
                } catch (ActivityNotFoundException e) { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
                    e.printStackTrace();
                }
            }
        }

        if (!isShareSizeExceed) {
            if (forbidden) {
                if (mAbsListViewFragmentListener != null) {
                    mAbsListViewFragmentListener.toShowForbiddenDialog();
                }
            } else {
                if (mFromSearchToEdit) {
                    showSearchResultView();
                } else {
                    switchToNormalView();
                }
            }
        }
    }

    private String toDateTimeString(Long sec) {
        Date date = new Date(sec.longValue() * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        String str = dateFormat.format(date);
        return str;
    }

    private class DetailInfoListener implements FileManagerService.OperationEventListener, OnDismissListener { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
        private TextView mDetailsText;
        private final String mName;
        private String mSize;
        private final String mModifiedTime;
        private final String mPermission;
        private long time;
        private final StringBuilder mStringBuilder = new StringBuilder();

        private boolean isDrm = false;
        private String mLicesesIssuerURL = "";// only for SD file type
        private String mCuttentConstraint;
        private String mCurrentCount;
        private String mDetailName;
        private String mDetailSize;
        public String mDetailPath;
        public String mDetailFilePath;
        public String mDetailPhonePath;
        public String mDetailSdPath;
        public String mDetailOtgPath;
        private String mDetailModifyTime;
        private String mDetailRead;
        private String mDetailWrite;
        private String mDetailExecute;
        private String mDrmCurrentCount;
        private String mDrmCurrentConstraint;
        private String mDrmLicesesIssuerURL;
        private String mProtectionStatus;
        private String mLicenseStartTime;
        private String mLicenseEndTime;
        private String mRemains;

        public DetailInfoListener(FileInfo fileInfo) {
            mStringBuilder.setLength(0);
            mName = mStringBuilder.append(getString(R.string.name))
                    .append(fileInfo.getFileName()).append("\n")
                    .toString();
            LogUtils.e(TAG,"Name is " + mName);
            mStringBuilder.setLength(0);
            mSize = mStringBuilder.append(getString(R.string.size))
                    .append(FileUtils.sizeToString(getActivity(), 0)).append(
                            " \n").toString();

            time = fileInfo.getFileLastModifiedTime();

            mStringBuilder.setLength(0);
            mModifiedTime = mStringBuilder.append(
                    getString(R.string.modified_time)).append(
                    DateFormat.getDateInstance().format(new Date(time)))
                    .append("\n").toString();
            mStringBuilder.setLength(0);
            mPermission = getPermission(fileInfo.getFile());
            getDetailValue(fileInfo);

        }

        public DetailInfoListener(FileInfo fileInfo, String path) {
            isDrm = fileInfo.isDrmFile();
            boolean isRightValid = DrmManager.getInstance(getActivity().getApplicationContext()).isRightsStatus(path);
            int schema = DrmManager.getInstance(getActivity().getApplicationContext()).getDrmScheme(path);
            String mMimeType = fileInfo.getMimeType();
            //DrmManager.getInstance();
            int mAction = DrmManager.getAction(mMimeType);

            mStringBuilder.setLength(0);
            mName = mStringBuilder.append(getString(R.string.name))
                    .append(": ").append(fileInfo.getFileName()).append("\n")
                    .toString();
            mStringBuilder.setLength(0);
            mSize = mStringBuilder.append(getString(R.string.size))
                    .append(": ").append(FileUtils.sizeToString(getActivity(), 0)).append(
                            " \n").toString();

            time = fileInfo.getFileLastModifiedTime();

            mStringBuilder.setLength(0);
            mModifiedTime = mStringBuilder.append(
                    getString(R.string.modified_time)).append(": ").append(
                    DateFormat.getDateInstance().format(new Date(time)))
                    .append("\n").toString();
            mStringBuilder.setLength(0);
            mPermission = getPermission(fileInfo.getFile()) + "\n";
            mStringBuilder.setLength(0);
            getDetailValue(fileInfo);

            if (DrmManager.mCurrentDrm == 20) {
                if (schema == DrmManager.DRM_SCHEME_OMA1_FL) {
                    if (isRightValid) {
                        mCurrentCount = mStringBuilder
                                .append(getString(R.string.drm_current_count))
                                .append(": ")
                                .append(getString(R.string.unlimited_usage))
                                .append("\n").toString();
                        mDrmCurrentCount = getString(R.string.unlimited_usage);
                        mStringBuilder.setLength(0);
                        mCuttentConstraint = mStringBuilder
                                .append(getString(R.string.drm_current_right))
                                .append(": ")
                                .append(getString(R.string.unlimited_usage))
                                .append("\n").toString();
                        mDrmCurrentConstraint = getString(R.string.unlimited_usage);
                    } else {
                        mCurrentCount = mStringBuilder
                                .append(getString(R.string.drm_current_count))
                                .append(": ")
                                .append(getString(R.string.not_available))
                                .append("\n").toString();
                        mDrmCurrentCount = getString(R.string.not_available);
                        mStringBuilder.setLength(0);
                        mCuttentConstraint = mStringBuilder
                                .append(getString(R.string.drm_current_right))
                                .append(": ")
                                .append(getString(R.string.not_available))
                                .append("\n").toString();
                        mDrmCurrentConstraint = getString(R.string.not_available);
                    }
                    mStringBuilder.setLength(0);
                } else if (schema == DrmManager.DRM_SCHEME_OMA1_CD
                        || schema == DrmManager.DRM_SCHEME_OMA1_SD
                        || schema == DrmManager.METHOD_SD) {// for CD && SD type
                    // add for PR863409,PR866854 by yane.wang@jrdcom.com
                    // 20141212
                    if (schema == DrmManager.DRM_SCHEME_OMA1_SD
                            || schema == DrmManager.METHOD_SD) {// for SD only
                        ContentValues contentValue = DrmManager.getInstance(getActivity().getApplicationContext())
                                .getMetadata(path);
                        if (contentValue != null) {
                            mLicesesIssuerURL = mStringBuilder
                                    .append(getString(R.string.right_url))
                                    .append(": ")
                                    .append(contentValue
                                            .getAsString(DrmManager.RIGHTS_ISSUER))
                                    .append("\n").toString();
                            mDrmLicesesIssuerURL = contentValue
                                    .getAsString(DrmManager.RIGHTS_ISSUER);
                        }
                    }
                    mStringBuilder.setLength(0);
                    if (isRightValid) {
                        ContentValues cv = DrmManager.getInstance(getActivity().getApplicationContext())
                                .getConstraints(path, mAction);
                        if (null != cv) {
                            String constrainType = cv.getAsString(DrmManager.CONSTRAINT_TYPE);
                            if (null == constrainType) {
                                mCuttentConstraint = mStringBuilder
                                        .append(getString(R.string.drm_current_right))
                                        .append(": ")
                                        .append(getString(R.string.not_available))
                                        .append("\n").toString();
                                mStringBuilder.setLength(0);
                                mDrmCurrentConstraint = getString(R.string.not_available);
                            }
                            if (!TextUtils.isEmpty(cv.getAsString(DrmManager.REMAINING_REPEAT_COUNT))) {
                                String useTime = null;
                                try {
                                    int times = Integer.parseInt(cv.getAsString(DrmManager.REMAINING_REPEAT_COUNT));
                                    useTime = String.format(getString(R.string.use_times), "" + times);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (null != useTime) {
                                    mCurrentCount = mStringBuilder
                                            .append(getString(R.string.drm_current_count))
                                            .append(": ").append(useTime)
                                            .append("\n").toString();
                                    mDrmCurrentCount = useTime;
                                    mStringBuilder.setLength(0);
                                } else {
                                    mCurrentCount = mStringBuilder
                                            .append(getString(R.string.drm_current_count))
                                            .append(": ")
                                            .append(getString(R.string.not_available))
                                            .append("\n").toString();
                                    mDrmCurrentCount = getString(R.string.not_available);
                                    mStringBuilder.setLength(0);
                                }
                            } else {
                                mCurrentCount = mStringBuilder
                                        .append(getString(R.string.drm_current_count))
                                        .append(": ")
                                        .append(getString(R.string.not_available))
                                        .append("\n").toString();
                                mDrmCurrentCount = getString(R.string.not_available);
                                mStringBuilder.setLength(0);
                            }
                            if ("count".equalsIgnoreCase(constrainType)) {
                                String useTime = null;
                                try {
                                    int times = Integer.parseInt(cv.getAsString(DrmManager.REMAINING_REPEAT_COUNT));
                                    useTime = String.format(getString(R.string.use_times), "" + times);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (null != useTime) {
                                    mCuttentConstraint = mStringBuilder
                                            .append(getString(R.string.drm_current_right))
                                            .append(": ").append(useTime)
                                            .append("\n").toString();
                                    mStringBuilder.setLength(0);
                                    mDrmCurrentConstraint = useTime;
                                } else {
                                    mCuttentConstraint = mStringBuilder
                                            .append(getString(R.string.drm_current_right))
                                            .append(": ")
                                            .append(getString(R.string.not_available))
                                            .append("\n").toString();
                                    mDrmCurrentConstraint = getString(R.string.not_available);
                                    mStringBuilder.setLength(0);
                                }
                            } else if ("datetime".equalsIgnoreCase(constrainType)) {
                                String startTime = getString(R.string.valid_after) + " " + cv.getAsString(DrmManager.LICENSE_START_TIME);
                                String endTime = getString(R.string.valid_until) + " " + cv.getAsString(DrmManager.LICENSE_EXPIRY_TIME);
                                if (TextUtils.isEmpty(startTime)) {
                                    startTime = getString(R.string.unlimited_usage);
                                }
                                if (TextUtils.isEmpty(endTime)) {
                                    startTime += "\n" + getString(R.string.unlimited_usage);
                                } else {
                                    startTime += "\n" + endTime;
                                }
                                mCuttentConstraint = mStringBuilder
                                        .append(getString(R.string.drm_current_right))
                                        .append(": ").append(startTime)
                                        .append("\n").toString();
                                mDrmCurrentConstraint = startTime;
                                mStringBuilder.setLength(0);
                            } else if ("interval".equalsIgnoreCase(constrainType)) {
                                String interval = cv.getAsString(DrmManager.LICENSE_AVAILABLE_TIME);
                                int line_index = interval.indexOf("-");
                                String year = interval.substring(0, line_index);
                                line_index = interval.indexOf("-", line_index + 1);
                                String month = interval.substring(line_index - 2, line_index);
                                String day = interval.substring(line_index + 1, line_index + 3);
                                int colon_index = interval.indexOf(":");
                                String hour = interval.substring(colon_index - 2, colon_index);
                                colon_index = interval.indexOf(":", colon_index + 1);
                                String minute = interval.substring(colon_index - 2, colon_index);
                                String second = interval.substring(colon_index + 1, colon_index + 3);
                                year = (year.equalsIgnoreCase("0000") ? "" : ("" + Integer.parseInt(year) + "Year-"));
                                month = (month.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(month) + "Month-"));
                                day = (day.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(day) + "Day "));
                                hour = (hour.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(hour) + "Hour "));
                                minute = (minute.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(minute) + "Minute "));
                                second = (second.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(second) + "Second"));
                                interval = getString(R.string.drm_interval, year + month + day + hour + minute + second);
                                mCuttentConstraint = mStringBuilder
                                        .append(getString(R.string.drm_current_right))
                                        .append(": ").append(interval)
                                        .append("\n").toString();
                                mDrmCurrentConstraint = interval;
                                mStringBuilder.setLength(0);
                            }
                        }
                    } else {
                        mCurrentCount = mStringBuilder
                                .append(getString(R.string.drm_current_count))
                                .append(": ")
                                .append(getString(R.string.not_available))
                                .append("\n").toString();
                        mDrmCurrentCount = getString(R.string.not_available);
                        mStringBuilder.setLength(0);
                        mCuttentConstraint = mStringBuilder
                                .append(getString(R.string.drm_current_right))
                                .append(": ")
                                .append(getString(R.string.not_available))
                                .append("\n").toString();
                        mDrmCurrentConstraint = getString(R.string.not_available);
                    }
                }
                // add for PR863409,PR866854 by yane.wang@jrdcom.com 20141212 begin
                else {
                    mCurrentCount = mStringBuilder
                            .append(getString(R.string.drm_current_count))
                            .append(": ")
                            .append(getString(R.string.not_available))
                            .append("\n").toString();
                    mDrmCurrentCount = getString(R.string.not_available);
                    mStringBuilder.setLength(0);
                    mCuttentConstraint = mStringBuilder
                            .append(getString(R.string.drm_current_right))
                            .append(": ")
                            .append(getString(R.string.not_available))
                            .append("\n").toString();
                    mDrmCurrentConstraint = getString(R.string.not_available);
                }
                // add for PR863409,PR866854 by yane.wang@jrdcom.com 20141212 end
                // add for MTK Drm by yane.wang@jrdcom.com 20150420 begin
            } else if (DrmManager.mCurrentDrm == 10) {
                mCuttentConstraint = "";
                try {
                    boolean rightsStatus = DrmManager.getInstance(getActivity().getApplicationContext()).canTransfer(path);
                    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1913721*/
                    if (rightsStatus == true) {
                        {
                            if (schema == DrmManager.DRM_SCHEME_OMA1_FL) {
                                if (isRightValid) {
                                    mCurrentCount = mStringBuilder
                                            .append(getString(R.string.drm_current_count))
                                            .append(": ")
                                            .append(getString(R.string.unlimited_usage))
                                            .append("\n").toString();
                                    mDrmCurrentCount = getString(R.string.unlimited_usage);
                                    mStringBuilder.setLength(0);
                                    mCuttentConstraint = mStringBuilder
                                            .append(getString(R.string.drm_current_right))
                                            .append(": ")
                                            .append(getString(R.string.unlimited_usage))
                                            .append("\n").toString();
                                    mDrmCurrentConstraint = getString(R.string.unlimited_usage);
                                } else {
                                    mCurrentCount = mStringBuilder
                                            .append(getString(R.string.drm_current_count))
                                            .append(": ")
                                            .append(getString(R.string.not_available))
                                            .append("\n").toString();
                                    mDrmCurrentCount = getString(R.string.not_available);
                                    mStringBuilder.setLength(0);
                                    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-14,BUG-1943981*/
                                    mCuttentConstraint = mStringBuilder
                                            .append(getString(R.string.drm_current_right))
                                            .append(": ")
                                            .append(getString(R.string.not_available))
                                            .append("\n").toString();
                                    mDrmCurrentConstraint = getString(R.string.not_available);
                                }
                                mStringBuilder.setLength(0);
                            } else if (schema == DrmManager.DRM_SCHEME_OMA1_CD
                                    || schema == DrmManager.DRM_SCHEME_OMA1_SD
                                    || schema == DrmManager.METHOD_SD) {// for CD && SD type
                                // add for PR863409,PR866854 by yane.wang@jrdcom.com
                                // 20141212
                                if (schema == DrmManager.DRM_SCHEME_OMA1_SD
                                        || schema == DrmManager.METHOD_SD) {// for SD only
                                    ContentValues contentValue = DrmManager.getInstance(getActivity().getApplicationContext())
                                            .getMetadata(path);
                                    if (contentValue != null) {
                                        mLicesesIssuerURL = mStringBuilder
                                                .append(getString(R.string.right_url))
                                                .append(": ")
                                                .append(contentValue
                                                        .getAsString(DrmManager.RIGHTS_ISSUER))
                                                .append("\n").toString();
                                        mDrmLicesesIssuerURL = contentValue
                                                .getAsString(DrmManager.RIGHTS_ISSUER);
                                    }
                                }
                                mStringBuilder.setLength(0);
                                if (isRightValid) {
                                    ContentValues cv = DrmManager.getInstance(getActivity().getApplicationContext())
                                            .getConstraints(path, mAction);
                                    if (null != cv) {
                                        String constrainType = cv.getAsString(DrmManager.CONSTRAINT_TYPE);
                                        if (null == constrainType) {
                                            mCuttentConstraint = mStringBuilder
                                                    .append(getString(R.string.drm_current_right))
                                                    .append(": ")
                                                    .append(getString(R.string.not_available))
                                                    .append("\n").toString();
                                            mStringBuilder.setLength(0);
                                            mDrmCurrentConstraint = getString(R.string.not_available);
                                        }
                                        if (!TextUtils.isEmpty(cv.getAsString(DrmManager.REMAINING_REPEAT_COUNT))) {
                                            String useTime = null;
                                            try {
                                                int times = Integer.parseInt(cv.getAsString(DrmManager.REMAINING_REPEAT_COUNT));
                                                useTime = String.format(getString(R.string.use_times), "" + times);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            if (null != useTime) {
                                                mCurrentCount = mStringBuilder
                                                        .append(getString(R.string.drm_current_count))
                                                        .append(": ").append(useTime)
                                                        .append("\n").toString();
                                                mDrmCurrentCount = useTime;
                                                mStringBuilder.setLength(0);
                                            } else {
                                                mCurrentCount = mStringBuilder
                                                        .append(getString(R.string.drm_current_count))
                                                        .append(": ")
                                                        .append(getString(R.string.not_available))
                                                        .append("\n").toString();
                                                mDrmCurrentCount = getString(R.string.not_available);
                                                mStringBuilder.setLength(0);
                                            }
                                        } else {
                                            mCurrentCount = mStringBuilder
                                                    .append(getString(R.string.drm_current_count))
                                                    .append(": ")
                                                    .append(getString(R.string.not_available))
                                                    .append("\n").toString();
                                            mDrmCurrentCount = getString(R.string.not_available);
                                            mStringBuilder.setLength(0);
                                        }
                                        if ("count".equalsIgnoreCase(constrainType)) {
                                            String useTime = null;
                                            try {
                                                int times = Integer.parseInt(cv.getAsString(DrmManager.REMAINING_REPEAT_COUNT));
                                                useTime = String.format(getString(R.string.use_times), "" + times);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            if (null != useTime) {
                                                mCuttentConstraint = mStringBuilder
                                                        .append(getString(R.string.drm_current_right))
                                                        .append(": ").append(useTime)
                                                        .append("\n").toString();
                                                mStringBuilder.setLength(0);
                                                mDrmCurrentConstraint = useTime;
                                            } else {
                                                mCuttentConstraint = mStringBuilder
                                                        .append(getString(R.string.drm_current_right))
                                                        .append(": ")
                                                        .append(getString(R.string.not_available))
                                                        .append("\n").toString();
                                                mDrmCurrentConstraint = getString(R.string.not_available);
                                                mStringBuilder.setLength(0);
                                            }
                                        } else if ("datetime".equalsIgnoreCase(constrainType)) {
                                            String startTime = getString(R.string.valid_after) + " " + cv.getAsString(DrmManager.LICENSE_START_TIME);
                                            String endTime = getString(R.string.valid_until) + " " + cv.getAsString(DrmManager.LICENSE_EXPIRY_TIME);
                                            if (TextUtils.isEmpty(startTime)) {
                                                startTime = getString(R.string.unlimited_usage);
                                            }
                                            if (TextUtils.isEmpty(endTime)) {
                                                startTime += "\n" + getString(R.string.unlimited_usage);
                                            } else {
                                                startTime += "\n" + endTime;
                                            }
                                            mCuttentConstraint = mStringBuilder
                                                    .append(getString(R.string.drm_current_right))
                                                    .append(": ").append(startTime)
                                                    .append("\n").toString();
                                            mDrmCurrentConstraint = startTime;
                                            mStringBuilder.setLength(0);
                                        } else if ("interval".equalsIgnoreCase(constrainType)) {
                                            String interval = cv.getAsString(DrmManager.LICENSE_AVAILABLE_TIME);
                                            int line_index = interval.indexOf("-");
                                            String year = interval.substring(0, line_index);
                                            line_index = interval.indexOf("-", line_index + 1);
                                            String month = interval.substring(line_index - 2, line_index);
                                            String day = interval.substring(line_index + 1, line_index + 3);
                                            int colon_index = interval.indexOf(":");
                                            String hour = interval.substring(colon_index - 2, colon_index);
                                            colon_index = interval.indexOf(":", colon_index + 1);
                                            String minute = interval.substring(colon_index - 2, colon_index);
                                            String second = interval.substring(colon_index + 1, colon_index + 3);
                                            year = (year.equalsIgnoreCase("0000") ? "" : ("" + Integer.parseInt(year) + "Year-"));
                                            month = (month.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(month) + "Month-"));
                                            day = (day.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(day) + "Day "));
                                            hour = (hour.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(hour) + "Hour "));
                                            minute = (minute.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(minute) + "Minute "));
                                            second = (second.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(second) + "Second"));
                                            interval = getString(R.string.drm_interval, year + month + day + hour + minute + second);
                                            mCuttentConstraint = mStringBuilder
                                                    .append(getString(R.string.drm_current_right))
                                                    .append(": ").append(interval)
                                                    .append("\n").toString();
                                            mDrmCurrentConstraint = interval;
                                            mStringBuilder.setLength(0);
                                        }
                                    }
                                } else {
                                    mCurrentCount = mStringBuilder
                                            .append(getString(R.string.drm_current_count))
                                            .append(": ")
                                            .append(getString(R.string.not_available))
                                            .append("\n").toString();
                                    mDrmCurrentCount = getString(R.string.not_available);
                                    mStringBuilder.setLength(0);
                                    mCuttentConstraint = mStringBuilder
                                            .append(getString(R.string.drm_current_right))
                                            .append(": ")
                                            .append(getString(R.string.not_available))
                                            .append("\n").toString();
                                    mDrmCurrentConstraint = getString(R.string.not_available);
                                }
                            }
                            // add for PR863409,PR866854 by yane.wang@jrdcom.com 20141212 begin
                            else {
                                mCurrentCount = mStringBuilder
                                        .append(getString(R.string.drm_current_count))
                                        .append(": ")
                                        .append(getString(R.string.not_available))
                                        .append("\n").toString();
                                mDrmCurrentCount = getString(R.string.not_available);
                                mStringBuilder.setLength(0);
                                mCuttentConstraint = mStringBuilder
                                        .append(getString(R.string.drm_current_right))
                                        .append(": ")
                                        .append(getString(R.string.not_available))
                                        .append("\n").toString();
                                mDrmCurrentConstraint = getString(R.string.not_available);
                            }
                            // add for PR863409,PR866854 by yane.wang@jrdcom.com 20141212 end
                            // add for MTK Drm by yane.wang@jrdcom.com 20150420 begin
                            /*MODIFIED-END by haifeng.tang,BUG-1943981*/
                        }
            /*MODIFIED-END by haifeng.tang,BUG-1913721*/
                        mProtectionStatus = getString(R.string.drm_can_forward);
                    } else if (rightsStatus == false) {
                        mProtectionStatus = getString(R.string.drm_can_not_forward);
                    }

                    ContentValues values = DrmManager.getInstance(getActivity().getApplicationContext()).getConstraints(path, mAction);
                    if (values == null || values.size() == 0) {
                        mLicenseStartTime = getString(R.string.drm_no_license);
                        mLicenseEndTime = getString(R.string.drm_no_license);
                        mRemains = getString(R.string.drm_no_license);
                    } else {
                        if (values.containsKey(DrmStore.ConstraintsColumns.LICENSE_START_TIME)) {
                            Long startL = values.getAsLong(DrmStore.ConstraintsColumns.LICENSE_START_TIME);
                            if (startL != null) {
                                if (startL == -1) {
                                    mLicenseStartTime = getString(R.string.drm_no_limitation);
                                } else {
                                    mLicenseStartTime = toDateTimeString(startL);
                                }
                            }
                        } else {
                            mLicenseStartTime = getString(R.string.drm_no_limitation);
                        }

                        if (values.containsKey(DrmStore.ConstraintsColumns.LICENSE_EXPIRY_TIME)) {
                            Long endL = values.getAsLong(DrmStore.ConstraintsColumns.LICENSE_EXPIRY_TIME);
                            if (endL != null) {
                                if (endL == -1) {
                                    mLicenseEndTime = getString(R.string.drm_no_limitation);
                                } else {
                                    mLicenseEndTime = toDateTimeString(endL);
                                }
                            } else {
                            }
                        } else {
                            mLicenseEndTime = getString(R.string.drm_no_limitation);
                        }

                        if (values.containsKey(DrmStore.ConstraintsColumns.REMAINING_REPEAT_COUNT)
                                && values.containsKey(DrmStore.ConstraintsColumns.MAX_REPEAT_COUNT)) {
                            Long remainCount = values.getAsLong(DrmStore.ConstraintsColumns.REMAINING_REPEAT_COUNT);
                            Long maxCount = values.getAsLong(DrmStore.ConstraintsColumns.MAX_REPEAT_COUNT);
                            if (remainCount != null && maxCount != null) {
                                if (remainCount == -1 || maxCount == -1) {
                                    mRemains = getString(R.string.drm_no_limitation);
                                } else {
                                    mRemains = remainCount.toString() + "/" + maxCount.toString();
                                }
                            }
                        } else {
                            mRemains = getString(R.string.drm_no_limitation);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //add for MTK Drm by yane.wang@jrdcom.com 20150420 end
            mStringBuilder.setLength(0);
        }

        private void appendPermission(boolean hasPermission, int title) {
            mStringBuilder.append(getString(title) + ": ");
            if (hasPermission) {
                mStringBuilder.append(getString(R.string.yes));
            } else {
                mStringBuilder.append(getString(R.string.no));
            }
        }

        private String getPermission(File file) {
            appendPermission(file.canRead(), R.string.readable_m);
            mStringBuilder.append("\n");
            appendPermission(file.canWrite(), R.string.writable_m);
            mStringBuilder.append("\n");
            appendPermission(file.canExecute(), R.string.executable_m);
            return mStringBuilder.toString();
        }

        private String getDetailPath(String path) {
            if (!TextUtils.isEmpty(path)) {
                path = path.substring(0, path.lastIndexOf("/"));
            }
            return path;
        }

        private void getDetailValue(FileInfo fileInfo) {
            mDetailName = fileInfo.getFileName();
            mDetailFilePath = getDetailPath(fileInfo.getFileAbsolutePath());
            mDetailPath = getDetailPath(fileInfo.getFileAbsolutePath());
            if (CategoryManager.getPhoneRootPath() != null) {
                mDetailPhonePath = mDetailPath.replace(CategoryManager.getPhoneRootPath(), getString(R.string.storage_phone));
                mDetailPath = mDetailPhonePath;
            }
            if (CategoryManager.getSDRootPath() != null) {
                mDetailSdPath = mDetailPath.replace(CategoryManager.getSDRootPath(), getString(R.string.storage_sdcard));
                mDetailPath = mDetailSdPath;
            }
            mDetailOtgPath = mDetailPath.replace(CategoryManager.OTG_ROOT_PATH, getString(R.string.storage_otg));
            mDetailPath = mDetailOtgPath;
            mDetailModifyTime = DateFormat.getDateInstance().format(new Date(time));
            if (fileInfo.getFile().canRead()) {
                mDetailRead = getString(R.string.yes);
            } else {
                mDetailRead = getString(R.string.no);
            }
            if (fileInfo.getFile().canWrite()) {
                mDetailWrite = getString(R.string.yes);
            } else {
                mDetailWrite = getString(R.string.no);
            }
            if (fileInfo.getFile().canExecute()) {
                mDetailExecute = getString(R.string.yes);
            } else {
                mDetailExecute = getString(R.string.no);
            }
        }

        @Override
        public void onTaskPrepare() {
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            mDetailSize = FileUtils.sizeToString(getActivity(), progressInfo.getTotal());
            List<String> titleAdapter = new ArrayList<String>();
            List<String> valueAdapter = new ArrayList<String>();
            titleAdapter.add(getString(R.string.detail_name_m));
            valueAdapter.add(mDetailName);
            titleAdapter.add(getString(R.string.detail_path_m_cn)); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1913721
            valueAdapter.add(mDetailPath);
            titleAdapter.add(getString(R.string.detail_size_m));
            valueAdapter.add(mDetailSize);
            titleAdapter.add(getString(R.string.detail_time_cn));
            valueAdapter.add(mDetailModifyTime);
            if (isDrm && DrmManager.mCurrentDrm == DrmManager.QCOM_DRM) {
                titleAdapter.add(getString(R.string.drm_current_count));
                valueAdapter.add(mDrmCurrentCount);
                titleAdapter.add(getString(R.string.drm_current_right));
                valueAdapter.add(mDrmCurrentConstraint);
                titleAdapter.add(getString(R.string.right_url));
                valueAdapter.add(mDrmLicesesIssuerURL);
                //add for MTK Drm by yane.wang@jrdcom.com 20150420 begin
            } else if (isDrm && DrmManager.mCurrentDrm == DrmManager.MTK_DRM) {
                titleAdapter.add(getString(R.string.drm_protection_status));
                valueAdapter.add(mProtectionStatus);
                titleAdapter.add(getString(R.string.drm_begin));
                valueAdapter.add(mLicenseStartTime);
                titleAdapter.add(getString(R.string.drm_end));
                valueAdapter.add(mLicenseEndTime);
                titleAdapter.add(getString(R.string.drm_use_left));
                valueAdapter.add(mRemains);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            final SimPickerDetailAdapter simAdapter = new SimPickerDetailAdapter(mActivity, titleAdapter, valueAdapter);
            builder.setSingleChoiceItems(simAdapter, -1, null)
                    .setNegativeButton(
                            mActivity.getResources().getString(R.string.ok), null);
            if (CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE ||
                    CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                builder.setTitle(R.string.menu_detail).setPositiveButton(mActivity.getResources().getString(R.string.open_dir), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CategoryManager.setCurrentMode(CategoryManager.PATH_MODE);
                        mApplication.mCurrentPath = mDetailFilePath;
                        ((FileBrowserActivity) getActivity()).openDirToListFragmentforPicture();
                        CategoryManager.mCurrentCagegory = -1;
                        switchToNormalView();
                        ((FileBrowserActivity) getActivity()).setFileActionMode(((FileBrowserActivity) getActivity()).FILE_MODE_NORMAL);
                        ((FileBrowserActivity) getActivity()).updateOptionMenu();
                    }
                });
            }
            builder.show();
        }

        @Override
        public void onTaskResult(int result) {
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (mApplication.mService != null) {
                mApplication.mService.cancel(mActivity.getClass().getName());
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mApplication.mService != null && mApplication.mService.isBusy(this.getClass().getName())) {
            return;
        }
        LogUtils.e(TAG,"OnItemClick filebrowserFragment....");
        boolean isOpenFile = false;
        final FileInfo selecteItemFileInfo = mAdapter.getItem(position);
        /** add encrypt file directe and file is direct */
        if (mAdapter.isMode(FileInfoAdapter.MODE_ADD_ENCRYPT_FILE) && !selecteItemFileInfo.getFile().isDirectory()) {
            setSelect(position);
            return;
        } else if (!mAdapter.isMode(FileInfoAdapter.MODE_EDIT)) {
            File tempFile = new File(SafeUtils.getDecryptRootPath(mActivity));
            if (null != tempFile) {
                File[] tempFiles = tempFile.listFiles();
                if (null != tempFiles && tempFiles.length > 0) {
                    for (File file : tempFiles) {
                        if (selecteItemFileInfo.getFileName().equals(file.getName())) {
                            openFile(new FileInfo(getActivity(), file));
                            isOpenFile = true;
                            return;
                        }
                    }
                }
            }
        }
        if (!isOpenFile && mAdapter.isMode(FileInfoAdapter.MODE_NORMAL) && CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE && !selecteItemFileInfo.isDirectory()) {
            long time = selecteItemFileInfo.getFileSize() / SafeUtils.FILE_TRANSFER_SPEED;
            List<FileInfo> fileInfos = new ArrayList<FileInfo>();
            fileInfos.add(selecteItemFileInfo);
            mSize = SafeUtils.calculateSize(fileInfos);
            mDecryptIEncDecListener = new DecryptIEncDecListener();
            mDecryptIEncDecListener.setFileCount(1);
            mDecryptIEncDecListener.setStatus(SafeUtils.OPEN_ENCRYPT_FILE);
            mApplication.getInitSecurityStatus(new sdkInitSuccessForDecrypt(selecteItemFileInfo), FileManagerApplication.FILE_DECRYPT);
            if (time < SafeUtils.ENCRYPT_OR_DECRYPT_TIME_LIMIT) {
                initDecryptAndShowDialog(SafeUtils.OPEN_ENCRYPT_FILE);
            } else {
                String message = String.format(getString(R.string.open_encrypt_file_need_decrypt), time);
                if (time > SafeUtils.ENCRYPT_OR_DECRYPT_TIME_MINUES) {
                    message = String.format(getString(R.string.open_encrypt_file_need_decrypt_time), time / 60, time % 60);
                }
                AlertDialog.Builder encryptDialog = new AlertDialog.Builder(getActivity());
                encryptDialog.setTitle(R.string.welcome_dialog_title);
                encryptDialog.setMessage(message);
                encryptDialog.setPositiveButton(R.string.drawer_open, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        initDecryptAndShowDialog(SafeUtils.OPEN_ENCRYPT_FILE);
                    }
                });
                encryptDialog.setNegativeButton(R.string.cancel, null);
                encryptDialog.show();
            }
            return;
        }
        if (!selecteItemFileInfo.getFile().exists()) {
            String error = getResources().getString(R.string.path_not_exists, selecteItemFileInfo.getFileName());
            deleteNotExistFiles(selecteItemFileInfo.getFileAbsolutePath());
            mToastHelper.showToast(error);
            return;
        }
        if (mAdapter.isMode(FileInfoAdapter.MODE_NORMAL) || mAdapter.isMode(FileInfoAdapter.MODE_SEARCH)
                || mAdapter.isMode(FileInfoAdapter.MODE_GLOBALSEARCH) || mAdapter.isMode(FileInfoAdapter.MODE_COPY)
                || mAdapter.isMode(FileInfoAdapter.MODE_ADD_ENCRYPT_FILE)) {

            if (mAdapter.isMode(FileInfoAdapter.MODE_GLOBALSEARCH) || mAdapter.isMode(FileInfoAdapter.MODE_SEARCH)) {
                /** record search info */
                CategoryManager.mCurrentMode = CategoryManager.PATH_MODE;
                mApplication.mCurrentPath = selecteItemFileInfo.getFileAbsolutePath();
                mApplication.mRecordSearchPath = mApplication.mCurrentPath;

                if (!selecteItemFileInfo.isDirectory()) {
                    openFile(selecteItemFileInfo);
                    return;
                    /** turn to file path */
                } else if (mAbsListViewFragmentListener != null) {
                    mAbsListViewFragmentListener.setFileActionMode(FileBrowserActivity.FILE_MODE_NORMAL);
                    updateActionMode(FileInfoAdapter.MODE_NORMAL);


                    if (getActivity() instanceof FileBrowserActivity) {
                        ((FileBrowserActivity) getActivity()).switchShortcutList(mApplication.mRecordSearchPath);
                        ((FileBrowserActivity) getActivity()).updateOptionMenu();
                    } else {
                        ((CategoryActivity) getActivity()).switchShortcutList(mApplication.mRecordSearchPath);
                        ((CategoryActivity) getActivity()).updateOptionMenu();
                    }

                    mAbsListViewFragmentListener.updateActionbar();
                }
            }
//            animateShow(getActionBarView());
            if (position >= mAdapter.getCount() || position < 0) {
                return;
            }

            if (selecteItemFileInfo.getFile().isDirectory()) {
                if (CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE
                        ) {
                    mApplication.mCurrentPath = selecteItemFileInfo.getFileAbsolutePath();
                    if (mAbsListViewFragmentListener != null) {
                        mAbsListViewFragmentListener.updateActionbar();
                    }
                }
                storeLastListPos();
                refreshPathBar();
                if (!mAdapter.isMode(FileInfoAdapter.MODE_ADD_ENCRYPT_FILE)) {
                    mAdapter.clearList();
                }
                if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                    CategoryManager.mCurrentMode = CategoryManager.PATH_MODE;
                    mApplication.mFileInfoManager.addItemList(selecteItemFileInfo.getSubFileInfo());

                    mApplication.mCurrentPath = selecteItemFileInfo.getFileAbsolutePath();
                    refreshPathBar();
                    if (mAbsListViewFragmentListener != null) {
                        mAbsListViewFragmentListener.updateActionbar();
                    }
                    if (mApplication != null && mApplication.mFileInfoManager != null) {
                        mApplication.mFileInfoManager.loadFileInfoList(mApplication.mCurrentPath);
                    }

                    refreshAdapter();
                    if (mAdapter != null && mAdapter.isMode(FileInfoAdapter.MODE_EDIT)) {
                        updateEditBarState();
                    }
                    restoreFirstPosition();
                } else {
                    if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
                        CategoryManager.mCurrentMode = CategoryManager.PATH_MODE;
                    }
                    showDirectoryContent(selecteItemFileInfo.getFileAbsolutePath());
                }
                if (mAbsListViewFragmentListener != null) {
                    mAbsListViewFragmentListener.updateActionbar();
                }
            } else {
                clickcount++;
                if (clickcount == 1) {
                    fir_time = System.currentTimeMillis();
                } else {
                    sec_time = System.currentTimeMillis();
                    if ((sec_time - fir_time) < 1000) {
                        fir_time = sec_time;
                        return;
                    }
                    fir_time = sec_time;
                }
                if (CategoryManager.isSafeCategory || mAdapter.isMode(FileInfoAdapter.MODE_COPY)) {
//                    decryptFile(selecteItemFileInfo);
                } else {
                    openFile(selecteItemFileInfo);
                }
            }
        } else {
            setSelect(position);
        }
    }

    public void openEncryptFileByFileInfo(FileInfo selecteItemFileInfo) {
        mDecryptFilePath = SafeUtils.getDecryptRootPath(mActivity) + File.separator + selecteItemFileInfo.getShowName();
        mDecryptIEncDecListener.setStatus(SafeUtils.OPEN_ENCRYPT_FILE);
        mApplication.mService.decryptFile(getActivity().getClass().getName(), getActivity(), new EncryptOperationListener(), mDecryptIEncDecListener, selecteItemFileInfo);
    }

    class UpdateEncryptCount implements  AbsUpdateEncryptFilesCount{

        @Override
        public void setUpdateEncryptCount(int position) {
            setSelect(position);
        }
    }

    private void setSelect(int position) {
        mAdapter.setSelect(position);
//        mAbsListViewFragmentListener.updateEncryptFileCount(SafeUtils.calculateFileCount(mAdapter.getCheckedFileInfoItemsList()));
        mSelectAll = true;
        updateEditBarByThread();
        FileInfo firstCheckedItem = mAdapter.getFirstCheckedFileInfoItem();
        mFirstPosition = mApplication.mFileInfoManager.getShowFileList().indexOf(firstCheckedItem);
    }

    public void returnFirstPosition() {
        new Thread() {
            public void run() {
                try {
                    sleep(300);
                    setViewPostion(mFirstPosition);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void cancelAllDialog() {
        ProgressDialogFragment pf = (ProgressDialogFragment) findFragmentByTag(HeavyOperationListener.HEAVY_DIALOG_TAG);
        if (pf != null) {
            pf.dismissAllowingStateLoss();
        }

        pf = (ProgressDialogFragment) findFragmentByTag(CategoryListListener.CATEGORY_LIST_DIALOG_TAG);
        if (pf != null) {
            pf.dismissAllowingStateLoss();
        }

        AlertDialogFragment af = (AlertDialogFragment) findFragmentByTag(DETAIL_DIALOG_TAG);

        if (af != null) {
            af.dismissAllowingStateLoss();
        }

        af = (AlertDialogFragment) findFragmentByTag(DELETE_DIALOG_TAG);
        if (af != null) {
            af.dismissAllowingStateLoss();
        }

        af = (AlertDialogFragment) findFragmentByTag(RENAME_EXTENSION_DIALOG_TAG);
        if (af != null) {
            af.dismissAllowingStateLoss();
        }

        AlertDialogFragment.EditTextDialogFragment renameDialogFragment = (AlertDialogFragment.EditTextDialogFragment) findFragmentByTag(RENAME_DIALOG_TAG);
        if (renameDialogFragment != null) {
            renameDialogFragment.dismissAllowingStateLoss();
        }

        AlertDialogFragment.EditTextDialogFragment createFolderDialogFragment = (AlertDialogFragment.EditTextDialogFragment) findFragmentByTag(CREATE_FOLDER_DIALOG_TAG);
        if (createFolderDialogFragment != null) {
            createFolderDialogFragment.dismissAllowingStateLoss();
        }
    }

    private void onUnmounted() {
        if (mAdapter != null
                && (mAdapter.getMode() == FileInfoAdapter.MODE_EDIT || mAdapter.getMode() == FileInfoAdapter.MODE_SEARCH)) {
            showNoSearchResultView(false, null);
            switchToNormalView();

        }
        if (mApplication.mService != null && mApplication.mService.isBusy(this.getClass().getName())) {
            mApplication.mService.cancel(this.getClass().getName());
        }
        cancelAllDialog();
    }

    private void updateEditBarState() {
        updateEditBarByThread();
    }

    private int getAdapterCount() {
        if (mAdapter != null) {
            return mAdapter.getCount();
        }
        return 0;
    }

    private void selectAllBtnClicked() {
        if (!mAdapter.isAllItemChecked()) {
            mApplication.currentOperation = FileManagerApplication.SELECT_ALL;
            mAdapter.setAllItemChecked(true);
        } else {
            mApplication.currentOperation = FileManagerApplication.OTHER;
            mAdapter.setAllItemChecked(false);
        }
//        mAbsListViewFragmentListener.updateEncryptFileCount(SafeUtils.calculateFileCount(mAdapter.getCheckedFileInfoItemsList()));
    }

    private void selectDoneBtnClicked() {
        mAdapter.setAllItemChecked(false);
        if (mFromSearchToEdit) {
            showSearchResultView();
        } else {
            switchToNormalView();
        }
    }

    private void detailsBtnClicked() {
        if (mAdapter.getItemEditFileInfoList().size() == 0) return;
        FileInfo info = mAdapter.getItemEditFileInfoList().get(0);
        LogUtils.e(TAG,"enter detailsBtnClicked function...");
        if (info.isDrmFile()) {
            LogUtils.e(TAG,"enter detailsBtnClicked function00...");
            String path = info.getFileAbsolutePath();
            mApplication.mService.getDetailInfo(mActivity.getClass().getName(), info,
                    new DetailInfoListener(mAdapter
                            .getItemEditFileInfoList().get(0), path));
        } else {
            LogUtils.e(TAG,"enter detailsBtnClicked function11...");
            mApplication.mService.getDetailInfo(mActivity.getClass().getName(), info,
                    new DetailInfoListener(mAdapter
                            .getItemEditFileInfoList().get(0)));
        }
    }

    private void copyBtnClicked() {
        mApplication.mFileInfoManager.savePasteList(FileInfoManager.PASTE_MODE_COPY,
                mAdapter.getItemEditFileInfoList());
        if (mAdapter.isMode(FileInfoAdapter.MODE_SEARCH) && mApplication.mFileInfoManager.getPasteCount() == 0) {
            showSearchResultView();
        } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
            if (!SafeUtils.getEncryptRootPath(mActivity).equals(mApplication.mCurrentPath)) {
                mApplication.mCurrentPath = SafeUtils.getEncryptRootPath(mActivity);
            }
            ((FileBrowserActivity) getActivity()).switchContentByViewMode();
            switchToCopyView();
            setCurrentCategory(CategoryManager.mCurrentCagegory);
            refreshPathBar();
        } else {
            ((FileBrowserActivity) getActivity()).updateCopyFragment();
            switchToCopyView();

            if (CategoryManager.mCurrentCagegory != -1) {
                setCurrentCategory(CategoryManager.mCurrentCagegory);
                CategoryManager.mCurrentCagegory = -1;
                setCopyFilePath(null);
            } else {
                List<FileInfo> list = mAdapter.getItemEditFileInfoList();
                FileInfo fileInfo = list.get(0);
                copyFilePath = fileInfo.getFileParentPath();
                setCopyFilePath(copyFilePath);
            }
        }
    }

    public static String getCopyFilePath() {
        return copyFilePath;
    }

    public static void setCopyFilePath(String copyFilePath) {
        FileBrowserFragment.copyFilePath = copyFilePath;
    }

    public static int getCurrentCategory(){
        return mCurrentCategory;
    }

    public static void setCurrentCategory(int curentCategory){
        mCurrentCategory = curentCategory;
    }


    private void shiftOutClicked(int mode, String mDesFolder) {
        LogUtils.d("SHIFT", "this is enter--shiftOutClicked(int mode)" + mAdapter.getItemEditFileInfoList().size());
        if (mApplication.mService != null) {
            mApplication.mService.ShiftOutFiles(mActivity.getClass().getName(),
                    mAdapter.getItemEditFileInfoList(), mDesFolder, mode, new HeavyOperationListener(R.string.move_out) {
                        @Override
                        public void onTaskResult(int errorType) {

                            LogUtils.i(TAG, "onTaskResult.errorType  is ->" + errorType);
                            super.onTaskResult(errorType);
                            mApplication.currentOperation = FileManagerApplication.OTHER;
                            switch (errorType) {
                                case FileManagerService.OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE:
                                    mToastHelper.showToast(R.string.insufficient_message_cn); // MODIFIED by haifeng.tang, 2016-05-11,BUG-2104433
                                    break;
                                case FileManagerService.OperationEventListener.ERROR_CODE_DELETE_FAILS:
                                    mToastHelper.showToast(R.string.delete_fail);
                                    break;
                                case FileManagerService.OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION:
                                    mToastHelper.showToast(R.string.copy_deny);
                                    break;
                                default:
//                                mApplication.mFileInfoManager.updateFileInfoList(mApplication.mCurrentPath,
//                                        mApplication.mSortType);
                    /*PR 714153 Zibin.wang add Begin */
                                    //refreshAdapter();
                    /*PR 714153 Zibin.wang add End */
                                    break;
                            }
                            SafeManager.mCurrentmode = SafeManager.FILE_NORMAL;
                            switchToNormalView();
                            LogUtils.i(TAG, "onTaskResult.errorType22333  is ->");
                            refreshAdapter(null);
                            clearFailedFiles(); // MODIFIED by songlin.qi, 2016-06-12,BUG-1989911
                            if (SafeUtils.isQuitSafe(mActivity)) {
                                mActivity.finish();
                            }
                        }
                    });
            //showNoFolderResultView(false);//PR-1174270 Nicky Ni -001 20151217
        } else {
            LogUtils.i(TAG, "mApplication.mService  is null");
        }

    }

    private void migrateBtnClicked() {
        CommonUtils.launchPhoneKeeperActivity(mActivity); // MODIFIED by songlin.qi, 2016-06-06,BUG-2223767
    }

    private void shortcutBtnClicked() {
//        if (CommonUtils.hasShortcut(mActivity, mAdapter.getItemEditSelect().get(0).getFileName())) {
//            mToastHelper.showToast(R.string.shortcut_exit);
//        } else {
//            mToastHelper.showToast(R.string.shortcut_success);
        try {
            Intent addShortcutIntent = new Intent(ACTION_ADD_SHORTCUT);
            addShortcutIntent.putExtra("duplicate", false);
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, mAdapter.getItemEditFileInfoList()
                    .get(0).getFileName());
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(mActivity,
                            R.drawable.ic_launcher_shortcut));

            Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
            launcherIntent.setClass(mActivity, mActivity.getClass());
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launcherIntent.putExtra("foldername", mAdapter.getItemEditFileInfoList().get(0)
                    .getFileAbsolutePath());
            launcherIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);//PR-1480589 Nicky Ni -001 20160130
            addShortcutIntent
                    .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
            mActivity.sendBroadcast(addShortcutIntent);
            mToastHelper.showToast(R.string.shortcut_success); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910955
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchToCopyView() {
        mSearchMode = false;//add for PR838074 by yane.wang@jrdcom.com 20141120
        if (mAdapter != null) {
//            if (mAdapter.isMode(FileInfoAdapter.MODE_SEARCH)) {
//                mFromSearchToEdit = true;
//            }
            int count = mAdapter.getCheckedItemsCount();
            updateActionMode(FileInfoAdapter.MODE_COPY);
        }
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.updateActionbar();
        }
        //updateEditBarByThread();
    }

    private boolean isSelectAll() {
        if (mAdapter.isAllItemChecked()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean checkIsSelectAll() {
        return isSelectAll();
    }

    private void cutBtnClicked() {
        mApplication.mFileInfoManager.savePasteList(FileInfoManager.PASTE_MODE_CUT,
                mAdapter.getItemEditFileInfoList());
        if (mAdapter.isMode(FileInfoAdapter.MODE_SEARCH) && mApplication.mFileInfoManager.getPasteCount() == 0) {
            showSearchResultView();
        } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
            if (!SafeUtils.getEncryptRootPath(mActivity).equals(mApplication.mCurrentPath)) {
                mApplication.mCurrentPath = SafeUtils.getEncryptRootPath(mActivity);
            }
            ((FileBrowserActivity) getActivity()).switchContentByViewMode();
            switchToCopyView();
            setCurrentCategory(CategoryManager.mCurrentCagegory);
            refreshPathBar();
        } else {
            //switchToNormalView();
            switchToCopyView();
            ((FileBrowserActivity) getActivity()).updateCopyFragment();

            if (CategoryManager.mCurrentCagegory != -1) {
                setCurrentCategory(CategoryManager.mCurrentCagegory);
                CategoryManager.mCurrentCagegory = -1;
                setCopyFilePath(null);
            } else {
                List<FileInfo> list = mAdapter.getItemEditFileInfoList();
                if(null != list && list.size() > 0) {
                    FileInfo fileInfo = list.get(0);
                    copyFilePath = fileInfo.getFileParentPath();
                    setCopyFilePath(copyFilePath);
                }
            }
        }
    }

    private void pasteBtnClicked() {
        LogUtils.e(TAG, "pasteBtnClicked");
        switchToNormalView();
        if (mApplication.mService != null) {
            mApplication.mService.pasteFiles(mActivity.getClass().getName(),
                    mApplication.mFileInfoManager.getPasteList(),
                    mApplication.mCurrentPath,
                    mApplication.mFileInfoManager.getPasteType(),
                    new HeavyOperationListener(R.string.pasting, mApplication.mFileInfoManager.getPasteList().size())); // MODIFIED by songlin.qi, 2016-06-01,BUG-1989911
            showNoFolderResultView(false);//PR-1174270 Nicky Ni -001 20151217
        } else {
            LogUtils.e(TAG, "mApplication.mService  is null");
        }
    }

    private void updatePasteBtn() {
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.pasteBtnUpdated();
        }
    }

    private void showNoShareDialog() {
        AlertDialogFragment.AlertDialogFragmentBuilder builder = new AlertDialogFragment.AlertDialogFragmentBuilder();
        AlertDialogFragment deleteDialogFragment = builder.setMessage(
                R.string.share_warning).setDoneTitle(R.string.ok)
                .setTitle(R.string.warning).create();
        deleteDialogFragment.show(getFragmentManager(), DELETE_DIALOG_TAG);
    }


    private void showDeleteDialog(int dMode) {
        deleteMode = dMode;
        LogUtils.d("DELE", "this is enter --222--" + deleteMode);
        FileInfo fileInfo = mAdapter.getFirstCheckedFileInfoItem();
        String name = null;
        String deleteMessage = null;
        boolean isNormal = false;
        if (deleteMode == SafeManager.DELETE_ALBUM_MODE) {
            deleteMessage = getResources().getString(R.string.delete_folder_message);
        } else {
            if (fileInfo == null && mAdapter.getItemEditSelect().size() > 0) {
                isNormal = true;
                fileInfo = mAdapter.getItemEditSelect().get(0);
            }
            if (fileInfo != null) {
                name = fileInfo.getFileName();
            }
            SpannableString spannableString = new SpannableString(name);
            spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD_ITALIC), 0, name == null ? 0 : name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int alertMsgId = R.string.alert_delete_multiple;
            if (mAdapter.getItem(0) != null) {
                if (mAdapter.getItem(0).getSubFileInfo() != null) {
                    deleteMessage = getResources().getString(R.string.delete_folder_message);
                } else {
                    List<FileInfo> selectInfo = mAdapter.getCheckedFileInfoItemsList();
                    int dirCount = 0;
                    for (int i = 0; i < selectInfo.size(); i++) {
                        if (selectInfo.get(i).isDirectory()) {
                            dirCount = dirCount + 1;
                        }
                    }
                    if (dirCount == 0) {
                        deleteMessage = getResources().getString(R.string.delete_determine_selected) + mAdapter.getCheckedItemsCount()
                                + getResources().getString(R.string.delete_document);
                    }
                    if (dirCount == selectInfo.size()) {
                        deleteMessage = getResources().getString(R.string.delete_determine_selected) + dirCount
                                + getResources().getString(R.string.delete_folder);
                    }
                    if (dirCount != 0 && dirCount < selectInfo.size()) {
                        deleteMessage = getResources().getString(R.string.delete_determine_selected) + dirCount + getResources().getString(R.string.delete_folder_and)
                                + (selectInfo.size() - dirCount) + getResources().getString(R.string.delete_document);
                    }
                }
            }
        }
        AlertDialog alertDialog = new AlertDialog.Builder(mActivity).setMessage(
                deleteMessage).setTitle(R.string.welcome_dialog_title).setPositiveButton(R.string.ok, new DeleteListener()).setNegativeButton(
                R.string.cancel, mCancelListner).show();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.negative_text_color));
    }

    private class DeleteListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int id) {
            deleteEditFiles(mAdapter.getItemEditFileInfoList(), SafeManager.NORMAL_DELETE_MODE);
        }
    }

    private void deleteEditFiles(List<FileInfo> fileInfos, final int type){
            /* MODIFIED-BEGIN by songlin.qi, 2016-06-01,BUG-1989911*/
        if (mApplication.mService != null && mAdapter != null) {
            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                unRegisterContentObservers();
            }
            List<FileInfo> editFileInfoList = mAdapter.getItemEditFileInfoList();
            if (deleteMode == SafeManager.DELETE_ALBUM_MODE) {
                editFileInfoList.clear();
                editFileInfoList.addAll(mApplication.mFileInfoManager.getShowFileList());
                fileInfos = editFileInfoList;
            }
            mApplication.mService.deleteFiles(mActivity
                            .getClass().getName(), deleteMode, fileInfos,
                    new HeavyOperationListener(R.string.deleting, fileInfos.size()) {
                        @Override
                        public void onTaskResult(int errorType) {
                            super.onTaskResult(errorType);
                            if (mFromSearchToEdit) {
                                mAdapter.clearChecked();
                                updateEditBarState();
                            } else {
//                                    if (type != SafeManager.SAFE_DELETE_MODE) {
                                //switchToNormalView();
                                if (getActivity() instanceof FileBrowserActivity) {
                                    ((FileBrowserActivity) getActivity()).setFileActionMode(((FileBrowserActivity) getActivity()).FILE_MODE_NORMAL);
                                    getActivity().invalidateOptionsMenu();
                                } else {
                                    ((CategoryActivity) getActivity()).setFileActionMode(((CategoryActivity) getActivity()).FILE_MODE_NORMAL);
                                    getActivity().invalidateOptionsMenu();
                                }
//                                    }
                            }
                            if (!deleteFlag && type != SafeManager.SAFE_DELETE_MODE) {
                                if (mApplication.mFileInfoManager.getDeleteStatus() == FileInfoManager.DELETE_MODE_CANCEL) {
                                    mApplication.mFileInfoManager.setDeleteStatus(FileInfoManager.DELETE_MODE_GOING);
                                } else {
                                    mToastHelper.showToast(R.string.deleted_cn);
                                }
                                int srcFileCount = mAdapter.getItemEditFileInfoList().size();
                                int size = mApplication.mFileInfoManager.getFailFiles().size();
                                if (size > 0) {
                                    if (size == 1) {
                                        int messageStringID = R.string.delete_single_fail_tip;
                                        new AlertDialog.Builder(mActivity).setMessage(mApplication.mFileInfoManager.getFailFiles().get(0).getName() +
                                                " " + getString(messageStringID)).setPositiveButton(R.string.ok, null).show();
                                    } else if (size > 1 && size == srcFileCount) {
                                        // all failed
                                        int messageStringID = R.string.delete_all_fail_tip;
                                        new AlertDialog.Builder(mActivity).setMessage(messageStringID
                                        ).setPositiveButton(R.string.ok, null).show();
                                            /* MODIFIED-END by songlin.qi,BUG-1989911*/
                                    } else {
                                        int messageStringID = R.string.delete_multi_fail_tip;
                                        new AlertDialog.Builder(mActivity).setMessage(messageStringID
                                        ).setPositiveButton(R.string.ok, null).show();
                                    }
                                }
                            }

                            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                                List<FileInfo> showFileList = mApplication.mFileInfoManager.getShowFileList();
                                if (showFileList != null && showFileList.size() > 0 && showFileList.get(0).getSubFileInfo() != null || mAdapter.getCount() < 1) {
                                    mApplication.mFileInfoManager.addAllItem(mApplication.mFileInfoList);
                                    mApplication.mFileInfoManager.loadAllFileInfoList();
                                    showPicDir();
                                    if (mAbsListViewFragmentListener != null) {
                                        mAbsListViewFragmentListener.updateActionbar();
                                    }
                                }
                            }

                            clearFailedFiles();
                            mAdapter.refresh();
                            mAdapter.notifyDataSetChanged();
                            deleteFlag = false;
                            if (SafeUtils.isQuitSafe(mActivity)) {
                                mActivity.finish();
                            }
                            showNoFolderResultView(mAdapter.isEmpty());
                            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                                registerContentObservers(CategoryManager.CATEGORY_PICTURES);
                            }
                        }
                    });
            deleteMode = 0;
        }
    }

    private class EncryptOperationListener implements FileManagerService.OperationEventListener{

        @Override
        public void onTaskPrepare() {

        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {

        }

        @Override
        public void onTaskResult(int result) {

        }
    }

    private class HeavyOperationListener implements
            FileManagerService.OperationEventListener, View.OnClickListener { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
        int mTitle = R.string.deleting;
        private boolean mPermissionToast = false;
        private boolean mOperationToast = false;
        public static final String HEAVY_DIALOG_TAG = "HeavyDialogFragment";
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-01,BUG-1989911*/
        private int mSrcFilesCount = 0;

        public HeavyOperationListener(int titleID) {
            this(titleID, 0);
        }

        public HeavyOperationListener(int titleID, int count) {
            mTitle = titleID;
            mSrcFilesCount = count;
        }

        /* MODIFIED-END by songlin.qi,BUG-1989911*/
        @Override
        public void onTaskPrepare() {
            ProgressDialogFragment heavyDialogFragment = ProgressDialogFragment
                    .newInstance(ProgressDialog.STYLE_HORIZONTAL, mTitle,
                            R.string.wait, R.string.cancel);
            heavyDialogFragment.show(getFragmentManager(), HEAVY_DIALOG_TAG);
            /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2003636*/
            if (FileSafeBrowserActivity.isFirstCreateAdd) {
                getFragmentManager().beginTransaction().commit();
            } else {
                getFragmentManager().executePendingTransactions();
            }
            /* MODIFIED-END by wenjing.ni,BUG-2003636*/
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            if (progressInfo.isFailInfo()) {
                /* MODIFIED-BEGIN by songlin.qi, 2016-06-01,BUG-1989911*/
                int stringId = -1;
                int failedSize = 0;
                if (mApplication != null && mApplication.mFileInfoManager != null) {
                    failedSize = mApplication.mFileInfoManager.getFailFiles().size();
                }
                /* MODIFIED-END by songlin.qi,BUG-1989911*/
                switch (progressInfo.getErrorCode()) {
                    case FileManagerService.OperationEventListener.ERROR_CODE_DELETE_CANCEL:
                        if (!mPermissionToast) {
                            mToastHelper.showToast(R.string.cancel_delete);
                            mApplication.mFileInfoManager.setDeleteStatus(FileInfoManager.DELETE_MODE_GOING);
                            mPermissionToast = true;
                            deleteFlag = true;
                        }
                        break;
                    case FileManagerService.OperationEventListener.ERROR_CODE_COPY_CANCEL:
                        if (!mPermissionToast) {
                            mToastHelper.showToast(R.string.cancel_copy);
                            mApplication.mFileInfoManager.setPasteStatus(FileInfoManager.PASTE_MODE_GOING);
                            mPermissionToast = true;
                            cutSameState = true;
                        }
                        break;
                    case FileManagerService.OperationEventListener.ERROR_CODE_CUT_CANCEL:
                        if (!mPermissionToast) {
                            mToastHelper.showToast(R.string.cancel_cut);
                            mApplication.mFileInfoManager.setPasteStatus(FileInfoManager.PASTE_MODE_GOING);
                            mPermissionToast = true;
                            cutSameState = true;
                        }
                        break;
                    case FileManagerService.OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION:
                        if (!mPermissionToast) {
                            mToastHelper.showToast(R.string.copy_deny);
                            mPermissionToast = true;
                            cutSameState = true;
                        }
                        break;
                    case FileManagerService.OperationEventListener.ERROR_CODE_DELETE_NO_PERMISSION:
                        if (!mPermissionToast) {
                            mToastHelper.showToast(R.string.delete_deny);
                            mPermissionToast = true;
                            deleteFlag = true;
                        }
                        break;
                    case FileManagerService.OperationEventListener.ERROR_CODE_DELETE_UNSUCCESS:
                        if (!mOperationToast) {
                            /* MODIFIED-BEGIN by songlin.qi, 2016-06-01,BUG-1989911*/
                            if (failedSize == 1) {
                                stringId = R.string.delete_single_fail_tip;
                            } else if (failedSize > 1 && failedSize == mSrcFilesCount) {
                                stringId = R.string.delete_all_fail_tip;
                            } else if (failedSize > 0) {
                                stringId = R.string.delete_multi_fail_tip;
                            }
                            if (stringId > 0) {
                                if (failedSize == 1) {
                                    String failedName = mApplication.mFileInfoManager.getFailFiles().get(0).getName();
                                    String toastText = failedName + " " + getString(stringId);
                                    mToastHelper.showToast(toastText);
                                } else {
                                    mToastHelper.showToast(stringId);
                                }
                                mToastHelper.showToast(stringId);
                                mOperationToast = true;
                                deleteFlag = true;
                            }
                        }
                        break;
                    case FileManagerService.OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS:
                        if (!mOperationToast) {
                            if (failedSize == 1) {
                                stringId = R.string.past_single_fail_tip;
                            } else if (failedSize > 1 && failedSize == mSrcFilesCount) {
                                stringId = R.string.past_all_fail_tip;
                            } else if (failedSize > 0) {
                                stringId = R.string.past_multi_fail_tip;
                            }
                            if (stringId > 0) {
                                if (failedSize == 1) {
                                    String failedName = mApplication.mFileInfoManager.getFailFiles().get(0).getName();
                                    String toastText = failedName + " " + getString(stringId);
                                    mToastHelper.showToast(toastText);
                                } else {
                                    mToastHelper.showToast(stringId);
                                }
                                mOperationToast = true;
                                cutSameState = true;
                            }
                            /* MODIFIED-END by songlin.qi,BUG-1989911*/
                        }
                        break;
                    default:
                        if (!mPermissionToast) {
                            mToastHelper.showToast(R.string.operation_fail);
                            mPermissionToast = true;
                            cutSameState = true;
                            deleteFlag = true;
                        }
                        break;
                }
                clearFailedFiles(); // MODIFIED by songlin.qi, 2016-06-12,BUG-1989911

            } else {
                ProgressDialogFragment heavyDialogFragment = (ProgressDialogFragment) findFragmentByTag(HEAVY_DIALOG_TAG);
                if (heavyDialogFragment != null) {
                    heavyDialogFragment.setProgress(progressInfo);
                }
            }
        }

        @Override
        public void onTaskResult(int errorType) {
            LogUtils.e("SHIFT", "this is enter refresh ---111---");
            mApplication.currentOperation = FileManagerApplication.OTHER;
            switch (errorType) {
                case ERROR_CODE_PASTE_TO_SUB:
                    cutSameState = true;
                    mToastHelper.showToast(R.string.paste_sub_folder);
                    break;
                case ERROR_CODE_CUT_SAME_PATH:
                    cutSameState = true;
                    mToastHelper.showToast(R.string.paste_same_folder);
                    break;
                case ERROR_CODE_NOT_ENOUGH_SPACE:
                    cutSameState = true;
                    mToastHelper.showToast(R.string.insufficient_message_cn); // MODIFIED by haifeng.tang, 2016-04-21,BUG-1940832
                    break;
                case ERROR_CODE_DELETE_FAILS:
                    deleteFlag = true;
                    mToastHelper.showToast(R.string.delete_fail);
                    break;
                case ERROR_CODE_COPY_NO_PERMISSION:
                    cutSameState = true;
                    mToastHelper.showToast(R.string.copy_deny);
                    break;
                default:
                    mApplication.mFileInfoManager.updateFileInfoList(mApplication.mCurrentPath,
                            mApplication.mSortType);
                    /*PR 714153 Zibin.wang add Begin */
                    //refreshAdapter();
                    if (mAdapter != null) {
                        mAdapter.refresh();
                        mAdapter.notifyDataSetChanged();
                    }
                    /*PR 714153 Zibin.wang add End */
                    break;
            }

            ProgressDialogFragment heavyDialogFragment = (ProgressDialogFragment) findFragmentByTag(HEAVY_DIALOG_TAG);
            if (heavyDialogFragment != null) {
                heavyDialogFragment.dismissAllowingStateLoss();
            }
            if (mApplication.mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_CUT) {
                refreshAdapter();
                if (!cutSameState && errorType != ERROR_CODE_USER_CANCEL) {
                    if (mApplication.mFileInfoManager.getPasteCount() != 0) {
                        if (mApplication.mFileInfoManager.getPasteStatus() == FileInfoManager.PASTE_MODE_CANCEL) {
                            mApplication.mFileInfoManager.setPasteStatus(FileInfoManager.PASTE_MODE_GOING);
                        } else {
                            mToastHelper.showToast(R.string.pasted_cn);//MODIFIED by haifeng.tang, 2016-04-09,BUG-1910723
                        }
                    }
                } else if (!cutSameState && errorType == ERROR_CODE_USER_CANCEL) {//PR 1535552 zibin.wang add 2016.01.30
                    mToastHelper.showToast(R.string.operation_fail);
                }
                mApplication.mFileInfoManager.clearPasteList();
                cutSameState = false;
                updatePasteBtn();
            } else if (mApplication.mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_COPY) {
                refreshAdapter();
                if (!cutSameState && errorType != ERROR_CODE_USER_CANCEL) {
                    if (mApplication.mFileInfoManager.getPasteCount() != 0) {
                        if (mApplication.mFileInfoManager.getPasteStatus() == FileInfoManager.PASTE_MODE_CANCEL) {
                            mApplication.mFileInfoManager.setPasteStatus(FileInfoManager.PASTE_MODE_GOING);
                        } else {
                            mToastHelper.showToast(R.string.pasted_cn); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910723
                        }
                    }
                } else if (!cutSameState && errorType == ERROR_CODE_USER_CANCEL) {//PR 1535552 zibin.wang add 2016.01.30
                    mToastHelper.showToast(R.string.operation_fail);
                }
                mApplication.mFileInfoManager.clearPasteList();
                cutSameState = false;
                updatePasteBtn();
            }

            int size = mApplication.mFileInfoManager.getFailFiles().size();
            if (size > 0) {

                LogUtils.e(TAG, "mApplication.currentOperation" + mApplication.currentOperation);
                if (size == 1) {

                    int messageStringID = -1;
                    if (mApplication.mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_CUT) {
                        messageStringID = R.string.cut_single_fail_tip;

                    } else if (mApplication.mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_COPY) {
                        messageStringID = R.string.copy_single_fail_tip;
                    }
                    if (messageStringID != -1) {

                        new AlertDialog.Builder(mActivity).setMessage(mApplication.mFileInfoManager.getFailFiles().get(0).getName() +
                                " " + getString(messageStringID)).setPositiveButton(R.string.ok, null).show();
                    }
                /* MODIFIED-BEGIN by songlin.qi, 2016-06-01,BUG-1989911*/
                } else if (size > 1 && size == mSrcFilesCount) {
                    // part of files failed
                    /* MODIFIED-END by songlin.qi,BUG-1989911*/
                    int messageStringID = -1;
                    if (mApplication.mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_CUT) {
                        messageStringID = R.string.cut_multi_fail_tip;

                    } else if (mApplication.mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_COPY) {
                        messageStringID = R.string.copy_multi_fail_tip;
                    }
                    if (messageStringID != -1) {
                        new AlertDialog.Builder(mActivity).setMessage(messageStringID
                        ).setPositiveButton(R.string.ok, null).show();
                    }
                /* MODIFIED-BEGIN by songlin.qi, 2016-06-01,BUG-1989911*/
                } else {
                    // all the files of failed
                    int messageStringID = -1;
                    if (mApplication.mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_CUT) {
                        messageStringID = R.string.cut_all_fail_tip;

                    } else if (mApplication.mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_COPY) {
                        messageStringID = R.string.copy_all_fail_tip;
                    }
                    if (messageStringID != -1) {
                        new AlertDialog.Builder(mActivity).setMessage(messageStringID
                        ).setPositiveButton(R.string.ok, null).show();
                    }
                    /* MODIFIED-END by songlin.qi,BUG-1989911*/
                }
            }
            clearFailedFiles(); // MODIFIED by songlin.qi, 2016-06-12,BUG-1989911

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    releaseWakeLock();
                }

            }, 500);
        }

        @Override
        public void onClick(View v) {
            if (mApplication.mService != null) {
                mApplication.mService.cancel(mActivity.getClass().getName());
            }
        }
    }

    private void releaseWakeLock() {
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.toReleaseWakeLock();
        }
    }

//    private class FavoriteOperationListener implements
//            FileManagerService.OperationEventListener, View.OnClickListener {
//        int mTitle = R.string.add_favorite;
//        int mFavoriteType = FavoriteManager.ADD_FAVORITE;
//
//        public static final String FAVORITE_DIALOG_TAG = "FavoriteDialogFragment";
//
//        public FavoriteOperationListener(int titleID, int favoriteType) {
//            mTitle = titleID;
//            mFavoriteType = favoriteType;
//        }
//
//        @Override
//        public void onTaskPrepare() {}
//
//        @Override
//        public void onTaskProgress(ProgressInfo progressInfo) {
//            if(progressInfo != null){
//                if (progressInfo.isFailInfo()) {
//                    switch (progressInfo.getErrorCode()) {
//                        case OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS:
//                            mToastHelper.showToast(R.string.operation_fail);
//                            break;
//                        default:
//                            break;
//                    }
//
//                } else {
//                    //add for PR934066 by yane.wang@jrdcom.com 20150304 begin
//                    //ProgressDialogFragment heavyDialogFragment = (ProgressDialogFragment)
//                            //getFragmentManager().findFragmentByTag(FAVORITE_DIALOG_TAG);
//                    ProgressDialogFragment heavyDialogFragment = (ProgressDialogFragment) getFragmentManager()
//                            .findFragmentByTag(FAVORITE_DIALOG_TAG);
//                    if (isResumed()) {
//                        if (heavyDialogFragment == null) {
//                            heavyDialogFragment = ProgressDialogFragment
//                                    .newInstance(ProgressDialog.STYLE_HORIZONTAL, mTitle,
//                                            R.string.wait, R.string.cancel);
//                            heavyDialogFragment.setCancelListener(this);
//                            heavyDialogFragment.show(getFragmentManager(),
//                                    FAVORITE_DIALOG_TAG);
//                            getFragmentManager().executePendingTransactions();
//                        }
//                        heavyDialogFragment.setProgress(progressInfo);
//                    }
//                  //add for PR934066 by yane.wang@jrdcom.com 20150304 end
//                    if (heavyDialogFragment != null) {
//                        heavyDialogFragment.setProgress(progressInfo);
//                    }
//                }
//            }
//        }
//
//        @Override
//        public void onTaskResult(int errorType) {
//            ProgressDialogFragment heavyDialogFragment = (ProgressDialogFragment)
//                    getFragmentManager().findFragmentByTag(FAVORITE_DIALOG_TAG);
//            if (heavyDialogFragment != null) {
//                heavyDialogFragment.dismissAllowingStateLoss();
//            }
//            if (mFavoriteType == FavoriteManager.DEL_FAVORITE) {
//                Runnable runnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        mFavoriteHandler.sendMessage(mFavoriteHandler.obtainMessage(0));
//                    }
//                };
//                new Thread(runnable).start();
//            }
//
//        }

//        @Override
//        public void onClick(View v) {
//            if (mApplication.mService != null) {
//                mApplication.mService.cancel(mActivity.getClass().getName());
//            }
//        }
//    }

    protected Handler mFavoriteHandler = new Handler() {
        public void handleMessage(Message msg) {
            showContent(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory);
        }
    };

//[FEATURE]-Add-BEGIN by TSBJ,shuang.liu1,09/24/2014,FR-791321,

    /**
     * Get the value that if the app is running in Dual Screen mode
     */
    private boolean isDualScreen() {
        boolean isDualScreen = false;
        try {
            IActivityManager activityManager = ActivityManagerNative.getDefault();
            Method method = Class.forName("android.app.IActivityManager").getMethod("isMultiStack");
            isDualScreen = (Boolean) method.invoke(activityManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isDualScreen;
    }

    /**
     * Create the default drag view when the item is long clicked. This method
     * may be override by subclass.
     */
    protected ImageView createDragView(int Position) {
        ImageView imageView = new ImageView(mActivity);
        imageView.setBackgroundColor(android.graphics.Color.RED);
        return imageView;
    }

    /**
     * Generate the intent for broadcast
     */
    public Intent genereteIntent() {
        Intent intent;
        boolean forbidden = false;
        String mimeType = mDragFile.getMimeType();

        if (TextUtils.isEmpty(mimeType) || mimeType.startsWith("unknown")) {
            mimeType = FileInfo.MIMETYPE_UNRECOGNIZED;
        }

        if (!forbidden) {
            intent = new Intent();
            intent.setAction(SEND_BROADCAST_MESSAGE);
            Uri uri = Uri.fromFile(mDragFile.getFile());
            intent.putExtra("type", mimeType);
            intent.putExtra("path", uri.toString());
            return intent;
        }

        return null;
    }

    /**
     * Update the DragView position
     */
    public void updateDragViewPosition(int x, int y) {
        mDragLayoutParams.x = x - mDragLayoutParams.width / 2;
        mDragLayoutParams.y = y - mDragLayoutParams.height / 2;
        getWindowManager().updateViewLayout(mDragView, mDragLayoutParams);
        return;
    }

    /**
     * Start drag the DragView
     */
    private void startDrag(int x, int y) {
        mDragLayoutParams = new WindowManager.LayoutParams();
        mDragLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mDragLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mDragLayoutParams.height = dpToPx(DRAGVIEW_HEIGHT);
        mDragLayoutParams.width = dpToPx(DRAGVIEW_WIDTH);
        mDragLayoutParams.x = x - mDragLayoutParams.width / 2;
        mDragLayoutParams.y = y - mDragLayoutParams.height / 2;
        mDragLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mDragLayoutParams.windowAnimations = 0;

        getWindowManager().addView(mDragView, mDragLayoutParams);

        mDraging = true;
        return;
    }

    /**
     * Stop drag the DragView
     */
    public void stopDrag() {
        getWindowManager().removeView(mDragView);
        mDragView = null;
        mDraging = false;
        return;
    }

    /**
     * Get the WindowManager
     */
    private WindowManager getWindowManager() {
        if (null == mWindowManager) {
            mWindowManager = (WindowManager) mActivity.getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * Get the flag that the drag is ready
     */
    public boolean isDragReady() {
        if (mDraging && (null != mDragView)) {
            return true;
        }
        return false;
    }

    private int dpToPx(float dp) {
        float scale = mActivity.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
    //[FEATURE]-Add-END by TSBJ,shuang.liu1,

    protected class SelectedItemInfo {
        int count;
        boolean hasDirectory;
        boolean hasDrm;
        boolean canShare;
    }

    private boolean isShareSizeExceed;
    private boolean forbidden;
    private boolean mFromSearchToEdit;
    private int firstIndex, lastIndex;
    private Handler mUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            SelectedItemInfo info = (SelectedItemInfo) msg.obj;
            if (mAbsListViewFragmentListener != null) {
                mAbsListViewFragmentListener.updateEditBar(info.count, info.hasDirectory, info.hasDrm, info.canShare);
            }
        }

    };
    private int clickcount;
    private long fir_time;
    private long sec_time;
    protected AbsListView mAbsListView;
    protected boolean isDataChanged;
    protected AbsListViewFragmentListener mAbsListViewFragmentListener;
    private int i = 0;

    protected AbsUpdateEncryptFilesCount mAbsUpdateEncryptFilesCount;

    private void onChanged() {
        LogUtils.i(TAG, "onChanged");
        isDataChanged = true;
        // ADD START FOR PR1059973 BY HONGBIN.CHEN 20150804
        if (CategoryManager.mCurrentMode == CategoryManager.PATH_MODE) {
            if (mApplication != null && mApplication.mService != null) {
                mApplication.mService.listFiles(mActivity.getClass().getName(),
                        mApplication.mCurrentPath, new ListListener(false), false, null, -1, ListFileTask.LIST_MODE_ONCHANGE); //MODIFIED by jian.xu, 2016-04-18,BUG-1868328 // MODIFIED by haifeng.tang, 2016-05-05,BUG-1987329
            }
            listCategoryFiles(false);
        } else {
            //PR-1401814 Nicky Ni -001 20160113 start
            if (mAdapter != null) {
                if (isChanageMode && i == 0) {
                    i++;
                    listCategoryFiles(false);
                } else if (isChanageMode && i == 1) {
                    isChanageMode = false;
                    i = 0;
                    listCategoryFiles(mAdapter.isEmpty());
                } else {
                    isChanageMode = false;
                    listCategoryFiles(false);
                } //[Bug-Fix] PR-1283107 TSNJ Junyong.Sun 2016/01/06

            } else {
                listCategoryFiles(false);
            }//PR-1401814 Nicky Ni -001 20160113 end
        }
        // ADD END FOR PR1059973 BY HONGBIN.CHEN 20150804
    }

    //private void notifyListDone(boolean isDone) {
    //mAbsListViewFragmentListener.notifyAbsListViewDone(isDone);
    //}
    public interface AbsListViewFragmentListener {
        public void updateEditBar(int count, boolean isHasDir, boolean isHasDrm, boolean canShare);

        public void reSearch();

        public void showBottomView(String string);

        public void setFileActionMode(int mode);

        public int getFileActionMode();

        public void updateActionbar();

        public void updateEncryptFileCount(int count);

        public void updateNormalBarView();

        public void setPrefsSortby(int sort);

        public void changeSearchMode(boolean flag);

        public void toShowForbiddenDialog();

        public void pasteBtnUpdated();

        public void toReleaseWakeLock();


        public void HideActionbar(boolean flag);

        public void isDeleteFlag(boolean flag);

        //public void notifyAbsListViewDone(boolean isDone);
        public int getSlideLimite();

        public void refreashSafeFilesCategory(); // MODIFIED by wenjing.ni, 2016-05-13,BUG-2127786

        public void switchDecryptDircetion(List<FileInfo> fileInfos);

        public void hideEditWindowWhenEncryptFile();
    }

    public interface AbsUpdateEncryptFilesCount {
        public void setUpdateEncryptCount(int position);
    }

    protected void switchToEditView(int position, int top) {
        mAdapter.setChecked(position, true);
    }

    protected void updateEditBarByThread() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (mAdapter == null) {
                    return;
                }
                infos = mAdapter.getCheckedFileInfoItemsList();
                if (!mSelectAll && infos.size() > 1000) {
                    try {
                        listDialogFragment = (ProgressDialogFragment) findFragmentByTag("");
                        if (isResumed()) {
                            FragmentManager fm = getFragmentManager();
                            if (listDialogFragment == null && fm != null) {
                                listDialogFragment = ProgressDialogFragment
                                        .newInstance(ProgressDialog.STYLE_HORIZONTAL, -1,
                                                R.string.select_all,
                                                R.string.cancel);
                                listDialogFragment.show(fm, "");
                                listDialogFragment.setCancelListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mAdapter.clearChecked();
                                        if (listDialogFragment != null) {
                                            listDialogFragment.dismissAllowingStateLoss();
                                        }
                                        infos = mAdapter.getCheckedFileInfoItemsList();
                                    }
                                });
                                fm.executePendingTransactions();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                updateSelectedFilter(infos);
                if (listDialogFragment != null) {
                    listDialogFragment.dismissAllowingStateLoss();
                }
                mSelectAll = false;
                SelectedItemInfo info = new SelectedItemInfo();
                info.count = infos.size();
                info.hasDirectory = mIsHasDirctory;
                info.hasDrm = mIsHasDrm;
                info.canShare = mCanShare;
                mUpdateHandler.sendMessage(mUpdateHandler.obtainMessage(0, info));
            }
        }).start();
    }

    ;

    @Override
    public void clickEditBtn() {
        switchToEditView();
    }

    @Override
    public void clickNewFolderBtn() {
        showCreateFolderDialog();
    }

    @Override
    public void clickSearchBtn() {
        switchToSearchView();
    }

    @Override
    public void clickGlobalSearchBtn() {
        switchToGlobalSearchView();
    }

    @Override
    public void clickPasteBtn() {
        pasteBtnClicked();
    }

    @Override
    public void clickDelteBtn(int mode) {
        LogUtils.d("DELE", "this is enter --111--" + mode);
        showDeleteDialog(mode);
    }

    @Override
    public void deleteFileInfo(FileInfo fileInfo) {
        mAdapter.addDeleteFileInfo(fileInfo);
        deleteEditFiles(mAdapter.getItemEditFileInfoList(), SafeManager.NORMAL_DELETE_MODE);
    }

    @Override
    public void clickCopyBtn() {
        copyBtnClicked();
    }

    @Override
    public void clickShiftOutBth(int mode, String mDesFolder) {
        LogUtils.d("SHIFT", "this is enter--clickShiftOutBth(int mode)" + mode);
        shiftOutClicked(mode, mDesFolder);

    }

    @Override
    public void clickMigrateBtn() {
        migrateBtnClicked();
    }

    @Override
    public void clickShortcutBtn() {
        shortcutBtnClicked();
    }

    @Override
    public void clickCutBtn() {
        cutBtnClicked();
    }

    @Override
    public void clickShareBtn() {
        share();
    }

    @Override
    public void clickRenameBtn(String mSearchtext) {
        mSearchMessage = mSearchtext;
        showRenameDialog();
    }

    @Override
    public void clickDetailsBtn() {
        detailsBtnClicked();
    }

    @Override
    public void clickSelectAllBtn() {
        selectAllBtnClicked();
        updateEditBarByThread();
    }

    @Override
    public void clickEncryptBtn(){
        final List<FileInfo> fileInfos = mAdapter.getItemEditFileInfoList();
        LogUtils.e(TAG,"Enter clickEncryptBtn functoin.......");
        long count = SafeUtils.calculateFileCount(fileInfos);
        if (count == 0) {
            mHandler.sendEmptyMessageDelayed(NON_ENCRYPT_FILE, 150);
            return;
        }
//        if (!SafeUtils.isNetworkAvailable(getActivity())) {
//            mHandler.sendEmptyMessageDelayed(NETWORK_ERROR, 150);
//            return;
//        }
        mApplication.getInitSecurityStatus(new sdkInitSuccessForEncrypt(), FileManagerApplication.FILE_ENCRYPT);
        calEncryptOrDecryptTime();
        if (mTime < SafeUtils.ENCRYPT_OR_DECRYPT_TIME_LIMIT) {
            LogUtils.e(TAG,"clickEncryptBtn and time is litter than ENCRYPT_OR_DECRYPT_TIME_LIMIT");
            initEncryptAndShowDialog();
        } else {
            LogUtils.e(TAG,"clickEncryptBtn and show encryptFileDialog.....");
            showEncryptFileDialog();
        }
    }

    private void initEncryptAndShowDialog() {
        mApplication.initSecuritySdk();
        showEncryptOrDecryptProgressDialog(SafeUtils.BATCH_FILE_ENCRYPTION);
    }

    private int mCount = 0;
    private long mTime = 0;
    private long mSize = 0;
    private void calEncryptOrDecryptTime() {
        final List<FileInfo> fileInfos = mAdapter.getItemEditFileInfoList();
        mSize = SafeUtils.calculateSize(fileInfos);
        mTime = mSize / SafeUtils.FILE_TRANSFER_SPEED;
        LogUtils.e(TAG, "encrypt time :" + mTime + ",size:"+mSize);
    }

    private void showEncryptOrDecryptProgressDialog(int status) {
        if (status == SafeUtils.BATCH_FILE_DECRYPTION) {
            mCount = SafeUtils.calculateFileCount(mAdapter.getNeedDecryptFileList());
        } else if (status == SafeUtils.OPEN_ENCRYPT_FILE){
            mCount = 1;
        } else {
            mCount = SafeUtils.calculateFileCount(mAdapter.getItemEditFileInfoList());
        }
        Message msg = new Message();
        msg.what = SafeUtils.OPEN_ENCRYPT_MSG;
        msg.obj = status;
        msg.arg1 = mCount;
        mHandler.sendMessage(msg);
    }

    private void encryptFile() {
        mEncryptIEncDecListener = new EncryptIEncDecListener();
        if (mTime < SafeUtils.ENCRYPT_OR_DECRYPT_TIME_LIMIT) {
            mApplication.mService.encryptFile(getActivity().getClass().getName(), getActivity(), new EncryptOperationListener(), mEncryptIEncDecListener, mAdapter, mApplication.mEncryptTargePath);
        } else {
            mApplication.mService.encryptFile(getActivity().getClass().getName(), getActivity(), new EncryptOperationListener(), mEncryptIEncDecListener, mAdapter, mApplication.mEncryptTargePath);
            mAbsListViewFragmentListener.hideEditWindowWhenEncryptFile();
        }
        mApplication.mEncryptTargePath = null;
    }

    private void showEncryptFileDialog() {
        String message = String.format(getString(R.string.encrypt_file_info), mTime);
        if (mTime > 60) {
            message = String.format(getString(R.string.encrypt_file_info_time), mTime / 60, mTime % 60);
        }
        AlertDialog.Builder encryptDialog = new AlertDialog.Builder(getActivity());
        encryptDialog.setTitle(R.string.welcome_dialog_title);
        encryptDialog.setMessage(message);
        encryptDialog.setPositiveButton(R.string.encrypt_button_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initEncryptAndShowDialog();
            }
        });
        encryptDialog.setNegativeButton(R.string.cancel, null);
        encryptDialog.show();
    }

    class sdkInitSuccessForEncrypt implements SdkInitSuccess {

        @Override
        public void sdkInitSuccess() {
            LogUtils.e(TAG, "sdkInitSuccessForEncrypt - sdkInitSuccess()");
            encryptFile();
        }

        @Override
        public void closeProgressDialog() {
            closeEncryptDecryptProgressDialog();
        }

        @Override
        public void showProgressDialog() {
            showEncryptOrDecryptProgressDialog(SafeUtils.BATCH_FILE_ENCRYPTION);
        }
    }

    class sdkInitSuccessForDecrypt implements SdkInitSuccess {

        private FileInfo mFileInfo;

        public sdkInitSuccessForDecrypt(FileInfo fileInfo) {
            mFileInfo = fileInfo;
        }

        public sdkInitSuccessForDecrypt() {
        }

        @Override
        public void sdkInitSuccess() {
            LogUtils.i(TAG, "sdkInitSuccessForDecrypt - sdkInitSuccess()");
            if (null != mFileInfo) {
                openEncryptFileByFileInfo(mFileInfo);
            } else {
                mDecryptIEncDecListener = new DecryptIEncDecListener();
                mDecryptIEncDecListener.setFileCount(mCount);
                mApplication.mService.decryptFiles(getActivity().getClass().getName(), getActivity(), new EncryptOperationListener(), mDecryptIEncDecListener, mAdapter);
            }
            mFileInfo = null;
        }

        @Override
        public void closeProgressDialog() {
            closeEncryptDecryptProgressDialog();
        }

        @Override
        public void showProgressDialog() {
            showEncryptOrDecryptProgressDialog(SafeUtils.BATCH_FILE_DECRYPTION);
        }
    }

    private void closeEncryptDecryptProgressDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (null != mCancelDialog) {
            mCancelDialog.dismiss();
            mCancelDialog = null;
        }
    }

    public interface SdkInitSuccess{
        public void sdkInitSuccess();
        public void closeProgressDialog();
        public void showProgressDialog();
    }

    @Override
    public void clickDecryptBtn(){
        final List<FileInfo> fileInfos = mAdapter.getItemEditFileInfoList();
        mAdapter.addAllListToNeedDecryptFileList(fileInfos);
        long count = SafeUtils.calculateFileCount(fileInfos);
        if (count == 0) {
            mHandler.sendEmptyMessageDelayed(NON_DECRYPT_FILE, 150);
            return;
        }
//        if (!SafeUtils.isNetworkAvailable(getActivity())) {
//            mHandler.sendEmptyMessageDelayed(NETWORK_ERROR, 150);
//            return;
//        }
        mApplication.getInitSecurityStatus(new sdkInitSuccessForDecrypt(), FileManagerApplication.FILE_DECRYPT);
        calEncryptOrDecryptTime();
        showDecryptFileDialog();
    }

    private void showDecryptFileDialog() {
        final List<FileInfo> fileInfos = mAdapter.getItemEditFileInfoList();
        if (mTime < SafeUtils.ENCRYPT_OR_DECRYPT_TIME_LIMIT) {
            mAbsListViewFragmentListener.switchDecryptDircetion(fileInfos);
        } else {
            String message = String.format(getString(R.string.decrypt_file_info), mTime);
            if (mTime > SafeUtils.ENCRYPT_OR_DECRYPT_TIME_MINUES) {
                message = String.format(getString(R.string.decrypt_file_info_time), mTime / 60, mTime % 60);
            }
            AlertDialog.Builder encryptDialog = new AlertDialog.Builder(getActivity());
            encryptDialog.setTitle(R.string.welcome_dialog_title);
            encryptDialog.setMessage(message);
            encryptDialog.setPositiveButton(R.string.dialog_button_continue_title, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAbsListViewFragmentListener.switchDecryptDircetion(fileInfos);
                }
            });
            encryptDialog.setNegativeButton(R.string.cancel, null);
            encryptDialog.show();
        }
    }

    @Override
    public void DecryptFileInfo() {
        initDecryptAndShowDialog(SafeUtils.BATCH_FILE_DECRYPTION);
    }

    private void initDecryptAndShowDialog(int status) {
        mApplication.initSecuritySdk();
        showEncryptOrDecryptProgressDialog(status);
    }

    @Override
    public void clickSelectDoneBtn() {
        selectDoneBtnClicked();
    }

    @Override
    public void refreshAdapter(String path) {
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
        // if in safe category show category content by CategoryManager.mCurrentSafeCategory
        if (CategoryManager.isSafeCategory) {
            showContent(path, CategoryManager.mCurrentSafeCategory);
        } else { // MODIFIED by yinglin, 2016-08-23,BUG-2669588
            showContent(path, CategoryManager.mCurrentCagegory);
        }
        /* MODIFIED-END by songlin.qi,BUG-2227088*/
    }

    @Override
    public void onBackPressed() {
        onBackPress();
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.updateActionbar();
        }
    }

    @Override
    public void updateAdapter() {
        LogUtils.e(TAG,"FileBrowserFragment updateAdapter enter..");
        if (mAdapter != null) {
            // ADD START FOR PR433886 BY HONGBIN.CHEN 20150901
            mAdapter.refresh();
            mAdapter.notifyDataSetChanged();
            LogUtils.e(TAG,"FileBrowserFragment updateAdapter notifyDataSetChanged..");
            // ADD END FOR PR433886 BY HONGBIN.CHEN 20150901
        }
        if (null != mAbsListView) {
            mAbsListView.setSelection(mAbsListView.getFirstVisiblePosition());
        }
    }

    @Override
    public void clearAdapter() {
        if (mAdapter != null) {
            mAdapter.clearList();
            //mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showNoSearchResults(boolean isShow, String args) {
        showNoSearchResultView(isShow, args);
    }

    @Override
    public void unMountUpdate() {
        onUnmounted();
    }

    @Override
    public int getAdapterSize() {
        return getAdapterCount();
    }

    @Override
    public void showBeforeSearchList() {
        showBeforeSearchContent();
    }

    @Override
    public void onConfiguarationChanged() {
    }

    @Override
    public void onScannerStarted() {
        isDataChanged = false;
    }

    @Override
    public void onScannerFinished() {
        if (mApplication != null && mApplication.mService != null && isDataChanged) {
            CategoryManager.mLastCagegory = -2;
            if (CategoryManager.mCurrentCagegory != CategoryManager.CATEGORY_PICTURES
                    && CategoryManager.mCurrentCagegory != CategoryManager.CATEGORY_MUSIC
                    && CategoryManager.mCurrentCagegory != CategoryManager.CATEGORY_VEDIOS) {
                return;
            }
            listCategoryFiles(false);
            mApplication.currentOperation = FileManagerApplication.OTHER;
        }
    }

    class EncryptIEncDecListener implements IEncDecListener {
        private int index = 1;
        private long currentEncryptProgress = 0;

        @Override
        public void onOperStart() throws RemoteException {
            LogUtils.i(TAG, "onOperStart");
        }

        @Override
        public void onOperProgress(long l, long l1) {
            try {
                LogUtils.i(TAG, "onOperProgress:" + l + ",l1:" + l1 + "," + Thread.currentThread().getName());
                currentEncryptProgress += l;
                int progress = (int)(currentEncryptProgress / mSize);
                LogUtils.i(TAG, "progress:" + progress);
                if (progress > 100) {
                    progress = 100;
                }
                Message msg = Message.obtain(mHandler, SafeUtils.UPDATE_ENCRYPT_PROCESS, progress, index);
                mHandler.sendMessage(msg);
                mHandler.removeMessages(SafeUtils.ENCRYPT_SUCCESS_DELET_FILE_MSG);
            } catch (RuntimeException e) {
                LogUtils.i(TAG, "onOperProgress exception:"+l);
            }
        }

        @Override
        public void onOperComplete(int i) {
            try {
                LogUtils.i(TAG, "onOperComplete:" + i);
                if (i == SafeUtils.SDK_SECURITY_EXCEPTION) {
                    mHandler.sendEmptyMessage(SafeUtils.SDK_SECURITY_EXCEPTION);
                } else {
                    Message msg = new Message();
                    msg.what = SafeUtils.ENCRYPT_SUCCESS_DELET_FILE_MSG;
                    msg.arg1 = index;
                    msg.obj = currentFile;
                    mHandler.sendMessageDelayed(msg, 150);
                    ++index;
                }
            } catch (RuntimeException e) {
                LogUtils.i(TAG, "onOperComplete exception");
            }
        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    }


    class DecryptIEncDecListener implements IEncDecListener {

        int status;
        int count;
        int index = 1;
        long currentDecryptProgress = 0;

        private void setStatus(int status){
            this.status = status;
        }

        private void setFileCount(int count) {
            this.count = count;
        }

        @Override
        public void onOperStart() throws RemoteException {
            LogUtils.i(TAG, "onOperStart");
            Message msg = new Message();
            msg.what = SafeUtils.OPEN_ENCRYPT_MSG;
            msg.obj = status;
            msg.arg1 = count;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onOperProgress(long l, long l1) throws RemoteException {
            LogUtils.i(TAG, "onOperProgress:" + l + ",l1:" + l1 + "," + Thread.currentThread().getName());
            currentDecryptProgress += l;
            int progress = (int)(currentDecryptProgress / mSize);
            LogUtils.i(TAG, "progress:" + progress);
            if (progress > 100) {
                progress = 100;
            }
            Message msg = Message.obtain(mHandler, SafeUtils.UPDATE_DECRYPT_PROCESS, progress, index);
            mHandler.sendMessage(msg);
            mHandler.removeMessages(SafeUtils.DECRYPT_SUCCESS_MSG);
        }

        @Override
        public void onOperComplete(int i) throws RemoteException {
            LogUtils.i(TAG, "onOperComplete:" + i);
            if (i == SafeUtils.SDK_SECURITY_EXCEPTION) {
                mHandler.sendEmptyMessage(SafeUtils.SDK_SECURITY_EXCEPTION);
            } else {
                Message msg = new Message();
                msg.what = SafeUtils.DECRYPT_SUCCESS_MSG;
                msg.arg1 = index;
                mHandler.sendMessageDelayed(msg, 150);
                ++index;
            }
        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    }

    private void startActivityOpenFile(FileInfo fileInfo) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String mimeType = fileInfo.getMimeType();
        String path = fileInfo.getFileAbsolutePath();
        boolean isDrmFile = fileInfo.isDrmFile();
        File file = new File(path);
        if (isDrmFile) {
            String drmOriginalType = DrmManager.getInstance(getActivity().getApplicationContext()).getOriginalMimeType(path);
            if (drmOriginalType != null && !drmOriginalType.isEmpty()) {
                mimeType = drmOriginalType;
            }
        }
        mUri = fileInfo.getContentUri(mApplication.mService);

        // add for PR1031419 by xiaohui.gao@jrdcom.com 20150629 start 	358
        mimeType = mimeType.toLowerCase();
        LogUtils.e(TAG, "Open uri file: " + mUri + " mimeType=" + mimeType);

        if (FileInfo.MIMETYPE_EXTENSION_UNKONW.toLowerCase().equals(mimeType)) {
            mimeType = "*/*";
        }
        LogUtils.e(TAG, " mimeType=" + mimeType);
        final String type = mimeType;
        // add for PR1031419 by xiaohui.gao@jrdcom.com 20150629 end
        if (mUri.toString().startsWith(FileInfo.FILE_URI_HEAD)) {
            MediaScannerConnection.scanFile(mActivity, new String[]{path}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {

                }

                @Override
                public void onScanCompleted(String s, Uri uri) {
                    LogUtils.e(TAG, "scan completed uri=" + uri);
                    mUri = uri;
                    intent.setDataAndType(mUri, type);
                    startAcitivityOfOpenFile(intent);
                }
            });
        } else {
            if (null != mUri && mUri.toString().startsWith(FileInfo.FILE_URI_HEAD) && !mUri.toString().startsWith(FileInfo.FILE_OTG_HEAD) && !mUri.toString().startsWith(FileInfo.FILE_SD_HEAD)) {
                mUri = FileProvider.getUriForFile(mActivity, FileInfo.FILE_PROVIDER, fileInfo.getFile());
                LogUtils.e(TAG, "uri turn to fileprovider:" + mUri);
            }
            intent.setDataAndType(mUri, mimeType);
            LogUtils.e(TAG,"startActivityOpenFile enter.....");
            startAcitivityOfOpenFile(intent);
        }
    }

    private void startAcitivityOfOpenFile(Intent intent) {
        LogUtils.e(TAG,"FileBrowserFragment enter.....");
        try {
            SafeManager.notQuitSafe = true;
            LogUtils.e(TAG,"FileBrowserFragment enter22.....");
            startActivity(intent);
        } catch (NullPointerException e) {
            e.printStackTrace();
            SafeManager.notQuitSafe = false;
            Message message = mHandler
                    .obtainMessage(2, 1, 11, R.string.msg_unable_open_file_in_app);
            LogUtils.e(TAG,"FileBrowserFragment enter33.....");
            mHandler.sendMessage(message);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            SafeManager.notQuitSafe = false;
            Message message = mHandler
                    .obtainMessage(2, 1, 11, R.string.msg_unable_open_file_in_app);
            LogUtils.e(TAG,"FileBrowserFragment enter44.....");
            mHandler.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
            SafeManager.notQuitSafe = false;
            Message message = mHandler
                    .obtainMessage(2, 1, 11, R.string.msg_unable_open_file_in_app);
            LogUtils.e(TAG,"FileBrowserFragment enter55.....");
            mHandler.sendMessage(message);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            Log.e(TAG,"mHandler message msg :" + msg.what);

            switch (msg.what) {
                case 0:
                    showDrmWifidisplyDiaog(mActivity);
                    break;

                case 1:
                    startActivityOpenFile((FileInfo) msg.obj);
                    break;

                case 2:
                    mToastHelper.showToast((Integer) msg.obj);
                    break;

                case 3:
                    try {
                        final String drmRefreshPath = (String) msg.obj;
                        Dialog drmRefreshDialog = DrmManager.getInstance(getActivity().getApplicationContext()).showRefreshLicenseDialog(mActivity, drmRefreshPath);
                        if (drmRefreshDialog != null) {
                            drmRefreshDialog.setOnDismissListener(new OnDismissListener() { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    if (!TextUtils.isEmpty(drmRefreshPath)) {
                                        File file = new File(drmRefreshPath);
                                        if (!file.exists()) {
                                            reloadContent();
                                        }
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                /* MODIFIED-BEGIN by haifeng.tang, 2016-05-09,BUG-2104433*/
                /*MODIFIED-BEGIN by jian.xu, 2016-04-18,BUG-1868328*/
                case ListListener.LIST_DIALOG_MSG:
                    try {
                        ProgressDialogFragment listDialogFragment = (ProgressDialogFragment) findFragmentByTag(ListListener.LIST_DIALOG_TAG);
                        if (isResumed()) {
                            FragmentManager fm = getFragmentManager();
                            if (listDialogFragment == null && fm != null) {
                                listDialogFragment = ProgressDialogFragment
                                        .newInstance(ProgressDialog.STYLE_HORIZONTAL, -1,
                                                R.string.loading,
                                                AlertDialogFragment.INVIND_RES_ID);

                                listDialogFragment.show(fm, ListListener.LIST_DIALOG_TAG);
                                fm.executePendingTransactions();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case CategoryListListener.CATEGORY_LIST_DIALOG_MSG:
                    try {
                        ProgressDialogFragment listDialogFragment = (ProgressDialogFragment) findFragmentByTag(CategoryListListener.CATEGORY_LIST_DIALOG_TAG);
                        if (isResumed()) {
                            FragmentManager fm = getFragmentManager();
                            if (listDialogFragment == null && fm != null) {
                                listDialogFragment = ProgressDialogFragment
                                        .newInstance(ProgressDialog.STYLE_HORIZONTAL, -1,
                                                R.string.loading,
                                                AlertDialogFragment.INVIND_RES_ID);

                                listDialogFragment.show(fm, CategoryListListener.CATEGORY_LIST_DIALOG_TAG);
                                fm.executePendingTransactions();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SafeUtils.ENCRYPT_SUCCESS_DELET_FILE_MSG:
                    LogUtils.e(TAG, "encrypt file success and delete files");
                    mApplication.currentOperation = FileManagerApplication.DETETE;
                    mApplication.mService.cancel(getActivity().getClass().getName());
                    List<FileInfo> fileInfos = mAdapter.getItemEditFileInfoList();
                    if (fileInfos.size() > 0) {
                        if (FileSecurityTask.stopEncrypt) {
                            deleteEditFiles(mAdapter.getEncryptedFileList(), SafeManager.SAFE_DELETE_MODE);
                        } else {
                            deleteEditFiles(fileInfos, SafeManager.SAFE_DELETE_MODE);
                        }
                        mToastHelper.showToast(String.format(getString(R.string.encrypted_files), mCount));
                        mAdapter.clearEncryptedFileList();
                    }
                    if (null != mProgressDialog) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    if (null != mCancelDialog) {
                        LogUtils.i(TAG, "close alert Dialog");
                        mCancelDialog.dismiss();
                        mCancelDialog = null;
                    }
                    break;
                case SafeUtils.DECRYPT_SUCCESS_MSG:
                    LogUtils.i(TAG, "decrypt success and open file");
                    if (null != mProgressDialog) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    if (null != mCancelDialog) {
                        LogUtils.i(TAG, "close alert Dialog");
                        mCancelDialog.dismiss();
                        mCancelDialog = null;
                    }
                    List<FileInfo> decryptFileList = mAdapter.getNeedDecryptFileList();
                    if (null != decryptFileList && decryptFileList.size() > 0) {
                        deleteEditFiles(decryptFileList, SafeManager.SAFE_DELETE_MODE);
                    } else {
                        openFile(new FileInfo(getActivity(), mDecryptFilePath));
                    }
                    mToastHelper.showToast(String.format(getString(R.string.decrypted_files), mCount));
                    mAdapter.clearEncryptedFileList();
                    break;
                case SafeUtils.OPEN_ENCRYPT_MSG:
                    if (null != mProgressDialog && mProgressDialog.isShowing()) {
                        return;
                    }
                    int count = msg.arg1;
                    LogUtils.i(TAG, "open encrypt file....");

                    if (SafeUtils.OPEN_ENCRYPT_FILE == (int)msg.obj) {
                        mProgressDialog = ProgressDialogFragment
                                .newInstance(ProgressDialog.STYLE_HORIZONTAL, R.string.opening_file,
                                        R.string.select_all,
                                        R.string.cancel);
                    } else if (SafeUtils.BATCH_FILE_DECRYPTION == (int)msg.obj) {
//                        mProgressDialog.setTitle(String.format(getString(R.string.decrypting_file_count), count));
                        mProgressDialog = ProgressDialogFragment
                                .newInstance(ProgressDialog.STYLE_HORIZONTAL, R.string.decrypting,
                                        R.string.select_all,
                                        R.string.cancel);
                        if (count > 1) {
                            mProgressDialog.setCancelListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    showWaitSecryptAlertDialog(R.string.decrypting);
                                }
                            });
                        }
                    } else {
//                        mProgressDialog.setTitle(String.format(getString(R.string.encryptint_file_count), count));
                        mProgressDialog = ProgressDialogFragment
                                .newInstance(ProgressDialog.STYLE_HORIZONTAL, R.string.encrypting,
                                        R.string.select_all,
                                        R.string.cancel);
                        if (count > 1) {
                            mProgressDialog.setCancelListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    showWaitSecryptAlertDialog(R.string.encrypting);
                                }
                            });
                        }
                    }
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show(getFragmentManager(), "");
                    getFragmentManager().executePendingTransactions();
                    mProgressDialog.setProgress(0);
                    if (count > 1) {
                        mProgressDialog.setCancelIsVisiable(View.VISIBLE);
                    } else {
                        mProgressDialog.setCancelIsVisiable(View.GONE);
                    }
                    break;
                case SafeUtils.UPDATE_ENCRYPT_PROCESS:
                    if (null != mProgressDialog) {
                        mProgressDialog.setProgress(msg.arg1);
                        mProgressDialog.setTitle(R.string.encrypting);
                    }
                    break;
                case SafeUtils.UPDATE_DECRYPT_PROCESS:
                    if (null != mProgressDialog) {
                        mProgressDialog.setProgress(msg.arg1);
                        if (mCount != 0) {
                            mProgressDialog.setTitle(R.string.decrypting);
                        }
                    }
                    break;
                case SafeUtils.SDK_SECURITY_EXCEPTION:
                    if (null != mProgressDialog) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    if (null != mCancelDialog) {
                        LogUtils.i(TAG, "close alert Dialog");
                        mCancelDialog.dismiss();
                        mCancelDialog = null;
                    }
                    mToastHelper.showToast(R.string.encrypt_or_decrypt_file_fail);
                    break;
                case NETWORK_ERROR:
                    mToastHelper.showToast(R.string.network_error_info);
                    break;
                case NON_ENCRYPT_FILE:
                    mToastHelper.showToast(R.string.no_file_can_encrypt);
                    break;
                case NON_DECRYPT_FILE:
                    mToastHelper.showToast(R.string.no_file_can_decrypt);
                    break;
                default:
                    break;
            }
        }
    };

    private void showWaitSecryptAlertDialog (int id){
        AlertDialog.Builder cancelDialog = new AlertDialog.Builder(mActivity);
        if (id == R.string.encrypting) {
            cancelDialog.setTitle(R.string.encrypting);
            cancelDialog.setMessage(R.string.cancel_encrypt_task);
        } else {
            cancelDialog.setTitle(R.string.decrypting);
            cancelDialog.setMessage(R.string.cancel_decrypt_task);
        }
        cancelDialog.setCancelable(false);
        mCancelDialog = cancelDialog.show();
        FileSecurityTask.stopEncrypt = true;
        mProgressDialog.dismiss();
    }

    /**
     * Stop close dialog
     */
    private void preventDismissDialog() {
        try {
            Field field = mProgressDialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(mProgressDialog, false);
            mProgressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class OpenFileThread extends Thread {

        FileInfo mFileInfo;

        public OpenFileThread(FileInfo fileInfo) {
            mFileInfo = fileInfo;
        }

        @Override
        public void run() {
            super.run();
            boolean canOpen = true;
            mDisplayManager = (DisplayManager) mActivity.getSystemService(Context.DISPLAY_SERVICE);
            WifiDisplayStatus mWifiDisplayStatus = mDisplayManager.getWifiDisplayStatus();
            File file = new File(mFileInfo.getFileAbsolutePath());
            String mimeType = mFileInfo.getMimeType();
            ContentResolver resolver = mActivity.getContentResolver();
            int HDCP_ENABLE = 0;
            try {
                HDCP_ENABLE = Settings.Global.getInt(resolver,
                        TCT_HDCP_DRM_NOTIFY, 0);
            } catch (Exception e) {
                HDCP_ENABLE = 0;
            }
//            if (FileInfo.MIMETYPE_EXTENSION_UNKONW.equals(mimeType)) {
//                Message message = mHandler.obtainMessage(2, 1, 11, R.string.msg_unable_open_file_in_app);
//                mHandler.sendMessage(message);
//                return;
//            }
            boolean isDrmFile = mFileInfo.isDrmFile();
            if (isDrmFile) {
                String drmPath = mFileInfo.getFileAbsolutePath();
                if (!DrmManager.getInstance(getActivity().getApplicationContext()).isRightsStatus(drmPath)) {
                    canOpen = false;
                    if (DrmManager.mCurrentDrm == 10) {
                        Message message = mHandler.obtainMessage(3, 1, 11, drmPath);
                        mHandler.sendMessage(message);
                    } else {
                        Message message = mHandler.obtainMessage(2, 1, 11, R.string.drm_no_valid_right);
                        mHandler.sendMessage(message);
                    }

                    if (mWifiDisplayStatus.getActiveDisplayState() == 2 && (0 == HDCP_ENABLE)) {
                        mHandler.sendEmptyMessage(0);
                    }
                }
            }

            if (canOpen) {
                if ((mWifiDisplayStatus.getActiveDisplayState() == 2) && (0 == HDCP_ENABLE) && isDrmFile) {
                    mHandler.sendEmptyMessage(0);
                } else {
                    Message message = mHandler.obtainMessage(1, 1, 11, mFileInfo);
                    mHandler.sendMessage(message);
                }
            }
        }
    }

    protected void openFile(FileInfo mFileInfo) {
        new OpenFileThread(mFileInfo).start();
    }

    public void checkNoFileResultView() {
        if (mApplication.mFileInfoManager.getShowFileList().size() == 0) {
            if (mNo_messageView != null) {
                mNo_messageView.setText(mActivity.getResources().getString(R.string.no_folder_cn)); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1913721
            }
            if (mNoFolderView != null) {
                mNoFolderView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void showNoFolderResultView(boolean isShow) {
        if (isShow) {
            if (CategoryManager.mCurrentCagegory >= 0) {
                checkNoFileResultView();
            } else {
                if (mNo_messageView != null) {
                    mNo_messageView.setText(mActivity.getResources().getString(R.string.no_folder_cn)); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1913721
                }
                if (mNoFolderView != null) {
                    mNoFolderView.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (mNoFolderView != null) {
                mNoFolderView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void showNoFolderResults(boolean isShow) {
        showNoFolderResultView(isShow);
    }

    @Override
    public void closeItemMorePop() {
        if (mAdapter.mItemMorePop != null) {
            mAdapter.mItemMorePop.dismiss();
        }
    }

    @Override
    public boolean isItemMorePop() {
        if (mAdapter.mItemMorePop != null) {
            return mAdapter.mItemMorePop.isShowing();
        }
        return false;
    }

    @Override
    public void clearChecked() {
        if (null != mAdapter) {
            mAdapter.clearChecked();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void clearDecryptAndEncryptList() {
        if (null != mAdapter) {
            mAdapter.clearEncryptedFileList();
            mAdapter.clearNeedDecryptFileList();
        }
    }

    @Override
    public void clickShortcutToNormal() {
        showNoSearchResultView(false, null);//PR-1175501 Nicky Ni -001 20151217
        switchToNormalView();
    }

    @Override
    public void clickPrivateBtn(boolean isFromFilesCategory) {

//        deCryptFiles();

        setPrivate(isFromFilesCategory);
    }


    private void setPrivate(final boolean isFromFilesCategory) { // MODIFIED by wenjing.ni, 2016-05-13,BUG-2127786
        LogUtils.d("SafeBox", "setPrivate()");
        List<FileInfo> fileInfoList = new ArrayList<>();
        if (!isFromFilesCategory) {
            fileInfoList.addAll(mAdapter.getItemEditFileInfoList());
        } else {
            fileInfoList.addAll(SafeUtils.getSafeFiles());
        }
        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-14,BUG-1943981*/
        if (mApplication != null && mApplication.mService != null) { //MODIFIED by wenjing.ni, 2016-04-14,BUG-1924019
            mApplication.mService.addPrivateFiles(mActivity
                            .getClass().getName(), fileInfoList,
                    new HeavyOperationListener(R.string.chip_encryption) {
                        @Override
                        public void onTaskResult(int errorType) {
                            super.onTaskResult(errorType);
                            LogUtils.d("DES", "this is enter DES---111---" + errorType);
                            mApplication.currentOperation = FileManagerApplication.OTHER;
                            switch (errorType) {
                                case ERROR_CODE_NOT_ENOUGH_SPACE:
                                    mToastHelper.showToast(R.string.insufficient_message_cn); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910684
                                    break;
                                case ERROR_CODE_DELETE_FAILS:
                                    mToastHelper.showToast(R.string.delete_fail);
                                    break;
                                case ERROR_CODE_COPY_NO_PERMISSION:
                                    mToastHelper.showToast(R.string.copy_deny);
                                    break;
                                case ERROR_SAFE_SIZE_LIMTED:
                                    mToastHelper.showToast(R.string.add_safe_size_limited);
                                    break;
                                case ERROR_SAFE_DRM_LIMTED:
                                    mToastHelper.showToast(R.string.add_safe_drm_limited);
                                    break;
                                case ERROR_CODE_SUCCESS:
                                    MountManager mMountPointManager = MountManager.getInstance();
                                    AlertDialog.Builder successBuilder = new AlertDialog.Builder(mActivity);
                                    successBuilder.setMessage(mActivity.getResources().getString(R.string.move_in_success) + PrivateFileOperationTask.mAddSuccessCount
                                            + mActivity.getResources().getString(R.string.move_in_count) + "\n"
                                            + SafeUtils.getSafeTitle(mMountPointManager, mActivity))
                                            .setPositiveButton(R.string.ok, null);
                                    successBuilder.create().show();
                                    break;
                                /*MODIFIED-END by haifeng.tang,BUG-1910684*/
                                default:
                                    break;
                            }
                            switchToNormalView();
                            if (SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN) {
                                FileSafeBrowserActivity fileSafeBrowserActivity = (FileSafeBrowserActivity) getActivity();
                                SafeManager.mCurrentmode = SafeManager.FILE_NORMAL;
                                CategoryManager.isSafeCategory = true;
                                /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2127786*/
                                if (isFromFilesCategory && fileSafeBrowserActivity.isRefreshFilesCategory
                                        && CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_DOCS) {
                                    mAbsListViewFragmentListener.refreashSafeFilesCategory();
                                } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_DOCS) {
                                /* MODIFIED-END by wenjing.ni,BUG-2127786*/
                                    fileSafeBrowserActivity.setActionbarTitle(R.string.category_files);
                                    CategoryManager.mCurrentCagegory = CategoryManager.SAFE_CATEGORY_FILES;
                                } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_VEDIOS) {
                                    fileSafeBrowserActivity.setActionbarTitle(R.string.category_vedios);
                                    CategoryManager.mCurrentCagegory = CategoryManager.SAFE_CATEGORY_VEDIO;
                                } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                                    fileSafeBrowserActivity.setActionbarTitle(R.string.category_pictures);
                                    CategoryManager.mCurrentCagegory = CategoryManager.SAFE_CATEGORY_PICTURES;
                                } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_MUSIC) {
                                    fileSafeBrowserActivity.setActionbarTitle(R.string.category_music);
                                    CategoryManager.mCurrentCagegory = CategoryManager.SAFE_CATEGORY_MUISC;
                                }
                            /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2003636*/
                            } else if (FileSafeBrowserActivity.isFirstCreateAdd) { // MODIFIED by yinglin, 2016-08-23,BUG-2669588
                                mAbsListViewFragmentListener.refreashSafeFilesCategory();
                            }
                            SafeUtils.clearSafeFiles();
                            LogUtils.e(TAG,"OnItemClick setPrivate....");
                            refreshAdapter(null);
                            if (SafeUtils.isQuitSafe(mActivity)) {
                                mActivity.finish();
                            }
                        }
                    }, isFromFilesCategory);
                    /* MODIFIED-END by wenjing.ni,BUG-2003636*/
        }
        /*MODIFIED-END by haifeng.tang,BUG-1943981*/

    }

    @Override
    public void deleteSafeTempFile() {
        final List<FileInfo> tempList = SafeUtils.getSafeTempFile(mActivity);
        if (tempList.size() > 0) {
            new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < tempList.size(); i++) {
                        new File(tempList.get(i).getFileAbsolutePath()).delete();
                    }
                }
            }.start();
        }
    }



    private class PrivateOperationListener implements
            FileManagerService.OperationEventListener, View.OnClickListener { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
        int mTitle = R.string.chip_encryption;
        int mPrivateType = SafeManager.ADD_PRIVATE;

        public static final String PRIVATE_DIALOG_TAG = "PrivateDialogFragment";

        public PrivateOperationListener(int titleID, int PrivateType) {
            mTitle = titleID;
            mPrivateType = PrivateType;
        }

        @Override
        public void onTaskPrepare() {
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            if (progressInfo != null) {
                if (progressInfo.isFailInfo()) {
                    switch (progressInfo.getErrorCode()) {
                        case FileManagerService.OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS:
                            mToastHelper.showToast(R.string.operation_fail);
                            break;
                        default:
                            break;
                    }
                    clearFailedFiles(); // MODIFIED by songlin.qi, 2016-06-12,BUG-1989911
                } else {
                    // add for PR934066 by yane.wang@jrdcom.com 20150304 begin
                    // ProgressDialogFragment heavyDialogFragment =
                    // (ProgressDialogFragment)
                    // getFragmentManager().findFragmentByTag(FAVORITE_DIALOG_TAG);
                    ProgressDialogFragment heavyDialogFragment = (ProgressDialogFragment) getFragmentManager()
                            .findFragmentByTag(PRIVATE_DIALOG_TAG);
                    if (isResumed()) {
                        if (heavyDialogFragment == null) {
                            heavyDialogFragment = ProgressDialogFragment
                                    .newInstance(ProgressDialog.STYLE_HORIZONTAL, mTitle,
                                            R.string.wait, R.string.cancel);
                            heavyDialogFragment.setCancelListener(this);
                            heavyDialogFragment.show(getFragmentManager(),
                                    PRIVATE_DIALOG_TAG);
//                            getFragmentManager().executePendingTransactions();
                        }
                        heavyDialogFragment.setProgress(progressInfo);
                    }
                    // add for PR934066 by yane.wang@jrdcom.com 20150304 end
                    if (heavyDialogFragment != null) {
                        heavyDialogFragment.setProgress(progressInfo);
                    }
                }
            }
        }

        @Override
        public void onTaskResult(int errorType) {
            LogUtils.d("PRI", "this is enter");
            switchToNormalView();
            LogUtils.e(TAG,"OnItemClick onTaskResult2222....");
            refreshAdapter(mApplication.mCurrentPath);
        }

        @Override
        public void onClick(View v) {
            if (mApplication.mService != null) {
                mApplication.mService.cancel(mActivity.getClass().getName());
            }
        }
    }

    /**
     * find fragment by tag
     *
     * @param tag
     * @return
     */
    private Fragment findFragmentByTag(String tag) { //PR 1750473 zibin.wang add 2016/03/09 add
        FragmentManager fm = getFragmentManager();
        if (fm == null) {
            return null;
        }

        return fm.findFragmentByTag(tag);
    }

    private void deleteNotExistFiles(String file) {
        MediaStoreHelper mMediaProviderHelper = new MediaStoreHelper(mActivity);
        MultiMediaStoreHelper.DeleteMediaStoreHelper deleteMediaStoreHelper = new MultiMediaStoreHelper.DeleteMediaStoreHelper(mMediaProviderHelper);
        deleteMediaStoreHelper.addRecord(file);
        deleteMediaStoreHelper.updateRecords();
    /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2003636*/
    }// MODIFIED by zibin.wang, 2016-05-06,BUG-2019352

    @Override
    public List<FileInfo> getSafeBoxInfo() {
        return mAdapter.getItemEditFileInfoList();
    }

    /* MODIFIED-BEGIN by songlin.qi, 2016-05-27,BUG-2208408*/
    public FileInfoAdapter getAdapter() {
        return mAdapter;
    }
    /* MODIFIED-END by songlin.qi,BUG-2208408*/

    /* MODIFIED-BEGIN by songlin.qi, 2016-06-12,BUG-1989911*/
    private void clearFailedFiles() {
        if (mApplication != null && mApplication.mFileInfoManager != null) {
            mApplication.mFileInfoManager.clearFailFiles();
        }
    }
    /* MODIFIED-END by songlin.qi,BUG-1989911*/

}
/* MODIFIED-END by wenjing.ni,BUG-2003636*/
