package cn.com.xy.sms.sdk.ui.notification;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;

public class DoActionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.i("DoActionActivity", "DoActionActivity is onCreated...");
        
        String actionData = getIntent().getStringExtra("action_data"); 
        int notificationId = getIntent().getIntExtra("notificationId", 0);
//        long msgId = getIntent().getLongExtraExtra("msgId");
        int dataType = getIntent().getIntExtra("action_type", 0) ;
        Map<String, String> extend = null;
        try {
            extend = (Map<String, String>) getIntent().getSerializableExtra("extend");
        } catch (Exception ex) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DoActionActivity  ERROR: "+ex.getMessage(), ex);
        }
        if (extend == null) {
            extend = new HashMap<String, String>();
        }
        if ( dataType == SmartNewNotificationManager.DATETYPE_FLAG_BTN_CONTENT ||
                dataType == SmartNewNotificationManager.DATATYPE_FLAG_HAVE_READ) {
            if (!StringUtils.isNull(actionData)) {
                DuoquUtils.doActionContext(this, actionData, extend);//         here
            }
        } 
//        else if (dataType == 2) {
//            DuoquUtils.getSdkDoAction().openSms(this, msgId, extend);
//        }
        
        DuoquNotificationViewManager.cancelNotification(this, notificationId);//6       6
        this.finish();

    }

}
