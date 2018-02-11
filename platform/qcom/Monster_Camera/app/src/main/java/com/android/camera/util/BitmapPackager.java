package com.android.camera.util;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;

public class BitmapPackager extends WeakReference<Bitmap>{

    private Bitmap mBitmap;
    public BitmapPackager(Bitmap r) {
        super(r);
        mBitmap=r;
    }

    @Override
    public void clear() {
        super.clear();
        if(mBitmap.isRecycled()){
            mBitmap=null;
            return;
        }
        mBitmap.recycle();
        mBitmap=null;
    }

    @Override
    public boolean enqueue() {
        return super.enqueue();
    }

    @Override
    public Bitmap get() {
        return mBitmap;
    }

    @Override
    public boolean isEnqueued() {
        return super.isEnqueued();
    }
    

}
