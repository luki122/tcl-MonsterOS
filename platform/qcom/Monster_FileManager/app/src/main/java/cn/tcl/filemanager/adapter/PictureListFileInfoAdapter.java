/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.IconManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.view.FileItemView;

/*MODIFIED-BEGIN by haifeng.tang, 2016-04-14,BUG-1939180*/


public class PictureListFileInfoAdapter extends FileInfoAdapter {

    private static final String TAG = PictureListFileInfoAdapter.class.getSimpleName();
    /*MODIFIED-END by haifeng.tang,BUG-1939180*/
    private GridView mGridView;
    private int xoff;
    private int yoff;
    private boolean isThirdAPP = false;

    /**
     * The constructor to construct a FileInfoAdapter.
     *
     * @param context            the context of FileManagerActivity
     * @param fileManagerService the service binded with FileManagerActivity
     * @param fileInfoManager    a instance of FileInfoManager, which manages all
     *                           files.
     */
    public PictureListFileInfoAdapter(Context context, FileInfoManager fileInfoManager, GridView gridView) {
        super(context, fileInfoManager);
        mGridView = gridView;
        xoff = context.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_listxoff);
        yoff = context.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_listyoff);
    }

    public PictureListFileInfoAdapter(Context context, FileInfoManager fileInfoManager, GridView gridView, boolean isThirdApp) {
        super(context, fileInfoManager);
        mGridView = gridView;
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

        final FileItemView viewHolder;
        final int position = pos;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.pictures_grid_item, null);
            viewHolder = new FileItemView(
                    (ImageView) convertView.findViewById(R.id.pictures_grid_view_item),
                    (TextView) convertView.findViewById(R.id.pictures_folder_name_view),
                    (TextView) convertView.findViewById(R.id.pictures_count_view));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FileItemView) convertView.getTag();
        }

        FileInfo currentItem = getItem(pos);
        if (currentItem != null) {
            ImageView picImage = viewHolder.getIcon();
            TextView nameView = viewHolder.getName();
            TextView countView = viewHolder.getCount();
            if (currentItem.getFile().isDirectory()){
                File[] files = currentItem.getFile().listFiles();
                if (null != files){
                    int length = currentItem.getFile().list(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String fileName) {
                            if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg") || fileName.endsWith(".png") ||
                                    fileName.endsWith(".gif") || fileName.endsWith(".bmp")) {
                                return true;
                            }
                            return false;
                        }
                    }).length;
                    if (length > 0 ){
                        String path = files[files.length - 1].getAbsolutePath();
                        picImage.setTag("icon" + path);
                        loadImage(new FileInfo(mContext, path));
                        setFolderNameText(nameView, currentItem);
                        setPicturesCount(countView, currentItem);
                    }
                }
            } else {
                picImage.setTag("icon" + currentItem.getFileAbsolutePath());
                loadImage(currentItem);
                nameView.setVisibility(View.GONE);
                countView.setVisibility(View.GONE);
            }
        }
        // ADD START FOR PR1044907 BY HONGBIN.CHEN 20150721
        return convertView;
    }

    protected void loadImage(final FileInfo fileInfo) {
        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                ImageView iconByTag = (ImageView) mGridView.findViewWithTag("icon" + fileInfo.getFileAbsolutePath());
                if (iconByTag != null) {
                    iconByTag.setScaleType(ImageView.ScaleType.CENTER_CROP);
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
