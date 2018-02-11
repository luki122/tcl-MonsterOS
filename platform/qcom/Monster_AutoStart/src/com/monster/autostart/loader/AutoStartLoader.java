package com.monster.autostart.loader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;


import com.monster.autostart.AppManagerApplication;
import com.monster.autostart.bean.AppInfo;
import com.monster.autostart.bean.AppManagerState;
import com.monster.autostart.bean.BroadcastSolution;
import com.monster.autostart.db.MulwareProvider;
import com.monster.autostart.db.MulwareProvider.MulwareTable;
import com.monster.autostart.interfaces.IAppsChangeCallBack;
import com.monster.autostart.interfaces.IBaseSolution;
import com.monster.autostart.utils.DeferredHandler;
import com.monster.autostart.utils.Utilities;
import com.monster.autostart.R;
public class AutoStartLoader implements IAppsChangeCallBack {

	Context sContext;

	List<AppInfo> sBgApplist;
	List<AppInfo> sBgAddedList;

	final Object mLock = new Object();

	static final boolean DEBUG = true;

	static final String TAG = "AutoStartLoader";

	static final HandlerThread sWorkerThread = new HandlerThread(
			"appmanager-loader");

	static {
		sWorkerThread.start();
	}

	DeferredHandler mHandler = new DeferredHandler();

	WeakReference<Callbacks> mCallbacks;

	public interface Callbacks {
		public void bintItems();
	}

	static final Handler sWorker = new Handler(sWorkerThread.getLooper());
 
	public void runOnMainThread(Runnable r) {
		if (sWorkerThread.getThreadId() == Process.myTid()) {
			// If we are on the worker thread, post onto the main handler
			mHandler.post(r);
		} else {
			r.run();
		}
	}

