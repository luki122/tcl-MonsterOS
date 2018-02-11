package com.monster.paymentsecurity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.Window;

import mst.app.MstActivity;

/**
 * 首页面
 *
 * Created by logic on 16-11-21.
 */
public class ScanActivity extends MstActivity implements FragmentChangeHandler {

    boolean showResult = false;
    Bundle args;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(com.mst.internal.R.layout.screen_simple);
        addScanFragment(ScanFragment.TAG);
    }

    @Override
    protected void initialUI(Bundle savedInstanceState) {
        super.initialUI(savedInstanceState);
        inflateToolbarMenu(R.menu.menu_main_act);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showResult){
            addScanResultFragment(ScanResultFragment.TAG, args);
            showResult = false;
        }
    }

    @Override
    protected void initialWindowParams(Window window) {
        super.initialWindowParams(window);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings){
            Intent settingIntent = new Intent(this,SettingPreferenceActivity.class);
            startActivity(settingIntent);
            return true;
        }
        return super.onMenuItemClick(item);
    }


    private void addScanResultFragment(String tag, Bundle args) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment =  fm.findFragmentByTag(tag);
        if (fragment == null){
            fragment = ScanResultFragment.create(args.getParcelable(ScanResultFragment.REPORT_DATA));
            ft.add(com.mst.internal.R.id.content,
                    fragment, tag);
        }else {
            fragment.getArguments().putParcelable(ScanResultFragment.REPORT_DATA,
                    args.getParcelable(ScanResultFragment.REPORT_DATA));
            ft.attach(fragment);
        }
        ft.commit();

    }

    private void addScanFragment(String tag) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = ScanFragment.create();
        ft.add(com.mst.internal.R.id.content,
                    fragment, tag);
        ft.commit();
    }


    @Override
    public void notifyFragmentChange(@action int action, @NonNull String fragment, Bundle args) {
        if (action == FragmentChangeHandler.ACTION_ADD){
            if (fragment.equals(ScanResultFragment.TAG)) {
                if (isResumed()) {
                    addScanResultFragment(fragment, args);
                }else {
                    showResult = true;
                    this.args = args;
                }
            }else if (fragment.equals(ScanFragment.TAG)){
                addScanFragment(fragment);
            }
        }else if (action == FragmentChangeHandler.ACTION_REMOVE){
            removeFragment(fragment);
            resetScanFragmentUI(args != null);
        }
    }

    private void removeFragment(String tag) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = fm.findFragmentByTag(tag);
        if (fragment != null) {
            ft.remove(fragment);
            ft.commit();
        }
    }

    private void resetScanFragmentUI(boolean startScan) {
        FragmentManager fm = getFragmentManager();
        ScanFragment sf = (ScanFragment) fm.findFragmentByTag(ScanFragment.TAG);
        if (startScan){
            sf.doScanningAction(true);
        }else {
            sf.resetUI();
        }
    }
}
