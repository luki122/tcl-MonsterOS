package com.monster.paymentsecurity.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.monster.paymentsecurity.FragmentChangeHandler;
import com.monster.paymentsecurity.R;
import com.monster.paymentsecurity.ScanResultFragment;
import com.monster.paymentsecurity.bean.WhiteListInfo;
import com.monster.paymentsecurity.util.IconCache;
import com.monster.paymentsecurity.db.WhiteListDao;
import com.monster.paymentsecurity.diagnostic.AppRisk;
import com.monster.paymentsecurity.diagnostic.DiagnosticReport;
import com.monster.paymentsecurity.diagnostic.RiskOrError;
import com.monster.paymentsecurity.diagnostic.SuggestFactory;
import com.monster.paymentsecurity.util.Utils;
import com.monster.paymentsecurity.views.PayListCard;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import mst.widget.SliderLayout;
import mst.widget.SliderView;
import mst.widget.recycleview.RecyclerView;
import tmsdk.common.module.qscanner.QScanResultEntity;

import static tmsdk.common.module.qscanner.QScanConstants.APK_TYPE_UNINSTALLED;

/**
 * 扫描结果adapter
 * Created by logic on 16-12-5.
 */
public class ScanResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final LayoutInflater inflater;
    private final DiagnosticReport mReport;
    private final IconCache mIconCache;

    private final List<AppRisk> appRisks;
    private final List<RiskOrError> notAppRisks;
    private final boolean handle;
    private boolean hasAddWifiRisk = false;

    private static final int VIEWTYPE_SCAN_RESULT_STATE = 1;
    private static final int VIEWTYPE_RISK_APP = 2;
