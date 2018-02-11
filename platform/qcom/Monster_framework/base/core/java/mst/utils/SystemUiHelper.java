package mst.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * SystemUiHelper
 *
 * @author liuzhicang
 */

public class SystemUiHelper {
    
    /**
     * 获取pkg的总通知数和已清理通知数
     *
     * @param context     Context对象。
     * @param pkg 包名。
     * @return count[0]---total count ，count[1]---clear count
     */
    public static long[] getNotifyCount(Context context, String pkg) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.parse("content://com.android.systemui.tcl.WdjNotificationProvider/count");
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        long[] count = new long[]{0, 0};
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (pkg.equals(cursor.getString(cursor.getColumnIndex("package")))) {
                    count[0] = cursor.getLong(cursor.getColumnIndex("total_count"));
                    count[1] = cursor.getLong(cursor.getColumnIndex("clear_count"));
                    break;
                }
            }
            cursor.close();
        }
        return count;
    }
}
