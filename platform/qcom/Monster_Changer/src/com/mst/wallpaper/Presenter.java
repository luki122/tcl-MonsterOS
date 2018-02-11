package com.mst.wallpaper;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference.BaseSavedState;
import android.view.AbsSavedState;

/**
 * Base on MVP,this class represents P that handle datas,
 * read/write file,database controll,and communication with
 * V and M.
 * <p>
 * This Presenter has it's LifeCycle,and it depends on Activity's LifeCycle.
 */
public interface Presenter {
	
	/**
	 * When Presenter created,this method will called.
	 * Call this method on Activity's {@link Activity.onCreate(Bundle savedInstance)} method
	 *  on usual
	 * @param onSaveInstance
	 */
	public void onCreate(Bundle onSaveInstance);
	
	/**
	 * Call this method to start data handle,call it in V
	 */
	public void start();
	
	/**
	 * Call this method to stop data handle,call it in V
	 */
	public void stop();
	
	/**
	 * When Presenter was stared to handle data,this 
	 * method will called,call it in P
	 * 
	 */
	public void onStart();
	
	/**
	 * Call this method to stop data handle,call it in P
	 */
	public void onStop();
	
	/**
	 * When Presenter finish all the data controll,call this method
	 * to notify V.
	 */
	public void finish();
	
	/**
	 * When V was destoried,call this method to recycle resources
	 */
	public void onDestory();
	
	
	public void onSaveInstanceState(Bundle instanceState);
	
	public void onRestoreInstanceState(Parcelable state);
	
	
    public static class BaseSavedState extends AbsSavedState {
    	
        public BaseSavedState(Parcel source) {
            super(source);
        }

        public BaseSavedState(Parcelable superState) {
            super(superState);
        }
        
        public static final Parcelable.Creator<BaseSavedState> CREATOR =
                new Parcelable.Creator<BaseSavedState>() {
            public BaseSavedState createFromParcel(Parcel in) {
                return new BaseSavedState(in);
            }

            public BaseSavedState[] newArray(int size) {
                return new BaseSavedState[size];
            }
        };
    }

}
