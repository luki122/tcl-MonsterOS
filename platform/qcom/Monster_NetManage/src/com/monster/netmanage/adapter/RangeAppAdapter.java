package com.monster.netmanage.adapter;

import static android.net.NetworkPolicyManager.POLICY_NONE;
//import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED;
import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;

import java.util.ArrayList;
import java.util.Collections;

import com.monster.netmanage.R;
import com.monster.netmanage.entity.AppItem;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.NetworkPolicyManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

/**
 * 流量应用排行
 * 
 * @author zhaolaichao
 *
 */
public class RangeAppAdapter  extends BaseAdapter{
	private final static String TAG = "RangeAppAdapter"; 
	public static final int CHANGE_STATE_TAG = 1;
	public static final String TYPE_DATA = "type_data";
    public static final String TYPE_WLAN = "type_wlan";
    public static final String STOP_NET = "STOP_NET";
    public static final String USE_NET = "USE_NET";
	private Context mContext;
	private PackageManager mPManager;
	private NetworkPolicyManager mPolicyManager;
	private INetworkManagementService mNetworkService;
	/**
	 * 流量应用集合
	 */
    private ArrayList<AppItem> mAppList = new ArrayList<AppItem>();
    private ArrayList<AppItem> mAppInfosByPolicy = new ArrayList<AppItem>();
	private ArrayList<AppItem> mAppInfosNoPolicy = new ArrayList<AppItem>();
	private ArrayList<AppItem> mAppInfosData = new ArrayList<AppItem>();
	private ArrayList<ArrayList<AppItem>> mArrayLists = new ArrayList<ArrayList<AppItem>>();
   
	private String mNetType;
	private String mUseNet;
    private Handler mHandler; 
    
