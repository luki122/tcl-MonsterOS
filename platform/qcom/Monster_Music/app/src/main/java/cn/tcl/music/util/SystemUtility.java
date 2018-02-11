package cn.tcl.music.util;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import cn.tcl.music.R;
import cn.tcl.music.app.MusicApplication;
import java.io.File;
import java.lang.reflect.Field;
import java.text.NumberFormat;

public class SystemUtility {
    private static String TAG="SystemUtility";
    private static int screenWidth;
    private static int screenHeight;
    private static float density;

    public enum NetWorkType {
        none, mobile, wifi
    }

    public static boolean GTE_HC;
    public static boolean GTE_ICS;
    public static boolean PRE_HC;

    private static Boolean _hasCamera = null;
    private static Boolean _isTablet = null;
    private static Integer _loadFactor = null;

    static {
        GTE_ICS = Build.VERSION.SDK_INT >= 14;
        GTE_HC = Build.VERSION.SDK_INT >= 11;
        PRE_HC = Build.VERSION.SDK_INT < 11;
    }

    private static void setScreenInfo() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) MusicApplication.getApp().getSystemService(
                Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        density = dm.density;
    }

    public static int getScreenWidth() {
        if (screenWidth == 0) {
            setScreenInfo();
        }
        return screenWidth;
    }

    public static int getScreenHeight() {
        if (screenHeight == 0) {
            setScreenInfo();
        }
        return screenHeight;
    }

    public static int[] getRealScreenSize(Activity activity) {
        int[] size = new int[2];
        int screenWidth = 0, screenHeight = 0;
        WindowManager w = activity.getWindowManager();
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        // since SDK_INT = 1;
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
            try {
                screenWidth = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
                screenHeight = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
            } catch (Exception ignored) {
            }
        }
        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
                screenWidth = realSize.x;
                screenHeight = realSize.y;
            } catch (Exception ignored) {
            }
        }
        size[0] = screenWidth;
        size[1] = screenHeight;
        return size;
    }

    public static int getTitleBarHeight(Activity activity) {
        int contentTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        // statusBarHeight是上面所求的状态栏的高度
        int titleBarHeight = contentTop - getStatusBarHeight(activity);

        return titleBarHeight;
    }

    public static float getDensity() {
        if (density == 0.0f) {
            setScreenInfo();
        }
        return density;
    }

    public static final boolean hasCamera() {
        PackageManager pckMgr = MusicApplication.getApp().getPackageManager();
        boolean flag = pckMgr.hasSystemFeature("android.hardware.camera.front");
        boolean flag1 = pckMgr.hasSystemFeature("android.hardware.camera");
        return flag || flag1;
    }

    public static boolean hasSDCard() {
        boolean mHasSDcard = false;
        mHasSDcard = Environment.MEDIA_MOUNTED.endsWith(Environment.getExternalStorageState());

        return mHasSDcard;
    }

    public static String getSdcardPath() {
        if (hasSDCard()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return "/sdcard/";
    }

    private static boolean sdcardCanWrite() {
        return Environment.getExternalStorageDirectory().canWrite();
    }

    public static boolean hasSdcardAndCanWrite() {
        return hasSDCard() && sdcardCanWrite();
    }

    /**
     * 获取SDCARD的可用大小,单位字节
     *
     * @return
     */
    public long getSdcardtAvailableStore() {

        if (hasSdcardAndCanWrite()) {
            String path = getSdcardPath();
            if (path != null) {
                StatFs statFs = new StatFs(path);

                long blocSize = statFs.getBlockSize();
                long availaBlock = statFs.getAvailableBlocks();
                return availaBlock * blocSize;
            }
        }

        return 0;
    }

    public static NetWorkType getNetworkType() {

        ConnectivityManager connMgr = (ConnectivityManager) MusicApplication.getApp().getSystemService(
                Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null) {
            switch (networkInfo.getType()) {
                case ConnectivityManager.TYPE_MOBILE:
                    return NetWorkType.mobile;
                case ConnectivityManager.TYPE_WIFI:
                    return NetWorkType.wifi;
            }
        }

        return NetWorkType.none;
    }

    /**
     * mac地址
     *
     * @param
     * @return
     */
    public static String getMacAddress() {
        WifiManager wifiManager = (WifiManager) MusicApplication.getApp().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getMacAddress() != null) {
            return wifiInfo.getMacAddress().replace(":", "");
        } else {
            return "0022f420d03f";
        }
    }

    public static String getUDPIP() {
        Context context = MusicApplication.getApp();

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifi.getDhcpInfo();
        int IpAddress = dhcpInfo.ipAddress;
        int subMask = dhcpInfo.netmask;
        return transformIp((~subMask) | IpAddress);
    }

    private static String transformIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    public static String getIP() {
        Context context = MusicApplication.getApp();

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return transformIp(wifi.getConnectionInfo().getIpAddress());
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            LogUtil.e(TAG, "Exception e: " + e.getMessage());
        }
        return "";
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            LogUtil.e(TAG, "Exception e: " + e.getMessage());
        }
        return 0;
    }

    public static String getPackage(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            LogUtil.e(TAG, "Exception e: " + e.getMessage());
        }
        return "";
    }

    public static void scanPhoto(File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        MusicApplication.getApp().sendBroadcast(intent);
    }

    public static void hideSoftInput(View paramEditText) {
        ((InputMethodManager) MusicApplication.getApp().getSystemService("input_method")).hideSoftInputFromWindow(
                paramEditText.getWindowToken(), 0);
    }

    public static void showKeyBoard(final View paramEditText) {
        paramEditText.requestFocus();
        paramEditText.post(new Runnable() {
            @Override
            public void run() {
                ((InputMethodManager) MusicApplication.getApp().getSystemService("input_method")).showSoftInput(
                        paramEditText, 0);
            }
        });
    }

    public static int getScreenHeight(Activity paramActivity) {
        Display display = paramActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static int getStatusBarHeight(Activity paramActivity) {
        Rect localRect = new Rect();
        paramActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        return localRect.top;

    }

    public static int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return MusicApplication.getApp().getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            LogUtil.e(TAG, "Exception e: " + e.getMessage());
        }
        return 0;
    }

    // below status bar,include actionbar, above softkeyboard
    public static int getAppHeight(Activity paramActivity) {
        Rect localRect = new Rect();
        paramActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        return localRect.height();
    }

    public static boolean isKeyBoardShow(Activity paramActivity) {
        int height = SystemUtility.getScreenHeight(paramActivity) - SystemUtility.getStatusBarHeight(paramActivity)
                - SystemUtility.getAppHeight(paramActivity);
        return height != 0;
    }

    /**
     * 用于获取当前手机采用的是哪个资源文件 layout, layout_hdpi, layout_xhdpi, layout_xxhdpi
     *
     * @author jerry.liu
     *
     */
    public enum PhoneResource {
        layout_default,
        layout_hdpi, layout_xhdpi, layout_xxhdpi,
    }

    /**
     * //加载系统默认存在的ic_launcher图片 根据获取到的图片的像素大小来判断当前手机取的是哪一个资源文件夹
     *
     * @return
     */
    public static PhoneResource getPhoneResource() {

        Bitmap bitmap = BitmapFactory
                .decodeResource(MusicApplication.getApp().getResources(), R.drawable.app_music);

        if (bitmap.getWidth() == 144) {
            return PhoneResource.layout_xxhdpi;
        } else if (bitmap.getWidth() == 96) {
            return PhoneResource.layout_xhdpi;
        } else if (bitmap.getWidth() == 72) {
            return PhoneResource.layout_hdpi;
        }
        // 72像素一下的都加载默认布局
        else {
            return PhoneResource.layout_default;
        }

    }

    public static boolean gotoGoogleMarket(Activity activity, String pck) {
        try {
            Intent intent = new Intent();
            intent.setPackage("com.android.vending");
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + pck));
            activity.startActivity(intent);
            return true;
        } catch (Exception e) {
            LogUtil.e(TAG, "Exception e: " + e.getMessage());
            return false;
        }
    }

    public static boolean isPackageExist(String pckName) {
        try {
            PackageInfo pckInfo = MusicApplication.getApp().getPackageManager().getPackageInfo(pckName, 0);
            if (pckInfo != null) {
                return true;
            }
        } catch (NameNotFoundException e) {
            LogUtil.e(TAG, "Exception e: " + e.getMessage());
        }
        return false;
    }

    public static void hideAnimatedView(View view) {
        if (PRE_HC && view != null) {
            view.setPadding(view.getWidth(), 0, 0, 0);
        }
    }

    public static boolean isLandscape() {
        return MusicApplication.getApp().getResources().getConfiguration().orientation == 2;
    }

    public static boolean isPortrait() {
        return MusicApplication.getApp().getResources().getConfiguration().orientation != 1;
    }

    public static boolean isTablet() {
        if (_isTablet == null) {
            _isTablet = Boolean
                    .valueOf((0xf & MusicApplication.getApp().getResources().getConfiguration().screenLayout) >= 3);
        }
        return _isTablet.booleanValue();
    }

    public static boolean isZhCN() {
        String lang = MusicApplication.getApp().getResources().getConfiguration().locale.getCountry();
        return lang.equalsIgnoreCase("CN");
    }

    public static String percent(double p1, double p2) {
        String str;
        double p3 = p1 / p2;
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);
        str = nf.format(p3);
        return str;
    }

    @SuppressWarnings("deprecation")
    public static void copyTextToBoard(String string) {
        if (TextUtils.isEmpty(string)) {
            return;
        }
        ClipboardManager clip = (ClipboardManager) MusicApplication.getApp().getSystemService(
                Context.CLIPBOARD_SERVICE);
        clip.setText(string);

    }

    protected boolean hasActionBar() {
        return Build.VERSION.SDK_INT >= 11;
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }

    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5F);
    }

    public static int px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int)(pxValue / fontScale + 0.5F);
    }

    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int)(spValue * fontScale + 0.5F);
    }

    public static int getDialogW(Context aty) {
        new DisplayMetrics();
        DisplayMetrics dm = aty.getResources().getDisplayMetrics();
        int w = dm.widthPixels - 100;
        return w;
    }

    public static void setTextViewSize(TextView tv, int spSize){
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
    }

}
