package com.monster.cloud.http;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.monster.cloud.constants.HttpConstant;
import com.monster.cloud.http.data.BaseHttpResultData;
import com.monster.cloud.utils.LogUtil;

import java.io.StringReader;

public class BaseJsonParseCallback<T> implements IHttpCallback {

	public static final String TAG = "BaseJsonParseCallback";

	DataResponse<T> response;
	Class<T> classOfType;
	
	public BaseJsonParseCallback(DataResponse<T> response, Class<T> classOfType) {
		this.response = response;
		this.classOfType = classOfType;
	}

	@Override
	public void onResponse(String result) {
		LogUtil.i(TAG, "onResponse result:" + result);

		parseBaseResultData(result, classOfType);
	}

	@Override
	public void onErrorResponse(RequestError error) {
		if (response != null) {
			response.onErrorResponse(error);
		}
	}
	
	private void parseBaseResultData(String result, Class<T> classOfType) {
		if (response != null) {
			Gson gson = new Gson();
			try {
				StringReader sr = new StringReader(result);
				BaseHttpResultData baseResult = gson.fromJson(sr, BaseHttpResultData.class);
				if (baseResult != null) {
					if (baseResult.getRetCode() == HttpConstant.RESULT_CODE_SUCCESS) {
						String body = baseResult.getBody().toString();
						T data = gson.fromJson(body, classOfType);

						if (data != null) {
							response.value = data;
							response.onResponse(response.value);
						} else {
							response.onErrorResponse(new RequestError(
									RequestError.ERROR_JSONPARSE, "data json parse error"));
						}
					} else {
						response.onErrorResponse(new RequestError(
								RequestError.ERROR_OTHER, "result code is "
								+ baseResult.getRetCode(), String.valueOf(baseResult.getRetCode())));
					}
				} else {
					response.onErrorResponse(new RequestError(
							RequestError.ERROR_JSONPARSE, "base json parse error"));
				}
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				response.onErrorResponse(new RequestError(
						RequestError.ERROR_JSONPARSE, "json parse error"));
			} catch (Exception e) {
				e.printStackTrace();
				response.onErrorResponse(new RequestError(
						RequestError.ERROR_JSONPARSE, "json parse error"));
			}
		}
	}

}
