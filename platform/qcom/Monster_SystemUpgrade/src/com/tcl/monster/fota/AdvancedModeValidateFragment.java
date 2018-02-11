
package com.tcl.monster.fota;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class AdvancedModeValidateFragment extends Fragment {

    EditText mEditText;
    Button mButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.advanced_validate, null);
        mEditText = (EditText) v.findViewById(R.id.passwordEditText);
        mButton = (Button) v.findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String pass = mEditText.getText().toString();
                String actualPass = "fotaapp*#1221#";
                AdvancedModeActivity activity = (AdvancedModeActivity) getActivity();
                if (pass.equalsIgnoreCase(actualPass)) {
                    activity.onValidated();
                } else {
                    activity.onValidateFail();
                }
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

            }
        });
        return v;
    }
}
