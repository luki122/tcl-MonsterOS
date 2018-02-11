package com.monster.launcher.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by antino on 16-8-17.
 */
public interface IBlur {
     Bitmap blur(Context context, Bitmap source, float scaleFactor, int radius);
}
