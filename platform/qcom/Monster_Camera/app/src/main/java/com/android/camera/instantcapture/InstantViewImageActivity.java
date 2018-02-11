package com.android.camera.instantcapture;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
/* MODIFIED-BEGIN by yuanxing.tan, 2016-05-05,BUG-2011611*/
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
/* MODIFIED-END by yuanxing.tan,BUG-2011611*/
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout; // MODIFIED by yuanxing.tan, 2016-05-05,BUG-2011611
import android.widget.ImageView;

import com.android.camera.Exif;
import com.android.camera.exif.ExifInterface;
import com.android.camera.util.CameraUtil;
import com.android.ex.camera2.portability.debug.Log;
import com.android.external.ExtSystemProperties;
import com.android.external.plantform.ExtBuild;
import com.tct.camera.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* MODIFIED-BEGIN by yuanxing.tan, 2016-05-05,BUG-2011611*/
public class InstantViewImageActivity extends Activity implements TextureView.SurfaceTextureListener,
    View.OnLayoutChangeListener {// MODIFIED by yuanxing.tan, 2016-03-21, BUG-1845001
    /* MODIFIED-END by yuanxing.tan,BUG-2011611*/
    private static final Log.Tag TAG = new Log.Tag("InstantActivity");

    private final int CHECK_BURST_PICTURE = 1;

    private ActionBar mActionBar;
    private TextureView mTextureView;
    private ImageView mBurstImageView;
    private ImageView mResultImageView;
    private FrameLayout mMainView; // MODIFIED by yuanxing.tan, 2016-05-05,BUG-2011611
    private DialogFragment mConfirmAndDeleteFragment;

    private InstantCaptureHelper mInstantCaptureHelper;
    private boolean mForgroundActivity;
    private static final String VIEW_BACK = "persist.sys.view_back";

    private static final int DOWN_SAMPLE_FACTOR = 4;

    private static final long GO_TO_CAMERA_DELAY = 1000;

    OnUiUpdateListener mOnUiUpdateListener = new OnUiUpdateListener() {
        @Override
        public void onUiUpdating(final int num) {
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
                            Log.i(TAG, "onUiUpdate " + num);
                            mActionBar.setTitle(getResources().getQuantityString(R.plurals.instant_capture_photos, num, num));
                            mBurstImageView.setVisibility(View.INVISIBLE);
                        }
                    }
            );
        }

        @Override
        public void onUiUpdated(final int num) {
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "onUiUpdated " + num + ", " + mDelayGoToCamera);
                            if (!mInstantCaptureHelper.isSingleShot() && mInstantCaptureHelper.hasSaveDone()) {
                                mActionBar.setTitle(getResources().getQuantityString(R.plurals.instant_capture_photos, num, num));
                                mBurstImageView.setVisibility(View.VISIBLE);
                                /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
                                mHandler.sendEmptyMessageDelayed(0,GO_TO_CAMERA_DELAY);
                            } else if (mInstantCaptureHelper.isSingleShot() && mInstantCaptureHelper.hasSaveDone()) {
                                mHandler.sendEmptyMessageDelayed(0,GO_TO_CAMERA_DELAY);
                            }
                            if (mDelayGoToCamera) {
                                mDelayGoToCamera = false;
                                startCameraActivity();
                            }
                        }
                    }
            );
        }
    };

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            startCameraActivity();
        }
    };

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
        if (mInstantCaptureHelper.gFirstFrame) {
            mInstantCaptureHelper.gFirstFrame = false;
            /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
            Log.i(TAG, "instant capture kpi, bitmap display in onSurfaceTextureUpdated");
        }
    }
    /* MODIFIED-BEGIN by yuanxing.tan, 2016-05-05,BUG-2011611*/
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                               int oldTop, int oldRight, int oldBottom) {
        Log.i(TAG, "onLayoutChange");
        updateTransform();
    }

    /**
     * Updates the transform matrix based current width and height of TextureView
     * and preview stream aspect ratio.
     */
    private void updateTransform() {
        int width = mMainView.getWidth();
        int height = mMainView.getHeight();
        float aspectRatio = mInstantCaptureHelper.getAspectRatio();
        Log.i(TAG, "updateTransform "+aspectRatio+", "+width+", "+height);
        if (aspectRatio == InstantCaptureHelper.MATCH_SCREEN || width == 0 || height == 0) {
            return;
        }
        boolean landscape = width > height;
        RectF previewRect = new RectF();
        int longerEdge = Math.max(width, height);
        int shorterEdge = Math.min(width, height);
        float previewLongerEdge = shorterEdge * aspectRatio;
        float previewShorterEdge = shorterEdge;
        float remainingSpaceAlongLongerEdge = longerEdge - previewLongerEdge;
        Log.i(TAG, "updateTransform " + remainingSpaceAlongLongerEdge);
        if (remainingSpaceAlongLongerEdge <= 0) {
            previewLongerEdge = longerEdge;
            previewShorterEdge = longerEdge / aspectRatio;
            if (landscape) {
                previewRect.set(0, height / 2 - previewShorterEdge / 2, previewLongerEdge,
                        height / 2 + previewShorterEdge / 2);
            } else {
                previewRect.set(width / 2 - previewShorterEdge / 2, 0,
                        width / 2 + previewShorterEdge / 2, previewLongerEdge);
            }
        } else {
            previewShorterEdge = shorterEdge;
            previewLongerEdge = shorterEdge * aspectRatio;
            if (landscape) {
                previewRect.set(remainingSpaceAlongLongerEdge / 2, 0, width-remainingSpaceAlongLongerEdge / 2, previewShorterEdge);
            } else {
                previewRect.set(0, remainingSpaceAlongLongerEdge / 2, previewShorterEdge, height - remainingSpaceAlongLongerEdge / 2);
            }
        }

        Matrix transform = new Matrix();
        transform.setRectToRect(new RectF(0, 0, width, height) , previewRect, Matrix.ScaleToFit.FILL);

        mTextureView.setTransform(transform);
    }
    /* MODIFIED-END by yuanxing.tan,BUG-2011611*/

    public interface OnUiUpdateListener {
        public void onUiUpdating(int num);
        public void onUiUpdated(int num);
    }

    private final BroadcastReceiver mKeyeventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "mKeyeventReceiver "+action);
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra("reason");
                Log.i(TAG, "mKeyeventReceiver reason "+reason);
                if ("homekey".equals(reason)) {
                    finishActivity();
                }
            }
        }
    };

    private boolean mHomeReceiverRegistered = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-25,BUG-1863182 */
        super.onCreate(savedInstanceState);
        mForgroundActivity = true;
        mInstantCaptureHelper = InstantCaptureHelper.getInstance();
        if (!mInstantCaptureHelper.isInitialized() || mInstantCaptureHelper.getForbidStartViewImageActivity()) {
            Log.i(TAG, "onCreate should not start,finish");
            super.finish();
            return;
            /* MODIFIED-END by yuanxing.tan,BUG-1863182 */
        }
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_ACTION_BAR);
        ExtBuild.init();// device() will return null if we don't init it.
        setContentView(R.layout.instant_view_image);
        mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setTitle("");
        mActionBar.setDisplayShowTitleEnabled(true);
        mTextureView = (TextureView) findViewById(R.id.textureview);
        mTextureView.setSurfaceTextureListener(this);// MODIFIED by yuanxing.tan, 2016-03-21, BUG-1845001
        mTextureView.addOnLayoutChangeListener(this);
        mBurstImageView = (ImageView) findViewById(R.id.icon_burst);
        mResultImageView = (ImageView) findViewById(R.id.result);
        mMainView = (FrameLayout) findViewById(R.id.mainView);
        Log.i(TAG, "onCreate");
        IntentFilter filter_keyevent = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mKeyeventReceiver, filter_keyevent);
        mHomeReceiverRegistered = true;
        mInstantCaptureHelper.registerOnUiUpdateListener(mOnUiUpdateListener, this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");
        mInstantCaptureHelper = InstantCaptureHelper.getInstance();
        mForgroundActivity = true;
        if (mInstantCaptureHelper.getForbidStartViewImageActivity()) {
            Log.i(TAG, "onNewIntent should not start,finish");
            finish();
            return;
        }
        resetView();
        super.onNewIntent(intent);
    }

    private void resetView() {
        mActionBar.setTitle("");
        mBurstImageView.setVisibility(View.INVISIBLE);
        mTextureView.setVisibility(View.INVISIBLE);
        mResultImageView.setVisibility(View.INVISIBLE);
        mDelayGoToCamera = false;
    }

    @Override
    protected void onStart() {// MODIFIED by yuanxing.tan, 2016-03-28,BUG-1861691
        Window window = getWindow();
/* MODIFIED-BEGIN by yongsheng.shan, 2016-06-14,BUG-2344343*/
//        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        if(ExtBuild.device() == ExtBuild.MTK_MT6755) {
            String view_back = ExtSystemProperties.get(VIEW_BACK);
            Log.i(TAG, "view_back value  = " + view_back);
            if (view_back != null && !view_back.equals("1")) { //MODIFIED by peixin, 2016-04-06,BUG-1913360
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                /* MODIFIED-END by yongsheng.shan,BUG-2344343*/
            }else {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
        }else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
        super.onStart();
        mOrientation = getWindowManager().getDefaultDisplay().getOrientation();
        Log.i(TAG, "onStart orientation = " + mOrientation);

        if (mInstantCaptureHelper.isCaptureDone()) {
            mInstantCaptureHelper.gFirstFrame = false;
            Log.i(TAG, "onStart capture done");
            showResultImageView(true);
        } else {
            Log.i(TAG, "onStart capture not done");
            mInstantCaptureHelper.changeDisplayOrientation(mOrientation);
            /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
            setSurfaceTexture();
        }

        /* MODIFIED-BEGIN by nie.lei, 2016-05-03,BUG-2012686*/
        if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
            int appOrientaion = getRequestedOrientation();
            if(appOrientaion != ActivityInfo.SCREEN_ORIENTATION_USER){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }
            Log.i(TAG, "onStart appOrientaion = " + getRequestedOrientation());
        }
        /* MODIFIED-END by nie.lei,BUG-2012686*/
    }

    public void setSurfaceTexture() {
        if (InstantCaptureHelper.USE_JPEG_AS_PICTURE_DISLAY) {
            return;
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "setSurfaceTexture in ui thread ");
                mTextureView.setVisibility(View.VISIBLE);
                mResultImageView.setVisibility(View.INVISIBLE);
                SurfaceTexture st = mInstantCaptureHelper.getSurfaceTexture();
                if (st != null && (mTextureView.getSurfaceTexture() == null || st != mTextureView.getSurfaceTexture())) {
                    Log.i(TAG, "setSurfaceTexture");
                    mTextureView.setSurfaceTexture(mInstantCaptureHelper.getSurfaceTexture());
                    mInstantCaptureHelper.setSurfaceTextureAttached();
                    updateTransform(); // MODIFIED by yuanxing.tan, 2016-05-05,BUG-2011611
                } else if (st == null) {
                    Log.i(TAG, "setSurfaceTexture st == null");
                } else {
                    Log.i(TAG, "setSurfaceTexture mTextureView.getSurfaceTexture() != null");
                }
            }
        });
    }

    /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
    public void showResultImageView(final boolean done) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean singleShot = mInstantCaptureHelper.isSingleShot();
                if (mResultImageView.getVisibility() == View.VISIBLE && singleShot) {
                    Log.i(TAG, "the image already show, return");
                    return;
                }
                ArrayList<byte[]> pictureDatas = mInstantCaptureHelper.getPictureDatas();
                int size = pictureDatas.size();
                Log.e(TAG, "showResultImageView, " + size);
                boolean needShowBurstIcon = false;

                if (size == 0) {
                    finish();
                    return;
                } else if (size == 1) {
                    if (singleShot) {
                        mActionBar.setTitle("");
                    } else {
                        mActionBar.setTitle(getResources().getQuantityString(R.plurals.instant_capture_photos, size, size));
                        needShowBurstIcon = true;
                    }
                } else {
                    mActionBar.setTitle(getResources().getQuantityString(R.plurals.instant_capture_photos, size, size));
                    needShowBurstIcon = true;
                }
                mTextureView.setVisibility(View.INVISIBLE);
                if (done && needShowBurstIcon) {
                    mBurstImageView.setVisibility(View.VISIBLE);
                }
                if (singleShot && (mResultImageView.getVisibility() == View.VISIBLE)) {
                    return;
                }
                if (singleShot) {
                    mResultImageView.setVisibility(View.VISIBLE);
                    final BitmapFactory.Options opts = new BitmapFactory.Options();
                    // Downsample the image
                    opts.inSampleSize = DOWN_SAMPLE_FACTOR;
                    mResultImageView.setImageBitmap(BitmapFactory.decodeByteArray(pictureDatas.get(0), 0, pictureDatas.get(0).length, opts));
                    Log.e(TAG, "instant capture kpi, bitmap display in showResultImageView");
                    return;
                }
                final ExifInterface exif = Exif.getExif(pictureDatas.get(size - 1));
                int orientation = Exif.getOrientation(exif);
                mDecodeTaskForReview = new DecodeImageForReview(pictureDatas.get(size - 1), orientation, false);
                mDecodeTaskForReview.execute();
            }
        });
        /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
    }

    private DecodeImageForReview mDecodeTaskForReview = null;
    private class DecodeTask extends AsyncTask<Void, Void, Bitmap> {
        private final byte [] mData;
        private final int mOrientation;
        private final boolean mMirror;

        public DecodeTask(byte[] data, int orientation, boolean mirror) {
            mData = data;
            mOrientation = orientation;
            mMirror = mirror;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            // Decode image in background.
            Bitmap bitmap = CameraUtil.downSample(mData, DOWN_SAMPLE_FACTOR);
            if (mOrientation != 0 || mMirror) {
                Matrix m = new Matrix();
                if (mMirror) {
                    // Flip horizontally
                    m.setScale(-1f, 1f);
                }
                m.preRotate(mOrientation);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m,
                        false);
            }
            return bitmap;
        }
    }

    private class DecodeImageForReview extends DecodeTask {
        public DecodeImageForReview(byte[] data, int orientation, boolean mirror) {
            super(data, orientation, mirror);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                return;
            }
            mResultImageView.setVisibility(View.VISIBLE);
            mResultImageView.setImageBitmap(bitmap);
            Log.i(TAG, "instant capture kpi, bitmap display");
            mDecodeTaskForReview = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "BURSTSHOTACTIVITY RESULT: " + requestCode + "," + resultCode);
        if (mDelayGoToCamera) {
            startCameraActivity();
            mDelayGoToCamera = false;
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        boolean screenOn = mInstantCaptureHelper.isScreenOn();
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
        Log.i(TAG, "onPause: screen:" + screenOn + " capture done:" + mInstantCaptureHelper.isCaptureDone());
        if (!screenOn && mInstantCaptureHelper.isCaptureDone()) {
        /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
            finishActivity();
        }
        /* MODIFIED-BEGIN by nie.lei, 2016-05-03,BUG-2012686*/
        if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
            int appOrientaion = getRequestedOrientation();
            if (appOrientaion != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            Log.i(TAG, "onPause appOrientaion = " + getRequestedOrientation());
        }
        /* MODIFIED-END by nie.lei,BUG-2012686*/
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        mInstantCaptureHelper.unRegisterOnUiUpdateListener();
        if (mHomeReceiverRegistered) {
            mHomeReceiverRegistered = false;
            unregisterReceiver(mKeyeventReceiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.rapid_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        startCameraActivity();
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
                if (mConfirmAndDeleteFragment != null) {
                    mConfirmAndDeleteFragment.dismiss();
                }
                mConfirmAndDeleteFragment = ConfirmAndDeleteDialogFragment.newInstance(mInstantCaptureHelper.getBurstCount());
                mConfirmAndDeleteFragment.show(getFragmentManager(), "dialog");
                return true;
            case android.R.id.home:
                Log.i(TAG, "home pressed");
                startCameraActivity();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean mDelayGoToCamera = false;

    private void startCameraActivity() {
        ArrayList<Uri> uris = mInstantCaptureHelper.getPictureUris();
        Log.i(TAG, "startCameraActivity " + mInstantCaptureHelper.getBurstCount() + ", " + uris);// MODIFIED by yuanxing.tan, 2016-03-28,BUG-1861691
        if (uris.size() > 0 && uris.size() >= mInstantCaptureHelper.getBurstCount()) {
            mInstantCaptureHelper.startCameraActivity(this, uris);
            finishActivity();
        } else {
            mDelayGoToCamera = true;
        }
    }

    @Override
    public void finish() {
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
        Log.i(TAG, "finish " + mForgroundActivity);
        if (mForgroundActivity) {
            mForgroundActivity = false;
            mDelayGoToCamera = false;
            /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
            moveTaskToBack(true);
        }
    }

    public static class ConfirmAndDeleteDialogFragment extends DialogFragment {

        private ConfirmAndDeleteDialogFragment() {
        }

        public static ConfirmAndDeleteDialogFragment newInstance(int deleteNum) {
            final ConfirmAndDeleteDialogFragment frag = new ConfirmAndDeleteDialogFragment();
            Bundle args = new Bundle();
            args.putInt("num", deleteNum);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String info = getResources().getString(R.string.delete_selection);
            int deleteNum = getArguments().getInt("num");

            if (deleteNum > 1) {
                info = String.format(getActivity().getResources().getString(R.string.delete_selection_burst), deleteNum);
            }
            return new AlertDialog.Builder(getActivity())
                    .setMessage(info)
                    .setPositiveButton(
                            R.string.delete, new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((InstantViewImageActivity) getActivity()).executeDeletion();
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
        task.execute(mInstantCaptureHelper.getPictureUris());
    }

    private int mOrientation;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mOrientation = getWindowManager().getDefaultDisplay().getOrientation();
        if (mInstantCaptureHelper.isCaptureDone()) {
            showResultImageView(true);
        } else {
            mInstantCaptureHelper.changeDisplayOrientation(mOrientation);
        }
        Log.e(TAG, "configChange: " + mOrientation);
    }

    public int getOrientation() {
        return mOrientation;
    }

    private class DeletionTask extends AsyncTask<ArrayList, Void, Void> {
        InstantViewImageActivity mContext;

        DeletionTask(InstantViewImageActivity context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(ArrayList... params) {
            ArrayList<Uri> uris = params[0];
            ContentResolver resolver = mContext.getContentResolver();
            for (int i = 0; i < uris.size(); i++)
                delete(uris.get(i), resolver);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (!mContext.isFinishing()) {
                mContext.finish();
            }
        }
    }

    private void delete(Uri uri, ContentResolver resolver) {
        Cursor c = resolver.query(uri, new String[]{
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
    }

    private final void finishActivity() {
        if (mConfirmAndDeleteFragment != null && mConfirmAndDeleteFragment.isAdded()) {
            mConfirmAndDeleteFragment.dismissAllowingStateLoss();
            mConfirmAndDeleteFragment = null;
        }
        finish();
    }
    public void openBurst(View view) {
        final String GALLERY_PACKAGE_NAME = "com.tct.gallery3d";
        final String BURSTSHOT_ACTIVITY_CLASS = "com.tct.gallery3d.app.BurstShotActivity";
        final String BURSTSHOT_ARRAYLIST = "burstshot-arraylist";
        final String KEY_LOCKED_CAMERA = "is-camera-review";// MODIFIED by yuanxing.tan, 2016-03-18, BUG-1533559
        List<Uri> uris = mInstantCaptureHelper.getPictureUris();
        ArrayList<String> idArrays = new ArrayList<>();
        for (Uri uri : uris) {
            idArrays.add(uri.getLastPathSegment());
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(GALLERY_PACKAGE_NAME, BURSTSHOT_ACTIVITY_CLASS));
        Bundle data = new Bundle();
        data.putStringArrayList(BURSTSHOT_ARRAYLIST, idArrays);
        data.putBoolean(KEY_LOCKED_CAMERA, true);// MODIFIED by yuanxing.tan, 2016-03-18, BUG-1533559
        intent.putExtras(data);
        try {
            startActivityForResult(intent, CHECK_BURST_PICTURE);
            mDelayGoToCamera = true;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uris.get(0), "image/*");
            startActivity(intent);
            finish();
        }
    }
}
