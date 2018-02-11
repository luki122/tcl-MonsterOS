package com.android.calculator2.exchange.bean;

import android.content.Context;

public class CurrencyBean {
    public String currency_ch;
    public String currency_en;
    public String currency_code;
    public String name_first_pinying;
    public int flag_id;
    public int pos;//用于常用货币的排序 其他排序按拼音先后排

    public String getSortLetters() {

        return name_first_pinying;

    }

}
