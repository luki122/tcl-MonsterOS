/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.filemanager.view;

import android.graphics.Bitmap;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import mst.widget.SliderView;

public class FileItemView {

    public TextView mName;
    public TextView mTime;
    public TextView mSize;
    public TextView mCount;
    public TextView mPath;
    public ImageView mIcon;
    public ImageView mShowIcon;
    public CheckBox mCheckBox;
    public Bitmap mThumbnail;
    public ImageView moreMenu;
    public GridView mGridView;
    public FrameLayout imageFrameLayout;
    public LinearLayout gridMesLayout;
    public LinearLayout mesbackLayout;
    public LinearLayout bottomLayout;
    public TextView lineview;
    public ImageView mArrow;
    private ImageView mPicture;
    private CheckBox mPicCheck;


    private RelativeLayout mSuccessLayout;
    private RelativeLayout mRunningLayout;
    private ProgressBar mDownloadProgressBar;
    private TextView mCurrentSize;
    private TextView mPercent;
    private SliderView mSliderView;


    /**
     * The constructor to construct an edit view tag
     *
     * @param name the name view of the item
     * @param size the size view of the item
     * @param icon the icon view of the item
     * @param box  the check box view of the item
     */
    public FileItemView(TextView name, TextView time, TextView size, TextView path, ImageView icon, TextView line
            , CheckBox box, ImageView arrow) {
        mName = name;
        mTime = time;
        mSize = size;
        mPath = path;
        mIcon = icon;
        lineview = line;
        mCheckBox = box;
        mArrow = arrow;
    }

    public FileItemView(SliderView sliderView, TextView name, RelativeLayout successLayout, TextView time, TextView size, RelativeLayout runningLayout, ProgressBar progressBar,TextView currentSize, TextView percent, ImageView icon
            , CheckBox box, ImageView arrow) {
        mSliderView = sliderView;
        mName = name;
        mSuccessLayout = successLayout;
        mTime = time;
        mSize = size;
        mRunningLayout = runningLayout;
        mDownloadProgressBar = progressBar;
        mCurrentSize = currentSize;
        mPercent = percent;
        mIcon = icon;
        mCheckBox = box;
        mArrow = arrow;
    }

    public FileItemView(TextView name, ImageView icon, ImageView showIcon, TextView time, TextView size, TextView path
            , LinearLayout layout, LinearLayout mesbLayout, LinearLayout bottomview, TextView gridlineView
            , CheckBox box) {
        mName = name;
        mIcon = icon;
        mShowIcon = showIcon;
        mTime = time;
        mSize = size;
        mPath = path;
        mesbackLayout = layout;
        gridMesLayout = mesbLayout;
        bottomLayout = bottomview;
        lineview = gridlineView;
        mCheckBox = box;
    }

    public FileItemView(TextView name, ImageView icon, TextView size, LinearLayout mesbLayout, CheckBox box) {
        mName = name;
        mIcon = icon;
        mSize = size;
        mCheckBox = box;
        gridMesLayout = mesbLayout;
    }

    public FileItemView(ImageView imageView,TextView name,TextView count) {
        mIcon = imageView;
        mName = name;
        mCount = count;
    }

    public FileItemView(ImageView imageView, CheckBox checkBox) {
        mPicture = imageView;
        mPicCheck = checkBox;
    }

    public FileItemView(TextView name, TextView size, ImageView icon, CheckBox box){
        mName = name;
        mSize = size;
        mIcon = icon;
        mCheckBox = box;
    }

    public FileItemView(SliderView sliderView, TextView name, TextView size, ImageView icon, CheckBox box){
        mSliderView = sliderView;
        mName = name;
        mSize = size;
        mIcon = icon;
        mCheckBox = box;
    }

    public FrameLayout getImageFrameLayout() {
        return imageFrameLayout;
    }

    public void setImageFrameLayout(FrameLayout imageFrameLayout) {
        this.imageFrameLayout = imageFrameLayout;
    }

    public TextView getCount() {
        return mCount;
    }

    public void setCount(TextView mCount) {
        this.mCount = mCount;
    }

    public CheckBox getPicCheck() {
        return mPicCheck;
    }


    public RelativeLayout getSuccessLayout() {
        return mSuccessLayout;
    }

    public void setSuccessLayout(RelativeLayout mSuccessLayout) {
        this.mSuccessLayout = mSuccessLayout;
    }


    public RelativeLayout getRunningLayout() {
        return mRunningLayout;
    }

    public void setRunningLayout(RelativeLayout mRunningLayout) {
        this.mRunningLayout = mRunningLayout;
    }

    public ProgressBar getDownloadProgressBar() {
        return mDownloadProgressBar;
    }

    public void setDownloadProgressBar(ProgressBar mDownloadProgressBar) {
        this.mDownloadProgressBar = mDownloadProgressBar;
    }

    public TextView getCurrentSize() {
        return mCurrentSize;
    }

    public void setCurrentSize(TextView mCurrentSize) {
        this.mCurrentSize = mCurrentSize;
    }


    public TextView getPercent() {
        return mPercent;
    }

    public void setPercent(TextView mPercent) {
        this.mPercent = mPercent;
    }

    public void setPicCheck(CheckBox checkBox) {
        mPicCheck = checkBox;
    }

    public ImageView getPicture() {
        return mPicture;
    }

    public void setPicture(ImageView imageView) {
        mPicture = imageView;
    }

    public GridView getGridView() {
        return mGridView;
    }

    public void setGridView(GridView gridView) {
        mGridView = gridView;
    }

    public TextView getName() {
        return mName;
    }

    public void setName(TextView name) {
        mName = name;
    }

    public void setTime(TextView time) {
        mTime = time;
    }

    public TextView getTime() {
        return mTime;
    }

    public TextView getSize() {
        return mSize;
    }

    public TextView getPath() {
        return mPath;
    }
    public void setSize(TextView size) {
        mSize = size;
    }

    public ImageView getIcon() {
        return mIcon;
    }

    public void setIcon(ImageView icon) {
        mIcon = icon;
    }

    public ImageView getShowIcon() {
        return mShowIcon;
    }

    public void setShowIcon(ImageView icon) {
        mShowIcon = icon;
    }

    public CheckBox getCheckBox() {
        return mCheckBox;
    }

    public void setCheckBox(CheckBox checkBox) {
        mCheckBox = checkBox;
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

//    public ImageView getMoreMenu() {
//        return moreMenu;
//    }

    public void setMoreMenu(ImageView moreMenu) {
        this.moreMenu = moreMenu;
    }

    public void setThumbnail(Bitmap thumbnail) {
        mThumbnail = thumbnail;
    }

    public LinearLayout getMesbackLayout() {
        return mesbackLayout;
    }

    public void setMesbackLayout(LinearLayout mesbackLayout) {
        this.mesbackLayout = mesbackLayout;
    }

    public LinearLayout getGridMesLayout() {
        return gridMesLayout;
    }

    public void setGridMesLayout(LinearLayout gridMesLayout) {
        this.gridMesLayout = gridMesLayout;
    }

    public LinearLayout getBottomLayout() {
        return bottomLayout;
    }

    public void setBottomLayout(LinearLayout bottomLayout) {
        this.bottomLayout = bottomLayout;
    }

    public TextView getLineview() {
        return lineview;
    }

    public void setLineview(TextView lineview) {
        this.lineview = lineview;
    }


    public SliderView getSliderView() {
        return mSliderView;
    }

}
