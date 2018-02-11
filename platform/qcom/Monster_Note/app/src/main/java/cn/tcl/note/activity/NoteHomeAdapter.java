/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

import cn.tcl.note.R;
import cn.tcl.note.data.NoData;
import cn.tcl.note.data.NoteHomeData;
import cn.tcl.note.db.DBData;
import cn.tcl.note.ui.DialogHelper;
import cn.tcl.note.util.FileUtils;
import cn.tcl.note.util.NoteLog;
import cn.tcl.note.util.SearchResultSpanUtil;
import mst.widget.SliderLayout;
import mst.widget.SliderView;

public class NoteHomeAdapter extends RecyclerView.Adapter {
    private String TAG = NoteHomeAdapter.class.getSimpleName();
    private final int TYPE_NORMAL = 1;
    private final int TYPE_NO = 2;
    private LinkedList<NoteHomeData> mAllData = new LinkedList<>();
    private LinkedList<NoteHomeData> mSearchData;
    private LinkedList<NoteHomeData> mShowData;
    private Context mContext;
    private String mSearchText;
    private String mDefaultImgStr;
    private String mDefaultAudioStr;
    private boolean iSSearch;
    private SliderView mCurrentSlider;

    public NoteHomeAdapter(Context context, Cursor cursor) {
        mContext = context;
        mDefaultImgStr = context.getResources().getString(R.string.home_default_line_img);
        mDefaultAudioStr = context.getResources().getString(R.string.home_default_line_audio);
        setHomeAdapter(cursor, true);
        mShowData = mAllData;
    }

