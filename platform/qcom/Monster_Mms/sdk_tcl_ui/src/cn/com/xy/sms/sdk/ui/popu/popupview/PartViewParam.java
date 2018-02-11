package cn.com.xy.sms.sdk.ui.popu.popupview;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class PartViewParam implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String HEAD = "H";
    public static final String BODY = "B";
    public static final String FOOT = "F";
    public ArrayList<Integer> mLayOutList = null;// Combination layout
    public boolean mNeedScroll = false;// Whether need to scroll
    public boolean mAddImageMark = false;// Whether need to add the IMG tags
    public int mBodyHeightType = 0; // The height type of the body
    public int mBodyMaxHeightType = 0; // The maximum high type of the body
    public int mPaddingLeftType = 0; // The layout and the container left
                                     // padding
    public int mPaddingTopType = 0; // The layout and the container top padding
    public int mPaddingRightType = 0; // The layout and the container right
                                      // padding
    public int mPaddingBottomType = 0; // The layout and the container bottom
                                       // padding
    public int mUiPartMarginTopType = 0;// UI layout with the former one control
                                        // the margin top of type

}
