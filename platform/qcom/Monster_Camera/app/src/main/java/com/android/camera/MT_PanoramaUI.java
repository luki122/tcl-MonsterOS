package com.android.camera;

import android.app.Activity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Matrix;

import com.android.camera.ui.FocusOverlay; // MODIFIED by peixin, 2016-05-03,BUG-2011831
import com.android.camera.ui.MT_AnimationController;
import com.android.camera.ui.MT_NaviLineImageView;
import com.android.camera.ui.MT_PanoProgressIndicator;
import com.android.camera.ui.PeekImageView;
import com.android.camera.ui.RotateLayout;
import com.android.camera.ui.Rotatable;
import com.android.camera.util.CameraUtil;
import com.tct.camera.R;

/**
 * Created by bin.zhang2-nb on 12/24/15.
 */
public class MT_PanoramaUI extends PhotoUI {

    private static final String TAG = "PanoramaView";

    protected RelativeLayout mPanoRootView;
    protected RelativeLayout mPanoView;
    protected RotateLayout mCenterIndicator;
    private ViewGroup mCollimatedArrowsDrawable;
    protected MT_PanoProgressIndicator mProgressIndicator;
    protected RotateLayout mScreenProgressLayout;
    private ViewGroup mDirectionSigns[] = new ViewGroup[4]; // up,down,left,right
    private MT_NaviLineImageView mNaviLine;

    /* MODIFIED-BEGIN by peixin, 2016-05-03,BUG-2011831*/
    protected final View mRootView;
    private final FocusOverlay mFocusUI;
    /* MODIFIED-END by peixin,BUG-2011831*/

    private static final int BLOCK_NUM = 9;
    private int mBlockSizes[] = { 17, 15, 13, 12, 11, 12, 13, 15, 17 };

    private Matrix mSensorMatrix[];
    private Matrix mDisplayMatrix = new Matrix();

    private static final int DIRECTION_RIGHT = 0;
    private static final int DIRECTION_LEFT = 1;
    private static final int DIRECTION_UP = 2;
    private static final int DIRECTION_DOWN = 3;
    private static final int DIRECTION_UNKNOWN = 4;

    private static final int[] DIRECTIONS = { DIRECTION_RIGHT, DIRECTION_DOWN, DIRECTION_LEFT,
            DIRECTION_UP };
    private static final int DIRECTIONS_COUNT = DIRECTIONS.length;

    private static final int TARGET_DISTANCE_HORIZONTAL = 120;
    private static final int TARGET_DISTANCE_VERTICAL = 160;
    private static final int PANO_3D_OVERLAP_DISTANCE = 32;
    private static final int PANO_3D_VERTICAL_DISTANCE = 240;
    private static final int NONE_ORIENTATION = -1;

    private int mDistanceHorizontal = 0;
    private int mDistanceVertical = 0;

    public static final int PANORAMA_VIEW = 0;

    MT_AnimationController mAnimationController;

    private boolean mNeedInitialize = true;
    private boolean mS3DMode = false;
    private boolean mIsCapturing = false;
    private int mViewCategory = PANORAMA_VIEW;
    private int mHoldOrientation = NONE_ORIENTATION;
    private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
    private int mDisplayOrientaion;


    private int mHalfArrowHeight = 0;
    private int mHalfArrowLength = 0;

    private int mSplWidth = 0;
    private int mSplHeight = 0;

    private static final boolean ANIMATION = true;

    private int mSensorDirection = DIRECTION_UNKNOWN;

    public MT_PanoramaUI(CameraActivity activity, PhotoController controller, View parent) {
        super(activity, controller, parent);
        /* MODIFIED-BEGIN by peixin, 2016-05-03,BUG-2011831*/
        mRootView = parent;
        mFocusUI = (FocusOverlay) mRootView.findViewById(R.id.focus_overlay);
        /* MODIFIED-END by peixin,BUG-2011831*/
    }

    public void init(Activity activity) {
        Log.i(TAG, "[init]...");
        //setOrientation(activity.getOrientationCompensation());
    }

