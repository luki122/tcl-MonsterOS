package com.monster.launcher.theme;

import android.util.Xml;

import com.monster.launcher.Log;
import com.monster.launcher.theme.interfaces.Contents;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by lj on 16-10-24.
 */
public class ZIPThemeConfigParseByPull {

    //com.monster.appmanager$com.monster.appmanager.MainActivity|应用管理#tcl_appman.png
    public static final String TAG = "ZIPThemeConfigParseByPull";
    private static final String THEME_NAME = "theme_name";

    private HashMap<String, String> mLabel_Icons;
    private String themeName;
    private boolean isHeteromorphicTheme = false;
    private int pTop = 0;
    private int pLeft = 0;
    private int pRight = 0;
    private int pBottom = 0;

    public void parse(InputStream is) throws Exception {

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, "UTF-8");
        int eventType = parser.getEventType();
        HashMap<String, String> packageClassMap = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("string-array")) {
                        String parserName = parser.getAttributeValue(0);
                        if (Contents.LABEL_ICON_MAP_NAME.equals(parserName)) {
                            packageClassMap = new HashMap<String, String>();
                        }
                    } else if (parser.getName().equals("item")) {
                        if (packageClassMap == null) break;
                        eventType = parser.next();
                        String packageClasseIcon = parser.getText();
                        String[] packageClasses_Icon = packageClasseIcon.split("#");
                        if (packageClasses_Icon.length == 2) {
                            String[] packageClasses = packageClasses_Icon[0].split("\\|");
                            for (String s : packageClasses) {
                                packageClassMap.put(s.trim(), packageClasses_Icon[1]);
                                String[] packageClass = s.split("\\$");
                                if (packageClass.length == 2) {
                                    packageClassMap.put(packageClass[0],
                                            packageClasses_Icon[1]);
                                }
                            }
                        }
                        Log.d(TAG, "parse icon:" + packageClasseIcon);
                    } else if (parser.getName().equals("dimen")) {
                        String parserName = null;
                        try {
                            parserName = parser.getAttributeValue(0);
                        } catch (Exception e) {
                            break;
                        }
                        eventType = parser.next();
                        String text = parser.getText();
                        text = text.replace("dp", "");
                        int intText = Integer.parseInt(text);
                        if (Contents.PTOP.equals(parserName)) {
                            pTop = intText;
                        } else if (Contents.PRIGHT.equals(parserName)) {
                            pRight = intText;
                        } else if (Contents.PLEFT.equals(parserName)) {
                            pLeft = intText;
                        } else if (Contents.PBOTTOM.equals(parserName)) {
                            pBottom = intText;
                        }
                        Log.d(TAG, "parse dimen " + parserName + ":" + intText);
                    } else if (parser.getName().equals("string")) {
                        String parserName = parser.getAttributeValue(0);
                        eventType = parser.next();
                        String text = parser.getText();
                        if (THEME_NAME.equals(parserName)) {
                            themeName = text;
                        }
                        Log.d(TAG, "parse string " + parserName + ":" + text);
                    } else if (parser.getName().equals("bool")) {
                        String parserName = parser.getAttributeValue(0);
                        eventType = parser.next();

                        String text = parser.getText();
                        if (Contents.HETEROMORPHIC_THEME.equals(parserName)) {
                            isHeteromorphicTheme = Boolean.parseBoolean(text);
                        }
                        Log.d(TAG, "parse bool " + parserName + ":" + text);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("string-array")) {
                        if (packageClassMap == null) break;
                        mLabel_Icons = packageClassMap;
                        packageClassMap = null;
                        Log.d(TAG, "icon_array parse end :");
                    } else if (parser.getName().equals("item")) {
//                        Log.d(TAG,"item parse end :");
                    }
                    break;
            }
            eventType = parser.next();
        }
    }

    public int getpBottom() {
        return pBottom;
    }

    public int getpRight() {
        return pRight;
    }

    public int getpLeft() {
        return pLeft;
    }

    public int getpTop() {
        return pTop;
    }

    public boolean isHeteromorphicTheme() {
        return isHeteromorphicTheme;
    }

    public String getThemeName() {
        return themeName;
    }


    public HashMap<String, String> getmLabel_Icons() {
        return mLabel_Icons;
    }

    public boolean hasData() {
        if (mLabel_Icons == null || mLabel_Icons.size() <= 0) {
            return false;
        }
        return true;
    }
}
