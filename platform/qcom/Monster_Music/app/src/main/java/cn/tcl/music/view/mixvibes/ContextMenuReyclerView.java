package cn.tcl.music.view.mixvibes;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class ContextMenuReyclerView extends RecyclerView {

    boolean mIsControlled = false;

	public ContextMenuReyclerView(Context context) {
		super(context);
	}

	public ContextMenuReyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ContextMenuReyclerView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected ContextMenuInfo getContextMenuInfo() {
		return mContextMenuInfo;
	}

    /**
     * Extra menu information provided to the
     * {@link OnCreateContextMenuListener#onCreateContextMenu(ContextMenu, View, ContextMenuInfo) }
     * callback when a context menu is brought up for this AdapterView.
     *
     */
    public static class RecyclerViewContextMenuInfo implements ContextMenuInfo {

        public RecyclerViewContextMenuInfo(int position) {
            this.position = position;
        }

        /**
         * The position in the adapter for which the context menu is being
         * displayed.
         */
        public int position;
    }
    
    private RecyclerViewContextMenuInfo mContextMenuInfo;

	
	@Override
	public boolean showContextMenuForChild(View originalView) {
        final int menuInfoPosition = getChildPosition(originalView);
        if (menuInfoPosition >= 0) {
            mContextMenuInfo = new RecyclerViewContextMenuInfo(menuInfoPosition);
            boolean handled = super.showContextMenuForChild(originalView);

            return handled;
        }
        return false;
	}

    public boolean isInTouchMode() {
        return super.isInTouchMode() && !mIsControlled;
    }

    public void setControlled (boolean isControlled)
    {
        mIsControlled = isControlled;
    }

    public boolean isControlled ()
    {
        return mIsControlled;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        setControlled(false);
        return super.dispatchTouchEvent(ev);
    }

    public void moveControlledSelection(boolean goDown)
    {
        if (getLayoutManager() == null)
            return;

        if (getAdapter() == null || getAdapter().getItemCount() <= 0)
            return;

        setControlled(true);
        //dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));

        boolean isGrid = getLayoutManager() instanceof GridLayoutManager;

        View focusedView = null;
        int oldFocusedPosition = 0;
        if (getFocusedChild() != null)
        {
            oldFocusedPosition = getLayoutManager().getPosition(getFocusedChild());
            if (goDown && oldFocusedPosition >= getAdapter().getItemCount() - 1 ||
                !goDown && oldFocusedPosition == 0)
                return;
        }

        if (isGrid)
        {
            final GridLayoutManager gridManader = (GridLayoutManager) getLayoutManager();
            final int spanCount = gridManader.getSpanCount();
            if (goDown)
            {
                focusedView = focusSearch(getFocusedChild(), View.FOCUS_RIGHT);
                final int numItems = getAdapter().getItemCount();
                if (focusedView == null)
                {
                    if (oldFocusedPosition < getAdapter().getItemCount())
                    {
                        int currentPosition = oldFocusedPosition - spanCount + 1;
                        if (currentPosition < 0)
                            currentPosition = 0;
                        View tempFocusedView = gridManader.findViewByPosition(currentPosition);
                        tempFocusedView.requestFocus();
                        if (oldFocusedPosition == gridManader.findLastVisibleItemPosition())
                        {
                            // Just make a focus down does not work if the we need to scroll the view,
                            // so there is another process to handle this specific case.
                            focusedView = focusSearch(tempFocusedView, View.FOCUS_DOWN);
                            if (focusedView != null)
                            {
                                // We have reach the row below, be we are at the end of this row. We need to come back.
                                int newPosition = 0;
                                if (gridManader.getPosition(focusedView) == getAdapter().getItemCount() - 1)
                                {
                                    // Since we are at the end of this grid, we may not need to come back with spanCount.
                                    // Handle this case by using the % spanCount to make sure we will be at the first item of the row.
                                    final int modulo = (numItems % spanCount); // Need to calculate the backward offset when we reach the last row
                                    int endOffset = 0;
                                    if (modulo > 0)
                                    {
                                        endOffset = spanCount - modulo;
                                    }
                                    newPosition = gridManader.getPosition(focusedView) - (spanCount - 1 - endOffset);
                                }
                                else
                                {
                                    newPosition = gridManader.getPosition(focusedView) - spanCount + 1;
                                }

                                focusedView = gridManader.findViewByPosition(newPosition);
                                if (focusedView != null)
                                    focusedView.requestFocus();
                            }

                        }
                        else
                        {
                            focusedView = focusSearch(tempFocusedView, View.FOCUS_DOWN);
                        }
                    }
                }
            }
            else
            {
                focusedView = focusSearch(getFocusedChild(), View.FOCUS_LEFT);
                if (focusedView == null)
                {
                    if (oldFocusedPosition > 0)
                    {

                        focusedView = focusSearch(getFocusedChild(), View.FOCUS_UP);
                        if (focusedView == null)
                            return;
                        int newPosition = gridManader.getPosition(focusedView) + spanCount - 1;

                        focusedView = gridManader.findViewByPosition(newPosition);
                    }
                }
            }

        }
        else
        {
            focusedView = focusSearch(getFocusedChild(), goDown? View.FOCUS_DOWN : View.FOCUS_UP);
        }

        if (focusedView == null)
        {
            View oldFocusedChild = getFocusedChild();
            if (oldFocusedChild != null)
            {
                final int oldPosition = getLayoutManager().getPosition(oldFocusedChild);

                if (oldPosition + 1 < getAdapter().getItemCount())
                {
                    focusedView = getLayoutManager().findViewByPosition(oldPosition + 1);
                    if (focusedView == null)
                    {
                        getLayoutManager().scrollToPosition(oldPosition + 1);
                    }
                    focusedView = getLayoutManager().findViewByPosition(oldPosition + 1);
                }

            }
            else
            {
                int firstVisibleItem = 0;

                if (getLayoutManager() instanceof LinearLayoutManager)
                {
                    firstVisibleItem = ((LinearLayoutManager)getLayoutManager()).findFirstVisibleItemPosition();
                }

                focusedView = getLayoutManager().findViewByPosition(firstVisibleItem);
            }
        }
        if (focusedView != null)
        {
            focusedView.requestFocus();
        }
        //dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN));

        setControlled(false);
    }

    private boolean rollBackFocusIfNeeded(boolean goDown, int oldFocusedPosition) {
        View focusedView;
        if (oldFocusedPosition == getAdapter().getItemCount() - 1 && goDown) // We are at the end, roll back
        {
            getLayoutManager().scrollToPosition(0);
            focusedView = getLayoutManager().findViewByPosition(0);
            if (focusedView != null)
            {
                focusedView.requestFocus();
            }
            return true;
        }
        else if (oldFocusedPosition == 0 && !goDown)
        {

            getLayoutManager().scrollToPosition(getAdapter().getItemCount() - 1);
            focusedView = getLayoutManager().findViewByPosition(getAdapter().getItemCount() - 1);
            if (focusedView != null)
            {
                focusedView.requestFocus();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
	public void stopScroll() { //TODO: temp
		try
		{
			super.stopScroll();		
		}
		catch(NullPointerException npe)
		{
			npe.printStackTrace();
		}
	}
	
}
