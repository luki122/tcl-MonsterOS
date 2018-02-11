package com.monster.paymentsecurity.diagnostic;

import android.content.Context;

import com.monster.paymentsecurity.db.WhiteListDao;
import com.monster.paymentsecurity.scan.BaseScanTask;
import com.monster.paymentsecurity.scan.Result;
import com.monster.paymentsecurity.scan.ScanningHelper;
import com.monster.paymentsecurity.scan.qscanner.QScanerHelper;

import java.util.ArrayList;
import java.util.List;

import tmsdk.bg.module.wifidetect.WifiDetectManager;
import tmsdk.common.module.qscanner.QScanConstants;
import tmsdk.common.module.qscanner.QScanResultEntity;

import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_SMS_APP;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_SYSTEM_PAYMENT_ENV;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_SYSTEM_UPDATE;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_WIFI_ARP;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_WIFI_DNS;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_WIFI_SECURITY;
import static com.monster.paymentsecurity.scan.BaseScanTask.SCAN_TYPE_WIFI_STATE;

/**
 * 扫描结果分析
 * Created by logic on 16-11-28.
 */
public class Diagnostor {

    private final Context context;
    private DiagnosticReport report;

    public Diagnostor(Context context){
        this.context = context;
        reset();
    }

    public void reset() {
        this.report = new DiagnosticReport();
    }

    public void diagnose(Result scanResult){
        if (scanResult == null) return  ;
        analysisResult(scanResult);
    }
    
    public void diagnose(List<Result> scanResults){
       if (scanResults == null)
           return ;
        Result result;
        for (int i = 0; i < scanResults.size(); i++){
            result = scanResults.get(i);
            diagnose(result);
        }
    }

    public DiagnosticReport getReport() {
        return report;
    }

    public int getTotalRiskCount() {
        return report.getTotalRiskCount();
    }

    public int getWifiRiskCount(){
        return report.getWifiRiskCount();
    }

    public int getMMSRiskCount(){
        return report.getMmsRiskCount();
    }

    public int getSystemBugRiskCount(){
        return report.getSystemBugCount();
    }

    public int getAppRiskCount(){
        return report.getAppRiskCount();
    }

    private RiskOrError analysisResult(Result result){
        final @BaseScanTask.ScanType int scanType = result.getScanType();
        RiskOrError riskOrError = null;
        if (!result.isSuccess()){
            //扫描失败，无法诊断出结果, 需根据扫描结果判断
            riskOrError = handleScanError(result, scanType);
            return riskOrError;
        }
        final Object rawData = result.getRawData();
        if (rawData instanceof Integer){
            switch (scanType) {
                case SCAN_TYPE_WIFI_STATE:
                    riskOrError = handleWiFiStateReport(scanType, (Integer) rawData);
                    break;
                case SCAN_TYPE_WIFI_SECURITY:
                    riskOrError = handleWiFiSecurityReport(scanType, (Integer) rawData);
                    break;
                case SCAN_TYPE_WIFI_DNS:
                    riskOrError = handleWiFiDNSReport(scanType, (Integer) rawData);
                    break;
                case SCAN_TYPE_WIFI_ARP:
                    handleWiFiARPReport(scanType, (Integer) rawData);
                    break;
                case SCAN_TYPE_SMS_APP:
                    riskOrError = handleSMSAPPReport(scanType, (Integer) rawData);
                    break;
                case SCAN_TYPE_SYSTEM_PAYMENT_ENV:
                    riskOrError = handleSystemPaymentEnvPReport(scanType, (Integer) rawData);
                    break;
                case SCAN_TYPE_SYSTEM_UPDATE:
                    riskOrError = handleSystemUpdateReport(scanType, (Integer) rawData);
                    break;
                case BaseScanTask.SCAN_TYPE_QSCANER_INSTALLED_APK:
                case BaseScanTask.SCAN_TYPE_QSCANER_UNINSTALLED_APKS:
                case BaseScanTask.SCAN_TYPE_QSCANER_UNINSTALLED_APK:
                default:
                    break;
            }

        }else if (rawData instanceof QScanResultEntity){
            riskOrError =  handleQScannerReport(scanType,(QScanResultEntity) rawData);
        }else if (rawData instanceof ArrayList){
            //noinspection unchecked
            riskOrError =  handleQScannerApksReport(scanType,(List<QScanResultEntity>) rawData);
        } else {
            riskOrError = handleScanError(result, scanType);
        }
        return riskOrError;
    }

    //扫描失败，无法诊断出结果, 需根据errcode判断
    private RiskOrError handleScanError(Result result, int scanType) {
        RiskOrError riskOrError = new RiskOrError(RiskOrError.RISK_NO, scanType, ScanningHelper.convertScanTypeToCategory(scanType));
        riskOrError.setScanError(result.getErrMsg());
        report.addRisk(riskOrError);
        return riskOrError;
    }

