/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.activity;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import cn.tcl.note.R;
import cn.tcl.note.db.DBData;
import cn.tcl.note.soundrecorderserver.RecordAbnormalState;
import cn.tcl.note.soundrecorderserver.SoundRecorderService;
import cn.tcl.note.ui.NotificationHelper;
import cn.tcl.note.ui.RecyclerViewHelper;
import cn.tcl.note.ui.SelectAttachPopupWindow;
import cn.tcl.note.ui.ToastHelper;
import cn.tcl.note.util.FileUtils;
import cn.tcl.note.util.ImageLoader;
import cn.tcl.note.util.NoteLog;
import cn.tcl.note.util.PermissionUtil;
import mst.view.menu.BottomWidePopupMenu;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.widget.toolbar.Toolbar;

/**
 * editor activity
 */
public class NoteEditorActivity extends RootActivity implements RecyclerViewHelper.OnResizeListener,
        Toolbar.OnMenuItemClickListener {

    public final static String ACTION_NEW_ADD = "cn.tcl.note.newadd";
    public final static String ACTION_VIEW = "cn.tcl.note.view";
    public final static String INTENT_KEY_ID = "id";
    private final static String ACTION_RECORD_APP = "com.android.soundrecorder.recordingfilelselector";
    public final static String KEY_DATA = "all_data";
    private final String INTENT_KEY_DATA = "data";
    private final String TAG = NoteEditorActivity.class.getSimpleName();
    //view
    private RecyclerViewHelper mRecyclerView;
    private BottomNavigationView mEditToolButton;
    private LinearLayout mLayoutRoot;
    private NoteEditorAdapter mAdapter;

    private final int REQ_CODE_CAMERA = 1;
    private final int REQ_CODE_ALBUM = 2;
    private final int REQ_CODE_RECORD = 3;
    private SelectAttachPopupWindow mPopupWindow;
    private String mPicName;

    private InputMethodManager mIMM;
    private RecordAbnormalState mRecordAbnormalState;

    private int mMode;
    private final int MODE_ADD = 1;
    private final int MODE_VIEW = 2;

    private ServiceConnection mAudioRecordService;
    //synchronization copy audio or img thread and UI thread
    private static ExecutorService exec = Executors.newSingleThreadExecutor();
    private Semaphore mSemTack = new Semaphore(1);
    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NoteLog.v(TAG, "onCreate start");
        setMstContentView(R.layout.activity_note_editor);
        initView();
        initAdapter();
        mIMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        handleMode();
        NoteLog.v(TAG, "onCreate end");
    }

    //handle different mode
    private void handleMode() {
        if (mMode == MODE_ADD) {
            mEditToolButton.setVisibility(View.VISIBLE);
            mLayoutRoot.setFocusable(false);
            mLayoutRoot.setFocusableInTouchMode(false);
        } else if (mMode == MODE_VIEW) {
            //hide tool bar
            mEditToolButton.setVisibility(View.GONE);
            //EditText can not get focus
            mLayoutRoot.setFocusable(true);
            mLayoutRoot.setFocusableInTouchMode(true);
        }
    }

    /**
     * when user touch edittext,then change mode to add mode
     */
    public boolean changeToEditMode() {
        if (mMode != MODE_ADD) {
            mMode = MODE_ADD;
            handleMode();
            return true;
        }
        return false;
    }

    private void initView() {
        NoteLog.v(TAG, "initView start");
        initToolBar(R.string.app_name);
        mToolBar.setTitle("");
//        mToolBar.setNavigationIcon(null);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToHome();
            }
        });
        mToolBar.inflateMenu(R.menu.menu_edit_toolbar);
        mToolBar.setOnMenuItemClickListener(this);
        mLayoutRoot = (LinearLayout) findViewById(R.id.layout_root);
        mRecyclerView = (RecyclerViewHelper) findViewById(R.id.recyclerView);
        mRecyclerView.setResizeListener(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setItemAnimator(null);
        //save cursor position when scroll
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                NoteLog.d(TAG, "scroll state=" + newState);
                mAdapter.onScrollChange(recyclerView, newState);

            }
        });

        //Toolbar button
        mEditToolButton = (BottomNavigationView) findViewById(R.id.edit_tool_button);
        mEditToolButton.setNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.toolbar_willdo) {
                    mAdapter.changeTextFlag(NoteEditorAdapter.TOOLBAR_WILLDO);
                } else if (menuItem.getItemId() == R.id.toolbar_dot) {
                    mAdapter.changeTextFlag(NoteEditorAdapter.TOOLBAR_DOT);
                } else if (menuItem.getItemId() == R.id.toolbar_pic) {
                    if (!mAdapter.isExceedImgNum()) {
                        ToastHelper.show(NoteEditorActivity.this, R.string.toast_img_max);
                        return true;
                    }
                    if (PermissionUtil.requestWritePermission(NoteEditorActivity.this)) {
                        //create a popup window ,let user chose way that get picture
                        getImgPopWindow();
                    }
                } else if (menuItem.getItemId() == R.id.toolbar_audio) {
                    if (!mAdapter.isExceedAudioNum()) {
                        ToastHelper.show(NoteEditorActivity.this, R.string.toast_audio_max);
                        return true;
                    }
                    if (PermissionUtil.requestRecordPermission(NoteEditorActivity.this)) {
                        //create a popup window ,let user chose way that get picture
                        getAudioPopWindow();
                    }

                } else {

                }
                return true;
            }
        });
        NoteLog.v(TAG, "initView end");
    }

    /**
     * forced show inputMethod,because sometimes inputMethod can not show
     *
     * @return
     */
    public boolean showInputMethod() {
        mIMM.showSoftInput(getCurrentFocus(), 0);
        return true;
    }

    public boolean closeInputMethod() {
        mIMM.hideSoftInputFromWindow(mRecyclerView.getWindowToken(), 0);
        return true;
    }

    //According to differt mode (such as view or new), init RecyclerView adapter
    private void initAdapter() {
        Intent i = getIntent();
        String action = i.getAction();
        NoteLog.i(TAG, "action is " + action);
        if (action == null) {
            //when activity had destroy,user tap notification.then don not resume activity
            finish();
            return;
        }
        if (action.equals(ACTION_NEW_ADD)) {
            // new add mode
            mAdapter = new NoteEditorAdapter(this);
            mMode = MODE_ADD;
        } else if (action.equals(ACTION_VIEW)) {
            // view mode
            mMode = MODE_VIEW;
            long id = i.getLongExtra(INTENT_KEY_ID, -1);
            Cursor cursor = getContentResolver().query(DBData.TABLE_URI,
                    new String[]{DBData.COLUMN_XML, DBData.COLUMN_TIME},
                    DBData.COLUMN_ID + "=?", new String[]{"" + id}, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                String XMLString = cursor.getString(cursor.getColumnIndex(DBData.COLUMN_XML));
                String time = cursor.getString(cursor.getColumnIndex(DBData.COLUMN_TIME));
                NoteLog.d(TAG, "get xml from DB is " + XMLString);
                mAdapter = new NoteEditorAdapter(this, XMLString, id, time);
            }
            cursor.close();

        }
        mRecyclerView.setAdapter(mAdapter);
        //set item move method
        SimpleItemTouchHelperCallback simpleItemTouchHelperCallback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(simpleItemTouchHelperCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    public void startDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtil.REQUEST_CODE_WRITE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImgPopWindow();
                } else {
                    NoteLog.d(TAG, "can't get write permissions");
                }
                break;
            case PermissionUtil.REQUEST_CODE_RECORD:
                int size = grantResults.length;
                if (size > 0) {
                    Boolean result = true;
                    for (int i = 0; i < size; i++) {
                        if ((permissions[i].equals(PermissionUtil.RECORD_PERMISSION)
                                || permissions[i].equals(PermissionUtil.WRITE_PERMISSION))
                                && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            result = false;
                        }
                    }
                    if (result) {
                        getAudioPopWindow();
                    } else {
                        NoteLog.d(TAG, "can't get record permissions");
                    }
                } else {
                    NoteLog.d(TAG, "can't get record permissions");
                }
                break;
            case PermissionUtil.REQUEST_CODE_SHARE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startShare();
                } else {
                    NoteLog.d(TAG, "can't get write permissions");
                }
                break;
        }
    }


    private void getImgPopWindow() {
        mPopupWindow = new SelectAttachPopupWindow(this, new BottomWidePopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onItemClicked(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.first_line) {
                    getPicFromCamera();
                } else if (menuItem.getItemId() == R.id.second_line) {
                    getPicFromAlbum();
                }
                return false;
            }
        }, R.string.pic_take_camera, R.string.pic_get_album);
        mPopupWindow.showAtLocation(mLayoutRoot, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void getAudioPopWindow() {
        mPopupWindow = new SelectAttachPopupWindow(this, new BottomWidePopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onItemClicked(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.first_line) {
                    mRecordAbnormalState = new RecordAbnormalState(NoteEditorActivity.this);
                    mRecordAbnormalState.beforeStartRecord();
                } else if (menuItem.getItemId() == R.id.second_line) {
                    getAudioFromApp();
                }
                return false;
            }
        }, R.string.audio_record, R.string.audio_get_app);
        mPopupWindow.showAtLocation(mLayoutRoot, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    //start record service
    public void startAudioRecord() {
        NoteLog.d(TAG, "bind record service");
        if (mAudioRecordService != null) {
            unbindService(mAudioRecordService);
        }
        mAudioRecordService = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
//                setButtonEnable(false, R.id.toolbar_audio);
                SoundRecorderService mService = ((SoundRecorderService.SoundRecorderBinder) service).getService();
                mService.setRecordAbnormalState(mRecordAbnormalState);
                mAdapter.addAudioRecordLine(mService);
                NoteLog.d(TAG, "bind record service success");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent intent = new Intent(this, SoundRecorderService.class);
        bindService(intent, mAudioRecordService, BIND_AUTO_CREATE);
    }

    /**
     * 0<=result<=3
     * 00 -> willdo enable, dot enable
     * 01 ->willdo enable, dot disable
     * 10 ->willdo disable, dot enable
     * 11 ->willdo disable, dot disable
     *
     * @param result
     */
    public void setWillDotEnable(int result) {
        NoteLog.d(TAG, "willdo dot enable result=" + result);
        setButtonEnable((result >> 1) == 0, R.id.toolbar_willdo);
        setButtonEnable((result & 1) == 0, R.id.toolbar_dot);
    }

    public void setButtonEnable(boolean enabled, int itemID) {
//        mEditToolButton.setItemEnable(itemID, enabled);
        if (itemID == R.id.toolbar_audio) {
            mEditToolButton.setItemEnable(itemID, enabled);
        }
        MenuItem item = mEditToolButton.getMenu().findItem(itemID);
        Drawable drawable = item.getIcon();
        int alphaValue = enabled ? 255 : 50;
        drawable.setAlpha(alphaValue);
        item.setIcon(drawable);
    }

    public void stopAudioRecord() {
        mAdapter.stopRecord();
    }

    public int getRecordState() {
        return mAdapter.getRecordState();
    }

    /**
     * after stop record,enable record button
     */
    public void releaseRecordButton() {
        setButtonEnable(true, R.id.toolbar_audio);
    }

    private void getPicFromAlbum() {

        Intent intent = new Intent();
        intent.setClassName("cn.tcl.filemanager","cn.tcl.filemanager.photopicker.ImagePickerPlusActivity");
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQ_CODE_ALBUM);
        }



