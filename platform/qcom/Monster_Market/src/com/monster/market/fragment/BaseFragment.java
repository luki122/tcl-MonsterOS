package com.monster.market.fragment;

import android.app.Fragment;

public abstract class BaseFragment extends Fragment {

    protected String TAG = getClass().getSimpleName();

    public abstract void initViews();

    public abstract void initData();

}
