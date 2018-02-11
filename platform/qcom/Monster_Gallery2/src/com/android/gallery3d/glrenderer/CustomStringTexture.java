/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.glrenderer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Typeface;
import android.text.TextPaint;

import com.android.gallery3d.R;
import com.android.gallery3d.ui.TypefaceManager;

// StringTexture is a texture shows the content of a specified String.
//
// To create a StringTexture, use the newInstance() method and specify
// the String, the font size, and the color.
public class CustomStringTexture extends CanvasTexture {

    private static final String TAG = "CustomStringTexture";

    private boolean mIsDateMode;
    
    private static Typeface mMonsterLight;

    private static TextPaint mYearAndMonthTextPaint; // 2016.07
    private static TextPaint mDayTextPaint; //21
    
    private static TextPaint mYearTextPaint; // 2016
    
    private TextPaint mMonthTextPaint; // 06 or Lu
    private static TextPaint mMonthTextPaintForEnglish; // Lu in English
    private static TextPaint mMonthTextPaintForChinese; // Lu in Chinese
    
    // font metrics for date mode
    private static FontMetricsInt mYearAndMonthFontMetrics;
    private static FontMetricsInt mDayFontMetrics;

    // font metrics for month mode
    private FontMetricsInt mMonthFontMetrics;
    private static FontMetricsInt mYearFontMetrics;
    
    //text size below:
    private static float mYearAndMonthTextSize;
    private static float mDayTextSize;
    
    private static float mYearTextSize;
    private static float mMonthTextSize;
    
    //text color below:
    private static int mYearAndMonthTextColor;
    private static int mDayTextColor;
    
    private static int mYearTextColor;
    private static int mMonthTextColor;
    
    // text for date mode
    private String mYearAndMonthText;
    private String mDayText;
    
    // text for month mode
    private String mYearText;
    private String mMonthText;
    
    // width and height for date mode
    private int mYearAndMonthTextWidth;
    private int mYearAndMonthTextHeight;
    
    private int mDayTextWidth;
    private int mDayTextHeight;

    // width and height for month mode
    private int mMonthTextWidth;
    
    private int mMonthTextHeight;
    
    private int mYearTextWidth;
    private int mYearTextHeight;
    
    private static int mSpacing;

    private int mLanguageCode = LANG_CODE_CHINESE;
    public static final int LANG_CODE_CHINESE = 0;
    public static final int LANG_CODE_ENGLISH = 1;
    
    public static void initialize(Context context/*, int languageCode*/) {
        Resources res = context.getResources();
        
        mMonsterLight = Typeface.create("monster-normal", Typeface.NORMAL);//when font size less than 24sp
        
        /* date mode below */
        //text size for date mode
        mYearAndMonthTextSize = res.getDimensionPixelSize(R.dimen.mst_date_mode_textsize_year_and_month);
        mDayTextSize = res.getDimensionPixelSize(R.dimen.mst_date_mode_textsize_date);
        //text color for date mode 
        mYearAndMonthTextColor = res.getColor(R.color.mst_date_mode_color_year_and_month);
        mDayTextColor = res.getColor(R.color.mst_date_mode_color_date);
        //text paint for date mode 
        mYearAndMonthTextPaint = getDefaultPaint(context, mYearAndMonthTextSize, mYearAndMonthTextColor, TypefaceManager.TFID_MONSTER_LIGHT);
        mDayTextPaint = getDefaultPaint(context, mDayTextSize, mDayTextColor, TypefaceManager.TFID_MONSTER_REGULAR);
        // font metrics for date mode
        mYearAndMonthFontMetrics = mYearAndMonthTextPaint.getFontMetricsInt();
        mDayFontMetrics = mDayTextPaint.getFontMetricsInt();
        
        /* month mode below */
        //text size for month mode
        mYearTextSize = res.getDimensionPixelSize(R.dimen.mst_month_mode_textsize_year);
        mMonthTextSize = res.getDimensionPixelSize(R.dimen.mst_month_mode_textsize_month);
        //text color for month mode 
        mYearTextColor = res.getColor(R.color.mst_month_mode_year);
        mMonthTextColor = res.getColor(R.color.mst_month_mode_month);
        //text paint for month mode 
        mYearTextPaint = getDefaultPaint(context, mYearTextSize, mYearTextColor, TypefaceManager.TFID_MONSTER_LIGHT);
        //if(languageCode == LANG_CODE_CHINESE) {
        mMonthTextPaintForChinese = getDefaultPaint(context, mMonthTextSize, mMonthTextColor, TypefaceManager.TFID_MONSTER_MEDIUM);
        // } else if(languageCode == LANG_CODE_ENGLISH){
        mMonthTextPaintForEnglish = getDefaultPaint(context, mMonthTextSize, mMonthTextColor, TypefaceManager.TFID_MONSTER_REGULAR);
        //}
        // font metrics for month mode
        mYearFontMetrics = mYearTextPaint.getFontMetricsInt();
        //mMonthFontMetrics = mMonthTextPaint.getFontMetricsInt();
        
        mSpacing = res.getDimensionPixelSize(R.dimen.mst_date_spacing);
    }
    
    private void switchToLanguage(int languageCode) {
        if(languageCode == LANG_CODE_CHINESE) {
            mMonthTextPaint = mMonthTextPaintForChinese;
        } else if(languageCode == LANG_CODE_ENGLISH) {
            mMonthTextPaint = mMonthTextPaintForEnglish;
        }
        mMonthFontMetrics = mMonthTextPaint.getFontMetricsInt();
    }

