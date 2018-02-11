/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monster.launcher;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import java.util.ArrayList;

public class PageIndicatorManager{

    private PageIndicatorCircle mPageIndicatorCircle;
    private PageIndicatorCube mPageIndicatorCube;

    public Launcher mLauncher;

    public Launcher getmLauncher() {
        return mLauncher;
    }

    private Workspace mWorkspace;
    protected int pageIndicatorCount;

    public static boolean ONDRAGING = false;

    public PageIndicatorManager(Workspace mWorkspace, Context context, PageIndicatorCircle mPageIndicatorCircle, PageIndicatorCube mPageIndicatorCube, int count) {
        this.mPageIndicatorCircle = mPageIndicatorCircle;
        this.mPageIndicatorCube = mPageIndicatorCube;
        mLauncher = (Launcher) context;
        this.mWorkspace = mWorkspace;
        pageIndicatorCount = count;

    }

    public void updateMarker(int indext){
        if(mPageIndicatorCircle != null){
            mPageIndicatorCircle.updateMarker(indext,mWorkspace.getPageIndicatorMarker(indext));
        }
        if(mPageIndicatorCube != null){
            mPageIndicatorCube.updateMarker(indext,mWorkspace.getPageIndicatorMarkerForCube(indext));
        }
    }

    public void addMarker(int index){
        Log.d("--lj--","addMarker : " + index);
        if(mPageIndicatorCircle !=null){
            mPageIndicatorCircle.addMarker(index,mWorkspace.getPageIndicatorMarker(index),true);
        }
        if(mPageIndicatorCube != null){
            mPageIndicatorCube.addMarker(index,mWorkspace.getPageIndicatorMarkerForCube(index),true);
        }
    }

    public void addMarkerIfNeed(int index){
        Log.d("--lj--","addMarkerIfNeed : " + index);
        //don't add cicle indicator
//        if(mPageIndicatorCircle !=null){
//            mPageIndicatorCircle.addMarker(index,mWorkspace.getPageIndicatorMarker(index),true);
//        }
        if(mPageIndicatorCube != null){
            mPageIndicatorCube.addMarker(index,mWorkspace.getPageIndicatorMarkerForCube(index),true);
        }
    }
    public void addMarkerAfterDragEndIfNeed(){
        Log.d("--lj--","addMarkerAfterDragEndIfNeed");
        if(mPageIndicatorCircle != null && mWorkspace.getPageCount() == mPageIndicatorCircle.mMarkers.size()+1){
            mPageIndicatorCircle.addMarker(mPageIndicatorCircle.mMarkers.size(),mWorkspace.getPageIndicatorMarker(mPageIndicatorCircle.mMarkers.size()),true);
        }
    }
    public void removeMarker(int index){
        Log.d("--lj--","removeMarker : " + index);
        if(mPageIndicatorCircle !=null){
            mPageIndicatorCircle.removeMarker(index, true);
        }
        if(mPageIndicatorCube!= null){
            mPageIndicatorCube.removeMarker(index, true);
        }
    }

    public void clear(){
        if(mPageIndicatorCircle !=null){
            mPageIndicatorCircle.clear();
        }
        if(mPageIndicatorCube!= null){
            mPageIndicatorCube.clear();
        }
    }

    public void reSetPageIndicatorDelay(){
        mWorkspace.postDelayed(new Runnable() {
            @Override
            public void run() {
                reSetPageIndicator();
            }
        },520);
    }
    public synchronized void reSetPageIndicator(){
        Log.d("--lj--","reSetPageIndicator");
        if (mPageIndicatorCircle == null || mPageIndicatorCircle.mMarkers == null) {
            mWorkspace.noNeedToAddPageIndicatorMaker = false;
            mWorkspace.noNeedToRemovePageIndicatorMaker = false;
            return;
        }
        int workspaceSize = mWorkspace.getPageCount();
        int circleIndicatorSize = mPageIndicatorCircle.mMarkers.size();
        int cubeIndicatorSize = mPageIndicatorCube.mMarkers.size();
        if(mPageIndicatorCircle !=null && workspaceSize != circleIndicatorSize){
            if(workspaceSize > circleIndicatorSize){
                for(int i = 0 ; i < workspaceSize - circleIndicatorSize;i++){
                    int index = circleIndicatorSize+i;
                    mPageIndicatorCircle.addMarker(index,mWorkspace.getPageIndicatorMarker(index),false);
                }
            }else if(workspaceSize < circleIndicatorSize){
                for(int i = 0 ; i < circleIndicatorSize - workspaceSize;i++){
                    int index = circleIndicatorSize-i-1;
                    mPageIndicatorCircle.removeMarker(index, false);
                }
            }
        }
        if(mPageIndicatorCube !=null && workspaceSize != cubeIndicatorSize){
            if(workspaceSize > cubeIndicatorSize){
                for(int i = 0 ; i < workspaceSize - cubeIndicatorSize;i++){
                    int index = cubeIndicatorSize+i;
                    mPageIndicatorCube.addMarker(index,mWorkspace.getPageIndicatorMarker(index),false);
                }
            }else if(workspaceSize < cubeIndicatorSize){
                for(int i = 0 ; i < cubeIndicatorSize - workspaceSize;i++){
                    int index = cubeIndicatorSize-i-1;
                    mPageIndicatorCube.removeMarker(index, false);
                }
            }
        }
        mWorkspace.noNeedToAddPageIndicatorMaker = false;
        mWorkspace.noNeedToRemovePageIndicatorMaker = false;
    }

