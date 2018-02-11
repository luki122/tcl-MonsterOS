package cn.tcl.music.database;

import android.text.TextUtils;

import cn.tcl.music.R;
import cn.tcl.music.util.CharacterParser;

/**
 * Created by caoyang on 16-8-30.
 * <p/>
 * The order is
 * A~Z( English, chinese )
 * #( Japan, Korean, Number, else, unknown)
 */
public class OrderUtils {

    public final static String TAG = "OrderUtils";
    private static final String IS_ENGLISH = "1 - ";
    private static final String IS_CHINESE = "2 - ";
    private static final String IS_JAPAN = "zzzz1 - ";
    private static final String IS_KOREAN = "zzzz2 - ";
    private static final String IS_NUMBER = "zzzz3 - ";
    private static final String IS_ELSE = "zzzz4 - ";
    private static final String IS_UNKNOWN = "zzzz5 - ";

    public static String keyFor(String title) {
        String key = "";
        if (!TextUtils.isEmpty(title)) {
            if (title.equals(R.string.unknown)) {
                key = IS_UNKNOWN + title;
            } else if (isLetter(title)) {
                key = title.substring(0,1).toLowerCase() + "1_" + title.toLowerCase();
            } else if (isChinese(title)) {
                CharacterParser characterParser = new CharacterParser();
                String pinyin = characterParser.getSelling(title);
                key = pinyin.substring(0,1).toLowerCase() + "2_" + pinyin.toLowerCase();
            } else if (isJapan(title)) {
                key = IS_JAPAN + title;
            } else if (isKorean(title)) {
                key = IS_KOREAN + title;
            } else if (isNumber(title)) {
                key = IS_NUMBER + title;
            } else {
                key = IS_ELSE + title;
            }
        }
        return key;
    }

    //is English
    private static boolean isLetter(String s) {
        char c = s.charAt(0);
        int in = (int) c;
        if ((in >= 65 && in <= 90) || (in >= 97 && in <= 122)) {
            return true;
        } else {
            return false;
        }
    }

    //is Chinese
    private static boolean isChinese(String s) {
        char c = s.charAt(0);
        int in = (int) c;
        if (in >= '一' && in <= '龥') {
            return true;
        } else {
            return false;
        }
    }

    //is Japan
    private static boolean isJapan(String s) {
        char c = s.charAt(0);
        int in = (int) c;
        if ((in >= '\u3040' && in <= '\u309F') || (in >= '\u30A0' && in <= '\u30FF')) {
            return true;
        } else {
            return false;
        }
    }

    //is Korean
    private static boolean isKorean(String s) {
        char c = s.charAt(0);
        int in = (int) c;
        if (in >= '\uAC00' && in <= '\uD7AF') {
            return true;
        } else {
            return false;
        }
    }

    //is Number
    private static boolean isNumber(String s) {
        char c = s.charAt(0);
        int in = (int) c;
        if (in >= 48 && in <= 57) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 根据artist生成相应的key
     * @param artist    歌手姓名
     * @return
     */
    public static String keyForArtist(String artist) {
        String key = "";
        if (!TextUtils.isEmpty(artist)) {
            if (isLetter(artist)) {
                key = artist.substring(0,1).toLowerCase() + "1_" + artist.toLowerCase();
            } else if (isChinese(artist)) {
                CharacterParser characterParser = new CharacterParser();
                String pinyin = characterParser.getSelling(artist);
                key = pinyin.substring(0,1).toLowerCase() + "2_" + pinyin.toLowerCase();
            } else if (isJapan(artist)) {
                key = IS_JAPAN + artist;
            } else if (isKorean(artist)) {
                key = IS_KOREAN + artist;
            } else if (isNumber(artist)) {
                key = IS_NUMBER + artist;
            } else {
                key = IS_UNKNOWN + artist;
            }
        }
        return key;
    }

    /**
     * 根据album生成相应的key
     * @param album    专辑名称
     * @return
     */
    public static String keyForAlbum(String album) {
        String key = "";
        if (!TextUtils.isEmpty(album)) {
            if (isLetter(album)) {
                key = album.substring(0,1).toLowerCase() + "1_" + album.toLowerCase();
            } else if (isChinese(album)) {
                CharacterParser characterParser = new CharacterParser();
                String pinyin = characterParser.getSelling(album);
                key = pinyin.substring(0,1).toLowerCase() + "2_" + pinyin.toLowerCase();
            } else if (isJapan(album)) {
                key = IS_JAPAN + album;
            } else if (isKorean(album)) {
                key = IS_KOREAN + album;
            } else if (isNumber(album)) {
                key = IS_NUMBER + album;
            } else {
                key = IS_UNKNOWN + album;
            }
        }
        return key;
    }
}
