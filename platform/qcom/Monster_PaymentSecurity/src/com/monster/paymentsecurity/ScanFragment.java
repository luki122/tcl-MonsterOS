package com.monster.paymentsecurity;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.monster.paymentsecurity.diagnostic.DiagnosticReport;
import com.monster.paymentsecurity.diagnostic.Diagnostor;
import com.monster.paymentsecurity.diagnostic.RiskOrError;
import com.monster.paymentsecurity.scan.Result;
import com.monster.paymentsecurity.scan.ScanningEngine;
import com.monster.paymentsecurity.scan.ScanningHelper;

import mst.app.dialog.AlertDialog;

import static com.monster.paymentsecurity.ScanResultFragment.REPORT_DATA;

/**
 * 扫描页
 *
 * Created by logic on 16-12-21.
 */
public class ScanFragment extends Fragment  implements ScanningEngine.ScanningResultObserver, View.OnClickListener{

    public static final String TAG = ScanFragment.class.getSimpleName();
    public static final String START_SCAN = "start_scan";

    private Button scanBtn;
    private TextView showProgressTxt;

    private ImageView scanLogo;

    private ImageView wifiScanPoint;
    private ImageView paymentScanPoint;
    private ImageView systemBugScanPoint;
    private ImageView appScanPoint;

    private TextView  wifiRiskCountTv;
    private TextView  paymentRiskCountTv;
    private TextView  systemBugRiskCountTv;
    private TextView  appRiskCountTv;
    AlertDialog dialog;

    private boolean scanning = false;

    private Diagnostor mDiagnostor;
    private ScanningEngine mScanningEngine;

    private View root;
    private FragmentChangeHandler fragHandler;

