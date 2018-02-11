package com.android.deskclock.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.android.deskclock.R;
import com.android.deskclock.Utils;
import com.android.deskclock.Util.SharePreferencesUtils;
import com.android.deskclock.worldclock.CityObj;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class WorldClockView extends View {

    private float mRadiusOffset;
    private final Paint mPaint = new Paint();
    private final Paint mFill = new Paint();
    private final Paint mTextPaint = new Paint();
    private final Paint mBigTimePaint = new Paint();
    private final Paint mBigTimeAmPmPaint = new Paint();
    private float mStrokeSize = 2;
    private int color_center_white;
    private int color_current_time;
    private int world_gray_text_color;
    private int color_half_gray;
    private int color_dot_gray;
    private int color_two_dot_gray;
    private int clock_white;
    private Context mContext;
    public int xCenter;
    public int yCenter;
    public float radius;

    private float grayDotRadius = 10;

    public String local_time;// "当地时间" 字符串

    private Bitmap currentTimeBitMap;

    private final int TEXT_PADDING = 10;// 两行文本的上下间距
    private  int LOCAL_TIME_TEXT_PADDING = 18;// 当地时间文本和上面大时间的上下间距

    private boolean isShowLocalTime = true;// true 显示大的本地时间 false显示各个城市时间
    private boolean isTime24 = false;// 是否是２４小时制

    private String big_time_str;
    private String big_time_am_pm;

    private List<WorldClockBean> mDataList;

    // 昨天明天的圆角矩形
    private float roundRec_w = 60;
    private float roundRec_h = 20;
    private float roundRec_r = 8;
    private int roundRec_text_color;
    private float roundRec_text_size;
    private float roundRec_strokeSize = 5;// 圆角矩形边缘的宽度
    private float roundRec_padding_left = 10;
    private float roundRec_y_offset = 10;//Y坐标的偏移量

    private int current_hour;
    private int current_min;

    private List<CityObj> m_city_list;
    private String str_local;

    public WorldClockView(Context context) {
        this(context, null);
    }

    public WorldClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context c) {

        mDataList = new ArrayList<WorldClockBean>();
        m_city_list = new ArrayList<CityObj>();

        mContext = c;
        Resources resources = c.getResources();
        mRadiusOffset = resources.getDimension(R.dimen.circletimer_marker_size);// 为了和秒表的圆大小保持一致
        mStrokeSize = resources.getDimension(R.dimen.circletimer_gray_circle_size);
        grayDotRadius = resources.getDimension(R.dimen.world_dot_size);
        local_time = resources.getString(R.string.local_time);
        roundRec_w = resources.getDimension(R.dimen.roundRec_w);
        roundRec_h = resources.getDimension(R.dimen.roundRec_h);
        roundRec_text_color = resources.getColor(R.color.roundRec_text_color);
        roundRec_text_size = resources.getDimension(R.dimen.roundRec_text_size);
        roundRec_r = resources.getDimension(R.dimen.roundRec_r);
        roundRec_padding_left = resources.getDimension(R.dimen.roundRec_padding_left);
        roundRec_strokeSize = resources.getDimension(R.dimen.roundRec_strokeSize);
        roundRec_y_offset = resources.getDimension(R.dimen.roundRec_y_offset);
        clock_white = resources.getColor(R.color.clock_white);
        str_local = resources.getString(R.string.str_local);

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeSize);
        mPaint.setColor(clock_white);

        mFill.setAntiAlias(true);
        mFill.setStyle(Paint.Style.FILL);

        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setAntiAlias(true);

        color_center_white = resources.getColor(R.color.white);
        color_half_gray = resources.getColor(R.color.clock_half_gray);
        color_dot_gray = resources.getColor(R.color.color_dot_gray);
        color_two_dot_gray = resources.getColor(R.color.color_two_dot_gray);
        color_half_gray = resources.getColor(R.color.clock_half_gray);
        color_current_time = resources.getColor(R.color.clock_red);
        world_gray_text_color = resources.getColor(R.color.world_gray_text_color);
        currentTimeBitMap = BitmapFactory.decodeResource(resources, R.drawable.current_time);

        mBigTimePaint.setAntiAlias(true);
        mBigTimePaint.setTextSize(Utils.dp2px(c, 40));// 大时间字体
        mBigTimePaint.setColor(color_current_time);
        Typeface tf = Typeface.create("monster-medium", -1);
        mBigTimePaint.setTypeface(tf);

        Typeface tf_normal = Typeface.create("monster-normal", -1);
        mPaint.setTypeface(tf_normal);
        mTextPaint.setTypeface(tf_normal);
        mBigTimeAmPmPaint.setTypeface(tf_normal);

        mBigTimeAmPmPaint.setAntiAlias(true);
        mBigTimeAmPmPaint.setTextSize(Utils.dp2px(c, 12));// 大时间的ＡＭ　ＰＭ
        mBigTimeAmPmPaint.setColor(color_current_time);

        LOCAL_TIME_TEXT_PADDING = Utils.dp2px(c, 14);

        updateTime(null, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        xCenter = getWidth() / 2 + 1;
        yCenter = getHeight() / 2;

        radius = Math.min(xCenter, yCenter) - mRadiusOffset;

        mFill.setColor(color_center_white);

        mPaint.setStrokeWidth(mStrokeSize);
        mPaint.setColor(clock_white);

        canvas.drawCircle(xCenter, yCenter, radius, mFill);// 画中间白圆
        canvas.drawCircle(xCenter, yCenter, radius, mPaint);// 画边圆环

        
        if(mDataList.size()>1 ||(mDataList.size() ==1 && mDataList.get(0).cities_list.size()>1)){
            isShowLocalTime = false;
        } else {
            isShowLocalTime = true;
        }
        
        if (!isShowLocalTime) {// 画晚上的半圆
            RectF oval = new RectF(xCenter - radius, yCenter - radius, xCenter + radius, yCenter + radius);
            mFill.setColor(color_half_gray);
            canvas.drawArc(oval, 0, 180, true, mFill);
        }

        for (int i = 0; i < mDataList.size(); i++) {
            drawCurrentDot(canvas, mDataList.get(i));
        }

        drawCurrentBigTime(canvas);

    }

    public void drawCurrentDot(Canvas canvas, WorldClockBean mData) {// 画当前时间的圆和文本

        int hour_24 = mData.time_hour_24;
        int min_24 = mData.time_min_24;
        
        for(int i=0;i<mData.cities_list.size();i++){
            if(SharePreferencesUtils.isSHowThisCity(mContext, mData.cities_list.get(i).mCityId)){
                mData.show_index = i;
                break;
            }
        }

        String time_str = mData.cities_list.get(mData.show_index).getTimeString();
        String am_pm = mData.cities_list.get(mData.show_index).getAmOrPm();
        String tom_yes = mData.cities_list.get(mData.show_index).getTomOrYes();
        String current_time_str = time_str + am_pm;
        String current_city = mData.cities_list.get(mData.show_index).mCityName;

        // Log.i("zouxu", "drawCurrentDot" + "current_city = " + current_city +
        // " hour_24=" + hour_24 + ",min_24="
        // + min_24);

        float persent = 90 + (hour_24 * 60 + min_24) * 360 / (24 * 60);// 计算出当前时间占整天的比例
                                                                       // +90是由于０点是垂直向下即90度
        final float dotRadians = (float) Math.toRadians(persent);

        float current_time_dot_center_x = xCenter + (float) (radius * Math.cos(dotRadians));
        float current_time_dot_center_y = yCenter + (float) (radius * Math.sin(dotRadians));

        mData.centerX = current_time_dot_center_x;
        mData.centerY = current_time_dot_center_y;

        // boolean is_draw_loacal = TextUtils.isEmpty(current_city);
        boolean is_draw_loacal = (current_hour == hour_24 && current_min == min_24);

        if (is_draw_loacal) {// 画本地的图片
            float current_time_x_start = current_time_dot_center_x - currentTimeBitMap.getWidth() / 2;
            float current_time_y_start = current_time_dot_center_y - currentTimeBitMap.getHeight() / 2;
            float current_time_x_end = current_time_dot_center_x + currentTimeBitMap.getWidth() / 2;
            float current_time_y_end = current_time_dot_center_y + currentTimeBitMap.getHeight() / 2;
            RectF dst = new RectF(current_time_x_start, current_time_y_start, current_time_x_end, current_time_y_end);// 前时间圆的区域
            canvas.drawBitmap(currentTimeBitMap, null, dst, mFill);
            int size = mData.cities_list.size();
            if(size > 2){
                mTextPaint.setColor(color_current_time);
                String str_size = ""+(size-1);
                mTextPaint.setTextSize(Utils.dp2px(mContext, 6));
                Rect rect = new Rect();
                mTextPaint.getTextBounds(str_size, 0, str_size.length(), rect);// 计算文本的宽高
                int str_w = rect.width();
                int str_h = rect.height();
                float str_x = current_time_x_start+currentTimeBitMap.getWidth();
                float str_y = current_time_y_start+str_h;
                canvas.drawText(str_size, str_x, str_y, mTextPaint);
            }
        } else {
            mFill.setColor(color_dot_gray);
            canvas.drawCircle(current_time_dot_center_x, current_time_dot_center_y, grayDotRadius, mFill);
            if (mData.cities_list.size() > 1) {// 多个就再画个同心圆
                mFill.setColor(color_two_dot_gray);
                canvas.drawCircle(current_time_dot_center_x, current_time_dot_center_y, grayDotRadius / 2, mFill);
            }
        }

        if (isShowLocalTime) {
            return;
        }

        // 画出当前时间文本
        if (is_draw_loacal && current_city.equals(str_local)) {
            mTextPaint.setColor(color_current_time);
        } else {
            mTextPaint.setColor(world_gray_text_color);
        }

        mTextPaint.setTextSize(Utils.dp2px(mContext, 10));
        Rect rect = new Rect();
        mTextPaint.getTextBounds(current_time_str, 0, current_time_str.length(), rect);// 计算文本的宽高
        int str_time_w = rect.width();
        int str_time_h = rect.height();

        mTextPaint.getTextBounds(current_city, 0, current_city.length(), rect);// 计算文本的宽高
        int str_city_w = rect.width();
        int str_city_h = rect.height();

        boolean isShowCity = !TextUtils.isEmpty(current_city);

        if (!isShowCity) {
            str_city_w = 0;
            str_city_h = 0;
        }

        int roundRec_with_all = 0;
        boolean is_show_tom_yes = !TextUtils.isEmpty(tom_yes);
        if (is_show_tom_yes) {
            roundRec_with_all = (int) (roundRec_w + roundRec_padding_left);
        }

        str_time_w = str_time_w + roundRec_with_all;
        int str_with = Math.max(str_time_w, str_city_w);// 文本矩形的宽度
        int str_height = str_time_h + TEXT_PADDING + str_city_h;// 文本矩形的高度

        float text_rectf_center = radius * 0.68f;// 文本矩形的中心点再半径的0.68位置

        float text_x_center = xCenter + (float) (text_rectf_center * Math.cos(dotRadians));// 计算城市＋时间文本的中心坐标
        float text_y_center = yCenter + (float) (text_rectf_center * Math.sin(dotRadians));

        float text_x = text_x_center - str_with / 2;
        float time_text_y = text_y_center + str_height / 2;

        canvas.drawText(current_time_str, text_x, time_text_y, mTextPaint);// 画时间文本

        float city_text_y = time_text_y - str_time_h - TEXT_PADDING;
        if (isShowCity) {
            canvas.drawText(current_city, text_x, city_text_y, mTextPaint);// 画城市文本
        }

        if (is_show_tom_yes) {// 画昨天明天的圆角矩形
            mPaint.setStrokeWidth(roundRec_strokeSize);
            mPaint.setColor(roundRec_text_color);

            float rec_start_x = text_x + str_city_w + roundRec_padding_left;
            float rec_start_y = city_text_y - roundRec_h;

            float rec_end_x = rec_start_x + roundRec_w;
            float rec_end_y = rec_start_y + roundRec_h;

            RectF rect_roun = new RectF(rec_start_x, rec_start_y, rec_end_x, rec_end_y);

            canvas.drawRoundRect(rect_roun, roundRec_r, roundRec_r, mPaint);

            mTextPaint.setTextSize(roundRec_text_size);
            mTextPaint.setColor(roundRec_text_color);
            mTextPaint.getTextBounds(tom_yes, 0, tom_yes.length(), rect);

            int tom_yes_w = rect.width();
            int tom_yes_h = rect.height();

            float round_text_x = rec_start_x + (roundRec_w - tom_yes_w) / 2;
            float round_text_y = rec_end_y - (roundRec_h - tom_yes_h - roundRec_strokeSize * 2) / 2
                    - roundRec_strokeSize * 2-roundRec_y_offset;
            canvas.drawText(tom_yes, round_text_x, round_text_y, mTextPaint);
        }

    }

    public void drawCurrentBigTime(Canvas canvas) {// 显示当前时间大圆
        if (!isShowLocalTime) {
            return;
        }

        Rect rect = new Rect();
        String big_time = "00:00";
//        mBigTimePaint.getTextBounds(big_time_str, 0, big_time_str.length(), rect);// 计算大时间文本的宽高 这个测量的宽高再各个时间可能会不一样　造成抖动
        mBigTimePaint.getTextBounds(big_time, 0, big_time.length(), rect);// 计算大时间文本的宽高
        int str_time_w = rect.width();
        int str_time_h = rect.height();
        
        Log.i("zouxu", "drawCurrentBigTime str_time_w="+str_time_w+",str_time_h = "+str_time_h);

        float big_time_x = xCenter - str_time_w / 2;
        float big_time_y = yCenter + str_time_h / 2;
        canvas.drawText(big_time_str, big_time_x, big_time_y, mBigTimePaint);// 画大时间

        if (!TextUtils.isEmpty(big_time_am_pm)) {
            float big_time_am_pm_x = xCenter + str_time_w / 2;
            float big_time_am_pm_y = yCenter + str_time_h / 2;
            canvas.drawText(big_time_am_pm, big_time_am_pm_x, big_time_am_pm_y, mBigTimeAmPmPaint);// 画大时间AM
                                                                                                   // PM
        }

        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(Utils.dp2px(mContext, 12));
        mTextPaint.setColor(0x5c000000);
        mTextPaint.getTextBounds(local_time, 0, local_time.length(), rect);// 计算当地时间文本的宽高
        int str_local_time_w = rect.width();
        int str_local_time_h = rect.height();
        float local_time_x = xCenter - str_local_time_w / 2;
        float local_time_y = yCenter + str_time_h / 2 + str_local_time_h + LOCAL_TIME_TEXT_PADDING;
        canvas.drawText(local_time, local_time_x, local_time_y, mTextPaint);// 画当地时间这个字符串

    }

    public void updateTime(List<CityObj> city_list, int off_set_min) {

        if (city_list != null) {
            m_city_list = new ArrayList<CityObj>();
            m_city_list.addAll(city_list);
        }

        String current_time_str;

        for (int i = 0; i < mDataList.size(); i++) {
            mDataList.get(i).cities_list.clear();
        }
        mDataList.clear();

        isTime24 = Utils.isTime24(mContext);
        Calendar c = Calendar.getInstance();

        c.add(Calendar.MINUTE, off_set_min);

        current_hour = c.get(Calendar.HOUR_OF_DAY);
        current_min = c.get(Calendar.MINUTE);

        String am_or_pm;// 显示ＡＭ还是ＰＭ

        if (isTime24) {
            am_or_pm = "";
        } else if (current_hour == 0) {
            am_or_pm = "  AM";
            current_hour = 12;
        } else if (current_hour == 12) {
            am_or_pm = "  PM";
            current_hour = 12;
        } else if (current_hour > 12) {
            am_or_pm = "  PM";
            current_hour = current_hour - 12;
        } else {
            am_or_pm = "  AM";
        }

        String str_hour;
        String str_min;
        str_hour = getAdd0String(current_hour);
        str_min = getAdd0String(current_min);

        current_time_str = str_hour + ":" + str_min;

        current_hour = c.get(Calendar.HOUR_OF_DAY);// 12小时制的时候再转回来原来的24小时时间
        current_min = c.get(Calendar.MINUTE);

        WorldClockBean local_bean = new WorldClockBean();
        local_bean.time_hour_24 = current_hour;
        local_bean.time_min_24 = current_min;

        CityObj local_city = new CityObj("", "", "", "");
        local_city.setAmOrPm(am_or_pm);
        local_city.setTimeString(current_time_str);
        local_city.mCityName = str_local;

        big_time_str = current_time_str;
        big_time_am_pm = am_or_pm;

        local_bean.cities_list.add(local_city);
        mDataList.add(local_bean);

        if (city_list != null && city_list.size() > 0) {
            for (int i = 0; i < city_list.size(); i++) {
                addToDataList(city_list.get(i), off_set_min);
            }
        }

        invalidate();
    }

    private void addToDataList(CityObj city, int off_set_min) {

        final Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, off_set_min);

        long now_time = now.getTimeInMillis();
        int now_hour = now.get(Calendar.HOUR_OF_DAY);
        int now_min = now.get(Calendar.MINUTE);

        String cityTZ = city.mTimeZone;

        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df1.setTimeZone(TimeZone.getTimeZone(cityTZ));
        String this_date_str = df1.format(now.getTime());

        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 用这个城市的时区的时间字符串转换到当前时区
        df2.setTimeZone(TimeZone.getDefault());
        Date this_date = new Date();

        try {
            this_date = df2.parse(this_date_str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int this_hour_24, this_hour, this_min_24, this_min;
        this_hour_24 = this_hour = this_date.getHours();
        this_min_24 = this_min = this_date.getMinutes();

        long this_time = this_date.getTime();

        String am_or_pm;// 显示ＡＭ还是ＰＭ

        if (isTime24) {
            am_or_pm = "";
        } else if (this_hour == 0) {
            am_or_pm = "  AM";
            this_hour = 12;
        } else if (this_hour == 12) {
            am_or_pm = "  PM";
            this_hour = 12;
        } else if (this_hour > 12) {
            am_or_pm = "  PM";
            this_hour = this_hour - 12;
        } else {
            am_or_pm = "  AM";
        }

        String str_hour;
        String str_min;
        str_hour = getAdd0String(this_hour);
        str_min = getAdd0String(this_min);
        String this_time_str = str_hour + ":" + str_min;

        city.setTimeString(this_time_str);
        city.setAmOrPm(am_or_pm);

        String tom_yes = "";// 昨天还是明天
        if (now_time > this_time && this_hour_24 * 60 + this_min_24 > now_hour * 60 + now_min) {
            tom_yes = mContext.getResources().getString(R.string.yestoday_lab);
        } else if (now_time < this_time && this_hour_24 * 60 + this_min_24 < now_hour * 60 + now_min) {
            tom_yes = mContext.getResources().getString(R.string.tomorrow_lab);
        }

        city.setTomOrYes(tom_yes);

        // int len = mDataList.size();
        for (int i = 0; i < mDataList.size(); i++) {
            WorldClockBean bean = mDataList.get(i);
            if (this_hour_24 == bean.time_hour_24 && this_min_24 == bean.time_min_24) {
                bean.cities_list.add(city);
                return;
            }
        }
        WorldClockBean data = new WorldClockBean();
        data.time_hour_24 = this_hour_24;
        data.time_min_24 = this_min_24;
        data.cities_list.add(city);
        mDataList.add(data);
    }

    private String getAdd0String(int time) {
        String str;
        if (time < 10) {
            str = "0" + time;
        } else {
            str = "" + time;
        }
        return str;
    }

    public void setShowLocalTime(boolean is) {
        isShowLocalTime = is;
        invalidate();
    }

    private boolean isMove = false;
    private float mLastX;
    private float mLastY;
    private float offset_angle;
    private int offset_min;
    private boolean isAimRunning = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isAimRunning) {
            return super.onTouchEvent(event);
        }

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:

            if(x<xCenter-radius-grayDotRadius*2 || x>xCenter+radius+grayDotRadius*2 || y<yCenter-radius-grayDotRadius*2 || y>yCenter+radius+grayDotRadius*2F){
                return super.onTouchEvent(event);
            }
            
            getParent().requestDisallowInterceptTouchEvent(true);

            mLastX = x;
            mLastY = y;
            offset_angle = 0;
            break;
        case MotionEvent.ACTION_MOVE:
            
            if(mLastX ==0 && mLastY ==0){
                return super.onTouchEvent(event);
            }
            
            isMove = true;

            float start = getAngle(mLastX, mLastY);
            float end = getAngle(x, y);

            // 如果是一、四象限，则直接end-start，角度值都是正值
            if (getQuadrant(x, y) == 1 || getQuadrant(x, y) == 4) {
                offset_angle += end - start;
            } else {// 二、三象限，色角度值是付值
                offset_angle += start - end;
            }

            offset_angle = offset_angle % 360;

            offset_min = (int) (24 * 60 * offset_angle / 360);

            updateTime(m_city_list, offset_min);

            mLastX = x;
            mLastY = y;

            Log.i("angle", "onTouchEvent off_set=" + offset_min);

            break;
        case MotionEvent.ACTION_UP:
            dealWithActionUp(x, y);
            getParent().requestDisallowInterceptTouchEvent(false);
            mLastX = 0;
            mLastY = 0;
            break;
        }

        // return super.onTouchEvent(arg0);
        return true;
    }

    public void dealWithActionUp(float x, float y) {
        if (isMove) {
            isMove = false;
            animRun(this);
            return;
        }
        isMove = false;

        // Toast.makeText(mContext, "x ="+x+",y="+y, Toast.LENGTH_SHORT).show();

        if (x > xCenter - radius / 2 && x < xCenter + radius / 2 && y > yCenter - radius / 2
                && y < yCenter + radius / 2) {
            setShowLocalTime(!isShowLocalTime);
        } else {
            for (int i = 0; i < mDataList.size(); i++) {
                WorldClockBean data = mDataList.get(i);
                if (data.cities_list.size() > 1) {// 有多个相同时间
                    if (x > data.centerX - grayDotRadius * 2 && x < data.centerX + grayDotRadius * 2
                            && y > data.centerY - grayDotRadius * 2 && y < data.centerY + grayDotRadius * 2) {// 点击范围为小圆点半径的两倍
                        data.show_index++;
                        if (data.show_index >= data.cities_list.size()) {
                            data.show_index = 0;
                        }
                        
                        for(int j=0;j<data.cities_list.size();j++){//其他的复位
                            if(j == data.show_index){
                                SharePreferencesUtils.setShowThisCity(mContext, data.cities_list.get(j).mCityId, true);
                            } else {
                                SharePreferencesUtils.setShowThisCity(mContext, data.cities_list.get(j).mCityId, false);
                            }
                        }
                        
                        invalidate();
                        return;
                    }
                }
            }
        }
    }

    private float getAngle(float xTouch, float yTouch) {
        double x = xTouch - xCenter;
        double y = yTouch - yCenter;
        return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
    }

    private int getQuadrant(float x, float y) {
        int tmpX = (int) (x - xCenter);
        int tmpY = (int) (y - yCenter);
        if (tmpX >= 0) {
            return tmpY >= 0 ? 4 : 1;
        } else {
            return tmpY >= 0 ? 3 : 2;
        }
    }

    public void animRun(final View view) {
        isAimRunning = true;
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "zx", 1.0F, 0.0F).setDuration(500);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                offset_min = (int)(offset_min*cVal);
                updateTime(m_city_list, offset_min);
            }
        });
        anim.addListener(new AnimatorListener() {

            @Override
            public void onAnimationCancel(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                isAimRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationStart(Animator arg0) {

            }

        });
        anim.start();
    }

}
