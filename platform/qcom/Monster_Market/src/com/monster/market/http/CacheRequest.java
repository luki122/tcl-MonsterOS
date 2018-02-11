package com.monster.market.http;

/**
 * Created by xiaobin on 16-11-22.
 */
public class CacheRequest extends Request {

    public int type = -1;
    public int subId = -1;
    public int pageIndex = -1;

    public CacheRequest(String url) {
        super(url);
    }

    public CacheRequest(String url, RequestMethod method) {
        super(url, method);
    }

    public CacheRequest(String url, RequestMethod method, IHttpCallback iHttpCallback) {
        super(url, method, iHttpCallback);
    }

    @Override
    public void execute() {
        task = new CacheRequestTask(this);
        task.execute();
    }
}
