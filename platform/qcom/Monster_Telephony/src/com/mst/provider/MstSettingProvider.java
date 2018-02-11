package com.mst.provider;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

/**
 * 这个类给外部程序提供访问内部数据的一个接口
 * 
 * @author HB
 * 
 */
public class MstSettingProvider extends ContentProvider {
	
	public static final String AUTHORITY = "com.mst.phone";
	public static final String DATABASE_NAME = "mstPhoneSetting.db";
	// 创建 数据库的时候，都必须加上版本信息；并且必须大于4
	public static final int DATABASE_VERSION = 4;
	
	public static final String TABLE_NAME = "setting";
	// Uri，外部程序需要访问就是通过这个Uri访问的，这个Uri必须的唯一的。
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/phone_setting");
	// 数据集的MIME类型字符串则应该以vnd.android.cursor.dir/开头
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/mst.phone.setting";
	// 单一数据的MIME类型字符串应该以vnd.android.cursor.item/开头
	public static final String CONTENT_TYPE_ITME = "vnd.android.cursor.item/mst.phone.setting";
	public static final int SETTINGS = 1;

	public static final String NAME = "name";
	public static final String VALUE = "value";

	public static final UriMatcher uriMatcher;
	static {
		// 常量UriMatcher.NO_MATCH表示不匹配任何路径的返回码
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		// 如果match()方法匹配content://hb.android.teacherProvider/teachern路径,返回匹配码为TEACHERS
		uriMatcher.addURI(AUTHORITY, "phone_setting", SETTINGS);
	}

	private DBOpenHelper dbOpenHelper = null;
	// UriMatcher类用来匹配Uri，使用match()方法匹配路径时返回匹配码

	/**
	 * 是一个回调函数，在ContentProvider创建的时候，就会运行,第二个参数为指定数据库名称，如果不指定，就会找不到数据库；
	 * 如果数据库存在的情况下是不会再创建一个数据库的。（当然首次调用 在这里也不会生成数据库必须调用SQLiteDatabase的
	 * getWritableDatabase,getReadableDatabase两个方法中的一个才会创建数据库）
	 */
	@Override
	public boolean onCreate() {
		// 这里会调用 DBOpenHelper的构造函数创建一个数据库；
		dbOpenHelper = new DBOpenHelper(this.getContext(),
				DATABASE_NAME, DATABASE_VERSION);
		return true;
	}

	/**
	 * 当执行这个方法的时候，如果没有数据库，他会创建，同时也会创建表，但是如果没有表，下面在执行insert的时候就会出错
	 * 这里的插入数据也完全可以用sql语句书写，然后调用 db.execSQL(sql)执行。
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// 获得一个可写的数据库引用，如果数据库不存在，则根据onCreate的方法里创建；
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		long id = 0;

		switch (uriMatcher.match(uri)) {
		case SETTINGS:
			id = db.insert(TABLE_NAME, null, values); // 返回的是记录的行号，主键为int，实际上就是主键值
			return ContentUris.withAppendedId(uri, id);
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case SETTINGS:
			count = db.delete(TABLE_NAME, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		db.close();
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case SETTINGS:
			count = db.update(TABLE_NAME, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		db.close();
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case SETTINGS:
			return CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		switch (uriMatcher.match(uri)) {
		case SETTINGS:
			return db.query(TABLE_NAME, projection, selection, selectionArgs,
					null, null, sortOrder);
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	/**
	 * 这个类继承SQLiteOpenHelper抽象类，用于创建数据库和表。创建数据库是调用它的父类构造方法创建。
	 * 
	 * @author HB
	 */
	public class DBOpenHelper extends SQLiteOpenHelper {
		
	    private final Context mContext;

		// 在SQLiteOepnHelper的子类当中，必须有该构造函数，用来创建一个数据库；
		public DBOpenHelper(Context context, String name,
				CursorFactory factory, int version) {
			// 必须通过super调用父类当中的构造函数
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
	        mContext = context;
		}

		 public DBOpenHelper(Context context, String name) {
			 this(context, name, DATABASE_VERSION);
		 }

		public DBOpenHelper(Context context, String name, int version) {
			this(context, name, null, version);
		}

		/**
		 * 只有当数据库执行创建 的时候，才会执行这个方法。如果更改表名，也不会创建，只有当创建数据库的时候，才会创建改表名之后 的数据表
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			System.out.println("create table");
			db.execSQL("create table " + TABLE_NAME
					+ "(" + BaseColumns._ID + " INTEGER PRIMARY KEY autoincrement,"
					+ NAME + " TEXT,"
					+ VALUE + " boolean)" + ";");
			createDefaultData(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
		
	    private void createDefaultData(SQLiteDatabase db) {
	    	String[] mName = {"overturn", "ringermode", "touch"};
	        for (int i = 0; i < mName.length; i++) {
	        	ContentValues values = new ContentValues();
	        	values.put("name", mName[i]);
	        	values.put("value", true);
	            db.insert(TABLE_NAME, null, values);            
	        }
	    }

	}
}