    /**
     * @param yyyyMMText 2016.07
     * @param dayText 21
     */
    public CustomStringTexture(Context context, String yyyyMMText, String dayText, String yearText, String monthText,  boolean dateMode, int languageCode) {
        super(0, 0);

        switchToLanguage(languageCode);
        
        mLanguageCode = languageCode;
        
        mYearAndMonthText = yyyyMMText;
        mDayText = dayText;

        mYearText = yearText;
        mMonthText = monthText;
        
        int width = 0;
        int height = 0;
        mIsDateMode = dateMode;
        if(dateMode) {
          //calculate width
            mYearAndMonthTextWidth = (int)Math.ceil(mYearAndMonthTextPaint.measureText(yyyyMMText));
            mDayTextWidth = (int)Math.ceil(mDayTextPaint.measureText(dayText));
            width = (int)Math.max(mYearAndMonthTextWidth, mDayTextWidth);
            //calculate height
            mYearAndMonthTextHeight = getTextHeight(mYearAndMonthFontMetrics);//mYearAndMonthFontMetrics.bottom - mYearAndMonthFontMetrics.top;
            mDayTextHeight = getTextHeight(mDayFontMetrics);//mDayFontMetrics.bottom - mDayFontMetrics.top;
            height = mYearAndMonthTextHeight + mDayTextHeight + mSpacing;
        } else {
            //calculate width
            mYearTextWidth = (int)Math.ceil(mYearTextPaint.measureText(yearText));
            mMonthTextWidth = (int)Math.ceil(mMonthTextPaint.measureText(monthText));
            width = (int)Math.max(mYearTextWidth, mMonthTextWidth);
            //calculate height
            mYearTextHeight = getTextHeight(mYearFontMetrics);
            mMonthTextHeight = getTextHeight(mMonthFontMetrics);//mMonthFontMetrics.bottom - mMonthFontMetrics.top;
            height = mYearTextHeight + mMonthTextHeight + mSpacing;
        }
        setSize(width, height);
    }
    
    private int getTextHeight(FontMetricsInt fontMetricsInt) {
        return fontMetricsInt.descent - fontMetricsInt.ascent;
    }

    // TCL ShenQianfeng Begin on 2016.07.29
    public static TextPaint getDefaultPaint(Context context, float textSize, int color, int typefaceId) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        //paint.setShadowLayer(2f, 0f, 0f, Color.BLACK);
        //paint.setTypeface(TypefaceManager.get(context, typefaceId));
        paint.setTypeface(mMonsterLight);
        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas, Bitmap backing) {
        int fontTopPadding1, fontTopPadding2;
        if(mIsDateMode) { 
            canvas.save(Canvas.ALL_SAVE_FLAG);
            fontTopPadding1 = Math.abs(mDayFontMetrics.ascent - mDayFontMetrics.top);
            canvas.translate(mWidth - mDayTextWidth, - fontTopPadding1);
            canvas.drawText(mDayText, 0, (mDayTextHeight - mDayFontMetrics.top - mDayFontMetrics.bottom) / 2, mDayTextPaint);
            canvas.restore();

            canvas.save(Canvas.ALL_SAVE_FLAG);
            fontTopPadding2 = Math.abs(mYearAndMonthFontMetrics.ascent - mYearAndMonthFontMetrics.top);
            canvas.translate(mWidth - mYearAndMonthTextWidth, mDayTextHeight + mSpacing - fontTopPadding1 - fontTopPadding2);
            canvas.drawText(mYearAndMonthText, 0, (mYearAndMonthTextHeight - mYearAndMonthFontMetrics.top - mYearAndMonthFontMetrics.bottom) / 2, mYearAndMonthTextPaint);
            canvas.restore();
        } else {
            if(mLanguageCode == LANG_CODE_CHINESE) {
                drawMonthModeForChinese(canvas);
            } else {
                drawMonthModeForEnglish(canvas);
            }
        }
    }

    private void drawMonthModeForEnglish(Canvas canvas) {
        int fontTopPadding1, fontTopPadding2;
        canvas.save(Canvas.ALL_SAVE_FLAG);
        fontTopPadding1 = Math.abs(mMonthFontMetrics.ascent - mMonthFontMetrics.top);
        canvas.translate(mWidth - mMonthTextWidth, - fontTopPadding1);
        canvas.drawText(mMonthText, 0, (mMonthTextHeight - mMonthFontMetrics.top - mMonthFontMetrics.bottom) / 2, mMonthTextPaint);
        canvas.restore();

        canvas.save(Canvas.ALL_SAVE_FLAG);
        fontTopPadding2 = Math.abs(mYearFontMetrics.ascent - mYearFontMetrics.top);
        canvas.translate(mWidth - mYearTextWidth, mMonthTextHeight + mSpacing - fontTopPadding1 - fontTopPadding2);
        canvas.drawText(mYearText, 0, (mYearTextHeight - mYearFontMetrics.top - mYearFontMetrics.bottom) / 2, mYearTextPaint);
        canvas.restore();
    }
    
    private void drawMonthModeForChinese(Canvas canvas) {
        int fontTopPadding;
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.translate(mWidth - mMonthTextWidth, 0);
        canvas.drawText(mMonthText, 0, (mMonthTextHeight - mMonthFontMetrics.top - mMonthFontMetrics.bottom) / 2, mMonthTextPaint);
        canvas.restore();

        canvas.save(Canvas.ALL_SAVE_FLAG);
        fontTopPadding = Math.abs(mYearFontMetrics.ascent - mYearFontMetrics.top);
        canvas.translate(mWidth - mYearTextWidth, mMonthTextHeight + mSpacing - fontTopPadding);
        canvas.drawText(mYearText, 0, (mYearTextHeight - mYearFontMetrics.top - mYearFontMetrics.bottom) / 2, mYearTextPaint);
        canvas.restore();
    }
}
