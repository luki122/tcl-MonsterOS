/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.activity.FileBrowserActivity;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.IconManager;
import cn.tcl.filemanager.manager.SafeManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.view.FileItemView;
import mst.widget.SliderView;
import cn.tcl.filemanager.activity.FileBaseActivity.deleteFileInfo;

/*MODIFIED-BEGIN by haifeng.tang, 2016-04-14,BUG-1939180*/


public class DownLoadListFileInfoAdapter extends FileInfoAdapter {

    private static final String TAG = DownLoadListFileInfoAdapter.class.getSimpleName();
    /*MODIFIED-END by haifeng.tang,BUG-1939180*/
    private ListView mListView;
    private int xoff;
    private int yoff;
    private boolean isThirdAPP = false;
    private deleteFileInfo mDelFileInfo;
    private boolean isClearCheckedList = false;

    /**
     * The constructor to construct a FileInfoAdapter.
     *
     * @param context            the context of FileManagerActivity
     * @param fileManagerService the service binded with FileManagerActivity
     * @param fileInfoManager    a instance of FileInfoManager, which manages all
     *                           files.
     */
    public DownLoadListFileInfoAdapter(Context context, FileInfoManager fileInfoManager, ListView listView, deleteFileInfo delFileInfo) {
        super(context, fileInfoManager);
        mDelFileInfo = delFileInfo;
        mListView = listView;
        xoff = context.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_listxoff);
        yoff = context.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_listyoff);
    }

    /**
     * This method gets the view to be displayed
     *
     * @param pos         the position of the item
     * @param convertView the view to be shown
     * @param parent      the parent view
     * @return the view to be shown
     */
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        return getListView(pos, convertView, parent);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };
    /**
     * This method gets the list view to be desplayed in list view.
     *
     * @param pos         the position of the item
     * @param convertView convertView the view to be shown
     * @param parent      parent the parent view
     * @return the list view to be shown
     */
    public View getListView(int pos, View convertView, ViewGroup parent) {

        final FileItemView viewHolder;
        final int position = pos;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_download_file_list, null);
            viewHolder = new FileItemView(
                    (SliderView) convertView.findViewById(R.id.slider_view1),
                    (TextView) convertView.findViewById(R.id.file_list_item_file_name),
                    (RelativeLayout) convertView.findViewById(R.id.file_download_success_layout),
                    (TextView) convertView.findViewById(R.id.edit_adapter_time),
                    (TextView) convertView.findViewById(R.id.edit_adapter_size),
                    (RelativeLayout) convertView.findViewById(R.id.file_download_running_layout),
                    (ProgressBar) convertView.findViewById(R.id.download_progressBar),
                    (TextView) convertView.findViewById(R.id.edit_adapter_current_size),
                    (TextView) convertView.findViewById(R.id.edit_adapter_percent),
                    (ImageView) convertView.findViewById(R.id.edit_adapter_img),
                    (CheckBox) convertView.findViewById(R.id.edit_checkbox), (ImageView) convertView.findViewById(R.id.ic_arrow));

            LayoutParams params = (LayoutParams) viewHolder.getName().getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            viewHolder.getName().setLayoutParams(params);
            viewHolder.getName().setEllipsize(TextUtils.TruncateAt.MIDDLE);

            viewHolder.getSliderView().addTextButton(1, mContext.getString(R.string.delete));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FileItemView) convertView.getTag();
        }

        FileInfo currentItem = getItem(pos);
        // ADD START FOR PR1044907 BY HONGBIN.CHEN 20150721
        if (currentItem != null) {
            LogUtils.e(this.getClass().getName(), "currentItem:" + currentItem.getFileName() + ",mod:" + currentItem.getFileLastModifiedTime());
            setNameText(viewHolder.getName(), currentItem);
            SliderView mSliderView = viewHolder.getSliderView();
            TextView mSize = viewHolder.getSize();
            ImageView mIcon = viewHolder.getIcon();
            CheckBox mCheck = viewHolder.getCheckBox();
            RelativeLayout successLayout = viewHolder.getSuccessLayout();
            RelativeLayout runningLayout = viewHolder.getRunningLayout();
            ProgressBar progressBar = viewHolder.getDownloadProgressBar();
            TextView currentSize = viewHolder.getCurrentSize();
            TextView percent = viewHolder.getPercent();
            String filePath = currentItem.getFileAbsolutePath();
            mIcon.setTag("icon" + filePath);
            mIcon.setScaleType(ScaleType.CENTER_INSIDE);
            successLayout.setTag("success" + filePath);
            runningLayout.setTag("running" + filePath);
            progressBar.setTag("bar" + filePath);
            currentSize.setTag("currentsize" + filePath);
            percent.setTag("percent" + filePath);
            mCheck.setTag("check" + filePath);

            mSliderView.setOnSliderButtonClickListener(new SliderView.OnSliderButtonLickListener() {
                @Override
                public void onSliderButtonClick(int i, View view, ViewGroup viewGroup) {
                    LogUtils.i(TAG, "onSliderButtonClick:" + pos);
                    if (null != mDelFileInfo) {
                        mDelFileInfo.deleteFile(currentItem);
                        mSliderView.close(true);
                        isClearCheckedList = true;
                    }
                }
            });
            if(isClearCheckedList){
                mCheckedFileList.clear();
                isClearCheckedList = false;
            }
            mSliderView.setLockDrag(false);

            if (currentItem.getTotalBytes() == currentItem.getCurrentBytes()) {
                successLayout.setVisibility(View.VISIBLE);
                runningLayout.setVisibility(View.GONE);
            } else {
                setRunningInfo(currentItem, viewHolder.getDownloadProgressBar(), viewHolder.getPercent(), viewHolder.getCurrentSize());
                successLayout.setVisibility(View.GONE);
                runningLayout.setVisibility(View.VISIBLE);
            }
            switch (mMode) {
                case MODE_EDIT:
                    LogUtils.i(TAG, "MODE_EDIT");
                    if (SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN) {
                        if (currentItem.isDirectory()) {
                            viewHolder.mArrow.setVisibility(View.VISIBLE);
                            mCheck.setVisibility(View.GONE);
                        } else {
                            viewHolder.mArrow.setVisibility(View.GONE);
                            mCheck.setVisibility(View.VISIBLE);
                        }
                    } else {
                        mCheck.setVisibility(View.VISIBLE);
                        viewHolder.mArrow.setVisibility(View.GONE);
                    }

                    viewHolder.getCheckBox().setChecked(mCheckedFileList.contains(currentItem));
                    setTimeSizeTextForDownload(viewHolder.getTime(), viewHolder.getSize(), currentItem);

                    mSliderView.close(true);
                    mSliderView.setLockDrag(true);
                    break;
                case MODE_NORMAL:
                    LogUtils.i(TAG,"MODE_NORMAL");
                    mCheck.setVisibility(View.GONE);
                    mCheck.setChecked(false); //MODIFIED by haifeng.tang, 2016-04-14,BUG-1939180
                    setArrowVisiable(viewHolder, currentItem);
                    setTimeSizeTextForDownload(viewHolder.getTime(), viewHolder.getSize(), currentItem);
                    break;
                case MODE_SEARCH:
                    LogUtils.i(TAG,"MODE_SEARCH");
                    viewHolder.getCheckBox().setVisibility(View.GONE);
                    viewHolder.mArrow.setVisibility(View.VISIBLE);
                    setTimeSizeTextForDownload(viewHolder.getTime(), viewHolder.getSize(), currentItem);
                    setSearchHighLight(viewHolder.getName(), ((FileBrowserActivity) mContext).getQueryText());
                    break;
                case MODE_GLOBALSEARCH:
                    LogUtils.i(TAG,"MODE_GLOBALSEARCH:"+currentItem.getFileAbsolutePath()+","+currentItem.getFileParentPath()+","+currentItem.getShowPath());
                    mCheck.setVisibility(View.GONE);
                    setArrowVisiable(viewHolder, currentItem);
                    setPathText(viewHolder.getTime(), viewHolder.getSize(), viewHolder.getPath(), currentItem);
                    setSearchHighLight(viewHolder.getName(), ((FileBrowserActivity) mContext).getQueryText());
                    break;
                case MODE_COPY:
                    LogUtils.i(TAG, "MODE_COPY");
                    mCheck.setVisibility(View.GONE);
                    setArrowVisiable(viewHolder, currentItem);
                    setTimeSizeTextForDownload(viewHolder.getTime(), viewHolder.getSize(), currentItem);
                    break;
                default:
                    break;
            }
            setIcon(pos, viewHolder, currentItem, IconManager.LIST_ITEM);
        }
        return convertView;
    }

    private void setArrowVisiable(FileItemView fileItemView, FileInfo fileInfo) {
        if (fileInfo.isDirectory()) {
            fileItemView.mArrow.setVisibility(View.VISIBLE);
        } else {
            fileItemView.mArrow.setVisibility(View.GONE);
        }
    }

    private void setIcon(final int pos, final FileItemView viewHolder, final FileInfo fileInfo, int mode) {
        int iconId = mIconManager.getIcon(fileInfo, mode);
        String filePath = fileInfo.getFileAbsolutePath();
        viewHolder.getIcon().setScaleType(ScaleType.CENTER);
        viewHolder.getIcon().setImageResource(iconId);
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        Drawable drawable = mIconManager.getImageCacheDrawable(filePath);
        if (drawable != null) {
            viewHolder.getIcon().setScaleType(ScaleType.CENTER_CROP);
            viewHolder.getIcon().setImageDrawable(drawable);
        }
        if (fileInfo.isHideFile()) {
            viewHolder.getIcon().setAlpha(HIDE_ICON_ALPHA);
        } else if (mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_CUT && mFileInfoManager.isPasteItem(fileInfo)) {
            viewHolder.getIcon().setAlpha(CUT_ICON_ALPHA);
        } else {
            viewHolder.getIcon().setAlpha(DEFAULT_ICON_ALPHA);
        }
        loadImage(fileInfo);
    }

    protected void loadImage(final FileInfo fileInfo) {
        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                ImageView iconByTag = (ImageView) mListView.findViewWithTag("icon" + fileInfo.getFileAbsolutePath());
                if (iconByTag != null) {
                    iconByTag.setScaleType(ScaleType.CENTER_CROP);
                    iconByTag.setImageDrawable((Drawable) msg.obj);
                }
            }
        };
        mIconManager.loadImage(mContext, fileInfo,
                new IconManager.IconCallback() {
                    public void iconLoaded(Drawable iconDrawable) {
                        if (iconDrawable != null) {
                            Message message = mHandler.obtainMessage(0, 1, 1, iconDrawable);
                            mHandler.sendMessage(message);
                        }
                    }
                });
    }

    /**
     * set download file running info
     * @param fileInfo current file info
     * @param progressBar download progress bar
     * @param percentView download percent
     * @param currentSizeView file current size
     */
    private void setRunningInfo(FileInfo fileInfo, ProgressBar progressBar, TextView percentView, TextView currentSizeView) {
        float percent = ((float) fileInfo.getCurrentBytes() / (float) fileInfo.getTotalBytes()) * 100;

        DecimalFormat df = new DecimalFormat("###.0");
        String strPercent = df.format(percent);

        progressBar.setProgress((int) percent);
        if (fileInfo.getCurrentBytes() == 0 || fileInfo.getTotalBytes() == 0) {
            percentView.setText("0%");
        } else {
            percentView.setText(strPercent + "%");
        }
        currentSizeView.setText(Formatter.formatFileSize(mContext, fileInfo.getCurrentBytes()) + "/"
                + Formatter.formatFileSize(mContext, fileInfo.getTotalBytes()));
    }

}
