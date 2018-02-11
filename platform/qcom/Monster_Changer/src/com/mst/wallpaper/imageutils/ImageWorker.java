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

package com.mst.wallpaper.imageutils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.mst.wallpaper.R;
import com.android.gallery3d.exif.ExifInterface;
import com.mst.wallpaper.widget.CropImageView;

public abstract class ImageWorker {
    private static final String TAG = "ImageWorker";
    private static final int FADE_IN_TIME = 200;

    private ImageCache mImageCache;
    private ImageCache.ImageCacheParams mImageCacheParams;
    private Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = false;
    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private boolean mCropNail = false;
    private final Object mPauseWorkLock = new Object();

    protected Resources mResources;

    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;
    
    private static final int MESSAGE_MEMORY_CLEAR = 4;
    public ImageLoaderCallback mImageLoaderCallback;
    private String updateCache = null;

    
    
    protected ImageWorker(Context context) {
        mResources = context.getResources();
    }

    public void loadImage(Object data, ImageView imageView) {
        loadImage(data, imageView, -1);
    }
    

    public void loadImage(Object data, ImageView imageView, int position) {

        if (data == null || imageView == null) {
            if (mImageLoaderCallback != null && position > -1) {
                mImageLoaderCallback.onImageLoadFailed(position);
            }
            return;
        }
        
        BitmapDrawable value = null;
        
        if (mImageCache != null) {
            value = mImageCache.getBitmapFromMemCache(getCacheName(String.valueOf(data)));
        }
        
        if (value != null) {
            imageView.setImageDrawable(value);
            if (mImageLoaderCallback != null && position > -1) {
                mImageLoaderCallback.onImageLoad(true, position);
            }
        } else if (cancelPotentialWork(data, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            if (position > -1) {
                task.setPosition(position);
            }
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mResources, mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, data);
        }
    }

