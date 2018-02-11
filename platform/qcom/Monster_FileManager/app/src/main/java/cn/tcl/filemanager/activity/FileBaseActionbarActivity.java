/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.activity;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.widget.LockPatternUtils;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.TypedObject;

import mst.app.MstActivity;
import mst.widget.toolbar.Toolbar;

/* MODIFIED-BEGIN by wenjing.ni, 2016-05-04,BUG-1956936*/
/* MODIFIED-END by wenjing.ni,BUG-1956936*/

/* MODIFIED-BEGIN by wenjing.ni, 2016-04-28,BUG-2000712*/
/* MODIFIED-END by wenjing.ni,BUG-2000712*/

/**
 * Created by hftang on 3/24/16.
 */
public class FileBaseActionbarActivity extends MstActivity {

    protected TextView mActionBarTv;

    protected LinearLayout mGlobalSearchImage;

    protected LinearLayout mStorageInfoBtn;

    protected LinearLayout mDownloadBtn;

    protected ImageView mGlobalSearchBack;

    protected RelativeLayout mSelectAllTitle;

    protected TextView mSelectAllTv;

    protected TextView mEditCancelTv;

    protected Toolbar mToolbar;

    protected MenuItem mSelectItem;

    protected MenuItem mCreateFolderName;

    protected MenuItem mAddEncryptItem;

    protected MenuItem mDeleteAlbum;

    protected RelativeLayout mGlobalSearchBar;

    protected LinearLayout mFilePathLayout;
    protected RelativeLayout mBottomBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getCustomToolbar();

