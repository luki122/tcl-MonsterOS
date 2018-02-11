package com.xy.smartsms.manager;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewManager;
import cn.com.xy.sms.sdk.ui.popu.util.XySdkUtil;
import cn.com.xy.sms.sdk.ui.simplebubbleview.DuoquSimpleBubbleViewManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkCallBack;

import com.android.mms.R;
import com.xy.smartsms.iface.IXYSmartMessageItem;
import com.xy.smartsms.iface.IXYSmartSmsHolder;
import com.xy.smartsms.iface.IXYSmartSmsListItemHolder;

//lichao add for editmode on xiaoyuan rich bubble begin
import com.android.mms.ui.MessageListItem;
import com.android.mms.ui.MessageItem;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
//lichao add for editmode on xiaoyuan rich bubble end
//lichao add
import com.android.mms.MmsConfig;

public class XYBubbleListItem implements SdkCallBack{
    private final static String TAG="XIAOYUAN/XYBubbleListItem";
    private static final boolean DEBUG = false;
    
    public final static int DUOQU_SMARTSMS_SHOW_BUBBLE_RICH=2;
    public final static int DUOQU_SMARTSMS_SHOW_BUBBLE_SIMPLE=1;
    public final static int DUOQU_SMARTSMS_SHOW_DEFAULT_PRIMITIVE =0;
    public final static int DUOQU_SMARTSMS_BIND_BUBBLE_FAILED=-1;

    private final static int DUOQU_CALLBACK_UITHREAD_NEEDPARSE=-2;
    private final static int DUOQU_CALLBACK_UITHREAD_NODATA=-1;
    private final static int DUOQU_CALLBACK_UITHREAD_HASDATA=0;
    private final static int DUOQU_CALLBACK_BACKTHREAD_HASDATA=1;
    private final static int DUOQU_CALLBACK_UITHREAD_SCOLLING=-4;

    private final static String DUOQU_RICH_BUBBLE_DISPLAY="DISPLAY";
    private SdkCallBack mSimpleCallBack =null;
    private IXYSmartSmsHolder mXYsmsHolder=null;
    private ViewGroup mRichItemGroup = null;
    //lichao delete for donot show Simple BubbleView in 2016-10-26
    //private ViewGroup mSimpleItemGroup=null;
    private View mSmsRootGroup=null;
    private IXYSmartMessageItem mMessageItem=null;
    private IXYSmartSmsListItemHolder mMsgListItem =null;
    private JSONObject mCacheItemData = null;
    private Activity mCtx;
    //private int mShowBubbleModel;
    //lichao add
    //View, general-purpose
    private ViewGroup mRichItemGroupLayout = null;
    private ImageView mSwitchSimpleBubbleIcon = null;
    private ImageView mSwitchRichBubbleIcon = null;

    //Data, use by a single, should save it in MessageItem
    //private int mBubbleModel;
    //boolean isSwitchRichVisible = false;
    //boolean isSwitchSimpleVisible = false;

    /**
     *  initialization SmartSmsBubble
     */
    public XYBubbleListItem(IXYSmartSmsHolder xySmsHolder,IXYSmartSmsListItemHolder msgListItem){
        this.mXYsmsHolder=xySmsHolder;
        this.mMsgListItem= msgListItem;
        if(null == mMsgListItem  || null ==xySmsHolder){
            return;
        }
        mCtx= xySmsHolder.getActivityContext();
        initView();
    }

    /**
     *  bind bubble view by showBubbleMode
     *  showBubbleMode 1:simple bubble 2:rich bubble
     *  if simple bubble need load rich bubble data,use control simple/rich change btn visibility
     */
    public void bindBubbleView(final IXYSmartMessageItem messageItem){
        if(messageItem == null ||  null == mXYsmsHolder){
            return;
        }
        this.mMessageItem = messageItem;
        if(mMessageItem.getHideRichBubbleByUser() == false
                && mMessageItem.getBubbleModelForItem() == DUOQU_SMARTSMS_SHOW_BUBBLE_RICH){
            bindRichBubbleView();
        }else{
            //bindSimpleBubbleView();
            hideRichAndShowSimpleBubbleView();
            showDefaultListItem();
        }
    }

