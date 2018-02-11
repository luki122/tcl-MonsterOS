package com.android.gallery3d.ui;

public interface ZoomAnimationListener {
    /**
     * @param type zoom in or zoom out 
     */
    public void onZoomStart(int animType);
    public void onZoomProgress(int animType, float progress);
    public void onZoomEnd(int animType);
}
