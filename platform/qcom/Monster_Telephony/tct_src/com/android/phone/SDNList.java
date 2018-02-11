/******************************************************************************/
/*                                                               Date:11/2015 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  huan_liu                                                        */
/*  Email  :  huan_liu@tcl.com                                                */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :                                                                */
/*  File     :                                                                */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 03/11/2015|        huan_liu      |      task-861782     |[SIM]SDN not fou- */
/*           |                      |                      |nd in SIM Tool or */
/*           |                      |                      | other menu       */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.phone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import static com.android.internal.telephony.PhoneConstants.SUBSCRIPTION_KEY;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.PhoneConstants;

/**
 * SDN List UI for the Phone app.
 */
public class SDNList extends ADNList {

    private static final String INTENT_EXTRA_NAME = "name";
    private static final String INTENT_EXTRA_NUMBER = "number";
    private static long mSub=-1;

    @Override
    protected Uri resolveIntent() {
        Intent intent = getIntent();
        //[FEATURE]-Update-BEGIN by TSNJ chunhua.liu, 12/13/2014, FR-870475
        mSub = intent.getLongExtra(PhoneConstants.SUBSCRIPTION_KEY, SubscriptionManager.getDefaultSubscriptionId());

        if (mSub == -1) {
            intent.setData(Uri.parse("content://icc/sdn"));
        } else {
            intent.setData(Uri.parse("content://icc/sdn/subId/" + mSub));
        }
        //[FEATURE]-Update-END by TSNJ chunhua.liu,
        return intent.getData();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO: is this what we really want?
        callSDN(position);

    }

    /**
     * Edit the item at the selected position in the list.
     */
    private void callSDN(int position) {
        if (mCursor.moveToPosition(position)) {
            // String name = mCursor.getString(NAME_COLUMN);
            String number = mCursor.getString(NUMBER_COLUMN);
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                Intent callIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED, Uri.fromParts("tel",
                        number, null));
                startActivity(callIntent);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void showTipToast(String title, String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
