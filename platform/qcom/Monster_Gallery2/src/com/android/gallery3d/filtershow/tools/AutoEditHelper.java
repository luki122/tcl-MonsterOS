package com.android.gallery3d.filtershow.tools;

/*MODIFIED-BEGIN by caihong.gu-nb, 2016-04-13,BUG-1930098*/
import android.content.Context;

import com.android.gallery3d.R;
/*MODIFIED-END by caihong.gu-nb,BUG-1930098*/
import com.android.gallery3d.filtershow.filters.FilterRepresentation;
import com.android.gallery3d.filtershow.filters.FilterUserPresetRepresentation;
import com.android.gallery3d.filtershow.filters.ImageFilterContrast;
import com.android.gallery3d.filtershow.filters.ImageFilterSharpen;
import com.android.gallery3d.filtershow.filters.ImageFilterVibrance;
import com.android.gallery3d.filtershow.imageshow.MasterImage;
import com.android.gallery3d.filtershow.pipeline.ImagePreset;
import com.android.gallery3d.util.LogUtil;

public class AutoEditHelper {

    private String TAG = "AutoEditHelper";

    public static boolean AUTO_EDIT_ON = false;

    public void showCurrentRepresentation(boolean isApply,Context mContext) { //MODIFIED by caihong.gu-nb, 2016-04-13,BUG-1930098
        AUTO_EDIT_ON = isApply;
        ImagePreset oldPreset = MasterImage.getImage().getPreset();

        int contrastValue = getCurrentContrastValue(oldPreset,
                ImageFilterContrast.SERIALIZATION_NAME);
        int vibranceValue = getCurrentContrastValue(oldPreset,
                ImageFilterVibrance.SERIALIZATION_NAME);
        int sharpenValue = getCurrentContrastValue(oldPreset, ImageFilterSharpen.SERIALIZATION_NAME);

        StringBuilder builder = new StringBuilder();
        /*MODIFIED-BEGIN by caihong.gu-nb, 2016-04-13,BUG-1930098*/
        builder.append("{\"CONTRAST\":{\"Name\":\"");
        builder.append(mContext.getString(R.string.contrast)+"\"");
        builder.append(",\"Value\":\"" + contrastValue + "\"},");
        builder.append("\"VIBRANCE\":{\"Name\":\"");
        builder.append(mContext.getString(R.string.vibrance)+"\"");
        builder.append(",\"Value\":\"" + vibranceValue + "\"},");
        builder.append("\"SHARPEN\":{\"Name\":\"");
        builder.append(mContext.getString(R.string.sharpness)+"\"");
        builder.append(",\"Value\":\"" + sharpenValue + "\"}}");
        /*MODIFIED-END by caihong.gu-nb,BUG-1930098*/

        //LogUtil.i(TAG, "showCurrentRepresentation json = " + builder.toString());
        oldPreset.readJsonFromString(builder.toString());
        FilterUserPresetRepresentation representation = new FilterUserPresetRepresentation(TAG,
                oldPreset, -1);

        MasterImage.getImage().setPreset(oldPreset, representation, true);
        MasterImage.getImage().setCurrentFilterRepresentation(representation);
    }

    public int getCurrentContrastValue(ImagePreset oldPreset, String name) {
        FilterRepresentation contrastRepresentation = oldPreset
                .getFilterWithSerializationName(name);
        String value = "0";
        if (contrastRepresentation != null) {
            value = contrastRepresentation.getStateRepresentation();
        }
        //LogUtil.i(TAG, "showCurrentRepresentation value == " + value + " name = :" + name);
        if (contrastRepresentation != null) {
            oldPreset.removeFilter(contrastRepresentation);
        }
        return Integer.parseInt(value);
    }

    public void resetAutoEditStatus() {
        AUTO_EDIT_ON = false;
    }
}
