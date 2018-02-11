package com.mst.wallpaper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mst.utils.DisplayUtils;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.mst.wallpaper.MainWorker.WallpaperHolder;
import com.mst.wallpaper.db.SharePreference;
import com.mst.wallpaper.db.WallpaperDbColumns.WallpaperColumns;
import com.mst.wallpaper.db.WallpaperDbController;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.utils.CommonUtil;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.FileUtils;
import com.mst.wallpaper.utils.WallpaperConfigUtil;
import com.mst.wallpaper.utils.WallpaperManager;
/**
 *一个全局的子线程，该线程独立于UI线程运行，一些复杂的，不是很耗时的操作都放到这个
 *线程中进行处理，这样避免UI线程的阻塞，也避免了不同场景创建不同线程导致的资源
 *浪费。使用方法如： 
 *<p>
 * MainWorker worker = MainWorker.getInstance();
 * <p>
 * worker.requestSetWallpaper(wallpaperHolder);
 *
 */
public class MainWorker  implements Runnable {
	private static final String NAME = "WallpaperMainWorker";
	private static final String TAG = NAME;
	
	
    private static final Object mLock = new Object();
    private Looper mLooper;
    private static MainWorker mWorker;
    private TaskHandler mTaskHandler;
    
    /**
     *
     * Callback for 
     *
     */
    public interface OnRequestListener<T,D>{
    	public void onSuccess(D data,T t,int statusCode);
    	
    	public void onStartRequest(D data,int statusCode);
    	
    }
    
    private SparseArray<OnRequestListener> mCallbackArray = new SparseArray<MainWorker.OnRequestListener>();
    private HashMap<String,Integer> mCallbackNameArray = new HashMap<String,Integer>();
    private int mCallbackKey = -1;
    
