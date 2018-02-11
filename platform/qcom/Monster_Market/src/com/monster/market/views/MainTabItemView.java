package com.monster.market.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.monster.market.R;

/**
 * Created by xiaobin on 16-7-28.
 */
public class MainTabItemView extends FrameLayout {

    private ImageView iv_icon;
    private TextView tv_text;

    public MainTabItemView(Context context) {
        super(context);
    }

    public MainTabItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView();

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.main_tab_item);

        int imgSrc = mTypedArray.getResourceId(R.styleable.main_tab_item_tab_img, R.drawable.ic_launcher);
        int textSrc = mTypedArray.getResourceId(R.styleable.main_tab_item_tab_text, R.string.app_name);

        iv_icon.setImageResource(imgSrc);
        tv_text.setText(textSrc);

        mTypedArray.recycle();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.view_main_tab_item, this);

        iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
        tv_text = (TextView) view.findViewById(R.id.tv_text);
    }

}
