package cn.com.xy.sms.sdk.ui.popu.widget;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class ListDialog extends Dialog {

    // data source
    // [{\"name\":\"东莞东\",\"spt\":\"06:26\",\"stt\":\"06:26\"},{\"name\":\"惠州\",\"spt\":\"07:04\",\"stt\":\"07:08\"},{\"name\":\"河源\",\"spt\":\"08:01\",\"stt\":\"08:13\"},{\"name\":\"龙川\",\"spt\":\"09:13\",\"stt\":\"09:16\"},{\"name\":\"赣州\",\"spt\":\"12:20\",\"stt\":\"12:31\"},{\"name\":\"兴国\",\"spt\":\"13:26\",\"stt\":\"13:30\"},{\"name\":\"吉安\",\"spt\":\"14:55\",\"stt\":\"15:10\"},{\"name\":\"向塘\",\"spt\":\"17:38\",\"stt\":\"17:58\"},{\"name\":\"九江\",\"spt\":\"19:50\",\"stt\":\"19:56\"},{\"name\":\"蕲春\",\"spt\":\"21:02\",\"stt\":\"21:05\"},{\"name\":\"黄州\",\"spt\":\"21:46\",\"stt\":\"21:49\"},{\"name\":\"麻城\",\"spt\":\"22:30\",\"stt\":\"22:39\"},{\"name\":\"潢川\",\"spt\":\"23:53\",\"stt\":\"00:20\"},{\"name\":\"阜阳\",\"spt\":\"01:38\",\"stt\":\"02:03\"},{\"name\":\"商丘南\",\"spt\":\"04:50\",\"stt\":\"04:56\"},{\"name\":\"菏泽\",\"spt\":\"06:02\",\"stt\":\"06:05\"},{\"name\":\"聊城\",\"spt\":\"07:55\",\"stt\":\"08:01\"},{\"name\":\"临清\",\"spt\":\"08:41\",\"stt\":\"08:45\"},{\"name\":\"清河城\",\"spt\":\"09:03\",\"stt\":\"09:07\"},{\"name\":\"衡水\",\"spt\":\"10:00\",\"stt\":\"10:06\"},{\"name\":\"任丘\",\"spt\":\"11:19\",\"stt\":\"11:32\"},{\"name\":\"天津\",\"spt\":\"13:34\",\"stt\":\"14:12\"},{\"name\":\"唐山\",\"spt\":\"17:02\",\"stt\":\"17:10\"},{\"name\":\"昌黎\",\"spt\":\"18:19\",\"stt\":\"18:22\"},{\"name\":\"北戴河\",\"spt\":\"18:42\",\"stt\":\"18:46\"},{\"name\":\"秦皇岛\",\"spt\":\"19:04\",\"stt\":\"19:08\"},{\"name\":\"山海关\",\"spt\":\"19:39\",\"stt\":\"19:46\"},{\"name\":\"绥中北\",\"spt\":\"20:28\",\"stt\":\"20:47\"},{\"name\":\"沈阳北\",\"spt\":\"00:50\",\"stt\":\"00:58\"},{\"name\":\"长春\",\"spt\":\"04:28\",\"stt\":\"04:40\"},{\"name\":\"陶赖昭\",\"spt\":\"05:50\",\"stt\":\"06:23\"}]"
    public JSONArray mDataSourceJsonArray = null;
    private OnSurelistener mOls = null;
    private boolean mNoDataReturn = true;
    private Context mContext;
    private String mDefaultSelectedStation;

    public ListDialog(OnSurelistener osl, JSONArray dataSourceJsonArray,
            Context context, int dialogStyle, String defaultSelectedStation) {
        super(context, dialogStyle);
        this.mDataSourceJsonArray = dataSourceJsonArray;
        this.mOls = osl;
        this.mContext = context;
        this.mDefaultSelectedStation = defaultSelectedStation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.duoqu_list_dialog);
        ListView dataListView = (ListView) findViewById(R.id.list);
        final DrawAdapter drawAdapter = new DrawAdapter();
        dataListView.setAdapter(drawAdapter);
        setListViewHeightBasedOnChildren(dataListView);
        findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ListDialog.this.dismiss();
            }
        });
        findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                for (String key : drawAdapter.mCheckedStates.keySet()) {
                    if (drawAdapter.mCheckedStates.get(key).equals(true)) {
                        try {
                            mNoDataReturn = false;
                            if (mOls != null) {
                                mOls.onClick(mDataSourceJsonArray
                                        .getJSONObject(Integer.parseInt(key)));
                            }
                            ListDialog.this.dismiss();
                        } catch (Throwable e) {
                            SmartSmsSdkUtil.smartSdkExceptionLog("ListDialog onCreate error:", e);
                        }
                    }
                }
            }

        });

    }

    public void setListViewHeightBasedOnChildren(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        if (totalHeight < ViewUtil.dp2px(mContext, 120)) {
            params.height = ViewUtil.dp2px(mContext, 120);
        }
        if (totalHeight > ViewUtil.dp2px(mContext, 200)) {
            params.height = ViewUtil.dp2px(mContext, 200);
        }
        listView.setLayoutParams(params);
    }

    @Override
    public void dismiss() {
        if (mNoDataReturn && mOls != null) {
            mOls.onClick(null);
        }

        super.dismiss();
    }

    class DrawAdapter extends BaseAdapter {

        // checked state
        private HashMap<String, Boolean> mCheckedStates;

        public DrawAdapter() {
            mCheckedStates = new HashMap<String, Boolean>();
            int j = -1;
            for (int i = 0; i < mDataSourceJsonArray.length(); i++) {

                JSONObject jsonObject = mDataSourceJsonArray.optJSONObject(i);
                String name = jsonObject.optString("name");
                if (name.equals(mDefaultSelectedStation)) {
                    j = i;
                }
                if (j == i) {
                    mCheckedStates.put(String.valueOf(i), true);
                } else {
                    mCheckedStates.put(String.valueOf(i), false);
                }

                if (j == -1 && i == mDataSourceJsonArray.length() - 1) {
                    mCheckedStates.put(String.valueOf(i), true);
                }
            }
        }

        @Override
        public int getCount() {
            return mDataSourceJsonArray.length();
        }

        @Override
        public Object getItem(int arg0) {
            JSONObject jsonObject = null;
            try {
                jsonObject = mDataSourceJsonArray.getJSONObject(arg0);
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("ListDialog getItem error:", e);
            }
            return jsonObject;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(final int arg0, View convertView, ViewGroup arg2) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.duoqu_list_items_content, null);
                viewHolder = new ViewHolder();
                viewHolder.mItemRadioButton = (RadioButton) convertView
                        .findViewById(R.id.item_rb);
                viewHolder.mItemTextView = (TextView) convertView
                        .findViewById(R.id.item_text);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.mItemTextView.setText(((JSONObject) getItem(arg0))
                    .optString("name"));

            viewHolder.mItemRadioButton
                    .setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            for (String key : mCheckedStates.keySet()) {
                                mCheckedStates.put(key, false);

                            }
                            mCheckedStates.put(String.valueOf(arg0),
                                    ((RadioButton) v).isChecked());
                            DrawAdapter.this.notifyDataSetChanged();
                        }
                    });
            convertView.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    for (String key : mCheckedStates.keySet()) {
                        mCheckedStates.put(key, false);

                    }
                    mCheckedStates.put(String.valueOf(arg0), true);
                    DrawAdapter.this.notifyDataSetChanged();
                }
            });

            boolean res = false;
            if (mCheckedStates.get(String.valueOf(arg0)) == null
                    || mCheckedStates.get(String.valueOf(arg0)) == false) {
                res = false;
                mCheckedStates.put(String.valueOf(arg0), false);
            } else
                res = true;

            viewHolder.mItemRadioButton.setChecked(res);
            return convertView;
        }
    }

    class ViewHolder {
        protected TextView mItemTextView;
        protected RadioButton mItemRadioButton;
    }
}