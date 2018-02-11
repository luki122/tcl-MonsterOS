/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.dialog;

/* MODIFIED-BEGIN by haifeng.tang, 2016-05-06,BUG-2048307*/
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.activity.FileSafeActivity;
import cn.tcl.filemanager.activity.FileSafeBrowserActivity;
import cn.tcl.filemanager.activity.SafeBoxSettingsActivity;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.manager.PrivateHelper;
import cn.tcl.filemanager.manager.SafeManager;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;

import mst.app.dialog.AlertDialog;

/* MODIFIED-END by haifeng.tang,BUG-2048307*/
/* MODIFIED-BEGIN by wenjing.ni, 2016-04-29,BUG-2002903*/
/* MODIFIED-END by wenjing.ni,BUG-2002903*/
/* MODIFIED-BEGIN by zibin.wang, 2016-05-16,BUG-2148511*/
/* MODIFIED-END by zibin.wang,BUG-2148511*/
/* MODIFIED-BEGIN by haifeng.tang, 2016-05-06,BUG-2048307*/
/* MODIFIED-END by haifeng.tang,BUG-2048307*/

/**
 * Created by hftang on 4/19/16.
 */
/* MODIFIED-BEGIN by haifeng.tang, 2016-05-06,BUG-2048307*/
@TargetApi(Build.VERSION_CODES.M)
public class PasswordDialog {


    private static final String TAG =PasswordDialog.class.getSimpleName() ;
    private AlertDialog passwordDialog;
    private AlertDialog forgetDialog; // MODIFIED by songlin.qi, 2016-05-27,BUG-2216886
    private String current_safe_name;
    private EditText mPasswrodEdit;
    private EditText mAnswerEdit;
    private Context mContext;
    public AlertDialog fingerPrintDialog = null;
    private static final int MSG_FINGER_AUTH_SUCCESS = 1001;
    private static final int MSG_FINGER_AUTH_FAIL = 1002;
    private static final int MSG_FINGER_AUTH_ERROR = 1003;
    private static final int MSG_FINGER_AUTH_HELP = 1004;
    private static final long LOCKOUT_DURATION = 30000;
    private CancellationSignal mFingerprintCancel;
    private String mCurrentSafePath;
    //private static boolean mInFingerprintLockout;
    private FingerprintManager mFingerprintManager;
    private FingerprintManagerCompat mFingerprintManagerCompat;
    private Activity mActivity;
    private boolean isSettingsEnter = false;
    private int fingerFailCount = 0;


