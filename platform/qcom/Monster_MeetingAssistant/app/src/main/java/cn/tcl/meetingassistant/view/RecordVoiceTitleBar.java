/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.bean.MeetingVoice;
import cn.tcl.meetingassistant.db.MeetingVoiceDBUtil;
import cn.tcl.meetingassistant.db.OnDoneInsertAndUpdateListener;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.services.RecordAbnormalState;
import cn.tcl.meetingassistant.services.SoundRecorderService;
import cn.tcl.meetingassistant.utils.CurrentTimeUtil;
import cn.tcl.meetingassistant.utils.FileUtils;
import cn.tcl.meetingassistant.utils.TimeFormatUtil;

/**
 * Created on 16-9-1.
 */
public class RecordVoiceTitleBar extends RelativeLayout implements View.OnClickListener,
        SoundRecorderService.OnRefreshTimeUiListener,SoundRecorderService.OnMobileStateChangeListener,
        SoundRecorderService.OnStateChangeListener {

    private final String TAG = RecordVoiceTitleBar.class.getSimpleName();

    private ImageButton mRecordVoicePauseBtn;
    private ImageButton mRecordVoiceStopBtn;
    private ImageButton mRecordVoiceMarkBtn;
    private TextView mRecordTimeView;
    private TimerView mMarkTimerVIew;
    private OnStopClickListener mOnStopClickListener;
    private ServiceConnection mAudioRecordService;
    private SoundRecorderService mRecorderService;
    private MeetingVoice mMeetingVoice;
    private Dialog mDialog;
    private OnStopRecordingListener mOnStopRecordingListener;

    public RecordVoiceTitleBar(Context context) {
        this(context,null);
    }

    public RecordVoiceTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater layoutInflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_voice_recorder_title, this, true);
        mRecordVoicePauseBtn = (ImageButton) view.findViewById(R.id.voice_record_pause_btn);
        mRecordVoiceStopBtn = (ImageButton) view.findViewById(R.id.voice_record_stop_btn);
        mRecordVoiceMarkBtn = (ImageButton) view.findViewById(R.id.voice_record_mark_btn);
        mRecordTimeView = (TextView) view.findViewById(R.id.voice_record_time_text);
        mMarkTimerVIew = (TimerView) view.findViewById(R.id.voice_record_mark_view);
        mMarkTimerVIew.setMarkAppearCenter(true);
        mRecordVoicePauseBtn.setOnClickListener(this);
        mRecordVoiceStopBtn.setOnClickListener(this);
        mRecordVoiceMarkBtn.setOnClickListener(this);
    }

    public RecordVoiceTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public RecordVoiceTitleBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs);
    }

    private void initAudioRecordService(SoundRecorderService service) {
        mRecorderService = service;
        mRecorderService.setOnRefreshTimeUiListener(this);
        mRecorderService.setOnMobileStateChangeListener(this);
        mRecorderService.setOnStateChangeListener(this);
    }

    private void startRecordAnimation(){
        ValueAnimator.AnimatorUpdateListener listener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float scale = (float) valueAnimator.getAnimatedValue();
                mRecordVoicePauseBtn.setScaleX(scale);
                mRecordVoicePauseBtn.setScaleY(scale);
            }
        };

        ValueAnimator scaleAnim1 = ValueAnimator.ofFloat(1f, 0.5f);
        scaleAnim1.addUpdateListener(listener);
        scaleAnim1.setInterpolator(new AccelerateDecelerateInterpolator());
        ValueAnimator scaleAnim2 = ValueAnimator.ofFloat(0.5f, 1f);
        scaleAnim2.addUpdateListener(listener);
        scaleAnim2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (mRecorderService.getState() == SoundRecorderService.STATE_IDLE || mRecorderService.getState() ==
                        SoundRecorderService.STATE_PAUSE_RECORDING) {
                    mRecordVoicePauseBtn.setImageResource(R.drawable.ic_record_voice_start);
                } else if (mRecorderService.getState() == SoundRecorderService.STATE_RECORDING) {
                    mRecordVoicePauseBtn.setImageResource(R.drawable.ic_record_pause);
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        scaleAnim2.setInterpolator(new DecelerateInterpolator());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleAnim1).before(scaleAnim2);
        animatorSet.setDuration(125);
        animatorSet.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.voice_record_pause_btn:
                startRecordAnimation();
                if(mRecorderService.getState() == SoundRecorderService.STATE_PAUSE_RECORDING){
                    mRecorderService.startRecord();
                    mRecordVoiceMarkBtn.setEnabled(true);
                    //mRecordVoicePauseBtn.setImageResource(R.drawable.ic_record_pause);
                } else if(mRecorderService.getState() == SoundRecorderService.STATE_RECORDING) {
                    mRecorderService.pauseRecord();
                    mRecordVoiceMarkBtn.setEnabled(false);
                   // mRecordVoicePauseBtn.setImageResource(R.drawable.ic_record_voice_start);
                }else {
                    mRecorderService.startRecord();
                    mRecordVoiceMarkBtn.setEnabled(true);
                    //mRecordVoicePauseBtn.setImageResource(R.drawable.ic_record_pause);
                }
                break;
            case R.id.voice_record_stop_btn:
                showDialog(null);
                break;
            case R.id.voice_record_mark_btn:
                mMeetingVoice.addDurationMark(mRecorderService.getCurrentRecordTime());
                mMarkTimerVIew.addMark(mRecorderService.getCurrentRecordTime());
                break;
            default:
                break;
        }
    }

    public void addOnStopClickListener(OnStopClickListener onStopClickListener){
        mOnStopClickListener = onStopClickListener;
    }

    @Override
    public void onRefreshTimeUi(long time) {
        mRecordTimeView.setText(TimeFormatUtil.getHourMuniteSecondString(time/1000));
        mMarkTimerVIew.setDuration(time);
    }

    @Override
    public void onBatteryStateChange(int level) {

    }

    @Override
    public boolean onAvailableSizeChange(int size) {
        return false;
    }

    @Override
    public void onStateChange(int stateCode) {
        if(stateCode == SoundRecorderService.STATE_IDLE){
            MeetingVoiceDBUtil.insert(mMeetingVoice, getContext(), new OnDoneInsertAndUpdateListener() {
                @Override
                public void onDone(long id) {
                    File file = new File(mMeetingVoice.getVoicePath());
                    if(!file.exists()){
                        MeetingLog.e(TAG,"No meeting voice file");
                        MeetingToast.makeText(getContext(),R.string.save_voice_file_error,
                                MeetingToast.LENGTH_SHORT).show();
                        return;
                    }
                    if(id >= 0){
                        mMeetingVoice.setId(id);
                        MeetingStaticInfo.getCurrentMeeting().addMeetingVoice(mMeetingVoice);
                        MeetingLog.d(TAG,"Meeting voice insert success, id = " + id);
                        MeetingToast.makeText(getContext(),R.string.save_voice_file_successful,
                                MeetingToast.LENGTH_SHORT).show();
                    }else {
                        MeetingLog.e(TAG,"Meeting voice insert error");
                    }

                    if(null != mOnStopClickListener){
                        mOnStopClickListener.onStopClick();
                    }
                    if (mOnStopRecordingListener != null) {
                        mOnStopRecordingListener.onStopRecording();
                    }
                    MeetingStaticInfo.updateCurrentTime(getContext());
                }
            });
        }else if(stateCode == SoundRecorderService.STATE_PAUSE_RECORDING){
            startRecordAnimation();
        }
    }

    public interface OnStopClickListener{
        void onStopClick();
    }


    public interface OnStopRecordingListener{
        void onStopRecording();
    }

    private RecordAbnormalState mRecordAbnormalState;

    public void startRecordService(RecordAbnormalState recordAbnormalState){
        mRecordAbnormalState = recordAbnormalState;
        mRecordVoiceMarkBtn.setEnabled(true);
        mMeetingVoice = new MeetingVoice();
        mMarkTimerVIew.reset();
        mMeetingVoice.setMeetingId(MeetingStaticInfo.getCurrentMeeting().getId());
        String audioName = getContext().getString(R.string.audio) + CurrentTimeUtil.getCurrentTimeByMillisecond()+".wav";
        mMeetingVoice.setVoicePath(FileUtils.getVoiceFilePath(audioName));
        mMeetingVoice.setCreateTime(System.currentTimeMillis());
        mAudioRecordService = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                SoundRecorderService mService = ((SoundRecorderService.SoundRecorderBinder) service).getService();
                initAudioRecordService(mService);
                mRecorderService.setSaveRecordFile(mMeetingVoice.getVoicePath());
                mRecorderService.setRecordAbnormalState(mRecordAbnormalState);
                mRecorderService.startRecord();
                MeetingLog.d(TAG, "bind record service success");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent intent = new Intent(getContext(), SoundRecorderService.class);
        getContext().bindService(intent, mAudioRecordService, Context.BIND_AUTO_CREATE);
    }

    public void showDialog(OnStopRecordingListener listener){
        mOnStopRecordingListener = listener;
        stopRecord();
    }


    public int getRecordState() {
        return mRecorderService.getState();
    }

    public void stopRecord(){
        DialogHelper.showDialog(getContext(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == DialogInterface.BUTTON_POSITIVE) {
                    mRecorderService.stopRecord();
                    try {
                        getContext().unbindService(mAudioRecordService);
                    }catch (Exception e){
                        MeetingLog.e(TAG,"error",e);
                    }

                }
            }
        },R.string.save,R.string.stop_save_voice,R.string.Confirm,R.string.cancel);
    }

    public void stopRecordInAbnormal(){
        mRecorderService.stopRecord();
        if(mOnStopRecordingListener != null){
            mOnStopRecordingListener.onStopRecording();
        }
        getContext().unbindService(mAudioRecordService);
    }

    public boolean isRecording(){
        if(mRecorderService != null){
            return mRecorderService.getState() == SoundRecorderService.STATE_RECORDING;
        }else{
            return false;
        }
    }

}
