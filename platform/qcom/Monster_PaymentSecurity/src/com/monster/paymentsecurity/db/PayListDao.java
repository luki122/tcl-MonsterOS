package com.monster.paymentsecurity.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.monster.paymentsecurity.bean.PayAppInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sandysheny on 16-11-22.
 */
public class PayListDao {
    private final Context context;

    public PayListDao(Context context) {
        this.context = context.getApplicationContext();
    }

    public void insert(PayAppInfo payAppInfo) {
        PayListDB mPayListDB = new PayListDB(context.getApplicationContext());
        mPayListDB.open();
        Map<String, Object> map = new HashMap<>();
        map.put(PayListDB.NAME, payAppInfo.getName());
        map.put(PayListDB.PACKAGENAME, payAppInfo.getPackageName());
        map.put(PayListDB.NEED_DETECT, payAppInfo.isNeedDetect() ? 1 : 0);
        insert(map);
        mPayListDB.close();
    }


    public void insert(Map<String, Object> map) {
        PayListDB mPayListDB = new PayListDB(context.getApplicationContext());
        mPayListDB.open();
        mPayListDB.insert(PayListDB.PAYLIST_TABLE, map);
        mPayListDB.close();
    }

    public void update(String packageName, boolean needDetect) {
        PayListDB mPayListDB = new PayListDB(context.getApplicationContext());
        mPayListDB.open();
        mPayListDB.update(PayListDB.PAYLIST_TABLE, new String[]{PayListDB.NEED_DETECT}, new Object[]{needDetect ? 1 : 0},
                new String[]{PayListDB.PACKAGENAME}, new String[]{packageName});
        mPayListDB.close();
    }

    public void delete(String packageName) {
        PayListDB mPayListDB = new PayListDB(context.getApplicationContext());
        mPayListDB.open();
        mPayListDB.delete(PayListDB.PAYLIST_TABLE, new String[]{PayListDB.PACKAGENAME}, new String[]{packageName});
        mPayListDB.close();
    }

    public List<PayAppInfo> getPayList() {
        PayListDB mPayListDB = new PayListDB(context.getApplicationContext());
        mPayListDB.open();
        List<PayAppInfo> result = new ArrayList<>();
        List<Map> list = mPayListDB.queryListMap("select * from " + PayListDB.PAYLIST_TABLE, null);
        for (Map item : list) {
            if (item.containsKey(PayListDB.PACKAGENAME)) {
                PayAppInfo payAppInfo = new PayAppInfo();
                payAppInfo.setName(String.valueOf(item.get(PayListDB.NAME)));
                payAppInfo.setPackageName(String.valueOf(item.get(PayListDB.PACKAGENAME)));
                payAppInfo.setNeedDetect((int) item.get(PayListDB.NEED_DETECT) == 1);
                result.add(payAppInfo);
            }
        }
        mPayListDB.close();
        return result;
    }

    public List<PayAppInfo> getPayList(boolean isNeedDetect) {
        PayListDB mPayListDB = new PayListDB(context.getApplicationContext());
        mPayListDB.open();
        List<PayAppInfo> result = new ArrayList<>();
        List<Map> list = mPayListDB.queryListMap("select * from " + PayListDB.PAYLIST_TABLE+ " where " + PayListDB
                .NEED_DETECT + "=?", new String[]{String.valueOf(isNeedDetect ? 1 : 0)});
        for (Map item : list) {
            if (item.containsKey(PayListDB.PACKAGENAME)) {
                PayAppInfo payAppInfo = new PayAppInfo();
                payAppInfo.setName(String.valueOf(item.get(PayListDB.NAME)));
                payAppInfo.setPackageName(String.valueOf(item.get(PayListDB.PACKAGENAME)));
                payAppInfo.setNeedDetect((int) item.get(PayListDB.NEED_DETECT) == 1);
                result.add(payAppInfo);
            }
        }
        mPayListDB.close();
        return result;
    }

    public PayAppInfo getPayApp(String packageName) {
        PayListDB mPayListDB = new PayListDB(context.getApplicationContext());
        mPayListDB.open();
        Map map = mPayListDB.queryItemMap("select * from " + PayListDB.PAYLIST_TABLE + " where " + PayListDB
                .PACKAGENAME + "=?", new String[]{packageName});
        PayAppInfo payAppInfo = null;
        if (map.containsKey(PayListDB.PACKAGENAME)) {
            payAppInfo = new PayAppInfo();
            payAppInfo.setName(String.valueOf(map.get(PayListDB.NAME)));
            payAppInfo.setPackageName(String.valueOf(map.get(PayListDB.PACKAGENAME)));
            payAppInfo.setNeedDetect((int) map.get(PayListDB.NEED_DETECT) == 1);
        }
        mPayListDB.close();
        return payAppInfo;
    }

    public boolean isNeedDetect(String packageName) {
        PayListDB mPayListDB = new PayListDB(context.getApplicationContext());
        mPayListDB.open();
        String sql = "select " + PayListDB.NEED_DETECT + " from " + PayListDB.PAYLIST_TABLE + " where " + PayListDB
                .PACKAGENAME + "=?";
        Map<String, Integer> map = mPayListDB.queryItemMap(sql, new String[]{packageName});
        mPayListDB.close();
        return map.containsKey(PayListDB.NEED_DETECT) && map.get(PayListDB.NEED_DETECT) == 1;
    }
}
