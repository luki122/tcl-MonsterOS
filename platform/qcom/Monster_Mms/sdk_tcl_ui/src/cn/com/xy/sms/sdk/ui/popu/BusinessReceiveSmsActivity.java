
package cn.com.xy.sms.sdk.ui.popu;

import java.util.LinkedList;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ViewFlipper;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.popupview.BasePopupView;
import cn.com.xy.sms.sdk.ui.popu.util.ResourceCacheUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.ui.popu.widget.CommonDialog;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.PopupMsgManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkCallBack;

/*import com.example.popupsdk.service.popu.DemoPopuView;*/
/**
 * 
 * @author Administrator
 *
 */
public class BusinessReceiveSmsActivity extends BaseActivity{

	private static final long serialVersionUID=-1 ; 
	
    ViewFlipper filFlipper = null;

    int size = 0;

    //int index = 0;

    SdkCallBack businessCallBack = null;

    private final long thread_id = -1;

//    public static BusinessReceiveSmsActivity smsActivity = null;
    boolean isListView = false;
    int currentIndex=0;//当前页面
    @TargetApi(Build.VERSION_CODES.KITKAT) 
    private void setStatusBarTransparent(){ 
    	Window window = getWindow();
    	if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) { 
    		// 托盘重叠显示在Activity上
    		View decorView = getWindow().getDecorView();
    		int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE; 
    		decorView.setSystemUiVisibility(uiOptions); 
    		//decorView.setOnSystemUiVisibilityChangeListener(this); 
    		// 设置托盘透明
    		window.addFlags( WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); 
    		Log.d("test", "VERSION.SDK_INT =" + VERSION.SDK_INT); 
    	  } else {
    		  //设置全屏
      		window.setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
      				WindowManager.LayoutParams.FLAG_FULLSCREEN); 

    		  Log.d("test", "SDK 小于19不设置状态栏透明效果");
    	 } 
		window.addFlags( WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); 

    }
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
      this.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
      setStatusBarTransparent();
        /**
         *当activity设置为这个属性的时候---》》android:configChanges="orientation"
         *横竖屏之间的切换会先执行onDestory方法，然后再次进入onCreate方法 ,所以我要在它销毁之前保留弹窗需要的数据。
         */
//        smsActivity = this;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	// TODO Auto-generated method stub
    	super.onConfigurationChanged(newConfig);
    	// PrintTestLogUtil.printTestLog("BusinessReceiveSmsActivity", "onConfigurationChanged : "+newConfig.orientation);
    	 size=0;
    	 filFlipper.removeAllViews();
    	 initData();
       
    }
    LinkedList<BusinessSmsMessage> list=null;
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
    	/**
    	 * 保存弹窗需要的数据
    	 */
    	
