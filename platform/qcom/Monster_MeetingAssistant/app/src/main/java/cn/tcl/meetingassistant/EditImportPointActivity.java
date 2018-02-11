/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import cn.tcl.meetingassistant.bean.ImportPoint;
import cn.tcl.meetingassistant.bean.Meeting;
import cn.tcl.meetingassistant.bean.MeetingDecisionData;
import cn.tcl.meetingassistant.bean.MeetingInfo;
import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.db.MeetingInfoDBUtil;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.services.RecordAbnormalState;
import cn.tcl.meetingassistant.utils.DensityUtil;
import cn.tcl.meetingassistant.utils.FileUtils;
import cn.tcl.meetingassistant.utils.MultiPicCopyUtil;
import cn.tcl.meetingassistant.utils.PermissionUtil;
import cn.tcl.meetingassistant.utils.TimeFormatUtil;
import cn.tcl.meetingassistant.view.AbsMeetingActivity;
import cn.tcl.meetingassistant.view.BackGroundActivity;
import cn.tcl.meetingassistant.view.CollapseView;
import cn.tcl.meetingassistant.view.ImportPointAdapter;
import cn.tcl.meetingassistant.view.NotificationHelper;
import cn.tcl.meetingassistant.view.RecordVoiceTitleBar;
import cn.tcl.meetingassistant.view.RecyclerViewItemDivider;
import cn.tcl.meetingassistant.view.SoundRecordItemView;
import mst.view.menu.PopupMenu;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-3.
 * the page user can edit import point
 */
public class EditImportPointActivity extends AbsMeetingActivity implements View.OnClickListener {

    private static final String TAG = EditImportPointActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10002;

    private static final int CODE_CAMERA_REQUEST = 10003;
    public static final int CODE_ALBUM_REQUEST = 10086;

    private final int MARGIN_TOP_RECORDING = 76;//DP
    private final int MARGIN_TOP_NOT_RECORDING = 65;//DP

    public static final String IMAGE_INFO_TO_PREVIEW = "image_info_to_preview";

    private enum PermissionRequester{
        SoundRecording,GetPic,PreViewPic,TackPhoto
    }

    private PermissionRequester mPermissionRequester;

    private enum PageState{
        SoundRecording,ShowMeetingInfo
    }

    private PageState mPageState;

    //Views
    private TextView mMeetingNameView;
    private TextView mEditMeetingBtn;
    private View mBottomBtns;
    private Button mEditDecisionBtn;
    private TextView mScanDecisionBtn;
    private ImageButton mTitleRecordVoiceBtn;
    private CollapseView mMeetingInfoTable;
    private ScrollView mMeetingInfoTableScroll;
    private ImageButton mMoreBtn;
    private RecyclerView mPageList;
    private LinearLayout mEditImportPointTitleBar;
    private RecordVoiceTitleBar mRecordVoiceTitleBar;
    private ImportPointAdapter mImportPointAdapter;

    // meeting info table
    private TextView mMeetingTopics;
    private TextView mMeetingPerson;
    private TextView mMeetingTime;
    private TextView mMeetingLocation;
    private View mBtnDivideView;
    private View mContentView;

    protected final long MEETING_NO_ID = -1;
    private long mMeetingId = MEETING_NO_ID;
    private Meeting mMeeting;
    private MeetingInfo mMeetingInfo;

    // the image which will be scanned
    private ImageInfo mImageInfo = new ImageInfo();


    private boolean mMeetingDeleteLock = false;

    // the params to support the handle of save image for import point
    public static final String IMPORT_POINT_IMAGE_PATH = "import_point_image_path";
    private String mImportPointImageDir;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private RecordAbnormalState mRecordAbnormalState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MeetingLog.i(TAG, "start onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_import_point_layout);
        initView();
        mPageState = PageState.ShowMeetingInfo;
        MeetingLog.i(TAG, "end onCreate");
    }

