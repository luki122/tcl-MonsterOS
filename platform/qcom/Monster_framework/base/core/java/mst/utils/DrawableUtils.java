package mst.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.os.Build;
import android.util.Log;


/**
 * Caution. Gross hacks ahead.
 */
public class DrawableUtils {

    private static final String LOG_TAG = "DrawableUtils";

    private static Method sSetConstantStateMethod;
    private static boolean sSetConstantStateMethodFetched;

    private static Field sDrawableContainerStateField;
    private static boolean sDrawableContainerStateFieldFetched;

    private DrawableUtils() {}

    public static boolean setContainerConstantState(DrawableContainer drawable,
            Drawable.ConstantState constantState) {
        if (Build.VERSION.SDK_INT >= 9) {
            // We can use getDeclaredMethod() on v9+
            return setContainerConstantStateV9(drawable, constantState);
        } else {
            // Else we'll just have to set the field directly
            return setContainerConstantStateInternal(drawable, constantState);
        }
    }

    private static boolean setContainerConstantStateV9(DrawableContainer drawable,
            Drawable.ConstantState constantState) {
        if (!sSetConstantStateMethodFetched) {
            try {
                sSetConstantStateMethod = DrawableContainer.class.getDeclaredMethod(
                        "setConstantState", DrawableContainer.DrawableContainerState.class);
                sSetConstantStateMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                Log.e(LOG_TAG, "Could not fetch setConstantState(). Oh well.");
            }
            sSetConstantStateMethodFetched = true;
        }
        if (sSetConstantStateMethod != null) {
            try {
                sSetConstantStateMethod.invoke(drawable, constantState);
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Could not invoke setConstantState(). Oh well.");
            }
        }
        return false;
    }

    private static boolean setContainerConstantStateInternal(DrawableContainer drawable,
            Drawable.ConstantState constantState) {
        if (!sDrawableContainerStateFieldFetched) {
            try {
                sDrawableContainerStateField = DrawableContainer.class
                        .getDeclaredField("mDrawableContainerStateField");
                sDrawableContainerStateField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.e(LOG_TAG, "Could not fetch mDrawableContainerStateField. Oh well.");
            }
            sDrawableContainerStateFieldFetched = true;
        }
        if (sDrawableContainerStateField != null) {
            try {
                sDrawableContainerStateField.set(drawable, constantState);
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Could not set mDrawableContainerStateField. Oh well.");
            }
        }
        return false;
    }

}
