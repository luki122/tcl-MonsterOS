package com.monster.market.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.market.R;
import com.monster.market.adapter.DownloadManagerAdapter;
import com.monster.market.db.AppDownloadDao;
import com.monster.market.download.AppDownloadData;
import com.monster.market.download.AppDownloadService;
import com.monster.market.download.AppDownloader;
import com.monster.market.download.DownloadInitListener;
import com.monster.market.download.DownloadManagerBean;
import com.monster.market.download.DownloadUpdateListener;
import com.monster.market.install.InstallNotification;
import com.monster.market.utils.LogUtil;
import com.monster.market.views.stickylistheaders.StickyListHeadersListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import mst.app.dialog.AlertDialog;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.MstListView;
import mst.widget.SliderLayout;

/**
 * Created by xiaobin on 16-8-18.
 */
public class DownloadManagerActivity extends BaseActivity {

    public static final String TAG = "DownloadManagerActivity";

    public static final int DOWNLOADED_MAX_COUNT = 200;
    public StickyListHeadersListView mListView;
    private LinearLayout ll_nodata;
    private Button btn_goto;
    private RelativeLayout rl_edit_delete;

    private List<DownloadManagerBean> list;
    private List<DownloadManagerBean> downloadingList;
    private List<DownloadManagerBean> downloadedList;
    private DownloadManagerAdapter adapter;

    private ActionMode actionMode;
    private boolean editMode = false; // 编辑模式

    private boolean stopFlag = false;

    private int showMoreCount = 10;
    private boolean needShowMore = true;
    private boolean hasShowExpansion = false;
    private boolean isSelectAll = false;
    private boolean alowUpdate = true;

    private int isOpenInstall = 0;

    private String mFailPackageName = null;
    private int mInitListPos = -1;

    private long lastRefresh;
    public int flagPostion = -1;

    private boolean showDialog = false; // 是否正在显示dialog