    @Override
    protected void onResume() {
        MeetingLog.i(TAG, "start onResume");
        super.onResume();
        setInfoTableData();
        initDecisionInfo();
        if(mPageState != null && mPageState == PageState.SoundRecording){
            mRecordVoiceTitleBar.setVisibility(View.VISIBLE);
            mEditImportPointTitleBar.setVisibility(View.GONE);
        }
        MeetingLog.i(TAG, "end onResume");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    private void initView() {
        mMeetingNameView = (TextView) findViewById(R.id.edit_import_meeting_name);
        mEditMeetingBtn = (TextView) findViewById(R.id.edit_meeting_info_btn);
        mBottomBtns = findViewById(R.id.edit_import_meeting_buttons);
        mEditDecisionBtn = (Button) findViewById(R.id.edit_decision_btn);
        mScanDecisionBtn = (TextView) findViewById(R.id.scan_decision_btn);
        mMeetingInfoTable = (CollapseView) findViewById(R.id.edit_import_meeting_info_table);
        mMoreBtn = (ImageButton) findViewById(R.id.edit_import_meeting_more_btn);
        mPageList = (RecyclerView) findViewById(R.id.edit_import_list);
        mBtnDivideView = (View) findViewById(R.id.divide_line_between_btn);
        mEditImportPointTitleBar = (LinearLayout) findViewById(R.id.edit_import_title_bar);
        mRecordVoiceTitleBar = (RecordVoiceTitleBar) findViewById(R.id.voice_record_title_bar);
        mTitleRecordVoiceBtn = (ImageButton) findViewById(R.id.edit_import_meeting_voice_btn);
        mMeetingInfoTableScroll = (ScrollView) findViewById(R.id.edit_import_meeting_info_table_scroll);
        mContentView = findViewById(R.id.edit_import_content);

        // init the list
        mImportPointAdapter = new ImportPointAdapter(this, mPageList);
        mPageList.setAdapter(mImportPointAdapter);
        mPageList.setLayoutManager(new LinearLayoutManager(this));
        mPageList.addItemDecoration(new RecyclerViewItemDivider());

        mEditMeetingBtn.setOnClickListener(this);
        mEditDecisionBtn.setOnClickListener(this);
        mScanDecisionBtn.setOnClickListener(this);
        mMoreBtn.setOnClickListener(this);
        mTitleRecordVoiceBtn.setOnClickListener(this);

        mRecordVoiceTitleBar.addOnStopClickListener(new RecordVoiceTitleBar.OnStopClickListener() {
            @Override
            public void onStopClick() {
                MeetingLog.i(TAG, "page change to edit mode");
                mRecordVoiceTitleBar.setVisibility(View.GONE);
                mEditImportPointTitleBar.setVisibility(View.VISIBLE);
                mImportPointAdapter.notifyDataSetChanged();
                mPageList.scrollToPosition(mImportPointAdapter.getItemCount() - 1);
                mPageState = PageState.ShowMeetingInfo;
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
                lp.setMargins(lp.leftMargin, DensityUtil.dip2px(EditImportPointActivity.this,MARGIN_TOP_NOT_RECORDING),lp.rightMargin,lp.bottomMargin);
                mContentView.setLayoutParams(lp);
            }
        });

        initInfoTable();

    }

    private void initInfoTable() {
        mMeetingInfoTable.setContent(R.layout.layout_meeting_info_expand, R.id.meeting_expand_topics, 1);
        mMeetingTopics = (TextView) findViewById(R.id.meeting_expand_topics);
        mMeetingPerson = (TextView) findViewById(R.id.meeting_expand_person);
        mMeetingTime = (TextView) findViewById(R.id.meeting_expand_time);
        mMeetingLocation = (TextView) findViewById(R.id.meeting_expand_location);
    }


    private void setInfoTableData() {
        mMeeting = MeetingStaticInfo.getCurrentMeeting();
        mMeetingInfo = mMeeting.getMeetingInfo();
        if (!TextUtils.isEmpty(mMeetingInfo.getTopics())) {
            StringBuffer displayString = new StringBuffer();
            int i = 1;
            for(String s : mMeetingInfo.getTopics().split("\n")){
                if(TextUtils.isEmpty(s)){
                    continue;
                }
                displayString.append(i).append(".\b").append(s).append("\n");
                i++;
            }
            displayString.deleteCharAt(displayString.length() - 1);
            //mMeetingTopics.addTextChangedListener(new SpannableTextWatcher(this));
            mMeetingTopics.setText(displayString.toString());
        }else{
            mMeetingTopics.setText(R.string.not_filled);
        }
        if (TextUtils.isEmpty(mMeetingInfo.getPersons())) {
            mMeetingPerson.setText(R.string.not_filled);
        } else {
            mMeetingPerson.setText(mMeetingInfo.getPersons());
        }
        if (TextUtils.isEmpty(mMeetingInfo.getAddress())) {
            mMeetingLocation.setText(R.string.not_filled);
        } else {
            mMeetingLocation.setText(mMeetingInfo.getAddress());
        }

        String startTime;
        String endTime;
        if (mMeetingInfo.getStartTime() == null || mMeetingInfo.getStartTime() == 0) {
            startTime = getString(R.string.not_filled);
        } else {
            startTime = TimeFormatUtil.getMMddTimeString(mMeetingInfo.getStartTime());
        }

        if (mMeetingInfo.getEndTime() == null || mMeetingInfo.getEndTime() == 0) {
            endTime = getString(R.string.not_filled);
        } else {
            endTime = TimeFormatUtil.getMMddTimeString(mMeetingInfo.getEndTime());
        }
        mMeetingTime.setText(startTime + " " + getString(R.string.to) + " " + endTime);

        if (mMeetingInfo.isEmpty()) {
            mEditMeetingBtn.setVisibility(View.VISIBLE);
            mMeetingInfoTableScroll.setVisibility(View.GONE);
        } else {
            mEditMeetingBtn.setVisibility(View.GONE);
            mMeetingInfoTableScroll.setVisibility(View.VISIBLE);
        }
        mMeetingNameView.setText(mMeetingInfo.getTitle());
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edit_meeting_info_btn:
                switchToEditMeetingPage();
                break;
            case R.id.edit_decision_btn:
                saveMeetingAndSwitchToEditDecisionPage();
                break;
            case R.id.scan_decision_btn:
                showScanDecision();
                break;
            case R.id.edit_import_meeting_more_btn:
                showMoreMenu();
                break;
            case R.id.edit_import_meeting_voice_btn:
                mPermissionRequester = PermissionRequester.SoundRecording;
                if (PermissionUtil.requestRecordPermission(this)) {
                    mRecordAbnormalState = new RecordAbnormalState(this);
                    mRecordAbnormalState.beforeStartRecord();
                }
                break;
            default:
                break;
        }
    }

