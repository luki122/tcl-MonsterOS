/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.tcl.note.R;
import cn.tcl.note.ui.MatrixImageView;
import cn.tcl.note.util.FileUtils;
import cn.tcl.note.util.ImageLoader;
import cn.tcl.note.util.NoteLog;
import mst.view.menu.bottomnavigation.BottomNavigationView;

/**
 * show all img
 */
public class ImgViewPager extends RootActivity {

    public final static String KEY_IMG_NAME = "imgName";
    private final String TAG = ImgViewPager.class.getSimpleName();
    private ViewPager mViewPager;
    private ArrayList<String> mAllFile;
    //all img's num
    private int mSize;
    //user click which img
    private int mIndex;
    private MatrixImageView[] mAllImgView;
    private ImageView mToolBarBack;
    private BottomNavigationView mCutImg;
    private TextView mTextView;
    private int mCurrentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_viewpager);
        mToolBarBack = (ImageView) findViewById(R.id.view_pager_toolbar_back);
        mToolBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTextView = (TextView) findViewById(R.id.toolbar_text);
        mViewPager = (ViewPager) findViewById(R.id.img_viewpager);
        Intent intent = getIntent();
        mAllFile = intent.getStringArrayListExtra(NoteEditorAdapter.NotePicView.KEY_LIST);
        mSize = mAllFile.size();
        mIndex = intent.getIntExtra(NoteEditorAdapter.NotePicView.KEY_INDEX, 0);
        mAllImgView = new MatrixImageView[mAllFile.size()];
        mViewPager.setAdapter(new MyPagerAdapter());

        setActionBarTitle(mIndex);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                NoteLog.d(TAG, "the " + position + "  pager is selected");
                setActionBarTitle(position);
                boolean fileExit = FileUtils.isExits(FileUtils.getPicWholePath(mAllFile.get(position)));
                mCutImg.setEnabled(fileExit);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mCutImg = (BottomNavigationView) findViewById(R.id.cut_img_bottom);
        mCutImg.setNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Intent intent = getImageClipIntent();
                startActivityForResult(intent, 2);
                return true;
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK) {
            ImageLoader.getInstance(ImageLoader.IMG_0, this).loadBitmap(mAllImgView[mCurrentIndex], mAllFile.get(mCurrentIndex));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        NoteLog.d(TAG, "onStart start");
        mViewPager.setCurrentItem(mCurrentIndex);
        getWindow().setStatusBarColor(getColor(R.color.home_item_background));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        NoteLog.d(TAG, "onReStart start");
//        ImageLoader.getInstance(ImageLoader.IMG_0, this).loadBitmap(mAllImgView[mCurrentIndex], mAllFile.get(mCurrentIndex));
    }

    private void setActionBarTitle(int index) {
        mCurrentIndex = index;
        String title = new StringBuilder().append(index + 1).append("/").append(mSize).toString();
        mTextView.setText(title);
    }

    // create a cut img intent
    private Intent getImageClipIntent() {

        Intent intent = new Intent();
        intent.setClass(this, CropActivity.class);
        String fileName = mAllFile.get(mCurrentIndex);
        ImageLoader.getInstance(ImageLoader.IMG_0, this).removeBitmapFromLruCache(fileName);
        intent.putExtra(KEY_IMG_NAME, fileName);
        return intent;
    }

    class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mAllFile.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            NoteLog.d(TAG, "destroy item :" + position);
            container.removeView(mAllImgView[position]);
            mAllImgView[position] = null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            NoteLog.d(TAG, "instantiate item:" + position);
            MatrixImageView imageView = new MatrixImageView(ImgViewPager.this);
            imageView.setImagePaddingTopByDp(21);
            imageView.setImagePaddingBottomByDp(6);
            mAllImgView[position] = imageView;
            ImageLoader.getInstance(ImageLoader.IMG_0, ImgViewPager.this).loadBitmap(mAllImgView[position], mAllFile.get(position));
            container.addView(mAllImgView[position], 0);
            return mAllImgView[position];
        }
    }

}