        mSelectAllTv = (TextView) mToolbar.findViewById(R.id.actionbar_button);
        mEditCancelTv = (TextView) mToolbar.findViewById(R.id.actionbar_button_cancel);
        mActionBarTv = (TextView) mToolbar.findViewById(R.id.actionbar_title);
        mGlobalSearchImage = (LinearLayout) mToolbar.findViewById(R.id.global_search_image);
        mStorageInfoBtn = (LinearLayout) mToolbar.findViewById(R.id.storage_info_btn);
        mDownloadBtn = (LinearLayout) mToolbar.findViewById(R.id.download_icon_btn);
        mGlobalSearchBack = (ImageView) mToolbar.findViewById(R.id.global_search_back);
        mSelectAllTitle = (RelativeLayout) mToolbar.findViewById(R.id.select_all_title);
        mGlobalSearchBar = ((RelativeLayout) mToolbar.findViewById(R.id.global_search_bar));
        mToolbar.inflateMenu(R.menu.normal_menu);
        setupActionModeWithDecor(mToolbar);
        mSelectItem = mToolbar.getMenu().findItem(R.id.select_item);
        mCreateFolderName = mToolbar.getMenu().findItem(R.id.createfolder_item);
        mAddEncryptItem = mToolbar.getMenu().findItem(R.id.add_encrypt_item);
        mDeleteAlbum = mToolbar.getMenu().findItem(R.id.delete_album_item).setVisible(false);
        mFilePathLayout = (LinearLayout) findViewById(R.id.file_path_layout);
        mBottomBarLayout = (RelativeLayout) findViewById(R.id.bottom_bar);
    }

    public Toolbar getCustomToolbar() {
        if (null == mToolbar) {
            mToolbar = (Toolbar) this.findViewById(R.id.my_toolbar);
        }
        return mToolbar;
    }

    public void setActionbarTitle(int text) {
        if (mActionBarTv != null) {
            if (R.string.category_fragment_title == text) {
                mStorageInfoBtn.setVisibility(View.VISIBLE);
                mGlobalSearchImage.setVisibility(View.VISIBLE);
                mDownloadBtn.setVisibility(View.VISIBLE);
                mGlobalSearchBack.setVisibility(View.VISIBLE);
                mToolbar.setNavigationIcon(null);
                mSelectItem.setVisible(false);
                mCreateFolderName.setVisible(false);
                mAddEncryptItem.setVisible(false);
                mDeleteAlbum.setVisible(false);
            } else {
                mStorageInfoBtn.setVisibility(View.GONE);
                mGlobalSearchImage.setVisibility(View.GONE);
                mDownloadBtn.setVisibility(View.GONE);
                mGlobalSearchBack.setVisibility(View.VISIBLE);
                mToolbar.setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);
                mSelectItem.setVisible(true);
                mDeleteAlbum.setVisible(false);
                if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_RECENT || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_VEDIOS
                        || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_MUSIC || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_APKS
                        || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_DOWNLOAD || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                    mCreateFolderName.setVisible(false);
                    mAddEncryptItem.setVisible(false);
                    mSelectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
                    mCreateFolderName.setVisible(true);
                    mSelectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                } else {
                    mCreateFolderName.setVisible(true);
                    mAddEncryptItem.setVisible(false);
                    mSelectItem.setVisible(false);
                }
            }
            mActionBarTv.setText(text);
            mActionBarTv.setTextColor(Color.BLACK);
            mActionBarTv.setAlpha(1f);
        }
    }

    public void setSearchTitle(int text) {
        if (mActionBarTv != null) {
            if (R.string.category_fragment_title == text) {
                mStorageInfoBtn.setVisibility(View.VISIBLE);
                mGlobalSearchImage.setVisibility(View.VISIBLE);
                mDownloadBtn.setVisibility(View.VISIBLE);
                mGlobalSearchBack.setVisibility(View.VISIBLE);
                mToolbar.setNavigationIcon(null);
                mSelectItem.setVisible(false);
                mCreateFolderName.setVisible(false);
                mAddEncryptItem.setVisible(false);
                mDeleteAlbum.setVisible(false);
            } else {
                mStorageInfoBtn.setVisibility(View.GONE);
                mGlobalSearchImage.setVisibility(View.GONE);
                mDownloadBtn.setVisibility(View.GONE);
                mGlobalSearchBack.setVisibility(View.VISIBLE);
                mToolbar.setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);
                mSelectItem.setVisible(true);
                mDeleteAlbum.setVisible(false);
                if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_RECENT || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_VEDIOS
                        || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_MUSIC || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_APKS
                        || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_DOWNLOAD || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                    mCreateFolderName.setVisible(false);
                    mAddEncryptItem.setVisible(false);
                    mSelectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
                    mCreateFolderName.setVisible(true);
                    mSelectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                } else {
                    mCreateFolderName.setVisible(true);
                    mAddEncryptItem.setVisible(false);
                    mSelectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                }
            }
            mActionBarTv.setText(text);
            mActionBarTv.setTextColor(Color.GRAY);
            mActionBarTv.setAlpha(0.5f);
        }
    }

    public void setActionbarTitle(String text) {
        if (getString(R.string.category_fragment_title).equals(text)) {
            mStorageInfoBtn.setVisibility(View.VISIBLE);
            mGlobalSearchImage.setVisibility(View.VISIBLE);
            mDownloadBtn.setVisibility(View.VISIBLE);
            mGlobalSearchBack.setVisibility(View.GONE);
            mToolbar.setNavigationIcon(null);
            mSelectItem.setVisible(false);
            mCreateFolderName.setVisible(false);
            mAddEncryptItem.setVisible(false);
            mDeleteAlbum.setVisible(false);
        } else {
            mStorageInfoBtn.setVisibility(View.GONE);
            mGlobalSearchImage.setVisibility(View.GONE);
            mDownloadBtn.setVisibility(View.GONE);
            mGlobalSearchBack.setVisibility(View.GONE);
            mToolbar.setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);
            mSelectItem.setVisible(true);
            mDeleteAlbum.setVisible(false);
            if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_RECENT || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_VEDIOS
                    || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_MUSIC || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_APKS
                    || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_DOWNLOAD || CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
                mCreateFolderName.setVisible(false);
                mAddEncryptItem.setVisible(false);
                mSelectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            } else if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
                mCreateFolderName.setVisible(true);
                mSelectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            } else {
                mCreateFolderName.setVisible(true);
                mAddEncryptItem.setVisible(false);
                mSelectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
        }
        if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE) {
            if (SafeUtils.SAFE_ROOT_DIR.equals(text)) {
                mActionBarTv.setText(R.string.encrypted_files);
            }
        } else {
            mActionBarTv.setText(text);
        }
        mActionBarTv.setTextColor(Color.BLACK);
        mActionBarTv.setAlpha(1f);
    }

    public void setActionBarButtonText(int text) {
        mSelectAllTv.setText(text);
    }

    public void setActionBarButtonOnClickLitener(View.OnClickListener litener) {
        mSelectAllTv.setOnClickListener(litener);
        mEditCancelTv.setOnClickListener(litener);
    }

    /* MODIFIED-BEGIN by wenjing.ni, 2016-05-04,BUG-1956936*/
    protected void setActionBarButtonVisiable(boolean isVisiable) {
        if (isVisiable) {
            mSelectAllTitle.setVisibility(View.VISIBLE);
            mSelectAllTv.setVisibility(View.VISIBLE);
            mEditCancelTv.setVisibility(View.VISIBLE);
            mGlobalSearchBack.setVisibility(View.VISIBLE);
            mToolbar.setNavigationIcon(null);
        } else {
            mSelectAllTitle.setVisibility(View.GONE);
            mSelectAllTv.setVisibility(View.GONE);
            mEditCancelTv.setVisibility(View.GONE);
        }
    }

    public boolean isSystemLock() {
        /*
        LockPatternUtils mLockPatternUtils = new LockPatternUtils(this);
        int mUserId = getIntent().getIntExtra(Intent.EXTRA_USER_ID, UserHandle.myUserId());
        switch (mLockPatternUtils.getKeyguardStoredPasswordQuality(mUserId)) {
            case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                return true;
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX:
                return true;
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
                return true;
//            case DevicePolicyManager.PASSWORD_QUALITY_MANAGED:
//                return true;
//            case DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED:
//                return true;
        }*/
        //return false;
        return true;
    }

}
