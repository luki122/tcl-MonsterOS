/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.activity.CategoryActivity;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.IconManager;
import cn.tcl.filemanager.manager.SafeManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.view.FileItemView;
import mst.widget.SliderView;
import cn.tcl.filemanager.activity.FileBaseActivity.deleteFileInfo;

import cn.tcl.filemanager.fragment.FileBrowserFragment.AbsUpdateEncryptFilesCount;

/*MODIFIED-BEGIN by haifeng.tang, 2016-04-14,BUG-1939180*/


public class ListFileInfoAdapter extends FileInfoAdapter {

    private static final String TAG = ListFileInfoAdapter.class.getSimpleName();
    /*MODIFIED-END by haifeng.tang,BUG-1939180*/
    private ListView mListView;
    private int xoff;
    private int yoff;
    private boolean isThirdAPP = false;
    private deleteFileInfo mDelFileInfo;

    private AbsUpdateEncryptFilesCount mAbsUpdateEncryptFilesCount;
    private boolean isClearCheckedList = false;

    /**
     * The constructor to construct a FileInfoAdapter.
     *
     * @param context            the context of FileManagerActivity
     * @param fileManagerService the service binded with FileManagerActivity
     * @param fileInfoManager    a instance of FileInfoManager, which manages all
     *                           files.
     */
    public ListFileInfoAdapter(Context context, FileInfoManager fileInfoManager, ListView listView) {
        super(context, fileInfoManager);
        mListView = listView;
        xoff = context.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_listxoff);
        yoff = context.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_listyoff);
    }

    public ListFileInfoAdapter(Context context, FileInfoManager fileInfoManager, ListView listView, deleteFileInfo delFileInfo) {
        super(context, fileInfoManager);
        mDelFileInfo = delFileInfo;
        mListView = listView;
        xoff = context.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_listxoff);
        yoff = context.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_listyoff);
    }

    public ListFileInfoAdapter(Context context, FileInfoManager fileInfoManager, ListView listView, boolean isThirdApp) {
        super(context, fileInfoManager);
        mListView = listView;
        isThirdAPP = isThirdApp;
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

    /**
     * This method gets the list view to be desplayed in list view.
     *
     * @param pos         the position of the item
     * @param convertView convertView the view to be shown
     * @param parent      parent the parent view
     * @return the list view to be shown
     */
    public View getListView(int pos, View convertView, ViewGroup parent) {

        LogUtils.i(TAG, "refresh adapter");
        FileItemView viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_file_list, null);
            viewHolder = new FileItemView(
                    (SliderView) convertView.findViewById(R.id.slider_view1),
                    (TextView) convertView.findViewById(android.R.id.text1),
                    (TextView) convertView.findViewById(android.R.id.text2),
                    (ImageView) convertView.findViewById(android.R.id.icon),
                    (CheckBox) convertView.findViewById(android.R.id.button1));

            LayoutParams params = (LayoutParams)viewHolder.getName().getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            viewHolder.getName().setLayoutParams(params);
            viewHolder.getName().setEllipsize(TextUtils.TruncateAt.MIDDLE);

            LayoutParams sizeParams = (LayoutParams)viewHolder.getSize().getLayoutParams();
            sizeParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            viewHolder.getSize().setLayoutParams(params);

            viewHolder.getSliderView().addTextButton(1, mContext.getString(R.string.delete));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FileItemView) convertView.getTag();
        }

        FileInfo currentItem = getItem(pos);
        if (currentItem != null) {
            showSize(currentItem, viewHolder.getSize());
            setNameText(viewHolder.getName(), currentItem);
            SliderView mSliderView = viewHolder.getSliderView();
            TextView mSize = viewHolder.getSize();
            ImageView mIcon = viewHolder.getIcon();
            CheckBox mCheck = viewHolder.getCheckBox();
            String filePath = currentItem.getFileAbsolutePath();
            mIcon.setTag("icon" + filePath);
            mSize.setTag("size" + filePath);

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
            mSliderView.close(false);
            mSliderView.setLockDrag(false);
            // mCheck.setTag("check" + filePath);
            LogUtils.e(TAG,"ListFileInfoAdapter mode is " + mMode);
            switch (mMode) {
                case MODE_EDIT:
                case MODE_ADD_ENCRYPT_FILE:
                    LogUtils.e(TAG, "MODE_EDIT SafeManager.mCurrentmode " + SafeManager.mCurrentmode);
                    if (SafeManager.mCurrentmode == SafeManager.FILE_MOVE_IN) {

                        if (currentItem.isDirectory()) {
                            LogUtils.e(TAG,"ListFileInfoAdapter view gone");
                            mCheck.setVisibility(View.GONE);
                        } else {
                            mCheck.setVisibility(View.VISIBLE);
                        }
                    } else {
                        mCheck.setVisibility(View.VISIBLE);
                    }
                    LogUtils.e(TAG,"Check list " + mCheckedFileList.contains(currentItem)+" item Name-->"+currentItem.getFileName()+" checked-->"+(mCheck.getVisibility() == View.VISIBLE));
                    viewHolder.getCheckBox().setChecked(mCheckedFileList.contains(currentItem));
//                    viewHolder.getLineview().setVisibility(View.VISIBLE);
                    setItemTimeSizeTextL(viewHolder.getSize(), currentItem);

                    //
                    mSliderView.close(true);
                    mSliderView.setLockDrag(true);
                    break;
                case MODE_NORMAL:
                    LogUtils.i(TAG,"MODE_NORMAL");
//                    viewHolder.getLineview().setVisibility(View.VISIBLE);
                    mCheck.setVisibility(View.GONE);
                    mCheck.setChecked(false); //MODIFIED by haifeng.tang, 2016-04-14,BUG-1939180
//                    setArrowVisiable(viewHolder, currentItem);
                    setItemTimeSizeTextL(viewHolder.getSize(), currentItem);
                    break;
                case MODE_SEARCH:
                    LogUtils.e(TAG,"MODE_SEARCH");
                    viewHolder.getCheckBox().setVisibility(View.GONE);
//                    viewHolder.mArrow.setVisibility(View.VISIBLE);
                    setItemTimeSizeTextL(viewHolder.getSize(), currentItem);
                    setSearchHighLight(viewHolder.getName(), ((CategoryActivity) mContext).getQueryText());
                    break;
                case MODE_GLOBALSEARCH:
                    LogUtils.i(TAG,"MODE_GLOBALSEARCH:"+currentItem.getFileAbsolutePath()+","+currentItem.getFileParentPath()+","+currentItem.getShowPath());
                    mCheck.setVisibility(View.GONE);
//                    setArrowVisiable(viewHolder, currentItem);
                    setPathText(viewHolder.getSize(), currentItem.getShowPath());
//                    viewHolder.getLineview().setVisibility(View.VISIBLE);
                    setSearchHighLight(viewHolder.getName(), ((CategoryActivity) mContext).getQueryText());
                    break;
                case MODE_COPY:
                    LogUtils.i(TAG, "MODE_COPY");
                    mCheck.setVisibility(View.GONE);
//                    setArrowVisiable(viewHolder, currentItem);
                    setItemTimeSizeTextL(viewHolder.getSize(), currentItem);
                    break;
                default:
                    break;
            }

            viewHolder.getCheckBox().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtils.e(TAG, "click position:" + pos);
                    if (null != mAbsUpdateEncryptFilesCount) {
                        mAbsUpdateEncryptFilesCount.setUpdateEncryptCount(pos);
                    }
                }
            });
            setIcon(pos, viewHolder, currentItem, IconManager.LIST_ITEM);
        }
        return convertView;
    }

    public void setAbsUpdateEncryptFilesCount(AbsUpdateEncryptFilesCount absUpdateEncryptFilesCount) {
        mAbsUpdateEncryptFilesCount = absUpdateEncryptFilesCount;
    }

    private void setArrowVisiable(FileItemView fileItemView, FileInfo fileInfo) {
        if (fileInfo.isDirectory()) {
            fileItemView.mArrow.setVisibility(View.VISIBLE);
        } else {
            fileItemView.mArrow.setVisibility(View.GONE);
        }
    }

    private void setIcon(final int pos, final FileItemView viewHolder, final FileInfo fileInfo, int mode) {
        int iconId;
        if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_SAFE && !fileInfo.isDirectory()) {
            iconId = R.drawable.encryption;
            viewHolder.getIcon().setScaleType(ScaleType.CENTER_INSIDE);
            viewHolder.getIcon().setImageResource(iconId);
        } else {
            iconId = mIconManager.getIcon(fileInfo, mode);
            String filePath = fileInfo.getFileAbsolutePath();
            viewHolder.getIcon().setScaleType(ScaleType.CENTER_INSIDE);
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

}
