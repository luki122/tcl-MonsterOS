package mst.widget.recycleview;

import android.view.View;
import android.view.ViewParent;

/**
 * Helper class for implementing nested scrolling child views compatible with Android platform
 * versions earlier than Android 5.0 Lollipop (API 21).
 *
 * <p>{@link android.view.View View} subclasses should instantiate a final instance of this
 * class as a field at construction. For each <code>View</code> method that has a matching
 * method signature in this class, delegate the operation to the helper instance in an overriden
 * method implementation. This implements the standard framework policy for nested scrolling.</p>
 *
 * <p>Views invoking nested scrolling functionality should always do so from the relevant
 * {@link ViewCompat}, {@link ViewGroupCompat} or {@link ViewParentCompat} compatibility
 * shim static methods. This ensures interoperability with nested scrolling views on Android
 * 5.0 Lollipop and newer.</p>
 */
public class NestedScrollingChildHelper {
    private final View mView;
    private ViewParent mNestedScrollingParent;
    private boolean mIsNestedScrollingEnabled;
    private int[] mTempNestedScrollConsumed;

    /**
     * Construct a new helper for a given view.
     */
    public NestedScrollingChildHelper(View view) {
        mView = view;
    }

    /**
     * Enable nested scrolling.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link NestedScrollingChild} interface method with the same signature to implement
     * the standard policy.</p>
     *
     * @param enabled true to enable nested scrolling dispatch from this view, false otherwise
     */
    public void setNestedScrollingEnabled(boolean enabled) {
        if (mIsNestedScrollingEnabled) {
        	mView.stopNestedScroll();
        }
        mIsNestedScrollingEnabled = enabled;
    }

    /**
     * Check if nested scrolling is enabled for this view.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link NestedScrollingChild} interface method with the same signature to implement
     * the standard policy.</p>
     *
     * @return true if nested scrolling is enabled for this view
     */
    public boolean isNestedScrollingEnabled() {
        return mIsNestedScrollingEnabled;
    }

