/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.recinbox.sdk.IflytekLybClient;

import org.w3c.dom.Text;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.bean.MeetingVoice;
import cn.tcl.meetingassistant.db.MeetingVoiceDBUtil;
import cn.tcl.meetingassistant.db.OnDoneInsertAndUpdateListener;
import cn.tcl.meetingassistant.iflytek.OfflineToText;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.services.PlayerService;
import cn.tcl.meetingassistant.services.PlayerService.OnRefreshUiListener;
import cn.tcl.meetingassistant.services.PlayerService.PlayerBinder;
import cn.tcl.meetingassistant.services.PlayerServiceConnectionManager;
import cn.tcl.meetingassistant.services.SoundRecorderService;
import cn.tcl.meetingassistant.utils.FileUtils;
import mst.view.menu.PopupMenu;
import mst.widget.SliderLayout;
import mst.widget.SliderView;

import java.io.File;
import java.text.NumberFormat;

/**
 * the item for audio
 */
public class SoundRecordItemView extends RelativeLayout implements OnClickListener {

    private final String TAG = SoundRecordItemView.class.getSimpleName();
    private MeetingVoice mMeetingVoice;
    private ServiceConnection mPlayerService;
    private PlayerService mService;

    //views for playing
    private View mAudioPlayContainer;
    private View mAudioProgressBarContainer;
    private ImageButton mVoicePlayBtn;
    private ImageButton mVoicePauseBtn;
    private ImageButton mVoiceStopBtn;
    private ImageButton mVoiceToTextBtn;
    private BookmarkSeekBar mBookmarkSeekBar;
    private TextView mVoiceFileName;
    private TextView mExpandText;

    //audio to text views
    private View mAudioToTextContainer;
    private TextView mAudioToTextFileName;
    private TextView mAudioToTextProgress;
    private AudioTextDeleteButton mAudioTextDeleteButton;

    //view for text
    private View mTextContainer;

    private ExpandEditTextView mTextView;

    private ImageButton mTextDeleteBtn;


    // the string change length whether save text or not
    private final int SAVE_MAX_LENGTH = 20;
    private int mNoSaveLength = 0;
    private boolean isTextSaved = true;
    private boolean isSaving = false;

    private ImportPointAdapter mParentViewAdapter;
    private OnRefreshUiListener mOnRefreshUiListener;

    private PlayerServiceConnectionManager mManager;
    private SliderView mSliderView;

    IflytekLybClient mIflytekLybClient;

    private Context mContext;

    public static final String STOP_BROADCAST = "stop_broadcast";

