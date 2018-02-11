package com.android.calculator2.exchange.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class RateBean {
    public String n1;
    public String n2;
    public float r;
    
    public void fromJson(JSONObject jsonObject){
        if (null == jsonObject) {
            return;
        }
        try {
            n1 = jsonObject.getString("n1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            n2 = jsonObject.getString("n2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            r =  Float.parseFloat(jsonObject.getString("r"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
    }

}
