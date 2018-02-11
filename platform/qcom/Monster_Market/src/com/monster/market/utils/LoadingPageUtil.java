package com.monster.market.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import mst.widget.FoldProgressBar;
import android.widget.TextView;

import com.monster.market.R;

/**
 * Created by xiaobin on 16-7-25.
 */
public class LoadingPageUtil implements View.OnClickListener {

    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    private static final int STATUS_NO_NETWORK = 1;
    private static final int STATUS_NETWORK_ERROR = 2;

    private Context context;

    private LinearLayout ll_load_page;
    private FoldProgressBar iv_loading;
    private LinearLayout ll_error;
    private ImageView iv_icon;
    private TextView tv_text;
    private Button btn;

    private int status;

    private OnShowListener onShowListener;
    private OnHideListener onHideListener;
    private OnRetryListener onRetryListener;

    private NetWorkReceiver netWorkReceiver;

    public void init(Context context, View view) {
        this.context = context;
        ll_load_page = (LinearLayout) view.findViewById(R.id.ll_load_page);
        iv_loading = (FoldProgressBar) view.findViewById(R.id.load_iv_loading);
        ll_error = (LinearLayout) view.findViewById(R.id.load_ll_error);
        iv_icon = (ImageView) view.findViewById(R.id.load_iv_icon);
        tv_text = (TextView) view.findViewById(R.id.load_tv_text);
        btn = (Button) view.findViewById(R.id.load_btn);
        btn.setOnClickListener(this);
    }

    public boolean isShowing() {
        if (ll_load_page.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }

    public void showLoadPage() {
        ll_load_page.setVisibility(View.VISIBLE);
        iv_loading.setVisibility(View.VISIBLE);
        if (onShowListener != null) {
            onShowListener.onShow();
        }
    }

    public void hideLoadPage() {
        ll_load_page.setVisibility(View.GONE);
        iv_loading.setVisibility(View.GONE);
        if (onHideListener != null) {
            onHideListener.onHide();
        }
    }

    public void showLoading() {
        unRegister();

        ll_error.setVisibility(View.GONE);
        iv_loading.setVisibility(View.VISIBLE);
        //iv_loading.clearAnimation();
        //iv_loading.startAnimation(createRotateAnimation());
    }

    public void showNoNetWork() {
        register();

        status = STATUS_NO_NETWORK;
        iv_loading.setVisibility(View.GONE);
        ll_error.setVisibility(View.VISIBLE);
        //iv_loading.clearAnimation();

        iv_icon.setImageResource(R.drawable.icon_no_network);
        tv_text.setText(context.getString(R.string.page_loading_no_network));
        btn.setText(context.getString(R.string.page_loading_set_network));
    }

    public void showNetworkError() {
        unRegister();

        status = STATUS_NETWORK_ERROR;
        iv_loading.setVisibility(View.GONE);
        ll_error.setVisibility(View.VISIBLE);
        //iv_loading.clearAnimation();

        iv_icon.setImageResource(R.drawable.icon_network_error);
        tv_text.setText(context.getString(R.string.page_loading_network_error));
        btn.setText(context.getString(R.string.page_loading_retry));
    }

    public void exit() {
        unRegister();
    }

    public void setOnShowListener(OnShowListener onShowListener) {
        this.onShowListener = onShowListener;
    }

    public void setOnHideListener(OnHideListener onHideListener) {
        this.onHideListener = onHideListener;
    }

    public void setOnRetryListener(OnRetryListener onRetryListener) {
        this.onRetryListener = onRetryListener;
    }

    public interface OnShowListener {
        public void onShow();
    }

    public interface OnHideListener {
        public void onHide();
    }

    public interface OnRetryListener {
        public void retry();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.load_btn:
                if (status == STATUS_NO_NETWORK) {
                    // open setting
                    Intent intent = new Intent(
                            android.provider.Settings.ACTION_SETTINGS);
                    context.startActivity(intent);
                } else if (status == STATUS_NETWORK_ERROR) {
                    showLoading();
                    if (onRetryListener != null) {
                        onRetryListener.retry();
                    }
                }
                break;
        }
    }

    /**
     * @Title: createRotateAnimation
     * @Description: 创建旋转动画
     * @param @return
     * @return RotateAnimation
     * @throws
     */
    private RotateAnimation createRotateAnimation() {
        RotateAnimation animation = null;
        animation = new RotateAnimation(0, 3600, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setFillAfter(true);
        animation.setDuration(10000);
        animation.setStartOffset(0);
        animation.setRepeatCount(1000);
        return animation;
    }

    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CONNECTIVITY_CHANGE_ACTION);
        if (netWorkReceiver == null) {
            netWorkReceiver = new NetWorkReceiver();
            context.registerReceiver(netWorkReceiver, filter);
        }
    }

    private void unRegister() {
        if (netWorkReceiver != null) {
            context.unregisterReceiver(netWorkReceiver);
            netWorkReceiver = null;
        }
    }

    private class NetWorkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SystemUtil.hasNetwork()) {
                showLoading();
                if (onRetryListener != null) {
                    onRetryListener.retry();
                }
            }
        }
    }

}
