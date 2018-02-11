package com.android.camera.ui;

/**
 * Created by sichao.hu on 11/23/15.
 */
public interface Lockable {
    public void lockSelf();//Lock with self->hash
    public void unLockSelf();//Unlock with self->hash
    public boolean isLocked();
    public int lock();
    public boolean unlockWithToken(int token);
}
