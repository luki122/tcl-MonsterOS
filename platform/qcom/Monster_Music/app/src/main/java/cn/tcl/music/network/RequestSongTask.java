package cn.tcl.music.network;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.xiami.core.exceptions.AuthExpiredException;
import com.xiami.core.exceptions.ResponseErrorException;
import com.xiami.sdk.XiamiSDK;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.util.Connectivity;
import cn.tcl.music.util.LogUtil;

public abstract class RequestSongTask extends AsyncTask<HashMap<String, Object>, Long, BaseSong> {
    private static final String TAG = RequestSongTask.class.getSimpleName();
    /** default request page size **/
    public static final int REQUEST_PAGE_SIZE = 20;
    /** default request first page number **/
    public static final int REQUEST_PAGE_FIRST = 3;
    private static XiamiSDK mXiamiSDK;
    public Context mContext;
    /** request methos **/
    private String mRequestMethod;
    private Class mBeanClass;
    private Gson mGson = new Gson();

    public RequestSongTask(String requestMethod, Context context, Class beanClass) {
        mXiamiSDK = getXiamiSDKInstance();
        mContext = context;
        mRequestMethod = requestMethod;
        mBeanClass = beanClass;
    }

    /**
     * call back in sub thread
     * @param baseSong
     */
    protected abstract void postInBackground(BaseSong baseSong);

    /**
     * set params,cannot be null,if no params or default params,set new HashMap()
     * @return
     */
    protected abstract HashMap<String, Object> getParams();

    @Override
    public BaseSong doInBackground(HashMap<String, Object>... params) {
        BaseSong baseSong = new BaseSong();
        try {
            HashMap<String, Object> values = getParams();
            if(values == null){
                values = params[0];
            }
            if(values == null){
                return null;
            }

            if(!MusicApplication.isNetWorkCanUsed()){
                LogUtil.d(TAG,"network is not available");
                baseSong.returnCode = DataRequest.Code.CODE_LOAD_FAIL;
                baseSong.returnMessage = "network is not available";
                return baseSong;
            }
            if (!MusicApplication.isNetWorkCanUsed()){
                LogUtil.d(TAG, "can not using network ,did not request xiami data");
                baseSong.returnCode = DataRequest.Code.CODE_LOAD_FAIL;
                baseSong.returnMessage = "network is not available";
                return baseSong;
            }
            LogUtil.d(TAG, "RequestSongTask--load--params = " + values.toString());
            String result = mXiamiSDK.xiamiSDKRequest(mRequestMethod, values);
            LogUtil.d(TAG, "RequestSongTask--load--result= " + result);

            if(TextUtils.isEmpty(result)){
                baseSong.returnCode = DataRequest.Code.CODE_LOAD_FAIL;
                baseSong.returnMessage = "net error:result empty";
                return baseSong;
            }

            result = result.replace("&nbsp;", "\\u0020");
            result = result.replace("&middot;", "\\u00B7");
            result = result.replace("&ndash;", "\\u2013");
            result = result.replace("&hellip;", "\\u2026");
            result = result.replace("&ldquo;", "\\u201C");
            result = result.replace("&rdquo;", "\\u201D");
            result = result.replace("&lsquo;", "\\u2018");
            result = result.replace("&rsquo;", "\\u2019");
            result = result.replace("&mdash;", "\\u2014");
            result = result.replace("&bull;", "\\u00B7");
            result = result.replace("&deg;", "\\u00B0");

            XiamiApiResponse response = mGson.fromJson(result, XiamiApiResponse.class);
            LogUtil.d(TAG, "RequestSongTask--response = "+response );

            if(response == null){
                baseSong.returnCode = DataRequest.Code.CODE_LOAD_FAIL;
                baseSong.returnMessage = result;
                return baseSong;
            }

            int state = response.state;
            JsonElement element = response.data;

            if(state == 0 && element != null && !element.isJsonNull()){
                baseSong = json2BaseSong(element);
                baseSong.returnCode = DataRequest.Code.CODE_LOAD_SUCCESS;
                postInBackground(baseSong);
            }
            else{
                LogUtil.d(TAG, "RequestSongTask--parse failed " );
                baseSong.returnCode = DataRequest.Code.CODE_LOAD_FAIL;
                baseSong.returnMessage = response.message;
            }

        } catch (NoSuchAlgorithmException e) {
            LogUtil.d(TAG, "RequestSongTask--NoSuchAlgorithmException");
            baseSong.returnCode = DataRequest.Code.CODE_LOAD_FAIL;
            baseSong.returnMessage = "no such algorithm exception";
        } catch (IOException e) {
            LogUtil.d(TAG, "RequestSongTask--IOException");
            baseSong.returnCode = DataRequest.Code.CODE_LOAD_FAIL;
            baseSong.returnMessage = "io exception"+e.toString();
        } catch (AuthExpiredException e) {
            LogUtil.d(TAG, "RequestSongTask--AuthExpiredException");
            baseSong.returnCode = DataRequest.Code.CODE_LOAD_FAIL;
            baseSong.returnMessage = "auth expired exception";
        } catch (ResponseErrorException e) {
            LogUtil.d(TAG, "RequestSongTask--ResponseErrorException");
            baseSong.returnCode = DataRequest.Code.CODE_LOAD_FAIL;
            baseSong.returnMessage = "response error exception";
        } catch (Exception e){
            LogUtil.d(TAG, "RequestSongTask--Exception");
            e.printStackTrace();
            baseSong.returnCode = DataRequest.Code.CODE_LOAD_FAIL;
            baseSong.returnMessage = e.toString()+"";
        }

        return baseSong;
    }

    /**
     * 支持多线程运行
     */
    public void executeMultiTask(){
        if(MusicApplication.isNetWorkCanUsed()){
            ((ThreadPoolExecutor)THREAD_POOL_EXECUTOR).setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
            executeOnExecutor(THREAD_POOL_EXECUTOR, new HashMap<String, Object>());
        }
    }

    public void executeMultiTask(HashMap<String, Object> params){
        if(MusicApplication.isNetWorkCanUsed()){
            ((ThreadPoolExecutor)THREAD_POOL_EXECUTOR).setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
            executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        }
    }

    private BaseSong json2BaseSong(JsonElement element){
        BaseSong baseSong = new BaseSong();
        if(element.isJsonArray()){
            JsonArray array = element.getAsJsonArray();
            int len = array.size();
            List list = new ArrayList();
            for(int i = 0; i < len; i++){
                list.add(mGson.fromJson(array.get(i), mBeanClass));
            }
            baseSong.returnArray = list;
        }
        else{
            baseSong = (BaseSong)mGson.fromJson(element, mBeanClass);
        }

        return baseSong;
    }

    private static XiamiSDK getXiamiSDKInstance(){
        if(mXiamiSDK == null){
            synchronized (RequestSongTask.class){
                if(mXiamiSDK == null){
                    mXiamiSDK = new XiamiSDK();
                }

            }
        }
        return mXiamiSDK;
    }
}
