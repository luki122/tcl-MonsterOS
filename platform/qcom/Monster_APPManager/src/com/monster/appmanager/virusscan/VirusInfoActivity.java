package com.monster.appmanager.virusscan;

import com.monster.appmanager.FullActivityBase;
import com.monster.appmanager.R;
import com.monster.appmanager.db.MulwareProvider.MulwareTable;
import com.monster.appmanager.widget.HorizontalListViewAdapter;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class VirusInfoActivity extends FullActivityBase implements OnClickListener{
	private static final boolean SHOW_ALL_AD_TYPE = false;
	public static final String TAG = "VirusInfoActivity";
	public static VirusScanMain.VirusInfo virusInfo;
	private ImageView headImage;
	private ImageView intercept;
	private TextView name;
	private TextView version;
	private PackageManager pm;
	private View virus_notifycation;
	private View virus_banner;
	private View virus_alert;
	private Button intercept_or_not;
	private Button uninstall;
	private boolean adProhibit;
	private View leftSpace;
	private View rightSpace;
	private View imgContainer;
	
	private ImageView notificationBlockView;
	private ImageView bannerBlockView;
	private ImageView alertBlockView;
	private AnimationDrawable notificationBlockAni;
	private AnimationDrawable bannerBlockAni;
	private AnimationDrawable alertBlockAni;
	private View notificationBlockSuccessView;
	private View bannerBlockSuccessView;
	private View alertBlockSuccessView;
	private int adType;
	private boolean isAniming = false;;
	private Resources mResouce;

	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pm = getPackageManager();
		
		setContentView(R.layout.virus_app_info);
		mResouce = getResources();
		virus_alert = findViewById(R.id.virus_alert);
		intercept_or_not = (Button)findViewById(R.id.button1);
		intercept_or_not.setText(R.string.disintercept);
		uninstall = (Button)findViewById(R.id.button2);
		uninstall.setText(R.string.uninstall_app);
		intercept_or_not.setOnClickListener(this);
		uninstall.setOnClickListener(this);
		virus_banner = findViewById(R.id.virus_banner);
		virus_notifycation = findViewById(R.id.virus_notifycation);
		headImage = (ImageView)findViewById(android.R.id.icon);
		intercept = (ImageView)findViewById(R.id.intercept);
		name = (TextView)findViewById(android.R.id.text1);
		version = (TextView)findViewById(android.R.id.text2);
		leftSpace = findViewById(R.id.left_space);
		rightSpace = findViewById(R.id.right_space);
		imgContainer = findViewById(R.id.virus_content);
		notificationBlockView = (ImageView)findViewById(R.id.virus_notifycation_anim);
		bannerBlockView = (ImageView)findViewById(R.id.virus_banner_anim);
		alertBlockView = (ImageView)findViewById(R.id.virus_alert_anim);
		notificationBlockSuccessView = findViewById(R.id.virus_notification_protected);
		bannerBlockSuccessView = findViewById(R.id.virus_banner_protected);
		alertBlockSuccessView = findViewById(R.id.virus_alert_protected);
		headImage.setImageDrawable(HorizontalListViewAdapter.getApplicationIcon(virusInfo.appEntry,pm));
		name.setText(pm.getApplicationLabel(virusInfo.appEntry));
		
		try {
			PackageInfo pi = pm.getPackageInfo(virusInfo.appEntry.packageName, 0);  
			version.setText(getResources().getString(R.string.version_text, pi.versionName));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} 
		
		int adTypeCount = 0;
		Cursor cursor = getContentResolver().query(MulwareTable.CONTENT_URI, new String[]{MulwareTable.AD_TYPE, MulwareTable.AD_PROHIBIT}, MulwareTable.AD_PACKAGENAME+"=?", new String[]{virusInfo.appEntry.packageName}, null);
		if(cursor!=null && cursor.moveToFirst()){
			adProhibit = cursor.getInt(1)==1;
			adType = cursor.getInt(0);
			if((adType&MyQScanListener.TYPE_AD_BLOCK)!=0){
				virus_notifycation.setVisibility(View.VISIBLE);
				adTypeCount ++;
			}
			if((adType&MyQScanListener.TYPE_AD_BANNER)!=0){
				virus_banner.setVisibility(View.VISIBLE);
				adTypeCount ++;
			}
			if((adType&MyQScanListener.TYPE_AD_CHABO)!=0){
				virus_alert.setVisibility(View.VISIBLE);
				adTypeCount ++;
			}
			intercept.setVisibility(adProhibit?View.VISIBLE:View.INVISIBLE);
			intercept_or_not.setText(adProhibit?R.string.disintercept:R.string.intercept);
			
			if(SHOW_ALL_AD_TYPE) {
				adTypeCount = 3;
				virus_notifycation.setVisibility(View.VISIBLE);
				virus_banner.setVisibility(View.VISIBLE);
				virus_alert.setVisibility(View.VISIBLE);
			}
			if(adTypeCount > 1) {
				leftSpace.setVisibility(View.GONE);
				rightSpace.setVisibility(View.GONE);
			} else {
				imgContainer.setBackgroundColor(getColor(R.color.virus_bg_color));
			}
		}
		
		updateAnimView();
		updateBlockSuccessView();
	}
	
	@Override
	public void onClick(View v) {
		if(isAniming) {
			return;
		}
		
		if(v == intercept_or_not){//拦截/取消拦截
			interceptHandle();
			startAnim();
		}else if(v == uninstall){//卸载
			Uri packageURI=Uri.parse("package:"+virusInfo.appEntry.packageName);//xx是包名
            Intent intent=new Intent(Intent.ACTION_DELETE,packageURI);
            startActivityForResult(intent, 0);
		}
	}
	
	private void updateAnimView() {
		if((adType&MyQScanListener.TYPE_AD_BLOCK)!=0){
			if(adProhibit) {
				notificationBlockAni = (AnimationDrawable)mResouce.getDrawable(R.anim.notify_ad_unblock_ani);
			} else {
				notificationBlockAni = (AnimationDrawable)mResouce.getDrawable(R.anim.notify_ad_block_ani);
			}
			notificationBlockView.setBackground(notificationBlockAni);
		} 
		
		if((adType&MyQScanListener.TYPE_AD_BANNER)!=0){
			if(adProhibit) {
				bannerBlockAni = (AnimationDrawable)mResouce.getDrawable(R.anim.banner_ad_unblock_ani);
			} else {
				bannerBlockAni = (AnimationDrawable)mResouce.getDrawable(R.anim.banner_ad_block_ani);
			}
			bannerBlockView.setBackground(bannerBlockAni);
		}
		
		if((adType&MyQScanListener.TYPE_AD_CHABO)!=0){
			if(adProhibit) {
				alertBlockAni = (AnimationDrawable)mResouce.getDrawable(R.anim.view_ad_unblock_ani);
			} else {
				alertBlockAni = (AnimationDrawable)mResouce.getDrawable(R.anim.view_ad_block_ani);
			}
			alertBlockView.setBackground(alertBlockAni);
		}
	}
	
	private void startAnim() {
		isAniming = true;
		long maxDuration = 0;
		long duration = 0;
		if((adType&MyQScanListener.TYPE_AD_BLOCK)!=0){
			notificationBlockAni.start();
			maxDuration = (notificationBlockAni.getNumberOfFrames() - 1) * notificationBlockAni.getDuration(0);
		} 
		
		if((adType&MyQScanListener.TYPE_AD_BANNER)!=0){
			bannerBlockAni.start();
			duration = (bannerBlockAni.getNumberOfFrames() - 1) * bannerBlockAni.getDuration(0);
		}
		
		if(duration > maxDuration) {
			maxDuration = duration;
		}
		
		if((adType&MyQScanListener.TYPE_AD_CHABO)!=0){
			alertBlockAni.start();
			duration = (alertBlockAni.getNumberOfFrames() - 1) * alertBlockAni.getDuration(0);
		}
		
		if(duration > maxDuration) {
			maxDuration = duration;
		}
		
		mHandler.postDelayed(mOnAnimEndAction, maxDuration);
	}
	
	private Runnable mOnAnimEndAction = new Runnable() {
		public void run() {
			if(adProhibit) {
				updateBlockSuccessView();
			}
			updateAnimView();
			intercept.setVisibility(adProhibit?View.VISIBLE:View.INVISIBLE);
			isAniming = false;
		}
	};
	
	private void updateBlockSuccessView() {
		if((adType&MyQScanListener.TYPE_AD_BLOCK)!=0){
			notificationBlockSuccessView.setVisibility(adProhibit ? View.VISIBLE : View.INVISIBLE);
		} 
		
		if((adType&MyQScanListener.TYPE_AD_BANNER)!=0){
			bannerBlockSuccessView.setVisibility(adProhibit ? View.VISIBLE : View.INVISIBLE);
		}
		
		if((adType&MyQScanListener.TYPE_AD_CHABO)!=0){
			alertBlockSuccessView.setVisibility(adProhibit ? View.VISIBLE : View.INVISIBLE);
		}
	}
	
	private void interceptHandle() {
			ContentValues values = new ContentValues();
			values.put(MulwareTable.AD_PROHIBIT, adProhibit?false:true);
			adProhibit = !adProhibit;
			ContentResolver contentResolver = getContentResolver();
			Cursor cursor = contentResolver.query(MulwareTable.CONTENT_URI, new String[]{MulwareTable.AD_PACKAGENAME}, MulwareTable.AD_PACKAGENAME+"=?", new String[]{virusInfo.appEntry.packageName}, null);
			if(cursor.getCount()>0 && cursor.moveToFirst()){
				contentResolver.update(MulwareTable.CONTENT_URI, values, MulwareTable.AD_PACKAGENAME+"=?", new String[]{virusInfo.appEntry.packageName});
			}
			intercept_or_not.setText(adProhibit?R.string.disintercept:R.string.intercept);
			if(!adProhibit) {
				updateBlockSuccessView();
			}
	}
	
	private Handler mHandler = new Handler();
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		try {
			pm.getPackageInfo(virusInfo.appEntry.packageName, 0);  
		} catch (NameNotFoundException e) {
			finish();
		} 
	}
}
