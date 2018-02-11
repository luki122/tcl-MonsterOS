package com.android.packageinstaller.adplugin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tmsdk.common.module.qscanner.QScanAdPluginEntity;
import tmsdk.common.module.qscanner.QScanResultEntity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.android.packageinstaller.adplugin.MulwareProvider.MulwareTable;

public class ScannerUtils {
	//扫描广告添加到数据库
			public static void addOrUpdateAdInfo(Context context,QScanResultEntity result) {
				//广告信息
				if (result.plugins != null) {
					ArrayList<QScanAdPluginEntity> plugins = result.plugins;
					if (plugins.size() > 0) {
						List<String> ipList = new LinkedList<String>();
						List<String> urlList = new LinkedList<String>();
						for (QScanAdPluginEntity n : plugins) {
							if(n.banIps!=null && n.banIps.size()>0){
								for(String ip:n.banIps){
									if(!ipList.contains(ip)){
										ipList.add(ip);
									}
								}
							}
							if(n.banUrls!=null && n.banUrls.size()>0){
								for(String url:n.banUrls){
									if(!urlList.contains(url)){
										urlList.add(url);
									}
								}
							}
						}
						ContentValues values = new ContentValues();
						setContentValues(values, ipList, urlList, result.packageName, plugins.size());
						ContentResolver contentResolver = context.getContentResolver();
						Cursor cursor = contentResolver.query(MulwareTable.CONTENT_URI, new String[]{MulwareTable.AD_PACKAGENAME}, MulwareTable.AD_PACKAGENAME+"=?", new String[]{result.packageName}, null);
						if(cursor != null && cursor.getCount()>0 && cursor.moveToFirst()){
							int i = contentResolver.update(MulwareTable.CONTENT_URI, values, MulwareTable.AD_PACKAGENAME+"=?", new String[]{result.packageName});
							//Toast.makeText(context, "i = " + i,Toast.LENGTH_LONG).show() ;
							Log.d("ScannerUtils", "i = " + i) ;
						}else{
							Uri uri = contentResolver.insert(MulwareTable.CONTENT_URI, values);
							//Toast.makeText(context, "uri = " + uri.toString(),Toast.LENGTH_LONG).show() ;
							Log.d("ScannerUtils","uri = " + uri.toString()) ;
						}				
					}
				}
			}	
			
			/**
			 * 检测结果转数据库数据
			 * @param values
			 * @param ipList
			 * @param urlList
			 * @param packageName
			 * @param count
			 */
			private static void setContentValues(ContentValues values, List<String> ipList, List<String> urlList, String packageName, int count){
				values.put(MulwareTable.AD_PACKAGENAME, packageName);
				values.put(MulwareTable.AD_COUNT, count);
				values.put(MulwareTable.AD_BANIPS, listToString(ipList));
				values.put(MulwareTable.AD_BANURLS, listToString(urlList));
			}
			
			/**
			 * 列表转字符串
			 * @param array
			 * @return
			 */
			private static String listToString(List<String> array){
				StringBuilder returnString = new StringBuilder();
				if(array!=null && array.size()>0){
					for(String string:array){
						returnString.append(string).append(MulwareProvider.SPLIT);
					}
					if(returnString.length()>0){
						returnString.replace(returnString.length()-1, returnString.length(), "");
					}
				}
				return returnString.toString();
			}
}
