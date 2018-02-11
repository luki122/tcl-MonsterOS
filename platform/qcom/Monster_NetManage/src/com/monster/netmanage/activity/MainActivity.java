package com.monster.netmanage.activity;

import static android.net.NetworkTemplate.buildTemplateMobileAll;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.monster.netmanage.DataCorrect;
import com.monster.netmanage.DataManagerApplication;
import com.monster.netmanage.R;
import com.monster.netmanage.adapter.MainFragementAdater;
import com.monster.netmanage.fragement.Sim01Fragement;
import com.monster.netmanage.net.SummaryForAllUidLoader;
import com.monster.netmanage.receiver.NetManagerReceiver;
import com.monster.netmanage.receiver.SimStateReceiver;
import com.monster.netmanage.receiver.SimStateReceiver.ISimStateChangeListener;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;
import com.monster.netmanage.utils.ToolsUtil;
import com.monster.netmanage.view.CircleView;
import com.mst.tms.NetInfoEntity;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.graphics.Color;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import mst.widget.toolbar.Toolbar;
import mst.widget.toolbar.Toolbar.OnMenuItemClickListener;

/**
 * 流量管理主界面
 * @author zhaolaichao
 *
 */
public class MainActivity extends FragmentActivity implements OnClickListener, OnPageChangeListener, OnMenuItemClickListener{
    private final static String TAG = "MainActivity";
    /**
     * 移动数据类型
     */
	private static final String TAB_MOBILE = "mobile";
	/**
	 * WIFI类型
	 */
	private static final String TAB_WIFI = "wifi";
	private static final int LOADER_COMMON = 1;
	private static final int LOADER_FREE = 2;
    private static final int LOADER_SUMMARY = 3;
	/**
	 * 当前选中的sim卡
	 */
	public static int PAGE_SELECTED_INDEX = 0;
	/**
	 * 第一张SIM
	 */
	private static final int FIRST_SIM_INDEX = 0;
	/**
	 * 第二张SIM
	 */
	private static final int SECOND_SIM_INDEX = 1;
	/**
	 * 更新校正时间
	 */
	private static final int CORRECT_TIME_TAG = 90000;
	private static final int CORRECT_FIRST_LIMITE_MAXTIME_TAG = 90001;
	private static final int CORRECT_SECOND_LIMITE_MAXTIME_TAG = 90002;
	/**
	 * 最大时长为15分钟
	 */
	private static final int CORRECT_LIMITE_MAXTIME = 15 * 60 * 1000;
	/**
	 * 今天所用流量统计
	 */
	private static final int STATS_DATA_TODAY = 10000;
	private NetworkTemplate mTemplate;
	private INetworkStatsSession mStatsSession;
	private INetworkStatsService mStatsService;
	private TelephonyManager mTelephonyManager;
	/**
	 * 圆周运动view
	 */
	public static CircleView mCircleView;
	/**
	 * 校正时间
	 */
	private TextView mTvCorrect;
	private ImageView mImvIndex1;
	private ImageView mImvIndex2;
	private ViewPager mViewPager;
	
	private Sim01Fragement mSim01Fragement;
	private Sim01Fragement mSim02Fragement;
	private MainFragementAdater mMainAdapter;
	private LinearLayout mLayImvIndex;
	private FrameLayout mLayCircle;
	/**
	 * 流量校正
	 */
	private Button mBtnCorrect;
	/**
	 * 发送校正短信
	 */
	public static  DataCorrect mDataCorrect;
	/**
	 * 状态栏高度
	 */
	private int mStatusBarHeight;
	/**
	 * 当前上网卡的卡槽索引
	 */
	private int mCurrentNetSimIndex = -1;
	/**
	 * 流量校正完成
	 */
    private long mCorrectOkTime;
    /**
	  * 当前网络数据类型
	  */
    private String mCurrentNetType = null;
    
