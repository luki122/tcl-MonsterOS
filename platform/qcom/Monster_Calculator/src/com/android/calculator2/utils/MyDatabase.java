package com.android.calculator2.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.android.calculator2.exchange.bean.CurrencyBean;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class MyDatabase {

    private SQLiteDatabase db;
    private ContryFlag m_flag;
    private HashMap<String, String> m_sort_letter = new HashMap<String, String>();

    private List<CurrencyBean> head_list = new ArrayList<CurrencyBean>();

    private boolean isShowChinese = true;

    public MyDatabase(Context context) {
        m_flag = new ContryFlag();
        isShowChinese = context.getResources().getConfiguration().locale.getCountry().equals("CN");
    }

    public void readOnlyOpen() {
        db = SQLiteDatabase.openDatabase(AppConst.filePath, null, SQLiteDatabase.OPEN_READONLY);
    }

    public List<CurrencyBean> getCurrencyList(String key, HashMap<String, String> all_currency,String star_letter) {
        readOnlyOpen();
        List<CurrencyBean> m_list = new ArrayList<CurrencyBean>();
        Cursor cursor;
        m_sort_letter.clear();
        
        m_list.clear();
        head_list.clear();
        
//        if(TextUtils.isEmpty(key)){
//            head_list.clear();
//        }
        
        
        String[] columnNames = { "currency_name_zh_ch", "currency_name_pinyin", "symbol", "search_field","currency_name_en" };
        String selection = null;
        String[] selectionArgs = null;
        // if (!TextUtils.isEmpty(key)) {
        // // selection = " search_field like ?";
        // // selectionArgs = new String[] { key };
        // // cursor = db.query("currency", columnNames, selection,
        // selectionArgs, null, null, "symbol");
        // cursor =
        // db.rawQuery("select  currency_name_zh_ch,  currency_name_pinyin,symbol from currency where search_field like  ?",new
        // String[]{"%"+key+"%"});
        // } else {
        // }
        cursor = db.query("currency", columnNames, selection, selectionArgs, null, null, "currency_name_pinyin");

        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            CurrencyBean bean = new CurrencyBean();
            bean.currency_ch = cursor.getString(cursor.getColumnIndex("currency_name_zh_ch"));
            bean.currency_code = cursor.getString(cursor.getColumnIndex("symbol"));
            bean.currency_en = cursor.getString(cursor.getColumnIndex("currency_name_en"));
            String search_field = cursor.getString(cursor.getColumnIndex("search_field"));
            if (all_currency.get(bean.currency_code) == null) {
                continue;
            }
            if (!TextUtils.isEmpty(key) && !search_field.toLowerCase().contains(key.toLowerCase())) {
                continue;
            }
            String country_pinyin = cursor.getString(cursor.getColumnIndex("currency_name_pinyin"));
            if(isShowChinese){
                country_pinyin = country_pinyin.substring(0, 1);
            } else {
                country_pinyin = bean.currency_en.substring(0, 1);
            }
            country_pinyin = country_pinyin.toUpperCase();
            bean.name_first_pinying = country_pinyin;
            bean.flag_id = m_flag.getFlagIdByCurrencyCode(bean.currency_code);
            if(TextUtils.isEmpty(key) && isFavCurrency(bean)){
                head_list.add(bean);
                bean.name_first_pinying = star_letter;
            }  else {
                m_list.add(bean);
//                m_sort_letter.put(bean.name_first_pinying, bean.name_first_pinying);
            }
            m_sort_letter.put(bean.name_first_pinying, bean.name_first_pinying);

        }

        Collections.sort(head_list, new FavCurrencyPinyinComparator());
        Collections.sort(m_list, new EmployeePinyinComparator(star_letter));
        head_list.addAll(m_list);//add zouxu 20160817 test
        cursor.close();
        close();
//        return m_list;
        return head_list;
    }

    public List<CurrencyBean> getHeadLis() {
        return head_list;
    }

    private boolean isFavCurrency(CurrencyBean bean) {
        String currency_code = bean.currency_code;
        
        if(currency_code.equals("CNY")){
            bean.pos = 0;
            return true;
        } else if(currency_code.equals("USD") ){
            bean.pos = 1;
            return true;
        } else if(currency_code.equals("HKD")){
            bean.pos = 2;
            return true;
        } else if(currency_code.equals("EUR")){
            bean.pos = 3;
            return true;
        } else if(currency_code.equals("GBP")){
            bean.pos = 4;
            return true;
        }
        return  false;
    }

    public HashMap<String, String> getSortLetterMap() {
        return m_sort_letter;
    }

    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
    }
}
