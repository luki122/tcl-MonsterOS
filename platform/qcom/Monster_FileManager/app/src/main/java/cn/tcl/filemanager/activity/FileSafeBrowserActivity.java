/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.activity;

import mst.app.dialog.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.IActivityListener;
import cn.tcl.filemanager.IActivitytoCategoryListener;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.adapter.ChooseMoveTypeAdapter;
import cn.tcl.filemanager.fragment.FileBrowserFragment.AbsListViewFragmentListener;
import cn.tcl.filemanager.fragment.GridFragment;
import cn.tcl.filemanager.fragment.ListsFragment;
import cn.tcl.filemanager.fragment.SafeCategoryFragment;
import cn.tcl.filemanager.fragment.SafeCategoryFragment.CategoryFragmentListener;
import cn.tcl.filemanager.manager.CategoryCountManager;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.manager.SafeManager;
import cn.tcl.filemanager.service.FileManagerService;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;
import cn.tcl.filemanager.view.HorizontalListView;


/**
 * Created by user on 16-3-3.
 */
public class FileSafeBrowserActivity extends FileBaseActivity implements CategoryFragmentListener, AbsListViewFragmentListener, AdapterView.OnItemClickListener {

    public static final String CATEGORY_TAG = "category";
    protected static final String LIST_MODE = "listMode";
    protected static final String GRID_MODE = "gridMode";
    public static final int SAFE_REQUEST_CODE = 11;
    public static final String SAFE_MODE = "mode";
    private static final String TAG = FileSafeBrowserActivity.class.getSimpleName();



//    private static final int FILE_NORMAL = 0;
//
//    public static final int FILE_MOVE_IN = 1;
//
//    private static final int FILE_MOVE_OUT =2;
//
//    public static int mCurrentmode = 0;

    private PowerManager.WakeLock wakeLock;

    private String mViewMode = LIST_MODE;
    private String mTagMode = CATEGORY_TAG;
    public static final String DESTORY_TAG = "destory";
    private boolean isDestoryFragment = false;
    LinearLayout mFilePathLayout;


    private String currentSafePath;
    protected SafeCategoryFragment mSafeCategoryFragment;
    protected ListsFragment mListFragment;
    protected GridFragment mGridFragment;
    //protected android.app.FragmentManager mFragmentManager;
    FragmentTransaction fragmentTransaction;
    FragmentManager mFragmentManager;
    //protected FileManagerApplication mApplication;
    private IActivitytoCategoryListener mActivitytoCategoryListener;
    HorizontalListView fileBrowerList;
    //protected IActivityListener mActivityListener;
    private AlertDialog mChooseShiftDialog;
    private boolean mSafeBtnIsVisiable = true; //MODIFIED by haifeng.tang, 2016-04-08,BUG-1912698
    /* MODIFIED-BEGIN by zibin.wang, 2016-05-11,BUG-2125562*/
    private String mSDCardRootPath;
    private String mOtgRootPath;
    private String mSafeRootPath;
    public static boolean isFirstCreateAdd = false; // MODIFIED by wenjing.ni, 2016-05-13,BUG-2003636
    /* MODIFIED-END by zibin.wang,BUG-2125562*/

