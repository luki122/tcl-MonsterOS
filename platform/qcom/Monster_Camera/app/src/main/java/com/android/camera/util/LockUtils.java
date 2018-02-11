package com.android.camera.util;

import android.util.SparseArray;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sichao.hu on 9/1/15.
 */
public class LockUtils {

    public interface Lock{
        /**
         * To get a lock in the lock poor
         * @return token to get unlocked in response or null if there's no availabe lock
         */
        public Integer aquireLock();

        /**
         * To get a lock in the lock poor , the token may be generated accroding to the hashId of the parameter
         * @param hash the parameter to generate token
         * @return the hash code parsed in here
         */
        public Integer aquireLock(int hash);

        /**
         * unlock one single lock with specific token , until all lock is unlocked, this MultiLock is opened
         * @param token , should be aquired by {@link #aquireLock()}
         * @return true if unlock succeed or false if failed
         */
        public boolean unlockWithToken(Integer token);
        public boolean isLocked();
    }

    private class MultiLock implements Lock{
        private SparseArray<Integer> mTokenMap =new SparseArray<>();
        private Random mRandomGenerator=new Random(10);
        private int RANDOM_BOUND =Integer.MAX_VALUE;
        private int UNLOCK_DELAY=0;//Add 500 ms delay to make sure the view  is converged from locked state and prevent it from frequent changing

        private Object LOCK_SYNCH=new Object();

        @Override
        public Integer aquireLock() {
            int token = mRandomGenerator.nextInt(RANDOM_BOUND);
            synchronized (LOCK_SYNCH) {
                while (mTokenMap.get(token, null) != null) {
                    token++;
                    if (token == Integer.MAX_VALUE) {
                        token = mRandomGenerator.nextInt(RANDOM_BOUND);
                    }
                }
                mIsLocked=true;
                mTokenMap.put(token, token);
            }
            return token;
        }

        //collision my cause unlock wrong lock
        @Override
        public Integer aquireLock(int hash) {
            synchronized (LOCK_SYNCH) {
                while (true) {
                    Integer hashInMap = mTokenMap.get(hash);
                    if (hashInMap != null) {
                        break;
                    }
                    mIsLocked = true;
                    mTokenMap.put(hash, hash);
                    break;
                }
            }
            return hash;
        }

        private boolean mIsLocked=false;
        @Override
        public boolean unlockWithToken(Integer token) {
            synchronized (LOCK_SYNCH) {
                if (mTokenMap.get(token, null) != null) {
                    mTokenMap.remove(token);
                    if(mTokenMap.size()==0){
                       mIsLocked=false;
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isLocked() {
            return mIsLocked;
        }
    }


    private LockUtils(){}
    private static class Holder{
        private static LockUtils INSTANCE=new LockUtils();
    }

    public static LockUtils getInstance(){return Holder.INSTANCE;}


    /**
     * Lock types
     */
    public enum LockType{
        /**
         * returns an instance of {@link MultiLock}
         */
        MULTILOCK,
    }

    /**
     * A simple factory to generate a lock
     * @param type lock type
     *             @see MultiLock
     * @return a {@link com.android.camera.util.LockUtils.Lock} implement response to the {@link com.android.camera.util.LockUtils.LockType}
     */
    public Lock generateMultiLock(LockType type){
        switch (type){
            case MULTILOCK:
                return new MultiLock();
        }
        return null;
    }
}