    private Timer mTimer;
    private TimerTask mTimeTask;
	private ArrayList<Fragment> mFragements = new ArrayList<Fragment>();
	private NetInfoEntity mNetInfoEntity = new NetInfoEntity();
	/**
	 * 自动校正开关状态
	 */
	private boolean mCorrectState;
	private boolean mSimState;
	private boolean mManState;
	Handler mHandler = new Handler() {
	     public void handleMessage(android.os.Message msg) {
	        switch (msg.what) {
	        case CORRECT_FIRST_LIMITE_MAXTIME_TAG:
	        case CORRECT_SECOND_LIMITE_MAXTIME_TAG:
	        case DataCorrect.MSG_TRAFFICT_NOTIFY :
	    	    //流量校正成功
	        	boolean dateChange = PreferenceUtil.getBoolean(MainActivity.this, "", PreferenceUtil.DATE_CHANGE_KEY, false);
	        	Log.v(TAG, "mManState>>>>>>" + mManState);
	        	mBtnCorrect.setText(R.string.data_correct);
	        	setBtnEnable(mBtnCorrect, true);
	        	PreferenceUtil.putString(MainActivity.this, DataManagerApplication.mImsiArray[msg.arg1], PreferenceUtil.CORRECT_DATA_KEY, getString(R.string.data_correct));
	        	PreferenceUtil.putBoolean(MainActivity.this, DataManagerApplication.mImsiArray[msg.arg1], PreferenceUtil.MAN_INPUT_CORRECT_KEY, false);
				if (dateChange) {
					if (mManState) {
						cancelTimer();
						correctAfterUpdateUI (true, msg.arg1);
						if (DataManagerApplication.mImsiArray.length == 1) {
							Toast.makeText(MainActivity.this, getString(R.string.single_data_correct_ok), Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(MainActivity.this, String.format(getString(R.string.data_correct_ok), "" + (msg.arg1 + 1)), Toast.LENGTH_SHORT).show();
						}
					} else {
						correctAfterUpdateUI (false, msg.arg1);
					}
				}
				mHandler.removeMessages(msg.what);
				if (FIRST_SIM_INDEX == msg.arg1) {
					mHandler.removeMessages(CORRECT_FIRST_LIMITE_MAXTIME_TAG);
				} else if (SECOND_SIM_INDEX == msg.arg1) {
					mHandler.removeMessages(CORRECT_SECOND_LIMITE_MAXTIME_TAG);
				}
				break;
	        case DataCorrect.MSG_TRAFFICT_ERROR :
	        	//流量校正失败
	        	dateChange = PreferenceUtil.getBoolean(MainActivity.this, "", PreferenceUtil.DATE_CHANGE_KEY, false);
	        	PreferenceUtil.putBoolean(MainActivity.this, DataManagerApplication.mImsiArray[msg.arg1], PreferenceUtil.MAN_INPUT_CORRECT_KEY, false);
	        	mBtnCorrect.setText(R.string.data_correct);
	        	setBtnEnable(mBtnCorrect, true);
	        	PreferenceUtil.putString(MainActivity.this, DataManagerApplication.mImsiArray[msg.arg1], PreferenceUtil.CORRECT_DATA_KEY, getString(R.string.data_correct));
	        	if (dateChange) {
	        		if (mManState) {
	        			if (DataManagerApplication.mImsiArray.length == 1) {
							Toast.makeText(MainActivity.this, getString(R.string.single_data_correct_error), Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(MainActivity.this, String.format(getString(R.string.data_correct_error), "" + (msg.arg1 + 1)), Toast.LENGTH_SHORT).show();
						}
	        		}
	        	}
				mHandler.removeMessages(msg.what);
				if (FIRST_SIM_INDEX == msg.arg1) {
					mHandler.removeMessages(CORRECT_FIRST_LIMITE_MAXTIME_TAG);
				} else if (SECOND_SIM_INDEX == msg.arg1) {
					mHandler.removeMessages(CORRECT_SECOND_LIMITE_MAXTIME_TAG);
				}
	        	break;
	        case CORRECT_TIME_TAG:
	        	//更新自动校正时间提示
	        	long currectedTime = PreferenceUtil.getLong(MainActivity.this, DataManagerApplication.mImsiArray[PAGE_SELECTED_INDEX], PreferenceUtil.CORRECT_OK_TIME_KEY, 0);
	        	if (currectedTime > 0) {
	        		String standardDate = StringUtil.getStandardDate("" + currectedTime);
	        		mTvCorrect.setText(String.format(getString(R.string.data_correct_info), standardDate));
	          	} else {
	          		mTvCorrect.setText("");
	          	}
	          	break;
	        case STATS_DATA_TODAY:
	        	statsDataUpdate(PAGE_SELECTED_INDEX, (NetInfoEntity) msg.obj);
	        	break;
	        default:
	        	break;
	        }
	 	 }
   	};
	
	/**
	 * 更新校正后的时间
	 */
	private void startUpdateTime() {
		//记录更新提示时间
		mTimeTask = new TimerTask(){ 
			public void run() { 
			      Message msg = mHandler.obtainMessage(); 
			      msg.what = CORRECT_TIME_TAG;
			      mHandler.sendMessage(msg); 
			} 
		};
		mTimer = new Timer(true);
		mTimer.schedule(mTimeTask,1000, 1000*60); //延时1000ms后执行，1min执行一次
	}
	
	private void cancelTimer() {
		if (null != mTimeTask) {
			mTimeTask.cancel();
		}
		if (null != mTimer) {
			mTimer.cancel();
		}
	}
	
	private void limitedCorrectTime(int selectedIndex) {
		Message msg = mHandler.obtainMessage(); 
		switch (selectedIndex) {
		case FIRST_SIM_INDEX:
			msg.what = CORRECT_FIRST_LIMITE_MAXTIME_TAG;
			break;
		case SECOND_SIM_INDEX:
			msg.what = CORRECT_SECOND_LIMITE_MAXTIME_TAG;
			break;
		default:
			break;
		}
	     msg.arg1 = selectedIndex;
	     mHandler.sendMessageDelayed(msg, CORRECT_LIMITE_MAXTIME); 
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		intStats();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int flag = getWindow().getDecorView().getSystemUiVisibility();
		getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | flag);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        //获取status_bar_height资源的ID  
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");  
        if (resourceId > 0) {  
            //根据资源ID获取响应的尺寸值  
        	mStatusBarHeight = getResources().getDimensionPixelSize(resourceId);  
        }  
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mCurrentNetSimIndex = ToolsUtil.getCurrentNetSimSubInfo(this.getBaseContext());
		mDataCorrect = DataCorrect.getInstance();
		mDataCorrect.initCorrect(this, mHandler);
		DataManagerApplication.mImsiArray = ToolsUtil.getIMSI(this);
		mLayCircle = (FrameLayout) findViewById(R.id.lay_circle);
		mCircleView = new CircleView(this);
		mLayCircle.addView(mCircleView);
		//初始化界面
		initToolBar();
		initView();
		updateUIBySimChange();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ToolsUtil.registerHomeKeyReceiver(this);
		if (TextUtils.isEmpty(DataManagerApplication.mImsiArray[0])) {
			PAGE_SELECTED_INDEX = 0;
		}
		mCorrectState = PreferenceUtil.getBoolean(this, DataManagerApplication.mImsiArray[PAGE_SELECTED_INDEX], PreferenceUtil.AUTO_CORRECT_STATE_KEY, true);
		mSimState = PreferenceUtil.getBoolean(this, DataManagerApplication.mImsiArray[PAGE_SELECTED_INDEX], PreferenceUtil.SIM_BASEINFO_KEY, false);
		boolean dateChange = PreferenceUtil.getBoolean(this, "", PreferenceUtil.DATE_CHANGE_KEY, false);
        //当天首次进入流量管理应用时进行流量校正
		if (mCorrectState && mSimState && !dateChange) {
			PreferenceUtil.putBoolean(this, "", PreferenceUtil.DATE_CHANGE_KEY, true);
			mDataCorrect.startCorrect(this, true, PAGE_SELECTED_INDEX);
		}
		
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		statsData();
	}
	@Override
	protected void onDestroy() {
		//移除流量校正监听
		cancelTimer();
		destoryLoder();
		if (null != mCircleView) {
			mCircleView.setMove(false);
			mLayCircle.removeAllViews();
			mCircleView = null;
		}
		mHandler.removeMessages(STATS_DATA_TODAY);
		mHandler.removeMessages(CORRECT_FIRST_LIMITE_MAXTIME_TAG);
		mHandler.removeMessages(CORRECT_SECOND_LIMITE_MAXTIME_TAG);
		super.onDestroy();
		ToolsUtil.mAllApps = null;
		mFragements.clear();
		mSim01Fragement = null;
		mSim02Fragement = null;
		mFragements = null;
		for (int i = 0; i < DataManagerApplication.mImsiArray.length; i++) {
        	PreferenceUtil.putString(MainActivity.this, DataManagerApplication.mImsiArray[i], PreferenceUtil.CORRECT_DATA_KEY, getString(R.string.data_correct));
		}
	}
	
	@Override
	public void onBackPressed() {
		//更新桌面
		ToolsUtil.updateIconReceiver();
		super.onBackPressed();
	}
	
	@Override
	protected void onPause() {
		ToolsUtil.unregisterHomeKeyReceiver(this);
		super.onPause();
	}
	
	private void intStats() {
		if (null == mStatsService) {
	         mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
		     try {
		          mStatsSession = mStatsService.openSession();
		     } catch (RemoteException e) {
		          throw new RuntimeException(e);
		     }
		}
	}
	
	public CircleView getCircleView() {
		return mCircleView;
	}
	/**
	 * 统计已用的手机流量
	 * @param simIndex
	 */
	public void statsAppsData(int simIndex, boolean isInit) {
		 // Match mobile traffic for this subscriber, but normalize it to
	    // catch any other merged subscribers.
		if (TAB_MOBILE.equals(mCurrentNetType)) {
			mTemplate = buildTemplateMobileAll(ToolsUtil.getActiveSubscriberId(this, ToolsUtil.getIdInDbBySimId(this, simIndex)));
			mTemplate = NetworkTemplate.normalize(mTemplate, mTelephonyManager.getMergedSubscriberIds());
		} 
        long start = StringUtil.getStartTime();
        long end  = StringUtil.getEndTime();
        if (isInit) {
			  getLoaderManager().initLoader(LOADER_SUMMARY,
					  SummaryForAllUidLoader.buildArgs(mTemplate, start, end), mSummaryCallbacks);
		  } else {
			  getLoaderManager().restartLoader(LOADER_SUMMARY,
					  SummaryForAllUidLoader.buildArgs(mTemplate, start, end), mSummaryCallbacks);
		  }
	}

	public void statsAppsCommon(int simIndex, boolean isInit) {
		 // Match mobile traffic for this subscriber, but normalize it to
	    // catch any other merged subscribers.
		if (TAB_MOBILE.equals(mCurrentNetType)) {
			mTemplate = buildTemplateMobileAll(ToolsUtil.getActiveSubscriberId(this, ToolsUtil.getIdInDbBySimId(this, simIndex)));
			mTemplate = NetworkTemplate.normalize(mTemplate, mTelephonyManager.getMergedSubscriberIds());
		} 
	   int closeDay = PreferenceUtil.getInt(this,  DataManagerApplication.mImsiArray[PAGE_SELECTED_INDEX], PreferenceUtil.CLOSEDAY_KEY, 1);
       long start = StringUtil.getDayByMonth(closeDay);
       long end  = StringUtil.getDayByNextMonth(closeDay);
       if (isInit) {
			  getLoaderManager().initLoader(LOADER_COMMON,
					  SummaryForAllUidLoader.buildArgs(mTemplate, start, end), mSummaryCallbacks);
	   } else {
			  getLoaderManager().restartLoader(LOADER_COMMON,
					  SummaryForAllUidLoader.buildArgs(mTemplate, start, end), mSummaryCallbacks);
	   }
	}
	
	public void statsAppsFree(int simIndex, boolean isInit) {
		 // Match mobile traffic for this subscriber, but normalize it to
	    // catch any other merged subscribers.
		if (TAB_MOBILE.equals(mCurrentNetType)) {
			mTemplate = buildTemplateMobileAll(ToolsUtil.getActiveSubscriberId(this, ToolsUtil.getIdInDbBySimId(this, simIndex)));
			mTemplate = NetworkTemplate.normalize(mTemplate, mTelephonyManager.getMergedSubscriberIds());
		} 
		long startTime = 0;
		long endTime = 0;
		int firstCorrect = PreferenceUtil.getInt(this, DataManagerApplication.mImsiArray[PAGE_SELECTED_INDEX], PreferenceUtil.DATAPLAN_CORRECT_FIRST_KEY, 0);
		int freeTotal = PreferenceUtil.getInt(this, DataManagerApplication.mImsiArray[PAGE_SELECTED_INDEX], PreferenceUtil.FREE_DATA_TOTAL_KEY, 0);
		if (firstCorrect == 0 && freeTotal > 0) {
			int closeDay = PreferenceUtil.getInt(this,  DataManagerApplication.mImsiArray[PAGE_SELECTED_INDEX], PreferenceUtil.CLOSEDAY_KEY, 1);
			//开始时间
			String freeStartTime = PreferenceUtil.getString(this, DataManagerApplication.mImsiArray[PAGE_SELECTED_INDEX], PreferenceUtil.FREE_DATA_START_TIME_KEY, "00:00");
			//结束时间
			String freeEndTime = PreferenceUtil.getString(this, DataManagerApplication.mImsiArray[PAGE_SELECTED_INDEX], PreferenceUtil.FREE_DATA_END_TIME_KEY, "5:00");
			startTime = StringUtil.getDayByMonth(closeDay, Integer.parseInt(freeStartTime.split(":")[0]), Integer.parseInt(freeStartTime.split(":")[1]));
			endTime = StringUtil.getDayByNextMonth(closeDay, Integer.parseInt(freeEndTime.split(":")[0]), Integer.parseInt(freeEndTime.split(":")[1]));
		}
       if (isInit) {
			  getLoaderManager().initLoader(LOADER_FREE,
					  SummaryForAllUidLoader.buildArgs(mTemplate, startTime, endTime), mSummaryCallbacks);
		  } else {
			  getLoaderManager().restartLoader(LOADER_FREE,
					  SummaryForAllUidLoader.buildArgs(mTemplate, startTime, endTime), mSummaryCallbacks);
		  }
	}
	
	/**
	 * 销毁加载器
	 */
	private void destoryLoder() {
		getLoaderManager().destroyLoader(LOADER_SUMMARY);
		getLoaderManager().destroyLoader(LOADER_COMMON);
		getLoaderManager().destroyLoader(LOADER_FREE);
	}
	
	 /**
     * sim卡状态发生改变时更新UI
     */
    private void updateUIBySimChange () {
    	SimStateReceiver.setSimStateChangeListener(new ISimStateChangeListener() {
			
			@Override
			public void onSimStateChange() {
				Log.e(TAG, "sim卡状态发生改变时更新UI");
			}
		});
    }
    
    private void initToolBar() {
    	View statusDividerView = findViewById(R.id.view_status_divider);
		android.view.ViewGroup.LayoutParams lp = statusDividerView.getLayoutParams();
		lp.height = mStatusBarHeight;
		Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
		toolbar.inflateMenu(R.menu.toolbar_action_button);
		toolbar.setOnMenuItemClickListener(this);
		toolbar.setTitle(R.string.app_title);
		toolbar.setBackgroundColor(Color.TRANSPARENT);
    }
    
	private void initView() {
		mTvCorrect = (TextView) findViewById(R.id.tv_correct_info);
		long currectedTime = PreferenceUtil.getLong(this, DataManagerApplication.mImsiArray[0], PreferenceUtil.CORRECT_OK_TIME_KEY, 0);
		if (currectedTime > 0) {
			String standardDate = StringUtil.getStandardDate("" + currectedTime);
			mTvCorrect.setText(String.format(getString(R.string.data_correct_info), standardDate));
		}
		mLayImvIndex = (LinearLayout) findViewById(R.id.lay_imv_index);
		mImvIndex1 = (ImageView) findViewById(R.id.imv_index_01);
		mImvIndex2 = (ImageView) findViewById(R.id.imv_index_02);
		mViewPager = (ViewPager) findViewById(R.id.view_pager_main);
		mBtnCorrect = (Button) findViewById(R.id.btn_correct);
		mBtnCorrect.setOnClickListener(this);
		Bundle bundle = new Bundle();
		bundle.putInt("CurrentSimIndex", FIRST_SIM_INDEX);
		mSim01Fragement = new Sim01Fragement();
		mFragements.add(mSim01Fragement);
		mSim01Fragement.setArguments(bundle);
		if (DataManagerApplication.mImsiArray.length > 1 ){ 
			if (!TextUtils.isEmpty(DataManagerApplication.mImsiArray [0]) && !TextUtils.isEmpty(DataManagerApplication.mImsiArray [1])) {
			    //有两张卡,当插入两张SIM卡才显示此指示器
			    mSim02Fragement = new Sim01Fragement();
			    bundle = new Bundle();
			    bundle.putInt("CurrentSimIndex", SECOND_SIM_INDEX);
			    mSim02Fragement.setArguments(bundle);
			    mFragements.add(mSim02Fragement);
			    mLayImvIndex.setVisibility(View.VISIBLE);
			}
		} else {
			//只有一张卡时刚切换成上网卡
			ToolsUtil.changeNetSim(this, mCurrentNetSimIndex);
		}
		mCurrentNetType = TAB_MOBILE;
        if (TextUtils.isEmpty(DataManagerApplication.mImsiArray [0])) {
        	//没有sim卡
        	setBtnEnable(mBtnCorrect, false);
        	mCurrentNetType = TAB_WIFI;
        }
		//获得FragmentManager
		FragmentManager fm = getSupportFragmentManager();
		mMainAdapter = new MainFragementAdater(fm, mFragements);
		mViewPager.setAdapter(mMainAdapter);
		mViewPager.setOnPageChangeListener(this);
		boolean cirViewColorState = PreferenceUtil.getBoolean(this, DataManagerApplication.mImsiArray [0], PreferenceUtil.NOTIFY_WARN_MONTH_KEY, false);
		if (cirViewColorState) {
			mCircleView.setColor(CircleView.CIRVIEW_WARN_COLOR);
		}
		//更新校正后的时间
		startUpdateTime();
	}

	/**
	 * 设置btn的状态
	 * @param btn
	 * @param enable
	 */
	private void setBtnEnable(Button btn, boolean enable) {
		if (enable) {
			if (btn.getText().toString().equals(getString(R.string.correcting))) {
				btn.setTextColor(getColor(R.color.color_time_correct_text));
				btn.setEnabled(false);
			} else {
				btn.setTextColor(getColor(R.color.color_btn_correct_text));
				btn.setEnabled(true);
			}
        } else {
        	btn.setTextColor(getColor(R.color.color_time_correct_text));
            btn.setEnabled(false);
        }
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_correct:
			//流量校正   要区别sim卡1、sim卡2
			//根据当前选中的位置来区别要使用哪个sim卡进行流量校正
			//sim基本信息设置完整检查 
			boolean simState = PreferenceUtil.getBoolean(this, DataManagerApplication.mImsiArray[PAGE_SELECTED_INDEX], PreferenceUtil.SIM_BASEINFO_KEY, false);
			if (!simState) {
				//引导用户设置套餐界面
	        	setDataPlanIndex();
				break;
			}
			mManState = true;
			setBtnEnable(mBtnCorrect, false);
			mBtnCorrect.setText(R.string.correcting);
			mDataCorrect.startCorrect(this, true, PAGE_SELECTED_INDEX);
			PreferenceUtil.putString(MainActivity.this, DataManagerApplication.mImsiArray[PAGE_SELECTED_INDEX], PreferenceUtil.CORRECT_DATA_KEY, getString(R.string.correcting));
			limitedCorrectTime(PAGE_SELECTED_INDEX);
			break;
		default:
			break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int selected) {
		//当前选中项
		PAGE_SELECTED_INDEX = selected;
		if (selected == 0) {
			mImvIndex1.setImageResource(R.drawable.circle_sim_index_select_bg);
			mImvIndex2.setImageResource(R.drawable.circle_sim_index_unselect_bg);
		} else {
			mImvIndex1.setImageResource(R.drawable.circle_sim_index_unselect_bg);
			mImvIndex2.setImageResource(R.drawable.circle_sim_index_select_bg);
		}
		boolean cirViewColorState = PreferenceUtil.getBoolean(this, DataManagerApplication.mImsiArray [PAGE_SELECTED_INDEX], PreferenceUtil.NOTIFY_WARN_MONTH_KEY, false);
		if (cirViewColorState) {
			mCircleView.setColor(CircleView.CIRVIEW_WARN_COLOR);
		} else {
			mCircleView.setColor(CircleView.CIRVIEW_COLOR);
		}
		mNetInfoEntity = new NetInfoEntity();
		correctAfterUpdateUI (false, selected);
		//更新统计流量数据
		statsData();
		Message msg = mHandler.obtainMessage();
		msg.what = CORRECT_TIME_TAG;
	    mHandler.sendMessage(msg); 
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem menuItem) {
		//切换设置界面
		int itemId = menuItem.getItemId();
		Log.e(TAG, "onMenuItemClick>>" + menuItem.getTitle());
		 if (itemId == R.id.menu_data_range) {
			 String simTitle = getString(R.string.net_control);
			 Intent intent = new Intent(MainActivity.this, DataRangeActivity.class);
			 intent.putExtra("SIM_TITLE", simTitle);
			 intent.putExtra("SIM_COUNT", true);
			 startActivity(intent);
         } else if (itemId == R.id.menu_sim_set) {
        	//切换设置界面
        	 setDataPlanIndex();
         }
		return false;
	}
	
	/**
	 * 未设置套餐时点击“流量校正”后引导用户设置套餐
	 */
	private void setDataPlanIndex() {
		Intent intent = null;
		//切换设置界面
     	if (TextUtils.isEmpty(DataManagerApplication.mImsiArray [0]) && TextUtils.isEmpty(DataManagerApplication.mImsiArray [1])) {
     		//没有卡时
     		Toast.makeText(MainActivity.this, R.string.insert_sim, Toast.LENGTH_SHORT).show();
     		return;
     	}
     	if (DataManagerApplication.mImsiArray.length > 1) {
     		if (!TextUtils.isEmpty(DataManagerApplication.mImsiArray [0]) && !TextUtils.isEmpty(DataManagerApplication.mImsiArray [1])) {
     			intent = new Intent(MainActivity.this, DataSetActivity.class);
     			startActivity(intent);
     		}
     	} else {
     		//当前只有一张sim卡
     		String simTitle = getString(R.string.sim_set);
     		//获得当前选择的sim的imsi号
     		intent = new Intent(MainActivity.this, SimDataSetActivity.class);
     		intent.putExtra("SIM_TITLE", simTitle);
     		intent.putExtra("CURRENT_IMSI", DataManagerApplication.mImsiArray [0]);
     		startActivity(intent);
     	}
	}
	
	/**
	 * 统计流量
	 */
	private void statsData() {
		statsAppsData(PAGE_SELECTED_INDEX, false);
		statsAppsCommon(PAGE_SELECTED_INDEX, false);
		statsAppsFree(PAGE_SELECTED_INDEX, false);
	}
	/**
	 * 校正流量后更新UI界面
	 * @param state   当前流量校正状态 true:校正已完成
	 * @param simIndex   SIM卡索引
	 */
	private void correctAfterUpdateUI (boolean state, int simIndex) {
		if (simIndex == FIRST_SIM_INDEX) {
			mSim01Fragement.updateUISim1(simIndex);
		} else if (simIndex == SECOND_SIM_INDEX){
			mSim02Fragement.updateUISim1(simIndex);
		}
		if (state) {
			mCorrectOkTime =  System.currentTimeMillis();
			PreferenceUtil.putLong(this, DataManagerApplication.mImsiArray[simIndex], PreferenceUtil.CORRECT_OK_TIME_KEY, mCorrectOkTime);
			String standardDate = StringUtil.getStandardDate("" + mCorrectOkTime);
			mTvCorrect.setText(String.format(getString(R.string.data_correct_info), standardDate));
			String correct = PreferenceUtil.getString(MainActivity.this, DataManagerApplication.mImsiArray[simIndex], PreferenceUtil.CORRECT_DATA_KEY, getString(R.string.data_correct));
			mBtnCorrect.setText(correct);
			setBtnEnable(mBtnCorrect, true);
			//更新校正后的时间
			startUpdateTime();
		} else {
			String correct = PreferenceUtil.getString(MainActivity.this, DataManagerApplication.mImsiArray[simIndex], PreferenceUtil.CORRECT_DATA_KEY, getString(R.string.data_correct));
			mBtnCorrect.setText(correct);
			setBtnEnable(mBtnCorrect, true);
		}
	}
	
	/**
	 * 流量统计界面更新
	 * @param usedTotalForDay 今天已用
	 */
    private void statsDataUpdate(int simIndex, NetInfoEntity netInfoEntity) { 
    	if (simIndex == FIRST_SIM_INDEX) {
    		mSim01Fragement.updateUIByDataStats(simIndex, netInfoEntity);
		} else if (simIndex == SECOND_SIM_INDEX){
			mSim02Fragement.updateUIByDataStats(simIndex, netInfoEntity);
		}
    }
    
	/**
	 * 统计在指定时间段内用的流量
	 */
	 private final LoaderCallbacks<NetworkStats> mSummaryCallbacks = new LoaderCallbacks<NetworkStats>() {
		 
		 @Override
	        public Loader<NetworkStats> onCreateLoader(int id, Bundle args) {
	             return new SummaryForAllUidLoader(MainActivity.this, mStatsSession, args);
	        }

	        @Override
	        public void onLoadFinished(Loader<NetworkStats> loader, NetworkStats data) {
	        	  NetworkStats.Entry entry = null;
	        	  NetworkStats stats =  data;
	              final int size = stats != null ? stats.size() : 0;
	              long usedData = 0;
	              for (int i = 0; i < size; i++) {
	                  entry = stats.getValues(i, entry);
	                 //统计前台和后台数据
	                  usedData = usedData + (entry.rxBytes + entry.txBytes);
	                  Log.v(TAG, "entry>>>>>>>" + entry);
	              }
	              int loaderId = loader.getId();
	              if (loaderId == LOADER_COMMON) {
	            	  mNetInfoEntity.setmUsedForMonth(usedData);
	              } else if (loaderId == LOADER_FREE) {
	            	  mNetInfoEntity.setmFreeUsedForMonth(usedData);
	              } else if (loaderId == LOADER_SUMMARY) {
	            	  mNetInfoEntity.setmUsedForDay(usedData);
	              }
	              String totalData = Formatter.formatFileSize(MainActivity.this, usedData);
	              Log.v(TAG,"统计使用数据>>format>>>" + totalData + ">>loaderId>>>>" + loader.getId() + ">>mNetInfoEntity>>" + mNetInfoEntity);
	              Message msg = mHandler.obtainMessage();
	              msg.what = STATS_DATA_TODAY;
	              msg.obj = mNetInfoEntity;
	              mHandler.sendMessage(msg);
	        }

	        @Override
	        public void onLoaderReset(Loader<NetworkStats> loader) {
	        	
	        }
	    };

}