    /**
     * switch to edit meeting decision page
     */
    private void switchToEditMeetingPage() {
        Intent intent = new Intent(this, EditMeetingInfoPageActivity.class);
        startActivity(intent);
        MeetingLog.i(TAG, "start activity " + EditMeetingInfoPageActivity.class.getSimpleName());
    }

    /**
     * switch to the decision edit page after save the meetingInfo
     * to make sure there is a meeting in database
     */
    private void saveMeetingAndSwitchToEditDecisionPage() {
        Intent intent = new Intent(EditImportPointActivity.this, EditDecisionActivity.class);
        startActivity(intent);
        MeetingLog.i(TAG, "start activity " + EditDecisionActivity.class.getSimpleName());
    }

    /**
     * open the decision activity
     */
    private void showScanDecision() {

        Intent intent = new Intent(this, BackGroundActivity.class);
        startActivity(intent);
        MeetingLog.i(TAG, "start activity " + DecisionListActivity.class.getSimpleName());
    }

    /**
     * show the menu
     */
    private void showMoreMenu() {
        PopupMenu popupMenu = new PopupMenu(this, mMoreBtn, Gravity.RIGHT);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_edit_meeting:
                        switchToEditMeetingPage();
                        break;
                    case R.id.menu_share:
                        showMeetingPreviewActivity(MeetingPreviewActivity.TYPE_SHARE_MEETING);
                        break;
                    case R.id.menu_export_to_local:
                        showMeetingPreviewActivity(MeetingPreviewActivity.TYPE_EXPORT_MEETING);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        popupMenu.inflate(R.menu.edit_meeting_menu);
        popupMenu.show();

    }

    private void showMeetingPreviewActivity(int type) {
        Intent intent = new Intent(this, MeetingPreviewActivity.class);
        intent.putExtra("operation_type", type);
        startActivity(intent);
        Intent intentBroadcast = new Intent();
        intentBroadcast.setAction(SoundRecordItemView.STOP_BROADCAST);
        sendBroadcast(intentBroadcast);
    }