    public static ScanFragment create(){
        return new ScanFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof  FragmentChangeHandler) {
            fragHandler = (FragmentChangeHandler) getActivity();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragHandler = null;
        mScanningEngine.stopScanning();
        mScanningEngine = null;
        mDiagnostor = null;
        Runtime.getRuntime().gc();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null){
            root = inflater.inflate(R.layout.fragment_scan, container, false);
            initView(root);
        }else {
            ViewGroup parent = (ViewGroup) root.getParent();
            parent.removeView(root);
        }
        return root;
    }

    private void initView(View root) {
        scanLogo = (ImageView) root.findViewById(R.id.scan_type_logo);
        scanBtn = (Button) root.findViewById(R.id.scanning_btn);
        showProgressTxt = (TextView) root.findViewById(R.id.scan_progress_tv);

        wifiScanPoint = (ImageView) root.findViewById(R.id.wifi_scan_result_pointer);
        paymentScanPoint = (ImageView) root.findViewById(R.id.sms_scan_result_pointer);
        systemBugScanPoint = (ImageView) root.findViewById(R.id.system_scan_result_pointer);
        appScanPoint = (ImageView) root.findViewById(R.id.app_scan_result_pointer);

        wifiRiskCountTv = (TextView) root.findViewById(R.id.wifi_risk_count);
        paymentRiskCountTv = (TextView) root.findViewById(R.id.sms_risk_count);
        systemBugRiskCountTv = (TextView) root.findViewById(R.id.system_bug_risk_count);
        appRiskCountTv = (TextView) root.findViewById(R.id.app_risk_count);

        mDiagnostor = new Diagnostor(getContext());
        mScanningEngine = new ScanningEngine(getContext());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        scanBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.scanning_btn:
                if (!scanning) {
                    doScanningAction(true);
                }else {
                    //弹框提示
                    showStopDialog();
                }
                break;
        }
    }

    @Override
    public void notifyScanningResult(Result result, float progress) {
        mDiagnostor.diagnose(result);
        switch (ScanningHelper.convertScanTypeToCategory(result.getScanType())){
            case RiskOrError.RISK_CATEGORY_WIFI:
                updateWifiScanUI();
                break;
            case RiskOrError.RISK_CATEGORY_APP_SECURITY:
                updateAppScanUI();
                break;
            case RiskOrError.RISK_CATEGORY_SYSTEM_BUG:
                updateSystemScanUI();
                break;
            case RiskOrError.RISK_CATEGORY_SMS_SECURITY:
                updateSMSScanUI();
                break;
        }
        updateScanProgress((int)(progress * 100), mDiagnostor.getTotalRiskCount());
    }

    private void updateWifiScanUI() {
        scanLogo.setImageResource(R.drawable.mps_wifi_security_large);
        if (mDiagnostor.getWifiRiskCount() > 0){
            wifiScanPoint.setVisibility(View.VISIBLE);
            wifiScanPoint.setImageResource(R.drawable.mps_risk_bg);
            wifiRiskCountTv.setVisibility(View.VISIBLE);
            wifiRiskCountTv.setText(getString(R.string.count,mDiagnostor.getWifiRiskCount()));
        }
    }

    private void updateSMSScanUI() {
        wifiScanPoint.setVisibility(View.VISIBLE);
        if (mDiagnostor.getWifiRiskCount() == 0) {
            wifiScanPoint.setImageResource(R.drawable.mps_finished);
        }
        scanLogo.setImageResource(R.drawable.mps_sms_security_large);
        if (mDiagnostor.getMMSRiskCount() > 0){
            paymentScanPoint.setVisibility(View.VISIBLE);
            paymentScanPoint.setImageResource(R.drawable.mps_risk_bg);
            paymentRiskCountTv.setVisibility(View.VISIBLE);
            paymentRiskCountTv.setText(getString(R.string.count,mDiagnostor.getMMSRiskCount()));
        }
    }

    private void updateSystemScanUI() {
        paymentScanPoint.setVisibility(View.VISIBLE);
        if (mDiagnostor.getMMSRiskCount() == 0) {
            paymentScanPoint.setImageResource(R.drawable.mps_finished);
        }
        scanLogo.setImageResource(R.drawable.mps_system_bug_large);
        if (mDiagnostor.getSystemBugRiskCount() > 0){
            systemBugScanPoint.setVisibility(View.VISIBLE);
            systemBugScanPoint.setImageResource(R.drawable.mps_risk_bg);
            systemBugRiskCountTv.setVisibility(View.VISIBLE);
            systemBugRiskCountTv.setText(getString(R.string.count,mDiagnostor.getSystemBugRiskCount()));
        }
    }

    private void updateAppScanUI() {
        systemBugScanPoint.setVisibility(View.VISIBLE);
        if (mDiagnostor.getSystemBugRiskCount() == 0) {
            systemBugScanPoint.setImageResource(R.drawable.mps_finished);
        }
        scanLogo.setImageResource(R.drawable.mps_app_security_large);
        if (mDiagnostor.getAppRiskCount() > 0){
            appScanPoint.setVisibility(View.VISIBLE);
            appScanPoint.setImageResource(R.drawable.mps_risk_bg);
            appRiskCountTv.setVisibility(View.VISIBLE);
            appRiskCountTv.setText(getString(R.string.count,mDiagnostor.getAppRiskCount()));
        }
    }

    private void updateScanProgress(int progress, int totalRisk) {
        showProgressTxt.setVisibility(View.VISIBLE);
        showProgressTxt.setText(Html.fromHtml(getString(R.string.scan_progress_and_risk_count, progress, totalRisk),
                Html.FROM_HTML_MODE_COMPACT));
    }

    public void resetUI() {
        scanBtn.setText(scanning ? R.string.stop_scanning: R.string.start_scanning);
        showProgressTxt.setText(null);
        showProgressTxt.setVisibility(scanning?View.VISIBLE : View.INVISIBLE);
        wifiScanPoint.setVisibility(View.INVISIBLE);
        wifiRiskCountTv.setVisibility(View.INVISIBLE);
        paymentScanPoint.setVisibility(View.INVISIBLE);
        paymentRiskCountTv.setVisibility(View.INVISIBLE);
        systemBugScanPoint.setVisibility(View.INVISIBLE);
        systemBugRiskCountTv.setVisibility(View.INVISIBLE);
        appScanPoint.setVisibility(View.INVISIBLE);
        appRiskCountTv.setVisibility(View.INVISIBLE);
        scanLogo.setImageResource(R.drawable.mps_wifi_security_large);
    }

    @Override
    public void notifyScanningState(@ScanningEngine.ScanningState int state) {
        if (state == ScanningEngine.FINISHED){
            scanning = false;
            scanBtn.setText(R.string.start_scanning);
            appScanPoint.setVisibility(View.VISIBLE);
            if(mDiagnostor.getAppRiskCount() == 0) {
                appScanPoint.setImageResource(R.drawable.mps_finished);
            }
            showScanResultFragment(false);
        }else if (state == ScanningEngine.CANCEL){
            scanning = false;
        }else if (state == ScanningEngine.SCANNING){
            scanning = true;
        }
    }

    private void showScanResultFragment(boolean isCanceled) {
        if (isDetached() || mDiagnostor == null) return;
        Bundle args = new Bundle();
        DiagnosticReport report = mDiagnostor.getReport();
        report.setScanCanceled(isCanceled);
        args.putParcelable(REPORT_DATA, report);
        fragHandler.notifyFragmentChange(FragmentChangeHandler.ACTION_ADD, ScanResultFragment.TAG, args);

        mDiagnostor.reset();
    }

    private void showStopDialog() {
        if (dialog == null) {
            dialog = new AlertDialog.Builder(getActivity())
                    .setPositiveButton(com.mst.R.string.ok, (dialog1, which) -> {
                        //正在扫描则停止
                        if (scanning) {
                            doScanningAction(false);
                        }
                    })
                    .setNegativeButton(com.mst.R.string.cancel, (dialog12, which) -> dialog12.dismiss())
                    .setMessage(R.string.confirm_stop_payment_env_scan)
                    .setTitle(R.string.stop_scanning).create();
        }
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null){
            dialog.dismiss();
            dialog = null;
            scanLogo.setImageDrawable(null);
        }
    }

    public void doScanningAction(boolean scanning) {
        this.scanning = scanning;
        resetUI();
        if (scanning){
            mDiagnostor.reset();
            mScanningEngine.startScanning(this);
        }else {
            mScanningEngine.stopScanning(this);
            //立即跳转到结果页
            showScanResultFragment(true);
        }
    }
}