    @Override
    public void onAttachFragment(Fragment fragment) {
        try {
            Log.d(TAG, "onAttachFragment(), fragment = " + fragment); // MODIFIED by songlin.qi, 2016-06-15,BUG-2227088
            if (fragment instanceof IActivityListener) {
                mActivityListener = (IActivityListener) fragment;
            }
            // set value for mActivitytoCategoryListener with attached fragment.
            if (fragment instanceof IActivitytoCategoryListener) {
                mActivitytoCategoryListener = (IActivitytoCategoryListener) fragment;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onAttachFragment(fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()"); // MODIFIED by songlin.qi, 2016-06-15,BUG-2227088
        if (mActivityListener != null) {
            mActivityListener.deleteSafeTempFile();
        }
    }

    @Override
    public void setMainContentView() {
        super.setMainContentView();

        Intent intent = getIntent();
        if (intent != null) {
            currentSafePath = intent.getStringExtra("currentsafepath");
            isDestoryFragment = intent.getBooleanExtra("destory_safe", false);
        }
        mlayout = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mFilePathLayout = (LinearLayout) findViewById(R.id.file_path_layout);
        mFilePathLayout.setVisibility(View.GONE);
        mGlobalSearchView.setVisibility(View.GONE);
        //mSearchBar.setVisibility(View.GONE);
        //setFileActionMode(FILE_MODE_NORMAL);
        if (isDestoryFragment) {
            mTagMode = DESTORY_TAG;
        } else {
            mTagMode = CATEGORY_TAG;
            changePrefCurrTag(CATEGORY_TAG);
        }
        /* MODIFIED-BEGIN by zibin.wang, 2016-05-11,BUG-2125562*/
        mSafeRootPath = new File(SharedPreferenceUtils.getCurrentSafeRoot(this)).getParent();
        if(mMountPointManager != null){
            if(mMountPointManager.isSDCardMounted()){
                mSDCardRootPath = mMountPointManager.getSDCardPath();
            }
            if(mMountPointManager.isOtgMounted()){
                mOtgRootPath = mMountPointManager.getUsbOtgPath();
            }
        }
        /* MODIFIED-END by zibin.wang,BUG-2125562*/
        setPathWidth(); // MODIFIED by wenjing.ni, 2016-05-11,BUG-2125384
        // init actionbar title
        setActionbarTitle(SafeUtils.getSafeTitle(mMountPointManager, this)); // MODIFIED by songlin.qi, 2016-06-15,BUG-2227088
        if (mSafeCategoryFragment == null) {
            mSafeCategoryFragment = new SafeCategoryFragment(mTagMode);
        }
//        if(mDestroySafeFragment==null) {
//            mDestroySafeFragment = new DestorySafeFragment();
//        }
        if (mListFragment == null) {
            mListFragment = new ListsFragment();
        }
        if (mGridFragment == null) {
            mGridFragment = new GridFragment();
        }
        mFragmentManager = getFragmentManager();
        fragmentTransaction = mFragmentManager.beginTransaction();
//        if (DESTORY_TAG.equals(mTagMode)) {
//            fragmentTransaction.replace(R.id.layout_main_frame, mDestroySafeFragment).commitAllowingStateLoss();
//            mCurrentFragment = mDestroySafeFragment;
//
//        } else if (CATEGORY_TAG.equals(mTagMode)) {
        if (fragmentTransaction != null) {
            fragmentTransaction.replace(R.id.layout_main_frame, mSafeCategoryFragment).commitAllowingStateLoss();
//            }
            mCurrentFragment = mSafeCategoryFragment;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateMenuBar(menu, getFileActionMode());
        return true;
    }

    public static boolean isDestory = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {


            /**
             * category fragment menu start
             */
            case R.id.add_file:
                addFileClick(true);

                return true;
            case R.id.set_safe:
                SafeManager.notQuitSafe = true;
                Intent intent = new Intent(this, SafeBoxSettingsActivity.class); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1909322
                intent.putExtra("setpassword", true);
                intent.putExtra("currentsafepath", currentSafePath);
                startActivityForResult(intent,SafeManager.ENETER_SAFE_SETTINGS_REQUEST_CODE);
                //mSafeCategoryFragment= null;


                return true;

            case R.id.file_detail:

                mActivityListener.clickDetailsBtn();

                return true;
            case R.id.file_delete:
                mActivityListener.clickDelteBtn(SafeManager.SAFE_DELETE_MODE);
                return true;
            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-25,BUG-1989942*/
            case R.id.select_item:
                mActivityListener.clickEditBtn();
                return true;

            case R.id.view_mode:
                if (TextUtils.equals(mViewMode,LIST_MODE)){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mViewMode = GRID_MODE;
                            /* MODIFIED-BEGIN by wenjing.ni, 2016-05-16,BUG-2145928*/
                            changePrefViewBy(GRID_MODE);
                            SharedPreferenceUtils.setCurrentViewMode(FileSafeBrowserActivity.this, GRID_MODE);
                            /* MODIFIED-END by wenjing.ni,BUG-2145928*/
                            switchContent(mGridFragment);
                        }
                    },200);


                }else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mViewMode = LIST_MODE;
                            /* MODIFIED-BEGIN by wenjing.ni, 2016-05-16,BUG-2145928*/
                            changePrefViewBy(LIST_MODE);
                            SharedPreferenceUtils.setCurrentViewMode(FileSafeBrowserActivity.this, LIST_MODE);
                            /* MODIFIED-END by wenjing.ni,BUG-2145928*/
                            switchContent(mListFragment);
                        }
                    },200);

                }
                invalidateOptionsMenu();
                return true;
                /* MODIFIED-END by haifeng.tang,BUG-1989942*/

            case R.id.move_out:
                LogUtils.i(TAG, "CategoryManager.mCurrentCagegory->" + CategoryManager.mCurrentCagegory);
//                SafeManager.mCurrentmode = SafeManager.FILE_MOVE_OUT;
                View removeDialogView = (View) mlayout.inflate(R.layout.choose_remove_dialog, null);
                ListView removeDialogList = (ListView) removeDialogView.findViewById(R.id.remove_list_view);
                ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
                String[] from = {"title", "content"};
                int[] to = {R.id.choose_remove_title, R.id.choose_remove_content};
                for (int i = 0; i < 2; i++) {
                    HashMap map = new HashMap<String, String>();
                    if (i == 0) {
                        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-15,BUG-1944818*/
                        map.put("title", getResources().getString(R.string.default_location_cn));
                        map.put("content", getResources().getString(R.string.draw_left_phone_storage)+File.separator+"File_Restore"); // MODIFIED by wenjing.ni, 2016-05-07,BUG-802835
                        list.add(map);
                    } else {
                        map.put("title", getResources().getString(R.string.specified_location_cn));
                        map.put("content", getResources().getString(R.string.specified_location_content_cn));
                        /*MODIFIED-END by haifeng.tang,BUG-1944818*/
                        list.add(map);
                    }
                }
                SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.choose_remove_item, from, to);
                removeDialogList.setAdapter(adapter);
                removeDialogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if (mChooseShiftDialog != null && mChooseShiftDialog.isShowing()) {
                            mChooseShiftDialog.dismiss();
                        }
                        if (i == 0) {
                            mActivityListener.clickShiftOutBth(i, SafeUtils.getSafeRestoreDefaultPath(mMountPointManager));
                        } else {
                            SafeManager.notQuitSafe = true;
                            Intent intent = new Intent(FileSafeBrowserActivity.this, PathSelectionActivity.class);
                            intent.setAction(Intent.ACTION_PICK);
                            intent.putExtra(EXTRA_SAFE_CATEGORY, true);
//                            intent.setType("*/*");
//                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            startActivityForResult(intent, SafeManager.REMOVE_SAFE_PATH_SELECT_REQUEST_CODE);
                        }
                    }
                });
                mChooseShiftDialog = new AlertDialog.Builder(this).setTitle(R.string.remove_to).setView(removeDialogView).show();
                CommonUtils.setDialogTitleInCenter(mChooseShiftDialog); // MODIFIED by songlin.qi, 2016-06-14,BUG-2269190
                return true;
