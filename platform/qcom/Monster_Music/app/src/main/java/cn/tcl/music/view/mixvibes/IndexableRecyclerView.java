package cn.tcl.music.view.mixvibes;

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

public class IndexableRecyclerView extends ContextMenuReyclerView {

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
                if(newState == SCROLL_STATE_DRAGGING){
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
        //如果mScroller自己来处理触摸事件，该方法返回true
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
        private float mIndexbarBottomMargin;
        private float mTextSize;
        private int mRecyclerViewWidth;//RecyclerView的宽度
        private int mRecyclerViewHeight;//RecyclerView的高度
        private int mCurrentSection = -1;
        private boolean mIsIndexing = false;//是否在索引条上
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
            mIndexbarBottomMargin = res.getDimension(R.dimen.dp_107);
            mIndexbarTopMargin = res.getDimension(R.dimen.dp_35);
            mTextSize = res.getDimension(R.dimen.sp_8);
//            mPreviewPadding = 5 * mDensity;
        }

        /**
         * 绘制索引条以及预览文本
         *
         * @param canvas Canvas
         */
        public void draw(Canvas canvas) {
            //绘制索引条背景
            Paint indexbarPaint = new Paint();
            indexbarPaint.setColor(Color.BLACK);
            indexbarPaint.setAlpha(0);
            indexbarPaint.setAntiAlias(true);
            canvas.drawRoundRect(mIndexbarRect, mIndexbarRightMargin, mIndexbarRightMargin, indexbarPaint);
            //右侧字母集合不能为空且集合个数必须大于0才绘制
            if (mSections != null && mSections.length > 0) {
                //只有在当前选中字母和手指按下未抬起的情况下才绘制预览背景以及文本
//                if (mCurrentSection >= 0 && mIsIndexing) {
//                    Paint previewPaint = new Paint();//绘制预览背景的画笔
//                    previewPaint.setColor(Color.BLACK);//设置颜色
//                    previewPaint.setAlpha(50);//设置透明度
//                    previewPaint.setAntiAlias(true);//抗锯齿
//                    previewPaint.setShadowLayer(3, 0, 0, Color.argb(64, 0, 0, 0));//设置阴影
//
//                    Paint previewTextPaint = new Paint();//绘制预览文本的画笔
//                    previewTextPaint.setColor(Color.WHITE);//设置颜色
//                    previewTextPaint.setAntiAlias(true);//抗锯齿
//                    previewTextPaint.setTextSize(50 * mScaledDensity);//设置文本大小
//
//                    //测量要预览的字符的宽度
//                    float previewTextWidth = previewTextPaint.measureText(mSections[mCurrentSection]);
//                    //预览的大小
//                    float previewSize = 2 * mPreviewPadding + previewTextPaint.descent() - previewTextPaint.ascent();
//                    //预览文本的背景区域
//                    RectF previewRect = new RectF((mRecyclerViewWidth - previewSize) / 2
//                            , (mRecyclerViewHeight - previewSize) / 2
//                            , (mRecyclerViewWidth - previewSize) / 2 + previewSize
//                            , (mRecyclerViewHeight - previewSize) / 2 + previewSize);
//                    //绘制预览文本的背景
//                    canvas.drawRoundRect(previewRect, 5 * mDensity, 5 * mDensity, previewPaint);
//                    //绘制预览文本
//                    canvas.drawText(mSections[mCurrentSection], previewRect.left + (previewSize - previewTextWidth) / 2 - 1
//                            , previewRect.top + mPreviewPadding - previewTextPaint.ascent() + 1, previewTextPaint);
//                }

                //绘制索引条上的每个字符
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
                    //绘制索引条上的字
                    canvas.drawText(mSections[i], mIndexbarRect.left + paddingLeft
                            , mIndexbarRect.top + sectionHeight * i + paddingTop - indexPaint.ascent(), indexPaint);
                }
            }
        }

        /**
         * 触摸事件
         *
         * @param ev
         * @return
         */
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (contains(ev.getX(), ev.getY())) {
                        //表示触摸事件发生在索引条上
                        mIsIndexing = true;
                        //根据当前的Y的值获取当前索引条上对应的索引
                        mCurrentSection = getSectionByPoint(ev.getY());
                        //重绘RecyclerView
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
                        if(mCurrentPosition == -1){
                            mCurrentSection = -1;
                        }else {
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

        /**
         * 当RecyclerView大小改变时，此方法是为了应对RecyclerView横竖屏切换
         *
         * @param w
         * @param h
         * @param oldw
         * @param oldh
         */
        public void onSizeChanged(int w, int h, int oldw, int oldh) {
            mRecyclerViewWidth = w;
            mRecyclerViewHeight = h;
            mCurrentSection = -1;
            mIndexbarRect = new RectF(w - mIndexbarWidth, mIndexbarTopMargin, w, mIndexbarHeight + mIndexbarTopMargin);
        }

        /**
         * 设置适配器
         *
         * @param adapter RecyclerView适配器
         */
        public void setAdapter(Adapter adapter) {
            if (adapter instanceof SectionIndexer) {
                mIndexer = (SectionIndexer) adapter;
                mSections = (String[]) mIndexer.getSections();
            }
        }

        /**
         * 是否触摸在索引条上
         *
         * @param x
         * @param y
         * @return
         */
        public boolean contains(float x, float y) {
            if (mIsIndexing) {
                return true;
            }
            return (x >= mIndexbarRect.left && y >= mIndexbarRect.top && y <= mIndexbarRect.top + mIndexbarRect.height());
        }

        /**
         * 获取当前触摸的是索引条上的哪一个字符
         *
         * @param y
         * @return
         */
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