    private void bindRichBubbleView() {
        if(null == mXYsmsHolder || null == mMessageItem){
            Log.w(TAG, "bindRichBubbleView mSmartSmsUiHolder or mMessageItem is null");
            return;
        }
        if(null == mRichItemGroup){
            Log.w(TAG, "bindRichBubbleView mRichItemGroup  is null");
            return;
        }
        mCacheItemData = XySdkUtil.getBubbleDataFromCache(mMessageItem.getMsgId());
        // parse RichBubble-data and displayed SimpleBubble
        if(mCacheItemData == null) {
            getRichBubbleData();
        } else  if(getShowRichViewStatu() == DUOQU_SMARTSMS_SHOW_BUBBLE_RICH ){
            bindRichView();
        }else{
            hideRichAndShowSimpleBubbleView();
            showDefaultListItem();
            //setSwitchRichIcon GONE because should not bindRichView yet
            //setSwitchRichIconVisible(false);
        }
    }

    //if showSimpleBubbleView, it must be hideRichBubbleView
    //if showSimpleBubbleView, Not always need show SwitchRichIcon
    public void hideRichAndShowSimpleBubbleView() {
        if(DEBUG) Log.w(TAG , "\n\n hideRichAndShowSimpleBubbleView");
        //hideRichBubbleView if RichBubbleView exist
        setRichBubbleViewVisible(false);
        //mSwitchSimpleBubbleIcon is contained in mSmsRootGroup
        setSwitchSimpleIconVisible(false);

        setSimpleBubbleViewVisible(true);

        boolean isRichBubbleItem = mMessageItem.getIsRichBubbleItem();
        if(DEBUG) Log.w(TAG , "hideRichAndShowSimpleBubbleView, isRichBubbleItem = "+isRichBubbleItem);
        //if no RichBubbleData, don't show SwitchRichIcon
        setSwitchRichIconVisible(isRichBubbleItem);
    }

