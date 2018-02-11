/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.manager;

//import FavoriteHelper;


public class FavoriteManager {
//    public static ArrayList<String> favoriteArray;
//    public static int DEL_FAVORITE = 100;
//    public static int ADD_FAVORITE = 200;
//
//    private FavoriteHelper mFavoriteHelper;
//    private Context mContext;
//    private SQLiteDatabase db;
//
//    public FavoriteManager(Context context) {
//        mContext = context;
//        mFavoriteHelper = new FavoriteHelper(context);
//    }
//
//    public static List<String> getFavoriteArray() {
//    	List<String> list = new CopyOnWriteArrayList<String>();
//    	if (favoriteArray != null) {
//    		list.addAll(favoriteArray);
//    	}
//    	return list;
//    }
//
//	public void clearAll() {
//		if (favoriteArray != null) {
//			favoriteArray.clear();
//		}
//	}
//
//    private void removeFromFavoriteArray(List<String> paths) {
//        int len = paths.size();
//        for (int i = 0; i < len; i++) {
//            if (!favoriteArray.isEmpty() && (new FileInfo(mContext, paths.get(i))).isFavorite() && favoriteArray.contains(paths.get(i))) {
//                favoriteArray.remove(paths.get(i));
//            }
//        }
//    }
//
//    private void updateFromFavoriteArray(String oldPath, String newPath) {
//        if (!favoriteArray.isEmpty() && (new FileInfo(mContext, oldPath).isFavorite() && favoriteArray.contains(oldPath))) {
//            favoriteArray.set(favoriteArray.indexOf(oldPath), newPath);
//        }
//    }
//
//    public void queryfavoriteFile() {
//        new Thread() {
//            public void run() {
//                ArrayList<String> array = new ArrayList<String>();
//
//                db = mFavoriteHelper.getWritableDatabase();
//                Cursor cursor = null;
//                String sql = null;
//                try {
//                    sql = "select _data from " + FavoriteHelper.TABLE_NAME;
//                    cursor = db.rawQuery(sql, null);
//                    if (cursor != null) {
//                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
//                            array.add(cursor.getString(0));
//                        }
//                    }
//                    FavoriteManager.favoriteArray = array;
//                } catch (Exception e) {
//                    FavoriteManager.favoriteArray = null;
//                } finally {
//                    if (cursor != null)
//                        cursor.close();
//                    if (db != null)
//                        db.close();
//                }
//            }
//        }.start();
//    }
//
//    public void deleteFavoriteFile(final List<String> paths) {
//        new Thread() {
//            public void run() {
//                if (!paths.isEmpty()) {
//                    db = mFavoriteHelper.getWritableDatabase();
//                    StringBuilder whereClause = new StringBuilder();
//                    whereClause.append("?");
//                    int len = paths.size() - 1;
//                    for (int i = 0; i < len; i++) {
//                        whereClause.append(",?");
//                    }
//                    String where = FavoriteHelper.FILE_PATH + " IN(" + whereClause.toString() + ")";
//                    String[] whereArgs = new String[paths.size()];
//                    paths.toArray(whereArgs);
//                    try {
//                        int num = db.delete(FavoriteHelper.TABLE_NAME, where, whereArgs);
//                        if (num > 0) {
//                            removeFromFavoriteArray(paths);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    } finally {
//                        if (db != null) {
//                            db.close();
//                        }
//                    }
//                }
//
//            }
//        }.start();
//    }
//
//    public void updataFavoriteFile(final String newPath, final String oldPath) {
//        new Thread() {
//            public void run() {
//                db = mFavoriteHelper.getWritableDatabase();
//                if (!TextUtils.isEmpty(newPath) && !TextUtils.isEmpty(oldPath)) {
//                    String where = FavoriteHelper.FILE_PATH + "=?";
//                    String[] whereArgs = new String[] {
//                            oldPath
//                    };
//                    ContentValues values = new ContentValues();
//                    values.put(FavoriteHelper.FILE_PATH, newPath);
//                    try {
//                        db.update(FavoriteHelper.TABLE_NAME, values, where, whereArgs);
//                        updateFromFavoriteArray(oldPath, newPath);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    } finally {
//                        if (db != null) {
//                            db.close();
//                        }
//                    }
//                }
//            }
//        }.start();
//    }
}
