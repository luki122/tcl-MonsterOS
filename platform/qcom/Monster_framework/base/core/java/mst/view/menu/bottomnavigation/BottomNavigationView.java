package mst.view.menu.bottomnavigation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import mst.utils.ParcelableCompat;
import mst.utils.ParcelableCompatCreatorCallbacks;
import mst.utils.SupportMenuInflater;
import mst.view.menu.MstMenuBuilder;
import mst.view.menu.MstMenuItemImpl;
import mst.view.menu.MstNavigationMenu;
import mst.view.menu.MstNavigationMenuPresenter;
import mst.widget.Snackbar;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mst.R;
import com.mst.R.anim;
import com.mst.internal.widget.ScrimInsetsFrameLayout;

public class BottomNavigationView extends LinearLayout  {

    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};


    private final BottomNavigationMenu mMenu;

    private OnNavigationItemSelectedListener mListener;
    private int mMaxHeight;

    private MenuInflater mMenuInflater;
    
    private ColorStateList mIconColorTintList;
    private LinearLayout mMenuParent;
    private LayoutInflater mInflater ;
    private int mTextAppearance;
    private ColorStateList mItemTextColor;
    private boolean mItemEnable = true;
    private int mIconSize;
    private int mVisibility;
    private AnimationRunnable mAnimationRunnable;
    public BottomNavigationView(Context context) {
        this(context, null);
    }

    public BottomNavigationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(getContext());
        mIconSize = context.getResources().getDimensionPixelSize(
				R.dimen.navigation_icon_size);
        setOrientation(LinearLayout.HORIZONTAL);
        mAnimationRunnable = new AnimationRunnable(this);
        createParent();
        // Create the menu
        mMenu = new BottomNavigationMenu(context);

        // Custom attributes
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.NavigationView, defStyleAttr,
                R.style.Widget_BottomNavigationView);

        //noinspection deprecation
        setBackgroundDrawable(a.getDrawable(R.styleable.NavigationView_android_background));
        if (a.hasValue(R.styleable.NavigationView_android_elevation)) {
            setElevation(a.getDimensionPixelSize(
                    R.styleable.NavigationView_android_elevation, 0));
        }

        mMaxHeight = a.getDimensionPixelSize(R.styleable.NavigationView_android_maxHeight, 0);

        final ColorStateList itemIconTint;
        if (a.hasValue(R.styleable.NavigationView_itemIconTint)) {
            itemIconTint = a.getColorStateList(R.styleable.NavigationView_itemIconTint);
        } else {
            itemIconTint = createDefaultColorStateList(android.R.attr.textColorSecondary);
        }
        mIconColorTintList = itemIconTint;
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
        if (textAppearanceSet) {
            setItemTextAppearance(textAppearance);
        }
        setItemTextColor(itemTextColor);
        setItemBackground(itemBackground);

        if (a.hasValue(R.styleable.NavigationView_menu)) {
            inflateMenu(a.getResourceId(R.styleable.NavigationView_menu, 0));
        }
        a.recycle();
        setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return true;
			}
		});
    }
    
    
    private void createParent(){
    }

    public void setItemEnable(int itemId,boolean enabled){
    	mItemEnable = enabled;
    	View itemView = findViewById(itemId);
    	if(itemView != null){
    		itemView.setEnabled(enabled);
    		enableItemWidget(enabled, itemView);
    	}
    }
    

    private void enableItemWidget(boolean enabled,View itemParent){
    	ImageView iconView = (ImageView) itemParent.findViewById(com.mst.R.id.menu_item_image);
    	TextView textView = (TextView) itemParent.findViewById(com.mst.R.id.menu_item_text);
    	if(iconView != null){
    		iconView.setEnabled(enabled);
    	}
    	if(textView != null){
    		textView.setEnabled(enabled);
    	}
    }
    
    public void clearItems(){
    	removeAllViews();
    	mMenu.clearAll();
    }
    
    public void removeItem(int id){
    	mMenu.removeItem(id);
    	updateMenuView(mMenu.getVisibleItems());
    }
    
    
    @Override
    public void setVisibility(int visibility) {
    	// TODO Auto-generated method stub
    	mVisibility = visibility;
    	mAnimationRunnable.setVisibility(mVisibility);
    	super.setVisibility(visibility);
    	postOnAnimation(mAnimationRunnable);
    }
    
    private static class AnimationRunnable implements Runnable , AnimationListener{

    	private WeakReference<BottomNavigationView> mView;
    	private Animation mAnimationIn,mAnimationOut;
    	private int visibility;
    	public  AnimationRunnable(BottomNavigationView view) {
    		mView = new WeakReference<BottomNavigationView>(view);
    		 mAnimationIn = AnimationUtils.loadAnimation(view.getContext(), com.mst.R.anim.slide_bottom_in);
    	        mAnimationOut = AnimationUtils.loadAnimation(view.getContext(), com.mst.R.anim.slide_bottom_out);
    	        mAnimationIn.setAnimationListener(this);
    	        mAnimationOut.setAnimationListener(this);
			// TODO Auto-generated constructor stub
		}
    	
    	public void setVisibility(int visibility){
    		this.visibility = visibility;
    	}
    	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			View view = mView.get();
			if(view == null){
				return;
			}
			if(visibility == View.VISIBLE){
				view.startAnimation(mAnimationIn);
			}else{
				view.startAnimation(mAnimationOut);
			}
			
		}
		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			BottomNavigationView view = mView.get();
			if(view != null){
				view.superVisibility(view.mVisibility);
			}
		}
		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			if(animation == mAnimationIn){
				BottomNavigationView view = mView.get();
				if(view != null){
					view.superVisibility(view.mVisibility);
				}
			}
			
		}
    	
    }
    
    private void superVisibility(int visibility){
//    	super.setVisibility(visibility);
    }

    
    public void showItem(int itemId,boolean show){
    	View itemView = findViewById(itemId);
    	if(itemView != null){
    		itemView.setVisibility(show?View.VISIBLE:View.GONE);
    		if(getChildCount() == 1){
    			super.setVisibility(View.GONE);
    		}
    	}
    }
    
	private void animateViewIn() {
		setTranslationY(getHeight());
				animate()
				.translationY(0f)
				.setInterpolator(
						mst.utils.AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
				.setDuration(333).setListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						// TODO Auto-generated method stub
						superVisibility(mVisibility);
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animator animation) {
						// TODO Auto-generated method stub

					}
				}).start();
	}

	private void animateViewOut() {
				animate()
				.translationY(getHeight())
				.setInterpolator(
						mst.utils.AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
				.setDuration(333)
				.setListener(new AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						// TODO Auto-generated method stub
						superVisibility(mVisibility);
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animator animation) {
						// TODO Auto-generated method stub

					}
				}).start();
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
        switch (MeasureSpec.getMode(heightSpec)) {
            case MeasureSpec.EXACTLY:
                // Nothing to do
                break;
            case MeasureSpec.AT_MOST:
            	heightSpec = MeasureSpec.makeMeasureSpec(
                        Math.min(MeasureSpec.getSize(heightSpec), mMaxHeight), MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.UNSPECIFIED:
            	heightSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.EXACTLY);
                break;
        }
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
        getMenuInflater().inflate(resId, mMenu);
        ArrayList<MstMenuItemImpl> menuItems = mMenu.getVisibleItems();
        updateMenuView(menuItems);
    }
    
     void updateMenuView(ArrayList<MstMenuItemImpl>  menuItems){
    	if(menuItems != null && menuItems.size() > 0){
    		int itemCount = menuItems.size();
    		for(int i = 0 ;i < itemCount ; i++){
    			View itemView = mInflater.inflate(com.mst.R.layout.bottom_navigation_menu_item, this,false);
    			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    			params.weight = 1;
    			params.gravity = Gravity.CENTER;
    			addView(itemView,params);
    			final MstMenuItemImpl item = menuItems.get(i);
    			itemView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if(mListener != null){
							mListener.onNavigationItemSelected(item);
						}
					}
				});
    			itemView.setId(item.getItemId());
    			setupMenuItem(item,itemView);
    		}
    		
    	}
    	updateMenuView();
    }

    private void setupMenuItem(MenuItem item,View itemView){
    	ImageView iconView = (ImageView) itemView.findViewById(com.mst.R.id.menu_item_image);
    	TextView textView = (TextView) itemView.findViewById(com.mst.R.id.menu_item_text);
    	iconView.setDuplicateParentStateEnabled(true);
    	textView.setDuplicateParentStateEnabled(true);
    	Drawable icon = item.getIcon();
    	if(icon != null){
	    	icon.mutate();
	    	icon.setBounds(0, 0, mIconSize, mIconSize);
	    	iconView.setImageDrawable(icon);
    	}else{
    		iconView.setVisibility(View.GONE);
    	}
    	if(TextUtils.isEmpty(item.getTitle())){
    		textView.setVisibility(View.GONE);
    	}else{
    		textView.setText(item.getTitle());
    	}
    	updateMenuView();
    }
    
    private void updateMenuView(){
    	int childCount = getChildCount();
    	if(childCount > 0){
    		for(int i = 0 ; i< childCount ;i++){
    			View itemView = getChildAt(i);
    			ImageView iconView = (ImageView) itemView.findViewById(com.mst.R.id.menu_item_image);
    	    	TextView textView = (TextView) itemView.findViewById(com.mst.R.id.menu_item_text);
    	    	textView.setTextAppearance(mTextAppearance);
    	    	if(iconView.getDrawable() != null){
    	    		iconView.getDrawable().setTintList(mIconColorTintList);
    	    	}
    	    	textView.setTextColor(mItemTextColor);
    		}
    	}
    }
    
    
    
    /**
     * Returns the {@link Menu} instance associated with this navigation view.
     */
    public Menu getMenu() {
        return mMenu;
    }


    /**
     * Returns the tint which is applied to our item's icons.
     *
     * @see #setItemIconTintList(ColorStateList)
     *
     * @attr ref R.styleable#NavigationView_itemIconTint
     */
    
    public ColorStateList getItemIconTintList() {
        return mIconColorTintList;
    }

    /**
     * Set the tint which is applied to our item's icons.
     *
     * @param tint the tint to apply.
     *
     * @attr ref R.styleable#NavigationView_itemIconTint
     */
    public void setItemIconTintList( ColorStateList tint) {
    	mIconColorTintList = tint;
    	updateMenuView();
    }

    /**
     * Returns the tint which is applied to our item's icons.
     *
     * @see #setItemTextColor(ColorStateList)
     *
     * @attr ref R.styleable#NavigationView_itemTextColor
     */
    
    public ColorStateList getItemTextColor() {
        return mItemTextColor;
    }

    /**
     * Set the text color which is text to our items.
     *
     * @see #getItemTextColor()
     *
     * @attr ref R.styleable#NavigationView_itemTextColor
     */
    public void setItemTextColor( ColorStateList textColor) {
    	mItemTextColor = textColor;
    	updateMenuView();
    }

    /**
     * Returns the background drawable for the menu items.
     *
     * @see #setItemBackgroundResource(int)
     *
     * @attr ref R.styleable#NavigationView_itemBackground
     */
    public Drawable getItemBackground() {
        return null;
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
    	
    }

    /**
     * Sets the currently checked item in this navigation menu.
     *
     * @param id The item ID of the currently checked item.
     */
    public void setCheckedItem( int id) {
        MenuItem item = mMenu.findItem(id);
        if (item != null) {
            
        }
    }

    /**
     * Set the text appearance of the menu items to a given resource.
     *
     * @attr ref R.styleable#NavigationView_itemTextAppearance
     */
    public void setItemTextAppearance( int resId) {
    	mTextAppearance = resId;
    	updateMenuView();
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