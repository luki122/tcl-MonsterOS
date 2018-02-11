package com.mst.wallpaper.utils;

import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.format.Time;


public class LunarUtils {
    public Time mSolarTime;
    public int day;
    public int curMonthDays;
    public int month;
    public int year;
    public boolean isLeap;
    public int mCurMonthTerm1;
    public int mCurMonthTerm2;

    private static final int MINYEAR = 1901;
    private Context mContext;

    int[] lunarInfo = { 0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950,
            0x16554, 0x056a0, 0x09ad0, 0x055d2, 0x04ae0, 0x0a5b6, 0x0a4d0,
            0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
            0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60,
            0x09570, 0x052f2, 0x04970, 0x06566, 0x0d4a0, 0x0ea50, 0x06e95,
            0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0,
            0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2,
            0x0a950, 0x0b557, 0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0,
            0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0, 0x0aea6, 0x0ab50,
            0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57,
            0x056a0, 0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250,
            0x0d558, 0x0b540, 0x0b6a0, 0x195a6, 0x095b0, 0x049b0, 0x0a974,
            0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
            0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0,
            0x0ab60, 0x096d5, 0x092e0, 0x0c960, 0x0d954, 0x0d4a0, 0x0da50,
            0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, 0x0a950,
            0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176,
            0x052b0, 0x0a930, 0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60,
            0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, 0x05aa0, 0x076a3,
            0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520,
            0x0dd45, 0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0,
            0x0aa50, 0x1b255, 0x06d20, 0x0ada0, 0x14b63 };

    int[] sTermInfo = { 0, 21208, 42457, 63806, 85297, 106944, 128777, 150811,
            173019, 195400, 217902, 240498, 263152, 285789, 308393, 330853,
            353200, 375364, 397347, 419120, 440735, 462194, 483512, 504758 };
    char[] Gan = { 0x7532, 0x4E59, 0x4E19, 0x4E01, 0x620A, 0x5DF1, 0x5E9A,
            0x8F9B, 0x58EC, 0x7678 };
    char[] tw_Gan = { 0x7532, 0x4E59, 0x4E19, 0x4E01, 0x620A, 0x5DF1, 0x5E9A,
            0x8F9B, 0x58EC, 0x7678 };
    char[] Zhi = { 0x5B50, 0x4E11, 0x5BC5, 0x536F, 0x8FB0, 0x5DF3, 0x5348,
            0x672A, 0x7533, 0x9149, 0x620C, 0x4EA5 };
    char[] tw_Zhi = { 0x5B50, 0x4E11, 0x5BC5, 0x536F, 0x8FB0, 0x5DF3, 0x5348,
            0x672A, 0x7533, 0x9149, 0x620C, 0x4EA5 };
    char[] Animals = { 0x9F20, 0x725B, 0x864E, 0x5154, 0x9F99, 0x86C7, 0x9A6C,
            0x7F8A, 0x7334, 0x9E21, 0x72D7, 0x732A };
    char[] tw_Animals = { 0x9F20, 0x725B, 0x864E, 0x5154, 0x9F8D, 0x86C7,
            0x99AC, 0x7F8A, 0x7334, 0x96DE, 0x72D7, 0x8C6C };

    char[] solarTerm = { 0x5C0F, 0x5BD2, 0x5927, 0x5BD2, 0x7ACB, 0x6625,
            0x96E8, 0x6C34, 0x60CA, 0x86F0, 0x6625, 0x5206, 0x6E05, 0x660E,
            0x8C37, 0x96E8, 0x7ACB, 0x590F, 0x5C0F, 0x6EE1, 0x8292, 0x79CD,
            0x590F, 0x81F3, 0x5C0F, 0x6691, 0x5927, 0x6691, 0x7ACB, 0x79CB,
            0x5904, 0x6691, 0x767D, 0x9732, 0x79CB, 0x5206, 0x5BD2, 0x9732,
            0x971C, 0x964D, 0x7ACB, 0x51AC, 0x5C0F, 0x96EA, 0x5927, 0x96EA,
            0x51AC, 0x81F3 };

