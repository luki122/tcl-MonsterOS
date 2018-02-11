package com.monster.interception.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import mst.preference.*;
import mst.preference.Preference.OnPreferenceChangeListener;
import com.monster.interception.R;
import com.monster.interception.util.BlackUtils;
import mst.app.MstActivity;

public class Settings extends MstActivity {
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Fragment f = Fragment.instantiate(this, SettingsFragment.class.getName(), null);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(com.mst.R.id.content, f);
        ViewGroup content = (ViewGroup)findViewById(com.mst.R.id.content);
        transaction.commitAllowingStateLoss();
        getFragmentManager().executePendingTransactions();
        setTitle(R.string.action_settings);
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }
}