    @Override
    public void onBackPressed() {
        if(mRecordVoiceTitleBar.isRecording()){
            mRecordVoiceTitleBar.showDialog(new RecordVoiceTitleBar.OnStopRecordingListener() {
                @Override
                public void onStopRecording() {
                    EditImportPointActivity.this.onBackPressed();
                }
            });
            return;
        }

        if (mMeeting.getImportPoints().size() <= 1 && mMeeting.getMeetingVoices().size()==0&&
                mMeeting.getMeetingDecisions().size()==0 && mMeeting.getMeetingInfo().isEmpty()){
            if(mMeeting.getImportPoints().size() == 1){
                ImportPoint importPoint = mMeeting.getImportPoints().get(0);
                if(importPoint.isEmpty()){
                    //delete meeting
                    if(mMeetingDeleteLock){
                        return;
                    }
                    mMeetingDeleteLock = true;
                    MeetingInfoDBUtil.delete(mMeeting.getId(), this, new MeetingInfoDBUtil.OnDoneDeletedListener() {
                        @Override
                        public void onDeleted(boolean isSuccess) {
                            MeetingStaticInfo.getInstance().getMeetingList().remove(mMeeting);
                            EditImportPointActivity.super.onBackPressed();
                            mMeetingDeleteLock = false;
                        }
                    });
                }else {
                    super.onBackPressed();
                }
            }
        }else{
            super.onBackPressed();
        }

    }

    // init the decisions of this meeting
    private void initDecisionInfo() {
        mMeetingId = MeetingStaticInfo.getCurrentMeeting().getId();
        mScanDecisionBtn.setVisibility(View.GONE);
        mBtnDivideView.setVisibility(View.GONE);

        List<MeetingDecisionData> decisionDatas = MeetingStaticInfo.getCurrentMeeting().getMeetingDecisions();

        if (decisionDatas != null && decisionDatas.size() > 0) {
            MeetingLog.i(TAG, "init decisions --> size is " + decisionDatas.size());
            mScanDecisionBtn.setVisibility(View.VISIBLE);
            StringBuilder stringBuilder = new StringBuilder();
            String num = "(" + decisionDatas.size() + ")";
            stringBuilder.append(getString(R.string.number_decision)).append(num);

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(stringBuilder);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(getColor(R.color.bullet_color)),stringBuilder.length() - num.length(),
                    stringBuilder.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mScanDecisionBtn.setText(spannableStringBuilder);
            //mScanDecisionBtn.setText(decisionDatas.size() + getString(R.string.number_decision));
            mBtnDivideView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public static class ImageInfo implements Parcelable {
        public String path;
        public int num;

        public static final Creator CREATOR = new Creator<ImageInfo>() {

            @Override
            public ImageInfo createFromParcel(Parcel parcel) {
                return new ImageInfo(parcel);
            }

            @Override
            public ImageInfo[] newArray(int i) {
                return new ImageInfo[i];
            }
        };

        public ImageInfo() {

        }

        public ImageInfo(Parcel parcel) {
            path = parcel.readString();
            num = parcel.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(path);
            parcel.writeInt(num);
        }
    }


    /**
     * go to the preview page
     *
     * @param dirPath
     * @param num
     */
    public void goToScanPage(String dirPath, int num) {

        mImageInfo.path = dirPath;
        mImageInfo.num = num;
        //check that if we got the external_storage permission
        mPermissionRequester = PermissionRequester.PreViewPic;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            startImagePreView(mImageInfo);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE && mPermissionRequester == PermissionRequester.PreViewPic) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startImagePreView(mImageInfo);
            } else {

            }
            return;
        }

