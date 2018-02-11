package cn.com.xy.sms.sdk.ui.popu.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.R;
import cn.com.xy.sms.sdk.ui.publicinfo.PublicInfoManager;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;

public class ContentUtil {
    private static final String TAG = "ContentUtil";
    public static final String CALLS_MESSAGE_UKNOWN = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_call_unknown);
    public static final String CALLS_MESSAGE_STRANGER = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_call_stranger);
    public static final String CALLS_MESSAGE_HARASS = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_call_harass);
    static ViewManger localViewManger = null;
    public static ViewManger viewMangerImpl = null;
    public static final int DUOQU_DIALOG_UNCHECK_TEXTCOLOR = Constant.getContext().getResources()
            .getColor(R.color.duoqu_dialog_uncheck_textcolor);
    public static final int DUOQU_DIALOG_TEXTCOLOR = Constant.getContext().getResources()
            .getColor(R.color.duoqu_tos_dialog_textcolor);
    public static final String NO_CITY = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duqou_select_arrive_city);
    public static final String NO_DATA = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_double_line);
    public static final String UNKNOW_DATA = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_unknow);
    public static final String SELECT_STATION = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duqou_select_arrive_city);

    public static final String NO_DATA_TIME = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_double_line_an);
    public static final String NO_DATA_EN = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_double_line_en);
    public static final String CHINESE = ContentUtil.getResourceString(Constant.getContext(), R.string.duoqu_chinese);

    private static final String GROUPTYPE = "group_type";

    /* zhaoxiachao start */
    private static DisplayMetrics sMetrics = null;
    public static final int IS_TRAINS = 0;
    public static final int IS_FLIGHT = 1;

    public static final String SELECT_TRAINS = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_ui_dialog_select_trains);
    public static final String SELECT_FLIGHT = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_ui_dialog_select_flight);
    public static final String FILGHT = ContentUtil.getResourceString(Constant.getContext(), R.string.duoqu_ui_filght);
    public static final String DIRECTION = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_ui_direction);
    public static final String DEPARTURE = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_ui_air_departure_city);
    public static final String FIGHT = ContentUtil.getResourceString(Constant.getContext(), R.string.duoqu_ui_fight);
    public static final String TRAIN = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_ui_train_num_str);
    /* zhaoxiachao end */
    public static final String ISCLICKABLE = "isClickAble";
    

    public static int getDimension(int dimenId) {
        return (int) Constant.getContext().getResources().getDimension(dimenId);
    }

    public static void setTextColor(TextView textView, String textColor) {
        try {
            if (textView != null && !StringUtils.isNull(textColor)) {
                int res = ResourceCacheUtil.parseColor(textColor);
                textView.setTextColor(res);
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
    }

    /**
     * Set the text view content
     * 
     * @param textView
     * @param value
     * @param defaultValue
     */
    public static void setText(TextView textView, String value, String defaultValue) {
        if (textView == null) {
            return;
        }

        if (StringUtils.isNull(value)) {
            textView.setText(defaultValue);
            return;
        }
        textView.setText(value.trim());
    }

    /**
     * Set the enabled state of view.
     * 
     * @param view
     * @param visibility
     */
    public static void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);

        }
    }

    /**
     * Return the string value associated with a particular resource ID
     * 
     * @param context
     * @param id
     * @return
     */
    public static String getResourceString(Context context, int id) {
        if (context != null) {
            try {
                return context.getResources().getString(id);
            } catch (Throwable ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * get the length of the string in Chinese or English
     */
    public static int getStringLength(String value) {
        int valueLength = 0;

        for (int i = 0; i < value.length(); i++) {
            Character temp = value.charAt(i);
            if (temp.toString().matches(CHINESE)) {
                valueLength += 2;
            } else {
                valueLength += 1;
            }
        }
        return valueLength;
    }

    /* QIK-634/yangzhi/2016.07.18---start--- */
    public static final int DATE_FORMAT_24 = 1;
    public static final int DATE_FORMAT_NORMAL = 0;

    /**
     * 获取时间文本，如果系统为12小时制时添加上午下午提示
     * 
     */
    public static String getTimeText(Context content, long time, int type) {
        if (time <= 0) {
            return null;
        }

        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTimeInMillis(time);
        String timeText = null;

        if (DateFormat.is24HourFormat(content) || type == DATE_FORMAT_24) {
            timeText = DateFormat.format("kk:mm", timeCalendar).toString();
        } else {
            String ampmValues = null;
            if (timeCalendar.get(Calendar.AM_PM) == 0) {
                ampmValues = content.getString(R.string.duoqu_am);
            } else {
                ampmValues = content.getString(R.string.duoqu_pm);
            }
            timeText = ampmValues + " " + DateFormat.format("h:mm", timeCalendar).toString();
        }

        return timeText;
    }

    public static String getTimeText(Context context, long time) {
        return getTimeText(context, time, DATE_FORMAT_NORMAL);
    }

    /* QIK-634/yangzhi/2016.07.18---end--- */
    public static void textSetColor(TextView textView, String color) {
        int colors = ResourceCacheUtil.parseColor(color);
        if (colors != -1 && textView != null) {
            textView.setTextColor(colors);
        }
    }

    public static void isTextSetColor(TextView textView, String color, int rescolor) {
        if (textView == null) {
            return;
        }
        if (!StringUtils.isNull(color)) {
            textView.setTextColor(ResourceCacheUtil.parseColor(color));
        } else {
            textView.setTextColor(rescolor);
        }
    }

    public static String getLanguage() {
    	 String language = Locale.getDefault().getLanguage();
         String country = Locale.getDefault().getCountry();
         if ("zh".equals(language)) {
             if ("HK".equalsIgnoreCase(country)
                     || "TW".equalsIgnoreCase(country)) {
                 return "zh-tw";
             }
             return "zh-cn";
         } else {
             return "en";
         }
    }

    public static String getBtnName(final JSONObject actionMap) {
        String btnName = (String) JsonUtil.getValueFromJsonObject(actionMap,
                "btn_name");
        if ("zh-cn".equalsIgnoreCase(getLanguage())) {
            return btnName;
        } else if ("zh-tw".equalsIgnoreCase(getLanguage())) {
            String ftName = (String) JsonUtil.getValueFromJsonObject(actionMap,
                    "ftName");
            if (!StringUtils.isNull(ftName)) {
                btnName = ftName;
            }
        } else {
            String egName = (String) JsonUtil.getValueFromJsonObject(actionMap,
                    "egName");
            if (!StringUtils.isNull(egName)) {
                btnName = egName;
            }
        }
        return btnName;
    }

    public static boolean bubbleDataIsNull(BusinessSmsMessage smsMessage) {
        return smsMessage == null || smsMessage.bubbleJsonObj == null;
    }

    public static void callBackExecute(SdkCallBack callBack, Object... obj) {
        if (callBack != null) {
            callBack.execute(obj);
        }
    }

    public static String getFormatDate(Date date, SimpleDateFormat dateFormat) {
        String str = "";
        try {
            if (date != null) {
                str = dateFormat.format(date);
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return str;
    }

    public static Date stringToDate(String dateStr, String formatStr) {
        if (StringUtils.isNull(dateStr)) {
            return null;
        }
        Date date = null;
        try {
            date = new SimpleDateFormat(formatStr).parse(dateStr);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
        return date;
    }

    public static void saveSelectedIndex(BusinessSmsMessage message, String selecedIndexKey,
            String selectedIndexValue) {
        if (message == null) {
            return;
        }
        try {
            message.bubbleJsonObj.put(selecedIndexKey, selectedIndexValue);
            ParseManager.updateMatchCacheManager(message);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(TAG, e);
        }
    }

    public static void setOnClickListener(View.OnClickListener onClickListener, View... views) {
        if (views == null || views.length == 0) {
            return;
        }
        for (View view : views) {
            if (view != null) {
                view.setOnClickListener(onClickListener);
            }
        }
    }

    public static void setClickableToFalse(View... views) {
        if (views == null || views.length == 0) {
            return;
        }
        for (View view : views) {
            if (view != null) {
                view.setClickable(false);
            }
        }
    }

    public static Handler mHandler = new Handler(Looper.getMainLooper());
    public static Pair<String, BitmapDrawable> sBubbleLogoc;
    public static final int TYPE_LOGOC_NORMAL = 1;
    public static final int TYPE_LOGOC_ROUND = 2;
    public static int DEF_CIRCLE_WITH = 6;

    public static void bindTextImageView(String phoneNumber, final ImageView imageView, final int type) {
        if (imageView == null || StringUtils.isNull(phoneNumber)) {
            return;
        }

        final String pNumber = StringUtils.getPhoneNumberNo86(phoneNumber);
        if (imageView != null) {
            imageView.setTag(pNumber);
        }

        if (sBubbleLogoc != null && pNumber.equals(sBubbleLogoc.first)) {
            setImage(imageView, sBubbleLogoc.second, TYPE_LOGOC_NORMAL);
            return;
        }

        JSONObject json = PublicInfoManager.getPublicInfoByPhoneIncache(phoneNumber);
        if (json != null) {
            final String logoName = json.optString("logoc");
            if (TextUtils.isEmpty(logoName)) {
                return;
            }
            BitmapDrawable bitmap = PublicInfoManager.getLogoDrawable(logoName);
            if (bitmap != null) {
                setImage(imageView, bitmap, type);
            } else {

                PublicInfoManager.publicInfoPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        final BitmapDrawable bd = PublicInfoManager.findLogoByLogoName(logoName, null);
                        if (bd == null) {
                            return;
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!pNumber.equals(imageView.getTag())) {
                                    return;
                                }
                                setImage(imageView, bd, type);
                            }
                        });
                    }
                });
            }
        } else {
            SdkCallBack callBack = new SdkCallBack() {
                @Override
                public void execute(final Object... obj) {
                    try {
                        if (obj != null && obj.length > 3) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!pNumber.equals(imageView.getTag())) {
                                        return;
                                    }

                                    final BitmapDrawable bd = (BitmapDrawable) obj[3];
                                    setImage(imageView, bd, type);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            PublicInfoManager.loadPublicInfofrombubble(Constant.getContext(), phoneNumber, callBack);
        }
    }

    public synchronized static void setImage(ImageView imageView, BitmapDrawable bd, int type) {
        try {
            if (imageView == null || bd == null) {
                return;
            }
            BitmapDrawable bg = bd;
            boolean cache = false;
            switch (type) {
            case TYPE_LOGOC_ROUND:
                if (sBubbleLogoc != null && sBubbleLogoc.first.equals(imageView.getTag()) && sBubbleLogoc.second != null
                        && !sBubbleLogoc.second.getBitmap().isRecycled()) {
                    bg = sBubbleLogoc.second;
                } else {
                    bg = getRoundedCornerBitmap(bd, DEF_CIRCLE_WITH);
                    cache = true;
                }
                break;
            default:
                bg = bd;
            }
            if (cache) {
                if (sBubbleLogoc != null && sBubbleLogoc.second != null) {
                    Bitmap bm = sBubbleLogoc.second.getBitmap();
                    if (bm != null && !bm.isRecycled()) {
                        bm.recycle();
                    }
                }

                sBubbleLogoc = new Pair<String, BitmapDrawable>((String) imageView.getTag(), bg);
            }
            imageView.setBackground(bg);
            imageView.requestLayout();
            imageView.invalidate();
        } catch (Throwable e) {
        }
    }

    public static BitmapDrawable getRoundedCornerBitmap(BitmapDrawable bitmapDrawable, int StrokeWidth) {
        if (bitmapDrawable == null)
            return null;
        Bitmap bitmap = bitmapDrawable.getBitmap();
        if (bitmap == null)
            return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xffffffff;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        float roundPx = width / 2;
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        Bitmap output_1 = Bitmap.createBitmap(width + 2 * StrokeWidth, height + 2 * StrokeWidth, Config.ARGB_8888);
        Canvas canvas_1 = new Canvas(output_1);
        Paint paint_1 = new Paint();
        paint_1.setAntiAlias(true);
        paint_1.setFilterBitmap(true);
        paint_1.setDither(true);
        paint_1.setColor(color);
        paint_1.setStyle(Paint.Style.STROKE);
        paint_1.setStrokeWidth(StrokeWidth);
        canvas_1.drawCircle(width / 2 + StrokeWidth, height / 2 + StrokeWidth, width / 2, paint_1);
        canvas_1.drawBitmap(output, StrokeWidth, StrokeWidth, paint_1);
        if (output != null && !output.isRecycled()) {
            output.recycle();
            output = null;
        }
        return new BitmapDrawable(output_1);
    }
    
    public static BitmapDrawable getRoundedRecCornerBitmap(BitmapDrawable bitmapDrawable) {
        if (bitmapDrawable == null)
            return null;
        Bitmap bitmap = bitmapDrawable.getBitmap();
        if (bitmap == null)
            return null;
        Drawable imageDrawable = new BitmapDrawable(bitmap);  
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        // 新建一个新的输出图片  
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  
        Canvas canvas = new Canvas(output);  
  
        // 新建一个矩形  
        RectF outerRect = new RectF(0, 0, width, height);  
  
        // 产生一个圆角矩形  
        final int color = 0xffffffff;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);  
        paint.setColor(color);  
        canvas.drawRoundRect(outerRect, 12,12, paint);  
  
  
        // 将源图片绘制到这个圆角矩形上  
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
        imageDrawable.setBounds(0, 0, width,height);  
        canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);  
        imageDrawable.draw(canvas);  
        canvas.restore();  
  
        return new BitmapDrawable(output);  
    }
    

    public static int getPxDimensionFromString(Context c, String string) {
        if (TextUtils.isEmpty(string) || c == null) {
            return -1;
        }

        string = string.trim().toLowerCase(Locale.ENGLISH);

        int length = string.length();

        if (length < 2) {
            return -1;
        }

        String unit = string.substring(length - 2);
        String value = string.substring(0, length - 2);
        int result = -1;

        if (!TextUtils.isEmpty(value)) {
            if ("px".equals(unit)) {
                try {
                    result = Integer.parseInt(value);
                } catch (Exception e) {
                    result = -1;
                }
            } else if ("dp".equals(unit)) {
                int intValue = -1;
                try {
                    intValue = Integer.parseInt(value);
                } catch (Exception e) {
                    intValue = -1;
                }

                if (intValue >= 0) {
                    result = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, intValue,
                            c.getResources().getDisplayMetrics());
                }
            }
        }
        return result;
    }

    /* COOLPAD-260 huangzhiqiang 20160816 start */
    public static void clearBubbleLogoCache(String number) {
        if (sBubbleLogoc != null && number.equals(sBubbleLogoc.first)) {
            sBubbleLogoc = null;
        }
    }
    /* COOLPAD-260 huangzhiqiang 20160816 end */

    /* zhaoxiachao start */
    public static List<JSONArray> getSimpleBubbleData(JSONArray jsonArray, int dataSize) throws JSONException {
        if (jsonArray == null) {
            return null;
        }
        
        List<JSONArray> arrays = new ArrayList<JSONArray>();
        int size = jsonArray.length();
        for (int i = 0; i < size; i++) {
            JSONObject outJason = jsonArray.getJSONObject(i);
            String groupType = outJason.optString(GROUPTYPE);
            boolean isAdd = false;
            if (StringUtils.isNull(groupType)) {
                groupType = "";
            } else {
                for (int j = 0; j < arrays.size(); j++) {
                    JSONArray ba = arrays.get(j);
                    JSONObject innerObj = null;
                    if(ba != null && ba.length() > 0){
                        innerObj = ba.getJSONObject(0);
                    }
                    
                    if (innerObj != null) {
                        if (groupType.equals(innerObj.optString(GROUPTYPE))) {
                            ba.put(outJason);
                            isAdd = true;
                            break;
                        }
                    } else {
                        ba.put(outJason);
                        isAdd = true;
                    }
                }
            }

            if (!isAdd && arrays.size() < dataSize) {
                arrays.add(new JSONArray().put(outJason));
            }
        }
        return arrays;
    }

    public synchronized static DisplayMetrics getDisplayMetrics() {

        if (sMetrics == null) {
            sMetrics = new DisplayMetrics();
            WindowManager manager = (WindowManager) Constant.getContext().getSystemService(Context.WINDOW_SERVICE);
            manager.getDefaultDisplay().getMetrics(sMetrics);
        }

        return sMetrics;
    }
    /* zhaoxiachao end */
    
    
    

    public static interface OnBottomClick {
        public void Onclick(int type, int select);
    }

}
