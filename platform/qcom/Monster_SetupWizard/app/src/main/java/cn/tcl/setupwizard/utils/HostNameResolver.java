/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.utils;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * used to parse host, it has priority over the system`s parsing rule.
 */
public class HostNameResolver
{
    private final static String HOST_CONF_FILE = "hosts";
    public static boolean NEED_HOST_RESOVLING = false;
    private static Map<String, String> hostMap = null;

    public static void loadHostConfiguration(String hostsPath) {
        File sd = Environment.getExternalStorageDirectory();
        if (sd == null || !sd.exists())
            return;

        StringBuilder sb = new StringBuilder(sd.getAbsolutePath());
        sb.append(File.separator);
        if (TextUtils.isEmpty(hostsPath))
            sb.append(HOST_CONF_FILE);
        else {
            sb.append(hostsPath);
        }

        String path = sb.toString();
        File file = new File(path);
        if (!file.exists() || file.length() == 0)
            return;

        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), "utf-8");
            Properties properties = new Properties();
            properties.load(reader);

            if (properties.size() == 0)
                return;

            Set<Entry<Object, Object>> entries = properties.entrySet();
            for (Entry<Object, Object> entry: entries) {
                String value = entry.getValue().toString();
                if (TextUtils.isEmpty(value))
                    continue;

                addNameMap(entry.getKey().toString(), value);
            }
        } catch (UnsupportedEncodingException e) {
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void clear() {
        if (hostMap != null) {
            hostMap.clear();
            hostMap = null;
        }
    }

    /**
     * add the mapping relation of domain and IP
     * @param server server`s domain
     * @param ip ip address
     */
    public static void addNameMap(String server, String ip) {
        if (hostMap == null) {
            hostMap = new HashMap<String, String>();
        }

        String sip = hostMap.get(server);
        if (sip == null)
            hostMap.put(server, ip);
        else {
            sip += ";" + ip;
            hostMap.put(server, sip);
        }
    }

    /**
     * parse the specified server`s domain to IP
     * @param server server`s domain
     * @return
     */
    public static String resovleHost(String server) {
        if (!NEED_HOST_RESOVLING || hostMap == null || hostMap.size() == 0) {
            return server;
        }

        String ip = hostMap.get(server);
        if (ip == null)
            return server;

        String[] ips = ip.split(";");
        int index = (int)(Math.random() * 1000000) % ips.length;
        return ips[index];
    }

    /**
     * parse URI to IP, it will return original URI if cannot parse.
     * @param url
     * @return
     */
    public static String resovleURL(String url) {
        if (!NEED_HOST_RESOVLING || hostMap == null || hostMap.size() == 0) {
            return url;
        }

        String realUrl = url;
        try {
            URL u = new URL(url);
            String host = u.getHost();
            String ip = hostMap.get(host);
            if (ip != null) {
                String[] ips = ip.split(";");
                int index = (int)(Math.random() * 1000000) % ips.length;
                realUrl = url.replace(host, ips[index]);
            }
        } catch (MalformedURLException e) {

        }

        return realUrl;
    }
}
