package com.android.systemui.tcl;

import android.app.ActionBar.LayoutParams;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.systemui.R;
import com.wandoujia.nisdk.core.model.NotificationPriority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import mst.app.dialog.AlertDialog;

public class NotifyInfoActivity extends BaseActivity implements OnClickListener {
    private Context mContext;
    private ImageButton mCleanBtn;
    private LinearLayout mContents;
    private LinearLayout mEmptyView;
    private RelativeLayout mNormalView;
    private Button mNotifyManagerBtn;
    private DataChangeReceiver dataChangeReceiver;
    private WdjNotifyClassify mWdjNotifyClassify;
    private NotifyHandle mNotifyHandle = null;
    private boolean hideManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = this;
        setMstContentView(R.layout.notify_info); //Activity的布局
        setTitle(R.string.notify_clean_title);
        getToolbar().setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);
        mNormalView = (RelativeLayout) this.findViewById(R.id.info);
        mEmptyView = (LinearLayout) this.findViewById(R.id.empty);
        mNotifyManagerBtn = (Button) this.findViewById(R.id.notifyManagerBtn);
        mNotifyManagerBtn.setOnClickListener(this);
        mWdjNotifyClassify = WdjNotifyClassify.getInstance(this);
        mNotifyHandle = SpamNotifyHandle.getInstance(this);

        Intent intent = this.getIntent();
        hideManager = intent.getBooleanExtra("hideManager", false);
        if (hideManager) {
            mNotifyManagerBtn.setVisibility(View.GONE);
        } else {

            mNotifyManagerBtn.setVisibility(View.VISIBLE);
        }
        inflateToolbarMenu(R.menu.notify_info_toolbar_menu);
        updateUI();
        dataChangeReceiver = new DataChangeReceiver();
        dataChangeReceiver.registerReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataChangeReceiver.unregisterReceiver();
    }


    private void updateUI() {
        List<WdjNotifyType> wdjNotifyTypeList = mNotifyHandle.getWdjNotifyTypeList();
        Collections.sort(wdjNotifyTypeList, comparator);
        if (0 < wdjNotifyTypeList.size()) {
            // 有提醒，显示提醒布局
            if (hideManager) {
                getToolbar().getMenu().findItem(R.id.menu_info_details).setVisible(false);
            } else {
                getToolbar().getMenu().findItem(R.id.menu_info_details).setVisible(true);
            }

            mEmptyView.setVisibility(View.GONE);
            mNormalView.setVisibility(View.VISIBLE);
            mContents = (LinearLayout) this.findViewById(R.id.cotents);
            mCleanBtn = (ImageButton) this.findViewById(R.id.clean);
            mCleanBtn.setOnClickListener(this);
            mContents.removeAllViews();
            inflateTipsView();
            Iterator<WdjNotifyType> iterator = wdjNotifyTypeList.iterator();
            while (iterator.hasNext()) {
                WdjNotifyType type = iterator.next();
                Queue<WdjNotifyClearItem> wdjNotifyClearItems = mNotifyHandle.getWdjNotifyClearItems(type);
                //添加动态视图
                View groupView = getNotifyGroupView(wdjNotifyClearItems);
                if (groupView != null) {
                    mContents.addView(groupView);
                }
            }
        } else {
            // 没有有提醒，显示empty布局
            getToolbar().getMenu().findItem(R.id.menu_info_details).setVisible(false);
            mEmptyView.setVisibility(View.VISIBLE);
            mNormalView.setVisibility(View.GONE);
        }
    }

    private void inflateTipsView() {
        SharedPreferences preferences = getSharedPreferences("notify_settings", MODE_PRIVATE);
        if (!preferences.getBoolean("user_close_tips", false)) {

            final View view = getLayoutInflater().inflate(R.layout.notify_info_tips, null);
            ImageView close = (ImageView) view.findViewById(R.id.iv_close_tip);
            close.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContents.removeView(view);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("user_close_tips", true);
                    editor.commit();
                }
            });
            mContents.addView(view);
        }
    }

    private Comparator<WdjNotifyType> comparator = new Comparator<WdjNotifyType>() {
        @Override
        public int compare(WdjNotifyType o1, WdjNotifyType o2) {
            if (o1.getUpdateTime() > o2.getUpdateTime()) {
                return -1;
            } else if (o1.getUpdateTime() < o2.getUpdateTime()) {
                return 1;
            }
            return 0;
        }
    };

    @Override
    public void onNavigationClicked(View view) {
        //在这里处理Toolbar上的返回按钮的点击事件
        finish();
    }

    public void inflateToolbarMenu(int resId) {
        if (getToolbar() != null) {
            getToolbar().inflateMenu(resId);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        //在这里处理Toolbar上的Menu的点击事件
        switch (item.getItemId()) {
            case R.id.menu_info_details:
                Intent intent = new Intent("android.intent.action.SHOW_NOTIFY_MANAGE_ACTIVITY");
                intent.putExtra("shownotify", false);
                startActivity(intent);
                break;

            default:
                break;
        }
        return super.onMenuItemClick(item);
    }

    private View getNotifyGroupView(Queue<WdjNotifyClearItem> wdjNotifyClearItems) {
        // TODO 动态添加布局(xml方式)
        if (wdjNotifyClearItems == null) return null;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.notify_detail_title, null);
        view.setLayoutParams(lp);

        // 获取全部的分类，为拦截设置做准备
        final Vector<String> tags = new Vector<String>();
        List<Boolean> openStates = new ArrayList<>();
        for (WdjNotifyClearItem item : wdjNotifyClearItems) {
            if (false == tags.contains(item.getFilterResult().categoryKey)) {
                tags.add(item.getFilterResult().categoryKey);
                openStates.add(mWdjNotifyClassify.getCustomPriority(item.getSbn().getPackageName(), item.getFilterResult().categoryKey) == NotificationPriority.IMPORTANT);
            }
        }
        List<WdjNotifyClearItem> temps = new ArrayList<>();
        for (WdjNotifyClearItem item : wdjNotifyClearItems) {
            temps.add(item);
        }
        Collections.reverse(temps);
        final WdjNotifyClearItem wdjNotifyClearItem = wdjNotifyClearItems.peek();
        // 设置应用的图标
        ImageButton ib = (ImageButton) view.findViewById(R.id.icon);
        Drawable drawable = Utils.getIconDrawable(this, wdjNotifyClearItem.getSbn().getPackageName());
        ib.setBackground(drawable);
        // 设置应用的名字
        TextView packagename = (TextView) view.findViewById(R.id.packagename);
        packagename.setText(Utils.getApplicationLabelAsUser(this, wdjNotifyClearItem.getSbn().getPackageName(), UserHandle.getUserId(wdjNotifyClearItem.getSbn().getUid())) + "-" + mWdjNotifyClassify.getCategoryByTag(this, wdjNotifyClearItem.getFilterResult().categoryKey));


        // 设置清除按钮
        final Button cleanSetBtn = (Button) view.findViewById(R.id.cleanSet);
        if (openStates.contains(false)) {
            cleanSetBtn.setTag(false);
            cleanSetBtn.setText(R.string.cancel_clear);
        } else {
            cleanSetBtn.setTag(true);
            cleanSetBtn.setText(R.string.confirm_clear);
        }
        cleanSetBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                showConfirmDialog(cleanSetBtn, wdjNotifyClearItem.getSbn(), tags);
            }
        });
        // 添加详情
        LinearLayout detail = (LinearLayout) view.findViewById(R.id.details);
        if (null != detail) {
            // 循环添加每个提醒,按时间先后顺序重新排序

            for (int i = 0; i < temps.size(); i++) {
                WdjNotifyClearItem item = temps.get(i);
                View detailsView = getNotifyItemView(item);
                if (i > 1) {
                    //默认只显示2个
                    detailsView.setVisibility(View.GONE);
                }
                detail.addView(detailsView);
            }
        }
        //当列表大于2时，默认只显示2个，其余收起
        ImageView expandState = (ImageView) view.findViewById(R.id.iv_expand);
        if (temps.size() > 2) {
            expandState.setVisibility(View.VISIBLE);
        } else {
            expandState.setVisibility(View.GONE);
        }
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.rl_title);
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = detail.getChildCount();
                if (n > 2) {
                    int visible = detail.getChildAt(2).getVisibility();
                    int resId =
                            visible == View.VISIBLE ? R.drawable.notify_info_expand : R.drawable.notify_info_collasp;
                    expandState.setImageDrawable(getDrawable(resId));
                    for (int i = 2; i < n; i++) {
                        detail.getChildAt(i).setVisibility(visible == View.VISIBLE ? View.GONE : View.VISIBLE);
                    }
                }
            }
        });
        return view;
    }

    private View getNotifyItemView(WdjNotifyClearItem tclSbn) {
        TextView title, time, description;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.notify_detail, null);
        title = (TextView) view.findViewById(R.id.title);
        time = (TextView) view.findViewById(R.id.time);
        description = (TextView) view.findViewById(R.id.desc);

        final StatusBarNotification sbn = tclSbn.getSbn();

        // 设置提醒title
        title.setText(getTitle(sbn));
        // 设置时间
        time.setText(DateUtils.getRelativeTimeSpanString(sbn.getPostTime()));  //("12:30");
        // 设置提醒内容
        description.setText(getContent(sbn.getNotification()));

        view.setLayoutParams(lp);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PendingIntent pi = sbn.getNotification().contentIntent;
                    if (pi == null) {
                        Intent launchIntent = Utils.getIntentByPackageName(NotifyInfoActivity.this, sbn.getPackageName());
                        if (launchIntent != null) {
                            pi = PendingIntent.getActivity(NotifyInfoActivity.this, 0, launchIntent, 0);
                        }
                    }
                    if (pi != null) {
                        pi.send();
                    }
                } catch (CanceledException e) {
                }
            }
        });
        return view;
    }

    public String getTitle(StatusBarNotification notification) {
        CharSequence title = notification.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE);
        return title == null ? Utils.getApplicationLabelAsUser(this, notification.getPackageName(), notification.getUserId()) : title.toString();
    }

    public String getContent(Notification notification) {
        CharSequence content = null;
        if (notification.extras != null) {
            content = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
        }

        if (TextUtils.isEmpty(content)) {
            content = notification.tickerText;
        }
        if (TextUtils.isEmpty(content) && notification.extras != null) {
            content = notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        }
        return content == null ? getString(R.string.notification_default_text) : content.toString();
    }

    public Intent getIntent(Notification notification) {
        Intent intent = notification.contentIntent.getIntent();
        return intent;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.clean:
                getToolbar().getMenu().findItem(R.id.menu_info_details).setVisible(false);
                mNotifyHandle.clearNotify();
                // 没有有提醒，显示empty布局
                mEmptyView.setVisibility(View.VISIBLE);
                mNormalView.setVisibility(View.GONE);
                break;
            case R.id.notifyManagerBtn:
                Intent intent = new Intent("android.intent.action.SHOW_NOTIFY_MANAGE_ACTIVITY");
                intent.putExtra("shownotify", false);
                startActivity(intent);
                break;
        }
    }

    private void showConfirmDialog(Button button, StatusBarNotification sbn, Vector<String> tags) {
        boolean open = ((Boolean) button.getTag() == true);
        String content;
        if (open) {
            content = getString(R.string.dialog_content_clear);
        } else {
            content = getString(R.string.dialog_content_no_clear);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(content);
        builder.setTitle(getString(R.string.dialog_tips));
        builder.setPositiveButton(getString(R.string.dialog_btn_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (open) {
                    button.setTag(false);
                    button.setText(R.string.cancel_clear);
                    for (String tag : tags) {
                        mWdjNotifyClassify.updateCustomRule(sbn.getPackageName(), tag, NotificationPriority.SPAM);
                    }
                } else {
                    button.setTag(true);
                    button.setText(R.string.confirm_clear);
                    for (String tag : tags) {
                        mWdjNotifyClassify.updateCustomRule(sbn.getPackageName(), tag, NotificationPriority.IMPORTANT);
                    }
                }
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_btn_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    private static final int MSG_UPDATE_UI = 0;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_UI:
                    updateUI();
                    break;
                default:
                    break;
            }
        }
    };

    private class DataChangeReceiver extends BroadcastReceiver {
        void registerReceiver() {
            IntentFilter inf = new IntentFilter();
            inf.addAction(Utils.ACTION_NOTIFY_SETTING_CHANGE);
            inf.addAction(Utils.ACTION_NOTIFICATION_DATA_CHANGE);
            mContext.registerReceiver(this, inf);
        }

        void unregisterReceiver() {
            mContext.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (Utils.ACTION_NOTIFY_SETTING_CHANGE.equals(actionStr) || Utils.ACTION_NOTIFICATION_DATA_CHANGE.equals(actionStr)) {
                handler.removeMessages(MSG_UPDATE_UI);
                handler.sendMessageDelayed(handler.obtainMessage(MSG_UPDATE_UI), 500);
            }
        }
    }

}
