/**
 * @author huliang
 */

package com.android.systemui.tcl;

import android.content.Context;
import android.service.notification.StatusBarNotification;

import com.android.systemui.R;
import com.wandoujia.nisdk.core.NIFilter;
import com.wandoujia.nisdk.core.NIFilter.FilterResult;
import com.wandoujia.nisdk.core.NIRules;
import com.wandoujia.nisdk.core.NISDK;
import com.wandoujia.nisdk.core.db.model.CategoryDBModel;
import com.wandoujia.nisdk.core.db.model.CustomRuleDBModel;
import com.wandoujia.nisdk.core.model.NICategory;
import com.wandoujia.nisdk.core.model.NotificationPriority;
import com.wandoujia.nisdk.updator.NIRulesUpdator;

import java.util.ArrayList;
import java.util.List;

public class WdjNotifyClassify {

    private static WdjNotifyClassify mInstance;
    private NIFilter niFilter;
    private NIRules niRules;
    private NIRulesUpdator rulesUpdator;

    public static final String PROMOTION = NIRules.CATEGORY_KEY_PROMOS;
    public static final String CONTENT = NIRules.CATEGORY_KEY_CONTENT;
    public static final String SHOPPING = NIRules.CATEGORY_KEY_SHOPPING;
    public static final String SOCIAL = NIRules.CATEGORY_KEY_SOCIAL;
    public static final String OPTIMIZATION = NIRules.CATEGORY_KEY_SYS_OPT;
    public static final String STATUS = NIRules.CATEGORY_KEY_STATUS;
    public static final String REMINDER = NIRules.CATEGORY_KEY_REMINDER;
    public static final String MAIL = NIRules.CATEGORY_KEY_MAIL;
    public static final String MESSAGE = NIRules.CATEGORY_KEY_MESSAGE;
    public static final String OTHERS = NIRules.CATEGORY_KEY_OTHERS;

    private WdjNotifyClassify(Context context) {
        NISDK.init(context);
        niRules = new NIRules();
        niFilter = new NIFilter(niRules);
        rulesUpdator = new NIRulesUpdator(new NIRulesUpdator.RuleConfiguration() {
            @Override
            public void onAppRuleUpdate(CustomRuleDBModel customRuleDBModel) {
                setClearContentDefault(customRuleDBModel);
            }

            @Override
            public void onCategoryRuleUpdate(CategoryDBModel categoryDBModel) {

            }
        });
    }

