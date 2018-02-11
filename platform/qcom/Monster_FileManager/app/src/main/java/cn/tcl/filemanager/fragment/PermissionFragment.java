/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.filemanager.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.activity.FileBrowserActivity;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.PermissionUtil;

public class PermissionFragment extends Fragment {

    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-27,BUG-1991729*/
    public static final String MANAGE_PERMISSIONS = "android.intent.action.tct.MANAGE_PERMISSIONS";
    public static final String PACKAGE_NAME = "android.intent.extra.tct.PACKAGE_NAME";
    /* MODIFIED-END by haifeng.tang,BUG-1991729*/

    private Context mContext;
    private Resources mResources;
    private TextView mWelcomeTitle;
    private TextView mWelcomeMsg; // MODIFIED by zibin.wang, 2016-06-25,BUG-2386888
    // private TextView mWelcomeContent;
    private TextView mDenyTitle;
    private TextView mDenyContent;
    private TextView mDenyLine;
    private ImageView mWelcomeImage;
    private ImageView mDenyImage;
    /* MODIFIED-BEGIN by songlin.qi, 2016-06-02,BUG-2241982*/
    private View mLayoutSetting;
    private TextView mBtnSettingExit;
    private TextView mBtnSetting;
    private View mBtnSettingContainer;
    /* MODIFIED-END by songlin.qi,BUG-2241982*/
    private Typeface tf;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = this.getActivity().getApplicationContext();
        mResources = mContext.getResources();
        return getContentView(inflater, container, savedInstanceState);
    }

    protected View getContentView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_permission, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogUtils.getAppInfo(getActivity());
        tf = CommonUtils.getRobotoMedium();
        mWelcomeTitle = (TextView) view.findViewById(R.id.permission_welcome_title);
        mWelcomeTitle.setTypeface(tf);
        mWelcomeMsg = (TextView) view.findViewById(R.id.permission_welcome_msg); // MODIFIED by zibin.wang, 2016-06-25,BUG-2386888
        mWelcomeImage = (ImageView) view.findViewById(R.id.permission_welcome_img);
        // mWelcomeContent = (TextView)
        // view.findViewById(R.id.permission_welcome_full);
        mDenyTitle = (TextView) view.findViewById(R.id.permission_deny_title);
        mDenyContent = (TextView) view.findViewById(R.id.permission_deny_content);
        mDenyImage = (ImageView) view.findViewById(R.id.permission_deny_img);
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-02,BUG-2241982*/
        mLayoutSetting = view.findViewById(R.id.permission_setting);
        mBtnSettingExit = (TextView) view.findViewById(R.id.permission_setting_exit);
        mBtnSetting = (TextView) view.findViewById(R.id.permission_setting_btn);
        mBtnSettingContainer = view.findViewById(R.id.permission_setting_btn_container);
        /* MODIFIED-END by songlin.qi,BUG-2241982*/
        mDenyLine = (TextView) view.findViewById(R.id.permission_deny_line);
        mBtnSetting.setText(getResources().getString(R.string.permission_settings).toUpperCase());
        mBtnSettingExit.setText(getResources().getString(R.string.permission_exit_btn)
                .toUpperCase());
        mBtnSetting.setTextColor(getResources().getColor(R.color.positive_text_color)); // MODIFIED by zibin.wang, 2016-06-25,BUG-2386888
        mBtnSettingExit.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        mBtnSettingContainer.setOnClickListener(new OnClickListener() { // MODIFIED by songlin.qi, 2016-06-02,BUG-2241982
            public void onClick(View view) {

                /* MODIFIED-BEGIN by haifeng.tang, 2016-04-27,BUG-1991729*/
                FileBrowserActivity fileBrowserActivity = (FileBrowserActivity) getActivity();
                fileBrowserActivity.requestPermission();
//                boolean isEnterPermission = false;
//                Intent intent;
//                try {
//                    // Goto setting application permission
//                    intent = new Intent(MANAGE_PERMISSIONS);
//                    intent.putExtra(PACKAGE_NAME, mContext.getPackageName());
//                    startActivityForResult(intent, PermissionUtil.JUMPTOSETTINGFORSTORAGE);
//                } catch (Exception e) {
//                    isEnterPermission = true;
//                }
//                if (isEnterPermission) {
//                    // Goto settings details
//                    Uri packageURI = Uri.parse("package:" + mContext.getPackageName());
//                    intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
//                    startActivityForResult(intent, PermissionUtil.JUMPTOSETTINGFORSTORAGE);
//                }
/* MODIFIED-END by haifeng.tang,BUG-1991729*/
            }
        });
        if (PermissionUtil.isSecondRequestPermission(mContext)) {
            updateView(1);
        } else {
            mWelcomeTitle.setVisibility(View.VISIBLE);
            mWelcomeMsg.setVisibility(View.VISIBLE); // MODIFIED by zibin.wang, 2016-06-25,BUG-2386888
            mWelcomeImage.setVisibility(View.VISIBLE);
            // mWelcomeContent.setVisibility(View.VISIBLE);
            mDenyTitle.setVisibility(View.GONE);
            mDenyContent.setVisibility(View.GONE);
            mDenyImage.setVisibility(View.GONE);
            mLayoutSetting.setVisibility(View.GONE);
            mDenyLine.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void updateView(int type) {

        switch (type) {
            case 1:
                mWelcomeTitle.setVisibility(View.GONE);
                mWelcomeMsg.setVisibility(View.GONE); // MODIFIED by zibin.wang, 2016-06-25,BUG-2386888
                mWelcomeImage.setVisibility(View.GONE);
                // mWelcomeContent.setVisibility(View.GONE);
                mDenyTitle.setVisibility(View.VISIBLE);
                mDenyContent.setVisibility(View.VISIBLE);
                mDenyImage.setVisibility(View.VISIBLE);
                mLayoutSetting.setVisibility(View.VISIBLE);
                mDenyLine.setVisibility(View.VISIBLE);
                break;

        }

    }

}
