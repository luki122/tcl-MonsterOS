package com.android.camera.ui;


import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Created by hoperun on 12/28/15.
 */
public class MT_AnimationController {
    private static final String TAG = "AnimationController";

    private static final int ANIM_DURATION = 180;
    private int mCenterDotIndex = 0;
    private int mDirectionDotIndex = 0;

    private ViewGroup[] mDirectionIndicators;
    private ViewGroup mCenterArrow;

    private Handler mHanler = new Handler();

    public MT_AnimationController(ViewGroup[] indicators, ViewGroup arrow) {
        mDirectionIndicators = indicators;
        mCenterArrow = arrow;
    }

    public void startDirectionAnimation() {
        Log.i(TAG, "[startDirectionAnimation]...");
        mDirectionDotIndex = 0;
        mApplyDirectionAnim.run();
    }

    public void stopDirectionAnimation() {
        // do nothing
    }

    public void startCenterAnimation() {
        Log.i(TAG, "[startCenterAnimation]...");
        mCenterDotIndex = 0;
        mApplyCenterArrowAnim.run();
    }

    public void stopCenterAnimation() {
        Log.i(TAG, "[stopCenterAnimation]...");
        if (mCenterArrow != null) {
            for (int i = 0; i < mCenterArrow.getChildCount(); i++) {
                mCenterArrow.getChildAt(i).clearAnimation();
            }
        }
    }

    private Runnable mApplyCenterArrowAnim = new Runnable() {
        private int dotCount = 0;

        public void run() {
            if (dotCount == 0) {
                dotCount = mCenterArrow.getChildCount();
            }
            if (dotCount <= mCenterDotIndex) {
                Log.w(TAG, "[run]mApplyCenterArrowAnim return,dotCount = " + dotCount
                        + ",mCenterDotIndex =" + mCenterDotIndex);
                return;
            }
            AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
            alpha.setDuration(ANIM_DURATION * 8);
            alpha.setRepeatCount(Animation.INFINITE);

            if (mCenterArrow != null) {
                mCenterArrow.getChildAt(mCenterDotIndex).startAnimation(alpha);
            }
            alpha.startNow();
            mCenterDotIndex++;
            mHanler.postDelayed(this, ANIM_DURATION * 2 / dotCount);
        }
    };

    private Runnable mApplyDirectionAnim = new Runnable() {
        private int dotCount = 0;

        public void run() {
            for (ViewGroup viewGroup : mDirectionIndicators) {
                if (viewGroup == null) {
                    Log.w(TAG, "[run]viewGroup is null,return!");
                    return;
                }
            }
            if (dotCount == 0) {
                dotCount = mDirectionIndicators[0].getChildCount();
            }

            if (dotCount <= mDirectionDotIndex) {
                Log.i(TAG, "[run]mApplyDirectionAnim,return,dotCount = " + dotCount
                        + ",mCenterDotIndex =" + mCenterDotIndex);
                return;
            }
            AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
            alpha.setDuration(ANIM_DURATION * dotCount * 3 / 2);
            alpha.setRepeatCount(Animation.INFINITE);

            mDirectionIndicators[0].getChildAt(mDirectionDotIndex).startAnimation(alpha);
            mDirectionIndicators[1].getChildAt(dotCount - mDirectionDotIndex - 1).startAnimation(
                    alpha);
            mDirectionIndicators[2].getChildAt(dotCount - mDirectionDotIndex - 1).startAnimation(
                    alpha);
            mDirectionIndicators[3].getChildAt(mDirectionDotIndex).startAnimation(alpha);
            alpha.startNow();

            mDirectionDotIndex++;
            mHanler.postDelayed(this, ANIM_DURATION / 2);
        }
    };
}