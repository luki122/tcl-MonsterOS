package com.android.camera;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.android.camera.debug.Log;
import com.android.camera.settings.SettingsManager;
import com.android.camera.ui.HelpTipCling;
 /*MODIFIED-BEGIN by nie.lei, 2016-04-01,BUG-1899903*/
import com.android.camera.util.CameraUtil;
import com.tct.camera.R;

import java.util.Locale;
 /*MODIFIED-END by nie.lei,BUG-1899903*/

/**
 * Created by nielei on 15-11-12.
 */
public abstract class HelpTip {

    public static final Log.Tag TAG = new Log.Tag("HelpTip");

    //draw type
    public static final int NO_DRAW = -1;
    public static final int CIRCLE = 0;
    public static final int RECTANGLE = 1;
    public static final int LINE = 2;

    public static final int SHOW_DELAY_TIME_MSG = 0;
    public static final int HELP_TIP_SHOW_DELAY = 150;//delay to display to avoid OOM when switching tips

    protected final SettingsManager mSettingsManager;
    protected final CameraActivity mActivity;
    protected final LayoutInflater mInflater;
    protected int mLayoutResId;
    protected int mCurTipGroupId = HelpTipsManager.UNKOWN_GROUP_ID;
    protected int mCurTipId = HelpTipsManager.UNKOWN_TIP_ID;
    protected int mDrawType = NO_DRAW;
    protected ViewGroup mRootView;//tip cling root view
    protected Handler mHandler = new MainHandler(Looper.getMainLooper());
    protected HelpTipCling mHelpTipCling;

    protected HelpTipController mHelpTipController;
    protected ImageView mRingAnimationImageView;
    protected boolean mIsShowExist = false;
    protected ViewGroup mTipClingContentView;//tip cling content view
    protected Button mTipNextButton;

    protected boolean mLongClickAnimFucus = false;//burst shot only for snap tip
    public static boolean mVideoReadyFlag = false;//make sure camera is ready when video moulde opens. //MODIFIED by nie.lei, 2016-04-01,BUG-1875810

    /**
     * Create a new overlay.
     *
     * @param tipId      tip id
     * @param controller help tip controller
     * @param activity   cameraActvity
     */
    public HelpTip(int tipId, HelpTipController controller, CameraActivity activity) {
        mActivity = activity;
        mCurTipId = tipId;
        mHelpTipController = controller;
        mInflater = LayoutInflater.from(mActivity);
        mRootView = (ViewGroup) mActivity.findViewById(R.id.helptips_placeholder_wrapper);
        mSettingsManager = mActivity.getSettingsManager();
    }

    /**
     * check if help tip needs to intercept touch events in MainActivityLayout
     * return boolean ,true means intercept
     */
    protected boolean checkToIntercept() {
        if (mCurTipGroupId == HelpTipsManager.MODE_GROUP) {
            return false;
        }
        return true;
    }

    /**
     * notify mode change for mode tip
     */
    protected void notifyModeChanged() {

    }

    /**
     * show tip cling
     */
    public void showHelpTipCling() {
        mHelpTipCling = (HelpTipCling) mInflater.inflate(mLayoutResId, mRootView, false);
        mRootView.addView(mHelpTipCling, -1);
        mRootView.setBackground(null);
        mRootView.setVisibility(View.VISIBLE);

        initWidgets();
        mIsShowExist = true;
    }

    /**
     * response when activity paused
     */
    public void doPause() {
        mCurTipGroupId = HelpTipsManager.UNKOWN_GROUP_ID;
        mCurTipId = HelpTipsManager.UNKOWN_TIP_ID;
        cleanUpHelpTip();
        hideHelpTipCling();
        mIsShowExist = false;
    }

    /**
     * hide tip cling
     */
    public void hideHelpTipCling() {
        mRootView.setBackground(null);
        mRootView.setVisibility(View.GONE);
        mIsShowExist = false;
    }

    /**
     * delay to show tip cling
     */
    public void showDelayHelpTip() {
        mHandler.sendEmptyMessageDelayed(SHOW_DELAY_TIME_MSG, HELP_TIP_SHOW_DELAY);
    }

    /**
     * init widgets for current tip
     */
    protected abstract void initWidgets();

