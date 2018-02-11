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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import java.util.List;
import java.util.Queue;
import java.util.Stack;
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
    NotifyHandle mNotifyHandle = null;

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
        boolean hideManager = intent.getBooleanExtra("hideManager", false);
        if (hideManager) {
            mNotifyManagerBtn.setVisibility(View.GONE);
        } else {
            inflateToolbarMenu(R.menu.notify_info_toolbar_menu);
            mNotifyManagerBtn.setVisibility(View.VISIBLE);
        }

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
        Stack<WdjNotifyType> wdjNotifyTypeStack = mNotifyHandle.getPackageStack();
        if (0 < wdjNotifyTypeStack.size()) {
            // 有提醒，显示提醒布局
            getToolbar().getMenu().findItem(R.id.menu_info_details).setVisible(true);
            mEmptyView.setVisibility(View.GONE);
            mNormalView.setVisibility(View.VISIBLE);
            mContents = (LinearLayout) this.findViewById(R.id.cotents);
            mCleanBtn = (ImageButton) this.findViewById(R.id.clean);
            mCleanBtn.setOnClickListener(this);
            mContents.removeAllViews();
            while (!wdjNotifyTypeStack.isEmpty()) {
                WdjNotifyType type = wdjNotifyTypeStack.pop();
                Queue<TclNotification> notifyQueue = mNotifyHandle.getNotifyQueue(type);
                //添加动态视图
                View child = addNotifyView(type, notifyQueue);
                if (child != null) {
                    mContents.addView(child);
                }
            }
        } else {
            // 没有有提醒，显示empty布局
            getToolbar().getMenu().findItem(R.id.menu_info_details).setVisible(false);
            mEmptyView.setVisibility(View.VISIBLE);
            mNormalView.setVisibility(View.GONE);
        }
    }

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

    private View addNotifyView(WdjNotifyType notifyType, Queue<TclNotification> notifyQueue) {
        // TODO 动态添加布局(xml方式)
        if (notifyQueue == null) return null;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.notify_detail_title, null);
        view.setLayoutParams(lp);

        // 获取全部的分类，为拦截设置做准备
        final Vector<String> tags = new Vector<String>();
        List<Boolean> openStates = new ArrayList<>();
        for (TclNotification tcl : notifyQueue) {
            if (false == tags.contains(tcl.getFilterResult().categoryKey)) {
                tags.add(tcl.getFilterResult().categoryKey);
                openStates.add(mWdjNotifyClassify.getCustomPriority(tcl.getSbn().getPackageName(), tcl.getFilterResult().categoryKey) == NotificationPriority.IMPORTANT);
            }
        }
        List<TclNotification> temps = new ArrayList<>();
        for (TclNotification tclsbns : notifyQueue) {
            temps.add(tclsbns);
        }
        Collections.reverse(temps);
        final TclNotification tclsbn = notifyQueue.peek();
        // 设置应用的图标
        ImageButton ib = (ImageButton) view.findViewById(R.id.icon);
        Drawable drawable = Utils.getIconDrawable(this, tclsbn.getSbn().getPackageName());
        ib.setBackground(drawable);
        // 设置应用的名字
        TextView packagename = (TextView) view.findViewById(R.id.packagename);
        packagename.setText(Utils.getApplicationLabelAsUser(this, tclsbn.getSbn().getPackageName(), notifyType.getUid()) + "-" + mWdjNotifyClassify.getCategoryByTag(this, tclsbn.getFilterResult().categoryKey));


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
                showConfirmDialog(cleanSetBtn, tclsbn.getSbn(), tags);
            }
        });
        // 添加详情
        LinearLayout detail = (LinearLayout) view.findViewById(R.id.details);
        if (null != detail) {
            // 循环添加每个提醒,按时间先后顺序重新排序

            for (int i = 0; i < temps.size(); i++) {
                TclNotification notification = temps.get(i);
                View detailsView = addDetailsView(notification);
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

    private View addDetailsView(TclNotification tclSbn) {
        TextView titleTV, timeTV, descTV;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.notify_detail, null);
        titleTV = (TextView) view.findViewById(R.id.title);
        timeTV = (TextView) view.findViewById(R.id.time);
        descTV = (TextView) view.findViewById(R.id.desc);

        final StatusBarNotification sbn = tclSbn.getSbn();

        // 设置提醒title
        titleTV.setText(getTitle(sbn));
        // 设置时间
        timeTV.setText(DateUtils.getRelativeTimeSpanString(sbn.getPostTime()));  //("12:30");
        // 设置提醒内容
        descTV.setText(getContent(sbn.getNotification()));

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
                updateUI();
            }
        }
    }

}
