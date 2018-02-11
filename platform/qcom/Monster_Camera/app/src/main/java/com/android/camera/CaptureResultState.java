package com.android.camera;

/**
 * Created by sichao.hu on 9/18/15.
 */
public class CaptureResultState {

    public interface ProgressListener{
        public void onProgressArchieved(Thumbnail thumb);
    }

    private boolean isThumbReady=false;
    private boolean isAnimationReady=false;

    private ProgressListener mListener;
    public CaptureResultState(ProgressListener listener){
        mListener=listener;
    }


    private Thumbnail mThumb;
    public void setThumbnail(Thumbnail thumb){
        mThumb=thumb;

    }


    public enum CaptureProgress {
        ANIMATION_DONE,
        THUMB_DONE,
    }


    public void setProgress(CaptureProgress progress){
        switch (progress){
            case ANIMATION_DONE:
                isAnimationReady=true;
                break;
            case THUMB_DONE:
                if(mThumb!=null) {
                    isThumbReady = true;
                }
                break;
        }
        if(isReady()){
            isThumbReady=false;
            isAnimationReady=false;
            mListener.onProgressArchieved(mThumb);
        }
    }

    public boolean isReady(){
        return isThumbReady&&isAnimationReady;
    }
}
