/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import cn.tcl.meetingassistant.utils.TimeFormatUtil;
import mst.widget.MstRecyclerView;
import mst.widget.SliderLayout;
import mst.widget.recycleview.LinearLayoutManager;
import mst.widget.recycleview.RecyclerView;
import cn.tcl.meetingassistant.utils.PermissionUtil;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.FloatingActionButton;
import mst.widget.MstCheckRecyclerAdapter;
import mst.widget.SliderView;
import mst.widget.toolbar.Toolbar;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.MstSearchView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cn.tcl.meetingassistant.bean.ImportPoint;
import cn.tcl.meetingassistant.bean.Meeting;
import cn.tcl.meetingassistant.bean.MeetingInfo;
import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.bean.MeetingVoice;
import cn.tcl.meetingassistant.db.AsyncQueryAll;
import cn.tcl.meetingassistant.db.MeetingInfoDBUtil;
import cn.tcl.meetingassistant.db.OnDoneInsertAndUpdateListener;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.CurrentTimeUtil;
import cn.tcl.meetingassistant.utils.FileUtils;
import cn.tcl.meetingassistant.utils.SearchResultSpanUtil;
import cn.tcl.meetingassistant.view.AbsMeetingActivity;
import cn.tcl.meetingassistant.view.DialogHelper;
import cn.tcl.meetingassistant.view.NotificationHelper;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * The meeting list page.This is the launch page of app.
 */
public class MeetingListActivity extends AbsMeetingActivity {

    private MstSearchView mSearchView;
    private static final String TAG = MeetingListActivity.class.getSimpleName();

    private MstRecyclerView mListView;
    private FloatingActionButton mAddBtn;
    private Toolbar mToolbar;
    private View mNoSearchResultContainer;
    private View mNoContentContainer;

    private MeetingSearchAdapter mMeetingSearchAdapter;
    private MeetingAdapter mMeetingAdapter;

    private boolean isSearching = false;
    private Drawable mToolBarIcon;

    // used to multi choose
    private BottomNavigationView mBottombar;
    private Set<Meeting> mChooseList = new HashSet<>();
    private boolean isMultiMode = false;

    private Set<Meeting> mSlideStatus;
    private List<Meeting> mMeetings;
    private Context mContext;
    private boolean isSilding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setMstContentView(R.layout.activity_meeting_list_layout);

        initToolBar();

