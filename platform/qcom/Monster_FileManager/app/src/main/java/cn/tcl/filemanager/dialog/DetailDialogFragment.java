/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.utils.CommonUtils;

public class DetailDialogFragment {

	public static AlertDialog createDetailDialog(Context context, String title,
			List<String> titleAdapter, List<String> valueAdapter) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		SimPickerAdapter simAdapter = new SimPickerAdapter(context, titleAdapter, valueAdapter);
		builder.setSingleChoiceItems(simAdapter, -1, null)
				.setTitle(title)
				.setNegativeButton(
						context.getResources().getString(R.string.ok), null);
		return builder.create();
	}

	private static class SimPickerAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<String> mTitleAdapter;
        private List<String> mNameAdapter;

        public SimPickerAdapter(Context context,List<String> titleAdapter,List<String> valueAdapter){
            mInflater = LayoutInflater.from(context);
            mTitleAdapter = titleAdapter;
            mNameAdapter = valueAdapter;
        }

        public int getCount() {
            return mTitleAdapter.size();
        }

        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            SingleHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.detail_dialog, null);

                holder = new SingleHolder();
                holder.detail_title = (TextView) convertView.findViewById(R.id.detail_title);
                holder.detail_value = (TextView)convertView.findViewById(R.id.detail_value);
                holder.divider = (TextView)convertView.findViewById(R.id.detail_divider);
                convertView.setTag(holder);
            } else {
                holder = (SingleHolder) convertView.getTag();
            }
            if (position == mTitleAdapter.size()) {
                holder.divider.setVisibility(View.GONE);
            } else {
                holder.divider.setVisibility(View.VISIBLE);
            }
            if(CommonUtils.getRobotoMedium()!=null){
                holder.detail_value.setTypeface(CommonUtils.getRobotoMedium());
            }
            holder.detail_title.setText(mTitleAdapter.get(position));
            holder.detail_value.setText(mNameAdapter.get(position));
            return convertView;
        }

    }

	private static class SingleHolder {
        TextView detail_title;
        TextView detail_value;
        TextView divider;
    }
}
