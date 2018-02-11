/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.util;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class XmlHash {
    private final static String TAG = XmlHash.class.getSimpleName();
    private static String mInitHash;
    //the string use save every time
    private static String mSaveHash = "";

    public static String getHash(String xmlString) {
        NoteLog.d(TAG, "start hash");
        long startTime = System.currentTimeMillis();
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("MD5").digest(xmlString.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            NoteLog.e(TAG, "NoSuchAlgorithmException", e);
        } catch (UnsupportedEncodingException e) {
            NoteLog.e(TAG, "UnsupportedEncodingException", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        String str = hex.toString();
        NoteLog.d(TAG, "hex str=" + str + " take time:" + (System.currentTimeMillis() - startTime));
        return str;
    }

    /**
     * when go in edit view,then save hash
     *
     * @param xmlString
     * @return
     */
    public static void initHash(String xmlString) {
        mInitHash = getHash(xmlString);
        mSaveHash = mInitHash;
        NoteLog.d(TAG, "init hash:" + mInitHash);
    }

    /**
     * when back save,whether have modify,if have,return true;
     *
     * @param xmlString
     * @return
     */
    public static boolean iSSameWithInit(String xmlString) {
        String nowHash = getHash(xmlString);
        if (nowHash.equals(mInitHash)) {
            NoteLog.d(TAG, "it is same hash with init");
            return true;
        } else {
            NoteLog.d(TAG, "it is not same hash with init");
            return false;
        }
    }

    public static boolean iSSameWithBeforeSave(String xmlString) {
        String nowHash = getHash(xmlString);
        if (nowHash.equals(mSaveHash)) {
            NoteLog.d(TAG, "it is same hash with save hash");
            return true;
        } else {
            mSaveHash = nowHash;
            NoteLog.d(TAG, "it is not same hash with save hash");
            return false;
        }
    }
}