        mListView = (MstRecyclerView) findViewById(R.id.meeting_list_recycler_view);
        mAddBtn = (FloatingActionButton) findViewById(R.id.meeting_list_add_btn);
        mNoContentContainer = findViewById(R.id.meeting_list_no_content_container);
        mNoSearchResultContainer = findViewById(R.id.meeting_list_no_search_result_container);
        initBottomNavigate();
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mMeetingAdapter = new MeetingAdapter();
        mMeetingSearchAdapter = new MeetingSearchAdapter(this);
        mListView.setAdapter(mMeetingAdapter);
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new meeting and switch page
                createNewMeetingAndSwitchPage();
            }
        });
        //print App info
        MeetingLog.appInfo(this);
        MeetingLog.i(TAG, "onCreate");
        queryAllMeeting();

        mSlideStatus = new HashSet<>();
    }

    @Override
    protected void onResume() {
        if (MeetingStaticInfo.getInstance().getMeetingList().size() > 0) {
            mListView.setVisibility(View.VISIBLE);
            mNoContentContainer.setVisibility(View.GONE);
            Collections.sort(MeetingStaticInfo.getInstance().getMeetingList(), new Comparator<Meeting>() {
                @Override
                public int compare(Meeting meeting, Meeting meeting2) {
                    int result = 0;
                    if (meeting2.getMeetingInfo().getUpdateTime() > meeting.getMeetingInfo().getUpdateTime()) {
                        result = 1;
                    } else {
                        result = -1;
                    }
                    return result;
                }
            });
            mMeetingAdapter.setList(MeetingStaticInfo.getInstance().getMeetingList());
        } else {
            mListView.setVisibility(View.GONE);
            mNoContentContainer.setVisibility(View.VISIBLE);
        }
//        if (!isMultiMode && !isSearching) {
//            gotoListMode();
//        }else{
//            goToMultiChoose();
//        }
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionUtil.requestRecordPermission(this);
    }

    private void initBottomNavigate() {
        mBottombar = (BottomNavigationView) findViewById(R.id.meeting_list_bottom_navigation);
        mBottombar.inflateMenu(R.menu.multi_mode_menu);
        mBottombar.setNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.multi_mode_delete_menu) {
                    if (!isMultiMode) {
                        return false;
                    } else {
                        if (mChooseList == null || mChooseList.size() == 0) {
                            return false;
                        }
                        DialogHelper.showDialog(MeetingListActivity.this, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == DialogInterface.BUTTON_POSITIVE) {
                                    deleteMeetingList(mChooseList);
                                }
                            }
                        }, R.string.dialog_back_title, R.string.delete_all_meeting_msg, R.string.Confirm, R.string.cancel);
                    }
                    return false;
                }
                return false;
            }
        });
    }

    public boolean initToolBar() {
        mToolbar = getToolbar();
        mToolbar.setTitle(R.string.MeetingAssistant);
        mToolBarIcon = mToolbar.getNavigationIcon();
        inflateToolbarMenu(R.menu.menu_main);
        mSearchView = (MstSearchView) findViewById(R.id.ab_search);
        mSearchView.needHintIcon(false);
        mSearchView.setQueryHint(getString(R.string.home_search_hint));
        mToolbar.setNavigationIcon(null);

        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mNoContentContainer.setVisibility(View.GONE);
                }
            }
        });

        mSearchView.setOnQueryTextListener(new MstSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                MeetingLog.i(TAG, "search " + newText);
                List<Meeting> list;
                if (TextUtils.isEmpty(newText)) {
                    mMeetingSearchAdapter.setList(new ArrayList<Meeting>());
                    mMeetingSearchAdapter.setSearchString(newText);
                    mListView.setAdapter(mMeetingSearchAdapter);
                    mMeetingSearchAdapter.notifyDataSetChanged();
                    mNoSearchResultContainer.setVisibility(View.INVISIBLE);
                } else {
                    list = new ArrayList<>();
                    for (Meeting meeting : MeetingStaticInfo.getInstance().getMeetingList()) {
                        if (meeting.search(newText, MeetingListActivity.this).isSucceed)
                            list.add(meeting);
                    }
                    if (list.size() == 0) {
                        mNoSearchResultContainer.setVisibility(View.VISIBLE);
                    } else {
                        mNoSearchResultContainer.setVisibility(View.INVISIBLE);
                    }
                    mMeetingSearchAdapter.setList(list);
                    mMeetingSearchAdapter.setSearchString(newText);
                    mListView.setAdapter(mMeetingSearchAdapter);
                    mMeetingSearchAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });

        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoSearchMode();
            }
        });

        mSearchView.setOnCloseListener(new MstSearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                closeSearchView();
                return gotoListMode();
            }
        });

        setupActionModeWithDecor(mToolbar);
        setActionModeListener(new ActionModeListener() {
            @Override
            public void onActionItemClicked(ActionMode.Item item) {
                switch (item.getItemId()) {
                    case ActionMode.NAGATIVE_BUTTON:
                        gotoListMode();
                        closeSearchView();
                        break;
                    case ActionMode.POSITIVE_BUTTON:
                        changeAllCheck();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onActionModeShow(ActionMode actionMode) {

            }

            @Override
            public void onActionModeDismiss(ActionMode actionMode) {

            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mMeetingAdapter.isChecked() || isSearching) {
            closeSearchView();
            gotoListMode();
        }else {
            super.onBackPressed();
        }
//        if(isSearching){
//            closeSearchView();
//        }else if(isMultiMode){
//            gotoListMode();
//        } else{
//            super.onBackPressed();
//        }
    }

    private void closeSearchView() {
        if(isSearching){
            isSearching = false;
            mToolbar.setNavigationIcon(null);
            if (!mSearchView.isIconified()) {
                mSearchView.setIconified(true);
                if (!mSearchView.isIconified()) {
                    mSearchView.setIconified(true);
                }
            }
        }
    }


    private void queryAllMeeting() {
        MeetingLog.i(TAG, "queryAllMeeting");
        AsyncQueryAll asyncQueryAll = new AsyncQueryAll(this, new AsyncQueryAll.CallBack() {
            @Override
            public void onPreExecute() {
                MeetingLog.i(TAG, "query all meeting onPreExecute");
            }

            @Override
            public void onPostExecute(List<Meeting> meetings) {
                MeetingLog.i(TAG, "query all meeting onPostExecute");
                MeetingStaticInfo.getInstance().setMeetingList(meetings);
                mMeetings = meetings;
                mMeetingAdapter.setList(meetings);
                initContentViewArea(meetings.size());
            }
        });
        asyncQueryAll.execute();
    }

    private void initContentViewArea(int dataSize) {
        if (mNoContentContainer == null) return;

        if (dataSize > 0) {
            mListView.setVisibility(View.VISIBLE);
            mNoContentContainer.setVisibility(View.INVISIBLE);
        } else {
            mNoContentContainer.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Copyright (C) 2016 Tcl Corporation Limited
     * <p>
     * Created on 16-8-3.
     * The list adapter of recyclerView for this page
     */
    private class MeetingAdapter extends MstCheckRecyclerAdapter {
        private List<Meeting> mMeetings = new ArrayList<>();
        private final int MEETING_ITEM_TYPE = 0;
        private final int MEETING_BLANK_TYPE = 1;
        private SliderView mCurrentSlider;
        private int longClickPosition = -1;

        private void setList(List<Meeting> list) {
            mMeetings = list;
            notifyDataSetChanged();
        }

        MeetingAdapter() {

        }

        @Override
        protected View getCheckBox(int position, RecyclerView.ViewHolder viewHolder) {
            View view = ((SliderMeetingItemViewHolder) viewHolder).mMultiBox;
            MeetingLog.i(TAG, "getCheckBox " + view);
            return view;
        }

        @Override
        protected View getMoveView(int position, RecyclerView.ViewHolder viewHolder) {
            View view = ((SliderMeetingItemViewHolder) viewHolder).mMoveContainer;
            MeetingLog.i(TAG, "getMoveView " + view);
            return view;
        }


        @Override
        public int getItemViewType(int position) {
            if (position == mMeetings.size()) {
                return MEETING_BLANK_TYPE;
            } else {
                return MEETING_ITEM_TYPE;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            super.onCreateViewHolder(parent, viewType);
            switch (viewType) {
                case MEETING_ITEM_TYPE:
                    return new SliderMeetingItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting_slider_layout, parent, false));
                case MEETING_BLANK_TYPE:
                    return new BlankItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting_layout, parent, false));
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
            // item type
            if (viewHolder instanceof SliderMeetingItemViewHolder) {
                super.onBindViewHolder(viewHolder, position);
                ((SliderMeetingItemViewHolder) viewHolder).mSliderView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       MeetingLog.i(TAG, "onclick item position = " + position);
                        if (isChecked()) {
                            ((SliderMeetingItemViewHolder) viewHolder).mMultiBox.setChecked(!((SliderMeetingItemViewHolder) viewHolder).mMultiBox.isChecked());
                        }
                    }
                });
                ((SliderMeetingItemViewHolder) viewHolder).mSliderView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (isSilding || isChecked()) {
                            if(isChecked()){
                                ((SliderMeetingItemViewHolder) viewHolder).mMultiBox.setChecked(!((SliderMeetingItemViewHolder) viewHolder).mMultiBox.isChecked());
                            }
                            return true;
                        }
                        addItemToChosenList(mMeetings.get(position));
                        ((SliderMeetingItemViewHolder) viewHolder).mMultiBox.setChecked(true);
                        MeetingLog.i(TAG, "onLongClick item position = " + position);
                        goToMultiChoose();
                        longClickPosition = position;
                        return true;
                    }
                });

                ((SliderMeetingItemViewHolder) viewHolder).mSliderView.setCustomBackground(SliderView.CUSTOM_BACKGROUND_RIPPLE);
                if (longClickPosition == position) {
                    ((SliderMeetingItemViewHolder) viewHolder).mMultiBox.setChecked(true);
                    longClickPosition = -1;
                }
                if (!isChecked()) {
                    ((SliderMeetingItemViewHolder) viewHolder).mMultiBox.setChecked(false);
                }

                SliderMeetingItemViewHolder meetingItemViewHolder = (SliderMeetingItemViewHolder) viewHolder;
                mMeetings = MeetingStaticInfo.getInstance().getMeetingList();
                final Meeting meeting = mMeetings.get(position);
                meetingItemViewHolder.mSubTitleViewLabel.setText(getString(R.string.meeting_time) + ":\b");
                if (meeting.getMeetingInfo().getStartTime() == null || meeting.getMeetingInfo().getStartTime() == 0) {
                    meetingItemViewHolder.mSubTitleView.setText(meetingItemViewHolder.mSubTitleView.getContext().getText(R.string.not_filled));
                } else {
                    meetingItemViewHolder.mSubTitleView.setText(TimeFormatUtil.getPointTimeString((meeting.getMeetingInfo().getStartTime())));
                }

                meetingItemViewHolder.mTitleView.setText(meeting.getMeetingInfo().getTitle());

                if (meeting.getMeetingVoices().size() > 0) {
                    meetingItemViewHolder.mSoundView.setVisibility(View.VISIBLE);
                } else {
                    meetingItemViewHolder.mSoundView.setVisibility(View.INVISIBLE);
                }
                meetingItemViewHolder.mSliderView.setLockDrag(isChecked());
