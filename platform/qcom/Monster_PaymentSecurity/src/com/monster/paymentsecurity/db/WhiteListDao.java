package com.monster.paymentsecurity.db;

import android.content.Context;

import com.monster.paymentsecurity.bean.WhiteListInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sandysheny on 16-11-22.
 */

public class WhiteListDao {

    private final Context context;

    public WhiteListDao(Context context) {
        this.context = context.getApplicationContext();
    }

    public void insert(WhiteListInfo whiteListInfo) {
        WhiteListDB mWhiteListDB = new WhiteListDB(context.getApplicationContext());
        mWhiteListDB.open();
        Map<String, Object> map = new HashMap<>();
        map.put(WhiteListDB.NAME, whiteListInfo.getName());
        map.put(WhiteListDB.PACKAGENAME, whiteListInfo.getPackageName());
        map.put(WhiteListDB.APKPATH, whiteListInfo.getApkPath());
        map.put(WhiteListDB.APPSTATE, whiteListInfo.isEnabled() ? 1 : 0);
        map.put(WhiteListDB.APKTYPE, whiteListInfo.getApkType());
        insert(map);
        mWhiteListDB.close();
    }

    public void insert(Map<String, Object> map) {
        WhiteListDB mWhiteListDB = new WhiteListDB(context.getApplicationContext());
        mWhiteListDB.open();
        mWhiteListDB.insert(WhiteListDB.WHITELIST_TABLE, map);
        mWhiteListDB.close();
    }

    public void delete(WhiteListInfo whiteListInfo) {
        WhiteListDB mWhiteListDB = new WhiteListDB(context.getApplicationContext());
        mWhiteListDB.open();
        mWhiteListDB.delete(WhiteListDB.WHITELIST_TABLE, new String[]{WhiteListDB.PACKAGENAME, WhiteListDB.APKTYPE}, new
                String[]{whiteListInfo.getPackageName(), String.valueOf(whiteListInfo.getApkType())});
        mWhiteListDB.close();
    }

    public void update(WhiteListInfo whiteListInfo) {
        WhiteListDB mWhiteListDB = new WhiteListDB(context.getApplicationContext());
        mWhiteListDB.open();
        mWhiteListDB.update(WhiteListDB.WHITELIST_TABLE, new String[]{WhiteListDB.APPSTATE}, new Object[]{whiteListInfo.isEnabled() ? 1 : 0},
                new String[]{WhiteListDB.PACKAGENAME, WhiteListDB.APKTYPE}, new String[]{whiteListInfo.getPackageName(), String.valueOf(whiteListInfo.getApkType())});
        mWhiteListDB.close();
    }

    public List<WhiteListInfo> getWhiteList() {
        WhiteListDB mWhiteListDB = new WhiteListDB(context.getApplicationContext());
        mWhiteListDB.open();
        List<WhiteListInfo> result = new ArrayList<>();
        List<Map> list = mWhiteListDB.queryListMap("select * from " + WhiteListDB.WHITELIST_TABLE + " where " + WhiteListDB
                .APPSTATE + "=?", new String[]{"1"});
        for (Map item : list) {
            if (item.containsKey(WhiteListDB.PACKAGENAME)) {
                WhiteListInfo whiteListInfo = new WhiteListInfo();
                whiteListInfo.setName(String.valueOf(item.get(WhiteListDB.NAME)));
                whiteListInfo.setPackageName(String.valueOf(item.get(WhiteListDB.PACKAGENAME)));
                whiteListInfo.setApkPath(String.valueOf(item.get(WhiteListDB.APKPATH)));
                whiteListInfo.setEnabled((int) item.get(WhiteListDB.APPSTATE) == 1);
                whiteListInfo.setApkType((int) item.get(WhiteListDB.APKTYPE));
                result.add(whiteListInfo);
            }
        }
        mWhiteListDB.close();
        return result;
    }

    public WhiteListInfo getWhiteListApp(String packageName) {
        WhiteListDB mWhiteListDB = new WhiteListDB(context.getApplicationContext());
        mWhiteListDB.open();
        Map map = mWhiteListDB.queryItemMap("select * from " + WhiteListDB.WHITELIST_TABLE + " where " + WhiteListDB
                .PACKAGENAME + "=? and " + WhiteListDB.APPSTATE + "=?", new String[]{packageName, "1"});
        WhiteListInfo whiteListInfo = null;
        if (map.containsKey(WhiteListDB.PACKAGENAME)) {
            whiteListInfo = new WhiteListInfo();
            whiteListInfo.setName(String.valueOf(map.get(PayListDB.NAME)));
            whiteListInfo.setPackageName(String.valueOf(map.get(PayListDB.PACKAGENAME)));
            whiteListInfo.setApkPath(String.valueOf(map.get(WhiteListDB.APKPATH)));
            whiteListInfo.setEnabled((int) map.get(WhiteListDB.APPSTATE) == 1);
        }
        mWhiteListDB.close();
        return whiteListInfo;
    }

}
