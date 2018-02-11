/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.content.ServiceConnection;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.tcl.meetingassistant.EditImportPointActivity;
import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.bean.ImportPoint;
import cn.tcl.meetingassistant.bean.Meeting;
import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.bean.MeetingVoice;
import cn.tcl.meetingassistant.db.ImportPointDBUtil;
import cn.tcl.meetingassistant.db.OnDoneInsertAndUpdateListener;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.services.PlayerService;
import cn.tcl.meetingassistant.utils.DensityUtil;
import mst.widget.SliderView;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-16.
 * the import point list adapter
 */
public class ImportPointAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = ImportPointAdapter.class.getSimpleName();
    private List<ImportPoint> mPointList = new ArrayList<>();
    private List<MeetingVoice> mVoiceList = new ArrayList<>();
    private Context mContext;
    private RecyclerView mRecyclerView;

    private ServiceConnection mPlayerServiceCnn;
    private PlayerService mPlayerService;
    private int mPlayingItemPosition;

    private ExpandHelper mExpandHelper;
    private SliderView mSliderView;
    private Set<Object> mSlideStatus = new HashSet<>();

    public ImportPointAdapter(Context context,RecyclerView recyclerView){
        mContext = context;
        mRecyclerView = recyclerView;
        mPlayingItemPosition = -1;
    }

    private final int ITEM_TITLE = 0;
    private final int ITEM_IMPORT = 1;
    private final int ITEM_VOICE = 2;
    private final int ITEM_LAST_BLANK = 3;
    private final int ITEM_EXPAND = 4;

    @Override
    public int getItemViewType(int position) {
        // this position is title
        if (position == 0) {
            return ITEM_TITLE;
        } else if (position > 0 && position <= mPointList.size()) { // this position is import card
            return ITEM_IMPORT;
        } else if(position == getItemCount() - 1){
            return ITEM_LAST_BLANK;
        }
        else{ // this position is voice
            return ITEM_VOICE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder holder;
        switch (viewType) {
            case ITEM_TITLE:
                holder = new ImportListTitleHolder(
                        new ImportPointListTitleView(mContext));
                break;
            case ITEM_IMPORT:
                holder = new ImportPointHolder(
                        new ImportPointItemViewSlider(mContext));
                break;
            case ITEM_VOICE:
                holder = new SoundRecordHolder(
                        new SoundRecordItemView(mContext));
                break;
            case ITEM_LAST_BLANK:
                holder = new BlankViewHolder(new View(mContext));
                break;
            default:
                holder = null;
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case ITEM_TITLE:
                ImportListTitleHolder titleHolder = (ImportListTitleHolder) holder;
                titleHolder.mImportPointListTitleView.setAddBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        createOneEmptyImportPointItem();
                    }
                });
                break;
            case ITEM_IMPORT:
                final ImportPointHolder importPointHolder = (ImportPointHolder) holder;
                // set background of import point item according by position
                if (null != mPointList && mPointList.size() >= position) {
                    importPointHolder.mImportPointItemView.setParentViewAdapter(this);
                    importPointHolder.mImportPointItemView.position = position;
                    importPointHolder.mImportPointItemView.setImportPoint(mPointList.get(position-1));
                    setViewBackgroundAccordingPosition(importPointHolder, position);
                    importPointHolder.mImportPointItemView.setActivity((EditImportPointActivity) mContext);
                } else {
                    importPointHolder.mImportPointItemView.setBackground(
                            ImportPointItemViewSlider.BACKGROUND.BACKGROUND_SINGLE);
                }
                // if the item is created new one
                if(position == mPointList.size() && isCreateNewImport){
                    isCreateNewImport = false;
                    importPointHolder.mImportPointItemView.setItemIsNew();
                    MeetingLog.i(TAG,"set new item focus");
                }
                break;
            case ITEM_VOICE:
                final SoundRecordHolder soundRecordHolder = (SoundRecordHolder) holder;
                soundRecordHolder.mSoundRecordItemView.setParentViewAdapter(this);
                // set background of import point item according by position
                if (null != mVoiceList && mVoiceList.size() >= position - mPointList.size()) {
                    soundRecordHolder.mSoundRecordItemView.setMeetingVoice(mVoiceList.get(
                            position - mPointList.size() -1));
                }

                // the last item of the list
                if(position == getItemCount() - 1){
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) soundRecordHolder.mSoundRecordItemView.getLayoutParams();
                    lp.setMargins(lp.leftMargin,lp.topMargin,lp.rightMargin,20);
                    soundRecordHolder.mSoundRecordItemView.setLayoutParams(lp);
                }
                break;
            default:
                // do nothing
                break;
        }
    }

    @Override
    public int getItemCount() {
        // count = title.size + pointList.size + voiceList.size
        return 2 + mPointList.size() + mVoiceList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        initData();
    }

    private void setViewBackgroundAccordingPosition(ImportPointHolder holder, int position) {
        if (mPointList.size() == 1 && position == 1) {
            holder.mImportPointItemView.setBackground(
                    ImportPointItemViewSlider.BACKGROUND.BACKGROUND_SINGLE);
        } else if (mPointList.size() > 1 && position == 1) {
            holder.mImportPointItemView.setBackground(
                    ImportPointItemViewSlider.BACKGROUND.BACKGROUND_TOP);
        } else if (mPointList.size() > 1 && position == mPointList.size()) {
            holder.mImportPointItemView.setBackground(
                    ImportPointItemViewSlider.BACKGROUND.BACKGROUND_BOTTOM);
        } else {
            holder.mImportPointItemView.setBackground(
                    ImportPointItemViewSlider.BACKGROUND.BACKGROUND_MID);
        }
    }

    public void setPointList(List<ImportPoint> pointList) {
        mPointList.clear();
        mPointList.addAll(pointList);
    }

    public void setVoiceList(List<MeetingVoice> voiceList) {
        mVoiceList = voiceList;
    }

    public ExpandHelper getExpandHelper() {
        if(mExpandHelper == null){
            mExpandHelper = new ExpandHelper();
        }
        return mExpandHelper;
    }

    public SliderView getmSliderView() {
        return mSliderView;
    }

    public void setmSliderView(SliderView mSliderView) {
        this.mSliderView = mSliderView;
    }

    public void addASlider(Object object){
        if(mSlideStatus.contains(object)){

        }else {
            mSlideStatus.clear();
            mSlideStatus.add(object);
        }
    }

    public void removeASlider(Object object){
        mSlideStatus.remove(object);
    }

    public boolean containASlider(Object object){
        return mSlideStatus.contains(object);
    }

    public static class ImportPointHolder extends RecyclerView.ViewHolder {

        public ImportPointItemViewSlider mImportPointItemView;

        public ImportPointHolder(View view) {
            super(view);
            if (view instanceof ImportPointItemViewSlider) {
                this.mImportPointItemView = (ImportPointItemViewSlider) view;

            }
        }
    }


    public static class ImportListTitleHolder extends RecyclerView.ViewHolder {

        public ImportPointListTitleView mImportPointListTitleView;

        public ImportListTitleHolder(View view) {
            super(view);
            setIsRecyclable(false);
            if (view instanceof ImportPointListTitleView) {
                this.mImportPointListTitleView = (ImportPointListTitleView) view;
            }
        }
    }

    public static class SoundRecordHolder extends RecyclerView.ViewHolder{
        public SoundRecordItemView mSoundRecordItemView;

        public SoundRecordHolder(View view) {
            super(view);
            setIsRecyclable(false);
            if (view instanceof SoundRecordItemView) {
                this.mSoundRecordItemView = (SoundRecordItemView) view;
            }
        }
    }

    public static class BlankViewHolder extends RecyclerView.ViewHolder{
        public View mView;

        public BlankViewHolder(View view) {
            super(view);
            mView = view;
            setIsRecyclable(false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.height = DensityUtil.dip2px(view.getContext(),10);
            mView.setLayoutParams(lp);
        }
    }

    private void initData(){
        mPointList = MeetingStaticInfo.getCurrentMeeting().getImportPoints();
        if(mPointList.size() == 0){
            createOneEmptyImportPointItem();
        }
        mVoiceList = MeetingStaticInfo.getCurrentMeeting().getMeetingVoices();
        notifyDataSetChanged();
    }


    private boolean isCreateNewImport = false;

    /**
     * create one import point item
     */
    private void createOneEmptyImportPointItem(){
        final ImportPoint importPoint = new ImportPoint();

        // set the meeting id
        importPoint.setMeetingId(MeetingStaticInfo.getCurrentMeeting().getId());
        importPoint.setCreatTime(System.currentTimeMillis());
        importPoint.setInfoContent("");

        // add the import point data in database
        ImportPointDBUtil.insert(importPoint, mContext, new OnDoneInsertAndUpdateListener() {
            @Override
            public void onDone(long id) {
                importPoint.setId(id);
                mPointList.add(importPoint);
                MeetingLog.d(TAG,"create a import point create time is" + importPoint.getCreatTime());
                isCreateNewImport = true;
                ImportPointAdapter.this.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(mPointList.size());
                MeetingStaticInfo.updateCurrentTime(mContext);
            }
        });
    }
}