    char[] tw_solarTerm = { 0x5C0F, 0x5BD2, 0x5927, 0x5BD2, 0x7ACB, 0x6625,
            0x96E8, 0x6C34, 0x9A5A, 0x87C4, 0x6625, 0x5206, 0x6E05, 0x660E,
            0x7A40, 0x96E8, 0x7ACB, 0x590F, 0x5C0F, 0x6EFF, 0x8292, 0x7A2E,
            0x590F, 0x81F3, 0x5C0F, 0x6691, 0x5927, 0x6691, 0x7ACB, 0x79CB,
            0x8655, 0x6691, 0x767D, 0x9732, 0x79CB, 0x5206, 0x5BD2, 0x9732,
            0x971C, 0x964D, 0x7ACB, 0x51AC, 0x5C0F, 0x96EA, 0x5927, 0x96EA,
            0x51AC, 0x81F3 };
    char[] dateStr1 = { 0x6B63, 0x4E00, 0x4E8C, 0x4E09, 0x56DB, 0x4E94, 0x516D,
            0x4E03, 0x516B, 0x4E5D, 0x5341, 0x51AC, 0x814A };

    char[] tw_dateStr1 = { 0x6B63, 0x4E00, 0x4E8C, 0x4E09, 0x56DB, 0x4E94,
            0x516D, 0x4E03, 0x516B, 0x4E5D, 0x5341, 0x51AC, 0x81D8 };

    char[] dateStr2 = { 0x521D, 0x5341, 0x5EFF, 0x5345, 0x25A1 };
    char[] tw_dateStr2 = { 0x521D, 0x5341, 0x5EFF, 0x5345, 0x25A1 };

    char[] yearMonthDay = { 0x95F0, 0x5E74, 0x6708, 0x0020 };

    char[] tw_yearMonthDay = { 0x958F, 0x5E74, 0x6708, 0x0020 };
    char[] lFtv = { 0x6625, 0x8282, 0x521D, 0x4E8C, 0x5143, 0x5BB5, 0x7AEF,
            0x5348, 0x4E03, 0x5915, 0x4E2D, 0x5143, 0x4E2D, 0x79CB, 0x91CD,
            0x9633, 0x814A, 0x516B, 0x5C0F, 0x5E74, 0x9664, 0x5915 };

    char[] tw_lFtv = { 0x6625, 0x7BC0, 0x521D, 0x4E8C, 0x5143, 0x5BB5, 0x7AEF,
            0x5348, 0x4E03, 0x5915, 0x4E2D, 0x5143, 0x4E2D, 0x79CB, 0x91CD,
            0x967D, 0x81D8, 0x516B, 0x5C0F, 0x5E74, 0x9664, 0x5915 };

    public LunarUtils(Context context) {
        mContext = context;
    }

    public LunarUtils(Time solarTime, Context context) {
        mContext = context;
        SetSolarDate(solarTime);
    }

