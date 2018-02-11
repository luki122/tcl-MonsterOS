package com.gapp.common.obj;

/**
 * Created by thundersoft on 16-7-28.
 */
public interface IManager {

    /**
     * int manager
     */
    void init();

    /**
     * recycle manager
     */
    void recycle();

    /**
     * this method will be called when memory is low
     *
     * @param level
     */
    void onTrimMemory(int level);
}
