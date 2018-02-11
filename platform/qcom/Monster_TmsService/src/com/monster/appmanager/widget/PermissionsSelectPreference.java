package com.monster.appmanager.widget;

import com.mst.tms.R;
import com.monster.appmanager.utils.AppPermissionGroup;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import mst.preference.Preference;

public class PermissionsSelectPreference extends Preference implements OnItemSelectedListener {
	private AppPermissionGroup group;
	private Spinner spinner;
	public static final int OPEN = 0;
	public static final int ASK = 1;
	public static final int CLOSED = 2;
	private int index = 2;
	public PermissionsSelectPreference(Context context) {
		super(context);
//		setLayoutResource(R.layout.preference_permissions_select);
//		setWidgetLayoutResource(R.layout.preference_permissions_widget);
	}
	
	@Override
	protected void onBindView(View arg0) {
		super.onBindView(arg0);
//		spinner = (Spinner)arg0.findViewById(R.id.permition_types);
//		if(spinner!=null){
//			spinner.setSelection(index);
//			spinner.setOnItemSelectedListener(this);
//		}		
	}

	public void setSelected(boolean areRuntimePermissionsGranted,
			boolean userFixed) {
//		index = setSelectedIndex(areRuntimePermissionsGranted, userFixed);
//		if(spinner!=null){
//			spinner.setSelection(index);
//		}	
	}
	
	public static int setSelectedIndex(boolean areRuntimePermissionsGranted,
			boolean userFixed) {
		int index = areRuntimePermissionsGranted?OPEN:ASK;
		if(!areRuntimePermissionsGranted){
			index = userFixed?CLOSED:ASK;
		}
		return index;
	}

	public void setGroup(AppPermissionGroup group) {
		this.group = group;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		boolean granted = false;
		boolean doNotAskAgain = false;
        if (group == null) {
            return;
        }
        switch(position){
        case OPEN:
        	granted = true;
        	doNotAskAgain = false;
        	break;
        case ASK:
            	granted = false;
            	doNotAskAgain = false;
            	break;
        case CLOSED:
        	granted = false;
        	doNotAskAgain = true;
        	break;        
        }
        onPermissionGrantResult(granted, doNotAskAgain);
	}
	
    public void onPermissionGrantResult(boolean granted, boolean doNotAskAgain) {
        if (group != null) {
            if (granted) {
            	group.grantRuntimePermissions(doNotAskAgain);
            } else {
            	group.grantRuntimePermissions(false);
            	group.revokeRuntimePermissions(doNotAskAgain);
            }
        }
    }

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
}