//
                meetingItemViewHolder.mSliderView.setSwipeListener(new SliderLayout.SwipeListener() {
                    @Override
                    public void onClosed(SliderLayout view) {
                        isSilding = false;
                        MeetingLog.i(TAG, "mSliderView onClosed current slider num is " + mSlideStatus.size());
                    }

                    @Override
                    public void onOpened(SliderLayout view) {
                        isSilding = true;
                        MeetingLog.i(TAG, "mSliderView onOpened current slider num is " + mSlideStatus.size());
                    }

                    @Override
                    public void onSlide(SliderLayout sliderLayout, float v) {
                        isSilding = true;
                    }
                });


                /**
                 * multi choose mode
                 */
                if (isChecked()) {
                    meetingItemViewHolder.mMultiBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b) {
                                addItemToChosenList(meeting);
                            } else {
                                removeItemFromChosenList(meeting);
                            }
                            if (MeetingLog.DEBUG) {
                                for (Meeting meeting1 : mChooseList) {
                                    MeetingLog.i(TAG, "choose meeting " + meeting1);
                                }
                            }
                            refreshActionModeTitle();
                        }
                    });
                }

                meetingItemViewHolder.mSliderView.setOnSliderButtonClickListener(new SliderView.OnSliderButtonLickListener() {
                    @Override
                    public void onSliderButtonClick(int i, View view, ViewGroup viewGroup) {
                        if (i == 0) {
                            DialogHelper.showDialog(MeetingListActivity.this, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i == DialogInterface.BUTTON_NEGATIVE) {
                                        // do nothing
                                    } else if (i == DialogInterface.BUTTON_POSITIVE) {
                                        deleteMeeting(mMeetings.get(position));
                                        meetingItemViewHolder.mSliderView.close(false);
                                        isSilding = false;
                                    } else {
                                        // do nothing
                                    }
                                }
                            }, R.string.dialog_back_title, R.string.delete_meeting, R.string.Confirm, R.string.cancel);
                        }
                    }
                });

                meetingItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isMultiMode) {
                            meetingItemViewHolder.mMultiBox.setChecked(!meetingItemViewHolder.mMultiBox.isChecked());
                        } else {
                            Intent intent = new Intent(view.getContext(), EditImportPointActivity.class);
                            MeetingStaticInfo.setCurrentMeeting(meeting);
                            view.getContext().startActivity(intent);
                            MeetingLog.i(TAG, "click meeting:name =  " +
                                    meeting.getMeetingInfo().getTitle());
                        }
                    }
                });

                if(mChooseList.contains(mMeetings.get(position))){
                    meetingItemViewHolder.mMultiBox.setChecked(true);
                }else {
                    meetingItemViewHolder.mMultiBox.setChecked(false);
                }
            }
            // layout Params
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) viewHolder.itemView.getLayoutParams();
            if (position == 0) {
                layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.meeting_list_first_item_marginTop);
            } else {
                layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.meeting_list_item_marginTop);
            }
            viewHolder.itemView.setLayoutParams(layoutParams);
        }


        @Override
        public int getItemCount() {
            if (mMeetings != null) {
                return mMeetings.size() + 1;
            } else {
                return 0;
            }
        }

        @Override
        public void setChecked(boolean checked) {
            isMultiMode = checked;
            super.setChecked(checked);
        }
    }

    private void deleteMeetingList(Set<Meeting> list) {
        List<Meeting> tempList = new LinkedList<>();
        tempList.addAll(list);
        for (Meeting meeting : tempList) {
            deleteMeeting(meeting);
        }
        tempList.clear();
        refreshActionModeTitle();
    }

    private void deleteMeeting(Meeting meeting) {
        long meetingId = meeting.getId();
        List<ImportPoint> importPoints = meeting.getImportPoints();
        if (importPoints.size() > 0) {
            for (ImportPoint importPoint : importPoints) {
                String dirPath = FileUtils.getImageDirPath() + importPoint.getCreatTime();
                File file = new File(dirPath);
                FileUtils.deleteDir(file);
            }
        }

        List<MeetingVoice> meetingVoiceList = meeting.getMeetingVoices();
        if (meetingVoiceList.size() > 0) {
            for (MeetingVoice meetingVoice : meetingVoiceList) {
                String dirPath = meetingVoice.getVoicePath();
                File file = new File(dirPath);
                FileUtils.deleteDir(file);
            }
        }
        MeetingStaticInfo.removeMeeting(meeting);
        if (mChooseList.contains(meeting)) {
            boolean result = mChooseList.remove(meeting);
            MeetingLog.i(TAG, "delete meeting " + meeting.getId() + " remove from chosen list " + result);
        }
        MeetingInfoDBUtil.delete(meetingId, MeetingListActivity.this, new MeetingInfoDBUtil.OnDoneDeletedListener() {
            @Override
            public void onDeleted(boolean isSuccess) {
                if (isSuccess) {
                    if (mMeetingAdapter.getItemCount() == 1) {
                        mNoContentContainer.setVisibility(View.VISIBLE);
                    }
                    mMeetingAdapter.notifyDataSetChanged();
                    refreshActionModeTitle();
                }
            }
        });
        mMeetingAdapter.notifyDataSetChanged();
    }

    private void addItemToChosenList(Meeting meeting){
        if (!mChooseList.contains(meeting)) {
            mChooseList.add(meeting);
            refreshActionModeTitle();
            MeetingLog.d(TAG, "onCheckedChanged add meeting to choose list " + meeting);
        }
    }

    private void removeItemFromChosenList(Meeting meeting){
        mChooseList.remove(meeting);
        refreshActionModeTitle();
        MeetingLog.d(TAG, "onCheckedChanged remove meeting from list " + meeting);
    }

    private class SliderMeetingItemViewHolder extends RecyclerView.ViewHolder {
        SliderView mSliderView;
        TextView mSubTitleViewLabel;
        TextView mTitleView;
        TextView mSubTitleView;
        ImageView mSoundView;
        View mItemView;
        CheckBox mMultiBox;
        View mMoveContainer;

        public SliderMeetingItemViewHolder(View itemView) {
            super(itemView);
            this.mItemView = itemView;
            mSliderView = (SliderView) mItemView;
            mSliderView.addTextButton(0, mSliderView.getContext().getString(R.string.delete));
            mSubTitleViewLabel = (TextView) mItemView.findViewById(R.id.item_meeting_detail_label);
            mTitleView = (TextView) mItemView.findViewById(R.id.item_meeting_title);
            mSubTitleView = (TextView) mItemView.findViewById(R.id.item_meeting_detail);
            mSoundView = (ImageView) mItemView.findViewById(R.id.item_meeting_voice);
            mMultiBox = (CheckBox) mItemView.findViewById(R.id.item_meeting_checkbox);
            mMoveContainer = mItemView.findViewById(R.id.item_content_layout_container);
        }

        public void setMultiMode(boolean isMultiMode) {
            mMultiBox.clearAnimation();
            if (isMultiMode) {
                mMultiBox.setVisibility(View.VISIBLE);
                mSliderView.setLockDrag(true);
                MeetingLog.i(TAG, "meetingItemViewHolder.mMultiBox VISIBLE " + mMultiBox);
            } else {
                mMultiBox.setVisibility(View.GONE);
                mSliderView.setLockDrag(false);
                MeetingLog.i(TAG, "meetingItemViewHolder.mMultiBox GONE    " + mMultiBox);
            }
        }
    }

    private static class BlankItemViewHolder extends RecyclerView.ViewHolder {
        TextView mSubTitleViewLabel;
        TextView mTitleView;
        TextView mSubTitleView;
        ImageView mSoundView;
        View mItemView;

        public BlankItemViewHolder(View itemView) {
            super(itemView);
            this.mItemView = itemView;
            mSubTitleViewLabel = (TextView) itemView.findViewById(R.id.item_meeting_detail_label);
            mTitleView = (TextView) itemView.findViewById(R.id.item_meeting_title);
            mSubTitleView = (TextView) itemView.findViewById(R.id.item_meeting_detail);
            mSoundView = (ImageView) itemView.findViewById(R.id.item_meeting_voice);
            mItemView.setVisibility(View.INVISIBLE);
        }
    }

    private void createNewMeetingAndSwitchPage() {
        final MeetingInfo mMeetingInfo = new MeetingInfo();
        mMeetingInfo.setUpdateTime(System.currentTimeMillis());
        String meetingName = getString(R.string.meeting) + CurrentTimeUtil.getCurrentTimeByMillisecond();
        mMeetingInfo.setTitle(meetingName);
        MeetingInfoDBUtil.insert(mMeetingInfo, this, new OnDoneInsertAndUpdateListener() {
            @Override
            public void onDone(long id) {
                mMeetingInfo.setId(id);
                Meeting meeting = new Meeting();
                meeting.setMeetingInfo(mMeetingInfo);
                MeetingStaticInfo.setCurrentMeeting(meeting);
                MeetingStaticInfo.addMeeting(meeting);
                MeetingLog.i(TAG, "insert meeting:name =  " + mMeetingInfo.getTitle());
                Intent intent = new Intent(MeetingListActivity.this, EditImportPointActivity.class);
                startActivity(intent);
                MeetingLog.i(TAG, "start activity " + EditImportPointActivity.class.getSimpleName());
            }
        });
    }


    public class MeetingSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Meeting> mMeetings = new ArrayList<>();
        private final int MEETING_ITEM_TYPE = 0;
        private final int MEETING_BLANK_TYPE = 1;
        private String mSearchString;
        private Context mContext;

        MeetingSearchAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == mMeetings.size()) {
                return MEETING_BLANK_TYPE;
            } else {
                return MEETING_ITEM_TYPE;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case MEETING_ITEM_TYPE:
                    return new SliderMeetingItemViewHolder(LayoutInflater.from(
                            parent.getContext()).inflate(R.layout.item_meeting_slider_layout, parent,
                            false));
                case MEETING_BLANK_TYPE:
                    return new BlankItemViewHolder(LayoutInflater.from(
                            parent.getContext()).inflate(R.layout.item_meeting_layout, parent,
                            false));
                default:
                    return null;
            }
        }

        private void setList(List<Meeting> list) {
            mMeetings.clear();
            mMeetings.addAll(list);
        }

        private void setSearchString(String searchString) {
            mSearchString = searchString;
        }


        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            // layout Params
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            if (position == 0) {
                layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.meeting_list_first_item_marginTop);
            } else {
                layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.meeting_list_item_marginTop);
            }
            holder.itemView.setLayoutParams(layoutParams);

            if (holder instanceof SliderMeetingItemViewHolder && position < mMeetings.size()) {

                SliderMeetingItemViewHolder meetingItemViewHolder = (SliderMeetingItemViewHolder) holder;
                meetingItemViewHolder.mSliderView.setLockDrag(true);

                Meeting meeting = mMeetings.get(position);

                meetingItemViewHolder.mMultiBox.setVisibility(View.GONE);

                Meeting.SearchResult result = meeting.getSearchResult();

                // set item sub title
                if (TextUtils.isEmpty(result.mResultTitle)) {
                    meetingItemViewHolder.mSubTitleViewLabel.setText(mContext.getText(R.string.not_filled));
                } else {
                    SearchResultSpanUtil.setSpan(result.mResultTitle + ":\b", null, meetingItemViewHolder.mSubTitleViewLabel,
                            R.color.search_result_subtitl_color);
                }

                // set item sub string
                if (TextUtils.isEmpty(result.mResultString)) {
                    meetingItemViewHolder.mSubTitleView.setText(mContext.getText(R.string.not_filled));
                } else {
                    SearchResultSpanUtil.setSpan(result.mResultString, mSearchString, meetingItemViewHolder.mSubTitleView,
                            R.color.search_result_substring_color);
                }

                String title = mMeetings.get(position).getMeetingInfo().getTitle();
                if (title.contains(mSearchString)) {
                    SearchResultSpanUtil.setSpan(title, mSearchString, meetingItemViewHolder.mTitleView,
                            R.color.search_result_title_color);
                } else {
                    meetingItemViewHolder.mTitleView.setText(title);
                }
                if (meeting.getMeetingVoices().size() > 0) {
                    meetingItemViewHolder.mSoundView.setVisibility(View.VISIBLE);
                } else {
                    meetingItemViewHolder.mSoundView.setVisibility(View.INVISIBLE);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), EditImportPointActivity.class);
                        MeetingStaticInfo.setCurrentMeeting(mMeetings.get(position));
                        view.getContext().startActivity(intent);
                        MeetingLog.i(TAG, "click meeting:name =  " +
                                mMeetings.get(position).getMeetingInfo().getTitle());
                    }
                });
            }


        }

        @Override
        public int getItemCount() {
            if (mMeetings != null) {
                return mMeetings.size() + 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    protected void onDestroy() {
        MeetingLog.d(TAG, "onDestroy");
        NotificationHelper.cancelNotification(this);
        super.onDestroy();
    }

    private void goToMultiChoose() {
        MeetingLog.d(TAG, "start goToMultiChoose");
        if(!mMeetingAdapter.isChecked()){
            mMeetingAdapter.setChecked(true);
            refreshActionModeTitle();

        }
        isMultiMode = true;
        mBottombar.setVisibility(View.VISIBLE);
        mToolbar.setVisibility(View.INVISIBLE);
        mAddBtn.setVisibility(View.GONE);
        showActionMode(true);
        mMeetingAdapter.notifyDataSetChanged();
        MeetingLog.d(TAG, "end goToMultiChoose");
    }

    private void gotoSearchMode() {
        MeetingLog.i(TAG, "start gotoSearchMode");
        mMeetingSearchAdapter.setList(new ArrayList<Meeting>());
        mMeetingSearchAdapter.setSearchString("");
        mListView.setAdapter(mMeetingSearchAdapter);
        mMeetingSearchAdapter.notifyDataSetChanged();
        mAddBtn.setVisibility(View.GONE);
        isSearching = true;
        mNoContentContainer.setVisibility(View.INVISIBLE);
        mToolbar.setNavigationOnClickListener(view1 -> closeSearchView());
        mToolbar.setNavigationIcon(mToolBarIcon);
        MeetingLog.i(TAG, "end gotoSearchMode");
    }

    private boolean gotoListMode() {
        MeetingLog.i(TAG, "start gotoListMode");
        if(mMeetingAdapter.isChecked()){
            mMeetingAdapter.setChecked(false);
        }
        if (mChooseList != null) {
            mChooseList.clear();
        }
        mToolbar.setVisibility(View.VISIBLE);
        mSearchView.setVisibility(View.VISIBLE);
        mAddBtn.setVisibility(View.VISIBLE);
        mBottombar.setVisibility(View.GONE);
        mListView.setAdapter(mMeetingAdapter);
        if (mMeetingAdapter.getItemCount() == 1) {
            mNoContentContainer.setVisibility(View.VISIBLE);
        }
        mNoSearchResultContainer.setVisibility(View.GONE);
        showActionMode(false);
        mMeetingAdapter.notifyDataSetChanged();
        MeetingLog.i(TAG, "end gotoListMode");
        return false;
    }

    private void changeAllCheck() {
        List<Meeting> allMeeting = MeetingStaticInfo.getInstance().getMeetingList();
        boolean chosenAll = mChooseList.containsAll(allMeeting);
        if (chosenAll) {
            mChooseList.removeAll(allMeeting);
        } else {
            mChooseList.addAll(allMeeting);
        }
        mMeetingAdapter.notifyDataSetChanged();
        refreshActionModeTitle();
    }

    private void refreshActionModeTitle(){
        List<Meeting> allMeeting = MeetingStaticInfo.getInstance().getMeetingList();
        boolean chosenAll = mChooseList.containsAll(allMeeting);
        if (!chosenAll) {
            getActionMode().setPositiveText(getResources().getString(R.string.choose_all));
        } else {
            getActionMode().setPositiveText(getResources().getString(R.string.choose_others));
        }
        getActionMode().setTitle(String.format(getString(R.string.meeting_chosen_number),mChooseList.size()));
        refreshBottomBar();
    }

    private void refreshBottomBar() {
        if (null != mChooseList && mChooseList.size() > 0) {
            mBottombar.setItemEnable(R.id.multi_mode_delete_menu, true);
        } else {
            mBottombar.setItemEnable(R.id.multi_mode_delete_menu, false);
        }
    }

}
