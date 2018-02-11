/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.gallery3d.filtershow.filters;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.editors.BasicEditor;
import com.android.gallery3d.filtershow.imageshow.MasterImage;
import com.android.gallery3d.filtershow.tools.AutoEditHelper;

public class ImageFilterSharpen extends ImageFilterRS {
    public static final String SERIALIZATION_NAME = "SHARPEN";
    private static final int AUTO_EDIT_SHARPEN_OFFSET = 5;
    private ScriptC_convolve3x3 mScript;

    private FilterBasicRepresentation mParameters;

    public ImageFilterSharpen() {
        mName = "Sharpen";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterRepresentation representation = new FilterBasicRepresentation("Sharpen", 0, 0, 100);
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setShowParameterValue(true);
        representation.setFilterClass(ImageFilterSharpen.class);
        representation.setTextId(R.string.sharpness);
        //representation.setOverlayId(R.drawable.filtershow_button_colors_sharpen);//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-08-06,PR1062534
        representation.setEditorId(BasicEditor.ID);
        representation.setSupportsPartialRendering(true);
        return representation;
    }

    public void useRepresentation(FilterRepresentation representation) {
        FilterBasicRepresentation parameters = (FilterBasicRepresentation) representation;
        mParameters = parameters;
    }

    @Override
    protected void resetAllocations() {
        // nothing to do
    }

    @Override
    public void resetScripts() {
        if (mScript != null) {
            mScript.destroy();
            mScript = null;
        }
    }

    @Override
    protected void createFilter(android.content.res.Resources res, float scaleFactor,
            int quality) {
        if (mScript == null) {
            /* MODIFIED-BEGIN by hao.yin, 2016-05-27,BUG-2204816*/
            try {
                //mScript = new ScriptC_convolve3x3(getRenderScriptContext(), res, R.raw.convolve3x3);
                mScript = new ScriptC_convolve3x3(getRenderScriptContext());
            } catch (Exception e) {
                return;
            }
            /* MODIFIED-END by hao.yin,BUG-2204816*/
        }
    }

    private void computeKernel() {
        float scaleFactor = getEnvironment().getScaleFactor();
        int result = mParameters.getValue();
        if (AutoEditHelper.AUTO_EDIT_ON) {
            result = result + AUTO_EDIT_SHARPEN_OFFSET;
        }
        float p1 = result * scaleFactor;
        float value = p1 / 100.0f;
        float f[] = new float[9];
        float p = value;
        f[0] = -p;
        f[1] = -p;
        f[2] = -p;
        f[3] = -p;
        f[4] = 8 * p + 1;
        f[5] = -p;
        f[6] = -p;
        f[7] = -p;
        f[8] = -p;
        if (mScript != null)// [BUGFIX]-Add by TCTNJ,xiangyu.liu, 2016-03-15,PR1814768
            mScript.set_gCoeffs(f);
    }

    @Override
    protected void bindScriptValues() {
        int w = getInPixelsAllocation().getType().getX();
        int h = getInPixelsAllocation().getType().getY();
        if (mScript != null) {// [BUGFIX]-Add by TCTNJ,xiangyu.liu, 2016-03-15,PR1814768
            mScript.set_gWidth(w);
            mScript.set_gHeight(h);
        }
    }

    @Override
    protected void runFilter() {
        if (mParameters == null) {
            return;
        }
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-17,PR1044901 begin
        MasterImage.getImage().setFilterSharpen(true);
        // [BUGFIX]-Add by TCTNJ,jian.pan1, 2015-07-17,PR1044901 end
        computeKernel();
        if (mScript != null) {// [BUGFIX]-Add by TCTNJ,xiangyu.liu, 2016-03-15,PR1814768
            mScript.set_gIn(getInPixelsAllocation());
            mScript.bind_gPixels(getInPixelsAllocation());
            mScript.forEach_root(getInPixelsAllocation(), getOutPixelsAllocation());
        }
    }

}
