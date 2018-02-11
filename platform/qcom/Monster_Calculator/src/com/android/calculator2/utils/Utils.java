package com.android.calculator2.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.calculator2.R;
import com.android.calculator2.exchange.bean.MainExchangeBean;
import com.android.calculator2.exchange.bean.RateBean;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Utils {

    public static boolean hasOps(Context conext, String data) {// 判断字符串是否包含加减乘除
        boolean has = false;
        if (!TextUtils.isEmpty(data)) {
            String op_add = conext.getString(R.string.op_add);
            String op_sub = conext.getString(R.string.op_sub);
            String op_mul = conext.getString(R.string.op_mul);
            String op_div = conext.getString(R.string.op_div);
            if (data.contains(op_add) || data.contains(op_sub) || data.contains(op_mul) || data.contains(op_div)) {
                has = true;
            }
        }
        return has;
    }

    public static String formatTosepara(Context contex, String str) {
        String point = contex.getString(R.string.dec_point);
        if (str.contains(point)) {
            String[] temp = str.split(point);
            String buff = temp[1];
            if (buff.length() > 2) {
                buff = buff.substring(0, 3);
            }
            return temp[0] + point + buff;
        }
        return str;
    }

    public static String remainTwoPoint(Context contex, String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }
        
        if(data.contains("E")){
            String []split_str = data.split("E");
            String str1 = remainTwoPoint(contex,split_str[0]);
            return str1+"E"+split_str[1];
        }
        
        String point = contex.getString(R.string.dec_point);
        if (data.equals(point)) {
            return "0.00";
        }
        if (data.endsWith(point)) {
            return data + "00";
        }
        if (data.contains(point)) {
            String temp0 = data.substring(0, data.indexOf(point));
            String temp1 = data.substring(data.indexOf(point));

            if (temp1.length() > 3) {
                String buf = temp0 + temp1.substring(0, 3);
                return buf;
            } else if (temp1.length() == 2) {
                return data + "0";
            } else {
                return data;
            }
        } else {
            return data + ".00";
        }
    }

    public static void saveRateJson(Context context, String json) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.rate_pre_name, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(AppConst.rate_pre_name, json);
        editor.commit();
    }

    //public static String rateJsonString = "";

    public static String getRateJson(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.rate_pre_name, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AppConst.rate_pre_name, "");
    }

    public static List<RateBean> getLocalRateList(Context context) {

//        if(TextUtils.isEmpty(rateJsonString)){
//            rateJsonString = getRateJson(context);
//        }

        return getRateListByJson(getRateJson(context));
    }

    private static HashMap<String, String> hexunAllCurrencyMap = new HashMap<String, String>();

    public static HashMap<String, String> getHeXunCurrencyMap(Context context) {
        if (hexunAllCurrencyMap.isEmpty()) {
            getLocalRateList(context);
        }
        return hexunAllCurrencyMap;
    }

    public static List<RateBean> getRateListByJson(String json) {
        List<RateBean> m_list = new ArrayList<RateBean>();
        JSONObject jsonData;
        hexunAllCurrencyMap.clear();
        try {
            jsonData = new JSONObject(json);
            JSONArray jsonRates = jsonData.getJSONArray("rate");
            for (int i = 0; i < jsonRates.length(); i++) {
                JSONObject jsonRate = jsonRates.getJSONObject(i);
                RateBean m_bean = new RateBean();
                m_bean.fromJson(jsonRate);
                m_list.add(m_bean);
                hexunAllCurrencyMap.put(m_bean.n1, m_bean.n1);
                hexunAllCurrencyMap.put(m_bean.n2, m_bean.n2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return m_list;
    }

    public static void saveExchangeBean1(Context context, MainExchangeBean m_bean) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.exchange_name, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString("currency1_code", m_bean.currency_code);
        editor.putString("currency1_ch", m_bean.currency_ch);
        editor.putString("currency1_en", m_bean.currency_en);
        editor.putInt("flag1_id", m_bean.flag_id);
        editor.commit();
    }

    public static MainExchangeBean getExchangeBean1(Context context) {
        MainExchangeBean m_bean = new MainExchangeBean();
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.exchange_name, Context.MODE_PRIVATE);
        m_bean.currency_code = sharedPreferences.getString("currency1_code", "");
        m_bean.currency_ch = sharedPreferences.getString("currency1_ch", "");
        m_bean.currency_en = sharedPreferences.getString("currency1_en", "");
        m_bean.flag_id = sharedPreferences.getInt("flag1_id", 0);
        return m_bean;
    }

    public static void saveExchangeBean2(Context context, MainExchangeBean m_bean) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.exchange_name, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString("currency2_code", m_bean.currency_code);
        editor.putString("currency2_ch", m_bean.currency_ch);
        editor.putString("currency2_en", m_bean.currency_en);

        editor.putInt("flag2_id", m_bean.flag_id);
        editor.commit();
    }

    public static MainExchangeBean getExchangeBean2(Context context) {
        MainExchangeBean m_bean = new MainExchangeBean();
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.exchange_name, Context.MODE_PRIVATE);
        m_bean.currency_code = sharedPreferences.getString("currency2_code", "");
        m_bean.currency_ch = sharedPreferences.getString("currency2_ch", "");
        m_bean.currency_en = sharedPreferences.getString("currency2_en", "");
        m_bean.flag_id = sharedPreferences.getInt("flag2_id", 0);
        return m_bean;
    }

    public static void saveExchangeBean3(Context context, MainExchangeBean m_bean) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.exchange_name, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString("currency3_code", m_bean.currency_code);
        editor.putString("currency3_ch", m_bean.currency_ch);
        editor.putString("currency3_en", m_bean.currency_en);
        editor.putInt("flag3_id", m_bean.flag_id);
        editor.commit();
    }

    public static MainExchangeBean getExchangeBean3(Context context) {
        MainExchangeBean m_bean = new MainExchangeBean();
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.exchange_name, Context.MODE_PRIVATE);
        m_bean.currency_code = sharedPreferences.getString("currency3_code", "");
        m_bean.currency_ch = sharedPreferences.getString("currency3_ch", "");
        m_bean.currency_en = sharedPreferences.getString("currency3_en", "");
        m_bean.flag_id = sharedPreferences.getInt("flag3_id", 0);
        return m_bean;
    }

    public static boolean isNotSaveExchangeData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.exchange_name, Context.MODE_PRIVATE);
        String currency1_code = sharedPreferences.getString("currency1_code", "");
        return TextUtils.isEmpty(currency1_code);
    }

    public static void showKeyBoard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, 0);
    }

    public static void hideKeyBoard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }
    
    public static void saveRateUpDateInfo(Context context,String info_ch,String info_en){
        
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.exchange_name, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString("update_time_ch", info_ch);
        editor.putString("update_time_en", info_en);
        editor.commit();
    }
    
    public static String getRateUpDataInfo(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.exchange_name, Context.MODE_PRIVATE);
        if(context.getResources().getConfiguration().locale.getCountry().equals("CN")){
            return sharedPreferences.getString("update_time_ch", "");
        } else {
            return sharedPreferences.getString("update_time_en", "");
        }
    }
    
    public static void saveUpdateTime(Context context){
        
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH)+1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        String update_time = year +"-"+month+"-"+day;
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.rate_update_time, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(AppConst.rate_update_time, update_time);
        editor.commit();
        
    }
    
    public static boolean isUpdateToday(Context context){
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH)+1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        String update_time = year +"-"+month+"-"+day;
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConst.rate_update_time, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AppConst.rate_update_time, "").equals(update_time);
    }
    
    public static int dip2px(Context context,float dipValue){
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dipValue*scale+0.5f);
   }

    public static boolean isNumber(Context context,String num){
        if(num.equals(context.getString(R.string.dec_point))){//.
            return true;
        } if(num.equals(context.getString(R.string.op_sub))){//-
            return true;
        } else if(num.equals(context.getString(R.string.op_sub)+context.getString(R.string.dec_point))){//-.
            return true;
        }else  if(num.startsWith(context.getString(R.string.op_sub))){//- 开始
            return num.substring(1).replace(context.getString(R.string.dec_point), "").matches("[0-9]+");
        } else if(num.contains(context.getString(R.string.dec_point))){
            return num.replace(context.getString(R.string.dec_point), "").matches("[0-9]+");
        } else {
            return num.matches("[0-9]+");
        }
    }

}
