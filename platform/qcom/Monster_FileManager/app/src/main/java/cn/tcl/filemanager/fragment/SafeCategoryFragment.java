/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
/*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1909322*/
import android.text.Editable;
/*MODIFIED-END by haifeng.tang,BUG-1909322*/
/* MODIFIED-BEGIN by wenjing.ni, 2016-05-11,BUG-2121823*/
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
/* MODIFIED-END by wenjing.ni,BUG-2121823*/
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager; // MODIFIED by haifeng.tang, 2016-04-21,BUG-1940832
import android.widget.AdapterView;
import android.widget.Button; //MODIFIED by haifeng.tang, 2016-04-15,BUG-1950773
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView; //MODIFIED by haifeng.tang, 2016-04-09,BUG-1909322
import android.widget.RelativeLayout;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.IActivitytoCategoryListener;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.activity.FileBrowserActivity;
import cn.tcl.filemanager.activity.FileSafeBrowserActivity;
import cn.tcl.filemanager.adapter.SafeCategoryAdapter;
import cn.tcl.filemanager.manager.CategoryManager;

/**
 * Created by user on 16-3-10.
 */
public class SafeCategoryFragment extends FileBrowserFragment implements AdapterView.OnItemClickListener, IActivitytoCategoryListener {

    protected FileManagerApplication mApplication;
    private CategoryFragmentListener mCategoryFragmentListener;
    private static final int DATA_UPDATED = 100;
    private String mMode = FileSafeBrowserActivity.CATEGORY_TAG;

    private Activity mActivity;
    private Context mContext;
    private Resources mResources;
    private GridView mGridView;
    private SafeCategoryAdapter mAdapter;
    public EditText mPasswordEdit;
    private Button mDestoryBtn; //MODIFIED by haifeng.tang, 2016-04-15,BUG-1950773
    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1909322*/
    private RelativeLayout mPasswordLayout;
    private ImageView showPassword;
    /*MODIFIED-END by haifeng.tang,BUG-1909322*/
    private boolean mFirstSafebox = false; // MODIFIED by wenjing.ni, 2016-05-13,BUG-2003636

    /* MODIFIED-BEGIN by wenjing.ni, 2016-05-03,BUG-802835*/
    public SafeCategoryFragment(){
    }
    /* MODIFIED-END by wenjing.ni,BUG-802835*/
    public SafeCategoryFragment(String mModeTag) {
        mMode = mModeTag;
    }


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        mContext = this.getActivity().getApplicationContext();
//
//        return inflater.inflate(R.layout.fragment_safe_category, container, false);
//    }


    @Override
    public void setHideInputMethod(FileBrowserActivity.HideInputMethodListener hideInputMethodListener) {

    }
    @Override
    public void refreshCategory() {
        refreshCategoryAdapter(); // MODIFIED by wenjing.ni, 2016-05-13,BUG-2003636
    }

    @Override
    public void onScannerStarted() {

    }

    @Override
    public void onScannerFinished() {

    }

    @Override
    public void disableCategoryEvent(boolean disable) {

    }


    public interface CategoryFragmentListener {
        public void switchCategoryList();
        //public void switchSafeCategoryList();

        public void updateCategoryNormalBarView();

        public void updateSafeCategory(); // MODIFIED by wenjing.ni, 2016-05-13,BUG-2003636

        //public void notifyCategoryDone(boolean isDone);

        //public void LandShowSize(PathProgressLayout mPhoneSize, PathProgressLayout mSDSize, PathProgressLayout mExternalSize);
    }