//    private static final int VIEWTYPE_RISK_APK_FILE = 3;
    private static final int VIEWTYPE_RISK_ITEM = 4;
    private static final int VIEWTYPE_SECURITY_ITEM = 5;
    private static final int VIEWTYPE_PAYLIST_ITEM = 6;
    private static final int VIEWTYPE_FINISHED_ITEM = 7;


    public ScanResultsAdapter(Context context, DiagnosticReport report, IconCache iconCache){
        this.mContext = context;
        this.mIconCache = iconCache;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mReport = report;
        appRisks = new ArrayList<>();
        notAppRisks = new ArrayList<>();
        this.handle = report.hasRisk();
        updateData();
    }

    private void updateData() {
        appRisks.clear();
        notAppRisks.clear();
        for (int i = 0; i < mReport.getCommonRisks().size(); i ++){
            RiskOrError risk = mReport.getCommonRisks().get(i);
            if (risk.isRisk()){//剔除掉异常
                if (risk.getCategory() == RiskOrError.RISK_CATEGORY_WIFI){//剔除掉wifirisk, 只保留一个
                    if (!hasAddWifiRisk){
                        notAppRisks.add(risk);
                        hasAddWifiRisk = true;
                    }
                }else {
                    notAppRisks.add(risk);
                }
            }
        }

        for (int i = 0; i < mReport.getAppRisks().size() ; i ++){
            AppRisk risk = mReport.getAppRisks().get(i);
            if (risk.isRisk()) {//剔除掉异常
                appRisks.add(risk);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == VIEWTYPE_SCAN_RESULT_STATE){
            return new ScanResultCard(inflater.inflate(R.layout.card_scan_result_state_item, viewGroup, false));
        }else if (viewType == VIEWTYPE_RISK_APP){
            return new AppRiskCard(inflater.inflate(R.layout.card_risk_app, viewGroup, false), this);
        }else if (viewType == VIEWTYPE_RISK_ITEM){
            return new RiskCard(inflater.inflate(R.layout.card_risk_item,viewGroup, false), this);
        }else if (viewType == VIEWTYPE_SECURITY_ITEM){
            return new SecurityCard(inflater.inflate(R.layout.card_security_protected_item, viewGroup, false));
        }else if (viewType == VIEWTYPE_PAYLIST_ITEM){
            return new PaymentAppListCard(inflater.inflate(R.layout.card_paylist,viewGroup,false));
        }else if (viewType == VIEWTYPE_FINISHED_ITEM){
            return new FinishBtnCard(inflater.inflate(R.layout.card_finished_btn,viewGroup,false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ScanResultCard)
        {
            ScanResultCard holder = (ScanResultCard) viewHolder;
            holder.bindData(mReport, handle);
        }
        else if (viewHolder instanceof AppRiskCard)
        {
            AppRiskCard holder = (AppRiskCard) viewHolder;
            holder.bindData(appRisks.get(position - 1), 0 == (position-1) );
        }
        else if (viewHolder instanceof RiskCard)
        {
            RiskCard holder = (RiskCard) viewHolder;
            holder.bindData(notAppRisks.get(position - mReport.getAppRiskCount() - 1));
        }
        else if (viewHolder instanceof SecurityCard)
        {
            SecurityCard holder = (SecurityCard) viewHolder;
            holder.bindData(mReport);
        }
        else if (viewHolder instanceof PaymentAppListCard)
        {
            PaymentAppListCard holder = (PaymentAppListCard) viewHolder;
            holder.bindData(false);
        }
        else if (viewHolder instanceof FinishBtnCard)
        {
            FinishBtnCard holder = (FinishBtnCard) viewHolder;
            holder.bindData(mReport);
        }
    }

    @Override
    public int getItemCount() {
        if (mReport.getTotalRiskCount() == 0){
            return Utils.showPayListCard(mContext)? 4 : 3;
        }else {
            return  2 +(Utils.showPayListCard(mContext)? 1 : 0)
                    + (mReport.needShowProtected()? 1: 0)
                    + mReport.getSystemBugCount()
                    + (mReport.getWifiRiskCount() >0 ? 1: 0)//注意，只用一个WIFI RISK
                    + mReport.getMmsRiskCount()
                    + mReport.getAppRiskCount();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return VIEWTYPE_SCAN_RESULT_STATE;
        }else if (mReport.getTotalRiskCount() == 0){
            if (position == 1){
                return VIEWTYPE_SECURITY_ITEM;
            }else if (position == 2){
                if (Utils.showPayListCard(mContext)){
                    return VIEWTYPE_PAYLIST_ITEM;
                }else {
                    return VIEWTYPE_FINISHED_ITEM;
                }
            }else {
                return VIEWTYPE_FINISHED_ITEM;
            }
        }else {
            if (mReport.getAppRiskCount() > 0 && position <= mReport.getAppRiskCount()){
                return VIEWTYPE_RISK_APP;
            }else if (position <= (mReport.getAppRiskCount() + notAppRisks.size())){
                return VIEWTYPE_RISK_ITEM;
            }else if (mReport.needShowProtected() && (position == getItemCount() - 3)){
                return VIEWTYPE_SECURITY_ITEM;
            } else if ((position == getItemCount() - 2)){
                if (Utils.showPayListCard(mContext)) {
                    return VIEWTYPE_PAYLIST_ITEM;
                }else {
                    return VIEWTYPE_SECURITY_ITEM;
                }
            }else {
                return VIEWTYPE_FINISHED_ITEM;
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void handleRisk(RiskOrError risk) {
        new SuggestFactory(mContext).create(risk).run();
        if(mReport.removeRisk(risk)) {
            updateData();
            notifyDataSetChanged();
        }
    }

    private void ignoreRisk(AppRisk risk) {
        WhiteListDao dao = new WhiteListDao(mContext);
        WhiteListInfo whiteApp = new WhiteListInfo();
        QScanResultEntity entity = risk.getEntity();
        whiteApp.setName(entity.softName);
        whiteApp.setPackageName(entity.packageName);
        whiteApp.setApkPath(entity.path);
        whiteApp.setApkType(entity.apkType);
        dao.insert(whiteApp);
        if (mReport.removeRisk(risk)) {
            updateData();
            notifyDataSetChanged();
        }
    }

    private DiagnosticReport getReport(){
        return mReport;
    }

    private IconCache getIconCache(){
        return mIconCache;
    }

    private static class ScanResultCard extends RecyclerView.ViewHolder {

        final ImageView state_icon;
        final TextView  result_desc;
        final Context mContext;

        ScanResultCard(View view){
            super(view);
            mContext = view.getContext().getApplicationContext();
            state_icon = (ImageView) view.findViewById(R.id.scan_result_state_logo);
            result_desc = (TextView) view.findViewById(R.id.scan_result_desc);
        }
        void bindData(DiagnosticReport report, boolean handle){
            if (report.isScanCanceled()){
                state_icon.setImageResource(R.drawable.scan_stop_no_risk);
                if (report.getTotalRiskCount() > 0){
                    result_desc.setText(Html.fromHtml(mContext.getString(R.string.scan_cancel_state_has_risk, report.getTotalRiskCount()),
                            Html.FROM_HTML_MODE_COMPACT));
                }else {
                    if(handle) {
                        result_desc.setText(R.string.handle_finish_no_risk);
                    }else {
                        result_desc.setText(R.string.scan_cancel_state_no_risk);
                    }
                }
            }else {
                if (report.getTotalRiskCount() > 0) {
                    result_desc.setText(Html.fromHtml(mContext.getString(R.string.scan_finish_state_has_risk, report.getTotalRiskCount()),
                            Html.FROM_HTML_MODE_COMPACT));
                    state_icon.setImageResource(R.drawable.scan_finished_has_risk);
                }else {
                    state_icon.setImageResource(R.drawable.scan_finished_no_risk);
                    if(handle) {
                        result_desc.setText(R.string.handle_finish_no_risk);
                    }else {
                        result_desc.setText(R.string.scan_finish_state_no_risk);
                    }
                }
            }
        }
    }

    private static class AppRiskCard extends RecyclerView.ViewHolder implements  View.OnClickListener, SliderView.OnSliderButtonLickListener {
         final TextView risk_category_title;
         final SliderView sliderView;
         final ImageView risk_app_icon;
         final TextView risk_app_name;
         final TextView risk_app_desc;
         final Button clean_btn;
         AppRisk risk;
         final WeakReference<ScanResultsAdapter> weakAdapter;
         final Context mContext;

        AppRiskCard(View view, ScanResultsAdapter adapter){
            super(view);
            weakAdapter = new WeakReference<>(adapter);
            mContext = view.getContext();
            risk_category_title = (TextView) view.findViewById(R.id.app_risk_category_title);
            risk_app_icon = (ImageView) view.findViewById(R.id.app_icon);
            risk_app_name = (TextView) view.findViewById(R.id.app_name);
            risk_app_desc = (TextView) view.findViewById(R.id.app_desc);
            clean_btn = (Button) view.findViewById(R.id.btn_clean_up);
            clean_btn.setOnClickListener(this);

            sliderView = (SliderView) view.findViewById(com.mst.internal.R.id.slider_view);
            sliderView.addTextButton(1, mContext.getString(R.string.ignore));
            sliderView.setButtonBackgroundColor(0, mContext.getColor(R.color.bg_ignore));
            sliderView.setOnSliderButtonClickListener(this);
            sliderView.setCustomBackground(SliderView.CUSTOM_BACKGROUND_RIPPLE);
        }

        void bindData(AppRisk risk, boolean first){
            if (this.risk == risk && !first) return;
            this.risk = risk;
            ScanResultsAdapter adapter = weakAdapter.get();
            if (adapter == null) return;
            if (first){
                risk_category_title.setVisibility(View.VISIBLE);
                DiagnosticReport report;
                report = adapter.getReport();
                risk_category_title.setText(mContext.getString(R.string.app_risk_category_title, report.getAppRiskCount()));
            }else {
                risk_category_title.setVisibility(View.GONE);
            }

            risk_app_name.setText(risk.getEntity().softName);
            if (isAppRisk(risk.getEntity())) {
                adapter.getIconCache().loadIcon(risk_app_icon, risk.getEntity().packageName);
                risk_app_desc.setText(R.string.the_app_containing_virus_recommended_to_clean);
            }else {
                adapter.getIconCache().loadIcon(risk_app_icon, risk.getEntity().path);
                risk_app_desc.setText(R.string.the_package_containing_virus_recommended_to_clean);
            }
        }

        @Override
        public void onClick(View v) {
            ScanResultsAdapter adapter = weakAdapter.get();
           if (v.getId() == R.id.btn_clean_up){
               sliderView.close(true);
                if (adapter != null)
                    adapter.handleRisk(risk);
            }
        }

        @Override
        public void onSliderButtonClick(int i, View view, ViewGroup viewGroup) {
            if (1 == i){
                ScanResultsAdapter adapter = weakAdapter.get();
                sliderView.close(true);
                //忽略应用
                if (adapter != null)
                    adapter.ignoreRisk(risk);
            }
        }
    }

    private static class RiskCard extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView risk_category_title;
        final ImageView risk_logo;
        final TextView risk_title;
        final TextView risk_summary;
        final Button btn_suggest;
        private RiskOrError risk;
        final WeakReference<ScanResultsAdapter> weakAdapter;
        final Context mContext;

        RiskCard(View view, ScanResultsAdapter adapter){
            super(view);
            risk_category_title = (TextView) view.findViewById(R.id.risk_category_title);
            risk_logo = (ImageView) view.findViewById(R.id.risk_logo);
            risk_title = (TextView) view.findViewById(R.id.risk_title);
            risk_summary = (TextView) view.findViewById(R.id.risk_summary);
            btn_suggest = (Button) view.findViewById(R.id.btn_suggest);
            btn_suggest.setOnClickListener(this);
            mContext = view.getContext();
            weakAdapter = new WeakReference<>(adapter);
        }

        void bindData(RiskOrError risk){
            if (this.risk == risk) return;
            this.risk = risk;
            risk_category_title.setText(Utils.getRiskCategoryTitle(risk.getScanType()));
            int logoId = Utils.getRiskCategoryLogo(risk.getScanType());
            if (logoId > 0) {
                risk_logo.setImageResource(logoId);
            }
            risk_title.setText(Utils.getRiskTitle(mContext, risk));
            risk_summary.setText(Utils.getRiskSummary(mContext, risk.getScanType()));
            btn_suggest.setText(Utils.getSuggestText(mContext, risk.getScanType()));
        }

        @Override
        public void onClick(View v) {
            ScanResultsAdapter adapter = weakAdapter.get();
            if (adapter != null)
              adapter.handleRisk(risk);
        }
    }

    private static class SecurityCard extends RecyclerView.ViewHolder{
        final View sperator;
        final View wifi_protected;
        final View mms_protected;
        final View system_protected;
        final View app_protected;

        SecurityCard(View view){
            super(view);
            sperator = view.findViewById(R.id.sperator);
            wifi_protected = view.findViewById(R.id.wifi_protected);
            mms_protected = view.findViewById(R.id.mms_protected);
            system_protected = view.findViewById(R.id.system_protected);
            app_protected = view.findViewById(R.id.app_protected);
        }

        void bindData(DiagnosticReport report){
            sperator.setVisibility(report.getTotalRiskCount() == 0 ? View.GONE :View.VISIBLE);
            wifi_protected.setVisibility(report.getWifiRiskCount() == 0 ? View.VISIBLE : View.GONE);
            mms_protected.setVisibility(report.getMmsRiskCount() == 0 ? View.VISIBLE : View.GONE);
            system_protected.setVisibility(report.getSystemBugCount() == 0 ? View.VISIBLE : View.GONE);
            app_protected.setVisibility(report.getAppRiskCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private static class PaymentAppListCard extends RecyclerView.ViewHolder{

        final PayListCard payList;

        PaymentAppListCard(View view){
            super(view);
            ViewStub stub = (ViewStub) view.findViewById(R.id.view_stub);
            View root = stub.inflate();
            payList = new PayListCard(view.getContext(), root);
        }

        void bindData(boolean refresh){
            payList.initData(refresh);
        }
    }

    private static class FinishBtnCard extends RecyclerView.ViewHolder implements View.OnClickListener{
        final Button finished_btn;
        final Context mContext;
        DiagnosticReport report;
        FinishBtnCard(View view){
            super(view);
            mContext = view.getContext();
            finished_btn = (Button) view.findViewById(R.id.finish_btn);
            finished_btn.setOnClickListener(this);
        }

        void bindData(final DiagnosticReport report){
            this.report = report;
            if (report.isScanCanceled()){
                finished_btn.setText(R.string.restart_scanning);
            }else {
                finished_btn.setText(R.string.finished);
            }
        }

        @Override
        public void onClick(View v) {
            if (mContext instanceof FragmentChangeHandler) {
                FragmentChangeHandler handler = (FragmentChangeHandler) mContext;
                Bundle args = null;
                if (report.isScanCanceled()) {
                    args = new Bundle();
//                    args.putBoolean(ScanFragment.START_SCAN, true);
                }
                handler.notifyFragmentChange(FragmentChangeHandler.ACTION_REMOVE, ScanResultFragment.TAG, args);
            }
        }
    }

    private static boolean isAppRisk(QScanResultEntity entity){
        return entity.apkType != APK_TYPE_UNINSTALLED;
    }

}
