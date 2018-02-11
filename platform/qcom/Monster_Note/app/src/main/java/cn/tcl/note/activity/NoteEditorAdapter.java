/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import cn.tcl.note.R;
import cn.tcl.note.data.CommonData;
import cn.tcl.note.data.NoteAttachData;
import cn.tcl.note.data.NoteAudioData;
import cn.tcl.note.data.NotePicData;
import cn.tcl.note.data.NoteTextData;
import cn.tcl.note.db.DBData;
import cn.tcl.note.soundrecorderserver.PlayerService;
import cn.tcl.note.soundrecorderserver.SoundRecorderService;
import cn.tcl.note.timesave.TimeSaveThread;
import cn.tcl.note.ui.DialogHelper;
import cn.tcl.note.ui.EditTextHelper;
import cn.tcl.note.ui.ToastHelper;
import cn.tcl.note.util.FileUtils;
import cn.tcl.note.util.ImageLoader;
import cn.tcl.note.util.NoteLog;
import cn.tcl.note.util.TimeUtils;
import cn.tcl.note.util.XmlHash;
import cn.tcl.note.util.XmlPrase;


public class NoteEditorAdapter extends RecyclerView.Adapter implements
        SoundRecorderService.OnRefreshTimeUiListener, SimpleItemTouchHelperCallback.onMoveListener {

    public static final int TOOLBAR_WILLDO = 1;
    public static final int TOOLBAR_DOT = 2;
    private final int IMG_AUDIO_NUM = 10;
    private final String TAG = NoteEditorAdapter.class.getSimpleName();
    //data type,this will decide use which viewHolder
    public final static int TYPE_TEXT = 1;
    public final static int TYPE_PIC = 2;
    public final static int TYPE_AUDIO = 3;
    private Context mContext;
    //mAllData contain all editor data
    private LinkedList<CommonData> mAllData;
    //which line that selection in
    private int mCurrentLine = -1;
    private int mCurrentSelectionPos = 0;
    //when meed move,it is true.then view will move to current line when onBindViewHolder
    private boolean mIsMove;
    //when the line is img or audio,cursor is in after if true;
    private boolean iSAfter = true;
    //line that recording
    private int mAudioRecordLine = -1;
    //line that playing audio
    private int mAudioPlayLine = -1;

    private SoundRecorderService mRecorderService;
    private PlayerService mPlayerService;
    private ServiceConnection mPlayConnection;

    //image scale size,y change to 0.2
    private final float IMG_SCALE_Y = 0.2f;

    private long mId = -1;
    private String mTime = "";

    private boolean hasLineFocus;
    private boolean isScrolling = false;

    private int mTextLineH;
    private int mImgAudioH;
    private int mSameAttraH;
    private int mTextAttraH;
    private int mAttraTextH;
    private int mImgShadowH;
    private int mFirstTop;
    private AnimationDrawable mWaveAnima;
    private int mAnimaIndex;

    //30s save thread
    private TimeSaveThread mTimeSaveThread;

    public NoteEditorAdapter(Context context) {
        mContext = context;
        mAllData = new LinkedList<>();
        mAllData.add(new NoteTextData());
        ContentValues contentValues = XmlPrase.toContentValues(mAllData);
        Uri uri = mContext.getContentResolver().insert(DBData.TABLE_URI, contentValues);
        mId = ContentUris.parseId(uri);
        NoteLog.d(TAG, "add a new line:" + mId);
        XmlHash.initHash(contentValues.getAsString(DBData.COLUMN_XML));
        mTimeSaveThread = new TimeSaveThread(context, mAllData, mId);
        mTimeSaveThread.start();
    }

    public NoteEditorAdapter(Context context, String xmlStr, long id, String time) {
        mId = id;
        mContext = context;
        mTime = time;
        mAllData = XmlPrase.prase(xmlStr);
        XmlHash.initHash(xmlStr);
        mTimeSaveThread = new TimeSaveThread(context, mAllData, id);
        mTimeSaveThread.start();
    }

    public LinkedList<CommonData> getAllData() {
        return mAllData;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        RecyclerView.ViewHolder viewHolder;
        switch (viewType) {
            case TYPE_TEXT:
                itemView = LayoutInflater.from(mContext).inflate(R.layout.item_text_view, parent, false);
                viewHolder = new NoteTextView(itemView);
                break;
            case TYPE_PIC:
                itemView = LayoutInflater.from(mContext).inflate(R.layout.item_pic_view, parent, false);
                viewHolder = new NotePicView(itemView);
                break;
            case TYPE_AUDIO:
                itemView = LayoutInflater.from(mContext).inflate(R.layout.item_audio_view, parent, false);
                viewHolder = new NoteAudioView(itemView);
                break;
            default:
                return null;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NoteLog.i(TAG, "onBindViewHolder holder=" + holder + "  position=" + position);
        if (mTextLineH == 0) {
            Resources resources = mContext.getResources();
            mTextLineH = (int) resources.getDimension(R.dimen.editor_line_text_height);
            mImgAudioH = (int) resources.getDimension(R.dimen.editor_line_img_audio_height);
            mSameAttraH = (int) resources.getDimension(R.dimen.editor_line_same_atta_height);
            mTextAttraH = (int) resources.getDimension(R.dimen.editor_line_text_attar_height);
            mAttraTextH = (int) resources.getDimension(R.dimen.editor_line_attar_text_height);
            mImgShadowH = (int) resources.getDimension(R.dimen.img_shadow_height);
            mFirstTop = (int) resources.getDimension(R.dimen.note_edit_recycler_padding);
        }
        mAllData.get(position).setViewHolder(holder);

        setMarginTop(position);
        setMarginTop(position + 1);
        if (holder instanceof NoteTextView) {
            NoteTextData temp = (NoteTextData) mAllData.get(position);
            ((NoteTextView) holder).init(temp.getText(), temp.getFlag());
        } else if (holder instanceof NotePicView) {
            NotePicData temp = (NotePicData) mAllData.get(position);
            ((NotePicView) holder).init(temp.getFileName());
        } else if (holder instanceof NoteAudioView) {
            NoteAudioData temp = (NoteAudioData) mAllData.get(position);
            ((NoteAudioView) holder).init();
        }
        //handle focus
        if (position == getCurrentLine()) {

            NoteLog.d(TAG, "onBindViewHolder mCurrentLine is " + getCurrentLine() +
                    " currentSelectionPosition=" + mCurrentSelectionPos + "  iSAfter=" + iSAfter);
            //if position can't see,then move to the position to use can see
            if (mIsMove) {
                int currentLine = position;
                if (holder instanceof NotePicView) {
                    currentLine++;
                }
                ((NoteEditorActivity) mContext).moveToPosition(currentLine);
                mIsMove = false;
            }
            if (holder instanceof NoteTextView) {
                int length = ((NoteTextData) (mAllData.get(position))).getText().length();
                if (mCurrentSelectionPos > length) {
                    mCurrentSelectionPos = length;
                }
                int selectionPos = mCurrentSelectionPos;
                ((NoteTextView) holder).mEditNoteText.requestFocus();
                ((NoteTextView) holder).mEditNoteText.setSelection(selectionPos);
                changeCurrentLine(position, selectionPos);
            } else {
                if (iSAfter) {
                    ((NoteAttachView) holder).mEditAft.requestFocus();
                } else {
                    ((NoteAttachView) holder).mEditBef.requestFocus();
                }
            }

        }
    }

    private void setMarginTop(int position) {
        if (position >= mAllData.size()) {
            return;
        }
        RecyclerView.ViewHolder holder = mAllData.get(position).getViewHolder();
        if (holder == null) {
            return;
        }
        View itemView = ((BaseViewHolder) holder).getItemView();
//        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        int height = 0;
        if (position > 0) {
            NoteLog.d(TAG, "set margin top=" + position);
            CommonData aboveData = mAllData.get(position - 1);
            if (aboveData instanceof NoteTextData) {
                if (holder instanceof NoteTextView) {
                    height = mTextLineH;
                } else if (holder instanceof NoteAudioView) {
                    height = mTextAttraH - mImgShadowH;
                } else if (holder instanceof NotePicView) {
                    height = mTextAttraH - mImgShadowH;
                }
            } else if (aboveData instanceof NotePicData) {
                if (holder instanceof NoteTextView) {
                    height = mAttraTextH;
                } else if (holder instanceof NotePicView) {
                    height = mSameAttraH - mImgShadowH;
                } else if (holder instanceof NoteAudioView) {
                    height = mImgAudioH - mImgShadowH;
                }
                height -= mImgShadowH;
            } else if (aboveData instanceof NoteAudioData) {
                if (holder instanceof NoteTextView) {
                    height = mAttraTextH;
                } else if (holder instanceof NoteAudioView) {
                    height = mSameAttraH - mImgShadowH;
                } else if (holder instanceof NotePicView) {
                    height = mImgAudioH - mImgShadowH;
                }
                height -= mImgShadowH;
            }
        } else if (position == 0) {
            if (holder instanceof NoteTextView) {
                height = mFirstTop;
            } else {
                height = mFirstTop - mImgShadowH;
            }

        }
//        lp.topMargin = height;
        itemView.setPadding(itemView.getPaddingLeft(), height,
                itemView.getPaddingRight(), itemView.getPaddingBottom());
    }

    @Override
    public int getItemCount() {
        return mAllData.size();
    }

    @Override
    public int getItemViewType(int position) {
        CommonData data = mAllData.get(position);
        if (data instanceof NoteTextData) {
            return TYPE_TEXT;
        } else if (data instanceof NotePicData) {
            return TYPE_PIC;
        } else if (data instanceof NoteAudioData) {
            return TYPE_AUDIO;
        } else {
            return -1;
        }
    }

    /**
     * when click tool bar,will change text flag.
     *
     * @param button show that use click which button.
     *               Have two value:TOOLBAR_WILLDO,TOOLBAR_DOT
     */
    public void changeTextFlag(int button) {
        CommonData temp = mAllData.get(getCurrentLine());
        NoteTextData textData;
        if (temp instanceof NoteTextData) {
            textData = (NoteTextData) temp;
        } else {
            return;
        }
        int oldFlag = textData.getFlag();
        int newFlag = -1;
        switch (button) {
            case TOOLBAR_WILLDO:
                if (oldFlag == NoteTextData.FLAG_NO) {
                    newFlag = NoteTextData.FLAG_WILLDO_UN;
                } else if (oldFlag == NoteTextData.FLAG_WILLDO_CK ||
                        oldFlag == NoteTextData.FLAG_WILLDO_UN) {
                    newFlag = NoteTextData.FLAG_NO;
                }
                break;
            case TOOLBAR_DOT:
                if (oldFlag == NoteTextData.FLAG_DOT) {
                    newFlag = NoteTextData.FLAG_NO;
                } else if (oldFlag == NoteTextData.FLAG_NO) {
                    newFlag = NoteTextData.FLAG_DOT;
                }
                break;
        }
        if (newFlag == -1) {
            return;
        }
        textData.setFlag(newFlag);
        setToolBarButtonEnable(newFlag);
        ((NoteTextView) textData.getViewHolder()).setFlagView(newFlag);

    }

    /**
     * add a new Img (it is filename) to next line
     *
     * @param fileName
     */
    public void addNewPicLine(String fileName) {
        NotePicData picData = new NotePicData(fileName);
        addNewAttachmentLine(picData);
    }

    /**
     * add a audio
     *
     * @param service record server
     */
    public void addAudioRecordLine(SoundRecorderService service) {
        initAudioRecordService(service);
        String audioName = FileUtils.getAudioName();
        mRecorderService.setSaveRecordFile(FileUtils.getAudioWholePath(audioName));
        int i= mRecorderService.startRecord();
        if(i==0) {
            ((NoteEditorActivity)mContext). setButtonEnable(false, R.id.toolbar_audio);
            NoteAudioData audioData = new NoteAudioData();
            int line = addNewAttachmentLine(audioData);
            changeAudioRecordLine(line);
            ((NoteAudioData) mAllData.get(getAudioRecordLine())).setFileName(audioName);
        }
    }

    public void addAudioFromApp(String fileName, long duration) {
        NoteAudioData audioData = new NoteAudioData(fileName, duration);
        addNewAttachmentLine(audioData);
    }

    //if line is a blank,then return true.
    private boolean isblankLine(int line) {
        NoteTextData noteTextData = (NoteTextData) mAllData.get(line);
        NoteTextView noteTextView = (NoteTextView) noteTextData.getViewHolder();
        if (noteTextData.getFlag() == NoteTextData.FLAG_NO && noteTextView.mEditNoteText.getText().length() <= 0) {
            return true;
        }
        return false;
    }

    // add a audio or img,handle selection issue
    private synchronized int addNewAttachmentLine(CommonData newData) {
        int newLine;
        int line = getCurrentLine();
        CommonData commonData = mAllData.get(line);
        BaseViewHolder baseViewHolder = (BaseViewHolder) commonData.getViewHolder();
        NoteLog.d(TAG, "line=" + line + "viewhord=" + baseViewHolder);

        //current line is a attachment,then add attachment acoording to before edit or after edit
        if (commonData instanceof NoteAttachData) {
            NoteAttachView noteAttachView = (NoteAttachView) commonData.getViewHolder();
            if (noteAttachView.getISCursorAfter()) {
                line++;
            } else {
                //not
            }
            newLine = addNewLine(line, newData);
            changeCurrentLineEND(newLine);
            mIsMove = true;
            ((NoteEditorActivity) mContext).moveToPosition(line);
            notifyItemInserted(line);
        } else {
            //current line is a text
            if (isblankLine(line)) {
                //this is a black line,will instead the line
                mAllData.set(line, newData);
                newLine = line;
                changeCurrentLineEND(line);
                mIsMove = true;
                ((NoteEditorActivity) mContext).moveToPosition(line);
                notifyItemChanged(line);
            } else {
                //this not black line
                int newLineNum = cutTextLine((NoteTextView) commonData.getViewHolder(), false) == -1 ? 0 : 1;
                if (isblankLine(line)) {
                    mAllData.set(line, newData);
                    newLine = line;
                } else {
                    newLine = addNewLine(line + 1, newData);
                }
                changeCurrentLineEND(newLine);
                mIsMove = true;
                ((NoteEditorActivity) mContext).moveToPosition(getCurrentLine());
                notifyItemRangeInserted(line + 1, 1 + newLineNum);
            }
        }
        //attachment is the latest line
        if (newLine == mAllData.size() - 1) {
            addNewLine(newLine + 1, new NoteTextData());
        }
        return newLine;
    }

    public void onRestart() {
        if (FileUtils.iSRefresh) {
            NoteLog.d(TAG, "onRestart refresh data");
            for (int i = mAllData.size() - 1; i >= 0; i--) {
                if (getItemViewType(i) == TYPE_PIC) {
                    NotePicView picView = (NotePicView) mAllData.get(i).getViewHolder();
                    if (picView != null) {
                        picView.init(((NoteAttachData) mAllData.get(i)).getFileName());
                    }
                }
            }
            FileUtils.iSRefresh = false;
        }
    }

    public void onResume() {
        if (mTimeSaveThread != null) {
            mTimeSaveThread.startSave();
        }
    }

    public void onPause() {
        if (mCurrentLine < 0) {
            return;
        }
        BaseViewHolder baseViewHolder = (BaseViewHolder) mAllData.get(mCurrentLine).getViewHolder();
        if (baseViewHolder instanceof NoteTextView) {
            //save text that don not save
            String str = ((NoteTextView) baseViewHolder).mEditNoteText.getText().toString();
            ((NoteTextData) mAllData.get(mCurrentLine)).setText(str);
        }
        if (mTimeSaveThread != null) {
            mTimeSaveThread.pauseSave();
        }
    }

    /**
     * save wdit text when after 30s
     */
    public void saveEditText() {
        if (mCurrentLine < 0) {
            return;
        }
        int position = mCurrentLine;
        BaseViewHolder baseViewHolder = (BaseViewHolder) mAllData.get(position).getViewHolder();
        if (baseViewHolder instanceof NoteTextView) {
            //save text that don not save
            String text = ((NoteTextView) baseViewHolder).mEditNoteText.getText().toString();
            ((NoteTextData) mAllData.get(position)).setText(text);
        }
    }

    /**
     * @return false if img's num is 10,or true
     */
    public boolean isExceedImgNum() {
        return isExceedNum(TYPE_PIC) < IMG_AUDIO_NUM;
    }

    /**
     * @return false if audio's num is 10,or true
     */
    public boolean isExceedAudioNum() {
        return isExceedNum(TYPE_AUDIO) < IMG_AUDIO_NUM;
    }

    public int getImgNum() {
        return isExceedNum(TYPE_PIC);
    }

    public int getAudioNum() {
        return isExceedNum(TYPE_AUDIO);
    }

    private int isExceedNum(int type) {
        int sum = 0;
        for (int i = mAllData.size() - 1; i >= 0; i--) {
            if (getItemViewType(i) == type) {
                sum++;
                if (sum >= IMG_AUDIO_NUM) {
                    return sum;
                }
            }
        }
        NoteLog.d(TAG, "type=" + type + " num is " + sum);
        return sum;
    }

    /**
     * the view  will truncate text after selection and new add a line
     *
     * @param noteTextView
     * @param isSaveBlank  when new line is empty,the line will be save if is true,
     *                     or will be abandon if is false.if the event source is enter,
     *                     this should be true.If is add pic or audio,it should be false.
     * @return new line num,if not add,return -1;
     */
    private int cutTextLine(NoteTextView noteTextView, boolean isSaveBlank) {
        int selePos = noteTextView.mEditNoteText.getSelectionStart();
        int line = noteTextView.getItemPosition();

        String newStr = noteTextView.mEditNoteText.getText().toString();
        //delete text after selection postion
        String cutStr = newStr.substring(0, selePos);
        noteTextView.mEditNoteText.setText(cutStr);
        ((NoteTextData) mAllData.get(line)).setText(cutStr);
        //text after selection
        String secStr = newStr.substring(selePos);

        //if the line is blank and is not last line,will be abandon.
        if (!isSaveBlank && "".equals(secStr)) {//&& line < mAllData.size() - 1) {
            return -1;
        }

        int flag = ((NoteTextData) mAllData.get(line)).getFlag();
        NoteTextData newData = new NoteTextData(secStr,
                flag == NoteTextData.FLAG_WILLDO_CK ? NoteTextData.FLAG_WILLDO_UN : flag);
        addNewLine(line + 1, newData);
        return line + 1;
    }

    /**
     * add a new data to alldata,and update UI
     *
     * @param line add data to which line
     * @param data
     */
    private int addNewLine(int line, CommonData data) {
        NoteLog.d(TAG, "add new line " + line);
        if (line <= getAudioRecordLine()) {
            changeAudioRecordLine(getAudioRecordLine() + 1);
        }
        if (line <= getAudioPlayLine()) {
            changeAudioPlayLine(getAudioPlayLine() + 1);
        }
        mAllData.add(line, data);
        return line;
    }

    /**
     * remove the line from mAlldata
     *
     * @param line
     */
    private void removeLine(int line) {
        NoteLog.d(TAG, "remove the line " + line);
        if (line < getAudioRecordLine()) {
            changeAudioRecordLine(getAudioRecordLine() - 1);
        }
        if (line < getAudioPlayLine()) {
            changeAudioPlayLine(getAudioPlayLine() - 1);
        }
        mAllData.remove(line);
        notifyItemRemoved(line);
    }

    private int getAudioRecordLine() {
        NoteLog.d(TAG, "current record line is " + mAudioRecordLine);
        return mAudioRecordLine;
    }

    private int getAudioPlayLine() {
        NoteLog.d(TAG, "current play line is " + mAudioPlayLine);
        return mAudioPlayLine;
    }

    private void changeCurrentLine(int line) {
        changeCurrentLine(line, -1);
    }

    //move selection to end,if is text,selection is in text's end.
    // If is attachment,is in after edit.
    private void changeCurrentLineEND(int line) {
        changeCurrentLine(line, -2);
    }

    //change current line to @line,selection position to @selePosition
    private void changeCurrentLine(int line, int selePositon) {
        NoteLog.d(TAG, "change current line from " + mCurrentLine + " to " + line);
        NoteLog.d(TAG, "change current selection position from " + mCurrentSelectionPos + " to " + selePositon);
        if (line < 0) {
            mCurrentLine = 0;
            mCurrentSelectionPos = 0;
            iSAfter = false;
            return;
        }
        mCurrentLine = line;
        if (selePositon >= 0) {
            mCurrentSelectionPos = selePositon;
        }
        if (selePositon == -2) {
            CommonData commonData = mAllData.get(line);
            if (commonData instanceof NoteTextData) {
                mCurrentSelectionPos = ((NoteTextData) commonData).getText().length();
            } else {
                iSAfter = true;

            }

        }
    }

    public int getCurrentLine() {
        return mCurrentLine;
    }


    private void initAudioRecordService(SoundRecorderService service) {
        mRecorderService = service;

        mRecorderService.setOnRefreshTimeUiListener(this);
    }

    private void changeAudioRecordLine(int line) {
        NoteLog.d(TAG, "change audio record line from " + getAudioRecordLine() + " to " + line);
        mAudioRecordLine = line;
    }

    private void changeAudioPlayLine(int line) {
        NoteLog.d(TAG, "change audio play line from " + getAudioPlayLine() + " to " + line);
        mAudioPlayLine = line;
    }


    /**
     * refresh audio record time
     *
     * @param time
     */
    @Override
    public void onRefreshTimeUi(long time) {
        if (getAudioRecordLine() != -1) {
            NoteAudioView audioView = (NoteAudioView) mAllData.get(getAudioRecordLine()).getViewHolder();
            audioView.mAudioRecordTime.setText(TimeUtils.formatTime(time));
        }
    }

    /**
     * when move item,callback the method.
     *
     * @param from from which line
     * @param to   to which line
     * @return
     */
    @Override
    public boolean onItemMove(int from, int to) {
        NoteLog.d(TAG, "start stmove from " + from + " to " + to);
        if (from > to) {
            for (int i = from; i > to; i--) {
                onOneItemMove(i, i - 1);
            }
        } else if (from < to) {
            for (int i = from; i < to; i++) {
                onOneItemMove(i, i + 1);
            }
        }
        NoteLog.d(TAG, "end stmove from " + from + " to " + to);
        return true;
    }

    private void onOneItemMove(int from, int to) {
        NoteLog.d(TAG, "exmove item from " + from + " to " + to);
        CommonData tempData = mAllData.get(from);
        NoteLog.d(TAG, "before exmove ,from=" + mAllData.get(from) + "  to=" + mAllData.get(to));
        mAllData.set(from, mAllData.get(to));
        mAllData.set(to, tempData);
        NoteLog.d(TAG, "after exmove ,from=" + mAllData.get(from) + "  to=" + mAllData.get(to));
        //change audio record and play line
        int recordLine = getAudioRecordLine();
        int playLine = getAudioPlayLine();
        if (recordLine == from) {
            changeAudioRecordLine(to);
        } else if (recordLine == to) {
            changeAudioRecordLine(from);
        }
        if (playLine == from) {
            changeAudioPlayLine(to);
        } else if (playLine == to) {
            changeAudioPlayLine(from);
        }
        if (mCurrentLine == from) {
            mCurrentLine = to;
        } else if (mCurrentLine == to) {
            mCurrentLine = from;
        }
        notifyItemMoved(from, to);
        for (CommonData commonData : mAllData) {
            NoteLog.d(TAG, "exmove all data=" + commonData);
        }
    }

    /**
     * handle back event.stop playing audio,pop a dialog when record a audio,
     * and save all data
     *
     * @return
     */
    public boolean onHandleBack() {
        if (stopRecordBack()) {
            stopPlayingAudioLine();
            saveAllData();
            return true;
        } else {
            return false;
        }

    }

    private boolean stopRecordBack() {
        if (getAudioRecordLine() != -1) {
            DialogHelper.showDialog(mContext, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_NEGATIVE) {
                                //not
                            } else if (which == DialogInterface.BUTTON_POSITIVE) {
                                stopRecordLine();
                                stopPlayingAudioLine();
                                saveAllData();
                                ((NoteEditorActivity) mContext).finish();
                            }
                        }
                    }, mContext.getString(R.string.dialog_return_title),
                    mContext.getString(R.string.dialog_back_msg));
            return false;
        } else {
            return true;
        }
    }

    //save all data when back to home
    private void saveAllData() {
        onPause();
        if(mTimeSaveThread != null){
            mTimeSaveThread.stopSave();
        }
        mTimeSaveThread = null;
        //save data to db
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver = mContext.getContentResolver();

                //save or update data
                ContentValues contentValues = XmlPrase.toContentValues(mAllData);
                // if is empty,don not save
                if (contentValues.get(DBData.COLUMN_FIRSTLINE).equals("")
                        && contentValues.get(DBData.COLUMN_IMG).equals("0")
                        && contentValues.get(DBData.COLUMN_AUDIO).equals("0")) {
                    contentResolver.delete(DBData.TABLE_URI, DBData.COLUMN_ID + "=?", new String[]{"" + mId});
                    return;
                }
                if (mId == -1) {
                    contentResolver.insert(DBData.TABLE_URI, contentValues);
                } else {
                    if (XmlHash.iSSameWithInit(contentValues.getAsString(DBData.COLUMN_XML))) {
                        //if current data is same with init data,then use old time.it means don not update time
                        contentValues.put(DBData.COLUMN_TIME, mTime);
                    }
                    contentResolver.update(DBData.TABLE_URI, contentValues, DBData.COLUMN_ID + "=?", new String[]{"" + mId});
                }

            }
        }).start();
    }

    public void stopPlayingAudioLine() {
        if (getAudioPlayLine() != -1) {
            mPlayerService.stopPlay();
            if (mPlayConnection != null) {
                mContext.unbindService(mPlayConnection);
                mPlayConnection = null;
            }
            int line = getAudioPlayLine();
            changeAudioPlayLine(-1);
            ((NoteAudioView) mAllData.get(line).getViewHolder()).init();

        }
    }

    public void stopRecord() {
        int line = getAudioRecordLine();
        if (line >= 0) {
            stopRecordLine();
            ((NoteAudioView) (mAllData.get(line).getViewHolder())).init();
        }
    }

    public int getRecordState() {
        return mRecorderService.getState();
    }

    private void stopRecordLine() {
        if (mRecorderService == null) {
            return;
        }
        NoteAudioData audioData = (NoteAudioData) mAllData.get(getAudioRecordLine());
        long time = mRecorderService.stopRecord();
        audioData.setDuration(time);

        String newName = FileUtils.getAudioName();
        String oldName = audioData.getFileName();
        File newFile = new File(FileUtils.getAudioWholePath(newName));
        File oldFile = new File(FileUtils.getAudioWholePath(oldName));
        oldFile.renameTo(newFile);
        audioData.setFileName(newName);

        changeAudioRecordLine(-1);
        ((NoteEditorActivity) mContext).releaseRecordButton();
        mRecorderService = null;
    }

    public void setLastLineCursor() {
        CommonData commonData = mAllData.getLast();
        if (commonData.getViewHolder() != null) {
            if (commonData instanceof NoteTextData) {
                EditText edittext = ((NoteTextView) commonData.getViewHolder()).mEditNoteText;
                int length = edittext.getText().length();
                edittext.requestFocus();
                edittext.setSelection(length);
                changeCurrentLine(mAllData.size() - 1, length);
            } else {
                EditText editText = ((NoteAttachView) commonData.getViewHolder()).mEditAft;
                editText.requestFocus();
                changeCurrentLine(mAllData.size() - 1, -2);
            }
        }
    }

    /**
     * get all img file name
     *
     * @return all img name
     */
    public ArrayList<String> getAllImgFile() {
        ArrayList<String> allFile = new ArrayList<>();
        for (CommonData commonData : mAllData) {
            if (commonData instanceof NotePicData) {
                NotePicData picData = (NotePicData) commonData;
                String fileNmae = picData.getFileName();
                if (FileUtils.isExits(FileUtils.getPicWholePath(fileNmae))) {
                    allFile.add(fileNmae);
                }

            }
        }
        if (NoteLog.DEBUG) {
            for (String fileName : allFile) {
                NoteLog.d(TAG, "get all img file =" + fileName);
            }
        }
        return allFile;
    }

    public void onScrollChange(RecyclerView recyclerView, int state) {
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            ///no move
            isScrolling = false;
        } else if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
            //move
            isScrolling = true;
            ((NoteEditorActivity) mContext).closeInputMethod();
        }
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int lastItem = linearLayoutManager.findLastVisibleItemPosition();
        int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
        NoteLog.d(TAG, "recycler scroll: firstItem=" + firstItem + "  lastItem=" + lastItem);
        int line = getCurrentLine();
        if (line <= lastItem && line >= firstItem) {
            //the line can see
            NoteLog.d(TAG, "redraw the line:" + line);
            if (!hasLineFocus) {
                NoteLog.d(TAG, "scroll get focus");
                BaseViewHolder holder = (BaseViewHolder) mAllData.get(line).getViewHolder();
                if(null!=holder) {
                    if (holder instanceof NoteTextView) {

                        int length = ((NoteTextData) (mAllData.get(line))).getText().length();
                        if (mCurrentSelectionPos > length) {
                            mCurrentSelectionPos = length;
                        }
                        if (!((NoteTextView) holder).mEditNoteText.isFocused()) {
                            ((NoteTextView) holder).mEditNoteText.requestFocus();
                            ((NoteTextView) holder).mEditNoteText.setSelection(mCurrentSelectionPos);
                        }
                    } else {
                        if (iSAfter) {
                            ((NoteAttachView) holder).mEditAft.requestFocus();
                        } else {
                            ((NoteAttachView) holder).mEditBef.requestFocus();
                        }
                    }
                }
            }
        }
    }

    public boolean getIsScroll() {
        return isScrolling;
    }

    class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener,
            EditTextHelper.OnEnterAndDelListener {
        private View mItemView;

        public BaseViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            //The reason of it is false is :1.not see item will not destroy,and item will can use getPosition
            setIsRecyclable(false);
        }

        public View getItemView() {
            return mItemView;
        }

        /**
         * get current line that selection is in
         *
         * @return line
         */
        public int getItemPosition() {
            return this.getAdapterPosition();
        }

        @Override
        public void onFocusChange(View view, boolean b) {
            int newline = getItemPosition();
            if (newline == -1 || newline >= mAllData.size()) {
                return;
            }
            if (b) {
                hasLineFocus = true;
                NoteLog.d(TAG, "Line #" + newline + " get focus");
                ((NoteEditorActivity) mContext).changeToEditMode();
                int selectionPosition = -1;

                if (this instanceof NoteAttachView) {
                    ((NoteEditorActivity) mContext).setWillDotEnable(3);
                    //if is attachment,keep beofre or after
                    if (view.getId() == R.id.item_pic_edit_after || view.getId() == R.id.item_audio_edit_after) {
                        ((NoteAttachView) this).setISCursorAfter(true);
                        iSAfter = true;
                        NoteLog.d(TAG, "this is a attachment view,cursor is after");
                    } else if (view.getId() == R.id.item_pic_edit_before || view.getId() == R.id.item_audio_edit_before) {
                        ((NoteAttachView) this).setISCursorAfter(false);
                        iSAfter = false;
                        NoteLog.d(TAG, "this is a attachment view,cursor is before");
                    }
                } else {
//                    ((NoteEditorActivity) mContext).showInputMethod();
                    //if is text,keep selection position
                    if(mAllData.get(getItemPosition()) instanceof NoteAudioData){
                        int flag = ((NoteTextData) mAllData.get(getItemPosition())).getFlag();
                        setToolBarButtonEnable(flag);
                        selectionPosition = ((NoteTextView) this).mEditNoteText.getSelectionEnd();
                        NoteLog.d(TAG, "get selection position is " + selectionPosition);
                    }
                }
                changeCurrentLine(newline, selectionPosition);
            } else {
                hasLineFocus = false;
                NoteLog.d(TAG, "Line #" + newline + " lose focus");
                //save text when Edittext lose focus
                if (this instanceof NoteTextView) {
                    if (isScrolling) {
                        NoteLog.d(TAG, "save scroll lost focus");
                        int selectionPosition = ((NoteTextView) this).mEditNoteText.getSelectionEnd();
                        NoteLog.d(TAG, "get selection position is " + selectionPosition);
                        changeCurrentLine(newline, selectionPosition);
                    }
                    String newStr = ((NoteTextView) this).mEditNoteText.getText().toString();
                    int line = getItemPosition();
                    ((NoteTextData) mAllData.get(line)).setText(newStr);
                    NoteLog.d(TAG, "save text is " + newStr);
                } else {
                    ((NoteAttachView) this).cancelBorder();
                }
            }
        }

        @Override
        synchronized public boolean onEnterListener(View view) {
            if (this instanceof NoteTextView) {
                int line = cutTextLine((NoteTextView) this, true);
                mIsMove = true;
                changeCurrentLine(line, 0);
                notifyItemInserted(line);
                return true;
            } else {
                int line = -1;
                if (view.getId() == R.id.item_pic_edit_after || view.getId() == R.id.item_audio_edit_after) {
                    line = getCurrentLine() + 1;
                } else if (view.getId() == R.id.item_pic_edit_before || view.getId() == R.id.item_audio_edit_before) {
                    line = getCurrentLine();
                }
                String text = ((EditText) view).getText().toString();
                addNewLine(line, new NoteTextData(text, NoteTextData.FLAG_NO));
                ((EditText) view).setText("");
                changeCurrentLine(line, text.length());
                mIsMove = true;
                ((NoteEditorActivity) mContext).moveToPosition(getCurrentLine());
                notifyItemInserted(line);
                return true;
            }
        }

        @Override
        public boolean onDelListener(View view) {
            int currentLine = getItemPosition();
            NoteLog.d(TAG, "handle del event,line is " + currentLine);
            if (currentLine < 0) {
                return true;
            }
            CommonData commonData = mAllData.get(currentLine);
            //current line is text
            if (this instanceof NoteTextView) {
                NoteTextData noteTextData = (NoteTextData) commonData;
                //if have flag,then del flag
                if (noteTextData.getFlag() != NoteTextData.FLAG_NO) {
                    noteTextData.setFlag(NoteTextData.FLAG_NO);
                    setToolBarButtonEnable(0);
                    ((NoteTextView) this).setFlagView(NoteTextData.FLAG_NO);
                } else {
                    //go in above line
                    changeCurrentLineEND(currentLine - 1);
                    if (currentLine > 0) {
                        CommonData aboveData = mAllData.get(currentLine - 1);
                        if (aboveData instanceof NoteAttachData) {
                            //if the next line del,then show border for attachment.
                            NoteAttachView attachView = (NoteAttachView) aboveData.getViewHolder();
                            attachView.setBorder();
                        } else if (aboveData instanceof NoteTextData) {
                            //if current line have text and above line is text,
                            // then add currnt text to above line
                            String addText = ((NoteTextData) aboveData).getText() + ((NoteTextView) this).mEditNoteText.getText();
                            ((NoteTextData) aboveData).setText(addText);
                            ((NoteTextView) this).mEditNoteText.setText("");
                        }
                    }
                    mIsMove = true;
                    //remove the blank line.
                    if (((NoteTextView) this).mEditNoteText.getText().length() == 0) {
                        if (mAllData.size() > 1) {
                            removeLine(currentLine);
                        } else {
                            noteTextData.setText("");
                        }
                        if (mCurrentLine == 0) {
                            notifyItemChanged(0);
                        } else {
                            notifyItemChanged(currentLine - 1);
                        }
                    } else {

                        notifyItemChanged(currentLine - 1);
                    }
                }
            } else { //current line is attachment
                if (view.getId() == R.id.item_pic_edit_after || view.getId() == R.id.item_audio_edit_after) {
                    ((NoteAttachView) this).handleDelEvent();
                } else if (view.getId() == R.id.item_pic_edit_before || view.getId() == R.id.item_audio_edit_before) {
                    changeCurrentLineEND(currentLine - 1);
                    mIsMove = true;
                    if (currentLine > 0) {
                        CommonData aboveData = mAllData.get(currentLine - 1);
                        if (aboveData instanceof NoteAttachData) {
                            //if the next line del,then show border for attachment.
                            NoteAttachView attachView = (NoteAttachView) aboveData.getViewHolder();
                            attachView.setBorder();
                        }
                    }
                    notifyItemChanged(currentLine - 1);
                }
            }
            ((NoteEditorActivity) mContext).showInputMethod();
            return true;
        }
    }

    private void setToolBarButtonEnable(int flag) {
        switch (flag) {
            case NoteTextData.FLAG_NO:
                flag = 0;
                break;
            case NoteTextData.FLAG_WILLDO_CK:
            case NoteTextData.FLAG_WILLDO_UN:
                flag = 1;
                break;
            case NoteTextData.FLAG_DOT:
                flag = 2;
                break;
        }
        ((NoteEditorActivity) mContext).setWillDotEnable(flag);
    }

    class NoteTextView extends BaseViewHolder implements View.OnClickListener {
        private CheckBox mWillDo;
        private EditTextHelper mEditNoteText;
        private ImageView mDot;

        public NoteTextView(View itemView) {
            super(itemView);
            mWillDo = (CheckBox) itemView.findViewById(R.id.item_willdo);
            mWillDo.setOnClickListener(this);

            mEditNoteText = (EditTextHelper) itemView.findViewById(R.id.item_text);
            mEditNoteText.setOnFocusChangeListener(this);
            mEditNoteText.setOnEnterAndDelListener(this);
            mDot = (ImageView) itemView.findViewById(R.id.item_dot);
        }

        public void init(String text, int flag) {
            mEditNoteText.setText(text);
            setFlagView(flag);
        }

        /**
         * According flag,change view status
         *
         * @param flag one value of NoteTextData.FLAG_NO,FLAG_WILLDO_UN,
         *             FLAG_WILLDO_CK,FLAG_DOT
         */
        public void setFlagView(int flag) {
            float mutil = 0;
            if (flag == NoteTextData.FLAG_NO) {
                //set line Spacing 1.8
                mutil = 1.45f;
            } else {
                //set line Spacing 1.5
                mutil = 1.25f;
            }
            mEditNoteText.setLineSpacing(mutil);
            switch (flag) {
                case NoteTextData.FLAG_NO:
                    mWillDo.setVisibility(View.GONE);
                    mDot.setVisibility(View.GONE);
                    mWillDo.setChecked(false);
                    mEditNoteText.setTextColor(mContext.getResources().getColor(R.color.text_unck));
                    break;
                case NoteTextData.FLAG_WILLDO_UN:
                    mDot.setVisibility(View.GONE);
                    mWillDo.setVisibility(View.VISIBLE);
                    mWillDo.setChecked(false);
                    break;
                case NoteTextData.FLAG_WILLDO_CK:
                    mDot.setVisibility(View.GONE);
                    mWillDo.setVisibility(View.VISIBLE);
                    mWillDo.setChecked(true);
                    mEditNoteText.setTextColor(mContext.getResources().getColor(R.color.text_check));
                    break;
                case NoteTextData.FLAG_DOT:
                    mDot.setVisibility(View.VISIBLE);
                    mWillDo.setVisibility(View.GONE);
                    mWillDo.setChecked(false);
                    break;
            }
        }

        @Override
        public void onClick(View view) {
            int id;
            int flag;
            boolean isCheck = mWillDo.isChecked();
            if (isCheck) {
                id = R.color.text_check;
                flag = NoteTextData.FLAG_WILLDO_CK;
            } else {
                id = R.color.text_unck;
                flag = NoteTextData.FLAG_WILLDO_UN;
            }
            ((NoteTextData) mAllData.get(getItemPosition())).setFlag(flag);
            mEditNoteText.setTextColor(mContext.getResources().getColor(id));
        }
    }

    public class NoteAttachView extends BaseViewHolder implements SimpleItemTouchHelperCallback.onPicMoveListener {

        protected EditTextHelper mEditBef;
        protected EditTextHelper mEditAft;
        public View mViewBorder;
        private boolean iSCursorAfter;
        protected boolean hasBorder = false;
        private int mMoveStartItem;
        private int mMoveEndItem;

        public NoteAttachView(View itemView) {
            super(itemView);
            mViewBorder = itemView.findViewById(R.id.attachment_border);
        }

        public void handleDelEvent() {
            if (hasBorder) {
                DialogHelper.showDelDialog(mContext, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NoteLog.d(TAG, "dialog click which=" + which);
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            int line = getItemPosition();
                            NoteAttachData attachData = (NoteAttachData) mAllData.get(line);
                            String fileName = attachData.getFileName();
                            delFile(fileName);
                            removeLine(line);
                            if (mAllData.size() == 0) {
                                NoteLog.d(TAG, "no have data.so add a new text");
                                addNewLine(0, new NoteTextData());
                            }
                            cancelBorder();
                            changeCurrentLineEND(line - 1);
                            notifyItemRangeChanged(line - 1, 2);
                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                            cancelBorder();
                        }
                    }
                }, this);

            } else {
                setBorder();
            }
        }

        protected void delFile(String fileName) {
        }

        public void setBorder() {
            hasBorder = true;
//            mViewBorder.setBackgroundResource(R.drawable.item_del_linearlayout_border);
            mViewBorder.setForeground(mContext.getDrawable(R.drawable.item_del_linearlayout_border));
        }

        public void cancelBorder() {
            hasBorder = false;
//            mViewBorder.setBackgroundResource(R.drawable.item_del_linearlayout_border_cancel);
            mViewBorder.setForeground(mContext.getDrawable(R.drawable.item_del_linearlayout_border_cancel));
        }

        public boolean getISCursorAfter() {
            return iSCursorAfter;
        }

        public void setISCursorAfter(boolean result) {
            iSCursorAfter = result;
        }

        @Override
        public void onItemSelected() {
            vibrator();
            mMoveStartItem = getItemPosition();
            ((NoteEditorActivity) mContext).closeInputMethod();
        }

        public void vibrator() {
            Vibrator vib = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(new long[]{35, 55}, -1);
        }

        @Override
        public void onItemClear() {
            mMoveEndItem = getItemPosition();
            try {
                notifyItemChanged(mMoveStartItem);
                notifyItemChanged(mMoveEndItem);
            } catch (Exception e) {
                NoteLog.e(TAG, "not item clear", e);
            }

        }
    }

    public class NotePicView extends NoteAttachView {
        public final static String KEY_INDEX = "index";
        public final static String KEY_LIST = "list";
        private ImageView mImageView;
        private float mMoveHeight;
        private float mMoveY;

        public NotePicView(View itemView) {
            super(itemView);

            mImageView = (ImageView) itemView.findViewById(R.id.item_iv_pic);
            mImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    NoteLog.d(TAG, "long click img");
                    getScaleBitmap();
                    ((NoteEditorActivity) mContext).moveRecyY(mMoveHeight);

                    ((NoteEditorActivity) mContext).startDrag(NotePicView.this);
                    mImageView.setImageResource(R.drawable.move_img_show);
                    NoteLog.d(TAG, "long click end");
                    return false;
                }
            });
            mImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        NoteLog.d(TAG, "touch y=" + event.getY() + "  rawY=" + event.getRawY() + " top=" + mImageView.getTop() + " view top=" + mViewBorder.getTop());
                        mMoveY = event.getY();
                    }
                    return false;
                }
            });
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NoteLog.d(TAG, "click img");
                    ArrayList<String> allFile = getAllImgFile();
                    if (allFile.size() == 0) {
                        return;
                    }
                    String fileName = ((NotePicData) mAllData.get(getItemPosition())).getFileName();
                    int index = allFile.indexOf(fileName);
                    Intent intent = new Intent();
                    intent.setClass(mContext, ImgViewPager.class);
                    intent.putExtra(KEY_INDEX, index);
                    intent.putStringArrayListExtra(KEY_LIST, allFile);
                    mContext.startActivity(intent);
                }
            });

            mEditBef = (EditTextHelper) itemView.findViewById(R.id.item_pic_edit_before);
            mEditAft = (EditTextHelper) itemView.findViewById(R.id.item_pic_edit_after);
            mEditBef.setOnEnterAndDelListener(this);
            mEditBef.setOnFocusChangeListener(this);
            mEditAft.setOnEnterAndDelListener(this);
            mEditAft.setOnFocusChangeListener(this);
            mViewBorder = mImageView;
        }

        public void init(String fileName) {
            boolean imgExits = FileUtils.isExits(FileUtils.getPicWholePath(fileName));
            if (!imgExits) {
                mImageView.setOnClickListener(null);
            }

            ImageLoader.getInstance(ImageLoader.IMG_16, mContext).loadBitmap(mImageView, fileName);
        }

        @Override
        public void onItemSelected() {
            super.onItemSelected();
        }

        @NonNull
        private Bitmap getScaleBitmap() {
//            Bitmap normalBitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
            int normalBitmapHeight = ((LinearLayout) mImageView.getParent()).getHeight();
            float maxScaleH = mContext.getResources().getDimension(R.dimen.audio_height);
            mImageView.setMaxHeight((int) maxScaleH);
            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            mMoveHeight = mMoveY * (1 - maxScaleH / normalBitmapHeight);
            NoteLog.d(TAG, "bitmap before height=" + normalBitmapHeight + "  after height=" + maxScaleH);
            return null;
        }

        @Override
        public void onItemClear() {
            mImageView.setMaxHeight((int) mContext.getResources().getDimension(R.dimen.editor_img_max_height));
            mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            init(((NotePicData) mAllData.get(getItemPosition())).getFileName());
            super.onItemClear();
        }

        @Override
        protected void delFile(String fileName) {
            FileUtils.delImgFile(fileName);
        }

    }


    public class NoteAudioView extends NoteAttachView implements View.OnClickListener, PlayerService.OnRefreshUiListener,
            SoundRecorderService.OnStateChangeListener {
        //audio record view
        private LinearLayout mAudioRecordLayout;
        private ImageView mAudioRecordStartPause;
        private ImageView mAudioRecordStop;
        private TextView mAudioRecordTime;
        private ImageView mAudioRecordWave;
        private FrameLayout mAudioRecordCircle;

        //audio show view
        private LinearLayout mAudioShowLayout;
        private ImageView mAudioShowStart;
        private TextView mAudioShowTitle;
        private TextView mAudioShowTime;

        //audio paly view
        private LinearLayout mAudioPlayLayout;
        private ImageView mAudioPlayStartPause;
        private ImageView mAudioPlayStop;
        private TextView mAudioPlayTitle;
        private TextView mAudioPlayTime;
        private TextView mAudioPlayTotalTime;
        private SeekBar mAudioPlaySeekBar;

        //audio lost
        private LinearLayout mAudioLostLayout;

        private RelativeLayout mAudioBorder;

        public NoteAudioView(View itemView) {
            super(itemView);
            initView(itemView);
        }

        @Override
        protected void delFile(String fileName) {
            //stop record or play
            int line = getItemPosition();
            if (line == getAudioRecordLine()) {
                mRecorderService.stopRecord();
                changeAudioRecordLine(-1);
                ((NoteEditorActivity) mContext).releaseRecordButton();
            } else if (line == getAudioPlayLine()) {
                mPlayerService.stopPlay();
                changeAudioPlayLine(-1);
            }
            FileUtils.delAudioFile(fileName);
        }

        private void initView(View itemView) {
            mAudioBorder = (RelativeLayout) itemView.findViewById(R.id.audio_border);
            mViewBorder = mAudioBorder;
            mEditBef = (EditTextHelper) itemView.findViewById(R.id.item_audio_edit_before);
            mEditAft = (EditTextHelper) itemView.findViewById(R.id.item_audio_edit_after);
            mEditBef.setOnEnterAndDelListener(this);
            mEditBef.setOnFocusChangeListener(this);
            mEditAft.setOnEnterAndDelListener(this);
            mEditAft.setOnFocusChangeListener(this);

            //audio record view
            mAudioRecordLayout = (LinearLayout) itemView.findViewById(R.id.layout_audio_record);
            mAudioRecordStartPause = (ImageView) itemView.findViewById(R.id.audio_record_start_pause);
            mAudioRecordStop = (ImageView) itemView.findViewById(R.id.audio_record_stop);
            mAudioRecordTime = (TextView) itemView.findViewById(R.id.audio_record_time);
            mAudioRecordWave = (ImageView) itemView.findViewById(R.id.audio_record_wave);
            mAudioRecordCircle = (FrameLayout) itemView.findViewById(R.id.audio_frame_circle);

            //audio show view
            mAudioShowLayout = (LinearLayout) itemView.findViewById(R.id.layout_audio_show);
            mAudioShowStart = (ImageView) itemView.findViewById(R.id.audio_show_start);
            mAudioShowTitle = (TextView) itemView.findViewById(R.id.audio_show_title);
            mAudioShowTime = (TextView) itemView.findViewById(R.id.audio_show_time);

            //audio paly view
            mAudioPlayLayout = (LinearLayout) itemView.findViewById(R.id.layout_audio_play);
            mAudioPlayStartPause = (ImageView) itemView.findViewById(R.id.audio_play_start_pause);
            mAudioPlayStop = (ImageView) itemView.findViewById(R.id.audio_play_stop);
            mAudioPlayTitle = (TextView) itemView.findViewById(R.id.audio_play_title);
            mAudioPlayTime = (TextView) itemView.findViewById(R.id.audio_play_time);
            mAudioPlayTotalTime = (TextView) itemView.findViewById(R.id.audio_play_total_time);
            mAudioPlaySeekBar = (SeekBar) itemView.findViewById(R.id.audio_play_seek_bar);
            mAudioPlaySeekBar.setPadding(0, mContext.getResources().getDimensionPixelSize(R.dimen.seek_bar_padding_top),
                    0, mContext.getResources().getDimensionPixelSize(R.dimen.seek_bar_padding_bottom));

            mAudioLostLayout = (LinearLayout) itemView.findViewById(R.id.audio_lost);

            mViewBorder.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ((NoteEditorActivity) mContext).startDrag(NoteAudioView.this);
                    return false;
                }
            });
        }

        public void init(boolean haveAnimation) {

            int line = getItemPosition();
            NoteLog.d(TAG, "init audio:" + line);
            NoteAudioData audioData = (NoteAudioData) mAllData.get(line);
            String fileName = audioData.getFileName();
            long duration = audioData.getDuration();
            NoteLog.d(TAG, "audio file name is" + fileName);
            if (FileUtils.isExits(FileUtils.getAudioWholePath(fileName))) {
                mAudioLostLayout.setVisibility(View.GONE);
            } else {
                mAudioLostLayout.setVisibility(View.VISIBLE);
                mAudioRecordLayout.setVisibility(View.GONE);
                mAudioShowLayout.setVisibility(View.GONE);
                mAudioPlayLayout.setVisibility(View.GONE);
                return;
            }
            if (line == getAudioPlayLine()) {
                mAudioRecordLayout.setVisibility(View.GONE);
                mAudioShowLayout.setVisibility(View.GONE);
                mAudioPlayLayout.setVisibility(View.VISIBLE);
                mAudioPlayTitle.setText(FileUtils.getFileNameNoSuffixes(fileName));
                mAudioPlayStartPause.setOnClickListener(this);
                mAudioPlayStop.setOnClickListener(this);
                mAudioPlaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mPlayerService.setSeekTo(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                mAudioPlaySeekBar.setMax((int) duration);
                mAudioPlayTotalTime.setText("/" + TimeUtils.formatTime(duration));
                mPlayerService.setOnRefreshUiListener(NoteAudioView.this);
                if (mPlayerService != null) {
                    int mState = mPlayerService.getCurrentState();
                    if (mState == PlayerService.STATE_IDLE || mState == PlayerService
                            .STATE_PAUSE_PLYING) {
                        mAudioPlayStartPause.setImageResource(R.drawable.audio_start);
                    } else if (mState == PlayerService.STATE_PLAYING) {
                        if (haveAnimation) {
                            AnimationDrawable playToPause = (AnimationDrawable) mContext.getDrawable(R.drawable.animation_pause_play);
                            mAudioPlayStartPause.setImageDrawable(playToPause);
                            playToPause.start();
                        } else {
                            mAudioPlayStartPause.setImageResource(R.drawable.audio_pause);
                        }
                    }
                    long time = mPlayerService.getCurrentTime();
                    onRefreshTimeUi(time);
                    onRefreshProgressUi(time);
                }
            } else if (line == getAudioRecordLine()) {
                if (mWaveAnima == null) {
                    mWaveAnima = (AnimationDrawable) mContext.getDrawable(R.drawable.animation_audio_record_wave);
                }
                mRecorderService.setOnStateChangeListener(this);
                stopPlayingAudioLine();
                mAudioShowLayout.setVisibility(View.GONE);
                mAudioPlayLayout.setVisibility(View.GONE);
                mAudioRecordLayout.setVisibility(View.VISIBLE);
                mAudioRecordStartPause.setOnClickListener(this);
                mAudioRecordStop.setOnClickListener(this);
                mWaveAnima.setCurrentIndex(mAnimaIndex);
                NoteLog.d(TAG, "animation index=" + mAnimaIndex);
                mAudioRecordWave.setImageDrawable(mWaveAnima);
                if (mRecorderService != null) {
                    int mState = mRecorderService.getState();
                    if (mState == SoundRecorderService.STATE_PAUSE_RECORDING) {
                        mAudioRecordStartPause.setImageResource(R.drawable.anim_record);
                        mAudioRecordWave.setImageDrawable(mWaveAnima.getFrame(mAnimaIndex));
                    } else if (mState == SoundRecorderService.STATE_RECORDING) {
                        mWaveAnima.start();

                        mAudioRecordStartPause.setImageResource(R.drawable.anim_play);
                    }
                    NoteEditorAdapter.this.onRefreshTimeUi(mRecorderService.getCurrentTime());
                }
            } else {
                mAudioRecordLayout.setVisibility(View.GONE);
                mAudioPlayLayout.setVisibility(View.GONE);
                mAudioShowLayout.setVisibility(View.VISIBLE);
                mAudioShowTitle.setText(FileUtils.getFileNameNoSuffixes(fileName));
                mAudioShowTime.setText(TimeUtils.formatTime(duration));
                mAudioShowStart.setOnClickListener(this);
            }
        }

        public void init() {
            init(false);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.audio_record_start_pause:
                    if (mRecorderService.getState() == SoundRecorderService.STATE_PAUSE_RECORDING) {
                        scaleRecordButton(R.drawable.anim_play);

                        mRecorderService.startRecord();
                        mWaveAnima.setCurrentIndex(mAnimaIndex);
                        if (mAudioRecordWave.getDrawable() instanceof AnimationDrawable) {

                        } else {
                            mAudioRecordWave.setImageDrawable(mWaveAnima);
                        }
                        mWaveAnima.start();

                    } else if (mRecorderService.getState() == SoundRecorderService.STATE_RECORDING) {

                        mRecorderService.pauseRecord();
                        pauseWave();
                    }
                    break;
                case R.id.audio_record_stop:
                    stopRecordLine();
                    mWaveAnima.stop();
                    changeToShowByAnima();
                    break;
                case R.id.audio_show_start:
                    stopPlayingAudioLine();
                    String fileName = ((NoteAudioData) mAllData.get(getItemPosition())).getFileName();
                    if (!FileUtils.isCanPlay(fileName)) {
                        ToastHelper.show(mContext, R.string.toast_audio_cannot_play);
                    }
                    Intent intent = new Intent(mContext, PlayerService.class);
                    intent.putExtra(PlayerService.KEY_FILE, fileName);
                    if (mPlayConnection != null) {
                        mContext.unbindService(mPlayConnection);
                        mPlayConnection = null;
                    }
                    mPlayConnection = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            if (service != null) {
                                mPlayerService = ((PlayerService.PlayerBinder) service).getService();
                                mPlayerService.setOnRefreshUiListener(NoteAudioView.this);
                                mPlayerService.startPlay();

                                changeAudioPlayLine(getItemPosition());
                                init(true);
                            }
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {

                        }
                    };
                    mContext.bindService(intent, mPlayConnection, Context.BIND_AUTO_CREATE);
                    break;
                case R.id.audio_play_start_pause:
                    int mState = mPlayerService.getCurrentState();
                    if (mState == PlayerService.STATE_IDLE || mState == PlayerService
                            .STATE_PAUSE_PLYING) {
                        AnimationDrawable pauseToPlay = (AnimationDrawable) mContext.getDrawable(R.drawable.animation_pause_play);
                        mAudioPlayStartPause.setImageDrawable(pauseToPlay);
                        pauseToPlay.start();
                        mPlayerService.startPlay();
                    } else if (mState == PlayerService.STATE_PLAYING) {
                        AnimationDrawable playToPause = (AnimationDrawable) mContext.getDrawable(R.drawable.animation_play_pause);
                        mAudioPlayStartPause.setImageDrawable(playToPause);
                        playToPause.start();
                        mPlayerService.pausePlay();
                    }
                    break;
                case R.id.audio_play_stop:
                    stopPlayingAudioLine();
                    break;
            }
        }

        private void pauseWave() {
            mWaveAnima.stop();
            mAnimaIndex = mWaveAnima.getCurrentIndex();
        }

        private void scaleRecordButton(int id) {
            ValueAnimator scaleAnim1 = ValueAnimator.ofFloat(1f, 0.5f);
            scaleAnim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float scale = (float) valueAnimator.getAnimatedValue();
                    mAudioRecordStartPause.setScaleX(scale);
                    mAudioRecordStartPause.setScaleY(scale);
                }
            });
            scaleAnim1.setInterpolator(new AccelerateDecelerateInterpolator());
            ValueAnimator scaleAnim2 = ValueAnimator.ofFloat(0.5f, 1f);
            scaleAnim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float scale = (float) valueAnimator.getAnimatedValue();
                    mAudioRecordStartPause.setScaleX(scale);
                    mAudioRecordStartPause.setScaleY(scale);
                }
            });
            scaleAnim2.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mAudioRecordStartPause.setImageResource(id);
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

        private void changeToShowByAnima() {
            int line = getItemPosition();
            NoteAudioData audioData = (NoteAudioData) mAllData.get(line);
            String fileName = audioData.getFileName();
            long duration = audioData.getDuration();
            mAudioShowLayout.setVisibility(View.VISIBLE);
            mAudioShowTitle.setText(FileUtils.getFileNameNoSuffixes(fileName));
            mAudioShowTime.setText(TimeUtils.formatTime(duration));
            mAudioShowStart.setOnClickListener(this);
            setShowViewAlpha(0);
            mAudioShowStart.setAlpha(0);


            //hide & show text
            final int time = 231;
            ValueAnimator hideShowAnim = ValueAnimator.ofFloat(0f, 1f);
            hideShowAnim.setDuration(time);
            hideShowAnim.setInterpolator(new LinearInterpolator());
            hideShowAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float alpha = (float) animation.getAnimatedValue();
                    setShowViewAlpha(alpha);
                    setRecordViewAlpha(1f - alpha);
                }
            });
            hideShowAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnableButton(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setEnableButton(true);
                    mAudioShowStart.setImageAlpha(255);
                    mAudioRecordStop.setImageAlpha(0);
                    mAudioRecordLayout.setVisibility(View.GONE);
                    init();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });


            ObjectAnimator recordPauseAnim = ObjectAnimator.ofFloat(mAudioRecordCircle, "alpha", 1f, 0f);
            recordPauseAnim.setDuration(time / 2);

            int transX = 0 - mAudioRecordStop.getLeft();
            NoteLog.d(TAG, "translationX transX=" + transX);
            ObjectAnimator moveAnim = ObjectAnimator.ofFloat(mAudioRecordStop, "translationX", 0f, transX);
            moveAnim.setInterpolator(new LinearInterpolator());
            moveAnim.setDuration(time);

            AnimationDrawable stopPlayAnim = (AnimationDrawable) mContext.getDrawable(R.drawable.animation_stop_play);
            mAudioRecordStop.setImageDrawable(stopPlayAnim);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(hideShowAnim).with(moveAnim).with(recordPauseAnim);
            animatorSet.start();
            stopPlayAnim.start();
        }

        private void setEnableButton(boolean enable) {
            mAudioRecordStop.setEnabled(enable);
            mAudioRecordStartPause.setEnabled(enable);
            mAudioShowStart.setEnabled(enable);
        }

        private void setShowViewAlpha(float alpha) {
            mAudioShowTitle.setAlpha(alpha);
            mAudioShowTime.setAlpha(alpha);
//            mAudioShowStart.setAlpha(alpha);
        }

        private void setRecordViewAlpha(float alpha) {
//            mAudioRecordStartPause.setAlpha(alpha);
            mAudioRecordTime.setAlpha(alpha);
//            mAudioRecordStop.setAlpha(alpha);
            mAudioRecordWave.setAlpha(alpha);
        }

        @Override
        public void onRefreshTimeUi(long time) {
            NoteLog.d(TAG, "refresh time=" + TimeUtils.formatTime(time));
            mAudioPlayTime.setText(TimeUtils.formatTime(time));
        }

        @Override
        public void onRefreshProgressUi(long progress) {
            mAudioPlaySeekBar.setProgress((int) progress);
        }

        @Override
        public void onCompletionPlay() {
            stopPlayingAudioLine();
        }

        @Override
        public void onPausePlay() {
            int mState = mPlayerService.getCurrentState();
            if (mState == PlayerService.STATE_PLAYING) {
                mAudioPlayStartPause.setImageResource(R.drawable.audio_start);
                mPlayerService.pausePlay();
            }
        }

        @Override
        public void onStateChange(int stateCode) {
            if (stateCode == SoundRecorderService.STATE_PAUSE_RECORDING) {
                scaleRecordButton(R.drawable.anim_record);
                pauseWave();
            }
        }

        @Override
        public void onItemSelected() {
            super.onItemSelected();
            mAudioBorder.setForeground(mContext.getDrawable(R.drawable.move_audio_show));
        }

        @Override
        public void onItemClear() {
            super.onItemClear();
            mAudioBorder.setForeground(null);
        }
    }

}
