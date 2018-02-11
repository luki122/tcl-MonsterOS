/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.adapter.FileInfoAdapter;
import cn.tcl.filemanager.adapter.ListFileInfoAdapter;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.service.FileManagerService;
import cn.tcl.filemanager.service.ListFileTask;
import cn.tcl.filemanager.service.ProgressInfo;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.PermissionUtil;
import cn.tcl.filemanager.view.HorizontalListView;

/* MODIFIED-BEGIN by zibin.wang, 2016-06-25,BUG-2383670*/
/* MODIFIED-END by zibin.wang,BUG-2383670*/

public class PathSelectionActivity extends FileBaseActivity implements OnItemClickListener {

    private final static String TAG = "PathSelectionActivity";

    private FileInfoManager mFileInfoManager;
    private static String mCurPath;
    private Intent mIntent;
    private int mTop = -1;
    private FileInfo mSelectedFileInfo;

    private Button mCancel;
    private Button mOk;
    private FileInfoAdapter mAdapter;
    private ListView mListView;

    private static final String RESULT_DIR_SEL = "result_dir_sel";
    private static final int ANDROID_PICK = 1;
    private static final int QRD_PICK = 2;

    private int mCurrentPick = -1;
    private String mCurFilePath;
    private LinearLayout btnLayout;
    /* MODIFIED-BEGIN by zibin.wang, 2016-06-25,BUG-2383670*/
    private LinearLayout mFilePathLayout;
    private HorizontalListView fileBrowserList;
    private String lastPath = "";
    private String[] paths;
    private DataAdapter pathBrowserAdapter;
    /* MODIFIED-END by zibin.wang,BUG-2383670*/
//    private Toolbar mMainToolbar = null;

    @Override
    public void setMainContentView() {
        setContentView(R.layout.select_path_main);
        LogUtils.getAppInfo(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = inflater.inflate(R.layout.file_select_actionbar, null);
        if(customActionBarView != null){
//          upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        //getSupportActionBar().setCustomView(customActionBarView,layoutParams);
            getActionBar().setDisplayShowTitleEnabled(false);
        }
        if(!getActionBar().isShowing()){
            getActionBar().show();
        }
        mCurPath = mMountPointManager.getRootPath();
        mIntent = getIntent();
        if (mIntent.getAction().equals(Intent.ACTION_PICK)) {
            mCurrentPick = ANDROID_PICK;
        } else if (mIntent.getAction().equals("com.android.fileexplorer.action.DIR_SEL")) {
            mCurrentPick = QRD_PICK;
        }

        mOk = (Button) findViewById(R.id.btn_ok);
        mOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (mCurrentPick == ANDROID_PICK) {
                    intent.putExtra(RESULT_DIR_SEL, mCurPath);
                } else if (mCurrentPick == QRD_PICK) {
                    intent.setData(Uri.fromFile(new File(mCurPath)));
                }
                LogUtils.i(
                        TAG,"mCurPath = " + mCurPath
                                + " ** mCurrentPick = " + mCurrentPick + " ** intent = "
                                + intent.getDataString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mCancel = (Button) findViewById(R.id.btn_cancel);
        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        btnLayout =(LinearLayout) findViewById(R.id.select_btn);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        mAdapter = new ListFileInfoAdapter(this, mFileInfoManager, mListView,true);//PR-1030556 Nicky Ni -001 20151203
        mListView.setAdapter(mAdapter);
        /* MODIFIED-BEGIN by zibin.wang, 2016-06-25,BUG-2383670*/
        mFilePathLayout = (LinearLayout) findViewById(R.id.file_path_layout);
        fileBrowserList = (HorizontalListView) findViewById(R.id.listview);
        pathBrowserAdapter = new DataAdapter(this);
        fileBrowserList.setAdapter(pathBrowserAdapter);
        fileBrowserList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isRtl = false;
                if (fileBrowserList != null) {
                    isRtl = fileBrowserList.isLayoutRtl();
                }
                if (isRtl) {
                    position = paths.length - 1 - position;
                }
                StringBuilder absolutePath = getAbsolutePath(position);

                if (position != paths.length - 1 && absolutePath != null) {
                    showDirectoryContent(absolutePath.toString());
                    updatePathBrowserText();
                }

            }
        });

