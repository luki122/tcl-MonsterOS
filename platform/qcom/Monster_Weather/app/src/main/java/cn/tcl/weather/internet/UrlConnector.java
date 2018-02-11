package cn.tcl.weather.internet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import cn.tcl.weather.utils.LogUtils;

public class UrlConnector{
    public Context mContext;
    public String TAG = "UrlConnector";

    public UrlConnector(Context context){
        mContext = context;
    }

    public void loadUrl(String url, String areaID, String partnerCode){
        String realUrl = url.replace("areaID", areaID);
        realUrl = realUrl.replace("partenerCode", partnerCode);

        Uri uri = Uri.parse(realUrl);
        Intent newIntent = new Intent(Intent.ACTION_VIEW, uri);

        try{
            mContext.startActivity(newIntent);
        }catch (Exception e){
            LogUtils.e(TAG, e.getMessage(), e);
        }
    }
}
