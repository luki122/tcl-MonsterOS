package cn.tcl.weather.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.tcl.weather.R;
import cn.tcl.weather.bean.CityWeatherInfo;

/**
 * Created by pengsong on 16-11-23.
 */
public class CustomMainPageTempView extends LinearLayout {

    private LayoutInflater mInflater;
    private ImageView mMinusSymbol;
    private TextView mTempTV;
    private ImageView mDegreeSymbol;

    public CustomMainPageTempView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public CustomMainPageTempView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CustomMainPageTempView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomMainPageTempView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context){
        mInflater = LayoutInflater.from(context);
        addViewToCurrentLayout();
        mMinusSymbol = (ImageView) findViewById(R.id.city_weather_symbol_minus);
        mTempTV = (TextView) findViewById(R.id.city_weather_temp);
        mDegreeSymbol = (ImageView) findViewById(R.id.city_weather_symbol_degree);
    }

    private void addViewToCurrentLayout(){
        View view = mInflater.inflate(R.layout.other_main_page_temp_layout,null);
        addView(view);
    }

    public void setCityWeatherTemp(CityWeatherInfo weatherInfo) {
        mTempTV.setText(weatherInfo.getAbsTempWithoutSymbol());
        if (!weatherInfo.getTempWithSymbol().equals("--")) {
            if (weatherInfo.getTempValue() != CityWeatherInfo.DEFAULT_ERRO_TEMP_VALUE && weatherInfo.getTempValue() < 0) {
                mMinusSymbol.setVisibility(View.VISIBLE);
            } else {
                mMinusSymbol.setVisibility(View.GONE);
            }
            mDegreeSymbol.setVisibility(View.VISIBLE);
        } else {
            mDegreeSymbol.setVisibility(View.VISIBLE);
        }
    }
}
