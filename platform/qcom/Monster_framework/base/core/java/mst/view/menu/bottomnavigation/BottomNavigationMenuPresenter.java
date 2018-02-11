package mst.view.menu.bottomnavigation;

import java.util.ArrayList;

import com.mst.R;

import mst.utils.ParcelableSparseArray;
import mst.view.menu.MstMenuBuilder;
import mst.view.menu.MstMenuItemImpl;
import mst.view.menu.MstMenuPresenter;
import mst.view.menu.MstMenuView;
import mst.view.menu.MstSubMenuBuilder;
import mst.widget.recycleview.RecyclerView;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BottomNavigationMenuPresenter implements MstMenuPresenter {

    private static final String STATE_HIERARCHY = "android:menu:list";
    private static final String STATE_ADAPTER = "android:menu:adapter";

    private BottomNavigationMenuView mMenuView;

    private Callback mCallback;
    private MstMenuBuilder mMenu;
    private int mId;

    private NavigationMenuAdapter mAdapter;
    private LayoutInflater mLayoutInflater;

    private int mTextAppearance;
    private boolean mTextAppearanceSet;
    private ColorStateList mTextColor;
    private ColorStateList mIconTintList;
    private Drawable mItemBackground;

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
        mLayoutInflater = LayoutInflater.from(context);
        mMenu = menu;
        Resources res = context.getResources();
        mPaddingTopDefault = res.getDimensionPixelOffset(
                R.dimen.navigation_padding_top_default);
        mPaddingSeparator = res.getDimensionPixelOffset(
                R.dimen.navigation_separator_vertical_padding);
    }

    @Override
    public MstMenuView getMenuView(ViewGroup root) {
        if (mMenuView == null) {
            mMenuView = (BottomNavigationMenuView) mLayoutInflater.inflate(
                    R.layout.bottom_navigation_menu, root, false);
            if (mAdapter == null) {
                mAdapter = new NavigationMenuAdapter();
            }
            mMenuView.setAdapter(mAdapter);
        }
        return mMenuView;
    }

    @Override
    public void updateMenuView(boolean cleared) {
        if (mAdapter != null) {
            mAdapter.update();
        }
    }

    @Override
    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    @Override
    public boolean onSubMenuSelected(MstSubMenuBuilder subMenu) {
        return false;
    }

    @Override
    public void onCloseMenu(MstMenuBuilder menu, boolean allMenusAreClosing) {
        if (mCallback != null) {
            mCallback.onCloseMenu(menu, allMenusAreClosing);
        }
    }

    @Override
    public boolean flagActionItems() {
        return false;
    }

    @Override
    public boolean expandItemActionView(MstMenuBuilder menu, MstMenuItemImpl item) {
        return false;
    }

    @Override
    public boolean collapseItemActionView(MstMenuBuilder menu, MstMenuItemImpl item) {
        return false;
    }

    @Override
    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        if (mMenuView != null) {
            SparseArray<Parcelable> hierarchy = new SparseArray<>();
            mMenuView.saveHierarchyState(hierarchy);
            state.putSparseParcelableArray(STATE_HIERARCHY, hierarchy);
        }
        if (mAdapter != null) {
            state.putBundle(STATE_ADAPTER, mAdapter.createInstanceState());
        }
        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        Bundle state = (Bundle) parcelable;
        SparseArray<Parcelable> hierarchy = state.getSparseParcelableArray(STATE_HIERARCHY);
        if (hierarchy != null) {
            mMenuView.restoreHierarchyState(hierarchy);
        }
        Bundle adapterState = state.getBundle(STATE_ADAPTER);
        if (adapterState != null) {
            mAdapter.restoreInstanceState(adapterState);
        }
    }

    public void setCheckedItem(MstMenuItemImpl item) {
        mAdapter.setCheckedItem(item);
    }
    
    public ColorStateList getItemTintList() {
        return mIconTintList;
    }

    public void setItemIconTintList( ColorStateList tint) {
        mIconTintList = tint;
        updateMenuView(false);
    }

    
    public ColorStateList getItemTextColor() {
        return mTextColor;
    }

    public void setItemTextColor( ColorStateList textColor) {
        mTextColor = textColor;
        updateMenuView(false);
    }

    public void setItemTextAppearance( int resId) {
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

    public void setUpdateSuspended(boolean updateSuspended) {
        if (mAdapter != null) {
            mAdapter.setUpdateSuspended(updateSuspended);
        }
    }

    private abstract static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

    }

    public static class NormalViewHolder   {
    	private View itemView;
        public NormalViewHolder(LayoutInflater inflater, ViewGroup parent,
                View.OnClickListener listener) {
        	  itemView = inflater.inflate(R.layout.bottom_navigation_item, null);
        	  itemView.setOnClickListener(listener);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            		LinearLayout.LayoutParams.MATCH_PARENT,
            		LinearLayout.LayoutParams.MATCH_PARENT, 1);
            parent.addView(itemView,params);
        }

    }



    /**
     * Handles click events for the menu items. The items has to be {@link NavigationMenuItemView}.
     */
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            BottomNavigationMenuItemView itemView = (BottomNavigationMenuItemView) v/*.getParent()*/;
            setUpdateSuspended(true);
            MstMenuItemImpl item = itemView.getItemData();
            boolean result = mMenu.performItemAction(item, BottomNavigationMenuPresenter.this, 0);
            if (item != null && item.isCheckable() && result) {
                mAdapter.setCheckedItem(item);
            }
            setUpdateSuspended(false);
            updateMenuView(false);
        }

    };
    

    public class NavigationMenuAdapter {

        private static final String STATE_CHECKED_ITEM = "android:menu:checked";

        private static final String STATE_ACTION_VIEWS = "android:menu:action_views";
        private static final int VIEW_TYPE_NORMAL = 0;
        private static final int VIEW_TYPE_SUBHEADER = 1;
        private static final int VIEW_TYPE_SEPARATOR = 2;

        private final ArrayList<NavigationMenuItem> mItems = new ArrayList<>();
        private MstMenuItemImpl mCheckedItem;
        private ColorDrawable mTransparentIcon;
        private boolean mUpdateSuspended;

        NavigationMenuAdapter() {
            prepareMenuItems();
        }

        public long getItemId(int position) {
            return position;
        }

        public int getItemCount() {
            return mItems.size();
        }

        public int getItemViewType(int position) {
        	NavigationMenuItem item = mItems.get(position);
            if (item instanceof NavigationMenuSeparatorItem) {
                return VIEW_TYPE_SEPARATOR;
            } else if (item instanceof NavigationMenuTextItem) {
             NavigationMenuTextItem textItem = (NavigationMenuTextItem) item;
                if (textItem.getMenuItem().hasSubMenu()) {
                    return VIEW_TYPE_SUBHEADER;
                } else {
                    return VIEW_TYPE_NORMAL;
                }
            }
            throw new RuntimeException("Unknown item type.");
        }

        public NormalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    return new NormalViewHolder(mLayoutInflater, parent, mOnClickListener);
        }

        public void onBindViewHolder(NormalViewHolder holder, int position) {
                	BottomNavigationMenuItemView itemView = (BottomNavigationMenuItemView) holder.itemView;
                    itemView.setIconTintList(mIconTintList);
                    if (mTextAppearanceSet) {
                        itemView.setTextAppearance(itemView.getContext(), mTextAppearance);
                    }
                    if (mTextColor != null) {
                        itemView.setTextColor(mTextColor);
                    }
                    NavigationMenuTextItem item = (NavigationMenuTextItem) mItems.get(position);
                    itemView.initialize(item.getMenuItem(), 0);
        }

        public void onViewRecycled(NormalViewHolder holder) {
                ((BottomNavigationMenuItemView) holder.itemView).recycle();
        }

        public void update() {
            prepareMenuItems();
            mMenuView.update(mAdapter);
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
                if (item.hasSubMenu()) {
                    SubMenu subMenu = item.getSubMenu();
                    if (subMenu.hasVisibleItems()) {
                        if (i != 0) {
                            mItems.add(new NavigationMenuSeparatorItem(mPaddingSeparator, 0));
                        }
                        mItems.add(new NavigationMenuTextItem(item));
                        boolean subMenuHasIcon = false;
                        int subMenuStart = mItems.size();
                        for (int j = 0, size = subMenu.size(); j < size; j++) {
                        	MstMenuItemImpl subMenuItem = (MstMenuItemImpl) subMenu.getItem(j);
                            if (subMenuItem.isVisible()) {
                                if (!subMenuHasIcon && subMenuItem.getIcon() != null) {
                                    subMenuHasIcon = true;
                                }
                                if (subMenuItem.isCheckable()) {
                                    subMenuItem.setExclusiveCheckable(false);
                                }
                                if (item.isChecked()) {
                                    setCheckedItem(item);
                                }
                                mItems.add(new NavigationMenuTextItem(subMenuItem));
                            }
                        }
                        if (subMenuHasIcon) {
                            appendTransparentIconIfMissing(subMenuStart, mItems.size());
                        }
                    }
                } else {
                    int groupId = item.getGroupId();
                    if (groupId != currentGroupId) { // first item in group
                        currentGroupStart = mItems.size();
                        currentGroupHasIcon = item.getIcon() != null;
                        if (i != 0) {
                            currentGroupStart++;
                            mItems.add(new NavigationMenuSeparatorItem(
                                    mPaddingSeparator, mPaddingSeparator));
                        }
                    } else if (!currentGroupHasIcon && item.getIcon() != null) {
                        currentGroupHasIcon = true;
                        appendTransparentIconIfMissing(currentGroupStart, mItems.size());
                    }
                    if (currentGroupHasIcon && item.getIcon() == null) {
                        item.setIcon(android.R.color.transparent);
                    }
                    mItems.add(new NavigationMenuTextItem(item));
                    currentGroupId = groupId;
                }
            }
            mUpdateSuspended = false;
        }

        private void appendTransparentIconIfMissing(int startIndex, int endIndex) {
            for (int i = startIndex; i < endIndex; i++) {
                NavigationMenuTextItem textItem = (NavigationMenuTextItem) mItems.get(i);
                MenuItem item = textItem.getMenuItem();
                if (item.getIcon() == null) {
                    if (mTransparentIcon == null) {
                        mTransparentIcon = new ColorDrawable(android.R.color.transparent);
                    }
                    item.setIcon(mTransparentIcon);
                }
            }
        }

        public void setCheckedItem(MstMenuItemImpl checkedItem) {
            if (mCheckedItem == checkedItem || !checkedItem.isCheckable()) {
                return;
            }
            if (mCheckedItem != null) {
                mCheckedItem.setChecked(false);
            }
            mCheckedItem = checkedItem;
            checkedItem.setChecked(true);
        }

        public Bundle createInstanceState() {
            Bundle state = new Bundle();
            if (mCheckedItem != null) {
                state.putInt(STATE_CHECKED_ITEM, mCheckedItem.getItemId());
            }
            // Store the states of the action views.
            SparseArray<ParcelableSparseArray> actionViewStates = new SparseArray<>();
            for (NavigationMenuItem navigationMenuItem : mItems) {
                if (navigationMenuItem instanceof NavigationMenuTextItem) {
                	MstMenuItemImpl item = ((NavigationMenuTextItem) navigationMenuItem).getMenuItem();
                    View actionView = item != null ? item.getActionView() : null;
                    if (actionView != null) {
                        ParcelableSparseArray container = new ParcelableSparseArray();
                        actionView.saveHierarchyState(container);
                        actionViewStates.put(item.getItemId(), container);
                    }
                }
            }
            state.putSparseParcelableArray(STATE_ACTION_VIEWS, actionViewStates);
            return state;
        }

        public void restoreInstanceState(Bundle state) {
            int checkedItem = state.getInt(STATE_CHECKED_ITEM, 0);
            if (checkedItem != 0) {
                mUpdateSuspended = true;
                for (NavigationMenuItem item : mItems) {
                    if (item instanceof NavigationMenuTextItem) {
                    	MstMenuItemImpl menuItem = ((NavigationMenuTextItem) item).getMenuItem();
                        if (menuItem != null && menuItem.getItemId() == checkedItem) {
                            setCheckedItem(menuItem);
                            break;
                        }
                    }
                }
                mUpdateSuspended = false;
                prepareMenuItems();
            }
            // Restore the states of the action views.
            SparseArray<ParcelableSparseArray> actionViewStates = state
                    .getSparseParcelableArray(STATE_ACTION_VIEWS);
            for (NavigationMenuItem navigationMenuItem : mItems) {
                if (navigationMenuItem instanceof NavigationMenuTextItem) {
                	MstMenuItemImpl item = ((NavigationMenuTextItem) navigationMenuItem).getMenuItem();
                    View actionView = item != null ? item.getActionView() : null;
                    if (actionView != null) {
                        actionView.restoreHierarchyState(actionViewStates.get(item.getItemId()));
                    }
                }
            }
        }

        public void setUpdateSuspended(boolean updateSuspended) {
            mUpdateSuspended = updateSuspended;
        }

    }

    /**
     * Unified data model for all sorts of navigation menu items.
     */
    private interface NavigationMenuItem {
    }

    /**
     * Normal or subheader items.
     */
    private static class NavigationMenuTextItem implements NavigationMenuItem {

        private final MstMenuItemImpl mMenuItem;

        private NavigationMenuTextItem(MstMenuItemImpl item) {
            mMenuItem = item;
        }

        public MstMenuItemImpl getMenuItem() {
            return mMenuItem;
        }

    }

    /**
     * Separator items.
     */
    private static class NavigationMenuSeparatorItem implements NavigationMenuItem {

        private final int mPaddingTop;

        private final int mPaddingBottom;

        public NavigationMenuSeparatorItem(int paddingTop, int paddingBottom) {
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
    
	public void setItemEnable(int itemId, boolean enabled) {
		if(mMenuView != null){
			BottomNavigationMenuItemView itemView  = (BottomNavigationMenuItemView) mMenuView.findViewById(itemId);
			//Log.d("bn", "setItemEnable-- has Item-->"+(itemView != null));
			if(itemView != null){
				itemView.setEnabled(enabled);
			}
		}
		
	}


}