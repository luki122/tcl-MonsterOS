package com.monster.market.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaobin on 16-8-15.
 */
public class PicBrowserUtil {

    public static final String TAG = "PicBrowserUtil";

    public static List<ImageView> mPicImgVList = new ArrayList<ImageView>();
    private static int mPicIndex;
    private static int mDefaultLocation[] = new int[2];
    private static int[] mDimen = new int[2];

    public static void resetImgVContainer() {
        mPicImgVList.clear();
    }

    public static void addImgV(ImageView pPicImgV) {

        int[] location = new int[2];

        final Rect rect = new Rect();
        pPicImgV.getHitRect(rect);
        pPicImgV.getLocationOnScreen(location);

        mPicImgVList.add(pPicImgV);
    }

    public static void setDefaultPicIndex(int pIndex) {

        mPicIndex = pIndex;
        try {
            ImageView imageView = mPicImgVList.get(pIndex);
            if (imageView != null) {
                imageView.getLocationOnScreen(mDefaultLocation);
                mDimen[0] = mPicImgVList.get(pIndex).getWidth();
                mDimen[1] = mPicImgVList.get(pIndex).getHeight();
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "内存被回收了");
        }

    }

    public static int getDefaultPicIndex() {
        return mPicIndex;
    }

    public static int[] getCurImgVLoc(int pIndex) {
        int[] location = new int[2];

        final Rect rect = new Rect();
        try {

            mPicImgVList.get(pIndex).getHitRect(rect);
            mPicImgVList.get(pIndex).getLocationOnScreen(location);

        } catch (Exception e) {
            LogUtil.e(TAG, "内存被回收了");
            location = mDefaultLocation;
        }

        return location;
    }

    public static int[] getCurImgVDimension(int pIndex) {
        int[] dimen = new int[2];
        try {
            dimen[0] = mPicImgVList.get(pIndex).getWidth();
            dimen[1] = mPicImgVList.get(pIndex).getHeight();
        } catch (Exception e) {
            LogUtil.e(TAG, "内存被回收了");
            dimen = mDimen;
        }
        return dimen;
    }

    public static Drawable getImgVByIndex(int pIndex) {

        return mPicImgVList.get(pIndex).getDrawable();

    }

    public static int getStatusHeight(Activity activity) {
        int statusHeight = 0;
        Rect localRect = new Rect();
        activity.getWindow().getDecorView()
                .getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight) {
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject;
                localObject = localClass.newInstance();

                int i5 = Integer.parseInt(localClass
                        .getField("status_bar_height").get(localObject)
                        .toString());
                statusHeight = activity.getResources()
                        .getDimensionPixelSize(i5);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return statusHeight;
    }


}