        showDirectoryContent(mCurPath);
        updatePathBrowserText();
        /* MODIFIED-END by zibin.wang,BUG-2383670*/
    }

    @Override
    public void serviceConnected() {
        if (mFileInfoManager == null) {
            mFileInfoManager = mApplication.mService.initFileInfoManager(this);
        }
        setMainContentView();
    }

    private void showDirectoryContent(String path) {
        mCurPath = path;
        onPathChanged();
        if (mMountPointManager.isRootPath(mCurPath)) {
            btnLayout.setVisibility(View.GONE);
            showRootPathContent();
            return;
        }
        if (mApplication.mService != null) {
            //[FEATURE]-Add-BEGIN by TSNJ,qinglian.zhang,09/15/2014,PR-787616,
            btnLayout.setVisibility(View.VISIBLE);
            mApplication.mService.listFiles(this.getClass()
                            /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
                            .getName(), path,
                    new ListListener(), true, null, -1, ListFileTask.LIST_MODE_PATH_SELECT);
                    /* MODIFIED-END by haifeng.tang,BUG-1987329*/
            //[FEATURE]-Add-END by TSNJ
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        if(menu.getItemId() == android.R.id.home){
                onBackPressed();
                return true;
        }
        return false;
    }

    private class ListListener implements
            FileManagerService.OperationEventListener {

        @Override
        public void onTaskResult(int result) {
            mFileInfoManager.loadFileInfoList(
                    mCurPath,
                    mApplication.mSortType);
            mAdapter.refresh();
            mAdapter.notifyDataSetChanged();
            int seletedItemPosition = restoreSelectedPosition();
            restoreListPosition(seletedItemPosition);

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

    private int restoreSelectedPosition() {
        if (mSelectedFileInfo == null) {
            return -1;
        } else {
            int curSelectedItemPosition = mAdapter
                    .getPosition(mSelectedFileInfo);
            return curSelectedItemPosition;
        }
    }

    private void restoreListPosition(final int seletedItemPosition) {
        mListView.setAdapter(mAdapter);
        if (seletedItemPosition == -1) {
            mListView.setSelectionAfterHeaderView();
        } else if (seletedItemPosition >= 0
                && seletedItemPosition < mAdapter.getCount()) {

            if (mTop == -1) {
                mListView.setSelection(seletedItemPosition);
            } else {
                mListView.setSelectionFromTop(seletedItemPosition, mTop);
            }

        }
    }

    private void showRootPathContent() {
        List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
        List<FileInfo> mountFileList = mMountPointManager.getMountPointFileInfo();
        if (mountFileList != null) {
            fileInfoList.addAll(mountFileList);
        }
        if(mFileInfoManager != null && fileInfoList != null) {
            mFileInfoManager.addItemList(fileInfoList);
        }
        if(mFileInfoManager != null) {
            mFileInfoManager.loadFileInfoList(mCurPath,
                    mApplication.mSortType);
        }
        if(mAdapter != null) {
            mAdapter.refresh();
            mAdapter.notifyDataSetChanged();
        }

    }

    private void refreshButton() {
        if (mMountPointManager.isRootPath(mCurPath)) {
            mOk.setEnabled(false);
        } else {
            mOk.setEnabled(true);
        }
    }

    private void onPathChanged() {
        refreshButton();
        refreshPath(mCurPath);
    }

    private void refreshPath(String initFileInfo){
        mCurFilePath = initFileInfo;
        LogUtils.v("wye", "mCurFilePath=" + mCurFilePath); // MODIFIED by zibin.wang, 2016-05-11,BUG-2125562
        if (mCurFilePath != null) {
            if (!mMountPointManager.isRootPath(mCurFilePath)) {
                String path = mMountPointManager
                        .getDescriptionPath(mCurFilePath);
                if (path != null && !path.isEmpty()) {
                    String result = null;
                    if(path.contains(MountManager.SEPARATOR)){
                        result = path.substring(path.lastIndexOf(MountManager.SEPARATOR)+1);
                        setActionbarTitle(result);
                    }else{
                        setActionbarTitle(path);
                    }
                }
            }else{
                setActionbarTitle(R.string.theme_name);
            }
        }
    }

    private void backToRootPath() {
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }

        mCurPath = mMountPointManager.getRootPath();
    }

    @Override
    public void onBackPressed() {
        if (mMountPointManager.isRootPath(mCurPath)) {
            finish();
        } else if (mCurPath != null
                && mMountPointManager.isSdOrPhonePath(mCurPath)) {
            backToRootPath();
        } else {
            mCurPath = new File(mCurPath).getParent();
        }
        showDirectoryContent(mCurPath);
        updatePathBrowserText(); // MODIFIED by zibin.wang, 2016-06-25,BUG-2383670
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileInfo selecteItemFileInfo = (FileInfo) mAdapter.getItem(position);
        mSelectedFileInfo = selecteItemFileInfo;

        if (selecteItemFileInfo.isDirectory()) {
            int top = view.getTop();
            mTop = top;
            mSelectedFileInfo = (FileInfo) mAdapter.getItem(position);
            showDirectoryContent(selecteItemFileInfo.getFileAbsolutePath());
        }
        updatePathBrowserText(); // MODIFIED by zibin.wang, 2016-06-25,BUG-2383670
    }

    @Override
    public void onClick(View view) {
        if (mApplication.mService.isBusy(this.getClass().getName())) {
            return;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {

        if (PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT == requestCode) {
            for (String permission : permissions) {
                if (checkSelfPermission( permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.i("SNS", "No permission, " + permission);
                    if (CommonUtils.hasM()) {
                        PermissionUtil.setSecondRequestPermission(this);
                        finish();
                    }

                } else {
                    if (CommonUtils.hasM()) {
                    }
                }
            }
        }
    }
    /* MODIFIED-BEGIN by zibin.wang, 2016-05-11,BUG-2125562*/
    /*MODIFIED-BEGIN by wenjing.ni, 2016-04-19,BUG-1963197*/
    @Override
    public void onUnmounted(String mountPoint) {
        super.onUnmounted(mountPoint);
        if(mMountPointManager !=null) {
            mCurPath = mMountPointManager.getRootPath();
        }
        if(mCurPath != null) {
            showDirectoryContent(mCurPath);
        }
    }
    /*MODIFIED-END by wenjing.ni,BUG-1963197*/

    /*MODIFIED-BEGIN by wenjing.ni, 2016-04-19,BUG-1963197*/
    @Override
    public void onMounted() {
        super.onMounted();
        refreshRootAdapter();
    }

    protected void refreshRootAdapter() {
        if (mCurPath != null && mMountPointManager != null && mCurPath.equals(mMountPointManager.getRootPath())) {
            showDirectoryContent(mCurPath);
        }
    }
    /* MODIFIED-END by zibin.wang,BUG-2125562*/

    /* MODIFIED-BEGIN by zibin.wang, 2016-06-25,BUG-2383670*/
    private void resetFileBrowserListParams(int width, int maxListViewWidth) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) fileBrowserList.getLayoutParams();
        int listWidth = width < maxListViewWidth ? width : ViewGroup.LayoutParams.MATCH_PARENT;
        if (params == null) {
            params = new LinearLayout.LayoutParams(listWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            params.width = listWidth;
        }
        fileBrowserList.setLayoutParams(params);
    }

    class DataAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public DataAdapter(Context c) {
            mInflater = (LayoutInflater) c.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if (paths != null) {
                return paths.length;
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            boolean isRtl = false;
            if (fileBrowserList != null) {
                isRtl = fileBrowserList.isLayoutRtl();
            }
            if (isRtl && paths != null) {
                position = paths.length - 1 - position;
            }
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.horizontallist_item, null);
                holder = new ViewHolder();
                holder.file_name = (TextView) convertView.findViewById(R.id.horizontallist_item_path);
                holder.file_name_hight = (TextView) convertView.findViewById(R.id.horizontallist_item_hight);
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

    private StringBuilder getAbsolutePath(int position) {
        StringBuilder absolutePath = new StringBuilder();
        String rootPath = null;
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }

        String[] temp=mMountPointManager.getDescriptionPath(mCurPath).split(File.separator);
        if (temp[0].equals(getResources().getString(R.string.phone_storage_cn))){
            rootPath=mMountPointManager.getPhonePath();
        }else if(temp[0].equals(getResources().getString(R.string.usbotg_m))) { // MODIFIED by songlin.qi, 2016-06-08,BUG-2278011
            rootPath= mMountPointManager.getUsbOtgPath();
        }else if (temp[0].equals(getResources().getString(R.string.sd_card))){
            rootPath=mMountPointManager.getSDCardPath();
        }

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

        return absolutePath;
    }

    public void updatePathBrowserText() {
        if (mMountPointManager.isRootPath(mCurPath)) {
            if (fileBrowserList != null) {
                fileBrowserList.setVisibility(View.GONE);
            }
            return;
        }

        if (fileBrowserList != null) {
            fileBrowserList.setVisibility(View.VISIBLE);
        }
        String currentPath = mMountPointManager.getDescriptionPath(mCurPath);
        if ((currentPath != null && lastPath == null) || !currentPath.equals(lastPath)) {
            paths = currentPath.split(File.separator);
            lastPath = currentPath;
            if (pathBrowserAdapter == null) {
                pathBrowserAdapter = new DataAdapter(this);
            }
            if (fileBrowserList == null) {
                return;
            }

            fileBrowserList.setAdapter(pathBrowserAdapter);
            int width = CommonUtils.getTotalWidthofListView(fileBrowserList);
            int maxListViewWidth = getMaxListViewWidth();
            resetFileBrowserListParams(width, maxListViewWidth);
            if (width >= maxListViewWidth) {
                fileBrowserList.count = 0;
                fileBrowserList.setSelection(fileBrowserList.getWidth());
            }
        }
    }

    private int getMaxListViewWidth() {
        return CommonUtils.getScreenWidth(this) - mFilePathLayout.getPaddingStart() - mFilePathLayout.getPaddingEnd();
    }
    /* MODIFIED-END by zibin.wang,BUG-2383670*/
}
