
package com.tcl.monster.fota;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.tcl.monster.fota.ui.Crouton;
import com.tcl.monster.fota.ui.Style;

import mst.app.MstActivity;

public class AdvancedModeActivity extends MstActivity {

    private static final String KEY_TESTER_VALIDATED = "key_tester_validated";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean validated = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(KEY_TESTER_VALIDATED, false);

        if (validated) {
            getFragmentManager().beginTransaction().replace(com.mst.R.id.content,
                    new AdvancedModeFragment()).commit();
        } else {
            // Display the fragment as the main content.
            getFragmentManager().beginTransaction().replace(com.mst.R.id.content,
                    new AdvancedModeValidateFragment()).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onValidated() {
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(com.mst.R.id.content,
                new AdvancedModeFragment()).commit();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
        .putBoolean(KEY_TESTER_VALIDATED, true).apply();
    }

    public void onValidateFail() {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
        .putBoolean(KEY_TESTER_VALIDATED, false).apply();
        Crouton.makeText(this, "Please input correct password.", Style.CONFIRM).show();
    }

    @Override
    public void onNavigationClicked(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}