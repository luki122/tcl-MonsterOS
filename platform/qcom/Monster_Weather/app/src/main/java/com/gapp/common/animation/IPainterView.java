package com.gapp.common.animation;

import com.gapp.common.obj.IManager;
import com.gapp.common.thread.IThreadQueue;
import com.gapp.common.utils.BitmapManager;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-15.
 * the painter for {@link ISprite}
 */
public interface IPainterView extends IManager, IThreadQueue {


//    void setZOrderMediaOverlay(boolean isMediaOverlay);

    /**
     * set the ZOrderOnTop
     *
     * @param isOnTop
     */
    void setZOrderOnTop(boolean isOnTop);

    /**
     * get the bmp loadder
     *
     * @return
     */
    BitmapManager getBitmapManager();

    /**
     * get height
     *
     * @return
     */
    int getHeight();

    /**
     * get width
     *
     * @return
     */
    int getWidth();

//    /**
//     * set bmp loadder
//     *
//     * @param bm
//     */
//    void setBitmapManager(BitmapManager bm);


    /**
     * add a sprite to draw
     *
     * @param sprite
     */
    void addSprite(ISprite sprite);

    /**
     * remove a sprite to draw
     *
     * @param sprite
     */
    void removeSprite(ISprite sprite);

    /**
     *
     */
    void clearSprites();

    /**
     * start running
     */
    void start();

    /**
     * stop running
     */
    void stop();


    /**
     * add a connecter to Painter
     *
     * @param connecter
     */
    void addServantConnnecter(IServantConnecter connecter);

    /**
     * remove a connecter to Painter
     *
     * @param connecter
     */
    void removeServantConnecter(IServantConnecter connecter);


    void clearServantConnecters();


    void getLocationOnScreen(int[] position);


    /**
     * set the {@link OnRunningListener}
     *
     * @param l
     */
    void setOnRunningListener(OnRunningListener l);


    /**
     * when the drawing thread is running this will be called
     */
    interface OnRunningListener {
        /**
         * before running
         */
        void onBeforeRunning();

        /**
         * after running
         */
        void onAfterRunning();
    }
}
