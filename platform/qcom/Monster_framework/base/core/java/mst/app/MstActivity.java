package mst.app;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import mst.utils.SystemConfig;
import mst.view.menu.MstMenuBuilder;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.ActionModeimpl;
import mst.widget.FloatingActionMode;
import mst.widget.FloatingToolbar;
import mst.widget.StandaloneActionMode;
import mst.widget.toolbar.Toolbar;
import mst.widget.toolbar.Toolbar.OnMenuItemClickListener;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.android.internal.widget.ActionBarContextView;
import com.mst.internal.widget.ActionModeContextView;




/**
 * This  is an abstract Activity,declare a Toolbar to replace ActionBar.
 * You should extends this,and initial UI in  {@link #initialUI(Bundle)}.
 * How to do:
 * <p>
 * <li>1. Toolbar's Id must be com.mst.R.id.toolbar,you need code like :</br>
 *  <pre>
 * 	&lt;mst.widget.Toolbar 
 *   		android:id="@com.mst:id/toolbar"
 *  		android:layout_width="match_parent"
 *   		android:layout_height="?android:attr/actionBarSize"
 * 	/&gt;
 * <li>2.Handle Menu Item click event in {@link #onMenuItemClick(MenuItem)}
 * <li>3.Handle Navigation Icon click event int {@link #onNavigationClicked(View)}
 * <li>4.There is no need to initialize Toolbar,just call {@link #getToolbar()} Method when 
 * 	you need it.
 *
 */
public abstract class MstActivity extends Activity implements OnMenuItemClickListener{
	
	private static final String TAG = "MstActivity";
	
	private Toolbar mToolbar;
	
	private Bundle mSavedInstanceState;
	
	private boolean mIsActionMode = false;
	
	private boolean mHasToolbar = true;
	
	private boolean mNeedBackIcon = false;
	
	private boolean mToolbarOverlay = false;
	
	private boolean mShowActionMode;
	
	private int mLayoutRes;
	
	private ActionMode mPrimaryActionMode;
	
	
	private Handler mLoadUIHandler = new Handler();
	
	private MstMenuBuilder mMenu;
	
	private FrameLayout mContent;
	private Window mWindow;
	private ActionModeContextView mPrimaryActionModeView;
    private PopupWindow mPrimaryActionModePopup;
    private Runnable mShowPrimaryActionModePopup;
    private ObjectAnimator mFadeAnim;
	private ActionModeCallBack mActionModeCallback = new ActionModeCallBack();
	
