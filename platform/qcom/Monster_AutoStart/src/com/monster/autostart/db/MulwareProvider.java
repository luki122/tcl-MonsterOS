/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.monster.autostart.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.monster.autostart.bean.AppInfo;
import com.monster.autostart.bean.AppManagerState;
import com.monster.autostart.utils.Utilities;

/**
 * Demonstration of bottom to top implementation of a content provider holding
 * structured data through displaying it in the UI, using throttling to reduce
 * the number of queries done when its data changes.
 */
public class MulwareProvider extends ContentProvider {
    // Debugging.
    static final String TAG = "MulwareProvider";
    public static final String SPLIT = "_"; 
    /**
     * The authority we use to get to our sample provider.
     */
    public static final String AUTHORITY = "com.monster.autostart.db";
    
    public static final class MulwareTable implements BaseColumns {

        // This class cannot be instantiated
        private MulwareTable() {}

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "mulwares";

        public static final String TABLE_AUTOAPP_NAME = "AutoStartApps";
        
        public static final String TABLE_DATA_COLLECT_NAME = "DataCollection";
        
        public static final String TABLE_WHITELIST_NAME = "WhiteList"; //for all white list app content (content will be broadcast or pkg)
        
        public static final String TABLE_BLACKLIST_NAME = "BlackList"; //for all black list app content (content will be broadcast or pkg)
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY + "/mulwares");

