/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.intsig.scanner.ScannerSDK;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.FileUtils;
import cn.tcl.meetingassistant.view.CropView;
/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-3.
 * the edit pic page
 */
public class EditPicActivity extends AppCompatActivity {

    private static final String TAG = EditPicActivity.class.getSimpleName();

    // load jni library
    static
    {
        System.loadLibrary("scanner");
    }

    // the bitmap of the pic to handle
    Bitmap mPicBitmap;

    // the out path of cut pic
    String mPicOutPut;

    // image store dir
    String RECORD_PIC_DIR = FileUtils.getImageDirPath();
    // temp image path
    String TEMP_PIC_URI = RECORD_PIC_DIR + ".temp_cut.jpg";

    // the cut pic view
    CropView preView;

    Toolbar mToolbar;

    // current scale
    private float mScale = 1.0f;
    private float[] mBitmapLeftCorner;

    // scanner sdk
    private ScannerSDK mScannerSDK;

    // bitmap detect bound
    private int[] mBitmapDetectBound;
    private static final int TRIM_IMAGE_MAXSIDE = 0;

    // trim bound
    private float[] mViewTtrimBound;
    private int [] mLastDetectBorder;

    //appkey for debug 21491770fe8c2b06698f004551-GPY
    //appkey for release  c893545a4bb811eb4fb8004550-GPY

    // APPKEY
    private static final String APPKEY = "c893545a4bb811eb4fb8004550-GPY";

    // current mode is smart mode or not
    private boolean mIsSmartMode = true;

    ImageButton mSmartExchange;
    TextView mSmartExchangeTextView;

    private View mExchangeView;
    private View mRotateToLeft;
    private View mRotateToRight;
    private View mConfirm;
    private View mBackBtn;
    private TextView mSmartTextView;
    private String mDestDir;

    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the pic uri
        Uri imageUri = getIntent().getData();
        MeetingLog.i(TAG,"onCreate :image uri :" + imageUri);

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 2;
        mPicBitmap = BitmapFactory.decodeFile(imageUri.getPath(), bmOptions);

        // get the dir path which image need to be stored
        mDestDir = getIntent().getStringExtra(EditImportPointActivity.IMPORT_POINT_IMAGE_PATH);
        MeetingLog.i(TAG,"onCreate:destDir :" + mDestDir);

        // pic file
        File picFileDir = new File(RECORD_PIC_DIR);

        //make sure the dir exists
        if (!picFileDir.exists()) {
            picFileDir.mkdir();
        }

        // compress pic
        try{
            mPicBitmap.compress(CompressFormat.JPEG, 50, new FileOutputStream(TEMP_PIC_URI));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * the default store path and name for outputting image
         */
        String picName ="meetingImage"+ System.currentTimeMillis()+".jpg";
        File destDir = new File(mDestDir);
        // make sure the dir is exists
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        // file path
        mPicOutPut = mDestDir + File.separator+ picName ;

        /**
         * get sdk
         */
        mScannerSDK = new ScannerSDK();
        new Thread(new Runnable() {

            @Override
            public void run() {

                int code = mScannerSDK.initSDK(EditPicActivity.this, APPKEY);
                MeetingLog.d(TAG, "mScannerSDK init code="+code);
            }
        }).start();

        // the bounds
        mBitmapDetectBound = new int[8];
        mViewTtrimBound = new float[8];
        mBitmapLeftCorner = new float[2];
        initView();
        hideNavigationBar();
    }

