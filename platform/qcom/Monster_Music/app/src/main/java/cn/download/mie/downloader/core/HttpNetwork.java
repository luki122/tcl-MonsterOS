package cn.download.mie.downloader.core;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpNetwork {

    private static final long timeoutMs = 30000;
    private static OkHttpClient client;

    static {
        client = new OkHttpClient();
        client.setConnectTimeout(timeoutMs, TimeUnit.SECONDS);
    }

    public HttpNetwork() {

    }

    public static Call connect(String url, Map<String, String> additionalHeaders, String method) throws IOException {
        Request.Builder requestBuilder = new Request.Builder();
        if( additionalHeaders != null) {
            for (String headerName : additionalHeaders.keySet()) {
                requestBuilder.addHeader(headerName, additionalHeaders.get(headerName));
            }
        }
        requestBuilder.url(url);
        requestBuilder.method(method, null);
        Request request = requestBuilder.build();
        Call call = client.newCall(request);
        return call;
    }
}
