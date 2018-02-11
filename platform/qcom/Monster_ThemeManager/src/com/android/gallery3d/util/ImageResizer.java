/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ImageResizer extends ImageWorker {
    private static final String TAG = "ImageResizer";
    protected int mImageWidth;
    protected int mImageHeight;
    public static int sCacheWidth;
    public static int sCacheHeight;
    public boolean mIsThumb;
    public static boolean sIsThumb;

    private Context mContext;

    public ImageResizer(Context context, int imageWidth, int imageHeight) {
        super(context);
        mContext = context;
        setImageSize(imageWidth, imageHeight);
        setImageThumb(false);
    }

    public ImageResizer(Context context, int imageSize) {
        super(context);
        mContext = context;
        setImageSize(imageSize);
        setImageThumb(false);
    }

    public ImageResizer(Context context, int imageWidth, int imageHeight, boolean isThumb) {
        super(context);
        mContext = context;
        setImageSize(imageWidth, imageHeight);
        setImageThumb(isThumb);
    }

    public ImageResizer(Context context, int imageSize, boolean isThumb) {
        super(context);
        mContext = context;
        setImageSize(imageSize);
        setImageThumb(isThumb);
    }

    public void setImageSize(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
        sCacheWidth = width;
        sCacheHeight = height;
    }

    public void setImageSize(int size) {
        setImageSize(size, size);
    }

    public void setImageThumb(boolean isThumb) {
        mIsThumb = isThumb;
        sIsThumb = isThumb;
    }

    private Bitmap processBitmap(int resId) {
        return decodeSampledBitmapFromResource(mResources, resId, mImageWidth, mImageHeight, getImageCache());
    }

    @Override
    protected Bitmap processBitmap(Object data) {
    	if(data instanceof InputStream){
    		return decodeSampledBitmap((InputStream)data, mImageWidth, mImageHeight, getImageCache());
    	}else{
    		return decodeSampledBitmapFromDescriptor(String.valueOf(data), mImageWidth, mImageHeight, getImageCache());
    	}
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth,
            int reqHeight, ImageCache cache) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight,
            ImageCache cache) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }
    
    public static Bitmap decodeSampledBitmap(InputStream inputStream, int reqWidth, int reqHeight,
            ImageCache cache) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        Bitmap bm = null;
        try {
            if(inputStream!=null) {
            	bm = BitmapFactory.decodeStream(inputStream,null,options);
            }
        } catch (Exception e) {
        	Log.d("bitmap","decodeStream catch exception-->"+e);
            e.printStackTrace();
        } finally{ 
            if(inputStream!=null) {
                try {
                	inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bm;
    }
    
    
    public static Bitmap decodeSampledBitmapFromDescriptor(String filename, int reqWidth, int reqHeight,
            ImageCache cache) {

    	
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        File file = new File(filename);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(fis!=null){
        	try {
				BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        options.inJustDecodeBounds = false;
        Bitmap bm = null;
        try {
            if(fis!=null) bm = BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{ 
            if(fis!=null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bm;
    }
    
    public static Bitmap decodeSampledBitmapFromStream(InputStream input,int reqWidth, int reqHeight) throws IOException{

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        	BitmapFactory.decodeStream(input, null, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;


        options.inJustDecodeBounds = false;
        Bitmap bm = null;
        try {
            if(input!=null) bm = BitmapFactory.decodeStream(input, null, options);
        } finally{ 
            if(input!=null) {
                try {
                	input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bm;
    }
    
    
    public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth,
            int reqHeight, boolean isThumb, ImageCache cache) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        options.inSampleSize = calculateInSampleSize(options, sCacheWidth, sCacheHeight);
        if (isThumb) {
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        } else {
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inPurgeable = true;
            options.inInputShareable = true;
        }
        options.inDither = false;
        options.inJustDecodeBounds = false;


        if (Utils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);	//bug-5577 java.lang.OutOfMemoryError
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
        options.inMutable = true;

        if (cache != null) {
            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

            if (inBitmap != null) {
                options.inBitmap = inBitmap;
            }
        }
    }


    
    /**
     * 计算抽样参数
     * @param options {@link BitmapFactory.Options}
     * @param reqWidth 请求宽度
     * @param reqHeight 请求高度
     * @return 抽样参数
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
    
        if (height > reqHeight || width > reqWidth) {
    
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
}