    @Override
    public void onAttach(Activity activity) {
        Log.d("niky", ">> onAttach(), activity= " + activity);
        try {
            mCategoryFragmentListener = (CategoryFragmentListener) activity;
            mActivity = activity;
            Log.d("niky", ">> onAttach(), mActivity= " + mActivity);
            mApplication = (FileManagerApplication) mActivity
                    .getApplicationContext();
        } catch (Exception e) {
            throw new ClassCastException(activity.toString()
                    + "must implement CategoryFragmentListener");
        }
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2003636*/
        Intent intent = mActivity.getIntent();
        if(intent != null){
            mFirstSafebox = intent.getBooleanExtra("FirstSafebox",false);
        }
        /* MODIFIED-END by wenjing.ni,BUG-2003636*/
        mContext = this.getActivity().getApplicationContext();
        mResources = mContext.getResources();
        setHasOptionsMenu(true);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDataContentObserver = new DataContentObserver(mHandler, mContext);
        //LogUtils.i(TAG, "onViewCreated");
        mGridView = (GridView) view.findViewById(R.id.safe_category_view);
        mGridView.setOnItemClickListener(this);
        mResources = mContext.getResources();
        mAdapter = new SafeCategoryAdapter(mActivity);

        mPasswordEdit = (EditText) view.findViewById(R.id.destory_safe_edit);
        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-15,BUG-1950773*/
        mDestoryBtn = (Button) view.findViewById(R.id.destory_safe_box);
        mDestoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileSafeBrowserActivity fileSafeBrowserActivity = (FileSafeBrowserActivity) mActivity;
                fileSafeBrowserActivity.destorySafeBox();
            }
        });
        /*MODIFIED-END by haifeng.tang,BUG-1950773*/
        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1909322*/
        mPasswordLayout = (RelativeLayout) view.findViewById(R.id.password_layout);
        showPassword = (ImageView) view.findViewById(R.id.btn_show_password);


        if (mMode.equals(FileSafeBrowserActivity.DESTORY_TAG)) {
            mGridView.setVisibility(View.GONE);
            mPasswordLayout.setVisibility(View.VISIBLE);
            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-21,BUG-1940832*/
            //show inputmethod need delay
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) mActivity
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(mPasswordEdit, InputMethodManager.SHOW_FORCED);
                    }
                }
            }, 500);
            /* MODIFIED-END by haifeng.tang,BUG-1940832*/
        } else {
            mGridView.setVisibility(View.VISIBLE);
            mPasswordLayout.setVisibility(View.GONE);
            mGridView.setAdapter(mAdapter);
        }

        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = mPasswordEdit.getText();
                if (editable != null && editable.length() >= 0) {
                    Object object = showPassword.getTag();
                    if (object == null) {

                        //display password text
                        mPasswordEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); // MODIFIED by wenjing.ni, 2016-04-29,BUG-2002903
                        mPasswordEdit.setSelection(mPasswordEdit.getText().length());
                        showPassword.setTag(true);
                        showPassword.setImageResource(R.drawable.ic_eye);
                    } else {
                        boolean isDiaplayPassword = (boolean) showPassword.getTag();
                        if (isDiaplayPassword) {

                            //hide password text,eg:set 1234 to ****
                            mPasswordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            mPasswordEdit.setSelection(mPasswordEdit.getText().length());
                            showPassword.setTag(false);
                            showPassword.setImageResource(R.drawable.ic_eye_off);
                        } else {
                            //display password text
                            mPasswordEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            mPasswordEdit.setSelection(mPasswordEdit.getText().length());
                            showPassword.setTag(true);
                            showPassword.setImageResource(R.drawable.ic_eye);
                        }
                    }
                }

            }
        });
    }

    @Override
    int getContentLayoutId() {
        return R.layout.fragment_safe_category;
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case DATA_UPDATED:
                    if (mMode.equals(FileSafeBrowserActivity.DESTORY_TAG)) {
                        refreshCategoryAdapter();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void refreshCategoryAdapter() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        CategoryManager.isSafeCategory = true;

        FileSafeBrowserActivity fileSafeBrowserActivity = (FileSafeBrowserActivity) getActivity();
        switch (position) {
            case CategoryManager.SAFE_CATEGORY_FILES:

                fileSafeBrowserActivity.setActionbarTitle(R.string.category_files);
                /* MODIFIED-BEGIN by songlin.qi, 2016-06-15,BUG-2227088*/
                CategoryManager.mCurrentSafeCategory = CategoryManager.SAFE_CATEGORY_FILES;
                break;
            case CategoryManager.SAFE_CATEGORY_MUISC:
                fileSafeBrowserActivity.setActionbarTitle(R.string.category_music);
                CategoryManager.mCurrentSafeCategory = CategoryManager.SAFE_CATEGORY_MUISC;
                break;
            case CategoryManager.SAFE_CATEGORY_PICTURES:
                fileSafeBrowserActivity.setActionbarTitle(R.string.category_pictures);
                CategoryManager.mCurrentSafeCategory = CategoryManager.SAFE_CATEGORY_PICTURES;
                break;
            case CategoryManager.SAFE_CATEGORY_VEDIO:
                fileSafeBrowserActivity.setActionbarTitle(R.string.category_vedios);
                CategoryManager.mCurrentSafeCategory = CategoryManager.SAFE_CATEGORY_VEDIO;
                /* MODIFIED-END by songlin.qi,BUG-2227088*/
                break;
        }
        mCategoryFragmentListener.updateCategoryNormalBarView();
        mCategoryFragmentListener.switchCategoryList();


    }

    /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2003636*/
    @Override
    public void onResume() {
        super.onResume();
        if(mFirstSafebox) {
            mCategoryFragmentListener.updateSafeCategory();
        }
        mFirstSafebox = false;
    }
    /* MODIFIED-END by wenjing.ni,BUG-2003636*/

    private DataContentObserver mDataContentObserver;

    private void registerContentObservers() {
        Uri uri = MediaStore.Files.getContentUri("external");
        // if (mDataContentObserver != null) {
        mContext.getContentResolver().registerContentObserver(uri, true,
                mDataContentObserver);
        // }
    }

    private void unRegisterContentObservers() {
        // if (mDataContentObserver != null) {
        mContext.getContentResolver().unregisterContentObserver(
                mDataContentObserver);
        // }
    }

    private class DataContentObserver extends ContentObserver {

        private Handler mmHandler;

        public DataContentObserver(Handler handler, Context context) {
            super(handler);
            mmHandler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            Message msg = mmHandler.obtainMessage(DATA_UPDATED);
            mmHandler.sendMessage(msg);
        }
    }


}
