/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.setupwizard.ui;

/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface; // MODIFIED by xinlei.sheng, 2016-11-17,BUG-3356295
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color; // MODIFIED by xinlei.sheng, 2016-11-04,BUG-2669930
import android.os.Bundle;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-30,BUG-2669930*/
import android.view.MenuItem;
/* MODIFIED-END by xinlei.sheng,BUG-2669930*/
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
/* MODIFIED-END by xinlei.sheng,BUG-2669930*/
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast; // MODIFIED by xinlei.sheng, 2016-09-30,BUG-2669930

/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-21,BUG-2669930*/
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
/* MODIFIED-END by xinlei.sheng,BUG-2669930*/

import cn.tcl.setupwizard.R;
import cn.tcl.setupwizard.utils.LogUtils;
import cn.tcl.setupwizard.utils.SimUtils;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-30,BUG-2669930*/
import cn.tcl.setupwizard.utils.SystemBarHelper;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-11-18,BUG-3356295*/
import cn.tcl.setupwizard.widget.SetupBottomWidePopupMenu;
import mst.view.menu.BottomWidePopupMenu;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/

public class SimSetActivity extends BaseActivity implements View.OnClickListener{

    public final static String TAG = "SimSetActivity";
    private SetupBottomWidePopupMenu mBottomMenu;
    /* MODIFIED-END by xinlei.sheng,BUG-3356295*/
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/

    private ImageView mSim1Icon, mSim2Icon;
    private TextView mSim1Name, mSim2Name;
    private Switch mDataSwitch;
    private Button mBtnContinue;
    private Button mBtnSkip;
    private TextView mDefaultSim;
    private TextView mSimPrompt;
    private View mDataSetLayout;
    private View mDefaultSimLayout;
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/

    private String mSim1OperatorName;
    private String mSim2OperatorName;
    private int mSim1NetworkType, mSim2NetworkType;
    private int mDefaultSimId;

    private boolean mSupportMultiSim;
    private boolean mSim1Enabled;
    private boolean mSim2Enabled;

    /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
    private BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {
                LogUtils.i(TAG, "sim state has changed");
                initSimData();
                setSimView();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim_set);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        registerReceiver(mSimStateReceiver, filter);
        initView();
        initSimData();
        setSimView();

        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-21,BUG-2669930*/
        ImageView imageView = (ImageView) findViewById(R.id.background_sim);
        Glide.with(this).load(R.drawable.gif_sim)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new GlideDrawableImageViewTarget(imageView, 1));
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSimStateReceiver);
    }

    @Override
    public void onSetupFinished() {
        if (!this.isDestroyed()) {
            this.finish();
        }
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    private void initSimData() {
        mSupportMultiSim = SimUtils.isMultiSimEnabled(this);
        if (mSupportMultiSim) {
            mSim1Enabled = SimUtils.isSimCardEnabledBySlotId(this, 0);
            mSim2Enabled = SimUtils.isSimCardEnabledBySlotId(this, 1);

            mSim1NetworkType = SimUtils.getNetworkTypeBySlotId(this, 0);
            mSim2NetworkType = SimUtils.getNetworkTypeBySlotId(this, 1);

            mSim1OperatorName = SimUtils.getSimOperatorNameBySlotId(this, 0);
            mSim2OperatorName = SimUtils.getSimOperatorNameBySlotId(this, 1);
            LogUtils.i(TAG, "mSim1Enabled: " + mSim1Enabled +
                    ", mSim1NetworkType: " + mSim1NetworkType +
                    ", mSim1OperatorName: " + mSim1OperatorName +
                    ", sim1DataEnabled: " + SimUtils.getDataEnabledBySlotId(this, 0));
            LogUtils.i(TAG, "mSim2Enabled: " + mSim2Enabled +
                    ", mSim2NetworkType: " + mSim2NetworkType +
                    ", mSim2OperatorName: " + mSim2OperatorName +
                    ", sim2DataEnabled: " + SimUtils.getDataEnabledBySlotId(this, 1));
            /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
            LogUtils.i(TAG, "defaultSimNetworkType: " + SimUtils.getNetworkType(this) +
                    ", defaultSimOperatorName: " + SimUtils.getSimOperatorName(this));
                    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
        } else {
            mSim1Enabled = SimUtils.isSimCardEnabled(this);
            mSim1NetworkType = SimUtils.getNetworkType(this);
            mSim1OperatorName = SimUtils.getSimOperatorName(this);
            mSim2Enabled = false;
            mSim2NetworkType = 0;
            mSim2OperatorName = null;
            LogUtils.i(TAG, "mDefaultSimEnabled: " + mSim1Enabled +
                ", mDefaultSimNetworkType: " + mSim1NetworkType +
                ", mDefaultSimOperatorName: " + mSim1OperatorName);
        }
    }

    private void initView() {
        mSim1Icon = (ImageView) findViewById(R.id.sim1_icon);
        mSim2Icon = (ImageView) findViewById(R.id.sim2_icon);
        mSim1Name = (TextView) findViewById(R.id.sim1_name);
        mSim2Name = (TextView) findViewById(R.id.sim2_name);

        mDataSwitch = (Switch) findViewById(R.id.sim_data_switch);
        mBtnContinue = (Button) findViewById(R.id.sim_btn_continue);
        mBtnSkip = (Button) findViewById(R.id.sim_btn_skip);
        mDefaultSim = (TextView) findViewById(R.id.sim_card_default);
        mSimPrompt = (TextView) findViewById(R.id.sim_prompt);
        mDataSetLayout = findViewById(R.id.sim_data_content);
        mDefaultSimLayout = findViewById(R.id.sim_data_default);

        mDataSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDataSwitch.isChecked()) {
                    if (mSim1Enabled && mSim2Enabled) {
                        mDefaultSimLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    mDefaultSimLayout.setVisibility(View.GONE);
                }
            }
        });
        mBtnSkip.setOnClickListener(this);
        mBtnContinue.setOnClickListener(this);
        findViewById(R.id.header_back).setOnClickListener(this);
