package mst.view.menu;

import java.util.ArrayList;

import mst.view.menu.MstMenuPresenter.Callback;

import com.mst.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BottomWideMenuPresenter implements MstMenuPresenter {

	private int mTextAppearance;
	private boolean mTextAppearanceSet;
	private ColorStateList mTextColor;
	private ColorStateList mIconTintList;
	private Drawable mItemBackground;
	private BottomMenuAdapter mAdapter;
	
	 private Callback mCallback;
	 private MstMenuBuilder mMenu;
	
	private BottomMenuListView mMenuView;

	private BottomWidePopupMenu mWindow;
	
	  /**
     * Padding to be inserted at the top of the list to avoid the first menu item
     * from being placed underneath the status bar.
     */
    private int mPaddingTopDefault;

    /**
     * Padding for separators between items
     */
    private int mPaddingSeparator;
	
	@Override
	public void initForMenu(Context context, MstMenuBuilder menu) {
		// TODO Auto-generated method stub
		mMenu = menu;
	}

	public void setupMenuWindow(BottomWidePopupMenu window){
		mWindow = window;
	}
	
	@Override
	public MstMenuView getMenuView(ViewGroup root) {
        if (mMenuView == null) {
            mMenuView = mWindow.getMenuView();
            if (mAdapter == null) {
                mAdapter = new BottomMenuAdapter();
            }
        }
        mWindow.setSingleChoiceItems(mAdapter, mWindow.getSelectedPosition(), mWindow);
//        mWindow.setNegativeButton(mWindow);
        return mMenuView;
	}

	public void setCheckedItem(MstMenuItemImpl item) {
		mAdapter.setCheckedItem(item);
	}

	public ColorStateList getItemTintList() {
		return mIconTintList;
	}

	public void setItemIconTintList(ColorStateList tint) {
		mIconTintList = tint;
		updateMenuView(false);
	}

	public ColorStateList getItemTextColor() {
		return mTextColor;
	}

	public void setItemTextColor(ColorStateList textColor) {
		mTextColor = textColor;
		updateMenuView(false);
	}

	public void setItemTextAppearance(int resId) {
		mTextAppearance = resId;
		mTextAppearanceSet = true;
		updateMenuView(false);
	}

	public Drawable getItemBackground() {
		return mItemBackground;
	}

	public void setItemBackground(Drawable itemBackground) {
		mItemBackground = itemBackground;
	}

	@Override
	public void updateMenuView(boolean cleared) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCallback(Callback cb) {
		// TODO Auto-generated method stub
		mCallback = cb;
	}

	@Override
	public boolean onSubMenuSelected(MstSubMenuBuilder subMenu) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCloseMenu(MstMenuBuilder menu, boolean allMenusAreClosing) {
		// TODO Auto-generated method stub
		if (mCallback != null) {
            mCallback.onCloseMenu(menu, allMenusAreClosing);
        }
		
	}

	@Override
	public boolean flagActionItems() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean expandItemActionView(MstMenuBuilder menu,
			MstMenuItemImpl item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean collapseItemActionView(MstMenuBuilder menu,
			MstMenuItemImpl item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		// TODO Auto-generated method stub

	}

	public void addItem(int itemId,CharSequence title,Drawable icon){
		mAdapter.addItem(itemId,title,icon);
	}
	
	public void removeItem(int itemId){
		mAdapter.removeItem(itemId);
	}
	
	
	  /**
     * Unified data model for all sorts of navigation menu items.
     */
    private interface BottomMenuItem {
    }

    /**
     * Normal or subheader items.
     */
    private static class BottomMenuTextItem implements BottomMenuItem {

        private final MstMenuItemImpl mMenuItem;

        private BottomMenuTextItem(MstMenuItemImpl item) {
            mMenuItem = item;
        }

        public MstMenuItemImpl getMenuItem() {
            return mMenuItem;
        }

    }
	

    /**
     * Separator items.
     */
    private static class BottomMenuSeparatorItem implements BottomMenuItem {

        private final int mPaddingTop;

        private final int mPaddingBottom;

        public BottomMenuSeparatorItem(int paddingTop, int paddingBottom) {
            mPaddingTop = paddingTop;
            mPaddingBottom = paddingBottom;
        }

        public int getPaddingTop() {
            return mPaddingTop;
        }

        public int getPaddingBottom() {
            return mPaddingBottom;
        }

    }
	
	
	
	class BottomMenuAdapter extends BaseAdapter{

		private static final String STATE_CHECKED_ITEM = "android:menu:checked";

        private static final String STATE_ACTION_VIEWS = "android:menu:action_views";
        private static final int VIEW_TYPE_NORMAL = 0;
        private static final int VIEW_TYPE_SUBHEADER = 1;
        private static final int VIEW_TYPE_SEPARATOR = 2;

        private final ArrayList<BottomMenuItem> mItems = new ArrayList<>();
        private MstMenuItemImpl mCheckedItem;
        private ColorDrawable mTransparentIcon;
        private boolean mUpdateSuspended;
		
        public BottomMenuAdapter() {
			// TODO Auto-generated constructor stub
        	prepareMenuItems();
		}
        
        
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mItems.size();
		}

		public void setCheckedItem(MstMenuItemImpl item) {
			// TODO Auto-generated method stub
			
		}

		public void addItem(int itemId,CharSequence title,Drawable icon){
			mMenu.add(0,itemId, 0, title);
//			mMenu.findItem(itemId).setIcon(icon);
			update();
		}
		
		public void removeItem(int itemId){
			mMenu.removeItem(itemId);
			update();
		}
		
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public void update() {
	            prepareMenuItems();
	            notifyDataSetChanged();
	        }
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView == null){
				convertView = LayoutInflater.from(mWindow.getContext()).inflate(com.mst.R.layout.bottom_wide_menu_item, null);
			}
			BottomMenuTextItem item = (BottomMenuTextItem) mItems.get(position);
			TextView title = (TextView) convertView.findViewById(android.R.id.text1);
			title.setText(item.mMenuItem.getTitle());
			return convertView;
		}
		
		
		

        /**
         * Flattens the visible menu items of {@link #mMenu} into {@link #mItems},
         * while inserting separators between items when necessary.
         */
		private void prepareMenuItems() {
			if (mUpdateSuspended) {
				return;
			}
			mUpdateSuspended = true;
			mItems.clear();

			int currentGroupId = -1;
			int currentGroupStart = 0;
			boolean currentGroupHasIcon = false;
			for (int i = 0, totalSize = mMenu.getVisibleItems().size(); i < totalSize; i++) {
				MstMenuItemImpl item = mMenu.getVisibleItems().get(i);
				if (item.isChecked()) {
					setCheckedItem(item);
				}
				if (item.isCheckable()) {
					item.setExclusiveCheckable(false);
				}

				int groupId = item.getGroupId();
				if (groupId != currentGroupId) { // first item in group
					currentGroupStart = mItems.size();
					currentGroupHasIcon = item.getIcon() != null;
					if (i != 0) {
						currentGroupStart++;
						mItems.add(new BottomMenuSeparatorItem(
								mPaddingSeparator, mPaddingSeparator));
					}
				} else if (!currentGroupHasIcon && item.getIcon() != null) {
					currentGroupHasIcon = true;
					appendTransparentIconIfMissing(currentGroupStart,
							mItems.size());
				}
				if (currentGroupHasIcon && item.getIcon() == null) {
					item.setIcon(android.R.color.transparent);
				}
				mItems.add(new BottomMenuTextItem(item));
				currentGroupId = groupId;

			}
			mUpdateSuspended = false;
		}
		
		
		
	     private void appendTransparentIconIfMissing(int startIndex, int endIndex) {
	            for (int i = startIndex; i < endIndex; i++) {
	            	BottomMenuTextItem textItem = (BottomMenuTextItem) mItems.get(i);
	                MenuItem item = textItem.getMenuItem();
	                if (item.getIcon() == null) {
	                    if (mTransparentIcon == null) {
	                        mTransparentIcon = new ColorDrawable(android.R.color.transparent);
	                    }
	                    item.setIcon(mTransparentIcon);
	                }
	            }
	        }
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
