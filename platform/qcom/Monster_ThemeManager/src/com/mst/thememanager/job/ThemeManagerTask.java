package com.mst.thememanager.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.util.Log;

import com.mst.thememanager.database.DatabaseFactory;
import com.mst.thememanager.database.SharePreferenceManager;
import com.mst.thememanager.database.ThemeDatabaseController;
import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.job.ThreadPool.Job;
import com.mst.thememanager.listener.OnDatabaseThemeLoadedListener;
import com.mst.thememanager.listener.OnThemeApplyListener;
import com.mst.thememanager.listener.OnThemeLoadedListener;
import com.mst.thememanager.parser.LocalThemeParser;
import com.mst.thememanager.parser.ThemeParser;
import com.mst.thememanager.utils.ArrayUtils;
import com.mst.thememanager.utils.Config;
import com.mst.thememanager.utils.TLog;

public class ThemeManagerTask  implements Task{
	private static final String TAG = "ThemeManagerTask";

    
	private OnThemeLoadedListener mThemeLoadListener;
	
	private boolean mLoadSuccess = false;


	private String mThemePath;

	private Object mLock = new Object();
	
	private Context mContext;
	private MultiTaskDealer mApplyDealer;
	private volatile int mApplyStatus;
	private ThemeApplyTask mApplyTask;
	public ThemeManagerTask(Context context){
		mContext = context;
		mApplyDealer = new MultiTaskDealer("apply_theme", 1);
		mApplyTask = new ThemeApplyTask();
	}

	@Override
	public boolean applyTheme(Theme theme, Context context,OnThemeApplyListener listener) {
		// TODO Auto-generated method stub
		if(theme == null){
			return false;
		}
		
		mApplyTask.setTheme(theme);
		mApplyTask.setContext(context);
		mApplyTask.setApplyListener(listener);
		mApplyDealer.addTask(mApplyTask);
		return false;
	}

	@Override
	public boolean themeApplied(Theme theme) {
		// TODO Auto-generated method stub
		
		if(theme == null){
			return false;
		}
		
		return getAppliedThemeId(mContext) == theme.id;
	}

	@Override
	public int getAppliedThemeId(Context context) {
		// TODO Auto-generated method stub
		return SharePreferenceManager.getIntPreference(context, SharePreferenceManager.KEY_APPLIED_THEME_ID, -1);
	}

	@Override
	public int getAppliedWallpaperId(Context context) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAppliedFontsId(Context context) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAppliedRingTongId(Context context) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean updateThemeFromInternet(Theme theme) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateThemeinDatabase(Theme theme) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deleteTheme(Theme theme) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteTheme(List<Theme> themes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadThemes(int themeType) {
		// TODO Auto-generated method stub
		if(themeType == Theme.THEME_NULL){
			return;
		}
		final int type = themeType;
		MultiTaskDealer dealer = new MultiTaskDealer("load_theme", 1);
		final ThemeDatabaseController<Theme> dbController = DatabaseFactory.createDatabaseController(themeType, mContext);
		Runnable loadThead = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				List<Theme> themes = dbController.getThemesByType(type);
				Theme defaultTheme = new Theme();
				defaultTheme.type = Theme.THEME_PKG;
				defaultTheme.id = Config.DEFAULT_THEME_ID;
				if(mThemeLoadListener != null){
						mThemeLoadListener.onThemeLoaded(true, defaultTheme);
				}
				if(themes != null && themes.size() > 0){
					if(mThemeLoadListener != null){
						for(Theme t:themes){
							mThemeLoadListener.onThemeLoaded(true, t);
						}
					}
				}
			}
		};
		dealer.addTask(loadThead);
	}

	@Override
	public void loadTheme(String themePath, int themeType) {
		// TODO Auto-generated method stub
		synchronized (mLock) {
			LocalThemeLoader loader = new LocalThemeLoader(new LocalThemeLoaderBridge(themePath));
			loader.startLoad();
			mThemePath = themePath;
			mLoadSuccess = false;
		}

	}


	@Override
	public void setThemeLoadListener(OnThemeLoadedListener listener) {
		// TODO Auto-generated method stub
		mThemeLoadListener = listener;
	}

	
	
	
	
	class LocalThemeLoaderBridge {

		LocalThemeLoader mThemeLoader;
		public Theme theme; 
		private String mPath;
		public  LocalThemeLoaderBridge(String path) {
			// TODO Auto-generated constructor stub
			mPath = path;
			theme = new Theme();
			theme.themeFilePath = path;
		}
		public void onLoadComplete(Theme theme) {
			// TODO Auto-generated method stub
			mThemeLoadListener.onThemeLoaded(mLoadSuccess, theme);
		}

		public String getPath() {
			// TODO Auto-generated method stub
			return mPath;
		}
		
	}



	@Override
	public void loadSystemTheme(int themeType) {
		// TODO Auto-generated method stub
		final int type = themeType;
		MultiTaskDealer dealer = new MultiTaskDealer(MultiTaskDealer.LOAD_THEME_FROM_DATABASE_TASK, 2);
		Runnable loadThread = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				ThemeDatabaseController<Theme> dbController = DatabaseFactory.createDatabaseController(type, mContext);
				ArrayList<Theme> themes = new ArrayList<Theme>();
				int count = dbController.getCount();
				if(count == 0){
					File systemDirFile = new File(Config.SYSTEM_THEME_LOADED_DIR);
					File[] themeFiles = systemDirFile.listFiles();
					if(ArrayUtils.isEmpty(themeFiles)){
						return;
					}
					ThemeParser<Theme, InputStream> parser = new LocalThemeParser();
					for(File file:themeFiles){
						String parentPath = file.getAbsolutePath();
						String descriptionXml = parentPath+File.separatorChar+Config.LOCAL_THEME_DESCRIPTION_FILE_NAME;
						try{
						FileInputStream descriptionStream = new FileInputStream(descriptionXml);
						Theme theme = parser.parser(descriptionStream);
						if(theme != null){
							 theme.loadedPath = parentPath;
							 theme.loaded = true;
							 theme.applyStatus = false;
							 theme.lastModifiedTime = System.currentTimeMillis();
							 theme.themeFilePath = Config.SYSTEM_THEME_DIR+theme.name;
							 dbController.insertTheme(theme);
						}
						}catch(Exception e){
							//do nothing
							Log.e(TAG, "load system theme catch exception-->"+e);
						}
					}
				}
			}
		};
		dealer.addTask(loadThread);
	}
	
	
	
}
	
	
