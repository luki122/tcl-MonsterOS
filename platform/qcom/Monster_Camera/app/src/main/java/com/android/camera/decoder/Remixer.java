package com.android.camera.decoder;

import java.util.List;

public abstract class Remixer {


    public static interface RemixProgressListener {
        public void onRemixDone();
    }

    protected RemixProgressListener mListener;

    public void setRemxingProgressListener(RemixProgressListener listener){
        mListener=listener;
    }

    public abstract void prepareForRemixer(String outputPath,List<String> paths);

    public abstract void setDisplayOrientation(int orientation);

    public abstract void startRemix();
    
    public abstract void releaseRemixer();
}
