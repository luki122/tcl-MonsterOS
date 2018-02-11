package cn.tcl.music.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.tcl.account.sdkapi.AuthConfig;
import com.tcl.account.sdkapi.SessionAuthorizationType;
import com.tcl.account.sdkapi.SessionStatusCallback;
import com.tcl.account.sdkapi.Token;
import com.tcl.account.sdkapi.UiAccountHelper;
import com.tcl.account.sdkapi.User;
import com.xiami.sdk.XiamiSDK;
import com.xiami.sdk.account.ILoginCallback;
import com.xiami.sdk.entities.XMLoginConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.adapter.ClosedTimeAdapter;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.fragments.QualityFragment;
import cn.tcl.music.model.ClosedTimeBean;
import cn.tcl.music.model.live.XiamiMemberInfo;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveGetMemberInfoTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.PreferenceUtil;
import cn.tcl.music.util.SDKUtil;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.util.live.UserInfoManager;
import mst.app.MstActivity;
import mst.app.dialog.AlertDialog;
import mst.widget.toolbar.Toolbar;

public class SettingsActivity extends MstActivity implements View.OnClickListener {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final String TCL_KEY_USERNAME = "userName";
    private static final String TCL_KEY_TOKEN = "token";
    private static final String TCL_ACCOUNT_CLIENT_ID_STRING = "clientId";
    private static final String TCL_ACCOUNT_CLIENT_ID = "99181295";
    private static final String TCL_ACCOUNT_WEB_API = "http://account.tclchn.com/account-web-api/receiveUserInfo";
    private static final String APPLICATION_CONTENT_TYPE = "application/json; charset=utf-8";

    private enum CountDownTime {
        COUNT_DOWN_TIME_0_MINUTES(0),
        COUNT_DOWN_TIME_10_MINUTES(10),
        COUNT_DOWN_TIME_15_MINUTES(15),
        COUNT_DOWN_TIME_30_MINUTES(30),
        COUNT_DOWN_TIME_45_MINUTES(45),
        COUNT_DOWN_TIME_60_MINUTES(60);
        private int mDownTime;

        CountDownTime(int time) {
            this.mDownTime = time;
        }

        public int getTime() {
            return mDownTime;
        }

        public static CountDownTime getCountDownTime(int index) {
            for (CountDownTime c : CountDownTime.values()) {
                if (c.ordinal() == index) {
                    return c;
                }
            }
            return null;
        }
    }

