/*
 * Copyright (C) 2011-2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is auto-generated. DO NOT MODIFY!
 * The source Renderscript file: /home/vinceshen/Work/GitSpace/CameraCN/app/src/main/rs/yuvconverter.rs
 */

package com.android.renderscript_post_process;

import android.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_yuvconverter extends ScriptC {
    private static final String __rs_resource_name = "yuvconverter";
    // Constructor
    public  ScriptC_yuvconverter(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_yuvconverter(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __ALLOCATION = Element.ALLOCATION(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __ALLOCATION;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private final static int mExportVarIdx_gYUVInput = 0;
    private Allocation mExportVar_gYUVInput;
    public synchronized void set_gYUVInput(Allocation v) {
        setVar(mExportVarIdx_gYUVInput, v);
        mExportVar_gYUVInput = v;
    }

    public Allocation get_gYUVInput() {
        return mExportVar_gYUVInput;
    }

    public Script.FieldID getFieldID_gYUVInput() {
        return createFieldID(mExportVarIdx_gYUVInput, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_convertRGB = 1;
    public Script.KernelID getKernelID_convertRGB() {
        return createKernelID(mExportForEachIdx_convertRGB, 58, null, null);
    }

    public void forEach_convertRGB(Allocation aout) {
        forEach_convertRGB(aout, null);
    }

    public void forEach_convertRGB(Allocation aout, Script.LaunchOptions sc) {
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_convertRGB, (Allocation) null, aout, null, sc);
    }

}

