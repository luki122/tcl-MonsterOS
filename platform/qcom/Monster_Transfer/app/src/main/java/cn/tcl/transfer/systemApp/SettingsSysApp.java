/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.systemApp;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.LocaleList;
import android.os.RemoteException;
import android.provider.Settings;
import android.security.Credentials;
import android.security.KeyStore;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.send.SendBackupDataService;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.NetWorkUtil;
import cn.tcl.transfer.util.Utils;

import com.android.internal.app.LocalePicker;
import com.android.internal.net.VpnProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsSysApp extends SysBaseApp {

    public static final String NAME = "com.android.settings";
    private static final String TAG = "SettingsSysApp";

    public SettingsSysApp() {
        super(NAME);
    }

    public SettingsSysApp(DataOutputStream outputStream, ICallback callback) {
        super(outputStream, callback, NAME);
    }

    @Override
    public void send() throws IOException {
        try {
            sendSqlFile();
            sendWLANFiles();
        } catch (RemoteException e) {
            Log.e(TAG, "sendAppList", e);
        }
    }

    @Override
    public ArrayList<String> getSysOtherFiles() {
        ArrayList<String> files = new ArrayList<>();
        String markedPayApList = "/data/user_de/0/com.android.settings/shared_prefs/markedPayApList.xml";
        String savedWifiAp = "/data/misc/wifi/wpa_supplicant.conf";
        files.add(markedPayApList);
        files.add(savedWifiAp);
        return files;
    }

    @Override
    public void sendSqlFile() throws IOException, RemoteException {
        sendFileHead(NetWorkUtil.TYPE_SYS_SQL_DATA, Utils.SYS_SETTINGS_BACKUP_PATH);
        mOutputStream.writeLong(SendBackupDataService.getLeftTime());
        mOutputStream.flush();
        mOutputStream.writeLong(1);
        mOutputStream.flush();
        sendFileBody(Utils.SYS_SETTINGS_BACKUP_PATH);
    }

    private void sendWLANFiles() throws IOException, RemoteException {
        if(SendBackupDataService.isCancelled) {
            return;
        }
        File file = new File(Utils.SYS_OTHER_DATA_BACKUP_PATH + "/markedPayApList.xml");
        if(SendBackupDataService.isCancelled) {
            return;
        }
        if(file.exists()) {
            mOutputStream.writeLong(NetWorkUtil.TYPE_SYS_OTHER_DATA);
            mOutputStream.flush();
            mOutputStream.writeUTF(Utils.SYS_OTHER_DATA_RECEIVED_PATH + "/markedPayApList.xml");
            mOutputStream.flush();
            mOutputStream.writeLong(file.length());
            mOutputStream.flush();
            mOutputStream.writeUTF(mAppName);
            mOutputStream.flush();
            mOutputStream.writeLong(SendBackupDataService.getLeftTime());
            mOutputStream.flush();
            sendFileBody(Utils.SYS_OTHER_DATA_BACKUP_PATH + "/markedPayApList.xml");
        }


        if(SendBackupDataService.isCancelled) {
            return;
        }
        File file1 = new File(Utils.SYS_OTHER_DATA_BACKUP_PATH + "/wpa_supplicant.conf");
        if(file1.exists()) {
            mOutputStream.writeLong(NetWorkUtil.TYPE_SYS_OTHER_DATA);
            mOutputStream.flush();
            mOutputStream.writeUTF(Utils.SYS_OTHER_DATA_RECEIVED_PATH + "/wpa_supplicant.conf");
            mOutputStream.flush();
            mOutputStream.writeLong(file1.length());
            mOutputStream.flush();
            mOutputStream.writeUTF(mAppName);
            mOutputStream.flush();
            mOutputStream.writeLong(SendBackupDataService.getLeftTime());
            mOutputStream.flush();
            sendFileBody(Utils.SYS_OTHER_DATA_BACKUP_PATH + "/wpa_supplicant.conf");
        }
    }


    public static void getData(Context context) {

        JSONObject settingsData = new JSONObject();
        try {
            //1. settings's switch
            //mobile data switch
            try {
                int mCellularData = Settings.System.getInt(context.getContentResolver(), Settings.System.TCT_DUALSIM_CELLULAR_DATA_ENABLE, 1);
                settingsData.put(Settings.System.TCT_DUALSIM_CELLULAR_DATA_ENABLE, mCellularData);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //4G switch
            try {
                int m4GNetwork = Settings.System.getInt(context.getContentResolver(), Settings.System.TCT_DUALSIM_4G_NETWORK_ENABLE, 1);
                settingsData.put(Settings.System.TCT_DUALSIM_4G_NETWORK_ENABLE, m4GNetwork);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //VoLTE switch
            try {
                int mVolte = Settings.System.getInt(context.getContentResolver(), Settings.System.TCT_DUALSIM_VOLTE_ENABLE, 1);
                settingsData.put(Settings.System.TCT_DUALSIM_VOLTE_ENABLE, mVolte);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //bluetooth switch
            try {
                int bluetooth = Settings.Global.getInt(context.getContentResolver(), Settings.Global.BLUETOOTH_ON, 0);
                settingsData.put(Settings.Global.BLUETOOTH_ON, bluetooth);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //NFC switch
            try {
                int nfcState = getNfcState(context);
                settingsData.put("nfcState", nfcState);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //default sim card for mobile data
            try {
                final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
                int subId = SubscriptionManager.getDefaultDataSubscriptionId();
                settingsData.put("subId", subId);
            } catch (Exception e) {
                e.printStackTrace();
            }


            //2. date settings
            try {
                Calendar now = Calendar.getInstance();
                Date date = now.getTime();
                long time = now.getTimeInMillis();
                settingsData.put("time", time);
            } catch (Exception e) {
                e.printStackTrace();
            }


//        String localeNames = context.getLocaleNames(getActivity());
            try {
                LocaleList locales = LocalePicker.getLocales();
                Locale locale = locales.get(0);
                settingsData.put("locale", locale.getDisplayLanguage());
            } catch (Exception e) {
                e.printStackTrace();
            }


            //3. bluetooth paired devices history
            //can not implemented

            //4. wifiHotspot settings；
            try {
                JSONObject wifiJson = new JSONObject();
                WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiConfiguration wifiConfig = mWifiManager.getWifiApConfiguration();
                if(wifiConfig != null) {
                    String ssid = wifiConfig.SSID;
                    int securityType = wifiConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA2_PSK) ? 1 : 0;    //1代表WPA2_PSK，0表示NONE
                    int band = wifiConfig.apBand;   //0表示2.4ghz，1表示5ghz
                    String password = wifiConfig.preSharedKey;

                    wifiJson.put("ssid", ssid);
                    wifiJson.put("securityType", securityType);
                    wifiJson.put("band", band);
                    wifiJson.put("password", password);

                    settingsData.put("wifiHotspot", wifiJson);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            //5. VPN settings；
            try {
                KeyStore keyStore = KeyStore.getInstance();
                JSONArray vpnList = new JSONArray();
                int i = 0;
                for (String key : keyStore.list(Credentials.VPN)) {
                    final VpnProfile profile = VpnProfile.decode(key, keyStore.get(Credentials.VPN + key));
                    if (profile != null) {
                        JSONObject tmp = setVpn(profile);
                        vpnList.put(i, tmp);
                    }
                    i++;
                }
                settingsData.put("vpn", vpnList);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //6. get auto locktime
            try {
                long lockTime = Settings.Secure.getLong(context.getContentResolver(), Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT, 5000);
                settingsData.put(Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT, lockTime);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //7. ringtong and vibrate
            try {
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                int ringerMode = audioManager.getRingerMode();
                settingsData.put("ringerMode", ringerMode);

                int ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                settingsData.put("ringVolume", ringVolume);

                int mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                settingsData.put("mediaVolume", mediaVolume);

                int notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                settingsData.put("notificationVolume", notificationVolume);

                int alarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                settingsData.put("alarmVolume", alarmVolume);

                int callVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                settingsData.put("callVolume", callVolume);

                Uri defaultPhoneRingtone = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
                if(defaultPhoneRingtone != null) {
                    settingsData.put("defaultPhoneRingtone", defaultPhoneRingtone.toString());
                } else {
                    settingsData.put("defaultPhoneRingtone", "");
                }
                Uri defaultNotificationRingtone = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                if(defaultNotificationRingtone != null) {
                    settingsData.put("defaultNotificationRingtone", defaultNotificationRingtone.toString());
                } else {
                    settingsData.put("defaultNotificationRingtone", "");
                }
                Uri defaultAlarmRingtone = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
                if(defaultAlarmRingtone != null) {
                    settingsData.put("defaultAlarmRingtone", defaultAlarmRingtone.toString());
                } else {
                    settingsData.put("defaultAlarmRingtone", "");
                }

                int vibrateOntap = Settings.System.getInt(context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
                settingsData.put(Settings.System.HAPTIC_FEEDBACK_ENABLED, vibrateOntap);

                int touchSound = Settings.System.getInt(context.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1);
                settingsData.put(Settings.System.SOUND_EFFECTS_ENABLED, touchSound);

                int callVibrate = Settings.System.getInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0);
                settingsData.put(Settings.System.VIBRATE_WHEN_RINGING, callVibrate);

                int screenSound = Settings.System.getInt(context.getContentResolver(), Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 1);
                settingsData.put(Settings.System.LOCKSCREEN_SOUNDS_ENABLED, screenSound);

                int dialSound = Settings.System.getInt(context.getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING, 1);
                settingsData.put(Settings.System.DTMF_TONE_WHEN_DIALING, dialSound);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //8. virtual key settings
            try {
                int softkeyold = Settings.System.getInt(context.getContentResolver(), "softkeymode", 0);
                settingsData.put("softkeymode", softkeyold);
            } catch (Exception e) {
                e.printStackTrace();
            }

            FileWriter fw = new FileWriter(Utils.SYS_SETTINGS_BACKUP_PATH);
            fw.write(settingsData.toString());
            fw.flush();
            fw.close();
            Log.d("SettingsSysApp", settingsData.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setData(Context context) {
        String settings = null;

        try {
            File file = new File(Utils.SYS_SETTINGS_RECEIVED_PATH);
            final int length = (int)file.length();
            FileReader fr = new FileReader(Utils.SYS_SETTINGS_RECEIVED_PATH);
            char[] ch = new char[length + 1];
            fr.read(ch);
            settings = String.valueOf(ch);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if(TextUtils.isEmpty(settings)) {
            return;
        }
        try {
            JSONObject settingsData = new JSONObject(settings);
            //1. settings's switch
            //mobile data switch
            try {
                if(settingsData.has(Settings.System.TCT_DUALSIM_CELLULAR_DATA_ENABLE)) {
                    int mCellularData = settingsData.getInt(Settings.System.TCT_DUALSIM_CELLULAR_DATA_ENABLE);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.TCT_DUALSIM_CELLULAR_DATA_ENABLE, mCellularData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //4G data switch
            try {
                if(settingsData.has(Settings.System.TCT_DUALSIM_4G_NETWORK_ENABLE)) {
                    int m4GNetwork = settingsData.getInt(Settings.System.TCT_DUALSIM_4G_NETWORK_ENABLE);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.TCT_DUALSIM_4G_NETWORK_ENABLE, m4GNetwork);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //VoLTE switch
            try {
                if(settingsData.has(Settings.System.TCT_DUALSIM_VOLTE_ENABLE)) {
                    int mVolte = settingsData.getInt(Settings.System.TCT_DUALSIM_VOLTE_ENABLE);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.TCT_DUALSIM_VOLTE_ENABLE, mVolte);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //bluetooth switch
            try {
                if(settingsData.has(Settings.Global.BLUETOOTH_ON)) {
                    int bluetooth = settingsData.getInt(Settings.Global.BLUETOOTH_ON);
                    if(bluetooth == 1) {
                        BluetoothAdapter.getDefaultAdapter().enable();
                    } else {
                        BluetoothAdapter.getDefaultAdapter().disable();
                    }
                    //Settings.Global.putInt(context.getContentResolver(), Settings.Global.BLUETOOTH_ON, bluetooth);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //NFC switch
            try {
                if(settingsData.has("nfcState")) {
                    int nfcState = settingsData.getInt("nfcState");
                    NfcAdapter nfcAdapter = NfcAdapter.getNfcAdapter(context);
                    if(nfcState == NfcAdapter.STATE_OFF) {
                        nfcAdapter.disable();
                    } else if(nfcState == NfcAdapter.STATE_ON) {
                        nfcAdapter.enable();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //default sim card for mobile data
            try {
                if(settingsData.has("subId")) {
                    int subId = settingsData.getInt("subId");
                    final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
                    subscriptionManager.setDefaultDataSubId(subId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //2. date settings
            try {
                if(settingsData.has("time")) {
                    long time = settingsData.getLong("time");
                    Date date = new Date();
                    date.setTime(time);
                    Calendar now = Calendar.getInstance();
                    now.setTime(date);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

//        String localeNames = context.getLocaleNames(getActivity());
            //language settings
            try {
                if(settingsData.has("locale")) {
                    String lang = settingsData.getString("locale");
                    Date date = new Date();
                    LocaleList locales = LocalePicker.getLocales();
                    List<LocalePicker.LocaleInfo> localeInfos = LocalePicker.getAllAssetLocales(context, false);
                    for(int i = 0; i < localeInfos.size(); i++) {
                        if(TextUtils.equals(lang, localeInfos.get(i).getLocale().getDisplayLanguage())) {
                            LocalePicker.updateLocale(localeInfos.get(i).getLocale());
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //3. bluetooth paired devices history
            //can not implemented

            //4. wifiHotspot settings；
            try {
                if(settingsData.has("wifiHotspot")) {
                    JSONObject wifiJson = settingsData.getJSONObject("wifiHotspot");
                    WifiConfiguration wifiConfig = new WifiConfiguration();

                    WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

                    wifiConfig.SSID = wifiJson.getString("ssid");
                    wifiConfig.apBand = wifiJson.getInt("band");
                    switch (wifiJson.getInt("securityType")) {
                        case 0:
                            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        case 1:
                            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA2_PSK);
                            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                            wifiConfig.preSharedKey = wifiJson.getString("password");
                    }
                    mWifiManager.setWifiApConfiguration(wifiConfig);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //5. VPN settings；
            try {
                if(settingsData.has("vpn")) {
                    JSONArray vpnList = settingsData.getJSONArray("vpn");
                    int length = vpnList.length();
                    if(length > 0) {
                        for(int i = 0; i < length; i++) {
                            JSONObject vpn= (JSONObject)vpnList.get(i);
                            VpnProfile vpnProfile = getVpn(vpn.toString());
                            KeyStore.getInstance().put(Credentials.VPN + vpnProfile.key, vpnProfile.encode(), KeyStore.UID_SELF, /* flags */ 0);
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //6. get auto locktime
            try {
                if(settingsData.has(Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT)) {
                    long time = settingsData.getLong(Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT);
                    Settings.Secure.putLong(context.getContentResolver(), Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT, time);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //7. ringtong and vibrate
            try {
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                if(settingsData.has("ringerMode")) {
                    int ringerMode = settingsData.getInt("ringerMode");
                    audioManager.setRingerMode(ringerMode);
                }

                if(settingsData.has("ringVolume")) {
                    int ringVolume = settingsData.getInt("ringVolume");
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, ringVolume, 0);
                }

                if(settingsData.has("mediaVolume")) {
                    int mediaVolume = settingsData.getInt("mediaVolume");
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaVolume, 0);
                }

                if(settingsData.has("notificationVolume")) {
                    int notificationVolume = settingsData.getInt("notificationVolume");
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, notificationVolume, 0);
                }

                if(settingsData.has("alarmVolume")) {
                    int alarmVolume = settingsData.getInt("alarmVolume");
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, alarmVolume, 0);
                }

                if(settingsData.has("callVolume")) {
                    int callVolume = settingsData.getInt("callVolume");
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, callVolume, 0);
                }

                if(settingsData.has("defaultPhoneRingtone")) {
                    String defaultPhoneRingtone = settingsData.getString("defaultPhoneRingtone");
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, Uri.parse(defaultPhoneRingtone));
                }

                if(settingsData.has("defaultNotificationRingtone")) {
                    String defaultNotificationRingtone = settingsData.getString("defaultNotificationRingtone");
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, Uri.parse(defaultNotificationRingtone));
                }

                if(settingsData.has("defaultAlarmRingtone")) {
                    String defaultAlarmRingtone = settingsData.getString("defaultAlarmRingtone");
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, Uri.parse(defaultAlarmRingtone));
                }

                if(settingsData.has(Settings.System.HAPTIC_FEEDBACK_ENABLED)) {
                    int vibrateOntap = settingsData.getInt(Settings.System.HAPTIC_FEEDBACK_ENABLED);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, vibrateOntap);
                }

                if(settingsData.has(Settings.System.SOUND_EFFECTS_ENABLED)) {
                    int touchSound = settingsData.getInt(Settings.System.SOUND_EFFECTS_ENABLED);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, touchSound);
                }

                if(settingsData.has(Settings.System.VIBRATE_WHEN_RINGING)) {
                    int callVibrate = settingsData.getInt(Settings.System.VIBRATE_WHEN_RINGING);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, callVibrate);
                }

                if(settingsData.has(Settings.System.LOCKSCREEN_SOUNDS_ENABLED)) {
                    int screenSound = settingsData.getInt(Settings.System.LOCKSCREEN_SOUNDS_ENABLED);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.LOCKSCREEN_SOUNDS_ENABLED, screenSound);
                }

                if(settingsData.has(Settings.System.DTMF_TONE_WHEN_DIALING)) {
                    int dialSound = settingsData.getInt(Settings.System.DTMF_TONE_WHEN_DIALING);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING, dialSound);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //8. virtual key settings
            try {
                if(settingsData.has("softkeymode")) {
                    int softkeyold = settingsData.getInt("softkeymode");
                    Settings.System.putInt(context.getContentResolver(), "softkeymode", softkeyold);
//                    final String INTENT_SOFTKEY_CHANGE ="android.intent.action.softkey_change";
//                    Intent softkeyChangeIntent=new Intent(INTENT_SOFTKEY_CHANGE);
//                    softkeyChangeIntent.putExtra("softkey", softkeyold);
//                    context.sendBroadcast(softkeyChangeIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("SettingsSysApp", settingsData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static int getNfcState(Context context) {

        if(context == null) {
            return NfcAdapter.STATE_OFF;
        }
        NfcAdapter mNfcAdapter = NfcAdapter.getNfcAdapter(context);
        int state = mNfcAdapter.getAdapterState();
        if(state < NfcAdapter.STATE_ON && state > 0) {
            state = NfcAdapter.STATE_OFF;
        } else if(state > NfcAdapter.STATE_TURNING_ON && state <= NfcAdapter.STATE_TURNING_OFF) {
            state = NfcAdapter.STATE_ON;
        } else {
            state = NfcAdapter.STATE_OFF;
        }
        return state;
    }


    public static JSONObject setVpn(VpnProfile profile) {
        try {
            JSONObject info = new JSONObject();
            info.put("key", profile.key);
            info.put("name",profile.name);
            info.put("type",profile.type);
            info.put("server",profile.server);
            info.put("username",profile.username);
            info.put("password",profile.password);
            info.put("dnsServers",profile.dnsServers);
            info.put("searchDomains",profile.searchDomains);
            info.put("routes",profile.routes);
            info.put("mppe",profile.mppe);
            info.put("l2tpSecret",profile.l2tpSecret);
            info.put("ipsecIdentifier",profile.ipsecIdentifier);
            info.put("ipsecSecret",profile.ipsecSecret);
            info.put("ipsecUserCert",profile.ipsecUserCert);
            info.put("ipsecCaCert",profile.ipsecCaCert);
            info.put("ipsecServerCert",profile.ipsecServerCert);
            info.put("saveLogin",profile.saveLogin);
            return info;
        } catch (JSONException e) {
            LogUtils.e("vpn", "CreateInfo json exception");
            return null;
        }
    }

    public static VpnProfile getVpn(String vpn) {
        try {
            JSONObject info = new JSONObject(vpn);
            VpnProfile profile = new VpnProfile(info.getString("key"));
            profile.name = info.getString("name");
            profile.type = info.getInt("type");
            profile.server = info.getString("server");
            profile.username = info.getString("username");
            profile.password = info.getString("password");
            profile.dnsServers = info.getString("dnsServers");
            profile.searchDomains = info.getString("searchDomains");
            profile.routes = info.getString("routes");
            profile.mppe = info.getBoolean("mppe");
            profile.l2tpSecret = info.getString("l2tpSecret");
            profile.ipsecIdentifier = info.getString("ipsecIdentifier");
            profile.ipsecSecret = info.getString("ipsecSecret");
            profile.ipsecUserCert = info.getString("ipsecUserCert");
            profile.ipsecCaCert = info.getString("ipsecCaCert");
            profile.ipsecServerCert = info.getString("ipsecServerCert");
            profile.saveLogin = info.getBoolean("saveLogin");
            return profile;
        } catch (JSONException e) {
            Log.e("vpn", "getInfo json exception:", e);
            return null;
        }
    }

}
