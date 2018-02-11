package com.android.camera.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.camera.app.AppController;
import com.android.camera.app.ModuleManager;
import com.android.camera.app.ModuleManagerImpl;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.LockUtils;
import com.tct.camera.R;
import com.android.camera.debug.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * No more used , Use StereoModeStripView instead
 */
@Deprecated
public class ModeStripView extends HorizontalScrollView implements  ModeStrip{
    private final static Log.Tag TAG = new Log.Tag("ModeStripView");




    private LockUtils.Lock mMultiLock;
    private ViewGroup mChildWrapper;//The scroller could only have on child , add ViewGroup to hold children views
    private int mVisualWidth=0;//The width of the visible area on the screen
    private View[] mGapHolder=new View[2];// Add view holder to enable the scroller start and end at the half of the screen
    private static final int GAP_HEIGHT=10;// The height for the gap_holder,could be any value
    private List<Integer> mCoordsX=new ArrayList<Integer>();

    private ModeScrollBar mModeScrollBar;
    List<Integer> mItemWidthList = new ArrayList<>();
    private Integer mModeSelectionLockToken;
    private Path mPath = new Path();
    private Paint mPaint;
    private RadialGradient mRadialGradient;
    private ObjectAnimator mRadiusAnimator;

    private final Integer mRadiusIncrement;
    private final Integer mPaintAlpha;
    private final int mRippleColor;
    private final float mAlphaFactor;
    private final int ENTER_MODE_ANIM_DURATION = 350;
    private final int LEAVE_MODE_ANIM_DURATION = 250;
    private final int ANIM_RESET_RADIUS = 0;
    private final int ANIM_RESET_ALPHA = 1;
    private final float RIPPLE_COLOR_ALPHA = 0.2f;

    private float mDownX;//animation position x
    private float mDownY;//animation position y
    private float mMaxRadius;//Max radius
    private float mRadius;//ripple radius
    private boolean mIsLeaveModeAnimating = false;
    private boolean mIsEnterModeAnimating = false;
    private int initModeIndex = -1;
    private int mClickIndex = -1;
    private int mSlideToModeIndex = -1;

    public ModeStripView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mChildWrapper=(ViewGroup)inflater.inflate(R.layout.strip_wrapper,null);
        this.addView(mChildWrapper);
        mMultiLock=LockUtils.getInstance().generateMultiLock(LockUtils.LockType.MULTILOCK);

        mPaintAlpha = getResources().getInteger(R.integer.mode_paint_alpha);
        mRadiusIncrement = getResources().getInteger(R.integer.mode_ripple_radius_increment);

