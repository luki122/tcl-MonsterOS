/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xdja.safecenter.ckms.opcode.OpCodeFactory;
import com.xdja.safekeyservice.jarv2.EntityManager;
import com.xdja.safekeyservice.jarv2.SecurityGroupManager;
import com.xdja.safekeyservice.jarv2.SecuritySDKManager;
import com.xdja.safekeyservice.jarv2.bean.ChallengeResult;
import com.xdja.safekeyservice.jarv2.bean.IVerifyPinResult;
import com.xdja.safekeyservice.jarv2.bean.InitResult;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cn.tcl.filemanager.fragment.FileBrowserFragment;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.manager.FileInfoComparator;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.service.FileManagerService;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.JSONObjectUtils;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;
import cn.tcl.filemanager.utils.ToastHelper;
//import FavoriteManager;

public class FileManagerApplication extends Application {


    public String mCurrentPath;
    //modify by liaoah
    public String mEncryptTargePath;
    //modify end
    public int mSortType = FileInfoComparator.SORT_BY_TIME;
    public FileManagerService mService;
    public FileInfoManager mFileInfoManager;
    public CategoryManager mCategoryManager;
    //public FavoriteManager mFavoriteManager;
//    public ShortCutManager mShortcutManager; // MODIFIED by haifeng.tang, 2016-04-25,BUG-1989942
    public static final int DETETE = 10001;
    public static final int RENAME = 10002;
    public static final int OTHER = 10003;
    public static final int PASTE = 10004;
    public static final int SELECT_ALL= 10005;
    public int currentOperation = OTHER;
    public List<FileInfo> mFileInfoList;
    public List<FileInfo> mSearchResultList;
    public String mRecordSearchPath;

    private Gson mGson;

    private static String mStrImei;

    private ToastHelper mToastHelper;

    /**
     * true: has verify system pwd and pass
     * false: unverify or verify pwd false
     */
    public boolean mIsVerifySystemPwd = false;
    public boolean mIsCategorySafe = false;
    public boolean mIsSafeMove = false;

    private static final String TAG = "FileManagerApplication";

    private FileBrowserFragment.SdkInitSuccess mSdkInitSuccess;
    private static final int SDK_INIT_EXCEPTION = 0X01;
    private static final int SDK_INIT_SUCCESS = 0X02;
    private static final int SECRYPT_CODE_ERROR = 0X03;
    private static final int SDK_NEED_VERIFY = 50008;
    private static final int NETWORK_ERROR = 70001;
    private static final int SDK_INIT_NOT_LEGAL = 70261;
    private static final int SGROUP_HAS_EXIST = 70515;
    private static final int ENTITY_HAS_EXIST = 71793;

    public static final int FILE_ENCRYPT = 0X01;
    public static final int FILE_DECRYPT = 0X02;

    private int mFileStatus;


