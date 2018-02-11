/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.DensityUtil;
import cn.tcl.meetingassistant.utils.FileUtils;
import cn.tcl.meetingassistant.utils.ImageLoader;
import cn.tcl.meetingassistant.view.AbsMeetingActivity;
import cn.tcl.meetingassistant.view.DialogHelper;
import cn.tcl.meetingassistant.view.MatrixImageView;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.widget.toolbar.Toolbar;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-24.
 * THe Activity to handle with the PhotoPreview
 */

public class PhotoPreviewActivity extends AbsMeetingActivity {

    private ViewPager mViewPager;

    private PhotoPreviewAdapter mPhotoPreviewAdapter;

    private TextView mTextView;

    List<File> mFileList = new ArrayList<>();
    private BottomNavigationView mBottomNavigationView;

    private ImageButton mBackBtn;

    private EditImportPointActivity.ImageInfo mImageInfo;

    private final String TAG = PhotoPreviewActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);

        mImageInfo = getIntent().
                getParcelableExtra(EditImportPointActivity.IMAGE_INFO_TO_PREVIEW);

        initView();

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        File[] files = FileUtils.getImageFilesByTime(mImageInfo.path);

        for (File file : files) {
            mFileList.add(file);
        }

        mPhotoPreviewAdapter = new PhotoPreviewAdapter(this);
        mPhotoPreviewAdapter.setFiles(mFileList);
        mViewPager.setAdapter(mPhotoPreviewAdapter);
        mViewPager.setCurrentItem(mImageInfo.num - 1);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mTextView.setText(String.valueOf(mViewPager.getCurrentItem() + 1) + "/" + String.valueOf(mFileList.size()));
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mTextView = (TextView) findViewById(R.id.picture_number);
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.photo_preview_delete_layout);
        mBackBtn = (ImageButton) findViewById(R.id.photo_preview_page_back_btn);
        Drawable navigationIcon = getResources().getDrawable(com.mst.R.drawable.ic_toolbar_back
                , getTheme());
        mBackBtn.setBackground(navigationIcon);
        mBottomNavigationView.inflateMenu(R.menu.photo_preview_menu);
        mBottomNavigationView.setNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.photo_preview_delete) {
                    DialogHelper.showDialog(PhotoPreviewActivity.this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == DialogInterface.BUTTON_POSITIVE) {
                                deleteImageFile(mFileList.get(mViewPager.getCurrentItem()));
                                MeetingStaticInfo.updateCurrentTime(PhotoPreviewActivity.this);
                            }
                        }
                    }, R.string.delete, R.string.delete_pic_msg, R.string.Confirm, R.string.cancel);
                }
                return false;
            }
        });
    }


    private void deleteImageFile(File file) {
        ImageDeleter deleter = new ImageDeleter();
        deleter.execute(file);
    }

    class ImageDeleter extends AsyncTask<File, Integer, Boolean> {

        File file;

        @Override
        protected Boolean doInBackground(File... files) {
            file = files[0];
            return file.delete();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            //TODO need to modify
            if (aBoolean) {
                Toast.makeText(PhotoPreviewActivity.this, "delete success", Toast.LENGTH_SHORT).show();
                mFileList.remove(file);
                if (mFileList.size() == 1) {
                    mPhotoPreviewAdapter = new PhotoPreviewAdapter(PhotoPreviewActivity.this);
                    mPhotoPreviewAdapter.setFiles(mFileList);
                    mViewPager.setAdapter(mPhotoPreviewAdapter);
                } else {
                    mPhotoPreviewAdapter.notifyDataSetChanged();
                }
                // if there is no file, finish this activity
                if (mFileList.size() <= 0) {
                    finish();
                }
            } else {
                Toast.makeText(PhotoPreviewActivity.this, "delete fail", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class PhotoPreviewAdapter extends PagerAdapter {

        private int mChildCount = 0;

        private List<File> files;
        private Context mContext;

        public PhotoPreviewAdapter(Context context) {
            this.mContext = context;
        }


        public void setFiles(List<File> files) {
            this.files = files;
        }

        public void removeFile(File file) {
            if (files.contains(file)) {
                files.remove(file);
            }
        }

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            String path = files.get(position).getAbsolutePath();
            ImageLoader loader = ImageLoader.getInstance(ImageLoader.IMG_0, mContext);
            MatrixImageView imageView = new MatrixImageView(mContext);
            loader.loadBitmap(imageView, path, true);
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            MeetingLog.i(PhotoPreviewAdapter.class.getSimpleName(), "destroyItem " + position);
            ImageView imageView = (ImageView) object;
            container.removeView((View) object);
            if (imageView.getTag() instanceof Bitmap) {
                Bitmap bitmap = (Bitmap) imageView.getTag();
                bitmap.recycle();
            }
        }

        @Override
        public void notifyDataSetChanged() {
            mChildCount = getCount();
            super.notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            if (mChildCount > 0) {
                mChildCount--;
                return POSITION_NONE;
            }
            return super.getItemPosition(object);

        }
    }
}