    public PasswordDialog(Activity activity, String path,boolean isSettings) {
        mActivity = activity;
        mContext = mActivity;
        mCurrentSafePath = path;
        isSettingsEnter = isSettings;
        mFingerprintManager = (FingerprintManager) mContext.getSystemService(
                Context.FINGERPRINT_SERVICE);
        mFingerprintManagerCompat = FingerprintManagerCompat.from(mContext);
    }


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_FINGER_AUTH_SUCCESS:
                    mFingerprintCancel = null;
                    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/
                    if (fingerPrintDialog != null && fingerPrintDialog.isShowing()) {
                        fingerPrintDialog.dismiss();
                        /*MODIFIED-END by haifeng.tang,BUG-1910684*/
                    }
                    SharedPreferenceUtils.setCurrentSafeName(mContext, new File(mCurrentSafePath).getName());
                    SharedPreferenceUtils.setCurrentSafeRoot(mContext, new File(mCurrentSafePath).getParent());
                    Intent intent = new Intent(mContext, FileSafeBrowserActivity.class);
                    intent.putExtra("isExistSafe", false);
                    intent.putExtra("currentsafepath", mCurrentSafePath);
                    intent.putExtra("currentsaferootpath", new File(mCurrentSafePath).getParent());
                    mContext.startActivity(intent);
                    break;
                case MSG_FINGER_AUTH_FAIL:
                    // No action required... fingerprint will allow up to 5 of these
                    break;
                case MSG_FINGER_AUTH_ERROR:
                    handleError(msg.arg1 /* errMsgId */, (CharSequence) msg.obj /* errStr */);
                    break;
                case MSG_FINGER_AUTH_HELP: {
                    // Not used
                }
                break;
            }
        }

    };


    protected void handleError(int errMsgId, CharSequence msg) {
        mFingerprintCancel = null;
        switch (errMsgId) {
            case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
                return; // Only happens if we get preempted by another activity. Ignored.
            case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
                SafeManager.mInFingerprintLockout = true;
                // We've been locked out.  Reset after 30s.
                if (!mHandler.hasCallbacks(mFingerprintLockoutReset)) {
                    mHandler.postDelayed(mFingerprintLockoutReset,
                            LOCKOUT_DURATION);
                    Toast.makeText(mContext, mContext.getString(R.string.use_password_auth), Toast.LENGTH_SHORT).show();
                    SharedPreferenceUtils.setFingerPrintLock(mContext,true);
                    if(fingerPrintDialog != null && fingerPrintDialog.isShowing()){
                        fingerPrintDialog.dismiss();
                    }
                    popPasswordDialog(mContext,mCurrentSafePath);
//                    Toast.makeText(mContext,mContext.getResources().getString(R.string.message_security_fingerprint_lockout),
//                            Toast.LENGTH_SHORT).show();
                }
                // Fall through to show message
                break;
        }
        //ScanFinger(); // start again
    }

    private void ScanFinger() {
        if (!SafeManager.mInFingerprintLockout) {
            mFingerprintCancel = new CancellationSignal();
            if (mContext.checkSelfPermission(Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            mFingerprintManager.authenticate(null, mFingerprintCancel, 0, mAuthCallback, null);
        }

    }


    private FingerprintManager.AuthenticationCallback mAuthCallback = new FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            //Toast.makeText(FileSafeActivity.this, "success", Toast.LENGTH_SHORT).show();
           /* MODIFIED-BEGIN by wenjing.ni, 2016-05-14,BUG-2104869*/
           try {
               if (fingerPrintDialog != null && fingerPrintDialog.isShowing()
                       && SafeUtils.getFingerAuthenticationResult(mContext, result.getFingerprint().getFingerId()) == 0) {
                   fingerFailCount = 0;
                   mHandler.obtainMessage(MSG_FINGER_AUTH_SUCCESS, 0, 0).sendToTarget();
               } else {
                   fingerFailCount++;
                   if(fingerFailCount <= 4) {
                       ScanFinger();
                       Toast.makeText(mContext, mContext.getString(R.string.fingerprint_fail), Toast.LENGTH_SHORT).show();
                   }  else {
                       stopFingerprint();
                       if(fingerPrintDialog != null && fingerPrintDialog.isShowing()){
                           fingerPrintDialog.dismiss();
                       }
                       SharedPreferenceUtils.setFingerPrintLock(mContext,true);
                       popPasswordDialog(mContext,mCurrentSafePath);

                       Toast.makeText(mContext, mContext.getString(R.string.use_password_auth), Toast.LENGTH_SHORT).show();
                   }
                   //ScanFinger();
               }
           }catch(Exception e){
               e.printStackTrace();
           }
           /* MODIFIED-END by wenjing.ni,BUG-2104869*/
        }

        @Override
        public void onAuthenticationFailed() {
            if (fingerPrintDialog != null && fingerPrintDialog.isShowing()) {
                fingerFailCount++;
                if(fingerFailCount <= 4) {
                    Toast.makeText(mContext, mContext.getString(R.string.fingerprint_fail), Toast.LENGTH_SHORT).show();
                }
//                else {
//                    Toast.makeText(mContext, mContext.getString(R.string.use_password_auth), Toast.LENGTH_SHORT).show();
//                }
                mHandler.obtainMessage(MSG_FINGER_AUTH_FAIL).sendToTarget();
            }
        }

        ;

        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            if (fingerPrintDialog != null && fingerPrintDialog.isShowing()) { //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910684
                //Toast.makeText(FileSafeActivity.this, "error", Toast.LENGTH_SHORT).show(); //MODIFIED by wenjing.ni, 2016-04-13,BUG-1935700
                mHandler.obtainMessage(MSG_FINGER_AUTH_ERROR, errMsgId, 0, errString).sendToTarget();
            }
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            if (fingerPrintDialog != null && fingerPrintDialog.isShowing()) { //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910684
                //Toast.makeText(FileSafeActivity.this, "help", Toast.LENGTH_SHORT).show(); //MODIFIED by wenjing.ni, 2016-04-13,BUG-1935700
                mHandler.obtainMessage(MSG_FINGER_AUTH_HELP, helpMsgId, 0, helpString).sendToTarget();
            }
        }
    };


    private final Runnable mFingerprintLockoutReset = new Runnable() {
        @Override
        public void run() {
            SafeManager.mInFingerprintLockout = false;
            ScanFinger();
        }
    };

    public boolean isRelateFingerPrint() {
        boolean isOnOff = (Settings.Global.getInt(mContext.getContentResolver(), "safe_settings", 0)) == 1;
        boolean isCnOnOff = Settings.System.getInt(mContext.getContentResolver(), "tct_filesecurity", 0) == 1;
        if (mFingerprintManagerCompat != null && mFingerprintManagerCompat.isHardwareDetected() && mFingerprintManagerCompat.hasEnrolledFingerprints()
                && SafeUtils.isUserFingerPrint(mContext) && (isOnOff || isCnOnOff)) {
            return true;
        }
        return false;
    }

    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/
    private void popFingerprintDialog(Context context, String safeName) {
        current_safe_name = SafeUtils.getCurrentSafePath(context);
        Log.d("MODEL", "this is enter popFingerprintDialog" + current_safe_name);
        mContext = context;
        String mSafeBoxName = SafeManager.getSafeBoxName(context, SafeUtils.getCurrentSafePath(context));
        if(mSafeBoxName == null){
            mSafeBoxName = Build.MODEL;
        }
        fingerPrintDialog = new AlertDialog.Builder(context).setTitle(mSafeBoxName).setView(R.layout.finger_dialog)
                .setPositiveButton(R.string.use_password, new OpenPasswordListener()).setNegativeButton(
                        R.string.cancel, null).show();
        fingerPrintDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                stopFingerprint();
            }
        });
        fingerPrintDialog.setCanceledOnTouchOutside(false); // MODIFIED by songlin.qi, 2016-06-01,BUG-2223763
        fingerPrintDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(mContext.getResources().getColor(R.color.positive_text_color));
        CommonUtils.setDialogTitleInCenter(fingerPrintDialog); // MODIFIED by songlin.qi, 2016-06-14,BUG-2269190
    }


    class OpenPasswordListener implements DialogInterface.OnClickListener {
        /*MODIFIED-END by haifeng.tang,BUG-1910684*/
        @Override
        public void onClick(DialogInterface dialog, int id) {
            if (fingerPrintDialog != null && fingerPrintDialog.isShowing()) {
                fingerPrintDialog.dismiss();
            }
            /* MODIFIED-BEGIN by haifeng.tang, 2016-05-06,BUG-2048307*/
            popPasswordDialog(mContext,mCurrentSafePath);
//            LayoutInflater mlayout = (LayoutInflater) mContext
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//            View passwordDialogLayout = (View) mlayout.inflate(R.layout.password_dialog, null);
//            mPasswrodEdit = (EditText) passwordDialogLayout.findViewById(R.id.password_dialog_edittext);
//            TextView mPasswrodForget = (TextView) passwordDialogLayout.findViewById(R.id.password_dialog_forget);
//            TextView mPasswrodOthers = (TextView) passwordDialogLayout.findViewById(R.id.password_dialog_other);
//            mPasswrodForget.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (passwordDialog != null && passwordDialog.isShowing()) {
//                        passwordDialog.dismiss();
//                    }
//                    LayoutInflater mlayout = (LayoutInflater) mContext
//                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//                    View questionDialogLayout = (View) mlayout.inflate(R.layout.question_dialog, null);
//                    TextView question = (TextView) questionDialogLayout.findViewById(R.id.question_dialog_title);
//                    mAnswerEdit = (EditText) questionDialogLayout.findViewById(R.id.question_dialog_edittext);
//                    question.setText(SafeManager.queryQuestion(mContext, current_safe_name));
//                    new AlertDialog.Builder(mContext).setTitle(R.string.set_password_safe_question).setView(questionDialogLayout)
//                            .setPositiveButton(R.string.ok, new QuestionListener()).setNegativeButton(R.string.cancel, null).show();
//
//                }
//            });
//            mPasswrodOthers.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (passwordDialog != null && passwordDialog.isShowing()) {
//                        passwordDialog.dismiss();
//                    }
//                }
//            });
//            passwordDialog = new AlertDialog.Builder(mContext).setTitle(R.string.category_safe).setView(passwordDialogLayout)
//                    .setPositiveButton(R.string.ok, new ForgetListener()).setNegativeButton(R.string.cancel, null).show();
/* MODIFIED-END by haifeng.tang,BUG-2048307*/

        }
    }


    public void stopFingerprint() {
        if (mFingerprintCancel != null && !mFingerprintCancel.isCanceled()) {
            mFingerprintCancel.cancel();
        }
        mFingerprintCancel = null;
    }


    /* MODIFIED-BEGIN by haifeng.tang, 2016-05-06,BUG-2048307*/
    public void popIdentityVerify(){
        if (isRelateFingerPrint() && !SafeManager.mInFingerprintLockout && !SharedPreferenceUtils.getFingerPrintLock(mContext)) {
            fingerFailCount = 0;
            popFingerprintDialog(mContext, mCurrentSafePath);
            ScanFinger();
            LogUtils.i(TAG, "relate fingerprint");
            return;
        }else {
            popPasswordDialog(mContext,mCurrentSafePath);
        }
    }

    public void popPasswordDialog(final Context context, String safeName) {

//        if (isRelateFingerPrint() && SafeManager.isRelateFingerPrint(mContext,mCurrentSafePath)) {
//            popFingerprintDialog(context, mCurrentSafePath);
//            ScanFinger();
//            LogUtils.i(TAG, "relate fingerprint");
//            return;
//        }else {
//            LogUtils.i(TAG,"don't relate fingerprint");
//        }
/* MODIFIED-END by haifeng.tang,BUG-2048307*/
        /* MODIFIED-END by haifeng.tang,BUG-2048307*/

        passwordDialog = null;
        current_safe_name = safeName + File.separator;
        LayoutInflater mlayout = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        View passwordDialogLayout = (View) mlayout.inflate(R.layout.password_dialog, null);
        mPasswrodEdit = (EditText) passwordDialogLayout.findViewById(R.id.password_dialog_edittext);
        TextView mPasswrodForget = (TextView) passwordDialogLayout.findViewById(R.id.password_dialog_forget);
        TextView mPasswrodOthers = (TextView) passwordDialogLayout.findViewById(R.id.password_dialog_other);
        final ImageView showPassword = (ImageView) passwordDialogLayout.findViewById(R.id.btn_show_password); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910684
        /* MODIFIED-BEGIN by zibin.wang, 2016-05-16,BUG-2148511*/
        if(isSettingsEnter){
            mPasswrodForget.setVisibility(View.GONE);
            mPasswrodOthers.setVisibility(View.GONE);
        }
        mPasswrodEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    passwordDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        /* MODIFIED-END by zibin.wang,BUG-2148511*/
        mPasswrodForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passwordDialog != null && passwordDialog.isShowing()) {
                    passwordDialog.dismiss();
                }
                LayoutInflater mlayout = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View questionDialogLayout = (View) mlayout.inflate(R.layout.question_dialog, null);
                TextView question = (TextView) questionDialogLayout.findViewById(R.id.question_dialog_title);
                mAnswerEdit = (EditText) questionDialogLayout.findViewById(R.id.question_dialog_edittext);
                question.setText(SafeManager.queryQuestion(context, current_safe_name));
                /* MODIFIED-BEGIN by zibin.wang, 2016-05-26,BUG-2202917*/
                /* MODIFIED-BEGIN by songlin.qi, 2016-05-27,BUG-2216886*/
                forgetDialog = new AlertDialog.Builder(context).setTitle(R.string.set_password_safe_question).setView(questionDialogLayout)
                        .setPositiveButton(R.string.dialog_confirm, new QuestionListener()).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (forgetDialog != null && forgetDialog.isShowing()) {
                                    forgetDialog.dismiss();
                                }
                            }
                        }).show();
                forgetDialog.setCanceledOnTouchOutside(false);
                forgetDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(mContext.getResources().getColor(R.color.positive_text_color)); // MODIFIED by songlin.qi, 2016-06-14,BUG-2269190

                SafeUtils.fieldDialog(forgetDialog);
                CommonUtils.setDialogTitleInCenter(forgetDialog); // MODIFIED by songlin.qi, 2016-06-14,BUG-2269190
                /* MODIFIED-END by songlin.qi,BUG-2216886*/

                mAnswerEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            forgetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                });
                /* MODIFIED-END by zibin.wang,BUG-2202917*/

            }
        });
        mPasswrodOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FileSafeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                passwordDialog.dismiss();
            }
        });

        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/
        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = mPasswrodEdit.getText();
                if (editable != null && editable.length() >= 0) {
                    Object object = showPassword.getTag();
                    if (object == null) {
                        //display password text
                        mPasswrodEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); // MODIFIED by wenjing.ni, 2016-04-29,BUG-2002903
                        mPasswrodEdit.setSelection(mPasswrodEdit.getText().length());
                        showPassword.setTag(true);
                        /* MODIFIED-BEGIN by haifeng.tang, 2016-04-28,BUG-1995590*/
                        showPassword.setImageResource(R.drawable.ic_eye);
                        /* MODIFIED-END by haifeng.tang,BUG-1995590*/
                        /* MODIFIED-END by haifeng.tang,BUG-1995590*/
                    } else {
                        boolean isDiaplayPassword = (boolean) showPassword.getTag();
                        if (isDiaplayPassword) {
                            //hide password text,eg:set 1234 to ****
                            /* MODIFIED-BEGIN by wenjing.ni, 2016-04-29,BUG-2002903*/
                            mPasswrodEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                            mPasswrodEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            mPasswrodEdit.setSelection(mPasswrodEdit.getText().length());
                            showPassword.setTag(false);
                            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-28,BUG-1995590*/
                            showPassword.setImageResource(R.drawable.ic_eye_off);
                        } else {
                            //display password text
//                            mPasswrodEdit.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            mPasswrodEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            /* MODIFIED-END by wenjing.ni,BUG-2002903*/
                            mPasswrodEdit.setSelection(mPasswrodEdit.getText().length());
                            showPassword.setTag(true);
                            showPassword.setImageResource(R.drawable.ic_eye);
                            /* MODIFIED-END by haifeng.tang,BUG-1995590*/
                        }
                    }
                }


            }
        });
        MountManager mMountManager = MountManager.getInstance();
        AlertDialog.Builder passwordBuilder = new AlertDialog.Builder(context);
        passwordBuilder.setTitle(SafeManager.getSafeBoxName(mContext, SafeUtils.getCurrentSafePath(mContext))).setView(passwordDialogLayout)
                .setPositiveButton(R.string.dialog_confirm, new ForgetListener());
        passwordBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (passwordDialog != null && passwordDialog.isShowing()) {
                    passwordDialog.dismiss();
                }
                if(isSettingsEnter && mActivity != null) {
                    Intent settings = new Intent();
                    mActivity.setResult(mActivity.RESULT_CANCELED);
                    mActivity.finish();
                }
            }
        });
        passwordDialog = passwordBuilder.create();
        passwordDialog.setCanceledOnTouchOutside(false); // MODIFIED by songlin.qi, 2016-05-27,BUG-2216886
        SafeUtils.fieldDialog(passwordDialog);
        passwordDialog.show();
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-14,BUG-2269190*/
        passwordDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(mActivity.getResources().getColor(R.color.positive_text_color));
                /* MODIFIED-END by songlin.qi,BUG-2269190*/
        CommonUtils.setDialogTitleInCenter(passwordDialog);
    }

    class QuestionListener implements DialogInterface.OnClickListener { //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910684
        @Override
        public void onClick(DialogInterface dialog, int id) {
            if(TextUtils.isEmpty(mAnswerEdit.getText().toString())){
                Toast.makeText(mContext, mContext.getResources().getString(R.string.answer_empty_tip), Toast.LENGTH_LONG).show();
            } else if (SafeManager.isAuthorizationAnswer(mContext, current_safe_name, mAnswerEdit.getText().toString())) {
//                if(passwordDialog!=null && passwordDialog.isShowing()){
//                    passwordDialog.dismiss();
//                }
                SharedPreferenceUtils.setCurrentSafeName(mContext, new File(current_safe_name).getName());
                SharedPreferenceUtils.setCurrentSafeRoot(mContext, new File(current_safe_name).getParent());

                /*MODIFIED-BEGIN by wenjing.ni, 2016-04-14,BUG-1924019*/
                Intent intent = new Intent(mContext, SafeBoxSettingsActivity.class);
                intent.putExtra("isForgetPassword", true);
                intent.putExtra("currentsafepath", current_safe_name);
                intent.putExtra("currentsaferootpath", new File(current_safe_name).getParent());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                /*MODIFIED-END by wenjing.ni,BUG-1924019*/
            } else {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.answer_mistake), Toast.LENGTH_LONG).show();
            }

        }
    }

    class ForgetListener implements DialogInterface.OnClickListener { //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910684
        @Override
        public void onClick(DialogInterface dialog, int id) {
            if (isAuthorizationPassword(mContext, current_safe_name, mPasswrodEdit.getText().toString())) {
                if(passwordDialog!=null && passwordDialog.isShowing()) {
                    passwordDialog.dismiss();
                }
                fingerFailCount = 0;
                SharedPreferenceUtils.setFingerPrintLock(mContext,false);
                SharedPreferenceUtils.setCurrentSafeName(mContext, new File(current_safe_name).getName());
                SharedPreferenceUtils.setCurrentSafeRoot(mContext, new File(current_safe_name).getParent());
                if(isSettingsEnter && mActivity != null){
                    Intent settings = new Intent();
                    mActivity.setResult(mActivity.RESULT_OK);
                    mActivity.finish();
                } else {
                    Intent intent = new Intent(mContext, FileSafeBrowserActivity.class);
                    intent.putExtra("isExistSafe", true);
                    intent.putExtra("currentsafepath", current_safe_name);
                    intent.putExtra("currentsaferootpath", new File(current_safe_name).getParent());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            } else {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.password_mistake), Toast.LENGTH_LONG).show();
            }

        }
    }


    public boolean isAuthorizationPassword(Context context, String dbPath, String mEditPassword) {
        try {
            if (dbPath == null || dbPath.equals("null/")) {
                return false;
            }
            PrivateHelper mPrivateHelper = new PrivateHelper(context, dbPath);
            Log.d("QUES", "this is sql path" + dbPath);
            SQLiteDatabase db = mPrivateHelper.getWritableDatabase();
            String password = null;
            Cursor cursor = null;
            try {
                cursor = db.query(PrivateHelper.USER_TABLE_NAME, new String[]{PrivateHelper.USER_FIELD_WT2}, null, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    password = cursor.getString(0);
                }
                if (mEditPassword.equals(password)) {
                    return true;
                }
            } catch (Exception e) {

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
