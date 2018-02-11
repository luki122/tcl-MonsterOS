/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.setupwizard.utils;

import cn.tcl.setupwizard.R;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

/**
 * Util class for config setting, including language and font size for now
 */
public class LanguageUtils {
    public static final boolean DEBUG = true;
    private static final String TAG = "LanguageUtils";
    private static LocaleInfo[] mLocaleInfos;

    /**
     * get all labels for all languages
     */
    public static String[] getLanguages(Context context) {
        mLocaleInfos = getLocaleInfos(context);

        String[] labels = new String[mLocaleInfos.length];
        for (int i = 0; i < mLocaleInfos.length; i++) {
            labels[i] = mLocaleInfos[i].getLabel();
        }

        return labels;
    }

    /**
     * get label for current language
     */
    public static String getCurrentLanguage() {
        if (0 != mLocaleInfos.length) {
            try {
                // IActivityManager am = ActivityManagerNative.getDefault();
                // Configuration config = am.getConfiguration();
                Configuration config = getConfiguration();
                if (config != null) {
                    Locale locale = config.locale;

                    for (int i = 0; i < mLocaleInfos.length; i++) {
                        if (locale.equals(mLocaleInfos[i].getLocale())) {
                            return mLocaleInfos[i].getLabel();
                        }
                    }
                }
                return mLocaleInfos[0].getLabel();
            } catch (Exception e) {
                if (DEBUG) {
                    Log.d(TAG, "Can't get current locale");
                }
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * set language by current label. Note that system will halt for a while
     * during locale migration, so need to take care of it.
     */
    public static String setLanguage(String label) {
        if (0 != mLocaleInfos.length) {
            Locale locale = null;
            for (int i = 0; i < mLocaleInfos.length; i++) {
                if (label.equals(mLocaleInfos[i].getLabel())) {
                    locale = mLocaleInfos[i].getLocale();
                }
            }

            if (null != locale) {
                try {
                    // IActivityManager am = ActivityManagerNative.getDefault();
                    // Configuration config = am.getConfiguration();
                    // config.setLocale(locale);
                    // config.userSetLocale = true;
                    // am.updateConfiguration(config);
                    // BackupManager.dataChanged("com.android.providers.settings");
                    changeLanguage(locale);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /*
     * get the configuration
     */
    private static Configuration getConfiguration() {
        Configuration config = null;
        try {
            Class amnClass = Class.forName("android.app.ActivityManagerNative");
            Object amn = null;

            // amn = ActivityManagerNative.getDefault();
            Method methodGetDefault = amnClass.getMethod("getDefault");
            methodGetDefault.setAccessible(true);
            amn = methodGetDefault.invoke(amnClass);

            // config = amn.getConfiguration();
            Method methodGetConfiguration = amnClass.getMethod("getConfiguration");
            methodGetConfiguration.setAccessible(true);
            config = (Configuration) methodGetConfiguration.invoke(amn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }

    /**
     * change the language by local
     *
     * @param locale
     */
    public static void changeLanguage(Locale locale) {
        try {
            Class amnClass = Class.forName("android.app.ActivityManagerNative");
            Object amn = null;
            Configuration config = null;

            // amn = ActivityManagerNative.getDefault();
            Method methodGetDefault = amnClass.getMethod("getDefault");
            methodGetDefault.setAccessible(true);
            amn = methodGetDefault.invoke(amnClass);

            // config = amn.getConfiguration();
            Method methodGetConfiguration = amnClass.getMethod("getConfiguration");
            methodGetConfiguration.setAccessible(true);
            config = (Configuration) methodGetConfiguration.invoke(amn);

            // config.userSetLocale = true;
            Class configClass = config.getClass();
            Field f = configClass.getField("userSetLocale");
            f.setBoolean(config, true);

            // set the locale to the new value
            config.locale = locale;
            config.setLocale(locale);

            // amn.updateConfiguration(config);
            Method methodUpdateConfiguration = amnClass.getMethod("updateConfiguration",
                    Configuration.class);
            methodUpdateConfiguration.setAccessible(true);
            methodUpdateConfiguration.invoke(amn, config);
            Log.e(TAG,"the locate language is "+config.locale.getLanguage());
            BackupManager.dataChanged("com.android.providers.settings");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * @return The list of system locale information.
     */
    private static LocaleInfo[] getLocaleInfos(Context context) {
        final Resources resources = context.getResources();
        final String[] locales = Resources.getSystem().getAssets().getLocales();

        final String[] specialLocaleCodes = resources.getStringArray(R.array.special_locale_codes);
        final String[] specialLocaleNames = resources.getStringArray(R.array.special_locale_names);

        Arrays.sort(locales);

        final int origSize = locales.length;
        final LocaleInfo[] preprocess = new LocaleInfo[origSize];
        int finalSize = 0;
        for (int i = 0; i < origSize; i++) {
            final String s = locales[i];
            final int len = s.length();
            if (len == 5) {
                String language = s.substring(0, 2);
                String country = s.substring(3, 5);
                final Locale l = new Locale(language, country);

                if (finalSize == 0) {
                    if (DEBUG) {
                        Log.v(TAG, "adding initial " + toTitleCase(l.getDisplayLanguage(l)));
                    }
                    preprocess[finalSize++] = new LocaleInfo(toTitleCase(l.getDisplayLanguage(l)),
                            l);
                } else {
                    // check previous entry:
                    // same language and a country -> upgrade to full name and
                    // insert ours with full name
                    // different language -> insert ours with language-only name
                    if (preprocess[finalSize - 1].locale.getLanguage().equals(language)) {
                        if (DEBUG) {
                            Log.v(TAG,
                                    "backing up and fixing "
                                            + preprocess[finalSize - 1].label
                                            + " to "
                                            + getDisplayName(preprocess[finalSize - 1].locale,
                                                    specialLocaleCodes, specialLocaleNames));
                        }
                        preprocess[finalSize - 1].label = toTitleCase(getDisplayName(
                                preprocess[finalSize - 1].locale, specialLocaleCodes,
                                specialLocaleNames));
                        if (DEBUG) {
                            Log.v(TAG,
                                    "  and adding "
                                            + toTitleCase(getDisplayName(l, specialLocaleCodes,
                                                    specialLocaleNames)));
                        }
                        preprocess[finalSize++] = new LocaleInfo(toTitleCase(getDisplayName(l,
                                specialLocaleCodes, specialLocaleNames)), l);
                    } else {
                        String displayName;
                        if (s.equals("zz_ZZ")) {
                            displayName = "Pseudo...";
                        } else {
                            displayName = toTitleCase(l.getDisplayLanguage(l));
                        }
                        if (DEBUG) {
                            Log.v(TAG, "adding " + displayName);
                        }
                        preprocess[finalSize++] = new LocaleInfo(displayName, l);
                    }
                }
            }
        }

        final LocaleInfo[] localeInfos = new LocaleInfo[finalSize];
        for (int i = 0; i < finalSize; i++) {
            localeInfos[i] = preprocess[i];
        }
        Arrays.sort(localeInfos);

        return localeInfos;
    }

    /*
     * @param s The string to transform
     * @return Formated string with first letter capitalized
     */
    private static String toTitleCase(String s) {
        if (s.length() == 0) {
            return s;
        }

        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /*
     * @return The display name for special locale
     */
    private static String getDisplayName(Locale l, String[] specialLocaleCodes,
            String[] specialLocaleNames) {
        String code = l.toString();

        for (int i = 0; i < specialLocaleCodes.length; i++) {
            if (specialLocaleCodes[i].equals(code)) {
                return specialLocaleNames[i];
            }
        }

        return l.getDisplayName(l);
    }

    public static class LocaleInfo implements Comparable<LocaleInfo> {
        final Collator sCollator = Collator.getInstance();

        private String label;

        private Locale locale;

        public LocaleInfo(String label, Locale locale) {
            this.label = label;
            this.locale = locale;
        }

        public String getLabel() {
            return label;
        }

        public Locale getLocale() {
            return locale;
        }

        @Override
        public String toString() {
            return this.label;
        }

        public int compareTo(LocaleInfo another) {
            return sCollator.compare(this.label, another.label);
        }
    }
}
