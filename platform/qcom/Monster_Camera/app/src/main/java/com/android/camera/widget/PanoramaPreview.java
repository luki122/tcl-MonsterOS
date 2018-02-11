package com.android.camera.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.camera.debug.Log;
import com.tct.camera.R;

/**
 * Created by Sean Scott on 9/18/16.
 */
public class PanoramaPreview extends FrameLayout{

    private static final Log.Tag TAG = new Log.Tag("PanoramaPreview");

    private final Context mContext;

    // The live preview for the panorama capture.
    private ImageView mLivePanorama;

    // The layout shown when the panorama capture direction is from left to right.
    private RelativeLayout mLTRLayout;
    // The small preview at the right of the holder when idle.
    private ImageView mLeftCameraPreview;

    // The panorama direction is from right to left.
    private RelativeLayout mRTLLayout;
    // The small preview at the right of the holder when idle.
    private ImageView mRightCameraPreview;

    private RelativeLayout mSelfieLayout;
    private ImageView mMiddleCameraPreview;

    private boolean mInitialized = false;

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_PANO_NORMAL = 1;
    public static final int TYPE_PANO_SELFIE = 2;
    public static final int TYPE_PANO_360_PHOTO = 3;
    private int mType = TYPE_UNKNOWN;

    public PanoramaPreview(Context context) {
        super(context);
        mContext = context;
    }

    public PanoramaPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public PanoramaPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLivePanorama = (ImageView) findViewById(R.id.panorama_live_preview);

        mLTRLayout = (RelativeLayout) findViewById(R.id.panorama_ltf_layout);
        mLeftCameraPreview = (ImageView) findViewById(R.id.panorama_left_camera_preview);

        mRTLLayout = (RelativeLayout) findViewById(R.id.panorama_rtf_layout);
        mRightCameraPreview = (ImageView) findViewById(R.id.panorama_right_camera_preview);

        mSelfieLayout = (RelativeLayout) findViewById(R.id.panorama_selfie_layout);
        mMiddleCameraPreview = (ImageView) findViewById(R.id.panorama_middle_camera_preview);

        mType = TYPE_UNKNOWN;
        mInitialized = true;
    }

    public int getLivePanoramaHeight() {
        return mLivePanorama == null ? 0 : mLivePanorama.getHeight();
    }

    public int getLivePanoramaWidth() {
        return mLivePanorama == null ? 0 :mLivePanorama.getWidth();
    }

    public int getLeftCameraPreviewHeight() {
        return mLeftCameraPreview == null ? 0 :mLeftCameraPreview.getHeight();
    }

    public int getLeftCameraPreviewWidth() {
        return mLeftCameraPreview == null ? 0 :mLeftCameraPreview.getWidth();
    }

    public int getRightCameraPreviewHeight() {
        return mRightCameraPreview == null ? 0 :mRightCameraPreview.getHeight();
    }

    public int getRightCameraPreviewWidth() {
        return mRightCameraPreview == null ? 0 :mRightCameraPreview.getWidth();
    }

    public int getSelfiePreviewHeight() {
        return mMiddleCameraPreview == null ? 0 :mMiddleCameraPreview.getHeight();
    }

    public int getSelfiePreviewWidth() {
        return mMiddleCameraPreview == null ? 0 :mMiddleCameraPreview.getWidth();
    }

    private void setContainerVisibility(boolean isHolderVisible, boolean isPanoramaLive,
                                        boolean isSelfie, boolean isDirectionLTR) {
        if (!mInitialized) {
            return;
        }

        if (!isHolderVisible) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);

        boolean isLivePanoramaVisible = isPanoramaLive;
        boolean isLTRLayoutVisible = !isPanoramaLive && !isSelfie && isDirectionLTR;
        boolean isRTLLayoutVisible = !isPanoramaLive && !isSelfie && !isDirectionLTR;
        boolean isSelfieLayoutVisible = !isPanoramaLive && isSelfie;

        mLivePanorama.setVisibility(isLivePanoramaVisible ? VISIBLE : INVISIBLE);
        mLTRLayout.setVisibility(isLTRLayoutVisible ? VISIBLE : INVISIBLE);
        mRTLLayout.setVisibility(isRTLLayoutVisible ? VISIBLE : INVISIBLE);
        mSelfieLayout.setVisibility(isSelfieLayoutVisible ? VISIBLE : INVISIBLE);
    }

    public void dismiss() {
        setContainerVisibility(false, false, false, false);
    }

    public void showCameraPreviewLTR() {
        setContainerVisibility(true, false, false, true);
    }

    public void showCameraPreviewRTL() {
        setContainerVisibility(true, false, false, false);
    }

    public void showCameraPreviewSelfie() {
        setContainerVisibility(true, false, true, false);
    }

    public void showPanoramaCapture() {
        setContainerVisibility(true, true, false, false);
    }

    public void showPanoramaSelfieCapture() {
        setContainerVisibility(true, true, true, false);
    }

    public void updateLivePanorama(Bitmap bitmap) {
        if (mLivePanorama != null) {
            mLivePanorama.setImageBitmap(bitmap);
        }
    }

    public void updateLeftCameraPreview(Bitmap bitmap) {
        if (mLeftCameraPreview != null) {
            mLeftCameraPreview.setImageBitmap(bitmap);
        }
    }

    public void updateRightCameraPreview(Bitmap bitmap) {
        if (mRightCameraPreview != null) {
            mRightCameraPreview.setImageBitmap(bitmap);
        }
    }

    public void updateMiddleCameraPreview(Bitmap bitmap) {
        if (mMiddleCameraPreview != null) {
            mMiddleCameraPreview.setImageBitmap(bitmap);
        }
    }

    public void setType(int type) {
        if (type < TYPE_UNKNOWN || type > TYPE_PANO_360_PHOTO) {
            Log.e(TAG, "Error type: " + type);
            return;
        }

        if (!mInitialized) {
            Log.e(TAG, "Widget is not initialized yet.");
            return;
        }

        if (mType == type) {
            return;
        }

        FrameLayout.LayoutParams params = (LayoutParams) getLayoutParams();
        Resources res = mContext.getResources();
        if (type == TYPE_PANO_SELFIE) {
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.topMargin = res.getDimensionPixelOffset(
                    R.dimen.panorama_selfie_preview_marginTop);
            params.height = res.getDimensionPixelOffset(R.dimen.panorama_selfie_preview_height);
            params.width = res.getDimensionPixelOffset(R.dimen.panorama_selfie_preview_width);
        } else {
            params.gravity = Gravity.TOP | Gravity.CENTER_VERTICAL;
            params.setMargins(res.getDimensionPixelOffset(R.dimen.panorama_preview_gap),
                    res.getDimensionPixelOffset(R.dimen.panorama_preview_top_margin),
                    res.getDimensionPixelOffset(R.dimen.panorama_preview_gap),
                    0);
            params.width = FrameLayout.LayoutParams.MATCH_PARENT;
            params.height = (type == TYPE_PANO_360_PHOTO) ?
                    res.getDimensionPixelOffset(R.dimen.panorama_360_photo_preview_height) :
                    res.getDimensionPixelOffset(R.dimen.panorama_preview_height);
        }
        setLayoutParams(params);
        mType = type;
    }
}
