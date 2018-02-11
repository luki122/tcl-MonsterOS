/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.activity;

import android.Manifest;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.LinkAddress;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MstSearchView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.IActivityListener;
import cn.tcl.filemanager.MountReceiver;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.fragment.CategoryFragment;
import cn.tcl.filemanager.fragment.FileBrowserFragment;
import cn.tcl.filemanager.fragment.GridFragment;
import cn.tcl.filemanager.fragment.ListsFragment;
import cn.tcl.filemanager.fragment.PermissionFragment;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.manager.SafeManager;
import cn.tcl.filemanager.service.FileManagerService;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.PermissionUtil;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;
import cn.tcl.filemanager.utils.ToastHelper;
import cn.tcl.filemanager.utils.ViewUtil;
import cn.tcl.filemanager.view.SearchViewEX;
import mst.app.dialog.AlertDialog;
import mst.view.menu.MstMenuView;
import mst.view.menu.bottomnavigation.BottomNavigationView;

//import com.jrdcom.filemanager.fragment.CategoryLandFragment;
//import FavoriteManager;
//import android.support.v4.content.PermissionChecker;

public class FileBaseActivity extends FileBaseActionbarActivity implements
        MountReceiver.MountListener, View.OnClickListener, MstSearchView.OnQueryTextListener,
        MstSearchView.OnCloseListener, PopupMenu.OnMenuItemClickListener {

    private static final String PREF_FILE_NAME = "FileBaseActivity";
    private static final String PREF_SORT_BY = "pref_sort_by";
    private static final String PREF_VIEW_BY = "pref_view_by";
    public static final String PREF_CURR_TAG = "curr_tag";
    private static final String CATEGORY_TAG = "category";
    private static final String PERMISSION_TAG = "permissions";

    public static final int FILE_MODE_INVALID = -1;
    public static final int FILE_MODE_NORMAL = 0;
    public static final int FILE_MODE_EDIT = 1;
    public static final int FILE_MODE_SEARCH = 2;
    public static final int FILE_MODE_GLOBALSEARCH = 3;
    public static final int FILE_COPY_NORMAL = 4;
    public static final int FILE_ADD_ENCRYPT = 5;
    public static final int FILE_ADD_DECRYPT = 6;
    private static final String TAG = FileBaseActivity.class.getSimpleName();
    protected static boolean IS_ITEM_MORE = false;

    public  static  final  String EXTRA_SAFE_CATEGORY="com.jrdcom.filemanager.SAFE_CATEGORY";

    protected ToastHelper mToastHelper;
    protected LinearLayout mNormalBar;
    protected LinearLayout mSearchBar;
    protected SearchViewEX mGlobalSearchView;
    protected ImageView mGlobalSearchCancel;
    protected ImageView mGlobalSearchBtn;
    protected SearchViewEX mSearchView;
    protected int mFileMode = FILE_MODE_NORMAL;
    protected String mSearchPath;
    protected boolean isSearching;
    protected int drawerClickPosition;
    protected int drawerNowPosition;
    protected boolean isSearchingDone;


    /**
     * normal menu
     */

    protected MenuItem mMenuSelect;
    protected MenuItem mMenuCreateFolder;
    protected MenuItem mMenuAddEncryptFile;

    protected PopupWindow mPopupWindow;

    protected ImageView mBtnSort;
    protected ImageView mBtnShare;
    protected ImageView mBtnDelete;
    protected ImageView mBtnMore;
    protected ImageView mBtnSearch;
    protected View floatingActionButtonContainer;
    protected ImageButton floatingActionButton;
    protected TextView mActionBarPathText;
    protected TextView mActionBarEditPathText;
    protected ImageView mBtnEditBack;
    protected ImageView mSearchBack;
    protected ActionBar mActionBar;
    protected FragmentTransaction mFragmentTransaction;
    protected ListsFragment mListFragment;
    protected GridFragment mGridFragment;
    protected CategoryFragment mCategoryFragment;
    protected PermissionFragment mPermissionFragment;
    protected Fragment mCurrentFragment;
    protected FileManagerApplication mApplication;
    protected MountManager mMountPointManager;
    protected int selectCount = 0;
    protected static final String LIST_MODE = "listMode";
    protected static final String GRID_MODE = "gridMode";
    protected IActivityListener mActivityListener;
    protected boolean mIsHasDir;
    protected boolean mIsHasDrm;
    protected boolean mCanShare;

    private MountReceiver mMountReceiver;
    protected int mWindowWidth = -1; // MODIFIED by wenjing.ni, 2016-05-11,BUG-2125384

    protected ImageView mDeleteImage;
    protected ImageView mCopyImage;
    protected ImageView mCutImage;
    protected ImageView mShareImage;
    protected ImageView mMoreImage;
    protected ImageView mPasteImage;
    protected ImageView mCancelImage;
    protected RelativeLayout mEditLayout;
    protected Button mDetailsBtn;
    protected Button mRenameBtn;
    protected Button mMoveToSafeBtn;

    protected MenuItem mEncryptCancelView;
    protected MenuItem mEncryptOkView;

    protected BottomNavigationView mBottomNavigationView;
    protected BottomNavigationView mBottomPasteView;
    protected BottomNavigationView mBottomAddCancleView;
    protected LinearLayout mMoreBottomView;
    protected LinearLayout mDeleteBottomView;
    protected LinearLayout mCopyBottomView;
    protected LinearLayout mCutBottomView;
    protected LinearLayout mShareBottomView;
    protected LinearLayout mPasteBottomView;
    protected LinearLayout mCancleBottomView;

    protected MenuItem mEncryptBottomView;
    protected MenuItem mRenameBottomView;
    protected MenuItem mDetailBottomView;
    protected MenuItem mDecryptBottomView;

    protected LinearLayout snackbarLayout;
    protected TextView snackTextView;
    protected ImageView mGlobalView;
    protected RelativeLayout mainLayout;
    protected String laucherFolderName;
    protected RelativeLayout mFragmentContent;
    protected String mQueryText; //MODIFIED by haifeng.tang, 2016-04-13,BUG-1938948
    protected boolean isSettingsEnter = false; // MODIFIED by wenjing.ni, 2016-05-04,BUG-802835

    private View mView;

    protected static HashMap<String, String[]> permissionMap = new HashMap<String, String[]>();

    static {
        //TODO:Define Activity class and permission here
        permissionMap.put(FileBrowserActivity.class.getName(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
        permissionMap.put(FileSelectionActivity.class.getName(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
        permissionMap.put(PathSelectionActivity.class.getName(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.getAppInfo(this);
//        if (PermissionUtil.isAllowPermission(this) && CommonUtils.hasM()) {
//            getSupportActionBar().hide();
//        }
        boolean isShowHidden = SharedPreferenceUtils.isShowHidden(this);
        mToastHelper = new ToastHelper(this);
        mPortraitOrientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        //mFragmentManager = getSupportFragmentManager();
        mApplication = (FileManagerApplication) getApplicationContext();
        MountManager.getInstance().init(mApplication);
        mMountPointManager = MountManager.getInstance();
//        mApplication.mShortcutManager = new ShortCutManager(this); // MODIFIED by haifeng.tang, 2016-04-25,BUG-1989942
        //mApplication.mFavoriteManager = new FavoriteManager(this);
        //mApplication.mFavoriteManager.queryfavoriteFile();
        mMountReceiver = MountReceiver.registerMountReceiver(this);
        mMountReceiver.registerMountListener(this);
        mApplication.mSortType = getPrefsSortBy();
        Intent intent = getIntent();
        if (intent != null) {
            laucherFolderName = intent.getStringExtra("foldername");
            isSettingsEnter = intent.getBooleanExtra("from_settings",false); // MODIFIED by wenjing.ni, 2016-05-04,BUG-802835
            // PR-1077564,1070907 Nicky Ni -001 20151209 start
            if (laucherFolderName != null) {
                File launcherFile = new File(laucherFolderName);
                if (!launcherFile.exists() || (!isShowHidden && launcherFile.isHidden())// PR-1175531 Nicky Ni -001 20151219
                        || isPathInvalid(laucherFolderName)) { //[BUG-FIX] by NJTS Junyong.Sun 2016/01/20 PR-1401197
                    laucherFolderName = null;
                    mToastHelper.showToast(R.string.shortcut_no_exist);
                }
            }
            // PR-1077564,1070907 Nicky Ni -001 20151209 end
        }
        final String className = this.getClass().getName();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Log.d("RUN", "this is enter runable" + className);
                if (permissionMap.get(className) != null) {
                    String[] permissions = permissionMap.get(className);
                    PermissionUtil.checkAndRequestPermissions(FileBaseActivity.this, permissions,
                            PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT);
                    Log.d("RUN", "this is enter runable");
                }
            }
        }, 1000);

        bindService(new Intent(getApplicationContext(),
                FileManagerService.class), mServiceConnection, BIND_AUTO_CREATE);




    }

    @Override
    public void onRestart() {
        super.onRestart();
        final String className = this.getClass().getName();
        Log.d("RUN", "this is enter runable" + className);
        if (!PermissionUtil.isShowPermissionDialog && permissionMap.get(className) != null && mCurrentFragment != mPermissionFragment) {
            String[] permissions = permissionMap.get(className);
            PermissionUtil.checkAndRequestPermissions(FileBaseActivity.this, permissions,
                    PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT);
            Log.d("RUN", "this is enter runable");
        }

        // init FileInfoManager when restart activity, avoid FileInfoManager is not associated with the activity
        initFileInfoManager();
    }

    public void initFileInfoManager() {
        if (mApplication != null && mApplication.mService != null) {
            mApplication.mFileInfoManager = mApplication.mService
                    .initFileInfoManager(this);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            View view = this.getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileBrowserActivity.comparepath = "";

        LogUtils.i(TAG, "onDestroy");
        LogUtils.i(TAG,"SafeManager.mCurrentmode->" + SafeManager.mCurrentmode);

        if (!getIntent().getBooleanExtra(EXTRA_SAFE_CATEGORY,false)){
            CategoryManager.mCurrentCagegory = -1;
        }
        if (mApplication != null && mApplication.mService != null) {
            unbindService(mServiceConnection);
            stopService(new Intent(this, FileManagerService.class));
        }
        unregisterReceiver(mMountReceiver);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mApplication.mService.disconnected(FileBaseActivity.this.getClass()
                    .getName());
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mApplication.mService = ((FileManagerService.ServiceBinder) service)
                    .getServiceInstance();
            serviceConnected();
//			FileInfo.setService(mApplication.mService);// add for PR928303 by
//														// yane.wang@jrdcom.com
//														// 20150210
        }

    };

    @Override
    public void onMounted() {
        MountManager.getInstance().init(this);
        /* MODIFIED-BEGIN by wenjing.ni, 2016-05-04,BUG-802835*/
    }

    @Override
    public void onUnmounted(String mountPoint) {
        MountManager.getInstance().init(this);
        /* MODIFIED-END by wenjing.ni,BUG-802835*/
        if (mApplication.mFileInfoManager != null
                && mApplication.mFileInfoManager.getPasteCount() > 0) {
            FileInfo fileInfo = mApplication.mFileInfoManager.getPasteList()
                    .get(0);
            if (fileInfo.getFileAbsolutePath().startsWith(
                    mountPoint + MountManager.SEPARATOR)) {
                mApplication.mFileInfoManager.clearPasteList();
            }
        }
    }

    @Override
    public void onEject() {
    }

    protected void showToastForUnmountCurrentSDCard(String path) {
        String mountPointDescription = MountManager.getInstance()
                .getDescriptionPath(path);
        if (MountManager.getInstance().isExternalMountPath(path)) {// add for
            // PR863716
            // by
            // yane.wang@jrdcom.com
            // 20141208
            mToastHelper.showToast(getString(R.string.unmounted,
                    mountPointDescription));
        }
    }

//	protected void initSortMenu(Menu menu) {
//		if (mApplication.mSortType == FileInfoComparator.SORT_BY_TYPE) {
//			menu.findItem(R.id.sort_type).setEnabled(false);
//			menu.findItem(R.id.sort_name).setEnabled(true);
//			menu.findItem(R.id.sort_size).setEnabled(true);
//			menu.findItem(R.id.sort_time).setEnabled(true);
//		} else if (mApplication.mSortType == FileInfoComparator.SORT_BY_NAME) {
//			menu.findItem(R.id.sort_type).setEnabled(true);
//			menu.findItem(R.id.sort_name).setEnabled(false);
//			menu.findItem(R.id.sort_size).setEnabled(true);
//			menu.findItem(R.id.sort_time).setEnabled(true);
//		} else if (mApplication.mSortType == FileInfoComparator.SORT_BY_SIZE) {
//			menu.findItem(R.id.sort_type).setEnabled(true);
//			menu.findItem(R.id.sort_name).setEnabled(true);
//			menu.findItem(R.id.sort_size).setEnabled(false);
//			menu.findItem(R.id.sort_time).setEnabled(true);
//		} else if (mApplication.mSortType == FileInfoComparator.SORT_BY_TIME) {
//			menu.findItem(R.id.sort_type).setEnabled(true);
//			menu.findItem(R.id.sort_name).setEnabled(true);
//			menu.findItem(R.id.sort_size).setEnabled(true);
//			menu.findItem(R.id.sort_time).setEnabled(false);
//		}
//	}

    protected boolean isCategoryFragment() {
        return mCurrentFragment == mCategoryFragment ;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    protected void initPopMenu(Menu menu) {
        if (mFileMode == FILE_MODE_NORMAL) {
            menu.findItem(R.id.select).setVisible(true);
        } else if (mFileMode == FILE_MODE_EDIT) {
            menu.findItem(R.id.select).setVisible(false);
        }
    }

    public void initEditWindow() {
        mDeleteBottomView = (LinearLayout)findViewById(R.id.delete_item);
        mCopyBottomView = (LinearLayout)findViewById(R.id.copy_item);
        mCutBottomView = (LinearLayout)findViewById(R.id.cut_item);
        mShareBottomView = (LinearLayout)findViewById(R.id.share_item);
        mMoreBottomView = (LinearLayout)findViewById(R.id.more_item);
        mPasteBottomView = (LinearLayout)findViewById(R.id.pastes_item);
        mCancleBottomView = (LinearLayout)findViewById(R.id.cancles_item);
        mBottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation_view);
        mBottomPasteView = (BottomNavigationView)findViewById(R.id.bottom_paste_cancle);
        mBottomAddCancleView = (BottomNavigationView)findViewById(R.id.bottom_add_cancle);
        mEncryptCancelView = mBottomAddCancleView.getMenu().findItem(R.id.encrypt_cancel);
        mEncryptOkView = mBottomAddCancleView.getMenu().findItem(R.id.encrypt_ok);
    }

    /**
     * Bottom edit of normal state
     */
    public void showEditWindow() {
        if (null != mBottomNavigationView) {
            mBottomAddCancleView.setVisibility(View.GONE);
            mBottomNavigationView.setVisibility(View.VISIBLE);
            mDeleteBottomView.setVisibility(View.VISIBLE);
            mCopyBottomView.setVisibility(View.VISIBLE);
            mCutBottomView.setVisibility(View.VISIBLE);
            mShareBottomView.setVisibility(View.VISIBLE);
            mMoreBottomView.setVisibility(View.VISIBLE);
            mPasteBottomView.setVisibility(View.GONE);
            mBottomPasteView.setVisibility(View.GONE);
            mCancleBottomView.setVisibility(View.GONE);

            mBottomNavigationView.setItemEnable(R.id.delete_item, false);
            mBottomNavigationView.setItemEnable(R.id.copy_item, false);
            mBottomNavigationView.setItemEnable(R.id.cut_item, false);
            mBottomNavigationView.setItemEnable(R.id.share_item, false);
            mBottomNavigationView.setItemEnable(R.id.more_item, false);
        }
    }
    /**
     * Bottom edit of picture category state
     */
    public void showEditPictureWindow() {
        mBottomNavigationView.setVisibility(View.VISIBLE);
        mBottomPasteView.setVisibility(View.GONE);
        mDeleteBottomView.setVisibility(View.VISIBLE);
        mCopyBottomView.setVisibility(View.GONE);
        mCutBottomView.setVisibility(View.GONE);
        mShareBottomView.setVisibility(View.GONE);
        mMoreBottomView.setVisibility(View.GONE);
        mPasteBottomView.setVisibility(View.GONE);
        mCancleBottomView.setVisibility(View.GONE);
    }

    public void showEncryptWindow() {
        if (null != mBottomAddCancleView) {
            mBottomNavigationView.setVisibility(View.GONE);
            mDeleteBottomView.setVisibility(View.GONE);
            mCopyBottomView.setVisibility(View.GONE);
            mCutBottomView.setVisibility(View.GONE);
            mShareBottomView.setVisibility(View.GONE);
            mMoreBottomView.setVisibility(View.GONE);
            mPasteBottomView.setVisibility(View.GONE);
            mCancleBottomView.setVisibility(View.GONE);

            mBottomAddCancleView.setVisibility(View.VISIBLE);
            mEncryptCancelView.setVisible(true);
            mEncryptOkView.setVisible(true);
            mEncryptOkView.setTitle(String.format(getString(R.string.encrypt_add_percent), 0));
        }
    }

    public void showDecryptWindow() {
        if (null != mBottomAddCancleView) {
            mBottomBarLayout.setVisibility(View.VISIBLE);
            mBottomNavigationView.setVisibility(View.GONE);
            mDeleteBottomView.setVisibility(View.GONE);
            mCopyBottomView.setVisibility(View.GONE);
            mCutBottomView.setVisibility(View.GONE);
            mShareBottomView.setVisibility(View.GONE);
            mMoreBottomView.setVisibility(View.GONE);
            mPasteBottomView.setVisibility(View.GONE);
            mCancleBottomView.setVisibility(View.GONE);

            mBottomAddCancleView.setVisibility(View.VISIBLE);
            mEncryptCancelView.setVisible(true);
            mEncryptOkView.setVisible(true);
            mEncryptOkView.setTitle(R.string.decrypt_button_title);
        }
    }

    public void dissMissPopWindow(){
        mPopupWindow.dismiss();
    }

    public void serviceConnected() {
        mApplication.mFileInfoManager = mApplication.mService
                .initFileInfoManager(this);
        mApplication.mCategoryManager = mApplication.mService
                .initCategoryManager(this);
        setMainContentView();
        getScreenSize();// add for PR940568 by yane.wang@jrdcom.com 20150305
        setPathWidth();// add for PR936416 by yane.wang@jrdcom.com 20150228
    }

    public void setMainContentView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customActionBarView = inflater.inflate(R.layout.actionbar, null);
        mFragmentContent = (RelativeLayout) findViewById(R.id.content_frame);
        initEditWindow();
        FragmentManager fragmentManager = getFragmentManager();
        if (customActionBarView != null) {

            mNormalBar = (LinearLayout) customActionBarView
                    .findViewById(R.id.normal_bar);
            mSearchBar = (LinearLayout) customActionBarView
                    .findViewById(R.id.search_bar);
            mSearchView = (SearchViewEX) customActionBarView
                    .findViewById(R.id.search_view);
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setOnCloseListener(this);
            mSearchView.setQuery(mQueryText, false);

            mBtnEditBack = (ImageView) customActionBarView
                    .findViewById(R.id.edit_back);
            mBtnEditBack.setOnClickListener(this);
            mSearchBack = (ImageView) customActionBarView
                    .findViewById(R.id.search_back);
            mSearchBack.setOnClickListener(this);
            mBtnSort = (ImageView) customActionBarView
                    .findViewById(R.id.sort_btn);
            mBtnShare = (ImageView) customActionBarView
                    .findViewById(R.id.share_btn);
            mBtnShare.setOnClickListener(this);
            mBtnDelete = (ImageView) customActionBarView
                    .findViewById(R.id.delete_btn);
            mBtnDelete.setOnClickListener(this);
            mBtnSort.setOnClickListener(this);
            mBtnMore = (ImageView) customActionBarView
                    .findViewById(R.id.more_btn);
            mBtnMore.setOnClickListener(this);
            mBtnSearch = (ImageView) customActionBarView
                    .findViewById(R.id.search_btn);
            mBtnSearch.setOnClickListener(this);

            mGlobalSearchBtn = (ImageView) customActionBarView
                    .findViewById(R.id.global_search_image);

            mGlobalSearchCancel = (ImageView) customActionBarView
                    .findViewById(R.id.global_search_cancel);
            mGlobalSearchCancel.setOnClickListener(this);
            mGlobalView = (ImageView) customActionBarView.findViewById(R.id.global_search_image);
            mGlobalView.setOnClickListener(this);
            floatingActionButtonContainer = findViewById(R.id.floating_action_button_container);
            ViewUtil.setupFloatingActionButton(
                    floatingActionButtonContainer, getResources());
            floatingActionButton = (ImageButton) findViewById(R.id.floating_action_button);
            snackbarLayout = (LinearLayout) findViewById(R.id.snackbarlayout);
            snackTextView = (TextView) findViewById(R.id.snackbarlayout_text);
            mainLayout = (RelativeLayout) findViewById(R.id.content_frame);
            floatingActionButton.setOnClickListener(this);
            floatingActionButtonContainer.setVisibility(View.GONE);
            floatingActionButton.setVisibility(View.GONE);
            mActionBarPathText = (TextView) customActionBarView
                    .findViewById(R.id.path_text);
            mActionBarEditPathText = (TextView) customActionBarView
                    .findViewById(R.id.edit_path_text);
//            mListFragment = (ListsFragment) fragmentManager
//                    .findFragmentById(R.id.listfragment);
//            mGridFragment = (GridFragment) fragmentManager
//                    .findFragmentById(R.id.gridfragment);
//            mCategoryFragment = (CategoryFragment) fragmentManager
//                    .findFragmentById(R.id.categoryfragment);
//            mPermissionFragment = (PermissionFragment) fragmentManager
//                    .findFragmentById(R.id.permissionfragment);
//            mFragmentTransaction = fragmentManager.beginTransaction();

            mListFragment = new ListsFragment(new DeleteFile());


            mGridFragment = new GridFragment();

            mCategoryFragment = new CategoryFragment();
            mPermissionFragment = new PermissionFragment();

            mFragmentTransaction = fragmentManager.beginTransaction();
            if (PermissionUtil.isAllowPermission(this) && CommonUtils.hasM()) {
            }

        }
    }

    /**
     * callback:delete of left slide
     */
    class DeleteFile implements deleteFileInfo{

        @Override
        public void deleteFile(FileInfo fileInfo) {
            if (null != fileInfo) {
                String deleteMessage;
                if (fileInfo.isDirectory()) {
                    deleteMessage = getString(R.string.delete_direct);
                } else {
                    deleteMessage = getString(R.string.delete_file);
                }
                AlertDialog alertDialog = new AlertDialog.Builder(FileBaseActivity.this).setMessage(
                        deleteMessage).setTitle(R.string.welcome_dialog_title).setPositiveButton(R.string.ok, new DeleteListener(fileInfo)).setNegativeButton(
                        R.string.cancel, new CancelListener()).show();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.negative_text_color));
                alertDialog.show();
            }
        }
    }

    /**
     * delete listener where confirm
     */
    private class DeleteListener implements DialogInterface.OnClickListener {
        private FileInfo fileInfo;
        public DeleteListener(FileInfo fileInfo){
            this.fileInfo = fileInfo;
        }
        @Override
        public void onClick(DialogInterface dialog, int id) {
            mActivityListener.deleteFileInfo(fileInfo);
        }
    }

    private class CancelListener implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            mActivityListener.clearChecked();
            mActivityListener.updateActionMode(mActivityListener.getActionMode());
        }
    }

    private int getLandWidth() {
        int width = -1;
        if (1700 <= mWindowWidth && mWindowWidth <= 1800) {// 1776
            width = 1140;
        } else if (1150 <= mWindowWidth && mWindowWidth <= 1350) {// 1280
            width = 810;
            mActionBarPathText.setTextSize(18);
        } else if (800 <= mWindowWidth && mWindowWidth <= 900) {// 854
            width = 540;
        } else if (480 == mWindowWidth) {
            width = 186;
        } else {
            width = 540;
        }
        return width;
    }

    // add for PR940568 by yane.wang@jrdcom.com 20150305 begin
    protected void getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        mWindowWidth = display.getWidth();
    }

    private int getPortWidth() {
        int width = -1;
        if (mWindowWidth == 1080) {
            width = 430;
        } else if (mWindowWidth == 720) {
            width = 330;
            mActionBarPathText.setTextSize(18);
        } else if (mWindowWidth == 480) {
            width = 194;
        } else if (mWindowWidth == 1440) {
            width = 530;
        } else if (mWindowWidth == 320) {
            width = 128;
        } else {
            width = 194;
        }
        return width;
    }

    protected void setPathWidth() {
        if (mActionBarPathText != null) {
            ViewGroup.LayoutParams lP = mActionBarPathText.getLayoutParams();
            if (mPortraitOrientation) {
                lP.width = getPortWidth();
                mActionBarPathText.setLayoutParams(lP);
            } else {
                lP.width = getLandWidth();
                mActionBarPathText.setLayoutParams(lP);
            }
        }
    }

    protected void updateCategoryNormalBar() {
        if (isCategoryFragment()) {
            // mActionBarPathText.setText(R.string.drawer_category);
            mActionBarPathText.setText(R.string.category_fragment_title);
        }
        if (CategoryManager.mCurrentCagegory != -1) {
            int s = CategoryManager
                    .getCategoryString(CategoryManager.mCurrentCagegory);
            if (s != 0) {
                mActionBarPathText.setText(getResources().getString(s));
            }
        }
    }


    public void updateCategoryNavigation(int id) {
    }

    protected void refreshPathAdapter(String path) {
    }

    protected boolean isFirstTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                "firstTimeEnterApp", Context.MODE_PRIVATE);
        return sp.getBoolean("firstTime", true);
    }

    protected void setNoFirstTime() {
        SharedPreferences sp = getSharedPreferences("firstTimeEnterApp",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("firstTime", false);
        editor.commit();
    }

    protected void changePrefsSortBy(int sort) {
        mApplication.mSortType = sort;
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putInt(PREF_SORT_BY, sort);
        editor.commit();
    }

    protected int getPrefsSortBy() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getInt(PREF_SORT_BY, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    protected boolean changePrefsShowHidenFile() {
        boolean hide = SharedPreferenceUtils.isShowHidden(this);
        SharedPreferenceUtils.setShowHidden(this, !hide);
        return hide;
    }

    // add for PR842401 by yane.wang@jrdcom.com 20141117 begin
    protected void changePrefViewBy(String viewMode) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString(PREF_VIEW_BY, viewMode);
        editor.commit();
    }

    protected String getPrefsViewBy() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getString(PREF_VIEW_BY, LIST_MODE);
    }

    // add for PR842401 by yane.wang@jrdcom.com 20141117 end
    protected void changePrefCurrTag(String currTag) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString(PREF_CURR_TAG, currTag);
        editor.commit();
    }

    protected String getPrefCurrTag() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getString(PREF_CURR_TAG, CATEGORY_TAG);
    }

    protected String getPermissionPrefCurrTag() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getString(PREF_CURR_TAG, PERMISSION_TAG);
    }

    protected int getFileMode() {
        return mFileMode;
    }

    protected void setFileMode(int mode) {
        mFileMode = mode;
        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1911057*/
        if (mApplication.mFileInfoManager!=null){
            mApplication.mFileInfoManager.mCurMode = mode;// add for PR972394 by
        }
        /*MODIFIED-END by haifeng.tang,BUG-1911057*/
        // yane.wang@jrdcom.com
        // 20150410

    }

    protected void updateDisplayOptions() {
//        final int MASK = ActionBar.DISPLAY_SHOW_TITLE
//                | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP
//                | ActionBar.DISPLAY_SHOW_CUSTOM;
//        int newFlags = 0;
//        newFlags |= ActionBar.DISPLAY_SHOW_TITLE;
//        if (getActionBar() != null) {
//            if (mFileMode == FILE_MODE_SEARCH) {
//                newFlags |= ActionBar.DISPLAY_HOME_AS_UP;
//                newFlags |= ActionBar.DISPLAY_SHOW_CUSTOM;
//                getActionBar().setDisplayOptions(newFlags, MASK);
//            } else {
//                getActionBar().setDisplayOptions(
//                        ActionBar.DISPLAY_SHOW_CUSTOM,
//                        ActionBar.DISPLAY_SHOW_CUSTOM
//                                | ActionBar.DISPLAY_SHOW_HOME
//                                | ActionBar.DISPLAY_SHOW_TITLE
//                                | ActionBar.DISPLAY_HOME_AS_UP);
//            }
//        }
    }

    protected void updateNormalBar() {
        LogUtils.d("APP", "this is enter updateNormalBar()");
        if (isCategoryFragment()) {
            // mActionBarPathText.setText(R.string.drawer_category);
            /*MODIFIED-BEGIN by wenjing.ni, 2016-04-16,BUG-1957788*/
            if(mActionBarPathText != null) {
                mActionBarPathText.setText(R.string.theme_name);
            }
            /*MODIFIED-END by wenjing.ni,BUG-1957788*/
        } else {
            if (mApplication == null) return;
            if (mMountPointManager == null) {
                mMountPointManager = MountManager.getInstance();
            }
            String path = mMountPointManager
                    .getDescriptionPath(mApplication.mCurrentPath);
            if (path != null && !path.isEmpty()) {
                String result = null;
                if (mApplication.mFileInfoManager.getPasteCount() == 0
                        || mBtnEditBack.getVisibility() == View.GONE) {// PR-1019469 Nicky Ni -001 20151207
                    if (path.contains(MountManager.SEPARATOR)) {
                        result = path.substring(path
                                .lastIndexOf(MountManager.SEPARATOR) + 1);
                        mActionBarPathText.setText(result);
                    } else {
                        mActionBarPathText.setText(path);
                    }
                }
            } else {
                // mActionBarPathText.setText(R.string.drawer_category);
                mActionBarPathText.setText(R.string.theme_name);
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return true;
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }



    public String getQueryText() {
        return mQueryText;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

    public void onClick(View v) {
    }

    protected void onDrawerClosed(View drawerView) {
    }

    protected void onDrawerStateChanged(int newState) {
    }

    public void onScannerFinished() {
    }

    public void onScannerStarted() {
    }

    protected boolean mPortraitOrientation = true;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPortraitOrientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

//    public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            int[] grantResults) {
//
//        if (PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT == requestCode) {
//            for (String permission : permissions) {
//                if (PermissionChecker.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
//                    Log.i("SNS", "No permission, " + permission);
//                    if (Build.VERSION.SDK_INT >= 23) {
//                        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                        //PermissionUtil.showPermissionPopWindow(this);
//                        //Toast.makeText(this, permission + " Denied", Toast.LENGTH_LONG).show();
//                        //finish();
//                    }
//
//                } else {
//                    switchContent(mCategoryFragment);
//                }
//            }
//        }

    //    }
//
    //[BUG-FIX]-BEGIN by NJTS Junyong.Sun 2016/01/20 PR-1401197
    protected boolean isPathInvalid(String path) {
        if (path != null) {
            FileInfo fileInfo = new FileInfo(this, path);
            if (!mMountPointManager.isExternalFile(fileInfo)) { //The path is internal file. Judge the phone storage.
                return CommonUtils.isPhoneStorageZero();
            } else {
                return false;
            }
        } else {
            return (CommonUtils.isPhoneStorageZero() && !mMountPointManager.isSDCardMounted() && !mMountPointManager.isOtgMounted());
        }
    }

    //[BUG-FIX]-END by NJTS Junyong.Sun 2016/01/20 PR-1401197
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("LAU", "this is Onnew Intent is ---111" + intent.getStringExtra("foldername"));
        laucherFolderName = intent.getStringExtra("foldername");
    }

    /**
     * delete of left slide interface
     */
    public interface deleteFileInfo{
        public void deleteFile(FileInfo fileInfo);
    }

    @Override
    public SharedPreferences getPreferences(int mode) {
        return getSharedPreferences(PREF_FILE_NAME, mode);
    }
}
