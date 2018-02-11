/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

//import android.R;

public class PLFUtils {
    private static final String TAG = "Utils";
    private static final String PATH = "/custpack/plf/WeatherCN/";
    private static final String FILE = "isdm_WeatherCN_defaults.xml";

    /** Pattern to match any part of a domain */
    private final static String WILD_STRING = "*";
    /** Will match any, single character */
    private final static char WILD_CHARACTER = '?';
    private final static String DOMAIN_SEPARATOR = "\\.";

    /**
     * 1 indicate that we show the account preset, 0 indicate not show.
     */
    private static final String DISPLAY_ACCOUNT_PRESET = "1";
    /**
     * get isdm value which is bool
     *
     * @param mContext
     * @param def_name : the name of isdmID
     * @return
     */
    public static boolean getBoolean(Context mContext, String def_name) {
        Resources res = mContext.getResources();
        int id = res.getIdentifier(def_name, "bool", mContext.getPackageName());
        // get the native isdmID value
        boolean result = mContext.getResources().getBoolean(id);
        try {
            String bool_frameworks = getISDMString(new File(PATH + FILE), def_name, "bool");
            if (null != bool_frameworks) {
                result = Boolean.parseBoolean(bool_frameworks);
            }
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * get isdm value which is bool
     * 
     * @param mContext
     * @param def_name : the name of isdmID
     * @return
     */
    public static String getString(Context mContext, String def_name) {
        Resources res = mContext.getResources();
        int id = res.getIdentifier(def_name, "string", mContext.getPackageName());
        // get the native isdmID value
        String result = mContext.getResources().getString(id);
        try {
            String string_frameworks = getISDMString(new File(PATH + FILE), def_name, "string");
            if (null != string_frameworks) {
                result = string_frameworks;
            }
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * get isdm value which is integer
     *
     * @param mContext
     * @param def_name : the name of isdmID
     * @return
     */
    public static int getInteger(Context mContext, String def_name) {
        Resources res = mContext.getResources();
        int id = res.getIdentifier(def_name, "integer", mContext.getPackageName());
        // get the native isdmID value
        int result = mContext.getResources().getInteger(id);
        try {
            String bool_frameworks = getISDMString(new File(PATH + FILE), def_name, "integer");
            if (null != bool_frameworks) {
                result = Integer.parseInt(bool_frameworks);
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * parser the XML file to get the isdmID value
     * 
     * @param file : xml file
     * @param name : isdmID
     * @param type : isdmID type like bool and string
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static String getISDMString(File file, String name, String type)
            throws XmlPullParserException,
            IOException {
        if (!file.exists() || null == file) {
            return null;
        }
        String result = null;
        InputStream inputStream = new FileInputStream(file);
        XmlPullParser xmlParser = Xml.newPullParser();
        xmlParser.setInput(inputStream, "utf-8");

        int evtType = xmlParser.getEventType();
        boolean query_end = false;
        while (evtType != XmlPullParser.END_DOCUMENT && !query_end) {

            switch (evtType) {
                case XmlPullParser.START_TAG:

                    String start_tag = xmlParser.getAttributeValue(null, "name");
                    String start_type = xmlParser.getName();
                    if (null != start_tag && type.equals(start_type) && start_tag.equals(name)) {
                        result = xmlParser.nextText();
                        query_end = true;
                    }
                    break;

                case XmlPullParser.END_TAG:

                    break;

                default:
                    break;
            }
            // move to next node if not tail
            evtType = xmlParser.next();
        }
        inputStream.close();
        return result;
    }
}
