package com.android.camera.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.camera.app.ModuleManager;
import com.android.camera.app.ModuleManagerImpl;
import com.android.camera.debug.Log;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.LockUtils;
import com.tct.camera.R;

public class StereoModeStripView extends FrameLayout implements ModeStrip{

    private static final Log.Tag TAG =new Log.Tag("StereoModeStripView");
    private static final float SELECTED_TRANSPARENT = 1;
    private static final float UNSELECTED_TRANSPARENT = 0.38f;

    private static int SCROLL_DELTA_X_THRESHOLD=20;
    private static final float SCROLL_IMPACT_RATIO=1.1f;

    private static final int MODE_CHANGE_ENSURE_DELAY=100;
    private static final int SCROLL_DURATION_UPPER =300;
    private static final int SCROLL_DURATION_LOWER =150;
    private static final int DIRECTION_NONE=-1;
    private static final int DIRECTION_LEFT=1;
    private static final int DIRECTION_RIGHT=2;
    private static final float SCROLL_FACTOR=2.5f;
    private final int mColorNormal;
    private final int mColorSelected;
    private final int mScreenWidth;
    private final int mModeStartPostion;
    private RelativeLayout mStereoGroup;
    private int mInitialTransX;//The origin coordinate (middle of the screen)
    private List<Integer> mChildrenWidths=new ArrayList<Integer>();
    private ModeStripViewAdapter mAdapter;
    private LockUtils.Lock mMultiLock;
    private OnModeIdListener mListener;
    private int mScrolllingDirection=DIRECTION_NONE;
    private ValueAnimator mScrollAnimator;

    private boolean mPaused=false;
    private int mScrollThreshold=SCROLL_DELTA_X_THRESHOLD;

    /*
    If we try to freeze scroll bar , mode index would get higher if we swap left , thus , reverse to the normal scroll action
     */
    private final boolean mNeedFreezeModeScroll;
    private static final int FIRST_MODE = 0;
    private int SINGLE_MODE = 1;// MODIFIED by nie.lei, 2016-03-24,BUG-1761286

    private final boolean ALL_CAPS;

