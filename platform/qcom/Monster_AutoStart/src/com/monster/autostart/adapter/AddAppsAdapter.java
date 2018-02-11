package com.monster.autostart.adapter;

import java.util.List;


import com.monster.autostart.activity.AddAutoStartAppActivity;
import com.monster.autostart.adapter.AutoStartManagerAdapter.ViewHolder;
import com.monster.autostart.bean.AppInfo;
import com.monster.autostart.bean.AppManagerState;
import com.monster.autostart.db.MulwareProvider;
import com.monster.autostart.interfaces.IBaseSolution;
import com.monster.autostart.utils.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.monster.autostart.R;
public class AddAppsAdapter extends BaseAdapter {

	private List<AppInfo> list;

	private ViewHolder holder;

	private LayoutInflater inflater;

	private Context sContext;
	
	public AddAppsAdapter(Context c, List<AppInfo> l) {
		list = l;
		inflater = LayoutInflater.from(c);
		sContext = c;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		AppInfo info = list.get(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.adapter_item_add_apps,
					parent, false);
			holder.icon = (ImageView) convertView.findViewById(R.id.item_icon);
			holder.title = (TextView) convertView.findViewById(R.id.item_title);
			holder.add = (TextView) convertView.findViewById(R.id.item_add);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.icon.setImageDrawable(info.getDrawable());
		holder.title.setText(info.getTitle());
		holder.add.setOnClickListener(new lvButtonListener(position));
		
		return convertView;
	}

	class ViewHolder {
		ImageView icon;
		TextView title;
		TextView add;
	}

	class lvButtonListener implements View.OnClickListener {
		
		int position;

		public lvButtonListener(int pos) {
			position = pos;
		}

		@Override
		public void onClick(final View v) {
			// TODO Auto-generated method stub
            String  ok  = sContext.getResources().getString(R.string.str_dialog_ok);
            String  cancel  = sContext.getResources().getString(R.string.str_dialog_cancel);
            String  attention  = sContext.getResources().getString(R.string.str_dialog_attention);
            
        	final AppInfo info = list.get(position);
			AlertDialog mDeleteDialog = new AlertDialog.Builder(sContext)
			.setTitle(attention)
			.setMessage("是否禁止" + info.getTitle() +"的自启动行为？")
			
			.setPositiveButton(ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

				

					Utilities.getInstance().setAppStatus(info.getTitle(),
							Utilities.COMPONENT_AUTO_START_ENABLE,
							((AddAutoStartAppActivity) sContext).getList(),
							PackageManager.COMPONENT_ENABLED_STATE_ENABLED);

					Log.e("sunset", "info.toString()=" + info.toString());
					
					String str = sContext.getResources().getString(R.string.str_already_add);
					
					Intent intent = new Intent();
					intent.putExtra("update", true);
					((AddAutoStartAppActivity) sContext).setResult(Activity.RESULT_OK,
							intent);
					((TextView)v).setText(str);
					((TextView)v).setClickable(false);
					((TextView)v).setBackground(null);
					((TextView)v).setTextColor(sContext.getResources().getColor(R.color.adapter_choose_textview_color));
					
				}
			})
			.setNegativeButton(cancel, null).show();
			
			//((AddAutoStartAppActivity) sContext).finish();
			//((AddAutoStartAppActivity) sContext).overridePendingTransition(
			//		R.anim.slide_left_in, R.anim.slide_right_out);
		}

	}
}
