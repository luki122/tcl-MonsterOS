package com.crunchfish.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;

import com.crunchfish.core.GestureDetectionCallback.GestureType;
import com.crunchfish.touchless_a3d.TouchlessA3D;
import com.crunchfish.touchless_a3d.TouchlessA3D.Rotate;
import com.crunchfish.touchless_a3d.exception.LicenseNotValidException;
import com.crunchfish.touchless_a3d.exception.LicenseServerUnavailableException;
import com.crunchfish.touchless_a3d.gesture.Event;
import com.crunchfish.touchless_a3d.gesture.Gesture;
import com.crunchfish.touchless_a3d.gesture.Identifiable;
import com.crunchfish.touchless_a3d.gesture.Pose;

public class GestureCore {
    
    static {
        System.loadLibrary("touchless_a3d"); // c-lib name
        System.loadLibrary("touchless_a3d_jni"); // jni layer for c-lib
    }
    private static String TAG = "GestureCore";
    private TouchlessA3D mTouchLessA3D;
    private static final String GESTURE_OPEN_HAND="open_hand.json";
    private static final String GESTURE_HAND_WINK="hand_wink.json";
    private static final String GESTURE_DRAG_N_DROP="drag_n_drop.json";
    private static final String GESTURE_WAVE="wave.json";
    private Gesture mOpenHandGesture;
    private Gesture mHandWinkGesture;
    private Gesture mDragAndDropGesture;
    private Gesture mWaveGesture;
    private final Gesture.Listener mOpenHandGestureListener=new OpenHandGestureListener();
    private final Gesture.Listener mHandWinkGestureListener=new HandwinkGestureListener();
    private final Gesture.Listener mDragAndDropGestuerListener=new DragAndDropGestureListener();
    private final Gesture.Listener mWaveGestureListener=new WaveGestureListener();
    private GestureDetectionCallback mCallback;
    public GestureCore(Context context){
        intializeGestures(context);
    }
    
    private void intializeGestures(Context context){
        String openHandJSON = null;
        String handWinkJSON=null;
        String dragAndDropJSON=null;
        String waveJSON=null;
        
        openHandJSON=readGestureJSON(context,GESTURE_OPEN_HAND);
        handWinkJSON=readGestureJSON(context,GESTURE_HAND_WINK);
        dragAndDropJSON=readGestureJSON(context,GESTURE_DRAG_N_DROP);
        waveJSON=readGestureJSON(context,GESTURE_WAVE);
        
        Log.w(TAG, "open hand json is "+openHandJSON);
        mOpenHandGesture = new Gesture(openHandJSON);
        mHandWinkGesture=new Gesture(handWinkJSON);
        mDragAndDropGesture=new Gesture(dragAndDropJSON);
        mWaveGesture=new Gesture(waveJSON);
        
        
        mOpenHandGesture.registerListener(mOpenHandGestureListener);
        mHandWinkGesture.registerListener(mHandWinkGestureListener);
        mDragAndDropGesture.registerListener(mDragAndDropGestuerListener);
        mWaveGesture.registerListener(mWaveGestureListener);
    }
    
    private static final String POSE_FOUND="pose_found";
    private static final String POSE_LOST="pose_lost";
    private abstract class AbsGestureListener implements Gesture.Listener{
        public abstract GestureType getType();
        @Override
        public final void onEvent(Event event) {
            Boolean foundPose=null;
            if(event.getText().equals(POSE_FOUND)){
                Log.w(TAG, "gesture found "+this.getType().toString());
                foundPose=true;
            }else if(event.getText().equals(POSE_LOST)){
                Log.w(TAG, "gesture not found");
                foundPose=false;
            }
            if(mCallback!=null){
                mCallback.onGestureDetected(foundPose,this.getType(),getCenterPoints(event));
            }
        }
    }
    
    private Rect getCenterPoints(Event event){
        Collection<Identifiable> idList=event.getIdentifiables();
        for(Identifiable obj:idList){
            if(obj.getType()==Identifiable.Type.POSE){
                Pose pos=(Pose)obj;
                return pos.getBoundingArea();
            }
        }
        return null;
    }
    
    private class OpenHandGestureListener extends AbsGestureListener{

        @Override
        public GestureType getType() {
            return GestureType.OPEN_HAND;
        }
    }
    
    private class HandwinkGestureListener extends AbsGestureListener{

        @Override
        public GestureType getType() {
            return GestureType.HAND_WINK;
        }
    }
    
    private class DragAndDropGestureListener extends AbsGestureListener{

        @Override
        public GestureType getType() {
            return GestureType.DRAG_N_DROP;
        }
    }
    
    private class WaveGestureListener extends AbsGestureListener{

        @Override
        public GestureType getType() {
            return GestureType.WAVE;
        }
    }
    
    
    /**
     * initialize Gesture detector , use application context to avoid activity leak
     * @param context
     * @param width
     * @param height
     */
    public void initializeGestureCore(int width,int height){
        try {
            mTouchLessA3D=new TouchlessA3D(width,height);
        } catch (LicenseNotValidException e) {
            Log.e(TAG, "LicenseNotAvailable");
            return;
        } catch (LicenseServerUnavailableException e) {
            Log.e(TAG, "LicenseServerUnavailable");
            return;
        }
        Log.w(TAG, "engine initialized success");
        mTouchLessA3D.setParameter(TouchlessA3D.Parameters.EXTENDED_RANGE, 1);
        mTouchLessA3D.registerGesture(mDragAndDropGesture);
    }
    
    public void setGestureListener(GestureDetectionCallback callback){
        mCallback=callback;
    }
    
    public void handleImage(long time,byte[] data,Rotate rotate){
        if(mTouchLessA3D==null){
            Log.w(TAG, "engine not initialized, abort");
            return;
        }
        mTouchLessA3D.handleImage(time, data, rotate);
    }
    
    private static String readGestureJSON(Context context,String jsonName){
        InputStream is;
        String json=null;
        try {
            Log.w(TAG, "try read json "+jsonName);
            is = context.getAssets().open(GESTURE_OPEN_HAND);
            Log.w(TAG, "read json success");
            int length = is.available();
            byte[] data = new byte[length];
            is.read(data);
            json = new String(data);
            is.close();
        } catch (IOException e) {
            return null;
        }
        return json;
    }
    
    public void releaseEngine(){
        if(mTouchLessA3D!=null){
            mTouchLessA3D.close();
            Log.w(TAG, "release engine");
        }
        mTouchLessA3D=null;
    }
}
