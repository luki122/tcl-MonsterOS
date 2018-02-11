package com.android.gallery3d.ui;

import com.android.gallery3d.data.DateGroupInfos;
import com.android.gallery3d.data.DateGroupInfos.DateInfo;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.ui.SlotView.SlotAnimation;
import com.android.gallery3d.ui.SlotView.SlotRenderer;
import com.android.gallery3d.ui.SlotView.Spec;
import com.android.gallery3d.util.LogUtil;

import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;

/*
 * This file was added by ShenQianfeng on 2016.06.27
 * [SCALE MODEL]
 * scale model is as below:
 * portrait :
 * column num:      2,   3,   4,   5,   6
 *                 knot:      4,    3,   2,   1,   0   [stored in mKnotScales]
 * landscape: 
 * column num:      6,   7,   8,   9,  10
 *                 knot:       4,    3,   2,   1,   0   [stored in mKnotScales]
 */
public class SlotViewScaleManager {
    
    private static final String TAG = "SlotViewScaleManager";
    
    private static final int UNKNOWN = -1;
    
    private SlotView mSlotView;
    
    private static final float SCALE_DELTA = 0.25f;
    
    private static final int MIN_KNOT_INDEX = 0;
    private static final int MAX_KNOT_INDEX = 4;
    
    //mKnotScales is calculated dynamically,
    //for example:
    //if mBeginColumnNum is 4,   mScreenOrientation is portrait, and PORTRAIT_MAX_COLUMN_NUM is 6
    //then mBeginKnot = PORTRAIT_MAX_COLUMN_NUM - mBeginColumnNum = 2;
    //the begin knot scale is 1.0f, 
    //the next knot's scale is (1.0f + SCALE_DELTA) = 1.25f;
    //the previous knot's scale is (1.0f - SCALE_DELTA) = 0.75f;
    //the mKnotScales looks like [0.5f, 0.75f, 1.0f, 1.25f, 1.5f];
    //so, if the mBeginColumnNum is 2, 
    //then the mKnotScales looks like [1.0f, 1.25f, 1.5f, 1.75f, 2.0f];
    //so, if the mBeginColumnNum is 6, 
    //then the mKnotScales looks like [0.0f, 0.25f, 0.5f, 0.75f, 1.0f]
    private float [] mKnotScales = new float[5];

    //when we touch the screen with two fingers, the scale always begin with 1.0f
    private static final float SCALE_BEGIN = 1.0f; 

    //mScreenOrientation indicate the screen orientation when we touch the screen with two fingers
    private int mScreenOrientation;
    //mBeginColumnNum indicate the column num when we touch the screen with two fingers
    private int mBeginColumnNum;
    
    //mBeginKnot indicate the knot value when we touch the screen with two fingers
    //the value is between 0 - 4 in [SCALE MODEL]
    private int mBeginKnot = UNKNOWN;
    
    //mCurrentKnot may be mBeginKnot - 2, or mBeginKnot - 3, and so on...
    //i.e. this value is calculated dynamically.
    private int mCurrentKnot = UNKNOWN;
    
    private int mPreviousKnot = UNKNOWN;
    
    //the previous or the next knot of mCurrentKnot when scaling.
    private int mTurningToKnot = UNKNOWN;
    
    //when column num reaches PORTRAIT_MIN_COLUMN_NUM or LANDSCAPE_MIN_COLUMN_NUM
    private boolean mFewestColumnMode;
    
    //when column num reaches PORTRAIT_MAX_COLUMN_NUM or LANDSCAPE_MAX_COLUMN_NUM
    private boolean mMostColumnMode;
    
    private float mDeltaScaleRelativeToCurrentKnot;
    
    private float mPreviousScale = 1.0f;
    private float mProgress;
    
    private Rect mTurningToRect = new Rect();
    private Rect mTempRect = new Rect();
    
    private int mVisibleStart;
    
    private boolean mIsScaling;
    
    private static float sDateToMonthProgressCutPoint = 0.5f;
    private static float sMonthToDateProgressCutPoint = 0.5f;
    
    
    private String mPrevYearAndMonth = "";
    
    public SlotViewScaleManager(SlotView slotView, int screenOrientation, int columnNum, int visibleStart) { 
        mSlotView = slotView;
        mScreenOrientation = screenOrientation; 
        mBeginColumnNum = columnNum;
        mVisibleStart = visibleStart;
        initBeginKnot();
        mCurrentKnot = mBeginKnot;
        generateKnotScales();
        mIsScaling = true;
    }
    