	private Runnable mLoadUIRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			initialUI(mSavedInstanceState);
		}
	};

	private Runnable mActionModeRunnable = new Runnable(){
		public void run() {
			if(mPrimaryActionMode != null){
				mPrimaryActionMode.setShow(mShowActionMode);
			}
	    	if(mShowActionMode){
	    		if (mPrimaryActionMode != null) {
	    			setHandledPrimaryActionMode(mPrimaryActionMode);
	            } 
	    	}else{
	    		dismissActionMode();
	    	}
		};
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		readThemeAttributes();
		initialWindowParams(getWindow());
		mWindow = getWindow();
		super.onCreate(savedInstanceState);
		mSavedInstanceState = savedInstanceState;
		if(mLayoutRes != 0){
			setContentView(mLayoutRes);
		}
		
		if(mHasToolbar){
			ViewStub stub = (ViewStub) findViewById(com.mst.R.id.toolbar_stub);
			stub.inflate();
			getToolbar();
		}
		
		if(mLayoutRes != 0){
			mContent = (FrameLayout)findViewById(com.mst.R.id.content);
		}
		getActionMode();
	}
	
	private void logD(String msg){
		if(SystemConfig.DEBUG){
			Log.d(TAG, msg);
		}
	}
	
	/**
	 * Reset window attributes here.
	 * @param window
	 */
	protected void initialWindowParams(Window window){
		
	}
	
	private void readThemeAttributes(){
		TypedArray a = getTheme().obtainStyledAttributes(null, com.mst.R.styleable.WindowStyle,
				com.mst.R.attr.windowLayoutStyle, com.mst.R.style.WindowStyle_Light);
		
		mHasToolbar = a.getBoolean(com.mst.R.styleable.WindowStyle_windowHasToolbar,true );
		
		mLayoutRes = a.getResourceId(com.mst.R.styleable.WindowStyle_windowLayout, 0);
		
		mNeedBackIcon = a.getBoolean(com.mst.R.styleable.WindowStyle_hasBackIcon, false);
		
		mToolbarOverlay = a.getBoolean(com.mst.R.styleable.WindowStyle_toolbarOverlay,false);
		
		if(mToolbarOverlay){
			mLayoutRes = com.mst.internal.R.layout.screen_simple_overlay;
		}
		a.recycle();
		
		if(SystemConfig.DEBUG){
			Log.d(TAG, "read window attr from theme:   hasToolbar--->"+mHasToolbar+"   hasLayoutRes--->"
					+(mLayoutRes != 0));
		}
	}
	
	public void showBackIcon(boolean show){
		mNeedBackIcon = show;
		getToolbar();
	}
	
	/**
	 * Initial all UI here.in the past code style,we will initial Views 
	 * in {@link #onCreate(Bundle)} Method. from now on, 
	 * do it at here.before work here,you have to call {@link #setContentView}
	 * method in onCreate().
	 * @param savedInstanceState
	 */
	protected void initialUI(Bundle savedInstanceState){
		
	}
	
	private void postLoadUI(){
		getWindow().getDecorView().post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mLoadUIHandler.post(mLoadUIRunnable);
			}
		});
	}
	
	
	public void setMstContentView(int layoutResID) {
		// TODO Auto-generated method stub
		if(mContent != null){
			mContent.removeAllViews();
			getLayoutInflater().inflate(layoutResID, mContent);
		}else{
			super.setContentView(layoutResID);
		}
		
		postLoadUI();
	}
	
	public void setMstContentView(View view){
		if(mContent != null){
			mContent.removeAllViews();
			mContent.addView(view, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
		}else{
			super.setContentView(view);
		}
		postLoadUI();
	}
	
	@Override
	public void setTitle(CharSequence title){
		if(getToolbar() != null){
			getToolbar().setTitle(title);
		}
	}

	/**
	 * Gets Toolbar in current Activity
	 * @return null if no Toolbar
	 */
	public Toolbar getToolbar(){
		if(mToolbar == null){
			mToolbar = (Toolbar)findViewById(com.mst.R.id.toolbar);
			mToolbar.setOnMenuItemClickListener(this);
			mToolbar.setNavigationOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View view) {
					// TODO Auto-generated method stub
					onNavigationClicked(view);
				}
			});
		}
		
		if(mNeedBackIcon){
			mToolbar.setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);
		}
		return mToolbar;
	}
	
	/**
	 * Inflate menu item into Toolbar 
	 * ,see{@link mst.widget.toolbar.Toolbar#inflateMenu(int resId)}
	 * @param resId
	 */
	public void inflateToolbarMenu(int resId){
		if(getToolbar() != null){
			getToolbar().inflateMenu(resId);
		}
	}
	
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Handle NavigationIcon click event here
	 * @param view NavigationIconView
	 */
	public void onNavigationClicked(View view){
		
	}
	
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK && mIsActionMode){
			showActionMode(false);
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	

	/**
	 * Bind callback to ActionMode,call this method to set callback
	 * to ActionMode,you can handle ActionMode Item Click in {@link mst.widget.ActionModeListener}
	 * @param listener
	 */
    public void setActionModeListener(ActionModeListener listener){
    	getActionMode().bindActionModeListener(listener);
    }
    
    
    public void setupActionModeWithDecor(View decor){
    	getActionMode().setupDecor(decor);
    }
    

    private void setHandledPrimaryActionMode(ActionMode mode) {
        mPrimaryActionMode = mode;
        mPrimaryActionMode.invalidate();
        mPrimaryActionModeView.initForMode(mPrimaryActionMode);
        if (mPrimaryActionModePopup != null) {
        	mPrimaryActionModePopup.showAtLocation(getWindow().getDecorView(),
                    Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);
        }
        mPrimaryActionModeView.sendAccessibilityEvent(
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    boolean shouldAnimatePrimaryActionModeView() {
        // We only to animate the action mode in if the decor has already been laid out.
        // If it hasn't been laid out, it hasn't been drawn to screen yet.
        return getWindow().getDecorView().isLaidOut();
    }
    

    private ActionMode createStandaloneActionMode(ActionMode.Callback callback) {
        if (mPrimaryActionModeView == null || mPrimaryActionModePopup == null) {
                // Use the tool bar theme.
                final TypedValue outValue = new TypedValue();
                final Resources.Theme baseTheme = getTheme();
                baseTheme.resolveAttribute(com.android.internal.R.attr.toolbarStyle, outValue, true);

                final Context actionBarContext = this;

                mPrimaryActionModeView = new ActionModeContextView(actionBarContext);
                mPrimaryActionModePopup = new PopupWindow(actionBarContext, null,
                		com.android.internal.R.attr.actionModePopupWindowStyle);
                mPrimaryActionModePopup.setWindowLayoutType(
                        WindowManager.LayoutParams.TYPE_APPLICATION_ABOVE_SUB_PANEL);
                mPrimaryActionModePopup.setContentView(mPrimaryActionModeView);
                mPrimaryActionModePopup.setWidth(MATCH_PARENT);

                actionBarContext.getTheme().resolveAttribute(
                		com.android.internal.R.attr.actionBarSize, outValue, true);
                final int height = TypedValue.complexToDimensionPixelSize(outValue.data,
                        actionBarContext.getResources().getDisplayMetrics());
                mPrimaryActionModeView.setContentHeight(height);
                mPrimaryActionModePopup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        if (mPrimaryActionModeView != null) {
            mPrimaryActionModeView.killMode();
            ActionMode mode = new StandaloneActionMode(
                    mPrimaryActionModeView.getContext(), mPrimaryActionModeView,
                    callback, mPrimaryActionModePopup == null);
        		View actionModeCusView = getLayoutInflater().inflate(com.mst.R.layout.toolbar_actionmode_layout, null);
        		mode.setCustomView(actionModeCusView);
            return mode;
        }
        return null;
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	cleanupPrimaryActionMode();
    }
    
    
    private void cleanupPrimaryActionMode() {
        if (mPrimaryActionMode != null) {
            mPrimaryActionMode.finish();
            mPrimaryActionMode = null;
        }
        if (mPrimaryActionModeView != null) {
            mPrimaryActionModeView.killMode();
        }
    }
    
    /**
     * Get current ActionMode
     * @return
     */
    public ActionMode getActionMode(){
    	
    	if(mPrimaryActionMode == null){
    		mPrimaryActionMode = createActionMode();
    	}
    	return mPrimaryActionMode;
    }
    
    
    private ActionMode createActionMode(){
    	return createStandaloneActionMode(mActionModeCallback);
    }
    
    
    /**
     * Show ActionMode to handle data option
     * @param show true to show,or dismiss
     */
    public void showActionMode(boolean show){
    	mShowActionMode = show;
    	showActionMode();
    }
    
    private void showActionMode(){
    	mLoadUIHandler.post(mActionModeRunnable);
    }
    
    private void dismissActionMode(){
    	 if (mPrimaryActionModePopup != null) {
             getWindow().getDecorView().removeCallbacks(mShowPrimaryActionModePopup);
         }
    	 if (mPrimaryActionModePopup != null) {
             mPrimaryActionModePopup.dismiss();
         }
    
    }
    
    /**
     * Update ActionMode's title.
     * @param title
     */
    public void updateActionModeTitle(CharSequence title){
    	getActionMode().updateTitle(title);
    }
    
    /**
     * Get the show Status of current ActionMode.
     * @return true if ActionMode is Showing,or false
     */
    public boolean isActionModeShowing(){
    	return getActionMode().isShowing();
    }
	
    /**
     * Get current OptionMenu,this Menu is attached 
     * to Toolbar.
     * @return menu attached to Toolbar
     */
    public Menu getOptionMenu(){
    	if(mToolbar != null){
    		return mToolbar.getMenu();
    	}
    	return null;
    }
    
    /**
     * Update your menu here,you can add new menu item by {@link android.view.Menu.addItem},or
     * remove old item by {@link android.view.Menu.removeItem} or {@link android.view.Menu.removeGroup}.
     * Before update Menu,Menu instance should be initial by call {@link #getOptionMenu()}.
     */
    public void updateOptionMenu(){
    	
    }

    
    class ActionModeCallBack extends android.view.ActionMode.Callback2{

		@Override
		public boolean onCreateActionMode(
				android.view.ActionMode actionMode, Menu menu) {
			// TODO Auto-generated method stub
//			MenuInflater inflater = actionMode.getMenuInflater();
//			inflater.inflate(com.mst.internal.R.menu.edit_mode_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(
				android.view.ActionMode paramActionMode, Menu paramMenu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(
				android.view.ActionMode paramActionMode, MenuItem paramMenuItem) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onDestroyActionMode(android.view.ActionMode paramActionMode) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
	
}
