package com.monster.market.http;

public interface IHttpCallback {
	
	void onResponse(String result);
	void onErrorResponse(RequestError error);

}
