package com.monster.permission.ui;

import android.annotation.Nullable;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import mst.widget.recycleview.RecyclerView;
import mst.widget.recycleview.RecyclerView.AdapterDataObserver;
import mst.preference.PreferenceScreen;
import mst.preference.PreferenceFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.monster.appmanager.R;
import com.monster.appmanager.utils.Utils;

public abstract class PermissionsFrameFragment extends PreferenceFragment implements MstPermission.OnPermChangeListener {
	private boolean allowNoneItem = false;
    private static final float WINDOW_ALIGNMENT_OFFSET_PERCENT = 50;

    private ViewGroup mPreferencesContainer;

    private View mLoadingView;
    private ViewGroup mPrefsView;
    private boolean mIsLoading;
    protected MstPermission mMstPermission;

    /**
     * Returns the view group that holds the preferences objects. This will
     * only be set after {@link #onCreateView} has been called.
     */
    protected final ViewGroup getPreferencesContainer() {
        return mPreferencesContainer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.permissions_frame, container,
                        false);
        mPrefsView = (ViewGroup) rootView.findViewById(R.id.prefs_container);
        if (mPrefsView == null) {
            mPrefsView = rootView;
        }
        mLoadingView = rootView.findViewById(R.id.loading_container);
        mPreferencesContainer = (ViewGroup) super.onCreateView(
                inflater, mPrefsView, savedInstanceState);
        setLoading(mIsLoading, false, true /* force */);
        mPrefsView.addView(mPreferencesContainer);
        return rootView;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	onCreatePreferences();
    	
        mMstPermission = new MstPermission(getContext());
        mMstPermission.setmListener(this);
    }
    
    public void onCreatePreferences() {
        PreferenceScreen preferences = getPreferenceScreen();
        if (preferences == null) {
            preferences = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(preferences);
        }
    }

    protected void setLoading(boolean loading, boolean animate) {
        setLoading(loading, animate, false);
    }

    private void setLoading(boolean loading, boolean animate, boolean force) {
        if (mIsLoading != loading || force) {
            mIsLoading = loading;
            if (getView() == null) {
                // If there is no created view, there is no reason to animate.
                animate = false;
            }
            if (mPrefsView != null) {
                setViewShown(mPrefsView, !loading, animate);
            }
            if (mLoadingView != null) {
                setViewShown(mLoadingView, loading, animate);
            }
        }
    }

    private void setViewShown(final View view, boolean shown, boolean animate) {
        if (animate) {
            Animation animation = AnimationUtils.loadAnimation(getContext(),
                    shown ? android.R.anim.fade_in : android.R.anim.fade_out);
            if (shown) {
                view.setVisibility(View.VISIBLE);
            } else {
                animation.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.setVisibility(View.INVISIBLE);
                    }
                });
            }
            view.startAnimation(animation);
        } else {
            view.clearAnimation();
            view.setVisibility(shown ? View.VISIBLE : View.INVISIBLE);
        }
    }
    

    /**
     * Hook for subclasses to change the default text of the empty view.
     * Base implementation leaves the default empty view text.
     *
     * @param textView the empty text view
     */
    protected void onSetEmptyText() {
    	emptyTextHandler.sendEmptyMessage(0);
    }
    
    private Handler emptyTextHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		if(!isAllowNoneItem()) {
    			return;
    		}
            boolean isEmpty = getListView().getChildCount()==0;
            TextView emptyView = (TextView) mPrefsView.findViewById(R.id.no_permissions);
            if(emptyView!=null){
                emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            }
    	};
    };
    
    protected void hideEmptyText() {
    	TextView emptyView = (TextView) mPrefsView.findViewById(R.id.no_permissions);
        if(emptyView!=null){
            emptyView.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	onRefreshPermission();
    }
    
    @Override
	public void onPermRefreshComplete() { }

	@Override
	public void onPermRefreshError() { }
	
	protected void onRefreshPermission() { }

	public boolean isAllowNoneItem() {
		return allowNoneItem;
	}

	public void setAllowNoneItem(boolean allowNoneItem) {
		this.allowNoneItem = allowNoneItem;
	}
}

