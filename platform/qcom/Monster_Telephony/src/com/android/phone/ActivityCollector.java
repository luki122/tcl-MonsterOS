/******************************************************************************/
/*                                                               Date:01/2016 */
/*                             PRESENTATION                                   */
/*                                                                            */
/*      Copyright 2012 TCL Communication Technology Holdings Limited.         */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/* Author:  Liu Huan                                                          */
/* E-MAIL:  huan_liu@tcl.com                                                  */
/* Role  :  CONTACTS                                                          */
/* Reference documents :                                                      */
/* -------------------------------------------------------------------------- */
/* Comments:                                                                  */
/* File    : packages/services/Telephony/src/com/android/phone                */
/* Labels  :                                                                  */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/* Modifications on Features list / Changes Request / Problems Report         */
/* -------------------------------------------------------------------------- */
/* date    | author         | key                | comment (what, where, why) */
/* --------|----------------|--------------------|--------------------------- */
/* 16/01/29| huan_liu       | defect:1245549     | [Call setting]SIM 2 call forwarding  */
/*         |                |                    | icon not disappear when pullout SIM 2*/
/*---------|----------------|--------------------|--------------------------- */
/******************************************************************************/

package com.android.phone;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<Activity>();
    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAllActivity() {
        List<Activity> tt = activities; 
        for (Activity activity : tt) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }
}
