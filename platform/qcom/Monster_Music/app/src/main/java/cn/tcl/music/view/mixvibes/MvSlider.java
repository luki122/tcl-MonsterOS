package cn.tcl.music.view.mixvibes;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

public class MvSlider extends View {
    	public interface OnSliderChangeListener {
        void onSliderProgressWillChange(MvSlider slider, double progress);
		}

	private GestureDetector.SimpleOnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			setPreProgress(maxProgress / 2);
			return true;
		}
	};
	private Drawable mThumb = null;
    private Drawable mBackgroundSlider;
	private GestureDetectorCompat mDetector;
    private OnSliderChangeListener mOnSliderChangeListener = null;
	private int fillColorGravity = Gravity.CENTER;
	
	private final static int ORIENTATION_VERTICAL = 1;
	
	private int maxProgress = 0;
	private int progress = 0;
	private int minProgress = 0;
	private int midProgress = 0;
	
	private int mThumbOffset;
	
	
	private int backgroundSliderWidth = 0;
	private int backgroundSliderHeight = 0;
	
	private int mPaddingLeft;
	private int mPaddingRight;
	private int mPaddingTop;
	private int mPaddingBottom;
	
	private String textToDisplay;
	
	
	private int fillColor;
	private int secondFillColor;
	private boolean splitColor;
	private float radiusFillColorRect;
	private Paint fillColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private RectF fillBackgroundColorRect = new RectF();
	private RectF secondFillBackgroundColorRect = new RectF();
	private float marginFillBgColor ;
	
	private boolean isVertical;
	
	private float relativeBackgroundSize;
	
    public MvSlider(Context context) {
        this(context, null);
    }
    
    public MvSlider(Context context, AttributeSet attrs) {
    	this(context, attrs, 0);
    }

    public MvSlider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode())
        {
	        mDetector = new GestureDetectorCompat(context, onGestureListener);
	        mDetector.setOnDoubleTapListener(onGestureListener);
	        mDetector.setIsLongpressEnabled(false);
        }
        TypedArray a = context.obtainStyledAttributes(attrs, com.mixvibes.mvlib.R.styleable.MvSlider
               , defStyle, 0);
        
        int orientation = a.getInteger(com.mixvibes.mvlib.R.styleable.MvSlider_orientation, 1);
        
        isVertical = orientation == ORIENTATION_VERTICAL;
        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();
        mPaddingTop = getPaddingTop();
        mPaddingBottom = getPaddingBottom();
        
        Drawable thumb = a.getDrawable(com.mixvibes.mvlib.R.styleable.MvSlider_thumb);
        setThumb(thumb);
        mBackgroundSlider = a.getDrawable(com.mixvibes.mvlib.R.styleable.MvSlider_backgroundSliderDrawable);
        
        maxProgress = a.getInteger(com.mixvibes.mvlib.R.styleable.MvSlider_maxProgress, 100);
        
        minProgress = a.getInteger(com.mixvibes.mvlib.R.styleable.MvSlider_minProgress, 0);
        
        midProgress = (maxProgress - minProgress) / 2;
        
        progress = a.getInteger(com.mixvibes.mvlib.R.styleable.MvSlider_progress, 0);
        
        splitColor = a.getBoolean(com.mixvibes.mvlib.R.styleable.MvSlider_splitColor, false);
        
        textToDisplay = a.getString(com.mixvibes.mvlib.R.styleable.MvSlider_android_text);
        
        relativeBackgroundSize = a.getFloat(com.mixvibes.mvlib.R.styleable.MvSlider_backgroundSliderRelSize, 1);
        float backgroundSize = a.getDimension(com.mixvibes.mvlib.R.styleable.MvSlider_backgroundSliderSize, 0);
    	if (isVertical)
    		backgroundSliderWidth = (int) (backgroundSize - mPaddingLeft - mPaddingRight);
    	else
    		backgroundSliderHeight = (int) (backgroundSize - mPaddingTop - mPaddingBottom);
        
        if (mBackgroundSlider != null)
        {
        	mBackgroundSlider.setCallback(this);
        	
        	float alpha = a.getFloat(com.mixvibes.mvlib.R.styleable.MvSlider_backgroundSliderAlpha, 1);
        	mBackgroundSlider.setAlpha((int) (alpha * 255));
        	
        }
        
        float dp = getResources().getDisplayMetrics().density;
        
        marginFillBgColor = a.getDimension(com.mixvibes.mvlib.R.styleable.MvSlider_marginFillColorRect, 2 * dp);
        radiusFillColorRect = a.getDimension(com.mixvibes.mvlib.R.styleable.MvSlider_radiusFillColorRect, 3 * dp);
        
        fillColor = a.getColor(com.mixvibes.mvlib.R.styleable.MvSlider_fillColor, Color.TRANSPARENT);
        secondFillColor = a.getColor(com.mixvibes.mvlib.R.styleable.MvSlider_secondFillColor, Color.TRANSPARENT);
        
        a.recycle();
        
    }
    
    public void setBackgroundSlider(int backgroundSliderResId)
    {
    	setBackgroundSlider(getResources().getDrawable(backgroundSliderResId));
    }
    
    public void setBackgroundSlider(Drawable backgroundSlider)
    {
    	if (mBackgroundSlider == backgroundSlider)
    		return;
        if (mBackgroundSlider != null)
        	mBackgroundSlider.setCallback(null);
        
        mBackgroundSlider = backgroundSlider;
        if (mBackgroundSlider != null)
        	mBackgroundSlider.setCallback(this);
        
        invalidate();
    }
    public void setGravityFillColor(int gravity)
    {
    	fillColorGravity = gravity;
    }
    
    public void setDefaultThumbColors (final int colorLine, final int colorCap)
    {
    }
    
    public int getCrossPaddingBottom() {
    	return mPaddingBottom;
    }
    
    public int getCrossPaddingTop() {
    	return mPaddingTop;
    }
    
    public int getCrossPaddingRight() {
    	return mPaddingRight;
    }
    
    public int getCrossPaddingLeft() {
    	return mPaddingLeft;
    }
    
    public Drawable getThumbDrawable(){
    	return mThumb;
    }
    
    public void setThumb(Drawable thumb) {
        boolean needUpdate;
        
        /*if (hidden) {
			mThumbHidden = thumb;
			return;
        }*/
			
        // This way, calling setThumb again with the same bitmap will result in
        // it recalcuating mThumbOffset (for example, if the bounds of the drawable has changed)
        if (mThumb != null && thumb != mThumb) {
            mThumb.setCallback(null);
            needUpdate = true;
        } else {
            needUpdate = false;
        }
        if (thumb != null) {

            if (isVertical)
            {
            	mThumbOffset = thumb.getIntrinsicHeight() / 2;
            	mPaddingTop = Math.max(mPaddingTop, mThumbOffset);
            	mPaddingBottom = Math.max(mPaddingBottom, mThumbOffset);
            }
            else
            {
            	mThumbOffset = thumb.getIntrinsicWidth() / 2;
            	mPaddingLeft = Math.max(mPaddingLeft, mThumbOffset);
            	mPaddingRight = Math.max(mPaddingRight, mThumbOffset);
            }

            if (needUpdate &&
                    (thumb.getIntrinsicWidth() != mThumb.getIntrinsicWidth()
                        || thumb.getIntrinsicHeight() != mThumb.getIntrinsicHeight())) {
                requestLayout();
            }
        }
        mThumb = thumb;
        invalidate();
        if (needUpdate) {
            updateThumbPos(getWidth(), getHeight());
            if (thumb != null && thumb.isStateful()) {

                int[] state = getDrawableState();
                thumb.setState(state);
            }
        }
    }
    
    private void updateThumbPos(int w, int h) {
    	int thumbSize = 0;
        int trackSize;
    	int supposedWidth = w - mPaddingRight - mPaddingLeft;
    	int supposedHeight = h - mPaddingTop - mPaddingBottom;
    	if (isVertical)
    	{
    		thumbSize = mThumb == null ? 0 : Math.min(w, mThumb.getIntrinsicWidth());
            if (backgroundSliderWidth > 0)
            	trackSize = (int) (backgroundSliderWidth * relativeBackgroundSize);
            else
            	trackSize = (int) (supposedWidth * relativeBackgroundSize);
    	}
    	else
    	{
    		thumbSize = mThumb == null ? 0 : Math.min(h, mThumb.getIntrinsicHeight());
            if (backgroundSliderHeight > 0)
            	trackSize = (int) (backgroundSliderHeight * relativeBackgroundSize);
            else
            	trackSize = (int) (supposedHeight * relativeBackgroundSize);
    	}
        
        float scale = maxProgress > 0 ? 
        			  (float) (progress - minProgress) / (float) (maxProgress - minProgress) 
        			  : 0;
        
        if (thumbSize > trackSize) {
            if (mThumb != null) {
            	int gap = 0;
            	if (isVertical)
            	{
            		if (thumbSize < w)
            			gap =  (w - thumbSize) / 2;
                    setThumbPos(supposedHeight, mThumb, scale, gap);
            	}
            	else
            	{
            		if (thumbSize < h)
            			gap =  (h - thumbSize) / 2;
                    setThumbPos(supposedWidth, mThumb, scale, gap);
            	}
            }
            int gapForCenteringTrackWidth = isVertical ? (w - trackSize) / 2 : 0;
            int gapForCenteringTrackHeight = isVertical ? 0 : (h - trackSize) / 2;
            if (mBackgroundSlider != null) {
            	mBackgroundSlider.setBounds(gapForCenteringTrackWidth, gapForCenteringTrackHeight ,
                        w - mPaddingRight - mPaddingLeft - gapForCenteringTrackWidth, h - mPaddingBottom
                        - mPaddingTop - gapForCenteringTrackHeight);
            }
        } else {
            if (mBackgroundSlider != null) {
            	mBackgroundSlider.setBounds(0, 0,
            								isVertical ? trackSize :
            										supposedWidth ,
            								isVertical ? supposedHeight :
            											trackSize);
            }
            int gap = Math.abs(thumbSize - trackSize) / 2;
            if (mThumb != null) {
                setThumbPos(isVertical ? supposedHeight : supposedWidth, mThumb, scale, gap);
            }
        }
    }
    
    private void setThumbPos(int sizeAvailable, Drawable thumb, float scale, int gap) {
        int thumbWidth = Math.min(getWidth(), thumb.getIntrinsicWidth());
        int thumbHeight = Math.min(getHeight(), thumb.getIntrinsicHeight());
        sizeAvailable -= isVertical ? thumbHeight : thumbWidth;

        sizeAvailable += mThumbOffset * 2;

        int thumbPos = (int) (scale * sizeAvailable);

        int rightBound = 0;
        int leftBound = 0;
        int topBound = 0; 
        int bottomBound = 0;
        if (gap == Integer.MIN_VALUE) {
            Rect oldBounds = thumb.getBounds();
            leftBound = oldBounds.left;
            rightBound = oldBounds.right;
            topBound = oldBounds.top;
            bottomBound = oldBounds.bottom;
        } else {
        	if (isVertical)
        	{
        		leftBound = gap;
            	rightBound = gap + thumbWidth;
        	}
        	else
        	{
                topBound = gap;
                bottomBound = gap + thumbHeight;
        	}
        }
        
        if (isVertical)
        {
	        // Canvas will be translated, so 0,0 is where we start drawing
	        topBound =  sizeAvailable - thumbPos;
	        bottomBound = topBound + thumbHeight;
        }
        else
        {
        	leftBound = thumbPos;
        	rightBound = leftBound + thumbWidth;
        }
        
        thumb.setBounds(leftBound, topBound, rightBound, bottomBound);
    }
    
    public int getThumbOffset()
    {
    	return mThumbOffset;
    }
    
    public Rect getThumbRect()
    {
    	Rect defaultBounds = new Rect(mThumb.getBounds());
    	Rect thumbRect;
    	float dp = getResources().getDisplayMetrics().density;
    	int sizeToAdd;
    	if (isVertical)
    	{
    		defaultBounds.set(defaultBounds.left, defaultBounds.top + mPaddingTop - mThumbOffset, defaultBounds.right, defaultBounds.bottom + mPaddingBottom - mThumbOffset);
    		sizeToAdd = (int)( 48 * dp - defaultBounds.bottom + defaultBounds.top );
    		int newTop = 0;
    		int newBottom = 0;
        	if (sizeToAdd > 0)
        	{
	    		if (defaultBounds.top - (sizeToAdd >> 1) < 0)
	    		{
	    			newTop = 0;
	    			newBottom = defaultBounds.bottom - defaultBounds.top + sizeToAdd;
	    		}
	    		else if (defaultBounds.bottom + (sizeToAdd >> 1) > getHeight())
	    		{
	    			
	    			newTop = defaultBounds.top + getHeight() - defaultBounds.bottom - sizeToAdd;
	    			newBottom = getHeight();
	    		}
	    		else
	    		{
	    			newTop = defaultBounds.top - (sizeToAdd >> 1);
	    			newBottom = defaultBounds.bottom + (sizeToAdd >> 1);
	    		}
        		thumbRect = new Rect(defaultBounds.left, newTop,
        							 defaultBounds.right, newBottom);
	        	return thumbRect;
        	}

    	}
    	else
    	{
    		defaultBounds.set(defaultBounds.left + mPaddingLeft - mThumbOffset, defaultBounds.top, defaultBounds.right + mPaddingRight - mThumbOffset, defaultBounds.bottom);

    		sizeToAdd = (int) (48 * dp - defaultBounds.right + defaultBounds.left);
    		int newLeft = 0;
    		int newRight = 0;
        	if (sizeToAdd > 0)
        	{
	    		if (defaultBounds.left - (sizeToAdd >> 1) < 0)
	    		{
	    			newLeft = 0;
	    			newRight = defaultBounds.right - defaultBounds.left + sizeToAdd;
	    		}
	    		else if (defaultBounds.right + (sizeToAdd >> 1) > getWidth())
	    		{
	    			
	    			newLeft =defaultBounds.left + getWidth() - defaultBounds.right - sizeToAdd;
	    			newRight = getWidth();
	    		}
	    		else
	    		{
	    			newLeft = defaultBounds.left - (sizeToAdd >> 1);
	    			newRight = defaultBounds.right + (sizeToAdd >> 1);
	    		}
        		thumbRect = new Rect(newLeft, defaultBounds.top,
        							 newRight, defaultBounds.bottom);
	        	return thumbRect;
        	}
    	}
    	return defaultBounds;
    }
    
    public void setOnSliderChangeListener (OnSliderChangeListener onCrossSliderChangeListener)
    {
    	mOnSliderChangeListener = onCrossSliderChangeListener;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
        if (mThumb != null)
        	updateThumbPos(w, h);
    	updateFillColorBackground(fillColorGravity);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(mPaddingLeft, mPaddingTop);
        if (mBackgroundSlider != null)
        {
        	mBackgroundSlider.draw(canvas);
        	//canvas.save();
        	//canvas.clipRect(fillBackgroundColorRect);
        	fillColorPaint.setColor(fillColor);
        	canvas.drawRoundRect(fillBackgroundColorRect, radiusFillColorRect, radiusFillColorRect, fillColorPaint);
    		//canvas.drawColor(fillColor);
    		//canvas.restore();
//    		canvas.save();
    		if (splitColor)
    		{
    			fillColorPaint.setColor(secondFillColor);
    			canvas.drawRoundRect(secondFillBackgroundColorRect, radiusFillColorRect, radiusFillColorRect, fillColorPaint);
    		}
//    		canvas.restore();
        }
        canvas.restore();

        if (mThumb != null) {
//        	Rect thumbRect = getThumbRect();
//        	Paint rectPainter = new Paint();
//        	Log.d("Slider","Rect : " + thumbRect.toString());
//        	rectPainter.setColor(Color.RED);
//    		canvas.drawRect(thumbRect, rectPainter);
        	canvas.save();
            // Translate the padding. For the y, we need to allow the thumb to
            // draw in its extra space
        	if (isVertical)
        		canvas.translate(mPaddingLeft, mPaddingTop - mThumbOffset);
        	else
        		canvas.translate(mPaddingLeft - mThumbOffset, mPaddingTop);
            mThumb.draw(canvas);
            canvas.restore();
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        if (!this.mDetector.onTouchEvent(event)) {

	        switch (event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	            case MotionEvent.ACTION_MOVE:
	        		trackTouchEvent(event);
	                break;
	        }
        }

       return true;
    }

	public void setPreProgress (int progress)
	{
		if (progress > maxProgress)
			progress = maxProgress;
		if (progress < minProgress)
			progress = minProgress;
		if (mOnSliderChangeListener != null)
		{
			mOnSliderChangeListener.onSliderProgressWillChange(this, (double) progress / (getMaxProgress() - getMinProgress()));
			return;
		}
		else
		{
			setProgress(progress);
		}
	}



	public int getMaxProgress() {
		return maxProgress;
	}

	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
	}

	public int getMinProgress() {
		return minProgress;
	}

	public void setMinProgress(int minProgress) {
		this.minProgress = minProgress;
	}

	public int getMidProgress() {
		return midProgress;
	}

	public void goToMinProgress()
	{
		setPreProgress(minProgress);
	}

	public void goToMaxProgress()
	{
		setPreProgress(maxProgress);
	}

	public void goToMidProgress()
	{
		setPreProgress(midProgress);
	}


	public void setProgress(double _progress) {
		int progress = (int) (maxProgress * _progress);
		synchronized(this)
		{
			this.progress = progress;
		}
        float scale = maxProgress > 0 ? (float) (progress - minProgress) / (float) (maxProgress - minProgress) : 0;
        if (mThumb != null) {
        	int availableSize = 0;
        	if (isVertical)
        		availableSize = getHeight() - mPaddingTop - mPaddingBottom;
        	else
        		availableSize = getWidth() - mPaddingLeft - mPaddingRight;

            setThumbPos(availableSize, mThumb, scale, Integer.MIN_VALUE);
            /*
             * Since we draw translated, the drawable's bounds that it signals
             * for invalidation won't be the actual bounds we want invalidated,
             * so just invalidate this whole view.
             *
             */
            if (mBackgroundSlider != null)
            	updateFillColorBackground(fillColorGravity);
            ((Activity) getContext()).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					invalidate();
				}
			});
        }
	}

	public synchronized int getProgress()
	{
		return progress;
	}

	public void setPlayerIdx(int playerIdx)
	{
		fillColorPaint.setColor(fillColor);
	}

	private void updateFillColorBackground(int gravity)
	{
		if (mThumb == null || mBackgroundSlider == null)
			return;
    	int centerThumb = 0;
    	int centerBackground = 0;
    	if (isVertical)
    	{
    		centerBackground = (mBackgroundSlider.getBounds().bottom) >> 1;
    		centerThumb = mThumb.getBounds().centerY() - mThumbOffset;
    		fillBackgroundColorRect.left = (int)( mBackgroundSlider.getBounds().left + marginFillBgColor);
    		fillBackgroundColorRect.right = (int) (mBackgroundSlider.getBounds().right - marginFillBgColor);

    		switch(gravity)
    		{
    		case Gravity.TOP:
        		fillBackgroundColorRect.top = (int) ( mBackgroundSlider.getBounds().top + marginFillBgColor);
				fillBackgroundColorRect.bottom = centerThumb;
    			break;
    		case Gravity.BOTTOM:
        		fillBackgroundColorRect.top = centerThumb;
        		fillBackgroundColorRect.bottom = (int) (mBackgroundSlider.getBounds().bottom - marginFillBgColor);
    			break;
    		case Gravity.CENTER:
			default:
				if (centerBackground < centerThumb)
				{
					fillBackgroundColorRect.top = centerBackground;
					fillBackgroundColorRect.bottom = centerThumb;
				}
				else
				{
					fillBackgroundColorRect.top = centerThumb;
					fillBackgroundColorRect.bottom = centerBackground;
				}
				break;
    		}
    	}
    	else
    	{
    		centerBackground = (mBackgroundSlider.getBounds().right) >> 1;
    		centerThumb = mThumb.getBounds().centerX() - mThumbOffset;
    		fillBackgroundColorRect.top = (int) ( mBackgroundSlider.getBounds().top + marginFillBgColor);
    		fillBackgroundColorRect.bottom = (int) (mBackgroundSlider.getBounds().bottom - marginFillBgColor);
    		switch(gravity)
    		{
    		case Gravity.LEFT:
        		fillBackgroundColorRect.left = (int)( mBackgroundSlider.getBounds().left + marginFillBgColor);
        		fillBackgroundColorRect.right = centerThumb;
    			break;
    		case Gravity.RIGHT:
        		fillBackgroundColorRect.left = centerThumb;
        		fillBackgroundColorRect.right = (int) (mBackgroundSlider.getBounds().right - marginFillBgColor);
    			break;
    		case Gravity.CENTER:
    		default:
				if (centerBackground < centerThumb)
				{
					fillBackgroundColorRect.left = centerBackground;
					fillBackgroundColorRect.right = centerThumb;
				}
				else
				{
					fillBackgroundColorRect.left = centerThumb;
					fillBackgroundColorRect.right = centerBackground;
				}
    			break;
    		}
    	}

    	if (!splitColor)
    		return;

    	if (isVertical)
    	{
    		secondFillBackgroundColorRect.set(fillBackgroundColorRect.left, centerThumb, fillBackgroundColorRect.right, (int) (mBackgroundSlider.getBounds().bottom - marginFillBgColor));
    	}
    	else
    	{
    		secondFillBackgroundColorRect.set(centerThumb, fillBackgroundColorRect.top, (int) (mBackgroundSlider.getBounds().right - marginFillBgColor), fillBackgroundColorRect.bottom);
    	}


	}

    private void trackTouchEvent(MotionEvent event) {
        int paddingStart;
        int paddingEnd;
        int coordinate;
        int size;
        if (isVertical)
        {
        	paddingStart = mPaddingTop;
        	paddingEnd = mPaddingBottom;
        	coordinate = (int) event.getY();
        	size = getHeight();
        }
        else
        {
        	paddingStart = mPaddingRight;
        	paddingEnd = mPaddingLeft;
        	size = getWidth();
        	coordinate = (int) (size - event.getX());
        }
        final int availableSize = size - paddingStart - paddingEnd;
        float scale;
        float progress = 0;
        if (coordinate > size - paddingEnd) {
            scale = 0.0f;
        } else if (coordinate < paddingStart) {
            scale = 1.0f;
        } else {
            scale = (float)(availableSize - coordinate + paddingStart) / (float)availableSize;
        }
        progress += scale * maxProgress;
        setPreProgress((int) progress);
    }

	public void lockUserAction(boolean lock) {
		setEnabled(!lock);
	}

	public void hideThumb(boolean hide) {
		/*if (hide == hidden)
			return;

		if (hide) {
			mThumbHidden = mThumb;
			setThumb(null);
		}*/
		//hidden = hide;
		setVisibility(hide ? View.GONE : View.VISIBLE);
		/*if (!hidden) {
			setThumb(mThumbHidden);
			mThumbHidden = null;
		}*/
	}
}
