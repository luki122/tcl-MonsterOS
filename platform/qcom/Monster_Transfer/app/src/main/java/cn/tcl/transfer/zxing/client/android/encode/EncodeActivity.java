/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.transfer.zxing.client.android.encode;

import android.graphics.Point;
import android.view.Display;
import android.view.MenuInflater;
import android.view.WindowManager;

import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.zxing.client.android.Contents;
import cn.tcl.transfer.zxing.client.android.FinishListener;
import cn.tcl.transfer.zxing.client.android.Intents;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import cn.tcl.transfer.R;

/**
 * This class encodes data from an Intent into a QR code, and then displays it full screen so that
 * another person can scan it with their device.
 *
 */
public final class EncodeActivity extends Activity {

    private static final String TAG = EncodeActivity.class.getSimpleName();

    private static final int MAX_BARCODE_FILENAME_LENGTH = 24;
    private static final Pattern NOT_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]");
    private static final String USE_VCARD_KEY = "USE_VCARD";

    private QRCodeEncoder qrCodeEncoder;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
        } else {
            String action = intent.getAction();
            if (Intents.Encode.ACTION.equals(action) || Intent.ACTION_SEND.equals(action)) {
                setContentView(R.layout.encode);
            } else {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.encode, menu);
        boolean useVcard = qrCodeEncoder != null && qrCodeEncoder.isUseVCard();
        int encodeNameResource = useVcard ? R.string.menu_encode_mecard : R.string.menu_encode_vcard;
        MenuItem encodeItem = menu.findItem(R.id.menu_encode);
        encodeItem.setTitle(encodeNameResource);
        Intent intent = getIntent();
        if (intent != null) {
            String type = intent.getStringExtra(Intents.Encode.TYPE);
            encodeItem.setVisible(Contents.Type.CONTACT.equals(type));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share:
                share();
                return true;
            case R.id.menu_encode:
                Intent intent = getIntent();
                if (intent == null) {
                    return false;
                }
                intent.putExtra(USE_VCARD_KEY, !qrCodeEncoder.isUseVCard());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            default:
                return false;
        }
    }

    private void share() {
        QRCodeEncoder encoder = qrCodeEncoder;
        if (encoder == null) { // Odd
            Log.e(TAG, "No existing barcode to send?");
            return;
        }

        String contents = encoder.getContents();
        if (contents == null) {
            Log.e(TAG, "No existing barcode to send?");
            return;
        }

        Bitmap bitmap;
        try {
            bitmap = encoder.encodeAsBitmap();
        } catch (WriterException we) {
            Log.e(TAG, "share", we);
            return;
        }
        if (bitmap == null) {
            return;
        }

        File bsRoot = new File(Environment.getExternalStorageDirectory(), "BarcodeScanner");
        File barcodesRoot = new File(bsRoot, "Barcodes");
        if (!barcodesRoot.exists() && !barcodesRoot.mkdirs()) {
            LogUtils.w(TAG, "Couldn't make dir " + barcodesRoot);
            showErrorMessage(R.string.msg_unmount_usb);
            return;
        }
        File barcodeFile = new File(barcodesRoot, makeBarcodeFileName(contents) + ".png");
        if (!barcodeFile.delete()) {
            LogUtils.w(TAG, "Could not delete " + barcodeFile);
            // continue anyway
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(barcodeFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
        } catch (FileNotFoundException fnfe) {
            Log.e(TAG, "Couldn't access file " + barcodeFile + " due to " + fnfe);
            showErrorMessage(R.string.msg_unmount_usb);
            return;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioe) {
                    // do nothing
                }
            }
        }

        Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " - " + encoder.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, contents);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + barcodeFile.getAbsolutePath()));
        intent.setType("image/png");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(Intent.createChooser(intent, null));
    }

    private static CharSequence makeBarcodeFileName(CharSequence contents) {
        String fileName = NOT_ALPHANUMERIC.matcher(contents).replaceAll("_");
        if (fileName.length() > MAX_BARCODE_FILENAME_LENGTH) {
            fileName = fileName.substring(0, MAX_BARCODE_FILENAME_LENGTH);
        }
        return fileName;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // This assumes the view is full screen, which is a good assumption
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        int width = displaySize.x;
        int height = displaySize.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 7 / 8;

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        try {
            boolean useVCard = intent.getBooleanExtra(USE_VCARD_KEY, false);
            qrCodeEncoder = new QRCodeEncoder(this, intent, smallerDimension, useVCard);
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            if (bitmap == null) {
                LogUtils.w(TAG, "Could not encode barcode");
                showErrorMessage(R.string.msg_encode_contents_failed);
                qrCodeEncoder = null;
                return;
            }

            ImageView view = (ImageView) findViewById(R.id.image_view);
            view.setImageBitmap(bitmap);

            TextView contents = (TextView) findViewById(R.id.contents_text_view);
            if (intent.getBooleanExtra(Intents.Encode.SHOW_CONTENTS, true)) {
                contents.setText(qrCodeEncoder.getDisplayContents());
                setTitle(qrCodeEncoder.getTitle());
            } else {
                contents.setText("");
                setTitle("");
            }
        } catch (WriterException e) {
            Log.e(TAG, "Could not encode barcode", e);
            showErrorMessage(R.string.msg_encode_contents_failed);
            qrCodeEncoder = null;
        }
    }

    private void showErrorMessage(int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.text_confirm, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }
}