    /**
     * Creates a worker thread with the given name. The thread
     * then runs a {@link android.os.Looper}.
     * @param name A name for the new thread

     */
    private MainWorker(String name) {
        Thread t = new Thread(null, this, name);
        mCallbackArray.clear();
        mCallbackNameArray.clear();
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
        synchronized (mLock) {
            while (mLooper == null) {
                try {
                    mLock.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        
        mTaskHandler = new TaskHandler(getLooper());
    }
    
   
    
    public static final MainWorker getMainWorker(){
    	synchronized (mLock) {
			if(mWorker == null){
				mWorker = new MainWorker(NAME);
			}
			
			return mWorker;
		}
    }
    
    private OnRequestListener getListener(String callbackName){
    	synchronized (mLock) {
    		Integer keyObj = mCallbackNameArray.get(callbackName);
    		if(keyObj != null){
	    		int key = keyObj;
	        	return mCallbackArray.get(key);
    		}else{
    			return null;
    		}
		}
    	
    }
    
    /**
     * 添加一个操作回调到回调队列中
     * @param listener
     * @param callbackName
     */
    public void addRequestListener(OnRequestListener listener,String callbackName){
    	synchronized (mLock) {
    		if( mCallbackNameArray.get(callbackName) == null){
        		mCallbackKey ++;
        		mCallbackNameArray.put(callbackName, mCallbackKey);
        		mCallbackArray.put(mCallbackKey,listener);
        	}
		}
    	
    }
    
	public void removeRequestListener(OnRequestListener listener,String callbackName) {
		// TODO Auto-generated method stub
		synchronized (mLock) {
			if(mCallbackNameArray.get(callbackName) != null){
				mCallbackNameArray.remove(callbackName);
			}
			if(mCallbackArray.indexOfValue(listener) != -1){
				mCallbackArray.removeAt(mCallbackArray.indexOfValue(listener));
			}
			mCallbackKey --;
		}
		
	}


    
    public Looper getLooper() {
        return mLooper;
    }
    
    public void run() {
        synchronized (mLock) {
            Looper.prepare();
            mLooper = Looper.myLooper();
            mLock.notifyAll();
        }
        Looper.loop();
    }
    
    public void quit() {
        mLooper.quit();
        mCallbackArray.clear();
        mCallbackNameArray.clear();
        mCallbackKey = -1;
    }
    
    /**
     * 发起设置壁纸的请求，不管是设置桌面壁纸还是设置锁屏壁纸都调用
     * 这个接口。该方法在UI线程中运行，它会将主要操作传递到子线程中
     * 去，让子线程执行
     * @param wallpaper
     * @param request
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public final  void requestSetWallpaper(WallpaperHolder holder){
    	holder.context = mContext;
    	OnRequestListener listener = getListener(holder.callbackName);
    	if(listener == null){
				return;
    	}
    	
    	if(holder.wallpaper == null ||holder. wallpaper.getWallpaperCount() == 0){
    			listener.onSuccess(holder.wallpaper, null, Config.SetWallpaperStatus.STATUS_WALLPAPER_EMPTY);
    			return;
    	}
    	if(wallpaperWasApply(holder.wallpaper, holder.context, holder.position)){
    			listener.onSuccess(holder.wallpaper, null, Config.SetWallpaperStatus.STATUS_WALLPAPER_APPLIED);
    			return;
    	}
    	if(Config.DEBUG){
			Log.d(TAG, "request wallpaper Type--->"+holder.wallpaper.type);
		}
    	switch (holder.wallpaper.type) {
		case Wallpaper.TYPE_DESKTOP:
			sendMessage(TaskHandler.MSG_SET_DESKTOP_WALLPAPER, holder);
			break;
		case Wallpaper.TYPE_KEYGUARD:
			sendMessage(TaskHandler.MSG_SET_KEYGUARD_WALLPAPER, holder);
			break;
		case Wallpaper.TYPE_OTHER:
			listener.onSuccess(holder.wallpaper, null, Config.SetWallpaperStatus.STATUS_UNKOWNE_WALLPAPER_TYPE);
			break;

		default:
			listener.onSuccess(holder.wallpaper, null, Config.SetWallpaperStatus.STATUS_FAILED);
			break;
		}
    	
    }
    
	public void requestLoadDesktopWallpaper(WallpaperHolder holder) {
		// TODO Auto-generated method stub
		holder.context = mContext;
		sendMessage(TaskHandler.MSG_LOAD_DESKTOP_WALLPAPER, holder);
	}
    
	public void requestDeleteWallpaper(WallpaperHolder holder) {
		// TODO Auto-generated method stub
		holder.context = mContext;
		sendMessage(TaskHandler.MSG_DELETE_WALLPAPER, holder);
	}

 
    
    private void sendMessage(int what, Object obj) {
        sendMessage(what, obj, 0, 0, false);
    }

    private void sendMessage(int what, Object obj, int arg1) {
        sendMessage(what, obj, arg1, 0, false);
    }

    private void sendMessage(int what, Object obj, int arg1, int arg2) {
        sendMessage(what, obj, arg1, arg2, false);
    }

    private void sendMessage(int what, Object obj, int arg1, int arg2, boolean async) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (async) {
            msg.setAsynchronous(true);
        }
        mTaskHandler.sendMessage(msg);
    }
    
	private boolean wallpaperWasApply(Wallpaper wallpaper, Context context,
			int position) {
		if (wallpaper.type == Wallpaper.TYPE_DESKTOP) {
			final int usedPosition = SharePreference.getIntPreference(context,
					SharePreference.KEY_SELECT_DESKTOP_POSITION, -1);
			final String usedPath = SharePreference.getStringPreference(
					context, SharePreference.KEY_SELECT_DESKTOP_PATH, "");
			final String path = wallpaper.getPathByKey(0);
			if (position == usedPosition || usedPath.equals(path)) {

				return true;
			}
		} else if (wallpaper.type == Wallpaper.TYPE_KEYGUARD) {
			int appliedId = SharePreference.getIntPreference(context, Config.WallpaperStored.CURRENT_KEYGUARD_WALLPAPER_ID, -1);
			String appliedName = SharePreference.getStringPreference(context, Config.WallpaperStored.CURRENT_KEYGUARD_WALLPAPER, "");
			return appliedId == wallpaper.id && appliedName.equals(wallpaper.name);
		}
		return false;
	}
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void handleSetDesktopWallpaper(WallpaperHolder holder){
    	
		OnRequestListener listener = getListener(holder.callbackName);
    	final Context context = holder.context;
    	final Wallpaper wallpaper = holder.wallpaper;
    	final int position = holder.position;
    	final android.app.WallpaperManager wallpaperManager = android.app.WallpaperManager.getInstance(context);
    	final String path = wallpaper.getPathByKey(0);
		InputStream input = null;
		try {
			if (Config.DEBUG) {
				Log.d(TAG, "setDeskwallpaper---->" + wallpaper.getPathByKey(0));
			}
			input = FileUtils.getStreamFromFile(wallpaper.getPathByKey(0),
					false);
			if (input != null) {
				wallpaperManager.setStream(input);
				input.close();
				input = null;
			}
			input = FileUtils.getStreamFromFile(wallpaper.getPathByKey(0),
					false);
			if (input != null) {
				Bitmap bitmap = BitmapFactory.decodeStream(input);
				input.close();
				input = null;
				int width = DisplayUtils.getWidthPixels(context);
				int height = DisplayUtils.getHeightPixels(context);
				if (bitmap != null) {
					final int wallpaperWidthBefore = bitmap.getWidth();
					final int wallpaperHeightBefore = bitmap.getHeight();
					if (wallpaperWidthBefore < wallpaperHeightBefore) {
						wallpaperManager
								.suggestDesiredDimensions(width, height);
					} else {
						wallpaperManager.suggestDesiredDimensions(2 * width,
								height);
					}
				}
				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
				}
			}
			
			if(listener != null){
				listener.onSuccess(wallpaper, null, Config.SetWallpaperStatus.STATUS_SUCCESS);
			}
			Config.isChangedByLocal = 1;
			Config.isWallPaperChanged = true;
			SharePreference.saveDesktopWallpaper(context, position, path);
		} catch (Exception e) {
			Log.e(TAG, "set wallpaper catched exception:" + e);
			if(listener != null){
				listener.onSuccess(wallpaper, null, Config.SetWallpaperStatus.STATUS_FAILED);
			}
		}
    	
    	
    }
    
    
    @SuppressWarnings("unchecked")
	private void handleSetKeyguardWallpaper(WallpaperHolder holder){
    	if(Config.DEBUG){
			Log.d(TAG, "handleSetKeyguardWallpaper--->"+holder.wallpaper.name);
		}
    	 try {
    	       SharePreference.setStringPreference(holder.context
              		 , Config.WallpaperStored.CURRENT_KEYGUARD_WALLPAPER, holder.wallpaper.name);
               SharePreference.setIntPreference(holder.context
              		 , Config.WallpaperStored.CURRENT_KEYGUARD_WALLPAPER_ID, holder.wallpaper.id);
               
             String currentPath = WallpaperManager.getCurrentKeyguardPaperPath(holder.context, holder.wallpaper.name);
             boolean isCopyRight =  FileUtils.copyFile(currentPath, Config.WallpaperStored.KEYGUARD_WALLPAPER_PATH, holder.context);
             if(getListener(holder.callbackName) != null){
            		 getListener(holder.callbackName).onSuccess(holder.wallpaper,holder.wallpaper.id
            				 ,isCopyRight? Config.SetWallpaperStatus.STATUS_SUCCESS:Config.SetWallpaperStatus.STATUS_FAILED);
             }
             
         } catch (Exception e) {
             e.printStackTrace();
         }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void handleLoadDesktopWallpaper(WallpaperHolder holder){
    	Cursor cursor = null;
    	String sort_desc = "modified ASC";
    	OnRequestListener listener = getListener(holder.callbackName);
    	if(Config.DEBUG){
    		Log.d(TAG, "handleLoadDesktopWallpaper callbackName:"+holder.callbackName+"  callback is NULL:"+(listener == null));
    	}
    	String appliedPath = SharePreference.getStringPreference(holder.context, SharePreference.KEY_SELECT_DESKTOP_PATH, "");
		ArrayList<Wallpaper> wallpapers = new ArrayList<Wallpaper>();
		int systemWallpaperId = 0;
		if(TextUtils.isEmpty(appliedPath)){
			appliedPath = Config.SYSTEM_DESKTOP_DEFAULT_WALLPAPER;
		}
		
		for(String path:Config.LOCAL_DESKTOP_WALLPAPERS){
			Wallpaper wallpaper = new Wallpaper(Wallpaper.TYPE_DESKTOP);
			wallpaper.addPaths(Config.SYSTEM_DESKTOP_WALLPAPER_PATH+path);
			wallpaper.applied =(Config.SYSTEM_DESKTOP_WALLPAPER_PATH+path).equals(appliedPath);
			wallpaper.systemFlag = Wallpaper.FLAG_SYSTEM;
			wallpaper.id = systemWallpaperId;
			systemWallpaperId++;
			wallpapers.add(wallpaper);
		}
		
		try {
            cursor = holder.context.getContentResolver().query(Config.WallpaperStored.LOCAL_WALLPAPER_URI, 
            		new String[] { Config.WallpaperStored.WALLPAPER_FILENAME,Config.WallpaperStored.WALLPAPER_ID }, null, null, sort_desc);
            if ((cursor != null)) {
            	while (cursor.moveToNext() ) {
            		String path = Config.WallpaperStored.WALLPAPER_PATH + cursor.getString(0);
            		int id = cursor.getInt(1);
            		Wallpaper wallpaper = new Wallpaper();
            		if(path.equals(appliedPath)){
            			wallpaper.applied = true;
            		}
            		wallpaper.id = id;
            		wallpaper.addPaths(path);
            		wallpapers.add(wallpaper);
				}
            }
        } catch (Exception e) {
            // Ignore error for lacking the data column from the source.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
		if(Config.DEBUG){
			Log.d(TAG, "loaded Wallpaper:"+wallpapers.size());
		}
		if(listener != null){
			listener.onSuccess(wallpapers, null, Config.LocalWallpaperStatus.STATUS_LOAD_SUCCESS);
		}
    }
    
    private void handleDeleteWallpaper(WallpaperHolder holder){
    	if(holder.type == Wallpaper.TYPE_KEYGUARD){
    		
            WallpaperDbController dbControl = new WallpaperDbController(holder.context);
            String path = null;
            for(Wallpaper w:holder.wallpapers){
            	path = Config.WallpaperStored.DEFAULT_SDCARD_KEYGUARD_WALLPAPER_PATH + w.name;
                FileUtils.deleteDirectory(path);
                dbControl.deleteWallpaperByName(w.name);
            }
            dbControl.close();
            if(!TextUtils.isEmpty(path)){
            	CommonUtil.sendScanFileBroadcast(mContext, path);
            }
    	}else{
    		List<Wallpaper> wallpapers = holder.wallpapers;
        	ContentResolver resolver = holder.context.getContentResolver();
        	if(resolver == null){
        		return;
        	}
        	for(Wallpaper w:wallpapers){
        		String args = String.valueOf(w.id);
        		resolver.delete(Config.WallpaperStored.LOCAL_WALLPAPER_URI, 
        				Config.WallpaperStored.WALLPAPER_ID+"=?",new String[]{args});
        		String filePath = w.getPathByKey(0);
        		if(!TextUtils.isEmpty(filePath)){
        			FileUtils.deleteFile(filePath);
        			CommonUtil.sendScanFileBroadcast(mContext, filePath);
        		}
        	}
    	}
    	
    }
    
    
    private class TaskHandler extends Handler{
    	public static final int MSG_SET_DESKTOP_WALLPAPER = 100;
    	public static final int MSG_SET_KEYGUARD_WALLPAPER = 101;
    	public static final int MSG_LOAD_DESKTOP_WALLPAPER = 102;
    	public static final int MSG_DELETE_WALLPAPER = 103;

		public TaskHandler(Looper looper) {
			// TODO Auto-generated constructor stub
			super(looper);
		}
		
		
    	
    	@Override
    	public void handleMessage(Message msg) {
    		
    		WallpaperHolder holder;
    		
    		switch (msg.what) {
			case MSG_SET_DESKTOP_WALLPAPER:
				holder = (WallpaperHolder) msg.obj;
				handleSetDesktopWallpaper(holder);
				break;
			case MSG_SET_KEYGUARD_WALLPAPER:
				holder = (WallpaperHolder) msg.obj;
				handleSetKeyguardWallpaper(holder);
				break;
			case MSG_LOAD_DESKTOP_WALLPAPER:
				holder = (WallpaperHolder) msg.obj;
				handleLoadDesktopWallpaper(holder);
				break;
			case MSG_DELETE_WALLPAPER:
				holder = (WallpaperHolder)msg.obj;
				handleDeleteWallpaper(holder);
				break;

			}
    	}
    	
    	
    }
    
    private Context mContext;
    
    public void setContext(Context context){
    	mContext = context;
    }
    
    
    /**
     * 该类主要是便于调用方和MainWorker之间进行数据
     * 传递而定义的一个数据封装
     *
     */
    public static  class WallpaperHolder{
    	public List<Wallpaper> wallpapers;
    	public Wallpaper wallpaper;
    	public int position;
    	public Context context;
    	public String callbackName;
    	
    	public int type  = Wallpaper.TYPE_OTHER;
    	
    	
    }












    
    
    
    
    
    
    
    
    
    
    
}