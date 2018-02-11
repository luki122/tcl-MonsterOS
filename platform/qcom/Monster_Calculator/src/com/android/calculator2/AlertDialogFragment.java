/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calculator2;

import mst.app.dialog.AlertDialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Method;

/**
 * Display a message with a dismiss putton, and optionally a second button.
 */
public class AlertDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public interface OnClickListener {
        /**
         * This method will be invoked when a button in the dialog is clicked.
         *
         * @param fragment the AlertDialogFragment that received the click
         * @param which    the button that was clicked (e.g.
         *                 {@link DialogInterface#BUTTON_POSITIVE}) or the position
         *                 of the item clicked
         */
        public void onClick(AlertDialogFragment fragment, int which);
    }

    private static final String NAME = AlertDialogFragment.class.getName();
    private static final String KEY_MESSAGE = NAME + "_message";
    private static final String KEY_BUTTON_NEGATIVE = NAME + "_button_negative";
    private static final String KEY_BUTTON_POSITIVE = NAME + "_button_positive";

    /**
     * Create and show a DialogFragment with the given message.
     *
     * @param activity            originating Activity
     * @param message             displayed message
     * @param positiveButtonLabel label for second button, if any.  If non-null, activity must
     *                            implement AlertDialogFragment.OnClickListener to respond.
     */
    public static void showMessageDialog(Activity activity, CharSequence message,
                                         @Nullable CharSequence positiveButtonLabel) {
        final AlertDialogFragment dialogFragment = new AlertDialogFragment();
        final Bundle args = new Bundle();
        args.putCharSequence(KEY_MESSAGE, message);
        args.putCharSequence(KEY_BUTTON_NEGATIVE, activity.getString(R.string.dismiss));
        if (positiveButtonLabel != null) {
            args.putCharSequence(KEY_BUTTON_POSITIVE, positiveButtonLabel);
        }
        dialogFragment.setArguments(args);
        if (activity != null) {
//            dialogFragment.show(activity.getFragmentManager(), null /* tag */);
//            dialogFragment.showAllowingStateLoss(activity.getFragmentManager(), null /* tag */);//chg zouxu 20161108 编译不了用下面的反射

            try {
                Class myClass = Class.forName(dialogFragment.getClass().getName());
                Method method = myClass.getMethod("showAllowingStateLoss", FragmentManager.class,String.class);
                method.setAccessible(true);
                method.invoke(dialogFragment, activity.getFragmentManager(),null);
            } catch (Exception e) {
                Log.i("zouxu","showMessageDialog Exception="+e.toString());
            }
        }
    }

    public AlertDialogFragment() {
        setStyle(STYLE_NO_TITLE, android.R.attr.alertDialogTheme);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments() == null ? Bundle.EMPTY : getArguments();
        final Context context = getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final TextView textView = (TextView) inflater.inflate(R.layout.dialog_message,
                null /* root */);
        textView.setText(args.getCharSequence(KEY_MESSAGE));
        final MyAlertDialog.Builder builder = new MyAlertDialog.Builder(context);
        builder.setView(textView)
//        builder.setMessage(args.getCharSequence(KEY_MESSAGE))//chg zouxu 20160908
                .setNegativeButton(args.getCharSequence(KEY_BUTTON_NEGATIVE), null /* listener */);
        final CharSequence positiveButtonLabel = args.getCharSequence(KEY_BUTTON_POSITIVE);
        if (positiveButtonLabel != null) {
            builder.setPositiveButton(positiveButtonLabel, this);
        }
        Dialog m_dialog = builder.create();
////        
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
            textView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN & 0xffffff0f);
//            int data = textView.getSystemUiVisibility();
//            textView.setSystemUiVisibility(data&0xffffff0f);//虚拟按键图标设为白色
        }

        return m_dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        final Activity activity = getActivity();
        if (activity instanceof AlertDialogFragment.OnClickListener /* always true */) {
            ((AlertDialogFragment.OnClickListener) activity).onClick(this, which);
        }
    }
}