    public void setHomeAdapter(Cursor cursor, Boolean isClose) {
        mAllData = new LinkedList<>();
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    long id = cursor.getInt(cursor.getColumnIndex(DBData.COLUMN_ID));
                    String firstLine = cursor.getString(cursor.getColumnIndex(DBData.COLUMN_FIRSTLINE));
                    String secondLine = cursor.getString(cursor.getColumnIndex(DBData.COLUMN_SECOND_LINE));
                    int willDo = cursor.getInt(cursor.getColumnIndex(DBData.COLUMN_WILL));
                    int imgNum = cursor.getInt(cursor.getColumnIndex(DBData.COLUMN_IMG));
                    int audioNum = cursor.getInt(cursor.getColumnIndex(DBData.COLUMN_AUDIO));
                    String time = cursor.getString(cursor.getColumnIndex(DBData.COLUMN_TIME));
                    NoteLog.d(TAG, "get data=" + "id = " + id + " first is " + firstLine + "  second is " + secondLine +
                            " willDo=" + willDo + " imgNum=" + imgNum + " audioNum=" + audioNum + "  time=" + time);
                    if (firstLine.equals("") && imgNum == 0 && audioNum == 0) {
                        FileUtils.deleteNote(mContext, id);
                        cursor.moveToNext();
                        continue;
                    }
                    mAllData.add(new NoteHomeData(id, firstLine, secondLine, willDo, imgNum, audioNum, time, mDefaultImgStr, mDefaultAudioStr));
                    cursor.moveToNext();
                }
            }
            if (mAllData.size() <= 0) {
                addDefaultData();
            }
        }
        if (isClose) {
            cursor.close();
        }
    }

    private void addDefaultData() {
        String str = mContext.getString(R.string.home_no_data);
        mAllData.add(new NoData(str));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NO) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_home_no_data, parent, false);
            return new NoDataHolder(itemView);
        }
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_file_list, parent, false);
        return new HomeItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((HomeItem) holder).init(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (mShowData.get(position) instanceof NoData) {
            return TYPE_NO;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return mShowData.size();
    }

    public void setSearchData(Cursor cursor) {
        iSSearch = true;
        mCurrentSlider = null;
        mShowData = new LinkedList<>();
        setSearchAdapter(cursor, true);
        notifyDataSetChanged();
    }

    public void setSearchAdapter(Cursor cursor, Boolean isClose) {
        mSearchData = new LinkedList<>();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                long id = cursor.getInt(cursor.getColumnIndex(DBData.COLUMN_ID));
                String firstLine = cursor.getString(cursor.getColumnIndex(DBData.COLUMN_FIRSTLINE));
                if (firstLine.length() > 0) {
                    String secondLine = cursor.getString(cursor.getColumnIndex(DBData.COLUMN_SECOND_LINE));
                    int willDo = cursor.getInt(cursor.getColumnIndex(DBData.COLUMN_WILL));
                    int imgNum = cursor.getInt(cursor.getColumnIndex(DBData.COLUMN_IMG));
                    int audioNum = cursor.getInt(cursor.getColumnIndex(DBData.COLUMN_AUDIO));
                    String time = cursor.getString(cursor.getColumnIndex(DBData.COLUMN_TIME));
                    String xmlStr = cursor.getString(cursor.getColumnIndex(DBData.COLUMN_XML));
                    NoteLog.d(TAG, "get data=" + "id = " + id + " first is " + firstLine + "  second is " + secondLine +
                            " willDo=" + willDo + " imgNum=" + imgNum + " audioNum=" + audioNum + "  time=" + time);
                    mSearchData.add(new NoteHomeData(id, firstLine, secondLine, willDo, imgNum, audioNum, time, mDefaultImgStr, mDefaultAudioStr, xmlStr));
                }
                cursor.moveToNext();
            }
        }
        if (isClose) {
            cursor.close();
        }
    }

    public void searchText(String text) {
        mSearchText = text;
        updateSearchView();
    }

    public void updateSearchView() {
        mShowData = new LinkedList<>();
        if (mSearchText.length() > 0) {
            if (mSearchData.size() > 0) {
                for (NoteHomeData noteHomeData : mSearchData) {
                    if (noteHomeData.searchResult(mSearchText)) {
                        mShowData.add(noteHomeData);
                    }
                }
            }
            if (mShowData.size() == 0) {
                String str = mContext.getString(R.string.search_no_data);
                mShowData.add(new NoData(str));
            }
        }
        notifyDataSetChanged();
    }

    public void closeSearch() {
        iSSearch = false;
        mShowData = mAllData;
        mSearchData = null;
        mSearchText = "";
        notifyDataSetChanged();
    }

    public void delItem() {
        final ArrayList<Long> deleAll = new ArrayList<>();
        ArrayList<NoteHomeData> delData = new ArrayList<>();
        for (NoteHomeData data : mAllData) {
            if (data.getCheck()) {
                deleAll.add(data.getId());
                delData.add(data);
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtils.deleteMoreNote(mContext, deleAll);
            }
        }).start();
        for (NoteHomeData data : delData) {
            mAllData.remove(data);
        }
        delData = null;
        if (mAllData.size() == 0) {
            addDefaultData();
        }
        notifyDataSetChanged();
    }

    public void checkAll(Boolean result) {
        for (NoteHomeData data : mAllData) {
            data.setCheck(result);
        }
        notifyDataSetChanged();
        ((NoteHomeActivity) mContext).setActionModeTitle(getCheckNum());
    }

    public boolean isCheck() {
        for (NoteHomeData data : mAllData) {
            if (data.getCheck()) {
                return true;
            }
        }
        return false;
    }

    private int getCheckNum() {
        int num = 0;
        for (NoteHomeData data : mAllData) {
            if (data.getCheck()) {
                num++;
            }
        }
        return num;
    }

    class HomeItem extends RecyclerView.ViewHolder {

        public HomeItem(View itemView) {
            super(itemView);
        }

        public void init(int position) {
        }
    }

    class HomeItemHolder extends HomeItem {
        private TextView mFirstLine;
        private TextView mSecondLine;
        private TextView mTime;
        private ImageView mWillImg;
        private ImageView mImgView;
        private ImageView mAudioImg;
        private CheckBox mDelCheck;
        private SliderView mSliderView;

        public HomeItemHolder(View itemView) {
            super(itemView);
            setIsRecyclable(false);
            mSliderView = (SliderView) itemView;
            mSliderView.addCustomButton(1, R.layout.swip_delete_button);
            mFirstLine = (TextView) itemView.findViewById(R.id.home_first_line);
            mSecondLine = (TextView) itemView.findViewById(R.id.home_second_line);
            mTime = (TextView) itemView.findViewById(R.id.home_time);
            mWillImg = (ImageView) itemView.findViewById(R.id.home_will);
            mImgView = (ImageView) itemView.findViewById(R.id.home_img);
            mAudioImg = (ImageView) itemView.findViewById(R.id.home_audio);
            mDelCheck = (CheckBox) itemView.findViewById(R.id.home_check_del);
        }

        @Override
        public void init(final int position) {
            if (position == 0) {
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) mSliderView.getLayoutParams();
                lp.topMargin = (int) mContext.getResources().getDimension(R.dimen.home_first_item_margin_top);
            }
            NoteLog.d(TAG, "init start position=" + position);
            mSliderView.setTag(position);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((NoteHomeActivity) mContext).getEditMode()) {
                        int position = getAdapterPosition();
                        long id = mShowData.get(position).getId();
                        ((NoteHomeActivity) mContext).goToEdit(id);
                    } else {
                        mAllData.get(position).setCheck();
                        checkAllUn();
                        ((NoteHomeActivity) mContext).setActionModeTitle(getCheckNum());
                        notifyItemChanged(position);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mCurrentSlider = null;
                    checkAll(false);
                    mAllData.get(position).setCheck(true);
                    ((NoteHomeActivity) mContext).enterEditMode(true);
                    ((NoteHomeActivity) mContext).setActionModeTitle(getCheckNum());
                    return false;
                }
            });

            mSliderView.setOnSliderButtonClickListener(new SliderView.OnSliderButtonLickListener() {
                @Override
                public void onSliderButtonClick(int i, View view, ViewGroup viewGroup) {
                    DialogHelper.showDialog(mContext, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                mCurrentSlider = null;
                                int position = getAdapterPosition();
                                long id = mShowData.get(position).getId();
                                NoteLog.d(TAG, "swip item id=" + id);
                                checkAll(false);
                                mAllData.get(position).setCheck(true);
                                delItem();
                            }
                        }
                    }, R.string.dialog_del_title, R.string.dialog_del_note_msg);

                }
            });

            mSliderView.setSwipeListener(new SliderLayout.SwipeListener() {
                @Override
                public void onClosed(SliderLayout sliderLayout) {
                    NoteLog.d(TAG, "onClosed " + position);
                    if (mCurrentSlider == mSliderView) {
                        mCurrentSlider = null;
                    }
                }

                @Override
                public void onOpened(SliderLayout sliderLayout) {
                    NoteLog.d(TAG, "onOpened " + position);
                    if (mCurrentSlider != null) {
                        NoteLog.d(TAG, "open close " + (int) mCurrentSlider.getTag());
                        mCurrentSlider.close(true);
                    }
                    mCurrentSlider = mSliderView;
                    NoteLog.d(TAG, "onOpened " + position + " end");
                }

                @Override
                public void onSlide(SliderLayout sliderLayout, float v) {
                    NoteLog.d(TAG, "onSlide " + position + " " + v);
                }
            });
            if (mCurrentSlider != null && position == (int) mCurrentSlider.getTag()) {
                if (mCurrentSlider.isOpened()) {
                    mSliderView.open(false);
                    mCurrentSlider = mSliderView;
                }
            }

            final NoteHomeData noteHomeData = mShowData.get(position);
            SearchResultSpanUtil.setSpan(noteHomeData.getFirstLine(), mSearchText, mFirstLine, R.color.search_color);
            SearchResultSpanUtil.setSpan(noteHomeData.getSecondLine(), mSearchText, mSecondLine, R.color.search_color);
            mTime.setText(noteHomeData.getmTime());
            mWillImg.setVisibility(noteHomeData.getWillNum() ? View.VISIBLE : View.GONE);
            mImgView.setVisibility(noteHomeData.getImgNum() ? View.VISIBLE : View.GONE);
            mAudioImg.setVisibility(noteHomeData.getAudioNum() ? View.VISIBLE : View.GONE);


            if (((NoteHomeActivity) mContext).getEditMode()) {
                //edit mode
                mDelCheck.setVisibility(View.VISIBLE);
                mSliderView.setLockDrag(true);
                itemView.setOnLongClickListener(null);
            } else {
                mDelCheck.setVisibility(View.GONE);
                mSliderView.setLockDrag(iSSearch);
            }

            mDelCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mAllData.get(getAdapterPosition()).setCheck(isChecked);
                    checkAllUn();
                    ((NoteHomeActivity) mContext).setActionModeTitle(getCheckNum());
                }
            });
            mDelCheck.setChecked(noteHomeData.getCheck());
            NoteLog.d(TAG, "init end position=" + position);
        }
    }

    private void checkAllUn() {
        int num = getCheckNum();
        if (num == 0) {
            ((NoteHomeActivity) mContext).changeAllCheck(true);
            ((NoteHomeActivity) mContext).setDelButtonStatus(false);
        } else if (num == mAllData.size()) {
            ((NoteHomeActivity) mContext).changeAllCheck(false);
            ((NoteHomeActivity) mContext).setDelButtonStatus(true);
        } else {
            ((NoteHomeActivity) mContext).setDelButtonStatus(true);
            ((NoteHomeActivity) mContext).changeAllCheck(true);
        }
    }

    class NoDataHolder extends HomeItem {
        private TextView mNoText;

        public NoDataHolder(View itemView) {
            super(itemView);
            mNoText = (TextView) itemView.findViewById(R.id.home_no_data);
        }

        @Override
        public void init(int position) {
            mNoText.setText(((NoData) mShowData.get(position)).getText());
        }
    }
}
