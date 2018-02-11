package mst.widget;

import mst.utils.ParcelableCompat;
import mst.utils.ParcelableCompatCreatorCallbacks;
import mst.utils.SupportMenuInflater;
import mst.view.menu.MstMenuBuilder;
import mst.view.menu.MstMenuItemImpl;
import mst.view.menu.MstNavigationMenu;
import mst.view.menu.MstNavigationMenuPresenter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;

import com.mst.R;
import com.mst.internal.widget.ScrimInsetsFrameLayout;


/**
 * Represents a standard navigation menu for application. The menu contents can be populated
 * by a menu resource file.
 * <p>NavigationView is typically placed inside a {@link mst.widget.DrawerLayout}.
 * </p>
 * <pre>
 * &lt;mst.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     android:id="@+id/drawer_layout"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     android:fitsSystemWindows="true"&gt;
 *
 *     &lt;!-- Your contents --&gt;
 *
 *     &lt;mst.widget.NavigationView
 *         android:id="@+id/navigation"
 *         android:layout_width="wrap_content"
 *         android:layout_height="match_parent"
 *         android:layout_gravity="start"
 *         app:menu="@menu/my_navigation_items" /&gt;
 * &lt;/mst.widget.DrawerLayout&gt;
 * </pre>
 */
public class NavigationView extends ScrimInsetsFrameLayout {

    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};

    private static final int PRESENTER_NAVIGATION_VIEW_ID = 1;

    private final MstNavigationMenu mMenu;
    private final MstNavigationMenuPresenter mPresenter = new MstNavigationMenuPresenter();

    private OnNavigationItemSelectedListener mListener;
    private int mMaxWidth;

    private MenuInflater mMenuInflater;

    public NavigationView(Context context) {
        this(context, null);
    }

    public NavigationView(Context context, AttributeSet attrs) {
        this(context, attrs, com.mst.R.attr.navigationStyle);
    }

    public NavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        // Create the menu
        mMenu = new MstNavigationMenu(context);

        // Custom attributes
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.NavigationView, defStyleAttr,
                R.style.Widget_NavigationView);

        //noinspection deprecation
        setBackgroundDrawable(a.getDrawable(R.styleable.NavigationView_android_background));
        if (a.hasValue(R.styleable.NavigationView_android_elevation)) {
            setElevation(a.getDimensionPixelSize(
                    R.styleable.NavigationView_android_elevation, 0));
        }
        setFitsSystemWindows(
                a.getBoolean(R.styleable.NavigationView_android_fitsSystemWindows, false));

        mMaxWidth = a.getDimensionPixelSize(R.styleable.NavigationView_android_maxWidth, 0);

        final ColorStateList itemIconTint;
        if (a.hasValue(R.styleable.NavigationView_itemIconTint)) {
            itemIconTint = a.getColorStateList(R.styleable.NavigationView_itemIconTint);
        } else {
            itemIconTint = createDefaultColorStateList(android.R.attr.textColorSecondary);
        }

        boolean textAppearanceSet = false;
        int textAppearance = 0;
        if (a.hasValue(R.styleable.NavigationView_itemTextAppearance)) {
            textAppearance = a.getResourceId(R.styleable.NavigationView_itemTextAppearance, 0);
            textAppearanceSet = true;
        }

        ColorStateList itemTextColor = null;
        if (a.hasValue(R.styleable.NavigationView_itemTextColor)) {
            itemTextColor = a.getColorStateList(R.styleable.NavigationView_itemTextColor);
        }

        if (!textAppearanceSet && itemTextColor == null) {
            // If there isn't a text appearance set, we'll use a default text color
            itemTextColor = createDefaultColorStateList(android.R.attr.textColorPrimary);
        }

        final Drawable itemBackground = a.getDrawable(R.styleable.NavigationView_itemBackground);

        mMenu.setCallback(new MstMenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MstMenuBuilder menu, MenuItem item) {
                return mListener != null && mListener.onNavigationItemSelected(item);
            }

            @Override
            public void onMenuModeChange(MstMenuBuilder menu) {}
        });
        mPresenter.setId(PRESENTER_NAVIGATION_VIEW_ID);
        mPresenter.initForMenu(context, mMenu);
        mPresenter.setItemIconTintList(itemIconTint);
        if (textAppearanceSet) {
            mPresenter.setItemTextAppearance(textAppearance);
        }
        mPresenter.setItemTextColor(itemTextColor);
        mPresenter.setItemBackground(itemBackground);
        mMenu.addMenuPresenter(mPresenter);
        addView((View) mPresenter.getMenuView(this));

        if (a.hasValue(R.styleable.NavigationView_menu)) {
            inflateMenu(a.getResourceId(R.styleable.NavigationView_menu, 0));
        }

        if (a.hasValue(R.styleable.NavigationView_headerLayout)) {
            inflateHeaderView(a.getResourceId(R.styleable.NavigationView_headerLayout, 0));
        }

        a.recycle();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState state = new SavedState(superState);
        state.menuState = new Bundle();
        mMenu.savePresenterStates(state.menuState);
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable savedState) {
        SavedState state = (SavedState) savedState;
        super.onRestoreInstanceState(state.getSuperState());
        mMenu.restorePresenterStates(state.menuState);
    }

    /**
     * Set a listener that will be notified when a menu item is clicked.
     *
     * @param listener The listener to notify
     */
    public void setNavigationItemSelectedListener(OnNavigationItemSelectedListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        switch (MeasureSpec.getMode(widthSpec)) {
            case MeasureSpec.EXACTLY:
                // Nothing to do
                break;
            case MeasureSpec.AT_MOST:
                widthSpec = MeasureSpec.makeMeasureSpec(
                        Math.min(MeasureSpec.getSize(widthSpec), mMaxWidth), MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.UNSPECIFIED:
                widthSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, MeasureSpec.EXACTLY);
                break;
        }
        // Let super sort out the height
        super.onMeasure(widthSpec, heightSpec);
    }


    /**
     * Inflate a menu resource into this navigation view.
     *
     * <p>Existing items in the menu will not be modified or removed.</p>
     *
     * @param resId ID of a menu resource to inflate
     */
    public void inflateMenu(int resId) {
        mPresenter.setUpdateSuspended(true);
        getMenuInflater().inflate(resId, mMenu);
        mPresenter.setUpdateSuspended(false);
        mPresenter.updateMenuView(false);
    }

    /**
     * Returns the {@link Menu} instance associated with this navigation view.
     */
    public Menu getMenu() {
        return mMenu;
    }

    /**
     * Inflates a View and add it as a header of the navigation menu.
     *
     * @param res The layout resource ID.
     * @return a newly inflated View.
     */
    public View inflateHeaderView( int res) {
        return mPresenter.inflateHeaderView(res);
    }

    /**
     * Adds a View as a header of the navigation menu.
     *
     * @param view The view to be added as a header of the navigation menu.
     */
    public void addHeaderView( View view) {
        mPresenter.addHeaderView(view);
    }

    /**
     * Removes a previously-added header view.
     *
     * @param view The view to remove
     */
    public void removeHeaderView( View view) {
        mPresenter.removeHeaderView(view);
    }

    /**
     * Gets the number of headers in this NavigationView.
     *
     * @return A positive integer representing the number of headers.
     */
    public int getHeaderCount() {
        return mPresenter.getHeaderCount();
    }

    /**
     * Gets the header view at the specified position.
     *
     * @param index The position at which to get the view from.
     * @return The header view the specified position or null if the position does not exist in this
     * NavigationView.
     */
    public View getHeaderView(int index) {
        return mPresenter.getHeaderView(index);
    }

    /**
     * Returns the tint which is applied to our item's icons.
     *
     * @see #setItemIconTintList(ColorStateList)
     *
     * @attr ref R.styleable#NavigationView_itemIconTint
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
     * @attr ref R.styleable#NavigationView_itemTextColor
     */
    
    public ColorStateList getItemTextColor() {
        return mPresenter.getItemTextColor();
    }

    /**
     * Set the text color which is text to our items.
     *
     * @see #getItemTextColor()
     *
     * @attr ref R.styleable#NavigationView_itemTextColor
     */
    public void setItemTextColor( ColorStateList textColor) {
        mPresenter.setItemTextColor(textColor);
    }

    /**
     * Returns the background drawable for the menu items.
     *
     * @see #setItemBackgroundResource(int)
     *
     * @attr ref R.styleable#NavigationView_itemBackground
     */
    public Drawable getItemBackground() {
        return mPresenter.getItemBackground();
    }

    /**
     * Set the background of the menu items to the given resource.
     *
     * @param resId The identifier of the resource.
     *
     * @attr ref R.styleable#NavigationView_itemBackground
     */
    public void setItemBackgroundResource( int resId) {
        setItemBackground(getContext().getDrawable(resId));
    }

    /**
     * Set the background of the menu items to a given resource. The resource should refer to
     * a Drawable object or 0 to use the background background.
     *
     * @attr ref R.styleable#NavigationView_itemBackground
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
     * @attr ref R.styleable#NavigationView_itemTextAppearance
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

    private ColorStateList createDefaultColorStateList(int baseColorThemeAttr) {
        TypedValue value = new TypedValue();
        if (!getContext().getTheme().resolveAttribute(baseColorThemeAttr, value, true)) {
            return null;
        }
        ColorStateList baseColor = getResources().getColorStateList(value.resourceId);
        if (!getContext().getTheme().resolveAttribute(android.R.attr.colorPrimary, value, true)) {
            return null;
        }
        int colorPrimary = value.data;
        int defaultColor = baseColor.getDefaultColor();
        return new ColorStateList(new int[][]{
                DISABLED_STATE_SET,
                CHECKED_STATE_SET,
                EMPTY_STATE_SET
        }, new int[]{
                baseColor.getColorForState(DISABLED_STATE_SET, defaultColor),
                colorPrimary,
                defaultColor
        });
    }

    /**
     * Listener for handling events on navigation items.
     */
    public interface OnNavigationItemSelectedListener {

        /**
         * Called when an item in the navigation menu is selected.
         *
         * @param item The selected item
         *
         * @return true to display the item as the selected item
         */
        public boolean onNavigationItemSelected(MenuItem item);
    }

    /**
     * User interface state that is stored by NavigationView for implementing
     * onSaveInstanceState().
     */
    public static class SavedState extends BaseSavedState {
        public Bundle menuState;

        public SavedState(Parcel in, ClassLoader loader) {
            super(in);
            menuState = in.readBundle(loader);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel( Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBundle(menuState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel parcel, ClassLoader loader) {
                return new SavedState(parcel, loader);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });
    }

}