        public static final Uri CONTENT_AUTO_APP_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_AUTOAPP_NAME);
        
        public static final Uri CONTENT_DATA_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_DATA_COLLECT_NAME);
        
        public static final Uri CONTENT_WL_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_WHITELIST_NAME);
        
        public static final Uri CONTENT_BL_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_BLACKLIST_NAME);
        
        /**
         * The content URI base for a single row of data. Callers must
         * append a numeric row id to this Uri to retrieve a row
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse("content://" + AUTHORITY + "/mulwares/");
        
        public static final Uri CONTENT_AUTO_APP_ID_URI_BASE = Uri.parse("content://" + AUTHORITY +"/" + TABLE_AUTOAPP_NAME+"/");

        public static final Uri CONTENT_DATA_URL_BASE = Uri.parse("content://" + AUTHORITY +"/" + TABLE_DATA_COLLECT_NAME+"/");
        
        /**
         * The MIME type of {@link #CONTENT_URI}.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/mulwares";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/mulwares";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = MulwareTable._ID + " COLLATE LOCALIZED ASC";

        public static final String AD_PACKAGENAME = "ad_packagename";
        public static final String AD_COUNT = "ad_count";
        public static final String AD_PROHIBIT = "ad_prohibit";
        public static final String AD_NAME = "ad_name";
        public static final String AD_BANIPS = "ad_banIps";
        public static final String AD_BANURLS = "ad_banUrls";
        public static final String AD_TYPE = "ad_type";
        
        /**M:Hazel start to add auto-start constant begin*/ 
        public static final String TITLE = "title";
        public static final String INTENT = "intent";
        public static final String STATUS = "status";
        
        public static final String DAT_PACKAGE_NAME = "package";
        public static final String DAT_CLASS_NAME = "class";
        public static final String DAT_ACTION = "action";
        public static final String DAT_WHERE = "wheres";
        
        
        public static final String WBLIST_CONTENT = "content";
        
        /**M:Hazel start to add auto-start constant end*/ 
        
        public static final String TABLE_SQL = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY,"                
        		+ AD_PACKAGENAME + " TEXT,"
                + AD_COUNT + " INTEGER,"
                + AD_PROHIBIT + " BOOLEAN DEFAULT false,"
                + AD_NAME + " TEXT,"
                + AD_BANIPS + " TEXT,"
                + AD_TYPE + " INTEGER,"
                + AD_BANURLS + " TEXT"+");";
        
       /**M:Hazel start to add auto start table begin*/ 
        public static final String TABLE_AUTOAPP = "CREATE TABLE " + TABLE_AUTOAPP_NAME + " ("
        		+ _ID + " INTEGER PRIMARY KEY,"
        		+ INTENT+" TEXT,"
        		+ STATUS+" INTEGER NOT NULL DEFAULT 0" +");";
        
        public static final String TABLE_DATA = "CREATE TABLE " + TABLE_DATA_COLLECT_NAME + " ("
        		+ _ID + " INTEGER PRIMARY KEY,"
        		+ DAT_PACKAGE_NAME +" TEXT,"
        		+ DAT_CLASS_NAME +" TEXT,"
        		+ DAT_ACTION +" TEXT," 
        		+ DAT_WHERE + " TEXT"+");";
        		
        public static final String TABLE_WHITELIST = "CREATE TABLE " + TABLE_WHITELIST_NAME + " ("
        		+ _ID + " INTEGER PRIMARY KEY,"
        		+ WBLIST_CONTENT+" TEXT"
        		+");";
        
        public static final String TABLE_BLACKLIST = "CREATE TABLE " + TABLE_BLACKLIST_NAME + " ("
        		+ _ID + " INTEGER PRIMARY KEY,"
        		+ WBLIST_CONTENT+" TEXT"
        		+");";
       /**M:Hazel start to add auto start table end*/ 
    }
    
    
    
    /**
     * This class helps open, create, and upgrade the database file.
     */
   static class DatabaseHelper extends SQLiteOpenHelper {

       private static final String DATABASE_NAME = "mulwares_data.db";
       private static final int DATABASE_VERSION = 1;

       DatabaseHelper(Context context) {

           // calls the super constructor, requesting the default cursor factory.
           super(context, DATABASE_NAME, null, DATABASE_VERSION);
       }

       /**
        *
        * Creates the underlying database with table name and column names taken from the
        * NotePad class.
        */
       @Override
       public void onCreate(SQLiteDatabase db) {
    	   //创建数据库
    	   //db.execSQL(MulwareTable.TABLE_SQL);
    	   /**M:Hazel start to create auto-start application begin*/
    	   db.execSQL(MulwareTable.TABLE_AUTOAPP);
    	   
    	   db.execSQL(MulwareTable.TABLE_DATA);
    	   
    	   db.execSQL(MulwareTable.TABLE_WHITELIST);
    	   
    	   db.execSQL(MulwareTable.TABLE_BLACKLIST);
    	   /**M:Hazel start to create auto-start application end*/
       }

       /**
        *
        * Demonstrates that the provider must consider what happens when the
        * underlying datastore is changed. In this sample, the database is upgraded the database
        * by destroying the existing data.
        * A real application should upgrade the database in place.
        */
       @Override
       public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

           // Logs that the database is being upgraded
           Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                   + newVersion + ", which will destroy all old data");

           // Kills the table and existing data
           db.execSQL("DROP TABLE IF EXISTS images");

           // Recreates the database with a new version
           onCreate(db);
       }
       
       
   }

   // A projection map used to select columns from the database
   private final HashMap<String, String> mMulwareProjectionMap;
   // Uri matcher to decode incoming URIs.
   private final UriMatcher mUriMatcher;

   // The incoming URI matches the main table URI pattern
   private static final int ID = 0;
   private static final int AD_COUNT = 1;
   private static final int AD_PACKAGENAME = AD_COUNT+1;
   private static final int AD_PROHIBIT = AD_PACKAGENAME+1;
   private static final int AD_NAME = AD_PROHIBIT+1;
   private static final int AD_BANIPS = AD_NAME+1;
   private static final int AD_BANURLS = AD_BANIPS+1;
   private static final int AD_TYPE = AD_BANURLS+1;
   
   private static final int AD_ID = AD_TYPE +1;

   private static final int DAT_ID = AD_ID + 1;
   
   private static final int WL_ID = DAT_ID + 1;
   
   private static final int BL_ID = WL_ID + 1;
   
   // Handle to a new DatabaseHelper.
   private DatabaseHelper mOpenHelper;

   /**
    * Global provider initialization.
    */
   public MulwareProvider() {
       // Create and initialize URI matcher.
       mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
       mUriMatcher.addURI(AUTHORITY, MulwareTable.TABLE_NAME, ID);
       mUriMatcher.addURI(AUTHORITY, MulwareTable.AD_PACKAGENAME , AD_PACKAGENAME);
       mUriMatcher.addURI(AUTHORITY, MulwareTable.AD_COUNT , AD_COUNT);
       mUriMatcher.addURI(AUTHORITY, MulwareTable.AD_PROHIBIT , AD_PROHIBIT);
       mUriMatcher.addURI(AUTHORITY, MulwareTable.AD_NAME , AD_NAME);
       mUriMatcher.addURI(AUTHORITY, MulwareTable.AD_BANIPS , AD_BANIPS);
       mUriMatcher.addURI(AUTHORITY, MulwareTable.AD_BANURLS , AD_BANURLS);
       mUriMatcher.addURI(AUTHORITY, MulwareTable.AD_TYPE , AD_TYPE);
       mUriMatcher.addURI(AUTHORITY, MulwareTable.TABLE_AUTOAPP_NAME, AD_ID);
       mUriMatcher.addURI(AUTHORITY, MulwareTable.TABLE_DATA_COLLECT_NAME, DAT_ID);
       mUriMatcher.addURI(AUTHORITY, MulwareTable.TABLE_WHITELIST_NAME, WL_ID);
       mUriMatcher.addURI(AUTHORITY, MulwareTable.TABLE_BLACKLIST_NAME, BL_ID);
       
       // Create and initialize projection map for all columns.  This is
       // simply an identity mapping.
       mMulwareProjectionMap = new HashMap<String, String>();
       mMulwareProjectionMap.put(MulwareTable._ID, MulwareTable._ID);
       mMulwareProjectionMap.put(MulwareTable.AD_PACKAGENAME, MulwareTable.AD_PACKAGENAME);
       mMulwareProjectionMap.put(MulwareTable.AD_COUNT, MulwareTable.AD_COUNT);
       mMulwareProjectionMap.put(MulwareTable.AD_PROHIBIT, MulwareTable.AD_PROHIBIT);
       mMulwareProjectionMap.put(MulwareTable.AD_NAME, MulwareTable.AD_NAME);
       mMulwareProjectionMap.put(MulwareTable.AD_BANIPS, MulwareTable.AD_BANIPS);
       mMulwareProjectionMap.put(MulwareTable.AD_BANURLS, MulwareTable.AD_BANURLS);       
       mMulwareProjectionMap.put(MulwareTable.AD_TYPE, MulwareTable.AD_TYPE);       
   }

   /**
    * Perform provider creation.
    */
   @Override
   public boolean onCreate() {
       mOpenHelper = new DatabaseHelper(getContext());
       // Assumes that any failures will be reported by a thrown exception.
       /**M:Hazel start to add begin*/
       AppManagerState.seAppProvider(this);
       Log.e(Utilities.TAG, "_CLS_:"+"MulwareProvider"+";"+"_FUNCTION_:"+"onCreate");  
       /**M:Hazel start to add end*/
       return true;
   }

   /**
    * Handle incoming queries.
    */
   @Override
   public Cursor query(Uri uri, String[] projection, String selection,
           String[] selectionArgs, String sortOrder) {
       // Constructs a new query builder and sets its table name
       SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
       switch (mUriMatcher.match(uri)) {
           case ID:
           case AD_PACKAGENAME:
           case AD_COUNT:
           case AD_PROHIBIT:
           case AD_NAME:
           case AD_BANIPS:
           case AD_BANURLS:
        	   qb.setTables(MulwareTable.TABLE_NAME);
               qb.setProjectionMap(mMulwareProjectionMap);
               break;
               
           case AD_ID:
        	   qb.setTables(MulwareTable.TABLE_AUTOAPP_NAME);
        	   break;
        	   
           case DAT_ID:
        	   qb.setTables(MulwareTable.TABLE_DATA_COLLECT_NAME);
        	   break;
           case WL_ID:
        	   qb.setTables(MulwareTable.TABLE_WHITELIST_NAME);
        	   break;
           case BL_ID:
        	   qb.setTables(MulwareTable.TABLE_BLACKLIST_NAME);
        	   break;
           default:
               throw new IllegalArgumentException("Unknown URI " + uri);
       }

       if (TextUtils.isEmpty(sortOrder)) {
           sortOrder = BaseColumns._ID;
       }

       SQLiteDatabase db = mOpenHelper.getReadableDatabase();

       Cursor c = qb.query(db, projection, selection, selectionArgs,
               null /* no group */, null /* no filter */, sortOrder);

       c.setNotificationUri(getContext().getContentResolver(), uri);
       return c;
   }

   /**
    * Return the MIME type for an known URI in the provider.
    */
   @Override
   public String getType(Uri uri) {
       switch (mUriMatcher.match(uri)) {
           case ID:
               return MulwareTable.CONTENT_TYPE;
           case AD_PACKAGENAME:
           case AD_COUNT:
           case AD_PROHIBIT:
           case AD_NAME:
           case AD_BANIPS:
           case AD_BANURLS:
               return MulwareTable.CONTENT_ITEM_TYPE;
           default:
               throw new IllegalArgumentException("Unknown URI " + uri);
       }
   }

   /**
    * Handler inserting new data.
    */
   @Override
   public Uri insert(Uri uri, ContentValues initialValues) {
//       if (mUriMatcher.match(uri) != ID) {
//           // Can only insert into to main URI.
//    	 
//           throw new IllegalArgumentException("Unknown URI " + uri);
//       }

       ContentValues values;

       if (initialValues != null) {
           values = new ContentValues(initialValues);
       } else {
           values = new ContentValues();
       }

       //values.put(key, value)

       SQLiteDatabase db = mOpenHelper.getWritableDatabase();

       String tableName = null;
       Uri notifyUri = null;
       switch (mUriMatcher.match(uri)) {
	       case ID:
	    	   tableName = MulwareTable.TABLE_NAME;
	    	   notifyUri = MulwareTable.CONTENT_ID_URI_BASE;
	           break;
	           
	       case AD_ID:
	    	   tableName = MulwareTable.TABLE_AUTOAPP_NAME;
	    	   notifyUri = MulwareTable.CONTENT_AUTO_APP_ID_URI_BASE;
	    	   break;
	    	   
	       case DAT_ID:
	    	   tableName = MulwareTable.TABLE_DATA_COLLECT_NAME;
	    	   notifyUri = MulwareTable.CONTENT_DATA_URL_BASE;	    	   
	    	   break;
	       case WL_ID:
	    	   tableName = MulwareTable.TABLE_WHITELIST_NAME;
	    	   break;
	       case BL_ID:
	    	   tableName = MulwareTable.TABLE_BLACKLIST_NAME;
	    	   break;
	       default:
	           throw new IllegalArgumentException("Unknown URI " + uri);
	   }
       Log.e("sunset", "tableName="+tableName);
       long rowId = db.insert(tableName, null, values);

       // If the insert succeeded, the row ID exists.
       if (rowId > 0) {
           Uri noteUri = ContentUris.withAppendedId(notifyUri, rowId);
           getContext().getContentResolver().notifyChange(noteUri, null);
           return noteUri;
       }

       throw new SQLException("Failed to insert row into " + uri);
   }

   /**
    * Handle deleting data.
    */
   @Override
   public int delete(Uri uri, String where, String[] whereArgs) {
       SQLiteDatabase db = mOpenHelper.getWritableDatabase();
       String finalWhere;

       int count;
       switch (mUriMatcher.match(uri)) {
           case ID:
           case AD_PACKAGENAME:
           case AD_COUNT:
           case AD_PROHIBIT:
           case AD_NAME:
           case AD_BANIPS:
           case AD_BANURLS:
        	   count = db.delete(MulwareTable.TABLE_NAME, where, whereArgs);
               break;
           default:
               throw new IllegalArgumentException("Unknown URI " + uri);
       }
       getContext().getContentResolver().notifyChange(uri, null);
       return count;
   }

   /**
    * Handle updating data.
    */
   @Override
   public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
       SQLiteDatabase db = mOpenHelper.getWritableDatabase();
       int count;
       switch (mUriMatcher.match(uri)) {
	       case ID:
	       case AD_PACKAGENAME:
	       case AD_COUNT:
	       case AD_PROHIBIT:
	       case AD_NAME:
	       case AD_BANIPS:
	       case AD_BANURLS:
        	   count = db.update(MulwareTable.TABLE_NAME, values, where, whereArgs);
               break;
               
           case AD_ID:
        	   count = db.update(MulwareTable.TABLE_AUTOAPP_NAME, values, where, whereArgs);       	 
        	   break;
        	   
           default:
               throw new IllegalArgumentException("Unknown URI " + uri);
       }

       getContext().getContentResolver().notifyChange(uri, null);

       return count;
   }
   
   /**M:Hazel add for insert single data begin*/
	public void insert(SQLiteDatabase db, AppInfo info) {
		ContentValues values = new ContentValues();
		/**M:Hazel will never write title in database begin*/
		//values.put(MulwareTable.TITLE, info.getTitle().toString());
		/**M:Hazel will never write title in database end*/
		values.put(MulwareTable.INTENT, info.getIntent().toUri(0));
		values.put(MulwareTable.STATUS, info.getStatus());
		db.insert(MulwareTable.TABLE_AUTOAPP_NAME, null, values);

	}
	
	public void insert(AppInfo info) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		/**M:Hazel will never write title in database begin*/
		//values.put(MulwareTable.TITLE, info.getTitle().toString());
		/**M:Hazel will never write title in database end*/
		values.put(MulwareTable.INTENT, info.getIntent().toUri(0));
		values.put(MulwareTable.STATUS, info.getStatus());
		db.insert(MulwareTable.TABLE_AUTOAPP_NAME, null, values);

	}
	
	public void insert(String info,String table){
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(MulwareTable.WBLIST_CONTENT, info);
		db.insert(table, null, values);

	}
	
   /**M:Hazel add for insert single data end*/
   
   /**M:Hazel add for insert multi data begin*/
	public void insert(List<AppInfo> list) {
		Log.e(Utilities.TAG, "_CLS_:"+"MulwareProvider"+";"+"_FUNCTION_:"+"insertApps");  
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			for (int i = 0; i < list.size(); i++) {
				insert(db, list.get(i));
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(Utilities.TAG,
					"_FUNCTION_: insertApps:"
							+ "insert multi app info and catch exception and content is"
							+ e.getMessage());
		} finally {
			db.endTransaction();
		}
	}
   /**M:Hazel add for insert multi data end*/

	
    public List<AppInfo> query(String[] columns, String selection, String[] selectionArgs){
    	List<AppInfo> list = new ArrayList<AppInfo>();
    	SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = db.query(MulwareTable.TABLE_AUTOAPP_NAME, columns, selection, selectionArgs, null, null, null);
		
		if (c != null) {
			try {
				while (c.moveToNext()) {
					AppInfo info = new AppInfo();
					/**M:Hazel will never read title in database begin*/
//					info.setTitle(c.getString(c
//							.getColumnIndex(MulwareProvider.MulwareTable.TITLE)));
					/**M:Hazel will never read title in database end*/
					String intentDescription = c
							.getString(c
									.getColumnIndex(MulwareProvider.MulwareTable.INTENT));
					Intent intent = Intent.parseUri(intentDescription, 0);
					info.setIntent(intent);

					info.setStatus(c.getInt(c
							.getColumnIndex(MulwareProvider.MulwareTable.STATUS)));

					String pkgName = intent.getComponent().getPackageName();
					Drawable d = Utilities.getInstance().getAppDrawable(AppManagerState.getInstance().getContext(), pkgName);
					
					
					/**M:Hazel parser title and never read title from database again begin*/
					String title = Utilities.getInstance().getTitle(AppManagerState.getInstance().getContext(), intent);
					info.setTitle(title);
					/**M:Hazel parser title and never read title from database again end*/
					
					info.setIcon(d);
					
					list.add(info);

				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				c.close();
			}
		}
    	return list;
    }
    
    
	public void update(AppInfo info) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(MulwareTable.STATUS, info.getStatus());
		db.update(MulwareTable.TABLE_AUTOAPP_NAME, cv, MulwareTable.INTENT
				+ "=?", new String[] { info.getIntent().toUri(0) });
	}
	
	public void update(SQLiteDatabase db,AppInfo info){
		ContentValues cv = new ContentValues();
		cv.put(MulwareTable.STATUS, info.getStatus());
		db.update(MulwareTable.TABLE_AUTOAPP_NAME, cv, MulwareTable.INTENT
				+ "=?", new String[] { info.getIntent().toUri(0) });
	}
	
	public void update(List<AppInfo> list){
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			for (int i = 0; i < list.size(); i++) {
				update(db, list.get(i));
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(Utilities.TAG,
					"_FUNCTION_: update:"
							+ "insert multi app info and catch exception and content is"
							+ e.getMessage());
		} finally {
			db.endTransaction();
		}
	}
	
	public void delete(Intent intent){
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.delete(MulwareTable.TABLE_AUTOAPP_NAME, MulwareTable.INTENT
				+ "=?", new String[] { intent.toUri(0) });
	}
	
	public void delete(SQLiteDatabase db,AppInfo info){
		db.delete(MulwareTable.TABLE_AUTOAPP_NAME, MulwareTable.INTENT
				+ "=?", new String[] { info.getIntent().toUri(0) });
	}
	
	public void delete(List<AppInfo> list){
		Log.e("sunset", "delete list.size="+list.size());
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			for (int i = 0; i < list.size(); i++) {
				delete(db, list.get(i));
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(Utilities.TAG,
					"_FUNCTION_: delete:"
							+ "insert multi app info and catch exception and content is"
							+ e.getMessage());
		} finally {
			db.endTransaction();
		}
	}
}

