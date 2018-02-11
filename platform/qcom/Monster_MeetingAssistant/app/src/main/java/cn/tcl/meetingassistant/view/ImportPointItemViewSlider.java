/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import cn.tcl.meetingassistant.EditImportPointActivity;
import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.bean.ImportPoint;
import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.db.ImportPointDBUtil;
import cn.tcl.meetingassistant.db.OnDoneInsertAndUpdateListener;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.DensityUtil;
import cn.tcl.meetingassistant.utils.FileUtils;
import cn.tcl.meetingassistant.utils.ImageLoader;
import mst.view.menu.BottomWidePopupMenu;
import mst.widget.SliderLayout;
import mst.widget.SliderView;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-16.
 * view of the import point item
 */
public class ImportPointItemViewSlider extends LinearLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private String TAG = ImportPointItemViewSlider.class.getSimpleName();

    // if this view is saving it's data ,set this value to true.
    // if saving has done,set this.data to false.
    // like a lock
    private boolean isSaving = false;

    private boolean isSaved = true;

    // the string change length whether save text or not
    private final int SAVE_MAX_LENGTH = 20;
    private int mNoSaveLength = 0;
    private boolean isNew = false;
    private boolean isInitFirst = true;
    private boolean isShown = false;
    private final int PADDING_LEFT_SINGLE = 18;
    private final int PADDING_LEFT_NO_SINGLE = 12;
    private final int PADDING_RIGHT_SINGLE = 12;
    private final int PADDING_RIGHT_NO_SINGLE = 6;
    private final int PADDING_BOTTOM_12 = 12;
    private final int PADDING_BOTTOM_6 = 6;

    private final int PADDING_EXPAND_SINGLE = 34;
    private final int PADDING_EXPAND_NOT_SINGLE = 28;

    // this view's background
    private static final int BACKGROUND_TOP_R = R.drawable.bg_import_point_card_up;
    private static final int BACKGROUND_MID_R = R.drawable.bg_import_point_card_mid;
    private static final int BACKGROUND_BOTTOM_R = R.drawable.bg_import_point_card_down;
    private static final int BACKGROUND_SINGLE_R = R.drawable.bg_import_point_card_single;

    public enum BACKGROUND {
        BACKGROUND_TOP, BACKGROUND_MID, BACKGROUND_BOTTOM, BACKGROUND_SINGLE
    }

    private ExpandEditTextView mEditText;

    private ImageView mImage1;
    private ImageView mImage2;
    private ImageView mImage3;
    private CircleImageView mImageMore;
    private ImageButton mImageCamera;
    private TextView mMoreText;
    private View mMoreLayout;
    private View mContainerView;
    private TextView mExpandTextView;
    private SilderView mSliderView;
    private View mContentView;
    private View mCircleBtnsContainer;

    private ImportPoint mImportPoint;
    private File[] mImagePaths;

    public int position = 0;

    private ImportPointAdapter mParentViewAdapter;

    private EditImportPointActivity mContainerActivity;

    private BottomWidePopupMenu mPopMenu;

    private boolean isFocused = false;

    public ImportPointItemViewSlider(Context context) {
        this(context, null);

    }

    public ImportPointItemViewSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater layoutInflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContainerView = layoutInflater.inflate(R.layout.item_import_point_slider_layout, this, true);
        mEditText = (ExpandEditTextView) mContainerView.findViewById(R.id.item_import_point_edit);
        mImage1 = (ImageView) mContainerView.findViewById(R.id.item_import_point_image_1);
        mImage2 = (ImageView) mContainerView.findViewById(R.id.item_import_point_image_2);
        mImage3 = (ImageView) mContainerView.findViewById(R.id.item_import_point_image_3);
        mImageMore = (CircleImageView) mContainerView.findViewById(R.id.item_import_point_image_more);
        mImageCamera = (ImageButton) mContainerView.findViewById(R.id.item_import_point_camera_btn);
        mMoreText = (TextView) mContainerView.findViewById(R.id.item_import_point_image_more_text);
        mMoreLayout = mContainerView.findViewById(R.id.item_import_point_image_more_layout);
        mExpandTextView = (TextView) mContainerView.findViewById(R.id.item_import_point_expand_text);
        mSliderView = (SilderView) mContainerView.findViewById(R.id.import_item_item_sliderView);
        mContentView = mContainerView.findViewById(R.id.item_import_point_slider_content);
        mCircleBtnsContainer = mContainerView.findViewById(R.id.item_import_point_circleBtn_container);
        mExpandTextView.setOnClickListener(this);
        mImage1.setOnClickListener(this);
        mImage2.setOnClickListener(this);
        mImage3.setOnClickListener(this);
        mImageMore.setOnClickListener(this);
        mImageCamera.setOnClickListener(this);

        mEditText.setOnExpandAbleListener(new ExpandEditTextView.OnExpandAbleListener() {
            @Override
            public void onExpandAble(boolean expandable) {
                if (expandable) {
                    mExpandTextView.setVisibility(VISIBLE);
                } else {
                    mExpandTextView.setVisibility(INVISIBLE);
                }
            }
        });

        mEditText.setOnExpandStatusChangedListener(new ExpandEditTextView.OnExpandStatusChangedListener() {
            @Override
            public void onExpandStatusChanged(boolean isExpand) {
                MeetingLog.i(TAG,"onExpandStatusChanged isExpand " + isExpand);
                if (isExpand) {
                    mParentViewAdapter.getExpandHelper().expand(mImportPoint);
                    mExpandTextView.setText(R.string.collapse);
                } else {
                    mParentViewAdapter.getExpandHelper().collapse(mImportPoint);
                    mExpandTextView.setText(R.string.expand);
                }
                if(MeetingStaticInfo.getCurrentMeeting().getImportPoints().size() == 1){
                    mSliderView.setLockDrag(true);
                }else {
                    mSliderView.setLockDrag(isExpand);
                }
            }
        });


        mEditText.addTextChangedListener(new SpannableTextWatcher(getContext()).setGap(12));

        mEditText.setOnFocusChangeListener(this);

        mEditText.setScroller(null);

        initSliderViewFnc();
    }

    private void initSliderViewFnc(){
        mSliderView.addTextButton(0,getContext().getString(R.string.delete));
        mSliderView.setOnSliderButtonClickListener(new SliderView.OnSliderButtonLickListener() {
            @Override
            public void onSliderButtonClick(int i, View view, ViewGroup viewGroup) {
                if(i == 0){
                    DialogHelper.showDialog(getContext(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(i == DialogInterface.BUTTON_NEGATIVE){
                                // do nothing
                            }else if(i == DialogInterface.BUTTON_POSITIVE){
                                deleteImportItem();
                            }else {
                                // do nothing
                            }
                        }
                    },R.string.dialog_back_title,R.string.delete_meeting_point,R.string.Confirm,R.string.cancel);

                }
            }
        });
        mSliderView.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
    }

    public ImportPointItemViewSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public ImportPointItemViewSlider(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs);
    }

    public void setActivity(EditImportPointActivity activity){
        mContainerActivity = activity;
    }

    @Override
    public void onClick(View view) {
        String path = FileUtils.getImageDirPath() + File.separator +
                mImportPoint.getCreatTime();
        switch (view.getId()) {
            case R.id.item_import_point_image_1:
                MeetingLog.i(TAG,"click image button 1");
                mContainerActivity.goToScanPage(path,1);
                break;
            case R.id.item_import_point_image_2:
                MeetingLog.i(TAG,"click image button 2");
                mContainerActivity.goToScanPage(path,2);
                break;
            case R.id.item_import_point_image_3:
                MeetingLog.i(TAG,"click image button 3");
                mContainerActivity.goToScanPage(path,3);
                break;
            case R.id.item_import_point_image_more:
                MeetingLog.i(TAG,"click image button 4");
                mContainerActivity.goToScanPage(path,4);
                break;
            case R.id.item_import_point_camera_btn:
                MeetingLog.i(TAG,"click camera button");
                showPicMenu();
                break;
            case R.id.item_import_point_expand_text:
                MeetingLog.i(TAG,"click expand textView");
                if(mEditText.isExpand){
                    editLoseFocus();
                    mEditText.collapse(new ExpandEditTextView.OnAnimatorEndListener() {
                        @Override
                        public void onEnd() {
                            mExpandTextView.setText(R.string.expand);
                            //transfer focus
                            MeetingLog.d(TAG,mImportPoint.getId() + " EditText finish collapse");
                            refreshSliderStatus();
                            if(MeetingStaticInfo.getCurrentMeeting().getImportPoints().size() > 1){
                                mSliderView.setLockDrag(false);
                            }
                        }
                    });
                }else{
                    isFocused = true;
                    mEditText.expand(new ExpandEditTextView.OnAnimatorEndListener() {
                        @Override
                        public void onEnd() {
                            //editRequestFocus();
                            mExpandTextView.setText(R.string.collapse);
                            mEditText.setSelection(mEditText.getTextString().length());
                            MeetingLog.d(TAG,mImportPoint.getId() + " EditText finish expand");
                            refreshSliderStatus();
                            mSliderView.setLockDrag(true);
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        MeetingLog.d(TAG,mImportPoint.getId() + " start onAttachedToWindow");
        super.onAttachedToWindow();
        if(!isFocused){
            MeetingLog.d(TAG,mImportPoint.getId() + " onAttachedToWindow -- is unfocused");
            editLoseFocus();
        }
        if((MeetingStaticInfo.getCurrentMeeting().getImportPoints().size() == 1 && isInitFirst &&
                MeetingStaticInfo.getCurrentMeeting().getMeetingVoices().size() == 0) || isNew){
            mEditText.expand(new ExpandEditTextView.OnAnimatorEndListener() {
                @Override
                public void onEnd() {
                    MeetingLog.d(TAG,mImportPoint.getId() + " onAttachedToWindow -- Add the first item for meeting and gain focus");
                    editRequestFocus();
                    isNew = false;
                    isInitFirst = false;
                    mEditText.setSelection(mEditText.getTextString().length());
                    mExpandTextView.setText(R.string.collapse);
                    refreshSliderStatus();
                }
            });
        }
        MeetingLog.d(TAG,mImportPoint.getId() + " end onAttachedToWindow");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setParentViewAdapter(ImportPointAdapter ParentViewAdapter) {
        mParentViewAdapter = ParentViewAdapter;
    }

    public void deleteImportItem(){
        MeetingLog.i(TAG,"deleteImportItem start before,import size is " + MeetingStaticInfo.getCurrentMeeting().getImportPoints().size());
        //delete pics
        String  dirPath = FileUtils.getImageDirPath() + mImportPoint.getCreatTime();
        File file  = new File(dirPath);
        boolean isDelete = true;
        isDelete =  FileUtils.deleteDir(file);
        MeetingLog.d(TAG,"delete file " + file.getAbsolutePath() + " is " + isDelete);

        if (isDelete){
            ImportPointDBUtil.delete(mImportPoint.getId(),mContainerActivity,new ImportPointDBUtil.OnDoneDeletedListener(){
                @Override
                public void postDeleted(boolean isSuccess) {
                    MeetingLog.i(TAG,"deleteImportItem task end ,result is " + isSuccess);
                    if (isSuccess){
                        MeetingStaticInfo.getCurrentMeeting().getImportPoints().remove(mImportPoint);
                        mParentViewAdapter.notifyDataSetChanged();
                    }
                    MeetingStaticInfo.updateCurrentTime(getContext());
                    MeetingLog.i(TAG,"deleteImportItem task end ,import size is " + MeetingStaticInfo.getCurrentMeeting().getImportPoints().size());
                }
            });
        }
    }

    private void refreshSliderStatus(){
        MeetingLog.i(TAG,"deleteImportItem done left " + MeetingStaticInfo.getCurrentMeeting().getImportPoints().size());
        if(MeetingStaticInfo.getCurrentMeeting().getImportPoints().size() == 1){
            mSliderView.close(true);
            mSliderView.setLockDrag(true);
        }else {
            mSliderView.setLockDrag(false);
        }
        mSliderView.setSwipeListener(null);
        if(mParentViewAdapter.containASlider(mImportPoint)){
            mSliderView.open(true);
            mParentViewAdapter.setmSliderView(mSliderView);
            MeetingLog.i(TAG,"mSliderView open  " + mImportPoint.getId());
        }else {
            mSliderView.close(true);
            MeetingLog.i(TAG,"mSliderView close " + mImportPoint.getId());
        }

        mSliderView.setSwipeListener(new SliderLayout.SimpleSwipeListener(){
            @Override
            public void onClosed(SliderLayout sliderLayout) {
                MeetingLog.d(TAG, "mSliderView onClosed " + mImportPoint.getId());
                if(mParentViewAdapter.getmSliderView() == mSliderView){
                    MeetingLog.d(TAG, "mSliderView setmSliderView null " + mImportPoint.getId());
                    mParentViewAdapter.setmSliderView(null);
                }
                setSliderStatus(false);
            }

            @Override
            public void onOpened(SliderLayout sliderLayout) {
                MeetingLog.d(TAG, "mSliderView onOpened " + mImportPoint.getId());
                if (mParentViewAdapter.getmSliderView() != null) {
                    mParentViewAdapter.getmSliderView().close(true);
                }
                mParentViewAdapter.setmSliderView(mSliderView);
                setSliderStatus(true);
                MeetingLog.d(TAG, "mSliderView onOpened " + mImportPoint.getId() + " end");
            }
        });
    }

    /**
     * get the image paths
     */
    private void initImagePaths() {
        // get the files
        mImagePaths = FileUtils.getImageFilesByTime(FileUtils.IMAGE_FILE_PATH +
                mImportPoint.getCreatTime());
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if(visibility == View.VISIBLE){
            MeetingLog.d(TAG,"onWindowVisibilityChanged reload images");
            showImages();
        }
    }

    /**
     * load images
     */
    private void showImages() {
        initImagePaths();

        ImageLoader imageLoader = ImageLoader.getInstance(ImageLoader.IMG_16,getContext());
        if (null == mImagePaths) {
            mImage1.setVisibility(View.GONE);
            mImage2.setVisibility(View.GONE);
            mImage3.setVisibility(View.GONE);
            mMoreLayout.setVisibility(View.GONE);
            return;
        }
        if (mImagePaths.length == 0) {
            mImage1.setVisibility(View.GONE);
            mImage2.setVisibility(View.GONE);
            mImage3.setVisibility(View.GONE);
            mMoreLayout.setVisibility(View.GONE);
        } else if (mImagePaths.length == 1) {
            mImage1.setVisibility(View.VISIBLE);
            mImage2.setVisibility(View.GONE);
            mImage3.setVisibility(View.GONE);
            mMoreLayout.setVisibility(View.GONE);

            imageLoader.loadBitmap(mImage1,mImagePaths[0].getPath(),false);
        } else if (mImagePaths.length == 2) {
            mImage1.setVisibility(View.VISIBLE);
            mImage2.setVisibility(View.VISIBLE);
            mImage3.setVisibility(View.GONE);
            mMoreLayout.setVisibility(View.GONE);

            imageLoader.loadBitmap(mImage1,mImagePaths[0].getPath(),false);
            imageLoader.loadBitmap(mImage2,mImagePaths[1].getPath(),false);
        } else if (mImagePaths.length == 3) {
            mImage1.setVisibility(View.VISIBLE);
            mImage2.setVisibility(View.VISIBLE);
            mImage3.setVisibility(View.VISIBLE);
            mMoreLayout.setVisibility(View.GONE);
            imageLoader.loadBitmap(mImage1,mImagePaths[0].getPath(),false);
            imageLoader.loadBitmap(mImage2,mImagePaths[1].getPath(),false);
            imageLoader.loadBitmap(mImage3,mImagePaths[2].getPath(),false);
        } else {
            mImage1.setVisibility(View.VISIBLE);
            mImage2.setVisibility(View.VISIBLE);
            mImage3.setVisibility(View.VISIBLE);
            mMoreLayout.setVisibility(View.VISIBLE);

            imageLoader.loadBitmap(mImage1,mImagePaths[0].getPath(),false);
            imageLoader.loadBitmap(mImage2,mImagePaths[1].getPath(),false);
            imageLoader.loadBitmap(mImage3,mImagePaths[2].getPath(),false);
            imageLoader.loadBitmap(mImageMore,mImagePaths[3].getPath(),false);
            mMoreText.setText(String.valueOf(mImagePaths.length - 3));
            if (mImagePaths.length==4){
                mImageMore.setHasShadow(false);
                mMoreText.setVisibility(View.GONE);
            }else {
                mImageMore.setHasShadow(true);
                mMoreText.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * the import point object which this view has to bind.
     *
     * @param mImportPoint the import point data the view should contain
     */
    public void setImportPoint(ImportPoint mImportPoint) {
        this.mImportPoint = mImportPoint;
        initContentData();
        refreshSliderStatus();
    }

    /**
     * if the view is out of window and the data has not been saved,
     * save it.
     */
    @Override
    protected void onDetachedFromWindow() {
        MeetingLog.i(TAG,mImportPoint.getId() + " start onDetachedFromWindow");
        super.onDetachedFromWindow();
        if(!isSaved && mEditText.isFocused()){
            saveThisViewContentToDB();
        }
        MeetingLog.i(TAG,mImportPoint.getId() + " end onDetachedFromWindow");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        MeetingLog.i(TAG,mImportPoint.getId() + " start dispatchKeyEvent");
        MeetingLog.i(TAG,"import point " + position + " : OnkeyDown");
        if(mEditText.isFocused()){
            if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
                saveThisViewContentToDB();
                editLoseFocus();
                //mEditText.setFocusable(false);
                MeetingLog.i(TAG, "import point " + position + " : OnkeyDown save to db");
            }
        }
        MeetingLog.i(TAG,mImportPoint.getId() + " start dispatchKeyEvent");
        return super.dispatchKeyEvent(event);
    }

    /**
     * if the edit text lost focus,call back this method
     *
     * @param view
     * @param b
     */
    @Override
    public void onFocusChange(View view, boolean b) {
        MeetingLog.d(TAG,mImportPoint.getId() +" start onFocusChange");
        MeetingLog.d(TAG,mImportPoint.getId() + " item get focus is " + b);
        if (!b) {
            saveThisViewContentToDB();
            mContainerActivity.showBottomBtns(mEditText);
           // editLoseFocus();
           // mEditText.collapse(null);
        }else{
            //isSaved = false;
            mContainerActivity.hideBottomBtns(mEditText);
            if(!mEditText.isExpand){
                mEditText.expand(null);
            }
        }
        MeetingLog.d(TAG,mImportPoint.getId() + " end onFocusChange");
    }

    /**
     * save this view's all content to DB
     */
    public void saveThisViewContentToDB() {
        if (isSaving) {
            return;
        } else {
            isSaving = !isSaving;
        }
        if(isSaved){
            return;
        }

        if (mImportPoint != null) {
            mImportPoint.setInfoContent(mEditText.getTextString());
            // make sure the meeting id is right number
            mImportPoint.setMeetingId(MeetingStaticInfo.getCurrentMeeting().getId());

            MeetingLog.i(TAG, "position:" + position + ":update a import point -->id: " + mImportPoint.getId() +
                    "content ---> " + mImportPoint.getInfoContent());
            ImportPointDBUtil.update(mImportPoint, getContext(), new OnDoneInsertAndUpdateListener() {
                @Override
                public void onDone(long id) {
                    isSaving = false;
                    //do nothing
                    isSaved = true;
                    MeetingLog.d(TAG,"import point saved result code is "+ id);
                    mNoSaveLength = mNoSaveLength % SAVE_MAX_LENGTH;

                }
            });
            MeetingStaticInfo.updateCurrentTime(getContext());
        }
    }

    public void setBackground(BACKGROUND background) {
        switch (background){
            case BACKGROUND_TOP:
                mEditText.setPadding(DensityUtil.dip2px(getContext(), PADDING_LEFT_NO_SINGLE),mEditText.getPaddingTop(),
                        DensityUtil.dip2px(getContext(), PADDING_LEFT_NO_SINGLE), mEditText.getPaddingBottom());
                mCircleBtnsContainer.setPadding(DensityUtil.dip2px(getContext(),PADDING_EXPAND_NOT_SINGLE),mCircleBtnsContainer.getPaddingTop(),
                        DensityUtil.dip2px(getContext(),PADDING_RIGHT_NO_SINGLE), DensityUtil.dip2px(getContext(), PADDING_BOTTOM_6));
                mContentView.setBackgroundResource(BACKGROUND_TOP_R);
                break;
            case BACKGROUND_MID:
                mEditText.setPadding(DensityUtil.dip2px(getContext(), PADDING_LEFT_NO_SINGLE),mEditText.getPaddingTop(),
                        DensityUtil.dip2px(getContext(), PADDING_LEFT_NO_SINGLE), mEditText.getPaddingBottom());
                mCircleBtnsContainer.setPadding(DensityUtil.dip2px(getContext(),PADDING_EXPAND_NOT_SINGLE),mCircleBtnsContainer.getPaddingTop(),
                        DensityUtil.dip2px(getContext(),PADDING_RIGHT_NO_SINGLE), DensityUtil.dip2px(getContext(), PADDING_BOTTOM_6));
                mContentView.setBackgroundResource(BACKGROUND_MID_R);
                break;
            case BACKGROUND_BOTTOM:
                mEditText.setPadding(DensityUtil.dip2px(getContext(), PADDING_LEFT_NO_SINGLE),mEditText.getPaddingTop(),
                        DensityUtil.dip2px(getContext(), PADDING_LEFT_NO_SINGLE), mEditText.getPaddingBottom());
                mCircleBtnsContainer.setPadding(DensityUtil.dip2px(getContext(),PADDING_EXPAND_NOT_SINGLE),mCircleBtnsContainer.getPaddingTop(),
                        DensityUtil.dip2px(getContext(),PADDING_RIGHT_NO_SINGLE), DensityUtil.dip2px(getContext(), PADDING_BOTTOM_12));
                mContentView.setBackgroundResource(BACKGROUND_BOTTOM_R);
                break;
            case BACKGROUND_SINGLE:
                mEditText.setPadding(DensityUtil.dip2px(getContext(),PADDING_LEFT_SINGLE),mEditText.getPaddingTop(),
                        DensityUtil.dip2px(getContext(),PADDING_LEFT_SINGLE), mEditText.getPaddingBottom());
                mCircleBtnsContainer.setPadding(DensityUtil.dip2px(getContext(),PADDING_EXPAND_SINGLE),mCircleBtnsContainer.getPaddingTop(),
                        DensityUtil.dip2px(getContext(),PADDING_RIGHT_SINGLE), DensityUtil.dip2px(getContext(),PADDING_BOTTOM_6));
                mContentView.setBackgroundResource(BACKGROUND_SINGLE_R);
                break;
            default:
                // do nothing
                break;
        }
    }

    private void initContentData() {
        if (null != mImportPoint) {
            mEditText.setText(mImportPoint.getInfoContent());
            mEditText.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            if(mImportPoint.getInfoContent().split("\n").length > 3){
                mExpandTextView.setVisibility(VISIBLE);
            }
            if(mParentViewAdapter.getExpandHelper().isExpand(mImportPoint)){
                mEditText.setExpand();
                mExpandTextView.setText(R.string.collapse);
            }else {
                mEditText.setCollapse();
                mExpandTextView.setText(R.string.expand);
            }
            isSaving = false;
            isSaved = true;
            // set the real time backup
            realTimeBackup();
        }
    }

    /**
     * show the pic menu
     */
    private void showPicMenu() {
        if (mPopMenu == null) {
            mPopMenu = new BottomWidePopupMenu(getContext());
            mPopMenu.inflateMenu(R.menu.important_point_bottom_menu);
            mPopMenu.setOnMenuItemClickedListener(new BottomWidePopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onItemClicked(MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.import_take_photo_menu_item:
                            mContainerActivity.goToCamera(FileUtils.getImageDirPath() + mImportPoint.getCreatTime());
                            break;
                        case R.id.import_take_photo_from_gallery_item:
                            mContainerActivity.getCheckPermissionAndGetPic(FileUtils.getImageDirPath() + mImportPoint.getCreatTime());
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });
        }
        mPopMenu.setCanceledOnTouchOutside(true);
        mPopMenu.show();
    }

    /**
     * set the backup function for changing length of text
     */
    private void realTimeBackup(){
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int oldCount, int newCount) {
                mNoSaveLength += Math.abs(newCount - oldCount) ;
                isSaved = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                MeetingLog.d(TAG,"length " + mNoSaveLength);
                MeetingLog.d(TAG,"isSaved " + isSaved);
                MeetingLog.d(TAG,"isSaving " + isSaving);
                if(mNoSaveLength >= SAVE_MAX_LENGTH){
                    saveThisViewContentToDB();
                }
            }
        });
    }

    public void editRequestFocus(){
        MeetingLog.d(TAG,mImportPoint.getId() + " start editRequestFocus");
        mEditText.setFocusable(true);
        mEditText.requestFocus();
        isFocused = true;
        mEditText.setSelection(mEditText.getTextString().length());
        MeetingLog.d(TAG,mImportPoint.getId() + " end editRequestFocus");
    }

    public void setItemIsNew(){
        isNew = true;
    }

    public void editLoseFocus(){
        MeetingLog.d(TAG,mImportPoint.getId() + " start editLoseFocus");
        mContainerView.setFocusable(true);
        mContainerView.setFocusableInTouchMode(true);
        mContainerView.requestFocus();
        isFocused = false;
        MeetingLog.d(TAG,mImportPoint.getId() + " end editLoseFocus");
    }

    private void setSliderStatus(boolean isSlider){
        if(isSlider){
            mParentViewAdapter.addASlider(mImportPoint);
        }else{
            mParentViewAdapter.removeASlider(mImportPoint);
        }
    }
}
