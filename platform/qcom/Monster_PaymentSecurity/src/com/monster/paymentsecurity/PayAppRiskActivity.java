package com.monster.paymentsecurity;

import android.content.Intent;
import android.os.Bundle;

import mst.app.AlertActivity;
import mst.app.dialog.AlertDialog;

/**
 * Created by sandysheny on 16-12-6.
 */

public class PayAppRiskActivity extends AlertActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDialog();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.pay_app_has_risk);
        builder.setPositiveButton(R.string.handle_risk, (dialog, which) -> {
            dialog.dismiss();
            Intent intent = new Intent(PayAppRiskActivity.this, ScanActivity.class);
            startActivity(intent);
            finish();

        });
        builder.setNegativeButton(R.string.continue_to_pay, (dialog, which) -> {
            dialog.dismiss();
            showConfirmDialog();

        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(dialog1 -> finish());
        dialog.show();
    }

    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_continue_to_pay);
        builder.setPositiveButton(R.string.handle_risk, (dialog, which) -> {
            dialog.dismiss();
            Intent intent = new Intent(PayAppRiskActivity.this, ScanActivity.class);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton(R.string.confirm_continue, (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(dialog1 -> finish());
        dialog.show();
    }
}
