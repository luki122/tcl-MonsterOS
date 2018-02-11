/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.camera;

import android.graphics.SurfaceTexture; // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
import android.view.View;

import com.android.camera.ShutterButton.OnShutterButtonListener;


public interface PhotoController extends OnShutterButtonListener {

    public static final int PREVIEW_STOPPED = 0;
    public static final int IDLE = 1;  // preview is active
    // Focus is in progress. The exact focus state is in Focus.java.
    public static final int FOCUSING = 2;
    public static final int SNAPSHOT_IN_PROGRESS = 3;
    // Switching between cameras.
    public static final int SWITCHING_CAMERA = 4;

    public static final int AE_AF_LOCKED =5;//In this state , exposure compensation is manually adjusted ,
    // AE/AF is suggested to be locked to make sure the EVO and focus is converged to expected corresponding area

    public static final int SCANNING_FOR_AE_AF_LOCK =6;// This state would be triggered in case of auto focus success ,
    // in this case ,AE/AF is converged and we are able to manually adjust the exposure compensation

    public static final int SNAPSHOT_IN_PROGRESS_DURING_LOCKED=7;//the preview need to recover to locked state after shutter during AE_AF_LOCKED

    public static final int SNAPSHOT_LONGSHOT_PENDING_START =8;

    public static final int SNAPSHOT_LONGSHOT=9;

    public static final int SNAPSHOT_LONGSHOT_PENDING_STOP=10;

    /* MODIFIED-BEGIN by sichao.hu, 2016-05-18,BUG-2145791*/
    public static final int PREVIEW_PENDING_START=11;//the state between request start preview and onPreviewStarted callback,
    // because the startPreview command is sent asynchronously , it's essential to asynchronously send stopPreview which would not triggered under PREVIEW_STOPPED state
    /* MODIFIED-END by sichao.hu,BUG-2145791*/


    public void onZoomChanged(float requestedZoom);

    public boolean isImageCaptureIntent();

    public boolean isCameraIdle();

    public boolean canCloseCamera();

    public void onCaptureDone();

    public void onCaptureCancelled();

    public void onCaptureRetake();

    public boolean cancelAutoFocus();

    public void stopPreview();

    public int getCameraState();

    public void onSingleTapUp(View view, int x, int y);

    public void onLongPress(int x,int y);

    public void updatePreviewAspectRatio(float aspectRatio);

    public void updateCameraOrientation();

    /**
     * This is the callback when the UI or buffer holder for camera preview,
     * such as {@link android.graphics.SurfaceTexture}, is ready to be used.
     * The controller can start the camera preview after or in this callback.
     */
    public void onPreviewUIReady();

    /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
    public void onSurfaceAvailable(SurfaceTexture surfaceTexture, int width, int height);

    public void onSurfaceTextureChanged(SurfaceTexture surfaceTexture,int w,int h);
    /* MODIFIED-END by sichao.hu,BUG-2743263*/


    /**
     * This is the callback when the UI or buffer holder for camera preview,
     * such as {@link android.graphics.SurfaceTexture}, is being destroyed.
     * The controller should try to stop the preview in this callback.
     */
    public void onPreviewUIDestroyed();

    /********************** Capture animation **********************/

    /**
     * Starts the pre-capture animation.
     */
    public void startPreCaptureAnimation();

    public void onEvoChanged(int value);

    public boolean isFacebeautyEnabled();

    public boolean isAttentionSeekerShow();

    public void updateFaceBeautySetting(String key, int value);

    public boolean isGesturePalmShow();

    public void onFaceDetected(boolean detected); //MODIFIED by sichao.hu, 2016-04-15,BUG-1951866

    void onExposureCompensationChanged(int value);

    void onMeteringStart();

    void onMeteringStop();

    void onMeteringAreaChanged(int x, int y);

    public void onAspectRatioClicked();

    boolean isShowPose();

    boolean isShowCompose();

    boolean isFilterSelectorScreen(); // MODIFIED by xuan.zhou, 2016-11-03,BUG-3311864
}
