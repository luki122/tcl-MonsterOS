package com.monster.market.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.monster.market.R;

import mst.preference.Preference;

/**
 * Created by xiaobin on 16-8-17.
 */
public class MarketPreference extends Preference {

    private View view;
    private int sum = 0;

    public MarketPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MarketPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MarketPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarketPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        setWidgetLayoutResource(R.layout.view_market_manager_pref);

        view = super.onCreateView(parent);
        TextView ut = (TextView) view.findViewById(R.id.message);

        if (sum > 0) {
            ut.setVisibility(View.VISIBLE);
            ut.setText(String.valueOf(sum));
        } else {
            ut.setVisibility(View.GONE);
            ut.setText(String.valueOf(0));
        }
        setView(view);
        return view;
    }

    private void setView(View view) {
        this.view = view;
    }

    private View getView() {
        return view;
    }

    public void setSum(int sum)
    {
        this.sum = sum;
    }

    public void setDisUpSum(int sum) {
        View v = getView();
        if (v == null) {
            return;
        }
        TextView ut = (TextView) v.findViewById(R.id.message);

        if (sum > 0) {
            ut.setVisibility(View.VISIBLE);
            ut.setText(String.valueOf(sum));
        } else {
            ut.setVisibility(View.GONE);
            ut.setText(String.valueOf(0));
        }
    }

}
