package com.monster.autostart.bean;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.monster.autostart.db.MulwareProvider;
import com.monster.autostart.interfaces.IBaseSolution;
import com.monster.autostart.loader.AutoStartLoader;

public class AppManagerState {

	static AppManagerState sInstance;

	static Context sContext;

	AutoStartLoader sLoader;
 
	AppsChangeController sAppsChangeController;
	
	List<IBaseSolution> sConfigList = new ArrayList<IBaseSolution>();

	private static WeakReference<MulwareProvider> sLauncherProvider;
	
	public AppManagerState() {
		// TODO Auto-generated constructor stub
		setSolution();
		setLoader();
		setAppsChangeController();
		
		sLoader.startLoader();
	}

	public static AppManagerState getInstance() {
		if (sInstance == null) {
			sInstance = new AppManagerState();
		}
		return sInstance;
	}

	public static void setApplicationContext(Context context) {
		sContext = context;
	}

	public Context getContext(){
		return this.sContext;
	}
	
	
	private void setSolution() {
		BroadcastSolution bc = new BroadcastSolution(sContext);
		sConfigList.add(bc);
	}
	
	public List<IBaseSolution> getSolution(){
		return sConfigList;
	}
	
	private void setLoader(){
		sLoader = new AutoStartLoader(sContext);
	}
	
	public AutoStartLoader getLoader(){
		return this.sLoader;
	}

	public static void seAppProvider(MulwareProvider provider) {
        sLauncherProvider = new WeakReference<MulwareProvider>(provider);
    }

    public MulwareProvider getAppProvider() {
        return sLauncherProvider.get();
    }
    
    
    public void setAppsChangeController(){
    	sAppsChangeController = new AppsChangeController(sContext);	
    	sAppsChangeController.addOnAppsChangedCallback(getLoader());
    }
    
    public AppsChangeController getController(){
    	return this.sAppsChangeController;
    }
	
}
