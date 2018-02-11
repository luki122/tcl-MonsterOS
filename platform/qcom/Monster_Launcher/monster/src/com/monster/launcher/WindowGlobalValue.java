package com.monster.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;

import com.monster.launcher.theme.utils.PhotoUtils;

/**
 * Created by antino on 16-7-1.
 */
public class WindowGlobalValue {
    private static final String TAG = WindowGlobalValue.class.getSimpleName();
    public static int navigatebarHeight = 0;
    public static int statusbarHeight = 0;
    private Palette mPalette;
    private Context mContext;
    private int mtextColor;
    private Drawable mBlurDrawable;
    Drawable mAllappBackground=null;
    public WindowGlobalValue(Context context) {
        mContext = context;
    }
    public static void setNavigatebarHeight(int value) {
        navigatebarHeight = value;
    }
    public static void setStatusbarHeight(int value) {
        statusbarHeight = value;
    }
    public static int getNavigatebarHeight() {
        return navigatebarHeight;

    }
    public static int getStatusbarHeight() {
        return statusbarHeight;
    }
    public  static int CC_BG_DEFAULT_COLOR = 0xffe5e5e5;
    public static int CC_TEXT_BLACK_DEFAULT_COLOR = 0xE5000000;
    public static int CC_TEXT_WHITE_DEFAULT_COLOR = 0xFFFFFFFF;
    public static int CC_BG_DEFAULT_FOLDER_TEXT_COLOR = 0xff000000;
    public static int CC_ALL_APP_HEAD_COLOR = 0x33999999;

    int[] mColors = {CC_TEXT_BLACK_DEFAULT_COLOR,
            CC_BG_DEFAULT_COLOR,
            CC_TEXT_BLACK_DEFAULT_COLOR,
            CC_BG_DEFAULT_FOLDER_TEXT_COLOR,
            CC_ALL_APP_HEAD_COLOR};
    public int[] getAllColors(){
        return mColors;
    }
    public int getTextColor() {
        return mColors[0];
    }
    public int getTextExtraColor(){
        return mColors[2];
    }

    public int getFolderBgColor() {
        return mColors[3];
    }

    public int getAllappViewBgColor() {
        return mColors[1];
    }

    public void cacAllappBackground(int startColor,int endColor){
        mAllappBackground = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{(startColor),(endColor)});
    }

    public Drawable getAllappBackground(){
        return mAllappBackground;
    }
    public void dealWallpaperForLauncher(Bitmap wallpaper,final Runnable callback){
        if(wallpaper == null){
            mColors[0] = CC_TEXT_BLACK_DEFAULT_COLOR;
            mColors[1] = CC_BG_DEFAULT_COLOR;
            mColors[2] = CC_TEXT_BLACK_DEFAULT_COLOR;
            mColors[3] = CC_BG_DEFAULT_FOLDER_TEXT_COLOR;
            mColors[4] = CC_ALL_APP_HEAD_COLOR;
            callback.run();
        }else{
            AsyncTask<Bitmap,Void,Void> as = new AsyncTask<Bitmap, Void, Void>() {
                @Override
                protected Void doInBackground(Bitmap... params) {
                    int[] result = mColors;
                    if(params!=null&&params[0]!=null){
                        result[0] = PhotoUtils.calcTextColor(params[0]);
                        result[1] = PhotoUtils.bitmapToRGB(params[0]);
                        result[2] = CC_TEXT_BLACK_DEFAULT_COLOR;
                        result[3] =  0xffF8F8F8;
                        float[] hsv = new float[3];
                        Color.colorToHSV(result[1],hsv);
                        result[4] = calcAllAppHeadColor(hsv,0.09f);
                        cacAllappBackground(calcAllAppHeadColor(hsv,0.05f),calcAllAppHeadColor(hsv,0.09f));
                        cacAllappHeadBackground(mColors[4],calcAllAppHeadColor(hsv,0.1f));
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    callback.run();
                }
            };
            as.execute(wallpaper);
        }
    }

    public boolean isBlackText(boolean isWorkspace){
        int color=isWorkspace?mColors[0]:mColors[2];
        if(color!=-1)
            return true;
        return false;
    }

    public boolean isBlackText(){
        if(mColors[0]!=-1)
            return true;
        return false;
    }
    Drawable mAllappHeadBackground=null;
    public void cacAllappHeadBackground(int startColor,int endColor){
        mAllappHeadBackground = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{startColor,endColor});
    }
    public void  setBackground(Drawable blurDrawable){
        mBlurDrawable = blurDrawable;
    }
    public Drawable  getBackground(){
        return mBlurDrawable;
    }
    public void setTextColor(int color){
        mColors[0] = color;
    }
    public static int calcAllAppHeadColor(float[] hsv,float detaFactor){
        float[] hsv1 = new float[3];
        hsv1[0] = hsv[0];
        hsv1[1] = hsv[1];
        float factor = hsv[2]>0.8f?-detaFactor:detaFactor;
        hsv1[2] = hsv[2]+factor;
        return Color.HSVToColor(hsv1);
    }
}
