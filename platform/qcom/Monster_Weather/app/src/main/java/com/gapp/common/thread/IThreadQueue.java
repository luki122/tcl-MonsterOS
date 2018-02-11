package com.gapp.common.thread;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-18.
 * $desc
 */
public interface IThreadQueue {

    /**
     * run a runnable in thread
     * @param runnable
     */
    void runOnThread(Runnable runnable);

}