//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(intent, REQ_CODE_ALBUM);
//        }
    }

    private void getPicFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mPicName = FileUtils.getPicName();
        if (mPicName == null) {
            return;
        }
        File newFile = new File(FileUtils.getPicWholePath(mPicName));
        Uri uri = FileProvider.getUriForFile(this, "cn.tcl.file", newFile);
        NoteLog.d(TAG, "file=" + newFile + "  uri=" + uri);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQ_CODE_CAMERA);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        NoteLog.d(TAG, "onRestart start");
        mAdapter.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        NoteLog.d(TAG, "onPause start");
        mAdapter.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NoteLog.d(TAG, "onResume start");
        mAdapter.onResume();
        NotificationHelper.cancelNotification(this);
        if (mRecordAbnormalState != null) {
            mRecordAbnormalState.resumeDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NoteLog.d(TAG, "onDestroy start");
        ImageLoader.getInstance(ImageLoader.IMG_16, this).clearCache();
        if (mAudioRecordService != null) {
            unbindService(mAudioRecordService);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            ArrayList<Uri> uriList;
            switch (requestCode) {
                case REQ_CODE_CAMERA:
                    //mPicName is the file name,adapter can load image by file name
                    mAdapter.addNewPicLine(mPicName);
                    break;
                case REQ_CODE_ALBUM:
                    addMultiAudioImg(data, NoteEditorAdapter.TYPE_PIC);
                    break;
                case REQ_CODE_RECORD:
                    addMultiAudioImg(data, NoteEditorAdapter.TYPE_AUDIO);
                    break;
            }
        }
    }

    //add multi audio or img,type is attachemnt type.
    // The value is TYPE_TEXT = 1,TYPE_PIC = 2,TYPE_AUDIO = 3;
    private void addMultiAudioImg(Intent data, int type) {
        ArrayList<Uri> uriList = getDataFromIntent(data, type);

        for (Uri uri : uriList) {
            switch (type) {
                case NoteEditorAdapter.TYPE_PIC:
                    UriToFileTask addTask = new UriToFileTask();
                    addTask.executeOnExecutor(exec, uri);
                    break;
                case NoteEditorAdapter.TYPE_AUDIO:
                    CopyAudioTask addAudioTask = new CopyAudioTask();
                    addAudioTask.executeOnExecutor(exec, uri);
                    break;
            }
        }
    }

    public void moveRecyY(float moveY) {
        NoteLog.d(TAG, "move y=" + moveY);
        mRecyclerView.offsetChildrenVertical((int) moveY);
    }


    private ArrayList<Uri> getDataFromIntent(Intent intent, int type) {
        ArrayList<Uri> uriList = new ArrayList<>();
        Uri uri = intent.getData();
        if (uri == null) {
            uriList = intent.getParcelableArrayListExtra(INTENT_KEY_DATA);
        } else {
            uriList.add(uri);
        }
        if (NoteLog.DEBUG) {
            for (Uri uriTemp : uriList) {
                NoteLog.d(TAG, "get uri=" + uriTemp);
            }
        }
        //can not >10
        int num = 0;
        int strId = 0;
        switch (type) {
            case NoteEditorAdapter.TYPE_PIC:
                num = mAdapter.getImgNum();
                strId = R.string.toast_img_max;
                break;
            case NoteEditorAdapter.TYPE_AUDIO:
                strId = R.string.toast_audio_max;
                num = mAdapter.getAudioNum();
                break;
        }
        if ((num + uriList.size()) > 10) {
            ToastHelper.show(this, strId);
            int leftNum = 10 - num;
            ArrayList<Uri> temp = new ArrayList<>();
            for (int i = 0; i < leftNum; i++) {
                temp.add(uriList.get(i));
            }
            uriList = temp;
        }
        return uriList;
    }

    /**
     * When one item can't see,can move to the item's position.
     *
     * @param postion
     */
    public void moveToPosition(int postion) {
        NoteLog.d(TAG, "move to position " + postion);
        mRecyclerView.scrollToPosition(postion);

    }

    @Override
    public void afterResize() {
        NoteLog.d(TAG, "after MyRecyclerView resize");
        moveToPosition(mAdapter.getCurrentLine());
    }

    @Override
    public boolean onTouchBlank() {
        NoteLog.d(TAG, "touch blank");
        if (!mAdapter.getIsScroll()) {
            NoteLog.d(TAG, "show input");
            changeToEditMode();
            mAdapter.setLastLineCursor();
            showInputMethod();
        } else {
            NoteLog.d(TAG, "con't show input");
        }
        return true;
    }

    //get audio from soundrecord
    private void getAudioFromApp() {
        Intent intent = new Intent();
        intent.setAction(ACTION_RECORD_APP);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQ_CODE_RECORD);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backToHome();
            return true;
        }
        return false;
    }

    private void backToHome() {
        if (mAdapter.onHandleBack()) {
            finish();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_theme:
                Intent intent = new Intent(this, ThemeActivity.class);
                startActivity(intent);
                break;
            case R.id.toolbar_share:
                if (PermissionUtil.requestWritePermission(this, PermissionUtil.REQUEST_CODE_SHARE)) {
                    startShare();
                }
                break;
        }
        return true;
    }

    private void startShare() {
        saveEditText();
        mAdapter.stopPlayingAudioLine();
        Intent shareIntent = new Intent(this, ShareActivity.class);
        shareIntent.putExtra(KEY_DATA, mAdapter.getAllData());
        startActivity(shareIntent);
    }

    /**
     * when TimeSaveThread save data after 30s,need save edittext that are edit
     */
    public void saveEditText() {
        mAdapter.saveEditText();
    }

    /**
     * start a task that copy uri img to note file.
     */
    class UriToFileTask extends AsyncTask<Uri, Void, String> {
        private final String LOWER_SPACE = "lowerSpace";

        @Override
        protected String doInBackground(Uri... params) {
            ContentResolver contentResolver = getContentResolver();
            InputStream is;
            try {
                is = contentResolver.openInputStream(params[0]);
                mPicName = FileUtils.getPicName();
                if (mPicName == null) {
                    return null;
                }
                int result = FileUtils.copyToFile(is, new File(FileUtils.getPicWholePath(mPicName)));
                if (result == 0) {
                    try {
                        mSemTack.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return mPicName;
                } else {
                    if (result == -2) {
                        return LOWER_SPACE;
                    }
                    return null;
                }
            } catch (FileNotFoundException e) {
                NoteLog.e(TAG, "image uri can not find", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String picName) {
            if (picName != null) {
                if (picName.equals(LOWER_SPACE)) {
                    ToastHelper.show(NoteEditorActivity.this, R.string.toast_no_space);
                    return;
                }
                mAdapter.addNewPicLine(picName);
                mSemTack.release();
            }
        }
    }

    /**
     * copy audio to to note file
     */
    class CopyAudioTask extends AsyncTask<Uri, Void, String> {
        private final String LOWER_SPACE = "lowerSpace";
        private long duration;

        @Override
        protected String doInBackground(Uri... params) {

            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(params[0], new String[]{
                    MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION}, null, null, null);
            String filePath = "";
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media
                        .DATA));
                duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media
                        .DURATION));
                NoteLog.d(TAG, "get audio path=" + filePath + "  duration=" + duration);
            }
            cursor.close();

            InputStream is;
            try {
                is = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                NoteLog.e(TAG, "open record file error", e);
                return null;
            }
            String fileName = new File(filePath).getName();
            fileName = FileUtils.getAudioName(fileName);
            NoteLog.d(TAG, "get audio file name is " + fileName);
            int result = FileUtils.copyToFile(is, new File(FileUtils.getAudioWholePath(fileName)));
            if (result == 0) {
                try {
                    mSemTack.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return fileName;
            } else {
                if (result == -2) {
                    return LOWER_SPACE;
                }
                return null;
            }
        }

        @Override
        protected void onPostExecute(String audioName) {
            if (audioName != null) {
                if (audioName.equals(LOWER_SPACE)) {
                    ToastHelper.show(NoteEditorActivity.this, R.string.toast_no_space);
                    return;
                }
                NoteLog.d(TAG, "add new record");
                mAdapter.addAudioFromApp(audioName, duration);
                mSemTack.release();
            }
        }
    }
}
