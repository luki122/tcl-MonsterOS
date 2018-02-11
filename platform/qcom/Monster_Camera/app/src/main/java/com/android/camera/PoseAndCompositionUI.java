/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
/* MODIFIED-BEGIN by shunyin.zhang, 2016-05-05,BUG-2013029*/
import android.widget.ImageView;

import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.ZoomBar;
import com.tct.camera.R;

/**
 * Created by administrator on 3/24/16.
 */
public class PoseAndCompositionUI implements View.OnClickListener,ZoomBar.ControlZoomBarCallback {
/* MODIFIED-END by shunyin.zhang,BUG-2013029*/
    protected final CameraActivity mActivity;
    private final PhotoController mController;
    private final View mRootView;
    private FrameLayout mPoseTipsLayout;
    private RotateImageView mPoseImage;
    private RotateImageView mCompositionImage;
    /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-19,BUG-1892480*/
    private PoseFragment mPoseFragment;
    private CompositionFragment mCompositionFragment;
    /*MODIFIED-END by shunyin.zhang,BUG-1892480*/
    private final static String POSE_TAG = "poseFragment";
    private final static String COMPOSE_TAG = "compositionFragment";
    private FrameLayout mPoseDetailLayout;
    private FrameLayout mComposeDetailLayout;
    /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-05,BUG-2013029*/
    private ZoomBar mZoomBar;
    private ImageView zoomIn;
    private ImageView zoomOut;
    /* MODIFIED-END by shunyin.zhang,BUG-2013029*/


    public PoseAndCompositionUI(CameraActivity activity, PhotoController controller, View parent) {
        mActivity = activity;
        mController = controller;
        mRootView = parent;
        initView();
    }


    public void initView() {
        mPoseTipsLayout = (FrameLayout) mRootView.findViewById(R.id.pose_composition_layout);
        mPoseImage = (RotateImageView) mRootView.findViewById(R.id.pose_image);
        mCompositionImage = (RotateImageView) mRootView.findViewById(R.id.composition_image);
        mPoseImage.setOnClickListener(this);
        mCompositionImage.setOnClickListener(this);
        mPoseDetailLayout = (FrameLayout)mRootView.findViewById(R.id.pose_layout);
        mComposeDetailLayout = (FrameLayout)mRootView.findViewById(R.id.composition_layout);
        /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-05,BUG-2013029*/
        mZoomBar = (ZoomBar)mRootView.findViewById(R.id.zoom_bar);
        zoomIn = (ImageView) mRootView.findViewById(R.id.zoom_in);
        zoomOut = (ImageView) mRootView.findViewById(R.id.zoom_out);
        /* MODIFIED-END by shunyin.zhang,BUG-2013029*/
    }

    @Override
    public void onClick(View v) {

        FragmentManager fragmentManager = mActivity.getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch (v.getId()) {
            case R.id.pose_image:
                mPoseDetailLayout.setVisibility(View.VISIBLE);
                mPoseTipsLayout.setVisibility(View.GONE);
                mPoseFragment = new PoseFragment();
                transaction.add(R.id.pose_layout, mPoseFragment, POSE_TAG);
                break;
            case R.id.composition_image:
                mComposeDetailLayout.setVisibility(View.VISIBLE);
                mPoseTipsLayout.setVisibility(View.GONE);
                mCompositionFragment = new CompositionFragment();
                transaction.add(R.id.composition_layout, mCompositionFragment, COMPOSE_TAG);
                break;
            default:
                break;

        }
        transaction.commit();
        showZoom(); // MODIFIED by shunyin.zhang, 2016-05-05,BUG-2013029

    }

    public void hidePoseLayout() {
        FragmentManager fragmentManager = mActivity.getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-19,BUG-1892480*/
        transaction.remove(fragmentManager.findFragmentByTag(POSE_TAG));
        mPoseDetailLayout.setVisibility(View.GONE);
        transaction.commit();
    }

    public void hideComposeLayout() {
        FragmentManager fragmentManager = mActivity.getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragmentManager.findFragmentByTag(COMPOSE_TAG));
        mComposeDetailLayout.setVisibility(View.GONE);
        transaction.commit();
    }

    public void  setPoseTipsVisibility(boolean isVisible) {
        if (isVisible) {
            if (mController.isImageCaptureIntent()) {
                mPoseTipsLayout.setVisibility(View.GONE);
            } else {
                mPoseTipsLayout.setVisibility(View.VISIBLE);
            }
        } else {
            mPoseTipsLayout.setVisibility(View.GONE);
        }
    }

    public boolean getPoseVisibility(){
        FragmentManager fragmentManager = mActivity.getFragmentManager();
        if (fragmentManager.findFragmentByTag(POSE_TAG) != null) {
            if (mPoseFragment == null) {
                hidePoseLayout();
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean getComposeVisibility(){
        FragmentManager fragmentManager = mActivity.getFragmentManager();
        if (fragmentManager.findFragmentByTag(COMPOSE_TAG) != null) {
            if (mCompositionFragment == null) {
                hideComposeLayout();
                return false;
            }
            return true;
        }
        return false;
        /*MODIFIED-END by shunyin.zhang,BUG-1892480*/
    }

    public void onCameraOpened() {
        /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-04,BUG-2013195*/
        if (!mController.isImageCaptureIntent()) {
            addRotatableToListenerPool();
            /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-05,BUG-2013029*/
            mZoomBar.setControlZoomBarCallback(this);
        }
    }

    public void onCameraDestroy() {
        removeRotatableToListenerPool();
        mZoomBar.resetControlZoomBarCallback();
        /* MODIFIED-END by shunyin.zhang,BUG-2013029*/
    }
    /* MODIFIED-END by shunyin.zhang,BUG-2013195*/
    private void addRotatableToListenerPool() {
        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mPoseImage, true));
        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mCompositionImage, true));
    }

    private void removeRotatableToListenerPool() {
        mActivity.removeRotatableFromListenerPool(mPoseImage.hashCode());
        mActivity.removeRotatableFromListenerPool(mCompositionImage.hashCode());
    }

    /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-05,BUG-2013029*/
    @Override
    public void onVisibility(boolean isVisible) {
        if (isVisible) {
            if (mPoseTipsLayout.getVisibility() == View.VISIBLE && mPoseImage.getVisibility() == View .VISIBLE) {
                zoomIn.setVisibility(View.INVISIBLE);
                zoomOut.setVisibility(View.INVISIBLE);
            } else {
                zoomIn.setVisibility(View.VISIBLE);
                zoomOut.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showZoom(){
        if (mZoomBar.getVisibility() == View .VISIBLE) {
            zoomIn.setVisibility(View.VISIBLE);
            zoomOut.setVisibility(View.VISIBLE);
        }
    }
    /* MODIFIED-END by shunyin.zhang,BUG-2013029*/
}
