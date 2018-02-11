package com.monster.paymentsecurity.scan.wifi;

import tmsdk.bg.module.wifidetect.WifiDetectManager;

/**
 * Created by logic on 16-11-22.
 */

final class WifiScanHelper {

    public static String getWifiScanStateDesc(int wifiScanState){
        String strRet = null;
        if(wifiScanState == WifiDetectManager.NETWORK_AVILABLE){
            strRet = "网络可用";
        }else if(wifiScanState == WifiDetectManager.NETWORK_NOTAVILABLE){
            strRet = "网络未链接";
        }else if(wifiScanState == WifiDetectManager.NETWORK_NOTAVILABLE_APPROVE){
            strRet = "网络不可用，需认证";
        }
        return strRet;
    }

    public static String getDnsPhishingDesc(int scanResult){
        String strRet = null;
        if(scanResult == WifiDetectManager.CLOUND_CHECK_NETWORK_ERROR){
            strRet = "联网异常";
        }else if(scanResult == WifiDetectManager.CLOUND_CHECK_NO_FAKE){
            strRet = "正常";
        }else if(scanResult == WifiDetectManager.CLOUND_CHECK_DNS_FAKE){
            strRet = "DNS劫持";
        }else if(scanResult == WifiDetectManager.CLOUND_CHECK_PHISHING_FAKE){
            strRet =  "虚假钓鱼WiFi";
        }
        return strRet;
    }

    public static String getARPDetectDesc(int ARPCode){
        String strRet = null;
        if(ARPCode == WifiDetectManager.ARP_OK){
            strRet = "ARP检测OK";
        }else if(ARPCode == WifiDetectManager.ARP_FAKE){
            strRet = "ARP攻击";
        }else if(ARPCode < 0){
            strRet = "ARP检查异常:[" + ARPCode + "]";
        }
        return strRet;
    }

    public static String getWifiSecurityDesc(int security){
        String strRet = null;
        if(security == WifiDetectManager.SECURITY_NONE){
            strRet = "无加密";
        }else if(security == WifiDetectManager.SECURITY_WEP){
            strRet = "EAP认证与可选的WEP";
        }else if(security == WifiDetectManager.SECURITY_PSK){
            strRet = "WPA pre-shared key";
        }else if(security == WifiDetectManager.SECURITY_EAP){
            strRet = "WPA使用EAP认证";
        }
        return strRet;
    }
}
