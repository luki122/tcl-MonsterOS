package com.mst.wallpaper.activity;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import mst.app.dialog.ProgressDialog;
import mst.widget.toolbar.Toolbar;
import mst.widget.toolbar.Toolbar.OnMenuItemClickListener;

import com.android.gallery3d.common.Utils;
import com.android.gallery3d.exif.ExifInterface;
import com.mst.wallpaper.db.SharePreference;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.IOUtils;
import com.mst.wallpaper.widget.BitmapRegionTileSource;
import com.mst.wallpaper.widget.CropView;
import com.mst.wallpaper.widget.TiledImageView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mst.wallpaper.R;
public class SetDesktopWallpaperActivity extends BaseActivity implements OnMenuItemClickListener{
	private static final String TAG = "SetWallpaperCropActivity";
	public static final String KEY_SET_AS_WALLPAPER = "set-as-wallpaper";
	
	public static final int MSG_IMAGE_LOAD_DONE = 1;
	public static final int MSG_IMAGE_LOAD_ERROR = 2;
	public static final int MSG_WALLPAPER_SAVE_DONE = 3;
	public static final int MSG_WALLPAPER_SAVE_ERROR = 4;
	public static final int MSG_IMAGE_TOO_SMALL = 5;
	private static final int MSG_ABANDON_SET_WALLPAPER = 6;
	
	private static final int ACTION_BTN_DONE = 1;
	private static final int DEFAULT_COMPRESS_QUALITY = 90;
	private static final float WALLPAPER_SCREENS_SPAN = 2f;
	private static final int MIN_IMAGE_WIDTH = 55;
	private static final int MIN_IMAGE_HEIGHT = 100;
	
	public static final File WALLPAPER_DIR = new File(Config.WallpaperStored.WALLPAPER_PATH);
	