        //init ripple paint and color
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAlpha(mPaintAlpha);
        mRippleColor = Color.WHITE;
        mAlphaFactor = RIPPLE_COLOR_ALPHA;
    }


    /**
     * Lock the view by an instance of {@link com.android.camera.util.LockUtils.Lock} within this
     * @return the token to unlock the view , null if lock not available
     */
    public Integer lockView(){
        return mMultiLock.aquireLock();
    }

    /**
     * Unlock this view by a token returned by {@link #lockView()}
     * @param token
     * @return see unLockByToken method in {@link com.android.camera.util.LockUtils.MultiLock}
     */
    public boolean unLockView(Integer token){
        return mMultiLock.unlockWithToken(token);
    }

    private OnModeIdListener mListener;
    public void setModeIndexChangeListener(OnModeIdListener listener){
        mListener=listener;
    }

    private Adapter mAdapter;
    public void setAdapter(Adapter adapter){
        mAdapter=adapter;
        notifyDatasetChanged();
    }

    public synchronized void notifyDatasetChanged(){
        int count=mAdapter.getCount();
        mChildWrapper.removeAllViews();
        if(mGapHolder[0]!=null){
            mChildWrapper.addView(mGapHolder[0]);
        }
        for(int i=0;i<count;i++){
            View view=mAdapter.getView(i, null, null);
            view.setTag(i);
            view.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View v) {
                    if (mMultiLock.isLocked()) {
                        Log.e(TAG, "Mode can't to switch. mode locked");
                        return;
                    }

                    if (mIsEnterModeAnimating || mIsLeaveModeAnimating) {
                        Log.e(TAG, "Mode can't to switch.Leaving or entering animation is running");
                        return;
                    }

                    mClickIndex = (Integer) v.getTag();
                    if(mCurrentIndex == mClickIndex ){
                        Log.e(TAG, "No need to change. mCurrentIndex equals to clickindex : " + mCurrentIndex);
                        return;
                    }

                    //lock mode selection
                    requestLockModeStripView(true);

                    if (mModeScrollBar != null) {
                        final int target = (Integer) v.getTag();

                        int[] pos = new int[2];
                        v.getLocationInWindow(pos);
                        int posCenter = pos[0] + mItemWidthList.get(target) / 2;
                        //scrollbar to right item with playing animation
                        mModeScrollBar.scrollToItem(mCurrentIndex, target, posCenter,
                                new ModeScrollBar.onBarStatueChangedListener() {
                                    @Override
                                    public void onScrollStarted() {
                                        getHandler().post(new Runnable() {
                                            @Override
                                            public void run() {
//                                                if(!AppController.ENABLE_BLUR_TRANS) {
//                                                    leaveModeAnimation(mCurrentIndex);
//                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onItemReached() {
                                        getHandler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateTextColor(target);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onEndArrived() {
                                        getHandler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                setCurrentItem(target, true);
//                                                if(!AppController.ENABLE_BLUR_TRANS) {
//                                                    enterModeAnimation(mClickIndex);
//                                                }
                                            }
                                        });

                                    }

                                    @Override
                                    public void onScrollFinished() {
                                        getHandler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                //unlock mode selection
                                                requestLockModeStripView(false);
                                            }
                                        });
                                    }
                                });
                    }
                }

            });
            mChildWrapper.addView(view);

        }
        if(mGapHolder[1]!=null){
            mChildWrapper.addView(mGapHolder[1]);
        }

        if(count==1){
            this.setVisibility(View.GONE);
        }

        //highlight init mode on OnMeasure calls
        if(initModeIndex != -1){
           Log.d(TAG,"Tony initModeIndex = " + initModeIndex);
           updateTextColor(initModeIndex);
        }
    }

    /***
     *  starts to play entering mode animation.
     * @param enterModeIndex entering mode index
     *        from Manaul mode 0 to MicroVideo mode 5
     */
    private void enterModeAnimation(final int enterModeIndex) {
        Log.d(TAG, "Tony enterModeAnimation index = " + enterModeIndex);
        //get view in HorizontalScrollView include 2 mGapHolder and 6 modes,so index need +1
        View v = mChildWrapper.getChildAt(enterModeIndex+1);

        // Calculate the childview position in parent
        // start animation from that position.
        mDownX = v.getLeft()  + v.getWidth()/2;
        mDownY = v.getTop();

        mRadiusAnimator = ObjectAnimator.ofFloat(ModeStripView.this, "radius", CameraUtil.dpToPixel(0), mMaxRadius + CameraUtil.dpToPixel(mRadiusIncrement))
                .setDuration(ENTER_MODE_ANIM_DURATION);

        mRadiusAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        mRadiusAnimator.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationStart(Animator animation) {
                mIsEnterModeAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setRadius(ANIM_RESET_RADIUS);
                setAlpha(ANIM_RESET_ALPHA);
                mIsEnterModeAnimating = false;
                mDownX = -1;
                mDownY = -1;
            }
        });

        mRadiusAnimator.start();
    }

    /***
     *  start leaving mode animations
     * @param leaveModeIndex leaving mode index
     *        from Manaul mode 0 to MicroVideo mode 5
     */
    private void leaveModeAnimation(final int leaveModeIndex) {
        Log.d(TAG, "Tony leaveModeAnimation index = " + leaveModeIndex);
        //get leaving modeview in HorizontalScrollView include 2 mGapHolder and 6 modes,so index need +1
        View v = mChildWrapper.getChildAt(leaveModeIndex + 1);

        // Calculate the childview relatived position in parent and get the center position
        // start animation from that position.
        mDownX = v.getLeft() + v.getWidth()/2;
        mDownY = v.getTop();

        mRadiusAnimator = ObjectAnimator.ofFloat(ModeStripView.this, "radius"
                , mMaxRadius + CameraUtil.dpToPixel(mRadiusIncrement),CameraUtil.dpToPixel(0))
                .setDuration(LEAVE_MODE_ANIM_DURATION);

        mRadiusAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mRadiusAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                mIsLeaveModeAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setRadius(ANIM_RESET_RADIUS);
                setAlpha(ANIM_RESET_ALPHA);
                mIsLeaveModeAnimating = false;
                mDownX = -1;
                mDownY = -1;
            }

        });

        mRadiusAnimator.start();
    }

    private void updateTextColor(int index) {
        if (mChildWrapper != null && mChildWrapper.getChildCount() >1) {
            for (int i = 1; i < mChildWrapper.getChildCount() - 1; i++) {
                View view = mChildWrapper.getChildAt(i);
                TextView mTx = (TextView) view.findViewById(R.id.mode_title);
                if (i == (index+1)) {
                    mTx.setTextColor(getResources().getColor(R.color.mode_title_select));
                } else {
                    mTx.setTextColor(getResources().getColor(R.color.mode_title_unselect));
                }
            }
        }
    }

    public void setRadius(final float radius) {
        mRadius = radius;
        if (mRadius > 0) {
            mRadialGradient = new RadialGradient(mDownX, mDownY, mRadius,
                    adjustAlpha(mRippleColor, mAlphaFactor), mRippleColor,
                    Shader.TileMode.MIRROR);
            mPaint.setShader(mRadialGradient);
        }
        invalidate();
    }

    public void requestLockModeStripView(boolean enable) {
        if (enable) {
            if (mModeSelectionLockToken != null) {
                return;
            }
            mModeSelectionLockToken = lockView();
        } else {
            if (mModeSelectionLockToken == null) {
                return;
            }
            unLockView(mModeSelectionLockToken);
            mModeSelectionLockToken = null;
        }
    }

    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    private int mCurrentIndex=-1;
    private int mPendingIndex=0;
    public synchronized void setCurrentItem(int index,boolean needAnimation){
        if(mAdapter==null||index>=mAdapter.getCount()){
            return;//invalid index
        }
        //View not ready or not initialized,
        //BottomBar calls OnMeasure to adjusts current mode according to saved mPendingIndex.
        if(mCoordsX.size()==0){
            Log.e(TAG, "View not ready or not initialized");
            if(mPendingIndex!=index){
                if(mListener!=null){
                    mListener.onModeIdChanged((int) mAdapter.getItemId(index));
                }
            }
            mPendingIndex = index;
        } else{
            //View is ready, put mode changes behind UI update.
            int targetX =mCoordsX.get(index);
            if(!needAnimation){
                this.scrollTo(targetX, 0);
            }else{
                this.smoothScrollTo(targetX, 0);
            }

            if(mPendingIndex!=index){
                if(mListener!=null){
                    mListener.onModeIdChanged((int) mAdapter.getItemId(index));
                }
            }
            mPendingIndex = index;
            mCurrentIndex = index;
        }
    }

    public void setCurrentItem(int index){
        setCurrentItem(index,false);
    }

    public void setCurrentModeWithModeIndex(int modeIndex){//Map the mode index into the scroller index
        if(mAdapter==null){
            return;
        }
        for(int i=0;i<mAdapter.getCount();i++){
            ModeStripViewAdapter.ModuleHolder item=(ModeStripViewAdapter.ModuleHolder)mAdapter.getItem(i);
            if(item.MODULE_INDEX==modeIndex){
                //when mChildWrapper init OK , mode title hightlight
                initModeIndex = i;
                updateTextColor(initModeIndex);
                setCurrentItem(i);
                return;
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public void fling(int velocityX) {
        super.fling(0);
    }//disable any fling jobs

    private static final int DIRECTION_NONE=-1;
    private static final int DIRECTION_LEFT=1;
    private static final int DIRECTION_RIGHT=2;

    private int mScrolllingDirection=DIRECTION_NONE;

    @Override
    public void lockSelf() {
        mMultiLock.aquireLock(this.hashCode());
    }

    @Override
    public void unLockSelf() {
        mMultiLock.unlockWithToken(this.hashCode());
    }

    public boolean isLocked() {
       return mMultiLock.isLocked();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_UP) {
            return false;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public synchronized boolean onTouchEvent(MotionEvent ev) {
        if(mMultiLock.isLocked()){
            return  false;
        }
        if(ev.getAction()==MotionEvent.ACTION_MOVE){
            if(ev.getHistorySize()!=0){
                double dx=ev.getX()-ev.getHistoricalX(0);
                double dy=ev.getY()-ev.getHistoricalY(0);
                boolean isHorizontalMove=(Math.abs(dx/dy)>=1);
                if(isHorizontalMove){
                    mScrolllingDirection=dx>0?DIRECTION_LEFT:DIRECTION_RIGHT;
                }else{
                    mScrolllingDirection=DIRECTION_NONE;
                }
            }

            return false;
        }
            if(MotionEvent.ACTION_UP==ev.getAction()){
            int targetIndex=mCurrentIndex;
            if(DIRECTION_LEFT==mScrolllingDirection){
                if (targetIndex>0 && mModeScrollBar != null) {
                    mSlideToModeIndex = targetIndex-1;
                    mModeScrollBar.scrollToLeft(mCurrentIndex, new ModeScrollBar.onBarStatueChangedListener() {
                        @Override
                        public void onScrollStarted() {
                            getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    requestLockModeStripView(true);
//                                    if(!AppController.ENABLE_BLUR_TRANS) {
//                                        leaveModeAnimation(mCurrentIndex);
//                                    }
                                }
                            });
                        }

                        @Override
                        public void onItemReached() {
                            getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    updateTextColor(mCurrentIndex - 1);
                                }
                            });
                        }

                        @Override
                        public void onEndArrived() {
                            getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    setCurrentItem(mCurrentIndex - 1, true);
//                                    if(!AppController.ENABLE_BLUR_TRANS) {
//                                        enterModeAnimation(mSlideToModeIndex);
//                                    }
                                }
                            });
                        }

                        @Override
                        public void onScrollFinished() {
                            getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    requestLockModeStripView(false);
                                }
                            });
                        }
                    });
                }
                //this.setCurrentItem(targetIndex,true);
            }else if(DIRECTION_RIGHT==mScrolllingDirection){
                if (targetIndex<mCoordsX.size()-1 && mModeScrollBar != null) {
                    if (targetIndex >= 0) {
                        mSlideToModeIndex = targetIndex + 1;
                    }
                    mModeScrollBar.scrollToRight(mCurrentIndex, new ModeScrollBar.onBarStatueChangedListener() {
                        @Override
                        public void onScrollStarted() {
                            getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    requestLockModeStripView(true);
//                                    if(!AppController.ENABLE_BLUR_TRANS) {
//                                        leaveModeAnimation(mCurrentIndex);
//                                    }
                                }
                            });
                        }

                        @Override
                        public void onItemReached() {
                            getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    updateTextColor(mCurrentIndex + 1);
                                }
                            });
                        }

                        @Override
                        public void onEndArrived() {
                            getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    setCurrentItem(mCurrentIndex + 1, true);
//                                    if(!AppController.ENABLE_BLUR_TRANS) {
//                                        enterModeAnimation(mSlideToModeIndex);
//                                    }
                                }
                            });
                        }

                        @Override
                        public void onScrollFinished() {
                            getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    requestLockModeStripView(false);
                                }
                            });
                        }
                    });
                }
                //this.setCurrentItem(targetIndex,true);
            }

            mScrolllingDirection=DIRECTION_NONE;
        }

        return false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxRadius = (float) Math.sqrt(w/2 * w/2 + h/2 * h/2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode()) {
            return;
        }
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        mPath.reset();
        mPath.addCircle(mDownX, mDownY, mRadius, Path.Direction.CW);
        canvas.clipPath(mPath);
        canvas.restore();
        canvas.drawCircle(mDownX, mDownY, mRadius, mPaint);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(onSaveInstanceState());
    }

    private int mStartCoord=0;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(mAdapter==null||mChildWrapper.getChildCount()==0){
            return;
        }else{
            synchronized(this){
                int count=mAdapter.getCount();
                mCoordsX.clear();
                mVisualWidth=this.getMeasuredWidth();

                //The visible area is not measured until onMeasure callback, initialize gap_holder here
                if(mGapHolder[0]==null||mGapHolder[1]==null){
                    int gapLeft=mVisualWidth/2;
                    int gapRight=gapLeft;
                    int childCount=mChildWrapper.getChildCount();
                    if(childCount!=0){
                        gapLeft=gapLeft-mChildWrapper.getChildAt(0).getMeasuredWidth()/2;
                        mStartCoord=gapLeft;
                        gapRight=gapRight-mChildWrapper.getChildAt(childCount-1).getMeasuredWidth()/2;
                    }
                    LinearLayout.LayoutParams lpLeft=new LinearLayout.LayoutParams(gapLeft, GAP_HEIGHT);
                    LinearLayout.LayoutParams lpRight=new LinearLayout.LayoutParams(gapRight, GAP_HEIGHT);
                    View gap_left=new View(this.getContext());
                    View gap_right=new View(this.getContext());
                    gap_left.setLayoutParams(lpLeft);
                    gap_right.setLayoutParams(lpRight);
                    mGapHolder[0]=gap_left;
                    mGapHolder[1]=gap_right;
                    notifyDatasetChanged();
                    measure(widthMeasureSpec,heightMeasureSpec);//force strip view re-calculate the left & right gap
                    return;
                }

                int coordsX=mStartCoord;
                for(int i=1;i<count+1;i++){
                    //i indicates for every index of item in adapter ,
                    //why start from 1 ? for the very first child is always the LEFT_GAP
                    View currentView=mChildWrapper.getChildAt(i);
                    int width=currentView.getWidth();
                    if(width==0){
                        mCoordsX.clear();
                        return;
                    }
                    int coord=coordsX+width/2-mVisualWidth/2;
                    mCoordsX.add(coord);
                    coordsX+=width;
                }
                if(mPendingIndex!=mCurrentIndex){
                    setCurrentItem(mPendingIndex);
                }
                setupScrollBar();
            }
        }
    }

    @Override
    public void attachScrollIndicator(ScrollIndicator scrollIndicator) {
        //dummy
    }

    public void addScrollBar(ModeScrollBar bar) {
        mModeScrollBar = bar;
    }

    public void setupScrollBar() {
        if (mModeScrollBar == null) {
            return;
        }

        if (mChildWrapper == null) {
            return;
        }

        int count = mChildWrapper.getChildCount();
        for (int i = 1; i < count - 1; i++) {
            View v = mChildWrapper.getChildAt(i);
            if (v.getWidth() == 0) {
                mItemWidthList.clear();
                return;
            }
            mItemWidthList.add(v.getWidth());
        }
        mModeScrollBar.setItemWidth(mItemWidthList);

        if (mCurrentIndex != -1) {
            mModeScrollBar.setOriIndex(mCurrentIndex);
        }
    }

    public void init(ModuleManager moduleManager){
        Adapter adapter=new ModeStripViewAdapter(moduleManager);
        setAdapter(adapter);
    }

    private class ModeStripViewAdapter extends BaseAdapter{

        private class ModuleHolder{
            public String MODULE_NAME;
            public int MODULE_INDEX;
            private ModuleHolder(int index){
                MODULE_INDEX=index;
                MODULE_NAME=CameraUtil.getCameraModeText(index, ModeStripView.this.getContext());
            }

            public View getModuleView(){
                LayoutInflater inflater=LayoutInflater.from(ModeStripView.this.getContext());
                View view=inflater.inflate(R.layout.mode_strip_item, null);
                TextView textView=(TextView)view.findViewById(R.id.mode_title);
                textView.setText(MODULE_NAME);
                return view;
            }
        }

        List<ModuleHolder> mModules=new ArrayList<ModuleHolder>();

        public ModeStripViewAdapter(ModuleManager manager){
            List<Integer> supportedModules=manager.getSupportedModeIndexList();
            for(int index : supportedModules){
                ModuleManagerImpl.ModuleAgent agent = manager.getModuleAgent(index);
                if(agent.needAddToStrip()) {
                    mModules.add(new ModuleHolder(index));
                }
            }
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

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public int lock() {
        return mMultiLock.aquireLock();
    }

    @Override
    public boolean unlockWithToken(int token) {
        return mMultiLock.unlockWithToken(token);
    }
}
