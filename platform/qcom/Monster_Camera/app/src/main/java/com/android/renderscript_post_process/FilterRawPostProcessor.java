/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.renderscript_post_process;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsic3DLUT;
import android.renderscript.Type;
import android.util.SparseArray;
import android.view.Surface;

import com.android.camera.debug.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by sichao.hu on 8/11/16.
 */
public class FilterRawPostProcessor {

    private static final int LUT_DIM=33;
    Log.Tag TAG=new Log.Tag("FiltRawProc");
    private int mWidth;
    private int mHeight;
    private Allocation mInputAllocation;
    private Allocation mOutputAllocation;
    private Allocation mMappedAllocation;
    private RenderScript mRS;
    private ScriptIntrinsic3DLUT mIntrinsicLUT;
    private ScriptC_yuvconverter mYUVConvertor;
    private SparseArray<Allocation> mLUTAllocations=new SparseArray<>();
    private Context mContext;

    public FilterRawPostProcessor(Context context, final int width, final int height){
        mContext=context;
        mRS=RenderScript.create(context);
        mWidth=width;
        mHeight=height;
        mYUVConvertor=new ScriptC_yuvconverter(mRS);
        mIntrinsicLUT=ScriptIntrinsic3DLUT.create(mRS, Element.U8_4(mRS));
        initializeInputAllocation();
    }

    private Allocation.OnBufferAvailableListener mBufferAvailableListener=new Allocation.OnBufferAvailableListener() {
        @Override
        public void onBufferAvailable(Allocation allocation) {

            Type.Builder rgbTypeBuilder = new Type.Builder(mRS, Element.RGBA_8888(mRS));
            rgbTypeBuilder.setX(mWidth);
            rgbTypeBuilder.setY(mHeight);
            mOutputAllocation = Allocation.createTyped(mRS, rgbTypeBuilder.create());
            mMappedAllocation = Allocation.createTyped(mRS, rgbTypeBuilder.create());
            allocation.ioReceive();
            Log.w(TAG, "post data available");
            //TODO: parse allocation into script
            mYUVConvertor.set_gYUVInput(mInputAllocation);
            mYUVConvertor.forEach_convertRGB(mOutputAllocation);

            Allocation lutAllocation= getLUTAllocation(mFilterIndex);
            if(lutAllocation==null){
                if(mListener!=null){
                    mListener.onImageAvailable(null);
                }
                return;
            }

            mIntrinsicLUT.setLUT(lutAllocation);
            mIntrinsicLUT.forEach(mOutputAllocation, mMappedAllocation);
            Bitmap bitmap=Bitmap.createBitmap(mWidth,mHeight, Bitmap.Config.ARGB_8888);
            mMappedAllocation.copyTo(bitmap);

            Matrix rotateMatrix=new Matrix();
            rotateMatrix.postRotate(mOrientation,mWidth/2.0f,mHeight/2.0f);
            Bitmap bitmapToRecycle=bitmap;
            bitmap=Bitmap.createBitmap(bitmap,0,0,mWidth,mHeight,rotateMatrix,false);
            bitmapToRecycle.recycle();

            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            byte[] jpegImg=baos.toByteArray();

            synchronized (mFilterProcessLock) {
                Log.w(TAG, "post process finish");
                mListener.onImageAvailable(jpegImg);
                mListener = null;
            }
        }
    };

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

    private void initializeInputAllocation(){
        if(mInputAllocation!=null){
            mInputAllocation.destroy();
        }
        Type.Builder yuvTypeBuilder = new Type.Builder(mRS, Element.YUV(mRS));
        yuvTypeBuilder.setX(mWidth);
        yuvTypeBuilder.setY(mHeight);
        yuvTypeBuilder.setYuvFormat(ImageFormat.YUV_420_888);
        mInputAllocation = Allocation.createTyped(mRS, yuvTypeBuilder.create(),
                Allocation.USAGE_IO_INPUT | Allocation.USAGE_SCRIPT);
        mInputAllocation.setOnBufferAvailableListener(mBufferAvailableListener);
    }

    public Surface getSurface(){
        return mInputAllocation.getSurface();
    }


    private OnImageAvailableListener mListener;
    private int mFilterIndex;
    private int mOrientation;
    private Object mFilterProcessLock =new Object();
    public void requestOneshotFilterProcess(int filterIndex,int orientation,OnImageAvailableListener listener){
        synchronized (mFilterProcessLock) {
            mFilterIndex=filterIndex;
            mOrientation=orientation;
            mListener = listener;
        }

    }
}
