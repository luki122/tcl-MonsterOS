package mst.widget;

import java.util.List;

import mst.utils.AnimationUtils;
import mst.utils.ViewUtil;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.mst.R;

@CoordinatorLayout.DefaultBehavior(FloatingActionButton.Behavior.class)
public class FloatingActionButton extends View {

	

	private static final int SIZE_LARGE = 0;

	private static final int SIZE_NORMAL = 1;

	private static final int SIZE_SMALL = 2;
	
	private static final int SIZE_NEGATIVE = -1;
	
	static final int SHOW_HIDE_ANIM_DURATION = 200;

	/**
	 * listener for main button clicked
	 */
	private OnFloatActionButtonClickListener mClickListener;

	private Context mContext;

	private Resources mRes;

	private Drawable mCenterDrawable;

	private Paint mPaint;
	
	private StateListAnimator mStateListAnimation;

	private int mWidth, mHeight;
	
	private boolean mIsHiding;
	
	private boolean mHasCustomSize = false;
	
    interface InternalVisibilityChangedListener {
        public void onShown();
        public void onHidden();
    }
	
	private final ViewOutlineProvider OVAL_OUTLINE_PROVIDER = new ViewOutlineProvider() {
		@Override
		public void getOutline(View view, Outline outline) {
			outline.setOval(0, 0, view.getWidth(), view.getHeight());
		}
	};

	/**
	 * listener for float action button clicked
	 * 
	 * @author alexluo
	 *
	 */
	public interface OnFloatActionButtonClickListener {
		/**
		 * if you want to do something when main button clicked, such as:show a
		 * dialog,or start an activity,you should implement this method
		 */
		void onClick(View view);
	}

	public FloatingActionButton(Context context) {
		this(context, null);
	}

