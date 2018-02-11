package com.android.packageinstaller.permission.utils;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;
import com.android.packageinstaller.R;

public class PermUtils {
	
	/**
	 * 判断权限是否存在
	 * @param context
	 * @param pkgName 安装包名
	 * @param permName 权限名
	 * @return 存在返回true 否则返回false 
	 */
	public static boolean isPermission(Context context,String pkgName,String permName){
		try {	
			PackageManager pm = context.getPackageManager();
			PackageInfo info = pm.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS);
			 if (info != null && info.requestedPermissions != null) {
	                for (int i = 0; i < info.requestedPermissions.length; i++) {
	                    String name = info.requestedPermissions[i] ;
	                    if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(permName) &&  permName.equals(name)){
	                    	return true ;
	                    }
	                }
			 }
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false ;
	}
	
	
	public static View get(Context context,String pkgName){
		PackageManager pm = context.getPackageManager();
		View view = null ;
		TextView txt_yx_perm,txt_other_perm ;
		LinearLayout llyt_ys_perm,llyt_other_perm ;
		try {
			view = LayoutInflater.from(context).inflate(R.layout.app_install_perm, null) ;
			txt_yx_perm = (TextView)view.findViewById(R.id.txt_yx_perm) ;
			txt_other_perm  = (TextView)view.findViewById(R.id.txt_other_perm) ;
			 llyt_ys_perm = (LinearLayout)view.findViewById(R.id.llyt_ys_perm) ;
			 llyt_other_perm  = (LinearLayout)view.findViewById(R.id.llyt_other_perm) ;
			 
			 LinearLayout.LayoutParams params = new LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,android.widget.LinearLayout.LayoutParams.WRAP_CONTENT) ;
			params.height = dip2px(context, 50) ;
//            PackageInfo info = pm.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS);
			PackageInfo info = pm.getPackageArchiveInfo(pkgName, PackageManager.GET_PERMISSIONS);
            Drawable openDrawable =
                    (Drawable) context.getDrawable(R.drawable.open_bg);
            openDrawable.setBounds(0, 0,
            		openDrawable.getIntrinsicWidth(),
            		openDrawable.getIntrinsicHeight());
            Drawable closeDrawable =
                    (Drawable) context.getDrawable(R.drawable.close_bg);
            closeDrawable.setBounds(0, 0,
            		closeDrawable.getIntrinsicWidth(),
            		closeDrawable.getIntrinsicHeight());
//                mStatusTextView.setCompoundDrawablesRelative(centerTextDrawable, null,
//                        null, null);
            
            if (info.requestedPermissions != null) {
                for (int i = 0; i < info.requestedPermissions.length; i++) {
                    PermissionInfo perm;
                    try {
                        perm = pm.getPermissionInfo(info.requestedPermissions[i], 0);
                    } catch (NameNotFoundException e) {
                        continue;
                    }

                    if ((perm.flags & PermissionInfo.FLAG_INSTALLED) == 0
                            || (perm.flags & PermissionInfo.FLAG_REMOVED) != 0) {
                        continue;
                    }

                    if (perm.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) {
                    	 PermissionGroupInfo group = getGroup(perm.group, pm);
                    	 View v = getPreference(context,perm, group, pm) ;
                    	 llyt_ys_perm.addView(v,params) ;
                    } else if (perm.protectionLevel == PermissionInfo.PROTECTION_NORMAL) {
                    	 PermissionGroupInfo group = getGroup(perm.group, pm);
                        View v = getPreference(context,perm, group, pm);
                        llyt_other_perm.addView(v,params) ;
                    }
                }    
            }
            if(llyt_ys_perm.getChildCount() > 0 ){
            	txt_yx_perm.setText(String.format(context.getString(R.string.lbl_yx_perm), llyt_ys_perm.getChildCount())) ;
            	llyt_ys_perm.setVisibility(View.VISIBLE) ;
            	txt_yx_perm.setVisibility(View.VISIBLE) ;
            	txt_yx_perm.setOnClickListener(new MyOnCLickListener(llyt_ys_perm,txt_yx_perm,openDrawable,closeDrawable)) ;
            }else{
            	llyt_ys_perm.setVisibility(View.GONE) ;
            	txt_yx_perm.setVisibility(View.GONE) ;
            }
            
            if(llyt_other_perm.getChildCount() > 0 ){
            	txt_other_perm.setText(String.format(context.getString(R.string.lbl_other_perm), llyt_other_perm.getChildCount())) ;
            	llyt_other_perm.setVisibility(View.VISIBLE) ;
            	txt_other_perm.setVisibility(View.VISIBLE) ;
            	txt_other_perm.setOnClickListener(new MyOnCLickListener(llyt_other_perm,txt_other_perm,openDrawable,closeDrawable)) ;
            }else{
            	llyt_other_perm.setVisibility(View.GONE) ;
            	txt_other_perm.setVisibility(View.GONE) ;
            }
            
        } catch (Exception e) {
            Log.e("PermUtils", "Problem getting package info for " + pkgName, e);
        }
        return view ;
	}
	
	
	
	private static void findOrCreate(PackageItemInfo group, PackageManager pm) {
   
    }
	
	private static View  getPreference(Context context,PermissionInfo perm, PermissionGroupInfo group,
            PackageManager pm) {
       View itemView = LayoutInflater.from(context).inflate(R.layout.item_perm, null) ;
       TextView txt = (TextView)itemView.findViewById(R.id.txt_item) ;
       txt.setText(perm.loadLabel(pm) + "") ;
       return itemView ;
    }
	
	 private static PermissionGroupInfo getGroup(String group, PackageManager pm) {
	        try {
	            return pm.getPermissionGroupInfo(group, 0);
	        } catch (NameNotFoundException e) {
	            return null;
	        }
	    }

	 public static int dip2px(Context context, float dipValue){
         final float scale = context.getResources().getDisplayMetrics().density;
         return (int)(dipValue * scale + 0.5f);
	 }

	public static int px2dip(Context context, float pxValue){
	         final float scale = context.getResources().getDisplayMetrics().density;
	         return (int)(pxValue / scale + 0.5f);
	 } 

	public static View getNewPermView(Context context,String pkgName){
		PackageManager pm = context.getPackageManager();
		View view = null ;
		TextView txt_yx_perm,txt_other_perm ;
		LinearLayout llyt_ys_perm,llyt_other_perm ;
		try {
			
			view = LayoutInflater.from(context).inflate(R.layout.app_install_perm, null) ;
			txt_yx_perm = (TextView)view.findViewById(R.id.txt_yx_perm) ;
			txt_other_perm  = (TextView)view.findViewById(R.id.txt_other_perm) ;
			 llyt_ys_perm = (LinearLayout)view.findViewById(R.id.llyt_ys_perm) ;
			 llyt_other_perm  = (LinearLayout)view.findViewById(R.id.llyt_other_perm) ;
			 
			 LinearLayout.LayoutParams params = new LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,android.widget.LinearLayout.LayoutParams.WRAP_CONTENT) ;
			params.height = dip2px(context, 46) ;
//            PackageInfo info = pm.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS);
			PackageInfo info = pm.getPackageArchiveInfo(pkgName, PackageManager.GET_PERMISSIONS);
			
			PackageInfo installInfo = pm.getPackageInfo(info.packageName, PackageManager.GET_PERMISSIONS);
			
            Drawable openDrawable =
                    (Drawable) context.getDrawable(R.drawable.open_bg);
            openDrawable.setBounds(0, 0,
            		openDrawable.getIntrinsicWidth(),
            		openDrawable.getIntrinsicHeight());
            Drawable closeDrawable =
                    (Drawable) context.getDrawable(R.drawable.close_bg);
            closeDrawable.setBounds(0, 0,
            		closeDrawable.getIntrinsicWidth(),
            		closeDrawable.getIntrinsicHeight());
            
            
            ArrayList<PermissionInfo> arr = extractPerms(info,installInfo, pm) ;
            for(int i = 0 ;i < arr.size() ;i++){
            	 PermissionGroupInfo group = getGroup(arr.get(i).group, pm);
              	 View v = getPreference(context,arr.get(i), group, pm) ;
              	 llyt_ys_perm.addView(v,params) ;
            }
            
            if(llyt_ys_perm.getChildCount() > 0 ){
               	txt_yx_perm.setText(String.format(context.getString(R.string.lbl_new_perm), llyt_ys_perm.getChildCount())) ;
               	llyt_ys_perm.setVisibility(View.VISIBLE) ;
               	txt_yx_perm.setVisibility(View.VISIBLE) ;
               	txt_yx_perm.setOnClickListener(new MyOnCLickListener(llyt_ys_perm,txt_yx_perm,openDrawable,closeDrawable)) ;
               }else{
               	llyt_ys_perm.setVisibility(View.GONE) ;
               	txt_yx_perm.setVisibility(View.GONE) ;
               }
            
         /*   if (info.requestedPermissions != null) {
                for (int i = 0; i < info.requestedPermissions.length; i++) {
                    PermissionInfo perm;
                    try {
                        perm = pm.getPermissionInfo(info.requestedPermissions[i], 0);
                    } catch (NameNotFoundException e) {
                        continue;
                    }

                    if ((perm.flags & PermissionInfo.FLAG_INSTALLED) == 0
                            || (perm.flags & PermissionInfo.FLAG_HIDDEN) != 0) {
                        continue;
                    }
                    String permName = info.requestedPermissions[i];
                    int existingIndex = -1;
                    if (info != null
                            && info.requestedPermissions != null) {
                        for (int j=0; j<info.requestedPermissions.length; j++) {
                            if (permName.equals(info.requestedPermissions[j])) {
                                existingIndex = j;
                                break;
                            }
                        }
                    }
                    
                          final int existingFlags = existingIndex >= 0 ?
                		   info.requestedPermissionsFlags[existingIndex] : 0;
                		   
                		   boolean newPerm = info != null
                                   && (existingFlags&PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0;
                		   
                		   if(newPerm){
                			   if (perm.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) {
                              	 PermissionGroupInfo group = getGroup(perm.group, pm);
                              	 View v = getPreference(context,perm, group, pm) ;
                              	 llyt_ys_perm.addView(v,params) ;
                              } else if (perm.protectionLevel == PermissionInfo.PROTECTION_NORMAL) {
                              	 PermissionGroupInfo group = getGroup(perm.group, pm);
                                  View v = getPreference(context,perm, group, pm);
                                  llyt_other_perm.addView(v,params) ;
                              }
                		   }
                		   
                		   if(llyt_ys_perm.getChildCount() > 0 ){
                           	txt_yx_perm.setText(String.format(context.getString(R.string.lbl_yx_perm), llyt_ys_perm.getChildCount())) ;
                           	llyt_ys_perm.setVisibility(View.VISIBLE) ;
                           	txt_yx_perm.setVisibility(View.VISIBLE) ;
                           	txt_yx_perm.setOnClickListener(new MyOnCLickListener(llyt_ys_perm,txt_yx_perm,openDrawable,closeDrawable)) ;
                           }else{
                           	llyt_ys_perm.setVisibility(View.GONE) ;
                           	txt_yx_perm.setVisibility(View.GONE) ;
                           }
                           
                           if(llyt_other_perm.getChildCount() > 0 ){
                           	txt_other_perm.setText(String.format(context.getString(R.string.lbl_other_perm), llyt_other_perm.getChildCount())) ;
                           	llyt_other_perm.setVisibility(View.VISIBLE) ;
                           	txt_other_perm.setVisibility(View.VISIBLE) ;
                           	txt_other_perm.setOnClickListener(new MyOnCLickListener(llyt_other_perm,txt_other_perm,openDrawable,closeDrawable)) ;
                           }else{
                           	llyt_other_perm.setVisibility(View.GONE) ;
                           	txt_other_perm.setVisibility(View.GONE) ;
                           }
                }    
            }*/
           
            
        } catch (Exception e) {
            Log.d("PermUtils", "Problem getting package info for " + pkgName, e);
        }
		  return view ;
	}
	
	private static ArrayList<PermissionInfo> extractPerms(PackageInfo info,
            PackageInfo installedPkgInfo,PackageManager pm) {
		ArrayList<PermissionInfo> arr = new ArrayList<PermissionInfo>() ;
        String[] strList = info.requestedPermissions;
        int[] flagsList = info.requestedPermissionsFlags;
        if ((strList == null) || (strList.length == 0)) {
            return arr;
        }
        
        for (int i=0; i<strList.length; i++) {
            String permName = strList[i];
            try {
                PermissionInfo tmpPermInfo = pm.getPermissionInfo(permName, 0);
                if (tmpPermInfo == null) {
                    continue;
                }
                int existingIndex = -1;
                if (installedPkgInfo != null
                        && installedPkgInfo.requestedPermissions != null) {
                    for (int j=0; j<installedPkgInfo.requestedPermissions.length; j++) {
                        if (permName.equals(installedPkgInfo.requestedPermissions[j])) {
                            existingIndex = j;
                            break;
                        }
                    }
                }
                final int existingFlags = existingIndex >= 0 ?
                        installedPkgInfo.requestedPermissionsFlags[existingIndex] : 0;
                        if (!isDisplayablePermission(tmpPermInfo, flagsList[i], existingFlags)) {
                            // This is not a permission that is interesting for the user
                            // to see, so skip it.
                            continue;
                        }
             
                final boolean newPerm = installedPkgInfo != null
                        && (existingFlags&PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0;
              if(newPerm){
            	  arr.add(tmpPermInfo) ;
              }
            } catch (NameNotFoundException e) {
               
            }
        }
        return arr ;
    }
	
	private static boolean isDisplayablePermission(PermissionInfo pInfo, int newReqFlags,
            int existingReqFlags) {
        final int base = pInfo.protectionLevel & PermissionInfo.PROTECTION_MASK_BASE;
        final boolean isNormal = (base == PermissionInfo.PROTECTION_NORMAL);

        // We do not show normal permissions in the UI.
        if (isNormal) {
            return false;
        }

        final boolean isDangerous = (base == PermissionInfo.PROTECTION_DANGEROUS)
                || ((pInfo.protectionLevel&PermissionInfo.PROTECTION_FLAG_PRE23) != 0);
        final boolean isRequired =
                ((newReqFlags&PackageInfo.REQUESTED_PERMISSION_REQUIRED) != 0);
        final boolean isDevelopment =
                ((pInfo.protectionLevel&PermissionInfo.PROTECTION_FLAG_DEVELOPMENT) != 0);
        final boolean wasGranted =
                ((existingReqFlags&PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0);
        final boolean isGranted =
                ((newReqFlags&PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0);

        // Dangerous and normal permissions are always shown to the user if the permission
        // is required, or it was previously granted
        if (isDangerous && (isRequired || wasGranted || isGranted)) {
            return true;
        }

        // Development permissions are only shown to the user if they are already
        // granted to the app -- if we are installing an app and they are not
        // already granted, they will not be granted as part of the install.
        if (isDevelopment && wasGranted) {
            
            return true;
        }
        return false;
    }
}

class MyOnCLickListener implements OnClickListener{
	View rootView ;
	TextView text ;
	Drawable openDrawable,closeDrawable ;
	boolean isOpen = true ;
	public MyOnCLickListener(View view,TextView t,Drawable open,Drawable close){
		rootView = view ;
		text = t ;
		openDrawable = open ;
		closeDrawable = close; 
				
	}

	@Override
	public void onClick(View v) {
		if(isOpen){
			isOpen =false ;
			rootView.setVisibility(View.GONE) ;
			text.setCompoundDrawablesRelative(null, null,
					closeDrawable, null);
		}else{
			isOpen =true ;
			rootView.setVisibility(View.VISIBLE) ;
			text.setCompoundDrawablesRelative(null, null,
					openDrawable, null);
		}
		
	}
}
