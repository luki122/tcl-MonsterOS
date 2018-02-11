package com.android.camera.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

/**
 * Created by sichao.hu on 11/2/15.
 */
public class BlurUtil {
    static RenderScript mRS;
    static ScriptIntrinsicBlur mScriptBlur;
    public static void initialize(Context context){
        mRS=RenderScript.create(context);
        mScriptBlur=ScriptIntrinsicBlur.create(mRS, Element.U8_4(mRS));
    }

    public static Bitmap blur(Bitmap input){
        Bitmap output=Bitmap.createBitmap(input.getWidth(),input.getHeight(), Bitmap.Config.ARGB_8888);
        Allocation inputAllocation=Allocation.createFromBitmap(mRS, input, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        Allocation outputAllocation=Allocation.createFromBitmap(mRS,output);
        mScriptBlur.setInput(inputAllocation);
        mScriptBlur.setRadius(25);
        mScriptBlur.forEach(outputAllocation);
        outputAllocation.copyTo(output);
        inputAllocation.destroy();
        outputAllocation.destroy();
        input.recycle();
        return output;
    }


    /**
     * Remove the destroy action here, since the GC will collect and clear the native pending resource, the explicit call of destroy would only do some acceleration , it's redundant here.
     */
    @Deprecated
    public static void destroy(){
//        mScriptBlur.destroy();
//        mRS.destroy();

    }

}
