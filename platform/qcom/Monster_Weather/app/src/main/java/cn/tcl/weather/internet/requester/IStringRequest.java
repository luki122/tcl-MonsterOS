/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet.requester;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-8-17.
 */
public class IStringRequest extends StringRequest {
    public IStringRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    public IStringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String str = null;
        try {
            str = new String(response.data, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Response.success(str, HttpHeaderParser.parseCacheHeaders(response));
    }
}
