/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.meetingassistant.DecisionListActivity;
import cn.tcl.meetingassistant.EditDecisionActivity;
import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.bean.MeetingDecisionData;
import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.db.MeetingDecisionDBUtil;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.TimeFormatUtil;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-8.
 * Decision List page layout
 */
public class DecisionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MeetingDecisionData> mMeetingDecisionList;

    private OnDataChangedListener mOnDataChangedListener;

    private final static int DATA_NOT_CHANGED = 0;

    private final static int DATA_ADDED = 1;

    private final static int DATA_REMOVED = 2;

    private final static int DATA_INIT = 3;

    private int mDataChanged = DATA_NOT_CHANGED;

    private String TAG = DecisionListAdapter.class.getSimpleName();

    private Activity mContext;

    public DecisionListAdapter(Activity context) {
        mMeetingDecisionList = new ArrayList<>();
        this.mContext = context;
    }

    public void setMeetingDecisionList(List<MeetingDecisionData> mMeetingDecisionList) {
        this.mMeetingDecisionList = mMeetingDecisionList;
        mDataChanged = DATA_INIT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DecisionHolder holder = new DecisionHolder(LayoutInflater.from(
                parent.getContext()).inflate(R.layout.item_decision_layout, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder1, final int position) {
        final MeetingDecisionData decision = mMeetingDecisionList.get(position);
        DecisionHolder holder = (DecisionHolder) holder1;

        String title = holder.mTitle.getContext().getResources().
                getString(R.string.decision) + " " + (position + 1);

        holder.mTitle.setText(title);

        // set decisionInfo
        if (TextUtils.isEmpty(decision.getDecisionInfo())) {
            holder.mDecisionInfo.setText(R.string.not_filled);
        } else {
            holder.mDecisionInfo.setText(decision.getDecisionInfo());
        }
        // set decision person
        if (TextUtils.isEmpty(decision.getPersons())) {
            holder.mPersons.setText(R.string.not_filled);
        } else {
            holder.mPersons.setText(decision.getPersons());
        }
        // set decision deadline
        if (null == decision.getDeadline() || decision.getDeadline() ==0) {
            holder.mDeadline.setText(R.string.not_filled);
        } else {
            holder.mDeadline.setText(TimeFormatUtil.getDateTimeTimeString(decision.getDeadline()));
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                switch (view.getId()) {
                    case R.id.decision_item_delete_btn:
                        DialogHelper.showDialog(mContext, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == DialogInterface.BUTTON_POSITIVE){
                                    MeetingDecisionDBUtil.delete(decision.getId(), view.getContext(),
                                            new MeetingDecisionDBUtil.OnDoneDeletedListener() {
                                                @Override
                                                public void postDeleted() {
                                                    mMeetingDecisionList.remove(decision);
                                                    mDataChanged = DATA_REMOVED;
                                                    notifyDataChange();
                                                    MeetingStaticInfo.updateCurrentTime(mContext);
                                                }

                                                @Override
                                                public void failDeleted() {
                                                    MeetingLog.e("Delete Decision", "Fail");
                                                }
                                            });
                                }else {
                                    // todo nothing
                                }
                            }
                        },R.string.dialog_back_title,R.string.delete_meeting_decision,R.string.Confirm,R.string.cancel);
                        break;
                    case R.id.decision_item_modify_btn:
                        Intent intent = new Intent(mContext, EditDecisionActivity.class);
                        intent.putExtra(EditDecisionActivity.DECISION_ID,
                                mMeetingDecisionList.get(position).getId());
                        MeetingDecisionData decisionData = mMeetingDecisionList.get(position);
                        mContext.startActivityForResult(intent, DecisionListActivity.REQUEST_CODE);
                        break;
                }
            }
        };
        holder.mDeleteBtn.setOnClickListener(onClickListener);
        holder.mModifyBtn.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return mMeetingDecisionList.size();
    }

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.mOnDataChangedListener = listener;
    }

    public interface OnDataChangedListener {
        void onDataChanged(int itemNumber);
    }

    public void notifyDataChange() {
        notifyDataSetChanged();
        if (mDataChanged == DATA_NOT_CHANGED) {
            return;
        }
        if (mDataChanged == DATA_INIT) {
            mOnDataChangedListener.onDataChanged(mMeetingDecisionList.size());
        }
        if (mDataChanged == DATA_ADDED && mMeetingDecisionList.size() <= 2) {
            mOnDataChangedListener.onDataChanged(mMeetingDecisionList.size());
        }
        if (mDataChanged == DATA_REMOVED && mMeetingDecisionList.size() <= 1) {
            mOnDataChangedListener.onDataChanged(mMeetingDecisionList.size());
        }
        mDataChanged = DATA_NOT_CHANGED;
    }

    public static class DecisionHolder extends RecyclerView.ViewHolder {
        TextView mTitle;
        TextView mDecisionInfo;
        TextView mPersons;
        TextView mDeadline;
        ImageButton mDeleteBtn;
        ImageButton mModifyBtn;

        public DecisionHolder(View view) {
            super(view);
            mTitle = (TextView) view.findViewById(R.id.decision_item_title);
            mDecisionInfo = (TextView) view.findViewById(R.id.decision_item_info);
            mPersons = (TextView) view.findViewById(R.id.decision_item_person);
            mDeadline = (TextView) view.findViewById(R.id.decision_item_deadline);
            mDeleteBtn = (ImageButton) view.findViewById(R.id.decision_item_delete_btn);
            mModifyBtn = (ImageButton) view.findViewById(R.id.decision_item_modify_btn);
        }
    }

}




