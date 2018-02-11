package com.mst.black;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.mst.tms.MarkManager;
import com.mst.tms.MarkResult;

public class BlackUtils {
    private static final String TAG = "BlackUtils";

    public static void addblack(Context context, String add_number,
            String add_name) {
        Uri uri = Uri.parse("content://com.android.contacts/black");
        add_number = add_number.replace("-", "").replace(" ", "");
        add_name = add_name.replace("-", "").replace(" ", "");
        if (!TextUtils.isEmpty(add_number)) {
            boolean isDigit = false;
            for (int i = 0; i < add_number.length(); i++) {
                if (Character.isDigit(add_number.charAt(i))) {
                    isDigit = true;
                }
            }
            if (add_number.indexOf('+', 1) > 0) {
                isDigit = false;
            }
            if (!isDigit) {
                return;
            }

            String lable = MarkManager.getUserMark(
                    context, add_number);
            int userMark = -1;
            if (lable == null) {
                MarkResult mr = MarkManager.getMark(add_number);
                if(mr != null) {
                    lable = mr.getName();
                    userMark =  mr.getTagCount();
                }
            }

            ContentResolver cr = context.getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put("isblack", 1);
            cv.put("lable", lable);
            cv.put("user_mark", userMark);

            cv.put("number", add_number);
            cv.put("black_name", add_name);
            cv.put("reject", 3);

            Uri uris = cr.insert(uri, cv);

        }

    }
    
    public static String getPhoneNumberEqualString(String number) {
        return " PHONE_NUMBERS_EQUAL(number, \"" + number + "\", 0) ";
    }
}
