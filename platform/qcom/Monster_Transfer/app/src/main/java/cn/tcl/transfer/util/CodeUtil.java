/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class CodeUtil {
    public static final String STORAGE = "STORAGE";
    public static final String SSID = "SSID";
    public static final String KEY = "Transfer";
    public static final String TAG = "CodeUtil";
    public static final String DES = "DES";

    public static String createInfo(String ssid) {
        try {
            JSONObject info = new JSONObject();
            info.put(SSID, ssid);
            info.put(STORAGE, getAvailableSize());
            return enCode(info.toString());
        } catch (JSONException e) {
            LogUtils.e(TAG, "CreateInfo json exception");
            return null;
        }
    }

    public static QRCodeInfo getInfo(String code) {
        String clearCode = deCode(code);
        if (TextUtils.isEmpty(clearCode)) {
            return null;
        }
        try {
            JSONObject QRInfo = new JSONObject(clearCode);
            QRCodeInfo info = new QRCodeInfo();
            info.setSsid(QRInfo.getString(SSID));
            info.setStorage(QRInfo.getLong(STORAGE));
            return info;
        } catch (JSONException e) {
            Log.e(TAG, "getInfo json exception:",e);
            return null;
        }
    }

    public static long getAvailableSize() {
        File path = Environment.getExternalStorageDirectory();

        try {
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return blockSize * availableBlocks;
        } catch (Exception e) {
            Log.e(TAG,"get sysDataSize exception:",e);
            return 0;
        }
    }

    public static String enCode(String code) {
        byte[] result = desCrypto(code.getBytes(), KEY);
        LogUtils.d(TAG, "encode result:" + new String(result));
        return new String(result);
    }

    public static String deCode(String code) {
        try {
            LogUtils.d(TAG, "decode code:" + code);
            byte[] decryResult = deCrypt(code.getBytes(), KEY);
            return new String(decryResult);
        } catch (Exception e) {
            Log.e(TAG, "decode exception:",e);
            return null;
        }
    }

    /**
     * Encryption the string
     */

    public static byte[] desCrypto(byte[] datasource, String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] key = Base64.encode(password.getBytes(), Base64.DEFAULT);
            DESKeySpec desKey = new DESKeySpec(key);
            //create SecretKeyFactory
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
            SecretKey securekey = keyFactory.generateSecret(desKey);
            //use Cipher to encode
            Cipher cipher = Cipher.getInstance(DES);
            //init Cipher instance
            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
            //get data and encode
            byte[] data = cipher.doFinal(datasource);
            return Base64.encode(data, Base64.DEFAULT);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decode the string
     */

    public static byte[] deCrypt(byte[] src, String password) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] key = Base64.encode(password.getBytes(), Base64.DEFAULT);
        byte[] data = Base64.decode(src, Base64.DEFAULT);
        //Create a DESKeySpec instance
        DESKeySpec desKey = new DESKeySpec(key);
        //Create a SecretKeyFactory
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        //Translate DESKeySpec instance to SecretKey instance
        SecretKey securekey = keyFactory.generateSecret(desKey);
        //Use Cipher instance to decode
        Cipher cipher = Cipher.getInstance(DES);
        //Init Chipher instance
        cipher.init(Cipher.DECRYPT_MODE, securekey, random);
        //Decode
        return cipher.doFinal(data);
    }
    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }
}
