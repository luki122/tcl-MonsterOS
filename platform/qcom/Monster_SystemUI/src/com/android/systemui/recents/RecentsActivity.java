/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.recents;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.Interpolators;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DebugFlagsChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.DockedFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowLastAnimationFrameEvent;
import com.android.systemui.recents.events.activity.ExitRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.IterateRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskSucceededEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.ToggleRecentsEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.component.ScreenPinningRequestEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.events.ui.HideIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.events.ui.ShowIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.SplitScreenModeEvent;
import com.android.systemui.recents.events.ui.StackViewScrolledEvent;
import com.android.systemui.recents.events.ui.UpdateFreeformTaskViewVisibilityEvent;
import com.android.systemui.recents.events.ui.UserInteractionEvent;
import com.android.systemui.recents.events.ui.focus.DismissFocusedTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusNextTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusPreviousTaskViewEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.RecentsPackageMonitor;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.RecentsView;
import com.android.systemui.recents.views.SystemBarScrimViews;
import com.android.systemui.stackdivider.WindowManagerProxy;
import com.android.systemui.statusbar.BaseStatusBar;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.android.systemui.tcl.BaseActivity;
import com.android.systemui.tcl.TranslucentActivity;
import com.monster.launchericon.utils.IconGetterManager;
import com.monster.launchericon.utils.IIconGetter;

/**
 * The main Recents activity that is started from RecentsComponent.
 */
public class RecentsActivity extends Activity implements ViewTreeObserver.OnPreDrawListener {

    private final static String TAG = "RecentsActivity";
    private final static boolean DEBUG = false;

    public final static int EVENT_BUS_PRIORITY = Recents.EVENT_BUS_PRIORITY + 1;
    public final static int INCOMPATIBLE_APP_ALPHA_DURATION = 150;

    private RecentsPackageMonitor mPackageMonitor;
    private Handler mHandler = new Handler();
    private long mLastTabKeyEventTime;
    private int mLastDeviceOrientation = Configuration.ORIENTATION_UNDEFINED;
    private int mLastDisplayDensity;
    private boolean mFinishedOnStartup;
    private boolean mIgnoreAltTabRelease;
    private boolean mIsVisible;
    private boolean mReceivedNewIntent;
    //add by liuzhicang begin
	private View mEmptyWhiteView;
    private String mPkgNameInSplitScreenMode;
    private int mPkgUidInSplitScreenMode;
    private PackageIntentReceiver packageIntentReceiver;
    //add by liuzhicang end
    // Top level views
    private RecentsView mRecentsView;
    private SystemBarScrimViews mScrimViews;
    private View mIncompatibleAppOverlay;
    
    /**Mst: tangjun add begin*/
    private ViewPager mSplitViewPager;
    private View mSplitViewPagerFrame;
    private LinearLayout mPageIndicatorLinear;
    private int previousDotIndex = 0;
    private LauncherApps mLauncherApps;
    private List<LauncherActivityInfo> mLauncherActivityInfoList;
    //Mst: tangjun add for fenshen begin
    private List<UserHandle> mLauncherActivityInfoUserList;
    //Mst: tangjun add for fenshen end
    private static final int NUMBER_PER_PAGE = 6;
    private static final int PAGE_INDICATOR_MAX = 30;
    private LayoutInflater mLayoutInflater;
    //private ArrayList<GridView> mPageGridViews = new ArrayList<GridView>();
    private int pageTotal = 0;
    private SplitscreenPagerAdapter mSplitscreenPagerAdapter;
    private int mOrientation;
    private boolean mIsFirstLauncher = false;
    public static final String CHANGETO_SPLITSCREEN = "com.android.systemui.changetosplitscreen";
    
    //Tcl_monster,mod for SplitScreen ,jun_tang@tcl.com,2016.10.24{
    private static final String SPLITSCREEN_XML_ROOT = "packagenames";
	private static final String SPLITSCREEN_XML_ITEM = "packagename";
	private ArrayList<String> mSplitScreenPackageList = null;
	//Tcl_monster}
    /**Mst: tangjun add end*/

    // Runnables to finish the Recents activity
    private Intent mHomeIntent;

    // The trigger to automatically launch the current task
    private int mFocusTimerDuration;
    private DozeTrigger mIterateTrigger;
    private final UserInteractionEvent mUserInteractionEvent = new UserInteractionEvent();
    private final Runnable mSendEnterWindowAnimationCompleteRunnable = () -> {
        EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
    };
    
    /**Mst: tangjun add begin*/
    class SplitscreenPagerAdapter extends PagerAdapter{  
        private ArrayList<GridView> mAdapterPageGridViews;
          
        public SplitscreenPagerAdapter(ArrayList<GridView> gridviews) {  
            this.mAdapterPageGridViews = gridviews;  
        }  
  
        @Override  
        public void destroyItem(ViewGroup container, int position, Object object)   {     
        	if(position >= mAdapterPageGridViews.size()) {
        		return;
        	}
            container.removeView(mAdapterPageGridViews.get(position)); 
        }  
  
  
        @Override  
        public Object instantiateItem(ViewGroup container, int position) {         
        	if(position >= mAdapterPageGridViews.size()) {
        		return null;
        	}
             container.addView(mAdapterPageGridViews.get(position), 0);
             return mAdapterPageGridViews.get(position);  
        }  
  
        @Override  
        public int getCount() {           
            return  mAdapterPageGridViews.size();
        }  
          
        @Override  
        public boolean isViewFromObject(View arg0, Object arg1) {             
            return arg0==arg1;
        }  
    }

    class  SplitscreenGridViewAdapter extends BaseAdapter {
    	
    	List<LauncherActivityInfo> mGridLauncherActivityInfoList;
    	List<UserHandle> mGridLauncherActivityInfoUserList;
    	Context mContext;
    	PackageManager mPackageManager;
    	
