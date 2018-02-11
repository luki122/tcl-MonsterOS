/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.ex.camera2.portability;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;

import com.android.ex.camera2.portability.debug.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class DispatchThread extends Thread {
    private static final Log.Tag TAG = new Log.Tag("DispatchThread");
    private static final long MAX_MESSAGE_QUEUE_LENGTH = 1024;

    private final Queue<DispatchRunnable> mJobQueue;
    private Boolean mIsEnded;
    private Handler mCameraHandler;
    private HandlerThread mCameraHandlerThread;
    private final Map<Integer,DispatchRunnable> mJobInstanceHash;//<instanceAction,JobIndex in Queue>

    public DispatchThread(Handler cameraHandler, HandlerThread cameraHandlerThread) {
        super("Camera Job Dispatch Thread");
        mJobQueue = new LinkedList<DispatchRunnable>();
        mJobInstanceHash=new HashMap<>();
        mIsEnded = new Boolean(false);
        mCameraHandler = cameraHandler;
        mCameraHandlerThread = cameraHandlerThread;
    }

    /**
     * Queues up the job.
     *
     * @param job The job to run.
     */
    public void runJob(final Runnable job) {
        if (isEnded()) {
            throw new IllegalStateException(
                    "Trying to run job on interrupted dispatcher thread");
        }
        synchronized (mJobQueue) {
            if (mJobQueue.size() == MAX_MESSAGE_QUEUE_LENGTH) {
                throw new RuntimeException("Camera master thread job queue full");
            }
            Log.w(TAG, " add to job queue");

            final DispatchRunnable runnable=new DispatchRunnable(null) {
                @Override
                public void run() {
                    job.run();
                }
            };
            mJobQueue.add(runnable);
            mJobQueue.notifyAll();
        }
    }

    public void runJobInstance(final Runnable job,final int instanceAction){
        if(isEnded()){
            throw new IllegalStateException(
                    "Trying to run job on interrupted dispatcher thread");
        }
        synchronized (mJobQueue){
            int size=mJobQueue.size();
            if (size== MAX_MESSAGE_QUEUE_LENGTH) {
                throw new RuntimeException("Camera master thread job queue full");
            }
            Log.w(TAG," add to job queue");

            final DispatchRunnable runnable=new DispatchRunnable(instanceAction) {
                @Override
                public void run() {
                    job.run();
                }
            };
            DispatchRunnable instanceRunnable=null;
            synchronized (mJobInstanceHash){
                instanceRunnable=mJobInstanceHash.get(instanceAction);
                if(instanceRunnable!=null){
                    mJobQueue.remove(instanceRunnable);
                }
                mJobInstanceHash.put(instanceAction,runnable);
            }
            mJobQueue.add(runnable);
            mJobQueue.notifyAll();
        }

    }

    /**
     * Queues up the job and wait for it to be done.
     *
     * @param job The job to run.
     * @param timeoutMs Timeout limit in milliseconds.
     * @param jobMsg The message to log when the job runs timeout.
     * @return Whether the job finishes before timeout.
     */
    public void runJobSync(final Runnable job, Object waitLock, long timeoutMs, String jobMsg) {
        String timeoutMsg = "Timeout waiting " + timeoutMs + "ms for " + jobMsg;
        synchronized (waitLock) {
            long timeoutBound = SystemClock.uptimeMillis() + timeoutMs;
            try {
                runJob(job);
                waitLock.wait(timeoutMs);
                if (SystemClock.uptimeMillis() > timeoutBound) {
                    throw new IllegalStateException(timeoutMsg);
                }
            } catch (InterruptedException ex) {
                if (SystemClock.uptimeMillis() > timeoutBound) {
                    throw new IllegalStateException(timeoutMsg);
                }
            }
        }
    }

    /**
     * Gracefully ends this thread. Will stop after all jobs are processed.
     */
    public void end() {
        synchronized (mIsEnded) {
            mIsEnded = true;
        }
        synchronized(mJobQueue) {
            mJobQueue.notifyAll();
        }
    }

    private boolean isEnded() {
        synchronized (mIsEnded) {
            return mIsEnded;
        }
    }

    @Override
    public void run() {
        Log.v(TAG,"thread start running");
        while(true) {
            DispatchRunnable job = null;
            synchronized (mJobQueue) {
                while (mJobQueue.size() == 0 && !isEnded()) {
                    try {
                        Log.w(TAG," wait for next job");
                        mJobQueue.wait();
                    } catch (InterruptedException ex) {
                        Log.w(TAG, "Dispatcher thread wait() interrupted, exiting");
                        break;
                    }
                }
                Log.w(TAG," poll job");
                job = mJobQueue.poll();
            }

            if (job == null) {
                // mJobQueue.poll() returning null means wait() is
                // interrupted and the queue is empty.
                Log.w(TAG," null job polled");
                if (isEnded()) {
                    break;
                }
                continue;
            }

            Integer jobAction=job.getIntanceAction();
            if(jobAction!=null){
                synchronized (mJobInstanceHash){
                    mJobInstanceHash.remove(jobAction);
                }
            }

            job.run();
            Log.w(TAG, " job run");

            synchronized (DispatchThread.this) {
                mCameraHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (DispatchThread.this) {
                            DispatchThread.this.notifyAll();
                            Log.w(TAG, " camera handle notifyAll");
                        }
                    }
                });
                try {
                    Log.w(TAG, " camera handle waitDone");
                    DispatchThread.this.wait();
                } catch (InterruptedException ex) {
                    // TODO: do something here.
                }
            }
        }
        mCameraHandlerThread.quitSafely();
    }

}

abstract class DispatchRunnable implements Runnable{
    private Integer mInstanceAction;
    DispatchRunnable(Integer action){
        mInstanceAction=action;
    }

    public Integer getIntanceAction(){
        return mInstanceAction;
    }
}
