package com.crunchfish.core;

import android.graphics.Rect;

public interface GestureDetectionCallback {
    public static enum GestureType{
        OPEN_HAND,
        HAND_WINK,
        DRAG_N_DROP,
        WAVE,;

        @Override
        public String toString() {
            String string="";
            switch(this){
            case OPEN_HAND:
                string="open_hand";
                break;
            case HAND_WINK:
                string="hand_wink";
                break;
            case DRAG_N_DROP:
                string="drag&drop";
                break;
            case WAVE:
                string="wave";
                break;
            }
            return string;
        }
        
        
    }
    
    /**
     * wrapped Gesture detection callback to be invoked directly in local application
     * @param detected if event of Crunch event callback returns "pose_found" , detected is true , "pose_lost" returns false 
     * @param type the exact gesture triggers the callback
     * @param boundArea the bound area of the hand detected in the view
     */
    public void onGestureDetected(Boolean detected,GestureType type,Rect boundArea);
    
}
