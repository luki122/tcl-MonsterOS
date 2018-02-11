/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera;

import android.app.Fragment;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

/*MODIFIED-BEGIN by shunyin.zhang, 2016-04-19,BUG-1892480*/
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.RotateLayout;
/*MODIFIED-END by shunyin.zhang,BUG-1892480*/
import com.android.camera.ui.ZoomBar;
import com.tct.camera.R;

/**
 * Created by administrator on 3/25/16.
 */
public class CompositionFragment extends Fragment implements View.OnClickListener {

    private View mView;
    private FrameLayout mCompositionLayout;
    private FrameLayout mCompositionDisplayLayout;
    /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-19,BUG-1892480*/
    private RotateLayout mDisplayLayout;
    private FrameLayout mCloseCompositionLayout;
    private FrameLayout mBackLayout;
    private ImageView mDisplayImage;
    private GridView mGridView;
    private RotateImageView mBackImage;
    /*MODIFIED-END by shunyin.zhang,BUG-1892480*/
    private TypedArray mTypedArray;
    private TypedArray mComposeTypedArray;
    /* MODIFIED-BEGIN by shunyin.zhang, 2016-04-27,BUG-2002683*/
    private final static String ZH_CN = "CN";
    private final static String ZH_TW = "TW";
    private String mLocaleLanguage;
    /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-05,BUG-2013029*/
    private ZoomBar mZoomBar;
    private ImageView zoomIn;
    private ImageView zoomOut;
    private RotateImageView mPoseImage;
    /* MODIFIED-END by shunyin.zhang,BUG-2013029*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.composition_fragment_layout, null);
        mTypedArray = getResources()
                .obtainTypedArray(R.array.compose_style_icon);
        mLocaleLanguage = getResources().getConfiguration().locale.getCountry();
        checkLanguage(mLocaleLanguage);
        /* MODIFIED-END by shunyin.zhang,BUG-2002683*/
        initView();
        initGridView();
        return mView;
    }

    private void initView() {
        /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-05,BUG-2013029*/
        mZoomBar = (ZoomBar)getActivity().findViewById(R.id.zoom_bar);
        zoomIn = (ImageView) getActivity().findViewById(R.id.zoom_in);
        zoomOut = (ImageView) getActivity().findViewById(R.id.zoom_out);
        mPoseImage = (RotateImageView) getActivity().findViewById(R.id.pose_image);
        /* MODIFIED-END by shunyin.zhang,BUG-2013029*/
        mCompositionLayout = (FrameLayout) mView.findViewById(R.id.composition_show_layout);
        mCompositionDisplayLayout = (FrameLayout) mView.findViewById(R.id.composition_display_layout);
        /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-19,BUG-1892480*/
        mCloseCompositionLayout = (FrameLayout) mView.findViewById(R.id.close_composition_layout);
        mDisplayImage = (ImageView) mView.findViewById(R.id.detail_image);
        mDisplayLayout = (RotateLayout)mView.findViewById(R.id.display_layout);
        mBackLayout = (FrameLayout) mView.findViewById(R.id.back_layout);
        mGridView = (GridView) mView.findViewById(R.id.composition_girdview);
        mBackImage = (RotateImageView) mView.findViewById(R.id.back_image);
        mCloseCompositionLayout.setOnClickListener(this);
        /*MODIFIED-END by shunyin.zhang,BUG-1892480*/
        mBackLayout.setOnClickListener(this);
    }

    private void initGridView() {
        GridAdapter adapter = new GridAdapter(getActivity(), mTypedArray);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCompositionLayout.setVisibility(View.GONE);
                mCompositionDisplayLayout.setVisibility(View.VISIBLE);
                mDisplayImage.setImageResource(mComposeTypedArray.getResourceId(position, 0));
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.close_composition_layout: //MODIFIED by shunyin.zhang, 2016-04-19,BUG-1892480
                getActivity().getFragmentManager().beginTransaction().remove(this).commit();
                getActivity().findViewById(R.id.pose_composition_layout).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.composition_layout).setVisibility(View.GONE);
                hideZoom(); // MODIFIED by shunyin.zhang, 2016-05-05,BUG-2013029
                break;
            case R.id.back_layout:
                mCompositionLayout.setVisibility(View.VISIBLE);
                mCompositionDisplayLayout.setVisibility(View.GONE);
                break;
            default:
                break;
        }

    }
    /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-19,BUG-1892480*/
    private void addRotatableToListenerPool(CameraActivity activity) {
        activity.addRotatableToListenerPool(new Rotatable.RotateEntity(mBackImage, true));
        activity.addRotatableToListenerPool(new Rotatable.RotateEntity(mDisplayLayout, true));
    }

    private void removeRotatableToListenerPool(CameraActivity activity) {
        if (mCompositionDisplayLayout.getVisibility() == View.VISIBLE) {
            activity.removeRotatableFromListenerPool(mBackImage.hashCode());
            activity.removeRotatableFromListenerPool(mDisplayLayout.hashCode());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        addRotatableToListenerPool((CameraActivity) getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        removeRotatableToListenerPool((CameraActivity) getActivity());
    }
    /*MODIFIED-END by shunyin.zhang,BUG-1892480*/
    private void checkLanguage(String country){
        switch (country) {
            case ZH_TW:
                mComposeTypedArray = getResources()
                        .obtainTypedArray(R.array.compose_tw_thumb_icon);
                break;
            case ZH_CN:
            default:
                mComposeTypedArray = getResources()
                        .obtainTypedArray(R.array.compose_thumb_icon);
                break;
        }
    }

    /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-05,BUG-2013029*/
    private void hideZoom(){
        if (mZoomBar.getVisibility() == View .VISIBLE && mPoseImage.getVisibility() == View.VISIBLE) {
            zoomIn.setVisibility(View.INVISIBLE);
            zoomOut.setVisibility(View.INVISIBLE);
        }
    }
    /* MODIFIED-END by shunyin.zhang,BUG-2013029*/
}
