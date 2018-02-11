package com.mst.thememanager.utils;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;

import com.mst.thememanager.ui.MainActivity;

public class FragmentUtils {


    /**
     * Start a new instance of the activity, showing only the given fragment.
     * When launched in this mode, the given preference fragment will be instantiated and fill the
     * entire activity.
     *
     * @param context The context.
     * @param fragmentName The name of the fragment to display.
     * @param args Optional arguments to supply to the fragment.
     * @param resultTo Option fragment that should receive the result of the activity launch.
     * @param resultRequestCode If resultTo is non-null, this is the request code in which
     *                          to report the result.
     * @param titleResId resource id for the String to display for the title of this set
     *                   of preferences.
     * @param title String to display for the title of this set of preferences.
     */
    public static void startWithFragment(Context context, String fragmentName, Bundle args,
            Fragment resultTo, int resultRequestCode, int titleResId,
            CharSequence title) {
        startWithFragment(context, fragmentName, args, resultTo, resultRequestCode,
                null /* titleResPackageName */, titleResId, title, false /* not a shortcut */);
    }

    /**
     * Start a new instance of the activity, showing only the given fragment.
     * When launched in this mode, the given preference fragment will be instantiated and fill the
     * entire activity.
     *
     * @param context The context.
     * @param fragmentName The name of the fragment to display.
     * @param args Optional arguments to supply to the fragment.
     * @param resultTo Option fragment that should receive the result of the activity launch.
     * @param resultRequestCode If resultTo is non-null, this is the request code in which
     *                          to report the result.
     * @param titleResPackageName Optional package name for the resource id of the title.
     * @param titleResId resource id for the String to display for the title of this set
     *                   of preferences.
     * @param title String to display for the title of this set of preferences.
     */
    public static void startWithFragment(Context context, String fragmentName, Bundle args,
            Fragment resultTo, int resultRequestCode, String titleResPackageName, int titleResId,
            CharSequence title) {
        startWithFragment(context, fragmentName, args, resultTo, resultRequestCode,
                titleResPackageName, titleResId, title, false /* not a shortcut */);
    }

    public static void startWithFragment(Context context, String fragmentName, Bundle args,
            Fragment resultTo, int resultRequestCode, int titleResId,
            CharSequence title, boolean isShortcut) {
        Intent intent = onBuildStartFragmentIntent(context, fragmentName, args,
                null /* titleResPackageName */, titleResId, title);
        if (resultTo == null) {
            context.startActivity(intent);
        } else {
            resultTo.startActivityForResult(intent, resultRequestCode);
        }
    }

    public static void startWithFragment(Context context, String fragmentName, Bundle args,
            Fragment resultTo, int resultRequestCode, String titleResPackageName, int titleResId,
            CharSequence title, boolean isShortcut) {
        Intent intent = onBuildStartFragmentIntent(context, fragmentName, args, titleResPackageName,
                titleResId, title);
        if (resultTo == null) {
            context.startActivity(intent);
        } else {
            resultTo.startActivityForResult(intent, resultRequestCode);
        }
    }

    public static void startWithFragmentAsUser(Context context, String fragmentName, Bundle args,
            int titleResId, CharSequence title, 
            UserHandle userHandle) {
        Intent intent = onBuildStartFragmentIntent(context, fragmentName, args,
                null /* titleResPackageName */, titleResId, title);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivityAsUser(intent, userHandle);
    }

    public static void startWithFragmentAsUser(Context context, String fragmentName, Bundle args,
            String titleResPackageName, int titleResId, CharSequence title,
            UserHandle userHandle) {
        Intent intent = onBuildStartFragmentIntent(context, fragmentName, args, titleResPackageName,
                titleResId, title);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivityAsUser(intent, userHandle);
    }

    /**
     * Build an Intent to launch a new activity showing the selected fragment.
     * The implementation constructs an Intent that re-launches the current activity with the
     * appropriate arguments to display the fragment.
     *
     *
     * @param context The Context.
     * @param fragmentName The name of the fragment to display.
     * @param args Optional arguments to supply to the fragment.
     * @param titleResPackageName Optional package name for the resource id of the title.
     * @param titleResId Optional title resource id to show for this item.
     * @param title Optional title to show for this item.
     * @return Returns an Intent that can be launched to display the given
     * fragment.
     */
    public static Intent onBuildStartFragmentIntent(Context context, String fragmentName,
            Bundle args, String titleResPackageName, int titleResId, CharSequence title) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClass(context, MainActivity.class);

		intent.putExtra(MainActivity.EXTRA_SHOW_FRAGMENT, fragmentName);
		intent.putExtra(MainActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
		intent.putExtra(
				MainActivity.EXTRA_SHOW_FRAGMENT_TITLE_RES_PACKAGE_NAME,
				titleResPackageName);
		intent.putExtra(MainActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID,
				titleResId);
		Log.d("aa", "titiledfasfs->"+title);
		intent.putExtra(MainActivity.EXTRA_SHOW_FRAGMENT_TITLE, title);
		return intent;
	}
    
	
}
