package com.gapp.common.obj;

/**
 * User : user
 * Date : 2016-08-11
 * Time : 16:47
 */
public interface IAnimation {

    /**
     * create animation
     */
    public void create();

    /**
     * resume animation
     */
    public void resume();

    /**
     * pause animation
     */
    public void pause();


    /**
     * destroy animation
     */
    public void destroy();

    /**
     * this method will be called when memory is low
     *
     * @param level
     */
    void onTrimMemory(int level);

}
