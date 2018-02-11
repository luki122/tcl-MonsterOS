package com.monster.autostart.adapter;

import java.util.List;


import com.monster.autostart.activity.AddAutoStartAppActivity;
import com.monster.autostart.activity.AutoStartMangerActivity;
import com.monster.autostart.adapter.AddAppsAdapter.ViewHolder;
import com.monster.autostart.adapter.AddAppsAdapter.lvButtonListener;
import com.monster.autostart.bean.AppInfo;
import com.monster.autostart.bean.AppManagerState;
import com.monster.autostart.db.MulwareProvider;
import com.monster.autostart.utils.Utilities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.monster.autostart.R;
public class AutoStartManagerAdapter extends BaseAdapter{

	private List<AppInfo> list;

	private ViewHolder holder;

	private LayoutInflater inflater;

	private Context sContext;
	
	public AutoStartManagerAdapter(Context c, List<AppInfo> l) {
		// TODO Auto-generated constructor stub
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
			convertView = inflater.inflate(R.layout.adapter_item_remove_apps,
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
		public void onClick(View v) {
			// TODO Auto-generated method stub
			AppInfo info = list.get(position);
			ComponentName cp = info.getIntent().getComponent();
			Log.e(Utilities.TAG, "@@cp.pkg="+cp.getPackageName()+";"+"cp.cls="+cp.getClassName()+";"+"status="+info.getStatus()+";"+"title="+info.getTitle());
			Utilities.getInstance().setAppStatus(info.getTitle(),
					Utilities.COMPONENT_AUTO_START_DISABLE,
					((AutoStartMangerActivity) sContext).getList(),
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

			list.remove(info);
			notifyDataSetChanged();

			((AutoStartMangerActivity) sContext).showContentSize(list.size());
			
			if (list.size() <= 0) {
				((AutoStartMangerActivity) sContext).showMask();
			}
		}

	}
}