    public void reset() {
        mPrevYearAndMonth = "";
    }

    private void initBeginKnot() {
        switch(mScreenOrientation) {
        case Configuration.ORIENTATION_LANDSCAPE:
            mBeginKnot = Spec.LANDSCAPE_MAX_COLUMN_NUM - mBeginColumnNum;
            break;
        case Configuration.ORIENTATION_PORTRAIT:
            mBeginKnot = Spec.PORTRAIT_MAX_COLUMN_NUM - mBeginColumnNum;
            break;
        default:
            break;
        }
    }
    
    private void generateKnotScales() {
        for(int i=0; i<mKnotScales.length; i++) {
            mKnotScales[i] = (i - mBeginKnot) * SCALE_DELTA + 1.0f;
        }
    }
    
    public int getVisibleStart() {
        return mVisibleStart;
    }
    
    public boolean isScaling() {
        return mIsScaling;
    }
    
    public void setIsScaling(boolean isScaling) {
        mIsScaling = isScaling;
    }
    
    public float getProgress() {
        return mProgress;
    }
    
    public boolean isFewestColumnMode() {
        return mFewestColumnMode;
    }
    
    public boolean isMostColumnMode() {
        return mMostColumnMode;
    }
    
    
    /*
     * @param scale this argument is passed in from GestureRecognizer.onScale
     */
    public void performScale(final float scale) {
        //LogUtil.d(TAG, "performScale:" + scale);
        if(mCurrentKnot >= mKnotScales.length) {
            //LogUtil.d(TAG, "performScale mCurrentKnot:" + mCurrentKnot + " mKnotScales.length:" + mKnotScales.length + " return---------");
            return;
        }
        mFewestColumnMode = false;
        mMostColumnMode = false;
        mDeltaScaleRelativeToCurrentKnot = scale - mKnotScales[mCurrentKnot];
        
        //reaches PORTRAIT_MAX_COLUMN_NUM or LANDSCAPE_MAX_COLUMN_NUM
        if(mPreviousKnot == MAX_KNOT_INDEX && mDeltaScaleRelativeToCurrentKnot > 0) {
            mMostColumnMode = true;
            //LogUtil.d(TAG, "performScale reaches MAX return");
            if(mBeginKnot == MAX_KNOT_INDEX) {
                mDeltaScaleRelativeToCurrentKnot = 0.0f;
            } else {
                return;
            }
        }
        //reaches PORTRAIT_MIN_COLUMN_NUM or LANDSCAPE_MIN_COLUMN_NUM
        if(mPreviousKnot == MIN_KNOT_INDEX && mDeltaScaleRelativeToCurrentKnot < 0) {
            mFewestColumnMode = true;
            //LogUtil.d(TAG, "performScale reaches MIN return");
            if(mBeginKnot == MIN_KNOT_INDEX) {
                mDeltaScaleRelativeToCurrentKnot = 0.0f;
            } else {
                return;
            }
        }
        mProgress = Math.abs(mDeltaScaleRelativeToCurrentKnot) / SCALE_DELTA;
        //record current　knot
        mPreviousKnot = mCurrentKnot;
        //LogUtil.d(TAG, "performScale current knot before calculation mCurrentKnot:" + mCurrentKnot);
        calculateCurrentKnot(scale);
        //LogUtil.d(TAG, "performScale current knot after calculation mCurrentKnot:" + mCurrentKnot);
        mTurningToKnot = calculateTurningToKnot(scale);
        //LogUtil.d(TAG, "performScale calculateTurningToKnot mTurningToKnot:" + mTurningToKnot);
        //we are crossing another knot. adjust the slot infos.
        if(UNKNOWN != mPreviousKnot && mPreviousKnot != mCurrentKnot) {
            final int prevColumnNum = getColumnNumByKnot(mPreviousKnot);
            final int columnNum = getColumnNumByKnot(mCurrentKnot);
            int mode = mSlotView.getColumnDisplayMode(columnNum);
            int scrollPosition = getScrollPositionAfterScale(columnNum, mVisibleStart, mode);
            /*
            LogUtil.d(TAG, "performScale CROSSING scrollPosition after scale:" + scrollPosition + 
                    " prevColumnNum:" + prevColumnNum + " columnNum:" + columnNum + 
                    " visibleStart:" + mVisibleStart); */
            mSlotView.setScrollPositionDirectly(scrollPosition);
            mSlotView.switchToColumn(prevColumnNum, columnNum, scrollPosition);
            mProgress = 0.0f;
        }
        mPreviousScale = scale;
    }
    
