package com.xy.smartsms.iface;

import android.view.View;
import android.widget.ListView;
import mst.widget.MstListView;

public interface IXYSmartSmsListItemHolder {

   public void showDefaultListItem();
   public View findViewById(int viewId);
   public View getListItemView();
   public IXYSmartSmsHolder getXySmartSmsHolder();
   //public int getShowBubbleMode();
   public int getCanTryToGetWhichBubbleType();//lichao modify in 2016-11-24
   /* UIX-169 songzhirong 20160531 start */
   public MstListView getListView();
   /* UIX-169 songzhirong 20160531 end */

}