    public void setRichBubbleViewVisible(boolean isVisible) {
        if (DEBUG) Log.w(TAG, "setRichBubbleViewVisible, isVisible=" + isVisible);
        mMessageItem.setIsRichBubbleViewVisible(isVisible);
        setRichBubbleViewVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void setRichBubbleViewVisibility(int visibility) {
        if (mRichItemGroupLayout != null) {
            mRichItemGroupLayout.setVisibility(visibility);
        }
    }

    public void setSimpleBubbleViewVisible(boolean isVisible) {
        if(DEBUG) Log.d(TAG, "setSimpleBubbleViewVisible, isVisible=" + isVisible);
        mMessageItem.setIsSimpleBubbleViewVisible(isVisible);
        setSimpleBubbleViewVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void setSimpleBubbleViewVisibility(int visibility) {
        if (mSmsRootGroup != null) {
            mSmsRootGroup.setVisibility(visibility);
        }
    }

    public void setSwitchRichIconVisible(boolean isVisible) {
        if(DEBUG) Log.d(TAG, "setSwitchRichIconVisible, isVisible=" + isVisible);
        mMessageItem.setIsSwitchRichVisible(isVisible);
        setSwitchRichIconVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void setSwitchRichIconVisibility(int visibility){
        if(null != mSwitchRichBubbleIcon){
            mSwitchRichBubbleIcon.setVisibility(visibility);
        }
    }

    //if need showRichBubbleView, it must be hideSimpleBubbleView
    private void hideSimpleAndShowRichBubbleView(){
        if(DEBUG) Log.d(TAG , "\n\n hideSimpleAndShowRichBubbleView");
        //hideSimpleBubbleView if SimpleBubble exist
        setSimpleBubbleViewVisible(false);
        //if no RichBubbleData, don't show SwitchRichIcon
        setSwitchRichIconVisible(false);

        setRichBubbleViewVisible(true);
        //mSwitchSimpleBubbleIcon is contained in mSmsRootGroup
        setSwitchSimpleIconVisible(true);
    }

    //Need to restore the previous state after reload or change from editmode to normal mode
    public void setSwitchSimpleIconVisible(boolean isVisible) {
        if (DEBUG) Log.d(TAG, "setSwitchSimpleIconVisible, isVisible=" + isVisible);
        mMessageItem.setIsSwitchSimpleVisible(isVisible);
        setSwitchSimpleIconVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    //No need to restore the previous state, set visibility directly
    private void setSwitchSimpleIconVisibility(int visibility){
        if(null != mSwitchSimpleBubbleIcon){
            mSwitchSimpleBubbleIcon.setVisibility(visibility);
        }
    }

    /**
     *  initialization SmartSmsBubble all views
     */
    private void initView() {
        //if need simple button
        //lichao delete for donot show Simple BubbleView in 2016-10-26
        //mSimpleItemGroup = (ViewGroup)mMsgListItem.findViewById(R.id.duoqu_simple_bubble_action_group);

        //mRichItemGroupLayout contains mRichItemGroup and mSwitchSimpleBubbleIcon
        mRichItemGroupLayout = (ViewGroup)mMsgListItem.findViewById(R.id.layout_duoqu_rich_item_group);
        mRichItemGroup = (ViewGroup)mMsgListItem.findViewById(R.id.duoqu_rich_item_group);
        setRichBubbleViewLongClickListener();
        mSwitchSimpleBubbleIcon = (ImageView)mMsgListItem.findViewById(R.id.img_id_switch_simple);
        setSwitchSimpleBubbleViewClickListener();

        //mSmsRootGroup contains mSwitchRichBubbleIcon
        mSmsRootGroup = mMsgListItem.findViewById(R.id.message_block);
        mSwitchRichBubbleIcon = (ImageView)mMsgListItem.findViewById(R.id.img_id_switch_rich);
        setSwitchRichBubbleViewClickListener();
    }

    //lichao add for editmode on xiaoyuan rich bubble begin
    private Handler mHandler;
    public void setBubbleListItemHandler(Handler handler) {
        mHandler = handler;
    }

    private void sendMessage(MessageItem messageItem, int message) {
        if (mHandler != null) {
            Message msg = Message.obtain(mHandler, message);
            msg.obj = messageItem;
            msg.sendToTarget(); // See ComposeMessageActivity.mMessageListItemHandler.handleMessage
        }else{
            Log.e(TAG, "XYBubbleListItem.java, sendMessage(), mHandler is null");
        }
    }

    private void setRichBubbleViewLongClickListener() {
        if(null == mRichItemGroup){
            return;
        }
        mRichItemGroup.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                //Toast.makeText(mCtx, "long click", Toast.LENGTH_SHORT).show();
                sendMessage((MessageItem)mMessageItem, MessageListItem.MSG_LIST_SHOW_EDIT);
                return true;
            }
        });
    }
    //lichao add for editmode on xiaoyuan rich bubble end

    private void setSwitchSimpleBubbleViewClickListener() {
        if(null == mSwitchSimpleBubbleIcon){
            return;
        }
        mSwitchSimpleBubbleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Toast.makeText(mCtx, "BubbleDetail click", Toast.LENGTH_SHORT).show();
                mMessageItem.setHideRichBubbleByUser(true);
                hideRichAndShowSimpleBubbleView();
                showDefaultListItem();
                //setSwitchRichIcon VISIBLE because can switch back to Rich bubble
                //setSwitchRichIconVisible(true);
            }
        });
    }

    private void setSwitchRichBubbleViewClickListener() {
        if(null == mSwitchRichBubbleIcon){
            return;
        }
        mSwitchRichBubbleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Toast.makeText(mCtx, "BubbleDetail click", Toast.LENGTH_SHORT).show();

                if(DEBUG) Log.d(TAG, "SwitchRich onClick, getIsRichBubbleItem() = "+mMessageItem.getIsRichBubbleItem());
                if(DEBUG) Log.d(TAG, "SwitchRich onClick, getShowRichViewStatu() = "+getShowRichViewStatu());

                if(mMessageItem.getIsRichBubbleItem() && mMessageItem.getHideRichBubbleByUser()){
                    mMessageItem.setHideRichBubbleByUser(false);
                    hideSimpleAndShowRichBubbleView();
                }
                else{
                    Log.e(TAG, "not RICH BUBBLE MODE");
                }
            }
        });
    }

    private int getShowRichViewStatu(){
        try {
            if(mCacheItemData == null){
                if(DEBUG) Log.d(TAG, "getShowRichViewStatu, mCacheItemData == null");
                return DUOQU_SMARTSMS_BIND_BUBBLE_FAILED;
            }
            else if(!mCacheItemData.has(DUOQU_RICH_BUBBLE_DISPLAY)){
                return DUOQU_SMARTSMS_SHOW_BUBBLE_RICH;
            }else {
                return mCacheItemData.getInt(DUOQU_RICH_BUBBLE_DISPLAY);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return DUOQU_SMARTSMS_SHOW_BUBBLE_SIMPLE;
    }
    private void setShowRichViewStatu(int statu){
        try {
            if(mCacheItemData == null){
                return;
            }
            mCacheItemData.put(DUOQU_RICH_BUBBLE_DISPLAY, statu);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * bind and display RichBubbleView
     */
    private boolean bindRichView(){
        
        final String msgIds = String.valueOf(mMessageItem.getMsgId());
        final HashMap extendMap = mMessageItem.getSmartSmsExtendMap();
        extendMap.put("isClickAble", !mXYsmsHolder.isEditAble());
        //lichao add for test begin
        String phoneNumber = mMessageItem.getPhoneNum();
        //if(DEBUG) Log.d(TAG, "bindRichView(), before change, phoneNumber = "+phoneNumber);
//        if (mMessageItem.getSmsBody().contains(MmsConfig.MY_PHONE_NUMBER)) {
//            phoneNumber = "106550101955";
//        }
        //lichao add for test end
        View richBubbleView = DuoquBubbleViewManager.getRichBubbleView(mCtx, mCacheItemData,msgIds,mMessageItem.getSmsBody(),
                phoneNumber,
                mMessageItem.getSmsReceiveTime(),
                mMsgListItem.getListItemView(), 
                mXYsmsHolder.getListView(), 
                extendMap);
        
        if(richBubbleView != null && mRichItemGroup!=null){
            mRichItemGroup.removeAllViews();
            ViewParent parent= richBubbleView.getParent();
            if(parent instanceof ViewGroup){
                ViewGroup p= (ViewGroup)parent;
                p.removeView(richBubbleView);
            }
            mRichItemGroup.addView(richBubbleView);
            //lichao add in 2016-11-12
            mMessageItem.setBubbleModelForItem(DUOQU_SMARTSMS_SHOW_BUBBLE_RICH);
            mMessageItem.setIsRichBubbleItem(true);
            hideSimpleAndShowRichBubbleView();
            setShowRichViewStatu(DUOQU_SMARTSMS_SHOW_BUBBLE_RICH);
        }else{
            //lichao add in 2016-11-12
            setShowRichViewStatu(DUOQU_SMARTSMS_BIND_BUBBLE_FAILED);
            noRichDataShowDefaultListItem();
        }
        return true;
    }

    /**
     *  displayed views according to callback status
     */
    private void getRichBubbleData(){
        final String msgId = String.valueOf(mMessageItem.getMsgId());
        mRichItemGroup.setTag(msgId);

        //lichao add for test
        String phoneNumber = mMessageItem.getPhoneNum();
        //if(DEBUG) Log.d(TAG, "getRichBubbleData(), before change, phoneNumber = "+phoneNumber);
//        if (mMessageItem.getSmsBody().contains(MmsConfig.MY_PHONE_NUMBER)) {
//            phoneNumber = "106550101955";
//        }
        //lichao add for test

        DuoquBubbleViewManager.getRichBubbleData(
                mCtx,
                msgId,
                phoneNumber,
                mMessageItem.getServiceCenterNum(),
                mMessageItem.getSmsBody(),
                mMessageItem.getSmsReceiveTime(),
                DuoquBubbleViewManager.DUOQU_RETURN_CACHE_SDK_MSG_ID,
                mMsgListItem.getListItemView(),
                null, 
                mRichItemGroup,
                mXYsmsHolder.getListView(),
                mMessageItem.getSmartSmsExtendMap(),this,isScrollFing());
    }

    /**
     * getRichBubbleData callback
     */
    public void execute(Object... obj) {
       
        if(obj==null || obj.length==0 || mXYsmsHolder ==null){
            noRichDataShowDefaultListItem();
            return;
        }
        if(obj.length > 2){
            String oldmsgid = (String)obj[2];
            String orgMsgId = (String)mRichItemGroup.getTag();
            if(StringUtils.isNull(orgMsgId)||StringUtils.isNull(oldmsgid)||!orgMsgId.equals(oldmsgid)){
                return;
            }
        }
        final int status = (Integer)obj[0];
        //if(DEBUG) Log.d(TAG, "XYBubbleListItem execute(), status: "+status+" msgid: "+obj[2]+" obj[1]: "+obj[1]);
        if(DEBUG) Log.d(TAG, "XYBubbleListItem execute(), status = "+status);
        switch(status){
        case DUOQU_CALLBACK_UITHREAD_NEEDPARSE:
            //cureent msg  need parse,show default  
            noRichDataShowDefaultListItem();
            break;
        case DUOQU_CALLBACK_UITHREAD_NODATA://-1
            //lichao add if judge
            ///if(!mMessageItem.getHideRichBubbleByUser()){
                //current msg  has not rich data,show default
                noRichDataShowDefaultListItem();
            //}
            break;
        case DUOQU_CALLBACK_UITHREAD_HASDATA:
            //UI THREAD CALLBACK HAS DATA
            mCacheItemData=(JSONObject)obj[1];
            if(mMessageItem.getBubbleModelForItem() == DUOQU_SMARTSMS_SHOW_BUBBLE_RICH
                    && getShowRichViewStatu() == DUOQU_SMARTSMS_SHOW_BUBBLE_RICH){
                bindRichView();
            }else{
                hideRichAndShowSimpleBubbleView();
                showDefaultListItem();
                //setSwitchRichIcon GONE because not RICH mode
                setSwitchRichIconVisible(false);
            }
            addRichItemDataToCache(mMessageItem.getMsgId(), mCacheItemData);
            break;
        case DUOQU_CALLBACK_BACKTHREAD_HASDATA:
            //BACKGROUD THREAD CALLBACK HASDATA
            if(null == mCtx){
                return;
            }
            mCacheItemData = (JSONObject)obj[1];
            addRichItemDataToCache(mMessageItem.getMsgId(), mCacheItemData);
            break;
        case DUOQU_CALLBACK_UITHREAD_SCOLLING://-4
              noRichDataShowDefaultListItem();
        default:
            
            break;
        }
    }
    private boolean isScrollFing(){
        if(null == mXYsmsHolder || null ==mXYsmsHolder.getListView()){
            return false;
        }
        boolean scroll_state=mXYsmsHolder.getListView().getFirstVisiblePosition()==0?true:false;
        return scroll_state?false:mXYsmsHolder.isScrolling();
    }
    /**
     * show default msg body ListItem
     */
    private void showDefaultListItem(){
        if(mMsgListItem != null){
            mMsgListItem.showDefaultListItem();
        }
        //lichao delete for donot show Simple BubbleView in 2016-10-26
        //bindSimpleBubbleView();
		//lichao change this hideBubbleView() to hideRichAndShowSimpleBubbleView()
        //hideBubbleView();
    }
    /**
     * has not richview only show  default msg body ListItem
     */
    private void noRichDataShowDefaultListItem(){
        if(DEBUG) Log.d(TAG, "noRichDataShowDefaultListItem, set mCacheItemData to null");
        //lichao add in 2016-11-12 begin
        mMessageItem.setBubbleModelForItem(DUOQU_SMARTSMS_BIND_BUBBLE_FAILED);
        mMessageItem.setIsRichBubbleItem(false);
        //lichao add in 2016-11-12 end
        mCacheItemData=null;
        hideRichAndShowSimpleBubbleView();
        showDefaultListItem();
        //setSwitchRichIcon GONE because no Rich Data
        setSwitchRichIconVisible(false);
    }

    /**
     * bind and display SimpleBubbleView
     */
    /*
    //lichao delete for donot show Simple BubbleView in 2016-10-26
    private void bindSimpleBubbleView(){
        if(mSimpleItemGroup ==null){
            return;
        }
        final String msgIds = String.valueOf(mMessageItem.getMsgId());
        mSimpleItemGroup.setTag(msgIds);
        String phoneNumber = mMessageItem.getPhoneNum();
        if(DEBUG) Log.d(TAG, "bindSimpleBubbleView(), before change, phoneNumber = "+phoneNumber);
        if (mMessageItem.getSmsBody().contains(MmsConfig.MY_PHONE_NUMBER)) {
            phoneNumber = "106550101955";
        }
        try{
            //query simple bubble data 
            DuoquSimpleBubbleViewManager.getSimpleBubbleData(
                msgIds,
                phoneNumber,
                mMessageItem.getServiceCenterNum(), mMessageItem.getSmsBody(),
                mMessageItem.getSmsReceiveTime(),
                DuoquSimpleBubbleViewManager.DUOQU_RETURN_CACHE_SDK_MSG_ID, mMessageItem.getSmartSmsExtendMap(),
                getSimpleBubbleDataCallBack(),isScrollFing());
        }catch(Exception e){
            mSimpleItemGroup.setVisibility(View.GONE);
            Log.e(TAG, "com.android.mms.ui.SmartSmsBubbleManager.getSimpleBubbleData error", e);
        }
    }
    */

    /*
    //lichao delete for donot show Simple BubbleView in 2016-10-26
    private SdkCallBack  getSimpleBubbleDataCallBack(){
         if(mSimpleCallBack ==null){
             mSimpleCallBack = new SdkCallBack(){
                public void execute(final Object... obj) {
                    if(obj==null || obj.length==0 || null == mXYsmsHolder){
                        return;
                    }
                    final int statu = (Integer)obj[0];
                    if(obj.length > 2){
                        String oldmsgid = (String)obj[2];
                        String orgMsgId = (String)mSimpleItemGroup.getTag();
                        if(StringUtils.isNull(orgMsgId)||StringUtils.isNull(oldmsgid)||!orgMsgId.equals(oldmsgid)){
                            return;
                        }
                    }
                    if(DEBUG) Log.d(TAG, "getSimpleBubbleDataCallBack(), statu = "+statu);
                    switch(statu){
                    case DUOQU_CALLBACK_UITHREAD_NEEDPARSE:
                        if(DEBUG) Log.d(TAG, "getSimpleBubbleDataCallBack(), NEED PARSE");
                        bindSimpleView(null,mMessageItem, mMsgListItem.getListItemView());
                        break;
                    case DUOQU_CALLBACK_UITHREAD_NODATA:
                        if(DEBUG) Log.d(TAG, "getSimpleBubbleDataCallBack(), NO DATA");
                        bindSimpleView(null,mMessageItem, mMsgListItem.getListItemView());
                        break;
                    case DUOQU_CALLBACK_UITHREAD_HASDATA:
                        if(DEBUG) Log.d(TAG, "getSimpleBubbleDataCallBack(), HAS DATA");
                        bindSimpleView((JSONArray)obj[1], mMessageItem, mMsgListItem.getListItemView());
                        break;
                    default:
                        break;  
                }
                }
            };
        }
        return mSimpleCallBack;
    }
    */
    /**
     * set views when displayed SimpleBubble
     */
    /*
    //lichao delete for donot show Simple BubbleView in 2016-10-26
    private void bindSimpleView(JSONArray btnData,final IXYSmartMessageItem mMessageItem,final View msgListItem){
        View buttonView= null;
        try{
            if(btnData != null){
                HashMap<String,Object> extend= mMessageItem.getSmartSmsExtendMap();
                extend.put("isClickAble", !mXYsmsHolder.isEditAble());
                buttonView=DuoquSimpleBubbleViewManager.getSimpleBubbleView(mCtx,btnData, mSimpleItemGroup,extend); 
            }
            if(buttonView!=null && mSimpleItemGroup != null){
                mSimpleItemGroup.setVisibility(View.VISIBLE);
            }
            else if(mSimpleItemGroup!=null){
                mSimpleItemGroup.setVisibility(View.GONE);
            }
        }catch(Exception e){
            e.printStackTrace();
            if(mSimpleItemGroup!=null){
                mSimpleItemGroup.setVisibility(View.GONE);
            }
        }
    }
    */

    private void addRichItemDataToCache(long msgId,JSONObject itemData){
        if(itemData == null){
            return;
        }
        if(!itemData.has(DUOQU_RICH_BUBBLE_DISPLAY)){
            setShowRichViewStatu(mMessageItem.getBubbleModelForItem());
        }
        XySdkUtil.putBubbleDataToCache(msgId, itemData);
    }
    
//    public static void loadBubbleData(Context ctx,final String recipientNumber){
//    if(ctx == null){
//        return;
//    }
//    Thread thread =  new Thread(){
//        public void run() {
//            ParseBubbleManager.loadBubbleDataByPhoneNum(recipientNumber,true);
////            mIsNotifyComposeMessage=ParseManager.isEnterpriseSms(ctx, recipientNumber, null, null);
//        } 
//    };
//    thread.start();
//}

    private boolean mIsCheckBoxMode = false;
    public void setIsCheckBoxMode(boolean isCheckBoxMode) {
        mIsCheckBoxMode = isCheckBoxMode;
        if(true == isCheckBoxMode){
            setSwitchSimpleIconVisibility(View.GONE);
            setSwitchRichIconVisibility(View.GONE);
        }else {
            setSwitchSimpleIconVisibility(
                    mMessageItem.getIsSwitchSimpleVisible()? View.VISIBLE : View.GONE);
            setSwitchRichIconVisibility(
                    mMessageItem.getIsSwitchRichVisible()? View.VISIBLE : View.GONE);
        }
    }
    public boolean getIsCheckBoxMode() {
        return mIsCheckBoxMode;
    }
}
