/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ActivityManager.StackInfo;
import android.app.ActivityManagerNative;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.IActivityListener;
import cn.tcl.filemanager.IActivitytoCategoryListener;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.adapter.FileInfoAdapter;
import cn.tcl.filemanager.adapter.GridFileInfoAdapter;
import cn.tcl.filemanager.adapter.SimPickerAdapter;
import cn.tcl.filemanager.dialog.AlertDialogFragment;
import cn.tcl.filemanager.dialog.PasswordDialog;
import cn.tcl.filemanager.dialog.PopDialogFragment;
import cn.tcl.filemanager.fragment.CategoryFragment.CategoryFragmentListener;
import cn.tcl.filemanager.fragment.CategoryFragment;
import cn.tcl.filemanager.fragment.FileBrowserFragment;
import cn.tcl.filemanager.fragment.FileBrowserFragment.AbsListViewFragmentListener;
import cn.tcl.filemanager.fragment.PermissionFragment;
import cn.tcl.filemanager.manager.CategoryCountManager;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.FileInfoComparator;
import cn.tcl.filemanager.manager.IconManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.manager.SafeManager;
import cn.tcl.filemanager.service.FileManagerService;
import cn.tcl.filemanager.service.ProgressInfo;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.PermissionUtil;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;
import cn.tcl.filemanager.view.CustomPopupWindowBasedAnchor;
import cn.tcl.filemanager.view.HorizontalListView;
import cn.tcl.filemanager.view.PathProgressLayout;
import cn.tcl.filemanager.view.SearchViewEX;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.AlertDialog.Builder;
import mst.view.menu.PopupMenu;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.toolbar.Toolbar;

/* MODIFIED-BEGIN by haifeng.tang, 2016-04-27,BUG-1991729*/
/* MODIFIED-END by haifeng.tang,BUG-1991729*/

public class CategoryActivity extends FileBaseActivity implements
        AbsListViewFragmentListener, CategoryFragmentListener,
        OnItemClickListener {

    private static final String TAG = CategoryActivity.class.getSimpleName();
    private static final String PHONE_TAG = "phone";
    private static final String SDCARD_TAG = "sdcard";
    private static final String USBOTG_TAG = "usbotg";// add for PR835938 by
    // yane.wang@jrdcom.com
    // 20141111
    private static final String CATEGORY_TAG = "category";
    private static final String PERMISSION_TAG = "permissions";
    private static final String FAVOURITE_TAG = "favourite";
    private static final String SETTINGS_TAG = "settings";
    private static final String GLOBAL_SEARCH = "global_search";
    private static final int STORAGE_SELECT_CODE = 1129;
    private static final int ADD_ENCRYPT_FILE = 1130;
    private static final String STORAGE_SELECT_TAG = "selectTag";

    private int VIEW_MODE = 1;
    private int SORT_MODE = 2;
    private int HIDE_MODE = 3;

    private static final float TRANSLUCENT_IMAGE = 0.25f;
    private static final float OPAQUE_IMAGE = 1f;

    private String mViewMode = LIST_MODE;
    private String mTagMode = CATEGORY_TAG;

    private PowerManager.WakeLock wakeLock;


    private MotionEvent mDragInitialEvent;

    private HideInputMethodListener mHideInputMethodListener;
    private IActivitytoCategoryListener mActivitytoCategoryListener;
    public AlertDialog mSelectResDialog;
    public static final int SORT_NAME = 0;
    public static final int SORT_TIME = 1;
    public static final int SORT_SIZE = 2;
    public static final int SORT_TYPE = 3;
    public static final int VIEW_BY_LIST = 0;
    public static final int VIEW_BY_GRID = 1;

    private boolean isItemClicked;
    private boolean isSearchMode;
    //	private PathProgressLayout phonePathLayout;
//	private PathProgressLayout sdcardPathLayout;
//	private PathProgressLayout usbPathLayout;
    private TextView phoneTextView;
    private TextView sdcardTextView;
    private TextView usbTextView;
    private MountManager mMountManager;
    private HorizontalListView fileBrowerList;

    private RelativeLayout mNormalBar;

    private Typeface tf;
    private static int LANDSPACE_WIDTH;
    private static int PORIT_WIDTH;
    private CustomPopupWindowBasedAnchor sortPop = null;
    protected static CustomPopupWindowBasedAnchor morePop = null;
    //private int edit_count_instance = 0;
    private Builder mBuilderTemp; // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433
    private boolean deleteFlag = false;
    private boolean mShareFlag = false;
    private boolean mSearchFromEdit = false;
    private boolean isRtl = false;//[BUG-FIX]-ADD by Junyong.Sun,2016/01/28,For PR-1529224

    private PasswordDialog mPasswordDialog;
    private BottomNavigationView mBottomView;

    private PopupMenu mPopupMenu;
    private FragmentTransaction transaction;

    @Override
    public void onAttachFragment(Fragment fragment) {
        try {
            if (fragment instanceof IActivityListener) {
                mActivityListener = (IActivityListener) fragment;
            }
            if (mCategoryFragment instanceof IActivitytoCategoryListener) {
                mActivitytoCategoryListener = (IActivitytoCategoryListener) mCategoryFragment;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onAttachFragment(fragment);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogUtils.timerMark(TAG+" start");
        super.onCreate(savedInstanceState);
        LogUtils.timerMark(TAG+" end");
        LogUtils.getAppInfo(this);
        isGrantExternalRW(this);
        updateOptionMenu();
    }

    /**
     * if build version > 23,request permissions;
     * @param context
     * @return
     */
    public boolean isGrantExternalRW(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ((Activity) context).requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.ACCESS_KEYGUARD_SECURE_STORAGE
                    , Manifest.permission.READ_PHONE_STATE
            }, PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT);
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        LogUtils.e(TAG, "onBackPressed!!");
        if (mActivityListener != null) {
            mActivityListener.showNoSearchResults(false, null);
            mActivityListener.showNoFolderResultView(false);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (mApplication.mService != null) {
            mApplication.mService.cancel(this.getClass().getName());
        }
        hideGlobalSearchViewInputMethod();

        LogUtils.e(TAG, "current_path:" + mApplication.mCurrentPath);
        LogUtils.e(TAG, "this is onbackpressed:" + mFileMode);
        if (mFileMode == FILE_MODE_EDIT) {
            LogUtils.e("BAR", "this is onbackpressed"); // MODIFIED by songlin.qi, 2016-06-01,BUG-2231253
            if (mApplication.mFileInfoManager.getShowFileList().size() == 0) {
                mActivityListener.showNoFolderResults(true);
            }
            if (getPasteCount() == 0) { // MODIFIED by songlin.qi, 2016-05-30,BUG-2220937
                updateBarTitle();
                mSelectAllTitle.setVisibility(View.GONE);
            } else {
                mActivityListener.switchToCopyView();
                mActivityListener.clearChecked();
                return;
            }
        }

        LogUtils.e(TAG, "currentMode:" + CategoryManager.mCurrentMode);
        /** false: picture category need reture floder status */
        boolean flag = true;
        List<FileInfo> showFileList = mApplication.mFileInfoManager.getShowFileList();
        if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES &&
                mMountPointManager.isSdOrPhonePath(mApplication.mCurrentPath) &&
                showFileList != null && showFileList.size() > 0 && !showFileList.get(0).isDirectory()) {
            mSelectAllTitle.setVisibility(View.GONE);
            if (mBtnEditBack != null) {
                mBtnEditBack.setVisibility(View.GONE);
            }
            flag = false;
        } else if (CategoryManager.mCurrentMode == CategoryManager.PATH_MODE && mFileMode == FILE_MODE_NORMAL) {
            LogUtils.e(TAG,"Path Mode file Normal Mode");
            mSelectAllTitle.setVisibility(View.GONE);
            if (mBtnEditBack != null) {
                mBtnEditBack.setVisibility(View.GONE);
            }
            if (getPasteCount() > 0 && CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE) { // MODIFIED by songlin.qi, 2016-05-30,BUG-2220937
                LogUtils.e(TAG,"Current Mode is CATEGORY_MODE");
                updateFragment(CATEGORY_TAG);
                changePrefCurrTag(CATEGORY_TAG);
                CategoryManager.setCurrentMode(CategoryManager.CATEGORY_MODE);
                mApplication.mFileInfoManager.clearPasteList();
                return;
            }
            if (SafeUtils.getEncryptRootPath(this).equals(mApplication.mCurrentPath)) {
                LogUtils.e(TAG,"CurrentPath is equals EncryptRootPath");
                updateFragment(CATEGORY_TAG);
                changePrefCurrTag(CATEGORY_TAG);
                CategoryManager.setCurrentMode(CategoryManager.CATEGORY_MODE);
                return;
            }
            if (mMountPointManager.isInternalMountPath(mApplication.mCurrentPath)
                    || mMountPointManager.isSDMountPath(mApplication.mCurrentPath)
                    || mMountPointManager.isUSBMountPath(mApplication.mCurrentPath)) {
                LogUtils.e(TAG,"MountPiont is sdcard otg....");
                updateFragment(CATEGORY_TAG);
                changePrefCurrTag(CATEGORY_TAG);
                CategoryManager.setCurrentMode(CategoryManager.CATEGORY_MODE);
                // finish();
                return;
            }
        } else {
            if (morePop != null && morePop.isShowing()) {
                morePop.dismiss();
            }
            if (PermissionUtil.isAllowPermission(this) && CommonUtils.hasM()) {
                changePrefCurrTag(PERMISSION_TAG);
            } else {
                changePrefCurrTag(CATEGORY_TAG);
            }
        }
        /**  Copy status return  */
        if (mFileMode == FILE_COPY_NORMAL || mFileMode == FILE_ADD_ENCRYPT) {
            if (mMountPointManager.isSdOrPhonePath(mApplication.mCurrentPath)
                    || mApplication.mCurrentPath.equals(mMountPointManager.getUsbOtgPath())
                    || mApplication.mCurrentPath.equals(SafeUtils.getEncryptRootPath(this))) { //Root directory to launch edit state
                LogUtils.e(TAG,"111111111111111111111");
                mApplication.mFileInfoManager.clearPasteList();
                setFileActionMode(FILE_MODE_NORMAL);
                mActivityListener.clearChecked();
                mActivityListener.updateActionMode(FILE_MODE_NORMAL);
                //Returns the file that triggers the copy or cut
                String copyFilePath = ((FileBrowserFragment) mCurrentFragment).getCopyFilePath();
                if (null == copyFilePath) {
                    mApplication.mCurrentPath = null;
                    CategoryManager.mCurrentMode = CategoryManager.CATEGORY_MODE;
                    CategoryManager.mCurrentCagegory = ((FileBrowserFragment) mCurrentFragment).getCurrentCategory();
                    if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                        copyStatusToPictureCategory();
                        ((FileBrowserFragment) mCurrentFragment).showPicDir();
                        LogUtils.e(TAG,"22222222222222222222");
                    } else {
                        switchContentByViewMode();
                        LogUtils.e(TAG,"333333333333333333333333333");
                    }
                } else {
                    updateCopyToCancelFragment(copyFilePath);
                    ((FileBrowserFragment) mCurrentFragment).returnFirstPosition();
                }
                updateActionbar();
                hideEditWindow();
                return;
            } else { //Other paths return to the upper layer
                mSelectAllTitle.setVisibility(View.GONE);
                if (mBtnEditBack != null) {
                    mBtnEditBack.setVisibility(View.GONE);
                }
                LogUtils.e(TAG,"44444444444444444444444");
                if (getPasteCount() > 0 && CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE) {
                    LogUtils.e(TAG,"555555555555555555");
                    updateFragment(CATEGORY_TAG);
                    changePrefCurrTag(CATEGORY_TAG);
                    CategoryManager.setCurrentMode(CategoryManager.CATEGORY_MODE);
                    return;
                }
                if (mMountPointManager.isInternalMountPath(mApplication.mCurrentPath)
                        || mMountPointManager.isSDMountPath(mApplication.mCurrentPath)
                        || mMountPointManager.isUSBMountPath(mApplication.mCurrentPath)) {
                    LogUtils.e(TAG,"66666666666666666666666666666");
                    updateFragment(CATEGORY_TAG);
                    changePrefCurrTag(CATEGORY_TAG);
                    CategoryManager.setCurrentMode(CategoryManager.CATEGORY_MODE);
                    // finish();
                    return;
                }
            }
        }

        int[] attrs = new int[]{android.R.attr.actionBarItemBackground};
        TypedArray a = getTheme().obtainStyledAttributes(attrs);
        Drawable d = a.getDrawable(0);
        if (mBtnSearch != null) {
            mBtnSearch.setBackground(d);
        }

        isSearchingDone = false;
        if (mCurrentFragment == mListFragment
                || mCurrentFragment == mGridFragment) {
            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE && mFileMode != FILE_MODE_EDIT) {
                LogUtils.e(TAG,"CATEGORY_SAFE MODE NOT EDIT");
                if (null == mApplication.mCurrentPath || SafeUtils.getEncryptRootPath(this).equals(mApplication.mCurrentPath)) {
                    LogUtils.e(TAG,"CATEGORY_SAFE CURRENTPATH IS NOT NULL");
                    mApplication.mCurrentPath = null;
                    ShowCategoryContent();
                } else {
                    LogUtils.e(TAG,"Category safe current path is null");
                    if (mActivityListener != null) {
                        mActivityListener.onBackPressed();
                    }
                }
            } else if (CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE
                    && mFileMode == FILE_MODE_NORMAL) {
                LogUtils.e(TAG,"CATEGORY_SAFE CURRENTPATH IS NOT NULL22222");
                ShowCategoryContent();
                // yane.wang@jrdcom.com 20150129
            } else if (CategoryManager.mCurrentMode == CategoryManager.PATH_MODE
                    && mFileMode == FILE_MODE_NORMAL
                    && mMountPointManager
                    .isSdOrPhonePath(mApplication.mCurrentPath) && flag) {
                LogUtils.e(TAG,"CATEGORY_SAFE CURRENTPATH IS NOT NULL33333");
                updateFragment(CATEGORY_TAG);
                drawerNowPosition = 0;
                drawerClickPosition = 0;
                // yane.wang@jrdcom.com 20150129
            } else {
                LogUtils.e(TAG,"CATEGORY_SAFE CURRENTPATH IS NOT NULL555555");
                if (mActivityListener != null) {// add for PR927979 by
                    // yane.wang@jrdcom.com 20150209
                    mActivityListener.onBackPressed();
                }
            }
            return;
        }
        LogUtils.e(TAG,"CATEGORY_SAFE CURRENTPATH IS NOT NULL666666");
        super.onBackPressed();
    }

    private void ShowCategoryContent() {
        switchContent(mCategoryFragment);
    }

    public void hideToolBar() {
        mFilePathLayout.setVisibility(View.GONE);
        mGlobalSearchView.setVisibility(View.GONE);
    }

    @Override
    public void serviceConnected() {
        super.serviceConnected();
        mViewMode = getPrefsViewBy();
        setFirstTag(true);
    }

    @Override
    public void setMainContentView() {
        super.setMainContentView();

        mPopupMenu = new PopupMenu(this,  mMoreBottomView, Gravity.BOTTOM);
        mPopupMenu.inflate(R.menu.more_bottom_menu);
        mEncryptBottomView = mPopupMenu.getMenu().getItem(0);
        mRenameBottomView = mPopupMenu.getMenu().getItem(1);
        mDetailBottomView = mPopupMenu.getMenu().getItem(2);

        if (laucherFolderName != null && !laucherFolderName.equals("") && !PermissionUtil.isAllowPermission(this)) {
            mTagMode = "";
        } else if (PermissionUtil.isAllowPermission(this) && CommonUtils.hasM()) {
            mTagMode = "permissions";
        } else {
            String path = null;
            if (getIntent() != null) {
                path = getIntent().getStringExtra("foldername");
            }
            if (mTagMode.equals(getPermissionPrefCurrTag()) || getPrefCurrTag().equals(GLOBAL_SEARCH) || PERMISSION_TAG.equals(mTagMode)// PR-1001077 Nicky Ni -001 20151201
                    || isPathInvalid(path) || isSettingsEnter) { //[BUG-FIX] by NJTS Junyong.Sun 2016/01/20 PR-1401197 // MODIFIED by wenjing.ni, 2016-05-04,BUG-1956936
                changePrefCurrTag(CATEGORY_TAG);
            }
            mTagMode = getPrefCurrTag();

            // if mApplication.mCurrentPath is null, the path info may invalid, change mTagMode to CATEGORY_TAG
            if (!mTagMode.equals(CATEGORY_TAG) &&
                    (mApplication == null || mApplication.mCurrentPath == null)) {
                mTagMode = CATEGORY_TAG;
                changePrefCurrTag(CATEGORY_TAG);
            }
        }

        mBottomPasteView.setNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.pastes_item:
                        if (morePop != null) {
                            morePop.dismiss();
                        }
                        mActivityListener.closeItemMorePop();
                        mApplication.currentOperation = FileManagerApplication.PASTE;
                        acquireWakeLock(getApplicationContext());
                        mActivityListener.clickPasteBtn();
                        updateActionbar();
                        hideEditWindow();
                        String result = path.substring(path.lastIndexOf(MountManager.SEPARATOR) + 1);
                        setActionbarTitle(result);
                        break;
                    case R.id.cancles_item:
                        if (mFileMode == FILE_COPY_NORMAL) {
                            mApplication.mFileInfoManager.clearPasteList();
                            setFileActionMode(FILE_MODE_NORMAL);
                            mActivityListener.clearChecked();
                            mActivityListener.updateActionMode(FILE_MODE_NORMAL);

                            String copyFilePath = ((FileBrowserFragment) mCurrentFragment).getCopyFilePath();
                            if (null == copyFilePath) {
                                mApplication.mCurrentPath = null;
                                CategoryManager.mCurrentMode = CategoryManager.CATEGORY_MODE;
                                CategoryManager.mCurrentCagegory = ((FileBrowserFragment) mCurrentFragment).getCurrentCategory();
                                if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                                    copyStatusToPictureCategory();
                                    ((FileBrowserFragment) mCurrentFragment).showPicDir();
                                } else {
                                    switchContentByViewMode();
                                }
                            } else {
                                updateCopyToCancelFragment(copyFilePath);
                                ((FileBrowserFragment) mCurrentFragment).returnFirstPosition();
                            }
                            updateActionbar();
                            updateOptionMenu();
                        } else {
                            onBackPressed();
                        }
                        if (morePop != null && morePop.isShowing()) {
                            morePop.dismiss();
                        }
                        hideEditWindow();
                        break;
                }
                return false;
            }
        });

        mBottomNavigationView.setNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.delete_item:
                        mApplication.currentOperation = FileManagerApplication.DETETE;
                        mActivityListener.clickDelteBtn(SafeManager.NORMAL_DELETE_MODE);
                        break;
                    case R.id.copy_item:
                        if (morePop != null) {
                            morePop.dismiss();
                        }
                        if (mFileMode == FILE_MODE_SEARCH) {
                            refreshPathAdapter(mApplication.mCurrentPath);
                        }
                        mActivityListener.clickCopyBtn();
                        mActivityListener.clearChecked();
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        setActionbarTitle((R.string.choice_file));
                        setCopyCutStatus();
                        break;
                    case R.id.cut_item:
                        if (morePop != null) {
                            morePop.dismiss();
                        }
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        if (mFileMode == FILE_MODE_SEARCH) {
                            refreshPathAdapter(mApplication.mCurrentPath);
                        }
                        mActivityListener.clickCutBtn();
                        mActivityListener.updateAdapter();
                        mActivityListener.clearChecked();
                        setActionbarTitle(R.string.choice_file);
                        setCopyCutStatus();
                        break;
                    case R.id.share_item:
                        if (morePop != null) {
                            morePop.dismiss();
                        }
                        mShareFlag = true;
                        mActivityListener.clickShareBtn();
                        hideEditWindow();
                        break;
                    case R.id.more_item:
                        if (null != mPopupMenu) {
                            mPopupMenu.show();
                        }
                        break;
                }
                return true;
            }
        });

        Builder infoDialog = new Builder(this);
        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.menu_system_safe:
                        Intent enterEncryptIntent = new Intent();
                        mApplication.mIsCategorySafe = false;
                        mApplication.mIsSafeMove = true;

                        if (SharedPreferenceUtils.isFristEnterSafe(CategoryActivity.this)) {
                            infoDialog.setTitle(R.string.welcome_dialog_title);
                            if (isSystemLock()) {
                                if (mApplication.mIsVerifySystemPwd) {
                                    // TODO to category safe
                                    Intent verificationIntent = new Intent();
                                    verificationIntent.setAction("com.tct.securitycenter.FingerprintVerify");
                                    startActivityForResult(verificationIntent, 100);
                                } else {
                                    //TODO Verify identity
                                    infoDialog.setMessage(R.string.welcome_dialog_info);
                                    infoDialog.setPositiveButton(R.string.welcome_dialog_verify_btn, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            mApplication.mIsVerifySystemPwd = true;
                                            Intent verificationIntent = new Intent();
                                            verificationIntent.setAction("com.tct.securitycenter.FingerprintVerify");
                                            startActivityForResult(verificationIntent, 100);
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
                        } else {
                            SharedPreferenceUtils.setFristEnterSafe(CategoryActivity.this, true);
                            enterEncryptIntent.putExtra(FileEncryptWelcomeActivity.STATUS_KEY, FileEncryptWelcomeActivity.MORE_STATUS);
                            enterEncryptIntent.setClass(CategoryActivity.this, FileEncryptWelcomeActivity.class);
                            startActivityForResult(enterEncryptIntent, 100);
                        }
                        break;
                    case R.id.menu_rename:
                        if (morePop != null) {
                            morePop.dismiss();
                        }
                        if (mPopupWindow != null) {
                            mPopupWindow.dismiss();
                        }
                        mApplication.currentOperation = FileManagerApplication.RENAME;
                        mActivityListener.clickRenameBtn(mQueryText);
                        break;
                    case R.id.menu_details:
                        if (morePop != null) {
                            morePop.dismiss();
                        }
                        if (mPopupWindow != null) {
                            mPopupWindow.dismiss();
                        }
                        mActivityListener.clickDetailsBtn();
                        break;
                }
                return true;
            }
        });

        mBottomAddCancleView.setNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.encrypt_cancel:
                        returnEncryptCategory();
                        mEncryptOkView.setTitle(String.format(getString(R.string.encrypt_add_percent), 0));
                        mBottomAddCancleView.setVisibility(View.GONE);
                        break;
                    case R.id.encrypt_ok:
                        if (morePop != null) {
                            morePop.dismiss();
                        }
                        if (mPopupWindow != null) {
                            mPopupWindow.dismiss();
                        }
                        if (mFileMode == FILE_ADD_ENCRYPT) {
                            mActivityListener.clickEncryptBtn();
                        } else {
                            mActivityListener.DecryptFileInfo();
                        }
                        break;
                }
                return true;
            }
        });

        mHideInputMethodListener = new HideInputMethodListener();

        RelativeLayout actionBarBaseView = (RelativeLayout) findViewById(R.id.actionbar_base);
        actionBarBaseView.setOnClickListener(this);

        mNormalBar = ((RelativeLayout) findViewById(R.id.normal_bar));

        mGlobalSearchBack.setOnClickListener(this);

        LinearLayout downLoadBtn = (LinearLayout) findViewById(R.id.download_icon_btn);
        downLoadBtn.setOnClickListener(this);

        LinearLayout storageBtn = (LinearLayout) findViewById(R.id.storage_info_btn);
        storageBtn.setOnClickListener(this);

        mSelectAllTv.setOnClickListener(this);
        mEditCancelTv.setOnClickListener(this);

        mGlobalSearchView = (SearchViewEX) findViewById(R.id.global_search);
        mGlobalSearchView.needHintIcon(false);
        mGlobalSearchView.setQueryHint(getString(R.string.searchview_hint));

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPress();
            }
        });

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // TODO Auto-generated method stub
                switch (item.getItemId()){
                    case R.id.select_item:
                        if (morePop != null) {
                            morePop.dismiss();
                        }
                        mActivityListener.clickEditBtn();
                        showEditButton();
                        break;
                    case R.id.createfolder_item:
                        mActivityListener.clickNewFolderBtn();
                        break;
                    case R.id.add_encrypt_item:
                        if (morePop != null) {
                            morePop.dismiss();
                        }
                        addEncryptFileFragment();
                        showEncryptWindow();
                        mFilePathLayout.setVisibility(View.VISIBLE);
                        setFileActionMode(FILE_ADD_ENCRYPT);
                        mActivityListener.clickEditBtn();

                        ((FileBrowserFragment) mCurrentFragment).updateActionMode(FileInfoAdapter.MODE_ADD_ENCRYPT_FILE);
                        ((FileBrowserFragment) mCurrentFragment).setCurrentCategory(CategoryManager.mCurrentCagegory);
                        CategoryManager.mCurrentCagegory = -1;

                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    case R.id.delete_album_item:
                        mApplication.currentOperation = FileManagerApplication.DETETE;
                        mActivityListener.clickDelteBtn(SafeManager.DELETE_ALBUM_MODE);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        // mTagMode="category";
        LANDSPACE_WIDTH = getResources().getDimensionPixelSize(R.dimen.main_filepathbrower_navigation_land_width);
        PORIT_WIDTH = getResources().getDimensionPixelSize(R.dimen.main_filepathbrower_navigation_porit_width);
        //edit_count_instance = getResources().getDimensionPixelSize(R.dimen.more_menu_edit_count);
//        mFilePathLayout = (LinearLayout) findViewById(R.id.file_path_layout);
        fileBrowerList = (HorizontalListView) findViewById(R.id.listview);
        tf = CommonUtils.getRobotoMedium();
        LogUtils.d("SHO", "this is screen width" + CommonUtils.getScreenWidth(this) + "this is screen height" + CommonUtils.getScreenHeight(this));
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if (PERMISSION_TAG.equals(mTagMode)) {
//            fragmentTransaction.show(mPermissionFragment).commitAllowingStateLoss();
            fragmentTransaction.replace(R.id.layout_main_frame, mPermissionFragment).commitAllowingStateLoss();
            mCurrentFragment = mPermissionFragment;
        } else if (CATEGORY_TAG.equals(mTagMode)) {
            //mActivityListener.setPaddingTop(false);
//                mLayout.setVisibility(View.GONE);0
//                fragmentTransaction.show(mCategoryFragment)
//                        .commitAllowingStateLoss();
            fragmentTransaction.replace(R.id.layout_main_frame, mCategoryFragment).commitAllowingStateLoss();
            mCurrentFragment = mCategoryFragment;
            CategoryManager.setCurrentMode(CategoryManager.CATEGORY_MODE);
        } else if (laucherFolderName != null && !laucherFolderName.equals("")) {
            LogUtils.d("LAU", "this is enter laucher folder name--8888" + laucherFolderName);
            switchShortcutList(laucherFolderName);
            mApplication.mCurrentPath = laucherFolderName;
            if (mViewMode.equals(LIST_MODE)) {
//                fragmentTransaction.show(mListFragment)
//                        .commitAllowingStateLoss();
                fragmentTransaction.replace(R.id.layout_main_frame, mListFragment).commitAllowingStateLoss();
                mCurrentFragment = mListFragment;
            } else {
//                fragmentTransaction.show(mGridFragment)
//                        .commitAllowingStateLoss();
                fragmentTransaction.replace(R.id.layout_main_frame, mGridFragment).commitAllowingStateLoss();
                mCurrentFragment = mGridFragment;
            }
            switchShortcutFragment(laucherFolderName);
        } else {
            LogUtils.d("LAU", "this is enter laucher folder name--444" + laucherFolderName);
            //mActivityListener.setPaddingTop(true);
            if (mViewMode.equals(LIST_MODE)) {
//                fragmentTransaction.show(mListFragment)
//                        .commitAllowingStateLoss();
                fragmentTransaction.replace(R.id.layout_main_frame, mListFragment).commitAllowingStateLoss();
                mCurrentFragment = mListFragment;
            } else {
//                fragmentTransaction.show(mGridFragment)
//                        .commitAllowingStateLoss();
                fragmentTransaction.replace(R.id.layout_main_frame, mGridFragment).commitAllowingStateLoss();
                mCurrentFragment = mGridFragment;
            }
            CategoryManager.setCurrentMode(CategoryManager.PATH_MODE);
        }
//        getFragmentManager().executePendingTransactions();
        //mGlobalSearchView.setOnQueryTextListener(tbxSearch_TextChanged); // MODIFIED by songlin.qi, 2016-06-12,BUG-2280817

        mGlobalSearchCancel.setOnClickListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /*PR 557108 zibin.wang add Start*/
        if (mPortraitOrientation) {
            //getWindow().setFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON, WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        /*PR 557108 zibin.wang end*/
        HideActionbar(false);
        // add for PR975490 by yane.wang@jrdcom.com 20150415 begin
        if (mApplication.mService == null) {
            return;
        }
        // add for PR975490 by yane.wang@jrdcom.com 20150415 end
        getScreenSize();// add for PR940568 by yane.wang@jrdcom.com 20150305
        setPathWidth();// add for PR936416 by yane.wang@jrdcom.com 20150228
        // if (mDrawerToggle != null) {
        // mDrawerToggle.onConfigurationChanged(newConfig);
        // }
        if (isCategoryFragment()) {
//            mCategoryFragment.onConfigurationChanged(newConfig);
            ShowCategoryContent();
            return;
        }
        if (mCurrentFragment == mGridFragment) {
            showGirdContent();
//            mActivityListener.showHideToolbar();//PR 1529211 zibin.wang add 2016.01.27
        }
        int instance = 0;
        int width = CommonUtils.getTotalWidthofListView(fileBrowerList);
        //[BUG-FIX]-BEGIN by Junyong.Sun,2016/01/28,For PR-1529224
        int maxListViewWidth = getMaxListViewWidth();
        resetFileBrowerListParams(width, maxListViewWidth);
        instance = width - maxListViewWidth;
        //[BUG-FIX]-END by Junyong.Sun,2016/01/28,For PR-1529224
        if (instance < 0) {
            fileBrowerList.mMaxX = 0;
        } else {
            fileBrowerList.mMaxX = instance;
            fileBrowerList.setSelection(fileBrowerList.getWidth());
        }
        if (sortPop != null) {
            //[BUG-FIX]-BEGIN by NJTS Junyong.Sun 2016/01/22 PR-1445808
            if (sortPop.isShowing()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sortPop.dismiss();
                        int offX = getResources().getDimensionPixelSize(R.dimen.sort_menu_pop_xoff);
                        int offY = getResources().getDimensionPixelSize(R.dimen.sort_menu_pop_yoff);
                        sortPop.update(CategoryActivity.this);
                        sortPop.showAtLocationBasedAnchor(mBtnMore, offX, offY);
                    }
                }, 200);
            }
            //[BUG-FIX]-END by NJTS Junyong.Sun 2016/01/22 PR-1445808
        }
        mActivityListener.closeItemMorePop();
