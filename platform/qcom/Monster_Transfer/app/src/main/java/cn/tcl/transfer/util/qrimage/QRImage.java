/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util.qrimage;

import java.util.Hashtable;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRImage {
    private static int QR_WIDTH = 600, QR_HEIGHT = 600;
    private static final int PADDING_SIZE_MIN = 0;

    public static Bitmap createQRImage(String info){
        Bitmap bitmap = null;
        try
        {
            if (TextUtils.isEmpty(info))
            {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = new QRCodeWriter().encode(info, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            boolean isFirstBlackPoint = false;
            int startX = 0;
            int startY = 0;
            for (int y = 0; y < QR_HEIGHT; y++)
            {
                for (int x = 0; x < QR_WIDTH; x++)
                {
                    if (bitMatrix.get(x, y))
                    {
                        if (isFirstBlackPoint == false)
                        {
                            isFirstBlackPoint = true;
                            startX = x;
                            startY = y;
                        }
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    }
                    else
                    {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }
                }
            }
            bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            if (startX <= PADDING_SIZE_MIN) return bitmap;

            int x1 = startX - PADDING_SIZE_MIN;
            int y1 = startY - PADDING_SIZE_MIN;
            if (x1 < 0 || y1 < 0) return bitmap;

            int w1 = QR_WIDTH - x1 * 2;
            int h1 = QR_HEIGHT - y1 * 2;

            Bitmap bitmapQR = Bitmap.createBitmap(bitmap, x1, y1, w1, h1);
            return bitmapQR;
        }catch (Exception e){
            e.printStackTrace();
        }
        return bitmap;
    }
}
