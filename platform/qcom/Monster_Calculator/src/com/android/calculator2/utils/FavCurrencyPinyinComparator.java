package com.android.calculator2.utils;

import java.util.Comparator;

import com.android.calculator2.exchange.bean.CurrencyBean;

public class FavCurrencyPinyinComparator implements Comparator<CurrencyBean> {

    public int compare(CurrencyBean o1, CurrencyBean o2) {
        return o1.pos > o2.pos ? 1 : -1;
    }
}