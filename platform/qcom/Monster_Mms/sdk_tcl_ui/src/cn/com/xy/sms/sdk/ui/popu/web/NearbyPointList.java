package cn.com.xy.sms.sdk.ui.popu.web;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.action.AbsSdkDoAction;
import cn.com.xy.sms.sdk.action.NearbyPoint;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;

public class NearbyPointList extends Activity {

    //public static final String TAG = "XIAOYUAN";

    private static final String LTAG = NearbyPointList.class.getSimpleName();

    private TextView mTitleNameTextView = null;
    private ListView mListView;
    private LinearLayout mloadMoreLinearLayout;
    private ImageView mLoadMoreImageView;
    private TextView mLoadMoreTextView;
    public static final int QUERY_GPS_CLOSED_ERROR = 0x1007;
    // private ImageView mHeadBackView = null;
    // private TextView mTitleNameView = null;
    // private ImageView mMenuView = null;
    private ArrayList<HashMap<String, Object>> mListItems = new ArrayList<HashMap<String, Object>>();
    private NearbyPointListViewAdapter mNearbyPointListViewAdapter;
    private ImageView mHeadBackLinearLayout = null;
    private LinearLayout mNearbyPointLoadingLinearLayout;// 附近网点加载中
    private LinearLayout mNearbyPointNotFindLinearLayout;// 没有找到相关结果
    private LinearLayout mNearbyPointNetworkLoseLinearLayout;// 网络出错
    private LinearLayout mCloseGpsErrorLinearLayout;// GPS定位出错
    private LinearLayout mNearbyPointListLinearLayout;// 附近网点
    private NearbyPoint mNearbyPoint = null;
    private int mTotal = 0; // 检索结果数据总数量
    private int mPageNum = 0; // 分页页码，默认为0,0代表第一页，1代表第二页，以此类推。
    private GetLocationThread mGetLocationThread = null;
    // private DoGetLocationThread mDoGetLocationThread = null;
    private String mAddress = null;
    private double mLocationLongitude = -1;
    private double mLocationLatitude = -1;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.duoqu_nearby_point_list);

        mAddress = getIntent().getStringExtra("address");

        mNearbyPoint = new NearbyPoint(NearbyPointList.this, mHandler);

        mNearbyPointLoadingLinearLayout = (LinearLayout) findViewById(R.id.duoqu_ll_nearby_point_loading);// 附近网点加载中
        mNearbyPointNotFindLinearLayout = (LinearLayout) findViewById(R.id.duoqu_ll_nearby_point_not_find);// 没有找到相关结果
        mNearbyPointNotFindLinearLayout
                .setOnClickListener(new RetryOnClickListener());
        mNearbyPointNetworkLoseLinearLayout = (LinearLayout) findViewById(R.id.duoqu_ll_nearby_point_network_lose);// 网络出错
        mNearbyPointNetworkLoseLinearLayout
                .setOnClickListener(new RetryOnClickListener());
        mNearbyPointListLinearLayout = (LinearLayout) findViewById(R.id.duoqu_ll_nearby_point_list);// 附近网点
        mCloseGpsErrorLinearLayout = (LinearLayout) findViewById(R.id.duoqu_gps_closed_error);// GPS错误
        mListView = (ListView) findViewById(R.id.duoqu_lv_nearby_point);// 附近网点列表
        mTitleNameTextView = (TextView) findViewById(R.id.duoqu_title_name);
        // titleNameView.setText(mQuery);
        ViewUtil.setTextViewValue(mTitleNameTextView, mAddress);
        mNearbyPointListViewAdapter = new NearbyPointListViewAdapter(
                NearbyPointList.this, mListItems); // 创建适配器
        mHeadBackLinearLayout = (ImageView) findViewById(R.id.duoqu_header_back);
        mHeadBackLinearLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        // mHeadBackView = (ImageView)
        // actionBarLayout.findViewById(R.id.duoqu_header_back);
        //
        // mTitleNameView = (TextView)
        // actionBarLayout.findViewById(R.id.duoqu_title_name);
        // mTitleNameView.setText(mAddress);
        //
        // mMenuView = (ImageView)
        // actionBarLayout.findViewById(R.id.duoqu_header_menu);

        View loadMoreView = getLayoutInflater().inflate(
                R.layout.duoqu_nearby_point_list_bottom, null);

        mLoadMoreImageView = (ImageView) loadMoreView
                .findViewById(R.id.duoqu_iv_load_more);

        mLoadMoreTextView = (TextView) loadMoreView
                .findViewById(R.id.duoqu_tv_load_more);

        // 添加底部点击加载更多数据
        mloadMoreLinearLayout = (LinearLayout) loadMoreView
                .findViewById(R.id.duoqu_ll_load_more);
        mloadMoreLinearLayout.setOnClickListener(new LoadMoreOnClickListener());
        mListView.addFooterView(loadMoreView);

        mListView.setDivider(null);// ListView去底线
        mListView.setAdapter(mNearbyPointListViewAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                    int position, long id) {
                //Log.d(TAG, "onCreate(), mListView onItemClick");
                // 跳转到地图
                /* UIX-145 lianghailun 20160504 start */
                try {
                    if (mListItems.size() > position) {
                        //Log.d(TAG, "onCreate(), mListView onItemClick >>>openMap");
                        DuoquUtils.getSdkDoAction().openMap(
                                getApplicationContext(),
                                (String) mListItems.get(position).get("name"),
                                (String) mListItems.get(position)
                                        .get("address"),
                                (Double) mListItems.get(position).get(
                                        "longitude"),
                                (Double) mListItems.get(position).get(
                                        "latitude"));
                    }
                } catch (Throwable e) {
                    // TODO: handle exception
                    SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointList  error:", e);
                }
                /* UIX-145 lianghailun 20160504 end */
            }
        });
        /*
         * // 当版本大于4.4时启用沉浸式状态栏 if (Build.VERSION.SDK_INT >=
         * Build.VERSION_CODES.KITKAT) { setImmersion(); }
         */

        // initListener();
        getLocation();
    }

    /**
     * 沉浸式设置
     */
    /*
     * public void setImmersion() { Window window = getWindow();
     * window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
     * WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
     * window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
     * WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION); }
     */

    // /**
    // * OnclickListener method
    // */
    // void initListener() {
    // mMenuView.setOnClickListener(new OnClickListener() {
    // @Override
    // public void onClick(View v) {
    // // TODO Auto-generated method stub
    // MenuWindow mLifeHallWindow = new MenuWindow(NearbyPointList.this, new
    // OnClickListener() {
    // public void onClick(View v) {
    // }
    // });
    // Rect frame = new Rect();
    // getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
    // int yOffset = frame.top +
    // NearbyPointList.this.findViewById(R.id.duoqu_header).getHeight();
    // int xOffset = ViewUtil.dp2px(NearbyPointList.this, 5);
    // //
    // mLifeHallWindow.showAsDropDown(SdkWebActivity.this.findViewById(R.id.duoqu_header));
    // mLifeHallWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.duoqu_popupwindow_menu));
    // if ("1".equals(ParseManager.queryDefService(NearbyPointList.this))) {
    // mLifeHallWindow.getmTextView_Reset().setVisibility(View.VISIBLE);
    // mLifeHallWindow.getmSplitline().setVisibility(View.VISIBLE);
    // } else {
    // mLifeHallWindow.getmTextView_Reset().setVisibility(View.GONE);
    // mLifeHallWindow.getmSplitline().setVisibility(View.GONE);
    // }
    // mLifeHallWindow.showAtLocation(NearbyPointList.this.findViewById(R.id.duoqu_ll_nearby_point_list),
    // Gravity.TOP | Gravity.RIGHT, xOffset, yOffset);
    // }
    // });
    // mHeadBackView.setOnClickListener(new OnClickListener() {
    //
    // @Override
    // public void onClick(View arg0) {
    // finish();
    // }
    // });
    // }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /**
     * 重新加载
     */
    private class RetryOnClickListener implements OnClickListener {
        @Override
        public void onClick(View arg0) {
            getLocation();
        }
    }

    /**
     * 加载更多点击事件
     */
    private class LoadMoreOnClickListener implements OnClickListener {
        @Override
        public void onClick(View arg0) {
            // 隐藏图片
            mLoadMoreImageView.setVisibility(View.GONE);
            // 修改提示为“加载中...”
            ViewUtil.setTextViewValue(mLoadMoreTextView, getApplication()
                    .getString(R.string.duoqu_tip_loading));
            // 禁用LinearLayout防止多次点击加载更多数据
            mloadMoreLinearLayout.setEnabled(false);

            // Log.d(LTAG, "加载更多数据");

            mPageNum++;

            mNearbyPoint.sendMapQueryUrl(mAddress, mLocationLatitude,
                    mLocationLongitude, mPageNum);
        }
    }

    /**
     * 获取位置经纬度
     */
    private void getLocation() {
        // 显示附近网点加载中提示
        setViewVisibility(View.VISIBLE, View.GONE, View.GONE, View.GONE,
                View.GONE);

        if (mGetLocationThread != null) {
            mGetLocationThread.isInterrupted();
            mGetLocationThread = null;
        }
        mGetLocationThread = new GetLocationThread();
        mGetLocationThread.start();
    }

    /**
     * 执行发送地图检索请求
     */
    private void doSendMapQueryUrl(double longitude, double latitude) {
        // 经纬度为空
        if (longitude <= 0 || latitude <= 0) {
            // 显示没有找到相关结果提示
            setViewVisibility(View.GONE, View.VISIBLE, View.GONE, View.GONE,
                    View.GONE);
            return;
        } else {// 经纬度不为空
            mNearbyPoint.sendMapQueryUrl(mAddress, latitude, longitude, 0);
        }
    }

    /**
     * 在单独线程上进行延时处理并通知Handler执行定位位置经纬度
     */
    private class GetLocationThread extends Thread {

        public void run() {
            try {
                // 延时200毫秒
                Thread.sleep(200);

                mHandler.obtainMessage(NearbyPoint.DO_GET_LOCATION)
                        .sendToTarget();
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointList  error:", e);
            }
        }
    }

    /**
     * 在单独线程上进行定位位置经纬度
     */
    private class DoGetLocationThread extends Thread {

        public void run() {
            try {
                //Log.d(TAG, "DoGetLocationThread run(), >>>getLocation");
                DuoquUtils.getSdkDoAction().getLocation(NearbyPointList.this,
                        mHandler);
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointList  error:", e);
            }
        }
    }

    /**
     * 执行获取位置经纬度
     */
    private void doGetLocation(double locationLongitude, double locationLatitude) {

        // 扩展参数的经纬度不为空
        if (locationLongitude > 0 && locationLatitude > 0) {
            try {
                // 进行地图检索
                doSendMapQueryUrl(locationLongitude, locationLatitude);
                return;
            } catch (Throwable ex) {
                SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointList  error:", ex);
                // 经纬度格式不正确，显示没有找到相关结果提示
                setViewVisibility(View.GONE, View.VISIBLE, View.GONE,
                        View.GONE, View.GONE);
                return;
            }
        }

        try {
            // 扩展参数的经纬度为空时执行定位
            DuoquUtils.getSdkDoAction().getLocation(getApplicationContext(),
                    mHandler);
        } catch (Throwable ex) {
             SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointList  error:", ex);
            // 经纬度格式不正确，显示没有找到相关结果提示
            setViewVisibility(View.GONE, View.VISIBLE, View.GONE, View.GONE,
                    View.GONE);
        }
    }

    /**
     * 显示/隐藏View
     * 
     * @param loadingVisibility
     *            附近网点加载中(true:显示 false:隐藏)
     * @param notFindVisibility
     *            没有找到相关结果(true:显示 false:隐藏)
     * @param networkLoseVisibility
     *            网络出错(true:显示 false:隐藏)
     * @param nearbyPointListVisibility
     *            附近网点(true:显示 false:隐藏)
     */
    private void setViewVisibility(int loadingVisibility,
            int notFindVisibility, int networkLoseVisibility,
            int nearbyPointListVisibility, int mCloseGpsErrorVisibility) {
        mNearbyPointLoadingLinearLayout.setVisibility(loadingVisibility);// 附近网点加载中
        mNearbyPointNotFindLinearLayout.setVisibility(notFindVisibility);// 没有找到相关结果
        mNearbyPointNetworkLoseLinearLayout
                .setVisibility(networkLoseVisibility);// 网络出错
        mNearbyPointListLinearLayout.setVisibility(nearbyPointListVisibility);// 附近网点
        mCloseGpsErrorLinearLayout.setVisibility(mCloseGpsErrorVisibility);// GPS出错

        // 没有找到相关结果或网络出错重置分页页码
        if (notFindVisibility == View.VISIBLE
                || networkLoseVisibility == View.VISIBLE) {
            mPageNum = 0;
        }
    }

    /**
     * 解析Json，获取ListView Item数据
     * 
     * @throws JSONException
     */
    private ArrayList<HashMap<String, Object>> getListItems(
            String strQueryResult) {
        try {
            // 初始化list数组对象
            ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

            JSONObject jsonQueryResultObject = new JSONObject(strQueryResult);

            // 设置检索结果数据总数量
            mTotal = jsonQueryResultObject.getInt("total");

            JSONArray jsonResultsArray = jsonQueryResultObject
                    .getJSONArray("results");
            JSONObject jsonObject;

            for (int i = 0; i < jsonResultsArray.length(); i++) {
                jsonObject = jsonResultsArray.getJSONObject(i);
                // 初始化map数组对象
                HashMap<String, Object> map = new HashMap<String, Object>();

                map.put("name", jsonObject.getString("name"));
                map.put("address", jsonObject.getString("address"));

                if (jsonObject.has("telephone")) {
                    map.put("phone", jsonObject.getString("telephone"));
                } else {
                    map.put("phone", "");
                }

                map.put("distance", jsonObject.getJSONObject("detail_info")
                        .getInt("distance"));

                jsonObject = jsonObject.getJSONObject("location");
                map.put("longitude", jsonObject.getDouble("lng"));
                map.put("latitude", jsonObject.getDouble("lat"));

                list.add(map);
            }
            return list;
        } catch (Throwable e) {
            return new ArrayList<HashMap<String, Object>>();
        }
    }

    /**
     * 解析检索结果，根据结果显示对应界面
     */
    private void analysisResult(String strQueryResult) {
        JSONObject jsonQueryResultObject;
        if (strQueryResult == null) {
            setViewVisibility(View.GONE, View.VISIBLE, View.GONE, View.GONE,
                    View.GONE);
            return;
        }
        try {
            // 判断检索结果是否正常，status非0则代表检索结果异常
            jsonQueryResultObject = new JSONObject(strQueryResult);
            if (jsonQueryResultObject.getInt("status") != 0) {
                throw new JSONException("返回结果异常");
            }

            // 判断检索结果数量，0代表没有检索到匹配数据
            if (jsonQueryResultObject.getInt("total") == 0) {
                throw new JSONException("检索结果数据数量为0");
            }

        } catch (Throwable e) {
            // 无法解析检索结果，显示没有搜索到相关结果提示
            setViewVisibility(View.GONE, View.VISIBLE, View.GONE, View.GONE,
                    View.GONE);
            return;
        }

        ArrayList<HashMap<String, Object>> newListItemDataArrayList = getListItems(strQueryResult);

        for (HashMap<String, Object> newListItem : newListItemDataArrayList) {
            mListItems.add(newListItem);
        }

        // 检索结果数据总数量大于10个显示底部点击加载更多数据
        if (mTotal > 10) {
            mloadMoreLinearLayout.setVisibility(View.VISIBLE);
        }

        // 刷新数据
        mNearbyPointListViewAdapter.notifyDataSetChanged();

        // 显示附件网点列表
        setViewVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE,
                View.GONE);
    }

    /**
     * 百度地图检索结果传递到附近网点列表
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isFinishing()) { // 所在的Activity已经释放
                return;
            }
            switch (msg.what) {
            case NearbyPoint.QUERY_RESULT_RECEIVE:
                analysisResult(msg.getData()
                        .getString(NearbyPoint.QUERY_RESULT));
                if (mTotal > (mPageNum + 1) * 10) {
                    // 显示图片
                    mLoadMoreImageView.setVisibility(View.VISIBLE);
                    // 修改提示为“点击加载更多数据”
                    ViewUtil.setTextViewValue(
                            mLoadMoreTextView,
                            getApplication().getString(
                                    R.string.duoqu_tip_load_more));
                    // 启用LinearLayout
                    mloadMoreLinearLayout.setEnabled(true);
                } else {
                    // 没有更多数据，隐藏点击加载更多数据
                    mloadMoreLinearLayout.setVisibility(View.GONE);
                }
                break;
            case NearbyPoint.QUERY_PARAM_ERROR:
                LogManager.i(LTAG, "地图检索参数错误");
                // 地图检索参数错误，显示没有搜索到相关结果提示
                setViewVisibility(View.GONE, View.VISIBLE, View.GONE,
                        View.GONE, View.GONE);
                break;
            case NearbyPoint.GET_QUERY_URL_FAILURE:
                LogManager.i(LTAG, "参数错误无法生成百度地图检索Url");
                // 参数错误无法生成百度地图检索Url，显示没有搜索到相关结果提示
                setViewVisibility(View.GONE, View.VISIBLE, View.GONE,
                        View.GONE, View.GONE);
                break;
            case NearbyPoint.QUERY_REQUEST_ERROR:
                LogManager.i(LTAG, "百度地图检索url请求失败");
                // 百度地图检索url请求失败，显示没有搜索到相关结果提示
                setViewVisibility(View.GONE, View.VISIBLE, View.GONE,
                        View.GONE, View.GONE);
                break;
            case AbsSdkDoAction.DO_SEND_MAP_QUERY_URL:
                mLocationLongitude = msg.getData().getDouble("longitude");
                mLocationLatitude = msg.getData().getDouble("latitude");
                doSendMapQueryUrl(mLocationLongitude, mLocationLatitude);
                break;
            case NearbyPoint.DO_GET_LOCATION:
                if (XyUtil.checkNetWork(getApplicationContext(), 2) != 0) {
                    // 显示网络出错提示
                    setViewVisibility(View.GONE, View.GONE, View.VISIBLE,
                            View.GONE, View.GONE);
                } else {
                    String locationLongitude = getIntent().getStringExtra(
                            "locationLongitude");
                    String locationLatitude = getIntent().getStringExtra(
                            "locationLatitude");
                    if (!StringUtils.isNull(locationLongitude)
                            && StringUtils.isNull(locationLatitude)) {
                        mLocationLongitude = Double
                                .parseDouble(locationLongitude);
                        mLocationLatitude = Double
                                .parseDouble(locationLatitude);
                    } else {
                        mLocationLongitude = -1;
                        mLocationLatitude = -1;
                    }
                    doGetLocation(mLocationLongitude, mLocationLatitude);
                }
                break;
            case QUERY_GPS_CLOSED_ERROR:
                LogManager.i(LTAG, "gps关闭");
                // 百度地图检索url请求失败，显示没有搜索到相关结果提示
                setViewVisibility(View.GONE, View.GONE, View.GONE, View.GONE,
                        View.VISIBLE);
                break;
            default:
                break;
            }
        }
    };
}
