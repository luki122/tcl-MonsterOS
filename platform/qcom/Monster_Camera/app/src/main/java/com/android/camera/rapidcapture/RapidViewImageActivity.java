
package com.android.camera.rapidcapture;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.android.camera.CameraActivity;
import com.android.camera.util.ImageLoader;
import com.tct.camera.R;

import java.io.File;

public class RapidViewImageActivity extends Activity {
    private static final String TAG = "RapidViewImageActivity";
    private Uri mSourceUri = null;
    private LoadBitmapTask mLoadBitmapTask = null;
    private ImageView image;
    private ActionBar mActionBar;
    private DialogFragment mConfirmAndDeleteFragment;
    public static boolean mIsRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();

        window.requestFeature(Window.FEATURE_ACTION_BAR);
        WindowManager.LayoutParams params = window.getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

        window.setAttributes(params);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.rapid_view_image);// need change
        image = (ImageView) findViewById(R.id.image);
        mActionBar = getActionBar();
        // hiden the actionbar icon
        mActionBar.setDisplayShowHomeEnabled(false);

        IntentFilter filter_screen_off = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mShutdownReceiver, filter_screen_off);

        if (intent.getData() != null) {
            mSourceUri = intent.getData();
            startLoadBitmap(mSourceUri);
        }
        RapidCaptureHelper.getInstance().acquireScreenWakeLock(this);
        mIsRunning = true;
    }
    private final BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "screen off");
            if (mConfirmAndDeleteFragment != null && mConfirmAndDeleteFragment.isVisible()) {
                mConfirmAndDeleteFragment.dismiss();
                mConfirmAndDeleteFragment = null;
            }
            finish();
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.rapid_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsRunning = false;
        Log.d(TAG,"onDestroy");
        unregisterReceiver(mShutdownReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                if (mConfirmAndDeleteFragment != null)
                {
                    mConfirmAndDeleteFragment.dismiss();
                }
                mConfirmAndDeleteFragment = ConfirmAndDeleteDialogFragment.newInstance();
                mConfirmAndDeleteFragment.show(getFragmentManager(), "dialog");
                return true;
            case android.R.id.home:
                boolean isInKeyguard = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked();
                Intent intent;
                if (isInKeyguard) {
                    intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
                } else {
                    intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                }
                intent.setClass(this, CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                try {
                    startActivity(intent);
                    return true;
                } catch (ActivityNotFoundException e) {
                    finish();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class ConfirmAndDeleteDialogFragment extends DialogFragment {
        public ConfirmAndDeleteDialogFragment() {
        }

        public static ConfirmAndDeleteDialogFragment newInstance() {
            final ConfirmAndDeleteDialogFragment frag = new ConfirmAndDeleteDialogFragment();
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.delete_selection)
                    .setPositiveButton(
                            R.string.delete, new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((RapidViewImageActivity) getActivity()).executeDeletion();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }).create();
        }
    }

    public void executeDeletion() {
        DeletionTask task = new DeletionTask(this);
        task.execute(mSourceUri);
    }

    private class DeletionTask extends AsyncTask<Uri, Void, Void> {
        RapidViewImageActivity mContext;

        DeletionTask(RapidViewImageActivity context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Uri... params) {
            Uri uri = params[0];
            ContentResolver resolver = mContext.getContentResolver();
            Cursor c = resolver.query(uri, new String[] {
                MediaStore.Audio.Media.DATA
            }, null, null, null);
            String path = null;
            try {
                if (c != null && c.moveToFirst()) {
                    path = c.getString(0);
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            resolver.delete(uri, null, null);
            if (!TextUtils.isEmpty(path)) {
                File f = new File(path);
                f.delete();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (!mContext.isFinishing()) {
                mContext.finish();
            }
        }
    }

    private void startLoadBitmap(Uri uri) {
        if (uri != null) {
            mLoadBitmapTask = new LoadBitmapTask();
            mLoadBitmapTask.execute(uri);
        }
    }

    private class LoadBitmapTask extends AsyncTask<Uri, Void, Bitmap> {
        int mBitmapSize;
        Context mContext;
        Rect mOriginalBounds;
        int mOrientation;

        public LoadBitmapTask() {
            mBitmapSize = getScreenImageSize();
            mContext = getApplicationContext();
            mOriginalBounds = new Rect();
            mOrientation = 0;
        }

        @Override
        protected Bitmap doInBackground(Uri... params) {
            Uri uri = params[0];
            Bitmap bmap = ImageLoader.loadConstrainedBitmap(uri, mContext, mBitmapSize,
                    mOriginalBounds, false);
            mOrientation = ImageLoader.getMetadataRotation(mContext, uri);
            return bmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            doneLoadBitmap(result, new RectF(mOriginalBounds), mOrientation);
        }
    }

    private int getScreenImageSize() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        return (int) Math.max(outMetrics.heightPixels, outMetrics.widthPixels);
    }

    private void doneLoadBitmap(Bitmap bitmap, RectF bounds, int orientation) {
        if (bitmap != null && bitmap.getWidth() != 0 && bitmap.getHeight() != 0) {
            image.setImageBitmap(bitmap);
        } else {

        }
    }
}
