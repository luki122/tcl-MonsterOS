/******************************************************************************/
/*                                                               Date:09/2013 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2012 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  Jianglong Pan                                                   */
/*  Email  :  Jianglong.Pan@tcl-mobile.com                                    */
/*  Role   :  PHONE                                                           */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :                                                                */
/*  File     :                                                                */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 11/28/2013|Jianglong Pan         |FR-543831             |For TF DM DEV     */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.providers.telephony.tfdm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.provider.Telephony;
import android.R.interpolator;
import com.tct.libs.util.TLog;
import android.util.Xml;

import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import org.xmlpull.v1.XmlPullParserException;

public class TFAPNXmlHelper {

    private static final String TAG = "pjldev";

    private static final String TF_APNS_FILE = "tf_apns.xml";
    private static final String TF_APN_TAG = "TF_APN";

    public static final String COLUMN_APN_NEW = "apnnew";

    public static final String COLUMN_GID = "gid";
    public static final String COLUMN_TRF_CURRENT = "trf_current";
    public static final String COLUMN_LOCAL_UPDATE = "local_update";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_IS_STORED = "is_stored";

    public static final String[] PROJECTION = new String[] {
        Telephony.Carriers.NAME,
        Telephony.Carriers.NUMERIC,
        Telephony.Carriers.MCC,
        Telephony.Carriers.MNC,
        Telephony.Carriers.APN,
        Telephony.Carriers.USER,
        Telephony.Carriers.SERVER,
        Telephony.Carriers.PASSWORD,
        Telephony.Carriers.PROXY,
        Telephony.Carriers.PORT,
        Telephony.Carriers.MMSPROXY,
        Telephony.Carriers.MMSPORT,
        Telephony.Carriers.MMSC,
        Telephony.Carriers.AUTH_TYPE,
        Telephony.Carriers.TYPE,
        Telephony.Carriers.CURRENT,
        Telephony.Carriers.PROTOCOL,
        Telephony.Carriers.ROAMING_PROTOCOL,
        Telephony.Carriers.CARRIER_ENABLED,
        Telephony.Carriers.BEARER,
        Telephony.Carriers.MVNO_TYPE,
        Telephony.Carriers.MVNO_MATCH_DATA,
        Telephony.Carriers.BEARER_BITMASK,
        Telephony.Carriers.MTU,
        Telephony.Carriers.EDITED,
        COLUMN_GID,
        COLUMN_TRF_CURRENT,
        COLUMN_LOCAL_UPDATE,
        COLUMN_APN_NEW,
        COLUMN_ID,
    };

    public static final int INDEX_COLUMN_NAME = 0;
    public static final int INDEX_COLUMN_NUMERIC = 1;
    public static final int INDEX_COLUMN_MCC = 2;
    public static final int INDEX_COLUMN_MNC = 3;
    public static final int INDEX_COLUMN_APN = 4;
    public static final int INDEX_COLUMN_USER = 5;
    public static final int INDEX_COLUMN_SERVER = 6;
    public static final int INDEX_COLUMN_PASSWORD = 7;
    public static final int INDEX_COLUMN_PROXY = 8;
    public static final int INDEX_COLUMN_PORT = 9;
    public static final int INDEX_COLUMN_MMSPROXY = 10;
    public static final int INDEX_COLUMN_MMSPORT = 11;
    public static final int INDEX_COLUMN_MMSC = 12;
    public static final int INDEX_COLUMN_AUTH_TYPE = 13;
    public static final int INDEX_COLUMN_TYPE = 14;
    public static final int INDEX_COLUMN_CURRENT = 15;
    public static final int INDEX_COLUMN_PROTOCOL = 16;
    public static final int INDEX_COLUMN_ROAMING_PROTOCOL = 17;
    public static final int INDEX_COLUMN_CARRIER_ENABLED = 18;
    public static final int INDEX_COLUMN_BEARER = 19;
    public static final int INDEX_COLUMN_MVNO_TYPE = 20;
    public static final int INDEX_COLUMN_MVNO_MATCH_DATA = 21;
    public static final int INDEX_COLUMN_BEARER_BITMASK = 22;
    public static final int INDEX_COLUMN_MTU = 23;
    public static final int INDEX_COLUMN_EDITED = 24;
    public static final int INDEX_COLUMN_GID = 25;
    public static final int INDEX_COLUMN_TRF_CURRENT = 26;
    public static final int INDEX_COLUMN_LOCAL_UPDATE = 27;
    public static final int INDEX_COLUMN_APN_NEW = 28;
    public static final int INDEX_COLUMN_ID = 29;

