/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONObjectUtils {

    public static int getResultCode(JSONObject result) {
        if (result == null) {
            return -1;
        }
        try {
            return result.getInt("ret_code");
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getResultError(JSONObject result) {
        if (result == null) {
            return "";
        }
        try {
            return result.getString("err_msg");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static JSONObject getResultObject(JSONObject result) {
        if (result == null) {
            return null;
        }
        try {
            return result.getJSONObject("result");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