    private ListView mTimeListView;
    private List<ClosedTimeBean> mClosedTimeList;
    private ClosedTimeAdapter mClosedTimeAdapter;
    private RelativeLayout mTimeClosed;
    private RelativeLayout mIgnoredFolder;
    private RelativeLayout mQuanity;
    private FrameLayout mQuanityFrameLayout;
    private QualityFragment mQualityFragment;
    private TextView mCountDownTimeShow;
    private Switch mNetWorkSwitch;
    private RelativeLayout mXiamiCountLayout;
    private TextView mTclUserNameTextView;
    private TextView mXiamiNameTextView;
    private TextView mXiamiPromptTextView;
    private UiAccountHelper mUiAccountHelper = null;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mUiAccountHelper = new UiAccountHelper(SettingsActivity.this, new SessionStatusCallback() {

            @Override
            public void onSuccess(Token token) {
                loginTclAccountSuccess(token);
                ToastUtil.showToast(SettingsActivity.this, R.string.tcl_login_success);
            }

            @Override
            public void onError(int err) {
                loginTclAccountError(err);
                ToastUtil.showToast(SettingsActivity.this, R.string.tcl_login_failed);
            }

            @Override
            public void onOAuth(String code) {
                LogUtil.d(TAG, "code is " + code);
            }
        });
        mUiAccountHelper.onCreate(savedInstanceState);
        initToolBar();
        initView();
        refreshXiamiMemberInfo();
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        toolbar.setTitle(getResources().getString(R.string.settings));
        toolbar.setTitleTextAppearance(SettingsActivity.this, R.style.ToolbarTitle);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (View.GONE == mQuanityFrameLayout.getVisibility()) {
            super.onBackPressed();
        } else {
            mQuanityFrameLayout.setVisibility(View.GONE);
        }
    }

    private void initView() {
        mTimeClosed = (RelativeLayout) findViewById(R.id.suretime_to_close);
        mIgnoredFolder = (RelativeLayout) findViewById(R.id.ignored_folder);
        mCountDownTimeShow = (TextView) findViewById(R.id.timed_close_show);
        mNetWorkSwitch = (Switch) findViewById(R.id.network_switch);
        mQuanity = (RelativeLayout) findViewById(R.id.audition_and_download_quanity);
        mQuanityFrameLayout = (FrameLayout) findViewById(R.id.audition_and_download_quanity_layout);
        if (PreferenceUtil.getValue(this, PreferenceUtil.NODE_NETWORK_SWITCH, PreferenceUtil.KEY_NETWORK_SWITCH, CommonConstants.NO_OPEN) == CommonConstants.OPEN) {
            mNetWorkSwitch.setChecked(true);
        }
        RelativeLayout loginRelative = (RelativeLayout) findViewById(R.id.user_name_linearlayout);
        MusicApplication.getApp().setCountDownTimeShow(mCountDownTimeShow);
        mXiamiCountLayout = (RelativeLayout) findViewById(R.id.xiami_account);
        mTclUserNameTextView = (TextView) findViewById(R.id.tv_tcl_account);
        mXiamiNameTextView = (TextView) findViewById(R.id.tv_xiami_account);
        mXiamiPromptTextView = (TextView) findViewById(R.id.tv_xiami_prompt);

        mTimeClosed.setOnClickListener(this);
        mIgnoredFolder.setOnClickListener(this);
        loginRelative.setOnClickListener(this);
        mNetWorkSwitch.setOnClickListener(this);
        mQuanity.setOnClickListener(this);
        mXiamiCountLayout.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUiAccountHelper.onResume();
        if (PreferenceUtil.getValue(this, PreferenceUtil.NODE_NETWORK_SWITCH, PreferenceUtil.KEY_NETWORK_SWITCH, CommonConstants.NO_OPEN) == CommonConstants.OPEN) {
            mNetWorkSwitch.setChecked(true);
        }
        Token token = UiAccountHelper.getCurrentToken(getApplicationContext());
        if (null != token && !token.isInvalid()) {
            if (!TextUtils.isEmpty(token.getUser().phone)) {
                mTclUserNameTextView.setText(token.getUser().phone);
            } else if (!TextUtils.isEmpty(token.getUser().email)) {
                mTclUserNameTextView.setText(token.getUser().email);
            } else {
                mTclUserNameTextView.setText(token.getUser().accountName);
            }
            mXiamiCountLayout.setVisibility(View.VISIBLE);
            XiamiSDK xiamiSDK = new XiamiSDK();
            if (xiamiSDK.isLogin()) {
                UserInfoManager userInfoManager = UserInfoManager.getInstance(SettingsActivity.this);
                XiamiMemberInfo info = userInfoManager.getmMemberInfo();
                if (info.is_vip) {
                    mXiamiPromptTextView.setText(info.nick_name + "  " + SettingsActivity.this.getResources().getString(R.string.vip));
                } else {
                    mXiamiPromptTextView.setText(info.nick_name);
                }
            } else {

            }
        } else {
            mTclUserNameTextView.setText(R.string.sign_up);
            mXiamiCountLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUiAccountHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUiAccountHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mUiAccountHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUiAccountHelper.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
            case CommonConstants.COUNT_DOWN_TIME_DIALOG_ID:
                dialog = initCountTimeDialog();
                break;
            default:
                dialog = null;
                break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        for (int i = 0; i < mClosedTimeList.size(); i++) {
            ClosedTimeBean bean = mClosedTimeList.get(i);
            if (PreferenceUtil.getValue(this, PreferenceUtil.NODE_COUNT_TIME, PreferenceUtil.KEY_COUNT_TIME,
                    CommonConstants.NO_OPEN) == i) {
                bean.setSelect(true);
            } else {
                bean.setSelect(false);
            }
        }
        super.onPrepareDialog(id, dialog);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.suretime_to_close:
                showDialog(CommonConstants.COUNT_DOWN_TIME_DIALOG_ID);
                break;
            case R.id.ignored_folder: {
                Intent intent = new Intent(this, IgnoreActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.user_name_linearlayout: {
                AuthConfig authConfig = new AuthConfig(SessionAuthorizationType.AUTH);
                authConfig.setSessionAuthorizationType(SessionAuthorizationType.AUTH);
                loginTclAccount(authConfig);
            }
                break;
            case R.id.network_switch: {
                if (mNetWorkSwitch.isChecked()) {
                    if (MusicApplication.getApp().isDataSaver()) {
                        mNetWorkSwitch.setChecked(false);
                    } else {
                        PreferenceUtil.saveValue(this, PreferenceUtil.NODE_NETWORK_SWITCH, PreferenceUtil.KEY_NETWORK_SWITCH, CommonConstants.OPEN);
                    }
                } else {
                    PreferenceUtil.saveValue(this, PreferenceUtil.NODE_NETWORK_SWITCH, PreferenceUtil.KEY_NETWORK_SWITCH, CommonConstants.NO_OPEN);
                }
            }
                break;
            case R.id.xiami_account: {
                XiamiSDK xiamiSDK = new XiamiSDK();
                if (xiamiSDK.isLogin()) {
                    //goto xiami account mamange
                    LogUtil.d(TAG, "xiami account already login");
                } else {
                    final Token token = UiAccountHelper.getCurrentToken(getApplicationContext());
                    if (null != token && !token.isInvalid()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                XiamiSDK.login(token.getToken(), new ILoginCallback() {
                                    @Override
                                    public void onLoginSuccess(String s, long l) {
                                        //store xiami userid
//                                        Toast.makeText(SettingsActivity.this,R.string.xiami_bind_success,Toast.LENGTH_SHORT).show();
                                        mXiamiCountLayout.setVisibility(View.VISIBLE);
                                        PreferenceUtil.saveValue(SettingsActivity.this, PreferenceUtil.NODE_XIAMI_MEMBER_INFO, PreferenceUtil.KEY_USER_ID, l);
                                        //get xiami member info
                                        refreshXiamiMemberInfo();
                                    }

                                    @Override
                                    public void onError(int i) {
                                        LogUtil.d(TAG, "i is " + i);
                                    }

                                    @Override
                                    public void onBindAccount(String s, long l) {
                                        XMLoginConfig config = new XMLoginConfig();
                                        config.setLayoutId(R.layout.activity_xiami_login);
                                        config.setWebViewContainerId(R.id.webview);
                                        config.setTopBarLeftBtnId(R.id.layout_left_area);
                                        XiamiSDK.bind(SettingsActivity.this, token.getToken(), config);
                                    }
                                });
                            }
                        }).start();
                    }
                }
            }
                break;
            case R.id.audition_and_download_quanity: {
                mQuanityFrameLayout.setVisibility(View.VISIBLE);
                mQualityFragment = new QualityFragment();
                getFragmentManager().beginTransaction().replace(R.id.audition_and_download_quanity_layout, mQualityFragment, TAG).commit();
            }
                break;
            default:
                break;
        }
    }

    private AlertDialog initCountTimeDialog() {
        LayoutInflater inflater = LayoutInflater.from(SettingsActivity.this);
        final View dialogView = inflater.inflate(R.layout.dialog_time_closed, null);

        TextView okTextView = (TextView) dialogView.findViewById(R.id.dialog_time_close_ok_tv);
        TextView cancelTextView = (TextView) dialogView.findViewById(R.id.dialog_time_close_cancel_tv);

        mTimeListView = (ListView) dialogView.findViewById(R.id.dialog_time_closed_list_view);
        mClosedTimeList = new ArrayList<ClosedTimeBean>();
        initClosedTimeList();
        mClosedTimeAdapter = new ClosedTimeAdapter(this, mClosedTimeList);
        mTimeListView.setAdapter(mClosedTimeAdapter);
        mTimeListView.setOnItemClickListener(mCountTimeDialogItemClickListener);

        final AlertDialog countTimeDialog = new AlertDialog.Builder(SettingsActivity.this).setView(dialogView).create();

        okTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicApplication.getApp().cancelCountDownTime();
                for (int i = 0; i < mClosedTimeList.size(); i++) {
                    ClosedTimeBean bean = mClosedTimeList.get(i);
                    if (bean.isSelect()) {
                        startCountDownTime(i);
                    }
                }
                countTimeDialog.dismiss();
            }
        });
        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countTimeDialog.dismiss();
            }
        });
        return countTimeDialog;
    }

    private void initClosedTimeList() {
        String[] timeTextList = new String[]{
                getResources().getString(R.string.dialog_item_no_close), getResources().getString(R.string.dialog_item_after_ten),
                getResources().getString(R.string.dialog_item_after_fifteen), getResources().getString(R.string.dialog_item_after_thirty),
                getResources().getString(R.string.dialog_item_after_forty_five), getResources().getString(R.string.dialog_item_after_sixty)};
        if (MusicApplication.getApp().getCountDownTimer() == null) {
            PreferenceUtil.saveValue(this, PreferenceUtil.NODE_COUNT_TIME, PreferenceUtil.KEY_COUNT_TIME, CommonConstants.NO_OPEN);
        }
        for (int i = 0; i < timeTextList.length; i++) {
            ClosedTimeBean bean = new ClosedTimeBean();
            bean.setTime(timeTextList[i]);
            bean.setSelect(PreferenceUtil.getValue(this, PreferenceUtil.NODE_COUNT_TIME, PreferenceUtil.KEY_COUNT_TIME,
                    CommonConstants.NO_OPEN) == i);
            mClosedTimeList.add(bean);
        }
    }

    private void startCountDownTime(int index) {
        PreferenceUtil.saveValue(this, PreferenceUtil.NODE_COUNT_TIME, PreferenceUtil.KEY_COUNT_TIME, index);
        if (index != CommonConstants.NO_OPEN) {
            mCountDownTimeShow.setVisibility(View.VISIBLE);
        } else {
            mCountDownTimeShow.setVisibility(View.GONE);
            return;
        }
        long milliSecond = CountDownTime.getCountDownTime(index).getTime() * CommonConstants.MINUTE_TO_MILLISECOND;
        MusicApplication.getApp().startCountDownTime(milliSecond);
    }

    private AdapterView.OnItemClickListener mCountTimeDialogItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            for (int i = 0; i < mClosedTimeList.size(); i++) {
                ClosedTimeBean bean = mClosedTimeList.get(i);
                if (i == position) {
                    bean.setSelect(true);
                } else {
                    bean.setSelect(false);
                }
            }
            mClosedTimeAdapter.notifyDataSetChanged();
        }
    };

    /**
     * 登录TCL账号
     *
     * @param authConfig
     */
    public void loginTclAccount(AuthConfig authConfig) {
        Token token = UiAccountHelper.getCurrentToken(getApplicationContext());
        if (null != token && !token.isInvalid()) {
            ToastUtil.showToast(SettingsActivity.this, R.string.log_in);
            return;
        }
        try {
            mUiAccountHelper.requestSSOAuth(authConfig, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * login tcl account success
     *
     * @param token
     */
    public void loginTclAccountSuccess(Token token) {
        if (null != token) {
            User user = token.getUser();
            if (null != user) {
                //展示用户信息,提交信息到接口服务器
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(TCL_KEY_USERNAME, user.accountName);
                jsonObject.put(TCL_KEY_TOKEN, token.getToken());
                jsonObject.put(TCL_ACCOUNT_CLIENT_ID_STRING, TCL_ACCOUNT_CLIENT_ID);

                OkHttpClient mOkHttpClient = new OkHttpClient();
                RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_CONTENT_TYPE), SDKUtil.encrypt(jsonObject.toJSONString()));
                final Request request = new Request.Builder().url(TCL_ACCOUNT_WEB_API).post(body).build();
                Call call = mOkHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        LogUtil.d(TAG, "onFailure " + request.toString());

                    }

                    @Override
                    public void onResponse(final Response response) throws IOException {
                        LogUtil.e(TAG, "response ----->" + SDKUtil.decrypt(response.body().string()));
                    }
                });
            }
        }
    }

    public void loginTclAccountError(int err) {
        LogUtil.d(TAG, "err is " + err);
        ToastUtil.showToast(this, R.string.tcl_login_failed);
    }

    private void refreshXiamiMemberInfo() {
        XiamiSDK xiamiSDK = new XiamiSDK();
        if (xiamiSDK.isLogin()) {
            long userId = PreferenceUtil.getValue(this, PreferenceUtil.NODE_XIAMI_MEMBER_INFO, PreferenceUtil.KEY_USER_ID, 0l);
            LogUtil.d(TAG, "userId is " + userId);
            LiveGetMemberInfoTask getMemberInfoTask = new LiveGetMemberInfoTask(this, new ILoadData() {
                @Override
                public void onLoadSuccess(int dataType, List datas) {
                    if (null != datas && !datas.isEmpty()) {
                        XiamiMemberInfo info = (XiamiMemberInfo) datas.get(0);
                        LogUtil.d(TAG, "info userid is " + info.user_id + " and nickname is " + info.nick_name);
                        UserInfoManager userInfoManager = UserInfoManager.getInstance(SettingsActivity.this);
                        userInfoManager.setmMemberInfo(info);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (info.is_vip) {
                                    mXiamiPromptTextView.setText(info.nick_name + "  " + SettingsActivity.this.getResources().getString(R.string.vip));
                                } else {
                                    mXiamiPromptTextView.setText(info.nick_name);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onLoadFail(int dataType, String message) {

                }
            }, userId);
            getMemberInfoTask.executeMultiTask();
        }
    }

}
