package com.monster.paymentsecurity.diagnostic;

import android.os.Parcel;

import com.monster.paymentsecurity.scan.BaseScanTask;

import tmsdk.common.module.qscanner.QScanResultEntity;

/**
 * 应用风险
 *
 * Created by logic on 16-12-5.
 */
public class AppRisk extends RiskOrError {
    private final QScanResultEntity entity;

    public AppRisk(@BaseScanTask.ScanType int scanType,
                   @RiskCategory int category,
                   QScanResultEntity entity) {
        super(RiskOrError.RISK_YELLOW, scanType, category);
        this.entity = entity;
    }

    private AppRisk(Parcel in){
        super(in);
        entity = in.readParcelable(QScanResultEntity.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(entity, flags);
    }

    public QScanResultEntity getEntity() {
        return entity;
    }

    public static final Creator<AppRisk> CREATOR = new Creator<AppRisk>() {
        @Override
        public AppRisk createFromParcel(Parcel in) {
            return new AppRisk(in);
        }

        @Override
        public AppRisk[] newArray(int size) {
            return new AppRisk[size];
        }
    };
}
