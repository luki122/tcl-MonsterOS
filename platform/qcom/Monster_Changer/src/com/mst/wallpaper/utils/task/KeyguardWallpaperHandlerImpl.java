package com.mst.wallpaper.utils.task;

import java.util.ArrayList;
import java.util.List;

import com.mst.wallpaper.adapter.WallpaperCropAdapter;
import com.mst.wallpaper.db.SharePreference;
import com.mst.wallpaper.db.WallpaperDbController;
import com.mst.wallpaper.imageutils.ImageResizer;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.object.WallpaperThemeInfo;
import com.mst.wallpaper.presenter.WallpaperDatabaseContract;
import com.mst.wallpaper.presenter.KeyguardWallpaperDbPresenterImpl;
import com.mst.wallpaper.presenter.WallpaperDatabaseContract.Presenter;
import com.mst.wallpaper.utils.CommonUtil;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.FileUtils;
import com.mst.wallpaper.utils.WallpaperConfigUtil;
import com.mst.wallpaper.utils.WallpaperManager;
import com.mst.wallpaper.widget.CropImageView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.mst.wallpaper.R;

public class KeyguardWallpaperHandlerImpl implements KeyguardWallpaperHandler, WallpaperDatabaseContract.View{

	
	private static final String TAG = "KeyguardWallpaperHandlerImpl";
	private static final int MAX_KEYGUARD_WALLPAPER_COUNT = 12;
	private KeyguardWallpaperHandlerView mView;
	
	private WallpaperDatabaseContract.Presenter mPresenter;
	
	private WallpaperManager mWallpaperManager;
	
	private String mCropType;
	
	private Context mContext;
	
	private List<String> mImageList = new ArrayList<String>();
	
	private String mWallpaperName;
	
	private boolean mFromTheme = false;
	
	public KeyguardWallpaperHandlerImpl(KeyguardWallpaperHandlerView view,Context context){
		mView = view;
		mPresenter = new KeyguardWallpaperDbPresenterImpl(this, context);
		mContext = context;
		mWallpaperManager = WallpaperManager.getInstance();
	}
	
	
	@Override
	public void handleIntent(Intent intent) {
		// TODO Auto-generated method stub
		mImageList.clear();
		 if (intent != null) {
			 String wallpaperName = getWallpaperName();
             Bundle bundle = intent.getExtras();
             if (bundle != null) {
                 mCropType = bundle.getString(Config.WallpaperStored.KEYGUARD_WALLPAPER_CROP_TYPE, "single");
                 if ("single".equals(mCropType)) {
                     Uri uri = intent.getData();
                     if (uri != null) {
                    	 String urlContent = uri.toSafeString();
                    	 if(urlContent != null && urlContent.startsWith("content://")){
                    		 mImageList.add(urlContent);
                    	 }else{
                    		 mImageList.add(uriToPath(uri));
                    	 }
                    	 
     				}
                 }else {
                	 ArrayList<Uri> list = intent.getParcelableArrayListExtra("images");
                	 if(list.size() > 0){
                		 for(Uri uri : list){
                			 mImageList.add(uriToPath(uri));
                		 }
                	 }
 				}
             } else if (intent.getData() != null && intent.getData().getScheme().equals("file")) {
                 mCropType = "single";
                 Uri uri = intent.getData();
                 if (uri != null) {
                	 mImageList.add(uri.getEncodedPath());
 				}
             }
             mView.onWallpaperIntentHandled(mImageList,wallpaperName);
             mWallpaperName = getWallpaperName();
         }
	}

	public void setImageList(List<String> images){
		mImageList = images;
	}
	
	@Override
	public void cropWallpaper(String wallpaperName,int currentItem, List<String> images) {
		// TODO Auto-generated method stub
		String fileName = "";
        if (currentItem < 9) {
            fileName = "data0" + (currentItem + 1);
        } else {
            fileName = "data" + (currentItem + 1);
        }
        if(Config.DEBUG){
        	Log.d(TAG, "fileName=" + fileName);
        }
        if(currentItem > MAX_KEYGUARD_WALLPAPER_COUNT){
        	return;
        }
        SaveKeyguardWallpaperTask task = new SaveKeyguardWallpaperTask(currentItem);
        task.execute(fileName);
        
        
	}

