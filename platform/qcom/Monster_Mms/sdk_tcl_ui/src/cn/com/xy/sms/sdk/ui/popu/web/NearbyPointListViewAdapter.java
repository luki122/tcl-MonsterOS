package cn.com.xy.sms.sdk.ui.popu.web;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.util.StringUtils;

public class NearbyPointListViewAdapter extends BaseAdapter {
    private Context mContext; // 运行上下文
    private ArrayList<HashMap<String, Object>> mListItems; // 附近网点信息集合
    private LayoutInflater mListContainer; // 视图容器

    public final class NearbyPointListItemView { // 自定义控件集合
        public TextView nearbyPointNameTextView;// 网点名称
        public TextView nearbyPointAddressTextView;// 网点地址
        public TextView nearbyPointPhoneTextView;// 网点电话
        public TextView nearbyPointDistanceTextView;// 网点距离
        public double longitude;// 经度
        public double latitude;// 纬度
    }

    public NearbyPointListViewAdapter(Context context,
            ArrayList<HashMap<String, Object>> listItems) {
        mContext = context;
        mListContainer = LayoutInflater.from(mContext);
        mListItems = listItems;
    }

    public int getCount() {
        if (mListItems == null) {
            return 0;
        }
        return mListItems.size();
    }

    public Object getItem(int arg0) {
        return arg0;
    }

    public long getItemId(int arg0) {
        return arg0;
    }

    /**
     * ListView Item设置
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        // Log.e("method", "getView");
        // 自定义视图
        NearbyPointListItemView nearbyPointListItemView = null;
        if (convertView == null) {
            nearbyPointListItemView = new NearbyPointListItemView();
            // 获取list_item布局文件的视图
            convertView = mListContainer.inflate(
                    R.layout.duoqu_nearby_point_list_item, null);
            // 获取控件对象
            nearbyPointListItemView.nearbyPointNameTextView = (TextView) convertView
                    .findViewById(R.id.duoqu_tv_nearby_point_name);
            nearbyPointListItemView.nearbyPointAddressTextView = (TextView) convertView
                    .findViewById(R.id.duoqu_tv_nearby_point_address);
            nearbyPointListItemView.nearbyPointPhoneTextView = (TextView) convertView
                    .findViewById(R.id.duoqu_tv_nearby_point_phone);
            nearbyPointListItemView.nearbyPointDistanceTextView = (TextView) convertView
                    .findViewById(R.id.duoqu_tv_nearby_point_distance);
            // 设置控件集到convertView
            convertView.setTag(nearbyPointListItemView);
        } else {
            nearbyPointListItemView = (NearbyPointListItemView) convertView
                    .getTag();
        }

        // 设置网点名称、地址、电话、距离信息
        nearbyPointListItemView.nearbyPointNameTextView
                .setText((String) mListItems.get(position).get("name"));
        nearbyPointListItemView.nearbyPointAddressTextView
                .setText((String) mListItems.get(position).get("address"));
        nearbyPointListItemView.nearbyPointDistanceTextView
                .setText(getDistanceString(Double.parseDouble(mListItems
                        .get(position).get("distance").toString())));
        nearbyPointListItemView.longitude = (Double) mListItems.get(position)
                .get("longitude");
        nearbyPointListItemView.latitude = (Double) mListItems.get(position)
                .get("latitude");

        String phone = (String) mListItems.get(position).get("phone");

        if (StringUtils.isNull(phone)) {
            nearbyPointListItemView.nearbyPointPhoneTextView
                    .setVisibility(View.GONE);
        } else {
            nearbyPointListItemView.nearbyPointPhoneTextView
                    .setVisibility(View.VISIBLE);
            nearbyPointListItemView.nearbyPointPhoneTextView
                    .setText(getPhoneString(phone));
        }

        return convertView;
    }

    /**
     * 距离数字转单位
     * 
     * @param distance
     *            距离(米)
     * @return
     */
    private String getDistanceString(double distance) {
        if (distance > 1000) {
            return String.format("%.1fkm", distance / 1000).replace(".0", "");
        }

        return String.format("%.0fm", distance);
    }

    /**
     * 电话号码格式处理
     * 
     * @param phone
     *            电话号码
     * @return
     */
    private String getPhoneString(String phone) {
        return "电话  " + phone.replace("(", "").replace(")", "-");
    }
}