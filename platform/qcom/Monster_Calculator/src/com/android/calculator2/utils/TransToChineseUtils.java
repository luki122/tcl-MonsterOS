package com.android.calculator2.utils;

import java.text.DecimalFormat;

public class TransToChineseUtils {
    private static final String digit_0 = "零壹贰叁肆伍陆柒捌玖";
    private static final String digit_1 = "零一二三四五六七八九";
    private static final String[] digit_2 = {"", "十", "百", "千"};
    private static final String[] digit_3 = {"", "拾", "佰", "仟"};
    private static final String[] digit_4 = {"", "万", "亿", "万亿", "亿亿"};

    /**
     * Description: 数字转化成整数
     *
     * @param str
     * @param bo
     * @return
     */
    public static String changeDigit(String str, boolean bo) {

        if(str.equals("0")){
            return "零";
        }

        StringBuffer strbu = new StringBuffer();
        int dou = str.indexOf(".");
        // :判断是否为小数还是整数，长度小于零为整数
        if (dou < 0) {
            dou = str.length();
        }
        // :获取整数部分
        String inter = str.substring(0, dou);
        strbu.append(changeInteger(Long.parseLong(inter), bo));
        // :处理小数部分
        if (dou != str.length()) {
            strbu.append("点");
            // :获取小数点后所有数
            String xh = str.substring(dou + 1);
            for (int i = 0; i < xh.length(); i++) {
                if (bo) {
                    strbu.append(digit_0.charAt(Integer.parseInt(xh.substring(
                            i, i + 1))));
                } else {
                    strbu.append(digit_1.charAt(Integer.parseInt(xh.substring(
                            i, i + 1))));
                }
            }
        }
        String strs = strbu.toString();
        // :处理特殊情况，可能不全
        if (strs.startsWith("零")) {
            strs = strs.substring(1);
        }
        if (strs.startsWith("一十")) {
            strs = strs.substring(1);
        }
        while (strs.endsWith("零")) {
            strs = strs.substring(0, strs.length() - 1);
        }
        if (strs.startsWith("点")) {
            strs = "零" + strs;
        }
        if (strs.endsWith("点")) {
            strs = strs.substring(0, strs.length() - 1);
        }
        return strs;
    }


    /**
     * 位数小于4时，调用处理数据
     *
     * @param str
     * @param bo
     * @return
     */
    public static String readNumber(String str, boolean bo) {
        StringBuffer strbu = new StringBuffer();
        if (str.length() != 4) {
            return null;
        }
        for (int i = 0; i < 4; i++) {
            char ch = str.charAt(i);
            if (ch == '0' && i > 1 && str.charAt(i - 1) == '0') {
                continue;
            }
            if (ch != '0' && i > 1 && str.charAt(i - 1) == '0') {
                strbu.append('零');
            }
            if (ch != '0') {
                if (bo) {
                    strbu.append(digit_0.charAt(ch - 48));
                    strbu.append(digit_3[4 - i - 1]);
                } else {
                    strbu.append(digit_1.charAt(ch - 48));
                    strbu.append(digit_2[4 - i - 1]);
                }
            }
        }
        return strbu.toString();
    }


    /**
     * 整数部分转换大写
     *
     * @param lon
     * @param bo
     * @return
     */
    public static String changeInteger(long lon, boolean bo) {
        StringBuffer strbu = new StringBuffer();
        // :增加3位数,为了完成大写转换
        String strN = "000" + lon;
        int strN_L = strN.length() / 4;
        // :根据不同的位数长度，消除strN"0"的个数
        strN = strN.substring(strN.length() - strN_L * 4);
        for (int i = 0; i < strN_L; i++) {
            String s1 = strN.substring(i * 4, i * 4 + 4);
            String s2 = readNumber(s1, bo);
            strbu.append(s2);
            if (s2.length() != 0) {
                strbu.append(digit_4[strN_L - i - 1]);
            }
        }
        String s = new String(strbu);
        if (s.length() != 0 && s.startsWith("零"))
            s = s.substring(1);
        return s;
    }


    /**
     * 人名币输出格式
     *
     * @param rmb
     * @return
     */
    public static String changeNumberRMB(double rmb) {
        String strRMB = "" + rmb;
        DecimalFormat df = new DecimalFormat("#.#");
        df.setMaximumFractionDigits(2);
        strRMB = df.format(rmb).toString();
        // :true人民币大写，并返回数据
        strRMB = changeDigit(strRMB, true);
        if (strRMB.indexOf("点") >= 0) {
            strRMB = strRMB + "零";
            strRMB = strRMB.replaceAll("点", "圆");
            String str1 = strRMB.substring(0, strRMB.indexOf("圆") + 1);
            String str2 = strRMB.substring(strRMB.indexOf("圆") + 1);
            strRMB = str1 + str2.charAt(0) + "角" + str2.charAt(1) + "分整";
        } else {
            strRMB = strRMB + "圆整";
        }
        return "人民币(大写):" + strRMB;
    }

}
