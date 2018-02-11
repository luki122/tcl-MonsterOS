package com.android.calculator2.utils;

import android.content.Context;

import java.util.Comparator;

import com.android.calculator2.exchange.bean.CurrencyBean;


public class EmployeePinyinComparator implements Comparator<CurrencyBean> {
    
    private String star;

    public EmployeePinyinComparator(String star_letter){
        star = star_letter;
    }

    public int compare(CurrencyBean o1, CurrencyBean o2) {
        if (o1.getSortLetters().equals("@") || o2.getSortLetters().equals(star)) {
            return 1;
        } else if (o1.getSortLetters().equals(star) || o2.getSortLetters().equals("@")) {
            return -1;
        } else if (o1.getSortLetters().equals("@") || o2.getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortLetters().equals("#") || o2.getSortLetters().equals("@")) {
            return 1;
        } else {
            return o1.getSortLetters().compareTo(o2.getSortLetters());
        }
    }

}
