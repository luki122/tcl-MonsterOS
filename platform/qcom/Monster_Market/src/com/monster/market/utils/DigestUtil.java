package com.monster.market.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author wanzheng@wandoujia.com (Zheng Wan)
 */
public class DigestUtil {

    private static MessageDigest messageDigest = null;

    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // private static MessageDigest md5 = null;
    /**
     * '0'-'9' and 'A'-'F'
     */
    private static final byte[] HEX_BYTES = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68,
            69, 70};

    public static synchronized String computeMd5forPkg(byte[] hex) {
        // convert input String to a char[]
        // convert that char[] to byte[]
        // get the md5 digest as byte[]
        // bit-wise AND that byte[] with 0xff
        // prepend "0" to the output StringBuffer to make sure that we don't end
        // up with
        // something like "e21ff" instead of "e201ff"

        if (hex == null) {
            return null;
        }
        int i1;
        int i2;
        byte[] byteBuffers = new byte[2 * hex.length];
        for (int i = 0; i < hex.length; ++i) {
            i1 = (hex[i] & 0xf0) >> 4;
            byteBuffers[2 * i] = HEX_BYTES[i1];
            i2 = hex[i] & 0xf;
            byteBuffers[2 * i + 1] = HEX_BYTES[i2];
        }

        byte[] md5Bytes = messageDigest.digest(byteBuffers);

        StringBuffer hexValue = new StringBuffer();

        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }
}