    public void indicatorsAttachedToWindow(){
        if(mPageIndicatorCircle !=null && mPageIndicatorCube!= null){
            ArrayList<PageIndicator.PageMarkerResources> markers = new ArrayList<PageIndicator.PageMarkerResources>();
            for (int i = 0; i < pageIndicatorCount; ++i) {
                markers.add(mWorkspace.getPageIndicatorMarker(i));
            }
            mPageIndicatorCircle.addMarkers(markers, true);

            ArrayList<PageIndicator.PageMarkerResources> markersCube = new ArrayList<PageIndicator.PageMarkerResources>();
            for (int i = 0; i < pageIndicatorCount; ++i) {
                markersCube.add(mWorkspace.getPageIndicatorMarkerForCube(i));
            }
            mPageIndicatorCube.addMarkers(markersCube, true);

            setOnClickListener();
        }
    }

    public void indicatorsDetachedToWindow(){
        mPageIndicatorCircle = null;
        mPageIndicatorCube = null;
    }


    public void setOnClickListener(){
        if(mPageIndicatorCircle !=null && mPageIndicatorCube!= null){
            AccessibilityManager am = (AccessibilityManager)
                    mLauncher.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (!am.isTouchExplorationEnabled()) {
                return ;
            }
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mLauncher.showOverviewMode(true);
                }
            };
            mPageIndicatorCircle.setOnClickListener(listener);
        }
    }

    protected boolean initialized(){
        return (mPageIndicatorCircle != null && mPageIndicatorCube != null);
    }

    public void showCubeIndicator(){
//        Log.d("--lijun--","showCubeIndicator");
        if(!ONDRAGING && mPageIndicatorCircle !=null && mPageIndicatorCube!= null) {
//            mPageIndicatorCircle.setVisibility(View.INVISIBLE);
            mPageIndicatorCircle.setOnTouchListener(null);

//            mPageIndicatorCube.setVisibility(View.VISIBLE);
            mWorkspace.refreshviewCaches();
            ONDRAGING = true;
            mPageIndicatorCube.initLeftRightIndicator(false);
            if(mLauncher != null && mLauncher.getDragController()!= null && !mLauncher.getDragController().isContainDropTarget(mPageIndicatorCube)){
                mLauncher.getDragController().addDropTarget(mPageIndicatorCube);
            }
        }
    }
    public void hideCubeIndicator(){
//        Log.d("--lijun--","hideCubeIndicator");
        if(mPageIndicatorCircle !=null && mPageIndicatorCube!= null) {
//            mPageIndicatorCircle.setVisibility(View.VISIBLE);
            mPageIndicatorCircle.setOnTouchListener(mCircleIndicatorTouchListener);

//            mPageIndicatorCube.setVisibility(View.INVISIBLE);
            ONDRAGING = false;
            if(mLauncher != null && mLauncher.getDragController()!= null){
                mLauncher.getDragController().removeDropTarget(mPageIndicatorCube);
            }
        }
    }

    public void hideAllIndicators(){
        if(mPageIndicatorCircle !=null && mPageIndicatorCube!= null) {
            mPageIndicatorCircle.setVisibility(View.INVISIBLE);
            mPageIndicatorCircle.setOnTouchListener(null);

            mPageIndicatorCube.setVisibility(View.INVISIBLE);
            ONDRAGING = false;
            if(mLauncher.getDragController() != null){
                mLauncher.getDragController().removeDropTarget(mPageIndicatorCube);
            }
        }
    }

    private final View.OnTouchListener mCircleIndicatorTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_MOVE: {
                    break;
                }
                case MotionEvent.ACTION_DOWN: {
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    break;
                }
            }
            return false;
        }
    };
}
