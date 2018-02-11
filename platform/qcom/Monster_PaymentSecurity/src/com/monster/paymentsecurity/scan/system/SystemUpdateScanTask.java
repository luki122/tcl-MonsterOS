package com.monster.paymentsecurity.scan.system;

import android.content.Context;
import android.text.TextUtils;

import com.monster.paymentsecurity.util.SettingUtil;
import com.monster.paymentsecurity.util.Utils;

/**
 * Created by logic on 16-11-21.
 * 检测系统版本的任务, 1表示有更新，　０表示无更新
 */
public class SystemUpdateScanTask extends SystemScanTask{

    public SystemUpdateScanTask(Context context) {
        super(context);
    }

    @Override
    protected Integer onStart() {
        Context context = (Context) weakContext.get();
        if (context == null)
            return 0;

        String saveVersion = SettingUtil.getSystemVersion(context);

        if (TextUtils.isEmpty(saveVersion))
            return 0;

        if (Utils.getSystemVersion().equalsIgnoreCase(SettingUtil.getSystemVersion(context)))
            return 0;

        return 1;
    }

    @Override
    protected @ScanType  int getScanType() {
        return SCAN_TYPE_SYSTEM_UPDATE;
    }
}