    public synchronized void writeXml(Map<String, String> aMap) {
        File file = initXml();
        OutputStream out = null;

        try {
            out = new BufferedOutputStream(new FileOutputStream(file, true));

            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(out, "utf-8");
            //serializer.startDocument(null, true);
            serializer.startTag(null, TF_APN_TAG);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            Set<String> apnset = aMap.keySet();
            Iterator<String> iterator = apnset.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = aMap.get(key);
                serializer.startTag(null, key);
                if (value == null) {
                    TLog.d(TAG, key + " = " + value);
                    value = "";
                }
                serializer.text(value);
                serializer.endTag(null, key);
            }

            serializer.endTag(null, TF_APN_TAG);
            serializer.endDocument();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized Map<String, Map<String, String>> readXml() {
        Map<String, Map<String, String>> maplist = new HashMap<String, Map<String, String>>();

        File file = initXml();

        InputStream in = null;
        XmlPullParser parser;

        try {
            in = new BufferedInputStream(new FileInputStream(file));

            parser = Xml.newPullParser();
            parser.setInput(new BufferedInputStream(in), "utf-8");

            maplist = loadFromXml(parser);
        } catch (FileNotFoundException e) {
            TLog.d(TAG, "FileNotFoundException ex = " + e);
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            TLog.d(TAG, "XmlPullParserException ex = " + e);
            e.printStackTrace();
        } catch (IOException e) {
            TLog.d(TAG, "IOException ex = " + e);
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return maplist;
    }

    public synchronized File initXml() {
        File file = new File(getDataCollectionPath());
        if (!file.exists()) {
            try {
                file.createNewFile();
                Runtime.getRuntime().exec("chmod 777 " + getDataCollectionPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public synchronized void resetXml() {
        File file = new File(getDataCollectionPath());
        if (file.exists()) {
            file.delete();
        }
    }

    public void writeAPNInfo(Map<String, String> map) {
        writeXml(map);
    }

    public String getAbsPersistentPath() {
        return "/persist/tfdm/";
    }

    private String getDataCollectionPath() {
        String path = getAbsPersistentPath() + TF_APNS_FILE;
        return path;
    }

    private Map<String, Map<String, String>> loadFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        Map<String, Map<String, String>> maplist = new HashMap<String, Map<String, String>>();

        String APNID = "-1";
        Map<String, String> map = new HashMap<String, String>();
        int eventType = parser.getEventType();

        String[] nodes = PROJECTION;

        String nodeName = null;

        boolean success = true;

        while (eventType != XmlPullParser.END_DOCUMENT && success) {

            switch (eventType) {

                case XmlPullParser.START_DOCUMENT:
                    nodeName = null;
                    break;

                case XmlPullParser.START_TAG:

                    nodeName = parser.getName();

                    if (TF_APN_TAG.equals(nodeName)) {
                        TLog.d(TAG, "START_TAG map = " + map);
                    }

                    break;

                case XmlPullParser.TEXT:

                    if (COLUMN_ID.equals(nodeName)) {
                        APNID = parser.getText();
                        TLog.d(TAG, "APNID = " + APNID);
                    }

                    for (int i = 0; i < nodes.length; i++) {
                        if (nodes[i].equals(nodeName)) {
                            //TLog.d(TAG, "TEXT nodeName = " + nodeName);
                            //TLog.d(TAG, "TEXT parser.getText() = " + parser.getText());
                            map.put(nodeName, parser.getText());
                        }
                    }

                    nodeName = null;
                    break;

                case XmlPullParser.END_TAG:
                    nodeName = null;

                    if (TF_APN_TAG.equals(parser.getName())) {
                        for (int i = 0; i < nodes.length; i++) {
                            if (!map.containsKey(nodes[i])) {
                                //TLog.d(TAG, "END_TAG nodes[i] = " + nodes[i]);
                                map.put(nodes[i], "");
                            }
                        }

                        TLog.d(TAG, "END_TAG map = " + map);

                        maplist.put(APNID, map);
                        map = new HashMap<String, String>();
                    }
                    break;

            }

            eventType = parser.next();

        }
        return maplist;
    }
}
