/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.recents.views;

import static android.app.ActivityManager.StackId.INVALID_STACK_ID;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.ActivityOptions.OnAnimationStartedListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewOutlineProvider;
import android.view.ViewPropertyAnimator;
import android.view.WindowInsets;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.RecentsDebugFlags;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.DockedFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.HideStackActionButtonEvent;
import com.android.systemui.recents.events.activity.LaunchNextTaskRequestEvent;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.ShowStackActionButtonEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEndedEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.misc.RecentsMemoryInfo;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.MstClearAllView.OnAnimEndListener;
import com.android.systemui.recents.views.MstTaskAdapter.OnItemListener;
import com.android.systemui.recents.views.RecentsTransitionHelper.AnimationSpecComposer;
import com.android.systemui.stackdivider.WindowManagerProxy;
import com.android.systemui.statusbar.FlingAnimationUtils;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import mst.widget.recycleview.RecyclerView;
import mst.widget.recycleview.RecyclerView.OnScrollListener;
import mst.widget.recycleview.LinearLayoutManager;

/**
 * This view is the the top level layout that contains TaskStacks (which are laid out according
 * to their SpaceNode bounds.
 */
public class RecentsView extends FrameLayout {

    private static final String TAG = "RecentsView";

    private static final int DEFAULT_UPDATE_SCRIM_DURATION = 200;
    private static final float DEFAULT_SCRIM_ALPHA = 0.33f;

    private static final int SHOW_STACK_ACTION_BUTTON_DURATION = 134;
    private static final int HIDE_STACK_ACTION_BUTTON_DURATION = 100;

    private TaskStack mStack;
    private TaskStackView mTaskStackView;
    private TextView mStackActionButton;
    private TextView mEmptyView;

    private boolean mAwaitingFirstLayout = true;
    private boolean mLastTaskLaunchedWasFreeform;

    @ViewDebug.ExportedProperty(category="recents")
    private Rect mSystemInsets = new Rect();
    private int mDividerSize;

    private Drawable mBackgroundScrim = new ColorDrawable(
            Color.argb((int) (DEFAULT_SCRIM_ALPHA * 255), 0, 0, 0)).mutate();
    private Animator mBackgroundScrimAnimator;

    private RecentsTransitionHelper mTransitionHelper;
    @ViewDebug.ExportedProperty(deepExport=true, prefix="touch_")
    private RecentsViewTouchHandler mTouchHandler;
    private final FlingAnimationUtils mFlingAnimationUtils;
    
    /**Mst: tangjun add to get tasks begin*/
    private ArrayList<Task> mMstTaskList = null;
    private MstTaskAdapter mAdapter;
    private MstTaskStackView mstTaskStackView;
    private int touchX;
    private int touchY;
    private boolean mIsTranslateY;
    private float mPagingTouchSlop;
    private VelocityTracker mVelocityTracker;
    private View mClearAllView;
    private MstClearAllView mClearAllImageView;
    private TextView mClearAllTextView;
    private AnimationDrawable mAnimationDrawable;
    private LinearLayoutManager mLinearLayoutManager;
    private boolean mSelfScroll;
    private int mItemWidth_0; 	//the width of item 0
    private int mItemWidth_normal; 	//the width of item 0
    private int mItemScroll_distance;
    private int totalDx = 0;; //RecylerView Distance
    private static final int MAX_DISTANCE = 1000;
    private Context mContext;
    private static final String LAUNCHER_PACKAGE_NAME = "com.monster.launcher";
    private TextView mEmptyView_Mst;
    private View mRecylerview_frame;
    private float mTotalMemSize;
    private float mTotalMemSizeGB;
    private boolean mClearAllAnimRun = false;
    /**Mst: tangjun add to get tasks begin*/

    public RecentsView(Context context) {
        this(context, null);
    }

