package com.mst.wallpaper.presenter;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.mst.wallpaper.db.SharePreference;
import com.mst.wallpaper.db.WallpaperDbController;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.object.WallpaperImageInfo;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.FileUtils;

public class KeyguardWallpaperDbPresenterImpl implements WallpaperDatabaseContract.Presenter {

	
	private WallpaperDatabaseContract.View mView;
	
	private Context mContext;
	
	private WallpaperDbController mDbController;
	
	public KeyguardWallpaperDbPresenterImpl(WallpaperDatabaseContract.View view,Context context){
		this.mView = view;
		mView.setPresenter(this);
		mContext = context;
		mDbController = new WallpaperDbController(mContext);
	}
	
	
	@Override
	public List<Wallpaper> queryAll() {
		// TODO Auto-generated method stub
		mDbController.openDb(mContext);
		List<Wallpaper> wallpapers = mDbController.queryAllKeyguardWallpapers();
		if(wallpapers != null && wallpapers.size() > 0){
			for(Wallpaper w : wallpapers){
				List<WallpaperImageInfo> imageInfo = mDbController.queryAllImagesByWallpaperId(w.id);
				if(imageInfo != null && imageInfo.size() > 0){
					for(WallpaperImageInfo info : imageInfo){
						w.addPaths(info.getBigIcon());
					}
				}
				
			}
		}
		mView.updateView(wallpapers);
		mDbController.close();
		return wallpapers;
	}

	@Override
	public Wallpaper queryById(int id) {
		// TODO Auto-generated method stub
		mDbController.openDb(mContext);
		Wallpaper wallpaper = mDbController.queryKeyguardWallpaperById(id);
		mView.updateView(wallpaper);
		mDbController.close();
		return wallpaper;
	}

	@Override
	public Wallpaper queryByName(String name) {
		// TODO Auto-generated method stub
		mDbController.openDb(mContext);
		Wallpaper wallpaper = mDbController.queryKeyguardWallpaperByName(name);
		mView.updateView(wallpaper);
		mDbController.close();
		return wallpaper;
	}

	@Override
	public boolean insert(Wallpaper data) {
		// TODO Auto-generated method stub
		mDbController.openDb(mContext);
		
		boolean insert = mDbController.insertWallpaper(data,
				data.systemFlag == Config.WallpaperStored.KEYGUARD_WALLPAPER_IS_SYSTEM);
		mDbController.close();
		return insert;
	}

	@Override
	public void update(Wallpaper wallpaper) {
		// TODO Auto-generated method stub
	}


	@Override
	public void delete(Wallpaper wallpaper) {
		// TODO Auto-generated method stub
		if(wallpaper.type == Wallpaper.TYPE_KEYGUARD){
			deleteKeyguardWallpaper(mContext, wallpaper.name);
		}
		
	}
	
	
    private void deleteKeyguardWallpaper(Context context, String wallpaperName) {
  	  String path = Config.WallpaperStored.DEFAULT_SDCARD_KEYGUARD_WALLPAPER_PATH + wallpaperName;
        FileUtils.deleteDirectory(path);
        mDbController .openDb(mContext);
        Wallpaper wallpaper = mDbController.queryKeyguardWallpaperByName(wallpaperName);
        if(wallpaper != null){
        	mDbController.deleteWallpaperByName(wallpaperName);
        }
        
        mDbController.close();
  }
	
	

}