    public void getInitSecurityStatus(FileBrowserFragment.SdkInitSuccess sdkInitSuccess, int file_status) {
        mSdkInitSuccess = sdkInitSuccess;
        mFileStatus = file_status;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, FileManagerService.class));
        mToastHelper = new ToastHelper(getApplicationContext());
        getGson();
    }

    public void initSecuritySdk() {
        (new InitSecuritySDKTask()).execute();
    }

    public Gson getGson() {
        if (null == mGson) {
            GsonBuilder builder = new GsonBuilder();
            mGson = builder.create();
        }
        return mGson;
    }

    /**
     * get challenge 4.2.1.2
     */
    private void getChallenge() {
        try{
            //get challenge
            JSONObject challengeResult
                    = SecuritySDKManager.getInstance().getChallenge(getApplicationContext());
            int resultCode = JSONObjectUtils.getResultCode(challengeResult);
            if (resultCode == 0) {
                JSONObject resultObject = JSONObjectUtils.getResultObject(challengeResult);
                if (resultObject != null) {
                    ChallengeResult cr = mGson.fromJson(resultObject.toString(), ChallengeResult.class);
                    String challenge = cr.getChallenge();
                    if (!TextUtils.isEmpty(challenge)) {
                        LogUtils.d(TAG, "Get the challenge value successfully, the challenge value is：" + challenge);
                        challenge = SafeUtils.getSignatureOfOpCode(challenge);
                        if (!TextUtils.isEmpty(challenge)) {
                            initSDK(challenge);
                        } else {
                            LogUtils.d(TAG, "security challenge value is null");
                        }
                        LogUtils.d(TAG, "getChallenge--initSDK");
                    } else {
                        LogUtils.d(TAG, "challenge value is null");
                    }
                } else {
                    LogUtils.d(TAG, "JSONObject is null");
                }
            } else {
                Message msg = Message.obtain(mHandler,resultCode,0,0,JSONObjectUtils.getResultError(challengeResult));
                mHandler.sendMessage(msg);
                LogUtils.d(TAG, "Get the challenge value false，message ： " + JSONObjectUtils.getResultError(challengeResult));
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtils.e(TAG, "Chip manager application not installed!!");
        }
    }

    /**
     * init sdk  4.2.1.1
     * @param challenge get challenge value
     */
    private void initSDK(String challenge) {

        final CountDownLatch latch = new CountDownLatch(1);

        SecuritySDKManager.getInstance().init(
                getApplicationContext(),
                challenge,
                new SecuritySDKManager.InitCallBack() {
                    @Override
                    public void onInitComplete(JSONObject result) {
                        int resultCode = JSONObjectUtils.getResultCode(result);
                        LogUtils.d(TAG, "resultCode：" + resultCode);
                        if (resultCode == 0) {
                            JSONObject resultObject = JSONObjectUtils.getResultObject(result);
                            if (resultObject != null) {
                                InitResult ir = mGson.fromJson(resultObject.toString(), InitResult.class);
                                long valid_hours = ir.getValid_hours();
                                if (valid_hours > 0) {
                                    LogUtils.d(TAG, "The initialization is successful,valid time：" + valid_hours);
                                } else {
                                    LogUtils.d(TAG, "The initialization is false,valid time：" + valid_hours);
                                    SecuritySDKManager.getInstance().refresh();
                                }
                                create();
                                latch.countDown();
                            } else {
                                LogUtils.d(TAG, "JSONObject is null");
                                mHandler.sendEmptyMessage(SDK_INIT_EXCEPTION);
                                latch.countDown();
                            }
                        } else {
                            LogUtils.d(TAG, "init sdk is false，message ： " + JSONObjectUtils.getResultError(result));
                            Message msg = Message.obtain(mHandler,resultCode,0,0,JSONObjectUtils.getResultError(result));
                            mHandler.sendMessage(msg);
                            latch.countDown();
                        }
                    }
                });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 4.2.2.1 Create a security account
     */
    private void create() {
        String deviceID = SafeUtils.getDeviceID();
        String opcode = OpCodeFactory.Coder().createEntity(deviceID, getImei(this));
        JSONObject create_cellgroup_jsonObject = EntityManager.getInstance().create(getImei(this), SafeUtils.getSignatureOfOpCode(opcode));
        int resultCode = JSONObjectUtils.getResultCode(create_cellgroup_jsonObject);
        LogUtils.d(TAG, "createEntity resultCode：" + resultCode);
        if (resultCode == 0 || resultCode == ENTITY_HAS_EXIST) {
            LogUtils.d(TAG, "create success");
            createSGroup();
        } else {
            LogUtils.d(TAG, "create false, message ： " + JSONObjectUtils.getResultError(create_cellgroup_jsonObject));
            Message msg = Message.obtain(mHandler,resultCode,0,0,JSONObjectUtils.getResultError(create_cellgroup_jsonObject));
            mHandler.sendMessage(msg);
        }
    }

    /**
     * 4.2.3.1 Create a security group by entity
     */
    private void createSGroup() {
        String opcode = OpCodeFactory.Coder().createGroup(SafeUtils.getDeviceID(), getImei(this), getImei(this), new String[]{getImei(this)});
        LogUtils.i(TAG, "createSGroup opcode:" + opcode);
        List<String> list = new ArrayList<>();
        list.add(getImei(this));
        JSONObject jsonDeviceId = EntityManager.getInstance().getDeviceID();
        JSONObject jsonDevices = EntityManager.getInstance().getDevices(list);
        LogUtils.d(TAG, "jsonDeviceId：" + jsonDeviceId + "\n jsonDevices:" + jsonDevices);
        JSONObject jsonObject = SecurityGroupManager.getInstance().createSGroup(getImei(this), getImei(this), list, SafeUtils.getSignatureOfOpCode(opcode));
        int resultCode = JSONObjectUtils.getResultCode(jsonObject);
        LogUtils.d(TAG, "createSGroup resultCode：" + resultCode);
        if (resultCode == 0 || resultCode == SGROUP_HAS_EXIST) {
            LogUtils.d(TAG, "createSGroup success");
            mHandler.sendEmptyMessage(SDK_INIT_SUCCESS);
        } else {
            LogUtils.d(TAG, "createSGroup false, message ： " + JSONObjectUtils.getResultError(jsonObject));
            Message msg = Message.obtain(mHandler,resultCode,0,0,JSONObjectUtils.getResultError(jsonObject));
            mHandler.sendMessage(msg);
        }
    }

    /**
     * get system imei
     * @return system imei value
     */
    public static String getImei(Context context) {
//        try {
//            if (null == mStrImei) {
//                TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
//                mStrImei = tm.getImei();
//            }
//            LogUtils.i(TAG, "mStrImei:" + mStrImei);
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//        if (TextUtils.isEmpty(mStrImei)) {
            long tempImei = SharedPreferenceUtils.getTempImeiValue(context);
            if (tempImei == 0) {
                tempImei = System.currentTimeMillis();
                SharedPreferenceUtils.setTempImeiValue(context, tempImei);
            }
            mStrImei = tempImei + "";
//        }
        LogUtils.i(TAG, "imei:" + mStrImei);
        return mStrImei;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SDK_NEED_VERIFY:
                    LogUtils.i(TAG, "SDK_NEED_VERIFY:" + SDK_NEED_VERIFY);
                    startVerifyPin();
                    break;
                case SDK_INIT_SUCCESS:
                    if (null != mSdkInitSuccess) {
                        mSdkInitSuccess.sdkInitSuccess();
                        mSdkInitSuccess = null;
                    }
                    break;
            }
            LogUtils.i(TAG, "msg.what:" + msg.what);
            if (msg.what != SDK_INIT_SUCCESS) {
                mSdkInitSuccess.closeProgressDialog();
                if (msg.what == NETWORK_ERROR) {
                    mToastHelper.showToast(msg.obj.toString());
                } else if (msg.what != SDK_NEED_VERIFY) {
                    if (mFileStatus == FILE_DECRYPT) {
                        mToastHelper.showToast(R.string.file_decrypt_fail);
                    } else {
                        mToastHelper.showToast(R.string.file_encrypt_fail);
                    }
                }
            }
        }
    };

    public void startVerifyPin() {
        SecuritySDKManager.getInstance().startVerifyPinActivity(getApplicationContext(), new IVerifyPinResult() {
            @Override
            public void onResult(int i, String s) {
                LogUtils.d(TAG, "start verifypin i ： " + i + ",s:" + s);
                if (i == 0) {
                    mSdkInitSuccess.showProgressDialog();
                    initSecuritySdk();
                } else {
                    mHandler.sendEmptyMessage(SECRYPT_CODE_ERROR);
                }
            }
        });

    }

    class InitSecuritySDKTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
//            getChallenge();
            mHandler.sendEmptyMessage(SDK_INIT_SUCCESS);
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

        }
    }

} // MODIFIED by haifeng.tang, 2016-04-25,BUG-1989926
