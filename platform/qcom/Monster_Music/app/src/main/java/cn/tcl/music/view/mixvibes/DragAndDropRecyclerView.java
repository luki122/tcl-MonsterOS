/* ----------|----------------------|---------------------|-------------------*/
/* 20/05/2015|xiaolong.zhang        |PR999146             |Cannot play the song when tapping a song after dragged a song in queue      */
/* ----------|----------------------|---------------------|-------------------*/
/* 12/06/2015|xiaolong.zhang        |PR1020403            |The songs disappear when move the songs sequence in queue      */
/* ----------|----------------------|---------------------|-------------------*/
package cn.tcl.music.view.mixvibes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class DragAndDropRecyclerView extends ContextMenuReyclerView {

    private static final String TAG = DragAndDropRecyclerView.class.getSimpleName();
    private static final int INVALID_POINTER_ID = -1;
    private static int LINE_THICKNESS = 1;
    private static final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 150;
    private static final int INVALID_ID = -1;
    /**
     * Determines the start of the upward drag-scroll region
     * at the top of the ListView. Specified by a fraction
     * of the ListView height, thus screen resolution agnostic.
     */
    private float mDragUpScrollStartFrac = 1.0f / 3.0f;
    private float mDragDownScrollStartFrac = 1.0f / 3.0f;

    private int mEdgeUpScrollStartY;
    private int mEdgeDownScrollStartY;

    private int activePointerId = INVALID_POINTER_ID;
    private int lastEventY, lastEventX;
    private int downX;
    private int downY;
    private int totalOffsetY, totalOffsetX;

    private BitmapDrawable hoverCell;
    private Drawable backgroundDrawable;
    private Rect hoverCellOriginalBounds = new Rect();
    private Rect hoverCellCurrentBounds = new Rect();

    private boolean cellIsMobile = false;
    private long mobileItemId = INVALID_ID;

    private int smoothScrollAmountAtEdge;
    private boolean usWaitingForScrollFinish;

    private boolean shouldManageUp = true;
    private boolean isHoverViewAnimatorFinish = true;//if the HoverViewAnimator finish will be true

    public interface OnMoveElementListener{
    	void startMove(int fromPosition);
    	void onElementMoved(int fromIndex, int toIndex);
    	void endMove(int fromPosition, int toPosition);
    }

    public OnMoveElementListener onMoveListener;

	public DragAndDropRecyclerView(Context context) {
		super(context);
		init(context);
	}

	public DragAndDropRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DragAndDropRecyclerView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private int mFromPosition = -1;
	private int mCurrentToPosition = - 1;

	private void manageMove(View viewToBeMoved)
	{
        if(!isHoverViewAnimatorFinish){//if animation is not finish,user can not drag
            return;
        }
		if (cellIsMobile)
			return;
        mobileItemId = getChildItemId(viewToBeMoved);
        mFromPosition = getChildPosition(viewToBeMoved);
        hoverCell = getAndAddHoverView(viewToBeMoved);
        if (backgroundDrawable == null){
            backgroundDrawable = getResources().getDrawable(com.mixvibes.mvlib.R.drawable.drag_cell_background);
            backgroundDrawable.getPadding(new Rect());//[bugfix] -add to trigger refresh drawable by TCTNJ-zhongrui.guo1,PR1016108,2015-06-3
        }
        viewToBeMoved.setVisibility(INVISIBLE);
        cellIsMobile = true;
        if (onMoveListener != null)
        	onMoveListener.startMove(mFromPosition);
	}

	public void manageMoveFromTouch(MotionEvent event, View viewToBeMoved, int yOffset)
	{
		MotionEvent cancelEvent = MotionEvent.obtain(event);
		cancelEvent.setAction(MotionEvent.ACTION_CANCEL);

		mLongPressDetector.onTouchEvent(cancelEvent);
		cancelEvent.recycle();
		shouldManageUp = true;
		if (viewToBeMoved == null)
			return;

        cancelLongPress();

        downX = (int) viewToBeMoved.getX();
        downY = (int) (viewToBeMoved.getTop() + event.getY());
        activePointerId = event.getPointerId(0);

        totalOffsetY = 0;
        totalOffsetX = 0;
        manageMove(viewToBeMoved);
	}

	OnItemTouchListener mOnItemTouchListener;

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		updateScrollStarts();
	}

	private float mDragUpScrollHeight;
	private float mDragDownScrollHeight;

    private void updateScrollStarts() {
        final int padTop = getPaddingTop();
        final int listHeight = getHeight() - padTop - getPaddingBottom();
        float heightF = (float) listHeight;

        float upScrollStartYF = padTop + mDragUpScrollStartFrac * heightF;
        float downScrollStartYF = padTop + (1.0f - mDragDownScrollStartFrac) * heightF;

        mEdgeUpScrollStartY = (int) upScrollStartYF;
        mEdgeDownScrollStartY = (int) downScrollStartYF;

       mDragUpScrollHeight = upScrollStartYF - padTop;
       mDragDownScrollHeight = padTop + listHeight - downScrollStartYF;
    }

    private GestureDetector mLongPressDetector;

    protected void init(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        smoothScrollAmountAtEdge = (int) (SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);
        LINE_THICKNESS = (int) ( LINE_THICKNESS * metrics.density);

        backgroundDrawable = context.getResources().getDrawable(com.mixvibes.mvlib.R.drawable.drag_cell_background);
        backgroundDrawable.getPadding(new Rect());//[bugfix] -add to trigger refresh drawable by TCTNJ-zhongrui.guo1,PR1016108,2015-06-3

        mLongPressDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent event) {

            	int localDownX = (int) event.getX();
                int localDownY = (int) event.getY();
                View selectedView = findChildViewUnder(localDownX, localDownY);
                if (selectedView == null) {
                    return;
                }
                downX = localDownX;
                downY = localDownY;
                activePointerId = event.getPointerId(0);

                totalOffsetY = 0;
                totalOffsetX = 0;

                manageMove(selectedView);
            }

        });

        //
        mOnItemTouchListener = new OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {

            	String actionToString = null;


            	switch (event.getAction())
            	{
            	case MotionEvent.ACTION_DOWN:
            		actionToString = "Action_DOWN";
            		break;
            	case MotionEvent.ACTION_MOVE:
            		actionToString = "Action_MOVE";
            		break;
            	case MotionEvent.ACTION_UP:
            		actionToString = "Action_UP";
            		break;
            	case MotionEvent.ACTION_CANCEL:
            		actionToString = "Action_CANCEL";
            		break;
            	case MotionEvent.ACTION_OUTSIDE:
            		actionToString = "Action_OUTSIDE";
            		break;
            	}


            	if (event.getAction() == MotionEvent.ACTION_UP)
            	{
                	if (!shouldManageUp )
                	{
                		shouldManageUp = true;
                		return false;
                	}

                	if(cellIsMobile)
                	{
                		touchEventsEnded();
                		return false;
                	}
            	}

                if (mLongPressDetector.onTouchEvent(event)) {
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        return cellIsMobile;
                    default:
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent event) {
                handleMotionEvent(event);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        };
        addOnItemTouchListener(mOnItemTouchListener);
    }
    @Override
    protected void onDetachedFromWindow() {
    	removeOnItemTouchListener(mOnItemTouchListener);
    	mOnItemTouchListener = null;
    	super.onDetachedFromWindow();
    }

    private void handleMotionEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (activePointerId == INVALID_POINTER_ID) {
                    break;
                }

                int pointerIndex = event.findPointerIndex(activePointerId);
                if (pointerIndex < 0)
                {
                	touchEventsCancelled();
                	break;
                }


                lastEventY = (int) event.getY(pointerIndex);
                lastEventX = (int) event.getX(pointerIndex);
                int deltaY = lastEventY - downY;
                int deltaX = lastEventX - downX;

                int newY = hoverCellOriginalBounds.top + deltaY + totalOffsetY;
                if (newY <= 0)
                	newY = 0;
                else if (newY >= getHeight() - hoverCellCurrentBounds.height())
                {
                	newY = getHeight() - hoverCellCurrentBounds.height();
                }

                if (cellIsMobile) {
                    hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left ,
                            newY);
                    if (hoverCell != null)
                    {
                    	hoverCell.setBounds(hoverCellCurrentBounds);
                    	invalidate();
                    }
                    if (backgroundDrawable != null)
                    {
                    	backgroundDrawable.setBounds(hoverCellCurrentBounds);
                    	invalidate();
                    }

                    handleCellSwitch();

                    handleMobileCellScroll();
                }
                break;
            case MotionEvent.ACTION_UP:
                touchEventsEnded();
                break;
            case MotionEvent.ACTION_CANCEL:
                touchEventsCancelled();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                /* If a multitouch event took place and the original touch dictating
                 * the movement of the hover cell has ended, then the dragging event
                 * ends and the hover cell is animated to its corresponding position
                 * in the listview. */
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                        MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    touchEventsEnded();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Creates the hover cell with the appropriate bitmap and of appropriate
     * size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
     * single time an invalidate call is made.
     */
    private BitmapDrawable getAndAddHoverView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        int top = v.getTop();
        int left = v.getLeft();

        v.setPressed(false);

        // Create a copy of the drawing cache so that it does not get
        // recycled by the framework when the list tries to clean up memory
        //v.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        v.setDrawingCacheEnabled(true);
        Bitmap b = getBitmapWithBorder(v);
        v.setDrawingCacheEnabled(false);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

        hoverCellOriginalBounds.set(left, top, left + w, top + h);
        hoverCellCurrentBounds.set(hoverCellOriginalBounds);

        drawable.setBounds(hoverCellCurrentBounds);
        backgroundDrawable.setBounds(hoverCellCurrentBounds);

        return drawable;
    }

    /**
     * Draws a black border over the screenshot of the view passed in.
     */
    private Bitmap getBitmapWithBorder(View v) {
        Bitmap bitmap = getBitmapFromView(v);
        //Canvas can = new Canvas(bitmap);

//        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//
//        Paint paint = new Paint();
//        paint.setStyle(Paint.Style.FILL);
//        //paint.setStrokeWidth(LINE_THICKNESS);
//        paint.setColor(0xff4d6370);
        //can.drawRect(rect, paint);
        //can.drawBitmap(bitmap, 0, 0, null);
        //can.drawColor(0xff4d6370, Mode.DST_OVER);
        //can.drawColor();


        return bitmap;
    }

    /**
     * Returns a bitmap showing a screenshot of the view passed in.
     */
    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }


    /**
     * dispatchDraw gets invoked when all the child views are about to be drawn.
     * By overriding this method, the hover cell (BitmapDrawable) can be drawn
     * over the recyclerviews's items whenever the recyclerviews is redrawn.
     */
    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        if (hoverCell != null) {
	        if (backgroundDrawable != null)
	        {
	        	backgroundDrawable.draw(canvas);
	        }
            hoverCell.draw(canvas);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
    }

    View currentChildUnder = null;


    /**
     * This method determines whether the hover cell has been shifted far enough
     * to invoke a cell swap. If so, then the respective cell swap candidate is
     * determined and the data set is changed. Upon posting a notification of the
     * data set change, a layout is invoked to place the cells in the right place.
     */
    private void handleCellSwitch() {
        ViewHolder mobileViewHolder = findViewHolderForItemId(mobileItemId);
        View mobileView = mobileViewHolder != null ? mobileViewHolder.itemView : null;
        if (mobileView != null) {
            View childViewUnder = findChildViewUnder(lastEventX, lastEventY);
            if (childViewUnder != null && childViewUnder != currentChildUnder) {
                final int originalItem = getChildPosition(mobileView);
            	int toPosition = getChildPosition(childViewUnder);

//            	if (toPosition == mCurrentToPosition)
//            		return;

            	mCurrentToPosition = toPosition;

                swapElements(originalItem, mCurrentToPosition);
                currentChildUnder = childViewUnder;
            }
        }
    }

    /**
     * Swaps the the elements with the given indices.
     *
     * @param fromIndex the from-element index
     * @param toIndex   the to-element index
     */
    private void swapElements(int fromIndex, int toIndex) {
    	if (fromIndex == toIndex)
    		return;
        Adapter<?> adapter = getAdapter();
        if (fromIndex == toIndex)
        	return;
        if(onMoveListener != null)
        	onMoveListener.onElementMoved(fromIndex, toIndex);
    }

    public void setOnMoveListener(OnMoveElementListener onMoveListener)
    {
    	this.onMoveListener = onMoveListener;
    }

    /**
     * Resets all the appropriate fields to a default state while also animating
     * the hover cell back to its correct location.
     */
    public void touchEventsEnded() {
        ViewHolder viewHolderForItemId = findViewHolderForItemId(mobileItemId);
        cancelLongPress();
        if (viewHolderForItemId == null) {
            cellIsMobile = false;
            return;
        }

        if (onMoveListener != null)
        {
        	if (mCurrentToPosition >= 0 && mFromPosition >= 0)
        		onMoveListener.endMove(mFromPosition, mCurrentToPosition);
        	mCurrentToPosition = -1;
        	mFromPosition = -1;
        }
        final View mobileView = viewHolderForItemId.itemView;
        if (cellIsMobile || usWaitingForScrollFinish) {
            cellIsMobile = false;
            usWaitingForScrollFinish = false;
            activePointerId = INVALID_POINTER_ID;

            // If the autoscroller has not completed scrolling, we need to wait for it to
            // finish in order to determine the final location of where the hover cell
            // should be animated to.
            if (getScrollState() != SCROLL_STATE_IDLE) {
                usWaitingForScrollFinish = true;
                return;
            }

            hoverCellCurrentBounds.offsetTo(mobileView.getLeft(), mobileView.getTop());

            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(hoverCell, "bounds",
                    sBoundEvaluator, hoverCellCurrentBounds);
            hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });
            hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mobileItemId = INVALID_ID;
                    mobileView.setVisibility(VISIBLE);
                    hoverCell = null;
                    setEnabled(true);
                    invalidate();
                    isHoverViewAnimatorFinish = true;
                }
            });
            hoverViewAnimator.start();
            isHoverViewAnimatorFinish = false;

            ObjectAnimator backgroundViewAnimator = ObjectAnimator.ofObject(backgroundDrawable, "bounds",
                    sBoundEvaluator, hoverCellCurrentBounds);
            hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });
            hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mobileItemId = INVALID_ID;
                    mobileView.setVisibility(VISIBLE);
                    hoverCell = null;
                    setEnabled(true);
                    invalidate();
                }
            });
            backgroundViewAnimator.start();
        } else {
            touchEventsCancelled();
        }

    }

    /**
     * This TypeEvaluator is used to animate the BitmapDrawable back to its
     * final location when the user lifts his finger by modifying the
     * BitmapDrawable's bounds.
     */
    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction),
                    interpolate(startValue.top, endValue.top, fraction),
                    interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int) (start + fraction * (end - start));
        }
    };

    /**
     * Resets all the appropriate fields to a default state.
     */
    public void touchEventsCancelled() {
        ViewHolder viewHolderForItemId = findViewHolderForItemId(mobileItemId);
        cancelLongPress();
        if (viewHolderForItemId == null) {
            cellIsMobile = false;
            return;
        }
        View mobileView = viewHolderForItemId.itemView;
        if (cellIsMobile) {
            mobileItemId = INVALID_ID;
            mobileView.setVisibility(VISIBLE);
            hoverCell = null;
            invalidate();
        }
        cellIsMobile = false;
        activePointerId = INVALID_POINTER_ID;
    }

    /**
     * Determines whether this recyclerview is in a scrolling state invoked
     * by the fact that the hover cell is out of the bounds of the recyclerview;
     */
    private void handleMobileCellScroll() {
        handleMobileCellScroll(hoverCellCurrentBounds);
    }

    /**
     * This method is in charge of determining if the hover cell is above/below or
     * left/right the bounds of the recyclerview. If so, the recyclerview does an appropriate
     * upward or downward smooth scroll so as to reveal new items.
     */
    public boolean handleMobileCellScroll(Rect r) {
        if (getLayoutManager().canScrollVertically()) {
            int offset = computeVerticalScrollOffset();
            int height = getHeight();
            int extent = computeVerticalScrollExtent();
            int range = computeVerticalScrollRange();
            int hoverViewTop = r.top;
            int hoverHeight = r.height();

            if (hoverViewTop <= mEdgeUpScrollStartY)
            {
            	int smoothScrollValue = (int) (( (hoverViewTop/mDragUpScrollHeight) - 1 ) * smoothScrollAmountAtEdge);
                scrollBy(0, smoothScrollValue);
                return true;
            }

        	if(hoverViewTop >= mEdgeDownScrollStartY)
        	{

        		int smoothScrollValue = (int) (((hoverViewTop + hoverHeight)/ (float) height) * smoothScrollAmountAtEdge);
        		scrollBy(0, smoothScrollValue);
                return true;
            }
        }

        if (getLayoutManager().canScrollHorizontally()) {
            int offset = computeHorizontalScrollOffset();
            int width = getWidth();
            int extent = computeHorizontalScrollExtent();
            int range = computeHorizontalScrollRange();
            int hoverViewLeft = r.left;
            int hoverWidth = r.width();

            if (hoverViewLeft <= 0 && offset > 0) {
                scrollBy(-smoothScrollAmountAtEdge, 0);
                return true;
            }

            if (hoverViewLeft + hoverWidth >= width && (offset + extent) < range) {
                scrollBy(smoothScrollAmountAtEdge, 0);
                return true;
            }
        }

        return false;
    }
}
