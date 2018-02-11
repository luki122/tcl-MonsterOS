package com.monster.market.views;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monster.market.R;


public class ExpandableTextView extends LinearLayout implements View.OnClickListener {

	private static final String TAG = ExpandableTextView.class.getSimpleName();

	/* The default number of lines */
	private static final int MAX_COLLAPSED_LINES = 8;

	/* The default animation duration */
	private static final int DEFAULT_ANIM_DURATION = 300;

	/* The default alpha value when the animation starts */
	private static final float DEFAULT_ANIM_ALPHA_START = 0.7f;

	/* 显示内容的TextView */
	protected TextView mTv;

	/* 箭头按钮ImageButton */
	protected ImageButton mButton;

	/* 是否需要重新布局。当调用了setText()方法后，该值置为true */
	private boolean mRelayout;

	/* 默认TextView处于收起状态 */
	private boolean mCollapsed = true;

	/* 收起状态下的整个View的高度 */
	private int mCollapsedHeight;

	/* TextView整个文本的高度 */
	private int mTextHeightWithMaxLines;

	/* 收起状态下的最大显示行数 */
	private int mMaxCollapsedLines;

	/* TextView的bottomMargin */
	private int mMarginBetweenTxtAndBottom;

	/* 箭头按钮的展开图标 */
	private Drawable mExpandDrawable;

	/* 箭头按钮的收起图标 */
	private Drawable mCollapseDrawable;

	/* 动画执行时长 */
	private int mAnimationDuration;

	/* 动画启动时内容的透明度 */
	private float mAnimAlphaStart;

	/* 是否正在执行动画 */
	private boolean mAnimating;

	/* 状态改变监听 */
	private OnExpandStateChangeListener mListener;

	/* 如果是在ListView中，需要使用到mCollapsedStatus和mPosition，保存当前position的展开或收起状态 */
	private SparseBooleanArray mCollapsedStatus;
	private int mPosition;

	public ExpandableTextView(Context context) {
		this(context, null);
	}

	public ExpandableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public ExpandableTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	@Override
	public void setOrientation(int orientation){
		if(LinearLayout.HORIZONTAL == orientation){
			throw new IllegalArgumentException("ExpandableTextView only supports Vertical Orientation.");
		}
		super.setOrientation(orientation);
	}

	@Override
	public void onClick(View view) {
		if (mButton.getVisibility() != View.VISIBLE || mAnimating) {
			return;
		}

		mCollapsed = !mCollapsed;
		mButton.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);

		if (mCollapsedStatus != null) {
			mCollapsedStatus.put(mPosition, mCollapsed);
		}

		// mark that the animation is in progress
		mAnimating = true;

		Animation animation;
		if (mCollapsed) {
			animation = new ExpandCollapseAnimation(this, getHeight(), mCollapsedHeight);
		} else {
			animation = new ExpandCollapseAnimation(this, getHeight(), getHeight() +
					mTextHeightWithMaxLines - mTv.getHeight());
		}

		animation.setFillAfter(true);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				applyAlphaAnimation(mTv, mAnimAlphaStart);
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				// clear animation here to avoid repeated applyTransformation() calls
				clearAnimation();
				// clear the animation flag
				mAnimating = false;

