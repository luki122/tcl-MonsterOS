package com.monster.cloud.http;

public class Request {

	public enum RequestMethod {
		GET, POST
	}

	public RequestMethod requestMethod = RequestMethod.GET;
	public String url;
	public IHttpCallback iHttpCallback;
	private RequestTask task;
	private String params;

	public int connectTimeout = 15000;

	public Request(String url) {
		this(url, RequestMethod.GET);
	}

	public Request(String url, RequestMethod method) {
		this(url, method, null);
	}

	public Request(String url, RequestMethod method, IHttpCallback iHttpCallback) {
		this.url = url;
		this.requestMethod = method;
		this.iHttpCallback = iHttpCallback;
	}

	public void execute() {
		task = new RequestTask(this);
		task.execute();
	}
	
	public void cancel() {
		if (task != null && !task.isCancelled()) {
			task.cancel(true);
		}
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getParams() {
		return params;
	}

}
