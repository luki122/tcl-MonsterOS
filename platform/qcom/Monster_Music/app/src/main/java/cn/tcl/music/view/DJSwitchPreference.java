package cn.tcl.music.view;

import android.annotation.SuppressLint;
import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import cn.tcl.music.R;

public class DJSwitchPreference extends SwitchPreference{

    private Context mContext;
    private boolean mChecked;
    private CompoundButton.OnCheckedChangeListener mListener=new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.setChecked(!isChecked);
                return;
            }
            DJSwitchPreference.this.setChecked(isChecked);
        }

    };
    @SuppressLint("NewApi")
    public DJSwitchPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    public DJSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public DJSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public DJSwitchPreference(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onClick() {

        new AlertDialog.Builder(mContext)
        .setMessage(R.string.setting_switch_djmode)
        .setPositiveButton(R.string.swicth, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                DJSwitchPreference.super.onClick();
            }
        })
        .setNegativeButton(R.string.cancel, null)
        .create()
        .show();
    }

    /**
     * 实现自定义按钮
     * @param view
     */
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
//        View checkableView = view.findViewById(R.id.switchWidget);
//            if (checkableView instanceof Switch) {
//                final Switch switchView = (Switch) checkableView;
//                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//                mChecked=sharedPreferences.getBoolean("preference_dj_mode",true);
//                switchView.setChecked(mChecked);
//                switchView.setOnCheckedChangeListener(mListener);
//                switchView.setSwitchPadding(0);
//            }
        }
}