    @Override
    public void onOrientationChanged(int orientation) {
        Log.i(TAG, "[onOrientationChangedis]...mIsCapturing = " + mIsCapturing);
        super.onOrientationChanged(orientation);
        if (!mIsCapturing) {
            mHoldOrientation = NONE_ORIENTATION;
            // in 3D Mode, the layout lock as 270
            if (mS3DMode) {
                Log.i(TAG, "[onOrientationChanged]orientation = " + orientation);
                return;
            }
            if (mProgressIndicator != null) {
                mProgressIndicator.setOrientation(orientation);
            }
        } else {
            mHoldOrientation = orientation;
        }
    }

    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    public void show(Activity activity, int orientation) {
        // display orientation and rotation will be updated when capture,
        // because camera may slip to gallery and rotate the display,then
        // display orientation and
        // rotation changed
        mDisplayOrientaion = getDisplayRotation(mActivity);
        mOrientation = orientation;

        Log.i(TAG, "[show]mNeedInitialize=" + mNeedInitialize + ", " +
                "mDisplayOrientaion=" + mDisplayOrientaion + ", mOrientation" + mOrientation);

        if (mNeedInitialize) {
            initializeViewManager(activity);
            mNeedInitialize = false;
        }
        showCaptureView();
    }
/*
    private RotateLayout.OnSizeChangedListener mOnSizeChangedListener = new RotateLayout.OnSizeChangedListener() {
        @Override
        public void onSizeChanged(int width, int height) {
            Log.d(TAG, "[onSizeChanged]width=" + width + " height=" + height);
            //mSplWidth =  Math.max(width, height);
            //mSplHeight =  Math.min(width, height);
        }
    };
*/
    private final View.OnLayoutChangeListener mPanoViewLayoutChangeListener
            = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                   int oldTop, int oldRight, int oldBottom) {
            int width = right - left;
            int height = bottom - top;
            mSplWidth =  width;
            mSplHeight =  height;
//            mSplWidth = Math.max(width, height);
//            mSplHeight = Math.min(width, height);
        }
    };

    private void initializeViewManager(Activity activity) {
        mPanoRootView = (RelativeLayout)activity.findViewById(R.id.pano_frame_layout);
        mPanoView = (RelativeLayout)activity.findViewById(R.id.pano_view);

        mScreenProgressLayout = (RotateLayout) activity.findViewById(R.id.on_screen_progress);
        mCenterIndicator = (RotateLayout) activity.findViewById(R.id.center_indicator);
        mDirectionSigns[DIRECTION_RIGHT] = (ViewGroup) activity.findViewById(R.id.pano_right);
        mDirectionSigns[DIRECTION_LEFT] = (ViewGroup) activity.findViewById(R.id.pano_left);
        mDirectionSigns[DIRECTION_UP] = (ViewGroup) activity.findViewById(R.id.pano_up);
        mDirectionSigns[DIRECTION_DOWN] = (ViewGroup) activity.findViewById(R.id.pano_down);
        mAnimationController = new MT_AnimationController(mDirectionSigns,
                (ViewGroup) mCenterIndicator.getChildAt(0));

//        mScreenProgressLayout.setOnSizeChangedListener(mOnSizeChangedListener);
        mPanoView.removeOnLayoutChangeListener(mPanoViewLayoutChangeListener);
        mPanoView.addOnLayoutChangeListener(mPanoViewLayoutChangeListener);

        mDistanceHorizontal = mS3DMode ? PANO_3D_OVERLAP_DISTANCE : TARGET_DISTANCE_HORIZONTAL;
        mDistanceVertical = mS3DMode ? PANO_3D_VERTICAL_DISTANCE : TARGET_DISTANCE_VERTICAL;
        if (mViewCategory == PANORAMA_VIEW) {
            mNaviLine = (MT_NaviLineImageView) activity.findViewById(R.id.navi_line);
            mCollimatedArrowsDrawable = (ViewGroup) activity.findViewById(R.id.static_center_indicator);

            mProgressIndicator = new MT_PanoProgressIndicator(activity, BLOCK_NUM, mBlockSizes);
            mProgressIndicator.setVisibility(View.GONE);

            mScreenProgressLayout.setOrientation(mOrientation, true);
            mProgressIndicator.setOrientation(mOrientation);

            prepareSensorMatrix();
        }

    }


    private void prepareSensorMatrix() {
        mSensorMatrix = new Matrix[4];

        mSensorMatrix[DIRECTION_LEFT] = new Matrix();
        mSensorMatrix[DIRECTION_LEFT].setScale(-1, -1);
        mSensorMatrix[DIRECTION_LEFT].postTranslate(0, mDistanceVertical);

        mSensorMatrix[DIRECTION_RIGHT] = new Matrix();
        mSensorMatrix[DIRECTION_RIGHT].setScale(-1, -1);
        mSensorMatrix[DIRECTION_RIGHT].postTranslate(mDistanceHorizontal * 2, mDistanceVertical);

        mSensorMatrix[DIRECTION_UP] = new Matrix();
        mSensorMatrix[DIRECTION_UP].setScale(-1, -1);
        mSensorMatrix[DIRECTION_UP].postTranslate(mDistanceHorizontal, 0);

        mSensorMatrix[DIRECTION_DOWN] = new Matrix();
        mSensorMatrix[DIRECTION_DOWN].setScale(-1, -1);
        mSensorMatrix[DIRECTION_DOWN].postTranslate(mDistanceHorizontal, mDistanceVertical * 2);
    }

    private void showCaptureView() {
        // reset orientation,since camera state is snapinprogress at last time.
        if (mHoldOrientation != NONE_ORIENTATION) {
            onOrientationChanged(mHoldOrientation);
        }
        if (mS3DMode) {
            for (int i = 0; i < 4; i++) {
                mDirectionSigns[i].setVisibility(View.INVISIBLE);
            }
            mCenterIndicator.setVisibility(View.VISIBLE);

            mAnimationController.startCenterAnimation();
        } else {
            mCenterIndicator.setVisibility(View.GONE);
        }

        mScreenProgressLayout.setOrientation(mOrientation, true);
        mProgressIndicator.setOrientation(mOrientation);

        mPanoView.setVisibility(View.VISIBLE);
        mPanoView.requestLayout();
        mProgressIndicator.setProgress(0);
        mProgressIndicator.setVisibility(View.VISIBLE);
        mPanoRootView.setVisibility(View.VISIBLE);
    }

    public void uninit() {
        Log.i(TAG, "[uninit]...");
        mNeedInitialize = true;
    }

    public void reset() {
        Log.i(TAG, "[reset] mViewCategory = " + mViewCategory
                + ",mPanoView = " + mPanoView);
        mPanoRootView.setVisibility(View.GONE);
        mPanoView.setVisibility(View.GONE);
        mAnimationController.stopCenterAnimation();
        mCenterIndicator.setVisibility(View.GONE);

        if (mViewCategory == PANORAMA_VIEW) {
            mSensorDirection = DIRECTION_UNKNOWN;
            mNaviLine.setVisibility(View.GONE);
            mCollimatedArrowsDrawable.setVisibility(View.GONE);
            for (int i = 0; i < 4; i++) {
                mDirectionSigns[i].setSelected(false);
                mDirectionSigns[i].setVisibility(View.VISIBLE);
            }
        }
    }

    public void hide() {
        mPanoRootView.setVisibility(View.GONE);
    }

    public boolean update(int type, Object... args) {
        Log.i(TAG, "[update] type =" + type);
        switch (type) {
            case MT_PanoramaModule.INFO_UPDATE_PROGRESS:
                int num = Integer.parseInt(args[0].toString());
                setViewsForNext(num);
                break;

            case MT_PanoramaModule.INFO_UPDATE_MOVING:
                if (args[0] != null && args[1] != null && args[2] != null) {
                    int xy = Integer.parseInt(args[0].toString());
                    int direction = Integer.parseInt(args[1].toString());
                    boolean show = Boolean.parseBoolean(args[2].toString());
                    updateMovingUI(xy, direction, show);
                }
                break;

            case MT_PanoramaModule.INFO_START_ANIMATION:
                startCenterAnimation();
                break;

            case MT_PanoramaModule.INFO_IN_CAPTURING:
                mIsCapturing = true;
                break;

            case MT_PanoramaModule.INFO_OUTOF_CAPTURING:
                mIsCapturing = false;
                break;

            default:
                break;
        }

        return true;
    }


    private void setViewsForNext(int imageNum) {
        if (!filterViewCategory(PANORAMA_VIEW)) {
            return;
        }

        Log.d(TAG, "---->setProgress" + (imageNum + 1));
        mProgressIndicator.setProgress(imageNum + 1);

        if (imageNum == 0) {
            if (!mS3DMode) {
                // in 3D Mode, direction animation do not show
                mAnimationController.startDirectionAnimation();
            } else {
                mNaviLine.setVisibility(View.VISIBLE);
            }
        } else {
            mNaviLine.setVisibility(View.INVISIBLE);
            mAnimationController.stopCenterAnimation();
            mCenterIndicator.setVisibility(View.GONE);
            mCollimatedArrowsDrawable.setVisibility(View.VISIBLE);
        }
    }

    private boolean filterViewCategory(int requestCategory) {
        if (mViewCategory != requestCategory) {
            return false;
        }
        return true;
    }

    private void updateMovingUI(int xy, int direction, boolean shown) {
        Log.d(TAG, "[updateMovingUI]xy:" + xy + ",direction:" + direction + ",shown:" + shown);
        if (!filterViewCategory(PANORAMA_VIEW)) {
            return;
        }
        // direction means sensor towards.
        if (direction == DIRECTION_UNKNOWN || shown || mNaviLine.getWidth() == 0
                || mNaviLine.getHeight() == 0) {
            // if the NaviLine has not been drawn well, return.
            mNaviLine.setVisibility(View.INVISIBLE);
            return;
        }

        short x = (short) ((xy & 0xFFFF0000) >> 16);
        short y = (short) (xy & 0x0000FFFF);

        switch (direction) {
            case 0: {
                direction = DIRECTION_DOWN;
                short tmp = x;
                x = (short)(0 - y);
                y = tmp;
            } break;

            case 1: {
                direction = DIRECTION_UP;
                short tmp = x;
                x = (short)(0 - y);
                y = tmp;
            } break;

            case 2: {
                direction = DIRECTION_RIGHT;
                short tmp = x;
                x = (short)(0 - y);
                y = tmp;
            } break;

            case 3: {
                direction = DIRECTION_LEFT;
                short tmp = x;
                x = (short)(0 - y);
                y = tmp;
            } break;
        }

        updateUIShowingMatrix(x, y, direction);
    }


    private void updateUIShowingMatrix(int x, int y, int direction) {
        // Be sure it's called in onFrame.
        float[] pts = { x, y };
        mSensorMatrix[direction].mapPoints(pts);
        Log.d(TAG, "[updateUIShowingMatrix]Matrix x = " + pts[0] + " y = " + pts[1]);

        prepareTransformMatrix(direction);
        mDisplayMatrix.mapPoints(pts);
        Log.d(TAG, "[updateUIShowingMatrix]DisplayMatrix x = " + pts[0] + " y = " + pts[1]);

        int fx = (int) pts[0];
        int fy = (int) pts[1];

        mNaviLine.setLayoutPosition(fx - mHalfArrowHeight, fy - mHalfArrowLength, fx
                + mHalfArrowHeight, fy + mHalfArrowLength);

        updateDirection(direction);
        mNaviLine.setVisibility(View.VISIBLE);
    }

    private void updateDirection(int direction) {
        Log.d(TAG, "[updateDirection]mDisplayOrientaion:" + mDisplayOrientaion
                + ",mSensorDirection =" + mSensorDirection);
        int index = 0;
        for (int i = 0; i < DIRECTIONS_COUNT; i++) {
            if (DIRECTIONS[i] == direction) {
                index = i;
                break;
            }
        }
        switch (mDisplayOrientaion) {
            case 270:
                direction = DIRECTIONS[(index - 1 + DIRECTIONS_COUNT) % DIRECTIONS_COUNT];
                break;

            case 0:
                break;

            case 90:
                direction = DIRECTIONS[(index + 1) % DIRECTIONS_COUNT];
                break;

            case 180:
                direction = DIRECTIONS[(index + 2) % DIRECTIONS_COUNT];
                break;

            default:
                break;
        }

        if (mSensorDirection != direction) {
            mSensorDirection = direction;
            if (mSensorDirection != DIRECTION_UNKNOWN) {
                // mViewChangedListener.onCaptureBegin();
                setOrientationIndicator(direction);
                mCenterIndicator.setVisibility(View.VISIBLE);

                mAnimationController.startCenterAnimation();
                for (int i = 0; i < 4; i++) {
                    mDirectionSigns[i].setVisibility(View.INVISIBLE);
                }
            } else {
                mCenterIndicator.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void setOrientationIndicator(int direction) {
        Log.d(TAG, "[setOrientationIndicator]direction = " + direction);
        if (direction == DIRECTION_RIGHT) {
            ((Rotatable) mCollimatedArrowsDrawable).setOrientation(0, ANIMATION);
            ((Rotatable) mCenterIndicator).setOrientation(0, ANIMATION);
            mNaviLine.setRotation(-90);
        } else if (direction == DIRECTION_LEFT) {
            ((Rotatable) mCollimatedArrowsDrawable).setOrientation(180, ANIMATION);
            ((Rotatable) mCenterIndicator).setOrientation(180, ANIMATION);
            mNaviLine.setRotation(90);
        } else if (direction == DIRECTION_UP) {
            ((Rotatable) mCollimatedArrowsDrawable).setOrientation(90, ANIMATION);
            ((Rotatable) mCenterIndicator).setOrientation(90, ANIMATION);
            mNaviLine.setRotation(180);
        } else if (direction == DIRECTION_DOWN) {
            ((Rotatable) mCollimatedArrowsDrawable).setOrientation(270, ANIMATION);
            ((Rotatable) mCenterIndicator).setOrientation(270, ANIMATION);
            mNaviLine.setRotation(0);
        }
    }


    private void prepareTransformMatrix(int direction) {
        mDisplayMatrix.reset();
        int halfPrewWidth = mSplWidth >> 1;
        int halfPrewHeight = mSplHeight >> 1;

        Log.d(TAG, "[prepareTransformMatrix]mSplWidth=" + mSplWidth
                + ", mSplHeight =" + mSplHeight);

        // Determine the length / height of the arrow.
        getArrowHL();

        // For simplified calculation of view rectangle, clip arrow length
        // for both view width and height.
        // Arrow may look like this "--------------->"
        float halfViewWidth = mS3DMode ? 65 * 4 : ((float) halfPrewWidth - mHalfArrowLength);
        float halfViewHeight = (float) halfPrewHeight - mHalfArrowLength;

        mDisplayMatrix.postScale(halfViewWidth / mDistanceHorizontal, halfViewHeight
                / mDistanceVertical);

        switch (mDisplayOrientaion) {
            case 270:
                mDisplayMatrix.postTranslate(-halfViewWidth * 2, 0);
                mDisplayMatrix.postRotate(-90);
                break;

            case 0:
//                mDisplayMatrix.postTranslate(0, -halfViewHeight * 2);
//                mDisplayMatrix.postRotate(90);
                break;

            case 90:
                mDisplayMatrix.postTranslate(0, -halfViewHeight * 2);
                mDisplayMatrix.postRotate(90);
                break;

            case 180:
                mDisplayMatrix.postTranslate((float) (-halfViewWidth * (mS3DMode ? 2.67 : 2)),
                        -halfViewHeight * 2);
                mDisplayMatrix.postRotate(180);
                break;

            default:
                break;
        }
        mDisplayMatrix.postTranslate(mHalfArrowLength, mHalfArrowLength);
    }

    private void getArrowHL() {
        if (mHalfArrowHeight == 0) {
            int naviWidth = mNaviLine.getWidth();
            int naviHeight = mNaviLine.getHeight();
            if (naviWidth > naviHeight) {
                mHalfArrowLength = naviWidth >> 1;
                mHalfArrowHeight = naviHeight >> 1;
            } else {
                mHalfArrowHeight = naviWidth >> 1;
                mHalfArrowLength = naviHeight >> 1;
            }
        }
    }

    private void startCenterAnimation() {
        mCollimatedArrowsDrawable.setVisibility(View.GONE);
        mAnimationController.startCenterAnimation();
        mCenterIndicator.setVisibility(View.VISIBLE);
    }

    /* MODIFIED-BEGIN by peixin, 2016-05-03,BUG-2011831*/
    /**
     * Shows or hides focus UI.
     *
     * @param show shows focus UI when true, hides it otherwise
     */
    public void showFocusUI(boolean show) {
        if (mFocusUI != null) {
            mFocusUI.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }
    /* MODIFIED-END by peixin,BUG-2011831*/

}
