package com.android.camera.permission;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tct.camera.R;

/**
 * Created by Sean Scott on 8/31/16.
 */
public class RationaleScreen extends Fragment {

    public interface Listener {
        void onExitClicked();
        void onSettingsClicked();
    }

    private Listener mListener;
    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.grant_access, container, false);
        LinearLayout exitLayout = (LinearLayout) view.findViewById(R.id.exit_layout);
        exitLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onExitClicked();
                }
            }
        });
        LinearLayout settingsLayout = (LinearLayout) view.findViewById(R.id.settings_layout);
        settingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onSettingsClicked();
                }
            }
        });
        return view;
    }
}