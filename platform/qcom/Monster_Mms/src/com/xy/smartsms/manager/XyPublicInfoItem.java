/******************************************************************************* 
 * Copyright (C) 2012-2015 Microfountain Technology, Inc. All Rights Reserved. 
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.   
 * Proprietary and confidential
 * 
 * Last Modified: 2015-9-25 19:17:14
 ******************************************************************************/
package com.xy.smartsms.manager;

import org.json.JSONObject;

import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.ui.publicinfo.PublicInfoManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkCallBack;

import com.xy.smartsms.iface.IXYConversationListItemHolder;

public class XyPublicInfoItem {

    IXYConversationListItemHolder mIXYConversationListItemHolder;
    TextView mFromView;
    ImageView mImageView;
    public static Handler mHandler = new Handler() {

    };

    public void bindTextImageView(
            final IXYConversationListItemHolder iXYConversationListItemHolder,
            TextView fromView, ImageView imageView) {

        this.mIXYConversationListItemHolder = iXYConversationListItemHolder;
        String phoneNumber = mIXYConversationListItemHolder.getPhoneNumber();
        final String phoneNum = phoneNumber;
        if (StringUtils.isPhoneNumber(phoneNumber))
            return;
        phoneNumber = StringUtils.getPhoneNumberNo86(phoneNumber);
        this.mFromView = fromView;
        this.mImageView = imageView;
        this.mFromView.setTag(phoneNumber);
        if (mImageView != null) {
            this.mImageView.setTag(phoneNumber);
        }

        JSONObject json = PublicInfoManager
                .getPublicInfoByPhoneIncache(phoneNumber);
        if (json != null) {
            String name = json.optString("name");
            if (!StringUtils.isNull(name)) {
                this.mFromView.setText(name);
            }
            final String logoName = json.optString("logoc");
            if (TextUtils.isEmpty(logoName)) {
                return;
            }
            BitmapDrawable bitmap = PublicInfoManager.getLogoDrawable(logoName);
            if (bitmap != null) {
                setImage(mImageView, bitmap);
            } else if (!mIXYConversationListItemHolder.isScrolling()) {

                PublicInfoManager.publicInfoPool.execute(new Runnable() {
                    @Override
                    public void run() {
                    	final BitmapDrawable bd = PublicInfoManager
                                .findLogoByLogoName(logoName, null);
                        if (bd == null) {
                            return;
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!phoneNum
                                        .equals(mIXYConversationListItemHolder
                                                .getPhoneNumber())) {
                                    return;
                                }
                                setImage(mImageView, bd);
                            }
                        });
                    }
                });
                
                
            }
        } else if (!mIXYConversationListItemHolder.isScrolling()) {
            SdkCallBack callBack = new SdkCallBack() {
                @Override
                public void execute(final Object... obj) {
                    try {
                        if (obj != null && obj.length > 3) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!phoneNum
                                            .equals(mIXYConversationListItemHolder
                                                    .getPhoneNumber())) {
                                        return;
                                    }
                                    final String name = (String) obj[1];
                                    final BitmapDrawable bd = (BitmapDrawable) obj[3];
                                    if (!StringUtils.isNull(name)) {
                                        mFromView.setText(name);
                                    }
                                    setImage(mImageView, bd);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            PublicInfoManager.loadPublicInfo(Constant.getContext(),
                    phoneNumber, callBack);
        }
    }

    public static void setImage(ImageView mImageView, BitmapDrawable bd) {
        try {
            if (mImageView == null || bd == null)
                return;
            mImageView.setImageDrawable(bd);
            mImageView.requestLayout();
            mImageView.invalidate();
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

}
