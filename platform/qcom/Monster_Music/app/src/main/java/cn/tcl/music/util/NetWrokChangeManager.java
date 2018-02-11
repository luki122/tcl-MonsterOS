package cn.tcl.music.util;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by xiaoliang on 6/18/16.
 */
public class NetWrokChangeManager {
    private static String TAG = "NetWrokChangeManager";
    private Context mContext ;
    private ArrayList<NetworkChangeObv> mNetworkChangeObvList;
    private volatile static NetWrokChangeManager mNetWrokChangeManager =null;
    private NetWrokChangeManager(Context context){
        this.mContext = context;
        mNetworkChangeObvList = new ArrayList<NetworkChangeObv>();
        mNetworkChangeObvList.clear();
    }

    public static synchronized NetWrokChangeManager getInstance (Context context){
        if (null == mNetWrokChangeManager){
            mNetWrokChangeManager = new NetWrokChangeManager(context);
        }
        return mNetWrokChangeManager;
    }

    public void registNetworkChangeObv( NetworkChangeObv obv){
        if (mNetworkChangeObvList == null){
            Log.d(TAG," mNetworkChangeObvList == null");
        }
        if(mNetworkChangeObvList != null && mNetworkChangeObvList.contains(obv) ){
            Log.d(TAG," already register");
        }
        mNetworkChangeObvList.add(obv);
    }

    public void unregistNetworkChangObv (NetworkChangeObv obv){
        if (mNetworkChangeObvList == null){
            Log.d(TAG," mNetworkChangeObvList == null");
        }
        if(mNetworkChangeObvList != null && mNetworkChangeObvList.contains(obv) ){
            mNetworkChangeObvList.remove(obv);
        }
    }
    public void netWorkCannotWork(){
        if (null == mNetworkChangeObvList){
            Log.d(TAG, " mNetworkChangeObvList == null");
            return;
        }
        for (int i =0 ;i <mNetworkChangeObvList.size(); i++){
            mNetworkChangeObvList.get(i).canNotUsingNetWork();
        }
    }
    public void netWorkCanWork(){
        if (null == mNetworkChangeObvList){
            Log.d(TAG," mNetworkChangeObvList == null");
            return;
        }
        for (int i =0 ;i <mNetworkChangeObvList.size(); i++){
            mNetworkChangeObvList.get(i).canUsingNetWork();
        }
    }
    public interface NetworkChangeObv{
        public void canNotUsingNetWork();
        public void canUsingNetWork();
    }

   public  void destoryInstance (){
       mNetworkChangeObvList.clear();
       mNetWrokChangeManager = null;
   }

}
