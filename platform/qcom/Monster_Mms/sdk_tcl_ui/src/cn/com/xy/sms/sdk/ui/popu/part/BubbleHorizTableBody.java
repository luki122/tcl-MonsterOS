package cn.com.xy.sms.sdk.ui.popu.part;

import java.util.Map;

import android.app.Activity;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.util.ChannelContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquHorizItemTableSec;
import cn.com.xy.sms.sdk.util.StringUtils;

public class BubbleHorizTableBody extends UIPart {

    private static int DEFAULT_SHOW_ROW = 5;
    private static int DEFAULT_LIMIT_ROW = 8;
    private DuoquHorizItemTableSec mContentListView = null;

    private static final String TABLE_KEY = "duoqu_table_data_horiz";

    public BubbleHorizTableBody(Activity mContext, BusinessSmsMessage message, XyCallBack callback, int layoutId,
            ViewGroup root, int partId) {
        super(mContext, message, callback, layoutId, root, partId);

    }

    @Override
    public void initUi() {
        mContentListView = (DuoquHorizItemTableSec) mView.findViewById(R.id.duoqu_horizl_list);
    }
    @Override
    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (message == null || mContentListView == null) {
            return;
        }
        ChannelContentUtil.setBodyDefaultBackGroundByTowKey(mMessage,mView);
        String fixKey = (String) message.getValue("m_by_text_d_1");
        int ad = 0;
        if(!StringUtils.isNull(fixKey)){
            ad ++;
        }
        
        int showRows = DEFAULT_SHOW_ROW;
        int maxRows = DEFAULT_LIMIT_ROW;
        try {
            Object value = message.getValue("default_num_of_rows_special") ;
            if (value !=null && !StringUtils.isNull((String)value)) {
                showRows = Integer.parseInt((String)value) ; 
                mContentListView.setSpecialUiPartFlag(true);
            }else {
                showRows = Integer.parseInt(String.valueOf(message.getValue("default_num_of_rows")));
                maxRows = Integer.parseInt(String.valueOf(message.getValue("maximum_num_of_rows")));
                mContentListView.setSpecialUiPartFlag(false);
            }
        } catch (Throwable e) {
            showRows = DEFAULT_SHOW_ROW;
            maxRows = DEFAULT_LIMIT_ROW;
        }
        showRows = showRows - ad;
        maxRows = maxRows - ad;

        mContentListView.setDefaultShowRow(showRows);
        mContentListView.setmDefaultLimitDataSize(maxRows);

        int size = message.getTableDataSize(TABLE_KEY);
        mContentListView.setContentList(message, size, TABLE_KEY, isRebind);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void changeData(Map<String, Object> param) {
        try {
            this.setContent(mMessage, true);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleVerticalTableBody changeData error:", e);
        }
    }
}
