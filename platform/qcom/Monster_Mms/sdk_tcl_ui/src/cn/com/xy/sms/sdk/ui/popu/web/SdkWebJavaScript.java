package cn.com.xy.sms.sdk.ui.popu.web;

import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONObject;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.webkit.JavascriptInterface;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.IccidInfo;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.net.NetWebUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.util.ConversationManager;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.PublicInfoParseManager;
import cn.com.xy.sms.util.SdkCallBack;

/**
 * SDKWEB JAVASCRIPT
 * 
 * @author Administrator
 * 
 */
public class SdkWebJavaScript {
    private IActivityParamForJS mActivityParam;
    private JSONObject mConversionJson;

    private boolean isCallBackData = false;

    public SdkWebJavaScript(IActivityParamForJS activityParam) {

        this.mActivityParam = activityParam;
    }

    @JavascriptInterface
    public void runOnAndroidJavaScript(final String str) {
    }

    @JavascriptInterface
    public String getConfigByKey(String cfKey) {
        return mActivityParam.getParamData(cfKey);
    }

    /* RM-356 zhengxiaobo 20160506 begin */
    @JavascriptInterface
    public void setConfigByKey(String cfKey, String value) {
        mActivityParam.setParamData(cfKey, value);
    }
    
    /* RM-356 zhengxiaobo 20160506 end */
    @JavascriptInterface
    public String getExtendValue(int type, String jsonStr) {
        JSONObject params;
        try {
            params = new JSONObject(jsonStr);
            JSONObject res = DuoquUtils.getSdkDoAction().getExtendValue(type,
                    params);
            if (res != null) {
                return res.toString();
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebJavaScript error ", e);
        }
        return null;
    }

    @JavascriptInterface
    public int checkOrientation() {
        return mActivityParam.checkOrientation();
    }

    @JavascriptInterface
    /**
     * asynchronous request url
     * @param url request address
     * @param postParamValue post params
     * @param callBackJSFunc The callback function name
     */
    public void asyncRequest(String url, String postParamValue,
            final String callBackJSFunc) {
//        Log.i("asyncRequest", "url=" + url + ",postParamValue="
//                + postParamValue);
        XyCallBack callBack = new XyCallBack() {
            @Override
            public void execute(final Object... obj) {
                mActivityParam.getWebView().post(new Runnable() {

                    @Override
                    public void run() {
                        mActivityParam.getWebView().loadUrl(
                                "javascript:" + callBackJSFunc + "('" + obj[0]
                                        + "','" + obj[1] + "')");
                    }
                });

            }
        };
        NetWebUtil.sendPostRequest(url, postParamValue, callBack);
    }

    @JavascriptInterface
    /**
     * asynchronous request url
     * @param url request address
     * @param postParamKey get params by key
     * @param callBackJSFunc The callback function name
     */
    public void asyncRequestByParamKey(String url, String postParamKey,
            final String callBackJSFunc) {
        XyCallBack callBack = new XyCallBack() {
            @Override
            public void execute(final Object... obj) {
                mActivityParam.getWebView().post(new Runnable() {
                    @Override
                    public void run() {
                        mActivityParam.getWebView().loadUrl(
                                "javascript:" + callBackJSFunc + "('"
                                        + (String) obj[0] + "','"
                                        + (String) obj[1] + "')");
                    }
                });

            }
        };
        NetWebUtil.sendPostRequest(url,
                mActivityParam.getParamData(postParamKey), callBack);
    }
    
    @JavascriptInterface
    public boolean downloadApp(String jsonParam) {
        return doAction("download",jsonParam);
    }

    @JavascriptInterface
    public boolean doAction(String actionType, String jsonParam) {
        try {
            if (jsonParam != null) {
                JSONObject jsObj = new JSONObject(jsonParam);
                Iterator<String> it = jsObj.keys();
                if (it != null) {
                    HashMap<String, Object> mapParam = new HashMap<String, Object>();
                    while (it.hasNext()) {
                        String key = it.next();
                        String value = (String) jsObj.get(key);
                        mapParam.put(key, value);
                    }
                    return DuoquUtils.doCustomAction(
                            mActivityParam.getActivity(), actionType, mapParam);
                }
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebJavaScript error ", e);
        }
        return false;
    }

    @JavascriptInterface
    public void closeWebView() {
        mActivityParam.getActivity().finish();
    }

    /**
     * open default service switch
     * 
     * @return succses:0
     */
    @JavascriptInterface
    public long openDefService() {
        return ParseManager.setDefServiceSwitch(mActivityParam.getActivity(),
                "1");
    }

    /**
     * save value
     * 
     * @return succses:0
     */
    @JavascriptInterface
    public long saveValueByKey(String key, String value) {
        long result = -1;
        try {

            SysParamEntityManager.insertOrUpdateKeyValue(
                    mActivityParam.getActivity(), key, value, null);
            result = 0;
        } catch (Throwable e) {
            result = -2;
        }
        return result;
    }

    /**
     * get value
     * 
     * @return succses:0
     */
    @JavascriptInterface
    public String getValueByKey(String key) {
        return SysParamEntityManager.queryValueParamKey(
                mActivityParam.getActivity(), key);
    }

    /**
     * close default service switch
     * 
     * @return succses:0
     */
    @JavascriptInterface
    public long closeDefService() {
        return ParseManager.setDefServiceSwitch(mActivityParam.getActivity(),
                "0");
    }

    /**
     * query default service switch
     * 
     * @return open:1 close:other value
     */
    @JavascriptInterface
    public String queryDefServiceSwitch() {
        return ParseManager.queryDefService(mActivityParam.getActivity());
    }

    /**
     * check has app
     * 
     * @param context
     * @param appName
     * @return
     */
    @JavascriptInterface
    public boolean checkHasAppName(String appName) {
        try {
            PackageManager packageManager = mActivityParam.getActivity()
                    .getPackageManager();

            packageManager.getPackageInfo(appName,PackageManager.GET_ACTIVITIES);

            return true;
        } catch (Throwable e) {

        }
        return false;
    }
    
    /**
     * 根据iccid或simIndex获取省份、省份编码及运营商
     * 优先获取用户设置的省份、省份编码及运营商，若不存在则使用sdk自动获取的省份、省份编码及运营商
     * 
     * @param simIndex -1:获取当前手机默认卡位,省份、省份编码及运营商 0:卡1 1:卡2
     * @return userProvinces：用户设置的省份 
     *         userAreacode：用户设置的省份编码  
     *  	   userOperator：用户设置的运营商（电信|移动|联通）
     */
    @JavascriptInterface
    public JSONObject getUserIccidInfo(int simIndex) {
		JSONObject res = null;
    	try {
    		LogManager.i("SdkWebJavaScript", "queryIccidInfo simIndex==="+simIndex);
			IccidInfo iccidInfo = IccidInfoManager.queryIccidInfo(DuoquUtils
					.getSdkDoAction().getIccidBySimIndex(simIndex), simIndex);
	    	
			if (iccidInfo != null) {
				res = new JSONObject();
				if(StringUtils.isNull(iccidInfo.userProvinces)){
					res.put("userProvinces", iccidInfo.provinces);// 省份
				}else{
					res.put("userProvinces", iccidInfo.userProvinces);// 用户设置的省份
				}
				if(StringUtils.isNull(iccidInfo.userAreacode)){
					res.put("userAreacode", iccidInfo.areaCode);// 省份编码
				}else{
					res.put("userAreacode", iccidInfo.userAreacode);// 用户设置的省份编码
				}
				if(StringUtils.isNull(iccidInfo.userOperator)){
					res.put("userOperator", iccidInfo.operator);// 运营商
				}else{
					res.put("userOperator", iccidInfo.userOperator);// 用户设置的运营商
				}
			}else{
				LogManager.i("SdkWebJavaScript", "queryIccidInfo iccidInfo is null");
			}
		} catch (Throwable e) {
			SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebJavaScript error ", e);
		}
		LogManager.i("SdkWebJavaScript", "getUserIccidInfo res=" + res);
		return res;
    }
    
    /**
     * 保存用户设置的省份、省份编码及运营商
     * 
     * @param iccids sim卡iccid
	 * @param simIndexs sim卡位
	 * @param userProvinces 用户设置的省份信息
	 * @param userAreacodes 用户设置的区域编码信息
	 * @param userOperators 用户设置的运营商信息
	 * @param deft 用户设置的默认sim卡信息
     */
    @JavascriptInterface
	public boolean saveUserIccidInfo(int simIndex, String userProvinces,
			String userAreacode, String userOperator, boolean deft) {
		if (simIndex < 0) {
			return false;
		}
		String iccid = DuoquUtils.getSdkDoAction().getIccidBySimIndex(
				(simIndex));
		LogManager.i("SdkWebJavaScript", "saveUserIccidInfo simIndex==="+simIndex+" ,iccid==="+iccid+" ,userProvinces==="+userProvinces+" ,userAreacode==="+userAreacode+" ,userOperator==="+userOperator);
		boolean result =  IccidInfoManager.insertOrUpdateIccid(iccid, simIndex,
				userProvinces, userAreacode, userOperator, deft);
		if(result){
			Intent intent = new Intent(ParseManager.UPDATE_ICCID_INFO_CACHE_ACTION);
			intent.putExtra("iccid", iccid);
			mActivityParam.getActivity().sendBroadcast(intent);
			LogManager.i("SdkWebJavaScript", "sendBroadcast iccid==="+iccid);
		}
		return result;
	}
    
    /**
     * 删除用户设置的省份、省份编码及运营商
     * @param simIndex
     */
    @JavascriptInterface
    public int clearUserIccidInfo(int simIndex){
    	String iccid = DuoquUtils.getSdkDoAction().getIccidBySimIndex(simIndex);
    	return IccidInfoManager.deleteIccidInfo(iccid, simIndex);
    }
    
    /**
     * 判断是否是双卡手机
     * @return true是|false否
     */
    @JavascriptInterface
    public boolean isDoubleSimPhone(){
    	return DuoquUtils.getSdkDoAction().isDoubleSimPhone();
    }
   
    /**
     * 
     * @param aType 动作类型
     * @param jsObj 其他的jsObj参数
     */
    @JavascriptInterface
    public void queryJson(final String aType,final String jsObj,boolean isNewThread){
    	String type = mActivityParam.getType();
    	if(StringUtils.isNull(type) || !type.startsWith("conversation_")){
            return;
        }

    	if(!StringUtils.isNull(aType) && aType.startsWith("conversation_") 
    			&& !StringUtils.isNull(jsObj)){
    		Thread thread = new Thread(){

    			@Override
    			public void run() {
    				try {
    				    mConversionJson = ConversationManager.queryConversationMsg(mActivityParam.getActivity(),
    				            aType,  new JSONObject(jsObj),  null);
   					 	LogManager.d("queryConversationMsg", "json ="+JsonUtil.jsonObjectToString(mConversionJson));
    					postData(false);

    				} catch (Throwable e) {
    					SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebJavaScript error ", e);
    				}
    				
    			}
        		
        	};
        	if(isNewThread){
            	thread.start();
        	}else{
        		thread.run();
        	}
    	}
    	
    }
    
    public static final int DATA_TYPE_CONVERSATION = 1;
    public void postData(boolean changeFlag){
    	 if(changeFlag){
    		 if(isCallBackData)return;
        	 isCallBackData = true; 
    	 }
    	 /*QIK-592 wangxingjian 20160727 begin*/
    	 mActivityParam.setData(DATA_TYPE_CONVERSATION, mConversionJson);
    	 /*QIK-592 wangxingjian 20160727 end*/
    }
    
    @JavascriptInterface
    public void getNameAndLogoNameByNum(final String phoneNum, final int numType, final int logoType, final String simIccid, final String extendStr,final String callBackJSFunc){
    	SdkCallBack callBack = new SdkCallBack() {
			
			@Override
			public void execute(final Object... obj) {

                mActivityParam.getWebView().post(new Runnable() {
                    @Override
                    public void run() {
                    	try {
                    		 if(obj!=null)
                    		 {
                    			JSONObject json = (JSONObject)obj[0];
           					 	LogManager.d("queryConversationMsg", "json ="+JsonUtil.jsonObjectToString(json));

                    			 mActivityParam.getWebView().loadUrl(
                                         "javascript:" + callBackJSFunc + "('"
         			                            + JsonUtil.jsonObjectToString(json) + "')");
                    		 }
                    		
						} catch (Throwable e) {
							SmartSmsSdkUtil.smartSdkExceptionLog("SdkWebJavaScript error ", e);
						}
                       
                    }
                });

            
			}
		};
		PublicInfoParseManager.getNameAndLogoNameByNum(mActivityParam.getActivity(), 
				phoneNum, numType, logoType, simIccid,JsonUtil.parseJSON2Map(extendStr), callBack);
    }
  
}
