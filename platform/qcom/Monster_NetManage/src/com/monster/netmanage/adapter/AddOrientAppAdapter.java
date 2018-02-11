package com.monster.netmanage.adapter;

import java.util.ArrayList;

import com.monster.netmanage.R;
import com.monster.netmanage.entity.AppItem;
import com.monster.netmanage.utils.PreferenceUtil;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import mst.widget.CycleImageView;
import mst.widget.recycleview.RecyclerView;

/**
 * 添加定向应用
 * 
 * @author zhaolaichao
 *
 */
public class AddOrientAppAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
	
	/**
	 * 要添加的应用集合
	 */
    private ArrayList<PackageInfo> mAppList = new ArrayList<PackageInfo>();
    private ArrayList<AppItem> mAddAppList = new ArrayList<AppItem>();
    private Context mContext;
    private PackageManager mPManager;
    private String mCurrectImsi;
    
	public AddOrientAppAdapter(Context context, String imsi) {
		super();
		this.mContext = context;
		mCurrectImsi = imsi;
		mPManager = mContext.getPackageManager();
	}
	
	public void setAppList(ArrayList<PackageInfo> mppList) {
		mAppList.clear();
		this.mAppList.addAll(mppList);
	}

	public ArrayList<AppItem> getAddAppList() {
		return mAddAppList;
	}

	/**
	 * 清除缓存
	 */
	public void clean() {
		if (null != mAddAppList) {
			mAddAppList.clear();
		}
	}
	@Override
	public int getItemCount() {
		return mAppList.size();
	}

	@Override
	public void onBindViewHolder(mst.widget.recycleview.RecyclerView.ViewHolder viewHolder, int position) {
		 AppViewHolder appHolder = (AppViewHolder) viewHolder;
		 appHolder.initData(position);
	}

	@Override
	public mst.widget.recycleview.RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
		 View view = LayoutInflater.from(mContext).inflate(R.layout.orient_app_item, parent, false);
		 AppViewHolder appHolder = new AppViewHolder(view);
		return appHolder;
	}
	
	class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		RelativeLayout layItem;
    	CycleImageView civLogo;
    	TextView tvAppName;
    	Button btnAdd;
    	
		public AppViewHolder(View itemView) {
			super(itemView);
			layItem = (RelativeLayout) itemView.findViewById(R.id.lay_item);
			civLogo = (mst.widget.CycleImageView) itemView.findViewById(R.id.imv_logo);
			tvAppName = (TextView) itemView.findViewById(R.id.tv_app);
			btnAdd = (Button) itemView.findViewById(R.id.btn_remove);
			civLogo.setVisibility(View.VISIBLE);
			btnAdd.setVisibility(View.VISIBLE);
			btnAdd.setText(mContext.getString(R.string.add));
			layItem.setOnClickListener(this);
			btnAdd.setOnClickListener(this);
		}
    	
		/**
		 * 初始化数据
		 * @param context
		 * @param addList
		 * @param position
		 */
		public void initData(int position) {
			//获取要添加的应用
			PackageInfo packageInfo = mAppList.get(position);
			String appName  = (String) packageInfo.applicationInfo.loadLabel(mPManager);
			layItem.setTag(position);
			//获得应用的logo
			Drawable logo = packageInfo.applicationInfo.loadIcon(mPManager);
			civLogo.setImageDrawable(logo);
			tvAppName.setText(appName);
			btnAdd.setTag(position);
		}

		@Override
		public void onClick(View v) {
			int index = 0;
			switch(v.getId()) {
			case R.id.btn_remove:
				Button btnRemove = (Button) v;
				index = (Integer) btnRemove.getTag();
				addApp(index, btnRemove);
				break;
			case R.id.lay_item:
//				RelativeLayout layItem = (RelativeLayout) v;
//				index = (Integer) layItem.getTag();
				break;
			}
		}
    }
	
	/**
	 * 添加应用
	 * @param position
	 */
	private  void addApp(int position, Button btnRemove) {
		PackageInfo packageInfo = mAppList.get(position);
		AppItem appItem = new AppItem();
		appItem.setAppUid(packageInfo.applicationInfo.uid);
		appItem.setPackageInfo(packageInfo);
		mAddAppList.add(appItem);
		//取出已添加过的UID
		StringBuffer addBf = new StringBuffer();
		String addedAppUids = PreferenceUtil.getString(mContext, mCurrectImsi, PreferenceUtil.ORIENT_APP_ADDED_KEY, "");
		addBf.append(addedAppUids);
		//添加新的UID
		addBf.append(packageInfo.applicationInfo.uid).append(",");
		PreferenceUtil.putString(mContext, mCurrectImsi, PreferenceUtil.ORIENT_APP_ADDED_KEY, addBf.toString());
		btnRemove.setText(mContext.getString(R.string.added));
		btnRemove.setBackground(mContext.getDrawable(R.drawable.transparent_ripple));
		btnRemove.setEnabled(false);
		btnRemove.getLayoutParams().width = dip2px(mContext, 60); 
		btnRemove.setLayoutParams(btnRemove.getLayoutParams());
		btnRemove.setTextColor(mContext.getColor(R.color.color_time_correct_text));
		Log.v("addApp", "addBf>>" + addBf.toString());
	}


    /** 
     * 将dip或dp值转换为px值，保证尺寸大小不变 
     *  
     * @param dipValue 
     * @param scale 
     *            （DisplayMetrics类中属性density） 
     * @return 
     */  
    public static int dip2px(Context context, float dipValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dipValue * scale + 0.5f);  
    }
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  
}