/*MODIFIED-BEGIN by haifeng.tang, 2016-04-15,BUG-1950773*/
//            case R.id.destory_safe_menu:
//                destorySafeBox();
//                return true;


        }
        return super.onOptionsItemSelected(item);

    }

    /* MODIFIED-BEGIN by wenjing.ni, 2016-05-11,BUG-2125384*/
    protected void setPathWidth() {
        if (mActionBarTv != null) {
            ViewGroup.LayoutParams lP = mActionBarTv.getLayoutParams();
            lP.width = getPortWidth();
            mActionBarTv.setLayoutParams(lP);
        }
    }

    private int getPortWidth() {
        int width = -1;
        width = mWindowWidth;
        return width;
    }
    /* MODIFIED-END by wenjing.ni,BUG-2125384*/

    public void destorySafeBox() {
        String mDestoryPassword = mSafeCategoryFragment.mPasswordEdit.getText().toString();

        if (mDestoryPassword == null || mDestoryPassword.equals("")) {
            Toast.makeText(this, getResources().getString(R.string.password_empty), Toast.LENGTH_LONG).show();
        } else if (!SafeManager.destorySafe(FileSafeBrowserActivity.this, SafeUtils.getCurrentSafePath(this), mDestoryPassword)) {
            Toast.makeText(this, getResources().getString(R.string.password_mistake), Toast.LENGTH_LONG).show();
        } else {
            /* MODIFIED-BEGIN by wenjing.ni, 2016-04-29,BUG-2003762*/
            InputMethodManager immEdit = (InputMethodManager) this
                    .getSystemService(INPUT_METHOD_SERVICE);
            if (immEdit != null) {
                immEdit.hideSoftInputFromWindow(mSafeCategoryFragment.mPasswordEdit.getWindowToken(), 0);
            }
            /* MODIFIED-END by wenjing.ni,BUG-2003762*/
            LayoutInflater mlayout = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

            View destoryDialogLayout = (View) mlayout.inflate(R.layout.destory_safe_dialog, null);
            final CheckBox mMoveCheckbox = (CheckBox) destoryDialogLayout.findViewById(R.id.destory_safe_checkBox);
            //mMoveCheckbox.setOnCheckedChangeListener(this);
            AlertDialog destroyDialog = new AlertDialog.Builder(FileSafeBrowserActivity.this).setTitle(R.string.destory_safe_cn).setView(destoryDialogLayout) // MODIFIED by songlin.qi, 2016-06-14,BUG-2269190
                    .setPositiveButton(R.string.destory, new DialogInterface.OnClickListener() { // MODIFIED by zibin.wang, 2016-05-13,BUG-1946917

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            isDestory = true;
                            if (mMoveCheckbox.isChecked()) {
                                mActivityListener.clickDestorySafe(SafeManager.DESTORY_RECOVER_MODE, SafeUtils.getSafeRestoreDefaultPath(mMountPointManager));
                            } else {
                                mActivityListener.clickDestorySafe(SafeManager.DESTORY_DELETE_MODE, "");
                            }


                        }
                    }).setNegativeButton(R.string.cancel, null).show();
            /* MODIFIED-BEGIN by songlin.qi, 2016-06-14,BUG-2269190*/
            destroyDialog.setCanceledOnTouchOutside(false);
            destroyDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.negative_text_color));
            /* MODIFIED-END by songlin.qi,BUG-2269190*/
            CommonUtils.setDialogTitleInCenter(destroyDialog);
        }
        /*MODIFIED-END by haifeng.tang,BUG-1950773*/
    }

    @Override
    public void switchCategoryList() {
        switchContentByViewMode();
    }

    @Override
    public void updateCategoryNormalBarView() {
        updateCategoryNormalBar();
    }

    /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2003636*/
    @Override
    public void updateSafeCategory() {
        if(mActivityListener != null){
            isFirstCreateAdd = true;
            mActivityListener.clickPrivateBtn(isFirstCreateAdd);
        }
    }
    /* MODIFIED-END by wenjing.ni,BUG-2003636*/

    protected void updateCategoryNormalBar() {
        // TODO
    }

    public boolean isSafeCategoryFragment() {
        return mCurrentFragment == mSafeCategoryFragment;
    }

    private void switchContentByViewMode() {
        Log.d(TAG, "switchContentByViewMode(), mViewMode = " + mViewMode); // MODIFIED by songlin.qi, 2016-06-15,BUG-2227088
        mViewMode = SharedPreferenceUtils.getCurrentViewMode(this);
        if (mViewMode == null) {
            mViewMode = LIST_MODE;
        }

        if (mViewMode.equals(LIST_MODE)) {// add for PR843769 by
            // yane.wang@jrdcom.com 20141118
            switchContent(mListFragment);
        } else if (mViewMode.equals(GRID_MODE)) {// add for PR843769 by
            // yane.wang@jrdcom.com
            // 20141118
            switchContent(mGridFragment);
        }
    }


    public void switchContent(Fragment to) {
        Log.d(TAG, "switchContent(), to=" + to); // MODIFIED by songlin.qi, 2016-06-15,BUG-2227088
        if (mCurrentFragment != mSafeCategoryFragment) {
            CategoryCountManager.getInstance().clearMap();
        }
        if (mApplication.mService != null) {
            mApplication.mService
                    .setListType(
                            SharedPreferenceUtils.isShowHidden(this) ? FileManagerService.FILE_FILTER_TYPE_ALL
                                    : FileManagerService.FILE_FILTER_TYPE_DEFAULT,
                            FileSafeBrowserActivity.this.getClass().getName());
        }
        if (mCurrentFragment != to) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.layout_main_frame, to);
            fragmentTransaction.commitAllowingStateLoss();
            mCurrentFragment = to;
            if (to == mListFragment) {
                /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
                if (SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN) {
                    CategoryManager.isSafeCategory = false;
                } else {
                    CategoryManager.isSafeCategory = true;
                }
                /* MODIFIED-END by songlin.qi,BUG-2227088*/
//                mActivityListener = mListFragment;
//                refreshPathAdapter(mApplication.mCurrentPath);
                mActivityListener = mListFragment;
                new Handler().post(new Runnable() {

                    @Override
                    public void run() {
                        updateViewByTag();
                        updateView(getFileMode());
                    }

                });
//                Log.d("PRD", "this is mCurrentFragment is --88" + mCurrentFragment);
                refreshPathAdapter(mApplication.mCurrentPath);
                //mActivityListener.setPaddingTop(true);
            } else if (to == mGridFragment) {
                /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
                if (SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN) {
                    CategoryManager.isSafeCategory = false;
                } else {
                    CategoryManager.isSafeCategory = true;
                }
                /* MODIFIED-END by songlin.qi,BUG-2227088*/
                mActivityListener = mGridFragment;
                new Handler().post(new Runnable() {

                    @Override
                    public void run() {
                        updateViewByTag();
                        updateView(getFileMode());
                    }

                });
                refreshPathAdapter(mApplication.mCurrentPath);
            } else if (to == mSafeCategoryFragment) {
                /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
                CategoryManager.isSafeCategory = false;
                CategoryManager.mCurrentSafeCategory = -1;
                /* MODIFIED-END by songlin.qi,BUG-2227088*/
                mActivitytoCategoryListener = (IActivitytoCategoryListener) mSafeCategoryFragment;
                if (mActivityListener != null) {
                    mActivityListener.clearAdapter();
                }
                mApplication.mFileInfoManager.getCategoryFileList().clear();
                updateViewByTag();
                //updateMoreButtonMenu();
                updateCategoryNormalBar();
                mActivitytoCategoryListener.refreshCategory();
            }
        } else if (to == mSafeCategoryFragment) {
            mApplication.mFileInfoManager.getCategoryFileList().clear();
            /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
            CategoryManager.isSafeCategory = false;
            CategoryManager.mCurrentSafeCategory = -1;
            /* MODIFIED-END by songlin.qi,BUG-2227088*/
            mActivitytoCategoryListener = (IActivitytoCategoryListener) mSafeCategoryFragment;
            if (mActivityListener != null) {
                mActivityListener.clearAdapter();
            }
            updateViewByTag();
//            dateMoreButtonMenu();
            updateCategoryNormalBar();
            mActivitytoCategoryListener.refreshCategory();
        } else if (to == mListFragment) {
            /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
            if (SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN) {
                CategoryManager.isSafeCategory = false;
            } else {
                CategoryManager.isSafeCategory = true;
            }
            /* MODIFIED-END by songlin.qi,BUG-2227088*/
            mActivityListener = (IActivityListener) mListFragment;
            new Handler().post(new Runnable() {

                @Override
                public void run() {
                    updateViewByTag();
                    updateView(getFileMode());
                }
            });
            refreshPathAdapter(mApplication.mCurrentPath);
            //mActivityListener.setPaddingTop(true);
        } else if (to == mGridFragment) {
            /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
            if (SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN) {
                CategoryManager.isSafeCategory = false;
            } else {
                CategoryManager.isSafeCategory = true;
            }
            /* MODIFIED-END by songlin.qi,BUG-2227088*/
            mActivityListener = (IActivityListener) mGridFragment;
            new Handler().post(new Runnable() {

                @Override
                public void run() {
                    updateViewByTag();
                    updateView(getFileMode());
                }

            });
            //mActivityListener.setPaddingTop(true);
            refreshPathAdapter(mApplication.mCurrentPath);
        }


    }

    private void updateViewByTag() {

    }

    private void updateView(int mode) {
        updateDisplayOptions();
        invalidateOptionsMenu();
        switch (mode) {
            case FILE_MODE_NORMAL:
                if ((mApplication.currentOperation != FileManagerApplication.PASTE) && mApplication.currentOperation != FileManagerApplication.RENAME) {
                /*PR 909272 zibin.wang 2015.11.18 Start*/
                    refreshPathAdapter(mApplication.mCurrentPath);
                /*PR 909272 zibin.wang 2015.11.18 End*/
                }

                selectCount = 0;
                break;
            case FILE_MODE_EDIT:
                InputMethodManager immEdit = (InputMethodManager) this
                        .getSystemService(INPUT_METHOD_SERVICE);
                if (immEdit != null) {
                    immEdit.hideSoftInputFromWindow(mGlobalSearchView.getWindowToken(), 0);
                }
                break;
            default:
                break;
        }
        //updateMoreButtonMenu();
    }


    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-08,BUG-1912698*/
    public void setAddSafeFileBtn(boolean safeBtnIsVisiable) {
        mSafeBtnIsVisiable = safeBtnIsVisiable;
        invalidateOptionsMenu();
    }
    /*MODIFIED-END by haifeng.tang,BUG-1912698*/

    @Override
    protected void refreshPathAdapter(String path) {
        super.refreshPathAdapter(path);
        //LogUtils.d("SNS","this is refreshPathAdapter(String path)");
        LogUtils.e(TAG,"refreshPathAdapter PATH2 " + path);
        mActivityListener.refreshAdapter(path);
    }

    @Override
    public void updateEditBar(int count, boolean isHasDir, boolean isHasDrm, boolean canShare) {
        if (mFileMode == FILE_MODE_NORMAL) {
            mBtnMore.setEnabled(true);
            return;
        }
        if (count == 0) {
            setActionbarTitle(R.string.unselected); // MODIFIED by haifeng.tang, 2016-04-27,BUG-1991729
        } else {
            setActionbarTitle(getResources().getString(R.string.menu_select).toString() + "(" + count + ")");
        }
        selectCount = count;
        mIsHasDir = isHasDir;
        mIsHasDrm = isHasDrm;
        mCanShare = canShare;
        //updateEditBarWidgetState(count);
        //updateMoreButtonMenu();
        invalidateOptionsMenu();
    }

    @Override
    public void reSearch() {

    }

    @Override
    public void showBottomView(String string) {

    }

    @Override
    public void setFileActionMode(int mode) {
        setFileMode(mode);

    }

    @Override
    public int getFileActionMode() {
        return getFileMode();
    }

    @Override
    public void updateActionbar() {
        updateBarTitle();
        invalidateOptionsMenu();
    }

    @Override
    public void updateEncryptFileCount(int count) {

    }

    public void updateBarTitle() {
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
        Log.d(TAG, "updateBarTitle()");
        if (mApplication == null)
            return;
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }
        if (CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE) {
            if (SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN && CategoryManager.mCurrentCagegory  >= 0) {
                if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_DOCS) {
                    setActionbarTitle(R.string.category_files);
                } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_MUSIC) {
                    setActionbarTitle(R.string.category_music);
                } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                    setActionbarTitle(R.string.category_pictures);
                } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_VEDIOS) {
                    setActionbarTitle(R.string.category_vedios);
                }
            } else if (CategoryManager.mCurrentSafeCategory  >= 0) {
                if (CategoryManager.mCurrentSafeCategory == CategoryManager.SAFE_CATEGORY_FILES) {
                    setActionbarTitle(R.string.category_files);
                } else if (CategoryManager.mCurrentSafeCategory == CategoryManager.SAFE_CATEGORY_MUISC) {
                    setActionbarTitle(R.string.category_music);
                } else if (CategoryManager.mCurrentSafeCategory == CategoryManager.SAFE_CATEGORY_PICTURES) {
                    setActionbarTitle(R.string.category_pictures);
                } else if (CategoryManager.mCurrentSafeCategory == CategoryManager.SAFE_CATEGORY_VEDIO) {
                    setActionbarTitle(R.string.category_vedios);
                }
            } else {
                setActionbarTitle(SafeUtils.getSafeTitle(mMountPointManager, this));
            }
        } else {
            setActionbarTitle(SafeUtils.getSafeTitle(mMountPointManager, this));
            /* MODIFIED-END by songlin.qi,BUG-2227088*/
        }
    }

    @Override
    public void updateNormalBarView() {
        //getPathBrowerText();
        updateNormalBar();
    }

    @Override
    public void setPrefsSortby(int sort) {
        changePrefsSortBy(sort);
    }

    @Override
    public void changeSearchMode(boolean flag) {

    }

    @Override
    public void toShowForbiddenDialog() {

    }

    @Override
    public void pasteBtnUpdated() {

    }

    @Override
    public void toReleaseWakeLock() {
        releaseWakeLock();
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

    @Override
    public void HideActionbar(boolean flag) {

    }

    @Override
    public void isDeleteFlag(boolean flag) {

    }

    @Override
    public int getSlideLimite() {
        return 0;
    }

    /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2127786*/
    @Override
    public void refreashSafeFilesCategory() {
        if(isRefreshFilesCategory) {
            setActionbarTitle(R.string.category_files);
            CategoryManager.mCurrentSafeCategory = CategoryManager.SAFE_CATEGORY_FILES; // MODIFIED by songlin.qi, 2016-06-15,BUG-2227088
            SafeManager.mCurrentmode = SafeManager.FILE_NORMAL;
            CategoryManager.isSafeCategory = true;
            updateCategoryNormalBarView();
            switchCategoryList();
        /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2003636*/
        } else if(isFirstCreateAdd && mActivitytoCategoryListener != null){
            mActivitytoCategoryListener.refreshCategory();
        }
        isRefreshFilesCategory = false;
        isFirstCreateAdd = false;
        /* MODIFIED-END by wenjing.ni,BUG-2003636*/
    }

    @Override
    public void switchDecryptDircetion(List<FileInfo> fileInfos) {

    }

    @Override
    public void hideEditWindowWhenEncryptFile() {

    }
    /* MODIFIED-END by wenjing.ni,BUG-2127786*/


    @Override
    public void onBackPressed() {
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
        Log.d(TAG, "onBackPressed()");
        if (mActivityListener != null) {
            mActivityListener.showNoSearchResults(false, null);
            mActivityListener.showNoFolderResultView(false);
        }
        if (mCurrentFragment == mSafeCategoryFragment) {
        /* MODIFIED-END by songlin.qi,BUG-2227088*/
            CategoryManager.isSafeCategory = false;
        }

        if (getFileActionMode() != FILE_MODE_EDIT || isDestory) {
            SafeManager.mCurrentmode = SafeManager.FILE_NORMAL;
        }
        if (mApplication.mService != null) {
            mApplication.mService.cancel(this.getClass().getName());
        }
        /* MODIFIED-BEGIN by haifeng.tang, 2016-04-28,BUG-1995590*/
        InputMethodManager imm = (InputMethodManager)
               getSystemService(INPUT_METHOD_SERVICE);
               /* MODIFIED-END by haifeng.tang,BUG-1995590*/
        if (imm != null) {
            imm.hideSoftInputFromWindow(mGlobalSearchView.getWindowToken(), 0);
        }

        if (mCurrentFragment == mListFragment
                || mCurrentFragment == mGridFragment) {
            if ((CategoryManager.mCurrentMode == CategoryManager.CATEGORY_MODE
                    && mFileMode == FILE_MODE_NORMAL)) {
                ShowCategoryContent();
                changePrefCurrTag(CATEGORY_TAG);
                // yane.wang@jrdcom.com 20150129
            } else if (CategoryManager.mCurrentMode == CategoryManager.PATH_MODE
                    && mFileMode == FILE_MODE_NORMAL
                    && mMountPointManager
                    .isSdOrPhonePath(mApplication.mCurrentPath)) {
                updateFragment(CATEGORY_TAG);
                drawerNowPosition = 0;
                drawerClickPosition = 0;
                // yane.wang@jrdcom.com 20150129
            } else if ((getFileActionMode() == FILE_MODE_EDIT && SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN)) {
                /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
                // back from FILE_MOVE_IN mode set value for mCurrentmode and mFileMode, them display category content
                setFileActionMode(FILE_MODE_NORMAL);
                SafeManager.mCurrentmode = SafeManager.FILE_NORMAL;
                if (CategoryManager.mCurrentSafeCategory != -1) {
                    CategoryManager.isSafeCategory = true;
                    switchCategoryList();
                } else {
                    ShowCategoryContent();
                    CategoryManager.isSafeCategory = false;
                    changePrefCurrTag(CATEGORY_TAG);
                }
                /* MODIFIED-END by songlin.qi,BUG-2227088*/
            } else {
                if (mActivityListener != null) {// add for PR927979 by
                    // yane.wang@jrdcom.com 20150209
                    mActivityListener.onBackPressed();
                }
            }
            updateBarTitle();

            return;
        } else if ((mCurrentFragment == mSafeCategoryFragment && mTagMode.equals(DESTORY_TAG))) {
            //if(mSafeCategoryFragment==null) {
            mTagMode = CATEGORY_TAG;
            mSafeCategoryFragment = new SafeCategoryFragment(mTagMode);
            //}
            fragmentTransaction = mFragmentManager.beginTransaction();
            if (fragmentTransaction != null) {
                fragmentTransaction.replace(R.id.layout_main_frame, mSafeCategoryFragment).commitAllowingStateLoss();
//            }
                mCurrentFragment = mSafeCategoryFragment;
            }
            setActionbarTitle(SafeUtils.getSafeTitle(mMountPointManager,this));
            return;
        }

        setActionbarTitle(SafeUtils.getSafeTitle(mMountPointManager,this));
        super.onBackPressed();
    }


    private void ShowCategoryContent() {
        switchContent(mSafeCategoryFragment);
    }

    private void updateFragment(String tag, boolean... isRootClicked) {
        mTagMode = tag;
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        if (tag == CATEGORY_TAG) {

            if (mApplication.mService != null
                    && mApplication.mService.isBusy(this.getClass().getName())) {
                mApplication.mService.cancel(this.getClass().getName());
            }
            CategoryManager.setCurrentMode(CategoryManager.CATEGORY_MODE);
            mApplication.mCurrentPath = null;
            ShowCategoryContent();
            //hideFloatButton();
            return;
        }
        switchContentByViewMode();
    }

    private AlertDialog chooseFileTypeDialog;
    private GridView mChooseFileMoveGridview;
    private LayoutInflater mlayout;

    private void chooseMoveFileTypeDialog() {
        View mChooseFileDialog = (View) mlayout.inflate(R.layout.choose_file_dialog, null);
        mChooseFileMoveGridview = (GridView) mChooseFileDialog.findViewById(R.id.choose_file_view);
        ChooseMoveTypeAdapter mChooseFileTypeadapter = new ChooseMoveTypeAdapter(this);
        mChooseFileMoveGridview.setAdapter(mChooseFileTypeadapter);
        mChooseFileMoveGridview.setOnItemClickListener(this);
        chooseFileTypeDialog = new AlertDialog.Builder(this).setTitle(R.string.choose_files).setView(mChooseFileDialog).
                setPositiveButton(R.string.cancel, null).show();
        CommonUtils.setDialogTitleInCenter(chooseFileTypeDialog); // MODIFIED by songlin.qi, 2016-06-14,BUG-2269190

    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (chooseFileTypeDialog != null && chooseFileTypeDialog.isShowing()) {
            chooseFileTypeDialog.dismiss();
        }
        if (i == CategoryManager.SAFE_CATEGORY_FILES) {
            setActionbarTitle(R.string.category_files);
            /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
            CategoryManager.mCurrentSafeCategory = CategoryManager.SAFE_CATEGORY_FILES;
        } else if (i == CategoryManager.SAFE_CATEGORY_MUISC) {
            setActionbarTitle(R.string.category_music);
            CategoryManager.mCurrentSafeCategory = CategoryManager.SAFE_CATEGORY_MUISC;
        } else if (i == CategoryManager.SAFE_CATEGORY_PICTURES) {
            setActionbarTitle(R.string.category_pictures);
            CategoryManager.mCurrentSafeCategory = CategoryManager.SAFE_CATEGORY_PICTURES;
        } else if (i == CategoryManager.SAFE_CATEGORY_VEDIO) {
            setActionbarTitle(R.string.category_vedios);
            CategoryManager.mCurrentSafeCategory = CategoryManager.SAFE_CATEGORY_VEDIO;
            /* MODIFIED-END by songlin.qi,BUG-2227088*/
        }

        addFileClick(false);

    }


    private void addFileClick(boolean isCategoryAdd) {
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
        Log.d(TAG, "addFileClick(), isCategoryAdd=" + isCategoryAdd);
        CategoryManager.isSafeCategory = false;
        if (SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN && selectCount == 0) {
            return;
        } else if (getFileActionMode() == FILE_MODE_EDIT) {
            mActivityListener.clickPrivateBtn(false);
        } else if (CategoryManager.mCurrentSafeCategory == CategoryManager.SAFE_CATEGORY_FILES) {
            SafeManager.notQuitSafe = true;
            setActionbarTitle(R.string.category_files);
            SafeManager.mCurrentmode = SafeManager.FILE_MOVE_IN;
            CategoryManager.mCurrentCagegory = CategoryManager.CATEGORY_DOCS;
            updateCategoryNormalBarView();
            Intent intent = new Intent(this, SelectSafeFilesActivity.class);
            intent.putExtra(EXTRA_SAFE_CATEGORY, true);
            startActivityForResult(intent, SAFE_REQUEST_CODE);
        } else if (CategoryManager.mCurrentSafeCategory == CategoryManager.SAFE_CATEGORY_MUISC) {
            setActionbarTitle(R.string.unselected);
            setFileActionMode(FILE_MODE_EDIT);
            SafeManager.mCurrentmode = SafeManager.FILE_MOVE_IN;
            CategoryManager.mCurrentCagegory = CategoryManager.CATEGORY_MUSIC;
            updateCategoryNormalBarView();
            switchCategoryList();
        } else if (CategoryManager.mCurrentSafeCategory == CategoryManager.SAFE_CATEGORY_PICTURES) {
            setActionbarTitle(R.string.unselected);
            setFileActionMode(FILE_MODE_EDIT);
            SafeManager.mCurrentmode = SafeManager.FILE_MOVE_IN;
            CategoryManager.mCurrentCagegory = CategoryManager.CATEGORY_PICTURES;
            updateCategoryNormalBarView();
            switchCategoryList();
        } else if (CategoryManager.mCurrentSafeCategory == CategoryManager.SAFE_CATEGORY_VEDIO) {
        /* MODIFIED-END by songlin.qi,BUG-2227088*/
            setActionbarTitle(R.string.unselected);
            setFileActionMode(FILE_MODE_EDIT);
            SafeManager.mCurrentmode = SafeManager.FILE_MOVE_IN;
            CategoryManager.mCurrentCagegory = CategoryManager.CATEGORY_VEDIOS;
            updateCategoryNormalBarView();
            switchCategoryList();

        } else if (isCategoryAdd && SafeManager.mCurrentmode != SafeManager.FILE_MOVE_IN) {
            chooseMoveFileTypeDialog();
        }
    }


    private void updateMenuBar(Menu menu, int mode) {
        Log.d(TAG, "updateMenuBar(), mode=" + mode); // MODIFIED by songlin.qi, 2016-06-15,BUG-2227088

        menu.clear();
        MenuInflater menuInflater = getMenuInflater();
        if (mTagMode.equals(DESTORY_TAG)) {
            setActionBarButtonVisiable(false);
            menuInflater.inflate(R.menu.safe_file_browser_menu, menu);
            menu.findItem(R.id.add_file).setVisible(false);
            menu.findItem(R.id.set_safe).setVisible(false);
            menu.findItem(R.id.move_out).setVisible(false);
            menu.findItem(R.id.file_delete).setVisible(false);
            menu.findItem(R.id.file_detail).setVisible(false);
//            menu.findItem(R.id.destory_safe_menu).setVisible(true); //MODIFIED by haifeng.tang, 2016-04-15,BUG-1950773

        } else if (mCurrentFragment == mSafeCategoryFragment) {
            setActionBarButtonVisiable(false);
            getActionBar().show();
            menuInflater.inflate(R.menu.safe_file_browser_menu, menu);
            menu.findItem(R.id.add_file).setVisible(true);
            menu.findItem(R.id.set_safe).setVisible(true);
            menu.findItem(R.id.move_out).setVisible(false);
            menu.findItem(R.id.file_delete).setVisible(false);
            menu.findItem(R.id.file_detail).setVisible(false);
//            menu.findItem(R.id.destory_safe_menu).setVisible(false); //MODIFIED by haifeng.tang, 2016-04-15,BUG-1950773
        } else if (mCurrentFragment == mListFragment || mCurrentFragment == mGridFragment) {
            getActionBar().show();
            switch (mode) {
                case FILE_MODE_NORMAL:
                    setActionBarButtonVisiable(false);
                    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-25,BUG-1989942*/
                    menuInflater.inflate(R.menu.safe_file_normal_menu, menu);
                    menu.findItem(R.id.add_file).setVisible(true);
                    if(mActivityListener.getAdapterSize() != 0) {
                        menu.findItem(R.id.select_item).setVisible(true);
                        menu.findItem(R.id.view_mode).setVisible(true).setTitle(mViewMode.equals(LIST_MODE) ? R.string.view_as_grid : R.string.view_as_list);
                    } else {
                        menu.findItem(R.id.select_item).setVisible(false);
                        menu.findItem(R.id.view_mode).setVisible(false);
                    }
                    /* MODIFIED-END by haifeng.tang,BUG-1989942*/

//                    menu.findItem(R.id.destory_safe_menu).setVisible(false); //MODIFIED by haifeng.tang, 2016-04-15,BUG-1950773
                    break;
                case FILE_MODE_EDIT:
                    /*MODIFIED-BEGIN by wenjing.ni, 2016-04-15,BUG-1951948*/
                    if(mActivityListener !=null && mActivityListener.getAdapterSize()>0) {
                        setActionBarButtonVisiable(true);
                        if (mActivityListener.checkIsSelectAll()) {
                            setActionBarButtonText(R.string.deselect_all);
                        } else {
                            setActionBarButtonText(R.string.select_all);
                        }
                    }
                    /*MODIFIED-END by wenjing.ni,BUG-1951948*/
                    menuInflater.inflate(R.menu.safe_file_browser_menu, menu);
                    if (SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN) {
                        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-08,BUG-1912698*/
                        MenuItem addFileMenu =menu.findItem(R.id.add_file);
                        if (mSafeBtnIsVisiable){
                            addFileMenu.setIcon(R.drawable.move_in);
                        }else {
                            addFileMenu.setIcon(R.drawable.move_in_disable);
                        }
                        addFileMenu.setEnabled(mSafeBtnIsVisiable);
                        /*MODIFIED-END by haifeng.tang,BUG-1912698*/
                        menu.findItem(R.id.add_file).setVisible(true);
                        menu.findItem(R.id.set_safe).setVisible(false);
                        menu.findItem(R.id.move_out).setVisible(false);
                        menu.findItem(R.id.file_delete).setVisible(false);
                        menu.findItem(R.id.file_detail).setVisible(false);
//                        menu.findItem(R.id.destory_safe_menu).setVisible(false); //MODIFIED by haifeng.tang, 2016-04-15,BUG-1950773
                    } else {
                        menu.findItem(R.id.add_file).setVisible(false);
                        menu.findItem(R.id.set_safe).setVisible(false);
                        if (selectCount > 0) {
                            menu.findItem(R.id.move_out).setVisible(true);
                            menu.findItem(R.id.file_delete).setVisible(true);
                        } else {
                            menu.findItem(R.id.move_out).setVisible(false);
                            menu.findItem(R.id.file_delete).setVisible(false);
                        }
                        if (selectCount == 1) {
                            menu.findItem(R.id.file_detail).setVisible(true);
                        } else {
                            menu.findItem(R.id.file_detail).setVisible(false);
                        }
//                        menu.findItem(R.id.destory_safe_menu).setVisible(false); //MODIFIED by haifeng.tang, 2016-04-15,BUG-1950773
                    }

                    break;
            }
        }

    }


    /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2127786*/
    public boolean isRefreshFilesCategory = false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isRefreshFilesCategory = false;
        initFileInfoManager();
        if (requestCode == SAFE_REQUEST_CODE) {

            if (mCurrentFragment == mSafeCategoryFragment) {
                isRefreshFilesCategory = true;
                /* MODIFIED-END by wenjing.ni,BUG-2127786*/
            }
            mActivityListener.clickPrivateBtn(true);

        } else if (requestCode == SafeManager.REMOVE_SAFE_PATH_SELECT_REQUEST_CODE && resultCode == RESULT_OK) {
            String mDesFolder = data.getStringExtra("result_dir_sel");
            Toast.makeText(FileSafeBrowserActivity.this, mDesFolder, Toast.LENGTH_SHORT).show();
            mActivityListener.clickShiftOutBth(FileInfoManager.SHIFT_OUT_TARGET_MODE, mDesFolder);
        } else if (requestCode == SafeManager.REMOVE_SAFE_PATH_SELECT_REQUEST_CODE && resultCode == RESULT_CANCELED) {
            SafeManager.mCurrentmode = SafeManager.FILE_NORMAL;
            setFileActionMode(FILE_MODE_NORMAL);
            updateActionbar();
        } else if (requestCode == SafeManager.ENETER_SAFE_SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            if (mCurrentFragment == mSafeCategoryFragment) {
                isRefreshFilesCategory = true;
            }
            finish();
        } else if (requestCode == SafeManager.ENETER_SAFE_SETTINGS_REQUEST_CODE) {
            setActionbarTitle(SafeUtils.getSafeTitle(mMountPointManager, this));
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            isDestoryFragment = intent.getBooleanExtra("destory_safe", false);
        }
        if (isDestoryFragment) {
            mTagMode = DESTORY_TAG;
        } else {
            mTagMode = CATEGORY_TAG;
            changePrefCurrTag(CATEGORY_TAG);
        }
        //test
        //if(mSafeCategoryFragment==null) {
        mSafeCategoryFragment = new SafeCategoryFragment(mTagMode);
        //}
        /* MODIFIED-BEGIN by songlin.qi, 2016-05-28,BUG-2216779*/
        if (mFragmentManager != null) {
            fragmentTransaction = mFragmentManager.beginTransaction();
        }
        /* MODIFIED-END by songlin.qi,BUG-2216779*/
        if (fragmentTransaction != null) {
            fragmentTransaction.replace(R.id.layout_main_frame, mSafeCategoryFragment).commitAllowingStateLoss();
//            }
            mCurrentFragment = mSafeCategoryFragment;
        }

        updateViewByTag();
        /* MODIFIED-BEGIN by wenjing.ni, 2016-05-04,BUG-802835*/
        if (isDestoryFragment) {
            setActionbarTitle(R.string.destory_safe_cn);
        } else {
            setActionbarTitle(SafeUtils.getSafeTitle(mMountPointManager,this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.safe_file_browser_menu, menu);
        setActionBarButtonOnClickLitener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivityListener.clickSelectAllBtn();
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SafeManager.mCurrentmode = SafeManager.FILE_NORMAL;
        CategoryManager.isSafeCategory = false;
        /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2003636*/
        isRefreshFilesCategory = false;
        isFirstCreateAdd = false;
        SafeManager.notQuitSafe = false;
        /* MODIFIED-END by wenjing.ni,BUG-2003636*/
        SafeUtils.clearSafeFiles(); // MODIFIED by haifeng.tang, 2016-04-23,BUG-1956936
        CategoryManager.mCurrentSafeCategory = -1; // MODIFIED by songlin.qi, 2016-06-15,BUG-2227088
    }
    /* MODIFIED-BEGIN by zibin.wang, 2016-05-11,BUG-2125562*/
    @Override
    public void onUnmounted(String mountPoint) {
        super.onUnmounted(mountPoint);
        if ((mSafeRootPath != null && mMountPointManager != null)
                && ((mSDCardRootPath != null && mSDCardRootPath.equals(mSafeRootPath) && !mMountPointManager.isSDCardMounted()) ||
                (mOtgRootPath != null && mOtgRootPath.equals(mSafeRootPath) && !mMountPointManager.isOtgMounted()))) {
            finish();
        }
    }
    /* MODIFIED-END by zibin.wang,BUG-2125562*/

    @Override
    protected void onStop() {
        super.onStop();
        if (SafeManager.notQuitSafe) {
            SafeManager.notQuitSafe = false;
        } else if (mApplication != null && mApplication.mService != null
                && mApplication.mService.isBusy(this.getClass().getName())) {

        } else {
            SafeManager.notQuitSafe = false;
            finish();
        }
    }
}
