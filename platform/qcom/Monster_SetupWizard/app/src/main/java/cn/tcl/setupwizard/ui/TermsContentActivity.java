/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import cn.tcl.setupwizard.R;

public class TermsContentActivity extends BaseActivity {

    private TextView mTermsName, mTermsContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_content);

        mTermsName = (TextView) findViewById(R.id.terms_name);
        mTermsContent = (TextView) findViewById(R.id.terms_content);
        findViewById(R.id.header_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(TermsContentActivity.this, UserTermsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        String action = getIntent().getAction();

        if (TextUtils.equals(action, UserTermsActivity.ACTION_LICENSE)) {
            mTermsName.setText(getString(R.string.user_terms_license));
            /* MODIFIED-BEGIN by xinlei.sheng, 2016-10-14,BUG-2669930*/
            mTermsContent.setText(R.string.privacy_content);
        } else if (TextUtils.equals(action, UserTermsActivity.ACTION_PRIVACY)) {
            mTermsName.setText(getString(R.string.user_terms_privacy));
            mTermsContent.setText(R.string.privacy_content);
            /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
        }
    }

    @Override
    public void onSetupFinished() {
        if (!this.isDestroyed()) {
            this.finish();
        }
    }
}
