package com.morpho.utils.multimedia;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.location.Location;
import android.media.ExifInterface;
import android.text.format.DateFormat;


public class JpegHandler {

    /**
     * 画像の回転角度
     */
    public static final int ROTATION_NORMAL =   0;
    public static final int ROTATION_90     =  90;
    public static final int ROTATION_180    = 180;
    public static final int ROTATION_270    = 270;

    /**
     * Bitmap を JPEGファイル形式で保存
     * 
     * @param bitmap         保存対象 Bitmap
     * @param filePath       保存先ファイルパス名
     * @param encodeQuality  JPEGのエンコード品質
     * @throws IOException
     */
    public static void compressBitmap(Bitmap bitmap, String filePath, int encodeQuality) throws IOException {
        // 書き出し先の FileOutputStream
        FileOutputStream fileOutput = null;

        // Bitmap から JPEG を生成し、バイト配列に変換
        ByteArrayOutputStream byte_os = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, encodeQuality, byte_os);
        byte_os.flush();
        byte[] jpegByteArray = byte_os.toByteArray();
        byte_os.close();
        
        // バイト配列をファイルとして出力
        fileOutput = new FileOutputStream(filePath);
        fileOutput.write(jpegByteArray, 0, jpegByteArray.length);
        fileOutput.flush();
        fileOutput.close();
    }

    /**
     * YVU420Semiplanar を JPEGファイル形式で保存
     * 
     * @param data           YVU420Semiplanarデータ配列
     * @param width          画像の幅
     * @param height         画像の高さ
     * @param filePath       保存先ファイルパス名
     * @param encodeQuality  JPEGのエンコード品質
     * @throws IOException
     */
    public static void compressYVU420ByteBuffer(ByteBuffer data, int width, int height, String filePath, int encodeQuality) throws IOException {

        byte[] raw = new byte[data.capacity()];
        data.get(raw);

        FileOutputStream fos = new FileOutputStream(new File(filePath));
        ByteArrayOutputStream byte_os = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage (raw, ImageFormat.NV21, width, height, null); 
        yuv.compressToJpeg(new Rect(0, 0, width, height), encodeQuality, fos);
        fos.close();

        raw = null;
        data.rewind();
    }

    /**
     *  JPEGファイルをBitmapにデコード
     * 
     * @param filePath       Jpegファイルパス名
     * @param max_memory     最大メモリサイズ
     */
    public static Bitmap decodeFile(String filepath, int max_memory) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        /* オリジナル画像のサイズを取得 */
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, opt);
        int dst_w = opt.outWidth;
        int dst_h = opt.outHeight;
        if (max_memory > 0) {
            while (true) {
                if (max_memory > dst_w * dst_h * 4) {
                    break;
                }
                dst_w = dst_w >> 1;
                dst_h = dst_h >> 1;
            }
        }
        /* スケールを算出 */
        int scaleW = opt.outWidth / dst_w;
        int scaleH = opt.outHeight / dst_h;
        int sampleSize = Math.max(scaleW, scaleH);
        opt.inSampleSize = sampleSize;
        opt.inJustDecodeBounds = false;
        /* 縮小デコード */
        return BitmapFactory.decodeFile(filepath, opt);
    }
    
    /**
     * JPEGファイルのサイズを取得
     * 
     * @param filePath  保存先ファイルパス名
     * @param width     幅
     * @param height    高さ
     */
    public static void getImageSize(String filepath, int[] width, int[] height) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, opt);
        width[0] = opt.outWidth;
        height[0] = opt.outHeight;
    }

    /**
     * JPEGバイトデータ列をファイルに保存
     * 
     * @param image     JPEGバイトデータ列
     * @param filePath  保存先ファイルパス名
     * @throws IOException
     */
    public static void saveAsFile(byte[] image, String filePath) throws IOException {
        FileOutputStream fileStream = null;

        fileStream = new FileOutputStream(filePath);
        fileStream.write(image);
        fileStream.flush();
        fileStream.close();
    }

    /**
     * JPEG の Exif に情報追記
     * 
     * @param filePath    JPEGファイルパス
     * @param location    位置情報
     * @param orientation 画像の回転情報(単位:degree)
     */
    public static void setExifData(String filePath, Location location, int orientation) {
        setExifData(filePath, location, orientation, null);
    }
    public static void setExifData(String filePath, Location location, int orientation, String externalJson) {
        if (filePath == null) {
            return;
        }
        
        try {
            ExifInterface exif = new ExifInterface(filePath);

            long dateTaken = System.currentTimeMillis();
            String nowTime = DateFormat.format("yyyy:MM:dd kk:mm:ss", dateTaken).toString();

            exif.setAttribute(ExifInterface.TAG_DATETIME, nowTime);
            exif.setAttribute("DateTimeOriginal", nowTime);
            exif.setAttribute("DateTimeDigitized", nowTime);
            exif.setAttribute(ExifInterface.TAG_MAKE, android.os.Build.MANUFACTURER);
            exif.setAttribute(ExifInterface.TAG_MODEL, android.os.Build.MODEL);
            
            // 回転情報を設定
            int orientationRotate = ExifInterface.ORIENTATION_NORMAL;
            switch (orientation) {
            case 90:
                orientationRotate = ExifInterface.ORIENTATION_ROTATE_90;
                break;
            case 180:
                orientationRotate = ExifInterface.ORIENTATION_ROTATE_180;
                break;
            case 270:
                orientationRotate = ExifInterface.ORIENTATION_ROTATE_270;
                break;
            }
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, "" + orientationRotate);
            
            // 位置情報が与えられたときは設定
            if (location != null) {
                double latitude  = location.getLatitude();     // 緯度
                double longitude = location.getLongitude();    // 経度

                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,      locationValueToString(latitude));
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,  latitudeValueToNorS(latitude));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,     locationValueToString(longitude));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitudeValueToEorW(longitude));
            }
            if (externalJson != null) {
                exif.setAttribute("UserComment", externalJson);
            }
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Double型の数値から "34/1,59/1,59/1" のような文字列を生成
     */
    private static String locationValueToString(double value){

        String result="";  

        int degrees, minutes, seconds;

        // 整数部分を degrees として取り出す
        Double d = new Double(value);
        degrees = d.intValue();
        result += degrees + "/1,";

        // minutes 用に value から degrees を引いて60倍する
        value -= degrees;
        value *= 60;

        // 整数部分を minutes として取り出す
        d = new Double(value);
        minutes = d.intValue();
        result += minutes + "/1,";

        // seconds 用に、value から minutes を引いて60倍する
        value -= minutes;
        value *= 60;

        // 整数部分を seconds として取り出す
        d = new Double(value);
        seconds = d.intValue();
        result += seconds + "/1";

        return result;
    }

    /**
     * 北緯(N) or 南緯(S) を示す文字列を返す
     */
    private static String latitudeValueToNorS(double value) {
        String result;

        if (value > 0) {
            result = "N";
        } else {
            result = "S";
        }
        
        return result;
    }

    /**
     * 東経(E) or 西経(W) を示す文字列を返す
     */
    private static String longitudeValueToEorW(double value) {
        String result;

        if (value > 0) {
            result = "E";
        } else {
            result = "W";
        }

        return result;
    }
}
