package cn.com.xy.sms.sdk.ui.popu.part;

/* MEIZU-568 huangzhiqiang 20160427 start */
import java.util.Map;
/* MEIZU-568 huangzhiqiang 20160427 end */

import org.json.JSONArray;

import android.app.Activity;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquVerticalItemTable;
import cn.com.xy.sms.sdk.util.StringUtils;

public class BubbleVerticalTableBody extends UIPart {
    
    /* MEIZU-568 huangzhiqiang 20160427 start */
    private static int    DEFAULT_SHOW_ROW = 5;
    private static int    DEFAULT_LIMIT_ROW = 8;
    /* MEIZU-568 huangzhiqiang 20160427 end */
    private DuoquVerticalItemTable mContentListView = null;
    
    private static final String TABLE_KEY = "duoqu_table_data_vert";
    
    public BubbleVerticalTableBody(Activity mContext, BusinessSmsMessage message, XyCallBack callback, int layoutId,
            ViewGroup root, int partId) {
        super(mContext, message, callback, layoutId, root, partId);
        
    }
    
    @Override
    public void initUi() {
        mContentListView = (DuoquVerticalItemTable) mView.findViewById(R.id.duoqu_horiz_list);
    }
    
    @Override
    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (message == null || mContentListView == null) {
            return;
        }
        
        ChannelContentUtil.setBodyDefaultBackGroundByTowKey(mMessage,mView);
        //设置布局显示行数
        String fixKey = (String) message.getValue("m_by_text_1");
        int ad = 0;
        if(!StringUtils.isNull(fixKey)){
            ad ++;
        }
        
        int showRows= DEFAULT_SHOW_ROW;
        int maxRows= DEFAULT_LIMIT_ROW;
            try{                                                       
                //default_num_of_rows,maximum_num_of_rows
            showRows = Integer.parseInt(String.valueOf(message.getValue("default_num_of_rows")));
            maxRows = Integer.parseInt(String.valueOf(message.getValue("maximum_num_of_rows")));
            }catch(Throwable e){
            showRows = DEFAULT_SHOW_ROW;
            maxRows = DEFAULT_LIMIT_ROW;
        }
        showRows = showRows - ad;
        maxRows = maxRows - ad;
                
        
        mContentListView.setDefaultShowRow(showRows);
        mContentListView.setmDefaultLimitDataSize(maxRows); 

        try{
            mContentListView.setmUseFirstTitle((Boolean) message.getValue("useFirstPadding"));
        }catch(Throwable e){
            
        }
        
        int size = message.getTableDataSize(TABLE_KEY);
        mContentListView.setContentList(message, size, TABLE_KEY, isRebind);
        JSONArray actionArr = mMessage.getActionJsonArray();
        int len = 0;
        if (actionArr != null) {
            len = actionArr.length();
        }
        
    }
    
    
    @Override
    public void destroy() {
        super.destroy();
    }
    
    /* MEIZU-568 huangzhiqiang 20160427 start */
    @Override
    public void changeData(Map<String, Object> param) {
        try {
            this.setContent(mMessage, true);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleVerticalTableBody changeData error:", e);
        }
    }
    /* MEIZU-568 huangzhiqiang 20160427 end */
}
