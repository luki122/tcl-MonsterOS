/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.activity.FileBrowserActivity;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.manager.IconManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.view.FileItemView;

public class GridFileInfoAdapter extends FileInfoAdapter {

	private static final String TAG = GridFileInfoAdapter.class.getSimpleName();
	private GridView mGridView;
    private CheckBox mCheckBox;
    private int xoff;
    private int yoff;

    /**
     * The constructor to construct a FileInfoAdapter.
     *
     * @param context the context of FileManagerActivity
     * @param fileInfoManager a instance of FileInfoManager, which manages all
     *            files.
     */
    public GridFileInfoAdapter(Context context, FileInfoManager fileInfoManager, GridView gridView) {
        super(context, fileInfoManager);
        this.mGridView = gridView;
        xoff = context.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_gridxoff);
        yoff = context.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_gridyoff);
    }

    /**
     * This method gets the view to be displayed
     *
     * @param pos the position of the item
     * @param convertView the view to be shown
     * @param parent the parent view
     * @return the view to be shown
     */
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        return getGridView(pos, convertView, parent);
    }

    /**
     * This method gets the list view to be desplayed in grid view.
     *
     * @param pos the position of the item
     * @param convertView convertView the view to be shown
     * @param parent parent the parent view
     * @return the list view to be shown
     */
    public View getGridView(int pos, View convertView, ViewGroup parent) {
        final FileItemView viewHolder;
        final int position = pos;
        if (convertView == null) {
        	convertView = LayoutInflater.from(mContext).inflate(R.layout.file_grid_item, null);
            viewHolder = new FileItemView((TextView) convertView
                    .findViewById(R.id.file_list_item_file_name),
                    (ImageView) convertView.findViewById(R.id.edit_adapter_img),
                    (TextView) convertView.findViewById(R.id.edit_adapter_size),
                    (LinearLayout)convertView.findViewById(R.id.file_grid_item_mes_layout),
                    mCheckBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FileItemView) convertView.getTag();
        }

        FileInfo currentItem = getItem(pos);
        if (currentItem != null && currentItem.isDirectory()) {
            mCheckBox = (CheckBox) convertView.findViewById(R.id.edit_checkbox_album);
        } else {
            mCheckBox = (CheckBox) convertView.findViewById(R.id.edit_checkbox);
        }
        if (currentItem != null) {
            ViewGroup.LayoutParams params = viewHolder.getIcon().getLayoutParams();
            if (currentItem.isDirectory()) {
                if (mGridView.getNumColumns() != 3) {
                    mGridView.setNumColumns(3);
                    params.height = (int)mContext.getResources().getDimension(R.dimen.picture_image_size);
                    viewHolder.getIcon().setLayoutParams(params);
                }
            } else {
                if (mGridView.getNumColumns() != 4) {
                    mGridView.setNumColumns(4);
                }
                params.height = (int)mContext.getResources().getDimension(R.dimen.picture_image_four_size);
                viewHolder.getIcon().setLayoutParams(params);
            }
            showNameAndSize(currentItem, viewHolder.getName(), viewHolder.getSize());
            setNameText(viewHolder.getName(), currentItem);
            ImageView mIcon = viewHolder.getIcon();
            LinearLayout gridLayout=viewHolder.getGridMesLayout();
            TextView mNameTextView=viewHolder.getName();
            TextView mFileSize=viewHolder.getSize();
            String filePath = currentItem.getFileAbsolutePath();
            mIcon.setTag("icon" + filePath);
            gridLayout.setTag("gridMes"+filePath);
            mNameTextView.setTag("mName"+filePath);
            mFileSize.setTag("mSize"+filePath);
            switch (mMode) {
                case MODE_EDIT:
                    mCheckBox.setVisibility(View.VISIBLE);
                    mCheckBox.setChecked(mCheckedFileList.contains(currentItem));
                    setTimeSizeTextForPictures(viewHolder.getTime(), viewHolder.getSize(), currentItem);
                    break;
                case MODE_NORMAL:
                    mCheckBox.setVisibility(View.GONE);
                    mCheckBox.setChecked(false); //MODIFIED by haifeng.tang, 2016-04-14,BUG-1939180
                    setTimeSizeTextForPictures(viewHolder.getTime(), viewHolder.getSize(), currentItem);
                    break;
                case MODE_SEARCH:
                    mCheckBox.setVisibility(View.GONE);
                    setTimeSizeTextForPictures(viewHolder.getTime(), viewHolder.getSize(), currentItem);
                    setSearchHighLight(viewHolder.getName(), ((FileBrowserActivity) mContext).getQueryText());
                    break;
                case MODE_GLOBALSEARCH:
                    mCheckBox.setVisibility(View.GONE);
                    setPathText(viewHolder.getTime(), viewHolder.getSize(), viewHolder.getPath(), currentItem);
                    setSearchHighLight(viewHolder.getName(), ((FileBrowserActivity) mContext).getQueryText());
                    break;
                case MODE_COPY:
                    mCheckBox.setVisibility(View.GONE);
                    setTimeSizeTextForPictures(viewHolder.getTime(), viewHolder.getSize(), currentItem);
                    break;
                default:
                    break;
            }
            setIcon(pos, viewHolder, currentItem, IconManager.GRID_ITEM);
        }
        return convertView;
    }

    @SuppressWarnings("deprecation")
    private void setIcon(final int pos, final FileItemView viewHolder, final FileInfo fileInfo, int mode) {
        int iconId = mIconManager.getIcon(fileInfo, mode);
        String filePath = fileInfo.getFileAbsolutePath();
        List<FileInfo> fileInfos = fileInfo.getSubFileInfo();
        if (null != fileInfos && fileInfos.size() > 0) {
            filePath = fileInfos.get(0).getFileAbsolutePath();
        }
        Drawable drawable = mIconManager.getImageCacheDrawable(filePath);
        filePath = fileInfo.getFileAbsolutePath();
        if (drawable != null) {
            setIconInfo(viewHolder.getIcon(), viewHolder.getName(), viewHolder.getSize(), drawable, fileInfo);
            viewHolder.getGridMesLayout().setBackground(mContext.getResources().getDrawable(R.drawable.list_corners_bg));
        } else {
            viewHolder.getIcon().setScaleType(ScaleType.CENTER_INSIDE);
            viewHolder.getIcon().setImageResource(iconId);
            viewHolder.getGridMesLayout().setBackground(mContext.getResources().getDrawable(R.drawable.list_corners_bg));
        }

        loadImage(fileInfo, filePath);

        if (!mCheckedFileList.contains(fileInfo)) {
            if (fileInfo.isHideFile()) {
                viewHolder.getGridMesLayout().setAlpha(HIDE_ICON_ALPHA);
            } else if (mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_CUT && mFileInfoManager.isPasteItem(fileInfo)) {
                if (viewHolder.getBottomLayout() != null) {
                    viewHolder.getBottomLayout().setVisibility(View.GONE);
                }
                viewHolder.getGridMesLayout().setAlpha(CUT_ICON_ALPHA);
            } else {
                viewHolder.getGridMesLayout().setAlpha(1f);
            }
        }
    }

    private void setIconInfo(ImageView icon, TextView name, TextView size, Drawable drawable, FileInfo fileInfo) {
        icon.setScaleType(ScaleType.CENTER_INSIDE);
        icon.setImageDrawable(drawable);
        icon.setBackground(null);
    }

	protected void loadImage(final FileInfo fileInfo, final String filePath) {
		final Handler mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				ImageView iconByTag;
                TextView nameTextView;
				TextView sizeColor;
				nameTextView=(TextView) mGridView.findViewWithTag("mName"+filePath);
				iconByTag = (ImageView) mGridView.findViewWithTag("icon" + filePath);
                sizeColor = (TextView) mGridView.findViewWithTag("mSize" + filePath);

                if (null != iconByTag && null != nameTextView && null != sizeColor) {
                    setIconInfo(iconByTag, nameTextView, sizeColor, (Drawable) msg.obj, fileInfo);
                }
			}
		};

        FileInfo subFileInfo = fileInfo;
        List<FileInfo> fileInfos = fileInfo.getSubFileInfo();
        if (fileInfo.isDirectory() && null != fileInfos && fileInfos.size() > 0) {
            subFileInfo = fileInfos.get(0);
        }
        mIconManager.loadImage(mContext, subFileInfo,
				new IconManager.IconCallback() {
					public void iconLoaded(Drawable iconDrawable) {
						if (iconDrawable != null) {
							Message message = mHandler.obtainMessage(0, 1, 1,
									iconDrawable);
							mHandler.sendMessage(message);
						}
					}
				});
	}

}
