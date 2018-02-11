package com.monster.market.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.monster.market.activity.UpdateSettingsPreferenceActivity;
import com.monster.market.bean.AppUpgradeInfo;
import com.monster.market.constants.Constant;
import com.monster.market.constants.WandoujiaDownloadConstant;
import com.monster.market.download.AppDownloadData;
import com.monster.market.download.AppDownloadService;
import com.monster.market.http.DataResponse;
import com.monster.market.http.RequestError;
import com.monster.market.http.RequestHelper;
import com.monster.market.http.data.AppUpgradeInfoRequestData;
import com.monster.market.http.data.AppUpgradeListResultData;
import com.monster.market.install.InstallNotification;
import com.monster.market.utils.ApkUtil;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.SettingUtil;
import com.monster.market.utils.SystemUtil;
import com.monster.market.utils.TimeUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaobin on 16-9-7.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    public static final String TAG = "NetworkChangeReceiver";

    private SharedPreferences sp;

    @Override
    public void onReceive(final Context context, Intent intent) {
        LogUtil.i(TAG, "========================NetworkChangeReceiver=======================");

        int currentNetStatus = SystemUtil.getNetStatus(context);
        LogUtil.i(TAG, "currentNetStatus="+currentNetStatus);

        sp = context.getSharedPreferences(Constant.SHARED_WIFI_UPDATE,
                context.MODE_APPEND);
        final SharedPreferences.Editor ed = sp.edit();

        int netStatus = sp.getInt(Constant.SHARED_NETSTATUS_KEY,
                Constant.SHARED_NETSTATUS_NO_NETWORK);

        LogUtil.i(TAG, "netStatus="+currentNetStatus);

        if(netStatus == currentNetStatus )
        {
            return;
        }

        if ((netStatus == Constant.SHARED_NETSTATUS_WIFI)
                && (currentNetStatus == Constant.SHARED_NETSTATUS_MOBILE)) {
            Intent networkChange = new Intent(context, AppDownloadService.class);
            networkChange.putExtra(AppDownloadService.DOWNLOAD_OPERATION,
                    AppDownloadService.OPERATION_NETWORK_MOBILE_PAUSE);
            context.startService(networkChange);
        }
        else if((netStatus == Constant.SHARED_NETSTATUS_MOBILE) &&
                (currentNetStatus == Constant.SHARED_NETSTATUS_WIFI))
        {
            Intent networkChange = new Intent(context, AppDownloadService.class);
            networkChange.putExtra(AppDownloadService.DOWNLOAD_OPERATION,
                    AppDownloadService.OPERATION_NETWORK_MOBILE_CONTINUE);
            context.startService(networkChange);
        }
        else if((netStatus == Constant.SHARED_NETSTATUS_NO_NETWORK) &&
                (currentNetStatus == Constant.SHARED_NETSTATUS_MOBILE))
        {
            Intent networkChange = new Intent(context, AppDownloadService.class);
            networkChange.putExtra(AppDownloadService.DOWNLOAD_OPERATION,
                    AppDownloadService.OPERATION_NETWORK_MOBILE_PAUSE);
            context.startService(networkChange);
        }
        else {
            Intent networkChange = new Intent(context, AppDownloadService.class);
            networkChange.putExtra(AppDownloadService.DOWNLOAD_OPERATION,
                    AppDownloadService.OPERATION_NETWORK_CHANGE);
            context.startService(networkChange);
        }
        ed.putInt(Constant.SHARED_NETSTATUS_KEY, currentNetStatus);
        ed.commit();


        if (SystemUtil.isWifiNetwork(context)) {

            final boolean bl = UpdateSettingsPreferenceActivity.getPreferenceValue(context,
                    UpdateSettingsPreferenceActivity.SOFTWARE_AUTO_UPDATE_TIP_KEY, true);

            final boolean bl2 = UpdateSettingsPreferenceActivity.getPreferenceValue(context,
                    UpdateSettingsPreferenceActivity.WIFI_AUTO_UPGRADE_KEY);

            if (!bl && !bl2)
                return;
            // 获取提示升级时间
            String time = sp.getString(
                    Constant.SHARED_WIFI_APPUPDATE_KEY_UPDATETIME, "0");
            if (!time.equals(TimeUtil.getStringDateShort())) {

                final Context m_context = context;
                new Thread() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        final List<AppUpgradeInfoRequestData> infoList = ApkUtil.getUpgradeList(context);
                        RequestHelper.getAppUpdateList(context, infoList,
                            new DataResponse<AppUpgradeListResultData>() {
                                @Override
                                public void onResponse(AppUpgradeListResultData value) {

                                    if (value.getAppList() != null) {
                                        SettingUtil.setLastUpdateAppCount(m_context, value.getAppList().size());
                                    }

                                    if (value.getAppList().size() > 0) {
                                        ArrayList<AppDownloadData> downDataList = new ArrayList<AppDownloadData>();
                                        for (AppUpgradeInfo info : value.getAppList()) {
                                            AppDownloadData data = SystemUtil.buildAppDownloadData(info);
                                            data.setPos(WandoujiaDownloadConstant.POS_UPDATE);
                                            data.setDownload_type(WandoujiaDownloadConstant.TYPE_UPDATE);
                                            downDataList.add(data);
                                        }

                                        if ((null != downDataList) && (downDataList.size() > 0)) {
                                            if (bl) {
                                                InstallNotification.sendUpdateNotify(downDataList);
                                            }
                                            if (bl2) {
                                                for (AppDownloadData downData : downDataList) {
                                                    AppDownloadService.startDownload(context, downData);
                                                }
                                            }

                                            SharedPreferences sp = context.getSharedPreferences(Constant.SHARED_WIFI_UPDATE,
                                                    context.MODE_APPEND);
                                            final SharedPreferences.Editor ed = sp.edit();
                                            ed.putString(
                                                    Constant.SHARED_WIFI_APPUPDATE_KEY_UPDATETIME,
                                                    TimeUtil.getStringDateShort());
                                            ed.commit();
                                        }


                                    } else {
                                        // no data

                                    }

                                }

                                @Override
                                public void onErrorResponse(RequestError error) {

                                }
                            });
                    }

                }.start();
            }
        }


        // 更新网络加载图片状态
        if (SettingUtil.isLoadingImage(context)) {
            ImageLoader.getInstance().denyNetworkDownloads(false);
        } else {
            ImageLoader.getInstance().denyNetworkDownloads(true);
        }

    }

}
