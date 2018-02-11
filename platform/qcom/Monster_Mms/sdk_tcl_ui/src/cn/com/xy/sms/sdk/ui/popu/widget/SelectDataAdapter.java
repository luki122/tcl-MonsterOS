package cn.com.xy.sms.sdk.ui.popu.widget;

import java.util.HashMap;

import org.json.JSONArray;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.util.StringUtils;

public class SelectDataAdapter extends BaseAdapter {

    public HashMap<String, Boolean> mCheckedStates = null;
    private AdapterDataSource mAdapterDataSource = null;
    private LayoutInflater mLayoutInflater = null;

    public SelectDataAdapter(Context context,
            AdapterDataSource adapterDataSource, DuoquDialogSelected selected) {
        if (adapterDataSource == null
                || adapterDataSource.getDataSrouce() == null) {
            return;
        }
        mAdapterDataSource = adapterDataSource;
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCheckedStates = new HashMap<String, Boolean>();
        int selectedIndex = -1;
        int dataLen = adapterDataSource.getDataSrouce().length();
        for (int i = 0; i < dataLen; i++) {
            if ((!StringUtils.isNull(selected.getSelectName()) && mAdapterDataSource
                    .getDisplayValue(i).equals(selected.getSelectName()))
                    || selected.getSelectIndex() == i) {
                selectedIndex = i;
            }
            if (selectedIndex == i) {
                mCheckedStates.put(String.valueOf(i), true);
            } else {
                mCheckedStates.put(String.valueOf(i), false);
            }

            if (selectedIndex == -1 && i == dataLen - 1) {
                mCheckedStates.put(String.valueOf(i), true);
            }
        }
    }

    @Override
    public int getCount() {
        return (mAdapterDataSource == null || mAdapterDataSource
                .getDataSrouce() == null) ? 0 : mAdapterDataSource
                .getDataSrouce().length();
    }

    @Override
    public Object getItem(int arg0) {
        return (mAdapterDataSource == null || mAdapterDataSource
                .getDataSrouce() == null) ? null : mAdapterDataSource
                .getDataSrouce().opt(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(final int index, View convertView, ViewGroup arg2) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(
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

        viewHolder.mItemTextView.setText(getDisplayValue(index));
        viewHolder.mItemRadioButton
                .setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        for (String key : mCheckedStates.keySet()) {
                            mCheckedStates.put(key, false);
                        }
                        mCheckedStates.put(String.valueOf(index),
                                ((RadioButton) v).isChecked());
                        SelectDataAdapter.this.notifyDataSetChanged();
                    }
                });
        convertView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                for (String key : mCheckedStates.keySet()) {
                    mCheckedStates.put(key, false);
                }
                mCheckedStates.put(String.valueOf(index), true);
                SelectDataAdapter.this.notifyDataSetChanged();
            }
        });

        boolean res = false;
        if (mCheckedStates.get(String.valueOf(index)) == null
                || mCheckedStates.get(String.valueOf(index)) == false) {
            res = false;
            mCheckedStates.put(String.valueOf(index), false);
        } else {
            res = true;
        }
        viewHolder.mItemRadioButton.setChecked(res);
        return convertView;
    }

    public JSONArray getDataSource() {
        return mAdapterDataSource == null ? null : mAdapterDataSource
                .getDataSrouce();
    }

    public String getDisplayValue(int index) {
        return mAdapterDataSource == null ? null : mAdapterDataSource
                .getDisplayValue(index);
    }
}

class ViewHolder {
    protected TextView mItemTextView;
    protected RadioButton mItemRadioButton;
}
