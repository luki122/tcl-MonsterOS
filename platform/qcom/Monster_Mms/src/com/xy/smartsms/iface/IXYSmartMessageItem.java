package com.xy.smartsms.iface;

import java.util.HashMap;

import android.R.integer;

public interface IXYSmartMessageItem {

    public long getMsgId();
    public HashMap getSmartSmsExtendMap();
    public String getPhoneNum();
    public String getServiceCenterNum();
    public long getSmsReceiveTime();
    public boolean isSms();
    public String getSmsBody();
    /*UIX标准方案UIX-173 kedeyuan 2016.06.06 starts*/
    public int getSimIndex();
    /*UIX标准方案UIX-173 kedeyuan 2016.06.06 ends*/

    //lichao add in 2016-10-27 begin
    public void setHideRichBubbleByUser(boolean hide);
    public boolean getHideRichBubbleByUser();
    //lichao add in 2016-10-27 end
	
    //lichao add in 2016-11-12 begin
    public void setIsSwitchSimpleVisible(boolean isVisible);
    public boolean getIsSwitchSimpleVisible();

    public void setIsSwitchRichVisible(boolean isVisible);
    public boolean getIsSwitchRichVisible();

    public void setIsSimpleBubbleViewVisible(boolean isVisible);
    public boolean getIsSimpleBubbleViewVisible();

    public void setIsRichBubbleViewVisible(boolean isVisible);
    public boolean getIsRichBubbleViewVisible();

    public void setBubbleModelForItem(int bubbleModelForItem);
    public int getBubbleModelForItem();

    public void setIsRichBubbleItem(boolean isRichBubbleItem);
    public boolean getIsRichBubbleItem();
    //lichao add in 2016-11-12 end
}