    public void loadImage(Object data, CropImageView imageView, int position) {

        if (data == null || imageView == null) {
            if (mImageLoaderCallback != null && position > -1) {
                mImageLoaderCallback.onImageLoadFailed(position);
            }
            return;
        }
        
        float rotation = getRotationFromExif(imageView.getContext(),String.valueOf(data));
        
        BitmapDrawable value = null;
        if (mImageCache != null) {
            value = mImageCache.getBitmapFromMemCache(getCacheName(String.valueOf(data)));
        }
        if (value != null) {
            imageView.setImageDrawable(value, rotation);
            if (mImageLoaderCallback != null && position > -1) {
                mImageLoaderCallback.onImageLoad(true, position);
            }
            Log.d(TAG, "memCache");
        } else if (cancelPotentialWork(data, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            if (position > -1) {
                task.setPosition(position);
            }
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mResources, mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable, rotation);
            Log.d(TAG, "asyncDrawable");
            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, data);
        }
    }
    
    public void setLoadingImage(Bitmap bitmap) {
        mLoadingBitmap = bitmap;
    }

    public void setLoadingImage(int resId) {
    	InputStream is = mResources.openRawResource(resId);
    	try {
            mLoadingBitmap = BitmapFactory.decodeStream(is);
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
		}
        try {
            is.close();
        } catch (Exception e) {
        }
    }

    public void addImageCache(FragmentManager fragmentManager,
            ImageCache.ImageCacheParams cacheParams) {
        mImageCacheParams = cacheParams;
        mImageCache = ImageCache.getInstance(fragmentManager, mImageCacheParams);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    public void addImageCache(Activity activity, String diskCacheDirectoryName) {
        mImageCacheParams = new ImageCache.ImageCacheParams(activity, diskCacheDirectoryName);
        mImageCacheParams.setMemCacheSizePercent(0.2f);
        mImageCache = ImageCache.getInstance(activity.getFragmentManager(), mImageCacheParams);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }
    
    public void addImageCache(Activity activity, String diskCacheDirectoryName, String updateName) {
    	updateCache = updateName;
      mImageCacheParams = new ImageCache.ImageCacheParams(activity, diskCacheDirectoryName);
      mImageCacheParams.setMemCacheSizePercent(0.2f);
      mImageCache = ImageCache.getInstance(activity.getFragmentManager(), mImageCacheParams);
      new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }
    
    
	public void removeImageCache(Context context, String path) {
		if (mImageCache != null) {
			if (mImageCache.hasMemKey(path)) {
				mImageCache.removeMemCache(path);
			}
		}
	}
	
	public void setCropNail(boolean cropNail) {
        mCropNail = cropNail;
    }
	
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    protected abstract Bitmap processBitmap(Object data);
    protected ImageCache getImageCache() {
        return mImageCache;
    }

    public static void cancelWork(ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
        }
    }

    public static boolean cancelPotentialWork(Object data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.data;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private class BitmapWorkerTask extends AsyncTask<Object, Void, BitmapDrawable> {
        private Object data;
        private final WeakReference<ImageView> imageViewReference;
        int mPosition = -1;
        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected BitmapDrawable doInBackground(Object... params) {

            data = params[0];
            final String dataString = String.valueOf(data);
            Bitmap bitmap = null;
            RecyclingBitmapDrawable drawable = null;

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
            	Log.d(TAG, mPauseWork+"==BitmapWorkerTask: "+isCancelled());
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {}
                }
            }
            String cacheDataString = getCacheName(dataString);
            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                bitmap = mImageCache.getBitmapFromDiskCache(cacheDataString);
            }
            if (bitmap == null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
            	try{
                bitmap = processBitmap(data);
            	}catch(Exception e){
            		
            	}
            }
            if (bitmap != null) {
                    drawable = new RecyclingBitmapDrawable(mResources, bitmap);

                if (mImageCache != null) {
                    mImageCache.addBitmapToCache(cacheDataString, drawable);
                }
            }else {
            	bitmap = processBitmap(data);
            	drawable = new RecyclingBitmapDrawable(mResources, bitmap);
            	if (mImageCache != null) {
                    mImageCache.addBitmapToCache(cacheDataString, drawable);
                }
			}
            return drawable;
        }

        @Override
        protected void onPostExecute(BitmapDrawable value) {
            if (isCancelled() || mExitTasksEarly) {
                value = null;
            }
            
            if(value instanceof RecyclingBitmapDrawable){
            	final RecyclingBitmapDrawable verifyBitmap = (RecyclingBitmapDrawable)value;
            	if(!verifyBitmap.decodeSuccess()){
            		 if (mImageLoaderCallback != null && mPosition > -1) {
                         mImageLoaderCallback.onImageLoadFailed(mPosition);
                     }
            	}
            }

            final ImageView imageView = getAttachedImageView();
            if (value != null && imageView != null) {
                setImageDrawable(imageView, value);
                if (mImageLoaderCallback != null && mPosition > -1) {
                    mImageLoaderCallback.onImageLoad(true, mPosition);
                }
            } else if (value == null && imageView != null) {
            	Drawable drawable = null;
            	if (mCropNail) {
            		//item load failed
					drawable = mResources.getDrawable(R.drawable.item_load_failed);
				}else {
					//wallpaper load failed
					drawable = mResources.getDrawable(R.drawable.item_load_failed);
				}
                setImageDrawable(imageView, drawable);
                if (mImageLoaderCallback != null && mPosition > -1) {
                    mImageLoaderCallback.onImageLoad(false,mPosition);
                }
            }
        }

        @Override
        protected void onCancelled(BitmapDrawable value) {
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }

        public void setPosition(int position) {
            mPosition = position;
        }
    }

    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    private void setImageDrawable(ImageView imageView, Drawable drawable) {
        if (mFadeInBitmap) {
            final TransitionDrawable td =
                    new TransitionDrawable(new Drawable[] {
                            new ColorDrawable(android.R.color.transparent),
                            drawable
                    });
            imageView.setBackgroundDrawable(new BitmapDrawable(mResources, mLoadingBitmap));

            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageDrawable(drawable);
        }
    }

    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer)params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
                    
                case MESSAGE_MEMORY_CLEAR:
                	clearMemoryCacheInternal();
                	break;
            }
            return null;
        }
    }

    protected void initDiskCacheInternal() {
        if (mImageCache != null) {
            mImageCache.initDiskCache(updateCache);
        }
    }

    protected void clearCacheInternal() {
        if (mImageCache != null) {
            mImageCache.clearCache();
        }
    }
    
    protected void clearMemoryCacheInternal() {
        if (mImageCache != null) {
            mImageCache.clearMemoryCache();
        }
    }

    protected void flushCacheInternal() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
    }

    protected void closeCacheInternal() {
        if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
        }
    }
    
    public void clearMemoryCache() {
        new CacheAsyncTask().execute(MESSAGE_MEMORY_CLEAR);
    }

    public void clearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }

    public void setImageLoaderCallback(ImageLoaderCallback callback) {
        mImageLoaderCallback = callback;
    }

    public interface ImageLoaderCallback {
        void onImageLoad(boolean success, int position);
        void onImageLoadFailed(int position);
    }

    private static int getRotationFromExif(Context context, String path) {
    	try{
        return getRotationFromExifHelper(path, null, 0, context, null);
    	}catch(Exception e){
    		return 0;
    	}
    }

    private static int getRotationFromExifHelper(
            String path, Resources res, int resId, Context context, Uri uri) throws IOException{
        ExifInterface ei = new ExifInterface();
            if (path != null) {
                ei.readExif(path);
            } else if (uri != null) {
                InputStream is = context.getContentResolver().openInputStream(uri);
                BufferedInputStream bis = new BufferedInputStream(is);
                ei.readExif(bis);
            } else {
                InputStream is = res.openRawResource(resId);
                BufferedInputStream bis = new BufferedInputStream(is);
                ei.readExif(bis);
            }
            Integer ori = ei.getTagIntValue(ExifInterface.TAG_ORIENTATION);
            if (ori != null) {
                return ExifInterface.getRotationForOrientationValue(ori.shortValue());
            }
      
        return 0;
    }
    
    private String getCacheName(String filename) {
    	File file = new File(filename);
    	StringBuffer sb = new StringBuffer();
    	if (file != null && file.exists()) {
			sb.append(file.length()).append("_");
			sb.append(file.hashCode()).append("_");
		}
    	sb.append(filename);
    	return sb.toString();
    }
}
