package com.mst.wallpaper.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.mst.wallpaper.imageutils.DiskLruCache;
import com.mst.wallpaper.imageutils.ImageCache;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.object.WallpaperImageInfo;
import com.mst.wallpaper.object.WallpaperThemeInfo;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.WallpaperConfigUtil;

import android.os.SystemProperties;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore.Files;
import android.util.Log;
import com.mst.wallpaper.R;
public class WallpaperDbHelper {
    private static final String TAG = "DbHelper";

    public DatabaseHelper mDBHelper;
    public SQLiteDatabase mSqlDb;
    
    public WallpaperDbHelper(Context context) {
    	openDb(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private Context mContext;
		private boolean isUpdate = false;
		private String mType;
		
        public DatabaseHelper(Context context) {
            super(context, WallpaperDbColumns.DATABASE_NAME, null, WallpaperDbColumns.DATABASE_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME + " ("
                    + WallpaperDbColumns.GroupColumns.ID + " INTEGER PRIMARY KEY,"
                    + WallpaperDbColumns.GroupColumns.DISPLAY_NAME + " TEXT UNIQUE, "
                    + WallpaperDbColumns.GroupColumns.COUNT + " INTEGER NOT NULL, "
                    + WallpaperDbColumns.GroupColumns.SYSTEM_FLAG + " INTEGER DEFAULT 0, "
                    + WallpaperDbColumns.GroupColumns.REMARK + " TEXT, "
                    
                    + WallpaperDbColumns.GroupColumns.DISPLAY_NAME_COLOR + " TEXT,"
                    + WallpaperDbColumns.GroupColumns.IS_DEFAULT_THEME + " INTEGER DEFAULT 0,"
                    + WallpaperDbColumns.GroupColumns.IS_TIME_BLACK + " INTEGER DEFAULT 0,"
                    + WallpaperDbColumns.GroupColumns.IS_STATUSBAR_BLACK + " INTEGER DEFAULT 0"
                    + ");");
            
            db.execSQL("CREATE TABLE " + WallpaperDbColumns.KEYGUARD_WALLPAPER_TABLE_NAME + " ("
                    + WallpaperDbColumns.WallpaperColumns.ID + " INTEGER PRIMARY KEY,"
                    + WallpaperDbColumns.WallpaperColumns.BLACK_WIDGET+" INTEGER,"
                    + WallpaperDbColumns.WallpaperColumns.IDENTIFY + " TEXT, " + WallpaperDbColumns.WallpaperColumns.PATH
                    + " TEXT UNIQUE NOT NULL, " + WallpaperDbColumns.WallpaperColumns.BELONG_GROUP + " INTEGER NOT NULL, "
                    + WallpaperDbColumns.WallpaperColumns.REMARK + " TEXT, " + "FOREIGN KEY" + "("
                    + WallpaperDbColumns.WallpaperColumns.BELONG_GROUP + ") REFERENCES "
                    + WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME + "(" + WallpaperDbColumns.GroupColumns.ID + ")"
                    + ");");
            
            if (!isUpdate) {
            	db.execSQL("CREATE TABLE " + WallpaperDbColumns.DESKTOP_WALLPAPER_TABLE_NAME + " ("
                        + Config.WallpaperStored.WALLPAPER_ID + " INTEGER PRIMARY KEY,"
                        + Config.WallpaperStored.WALLPAPER_MODIFIED + " INTEGER, "
                        + Config.WallpaperStored.WALLPAPER_BLACK_WIDGET+" INTEGER,"
                        + Config.WallpaperStored.WALLPAPER_OLDPATH + " TEXT UNIQUE NOT NULL, " 
                        + Config.WallpaperStored.WALLPAPER_FILENAME
                        + " TEXT UNIQUE NOT NULL" + ");");
			}
            
            // 创建插入触发器
            db.execSQL("CREATE TRIGGER " + WallpaperDbColumns.FK_INSERT_GROUP + " BEFORE INSERT " + " ON "
                    + WallpaperDbColumns.KEYGUARD_WALLPAPER_TABLE_NAME + " FOR EACH ROW BEGIN"
                    + " SELECT CASE WHEN ((SELECT " + WallpaperDbColumns.GroupColumns.ID + " FROM "
                    + WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME + " WHERE " + WallpaperDbColumns.GroupColumns.ID
                    + "=" + "NEW." + WallpaperDbColumns.WallpaperColumns.BELONG_GROUP + " ) IS NULL)"
                    + " THEN RAISE (ABORT,'Foreign Key Violation') END;" + "  END;");

            // 创建删除组触发器
            db.execSQL("CREATE TRIGGER " + WallpaperDbColumns.FK_DELETE_GROUP + " BEFORE DELETE " + "ON "
                    + WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME + " FOR EACH ROW BEGIN" + " DELETE FROM "
                    + WallpaperDbColumns.KEYGUARD_WALLPAPER_TABLE_NAME + " WHERE "
                    + WallpaperDbColumns.WallpaperColumns.BELONG_GROUP + "=OLD." + WallpaperDbColumns.GroupColumns.ID + ";"
                    + " END;");
            
            boolean isFirst = SharePreference.getBooleanPreference(mContext, Config.IS_FIRST_TIME_START, true);
            
            String version = WallpaperConfigUtil.getConfigVersion();
            if (version != null) {
            	SharePreference.setStringPreference(mContext, Config.WallpaperStored.WALLPAPER_VERSION, version);
			}
            
            if (isFirst) {
            	insertSystemLockWallpapers(db);
            } else if (isUpdate) {
            	if (WallpaperConfigUtil.getSwithFlag() && WallpaperConfigUtil.getDestinationPath() != null) {
            		refreshDbFromPath(WallpaperConfigUtil.getDestinationPath(), db);
            		WallpaperConfigUtil.setSwithFlag(false);
            		WallpaperConfigUtil.setDestinationPath(null);
            	} else {
	            	insertSystemLockWallpapers(db);
	            	findSdCardLockWallpapers(db);
            	}
            	isUpdate = false;
            	try {
            		File cacheFile = ImageCache.getDiskCacheDir(mContext, Config.KEYGUARD_WALLPAPER_CACHE_DIR);
            		if (cacheFile != null && cacheFile.exists()) {
            			DiskLruCache.deleteContents(cacheFile);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        	isUpdate = true;
            db.execSQL("DROP TABLE IF EXISTS " + WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + WallpaperDbColumns.KEYGUARD_WALLPAPER_TABLE_NAME);
            db.execSQL("DROP TRIGGER IF EXISTS " + WallpaperDbColumns.FK_INSERT_GROUP);
            db.execSQL("DROP TRIGGER IF EXISTS " + WallpaperDbColumns.FK_DELETE_GROUP);
            onCreate(db);
        }
        
        private void insertSystemLockWallpapers(SQLiteDatabase db) {
        	try {
                mType = Config.WallpaperStored.WALLPAPER_KEYGUARD_TYPE;
                File file = new File(Config.WallpaperStored.DEFAULT_SYSTEM_KEYGUARD_WALLPAPER_PATH);
                getFile(db, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
    	}
        
        private void findSdCardLockWallpapers(SQLiteDatabase db){
            try {
                File file = new File(Config.WallpaperStored.DEFAULT_SDCARD_KEYGUARD_WALLPAPER_PATH);
                getFile(db, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        private void getFile(SQLiteDatabase db, File file) {
            if (!file.exists()) {
                return;
            }
            if (file.isFile()) {
                String fileName = file.getName();
                if (isImageFile(file)) {
                    List<Wallpaper> wallpapers = queryAllWallpaperInfos(db);
                    for (int i = 0; i < wallpapers.size(); i++) {
                        if (wallpapers.get(i).name.equals(file.getParentFile().getName())) {
                        	WallpaperImageInfo info = new WallpaperImageInfo();
                            String[] nameStrings = file.getName().split("\\.");
                            info.setIdentify(nameStrings[0]);
                            info.setBigIcon(file.getPath());
                            info.setBelongGroup(wallpapers.get(i).id);
                            insertPicture(db, info);
                        }
                    }
                }

            } else {
                File[] files = file.listFiles();
                
                if (file.isDirectory()) {
					File[] mFiles = file.listFiles();
					for (File mFile : mFiles) {
						if (mFile.getName().contains(Config.WallpaperStored.DEFAULT_KEYGUARD_FILE_NAME)) {
							Arrays.sort(files, new Comparator<File>() {
								@Override
								public int compare(File file1, File file2) {
									// TODO Auto-generated method stub
									return Long.valueOf(file1.lastModified()).compareTo(file2.lastModified());
								}
							});
							break;
						}
					}
				}

                String tempName = file.getParentFile().getName();
                //add mType.equals(file.getParentFile().getName().replace(".", "")) for NextDay
                if (null != files && (mType.equals(file.getParentFile().getName()) || mType.equals(tempName.replace(".", ""))) && files.length > 0) {
                    boolean isSystem = false;
                    Wallpaper wallpaper = new Wallpaper();
                    wallpaper.name = file.getName();
                    wallpaper.count = files.length;
                    for (File mFile : files) {
						if (mFile.getName().contains(".xml")) {
							wallpaper.count = files.length - 1;
							break;
						}
					}
                    WallpaperThemeInfo mThemeInfo = WallpaperConfigUtil.parseWallpaperThemeByName(mContext, file.toString());
                    if (mThemeInfo == null) {
                    	mThemeInfo = new WallpaperThemeInfo();
					}
                    wallpaper.themeColor = (mThemeInfo.nameColor);
                    wallpaper.isDefaultTheme = ("false".equals(mThemeInfo.isDefault)? 0 : 1);
                    wallpaper.isTimeBlack = ("false".equals(mThemeInfo.timeBlack)? 0 : 1);
                    wallpaper.isStatusBarBlack = ("false".equals(mThemeInfo.statusBarBlack)? 0 : 1);
                    
                    if (wallpaper.isDefaultTheme == 1) {
                    	SharePreference.setStringPreference(mContext, Config.WallpaperStored.CURRENT_KEYGUARD_WALLPAPER, mThemeInfo.name);
                    	if (wallpaper.isTimeBlack == 0) {
    						SharePreference.setStringPreference(mContext, Config.WallpaperStored.CURRENT_KEYGUARD_GROUP_TIME_BLACK, "false");
    					} else {
    						SharePreference.setStringPreference(mContext, Config.WallpaperStored.CURRENT_KEYGUARD_GROUP_TIME_BLACK, "true");
    					}
    					if (wallpaper.isStatusBarBlack == 0) {
    						SharePreference.setStringPreference(mContext, Config.WallpaperStored.CURRENT_KEYGUARD_GROUP_STATUS_BLACK, "false");
    					} else {
    						SharePreference.setStringPreference(mContext, Config.WallpaperStored.CURRENT_KEYGUARD_GROUP_STATUS_BLACK, "true");
    					}
					}
                    
                    //add for wallpaper NextDay
                    if (file.getPath().contains(Config.WallpaperStored.DEFAULT_SYSTEM_KEYGUARD_WALLPAPER_FLAG) || 
                    		file.getPath().contains("." + Config.WallpaperStored.WALLPAPER_KEYGUARD_TYPE)) {
                        isSystem = true;
                    }
                    insertWallpaper(db, wallpaper, isSystem);
                }
                
                if (files != null) {
                    for (File sFile : files) {
                    	getFile(db, sFile);
                    }
                }
            }
        }
        
        private boolean isImageFile(File file) {
            String filecode = "";
            boolean isPicture = false;
            try {
                FileInputStream inputStream = new FileInputStream(file);
                byte[] buffer = new byte[2];
                if (inputStream.read(buffer) != -1) {
                    for (int i = 0; i < buffer.length; i++) {
                        filecode += Integer.toString((buffer[i] & 0xFF));
                    }
                    switch (Integer.parseInt(filecode)) {
                        case 255216: // fileType = "jpg";
                            isPicture = true;
                            break;
                        case 7173: // fileType = "gif";
                            isPicture = true;
                            break;
                        case 6677: // fileType = "bmp";
                            isPicture = true;
                            break;
                        case 13780: // fileType = "png";
                            isPicture = true;
                            break;
                        default:
                            isPicture = false;
                    }

                }
                inputStream.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return isPicture;
        }
        
        private List<Wallpaper> queryAllWallpaperInfos(SQLiteDatabase db) {
            List<Wallpaper> list = new ArrayList<Wallpaper>();
            Cursor c = db.query(WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME, null, null, null, null, null,
                    null);
            while (c.moveToNext()) {
                Wallpaper wallpaper = new Wallpaper();
                wallpaper.id = (c.getInt(WallpaperDbColumns.GroupColumns.ID_INDEX));
                wallpaper.name = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_INDEX));
                wallpaper.count = (c.getInt(WallpaperDbColumns.GroupColumns.COUNT_INDEX));
                wallpaper.systemFlag = (c.getInt(WallpaperDbColumns.GroupColumns.SYSTEM_FLAG_INDEX));
                
                wallpaper.themeColor = (c.getString(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_COLOR_INDEX));
                wallpaper.isDefaultTheme=(c.getInt(WallpaperDbColumns.GroupColumns.IS_DEFAULT_THEME_INDEX));
                wallpaper.isTimeBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_TIME_BLACK_INDEX));
                wallpaper.isStatusBarBlack = (c.getInt(WallpaperDbColumns.GroupColumns.IS_STATUSBAR_BLACK_INDEX));
                
                list.add(wallpaper);
            }
            c.close();
            return list;
        }
        
        private boolean insertPicture(SQLiteDatabase db, WallpaperImageInfo info) {
            long id = 0;
            ContentValues values = new ContentValues();
            values.put(WallpaperDbColumns.WallpaperColumns.IDENTIFY, info.getIdentify());
            values.put(WallpaperDbColumns.WallpaperColumns.PATH, String.valueOf(info.getBigIcon()));
            values.put(WallpaperDbColumns.WallpaperColumns.BELONG_GROUP, info.getBelongGroup());
            if (db.isOpen()) {
                id = db.insert(WallpaperDbColumns.KEYGUARD_WALLPAPER_TABLE_NAME, null, values);
            }
            return id > 0 ? true : false;
        }
        
        private boolean insertWallpaper(SQLiteDatabase db, Wallpaper wallpaper, boolean isSystem) {
            long id = 0;
            ContentValues values = new ContentValues();
            values.put(WallpaperDbColumns.GroupColumns.DISPLAY_NAME, wallpaper.name);
            values.put(WallpaperDbColumns.GroupColumns.COUNT, wallpaper.count);
            
            values.put(WallpaperDbColumns.GroupColumns.DISPLAY_NAME_COLOR,wallpaper.themeColor);
            if (WallpaperConfigUtil.getSwithFlag()) {
				wallpaper.isDefaultTheme = 1;
			}
            values.put(WallpaperDbColumns.GroupColumns.IS_DEFAULT_THEME, wallpaper.isDefaultTheme);
            values.put(WallpaperDbColumns.GroupColumns.IS_TIME_BLACK, wallpaper.isTimeBlack);
            values.put(WallpaperDbColumns.GroupColumns.IS_STATUSBAR_BLACK, wallpaper.isStatusBarBlack);
            
            if (isSystem) {
                values.put(WallpaperDbColumns.GroupColumns.SYSTEM_FLAG, 1);
            }
            if (db.isOpen()) {
                id = db.insert(WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME, null, values);
            }
            return id > 0 ? true : false;
        }
        
        public void refreshDb(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + WallpaperDbColumns.KEYGUARD_WALLPAPER_GROUP_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + WallpaperDbColumns.KEYGUARD_WALLPAPER_TABLE_NAME);
            db.execSQL("DROP TRIGGER IF EXISTS " + WallpaperDbColumns.FK_INSERT_GROUP);
            db.execSQL("DROP TRIGGER IF EXISTS " + WallpaperDbColumns.FK_DELETE_GROUP);
            onCreate(db);
        }
        
        public void refreshDbFromPath(String path, SQLiteDatabase db) {
        	try {
                mType = Config.WallpaperStored.WALLPAPER_KEYGUARD_TYPE;
                File file = new File(path);
                getFile(db, file);
            } catch (Exception e) {
            	Log.d("Wallpaper_DEBUG", "DBHelper--------onCreat--------refreshDbFromPath e = "+e);
            }
        }
        
        public void findNextDayWallpaper(SQLiteDatabase db) {
        	try {
                mType = Config.WallpaperStored.WALLPAPER_KEYGUARD_TYPE;
                File file = new File(Config.WallpaperStored.NEXTDAY_WALLPAPER_PATH);
                getFile(db, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }

    public void openDb(Context context) {
        mDBHelper = new DatabaseHelper(context);
        try {
            mSqlDb = mDBHelper.getWritableDatabase();
        } catch (Exception e) {
            mSqlDb = mDBHelper.getReadableDatabase();
            e.printStackTrace();
        }
    }

    public void closeDb() throws SQLiteException {
        if (mDBHelper != null) {
            mDBHelper.close();
            mDBHelper = null;
        }
        if (mSqlDb != null) {
            mSqlDb.close();
            mSqlDb = null;
        }
    }
    
    public void refreshDb() {
    	if (mDBHelper != null && mSqlDb != null) {
    		mDBHelper.isUpdate = true;
			mDBHelper.refreshDb(mSqlDb);
		}
    }
    
}