package com.android.calendar.mst.legalholiday;

public interface LegalHoliday {

    public static final int DAY_TYPE_NORMAL = 0;
    public static final int DAY_TYPE_HOLIDAY = 1;
    public static final int DAY_TYPE_WORKDAY = 2;

    public int getDayType(int julianDay);
}