    /**
     * init commom widget
     */
    public void initCommomWidget() {
        mTipClingContentView = (ViewGroup) mRootView.findViewById(R.id.help_tip_cling_content);
        mTipClingContentView.setVisibility(View.VISIBLE);
         /*MODIFIED-BEGIN by nie.lei, 2016-04-01,BUG-1899903*/
        if(mCurTipGroupId == HelpTipsManager.WELCOME_GROUP && mCurTipId == HelpTipsManager.CAMERAKEY_TIP){
            if(CameraUtil.isLayoutDirectionRtl(mActivity)) {
                mTipNextButton = (Button) mTipClingContentView.findViewById(R.id.camerakey_next);
            }else {
                mTipNextButton = (Button) mTipClingContentView.findViewById(R.id.next);
            }
        }else {
            mTipNextButton = (Button) mTipClingContentView.findViewById(R.id.next);
        }

        if(mTipNextButton != null){
            mTipNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissHelpTip();
                }
            });
        }
         /*MODIFIED-END by nie.lei,BUG-1899903*/

    }

    /**
     * get group id of current tip
     */
    public int getCurTipGroupId() {
        return mCurTipGroupId;
    }

    /**
     * get tip id of current tip in group
     */
    public int getCurTipId() {
        return mCurTipId;
    }

    /**
     * response when clicking "dismiss" button
     */
    protected abstract void dismissHelpTip();

    /**
     * finish current tip session and go to next tip
     *
     * @param dismiss true if clicking "dismiss" button
     */
    protected abstract void goToNextTip(boolean dismiss);

    /**
     * update help tip step into sharedPreference
     *
     * @param tipId  ,tip id
     * @param isOver true if current tip's group is over
     */
    protected abstract void updateCurHelpTipStep(int tipId, boolean isOver);

    /**
     * get tip display status
     * return true if tip is show and exists.
     */
    public boolean IsShowExist() {
        return mIsShowExist;
    }

    /**
     * play ring animation
     */
    public void playAnimation() {
        mRingAnimationImageView = (ImageView) mRootView.findViewById(R.id.anim_focus);
        try {
            if (mRingAnimationImageView != null) {
                mRingAnimationImageView.setBackgroundResource(R.drawable.tutorial_shutter_animation);
                AnimationDrawable d = (AnimationDrawable) mRingAnimationImageView.getBackground();
                if (mRingAnimationImageView != null && d != null) {
                    mRingAnimationImageView.setBackground(d);
                    d.start();
                }
                mRingAnimationImageView.setOnClickListener(null);
                mRingAnimationImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCurTipId == HelpTipsManager.SNAP_TIP && mLongClickAnimFucus) {
                            return;
                        }
                        clickAnimFucus();
                    }
                });
                if (mCurTipId == HelpTipsManager.SNAP_TIP) {
                    mRingAnimationImageView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            longClickAnimFucus();
                            return false;
                        }
                    });
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "playAnimation OOM out of memory mCurTipId = " + mCurTipId);
            if (mHelpTipCling != null) {
                closeAndFinishHelptip();
            }
        }
    }

    /**
     * burst shot through long clicking the view of ring animation to
     * it is only used to burst shot for snap tip
     */
    public void longClickAnimFucus() {
    }

    /**
     * clean related resource to close current tip and finish current tip
     * help tip controller will be called to release tip.
     */
    public void closeAndFinishHelptip() {
        mCurTipGroupId = HelpTipsManager.UNKOWN_GROUP_ID;
        mCurTipId = HelpTipsManager.UNKOWN_TIP_ID;

        cleanUpHelpTip();
        hideHelpTipCling();
        mHelpTipController.notifyFinishHelpTip();
    }

    /**
     * response when clicking the view of "ring" animation
     */
    protected abstract void clickAnimFucus();

    /**
     * remove root views on help tip and set semi-transparent background
     */
    private void removeOverlayViews() {
        if (mRootView != null) {
            mRootView.removeAllViews();
            mRootView.setBackground(mActivity.getResources().getDrawable(R.color.tourial_semitransparent));
        }
    }

    /**
     * clear animation and popup window
     */
    protected void cleanUpHelpTip() {
        //remove animation on mRingAnimationImageView
        if (mRingAnimationImageView != null) {
            AnimationDrawable ad = (AnimationDrawable) mRingAnimationImageView.getBackground();
            if (ad != null) {
                ad.stop();
                ad = null;
            }
            mRingAnimationImageView.setBackground(null);
            mRingAnimationImageView.clearAnimation();
            mRingAnimationImageView.setOnClickListener(null);
        }

        if (mHelpTipCling != null) {
            mHelpTipCling.cleanDestroy();
            mHelpTipCling.removeAllViews();
        }

        mHelpTipCling = null;
        mRingAnimationImageView = null;
        removeOverlayViews();
        System.gc();
    }

    /**
     * response when clicking hit rect
     */
    public void clickHitRectResponse(int index) {
    }

    private class MainHandler extends android.os.Handler {
        public MainHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_DELAY_TIME_MSG:
                    if (mIsShowExist) {
                        showHelpTipCling();
                    }
                    break;

                default:
                    break;
            }
        }
    }
}