//		LogUtils.d("SNS","this is fileBrowerList.mMaxX"+fileBrowerList.mMaxX+" mCurrentX is "+fileBrowerList.mCurrentX+"mNextX is "
//+fileBrowerList.mNextX+"mDisplayOffset is "+fileBrowerList.mDisplayOffset+"filebrowerList is"+fileBrowerList.getWidth()); //MODIFIED by haifeng.tang, 2016-04-07,BUG-1913438

    }

    //[BUG-FIX]-BEGIN by Junyong.Sun,2016/01/28,For PR-1529224
    private void resetFileBrowerListParams(int width, int maxListViewWidth) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) fileBrowerList.getLayoutParams();
        int listWidth = width < maxListViewWidth ? width : ViewGroup.LayoutParams.MATCH_PARENT;
        if (params == null) {
            params = new LinearLayout.LayoutParams(listWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            params.width = listWidth;
        }
        fileBrowerList.setLayoutParams(params);
    }

    //[BUG-FIX]-END by NJTS Junyong.Sun 2016/01/22 PR-1445808
    private void showGirdContent() {
        //LogUtils.d("SNS","this is showGirdContent()");
        mActivityListener.onConfiguarationChanged();
    }

    @Override
    public void updateCategoryNavigation(int id) {
        super.updateCategoryNavigation(id);
        if ((id == 0)
                && (!isCategoryFragment())) {
            ShowCategoryContent();
        }
    }

    public void switchContent(Fragment to) {
        //LogUtils.d("SNS","switchContent(Fragment to)");
        if (mCurrentFragment != mCategoryFragment || to != mCategoryFragment) {
            CategoryCountManager.getInstance().clearMap();
        }
        if (mApplication.mService != null) {
            mApplication.mService
                    .setListType(
                            SharedPreferenceUtils.isShowHidden(this) ? FileManagerService.FILE_FILTER_TYPE_ALL
                                    : FileManagerService.FILE_FILTER_TYPE_DEFAULT,
                            CategoryActivity.this.getClass().getName());
        }

        if (mCurrentFragment != to) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.layout_main_frame, to).commitAllowingStateLoss();
            mCurrentFragment = to;
            if (to == mListFragment) {
                mActivityListener = mListFragment;
                refreshPathAdapter(mApplication.mCurrentPath);
            } else if (to == mGridFragment) {
                mActivityListener = mGridFragment;
                refreshPathAdapter(mApplication.mCurrentPath);
            } else if (to == mCategoryFragment) {
                CategoryManager.mCurrentCagegory = -1;
                mActivitytoCategoryListener = mCategoryFragment;
                if (mActivityListener != null) {
                    mActivityListener.clearAdapter();
                }
                mApplication.mFileInfoManager.getCategoryFileList().clear();
                mActivitytoCategoryListener.refreshCategory();
            }
        } else if (to == mCategoryFragment) {
            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-27,BUG-1991729*/
            if (mApplication.mFileInfoManager != null) {
                mApplication.mFileInfoManager.getCategoryFileList().clear();
                CategoryManager.mCurrentCagegory = -1;
                mActivitytoCategoryListener = mCategoryFragment;
                if (mActivityListener != null) {
                    mActivityListener.clearAdapter();
                }
                /* MODIFIED-BEGIN by songlin.qi, 2016-06-06,BUG-2254120*/
                if (mActivitytoCategoryListener != null) {
                    mActivitytoCategoryListener.refreshCategory();
                }
                /* MODIFIED-END by songlin.qi,BUG-2254120*/
                /* MODIFIED-END by haifeng.tang,BUG-1991729*/
            }

        } else if (to == mListFragment) {
            mActivityListener = mListFragment;
            refreshPathAdapter(mApplication.mCurrentPath);
        } else if (to == mGridFragment) {
            mActivityListener = mGridFragment;

            refreshPathAdapter(mApplication.mCurrentPath);
        }

        new Handler().post(new Runnable() {

            @Override
            public void run() {
                updateActionbar();
            }

        });
    }

    public void updateCopyFragment() {
        if (mMountPointManager.isSDCardMounted() || mMountPointManager.isOtgMounted()) {
            Intent intent = new Intent(CategoryActivity.this, SelectStorageActivity.class);
            startActivityForResult(intent, STORAGE_SELECT_CODE);
        } else {
            mApplication.mCurrentPath = mMountPointManager.getPhonePath();
            CategoryManager.setCurrentMode(CategoryManager.PATH_MODE);
            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                switchContent(mListFragment);
            } else {
                switchContentByViewMode();
            }
        }
    }

    public void openDirToListFragmentforPicture() {
        switchContent(mListFragment);
    }

    private void updateCopyToCancelFragment(String path) {
        mApplication.mCurrentPath = path;
        CategoryManager.setCurrentMode(CategoryManager.PATH_MODE);
        switchContentByViewMode();
    }


    public void addEncryptFileFragment() {
        if(mMountPointManager.isSDCardMounted()){
            Intent intent=new Intent(CategoryActivity.this,SelectStorageActivity.class);
            startActivityForResult(intent, STORAGE_SELECT_CODE);
        }else{
            mApplication.mCurrentPath = mMountPointManager.getPhonePath();
            CategoryManager.setCurrentMode(CategoryManager.PATH_MODE);
            switchContentByViewMode();
        }
    }

    @Override
    protected void refreshPathAdapter(String path) {
        super.refreshPathAdapter(path);
        LogUtils.e(TAG,"refreshPathAdapter enter..");
        //LogUtils.d("SNS","this is refreshPathAdapter(String path)");
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-06,BUG-2254120*/
        if (mActivityListener != null) {
            mActivityListener.refreshAdapter(path);
        }
        /* MODIFIED-END by songlin.qi,BUG-2254120*/
    }

    private void updateFragment(String tag, boolean... isRootClicked) {
        mTagMode = tag;
        if (tag == PHONE_TAG) {
            CategoryManager.setCurrentMode(CategoryManager.PATH_MODE);
            // add for PR1020254 by xiaohui.gao@jrdcom.com 20150702 start
            if (isRootClicked.length > 0 && isRootClicked[0]) {
                mApplication.mCurrentPath = mMountPointManager.getPhonePath();
            } else {
                if (!TextUtils.isEmpty(mApplication.mCurrentPath)
                        && mApplication.mCurrentPath
                        .startsWith(mMountPointManager.getPhonePath())) {
                } else {
                    mApplication.mCurrentPath = mMountPointManager
                            .getPhonePath();
                }
            }
            if (laucherFolderName != null) {
                mApplication.mCurrentPath = laucherFolderName;
            }
            //Log.d("SHO","this is enter path --333---"+mMountPointManager.getPhonePath());
            // add for PR1020254 by xiaohui.gao@jrdcom.com 20150702 end
            CategoryManager.mLastCagegory = -2;// add for PR936938 by
            // yane.wang@jrdcom.com 20150302
        } else if (tag == SDCARD_TAG) {
            CategoryManager.setCurrentMode(CategoryManager.PATH_MODE);
            if (mApplication.mCurrentPath == mMountPointManager.getSDCardPath()) {
                return;
            }
            mApplication.mCurrentPath = mMountPointManager.getSDCardPath();
            if (laucherFolderName != null) {
                mApplication.mCurrentPath = laucherFolderName;
            }
            CategoryManager.mLastCagegory = -2;// add for PR936938 by
            // yane.wang@jrdcom.com 20150302
        } else if (tag == CATEGORY_TAG) {
            if (mApplication.mService != null
                    && mApplication.mService.isBusy(this.getClass().getName())) {
                mApplication.mService.cancel(this.getClass().getName());
            }
            CategoryManager.setCurrentMode(CategoryManager.CATEGORY_MODE);
            mApplication.mCurrentPath = null;
            ShowCategoryContent();
            return;
        }
        // add for PR835938 by yane.wang@jrdcom.com 20141111 begin
        else if (tag == USBOTG_TAG) {
            CategoryManager.setCurrentMode(CategoryManager.PATH_MODE);
            if (mApplication.mCurrentPath == mMountPointManager.getUsbOtgPath()) {
                return;
            }
            mApplication.mCurrentPath = mMountPointManager.getUsbOtgPath();
            if (laucherFolderName != null) {
                mApplication.mCurrentPath = laucherFolderName;
            }
            CategoryManager.mLastCagegory = -2;// add for PR936938 by
            // yane.wang@jrdcom.com 20150302
        }
        // add for PR835938 by yane.wang@jrdcom.com 20141111 end
        else if (tag == FAVOURITE_TAG) {
            if (mApplication.mService != null
                    && mApplication.mService.isBusy(this.getClass().getName())) {
                mApplication.mService.cancel(this.getClass().getName());
            }
            CategoryManager.setCurrentMode(CategoryManager.CATEGORY_MODE);
            mApplication.mCurrentPath = null;
            // CategoryManager.mCurrentCagegory =
            // CategoryManager.CATEGORY_FAVORITE;
            updateCategoryNormalBarView();
            switchCategoryList();
            return;
        } else if (tag == GLOBAL_SEARCH) {
            if (mApplication.mService != null
                    && mApplication.mService.isBusy(this.getClass().getName())) {
                mApplication.mService.cancel(this.getClass().getName());
            }
            /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1913721*/
            mApplication.mCurrentPath = null;
            //updateCategoryNormalBarView();

            if (getFileMode() != FILE_MODE_GLOBALSEARCH && getFileMode() == FILE_MODE_SEARCH) {
                switchCategoryList();
            }
            /*MODIFIED-END by haifeng.tang,BUG-1913721*/
            return;
        }
        switchContentByViewMode();
    }

    @SuppressLint("InlinedApi")
    private void changeStatusBarColor(boolean flag) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                /*PR 958557 zibin.wang add Start*/
