package com.monster.paymentsecurity.scan.qscanner;

import tmsdk.common.module.qscanner.QScanConstants;
import tmsdk.common.module.qscanner.QScanResultEntity;

/**
 * Created by logic on 16-11-22.
 */

public final class QScanerHelper {


    /**
     * 得到扫描结果的建议　
     *
     * @param entity TM APK扫描结果
     * @return
     */
    public static String getQScanEntityAdvice(QScanResultEntity entity) {
        StringBuilder content = new StringBuilder();
        switch (entity.advice) {
            case QScanConstants.ADVICE_NONE:
                content.append("无建议");
                break;

            case QScanConstants.ADVICE_CLEAR:
                content.append("建议清除");
                break;

            case QScanConstants.ADVICE_UPDATE:
                content.append("建议升级");
                break;

            case QScanConstants.ADVICE_CLEAR_UPDATE:
                content.append("建议清除或升级");
                break;

            case QScanConstants.ADVICE_CHECK_PAGE:
                content.append("建议查看清除方法");
                break;

            case QScanConstants.ADVICE_CHECK_PAGE_UPDATE:
                content.append("建议查看清除方法或者升级");
                break;

            case QScanConstants.ADVICE_DOWN_TOOL:
                content.append("建议下载专杀清除");
                break;

            case QScanConstants.ADVICE_DOWN_TOOL_UPDATE:
                content.append("建议下载专杀清除或者升级");
                break;

            default:
                content.append("无建议");
                break;
        }
        return content.toString();
    }

    /**
     * 得到扫描结果的描述
     *
     * @param entity TM APK扫描结果
     * @return
     */
    public static String getQScanEntityDes(QScanResultEntity entity) {
        StringBuilder content = new StringBuilder();
        String message = entity.softName + "##" + entity.discription;
        if (message.length() == 0) {
            message = entity.path;
        }
        boolean isNormal = false;
        switch (entity.type) {
            case QScanConstants.TYPE_OK:
                content.append(message).append(" 正常");
                isNormal = true;
                break;

            case QScanConstants.TYPE_RISK:
                content.append(message).append("  风险");
                break;

            case QScanConstants.TYPE_VIRUS:
                content.append(message).append(" ").append(entity.name).append(" 病毒");
                break;

            case QScanConstants.TYPE_SYSTEM_FLAW:
                content.append(message).append(" ").append(entity.name).append(" 系统漏洞");
                break;

            case QScanConstants.TYPE_TROJAN:
                content.append(message).append(" ").append(entity.name).append(" 专杀木马");
                break;

            case QScanConstants.TYPE_NOT_OFFICIAL:
                content.append(message).append(" ").append(entity.name).append(" 非官方证书");
                break;

            case QScanConstants.TYPE_RISK_PAY:
                content.append(message).append(" ").append(entity.name).append(" 支付风险");
                break;

            case QScanConstants.TYPE_RISK_STEALACCOUNT:
                content.append(message).append(" ").append(entity.name).append(" 账号风险");
                break;

            case QScanConstants.TYPE_UNKNOWN:
                content.append(message).append("  未知");
                isNormal = true;
                break;

            default:
                content.append(message).append("  未知");
                break;
        }
        if(isNormal) {
            return null;
        }
        return content.toString();
    }

    /**
     * 判断应用程序包含插件
     *
     * @param entity　TM APK扫描结果
     * @return
     */
    public static boolean isQScanEntityHasAdPlug(QScanResultEntity entity){
        return entity.plugins.size() > 0;
    }

    /**
     * 是否支付应用
     *
     * @param entity
     * @return
     */
    public static boolean isPaymentApp(QScanResultEntity entity){
        return  entity.isInPayList;
    }
}
