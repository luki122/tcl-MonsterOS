package com.android.camera.permission;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tct.camera.R;

/**
 * Created by Sean Scott on 8/31/16.
 */
public class WelcomeScreen extends Fragment {

    public interface Listener {
        void onLoadComplete();
    }

    private Listener mListener;
    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.perms_needed, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (mListener != null) {
            mListener.onLoadComplete();
        }
    }
}