//        BusinessSmsMessage message = PopupMsgManager.getBussinessMessageByIndex(0);
//        savedInstanceState.putSerializable("message", message);
    	//LinkedList<BusinessSmsMessage> list= PopupMsgManager.businessSmsList;
    	if(list == null){
    	   list= new LinkedList<BusinessSmsMessage>();
    	}else{
    		list.clear();
    	}
    	list.addAll(PopupMsgManager.businessSmsList);
    	
    	savedInstanceState.putSerializable("message",list);
 
    	super.onSaveInstanceState(savedInstanceState);
        
    }

    @Override
    protected void onDestroy() {
    	 
        try {
            super.onDestroy();
            destory();
            ResourceCacheUtil.clearCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
        filFlipper = null;
    }
    
    void destory(){
    	try{
        PopupMsgManager.clearBusinessMessage(list);
        int count = filFlipper.getChildCount();
        View tempView=null;
        for (int i = 0; i < count; i++) {
        	tempView=   filFlipper.getChildAt(i);
            if(tempView instanceof BasePopupView){
             destoryPopuView((BasePopupView)tempView);
            } 
        }
        DuoquUtils.getSdkDoAction().getThirdPopupView(null, null, null, null);
        filFlipper.removeAllViews();
//        smsActivity = null;
       
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }

    @Override
    protected void onPause() {
        super.onPause();
        //timeLog.log("UIPart", "进入----------onPause : "+this.getClass().getName());
//        stopClearStatu= PopupMsgManager.clearUserClickBusinessMessage();
//        timeLog.log("PopupMsgManager", "onPause : "+stopClearStatu);
    }
    
    @Override
    protected void onResume() {
        try {
        
            // TODO Auto-generated method stub
            super.onResume();
            //if (isfirst == false) {
                initData();
           // } else
            //{

            //}
            showMsgCount();
            // LogManager.i("BusinessReceiveSmsActivity",
            // "onResume ="+System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

       @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
            // TODO: handle exception
        }
        savedInstanceState = null;
    }

    @Override
    public void initBefore(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        
        if(savedInstanceState!=null){
            Object list = savedInstanceState.getSerializable("message");
            if (list instanceof LinkedList) {
                try {
                    LinkedList<BusinessSmsMessage> listMsg = (LinkedList<BusinessSmsMessage>) savedInstanceState
                            .getSerializable("message");
                    int size = -1;
                    if (listMsg != null) {
                        size = listMsg.size();
                        stopClearStatu = PopupMsgManager.addAllToFirst(listMsg);
                    }
//                    timeLog.log("PopupMsgManager", "initBefore onCreate cache find listMsg size: " + size
//                            + " stopClearStatu: " + stopClearStatu);
                    // Log.d("isRun", message.toString());
                } catch (ClassCastException ex) {
                    // TODO: handle exception
                }
            }
      }else{
//    	  timeLog.log("PopupMsgManager", "onCreate savedInstanceState is null");
      }
     
    }

    @Override
    public void initAfter() {
        try {
        	
//        	 timeLog.log("UIPart", "start initAfter  start: "+this.getClass().getName());
              filFlipper = (ViewFlipper) findViewById(R.id.sms_popu_frame);
              businessCallBack = new SdkCallBack() {
                  @Override
                  public void execute(Object... obj) {
                      if (obj != null) {
                          int len = obj.length;
                          if (len > 0) {
                        	  System.out.println("callback : "+obj[0]);
                              if(obj[0] instanceof Byte){
	                        	  byte statu = (Byte)obj[0];
	                              switch (statu) {
	                                  case BasePopupView.POPU_CMD_DEL: // 删除
										executeDel();
	                                      break;
	                                  case BasePopupView.POPU_CMD_READ:// 已读
	                                      executeRead();
	                                      break;
	                                  case BasePopupView.POPU_CMD_OPEN:// 打开
	//                                      executeOpen();
	                                      break;
	                                 case BasePopupView.POPU_CMD_CALL:
	//                                      executeCall();
	//                                      finish();
	                                      break;
	                                 case BasePopupView.POPU_CMD_DOACTION:
	 								     delCurrentPopuView();
	                                	break;
	                                 case 9: // 第三方回调     
	 								     delCurrentPopuView();
	                                       break;
	                                  default:
	                                      break;
	                              }
                              }else{
                            	  int statu = (Integer)obj[0];
                            	  switch (statu) {
                                  case 0: // 删除
									executeDel();
                                      break;
                                  case 1:// 已读
                                      executeRead();
                                      break;
                                  case 3:// 打开
//                                      executeOpen();
                                      break;
                                 case 2:
//                                      executeCall();
//                                      finish();
                                      break;
                                 case 6:
 	 								     delCurrentPopuView();
 	                                	break;
                                 case 9: // 第三方回调     
 								     delCurrentPopuView();
                                       break;
                                  default:
                                      break;
                              }
                              }
                          }
                      }
                  }
              };
              initData();
//              timeLog.log("UIPart", "end initAfter  start: "+this.getClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean isfirst = true;
 
    
    //初始化短信数据
    public synchronized void initData() {
//    	 timeLog.log("PopupMsgManager", " start initData  : "+this.getClass().getName());
    	if(stopClearStatu==1 || PopupMsgManager.hasRemoveData){
    		 PopupMsgManager.hasRemoveData=false;
    		 PopupMsgManager.hasPhoneThird.clear();
    		stopClearStatu=0;
    		//isrestart=false;
    	    filFlipper.removeAllViews();  
	        LogManager.i("BusinessReceiveSmsActivity", "restart initData prevSize ="+size);
//	        for(int i=0;i<size;i++){
//	        	PopupMsgManager.removeBusinessMessageByIndex(i);
//	        }
//	        size=0;
	        size = PopupMsgManager.getBusinessMessageSize();
	        LogManager.i("BusinessReceiveSmsActivity", "restart initData size ="+size);
	       {
	    	   
	           // for (int i = size-1; i >=0; i--) {
	    	    for (int i = 0; i <size; i++) {
	              BusinessSmsMessage message = PopupMsgManager.getBussinessMessageByIndex(i);
	              if(!addThirdPopupView(message)){//添加第三方视图没有成功
	                if(addPopupView(message,i)){
	            	  break;
	               }
	              }
	            }
	
	        }
    	}else{
	        int prevSize = filFlipper.getChildCount();
	        if(ViewUtil.getChannelType() ==3){//联想渠道
	        	View tempView =null;
		        for(int i = 0; i < prevSize;i++){
		        	tempView =filFlipper.getChildAt(i);
		        	if(tempView != null){
		        		String phoneNumber=(String)tempView.getTag();
		        		 
		        	    if(phoneNumber != null){//第三方弹窗视图包含tag值为短信号码
		        	    	if(PopupMsgManager.hasPhoneThird.contains(phoneNumber)){
		        	    		 
		        	    		PopupMsgManager.hasPhoneThird.remove(phoneNumber);
		        	    		filFlipper.removeViewAt(i);
		        	    		i--;
		        	    		prevSize--;
		        	    	}
		        	    }
		        	}
		        }
	        }
	        LogManager.i("PopupMsgManager", "initData prevSize ="+prevSize);
	        size = PopupMsgManager.getBusinessMessageSize();
	        LogManager.i("PopupMsgManager", "initData size ="+size);
	       {
	    	   
	            for (int i = prevSize; i < size; i++) {
	              BusinessSmsMessage message = PopupMsgManager.getBussinessMessageByIndex(i);
	              if(!addThirdPopupView(message)){//添加第三方视图没有成功
		                if(addPopupView(message,i)){
		            	  break;
		               }
		          }
	            }
	
	        }
    	}
//
//        if (isfirst) {
//            isfirst = false;
//            if (filFlipper.getChildCount() > 0) {
//               // index = 0;
//                filFlipper.setDisplayedChild(0);
//            }
//            Log.i("isRun","-----------------zui  hou----------------");
//           // showMsgCount();
//        }
    	displayPopupView();
//        timeLog.log("UIPart", " end initData : "+this.getClass().getName());
    }

    /**
     * 添加第三方视图
     * @param message
     */
    public boolean addThirdPopupView(BusinessSmsMessage message){
//    	Log.d("BusinessReceiveSmsActivity","addThirdPopupView 1");
    	if(message != null && message.messageBody == null){
    		
//    		Log.d("BusinessReceiveSmsActivity","addThirdPopupView 2");
      	  //添加第三方弹窗视图
      	 View view =DuoquUtils.getSdkDoAction().getThirdPopupView(BusinessReceiveSmsActivity.this, message.originatingAddress, message.valueMap, businessCallBack);
           if(view != null){
        	   LogManager.i("PopupMsgManager", "addThirdPopupView view is null number: "+message.originatingAddress+" view hascode: "+view.hashCode());
        	   view.setTag(message.originatingAddress);
        	   message.valueMap =null;//避免刷新时,添加重复数据
          	   filFlipper.addView(view);
           }else{
        	   PopupMsgManager.removeBusinessMessage(message);
        	   LogManager.i("PopupMsgManager", "addThirdPopupView view is null number: "+message.originatingAddress);
           }
           return true;
        }
    	return false;
    }
    void displayPopupView(){
    	int childCount =filFlipper.getChildCount();
    	if(childCount > 0){
    		if(KeyManager.channel.equals("NQIDAQABCOOL")){
    			//奇酷渠道展示最后收到的短信
    			int lastC=childCount-1;
    		  if(filFlipper.getDisplayedChild() != lastC){
    		   filFlipper.setDisplayedChild(lastC);
    		 
    		  }
    		}else{
    			if(filFlipper.getDisplayedChild() != 0){
    				  filFlipper.setDisplayedChild(0);
    				
    			}
    		}
    	}
    }
    
    public boolean addPopupView( BusinessSmsMessage message,int i){
     
//    	timeLog.log("UIPart", " start showPopupView : ");
    	BasePopupView tempPopuView = null;
        if (message != null) {
            String titleNo = message.getTitleNo();
            try {
            	 tempPopuView = ViewManger.getView(BusinessReceiveSmsActivity.this,businessCallBack,message,titleNo);   
//            	 timeLog.log("UIPart", " start showPopupView tempPopuView: "+tempPopuView.getClass().getName());
            } catch (Exception e) {
                e.printStackTrace();
                if (LogManager.debug) {
                    Log.i("handleCrtImageFail", e.getLocalizedMessage());
                }
                handleCrtImageFail(i);
                return true;
            }
            if (tempPopuView != null) {
//            	timeLog.log("UIPart", " 1 bind showPopupView tempPopuView: "+tempPopuView.getClass().getName());
            	filFlipper.addView(tempPopuView);
//                timeLog.log("UIPart", " 2 bind showPopupView tempPopuView: "+tempPopuView.getClass().getName());
            } else {
                handleCrtImageFail(i);
                return true;
            }
        }
            return false;

    }
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // LogManager.i("BusinessReceiveSmsActivity",
        // "onStart ="+System.currentTimeMillis());

    }
  

    private void updateRead() {
    	try {
    		View view = getCurrentPopuView();
    		if(view instanceof BasePopupView){
    			 BasePopupView popuView = (BasePopupView)view;
            	 if(popuView != null){
    		    	  BusinessSmsMessage message = popuView.mBusinessSmsMessage;
    		       	  if(message!=null){
    	       			  String msgId = (String)message.getValue("msgId");
    	       			  LogManager.i("BusinessReceiveSmsActivity", "关闭msgId ="+msgId);
    	       			  if(!StringUtils.isNull(msgId)){
    	       				DuoquUtils.getSdkDoAction().markAsReadForDatabase(BusinessReceiveSmsActivity.this, msgId);
    	       			  }
    		       	  }
    		      }
    		}
		} catch (Exception e) {
		e.printStackTrace();
		}
    	 
    }

    private void executeRead() {
//        updateRead();
        delCurrentPopuView(false);
    }

    private synchronized void delCurrentPopuView(boolean isDel) {
        delCurrentPopuView();
    }

    
    private void executeDel(){

//    	try {
//    		 BasePopupView popuView = getCurrentPopuView();
//			if(popuView!=null)
//			{
//				popuView.setVisibility(View.INVISIBLE);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
    	 View  view = getCurrentPopuView();
    	 if(view != null && view instanceof BasePopupView){
    		 BasePopupView popuView =(BasePopupView)view;
    		 BusinessSmsMessage message = popuView.mBusinessSmsMessage;
	       	  if(message!=null){
	       		  if(ViewManger.isOpensmsEnable(message))
	       		  {//即顯是沒有確認的
	       			    deleteMsg();
						delCurrentPopuView(true);
	       		  }
	       		  else
	       		  {
	       			try {
	       	    		CommonDialog commonDialog =  new CommonDialog(BusinessReceiveSmsActivity.this, "删除",
	       						"将会删除此信息。", "取消", "删除", new CommonDialog.onExecListener(){
	       					public void execSomething() {
	       						deleteMsg();
	       						delCurrentPopuView(true);
	       					}
	       				}, null,null);
	       				commonDialog.show();
	       			} catch (Exception e) {
	       				e.printStackTrace();
	       			}
	       		  }
	       		  
	       	  }
    	 }
    	
    	
    
			
    
	}
   
    private void deleteMsg(){
    	
    	try {
       		View view = getCurrentPopuView();
    		if(view instanceof BasePopupView){
    		  BasePopupView popuView = (BasePopupView)view;
        	  if(popuView != null){
		    	  BusinessSmsMessage message = popuView.mBusinessSmsMessage;
		       	  if(message!=null){
	       			  final String msgId = (String)message.getValue("msgId");
	       			  LogManager.i("BusinessReceiveSmsActivity", "删除msgId ="+msgId);
	       			  if(!StringUtils.isNull(msgId)){	
		       				DuoquUtils.getSdkDoAction().deleteMsgForDatabase(BusinessReceiveSmsActivity.this, msgId);
	       			  }
	       			
		       	  }
		      }
    		}
		} catch (Exception e) {
		e.printStackTrace();
		}
    	 
    
    }
    
    /**
     * 删除当前
     */
    private synchronized void delCurrentPopuView() {
        try {
        	View view =getCurrentPopuView();
        	System.out.println("callback : delCurrentPopuView childcount : "+filFlipper.getChildCount());
			if (view != null) {
				if (view instanceof BasePopupView) {
                    updateRead();
					BasePopupView popuView = (BasePopupView) view;
					PopupMsgManager.removeBusinessMessage(popuView.mBusinessSmsMessage);
					destoryPopuView(popuView);
				} else {
					// 删除第三方视图
					String phoneNumber = (String) view.getTag();
					PopupMsgManager.removeBusinessMessageByNum(BusinessReceiveSmsActivity.this, phoneNumber, true,null);
				}
				filFlipper.removeView(view);
				displayPopupView();
			}
			System.out.println("callback : delCurrentPopuView: "+filFlipper.getChildCount());
        	showMsgCount();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("callback : delCurrentPopuView: error: "+e.getMessage());
        }
    }

    private void destoryPopuView(BasePopupView popuView) {
        if (popuView != null) {
            popuView.destroy();
        }
    }

    /**
     * 获取当前的popuView
     *
     * @return
     */
    public View getCurrentPopuView() {
      return  filFlipper.getCurrentView();
       
    }

    private void showMsgCount() {
    	View view=getCurrentPopuView();
    	int count = filFlipper.getChildCount();
//    	if(view instanceof BasePopupView){
//    		  BasePopupView popuView =(BasePopupView)view;
//    	}
       
        if (view != null) {
           // popuView.showMsgCount((filFlipper.getDisplayedChild() + 1) + "/" + size);

        } else {
            finish();
        }
    }

 


    @Override
    public int getLayoutId() {
    	return R.layout.duoqu_smspopu_frame;
        
    }

  
    @Override
    public void onAttachedToWindow() {

        super.onAttachedToWindow();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if (KeyEvent.KEYCODE_BACK == keyCode) {
            	//BasePopupView popuView  =getCurrentPopuView();
            	//if(popuView!=null){
            	//	int index =popuView.smsIndex;
            	//	PopupMsgManager.removeBusinessMessageByIndex(index);
            		delCurrentPopuView();
            	//}
              //  index = filFlipper.getDisplayedChild();
               //  PopupMsgManager.removeBusinessMessageByIndex(index);
              //  delCurrentPopuView();

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onKeyDown(keyCode, event);
    }
 

 

    public void handleCrtImageFail(int index) {
        try {
        	if(index > -1){
            PopupMsgManager.removeBusinessMessageByIndex(index);
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
 
    int stopClearStatu=0;
    @Override
    protected void onRestart() {
    	// TODO Auto-generated method stub
    	super.onRestart();
    	//PrintTestLogUtil.printTestLog("BusinessReceiveSmsActivity", "onRestart");
    }
    @Override
    protected void onPostResume() {
    	// TODO Auto-generated method stub
    	super.onPostResume();
    	//PrintTestLogUtil.printTestLog("BusinessReceiveSmsActivity", "onPostResume");
    } 
 
   
    @Override
    protected void onStop() {
    	super.onStop();
       // PrintTestLogUtil.printTestLog("BusinessReceiveSmsActivity", "onStop");
        //finish();
        stopClearStatu= PopupMsgManager.clearUserClickBusinessMessage();
        if(PopupMsgManager.businessSmsList.isEmpty()){
        	finish();
        }
    }
   
    public int getMsgCount(){
    	return size;
    }
    
    public int getCurrentIndex(){
    	if(filFlipper!=null)    		
    	return filFlipper.getDisplayedChild()+1;
    	return 1;
    }
    
    @Override  
    public Resources getResources() {  
        Resources res = super.getResources();
        try {
            Configuration config = res.getConfiguration();
            if (res != null && config != null) {
                // config.setToDefaults();
                config.fontScale = 1.0f;
                res.updateConfiguration(config, res.getDisplayMetrics());
            }

        } catch (Exception ex) {
            if (LogManager.debug) {
                ex.printStackTrace();
            }
        }
        return res;  
    }  
}
