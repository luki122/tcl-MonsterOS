package cn.com.xy.sms.sdk.ui.popu;

import android.app.Activity;
import android.os.Bundle;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;

public class BaseActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        initBefore(savedInstanceState);

        setView();
        try {
            initAfter();
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BaseActivity onCreate error:", e);
        }

    }

    public void initBefore(Bundle savedInstanceState) {

    }

    public void initAfter() {

    }

    public boolean isBl() {
        return true;
    }

    public void setView() {
        setContentView(getLayoutId());

    }

    public int getLayoutId() {
        return 0;
    }

    public Activity getActivity() {
        return BaseActivity.this;
    }

}