    public RecentsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecentsView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecentsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);

        SystemServicesProxy ssp = Recents.getSystemServices();
        mTransitionHelper = new RecentsTransitionHelper(getContext());
        mDividerSize = ssp.getDockedDividerSize(context);
        mTouchHandler = new RecentsViewTouchHandler(this);
        mFlingAnimationUtils = new FlingAnimationUtils(context, 0.3f);

        LayoutInflater inflater = LayoutInflater.from(context);
        if (RecentsDebugFlags.Static.EnableStackActionButton) {
            float cornerRadius = context.getResources().getDimensionPixelSize(
                    R.dimen.recents_task_view_rounded_corners_radius);
            mStackActionButton = (TextView) inflater.inflate(R.layout.recents_stack_action_button,
                    this, false);
            mStackActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().send(new DismissAllTaskViewsEvent());
                }
            });
            /**Mst: tangjun mod begin*/
            //addView(mStackActionButton);
            /**Mst: tangjun mod end*/
            mStackActionButton.setClipToOutline(true);
            mStackActionButton.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
                }
            });
        }
        mEmptyView = (TextView) inflater.inflate(R.layout.recents_empty, this, false);
        /**Mst: tangjun mod begin*/
        //addView(mEmptyView);
        /**Mst: tangjun mod end*/
        
        /**Mst: tangjun add begin*/
        mPagingTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mItemWidth_0 = context.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_first_right_see_padding) + 
        		context.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_normal_paddingleft) + 
        		context.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_width);
        mItemWidth_normal = context.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_normal_paddingleft) + 
        		context.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_width);
        mItemScroll_distance = mItemWidth_0 - context.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_first_right_see_padding);
        mContext = context;
        mTotalMemSize = RecentsMemoryInfo.getmem_total();
        mTotalMemSizeGB = mTotalMemSize / 1024 / 1024;
        mTotalMemSizeGB = ((float)Math.round(mTotalMemSizeGB*10))/10;
        /**Mst: tangjun add end*/
    }
    
    /**Mst: tangjun add for recent button click begin*/
    public final void onBusEvent(LaunchNextTaskRequestEvent event) {
        int launchTaskIndex = mStack.indexOfStackTask(mStack.getLaunchTarget());
        Log.d("181818", "recents onBusEvent launchTaskIndex = " + launchTaskIndex);
        if (launchTaskIndex < mStack.getStackTasks().size() - 1) {
            launchTaskIndex = Math.max(0, launchTaskIndex + 1);
        } else {
            launchTaskIndex = mStack.getTaskCount() - 1;
        }
        if (launchTaskIndex != -1) {
        	
            final Task launchTask = mStack.getStackTasks().get(launchTaskIndex);

            EventBus.getDefault().send(new LaunchTaskEvent(mTaskStackView.getChildViewForTask(launchTask),
                        launchTask, null, INVALID_STACK_ID, false /* screenPinningRequested */));
        } else if (mStack.getTaskCount() == 0) {
            // If there are no tasks, then just hide recents back to home.
            EventBus.getDefault().send(new HideRecentsEvent(false, true));
        }
    }
    /**Mst: tangjun add for recent button click end*/
    
    /**Mst: tangjun add for orientation begin*/
    public void onConfigurationChanged() {

    	if(getVisibility() != View.VISIBLE) {
    		return;
    	}
    	
        /**Mst: tangjun add begin*/
        mItemWidth_0 = this.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_first_right_see_padding) + 
        		this.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_normal_paddingleft) + 
        		this.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_width);
        mItemWidth_normal = this.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_normal_paddingleft) + 
        		this.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_width);
        mItemScroll_distance = mItemWidth_0 - this.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_first_right_see_padding);
        /**Mst: tangjun add end*/
        
    	LayoutParams layoutparam = (LayoutParams) mstTaskStackView.getLayoutParams();
    	if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		layoutparam.width = LayoutParams.MATCH_PARENT;
    	} else {
    		layoutparam.width = this.getResources().getDimensionPixelSize(R.dimen.mst_recents_task_view_width_lanscape);
    	}
    	mstTaskStackView.setLayoutParams(layoutparam);
    	mstTaskStackView.setAdapter(mAdapter);
    	
    	layoutparam = (LayoutParams) mClearAllView.getLayoutParams();
    	if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		layoutparam.bottomMargin = this.getResources().getDimensionPixelSize(R.dimen.mst_clear_all_icon_marginbottom_portait);
    	} else {
    		layoutparam.bottomMargin = this.getResources().getDimensionPixelSize(R.dimen.mst_clear_all_icon_marginbottom_landscape);
    	}
    	mClearAllView.setLayoutParams(layoutparam);
    }
    /**Mst: tangjun add for orientation end*/
    
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
    	// TODO Auto-generated method stub
    	super.onConfigurationChanged(newConfig);

    	//Log.d("111111", "---RecentsView onConfigurationChanged orientation = " + newConfig.orientation + ", getVisibility = " + getVisibility());
    	if(getVisibility() != View.VISIBLE) {
    		return;
    	}
    	
        /**Mst: tangjun add begin*/
        mItemWidth_0 = this.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_first_right_see_padding) + 
        		this.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_normal_paddingleft) + 
        		this.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_width);
        mItemWidth_normal = this.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_normal_paddingleft) + 
        		this.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_width);
        mItemScroll_distance = mItemWidth_0 - this.getResources().getDimensionPixelSize(R.dimen.mst_recent_task_item_first_right_see_padding);
        /**Mst: tangjun add end*/
        
    	LayoutParams layoutparam = (LayoutParams) mstTaskStackView.getLayoutParams();
    	if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
    		layoutparam.width = LayoutParams.MATCH_PARENT;
    	} else {
    		layoutparam.width = this.getResources().getDimensionPixelSize(R.dimen.mst_recents_task_view_width_lanscape);
    	}
    	mstTaskStackView.setLayoutParams(layoutparam);
    	mstTaskStackView.setAdapter(mAdapter);
    	
    	layoutparam = (LayoutParams) mClearAllView.getLayoutParams();
    	if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
    		layoutparam.bottomMargin = this.getResources().getDimensionPixelSize(R.dimen.mst_clear_all_icon_marginbottom_portait);
    	} else {
    		layoutparam.bottomMargin = this.getResources().getDimensionPixelSize(R.dimen.mst_clear_all_icon_marginbottom_landscape);
    	}
    	mClearAllView.setLayoutParams(layoutparam);
    }
    
    /**Mst: tangjun add begin*/
    private void acquireVelocityTracker(final MotionEvent event) { 
        if(null == mVelocityTracker) { 
        	mVelocityTracker= VelocityTracker.obtain();
        } 
        mVelocityTracker.addMovement(event); 
    }
    
    private void releaseVelocityTracker() { 
        if(null != mVelocityTracker) {
        	mVelocityTracker.clear(); 
        	mVelocityTracker.recycle(); 
        	mVelocityTracker = null; 
        } 
    }
    
    private void onTaskViewDismissed(Task task) {
        // Remove any stored data from the loader
        RecentsTaskLoader loader = Recents.getTaskLoader();
        loader.deleteTaskData(task, false);

        // Remove the task from activity manager
        SystemServicesProxy ssp = Recents.getSystemServices();
        ssp.removeTask(task.key.id);
        ssp.forceStopPackage(task.key.baseIntent.getComponent().getPackageName());
    }
    
    private void startAnimationForDismiss(final View view, final int position) {
    	ObjectAnimator transAnim = ObjectAnimator.ofFloat(view, "TranslationY", view.getTranslationY(), -MAX_DISTANCE).setDuration(120);
    	ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "Alpha", view.getAlpha(), 0).setDuration(120);
    	AnimatorSet animSet = new AnimatorSet();
    	animSet.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				//Dissmiss the recent app
				int index = position;
				view.setTranslationY(0);
				//view.setAlpha(1);
				if(index >= mMstTaskList.size() && mMstTaskList.size() > 0) {
					index = mMstTaskList.size() - 1;
				}
		        
				Task t = mMstTaskList.get(index);
				mMstTaskList.remove(index);
		        mAdapter.setTaskList(mMstTaskList);
		        
		        //Mst: tangjun because we add another padding view in first of adapter, so we need to plus one begin 
		        mAdapter.notifyItemRemoved(index + 1);
		        mAdapter.notifyItemRangeChanged(0, mMstTaskList.size() + 1);
		        //Mst: tangjun because we add another padding view in first of adapter, so we need to plus one end 
		        
		        if(mMstTaskList.size() == 0) {
		            // If there are no remaining tasks, then just close recents
		            EventBus.getDefault().send(new AllTaskViewsDismissedEvent(
		                    R.string.recents_empty_message_dismissed_all));
		        }
		        
		        onTaskViewDismissed(t);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
			}
		});
    	animSet.play(transAnim).with(alphaAnim);
    	animSet.setInterpolator(new AccelerateInterpolator());
    	animSet.start();
    }
    
    private void startAnimationForBack(View view) {
    	ObjectAnimator transAnim = ObjectAnimator.ofFloat(view, "TranslationY", view.getTranslationY(), 0).setDuration(120);
    	ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "Alpha", view.getAlpha(), 1).setDuration(120);
    	AnimatorSet animSet = new AnimatorSet();
    	animSet.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
			}
		});
    	animSet.play(transAnim).with(alphaAnim);
    	animSet.start();
    }
    
    private void startAnimationForLock(final View view, final int position) {
    	ObjectAnimator transAnim = ObjectAnimator.ofFloat(view, "TranslationY", view.getTranslationY(), 0).setDuration(120);
    	ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "Alpha", 1, 1).setDuration(120);
    	AnimatorSet animSet = new AnimatorSet();
    	animSet.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				lockOrUnlockRecents(view, position);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
			}
		});
    	animSet.play(transAnim).with(alphaAnim);
    	animSet.start();
    }
    
    /**
     * 
     * @param view
     * Mst: tangjun lock or unlock recent app 
     */
    private void lockOrUnlockRecents(View view, int position) {
    	if(position >= mMstTaskList.size() && mMstTaskList.size() > 0) {
    		position = mMstTaskList.size() - 1;
    	}
    	Task t = mMstTaskList.get(position);
		View lockView = view.findViewById(R.id.lockicon);
		
     	String key = RecentsConfiguration.getRecentsAppsKey(t);
    	if(RecentsConfiguration.readRecentsAppsLockState(getContext(), key)){
    		lockView.setVisibility(View.GONE);
    		RecentsConfiguration.writeRecentsAppLockState(getContext(), key, false);
    	}else{
    		lockView.setVisibility(View.VISIBLE);
    		RecentsConfiguration.writeRecentsAppLockState(getContext(), key, true);
    	}
    }
    @Override
    protected void onFinishInflate() {
    	// TODO Auto-generated method stub
    	super.onFinishInflate();
    	initMstTaskStackView();
    }
    
    private int getScrolledDistance() {
    	View firstVisibleItem = mstTaskStackView.getChildAt(0);
    	if(firstVisibleItem != null) {
	    	int firstItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
	    	int firstItemRight = mLinearLayoutManager.getDecoratedRight(firstVisibleItem);
	    	if(firstItemPosition == 0) {
	    		return mItemWidth_0 - firstItemRight;
	    	} else {
	    		return mItemWidth_0 + firstItemPosition * mItemWidth_normal - firstItemRight;
	    	}
    	}
    	return 0;
    }
    
	private void initMstTaskStackView() {
    	mstTaskStackView = (MstTaskStackView)findViewById(R.id.id_recyclerview_horizontal);
    	mLinearLayoutManager = new LinearLayoutManager(this.getContext());  
    	mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);  
        mstTaskStackView.setLayoutManager(mLinearLayoutManager);
        MstDefaultItemAnimator animator = new MstDefaultItemAnimator();
        animator.setChangeDuration(150);
        animator.setMoveDuration(150);
        mstTaskStackView.setItemAnimator(animator);
        mstTaskStackView.setOnScrollListener(new OnScrollListener() {
    		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    			//already stop
    			if(newState == 0) {
    				if(!mSelfScroll) {
	    				totalDx = getScrolledDistance();
//	    				mSelfScroll = true;
//	    				Log.e("151515", "onScrollStateChanged totalDx = " + totalDx);
//	    				int index = totalDx / 630;
//	    				if(totalDx % 630 != 0) {
//		    				if(totalDx % 630 > 300) {
//		    					mstTaskStackView.smoothScrollBy((index + 1) * 630 - totalDx, 0);
//		    				} else {
//		    					mstTaskStackView.smoothScrollBy(index* 630 - totalDx, 0);
//		    				}
//	    				} else {
//	    					mSelfScroll = false;
//	    				}
    				} else {
    					mSelfScroll = false;
    				}
    			}
    		}

    		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    		}
		});
        
        mClearAllView = findViewById(R.id.clear_all_icon);
        mClearAllTextView = (TextView)findViewById(R.id.clear_recents_text);
        mClearAllImageView = (MstClearAllView)findViewById(R.id.clear_recents_image);
        mAnimationDrawable = (AnimationDrawable)mClearAllImageView.getDrawable();
    	mClearAllView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.e("181818", "-clearAllRecentApps ");
				clearAllRecentApps();
			}
		});
    	
    	mEmptyView_Mst = (TextView)findViewById(R.id.emptyview);
    	mRecylerview_frame = findViewById(R.id.mst_recylerview_frame);
	}
	
	/**Mst: tangjun add begin*/
	public void setMstTaskStackViewScrollWhenStart() {
		if(mMstTaskList != null && mMstTaskList.size() > 0) {
			if(mstTaskStackView != null) {
				totalDx = getScrolledDistance();
				//Log.e("151515", "--onWindowFocusChanged totalDx =  " + totalDx);
				//mstTaskStackView.scrollToPosition(0);
				
				if("com.monster.launcher".equals(Recents.getSystemServices().getSecondTopMostTask().topActivity.getPackageName())) {
					mstTaskStackView.scrollBy(0 - totalDx, 0);
				} else {
					mstTaskStackView.scrollBy(mItemScroll_distance - totalDx, 0);
				}
			}
		}
	}
	/**Mst: tangjun add end*/
    	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasWindowFocus);
