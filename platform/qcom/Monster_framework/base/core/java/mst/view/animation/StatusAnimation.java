/*
 * Copyright (C) 2007 The Android Open Source Project
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

package mst.view.animation;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;


/**
 * Defines common utilities for working with animations.
 *
 */
public class StatusAnimation implements ValueAnimator.AnimatorUpdateListener,Animator.AnimatorListener{

    private int mChildWidth = 0;
    private int mParentWidth = 0;
    private View mParent;
    private View mChild;
    private ValueAnimator mAnimator;
    private final static int LEFT_IN = 1;
    private final static int RIGHT_IN = 2;
    private final static int TOP_IN = 3;
    private final static int BOTTOM_IN = 4;
    private int mType = LEFT_IN;

    private boolean isRelease = false;
    private boolean isOpen = false;

    private ArrayList<View> mAnimViews;


    public StatusAnimation(View child, View parent){
        mChild = child;
        mParent = parent;
        mAnimViews = new ArrayList<>();
        createAnimation();
    }

    public StatusAnimation(){
        mAnimViews = new ArrayList<>();
        createAnimation();
    }

    private void createAnimation(){
        mAnimator = ValueAnimator.ofFloat(0f,1f);
        mAnimator.addUpdateListener(this);
        mAnimator.addListener(this);
    }

    public void setType(int type){
        mType = type;
    }

    public void setDuration(long d){
        if(mAnimator != null) {
            mAnimator.setDuration(d);
        }
    }

    public void addAnimationView(View v){
        mAnimViews.add(v);
    }

    public void setInterpolator(Interpolator p){
        if(mAnimator != null) {
            mAnimator.setInterpolator(p);
        }
    }

    private void init(final boolean isopen, final View child, final View parent){
        if(mParentWidth == 0) {
            mParentWidth = parent.getWidth();
        }
        if(mChildWidth == 0){
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            int margin = 0;
            switch (mType) {
                case LEFT_IN:
                    if(lp instanceof RelativeLayout.LayoutParams) {
                        margin = ((RelativeLayout.LayoutParams)lp).leftMargin;
                    }else if(lp instanceof LinearLayout.LayoutParams){
                        margin = ((LinearLayout.LayoutParams)lp).leftMargin;
                    }else if(lp instanceof FrameLayout.LayoutParams){
                        margin = ((FrameLayout.LayoutParams)lp).leftMargin;
                    }
                    break;
                case RIGHT_IN:
                    if(lp instanceof RelativeLayout.LayoutParams) {
                        margin = ((RelativeLayout.LayoutParams)lp).rightMargin;
                    }else if(lp instanceof LinearLayout.LayoutParams){
                        margin = ((LinearLayout.LayoutParams)lp).rightMargin;
                    }else if(lp instanceof FrameLayout.LayoutParams){
                        margin = ((FrameLayout.LayoutParams)lp).rightMargin;
                    }
                    break;
                case TOP_IN:
                    if(lp instanceof RelativeLayout.LayoutParams) {
                        margin = ((RelativeLayout.LayoutParams)lp).topMargin;
                    }else if(lp instanceof LinearLayout.LayoutParams){
                        margin = ((LinearLayout.LayoutParams)lp).topMargin;
                    }else if(lp instanceof FrameLayout.LayoutParams){
                        margin = ((FrameLayout.LayoutParams)lp).topMargin;
                    }
                    break;
                case BOTTOM_IN:
                    if(lp instanceof RelativeLayout.LayoutParams) {
                        margin = ((RelativeLayout.LayoutParams)lp).bottomMargin;
                    }else if(lp instanceof LinearLayout.LayoutParams){
                        margin = ((LinearLayout.LayoutParams)lp).bottomMargin;
                    }else if(lp instanceof FrameLayout.LayoutParams){
                        margin = ((FrameLayout.LayoutParams)lp).bottomMargin;
                    }
                    break;
            }
            int cwidth = child.getWidth();
            mChildWidth = cwidth == 0 ? 0 : cwidth+margin;
        }
//                    android.util.Log.e("test","StatusAnimation init width = "+mParentWidth);
        switch (mType) {
            case LEFT_IN:
                parent.setTranslationX(isopen?0:-mChildWidth);
                break;
            case RIGHT_IN:
                parent.setTranslationX(isopen?0:mChildWidth);
                break;
            case TOP_IN:
                parent.setTranslationY(isopen?0:-mChildWidth);
                break;
            case BOTTOM_IN:
                parent.setTranslationY(isopen?0:mChildWidth);
                break;
        }
    }

    public void setStatus(final boolean isopen, boolean relayout, final View child, final View parent){
        isOpen = isopen;
        if(parent != null && child != null) {
            if(mParentWidth == 0 || mChildWidth == 0) {
                parent.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        init(isopen, child, parent);
                        parent.getViewTreeObserver().removeOnPreDrawListener(this);
                        return true;
                    }
                });
            }else{
                init(isopen, child, parent);
            }
            if (relayout) {
                parent.requestLayout();
            }
        }

    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        mAnimViews.clear();
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        setStatus(isOpen,false, mChild, mParent);
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if(mAnimViews.size() > 0){
            for(View parent : mAnimViews){
                move(parent,(float)animation.getAnimatedValue());
            }
        }else {
            move(mParent, (float)animation.getAnimatedValue());
        }
    }

    private void move(View v,float c){
        if(v != null){
            int d = (int)(c*mChildWidth);
            if(isOpen){
                d = mChildWidth-d;
            }
            switch (mType) {
                case LEFT_IN:
                    v.setTranslationX(-d);
                    break;
                case RIGHT_IN:
                    v.setTranslationX(d);
                    break;
                case TOP_IN:
                    v.setTranslationY(-d);
                    break;
                case BOTTOM_IN:
                    v.setTranslationY(d);
                    break;
            }
        }
    }


    public void start(boolean open){
        if(isOpen != open || mAnimViews.size() > 0) {
            isOpen = open;
            if (mAnimator != null) {
//                android.util.Log.e("test","StatusAnimation : start >>>>>>>>>>");
                mAnimator.start();
            }
        }
    }


    public void cancel(){
        if(mAnimator != null) {
            mAnimator.cancel();
        }
    }

    public void release(){
        mAnimator.removeAllUpdateListeners();
        mAnimator.removeUpdateListener(this);
        isRelease = true;
        mAnimator = null;
        mParent = null;
    }

    public boolean isOpened(){
        return isOpen;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
//        android.util.Log.e("test","StatusAnimation ~~~~~~~~~~~~~~~~~~~~~~");
        release();
    }
}
