package com.android.systemui.tcl;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.tcl.NotificationBackend.AppRow;
import com.wandoujia.nisdk.core.db.model.CustomRuleDBModel;
import com.wandoujia.nisdk.core.model.NotificationPriority;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import mst.app.MstActivity;

public class AppNotifySetting extends BaseActivity implements OnClickListener {


    private Intent mIntent;
    private RelativeLayout mBlockArea;
    private RelativeLayout mIntelligentArea;
    private LinearLayout mSuperScript;
    private LinearLayout mLockScreenNotify;
    private LinearLayout mIntelligentNotify;
    private LinearLayout mContentLayout;

    // 保留该分类的各个子类
    private LinkedHashMap<Integer, List<String>> mCategoryMap;
    // 保存该分类是否接收
    private LinkedHashMap<Integer, Boolean> mCategoryPriorityMap;

    private String mPackageName;
    private int mUid;
    private Switch mBlock;
    private Switch mShowDetailLockScreen;
    private List<Switch> switches = new ArrayList<>();
    private final NotificationBackend mBackend = new NotificationBackend();

    private AppRow mAppRow;

    private boolean mIsSystemPackage;

    private WdjNotifyClassify mWdjNotify;
    private List<CustomRuleDBModel> customRule;

    private static final String ACTION = "com.monster.notification.unread_setting_change";
    private static final String EXTRA = "package";

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);

        setMstContentView(R.layout.app_notify_setting);
        getToolbar().setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);

        // 根据intent查找需要设置的应用信息
        mIntent = getIntent();
        mPackageName = mIntent.getStringExtra(Settings.EXTRA_APP_PACKAGE);
        mUid = mIntent.getIntExtra(Settings.EXTRA_APP_UID, 0);
        Log.e("kebelzc24", "pkg = " + mPackageName);
        //find view
        mBlockArea = (RelativeLayout) this.findViewById(R.id.block_area);
        mIntelligentArea = (RelativeLayout) this.findViewById(R.id.intelligent_area);
        mContentLayout = (LinearLayout) findViewById(R.id.content_layout);
        // 设置应用通知的设置
        mBlock = (Switch) this.findViewById(R.id.allow_send_checkbox);
        mBlock.setOnClickListener(this);
        // 设置角标
        mSuperScript = (LinearLayout) this.findViewById(R.id.superscript);
        // 设置锁屏通知
        mLockScreenNotify = (LinearLayout) this.findViewById(R.id.lock_screen);
        // 设置智能提醒
        mIntelligentNotify = (LinearLayout) this.findViewById(R.id.intelligent_notify);
        loadAppInfo();
        new Thread(loadAppIcon).start();
        sortCategory();
        setUi();
    }

    private void setUi() {
        // 设置应用名字
        setTitle(mAppRow.label);
        mBlock.setChecked(mAppRow.banned);
        // 更新界面显示
        mContentLayout.setVisibility(mAppRow.banned ? View.VISIBLE : View.GONE);
        addSubItemView();
    }

    private void setAppIcon() {
        // 设置应用图标
        ImageView icon = (ImageView) this.findViewById(R.id.icon);
        icon.setBackground(mAppRow.icon);
    }

    private void addSubItemView() {
        mSuperScript.addView(addItemWithDescView(R.drawable.wdj_launcher_notify,
                R.string.notify_desk_superscript, R.string.notify_desk_superscript_desc, mSuperScriptCallBack, mAppRow.superscript));
        mLockScreenNotify.addView(addItemWithDescView(R.drawable.wdj_lock_screen_notify,
                R.string.notify_lock_screen, R.string.notify_lock_screen_desc, mLockScreenNotifyCallBack, mAppRow.lockscreennotify));

        mLockScreenNotify.addView(addItemWithDescView(R.drawable.wdj_show_detail,
                R.string.notify_show_detail, R.string.notify_show_detail_desc, mShowdetailCallBack, !mAppRow.sensitive));
        if (0 == mCategoryMap.size()) {
            mIntelligentArea.setVisibility(View.GONE);
        } else {
            // 再显示所有分类
            for (Integer categoryResID : mCategoryMap.keySet()) {
                mIntelligentNotify.addView(addItemWithDescView(mWdjNotify.getDrawableWithCategoryResID(categoryResID),
                        categoryResID, 0, mSmartCategoryCallBack,
                        mCategoryPriorityMap.get(categoryResID), categoryResID));
            }
        }

    }

    private Runnable loadAppIcon = new Runnable() {
        @Override
        public void run() {
            mAppRow.icon = Utils.getIconDrawable(AppNotifySetting.this, mAppRow.pkg);
            handler.sendEmptyMessage(MSG_LOAD_APP_ICON_COMPLETE);
        }
    };

    private void loadAppInfo() {
        final PackageManager pm = getPackageManager();
        final PackageInfo info = Utils.findPackageInfo(pm, mPackageName, mUid);
        mAppRow = mBackend.loadAppRow(AppNotifySetting.this, pm, info.applicationInfo);
        mIsSystemPackage = Utils.isSystemPackage(pm, info);
    }

    // 角标提醒回调函数
    private OnClickListener mSuperScriptCallBack = new OnClickListener() {

        @Override
        public void onClick(View view) {
            // TODO Auto-generated method stub
            Boolean banned = ((Switch) view).isChecked();
            final boolean success = mBackend.setSuperScript(mPackageName, mUid, banned);
            notifyLauncherChange(mPackageName);
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    // 锁屏通知回调函数
    private OnClickListener mLockScreenNotifyCallBack = new OnClickListener() {

        @Override
        public void onClick(View view) {
            // TODO Auto-generated method stub
            Boolean banned = ((Switch) view).isChecked();
            final boolean success = mBackend.setLockScreenNotify(mPackageName, mUid, banned);
            if (success) {
                mShowDetailLockScreen.setEnabled(banned);
                mAppRow.lockscreennotify = banned;
            }

        }

    };

    // 显示详情回调函数
    private OnClickListener mShowdetailCallBack = new OnClickListener() {

        @Override
        public void onClick(View view) {
            // TODO Auto-generated method stub
            Boolean banned = ((Switch) view).isChecked();
            final boolean success = mBackend.setSensitive(mPackageName, mUid, !banned);
        }

    };

    // 显示智能通知的回调函数
    private OnClickListener mSmartCategoryCallBack = new OnClickListener() {

        @Override
        public void onClick(View view) {
            // TODO Auto-generated method stub
            Boolean banned = ((Switch) view).isChecked();
            List<String> Categorys = mCategoryMap.get(view.getTag());
            if (null != Categorys) {
                for (String category : Categorys) {
                    mWdjNotify.updateCustomRule(mPackageName, category,
                            (true == banned) ? NotificationPriority.IMPORTANT : NotificationPriority.SPAM);
                    //updateCustomRule异步执行，需要延迟一会在刷新
                    handler.removeMessages(MSG_UPDATE_NOTIFY_INFO);
                    handler.sendEmptyMessageDelayed(MSG_UPDATE_NOTIFY_INFO, 150);
                }
            }
        }
    };

    private static final int MSG_UPDATE_NOTIFY_INFO = 1;
    private static final int MSG_LOAD_APP_INFO_COMPLETE = 2;
    private static final int MSG_LOAD_APP_ICON_COMPLETE = 3;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_UPDATE_NOTIFY_INFO:
                    updateNotifyInfoActivity();
                    break;
                case MSG_LOAD_APP_INFO_COMPLETE:
                    setUi();
                    break;
                case MSG_LOAD_APP_ICON_COMPLETE:
                    setAppIcon();
                    break;
                default:
                    break;
            }
        }
    };

    private void updateNotifyInfoActivity() {
        sendBroadcast(new Intent(Utils.ACTION_NOTIFY_SETTING_CHANGE));
    }

    // 不是豌豆荚的分类调用这个接口
    private View addItemWithDescView(int iconRes, int name, int desc, OnClickListener callback, boolean ischecked) {
        return addItemWithDescView(iconRes, name, desc, callback, ischecked, 0);
    }

    // 豌豆荚的分类调用此接口，传入categoryID 方便回调函数使用
    private View addItemWithDescView(int iconRes, int name, int desc, OnClickListener callback, boolean ischecked, int categoryID) {
        // TODO 动态添加布局(xml方式)
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        RelativeLayout inflater = new RelativeLayout(this);
        View view = inflater.inflate(this, R.layout.mst_app_notify_setting_item_with_desc, null);
        view.setLayoutParams(lp);

        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        icon.setBackgroundResource(iconRes);

        TextView item = (TextView) view.findViewById(R.id.item);
        item.setText(name);

        TextView descView = (TextView) view.findViewById(R.id.desc);
        if (0 != desc) {
            descView.setVisibility(View.VISIBLE);
            descView.setText(desc);
        }

        Switch onoff = (Switch) view.findViewById(R.id.onoff_switch);
        onoff.setOnClickListener(callback);
        onoff.setChecked(ischecked);
        // 保存categoryID，方便回调函数中使用
        onoff.setTag(categoryID);
        switches.add(onoff);
        if (R.string.notify_show_detail == name) {
            mShowDetailLockScreen = onoff;
        }
        return view;
    }


    @Override
    public void onNavigationClicked(View view) {
        //在这里处理Toolbar上的返回按钮的点击事件
        finish();
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {
            case R.id.allow_send_checkbox: {
                Boolean banned = mBlock.isChecked();
                final boolean success = mBackend.setNotificationsBanned(mPackageName, mUid, banned);
                if (success) {
                    sendUpdateBroadCast(mPackageName, banned);
                }
                mContentLayout.setVisibility(banned ? View.VISIBLE : View.GONE);
            }
            break;

        }
    }

    private void sendUpdateBroadCast(String pkg, boolean banned) {
        Intent intent = new Intent(Utils.ACTION_NOTIFY_BANNED_CHANGE);
        intent.putExtra("package", pkg);
        intent.putExtra("banned", banned);
        intent.putExtra("uid", mUid);
        sendBroadcast(intent);
    }



    // 获取本应用的notify分类
    private void sortCategory() {
        mCategoryMap = new LinkedHashMap<Integer, List<String>>();
        mCategoryPriorityMap = new LinkedHashMap<Integer, Boolean>();
        // 查询应用在豌豆荚中的提醒种类
        mWdjNotify = WdjNotifyClassify.getInstance(this);
        customRule = mWdjNotify.getCustomRuleDBModel(mPackageName);
        // 根据种类来显示界面
        for (CustomRuleDBModel rule : customRule) {
            int key = mWdjNotify.getCategoryResID(rule.categoryKey);
            List<String> categorys = mCategoryMap.get(key);
            if (null == categorys) {
                categorys = new ArrayList<String>();
                mCategoryMap.put(key, categorys);
            }
            categorys.add(rule.categoryKey);

            if (rule.priority == NotificationPriority.SPAM ||
                    (rule.priority == NotificationPriority.NORMAL &&
                            mWdjNotify.getCategory(rule.categoryKey).priority == NotificationPriority.SPAM)) {
                mCategoryPriorityMap.put(key, false);
            } else {
                mCategoryPriorityMap.put(key, true);
            }
        }
    }

    //notify launcher to update
    private void notifyLauncherChange(String pkg) {
        Intent intent = new Intent(ACTION);
        intent.putExtra(EXTRA, pkg);
        sendBroadcast(intent);
    }

}
