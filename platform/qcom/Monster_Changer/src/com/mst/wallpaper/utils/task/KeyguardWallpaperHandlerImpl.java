package com.mst.wallpaper.utils.task;

import java.util.ArrayList;
import java.util.List;

import com.mst.wallpaper.adapter.WallpaperCropAdapter;
import com.mst.wallpaper.db.SharePreference;
import com.mst.wallpaper.db.WallpaperDbController;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.object.WallpaperThemeInfo;
import com.mst.wallpaper.presenter.WallpaperDatabaseContract;
import com.mst.wallpaper.presenter.KeyguardWallpaperDbPresenterImpl;
import com.mst.wallpaper.presenter.WallpaperDatabaseContract.Presenter;
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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.mst.wallpaper.R;

public class KeyguardWallpaperHandlerImpl implements KeyguardWallpaperHandler, WallpaperDatabaseContract.View{

	
	private static final String TAG = "KeyguardWallpaperHandlerImpl";

	private KeyguardWallpaperHandlerView mView;
	
	private WallpaperDatabaseContract.Presenter mPresenter;
	
	private WallpaperManager mWallpaperManager;
	
	private String mCropType;
	
	private Context mContext;
	
	private List<String> mImageList = new ArrayList<String>();
	
	private String mWallpaperName;
	
	
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
                    	 mImageList.add(uriToPath(uri));
     				}
                 }else {
                	 ArrayList<Uri> list = intent.getParcelableArrayListExtra("images");
                	 Log.d("picker", "size-->"+list.size());
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
	
	
	private String getWallpaperName(){
        
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
            return bool;
        }
        

   	 private boolean saveImageFile(String fileName,int currenItem) {
   			boolean success = false;
   			Log.d(TAG, "saveImageFile=" + fileName);
   			String wallpaperName = mWallpaperName;
   			View view = mView.getItemView(currenItem);
   			Bitmap bitmap = mBitmap;
   			if (bitmap != null) {
   				WallpaperDbController control = new WallpaperDbController(mContext);
   				StringBuffer path = new StringBuffer(Config.WallpaperStored.DEFAULT_SDCARD_KEYGUARD_WALLPAPER_PATH);
   				path.append(wallpaperName).append("/").append(fileName).append(".png");
   				success = FileUtils.writeImage(bitmap, path.toString(), 100);
   				
   				StringBuffer filePath = new StringBuffer(Config.WallpaperStored.DEFAULT_SDCARD_KEYGUARD_WALLPAPER_PATH);
   				filePath.append(wallpaperName).append("/").append(Config.WallpaperStored.KEYGUARD_SET_FILE);
   				
   				WallpaperThemeInfo mThemeInfo = new WallpaperThemeInfo();
   				mThemeInfo.name = wallpaperName;
   				String defaultGroup = Config.WallpaperStored.DEFAULT_KEYGUARD_GROUP;
   	            mThemeInfo.timeBlack = "false";
   				String fileString = WallpaperConfigUtil.creatWallpaperConfigurationXmlFile(filePath.toString(), mThemeInfo);
   				
   				if (mImageList != null) {
   					mWallpaperManager.updateKeyguardWallpaper(control, mThemeInfo, mImageList.size());
   					
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
            Log.d(TAG, "onPostExecute=" + result);
            boolean saved;
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
                Toast.makeText(mContext, R.string.wallpaper_set_success, Toast.LENGTH_SHORT).show();
                saved = true;
            } else {
                Log.d(TAG, (mImageList == null) +" = onPostExecute: next = " + mCurrentItem);
            }
            
            mView.refreshStatus((mImageList.size() -1) == mCurrentItem);
        
        }
    }
	
}
