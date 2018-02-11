package com.monster.appmanager.widget;

import com.monster.appmanager.R;
import com.monster.appmanager.utils.AppPermissionGroup;
import com.monster.permission.ui.MstPermission;
import com.monster.permission.ui.MstPermission.MstPermEntry;

import android.content.Context;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;
import mst.preference.Preference;
import mst.view.menu.PopupMenu;

public class PermissionsSelectPreference extends Preference implements OnItemSelectedListener, View.OnClickListener {
	private AppPermissionGroup group;
	private Spinner spinner;
	public static final int OPEN = 0;
	public static final int ASK = 1;
	public static final int CLOSED = 2;
	private int index = 2;
	private View spinnerBtn;
	private TextView spinnerText;
	private String[] permissionTypes;
	private MstPermEntry permEntry;
	
	public PermissionsSelectPreference(Context context) {
		super(context);
		init(context, 0);
	}
	
	public PermissionsSelectPreference(Context context, int layout) {
		super(context);
		init(context, layout);
	}
	
	public void init(Context context, int layout) {
		permissionTypes = getContext().getResources().getStringArray(R.array.permition_types);
		setSelectable(false);
		if(layout <= 0) {
			layout = R.layout.permission_app_item;
		}
		setLayoutResource(layout);
//		setLayoutResource(R.layout.preference_permissions_select);
//		setWidgetLayoutResource(R.layout.preference_permissions_widget);
	}
	
	@Override
	protected void onBindView(View arg0) {
		super.onBindView(arg0);
		spinner = (Spinner)arg0.findViewById(R.id.permition_types);
		if(spinner!=null){
			spinner.setSelection(index);
			spinner.setOnItemSelectedListener(this);
		}		
		
		spinnerBtn = arg0.findViewById(R.id.spinner_btn);
		spinnerBtn.setOnClickListener(this);
		spinnerText = (TextView)spinnerBtn.findViewById(R.id.spinner_text);
		spinnerText.setText(getCurrenModeLabel());
	}

	public void setSelected(boolean areRuntimePermissionsGranted,
			boolean userFixed) {
		index = setSelectedIndex(areRuntimePermissionsGranted, userFixed);
		if(spinner!=null){
			spinner.setSelection(index);
		}	
		if(spinnerText != null){
			spinnerText.setText(permissionTypes[index]);
		}
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

	@Override
	public void onClick(View v) {
		if(v == spinnerBtn) {
			PopupMenu popupMenu = new PopupMenu(getContext(), v, Gravity.RIGHT);
			popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					int id = item.getItemId();
					int mode = MstPermission.UNKNOWN_MODE;
					if(id == R.id.option_ask) {
						mode = MstPermission.ASK_MODE;
					} else if(id == R.id.option_allow) {
						mode = MstPermission.ALLOW_MODE;
					} else if(id == R.id.option_disallow) {
						mode = MstPermission.DISABLE_MODE;
					}
					permEntry.setStatus(mode);
					spinnerText.setText(item.getTitle());
					updateMode();
					return true;
				}
			});
			popupMenu.inflate(R.menu.shortcut_options);
			popupMenu.show();
		}
	}
	
	public void setSelected(MstPermEntry entry) {
		this.permEntry = entry;
		if(spinnerText != null){
			spinnerText.setText(getCurrenModeLabel());
		}
	}
	
	private String getCurrenModeLabel() {
		String label = null;
		int mode = permEntry.getStatus();
		if(mode == MstPermission.ALLOW_MODE) {
			label = permissionTypes[0];
		} else if(mode == MstPermission.ASK_MODE) {
			label = permissionTypes[1];
		} else {
			label = permissionTypes[2];
		}
		
		return label;
	}
	
	private void updateMode() {
		MstPermission.mstUpdatePermissionStatusToDb(getContext(), permEntry);
	}
}
