package com.monster.paymentsecurity.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.monster.paymentsecurity.diagnostic.AppRisk;
import com.monster.paymentsecurity.R;

import java.util.List;

/**
 * Created by logic on 16-12-6.
 */
public class ListAppRiskAdapter extends ArrayAdapter<AppRisk> {

    public ListAppRiskAdapter(Context context, List<AppRisk> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

//        AppRisk item = getItem(position);

        View view = convertView;
        if (view == null) {
            LayoutInflater li =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.card_risk_app, parent, false);
        }

        return view;
    }


    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }
}
