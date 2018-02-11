package com.android.calculator2.exchange.net;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.android.calculator2.R;
import com.android.calculator2.exchange.bean.RateBean;
import com.android.calculator2.utils.AppConst;
import com.android.calculator2.utils.Utils;

public class MyRateRequest {

    private List<RateBean> rate_list = new ArrayList<RateBean>();
    private String update_info;

    public List<RateBean> getRateList() {
        return rate_list;
    }

    public void clearRateListData(){
        rate_list.clear();
        rate_list = null;
    }

    public String getUpdateInfo() {
        return update_info;
    }


    public synchronized boolean getRateFromNet(Context context) {
        boolean is_sucess = false;
        rate_list.clear();
        JSONObject jsonParams = new JSONObject();
        String response = ConnectManager.post(context, AppConst.hexun_rate, jsonParams, true);
        if (response != null) {
            is_sucess = true;
            rate_list = Utils.getRateListByJson(response);
            Utils.saveRateJson(context, response);

            Calendar now = Calendar.getInstance();
            int year = now.get(Calendar.YEAR);
            int month = now.get(Calendar.MONTH) + 1;
            int day = now.get(Calendar.DAY_OF_MONTH);
//            update_info = context.getString(R.string.str_update_info);

            String update_info_ch = String.format(context.getString(R.string.str_update_info_ch), year, month, day);
            String update_info_en = String.format(context.getString(R.string.str_update_info_en), getYMD(year, month, day));

            if (context.getResources().getConfiguration().locale.getCountry().equals("CN")) {
                update_info = update_info_ch;
            } else {
                update_info = update_info_en;
            }

            Utils.saveRateUpDateInfo(context, update_info_ch, update_info_en);
            Utils.saveUpdateTime(context);
        }
        return is_sucess;
    }

    public String getYMD(int year, int month, int day) {

        String str_mon = "";
        if (month < 10) {
            str_mon = "0" + month;
        } else {
            str_mon = "" + month;
        }
        String str_day = "";
        if (day < 10) {
            str_day = "0" + day;
        } else {
            str_day = "" + day;
        }

        return year + str_mon + str_day;

    }

}
