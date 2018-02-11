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

package com.android.gallery3d.ui;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import com.android.gallery3d.R;
import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DateGroupInfos;
import com.android.gallery3d.data.GroupInfo;
import com.android.gallery3d.glrenderer.BitmapTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.StringTexture;
import com.android.gallery3d.ui.AlbumSlidingWindow.SlotViewStatusGetter;
import com.android.gallery3d.ui.SlotViewScaleManager.LeftOverAnimation;
import com.android.gallery3d.util.GalleryUtils;
import com.googlecode.mp4parser.h264.model.PictureParameterSet.PPSExt;

public class SlotView extends GLView implements SlotViewStatusGetter {
    @SuppressWarnings("unused")
    private static final String TAG = "SlotView";

    private static final boolean WIDE = false;
    private static final int INDEX_NONE = -1;

    public static final int RENDER_MORE_PASS = 1;
    public static final int RENDER_MORE_FRAME = 2;
    
    // TCL ShenQianfeng Begin on 2016.11.03
    private long mScaleEndTime = -1;
    // TCL ShenQianfeng End on 2016.11.03
    
    public interface Listener {
        public void onDown(int index);
        public void onUp(boolean followedByLongPress);
        public void onSingleTapUp(int index);
        public void onLongTap(int index);
        public void onScrollPositionChanged(int position, int total);
    }

    public static class SimpleListener implements Listener {
        @Override public void onDown(int index) {}
        @Override public void onUp(boolean followedByLongPress) {}
        @Override public void onSingleTapUp(int index) {}
        @Override public void onLongTap(int index) {}
        @Override public void onScrollPositionChanged(int position, int total) {}
    }

    public static interface SlotRenderer {
        public void prepareDrawing();
        public void onVisibleRangeChanged(int visibleStart, int visibleEnd);
        public void onSlotSizeChanged(int width, int height);
        public int renderSlot(GLCanvas canvas, int index, int pass, int width, int height);

        public void renderString(GLCanvas canvas, String text, boolean dateMode);
        // renderStringWhenScale is always date mode, no month month mode text.
        public void renderDateModeStringWhenScale(GLCanvas canvas, String text, boolean fromDateToMonth, float progress);
        public void renderMonthModeStringWhenScale(GLCanvas canvas, String text, boolean fromDateToMonth ,float progress);
        public void onConfigurationChanged(Configuration config);
    }
    
    // TCL ShenQianfeng Begin on 2016.08.11
    public interface SelectionStatusGetter {
        public boolean isInSelectionMode();
        public void select(int slotIndex);
        public void unselect(int slotIndex);
        public boolean isItemSelected(int slotIndex);
    }
    
    private SelectionStatusGetter mSelectionStatusGetter;
    
    public void setSelectionStatusGetter(SelectionStatusGetter getter) {
        mSelectionStatusGetter = getter;
    }
    // TCL ShenQianfeng End on 2016.08.11

    // TCL ShenQianfeng Begin on 2016.07.26
    private DateGroupInfos mDateGroupInfos = new DateGroupInfos();
    public static final int MSG_SET_DATE_GROUP_INFOS = 100;
    // TCL ShenQianfeng End on 2016.07.26
    
    // TCL ShenQianfeng Begin on 2016.08.11
    private boolean mScrollSelect;
    private boolean mToSelect;
    // TCL ShenQianfeng End on 2016.08.11

    // private final GestureDetector mGestureDetector;
    
    // TCL ShenQianfeng Begin on 2016.06.18
    // add this for two-finger scale
    private final GestureRecognizer mScaleGestureRecognizer;
    private SlotViewScaleManager mSlotViewScaleManager;
    // TCL ShenQianfeng End on 2016.06.18

    private final ScrollerHelper mScroller;
    private final Paper mPaper = new Paper();

    private Listener mListener;
    private UserInteractionListener mUIListener;

    private boolean mMoreAnimation = false;
    private SlotAnimation mAnimation = null;
    private final Layout mLayout = new Layout();
    private int mStartIndex = INDEX_NONE;

    // whether the down action happened while the view is scrolling.
    private boolean mDownInScrolling;
    private int mOverscrollEffect = OVERSCROLL_NONE;//OVERSCROLL_3D;
    private final Handler mHandler;

    private SlotRenderer mRenderer;

    private int[] mRequestRenderSlots = new int[16];

    public static final int OVERSCROLL_3D = 0;
    public static final int OVERSCROLL_SYSTEM = 1;
    public static final int OVERSCROLL_NONE = 2;

    // to prevent allocating memory
    private final Rect mTempRect = new Rect();
    
    //TCL ShenQianfeng Begin on 2016.06.27
    private int mCurrentOrientation;
    private int mScreenWidth;
    
    private int mCurrentFirstGroupIndex;
    private int mCurrentMode;//date mode or month mode;
    
    private int mTouchSlop;
    private int mOneScreenSlotNum = 15;//it's a approximate number of slots;

    private AbstractGalleryActivity mActivity;
    //TCL ShenQianfeng End on 2016.06.27

