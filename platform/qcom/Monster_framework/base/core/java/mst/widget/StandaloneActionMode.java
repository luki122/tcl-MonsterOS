package mst.widget;

import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuPopupHelper;
import com.android.internal.view.menu.SubMenuBuilder;
import com.android.internal.widget.ActionBarContextView;
import com.mst.internal.widget.ActionModeContextView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.ArrayMap;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import mst.view.menu.MstMenuBuilder;
import mst.view.menu.MstMenuPopupHelper;
import mst.view.menu.MstSubMenuBuilder;


public class StandaloneActionMode extends ActionMode implements MstMenuBuilder.Callback {
    private Context mContext;
    private ActionModeContextView mContextView;
    private ActionMode.Callback mCallback;
    private WeakReference<View> mCustomView;
    private boolean mFinished;
    private boolean mFocusable;

    private MstMenuBuilder mMenu;
    
    private TextView mTitleView;
	private Button mNagativeView, mPositiveView;
	private CharSequence mTitle;
	private CharSequence mPositiveText;
	private CharSequence mNagativeText;
	
	private ActionModeListener mListener;
	private ArrayMap<Integer,Item> mItems = new ArrayMap<Integer,Item>();
	
	private OnClickListener mEditWidgetClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (mListener != null) {
				mListener.onActionItemClicked(mItems.get(v.getId()));
			}
		}
	};

    public StandaloneActionMode(Context context, ActionModeContextView view,
            ActionMode.Callback callback, boolean isFocusable) {
        mContext = context;
        mContextView = view;
        mCallback = callback;

        mMenu = new MstMenuBuilder(view.getContext()).setDefaultShowAsAction(
                        MenuItem.SHOW_AS_ACTION_IF_ROOM);
        mMenu.setCallback(this);
        mFocusable = isFocusable;
        
    }

    @Override
    public void setTitle(CharSequence title) {
    	updateTitle(title);
    }

    @Override
    public void setSubtitle(CharSequence subtitle) {
    	//do nothing
    }

    @Override
    public void setTitle(int resId) {
        setTitle(resId != 0 ? mContext.getString(resId) : null);
    }

    @Override
    public void setSubtitle(int resId) {
    	//do nothing
    }

    @Override
    public void setTitleOptionalHint(boolean titleOptional) {
    	//do nothing
    }

    @Override
    public boolean isTitleOptional() {
        return mContextView.isTitleOptional();
    }

    @Override
    public void setCustomView(View view) {
        mContextView.setCustomView(view);
        mCustomView = view != null ? new WeakReference<View>(view) : null;
        setupEditWidget();
    }

    
    
    private void setupEditWidget(){
    	TypedArray a = mContext.getTheme().obtainStyledAttributes(
                null, com.mst.internal.R.styleable.ActionMode, com.mst.R.attr.actionModeStyle, com.mst.R.style.ActionMode_Light);
		mTitle = a.getText(com.mst.internal.R.styleable.ActionMode_title);
		if(mTitle != null){
			setTitle(mTitle);
		}
		mPositiveText = a.getText(com.mst.internal.R.styleable.ActionMode_actionPositiveText);
		mNagativeText = a.getText(com.mst.internal.R.styleable.ActionMode_actionNagativeText);
		if(mPositiveText != null){
			setPositiveText(mPositiveText);
		}
		
		if(mNagativeText != null){
			setNagativeText(mNagativeText);
		}
		a.recycle();
		
		
		
		
    }
    @Override
    public void invalidate() {
        mCallback.onPrepareActionMode(this, mMenu);
    }

    @Override
    public void finish() {
        if (mFinished) {
            return;
        }
        mFinished = true;

        mContextView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        mCallback.onDestroyActionMode(this);
    }

    @Override
    public Menu getMenu() {
        return mMenu;
    }

    @Override
    public CharSequence getTitle() {
        return mTitle;
    }

    @Override
    public CharSequence getSubtitle() {
        return null;
    }

    @Override
    public View getCustomView() {
        return mCustomView != null ? mCustomView.get() : null;
    }

    @Override
    public MenuInflater getMenuInflater() {
        return new MenuInflater(mContextView.getContext());
    }

    public boolean onMenuItemSelected(MstMenuBuilder menu, MenuItem item) {
        return mCallback.onActionItemClicked(this, item);
    }

    public void onCloseMenu(MstMenuBuilder menu, boolean allMenusAreClosing) {
    }

    public boolean onSubMenuSelected(MstSubMenuBuilder subMenu) {
        if (!subMenu.hasVisibleItems()) {
            return true;
        }

        new MstMenuPopupHelper(mContextView.getContext(), subMenu).show();
        return true;
    }

    public void onCloseSubMenu(MstSubMenuBuilder menu) {
    }

    public void onMenuModeChange(MstMenuBuilder menu) {
        invalidate();
        mContextView.showOverflowMenu();
    }

    public boolean isUiFocusable() {
        return mFocusable;
    }

	@Override
	public void prepareActionMode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTitle(CharSequence title) {
		// TODO Auto-generated method stub
		mTitleView = (TextView) mContextView.findViewById(com.android.internal.R.id.title);
		if(mTitleView != null){
			mTitleView.setText(title);
		}
	}

	@Override
	public void bindActionModeListener(ActionModeListener listener) {
		// TODO Auto-generated method stub
		mListener = listener;
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isShowing() {
		// TODO Auto-generated method stub
		return super.isShowing();
	}

	@Override
	public void setPositiveText(CharSequence text) {
		// TODO Auto-generated method stub
		if(mContextView == null){
			return;
		}
		mPositiveView = (Button) mContextView.findViewById(android.R.id.text2);
		if(mPositiveView != null){
			mPositiveView.setOnClickListener(mEditWidgetClickListener);
			if(mItems.get(mPositiveView.getId()) == null){
				Item positiveItem = new Item();
				positiveItem.itemView = mPositiveView;
				mItems.put(mPositiveView.getId(),positiveItem);
			}
			mPositiveView.setText(text);
		}
	}

	@Override
	public void setNagativeText(CharSequence text) {
		// TODO Auto-generated method stub
		if(mContextView == null){
			return;
		}
		mNagativeView = (Button) mContextView.findViewById(android.R.id.text1);
		if(mNagativeView != null){
			mNagativeView.setOnClickListener(mEditWidgetClickListener);
			if(mItems.get(mNagativeView.getId()) == null){
				Item nagativeItem = new Item();
				nagativeItem.itemView = mNagativeView;
				mItems.put(mNagativeView.getId(),nagativeItem);
			}
			mNagativeView.setText(text);
		}
	}

	@Override
	public void setupDecor(View decor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showItem(int itemId,boolean show) {
		View view = mContextView.findViewById(itemId);
		if(view != null){
			view.setVisibility(show?View.VISIBLE:View.GONE);
		}
		
	}
	
	
	@Override
	public void enableItem(int itemId, boolean enabled) {
		// TODO Auto-generated method stub
		View view = mContextView.findViewById(itemId);
		if(view != null){
			view.setEnabled(enabled);
		}
	}
	
	
	@Override
	public void setItemTextAppearance(int itemId, int textAppearanceId) {
		// TODO Auto-generated method stub
		View view = mContextView.findViewById(itemId);
		if(view != null && view instanceof TextView){
			((TextView)view).setTextAppearance(textAppearanceId);
		}
	}
	
	
	
	
	
	
	
	
}