    private int getScrollPositionAfterScale(int unitCountAfterScale, int visibleStart, int turningToMode) {
        mTempRect = mSlotView.getSlotRect(visibleStart, unitCountAfterScale, mTempRect, turningToMode);
        if(mTempRect.top == mSlotView.getSpec().slotAreaTopPadding) {
            return 0;
        } 
        return mTempRect.top;
    }
    
    public void applySlotScaleEffect(GLCanvas canvas, int slotIndex, Rect currentKnotRect) {
        int turningToColumnNum = getColumnNumByKnot(mTurningToKnot);
        int turningToMode = mSlotView.getColumnDisplayMode(turningToColumnNum);
        mSlotView.getSlotRect(slotIndex, turningToColumnNum, mTurningToRect, turningToMode);
        float zoomRatio = (float)mTurningToRect.width() / (float)currentKnotRect.width();
        zoomRatio = 1 + (zoomRatio - 1) * mProgress;
        int scrollPosAfterScale = getScrollPositionAfterScale(turningToColumnNum, mVisibleStart, turningToMode);
        int curScrollPos = mSlotView.getScrollPosition();
        
        if(curScrollPos == mSlotView.getSpec().slotAreaTopPadding) {
            curScrollPos = 0;
        }
        /*
        LogUtil.d(TAG, "applySlotScaleEffect visibleStart:" + mVisibleStart + 
                " scrollPosAfterScale:" + scrollPosAfterScale + 
                " curScrollPos:" + curScrollPos);
               */
        float offsetX = mTurningToRect.centerX() - currentKnotRect.centerX();
        float offsetY = (mTurningToRect.centerY() - scrollPosAfterScale) - 
                                         (currentKnotRect.centerY() - curScrollPos);
        canvas.translate(offsetX * mProgress, offsetY * mProgress );
        float zoomOffset = (currentKnotRect.width() - currentKnotRect.width() * zoomRatio) / 2.0f;
        canvas.translate(zoomOffset, zoomOffset);
        canvas.scale(zoomRatio, zoomRatio, 1.0f);
    }

