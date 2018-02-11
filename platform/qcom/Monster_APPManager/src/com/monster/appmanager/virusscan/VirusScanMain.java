package com.monster.appmanager.virusscan;

import java.util.LinkedList;
import java.util.List;

import com.monster.appmanager.FullActivityBase;
import com.monster.appmanager.R;
import com.monster.appmanager.Utils;
import com.monster.appmanager.db.MulwareProvider.MulwareTable;
import com.monster.appmanager.widget.HorizontalListViewAdapter;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class VirusScanMain extends FullActivityBase implements OnClickListener, OnItemClickListener {
	public static final String TAG = "VirusScanMain";
	public static final int REQUEST_CODE = 10001;
	private ListView allApps;
	private Button scanVirus;
	private Button oneKeyIntercept;
	private List<VirusInfo> virusList = new LinkedList<VirusInfo>();
	private LayoutInflater layoutInflater;
	private PackageManager pm;
	//未拦截广告数量
	private int virusCountNotIntercept;
	//已拦截广告数量
	private int virusCountIntercept;
	private mst.app.dialog.ProgressDialog mProDialog; 
	private View spaceView;
	private TextView mEmptyView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pm = getPackageManager();
		layoutInflater = LayoutInflater.from(this);
		setContentView(R.layout.virus_scan_main);
		allApps = (ListView) findViewById(R.id.all_apps);
		allApps.setOnItemClickListener(this);
		scanVirus = (Button) findViewById(R.id.button1);
		oneKeyIntercept = (Button) findViewById(R.id.button2);
		spaceView = (View) findViewById(R.id.space_view);
		scanVirus.setOnClickListener(this);
		oneKeyIntercept.setOnClickListener(this);
		mEmptyView = (TextView)findViewById(R.id.empty_msg);
		mEmptyView.setText(R.string.empty_ads);
		scanVirus.setText(R.string.virus_scan);
		oneKeyIntercept.setText(R.string.one_key_intercept);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		initList();
	}
	
	private void initList(){
		// 查询病毒库
		new Thread() {
			public void run() {
				virusCountIntercept = 0;
				virusCountNotIntercept = 0;
				Cursor cursor = getContentResolver().query(
						MulwareTable.CONTENT_URI,
						new String[] { MulwareTable.AD_PACKAGENAME , MulwareTable.AD_PROHIBIT, MulwareTable.AD_COUNT, MulwareTable.AD_TYPE}, null,
						null,  MulwareTable.AD_PROHIBIT);
				virusList.clear();
				if (cursor != null && cursor.getCount() > 0) {
					for (int i = 0; i < cursor.getCount(); i++) {
						if (cursor.moveToPosition(i)) {
							VirusInfo virusInfo = new VirusInfo();
							virusInfo.appEntry = getApplicationInfoByPackageName(cursor.getString(0));
							if(virusInfo.appEntry!=null && !Utils.isThirdPartyAppDisabled(virusInfo.appEntry)){
								boolean intercept = cursor.getInt(1)==1;
								if(intercept){
									virusCountIntercept++;
								}else{
									virusCountNotIntercept++;
								}
								virusInfo.prohibit = intercept;
//								virusInfo.adCount = cursor.getInt(2);
								virusInfo.adCount = getAdTypeCount(cursor.getInt(3));
								if(virusList.size()==0 || virusList.get(virusList.size()-1).prohibit!=virusInfo.prohibit){
									virusInfo.showTitle = true;
								}
								virusList.add(virusInfo);
							}
						}
					}
				}
				if (cursor != null) {
					cursor.close();
				}
				if(isInterceptingDialogShowing()) {
					queryHandler.sendEmptyMessageDelayed(0, 1000);
				} else {
					queryHandler.sendEmptyMessage(0);
				}
			};
		}.start();
	}

	private Handler queryHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			oneKeyIntercept.setVisibility(virusCountNotIntercept==0?View.GONE:View.VISIBLE);
			spaceView.setVisibility(virusCountNotIntercept==0?View.GONE:View.VISIBLE);
			mEmptyView.setVisibility((virusCountNotIntercept + virusCountIntercept) > 0 ? View.GONE : View.VISIBLE);
			allApps.setAdapter(new AppAdapter());
			closeInterceptingDialog();
		};
	};

	@Override
	public void onClick(View v) {
		if(v == scanVirus) {
//			Intent intent = new Intent(this, ScannerActivity.class);			
			Intent intent = new Intent(this, AdScanningActivity.class);			
			startActivityForResult(intent, REQUEST_CODE);
		} else if(v == oneKeyIntercept) {
			showInterceptingDialog();
			ContentValues contentValues = new ContentValues();
			contentValues.put(MulwareTable.AD_PROHIBIT, true);
			getContentResolver().update(
						MulwareTable.CONTENT_URI, contentValues,
						null, null);
			initList();
		}
	}


	/**
	 * 通过包名获取应用程序信息
	 * @param context Context对象。
	 * @param packageName 包名。
	 * @return 返回包名所对应的应用程序的名称。
	 */
	public ApplicationInfo getApplicationInfoByPackageName(String packageName) {
		
		ApplicationInfo applicationInfo = null;
		try {
			applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return applicationInfo;
	}

	class AppAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return virusList != null ? virusList.size() : 0;
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
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.virus_app_item, parent, false);
				ContentViews contentViews = new ContentViews();
				contentViews.title = (TextView) convertView.findViewById(android.R.id.title);
				contentViews.appImage = (ImageView) convertView.findViewById(android.R.id.icon);
				contentViews.appName = (TextView) convertView.findViewById(android.R.id.text1);
				contentViews.appVirusInfo = (TextView) convertView.findViewById(android.R.id.text2);
				convertView.setTag(contentViews);
			}
			ContentViews contentViews = (ContentViews) convertView.getTag();
			VirusInfo virusInfo = virusList.get(position);
			contentViews.appImage.setImageDrawable(HorizontalListViewAdapter.getApplicationIcon(virusInfo.appEntry,pm));
			contentViews.appName.setText(pm.getApplicationLabel(virusInfo.appEntry));
			contentViews.appVirusInfo.setText(getResources().getString(R.string.virus_count_info, virusInfo.adCount));
			if (virusInfo.showTitle) {
				contentViews.title.setVisibility(View.VISIBLE);
				contentViews.title.setText(getResources().getString(virusInfo.prohibit?R.string.virus_count_intercept_title:R.string.virus_count_title, 
						virusInfo.prohibit?virusCountIntercept:virusCountNotIntercept));
			} else {
				contentViews.title.setVisibility(View.GONE);
			}
			return convertView;
		}

		class ContentViews {
			private TextView title;
			private ImageView appImage;
			private TextView appName;
			private TextView appVirusInfo;
		}
	}
	
	class VirusInfo{
		public ApplicationInfo appEntry;
		public boolean prohibit;
		public int adCount;
		
		public boolean showTitle;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		VirusInfoActivity.virusInfo = virusList.get(position);
		Intent intent = new Intent(this, VirusInfoActivity.class);
		startActivity(intent);
	}
	
	private void showInterceptingDialog() {
		closeInterceptingDialog();
		mProDialog = new mst.app.dialog.ProgressDialog(this);
		mProDialog.setMessage(getString(R.string.intercepting_ad_dlg_title));
		mProDialog.show();
	}
	
	private void closeInterceptingDialog() {
		if(mProDialog != null && mProDialog.isShowing()) {
			mProDialog.dismiss();
			mProDialog = null;
		}
	}
	
	private boolean isInterceptingDialogShowing() {
		return mProDialog != null && mProDialog.isShowing();
	}
	
	private int getAdTypeCount(int adType) {
		int adTypeCount = 0;
		if((adType&MyQScanListener.TYPE_AD_BLOCK)!=0){
			adTypeCount ++;
		}
		if((adType&MyQScanListener.TYPE_AD_BANNER)!=0){
			adTypeCount ++;
		}
		if((adType&MyQScanListener.TYPE_AD_CHABO)!=0){
			adTypeCount ++;
		}
		
		adTypeCount = adTypeCount == 0 ? 1 : adTypeCount;
		return adTypeCount;
	}
}
