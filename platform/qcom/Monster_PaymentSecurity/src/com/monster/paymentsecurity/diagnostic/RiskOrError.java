package com.monster.paymentsecurity.diagnostic;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.monster.paymentsecurity.scan.BaseScanTask;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 风险基类
 * Created by logic on 16-11-22.
 */
public class RiskOrError implements Parcelable {

    @IntDef(value = {RISK_NO, RISK_RED, RISK_YELLOW})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RiskLevel{}
    public static final int RISK_NO = 0;
    public static final int RISK_RED = 1;
    public static final int RISK_YELLOW = 2;

    @IntDef(value = {RISK_CATEGORY_WIFI,
            RISK_CATEGORY_SMS_SECURITY,
            RISK_CATEGORY_SYSTEM_BUG,
            RISK_CATEGORY_APP_SECURITY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RiskCategory{}
    public static final int RISK_CATEGORY_WIFI = 10;
    public static final int RISK_CATEGORY_SMS_SECURITY = 11;
    public static final int RISK_CATEGORY_SYSTEM_BUG = 12;
    public static final int RISK_CATEGORY_APP_SECURITY = 13;

    private final int level;//风险等级
    private final int scanType;//风险对应的扫描类型
    private final int category;//对应的扫描类型分类
    private String description;//风险描述
    private String scanError;//扫描出错

    public RiskOrError(@RiskLevel int riskLevel,
                       @BaseScanTask.ScanType int scanType,
                       @RiskCategory int category){
        this.level = riskLevel;
        this.scanType = scanType;
        this.category = category;
    }

    protected RiskOrError(Parcel in) {
        level = in.readInt();
        scanType = in.readInt();
        category = in.readInt();
        description = in.readString();
        scanError = in.readString();
    }

    @Override

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(level);
        dest.writeInt(scanType);
        dest.writeInt(category);
        dest.writeString(description);
        dest.writeString(scanError);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RiskOrError> CREATOR = new Creator<RiskOrError>() {
        @Override
        public RiskOrError createFromParcel(Parcel in) {
            return new RiskOrError(in);
        }

        @Override
        public RiskOrError[] newArray(int size) {
            return new RiskOrError[size];
        }
    };

    public @RiskOrError.RiskLevel int getLevel(){
        return level;
    }

    public void setDescription(String desc){
        this.description = desc;
    }

    public void setScanError(String errmsg){
        this.scanError = errmsg;
    }

    public @BaseScanTask.ScanType int getScanType() {
        return scanType;
    }

    public @RiskOrError.RiskCategory int getCategory(){
        return  category;
    }

    public String getDescription() {
        return description;
    }

    public String getScanError() {
        return scanError;
    }

    public boolean isRisk() {
        return level != RISK_NO && TextUtils.isEmpty(scanError);
    }

}