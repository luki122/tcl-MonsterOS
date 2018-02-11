package com.monster.paymentsecurity.scan;

import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_ERRCODE_DEFAULT;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_ERRCODE_INIT_QSCANNER_ERROR;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_ERRCODE_PREPARE_ERROR;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_ERRCODE_QSCANNER_SCAN_ERROR;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_ERRCODE_QSCANNER_APK_FILE_NOT_FOUND;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_ERRCODE_WIFI_ARP_EXCEPTION;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_ERRCODE_WIFI_DNS_PHISHING;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_ERRCODE_WIFI_SECURITY_BSSID_NOT_FOUND;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_ERRCODE_WIFI_SECURITY_EXCEPTION;

/**
 * Created by logic on 16-11-21.
 */

public final class Result<T> {
    private T rawData;
    private @BaseScanTask.ScanErrCode int errCode = SCAN_ERRCODE_DEFAULT;
    private @BaseScanTask.ScanType final int scanType;

    public Result(@BaseScanTask.ScanType int scanType) {
        this.scanType = scanType;
    }

    public boolean isSuccess(){
        return  errCode == SCAN_ERRCODE_DEFAULT && rawData != null;
    }

    public void setRawData(T rawData) {
        this.rawData = rawData;
    }

    public void setErrCode(@BaseScanTask.ScanErrCode int errCode) {
        this.errCode = errCode;
    }

    public T getRawData(){
        return rawData;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getErrMsg(){
        switch (errCode){
            case SCAN_ERRCODE_WIFI_SECURITY_BSSID_NOT_FOUND:
                return "WiFi加密检测：未找到加密的BSSID";
            case SCAN_ERRCODE_WIFI_SECURITY_EXCEPTION:
                return "WiFi加密检测：未知异常";
            case SCAN_ERRCODE_WIFI_ARP_EXCEPTION:
                return "WiFi ARP检测异常";
            case SCAN_ERRCODE_QSCANNER_APK_FILE_NOT_FOUND:
                return "安装包文件不存在";
            case SCAN_ERRCODE_QSCANNER_SCAN_ERROR:
                return "扫描错误：云查杀网络异常或其他原因";
            case SCAN_ERRCODE_INIT_QSCANNER_ERROR:
                return "扫描错误：不能初始化Qscanner";
            case SCAN_ERRCODE_PREPARE_ERROR:
                return "扫描错误:　任务prepare fail";
            case SCAN_ERRCODE_WIFI_DNS_PHISHING:
            case SCAN_ERRCODE_DEFAULT:
                break;
        }
        return "";
    }

    public @BaseScanTask.ScanType int getScanType(){
        return scanType;
    }
}