/* MODIFIED-BEGIN by xinlei.sheng, 2016-11-17,BUG-3356295*/
//        findViewById(R.id.sim_card_select).setOnClickListener(this);
        mDefaultSimLayout.setOnClickListener(this);
        /* MODIFIED-END by xinlei.sheng,BUG-3356295*/
        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-30,BUG-2669930*/
        mBottomMenu = new SetupBottomWidePopupMenu(this);
        mBottomMenu.inflateMenu(R.menu.sim_select_menu);
        /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-17,BUG-3356295*/
        mBottomMenu.setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        /* MODIFIED-END by xinlei.sheng,BUG-3356295*/
        mBottomMenu.setOnMenuItemClickedListener(new BottomWidePopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onItemClicked(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.select_sim1) {
                    mDefaultSim.setText(getString(R.string.sim_card) + "1(" + mSim1OperatorName + ")");
                    mDefaultSimId = 0;
                } else if (menuItem.getItemId() == R.id.select_sim2){
                    mDefaultSim.setText(getString(R.string.sim_card) + "2(" + mSim2OperatorName + ")");
                    mDefaultSimId = 1;
                }

                return false;
            }
        });
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    private void setSimView() {
        if (!mSupportMultiSim) {
            findViewById(R.id.sim2_content).setVisibility(View.GONE);
            mDefaultSimLayout.setVisibility(View.GONE);

            if (mSim1Enabled) {
                mSim1Icon.setBackground(getDrawable(R.drawable.ic_sim_enabled));
                mSim1Name.setText(mSim1OperatorName);
                mSim1Name.setTextColor(Color.parseColor("#DB000000")); // MODIFIED by xinlei.sheng, 2016-11-04,BUG-2669930

                mBtnContinue.setVisibility(View.VISIBLE);
                mBtnSkip.setVisibility(View.GONE);
                mDataSetLayout.setVisibility(View.VISIBLE);
                mDataSwitch.setChecked(true);
                mSimPrompt.setVisibility(View.GONE);

            } else {
                mSim1Icon.setBackground(getDrawable(R.drawable.ic_sim_disabled));
                mSim1Name.setText(getString(R.string.sim_card_disabled, ""));
                mSim1Name.setTextColor(Color.parseColor("#59000000")); // MODIFIED by xinlei.sheng, 2016-11-04,BUG-2669930

                mBtnContinue.setVisibility(View.GONE);
                mBtnSkip.setVisibility(View.VISIBLE);
                mDataSetLayout.setVisibility(View.GONE);
                mSimPrompt.setVisibility(View.VISIBLE);
            }

        } else {
            findViewById(R.id.sim1_content).setVisibility(View.VISIBLE);
            findViewById(R.id.sim2_content).setVisibility(View.VISIBLE);

            if (!mSim1Enabled && !mSim2Enabled) {
                mSim1Icon.setBackground(getDrawable(R.drawable.ic_sim_disabled));
                mSim1Name.setText(getString(R.string.sim_card_disabled, "1"));
                /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-2669930*/
                mSim1Name.setTextColor(Color.parseColor("#59000000"));
                mSim2Icon.setBackground(getDrawable(R.drawable.ic_sim_disabled));
                mSim2Name.setText(getString(R.string.sim_card_disabled, "2"));
                mSim2Name.setTextColor(Color.parseColor("#59000000"));
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/

                mBtnContinue.setVisibility(View.GONE);
                mBtnSkip.setVisibility(View.VISIBLE);
                mDataSetLayout.setVisibility(View.GONE);
                mSimPrompt.setVisibility(View.VISIBLE);

            } else {
                mBtnContinue.setVisibility(View.VISIBLE);
                mBtnSkip.setVisibility(View.GONE);
                mDataSetLayout.setVisibility(View.VISIBLE);
                mSimPrompt.setVisibility(View.GONE);
                mDataSwitch.setChecked(true);

                // set default SIM
                if (mSim1Enabled && mSim2Enabled) {
                    mDefaultSimLayout.setVisibility(View.VISIBLE);
                    mDefaultSimId = SimUtils.getDataDefaultSim(mSim1NetworkType, mSim2NetworkType);
                    //mDefaultSimId = SimUtils.getDefaultSimId();
                    String defaultSimStr = getString(R.string.sim_card) + Integer.toString(mDefaultSimId + 1);
                    /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-30,BUG-2669930*/
                    if (mDefaultSimId == 0) {
                        /* MODIFIED-BEGIN by xinlei.sheng, 2016-10-19,BUG-2669930*/
                        defaultSimStr += " (" + mSim1OperatorName + ")";
                    } else {
                        defaultSimStr += " (" + mSim2OperatorName + ")";
                        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                    }
                    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                    mDefaultSim.setText(defaultSimStr);
                    LogUtils.i(TAG, "mDefaultSimId: " + mDefaultSimId);
                } else {
                    mDefaultSimLayout.setVisibility(View.GONE);
                }

                // set state of SIM
                if (mSim1Enabled) {
                    mSim1Icon.setBackground(getDrawable(R.drawable.ic_sim_enabled));
                    mSim1Name.setText(mSim1OperatorName);
                    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-2669930*/
                    mSim1Name.setTextColor(Color.parseColor("#DB000000"));
                } else {
                    mSim1Icon.setBackground(getDrawable(R.drawable.ic_sim_disabled));
                    mSim1Name.setText(getString(R.string.sim_card_disabled, "1"));
                    mSim1Name.setTextColor(Color.parseColor("#59000000"));
                }
                if (mSim2Enabled) {
                    mSim2Icon.setBackground(getDrawable(R.drawable.ic_sim_enabled));
                    mSim2Name.setText(mSim2OperatorName);
                    mSim2Name.setTextColor(Color.parseColor("#DB000000"));
                } else {
                    mSim2Icon.setBackground(getDrawable(R.drawable.ic_sim_disabled));
                    mSim2Name.setText(getString(R.string.sim_card_disabled, "2"));
                    mSim2Name.setTextColor(Color.parseColor("#59000000"));
                    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.header_back:
                finish();
                /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-30,BUG-2669930*/
                Intent languageIntent = new Intent(this, WifiSetActivity.class);
                languageIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(languageIntent);
                break;
            case R.id.sim_btn_skip:
                startActivity(new Intent(this, OtherServiceActivity.class));
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                break;
            case R.id.sim_btn_continue:
                if (mDataSwitch.isChecked()) {
                    if (mSim1Enabled && mSim2Enabled) {
                        SimUtils.setDataEnabledBySlotId(this, mDefaultSimId, true);
                    } else if (mSim1Enabled || mSim2Enabled) {
                        SimUtils.setDataEnabled(this, true);
                    }
                } else {
                    if (mSim1Enabled && mSim2Enabled) {
                        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
                        SimUtils.setDataEnabledBySlotId(this, 0, false);
                        SimUtils.setDataEnabledBySlotId(this, 1, false);
                        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                    } else if (mSim1Enabled || mSim2Enabled) {
                        SimUtils.setDataEnabled(this, false);
                    }
                }
                /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-30,BUG-2669930*/
                startActivity(new Intent(this, OtherServiceActivity.class));
                break;
            case R.id.sim_data_default: // MODIFIED by xinlei.sheng, 2016-11-17,BUG-3356295
                /* MODIFIED-BEGIN by xinlei.sheng, 2016-10-19,BUG-2669930*/
                String menuItem1 = getString(R.string.sim_card) + "1 (" + mSim1OperatorName + ")";
                String menuItem2 = getString(R.string.sim_card) + "2 (" + mSim2OperatorName + ")";
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                mBottomMenu.getMenu().getItem(0).setTitle(menuItem1);
                mBottomMenu.getMenu().getItem(1).setTitle(menuItem2);
                SystemBarHelper.hideSystemBars(mBottomMenu); // MODIFIED by xinlei.sheng, 2016-10-27,BUG-2669930
                mBottomMenu.show();
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                break;

            default:
        }
    }
}