    	public  SplitscreenGridViewAdapter(Context context, List<LauncherActivityInfo> gridLauncherActivityInfoList, List<UserHandle> gridLauncherActivityInfoUserList) {
			// TODO Auto-generated constructor stub
    		mContext = context;
    		mGridLauncherActivityInfoList = gridLauncherActivityInfoList;
    		mGridLauncherActivityInfoUserList = gridLauncherActivityInfoUserList;
    		mPackageManager = context.getPackageManager();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mGridLauncherActivityInfoList.size();
		}

		@Override
		public LauncherActivityInfo getItem(int position) {
			// TODO Auto-generated method stub
			return mGridLauncherActivityInfoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Holder holder;
			if(convertView == null){
				convertView = mLayoutInflater.inflate(R.layout.splitscreen_gridview_item, null);
				holder = new Holder();
				holder.appTextView = (TextView)convertView;
				
				Drawable d = null;
				IconGetterManager iconGetter  = IconGetterManager.getInstance(mContext, false, true);
				if(iconGetter != null) {
					d = iconGetter.getIconDrawable(mGridLauncherActivityInfoList.get(position).getComponentName(), mGridLauncherActivityInfoUserList.get(position));
				}
				if(d == null) {
					d = mGridLauncherActivityInfoList.get(position).getBadgedIcon(0);
				}
				d = Utilities.createIconBitmapDrawable(d, mContext);

				holder.appTextView.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
				holder.appTextView.setText(mGridLauncherActivityInfoList.get(position).getLabel());
				holder.setOnClickListener(mGridLauncherActivityInfoList.get(position).getComponentName().getPackageName(), mGridLauncherActivityInfoUserList.get(position));
				convertView.setTag(holder);
			}else{
				holder = (Holder) convertView.getTag();
			}
			return convertView;
		}
		
		class Holder{
			TextView appTextView;
			String packageName;
			UserHandle mUserHandle;
			OnClickListener onclick = new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
				    try{  
//				    	Intent intent = mPackageManager.getLaunchIntentForPackage(packageName);
//				        SystemServicesProxy.startActivityAsUser(RecentsActivity.this, intent, null, mUserHandle);
//                        //modify by liuzhicang begin
//                        // 系统对RecentsActivity有特殊处理，先起一个透明的activity
                        mPkgNameInSplitScreenMode = packageName;
                        ApplicationInfo applicationInfo = mLauncherApps.getApplicationInfo(packageName, PackageManager.GET_META_DATA, mUserHandle);
                        if (applicationInfo != null) {
                            mPkgUidInSplitScreenMode = applicationInfo.uid;
                        }

				        Intent dest = mPackageManager.getLaunchIntentForPackage(packageName);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("intent",dest);
                        bundle.putParcelable("userhandle", mUserHandle);
                        Intent intent = new Intent(RecentsActivity.this, TranslucentActivity.class);
                        intent.putExtras(bundle);
				        startActivity(intent);
//                        //modify by liuzhicang end
				    }catch(Exception e){  
				        Toast.makeText(mContext, "don't installed", Toast.LENGTH_LONG).show();  
				    }
				}
			};
			