//		Log.e("111111", "--onWindowFocusChanged hasWindowFocus =  " + hasWindowFocus);
//		/**Mst: tangjun add begin*/
//		if(hasWindowFocus && mMstTaskList != null && mMstTaskList.size() > 0) {
//			if(mstTaskStackView != null) {
//				totalDx = getScrolledDistance();
//				//Log.e("151515", "--onWindowFocusChanged totalDx =  " + totalDx);
//				//mstTaskStackView.scrollToPosition(0);
//				
//				if("com.monster.launcher".equals(Recents.getSystemServices().getSecondTopMostTask().topActivity.getPackageName())) {
//					mstTaskStackView.scrollBy(0 - totalDx, 0);
//				} else {
//					mstTaskStackView.scrollBy(mItemScroll_distance - totalDx, 0);
//				}
//			}
//		}
//		/**Mst: tangjun add end*/
	}
	
	public void setViewClipPercent() {
		float unusedMem = RecentsMemoryInfo.getmem_unused(mContext, Recents.getSystemServices().getActivityManager());
		if(mClearAllImageView != null) {
			float percent = 1 -  unusedMem / mTotalMemSize;
			mClearAllImageView.setViewClipPercent(percent);
		}
		setClearAllText(unusedMem);
	}
	
	private void setClearAllText(float unsedMem) {
		StringBuilder totalMemString = new StringBuilder(String.valueOf(mTotalMemSizeGB));
		totalMemString.append("GB");
		StringBuilder unusedMemString = new StringBuilder();
		//Log.d("111111", "---setClearAllText 11unsedMem" + unsedMem);
		if(unsedMem >= 1024.0f * 1024.0f) {
			unsedMem = unsedMem / 1024 / 1024;
			//Log.d("111111", "---setClearAllText 22unsedMem" + unsedMem);
			unsedMem = ((float)Math.round(unsedMem*10))/10;
			unusedMemString.append(unsedMem).append("GB");
		} else if(unsedMem >= 1024.0f) {
			unsedMem = unsedMem / 1024;
			unsedMem = (float)(Math.round(unsedMem*10)/10);
			unusedMemString.append(unsedMem).append("MB");
		} else {
			unusedMemString.append(unsedMem).append("KB");
		}
		mClearAllTextView.setText(mContext.getString(R.string.memory_info, unusedMemString, totalMemString));
	}
	
	private void clearAllRecentApps() {
    	int first = mLinearLayoutManager.findFirstVisibleItemPosition();
    	int last = mLinearLayoutManager.findLastVisibleItemPosition();
    	
    	mClearAllImageView.startCircleAnim();
    	mClearAllAnimRun = true;
    	mAnimationDrawable.stop();
    	mAnimationDrawable.start();
    	mClearAllImageView.setOnAnimEndListener(new OnAnimEndListener() {
			
			@Override
			public void onAnimEnd() {
				// TODO Auto-generated method stub
				mAnimationDrawable.stop();
				mClearAllAnimRun = false;
	            // If there are no remaining tasks, then just close recents
				if(mMstTaskList != null && mMstTaskList.size() == 0) {
					EventBus.getDefault().send(new AllTaskViewsDismissedEvent(
							R.string.recents_empty_message_dismissed_all));
				} else {
					Task t = mMstTaskList.get(0);
					if(t.key.baseIntent.getComponent().getPackageName().equals(
							Recents.getSystemServices().getSecondTopMostTask().topActivity.getPackageName())) {
			            EventBus.getDefault().send(new LaunchTaskEvent(mTaskStackView.getChildViewForTask(t),
		                        t, null, INVALID_STACK_ID, false /* screenPinningRequested */));
					} else {
						EventBus.getDefault().send(new AllTaskViewsDismissedEvent(
								R.string.recents_empty_message_dismissed_all));
					}
				}
				
		        //mstTaskStackView.setAdapter(mAdapter);
		        
				float unusedMem = RecentsMemoryInfo.getmem_unused(mContext, Recents.getSystemServices().getActivityManager());
				setClearAllText(unusedMem);
			}
		});
    	//Log.e("181818", "-first = " + first + ", last = " + last);
    	
		int delay = 0;
    	for(int index = 0; index <= last - first && mMstTaskList.size() > 0; index++ ) {
    		//Mst: tangjun because we add another padding view in first of adapter, so we need to minus one begin
    		if(first == 0 && index == 0) {
    			continue;
    		}
    		Task t = mMstTaskList.get(first+index - 1);
    		//Mst: tangjun mod for no anim if the task is locked begin 2016.11.2
			String key = RecentsConfiguration.getRecentsAppsKey(t);
			float translate = 0;
			float alpha = 0;
			if (RecentsConfiguration.readRecentsAppsLockState(getContext(), key) ||
					t.key.baseIntent.getComponent().getPackageName().equals(Recents.getSystemServices().getSecondTopMostTask().topActivity.getPackageName())) {
				translate = 0;
				alpha = 1;
			} else {
				translate = -1000;
				alpha = 0;
			}
			//Mst: tangjun mod for no anim if the task is locked end 2016.11.2
    		final View view = mstTaskStackView.getChildAt(index);
    		final boolean isLast = index == last - first;
	    	ObjectAnimator transAnim = ObjectAnimator.ofFloat(view, "TranslationY", 0, translate);
	    	ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "Alpha", 1, alpha);
	    	AnimatorSet animSet = new AnimatorSet();
	    	
	    	animSet.setStartDelay(delay);
	    	animSet.setDuration(250 - delay);
	    	delay += 50;
	    	animSet.addListener(new AnimatorListener() {
				
				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animator animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animator animation) {
					// TODO Auto-generated method stub
					//clearAllRecentApps
					if(isLast) {
			            
						int count = mMstTaskList.size();
						int j = 0;
						for (int i = 0; i < count; i++) {
							Task t = mMstTaskList.get(j);
							String key = RecentsConfiguration.getRecentsAppsKey(t);
							if (!RecentsConfiguration.readRecentsAppsLockState(getContext(), key) && 
									!t.key.baseIntent.getComponent().getPackageName().equals(Recents.getSystemServices().getSecondTopMostTask().topActivity.getPackageName())) {
								mMstTaskList.remove(j);
								mAdapter.setTaskList(mMstTaskList);
								//Mst: tangjun because we add another padding view in first of adapter, so we need to plus one
								//mAdapter.notifyItemRemoved(j + 1);
								//mAdapter.notifyItemRangeRemoved(0, count+1);
								onTaskViewDismissed(t);
							} else {
								j++; 	//if not remove , then ++
							}
						}
					}
					view.setTranslationY(0);
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub
				}
			});
	    	animSet.play(transAnim).with(alphaAnim);
	    	animSet.start();
    	}
    }
    
    private void updateMstTaskStackView(TaskStack stack) {
    	mMstTaskList = stack.getStackTasks();
        for(int i = 0; i < mMstTaskList.size(); i++) {
        	//Log.d("181818", "---RecentsView updateMstTaskStackView i = " + i);
        	//Log.d("181818", "---RecentsView updateMstTaskStackView thumbnail = " + mMstTaskList.get(i).thumbnail);
        	//Log.d("181818", "---RecentsView updateMstTaskStackView title = " + mMstTaskList.get(i).title);
        }
    	if(mAdapter == null) {
    		mAdapter = new MstTaskAdapter(this.getContext(),  mMstTaskList);
    		mstTaskStackView.setAdapter(mAdapter);
    	} else {
    		mAdapter.setTaskList(mMstTaskList);
    		mAdapter.notifyDataSetChanged();
    	}
    	mAdapter.setOnItemListener(new OnItemListener() {

			@Override
			public void onItemClick(View view, int position) {
				// TODO Auto-generated method stub
				//launch the recent app
				if(mMstTaskList.size() <= 0 || mClearAllAnimRun) {
					return;
				}
		        final SystemServicesProxy ssp = Recents.getSystemServices();
		        Log.d("181818", "----onItemClick position = " + position);
		    	if(position >= mMstTaskList.size() && mMstTaskList.size() > 0) {
		    		position = mMstTaskList.size() - 1;
		    	}
				Task task = mMstTaskList.get(position);

				if (ssp.startActivityFromRecents(view, getContext(), task.key,
						task.title, null)) {
					//TODO
				} else {
					// Dismiss the task and return the user to home if we fail to
					// launch the task
				}
			}

			@Override
			public void onItemDismiss(View view, int position) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAllItemDismiss() {
				// TODO Auto-generated method stub
			}

			@Override
			public boolean onItemTouchListener(View view, final int position,
					MotionEvent event) {
				// TODO Auto-generated method stub
				if(mMstTaskList.size() <= 0 || mClearAllAnimRun) {
					return false;
				}
				acquireVelocityTracker(event); 
				switch (event.getAction()) { 
		    	case MotionEvent.ACTION_DOWN: 
		    		touchX = (int) event.getRawX();
		    		touchY = (int) event.getRawY();
		    		mstTaskStackView.setTouchIntercept(true);
		    		mIsTranslateY = false;
		    		break;

		    	case MotionEvent.ACTION_MOVE: 
		    		if((Math.abs(event.getRawY() - touchY) > mPagingTouchSlop && Math.abs(event.getRawY() - touchY)> Math.abs(event.getRawX() - touchX)) 
		    				|| mIsTranslateY) {
		    			view.setTranslationY(event.getRawY() - touchY);
		    			if(event.getRawY() - touchY <= 0) {
		    				view.setAlpha((MAX_DISTANCE + event.getRawY() - touchY) / MAX_DISTANCE);
		    			}
		    			mIsTranslateY = true;
		    			mstTaskStackView.setTouchIntercept(false);
		    			return true;
		    		}
		    		break;

		    	case MotionEvent.ACTION_UP:
		    	case MotionEvent.ACTION_CANCEL:
		    		mVelocityTracker.computeCurrentVelocity(1000); 
		    		final float velocityX = mVelocityTracker.getXVelocity(); 
		    		final float velocityY = mVelocityTracker.getYVelocity();
		    		if(mstTaskStackView.getTouchIntercept()) {
		    			return false;
		    		} else {
		    			//Log.e("151515", "velocityX = " + velocityX + ", velocityY = " + velocityY + ", mFlingAnimationUtils.getMinVelocityPxPerSecond() = " + mFlingAnimationUtils.getMinVelocityPxPerSecond());
		    			if(event.getRawY() - touchY < 0) {
			    			if(event.getRawY() - touchY < - 300 || (velocityY < -mFlingAnimationUtils.getMinVelocityPxPerSecond() && event.getRawY() - touchY < - 100) ) {
			    				startAnimationForDismiss(view, position);
			    			} else {
			    				startAnimationForBack(view);
			    			}
		    			} else {
		    				if(event.getRawY() - touchY > 200 || (velocityY > mFlingAnimationUtils.getMinVelocityPxPerSecond() && event.getRawY() - touchY > 100) ) {
			    				startAnimationForLock(view, position);
			    			} else {
			    				startAnimationForBack(view);
			    			}
		    			}
		    			mstTaskStackView.setTouchIntercept(true);
		    			return true;
		    		}

		    	default: 
		    		break; 
		    	} 
				return false;
			}

			@Override
			public void onItemSplitScreenImageClick(int position) {
				// TODO Auto-generated method stub
				Log.d("111111", "---onItemSplitScreenImageClick position = " + position);
				startTaskInDockedMode(position);
			}

		});
    }
    
    /**Mst: tangjun add for SplitScreen begin*/
    private void startTaskInDockedMode(int position) {
    	final Task task = mMstTaskList.get(position);
        // Dock the task and launch it
        SystemServicesProxy ssp = Recents.getSystemServices();
        /**Mst: tangjun add for save current dock task begin*/
        SystemServicesProxy.mDockRunningTaskPackageName = task.key.baseIntent.getComponent().getPackageName();
        //add for app fenshen
        SystemServicesProxy.mDockRunningTaskUserId = task.key.userId;
        if(!SystemServicesProxy.mDockPackageNames.contains(SystemServicesProxy.mDockRunningTaskPackageName)) {
        	SystemServicesProxy.mDockPackageNames.add(SystemServicesProxy.mDockRunningTaskPackageName);
        }
        /**Mst: tangjun add for save current dock task end*/
        if (ssp.startTaskInDockedMode(task.key.id, TaskStack.DockState.LEFT.createMode)) {
//            final OnAnimationStartedListener startedListener =
//                    new OnAnimationStartedListener() {
//                @Override
//                public void onAnimationStarted() {
//                    EventBus.getDefault().send(new DockedFirstAnimationFrameEvent());
//                    // Remove the task and don't bother relaying out, as all the tasks will be
//                    // relaid out when the stack changes on the multiwindow change event
//                    mTaskStackView.getStack().removeTask(task, null,
//                            true /* fromDockGesture */);
//                }
//            };

//            final Rect taskRect = getTaskRect(event.taskView);
//            IAppTransitionAnimationSpecsFuture future =
//                    mTransitionHelper.getAppTransitionFuture(
//                            new AnimationSpecComposer() {
//                                @Override
//                                public List<AppTransitionAnimationSpec> composeSpecs() {
//                                    return mTransitionHelper.composeDockAnimationSpec(
//                                            event.taskView, taskRect);
//                                }
//                            });
//            ssp.overridePendingAppTransitionMultiThumbFuture(future,
//                    mTransitionHelper.wrapStartedListener(startedListener),
//                    true /* scaleUp */);

            MetricsLogger.action(mContext, MetricsEvent.ACTION_WINDOW_DOCK_DRAG_DROP,
                    task.getTopComponent().flattenToShortString());
            
            Intent intent = new Intent(RecentsActivity.CHANGETO_SPLITSCREEN);
            mContext.sendBroadcast(intent);
        }
    }
    
    public void setSplitScreenPackageNames(ArrayList<String> splitScreenPackageList) {
    	if(mAdapter != null) {
    		mAdapter.setSplitScreenPackageNames(splitScreenPackageList);
    	}
    }
    /**Mst: tangjun add for SplitScreen end*/
    /**Mst: tangjun add end*/

    /**
     * Called from RecentsActivity when it is relaunched.
     */
    public void onReload(boolean isResumingFromVisible, boolean isTaskStackEmpty) {
        RecentsConfiguration config = Recents.getConfiguration();
        RecentsActivityLaunchState launchState = config.getLaunchState();

        if (mTaskStackView == null) {
            isResumingFromVisible = false;
            mTaskStackView = new TaskStackView(getContext());
            mTaskStackView.setSystemInsets(mSystemInsets);
            /**Mst: tangjun add begin*/
            //addView(mTaskStackView);
            /**Mst: tangjun add end*/
        }

        // Reset the state
        mAwaitingFirstLayout = !isResumingFromVisible;
        mLastTaskLaunchedWasFreeform = false;

        // Update the stack
        mTaskStackView.onReload(isResumingFromVisible);

        if (isResumingFromVisible) {
            // If we are already visible, then restore the background scrim
            animateBackgroundScrim(1f, DEFAULT_UPDATE_SCRIM_DURATION);
        } else {
            // If we are already occluded by the app, then set the final background scrim alpha now.
            // Otherwise, defer until the enter animation completes to animate the scrim alpha with
            // the tasks for the home animation.
            if (launchState.launchedViaDockGesture || launchState.launchedFromApp
                    || isTaskStackEmpty) {
                mBackgroundScrim.setAlpha(255);
            } else {
                mBackgroundScrim.setAlpha(0);
            }
        }
    }

    /**
     * Called from RecentsActivity when the task stack is updated.
     */
    public void updateStack(TaskStack stack, boolean setStackViewTasks) {
        mStack = stack;
        if (setStackViewTasks) {
            mTaskStackView.setTasks(stack, true /* allowNotifyStackChanges */);
        }

        // Update the top level view's visibilities
        if (stack.getTaskCount() > 0) {
            hideEmptyView();
        } else {
            showEmptyView(R.string.recents_empty_message);
        }
        
        /**Mst: tangjun add begin*/
        updateMstTaskStackView(stack);
        /**Mst: tangjun add end*/
    }

    /**
     * Returns the current TaskStack.
     */
    public TaskStack getStack() {
        return mStack;
    }

    /*
     * Returns the window background scrim.
     */
    public Drawable getBackgroundScrim() {
        return mBackgroundScrim;
    }

    /**
     * Returns whether the last task launched was in the freeform stack or not.
     */
    public boolean isLastTaskLaunchedFreeform() {
        return mLastTaskLaunchedWasFreeform;
    }

    /** Launches the focused task from the first stack if possible */
    public boolean launchFocusedTask(int logEvent) {
        if (mTaskStackView != null) {
            Task task = mTaskStackView.getFocusedTask();
            if (task != null) {
                TaskView taskView = mTaskStackView.getChildViewForTask(task);
                EventBus.getDefault().send(new LaunchTaskEvent(taskView, task, null,
                        INVALID_STACK_ID, false));

                if (logEvent != 0) {
                    MetricsLogger.action(getContext(), logEvent,
                            task.key.getComponent().toString());
                }
                return true;
            }
        }
        return false;
    }

    /** Launches the task that recents was launched from if possible */
    public boolean launchPreviousTask() {
        if (mTaskStackView != null) {
        	/**Mst: tangjun mod begin*/
            //TaskStack stack = mTaskStackView.getStack();
        	TaskStack stack = mStack;
        	/**Mst: tangjun mod end*/
            Task task = stack.getLaunchTarget();
            if (task != null) {
                TaskView taskView = mTaskStackView.getChildViewForTask(task);
                EventBus.getDefault().send(new LaunchTaskEvent(taskView, task, null,
                        INVALID_STACK_ID, false));
                return true;
            }
        }
        return false;
    }

    /** Launches a given task. */
    public boolean launchTask(Task task, Rect taskBounds, int destinationStack) {
        if (mTaskStackView != null) {
            // Iterate the stack views and try and find the given task.
            List<TaskView> taskViews = mTaskStackView.getTaskViews();
            int taskViewCount = taskViews.size();
            for (int j = 0; j < taskViewCount; j++) {
                TaskView tv = taskViews.get(j);
                if (tv.getTask() == task) {
                    EventBus.getDefault().send(new LaunchTaskEvent(tv, task, taskBounds,
                            destinationStack, false));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Hides the task stack and shows the empty view.
     */
    public void showEmptyView(int msgResId) {
        mTaskStackView.setVisibility(View.INVISIBLE);
        mEmptyView.setText(msgResId);
        mEmptyView.setVisibility(View.VISIBLE);
        mEmptyView.bringToFront();
        if (RecentsDebugFlags.Static.EnableStackActionButton) {
            mStackActionButton.bringToFront();
        }

        /**Mst: tangjun add begin*/
        mRecylerview_frame.setVisibility(View.INVISIBLE);
        mEmptyView_Mst.setVisibility(View.VISIBLE);
        /**Mst: tangjun add end*/
    }

    /**
     * Shows the task stack and hides the empty view.
     */
    public void hideEmptyView() {
    	
        /**Mst: tangjun add begin*/
    	mEmptyView_Mst.setVisibility(View.INVISIBLE);
    	mRecylerview_frame.setVisibility(View.VISIBLE);
        /**Mst: tangjun add end*/
        
        mEmptyView.setVisibility(View.INVISIBLE);
        mTaskStackView.setVisibility(View.VISIBLE);
        mTaskStackView.bringToFront();
        if (RecentsDebugFlags.Static.EnableStackActionButton) {
            mStackActionButton.bringToFront();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        EventBus.getDefault().register(this, RecentsActivity.EVENT_BUS_PRIORITY + 1);
        EventBus.getDefault().register(mTouchHandler, RecentsActivity.EVENT_BUS_PRIORITY + 2);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().unregister(mTouchHandler);
    }

    /**
     * This is called with the full size of the window since we are handling our own insets.
     */
    /*
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (mTaskStackView.getVisibility() != GONE) {
            mTaskStackView.measure(widthMeasureSpec, heightMeasureSpec);
        }

        // Measure the empty view to the full size of the screen
        if (mEmptyView.getVisibility() != GONE) {
            measureChild(mEmptyView, MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
        }

        if (RecentsDebugFlags.Static.EnableStackActionButton) {
            // Measure the stack action button within the constraints of the space above the stack
            Rect buttonBounds = mTaskStackView.mLayoutAlgorithm.mStackActionButtonRect;
            measureChild(mStackActionButton,
                    MeasureSpec.makeMeasureSpec(buttonBounds.width(), MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(buttonBounds.height(), MeasureSpec.AT_MOST));
        }

        setMeasuredDimension(width, height);
    }
    */

    /**
     * This is called with the full size of the window since we are handling our own insets.
     */
    /*
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mTaskStackView.getVisibility() != GONE) {
            mTaskStackView.layout(left, top, left + getMeasuredWidth(), top + getMeasuredHeight());
        }

        // Layout the empty view
        if (mEmptyView.getVisibility() != GONE) {
            int leftRightInsets = mSystemInsets.left + mSystemInsets.right;
            int topBottomInsets = mSystemInsets.top + mSystemInsets.bottom;
            int childWidth = mEmptyView.getMeasuredWidth();
            int childHeight = mEmptyView.getMeasuredHeight();
            int childLeft = left + mSystemInsets.left +
                    Math.max(0, (right - left - leftRightInsets - childWidth)) / 2;
            int childTop = top + mSystemInsets.top +
                    Math.max(0, (bottom - top - topBottomInsets - childHeight)) / 2;
            mEmptyView.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        }

        if (RecentsDebugFlags.Static.EnableStackActionButton) {
            // Layout the stack action button such that its drawable is start-aligned with the
            // stack, vertically centered in the available space above the stack
            Rect buttonBounds = getStackActionButtonBoundsFromStackLayout();
            mStackActionButton.layout(buttonBounds.left, buttonBounds.top, buttonBounds.right,
                    buttonBounds.bottom);
        }

        if (mAwaitingFirstLayout) {
            mAwaitingFirstLayout = false;

            // If launched via dragging from the nav bar, then we should translate the whole view
            // down offscreen
            RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
            if (launchState.launchedViaDragGesture) {
                setTranslationY(getMeasuredHeight());
            } else {
                setTranslationY(0f);
            }
        }
    }
    */

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        mSystemInsets.set(insets.getSystemWindowInsets());
        mTaskStackView.setSystemInsets(mSystemInsets);
        requestLayout();
        return insets;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

		//add by liuzhicang begin
		//if we touch mClearAllView
		if (isInViewRect(mClearAllView, ev)) {
			return true;
		}
		//add by liuzhicang end
		if(ev.getPointerCount() > 1) {
			return true;
		}
		return mTouchHandler.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
		//add by liuzhicang begin
		//if we touch mClearAllView
    	//if (isInViewRect(mClearAllView, ev)) {
		if (isInViewRect(mClearAllView, ev) && ev.getAction() != MotionEvent.ACTION_MOVE) {
			return  mClearAllView.onTouchEvent(ev) ;
		}
		//add by liuzhicang end
		return mTouchHandler.onTouchEvent(ev);
    }

	//add by liuzhicang begin for touch event
	private boolean isInViewRect(View view, MotionEvent ev) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		RectF rect = new RectF(location[0], location[1], location[0] + view.getWidth(),
				location[1] + view.getHeight());
		float x = ev.getRawX();
		float y = ev.getRawY();
		return rect.contains(x, y);
	}
	//add by liuzhicang end for touch event

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);

        ArrayList<TaskStack.DockState> visDockStates = mTouchHandler.getVisibleDockStates();
        for (int i = visDockStates.size() - 1; i >= 0; i--) {
            visDockStates.get(i).viewState.draw(canvas);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        ArrayList<TaskStack.DockState> visDockStates = mTouchHandler.getVisibleDockStates();
        for (int i = visDockStates.size() - 1; i >= 0; i--) {
            Drawable d = visDockStates.get(i).viewState.dockAreaOverlay;
            if (d == who) {
                return true;
            }
        }
        return super.verifyDrawable(who);
    }

    /**** EventBus Events ****/

    public final void onBusEvent(LaunchTaskEvent event) {
        mLastTaskLaunchedWasFreeform = event.task.isFreeformTask();
        /**Mst: tangjun  mod for change anim begin*/
//        mTransitionHelper.launchTaskFromRecents(mStack, event.task, mTaskStackView, event.taskView,
//                event.screenPinningRequested, event.targetTaskBounds, event.targetTaskStack);
        //why not 0, see MstTaskAdapter
        if(mstTaskStackView.getChildAt(1) != null) {
        	mTransitionHelper.launchTaskFromRecents(mstTaskStackView.getChildAt(1), mStack, event.task, mTaskStackView, event.taskView,
        			event.screenPinningRequested, event.targetTaskBounds, event.targetTaskStack);
        }
        /**Mst: tangjun  mod for change anim end*/
    }

    public final void onBusEvent(DismissRecentsToHomeAnimationStarted event) {
        int taskViewExitToHomeDuration = TaskStackAnimationHelper.EXIT_TO_HOME_TRANSLATION_DURATION;
        if (RecentsDebugFlags.Static.EnableStackActionButton) {
            // Hide the stack action button
            hideStackActionButton(taskViewExitToHomeDuration, false /* translate */);
        }
        animateBackgroundScrim(0f, taskViewExitToHomeDuration);
    }

    public final void onBusEvent(DragStartEvent event) {
        updateVisibleDockRegions(mTouchHandler.getDockStatesForCurrentOrientation(),
                true /* isDefaultDockState */, TaskStack.DockState.NONE.viewState.dockAreaAlpha,
                TaskStack.DockState.NONE.viewState.hintTextAlpha,
                true /* animateAlpha */, false /* animateBounds */);

        // Temporarily hide the stack action button without changing visibility
        if (mStackActionButton != null) {
            mStackActionButton.animate()
                    .alpha(0f)
                    .setDuration(HIDE_STACK_ACTION_BUTTON_DURATION)
                    .setInterpolator(Interpolators.ALPHA_OUT)
                    .start();
        }
    }

    public final void onBusEvent(DragDropTargetChangedEvent event) {
        if (event.dropTarget == null || !(event.dropTarget instanceof TaskStack.DockState)) {
            updateVisibleDockRegions(mTouchHandler.getDockStatesForCurrentOrientation(),
                    true /* isDefaultDockState */, TaskStack.DockState.NONE.viewState.dockAreaAlpha,
                    TaskStack.DockState.NONE.viewState.hintTextAlpha,
                    true /* animateAlpha */, true /* animateBounds */);
        } else {
            final TaskStack.DockState dockState = (TaskStack.DockState) event.dropTarget;
            updateVisibleDockRegions(new TaskStack.DockState[] {dockState},
                    false /* isDefaultDockState */, -1, -1, true /* animateAlpha */,
                    true /* animateBounds */);
        }
        if (mStackActionButton != null) {
            event.addPostAnimationCallback(new Runnable() {
                @Override
                public void run() {
                    // Move the clear all button to its new position
                    Rect buttonBounds = getStackActionButtonBoundsFromStackLayout();
                    mStackActionButton.setLeftTopRightBottom(buttonBounds.left, buttonBounds.top,
                            buttonBounds.right, buttonBounds.bottom);
                }
            });
        }
    }

    public final void onBusEvent(final DragEndEvent event) {
        // Handle the case where we drop onto a dock region
        if (event.dropTarget instanceof TaskStack.DockState) {
            final TaskStack.DockState dockState = (TaskStack.DockState) event.dropTarget;

            // Hide the dock region
            updateVisibleDockRegions(null, false /* isDefaultDockState */, -1, -1,
                    false /* animateAlpha */, false /* animateBounds */);

            // We translated the view but we need to animate it back from the current layout-space
            // rect to its final layout-space rect
            Utilities.setViewFrameFromTranslation(event.taskView);

            // Dock the task and launch it
            SystemServicesProxy ssp = Recents.getSystemServices();
            if (ssp.startTaskInDockedMode(event.task.key.id, dockState.createMode)) {
                final OnAnimationStartedListener startedListener =
                        new OnAnimationStartedListener() {
                    @Override
                    public void onAnimationStarted() {
                        EventBus.getDefault().send(new DockedFirstAnimationFrameEvent());
                        // Remove the task and don't bother relaying out, as all the tasks will be
                        // relaid out when the stack changes on the multiwindow change event
                        mTaskStackView.getStack().removeTask(event.task, null,
                                true /* fromDockGesture */);
                    }
                };

                final Rect taskRect = getTaskRect(event.taskView);
                IAppTransitionAnimationSpecsFuture future =
                        mTransitionHelper.getAppTransitionFuture(
                                new AnimationSpecComposer() {
                                    @Override
                                    public List<AppTransitionAnimationSpec> composeSpecs() {
                                        return mTransitionHelper.composeDockAnimationSpec(
                                                event.taskView, taskRect);
                                    }
                                });
                ssp.overridePendingAppTransitionMultiThumbFuture(future,
                        mTransitionHelper.wrapStartedListener(startedListener),
                        true /* scaleUp */);

                MetricsLogger.action(mContext, MetricsEvent.ACTION_WINDOW_DOCK_DRAG_DROP,
                        event.task.getTopComponent().flattenToShortString());
            } else {
                EventBus.getDefault().send(new DragEndCancelledEvent(mStack, event.task,
                        event.taskView));
            }
        } else {
            // Animate the overlay alpha back to 0
            updateVisibleDockRegions(null, true /* isDefaultDockState */, -1, -1,
                    true /* animateAlpha */, false /* animateBounds */);
        }

        // Show the stack action button again without changing visibility
        if (mStackActionButton != null) {
            mStackActionButton.animate()
                    .alpha(1f)
                    .setDuration(SHOW_STACK_ACTION_BUTTON_DURATION)
                    .setInterpolator(Interpolators.ALPHA_IN)
                    .start();
        }
    }

    public final void onBusEvent(final DragEndCancelledEvent event) {
        // Animate the overlay alpha back to 0
        updateVisibleDockRegions(null, true /* isDefaultDockState */, -1, -1,
                true /* animateAlpha */, false /* animateBounds */);
    }

    private Rect getTaskRect(TaskView taskView) {
        int[] location = taskView.getLocationOnScreen();
        int viewX = location[0];
        int viewY = location[1];
        return new Rect(viewX, viewY,
                (int) (viewX + taskView.getWidth() * taskView.getScaleX()),
                (int) (viewY + taskView.getHeight() * taskView.getScaleY()));
    }

    public final void onBusEvent(DraggingInRecentsEvent event) {
        if (mTaskStackView.getTaskViews().size() > 0) {
            setTranslationY(event.distanceFromTop - mTaskStackView.getTaskViews().get(0).getY());
        }
    }

    public final void onBusEvent(DraggingInRecentsEndedEvent event) {
        ViewPropertyAnimator animator = animate();
        if (event.velocity > mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            animator.translationY(getHeight());
            animator.withEndAction(new Runnable() {
                @Override
                public void run() {
                    WindowManagerProxy.getInstance().maximizeDockedStack();
                }
            });
            mFlingAnimationUtils.apply(animator, getTranslationY(), getHeight(), event.velocity);
        } else {
            animator.translationY(0f);
            animator.setListener(null);
            mFlingAnimationUtils.apply(animator, getTranslationY(), 0, event.velocity);
        }
        animator.start();
    }

    public final void onBusEvent(EnterRecentsWindowAnimationCompletedEvent event) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (!launchState.launchedViaDockGesture && !launchState.launchedFromApp
                && mStack.getTaskCount() > 0) {
            animateBackgroundScrim(1f,
                    TaskStackAnimationHelper.ENTER_FROM_HOME_TRANSLATION_DURATION);
        }
    }

    public final void onBusEvent(AllTaskViewsDismissedEvent event) {
        hideStackActionButton(HIDE_STACK_ACTION_BUTTON_DURATION, true /* translate */);
    }

    public final void onBusEvent(DismissAllTaskViewsEvent event) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (!ssp.hasDockedTask()) {
            // Animate the background away only if we are dismissing Recents to home
            animateBackgroundScrim(0f, DEFAULT_UPDATE_SCRIM_DURATION);
        }
    }

    public final void onBusEvent(ShowStackActionButtonEvent event) {
        if (!RecentsDebugFlags.Static.EnableStackActionButton) {
            return;
        }

        showStackActionButton(SHOW_STACK_ACTION_BUTTON_DURATION, event.translate);
    }

    public final void onBusEvent(HideStackActionButtonEvent event) {
        if (!RecentsDebugFlags.Static.EnableStackActionButton) {
            return;
        }

        hideStackActionButton(HIDE_STACK_ACTION_BUTTON_DURATION, true /* translate */);
    }

    public final void onBusEvent(MultiWindowStateChangedEvent event) {
        updateStack(event.stack, false /* setStackViewTasks */);
    }

    /**
     * Shows the stack action button.
     */
    private void showStackActionButton(final int duration, final boolean translate) {
        if (!RecentsDebugFlags.Static.EnableStackActionButton) {
            return;
        }

        final ReferenceCountedTrigger postAnimationTrigger = new ReferenceCountedTrigger();
        if (mStackActionButton.getVisibility() == View.INVISIBLE) {
            mStackActionButton.setVisibility(View.VISIBLE);
            mStackActionButton.setAlpha(0f);
            if (translate) {
                mStackActionButton.setTranslationY(-mStackActionButton.getMeasuredHeight() * 0.25f);
            } else {
                mStackActionButton.setTranslationY(0f);
            }
            postAnimationTrigger.addLastDecrementRunnable(new Runnable() {
                @Override
                public void run() {
                    if (translate) {
                        mStackActionButton.animate()
                            .translationY(0f);
                    }
                    mStackActionButton.animate()
                            .alpha(1f)
                            .setDuration(duration)
                            .setInterpolator(Interpolators.FAST_OUT_SLOW_IN)
                            .start();
                }
            });
        }
        postAnimationTrigger.flushLastDecrementRunnables();
    }

    /**
     * Hides the stack action button.
     */
    private void hideStackActionButton(int duration, boolean translate) {
        if (!RecentsDebugFlags.Static.EnableStackActionButton) {
            return;
        }

        final ReferenceCountedTrigger postAnimationTrigger = new ReferenceCountedTrigger();
        hideStackActionButton(duration, translate, postAnimationTrigger);
        postAnimationTrigger.flushLastDecrementRunnables();
    }

    /**
     * Hides the stack action button.
     */
    private void hideStackActionButton(int duration, boolean translate,
                                       final ReferenceCountedTrigger postAnimationTrigger) {
        if (!RecentsDebugFlags.Static.EnableStackActionButton) {
            return;
        }

        if (mStackActionButton.getVisibility() == View.VISIBLE) {
            if (translate) {
                mStackActionButton.animate()
                    .translationY(-mStackActionButton.getMeasuredHeight() * 0.25f);
            }
            mStackActionButton.animate()
                    .alpha(0f)
                    .setDuration(duration)
                    .setInterpolator(Interpolators.FAST_OUT_SLOW_IN)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mStackActionButton.setVisibility(View.INVISIBLE);
                            postAnimationTrigger.decrement();
                        }
                    })
                    .start();
            postAnimationTrigger.increment();
        }
    }

    /**
     * Updates the dock region to match the specified dock state.
     */
    private void updateVisibleDockRegions(TaskStack.DockState[] newDockStates,
            boolean isDefaultDockState, int overrideAreaAlpha, int overrideHintAlpha,
            boolean animateAlpha, boolean animateBounds) {
        ArraySet<TaskStack.DockState> newDockStatesSet = Utilities.arrayToSet(newDockStates,
                new ArraySet<TaskStack.DockState>());
        ArrayList<TaskStack.DockState> visDockStates = mTouchHandler.getVisibleDockStates();
        for (int i = visDockStates.size() - 1; i >= 0; i--) {
            TaskStack.DockState dockState = visDockStates.get(i);
            TaskStack.DockState.ViewState viewState = dockState.viewState;
            if (newDockStates == null || !newDockStatesSet.contains(dockState)) {
                // This is no longer visible, so hide it
                viewState.startAnimation(null, 0, 0, TaskStackView.SLOW_SYNC_STACK_DURATION,
                        Interpolators.FAST_OUT_SLOW_IN, animateAlpha, animateBounds);
            } else {
                // This state is now visible, update the bounds and show it
                int areaAlpha = overrideAreaAlpha != -1
                        ? overrideAreaAlpha
                        : viewState.dockAreaAlpha;
                int hintAlpha = overrideHintAlpha != -1
                        ? overrideHintAlpha
                        : viewState.hintTextAlpha;
                Rect bounds = isDefaultDockState
                        ? dockState.getPreDockedBounds(getMeasuredWidth(), getMeasuredHeight())
                        : dockState.getDockedBounds(getMeasuredWidth(), getMeasuredHeight(),
                        mDividerSize, mSystemInsets, getResources());
                if (viewState.dockAreaOverlay.getCallback() != this) {
                    viewState.dockAreaOverlay.setCallback(this);
                    viewState.dockAreaOverlay.setBounds(bounds);
                }
                viewState.startAnimation(bounds, areaAlpha, hintAlpha,
                        TaskStackView.SLOW_SYNC_STACK_DURATION, Interpolators.FAST_OUT_SLOW_IN,
                        animateAlpha, animateBounds);
            }
        }
    }

    /**
     * Animates the background scrim to the given {@param alpha}.
     */
    private void animateBackgroundScrim(float alpha, int duration) {
        Utilities.cancelAnimationWithoutCallbacks(mBackgroundScrimAnimator);
        // Calculate the absolute alpha to animate from
        int fromAlpha = (int) ((mBackgroundScrim.getAlpha() / (DEFAULT_SCRIM_ALPHA * 255)) * 255);
        int toAlpha = (int) (alpha * 255);
        mBackgroundScrimAnimator = ObjectAnimator.ofInt(mBackgroundScrim, Utilities.DRAWABLE_ALPHA,
                fromAlpha, toAlpha);
        mBackgroundScrimAnimator.setDuration(duration);
        mBackgroundScrimAnimator.setInterpolator(toAlpha > fromAlpha
                ? Interpolators.ALPHA_IN
                : Interpolators.ALPHA_OUT);
        mBackgroundScrimAnimator.start();
    }

    /**
     * @return the bounds of the stack action button.
     */
    private Rect getStackActionButtonBoundsFromStackLayout() {
        Rect actionButtonRect = new Rect(mTaskStackView.mLayoutAlgorithm.mStackActionButtonRect);
        int left = isLayoutRtl()
                ? actionButtonRect.left - mStackActionButton.getPaddingLeft()
                : actionButtonRect.right + mStackActionButton.getPaddingRight()
                        - mStackActionButton.getMeasuredWidth();
        int top = actionButtonRect.top +
                (actionButtonRect.height() - mStackActionButton.getMeasuredHeight()) / 2;
        actionButtonRect.set(left, top, left + mStackActionButton.getMeasuredWidth(),
                top + mStackActionButton.getMeasuredHeight());
        return actionButtonRect;
    }

    public void dump(String prefix, PrintWriter writer) {
        String innerPrefix = prefix + "  ";
        String id = Integer.toHexString(System.identityHashCode(this));

        writer.print(prefix); writer.print(TAG);
        writer.print(" awaitingFirstLayout="); writer.print(mAwaitingFirstLayout ? "Y" : "N");
        writer.print(" insets="); writer.print(Utilities.dumpRect(mSystemInsets));
        writer.print(" [0x"); writer.print(id); writer.print("]");
        writer.println();

        if (mStack != null) {
            mStack.dump(innerPrefix, writer);
        }
        if (mTaskStackView != null) {
            mTaskStackView.dump(innerPrefix, writer);
        }
    }
}
