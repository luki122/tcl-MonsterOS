/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera;


import android.app.Activity; // MODIFIED by fei.hui, 2016-10-25,BUG-3167899
import android.app.Fragment;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/*MODIFIED-BEGIN by shunyin.zhang, 2016-04-19,BUG-1892480*/
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.RotateLayout;
/*MODIFIED-END by shunyin.zhang,BUG-1892480*/
import com.android.camera.ui.ZoomBar;
import com.tct.camera.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by administrator on 3/25/16.
 */
public class PoseFragment extends Fragment implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private View mView;
    private PoseSelectorShowingCallback mPoseSelectorShowingCallback; // MODIFIED by fei.hui, 2016-10-25,BUG-3167899
    private FrameLayout mClosePoseLayout; //MODIFIED by shunyin.zhang, 2016-04-19,BUG-1892480
    private TextView mWomanPoseText;
    private TextView mManPoseText;
    private TextView mAllPoseText;
    private PoseViewPager mPoseViewPager; // MODIFIED by fei.hui, 2016-09-29,BUG-2994050
    private ImageView mPoseDisplayImage;
    private FrameLayout mPoseDisplayLayout;
    private LinearLayout mPoseTitleLayout;
    private LinearLayout mPoseLayout;
    private FrameLayout mPoseBackLayout;
    /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-19,BUG-1892480*/
    private RotateLayout mDisplayLayout;
    private RotateImageView mBackImage;
    /*MODIFIED-END by shunyin.zhang,BUG-1892480*/
    private List<View> mViewList;
    private TypedArray[] mTypedArray;
    private TypedArray[] mPoseTypedArray;
    private int[] mTitleImageIds;
    /* MODIFIED-BEGIN by shunyin.zhang, 2016-04-27,BUG-2002683*/
    private final static String ZH_CN = "CN";
    private final static String ZH_TW = "TW";
    private String mLocaleLanguage;
    /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-05,BUG-2013029*/
    private ZoomBar mZoomBar;
    private ImageView zoomIn;
    private ImageView zoomOut;
    /* MODIFIED-END by shunyin.zhang,BUG-2013029*/

    private static final int POSE_MAX_NUMBER = 8;

    /* MODIFIED-BEGIN by fei.hui, 2016-10-25,BUG-3167899*/
    public interface PoseSelectorShowingCallback {
        void setIsPoseSelectorShowing(Boolean isPoseSelectorShowing);
    }

    @Override
    public void onAttach(Activity activity) {
        mPoseSelectorShowingCallback = (PoseSelectorShowingCallback) activity;
        super.onAttach(activity);
    }
    /* MODIFIED-END by fei.hui,BUG-3167899*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.pose_fragment_layout, null);
        mTypedArray = new TypedArray[]{getResources()
                .obtainTypedArray(R.array.woman_pose_style_icon), getResources()
                .obtainTypedArray(R.array.man_pose_style_icon), getResources()
                .obtainTypedArray(R.array.all_pose_style_icon)};
        mLocaleLanguage = getResources().getConfiguration().locale.getCountry();
        checkLanguage(mLocaleLanguage);
        /* MODIFIED-END by shunyin.zhang,BUG-2002683*/
        initView();
        initViewPage();
        for (int i = 0; i < mTypedArray.length; i++) {
            initGridView(i,mTypedArray[i],mPoseTypedArray[i]);
        }
        return mView;
    }

    private void initView() {
        mTitleImageIds = new int[]{R.drawable.woman_title, R.drawable.man_title, R.drawable.all_title};
        mPoseDisplayLayout = (FrameLayout) mView.findViewById(R.id.pose_display_layout);
        /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-19,BUG-1892480*/
        mDisplayLayout = (RotateLayout) mView.findViewById(R.id.display_layout);
        mClosePoseLayout = (FrameLayout) mView.findViewById(R.id.close_pose_layout);
        /*MODIFIED-END by shunyin.zhang,BUG-1892480*/
        mWomanPoseText = (TextView) mView.findViewById(R.id.women_pose_btn);
        mManPoseText = (TextView) mView.findViewById(R.id.men_pose_btn);
        mAllPoseText = (TextView) mView.findViewById(R.id.all_pose_btn);
        mPoseViewPager = (PoseViewPager) mView.findViewById(R.id.pose_viewpager); // MODIFIED by fei.hui, 2016-09-29,BUG-2994050
        mPoseDisplayImage = (ImageView) mView.findViewById(R.id.detail_image);
        mPoseBackLayout = (FrameLayout) mView.findViewById(R.id.back_layout);
        mPoseLayout = (LinearLayout) mView.findViewById(R.id.pose_show_layout);
        mPoseTitleLayout = (LinearLayout) mView.findViewById(R.id.pose_title_layout);
        /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-05,BUG-2013029*/
        mZoomBar = (ZoomBar)getActivity().findViewById(R.id.zoom_bar);
        zoomIn = (ImageView) getActivity().findViewById(R.id.zoom_in);
        zoomOut = (ImageView) getActivity().findViewById(R.id.zoom_out);
        /* MODIFIED-END by shunyin.zhang,BUG-2013029*/
        /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-19,BUG-1892480*/
        mBackImage = (RotateImageView) mView.findViewById(R.id.back_image);
        mClosePoseLayout.setOnClickListener(this);
        /*MODIFIED-END by shunyin.zhang,BUG-1892480*/
        mWomanPoseText.setOnClickListener(this);
        mManPoseText.setOnClickListener(this);
        mAllPoseText.setOnClickListener(this);
        mPoseBackLayout.setOnClickListener(this);

    }

    private void initViewPage() {
        LayoutInflater lf = getActivity().getLayoutInflater();
        View view1 = lf.inflate(R.layout.pose_gridview_layout, null);
        View view2 = lf.inflate(R.layout.pose_gridview_layout, null);
        View view3 = lf.inflate(R.layout.pose_gridview_layout, null);
        mViewList = new ArrayList<View>();
        mViewList.add(view1);
        mViewList.add(view2);
        mViewList.add(view3);
        PagerAdapter pagerAdapter = new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {

                return arg0 == arg1;
            }

            @Override
            public int getCount() {

                return mViewList.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView(mViewList.get(position));

            }

            @Override
            public int getItemPosition(Object object) {

                return super.getItemPosition(object);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(mViewList.get(position));
                return mViewList.get(position);
            }

        };
        mPoseViewPager.setAdapter(pagerAdapter);
        mPoseViewPager.addOnPageChangeListener(this);
    }

    private void initGridView(int position, TypedArray styleTypeArray, final TypedArray thumbTypedArray) {
        GridView poseGridView = (GridView) mViewList.get(position).findViewById(R.id.pose_gridview);
        GridAdapter adapter = new GridAdapter(getActivity(), styleTypeArray);
        poseGridView.setAdapter(adapter);
        poseGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPoseLayout.setVisibility(View.GONE);
                mPoseDisplayLayout.setVisibility(View.VISIBLE);
                mView.setClickable(false); // MODIFIED by fei.hui, 2016-09-29,BUG-2994050
                mPoseDisplayImage.setImageResource(thumbTypedArray.getResourceId(position, 0));
                mPoseSelectorShowingCallback.setIsPoseSelectorShowing(false); // MODIFIED by fei.hui, 2016-10-25,BUG-3167899
                if(position == POSE_MAX_NUMBER){
                    getActivity().getFragmentManager().beginTransaction().remove(PoseFragment.this).commit();
                    getActivity().findViewById(R.id.pose_layout).setVisibility(View.GONE);
                }
            }
        });

    }

    private void changeViewPageTitle(int position) {
        mPoseTitleLayout.setBackgroundResource(mTitleImageIds[position]);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        changeViewPageTitle(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.close_pose_layout: //MODIFIED by shunyin.zhang, 2016-04-19,BUG-1892480
                getActivity().getFragmentManager().beginTransaction().remove(this).commit();
                getActivity().findViewById(R.id.pose_composition_layout).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.pose_layout).setVisibility(View.GONE);
                hideZoom(); // MODIFIED by shunyin.zhang, 2016-05-05,BUG-2013029
                /* MODIFIED-BEGIN by fei.hui, 2016-10-25,BUG-3167899*/
                mPoseSelectorShowingCallback.setIsPoseSelectorShowing(false);
                break;
            case R.id.back_layout:
                mPoseLayout.setVisibility(View.VISIBLE);
                mPoseDisplayLayout.setVisibility(View.GONE);
                mPoseSelectorShowingCallback.setIsPoseSelectorShowing(true);
                /* MODIFIED-END by fei.hui,BUG-3167899*/
                break;
            case R.id.women_pose_btn:
                mPoseViewPager.setCurrentItem(0, true);
                break;
            case R.id.men_pose_btn:
                mPoseViewPager.setCurrentItem(1, true);
                break;
            case R.id.all_pose_btn:
                mPoseViewPager.setCurrentItem(2, true);
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
        if (mPoseDisplayLayout.getVisibility() == View.VISIBLE) {
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
    private void checkLanguage(String country) {
        switch (country) {
            case ZH_TW:
                mPoseTypedArray = new TypedArray[]{getResources()
                        .obtainTypedArray(R.array.woman_tw_pose_thumb_icon), getResources()
                        .obtainTypedArray(R.array.man_tw_pose_thumb_icon), getResources()
                        .obtainTypedArray(R.array.all_tw_pose_thumb_icon)};
                break;
            case ZH_CN:
            default:
                mPoseTypedArray = new TypedArray[]{getResources()
                        .obtainTypedArray(R.array.woman_pose_thumb_icon), getResources()
                        .obtainTypedArray(R.array.man_pose_thumb_icon), getResources()
                        .obtainTypedArray(R.array.all_pose_thumb_icon)};
                break;
        }
    }

    /* MODIFIED-BEGIN by feifei.xu, 2016-11-02,BUG-3299499*/
    //hide the back imageview button in mPoseDisplaylayout
    public void hidePoseBackView() {
        if (mPoseBackLayout != null && mPoseBackLayout.getVisibility() == View.VISIBLE) {
            mPoseBackLayout.setVisibility(View.GONE);
        }
    }

    //show the back imageview button in mPoseDisplaylayout
    public void showPoseBackView() {
        if (mPoseBackLayout != null && mPoseBackLayout.getVisibility() != View.VISIBLE) {
            mPoseBackLayout.setVisibility(View.VISIBLE);
        }
    }
    /* MODIFIED-END by feifei.xu,BUG-3299499*/

    /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-05,BUG-2013029*/
    private void hideZoom(){
        if (mZoomBar.getVisibility() == View .VISIBLE) {
            zoomIn.setVisibility(View.INVISIBLE);
            zoomOut.setVisibility(View.INVISIBLE);
        }
    }
    /* MODIFIED-END by shunyin.zhang,BUG-2013029*/
}
