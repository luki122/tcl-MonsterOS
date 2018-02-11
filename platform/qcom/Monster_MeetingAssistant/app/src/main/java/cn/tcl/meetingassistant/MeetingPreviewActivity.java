/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.meetingassistant.bean.ImportPoint;
import cn.tcl.meetingassistant.bean.MeetingDecisionData;
import cn.tcl.meetingassistant.bean.MeetingInfo;
import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.bean.MeetingVoice;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.FileUtils;
import cn.tcl.meetingassistant.utils.PermissionUtil;
import cn.tcl.meetingassistant.utils.TimeFormatUtil;
import cn.tcl.meetingassistant.utils.Utility;
import cn.tcl.meetingassistant.view.AbsMeetingActivity;
import cn.tcl.meetingassistant.view.DialogHelper;
import cn.tcl.meetingassistant.view.ImportPointPreviewItemView;
import cn.tcl.meetingassistant.view.MeetingToast;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;



/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-1.
 * the page user can preview metting
 */
public class MeetingPreviewActivity extends AbsMeetingActivity implements View.OnClickListener{
    private MeetingInfo mMeetingInfo;
    public static int TYPE_SHARE_MEETING = 0;
    public static int TYPE_EXPORT_MEETING = 1;
    public static final String OPERATION_TYPE = "operation_type";
    public static final String SHARE = "share";
    public static final String SUCCESSFUL_SHARE = "I have successfully share my message through my app";

    public final String TAG = MeetingPreviewActivity.class.getSimpleName();
    private int mType = 0;
    private Button mPictureBtn;
    private Button mPdfBtn;
    private AlertDialog mDialog;
    private Uri mUri;
    private Context mContext;

    private String mImageFilePath;
    private String mPdfFilePath;
    private Bitmap mSaveBitMap;

    private enum SaveMode{
        PDF,PIC
    }

    private SaveMode mSaveMod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MeetingLog.i(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_preview_layout);
        mContext = this;

        Intent intent = getIntent();
        mType = intent.getIntExtra(OPERATION_TYPE, 0);

        mMeetingInfo = MeetingStaticInfo.getCurrentMeeting().getMeetingInfo();
        initMeetingInfo();
        initDecisionInfo();
        initImportPointInfo();
        initSoundRecordInfo();
        initBottomButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initMeetingInfo(){
        MeetingLog.i(TAG,"initMeetingInfo");
        initMeetingTitleInfo();
        initMeetingTopicsInfo();
        initMeetingTimeInfo();
        initMeetingPersonInfo();
        initMeetingLocationInfo();
    }

    private void initMeetingTitleInfo(){

        TextView meetingTitle = (TextView) findViewById(R.id.meeting_info_title);
        meetingTitle.setText(mMeetingInfo.getTitle());
    }

    private void initMeetingTopicsInfo(){
        LinearLayout parent = (LinearLayout)findViewById(R.id.meeting_info_topics);
        if(parent == null) return;

        String topics = mMeetingInfo.getTopics();
        List<String> topicList = Utility.parseStringByNewLine(topics);
        List<View> topicViewList = createMeetingTopicViews(topicList);
        addTopicViews(topicViewList);
    }

    private List<View> createMeetingTopicViews(List<String> strList){
        List<View> viewList = new ArrayList<>();

        if(strList != null && strList.size() > 0) {
            for (int i = 0; i < strList.size(); i++) {
                View view = createMeetingTopicView();
                if (view != null) {
                    TextView textView =
                            (TextView) view.findViewById(R.id.meeting_info_topic_text);
                    if (textView != null) {
                        textView.setText(i+1+". "+strList.get(i));
                    }
                    viewList.add(view);
                }
            }
        } else {
            View view = createMeetingTopicView();
            if (view != null) {
                TextView textView =
                        (TextView) view.findViewById(R.id.meeting_info_topic_text);
                if (textView != null) {
                    textView.setCompoundDrawables(null,null,null,null);
                }
                viewList.add(view);
            }
        }

        return viewList;
    }

    private View createMeetingTopicView(){
        LinearLayout parent = (LinearLayout)findViewById(R.id.meeting_info_topics);
        if(parent == null) return null;

        return LayoutInflater.from(MeetingPreviewActivity.this).inflate(
                R.layout.layout_meeting_preview_meeting_info_topic, parent, false);
    }