	protected CropView mCropView;
    protected Uri mUri;
    private boolean mIsWallpaper = false;
    private View mPortraitView;
    private View mLandscapeView;
    private ProgressDialog mProgressDialog;
    private Toolbar mToolbar;
    private BitmapRegionTileSource mSource;
    private int mWallpaperType = TiledImageView.WALLPAPER_TYPE_PORT;
    private int mWidth;
    private int mHeight;
    private String mFilePath;
    private boolean mDialogShow = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | 
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		getWindow().setNavigationBarColor(Color.TRANSPARENT);
		super.onCreate(savedInstanceState);
		
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize UI
        setContentView(R.layout.wallpaper_crop); 
		init();
		initBottomBar();
		if(mProgressDialog != null && mProgressDialog.isShowing()){
			mProgressDialog.dismiss();
		}
	}
	
    

    
	
	@Override
    protected void onResume() {
        super.onResume();
	}

	@Override
    protected void onPause() {
        super.onPause();
        if (mCropView != null) {
			mCropView.resume();
		}
    }
	
	@Override
    protected void onDestroy() {
		super.onDestroy();
		if (mCropView != null) {
			mCropView.destroy_new();
		}
		if(mSource != null){
			mSource.destroy();
		}
		System.gc();
		mSource = null;
	}
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_IMAGE_LOAD_DONE:
				dismissProgressDialogIfShown();
				if (!mDialogShow && mCropView != null && mCropView.getImageWidth() < MIN_IMAGE_WIDTH
						&& mCropView.getImageHeight() < MIN_IMAGE_HEIGHT) {
					showNoticeDialog(R.string.image_too_small);
				}
				break;
			
			case MSG_IMAGE_LOAD_ERROR:
				dismissProgressDialogIfShown();
				if (!mDialogShow) {
					showNoticeDialog(R.string.image_load_failed);
				}
				break;
			case MSG_WALLPAPER_SAVE_DONE:
				dismissProgressDialogIfShown();
				Toast.makeText(SetDesktopWallpaperActivity.this, R.string.wallpaper_set_success, Toast.LENGTH_SHORT).show();
				setResult(Activity.RESULT_OK);
             finish();
				break;
			case MSG_WALLPAPER_SAVE_ERROR:
				dismissProgressDialogIfShown();
				updateSelectPath(true);
				Toast.makeText(SetDesktopWallpaperActivity.this, R.string.wallpaper_set_failed, Toast.LENGTH_SHORT).show();
				setResult(Activity.RESULT_OK);
                finish();
				break;
			case MSG_IMAGE_TOO_SMALL:
			case MSG_ABANDON_SET_WALLPAPER:
                finish();
                break;
			}
		}
	};
	
	private void sendMyBroadcast() {
		Intent intent = new Intent();
		intent.setAction(Config.Action.ACTION_WALLPAPER_SET);
		sendBroadcast(intent);
	}
	
	private void updateSelectPath(boolean failed) {
		if (failed) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SetDesktopWallpaperActivity.this);
			SharedPreferences.Editor editor = sp.edit();
			editor.putString("selectpath", "-1");
			editor.commit();
		}
	}
	
	private void dismissProgressDialogIfShown() {
        if (mProgressDialog != null) {
        	if(!isDestroyed() || !isFinishing()){
        		mProgressDialog.dismiss();
        		mProgressDialog = null;
        	}
        }
    }
	

	
	protected void init() {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		mWidth = dm.widthPixels;
		mHeight = dm.heightPixels;

		showLoadDialog(R.string.image_loading);
		mCropView = (CropView) findViewById(R.id.cropView);
		mToolbar = (Toolbar)findViewById(R.id.crop_toolbar);
		mToolbar.inflateMenu(R.menu.wallpaper_crop_done);
		mToolbar.setOnMenuItemClickListener(this);
		mToolbar.setNavigationOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(MSG_ABANDON_SET_WALLPAPER);
			}
		});
		Intent cropIntent = getIntent();
		mUri = cropIntent.getData();

		if (mUri == null) {
			Log.e(TAG, "No URI passed in intent, exiting WallpaperCropActivity");
			finish();
			return;
		}
		if (mUri.getPath().contains("/external/images/media")) {
			mFilePath = getRealPathFromURI(mUri);
		} else {
			mFilePath = mUri.getPath();
		}
		
		Log.d("crop", "path:"+mFilePath);

		final int rotation = getRotationFromExif(SetDesktopWallpaperActivity.this,
				mUri);
		new Thread(new Runnable() {

			@Override
			public void run() {
				Looper.prepare();
				mSource = new BitmapRegionTileSource(
						getApplicationContext(), mUri, mWidth, rotation, mHandler);
				mCropView.setTileSource(mSource, null, mWallpaperType);
				mCropView.setTouchEnabled(true);
				Looper.loop(); //此两处Looper必须要，不然会出错
			}
		}).start();
	}
	
	private void showLoadDialog(int resId) {
		mProgressDialog = ProgressDialog.show(
                this, null, getString(resId), true, false);
        mProgressDialog.setCanceledOnTouchOutside(false);
	}
	
	private void showNoticeDialog(int resId) {
		mDialogShow = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.set_wallpaper_dialog_title));
		builder.setMessage(getString(resId));
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.confirm_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
				mDialogShow = false;
				mHandler.sendEmptyMessage(MSG_IMAGE_TOO_SMALL);
			}
		});
		builder.show();
	}
	
	private void initBottomBar() {
		LinearLayout bottomBar = (LinearLayout) findViewById(R.id.crop_wallpaper_bottombar);
		bottomBar.setVisibility(View.VISIBLE);
		mPortraitView = findViewById(R.id.cropimage_bottom_control_portrait);
		mPortraitView.setSelected(true);
		mLandscapeView = findViewById(R.id.cropimage_bottom_control_landscape);
		mPortraitView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPortraitView.setSelected(true);
				mLandscapeView.setSelected(false);
				mWallpaperType = TiledImageView.WALLPAPER_TYPE_PORT;
				mCropView.setTileSource(mSource, null, mWallpaperType);
			}
		});
		mLandscapeView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPortraitView.setSelected(false);
				mLandscapeView.setSelected(true);
				mWallpaperType = TiledImageView.WALLPAPER_TYPE_LAND;
				mCropView.setTileSource(mSource, null, mWallpaperType);
			}
		});
	}
	
	public static int getRotationFromExif(Context context, Uri uri) {
        return getRotationFromExifHelper(null, null, 0, context, uri);
    }

    public static int getRotationFromExif(Resources res, int resId) {
        return getRotationFromExifHelper(null, res, resId, null, null);
    }

    private static int getRotationFromExifHelper(
            String path, Resources res, int resId, Context context, Uri uri) {
        ExifInterface ei = new ExifInterface();
        try {
            if (path != null) {
                ei.readExif(path);
            } else if (uri != null) {
                InputStream is = context.getContentResolver().openInputStream(uri);
                BufferedInputStream bis = new BufferedInputStream(is);
                ei.readExif(bis);
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(bis);
            } else {
                InputStream is = res.openRawResource(resId);
                BufferedInputStream bis = new BufferedInputStream(is);
                ei.readExif(bis);
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(bis);
            }
            Integer ori = ei.getTagIntValue(ExifInterface.TAG_ORIENTATION);
            if (ori != null) {
                return ExifInterface.getRotationForOrientationValue(ori.shortValue());
            }
        } catch (IOException e) {
            Log.w(TAG, "Getting exif data failed", e);
        }
        return 0;
    }
    
    protected void cropImageAndSetWallpaper(Uri uri,
            OnBitmapCroppedHandler onBitmapCroppedHandler, Handler handler) {
    	if (!mCropView.canCrop()) {
			return;
		}
        showLoadDialog(R.string.wallpaper_setting);
        
        Point minDims = new Point();
        Point maxDims = new Point();
        Display d = getWindowManager().getDefaultDisplay();
        d.getCurrentSizeRange(minDims, maxDims);

        Point displaySize = new Point();
        d.getSize(displaySize);

        int maxDim = Math.max(maxDims.x, maxDims.y);
        final int minDim = Math.min(minDims.x, minDims.y);
        int defaultWallpaperWidth;
        if (isScreenLarge(getResources())) {
            defaultWallpaperWidth = (int) (maxDim *
                    wallpaperTravelToScreenWidthRatio(maxDim, minDim));
        } else {
            defaultWallpaperWidth = Math.max((int)
                    (minDim * WALLPAPER_SCREENS_SPAN), maxDim);
        }

        boolean isPortrait = displaySize.x < displaySize.y;
        int portraitHeight;
        if (isPortrait) {
            portraitHeight = mCropView.getHeight();
        } else {
            // TODO: how to actually get the proper portrait height?
            // This is not quite right:
            portraitHeight = Math.max(maxDims.x, maxDims.y);
        }
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Point realSize = new Point();
            d.getRealSize(realSize);
            portraitHeight = Math.max(realSize.x, realSize.y);
        }
        // Get the crop
        RectF cropRect = mCropView.getCrop();
        int cropRotation = mCropView.getImageRotation();
        float cropScale = mCropView.getHeight() / (float) cropRect.height();
        Point inSize = mCropView.getSourceDimensions();
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.setRotate(cropRotation);
        float[] rotatedInSize = new float[] { inSize.x, inSize.y };
        rotateMatrix.mapPoints(rotatedInSize);
        rotatedInSize[0] = Math.abs(rotatedInSize[0]);
        rotatedInSize[1] = Math.abs(rotatedInSize[1]);

        final int outWidth = (mWallpaperType == TiledImageView.WALLPAPER_TYPE_PORT ? mWidth : 2 * mWidth);
        int outHeight = mCropView.getHeight()/*(int) Math.round(cropRect.height() * cropScale)*/;
		 if(outHeight == 0) outHeight = mHeight;
        
        BitmapCropTask cropTask = new BitmapCropTask(getApplicationContext(), uri, cropRect, cropRotation, outWidth, outHeight, true, true, handler, mFilePath);
        if (onBitmapCroppedHandler != null) {
            cropTask.setOnBitmapCropped(onBitmapCroppedHandler);
        }
        cropTask.execute();
    }
    
    public interface OnBitmapCroppedHandler {
        public void onBitmapCropped(byte[] imageBytes);
    }
    
    private static boolean isScreenLarge(Resources res) {
        Configuration config = res.getConfiguration();
        return config.smallestScreenWidthDp >= 720;
    }
    
    // As a ratio of screen height, the total distance we want the parallax effect to span
    // horizontally
    private static float wallpaperTravelToScreenWidthRatio(int width, int height) {
        float aspectRatio = width / (float) height;

        // At an aspect ratio of 16/10, the wallpaper parallax effect should span 1.5 * screen width
        // At an aspect ratio of 10/16, the wallpaper parallax effect should span 1.2 * screen width
        // We will use these two data points to extrapolate how much the wallpaper parallax effect
        // to span (ie travel) at any aspect ratio:

        final float ASPECT_RATIO_LANDSCAPE = 16/10f;
        final float ASPECT_RATIO_PORTRAIT = 10/16f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE = 1.5f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT = 1.2f;

        // To find out the desired width at different aspect ratios, we use the following two
        // formulas, where the coefficient on x is the aspect ratio (width/height):
        //   (16/10)x + y = 1.5
        //   (10/16)x + y = 1.2
        // We solve for x and y and end up with a final formula:
        final float x =
            (WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE - WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT) /
            (ASPECT_RATIO_LANDSCAPE - ASPECT_RATIO_PORTRAIT);
        final float y = WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT - x * ASPECT_RATIO_PORTRAIT;
        return x * aspectRatio + y;
    }
    
    
    protected static class BitmapCropTask extends AsyncTask<Void, Void, Boolean> {
        Uri mInUri = null;
        Context mContext;
        String mInFilePath;
        byte[] mInImageBytes;
        int mInResId = 0;
        InputStream mInStream;
        RectF mCropBounds = null;
        int mOutWidth, mOutHeight;
        int mRotation;
        String mOutputFormat = "jpg"; // for now
        boolean mSetWallpaper;
        boolean mSaveCroppedBitmap;
        Bitmap mCroppedBitmap;
        //Runnable mOnEndRunnable;
        Resources mResources;
        OnBitmapCroppedHandler mOnBitmapCroppedHandler;
        boolean mNoCrop;
        Handler mTaskHandler;

        public BitmapCropTask(Context c, String filePath,
                RectF cropBounds, int rotation, int outWidth, int outHeight,
                boolean setWallpaper, boolean saveCroppedBitmap, /*Runnable onEndRunnable, */Handler handler) {
            mContext = c;
            mInFilePath = filePath;
            mTaskHandler = handler;
            init(cropBounds, rotation,
                    outWidth, outHeight, setWallpaper, saveCroppedBitmap/*, onEndRunnable*/);
        }

        public BitmapCropTask(byte[] imageBytes,
                RectF cropBounds, int rotation, int outWidth, int outHeight,
                boolean setWallpaper, boolean saveCroppedBitmap, /*Runnable onEndRunnable, */Handler handler) {
            mInImageBytes = imageBytes;
            mTaskHandler = handler;
            init(cropBounds, rotation,
                    outWidth, outHeight, setWallpaper, saveCroppedBitmap/*, onEndRunnable*/);
        }

        public BitmapCropTask(Context c, Uri inUri,
                RectF cropBounds, int rotation, int outWidth, int outHeight,
                boolean setWallpaper, boolean saveCroppedBitmap, /*Runnable onEndRunnable, */Handler handler, String path) {
            mContext = c;
            mInUri = inUri;
            mInFilePath = path;
            mTaskHandler = handler;
            init(cropBounds, rotation,
                    outWidth, outHeight, setWallpaper, saveCroppedBitmap/*, onEndRunnable*/);
        }

        public BitmapCropTask(Context c, Resources res, int inResId,
                RectF cropBounds, int rotation, int outWidth, int outHeight,
                boolean setWallpaper, boolean saveCroppedBitmap, /*Runnable onEndRunnable, */Handler handler) {
            mContext = c;
            mInResId = inResId;
            mResources = res;
            mTaskHandler = handler;
            init(cropBounds, rotation,
                    outWidth, outHeight, setWallpaper, saveCroppedBitmap/*, onEndRunnable*/);
        }

        private void init(RectF cropBounds, int rotation, int outWidth, int outHeight,
                boolean setWallpaper, boolean saveCroppedBitmap/*, Runnable onEndRunnable*/) {
            mCropBounds = cropBounds;
            mRotation = rotation;
            mOutWidth = outWidth;
            mOutHeight = outHeight;
            mSetWallpaper = setWallpaper;
            mSaveCroppedBitmap = saveCroppedBitmap;
            //mOnEndRunnable = onEndRunnable;
            if ((int) cropBounds.top == 0 && (int)cropBounds.left == 0 && (int)cropBounds.right / (int)cropBounds.bottom == outWidth/ outHeight) {
    			mNoCrop = true;
    		}
        }

        public void setOnBitmapCropped(OnBitmapCroppedHandler handler) {
            mOnBitmapCroppedHandler = handler;
        }

        public void setNoCrop(boolean value) {
            mNoCrop = value;
        }


        // Helper to setup input stream
        private void regenerateInputStream() {
            if (mInUri == null && mInResId == 0 && mInFilePath == null && mInImageBytes == null) {
                Log.w(TAG, "cannot read original file, no input URI, resource ID, or " +
                        "image byte array given");
            } else {
                Utils.closeSilently(mInStream);
                try {
                    if (mInUri != null) {
                    	ContentResolver contentResolver;
                    	InputStream inputStream ;
                    	if(mContext==null){
                    		Log.i(TAG,"regenerateInputStream mContext==null");
                    	}else if((contentResolver=mContext.getContentResolver()) == null){
                    		Log.i(TAG,"regenerateInputStream contentResolver==null");
                    	}else if((inputStream =contentResolver.openInputStream(mInUri))==null){
                    		Log.i(TAG,"regenerateInputStream inputStream==null");
                    	}else{
                            mInStream = new BufferedInputStream(inputStream);   
                    	}
                    	
                    } else if (mInFilePath != null) {
                        mInStream = mContext.openFileInput(mInFilePath);
                    } else if (mInImageBytes != null) {
                        mInStream = new BufferedInputStream(
                                new ByteArrayInputStream(mInImageBytes));
                    } else {
                        mInStream = new BufferedInputStream(
                                mResources.openRawResource(mInResId));
                    }
                } catch (FileNotFoundException e) {
                    Log.w(TAG, "cannot read file: " + mInUri.toString(), e);
                }
                catch(Exception e){
                	Log.w(TAG, "other error", e);
                }
            }
        }

        public Point getImageBounds() {
            regenerateInputStream();
            if (mInStream != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(mInStream, null, options);
                if (options.outWidth != 0 && options.outHeight != 0) {
                    return new Point(options.outWidth, options.outHeight);
                }
            }
            return null;
        }

        public void setCropBounds(RectF cropBounds) {
            mCropBounds = cropBounds;
        }

        public Bitmap getCroppedBitmap() {
            return mCroppedBitmap;
        }
        
        public boolean cropBitmap() {
            boolean failure = false;

            regenerateInputStream();

            WallpaperManager wallpaperManager = null;
            if (mSetWallpaper) {
                wallpaperManager = WallpaperManager.getInstance(mContext.getApplicationContext());
            }
            if (mSetWallpaper && mNoCrop && mInStream != null) {
                try {
                    wallpaperManager.setStream(mInStream);
                    wallpaperManager.suggestDesiredDimensions(mOutWidth, mOutHeight);
                } catch (IOException e) {
                    Log.w(TAG, "cannot write stream to wallpaper", e);
                    failure = true;
                }
                return !failure;
            }
            if (mInStream != null) {
                // Find crop bounds (scaled to original image size)
                Rect roundedTrueCrop = new Rect();
                Matrix rotateMatrix = new Matrix();
                Matrix inverseRotateMatrix = new Matrix();
                if (mRotation > 0) {
                    rotateMatrix.setRotate(mRotation);
                    inverseRotateMatrix.setRotate(-mRotation);

                    mCropBounds.roundOut(roundedTrueCrop);
                    mCropBounds = new RectF(roundedTrueCrop);

                    Point bounds = getImageBounds();

                    float[] rotatedBounds = new float[] { bounds.x, bounds.y };
                    rotateMatrix.mapPoints(rotatedBounds);
                    rotatedBounds[0] = Math.abs(rotatedBounds[0]);
                    rotatedBounds[1] = Math.abs(rotatedBounds[1]);

                    mCropBounds.offset(-rotatedBounds[0]/2, -rotatedBounds[1]/2);
                    inverseRotateMatrix.mapRect(mCropBounds);
                    mCropBounds.offset(bounds.x/2, bounds.y/2);

                    regenerateInputStream();
                }
                
                mCropBounds.roundOut(roundedTrueCrop);
                
                if (roundedTrueCrop.width() <= 0 || roundedTrueCrop.height() <= 0) {
                    Log.w(TAG, "crop has bad values for full size image");
                    failure = true;
                    return false;
                }
                
                // See how much we're reducing the size of the image
                int scaleDownSampleSize = Math.min(roundedTrueCrop.width() / mOutWidth,
                        roundedTrueCrop.height() / mOutHeight);
                // Attempt to open a region decoder
                BitmapRegionDecoder decoder = null;
                try {
                    decoder = BitmapRegionDecoder.newInstance(mInStream, true);
                } catch (IOException e) {
                    Log.w(TAG, "cannot open region decoder for file: " + mInUri.toString(), e);
                }
                Bitmap crop = null;
                if (decoder != null) {
                    // Do region decoding to get crop bitmap
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    if (scaleDownSampleSize > 1) {
                        options.inSampleSize = scaleDownSampleSize;
                    }
                    try {
                    	crop = decoder.decodeRegion(roundedTrueCrop, options);
					} catch (OutOfMemoryError e) {
						// TODO: handle exception
					}
                    decoder.recycle();
                }

                if (crop == null) {
                    // BitmapRegionDecoder has failed, try to crop in-memory
                    regenerateInputStream();
                    Bitmap fullSize = null;
                    if (mInStream != null) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        if (scaleDownSampleSize > 1) {
                            options.inSampleSize = scaleDownSampleSize;
                        }
                        try {
                        	fullSize = BitmapFactory.decodeStream(mInStream, null, options);
						} catch (OutOfMemoryError e) {
							// TODO: handle exception
						}
                        Utils.closeSilently(mInStream);
                    }
                    if (fullSize != null) {
                    	if (scaleDownSampleSize > 1) {
                    		mCropBounds.left /= scaleDownSampleSize;
                            mCropBounds.top /= scaleDownSampleSize;
                            mCropBounds.bottom /= scaleDownSampleSize;
                            mCropBounds.right /= scaleDownSampleSize;
                            mCropBounds.roundOut(roundedTrueCrop);
						}
                    	int x = roundedTrueCrop.left < 2 ? 0 : roundedTrueCrop.left;
                    	int y = roundedTrueCrop.top < 2 ? 0 : roundedTrueCrop.top;
                    	try {
                    		crop = Bitmap.createBitmap(fullSize, x, y, mCropBounds.width() > (fullSize.getWidth() - roundedTrueCrop.left) ? (fullSize.getWidth() - roundedTrueCrop.left) : (int) Math.floor(mCropBounds.width()),
                            		mCropBounds.height() > (fullSize.getHeight() - roundedTrueCrop.top) ? (fullSize.getHeight() - roundedTrueCrop.top) : (int) Math.floor(mCropBounds.height()));
						} catch (Exception e) {
							Log.e(TAG, "Bitmap.createBitmap Exception : ", e);
						}
                    	if (!fullSize.isRecycled()) {
							fullSize.recycle();
							fullSize = null;
						}
                    }
                }
                if (crop == null) {
                    Log.w(TAG, "cannot decode file: " + mInUri.toString());
                    failure = true;
                    return false;
                }
                
                if (mOutWidth > 0 && mOutHeight > 0 || mRotation > 0) {
                    float[] dimsAfter = new float[] { crop.getWidth(), crop.getHeight() };
                    rotateMatrix.mapPoints(dimsAfter);
                    dimsAfter[0] = Math.abs(dimsAfter[0]);
                    dimsAfter[1] = Math.abs(dimsAfter[1]);

                    if (!(mOutWidth > 0 && mOutHeight > 0)) {
                        mOutWidth = Math.round(dimsAfter[0]);
                        mOutHeight = Math.round(dimsAfter[1]);
                    }
                    RectF cropRect = new RectF(0, 0, dimsAfter[0], dimsAfter[1]);
                    RectF returnRect = new RectF(0, 0, mOutWidth, mOutHeight);
                    
                    Matrix m = new Matrix();
                    if (mRotation == 0) {
                        m.setRectToRect(cropRect, returnRect, Matrix.ScaleToFit.FILL);
                    } else {
                        Matrix m1 = new Matrix();
                        m1.setTranslate(-crop.getWidth() / 2f, -crop.getHeight() / 2f);
                        Matrix m2 = new Matrix();
                        m2.setRotate(mRotation);
                        Matrix m3 = new Matrix();
                        m3.setTranslate(dimsAfter[0] / 2f, dimsAfter[1] / 2f);
                        Matrix m4 = new Matrix();
                        m4.setRectToRect(cropRect, returnRect, Matrix.ScaleToFit.FILL);

                        Matrix c1 = new Matrix();
                        c1.setConcat(m2, m1);
                        Matrix c2 = new Matrix();
                        c2.setConcat(m4, m3);
                        m.setConcat(c2, c1);
                    }
                    Bitmap tmp = null;
                    try {
                    	tmp = Bitmap.createBitmap((int) returnRect.width(),
                                (int) returnRect.height(), Bitmap.Config.ARGB_8888);
					} catch (OutOfMemoryError e) {
						e.printStackTrace();
					}
                    if (tmp != null) {
                        Canvas c = new Canvas(tmp);
                        Paint p = new Paint();
                        p.setFilterBitmap(true);
                        c.drawBitmap(crop, m, p);
                        if (!crop.isRecycled()) {
							crop.recycle();
						}
                        crop = tmp;
                        tmp = null;
                    }else {
                    	failure = true;
                    	if (!crop.isRecycled()) {
							crop.recycle();
						}
						return false;
					}
                }

                if (mSaveCroppedBitmap) {
                    mCroppedBitmap = crop;
                    crop = null;
                }
                
                if (mSetWallpaper && wallpaperManager != null) {
                	if (mInFilePath != null) {
                    	int pos = mInFilePath.lastIndexOf('.');
    					mOutputFormat = mInFilePath.substring(pos + 1);
    				}
                		try {
                            wallpaperManager.setBitmap(mCroppedBitmap);
                            wallpaperManager.suggestDesiredDimensions(mOutWidth, mOutHeight);
                        } catch (NoSuchMethodError e) {
                        	Log.w(TAG, "NoSuchMethodError to wallpaper ", e);
                            failure = setStreamToWallpaper(mCroppedBitmap);
    					} catch (IOException e) {
                            Log.w(TAG, "NoSuchMethodError-->cannot write stream to wallpaper", e);
                            failure = true;
                        } 
                		catch (Exception e) {
                            Log.w(TAG, "ohter error",e);
                            failure = true;
                        } 
                    
                }
            }
            return !failure; // True if any of the operations failed
        }
        
        private boolean setStreamToWallpaper(Bitmap crop) {
        	boolean failure = false;
        	// Get output compression format
        	CompressFormat cf =
                    convertExtensionToCompressFormat(getFileExtension(mOutputFormat));

            // Compress to byte array
            ByteArrayOutputStream tmpOut = new ByteArrayOutputStream(2048);
            if (crop.compress(cf, DEFAULT_COMPRESS_QUALITY, tmpOut)) {
            	WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
                // If we need to set to the wallpaper, set it
                if (mSetWallpaper && wallpaperManager != null) {
                    try {
                        byte[] outByteArray = tmpOut.toByteArray();
                        wallpaperManager.setStream(new ByteArrayInputStream(outByteArray));
                        wallpaperManager.suggestDesiredDimensions(mOutWidth, mOutHeight);
                        if (mOnBitmapCroppedHandler != null) {
                            mOnBitmapCroppedHandler.onBitmapCropped(outByteArray);
                        }
                    } catch (IOException ex) {
                        Log.w(TAG, "-->cannot write stream to wallpaper", ex);
                        failure = true;
                    }
                }
            } else {
                Log.w(TAG, "-->cannot compress bitmap");
                failure = true;
            }
            return failure;
		}
        
        @Override
        protected Boolean doInBackground(Void... params) {
            return cropBitmap();
        }

        @Override
        protected void onPostExecute(Boolean result) {
        	if (result) {
        		if (onEndCrop != null) {
                    onEndCrop.run();
                }
			}else {
				mTaskHandler.sendEmptyMessage(MSG_WALLPAPER_SAVE_ERROR);
			}
            
        }
        
        Runnable onEndCrop = new Runnable() {
            public void run() {
            	if (mInFilePath != null) {
            		File oldFile = new File(mInFilePath);
        			File newFile = new File(WALLPAPER_DIR, newFileName(oldFile));
        			try {
        				//if (!WALLPAPER_DIR.exists()) {
							WALLPAPER_DIR.mkdirs();
						//}
                		if (!newFile.exists()) {
                			newFile.createNewFile();
        				}
                    } catch (IOException e) {
                        Log.e(TAG, "fail to create new file: "
                                + newFile.getAbsolutePath(), e);
                        mTaskHandler.sendEmptyMessage(MSG_WALLPAPER_SAVE_ERROR);
                    }
                	if (mNoCrop) {
    					copyFile(mInFilePath, newFile.getPath());
    				}else if (mCroppedBitmap != null) {
                		newFile.setReadable(true, false);
                		newFile.setWritable(true, false);
                        try {
                        	FileOutputStream fos = new FileOutputStream(newFile);
    						mCroppedBitmap.compress(CompressFormat.JPEG, 100, fos);
    						fos.flush();
    						fos.close();
    					}catch (FileNotFoundException e) {
    						e.printStackTrace();
    					}catch (IOException e) {
    						e.printStackTrace();
    					} finally {
    						if (mCroppedBitmap != null && !mCroppedBitmap.isRecycled()) {
    							mCroppedBitmap.recycle();
    						}
    					}
    				}
                	long now = System.currentTimeMillis() / 1000;
    	            ContentValues values = new ContentValues();
    	            values.put(Config.WallpaperStored.WALLPAPER_MODIFIED, now);
    	            values.put(Config.WallpaperStored.WALLPAPER_OLDPATH, mInFilePath);
    	            values.put(Config.WallpaperStored.WALLPAPER_FILENAME, newFile.getName());
    	            Cursor cursor = null;
    				
    	            try {
    					cursor = mContext.getContentResolver().query(Config.WallpaperStored.LOCAL_WALLPAPER_URI, new String[] { Config.WallpaperStored.WALLPAPER_FILENAME }, 
    							Config.WallpaperStored.WALLPAPER_OLDPATH + " = '" + mInFilePath + "'", null, null);
    					if (cursor != null && cursor.moveToNext()) {
    						mContext.getContentResolver().update(Config.WallpaperStored.LOCAL_WALLPAPER_URI, values, Config.WallpaperStored.WALLPAPER_OLDPATH + " = '" + mInFilePath + "'", null);
    					}else {
    						mContext.getContentResolver().insert(Config.WallpaperStored.LOCAL_WALLPAPER_URI, values);
    					}
    					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
    					SharedPreferences.Editor editor = sp.edit();
    					editor.putString(SharePreference.KEY_SELECT_DESKTOP_PATH, newFile.getName());
    					editor.commit();
    					SharePreference.saveDesktopWallpaper(mContext, -1, newFile.getAbsolutePath());
						Config.isChangedByLocal = 2;
						Config.isWallPaperChanged = true;
    				} catch (Exception e) {
    					e.printStackTrace();
    					mTaskHandler.sendEmptyMessage(MSG_WALLPAPER_SAVE_ERROR);
    				} finally {
    					if (cursor != null) {
    						cursor.close();
    					}
    				}
    	            mTaskHandler.sendEmptyMessage(MSG_WALLPAPER_SAVE_DONE);
				}
            }
        };
        
        private String newFileName(File oldFile) {
        	StringBuffer newName = new StringBuffer();
			if (oldFile != null) {
				newName.append("monster_").append(oldFile.getPath().hashCode()).append("_").append(oldFile.getName());
			}
			return newName.toString();
		}
        
        private void copyFile(String oldPath, String newPath) {
            try {   
                int bytesum = 0;   
                int byteread = 0;   
                File oldfile = new File(oldPath);   
                if (oldfile.exists()) { //文件存在时   
                    InputStream inStream = new FileInputStream(oldPath); //读入原文件   
                    FileOutputStream fos = new FileOutputStream(newPath);   
                    byte[] buffer = new byte[1024];   
                    int length;   
                    while ( (byteread = inStream.read(buffer)) != -1) {   
                        bytesum += byteread; //字节数 文件大小   
                        fos.write(buffer, 0, byteread);   
                    }
                    IOUtils.closeQuietly(inStream);
                    IOUtils.closeQuietly(fos); 
                }
            }catch (Exception e) {   
                e.printStackTrace();   
            }   
        }
        
        private String newFilePath(String oldPath) {
        	File oldFile = new File(oldPath);
			File newFile = new File(WALLPAPER_DIR, "iuni_" + oldFile.getName());
			try {
        		if (!newFile.exists()) {
        			newFile.createNewFile();
				}
            } catch (IOException e) {
                Log.e(TAG, "fail to create new file: "
                        + newFile.getAbsolutePath(), e);
                return null;
            }
			return newFile.getPath();
		}
        
        private CompressFormat convertExtensionToCompressFormat(String extension) {
            return extension.equals("png")
                    ? CompressFormat.PNG
                    : CompressFormat.JPEG;
        }
    }
    
    protected static String getFileExtension(String requestFormat) {
        String outputFormat = (requestFormat == null)
                ? "jpg"
                : requestFormat;
        outputFormat = outputFormat.toLowerCase();
        return (outputFormat.equals("png")/* || outputFormat.equals("gif")*/)
                ? "png" // We don't support gif compression.
                : "jpg";
    }
    
    private String getRealPathFromURI(Uri contentUri) {  
  	  
        // can post image  
        String [] proj={MediaStore.Images.Media.DATA};  
        Cursor cursor = managedQuery( contentUri,  
                        proj, // Which columns to return  
                        null,       // WHERE clause; which rows to return (all rows)  
                        null,       // WHERE clause selection arguments (none)  
                        null); // Order-by clause (ascending by name)  
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);  
        cursor.moveToFirst();  
  
        return cursor.getString(column_index);  
    }


	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId() == R.id.wallpaper_crop_done){
			cropImageAndSetWallpaper(mUri, null, mHandler);
		}
		return true;
	}  

}