    public SlotView(final AbstractGalleryActivity activity, Spec spec) {
        // TCL ShenQianfeng Begin on 2016.10.19
        mActivity = activity;
        // TCL ShenQianfeng End on 2016.10.19
        //mGestureDetector = new GestureDetector(activity, new MyGestureListener());
        //TCL ShenQianfeng Begin on 2016.06.18
        initLayoutUnitCount(activity);
        mScaleGestureRecognizer = new GestureRecognizer(activity, new MyScaleGestureListerner());
        mTouchSlop = ViewConfiguration.get(activity).getScaledTouchSlop();
        //TCL ShenQianfeng End on 2016.06.18
        
        mScroller = new ScrollerHelper(activity);
        // TCL ShenQianfeng Begin on 2016.07.26
        // Original:
        //mHandler = new SynchronizedHandler(activity.getGLRoot());
        // Modify To:
        mHandler = new SynchronizedHandler(activity.getGLRoot()) {

            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                case MSG_SET_DATE_GROUP_INFOS: {
                    if(mDateGroupInfos != null) {
                        mDateGroupInfos.clear();
                        mDateGroupInfos = null;
                    }
                    mDateGroupInfos = (DateGroupInfos)msg.obj;
                    mLayout.initLayoutParameters(-1);
                    invalidate();
                    break;
                }
                default:
                    break;
                }
            }
        };
        // TCL ShenQianfeng End on 2016.07.26
        setSlotSpec(spec);
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth =  dm.widthPixels;
        
        // TCL BaiYuan Begin on 2016.11.03
        colors = GalleryUtils.intColorToFloatARGBArray(  activity.getResources().getColor( R.color.red));
        emptyPage.setBackgroundColor(colors);
        Bitmap bitmap = BitmapFactory.decodeResource(activity.getApplicationContext().getResources(), R.drawable.ic_empty);
        mBitmapTexture = new BitmapTexture(bitmap);
        String emtpyText = mActivity.getString(R.string.empty_page_tip);
        SPACE_ICON_TEXT = (int) (SPACE_ICON_TEXT * dm.density);
        mStringTexture = StringTexture.newCustomerInstance(mActivity, emtpyText, TEXT_SIZE_IN_SP, mActivity.getApplicationContext().getColor(R.color.empty_page_tip));
        bounds = (int) (dm.density * EMPTY_ICON_WIDTH_DP);
        // TCL BaiYuan End on 2016.11.03
    }
    
    //TCL ShenQianfeng Begin on 2016.06.27
    
    public void clearDateGroupInfos() {
        mDateGroupInfos.clear();
    }
    
    public void setDateGroupInfos(DateGroupInfos info) {
        mHandler.removeMessages(MSG_SET_DATE_GROUP_INFOS);
        mHandler.obtainMessage(MSG_SET_DATE_GROUP_INFOS, info).sendToTarget();
    }
    
    private void initLayoutUnitCount(Context context) {
        int currentOrientation  = context.getResources().getConfiguration().orientation;
        switch(currentOrientation) {
        case Configuration.ORIENTATION_PORTRAIT:
            mLayout.mUnitCount = Spec.PORTRAIT_DEFAULT_COLUMN_NUM;
            break;
        case Configuration.ORIENTATION_LANDSCAPE:
            mLayout.mUnitCount = Spec.LANDSCAPE_DEFAULT_COLUMN_NUM;
            break;
        case Configuration.ORIENTATION_UNDEFINED:
        default:
            mLayout.mUnitCount = Spec.PORTRAIT_DEFAULT_COLUMN_NUM;
            break;
        }
        mCurrentOrientation = currentOrientation;
    }
    
    public int getCurrentUnitCount() {
        return mLayout.mUnitCount;
    }
    //TCL ShenQianfeng End on 2016.06.27

    public void setSlotRenderer(SlotRenderer slotDrawer) {
        mRenderer = slotDrawer;
        if (mRenderer != null) {
            mRenderer.onSlotSizeChanged(mLayout.mSlotWidth, mLayout.mSlotHeight);
            mRenderer.onVisibleRangeChanged(getVisibleStart(), getVisibleEnd());
        }
    }

    public void setCenterIndex(int index) {
        int slotCount = mLayout.mSlotCount;
        if (index < 0 || index >= slotCount) {
            return;
        }
        Rect rect = mLayout.getSlotRect(index, mTempRect);
        int position = WIDE
                ? (rect.left + rect.right - getWidth()) / 2
                : (rect.top + rect.bottom - getHeight()) / 2;
        setScrollPosition(position);
    }
    
    public void makeSlotVisible(int index) {
        Rect rect = mLayout.getSlotRect(index, mTempRect);
        int visibleBegin = WIDE ? mScrollX : mScrollY;
        int visibleLength = WIDE ? getWidth() : getHeight();
        int visibleEnd = visibleBegin + visibleLength;
        int slotBegin = WIDE ? rect.left : rect.top;
        int slotEnd = WIDE ? rect.right : rect.bottom;

        int position = visibleBegin;
        if (visibleLength < slotEnd - slotBegin) {
            position = visibleBegin;
        } else if (slotBegin < visibleBegin) {
            position = slotBegin;
        } else if (slotEnd > visibleEnd) {
            position = slotEnd - visibleLength;
        }

        setScrollPosition(position);
    }
    
    //TCL ShenQianfeng Begin on 2016.06.28
    public int getScrollPosition() {
        return mLayout.mScrollPosition;
    }
    //TCL ShenQianfeng End on 2016.06.28

    public void setScrollPosition(int position) {
        //LogUtil.d(TAG, "SlotView::setScrollPosition 111 position:" + position + " ");
        position = Utils.clamp(position, 0, mLayout.getScrollLimit());
        //LogUtil.d(TAG, "SlotView::setScrollPosition 222 position:" + position + " ");
        mScroller.setPosition(position);
        updateScrollPosition(position, false);
    }
    
    //TCL ShenQianfeng Begin on 2016.06.29
    // regardless the content length and scroll limit
    public void setScrollPositionDirectly(int position) {
        mScroller.setPosition(position);
    }
    //TCL ShenQianfeng End on 2016.06.29

    public void setSlotSpec(Spec spec) {
        mLayout.setSlotSpec(spec);
    }

    @Override
    public void addComponent(GLView view) {
        super.addComponent(view);
        // TCL BaiYuan Begin on 2016.11.03
        /*
        throw new UnsupportedOperationException();
        */
        // TCL BaiYuan End on 2016.11.03
    }

    @Override
    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        // TCL BaiYuan Begin on 2016.11.03
        emptyPage.layout(0, 0, r - l, b - t);
        
        // TCL BaiYuan End on 2016.11.03
        
        if (!changeSize) return;

        // Make sure we are still at a resonable scroll position after the size
        // is changed (like orientation change). We choose to keep the center
        // visible slot still visible. This is arbitrary but reasonable.
        // TCL ShenQianfeng Begin on 2016.11.07
        // Original:
        /*
        int visibleIndex =
                (mLayout.getVisibleStart() + mLayout.getVisibleEnd()) / 2;*/
        // Modify To:
        int visibleIndex = mLayout.getVisibleStart();
        // TCL ShenQianfeng End on 2016.11.07

        mLayout.setSize(r - l, b - t);
        makeSlotVisible(visibleIndex);
        if (mOverscrollEffect == OVERSCROLL_3D) {
            mPaper.setSize(r - l, b - t);
        }
    }
    
    // TCL ShenQianfeng Begin on 2016.11.17
    @Override
    public void onWindowsInsetsChanged(Rect newWindowInsets) {
        mLayout.initLayoutParameters(-1);
    }
    // TCL ShenQianfeng End on 2016.11.17

    public void startScatteringAnimation(RelativePosition position) {
        mAnimation = new ScatteringAnimation(position);
        mAnimation.start();
        if (mLayout.mSlotCount != 0) invalidate();
    }

    public void startRisingAnimation() {
        mAnimation = new RisingAnimation();
        mAnimation.start();
        if (mLayout.mSlotCount != 0) invalidate();
    }

    private void updateScrollPosition(int position, boolean force) {
        if (!force && (WIDE ? position == mScrollX : position == mScrollY)) return;
        if (WIDE) {
            mScrollX = position;
        } else {
            // TCL ShenQianfeng Begin on 2016.10.29
            if(position <0) {
                position = 0;
            }
            if(position > mLayout.getScrollLimit()) {
                position = mLayout.getScrollLimit();
            }
            // TCL ShenQianfeng End on 2016.10.29
            mScrollY = position;
            //LogUtil.i2(TAG, "updateScrollPosition: mScrollY:" + mScrollY );
        }
        mLayout.setScrollPosition(position);
        onScrollPositionChanged(position);
    }

    protected void onScrollPositionChanged(int newPosition) {
        int limit = mLayout.getScrollLimit();
        mListener.onScrollPositionChanged(newPosition, limit);
    }

    public Rect getSlotRect(int slotIndex) {
        // TCL ShenQianfeng Begin on 2016.08.08
        // Original:
        //return mLayout.getSlotRect(slotIndex, new Rect());
        // Modify To:
        return mLayout.getSlotRect(slotIndex, mTempRect);
        // TCL ShenQianfeng End on 2016.08.08
    }

    @Override
    protected boolean onTouch(MotionEvent event) {
        if (mUIListener != null) mUIListener.onUserInteraction();
        //mGestureDetector.onTouchEvent(event);
        //TCL ShenQianfeng Begin on 2016.06.18
        mScaleGestureRecognizer.onTouchEvent(event);
        //TCL ShenQianfeng End on 2016.06.18
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownInScrolling = !mScroller.isFinished();
                mScroller.forceFinished();
                break;
            case MotionEvent.ACTION_UP:
                mPaper.onRelease();
                invalidate();
                break;
        }
        return true;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setUserInteractionListener(UserInteractionListener listener) {
        mUIListener = listener;
    }

    public void setOverscrollEffect(int kind) {
        mOverscrollEffect = kind;
        mScroller.setOverfling(kind == OVERSCROLL_SYSTEM);
    }

    private static int[] expandIntArray(int array[], int capacity) {
        while (array.length < capacity) {
            array = new int[array.length * 2];
        }
        return array;
    }

    @Override
    protected void render(GLCanvas canvas) {
        long time = System.currentTimeMillis();
        super.render(canvas);
        if (mRenderer == null) return;
        mRenderer.prepareDrawing();
        long animTime = AnimationTime.get();
        boolean more = mScroller.advanceAnimation(animTime);
        // TCL ShenQianfeng Begin on 2016.10.26
        // Annotated Below:
        //more |= mLayout.advanceAnimation(animTime);
        // TCL ShenQianfeng End on 2016.10.26
        int oldX = mScrollX;
        //LogUtil.d(TAG, "SlotView::render ----> mScroller.getPosition(): " + mScroller.getPosition());
        updateScrollPosition(mScroller.getPosition(), false);
        boolean paperActive = false;
        if (mOverscrollEffect == OVERSCROLL_3D) {
            // Check if an edge is reached and notify mPaper if so.
            int newX = mScrollX;
            int limit = mLayout.getScrollLimit();
            if (oldX > 0 && newX == 0 || oldX < limit && newX == limit) {
                float v = mScroller.getCurrVelocity();
                if (newX == limit) v = -v;
                // I don't know why, but getCurrVelocity() can return NaN.
                if (!Float.isNaN(v)) {
                    mPaper.edgeReached(v);
                }
            }
            paperActive = mPaper.advanceAnimation();
        }
        more |= paperActive;
        if (mAnimation != null) {
            more |= mAnimation.calculate(animTime);
        }
        canvas.translate(-mScrollX, -mScrollY);
        //TCL ShenQianfeng Begin on 2016.06.25
        //long time = System.currentTimeMillis();
        renderDateItem(canvas);
        //LogUtil.d(TAG, "============================ time:" + (System.currentTimeMillis() - time));
        //TCL ShenQianfeng End on 2016.06.25
        int requestCount = 0;
        int requestedSlot[] = expandIntArray(mRequestRenderSlots, mLayout.mVisibleEnd - mLayout.mVisibleStart);
        for (int i = mLayout.mVisibleEnd - 1; i >= mLayout.mVisibleStart; --i) {
            int r = renderItem(canvas, i, 0, paperActive);
            if ((r & RENDER_MORE_FRAME) != 0) more = true;
            if ((r & RENDER_MORE_PASS) != 0) requestedSlot[requestCount++] = i;
        }

        for (int pass = 1; requestCount != 0; ++pass) {
            int newCount = 0;
            for (int i = 0; i < requestCount; ++i) {
                int r = renderItem(canvas, requestedSlot[i], pass, paperActive);
                if ((r & RENDER_MORE_FRAME) != 0) more = true;
                if ((r & RENDER_MORE_PASS) != 0) requestedSlot[newCount++] = i;
            }
            requestCount = newCount;
        }
        canvas.translate(mScrollX, mScrollY);
        if (more) invalidate();
        // TCL ShenQianfeng Begin on 2016.07.13
        // Original:
        /*
        final UserInteractionListener listener = mUIListener;
        if (mMoreAnimation && !more && listener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onUserInteractionEnd();
                }
            });
        }
        */
        // Modify To:
        //if(! isSlotViewScrolling()) {
        /*
        if (mMoreAnimation && !more && mUIListener != null && ! isSlotViewScrolling()) {
            mHandler.postDelayed(mRunnable, 10);
        }
        */
        // TCL ShenQianfeng End on 2016.07.13
        //mMoreAnimation = more;
        //TCL ShenQianfeng Begin on 2016.06.30
        if( isScaleLeftoverAnimating() ) {
            LeftOverAnimation leftOverAnimation = (LeftOverAnimation)mAnimation;
            if(leftOverAnimation.getProgress() == 1.0f) {
                leftOverAnimation.forceStop();
                leftOverAnimation.onAnimationDone();
                mAnimation = null;
            }
        }
        //TCL ShenQianfeng End on 2016.06.30
        
        time = System.currentTimeMillis() - time;
        //LogUtil.d(TAG, "SlotView render --> time: " + time);
    }

    public boolean isScaleLeftoverAnimating() {
        return null != mAnimation && (mAnimation instanceof LeftOverAnimation) && mAnimation.isActive();
    }
    
    //TCL ShenQianfeng Begin on 2016.06.25
    private static final int DATE_INFO_JUDGE_DISTANCE = 150;
    
    public int getColumnDisplayMode(final int unitCount) {
        boolean portraitMax = (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT &&
                unitCount == Spec.PORTRAIT_MAX_COLUMN_NUM);
        boolean landscapeMax = (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE && 
                unitCount == Spec.LANDSCAPE_MAX_COLUMN_NUM);
        if(portraitMax || landscapeMax) {
            return DateGroupInfos.MODE_MONTH;
        }
        return DateGroupInfos.MODE_DATE;
    }

    private void renderDateItem(GLCanvas canvas) {
        //long time = System.currentTimeMillis();
        if(mDateGroupInfos.isEmpty()) return;

        final int curScrollPosition = mLayout.mScrollPosition;
        ArrayList<Point> curGroupBounds = mDateGroupInfos.getGroupBounds(mCurrentMode);

        //LogUtil.d(TAG, "renderDateItem scrollPosition:" +  scrollPosition + " firstGroupIndex: " + firstGroupIndex);
        if(INDEX_NONE == mCurrentFirstGroupIndex) {
            //LogUtil.e2(TAG, "firstGroupIndex not found in renderDateItem");
            return;
        }
        boolean isScaling = (null != mSlotViewScaleManager && mSlotViewScaleManager.isScaling());
        boolean leftoverAnimating = false;
        LeftOverAnimation leftOverAnimation = null;
        if(null != mAnimation && mAnimation instanceof LeftOverAnimation) {
            leftOverAnimation = (LeftOverAnimation)mAnimation;
            if(leftOverAnimation.isActive()) {
                leftoverAnimating = true;
            }
        }
        if(isScaling || leftoverAnimating) {
            mSlotViewScaleManager.reset();
        }
        Spec spec = getSpec();
        for(int curGroupIndex = mCurrentFirstGroupIndex; curGroupIndex < curGroupBounds.size(); curGroupIndex++) {
            Point bound = curGroupBounds.get(curGroupIndex);
            int extra = (curGroupIndex == 0 ? spec.slotAreaTopPadding : spec.slotGroupGap);
            int curGroupTop = bound.x /*+ (curFirstGroupIndex == 0 ? getSpec().slotAreaTopPadding : 0)*/ ;
            curGroupTop += extra;
            if(curGroupBounds.get(curGroupIndex).x > curScrollPosition + mLayout.mHeight) {
                break;
            }
            String text = mDateGroupInfos.getGroupDateText(curGroupIndex, mCurrentMode);
            //LogUtil.d(TAG, "renderDateItem text:" +  text + " top:" + top);
            //int drawPos = 0;

            if(isScaling) {
                //LogUtil.d(TAG, "renderDateItem 111111 isScaling:" + isScaling );
                float progress = mSlotViewScaleManager.getProgress();
                mSlotViewScaleManager.applyDateScaleEffect(mRenderer, canvas, 
                        curScrollPosition, curGroupTop, 
                        mCurrentFirstGroupIndex, curGroupIndex,
                        progress, text);
                continue;
            } else if(leftoverAnimating) {
                //LogUtil.d(TAG, "renderDateItem 222222 leftoverAnimating : " + leftoverAnimating);
                float progress = leftOverAnimation.getProgress();
                mSlotViewScaleManager.applyDateScaleEffect(mRenderer, canvas, 
                        curScrollPosition, curGroupTop, 
                        mCurrentFirstGroupIndex, curGroupIndex, 
                        progress, text);
                continue;
            } else {
                //LogUtil.d(TAG, "renderDateItem 333333");
                canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
                if(curGroupIndex == mCurrentFirstGroupIndex) {
                    int nextGroupIndex = curGroupIndex + 1;
                    if(nextGroupIndex < curGroupBounds.size()) {
                        int nextTop = curGroupBounds.get(nextGroupIndex).x;
                        int nextExtra = (nextGroupIndex == 0 ? getSpec().slotAreaTopPadding : spec.slotGroupGap);
                        nextTop += nextExtra;
                        int diff = nextTop - (curScrollPosition /*+ slotAreaTopPadding*/);
                        if(diff < DATE_INFO_JUDGE_DISTANCE && diff > 0) {
                            canvas.translate(0, curScrollPosition - (DATE_INFO_JUDGE_DISTANCE - diff));
                            //LogUtil.d(TAG, "diff: " + diff + " - (DATE_INFO_JUDGE_DISTANCE - diff):" + (- (DATE_INFO_JUDGE_DISTANCE - diff)));
                        } else {
                            if(curGroupIndex == mCurrentFirstGroupIndex && curScrollPosition < curGroupTop) {
                                    canvas.translate(0, curGroupTop);
                            } else {
                                canvas.translate(0, curScrollPosition);
                            }
                        }
                    } else {
                        if(curGroupIndex == 0) {
                            int slotAreaTopPadding = getSpec().slotAreaTopPadding;
                            if(curScrollPosition < slotAreaTopPadding) {
                                canvas.translate(0, slotAreaTopPadding);
                            } else {
                                canvas.translate(0, curScrollPosition);
                            }
                        } else {
                            canvas.translate(0, curGroupTop);
                        }
                    }
                } else {
                    canvas.translate(0, curGroupTop);
                }
                //int mode = getColumnDisplayMode(getCurrentUnitCount());
                boolean dateMode = mCurrentMode == DateGroupInfos.MODE_DATE;
                long rendertime = System.currentTimeMillis();
                mRenderer.renderString(canvas, text, dateMode);
                rendertime = System.currentTimeMillis() - rendertime;
               // LogUtil.d(TAG, "renderDateItem: rendertime:" + rendertime);
                canvas.restore();
            }
        }
        //time = System.currentTimeMillis() - time;
        //LogUtil.d(TAG, "renderDateItem: time:" + time);
    }
    
    
    //TCL ShenQianfeng End on 2016.06.25

    private int renderItem(GLCanvas canvas, int index, int pass, boolean paperActive) {
        canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
        Rect rect = mLayout.getSlotRect(index, mTempRect);
        if (paperActive) {
            canvas.multiplyMatrix(mPaper.getTransform(rect, mScrollX), 0);
        } else {
            canvas.translate(rect.left, rect.top, 0);
        }
        //TCL ShenQianfeng Begin on 2016.06.28
        //Original:
        /*
        if (mAnimation != null && mAnimation.isActive()) {
            mAnimation.apply(canvas, index, rect);
        }
        */
        //Modify To:
        int result = 0;
        if(null != mSlotViewScaleManager && mSlotViewScaleManager.isScaling()) {
            //LogUtil.d(TAG, "renderItem:  111111111111 ");
            mSlotViewScaleManager.applySlotScaleEffect(canvas, index, rect);
        } else if (mAnimation != null && mAnimation.isActive()) {
            //LogUtil.d(TAG, "renderItem:  222222222222 ");
            mAnimation.apply(canvas, index, rect);
            if(mAnimation != null && mAnimation.isActive()) {
                result |= RENDER_MORE_FRAME;
            }
        } /* 
        else {
            //LogUtil.d(TAG, "renderItem:  333333333333 ");
        }
        */
        //TCL ShenQianfeng End on 2016.06.28
        result |= mRenderer.renderSlot(canvas, index, pass, rect.right - rect.left, rect.bottom - rect.top);
        canvas.restore();
        return result;
    }

    public static abstract class SlotAnimation extends Animation {
        protected float mProgress = 0;

        public SlotAnimation() {
            setInterpolator(new DecelerateInterpolator(4));
            setDuration(1500);
        }

        @Override
        protected void onCalculate(float progress) {
            mProgress = progress;
        }

        abstract public void apply(GLCanvas canvas, int slotIndex, Rect target);
    }

    public static class RisingAnimation extends SlotAnimation {
        private static final int RISING_DISTANCE = 128;

        @Override
        public void apply(GLCanvas canvas, int slotIndex, Rect target) {
            canvas.translate(0, 0, RISING_DISTANCE * (1 - mProgress));
        }
    }

    public static class ScatteringAnimation extends SlotAnimation {
        private int PHOTO_DISTANCE = 1000;
        private RelativePosition mCenter;

        public ScatteringAnimation(RelativePosition center) {
            mCenter = center;
        }

        @Override
        public void apply(GLCanvas canvas, int slotIndex, Rect target) {
            canvas.translate(
                    (mCenter.getX() - target.centerX()) * (1 - mProgress),
                    (mCenter.getY() - target.centerY()) * (1 - mProgress),
                    slotIndex * PHOTO_DISTANCE * (1 - mProgress));
            canvas.setAlpha(mProgress);
        }
    }

    // This Spec class is used to specify the size of each slot in the SlotView.
    // There are two ways to do it:
    //
    // (1) Specify slotWidth and slotHeight: they specify the width and height
    //     of each slot. The number of rows and the gap between slots will be
    //     determined automatically.
    // (2) Specify rowsLand, rowsPort, and slotGap: they specify the number
    //     of rows in landscape/portrait mode and the gap between slots. The
    //     width and height of each slot is determined automatically.
    //
    // The initial value of -1 means they are not specified.
    public static class Spec {
        public int slotWidth = -1;
        public int slotHeight = -1;
        public int slotHeightAdditional = 0;

        public int rowsLand = -1;
        public int rowsPort = -1;
        public int slotGap = -1;
        
        //TCL ShenQianfeng Begin on 2016.06.17
        public int slotGapForTwoColumns = 0;
        public int slotGapForThreeColumns = 0;
        public int slotGapForFourColumns = 0;
        public int slotGapForFiveColumns = 0;
        public int slotGapForSixColumns = 0;
        
        public int slotAreaLeftPadding = 0;
        public int slotAreaTopPadding = 0;
        public int slotAreaRightPadding = 0;
        public int slotAreaBottomPadding = 0;
        
        public int slotGroupGap = 0;
        
        public int slotDateAreaWidth = 0;
        
        public static final int PORTRAIT_MIN_COLUMN_NUM = 2;
        public static final int PORTRAIT_MAX_COLUMN_NUM = 6;
        
        public static final int PORTRAIT_LANDSCAPE_DIFF = 4; 
        
        public static final int LANDSCAPE_MIN_COLUMN_NUM = PORTRAIT_MIN_COLUMN_NUM + PORTRAIT_LANDSCAPE_DIFF ;
        public static final int LANDSCAPE_MAX_COLUMN_NUM = PORTRAIT_MAX_COLUMN_NUM + PORTRAIT_LANDSCAPE_DIFF;
        
        public static final int PORTRAIT_DEFAULT_COLUMN_NUM = 4;
        public static final int LANDSCAPE_DEFAULT_COLUMN_NUM = PORTRAIT_DEFAULT_COLUMN_NUM + PORTRAIT_LANDSCAPE_DIFF;
        //TCL ShenQianfeng End on 2016.06.17
        
        public int getSlotGap(final int unitCount) {
            switch(unitCount) {
            case 2:
                return slotGapForTwoColumns;
            case 3:
                return slotGapForThreeColumns;
            case 4:
                return slotGapForFourColumns;
            case 5:
                return slotGapForFiveColumns;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                return slotGapForSixColumns;
            }
            return slotGapForSixColumns;
        }
    }
    
    //TCL ShenQianfeng Begin on 2016.06.20
    /*
    public interface DateGroupInfoComsumer {
        public void consume(int index, Entry<String, Integer> data);
    }
    */
    //TCL ShenQianfeng End on 2016.06.20
    
    public class Layout {

        private int mVisibleStart;
        private int mVisibleEnd;

        private int mSlotCount;
        private int mSlotWidth;
        private int mSlotHeight;
        //private int mSlotGap;

        private Spec mSpec;

        private int mWidth;
        private int mHeight;

        private int mUnitCount = Spec.PORTRAIT_DEFAULT_COLUMN_NUM;//Shenqianfeng added on 2016.6.20
        private int mContentLength;
        private int mScrollPosition;

        // TCL ShenQianfeng Begin on 2016.10.26
        // Annotated Below:
        //private IntegerAnimation mVerticalPadding = new IntegerAnimation();
        //private IntegerAnimation mHorizontalPadding = new IntegerAnimation();
        // TCL ShenQianfeng End on 2016.10.26
        
        public void setSlotSpec(Spec spec) {
            mSpec = spec;
        }

        public boolean setSlotCount(int slotCount) {
            if (slotCount == mSlotCount) return false;
            // TCL ShenQianfeng Begin on 2016.10.26
            // Annotated Below:
            /*
            if (mSlotCount != 0) {
                mHorizontalPadding.setEnabled(true);
                mVerticalPadding.setEnabled(true);
            }
            */
            // TCL ShenQianfeng End on 2016.10.26
            mSlotCount = slotCount;
            // TCL ShenQianfeng Begin on 2016.07.25
            // Original:
            /*
            int hPadding = mHorizontalPadding.getTarget();
            int vPadding = mVerticalPadding.getTarget();
            initLayoutParameters(-1);
            return vPadding != mVerticalPadding.getTarget()
                    || hPadding != mHorizontalPadding.getTarget();
             */
            // Modify To:
            initLayoutParameters(-1);
            return true;
            // TCL ShenQianfeng End on 2016.07.25
            
        }
        
        //TCL ShenQianfeng Begin on 2016.06.20
        public int getSlotWidthByUnitCount(int unitCount) {
            //LogUtil.d(TAG, "getSlotWidthByUnitCount : unitCount:" + unitCount + " mWidth:" + mWidth);
            int wholeWidth = mWidth;
            if(wholeWidth == 0) {
                // mWidth may be 0, so we need to set it to screen width as default , in order to avoid crash.
                wholeWidth = mScreenWidth;
            }
            int slotGap = mSpec.getSlotGap(unitCount);
            return (wholeWidth - mSpec.slotDateAreaWidth - 
                    mSpec.slotAreaLeftPadding - 
                    mSpec.slotAreaRightPadding - 
                    (unitCount - 1) * slotGap) / unitCount;
        }
        
        private Rect getSlotRect(int index, int unitCount, int slotWidth, int slotHeight, Rect rect, int mode) {
            DateGroupInfos dateGroupInfos = getDateGroupInfos();
            //modify by liaoah begin
            if (null == dateGroupInfos) {
                 rect.set(0,0,0,0);
                 return rect;
            }
            //modify end
            DateGroupInfos.SlotPositionInfo slotPositionInfo = new DateGroupInfos.SlotPositionInfo();
            dateGroupInfos.getSlotPositionInfo(index, slotPositionInfo, unitCount, mode);
            int groupIndex = slotPositionInfo.mGroupIndex;
            Point groupBound = dateGroupInfos.getGroupBound(groupIndex, unitCount, mSpec, slotHeight, mode);
            if(null == groupBound) {
                rect.set(0, 0, 0, 0);
                return rect;
            }
            int slotGap = mSpec.getSlotGap(unitCount);
            int x = mSpec.slotDateAreaWidth + mSpec.slotAreaLeftPadding + 
                    slotPositionInfo.mColumn * (slotWidth + slotGap);
            int y = groupBound.x + slotPositionInfo.mRow * (slotHeight + slotGap) +
                            (0 == groupIndex ? mSpec.slotAreaTopPadding : mSpec.slotGroupGap );
            rect.set(x, y, x + slotWidth, y + slotHeight);
            /*
            LogUtil.d(TAG, "RECT index:" + index +  " x:" + x + " y:" + y + " group:" + slotPositionInfo.mGroupIndex + 
                                            " row in group:" + slotPositionInfo.mRow +
                                            " column in group:" + slotPositionInfo.mColumn);
                                            */
            return rect;
        }
        
        public Rect getSlotRect(int index, int unitCount, Rect rect, int mode) {
            int slotWidth = getSlotWidthByUnitCount(unitCount);
            int slotHeight = slotWidth;
            return getSlotRect(index, unitCount, slotWidth, slotHeight, rect, mode);
        }
        //TCL ShenQianfeng End on 2016.06.20
        
        /*
         * return rect in current mode[MODE_DATE, MODE_MONTH], not the mode turning to.
         */
        public Rect getSlotRect(int index, Rect rect) {
            int currentMode = getColumnDisplayMode(mUnitCount);
            return getSlotRect(index, mUnitCount, rect, currentMode);
            //TCL ShenQianfeng Begin on 2016.06.18
            //Original:
            /*
            int col, row;
            if (WIDE) {
                col = index / mUnitCount;
                row = index - col * mUnitCount;
            } else {
                row = index / mUnitCount;
                col = index - row * mUnitCount;
            }
            int x = mHorizontalPadding.get() + col * (mSlotWidth + mSlotGap);
            int y = mVerticalPadding.get() + row * (mSlotHeight + mSlotGap);
            rect.set(x, y, x + mSlotWidth, y + mSlotHeight);
            return rect;
            */
            //Modify To:
            
            //TCL ShenQianfeng End on 2016.06.18
            
        }

        public int getSlotWidth() {
            return mSlotWidth;
        }

        public int getSlotHeight() {
            return mSlotHeight;
        }

        // Calculate
        // (1) mUnitCount: the number of slots we can fit into one column (or row).
        // (2) mContentLength: the width (or height) we need to display all the
        //     columns (rows).
        // (3) padding[]: the vertical and horizontal padding we need in order
        //     to put the slots towards to the center of the display.
        //
        // The "major" direction is the direction the user can scroll. The other
        // direction is the "minor" direction.
        //
        // The comments inside this method are the description when the major
        // directon is horizontal (X), and the minor directon is vertical (Y).

        // TCL ShenQianfeng Begin on 2016.07.29
        // Annotated Below:
        /*
        private void initLayoutParameters(
                int majorLength, int minorLength,  // The view width and height 
                int majorUnitSize, int minorUnitSize,  // The slot width and height 
                int[] padding) {
            int unitCount = (minorLength + mSlotGap) / (minorUnitSize + mSlotGap);
            if (unitCount == 0) unitCount = 1;
            mUnitCount = unitCount;

            // We put extra padding above and below the column.
            int availableUnits = Math.min(mUnitCount, mSlotCount);
            int usedMinorLength = availableUnits * minorUnitSize +
                    (availableUnits - 1) * mSlotGap;
            padding[0] = (minorLength - usedMinorLength) / 2;

            // Then calculate how many columns we need for all slots.
            int count = ((mSlotCount + mUnitCount - 1) / mUnitCount);
            mContentLength = count * majorUnitSize + (count - 1) * mSlotGap;

            // If the content length is less then the screen width, put
            // extra padding in left and right.
            padding[1] = Math.max(0, (majorLength - mContentLength) / 2);
        }
        */
        // TCL ShenQianfeng End on 2016.07.29
        
        //TCL ShenQianfeng Begin on 2016.06.20
        //Original:
        /*
        private void initLayoutParameters() {
            // Initialize mSlotWidth and mSlotHeight from mSpec
            if (mSpec.slotWidth != -1) {
                mSlotGap = 0;
                mSlotWidth = mSpec.slotWidth;
                mSlotHeight = mSpec.slotHeight;
            } else {
                int rows = (mWidth > mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
                mSlotGap = mSpec.slotGap;
                mSlotHeight = Math.max(1, (mHeight - (rows - 1) * mSlotGap) / rows);
                mSlotWidth = mSlotHeight - mSpec.slotHeightAdditional;
            }

            if (mRenderer != null) {
                mRenderer.onSlotSizeChanged(mSlotWidth, mSlotHeight);
            }

            int[] padding = new int[2];
            if (WIDE) {
                initLayoutParameters(mWidth, mHeight, mSlotWidth, mSlotHeight, padding);
                mVerticalPadding.startAnimateTo(padding[0]);
                mHorizontalPadding.startAnimateTo(padding[1]);
            } else {
                initLayoutParameters(mHeight, mWidth, mSlotHeight, mSlotWidth, padding);
                mVerticalPadding.startAnimateTo(padding[1]);
                mHorizontalPadding.startAnimateTo(padding[0]);
            }
            updateVisibleSlotRange();
        }
        */
        //Modify To:

        public void initLayoutParameters(int unitCount) {
            if(-1 != unitCount) {
                mUnitCount = unitCount;
            }
            mCurrentMode = getColumnDisplayMode(getCurrentUnitCount());
            int mode = getColumnDisplayMode(mUnitCount);
            mSlotWidth = getSlotWidthByUnitCount(mUnitCount);
            mSlotHeight = mSlotWidth;
            /*
            LogUtil.i(TAG, "initLayoutParameters unitCount:" + unitCount  + " mSlotWidth:" + mSlotWidth + " mSlotHeight:" + mSlotHeight + 
                    " mWidth:" + mWidth + " mHeight:" + mHeight); */
            /*
            if (mRenderer != null) {
                mRenderer.onSlotSizeChanged(mSlotWidth, mSlotHeight);
            }
            */
            DateGroupInfos dateGroupInfos = getDateGroupInfos();
            if(dateGroupInfos == null) return;
            dateGroupInfos.initGroupBounds(mUnitCount, mSpec, mSlotHeight);
            mContentLength = dateGroupInfos.getContentLength(mSpec, mode);
            
            int displayRotation = mActivity.getGLRoot().getDisplayRotation();
            /*
            if(displayRotation == 0 || displayRotation == 180) {
                mContentLength += mActivity.getSystemUIManager().getNavigationBarHeight();
            }
            */
            mContentLength += mActivity.getWindowInsets().bottom;
            
            if(mSelectionStatusGetter.isInSelectionMode()) {
                mContentLength += mActivity.getSystemUIManager().getActionBarHeight();
            }
            //LogUtil.i(TAG, "initLayoutParameters mContentLength:" + mContentLength);
            //modify by liaoah begin
            if(mContentLength < mHeight) {
                //mScrollPosition = Math.min(mScrollPosition, mContentLength - mHeight);
                mScrollPosition = 0;
            } else {
                mScrollPosition = Math.min(mScrollPosition, mContentLength - mHeight);
            }
            setScrollPositionDirectly(mScrollPosition);
            //modify end
            updateVisibleSlotRange();
        }
        
        private void initLayoutParametersForScaling(int unitCount, int position) {
            if(-1 != unitCount) {
                mUnitCount = unitCount;
            }
            //int mode = getColumnDisplayMode(mUnitCount);
            mCurrentMode = getColumnDisplayMode(getCurrentUnitCount());
            mScrollPosition = position;//add this line 
            /*
            mSlotWidth = (mWidth - mSpec.slotDateAreaWidth - mSpec.slotAreaLeftPadding - mSpec.slotAreaRightPadding) / mUnitCount
                                            - (mSpec.slotLeftPadding + mSpec.slotRightPadding);
            */
            mSlotWidth = getSlotWidthByUnitCount(mUnitCount);
            mSlotHeight = mSlotWidth;
            /*
            LogUtil.d(TAG, "initLayoutParametersForScaling unitCount:" + unitCount  + 
                    " mSlotWidth:" + mSlotWidth + 
                    " mSlotHeight:" + mSlotHeight + 
                    " mWidth:" + mWidth + 
                    " mHeight:" + mHeight); */
            /*
            if (mRenderer != null) {
                mRenderer.onSlotSizeChanged(mSlotWidth, mSlotHeight);
            }
            */
            DateGroupInfos dateGroupInfos = getDateGroupInfos();
            dateGroupInfos.initGroupBounds(mUnitCount, mSpec, mSlotHeight);
            mContentLength = dateGroupInfos.getContentLength(mSpec, mCurrentMode);
            int displayRotation = mActivity.getGLRoot().getDisplayRotation();
            if(displayRotation == 0 || displayRotation == 180) {
                mContentLength += mActivity.getSystemUIManager().getNavigationBarHeight();
            }
            //adjust scroll position here.
            if(mContentLength < mHeight) {
                //mScrollPosition = Math.min(mScrollPosition, mContentLength - mHeight);
                mScrollPosition = 0;
            } else {
                mScrollPosition = Math.min(mScrollPosition, mContentLength - mHeight);
            }
            //LogUtil.i(TAG, "initLayoutParametersForScaling mContentLength:" + mContentLength);
            updateVisibleSlotRangeForScale();
        }

        public void setScrollPositionInLayout(final int scrollPosition) {
            mLayout.mScrollPosition = scrollPosition;
        }

        //TCL ShenQianfeng End on 2016.06.20
        public void setSize(int width, int height) {
            //LogUtil.d(TAG, "Layout --> setSize=width:" + width + " height:" + height);
            mWidth = width;
            mHeight = height;
            //TCL ShenQianfeng Begin on 2016.06.20
            //Original:
            //initLayoutParameters();
            //Modify To:
            initLayoutParameters(-1);//
            //TCL ShenQianfeng End on 2016.06.20
        }

        //TCL ShenQianfeng Begin on 2016.06.20
        //Original:
        /*
        private void updateVisibleSlotRange() {
            int position = mScrollPosition;

            if (WIDE) {
                int startCol = position / (mSlotWidth + mSlotGap);
                int start = Math.max(0, mUnitCount * startCol);
                int endCol = (position + mWidth + mSlotWidth + mSlotGap - 1) /
                        (mSlotWidth + mSlotGap);
                int end = Math.min(mSlotCount, mUnitCount * endCol);
                setVisibleRange(start, end);
            } else {
                int startRow = position / (mSlotHeight + mSlotGap);
                int start = Math.max(0, mUnitCount * startRow);
                int endRow = (position + mHeight + mSlotHeight + mSlotGap - 1) /
                        (mSlotHeight + mSlotGap);
                int end = Math.min(mSlotCount, mUnitCount * endRow);
                setVisibleRange(start, end);
            }
        }
        */
        //Modify To:
        public void updateVisibleSlotRange() {
            //LogUtil.d(TAG, "updateVisibleSlotRange -- ");
            int mode = getColumnDisplayMode(getCurrentUnitCount());
            int position = mScrollPosition;
            int start = getDateGroupInfos().getFirstSlotIndexByPosition(position, mUnitCount, mSpec, mSlotHeight, mode);
            //LogUtil.d(TAG, "updateVisibleSlotRange -- getFirstSlotIndexByPosition  position:"  + position + " start: " +  start);
            start = Math.max(0, start - 5);
            int slotGap = mSpec.getSlotGap(mUnitCount);
            mOneScreenSlotNum = (mHeight / (mSlotHeight + slotGap) + 1 ) * mUnitCount;
            int end  = start + mOneScreenSlotNum + 30; //int end  = start + mOneScreenSlotNum + 30;
            end = Math.min(end, mSlotCount);
            end = Math.min(end, start + AlbumSlotRenderer.CACHE_SIZE);
            //LogUtil.d(TAG, "updateVisibleSlotRange -- start: " +  start + " end:" + end);
            setVisibleRange(start, end);
            mCurrentFirstGroupIndex = mDateGroupInfos.getGroupByPosition(mScrollPosition, mCurrentMode);
        }
        
        public void updateVisibleSlotRangeForScale() {
            //LogUtil.d(TAG, "updateVisibleSlotRangeForScale -- unitCount:" + mUnitCount);
            int mode = getColumnDisplayMode(getCurrentUnitCount());
            int position = mScrollPosition;
            int start = getDateGroupInfos().getFirstSlotIndexByPosition(position, mUnitCount, mSpec, mSlotHeight, mode);
            //LogUtil.d(TAG, "updateVisibleSlotRange -- getFirstSlotIndexByPosition  position:"  + position + " start: " +  start);
            start = Math.max(0, start - 5);
            int slotGap = mSpec.getSlotGap(mUnitCount);
            mOneScreenSlotNum = (mHeight / (mSlotHeight + slotGap) + 1 ) * mUnitCount;
            int end  = start + mOneScreenSlotNum + 40;
            end = Math.min(end, mSlotCount);
            end = Math.min(end, start + AlbumSlotRenderer.CACHE_SIZE);
            //LogUtil.d(TAG, "updateVisibleSlotRange -- start: " +  start + " end:" + end);
            setVisibleRange(start, end);
            mCurrentFirstGroupIndex = mDateGroupInfos.getGroupByPosition(mScrollPosition, mCurrentMode);
        }
        //TCL ShenQianfeng End on 2016.06.20
        

        public void setScrollPosition(int position) {
            if (mScrollPosition == position) return;
            mScrollPosition = position;
            updateVisibleSlotRange();
        }

        private void setVisibleRange(int start, int end) {
            if (start == mVisibleStart && end == mVisibleEnd) return;
            if (start < end) {
                mVisibleStart = start;
                mVisibleEnd = end;
            } else {
                mVisibleStart = mVisibleEnd = 0;
            }
            if (mRenderer != null) {
                //LogUtil.d(TAG, "mRenderer onVisibleRangeChanged mVisibleStart:" + mVisibleStart + " mVisibleEnd:" + mVisibleEnd);
                mRenderer.onVisibleRangeChanged(mVisibleStart, mVisibleEnd);
            }
        }

        public int getVisibleStart() {
            return mVisibleStart;
        }

        public int getVisibleEnd() {
            return mVisibleEnd;
        }

        //TCL ShenQianfeng Begin on 2016.06.24
        //Original:
        /*
        public int getSlotIndexByPosition(float x, float y) {
            int absoluteX = Math.round(x) + (WIDE ? mScrollPosition : 0);
            int absoluteY = Math.round(y) + (WIDE ? 0 : mScrollPosition);

            absoluteX -= mHorizontalPadding.get();
            absoluteY -= mVerticalPadding.get();

            if (absoluteX < 0 || absoluteY < 0) {
                return INDEX_NONE;
            }

            int columnIdx = absoluteX / (mSlotWidth + mSlotGap);
            int rowIdx = absoluteY / (mSlotHeight + mSlotGap);

            if (!WIDE && columnIdx >= mUnitCount) {
                return INDEX_NONE;
            }

            if (WIDE && rowIdx >= mUnitCount) {
                return INDEX_NONE;
            }

            if (absoluteX % (mSlotWidth + mSlotGap) >= mSlotWidth) {
                return INDEX_NONE;
            }

            if (absoluteY % (mSlotHeight + mSlotGap) >= mSlotHeight) {
                return INDEX_NONE;
            }

            int index = WIDE
                    ? (columnIdx * mUnitCount + rowIdx)
                    : (rowIdx * mUnitCount + columnIdx);

            return index >= mSlotCount ? INDEX_NONE : index;
        }
        */
        //Modify To:
        public int getSlotIndexByPosition(float x, float y) {
            int mode = getColumnDisplayMode(getCurrentUnitCount());
            int absoluteX = Math.round(x) + (WIDE ? mScrollPosition : 0);
            int absoluteY = Math.round(y) + (WIDE ? 0 : mScrollPosition);
            if (absoluteX < 0 || absoluteY < 0) {
                return INDEX_NONE;
            }
            DateGroupInfos infos = getDateGroupInfos();
            int groupIndex = infos.getGroupByPosition(absoluteY, mode);
            if(INDEX_NONE == groupIndex) {
                return INDEX_NONE;
            }
            Point groupBound = infos.getGroupBound(groupIndex, mode);
            if(null == groupBound) {
                return INDEX_NONE;
            }
            int rowTop = 0;
            int rowBottom = 0;
            if(0 == groupIndex) {
                rowTop = groupBound.x + mSpec.slotAreaTopPadding;
            } else {
                rowTop = groupBound.x + mSpec.slotGroupGap;
            }
            //It's white padding between [  groupBound.x,   first rowTop)
            if(absoluteY >= groupBound.x && absoluteY < rowTop) {
                //LogUtil.d(TAG, "getSlotIndexByPosition  absoluteY: " + absoluteY + " groupBound.x:" + groupBound.x + " rowTop:" + rowTop);
                return INDEX_NONE;
            }
            int totalSlotsNumBefore = infos.getTotalSlotNumBeforeGroup(groupIndex, mode);
            int rowNumInThisGroup = infos.getRowNumInGroup(groupIndex, mUnitCount, mode);
            int slotGap = mSpec.getSlotGap(mUnitCount);
            for(int i = 0; i < rowNumInThisGroup; i++) {
                rowBottom = rowTop + mSlotHeight;
                //LogUtil.d(TAG, " scrollPosition: " + scrollPosition + " rowTop:" + rowTop + " rowBottom:" + rowBottom);
                if(absoluteY >= rowTop && absoluteY <= rowBottom) {
                    for(int j = 0; j < mUnitCount; j ++ ) {
                        int left = mSpec.slotDateAreaWidth + mSpec.slotAreaLeftPadding + j * (slotGap + mSlotWidth);
                        int right = left + mSlotWidth;
                        
                        if(absoluteX >= left && absoluteX <= right) {
                            GroupInfo groupInfo = infos.getValueByGroupIndex(groupIndex, mode);
                            if(null == groupInfo) return INDEX_NONE;
                            if(i * mUnitCount + j >= groupInfo.mNumOfCurrentGroup) {
                                return INDEX_NONE;
                            }
                            return totalSlotsNumBefore + i * mUnitCount + j;
                        }
                    }
                }
                rowTop = rowBottom + slotGap;
            }
            return INDEX_NONE;
        }
        //TCL ShenQianfeng End on 2016.06.24
        
        public int getScrollLimit() {
            int limit = WIDE ? mContentLength - mWidth : mContentLength - mHeight;
            //LogUtil.d(TAG, "getScrollLimit limit:" + limit + " mContentLength:" + mContentLength + " mHeight:" + mHeight + " mWidth:" + mWidth);
            return limit <= 0 ? 0 : limit;
        }
        // TCL ShenQianfeng Begin on 2016.10.26
        // Annotated Below:
        /*
        public boolean advanceAnimation(long animTime) {
            // use '|' to make sure both sides will be executed
            return mVerticalPadding.calculate(animTime) | mHorizontalPadding.calculate(animTime);
        }
        */
        // TCL ShenQianfeng End on 2016.10.26
    }

    //TCL ShenQianfeng Begin on 2016.06.28
    
    public DateGroupInfos getDateGroupInfos() {
        return mDateGroupInfos;
    }
    
    public Layout getLayout() {
        return mLayout;
    }
    
    public Spec getSpec() {
        return mLayout.mSpec;
    }
    
    public void switchToColumn(final int prevColumnNum, final int switchToColumnNum, int scrollPosition) {
        mLayout.initLayoutParametersForScaling(switchToColumnNum, scrollPosition);
    }
    //TCL ShenQianfeng End on 2016.06.28

    //MyScaleGestureListerner was added by ShenQianfeng
    private class MyScaleGestureListerner implements GestureRecognizer.Listener {
        
        // TCL ShenQianfeng Begin on 2016.11.03
        private boolean isDown;
        // TCL ShenQianfeng End on 2016.11.03

        @Override
        public boolean onSingleTapUp(float x, float y) {
            cancelDown(false);
            if (mDownInScrolling) return true;
            int index = mLayout.getSlotIndexByPosition(x, y);
            if (index != INDEX_NONE) mListener.onSingleTapUp(index);
            //LogUtil.d(TAG, "MyGestureListener:: onSingleTapUp -------- ");
            return true;
        }

        @Override
        public boolean onDoubleTap(float x, float y) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            long currentTime = System.currentTimeMillis();
            if(currentTime - mScaleEndTime < 100) {
                //LogUtil.d(TAG, "onScroll......currentTime - mScaleEndTime < 100 return ...");
                return false;
            }
            float deltaX = Math.abs(e2.getX() - e1.getX());
            float deltaY = Math.abs(e2.getY() - e1.getY());
            //LogUtil.i(TAG, "onScroll 1111111 ---> distanceX:" + distanceX + " distanceY:" + distanceY + " deltaX:"+ deltaX + " deltaY:" + deltaY);
            //LogUtil.i(TAG, " e1:" + e1.getX() + " " + e1.getY() + " e2:" + e2.getX() + " " + e2.getY() );
            if( ! mScrollSelect && mSelectionStatusGetter.isInSelectionMode()) {
                if(deltaX == 0 && deltaY > 0) {
                    mScrollSelect = true;
                } else {
                    float tangent = deltaY / deltaX;
                    if(tangent < 0.57735f) {
                        mScrollSelect = true;
                    }
                }
            }
            if(mScrollSelect) {
                int index = mLayout.getSlotIndexByPosition(e2.getX(), e2.getY());
                if(mToSelect) {
                    mSelectionStatusGetter.select(index);
                } else {
                    mSelectionStatusGetter.unselect(index);
                }
                return false;
            }
            // TCL ShenQianfeng Begin on 2016.08.08
            if(deltaY < mTouchSlop || e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
                /*
                LogUtil.d(TAG, "onScroll 22222............. mTouchSlop:" + mTouchSlop + " e2.getY() - e1.getY():" + (e2.getY() - e1.getY()) + 
                        " e1.getPointerCount():" + e1.getPointerCount() + " e2.getPointerCount():" + e2.getPointerCount()); */
                return false;
            }
            
            if(null != mSlotViewScaleManager && mSlotViewScaleManager.isScaling()) {
                //LogUtil.d(TAG, "onScroll...333333..........");
                invalidate();
                return true;
            }
            // TCL ShenQianfeng End on 2016.08.08
            cancelDown(false);
            float distance = WIDE ? distanceX : distanceY;
            //LogUtil.d(TAG, "onScroll...444444...........");
            int overDistance = mScroller.startScroll(Math.round(distance), 0, mLayout.getScrollLimit());
            if (mOverscrollEffect == OVERSCROLL_3D && overDistance != 0) {
                mPaper.overScroll(overDistance);
            }
            invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            long currentTime = System.currentTimeMillis();
            if(currentTime - mScaleEndTime < 100) {
                //LogUtil.d(TAG, "onFling......currentTime - mScaleEndTime < 100 return ...");
                return false;
            }
            //LogUtil.d(TAG, "onFling......e1:" + e1.getX() + " " + e1.getY() + " e2:" + e2.getX() + " " + e2.getY());
            cancelDown(false);
            int scrollLimit = mLayout.getScrollLimit();
            if (scrollLimit == 0) return false;
            float velocity = WIDE ? velocityX : velocityY;
            mScroller.fling((int) -velocity, 0, scrollLimit);
            if (mUIListener != null) mUIListener.onUserInteractionBegin();
            //LogUtil.d(TAG, "MyGestureListener:: onFling -------- scrollLimit");
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(float focusX, float focusY) {
            if(mSelectionStatusGetter.isInSelectionMode()) {
                return false;
            }
            //LogUtil.d(TAG, "onScaleBegin  focusX:" + focusX + " focusY:" + focusY);
            if(null == mDateGroupInfos) return false;
            int currentUnitCount = getCurrentUnitCount();
            mLayout.updateVisibleSlotRangeForScale();
            int mode = getColumnDisplayMode(currentUnitCount);
            DateGroupInfos infos = mDateGroupInfos;
            int firstSlot = infos.getFirstSlotIndexByPosition(mLayout.mScrollPosition, 
                    currentUnitCount, 
                    mLayout.mSpec, 
                    mLayout.mSlotHeight, 
                    mode);
            mSlotViewScaleManager = new SlotViewScaleManager(SlotView.this, 
                    mCurrentOrientation, 
                    currentUnitCount, 
                    firstSlot);
            return true;
        }

        @Override
        public boolean onScale(float focusX, float focusY, float scale) {
            //LogUtil.d(TAG, "MyGestureListener:: onScale  focusX:" + focusX + " focusY:" + focusY + " scale:" + scale);
            if(null != mSlotViewScaleManager) {
                mSlotViewScaleManager.performScale(scale);
                invalidate();
            }
            return false;
        }

        @Override
        public void onScaleEnd() {
            //LogUtil.d(TAG, "MyGestureListener:: onScaleEnd");
            if(null != mSlotViewScaleManager) {
                mSlotViewScaleManager.setIsScaling(false);
                int visibleStart = mSlotViewScaleManager.getVisibleStart();
                mAnimation = mSlotViewScaleManager.doLeftAnimation(visibleStart);
            }
            mScaleEndTime = System.currentTimeMillis();
        }

        @Override
        public void onDown(float x, float y) {
            long currentTime = System.currentTimeMillis();
            if(mScaleEndTime != -1 && currentTime - mScaleEndTime > 100) {
                //LogUtil.d(TAG, "onDown......reset mScaleEndTime ");
                mScaleEndTime = -1;
            }
            //LogUtil.d(TAG, "MyGestureListener:: onDown -------- ");
            // TCL ShenQianfeng Begin on 2016.08.11
            if(mSelectionStatusGetter.isInSelectionMode()) {
                int index = mLayout.getSlotIndexByPosition(x, y);
                mToSelect = ! mSelectionStatusGetter.isItemSelected(index);
            }
            // TCL ShenQianfeng End on 2016.08.11
        }

        @Override
        public void onUp() {
            // TCL ShenQianfeng Begin on 2016.07.12
            mScrollSelect = false;
            // TCL ShenQianfeng End on 2016.07.12
        }
        
        // TCL ShenQianfeng Begin on 2016.11.03
        @Override
        public void onShowPress(MotionEvent e) {
            GLRoot root = getGLRoot();
            root.lockRenderThread();
            try {
                if (isDown) return;
                int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
                if (index != INDEX_NONE) {
                    isDown = true;
                    mListener.onDown(index);
                }
            } finally {
                root.unlockRenderThread();
            }
        }
        
        private void cancelDown(boolean byLongPress) {
            if (!isDown) return;
            isDown = false;
            mListener.onUp(byLongPress);
        }
        
        @Override
        public void onLongPress(MotionEvent e) {
            cancelDown(true);
            if (mDownInScrolling) return;
            lockRendering();
            try {
                int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
                if (index != INDEX_NONE) mListener.onLongTap(index);
            } finally {
                unlockRendering();
            }
        }
        // TCL ShenQianfeng End on 2016.11.03
    }

    public void setStartIndex(int index) {
        mStartIndex = index;
    }

    // Return true if the layout parameters have been changed
    public boolean setSlotCount(int slotCount) {
        boolean changed = mLayout.setSlotCount(slotCount);

        // mStartIndex is applied the first time setSlotCount is called.
        if (mStartIndex != INDEX_NONE) {
            setCenterIndex(mStartIndex);
            mStartIndex = INDEX_NONE;
        }
        // Reset the scroll position to avoid scrolling over the updated limit.
        setScrollPosition(WIDE ? mScrollX : mScrollY);
        return changed;
    }

    public int getVisibleStart() {
        return mLayout.getVisibleStart();
    }

    public int getVisibleEnd() {
        return mLayout.getVisibleEnd();
    }

    public int getScrollX() {
        return mScrollX;
    }

    public int getScrollY() {
        return mScrollY;
    }
    
    //TCL ShenQianfeng Begin on 2016.06.28
    public Rect getSlotRect(int index, int unitCount, Rect rect, int mode) {
        return mLayout.getSlotRect(index, unitCount, rect, mode);
    }
    //TCL ShenQianfeng End on 2016.06.28

    public Rect getSlotRect(int slotIndex, GLView rootPane) {
        // Get slot rectangle relative to this root pane.
        Rect offset = new Rect();
        rootPane.getBoundsOf(this, offset);
        Rect r = getSlotRect(slotIndex);
        r.offset(offset.left - getScrollX(), offset.top - getScrollY());
        return r;
    }

    private static class IntegerAnimation extends Animation {
        private int mTarget;
        private int mCurrent = 0;
        private int mFrom = 0;
        private boolean mEnabled = false;

        public void setEnabled(boolean enabled) {
            mEnabled = enabled;
        }

        public void startAnimateTo(int target) {
            if (!mEnabled) {
                mTarget = mCurrent = target;
                return;
            }
            if (target == mTarget) return;

            mFrom = mCurrent;
            mTarget = target;
            setDuration(180);
            start();
        }

        public int get() {
            return mCurrent;
        }

        public int getTarget() {
            return mTarget;
        }

        @Override
        protected void onCalculate(float progress) {
            mCurrent = Math.round(mFrom + progress * (mTarget - mFrom));
            if (progress == 1f) mEnabled = false;
        }
    }
    
    //TCL ShenQianfeng Begin on 2016.06.27
    
    private int mapUnitCountToTheOtherOrientation(final int currentOrientation , final int changeToOrientation, final int currentUnitCount) {
        int unitCount = currentUnitCount;
        switch(changeToOrientation) {
        case Configuration.ORIENTATION_PORTRAIT:
            if(currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                unitCount = currentUnitCount - Spec.PORTRAIT_LANDSCAPE_DIFF;
            }
            break;
        case Configuration.ORIENTATION_LANDSCAPE:
            if(currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                unitCount = currentUnitCount + Spec.PORTRAIT_LANDSCAPE_DIFF;
            }
            break;
        case Configuration.ORIENTATION_UNDEFINED:
        default:
            break;
        }
        return unitCount;
    }
    
    public void onConfigurationChanged(Configuration config) {
        //change locale 
        mRenderer.onConfigurationChanged(config);
        //change orientation
        int changeToOrientation = config.orientation;
        if(mCurrentOrientation == changeToOrientation) return;
        mLayout.mUnitCount = mapUnitCountToTheOtherOrientation(mCurrentOrientation, changeToOrientation, mLayout.mUnitCount);
        mCurrentOrientation = changeToOrientation;
    }
    
    @Override
    public boolean isSlotViewScrolling() {
        return (mScroller != null && ! mScroller.isFinished());
    }
    
    //TCL ShenQianfeng End on 2016.06.27
    
    // TCL BaiYuan Begin on 2016.11.03
    private float[] colors ;
    private BitmapTexture mBitmapTexture ;
    private StringTexture mStringTexture;
    private boolean isAttached;
    private final int EMPTY_TOP_DP_PA_PORTRAIT = 160;
    private final int EMPTY_TOP_DP_PA_LANDSCAPE = 52;
    private int bitmapStartX;
    private int bitmapStartY;
    private int stringStartX;
    private int stringStartY;
    private final int EMPTY_ICON_WIDTH_DP = 80;
    private int bounds;
    private int SPACE_ICON_TEXT = 9;
    private final int TEXT_SIZE_IN_SP = 16;
    private GLView emptyPage = new GLView(){
        
        @Override
        protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
            // TCL BaiYuan Begin on 2016.11.07
            DisplayMetrics metrics = mActivity.getAndroidContext().getResources().getDisplayMetrics(); 
            float yStart;
            int width = right - left;
            if (Configuration.ORIENTATION_PORTRAIT == mActivity.getApplicationContext().getResources().getConfiguration().orientation) {
                yStart = metrics.density * EMPTY_TOP_DP_PA_PORTRAIT;
            }else{
                yStart = metrics.density * EMPTY_TOP_DP_PA_LANDSCAPE;
            }
            bitmapStartX = (width - bounds) / 2;
            bitmapStartY = (int) yStart;
            stringStartX = (width - mStringTexture.getWidth()) / 2 + 2;         // 微调2px？
            stringStartY = bitmapStartY + bounds + SPACE_ICON_TEXT;
            // TCL BaiYuan End on 2016.11.07
        }
        
        @Override
        protected void render(GLCanvas canvas) {
            canvas.save();
            canvas.drawTexture(mBitmapTexture, bitmapStartX, bitmapStartY,  bounds,  bounds);
            canvas.drawTexture(mStringTexture, stringStartX, stringStartY, mStringTexture.getWidth(), mStringTexture.getHeight());
            canvas.restore();
        }
    };
    
    public void showEmptyPage(boolean isShow){
        if (isShow) {
            if (!isAttached) {
                isAttached = true;
                addComponent(emptyPage);
                invalidate();
            }
        }else{
            if (isAttached) {
                isAttached = false;
                removeComponent(emptyPage);
                invalidate();
            }
        }
    }
    // TCL BaiYuan End on 2016.11.03
}
