package cn.com.xy.sms.sdk.ui.popu.util;

import android.app.Activity;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleAirBody;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleBodyPostMessage;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleBottomTwo;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleCodeHead;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleGeneralOneBody;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleHorizTableBody;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleSmsTextBody;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleTrainBody;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleVerticalTableBody;
import cn.com.xy.sms.sdk.ui.popu.part.UIPart;

public class ViewMangerImpl extends ViewManger {
    
    /*
     * 101-499 for head;501~899 for body;901~999 for button Don't set in
     * multiples of 100 number
     */
    private final static Integer VIEW_PART_ID[] = { 
        ViewPartId.PART_HEAD_CODE, 
        ViewPartId.PART_BODY_HORIZ_TABLE,
        ViewPartId.PART_BOTTOM_TWO_BUTTON, 
        ViewPartId.PART_BODY_TRAIN_TABLE, 
        ViewPartId.PART_BODY_AIR_TABLE, 
        ViewPartId.PART_ORIGIN_SMS_TEXT,
        ViewPartId.PART_BODY_GENENARALONE,
        ViewPartId.PART_POST_BODY,
        ViewPartId.PART_BODY_HORIZ_TABLE_SEC,
    }; // all
                                                                                                                   // part
            
    public Integer[] getViewPartIdArr(){
        return VIEW_PART_ID;
    }

    /**
     * Head part
     * 
     * @param context
     * @param message
     * @param xyCallBack
     * @param root
     * @param partId
     * @return
     * @throws Exception
     */
    private UIPart getHeadUIPartByPartId(Activity context, BusinessSmsMessage message, XyCallBack xyCallBack,
            ViewGroup root, int partId) throws Exception {
        UIPart part = null;
        switch (partId) {
            case ViewPartId.PART_HEAD_CODE:
                part = new BubbleCodeHead(context, message, xyCallBack, R.layout.duoqu_title_head, root, partId);
                break;
            default:
                break;
        }
        return part;
    }
    
    /**
     * Body part
     * 
     * @param context
     * @param message
     * @param xyCallBack
     * @param root
     * @param partId
     * @return
     * @throws Exception
     */
    private UIPart getBodyUIPartByPartId(Activity context, BusinessSmsMessage message, XyCallBack xyCallBack,
            ViewGroup root, int partId) throws Exception {
        UIPart part = null;
        switch (partId) {
            case ViewPartId.PART_BODY_HORIZ_TABLE:
                part = new BubbleVerticalTableBody(context, message, xyCallBack, R.layout.duoqu_vertical_table_body, root,
                        partId);
                break;
            case ViewPartId.PART_BODY_TRAIN_TABLE:
                part = new BubbleTrainBody(context, message, xyCallBack, R.layout.duoqu_train_body, root, partId);
                break;
            case ViewPartId.PART_BODY_AIR_TABLE:
                part = new BubbleAirBody(context, message, xyCallBack, R.layout.duoqu_air_body, root, partId);
                break; 
            case ViewPartId.PART_ORIGIN_SMS_TEXT:
                part = new BubbleSmsTextBody(context, message, xyCallBack, R.layout.duoqu_sms_origin_text, root,
                        partId);
                break;
            case ViewPartId.PART_BODY_GENENARALONE:
                part = new BubbleGeneralOneBody(context, message, xyCallBack, R.layout.duoqu_bubble_body_generalone, root, partId);
                break;
            case ViewPartId.PART_BODY_HORIZ_TABLE_SEC:
                part = new BubbleHorizTableBody(context, message, xyCallBack, R.layout.duoqu_horizl_table_body, root,
                        partId);
                break;
            case ViewPartId.PART_POST_BODY:
                part = new BubbleBodyPostMessage(context, message, xyCallBack, R.layout.duoqu_bubble_body_postmessage,
                        root, partId);
            default:
                break;
        }
        return part;
    }
    
    /**
     * Foot part
     * 
     * @param context
     * @param message
     * @param xyCallBack
     * @param root
     * @param partId
     * @return
     * @throws Exception
     */
    private UIPart getFootUIPartByPartId(Activity context, BusinessSmsMessage message, XyCallBack xyCallBack,
            ViewGroup root, int partId) throws Exception {
        UIPart part = null;
        switch (partId) {
            case ViewPartId.PART_BOTTOM_TWO_BUTTON:
                part = new BubbleBottomTwo(context, message, xyCallBack, R.layout.duoqu_bubble_bottom_two, root,
                        partId);
                break;
            default:
                break;
        }
        return part;
    }
    
    public UIPart getUIPartByPartId(Activity context, BusinessSmsMessage message, XyCallBack xyCallBack,
            ViewGroup root, int partId) throws Exception {
        UIPart part = null;
        if (partId < 500) {
            part = getHeadUIPartByPartId(context, message, xyCallBack, root, partId);
        } else if (partId < 900) {
            part = getBodyUIPartByPartId(context, message, xyCallBack, root, partId);
            
        } else if (partId >= 900) {
            part = getFootUIPartByPartId(context, message, xyCallBack, root, partId);
        }
        return part;
    }
}
