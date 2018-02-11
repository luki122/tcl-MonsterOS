package com.mst.wallpaper.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import mst.utils.DisplayUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import com.mst.wallpaper.WallpaperApplication;
import com.mst.wallpaper.db.WallpaperDbController;
import com.mst.wallpaper.db.SharePreference;
import com.mst.wallpaper.db.WallpaperDbColumns;
import com.mst.wallpaper.object.WallpaperImageInfo;
import com.mst.wallpaper.object.WallpaperThemeInfo;
import com.mst.wallpaper.object.Wallpaper;

import android.R.integer;
import android.R.string;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.util.Xml;

public class WallpaperConfigUtil {
	private static String mConfigVersion;
	
	private static boolean mSwithFlag = false;
	private static String mDestPath = null;
	

	
	public static String getConfigVersion() {
		String filePath = Config.WallpaperStored.DEFAULT_SYSTEM_KEYGUARD_WALLPAPER_PATH 
				+ Config.WallpaperStored.WALLPAPER_SET_FILE;
		parseFile(filePath, null);
		
		return mConfigVersion;
	}

	public static WallpaperThemeInfo parseWallpaperThemeByName(Context mContext, String themeName) {
		String filePath = themeName + File.separator + Config.WallpaperStored.KEYGUARD_SET_FILE;
		WallpaperThemeInfo mThemeInfo = new WallpaperThemeInfo();
		parseFile(filePath, mThemeInfo);
		
		return mThemeInfo;
	}
	
	public static void parseFile(String filePath, WallpaperThemeInfo themeInfo) {
		File mFile = new File(filePath);

        XmlPullParser parser = Xml.newPullParser();
        //Log.d("parse", "file:"+filePath);
		try {
			InputStream inputStream = new FileInputStream(mFile);
			parser.setInput(inputStream, "utf-8");
			int eventType = parser.getEventType();
			
			while (XmlPullParser.END_DOCUMENT != eventType) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					
					break;
					
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					if ("wallpaper".equals(tagName)) {
						if (filePath.contains(Config.WallpaperStored.WALLPAPER_SET_FILE)) {
							mConfigVersion = parser.getAttributeValue(0);
							break;
						}
						
					} else if ("lockpaper".equals(tagName)) {
						if (themeInfo != null) {
							themeInfo.name = parser.getAttributeValue(0);
							themeInfo.isDefault = parser.getAttributeValue(1);
						}
						
					} else if ("title_color".equals(tagName)) {
						if (themeInfo != null) {
							themeInfo.nameColor = parser.nextText();
						}
						
					} else if ("time_black".equals(tagName)) {
						if (themeInfo != null) {
							themeInfo.timeBlack = parser.nextText();
						}
						
					} else if ("statusbar_black".equals(tagName)) {
						if (themeInfo != null) {
							themeInfo.statusBarBlack = parser.nextText();
						}
						
					}
					break;
					
				case XmlPullParser.END_TAG:
					String endName = parser.getName();
					
					break;
				}
				eventType = parser.next();
			}
			
		} catch (Exception e) {
		}
	}
	
	public static void setSwithFlag(boolean value) {
		mSwithFlag = value;
	}
	
	public static void setDestinationPath(String path) {
		mDestPath = path;
	}
	
	public static boolean getSwithFlag() {
		return mSwithFlag;
	}
	
	public static String getDestinationPath() {
		return mDestPath;
	}
	
	public static void updateSystemDefaultWallpaper(Context mContext){
        String current_group = SharePreference.getStringPreference(mContext, Config.WallpaperStored.CURRENT_KEYGUARD_WALLPAPER, null);
        if (current_group == null) {
        	WallpaperDbController mDbControl = new WallpaperDbController(mContext);
        	Wallpaper wallpaper = mDbControl.queryDefaultKeyguardWallpaper();
        	if (wallpaper != null) {
        		current_group = wallpaper.name;
			} else {
				current_group = Config.WallpaperStored.DEFAULT_KEYGUARD_GROUP;
			}
        	mDbControl.close();
		}
        
        String currentPath = WallpaperTimeUtils.getCurrentKeyguardWallpaperPaperPath(mContext, current_group);
        
        FileUtils.copyFile(currentPath, Config.WallpaperStored.KEYGUARD_WALLPAPER_PATH, mContext);
    }

    public static String creatWallpaperConfigurationXmlFile(String filePath, WallpaperThemeInfo mThemeInfo) {
    	File myFile = new File(filePath);
		
    	try {
    		FileOutputStream fos = new FileOutputStream(myFile);
        	
        	XmlSerializer serializer = Xml.newSerializer();
        	
        	serializer.setOutput(fos, "UTF-8");
        	
    		// <?xml version=”1.0″ encoding=”UTF-8″ standalone=”yes”?>
    		serializer.startDocument("UTF-8", true);
    		
    		// <wallpaper>
    		serializer.startTag("", "wallpaper");
    		
    		// <lockpaper name="name", default="defaultValue">
    		serializer.startTag("", "lockpaper");
    		serializer.attribute("", "name", mThemeInfo.name);
    		serializer.attribute("", "default", mThemeInfo.isDefault);
    		
    		// <title_color>titleColor</title_color>
    		serializer.startTag("", "title_color");
    		serializer.text(mThemeInfo.nameColor);
    		serializer.endTag("", "title_color");
    		
    		// <time_black>timeBlack</time_black>
    		serializer.startTag("", "time_black");
    		serializer.text(mThemeInfo.timeBlack);
    		serializer.endTag("", "time_black");
    		
    		// <statusbar_black>statusbarBlack</statusbar_black>
    		serializer.startTag("", "statusbar_black");
    		serializer.text(mThemeInfo.statusBarBlack);
    		serializer.endTag("", "statusbar_black");
    		
    		// </lockpaper>
    		serializer.endTag("", "lockpaper");
    		    		
    		// </wallpaper>
    		serializer.endTag("","wallpaper");
    		serializer.endDocument();
    		
    		fos.flush();
    		fos.close();
    		
//    		return writer.toString();
    		
    		
    	} catch(Exception e) {
    		
    	}
    	return filePath;
    }
    
	
	

	
	
}
