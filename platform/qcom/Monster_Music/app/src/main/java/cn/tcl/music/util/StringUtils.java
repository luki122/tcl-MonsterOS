package cn.tcl.music.util;

import android.text.TextUtils;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Pattern emailer = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    private static final ThreadLocal<SimpleDateFormat> dateFormater = new ThreadLocal() {
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };
    private static final String _BR = "<br/>";
    private static final ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal() {
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    public StringUtils() {
    }

    public static String subString(String str, int length) throws Exception {
        byte[] bytes = str.getBytes("Unicode");
        int n = 0;

        int i;
        for (i = 2; i < bytes.length && n < length; ++i) {
            if (i % 2 == 1) {
                ++n;
            } else if (bytes[i] != 0) {
                ++n;
            }
        }

        if (i % 2 == 1) {
            if (bytes[i - 1] != 0) {
                --i;
            } else {
                ++i;
            }
        }

        return new String(bytes, 0, i, "Unicode");
    }

    public static String toDBC(String input) {
        char[] c = input.toCharArray();

        for (int i = 0; i < c.length; ++i) {
            if (c[i] == 12288) {
                c[i] = 32;
            } else if (c[i] > '\uff00' && c[i] < '｟') {
                c[i] -= 'ﻠ';
            }
        }

        return new String(c);
    }

    public static long calculateWeiboLength(CharSequence c) {
        double len = 0.0D;

        for (int i = 0; i < c.length(); ++i) {
            char temp = c.charAt(i);
            if (temp > 0 && temp < 127) {
                len += 0.5D;
            } else {
                ++len;
            }
        }

        return Math.round(len);
    }

    public static String[] split(String str, String splitsign) {
        if (str != null && splitsign != null) {
            int index;
            ArrayList al;
            for (al = new ArrayList(); (index = str.indexOf(splitsign)) != -1; str = str.substring(index + splitsign.length())) {
                al.add(str.substring(0, index));
            }

            al.add(str);
            return (String[]) al.toArray(new String[0]);
        } else {
            return null;
        }
    }

    public static String replace(String from, String to, String source) {
        if (source != null && from != null && to != null) {
            StringBuffer bf = new StringBuffer("");
            boolean index = true;

            int index1;
            while ((index1 = source.indexOf(from)) != -1) {
                bf.append(source.substring(0, index1) + to);
                source = source.substring(index1 + from.length());
                source.indexOf(from);
            }

            bf.append(source);
            return bf.toString();
        } else {
            return null;
        }
    }

    public static String htmlencode(String str) {
        return str == null ? null : replace("\"", "&quot;", replace("<", "&lt;", str));
    }

    public static String htmldecode(String str) {
        return str == null ? null : replace("&quot;", "\"", replace("&lt;", "<", str));
    }

    public static String htmlshow(String str) {
        if (str == null) {
            return null;
        } else {
            str = replace("<", "&lt;", str);
            str = replace(" ", "&nbsp;", str);
            str = replace("\r\n", "<br/>", str);
            str = replace("\n", "<br/>", str);
            str = replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;", str);
            return str;
        }
    }

    public static String toLength(String str, int length) {
        if (str == null) {
            return null;
        } else if (length <= 0) {
            return "";
        } else {
            try {
                if (str.getBytes("GBK").length <= length) {
                    return str;
                }
            } catch (Exception var5) {
                ;
            }

            StringBuffer buff = new StringBuffer();
            int index = 0;

            for (length -= 3; length > 0; ++index) {
                char c = str.charAt(index);
                if (c < 128) {
                    --length;
                } else {
                    --length;
                    --length;
                }

                buff.append(c);
            }

            buff.append("...");
            return buff.toString();
        }
    }

    public static String getUrlFileName(String urlString) {
        String fileName = urlString.substring(urlString.lastIndexOf("/"));
        fileName = fileName.substring(1, fileName.length());
        if (fileName.equalsIgnoreCase("")) {
            Calendar c = Calendar.getInstance();
            fileName = String.valueOf(c.get(1)) + c.get(2) + c.get(5) + c.get(12);
        }

        return fileName;
    }

    public static String replaceSomeString(String str) {
        String dest = "";

        try {
            if (str != null) {
                str = str.replaceAll("\r", "");
                str = str.replaceAll("&gt;", ">");
                str = str.replaceAll("&ldquo;", "“");
                str = str.replaceAll("&rdquo;", "”");
                str = str.replaceAll("&#39;", "\'");
                str = str.replaceAll("&nbsp;", "");
                str = str.replaceAll("<br\\s*/>", "\n");
                str = str.replaceAll("&quot;", "\"");
                str = str.replaceAll("&lt;", "<");
                str = str.replaceAll("&lsquo;", "《");
                str = str.replaceAll("&rsquo;", "》");
                str = str.replaceAll("&middot;", "·");
                str = str.replace("&mdash;", "—");
                str = str.replace("&hellip;", "…");
                str = str.replace("&amp;", "×");
                str = str.replaceAll("\\s*", "");
                str = str.trim();
                str = str.replaceAll("<p>", "\n      ");
                str = str.replaceAll("</p>", "");
                str = str.replaceAll("<div.*?>", "\n      ");
                str = str.replaceAll("</div>", "");
                dest = str;
            }
        } catch (Exception var3) {
            ;
        }

        return dest;
    }

    public static String delHTMLTag(String htmlStr) {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>";
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>";
        String regEx_html = "<[^>]+>";
        Log.v("htmlStr", htmlStr);

        try {
            Pattern p_script = Pattern.compile(regEx_script, 2);
            Matcher m_script = p_script.matcher(htmlStr);
            htmlStr = m_script.replaceAll("");
            Pattern p_style = Pattern.compile(regEx_style, 2);
            Matcher m_style = p_style.matcher(htmlStr);
            htmlStr = m_style.replaceAll("");
            Pattern p_html = Pattern.compile(regEx_html, 2);
            Matcher m_html = p_html.matcher(htmlStr);
            htmlStr = m_html.replaceAll("");
        } catch (Exception var10) {
            ;
        }

        return htmlStr;
    }

    public static String delSpace(String str) {
        if (str != null) {
            str = str.replaceAll("\r", "");
            str = str.replaceAll("\n", "");
            str = str.replace(" ", "");
        }

        return str;
    }

    public static boolean isNotNull(String str) {
        return str != null && !"".equalsIgnoreCase(str.trim());
    }

    public static Date toDate(String sdate) {
        try {
            return ((SimpleDateFormat) dateFormater.get()).parse(sdate);
        } catch (ParseException var2) {
            return null;
        }
    }

    public static String friendly_time(String sdate) {
        Date time = toDate(sdate);
        if (time == null) {
            return "Unknown";
        } else {
            String ftime = "";
            Calendar cal = Calendar.getInstance();
            String curDate = ((SimpleDateFormat) dateFormater2.get()).format(cal.getTime());
            String paramDate = ((SimpleDateFormat) dateFormater2.get()).format(time);
            if (curDate.equals(paramDate)) {
                int lt1 = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000L);
                if (lt1 == 0) {
                    ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000L, 1L) + "分钟前";
                } else {
                    ftime = lt1 + "小时前";
                }

                return ftime;
            } else {
                long lt = time.getTime() / 86400000L;
                long ct = cal.getTimeInMillis() / 86400000L;
                int days = (int) (ct - lt);
                if (days == 0) {
                    int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000L);
                    if (hour == 0) {
                        ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000L, 1L) + "分钟前";
                    } else {
                        ftime = hour + "小时前";
                    }
                } else if (days == 1) {
                    ftime = "昨天";
                } else if (days == 2) {
                    ftime = "前天";
                } else if (days > 2 && days <= 10) {
                    ftime = days + "天前";
                } else if (days > 10) {
                    ftime = ((SimpleDateFormat) dateFormater2.get()).format(time);
                }

                return ftime;
            }
        }
    }

    public static String trimmy(String str) {
        String dest = "";
        if (str != null) {
            str = str.replaceAll("-", "");
            str = str.replaceAll("\\+", "");
            dest = str;
        }

        return dest;
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\r");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }

        return dest;
    }

    public static boolean isToday(String sdate) {
        boolean b = false;
        Date time = toDate(sdate);
        Date today = new Date();
        if (time != null) {
            String nowDate = ((SimpleDateFormat) dateFormater2.get()).format(today);
            String timeDate = ((SimpleDateFormat) dateFormater2.get()).format(time);
            if (nowDate.equals(timeDate)) {
                b = true;
            }
        }

        return b;
    }

    public static boolean isEmpty(String input) {
        if (input != null && !"".equals(input) && !"null".equalsIgnoreCase(input)) {
            for (int i = 0; i < input.length(); ++i) {
                char c = input.charAt(i);
                if (c != 32 && c != 9 && c != 13 && c != 10) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean isEmail(String email) {
        return email != null && email.trim().length() != 0 ? emailer.matcher(email).matches() : false;
    }

    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception var3) {
            return defValue;
        }
    }

    public static int toInt(Object obj) {
        return obj == null ? 0 : toInt(obj.toString(), 0);
    }

    public static String toString(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception var2) {
            return 0L;
        }
    }

    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception var2) {
            return false;
        }
    }

    public static boolean isHandset(String handset) {
        try {
            if (!handset.substring(0, 1).equals("1")) {
                return false;
            } else if (handset != null && handset.length() == 11) {
                String e = "^[0123456789]+$";
                Pattern regex = Pattern.compile(e);
                Matcher matcher = regex.matcher(handset);
                boolean isMatched = matcher.matches();
                return isMatched;
            } else {
                return false;
            }
        } catch (RuntimeException var5) {
            return false;
        }
    }

    public static boolean isChinese(String str) {
        Pattern pattern = Pattern.compile("[Α-￥]+$");
        return pattern.matcher(str).matches();
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public static boolean isDouble(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        return pattern.matcher(str).matches();
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isLenghtStrLentht(String text, int lenght) {
        return text.length() <= lenght;
    }

    public static boolean isSMSStrLentht(String text) {
        return text.length() <= 70;
    }

    public static boolean checkEmail(String email) {
        Pattern pattern = Pattern.compile("^\\w+([-.]\\w+)*@\\w+([-]\\w+)*\\.(\\w+([-]\\w+)*\\.)*[a-z]{2,3}$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isShareStrLentht(String text, int lenght) {
        return text.length() <= 120;
    }

    public static String getFileNameFromUrl(String url) {
        String extName = "";
        int index = url.lastIndexOf(63);
        if (index > 1) {
            extName = url.substring(url.lastIndexOf(46) + 1, index);
        } else {
            extName = url.substring(url.lastIndexOf(46) + 1);
        }

        String filename = hashKeyForDisk(url) + "." + extName;
        return filename;
    }

    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            e.update(key.getBytes());
            cacheKey = bytesToHexString(e.digest());
        } catch (NoSuchAlgorithmException var3) {
            cacheKey = String.valueOf(key.hashCode());
        }

        return cacheKey;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; ++i) {
            String hex = Integer.toHexString(255 & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }

            sb.append(hex);
        }

        return sb.toString();
    }

    public static byte[] getGBKBytes(String str) {
        if (str == null) {
            return null;
        } else {
            Object bytes = null;

            byte[] bytes1;
            try {
                bytes1 = str.getBytes("GBK");
            } catch (Exception var5) {
                try {
                    bytes1 = str.getBytes("gbk");
                } catch (Exception var4) {
                    bytes1 = str.getBytes();
                }
            }

            return bytes1;
        }
    }

    public static boolean startsWithIgnoreCase(String source, String prefix) {
        return source == prefix ? true : (source != null && prefix != null ? source.regionMatches(true, 0, prefix, 0, prefix.length()) : false);
    }

    public static String removeEmptyLines(String content) {
        if (TextUtils.isEmpty(content)) {
            return content;
        } else {
            Pattern pattern = Pattern.compile("[\\r,\\n]{2,}");
            Matcher matcher = pattern.matcher(content);
            return matcher.replaceAll("\n");
        }
    }

    public static boolean isLegalContent(String content) {
        if (TextUtils.isEmpty(content)) {
            return false;
        } else {
            Pattern pattern = Pattern.compile("\\s*");
            Matcher matcher = pattern.matcher(content);
            if (matcher.matches()) {
                int start = matcher.start();
                int end = matcher.end();
                if (end - start >= content.length()) {
                    return false;
                }
            }

            return true;
        }
    }

    public static boolean isNetworkUrl(String url) {
        return url != null && url.length() != 0 ? isHttpUrl(url) || isHttpsUrl(url) : false;
    }

    public static boolean isHttpUrl(String url) {
        return url != null && url.length() > 6 && url.substring(0, 7).equalsIgnoreCase("http://");
    }

    public static boolean isHttpsUrl(String url) {
        return url != null && url.length() > 7 && url.substring(0, 8).equalsIgnoreCase("https://");
    }

    public static String toJsString(String value) {
        if (value == null) {
            return "null";
        } else {
            StringBuilder out = new StringBuilder(1024);
            out.append("\"");
            int i = 0;

            for (int length = value.length(); i < length; ++i) {
                char c = value.charAt(i);
                switch (c) {
                    case '\b':
                        out.append("\\b");
                        break;
                    case '\t':
                        out.append("\\t");
                        break;
                    case '\n':
                        out.append("\\n");
                        break;
                    case '\f':
                        out.append("\\f");
                        break;
                    case '\r':
                        out.append("\\r");
                        break;
                    case '\"':
                    case '/':
                    case '\\':
                        out.append('\\').append(c);
                        break;
                    default:
                        if (c <= 31) {
                            out.append(String.format("\\u%04x", new Object[]{Integer.valueOf(c)}));
                        } else {
                            out.append(c);
                        }
                }
            }

            out.append("\"");
            return out.toString();
        }
    }
}
