package com.android.external.plantform;

import android.hardware.Camera;

/**
 * Created by bin.zhang on 12/4/15.
 */
public interface IExtCamera {
    public void create(Camera camera);
    public void setGestureCallback(IExtGestureCallback cb);
    public void startGestureDetection();
    public void stopGestureDetection();
    public void destroy();

    public void startRama(int num);
    public void stopRama(int isMerge);
    public void setRamaCallback(IExtPanoramaCallback cb);
    public void setRamaMoveCallback(IExtPanoramaMoveCallback cb);
}
