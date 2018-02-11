package com.tcl.monster.fota.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Intent;

import com.tcl.monster.fota.FotaApp;
import com.tcl.monster.fota.listener.OnGotSpopsListener;
import com.tcl.monster.fota.model.DownloadInfo;
import com.tcl.monster.fota.model.Spop;
import com.tcl.monster.fota.model.UpdatePackageInfo;
import com.tcl.monster.fota.service.LogUploadService;

/**
 * This class is used to parse xml infomation during check state .
 * 
 * @author haijun.chen
 */
public class ResponseParser {

    public static final String TAG = ResponseParser.class.getSimpleName();

    /**
     * This method is used to parse input stream information in second phrase of
     * check .
     * 
     * @param xml
     * @return
     */
    public static DownloadInfo parsePreDownloadResponse(InputStream xml) {
        DownloadInfo info = null;
        DownloadInfo.FileInfo fileInfo = null;
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance()
                    .newPullParser();
            parser.setInput(xml, "UTF-8");

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        info = new DownloadInfo();
                        break;
                    case XmlPullParser.START_TAG:

                        if ("FILE".equals(nodeName)) {
                            fileInfo = new DownloadInfo.FileInfo();
                        }
                        if ("FILE_ID".equals(nodeName)) {
                            fileInfo.mFileId = parser.nextText();
                        }
                        if ("DOWNLOAD_URL".equals(nodeName)) {
                            fileInfo.mUrl = parser.nextText();
                        }

                        if ("SLAVE".equals(nodeName)) {
                            info.mServers.add(parser.nextText());
                        }

                        break;
                    case XmlPullParser.END_TAG:
                        if ("FILE".equals(nodeName)) {
                            if (fileInfo != null) {
                                info.mFiles.add(fileInfo);
                            }
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (xml != null) {
                xml.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * This method is used to parse input stream from server in first phrase of
     * check . Since the input stream contains spops information so we use a
     * listener to handle spop we got .
     * 
     * @param xml
     * @param listener
     * @return a UpdatePackageInfo contains update package information
     */
    public static UpdatePackageInfo parseCheckResponse(InputStream xml , OnGotSpopsListener listener){
        UpdatePackageInfo info = null;
        UpdatePackageInfo.UpdateFile file = null;
        List<Spop> mSpops = null;
        Spop spop = null;
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance()
                    .newPullParser();
            parser.setInput(xml, "UTF-8");

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        info = new UpdatePackageInfo();
					    break;
				    case XmlPullParser.START_TAG:
					
                        if ("UPDATE_DESC".equals(nodeName)) {
                            info.mUpdateDesc = parser.nextText();
                        }
                        if (FotaUtil.getCurrentLanguageType().equals(nodeName)) {
                            info.mUpdateDesc = parser.nextText();
                        }

                        if ("ENCODING_ERROR".equals(nodeName)) {
                            info.mEncodingError = parser.nextText();
                        }
                        if ("CUREF".equals(nodeName)) {
                            info.mCuref = parser.nextText();
                        }

                        if ("TYPE".equals(nodeName)) {
                            info.mType = parser.nextText();
                        }
                        if ("FV".equals(nodeName)) {
                            info.mFv = parser.nextText();
                        }
                        if ("TV".equals(nodeName)) {
                            try {
                                info.mTv = parser.nextText();
                            } catch (Exception e) {
                            }
                        }

                        if ("SVN".equals(nodeName)) {
                            info.mSvn = parser.nextText();
                        }
                        if ("year".equals(nodeName)) {
                            info.mReleaseYear = parser.nextText();
                        }
                        if ("FW_ID".equals(nodeName)) {
                            info.mFirmwareId = parser.nextText();
                        }

                        if ("FILESET_COUNT".equals(nodeName)) {
                            info.mFileCount = Integer.parseInt(parser.nextText());
                        }
                        if ("FILE".equals(nodeName)) {
                            file = new UpdatePackageInfo.UpdateFile();
                        }
                        if ("FILENAME".equals(nodeName)) {
                            file.mFileName = parser.nextText();
                        }
                        if ("FILE_ID".equals(nodeName)) {
                            file.mFileId = parser.nextText();
                        }
                        if ("SIZE".equals(nodeName)) {
                            try {
                                file.mFileSize = Long.parseLong(parser.nextText().trim());
                            } catch (Exception e) {
                            }
                        }
                        if ("CHECKSUM".equals(nodeName)) {
                            file.mCheckSum = parser.nextText();
                        }

                        if ("FILE_VERSION".equals(nodeName)) {
                            file.mFileVersion = parser.nextText();
                        }

                        if ("INDEX".equals(nodeName)) {
                            file.mFileIndex = parser.nextText();
                        }
                        if ("SPOP_LIST".equals(nodeName)) {
                            mSpops = new ArrayList<Spop>();
                        }

                        if ("SPOP".equals(nodeName)) {
                            spop = new Spop();
                        }
                        if ("SPOP_TYPE".equals(nodeName)) {
                            spop.type = Integer.parseInt(parser.nextText().trim());
                        }
                        if ("SPOP_DATA".equals(nodeName)) {
                            spop.data = parser.nextText().trim();
                        }
					    if("HANDSET_LOG".equals(nodeName)){
						Intent i = new Intent(FotaApp.getApp() , LogUploadService.class);
						i.setAction(LogUploadService.ACTION_UPLOAD_LOG);
						FotaApp.getApp().startService(i);
                        }

                        break;
                    case XmlPullParser.END_TAG:
                        if ("FILE".equals(nodeName)) {
                            if (file != null) {
                                info.mFiles.add(file);
                            }
                        }
                        if ("SPOP".equals(nodeName)) {
                            mSpops.add(spop);
                        }
                        if ("SPOP_LIST".equals(nodeName)) {
                            if (listener != null) {
                                listener.onGotSpops(mSpops);
                            }
                        }

                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (xml != null) {
                xml.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return info;
    }

    public static boolean parseRootUpdate(String str) throws JSONException {
        JSONObject jsonObject = new JSONObject(str);
        jsonObject = jsonObject.getJSONObject("data");
        return jsonObject.getBoolean("root");
    }

    public static int parseCheckTimeBegin(String str) throws JSONException {
        JSONObject jsonObject = new JSONObject(str);
        jsonObject = jsonObject.getJSONObject("data");
        return jsonObject.getInt("begin");
    }

    public static int parseCheckTimeEnd(String str) throws JSONException {
        JSONObject jsonObject = new JSONObject(str);
        jsonObject = jsonObject.getJSONObject("data");
        return jsonObject.getInt("end");
    }

    public static int parseRemindCount(String str) throws JSONException {
        JSONObject jsonObject = new JSONObject(str);
        jsonObject = jsonObject.getJSONObject("data");
        return jsonObject.getInt("count");
    }

    public static int parseMaxWifiDownloadSize(String str) throws JSONException {
        JSONObject jsonObject = new JSONObject(str);
        jsonObject = jsonObject.getJSONObject("data");
        return jsonObject.getInt("size");
    }

    public static boolean parseClearStatus(String str) throws JSONException {
        JSONObject jsonObject = new JSONObject(str);
        jsonObject = jsonObject.getJSONObject("data");
        return jsonObject.getBoolean("clear");
    }

    public static boolean parseCheckWifiOnly(String str) throws JSONException {
        JSONObject jsonObject = new JSONObject(str);
        jsonObject = jsonObject.getJSONObject("data");
        return jsonObject.getBoolean("check");
    }

    public static boolean parseDownloadWifiOnly(String str) throws JSONException {
        JSONObject jsonObject = new JSONObject(str);
        jsonObject = jsonObject.getJSONObject("data");
        return jsonObject.getBoolean("download");
    }

    public static boolean parseAutoDownload(String str) throws JSONException {
        JSONObject jsonObject = new JSONObject(str);
        jsonObject = jsonObject.getJSONObject("data");
        jsonObject = jsonObject.getJSONObject("type_data");
        return jsonObject.getString("download").endsWith("auto");
    }

    public static boolean parseAutoInstall(String str) throws JSONException {
        JSONObject jsonObject = new JSONObject(str);
        jsonObject = jsonObject.getJSONObject("data");
        jsonObject = jsonObject.getJSONObject("type_data");
        return jsonObject.getString("install").endsWith("auto");
    }

    public static int parsePriority(String str) throws JSONException {
        JSONObject jsonObject = new JSONObject(str);
        jsonObject = jsonObject.getJSONObject("data");
        if (jsonObject.getString("type").equals("optional")) {
            return 1;
        } else {
            return 0;
        }
    }

    public static boolean parseLogUpload(String str) throws JSONException {
        JSONObject jsonObject = new JSONObject(str);
        return jsonObject.getBoolean("log");
    }

    public static int parseCheckPeriod(String str) {
        int period = 86400;
        try {
            JSONObject jsonObject = new JSONObject(str);
            jsonObject = jsonObject.getJSONObject("data");

            period = jsonObject.getInt("time");
            FotaLog.d(TAG, "parseCheckPeriod -> period = " + period);
            /*
             * if (period==86400) { return 1; } else if(period==604800){ return
             * 7; } else if(period==1209600){ return 14; } else
             * if(period==2419200){ return 30; } else if(period==64800){ return
             * 18; } else if(period==0){ return 0; }
             */} catch (Exception e) {
            FotaLog.v(TAG, e.toString());
        }
        return period;
    }

    public static int parseRemindPeriod(String str) {
        int period = 7200;
        try {
            JSONObject jsonObject = new JSONObject(str);
            jsonObject = jsonObject.getJSONObject("data");

            period = jsonObject.getInt("reminder");

            if (period == 3600) {
                return 1;
            }
            else if (period == 7200) {
                return 2;
            }
            else if (period == 10800) {
                return 3;
            }
            else if (period == 21600) {
                return 6;
            }
            else if (period == 43200) {
                return 12;
            }
            else if (period == 86400) {
                return 24;
            }
        } catch (Exception e) {
            FotaLog.v(TAG, e.toString());
        }

        return 1;
    }
}