    private void addTopicViews(List<View> viewList) {
        if(viewList == null || viewList.size() <= 0) return;

        LinearLayout parent = (LinearLayout)findViewById(R.id.meeting_info_topics);
        if(parent == null) return;

        int marginBottomDimenId = R.dimen.layout_common_2dp;
        for(int i = 0; i < viewList.size(); i++){
            View view = viewList.get(i);
            if(i == viewList.size() - 1){
                marginBottomDimenId = 0;
            }
            LinearLayout.LayoutParams layoutParams = Utility.creatLayoutParams(
                    MeetingPreviewActivity.this, 0, 0, 0, marginBottomDimenId);
            view.setLayoutParams(layoutParams);
            parent.addView(view);
        }

    }

    private void initMeetingTimeInfo(){
        TextView meetingTime = (TextView) findViewById(R.id.meeting_expand_time);
        String startTime;
        String endTime;
        if (mMeetingInfo.getStartTime() == null || mMeetingInfo.getStartTime() == 0) {
            startTime = getString(R.string.not_filled);
        }else {
            startTime = TimeFormatUtil.getMMddTimeString(mMeetingInfo.getStartTime());
        }

        if (mMeetingInfo.getStartTime() == null || mMeetingInfo.getStartTime() == 0) {
            endTime = getString(R.string.not_filled);
        }else {
            endTime = TimeFormatUtil.getMMddTimeString(mMeetingInfo.getEndTime());
        }
        meetingTime.setText(startTime + "   "+getString(R.string.to)+"   " + endTime);
    }

    private void initMeetingPersonInfo(){
        TextView meetingPerson = (TextView) findViewById(R.id.meeting_expand_person);
        String persons = mMeetingInfo.getPersons();
        if (!TextUtils.isEmpty(persons)) {
            meetingPerson.setText(persons);
        }
    }

    private void initMeetingLocationInfo(){
        TextView meetingLocation = (TextView) findViewById(R.id.meeting_expand_location);
        String location = mMeetingInfo.getAddress();
        if (!TextUtils.isEmpty(location)) {
            meetingLocation.setText(location);
        }
    }
    private void initDecisionInfo(){
//        initDecisionTitleInfo();
        initDecisionContentInfo();
    }

//    private void initDecisionTitleInfo() {
//        View view = findViewById(R.id.meeting_decision_title);
//        if(view != null) {
//            TextView textView =
//                    (TextView)view.findViewById(R.id.meeting_preview_sub_title);
//            if(textView != null) {
//                textView.setText(R.string.decision);
//            }
//        }
//    }

