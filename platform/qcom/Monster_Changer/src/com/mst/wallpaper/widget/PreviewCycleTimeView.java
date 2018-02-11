package com.mst.wallpaper.widget;

import mst.widget.ViewPager;
import mst.widget.ViewPager.OnPageChangeListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;
import com.mst.wallpaper.R;
public class PreviewCycleTimeView extends  View implements PageIndicator ,OnPageChangeListener{

    private static final int INVALID_POINTER = -1;
    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mListener;
    private int mCurrentPage;
    private float mPageOffset;
    private int mScrollState;
    private int mOrientation;
    private boolean mCentered;
    private int mTouchSlop;
    private float mLastMotionX = -1;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsDragging;
    private Bitmap mPointBitmap;
    private Bitmap mDialBitmap;
    private Matrix matrix;
    private static final float DEFAULT_ROTATE = 180.0f; //默认六点
    private static final float PAGE_ROTATE = 60.0f; //两小时,六十度
    private float mRotate = DEFAULT_ROTATE;

    public PreviewCycleTimeView(Context context) {
        this(context, null);
    }

    public PreviewCycleTimeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewCycleTimeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Load defaults from resources
        final Resources res = getResources();
        final int defaultOrientation = res.getInteger(R.integer.default_circle_indicator_orientation);
        final boolean defaultCentered = res.getBoolean(R.bool.default_circle_indicator_centered);

        mCentered = defaultCentered;
        mOrientation = defaultOrientation;
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledPagingTouchSlop();
        mPointBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.clock_pointer);
        mDialBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.clock_dial);
        matrix = new Matrix();

    }

    public void setCentered(boolean centered) {
        mCentered = centered;
        invalidate();
    }

    public boolean isCentered() {
        return mCentered;
    }

    public void setOrientation(int orientation) {
        switch (orientation) {
            case HORIZONTAL:
            case VERTICAL:
                mOrientation = orientation;
                requestLayout();
                break;

            default:
                throw new IllegalArgumentException("Orientation must be either HORIZONTAL or VERTICAL.");
        }
    }

    public int getOrientation() {
        return mOrientation;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewPager == null) {
            return;
        }
        final int count = mViewPager.getAdapter().getCount();

        if (mCurrentPage >= count) {
            setCurrentItem(count - 1);
            return;
        }

        matrix.reset();

        float tempRotate = mCurrentPage * PAGE_ROTATE + DEFAULT_ROTATE;
        tempRotate += mPageOffset * PAGE_ROTATE;
//        if (tempRotate > 360) {
//            mRotate = tempRotate % 360;
//        } else {
//            mRotate = tempRotate;
//        }
        mRotate = tempRotate;

        canvas.drawBitmap(mDialBitmap, 0, 0, null);
        matrix.preRotate(mRotate, mPointBitmap.getWidth() / 2, mPointBitmap.getHeight() / 2);
        canvas.drawBitmap(mPointBitmap, matrix, null);

    }

    public boolean onTouchEvent(android.view.MotionEvent ev) {
        if (super.onTouchEvent(ev)) {
            return true;
        }
        if ((mViewPager == null) || (mViewPager.getAdapter().getCount() == 0)) {
            return false;
        }

        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId( 0);
                mLastMotionX = ev.getX();
                break;

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = ev.findPointerIndex( mActivePointerId);
                final float x = ev.getX( activePointerIndex);
                final float deltaX = x - mLastMotionX;

                if (!mIsDragging) {
                    if (Math.abs(deltaX) > mTouchSlop) {
                        mIsDragging = true;
                    }
                }

                if (mIsDragging) {
                    mLastMotionX = x;
                    if (mViewPager.isFakeDragging() || mViewPager.beginFakeDrag()) {
                        mViewPager.fakeDragBy(deltaX);
                    }
                }

                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!mIsDragging) {
                    final int count = mViewPager.getAdapter().getCount();
                    final int width = getWidth();
                    final float halfWidth = width / 2f;
                    final float sixthWidth = width / 6f;

                    if ((mCurrentPage > 0) && (ev.getX() < halfWidth - sixthWidth)) {
                        if (action != MotionEvent.ACTION_CANCEL) {
                            mViewPager.setCurrentItem(mCurrentPage - 1);
                        }
                        return true;
                    } else if ((mCurrentPage < count - 1) && (ev.getX() > halfWidth + sixthWidth)) {
                        if (action != MotionEvent.ACTION_CANCEL) {
                            mViewPager.setCurrentItem(mCurrentPage + 1);
                        }
                        return true;
                    }
                }

                mIsDragging = false;
                mActivePointerId = INVALID_POINTER;
                if (mViewPager.isFakeDragging())
                    mViewPager.endFakeDrag();
                break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mLastMotionX = ev.getX( index);
                mActivePointerId = ev.getPointerId( index);
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                final int pointerIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId( pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId( newPointerIndex);
                }
                mLastMotionX = ev.getX(
                		ev.findPointerIndex(mActivePointerId));
                break;
        }

        return true;
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        if (mViewPager != null) {
            mViewPager.setOnPageChangeListener(null);
        }
        if (view.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        mViewPager.setOnPageChangeListener(this);
        invalidate();
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mViewPager.setCurrentItem(item);
        mCurrentPage = item;
        invalidate();
    }

    @Override
    public void notifyDataSetChanged() {
        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mScrollState = state;

        if (mListener != null) {
            mListener.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mCurrentPage = position;
        mPageOffset = positionOffset;
        invalidate();

        if (mListener != null) {
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            mCurrentPage = position;
            invalidate();
        }

        if (mListener != null) {
            mListener.onPageSelected(position);
        }
    }

    @Override
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = ( SavedState ) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPage = savedState.currentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = mCurrentPage;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}