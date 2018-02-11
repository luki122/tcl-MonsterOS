package cn.tcl.music.view.image;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.webkit.URLUtil;

import java.io.IOException;

import cn.tcl.music.view.image.ImageResizer;
import cn.tcl.music.common.cache.CacheController;
import cn.tcl.music.util.LogUtil;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images fetched from a URL.
 */
public class ImageFetcher extends ImageResizer {
    private static final String TAG = "ImageFetcher";

    /**
     * Initialize providing a target image width and height for the processing images.
     *
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageFetcher(Context context, int imageWidth, int imageHeight, int emptyImageResource) {
        super(context, imageWidth, imageHeight, emptyImageResource);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageSize
     */
    public ImageFetcher(Context context, int imageSize, int emptyImageResource) {
        super(context, imageSize, emptyImageResource);
    }


    /**
     * The main process method, which will be called by the ImageWorker in the AsyncTask background
     * thread.
     *
     * @param data The data to load the bitmap, in this case, a regular http URL
     * @return The downloaded and resized bitmap
     */
    private Bitmap processBitmap(String data) {
        Bitmap bitmap = null;
        if (TextUtils.isEmpty(data)) {
            return bitmap;
        }

        if (URLUtil.isNetworkUrl(data)) {
            bitmap = processHttpBitmap(data, mImageWidth, mImageHeight);
        }
        else {
            bitmap = decodeSampledBitmapFromFile(data, mImageWidth, mImageHeight, mCacheController.getImageCache());
        }
        return bitmap;
    }

    /**
     * The main process method, which will be called by the ImageWorker in the AsyncTask background
     * thread.
     *
     * @param data The data to load the bitmap, in this case, a regular http URL
     * @return The downloaded and resized bitmap
     */
    private Bitmap processBitmap(String data, int imageWidth, int imageHeight) {
        Bitmap bitmap = null;
        if (TextUtils.isEmpty(data))
        {
            return null;
        }

        if (URLUtil.isNetworkUrl(data)) {
            bitmap = processHttpBitmap(data, imageWidth, imageHeight);
        }
        else {
            bitmap = decodeSampledBitmapFromFile(data, mImageWidth, mImageHeight, mCacheController.getImageCache());
        }
        return bitmap;
    }

    private Bitmap processHttpBitmap(String data, int imageWidth, int imageHeight) {
        CacheController.FileHttpObject fileHttpObject = mCacheController.getOrDownloadHttpImage(data);

        Bitmap bitmap = null;
        if (fileHttpObject.fileDescriptor != null) {
            bitmap = decodeSampledBitmapFromDescriptor(fileHttpObject.fileDescriptor, imageWidth,
                    imageHeight, mCacheController.getImageCache());
        }

        if (fileHttpObject.fileInputStream != null) {
            try {
                fileHttpObject.fileInputStream.close();
            } catch (IOException e) {}
        }
        return bitmap;
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        if (data instanceof Integer) {
            return super.processBitmap(data);
        }
        return processBitmap(String.valueOf(data));
    }


    @Override
    protected Bitmap processMosaicBitmap(Object[] dataArray, int numberOfTiles) {

        final int dataSize = dataArray.length;
        if (dataSize == 0) {
            return null;
        }
        if (numberOfTiles == 0) {
            return null;
        }

        final int tilesPerSide = numberOfTiles >> 1;//等下
        int spaceBetweenSquares = (int) (2 * mResources.getDisplayMetrics().density);

        final int rectHeight = (mImageHeight - spaceBetweenSquares) / tilesPerSide;
        final int rectWidth = (mImageWidth - spaceBetweenSquares) / tilesPerSide;


        Bitmap finalBitmap;
        try {
            finalBitmap = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            // TCT: Why there will be in here
            //      Whether the bitmap wasn't released in time when it will not be used anymore
            //      Mixvibe, please have a check about it?
            finalBitmap = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_4444);
        }
        Canvas canvas = new Canvas(finalBitmap);

        int k = 0;
        for (int j = 0; j < tilesPerSide; j++)
        {
            for (int i = 0; i < tilesPerSide; i++)
            {
                Bitmap bmp = null;
                if (k >= dataSize) {
                    k = dataSize - 1;
                }
                if (dataArray[k] == null)
                {
                    if (k >= mEmptyImageResources.length)
                    {
                        bmp = processBitmap(mEmptyImageResources[0]);
                    }
                    else
                    {
                        bmp = processBitmap(mEmptyImageResources[k]);
                    }
                }
                else
                {
                    bmp = processBitmap(String.valueOf(dataArray[k]), rectWidth, rectHeight);
                }
                if (bmp == null) {
                    if (k >= mEmptyImageResources.length)
                    {
                        bmp = processBitmap(mEmptyImageResources[0]);
                    }
                    else
                    {
                        bmp = processBitmap(mEmptyImageResources[k]);
                    }
                }

                if (bmp != null)
                {

                    try{
                        Bitmap scaledBmp = Bitmap.createScaledBitmap(bmp, rectWidth, rectHeight, true);
                        canvas.drawBitmap(scaledBmp, (rectWidth + spaceBetweenSquares) * i, (rectHeight + spaceBetweenSquares) * j, null);
                    }catch (Exception e){
                        LogUtil.e(TAG, "message : " + e.getMessage());
                    }

                }
                k++;
            }
        }

        return finalBitmap;

    }


    public void setDeckLoadingImage(int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(mResources, resId, options);
            super.setLoadingImage(bitmap);
        } catch (Exception e) {
            LogUtil.e(TAG, "Exception : " + e.getMessage());
        }
    }

    public Bitmap getArtWorkBitmap(String data) {
        Bitmap bitmap = null;
        if (TextUtils.isEmpty(data)) {
            return bitmap;
        }

        if (URLUtil.isNetworkUrl(data)) {
            bitmap = processHttpBitmap(data, mImageWidth, mImageHeight);
        }
        else {
            bitmap = decodeSampledBitmapFromFile(data, mImageWidth, mImageHeight, mCacheController.getImageCache());
        }
        return bitmap;
    }
}