	@Override
	public void updateView(Wallpaper data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateView(List<Wallpaper> wallpaperList) {
		// TODO Auto-generated method stub
		
	}
	
	public void setWallpaperName(String name){
		mWallpaperName = name;
	}
	
	
	public String getWallpaperName(){
        
        List<Wallpaper> wallpapers = mPresenter.queryAll();
        StringBuffer name = new StringBuffer(Config.WallpaperStored.DEFAULT_KEYGUARD_FILE_NAME);
        int id = 1;
        if (wallpapers != null && wallpapers.size() != 0) {
            String wallpaperName = wallpapers.get(wallpapers.size() - 1).name;
            if (wallpaperName.contains(Config.WallpaperStored.DEFAULT_KEYGUARD_FILE_NAME)) {
				int currentNumber = Integer.valueOf(wallpaperName.replace(Config.WallpaperStored.DEFAULT_KEYGUARD_FILE_NAME, ""));
				id = currentNumber + 1;
				
			} else {
				id = wallpapers.get(wallpapers.size() - 1).id + 1;
			}
            
        }
        if (id < 10) {
            name.append("0").append(id);
        } else {
        	name.append(id);
        }
        return name.toString();
    }

	
	public void setFromTheme(){
		mFromTheme = true;
	}
	
	
	
	
	
    private String uriToPath(Uri uri) {
        String path = uri.getPath();
        try {
            Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            // 第一行第二列保存路径strRingPath
            cursor.moveToFirst();
            path = cursor.getString(1);
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }
	
    class SaveKeyguardWallpaperTask extends AsyncTask<String, Integer, Boolean>{

    	private int mCurrentItem;
    	private Bitmap mBitmap;
    	public SaveKeyguardWallpaperTask(int currentItem){
    		this.mCurrentItem = currentItem;
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		// TODO Auto-generated method stub
    		super.onPreExecute();
    		View view = mView.getItemView(mCurrentItem);
			if (view != null) {
				CropImageView cropImageView = (CropImageView) view.findViewById(R.id.wallpaper_crop_item);
				mBitmap = cropImageView.getCropImage();
			}
    	}
    	
    	
        @Override
        protected Boolean doInBackground(String... params) {
            boolean bool = saveImageFile(params[0],mCurrentItem);
            String wallpaperName = mWallpaperName;
            if (mImageList != null && (mImageList.size() < 2 || mCurrentItem == (mImageList.size() - 1))) {
                SharePreference.setStringPreference(mContext, Config.WallpaperStored.CURRENT_KEYGUARD_WALLPAPER, wallpaperName);
                
                WallpaperDbController mWallpaperDbController = new WallpaperDbController(mContext);
                Wallpaper wallpaper = mWallpaperDbController.queryKeyguardWallpaperByName(wallpaperName);
                SharePreference.setIntPreference(mContext, Config.WallpaperStored.CURRENT_KEYGUARD_WALLPAPER_ID, wallpaper.id);
                if (wallpaper.isTimeBlack == 0) {
                	SharePreference.setStringPreference(mContext, Config.WallpaperStored.CURRENT_KEYGUARD_GROUP_TIME_BLACK, "false");
    			} else {
    				SharePreference.setStringPreference(mContext, Config.WallpaperStored.CURRENT_KEYGUARD_GROUP_TIME_BLACK, "true");
    			}
                if (wallpaper.isStatusBarBlack== 0) {
                	SharePreference.setStringPreference(mContext, Config.WallpaperStored.CURRENT_KEYGUARD_GROUP_STATUS_BLACK, "false");
    			} else {
    				SharePreference.setStringPreference(mContext, Config.WallpaperStored.CURRENT_KEYGUARD_GROUP_STATUS_BLACK, "true");
    			}
                mWallpaperDbController.close();
                
                String currentPath = WallpaperManager.getCurrentKeyguardPaperPath(mContext, wallpaperName);
                
                boolean res = FileUtils.copyFile(currentPath, Config.WallpaperStored.KEYGUARD_WALLPAPER_PATH, mContext);
                Log.d(TAG, "set keyguard wallpaper success =" + res);
                bool = true;
            } else {
                Log.d(TAG, (mImageList == null) +" = onPostExecute: next = " + mCurrentItem);
                bool = false;
            }
            
            return bool;
        }
        

   	 private boolean saveImageFile(String fileName,int currenItem) {
   			boolean success = false;
   			if(Config.DEBUG){
				Log.d(TAG, "saveImageFile  --->"+fileName);
			}
   			String wallpaperName = mWallpaperName;
   			Bitmap bitmap = mBitmap;
   			if(bitmap == null && mFromTheme){
   				int reqWidth = mContext.getResources().getDisplayMetrics().widthPixels;
   				int reqHeight = mContext.getResources().getDisplayMetrics().heightPixels;
   				bitmap = ImageResizer.decodeSampledBitmapFromDescriptor(mImageList.get(currenItem), reqWidth, reqHeight, null);
   			}
   			if (bitmap != null) {
   				WallpaperDbController control = new WallpaperDbController(mContext);
   				if(mFromTheme){
   					List<Wallpaper> wallpapers = mPresenter.queryAll();
   					ArrayList<Wallpaper> wallpapersFromTheme = new ArrayList<Wallpaper>();
   					if(wallpapers != null && wallpapers.size() > 0){
   						for(Wallpaper w : wallpapers){
   							if(w.isFromTheme == 1){
   								wallpapersFromTheme.add(w);
   							}
   						}
   					}
   					if(wallpapersFromTheme.size() > 0){
   			            WallpaperDbController dbControl = new WallpaperDbController(mContext);
   			            String path = null;
   			            for(Wallpaper w:wallpapersFromTheme){
   			            	path = Config.WallpaperStored.DEFAULT_SDCARD_KEYGUARD_WALLPAPER_PATH + w.name;
   			                FileUtils.deleteDirectory(path);
   			                dbControl.deleteWallpaperByName(w.name);
   			            }
   			            dbControl.close();
   			            if(!TextUtils.isEmpty(path)){
   			            	CommonUtil.sendScanFileBroadcast(mContext, path);
   			            }
   					}
   				}
   				StringBuffer path = new StringBuffer(Config.WallpaperStored.DEFAULT_SDCARD_KEYGUARD_WALLPAPER_PATH);
   				path.append(wallpaperName).append("/").append(fileName).append(".png");
   				success = FileUtils.writeImage(bitmap, path.toString(), 100);
   				
   				StringBuffer filePath = new StringBuffer(Config.WallpaperStored.DEFAULT_SDCARD_KEYGUARD_WALLPAPER_PATH);
   				filePath.append(wallpaperName).append("/").append(Config.WallpaperStored.KEYGUARD_SET_FILE);
   				
   				WallpaperThemeInfo wallpaperInfo = new WallpaperThemeInfo();
   				wallpaperInfo.name = wallpaperName;
   				String defaultGroup = Config.WallpaperStored.DEFAULT_KEYGUARD_GROUP;
   	            wallpaperInfo.timeBlack = "false";
   	            wallpaperInfo.isFromTheme = mFromTheme?1:0;
   				String fileString = WallpaperConfigUtil.creatWallpaperConfigurationXmlFile(filePath.toString(), wallpaperInfo);
   				
   				if (mImageList != null) {
   					mWallpaperManager.updateKeyguardWallpaper(control, wallpaperInfo, mImageList.size());
   					
   					mWallpaperManager.updateKeyguardWallpaperThemeInfo(control, wallpaperName, fileName, path.toString());
   					
   				}
   				control.close();
   				if (bitmap != null) {
   					if (!bitmap.isRecycled()) {
   						bitmap.recycle();
   					}
   					bitmap = null;
   				}
   			}
   			return success;
   		}

        protected void onPostExecute(Boolean result) {
        	if(result && !mFromTheme){
        		Toast.makeText(mContext, R.string.wallpaper_set_success, Toast.LENGTH_SHORT).show();
        	}
            mView.refreshStatus((mImageList.size() -1) == mCurrentItem);
        
        }
    }
	
}