    private RiskOrError handleQScannerApksReport(int scanType, List<QScanResultEntity> rawData) {
        RiskOrError riskOrError = null;
        QScanResultEntity entity;

        //白名单过滤
        WhiteListDao dao = new WhiteListDao(context);

        for (int i = 0; i < rawData.size(); i ++){
            entity = rawData.get(i);
            if (isQscanEntityNotNormal(entity) && (dao.getWhiteListApp(entity.packageName) == null))
            {
                riskOrError = new AppRisk(
                        scanType,
                        ScanningHelper.convertScanTypeToCategory(scanType),
                        entity
                );
                riskOrError.setDescription(QScanerHelper.getQScanEntityDes(entity));

                report.addRisk(riskOrError);
            }
        }
        return riskOrError;
    }

    private boolean isQscanEntityNotNormal(QScanResultEntity entity) {
        return entity.advice != QScanConstants.ADVICE_NONE
                && entity.type != QScanConstants.TYPE_OK
                && entity.type != QScanConstants.TYPE_UNKNOWN;
    }

    private RiskOrError handleQScannerReport(@BaseScanTask.ScanType int scanType, QScanResultEntity entity) {
        RiskOrError riskOrError = null;
        if (isQscanEntityNotNormal(entity))
        {
            riskOrError = new AppRisk(
                    scanType,
                    ScanningHelper.convertScanTypeToCategory(scanType),
                    entity
            );
            riskOrError.setDescription(QScanerHelper.getQScanEntityDes(entity));

            report.addRisk(riskOrError);
        }
        return riskOrError;
    }

    private RiskOrError handleSystemUpdateReport(@BaseScanTask.ScanType int scanType, int systemUpdate) {
        RiskOrError riskOrError = null;
        if (systemUpdate != 0)
        {
            riskOrError = new RiskOrError(RiskOrError.RISK_YELLOW,
                    scanType,
                    ScanningHelper.convertScanTypeToCategory(scanType)
            );
            riskOrError.setDescription("检测有新的系统更新");
            report.addRisk(riskOrError);
        }
        return riskOrError;
    }

    private RiskOrError handleSystemPaymentEnvPReport(@BaseScanTask.ScanType int scanType, int paymentEnv) {
        RiskOrError riskOrError = null;
        if (paymentEnv != 0)
        {
            riskOrError = new RiskOrError(RiskOrError.RISK_YELLOW,
                    scanType,
                    ScanningHelper.convertScanTypeToCategory(scanType)
            );
            riskOrError.setDescription("支付环境检测未开启");
            report.addRisk(riskOrError);
        }
        return riskOrError;
    }

    private RiskOrError handleSMSAPPReport(@BaseScanTask.ScanType int scanType, int smsAppRet) {
        RiskOrError riskOrError = null;
        if (smsAppRet != 0)
        {
            riskOrError = new RiskOrError(RiskOrError.RISK_RED,
                    scanType,
                    ScanningHelper.convertScanTypeToCategory(scanType)
            );
            riskOrError.setDescription("默认短息不是系统短信应用");
            report.addRisk(riskOrError);
        }
        return riskOrError;
    }


    private RiskOrError handleWiFiARPReport(@BaseScanTask.ScanType int scanType, int ARPState) {
        RiskOrError riskOrError = null;
        if (ARPState == WifiDetectManager.ARP_FAKE)
        {
            riskOrError = new RiskOrError(RiskOrError.RISK_RED,
                    scanType,
                    ScanningHelper.convertScanTypeToCategory(scanType)
            );
            riskOrError.setDescription("ARP攻击");
            report.addRisk(riskOrError);
        }
        return riskOrError;
    }

    private RiskOrError handleWiFiDNSReport(@BaseScanTask.ScanType int scanType, int wifiDnsState)
    {
        RiskOrError riskOrError  = null;

        if (wifiDnsState == WifiDetectManager.CLOUND_CHECK_DNS_FAKE)
        {
            riskOrError = new RiskOrError(RiskOrError.RISK_RED,
                    scanType,
                    ScanningHelper.convertScanTypeToCategory(scanType)
            );
            riskOrError.setDescription("DNS被劫持");
            report.addRisk(riskOrError);
        }else if (wifiDnsState == WifiDetectManager.CLOUND_CHECK_PHISHING_FAKE){
            riskOrError = new RiskOrError(RiskOrError.RISK_RED,
                    scanType,
                    ScanningHelper.convertScanTypeToCategory(scanType)
            );
            riskOrError.setDescription("钓鱼WiFI");
            report.addRisk(riskOrError);
        }
        return riskOrError;
    }

    private RiskOrError handleWiFiSecurityReport(@BaseScanTask.ScanType int scanType, int securityState) {
        RiskOrError riskOrError = null;
        if(securityState == WifiDetectManager.SECURITY_NONE)
        {
            riskOrError = new RiskOrError(RiskOrError.RISK_RED,
                    scanType,
                    ScanningHelper.convertScanTypeToCategory(scanType)
            );
            riskOrError.setDescription("正在使用一个未未加密的WiFi网络");
            report.addRisk(riskOrError);
        }
        return riskOrError;
    }

    private RiskOrError handleWiFiStateReport(@BaseScanTask.ScanType int scanType, int wifiState) {
        //noting
        return null;
    }

}