    private void initDecisionContentInfo(){

        List<MeetingDecisionData> decisionsArrayList = MeetingStaticInfo.getCurrentMeeting().getMeetingDecisions();
        if(decisionsArrayList == null) return;
        int length = decisionsArrayList.size();
        int notEmptySize = 0;
        if(length > 0){
            List<View> decisionItemViews = new ArrayList<>();
            for(int i = 0; i < length; i++){
                if(!decisionsArrayList.get(i).isEmpty()){
                    View view = createDecisionInfoItemView(decisionsArrayList.get(i),i+1);
                    notEmptySize++;
                    if(view != null){
                        decisionItemViews.add(view);
                    }
                }
            }

            addDecisionItemViews(decisionItemViews);
            if(notEmptySize > 0) {
                setViewBackground(decisionItemViews);

                View view = findViewById(R.id.meeting_decision);
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    private View createDecisionInfoItemView(final MeetingDecisionData data,final int i){
        LinearLayout parent = (LinearLayout)findViewById(R.id.meeting_decision_content);
        if(parent == null) return null;

        View itemView = LayoutInflater.from(MeetingPreviewActivity.this).inflate(R.layout.layout_meeting_preview_decision_list, parent, false);
        if(itemView == null) return null;
        TextView decisionNumber = (TextView)itemView.findViewById(R.id.decision_number);
        TextView topicView = (TextView)itemView.findViewById(R.id.meeting_decision_topic);
        TextView parsonView = (TextView)itemView.findViewById(R.id.meeting_decision_person);
        TextView endTimeView = (TextView)itemView.findViewById(R.id.meeting_decision_endtime);

        String decisionTopic = "";
        String decisionPerson = "";
        String decisionDeadline = "";

        if(data != null){
            decisionTopic = data.getDecisionInfo();
            decisionPerson = data.getPersons();
            decisionNumber.setText(getString(R.string.decision) +" " +i);
            if(data.getDeadline() != 0){
                decisionDeadline = TimeFormatUtil.getDateTimeTimeString(data.getDeadline());
            }
        }

        if(topicView != null && !decisionTopic.isEmpty()){
            topicView.setText(decisionTopic);
        }

        if(parsonView != null && !decisionPerson.isEmpty()){
            parsonView.setText(decisionPerson);
        }

        if(endTimeView != null && !decisionDeadline.isEmpty()){
            endTimeView.setText(decisionDeadline);
        }

        return itemView;
    }

    private void addDecisionItemViews(List<View> viewList){
        if(viewList == null || viewList.size() == 0) return;

        LinearLayout parent = (LinearLayout)findViewById(R.id.meeting_decision_content);
        if(parent == null) return;

        for(int i = 0; i < viewList.size(); i++){
            View view = viewList.get(i);
            LinearLayout.LayoutParams layoutParams = Utility.creatLayoutParams(
                    MeetingPreviewActivity.this, 0, 0, 0, R.dimen.layout_common_1dp);
            view.setLayoutParams(layoutParams);
            parent.addView(view);
        }
    }

    private void initImportPointInfo() {
        initImportPointTitleInfo();
        initImportPointContentInfo();
    }

    private void initImportPointTitleInfo() {
        View view = findViewById(R.id.meeting_import_point_title);
        if(view != null) {
            TextView textView =
                    (TextView)view.findViewById(R.id.meeting_preview_sub_title);
            if(textView != null) {
                textView.setText(R.string.highlight);
            }
        }
    }

    private void initImportPointContentInfo() {
        List<ImportPoint> queryResult = MeetingStaticInfo.getCurrentMeeting().getImportPoints();

        if(queryResult == null) return;
        int length = queryResult.size();
        MeetingLog.d(TAG,"initImportPointInfo onDoneQuery length = "+length);
        if(length > 0){
            List<View> importPointItemViews = new ArrayList<>();
            for(int i = 0; i < length; i++){
                ImportPoint data = queryResult.get(i);
                if(data != null && !data.isEmpty()) {
                    View view = new ImportPointPreviewItemView(data, MeetingPreviewActivity.this);
                    importPointItemViews.add(view);
                }
            }

            addImportPointItemViews(importPointItemViews);
            if(importPointItemViews.size() > 0) {
                setViewBackground(importPointItemViews);

                View view = findViewById(R.id.meeting_import_point);
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    private void addImportPointItemViews(List<View> viewList) {
        if(viewList == null || viewList.size() == 0) return;

        LinearLayout parent = (LinearLayout)findViewById(R.id.meeting_import_point_content);
        if(parent == null) return;

        for(int i = 0; i < viewList.size(); i++){
            View view = viewList.get(i);
            LinearLayout.LayoutParams layoutParams = Utility.creatLayoutParams(
                    MeetingPreviewActivity.this, 0, 0, 0, R.dimen.layout_common_1dp);
            view.setLayoutParams(layoutParams);
            parent.addView(view);
        }
    }

    private void setViewBackground(List<View> viewList){
        if(viewList == null || viewList.size() == 0) return;

        int length = viewList.size();
        if(length == 1) {
            View view = viewList.get(0);
            view.setBackground(null);
            view.setBackgroundColor(Color.WHITE);
        } else {
            int resId = 0;
            for (int i = 0; i < length; i++) {
                View view = viewList.get(i);
                view.setBackground(null);
                if (i == 0) {
                    resId = R.drawable.bg_import_point_card_up;
                } else if (i == length - 1) {
                    resId = R.drawable.bg_import_point_card_down;
                } else {
                    resId = R.drawable.bg_import_point_card_mid;
                }
                view.setBackgroundResource(resId);
            }
        }
    }

    private void initBottomButton() {
        mPictureBtn = (Button) findViewById(R.id.btn_bottom_right);
        mPdfBtn = (Button) findViewById(R.id.btn_bottom_left);
        if (mType == TYPE_EXPORT_MEETING) {
            mPictureBtn.setText(R.string.export_meeting_as_picture);
            mPdfBtn.setText(R.string.export_meeting_as_pdf);
        }else if(mType == TYPE_SHARE_MEETING){
            mPictureBtn.setText(R.string.share_meeting_as_picture);
            mPdfBtn.setText(R.string.share_meeting_as_pdf);
        }

        mPictureBtn.setOnClickListener(this);
        mPdfBtn.setOnClickListener(this);
    }

    private void initSoundRecordInfo() {
        //initSoundRecordTitleInfo();
        initSoundRecordContentInfo();
    }

    private void initSoundRecordTitleInfo() {
        View view = findViewById(R.id.meeting_sound_record_title);
        if(view != null) {
            TextView textView =
                    (TextView)view.findViewById(R.id.meeting_preview_sub_title);
            if(textView != null) {
                textView.setText(R.string.audio);
            }
        }
    }

    private void initSoundRecordContentInfo(){
        List<MeetingVoice> list = MeetingStaticInfo.getCurrentMeeting().getMeetingVoices();
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.meeting_sound_record);

        for(MeetingVoice meetingVoice : list){
            String content = meetingVoice.getVoiceText();
            if(!TextUtils.isEmpty(content)){
                viewGroup.addView(createSoundRecordItemView(meetingVoice));
                viewGroup.setVisibility(View.VISIBLE);
            }
        }

    }

    private View createSoundRecordItemView(final MeetingVoice data){
        LinearLayout parent = (LinearLayout)findViewById(R.id.meeting_decision_content);
        if(parent == null) return null;
        View itemView = LayoutInflater.from(MeetingPreviewActivity.this).inflate(R.layout.layout_meeting_preview_sound_record, parent, false);
        if(itemView == null) return null;

        TextView fileNameView = (TextView)itemView.findViewById(R.id.meeting_preview_sub_title);
        TextView textView = (TextView)itemView.findViewById(R.id.meeting_sound_record_content);

        String fileName = "";
        String text = "";
        if(data != null){
            fileName = new File(data.getVoicePath()).getName();
            text = data.getVoiceText();
        }
        fileNameView.setText(fileName.substring(0,fileName.indexOf('.')));
        textView.setText(text);
        return itemView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_bottom_right:
                mPictureBtn.setClickable(false);
                mSaveMod = SaveMode.PIC;
                saveFile(mSaveMod);
                break;
            case R.id.btn_bottom_left:
                mPdfBtn.setClickable(false);
                mSaveMod = SaveMode.PDF;
                saveFile(mSaveMod);
                break;
            default:
                break;
        }
    }

    class SaveMeetingPictureAsync extends AsyncTask<Void,String,String>{

        @Override
        synchronized protected String doInBackground(Void... voids) {
            return  saveMeetingAsPicture();
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = DialogHelper.showProgressDialog(MeetingPreviewActivity.this,getString(R.string.please_wait));
            mDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            if (mDialog.isShowing()){
                mDialog.dismiss();
                showToast(mImageFilePath);
            }
            MeetingToast.makeText(MeetingPreviewActivity.this,"",MeetingToast.LENGTH_SHORT).setGravity(Gravity.TOP,0,0);
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,mUri));
            mPictureBtn.setClickable(true);
            if(mType == TYPE_SHARE_MEETING){
                scannerFile(s,true,mSaveMod);
                //shareMeetingByPicture(s,);
            }else {
                scannerFile(s,false,mSaveMod);
            }
            if(mSaveBitMap != null){
                mSaveBitMap.recycle();
                mSaveBitMap = null;
            }
            System.gc();
        }
    }

    class SaveMeetingPDFAsync extends AsyncTask<Void,String,String>{
        @Override
        synchronized protected String doInBackground(Void... voids) {
            return saveMeetingAsPDF();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = DialogHelper.showProgressDialog(MeetingPreviewActivity.this,getString(R.string.please_wait));
            mDialog.show();
        }
        @Override
        protected void onPostExecute(String s) {
            if (mDialog.isShowing()){
                mDialog.dismiss();
                showToast(mPdfFilePath);
            }
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,mUri));
            mPdfBtn.setClickable(true);
            if(mType == TYPE_SHARE_MEETING){
                scannerFile(s,true,mSaveMod);
                //shareMeetingByPDF();
            }else{
                scannerFile(s,false,mSaveMod);
            }
            System.gc();
        }
    }

