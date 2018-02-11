package com.mst.wallpaper.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.object.WallpaperImageInfo;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.WallpaperConfigUtil;
import com.mst.wallpaper.utils.WallpaperTimeUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.util.Log;


public class WallpaperDbController extends WallpaperDbHelper {

    private Context mContext;

    public WallpaperDbController(Context context) {
    	super(context);
        //super.openDb(context);
        mContext = context;
    }

    public void close() {
        super.closeDb();
    }

    public boolean insertWallpaperImage(WallpaperImageInfo info) {
        long id = 0;
        ContentValues values = new ContentValues();
        values.put(WallpaperDbColumns.WallpaperColumns.IDENTIFY, info.getIdentify());
        values.put(WallpaperDbColumns.WallpaperColumns.PATH, String.valueOf(info.getBigIcon()));
        values.put(WallpaperDbColumns.WallpaperColumns.BELONG_GROUP, info.getBelongGroup());
        if (mSqlDb.isOpen()) {
            id = mSqlDb.insert(WallpaperDbColumns.KEYGUARD_WALLPAPER_TABLE_NAME, null, values);
        }
        return id > 0 ? true : false;
    }

    public boolean insertWallpaper(Wallpaper wallpaper, boolean isSystem) {
        long id = 0;
        ContentValues values = new ContentValues();
        values.put(WallpaperDbColumns.GroupColumns.DISPLAY_NAME, wallpaper.name);
        values.put(WallpaperDbColumns.GroupColumns.COUNT, wallpaper.count);
        
        values.put(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_COLOR, wallpaper.themeColor);
        if (WallpaperConfigUtil.getSwithFlag()) {
			wallpaper.isDefaultTheme = (1);
		}
        values.put(WallpaperDbColumns.GroupColumns.IS_DEFAULT_THEME, wallpaper.isDefaultTheme);
        values.put(WallpaperDbColumns.GroupColumns.IS_TIME_BLACK, wallpaper.isTimeBlack);
        values.put(WallpaperDbColumns.GroupColumns.IS_STATUSBAR_BLACK, wallpaper.isStatusBarBlack);
        values.put(WallpaperDbColumns.GroupColumns.IS_FROM_THEME, wallpaper.isFromTheme);
        
        if (isSystem) {
            values.put(WallpaperDbColumns.GroupColumns.SYSTEM_FLAG, 1);
        }
        if (mSqlDb.isOpen()) {
            id = mSqlDb.insert(WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME, null, values);
        }
        return id > 0 ? true : false;
    }

    public boolean deleteWallpaperImageByPath(String path) {
        long bool = 0;
        if (mSqlDb.isOpen()) {
            bool = mSqlDb.delete(WallpaperDbColumns.KEYGUARD_WALLPAPER_TABLE_NAME, WallpaperDbColumns.WallpaperColumns.PATH + "=?",
                    new String[] {path});
        }
        return bool > 0 ? true : false;
    }

    public boolean deleteWallpaperById(int id) {
        long bool = 0;
        if (mSqlDb.isOpen()) {
            bool = mSqlDb.delete(WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME, WallpaperDbColumns.GroupColumns.ID
                    + "=?", new String[] {id + ""});
        }
        return bool > 0 ? true : false;
    }

    public boolean deleteWallpaperByName(String name) {
        long bool = 0;
        if (mSqlDb.isOpen()) {
            bool = mSqlDb.delete(WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME,
                    WallpaperDbColumns.GroupColumns.DISPLAY_NAME + "=?", new String[] {name});
        }
        return bool > 0 ? true : false;
    }

    public List<Wallpaper> queryAllKeyguardWallpapers() {
        List<Wallpaper> list = new ArrayList<Wallpaper>();
        Cursor c = mSqlDb.query(WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME, null, null, null, null, null,
                null);
        while (c.moveToNext()) {
            Wallpaper wallpaper = new Wallpaper();
            wallpaper.id = (c.getInt(WallpaperDbColumns.GroupColumns.ID_INDEX));
            wallpaper.name = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_INDEX));
            wallpaper.count = (c.getInt(WallpaperDbColumns.GroupColumns.COUNT_INDEX));
            wallpaper.systemFlag = (c.getInt(WallpaperDbColumns.GroupColumns.SYSTEM_FLAG_INDEX));
            
