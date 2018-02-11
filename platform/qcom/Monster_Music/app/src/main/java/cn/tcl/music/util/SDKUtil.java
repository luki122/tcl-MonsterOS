package cn.tcl.music.util;

/**
 * 2015-11-03
 */
public class SDKUtil {
    //TODO 打包的时候记得去掉
    public static final String KEY = "67781db321558f8135ca99453627173c";
    public static final String SECRET = "a30d729a5f0c2cdd559af6ba4d1dabda";

    public static final String THIRD_PARTY_TOKEN = "";

    /**
     * 字符串加密
     * @param str
     *            待加密字符串
     * @return 加密后的字符串
     */

    public static String encrypt(String str) {
        String rstStr = "";
        if (str != null && str.length() > 0) {
            char[] charArray = str.toCharArray();
            int j = 0;
            for (int i = 0; i < charArray.length; i++) {
                charArray[i] = (char) (charArray[i] ^ (666 + j));
                if (j++ > 10000) {
                    j = 0;
                }
            }
            rstStr = new String(charArray);
        }
        return rstStr;
    }

    /**
     * 字符串解密
     * @param str 待解密字符串
     * @return 解密后的字符串
     */
    public static String decrypt(String str) {
        return encrypt(str);
    }
}