   public RangeAppAdapter(Context context, NetworkPolicyManager networkPolicyManager, Handler handler) {
		super();
		this.mPolicyManager = networkPolicyManager;
		this.mContext = context;
		mHandler = handler;
		mPManager = mContext.getPackageManager();
		mNetworkService = INetworkManagementService.Stub.asInterface(
                ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
	}
   
	public void setNetType(String metType) {
	     this.mNetType = metType;
    }

	public void setUseNet(String useNet) {
	     this.mUseNet = useNet;
   }
	public void setAppList(ArrayList<AppItem> appList) {
		mAppList.clear();
		this.mAppList.addAll(appList);
		Log.v(TAG, "setAppList>>移动数据>>>" + mAppList.size());
	}

	public ArrayList<AppItem> getAppList() {
		return mAppList;
	}
	
	/**
	 * 允许使用网络
	 * @param appList
	 */
	public void setUsedNetList(ArrayList<AppItem> appList) {
		mAppInfosNoPolicy = null;
		mAppInfosNoPolicy = new ArrayList<AppItem>();
		mAppInfosNoPolicy.addAll(appList);
		Log.v(TAG, "mAppInfosNoPolicy>>移动数据>>>" + mAppInfosNoPolicy.size() + ">>>appList>" + appList.size());
	}
	
	/**
	 * 禁止使用网络
	 * @param appList
	 */
	public ArrayList<AppItem> getAppInfosByPolicy() {
		return mAppInfosByPolicy;
	}
	
	/**
	 * 禁止使用网络
	 * @param appList
	 */
	public void setStopNetList(ArrayList<AppItem> appList) {
		mAppInfosByPolicy = null;
		mAppInfosByPolicy = new ArrayList<AppItem>();
		mAppInfosByPolicy.addAll(appList);
		Log.v(TAG, "mAppInfosByPolicy>>after>>>" + mAppInfosByPolicy.size() + ">>>appList>" + appList.size());
	}
	
	/**
	 * 允许使用网络
	 * @param appList
	 */
	public ArrayList<AppItem> getAppInfosNoPolicy() {
		return mAppInfosNoPolicy;
	}

	/**
	 * 设置流量应用的集合
	 * @param appInfosData
	 */
	public void setAppInfosData(ArrayList<AppItem> appInfosData) {
		this.mAppInfosData = appInfosData;
		Log.v(TAG, "mAppInfosData>>>" + mAppInfosData.size());
		AppItemTask appItemTask = new AppItemTask();
		appItemTask.execute();
	}


	/**
	 * 禁止使用网络
	 * @param uid
	 * @param policy   网络类型
	 */
	 private void applyDataChange(int uid, int policy) {
	        try {
				mPolicyManager.setUidPolicy(uid, policy);
	        	Log.v(TAG, "uid->>>>>>" + uid);
	        } catch (Exception e) {
	        	e.printStackTrace();
	            Log.e("ttt", "No bandwidth control; leaving>>>" + e.getMessage());
	        }
	 }
	 
	 /**
	   * 设置上网类型
	   * @param type
	   * @param uid
	   * @param isReject
	   */
   private void applyChange(String type, int uid, boolean isReject) {
	   try {
		   switch (type) {
		   case TYPE_DATA:
			   mNetworkService.setUidDataRules(uid, isReject); //传入true代表要禁止其联网。
			   break;
		   case TYPE_WLAN:
			   mNetworkService.setUidWlanRules(uid, isReject);
			   break;
		   }
	  } catch (RemoteException e) {
		  e.printStackTrace();
	  }
   }
   
   private void save(String type, ArrayList<AppItem> saveList) {
	   if (saveList.size() == 0) {
		   PreferenceUtil.putString(mContext, "", type, null);
           return;
       }
       StringBuilder sb = new StringBuilder();
       for (AppItem appItem : saveList) {
           sb.append(appItem.getAppUid()).append(",");
       }
       Log.d(TAG, "sb:" + sb);
       switch (type) {
           case TYPE_DATA:
        	   PreferenceUtil.putString(mContext, "", TYPE_DATA, sb.substring(0, sb.length() - 1));
               break;
           case TYPE_WLAN:
        	   PreferenceUtil.putString(mContext, "", TYPE_WLAN, sb.substring(0, sb.length() - 1));
               break;
       }
   }
   
	@Override
	public int getCount() {
		return mAppList.size();
	}

	@Override
	public Object getItem(int position) {
		return mAppList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if ( convertView == null ) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.orient_app_item, null);
			viewHolder.imvLogo = (ImageView) convertView.findViewById(R.id.imv_logo);
			viewHolder.tvAppName = (TextView) convertView.findViewById(R.id.tv_app);
			viewHolder.pbData = (ProgressBar) convertView.findViewById(R.id.pb_data);
			viewHolder.tvUseData = (TextView) convertView.findViewById(R.id.tv_use_data);
			viewHolder.tBtnRange = (Switch) convertView.findViewById(R.id.togglebtn);
			viewHolder.imvLogo.setVisibility(View.VISIBLE);
			viewHolder.tBtnRange.setVisibility(View.VISIBLE);
			viewHolder.pbData.setVisibility(View.VISIBLE);
			viewHolder.tvUseData.setVisibility(View.VISIBLE);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.tBtnRange.setOnCheckedChangeListener(null);
		//获取要添加的应用
		AppItem appItem = mAppList.get(position);
		PackageInfo packageInfo = appItem.getPackageInfo();
		ApplicationInfo applicationInfo = packageInfo.applicationInfo;
		String appName  = (String) applicationInfo.loadLabel(mPManager);
		viewHolder.tvUseData.setText(StringUtil.formatFloatDataFlowSize(mContext, appItem.getAppData()));
		long maxData = mAppList.get(0).getAppData();
		final int percentTotal = maxData != 0 ? (int) (appItem.getAppData() * 100 / maxData) : 0;
		viewHolder.pbData.setProgress(percentTotal);
		//获得应用的logo
		Drawable logo = appItem.getPackageInfo().applicationInfo.loadIcon(mPManager);
		viewHolder.imvLogo.setImageDrawable(logo);
		viewHolder.tvAppName.setText(appName);
		viewHolder.tBtnRange.setTag(position);
		int policy = mPolicyManager.getUidPolicy(applicationInfo.uid);
		//检查uid是否设置了，并设置checkbox。
		if (mNetType.equals(mContext.getString(R.string.data_mobile)) || mNetType.equals(mContext.getString(R.string.data_wifi))) {
			boolean isMatch = false;
			for (int i = 0; i < mAppInfosNoPolicy.size(); i++) {
				if (applicationInfo.uid == mAppInfosNoPolicy.get(i).getAppUid()) {
					isMatch = true;
					break;
				} 
			}
			if (isMatch) {
				viewHolder.tBtnRange.setChecked(true);
			} else {
				viewHolder.tBtnRange.setChecked(false);
			}
		} else if (mNetType.equals(mContext.getString(R.string.net_bg))) {
			if (policy == POLICY_REJECT_METERED_BACKGROUND) {
				viewHolder.tBtnRange.setChecked(false);
			} else {
				viewHolder.tBtnRange.setChecked(true);
			}
		}
		viewHolder.tBtnRange.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int index = (Integer) buttonView.getTag();
				Switch tBtn = (Switch) buttonView;
				setOnSelectedListener(index, tBtn);
			}
		});
		return convertView;
	}

	class ViewHolder {
		ImageView imvLogo;
		TextView tvAppName;
		ProgressBar pbData;
		TextView tvUseData;
		Switch tBtnRange;
	}
	
	/**
	 * 点击switch事件 
	 * @param switchBtn
	 */
	public void setOnSelectedListener(int index, Switch tBtn) {
		PackageInfo packageInfoItem = mAppList.get(index).getPackageInfo();
		int uid = packageInfoItem.applicationInfo.uid;
		String typeNet = null;
		if (tBtn.isChecked()) {
			if (mNetType.equals(mContext.getString(R.string.data_mobile))) {
				//允许使用移动数据
				typeNet = TYPE_DATA;
				applyChange(TYPE_DATA, uid, false);
			} else if (mNetType.equals(mContext.getString(R.string.net_bg))) {
				//允许使用后台数据
				typeNet = null;
				applyDataChange(uid, POLICY_NONE);
			} else if (mNetType.equals(mContext.getString(R.string.data_wifi))) {
				typeNet = TYPE_WLAN;
				applyChange(TYPE_WLAN, uid, false);
			}
	      	mAppInfosNoPolicy.add(mAppList.get(index));
		    mAppInfosByPolicy.remove(index);
			mAppList.remove(index);
			if (typeNet != null) {
				save(typeNet, mAppInfosByPolicy);
			}
			//更新界面
			updateItem();
		} else {
			if (mNetType.equals(mContext.getString(R.string.data_mobile))) {
				//禁止使用移动数据
				typeNet = TYPE_DATA;
				applyChange(TYPE_DATA, uid, true);
			} else if (mNetType.equals(mContext.getString(R.string.net_bg))) {
				//禁止使用后台数据
				//保存初始状态
				typeNet = null;
				applyDataChange(uid, POLICY_REJECT_METERED_BACKGROUND);
			} else if (mNetType.equals(mContext.getString(R.string.data_wifi))) {
				typeNet = TYPE_WLAN;
				applyChange(TYPE_WLAN, uid, true);
			}
			Log.v(TAG, "mAppList>>移动数据>>>" + mAppList.size() + "--->>index>>" + index);
			mAppInfosByPolicy.add(mAppList.get(index));
			mAppInfosNoPolicy.remove(index);
			mAppList.remove(index);
			if (typeNet != null) {
				save(typeNet, mAppInfosByPolicy);
			}
			//更新界面
			updateItem();
		}
	}
	
	/**
	 * 更新界面
	 */
	private void updateItem() {
		  Collections.sort(mAppInfosByPolicy);
		  Collections.sort(mAppInfosNoPolicy);
		  mArrayLists.clear();
		  mArrayLists.add(mAppInfosByPolicy);
		  mArrayLists.add(mAppInfosNoPolicy);
		  Log.e(TAG, "mAppList.size()>>>" + mAppList.size());
		  Message msg = mHandler.obtainMessage();
		  msg.what = CHANGE_STATE_TAG;
		  msg.obj = mArrayLists;
		  mHandler.sendMessage(msg);
	}
    
	private class AppItemTask extends AsyncTask<Void, Void, Void> {

		public AppItemTask() {
			super();
		}

		@Override
		protected Void doInBackground(Void... params) {
			for (int i = 0; i < mAppList.size(); i++) {
				AppItem appItem = mAppList.get(i);
				for (int j = 0; j < mAppInfosData.size(); j++) {
					AppItem appItemData = mAppInfosData.get(j);
					//appItemData包含前台数据和后台数据
					if (appItem.getAppUid() == appItemData.getAppUid()) {
						//卡1|卡2流量数据
						long orginData = appItem.getAppData();
						appItem.setAppData(orginData + appItemData.getAppData());
					}
				}
			}
			Collections.sort(mAppList);
			//更新允许上网数据集合
			for (int i = 0; i < mAppInfosNoPolicy.size(); i++) {
				AppItem appItemNoPolicy = mAppInfosNoPolicy.get(i);
				for (int j = 0; j < mAppInfosData.size(); j++) {
					AppItem appItemData = mAppInfosData.get(j);
					//包含前台数据和后台数据
					if (appItemNoPolicy.getAppUid() == appItemData.getAppUid()) {
						//卡1|卡2流量数据
						long orginData = appItemNoPolicy.getAppData();
						appItemNoPolicy.setAppData(orginData + appItemData.getAppData());
//						break;
					}
				}
			}
			Collections.sort(mAppInfosNoPolicy);
			//更新禁止上网数据集合
			for (int i = 0; i < mAppInfosByPolicy.size(); i++) {
				AppItem appItemPolicy = mAppInfosByPolicy.get(i);
				for (int j = 0; j < mAppInfosData.size(); j++) {
					AppItem appItemData = mAppInfosData.get(j);
					//包含前台数据和后台数据
					if (appItemPolicy.getAppUid() == appItemData.getAppUid()) {
						//卡1|卡2流量数据
						long orginData = appItemPolicy.getAppData();
						appItemPolicy.setAppData(orginData + appItemData.getAppData());
					}
				}
			}
			Collections.sort(mAppInfosByPolicy);
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
