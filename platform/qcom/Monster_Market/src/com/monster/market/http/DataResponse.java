package com.monster.market.http;

public abstract class DataResponse<T>  {
	
	public int code;
	public T value;
	
	public abstract void onResponse(T value);
	public abstract void onErrorResponse(RequestError error);

}
