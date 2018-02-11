package com.monster.cloud.http.data;

import com.google.gson.JsonObject;


public class BaseHttpResultData {

	private int retCode;
	private JsonObject body;

	public int getRetCode() {
		return retCode;
	}

	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(JsonObject body) {
		this.body = body;
	}

}
