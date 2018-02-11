package com.android.camera.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.renderscript.Allocation;
import android.renderscript.Allocation.MipmapControl;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

public class NV21Convertor {
    private Context mContext;
    private ScriptIntrinsicYuvToRGB mScript;
    private RenderScript mRS;
    public NV21Convertor(Context context){
        mContext=context;
        mRS=RenderScript.create(mContext);
        mScript=ScriptIntrinsicYuvToRGB.create(mRS, Element.U8_4(mRS));
    }

    public Bitmap convertNV21ToBitmap(byte[] nv21Data,int width,int height){
        Type.Builder inputBuilder=new Type.Builder(mRS,Element.U8(mRS));
        inputBuilder.setX(nv21Data.length);


        Allocation inputAllocation=Allocation.createTyped(mRS, inputBuilder.create(), MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        inputAllocation.copyFrom(nv21Data);
        mScript.setInput(inputAllocation);

        Type.Builder outputBuilder=new Type.Builder(mRS,Element.RGBA_8888(mRS));
        outputBuilder.setX(width);
        outputBuilder.setY(height);

        Allocation outputAllocation=Allocation.createTyped(mRS, outputBuilder.create(),MipmapControl.MIPMAP_NONE,Allocation.USAGE_SCRIPT);
        mScript.forEach(outputAllocation);
        Bitmap bmp=Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        outputAllocation.copyTo(bmp);

        inputAllocation.destroy();
        outputAllocation.destroy();
        return bmp;
    }

    /**
     * The explict call of destroy would only do some acceleration , redundant here . GC would do the job
     */
    @Deprecated
    public void release(){
//        mScript.destroy();
//        mRS.destroy();
    }

}