	public void runOnWorkerThread(Runnable r) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            // If we are not on the worker thread, then post to the worker handler
            sWorker.post(r);
        }
    }

	
	public AutoStartLoader(Context c) {
		// TODO Auto-generated constructor stub
		sContext = c;
	}

	public void startLoader() {
		synchronized (mLock) {
			sWorkerThread.setPriority(Thread.MAX_PRIORITY);
			sWorker.post(new LoaderTask());
		}

	}

	public void initialize(Callbacks callbacks) {
		synchronized (mLock) {
			mCallbacks = new WeakReference<Callbacks>(callbacks);
		}
	}

	public Callbacks getCallback() {
		return mCallbacks != null ? mCallbacks.get() : null;
	}

	private class LoaderTask implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			sBgApplist = queryAppInfoList();

			AppManagerState mState = AppManagerState.getInstance();

			if (sBgApplist.size() <= 0) {
				List<IBaseSolution> solution = mState.getSolution();
				for (IBaseSolution cl : solution) {
					sBgApplist = cl.filter();
					mState.getAppProvider().insert(sBgApplist);
				}
             /**M:Hazel add for configure white list content begin at 2016-10-24 begin*/
				Resources res = sContext.getResources();
				configWBList(MulwareProvider.MulwareTable.TABLE_WHITELIST_NAME,
						res.getStringArray(R.array.white_list_content),
						MulwareProvider.MulwareTable.CONTENT_WL_URI);
				
				configWBList(MulwareProvider.MulwareTable.TABLE_BLACKLIST_NAME,
						res.getStringArray(R.array.black_list_content),
						MulwareProvider.MulwareTable.CONTENT_BL_URI);
		     /**M:Hazel add for configure white list content begin at 2016-10-24 end*/
			}else
				Log.e(Utilities.TAG, "_CLS_:"+"AutoStartLoader"+";"+"_FUNCTION_:"+"LoaderTask" +"sBgApplist.size>0");
				//print(sBgApplist);
 
			final Runnable r = new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Callbacks callbacks = getCallback();
					if(callbacks!=null){
						callbacks.bintItems();
					}else{
						Log.e(Utilities.TAG, "#No need to bind items!");
					}
				}
			};

			runOnMainThread(r);
		}

	}

	public List<AppInfo> queryAppInfoList() {
		List<AppInfo> list = new ArrayList<AppInfo>();
		ContentResolver cr = sContext.getContentResolver();
		Cursor c = cr.query(MulwareTable.CONTENT_AUTO_APP_URI, null, null,
				null, null);
		if (c != null) {
			try {
				while (c.moveToNext()) {
					AppInfo info = new AppInfo();

					String intentDescription = c
							.getString(c
									.getColumnIndex(MulwareProvider.MulwareTable.INTENT));
					Intent intent = Intent.parseUri(intentDescription, 0);
					info.setIntent(intent);

					/**
					 * M:Hazel parser title and never read title from database
					 * again begin
					 */
					String title = Utilities.getInstance().getTitle(sContext,
							intent);
					info.setTitle(title);
					/**
					 * M:Hazel parser title and never read title from database
					 * again end
					 */

					info.setStatus(c.getInt(c
							.getColumnIndex(MulwareProvider.MulwareTable.STATUS)));

					list.add(info);

				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				c.close();
			}
		}

		return list;

	}

	void print(List<AppInfo> list) {
		if (DEBUG) {
			for (AppInfo info : list) {
				Log.e(Utilities.TAG,
						"list.size=" + list.size() + ";" + info.toString());
			}
		}

	}

	@Override
	public void onPackageRemoved(String packageName) {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:" + "AutoStartLoader" + ";" + "_FUNCTION_:"
				+ "onPackageRemoved");
		int op = PackageUpdatedTask.OP_REMOVE;
		enqueuePackageUpdated(new PackageUpdatedTask(op,
				new String[] { packageName }));

	}

	@Override
	public void onPackageAdded(String packageName) {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:" + "AutoStartLoader" + ";" + "_FUNCTION_:"
				+ "onPackageAdded");
		int op = PackageUpdatedTask.OP_ADD;
		enqueuePackageUpdated(new PackageUpdatedTask(op,
				new String[] { packageName }));
	}

	@Override
	public void onPackageChanged(String packageName) {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:" + "AutoStartLoader" + ";" + "_FUNCTION_:"
				+ "onPackageChanged"+"name="+packageName);
		AppManagerState state = AppManagerState.getInstance();
	}

	@Override
	public void onPackagesAvailable(String[] packageNames, boolean replacing) {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:" + "AutoStartLoader" + ";" + "_FUNCTION_:"
				+ "onPackagesAvailable");
	}

	@Override
	public void onPackagesUnavailable(String[] packageNames, boolean replacing) {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:" + "AutoStartLoader" + ";" + "_FUNCTION_:"
				+ "onPackagesUnavailable");
	}

	void enqueuePackageUpdated(PackageUpdatedTask task) {
		sWorker.post(task);
	}

	private class PackageUpdatedTask implements Runnable {

		public static final int OP_NONE = 0;
		public static final int OP_ADD = 1;
		public static final int OP_UPDATE = 2;
		public static final int OP_REMOVE = 3;
		public static final int OP_UNAVAILABLE = 4;

		int mOp;
		String[] mPackages;

		public PackageUpdatedTask(int op, String[] packages) {
			mOp = op;
			mPackages = packages;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			final String[] packages = mPackages;
			final int N = packages.length;

			switch (mOp) {

			case OP_ADD:
				for (int i = 0; i < N; i++) {
					addPackage(packages[i]);
				}
				break;

			case OP_UPDATE:
				break;

			case OP_REMOVE:
				for (int i = 0; i < N; i++) {
					removePackage(packages[i]);
				}
				break;
			default:
				break;
			}

			final Callbacks callbacks = getCallback();

			mHandler.post(new Runnable() {
				public void run() {
					Callbacks cb = getCallback();
					if (callbacks == cb && cb != null) {
						callbacks.bintItems();
					}
				}
			});
		}

	}

	public void addPackage(String packageName) {
     
		PackageManager pm = sContext.getPackageManager();

		AppManagerState state = AppManagerState.getInstance();

		List<IBaseSolution> solution = state
				.getSolution();
		
		for (IBaseSolution cl : solution) {
			List<AppInfo> result = cl.deteced(packageName);

			if(result.size()>0){
				state.getAppProvider().insert(result);
			}
		}
	}

	public void removePackage(String packageName) {
		Log.e("sunset", "removePackage=" + packageName);
		/** Note:sBgAddedList will be invalidation when kill this application. */
		sBgAddedList = queryAppInfoList();
		AppManagerState state = AppManagerState.getInstance();
		if (sBgAddedList != null && sBgAddedList.size() > 0) {
			for (int i = sBgAddedList.size() - 1; i >= 0; i--) {
				AppInfo info = sBgAddedList.get(i);
				final ComponentName component = info.intent.getComponent();
				if (packageName.equals(component.getPackageName())) {
					sBgAddedList.remove(i);
					state.getAppProvider().delete(info.getIntent());
				}
			}
		}
	}

	
	public List<AppInfo> getAppsList(){
		return this.sBgApplist;
	}
	
	/**M:Hazel add for configure white list content begin at 2016-10-24 begin*/
	private void configWBList(String table,String[] actions,Uri url) {
		List<String> l = queryWhiteList(url);
		AppManagerState state = AppManagerState.getInstance();
		Resources res = sContext.getResources();
		if(l.size()<=0){
			//String[] actions = res.getStringArray(R.array.white_list_content);
			for(String content :actions){
				state.getAppProvider().insert(content,table);
			}
		}
	}

	private List<String> queryWhiteList(Uri url) {
		List<String> list = new ArrayList<String>();
		ContentResolver cr = sContext.getContentResolver();
		Cursor c = cr
				.query(url, null, null, null, null);
		if (c != null) {
			try {
				while (c.moveToNext()) {
					String wlDescription = c
							.getString(c
									.getColumnIndex(MulwareProvider.MulwareTable.WBLIST_CONTENT));

					list.add(wlDescription);

				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				c.close();
			}
		}
		return list;
	}
	/**M:Hazel add for configure white list content begin at 2016-10-24 END*/
}