    public void applyDateScaleEffect(SlotRenderer renderer, GLCanvas canvas, final int curScrollPos, 
            int currentTop, final int curFirstGroupIndex, final int currentGroupIndex, float progress, String text) {
        //int currentUnitCount = getColumnNumByKnot(mCurrentKnot);
        //LogUtil.d(TAG, "applyDateScaleEffect ---- > ");
        int turningToUnitCount = getColumnNumByKnot(mTurningToKnot);
        int turningToMode = mSlotView.getColumnDisplayMode(turningToUnitCount);
        int curMode = mSlotView.getColumnDisplayMode(mSlotView.getCurrentUnitCount());
        
        DateGroupInfos infos = mSlotView.getDateGroupInfos();
        int slotHeight = mSlotView.getLayout().getSlotWidthByUnitCount(turningToUnitCount);
        int extra = currentGroupIndex == 0 ? mSlotView.getSpec().slotAreaTopPadding : mSlotView.getSpec().slotGroupGap;
        
        if(curMode == turningToMode) {
            Point bound = infos.getGroupBound(currentGroupIndex, turningToUnitCount, mSlotView.getSpec(), slotHeight, turningToMode);
            int scrollPosAfterScale = getScrollPositionAfterScale(turningToUnitCount, mVisibleStart, turningToMode);
            int afterScaleGroupTop = bound.x + extra;
            if(currentGroupIndex == curFirstGroupIndex) {
                int firstVisibleStart = mVisibleStart;
                int afterScalePosOfFirstVisibleStart = getScrollPositionAfterScale(turningToUnitCount, firstVisibleStart, turningToMode);
                int pos = 0;
                boolean firstGroupOnTop = false;
                if(currentGroupIndex == 0 && curScrollPos < mSlotView.getSpec().slotAreaTopPadding) {
                    //LogUtil.d(TAG, "firstGroupOnTop:" + firstGroupOnTop + " currentGroupIndex:" + currentGroupIndex);
                    pos = mSlotView.getSpec().slotAreaTopPadding;
                    firstGroupOnTop = true;
                } else {
                    pos = curScrollPos;
                }
                int y1 = ( pos- curScrollPos);
                int y2 = (afterScalePosOfFirstVisibleStart - scrollPosAfterScale);
                int animateTop = (int)(pos + (y2 - y1) * progress);
                if(firstGroupOnTop && animateTop < mSlotView.getSpec().slotAreaTopPadding) {
                    animateTop = mSlotView.getSpec().slotAreaTopPadding;
                }
                canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
                canvas.translate(0, animateTop);
                boolean dateMode = curMode == DateGroupInfos.MODE_DATE;
                renderer.renderString(canvas, text, dateMode);
                canvas.restore();
                 
            } else {
                int y1 = (currentTop - curScrollPos);
                int y2 = (afterScaleGroupTop - scrollPosAfterScale);
                int animateTop = (int)(currentTop + (y2 - y1) * progress);
                canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
                canvas.translate(0, animateTop);
                boolean dateMode = curMode == DateGroupInfos.MODE_DATE;
                renderer.renderString(canvas, text, dateMode);
                canvas.restore();
            }
        } else {
            DateInfo dateInfo = infos.matchDate(curMode, turningToMode, currentGroupIndex);
            int scrollPosAfterScale = getScrollPositionAfterScale(turningToUnitCount, mVisibleStart, turningToMode);
            int index = 0;

            for(int i = dateInfo.fromGroupIndex; i<= dateInfo.toGroupIndex; i++) {
                    Point bound = infos.getGroupBound(i, turningToUnitCount, mSlotView.getSpec(), slotHeight, dateInfo.mode);
                    if(null == bound) continue;
                    int topAfterScale = bound.x + extra;
                    if(currentGroupIndex == curFirstGroupIndex) {
                        int slotAreaTopPadding = mSlotView.getSpec().slotAreaTopPadding;
                        if(topAfterScale < slotAreaTopPadding) {
                            topAfterScale = slotAreaTopPadding;
                        }
                    } 
                    int y1 = (currentTop - curScrollPos);
                    int y2 = (topAfterScale - scrollPosAfterScale);
                    int animateTop = (int)(currentTop + (y2 - y1) * progress);
                    canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
                    canvas.translate(0, animateTop);
                    if(curMode == DateGroupInfos.MODE_DATE && 
                            turningToMode == DateGroupInfos.MODE_MONTH ) {
                        if(mProgress < sDateToMonthProgressCutPoint) {
                            //render               21
                            //                  2016.07
                            // but should apply alpha transformation
                            float tmpProgress = -1.6f * mProgress + 1;
                            renderer.renderDateModeStringWhenScale(canvas, dateInfo.text, true, tmpProgress);
                            //LogUtil.d(TAG, " mProgress: " + mProgress + " tmpProgress:" + tmpProgress);
                        } else {
                            //render              06
                            //                        2016
                            //renderer.renderStringWhenScale(canvas, dateInfo.text, true, mProgress);
                            //renderer.renderString(canvas, dateInfo.text, true);
                            float tmpProgress =  2 * mProgress - 1;
                            String currentYearAndMonth = text.substring(0, 7);
                            //LogUtil.d(TAG, "prevYearAndMonth:" + mPrevYearAndMonth + " currentYearAndMonth:" + currentYearAndMonth);
                            if( ! mPrevYearAndMonth.equals(currentYearAndMonth)) {
                                renderer.renderMonthModeStringWhenScale(canvas, text, true, tmpProgress);
                            } 
                            mPrevYearAndMonth = currentYearAndMonth;
                        }
                    } else {
                        if(mProgress < sMonthToDateProgressCutPoint) {
                            //render              06
                            //                        2016
                            float tmpProgress =  -1.6f * mProgress + 1;
                            String currentYearAndMonth = text.substring(0, 7);
                            //LogUtil.d(TAG, "prevYearAndMonth:" + mPrevYearAndMonth + " currentYearAndMonth:" + currentYearAndMonth);
                            if( ! mPrevYearAndMonth.equals(currentYearAndMonth)) {
                                renderer.renderMonthModeStringWhenScale(canvas, text, true, tmpProgress);
                            } 
                            mPrevYearAndMonth = currentYearAndMonth;
                        } else {
                            //render               21
                            //                  2016.07
                            // but should apply alpha transformation
                            float tmpProgress = -2 * (1 - mProgress) + 1;
                            String dateText = dateInfo.textsMapFromMonthToDate.get(index);
                            renderer.renderDateModeStringWhenScale(canvas, dateText, true, tmpProgress);
                            //LogUtil.d(TAG, " mProgress: " + mProgress + " tmpProgress:" + tmpProgress);
                        }
                    }
                    canvas.restore();
                    
                    index ++;
                    
                    /*
                    LogUtil.d(TAG, " applyDateScaleEffect 333 --> firstGroupIndex: " + curFirstGroupIndex + 
                            " currentGroupIndex:" + currentGroupIndex + 
                            " animateTop:" + animateTop + 
                            " currentTop:" + currentTop + 
                            " bound.x:" + topAfterScale + 
                            " progress:" + progress + 
                            " text:" + text +
                            " dateInfo:" + dateInfo);*/
            }
        }
    }

