package mst.view.menu;

import java.util.ArrayList;

import mst.utils.ParcelableCompat;
import mst.utils.ParcelableCompatCreatorCallbacks;
import mst.utils.SupportMenuInflater;
import mst.view.menu.bottomnavigation.BottomNavigationView.SavedState;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.BaseSavedState;
import android.widget.ListAdapter;
import android.widget.SpinnerPopupDialog;

public class BottomWidePopupMenu extends SpinnerPopupDialog implements DialogInterface.OnClickListener{
	
	private OnMenuItemClickListener mListener;
	
	private BottomWideMenu mMenu;
	
	private BottomWideMenuPresenter mPresenter = new BottomWideMenuPresenter();
	
	private MenuInflater mMenuInflater;
	
	public interface OnMenuItemClickListener{
		
		public boolean onItemClicked(MenuItem item);
		
	}
	
	public BottomWidePopupMenu(Context context){
		super(context);
		getMenu();
		mPresenter.initForMenu(getContext(), mMenu);
		mPresenter.setupMenuWindow(this);
	}

	
	@Override
	public void setMultipleChoiceItems(CharSequence[] entries,
			boolean[] selectedItem,
			OnMultiChoiceClickListener multiChoiceListener) {
		// do nothing
	}
	
	@Override
	public void setMultipleChoiceItems(ListAdapter adapter,
			boolean[] selectedItem,
			OnMultiChoiceClickListener multiChoiceListener) {
		// do nothing
	}
	
	
	@Override
	public void setPositiveButton(OnClickListener listener) {
		// do nothing
	}
	
	
	@Override
	public Bundle onSaveInstanceState() {
		Bundle bundle = super.onSaveInstanceState();
		mMenu.savePresenterStates(bundle);
		return bundle;
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		mMenu.restorePresenterStates(savedInstanceState);
		
	}
	
	public void updateMenuView(ArrayList<MstMenuItemImpl> items){
		mPresenter.updateMenuView(true);
	}
	
	/**
	 * Sets callback for click menu item
	 * @param listener
	 */
	public void setOnMenuItemClickedListener(OnMenuItemClickListener listener){
		mListener = listener;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if(mListener!=null && which >= 0 ){
			mListener.onItemClicked(getMenu().getItem(which));
		}
		dialog.dismiss();
	}
	
	/**
	 * Inflate menu xml into this window
	 * @param menuResId
	 */
	public void inflateMenu(int menuResId){
		getMenuInflater().inflate(menuResId, getMenu());
		mPresenter.getMenuView(null);
	}
	
	
	/**
	 * Gets current menu
	 * @return
	 */
	public Menu getMenu(){
		if(mMenu == null){
			mMenu = new BottomWideMenu(getContext());
			mMenu.addMenuPresenter(mPresenter);
		}
		
		return mMenu;
	}
	
	public void addItem(int itemId,CharSequence title,Drawable icon){
		
		mPresenter.addItem(itemId,title,icon);
	}
	
	public void removeItem(int itemId){
		mPresenter.removeItem(itemId);
	}
	

    /**
     * Returns the tint which is applied to our item's icons.
     *
     * @see #setItemIconTintList(ColorStateList)
     *
     */
    
    public ColorStateList getItemIconTintList() {
        return mPresenter.getItemTintList();
    }

    /**
     * Set the tint which is applied to our item's icons.
     *
     * @param tint the tint to apply.
     *
     * @attr ref R.styleable#NavigationView_itemIconTint
     */
    public void setItemIconTintList( ColorStateList tint) {
        mPresenter.setItemIconTintList(tint);
    }

    /**
     * Returns the tint which is applied to our item's icons.
     *
     * @see #setItemTextColor(ColorStateList)
     *
     */
    
    public ColorStateList getItemTextColor() {
        return mPresenter.getItemTextColor();
    }

    /**
     * Set the text color which is text to our items.
     *
     * @see #getItemTextColor()
     *
     */
    public void setItemTextColor( ColorStateList textColor) {
        mPresenter.setItemTextColor(textColor);
    }

    /**
     * Returns the background drawable for the menu items.
     *
     * @see #setItemBackgroundResource(int)
     *
     */
    public Drawable getItemBackground() {
        return mPresenter.getItemBackground();
    }

    /**
     * Set the background of the menu items to the given resource.
     *
     * @param resId The identifier of the resource.
     *
     */
    public void setItemBackgroundResource( int resId) {
        setItemBackground(getContext().getDrawable(resId));
    }

    /**
     * Set the background of the menu items to a given resource. The resource should refer to
     * a Drawable object or 0 to use the background background.
     *
     */
    public void setItemBackground(Drawable itemBackground) {
        mPresenter.setItemBackground(itemBackground);
    }

    /**
     * Sets the currently checked item in this navigation menu.
     *
     * @param id The item ID of the currently checked item.
     */
    public void setCheckedItem( int id) {
        MenuItem item = mMenu.findItem(id);
        if (item != null) {
            mPresenter.setCheckedItem((MstMenuItemImpl) item);
        }
    }

    /**
     * Set the text appearance of the menu items to a given resource.
     *
     */
    public void setItemTextAppearance( int resId) {
        mPresenter.setItemTextAppearance(resId);
    }

    private MenuInflater getMenuInflater() {
        if (mMenuInflater == null) {
            mMenuInflater = new SupportMenuInflater(getContext());
        }
        return mMenuInflater;
    }


	public int getSelectedPosition() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
	
	
}
