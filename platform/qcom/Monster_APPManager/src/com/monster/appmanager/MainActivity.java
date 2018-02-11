package com.monster.appmanager;

import com.monster.appmanager.viewhelp.FunctionsHelp;
import com.monster.appmanager.virusscan.MainScanResultActivity;
import com.monster.appmanager.virusscan.PermissionScan;
import com.monster.appmanager.virusscan.PermissionScan.OnPermissionScanListener;
import com.monster.appmanager.virusscan.ScannerActivity;
import com.monster.appmanager.virusscan.VirusScanMain;
import com.monster.appmanager.widget.GestureView.GestureListener;
import com.monster.appmanager.widget.HorizontalListView;
import com.monster.appmanager.widget.HorizontalListViewAdapter;
import com.monster.appmanager.widget.ScanParent;
import com.monster.appmanager.widget.ScrollerApps;
import com.monster.appmanager.widget.panel.SlidingIndicator;
import com.monster.appmanager.widget.panel.SlidingUpPanelLayout;
import com.monster.appmanager.widget.panel.SlidingUpPanelLayout.PanelSlideListener;
import com.monster.appmanager.widget.panel.SlidingUpPanelLayout.PanelState;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends ScannerActivity implements OnClickListener, 
											GestureListener, OnPermissionScanListener, PanelSlideListener{
	private Button oneKeyScan;
	private LinearLayout galleryImageViewContent;
	private HorizontalListView galleryImageView;
	public ScanParent scanParent;
	private HorizontalListViewAdapter hListViewAdapter;
	private LinearLayout functionGroup;
	private ImageView functionOpen;
	private FunctionsHelp functionsHelp;
	public static MainActivity mainActivity;
	private PackageManager pm;
	private LayoutInflater mInflater;
	private View appCount;
	private int itemWidth = 0;
	private static final int[] gewei = {R.drawable.gewei0, R.drawable.gewei1, R.drawable.gewei2, R.drawable.gewei3, 
		R.drawable.gewei4, R.drawable.gewei5, R.drawable.gewei6, R.drawable.gewei7, R.drawable.gewei8,  R.drawable.gewei9};
	private static final int[] shiwei = {R.drawable.shiwei0, R.drawable.shiwei1, R.drawable.shiwei2, R.drawable.shiwei3, 
		R.drawable.shiwei4, R.drawable.shiwei5, R.drawable.shiwei6, R.drawable.shiwei7, R.drawable.shiwei8,  R.drawable.shiwei9};
	private static final int[] baiwei = {R.drawable.baiwei0, R.drawable.baiwei1, R.drawable.baiwei2, R.drawable.baiwei3, 
		R.drawable.baiwei4, R.drawable.baiwei5, R.drawable.baiwei6, R.drawable.baiwei7, R.drawable.baiwei8,  R.drawable.baiwei9};
	private ListView baiWei;
	private ListView shiWei;
	private ListView geWei;
	private boolean isAdScanning = false;
	private boolean isOneKeyScanning = false;
	private boolean isWaitingClosePannel = false;
	private View animView;
//	private GestureView mTagScrollView;
	private SlidingUpPanelLayout mSlidingPanel;
	private boolean isStop = false;
	private View mFunctionArea;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		isMainActivity = true;
		super.onCreate(savedInstanceState);
		showBackIcon(false);
		mainActivity = this;
		pm = getPackageManager();
		setMstContentView(R.layout.panel_container);
		initToolbar();
		setTitle(R.string.app_manager);
		appCount = findViewById(R.id.app_count);		
		appCount.setOnClickListener(this);
		scanParent = (ScanParent)findViewById(R.id.scan_img_parent);		
		
		galleryImageView = (HorizontalListView)findViewById(R.id.gallery_image_view);		
		oneKeyScan = (Button)findViewById(R.id.one_key_scan);
		galleryImageViewContent = (LinearLayout)findViewById(R.id.gallery_image_view_content);
		functionGroup = (LinearLayout)findViewById(R.id.function_group);
		functionOpen = (ImageView)findViewById(R.id.function_open);
		functionOpen.setOnClickListener(this);
		functionsHelp = new FunctionsHelp(); 
		functionsHelp.initViews(functionGroup, this);
		oneKeyScan.setOnClickListener(this);
		itemWidth = getResources().getDimensionPixelSize(R.dimen.thumnail_default_width);
		animView = findViewById(R.id.anim_view);
//		mTagScrollView = (GestureView)findViewById(R.id.function_area);
//		mTagScrollView.setListener(this);
		mFunctionArea = findViewById(R.id.function_area);
        mSlidingPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mSlidingPanel.addPanelSlideListener(this);
        mSlidingPanel.setClickable(false);
		findViewById(R.id.top_container).setOnClickListener(this);;
		mSlidingPanel.setIndicator((SlidingIndicator)functionOpen);
		
		mInflater = LayoutInflater.from(this);
		initNumberPicker();
		if(!isAdScanning()) {
			toggleButtonClickable(false);
		}
	}
	
	private void initNumberPicker(){
		baiWei=(ListView) findViewById(R.id.baiwei);
		shiWei=(ListView) findViewById(R.id.shiwei);
		geWei=(ListView) findViewById(R.id.gewei);
		baiWei.setEnabled(false);
		shiWei.setEnabled(false);
		geWei.setEnabled(false);
		geWei.setFriction(ViewConfiguration.getScrollFriction() * 0.2f);
	}

	@Override
	public void onClick(View v) {
		if(isOneKeyScanning) {
			return;
		}
		if(v != functionOpen && isPannelOpen()) {
			mSlidingPanel.setPanelState(PanelState.COLLAPSED);
			return;
		}
		if(v == functionOpen){
			if(!isWaitingClosePannel) {
//				functionsHelp.openOrCloseGridView();
//				functionGroup.scrollTo(1, 0);
				if(isPannelOpen()) {
					mSlidingPanel.setPanelState(PanelState.COLLAPSED);
				} else {
					mSlidingPanel.setPanelState(PanelState.EXPANDED);
				}
			}
		} else if(v == appCount){
			if(!isAdScanning()) {
				startActivity(new Intent(android.app.monster.MulwareProviderHelp.ACTION_APP_LIST));
			}
		} else if(v == oneKeyScan){
			if(onekeyScanHandle()) {
				return;
			}
			isOneKeyScanning = true;
			isWaitingClosePannel = false;
			functionOpen.setClickable(false);
			functionsHelp.setDisableClick(isOneKeyScanning);
			oneKeyScan.setVisibility(View.INVISIBLE);
			mFunctionArea.setVisibility(View.INVISIBLE);
			galleryImageViewContent.setVisibility(View.VISIBLE);
			mSlidingPanel.setTouchEnabled(false);
	        if(packagesName!=null && packagesName.size()>0){
	        	startScan();
	        	new Thread(){
	    			public void run() {
	    				if(!destroyEd){
	    					try {
	    						sleep(500);
	    						scroolHandler.sendEmptyMessage(packagesName.size()-1);
	    						ScrollerApps.DEFAULT_APP_COUNT = packagesName.size();
	    						sleep(ScrollerApps.DEFAULT_DURATION*ScrollerApps.DEFAULT_APP_COUNT);
	    						scroolHandler.sendEmptyMessage(packagesName.size());
	    					} catch (InterruptedException e) {
	    						e.printStackTrace();
	    					}
	    					
	    					/*for(int i=0; i<=packagesName.size(); i++){
	    						try {
		    						sleep(ScrollerApps.DEFAULT_DURATION);
		    					} catch (InterruptedException e) {
		    						e.printStackTrace();
		    					}
		    					scroolHandler.sendEmptyMessage(i);
	    					}*/
	    				}
	    			};
	    		}.start();
	        }
		}
	}

	@Override
	public void onLoadEntriesCompleted() {
        super.onLoadEntriesCompleted();
        hListViewAdapter = new HorizontalListViewAdapter(getApplicationContext(), entries, R.layout.horizontal_bottom_item, pm);  
		galleryImageView.setAdapter(hListViewAdapter);  
		
		int type = 100;
        if(entries.size()<100){
        	baiWei.setVisibility(View.GONE);
        	type = 10;
        }
        if(entries.size()<10){
        	shiWei.setVisibility(View.GONE);
        	type =1;
        }
        
        if(type == 1) {
        	geWei.setAdapter(new NumberAdapter(baiwei, 1));
        } else if(type == 10) {
        	geWei.setAdapter(new NumberAdapter(shiwei, 1));
        	shiWei.setAdapter(new NumberAdapter(baiwei, 10));
        } else {
        	geWei.setAdapter(new NumberAdapter(gewei, 1));
        	shiWei.setAdapter(new NumberAdapter(shiwei, 10));
        	baiWei.setAdapter(new NumberAdapter(baiwei, 100));
        }
                
        if(!isAdScanning()){
        	numberScroolHandler.sendEmptyMessageDelayed(0, 100);
        }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == FunctionsHelp.REQUEST_CODE){
			Intent intent = new Intent();
			intent.setClass(this, VirusScanMain.class);
			startActivity(intent);
		} else if(requestCode == FunctionsHelp.REQUEST_CODE_ONE_KEY_SCAN && resultCode == RESULT_OK) {
			numberScroolHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					oneKeyScan.performClick();
				}
			}, 250);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private boolean destroyEd = false;
	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroyEd = true;
		cancelScan();
	}
	
	@Override
	public void onLoadFinish(Message msg) {
		
		
	}
	
	
	private Handler numberScroolHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(!isStop) {
				if(viewVisible(geWei)) {
					geWei.smoothScrollToPosition((geWei.getAdapter().getCount()-1));
				}
				if(viewVisible(shiWei)) {
					shiWei.smoothScrollToPosition(shiWei.getAdapter().getCount()-1);
				}
				if(viewVisible(baiWei)) {
					baiWei.smoothScrollToPosition(baiWei.getAdapter().getCount()-1);
				}
			} else {
				resetListViewSelection();
			}
			numberScroolHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					toggleButtonClickable(true);
				}
			}, 500);
			//geWei.smoothScrollBy(geWei.getHeight()*geWei.getAdapter().getCount(), 1000);
			//shiWei.smoothScrollBy(shiWei.getHeight()*shiWei.getAdapter().getCount(), 1000);
			//baiWei.smoothScrollBy(baiWei.getHeight()*baiWei.getAdapter().getCount(), 1000);
		};
	};
	
	private Handler scroolHandler = new Handler(){
		public void handleMessage(Message msg) {
			//gallery_image_view.scrollTo(msg.what*itemWidth);		
			galleryImageView.scrollToPosition(msg.what, itemWidth);		
			if(msg.what == packagesName.size()){
				ScrollerApps.DEFAULT_APP_COUNT = 0;
				galleryImageView.scrollToPosition(0, itemWidth);		
				galleryImageViewContent.setVisibility(View.GONE);
				scroolHandler.removeMessages(packagesName.size());
				if(!isAdScanning()){
					oneKeyScan.setVisibility(View.VISIBLE);
					mFunctionArea.setVisibility(View.VISIBLE);
				}
				isOneKeyScanning = false;
				functionsHelp.setDisableClick(isOneKeyScanning);
				functionOpen.setClickable(true);
				mSlidingPanel.setTouchEnabled(true);
			}
		};
	};

	@Override
	public void onScaning(Message msg){
		if(viewVisible(geWei)) {
			geWei.setSelection(mCount%10);
		}
		if(viewVisible(shiWei)) {
			shiWei.setSelection(mCount/10);
		}
		if(viewVisible(baiWei)) {
			baiWei.setSelection(mCount/100);
		}
		
		if(mCount == packagesName.size()){
			mCount = 0;
			if(isAdScanning()) {
				super.onLoadFinish(msg);
				onScanComplete();
			} else {
//				new PermissionScan().scan(getApplicationContext(), this);
				onPermissionScanResult(0, 0);
			}
		}
	}
	
	class NumberAdapter extends BaseAdapter{
		private int type;
		private int[] numberIcons;
		
		public NumberAdapter(int[] numberIcons, int type) {
			this.numberIcons = numberIcons;
			this.type = type;
		}
		
		@Override
		public int getCount() {
			if(type == 1){
				return (entries.size()+1)%10+10;
			}else if(type == 10){
				return entries.size()/10+1;
			}else if(type ==100){
				return entries.size()/100+1;
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = mInflater.inflate(R.layout.number_item, parent, false);
			}
			
			((ImageView)convertView).setImageResource(numberIcons[position%numberIcons.length]);
			return convertView;
		}
		
	}
	
	public Button getOneKeyScan() {
		return oneKeyScan;
	}
	
	public boolean isAdScanning() {
		return isAdScanning;
	}

	public void setAdScanning(boolean isAdScanning) {
		this.isAdScanning = isAdScanning;
	}
	
	public boolean isPannelOpen() {
//		return animView.getVisibility() != View.VISIBLE;
		return mSlidingPanel.getPanelState() == PanelState.EXPANDED;
	}
	
	private boolean isPannelClose() {
		return mSlidingPanel.getPanelState() == PanelState.COLLAPSED;
	}
	
	private boolean onekeyScanHandle() {
		boolean result = false;
		if(isPannelOpen()) {
			if(!isWaitingClosePannel) {
				isWaitingClosePannel = true;
				functionOpen.performClick();
			}
			scroolHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					oneKeyScan.performClick();
				}
			}, 200);
			result = true;
		}
		
		return result;
	}

	@Override
	public void onGestureDown() {
		if(!isOneKeyScanning && isPannelOpen()) {
			functionOpen.performClick();
		}
	}

	@Override
	public void onGestureUp() {
		if(!isOneKeyScanning && !isPannelOpen()) {
			functionOpen.performClick();
		}
	}
	
	private void toggleButtonClickable(boolean clickable) {
		isOneKeyScanning = !clickable;
		functionOpen.setClickable(clickable);
		functionsHelp.setDisableClick(!clickable);
	}
	
	private boolean viewVisible(View view) 	{
		return view.getVisibility() == View.VISIBLE;
	}
	
	private void onScanComplete() {
		scroolHandler.sendEmptyMessageDelayed(packagesName.size(), 500);
	}
	
	@Override
	public void onPermissionScanResult(int enabledAlertWindowCounts, int enabledShortcutCounts) {
		if(!isDestroyed() && !isFinishing()) {
			Intent intent = new Intent(this, MainScanResultActivity.class);
			intent.putExtra(MainScanResultActivity.EXTRA_KEY_ALERT_WINDOW_COUNT, enabledAlertWindowCounts);
			intent.putExtra(MainScanResultActivity.EXTRA_KEY_SHORTCUT_COUNT, enabledShortcutCounts);
			startActivityForResult(intent, FunctionsHelp.REQUEST_CODE_ONE_KEY_SCAN);
		}
		onScanComplete();
	}

	@Override
	public void onPanelSlide(View panel, float slideOffset) {
	}

	@Override
	public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
		if(newState == PanelState.COLLAPSED) {
//			functionOpen.setImageResource(R.drawable.function_open);
		} else if(newState == PanelState.EXPANDED) {
//			functionOpen.setImageResource(R.drawable.function_close);
		}
	}
	
	private void initToolbar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
		ViewGroup mainContent = (ViewGroup)findViewById(R.id.main_content);
		View toolbar = getToolbar();
		((ViewGroup)toolbar.getParent()).removeView(toolbar);
		mainContent.addView(toolbar, 0);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		isStop = false;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		isStop = true;
		resetListViewSelection();
	}
	
	private void resetListViewSelection() {
		if(oneKeyScan.getVisibility() != View.VISIBLE) {
			return;
		}
		if(geWei.getAdapter() != null) {
			geWei.setSelection(geWei.getAdapter().getCount() - 1);
		}
		if(shiWei.getAdapter() != null) {
			shiWei.setSelection(shiWei.getAdapter().getCount() - 1);
		}
		if(baiWei.getAdapter() != null) {
			baiWei.setSelection(baiWei.getAdapter().getCount() - 1);
		}
	}
}