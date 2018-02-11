package com.gapp.common.thread;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-18.
 * $desc
 */
public class ThreadQueue implements IThreadQueue {

    private Queue<Runnable> mRunnables = new LinkedList<>();

    @Override
    public void runOnThread(Runnable runnable) {
        mRunnables.add(runnable);
    }


    /**
     * call this method int the thread which you want to runing on
     */
    public void doRunnablesOnThread() {
        while (!mRunnables.isEmpty()) {
            mRunnables.poll().run();
        }
    }
}
