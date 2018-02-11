/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import mst.app.dialog.AlertDialog;

import java.io.File;
import java.util.List;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.adapter.SafeStorageAdapter;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.manager.PrivateHelper;
import cn.tcl.filemanager.manager.SafeManager;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.SafeInfo;
import cn.tcl.filemanager.utils.SafeUtils;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;

/*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/
/*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1909910*/
/*MODIFIED-END by haifeng.tang,BUG-1909910*/
/*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/
/*MODIFIED-END by haifeng.tang,BUG-1910684*/
/* MODIFIED-BEGIN by songlin.qi, 2016-05-31,BUG-2222816*/
/* MODIFIED-END by songlin.qi,BUG-2222816*/
/*MODIFIED-END by haifeng.tang,BUG-1910684*/

/**
 * Created by user on 16-2-26.
 */
@TargetApi(Build.VERSION_CODES.M) //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910684
public class FileSafeActivity extends FileBaseActionbarActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private static final int MSG_REFRESH_FINGERPRINT_TEMPLATES = 1000;
    private static final int MSG_FINGER_AUTH_SUCCESS = 1001;
    private static final int MSG_FINGER_AUTH_FAIL = 1002;
    private static final int MSG_FINGER_AUTH_ERROR = 1003;
    private static final int MSG_FINGER_AUTH_HELP = 1004;
    private static final long LOCKOUT_DURATION = 30000;
    private static final int MAX_SAFE_BOX = 10;
    private static final String TAG = FileSafeActivity.class.getSimpleName();
    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1909910*/
    private Context mContext;

    /*password layout start */
    private String current_safe_name;
    private AlertDialog passwordDialog;
    private AlertDialog forgetDialog;
    private EditText mPasswrodEdit;
    private EditText mAnswerEdit;
    /*password layout end */
    /*MODIFIED-END by haifeng.tang,BUG-1909910*/

    private ListView safeStorage_list;
    private SafeStorageAdapter adapter;
    protected MountManager mMountPointManager;
    protected FileManagerApplication mApplication;
    private boolean isExistSafe = false;
    private int countSafe = 0;
    private List<SafeInfo> safeList;
    private FingerprintManager mFingerprintManager;
    private CancellationSignal mFingerprintCancel;
    private String mCurrentSafePath;
    private FingerprintManagerCompat mFingerprintManagerCompat;

    private MenuItem mCreateSafeBox;

    private int category;

    private boolean mAddSafeBoxVisiable; //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910684
    public AlertDialog fingerPrintDialog = null;
    private boolean mFirstSafebox = false; // MODIFIED by wenjing.ni, 2016-05-13,BUG-2003636

    private View createSafeView;
    private int fingerFailCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this; //MODIFIED by haifeng.tang, 2016-04-09,BUG-1909910
        setContentView(R.layout.safe_storage);
        /* MODIFIED-BEGIN by wenjing.ni, 2016-05-13,BUG-2003636*/
        Intent intent = getIntent();
        if(intent != null){
            mFirstSafebox = intent.getBooleanExtra("FirstSafebox",false);
        }
        /* MODIFIED-END by wenjing.ni,BUG-2003636*/
        mMountPointManager = MountManager.getInstance();
        mFingerprintManager = (FingerprintManager) getSystemService(
                FINGERPRINT_SERVICE);
        mFingerprintManagerCompat = FingerprintManagerCompat.from(this);
        safeList = SafeUtils.getSafeItem(mMountPointManager, this);
        mApplication = (FileManagerApplication) getApplicationContext();
        safeStorage_list = (ListView) findViewById(R.id.safe_storage_list);

        adapter = new SafeStorageAdapter(FileSafeActivity.this, mApplication, mMountPointManager);
        safeStorage_list.setAdapter(adapter);
        safeStorage_list.setOnItemClickListener(this);

        /* use menu item to instead
        createSafeView = findViewById(R.id.create_safe_view);
        createSafeView.setVisibility(View.VISIBLE);
        createSafeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActionbarTitle(R.string.create_safe_to_cn);
                adapter = new SafeStorageAdapter(mContext, mApplication, mMountPointManager);
                safeStorage_list.setAdapter(adapter);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        category = SafeStorageAdapter.CATEGORY_STORAGE_LIST;
                        adapter.setCategory(SafeStorageAdapter.CATEGORY_STORAGE_LIST);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.addAll(SafeUtils.getStorageItem(mMountPointManager, FileSafeActivity.this));
                                adapter.notifyDataSetChanged();
                                createSafeView.setVisibility(View.GONE);
                            }
                        });
                    }
                }).start();
            }
        });
        */ // MODIFIED by songlin.qi, 2016-06-05,BUG-2223767

    }

    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/
    private void setAddSafeBoxVisiable(boolean visiable) {
        mAddSafeBoxVisiable = visiable;
        /*MODIFIED-END by haifeng.tang,BUG-1910684*/
        invalidateOptionsMenu();
    }


    private void loadData(final boolean isBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                category = SafeStorageAdapter.CATEGORY_SAFE_BOX_LIST;
                final List<SafeInfo> safeInfos = SafeUtils.getSafeItem(mMountPointManager, FileSafeActivity.this);

                int safeBoxSize = safeInfos.size();
                if (safeBoxSize == 0) {
                    if (isBack) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                        return; //MODIFIED by haifeng.tang, 2016-04-13,BUG-1913721

                    } else {
                        //has no safe box ,show storage list to for create safe box
                        safeInfos.addAll(SafeUtils.getStorageItem(mMountPointManager, FileSafeActivity.this));
                        category = SafeStorageAdapter.CATEGORY_STORAGE_LIST;
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setCategory(category);
                        adapter.addAll(safeInfos);
                        adapter.notifyDataSetChanged();
                        if (category == SafeStorageAdapter.CATEGORY_SAFE_BOX_LIST) {
                            if (adapter.getCount() >= MAX_SAFE_BOX) {
                                /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/
                                /* MODIFIED-BEGIN by songlin.qi, 2016-06-05,BUG-2223767*/
                                //createSafeView.setVisibility(View.GONE);
                                setAddSafeBoxVisiable(false);
                            } else {
                                setAddSafeBoxVisiable(true);
                                //createSafeView.setVisibility(View.VISIBLE);
                            }

                            /*MODIFIED-BEGIN by haifeng.tang, 2016-04-18,BUG-1950773*/
                            setActionbarTitle(R.string.choose_safe_cn);
                        } else {
                            setAddSafeBoxVisiable(false);
                            //createSafeView.setVisibility(View.GONE);
                            /* MODIFIED-END by songlin.qi,BUG-2223767*/
                            /*MODIFIED-END by haifeng.tang,BUG-1910684*/
                            setActionbarTitle(R.string.create_safe_to_cn);
                            /*MODIFIED-END by haifeng.tang,BUG-1950773*/
                        }
                    }
                });

            }
        }).start();

    }


    @Override
    protected void onResume() {
        super.onResume();
        loadData(false);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("SAFE", "this is count" + countSafe + "i is" + i);
        //SafeUtils.getSafeRootPath(this,i,mMountPointManager);
        if (category == SafeStorageAdapter.CATEGORY_STORAGE_LIST) {
            Intent intent = new Intent(this, SafeBoxSettingsActivity.class); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1909322
            intent.putExtra("StorageLocation", i);
            intent.putExtra("isExistSafe", false);
            intent.putExtra("FirstSafebox",mFirstSafebox); // MODIFIED by wenjing.ni, 2016-05-13,BUG-2003636
            startActivity(intent);
            adapter.clear();
            adapter.notifyDataSetChanged();
            finish();
        } else {
            mCurrentSafePath = adapter.getItem(i);
            /*MODIFIED-BEGIN by wenjing.ni, 2016-04-13,BUG-1940959*/
            SharedPreferenceUtils.setCurrentSafeName(this, new File(mCurrentSafePath).getName());
            SharedPreferenceUtils.setCurrentSafeRoot(this, new File(mCurrentSafePath).getParent());
            /*MODIFIED-END by wenjing.ni,BUG-1940959*/
            if (isRelateFingerPrint() && !SafeManager.mInFingerprintLockout && !SharedPreferenceUtils.getFingerPrintLock(this)) {
                /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/
                fingerFailCount = 0;
                popFingerprintDialog(this, mCurrentSafePath);
                ScanFinger();
            } else {
                popPasswordDialog(this, mCurrentSafePath);
                /*MODIFIED-END by haifeng.tang,BUG-1910684*/
            }
            loadData(true); //MODIFIED by wenjing.ni, 2016-04-13,BUG-1940959
        }

    }


    @Override
    public void onBackPressed() {
        if (category == SafeStorageAdapter.CATEGORY_STORAGE_LIST) {
            loadData(true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        /*
        if (view.getId() == R.id.create_safe_box) {
            view.setVisibility(View.GONE);
            setActionbarTitle(R.string.create_safe_to_cn); //MODIFIED by haifeng.tang, 2016-04-18,BUG-1950773
            adapter = new SafeStorageAdapter(this, mApplication, mMountPointManager);
            safeStorage_list.setAdapter(adapter);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    category = SafeStorageAdapter.CATEGORY_STORAGE_LIST;
                    adapter.setCategory(SafeStorageAdapter.CATEGORY_STORAGE_LIST);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.addAll(SafeUtils.getStorageItem(mMountPointManager, FileSafeActivity.this));
                            adapter.notifyDataSetChanged();
                        }
                    });

                }
            }).start();


        }
        */
    }


    private final Runnable mFingerprintLockoutReset = new Runnable() {
        @Override
        public void run() {
            SafeManager.mInFingerprintLockout = false;
            ScanFinger();
        }
    };

    private void ScanFinger() {
        if (!SafeManager.mInFingerprintLockout) {
            mFingerprintCancel = new CancellationSignal();
            if (checkSelfPermission(Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
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

    private void stopFingerprint() {
        if (mFingerprintCancel != null && !mFingerprintCancel.isCanceled()) {
            mFingerprintCancel.cancel();
        }
        mFingerprintCancel = null;
    }

    /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/

    private FingerprintManager.AuthenticationCallback mAuthCallback = new FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            //Toast.makeText(FileSafeActivity.this, "success", Toast.LENGTH_SHORT).show();
            /* MODIFIED-BEGIN by wenjing.ni, 2016-05-14,BUG-2104869*/
            try {
                if (fingerPrintDialog != null && fingerPrintDialog.isShowing()
                        && SafeUtils.getFingerAuthenticationResult(FileSafeActivity.this, result.getFingerprint().getFingerId()) == 0) {
                    fingerFailCount =0;
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
                }
            } catch (Exception e){
            /* MODIFIED-END by wenjing.ni,BUG-2104869*/

            }
        }

        @Override
        public void onAuthenticationFailed() {
            if (fingerPrintDialog != null && fingerPrintDialog.isShowing()) {
                fingerFailCount++;
                if(fingerFailCount <= 4) {
                    Toast.makeText(FileSafeActivity.this, getString(R.string.fingerprint_fail), Toast.LENGTH_SHORT).show();
                }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create_safe_box) {
            setActionbarTitle(R.string.create_safe_to_cn);
            adapter = new SafeStorageAdapter(this, mApplication, mMountPointManager);
            safeStorage_list.setAdapter(adapter);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    category = SafeStorageAdapter.CATEGORY_STORAGE_LIST;
                    adapter.setCategory(SafeStorageAdapter.CATEGORY_STORAGE_LIST);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.addAll(SafeUtils.getStorageItem(mMountPointManager, FileSafeActivity.this));
                            adapter.notifyDataSetChanged();
                        }
                    });

                }
            }).start();
            item.setVisible(false);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /* MODIFIED-BEGIN by songlin.qi, 2016-06-05,BUG-2223767*/
        mCreateSafeBox = menu.findItem(R.id.create_safe_box);
        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1910684*/
        //setAddSafeBoxVisiable(mAddSafeBoxVisiable);
        mCreateSafeBox.setVisible(mAddSafeBoxVisiable);
        /*MODIFIED-END by haifeng.tang,BUG-1910684*/
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.file_safe_menu, menu);
        /* MODIFIED-END by songlin.qi,BUG-2223767*/
        return super.onCreateOptionsMenu(menu);
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
                    SharedPreferenceUtils.setCurrentSafeName(FileSafeActivity.this, new File(mCurrentSafePath).getName());
                    SharedPreferenceUtils.setCurrentSafeRoot(FileSafeActivity.this, new File(mCurrentSafePath).getParent());
                    Intent intent = new Intent(FileSafeActivity.this, FileSafeBrowserActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("isExistSafe", false);
                    intent.putExtra("currentsafepath", mCurrentSafePath);
                    intent.putExtra("currentsaferootpath", new File(mCurrentSafePath).getParent());
                    startActivity(intent);
                    finish();
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

        ;
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
                    Toast.makeText(FileSafeActivity.this, getString(R.string.use_password_auth), Toast.LENGTH_SHORT).show();
                    SharedPreferenceUtils.setFingerPrintLock(mContext,true);
                    if(fingerPrintDialog != null && fingerPrintDialog.isShowing()){
                        fingerPrintDialog.dismiss();
                    }
                    popPasswordDialog(mContext,mCurrentSafePath);
//                    Toast.makeText(FileSafeActivity.this,getResources().getString(R.string.message_security_fingerprint_lockout),
//                            Toast.LENGTH_SHORT).show();
                }
                // Fall through to show message
                break;
        }
        //ScanFinger(); // start again
    }

    public boolean isRelateFingerPrint() {
        boolean isOnOff = (Settings.Global.getInt(this.getContentResolver(), "safe_settings", 0)) == 1;
        boolean isCnOnOff = Settings.System.getInt(getContentResolver(), "tct_filesecurity", 0) == 1;
        if (mFingerprintManagerCompat != null && mFingerprintManagerCompat.isHardwareDetected() && mFingerprintManagerCompat.hasEnrolledFingerprints()
                && SafeUtils.isUserFingerPrint(this) && (isOnOff || isCnOnOff)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopFingerprint();
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
        fingerPrintDialog.setCanceledOnTouchOutside(false); // MODIFIED by songlin.qi, 2016-06-01,BUG-2223763
        fingerPrintDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.positive_text_color));
        CommonUtils.setDialogTitleInCenter(fingerPrintDialog); // MODIFIED by songlin.qi, 2016-06-14,BUG-2269190

    }

    private void popPasswordDialog(final Context context, String safeName) { //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910684
        passwordDialog = null;
        current_safe_name = safeName + File.separator;
        LayoutInflater mlayout = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        View passwordDialogLayout = (View) mlayout.inflate(R.layout.password_dialog, null);
        mPasswrodEdit = (EditText) passwordDialogLayout.findViewById(R.id.password_dialog_edittext);
        /* MODIFIED-BEGIN by songlin.qi, 2016-05-31,BUG-2222816*/
        mPasswrodEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (passwordDialog != null) {
                        passwordDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            }
        });
        /* MODIFIED-END by songlin.qi,BUG-2222816*/
        TextView mPasswrodForget = (TextView) passwordDialogLayout.findViewById(R.id.password_dialog_forget);
        TextView mPasswrodOthers = (TextView) passwordDialogLayout.findViewById(R.id.password_dialog_other);
        mPasswrodOthers.setVisibility(View.GONE); // MODIFIED by haifeng.tang, 2016-04-20,BUG-1925055
        final ImageView showPassword = (ImageView) passwordDialogLayout.findViewById(R.id.btn_show_password); //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910684

        mPasswrodForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passwordDialog != null && passwordDialog.isShowing()) {
                    passwordDialog.dismiss();
                }
                LayoutInflater mlayout = (LayoutInflater) context
                        .getSystemService(LAYOUT_INFLATER_SERVICE);

                View questionDialogLayout = (View) mlayout.inflate(R.layout.question_dialog, null);
                TextView question = (TextView) questionDialogLayout.findViewById(R.id.question_dialog_title);
                mAnswerEdit = (EditText) questionDialogLayout.findViewById(R.id.question_dialog_edittext);
                /* MODIFIED-BEGIN by songlin.qi, 2016-05-31,BUG-2222816*/
                mAnswerEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            if (forgetDialog != null) {
                                forgetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                            }
                        }
                    }
                });
                /* MODIFIED-END by songlin.qi,BUG-2222816*/
                question.setText(SafeManager.queryQuestion(context, current_safe_name));
                forgetDialog = new AlertDialog.Builder(context).setTitle(R.string.set_password_safe_question).setView(questionDialogLayout)
                        .setPositiveButton(R.string.dialog_confirm, new QuestionListener()).setNegativeButton(R.string.cancel, null).show();
                forgetDialog.setCanceledOnTouchOutside(false); // MODIFIED by songlin.qi, 2016-06-01,BUG-2223763
                forgetDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.positive_text_color));
                CommonUtils.setDialogTitleInCenter(forgetDialog); // MODIFIED by songlin.qi, 2016-06-14,BUG-2269190
            }
        });
        mPasswrodOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passwordDialog != null && passwordDialog.isShowing()) {
                    passwordDialog.dismiss();
                }
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
                        /* MODIFIED-BEGIN by songlin.qi, 2016-05-31,BUG-2222816*/
                        mPasswrodEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        mPasswrodEdit.setSelection(mPasswrodEdit.getText().length());
                        showPassword.setTag(true);
                        showPassword.setImageResource(R.drawable.ic_eye); // MODIFIED by haifeng.tang, 2016-04-28,BUG-1995590
                    } else {
                        boolean isDiaplayPassword = (boolean) showPassword.getTag();
                        if (isDiaplayPassword) {
                            //hide password text,eg:set 1234 to ****
                            mPasswrodEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            mPasswrodEdit.setSelection(mPasswrodEdit.getText().length());
                            showPassword.setTag(false);
                            /* MODIFIED-BEGIN by haifeng.tang, 2016-04-28,BUG-1995590*/
                            showPassword.setImageResource(R.drawable.ic_eye_off);
                        } else {
                            //display password text
                            mPasswrodEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            /* MODIFIED-END by songlin.qi,BUG-2222816*/
                            mPasswrodEdit.setSelection(mPasswrodEdit.getText().length());
                            showPassword.setTag(true);
                            showPassword.setImageResource(R.drawable.ic_eye);
                            /* MODIFIED-END by haifeng.tang,BUG-1995590*/
                        }
                    }
                }


            }
        });

        AlertDialog.Builder passwordBuilder = new AlertDialog.Builder(context);
        passwordBuilder.setTitle(SafeManager.getSafeBoxName(mContext, SafeUtils.getCurrentSafePath(mContext))).setView(passwordDialogLayout)
                .setPositiveButton(R.string.dialog_confirm, new ForgetListener());
        passwordBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (passwordDialog != null && passwordDialog.isShowing()) {
                    passwordDialog.dismiss();
                }
            }
        });

        passwordDialog = passwordBuilder.create();
        SafeUtils.fieldDialog(passwordDialog);
        passwordDialog.show();
        passwordDialog.setCanceledOnTouchOutside(false); // MODIFIED by songlin.qi, 2016-06-01,BUG-2223763
        passwordDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.positive_text_color));
        CommonUtils.setDialogTitleInCenter(passwordDialog); // MODIFIED by songlin.qi, 2016-06-14,BUG-2269190
    }


    public boolean isAuthorizationPassword(Context context, String dbPath, String mEditPassword) {
        if (dbPath == null || dbPath.equals("null/")) {
            return false;
        }
        String password = null;
        Cursor cursor = null;
        /* MODIFIED-BEGIN by wenjing.ni, 2016-04-20,BUG-1967152*/
        SQLiteDatabase db = null;
        try {
            PrivateHelper mPrivateHelper = new PrivateHelper(context, dbPath);
            Log.d("QUES", "this is sql path" + dbPath);
            db = mPrivateHelper.getWritableDatabase();
            /* MODIFIED-END by wenjing.ni,BUG-1967152*/
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
    }

    class OpenPasswordListener implements DialogInterface.OnClickListener {
        /*MODIFIED-END by haifeng.tang,BUG-1910684*/
        @Override
        public void onClick(DialogInterface dialog, int id) {
            if (fingerPrintDialog != null && fingerPrintDialog.isShowing()) {
                fingerPrintDialog.dismiss();
            }
            popPasswordDialog(FileSafeActivity.this,current_safe_name);

        }
    }

    class ForgetListener implements DialogInterface.OnClickListener { //MODIFIED by haifeng.tang, 2016-04-09,BUG-1910684
        @Override
        public void onClick(DialogInterface dialog, int id) {
            if (isAuthorizationPassword(mContext, current_safe_name, mPasswrodEdit.getText().toString())) {
                if(passwordDialog!=null && passwordDialog.isShowing()){
                    passwordDialog.dismiss();
                }
                fingerFailCount = 0;
                SharedPreferenceUtils.setFingerPrintLock(mContext,false);
                SharedPreferenceUtils.setCurrentSafeName(mContext, new File(current_safe_name).getName());
                SharedPreferenceUtils.setCurrentSafeRoot(mContext, new File(current_safe_name).getParent());
                Intent intent = new Intent(mContext, FileSafeBrowserActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("isExistSafe", true);
                intent.putExtra("currentsafepath", current_safe_name);
                intent.putExtra("currentsaferootpath", new File(current_safe_name).getParent());
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } else {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.password_mistake), Toast.LENGTH_LONG).show();
            }

        }
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
                startActivity(intent);
                /*MODIFIED-END by wenjing.ni,BUG-1924019*/
            } else {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.answer_mistake), Toast.LENGTH_LONG).show();
            }

        }
    }

    /* MODIFIED-BEGIN by wenjing.ni, 2016-04-22,BUG-1986090*/
    @Override
    protected void onStop() {
        super.onStop();
        if(fingerPrintDialog != null && fingerPrintDialog.isShowing()){
            fingerPrintDialog.dismiss();
            stopFingerprint();
        }
    }
    /* MODIFIED-END by wenjing.ni,BUG-1986090*/
}
