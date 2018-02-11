package cn.tcl.music.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.SectionIndexer;
import cn.tcl.music.R;
import cn.tcl.music.view.*;

public class IndexableRecyclerView extends cn.tcl.music.view.ContextMenuReyclerView {

    private IndexScroller mScroller = null;
    private GestureDetector mGestureDetector = null;
    private boolean isMoveToTop = false;
    private int mCurrentPosition = 0;

    public IndexableRecyclerView(Context context) {
        this(context, null);
    }

    public IndexableRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndexableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScroller = new IndexScroller(getContext(), this);
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener());
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isMoveToTop) {
                    isMoveToTop = false;
                    LinearLayoutManager llm = (LinearLayoutManager) recyclerView
                            .getLayoutManager();
                    int n = mCurrentPosition - llm.findFirstVisibleItemPosition();
                    if (0 <= n && n < recyclerView.getChildCount()) {
                        int top = recyclerView.getChildAt(n).getTop();
                        recyclerView.scrollBy(0, top);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_DRAGGING) {
                    mScroller.mCurrentSection = -1;
                }
            }
        });
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);
        if (mScroller != null) {
            mScroller.draw(c);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mScroller != null && mScroller.onTouchEvent(ev)) {
            return true;
        }
        mGestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mScroller.contains(ev.getX(), ev.getY()))
            return true;
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (mScroller != null)
            mScroller.setAdapter(adapter);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mScroller != null)
            mScroller.onSizeChanged(w, h, oldw, oldh);
    }

    private class IndexScroller {

        private float mIndexbarWidth;
        private float mIndexbarHeight;
        private float mIndexbarTopMargin;
        private float mIndexbarRightMargin;
        private float mTextSize;
        private int mCurrentSection = -1;
        private boolean mIsIndexing = false;
        private RecyclerView mRecyclerView = null;
        private SectionIndexer mIndexer = null;
        private String[] mSections = null;
        private RectF mIndexbarRect;

        public IndexScroller(Context context, RecyclerView recyclerView) {
            Resources res = context.getResources();
            mRecyclerView = recyclerView;
            setAdapter(mRecyclerView.getAdapter());
            mIndexbarWidth = res.getDimension(R.dimen.dp_20);
            mIndexbarRightMargin = res.getDimension(R.dimen.dp_5);
            mIndexbarHeight = res.getDimension(R.dimen.dp_351);
            mIndexbarTopMargin = res.getDimension(R.dimen.dp_35);
            mTextSize = res.getDimension(R.dimen.sp_8);
        }

        public void draw(Canvas canvas) {
            Paint indexbarPaint = new Paint();
            indexbarPaint.setColor(Color.BLACK);
            indexbarPaint.setAlpha(0);
            indexbarPaint.setAntiAlias(true);
            canvas.drawRoundRect(mIndexbarRect, mIndexbarRightMargin, mIndexbarRightMargin, indexbarPaint);
            if (mSections != null && mSections.length > 0) {
                Paint indexPaint = new Paint();
                indexPaint.setAntiAlias(true);
                indexPaint.setTextSize(mTextSize);
                float sectionHeight = mIndexbarRect.height() / mSections.length;
                float paddingTop = (sectionHeight - mTextSize) / 2;
                for (int i = 0; i < mSections.length; i++) {
                    indexPaint.setColor(Color.BLACK);
                    indexPaint.setTypeface(Typeface.SANS_SERIF);
                    indexPaint.setAlpha(25);
                    if (i == mCurrentSection) {
                        indexPaint.setAlpha(86);
                    }
                    float paddingLeft = (mIndexbarWidth - indexPaint.measureText(mSections[i])) / 2;
                    canvas.drawText(mSections[i], mIndexbarRect.left + paddingLeft
                            , mIndexbarRect.top + sectionHeight * i + paddingTop - indexPaint.ascent(), indexPaint);
                }
            }
        }

        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (contains(ev.getX(), ev.getY())) {
                        mIsIndexing = true;
                        mCurrentSection = getSectionByPoint(ev.getY());
                        mRecyclerView.invalidate();
                        moveToPosition(mIndexer.getPositionForSection(mCurrentSection));
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mIsIndexing) {
                        if (contains(ev.getX(), ev.getY())) {
                            mCurrentSection = getSectionByPoint(ev.getY());
                            mRecyclerView.invalidate();
                            moveToPosition(mIndexer.getPositionForSection(mCurrentSection));
                        }
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mIsIndexing) {
                        mIsIndexing = false;
                        if (mCurrentPosition == -1) {
                            mCurrentSection = -1;
                        } else {
                            mCurrentSection = getSectionByPoint(ev.getY());
                            mRecyclerView.invalidate();
                        }
                    }
                    break;
            }
            return false;
        }

        private void moveToPosition(int position) {
            mCurrentPosition = position;
            if (position > -1) {
                LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                int firstItem = llm.findFirstVisibleItemPosition();
                int lastItem = llm.findLastVisibleItemPosition();
                if (position <= firstItem) {
                    mRecyclerView.scrollToPosition(position);
                } else if (position <= lastItem) {
                    int top = mRecyclerView.getChildAt(position - firstItem).getTop();
                    mRecyclerView.scrollBy(0, top);
                } else {
                    mRecyclerView.scrollToPosition(position);
                    isMoveToTop = true;
                }
            } else {
                mCurrentSection = -1;
            }
        }

        public void onSizeChanged(int w, int h, int oldw, int oldh) {
            mCurrentSection = -1;
            mIndexbarRect = new RectF(w - mIndexbarWidth, mIndexbarTopMargin, w, mIndexbarHeight + mIndexbarTopMargin);
        }

        public void setAdapter(Adapter adapter) {
            if (adapter instanceof SectionIndexer) {
                mIndexer = (SectionIndexer) adapter;
                mSections = (String[]) mIndexer.getSections();
            }
        }

        public boolean contains(float x, float y) {
            if (mIsIndexing) {
                return true;
            }
            return (x >= mIndexbarRect.left && y >= mIndexbarRect.top && y <= mIndexbarRect.top + mIndexbarRect.height());
        }

        private int getSectionByPoint(float y) {
            if (mSections == null || mSections.length == 0)
                return 0;
            if (y < mIndexbarRect.top + mIndexbarRightMargin)
                return 0;
            if (y >= mIndexbarRect.top + mIndexbarRect.height() - mIndexbarRightMargin)
                return mSections.length - 1;
            return (int) ((y - mIndexbarRect.top - mIndexbarRightMargin) /
                    ((mIndexbarRect.height() - 2 * mIndexbarRightMargin) / mSections.length));
        }
    }
}

