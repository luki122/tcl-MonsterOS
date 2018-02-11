/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.activity;

import mst.app.dialog.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import cn.tcl.filemanager.IActivityListener;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.dialog.AlertDialogFragment;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.manager.SafeManager;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.SafeInfo;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;

/* MODIFIED-BEGIN by songlin.qi, 2016-06-08,BUG-2274533*/
/*MODIFIED-BEGIN by haifeng.tang, 2016-04-13,BUG-1938948*/
/*MODIFIED-END by haifeng.tang,BUG-1938948*/
/* MODIFIED-END by songlin.qi,BUG-2274533*/
/* MODIFIED-BEGIN by zibin.wang, 2016-05-18,BUG-2165659*/
/* MODIFIED-END by zibin.wang,BUG-2165659*/
/* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
/* MODIFIED-END by haifeng.tang,BUG-1989942*/

/**
 * Created by user on 16-3-1.
 */
public class SafeBoxSettingsActivity extends FileBaseActionbarActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    public static final String SAFE_RENAME_DIALOG_TAG = "SafeRenameDialogTag";
    public static final int MIN_LIMIT = 4;
    public static final int MAX_LIMIT = 14;
    protected MountManager mMountPointManager;
    private Spinner spinner;
    private EditText mSafeBoxName; // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942
    private EditText mEditFrist;
    private EditText mEditAgain;
    private EditText mEditAnswer;
    //private Button mButtonSave;
    private int safe_storage = 0;
    private int question_index = 0;
    private LinearLayout mSetPasswordLayout;
    private LinearLayout mSetSafeLayout;
    private LinearLayout mUpdatePasswordLayout;
    private LinearLayout mUpdateQuestionLayout;
    private LinearLayout mForgetPasswordLayout; //MODIFIED by wenjing.ni, 2016-04-14,BUG-1924019
    private Context mContext; // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942


    private boolean isSafeSetting = false;
    private RelativeLayout mUpadtePassword;
    private RelativeLayout mUpadteQuestion;
    private RelativeLayout mChangeSafe;
    private RelativeLayout mSafeLocation;
    private RelativeLayout mSafeBoxNameLayout; // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942

    private EditText mPasswordOriginal;
    private EditText mPasswordFrist;
    private EditText mPasswordConfirm;
    private CheckBox mShowPassword;

    private EditText mQuestionOriginal;
    private EditText mQuestionAnswer;
    private Spinner mQuestionSpinner;
    private CheckBox mUpdateShowPassword;

    /*MODIFIED-BEGIN by wenjing.ni, 2016-04-14,BUG-1924019*/
    private EditText mForgetFrist;
    private EditText mForgetConfirm;
    private CheckBox mForgetShowPassword;


    private TextView mSafeBoxNameTv; // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942

    //private EditText mDestoryOriginal;

    private String currentSafePath;
    private String currentRootPath;
    private boolean isForgetPassword;
    /*MODIFIED-END by wenjing.ni,BUG-1924019*/

    private IActivityListener mActivityListener;
    private FingerprintManagerCompat mFingerprintManager;

    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-15,BUG-1950773*/
    private Button mSaveBox;
    private View mSaveBoxContainer; // MODIFIED by songlin.qi, 2016-05-28,BUG-2212243
    private MenuItem mDestoryMenu;
    private boolean isEnterSetting = false; // MODIFIED by wenjing.ni, 2016-04-20,BUG-1967152
    /*MODIFIED-END by haifeng.tang,BUG-1950773*/
    private boolean isSettingEnter = false; // MODIFIED by wenjing.ni, 2016-05-04,BUG-802835
    private boolean isRelateFingerPrint = false;
    private String password;
    private String confirmPassord;
    private String answer;
    private boolean mFirstSafebox = false; // MODIFIED by wenjing.ni, 2016-05-13,BUG-2003636
    private ImageView mQuestionShowPassword;

    // max length of box name
    private final int BOX_NAME_MAX_LENGHT = 20;
    private TextView nameLimitTextIndicator; // MODIFIED by songlin.qi, 2016-06-05,BUG-2241761

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //super.setMainContentView();
        setContentView(R.layout.safe_password);
        Intent intent = getIntent();
        mContext = this; // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942
        if (intent != null) {
            safe_storage = intent.getIntExtra("StorageLocation", 0);
            isSafeSetting = intent.getBooleanExtra("setpassword", false);
            currentSafePath = intent.getStringExtra("currentsafepath");
            /*MODIFIED-BEGIN by wenjing.ni, 2016-04-14,BUG-1924019*/
            currentRootPath = intent.getStringExtra("currentsaferootpath");
            isForgetPassword = intent.getBooleanExtra("isForgetPassword", false);
            isSettingEnter = intent.getBooleanExtra("from_settings", false); // MODIFIED by wenjing.ni, 2016-05-04,BUG-802835
            mFirstSafebox = intent.getBooleanExtra("FirstSafebox",false);
            /*MODIFIED-END by wenjing.ni,BUG-1924019*/
        }
        mFingerprintManager = FingerprintManagerCompat.from(this);
        mSetPasswordLayout = (LinearLayout) findViewById(R.id.safe_password_set_lay);
        mSetSafeLayout = (LinearLayout) findViewById(R.id.safe_set_lay);
        mUpdatePasswordLayout = (LinearLayout) findViewById(R.id.update_password_lay);
        mUpdateQuestionLayout = (LinearLayout) findViewById(R.id.update_question_lay);
        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-14,BUG-1924019*/
        mForgetPasswordLayout = (LinearLayout) findViewById(R.id.forget_password_lay);
        //mDestorySafeLayout =(LinearLayout) findViewById(R.id.destory_safe_lay);
        if (isForgetPassword) { // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942
            mSetSafeLayout.setVisibility(View.GONE);
            mSetPasswordLayout.setVisibility(View.GONE);
            mUpdatePasswordLayout.setVisibility(View.GONE);
            mUpdateQuestionLayout.setVisibility(View.GONE);
            mForgetPasswordLayout.setVisibility(View.VISIBLE);
            setActionbarTitle(R.string.forget_password);
        } else if (!isSafeSetting || isSettingEnter) { // MODIFIED by wenjing.ni, 2016-05-04,BUG-802835
            mSetSafeLayout.setVisibility(View.GONE);
            mSetPasswordLayout.setVisibility(View.VISIBLE);
            mUpdatePasswordLayout.setVisibility(View.GONE);
            mUpdateQuestionLayout.setVisibility(View.GONE);
            mForgetPasswordLayout.setVisibility(View.GONE);
            /*MODIFIED-END by wenjing.ni,BUG-1924019*/
            //mDestorySafeLayout.setVisibility(View.GONE);
            setActionbarTitle(R.string.set_password);
        } else if (isSafeSetting) {
            mSetPasswordLayout.setVisibility(View.GONE);
            mSetSafeLayout.setVisibility(View.VISIBLE);
            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
            mSafeBoxNameTv = (TextView) findViewById(R.id.safe_box_name_tv);
            mSafeBoxNameLayout = (RelativeLayout) findViewById(R.id.safe_box_name_layout);
            mSafeBoxNameTv.setText(SafeManager.getSafeBoxName(this, SafeUtils.getCurrentSafePath(this)));
            mSafeBoxNameLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRenameDialog(SafeManager.getSafeBoxName(mContext, SafeUtils.getCurrentSafePath(mContext)));
                }
            });
            mUpdatePasswordLayout.setVisibility(View.GONE);
            mUpdateQuestionLayout.setVisibility(View.GONE);
            mForgetPasswordLayout.setVisibility(View.GONE); //MODIFIED by wenjing.ni, 2016-04-14,BUG-1924019
            //mDestorySafeLayout.setVisibility(View.GONE);
            setActionbarTitle(R.string.safe_set_title_cn);

            //MODIFIED by haifeng.tang, 2016-04-18,BUG-1950773
        }
        mMountPointManager = MountManager.getInstance();
        mSafeBoxName = (EditText) findViewById(R.id.safe_box_name);
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-05,BUG-2241761*/
        nameLimitTextIndicator = (TextView) findViewById(R.id.name_limit_text_indicator);
        /* MODIFIED-BEGIN by haifeng.tang, 2016-04-26,BUG-1989911*/
        mSafeBoxName.setText(Build.MODEL); // MODIFIED by wenjing.ni, 2016-05-03,BUG-802835
        mSafeBoxName.setSelection(mSafeBoxName.getText().length());
        setEditTextFilter(mSafeBoxName); // MODIFIED by songlin.qi, 2016-06-08,BUG-2274533
        mSafeBoxName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = mSafeBoxName.getText().length();
                if (length >= 20) {
                    nameLimitTextIndicator.setVisibility(View.VISIBLE);
                } else {
                    nameLimitTextIndicator.setVisibility(View.GONE);
                }
            }
        });
        /* MODIFIED-END by songlin.qi,BUG-2241761*/
        /* MODIFIED-END by haifeng.tang,BUG-1989911*/
        /* MODIFIED-END by haifeng.tang,BUG-1989942*/
        mEditFrist = (EditText) findViewById(R.id.set_password_enter);
        mEditAgain = (EditText) findViewById(R.id.set_password_conform);
        mEditAnswer = (EditText) findViewById(R.id.set_password_answer);
        mShowPassword = (CheckBox) findViewById(R.id.set_password_show);
        mSaveBox = (Button) findViewById(R.id.save_btn); //MODIFIED by haifeng.tang, 2016-04-15,BUG-1950773
        mSaveBoxContainer = findViewById(R.id.save_btn_container); // MODIFIED by songlin.qi, 2016-05-28,BUG-2212243

        //mEditFrist.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        //mEditAgain.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        spinner = (Spinner) findViewById(R.id.set_password_spinner);
        //mButtonSave = (Button)findViewById(R.id.set_password_save);
        //mButtonSave.setOnClickListener(this);
        spinner.setOnItemSelectedListener(this);
        mShowPassword.setOnCheckedChangeListener(this);
        mSaveBox.setOnClickListener(this); //MODIFIED by haifeng.tang, 2016-04-15,BUG-1950773


        mUpadtePassword = (RelativeLayout) findViewById(R.id.safe_set_update_password);
        mUpadteQuestion = (RelativeLayout) findViewById(R.id.safe_set_secuity_problem);
        mChangeSafe = (RelativeLayout) findViewById(R.id.safe_set_change_safe);
        mSafeLocation = (RelativeLayout) findViewById(R.id.safe_set_safe_location);
        mUpadtePassword.setOnClickListener(this);
        mUpadteQuestion.setOnClickListener(this);
        mChangeSafe.setOnClickListener(this);
        mSafeLocation.setOnClickListener(this);

        mPasswordOriginal = (EditText) findViewById(R.id.update_password_original);
        mPasswordFrist = (EditText) findViewById(R.id.update_password_frist);
        mPasswordConfirm = (EditText) findViewById(R.id.update_password_confirm);
        mUpdateShowPassword = (CheckBox) findViewById(R.id.update_password_show);
        mUpdateShowPassword.setOnCheckedChangeListener(this); // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942

        mQuestionOriginal = (EditText) findViewById(R.id.update_question_original);
        mQuestionAnswer = (EditText) findViewById(R.id.update_question_answer);
        mQuestionSpinner = (Spinner) findViewById(R.id.update_question_spinner);
        mQuestionShowPassword =(ImageView) findViewById(R.id.update_question_show_password);
        mQuestionSpinner.setOnItemSelectedListener(this);
        mQuestionShowPassword.setOnClickListener(this);

        /*MODIFIED-BEGIN by wenjing.ni, 2016-04-14,BUG-1924019*/
        mForgetFrist = (EditText) findViewById(R.id.forget_password_enter);
        mForgetConfirm = (EditText) findViewById(R.id.forget_password_conform);
        mForgetShowPassword = (CheckBox) findViewById(R.id.forget_password_show);
        mForgetShowPassword.setOnCheckedChangeListener(this); // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942
        /*MODIFIED-END by wenjing.ni,BUG-1924019*/
        //mDestoryOriginal = (EditText)findViewById(R.id.destory_safe_edit);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        question_index = i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.password_menu, menu);
        mDestoryMenu = menu.findItem(R.id.destory_safe);
        if (isSafeSetting) {
            Log.d("POP", "this is enter 444");
            mDestoryMenu.setVisible(true);
            /*MODIFIED-BEGIN by haifeng.tang, 2016-04-15,BUG-1950773*/
            mSaveBox.setVisibility(View.GONE);
            /* MODIFIED-BEGIN by songlin.qi, 2016-05-28,BUG-2212243*/
            mSaveBoxContainer.setVisibility(View.GONE);
        } else {
            Log.d("POP", "this is enter 555");
            mDestoryMenu.setVisible(false);
            mSaveBox.setVisibility(View.VISIBLE);
            mSaveBoxContainer.setVisibility(View.VISIBLE);
            /* MODIFIED-END by songlin.qi,BUG-2212243*/
            /*MODIFIED-END by haifeng.tang,BUG-1950773*/
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {


            case R.id.destory_safe:
                Intent intent = new Intent(this, FileSafeBrowserActivity.class);
                intent.putExtra("destory_safe", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.safe_set_update_password:
                if (mSaveBox != null) {
                    mSaveBoxContainer.setVisibility(View.VISIBLE); // MODIFIED by songlin.qi, 2016-05-28,BUG-2212243
                    mSaveBox.setVisibility(View.VISIBLE);
                    mSaveBox.setText(R.string.save); //MODIFIED by haifeng.tang, 2016-04-18,BUG-1950773
                }
                if (mDestoryMenu != null) {
                    mDestoryMenu.setVisible(false);
                }
                /* MODIFIED-BEGIN by zibin.wang, 2016-05-18,BUG-2165659*/
                mPasswordOriginal.requestFocus();
                InputMethodManager inputMethodManager= (InputMethodManager) mPasswordOriginal.getContext().getSystemService(INPUT_METHOD_SERVICE);
                if (inputMethodManager!=null){
                    inputMethodManager.showSoftInput(mPasswordOriginal,0);
                }
                /* MODIFIED-END by zibin.wang,BUG-2165659*/
                mSetSafeLayout.setVisibility(View.GONE);
                mSetPasswordLayout.setVisibility(View.GONE);
                mUpdatePasswordLayout.setVisibility(View.VISIBLE);
                mUpdateQuestionLayout.setVisibility(View.GONE);
                mForgetPasswordLayout.setVisibility(View.GONE); //MODIFIED by wenjing.ni, 2016-04-14,BUG-1924019
                //mDestorySafeLayout.setVisibility(View.GONE);
                setActionbarTitle(R.string.modify_password);
                break;
            case R.id.safe_set_secuity_problem:
                if (mSaveBox != null) {
                    mSaveBoxContainer.setVisibility(View.VISIBLE); // MODIFIED by songlin.qi, 2016-05-28,BUG-2212243
                    mSaveBox.setVisibility(View.VISIBLE);
                    mSaveBox.setText(R.string.save); //MODIFIED by haifeng.tang, 2016-04-18,BUG-1950773
                }
                if (mDestoryMenu != null) {
                    mDestoryMenu.setVisible(false);
                }
                /* MODIFIED-BEGIN by zibin.wang, 2016-05-18,BUG-2165659*/
                mQuestionOriginal.requestFocus();
                mUpdateQuestionLayout.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager)mQuestionOriginal.getContext().getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(mQuestionOriginal, 0);
                }
                /* MODIFIED-END by zibin.wang,BUG-2165659*/
                mSetSafeLayout.setVisibility(View.GONE);
                mSetPasswordLayout.setVisibility(View.GONE);
                mUpdatePasswordLayout.setVisibility(View.GONE);
                mForgetPasswordLayout.setVisibility(View.GONE); //MODIFIED by wenjing.ni, 2016-04-14,BUG-1924019
                //mDestorySafeLayout.setVisibility(View.GONE);
                setActionbarTitle(R.string.modify_security_question);

                break;
            case R.id.safe_set_change_safe:
                List<SafeInfo> safeList = SafeUtils.getSafeItem(mMountPointManager, this);
                Intent intent = new Intent(this, FileSafeActivity.class);
                intent.putExtra("isExistSafe", true);
                intent.putExtra("safecount", safeList.size());
                startActivity(intent);
                finish();
                break;
            case R.id.safe_set_safe_location:
                /* MODIFIED-BEGIN by wenjing.ni, 2016-05-11,BUG-1950773*/
                String rootPath =null;
                String safePath = new File(SharedPreferenceUtils.getCurrentSafeRoot(this)).getParent();
                if(safePath.equals(mMountPointManager.getPhonePath())){
                    rootPath = getResources().getString(R.string.phone_storage_cn);
                } else if(safePath.equals(mMountPointManager.getSDCardPath())){
                    rootPath = getResources().getString(R.string.sd_card);
                } else if(safePath.equals(mMountPointManager.getUsbOtgPath())){
                    rootPath = getResources().getString(R.string.usbotg_m); // MODIFIED by songlin.qi, 2016-06-08,BUG-2278011
                }
                /*MODIFIED-BEGIN by haifeng.tang, 2016-04-18,BUG-1950773*/
                AlertDialog mSafeLocationDialog = new AlertDialog.Builder(this)
                        .setMessage(getResources().getString(R.string.safe_location_current_cn) +
                                rootPath+"  .File_SafeBox"+ File.separator + SharedPreferenceUtils.getCurrentSafeName(this)
                                /* MODIFIED-END by wenjing.ni,BUG-1950773*/
                                + "." + getResources().getString(R.string.safe_location_content_cn)).setPositiveButton(R.string.ok, null).show();
                                /*MODIFIED-END by haifeng.tang,BUG-1950773*/
                break;
            case R.id.save_btn:
            /*MODIFIED-END by haifeng.tang,BUG-1950773*/
                final String safeName = getSetSafeName(); // MODIFIED by songlin.qi, 2016-05-27,BUG-2216782
                if (mSetPasswordLayout.getVisibility() == View.VISIBLE) {
                    password = mEditFrist.getText().toString();
                    confirmPassord = mEditAgain.getText().toString();
                    answer = mEditAnswer.getText().toString();
                    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-13,BUG-1938948*/
                      if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassord)) {
                        Toast.makeText(this, getResources().getString(R.string.password_empty), Toast.LENGTH_LONG).show();
                    } else if (!password.equals(confirmPassord)) {
                        Toast.makeText(this, getResources().getString(R.string.password_consistent), Toast.LENGTH_LONG).show();
                    } else if (password.length() < MIN_LIMIT || password.length() > MAX_LIMIT) {
                        Toast.makeText(this, getResources().getString(R.string.password_length_error), Toast.LENGTH_LONG).show(); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910955
                    } else if (answer == null || answer.equals("")) {
                    /*MODIFIED-END by haifeng.tang,BUG-1938948*/
                        Toast.makeText(this, getResources().getString(R.string.answer_empty), Toast.LENGTH_LONG).show();
                    } else if (TextUtils.isEmpty(mSafeBoxName.getText())) {
                          Toast.makeText(this, getResources().getString(R.string.safe_box_name_empty), Toast.LENGTH_LONG).show();
                    } else {
                        if (isRelateFingerPrint() || (isSetFingerPrint() && isSettingEnter) || isSetFingerPrint()) {
                            Log.d("FIN", "this is enter--111---- " + isRelateFingerPrint());
                            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-26,BUG-1989911*/
                            if (isSettingEnter && isSetFingerPrint()) {
                                Intent settingsIntent = new Intent();
                                setResult(RESULT_OK, settingsIntent);
                            } else if (!isRelateFingerPrint() && isSetFingerPrint()){
                                SafeUtils.openSafeBoxRelate(SafeBoxSettingsActivity.this);
                                Toast.makeText(mContext, getResources().getString(R.string.finger_print_relation), Toast.LENGTH_LONG).show();
                            }
                            SafeUtils.getSafeRootPath(this, safe_storage, mMountPointManager, password, answer, question_index, TextUtils.isEmpty(safeName)? Build.MODEL:safeName,isSettingEnter,mFirstSafebox); // MODIFIED by songlin.qi, 2016-05-27,BUG-2216782
                            isRelateFingerPrint = false;
                            isSettingEnter = false;
                            //Toast.makeText(SafeBoxSettingsActivity.this,getResources().getString(R.string.finger_print_relation),Toast.LENGTH_LONG).show(); // MODIFIED by songlin.qi, 2016-05-31,BUG-2222796
                            /* MODIFIED-END by haifeng.tang,BUG-1989911*/
                            finish();
                        } else {
                            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-27,BUG-1991729*/
                            AlertDialog fingerprintRelativeDialog = new AlertDialog.Builder(this).setMessage(R.string.fingerprint_relative_dialog_cn) // MODIFIED by songlin.qi, 2016-06-14,BUG-2269190
                                    .setPositiveButton(R.string.fingerprint_relative_dialog_ok, new DialogInterface.OnClickListener() { // MODIFIED by wenjing.ni, 2016-05-07,BUG-802835
                                    /* MODIFIED-END by haifeng.tang,BUG-1991729*/
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            isRelateFingerPrint = true;
                                                try {
                                                    /* MODIFIED-BEGIN by wenjing.ni, 2016-05-10,BUG-1967152*/
                                                    if (isSetFingerPrint() && SafeUtils.isUserFingerPrint(SafeBoxSettingsActivity.this)) {
                                                        if (isSettingEnter) {
                                                            isSettingEnter = false;
                                                            Intent settingsIntent = new Intent();
                                                            setResult(RESULT_OK, settingsIntent);
                                                        }
                                                        SafeUtils.openSafeBoxRelate(SafeBoxSettingsActivity.this);
                                                        SafeUtils.getSafeRootPath(SafeBoxSettingsActivity.this, safe_storage, mMountPointManager, password, answer, question_index, TextUtils.isEmpty(safeName) ? Build.MODEL : safeName, true, mFirstSafebox); // MODIFIED by songlin.qi, 2016-05-27,BUG-2216782
                                                        Toast.makeText(mContext, getResources().getString(R.string.finger_print_relation), Toast.LENGTH_LONG).show();
                                                        finish();
                                                    } else {
                                                        isEnterSetting = true;
                                                        Intent intent = new Intent();
                                                        intent.setClassName("com.android.settings",
                                                                "com.android.settings.fingerprint.FingerprintEnrollEnrolling");
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                                        intent.putExtra("enroll_fingerprint_tag", 0);
                                                        intent.putExtra("enroll_feature", 2);
                                                        startActivity(intent);
                                                    }

                                                } catch (ActivityNotFoundException e) {
                                                   e.printStackTrace();
                                                   /* MODIFIED-END by wenjing.ni,BUG-1967152*/
                                                } catch(Exception e){
                                                    e.printStackTrace();
                                                }
                                        }
                                    }).setNegativeButton(R.string.fingerprint_relative_dialog_cancel_cn, new DialogInterface.OnClickListener() { // MODIFIED by haifeng.tang, 2016-04-27,BUG-1991729
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            SafeUtils.getSafeRootPath(mContext, safe_storage, mMountPointManager, password, answer, question_index, TextUtils.isEmpty(safeName)? Build.MODEL:safeName,false,mFirstSafebox); // MODIFIED by songlin.qi, 2016-05-27,BUG-2216782
                                            finish();
                                        }
                                    }).show();

                            /* MODIFIED-BEGIN by songlin.qi, 2016-06-14,BUG-2269190*/
                            fingerprintRelativeDialog.setCanceledOnTouchOutside(false);
                            fingerprintRelativeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.positive_text_color));
                            /* MODIFIED-END by songlin.qi,BUG-2269190*/
                            CommonUtils.setDialogTitleInCenter(fingerprintRelativeDialog);
                        }
                    }
                } else if (mUpdatePasswordLayout.getVisibility() == View.VISIBLE) {
                    if (mPasswordFrist.getText().toString() == null || mPasswordConfirm.getText().toString() == null ||
                            mPasswordFrist.getText().toString().equals("") || mPasswordConfirm.getText().toString().equals("") ||
                            mPasswordOriginal.getText().toString() == null || mPasswordOriginal.getText().toString().equals("")) {
                        Toast.makeText(this, getResources().getString(R.string.password_empty), Toast.LENGTH_LONG).show();
                    } else if (!mPasswordFrist.getText().toString().equals(mPasswordConfirm.getText().toString())) {
                        Toast.makeText(this, getResources().getString(R.string.password_consistent), Toast.LENGTH_LONG).show();
                    } else {
                        if (SafeManager.isAuthorizationOriginalPassword(this, currentSafePath + File.separator,
                                mPasswordOriginal.getText().toString(), mPasswordFrist.getText().toString())) {
                            Toast.makeText(this, getResources().getString(R.string.update_success), Toast.LENGTH_LONG).show();
                            /*MODIFIED-BEGIN by haifeng.tang, 2016-04-15,BUG-1950773*/
                            Intent intent2 = new Intent(this, FileSafeBrowserActivity.class);
                            intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // MODIFIED by wenjing.ni, 2016-05-04,BUG-802835
                            intent2.putExtra("isExistSafe", true);
                            intent2.putExtra("currentsafepath", currentSafePath);
                            startActivity(intent2);
                            /*MODIFIED-END by haifeng.tang,BUG-1950773*/
                            finish();
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.update_fail), Toast.LENGTH_LONG).show();
                        }
                    }

                } else if (mUpdateQuestionLayout.getVisibility() == View.VISIBLE) {
                    if (mQuestionOriginal.getText().toString() == null || mQuestionOriginal.getText().toString().equals("")) {
                        Toast.makeText(this, getResources().getString(R.string.password_empty), Toast.LENGTH_LONG).show();
                    } else if (mQuestionAnswer.getText().toString() == null || mQuestionAnswer.getText().toString().equals("")) {
                        Toast.makeText(this, getResources().getString(R.string.answer_empty), Toast.LENGTH_LONG).show();
                    } else {
                        if (SafeManager.isAuthorizationOriginalQuestion(this, currentSafePath + File.separator, mQuestionOriginal.getText().toString(),
                                question_index, mQuestionAnswer.getText().toString())) {
                            Toast.makeText(this, getResources().getString(R.string.update_success), Toast.LENGTH_LONG).show();
                            /*MODIFIED-BEGIN by haifeng.tang, 2016-04-15,BUG-1950773*/
                            /* MODIFIED-BEGIN by wenjing.ni, 2016-05-04,BUG-802835*/
                            Intent intent1 = new Intent(this, FileSafeBrowserActivity.class);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            intent1.putExtra("isExistSafe", true);
                            intent1.putExtra("currentsafepath", currentSafePath);
                            startActivity(intent1);
                            /* MODIFIED-END by wenjing.ni,BUG-802835*/
                            /*MODIFIED-END by haifeng.tang,BUG-1950773*/
                            finish();
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.update_fail), Toast.LENGTH_LONG).show();
                        }
                    }

                /*MODIFIED-BEGIN by wenjing.ni, 2016-04-14,BUG-1924019*/
                } else if (mForgetPasswordLayout.getVisibility() == View.VISIBLE) {
                    final String forgetPassword = mForgetFrist.getText().toString();
                    final String forgetConfirmPassord = mForgetConfirm.getText().toString();
                    if (TextUtils.isEmpty(forgetPassword) || TextUtils.isEmpty(forgetConfirmPassord)) {
                        Toast.makeText(this, getResources().getString(R.string.password_empty), Toast.LENGTH_LONG).show();
                    } else if (!forgetPassword.equals(forgetConfirmPassord)) {
                        Toast.makeText(this, getResources().getString(R.string.password_consistent), Toast.LENGTH_LONG).show();
                    } else if (forgetPassword.length() < MIN_LIMIT || forgetPassword.length() > MAX_LIMIT) {
                        Toast.makeText(this, getResources().getString(R.string.password_length_error), Toast.LENGTH_LONG).show(); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910955
                    } else {
                        if (SafeManager.UpdatePassword(this, currentSafePath + File.separator, forgetPassword)) { // MODIFIED by haifeng.tang, 2016-04-23,BUG-1989942
                            /*MODIFIED-BEGIN by haifeng.tang, 2016-04-15,BUG-1950773*/
                            Intent intent3 = new Intent(this, FileSafeBrowserActivity.class);
                            intent3.putExtra("isExistSafe", true);
                            intent3.putExtra("currentsafepath", currentSafePath);
                            intent3.putExtra("currentsaferootpath", currentRootPath);
                            intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent3);
                            /*MODIFIED-END by haifeng.tang,BUG-1950773*/
                            finish();
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.update_fail), Toast.LENGTH_LONG).show();
                        }
                    }
                    /*MODIFIED-END by wenjing.ni,BUG-1924019*/
                }

                break;
            case R.id.update_question_show_password:
                Editable editable = mQuestionOriginal.getText();
                if (editable != null && editable.length() >= 0) {
                    Object object = mQuestionShowPassword.getTag();
                    if (object == null) {

                        //display password text
                        mQuestionOriginal.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); // MODIFIED by wenjing.ni, 2016-04-29,BUG-2002903
                        mQuestionOriginal.setSelection(mQuestionOriginal.getText().length());
                        mQuestionShowPassword.setTag(true);
                        mQuestionShowPassword.setImageResource(R.drawable.ic_eye);
                    } else {
                        boolean isDiaplayPassword = (boolean) mQuestionShowPassword.getTag();
                        if (isDiaplayPassword) {

                            //hide password text,eg:set 1234 to ****
                            mQuestionOriginal.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            mQuestionOriginal.setSelection(mQuestionOriginal.getText().length());
                            mQuestionShowPassword.setTag(false);
                            mQuestionShowPassword.setImageResource(R.drawable.ic_eye_off);
                        } else {
                            //display password text
                            mQuestionOriginal.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            mQuestionOriginal.setSelection(mQuestionOriginal.getText().length());
                            mQuestionShowPassword.setTag(true);
                            mQuestionShowPassword.setImageResource(R.drawable.ic_eye);
                        }
                    }
                }
                break;

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.set_password_show:
                if (b) {
                    mEditFrist.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mEditAgain.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    mEditFrist.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mEditAgain.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                /*MODIFIED-BEGIN by haifeng.tang, 2016-04-13,BUG-1938948*/
                moveCursorToEnd(mEditFrist);
                moveCursorToEnd(mEditAgain);
                /*MODIFIED-END by haifeng.tang,BUG-1938948*/
                break;
            case R.id.update_password_show:
                if (b) {
                    mPasswordOriginal.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mPasswordFrist.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mPasswordConfirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    mPasswordOriginal.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mPasswordFrist.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mPasswordConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                /*MODIFIED-BEGIN by haifeng.tang, 2016-04-13,BUG-1938948*/
                moveCursorToEnd(mPasswordOriginal);
                moveCursorToEnd(mPasswordFrist);
                moveCursorToEnd(mPasswordConfirm);
                break;
            /*MODIFIED-BEGIN by wenjing.ni, 2016-04-14,BUG-1924019*/
            case R.id.forget_password_show:
                if (b) {
                    mForgetFrist.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mForgetConfirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    mForgetFrist.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mForgetConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                moveCursorToEnd(mForgetFrist);
                moveCursorToEnd(mForgetConfirm);

                break;
                /*MODIFIED-END by wenjing.ni,BUG-1924019*/
        }
    }

    /**
     * move edittext cursor to end selection
     *
     * @param editText
     */
    private void moveCursorToEnd(EditText editText) {

        Editable editable = editText.getText();
        if (editable != null && editable.length() > 0) {
            editText.setSelection(editable.length());
        }

    }


    public boolean isRelateFingerPrint() {
        boolean isOnOff = (Settings.Global.getInt(this.getContentResolver(), "safe_settings", 0)) == 1;
        boolean isCnOnOff = Settings.System.getInt(getContentResolver(), "tct_filesecurity", 0) == 1;
        if (mFingerprintManager == null) {
            Log.d("FINGER", "this is enter mFingerprintManager is null");
            return false;
        }
        /*MODIFIED-END by haifeng.tang,BUG-1938948*/
        if (mFingerprintManager != null && mFingerprintManager.isHardwareDetected() && mFingerprintManager.hasEnrolledFingerprints()
                && (isOnOff || isCnOnOff)) {
            return true;
        }
        return false;
    }


    /* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1989942*/
    private void showRenameDialog(String name) {
        int selection = name.length();
        AlertDialogFragment.EditDialogFragmentBuilder builder = new AlertDialogFragment.EditDialogFragmentBuilder();
        builder.setDefault(name, selection, false).setDoneTitle(R.string.save) //MODIFIED by haifeng.tang, 2016-04-09,BUG-1913721
                .setCancelTitle(R.string.cancel)
                .setTitle(R.string.rename);
        AlertDialogFragment.EditTextDialogFragment renameDialogFragment = builder.create();
            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-21,BUG-1940832*/

        // set max length of box name
        renameDialogFragment.setFileNameMaxLength(BOX_NAME_MAX_LENGHT);
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-05,BUG-2241761*/
        // set invalid checking to false, as name can be other characters
        renameDialogFragment.setInvalidChecking(false);
        // set too long toast display as false, for the text indicator instead
        renameDialogFragment.setTooLongInputPrompting(false);
        /* MODIFIED-END by songlin.qi,BUG-2241761*/

        renameDialogFragment.setOnEditTextDoneListener(new AlertDialogFragment.EditTextDialogFragment.EditTextDoneListener() {
            @Override
            public void onClick(String text) {
                if (SafeManager.updateSafeBoxName(mContext,SafeUtils.getCurrentSafePath(mContext),text)){
                    /* MODIFIED-BEGIN by wenjing.ni, 2016-05-04,BUG-802835*/
                    //mSafeBoxNameTv.setText(text);
                    Toast.makeText(mContext, R.string.toast_update_safe_box_name_sucess,Toast.LENGTH_LONG).show();
                    Intent nameIntent = new Intent(SafeBoxSettingsActivity.this, FileSafeBrowserActivity.class);
                    nameIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //MODIFIED by wenjing.ni, 2016-04-15,BUG-1950762
                    nameIntent.putExtra("isExistSafe", true);
                    nameIntent.putExtra("currentsafepath", currentSafePath);
                    startActivity(nameIntent);
                    finish();
                    /* MODIFIED-END by wenjing.ni,BUG-802835*/
                }else {
                    Toast.makeText(mContext,R.string.toast_update_safe_box_name_fail,Toast.LENGTH_LONG).show();
                }


            }
        });
        renameDialogFragment.show(getFragmentManager(), SAFE_RENAME_DIALOG_TAG);
    }
    /* MODIFIED-END by haifeng.tang,BUG-1989942*/

    /* MODIFIED-BEGIN by wenjing.ni, 2016-04-20,BUG-1967152*/
    public boolean isSetFingerPrint() {
        if (mFingerprintManager == null) {
            Log.d("FINGER", "this is enter mFingerprintManager is null");
            return false;
        }
        /*MODIFIED-END by haifeng.tang,BUG-1938948*/
        if (mFingerprintManager != null && mFingerprintManager.isHardwareDetected() && mFingerprintManager.hasEnrolledFingerprints()
                && SafeUtils.isUserFingerPrint(this)) {
            return true;
        }
        return false;
    }
    /* MODIFIED-END by wenjing.ni,BUG-1967152*/

    @Override
    public void onBackPressed() {

        if (mUpdateQuestionLayout.getVisibility() == View.VISIBLE || mUpdatePasswordLayout.getVisibility() == View.VISIBLE) { //MODIFIED by haifeng.tang, 2016-04-13,BUG-1938948
            mSetSafeLayout.setVisibility(View.VISIBLE);
            mSetPasswordLayout.setVisibility(View.GONE);
            mUpdatePasswordLayout.setVisibility(View.GONE);
            mUpdateQuestionLayout.setVisibility(View.GONE);
            mForgetPasswordLayout.setVisibility(View.GONE); //MODIFIED by wenjing.ni, 2016-04-14,BUG-1924019
            mDestoryMenu.setVisible(true);
            mSaveBoxContainer.setVisibility(View.GONE); // MODIFIED by songlin.qi, 2016-05-28,BUG-2212243
            mSaveBox.setVisibility(View.GONE); //MODIFIED by haifeng.tang, 2016-04-15,BUG-1950773
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (isEnterSetting && isSetFingerPrint()) {
                /* MODIFIED-BEGIN by songlin.qi, 2016-05-27,BUG-2216782*/
                String name = getSetSafeName();
                if(!isRelateFingerPrint()) {
                    SafeUtils.openSafeBoxRelate(SafeBoxSettingsActivity.this);
                }
                SafeUtils.getSafeRootPath(SafeBoxSettingsActivity.this, safe_storage, mMountPointManager, password, answer, question_index, TextUtils.isEmpty(name) ? Build.MODEL : name, false,mFirstSafebox);
                /* MODIFIED-END by songlin.qi,BUG-2216782*/
                Toast.makeText(mContext, getResources().getString(R.string.finger_print_relation), Toast.LENGTH_LONG).show();
                finish();
            }
            isEnterSetting = false;
        }catch(Exception e){
            e.printStackTrace();
        }
    /* MODIFIED-BEGIN by songlin.qi, 2016-05-27,BUG-2216782*/
    }


    private String getSetSafeName() {
        String result = null;
        if (mSafeBoxName != null && mSafeBoxName.getText() != null) {
            result = mSafeBoxName.getText().toString();
        }

        return result;
    }
    /* MODIFIED-END by songlin.qi,BUG-2216782*/

    /* MODIFIED-BEGIN by songlin.qi, 2016-06-08,BUG-2274533*/
    private void setEditTextFilter(final EditText edit) {
        if (edit == null) return;

        final int nameMaxLength = 20;
        InputFilter filter = new InputFilter.LengthFilter(nameMaxLength) {
            private final int VIBRATOR_TIME = 100;

            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                boolean isTooLong = false;
                CharSequence result = null;
                try {
                    int length = source.toString().length() + dest.toString().length();
                    if (length <= nameMaxLength) {
                        int keep = nameMaxLength - (dest.length() - (dend - dstart));
                        if (keep <= 0) {
                            isTooLong = true;
                            result = "";
                        } else if (keep >= end - start) {
                            // return null; // keep original
                        } else {
                            isTooLong = true;
                            boolean needsub = true;
                            keep += start;
                            if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                                --keep;
                                if (keep == start) {
                                    result = "";
                                    needsub = false;
                                }
                            }
                            if (needsub) {
                                result = source.subSequence(start, keep);
                                boolean hasComposing = false;
                                Object composingObj = null;
                                if (source instanceof Spanned) {
                                    Spanned text = (Spanned) source;
                                    Object[] sps = text.getSpans(0, text.length(),
                                            Object.class);
                                    if (sps != null) {
                                        for (int i = sps.length - 1; i >= 0; i--) {
                                            Object o = sps[i];
                                            if ((text.getSpanFlags(o) & Spanned.SPAN_COMPOSING) != 0) {
                                                hasComposing = true;
                                                composingObj = o;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (hasComposing) {
                                    SpannableString ss = new SpannableString(result);
                                    ss.setSpan(composingObj, 0, ss.length(),
                                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_COMPOSING);
                                    result = ss;
                                }
                            }
                        }
                    } else {
                        result = "";
                        isTooLong = true;

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (isTooLong) {
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    boolean hasVibrator = vibrator.hasVibrator();
                    if (hasVibrator) {
                        vibrator.vibrate(new long[]{VIBRATOR_TIME, VIBRATOR_TIME}, -1);
                    }
                }
                return result;
            }
        };
        edit.setFilters(new InputFilter[]{filter});
    }
    /* MODIFIED-END by songlin.qi,BUG-2274533*/

    @Override
    protected void onStop() {
        super.onStop();
        if(mUpdatePasswordLayout.getVisibility() == View.VISIBLE || mUpdateQuestionLayout.getVisibility() == View.VISIBLE
                || mSetSafeLayout.getVisibility() == View.VISIBLE){
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
