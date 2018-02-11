/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */
package com.mediatek.contacts.model;

import android.content.ContentValues;
import android.content.Context;
//import android.net.sip.SipManager;
import mst.provider.ContactsContract.CommonDataKinds.Event;
import mst.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.telephony.TelephonyManager;

import com.android.contacts.common.R;
import com.android.contacts.common.model.account.BaseAccountType;
import com.android.contacts.common.model.account.AccountType.EditField;
import com.android.contacts.common.model.account.BaseAccountType.PostalActionInflater;
import com.android.contacts.common.model.account.BaseAccountType.SimpleInflater;
import com.android.contacts.common.model.dataitem.DataKind;
import com.android.contacts.common.util.CommonDateUtils;
import com.google.common.collect.Lists;
import com.mediatek.contacts.ContactsSystemProperties;
import com.mediatek.contacts.util.AccountTypeUtils;
import com.mediatek.contacts.util.Log;

public class LocalPhoneAccountType extends BaseAccountType {
    private static final String TAG = "LocalPhoneAccountType";

    public static final String ACCOUNT_TYPE = AccountTypeUtils.ACCOUNT_TYPE_LOCAL_PHONE;

    public LocalPhoneAccountType(Context context, String resPackageName) {
        Log.i(TAG, "[LocalPhoneAccountType]resPackageName:" + resPackageName);
        this.accountType = ACCOUNT_TYPE;
        this.resourcePackageName = null;
        this.syncAdapterPackageName = resPackageName;
        this.titleRes = R.string.account_phone_only;
        this.iconRes = R.drawable.mtk_contact_account_phone;
        TelephonyManager telephonyManager = new TelephonyManager(context);

        try {
            addDataKindStructuredName(context); // overwrite
            addDataKindDisplayName(context); // overwrite
            addDataKindIm(context); // overwrite

            addDataKindPhoneticName(context);
            addDataKindNickname(context);
            addDataKindPhone(context);
            addDataKindEmail(context);
            addDataKindStructuredPostal(context);
            addDataKindOrganization(context);
            addDataKindPhoto(context);
            addDataKindNote(context);
            // mengxue.liu 2016-07-13 modified begin
            //addDataKindWebsite(context);
            addDataKindEvent(context);
            // mengxue.liu 2016-07-13 modified end
            addDataKindGroupMembership(context);
            // / M: VOLTE IMS Call feature.
            if (ContactsSystemProperties.MTK_VOLTE_SUPPORT
                    && ContactsSystemProperties.MTK_IMS_SUPPORT) {
                addDataKindImsCall(context);
            }
//            boolean isVoiceCapable = telephonyManager.isVoiceCapable();
//            boolean isVoipSupported = SipManager.isVoipSupported(context);
//            Log.i(TAG, "[LocalPhoneAccountType]isVoiceCapable = " + isVoiceCapable
//                    + ",isVoipSupported = " + isVoipSupported);
//            if (isVoiceCapable && isVoipSupported) {
//                addDataKindSipAddress(context);
//            }
        } catch (DefinitionException e) {
            Log.e(TAG, "[LocalPhoneAccountType]DefinitionException:", e);
        }
    }

    @Override
    public boolean isGroupMembershipEditable() {
        return true;
    }

    @Override
    public boolean areContactsWritable() {
        return true;
    }

    // mengxue.liu 2016-07-13 modified begin
    private DataKind addDataKindEvent(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind(Event.CONTENT_ITEM_TYPE,
                    R.string.birthdayLabelsGroup, Weight.EVENT, true));
        kind.actionHeader = new EventActionInflater();
        kind.actionBody = new SimpleInflater(Event.START_DATE);
        kind.typeOverallMax = 1;
        kind.typeColumn = Event.TYPE;
        kind.typeList = Lists.newArrayList();
        kind.dateFormatWithoutYear = CommonDateUtils.NO_YEAR_DATE_FORMAT;
        kind.dateFormatWithYear = CommonDateUtils.FULL_DATE_FORMAT;
        kind.typeList.add(buildEventType(Event.TYPE_BIRTHDAY, true).setSpecificMax(1));

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Event.DATA, R.string.birthdayLabelsGroup, FLAGS_EVENT));

        return kind;
    }
    // mengxue.liu 2016-07-13 modified end
    @Override
    protected DataKind addDataKindStructuredPostal(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind(StructuredPostal.CONTENT_ITEM_TYPE,
                R.string.postalLabelsGroup, Weight.STRUCTURED_POSTAL, true));
        kind.actionHeader = new PostalActionInflater();
        kind.actionBody = new SimpleInflater(StructuredPostal.FORMATTED_ADDRESS);
        kind.typeColumn = StructuredPostal.TYPE;
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(buildPostalType(StructuredPostal.TYPE_HOME));
        kind.typeList.add(buildPostalType(StructuredPostal.TYPE_WORK));
        kind.typeList.add(buildPostalType(StructuredPostal.TYPE_OTHER));

        kind.fieldList = Lists.newArrayList();

        kind.fieldList.add(new EditField(StructuredPostal.STREET, R.string.addressLabelsGroup,
        		FLAGS_POSTAL));
        kind.maxLinesForDisplay = MAX_LINES_FOR_POSTAL_ADDRESS;

        return kind;
    }
}
