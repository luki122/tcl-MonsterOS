package com.monster.paymentsecurity.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import mst.preference.Preference;
import com.monster.paymentsecurity.R;


/**
 * Created by sandysheny on 16-11-28.
 */

public class PayListPreference extends Preference {
    private PayListCard payListCard;

    public PayListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PayListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PayListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PayListPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        setLayoutResource(R.layout.layout_paylist_card);

        View view = super.onCreateView(parent);
        payListCard = new PayListCard(getContext(), view);

        return view;
    }

    public void refresh() {
       if (payListCard != null) {
           payListCard.initData(true);
       }
    }


}