    public static WdjNotifyClassify getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new WdjNotifyClassify(context);
        }
        return mInstance;
    }

    // 识别一条通知, 转化成一个分类结果, 结果同步返回, 可能会耗时, 不要在主线程调用
    public FilterResult filter(StatusBarNotification sbn) {
        return niFilter.filter(sbn);
    }

    // 返回当前初始化状态, 可以在主线程调用
    public boolean isInitialized() {
        return niRules.isInitialized();
    }

    // 返回所有分类的列表, 需要初始化完成, 可以在主线程调用
    public List<NICategory> getAllCategories() {
        if (false == isInitialized()) {
            return null;
        }
        return niRules.getAllCategories();
    }

    // 查询指定包名对应的所有分类和设置, packageName 可以是 null, null 则会返回所有应用的分类和设置,
    // 要读取数据库，不可以在主线程调用
    private List<CustomRuleDBModel> queryCustomRules(String packageName) {
        if (false == isInitialized()) {
            return null;
        }
        return niRules.queryCustomRules(packageName);
    }

    //默认清理内容资讯
    private void setClearContentDefault(CustomRuleDBModel customRuleDBModel) {
        if (customRuleDBModel != null) {
            String categoryKey = customRuleDBModel.categoryKey;
            String packageName = customRuleDBModel.packageName;
            if (categoryKey.equals(NIRules.CATEGORY_KEY_CONTENT)) {
                updateCustomRule(packageName, categoryKey, NotificationPriority.SPAM);
            }
        }

    }

    /**
     * 排序:
     * 聊天--message
     * 邮件--mail
     * 提醒--reminder
     * 消费--shopping
     * 社交动态--social
     * 手机优化--optimization
     * 运行状态--status
     * 广告推广--promotion
     * 内容资讯--content
     * 其它--others
     */
    public List<CustomRuleDBModel> getCustomRuleDBModel(String packageName) {
        List<CustomRuleDBModel> modelList = new ArrayList<>();
        List<CustomRuleDBModel> ruleDBModels = queryCustomRules(packageName);
        if (ruleDBModels != null) {
            List<String> tags = getSortTags();
            for (String tag : tags) {
                for (CustomRuleDBModel model : ruleDBModels) {
                    if (model.categoryKey.equals(tag)) {
                        modelList.add(model);
                        break;
                    }
                }
            }

        }
        return modelList;
    }

    // 获取针对应用某个分类的重要性设置
    public NotificationPriority getCustomPriority(String packageName, String categoryKey) {
        if (false == isInitialized()) {
            return null;
        }
        return niRules.getCustomPriority(packageName, categoryKey);
    }

    // 获取分类，包含了分类的重要性
    public NICategory getCategory(String categoryKey) {
        return niRules.getCategory(categoryKey);
    }

    // 主动触发规则更新, 但是还是会参照规则更新间隔，如果不到时间就什么都不做, 可以在主线程调用
    public void sync() {
        rulesUpdator.sync();
    }

    // 获取上次规则更新成功的时间, 可以在主线程调用
    public long getLastSyncedTime() {
        return rulesUpdator.getLastSyncedTime();
    }

    // 获取规则更新间隔, 可以在主线程调用
    public long getSyncInterval() {
        return rulesUpdator.getSyncInterval();
    }

    // 设置规则更新间隔, 可以在主线程调用
    public void setSyncInterval(long interval) {
        rulesUpdator.setSyncInterval(interval);
    }

    // 设置对应应用对应分类的优先级, 会异步设置，可以在主线程调用
    public void updateCustomRule(String packageName, String categoryKey, NotificationPriority priority) {
        rulesUpdator.updateCustomRule(packageName, categoryKey, priority);
    }

    // 设置分类整体的优先级, 会异步设置可以在主线程调用
    public void updateCategory(String categoryKey, NotificationPriority priority) {
        rulesUpdator.updateCategory(categoryKey, priority);
    }

    public void setRuleEventCallback(NIRules.RuleEventCallback callback) {
        niRules.setRuleEventCallback(callback);
    }

    /**
     * 排序:
     * 聊天--message
     * 邮件--mail
     * 提醒--reminder
     * 消费--shopping
     * 社交动态--social
     * 手机优化--optimization
     * 运行状态--status
     * 广告推广--promotion
     * 内容资讯--content
     * 其它--others
     */
    private List<String> getSortTags() {
        List<String> tags = new ArrayList<>();
        tags.add(MESSAGE);
        tags.add(MAIL);
        tags.add(REMINDER);
        tags.add(SHOPPING);
        tags.add(SOCIAL);
        tags.add(OPTIMIZATION);
        tags.add(STATUS);
        tags.add(PROMOTION);
        tags.add(CONTENT);
        tags.add(OTHERS);
        return tags;
    }

    public int getCategoryByTagResID(String tag) {
        switch (tag) {
            case "function":
                return R.string.function;
            case "promotion":
                return R.string.promotion;
            case "ads":
                return R.string.ads;
            case "news":
                return R.string.news;
            case "recommend":
                return R.string.recommend;
            case "subscribe":
                return R.string.subscribe;
            case "tracking":
                return R.string.tracking;
            case "order":
                return R.string.order;
            case "finance":
                return R.string.finance;
            case "update":
                return R.string.update;
            case "battery":
                return R.string.battery;
            case "permission":
                return R.string.permission;
            case "storage":
                return R.string.storage;
            case "appupgrade":
                return R.string.appupgrade;
            case "status":
                return R.string.status;
            case "download":
                return R.string.download;
            case "warn":
                return R.string.warn;
            case "calendar":
                return R.string.calendar;
            case "weather":
                return R.string.weather;
            case "navi":
                return R.string.navi;
            case "roadcondition":
                return R.string.roadcondition;
            case "todo":
                return R.string.todo;
            case "game":
                return R.string.game;
            case "mail":
                return R.string.mail;
            case "message":
                return R.string.message;
            case "privateletter":
                return R.string.privateletter;
            case "unknown":
                return R.string.others;
            default:
                return R.string.others;
        }
    }

    public String getCategoryByTag(Context context, String tag) {
        return context.getString(getCategoryResID(tag));
    }

    public String getCategoryStringByTag(String tag) {
        switch (tag) {
            case "function":
            case "promotion":
            case "ads":
                return PROMOTION;
            case "news":
            case "recommend":
            case "subscribe":
                return CONTENT;
            case "tracking":
            case "order":
            case "finance":
                return SHOPPING;
            case "update":
                return SOCIAL;
            case "battery":
            case "permission":
            case "storage":
            case "appupgrade":
                return OPTIMIZATION;
            case "status":
            case "download":
            case "warn":
                return STATUS;
            case "calendar":
            case "weather":
            case "navi":
            case "roadcondition":
            case "todo":
            case "game":
                return REMINDER;
            case "mail":
                return MAIL;
            case "message":
            case "privateletter":
                return MESSAGE;
            case "unknown":
                return OTHERS;
            default:
                return OTHERS;
        }
    }

    public int getCategoryResID(String categoryKey) {
        switch (categoryKey) {
            case PROMOTION:
                return R.string.promotion;
            case CONTENT:
                return R.string.content;
            case SHOPPING:
                return R.string.shopping;
            case SOCIAL:
                return R.string.social;
            case OPTIMIZATION:
                return R.string.optimization;
            case STATUS:
                return R.string.status;
            case REMINDER:
                return R.string.reminder;
            case MAIL:
                return R.string.mail;
            case MESSAGE:
                return R.string.message;
            case OTHERS:
                return R.string.others;
            default:
                return R.string.others;
        }
    }

    /**
     * 根據不同的分類設置不同的圖片
     */
    public int getDrawableWithCategoryResID(int categoryResID) {
        switch (categoryResID) {
            case R.string.message:
                return R.drawable.wdj_message;
            case R.string.mail:
                return R.drawable.wdj_mail;
            case R.string.reminder:
                return R.drawable.wdj_reminder;
            case R.string.shopping:
                return R.drawable.wdj_shopping;
            case R.string.social:
                return R.drawable.wdj_social;
            case R.string.optimization:
                return R.drawable.wdj_optimization;
            case R.string.status:
                return R.drawable.wdj_status;
            case R.string.promotion:
                return R.drawable.wdj_promotion;
            case R.string.content:
                return R.drawable.wdj_content;
            case R.string.others:
                return R.drawable.wdj_others;
            default:
                return R.drawable.wdj_others;

        }
    }
}
