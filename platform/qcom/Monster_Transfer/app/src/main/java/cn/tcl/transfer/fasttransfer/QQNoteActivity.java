/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.fasttransfer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Method;

import cn.tcl.transfer.R;
import cn.tcl.transfer.util.AppUtils;
import cn.tcl.transfer.util.DialogBuilder;
import mst.app.MstActivity;

public class QQNoteActivity extends MstActivity {
    private static final int CREATE_CODE = 1;
    private static final int DEF_SMS_REQ = 2;
    private static final int PERMISSION_CODE = 1;
    private Button mBtnCancel;
    private Button mBtnContinue;
    private Dialog mDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_qq_note);
        mBtnCancel = (Button)findViewById(R.id.cancel);
        mBtnContinue = (Button)findViewById(R.id.ok);
        mBtnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if (!Settings.System.canWrite(QQNoteActivity.this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, CREATE_CODE);
                    } else {
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                                || checkSelfPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES) != PackageManager.PERMISSION_GRANTED
                                || checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                                || checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED
                                || checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
                                || checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                                || checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED
                                || checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED
                                || checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED
                                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.REQUEST_INSTALL_PACKAGES, Manifest.permission.READ_SMS, Manifest.permission.READ_CALENDAR, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CALENDAR, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE);
                        } else {
                            Intent intent = new Intent(QQNoteActivity.this, ScanActivity.class);
                            startActivity(intent);
                        }
                    }

            }
        });
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_CODE) {
            if (Settings.System.canWrite(this)) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.REQUEST_INSTALL_PACKAGES, Manifest.permission.READ_SMS, Manifest.permission.READ_CALENDAR, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CALENDAR, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_CODE);
                } else {
                    Intent intent = new Intent(QQNoteActivity.this, ScanActivity.class);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(QQNoteActivity.this, R.string.qq_permission_denied, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == DEF_SMS_REQ) {
            if (!TextUtils.equals(Telephony.Sms.getDefaultSmsPackage(this),getPackageName())) {
                Toast.makeText(QQNoteActivity.this, R.string.sms_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onNavigationClicked(View view) {
        this.finish();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(QQNoteActivity.this, ScanActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(QQNoteActivity.this, R.string.qq_permission_denied, Toast.LENGTH_SHORT).show();
            }

        }
    }
}