    public void hideNavigationBar() {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

        if( android.os.Build.VERSION.SDK_INT >= 19 ){
            uiFlags |= 0x00001000;
        } else {
            uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    }

    public void initView() {
        setContentView(R.layout.activity_edit_pic);

        mToolbar = (Toolbar) findViewById(R.id.cut_pic_page_toolbar);

        mExchangeView = (View)findViewById(R.id.exchanged);
        mRotateToLeft = (View)findViewById(R.id.rotate_to_left);
        mRotateToRight = (View)findViewById(R.id.rotate_to_right);
        mConfirm = (View)findViewById(R.id.cut_pic_page_confirm_btn);
        mBackBtn = findViewById(R.id.cut_pic_page_back_btn);
        mSmartTextView = (TextView) findViewById(R.id.smart_exchange_text);

        // the crop view
        preView = (CropView)findViewById(R.id.preView);

        // set cropView's inner pic
        mScale = preView.getScale();
        if(mPicBitmap != null) {
            preView.post(new Runnable() {
                @Override
                public void run() {
                    preView.setBitmap(mPicBitmap);

                }
            });
            preView.post(smartCutPic);
        }

        /**
         * exchange the smart mode and full screen mode;
         */
        mSmartExchange = (ImageButton) findViewById(R.id.smart_exchange);
        mExchangeView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mIsSmartMode = !mIsSmartMode;
                exChangeSmartMode();
            }
        });
        mSmartExchangeTextView = (TextView)findViewById(R.id.smart_exchange_text);

        /**
         * save the pic;
         */
        mConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startTrim();
                mBitmapDetectBound = preView.getCropBound();
            }
        });
        /**
         * left rotate the pic ;
         */
        ImageButton leftRotate =(ImageButton)findViewById(R.id.left_rotate);
        mRotateToLeft.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                rotatePic(270);
            }
        });
        /**
         * Right rotate the pic ;
         */
        ImageButton rightRotate =(ImageButton) findViewById(R.id.right_rotate);
        mRotateToRight.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                rotatePic(90);
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void rotatePic(float rotateDegrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateDegrees);
        mPicBitmap = Bitmap.createBitmap(mPicBitmap,0,0,mPicBitmap.getWidth(),mPicBitmap.getHeight(),matrix,true);
        preView.setBitmap(mPicBitmap);
        try{
            mPicBitmap.compress(CompressFormat.JPEG, 50, new FileOutputStream(TEMP_PIC_URI));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mIsSmartMode) {
            preView.post(smartCutPic);
        }else {
            preView.setFull(false);
        }
    }
    private void startTrim(){
        if(!TextUtils.isEmpty(TEMP_PIC_URI)){
            TrimTask trimTask = new TrimTask(TEMP_PIC_URI);
            trimTask.execute();
        }
    }


    /**
     * the async task to trim pic
     */
    class TrimTask extends AsyncTask<Void, Void, Boolean>{
        private long mStartTime;
        boolean trimSucced = false;
        /**
         * the abstract path the trimmed pic to store
         */
        private String mPath;
        public TrimTask(String path){
            mPath = path;
        }
        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            MeetingLog.d(TAG,"TrimTask, doInBackground");
            long tempTime = 0;
            boolean succeed = false;
            mStartTime = System.currentTimeMillis();

            int threadContext = mScannerSDK.initThreadContext();
            MeetingLog.d(TAG,"TrimTask, initThreadContext, cost time:"+(System.currentTimeMillis()-mStartTime));

            tempTime = System.currentTimeMillis();
            int imageStruct = mScannerSDK.decodeImageS(mPath);
            MeetingLog.d(TAG,"TrimTask, decodeImageS, cost time:"+(System.currentTimeMillis()-tempTime));

            if(imageStruct != 0){
                tempTime = System.currentTimeMillis();
                trimSucced = mScannerSDK.trimImage(threadContext, imageStruct, mBitmapDetectBound,TRIM_IMAGE_MAXSIDE);
                if(!trimSucced) return false;
                MeetingLog.d(TAG,trimSucced+"+trimSucceed");
                MeetingLog.d(TAG,"TrimTask, trimImage, cost time:"+(System.currentTimeMillis()-tempTime));
                tempTime = System.currentTimeMillis();
                mScannerSDK.saveImage(imageStruct, mPicOutPut, 80);
                MeetingLog.i(TAG,"TrimTask, saveImage, cost time:"+(System.currentTimeMillis()-tempTime));
                tempTime = System.currentTimeMillis();
                mScannerSDK.releaseImage(imageStruct);
                MeetingLog.d(TAG,"TrimTask, releaseImage, cost time:"+(System.currentTimeMillis()-tempTime));
                succeed = true;
            }
            mScannerSDK.destroyContext(threadContext);
            return succeed;
        }
        protected void onPostExecute(Boolean result) {
            MeetingLog.d(TAG, "result="+result);
            if(result){
                // Toast.makeText(EditPicActivity.this,"trim successful", Toast.LENGTH_LONG).show();
                MeetingStaticInfo.updateCurrentTime(EditPicActivity.this);
                mPicBitmap.recycle();
                finish();
            }else{
                //if(!trimSucced) Toast.makeText(EditPicActivity.this,"trim fail", Toast.LENGTH_LONG).show();
                MeetingLog.d(TAG, "result="+result);
            }
            dismissProgressDialog();
        }
    }
    protected void exChangeSmartMode (){
        if (mIsSmartMode) {
            goToSmartMode();
            mSmartTextView.setText(R.string.full_image);
            mSmartExchange.setImageResource(R.drawable.ic_full_image);
        } else {
            goToCustomMode();
            mSmartTextView.setText(R.string.smart_crop);
            mSmartExchange.setImageResource(R.drawable.ic_smart_cut);
        }
    }

    //full screen
    private void goToCustomMode() {
        preView.setFull(false);
    }

    private void goToSmartMode() {
        preView.post(smartCutPic);
    }

    private Runnable smartCutPic = new Runnable() {
        @Override
        public void run() {
            //Image is in the center of the view,and scale to make sure one of the bound fill the
            //screen.
            //In order to display the trim bound,need to calculate the ratio of view's size and
            //actual image size.
            //As the same time,need to calculate the image left-top coordinates to detect the borders
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(TEMP_PIC_URI, options);
            int bitmapWidth = options.outWidth;
            int bitmapHeight = options.outHeight;
            MeetingLog.d(TAG, "bitmapWidth="+bitmapWidth+" bitmapHeight="+bitmapHeight);
            if(bitmapWidth > 0 && bitmapHeight > 0){
                    int viewWidth = preView.getWidth();
                    int viewHeight = preView.getHeight();

                    float scale = preView.getScale();
                    float leftX = (viewWidth - bitmapWidth*scale)/2;
                    float leftY = (viewHeight - bitmapHeight*scale)/2;
                    mScale = scale;

                    mBitmapLeftCorner = new float[]{leftX,leftY};
                    //mCurrentInputImagePath = imageFilePath;
                    DetectBorderTask detectTask = new DetectBorderTask(TEMP_PIC_URI);
                    detectTask.execute();
            }else{
                MeetingLog.d(TAG, "bitmapWidth="+bitmapWidth+" bitmapHeight="+bitmapHeight);
            }
        }
    };
    float[] bound ;
    class DetectBorderTask extends AsyncTask<Void, Void, Boolean>{
        private long mStartTime;
        /**
         * The image's absolute path to be detected
         */
        private String mPath;
        public DetectBorderTask(String path){
            mPath = path;
        }
        @Override
        protected void onPreExecute() {
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean succeed = false;
            long tempTime = 0;
            mStartTime = System.currentTimeMillis();
            int threadContext = mScannerSDK.initThreadContext();
            MeetingLog.d(TAG,"DetectBorderTask, initThreadContext, cost time:"+(System.currentTimeMillis()-mStartTime));
            tempTime = System.currentTimeMillis();
            int imageStruct = mScannerSDK.decodeImageS(mPath);
            MeetingLog.d(TAG,"DetectBorderTask, decodeImageS, cost time:"+(System.currentTimeMillis()-tempTime));

            MeetingLog.d(TAG, "mPath="+mPath +"imageStruct ="+imageStruct);
            mLastDetectBorder = null;
            if(imageStruct != 0){
                // detect borders
                tempTime = System.currentTimeMillis();
                mLastDetectBorder = mScannerSDK.detectBorder(threadContext, imageStruct);
                MeetingLog.d(TAG,"DetectBorderTask, detectBorder, cost time:"+(System.currentTimeMillis()-tempTime));
                MeetingLog.d(TAG, "detectAndTrimImageBorder, borders="+Arrays.toString(mLastDetectBorder));
                tempTime = System.currentTimeMillis();
                int[] imgBound = getImageSizeBound(mPath);
                bound = getScanBoundF(imgBound, mLastDetectBorder);
                MeetingLog.d(TAG, "detectAndTrimImageBorder, trimImage");
                MeetingLog.d(TAG,"DetectBorderTask, fix border, cost time:"+(System.currentTimeMillis()-tempTime));
                tempTime = System.currentTimeMillis();
                mScannerSDK.releaseImage(imageStruct);
                MeetingLog.d(TAG,"DetectBorderTask, releaseImage, cost time:"+(System.currentTimeMillis()-tempTime));
                succeed  = true;
            }
            mScannerSDK.destroyContext(threadContext);
            MeetingLog.d(TAG,"DetectBorderTask, cost time:"+(System.currentTimeMillis()-mStartTime));
            return succeed;
        }
        protected void onPostExecute(Boolean result) {

            //dismissProgressDialog();
            MeetingLog.d(TAG, "result="+result);
            if(result){
                // marke the borders of trimming
                preView.setCrop(bound);
                //mBtnNext.setVisibility(View.VISIBLE);
            }else{
                MeetingLog.d(TAG, "result="+result);
            }
        }

    }
    /**
     * get the image size
     *
     * @param pathName
     * @return size
     */
    private static int[] getImageSizeBound(String pathName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(pathName, options);
        int[] wh = null;
        if (options.mCancel || options.outWidth == -1
                || options.outHeight == -1) {
            MeetingLog.d(TAG, "getImageBound error " + pathName);
        } else {
            wh = new int[2];
            wh[0] = options.outWidth;
            wh[1] = options.outHeight;
        }
        return wh;
    }
    /**
     * Adjust the borders to make sure the borders in the view
     * @param size the original image size
     * @param borders the detect borders
     * @return resized borders
     */
    private static float[] getScanBoundF(int[] size, int[] borders) {
        float[] bound = null;
        if (size != null) {
            if ((borders == null)) {
                MeetingLog.d(TAG, "did not found bound");
                bound = new float[] { 0, 0, size[0], 0, size[0], size[1], 0, size[1] };
            } else {
                bound = new float[8];
                for (int j = 0; j < bound.length; j++) {
                    bound[j] = borders[j];
                }
                for (int i = 0; i < 4; i++) { // the bound for detect
                    if (bound[i * 2] < 0)// x
                        bound[i * 2] = 0;
                    if (bound[i * 2 + 1] < 0)// y
                        bound[i * 2 + 1] = 0;
                    if (bound[i * 2] > size[0])// x
                        bound[i * 2] = size[0];
                    if (bound[i * 2 + 1] > size[1])// y
                        bound[i * 2 + 1] = size[1];
                }
            }
        }
        return bound;
    }


    private void showProgressDialog(){
        if(null == mProgressDialog){
            mProgressDialog = new ProgressDialog(this);
        }
        mProgressDialog.show();
    }

    private void dismissProgressDialog(){
        if(null != mProgressDialog){
            mProgressDialog.dismiss();
        }
    }
}
