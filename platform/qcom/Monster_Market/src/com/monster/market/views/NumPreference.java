package com.monster.market.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.monster.market.R;

import mst.preference.Preference;

/**
 * Created by xiaobin on 16-10-11.
 */
public class NumPreference extends Preference {

    private View view;
    private int sum = 0;

    public NumPreference(Context context) {
        super(context);
    }

    public NumPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        setWidgetLayoutResource(R.layout.preference_num_view);

        view = super.onCreateView(parent);
        TextView ut = (TextView) view.findViewById(R.id.message);

        if (sum > 0 && sum < 100) {
            ut.setVisibility(View.VISIBLE);
            ut.setText(String.valueOf(sum));
        } else if (sum >= 100) {
            ut.setVisibility(View.VISIBLE);
            ut.setText("99+");
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

        if (sum > 0 && sum < 100) {
            ut.setVisibility(View.VISIBLE);
            ut.setText(String.valueOf(sum));
        } else if (sum >= 100) {
            ut.setVisibility(View.VISIBLE);
            ut.setText("99+");
        } else {
            ut.setVisibility(View.GONE);
            ut.setText(String.valueOf(0));
        }
    }

}
