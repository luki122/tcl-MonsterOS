/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

import java.util.List;

import cn.tcl.filemanager.adapter.FileInfoAdapter;


public class PicturesGestureListener implements OnGestureListener {

    private static final String TAG = "PicturesGestureListener";
    private FileInfoAdapter mAdapter;
    private GridView mGridView;
    private Context mContext;
    private int screenWidth, screenHeight, subViewWidth, subViewHeight, horizonSpace, numColumns = 3;
    private List<FileInfo> mCheckList;
    private int[] mInitLeftLocation = {-1, -1};

    private static final int DEFAULT_STATUS = 0;
    private static final int SLIDE_CHECKED = 1;
    private static final int SLIDE_NOT_CHECK = 2;
    private int mIsSelectIntent = DEFAULT_STATUS;



    public PicturesGestureListener(Context context, FileInfoAdapter adapter, GridView gridView) {
        mAdapter = adapter;
        mGridView = gridView;
        mContext = context;
        getScreenDisplay();
    }

    private void getScreenDisplay() {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        LogUtils.i(TAG, "screenWidth:" + screenWidth + ",screenHeight:" + screenHeight);
    }

    private void getGridViewItemInfo() {
        View view = mGridView.getChildAt(mGridView.getFirstVisiblePosition());
        if (null == view) {
            return;
        }
        subViewHeight = view.getMeasuredHeight();
        subViewWidth = mGridView.getColumnWidth();
        horizonSpace = mGridView.getHorizontalSpacing();
        numColumns = mGridView.getNumColumns();
        mGridView.getChildAt(0).getLocationOnScreen(mInitLeftLocation);
        LogUtils.i(TAG, "subViewWidth:" + subViewWidth + ",subViewHeight:" + subViewHeight + ",horizonSpace:" + horizonSpace);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        LogUtils.i(TAG, "onDown-e:" + e.getAction());
        mCheckList = mAdapter.getCheckedFileInfoItemsList();
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        LogUtils.i(TAG, "onShowPress-e:" + e.getAction());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        LogUtils.i(TAG, "onSingleTapUp-e:" + e.getAction());
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mAdapter.getMode() == FileInfoAdapter.MODE_EDIT) {
            float startX, startY, endX, endY;
            startX = e1.getX();
            startY = e1.getY();
            endX = e2.getX();
            endY = e2.getY();

            if (subViewHeight == 0) {
                getGridViewItemInfo();
            }
            return getPositionByCoordinate(startX, startY, endX, endY);
        }
        LogUtils.i(TAG, "onScroll!!!!");
        return false;
    }

    /**
     * get gridview item position by coordinate
     *
     * @param startX start point of X
     * @param startY start point of Y
     * @param endX   end point of X
     * @param endY   end point of Y
     * @return position
     */
    private boolean getPositionByCoordinate(float startX, float startY, float endX, float endY) {

        int level = 5;
        int position = mGridView.getFirstVisiblePosition();
        int[] rightLocation = {-1, -1};
        int[] leftLocation = {-1, -1};

        /** From cache access */
        View childView = mGridView.getChildAt(position);
        /** whether the current value is null, from zero start */
        if (null == childView){
            childView = mGridView.getChildAt(0);
        }
        if (null != childView) {
            childView.getLocationInWindow(leftLocation);
        }


        if (mIsSelectIntent == DEFAULT_STATUS) {
            // init data
            int a = (int)(endY- startY);
            int b = (int)(endX- startX);
            int c = (int) Math.sqrt(a * a + b * b);

            // radian
            double A = Math.acos((b * b + c * c - a * a) / (2.0 * b * c));
            // angle
            A = Math.toDegrees(A);
            LogUtils.i(TAG, "temp:" + A);
            if (A < 35) {
                mIsSelectIntent = SLIDE_CHECKED;
            } else {
                mIsSelectIntent = SLIDE_NOT_CHECK;
            }
        }

        if (mIsSelectIntent != SLIDE_CHECKED) {
            return false;
        }

        /** Calculate the upper left and lower right coordinate values of the first item */
        rightLocation[0] = mInitLeftLocation[0] + subViewWidth;
        leftLocation[1] = leftLocation[1] - mInitLeftLocation[1];
        while (leftLocation[1] > horizonSpace) {
            leftLocation[1] = leftLocation[1] - subViewHeight - horizonSpace;
        }
        rightLocation[1] = leftLocation[1] + subViewHeight;
        int firstSelectPosition, endSelectPosition, xColumn = 0, yRow = 0;
        LogUtils.i(TAG, "rightLocation[0]:" + rightLocation[0] + "rightLocation[1]:" + rightLocation[1]);
        LogUtils.i(TAG, "leftLocation[0]:" + leftLocation[0] + "leftLocation[1]:" + leftLocation[1]);

        /** change coordinate for left-top and right-bottom */
        float temp;
        if (startX > endX) {
            temp = startX;
            startX = endX;
            endX = temp;
        }

        if (startY > endY) {
            temp = startY;
            startY = endY;
            endY = temp;
        }
        LogUtils.i(TAG, "startX:" + startX + "startY:" + startY);
        LogUtils.i(TAG, "endX:" + endX + "endY:" + endY);

        /** get the first item level by coordinate */
        for (int i = 0; i < level; i++) {
            if (rightLocation[0] + (subViewWidth + horizonSpace) * i > startX) {
                xColumn = i;
                break;
            }
        }
        for (int i = 0; i < level; i++) {
            if (rightLocation[1] + (subViewWidth + horizonSpace) * i > startY) {
                yRow = i;
                break;
            }
        }

        firstSelectPosition = yRow * numColumns + xColumn + position;
        /** get the first item level by coordinate */
        for (int i = level; i >= 0; i--) {
            if (leftLocation[0] + (subViewWidth + horizonSpace) * i < endX) {
                xColumn = i;
                break;
            }
        }
        for (int i = level; i >= 0; i--) {
            if (leftLocation[1] + (subViewWidth + horizonSpace) * i < endY) {
                yRow = i;
                break;
            }
        }
        endSelectPosition = yRow * numColumns + xColumn + position;
        /** Comparative large  */
        int startIndex, endIndex;
        if (firstSelectPosition < endSelectPosition) {
            startIndex = firstSelectPosition;
            endIndex = endSelectPosition;
        } else {
            startIndex = endSelectPosition;
            endIndex = firstSelectPosition;
        }

        if (endIndex >= mAdapter.getCount()) {
            endIndex = mAdapter.getCount() - 1;
        }
        /** the same column */
        int startRemainder = startIndex % numColumns;
        int endRemainder = endIndex % numColumns;
        if (startRemainder == endRemainder) {
            /** set check list */
//            for (int i = startIndex; i <= endIndex; i += numColumns) {
//                setAdapterCheckOfIndex(i);
//            }
//            return false;
        } else if (Math.abs(endRemainder - startRemainder) == 1) {
            /** set check list */
            for (int i = startIndex; i <= endIndex; i++) {
                if (i % numColumns == startRemainder || i % numColumns == endRemainder) {
                    setAdapterCheckOfIndex(i);
                }
            }
//            return false;
        } else {
            /** set check list */
            for (int i = startIndex; i <= endIndex; i++) {
                setAdapterCheckOfIndex(i);
            }
        }
        LogUtils.i(TAG, "startIndex:" + startIndex + ",endIndex:" + endIndex);
        return true;
    }

    /**
     * set check status of index
     *
     * @param i need setcheck index
     */
    private void setAdapterCheckOfIndex(int i) {
        FileInfo fileInfo = mAdapter.getItem(i);
        if (mCheckList.contains(fileInfo)) {
            mAdapter.setChecked(i, false);
        } else {
            mAdapter.setChecked(i, true);
        }
    }

    @Override
    public void onLongPress(MotionEvent e) {
        LogUtils.i(TAG, "onLongPress-e:" + e.getAction());
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        LogUtils.i(TAG, "onFling-e:" + e1.getAction() + "e2:" + e2.getAction() + ",velocityX:" + velocityX + ",velocityY:" + velocityY);
        return false;
    }

    public void up() {
        LogUtils.i(TAG, "up!!!!");
        mIsSelectIntent = DEFAULT_STATUS;
        mCheckList = mAdapter.getCheckedFileInfoItemsList();
    }
}
