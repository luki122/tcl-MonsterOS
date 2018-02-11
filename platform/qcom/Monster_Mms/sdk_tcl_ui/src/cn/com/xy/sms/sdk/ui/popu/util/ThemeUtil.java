package cn.com.xy.sms.sdk.ui.popu.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.LruCache;
import android.view.View;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.util.StringUtils;

public class ThemeUtil {
    public static final int                  SET_NULL                 = -1;
    private static final int                 ERROR_INDEX              = -9999;
    private static final int                 ERROR_RESID              = -9998;
    public static final int                  THEME_DEFAULT_TEXT_COLOR = Color.BLACK;
    private static LruCache<String, Integer> mTextColorCache          = new LruCache<String, Integer>(100);
    private static LruCache<String, Integer> mResCache                = new LruCache<String, Integer>(100);
    
    public static int getResIndex(String name) {
        try {
            return Integer.parseInt(name);
        } catch (Throwable e) {
            return ERROR_INDEX;
        }
    }
    
    public static int getColorId(int colorNameIndex) {
        int colorId = ERROR_RESID;
        switch (colorNameIndex) {
            case 1030:
                colorId = R.color.duoqu_theme_color_1030;
                break;
            case 1020:
                colorId = R.color.duoqu_theme_color_1020 ;
                break;
            case 1051:
                colorId = R.color.duoqu_theme_color_1051;
                break;
            case 1100:
                colorId = R.color.duoqu_theme_color_1100;
                break;
            case 1110:
                colorId = R.color.duoqu_theme_color_1110;
                break;
            case 3010:
                colorId = R.color.duoqu_theme_color_3010;
                break;
            case 5010:
                colorId = R.color.duoqu_theme_color_5010;
                break;
            case 1090:
                colorId = R.color.duoqu_theme_color_1090;
                break;
            case 4010:
                colorId = R.color.duoqu_theme_color_4010;
                break;
            case 5011:
                colorId = R.color.duoqu_theme_color_5011;
                break;
            case 1050:
                colorId = R.color.duoqu_theme_color_1050;
                break;
            case 1091:
                colorId = R.color.duoqu_theme_color_1091;
                break;
            case 1040:
                colorId = R.color.duoqu_theme_color_1040;
                break;
            case 1080:
                colorId = R.color.duoqu_theme_color_1080;
                break;
            case 1092:
                colorId = R.color.duoqu_theme_color_1092;
                break;
            case 1010:
                colorId = R.color.duoqu_theme_color_1010;
                break;
        }
        return colorId;
    }
    
