package mst.widget;

import mst.widget.ActionMode.Item;
import android.view.View;

public interface ActionModeListener {
	/**
	 * When ActionMode's widget clicked,this called.
	 * @param view
	 */
	public void onActionItemClicked(Item item);

	/**
	 * When ActionMode is showing ,this called
	 * @param actionMode
	 */
	public void onActionModeShow(ActionMode actionMode);

	/**
	 * When ActionMode is Dismiss.this called.
	 * @param actionMode
	 */
	public void onActionModeDismiss(ActionMode actionMode);
}
