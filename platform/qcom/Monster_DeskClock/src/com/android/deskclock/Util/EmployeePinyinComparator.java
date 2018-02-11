package com.android.deskclock.Util;

import java.util.Comparator;

import com.android.deskclock.worldclock.CityObj;

public class EmployeePinyinComparator implements Comparator<CityObj> {

    public int compare(CityObj o1, CityObj o2) {
        if (o1.getSortLetters().equals("@") || o2.getSortLetters().equals("★")) {
            return 1;
        } else if (o1.getSortLetters().equals("★") || o2.getSortLetters().equals("@")) {
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
