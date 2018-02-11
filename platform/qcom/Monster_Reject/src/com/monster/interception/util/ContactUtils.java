package com.monster.interception.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.monster.interception.InterceptionApplication;
import com.monster.interception.database.BlackItem;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

public class ContactUtils {
    public static String getContactNameByPhoneNumber(Context context, String address) {
        if (TextUtils.isEmpty(address) || address.length() <= 3) {
            return "";
        }
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = '" + address + "'",
                null,
                null);
        if (cursor == null) {
            return null;
        }
        try {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                String name = cursor.getString(nameFieldColumnIndex);
                return name;
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public static List<BlackItem> getBlackItemListByDataId(Context context, long[] dataIds) {
        if (dataIds == null || dataIds.length == 0) {
            return null;
        }
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        StringBuilder extraSel = new StringBuilder();
        for(long dataId : dataIds) {
            extraSel.append(dataId + ",");
        }
        extraSel.deleteCharAt(extraSel.length() - 1);
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                ContactsContract.CommonDataKinds.Phone._ID + " IN (" + extraSel.toString() + ")",
                null,
                null);
        if (cursor == null) {
            return null;
        }
        ArrayList<BlackItem> blackItemList = new ArrayList<>();
        //tangyisen add remove duplicate number
        HashSet<BlackItem> blackItemSet = new HashSet<>();
        try {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                int numberFieldColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME); 
                String number = cursor.getString(numberFieldColumnIndex);
                String name = cursor.getString(nameFieldColumnIndex);
                //
                number = number.replace("-", "").replace(" ", "");
                name = name.replace("-", "").replace(" ", "");
                if (!TextUtils.isEmpty(number)) {
                    if (InterceptionUtils.isNoneDigit(number)) {
                        continue;
                    }
                    List<String> blacklist = InterceptionApplication.getInstance().getBlackList();
                    if (blacklist != null && blacklist.contains(number)) {
                        continue;
                    } 
                }
                BlackItem blackItem = new BlackItem();
                blackItem.setmNumber(number);
                blackItem.setmBlackName(name);
                //blackItemList.add(blackItem);
                blackItemSet.add(blackItem);
            }
            blackItemList.addAll(blackItemSet);
            return blackItemList;
        } finally {
            cursor.close();
        }
    }
}