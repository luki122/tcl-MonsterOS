package cn.com.xy.sms.sdk.ui.popu.widget;

import org.json.JSONArray;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.util.SdkCallBack;

public class ListDialog4NumSwitch extends Dialog {

    public JSONArray mDataSourceJsonArray = null;
    SelectDataAdapter mDataAdapter = null;
    SdkCallBack mCallBack = null;
    private String mTitleName = "";
    private TextView mTitle = null;
    ListView mListView = null;
    private Context mContext;

    public ListDialog4NumSwitch(SelectDataAdapter dataAdapter,
            SdkCallBack callBack, Context context, int dialogStyle,
            String titleName) {
        super(context, dialogStyle);
        this.mTitleName = titleName;
        this.mDataAdapter = dataAdapter;
        this.mCallBack = callBack;
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.duoqu_list_dialog);

        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText(mTitleName);
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(mDataAdapter);

        setListViewHeightBasedOnChildren(mListView);

        findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentUtil.callBackExecute(mCallBack);
                ListDialog4NumSwitch.this.dismiss();
            }
        });

        findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialog4NumSwitch.this.dismiss();
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
        if (params.height > ViewUtil.dp2px(mContext, 179)) {
            params.height = ViewUtil.dp2px(mContext, 179);
        }
        listView.setLayoutParams(params);
    }

}