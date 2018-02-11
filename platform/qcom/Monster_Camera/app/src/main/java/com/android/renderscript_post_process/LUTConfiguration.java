/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.renderscript_post_process;

import com.tct.camera.R;
import com.android.gl_component.PlainShader;

/**
 * Created by sichao.hu on 8/15/16.
 */
public class LUTConfiguration {
    public static class LUT_Property{
        private int mId;
        private int mNeedVignetting;
        public LUT_Property(int id,int needVignetting){
            mId=id;
            mNeedVignetting=needVignetting;
        }
        public int getId(){
            return mId;
        }
        public int isNeedVignetting(){
            return mNeedVignetting;
        }
    }
    /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
    public static final LUT_Property[] LUT_INDICES=new LUT_Property[]{
            /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
            new LUT_Property(R.drawable.armaro_lut,PlainShader.FALSE),
            new LUT_Property(R.drawable.juno_lut,PlainShader.FALSE),
            new LUT_Property(R.drawable.lofi_lut,PlainShader.FALSE),
            new LUT_Property(R.drawable.gingham_lut, PlainShader.FALSE),
            new LUT_Property(R.drawable.clarendon_lut,PlainShader.FALSE),
            new LUT_Property(R.drawable.inkwell_lut,PlainShader.FALSE),
            new LUT_Property(R.drawable.reyes_lut,PlainShader.FALSE),
//            new LUT_Property(R.drawable.infrared_amelie_lightpink_lut,PlainShader.FALSE),
/* MODIFIED-END by sichao.hu,BUG-2821981*/
            new LUT_Property(R.drawable.hefe_lut,PlainShader.FALSE),

            new LUT_Property(R.drawable.no_filter_lut,PlainShader.FALSE),
//            new LUT_Property(R.drawable.infrared_amelie_lightpink_lut,PlainShader.FALSE),
//            new LUT_Property(R.drawable.infrared_amelie_pink_lut_02,PlainShader.FALSE),
//            new LUT_Property(R.drawable.infrared_yellow_lut_01,PlainShader.FALSE),

    };
}