    public SlotAnimation doLeftAnimation(int visibleStart) {
        //LogUtil.d(TAG, "doLeftAnimation -----------------------");
        if(mMostColumnMode || 
                (mProgress < 0 || mProgress >= 1) || 
                (UNKNOWN == mCurrentKnot || UNKNOWN == mTurningToKnot)) {
            //LogUtil.d(TAG, "doLeftAnimation -----------------------return...");
            return null;
        }
        int unitCountAfterScale = getColumnNumByKnot(mTurningToKnot);
        mSlotView.getLayout().updateVisibleSlotRangeForScale();
        //int visibleStart = mSlotView.getVisibleStart();
        int mode = mSlotView.getColumnDisplayMode(unitCountAfterScale);
        int afterScaleScrollPos = getScrollPositionAfterScale(unitCountAfterScale, visibleStart, mode);
        SlotAnimation slotAnimation = new LeftOverAnimation(mCurrentKnot, mTurningToKnot, mProgress, 
                visibleStart, mSlotView.getScrollPosition(), afterScaleScrollPos);
        slotAnimation.start();
        return slotAnimation;
    }
    
    public class LeftOverAnimation extends SlotAnimation {
        
        private float GO_BACK_TO_CURRENT_RATIO = 0.5f;
        
        private int mCurrentKnot;
        private int mTurningToKnot;
        private float mCurrentProgress;
        private boolean mGoBackToCurrent;
        private int mVisibleStart;
        private Rect mTurningToRect = new Rect();
        private int mCurrentScrollPosition;
        private int mAfterScaleScrollPosition;
        
        
        public LeftOverAnimation(int currentKnot, int turningToKnot, float progress, int visibleStart, int curScrollPos, int afterScaleScrollPos) {
            super();
            setDuration(600);
            mCurrentScrollPosition = curScrollPos;
            mAfterScaleScrollPosition = curScrollPos == 0 ? 0 : afterScaleScrollPos;
            mCurrentKnot = currentKnot;
            mTurningToKnot = turningToKnot;
            mCurrentProgress = progress;
            mVisibleStart = visibleStart;
            mGoBackToCurrent = (progress < GO_BACK_TO_CURRENT_RATIO) ? true : false;
        }

        @Override
        protected void onCalculate(float progress) {
            if(mGoBackToCurrent) {
                mProgress = mCurrentProgress - mCurrentProgress * progress;
            } else {
                mProgress = mCurrentProgress + (1 - mCurrentProgress) * progress;
            }
            //LogUtil.d(TAG, "onCalculate ---> progress:" + progress);
        }

        @Override
        public void apply(GLCanvas canvas, int slotIndex, Rect currentKnotRect) {
            //LogUtil.i(TAG, "LeftOverAnimation ------ apply");
            int mTurningToUnitCount = getColumnNumByKnot(mTurningToKnot);
            int mode = mSlotView.getColumnDisplayMode(mTurningToUnitCount);
            mTurningToRect = mSlotView.getSlotRect(slotIndex, mTurningToUnitCount, mTurningToRect, mode);
            float zoomRatio = (float)mTurningToRect.width() / (float)currentKnotRect.width();
            zoomRatio = 1 + (zoomRatio - 1) * mProgress;
            float offsetX = mTurningToRect.centerX() - currentKnotRect.centerX();
            float offsetY = (mTurningToRect.centerY() - mAfterScaleScrollPosition) - 
                        (currentKnotRect.centerY() - mCurrentScrollPosition);
            canvas.translate(offsetX * mProgress, offsetY * mProgress );
            float zoomOffset = (currentKnotRect.width() - currentKnotRect.width() * zoomRatio) / 2.0f;
            canvas.translate(zoomOffset, zoomOffset);
            canvas.scale(zoomRatio, zoomRatio, 1.0f);
        }