//            Window window = getWindow();
//            if (CommonUtils.isMemory512(mApplication)) {
//                window.setStatusBarColor(Color.BLACK);
//                /*PR 958557 zibin.wang add End*/
//            } else {
//                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                if (flag) {
//                    window.setStatusBarColor(Color.BLACK);
//                } else {
//                    window.setStatusBarColor(getResources().getColor(R.color.filemanager_theme_color_dark));
//                }
//            }
//        }
    }

    public void switchContentByViewMode() {
        if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
            switchContent(mGridFragment);
        } else if (mViewMode.equals(LIST_MODE)) {// add for PR843769 by
            // yane.wang@jrdcom.com 20141118
            switchContent(mListFragment);
        } else if (mViewMode.equals(GRID_MODE)) {// add for PR843769 by
            // yane.wang@jrdcom.com
            // 20141118
            switchContent(mGridFragment);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPasswordDialog != null && mPasswordDialog.fingerPrintDialog != null && mPasswordDialog.fingerPrintDialog.isShowing()) {
            mPasswordDialog.fingerPrintDialog.dismiss();
            mPasswordDialog.stopFingerprint();
        }

    }

    // add by long.tang@tcl.com on 2015.03.31 start
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListFragment != null) {
            mListFragment.clearAll();
        }
        if (mGridFragment != null) {
            mGridFragment.clearAll();
        }
        IconManager.getInstance().clearAll();
        // ADD START FOR PR1057196 BY HONGBIN.CHEN 20150729
        if (mApplication != null) {
            if (mApplication.mService != null) {
                mApplication.mService.cancel(this.getClass().getName());
            }
            // ADD START FOR PR1101243 BY HONGBIN.CHEN 20151019 581
            if (mApplication.mFileInfoManager != null) {
                mApplication.mFileInfoManager.clearAll();
            }
            // ADD END FOR PR1101243 BY HONGBIN.CHEN 20151019
            // mApplication.mFileInfoManager.clearAll();
        }
        // ADD END FOR PR1057196 BY HONGBIN.CHEN 20150729
        CategoryCountManager.getInstance().clearMap();

        // reset value of mTagMode to CATEGORY_TAG
        changePrefCurrTag(CATEGORY_TAG);
    }


    private void setFirstTag(boolean set) {
        if (CATEGORY_TAG.equals(mTagMode)) {
            if (set) updateFragment(CATEGORY_TAG);
        } else if (PHONE_TAG.equals(mTagMode)) {
            if (set) updateFragment(PHONE_TAG);
        } else if (SDCARD_TAG.equals(mTagMode)) {
            if (mMountPointManager.isSDCardMounted()) {
                if (set) updateFragment(SDCARD_TAG);
            } else {
                if (set) updateFragment(CATEGORY_TAG);
            }
        } else if (USBOTG_TAG.equals(mTagMode)) {
            if (mMountPointManager.isOtgMounted()) {
                if (mMountPointManager.isSDCardMounted()) {
                    if (set) updateFragment(USBOTG_TAG);
                } else {
                    if (set) updateFragment(USBOTG_TAG);
                }
            } else {
                if (set) updateFragment(CATEGORY_TAG);
            }
        }
        if(set) {
            updateActionbar();
        }
    }


    @Override
    public void onEject() {
        if (mActivitytoCategoryListener != null) {
            mActivitytoCategoryListener.disableCategoryEvent(false);
        }
    }

    @Override
    public void onMounted() {
        super.onMounted();
        if (mFileMode == FILE_MODE_EDIT || mFileMode == FILE_MODE_SEARCH) {
            if (mActivityListener != null) {
                mActivityListener.onBackPressed();
            }
        }
        if (isCategoryFragment()) {
            updateCategoryContent();//add for PR943138 by yane.wang@jrdcom.com 20150325
            if (mActivitytoCategoryListener == null) {
                mActivitytoCategoryListener = (IActivitytoCategoryListener) mCategoryFragment;
            }
            /* MODIFIED-BEGIN by songlin.qi, 2016-06-06,BUG-2254120*/
            if (mActivitytoCategoryListener != null) {
                mActivitytoCategoryListener.refreshCategory();
            }
            /* MODIFIED-END by songlin.qi,BUG-2254120*/
        } else if (mApplication.mCurrentPath == null) {
            FileInfo.mountReceiver = true;
            updateCategoryItems();
        }
        setFirstTag(false);
    }

    @Override
    public void onUnmounted(String mountPoint) {
        super.onUnmounted(mountPoint);
        try {
            if (mFileMode == FILE_MODE_EDIT || mFileMode == FILE_MODE_SEARCH) {
                if (mActivityListener != null) {
                    mActivityListener.onBackPressed();
                }
            }
            setFirstTag(false);

            if (isCategoryFragment()) {
                updateCategoryContent();//add for PR943138 by yane.wang@jrdcom.com 20150325
                if (mActivitytoCategoryListener == null) {
                    mActivitytoCategoryListener = (IActivitytoCategoryListener) mCategoryFragment;
                }
                /* MODIFIED-BEGIN by songlin.qi, 2016-06-06,BUG-2254120*/
                if (mActivitytoCategoryListener != null) {
                    mActivitytoCategoryListener.refreshCategory();
                }
                /* MODIFIED-END by songlin.qi,BUG-2254120*/
                //add for PR974411 by yane.wang@jrdcom.com 20150410 begin
                if (mActivityListener != null) {
                    mActivityListener.unMountUpdate();
                }
                //add for PR974411 by yane.wang@jrdcom.com 20150410 end
            } else {
                if (mountPoint != null && mApplication.mCurrentPath != null && mApplication.mCurrentPath.startsWith(mountPoint)) {
                    if (mApplication.mService != null && mApplication.mService.isBusy(this.getClass().getName())) {
                        mApplication.mService.cancel(this.getClass().getName());
                    }
                    drawerNowPosition = 0;
                    drawerClickPosition = 0;
                    CategoryManager.setCurrentMode(CategoryManager.CATEGORY_MODE);
                    /*PR 575537 zibin.wang start */
//                    if (mApplication.mFileInfoManager.getPasteType()!=FileInfoManager.PASTE_MODE_CUT
//                            ||mApplication.mFileInfoManager.getPasteType()!=FileInfoManager.PASTE_MODE_CUT) {
                    if (mApplication.currentOperation != FileManagerApplication.PASTE) {
                        mActivityListener.unMountUpdate();
                    }
                    /*PR 575537 zibin.wang end*/
                    updateFragment(CATEGORY_TAG);
                    // ADD START FOR PR510408 BY HONGBIN.CHEN 20150817
                } else if (CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE && CategoryManager.mCurrentCagegory < 0) {
                    updateFragment(CATEGORY_TAG);
                }
                // ADD END FOR PR510408 BY HONGBIN.CHEN 20150817
            }
            if (mActivitytoCategoryListener == null) {
                mActivitytoCategoryListener = mCategoryFragment;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        showToastForUnmountCurrentSDCard(mountPoint);
    }

    private void acquireWakeLock(Context context) {
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) (context
                    .getSystemService(POWER_SERVICE));
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "Paste Task");
            wakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private void migrateBtnClicked() {
        CommonUtils.launchPhoneKeeperActivity(this); // MODIFIED by songlin.qi, 2016-06-06,BUG-2223767
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        mApplication.currentOperation = FileManagerApplication.OTHER;
        switch (item.getItemId()) {
            case R.id.space_clear:
                migrateBtnClicked();
                updateOptionMenu();
                break;

            case R.id.global_search:
                GlobalSearchData(getQueryText());
                break;
            case R.id.select:
                break;
        }
        super.onMenuItemClick(item);
        return true;
    }

    private void showChoiceResourceDialog(int type) {
        //String title = null;
        int sortpopwidth = getResources().getDimensionPixelSize(R.dimen.sort_menu_width);
        int sortPopheight = getResources().getDimensionPixelSize(R.dimen.sort_menu_pop_height);
        int sortPopXoff = getResources().getDimensionPixelSize(R.dimen.sort_menu_pop_xoff);
        int sortPopYoff = getResources().getDimensionPixelSize(R.dimen.sort_menu_pop_yoff);
        int choiceItem = 0;
        LayoutInflater inflater;
        ListView sortList;
        LinearLayout sortmenu;
        SimPickerAdapter simAdapter;
        TextView sortTitle;
        List<String> textAdapter;
        textAdapter = new ArrayList<String>();
        textAdapter.add(getString(R.string.sort_by_name));
        //textAdapter.add(getString(R.string.sort_by_date));
        textAdapter.add(getString(R.string.sort_by_time));
        textAdapter.add(getString(R.string.sort_by_size));
        textAdapter.add(getString(R.string.sort_by_type));
        //title = getString(R.string.sort_by);

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        choiceItem = preference.getInt(PopDialogFragment.SORT_ITEM, getPrefsSortBy());
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        sortmenu = (LinearLayout) inflater.inflate(R.layout.sort_menu, null);
        sortList = (ListView) sortmenu.findViewById(R.id.sort_list);
        sortList.setOverScrollMode(View.OVER_SCROLL_NEVER);
        sortTitle = (TextView) sortmenu.findViewById(R.id.name_item);
        if (tf != null) {
            sortTitle.setTypeface(tf);
        }
        simAdapter = new SimPickerAdapter(this, textAdapter, choiceItem);
        simAdapter.setSingleChoice(true);
        simAdapter.setSingleChoiceIndex(choiceItem);
        sortList.setAdapter(simAdapter);
        sortPop = new CustomPopupWindowBasedAnchor(
                sortmenu, sortpopwidth, LayoutParams.WRAP_CONTENT, CategoryActivity.this);
        // pop.showAsDropDown(mBtnSort, -250, -130);
        sortPop.showAtLocationBasedAnchor(mBtnMore, sortPopXoff, sortPopYoff);
//        LogUtils.d("POP", ">>>>>>>>> CustomPopupWindowBasedAnchor pop = " + pop);
//        LogUtils.d("POP", "this is onclick --222" + pop.isShowing());
        sortList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                sortPop.dismiss();
                // LogUtils.d("POP", ">>>>>>>>> pop = " + pop);
                sortyByCategory(position);
            }

        });
    }

    private void refreshData(int which) {
        switch (which) {
            case SORT_TYPE:
                changePrefsSortBy(FileInfoComparator.SORT_BY_TYPE);
                mApplication.mFileInfoManager.sort(mApplication.mSortType);
                mActivityListener.updateAdapter();
                break;
            case SORT_NAME:
                changePrefsSortBy(FileInfoComparator.SORT_BY_NAME);
                mApplication.mFileInfoManager.sort(mApplication.mSortType);
                mActivityListener.updateAdapter();
                break;
            case SORT_SIZE:
                changePrefsSortBy(FileInfoComparator.SORT_BY_SIZE);
                mApplication.mFileInfoManager.sort(mApplication.mSortType);
                mActivityListener.updateAdapter();
                break;
            case SORT_TIME:
                changePrefsSortBy(FileInfoComparator.SORT_BY_TIME);
                mApplication.mFileInfoManager.sort(mApplication.mSortType);
                mActivityListener.updateAdapter();
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        mApplication.currentOperation = FileManagerApplication.OTHER;
        switch (v.getId()) {
            case R.id.global_search_back:
                mGlobalSearchBar.setVisibility(View.GONE);
                mNormalBar.setVisibility(View.VISIBLE);
                backPress();
                break;
            case R.id.global_search_cancel:
//                mGlobalSearchView.setText("");
                mActivityListener.showNoSearchResults(false, "");
                break;
            case R.id.sort_btn:
                showChoiceResourceDialog(SORT_MODE);
                break;
            case R.id.share_btn:
                mActivityListener.clickShareBtn();
                break;
            case R.id.delete_btn:
                mApplication.currentOperation = FileManagerApplication.DETETE;
                mActivityListener.clickDelteBtn(SafeManager.NORMAL_DELETE_MODE);
                break;
            case R.id.more_btn:
                if (!mActivityListener.isItemMorePop() || !(CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE)) {
                    int morePopX = getResources().getDimensionPixelSize(R.dimen.more_menu_pop_xoff);
                    int morePopY = getResources().getDimensionPixelSize(R.dimen.more_menu_pop_yoff);
                    if (morePop != null && morePop.isShowing()) {
                        morePop.dismiss();
                        morePop = null;
                    }
                }
                //IS_ITEM_MORE = false;
                break;
            case R.id.global_search_image:
                // PR-1030060 Nicky Ni -001 20151209 start
                showGlobalSearchLayout(FILE_MODE_GLOBALSEARCH); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1913721

                break;

            case R.id.search_btn:
                if (isCategoryFragment()) {
                    changePrefCurrTag(GLOBAL_SEARCH);
                    updateFragment(GLOBAL_SEARCH);
                    CategoryManager.mCurrentCagegory = -1;
                    setFileActionMode(FILE_MODE_GLOBALSEARCH);
//                    mGlobalSearchView.setText("");
                    //mGlobalSearchView.requestFocusFromTouch();
                    mApplication.mFileInfoManager.saveListBeforeSearch();
                    isSearchMode = true;
                    mActivityListener.clickGlobalSearchBtn();
                } else {
                    mActivityListener.clickSearchBtn();
                    setFocusOnSearchView();
                    /* MODIFIED-BEGIN by songlin.qi, 2016-06-12,BUG-2280817*/
                    if (!TextUtils.isEmpty(mGlobalSearchView.getQuery())) {
                        mGlobalSearchView.setQuery("", false);
                        /* MODIFIED-END by songlin.qi,BUG-2280817*/
                        mQueryText = "";
                    }
                    setSearchMode(true);
                    mApplication.mFileInfoManager.saveListBeforeSearch();
                    //mGlobalSearch=false;
                }
                break;
            case R.id.floating_action_button:
                if (morePop != null) {
                    morePop.dismiss();
                }
                mActivityListener.closeItemMorePop();
                mApplication.currentOperation = FileManagerApplication.PASTE;
                acquireWakeLock(this.getApplicationContext());
                mActivityListener.clickPasteBtn();
                break;
            case R.id.search_back:
            case R.id.edit_back:
                if (mFileMode == FILE_COPY_NORMAL) {
                    mApplication.mFileInfoManager.clearPasteList();
                    mBtnEditBack.setVisibility(View.GONE);
                    setFileActionMode(FILE_MODE_NORMAL);
                    updateActionbar();
                    mActivityListener.clearChecked();
                    mActivityListener.updateActionMode(FILE_MODE_NORMAL);
                } else {
                    onBackPressed();
                }
                if (morePop != null && morePop.isShowing()) {
                    morePop.dismiss();
                }
                break;
            case R.id.phone_size_parent:
                laucherFolderName = null;
                changePrefCurrTag(PHONE_TAG);
                startFileBrowerActivity();
//                updateFragment(PHONE_TAG);
                break;
            case R.id.sd_size_parent:
                laucherFolderName = null;
                changePrefCurrTag(SDCARD_TAG);
                startFileBrowerActivity();
//                updateFragment(SDCARD_TAG);
                break;
            case R.id.external_size_parent:
                laucherFolderName = null;
                changePrefCurrTag(USBOTG_TAG);
                startFileBrowerActivity();
//                updateFragment(USBOTG_TAG);
                break;
            case R.id.sd_external_size_parent:
                Intent mIntent = new Intent();
                mIntent.setClass(CategoryActivity.this, SelectStorageForExternalActivity.class);
                startActivityForResult(mIntent, STORAGE_SELECT_CODE);
                break;
            case R.id.list_item:
                if (morePop != null) {
                    morePop.dismiss();
                }
                mActivityListener.clearAdapter();
                mViewMode = LIST_MODE;
                changePrefViewBy(LIST_MODE);
                SharedPreferenceUtils.setCurrentViewMode(this, LIST_MODE);
                switchContent(mListFragment);
                break;
            case R.id.actionbar_base:
                if (mCategoryFragment == mCurrentFragment) {
                    showGlobalSearchLayout(FILE_MODE_GLOBALSEARCH);
                    mGlobalSearchBar.setVisibility(View.VISIBLE);
                    mGlobalSearchView.setVisibility(View.VISIBLE);
                    mNormalBar.setVisibility(View.GONE);
                    mGlobalSearchView.setFocusable(true);
                    showInputMethod();
                }
                break;
            case R.id.storage_info_btn:
                Intent intent = new Intent(this, PieChartActivity.class);
                startActivity(intent);
                break;
            case R.id.download_icon_btn:
                CategoryManager.mCurrentCagegory = CategoryManager.CATEGORY_DOWNLOAD;
                startFileBrowerActivity();
//                updateCategoryNormalBarView();
//                switchCategoryList();
//                showActionMode(true);
                break;
            default:
                break;
        }
    }

    private void returnEncryptCategory() {
        mApplication.mFileInfoManager.clearPasteList();
        setFileActionMode(FILE_MODE_NORMAL);
        mActivityListener.clearChecked();
        mActivityListener.clearDecryptAndEncryptList();
        mActivityListener.updateActionMode(FILE_MODE_NORMAL);

        mApplication.mCurrentPath = null;
        CategoryManager.mCurrentMode = CategoryManager.CATEGORY_MODE;
        CategoryManager.mCurrentCagegory = CategoryManager.CATEGORY_SAFE;
        ((FileBrowserFragment) mCurrentFragment).setCurrentCategory(-1);
        updateActionbar();
        switchContentByViewMode();
        if (morePop != null && morePop.isShowing()) {
            morePop.dismiss();
        }
        hideEditWindow();
    }

    // Hide edit window and selectAll window
    public void hideEditWindow() {
        if (mBottomBarLayout != null) {
            mBottomBarLayout.setVisibility(View.GONE);
        }
        if (mSelectAllTitle != null) {
            mSelectAllTitle.setVisibility(View.GONE);
        }
        if(mBottomPasteView.getVisibility() != View.GONE) {
            mBottomPasteView.setVisibility(View.GONE);
        }
        if(mBottomNavigationView.getVisibility() != View.GONE){
            mBottomNavigationView.setVisibility(View.GONE);
        }
        if(mBottomAddCancleView.getVisibility() != View.GONE){
            mBottomAddCancleView.setVisibility(View.GONE);
        }
        if( mBottomBarLayout.getVisibility() != View.GONE){
            mBottomBarLayout.setVisibility(View.GONE);
        }
        setFileActionMode(FILE_MODE_NORMAL);
        setGlobalSearchViewEnable();
    }

    /**
     * set edit item is invisiable when show list is null
     */
    public void setEditItemIsVisiable() {
        if (CategoryManager.mCurrentCagegory > -1 && CategoryManager.mCurrentCagegory < 11) {
            if (mSelectItem != null) {
                if (mApplication.mFileInfoManager.getShowFileList().size() < 1) {
                    mSelectItem.setVisible(false);
                } else {
                    mSelectItem.setVisible(true);
                }
            }
        }
    }

    private void setCopyCutStatus(){
        mFilePathLayout.setVisibility(View.VISIBLE);
        mBottomNavigationView.setVisibility(View.GONE);
        mBottomPasteView.setVisibility(View.VISIBLE);
        mPasteBottomView.setVisibility(View.VISIBLE);
        mCancleBottomView.setVisibility(View.VISIBLE);
        mToolbar.getNavigationIcon().setAlpha(200);
        mDeleteBottomView.setVisibility(View.GONE);
        mCopyBottomView.setVisibility(View.GONE);
        mCutBottomView.setVisibility(View.GONE);
        mShareBottomView.setVisibility(View.GONE);
        mMoreBottomView.setVisibility(View.GONE);
        mPasteBottomView.setVisibility(View.VISIBLE);
        mCancleBottomView.setVisibility(View.VISIBLE);
        mSelectAllTitle.setVisibility(View.GONE);
        if (CategoryManager.CATEGORY_SAFE != CategoryManager.mCurrentCagegory) {
            mFilePathLayout.setVisibility(View.VISIBLE);
        }
    }

    public void showBottomView(String message) {
        if (!deleteFlag) {
            final Handler mHandler = new Handler();
            mBuilderTemp = new Builder(
                    CategoryActivity.this);
            mHandler.removeCallbacks(popupWindowControlRunnable);
            showUndoButton(message);
            mHandler.postDelayed(popupWindowControlRunnable, 1500);// PR-1490812 Nicky Ni -001 20160125
            mBuilderTemp.create().dismiss();
        } else {
            deleteFlag = false;
        }
    }

    private void updateEditBarWidgetState(int selectedCount) {
        if (selectedCount == 0) {
            mBottomNavigationView.setItemEnable(R.id.share_item, false);
        } else {
            if (mCanShare) {
                mBottomNavigationView.setItemEnable(R.id.share_item, true);
            } else {
                mBottomNavigationView.setItemEnable(R.id.share_item, false);
            }
        }
        mBtnMore.setEnabled(false);
    }

    //To determine the status of the file edit
    private void updateDownloadEditStatus() {
        if (((FileBrowserFragment) mCurrentFragment).getDownloadEditStatus()) {
            mBottomNavigationView.setItemEnable(R.id.delete_item, true);
            mBottomNavigationView.setItemEnable(R.id.copy_item, false);
            mBottomNavigationView.setItemEnable(R.id.cut_item, false);
            mBottomNavigationView.setItemEnable(R.id.share_item, false);
            mBottomNavigationView.setItemEnable(R.id.more_item, false);
        }
    }

    private void updateSearchResultItem(String query) {
        int resultCount = mApplication.mFileInfoManager.getSearchItemsCount();
        if (resultCount == 0 && !isSearching) {
            mActivityListener.showNoSearchResults(true, query);
            /* MODIFIED-BEGIN by wenjing.ni, 2016-04-29,BUG-2003617*/
            mActivityListener.showNoFolderResultView(false);
        } else {
            mActivityListener.showNoSearchResults(false, null);
            mActivityListener.showNoFolderResultView(false);
            /* MODIFIED-END by wenjing.ni,BUG-2003617*/
        }
    }

    private class SearchListener implements FileManagerService.OperationEventListener { // MODIFIED by haifeng.tang, 2016-05-09,BUG-2104433

        private String mSearchText;

        public SearchListener(String text) {
            mSearchText = text;
        }

        @Override
        public void onTaskResult(int result) {
            isSearching = false;
            updateSearchResultItem(mSearchText);
            if (result != FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS
                    || !mSearchText.equals(mQueryText)) {
                return;
            }
            mActivityListener.updateAdapter();
        }

        @Override
        public void onTaskPrepare() {
            mApplication.mFileInfoManager.getSearchFileList().clear();
            isSearching = true;
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            if (!progressInfo.isFailInfo()) {
                if (progressInfo.getFileInfo() != null) {
                    updateSearchResultItem(mSearchText);
                }
            }
        }
    }

    @Override
    public boolean onClose() {
        setSearchMode(false);
        super.onClose();
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-12,BUG-2280817*/
        if (mGlobalSearchView != null) {
            hideGlobalSearchViewInputMethod();
            mGlobalSearchView.clearFocus();
            /* MODIFIED-END by songlin.qi,BUG-2280817*/
        }
        super.onQueryTextSubmit(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String queryString) {
        LogUtils.e(TAG,"onQueryTextChange....");
        if (TextUtils.isEmpty(mQueryText) && TextUtils.isEmpty(queryString)) {
            return false;
        }

        if (!TextUtils.isEmpty(mQueryText)
                && !TextUtils.isEmpty(queryString)
                && TextUtils.equals(mQueryText.toLowerCase(),
                queryString.toLowerCase())) {
            return false;
        }

        String regx = ".*[/\\\\:*?\"<>|].*";
        Pattern p = Pattern.compile(regx);
        Matcher m = p.matcher(queryString);
        if (m.find()) {
            mToastHelper.showToast(R.string.invalid_char_prompt);
            return false;
        }

        mQueryText = queryString;

        if (TextUtils.isEmpty(queryString)) {
            if (isSearchMode) {
                if (mApplication.mService != null) {
                    if (mApplication.mService.isBusy(getClass().getName())) {
                        mApplication.mService.cancel(getClass().getName());
                    }
                }
                mActivityListener.refreshAdapter(mApplication.mCurrentPath);
                // ADD START FOR PR1044990 BY HONGBIN.CHEN 20150721
                mActivityListener.showBeforeSearchList();
                mActivityListener.showNoSearchResults(false, null);
                // ADD END FOR PR1044990 BY HONGBIN.CHEN 20150721
            }
            return false;
        }

        mActivityListener.clearAdapter();


        requestSearch(queryString);
        return true;
    }

    private void requestSearch(String query) {
        mSearchPath = mApplication.mCurrentPath;
        if (mGlobalSearchView != null) { // MODIFIED by songlin.qi, 2016-06-12,BUG-2280817
            if (mApplication.mService != null) {
                if (CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE) {
                    mApplication.mService
                            .categorySearch(this.getClass().getName(), query,
                                    new SearchListener(query),
                                    mApplication.mFileInfoManager
                                            .getCategoryFileList(),
                                    CategoryManager.mCurrentCagegory);
                } else {
                    mApplication.mService.search(this.getClass().getName(),
                            query, mSearchPath, new SearchListener(query));
                }
            }
        }
    }

    private void setSearchMode(boolean flag) {
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-12,BUG-2280817*/
        if (mGlobalSearchView == null) {
            return;
        }

        final CharSequence queryText = mGlobalSearchView.getQuery();
        mGlobalSearchView.onActionViewExpanded();
        isSearchMode = flag;

        if (!TextUtils.isEmpty(queryText) && mFileMode != FILE_MODE_SEARCH) {
            mGlobalSearchView.setQuery(queryText, false);
            /* MODIFIED-END by songlin.qi,BUG-2280817*/
        }

        if (flag) {
            setFocusOnSearchView();
        } else {
            if (mFileMode == FILE_MODE_SEARCH) {
                setFocusOnSearchView();
            } else {
                mGlobalSearchView.setQuery("", false); // MODIFIED by songlin.qi, 2016-06-12,BUG-2280817
            }
        }

        if (mSearchBack != null) {
            mSearchBack.setVisibility(flag ? View.VISIBLE : View.GONE);
        }
        updateDisplayOptions();
    }

    private void setFocusOnSearchView() {
        mGlobalSearchView.setIconified(false); // MODIFIED by songlin.qi, 2016-06-12,BUG-2280817
    }

    private void updateViewByTag() {
        if (isCategoryFragment()) {
            //mGlobalSearchView.setVisibility(View.VISIBLE);
            mNormalBar.setVisibility(View.VISIBLE);
            mGlobalSearchView.setVisibility(View.GONE);
            //mSelectAll.setVisibility(View.GONE);
            mSearchBar.setVisibility(View.GONE);
            //mBtnEdit.setVisibility(View.GONE);
            mBtnSort.setVisibility(View.GONE);
            mBtnShare.setVisibility(View.GONE);
            mBtnDelete.setVisibility(View.GONE);
            mBtnSearch.setVisibility(View.GONE);
            mGlobalView.setVisibility(View.VISIBLE);
        } else if (mFileMode == FILE_MODE_GLOBALSEARCH) {
            //mSelectAll.setVisibility(View.GONE);
            //mBtnEdit.setVisibility(View.VISIBLE);
            mGlobalSearchView.setVisibility(View.GONE);
            mBtnShare.setVisibility(View.GONE);
            mBtnSort.setVisibility(View.VISIBLE);
            mBtnSearch.setVisibility(View.VISIBLE);
            mGlobalView.setVisibility(View.GONE);
        } else {
            //mSelectAll.setVisibility(View.GONE);
            //mBtnEdit.setVisibility(View.VISIBLE);
            mGlobalSearchView.setVisibility(View.GONE);
            mBtnShare.setVisibility(View.GONE);
            mShareBottomView.setEnabled(false);
            mBottomNavigationView.setItemEnable(R.id.share_item, false);
            //mBtnDelete.setVisibility(View.VISIBLE);
            mBtnSort.setVisibility(View.VISIBLE);
            mBtnSearch.setVisibility(View.VISIBLE);
            mGlobalView.setVisibility(View.GONE);
            mBtnDelete.setVisibility(View.GONE);
        }
        updateMoreButtonMenu();
    }

    private void updatePasteBtn() {
    }

    private void showInputMethod() {
        InputMethodManager immshow = (InputMethodManager) this
                .getSystemService(INPUT_METHOD_SERVICE);
        if (immshow != null) {
            immshow.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void hideGlobalSearchViewInputMethod() {
        InputMethodManager immEdit = (InputMethodManager) this
                .getSystemService(INPUT_METHOD_SERVICE);
        if (immEdit != null) {
            immEdit.hideSoftInputFromWindow(mGlobalSearchView.getWindowToken(), 0);
        }
    }

    /**
     * Show more edit bottom
     */
    public void showMoreButton() {
        List<FileInfo> list = mApplication.mFileInfoManager.getShowFileList();
        if (list != null && list.size() > 0) {
            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES
                    && list.get(0).isDirectory()) {
                mMoreImage.setVisibility(View.GONE);
            } else {
                mMoreImage.setVisibility(View.VISIBLE);
            }
        }
    }

    public void showMoreBottomView(){
        List<FileInfo> list = mApplication.mFileInfoManager.getShowFileList();
        if (list != null && list.size() > 0) {
            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES
                    && list.get(0).isDirectory()) {
                mMoreBottomView.setVisibility(View.VISIBLE);
            } else {
                mMoreBottomView.setVisibility(View.VISIBLE);
            }
        }
    }


    private void updateMenuBar(Menu menu, int mode) {
        if (mCurrentFragment == mPermissionFragment) {
            if (mFilePathLayout != null) {
                mFilePathLayout.setVisibility(View.GONE);
            }
            if (mGlobalSearchView != null) {
                mGlobalSearchView.setVisibility(View.GONE);
            }
//            getActionBar().hide();
            return;

        }
        menu.clear();
        MenuInflater menuInflater = getMenuInflater();
        if (isCategoryFragment()) {
//            getActionBar().show();
//            menuInflater.inflate(R.menu.fragment_category_menu, menu);

            mNormalBar.setVisibility(View.VISIBLE);
            mGlobalSearchBar.setVisibility(View.GONE);

            mGlobalSearchView.setVisibility(View.GONE);
            clearSearchContent();
            mFilePathLayout.setVisibility(View.GONE);

            setActionBarButtonVisiable(false); // MODIFIED by haifeng.tang, 2016-04-25,BUG-1989942
            hideEditWindow();
        } else if (CategoryManager.mCurrentCagegory == -1 && GLOBAL_SEARCH.equals(getPrefCurrTag())) {
            mNormalBar.setVisibility(View.GONE);
            mGlobalSearchBar.setVisibility(View.VISIBLE);
            mGlobalSearchView.setVisibility(View.VISIBLE);
            mGlobalSearchView.requestFocus();
        } else if (mCurrentFragment == mListFragment || mCurrentFragment == mGridFragment) {
//            getActionBar().show();
            if (CategoryManager.mCurrentMode == CategoryManager.PATH_MODE) {
                if (CategoryManager.mCurrentCagegory != -1) {
                    mFilePathLayout.setVisibility(View.GONE);
                } else {
                    mFilePathLayout.setVisibility(View.VISIBLE);
                }
            } else {
                mFilePathLayout.setVisibility(View.GONE);
            }

            if (mode == FILE_MODE_GLOBALSEARCH) {
                mGlobalSearchView.setVisibility(View.VISIBLE);
            } else {
                mGlobalSearchView.setVisibility(View.GONE);
            }
            switch (mode) {
                case FILE_MODE_NORMAL:
                    if (null != mToolbar.getNavigationIcon()) {
                        mToolbar.getNavigationIcon().setAlpha(200);
                    }
                    showActionMode(false);
                    mNormalBar.setVisibility(View.VISIBLE);
                    mGlobalSearchBar.setVisibility(View.GONE);
                    FileInfoAdapter infoAdapter = ((FileBrowserFragment) mCurrentFragment).getAdapter();
//                    mCategoryFragment.isEmptyCategory();
//                    menuInflater.inflate(R.menu.normal_menu, menu);
//                    mSelectItem = menu.findItem(R.id.select_item); // MODIFIED by songlin.qi, 2016-05-27,BUG-2208408
//                    mMenuCreateFolder = menu.findItem(R.id.createfolder_item);
//                    mMenuAddEncryptFile = menu.findItem(R.id.add_encrypt_item);
                    mAddEncryptItem.setVisible(false);
                    if (CategoryManager.mCurrentMode != CategoryManager.PATH_MODE ||
                            CategoryManager.CATEGORY_PICTURES == CategoryManager.mCurrentCagegory) {
                        mCreateFolderName.setVisible(false);
                        mAddEncryptItem.setVisible(false);
                        mSelectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    }
                    /** Category encrypt menu item */
                    if (CategoryManager.CATEGORY_SAFE == CategoryManager.mCurrentCagegory) {
                        mCreateFolderName.setVisible(true);
                        mAddEncryptItem.setVisible(true);
                        mSelectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                        mCreateFolderName.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                        mAddEncryptItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                    }
                    mActivityListener.updateAdapter();  // need refresh
                    if (null != mPopupWindow) {
                        mPopupWindow.dismiss();
                    }
                    mBtnEditBack.setVisibility(View.GONE);
                    /*MODIFIED-END by haifeng.tang,BUG-1910771*/

                    setActionBarButtonVisiable(false);

                    /* MODIFIED-BEGIN by haifeng.tang, 2016-05-09,BUG-2104433*/
                    if ((mShareFlag || mApplication.currentOperation != FileManagerApplication.PASTE) && mApplication.currentOperation != FileManagerApplication.RENAME
                            && isSearchMode && CategoryManager.mCurrentCagegory != CategoryManager.CATEGORY_PICTURES) {
                        refreshPathAdapter(mApplication.mCurrentPath);
                        /* MODIFIED-END by haifeng.tang,BUG-2104433*/
                        mShareFlag = false;
                    }
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    /* MODIFIED-BEGIN by songlin.qi, 2016-05-27,BUG-2208408*/
                    // if the folder is null, hide select and grad item
                    /* MODIFIED-BEGIN by songlin.qi, 2016-06-01,BUG-2231253*/
                    if (CategoryManager.mCurrentMode != CategoryManager.CATEGORY_MODE) {
                        if (infoAdapter != null) {
                            if (infoAdapter.isEmpty()) {
                                if (mSelectItem != null) {
                                    mSelectItem.setVisible(false);
                                }
                            } else {
                                if (mSelectItem != null) {
                                    mSelectItem.setVisible(true);
                                }
                                /* MODIFIED-END by songlin.qi,BUG-2231253*/
                            }
                        }
                    }
                    /* MODIFIED-END by songlin.qi,BUG-2208408*/

                    if (isCategoryFragment()) {
                        /* MODIFIED-BEGIN by songlin.qi, 2016-05-30,BUG-2220937*/
                        if (getPasteCount() > 0 && CategoryManager.mCurrentMode != CategoryManager.CATEGORY_MODE
                                ) {
                            if (mApplication != null && mApplication.mService != null
                                    && mApplication.currentOperation == mApplication.PASTE
                                    /* MODIFIED-END by songlin.qi,BUG-2220937*/
                                    && mApplication.mService.isBusy(getClass().getName())) {
                            } else {
                                mBottomNavigationView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    hideEditWindow();
                    selectCount = 0;
                    String path = mMountPointManager
                            .getDescriptionPath(mApplication.mCurrentPath);
                    if (path != null && !path.isEmpty()) {
                        String result = null;
                        if (CategoryManager.mCurrentCagegory == -1) {
                            if (path.contains(MountManager.SEPARATOR)) {
                                result = path.substring(path
                                        .lastIndexOf(MountManager.SEPARATOR) + 1);
                                setActionbarTitle(result);
                            } else {
                                setActionbarTitle(path);
                            }
                        }
                        if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                            List<FileInfo> showFileList = mApplication.mFileInfoManager.getShowFileList();
                            if (showFileList != null && showFileList.size() > 0 && !showFileList.get(0).isDirectory()) {
                                mDeleteAlbum.setVisible(true);
                                mSelectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                            } else {
                                mDeleteAlbum.setVisible(false);
                                mSelectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                            }
                        } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
                            File[] files = (new File(mApplication.mCurrentPath)).listFiles();
                            if (files != null && files.length > 0) {
                                mSelectItem.setVisible(true);
                                mSelectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                            } else {
                                mSelectItem.setVisible(false);
                            }
                        }
                    }
                    InputMethodManager immNormal = (InputMethodManager) this
                            .getSystemService(INPUT_METHOD_SERVICE);
                    if (immNormal != null) {
                        immNormal.hideSoftInputFromWindow(mGlobalSearchView.getWindowToken(), 0, new ResultReceiver(new Handler()) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                super.onReceiveResult(resultCode, resultData);
                            }
                        });
                    }
                    break;
                /** encrypt file to direct */
                case FILE_ADD_DECRYPT:
//                    setFileActionMode(FILE_MODE_NORMAL);
//                    mActivityListener.clearChecked();
                    mActivityListener.updateActionMode(FILE_MODE_NORMAL);
                    mNormalBar.setVisibility(View.VISIBLE);
                    mGlobalSearchBar.setVisibility(View.GONE);
                    if (null != mPopupWindow) {
                        mPopupWindow.dismiss();
                    }
                    mBtnEditBack.setVisibility(View.GONE);
                    setActionBarButtonVisiable(false);
                    if ((mShareFlag || mApplication.currentOperation != FileManagerApplication.PASTE) && mApplication.currentOperation != FileManagerApplication.RENAME
                            && isSearchMode && CategoryManager.mCurrentCagegory != CategoryManager.CATEGORY_PICTURES) {
                        refreshPathAdapter(mApplication.mCurrentPath);
                        mShareFlag = false;
                    }
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    hideEditWindow();
                    setFileActionMode(FILE_ADD_DECRYPT);
                    showDecryptWindow();
                    break;
                case FILE_MODE_EDIT:
                    showSelectAllAndBottomView();
                    if (selectCount > 0) {
                        mBottomNavigationView.setItemEnable(R.id.delete_item, true);
                        mBottomNavigationView.setItemEnable(R.id.copy_item, true);
                        mBottomNavigationView.setItemEnable(R.id.cut_item, true);
                        mBottomNavigationView.setItemEnable(R.id.more_item, true);
                        mMoreBottomView.setVisibility(View.VISIBLE);
                        if (selectCount == 1) {
                            mRenameBottomView.setVisible(true);
                            mBottomNavigationView.setItemEnable(R.id.delete_item, true);
                            mBottomNavigationView.setItemEnable(R.id.copy_item, true);
                            mBottomNavigationView.setItemEnable(R.id.cut_item, true);
                            mBottomNavigationView.setItemEnable(R.id.more_item, true);
                            showMoreBottomView();
                            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
                                mDetailBottomView.setVisible(false);
                            } else {
                                mDetailBottomView.setVisible(true);
                            }
                        } else {
                            mRenameBottomView.setVisible(false);
                            mDetailBottomView.setVisible(false);
                            mBottomNavigationView.setItemEnable(R.id.delete_item, true);
                            mBottomNavigationView.setItemEnable(R.id.copy_item, true);
                            mBottomNavigationView.setItemEnable(R.id.cut_item, true);
                            mMoreBottomView.setVisibility(View.VISIBLE);
                        }
                        hideGlobalSearchViewInputMethod();
                    } else {
                        mBottomNavigationView.setItemEnable(R.id.delete_item, false);
                        mBottomNavigationView.setItemEnable(R.id.copy_item, false);
                        mBottomNavigationView.setItemEnable(R.id.cut_item, false);
                        mBottomNavigationView.setItemEnable(R.id.share_item, false);
                        mBottomNavigationView.setItemEnable(R.id.more_item, false);
                        showMoreBottomView();
                        mRenameBottomView.setVisible(true);
                        mDetailBottomView.setVisible(true);
                        mMoreBottomView.setVisibility(View.VISIBLE);
                    }
                    break;
                case FILE_ADD_ENCRYPT:
                    showSelectAllAndBottomView();
                    showEncryptWindow();
                    break;
                case FILE_MODE_SEARCH:
                    mToolbar.getNavigationIcon().setAlpha(200);
                    showActionMode(false);
                    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1913721*/
                    mFilePathLayout.setVisibility(View.GONE);
                    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-13,BUG-1938948*/
                    mGlobalSearchView.setVisibility(View.VISIBLE);
                    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-13,BUG-1935947*/
                    mGlobalSearchView.setIconified(false);
                    mGlobalSearchView.setQuery("", false);
                    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-22,BUG-1913721*/
                    mGlobalSearchView.setQueryHint(getString(R.string.searchview_hint));
                    break;
                    /*MODIFIED-END by haifeng.tang,BUG-1938948*/
/*MODIFIED-END by haifeng.tang,BUG-1913721*/
                case FILE_MODE_GLOBALSEARCH:
                    mToolbar.getNavigationIcon().setAlpha(200);
                    showActionMode(false);
                    mGlobalSearchView.setVisibility(View.VISIBLE);
                    mGlobalSearchView.setIconified(false);
                    mGlobalSearchView.setQueryHint(getString(R.string.searchview_hint));
                    /* MODIFIED-END by haifeng.tang,BUG-1913721*/
                    /*MODIFIED-END by haifeng.tang,BUG-1935947*/
                    break;
                case FILE_COPY_NORMAL:
                    if (null != mToolbar.getNavigationIcon()) {
                        mToolbar.getNavigationIcon().setAlpha(200);
                    }
                    showActionMode(false);
                    setActionBarButtonVisiable(false); // MODIFIED by haifeng.tang, 2016-04-21,BUG-1984970
                    if (!isCategoryFragment()) {
                        if (getPasteCount() > 0 && CategoryManager.mCurrentMode != CategoryManager.CATEGORY_MODE) { // MODIFIED by songlin.qi, 2016-05-30,BUG-2220937
                            mBottomNavigationView.setVisibility(View.GONE);
                        } else {
                            mBottomNavigationView.setVisibility(View.GONE);
                        }
                    } else {
                        mBottomNavigationView.setVisibility(View.GONE);
                    }
                    if (getPasteCount() != 0) { // MODIFIED by songlin.qi, 2016-05-30,BUG-2220937
                        if (mBtnEditBack != null) {
                            mBtnEditBack.setVisibility(View.VISIBLE);
                            mBtnEditBack.setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_clear_white_24dp));
                        }
                        setActionbarTitle(R.string.choice_file);
                        setActionbarTitle(R.string.choice_file);
                    }
                    selectCount = 0;
                    break;
                default:
                    break;
            }


            updateMoreButtonMenu();

        }


    }

    private void showSelectAllAndBottomView() {
        mBottomBarLayout.setVisibility(View.VISIBLE);
        mToolbar.getNavigationIcon().setAlpha(0);
        showActionMode(true);
        getActionMode().setPositiveText(getString(R.string.select_all));
        getActionMode().setNagativeText(getString(R.string.cancel));
        setActionModeListener(new ActionModeListener() {
            @Override
            public void onActionItemClicked(ActionMode.Item item) {
                int id = item.getItemId();
                switch (id) {
                    case ActionMode.NAGATIVE_BUTTON:
                        backPress();
                        mBottomBarLayout.setVisibility(View.GONE);
                        mToolbar.getNavigationIcon().setAlpha(200);
                        mBottomNavigationView.setVisibility(View.GONE);
                        mBottomAddCancleView.setVisibility(View.GONE);
                        break;
                    case ActionMode.POSITIVE_BUTTON:
                        mToolbar.getNavigationIcon().setAlpha(200);
                        if (mActivityListener != null) {
                            mActivityListener.clickSelectAllBtn();
                        }
                        break;
                }
            }

            @Override
            public void onActionModeShow(ActionMode actionMode) {

            }

            @Override
            public void onActionModeDismiss(ActionMode actionMode) {

            }
        });
        if (mActivityListener != null && mActivityListener.getAdapterSize() > 0) {
            setActionBarButtonVisiable(false);
            if (mActivityListener.checkIsSelectAll()) {
                getActionMode().setPositiveText(getString(R.string.deselect_all_item));
            } else {
                getActionMode().setPositiveText(getString(R.string.select_all));
            }
        }
        mSelectItem.setVisible(false);
        mCreateFolderName.setVisible(false);
        mDeleteAlbum.setVisible(false);
    }

    /**
     * set operat imageview is enable
     *
     * @param alpha
     */
    private void clearSearchContent() {
        mGlobalSearchView.setQuery("", false);
    }


    protected void showForbiddenDialog() {
        AlertDialogFragment.AlertDialogFragmentBuilder builder = new AlertDialogFragment.AlertDialogFragmentBuilder();
        AlertDialogFragment forbiddenDialogFragment = builder
                .setTitle(R.string.drm_forwardforbidden_title)
                .setIcon(R.drawable.ic_dialog_alert_holo_light)
                .setMessage(R.string.drm_forwardforbidden_message)
                .setCancelable(false).setCancelTitle(R.string.ok).create();
        forbiddenDialogFragment.show(getFragmentManager(), AlertDialogFragment.TAG);
    }

    @Override
    public void updateEditBar(int count, boolean isHasDir, boolean isHasDrm,
                              boolean canShare) {
        if (mFileMode == FILE_MODE_NORMAL) {
            mBtnMore.setEnabled(true);
            return;
        }

        /*PR 10689096 zibin.wang add End*/
        changeStatusBarColor(true);
        // mFilePathLayout.setBackgroundColor(getResources().getColor(R.color.search_line_color));
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        mSelectItem.setVisible(false);
        mCreateFolderName.setVisible(false);
        mDeleteAlbum.setVisible(false);
        setActionbarTitle(null);

        selectCount = count;
        mIsHasDir = isHasDir;
        mIsHasDrm = isHasDrm;
        mCanShare = canShare;
        updateEditBarWidgetState(count);
        updateMoreButtonMenu();
        updateOptionMenu();
        updateDownloadEditStatus();
        LogUtils.d(TAG, "this is enter updateEditBar");
    }

    @Override
    public void setFileActionMode(int mode) {
        setFileMode(mode);
    }

    @Override
    public void updateActionbar() {
        updateBarTitle();
        updateOptionMenu();
    }

    @Override
    public void updateEncryptFileCount(int count) {
        if(null != mEncryptOkView) {
            mEncryptOkView.setTitle(String.format(getString(R.string.encrypt_add_percent), count));
        }
    }

    @Override
    public void hideEditWindowWhenEncryptFile() {
        if (mEditLayout != null) {
            mEditLayout.setVisibility(View.GONE);
        }
//        if (mSelectAllTitle != null) {
//            mSelectAllTitle.setVisibility(View.GONE);
//        }
        setFileActionMode(FILE_MODE_NORMAL);
        invalidateOptionsMenu();
        setGlobalSearchViewEnable();
    }

    public void updateBarTitle() {
        if (mFileMode == FILE_ADD_ENCRYPT) {
            return;
        }

        if (mApplication == null)
            return;
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }
        String path = mMountPointManager
                .getDescriptionPath(mApplication.mCurrentPath);
        if (path != null && !path.isEmpty() && !path.endsWith(SafeUtils.SAFE_ROOT_DIR) &&
                (CategoryManager.mCurrentMode == CategoryManager.PATH_MODE || !MountManager.getInstance().isSdOrPhonePath(mApplication.mCurrentPath))) {
            String result = null;
            if (getPasteCount() == 0) { // MODIFIED by songlin.qi, 2016-05-30,BUG-2220937
                if (path.contains(MountManager.SEPARATOR)) {
                    result = path.substring(path
                            .lastIndexOf(MountManager.SEPARATOR) + 1);
                    setActionbarTitle(result);
                } else {
                    if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                        List<FileInfo> fileInfos = mApplication.mFileInfoManager.getShowFileList();
                        if (null != fileInfos && fileInfos.size() > 0) {
                            if (fileInfos.get(0).isDirectory()) {
                                mFilePathLayout.setVisibility(View.GONE);
                                setActionbarTitle(getString(R.string.category_pictures));
                            } else {
                                mFilePathLayout.setVisibility(View.GONE);
                                setActionbarTitle(getString(R.string.phone_storage_cn));
                            }
                        } else {
                            mFilePathLayout.setVisibility(View.GONE);
                            setActionbarTitle(getString(R.string.category_pictures));
                        }
                    } else {
                        mFilePathLayout.setVisibility(View.GONE);
                        setActionbarTitle(path);
                    }
                }
            }
        } else if (CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE && getPasteCount() > 0) { // MODIFIED by songlin.qi, 2016-05-30,BUG-2220937
            //setActionbarTitle(mApplication.mFileInfoManager.getPasteCount()+" "+getResources().getString(R.string.file_copy));
            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_RECENT ||
//                    CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_APKS ||
                    CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_BLUETOOTH ||
                    CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_DOCS ||
                    CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_DOWNLOAD ||
                    CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_MUSIC ||
                    CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE ||
                    CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES ||
                    CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_VEDIOS) {
                setActionbarTitle(R.string.choice_file);
            } else {
                setSearchTitle(R.string.category_fragment_title);
            }
        } else if (CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE ||
                MountManager.getInstance().isSdOrPhonePath(mApplication.mCurrentPath)) {
            //LogUtils.d("TI","this is enter "+CategoryManager.mCurrentCagegory);
            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_RECENT) {
                setActionbarTitle(R.string.main_recents_cn);
            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-27,BUG-2001239*/
            } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_APKS) {
                setActionbarTitle(R.string.main_installers);
                /* MODIFIED-END by haifeng.tang,BUG-2001239*/
            } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_BLUETOOTH) {
                setActionbarTitle(R.string.category_bluetooth);
            } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_DOCS) {
                setActionbarTitle(R.string.category_files);
            } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_DOWNLOAD) {
                setActionbarTitle(R.string.category_download);
                mFilePathLayout.setVisibility(View.GONE);
            } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_MUSIC) {
                setActionbarTitle(R.string.category_music);
            } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                setActionbarTitle(R.string.category_pictures);
                mFilePathLayout.setVisibility(View.GONE);
            } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_VEDIOS) {
                setActionbarTitle(R.string.category_vedios);
            } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
                setActionbarTitle(R.string.category_safe);
            } else {
                setSearchTitle(R.string.category_fragment_title);
            }
        } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
            setActionbarTitle(R.string.category_safe);
        } else {
            setSearchTitle(R.string.category_fragment_title);
        }
    }

    @Override
    public int getFileActionMode() {
        return getFileMode();
    }

    @Override
    public void updateNormalBarView() {
        //LogUtils.d("SNS","this is enter updatebarview");
        LogUtils.d("BAR", "this is enter updateNormalBar() --111");
//        mFilePathLayout.setVisibility(View.GONE);
        //mActivityListener.setPaddingTop(true);
        //home_item.setTextColor(getResources().getColor(R.color.main_nava_item_light));
//		home_item.setTextColor(getResources().getColor(R.color.main_bac_color));
//		home_item.setAlpha(0.3f);
//	    if (adapter!=null)
//	    {
//	       adapter.notifyDataSetChanged();
//	    }
        getPathBrowerText();
        updateNormalBar();
    }

    @Override
    public void setPrefsSortby(int sort) {
        changePrefsSortBy(sort);
    }

    @Override
    public void changeSearchMode(boolean flag) {
        setSearchMode(flag);
    }

    @Override
    public void toShowForbiddenDialog() {
        showForbiddenDialog();
    }

    @Override
    public void switchCategoryList() {
        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1913721*/
        LogUtils.i(TAG, "switchCategoryList");
        switchContentByViewMode();
    }

    @Override
    public void pasteBtnUpdated() {
//        updatePasteBtn();
/*MODIFIED-END by haifeng.tang,BUG-1913721*/
    }

    @Override
    public void updateCategoryNormalBarView() {
        updateCategoryNormalBar();
    }

    @Override
    public void toReleaseWakeLock() {
        releaseWakeLock();
    }

    // [FEATURE]-Add-BEGIN by TSBJ,shuang.liu1,09/24/2014,FR-791321,

    /**
     * Handle the touch event to update the drag View position in Dual Screen
     */
    private boolean handleTouchEvent(MotionEvent event) {
        FileBrowserFragment fragment;

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDragInitialEvent = event;
        }

        if (mCurrentFragment == mListFragment
                || mCurrentFragment == mGridFragment) {
            fragment = (FileBrowserFragment) mCurrentFragment;
            if (fragment != null && fragment.isDragReady()) {// add for PR848307
                // by
                // yane.wang@jrdcom.com
                // 20141124
                switch (action) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        fragment.stopDrag();
                        if (isDropInAnotherApp(event)) {
                            this.sendBroadcast(fragment.genereteIntent());
                        }
                        break;

                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        int x = (int) event.getRawX();
                        int y = (int) event.getRawY();
                        fragment.updateDragViewPosition(x, y);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isDropInAnotherApp(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        int dragWindow = -1;
        int dropWindow = -1;

        IActivityManager activityManager = ActivityManagerNative.getDefault();

        try {
            Method getStackMehtod = Class.forName(
                    "android.app.IActivityManager").getMethod("getFocusStack");
            dragWindow = (Integer) getStackMehtod.invoke(activityManager);
            Method getStackBoxesMethod = Class.forName(
                    "android.app.IActivityManager").getMethod(
                    "getTempStackBoxes");
            List<StackInfo> stackBoxInfos = (List<StackInfo>) getStackBoxesMethod
                    .invoke(activityManager);
            if (stackBoxInfos != null) {
                int len = stackBoxInfos.size();
                for (int i = 0; i < len; i++) {
                    if (stackBoxInfos.get(i).bounds != null) {
                        Rect childTop = stackBoxInfos.get(i).bounds;
                        if (childTop.contains(x, y)) {
                            dropWindow = 0;
                        } else {
                            dropWindow = -1;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (dropWindow != -1) {
            return dragWindow != dropWindow;
        }

        return false;
    }

    public MotionEvent getDragInitialEvent() {
        return mDragInitialEvent;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        handleTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }


    // add for PR845930 by yane.wang@jrdcom.com 20141127 begin
    private void updateCategoryItems() {
        if (FileInfo.mountReceiver && FileInfo.scanFinishReceiver) {
            switchCategoryList();
        }
    }

    @Override
    public void onScannerFinished() {
        if (mActivitytoCategoryListener == null) {
            if (mCategoryFragment == null) {
//                mCategoryFragment = (CategoryFragment) getFragmentManager().findFragmentById(R.id.categoryfragment);
                mCategoryFragment = new CategoryFragment();
            }
            mActivitytoCategoryListener = (IActivitytoCategoryListener) mCategoryFragment;
        }
        if (mActivitytoCategoryListener != null) {
            mActivitytoCategoryListener.disableCategoryEvent(true);// add for PR931139 by yane.wang@jrdcom.com 20150213
            mActivitytoCategoryListener.onScannerFinished();
            if (mActivityListener != null) { //MODIFIED by haifeng.tang, 2016-04-07,BUG-1913438
                mActivityListener.onScannerFinished();
            }
        }
    }

    @Override
    public void onScannerStarted() {
        if (mActivitytoCategoryListener == null) {
            if (mCategoryFragment == null) {
//                mCategoryFragment = (CategoryFragment) getFragmentManager().findFragmentById(R.id.categoryfragment);
                mCategoryFragment = new CategoryFragment();
            }
            mActivitytoCategoryListener = (IActivitytoCategoryListener) mCategoryFragment;
        }
        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-07,BUG-1913438*/
        //mActivityListener can is null
        if (mActivitytoCategoryListener != null && mActivityListener != null) {
        /*MODIFIED-END by haifeng.tang,BUG-1913438*/
            mActivitytoCategoryListener.onScannerStarted();
            mActivityListener.onScannerStarted();
        }
    }

//	public void notifyAbsListViewDone(boolean isDone) {
//	}

    public void notifyCategoryDone(boolean isDone) {
    }

    private void updateCategoryContent() {
        if (mPortraitOrientation) {
//            mLayout.setVisibility(View.GONE);
            mCurrentFragment = mCategoryFragment;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getCustomToolbar().inflateMenu(R.menu.fragment_category_menu);
//        setActionBarButtonOnClickLitener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch (v.getId()) {
//                    case R.id.actionbar_button_cancel:
//                        setFileActionMode(FILE_MODE_NORMAL);
//                        updateActionbar();
//                        updateOptionMenu();
//                        mActivityListener.clearChecked();
//                        mActivityListener.updateActionMode(FILE_MODE_NORMAL);
//                        hideEditWindow();
//                        break;
//                    case R.id.actionbar_button:
//                /*MODIFIED-BEGIN by wenjing.ni, 2016-04-13,BUG-1941073*/
//                        if (mActivityListener != null) {
//                            mActivityListener.clickSelectAllBtn();
//                        }
//                /*MODIFIED-END by wenjing.ni,BUG-1941073*/
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void updateOptionMenu(){
        updateMenuBar(getOptionMenu(), getFileActionMode());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean flag = false;
        switch (item.getItemId()) {

            /**
             * category fragment menu start
             */
            case R.id.global_search_image:
                showGlobalSearchLayout(FILE_MODE_GLOBALSEARCH); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1913721
                flag = true;
                break;

            case R.id.space_clear:
                migrateBtnClicked();
                updateOptionMenu();
                break;

            /**
             * category fragment menu end
             */

            case R.id.sort_type:
                sortyByCategory(SORT_TYPE);
                flag = true;
                break;
            case R.id.sort_name:
                sortyByCategory(SORT_NAME);
                flag = true;
                break;
            case R.id.sort_size:
                sortyByCategory(SORT_SIZE);
                flag = true;
                break;
            case R.id.sort_time:
                sortyByCategory(SORT_TIME);
                flag = true;
                break;
            case R.id.search_btn:
                showSearchLayout();
                flag = true;
                break;

            case R.id.select_item:
                mActivityListener.clickEditBtn();
                showEditButton();
                flag = true;
                break;
            case android.R.id.home:
                // handleNavigationDrawerToggle();
                backPress();
                flag = true;
                break;

        }
        if (flag){
            updateOptionMenu();
            return flag;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void switchDecryptDircetion(List<FileInfo> fileInfos) {
        if (morePop != null) {
            morePop.dismiss();
        }
        addEncryptFileFragment();
        showDecryptWindow();
        mFilePathLayout.setVisibility(View.VISIBLE);
        setFileActionMode(FILE_ADD_DECRYPT);

        ((FileBrowserFragment) mCurrentFragment).updateActionMode(FileInfoAdapter.MODE_ADD_DECRYPT_FILE);
        ((FileBrowserFragment) mCurrentFragment).setCurrentCategory(CategoryManager.mCurrentCagegory);
        CategoryManager.mCurrentCagegory = -1;

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        invalidateOptionsMenu();
    }

    /**
     * Show bottom edit button
     */
    public void showEditButton() {
        List<FileInfo> list = mApplication.mFileInfoManager.getShowFileList();
        if (list != null && list.size() > 0) {
            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES
                    && list.get(0).isDirectory()) {
                showEditPictureWindow();
            } else {
                showEditWindow();
            }
        }
    }

    /**
     * from copy status to picture type status
     */
    private void copyStatusToPictureCategory(){
        if (mCurrentFragment != mCategoryFragment || ((Fragment) mGridFragment) != mCategoryFragment) {
            CategoryCountManager.getInstance().clearMap();
        }
        if (mApplication.mService != null) {
            mApplication.mService
                    .setListType(
                            SharedPreferenceUtils.isShowHidden(this) ? FileManagerService.FILE_FILTER_TYPE_ALL
                                    : FileManagerService.FILE_FILTER_TYPE_DEFAULT,
                            CategoryActivity.this.getClass().getName());
        }

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.layout_main_frame, mGridFragment).commitAllowingStateLoss();
        mCurrentFragment = mGridFragment;
        mActivityListener = mGridFragment;
    }

    /* MODIFIED-BEGIN by songlin.qi, 2016-06-12,BUG-1989911*/
    private void clearFailedFiles() {
        if (mApplication != null && mApplication.mFileInfoManager != null) {
            mApplication.mFileInfoManager.clearFailFiles();
        }
    }
    /* MODIFIED-END by songlin.qi,BUG-1989911*/

    public void setGlobalSearchViewUnable() {
        if (null != mGlobalSearchView) {
            mGlobalSearchView.setInputType(0);
            mGlobalSearchView.setEnabled(false);
        }
    }

    public void setGlobalSearchViewEnable() {
        if (null != mGlobalSearchView) {
            mGlobalSearchView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            mGlobalSearchView.setEnabled(true);
        }
    }

    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1913721*/
    private void showGlobalSearchLayout(int category) {
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-12,BUG-2280817*/
        // set for global search
        mGlobalSearchView.setOnQueryTextListener(tbxSearch_TextChanged);
        /* MODIFIED-END by songlin.qi,BUG-2280817*/
        setFileActionMode(category);
        CategoryManager.mCurrentCagegory = -1;
        /*MODIFIED-END by haifeng.tang,BUG-1913721*/
        if (mViewMode == LIST_MODE) {
            switchContent(mListFragment);
        } else {
            switchContent(mGridFragment);
        }
        changePrefCurrTag(GLOBAL_SEARCH);
        updateFragment(GLOBAL_SEARCH);
        isSearchMode = true;
        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1913721*/
        if (category == FILE_MODE_GLOBALSEARCH) {
            mActivityListener.clickGlobalSearchBtn();
        } else {
            mActivityListener.clickSearchBtn();
        }
//        mActivityListener.setHideInputMethod(mHideInputMethodListener);

        mActivityListener.clearAdapter();

    }

    private void showSearchLayout() {
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-12,BUG-2280817*/
        // set for path search
        if (mActivityListener != null) {
            mActivityListener.clickSearchBtn();
        }
        setFocusOnSearchView();

        if (!TextUtils.isEmpty(mGlobalSearchView.getQuery())) {
            mGlobalSearchView.setQuery("", false);
        }
        mGlobalSearchView.setOnQueryTextListener(this);
        /* MODIFIED-END by songlin.qi,BUG-2280817*/
        mQueryText = ""; //MODIFIED by haifeng.tang, 2016-04-13,BUG-1938948
        setSearchMode(true);
        mApplication.mFileInfoManager.saveListBeforeSearch();
        mActivityListener.clearAdapter();
    }

    private void sortyByCategory(int category) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(CategoryActivity.this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PopDialogFragment.SORT_ITEM, category);
        editor.commit();
        refreshData(category);
    }


    private void updateMoreButtonMenu() {
        if (mBtnMore == null)
            return;
        if (isCategoryFragment()) {
            if (CommonUtils.checkApkExist(this, CommonUtils.PHONE_KEEPER_PACKAGE_NAME)) {
                mBtnMore.setVisibility(View.VISIBLE);
            } else {
                mBtnMore.setVisibility(View.GONE);
            }
        } else {
            mBtnMore.setVisibility(View.VISIBLE);
            if (mFileMode == FILE_MODE_EDIT) {
//				mBtnMore.setEnabled(selectCount != 0);
//			} else {
                mBtnMore.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backPress();
        }
        return false;
    }

    private void backPress() {
        /** from file path to search result list */
        if (null != mApplication.mRecordSearchPath && mApplication.mRecordSearchPath.equals(mApplication.mCurrentPath)) {
            File file = new File(mApplication.mRecordSearchPath);
            if (file.exists() && !file.isDirectory()) {
                switchToCategoryOnBack();
            } else {
                hideEditWindow();
                showGlobalSearchLayout(FILE_MODE_GLOBALSEARCH);
                mApplication.mFileInfoManager.addItemList(mApplication.mSearchResultList);
                mApplication.mFileInfoManager
                        .updateSearchList(FileInfoComparator.SORT_BY_TYPE);
                mActivityListener.updateAdapter();
                updateSearchResultItem(mQueryText);
                updateActionbar();
                updateOptionMenu();
                mApplication.mRecordSearchPath = null;
                mFilePathLayout.setVisibility(View.GONE);
            }
            /** return encrypt category */
        } else if (MountManager.getInstance().isSdOrPhonePath(mApplication.mCurrentPath) &&
                CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE &&
                (mFileMode == FILE_ADD_ENCRYPT || mFileMode == FILE_ADD_DECRYPT)) {
            returnEncryptCategory();
        } else if (mFileMode == FILE_MODE_GLOBALSEARCH && !(mApplication.mCurrentPath != null
                && !MountManager.getInstance().isSdOrPhonePath(mApplication.mCurrentPath))) {
            switchToCategoryOnBack();
        } else if (isCategoryFragment()) {
            this.finish();
        } else {
            onBackPressed();
            updateOptionMenu();
        }
    }

    /**
     * switch to main activity when click back
     */
    private void switchToCategoryOnBack(){
        if (null != mApplication.mFileInfoManager) {
            mApplication.mFileInfoManager.clearAll();
        }
        globalSearchToCategory();
    }

    private void globalSearchToCategory() {
        setFileActionMode(FILE_MODE_NORMAL);
        changePrefCurrTag(CATEGORY_TAG);
        updateFragment(CATEGORY_TAG);
        hideGlobalSearchViewInputMethod();

        updateOptionMenu();
        mGlobalSearchView.clearFocus();
        if (mActivityListener != null) {
            mActivityListener.onGlobalSearchBackPressed();
        }
        mViewMode = getPrefsViewBy();
    }

    private void GlobalSearchData(CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence)) {
            return;
        }
        mQueryText = charSequence.toString();

        if (TextUtils.isEmpty(charSequence.toString())) {
            if (isSearchMode) {
                if (mApplication.mService != null) {
                    if (mApplication.mService.isBusy(getClass().getName())) {
                        mApplication.mService.cancel(getClass().getName());
                    }
                }
                // ADD START FOR PR1044990 BY HONGBIN.CHEN 20150721
                mActivityListener.showBeforeSearchList();
                mActivityListener.showNoSearchResults(false, null);
                // ADD END FOR PR1044990 BY HONGBIN.CHEN 20150721
            }
            return;
        }
        mActivityListener.clearAdapter();
        String regx = ".*[/\\\\:*?\"<>|].*";
        Pattern p = Pattern.compile(regx);
        Matcher m = p.matcher(charSequence.toString());
        if (m.find()) {
            mToastHelper.showToast(R.string.invalid_char_prompt);
            return;
        }
        requestSearchFiles(charSequence.toString()); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1913721
    }

    public void getPathBrowerText() {
        path = mMountPointManager
                .getDescriptionPath(mApplication.mCurrentPath);
        LogUtils.d("SHO", "path is path --111" + path);
        if ((!TextUtils.isEmpty(path) && !TextUtils.isEmpty(comparepath)) || (!TextUtils.isEmpty(path) && !path.equals(comparepath))) {
            paths = path.split(File.separator);
            LogUtils.d("SHO", "path is path --222" + paths.length);
            LogUtils.d("SNS", "this is PathBrowserText----333" + path);
            comparepath = path;
            adapter = new DataAdapter(this);
            // fileBrowerList.setSelection(adapter.getCount());
            // servercollectionhanlder.sendEmptyMessage(0);
            /*MODIFIED-BEGIN by wenjing.ni, 2016-04-16,BUG-1957056*/
            if (fileBrowerList == null) {
                return;
            }
            /*MODIFIED-END by wenjing.ni,BUG-1957056*/
            fileBrowerList.setAdapter(adapter);
            //[BUG-FIX]-BEGIN by Junyong.Sun,2016/01/28,For PR-1529224
            isRtl = fileBrowerList.isLayoutRtl();
            int width = CommonUtils.getTotalWidthofListView(fileBrowerList);
            int maxListViewWidth = getMaxListViewWidth();
            resetFileBrowerListParams(width, maxListViewWidth);
            if (width >= maxListViewWidth) {
                fileBrowerList.count = 0;
                fileBrowerList.setSelection(fileBrowerList.getWidth());
            }
            //[BUG-FIX]-END by Junyong.Sun,2016/01/28,For PR-1529224
        }
//			else {
//			adapter.notifyDataSetInvalidated();
//		}
        LogUtils.d("SNS", "this is PathBrowserText");
    }

    //[BUG-FIX]-BEGIN by Junyong.Sun,2016/01/28,For PR-1529224
    private int getMaxListViewWidth() {
        return CommonUtils.getScreenWidth(this) - mFilePathLayout.getPaddingStart() - mFilePathLayout.getPaddingEnd();
    }

    //[BUG-FIX]-END by Junyong.Sun,2016/01/28,For PR-1529224
    private String[] paths;
    private DataAdapter adapter;
    private GridFileInfoAdapter mGridAdapter;
    private FileInfoAdapter mFileAdapter;
    private String path;
    public static String comparepath = "";
    private StringBuilder absolutePath;

    class DataAdapter extends BaseAdapter {
        private Context mcontext;
        private LayoutInflater mInflater;

        public DataAdapter(Context c) {
            this.mcontext = c;
            mInflater = (LayoutInflater) c
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            LogUtils.d("SNS", "this is PathBrowserText ---4444" + path);
        }

        @Override
        public int getCount() {
            return paths.length;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            LogUtils.d("SHO", "path is path --333" + paths.length);
            if (isRtl) {//[BUG-FIX] by Junyong.Sun,2016/01/28,For PR-1529224
                position = paths.length - 1 - position;
            }
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.horizontallist_item,
                        null);
                holder = new ViewHolder();
                holder.file_name = (TextView) convertView
                        .findViewById(R.id.horizontallist_item_path);
                holder.file_name_hight = (TextView) convertView
                        .findViewById(R.id.horizontallist_item_hight);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (position == 0) {
                holder.file_name.setText(getString(R.string.storage) + " / " + paths[position].toUpperCase());
                holder.file_name_hight.setText(getString(R.string.storage) + " / " + paths[position].toUpperCase());
            } else {
                holder.file_name.setText(" / " + paths[position].toUpperCase());
                holder.file_name_hight.setText(" / " + paths[position].toUpperCase());
            }

//            holder.path_icon.setColorFilter(getResources().getColor(R.color.main_recent_color), PorterDuff.Mode.SRC_IN);
            //final int positions = position;
            // holder.file_name.setOnClickListener(new OnClickListener() {
            //
            // @Override
            // public void onClick(View v) {
            //
            // // switchContentByViewMode();
            // // Toast.makeText(FileBrowserActivity.this,
            // // absolutePath.toString(), Toast.LENGTH_LONG).show();
            // }
            //
            // });

            return convertView;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView file_name;
            TextView file_name_hight;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (isRtl) {
            position = paths.length - 1 - position;
        }
        absolutePath = getAbsolutePath(position);
        if (mFileMode == FILE_MODE_EDIT || mFileMode == FILE_MODE_SEARCH) {
            mActivityListener.onBackPressed();
        }
        //getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.main_bar_color));
//		String rootPath = null;
//		if (mTagMode == PHONE_TAG) {
//			rootPath = mMountPointManager.getPhonePath();
//		} else if (mTagMode == SDCARD_TAG) {
//			rootPath = mMountPointManager.getSDCardPath();
//		} else if (mTagMode == USBOTG_TAG) {
//			rootPath = mMountPointManager.getUsbOtgPath();
//		}
//		for (int i = 0; i <= position; i++) {
//			if (i == position && position != 0) {
//				absolutePath.append(paths[i]);
//			} else if (i == 0 && i != position) {
//				absolutePath.append(rootPath + File.separator);
//			} else if (i == position && position == 0) {
//				absolutePath.append(rootPath);
//			} else {
//				absolutePath.append(paths[i] + File.separator);
//			}
//		}
        LogUtils.d("SNS", "this is mApplication.mCurrentPath"
                + mApplication.mCurrentPath);
        if (position != paths.length - 1) {
            mApplication.mCurrentPath = absolutePath.toString();
//            mActivityListener.showHideToolbar();//PR 1488305 zibin.wang 2016.01.21
            refreshPathAdapter(mApplication.mCurrentPath);
            hideEditWindow();
        }
        updateBarTitle(); // MODIFIED by songlin.qi, 2016-06-01,BUG-2231078
    }

    private StringBuilder getAbsolutePath(int position) {
        StringBuilder absolutePath = new StringBuilder();
        String rootPath = mMountPointManager.getPhonePath();
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }

        if (mTagMode == PHONE_TAG) {
            rootPath = mMountPointManager.getPhonePath();
        } else if (mTagMode == SDCARD_TAG) {
            rootPath = mMountPointManager.getSDCardPath();
        } else if (mTagMode == USBOTG_TAG) {
            rootPath = mMountPointManager.getUsbOtgPath();
        /* MODIFIED-BEGIN by wenjing.ni, 2016-05-11,BUG-1938835*/
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-08,BUG-2278011*/
        } else if (mTagMode == GLOBAL_SEARCH) {
            /* MODIFIED-BEGIN by songlin.qi, 2016-06-05,BUG-2243459*/
            String descriptionPath = mMountPointManager.getDescriptionPath(mApplication.mCurrentPath);
            if (descriptionPath != null) {
                String[] temp = descriptionPath.split(File.separator);
                if (temp[0].equals(getResources().getString(R.string.phone_storage_cn))) {
                    rootPath = mMountPointManager.getPhonePath();
                } else if (temp[0].equals(getResources().getString(R.string.usbotg_m))) {
                /* MODIFIED-END by songlin.qi,BUG-2278011*/
                    rootPath = mMountPointManager.getUsbOtgPath();
                } else if (temp[0].equals(getResources().getString(R.string.sd_card))) {
                    rootPath = mMountPointManager.getSDCardPath();
                }
                /* MODIFIED-END by songlin.qi,BUG-2243459*/
            }
        }
        /* MODIFIED-END by wenjing.ni,BUG-1938835*/
        for (int i = 0; i <= position; i++) {
            if (i == position && position != 0) {
                absolutePath.append(paths[i]);
            } else if (i == 0 && i != position) {
                absolutePath.append(rootPath + File.separator);
            } else if (i == position && position == 0) {
                absolutePath.append(rootPath);
            } else {
                absolutePath.append(paths[i] + File.separator);
            }
        }
//			LogUtils.d("SNS", "this is mApplication.mCurrentPath"
//					+ mApplication.mCurrentPath);
        return absolutePath;
    }

    public void hideBottomBar(){
        mBottomNavigationView.setVisibility(View.GONE);
        mMoreBottomView.setVisibility(View.GONE);
    }

    /**
     * This method shows the undo button.
     */
    private void showUndoButton(String profileName) {
        snackbarLayout.setVisibility(View.VISIBLE);
        StringBuilder builder = new StringBuilder();
        builder.append(profileName);
        snackTextView.setText(builder.toString());
    }

    Runnable popupWindowControlRunnable = new Runnable() {
        public void run() {
            if (snackbarLayout.getVisibility() == View.VISIBLE) {
                snackbarLayout.setVisibility(View.GONE);
                mBuilderTemp.create().dismiss();
                if (mActivitytoCategoryListener != null) {
                    mActivitytoCategoryListener.refreshCategory();
                }
            }
        }
    };

    public void HideActionbar(boolean flag) {
//        if (!mPortraitOrientation && mFileMode != FILE_MODE_EDIT) {
//            if (flag) {
//                getActionBar().hide();
//            } else {
//                if (mCurrentFragment != mPermissionFragment) {
//                    getActionBar().show();
//                }
//            }
//        }
    }

    private OnEditorActionListener lEditorActionListener = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideGlobalSearchViewInputMethod();
                //GlobalSearchData(mGlobalSearchView.getText().toString().toUpperCase());
            }
            return false;
        }
    };
    /**
     * Dynamic search
     */

    private SearchViewEX.OnQueryTextListener tbxSearch_TextChanged = new SearchViewEX.OnQueryTextListener() {

        @Override
        public boolean onQueryTextSubmit(String query) {
            mApplication.mFileInfoManager.getSearchFileList().clear();
            isSearching = true;
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {

            if (TextUtils.isEmpty(newText)) {
                if (mActivityListener != null) {
                    mActivityListener.clearAdapter();
                }
                //return;
            }
            mQueryText = newText.toString();

            if (TextUtils.isEmpty(newText.toString())) {
                if (isSearchMode) {
                    if (mApplication.mService != null) {
                        if (mApplication.mService.isBusy(getClass().getName())) {
                            mApplication.mService.cancel(getClass().getName());
                        }
                    }
                    // ADD START FOR PR1044990 BY HONGBIN.CHEN 20150721
                    //mActivityListener.showBeforeSearchList();
                    if (mActivityListener != null) {
                        mActivityListener.showNoSearchResults(false, null);
                    }
                    // ADD END FOR PR1044990 BY HONGBIN.CHEN 20150721
                }
                return true;
            }
            if (mActivityListener != null) {
                mActivityListener.clearAdapter();
            }
            String regx = ".*[/\\\\:*?\"<>|].*";
            Pattern p = Pattern.compile(regx);
            Matcher m = p.matcher(newText.toString());
            if (m.find()) {
                mToastHelper.showToast(R.string.invalid_char_prompt);
                return true;
            }
            requestSearchFiles(newText.toString()); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1913721
            return true;
        }

//        @Override
//        public void afterTextChanged(Editable s) {
//
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count,
//                                      int after) {
//            mApplication.mFileInfoManager.getSearchFileList().clear();
//            isSearching = true;
//        }
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before,
//                                  int count) {
//
//        }
    };


    private void requestSearchFiles(String query) { //MODIFIED by haifeng.tang, 2016-04-09,BUG-1913721
        String mPhonePathString = mMountPointManager.getPhonePath();
        String mSDcardPathString = mMountPointManager.getSDCardPath();
        String mUsbPathString = mMountPointManager.getUsbOtgPath();
        if (mGlobalSearchView != null) {
            if (mApplication.mService != null) {
                mApplication.mService.Globalsearch(this.getClass().getName(),
                        query, mPhonePathString, mSDcardPathString, mUsbPathString, new SearchListener(query));
            }
        }
    }

    public static boolean isMorePopShow() {
        if (morePop != null) {
            return morePop.isShowing();
        }
        return false;
    }

    @Override
    public void isDeleteFlag(boolean flag) {
        if (flag) {
            deleteFlag = false;
        } else {
            deleteFlag = true;
        }
    }


    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-27,BUG-1991729*/
    public void requestPermission() {
        boolean isEnterPermission = false;
        Intent intent;
        try {
            // Goto setting application permission
            intent = new Intent(PermissionFragment.MANAGE_PERMISSIONS);
            intent.putExtra(PermissionFragment.PACKAGE_NAME, getPackageName());
            startActivityForResult(intent, PermissionUtil.JUMPTOSETTINGFORSTORAGE);
        } catch (Exception e) {
            isEnterPermission = true;
        }
        if (isEnterPermission) {
            // Goto settings details
            Uri packageURI = Uri.parse("package:" + getPackageName());
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
            startActivityForResult(intent, PermissionUtil.JUMPTOSETTINGFORSTORAGE);
        }
    }
    /* MODIFIED-END by haifeng.tang,BUG-1991729*/

    /*PR 850749 zibin.wang add start*/
    @Override
    public void reSearch() {
        /* PR 905269 zibin.wang 2015.11.19 start */
        if (mQueryText == null) {
            return;
        }
        if (mFileMode == FILE_MODE_SEARCH
                && CategoryManager.mCurrentMode != CategoryManager.CATEGORY_MODE
                && mApplication.currentOperation !=
                FileManagerApplication.RENAME) {
            /* PR 905269 zibin.wang 2015.11.19 end */
            /* MODIFIED-BEGIN by songlin.qi, 2016-06-12,BUG-2280817*/
            mGlobalSearchView.setQuery(mQueryText, false);
            requestSearch(mQueryText);
        } else if (mApplication.currentOperation != FileManagerApplication.RENAME) {
            refreshPathAdapter(mApplication.mCurrentPath);
            mSearchFromEdit = true;
            mGlobalSearchView.setQuery(mQueryText, false);
            /* MODIFIED-END by songlin.qi,BUG-2280817*/
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO: handle exception
            }
            requestSearch(mQueryText);
        }
        if (adapter != null && mApplication.currentOperation == FileManagerApplication.RENAME
                && mFilePathLayout.isShown()) {
            adapter.notifyDataSetChanged();
        }
    }
    /*PR 850749 zibin.wang add end*/

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT == requestCode) {
            PermissionUtil.isShowPermissionDialog = false;
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    if (CommonUtils.hasM() && mCurrentFragment == mPermissionFragment) {
                        if (!PermissionUtil.isSecondRequestPermission(this)) {
                            mPermissionFragment.updateView(1);
                        }
                        PermissionUtil.setSecondRequestPermission(this);
                    } else {
                        mApplication.mCurrentPath = null;
                        finish();
                    }

                } else {
                    if (CommonUtils.hasM() && mCurrentFragment == mPermissionFragment) {
                        if (mPortraitOrientation) {
                            switchContent(mCategoryFragment);
                        }
//                        else {
//                            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//                            fragmentTransaction.hide(mCurrentFragment);
////                            mLayout.setVisibility(View.VISIBLE);
//                            fragmentTransaction.show(mCategoryLandFragment)
//                                    .commitAllowingStateLoss();
//                            mCurrentFragment = mCategoryLandFragment;
//                        }
                        CategoryManager.setCurrentMode(CategoryManager.CATEGORY_MODE);
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { // MODIFIED by haifeng.tang, 2016-04-26,BUG-1989911
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.d("RESU", "this is resultcode" + resultCode + PermissionUtil.isAllowPermission(this));
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                LogUtils.e(TAG,"onActivityResult.....resultCode");
                mApplication.mCurrentPath = SafeUtils.getEncryptRootPath(this);
                CategoryManager.mCurrentCagegory = CategoryManager.CATEGORY_SAFE;
                startFileBrowerActivity();
            }
        }
        if (requestCode == 100) {
            if (resultCode == RESULT_OK && mApplication.mIsSafeMove) {
                //todo  Encrypt
                /** encrypt file */
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }
                if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
                    mActivityListener.clickDecryptBtn();
                } else {
                    mActivityListener.clickEncryptBtn();
                }
            }else if(resultCode == RESULT_OK && mApplication.mIsCategorySafe){
                //todo Welcome And Encrypt
                if(SharedPreferenceUtils.isFristEnterSafe(CategoryActivity.this)){
                    //todo  Encrypt
                    mApplication.mCurrentPath = SafeUtils.getEncryptRootPath(this);
                    CategoryManager.mCurrentCagegory = CategoryManager.CATEGORY_SAFE;
//                    updateCategoryNormalBarView();
//                    switchCategoryList();
                    startFileBrowerActivity();
                } else {
                    //todo Welcome
                    Intent enterCateEncryptIntent = new Intent();
                    SharedPreferenceUtils.setFristEnterSafe(CategoryActivity.this, true);
                    enterCateEncryptIntent.putExtra(FileEncryptWelcomeActivity.STATUS_KEY, FileEncryptWelcomeActivity.CATEGORY_STATUS);
                    enterCateEncryptIntent.setClass(CategoryActivity.this, FileEncryptWelcomeActivity.class);
                    startActivityForResult(enterCateEncryptIntent, 101);
                }
            }else{
                LogUtils.e(TAG,"CategoryActivity backpress now.......");
                onBackPressed();
            }
        } else {
            if (requestCode == PermissionUtil.JUMPTOSETTINGFORSTORAGE) {
                if (!PermissionUtil.isAllowPermission(this)) {
                    if (CommonUtils.hasM()) {
                        //mFragmentContent.setPadding(0, (int) getResources().getDimension(R.dimen.main_frame_padding_top), 0, 0);
                        //getActionBar().show();
                        if (mPortraitOrientation) {
                            switchContent(mCategoryFragment);
                        }
//                else {
//                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//                    fragmentTransaction.hide(mCurrentFragment);
////                    mLayout.setVisibility(View.VISIBLE);
//                    fragmentTransaction.show(mCategoryLandFragment)
//                            .commitAllowingStateLoss();
//                    mCurrentFragment = mCategoryLandFragment;
//                }
                        CategoryManager.setCurrentMode(CategoryManager.CATEGORY_MODE);
                        // switchContent(mCategoryFragment);
                    }
                }
            }
            // Return to the memory select state for operation
            if (requestCode == STORAGE_SELECT_CODE && resultCode == STORAGE_SELECT_CODE) {
                String mSelectTag = data.getExtras().getString(STORAGE_SELECT_TAG);
                mApplication.mCurrentPath = null;
                if (TextUtils.equals(mSelectTag, PHONE_TAG)
                        || TextUtils.equals(mSelectTag, SDCARD_TAG)
                        || TextUtils.equals(mSelectTag, USBOTG_TAG)) {
                    changePrefCurrTag(mSelectTag);
                    startFileBrowerActivity();
                    return;
                }
            }
        }
    }

    public void switchShortcutList(String path) {
        try {
            if (path != null && mMountPointManager != null) {
                if (mMountPointManager.getPhonePath() != null
                        && path.startsWith(mMountPointManager.getPhonePath())) {
                    changePrefCurrTag(PHONE_TAG);
                    // updateFragment(PHONE_TAG);
                } else if (mMountPointManager.getSDCardPath() != null
                        && path.startsWith(mMountPointManager.getSDCardPath())) {
                    changePrefCurrTag(SDCARD_TAG);
                    // updateFragment(SDCARD_TAG);
                } else if (mMountPointManager.getUsbOtgPath() != null
                        && path.startsWith(mMountPointManager.getUsbOtgPath())) {
                    changePrefCurrTag(USBOTG_TAG);
                    // updateFragment(USBOTG_TAG);
                }
            }
        } catch (Exception e) {

        }
    }

    public void switchShortcutFragment(String path) {
        try {
            if (path != null && mMountPointManager != null) {
                if (mMountPointManager.getPhonePath() != null
                        && path.startsWith(mMountPointManager.getPhonePath())) {
                    // changePrefCurrTag(PHONE_TAG);
                    updateFragment(PHONE_TAG);
                } else if (mMountPointManager.getSDCardPath() != null
                        && path.startsWith(mMountPointManager.getSDCardPath())) {
                    // changePrefCurrTag(SDCARD_TAG);
                    updateFragment(SDCARD_TAG);
                } else if (mMountPointManager.getUsbOtgPath() != null
                        && path.startsWith(mMountPointManager.getUsbOtgPath())) {
                    // changePrefCurrTag(USBOTG_TAG);
                    updateFragment(USBOTG_TAG);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtils.d("LAU", "this is Onnew Intent is" + intent.getStringExtra("foldername"));
        laucherFolderName = intent.getStringExtra("foldername");
        boolean isShowHidden = SharedPreferenceUtils.isShowHidden(this);
        // PR-1077564,1070907 Nicky Ni -001 20151209 start
        if (laucherFolderName != null) {
            File launcherFile = new File(laucherFolderName);
            if ((!launcherFile.exists()) || (!isShowHidden && launcherFile.isHidden())) {// PR-1175531 Nicky Ni -001 20151219
                laucherFolderName = null;
                mToastHelper.showToast(R.string.shortcut_no_exist);
            } else if (isPathInvalid(laucherFolderName)) { //[BUG-FIX] by NJTS Junyong.Sun 2016/01/20 PR-1401197
                LogUtils.d("LAU", "updateFragment-->CATEGORY_TAG");
                laucherFolderName = null;
                updateFragment(CATEGORY_TAG);
                changePrefCurrTag(CATEGORY_TAG);
                CategoryManager.setCurrentMode(CategoryManager.CATEGORY_MODE);
                mToastHelper.showToast(R.string.shortcut_no_exist);
            } else {// PR-1077564,1070907 Nicky Ni -001 20151209 end
                // PR-1157541 Nicky Ni -001 20151214 start
                if (mApplication.mCurrentPath != null && !mApplication.mCurrentPath.equals(laucherFolderName)) {
                    if (mActivityListener != null) {
                        mActivityListener.closeItemMorePop();
                    }
                    if (morePop != null && morePop.isShowing()) {
                        morePop.dismiss();
                    }
                    //[Bug-Fix-BEGIN] PR-1202681 TSNJ Junyong.Sun 2016/01/06
                    if (LIST_MODE.equals(getPrefsViewBy())) {
                        mListFragment.clearAdapter();
                    } else {
                        mGridFragment.clearAdapter();
                    }
                    //[Bug-Fix-END] PR-1202681 TSNJ Junyong.Sun 2016/01/06
                }
                if (mActivityListener != null) {
                    mActivityListener.clickShortcutToNormal();// PR-1159016 Nicky Ni -001 20151215
                }
                // PR-1157541 Nicky Ni -001 20151214 end
                switchShortcutFragment(laucherFolderName);
                mApplication.mCurrentPath = laucherFolderName;
                refreshPathAdapter(mApplication.mCurrentPath);
//                mActivityListener.showHideToolbar();/*PR 1356162 zibin.wang add 20160107*/
            }
        } else {// PR-1530854 haifeng.tang@tcl.com  20160128 START
            if (isCategoryFragment()) {
//                mActivityListener.showHideToolbar();
            }// PR-1530854 haifeng.tang@tcl.com  20160128 END
        }
    }

    /*PR 1047161 zibin.wang add Start*/
    @Override
    public void LandShowSize(PathProgressLayout mPhoneSize, PathProgressLayout mSDSize, PathProgressLayout mExternalSize) {
//        mMountManager = MountManager.getInstance();
//        if (mMountManager.isOtgMounted() && mMountManager.isSDCardMounted()&&!mPortraitOrientation) {
//            mPhoneSize.setVisibility(View.GONE);
//            mSDSize.setVisibility(View.GONE);
//            mExternalSize.setVisibility(View.GONE);
//        }else if(mMountManager.isOtgMounted() || mMountManager.isSDCardMounted()){
//            mPhoneSize.setVisibility(View.VISIBLE);
//            mSDSize.setVisibility(View.VISIBLE);
//            mExternalSize.setVisibility(View.VISIBLE);
//        }
    }
    /*PR 1047161 zibin.wang add End*/

    public int getSlideLimite() {

        if (mPortraitOrientation) {
            if (getPrefsViewBy().equals(GRID_MODE)) {
                return 12;
            } else {
                return getListPortLimita();
            }
        } else {
            if (getPrefsViewBy().equals(GRID_MODE)) {
                return 10;
            } else {
                return getListLandLimita();
            }
        }
    }

    /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2127786*/
    @Override
    public void refreashSafeFilesCategory() {

    }
    /* MODIFIED-END by wenjing.ni,BUG-2127786*/

    private int getListLandLimita() {
        int count = 0;
        if (CommonUtils.getScreenWidth(this) <= 800) {
            count = 3;
        } else {
            count = 4;
        }
        return count;
    }

    private int getListPortLimita() {
        int count = 0;
        if (CommonUtils.getScreenWidth(this) <= 480) {
            count = 6;
        } else {
            count = 7;
        }
        return count;
    }

    /* MODIFIED-BEGIN by songlin.qi, 2016-05-30,BUG-2220937*/
    private int getPasteCount() {
        if (mApplication == null || mApplication.mFileInfoManager == null) {
            return 0;
        }
        return mApplication.mFileInfoManager.getPasteCount();
    }
    /* MODIFIED-END by songlin.qi,BUG-2220937*/

    public interface HideInputMethod {
        public void hideInputMethod();
    }

    public class HideInputMethodListener implements HideInputMethod {
        public void hideInputMethod() {
            hideGlobalSearchViewInputMethod();
            mGlobalSearchView.clearFocus();
        }
    }

    private  void startFileBrowerActivity() {
        Intent intent = null;
        intent = new Intent(this, FileBrowserActivity.class);
        startActivity(intent);
    }
}