    int lYearDays(int y) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            sum += ((lunarInfo[y - 1900] & i) > 0) ? 1 : 0;
        }
        return (sum + leapDays(y));
    }

    int leapDays(int y) {
        if (leapMonth(y) > 0) {
            return (((lunarInfo[y - 1900] & 0x10000) > 0) ? 30 : 29);
        } else {
            return (0);
        }
    }

    int leapMonth(int y) {
        return (lunarInfo[y - 1900] & 0xf);
    }

    int monthDays(int y, int m) {
        return (((lunarInfo[y - 1900] & (0x10000 >> m)) > 0) ? 30 : 29);
    }

    int sTerm(int y, int n) {
        Time solarTime = new Time();
        if (n > sTermInfo.length || n < 0)
            return -1;
        solarTime.normalize(true);
        solarTime
                .set((long) (31556976080l * (y - 1900) + (long) sTermInfo[n] * 60000)
                        + Date.UTC(0, 0, 6, 2, 13, 0) - solarTime.gmtoff * 1000);

        return (solarTime.monthDay);
    }
 
   public void SetSolarDate(Time solarTime) {

        int i, leap = 0, temp = 0;
        if (solarTime.year > solarTime.getActualMaximum(Time.YEAR)
                || solarTime.year < MINYEAR) {
            solarTime.year = MINYEAR;
            solarTime.month = 1;
            solarTime.monthDay = 1;
        }
        mSolarTime = new Time(solarTime);

        int offset = (int) ((long) (Date.UTC(solarTime.year - 1900,
                solarTime.month, solarTime.monthDay, 0, 0, 0) - Date.UTC(0, 0,
                31, 0, 0, 0)) / (long) 86400000);

        for (i = 1900; i < 2050 && offset > 0; i++) {
            temp = lYearDays(i);
            offset -= temp;
        }

        if (offset < 0) {
            offset += temp;
            i--;
        }

        year = i;

        leap = leapMonth(i);
        isLeap = false;

        for (i = 1; i < 13 && offset > 0; i++) {
            if (leap > 0 && i == (leap + 1) && isLeap == false) {
                --i;
                isLeap = true;
                temp = leapDays(year);
            } else {
                temp = monthDays(year, i);
            }

            if (isLeap == true && i == (leap + 1)) {
                isLeap = false;
            }
            offset -= temp;
        }

        if (offset == 0 && leap > 0 && i == leap + 1) {
            if (isLeap) {
                isLeap = false;
            } else {
                isLeap = true;
                --i;
            }
        }

        if (offset < 0) {
            offset += temp;
            --i;
        }

        if (isLeap) {
            curMonthDays = leapDays(year);
        } else {
            curMonthDays = monthDays(year, i);
        }
        month = i;
        day = offset + 1;

        mCurMonthTerm1 = sTerm(mSolarTime.year, mSolarTime.month * 2);
        mCurMonthTerm2 = sTerm(mSolarTime.year, mSolarTime.month * 2 + 1);
    }

    public void GotoNextDay() {
        if (++day > curMonthDays) {
            day = 1;
            if (++month > 12) {
                month = 1;
                if (++year > 150) {
                }
            }
            int leap = leapMonth(year);
            if (leap > 0) {
                if (month == leap + 1) {
                    if (isLeap) {
                        isLeap = false;
                    } else {
                        month--;
                        isLeap = true;
                    }
                }
            } else {
                isLeap = false;
            }
            if (isLeap) {
                curMonthDays = leapDays(year);
            } else {
                curMonthDays = monthDays(year, month);
            }
        }
        mSolarTime.monthDay++;
        mSolarTime.normalize(true);
        if (mSolarTime.monthDay == 1) {
            mCurMonthTerm1 = sTerm(mSolarTime.year, mSolarTime.month * 2);
            mCurMonthTerm2 = sTerm(mSolarTime.year, mSolarTime.month * 2 + 1);
        }
    }

    public String GetLunarDateString() {

        Resources resources = mContext.getResources();
        Configuration config = resources.getConfiguration();

        StringBuffer sb = new StringBuffer();
        sb.setLength(4);

        if (config.locale.equals(Locale.TAIWAN)) {
            sb.setCharAt(0, tw_yearMonthDay[3]);
            if (mSolarTime.monthDay == mCurMonthTerm1) {
                sb.setCharAt(1, tw_solarTerm[mSolarTime.month * 4]);
                sb.setCharAt(2, tw_solarTerm[mSolarTime.month * 4 + 1]);
            } else if (mSolarTime.monthDay == mCurMonthTerm2) {
                sb.setCharAt(1, tw_solarTerm[mSolarTime.month * 4 + 2]);
                sb.setCharAt(2, tw_solarTerm[mSolarTime.month * 4 + 3]);
            } else if (day == 1) {
                if (isLeap) {
                    sb.setCharAt(0, tw_yearMonthDay[0]);
                }
                if (month == 1) {
                    sb.setCharAt(1, tw_dateStr1[0]);
                } else {
                    sb.setCharAt(1, tw_dateStr1[month]);
                }
                sb.setCharAt(2, tw_yearMonthDay[2]);
            } else if (day % 10 == 0) {
                if (10 == day) {
                    sb.setCharAt(1, tw_dateStr2[0]);
                } else {
                    sb.setCharAt(1, tw_dateStr1[day / 10]);
                }
                sb.setCharAt(2, tw_dateStr1[10]);
            } else {
                sb.setCharAt(1, tw_dateStr2[day / 10]);
                sb.setCharAt(2, tw_dateStr1[day % 10]);
            }
        } else {

            sb.setCharAt(0, yearMonthDay[3]);
            if (mSolarTime.monthDay == mCurMonthTerm1) {
                sb.setCharAt(1, solarTerm[mSolarTime.month * 4]);
                sb.setCharAt(2, solarTerm[mSolarTime.month * 4 + 1]);
            } else if (mSolarTime.monthDay == mCurMonthTerm2) {
                sb.setCharAt(1, solarTerm[mSolarTime.month * 4 + 2]);
                sb.setCharAt(2, solarTerm[mSolarTime.month * 4 + 3]);
            } else if (day == 1) {
                if (isLeap) {
                    sb.setCharAt(0, yearMonthDay[0]);
                }
                if (month == 1) {
                    sb.setCharAt(1, dateStr1[0]);
                } else {
                    sb.setCharAt(1, dateStr1[month]);
                }
                sb.setCharAt(2, yearMonthDay[2]);
            } else if (day % 10 == 0) {
                if (10 == day) {
                    sb.setCharAt(1, dateStr2[0]);
                } else {
                    sb.setCharAt(1, dateStr1[day / 10]);
                }
                sb.setCharAt(2, dateStr1[10]);
            } else {
                sb.setCharAt(1, dateStr2[day / 10]);
                sb.setCharAt(2, dateStr1[day % 10]);
            }
        }
        return sb.toString();
    }

    public String GetLunarNYRString() {

        Resources resources = mContext.getResources();
        Configuration config = resources.getConfiguration();

        int yOffset = year - 1900 + 36;
        StringBuffer sb = new StringBuffer();
        sb.setLength(12);

        if (config.locale.equals(Locale.TAIWAN)) {
            sb.setCharAt(0, tw_Gan[yOffset % 10]);
            sb.setCharAt(1, tw_Zhi[yOffset % 12]);
            sb.setCharAt(2, tw_yearMonthDay[1]);
            sb.setCharAt(3, tw_yearMonthDay[3]);
            if (isLeap) {
                sb.setCharAt(4, tw_yearMonthDay[0]);
            } else {
                sb.setCharAt(4, tw_yearMonthDay[3]);
            }

            if (month == 1) {
                sb.setCharAt(5, tw_dateStr1[0]);
            } else {
                sb.setCharAt(5, tw_dateStr1[month]);
            }
            sb.setCharAt(6, tw_yearMonthDay[2]);

            if (day % 10 == 0) {
                if (10 == day) {
                    sb.setCharAt(7, tw_dateStr2[0]);
                } else {
                    sb.setCharAt(7, tw_dateStr1[day / 10]);
                }
                sb.setCharAt(8, tw_dateStr1[10]);
            } else {
                sb.setCharAt(7, tw_dateStr2[day / 10]);
                sb.setCharAt(8, tw_dateStr1[day % 10]);
            }

            if (mSolarTime.monthDay == mCurMonthTerm1) {
                sb.setCharAt(9, tw_solarTerm[mSolarTime.month * 4]);
                sb.setCharAt(10, tw_solarTerm[mSolarTime.month * 4 + 1]);
            } else if (mSolarTime.monthDay == mCurMonthTerm2) {
                sb.setCharAt(9, tw_solarTerm[mSolarTime.month * 4 + 2]);
                sb.setCharAt(10, tw_solarTerm[mSolarTime.month * 4 + 3]);
            } else {
                sb.setCharAt(9, tw_yearMonthDay[3]);
                sb.setCharAt(10, tw_yearMonthDay[3]);
            }

        } else {

            sb.setCharAt(0, Gan[yOffset % 10]);
            sb.setCharAt(1, Zhi[yOffset % 12]);
            sb.setCharAt(2, yearMonthDay[1]);
            sb.setCharAt(3, yearMonthDay[3]);
            if (isLeap) {
                sb.setCharAt(4, yearMonthDay[0]);
            } else {
                sb.setCharAt(4, yearMonthDay[3]);
            }

            if (month == 1) {
                sb.setCharAt(5, dateStr1[0]);
            } else {
                sb.setCharAt(5, dateStr1[month]);
            }
            sb.setCharAt(6, yearMonthDay[2]);

            if (day % 10 == 0) {
                if (10 == day) {
                    sb.setCharAt(7, dateStr2[0]);
                } else {
                    sb.setCharAt(7, dateStr1[day / 10]);
                }
                sb.setCharAt(8, dateStr1[10]);
            } else {
                sb.setCharAt(7, dateStr2[day / 10]);
                sb.setCharAt(8, dateStr1[day % 10]);
            }

            if (mSolarTime.monthDay == mCurMonthTerm1) {
                sb.setCharAt(9, solarTerm[mSolarTime.month * 4]);
                sb.setCharAt(10, solarTerm[mSolarTime.month * 4 + 1]);
            } else if (mSolarTime.monthDay == mCurMonthTerm2) {
                sb.setCharAt(9, solarTerm[mSolarTime.month * 4 + 2]);
                sb.setCharAt(10, solarTerm[mSolarTime.month * 4 + 3]);
            } else {
                sb.setCharAt(9, yearMonthDay[3]);
                sb.setCharAt(10, yearMonthDay[3]);
            }
        }
        return sb.toString();
    }
}