			void setOnClickListener(String mTmpPackageName, UserHandle userHandle) {
				mUserHandle = userHandle;
				packageName = mTmpPackageName;
				appTextView.setOnClickListener(onclick);
			}
		}
    	
    }
    /**Mst: tangjun add end*/

    /**
     * A common Runnable to finish Recents by launching Home with an animation depending on the
     * last activity launch state. Generally we always launch home when we exit Recents rather than
     * just finishing the activity since we don't know what is behind Recents in the task stack.
     */
    class LaunchHomeRunnable implements Runnable {

        Intent mLaunchIntent;
        ActivityOptions mOpts;

        /**
         * Creates a finish runnable that starts the specified intent.
         */
        public LaunchHomeRunnable(Intent launchIntent, ActivityOptions opts) {
            mLaunchIntent = launchIntent;
            mOpts = opts;
        }

        @Override
        public void run() {
            try {
                mHandler.post(() -> {
                    ActivityOptions opts = mOpts;
                    if (opts == null) {
                        opts = ActivityOptions.makeCustomAnimation(RecentsActivity.this,
                                R.anim.recents_to_launcher_enter, R.anim.recents_to_launcher_exit);
                    }
                    startActivityAsUser(mLaunchIntent, opts.toBundle(), UserHandle.CURRENT);
                });
            } catch (Exception e) {
                Log.e(TAG, getString(R.string.recents_launch_error_message, "Home"), e);
            }
        }
    }

    /**
     * Broadcast receiver to handle messages from the system
     */
    final BroadcastReceiver mSystemBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                // When the screen turns off, dismiss Recents to Home
                dismissRecentsToHomeIfVisible(false);
            } else if (action.equals(Intent.ACTION_TIME_CHANGED)) {
                // For the time being, if the time changes, then invalidate the
                // last-stack-active-time, this ensures that we will just show the last N tasks
                // the next time that Recents loads, but prevents really old tasks from showing
                // up if the task time is set forward.
                Prefs.putLong(RecentsActivity.this, Prefs.Key.OVERVIEW_LAST_STACK_TASK_ACTIVE_TIME,
                        0);
            }
            //Mst: tangjun add begin
            else if(action.equals(CHANGETO_SPLITSCREEN)) {
                setSplitScreenVisibility(true);
                updateSplitScreenApplication();
                updateSplitScreenView();
            }
            //Mst: tangjun add end
        }
    };

    private final OnPreDrawListener mRecentsDrawnEventListener =
            new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
                    EventBus.getDefault().post(new RecentsDrawnEvent());
                    return true;
                }
            };

    /**
     * Dismisses recents if we are already visible and the intent is to toggle the recents view.
     */
    boolean dismissRecentsToFocusedTask(int logCategory) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.isRecentsActivityVisible()) {
            // If we have a focused Task, launch that Task now
            if (mRecentsView.launchFocusedTask(logCategory)) return true;
        }
        return false;
    }

    /**
     * Dismisses recents back to the launch target task.
     */
    boolean dismissRecentsToLaunchTargetTaskOrHome() {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.isRecentsActivityVisible()) {
            // If we have a focused Task, launch that Task now
            if (mRecentsView.launchPreviousTask()) return true;
            // If none of the other cases apply, then just go Home
            dismissRecentsToHome(true /* animateTaskViews */);
        }
        return false;
    }

    /**
     * Dismisses recents if we are already visible and the intent is to toggle the recents view.
     */
    boolean dismissRecentsToFocusedTaskOrHome() {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.isRecentsActivityVisible()) {
            // If we have a focused Task, launch that Task now
            if (mRecentsView.launchFocusedTask(0 /* logCategory */)) return true;
            // If none of the other cases apply, then just go Home
            dismissRecentsToHome(true /* animateTaskViews */);
            return true;
        }
        return false;
    }

    /**
     * Dismisses Recents directly to Home without checking whether it is currently visible.
     */
    void dismissRecentsToHome(boolean animateTaskViews) {
        dismissRecentsToHome(animateTaskViews, null);
    }

    /**
     * Dismisses Recents directly to Home without checking whether it is currently visible.
     *
     * @param overrideAnimation If not null, will override the default animation that is based on
     *                          how Recents was launched.
     */
    void dismissRecentsToHome(boolean animateTaskViews, ActivityOptions overrideAnimation) {
        DismissRecentsToHomeAnimationStarted dismissEvent =
                new DismissRecentsToHomeAnimationStarted(animateTaskViews);
        dismissEvent.addPostAnimationCallback(new LaunchHomeRunnable(mHomeIntent,
                overrideAnimation));
        Recents.getSystemServices().sendCloseSystemWindows(
                BaseStatusBar.SYSTEM_DIALOG_REASON_HOME_KEY);
        EventBus.getDefault().send(dismissEvent);
    }

    /** Dismisses Recents directly to Home if we currently aren't transitioning. */
    boolean dismissRecentsToHomeIfVisible(boolean animated) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        //modify by liuzhicang "!isInMultiWindowMode()"
        if (ssp.isRecentsActivityVisible() && !isInMultiWindowMode()) {
            // Return to Home
            dismissRecentsToHome(animated);
            return true;
        }
        return false;
    }
    
    /**Mst: tangjun add begin*/
    //Tcl_monster,mod for SplitScreen ,jun_tang@tcl.com,2016.10.24{
    private void startParseSplitScreenXml( ) {
    	//XmlResourceParser parser = mContext.getResources().getXml(com.android.internal.R.xml.splitscreen_packagename);
    	XmlResourceParser parser = AppGlobals.getInitialApplication().getResources().getXml(com.android.internal.R.xml.splitscreen_packagename);
    	//XmlResourceParser parser = AppGlobals.getInitialApplication().getResources().getXml(R.xml.splitscreen_packagename);
    	if(mSplitScreenPackageList == null) {
    		mSplitScreenPackageList = new ArrayList<String>();
    		mSplitScreenPackageList.clear();
        	try {
        		parser.next();
        		int eventType = parser.getEventType();
        		while (eventType != XmlPullParser.END_DOCUMENT) {
        			if (eventType == XmlPullParser.START_TAG) {
        				String elemName = parser.getName();
        				if (SPLITSCREEN_XML_ITEM.equals(elemName)) {
        					String name = parser.getAttributeValue(null, "id");
        					if (name != null && (!"".equals(name))) {
        						mSplitScreenPackageList.add(name);
        					}
        				}
        			}
        			eventType = parser.next();
        		}
        	} catch (XmlPullParserException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    	}
	}
    //Tcl_monster}
    /**Mst: tangjun add end*/

    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFinishedOnStartup = false;
        
        int flag = getWindow().getDecorView().getSystemUiVisibility();
        getWindow().getDecorView().setSystemUiVisibility(flag | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // In the case that the activity starts up before the Recents component has initialized
        // (usually when debugging/pushing the SysUI apk), just finish this activity.
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp == null) {
            mFinishedOnStartup = true;
            finish();
            return;
        }

        // Register this activity with the event bus
        EventBus.getDefault().register(this, EVENT_BUS_PRIORITY);

        // Initialize the package monitor
        mPackageMonitor = new RecentsPackageMonitor();
        mPackageMonitor.register(this);

        //add by liuzhicang begin
        packageIntentReceiver = new PackageIntentReceiver();
        packageIntentReceiver.registerReceiver();
        //add by liuzhicang end

        // Set the Recents layout
        setContentView(R.layout.recents);
        takeKeyEvents(true);
        mRecentsView = (RecentsView) findViewById(R.id.recents_view);
        mRecentsView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mScrimViews = new SystemBarScrimViews(this);
        getWindow().getAttributes().privateFlags |=
                WindowManager.LayoutParams.PRIVATE_FLAG_FORCE_DECOR_VIEW_VISIBILITY;

        Configuration appConfiguration = Utilities.getAppConfiguration(this);
        mLastDeviceOrientation = appConfiguration.orientation;
        mLastDisplayDensity = appConfiguration.densityDpi;
        mFocusTimerDuration = getResources().getInteger(R.integer.recents_auto_advance_duration);
        mIterateTrigger = new DozeTrigger(mFocusTimerDuration, new Runnable() {
            @Override
            public void run() {
                dismissRecentsToFocusedTask(MetricsEvent.OVERVIEW_SELECT_TIMEOUT);
            }
        });

        // Set the window background
        getWindow().setBackgroundDrawable(mRecentsView.getBackgroundScrim());

        // Create the home intent runnable
        mHomeIntent = new Intent(Intent.ACTION_MAIN, null);
        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        // Register the broadcast receiver to handle messages when the screen is turned off
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        //Mst: tangjun add begin
        filter.addAction(CHANGETO_SPLITSCREEN);
        //Mst: tangjun add end
        registerReceiver(mSystemBroadcastReceiver, filter);

        getWindow().addPrivateFlags(LayoutParams.PRIVATE_FLAG_NO_MOVE_ANIMATION);

        // Reload the stack view
        reloadStackView();
        
        //Mst: tangjun add begin
        mLauncherApps = (LauncherApps) this.getSystemService("launcherapps");
        mIsFirstLauncher = true;
        mLayoutInflater = LayoutInflater.from(this);
        mOrientation = this.getResources().getConfiguration().orientation;
        startParseSplitScreenXml();
        mRecentsView.setSplitScreenPackageNames(mSplitScreenPackageList);
        //Mst: tangjun add end
        //add by liuzhicang begin
        inflateSplitScreenView();
        updateSplitScreenApplication();
        updateSplitScreenView();
        //add by liuzhicang end
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Notify that recents is now visible
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, true));
        MetricsLogger.visible(this, MetricsEvent.OVERVIEW_ACTIVITY);

        // Notify of the next draw
        mRecentsView.getViewTreeObserver().addOnPreDrawListener(mRecentsDrawnEventListener);
        

        //Mst: tangjun add begin
        mRecentsView.setViewClipPercent();
        //Mst: tangjun add end
    }

    //add by liuzhicang begin
    private void inflateSplitScreenView() {
        mSplitViewPagerFrame = Utilities.findViewStubById(this, R.id.splitscreen_recents).inflate();
        mSplitViewPagerFrame.setWillNotDraw(false);
        mSplitViewPagerFrame.setVisibility(View.GONE);
        mRecentsView.setVisibility(View.VISIBLE);
        mSplitViewPager = (ViewPager) mSplitViewPagerFrame.findViewById(R.id.viewpager);
        mPageIndicatorLinear = (LinearLayout) mSplitViewPagerFrame.findViewById(R.id.pageindicator);
        mEmptyWhiteView = mSplitViewPagerFrame.findViewById(R.id.empty_white_view);
    }

    private void updateSplitScreenApplication() {
        List<UserHandle> userHandleList = Recents.getSystemServices().getUserHandleList();
        mLauncherActivityInfoList = new ArrayList<LauncherActivityInfo>();
        mLauncherActivityInfoUserList = new ArrayList<UserHandle>();
        int index = 0;
        for (int i = 0; i < userHandleList.size(); i++) {
            //get all apps include fenshen app
            List<LauncherActivityInfo> tmp = mLauncherApps.getActivityList(null, userHandleList.get(i));
            mLauncherActivityInfoList.addAll(tmp);
            //mLauncherActivityInfoList = mLauncherApps.getActivityList(null, android.os.Process.myUserHandle());
            int tmpCount = mLauncherActivityInfoUserList.size();
            for (int k = tmpCount; k < (tmpCount + tmp.size()); k++) {
                mLauncherActivityInfoUserList.add(userHandleList.get(i));
            }
            int count = mLauncherActivityInfoList.size();
            for (int j = index; j < count; j++) {
                LauncherActivityInfo launcherActivityInfo = mLauncherActivityInfoList.get(index);
                if (launcherActivityInfo.getComponentName().getPackageName().contains("com.monster.launcher") ||
                        (SystemServicesProxy.mDockRunningTaskPackageName != null &&
                                launcherActivityInfo.getComponentName().getPackageName().contains(SystemServicesProxy.mDockRunningTaskPackageName)
                                && userHandleList.get(i).getIdentifier() == SystemServicesProxy.mDockRunningTaskUserId)
                        || !mSplitScreenPackageList.contains(launcherActivityInfo.getComponentName().getPackageName())) {
                    mLauncherActivityInfoList.remove(index);
                    mLauncherActivityInfoUserList.remove(index);
                } else {
                    index++;
                }
            }
        }
    }

    private void updateSplitScreenView() {

        int size = mLauncherActivityInfoList.size();
        int pageTotal = size % NUMBER_PER_PAGE > 0 ? size / NUMBER_PER_PAGE + 1 : size / NUMBER_PER_PAGE;
        //if(size > 0 && this.pageTotal != pageTotal) {
        if (size > 0) {
            this.pageTotal = pageTotal;
            ArrayList<GridView> mPageGridViews = new ArrayList<GridView>();
            mPageIndicatorLinear.removeAllViews();
            for (int i = 0; i < pageTotal; i++) {
                GridView grid = (GridView) mLayoutInflater.inflate(R.layout.splitscreen_gridview, null);
                mPageGridViews.add(grid);
                int end = Math.min((i + 1) * NUMBER_PER_PAGE, mLauncherActivityInfoList.size());
                List<LauncherActivityInfo> tmpLauncherActivityInfoList = mLauncherActivityInfoList.subList(i * NUMBER_PER_PAGE, end);
                List<UserHandle> tmpLauncherActivityInfoUserList = mLauncherActivityInfoUserList.subList(i * NUMBER_PER_PAGE, end);
                SplitscreenGridViewAdapter splitscreenGridViewAdapter = new SplitscreenGridViewAdapter(this, tmpLauncherActivityInfoList, tmpLauncherActivityInfoUserList);
                grid.setAdapter(splitscreenGridViewAdapter);
                ImageView v = new ImageView(this);
                v.setImageResource(R.drawable.splitscreen_dot);
                if (i == 0) {
                    v.setAlpha(1.0f);
                } else {
                    v.setAlpha(0.3f);
                }
                previousDotIndex = 0;
                if (i < PAGE_INDICATOR_MAX) {
                    mPageIndicatorLinear.addView(v);
                }
            }
            mSplitscreenPagerAdapter = new SplitscreenPagerAdapter(mPageGridViews);
            mSplitViewPager.setAdapter(mSplitscreenPagerAdapter);
            mSplitViewPager.setCurrentItem(previousDotIndex);
            mSplitViewPager.setOnPageChangeListener(new OnPageChangeListener() {

                @Override
                public void onPageSelected(int paramInt) {
                    // TODO Auto-generated method stub
                    if (paramInt >= PAGE_INDICATOR_MAX) {
                        return;
                    }
                    if (paramInt != previousDotIndex) {
                        View v = mPageIndicatorLinear.getChildAt(paramInt);
                        v.setAlpha(1.0f);
                        v = mPageIndicatorLinear.getChildAt(previousDotIndex);
                        v.setAlpha(0.3f);
                        previousDotIndex = paramInt;
                    }
                }

                @Override
                public void onPageScrolled(int paramInt1, float paramFloat, int paramInt2) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onPageScrollStateChanged(int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
        }
    }

    private void setSplitScreenVisibility(boolean splitEventFromRecentActivity) {
        boolean hasDockedTask = Recents.getSystemServices().hasDockedTask();
        if (hasDockedTask) {
            mRecentsView.setVisibility(View.GONE);
            mSplitViewPagerFrame.setVisibility(View.VISIBLE);
        } else {
            mSplitViewPagerFrame.setVisibility(View.GONE);
            mRecentsView.setVisibility(View.VISIBLE);
        }
        if (splitEventFromRecentActivity) {
            mEmptyWhiteView.setVisibility(View.VISIBLE);
        } else {
            mEmptyWhiteView.setVisibility(View.GONE);
        }
    }
    //add by liuzhicang end

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mReceivedNewIntent = true;

        // Reload the stack view
        reloadStackView();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	// TODO Auto-generated method stub
    	super.onWindowFocusChanged(hasFocus);
    	//Log.d("101010", "--RecentsActivity onWindowFocusChanged orientation = " + this.getResources().getConfiguration().orientation);
    	//Log.d("101010", "--RecentsActivity onWindowFocusChanged mOrientation = " + mOrientation);
        /**Mst: tangjun add begin*/
        if(mIsFirstLauncher) {
        	mRecentsView.onConfigurationChanged();
        	mOrientation = this.getResources().getConfiguration().orientation;
        	mIsFirstLauncher = false;
    	}
        /**Mst: tangjun add end*/

    }

    /**
     * Reloads the stack views upon launching Recents.
     */
    private void reloadStackView() {
        // If the Recents component has preloaded a load plan, then use that to prevent
        // reconstructing the task stack
        RecentsTaskLoader loader = Recents.getTaskLoader();
        RecentsTaskLoadPlan loadPlan = RecentsImpl.consumeInstanceLoadPlan();
        if (loadPlan == null) {
            loadPlan = loader.createLoadPlan(this);
        }

        // Start loading tasks according to the load plan
        RecentsConfiguration config = Recents.getConfiguration();
        RecentsActivityLaunchState launchState = config.getLaunchState();
        if (!loadPlan.hasTasks()) {
            loader.preloadTasks(loadPlan, launchState.launchedToTaskId,
                    !launchState.launchedFromHome);
        }

        RecentsTaskLoadPlan.Options loadOpts = new RecentsTaskLoadPlan.Options();
        loadOpts.runningTaskId = launchState.launchedToTaskId;
        loadOpts.numVisibleTasks = launchState.launchedNumVisibleTasks;
        loadOpts.numVisibleTaskThumbnails = launchState.launchedNumVisibleThumbnails;
        loader.loadTasks(this, loadPlan, loadOpts);
        TaskStack stack = loadPlan.getTaskStack();
        mRecentsView.onReload(mIsVisible, stack.getTaskCount() == 0);
        mRecentsView.updateStack(stack, true /* setStackViewTasks */);

        // Update the nav bar scrim, but defer the animation until the enter-window event
        boolean animateNavBarScrim = !launchState.launchedViaDockGesture;
        mScrimViews.updateNavBarScrim(animateNavBarScrim, stack.getTaskCount() > 0, null);

        // If this is a new instance relaunched by AM, without going through the normal mechanisms,
        // then we have to manually trigger the enter animation state
        boolean wasLaunchedByAm = !launchState.launchedFromHome &&
                !launchState.launchedFromApp;
        if (wasLaunchedByAm) {
            EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
        }

        // Keep track of whether we launched from the nav bar button or via alt-tab
        if (launchState.launchedWithAltTab) {
            MetricsLogger.count(this, "overview_trigger_alttab", 1);
        } else {
            MetricsLogger.count(this, "overview_trigger_nav_btn", 1);
        }

        // Keep track of whether we launched from an app or from home
        if (launchState.launchedFromApp) {
            Task launchTarget = stack.getLaunchTarget();
            int launchTaskIndexInStack = launchTarget != null
                    ? stack.indexOfStackTask(launchTarget)
                    : 0;
            MetricsLogger.count(this, "overview_source_app", 1);
            // If from an app, track the stack index of the app in the stack (for affiliated tasks)
            MetricsLogger.histogram(this, "overview_source_app_index", launchTaskIndexInStack);
        } else {
            MetricsLogger.count(this, "overview_source_home", 1);
        }

        // Keep track of the total stack task count
        int taskCount = mRecentsView.getStack().getTaskCount();
        MetricsLogger.histogram(this, "overview_task_count", taskCount);

        // After we have resumed, set the visible state until the next onStop() call
        mIsVisible = true;
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

        // Workaround for b/28705801, on first docking, we may receive the enter animation callback
        // before the first layout, so in such cases, send the event on the next frame after all
        // the views are laid out and attached (and registered to the EventBus).
        mHandler.removeCallbacks(mSendEnterWindowAnimationCompleteRunnable);
        if (!mReceivedNewIntent) {
            mHandler.post(mSendEnterWindowAnimationCompleteRunnable);
        } else {
            mSendEnterWindowAnimationCompleteRunnable.run();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
		
        mIgnoreAltTabRelease = false;
        mIterateTrigger.stopDozing();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Notify of the config change
        Configuration newDeviceConfiguration = Utilities.getAppConfiguration(this);
        /**Mst: tangjun mod begin*/
//        int numStackTasks = mRecentsView.getStack().getStackTaskCount();
//        EventBus.getDefault().send(new ConfigurationChangedEvent(false /* fromMultiWindow */,
//                mLastDeviceOrientation != newDeviceConfiguration.orientation,
//                mLastDisplayDensity != newDeviceConfiguration.densityDpi, numStackTasks > 0));
//        mLastDeviceOrientation = newDeviceConfiguration.orientation;
//        mLastDisplayDensity = newDeviceConfiguration.densityDpi;
        if(mRecentsView != null && mRecentsView.getStack() != null) {
        	int numStackTasks = mRecentsView.getStack().getStackTaskCount();
            EventBus.getDefault().send(new ConfigurationChangedEvent(false /* fromMultiWindow */,
                    mLastDeviceOrientation != newDeviceConfiguration.orientation,
                    mLastDisplayDensity != newDeviceConfiguration.densityDpi, numStackTasks > 0));
            mLastDeviceOrientation = newDeviceConfiguration.orientation;
            mLastDisplayDensity = newDeviceConfiguration.densityDpi;
        }
        /**Mst: tangjun mod end*/
        
        /**Mst: tangjun add begin*/
        if(mSplitViewPager != null) {
        	int viewpager_padding_top = 0;
        	if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        		viewpager_padding_top = this.getResources().getDimensionPixelSize(R.dimen.mst_splitscreen_viewpager_paddingtop_portait);
        	} else {
        		viewpager_padding_top = this.getResources().getDimensionPixelSize(R.dimen.mst_splitscreen_viewpager_paddingtop_landscape);
        	}
        	mSplitViewPager.setPadding(0, viewpager_padding_top, 0, 0);
        	
        	TextView select_docked_app_text = (TextView)findViewById(R.id.select_docked_app_text);
        	int select_docked_app_text_padding = this.getResources().getDimensionPixelSize(R.dimen.select_docked_app_margintop);
        	select_docked_app_text.setPadding(0, select_docked_app_text_padding, 0, 0);
        }
        /**Mst: tangjun add end*/
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        
        //Log.d("111111", "---onMultiWindowModeChanged isInMultiWindowMode = " + isInMultiWindowMode);
        //Log.d("111111", "---onMultiWindowModeChanged mSplitViewPagerFrame.getVisibility() = " + mSplitViewPagerFrame.getVisibility());
        //Log.d("111111", "---onMultiWindowModeChanged isRecentsActivityVisible = " + SystemServicesProxy.getInstance(this).isRecentsActivityVisible());
        /**Mst: tangjun add for when not docked and mSplitViewPagerFrame is visible, close the RecentsActivity begin*/
        if(!isInMultiWindowMode && SystemServicesProxy.getInstance(this).isRecentsActivityVisible() 
        		&& mSplitViewPagerFrame != null && mSplitViewPagerFrame.getVisibility() == View.VISIBLE) {
        	EventBus.getDefault().send(new ToggleRecentsEvent());
        }
        /**Mst: tangjun add for when not docked and mSplitViewPagerFrame is visible, close the RecentsActivity end*/

        // Reload the task stack completely
        RecentsConfiguration config = Recents.getConfiguration();
        RecentsActivityLaunchState launchState = config.getLaunchState();
        RecentsTaskLoader loader = Recents.getTaskLoader();
        RecentsTaskLoadPlan loadPlan = loader.createLoadPlan(this);
        loader.preloadTasks(loadPlan, -1 /* runningTaskId */,
                false /* includeFrontMostExcludedTask */);

        RecentsTaskLoadPlan.Options loadOpts = new RecentsTaskLoadPlan.Options();
        loadOpts.numVisibleTasks = launchState.launchedNumVisibleTasks;
        loadOpts.numVisibleTaskThumbnails = launchState.launchedNumVisibleThumbnails;
        loader.loadTasks(this, loadPlan, loadOpts);

        TaskStack stack = loadPlan.getTaskStack();
        int numStackTasks = stack.getStackTaskCount();
        boolean showDeferredAnimation = numStackTasks > 0;

        EventBus.getDefault().send(new ConfigurationChangedEvent(true /* fromMultiWindow */,
                false /* fromDeviceOrientationChange */, false /* fromDisplayDensityChange */,
                numStackTasks > 0));
        EventBus.getDefault().send(new MultiWindowStateChangedEvent(isInMultiWindowMode,
                showDeferredAnimation, stack));
		//add by liuzhicang begin
        if (isInMultiWindowMode && mEmptyWhiteView != null) {
            mEmptyWhiteView.setVisibility(View.GONE);
        }
		//add by liuzhicang end
    }

    @Override
    protected void onResume() {
        super.onResume();
        //add by liuzhicang begin
        sendBroadcast(new Intent(BaseActivity.ACTION_FINISH_ACTIVITY));
        setSplitScreenVisibility(false);
        //add by liuzhicang end
        
        /**Mst: tangjun add begin*/
        //if(mOrientation != this.getResources().getConfiguration().orientation) {
    	mRecentsView.onConfigurationChanged();
    	mOrientation = this.getResources().getConfiguration().orientation;
    	//}
    	mRecentsView.setMstTaskStackViewScrollWhenStart();
        /**Mst: tangjun add end*/
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Notify that recents is now hidden
        mIsVisible = false;
        mReceivedNewIntent = false;
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, false));
        MetricsLogger.hidden(this, MetricsEvent.OVERVIEW_ACTIVITY);

        // Workaround for b/22542869, if the RecentsActivity is started again, but without going
        // through SystemUI, we need to reset the config launch flags to ensure that we do not
        // wait on the system to send a signal that was never queued.
        RecentsConfiguration config = Recents.getConfiguration();
        RecentsActivityLaunchState launchState = config.getLaunchState();
        launchState.reset();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // In the case that the activity finished on startup, just skip the unregistration below
        if (mFinishedOnStartup) {
            return;
        }

        // Unregister the system broadcast receivers
        unregisterReceiver(mSystemBroadcastReceiver);

        // Unregister any broadcast receivers for the task loader
        mPackageMonitor.unregister();

        EventBus.getDefault().unregister(this);
        //add by liuzhicang begin
        packageIntentReceiver.unregisterReceiver();
        //add by liuzhicang end
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(mScrimViews, EVENT_BUS_PRIORITY);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(mScrimViews);
    }

    @Override
    public void onTrimMemory(int level) {
        RecentsTaskLoader loader = Recents.getTaskLoader();
        if (loader != null) {
            loader.onTrimMemory(level);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_TAB: {
                int altTabKeyDelay = getResources().getInteger(R.integer.recents_alt_tab_key_delay);
                boolean hasRepKeyTimeElapsed = (SystemClock.elapsedRealtime() -
                        mLastTabKeyEventTime) > altTabKeyDelay;
                if (event.getRepeatCount() <= 0 || hasRepKeyTimeElapsed) {
                    // Focus the next task in the stack
                    final boolean backward = event.isShiftPressed();
                    if (backward) {
                        EventBus.getDefault().send(new FocusPreviousTaskViewEvent());
                    } else {
                        EventBus.getDefault().send(
                                new FocusNextTaskViewEvent(0 /* timerIndicatorDuration */));
                    }
                    mLastTabKeyEventTime = SystemClock.elapsedRealtime();

                    // In the case of another ALT event, don't ignore the next release
                    if (event.isAltPressed()) {
                        mIgnoreAltTabRelease = false;
                    }
                }
                return true;
            }
            case KeyEvent.KEYCODE_DPAD_UP: {
                EventBus.getDefault().send(
                        new FocusNextTaskViewEvent(0 /* timerIndicatorDuration */));
                return true;
            }
            case KeyEvent.KEYCODE_DPAD_DOWN: {
                EventBus.getDefault().send(new FocusPreviousTaskViewEvent());
                return true;
            }
            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_FORWARD_DEL: {
                if (event.getRepeatCount() <= 0) {
                    EventBus.getDefault().send(new DismissFocusedTaskViewEvent());

                    // Keep track of deletions by keyboard
                    MetricsLogger.histogram(this, "overview_task_dismissed_source",
                            Constants.Metrics.DismissSourceKeyboard);
                    return true;
                }
            }
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onUserInteraction() {
        EventBus.getDefault().send(mUserInteractionEvent);
    }

    @Override
    public void onBackPressed() {
        // Back behaves like the recents button so just trigger a toggle event
        EventBus.getDefault().send(new ToggleRecentsEvent());
    }

    /**** EventBus events ****/

    public final void onBusEvent(ToggleRecentsEvent event) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (launchState.launchedFromHome) {
            dismissRecentsToHome(true /* animateTaskViews */);
        } else {
            dismissRecentsToLaunchTargetTaskOrHome();
        }
    }

    public final void onBusEvent(IterateRecentsEvent event) {
        final RecentsDebugFlags debugFlags = Recents.getDebugFlags();

        // Start dozing after the recents button is clicked
        int timerIndicatorDuration = 0;
        if (debugFlags.isFastToggleRecentsEnabled()) {
            timerIndicatorDuration = getResources().getInteger(
                    R.integer.recents_subsequent_auto_advance_duration);

            mIterateTrigger.setDozeDuration(timerIndicatorDuration);
            if (!mIterateTrigger.isDozing()) {
                mIterateTrigger.startDozing();
            } else {
                mIterateTrigger.poke();
            }
        }

        // Focus the next task
        EventBus.getDefault().send(new FocusNextTaskViewEvent(timerIndicatorDuration));

        MetricsLogger.action(this, MetricsEvent.ACTION_OVERVIEW_PAGE);
    }

    public final void onBusEvent(UserInteractionEvent event) {
        // Stop the fast-toggle dozer
        mIterateTrigger.stopDozing();
    }

    public final void onBusEvent(HideRecentsEvent event) {
        if (event.triggeredFromAltTab) {
            // If we are hiding from releasing Alt-Tab, dismiss Recents to the focused app
            if (!mIgnoreAltTabRelease) {
                dismissRecentsToFocusedTaskOrHome();
            }
        } else if (event.triggeredFromHomeKey) {
            dismissRecentsToHome(true /* animateTaskViews */);

            // Cancel any pending dozes
            EventBus.getDefault().send(mUserInteractionEvent);
        } else {
            // Do nothing
        }
    }

    public final void onBusEvent(EnterRecentsWindowLastAnimationFrameEvent event) {
        EventBus.getDefault().send(new UpdateFreeformTaskViewVisibilityEvent(true));
        mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        mRecentsView.invalidate();
    }

    public final void onBusEvent(ExitRecentsWindowFirstAnimationFrameEvent event) {
        if (mRecentsView.isLastTaskLaunchedFreeform()) {
            EventBus.getDefault().send(new UpdateFreeformTaskViewVisibilityEvent(false));
        }
        mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        mRecentsView.invalidate();
    }

    public final void onBusEvent(DockedFirstAnimationFrameEvent event) {
        mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        mRecentsView.invalidate();
    }

    public final void onBusEvent(CancelEnterRecentsWindowAnimationEvent event) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        int launchToTaskId = launchState.launchedToTaskId;
        if (launchToTaskId != -1 &&
                (event.launchTask == null || launchToTaskId != event.launchTask.key.id)) {
            SystemServicesProxy ssp = Recents.getSystemServices();
            ssp.cancelWindowTransition(launchState.launchedToTaskId);
            ssp.cancelThumbnailTransition(getTaskId());
        }
    }

    public final void onBusEvent(ShowApplicationInfoEvent event) {
        // Create a new task stack with the application info details activity
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", event.task.key.getComponent().getPackageName(), null));
        intent.setComponent(intent.resolveActivity(getPackageManager()));
        TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(intent).startActivities(null,
                        new UserHandle(event.task.key.userId));

        // Keep track of app-info invocations
        MetricsLogger.count(this, "overview_app_info", 1);
    }

    public final void onBusEvent(ShowIncompatibleAppOverlayEvent event) {
        if (mIncompatibleAppOverlay == null) {
            mIncompatibleAppOverlay = Utilities.findViewStubById(this,
                    R.id.incompatible_app_overlay_stub).inflate();
            mIncompatibleAppOverlay.setWillNotDraw(false);
            mIncompatibleAppOverlay.setVisibility(View.VISIBLE);
        }
        mIncompatibleAppOverlay.animate()
                .alpha(1f)
                .setDuration(INCOMPATIBLE_APP_ALPHA_DURATION)
                .setInterpolator(Interpolators.ALPHA_IN)
                .start();
    }

    public final void onBusEvent(HideIncompatibleAppOverlayEvent event) {
        if (mIncompatibleAppOverlay != null) {
            mIncompatibleAppOverlay.animate()
                    .alpha(0f)
                    .setDuration(INCOMPATIBLE_APP_ALPHA_DURATION)
                    .setInterpolator(Interpolators.ALPHA_OUT)
                    .start();
        }
    }

    public final void onBusEvent(DeleteTaskDataEvent event) {
        // Remove any stored data from the loader
        RecentsTaskLoader loader = Recents.getTaskLoader();
        loader.deleteTaskData(event.task, false);

        // Remove the task from activity manager
        SystemServicesProxy ssp = Recents.getSystemServices();
        ssp.removeTask(event.task.key.id);
    }

    public final void onBusEvent(AllTaskViewsDismissedEvent event) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.hasDockedTask()) {
            mRecentsView.showEmptyView(event.msgResId);
        } else {
            // Just go straight home (no animation necessary because there are no more task views)
            dismissRecentsToHome(false /* animateTaskViews */);
        }

        // Keep track of all-deletions
        MetricsLogger.count(this, "overview_task_all_dismissed", 1);
    }

    public final void onBusEvent(LaunchTaskSucceededEvent event) {
        MetricsLogger.histogram(this, "overview_task_launch_index", event.taskIndexFromStackFront);
    }

    public final void onBusEvent(LaunchTaskFailedEvent event) {
        // Return to Home
        dismissRecentsToHome(true /* animateTaskViews */);

        MetricsLogger.count(this, "overview_task_launch_failed", 1);
    }

    public final void onBusEvent(ScreenPinningRequestEvent event) {
        MetricsLogger.count(this, "overview_screen_pinned", 1);
    }

    public final void onBusEvent(DebugFlagsChangedEvent event) {
        // Just finish recents so that we can reload the flags anew on the next instantiation
        finish();
    }

    public final void onBusEvent(StackViewScrolledEvent event) {
        // Once the user has scrolled while holding alt-tab, then we should ignore the release of
        // the key
        mIgnoreAltTabRelease = true;
    }

    public final void onBusEvent(final DockedTopTaskEvent event) {
        //add by liuzhicang begin
        updateSplitScreenApplication();
        updateSplitScreenView();
        //add by liuzhicang end
        mRecentsView.getViewTreeObserver().addOnPreDrawListener(mRecentsDrawnEventListener);
        mRecentsView.invalidate();
    }

    @Override
    public boolean onPreDraw() {
        mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
        // We post to make sure that this information is delivered after this traversals is
        // finished.
        mRecentsView.post(new Runnable() {
            @Override
            public void run() {
                Recents.getSystemServices().endProlongedAnimations();
            }
        });
        return true;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        EventBus.getDefault().dump(prefix, writer);
        Recents.getTaskLoader().dump(prefix, writer);

        String id = Integer.toHexString(System.identityHashCode(this));

        writer.print(prefix); writer.print(TAG);
        writer.print(" visible="); writer.print(mIsVisible ? "Y" : "N");
        writer.print(" [0x"); writer.print(id); writer.print("]");
        writer.println();

        if (mRecentsView != null) {
            mRecentsView.dump(prefix, writer);
        }
    }

    private class PackageIntentReceiver extends BroadcastReceiver {
        void registerReceiver() {
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            RecentsActivity.this.registerReceiver(this, filter);
        }

        void unregisterReceiver() {
            RecentsActivity.this.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_PACKAGE_ADDED) || action.equals(Intent.ACTION_PACKAGE_REMOVED) || action.equals(Intent.ACTION_PACKAGE_CHANGED)) {
                updateSplitScreenApplication();
                updateSplitScreenView();
            }
            if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                //分屏模式下用户卸载了应用重新进入选择应用界面
                int dockSide = WindowManagerProxy.getInstance().getDockSide();
                if (dockSide != WindowManager.DOCKED_INVALID) {
                    int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
                    Uri data = intent.getData();
                    String pkgName = data.getEncodedSchemeSpecificPart();
                    if (pkgName.equals(mPkgNameInSplitScreenMode) && uid == mPkgUidInSplitScreenMode) {
                        EventBus.getDefault().send(new SplitScreenModeEvent(false));
                    }
                }
            }
        }
    }
}