        else if (requestCode == PermissionUtil.REQUEST_CODE_RECORD) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mRecordAbnormalState = new RecordAbnormalState(this);
                mRecordAbnormalState.beforeStartRecord();
            } else {

            }
        }

        else if(requestCode == PermissionUtil.REQUEST_CODE_WRITE && mPermissionRequester != null &&
                mPermissionRequester == PermissionRequester.GetPic && !TextUtils.isEmpty(mPicDirPath)){
            getPicFromAlbum(mPicDirPath);
        }

        else if(requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE && mPermissionRequester != null &&
                mPermissionRequester == PermissionRequester.TackPhoto && !TextUtils.isEmpty(mPhotoPath)){
            goToCamera(mPhotoPath);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startImagePreView(ImageInfo imageInfo) {
        Intent intent = new Intent(this, PhotoPreviewActivity.class);
        intent.putExtra(IMAGE_INFO_TO_PREVIEW, mImageInfo);
        startActivity(intent);
    }

    private String mPhotoPath;

    public void goToCamera(String path) {
        mPermissionRequester = PermissionRequester.TackPhoto;
        mPhotoPath = path;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            startCameraPage(mPhotoPath);
        }
    }


    //deal with the picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (resultCode == RESULT_CANCELED) {
            MeetingLog.i(TAG, "onActivityResult RESULT_CANCELED");
            return;
        }

        switch (requestCode) {
            case CODE_CAMERA_REQUEST:
                if (FileUtils.hasSdcard()) {
                    File tempFile = new File(FileUtils.getTempImagePath());
                    //rotate the picture
                    // DegreePicture(Uri.fromFile(tempFile));

                    // goto the page of edit pic
                    Intent newIntent = new Intent(this, EditPicActivity.class);
                    newIntent.setData(Uri.fromFile(tempFile));
                    newIntent.putExtra(IMPORT_POINT_IMAGE_PATH, mImportPointImageDir);
                    // import point id for image
                    startActivity(newIntent);
                }
                break;
            case CODE_ALBUM_REQUEST:
                MultiPicCopyUtil.addPicToDir(this, intent, mImportPointImageDir, new MultiPicCopyUtil.OnCopiedAPicListener() {
                    @Override
                    public void onCopied(String picName) {
                        mImportPointAdapter.notifyDataSetChanged();
                    }
                });
                break;
            default:
                return;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    private void startCameraPage(String string) {
        mImportPointImageDir = string;
        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (FileUtils.hasSdcard()) {
            File file = new File(FileUtils.getTempImagePath());
            if (file.exists()) {
                file.delete();
                MeetingLog.i(TAG, "delete file " + file.getPath());
            }

            File newFile = new File(file.getPath());

            Uri uri = FileProvider.getUriForFile(this, "cn.tcl.meetingassistant.fileprovider", newFile);
            intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        startActivityForResult(intentFromCapture, CODE_CAMERA_REQUEST);
    }

    public void startRecordVoice() {
        MeetingLog.i(TAG, "page change to recording voice");
        mRecordVoiceTitleBar.setVisibility(View.VISIBLE);
        mRecordVoiceTitleBar.startRecordService(mRecordAbnormalState);
        mEditImportPointTitleBar.setVisibility(View.GONE);
        mPageState = PageState.SoundRecording;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
        lp.setMargins(lp.leftMargin, DensityUtil.dip2px(this,MARGIN_TOP_RECORDING),lp.rightMargin,lp.bottomMargin);
        mContentView.setLayoutParams(lp);
    }

    // the pic dir path you want to get pic from gallery
    private String mPicDirPath;

    /**
     * entrance for get pic from Gallery
     * @param picDirPath
     */
    public void getCheckPermissionAndGetPic(String picDirPath){
        mPicDirPath = picDirPath;
        mPermissionRequester = PermissionRequester.GetPic;
        if (PermissionUtil.requestWritePermission(this)) {
            getPicFromAlbum(picDirPath);
        }
    }

    /**
     * go to the page of Gallery
     *
     * @param picDirPath
     */
    private void getPicFromAlbum(String picDirPath) {
        mImportPointImageDir = picDirPath;
        Intent intent = new Intent();
        intent.setClassName("cn.tcl.filemanager","cn.tcl.filemanager.photopicker.ImagePickerPlusActivity");
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, EditImportPointActivity.CODE_ALBUM_REQUEST);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            MeetingLog.i(TAG,"back");
        }
        return super.onKeyDown(keyCode, event);
    }

    //start record service
    public void startAudioRecord() {
        startRecordVoice();
    }

    public int getRecordState() {
        return mRecordVoiceTitleBar.getRecordState();
    }

    public void stopAudioRecord() {
        MeetingLog.i(TAG,"stopAudioRecord start");
        mRecordVoiceTitleBar.stopRecordInAbnormal();
        MeetingLog.i(TAG,"stopAudioRecord end");
    }

    @Override
    protected void onDestroy() {
        MeetingLog.d(TAG,"onDestroy");
        NotificationHelper.cancelNotification(this);
        super.onDestroy();
    }

    public void hideBottomBtns(EditText view){
        //mBottomBtns.setVisibility(View.GONE);
        // show soft input
        InputMethodManager immshow = (InputMethodManager) this
                .getSystemService(INPUT_METHOD_SERVICE);
        if (immshow != null && view.getInputType() != 0) {
            MeetingLog.d(TAG,"view get focus and show input");
            immshow.showSoftInput(getCurrentFocus(), 0);
        }else{
            MeetingLog.d(TAG,"immshow is " + immshow);
            MeetingLog.d(TAG,"view's input type is " + view.getInputType());
        }
    }

    public void showBottomBtns(EditText view){
        //mBottomBtns.setVisibility(View.VISIBLE);
        // hide soft input
        InputMethodManager immEdit = (InputMethodManager) this
                .getSystemService(INPUT_METHOD_SERVICE);
        if (immEdit != null) {
            immEdit.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
