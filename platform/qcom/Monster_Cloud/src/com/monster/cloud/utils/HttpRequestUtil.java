package com.monster.cloud.utils;

import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by yubai on 16-11-17.
 */
public class HttpRequestUtil {

    /**
     * https get request
     */
    public static HttpsURLConnection sendGetRequest(String urlString, Map<String, String> params,
                                                    Map<String, String> headers) {
        try {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(urlString);
            if (null != params) {
                urlBuilder.append("?");

                Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> param = iterator.next();
                    urlBuilder
                            .append(URLEncoder.encode(param.getKey(), "UTF-8"))
                            .append('=')
                            .append(URLEncoder.encode(param.getValue(), "UTF-8"));
                    if (iterator.hasNext()) {
                        urlBuilder.append('&');
                    }
                }
            }


            TrustManager[] trustManagers = {new CloudX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, new java.security.SecureRandom());

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            URL url = new URL(urlBuilder.toString());
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sslSocketFactory);

            conn.setRequestMethod("GET");

            if (null != headers && !headers.isEmpty()) {
                Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> param = iterator.next();
                    conn.setRequestProperty(param.getKey(), param.getValue());
                }
            }
            conn.getResponseCode();
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // HTTPS的信任管理类
    static class CloudX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

}
