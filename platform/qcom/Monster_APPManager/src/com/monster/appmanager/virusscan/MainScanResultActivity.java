package com.monster.appmanager.virusscan;

import java.util.ArrayList;
import java.util.List;

import com.monster.appmanager.FullActivityBase;
import com.monster.appmanager.R;
import com.monster.appmanager.applications.InstalledAppDetails;
import com.monster.appmanager.db.MulwareProvider.MulwareTable;
import com.monster.appmanager.utils.PermissionApps;
import com.monster.appmanager.utils.PermissionApps.Callback;
import com.monster.appmanager.utils.PermissionApps.PermissionApp;
import com.monster.appmanager.utils.Utils;
import com.monster.appmanager.widget.PermissionsSelectPreference;
import com.monster.permission.ui.ManagePermissionsInfoActivity;
import com.monster.permission.ui.MstPermission;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.ArraySet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainScanResultActivity extends FullActivityBase implements OnClickListener, Callback, OnCheckedChangeListener{
	public static final String EXTRA_KEY_ALERT_WINDOW_COUNT = "alert_count";
	public static final String EXTRA_KEY_SHORTCUT_COUNT = "shortcut_count";
	private static final boolean IS_INCLUDE_ASK = true;
	private static final boolean IS_DEBUG = false;
	private static final boolean IS_SHOW_PERMISSION_ROW = false;
	private static final int ITEM_PERMISSION = 0;
	private static final int ITEM_ADS = 1;
	private static final int ITEM_OVER_LAY = 2;
	private static final int ITEM_SHORTCUT = 3;
	private Button startScan;
	private Button oneKeyIntercept;
	private CheckBox[] buttons = new CheckBox[4];
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		if(isJump()) {
			finish();
			return;
		}
		
		setContentView(R.layout.virus_main_scan_result);
		startScan = (Button) findViewById(R.id.rescan);
		startScan.setOnClickListener(this);
		oneKeyIntercept = (Button) findViewById(R.id.one_key_optimization);
		oneKeyIntercept.setOnClickListener(this);
//		oneKeyIntercept.setClickable(false);
		initViews();
		initRotateView();
		initPermission();
	}
	
	private final int[] rowViewIds = {R.id.permissions, R.id.advertisement, R.id.suspension_window, R.id.add_shortcut}; 
	private final int[] rowImageIds = {R.drawable.get_sensitive_permission, R.drawable.advertising_interception, R.drawable.suspension_window, R.drawable.add_shortcut}; 
	private final int[] rowTitleIds = {R.string.get_sensitive_permission, R.string.ad_intercept, R.string.suspension_window, R.string.add_shortcut}; 
	private final int[] rowSummeryIds = {R.string.get_sensitive_permission_summery, R.string.ad_intercept_summery, R.string.suspension_window_summery, R.string.add_shortcut_summery}; 
	private final int[] rowValues = new int[rowViewIds.length];
	
	private View[] rowViews = new View[rowViewIds.length];
	private ImageView[] rowIcons = new ImageView[rowViewIds.length];
	private TextView[] rowSummery = new TextView[rowViewIds.length];
	private ImageView rotateView;
	private ViewGroup decorView;
	private List<Integer> handleSequenceList = new ArrayList<>();
	private SwipeHelper swipeHelper;
	private boolean isAnimationStart = false;
    private PermissionApps mAlertWindowPermissions;
    private PermissionApps mShortcutPermissions;
    private ArraySet<String> mLauncherPkgs;
    private int[] mRotateLocation;
	
	private void initViews() {
		for(int i=0; i<rowViewIds.length; i++){
			View view = null;
			if(rowViews[i] == null) {
				view = findViewById(rowViewIds[i]);
				rowViews[i] = view;
				view.setBackgroundResource(R.drawable.dialog_btn_selector);
				view.setOnClickListener(this);
			} else {
				view = rowViews[i];
			}
			
			if(rowValues[i] <=0 && !IS_DEBUG) {
				rowViews[i].setVisibility(View.GONE);
				continue;
			}
			
			ImageView image = (ImageView)view.findViewById(android.R.id.icon);
			TextView title = (TextView)view.findViewById(android.R.id.text1);
			TextView summery = (TextView)view.findViewById(android.R.id.text2);
			buttons[i] = (CheckBox)view.findViewById(android.R.id.button1);
			
			buttons[i].setClickable(true);
			buttons[i].setChecked(true);
			buttons[i].setOnCheckedChangeListener(this);
//			image.setImageResource(R.drawable.rotate_optimize);
			image.setBackgroundResource(rowImageIds[i]);
			title.setText(rowTitleIds[i]);
			summery.setText(getResources().getString(rowSummeryIds[i], rowValues[i]));
			rowSummery[i] = summery;
			
			if(rowIcons[i] == null) {
				android.view.ViewGroup.LayoutParams params= image.getLayoutParams();
				params.width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
				params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
				image.getAdjustViewBounds();
				rowIcons[i] = image;
			}
		}
	}

	@Override
	public void onClick(View v) {
		if(v == startScan){
			setResult(RESULT_OK);
			finish();
		}else if(v == oneKeyIntercept){
			if(hasItemsSelected()) {
				startOptimize();
			} else {
				Toast.makeText(this, R.string.one_key_intercept_ask_for_item_select, Toast.LENGTH_SHORT).show();
			}
		} else if(v == rowViews[ITEM_ADS]) {
			Intent intent = new Intent(this, VirusScanMain.class);
			startActivity(intent);
		} else if(v == rowViews[ITEM_OVER_LAY]) {
			Intent intent = new Intent(this, ManagePermissionsInfoActivity.class);
			intent.setAction(ManagePermissionsInfoActivity.MANAGE_PERMISSION_APPS);
			intent.putExtra(Intent.EXTRA_PERMISSION_NAME, MstPermission.SYSTEM_ALERT_WINDOW_GROUP);
			startActivity(intent);
		} else if(v == rowViews[ITEM_SHORTCUT]) {
			Intent intent = new Intent(this, ManagePermissionsInfoActivity.class);
			intent.setAction(ManagePermissionsInfoActivity.MANAGE_PERMISSION_APPS);
			intent.putExtra(Intent.EXTRA_PERMISSION_NAME, MstPermission.INSTALL_SHORTCUT_GROUP);
			startActivity(intent);
		}
	}
	
	private void toggleAllClick(boolean clickable) {
		startScan.setClickable(clickable);
		oneKeyIntercept.setClickable(clickable);
		for (int i = 0; i < buttons.length; i++) {
			if(buttons[i] != null) {
				buttons[i].setClickable(clickable);
			}
			rowViews[i].setClickable(clickable);
		}
	}
	
	/**
	 *  禁止所有广告
	 */
	private void disabledAllAds() {
		ContentValues contentValues = new ContentValues();
		contentValues.put(MulwareTable.AD_PROHIBIT, true);
		getContentResolver().update(MulwareTable.CONTENT_URI, contentValues, null, null);
	}
	
	/**
	 * 禁止所有悬浮窗权限
	 */
	private void disabledAllOverlayPermission() {
		MstPermission.mstUpdatePermissionStatusToDb(getApplicationContext(), MstPermission.SYSTEM_ALERT_WINDOW_GROUP, MstPermission.DISABLE_MODE);
	}
	
	/**
	 * 禁止所有应用创建快捷方式
	 */
	private void disabledAllShortcutCreated() {
		MstPermission.mstUpdatePermissionStatusToDb(getApplicationContext(), MstPermission.INSTALL_SHORTCUT_GROUP, MstPermission.DISABLE_MODE);
	}
	
	private void disableHandle(int index) {
		switch (index) {
		case ITEM_PERMISSION:
			break;
		case ITEM_ADS:
			disabledAllAds();
			break;
		case ITEM_OVER_LAY:
			disabledAllOverlayPermission();
			break;
		case ITEM_SHORTCUT:
			disabledAllShortcutCreated();
			break;

		default:
			break;
		}
	}

	private void startOptimize() {
		handleSequenceList.clear();
		for (int i = 0; i < rowImageIds.length; i++) {
			if(buttons[i] != null && buttons[i].isChecked()) {
				handleSequenceList.add(i);
			}
		}
		
		if(handleSequenceList.size() >0){
			toggleAllClick(false);
			startAnimation();
		}
	}
	
	private void startAnimation() {
		if(handleSequenceList.size() > 0) {
			isAnimationStart = true;
			Integer index = handleSequenceList.remove(0);
			startRotateAnimate(rowIcons[index], index);
		} else {
			// all handle done
			isAnimationStart = false;
			toggleAllClick(!isAnimationStart);
			
			Intent intent = new Intent(this, OptimizeResultActivity.class);
			intent.putExtra(OptimizeResultActivity.TYPE, OptimizeResultActivity.TYPE_OPTIMIZE_COMPLETE);
			startActivity(intent);
			finish();
		}
	}
	
	private void initRotateView() {
		rotateView = new ImageView(this);
		rotateView.setImageResource(R.drawable.rotate_optimize);
    	decorView = (ViewGroup)getWindow().getDecorView();
    	swipeHelper = new SwipeHelper(getApplicationContext());
    	
    	if(!IS_SHOW_PERMISSION_ROW) {
    		rowViews[0].setVisibility(View.GONE);
    	}
	}
	
    private void startRotateAnimate(ImageView progressImage, final int index) {
    	int[] location = new int[2];
    	if(mRotateLocation != null) {
    		location[0] = mRotateLocation[0];
    		location[1] = mRotateLocation[1];
    	} else {
    		progressImage.getLocationInWindow(location);
    	}
    	
    	LayoutParams params = new LayoutParams(progressImage.getWidth(), progressImage.getHeight());
    	params.leftMargin = location[0];
    	params.topMargin = location[1];
    	if(mRotateLocation == null) {
    		mRotateLocation = new int[2];
    		mRotateLocation[0] = location[0];
    		mRotateLocation[1] = location[1];
    	}
    	decorView.addView(rotateView, params);
    	
        int times = 1;
        int duration=400;
        long totalDuration = duration * times;
        
        Interpolator lin = new LinearInterpolator();
//        Interpolator lin = AnimationUtils.loadInterpolator(getApplicationContext(), android.R.interpolator.fast_out_linear_in);
        Animation am = new RotateAnimation(0, 360 * times, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        am.setDuration(totalDuration);
        am.setRepeatCount(0);
        am.setInterpolator(lin);
        am.setFillAfter(false);
        rotateView.setAnimation(am);
        am.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				buttons[index].setChecked(false);
				decorView.removeView(rotateView);
				
				dismissRowView(rowViews[index], index);
			}
		});
        am.startNow();
        
        disableHandle(index);
    }
    
    private void dismissRowView(View view, final int index) {
    	Runnable endAction = new Runnable() {
			@Override
			public void run() {
				rowViews[index].setVisibility(View.GONE);
				startAnimation();
			}
		};
    	swipeHelper.dismissChild(view, 0, endAction, 180, true, 260);
    }

    private static class SwipeHelper{
        public static final int X = 0;
        public static final int Y = 1;
        private int mSwipeDirection;
        private int MAX_ESCAPE_ANIMATION_DURATION = 400; // ms
        private int DEFAULT_ESCAPE_ANIMATION_DURATION = 200; // ms
        
        private final Interpolator mFastOutLinearInInterpolator;
        private static LinearInterpolator sLinearInterpolator = new LinearInterpolator();
        
        public SwipeHelper(Context context) {
            mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context,
                    android.R.interpolator.fast_out_linear_in);
		}

        private ObjectAnimator createTranslationAnimation(View v, float newPos) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(v,
                    mSwipeDirection == X ? "translationX" : "translationY", newPos);
            return anim;
        }
    	
    	public void dismissChild(final View view, float velocity, final Runnable endAction,
    			long delay, boolean useAccelerateInterpolator, long fixedDuration) {
    		final View animView = view;
    		float newPos;
    		boolean isLayoutRtl = view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;

    		if (velocity < 0
    				|| (velocity == 0 && getTranslation(animView) < 0)
    				|| (velocity == 0 && getTranslation(animView) == 0 && mSwipeDirection == Y)
    				|| (velocity == 0 && getTranslation(animView) == 0 && isLayoutRtl)) {
    			newPos = -getSize(animView);
    		} else {
    			newPos = getSize(animView);
    		}
    		long duration;
    		if (fixedDuration == 0) {
    			duration = MAX_ESCAPE_ANIMATION_DURATION;
    			if (velocity != 0) {
    				duration = Math.min(duration,
    						(int) (Math.abs(newPos - getTranslation(animView)) * 1000f / Math
    								.abs(velocity))
    						);
    			} else {
    				duration = DEFAULT_ESCAPE_ANIMATION_DURATION;
    			}
    		} else {
    			duration = fixedDuration;
    		}

    		animView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    		ObjectAnimator anim = createTranslationAnimation(animView, newPos);
    		if (useAccelerateInterpolator) {
    			anim.setInterpolator(mFastOutLinearInInterpolator);
    		} else {
    			anim.setInterpolator(sLinearInterpolator);
    		}
    		anim.setDuration(duration);
    		if (delay > 0) {
    			anim.setStartDelay(delay);
    		}
    		anim.addListener(new AnimatorListenerAdapter() {
    			public void onAnimationEnd(Animator animation) {
    				if (endAction != null) {
    					endAction.run();
    				}
    				animView.setLayerType(View.LAYER_TYPE_NONE, null);
    			}
    		});
    		anim.start();
    	}

    	protected float getTranslation(View v) {
    		return mSwipeDirection == X ? v.getTranslationX() : v.getTranslationY();
    	}
    	
        private float getSize(View v) {
            return mSwipeDirection == X ? v.getMeasuredWidth() :
                    v.getMeasuredHeight();
        }
    }
    
    private void initItemCount() {
    	initAdsCount();
    	initOverlayCount();
    	initShortcutCreatedCount();
    }
    
    private void initAdsCount() {
		Cursor cursor = getContentResolver().query(
				MulwareTable.CONTENT_URI, new String[] { MulwareTable.AD_PROHIBIT,},MulwareTable.AD_PROHIBIT+"=?", new String[] { "0",},  MulwareTable.AD_PROHIBIT);
		if(cursor!=null){
			rowValues[ITEM_ADS] = cursor.getCount();
			cursor.close();
		}
    }
    
    private void initOverlayCount() {
//    	rowValues[ITEM_OVER_LAY] = getIntent().getIntExtra(EXTRA_KEY_ALERT_WINDOW_COUNT, 0);
    	rowValues[ITEM_OVER_LAY] = MstPermission.mstGetNotDiabledPermissionCount(getApplicationContext(), MstPermission.SYSTEM_ALERT_WINDOW_GROUP);
    }
    
    private void initShortcutCreatedCount() {
//    	rowValues[ITEM_SHORTCUT] = getIntent().getIntExtra(EXTRA_KEY_SHORTCUT_COUNT, 0);
    	rowValues[ITEM_SHORTCUT] = MstPermission.mstGetNotDiabledPermissionCount(getApplicationContext(), MstPermission.INSTALL_SHORTCUT_GROUP);
    }
    
    private boolean isJump() {
    	boolean result = true;
    	
    	initItemCount();
    	for (int i = 0; i < rowValues.length; i++) {
			if(rowValues[i] > 0) {
				result = false;
				break;
			}
		}
    	
    	if(result && !IS_DEBUG) {
    		Intent intent = new Intent(this, OptimizeResultActivity.class);
    		intent.putExtra(OptimizeResultActivity.TYPE, OptimizeResultActivity.TYPE_SEARCH_COMPLETE);
    		startActivity(intent);
    	}
    	
    	if(IS_DEBUG) {
    		result = false;
    	}
    	
    	return result;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	refreshUi();
    	updateAdsCount();
    }
        
    private void refreshUi() {
    	for (int i = 0; i < rowValues.length; i++) {
    		if(rowViewIds[i] == R.id.suspension_window && rowValues[i] > 0 && mAlertWindowPermissions != null) {
    			mAlertWindowPermissions.refresh(true);
    		} else if(rowViewIds[i] == R.id.add_shortcut && rowValues[i] > 0 && mShortcutPermissions != null) {
    			mShortcutPermissions.refresh(true);
    		}
    	}
    }
    
    @Override
	public void onPermissionsLoaded(PermissionApps permissionApps) {
		if(permissionApps == mAlertWindowPermissions) {
			updatePermissionItem(permissionApps, ITEM_OVER_LAY);
		} else if(permissionApps == mShortcutPermissions) {
			updatePermissionItem(permissionApps, ITEM_SHORTCUT);
		}
	}
    
    private void updateAdsCount() {
    	initAdsCount();
    	updateRow(ITEM_ADS);
    }
    
    private void initPermission() {
    	boolean hasPermission = false;
    	for (int i = 0; i < rowValues.length; i++) {
    		/*if(rowViewIds[i] == R.id.suspension_window && rowValues[i] > 0) {
    			mAlertWindowPermissions = new PermissionApps(this, Manifest.permission_group.SYSTEM_ALERT_WINDOW, this);
    			hasPermission = true;
    		} else if(rowViewIds[i] == R.id.add_shortcut && rowValues[i] > 0) {
    			mShortcutPermissions = new PermissionApps(this, Manifest.permission_group.INSTALL_SHORTCUT, this);
    			hasPermission = true;
    		}*/
    	}
    	if(hasPermission) {
    		mLauncherPkgs = Utils.getLauncherPackages(this);
    	}
    }

	private void updatePermissionItem(PermissionApps permissionApps, int itemIndex) {
		int[] indexCount;
		int enabledCount; 
		indexCount = permissionApps.getAllTypeCount(mLauncherPkgs);
		enabledCount = indexCount[PermissionsSelectPreference.OPEN];
		if(IS_INCLUDE_ASK) {
			enabledCount += indexCount[PermissionsSelectPreference.ASK];
		}
		rowValues[itemIndex] = enabledCount;
		updateRow(itemIndex);
	}
	
	private void updateRow(int itemIndex) {
		if(rowValues[itemIndex] > 0) {
			updateRowSummery(itemIndex);
		} else {
			rowViews[itemIndex].setVisibility(View.GONE);
		}
	}
	
    private void updateRowSummery(int index) {
    	initAdsCount();
    	rowSummery[index].setText(getResources().getString(rowSummeryIds[index], rowValues[index]));
    }
    
//    private void updateOneKeyIntercept() {
//    	boolean clickable = false;
//		for (int i = 0; i < rowImageIds.length; i++) {
//			if(buttons[i] != null && buttons[i].isChecked()) {
//				clickable = true;
//				break;
//			}
//		}
//		oneKeyIntercept.setClickable(clickable);
//    }

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		oneKeyIntercept.setEnabled(hasItemsSelected());
//		updateOneKeyIntercept();
	}
	
	private boolean hasItemsSelected() {
		boolean result = false;
		
		for (int i = 0; i < rowImageIds.length; i++) {
			if(buttons[i] != null && buttons[i].isChecked()) {
				result = true;
				break;
			}
		}
		return result;
	}
}
