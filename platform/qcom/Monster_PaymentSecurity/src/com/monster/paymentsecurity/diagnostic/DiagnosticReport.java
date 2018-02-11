package com.monster.paymentsecurity.diagnostic;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 诊断报告
 * Created by logic on 16-11-28.
 */
public class DiagnosticReport implements Parcelable{

    private  final List<RiskOrError> riskOrErrors = new ArrayList<>();//普通风险
    private  final List<AppRisk> appRisks = new ArrayList<>();//app风险
    private  int totalRiskCount = 0;
    private  int wifiRiskCount = 0;
    private  int mmsRiskCount = 0;
    private  int systemBugCount = 0;
    private  int appRiskCount = 0;

    public boolean isScanCanceled() {
        return scanCanceled;
    }

    public void setScanCanceled(boolean scanCanceled) {
        this.scanCanceled = scanCanceled;
    }

    private  boolean scanCanceled;

    public int getAppRiskCount() {
        return appRiskCount;
    }

    public int getSystemBugCount() {
        return systemBugCount;
    }

    public int getMmsRiskCount() {
        return mmsRiskCount;
    }

    public int getWifiRiskCount() {
        return wifiRiskCount;
    }

    public int getTotalRiskCount() {
        return totalRiskCount;
    }

    public boolean needShowProtected(){
        return appRiskCount == 0 || systemBugCount == 0 || mmsRiskCount == 0 || wifiRiskCount == 0;
    }

    public DiagnosticReport(){

    }

    private DiagnosticReport(Parcel in) {
        scanCanceled = in.readInt() != 0;
        totalRiskCount = in.readInt();
        wifiRiskCount = in.readInt();
        mmsRiskCount = in.readInt();
        systemBugCount = in.readInt();
        appRiskCount = in.readInt();
        in.readTypedList(riskOrErrors, RiskOrError.CREATOR);
        in.readTypedList(appRisks, AppRisk.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(scanCanceled ? 1 : 0);
        dest.writeInt(totalRiskCount);
        dest.writeInt(wifiRiskCount);
        dest.writeInt(mmsRiskCount);
        dest.writeInt(systemBugCount);
        dest.writeInt(appRiskCount);
        dest.writeTypedList(riskOrErrors);
        dest.writeTypedList(appRisks);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DiagnosticReport> CREATOR = new Creator<DiagnosticReport>() {
        @Override
        public DiagnosticReport createFromParcel(Parcel in) {
            return new DiagnosticReport(in);
        }

        @Override
        public DiagnosticReport[] newArray(int size) {
            return new DiagnosticReport[size];
        }
    };

    public boolean hasRisk(){
        return totalRiskCount != 0;
    }

    public boolean normal(){
        return totalRiskCount == 0;
    }

    public List<RiskOrError> getCommonRisks(){
        return riskOrErrors;
    }

    public List<AppRisk> getAppRisks(){
        return appRisks;
    }

    public void addRisk(RiskOrError riskOrError){
        if (riskOrError instanceof AppRisk){
            appRisks.add((AppRisk) riskOrError);
        }else {
            riskOrErrors.add(riskOrError);
        }
        if (riskOrError.isRisk()) {
            totalRiskCount ++;
            if (riskOrError.getCategory() == RiskOrError.RISK_CATEGORY_APP_SECURITY){
                appRiskCount ++;
            }else if (riskOrError.getCategory() == RiskOrError.RISK_CATEGORY_SMS_SECURITY){
                mmsRiskCount++;
            }else if (riskOrError.getCategory() == RiskOrError.RISK_CATEGORY_SYSTEM_BUG){
                systemBugCount++;
            }else if (riskOrError.getCategory() == RiskOrError.RISK_CATEGORY_WIFI){
                appRiskCount++;
            }
        }
    }

    public boolean removeRisk(RiskOrError riskOrError){
        if (!contains(riskOrError))
            return false;
        if (riskOrError instanceof AppRisk){
            appRisks.remove(riskOrError);
        }else {
            riskOrErrors.remove(riskOrError);
        }
        if (riskOrError.isRisk()) {
            totalRiskCount --;
            if (riskOrError.getCategory() == RiskOrError.RISK_CATEGORY_APP_SECURITY){
                appRiskCount--;
            }else if (riskOrError.getCategory() == RiskOrError.RISK_CATEGORY_SMS_SECURITY){
                mmsRiskCount--;
            }else if (riskOrError.getCategory() == RiskOrError.RISK_CATEGORY_SYSTEM_BUG){
                systemBugCount--;
            }else if (riskOrError.getCategory() == RiskOrError.RISK_CATEGORY_WIFI){
                appRiskCount--;
            }
            return true;
        }
        return false;
    }

    private boolean contains(RiskOrError riskOrError) {
        if (riskOrError == null)
            return false;
        //noinspection SuspiciousMethodCalls
        return (riskOrErrors.contains(riskOrError)
                || appRisks.contains(riskOrError));
    }
}