            wallpaper.themeColor = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_COLOR_INDEX));
            wallpaper.isDefaultTheme = (c.getInt(WallpaperDbColumns.GroupColumns.IS_DEFAULT_THEME_INDEX));
            wallpaper.isTimeBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_TIME_BLACK_INDEX));
            wallpaper.isStatusBarBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_STATUSBAR_BLACK_INDEX));
            wallpaper.isFromTheme = (c.getInt(WallpaperDbColumns.GroupColumns.IS_FROM_THEME_INDEX));
            list.add(wallpaper);
        }
        c.close();
        return list;
    }
    
    public Wallpaper queryKeyguardWallpaperById(int id) {
        Wallpaper wallpaper = new Wallpaper();
        Cursor c = mSqlDb.query(WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME, null,
                WallpaperDbColumns.GroupColumns.ID + "=?", new String[] {String.valueOf(id)}, null, null, null);
        while (c.moveToNext()) {
            wallpaper.id = (c.getInt(WallpaperDbColumns.GroupColumns.ID_INDEX));
            wallpaper.name = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_INDEX));
            wallpaper.count = (c.getInt(WallpaperDbColumns.GroupColumns.COUNT_INDEX));
            wallpaper.systemFlag = (c.getInt(WallpaperDbColumns.GroupColumns.SYSTEM_FLAG_INDEX));
            
            wallpaper.themeColor = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_COLOR_INDEX));
            wallpaper.isDefaultTheme = (c.getInt(WallpaperDbColumns.GroupColumns.IS_DEFAULT_THEME_INDEX));
            wallpaper.isTimeBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_TIME_BLACK_INDEX));
            wallpaper.isStatusBarBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_STATUSBAR_BLACK_INDEX));
            wallpaper.isFromTheme = (c.getInt(WallpaperDbColumns.GroupColumns.IS_FROM_THEME_INDEX));
        }
        c.close();
        return wallpaper;
    }
    

    public List<Wallpaper> queryAllWallpapersOrderBy() {
        List<Wallpaper> list = new ArrayList<Wallpaper>();
        Cursor c = mSqlDb.query(WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME, null, null, null, null, null,
                WallpaperDbColumns.GroupColumns.SYSTEM_FLAG + " desc, " + WallpaperDbColumns.GroupColumns.ID + " desc");
        String[] names = c.getColumnNames();
        for(String n:names){
        	Log.d("nn", n);
        }
        while (c.moveToNext()) {
            Wallpaper wallpaper = new Wallpaper();
            wallpaper.id = (c.getInt(WallpaperDbColumns.GroupColumns.ID_INDEX));
            wallpaper.name = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_INDEX));
            wallpaper.count = (c.getInt(WallpaperDbColumns.GroupColumns.COUNT_INDEX));
            wallpaper.systemFlag = (c.getInt(WallpaperDbColumns.GroupColumns.SYSTEM_FLAG_INDEX));
            
            wallpaper.themeColor = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_COLOR_INDEX));
            wallpaper.isDefaultTheme = (c.getInt(WallpaperDbColumns.GroupColumns.IS_DEFAULT_THEME_INDEX));
            wallpaper.isTimeBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_TIME_BLACK_INDEX));
            wallpaper.isStatusBarBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_STATUSBAR_BLACK_INDEX));
            wallpaper.isFromTheme = (c.getInt(WallpaperDbColumns.GroupColumns.IS_FROM_THEME_INDEX));
			list.add(wallpaper);
        }
        c.close();
        
        
        return list;
    }

    public List<WallpaperImageInfo> queryAllImagesByWallpaperId(int wallpaperId) {
        List<WallpaperImageInfo> list = new ArrayList<WallpaperImageInfo>();
        Cursor c = mSqlDb
                .query(WallpaperDbColumns.KEYGUARD_WALLPAPER_TABLE_NAME, null, WallpaperDbColumns.WallpaperColumns.BELONG_GROUP
                        + "=?", new String[] {wallpaperId + ""}, null, null, null);
        while (c.moveToNext()) {
            WallpaperImageInfo pictureInfo = new WallpaperImageInfo();
            pictureInfo.setId(c.getInt(WallpaperDbColumns.WallpaperColumns.ID_INDEX));
            pictureInfo.setIdentify(c.getString(WallpaperDbColumns.WallpaperColumns.IDENTIFY_INDEX));
            pictureInfo.setBigIcon(c.getString(WallpaperDbColumns.WallpaperColumns.PATH_INDEX));
            pictureInfo.setBelongGroup(c.getInt(WallpaperDbColumns.WallpaperColumns.BELONG_GROUP_INDEX));
            list.add(pictureInfo);
        }
//        Collections.sort(list, new WallpaperTimeUtils.WallpaperImageInfoComparator());
        c.close();
        return list;
    }

    public Wallpaper queryKeyguardWallpaperByName(String name) {
        Wallpaper wallpaper = new Wallpaper();
        Cursor c = mSqlDb.query(WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME, null,
                WallpaperDbColumns.GroupColumns.DISPLAY_NAME + "=?", new String[] {name}, null, null, null);
        while (c.moveToNext()) {
            wallpaper.id = (c.getInt(WallpaperDbColumns.GroupColumns.ID_INDEX));
            wallpaper.name = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_INDEX));
            wallpaper.count = (c.getInt(WallpaperDbColumns.GroupColumns.COUNT_INDEX));
            wallpaper.systemFlag = (c.getInt(WallpaperDbColumns.GroupColumns.SYSTEM_FLAG_INDEX));
            
            wallpaper.themeColor = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_COLOR_INDEX));
            wallpaper.isDefaultTheme = (c.getInt(WallpaperDbColumns.GroupColumns.IS_DEFAULT_THEME_INDEX));
            wallpaper.isTimeBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_TIME_BLACK_INDEX));
            wallpaper.isStatusBarBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_STATUSBAR_BLACK_INDEX));
            wallpaper.isFromTheme = (c.getInt(WallpaperDbColumns.GroupColumns.IS_FROM_THEME_INDEX));
        }
        c.close();
        return wallpaper;
    }
    
    
    

    public List<List<WallpaperImageInfo>> queryAllWallpaperImageInfos() {
        List<List<WallpaperImageInfo>> lists = new ArrayList<List<WallpaperImageInfo>>();
        List<Wallpaper> wallpapers = queryAllKeyguardWallpapers();
        for (int i = 0; i < wallpapers.size(); i++) {
            List<WallpaperImageInfo> pictureInfos = queryAllImagesByWallpaperId((int)wallpapers.get(i).id);
            lists.add(pictureInfos);
        }
        return lists;
    }

    public List<List<WallpaperImageInfo>> queryAllWallpaperImageInfosOrderBy() {
        List<List<WallpaperImageInfo>> lists = new ArrayList<List<WallpaperImageInfo>>();
        List<Wallpaper> wallpapers = queryAllWallpapersOrderBy();
        for (int i = 0; i < wallpapers.size(); i++) {
            List<WallpaperImageInfo> pictureInfos = queryAllImagesByWallpaperId((int)wallpapers.get(i).id);
            lists.add(pictureInfos);
        }
        return lists;
    }

    public List<Wallpaper> queryAllSdcardWallpaperInfos() {
        List<Wallpaper> lists = new ArrayList<Wallpaper>();
        Cursor c = mSqlDb.query(WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME, null,
                WallpaperDbColumns.GroupColumns.SYSTEM_FLAG + "=?", new String[] {0 + ""}, null, null, null, null);
        while (c.moveToNext()) {
            Wallpaper wallpaper = new Wallpaper();
            wallpaper.id = (c.getInt(WallpaperDbColumns.GroupColumns.ID_INDEX));
            wallpaper.name = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_INDEX));
            wallpaper.count = (c.getInt(WallpaperDbColumns.GroupColumns.COUNT_INDEX));
            wallpaper.systemFlag = (c.getInt(WallpaperDbColumns.GroupColumns.SYSTEM_FLAG_INDEX));
            
            wallpaper.themeColor = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_COLOR_INDEX));
            wallpaper.isDefaultTheme = (c.getInt(WallpaperDbColumns.GroupColumns.IS_DEFAULT_THEME_INDEX));
            wallpaper.isTimeBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_TIME_BLACK_INDEX));
            wallpaper.isStatusBarBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_STATUSBAR_BLACK_INDEX));
            wallpaper.isFromTheme = (c.getInt(WallpaperDbColumns.GroupColumns.IS_FROM_THEME_INDEX));
            
            lists.add(wallpaper);
        }
        c.close();
        return lists;
    }

    public boolean deleteAllSdcardWallpaperInfos() {
        long bool = mSqlDb.delete(WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME,
                WallpaperDbColumns.GroupColumns.SYSTEM_FLAG + "=?", new String[] {0 + ""});
        return bool > 0;
    }
    
    public Wallpaper queryDefaultKeyguardWallpaper() {
        Wallpaper wallpaper = new Wallpaper();
        Cursor c = mSqlDb.query(WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME, null,
                WallpaperDbColumns.GroupColumns.IS_DEFAULT_THEME + "=?", new String[] {1 + ""}, null, null, null);
        
        if (c == null || c.getCount() <= 0) return null;
		
        while (c.moveToNext()) {
            wallpaper.id = (c.getInt(WallpaperDbColumns.GroupColumns.ID_INDEX));
            wallpaper.name = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_INDEX));
            wallpaper.count = (c.getInt(WallpaperDbColumns.GroupColumns.COUNT_INDEX));
            wallpaper.systemFlag = (c.getInt(WallpaperDbColumns.GroupColumns.SYSTEM_FLAG_INDEX));
            wallpaper.themeColor = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_COLOR_INDEX));
            wallpaper.isDefaultTheme = (c.getInt(WallpaperDbColumns.GroupColumns.IS_DEFAULT_THEME_INDEX));
            wallpaper.isTimeBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_TIME_BLACK_INDEX));
            wallpaper.isStatusBarBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_STATUSBAR_BLACK_INDEX));
            wallpaper.isFromTheme = (c.getInt(WallpaperDbColumns.GroupColumns.IS_FROM_THEME_INDEX));
        }
        c.close();
        return wallpaper;
    }
    
    public void updateNextDayDB(String insertPath, String deletePath) {
    	List<Wallpaper> wallpapers = queryAllKeyguardWallpapers();
    	for (int i = 0; i < wallpapers.size(); i++) {
            if (wallpapers.get(i).name.equals("NextDay")) {
                boolean b = deleteWallpaperImageByPath(deletePath);
                
                long id = 0;
                ContentValues values = new ContentValues();
                String identify = insertPath.replace(Config.WallpaperStored.NEXTDAY_WALLPAPER_PATH + "NextDay" + File.separator, "").replace(".jpg", "");
                values.put(WallpaperDbColumns.WallpaperColumns.IDENTIFY, identify);
                values.put(WallpaperDbColumns.WallpaperColumns.PATH, insertPath);
                values.put(WallpaperDbColumns.WallpaperColumns.BELONG_GROUP, wallpapers.get(i).id);
                if (mSqlDb.isOpen()) {
                    id = mSqlDb.insert(WallpaperDbColumns.KEYGUARD_WALLPAPER_TABLE_NAME, null, values);
                }
                
                
                break;
            }
        }
    }
    
    public boolean insertImages(ContentValues values) {
        long id = 0;
        if (mSqlDb.isOpen()) {
            id = mSqlDb.insert(WallpaperDbColumns.KEYGUARD_WALLPAPER_TABLE_NAME, null, values);
        }
        return id > 0 ? true : false;
    }
    
}