    /*BroadcastReceiver for listener Battery status*/
    private BroadcastReceiver mSoundRecordBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SoundRecordItemView.STOP_BROADCAST)) {
                MeetingLog.d(TAG,"receive the broadcast to stop");
                stop();
            }
        }
    };

    public SoundRecordItemView(Context paramContext) {
        this(paramContext, null);
    }

    public SoundRecordItemView(Context context, AttributeSet paramAttributeSet) {
        super(context, paramAttributeSet);
        mContext = context;
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                inflate(R.layout.item_voice_layout, this, true);
        mAudioPlayContainer = view.findViewById(R.id.voice_item_title_play);

        mBookmarkSeekBar = ((BookmarkSeekBar) view.findViewById(R.id.voice_item_progress_bar));
        mVoicePlayBtn = ((ImageButton) view.findViewById(R.id.voice_item_play_btn));
        mVoiceToTextBtn = ((ImageButton) view.findViewById(R.id.voice_item_toText_btn));
        mVoiceStopBtn = ((ImageButton) view.findViewById(R.id.voice_item_stop_btn));
        mVoiceFileName = ((TextView) view.findViewById(R.id.voice_item_file_name));
        mVoicePauseBtn = (ImageButton) view.findViewById(R.id.voice_item_pause_btn);
        mTextDeleteBtn = (ImageButton) view.findViewById(R.id.voice_item_text_delete_btn);
        mAudioToTextContainer = view.findViewById(R.id.voice_item_title_toText);
        mAudioToTextFileName = (TextView) view.findViewById(R.id.voice_item_file_name_toText);
        mAudioToTextProgress = (TextView) view.findViewById(R.id.voice_item_file_name_toText_progress);
        mAudioTextDeleteButton = (AudioTextDeleteButton) view.findViewById(R.id.voice_item_toText_progress_btn);
        mSliderView = (SliderView) view.findViewById(R.id.meeting_voice_sliderView);
        mSliderView.addTextButton(0,context.getString(R.string.delete));
        mTextContainer = view.findViewById(R.id.voice_item_text_container);
        mTextView = (ExpandEditTextView) view.findViewById(R.id.voice_item_audio_toText);
        mAudioProgressBarContainer = view.findViewById(R.id.voice_item_progress_bar_container);
        mExpandText = (TextView) view.findViewById(R.id.voice_item_audio_toText_expand_text);

        mAudioPlayContainer.setOnClickListener(this);
        mTextDeleteBtn.setOnClickListener(this);
        mTextView.setOnExpandAbleListener(new ExpandEditTextView.OnExpandAbleListener() {
            @Override
            public void onExpandAble(boolean expandable) {
                if(expandable){
                    mExpandText.setVisibility(View.VISIBLE);
                }else{
                    mExpandText.setVisibility(View.INVISIBLE);
                }
            }
        });

        mTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                MeetingLog.i(TAG,"mTextView setOnFocusChangeListener " + b);
                if(b){
                    mTextView.expand(null);
                    //editRequestFocus();
                }else {
                    editLoseFocus();
                }
            }
        });
        mVoicePlayBtn.setOnClickListener(this);
        mVoiceToTextBtn.setOnClickListener(this);
        mVoiceStopBtn.setOnClickListener(this);
        mVoicePauseBtn.setOnClickListener(this);
        mAudioTextDeleteButton.setOnClickListener(this);
        mExpandText.setOnClickListener(this);
        mBookmarkSeekBar.setOnProgressChangeListener(new BookmarkSeekBar.OnProgressChangeListener() {
            public void onProgressChange(int paramAnonymousInt) {
                if (mService != null) {
                    mService.setSeekTo(paramAnonymousInt);
                }
            }

            @Override
            public void onProgressPress() {
                if(mService != null){
                    mService.pausePlayPiece();
                }
            }
        });

        mOnRefreshUiListener = new OnRefreshUiListener() {
            public void onCompletionPlay() {
                MeetingLog.d(TAG,"onCompletionPlay");
                goToIdle();
                mManager.setManagerInfo(mPlayerService,
                        "",mService);
            }

            public void onPausePlay() {
                MeetingLog.d(TAG,"onPausePlay");
                goToPause();
            }

            @Override
            public void onDestroy() {
                MeetingLog.d(TAG,"onDestroy");
                goToIdle();
                mManager.setManagerInfo(mPlayerService,
                        "",mService);
            }

            public void onRefreshProgressUi(long progress) {
                mBookmarkSeekBar.setProgress((int) progress);
            }

            public void onRefreshTimeUi(long paramAnonymous2Long) {
            }
        };

        // set real time backup
        realTimeBackup();

        // set the listener to know when text should to be saved
        mTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // text is changed,need to save
                isTextSaved = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // init IflytekLybClient
        initIflytekLybClient();

        mTextView.setOnExpandStatusChangedListener(new ExpandEditTextView.OnExpandStatusChangedListener() {
            @Override
            public void onExpandStatusChanged(boolean isExpand) {
                rotateArrow(isExpand);
                if(isExpand){
                    mParentViewAdapter.getExpandHelper().expand(mMeetingVoice);
                    mExpandText.setText(R.string.collapse);
                }else {
                    mParentViewAdapter.getExpandHelper().collapse(mMeetingVoice);
                    mExpandText.setText(R.string.expand);
                }
            }
        });



        mSliderView.setOnSliderButtonClickListener(new SliderView.OnSliderButtonLickListener() {
            @Override
            public void onSliderButtonClick(int i, View view, ViewGroup viewGroup) {
                if(i == 0){
                    int msg ;
                    if (!TextUtils.isEmpty(mMeetingVoice.getVoiceText())){
                        msg = R.string.delete_sound_record_and_word;
                    }else {
                        msg = R.string.delete_sound_record;
                    }
                    DialogHelper.showDialog(getContext(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(i == DialogInterface.BUTTON_NEGATIVE){
                                // do nothing
                            }else if(i == DialogInterface.BUTTON_POSITIVE){
                                deleteMeetingVoice();
                            }else {
                                // do nothing
                            }
                        }
                    },R.string.dialog_back_title,msg,R.string.Confirm,R.string.cancel);
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MeetingLog.d(TAG, "onDetachedFromWindow");
        if(isTextSaved == false){
            MeetingLog.d(TAG, "onDetachedFromWindow save data");
            saveAudioText(mTextView.getTextString());
        }
    }

    public SoundRecordItemView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        this(paramContext, paramAttributeSet);
    }

    public SoundRecordItemView(Context paramContext, AttributeSet paramAttributeSet, int paramInt1, int paramInt2) {
        this(paramContext, paramAttributeSet);
    }


    public void onClick(View paramView) {
        switch (paramView.getId()) {
            case R.id.voice_item_play_btn:
                MeetingLog.i(TAG,"click play button");
                play(mMeetingVoice.getVoicePath());
                break;
            case R.id.voice_item_pause_btn:
                MeetingLog.i(TAG,"click pause button");
                pause();
                break;
            case R.id.voice_item_stop_btn:
                MeetingLog.i(TAG,"click stop button");
                stop();
                break;
            case R.id.voice_item_toText_btn:
                MeetingLog.i(TAG,"click audio to text button");
                showAudioToTextMenu();
                break;
            case R.id.voice_item_toText_progress_btn:
                MeetingLog.i(TAG,"click audio to text progress button");
                OfflineToText.getInstance(getContext()).stopRecognize();
                break;
            case R.id.voice_item_audio_toText_expand_text:
                MeetingLog.i(TAG,"click audio to text expand textView");
                if(mTextView.isExpand){
                    editLoseFocus();
                    mTextView.collapse(null);
                }else{
                    editRequestFocus();
                    mTextView.expand(new ExpandEditTextView.OnAnimatorEndListener() {
                        @Override
                        public void onEnd() {
                            mTextView.setSelection(mTextView.getTextString().length());
                            MeetingLog.d(TAG,mMeetingVoice.getId() + " EditText finish expand");
                        }
                    });
                }
                break;
            case R.id.voice_item_text_delete_btn:
                MeetingLog.i(TAG,"click audio delete button");
                DialogHelper.showDialog(getContext(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if( i == DialogInterface.BUTTON_POSITIVE){
                            setMeetingVoiceText("");
                            mTextView.setText("");
                        }
                    }
                },R.string.dialog_back_title,R.string.delete_audio_to_text,R.string.Confirm,R.string.cancel);
                break;
            default:
                break;
        }
    }
    public void deleteMeetingVoice(){
        MeetingLog.i(TAG,"deleteMeetingVoice start");
        MeetingLog.i(TAG,"deleteMeetingVoice task before,voices list size is " + MeetingStaticInfo.getCurrentMeeting().getMeetingVoices().size());
        //delete pics
        String  dirPath = mMeetingVoice.getVoicePath();
        File file  = new File(dirPath);
        boolean isDelete = true;
        isDelete =  FileUtils.deleteDir(file);
        MeetingLog.d(TAG,"delete file " + file.getAbsolutePath() + " is " + isDelete);
        if (isDelete){
            MeetingVoiceDBUtil.delete(mMeetingVoice.getId(),getContext(),new MeetingVoiceDBUtil.OnDoneDeletedListener(){
                @Override
                public void postDeleted(boolean isSuccess) {
                    MeetingLog.i(TAG,"deleteMeetingVoice task end,result is " + isSuccess);
                    if (isSuccess){
                        MeetingStaticInfo.getCurrentMeeting().getMeetingVoices().remove(mMeetingVoice);
                        mParentViewAdapter.notifyDataSetChanged();
                    }
                    MeetingStaticInfo.updateCurrentTime(getContext());
                    MeetingLog.i(TAG,"deleteMeetingVoice task end,voices list size is " + MeetingStaticInfo.getCurrentMeeting().getMeetingVoices().size());
                }
            });
        }
        MeetingLog.i(TAG,"deleteMeetingVoice end");
    }

    public void setParentViewAdapter(ImportPointAdapter ParentViewAdapter) {
        mParentViewAdapter = ParentViewAdapter;
    }

    public void pause() {
        this.mService.pausePlay();
        mVoicePlayBtn.setVisibility(VISIBLE);
        mVoicePauseBtn.setVisibility(GONE);
    }

    public void play(final String paramString) {
        MeetingLog.i(TAG,"play " + paramString);
        mManager = PlayerServiceConnectionManager.getInstance();
        mPlayerService = mManager.getPlayerServiceConnection();
        if (mPlayerService == null || (mPlayerService != null && !mManager.getCurrentPlayFile().
                equals(mMeetingVoice.getVoicePath()))) {
            if(mManager.getService()!=null){
                mManager.getService().stopPlay();
            }
            if (mPlayerService != null) {
                try {
                    getContext().unbindService(mPlayerService);
                }catch (Exception e){
                    MeetingLog.e(TAG,"play unbind error " ,e);
                }

            }
            mPlayerService = new ServiceConnection() {
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    MeetingLog.d(TAG,"ServiceConnection connected ");
                    mService = ((PlayerBinder) iBinder).getService();
                    mBookmarkSeekBar.setMax(mService.getDuration());
                    mService.setOnRefreshUiListener(mOnRefreshUiListener);
                    mManager.setManagerInfo(mPlayerService,
                            mMeetingVoice.getVoicePath(),mService);
                    mService.startPlay();
                    registerReceiver();
                    goToPlay();
                }

                public void onServiceDisconnected(ComponentName paramAnonymousComponentName) {

                }
            };
            Intent localIntent = new Intent(getContext(), PlayerService.class);
            localIntent.putExtra("filename", paramString);
            getContext().bindService(localIntent, this.mPlayerService, Context.BIND_AUTO_CREATE);
        }else{
            restartPlay();
            mVoicePlayBtn.setVisibility(GONE);
            mVoicePauseBtn.setVisibility(VISIBLE);
        }

    }

    public void setMeetingVoice(MeetingVoice meetingVoice) {
        MeetingLog.d(TAG,"setMeetingVoice start voice path is " + meetingVoice.getVoicePath());
        mManager = PlayerServiceConnectionManager.getInstance();
        mMeetingVoice = meetingVoice;
        mService = mManager.getService();
        mPlayerService = mManager.getPlayerServiceConnection();
        File file = new File(meetingVoice.getVoicePath());
        String fileName =  file.getName();
        String name = fileName.substring(0,fileName.indexOf("."));
        mVoiceFileName.setText(name);
        mAudioToTextFileName.setText(name);
        if(mManager.getCurrentPlayFile().equals(meetingVoice.getVoicePath()) && null != mManager.getService()) {
            if(mManager.getService().getCurrentState() == PlayerService.STATE_PLAYING){
                mAudioProgressBarContainer.setVisibility(VISIBLE);
                mVoicePlayBtn.setVisibility(GONE);
                mVoiceToTextBtn.setVisibility(GONE);
                mVoiceStopBtn.setVisibility(VISIBLE);
                mVoicePauseBtn.setVisibility(VISIBLE);
                mBookmarkSeekBar.setBookmarks(mMeetingVoice.getDurationLong());
                mBookmarkSeekBar.setProgress(0);
                mBookmarkSeekBar.setMax(mManager.getService().getDuration());
                mManager.getService().setOnRefreshUiListener(mOnRefreshUiListener);
            }else if(mManager.getService().getCurrentState() == PlayerService.STATE_PAUSE_PLYING){
                mAudioProgressBarContainer.setVisibility(VISIBLE);
                mVoicePlayBtn.setVisibility(VISIBLE);
                mVoiceToTextBtn.setVisibility(GONE);
                mVoiceStopBtn.setVisibility(VISIBLE);
                mVoicePauseBtn.setVisibility(GONE);
                mBookmarkSeekBar.setBookmarks(mMeetingVoice.getDurationLong());
                mBookmarkSeekBar.setMax(mManager.getService().getDuration());
                mBookmarkSeekBar.setProgress((int) mManager.getService().getPlayedTime());
                mManager.getService().setOnRefreshUiListener(mOnRefreshUiListener);
            }else {
                goToIdle();
            }
        }else{
            goToIdle();
        }

        // if the audio file is not exists
        if(!file.exists()){
            goToIdle();
            mVoicePlayBtn.setEnabled(false);
            mVoiceFileName.setText(getContext().getText(R.string.no_file));
        }else{
            mVoicePlayBtn.setEnabled(true);
        }

        // init the status of textview
        setAudioToTextView(mMeetingVoice);

        mSliderView.setSwipeListener(null);

        if(mParentViewAdapter.containASlider(meetingVoice)){
            mSliderView.open(false);
            mParentViewAdapter.setmSliderView(mSliderView);
            MeetingLog.i(TAG,"mSliderView open  " + meetingVoice.getId());
        }else {
            mSliderView.close(false);
            MeetingLog.i(TAG,"mSliderView close " + meetingVoice.getId());
        }

        mSliderView.setSwipeListener(new SliderLayout.SimpleSwipeListener(){
            @Override
            public void onClosed(SliderLayout sliderLayout) {
                MeetingLog.d(TAG, "mSliderView onClosed " + mMeetingVoice.getId());
                if(mParentViewAdapter.getmSliderView() == mSliderView){
                    MeetingLog.d(TAG, "mSliderView setmSliderView null " + mMeetingVoice.getId());
                    mParentViewAdapter.setmSliderView(null);
                }
                setSliderStatus(false);
            }

            @Override
            public void onOpened(SliderLayout sliderLayout) {
                MeetingLog.d(TAG, "mSliderView onOpened " + mMeetingVoice.getId());
                if (mParentViewAdapter.getmSliderView() != null) {
                    mParentViewAdapter.getmSliderView().close(true);
                }
                mParentViewAdapter.setmSliderView(mSliderView);
                setSliderStatus(true);
                MeetingLog.d(TAG, "mSliderView onOpened " + mMeetingVoice.getId() + " end");
            }
        });
        MeetingLog.d(TAG,"setMeetingVoice end voice path is " + meetingVoice.getVoicePath());
    }

    public void setPlayerService(ServiceConnection paramServiceConnection) {
        this.mPlayerService = paramServiceConnection;
    }

    public void restartPlay(){
        if(mService != null){
            mService.startPlay();
        }
        registerReceiver();
    }

    public void stop() {
        mManager.setManagerInfo(mPlayerService,"",mService);
        if(mService!=null){
            mService.stopPlay();
        }
        unregisterReceiver();
        goToIdle();
    }

    private void goToIdle(){
        mAudioProgressBarContainer.setVisibility(GONE);
        mVoicePlayBtn.setVisibility(VISIBLE);
        mVoiceToTextBtn.setVisibility(VISIBLE);
        mVoiceStopBtn.setVisibility(GONE);
        mVoicePauseBtn.setVisibility(GONE);
        lockSlider(false);
    }

    private void goToPause(){
        mAudioProgressBarContainer.setVisibility(VISIBLE);
        mVoicePlayBtn.setVisibility(VISIBLE);
        mVoiceToTextBtn.setVisibility(GONE);
        mVoiceStopBtn.setVisibility(VISIBLE);
        mVoicePauseBtn.setVisibility(GONE);
        lockSlider(true);
    }

    private void goToPlay(){
        mAudioProgressBarContainer.setVisibility(VISIBLE);
        mVoicePlayBtn.setVisibility(GONE);
        mVoiceToTextBtn.setVisibility(GONE);
        mVoiceStopBtn.setVisibility(VISIBLE);
        mVoicePauseBtn.setVisibility(VISIBLE);
        mBookmarkSeekBar.setBookmarks(mMeetingVoice.getDurationLong());
        mBookmarkSeekBar.setProgress(0);
        lockSlider(true);
    }

    private void registerReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SoundRecordItemView.STOP_BROADCAST);
        getContext().registerReceiver(mSoundRecordBroadcastReceiver,intentFilter);
    }

    private void unregisterReceiver(){
        try {
            getContext().unregisterReceiver(mSoundRecordBroadcastReceiver);
        }catch (IllegalArgumentException e){
            MeetingLog.i(TAG,"Receiver not registered");
        }

    }

    /**
     * start to convert audio to text
     */
    private void startConvert(){
        OfflineToText offlineToText = OfflineToText.getInstance(getContext());
        if(!offlineToText.isIdle()){
            Toast.makeText(getContext(), R.string.audio_converting,Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(mMeetingVoice.getVoicePath());
        if(file.exists()){
            mAudioToTextContainer.setVisibility(VISIBLE);
            mAudioPlayContainer.setVisibility(GONE);

            offlineToText.setRecognize(file.getAbsolutePath(), new OfflineToText.OnStatusChangedListener() {
                @Override
                public void onStart() {
                    mAudioTextDeleteButton.setProgress(0);
                }

                @Override
                public void onResult(Bundle params, String result) {

                }

                @Override
                public void onFinish(String result) {
                    setMeetingVoiceText(result);
                }

                @Override
                public void onProgress(float value) {
                    mAudioTextDeleteButton.setProgress(value);
                    NumberFormat num = NumberFormat.getPercentInstance();
                    num.setMaximumIntegerDigits(3);
                    num.setMaximumFractionDigits(2);
                    String msg = getContext().getString(R.string.audio_converting_to_text) +
                            num.format(value/100) + "......";
                    mAudioToTextProgress.setText(msg);
                    MeetingLog.i(TAG,msg);
                }

                @Override
                public void onStopByUser() {
                    endConvertFail();
                }
            });
            offlineToText.startConvert();
            mAudioToTextProgress.setText(R.string.start_to_convert);
        }else {
            MeetingLog.i(TAG,"startConvert no file");
        }

    }

    /**
     * end converting audio to text
     */
    private void setMeetingVoiceText(String result){
        mMeetingVoice.setVoiceText(result);
        saveAudioText(result);
    }

    /**
     * stop converting audio to text
     */
    private void endConvertFail(){
        mAudioToTextContainer.setVisibility(GONE);
        mAudioPlayContainer.setVisibility(VISIBLE);
    }

    private void setAudioToTextView(MeetingVoice meetingVoice){
        MeetingLog.i(TAG,"setAudioToTextView start " + meetingVoice.getVoicePath());
        if(null == meetingVoice){
            MeetingLog.e(TAG,"setAudioToTextView",new NullPointerException("meetingVoice is null"));
            return;
        }
        String text = meetingVoice.getVoiceText();
        if(!TextUtils.isEmpty(text)){
            if(TextUtils.isEmpty(mTextView.getTextString())){
                mTextView.setText(text);
                MeetingLog.i(TAG,"ExpandEditTextView set audio to text " + text);
            }
            mTextContainer.setVisibility(VISIBLE);
        }else {
            mTextContainer.setVisibility(GONE);
            mVoiceToTextBtn.setVisibility(VISIBLE);
        }
        if(mParentViewAdapter.getExpandHelper().isExpand(mMeetingVoice)){
            mTextView.setExpand();
            mExpandText.setText(R.string.collapse);
        }else {
            mTextView.setCollapse();
            mExpandText.setText(R.string.expand);
        }
        MeetingLog.i(TAG,"setAudioToTextView end " + meetingVoice.getVoicePath());
    }

    public void rotateArrow(boolean isExpand) {
        if(isExpand){
            mExpandText.setText(R.string.collapse);
        }else {
            mExpandText.setText(R.string.expand);
        }
    }

    private void saveAudioText(final String text){
        // set this text is saved
        isTextSaved = true;
        if(isSaving){
            return;
        }
        mMeetingVoice.setVoiceText(text);
        isSaving = true;
        MeetingVoiceDBUtil.update(mMeetingVoice,getContext(), new OnDoneInsertAndUpdateListener() {
            @Override
            public void onDone(long id) {
                if(id > 0){
                    mMeetingVoice.setVoiceText(text);
                    mAudioToTextContainer.setVisibility(GONE);
                    mAudioPlayContainer.setVisibility(VISIBLE);
                    if(TextUtils.isEmpty(text)){
                        mVoiceToTextBtn.setVisibility(VISIBLE);
                    }else {
                        mVoiceToTextBtn.setVisibility(GONE);
                    }
                    mNoSaveLength = mNoSaveLength % SAVE_MAX_LENGTH;
                    MeetingLog.d(TAG,"endConvertSuccessful + updated");
                    setAudioToTextView(mMeetingVoice);
                }else{
                    MeetingLog.d(TAG,"endConvertSuccessful + update error");
                }
                isSaving = false;
            }
        });
        MeetingStaticInfo.updateCurrentTime(getContext());
    }

    /**
     * set the backup function for changing length of text
     */
    private void realTimeBackup(){
        mTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequenceq, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int oldCount, int newCount) {
                mNoSaveLength += Math.abs(newCount - oldCount) ;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(mNoSaveLength >= SAVE_MAX_LENGTH){
                    MeetingLog.i(TAG,"save voice text");
                    saveAudioText(mTextView.getTextString());
                }
            }
        });
    }

    private void initIflytekLybClient(){
        mIflytekLybClient = new IflytekLybClient(getContext());
    }

    private void showAudioToTextMenu(){
        PopupMenu popupMenu = new PopupMenu(mContext, mVoiceToTextBtn, Gravity.RIGHT);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.audio_to_text_offline:
                        startConvert();
                        break;
                    case R.id.audio_to_text_online:
                        long duration = FileUtils.getAudioDura(mMeetingVoice.getVoicePath());
                        File file = new File(mMeetingVoice.getVoicePath());
                        String fileName =  file.getName();
                        // build a json
                        String name = fileName.substring(0,fileName.indexOf("."));
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("{\"filePath\":\"").append(mMeetingVoice.getVoicePath()).append("\",\"duration\":\"").
                                append(duration).append("\",\"title\":\"").append(name).append("\",\"desc\":\"\"}");
                        String json = stringBuffer.toString();
                        mIflytekLybClient.startOrder(json);
                        break;
                    case R.id.audio_to_text_order_result:
                        mIflytekLybClient.getOrderList();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        if(!TextUtils.isEmpty(mMeetingVoice.getVoiceText())) {
            MeetingLog.i(TAG,"load menu 2 ");
            popupMenu.inflate(R.menu.audio_to_text_menu_two);
        }else {
            MeetingLog.i(TAG,"load menu 3 ");
            popupMenu.inflate(R.menu.audio_to_text_menu);
        }
        popupMenu.show();
    }

    public void editRequestFocus(){
        MeetingLog.d(TAG,mMeetingVoice.getId() + " start editRequestFocus");
        mTextView.setFocusable(true);
        mTextView.requestFocus();
        mTextView.setSelection(mTextView.getTextString().length());
        MeetingLog.d(TAG,mMeetingVoice.getId() + " end editRequestFocus");
    }

    public void editLoseFocus(){
        MeetingLog.d(TAG,mMeetingVoice.getId() + " start editLoseFocus");
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.requestFocus();
        MeetingLog.d(TAG,mMeetingVoice.getId() + " end editLoseFocus");
    }

    private void lockSlider(boolean lock){
        mSliderView.setLockDrag(lock);
        if(lock){
            mSliderView.close(true);
        }
    }

    public SliderView getSliderView(){
        return mSliderView;
    }

    private void setSliderStatus(boolean isSlider){
        if(isSlider){
            mParentViewAdapter.addASlider(mMeetingVoice);
        }else{
            mParentViewAdapter.removeASlider(mMeetingVoice);
        }
    }
}