    public StereoModeStripView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mNeedFreezeModeScroll = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_FIX_MODE_SWITCHING,false);
        ALL_CAPS = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_MODE_NAME_ALL_UPPER_CASE, true);
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mStereoGroup=(RelativeLayout)inflater.inflate(R.layout.stereo_group_layout, null);
        mMultiLock= LockUtils.getInstance().generateMultiLock(LockUtils.LockType.MULTILOCK);
        SCROLL_DELTA_X_THRESHOLD=context.getResources().getDimensionPixelSize(R.dimen.modestrip_touch_delta_threshold);
        Log.w(TAG, "threshold is " + SCROLL_DELTA_X_THRESHOLD);
        this.addView(mStereoGroup);
        this.setBackgroundColor(Color.TRANSPARENT);

        mColorNormal = context.getResources().getColor(R.color.mode_name_normal);
        mColorSelected = context.getResources().getColor(R.color.mode_name_selected);
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        mModeStartPostion = (int)getResources().getDimension(R.dimen.mode_start_postion);
    }


    private int mWidth;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mScrollAnimator!=null&&mScrollAnimator.isRunning()){
            mScrollAnimator.end();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(mAdapter==null){
            return;
        }
        mWidth=this.getMeasuredWidth();
        this.measureChildren(widthMeasureSpec, heightMeasureSpec);
        mChildrenWidths.clear();
        for(int i=0;i<mStereoGroup.getChildCount();i++){
            mChildrenWidths.add(mStereoGroup.getChildAt(i).getMeasuredWidth());
        }
        Log.w(TAG, "stereoGroup size is "+mStereoGroup.getChildCount());
        mInitialTransX=mWidth/2;
        if(mNeedFreezeModeScroll){
            mTransX = mModeStartPostion;// MODIFIED by nie.lei, 2016-03-24,BUG-1761286
            mIndicatorTransX=getPositionX(mTransX,mCurrentModeIndex)-mWidth/2;
        }else{
            mTransX=getTargetTranslationX(mCurrentModeIndex);
        }
        Log.w(TAG,"mTransX is "+mTransX);
        if(mScrollIndicator!=null){
            attachScrollIndicator(mScrollIndicator);//Just to initialize indicator width
            if(mNeedFreezeModeScroll){
                View currentChild=mStereoGroup.getChildAt(mCurrentModeIndex);
                int width=currentChild.getMeasuredWidth();
                mScrollIndicator.animateTrans(0,width,0,mIndicatorTransX,-1);
            }
        }
        invalidate();
    }


    /**
     * Calculate the exact X-axis-coordinate of the center of a StereoRotateTextView
     * Pi=offset+sum{Wn;(n range from 0 to i-1)}+(Wi)/2;
     * @param offset , the offset of the stereo-group
     * @param index the arbitrary target TextView
     * @return
     */
    private int getPositionX(int offset,int index){
        if(mChildrenWidths.size()==0){
            return 0;
        }
        int sumPrevWidth=0;
        for(int i=0;i<index;i++){
            sumPrevWidth+=mChildrenWidths.get(i);
        }
        int position=offset+sumPrevWidth+mChildrenWidths.get(index)/2;
        return position;
    }

    /**
     * Calculate the corresponding rotate-degree of the arbitrary StereoRotateTextView at position
     * @param position
     * @param origin
     * @param threshold
     * @return
     */
    private static final int FINAL_ANGLE =55;
    private int mapPostionDegree(int position, int origin,int border){
        if(position<origin){
            int mappedArea=origin;
            int degree= FINAL_ANGLE *(origin-position)/mappedArea;
            return degree;
        }else{
            int mappedArea=border-origin;
            int degree=-1* FINAL_ANGLE *(position-origin)/mappedArea;
            return degree;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mWidth==0||mInitialTransX==0){//Not measured yet
            super.onDraw(canvas);
            return;
        }
        float minAbsFromCenter=-1;
        int indexOfChildClosestToCenter=0;
        int indicatorTransX=0;
        if(mScrollIndicator!=null){
            indicatorTransX=((StereoScrollIndicatorView)mScrollIndicator).getIndicatorTransX();
        }
        for(int i=0;i<mStereoGroup.getChildCount();i++){
            View child = (View)mStereoGroup.getChildAt(i);
            if(child == null){
                continue;
            }
            int childPosition=getPositionX(mTransX, i);
            if(!mNeedFreezeModeScroll){
                int mappedDegree=mapPostionDegree(childPosition, mInitialTransX,mWidth);
                boolean pivotAlignLeft=((childPosition-mInitialTransX)<0);
                ((StereoRotateTextView)child).rotateY(mappedDegree, pivotAlignLeft);
            }
            float childTransX=childPosition - child.getMeasuredWidth() / 2;
            child.setTranslationX(childTransX);
            float childFromCenter=Math.abs(mWidth/2-childPosition);
            if(!mNeedFreezeModeScroll) {
                if (minAbsFromCenter < 0) {
                    minAbsFromCenter = childFromCenter;
                } else if (childFromCenter < minAbsFromCenter) {
                    minAbsFromCenter = childFromCenter;
                    indexOfChildClosestToCenter = i;
                }
            }else{
                int transXofViewFromCenter=childPosition-mWidth/2;

                int gapFromIndicator=Math.abs(indicatorTransX-transXofViewFromCenter);
                if(minAbsFromCenter<0){
                    minAbsFromCenter=gapFromIndicator;
                }else if(gapFromIndicator<minAbsFromCenter){
                    minAbsFromCenter=gapFromIndicator;
                    indexOfChildClosestToCenter=i;
                }
            }

//            if (i == mCurrentModeIndex) {
//                child.setTextColor(getResources().getColor(R.color.mode_name_text_color_selected));
//            } else {
            if(!mNeedFreezeModeScroll){
                ((StereoRotateTextView)child).setTextColor(getResources().getColor(R.color.mode_name_text_color_unselected));
            }
//            }
        }
        if(!mNeedFreezeModeScroll){
            ((StereoRotateTextView)mStereoGroup.getChildAt(indexOfChildClosestToCenter)).setTextColor(getResources().getColor(R.color.mode_name_text_color_selected));
        }else {
            if(mAdapter !=null){
                mAdapter.setIndex(mCurrentModeIndex);
            }
        }

        //invalidate all children to display them in expected degree
//        for(int i=0;i<mStereoGroup.getChildCount();i++){
//            mStereoGroup.getChildAt(i).invalidate();
//        }
        super.onDraw(canvas);
    }

    private interface ConditionalRunnable extends Runnable{

        public void run(double deltaX,int duration);

        public void run(double deltaX,int duration ,int indexChangeset);
    }

    private final ConditionalRunnable mSwitchingRunnable=new ConditionalRunnable() {
        @Override
        public void run() {
//            run(SCROLL_DURATION_UPPER);
        }

        @Override
        public void run(double deltaX,int duration) {
            if(Math.abs(deltaX)<mScrollThreshold){
                return;
            }
            Log.w(TAG,"start scroll , scrollThreshold is "+mScrollThreshold+" duration is "+duration+" deltaX is "+deltaX+" isLocked ?"+isLocked());
            mScrollThreshold=0;
            final int currentIndex=mCurrentModeIndex;
            Log.w(TAG,"currentIndex is "+currentIndex);
            if(mScrolllingDirection==DIRECTION_LEFT){
                if(mCurrentModeIndex>0){
                    mCurrentModeIndex--;
                }
            }
            if(mScrolllingDirection==DIRECTION_RIGHT){
                if(mCurrentModeIndex<mAdapter.getCount()-1){
                    mCurrentModeIndex++;
                }
            }
            final int targetIndex=mCurrentModeIndex;
            Log.w(TAG, "targetIndex is " + targetIndex);
            if(currentIndex!=targetIndex&&mAdapter!=null&&(targetIndex>=0&&targetIndex<mAdapter.getCount())){
                mListener.onModeIdChanging();
                switchToMode(currentIndex, targetIndex,duration);
            }
            mScrolllingDirection=DIRECTION_NONE;
        }

        @Override
        public void run(double deltaX,int duration, int indexChangeset) {
            if(Math.abs(deltaX)<mScrollThreshold){
                return;
            }
            final int currentIndex=mCurrentModeIndex;
            Log.w(TAG,"currentIndex is "+currentIndex);
            mCurrentModeIndex+=indexChangeset;
            if(mCurrentModeIndex<0){
                mCurrentModeIndex=0;
            }
            if(mCurrentModeIndex>mAdapter.getCount()-1){
                mCurrentModeIndex=mAdapter.getCount()-1;
            }
            final int targetIndex=mCurrentModeIndex;
            Log.w(TAG, "targetIndex is " + targetIndex);
            if(currentIndex!=targetIndex&&mAdapter!=null&&(targetIndex>=0&&targetIndex<mAdapter.getCount())){
                mListener.onModeIdChanging();
                switchToMode(currentIndex, targetIndex,duration);
            }
            mScrolllingDirection=DIRECTION_NONE;
        }
    };

    private boolean mIsWaitingForScroll=true;
    private double mDeltaXInSingleTouch=0;
    private VelocityObserver mVelocityObserver=new VelocityObserver();
    private List<PointF> mOperationHistory=new ArrayList<>();
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mPaused) {
            return false;
        }
        if(ev.getAction()==MotionEvent.ACTION_DOWN){
            mOperationHistory.clear();
            mOperationHistory.add(new PointF(ev.getX(),ev.getY()));
        }else if(ev.getAction()==MotionEvent.ACTION_MOVE){
            mOperationHistory.add(new PointF(ev.getX(),ev.getY()));
            if(mOperationHistory.size()<2){
                mScrolllingDirection=DIRECTION_NONE;
                return true;
            }
            PointF lastCoord=mOperationHistory.get(mOperationHistory.size()-2);
            double dx=ev.getX()-lastCoord.x;
            double dy=ev.getY()-lastCoord.y;
            Log.v(TAG,"scroll orientation ratio is "+Math.abs(dx/dy));
            boolean isHorizontalMove=(Math.abs(dx/dy)>=SCROLL_IMPACT_RATIO);
            if(isHorizontalMove){
                if(!checkMovementDuringTouchEvent(dx,false)){
                    return false;
                }
            }else{
                mScrolllingDirection=DIRECTION_NONE;
            }

            Log.w(TAG, "ScrollingDirection is " + mScrolllingDirection);
        }else if(ev.getAction()==MotionEvent.ACTION_UP){
            Log.v(TAG,"on action up");
            profileHistoryOperation(mOperationHistory);
            if(mOperationHistory.size()==1){
                PointF lastCoord=mOperationHistory.get(0);
                double dx=ev.getX()-lastCoord.x;
                double dy=ev.getY()-lastCoord.y;
                Log.v(TAG,"scroll orientation ratio is "+Math.abs(dx/dy));
                boolean isHorizontalMove=(Math.abs(dx/dy)>=SCROLL_IMPACT_RATIO);
                if(isHorizontalMove){
                    checkMovementDuringTouchEvent(dx,true);
                }
            }
            mOperationHistory.clear();
            mScrolllingDirection=DIRECTION_NONE;
            mIsWaitingForScroll=true;
            mDeltaXInSingleTouch=0;
        }
        return true;//Whenever touch on the stereoModeStripView , the touch event should be blocked
    }

    /**
     *
     * @param dx
     * @param injectVelocity true means the scrolling history would add an fake velocity which is 0 to make the scroll even smoother
     * @return
     */
    private boolean checkMovementDuringTouchEvent(double dx,boolean injectVelocity){
        ConditionalRunnable switchRunnable=null;
        if(mDeltaXInSingleTouch==0){
            mVelocityObserver.start();
        }
        mDeltaXInSingleTouch+=dx;
        mVelocityObserver.record();

        if(mNeedFreezeModeScroll){
            mScrolllingDirection = dx > 0 ? DIRECTION_RIGHT : DIRECTION_LEFT;
        }else {
            mScrolllingDirection = dx > 0 ? DIRECTION_LEFT : DIRECTION_RIGHT;
        }

        if(mIsWaitingForScroll){
            Log.v(TAG,"start one single scroll");
            Log.w(TAG,"deltaX is "+mDeltaXInSingleTouch);
            float velocity=mVelocityObserver.getVelocityX((float) Math.abs(mDeltaXInSingleTouch));
            postScrolling(mScrolllingDirection, injectVelocity?0:velocity,mDeltaXInSingleTouch);//Once the we receive the touch event up ,
            // the action of scrolling should be recorded, record once for one single touch down-up
        }
        if(mListener!=null){
            switchRunnable=mSwitchingRunnable;
        };

        if(isLocked()){
            switchRunnable=null;
            return false;
        }

        if(switchRunnable!=null){
            Log.w(TAG,"clear directions");
            mPendingIndexUpdateHistory.clear();//We clear pending directions here
            // because here is the user's very first intent to swipe mode , any pending changesets should be reset
            int duration= SCROLL_DURATION_UPPER;
            float velocityX=mVelocityObserver.getVelocityX((float)Math.abs(mDeltaXInSingleTouch));
            if(velocityX!=0){
                /* MODIFIED-BEGIN by peixin, 2016-04-26,BUG-1995405*/
                try {
                    if (-1 != mCurrentModeIndex) {
                        int childWidth = mChildrenWidths.get(mCurrentModeIndex);
                        duration = trimDuration((int) ((childWidth / velocityX) * SCROLL_FACTOR));
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                /* MODIFIED-END by peixin,BUG-1995405*/
            }
            switchRunnable.run(mDeltaXInSingleTouch,duration);
        }
        return true;
    }

    private void profileHistoryOperation(List<PointF> operationHistory){
        StringBuilder stringBuilder=new StringBuilder("operation history : ");
        for(PointF p:operationHistory){
            stringBuilder.append(p.x).append(",").append(p.y).append("; ");
        }
        Log.v(TAG,stringBuilder.toString());
    }

    private AnimatorUpdateListener mAnimatorUpdateListener=new AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if(!mNeedFreezeModeScroll) {
                mTransX = (int) animation.getAnimatedValue();
            }
            invalidate();
        }
    };

    private int mInternalLock=-1;
    private final AnimatorListener mAnimatorLifecycleListener=new AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) {
            Log.v(TAG,"Start scrolling");
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            Log.w(TAG, "scroll animation end");
            if (mInternalLock != -1) {
                StereoModeStripView.this.unLockView(mInternalLock);
                mInternalLock=-1;
            }
            mScrollAnimator.removeListener(mAnimatorLifecycleListener);
            mScrollAnimator.removeAllUpdateListeners();


            if(!updateModesInHistory()){
                mScrollThreshold=SCROLL_DELTA_X_THRESHOLD;
                if (mListener != null) {
                    Log.w(TAG, "posting scroll not effective, change mode");
//                    StereoModeStripView.this.removeCallbacks(mModeIdChangedRunnable);
                    mModeIdChangedRunnable.setTargetModeId(mCurrentModeIndex);
//                    StereoModeStripView.this.postDelayed(mModeIdChangedRunnable, MODE_CHANGE_ENSURE_DELAY);
                    mModeIdChangedRunnable.run();
                }
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            if(mInternalLock!=-1){
                StereoModeStripView.this.unLockView(mInternalLock);
                mInternalLock=-1;
            }
            if (mListener != null) {
                Log.w(TAG, "posting scroll not effective, change mode");
//                StereoModeStripView.this.removeCallbacks(mModeIdChangedRunnable);
                mModeIdChangedRunnable.setTargetModeId(mCurrentModeIndex);
//                StereoModeStripView.this.postDelayed(mModeIdChangedRunnable, MODE_CHANGE_ENSURE_DELAY);
                mModeIdChangedRunnable.run();
            }
            mScrollAnimator.removeListener(this);
            mScrollAnimator.removeAllUpdateListeners();
        }
    };

    /**
     * calculate the targeting index-update
     * @return the changeset which would be applied to the currentIndex , or 0 not to use continous-mode-changing
     */
    private int calculateUpdateIndex(){
        if(mPendingIndexUpdateHistory.size()<=1){
            return 0;
        }
        int index=0;
        for(ScrollHistory history:mPendingIndexUpdateHistory){
            if(history.direction==DIRECTION_LEFT){
                index--;
            }else if(history.direction==DIRECTION_RIGHT){
                index++;
            }
        }
        if(index!=0){
            Log.w(TAG,"clear directions to boost swiping");
            mPendingIndexUpdateHistory.clear();
        }
        return index;
    }


    private ScrollIndicator mScrollIndicator;
    @Override
    public void attachScrollIndicator(ScrollIndicator scrollIndicator){
        if(scrollIndicator == null){
            Log.w(TAG,"attachScrollIndicator = null");
            return;
        }
        mScrollIndicator=scrollIndicator;
        Log.w(TAG,"currentModeIndex is "+mCurrentModeIndex);
        if(mWidth==0){//Not measured yet
            return;
        }
        if(mStereoGroup.getChildCount()!=0){
            int index=mCurrentModeIndex;
            if(mCurrentModeIndex<0||mCurrentModeIndex>=mStereoGroup.getChildCount()){
                index=0;
            }
            View currentChild=mStereoGroup.getChildAt(index);
            int width=currentChild.getMeasuredWidth();
            mScrollIndicator.initializeWidth(width);
        }
    }

    private int mTransX;
    private int mIndicatorTransX;
    private int mCurrentModeIndex=0;
    public void setCurrentModeWithModeIndex(int modeId){
        Log.w(TAG,"set "+modeId+" to be current mode");
        if(mAdapter==null){
            return;
        }
        int targetIndex=-1;
        for(int i=0;i<mAdapter.getCount();i++){
            if(mAdapter.getItemId(i)==modeId){
                targetIndex=i;
                break;
            }
        }
        if(targetIndex!=-1){
            switchToMode(mCurrentModeIndex,targetIndex, SCROLL_DURATION_LOWER);
        }
        mCurrentModeIndex=targetIndex;
    }

    public void initDefaultMode(int modeId){
        Log.w(TAG,"set "+modeId+" to be current mode");
        if(mAdapter==null){
            return;
        }
        int targetIndex=-1;
        for(int i=0;i<mAdapter.getCount();i++){
            if(mAdapter.getItemId(i)==modeId){
                targetIndex=i;
                break;
            }
        }
        if(targetIndex!=-1){
            switchToMode(mCurrentModeIndex, targetIndex, 0);
        }
        mCurrentModeIndex=targetIndex;
    }


    private class ScrollHistory{
        int direction;
        float velocityX;
        double deltaX;
    }
    private List<ScrollHistory> mPendingIndexUpdateHistory=new LinkedList<>();
    public void postScrolling(int direction,float velocityX,double deltaX){
        if(direction<DIRECTION_NONE||direction>DIRECTION_RIGHT){
            return;
        }

        if(direction!=DIRECTION_NONE) {
            mIsWaitingForScroll=false;
            Log.v(TAG,"cancel wait for scroll");
            Log.w(TAG, "add direction " + direction);
            ScrollHistory history=new ScrollHistory();
            history.direction=direction;
            history.velocityX=velocityX;
            history.deltaX=deltaX;
            Log.w(TAG, "velocity is " + velocityX * 1000);
            mPendingIndexUpdateHistory.add(history);
        }
    }

    private void switchToMode(int fromIndex,int toIndex,int duration){
        Log.w(TAG, String.format("from %d to %d", fromIndex, toIndex));
        if(fromIndex==toIndex||mAdapter==null){
            return;
        }
        if(toIndex>=0&&toIndex<mAdapter.getCount()){
            if(mWidth==0){//Not measured yet, skip the animation
                mCurrentModeIndex=toIndex;
                Log.w(TAG, "width ==0 , start modeChanged");
                if(mScrollIndicator!=null){
                    attachScrollIndicator(mScrollIndicator);//Just to initialize indicator width
                }
                if (mListener != null) {
                    Log.w(TAG, "posting scroll not effective, change mode");
//                    this.removeCallbacks(mModeIdChangedRunnable);
                    mModeIdChangedRunnable.setTargetModeId(mCurrentModeIndex);
//                    this.postDelayed(mModeIdChangedRunnable, MODE_CHANGE_ENSURE_DELAY);
                    mModeIdChangedRunnable.run();
                }
                return;
            }
            int currentTransX=getTargetTranslationX(fromIndex);
            Log.w(TAG, " start scroll animation ,currentTransX is "+currentTransX);
            int targetTransX=getTargetTranslationX(toIndex);

            if(mScrollIndicator!=null){
                View from=mStereoGroup.getChildAt(fromIndex);
                View to=mStereoGroup.getChildAt(toIndex);
                int fromWidth=from.getMeasuredWidth();
                int toWidth=to.getMeasuredWidth();

                if(mNeedFreezeModeScroll) {
                    int fromOffset=getPositionX(mTransX,fromIndex)-mWidth/2;
                    int toOffSet=getPositionX(mTransX,toIndex)-mWidth/2;
                    mScrollIndicator.animateTrans(fromWidth,toWidth,fromOffset, toOffSet, duration);
                }else{
                    mScrollIndicator.animateWidth(fromWidth,toWidth,duration);
                }
            }

            mScrollAnimator=ValueAnimator.ofInt(currentTransX,targetTransX);
            mScrollAnimator.setDuration(duration);
            Log.w(TAG,"duration is "+duration);
            mScrollAnimator.addUpdateListener(mAnimatorUpdateListener);
            mScrollAnimator.addListener(mAnimatorLifecycleListener);
            if(mInternalLock==-1) {
                mInternalLock = StereoModeStripView.this.lockView();
            }
            mScrollAnimator.start();
        }
    }

    private void switchToMode(int fromIndex,int toIndex){
        switchToMode(fromIndex, toIndex, SCROLL_DURATION_UPPER);
    }

    private int getTargetTranslationX(int currentModeIndex){
        if(currentModeIndex>=mChildrenWidths.size() || currentModeIndex < 0){
            return 0;
        }
        //For arbitrary i , TransX = w/2 - w0-w1-....-w(i-1)-wi/2
        //if i==0; then TransX=w/2-w0/2;
        int transX=this.getMeasuredWidth()/2-mChildrenWidths.get(currentModeIndex)/2;
        for(int i=0;i<currentModeIndex;i++){
            transX-=mChildrenWidths.get(i);
        }
        return transX;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mAdapter=(ModeStripViewAdapter)adapter;
        notifyDatasetChanged();
    }

    @Override
    public void notifyDatasetChanged() {
        if(mAdapter==null){
            return;
        }
        if (mNeedFreezeModeScroll){
            /* MODIFIED-BEGIN by nie.lei, 2016-03-24,BUG-1761286 */
            int modeSize = mAdapter.getCount();
            for(int i=0;i< modeSize;i++){
            /* MODIFIED-END by nie.lei,BUG-1761286 */
                ModeStripViewAdapter.VdfModuleHolder holder = (ModeStripViewAdapter.VdfModuleHolder)mAdapter.getItem(i);
                if(holder != null && holder instanceof ModeStripViewAdapter.VdfModuleHolder){
                    holder.mItem.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isLocked() || mPaused) {
                                return;
                            }
                            if (mListener != null) {
                                int currentMode = (int) mAdapter.getItemId(mCurrentModeIndex);
                                int selectingMode = (int) view.getTag();
                                if (currentMode == selectingMode) {
                                    return;
                                }
                                mListener.onModeIdChanging();
                            }
                            setCurrentModeWithModeIndex((int) view.getTag());
                        }
                    });

                    /* MODIFIED-BEGIN by nie.lei, 2016-03-24,BUG-1761286 */
                    if(modeSize > SINGLE_MODE){
                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(holder.width,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
/* MODIFIED-BEGIN by wenhua.tu, 2016-04-25,BUG-1992169*/
//                        if(i == FIRST_MODE ) {
//                            ImageView itemImg = (ImageView) holder.mItemImg;
//                            itemImg.setVisibility(View.GONE);
//                        }
                        TextView tx = (TextView)holder.mItemName;
                        LinearLayout.LayoutParams txlp = (LinearLayout.LayoutParams) tx.getLayoutParams();
                        int gap = (int)StereoModeStripView.this.getContext().getResources().getDimension(R.dimen.mode_item_divition);
                        int marginLeft = (int)StereoModeStripView.this.getContext().getResources().getDimension(R.dimen.first_mode_margin_left);
                        txlp.setMargins(marginLeft, txlp.topMargin, 0, txlp.bottomMargin);
                        txlp.width = LinearLayout.LayoutParams.MATCH_PARENT;
                        txlp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        /* MODIFIED-END by wenhua.tu,BUG-1992169*/
                        txlp.gravity = Gravity.CENTER;
                        tx.setLayoutParams(txlp);

                        mStereoGroup.addView(holder.mItem, lp);
                    }else if(modeSize == SINGLE_MODE){
                        RelativeLayout.LayoutParams lpMode = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        TextView tx = (TextView)holder.mItemName;
/* MODIFIED-BEGIN by wenhua.tu, 2016-04-25,BUG-1992169*/
//                        ImageView itemImg = (ImageView)holder.mItemImg;
//                        itemImg.setVisibility(View.GONE);
                        LinearLayout.LayoutParams txlp = (LinearLayout.LayoutParams) tx.getLayoutParams();
                        txlp.width = LinearLayout.LayoutParams.MATCH_PARENT;
                        txlp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        /* MODIFIED-END by wenhua.tu,BUG-1992169*/
                        int singleMarginLeft = (int)StereoModeStripView.this.getContext().getResources().getDimension(R.dimen.single_mode_margin_left);
                        int singleMarginRight = (int)StereoModeStripView.this.getContext().getResources().getDimension(R.dimen.single_mode_margin_right);
                        int singleMarginTop = (int)StereoModeStripView.this.getContext().getResources().getDimension(R.dimen.single_mode_margin_top);
                        int singleMarginBottom = (int)StereoModeStripView.this.getContext().getResources().getDimension(R.dimen.single_mode_margin_bottom);
                        txlp.setMargins(singleMarginLeft,singleMarginTop,singleMarginRight, singleMarginBottom);
                        tx.setLayoutParams(txlp);
                        mStereoGroup.addView(holder.mItem, lpMode);
                    }else {
                        Log.e(TAG,"mode size invalid < 1");
                        return;
                    }
                }
            }
            mAdapter.setIndex(mCurrentModeIndex);
            /* MODIFIED-END by nie.lei,BUG-1761286 */
        }else {
            for(int i=0;i<mAdapter.getCount();i++){
                if(!(mAdapter.getView(i, null, null) instanceof StereoRotateTextView)){
                    continue;
                }
                StereoRotateTextView stereoText=(StereoRotateTextView)mAdapter.getView(i, null, null);
                stereoText.setMaxRotation(FINAL_ANGLE);
                stereoText.setTag((int) mAdapter.getItemId(i));
                stereoText.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isLocked()) {
                            return;
                        }
                        if (mListener != null) {
                            int currentMode = (int) mAdapter.getItemId(mCurrentModeIndex);
                            int selectingMode = (int) view.getTag();
                            if (currentMode == selectingMode) {
                                return;
                            }
                            mListener.onModeIdChanging();
                        }
                        setCurrentModeWithModeIndex((int) view.getTag());
                    }
                });
                mStereoGroup.addView(stereoText);
            }
            invalidate();
        }
    }


    @Override
    public void setModeIndexChangeListener(OnModeIdListener listener) {
        mListener=listener;
    }


    @Override
    public Integer lockView() {
        return mMultiLock.aquireLock();
    }


    @Override
    public boolean unLockView(Integer token) {
        boolean result=mMultiLock.unlockWithToken(token);
        onLockStateUpdate();
        return result;
    }

    @Override
    public void init(ModuleManager moduleManager){
        Adapter adapter=new ModeStripViewAdapter(moduleManager);
        setAdapter(adapter);
    }

    private class ModeStripViewAdapter extends BaseAdapter {
        private class VdfModuleHolder extends ModuleHolder{
            public View mItem;
            public TextView mItemName;
            public ImageView mItemImg;
            public int width;
            private VdfModuleHolder(int index){
                super(index);
            }

            @Override
            public View getModuleView(){
                LayoutInflater inflater=LayoutInflater.from(StereoModeStripView.this.getContext());
                mItem = inflater.inflate(R.layout.mode_item, null);
                mItemImg = (ImageView)mItem.findViewById(R.id.mode_item_img);
                mItemName = (TextView)mItem.findViewById(R.id.mode_item_name);
                mItemName.setText(MODULE_NAME);
                mItem.setTag(MODULE_INDEX);
                return mItem;
            }
        }

        private class ModuleHolder{
            public String MODULE_NAME;
            public int MODULE_INDEX;
            private ModuleHolder(int index){
                MODULE_INDEX=index;
                MODULE_NAME= CameraUtil.getCameraModeText(index, StereoModeStripView.this.getContext());
                getModuleView();
            }

            public View getModuleView(){
                LayoutInflater inflater=LayoutInflater.from(StereoModeStripView.this.getContext());
                StereoRotateTextView view=(StereoRotateTextView)inflater.inflate(R.layout.stereo_text, null);
                view.setAllCaps(ALL_CAPS);
                view.setText(MODULE_NAME);
                return view;
            }
        }

        List<ModuleHolder> mModules=new ArrayList<ModuleHolder>();

        private int mDefaultModeIndex;

        public ModeStripViewAdapter(ModuleManager manager){
            int defaultModeIndex=0;
            List<Integer> supportedModules=manager.getSupportedModeIndexList();
            for(int index : supportedModules){
                ModuleManagerImpl.ModuleAgent agent = manager.getModuleAgent(index);
                if(agent.needAddToStrip()) {
                    if(mNeedFreezeModeScroll){
                        mModules.add(new VdfModuleHolder(index));
                    }else {
                        mModules.add(new ModuleHolder(index));
                    }

                    if(manager.getDefaultModuleIndex()==index){
                        mDefaultModeIndex=mModules.size()-1;//Larger than 0 at least
                        Log.w(TAG,"default index is "+mDefaultModeIndex);
                    }
                }
            }

            if (mNeedFreezeModeScroll){
                int mModeWidth = 0;
                if(mModules.size() > 0){
                    mModeWidth = (mScreenWidth)/mModules.size();
                    for(int i=0; i<mModules.size(); i++){
                        ModuleHolder moduleHolder = mModules.get(i);
                        if(moduleHolder != null && moduleHolder instanceof VdfModuleHolder){
                            if(0 == i){
                                ((VdfModuleHolder)moduleHolder).mItemImg.setVisibility(View.INVISIBLE);
                            }

                            ((VdfModuleHolder)moduleHolder).width = mModeWidth;
                        }
                    }
                } else{
                    mModeWidth = mScreenWidth;
                }
                Log.d(TAG, "mScreenWidth="+mScreenWidth+", mModeWidth="+mModeWidth+", mModules.size()="+mModules.size());
            }
        }

        public void setIndex(int index){
            for(int i=0; i<mModules.size(); i++){
                ModuleHolder moduleHolder = mModules.get(i);
                if(moduleHolder != null && moduleHolder instanceof VdfModuleHolder) {
                    if (index == i) {
                        ((VdfModuleHolder) moduleHolder).mItemName.setTextColor(mColorSelected);
                        ((VdfModuleHolder) moduleHolder).mItemName.setAlpha(SELECTED_TRANSPARENT);
                    } else {
                        ((VdfModuleHolder) moduleHolder).mItemName.setTextColor(mColorNormal);
                        ((VdfModuleHolder) moduleHolder).mItemName.setAlpha(UNSELECTED_TRANSPARENT);
                    }
                }
            }
        }

        public int getDefaultModeIndex(){
            return mDefaultModeIndex;
        }

        @Override
        public int getCount() {
            return mModules.size();
        }

        @Override
        public Object getItem(int position) {
            return mModules.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mModules.get(position).MODULE_INDEX;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mModules.get(position).getModuleView();
        }
    }

    private void onLockStateUpdate(){
        if(!this.isLocked()){
            this.post(new Runnable() {
                @Override
                public void run() {
                    if(!StereoModeStripView.this.isLocked()) {
                        if (mPendingIndexUpdateHistory.size() > 0 && !mPaused) {//Would do mode swipe according to history , close mode first by calling onModeIdChanging
                            mListener.onModeIdChanging();
                            updateModesInHistory();
                        }
                    }
                }
            });
        }
    }

    private boolean updateModesInHistory(){
        int direction=DIRECTION_NONE;
        int animatorDuration= SCROLL_DURATION_UPPER;
        int indexChangeset=0;
        double deltaX=0;
        if(mPendingIndexUpdateHistory.size()>0&&!mPaused){
            //Here we check the pending-swipe record , if the record size is bigger than 1 ,
            //it's supposed to navigate to the final target mode instead of navigating to it step by step, that could boost the user's impact on the swiping responding
            indexChangeset=calculateUpdateIndex();
            if(indexChangeset==0) {
                ScrollHistory scrollHistory=mPendingIndexUpdateHistory.remove(0);
                direction = scrollHistory.direction;
                int width=mChildrenWidths.get(mCurrentModeIndex);
                if(scrollHistory.velocityX==0){
                    animatorDuration= SCROLL_DURATION_LOWER;
                }else {
                    int calculatedDuration = (int) (width *SCROLL_FACTOR/ scrollHistory.velocityX);
                    animatorDuration = trimDuration(calculatedDuration);
                }
                Log.w(TAG,"animatorDuration is "+animatorDuration);
                deltaX=scrollHistory.deltaX;
            }else{
                if(mNeedFreezeModeScroll){
                    direction = indexChangeset > 0 ? DIRECTION_LEFT : DIRECTION_RIGHT;
                }else {
                    direction = indexChangeset > 0 ? DIRECTION_RIGHT : DIRECTION_LEFT;
                }
                animatorDuration= SCROLL_DURATION_UPPER;
                deltaX=SCROLL_DELTA_X_THRESHOLD;
            }
            Log.w(TAG,"pending direction is "+direction);
        }
        if (direction != DIRECTION_NONE) {
            mScrolllingDirection = direction;
            if (mScrolllingDirection == DIRECTION_LEFT) {
                if (mCurrentModeIndex <= 0) {
                    mScrolllingDirection = DIRECTION_NONE;
                    if (mListener != null) {
                        Log.w(TAG, "posting scroll not effective, change mode");
//                        this.removeCallbacks(mModeIdChangedRunnable);
                        mModeIdChangedRunnable.setTargetModeId(mCurrentModeIndex);
//                        this.postDelayed(mModeIdChangedRunnable, MODE_CHANGE_ENSURE_DELAY);
                        mModeIdChangedRunnable.run();
                    }
                    return true;
                }
            }
            if (mScrolllingDirection == DIRECTION_RIGHT) {
                if (mCurrentModeIndex >= mAdapter.getCount() - 1) {
                    mScrolllingDirection = DIRECTION_NONE;
                    if (mListener != null) {
                        Log.w(TAG, "posting scroll not effective, change mode");
//                        this.removeCallbacks(mModeIdChangedRunnable);
                        mModeIdChangedRunnable.setTargetModeId(mCurrentModeIndex);
//                        this.postDelayed(mModeIdChangedRunnable,MODE_CHANGE_ENSURE_DELAY);
                        mModeIdChangedRunnable.run();
                    }
                    return true;
                }
            }
            Log.w(TAG, "continue to scroll , direction is " + mScrolllingDirection + " currentIndex is " + mCurrentModeIndex);
//                mDeltaXInSingleTouch = SCROLL_DELTA_X_THRESHOLD + 1;
            mScrollThreshold=0;
            if(indexChangeset!=0){
                mSwitchingRunnable.run(deltaX,animatorDuration,indexChangeset);
            }else {
                mSwitchingRunnable.run(deltaX,animatorDuration);
            }
            mScrollThreshold=SCROLL_DELTA_X_THRESHOLD;
//                mDeltaXInSingleTouch = 0;
            return true;
        }else{
            return false;
        }
    }

    private abstract class ModeIdChangeRunnable implements Runnable{
        protected int mTargetModeId;
        public void setTargetModeId(int targetId){
            mTargetModeId=targetId;
        }
    }

    private final ModeIdChangeRunnable mModeIdChangedRunnable=new ModeIdChangeRunnable() {
        @Override
        public void run() {
            if(mListener!=null) {
                mListener.onModeIdChanged((int) mAdapter.getItemId(mTargetModeId));
            }
        }
    };

    @Override
    public boolean isLocked(){
        return mMultiLock.isLocked();
    }

    @Override
    public void lockSelf() {
        mMultiLock.aquireLock(this.hashCode());
    }

    @Override
    public void unLockSelf() {
        mMultiLock.unlockWithToken(this.hashCode());
        onLockStateUpdate();
    }

    @Override
    public int lock() {
        return mMultiLock.aquireLock();
    }

    @Override
    public boolean unlockWithToken(int token) {
        return mMultiLock.unlockWithToken(token);
    }

    @Override
    public void pause() {
        Log.w(TAG,"mode strip pause");
        mPaused=true;
        mScrollThreshold=SCROLL_DELTA_X_THRESHOLD;
        mPendingIndexUpdateHistory.clear();
    }

    @Override
    public void resume() {
        Log.w(TAG,"mode strip resume");
        mPaused=false;
        mScrollThreshold=SCROLL_DELTA_X_THRESHOLD;
    }

    private int trimDuration(int duration){
        return duration>SCROLL_DURATION_UPPER?SCROLL_DURATION_UPPER:(duration< SCROLL_DURATION_LOWER?SCROLL_DURATION_LOWER:duration);
    }

}


class VelocityObserver {
    private long mStartTime =0;
    private long mEndTime =0;

    private void resetTimer(){
        mStartTime =System.currentTimeMillis();
        mEndTime =mStartTime;
    }

    public float getVelocityX(float deltaX){
        long gap= mEndTime - mStartTime;
        if(gap==0){
            return 0;
        }
        float velocityX=Math.abs(deltaX/(float)gap);
        return velocityX>0?velocityX:0;
    }

    public void start(){
        resetTimer();
    }

    public void record(){
        mEndTime = System.currentTimeMillis();
    }
}
