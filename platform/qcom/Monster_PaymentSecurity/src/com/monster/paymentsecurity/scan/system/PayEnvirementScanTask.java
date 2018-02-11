package com.monster.paymentsecurity.scan.system;

import android.content.Context;

import com.monster.paymentsecurity.util.SettingUtil;

/**
 * Created by logic on 16-11-21.
 * 支付环境扫描任务 1表示开启，０表示关闭了
 */
public class PayEnvirementScanTask extends SystemScanTask {

    public PayEnvirementScanTask(Context context) {
        super(context);
    }

    @Override
    protected Integer onStart() {
        Context context = (Context) weakContext.get();
        if (context == null)
            return 1;

        return !SettingUtil.isPayAppDetectionEnable(context) ? 1 : 0;
    }

    @Override
    protected @ScanType int getScanType() {
        return SCAN_TYPE_SYSTEM_PAYMENT_ENV;
    }
}
