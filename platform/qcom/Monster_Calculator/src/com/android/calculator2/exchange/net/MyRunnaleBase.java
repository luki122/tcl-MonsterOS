package com.android.calculator2.exchange.net;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public abstract class MyRunnaleBase implements Runnable {

    private Handler mHandler;
    private static final int IS_NET_REQUEST = 1;

    public abstract Object executeRunnableRequestData();

    public MyRunnaleBase(Context context, final int funcid,final ResponseResultInterface responseResultInterface) {
        
        if (Looper.myLooper() != null) {
            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                    case IS_NET_REQUEST:
                        if (responseResultInterface != null) {
                            responseResultInterface.OnResponseResults(funcid,msg.obj);
                        }
                        break;

                    default:
                        break;
                    }
                }
            };
        }
        MyThreadPool.dentalThreadExecute(this);
    }

    @Override
    public void run() {
        Object object = executeRunnableRequestData();
        if (null != mHandler) {
            Message msg = new Message();
            msg.obj = object;
            msg.what = IS_NET_REQUEST;
            mHandler.sendMessage(msg);
        }
    }

}