    private String saveMeetingAsPicture() {
        mSaveBitMap = getScrollBitmap();
        File file;
        String filePath = getPicturePath();
        if(mSaveBitMap != null) {
            file = new File(filePath);
            mUri = FileProvider.getUriForFile(this, "cn.tcl.meetingassistant.fileprovider", file);
            MeetingLog.i(TAG,mUri.toString());
            //mUri = Uri.fromFile(file);
            savePicToImageFile(mSaveBitMap, filePath);
        }
        return filePath;
    }

    private Bitmap getScrollBitmap() {
        LinearLayout scrollView = (LinearLayout)findViewById(R.id.meeting_preview_content);
        if(scrollView == null) return null;

        int height = 0;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            height += scrollView.getChildAt(i).getHeight();
        }

        Bitmap bitmap = Bitmap.createBitmap(scrollView.getWidth(), height, Bitmap.Config.RGB_565);
        if(bitmap != null) {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(mContext.getColor(R.color.app_background));
            if(canvas != null) {
                scrollView.draw(canvas);
            }
        }
        return bitmap;
    }

    private void savePicToImageFile(Bitmap bitmap, String filepath) {
        if(bitmap == null || filepath.isEmpty()) return;

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filepath);
            if (null != fos) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String saveMeetingAsPDF() {
        String pdfFilePath = getPdFilePath();

        File file = new File(pdfFilePath);
        mUri = Uri.fromFile(file);
        //mUri = FileProvider.getUriForFile(this, "cn.tcl.meetingassistant.fileprovider", file);
        MeetingLog.i(TAG,mUri.toString());
        //mUri = Uri.fromFile(file);

        LinearLayout view = (LinearLayout)findViewById(R.id.meeting_preview_content);
        int width = view.getWidth();
        int height = 0;
        for (int i = 0; i < view.getChildCount(); i++) {
            height += view.getChildAt(i).getHeight();
        }

        PdfDocument doc = new PdfDocument();
        // crate a page description
        PageInfo pageInfo = new PageInfo.Builder(width, height, 1).create();
        // start a page
        Page page = doc.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        canvas.drawColor(mContext.getColor(R.color.app_background));
        // draw something on the page
        view.draw(canvas);

        // finish the page
        doc.finishPage(page);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(pdfFilePath);
            doc.writeTo(fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(doc != null) {
                doc.close();
            }
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return pdfFilePath;
    }

    private void shareMeetingByPicture(Uri uri) {
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, SHARE);
        intent.putExtra(Intent.EXTRA_TEXT, SUCCESSFUL_SHARE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getTitle()));
    }

    private void shareMeetingByPDF(Uri uri) {
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, SHARE);
        intent.putExtra(Intent.EXTRA_TEXT, SUCCESSFUL_SHARE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getTitle()));
    }

    private String getPdFilePath() {
        String filePath = FileUtils.getUnusedFileName(mMeetingInfo.getTitle(),"pdf",mContext);
        mPdfFilePath = filePath;
        return mPdfFilePath;
    }

    private String getPicturePath() {
        String fileName = FileUtils.getUnusedFileName(mMeetingInfo.getTitle(),"jpg",mContext);
        mImageFilePath = fileName;
        return fileName;
    }

    private void showToast(String filePath){
        if(mType == TYPE_EXPORT_MEETING){
            File file = new File(filePath);
            if(file.exists()){
                MeetingToast.makeText(this,R.string.successful_exporting,
                        MeetingToast.LENGTH_SHORT).show();
                onBackPressed();
            }else {
                MeetingToast.makeText(this,R.string.Failure_to_export,
                        MeetingToast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtil.REQUEST_CODE_WRITE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveFile(mSaveMod);
            } else {
                // Permission Denied
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                mPictureBtn.setClickable(true);
                mPdfBtn.setClickable(true);
            }
            return;
        }
    }

    private void saveFile(SaveMode mode){
        if(PermissionUtil.requestWritePermission(this)){
            if(mode == SaveMode.PDF){
                new SaveMeetingPDFAsync().execute();
            }else if(mode == SaveMode.PIC){
                new SaveMeetingPictureAsync().execute();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onDestroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSaveBitMap != null){
            mSaveBitMap.recycle();
            mSaveBitMap = null;
        }
        System.gc();
    }

    private void scannerFile(String filePath,boolean toShare,SaveMode shareType){
        MediaScannerConnection.scanFile(this, new String[]{filePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String s, Uri uri) {
                MeetingLog.i(TAG,"onScanCompleted "+ uri);
                if(!toShare){
                    return;
                }
                if(shareType == SaveMode.PIC){
                    shareMeetingByPicture(uri);
                }else if(shareType == SaveMode.PDF){
                    shareMeetingByPDF(uri);
                }
            }
        });
    }

}
