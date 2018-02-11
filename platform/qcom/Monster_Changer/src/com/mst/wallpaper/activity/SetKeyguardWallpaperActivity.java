package com.mst.wallpaper.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;
import mst.widget.ViewPager.OnPageChangeListener;
import mst.widget.toolbar.Toolbar;
import mst.widget.toolbar.Toolbar.OnMenuItemClickListener;
import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mst.wallpaper.activity.SetKeyguardWallpaperActivity.SdMountRecevier;
import com.mst.wallpaper.adapter.WallpaperCropAdapter;
import com.mst.wallpaper.adapter.WallpaperCropAdapter.OnItemClickedListener;
import com.mst.wallpaper.db.WallpaperDbController;
import com.mst.wallpaper.imageutils.AsyncTask;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.imageutils.ImageWorker.ImageLoaderCallback;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.presenter.DatabasePresenter;
import com.mst.wallpaper.presenter.WallpaperDatabaseContract;
import com.mst.wallpaper.presenter.WallpaperDatabaseContract.Presenter;
import com.mst.wallpaper.presenter.KeyguardWallpaperDbPresenterImpl;
import com.mst.wallpaper.utils.BitmapUtils;
import com.mst.wallpaper.utils.CommonUtil;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.FileUtils;
import com.mst.wallpaper.utils.WallpaperManager;
import com.mst.wallpaper.utils.task.KeyguardWallpaperHandler;
import com.mst.wallpaper.utils.task.KeyguardWallpaperHandlerImpl;
import com.mst.wallpaper.utils.task.KeyguardWallpaperHandlerView;
import com.mst.wallpaper.utils.task.WidgetColorThread;
import com.mst.wallpaper.utils.task.WidgetColorThread.OnColorPickerListener;
import com.mst.wallpaper.widget.CropImageView;
import com.mst.wallpaper.widget.CropViewPager;
import com.mst.wallpaper.widget.TimeWidget;
import com.mst.wallpaper.R;
public class SetKeyguardWallpaperActivity extends BaseActivity implements ImageLoaderCallback,OnPageChangeListener
,KeyguardWallpaperHandlerView,OnClickListener,OnMenuItemClickListener,OnItemClickedListener,OnColorPickerListener{

    private static final String TAG = "WallpaperCropActivity";
    private static final float IMAGE_SCALE = 1.5f;
    private static final int TIME_STAMP_HANDLE_PRE_BTN = 600;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MSG_HANDLE_PRE_BTN = 0X01;
    private static final int ITEM_OK = 0;
    private static final int DIALOG_CROP = 1;
    private static final int DIALOG_CANCEL = 2;
    private static final String IMAGE_CACHE_DIR = "crop_thumbs";
    private CropViewPager mPager;
    private ImageView mPreviousBtn;
    private ImageView mNextBtn;
    private LinearLayout mBottomBar;
    private Toolbar mToolbar;
    private Context mContext;
    private WallpaperCropAdapter mCropAdapter;
    private ImageResizer mImageResizer;
    
    private String mWallpaperName = "";
    private String mCropType = "";
   
    private boolean mCancelFlag = false;
    private boolean mIsSaveFlag = false;
    private boolean mIsCropFlag = false;
    private boolean mShowToolbar = true;
    private SdMountRecevier mSdMountRecevier;
    private List<String> mImageList = null;
    private Intent mIntent;
    private WallpaperManager mWallpaperManager;
	private KeyguardWallpaperHandler mWallpaperHandler;
	private TimeWidget mTimeWidget;
	private float mOldX = 0;
    private float mOldY = 0;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_HANDLE_PRE_BTN:
				showProgress(false, mPager.getCurrentItem());
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                updateOptionButtonStatus(true, true);
				break;

			default:
				break;
			}
		};
	};
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mContext = this;
		mWallpaperHandler = new KeyguardWallpaperHandlerImpl(this,this);
		getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | 
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		getWindow().setNavigationBarColor(Color.TRANSPARENT);
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wallpaper_crop);
        mWallpaperManager = WallpaperManager.getInstance();
        mIntent = getIntent();
        initialView();
        mImageResizer = initImageResizer(IMAGE_CACHE_DIR, IMAGE_SCALE, this);
         mIntent = getIntent();
         checkPermission();
         if (mSdMountRecevier == null) {
             registSdMountRecevier();
         }
	}
	
	private void checkPermission(){
    	int permission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) & 
    			checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    	if(permission != PackageManager.PERMISSION_GRANTED){
    		requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}
    		, PERMISSION_REQUEST_CODE);
    	}else{
    		handlerIntent();
    	}
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode,
    		String[] permissions, int[] grantResults) {
    	// TODO Auto-generated method stub
    	if(requestCode == PERMISSION_REQUEST_CODE){
    		handlerIntent();
    	}
    }
    
    
    private void handlerIntent(){
    	mWallpaperHandler.handleIntent(mIntent);
    }
	
    public int getCurrentItem() {
    	return mPager.getCurrentItem();
    }
	
	public void initialView(){
		 mPager = ( CropViewPager ) findViewById(R.id.wallpaper_crop_pager);		
	     mPager.setOnPageChangeListener(this);
	     mBottomBar = ( LinearLayout ) findViewById(R.id.wallpaper_crop_bottom_bar);
	     mPreviousBtn = ( ImageView ) findViewById(R.id.wallpaper_crop_previous);
         mNextBtn = ( ImageView ) findViewById(R.id.wallpaper_crop_next);
         mToolbar = (Toolbar)findViewById(R.id.crop_toolbar);
         mTimeWidget = (TimeWidget)findViewById(R.id.time_layout);
         mToolbar.setOnMenuItemClickListener(this);
         mToolbar.setNavigationOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog(DIALOG_CANCEL);
			}
		});
         mPreviousBtn.setOnClickListener(this);
         mNextBtn.setOnClickListener(this);
	}
	
	/**
	 * 显示或者隐藏ProgressDialog
	 * @param show
	 * @param position
	 */
    private void showProgress(boolean show, int position) {
        View view = getViewByPosition(position);
        if (show) {
            if (view != null) {
                view.findViewById(R.id.wallpaper_crop_pb).setVisibility(View.VISIBLE);
            }
        } else {
            if (view != null) {
                view.findViewById(R.id.wallpaper_crop_pb).setVisibility(View.INVISIBLE);
            }
        }
    }
    
    /**
     * 根据当前位置获取当前View
     * @param position
     * @return
     */
    private View getViewByPosition(int position) {
        View view = null;
        List<View> views = mCropAdapter.getItemViews();
        if (views != null && views.size() > 0) {
            view = views.get(position);
        }
        return view;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CROP:
                ProgressDialog dialog = new ProgressDialog(mContext);
                dialog.setTitle("");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage(getString(R.string.setting_wallpaper));
                return dialog;

            case DIALOG_CANCEL:
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	builder.setTitle(R.string.wallpaper_crop_cancel_title);
            	builder.setMessage(R.string.wallpaper_crop_cancel_msg);
            	builder.setCancelable(true);
            	builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	dialog.dismiss();
                    }
                });
            	builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteWallpapers(mContext, mWallpaperName);
                        finish();
                    }
                });
                return builder.show();
            default:
                break;
        }
        return super.onCreateDialog(id);
    }
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	// TODO Auto-generated method stub
    	
    	if(keyCode == KeyEvent.KEYCODE_BACK){
    		showDialog(DIALOG_CANCEL);
    		return true;
    	}
    	
    	return super.onKeyDown(keyCode, event);
    }
    
    
    private void deleteWallpapers(Context context, String groupName) {
        String path = Config.WallpaperStored.DEFAULT_SDCARD_KEYGUARD_WALLPAPER_PATH + groupName;
        FileUtils.deleteDirectory(path);
        WallpaperDbController dbControl = new WallpaperDbController(context);
        dbControl.deleteWallpaperByName(groupName);
        dbControl.close();
    }
    

	@Override
	public void onImageLoad(boolean success, int position) {
		// TODO Auto-generated method stub
		 Log.d(TAG, "callBack=" + success);
		 if(position == 0){
			 updateTimeWidgetColor(position);
		 }
         showProgress(false, position);
				if (mImageList != null) {
					if (mImageList.size() == 1) {
					}else if (mImageList.size() > 1) {
						mNextBtn.setEnabled(true);
					} 
				}
	}

	@Override
	public void onImageLoadFailed(int position) {
		// TODO Auto-generated method stub
		 Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
         if (mCancelFlag) {
             mWallpaperManager.deleteKeyguardWallpaperByName(mContext, mWallpaperName);
         }
         finish();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		 if (mImageList != null && mImageList.size() > 0) {
             mNextBtn.setImageResource(R.drawable.wallpaper_crop_next);
             if (position == 0) {
            	 updatePreviousBtnStatus(false);
             } else if (position == (mImageList.size() - 1)) {
                 mNextBtn.setImageResource(R.drawable.wallpaper_crop_ok);
                 updatePreviousBtnStatus(true);
             } else {
            	 updatePreviousBtnStatus(true);
             }
         }
		 mTimeWidget.onPageSelected(position);
		 updateTimeWidgetColor(position);
	}
	
	private void updatePreviousBtnStatus(boolean enable){
		mPreviousBtn.setEnabled(enable);
    	int previousTint = Color.WHITE;
    	if(!enable){
    		previousTint = getColor(R.color.previous_btn_tint_disable);
    	}
    	mPreviousBtn.getDrawable().setTint(previousTint);
	}
	
	
	private void updateTimeWidgetColor(int position){
		updateWidgetColor(position);
	}
	
	private void updateWidgetColor(int position) {
		ImageView iv = mCropAdapter.getItemImageView(position);
		WidgetColorThread task = new WidgetColorThread(iv);
		task.setOnColorPickerListener(this);
		task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, null);
	}
	
	private void updateWidgetColorInner(int color){
		mTimeWidget.setBlackStyle(false,color);
		Window window = getWindow();
		View decorView = window.getDecorView();
		CommonUtil.lightNavigationBar(window, decorView, color != Config.Color.COLOR_WHITE);
	}
	

	@Override
	public void onWallpaperIntentHandled(List<String> images,String name) {
		// TODO Auto-generated method stub
		mWallpaperName = name;
		mImageList = images;
		mCropAdapter = new WallpaperCropAdapter(this, images);
		mPager.setAdapter(mCropAdapter);
		mCropAdapter.setImageResizer(mImageResizer);
		visibleBottomBar(images != null && images.size() > 1);
	}
	
	private void visibleBottomBar(boolean visible){
		if(!visible){
			mToolbar.inflateMenu(R.menu.wallpaper_crop_done);
		}
		mBottomBar.setVisibility(visible?View.VISIBLE:View.GONE);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unRegistSdMountRecevier();
	}

    private void registSdMountRecevier() {
        mSdMountRecevier = new SdMountRecevier();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mSdMountRecevier, filter);
    }

    private void unRegistSdMountRecevier() {
        unregisterReceiver(mSdMountRecevier);
        mSdMountRecevier = null;
    }

    class SdMountRecevier extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();

            if (Intent.ACTION_MEDIA_SHARED.equals(action) || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)
                    || Intent.ACTION_MEDIA_EJECT.equals(action) || Intent.ACTION_MEDIA_REMOVED.equals(action)
                    || Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                Toast.makeText(mContext, getString(R.string.wallpaper_crop_sdcard_error), Toast.LENGTH_SHORT)
                        .show();
                mIsSaveFlag = false;
                SetKeyguardWallpaperActivity.this.finish();
            }
        }
    }

    private void updateOptionButtonStatus(boolean preBtnStatus,boolean nextBtnStatus){
    	mPreviousBtn.setEnabled(preBtnStatus);
        mNextBtn.setEnabled(nextBtnStatus);
    }
    
    
	@Override
	public void onClick(View v) {
		View view = getViewByPosition(mPager.getCurrentItem());
        CropImageView cropImageView = ( CropImageView ) view.findViewById(R.id.wallpaper_crop_item);
        switch (v.getId()) {
            case R.id.wallpaper_crop_previous:
            	if (cropImageView.mIsSaveEnable) {
            		if (mPager.getCurrentItem() == 1) {
            			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
					}else {
						showProgress(true, mPager.getCurrentItem());
						updateOptionButtonStatus(false, false);
						mHandler.sendEmptyMessageAtTime(MSG_HANDLE_PRE_BTN, TIME_STAMP_HANDLE_PRE_BTN);
					}
				}
                break;
            case R.id.wallpaper_crop_next:
                if (view != null) {
                    if (cropImageView.mIsSaveEnable) {
                    	mWallpaperHandler.cropWallpaper(mWallpaperName,mPager.getCurrentItem(), mImageList);
                        showProgress(true, mPager.getCurrentItem());
                        mIsCropFlag = true;
                        mCancelFlag = true;
                        updateOptionButtonStatus(false, false);
                    }
                }
                break;
            default:
                break;
        }
		
		
	}

	private void showToolbar( ){
		boolean show = mToolbar.getVisibility() == View.VISIBLE;
		mToolbar.setVisibility(show?View.GONE:View.VISIBLE);
	}
	
	
	
	@Override
	public View getItemView(int position) {
		// TODO Auto-generated method stub
		return getViewByPosition(mPager.getCurrentItem());
	}

	@Override
	public void refreshStatus(boolean finish) {
		// TODO Auto-generated method stub
		showProgress(false, mPager.getCurrentItem());
        if (mCancelFlag) {
        	mPager.setCurrentItem(mPager.getCurrentItem() + 1);
        	updatePreviousBtnStatus(true);
            mNextBtn.setEnabled(true);
		}
        mIsCropFlag = false;
        if(finish){
        	finish();
        }
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId() == R.id.wallpaper_crop_done){
		       View view = getViewByPosition(mPager.getCurrentItem());
               if (view != null) {
                   CropImageView cropImageView = ( CropImageView ) view
                           .findViewById(R.id.wallpaper_crop_item);
                   if (cropImageView.mIsSaveEnable) {
                       showProgress(true, mPager.getCurrentItem());
                       mIsCropFlag = true;
                       mWallpaperHandler.cropWallpaper(mWallpaperName,mPager.getCurrentItem(), mImageList);
                   }
               }
		}
		return true;
	}

	@Override
	public void onItemClicked(View view) {
		// TODO Auto-generated method stub
		showToolbar();
	}

	@Override
	public void onColorPicked(int color) {
		// TODO Auto-generated method stub
		updateWidgetColorInner(color);
	}
	
	
	
	
	
	
	
	
}