				// notify the listener
				if (mListener != null) {
					mListener.onExpandStateChanged(mTv, !mCollapsed);
				}
			}
			@Override
			public void onAnimationRepeat(Animation animation) { }
		});

		clearAnimation();
		startAnimation(animation);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// while an animation is in progress, intercept all the touch events to children to
		// prevent extra clicks during the animation
		return mAnimating;
	}

	@Override
	protected void onFinishInflate() {
		//当xml布局文件加载完成之后，执行onFinishInflate()方法，在这里完成View的初始化。
		findViews();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 如果没有改变显示内容，或者显示内容为空，执行super.onMeasure()并返回
		if (!mRelayout || getVisibility() == View.GONE) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		mRelayout = false;

		// Setup with optimistic case
		// i.e. Everything fits. No button needed
		mButton.setVisibility(View.GONE);
		mTv.setMaxLines(Integer.MAX_VALUE);

		// Measure
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// If the text fits in collapsed mode, we are done.
		if (mTv.getLineCount() <= mMaxCollapsedLines) {
			return;
		}

		// Saves the text height w/ max lines
		mTextHeightWithMaxLines = getRealTextViewHeight(mTv);

		// Doesn't fit in collapsed mode. Collapse text view as needed. Show
		// button.
		if (mCollapsed) {
			mTv.setMaxLines(mMaxCollapsedLines);
		}
		mButton.setVisibility(View.VISIBLE);

		// Re-measure with new setup
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (mCollapsed) {
			// Gets the margin between the TextView's bottom and the ViewGroup's bottom
			mTv.post(new Runnable() {
				@Override
				public void run() {
					mMarginBetweenTxtAndBottom = getHeight() - mTv.getHeight();
				}
			});
			// Saves the collapsed height of this ViewGroup
			mCollapsedHeight = getMeasuredHeight();
		}
	}

	public void setOnExpandStateChangeListener(@Nullable OnExpandStateChangeListener listener) {
		mListener = listener;
	}

	public void setText(@Nullable CharSequence text) {
		mRelayout = true;
		mTv.setText(text);
		setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
	}

	public void setText(@Nullable CharSequence text, @NonNull SparseBooleanArray collapsedStatus, int position) {
		mCollapsedStatus = collapsedStatus;
		mPosition = position;
		boolean isCollapsed = collapsedStatus.get(position, true);
		clearAnimation();
		mCollapsed = isCollapsed;
		mButton.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);
		setText(text);
		getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
		requestLayout();
	}

	@Nullable
	public CharSequence getText() {
		if (mTv == null) {
			return "";
		}
		return mTv.getText();
	}

	private void init(AttributeSet attrs) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
		mMaxCollapsedLines = typedArray.getInt(R.styleable.ExpandableTextView_maxCollapsedLines, MAX_COLLAPSED_LINES);
		mAnimationDuration = typedArray.getInt(R.styleable.ExpandableTextView_animDuration, DEFAULT_ANIM_DURATION);
		mAnimAlphaStart = typedArray.getFloat(R.styleable.ExpandableTextView_animAlphaStart, DEFAULT_ANIM_ALPHA_START);
		mExpandDrawable = typedArray.getDrawable(R.styleable.ExpandableTextView_expandDrawable);
		mCollapseDrawable = typedArray.getDrawable(R.styleable.ExpandableTextView_collapseDrawable);

		if (mExpandDrawable == null) {
			mExpandDrawable = getDrawable(getContext(), R.drawable.page_content_arrow_down);
		}
		if (mCollapseDrawable == null) {
			mCollapseDrawable = getDrawable(getContext(), R.drawable.page_content_arrow_up);
		}

		typedArray.recycle();

		// 强制把方向设置为垂直
		setOrientation(LinearLayout.VERTICAL);

		// 默认设置为gone
		setVisibility(GONE);
	}

	private void findViews() {
		mTv = (TextView) findViewById(R.id.expandable_text);
		mButton = (ImageButton) findViewById(R.id.expand_collapse);
		mButton.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);
		setOnClickListener(this);
	}

	private static boolean isPostHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	private static boolean isPostLolipop() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void applyAlphaAnimation(View view, float alpha) {
		if (isPostHoneycomb()) {
			view.setAlpha(alpha);
		} else {
			AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, alpha);
			// make it instant
			alphaAnimation.setDuration(0);
			alphaAnimation.setFillAfter(true);
			view.startAnimation(alphaAnimation);
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static Drawable getDrawable(@NonNull Context context, @DrawableRes int resId) {
		Resources resources = context.getResources();
		if (isPostLolipop()) {
			return resources.getDrawable(resId, context.getTheme());
		} else {
			return resources.getDrawable(resId);
		}
	}

	private static int getRealTextViewHeight(@NonNull TextView textView) {
		int textHeight = textView.getLayout().getLineTop(textView.getLineCount());
		int padding = textView.getCompoundPaddingTop() + textView.getCompoundPaddingBottom();
		return textHeight + padding;
	}

	class ExpandCollapseAnimation extends Animation {
		private final View mTargetView;
		private final int mStartHeight;
		private final int mEndHeight;

		public ExpandCollapseAnimation(View view, int startHeight, int endHeight) {
			mTargetView = view;
			mStartHeight = startHeight;
			mEndHeight = endHeight;
			setDuration(mAnimationDuration);
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			final int newHeight = (int)((mEndHeight - mStartHeight) * interpolatedTime + mStartHeight);
			mTv.setMaxHeight(newHeight - mMarginBetweenTxtAndBottom);
			if (Float.compare(mAnimAlphaStart, 1.0f) != 0) {
				applyAlphaAnimation(mTv, mAnimAlphaStart + interpolatedTime * (1.0f - mAnimAlphaStart));
			}
			mTargetView.getLayoutParams().height = newHeight;
			mTargetView.requestLayout();
		}

		@Override
		public void initialize( int width, int height, int parentWidth, int parentHeight ) {
			super.initialize(width, height, parentWidth, parentHeight);
		}

		@Override
		public boolean willChangeBounds( ) {
			return true;
		}
	}

	public interface OnExpandStateChangeListener {
		/**
		 * Called when the expand/collapse animation has been finished
		 *
		 * @param textView - TextView being expanded/collapsed
		 * @param isExpanded - true if the TextView has been expanded
		 */
		void onExpandStateChanged(TextView textView, boolean isExpanded);
	}
}