    /**
     * Check if this view has a nested scrolling parent view currently receiving events for
     * a nested scroll in progress.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link NestedScrollingChild} interface method with the same signature to implement
     * the standard policy.</p>
     *
     * @return true if this view has a nested scrolling parent, false otherwise
     */
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingParent != null;
    }

    /**
     * Start a new nested scroll for this view.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link NestedScrollingChild} interface method with the same signature to implement
     * the standard policy.</p>
     *
     * @param axes Supported nested scroll axes.
     *             See {@link NestedScrollingChild#startNestedScroll(int)}.
     * @return true if a cooperating parent view was found and nested scrolling started successfully
     */
    public boolean startNestedScroll(int axes) {
        if (hasNestedScrollingParent()) {
            // Already in progress
            return true;
        }
        if (isNestedScrollingEnabled()) {
            ViewParent p = mView.getParent();
            View child = mView;
            while (p != null) {
                if (p.onStartNestedScroll(child, mView, axes)) {
                    mNestedScrollingParent = p;
                    p.onNestedScrollAccepted( child, mView, axes);
                    return true;
                }
                if (p instanceof View) {
                    child = (View) p;
                }
                p = p.getParent();
            }
        }
        return false;
    }

    /**
     * Stop a nested scroll in progress.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link NestedScrollingChild} interface method with the same signature to implement
     * the standard policy.</p>
     */
    public void stopNestedScroll() {
        if (mNestedScrollingParent != null) {
        	mNestedScrollingParent.onStopNestedScroll(mView);
            mNestedScrollingParent = null;
        }
    }

    /**
     * Dispatch one step of a nested scrolling operation to the current nested scrolling parent.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link NestedScrollingChild} interface method with the same signature to implement
     * the standard policy.</p>
     *
     * @return true if the parent consumed any of the nested scroll
     */
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
            int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        if (isNestedScrollingEnabled() && mNestedScrollingParent != null) {
            if (dxConsumed != 0 || dyConsumed != 0 || dxUnconsumed != 0 || dyUnconsumed != 0) {
                int startX = 0;
                int startY = 0;
                if (offsetInWindow != null) {
                    mView.getLocationInWindow(offsetInWindow);
                    startX = offsetInWindow[0];
                    startY = offsetInWindow[1];
                }

                mNestedScrollingParent.onNestedScroll(mView, dxConsumed,
                        dyConsumed, dxUnconsumed, dyUnconsumed);

                if (offsetInWindow != null) {
                    mView.getLocationInWindow(offsetInWindow);
                    offsetInWindow[0] -= startX;
                    offsetInWindow[1] -= startY;
                }
                return true;
            } else if (offsetInWindow != null) {
                // No motion, no dispatch. Keep offsetInWindow up to date.
                offsetInWindow[0] = 0;
                offsetInWindow[1] = 0;
            }
        }
        return false;
    }

    /**
     * Dispatch one step of a nested pre-scrolling operation to the current nested scrolling parent.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link NestedScrollingChild} interface method with the same signature to implement
     * the standard policy.</p>
     *
     * @return true if the parent consumed any of the nested scroll
     */
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        if (isNestedScrollingEnabled() && mNestedScrollingParent != null) {
            if (dx != 0 || dy != 0) {
                int startX = 0;
                int startY = 0;
                if (offsetInWindow != null) {
                    mView.getLocationInWindow(offsetInWindow);
                    startX = offsetInWindow[0];
                    startY = offsetInWindow[1];
                }

                if (consumed == null) {
                    if (mTempNestedScrollConsumed == null) {
                        mTempNestedScrollConsumed = new int[2];
                    }
                    consumed = mTempNestedScrollConsumed;
                }
                consumed[0] = 0;
                consumed[1] = 0;
                mNestedScrollingParent.onNestedPreScroll(mView, dx, dy, consumed);

                if (offsetInWindow != null) {
                    mView.getLocationInWindow(offsetInWindow);
                    offsetInWindow[0] -= startX;
                    offsetInWindow[1] -= startY;
                }
                return consumed[0] != 0 || consumed[1] != 0;
            } else if (offsetInWindow != null) {
                offsetInWindow[0] = 0;
                offsetInWindow[1] = 0;
            }
        }
        return false;
    }

    /**
     * Dispatch a nested fling operation to the current nested scrolling parent.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link NestedScrollingChild} interface method with the same signature to implement
     * the standard policy.</p>
     *
     * @return true if the parent consumed the nested fling
     */
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        if (isNestedScrollingEnabled() && mNestedScrollingParent != null) {
            return mNestedScrollingParent.onNestedFling(mView, velocityX,
                    velocityY, consumed);
        }
        return false;
    }

    /**
     * Dispatch a nested pre-fling operation to the current nested scrolling parent.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link NestedScrollingChild} interface method with the same signature to implement
     * the standard policy.</p>
     *
     * @return true if the parent consumed the nested fling
     */
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        if (isNestedScrollingEnabled() && mNestedScrollingParent != null) {
            return mNestedScrollingParent.onNestedPreFling(mView, velocityX,
                    velocityY);
        }
        return false;
    }

    /**
     * View subclasses should always call this method on their
     * <code>NestedScrollingChildHelper</code> when detached from a window.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link NestedScrollingChild} interface method with the same signature to implement
     * the standard policy.</p>
     */
    public void onDetachedFromWindow() {
    	mView.stopNestedScroll();
    }

    /**
     * Called when a nested scrolling child stops its current nested scroll operation.
     *
     * <p>This is a delegate method. Call it from your {@link android.view.View View} subclass
     * method/{@link NestedScrollingChild} interface method with the same signature to implement
     * the standard policy.</p>
     *
     * @param child Child view stopping its nested scroll. This may not be a direct child view.
     */
    public void onStopNestedScroll(View child) {
    	mView.stopNestedScroll();
    }
}