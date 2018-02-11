package com.android.camera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.camera.debug.Log;
import com.android.camera.ui.ModuleLayoutWrapper;
import com.android.camera.widget.PanoramaPreview;
import com.android.ex.camera2.portability.CameraAgent;
/* MODIFIED-BEGIN by guodong.zhang, 2016-11-03,BUG-3298712*/
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraSettings;
/* MODIFIED-END by guodong.zhang,BUG-3298712*/
import com.android.ex.camera2.portability.Size;
import com.tct.camera.R;

public class TS_PanoramaGPUI extends PhotoUI {
    private static final Log.Tag TAG = new Log.Tag("TS_PANO_UI");

    private final TS_PanoramaController mController;
    private final View mRootView;

    private PanoramaPreview mPanoramaPreview;

    public TS_PanoramaGPUI(CameraActivity activity, TS_PanoramaController controller, View parent) {
        super(activity, controller, parent);
        mController = controller;
        mRootView = parent;
        initPanoramaPreview();
    }

    protected void initPanoramaPreview() {
        ModuleLayoutWrapper moduleRoot =
                (ModuleLayoutWrapper) mRootView.findViewById(R.id.module_layout);
        mActivity.getLayoutInflater().inflate(R.layout.panorama_preview, moduleRoot, true);
        mPanoramaPreview = (PanoramaPreview) mRootView.findViewById(R.id.panorama_preview_holder);

        checkPanoramaPreviewType();

        if (mController.isRoundCornerPreview()) {
            mPanoramaPreview.setBackground(
                    mActivity.getDrawable(R.drawable.panorama_preview_holder_bg));
        } else {
            mPanoramaPreview.setBackgroundColor(
                    mActivity.getColor(R.color.panorama_preview_background));
        }

        mPanoramaPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mController.isSelfie()) {
                    return;
                }
                boolean isLTR = mController.isDirectionLTR();
                isLTR = !isLTR;
                mController.onDirectionInverted(isLTR);
            }
        });
    }

    private void checkPanoramaPreviewType() {
        if (mController.isSelfie()) {
            mPanoramaPreview.setType(PanoramaPreview.TYPE_PANO_SELFIE);
        } else if (mController.is360Photo()) {
            mPanoramaPreview.setType(PanoramaPreview.TYPE_PANO_360_PHOTO);
        } else {
            mPanoramaPreview.setType(PanoramaPreview.TYPE_PANO_NORMAL);
        }
    }

    private int mMode = TS_PanoramaController.PANO_MODE_CLEAR;

    public void transitionToMode(int mode) {
        if (mPanoramaPreview == null) {
            return;
        }

        // Check the type when view mode changed.
        checkPanoramaPreviewType();

        Log.d(TAG, "transition from " + getCurrentMode() + " to " + mode);
        switch (mode) {
            case TS_PanoramaController.PANO_MODE_CLEAR:
                mPanoramaPreview.dismiss();
                break;

            case TS_PanoramaController.PANO_MODE_PREVIEW_LTR:
                mPanoramaPreview.showCameraPreviewLTR();
                break;

            case TS_PanoramaController.PANO_MODE_PREVIEW_RTL:
                mPanoramaPreview.showCameraPreviewRTL();
                break;

            case TS_PanoramaController.PANO_MODE_CAPTURE:
                mPanoramaPreview.showPanoramaCapture();
                break;

            case TS_PanoramaController.PANO_MODE_SELFIE_PREVIEW:
                mPanoramaPreview.showCameraPreviewSelfie();
                break;

            case TS_PanoramaController.PANO_MODE_SELFIE_CAPTURE:
                mPanoramaPreview.showPanoramaSelfieCapture();
                break;
        }
        mMode = mode;
    }

    public int getCurrentMode() {
        return mMode;
    }

    public int getLivePanoramaWidth() {
        if (mPanoramaPreview == null) {
            return 0;
        }
        return mPanoramaPreview.getLivePanoramaWidth();
    }

    public int getLivePanoramaHeight() {
        if (mPanoramaPreview == null) {
            return 0;
        }
        return mPanoramaPreview.getLivePanoramaHeight();
    }

    public int getCameraPreviewWidth() {
        if (mPanoramaPreview == null) {
            return 0;
        }
        if (mController.isSelfie()) {
            return mPanoramaPreview.getSelfiePreviewWidth();
        } else if (mController.isDirectionLTR()) {
            return mPanoramaPreview.getLeftCameraPreviewWidth();
        } else {
            return mPanoramaPreview.getRightCameraPreviewWidth();
        }
    }

    public int getCameraPreviewHeight() {
        if (mPanoramaPreview == null) {
            return 0;
        }
        if (mController.isSelfie()) {
            return mPanoramaPreview.getSelfiePreviewHeight();
        } else if (mController.isDirectionLTR()) {
            return mPanoramaPreview.getLeftCameraPreviewHeight();
        } else {
            return mPanoramaPreview.getRightCameraPreviewHeight();
        }
    }

    public void updatePreviewFrame(Bitmap bitmap, boolean isSelfie, boolean isLTR) {
        if (mPanoramaPreview == null) {
            return;
        }

        if (mController.isRoundCornerPreview()) {
            bitmap = toRoundCorner(bitmap,
                    mActivity.getResources().getDimension(R.dimen.panorama_selfie_preview_radius),
                    isSelfie, isLTR);
        }
        if (isSelfie) {
            mPanoramaPreview.updateMiddleCameraPreview(bitmap);
        } else if (isLTR) {
            mPanoramaPreview.updateLeftCameraPreview(bitmap);
        } else {
            mPanoramaPreview.updateRightCameraPreview(bitmap);
        }
    }

    public void cleanPreviewFrame(boolean isSelfie, boolean isLTR) {
        if (mPanoramaPreview == null) {
            return;
        }

        if (isSelfie) {
            mPanoramaPreview.updateMiddleCameraPreview(null);
        } else if (isLTR) {
            mPanoramaPreview.updateLeftCameraPreview(null);
        } else {
            mPanoramaPreview.updateRightCameraPreview(null);
        }
    }

    public void updateLivePanorama(Bitmap bitmap) {
        if (mPanoramaPreview != null) {
            mPanoramaPreview.updateLivePanorama(bitmap);
        }
    }

    public void cleanLivePanorama() {
        if (mPanoramaPreview != null) {
            mPanoramaPreview.updateLivePanorama(null);
        }
    }

    private Bitmap toRoundCorner(Bitmap bitmap, float pixels,
                                 boolean isSelfie, boolean isLTR) {
        if (bitmap == null) {
            return null;
        }
        if (isSelfie) {
            // Seems no need to do the round corner when preview is in center.
            return bitmap;
        }
        Bitmap output = Bitmap.createBitmap(
                bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();

        final Rect rect;
        int margin = (int) pixels;
        if (isLTR) {
            rect= new Rect(0, 0, bitmap.getWidth() + margin, bitmap.getHeight());
        } else {
            rect = new Rect(-margin, 0, bitmap.getWidth(), bitmap.getHeight());
        }

        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public void startSaveProgress(){
        mActivity.getCameraAppUI().changeShutterButtonSavePanorama(true);
        mActivity.getCameraAppUI().startProgressAnimation();
    }

    public void stopSaveProgress(){
        mActivity.getCameraAppUI().changeShutterButtonSavePanorama(false);
        mActivity.getCameraAppUI().stopProgressAnimation();
    }

    public void setSaveProgress(int progress){
        mActivity.getCameraAppUI().setProgressOfShutterProgress(progress);
    }

    public void initSaveProgress(int max, int step){
        mActivity.getCameraAppUI().mapShutterProgress(max, step);
    }

    private Rect[] mStillRects = new Rect[20];
    private int mFaceNum = 0;

    public Rect[] getStillRects(){
        return mStillRects;
    }

    public int getFaceNum(){
        return mFaceNum;
    }

    private Size getStillImageSize() {
        return mController.getStillImageSize();
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, CameraAgent.CameraProxy camera) {
        super.onFaceDetection(faces, camera);
        if (mStillRects[0] == null) {
            for (int i = 0; i < mStillRects.length; i++) {
                mStillRects[i] = new Rect();
            }
        }
        if (faces.length > 0) {
            Size mStillSize = getStillImageSize();
            int width = mStillSize.width();
            int height = mStillSize.height();
            // Engineへ渡す用
            // (-1000, -1000) => 静止画座標系に変換
            for (int i = 0; i< faces.length; i++){
                mStillRects[i].left = (int) ((1000 + faces[i].rect.left) * width / 2000);
                mStillRects[i].right = (int) ((1000 + faces[i].rect.right) * width / 2000);
                mStillRects[i].top = (int) ((1000 + faces[i].rect.top) * height / 2000);
                mStillRects[i].bottom = (int) ((1000 + faces[i].rect.bottom) * height / 2000);
            }
            mFaceNum = faces.length;
        }
    }


    /* MODIFIED-BEGIN by guodong.zhang, 2016-11-03,BUG-3298712*/
    @Override
    public void initializeZoom(CameraCapabilities capabilities, CameraSettings settings) {}
    /* MODIFIED-END by guodong.zhang,BUG-3298712*/
}
