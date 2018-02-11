package com.monster.paymentsecurity.scan.qscanner;

import android.content.Context;

import com.monster.paymentsecurity.scan.Result;
import com.monster.paymentsecurity.util.SettingUtil;
import com.monster.paymentsecurity.util.Utils;

import java.util.ArrayList;
import java.util.List;

import tmsdk.common.module.qscanner.QScanConstants;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.module.qscanner.QScanListenerV2;

/**
 * Created by logic on 16-11-21.
 */
public class ScanInstalledApk extends QScannerTask {

    private final String pkgName;

    public ScanInstalledApk(Context context, String packageName) {
        super(context);
        this.pkgName = packageName;
    }

    @Override
    protected QScanResultEntity onStart() {
        Context context = weakContext.get();
        boolean isCloudScan;
        isCloudScan = context != null && Utils.isWifiOk(context) && SettingUtil.isScanCloudEnable(context);
        result.setErrCode(SCAN_ERRCODE_DEFAULT);
        //这是一个同步的调用，回调会先执行
        //noinspection unchecked
        mQScannerMananger.scanSelectedPackages(new ArrayList<String>() {
            {
                add(pkgName);
            }
        }, new MyQScanListener(result), isCloudScan);
        return (QScanResultEntity) result.getRawData();
    }

    @Override
    protected @ScanType int getScanType() {
        return SCAN_TYPE_QSCANER_INSTALLED_APK;
    }

    private static final class MyQScanListener extends QScanListenerV2 {
        private final Result<QScanResultEntity> taskResult;

        private MyQScanListener(Result<QScanResultEntity> result){
            this.taskResult = result;
        }

        /**
         *
         * @param scanType 这是TMSDK的扫描类型
         */
        @Override
        public void onScanStarted(int scanType) {}

        /**
         * 云扫描出现网络错误
         *
         * @param scanType 扫描类型，具体参考{@link QScanConstants#SCAN_INSTALLEDPKGS} ~
         *                 {@link QScanConstants#SCAN_SPECIALS}
         * @param errCode  错误码
         */
        @Override
        public void onScanError(int scanType, int errCode) {
            taskResult.setErrCode(SCAN_ERRCODE_QSCANNER_SCAN_ERROR);
        }

        @Override
        public void onScanProgress(int i, int i1, QScanResultEntity qScanResultEntity) {
            super.onScanProgress(i, i1, qScanResultEntity);
            taskResult.setRawData(qScanResultEntity);
        }

        /**
         * 扫描结束
         *
         * @param scanType 扫描类型，具体参考{@link QScanConstants#SCAN_INSTALLEDPKGS} ~
         *                                  {@link QScanConstants#SCAN_SPECIALS}
         * @param qScanresults  扫描的所有结果
         */
        @Override
        public void onScanFinished(int scanType, List<QScanResultEntity> qScanresults) {
            super.onScanFinished(scanType, qScanresults);
//            if(qScanresults.size() != 0)
//                taskResult.setRawData(qScanresults.get(0));
        }
    }
}
