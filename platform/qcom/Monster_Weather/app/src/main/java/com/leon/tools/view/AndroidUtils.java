/* Copyright (C) 2016 Tcl Corporation Limited */
package com.leon.tools.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import cn.tcl.weather.WeatherCNApplication;

public class AndroidUtils {
    private final static int BMP_SIZE = 1024 * 1024;
    private final static int BMP_MAX_HEIGHT = 1920;
    private final static int BMP_MAX_WIDTH = 1080;

    private final static String TITLE = "AndroidUtils";
    private static boolean IsDebug = true;

    public static float dip2px(Context context, float dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return dpValue * density;
    }

    public static float px2dip(Context context, float pxValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return pxValue / density;
    }

    public static float px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return pxValue / fontScale;
    }

    public static float sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return spValue * fontScale;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // origin picture's width
        final int width = options.outWidth;
        final int height = options.outHeight;

        if (0 == reqWidth || reqWidth > BMP_MAX_WIDTH) {
            reqWidth = BMP_MAX_WIDTH;
        }

        if (0 == reqHeight || reqHeight > BMP_MAX_HEIGHT) {
            reqHeight = BMP_MAX_HEIGHT;
        }

        int inSampleSizeW = 1;
        int inSampleSizeH = 1;
        if (width > reqWidth && reqWidth > 0) {
            inSampleSizeW = Math.round((float) width / (float) reqWidth);
        }

        if (height > reqHeight && reqHeight > 0) {
            inSampleSizeH = Math.round((float) height / (float) reqHeight);
        }
        final int size = inSampleSizeW > inSampleSizeH ? inSampleSizeW : inSampleSizeH;
//        if(width* height * 4 > size * BMP_SIZE)// if bitmap is two big then change to rgb 565
//            options.inPreferredConfig = Bitmap.Config.RGB_565;
        return size;
    }

    public static int getBmpSize(Bitmap bitmap) {
        if (null == bitmap) {
            return 0;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {// API
            // 12
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static Bitmap decodeSampledBitmapFromResource(Context context, int resId, int reqWidth, int reqHeight) {
        return decodeSampledBitmapFromResource(context.getResources(), resId, reqWidth, reqHeight);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeBitmap(Resources res, int resId) {
        return decodeSampledBitmapFromResource(res, resId, 0, 0);
    }

    public static Bitmap decodeSampledBitmapFromPath(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }


    public static File copyFile(InputStream is, String filePath) {
        BufferedInputStream bis = new BufferedInputStream(is);
        FileOutputStream fos = null;
        File tmp = null;
        File file = null;
        try {
            tmp = createFile(filePath + ".tmp");
            fos = new FileOutputStream(tmp);
            byte[] data = new byte[1024];
            int length = 0;
            while ((length = bis.read(data)) != -1) {
                fos.write(data, 0, length);
                fos.flush();
            }
            file = new File(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (null != file) {
            tmp.renameTo(file);
            return file;
        }
        return null;
    }

    public static File createFile(String path) throws IOException {
        File file = new File(path);
        return createFile(file);
    }

    public static File createFile(File file) throws IOException {
        if (!file.exists()) {
            File pfile = file.getParentFile();
            if (!pfile.exists()) {
                pfile.mkdirs();
            }
            file.createNewFile();
        }
        return file;
    }

    public static boolean requestPermission(Activity activity, int requestCode, String... permissions) {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> requestPermissions = new ArrayList<>(permissions.length);
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions.add(permission);
                }
            }

            if (requestPermissions.size() > 0) {
                ActivityCompat.requestPermissions(activity, requestPermissions.toArray(new String[requestPermissions.size()]), requestCode);
                return true;
            }
        }
        return false;
    }


    public static void sendMessageCallback(int requestCode, String callback, int[] grantResults) {
        Message msg = Message.obtain();
        msg.what = requestCode;
        msg.obj = grantResults;
        WeatherCNApplication.getWeatherCnApplication().sendMessage(callback, msg);
    }

    public static boolean grantedPermission(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    /**
     * check network is useable
     *
     * @param context
     * @return
     */
    public static boolean netWorkUseable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null) {
            return manager.getBackgroundDataSetting();
        }
        return false;
    }
}
