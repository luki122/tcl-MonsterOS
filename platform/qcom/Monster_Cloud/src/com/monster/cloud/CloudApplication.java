package com.monster.cloud;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.monster.cloud.imageloader.UnlimitedDiscCache;
import com.monster.cloud.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.tencent.qqpim.sdk.utils.QQPimUtils;
import com.tencent.QQPimSDK;

/**
 * Created by xiaobin on 16-10-11.
 */
public class CloudApplication extends Application {

    public static final String TAG = "CloudApplication";

    private static CloudApplication instance;

    private List<Activity> activityList;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i("MarketApplication", "onCreate()");
        QQPimSDK.init(getApplicationContext());
        instance = this;

        if (activityList == null) {
            activityList = new ArrayList<Activity>();
        } else {
            activityList.clear();
        }

        initImageLoader(this);

    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you
        // may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisk(true).build();
        File cacheDir = StorageUtils.getOwnCacheDirectory(context,
                "/TCLStore Download/Cache");
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).defaultDisplayImageOptions(defaultOptions)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCache(new UnlimitedDiscCache(cacheDir))
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCacheSize(100 * 1024 * 1024)
                .memoryCache(new LruMemoryCache(20 * 1024 * 1024))
                //.memoryCache(new WeakMemoryCache())
                .memoryCacheSize(20 * 1024 * 1024)
                // .writeDebugLogs() // Remove
                // for
                // release
                // app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    /**
     * 获取Application实例
     * @return
     */
    public static CloudApplication getInstance() {
        return instance;
    }

    /**
     * 加入管理Activity
     * @param activity
     */
    public void addActivity(Activity activity) {
        if (activityList != null) {
            activityList.add(activity);
        }
    }

    /**
     * 移除管理Activity
     * @param activity
     */
    public void removeActivity(Activity activity) {
        if (activityList != null) {
            activityList.remove(activity);
        }
    }

    /**
     * 退出App
     */
    public void exitApp() {
        if (activityList != null) {
            for (Activity activity : activityList) {
                if (activity != null && !activity.isFinishing()) {
                    activity.finish();
                }
            }
            activityList.clear();
        }
    }

}
