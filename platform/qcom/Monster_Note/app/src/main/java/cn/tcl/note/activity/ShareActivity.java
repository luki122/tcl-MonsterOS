/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.tcl.note.R;
import cn.tcl.note.data.CommonData;
import cn.tcl.note.data.NoteAudioData;
import cn.tcl.note.data.NotePicData;
import cn.tcl.note.data.NoteTextData;
import cn.tcl.note.ui.DialogHelper;
import cn.tcl.note.ui.ToastHelper;
import cn.tcl.note.util.FileUtils;
import cn.tcl.note.util.ImageLoader;
import cn.tcl.note.util.NoteLog;
import mst.view.menu.bottomnavigation.BottomNavigationView;

public class ShareActivity extends RootActivity {
    private final String TAG = ShareActivity.class.getSimpleName();
    private ArrayList<CommonData> mAllData;
    private LinearLayout mAddContent;
    private LinearLayout mRootContent;
    private Bitmap mShareBitmap;

    private int mTextImgH;
    private int mImgTextH;
    private int mImgImgH;
    private int mTextTextH;
    private int mShadowH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_share);
        mAllData = (ArrayList<CommonData>) getIntent().getSerializableExtra(NoteEditorActivity.KEY_DATA);
        removeAudioData();
        initView();
    }

    private void removeAudioData() {
        for (int i = 0; i < mAllData.size(); i++) {
            if (mAllData.get(i) instanceof NoteAudioData) {
                mAllData.remove(i);
                i--;
            }
        }
    }

    private void initView() {
        initToolBar(R.string.toolbar_share);
        mRootContent = (LinearLayout) findViewById(R.id.share_content_root);
        mAddContent = (LinearLayout) findViewById(R.id.share_content_add);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = null;
        CheckBox checkBox = null;
        ImageView point = null;
        TextView textView = null;
        ImageView img = null;
        int i = -1;
        int height = 0;
        CommonData aboveData;
        for (CommonData commonData : mAllData) {
            i++;
            if (mTextTextH == 0) {
                mTextTextH = (int) getResources().getDimension(R.dimen.share_text_text);
                mImgImgH = (int) getResources().getDimension(R.dimen.share_img_img);
                mTextImgH = (int) getResources().getDimension(R.dimen.share_text_img);
                mImgTextH = (int) getResources().getDimension(R.dimen.share_img_text);
                mShadowH = (int) getResources().getDimension(R.dimen.img_shadow_height);
            }
            if (i > 0) {
                aboveData = mAllData.get(i - 1);
                if (commonData instanceof NotePicData) {
                    if (aboveData instanceof NotePicData) {
                        height = mImgImgH - mShadowH;
                    } else if (aboveData instanceof NoteTextData) {
                        height = mTextImgH;
                    }
                    height -= mShadowH;
                } else if (commonData instanceof NoteTextData) {
                    if (aboveData instanceof NotePicData) {
                        height = mImgTextH - mShadowH;
                    } else if (aboveData instanceof NoteTextData) {
                        height = mTextTextH;
                    }
                }
            } else {
                height = 0;
            }
            if (commonData instanceof NoteTextData) {
                view = inflater.inflate(R.layout.share_item_text, mAddContent, false);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                lp.topMargin = height;
                checkBox = (CheckBox) view.findViewById(R.id.item_willdo);
                point = (ImageView) view.findViewById(R.id.item_dot);
                textView = (TextView) view.findViewById(R.id.share_text);
                NoteTextData noteTextData = (NoteTextData) commonData;
                checkBox.setEnabled(false);
                switch (noteTextData.getFlag()) {
                    case NoteTextData.FLAG_NO:
                        checkBox.setVisibility(View.GONE);
                        point.setVisibility(View.GONE);
                        break;
                    case NoteTextData.FLAG_WILLDO_UN:
                        point.setVisibility(View.GONE);
                        checkBox.setVisibility(View.VISIBLE);
                        checkBox.setChecked(false);
                        break;
                    case NoteTextData.FLAG_WILLDO_CK:
                        point.setVisibility(View.GONE);
                        checkBox.setVisibility(View.VISIBLE);
                        checkBox.setChecked(true);
                        textView.setTextColor(getResources().getColor(R.color.text_check));
                        break;
                    case NoteTextData.FLAG_DOT:
                        point.setVisibility(View.VISIBLE);
                        checkBox.setVisibility(View.GONE);
                        break;
                }
                textView.setVisibility(View.VISIBLE);
                textView.setText(noteTextData.getText());

                mAddContent.addView(view);
            } else if (commonData instanceof NotePicData) {
                NotePicData picData = (NotePicData) commonData;
                view = inflater.inflate(R.layout.share_item_img, mAddContent, false);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                lp.topMargin = height;
                img = (ImageView) view.findViewById(R.id.share_item_img);
                ImageLoader.getInstance(ImageLoader.IMG_2, this).loadBitmap(img, picData.getFileName());
                mAddContent.addView(view);
            }

        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.share_button);
        bottomNavigationView.setNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                DialogHelper.showProgressDialog(ShareActivity.this);
                if (menuItem.getItemId() == R.id.share_save) {
                    new SaveBitmapToAlbum().execute();
                } else if (menuItem.getItemId() == R.id.share_share) {
                    shareApp();
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private Bitmap viewToImg() {
        if (mShareBitmap == null) {
            while (!ImageLoader.getInstance(ImageLoader.IMG_2, this).iSloadFinish()) {
                NoteLog.d(TAG, "wait load img Zzzzz....");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mShareBitmap = Bitmap.createBitmap(mRootContent.getWidth(), mRootContent.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mShareBitmap);
            mRootContent.draw(canvas);
        }
        return mShareBitmap;
    }

    private void shareApp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtils.writeShareImg(viewToImg(), ShareActivity.this);
            }
        }).start();

    }

    private void recycleBitmap() {
        if (mShareBitmap != null) {
            mShareBitmap.recycle();
            mShareBitmap = null;
        }
    }

    @Override
    protected void onDestroy() {
        recycleBitmap();
        super.onDestroy();
    }

    class SaveBitmapToAlbum extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = FileUtils.saveShareImg(ShareActivity.this, viewToImg());
            recycleBitmap();
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            DialogHelper.disProgressDialog();
            if (result) {
                ToastHelper.show(ShareActivity.this, R.string.share_save_album);
            } else {
                ToastHelper.show(ShareActivity.this, R.string.share_save_album_fail);
            }
        }
    }
}