        public float getProgress() {
            return mProgress;
        }

        public void onAnimationDone() {
            //LogUtil.i2(TAG, "LeftOverAnimation::onAnimationDone ----> ");
            final int prevColumnNum = getColumnNumByKnot(mCurrentKnot);
            final int turningTocolumnNum = getColumnNumByKnot(mTurningToKnot);
            int mode = mSlotView.getColumnDisplayMode(turningTocolumnNum);
            int scrollPosition = getScrollPositionAfterScale(turningTocolumnNum, mVisibleStart, mode);
            /*
            LogUtil.d(TAG, "onAnimationDone  scrollPosition after scale:" + scrollPosition + 
                    " prevColumnNum:" + prevColumnNum + " turningTocolumnNum:" + turningTocolumnNum);
            */
            mSlotView.setScrollPositionDirectly(scrollPosition);
            mSlotView.switchToColumn(prevColumnNum, turningTocolumnNum, scrollPosition);
        }
    }
    
    private int getColumnNumByKnot(final int knot) {
        switch(mScreenOrientation) {
        case Configuration.ORIENTATION_LANDSCAPE:
            return Spec.LANDSCAPE_MAX_COLUMN_NUM - knot;
        case Configuration.ORIENTATION_PORTRAIT:
            return Spec.PORTRAIT_MAX_COLUMN_NUM - knot;
        default:
            break;
        }
        return UNKNOWN;
    }
    
    private void calculateCurrentKnot(final float scale) {
        if(scale <= mKnotScales[MIN_KNOT_INDEX]) {
            mCurrentKnot = MIN_KNOT_INDEX;
            return;
        }
        if(scale >= mKnotScales[MAX_KNOT_INDEX]) {
            mCurrentKnot = MAX_KNOT_INDEX;
            return;
        }
        for(int i = MIN_KNOT_INDEX + 1; i <= MAX_KNOT_INDEX - 1; i ++) {
            if((mPreviousScale >= mKnotScales[i] && scale <= mKnotScales[i]) || 
                 (mPreviousScale <= mKnotScales[i] && scale >= mKnotScales[i])) {
                //yes, we are crossing the knot [i]
                mCurrentKnot = i;
                break;
            }
        }
    }
    
    private int calculateTurningToKnot(final float scale) {
        /*
        LogUtil.d(TAG, "calculateTurningToKnot scale:" + scale + 
                " mKnotScales[MAX_KNOT_INDEX]:" + mKnotScales[MAX_KNOT_INDEX] +
                " mKnotScales[MIN_KNOT_INDEX]:" + mKnotScales[MIN_KNOT_INDEX]);*/
        int i = mCurrentKnot;
        if(MAX_KNOT_INDEX == i) {
            if(scale >= mKnotScales[MAX_KNOT_INDEX]) {
                return MAX_KNOT_INDEX;
            }
            return MAX_KNOT_INDEX - 1;
        }
        if(MIN_KNOT_INDEX == i) {
            if(scale <= mKnotScales[MIN_KNOT_INDEX]) {
                return MIN_KNOT_INDEX;
            } 
            return MIN_KNOT_INDEX + 1;
        }
        if(scale >= mKnotScales[i + 1]) {
            //LogUtil.e(TAG, "It's logically impossible !, but we still return mCurrentKnot + 1 to ensure the rationality");
            return i + 1;
        } else if(scale < mKnotScales[i + 1] && scale > mKnotScales[i]) {
            return i + 1;
        } else if(scale == mKnotScales[i]) {
            return i;
        } else if(scale > mKnotScales[i - 1] && scale < mKnotScales[i]) {
            return i - 1;
        } else if(scale <= mKnotScales[i - 1]){
            //LogUtil.e(TAG, "It's logically impossible too!, but we still return mCurrentKnot - 1 to ensure the rationality");
            return i - 1;
        }
        return i;
    }
}
