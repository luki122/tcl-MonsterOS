package com.monster.cloud.http;

public interface IHttpCallback {
	
	void onResponse(String result);
	void onErrorResponse(RequestError error);

}
