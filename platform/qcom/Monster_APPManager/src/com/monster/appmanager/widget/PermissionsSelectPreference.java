package com.monster.appmanager.widget;

import com.monster.appmanager.R;
import com.monster.appmanager.utils.AppPermissionGroup;
import com.monster.permission.ui.MstPermission;
import com.monster.permission.ui.MstPermission.MstPermEntry;
import com.monster.permission.ui.MstPermission.MstPermGroup;
import com.monster.permission.ui.MstPermission.PermOpSelectItem;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;
import mst.preference.Preference;

public class PermissionsSelectPreference extends Preference 
						implements  Preference.OnPreferenceClickListener, DialogInterface.OnClickListener {
	public static final int OPEN = 0;
	public static final int ASK = 1;
	public static final int CLOSED = 2;
	private TextView spinnerText;
	private PermOpSelectItem permOpSelectItem;
	
	public PermissionsSelectPreference(Context context) {
		super(context);
		init(context, 0);
	}
	
	public PermissionsSelectPreference(Context context, int layout) {
		super(context);
		init(context, layout);
	}
	
	public void init(Context context, int layout) {
		if(layout <= 0) {
			layout = R.layout.permission_app_item;
		}
		setLayoutResource(layout);
		setSelectable(true);
		setOnPreferenceClickListener(this);
		permOpSelectItem = new PermOpSelectItem(context);
		permOpSelectItem.setListener(this);
	}
	
	@Override
	protected void onBindView(View arg0) {
		super.onBindView(arg0);
		spinnerText = (TextView)arg0.findViewById(R.id.widget_text3);
		updateSpinnerText();
	}

	public void setSelected(MstPermEntry entry, MstPermission mstPermission) {
		permOpSelectItem.setPermEntry(entry);
		updateSpinnerText();
		
		if(mstPermission != null) {
			if(MstPermission.VR_SERVICE_GROUP.equals(entry.getGroupName())) {
				MstPermGroup permGroup =  mstPermission.getPermGroupMap().get(MstPermission.VR_SERVICE_GROUP);
				if(permGroup != null) {
					permOpSelectItem.setAppGroupMap(permGroup.getAppGroupMap());
				}
			}
		}
	}

	@Override
	public boolean onPreferenceClick(Preference arg0) {
		permOpSelectItem.showPermOpSelectDialog(getContext());
		return true;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		updateSpinnerText();
	}
	
	private void updateSpinnerText() {
		if(spinnerText != null){
			spinnerText.setText(permOpSelectItem.getCurrenModeLabel());
		}
	}
	
	
	public void setSelected(boolean areRuntimePermissionsGranted, boolean userFixed) { }
	public static int setSelectedIndex(boolean areRuntimePermissionsGranted,
			boolean userFixed) {
		int index = areRuntimePermissionsGranted?OPEN:ASK;
		if(!areRuntimePermissionsGranted){
			index = userFixed?CLOSED:ASK;
		}
		return index;
	}
	public void setGroup(AppPermissionGroup group) { }
}
