package mst.widget;

import android.graphics.drawable.Drawable;
import android.view.View;

public abstract class ActionMode extends android.view.ActionMode{
	
	public static final int POSITIVE_BUTTON = android.R.id.text2;
	
	public static final int NAGATIVE_BUTTON = android.R.id.text1;
	
	public static final int TITLE = com.android.internal.R.id.title;
	
	protected boolean mIsShow = false;
	/**
	 * Initial Action Mode here
	 */
	public abstract void prepareActionMode();
	
	/**
	 * Update ActionMode Title,this title is Can be customized.
	 * @param title
	 */
	public abstract void updateTitle(CharSequence title);
	
	/**
	 * Bind callback for Action Mode,See{@link mst.widget.ActionModeListener}.
	 * @param listener
	 */
	public abstract void bindActionModeListener(ActionModeListener listener);
	
	/**
	 * When caller invoke this method,ActionMode will show at screen top
	 */
	public abstract void show();
	
	/**
	 * When caller invoke this method,ActionMode will dismiss immediately.
	 */
	public abstract void dismiss();
	
	/**
	 * Get current ActionMode is showing or not.
	 * @return true if ActionMode is showing
	 */
	public  boolean isShowing(){
		return mIsShow;
	}
	
	public void setShow(boolean show){
		mIsShow = show;
	}
	
	public abstract void setTitle(CharSequence title);
	
	public abstract void setPositiveText(CharSequence text);
	
	public abstract void setNagativeText(CharSequence text);

	public abstract void showItem(int itemId,boolean show);
	
	public abstract void enableItem(int itemId,boolean enabled);
	
	public abstract void setItemTextAppearance(int itemId,int textAppearanceId);
	
	public abstract void setupDecor(View decor);
	
	public static class Item{
		public View itemView;
		
		public void setItemBackground(Drawable background){
			if(background != null){
				itemView.setBackground(background);
			}
		}
		
		public int getItemId(){
			return itemView.getId();
		}
	}
	
}
