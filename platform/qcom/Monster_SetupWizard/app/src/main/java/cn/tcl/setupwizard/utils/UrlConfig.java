/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public final class UrlConfig {

    private static final String PROTOCOL_HOST_T = "https://safe.tclclouds.com/";
    private static final String PROTOCOL_HOST_I = "https://login.tclclouds.com/";
    private static final String PROTOCOL_HOST_O = "https://login.alcatelonetouch.com/";
    private static String PROTOCOL_HOST = PROTOCOL_HOST_O;
    @Deprecated
    private static final String ROOT_API = "wap/";//V1.0
    @Deprecated
    private static final String ROOT_API_V1_0_1 = "page/1.0.1/wap/";//V1.0.1
    private static final String ROOT_API_V1_1 = "page/1.1/wap/";//V1.1

    private static final String REGISTER_URI = "reg.html";

    private static final String LOGIN_URI = "login.html";

    private static final String FORGOT_PWD_URI = "forgot.html";

    private static final String ACTIVATE_URI = "verify_active.html";

    private static final String BIND_URI = "active.html";

    private static final String DOMAIN_GLOBAL = "GLOBAL";
    private static final String DOMAIN_CHINA = "CHINA";
    private static final String DOMAIN_DEFAULT = DOMAIN_GLOBAL;

    public static void init(Context context) {
        Context appContext = context.getApplicationContext();
        String defaultDomain = MetaUtil.getMetaData(appContext, "TCL_SDK_DOMAIN_VERSION");
        if (TextUtils.isEmpty(defaultDomain)) {
            defaultDomain = DOMAIN_DEFAULT;
        }

        String domain = defaultDomain;
        InputStream is = null;
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            path += File.separator + "tcl_sdk_domain_version.properties";
            File file= new File(path);
            if (file.exists()) {
                is = new FileInputStream(file);
            } else {
                AssetManager am = appContext.getAssets();
                is = am.open("tcl_sdk_domain_version.properties");
            }

            InputStreamReader reader = new InputStreamReader(is, "utf-8");
            Properties properties = new Properties();
            properties.load(reader);
            domain = properties.getProperty("domain_version", DOMAIN_DEFAULT);

            if (TextUtils.isEmpty(domain)) {
                domain = defaultDomain;
            }

        } catch (Exception e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        if (DOMAIN_GLOBAL.equalsIgnoreCase(domain)) {
            PROTOCOL_HOST = PROTOCOL_HOST_O;
        }

        else if (DOMAIN_CHINA.equalsIgnoreCase(domain)) {
            PROTOCOL_HOST = PROTOCOL_HOST_I;
        }

        else {
            PROTOCOL_HOST = PROTOCOL_HOST_T;
        }

        HostNameResolver.NEED_HOST_RESOVLING = true;
        HostNameResolver.loadHostConfiguration(null);
        PROTOCOL_HOST = HostNameResolver.resovleURL(PROTOCOL_HOST);
        HostNameResolver.clear();
        HostNameResolver.NEED_HOST_RESOVLING = false;
    }

    public static String getDomainUrl(){
        return PROTOCOL_HOST;
    }

    static String getIndexUrl(){
        return getLoginURL();
    }

    private static String getAbsoluteURI(String uri) {
        StringBuilder sb = new StringBuilder(PROTOCOL_HOST);
        sb.append(ROOT_API_V1_1);
        sb.append(uri);
        return sb.toString();
    }

    private static String getRegisterURL() {
        return getAbsoluteURI(REGISTER_URI);
    }

    private static String getLoginURL() {
        return getAbsoluteURI(LOGIN_URI);
    }

    private static String getForgotPWDURL() {
        return getAbsoluteURI(FORGOT_PWD_URI);
    }

    private static String getActivateURL(int accountType) {
        return getAbsoluteURI(ACTIVATE_URI);
    }

    private static String getBindURL() {
        return getAbsoluteURI(BIND_URI);
    }
}

