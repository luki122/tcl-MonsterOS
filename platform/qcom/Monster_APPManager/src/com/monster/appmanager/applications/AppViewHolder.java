/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monster.appmanager.applications;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.android.settingslib.applications.ApplicationsState;
import com.monster.appmanager.R;
import com.monster.appmanager.utils.AppPermissionGroup;
import com.monster.appmanager.utils.AppPermissionsForGet;
import com.monster.appmanager.utils.Utils;
import com.monster.permission.ui.MstPermission.MstAppGroup;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.icu.text.ListFormatter;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

// View Holder used when displaying views
public class AppViewHolder {
    public ApplicationsState.AppEntry entry;
    public View rootView;
    public TextView appName;
    public ImageView appIcon;
    public TextView summary;
    public TextView disabled;
    public ViewGroup widgetFrame;
    private static RelativeLayout.LayoutParams titleLayoutParams;
    public Button btnKill;

    static public AppViewHolder createOrRecycle(LayoutInflater inflater, View convertView, int listType) {
    	AppViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.preference_app, null);
            ViewGroup widgetFrame =  (ViewGroup) convertView.findViewById(android.R.id.widget_frame);
            inflater.inflate(R.layout.widget_text_views, widgetFrame);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new AppViewHolder();
            holder.rootView = convertView;
            holder.appName = (TextView) convertView.findViewById(android.R.id.title);
            holder.appIcon = (ImageView) convertView.findViewById(android.R.id.icon);
            holder.summary = (TextView) convertView.findViewById(R.id.widget_text1);
            holder.disabled = (TextView) convertView.findViewById(R.id.widget_text2);
            holder.btnKill = (Button)convertView.findViewById(R.id.btn_kill);
            holder.widgetFrame = widgetFrame;
            titleLayoutParams = (RelativeLayout.LayoutParams)holder.appName.getLayoutParams();
            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
        	holder = (AppViewHolder)convertView.getTag();
        }
        
        updateViewByType(holder, listType, titleLayoutParams);
        
        return holder;
    }

    void updateSizeText(CharSequence invalidSizeStr, int whichSize) {
        if (ManageApplications.DEBUG) Log.i(ManageApplications.TAG, "updateSizeText of "
                + entry.label + " " + entry + ": " + entry.sizeStr);
        if (entry.sizeStr != null) {
            switch (whichSize) {
                case ManageApplications.SIZE_INTERNAL:
                    summary.setText(entry.internalSizeStr);
                    break;
                case ManageApplications.SIZE_EXTERNAL:
//                    summary.setText(entry.externalSizeStr);
                	long cacheSize = entry.cacheSize + entry.externalCacheSize;
                    summary.setText(getSizeStr(summary.getContext(), cacheSize));
                    break;
                default:
                    summary.setText(entry.sizeStr);
                    break;
            }
        } else if (entry.size == ApplicationsState.SIZE_INVALID) {
            summary.setText(invalidSizeStr);
        }
    }
    
    private int position = 0;
    /**
     * add by luolaigang for view permissions
     */
	public void updatePermissionText() {
		position++;
        final Message msg = new Message();
        msg.what = position;
		summary.setText("");
		new Thread(){
			@Override
			public void run() {
				super.run();
				int[] counts = new int[3];
		        ArrayList<CharSequence> groupLabels = new ArrayList<>();
		        CharSequence summaryText = null;
				if(getPermissionsCount(summary.getContext(), entry.info.packageName, counts, groupLabels)){
					summaryText = counts[0]+"";
		            final Resources res = summary.getContext().getResources();
		           
		            if (counts != null) {
		                int totalCount = counts[1];
		                int additionalCounts = counts[2];

		                if (totalCount == 0) {
		                	summaryText = res.getString(R.string.runtime_permissions_summary_no_permissions_requested);
		                } else {
		                    final ArrayList<CharSequence> list = new ArrayList(Arrays.asList(groupLabels));
		                    if (additionalCounts > 0) {
		                        // N additional permissions.
		                        list.add(res.getQuantityString(
		                                R.plurals.runtime_permissions_additional_count,
		                                additionalCounts, additionalCounts));
		                    }
		                    if (list.size() == 0) {
		                    	summaryText = res.getString(R.string.runtime_permissions_summary_no_permissions_granted);
		                    } else {
		                    	summaryText = ListFormatter.getInstance().format(list);
		                    }
		                }
		            }
		            if(counts[1]>0){
		            	summaryText = res.getString(R.string.permission_count, ""+counts[1]);
		            }
		            msg.obj = summaryText;
		            updateTextHandler.sendMessage(msg);
				}
			}
		}.start();

	}
	
	public void updatePermissionText(MstAppGroup appGroup) {
		int count = 0;
		final Resources res = summary.getContext().getResources();
		if(appGroup != null) {
			count = appGroup.size();
		}
		String  summaryText = res.getString(R.string.permission_count, ""+count);
		summary.setText(summaryText);
	}
	
	private Handler updateTextHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what == position){
				summary.setText((String)msg.obj);
			}			
		};
	};
	
	/**
	 * add by luolaigang for view permissions
	 * 获取权限
	 * @param context
	 * @param pkg
	 * @param counts
	 * @param grantedGroups
	 * @return
	 */
    public boolean getPermissionsCount(Context context, String pkg, int[] counts, ArrayList<CharSequence> grantedGroups) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkg, PackageManager.GET_PERMISSIONS);
            AppPermissionsForGet appPermissions = new AppPermissionsForGet(context, packageInfo, null, false, null);
            int grantedCount = 0;
            int totalCount = 0;
            int additionalCount = 0;

            for (AppPermissionGroup group : appPermissions.getPermissionGroups()) {
            	if(pkg.equals("com.example.nettest")){
            		System.out.println("group.getName()="+group.getName());
            	}
                if (Utils.shouldShowPermission(group, pkg)) {
                    totalCount++;
                    if (group.areRuntimePermissionsGranted()) {
                        grantedCount++;

                        if (Utils.OS_PKG.equals(group.getDeclaringPackage())) {
                            grantedGroups.add(group.getLabel());
                        } else {
                            additionalCount++;
                        }
                    }
                }
            }

            // Sort
            Collator coll = Collator.getInstance();
            coll.setStrength(Collator.PRIMARY);
            Collections.sort(grantedGroups, coll);

            // Set results
            counts[0] = grantedCount;
            counts[1] = totalCount;
            counts[2] = additionalCount;

            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * 根据列表类型修改ui布局
     * 
     * @param listType
     */
    private static void updateViewByType(AppViewHolder holder,  int listType, RelativeLayout.LayoutParams params) {
    	if(listType == ManageApplications.LIST_TYPE_STORAGE 
    			|| listType == ManageApplications.LIST_TYPE_MAIN) {
    		holder.widgetFrame.setVisibility(View.GONE);
    		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(params);
    		lp.addRule(RelativeLayout.CENTER_VERTICAL);
    		holder.appName.setLayoutParams(lp);
    		
    		if(listType == ManageApplications.LIST_TYPE_STORAGE) {
    			holder.rootView.findViewById(R.id.widget_frame2).setVisibility(View.VISIBLE);;
    			holder.summary = (TextView)holder.rootView.findViewById(R.id.widget_text3);    		
    		}
    		holder.btnKill.setVisibility(View.GONE);
    	} else if(listType == R.id.running_app) {
    		holder.widgetFrame.setVisibility(View.VISIBLE);
    		holder.appName.setLayoutParams(params);
    		holder.btnKill.setVisibility(View.VISIBLE);
//    		holder.btnKill.setTag(holder.entry);
    	} else if(listType == R.id.permissions) {
    			holder.rootView.findViewById(R.id.widget_frame2).setVisibility(View.VISIBLE);
    	}
    }
    
    private static String getSizeStr(Context context, long size) {
        if (size < 0) {
        	size = 0;
        }
        return Formatter.formatFileSize(context, size);
    }
}