	public FloatingActionButton(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.floatActionButtonStyle);
	}

	public FloatingActionButton(Context context, AttributeSet attrs,
			int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public FloatingActionButton(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr);
	}

	private void init(Context context, AttributeSet attributeSet, int defStyle) {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);

		mContext = context;
		mRes = context.getResources();
		TypedArray attr = context.obtainStyledAttributes(attributeSet,
				R.styleable.FloatingActionButton, defStyle, 0);

		int buttonSize = attr.getLayoutDimension(R.styleable.FloatingActionButton_size,
				"size");
		mCenterDrawable = attr
				.getDrawable(R.styleable.FloatingActionButton_centerImage);
		attr.recycle();
		switch (buttonSize) {
		case SIZE_LARGE:
			buttonSize = R.dimen.floating_button_size_large;
			break;
		case SIZE_NORMAL:
			buttonSize = R.dimen.floating_button_size_normal;
			break;
		case SIZE_SMALL:
			buttonSize = R.dimen.floating_button_size_small;
			break;
			default:{
				mHasCustomSize = true;
				mWidth = mHeight = buttonSize;
			}

		}
		if(!mHasCustomSize){
			mWidth = mHeight = mRes.getDimensionPixelOffset(buttonSize);
		}
		
		setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (mClickListener != null) {
					mClickListener.onClick(v);

				}
			}
		});
		mStateListAnimation =  AnimatorInflater.loadStateListAnimator(
				mContext, R.anim.floating_action_button_state_list_anim);
		setStateListAnimator(mStateListAnimation);
	}

	/**
	 * clip this view to cycle by set outline
	 */
	private void clip() {
        setOutlineProvider(OVAL_OUTLINE_PROVIDER);
		setClipToOutline(true);
	}

	/**
	 * set click listener to float button
	 * 
	 * @param listener
	 */
	public void setOnFloatingActionButtonClickListener(
			OnFloatActionButtonClickListener listener) {
		mClickListener = listener;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (mCenterDrawable != null) {
			final int width = mCenterDrawable.getIntrinsicWidth();
			final int height = mCenterDrawable.getIntrinsicHeight();
			final int parentWidth = getMeasuredWidth();
			final int parentHeight = getMeasuredHeight();

			final int left = parentWidth / 2 - width / 2;
			final int top = parentHeight / 2 - height / 2;
			mCenterDrawable.setBounds(left, top, left + width, top + height);
			mCenterDrawable.draw(canvas);
		}
	}

	/**
	 * update current icon drawable
	 * 
	 * @param icon
	 */
	public void setIconDrawable(Drawable icon) {
		mCenterDrawable = icon;
		invalidate();
	}

	/**
	 * Get current  center icon drawable
	 * @return
	 */
	public Drawable getIconDrawable(){
		return mCenterDrawable;
	}
	
	
	/**
	 * play animation when click float button
	 */
	public void playTranslateAnimation() {

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		setMeasuredDimension(mWidth, mHeight);
		clip();
	}
	
    void hide( final InternalVisibilityChangedListener listener, final boolean fromUser) {
        if (mIsHiding || getVisibility() != View.VISIBLE) {
            // A hide animation is in progress, or we're already hidden. Skip the call
            if (listener != null) {
                listener.onHidden();
            }
            return;
        }

        if (!isLaidOut() || isInEditMode()) {
            // If the view isn't laid out, or we're in the editor, don't run the animation
            setVisibility(View.GONE);
            if (listener != null) {
                listener.onHidden();
            }
        } else {
            animate().cancel();
            animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(SHOW_HIDE_ANIM_DURATION)
                    .setInterpolator(AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
                    .setListener(new AnimatorListenerAdapter() {
                        private boolean mCancelled;

                        @Override
                        public void onAnimationStart(Animator animation) {
                            mIsHiding = true;
                            mCancelled = false;
                            setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            mIsHiding = false;
                            mCancelled = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mIsHiding = false;
                            if (!mCancelled) {
                                setVisibility(View.GONE);
                                if (listener != null) {
                                    listener.onHidden();
                                }
                            }
                        }
                    });
        }
    }

    void show( final InternalVisibilityChangedListener listener, final boolean fromUser) {
        if (mIsHiding || getVisibility() != View.VISIBLE) {
            if (isLaidOut() && !isInEditMode()) {
                animate().cancel();
                if (getVisibility() != View.VISIBLE) {
                    // If the view isn't visible currently, we'll animate it from a single pixel
                    setAlpha(0f);
                    setScaleY(0f);
                    setScaleX(0f);
                }
                animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(SHOW_HIDE_ANIM_DURATION)
                        .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (listener != null) {
                                    listener.onShown();
                                }
                            }
                        });
            } else {
                setVisibility(View.VISIBLE);
                setAlpha(1f);
                setScaleY(1f);
                setScaleX(1f);
                if (listener != null) {
                    listener.onShown();
                }
            }
        }
    }
	

    /**
     * Behavior designed for use with {@link FloatingActionButton} instances. It's main function
     * is to move {@link FloatingActionButton} views so that any displayed {@link Snackbar}s do
     * not cover them.
     */
    public static class Behavior extends CoordinatorLayout.Behavior<FloatingActionButton> {
        // We only support the FAB <> Snackbar shift movement on Honeycomb and above. This is
        // because we can use view translation properties which greatly simplifies the code.
        private static final boolean SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;

        private ValueAnimator mFabTranslationYAnimator;
        private float mFabTranslationY;
        private Rect mTmpRect;

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent,
                FloatingActionButton child, View dependency) {
            // We're dependent on all SnackbarLayouts (if enabled)
            return SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child,
                View dependency) {
            if (dependency instanceof Snackbar.SnackbarLayout) {
                updateFabTranslationForSnackbar(parent, child, dependency);
            } else if (dependency instanceof AppBarLayout) {
                // If we're depending on an AppBarLayout we will show/hide it automatically
                // if the FAB is anchored to the AppBarLayout
                updateFabVisibility(parent, (AppBarLayout) dependency, child);
            }
            return false;
        }

        private boolean updateFabVisibility(CoordinatorLayout parent,
                AppBarLayout appBarLayout, FloatingActionButton child) {
            final CoordinatorLayout.LayoutParams lp =
                    (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            if (lp.getAnchorId() != appBarLayout.getId()) {
                // The anchor ID doesn't match the dependency, so we won't automatically
                // show/hide the FAB
                return false;
            }

            if (child.getVisibility() != VISIBLE) {
                // The view isn't set to be visible so skip changing it's visibility
                return false;
            }

            if (mTmpRect == null) {
                mTmpRect = new Rect();
            }

            // First, let's get the visible rect of the dependency
            final Rect rect = mTmpRect;
            parent.getDescendantRect(appBarLayout, rect);

            if (rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
                // If the anchor's bottom is below the seam, we'll animate our FAB out
                child.hide(null, false);
            } else {
                // Else, we'll animate our FAB back in
                child.show(null, false);
            }
            return true;
        }

        private void updateFabTranslationForSnackbar(CoordinatorLayout parent,
                final FloatingActionButton fab, View snackbar) {
            if (fab.getVisibility() != View.VISIBLE) {
                return;
            }

            final float targetTransY = getFabTranslationYForSnackbar(parent, fab);
            if (mFabTranslationY == targetTransY) {
                // We're already at (or currently animating to) the target value, return...
                return;
            }

            final float currentTransY = fab.getTranslationY();

            // Make sure that any current animation is cancelled
            if (mFabTranslationYAnimator != null && mFabTranslationYAnimator.isRunning()) {
                mFabTranslationYAnimator.cancel();
            }

            if (Math.abs(currentTransY - targetTransY) > (fab.getHeight() * 0.667f)) {
                // If the FAB will be travelling by more than 2/3 of it's height, let's animate
                // it instead
                if (mFabTranslationYAnimator == null) {
                    mFabTranslationYAnimator = ViewUtil.createAnimator();
                    mFabTranslationYAnimator.setInterpolator(
                            AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                    mFabTranslationYAnimator.addUpdateListener(
                            new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animator) {
                                	fab.setTranslationY(
                                            (float)animator.getAnimatedValue());
                                }
                            });
                }
                mFabTranslationYAnimator.setFloatValues(currentTransY, targetTransY);
                mFabTranslationYAnimator.start();
            } else {
                // Now update the translation Y
            	fab.setTranslationY( targetTransY);
            }

            mFabTranslationY = targetTransY;
        }

        private float getFabTranslationYForSnackbar(CoordinatorLayout parent,
                FloatingActionButton fab) {
            float minOffset = 0;
            final List<View> dependencies = parent.getDependencies(fab);
            for (int i = 0, z = dependencies.size(); i < z; i++) {
                final View view = dependencies.get(i);
                if (view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                    minOffset = Math.min(minOffset,
                    		view.getTranslationY() - view.getHeight());
                }
            }

            return minOffset;
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, FloatingActionButton child,
                int layoutDirection) {
            // First, lets make sure that the visibility of the FAB is consistent
            final List<View> dependencies = parent.getDependencies(child);
            for (int i = 0, count = dependencies.size(); i < count; i++) {
                final View dependency = dependencies.get(i);
                if (dependency instanceof AppBarLayout
                        && updateFabVisibility(parent, (AppBarLayout) dependency, child)) {
                    break;
                }
            }
            // Now let the CoordinatorLayout lay out the FAB
            parent.onLayoutChild(child, layoutDirection);
            // Now offset it if needed
            return true;
        }

    }

}