    public static void setTextColor(Context context, TextView textView, String relativePath, int defaultResId) {
        try {
            if (context == null || textView == null)
                return;
            if (StringUtils.isNull(relativePath)) {
                int color = context.getResources().getColor(defaultResId);
                textView.setTextColor(color);
            } else {
                relativePath = relativePath.trim();
                Integer textColorId = mTextColorCache.get(relativePath);
                if (textColorId == null) {
                    int colorIndex = getResIndex(relativePath);
                    if (colorIndex != ERROR_INDEX) {
                        textColorId = getColorId(colorIndex);
                        if (textColorId == ERROR_RESID) {
                            textColorId = defaultResId;
                        } else {
                            mTextColorCache.put(relativePath, textColorId);
                        }
                    } else {
                        try {
                            textView.setTextColor(Color.parseColor(relativePath));
                            return;
                        } catch (Throwable e) {
                            textColorId = defaultResId;
                        }
                    }
                }
                textView.setTextColor(context.getResources().getColor(textColorId));
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("ThemeUtil setTextColor error:", e);
        }
    }
    
    public static void setViewBg(Context context, View view, String relativePath, int defaultResId) {
        setViewBg(context, view, relativePath, SET_NULL, SET_NULL, false, defaultResId);
    }
    
    public static void setViewBg(Context context, View view, String relativePath, int resId, int width,
            int defaultResId) {
        setViewBg(context, view, relativePath, resId, width, false,
        
        defaultResId);
    }
    
    public static int getColorInteger(Context context, String num) {
        if (StringUtils.isNull(num)) {
            return 0;
        }
        return context.getResources().getColor(getColorId(getResIndex(num)));
        
    }
    
    public static void setViewBg(Context context, View view, String relativePath, int resId, int width, boolean cache,
            int defaultColorId) {
        if (context == null || view == null) {
            return;
        }
        
        try {
            Drawable dw = ViewUtil.getDrawable(context, relativePath, false, cache);
            if (dw != null) {
                ViewUtil.setBackground(view, dw);
                return;
            }
            
            if (!StringUtils.isNull(relativePath)) {
                relativePath = relativePath.trim();
                Integer colorId = mResCache.get(relativePath);
                if (colorId == null) {
                    int colorIndex = getResIndex(relativePath);
                    if (colorIndex == ERROR_INDEX) {
                        boolean isSuccess = ViewUtil.setViewBg2(context, view, relativePath);
                        if (isSuccess) {
                            return;
                        }
                    }
                    colorId = getColorId(colorIndex);
                    if (colorId == ERROR_RESID) {
                        colorId = defaultColorId;
                    } else {
                        mResCache.put(relativePath, colorId);
                    }
                }
                
                if (resId != SET_NULL) {
                    view.setBackgroundResource(resId);
                    GradientDrawable myGrad = (GradientDrawable) view.getBackground();
                    if (width > 0) {
                        /* QIKBOX-106/108 lilong 2016.04.26 start */
                        myGrad.setStroke(width, Constant.getContext().getResources().getColor(colorId));
                        /* QIKBOX-106/108 lilong 2016.04.26 end */
                    } else {
                        myGrad.setColor(Constant.getContext().getResources().getColor(colorId));
                    }
                } else {
                    setBgColorWithoutShapeChange(context,view,colorId);
                }
            } else {
                /* QIKBOX-106/108 lilong 2016.04.26 start */
                setBgColorWithoutShapeChange(context,view,defaultColorId);
                /* QIKBOX-106/108 lilong 2016.04.26 end */
                
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("TrainDataUtil setViewBg error:", e);
        }
    }
    
    /* QIKBOX-119 lianghailun 20160527 start */
    private static void setBgColorWithoutShapeChange(Context context , View view,int colorId){
        if(view.getBackground() instanceof GradientDrawable){
            GradientDrawable myGrad = (GradientDrawable) view.getBackground();
            myGrad.setColor(context.getResources().getColor(colorId));
        }else if(view.getBackground() instanceof ShapeDrawable){
            ShapeDrawable bgShape = (ShapeDrawable )view.getBackground();
            bgShape.getPaint().setColor(context.getResources().getColor(colorId));
        }else{
            view.setBackgroundResource(colorId);
        }
    }
    /* QIKBOX-119 lianghailun 20160527 start */
    
    /* QIKBOX-106/108 lilong 2016.04.26 starts */
    public static void setTextViewStyle(Context context, BusinessSmsMessage message, TextView textView,
            String standColor, String expireColor, int defaultColor, int expireDefaultColor, int expireValue) {
        String usecolor = (String) message.getValue(expireValue == 1 ? expireColor : standColor);
        int useDefaultColor = expireValue == 1 ? expireDefaultColor : defaultColor;
        setTextColor(context, textView, usecolor, useDefaultColor);// v_bt_body_split_bg
    }
    
    public static void setBgViewStyle(Context context, BusinessSmsMessage message, View BgView, String standColor,
            String expireColor, int defaultColor, int expireDefaultColor, int expireValue) {
        String usecolor = (String) message.getImgNameByKey(expireValue == 1 ? expireColor : standColor);
        int useDefaultColor = expireValue == 1 ? expireDefaultColor : defaultColor;
        setViewBg(context, BgView, usecolor, useDefaultColor);// v_bt_body_split_bg
    }
    
    /* QIKBOX-106/108 lilong 2016.04.26 end */
    
    /* UIX标准方案UIX-149/ kedeyuan/20160.5.16 starts */
    public static void setTextandViewStyle(Context context, BusinessSmsMessage message, TextView textView,
            String key_name, String color_key_name, int defaultColorResId) {
        String valueString = (String) message.getValue(key_name);
        if (!StringUtils.isNull(valueString)) {
            textView.setText(valueString);
            textView.setVisibility(View.VISIBLE);
            setTextColor(context, textView, (String) message.getValue(color_key_name), defaultColorResId);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }
        
    }
    
    /* UIX标准方案UIX-149/ kedeyuan/20160.5.16 ends */
    
}