    private final long openOrCloseEditModeDelay = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_download_manager);

        getIntentData();

        initViews();
        setListener();
        AppDownloadService.checkInit(this, new DownloadInitListener() {

            @Override
            public void onFinishInit() {
                initData();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (stopFlag) {
            updateListener.downloadProgressUpdate();
            stopFlag = false;
        }
        AppDownloadService.registerUpdateListener(updateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopFlag = true;
        AppDownloadService.unRegisterUpdateListener(updateListener);

        if (adapter != null) {
            adapter.clearProgressBtnTag(mListView);
        }
    }

    @Override
    public void initViews() {
        mToolbar = getToolbar();
        mToolbar.setTitle(R.string.download_manager_pref);

        actionMode = getActionMode();
        actionMode.bindActionModeListener(new ActionModeListener() {
            @Override
            public void onActionItemClicked(ActionMode.Item item) {
                if (item.getItemId() == ActionMode.NAGATIVE_BUTTON) {
                    if (editMode) {
                        alowUpdate = false;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                alowUpdate = true;
                            }
                        }, openOrCloseEditModeDelay);

                        closeEditMode();
                    }
                } else if (item.getItemId() == ActionMode.POSITIVE_BUTTON) {
                    if (isSelectAll) {
                        adapter.getSelectSet().clear();
                        adapter.notifyDataSetChanged();
                    } else {
                        for (DownloadManagerBean bean : list) {
                            if (bean.getDownloadData() != null) {
                                adapter.getSelectSet().add(bean.getDownloadData()
                                                .getTaskId());
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                    checkSelect();
                }
            }

            @Override
            public void onActionModeShow(ActionMode actionMode) {

            }

            @Override
            public void onActionModeDismiss(ActionMode actionMode) {

            }
        });

        ll_nodata = (LinearLayout) findViewById(R.id.ll_nodata);
        btn_goto = (Button) findViewById(R.id.btn_goto);
        mListView = (StickyListHeadersListView) findViewById(R.id.mListView);
        rl_edit_delete = (RelativeLayout) findViewById(R.id.rl_edit_delete);
    }

    @Override
    public void initData() {
        list = new ArrayList<DownloadManagerBean>();
        downloadingList = new ArrayList<DownloadManagerBean>();
        downloadedList = new ArrayList<DownloadManagerBean>();
        adapter = new DownloadManagerAdapter(this, list);
        mListView.setAdapter(adapter);
        updateData(true);
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }

    private void getIntentData() {
        InstallNotification.cancelInstalledNotify();
        InstallNotification.cancelInstallFailedNotify();
        InstallNotification.cancelInstallingNotify();
        InstallNotification.cancelUpdateInstalledNotify();
        InstallNotification.cancelUpdateInstallFailedNotify();

        isOpenInstall = getIntent().getIntExtra("openinstall", 0);

        if (isOpenInstall == 1) {
            InstallNotification.install_success.clear();
        } else if (isOpenInstall == 2) {
            mFailPackageName = getIntent().getStringExtra("packageName");
            InstallNotification.install_failed.clear();
        } else if (isOpenInstall == 3) {
            InstallNotification.update_success.clear();
        } else if (isOpenInstall == 4) {
            mFailPackageName = getIntent().getStringExtra("packageName");
            InstallNotification.update_failed.clear();
        }
        Bundle myBundle = getIntent().getExtras();
        if (myBundle != null) {
            ArrayList<AppDownloadData> upLists = myBundle.getParcelableArrayList("updatedata");
            if (upLists != null) {
                for (int i = 0; i < upLists.size(); i++) {
                    AppDownloadService.startDownload(this, upLists.get(i));
                }
                InstallNotification.cancelUpdateNotify();
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (editMode) {
            alowUpdate = false;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    alowUpdate = true;
                }
            }, openOrCloseEditModeDelay);

            closeEditMode();
        } else {
            super.onBackPressed();
        }
    }

    // ============处理StickyListHeadersListView头部悬浮点击事件问题(start)============

    private int screenHeadHeight = 0; // 通知栏高度
    private int screenWidth = 0; // 屏幕宽度

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (screenHeadHeight == 0) {
            Rect frame = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            screenHeadHeight = frame.top;
        }
        if (screenWidth == 0) {
            screenWidth = getResources().getDisplayMetrics().widthPixels;
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mListView.getHeaderBottomPosition() != 0
                    && ev.getRawY() > (mToolbar.getHeight() + screenHeadHeight)
                    && ev.getRawY() < (mToolbar.getHeight()
                    + mListView.getHeaderBottomPosition() + screenHeadHeight)) {

                if (ev.getRawX() > screenWidth * (4 / (5 * 1.0f))
                        && mListView.getmCurrentHeaderId() == DownloadManagerBean.TYPE_DOWNLOADED) {
                    closeSliderView(mListView, true);
                    showDeleteAllFinishDialog();
                }
                return false;

            }
        }

        return super.dispatchTouchEvent(ev);
    }

    // ============处理StickyListHeadersListView头部悬浮点击事件问题(end)============

    private void setListener() {
        btn_goto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DownloadManagerActivity.this,
                        MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        mListView.setOnHeaderClickListener(new StickyListHeadersListView.OnHeaderClickListener() {

            @Override
            public void onHeaderClick(StickyListHeadersListView l, View header,
                                      int itemPosition, long headerId, boolean currentlySticky) {
                if (currentlySticky) {
                    long id = adapter.getHeaderId(itemPosition);
                    if (id == DownloadManagerBean.TYPE_DOWNLOADING) {

                    } else {
                        showDeleteAllFinishDialog();
                    }
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long arg3) {
                if (hasShowExpansion && position == list.size() - 1) {
                    hasShowExpansion = false;
                    needShowMore = false;
                    closeSliderView(true);
                    updateData(true);
                } else {
                    if (editMode) {
                        if (!adapter.getSelectSet().contains(list.get(position)
                                        .getDownloadData().getTaskId())) {
                            adapter.getSelectSet().add(
                                    list.get(position).getDownloadData()
                                            .getTaskId());

                            CheckBox cb = (CheckBox) view
                                    .findViewById(R.id.checkbox);
                            cb.setChecked(true);
                        } else {
                            adapter.getSelectSet().remove(
                                    list.get(position).getDownloadData()
                                            .getTaskId());

                            CheckBox cb = (CheckBox) view
                                    .findViewById(R.id.checkbox);
                            cb.setChecked(false);
                        }
                        checkSelect();
                    }
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                if (hasShowExpansion && position == list.size() - 1) {
                    return false;
                }

                if (!editMode) {
                    adapter.getSelectSet().add(
                            list.get(position).getDownloadData().getTaskId());

                    alowUpdate = false;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alowUpdate = true;
                        }
                    }, openOrCloseEditModeDelay);

                    openEditMode();
                }

                return true;
            }
        });

        rl_edit_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectDeleteDialog();
            }
        });
    }

    private void updateData(final boolean notify) {
        new Thread() {
            @Override
            public void run() {

                long start = System.currentTimeMillis();

                List<DownloadManagerBean> tempList = new ArrayList<DownloadManagerBean>();

                // 获取下载管理列表
                List<DownloadManagerBean> tempDownloadingList = new ArrayList<DownloadManagerBean>();
                Map<String, AppDownloader> map = AppDownloadService.getDownloaders();
                if (map == null) {
                    return;
                }
                List<AppDownloader> downloaders = new ArrayList<AppDownloader>();
                for (String key : map.keySet()) {
                    downloaders.add(map.get(key));
                }

                for (AppDownloader downloader : downloaders) {
                    if (!(downloader.getStatus() >= AppDownloader.STATUS_INSTALL_WAIT)) {
                        DownloadManagerBean bean = new DownloadManagerBean();
                        bean.setType(DownloadManagerBean.TYPE_DOWNLOADING);
                        bean.setDownloadData(downloader.getAppDownloadData());
                        long fileSize = downloader.getFileSize();
                        long downloadSize = downloader.getDownloadSize();
                        int progress = (int) ((float) (downloadSize * 1.0)
                                / (fileSize * 1.0) * 100);
                        bean.setFileSize(fileSize);
                        bean.setDownloadSize(downloadSize);
                        bean.setProgress(progress);
                        bean.setDownloadStatus(downloader.getStatus());
                        bean.setCreateTime(downloader.getCreateTime());
                        tempDownloadingList.add(bean);
                    }
                }

                sortDownloadingList(tempDownloadingList);
//				downloadingList.clear();
//				downloadingList.addAll(tempDownloadingList);

                // 获取已完成列表
                List<DownloadManagerBean> tempDownloadedList = new ArrayList<DownloadManagerBean>();
                AppDownloadDao downloadDao = AppDownloadService.getAppDownloadDao();
                List<AppDownloadData> tempDownloaded = downloadDao.getDownloadedApp();
                for (AppDownloadData data : tempDownloaded) {
                    String filePath = data.getFileDir() + File.separator
                            + data.getFileName();
                    DownloadManagerBean bean = new DownloadManagerBean();
                    bean.setType(DownloadManagerBean.TYPE_DOWNLOADED);
                    bean.setDownloadData(data);
                    bean.setFilePath(filePath);
                    bean.setDownloadStatus(data.getStatus());

                    tempDownloadedList.add(bean);
                }

                sortDownloadedList(tempDownloadedList);
                // 检查并删除多余的下载记录
                checkAndDeleteDownloadedTask(tempDownloadedList);

//				downloadedList.clear();
//				downloadedList.addAll(tempDownloadedList);

//				list.clear();
//				list.addAll(downloadingList);
                tempList.addAll(tempDownloadingList);

                if (needShowMore) {
                    if (tempDownloadedList.size() > showMoreCount) {
                        hasShowExpansion = true;
                        for (int i = 0; i < showMoreCount; i++) {
                            tempList.add(tempDownloadedList.get(i));
                        }
                        DownloadManagerBean bean = new DownloadManagerBean();
                        bean.setType(DownloadManagerBean.TYPE_DOWNLOADED);
                        tempList.add(bean);
                    } else {
                        tempList.addAll(tempDownloadedList);
                        needShowMore = false;
                    }
                } else {
                    tempList.addAll(tempDownloadedList);
                }


                mInitListPos = locateFailIntalledApp(mFailPackageName, tempList);

                MessageObject obj = new MessageObject(tempList, tempDownloadingList, tempDownloadedList, notify);
                handler.sendMessage(handler.obtainMessage(100, obj));


            }
        }.start();
    }

    private int locateFailIntalledApp(String pPackageName, List<DownloadManagerBean> pList){

        if (pPackageName != null && pList != null) {
            for (int i = 0; i < pList.size(); i++) {
                if (pList.get(i).getDownloadData() != null) {
                    if (pPackageName.equals(pList.get(i).getDownloadData()
                            .getPackageName())) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    public static final class MessageObject {
        protected List<DownloadManagerBean> tempList;
        protected List<DownloadManagerBean> tempDownloadingList;
        protected List<DownloadManagerBean> tempDownloadedList;
        protected boolean notify;

        public MessageObject(List<DownloadManagerBean> tempList,
                             List<DownloadManagerBean> tempDownloadingList,
                             List<DownloadManagerBean> tempDownloadedList,
                             boolean notify) {
            this.tempList = tempList;
            this.tempDownloadingList = tempDownloadingList;
            this.tempDownloadedList = tempDownloadedList;
            this.notify = notify;
        }
    }

    private void sortDownloadingList(List<DownloadManagerBean> downloadingList) {
        Collections.sort(downloadingList,
                new Comparator<DownloadManagerBean>() {
                    @Override
                    public int compare(DownloadManagerBean bean1,
                                       DownloadManagerBean bean2) {
                        if (bean1.getCreateTime() < bean2.getCreateTime()) {
                            return -1;
                        } else if (bean1.getCreateTime() > bean2
                                .getCreateTime()) {
                            return 1;
                        }
                        return 0;
                    }
                });
    }

    private void sortDownloadedList(List<DownloadManagerBean> downloadedList) {
        Collections.sort(downloadedList, new Comparator<DownloadManagerBean>() {
            @Override
            public int compare(DownloadManagerBean bean1,
                               DownloadManagerBean bean2) {
                if (bean1.getDownloadData().getFinishTime() > bean2
                        .getDownloadData().getFinishTime()) {
                    return -1;
                } else if (bean1.getDownloadData().getFinishTime() < bean2
                        .getDownloadData().getFinishTime()) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private void checkAndDeleteDownloadedTask(List<DownloadManagerBean> downloadedList) {
        if (downloadedList != null && downloadedList.size() > DOWNLOADED_MAX_COUNT) {
            int deleteCount = downloadedList.size() - DOWNLOADED_MAX_COUNT;
            List<DownloadManagerBean> deleteList = downloadedList
                    .subList(downloadedList.size() - deleteCount, downloadedList.size());
            AppDownloadDao dao = AppDownloadService.getAppDownloadDao();
            for (DownloadManagerBean bean : deleteList) {
                dao.delete(bean.getDownloadData().getTaskId());
            }
            downloadedList.removeAll(deleteList);
        }
    }

    private void openEditMode() {
        closeSliderView(mListView, true);
        lockSliderView(mListView, true);

        rl_edit_delete.setVisibility(View.VISIBLE);

        if (hasShowExpansion && needShowMore) {
            hasShowExpansion = false;
            needShowMore = false;
            updateData(true);
        }

        editMode = true;

        if (!isActionModeShowing()) {
            showActionMode(true);
        }

        adapter.setEditMode(true);
        adapter.setNeedAnim(true);
        adapter.notifyDataSetChanged();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.setNeedAnim(false);
            }
        }, 1);

        checkSelect();
    }

    /**
     * @Title: closeEditMode
     * @Description: 关闭编辑模式
     * @param
     * @return void
     * @throws
     */
    private void closeEditMode() {
        editMode = false;

        mListView.setLongClickable(true);
        mListView.setSelector(R.drawable.list_item_selector);
        lockSliderView(mListView, false);

        rl_edit_delete.setVisibility(View.GONE);

        showActionMode(false);

        for (int i = 0; i < mListView.getChildCount(); i++) {
            CheckBox cb = (CheckBox) mListView.getChildAt(i)
                    .findViewById(R.id.checkbox);
            cb.setChecked(false);
            cb.postInvalidate();
        }
        adapter.getSelectSet().clear();

        adapter.setEditMode(false);
        adapter.setNeedAnim(true);
        adapter.notifyDataSetChanged();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.setNeedAnim(false);
            }
        }, 1);
    }

    /**
     * @Title: checkSelect
     * @Description: 检查是否全选状态
     * @param
     * @return void
     * @throws
     */
    private void checkSelect() {
        int allCount = list.size();
        if (needShowMore) {
            allCount = allCount - 1;
        }
        if (adapter.getSelectSet().size() == allCount) {
            isSelectAll = true;
        } else {
            isSelectAll = false;
        }
        if (isSelectAll) {
            actionMode.setPositiveText(getString(R.string.downloadman_reverse_selection));
        } else {
            actionMode.setPositiveText(getString(R.string.downloadman_select_all));
        }
        if (adapter.getSelectSet().size() == 0) {

        } else {

        }
    }


    private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
        @Override
        public void downloadProgressUpdate() {
            if (alowUpdate) {
                updateData(true);
                lastRefresh = System.currentTimeMillis();
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            MessageObject obj = (MessageObject) msg.obj;
            downloadingList.clear();
            downloadingList.addAll(obj.tempDownloadingList);

            downloadedList.clear();
            downloadedList.addAll(obj.tempDownloadedList);

            list.clear();
            list.addAll(obj.tempList);

            if (list.size() == 0) {
                ll_nodata.setVisibility(View.VISIBLE);
            } else {
                ll_nodata.setVisibility(View.GONE);
            }

            adapter.setDownloadingCount(downloadingList.size());
            adapter.setDownloadedCount(downloadedList.size());
            adapter.updateSectionIndice();
            if (obj.notify) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.updateListData(mListView, list);
            }
            if ((isOpenInstall != 0) && (downloadedList.size() > 0)
                    && (downloadingList.size() > 0)) {
                isOpenInstall = 0;
                int size = downloadingList.size();
                mListView.setSelection(size);

            }

            if(mInitListPos > 0){
                mListView.setSelection(mInitListPos);
            }

        }
    };

    private DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface arg0) {
            showDialog = false;
            closeSliderView(mListView, true);
        }
    };

    /**
     * @Title: showDeleteDialog
     * @Description: 显示删除单个任务对话框
     * @param @param bean
     * @return void
     * @throws
     */
    public void showDeleteDialog(final DownloadManagerBean bean) {
        if (showDialog) {
            return;
        }


        if (bean.getDownloadData() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.downloadman_dlg_delete_confirm));

        View alertDialogView;
        TextView tv_message;
        final CheckBox checkbox;

        alertDialogView = getLayoutInflater().inflate(
                R.layout.alert_dialog_delete_confirm, null);
        tv_message = (TextView) alertDialogView.findViewById(R.id.message);
        checkbox = (CheckBox) alertDialogView.findViewById(R.id.checkbox);

        checkbox.setChecked(true);
        if (bean.getDownloadStatus() >= AppDownloader.STATUS_INSTALL_WAIT) {
            tv_message.setText(getString(R.string.downloadman_delete_the_record));
            File file = new File(bean.getFilePath());
            if (file.exists()) {
                checkbox.setVisibility(View.VISIBLE);
            } else {
                checkbox.setVisibility(View.GONE);
            }
        } else {
            tv_message.setText(getString(R.string.downloadman_delete_the_task));
            checkbox.setVisibility(View.GONE);
        }

        builder.setView(alertDialogView);
        builder.setPositiveButton(getString(R.string.dialog_confirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (bean.getDownloadData() != null) {
                            if (bean.getDownloadStatus() >= AppDownloader.STATUS_INSTALL_WAIT) { // 删除已完成任务
                                if (checkbox.isChecked()) {
                                    File file = new File(bean.getFilePath());
                                    file.delete();
                                }
                                AppDownloadService.getAppDownloadDao().delete(
                                        bean.getDownloadData().getTaskId());
                                AppDownloadService.updateDownloadProgress();
                            } else {
                                AppDownloadService.cancelDownload(
                                        DownloadManagerActivity.this, bean.getDownloadData());
                            }

                            updateData(true);
                        }

                    }
                });
        builder.setNegativeButton(getString(R.string.dialog_cancel), null);
        builder.setOnDismissListener(dismissListener);
        builder.create().show();

        showDialog = true;
    }

    /**
     * @Title: showSelectDeleteDialog
     * @Description: 显示删除已选择对话框
     * @param
     * @return void
     * @throws
     */
    private void showSelectDeleteDialog() {
        if (showDialog) {
            return;
        }

        if (adapter.getSelectSet().size() == 0) {
            Toast.makeText(this, getString(R.string.downloadman_no_select),
                    Toast.LENGTH_SHORT).show();
        } else {
            boolean hasFinishAndExist = false;
            for (String i : adapter.getSelectSet()) {
                for (DownloadManagerBean bean : list) {
                    if (bean.getDownloadData() != null
                            && (bean.getDownloadData().getTaskId().equals(i))) {
                        // 已完成
                        if (bean.getDownloadStatus() >= AppDownloader.STATUS_INSTALL_WAIT) {
                            File file = new File(bean.getFilePath());
                            if (file.exists()) {
                                hasFinishAndExist = true;
                                break;
                            }
                        }
                    }
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(
                    this);
            builder.setTitle(getString(R.string.downloadman_dlg_delete_confirm));

            View alertDialogView;
            TextView tv_message;
            final CheckBox checkbox;

            alertDialogView = getLayoutInflater().inflate(
                    R.layout.alert_dialog_delete_confirm, null);
            tv_message = (TextView) alertDialogView.findViewById(R.id.message);
            checkbox = (CheckBox) alertDialogView.findViewById(R.id.checkbox);

            tv_message.setText(getString(R.string.downloadman_delete_select_task_and_record));
            checkbox.setChecked(true);

            if (hasFinishAndExist) {
                checkbox.setVisibility(View.VISIBLE);
            } else {
                checkbox.setVisibility(View.GONE);
            }

            builder.setView(alertDialogView);
            builder.setPositiveButton(getString(R.string.dialog_confirm),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            selectDelete(checkbox.isChecked());
                        }
                    });
            builder.setNegativeButton(getString(R.string.dialog_cancel), null);
            builder.setOnDismissListener(dismissListener);
            builder.create().show();

            showDialog = true;
        }
    }


    public void showDeleteAllFinishDialog() {
        if (editMode) {
            return;
        }

        if (showDialog) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.downloadman_dlg_delete_confirm));

        View alertDialogView;
        TextView tv_message;
        final CheckBox checkbox;

        alertDialogView = getLayoutInflater().inflate(
                R.layout.alert_dialog_delete_confirm, null);
        tv_message = (TextView) alertDialogView.findViewById(R.id.message);
        checkbox = (CheckBox) alertDialogView.findViewById(R.id.checkbox);

        tv_message.setText(getString(R.string.downloadman_clear_record));
        checkbox.setChecked(true);

        boolean existFile = false;
        for (DownloadManagerBean bean : downloadedList) {
            if (bean.getType() == DownloadManagerBean.TYPE_DOWNLOADED) {
                File file = new File(bean.getFilePath());
                if (file.exists()) {
                    existFile = true;
                    break;
                }
            }
        }
        if (existFile) {
            checkbox.setVisibility(View.VISIBLE);
        } else {
            checkbox.setVisibility(View.GONE);
        }

        builder.setView(alertDialogView);
        builder.setPositiveButton(getString(R.string.dialog_confirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        DownloadManagerBean bean = null;
                        AppDownloadDao dao = AppDownloadService
                                .getAppDownloadDao();
                        for (int i = 0; i < downloadedList.size(); i++) {
                            bean = downloadedList.get(i);
                            if (bean.getType() == DownloadManagerBean.TYPE_DOWNLOADED) {
                                if (checkbox.isChecked() && checkbox.getVisibility() == View.VISIBLE) {
                                    File file = new File(bean.getFilePath());
                                    file.delete();
                                }
                                dao.delete(bean.getDownloadData().getTaskId());
                            }
                        }
                        updateData(true);
                    }
                });
        builder.setNegativeButton(getString(R.string.dialog_cancel), null);
        builder.setOnDismissListener(dismissListener);
        builder.create().show();

        showDialog = true;
    }

    /**
     * @Title: selectDelete
     * @Description: 选择后删除操作
     * @param
     * @return void
     * @throws
     */
    public void selectDelete(boolean check) {
        alowUpdate = false;
        // 需要移除的列表
        List<DownloadManagerBean> tempDelete = new ArrayList<DownloadManagerBean>();

        for (String i : adapter.getSelectSet()) {
            for (DownloadManagerBean bean : list) {
                if (bean.getDownloadData() != null
                        && (bean.getDownloadData().getTaskId().equals(i))) {
                    tempDelete.add(bean);
                    // 已完成
                    if (bean.getDownloadStatus() >= AppDownloader.STATUS_INSTALL_WAIT) {
                        if (check) {
                            File file = new File(bean.getFilePath());
                            file.delete();
                        }
                        AppDownloadService.getAppDownloadDao().delete(
                                bean.getDownloadData().getTaskId());
                    } else {
                        AppDownloadService.cancelDownload(this,
                                bean.getDownloadData());
                    }
                    break;
                }
            }
        }

        if (tempDelete.size() > 0) {
            list.removeAll(tempDelete);
            adapter.notifyDataSetChanged();
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                alowUpdate = true;
                AppDownloadService.updateDownloadProgress();
            }
        }, openOrCloseEditModeDelay);

        closeEditMode();
    }

    private void closeSliderView(MstListView listView, boolean anim) {
        if (listView == null) {
            return;
        }

        int count = listView.getChildCount();

        for (int i = 0; i < count; i++) {
            View view = listView.getChildAt(i);
            SliderLayout sliderLayout = (SliderLayout) view.findViewById(com.mst.R.id.slider_view);
            if (sliderLayout != null && !sliderLayout.isClosed()) {
                sliderLayout.close(anim);
            }
        }
    }

    private void lockSliderView(MstListView listView, boolean locked) {
        if (listView == null) {
            return;
        }

        int count = listView.getChildCount();

        for (int i = 0; i < count; i++) {
            View view = listView.getChildAt(i);
            SliderLayout sliderLayout = (SliderLayout) view.findViewById(com.mst.R.id.slider_view);
            if (sliderLayout != null) {
                sliderLayout.setLockDrag(locked);
            }
        }
    }

    public void closeSliderView(boolean anim) {
        closeSliderView(mListView, anim);
    }

    public void lockSliderView(boolean locked) {
        lockSliderView(mListView, locked);
    }

}
