package com.android.systemui.recents.views;

import com.android.systemui.recents.Constants;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewOutlineProvider;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.systemui.R;
import mst.widget.recycleview.RecyclerView.LayoutParams;

public class MstTaskView extends FrameLayout implements Task.TaskCallbacks, 
	View.OnClickListener, View.OnLongClickListener {
	
    View mContent;
    View mLockView;
    
    @ViewDebug.ExportedProperty(deepExport=true, prefix="task_")
    private Task mTask;
    @ViewDebug.ExportedProperty(category="recents")
    private boolean mTaskDataLoaded;
    @ViewDebug.ExportedProperty(category="recents")
    private boolean mClipViewInStack = true;
    @ViewDebug.ExportedProperty(category="recents")
    private boolean mTouchExplorationEnabled;
    @ViewDebug.ExportedProperty(category="recents")
    private boolean mIsDisabledInSafeMode;
    
    @ViewDebug.ExportedProperty(category="recents")
    private Point mDownTouchPos = new Point();
    
    @ViewDebug.ExportedProperty(deepExport=true, prefix="thumbnail_")
    MstTaskViewThumbnail mThumbnailView;
    @ViewDebug.ExportedProperty(deepExport=true, prefix="header_")
    MstTaskViewHeader mHeaderView;
    private View mIncompatibleAppToastView;
    
    private Toast mDisabledAppToast;
	
    public MstTaskView(Context context) {
        this(context, null);
    }

    public MstTaskView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MstTaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MstTaskView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOnLongClickListener(this);
    }
    
    /**
     * Called from RecentsActivity when it is relaunched.
     */
    void onReload(boolean isResumingFromVisible) {
        if (!isResumingFromVisible) {
            resetViewProperties();
        }
    }
    
    /** Gets the task */
    Task getTask() {
        return mTask;
    }
    
    @Override
    protected void onFinishInflate() {
    	super.onFinishInflate();
        // Bind the views
        mContent = findViewById(R.id.task_view_content);
        mHeaderView = (MstTaskViewHeader) findViewById(R.id.task_view_bar);
        mThumbnailView = (MstTaskViewThumbnail) findViewById(R.id.task_view_thumbnail);
        //mThumbnailView.updateClipToTaskBar(mHeaderView);
        mLockView = findViewById(R.id.lockicon);
    }
    
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
    	// TODO Auto-generated method stub
    	super.onConfigurationChanged(newConfig);
//    	Log.d("111111", "---MstTaskView onConfigurationChanged orientation = " + newConfig.orientation);
//    	mst.widget.recycleview.RecyclerView.LayoutParams layoutparam = (mst.widget.recycleview.RecyclerView.LayoutParams)getLayoutParams();
//    	if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//    		layoutparam.topMargin = (int)Utilities.dpToPx(this.getResources(), 95);
//    		layoutparam.height = (int)Utilities.dpToPx(this.getResources(), 368);
//    	} else {
//    		layoutparam.topMargin = (int)Utilities.dpToPx(this.getResources(), 20);
//    		layoutparam.height = (int)Utilities.dpToPx(this.getResources(), 230);
//    	}
//    	setMinimumHeight(layoutparam.height);
//    	setLayoutParams(layoutparam);
    }
    
    /**
     * Update the task view when the configuration changes.
     */
    void onConfigurationChanged() {
        mHeaderView.onConfigurationChanged();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            mHeaderView.onTaskViewSizeChanged(w, h);
            mThumbnailView.onTaskViewSizeChanged(w, h);
        }
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mDownTouchPos.set((int) (ev.getX() * getScaleX()), (int) (ev.getY() * getScaleY()));
        }
        return super.onInterceptTouchEvent(ev);
    }
    
    /** Resets this view's properties */
    void resetViewProperties() {
        setVisibility(View.VISIBLE);
        getHeaderView().reset();

        if (mIncompatibleAppToastView != null) {
            mIncompatibleAppToastView.setVisibility(View.INVISIBLE);
        }
    }
    
    /** Enables/disables handling touch on this task view. */
    void setTouchEnabled(boolean enabled) {
        setOnClickListener(enabled ? this : null);
    }
    
    public MstTaskViewHeader getHeaderView() {
        return mHeaderView;
    }
    
    /**** TaskCallbacks Implementation ****/

    public void onTaskBound(Task t, boolean touchExplorationEnabled, int displayOrientation,
            Rect displayRect) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        mTouchExplorationEnabled = touchExplorationEnabled;
        mTask = t;
        mTask.addCallback(this);
        mIsDisabledInSafeMode = !mTask.isSystemApp && ssp.isInSafeMode();
        mThumbnailView.bindToTask(mTask, mIsDisabledInSafeMode, displayOrientation, displayRect);
        mHeaderView.bindToTask(mTask, mTouchExplorationEnabled, mIsDisabledInSafeMode);
        
    	/**Mst: tangjun add for lockView begin*/
     	String key = RecentsConfiguration.getRecentsAppsKey(mTask);
    	if(RecentsConfiguration.readRecentsAppsLockState(getContext(), key)){
    		mLockView.setVisibility(View.VISIBLE);
    	}else{
    		mLockView.setVisibility(View.GONE);
    	}
    	/**Mst: tangjun add for lockView end*/

        if (!t.isDockable && ssp.hasDockedTask()) {
            if (mIncompatibleAppToastView == null) {
                mIncompatibleAppToastView = Utilities.findViewStubById(this,
                        R.id.incompatible_app_toast_stub).inflate();
                TextView msg = (TextView) findViewById(com.android.internal.R.id.message);
                msg.setText(R.string.recents_incompatible_app_message);
            }
            mIncompatibleAppToastView.setVisibility(View.VISIBLE);
        } else if (mIncompatibleAppToastView != null) {
            mIncompatibleAppToastView.setVisibility(View.INVISIBLE);
        }
    }
    
    @Override
    public void onTaskDataLoaded(Task task, ActivityManager.TaskThumbnailInfo thumbnailInfo) {
        // Update each of the views to the new task data
        mThumbnailView.onTaskDataLoaded(thumbnailInfo);
        mHeaderView.onTaskDataLoaded();
        mTaskDataLoaded = true;
    }
    

    @Override
    public void onTaskDataUnloaded() {
        // Unbind each of the views from the task and remove the task callback
        mTask.removeCallback(this);
        mThumbnailView.unbindFromTask();
        mHeaderView.unbindFromTask(mTouchExplorationEnabled);
        mTaskDataLoaded = false;
    }

    @Override
    public void onTaskStackIdChanged() {
        // Force rebind the header, the thumbnail does not change due to stack changes
        mHeaderView.bindToTask(mTask, mTouchExplorationEnabled, mIsDisabledInSafeMode);
        mHeaderView.onTaskDataLoaded();
    }

	/**** View.OnClickListener Implementation ****/
    @Override
    public void onClick(final View v) {
        if (mIsDisabledInSafeMode) {
            Context context = getContext();
            String msg = context.getString(R.string.recents_launch_disabled_message, mTask.title);
            if (mDisabledAppToast != null) {
                mDisabledAppToast.cancel();
            }
            mDisabledAppToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            mDisabledAppToast.show();
            return;
        }
   }

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}
}
