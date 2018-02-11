package com.monster.interception.activity;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import mst.preference.CheckBoxPreference;
import mst.preference.Preference;
import mst.preference.Preference.OnPreferenceChangeListener;
import mst.preference.PreferenceScreen;
import mst.preference.PreferenceFragment;
import mst.preference.SwitchPreference;

import com.monster.interception.util.BlackUtils;
import com.monster.interception.R;

public class SettingsFragment extends PreferenceFragment  implements OnPreferenceChangeListener{
    private Preference mBlack, mMark;
    private SwitchPreference mSmsSwitch, mInterceptSwitch;
    private int mBlackCount = 0;
    private int mMarkCount = 0;
    private ContentResolver mContentResolver;
    private AsyncQueryHandler mQueryHandler;
    private Activity mActivity;
    private SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.layout.set);

        mSmsSwitch = (SwitchPreference) findPreference("sms");
        mInterceptSwitch = (SwitchPreference) findPreference("intercept");
        mBlack = (Preference) findPreference("black_name");
        mMark = (Preference) findPreference("mark");
        mActivity = getActivity();

        mSmsSwitch.setOnPreferenceChangeListener(this);
        mInterceptSwitch.setOnPreferenceChangeListener(this);
        mContentResolver = mActivity.getContentResolver();
        mQueryHandler = new QueryHandler(mContentResolver, mActivity);
        mPreferences = mActivity.getSharedPreferences("settings",
                Context.MODE_PRIVATE);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mQueryHandler.startQuery(0, null, BlackUtils.BLACK_URI, null,
                "isblack=1 and reject>0", null, null);
        mQueryHandler.startQuery(1, null, BlackUtils.MARK_URI, null, null, null, null);
    }

    @Override
    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        // TODO Auto-generated method stub
        boolean b = (Boolean) arg1;
        String s = arg0.getKey();
        if (s.equals("sms")) {
            Intent intent = new Intent("com.android.reject.RUBBISH_MSG_REJECT");
            intent.putExtra("isRejectRubbishMsg", b);
            mActivity. sendBroadcast(intent);
        } else {
//            Intent intent = new Intent("com.android.reject.BLACK_MSG_REJECT");
//            intent.putExtra("isRejectBlack", b);
//            mActivity.sendBroadcast(intent);
            Editor editor = mPreferences.edit();
            editor.putBoolean("notification", b);
            editor.apply();
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        // TODO Auto-generated method stub
        String key = preference.getKey();
        if (key.equals("black_name")) {
            Intent intent = new Intent(mActivity.getApplicationContext(), BlackList.class);
            mActivity.startActivity(intent);
        } else if (key.equals("mark")) {
            Intent intent = new Intent(mActivity.getApplicationContext(), MarkList.class);
            mActivity.startActivity(intent);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private class QueryHandler extends AsyncQueryHandler {
        private final Context context;

        public QueryHandler(ContentResolver cr, Context context) {
            super(cr);
            this.context = context;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            // TODO Auto-generated method stub
            super.onQueryComplete(token, cookie, cursor);
            if (cursor != null) {
                if (token == 0) {
                    mBlackCount = cursor.getCount();
                    // black_name.SetArrowText(black_list_size+getResources().getString(R.string.item),
                    // true);

                } else {
                    mMarkCount = cursor.getCount();
                    // mark.SetArrowText(mark_list_size+getResources().getString(R.string.item),
                    // true);
                }

            }
            if (cursor != null) {
                cursor.close();
            }
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            // TODO Auto-generated method stub
            super.onUpdateComplete(token, cookie, result);
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            // TODO Auto-generated method stub
            super.onInsertComplete(token, cookie, uri);
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            // TODO Auto-generated method stub
            super.onDeleteComplete(token, cookie, result);
            System.out.println("删除完毕" + result);
        }

    }
}
