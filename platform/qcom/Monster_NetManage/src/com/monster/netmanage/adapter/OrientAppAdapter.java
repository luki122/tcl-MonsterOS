package com.monster.netmanage.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.monster.netmanage.R;
import com.monster.netmanage.entity.AppItem;
import com.monster.netmanage.utils.PreferenceUtil;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import mst.app.dialog.AlertDialog.Builder;
import mst.widget.CycleImageView;
import mst.widget.recycleview.RecyclerView;
import mst.widget.recycleview.RecyclerView.ViewHolder;

/**
 * 添加定向应用
 * 
 * @author zhaolaichao
 *
 */
public class OrientAppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	public final static int UPDATE_UI_TAG = 1000;
	/**
	 * 统计流量集合
	 */
	private ArrayList<AppItem> mStatsAppList = new ArrayList<AppItem>();
	/**
     * 定向应用集合
     */
    private ArrayList<AppItem> mAddedAppList = new ArrayList<AppItem>();
    /**
     * 要添加的应用集合
     */
    private ArrayList<AppItem> mAddAppList = new ArrayList<AppItem>();
    
	private Context mContext;
	private static PackageManager mPManager;
	private String mCurrectImsi;
	private Handler mHandler;
	private AppItemTask mAppItemTask;
	
	public OrientAppAdapter(Context context, Handler handler, String imsi) {
		super();
		this.mContext = context;
		this.mCurrectImsi = imsi;
		this.mHandler = handler;
		mPManager = mContext.getPackageManager();
	}

	/**
	 * 设置数据
	 * @param addAppList
	 */
	public void setAddAppList(ArrayList<AppItem> addAppList) {
		if (addAppList.size() == 0) {
			return;
		}
		mAddAppList.clear();
		for (int i = 0; i < addAppList.size(); i++) {
			AppItem addItem = addAppList.get(i);
			boolean isexit = false;
			for (int j = 0; j < mAddedAppList.size(); j++) {
				AppItem appItem = mAddedAppList.get(j);
				if (addItem.getAppUid() == appItem.getAppUid()) {
					isexit = true;
					break;
				}
			}
			if (!isexit) {
				mAddAppList.add(addItem);
			}
		}
		mAddedAppList.addAll(mAddAppList);
		Log.e("mAddedAppList", "mAddedAppList>>" + mAddedAppList.size());
		mAppItemTask = new AppItemTask();
		mAppItemTask.execute();
	}
	
	/**
	 * 统计所有的app的流量使用情况集合
	 * @param statsAppList
	 */
    public void setStatusApps(ArrayList<AppItem> statsAppList) {
    	mStatsAppList = statsAppList;
    }
    
    /**
     * 移除task
     */
    public void clear() {
    	if(null != mAppItemTask){
    		mAppItemTask.cancel(true);
    	}
    	if (mAddedAppList != null) {
    		mAddedAppList.clear();
    	}
    	if (mAddAppList != null) {
    		mAddAppList.clear();
    	}
    }
    
	@Override
	public int getItemCount() {
		return mAddedAppList.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		AppViewHolder appHolder = (AppViewHolder) viewHolder;
		appHolder.initData(mContext, mAddedAppList, position);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.orient_app_item, viewGroup, false);
		AppViewHolder viewHolder = new AppViewHolder(view);
		return viewHolder;
	}
	
   class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
	   RelativeLayout layItem;
    	CycleImageView civLogo;
    	TextView tvAppName;
    	TextView tvUseData;
    	ProgressBar pbData;
    	Button btnRemove;
    	
		public AppViewHolder(View itemView) {
			super(itemView);
			layItem = (RelativeLayout) itemView.findViewById(R.id.lay_item);
			civLogo = (mst.widget.CycleImageView) itemView.findViewById(R.id.imv_logo);
			tvAppName = (TextView) itemView.findViewById(R.id.tv_app);
			pbData = (ProgressBar) itemView.findViewById(R.id.pb_data);
			tvUseData = (TextView) itemView.findViewById(R.id.tv_use_data);
			btnRemove = (Button) itemView.findViewById(R.id.btn_remove);
			civLogo.setVisibility(View.VISIBLE);
			btnRemove.setVisibility(View.VISIBLE);
			pbData.setVisibility(View.VISIBLE);
			tvUseData.setVisibility(View.VISIBLE);
		}
    	
		/**
		 * 初始化数据
		 * @param context
		 * @param addedList
		 * @param position
		 */
		public void initData(Context context, ArrayList<AppItem> addedList, int position) {
			//获取要添加的应用
			AppItem appItem = addedList.get(position);
			PackageInfo packageInfo = appItem.getPackageInfo();
			ApplicationInfo applicationInfo = packageInfo.applicationInfo;
			String appName  = (String) applicationInfo.loadLabel(mPManager);
			layItem.setTag(position);
			tvUseData.setText(Formatter.formatFileSize(context, appItem.getAppData()));
			long maxData = addedList.get(0).getAppData();
			final int percentTotal = maxData != 0 ? (int) (appItem.getAppData() * 100 / maxData) : 0;
			pbData.setProgress(percentTotal);
			//获得应用的logo
			Drawable logo = applicationInfo.loadIcon(mPManager);
			civLogo.setImageDrawable(logo);
			tvAppName.setText(appName);
			btnRemove.setTag(position);
			btnRemove.setOnClickListener(this);
			layItem.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			int index = 0;
			switch(v.getId()) {
			case R.id.btn_remove:
				Button btnRemove = (Button) v;
				index = (Integer) btnRemove.getTag();
				showCleanDialog(index);
				break;
			case R.id.lay_item:
//				RelativeLayout layItem = (RelativeLayout) v;
//				index = (Integer) layItem.getTag();
				break;
			}
		}
    }
    
   /**
	 * 移除提示
	 */
	private void showCleanDialog(final int position) {
		PackageInfo packageInfo = mAddedAppList.get(position).getPackageInfo();
		ApplicationInfo applicationInfo = packageInfo.applicationInfo;
		String appName  = (String) applicationInfo.loadLabel(mPManager);
		mst.app.dialog.AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(mContext.getString(R.string.remove_orient_app_info));
		String message = String.format(mContext.getString(R.string.remove_orient_app_content), appName);
		builder.setMessage(message);
		builder.setPositiveButton(com.mst.R.string.ok, new mst.app.dialog.AlertDialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//提示用户
				removeApp(position);
			}
		});
		builder.setNegativeButton(com.mst.R.string.cancel, new mst.app.dialog.AlertDialog.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
			
		});
		builder.create().show();
	}
	
    /**
	 * 移除应用
	 * @param position
	 */
	private void removeApp(int position) {
		String addedAppUids = PreferenceUtil.getString(mContext, mCurrectImsi, PreferenceUtil.ORIENT_APP_ADDED_KEY, "");
		if (TextUtils.isEmpty(addedAppUids)){
			return;
		}
		if (addedAppUids.contains(",")) {
			String[] uidsArray = addedAppUids.split(",");
			PackageInfo packageInfo = mAddedAppList.get(position).getPackageInfo();
			int removeUid = packageInfo.applicationInfo.uid;
			ArrayList<String> uidList = new ArrayList<String>( Arrays.asList(uidsArray));
			if(uidList.contains("" + removeUid)) {
				uidList.remove("" + removeUid);
			}
			StringBuffer addedBf = new StringBuffer();
			for (int i = 0; i < uidList.size(); i++) {
				addedBf.append(uidList.get(i)).append(",");
			}
			//更新增加定向应用的UID
			PreferenceUtil.putString(mContext, mCurrectImsi, PreferenceUtil.ORIENT_APP_ADDED_KEY, addedBf.toString());
		}
		mAddedAppList.remove(position);
		notifyDataSetChanged();
		if(mAddedAppList.size() == 0) {
			mHandler.sendEmptyMessage(UPDATE_UI_TAG);
		}
	}
	 
	private class AppItemTask extends AsyncTask<Void, Void, Void> {

		public AppItemTask() {
			super();
		}

		@Override
		protected Void doInBackground(Void... params) {
			for (int i = 0; i < mAddAppList.size(); i++) {
				AppItem appItem = mAddAppList.get(i);
				for (int j = 0; j < mStatsAppList.size(); j++) {
					AppItem appItemData = mStatsAppList.get(j);
					if (appItem.getAppUid() == appItemData.getAppUid()) {
						Log.e("getAppData", "appItemData>>" + appItemData);
						appItem.setAppData(appItemData.getAppData());
						break;
					}
				}
			}
			Collections.sort(mAddedAppList);
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			notifyDataSetChanged();
		}
	}
}
