/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.renderscript_post_process;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsic3DLUT;
import android.renderscript.Type;
import android.util.SparseArray;

import com.android.camera.debug.Log;
import com.tct.camera.R;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by sichao.hu on 8/15/16.
 */
public class FilterCompressedPostProcessor {
    private static final String NAME="FiltComProcess";
    private Log.Tag TAG=new Log.Tag(NAME);
    private static final int LUT_DIM=33;
    private RenderScript mRS;
    private ScriptIntrinsic3DLUT mInstrinsicLUT;
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private SparseArray<Allocation> mLUTAllocations=new SparseArray<>();
    public FilterCompressedPostProcessor(Context context){
        mContext=context;
        mHandlerThread=new HandlerThread(NAME);
        mHandlerThread.start();
        mHandler=new Handler(mHandlerThread.getLooper());
        mRS=RenderScript.create(context);
        mInstrinsicLUT =ScriptIntrinsic3DLUT.create(mRS, Element.U8_4(mRS));
        Type.Builder lutBuilder=new Type.Builder(mRS,Element.RGBA_8888(mRS));
        lutBuilder.setX(LUT_DIM);
        lutBuilder.setY(LUT_DIM);
        lutBuilder.setZ(LUT_DIM);
    }

    public void requestProcess(final byte[] jpeg,final int index,final OnImageAvailableListener listener){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Allocation lutAllocation=getLUTAllocation(index);
                if(lutAllocation==null){
                    listener.onImageAvailable(jpeg);
                    return;
                }

                Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
                ByteBuffer nativeBuffer = ByteBuffer.allocate(bitmap.getAllocationByteCount());
                bitmap.copyPixelsToBuffer(nativeBuffer);
                nativeBuffer.position(0);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Log.w(TAG,"bitmap spec is "+width+"x"+height);
                Type.Builder builder = new Type.Builder(mRS, Element.RGBA_8888(mRS));
                builder.setX(width);
                builder.setY(height);
                Allocation inputAllocation = Allocation.createTyped(mRS, builder.create());
                byte[] nativeArray=nativeBuffer.array();
                inputAllocation.copyFrom(nativeArray);

                Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Allocation outputAllocation=Allocation.createFromBitmap(mRS,outBitmap);
                mInstrinsicLUT.setLUT(lutAllocation);
                mInstrinsicLUT.forEach(inputAllocation, outputAllocation);
                outputAllocation.copyTo(outBitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                outBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                byte[] jpegImg = baos.toByteArray();
                Log.w(TAG,"on image processed");
                listener.onImageAvailable(jpegImg);

            }
        });
    }

    private Allocation getLUTAllocation(int filterIndex){
        if(filterIndex<0||filterIndex>=LUTConfiguration.LUT_INDICES.length){
            Log.e(TAG, "Filter index invalid");
            return null;
        }
        LUTConfiguration.LUT_Property lutIndex=LUTConfiguration.LUT_INDICES[filterIndex];
        int lutId=lutIndex.getId();
        Allocation lutAllocation=mLUTAllocations.get(lutId,null);
        if(lutAllocation==null){//Lazy-loading LUT into renderscript allocation
            Type.Builder lutBuilder=new Type.Builder(mRS,Element.RGBA_8888(mRS));
            lutBuilder.setX(LUT_DIM);
            lutBuilder.setY(LUT_DIM);
            lutBuilder.setZ(LUT_DIM);
            lutAllocation=Allocation.createTyped(mRS, lutBuilder.create());
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inScaled=false;
            Bitmap bitmap=BitmapFactory.decodeResource(mContext.getResources(), lutId,options);
            ByteBuffer lutBuffer=ByteBuffer.allocate(bitmap.getAllocationByteCount());
            bitmap.copyPixelsToBuffer(lutBuffer);
            lutBuffer.position(0);
            lutAllocation.copyFrom(lutBuffer.array());
            mLUTAllocations.put(lutId,lutAllocation);
        }
        return lutAllocation;
    }

    public void release(){
        mHandlerThread.quitSafely();
        mHandler.removeCallbacks(null);
        mHandlerThread=null;
        mHandler=null;
    }
}
