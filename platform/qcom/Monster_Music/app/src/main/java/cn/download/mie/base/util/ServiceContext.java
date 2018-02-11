package cn.download.mie.base.util;


import android.content.Context;

public abstract class ServiceContext
{
	protected static ServiceContext _instance = null;
	Context mContext;
	
	public static ServiceContext getInstance() {
		return _instance;
	}
	
	public void resetContext()
	{
		
	}
	
	public ServiceContext(Context context)
	{
		this.mContext = context;
	}
	
	public Context getApplicationContext()
	{
		return mContext;
	}

	public abstract void registerSystemObject(String name, Object obj);
	public abstract Object getSystemObject(String name);
}
