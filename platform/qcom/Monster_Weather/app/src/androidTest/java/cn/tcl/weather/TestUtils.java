/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather;

/**
 * Created by thundersoft on 16-7-28.
 */
public class TestUtils {


    public static class Lock {

        public void lockWait() {
            lockWait(1000);
        }

        public void lockWait(int timeMills) {
            synchronized (this) {
                try {
                    wait(timeMills);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        public void lockNotify() {
            synchronized (this) {
                notify();
            }
        }

    }
}
