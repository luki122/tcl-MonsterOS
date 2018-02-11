package cn.com.xy.sms.sdk.ui.popu.popupview;

import android.view.View;

public interface IBubbleView {
    /**
     * Add rich bubble view
     * 
     * @param view
     * @param place
     *            [1-4] 1:bubble top,2:bubble bottom 3:bubble left 4:bubble
     *            right
     * @throws Exception
     */
    public void addExtendView(View view, int place) throws Exception;

    /**
     * Remove the extend view
     * 
     * @param mView
     * @param place
     * @throws Exception
     */
    public void removeAllExtendView() throws Exception;
}
