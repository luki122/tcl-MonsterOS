/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.photopicker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.support.v4.util.LongSparseArray;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.activity.FileBaseActionbarActivity;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.PermissionUtil;
import mst.widget.toolbar.Toolbar;

public class ImagePickerPlusActivity extends FileBaseActionbarActivity {

    public static final String EXTRA_DISK_CACHE_PATH = "extra_disk_cache_path";
    private String a;
    private final static int RESULT_MUT_PIC = 0x0A;

    public static final String EXTRA_PICK_RETURN_DATA = "data";

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (isAlbumMode) {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }
            isAlbumMode = !isAlbumMode;
            if (adapter instanceof MyCursorAdapter) {
                try {
                    ((MyCursorAdapter) adapter).changeCursor(getAlbumnAdapterDatas());
                } catch (Exception e) {
                }
            } else {
                adapter.notifyDataSetChanged();
            }
            if (!isAlbumMode) {
                lv.setSelection(0);
            }
            return true;
        } else
        if (id == R.id.action_finish) {
            int b = datas.size();  //get the size of data
            Intent intent = new Intent();
            if (b == 1) {  //if choose only one picture, return result with type data
                setResult(RESULT_OK, intent.setData(datas.get(0).uri));
            } else {  //if choose one more picture, return result with intent-->UriList
                setResult(RESULT_OK, intent.putParcelableArrayListExtra(EXTRA_PICK_RETURN_DATA, picUriList));
            }
            finish();
            return true;
        }
        return super.onMenuItemClick(item);
    }

    private MenuItem menuItem;

    @Override
    public void updateOptionMenu(){
        menuItem = toolbar.getMenu().findItem(R.id.action_finish);
        String str = null;  //use placeholder
        menuItem.setTitle(str);
        menuItem.setEnabled(false);
    }

    private boolean isAlbumMode = true;
    private BaseAdapter adapter;
    private boolean flag = true;
    private Handler mHandler;

    private ArrayList<String> choicePhotoPaths = new ArrayList<>();
    private ArrayList<ItemImageInfo> datas = new ArrayList<>();
    private ArrayList<Uri> picUriList = new ArrayList<Uri>();

    private int CAN_CHECK_COUNT;
    private LongSparseArray<AlbumInfo> itemAlbumDatas;
    private ListView lv;
    private int imgViewWidthAndHeight;

    private long clickAlbumId;
    private View clickItemView;

    private String diskCachePath;
    private Toolbar toolbar;

    @Override
    protected void onDestroy() {
        flag = false;
        if (adapter instanceof MyCursorAdapter) {
            if (!((MyCursorAdapter) adapter).getCursor().isClosed()) {
                ((MyCursorAdapter) adapter).getCursor().close();
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return backPress();
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean backPress(){
        if (isAlbumMode) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        isAlbumMode = !isAlbumMode;
        if (adapter instanceof MyCursorAdapter) {
            try {
                ((MyCursorAdapter) adapter).changeCursor(getAlbumnAdapterDatas());
            } catch (Exception e) {
            }
        } else {
            adapter.notifyDataSetChanged();
        }
        if (!isAlbumMode) {
            lv.setSelection(0);
        }
        toolbar.setTitle(getString(R.string.category_pictures));
        return true;
    }

    private void setLayoutHeight(ImageView imageView) {
        ViewGroup.LayoutParams vl = imageView.getLayoutParams();
        if (vl == null) {
            vl = new ViewGroup.LayoutParams(imgViewWidthAndHeight, imgViewWidthAndHeight);
        }
        vl.width = imgViewWidthAndHeight;
        vl.height = imgViewWidthAndHeight;
        imageView.setLayoutParams(vl);
        imageView.setImageBitmap(null);
    }

    LoadPhonePhotoThread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_picker_plus_layout);
        toolbar = (Toolbar) findViewById(R.id.photopicker_toolbar);
        toolbar.inflateMenu(R.menu.main_finish);
        toolbar.setTitle(getString(R.string.category_pictures));
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPress();
            }
        });
        lv = (ListView) findViewById(R.id.list_view);
        if (isGrantExternalRW(this)) {
            loadData();
            updateOptionMenu();
        }
        diskCachePath = getIntent().getStringExtra(EXTRA_DISK_CACHE_PATH);
        if (!TextUtils.isEmpty(diskCachePath)) {
            new File(diskCachePath).mkdirs();
        }
        CAN_CHECK_COUNT = 10;  //set CAN_CHECK_COUNT for the superior limit choosing the picture
        setActionbarTitle(getString(R.string.select_pic));

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);  //put some information on the current window in the DisplayMetrics class

        imgViewWidthAndHeight = outMetrics.widthPixels / 3;

        try {
            itemAlbumDatas = new LongSparseArray<>();
        } catch (Exception e) {
            itemAlbumDatas = getAllAdapterDatas();
        }
    }

    private void loadData() {
        t = new LoadPhonePhotoThread();
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                ImageView imgView = (ImageView) msg.obj;
                Bitmap b = msg.getData().getParcelable("bitmap");
                Long msgId = msg.getData().getLong("imgId");
                Long nowMsgId = (Long) imgView.getTag();
                if (msgId.longValue() == nowMsgId.longValue()) {
                    if (null != b) {
                        imgView.setImageBitmap(b);
                    } else {
                        imgView.setImageDrawable(null);
                    }
                } else {
                    imgView.setImageDrawable(null);
                }
            }
        };

        Cursor albumCursor = null;
        try {
            albumCursor = getAlbumnAdapterDatas();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (albumCursor != null) {
            adapter = new MyCursorAdapter(this, albumCursor, false);
        } else {
            adapter = new MyBaseAdapter();
        }
        lv.setAdapter(adapter);
    }

    /**
     * if build version > 23,request permissions;
     * @param context
     * @return
     */
    public boolean isGrantExternalRW(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ((Activity) context).requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT);
            return false;
        }
        return true;
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT == requestCode) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else {
                    loadData();
                }
            }
        }
    }

    /**
     * if view is null, set a fixed view, then refresh the view for new data
     */
    class MyBaseAdapter extends BaseAdapter {
        private int size = itemAlbumDatas.size();
        private LayoutInflater layoutInfalter = getLayoutInflater();

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {  //if convertView is null, must init the item
                view = layoutInfalter.inflate(R.layout.list_grid_item, parent, false);
            }

            ImageView imageView = (ImageView) view.findViewById(R.id.image_item);
            ImageView imageView2 = (ImageView) view.findViewById(R.id.image_item_2);
            ImageView imageView3 = (ImageView) view.findViewById(R.id.image_item_3);

            setLayoutHeight(imageView);
            setLayoutHeight(imageView2);
            setLayoutHeight(imageView3);

            TextView tv = (TextView) view.findViewById(R.id.tv_info);
            TextView tv2 = (TextView) view.findViewById(R.id.tv_info2);
            TextView tv_2 = (TextView) view.findViewById(R.id.tv_info_2);
            TextView tv2_2 = (TextView) view.findViewById(R.id.tv_info2_2);
            TextView tv_3 = (TextView) view.findViewById(R.id.tv_info_3);
            TextView tv2_3 = (TextView) view.findViewById(R.id.tv_info2_3);

            View bottomCt = view.findViewById(R.id.bottom_ct);
            View bottomCt2 = view.findViewById(R.id.bottom_ct_2);
            View bottomCt3 = view.findViewById(R.id.bottom_ct_3);

            View allCt = view.findViewById(R.id.all_ct);
            View allCt2 = view.findViewById(R.id.all_ct2);
            View allCt3 = view.findViewById(R.id.all_ct3);

            allCt.setOnClickListener(onItemClick);
            allCt2.setOnClickListener(onItemClick);
            allCt3.setOnClickListener(onItemClick);

            CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox);
            CheckBox cb2 = (CheckBox) view.findViewById(R.id.checkbox_2);
            CheckBox cb3 = (CheckBox) view.findViewById(R.id.checkbox_3);

            TextView blackBg = (TextView) view.findViewById(R.id.tv_checked_bg);
            TextView blackBg2 = (TextView) view.findViewById(R.id.tv_checked_bg2);
            TextView blackBg3 = (TextView) view.findViewById(R.id.tv_checked_bg3);

            cb.setTag(R.string.view_tag_key, blackBg);
            cb2.setTag(R.string.view_tag_key, blackBg2);
            cb3.setTag(R.string.view_tag_key, blackBg3);

            allCt.setTag(R.string.view_tag_key, R.id.checkbox);
            allCt2.setTag(R.string.view_tag_key, R.id.checkbox_2);
            allCt3.setTag(R.string.view_tag_key, R.id.checkbox_3);

            if (isAlbumMode) {

                cb.setVisibility(View.GONE);
                cb2.setVisibility(View.GONE);
                cb3.setVisibility(View.GONE);

                blackBg.setVisibility(View.GONE);
                blackBg2.setVisibility(View.GONE);
                blackBg3.setVisibility(View.GONE);

                bottomCt.setVisibility(View.VISIBLE);
                bottomCt2.setVisibility(View.VISIBLE);
                bottomCt3.setVisibility(View.VISIBLE);

                AlbumInfo albumInfo;
                ItemImageInfo imgInfo;
                int dataSize = itemAlbumDatas.size();

                if (position * 3 < dataSize) {  //if position * 3 < dataSize,get albumInfo albumInfo.photoCount and albumInfo.choiceCount, then setText ande Visibility
                    albumInfo = itemAlbumDatas.valueAt(position * 3);
                    imgInfo = albumInfo.getConver();
                    t.addTask(imgInfo.filePath, imageView, imgInfo.imageId, imgInfo.orientation);
                    bottomCt.setVisibility(View.VISIBLE);
                    tv.setText(albumInfo.albumName);
                    String photoStr = String.format(getString(R.string.brackets_double), albumInfo.photoCount);  //seize a seat
                    tv2.setText(photoStr);  //get the albumName of photo
                    if (albumInfo.choiceCount > 0) {
                        String choiceStr = String.format(getString(R.string.selected_pick), albumInfo.choiceCount);
                        blackBg.setText(choiceStr);  //get the albumName of choiceCount
                        blackBg.setVisibility(View.VISIBLE);
                    }
                    allCt.setTag(albumInfo.albumId);
                    allCt.setVisibility(View.VISIBLE);
                } else {
                    allCt.setVisibility(View.INVISIBLE);
                }

                if (position * 3 + 1 < dataSize) {  //if position * 3 + 1 < dataSize,get albumInfo albumInfo.photoCount and albumInfo.choiceCount, then setText ande Visibility
                    albumInfo = itemAlbumDatas.valueAt(position * 3 + 1);
                    imgInfo = albumInfo.getConver();
                    t.addTask(imgInfo.filePath, imageView2, imgInfo.imageId, imgInfo.orientation);
                    bottomCt2.setVisibility(View.VISIBLE);
                    tv_2.setText(albumInfo.albumName);
                    String photoStr = String.format(getString(R.string.brackets_double), albumInfo.photoCount);  //seize a seat
                    tv2_2.setText(photoStr);
                    if (albumInfo.choiceCount > 0) {
                        String choiceStr = String.format(getString(R.string.selected_pick), albumInfo.choiceCount);
                        blackBg2.setText(choiceStr);
                        blackBg2.setVisibility(View.VISIBLE);
                    }
                    allCt2.setTag(albumInfo.albumId);
                    allCt2.setVisibility(View.VISIBLE);
                } else {
                    allCt2.setVisibility(View.INVISIBLE);
                }

                if (position * 3 + 2 < dataSize) {  //if position * 3 + 2 < dataSize,get albumInfo albumInfo.photoCount and albumInfo.choiceCount, then setText ande Visibility
                    albumInfo = itemAlbumDatas.valueAt(position * 3 + 2);
                    imgInfo = albumInfo.getConver();
                    t.addTask(imgInfo.filePath, imageView3, imgInfo.imageId, imgInfo.orientation);
                    bottomCt3.setVisibility(View.VISIBLE);
                    tv_3.setText(albumInfo.albumName);
                    String photoStr = String.format(getString(R.string.brackets_double), albumInfo.photoCount);  //seize a seat
                    tv2_3.setText(photoStr);
                    if (albumInfo.choiceCount > 0) {
                        String choiceStr = String.format(getString(R.string.selected_pick), albumInfo.choiceCount);
                        blackBg3.setText(choiceStr);
                        blackBg3.setVisibility(View.VISIBLE);
                    }
                    allCt3.setTag(albumInfo.albumId);
                    allCt3.setVisibility(View.VISIBLE);
                } else {
                    allCt3.setVisibility(View.INVISIBLE);
                }

            } else {

                cb.setVisibility(View.VISIBLE);
                cb2.setVisibility(View.VISIBLE);
                cb3.setVisibility(View.VISIBLE);

                cb.setOnCheckedChangeListener(null);
                cb2.setOnCheckedChangeListener(null);
                cb3.setOnCheckedChangeListener(null);

                bottomCt.setVisibility(View.GONE);
                bottomCt2.setVisibility(View.GONE);
                bottomCt3.setVisibility(View.GONE);

                AlbumInfo albumInfo = itemAlbumDatas.get(clickAlbumId);
                ItemImageInfo imgInfo;
                int dataSize = albumInfo.size();

                if (position * 3 < dataSize) {  //if position * 3 < dataSize,get imgInfo , then setText ande Visibility
                    imgInfo = albumInfo.getImageInfoByIndex(position * 3);
                    t.addTask(imgInfo.filePath, imageView, imgInfo.imageId, imgInfo.orientation);
                    allCt.setVisibility(View.VISIBLE);
                    allCt.setTag(imgInfo.filePath);
                    allCt.setTag(R.string.view_tag_key2, imgInfo.orientation);
                    cb.setTag(position * 3);
                    if (imgInfo.isChecked) {
                        cb.setChecked(true);
                        blackBg.setVisibility(View.VISIBLE);
                        blackBg.setText(getSizeStr(imgInfo.size, imgInfo.filePath));
                    } else {
                        cb.setChecked(false);
                        blackBg.setVisibility(View.GONE);
                    }
                } else {
                    allCt.setVisibility(View.INVISIBLE);
                }

                if (position * 3 + 1 < dataSize) {  //if position * 3 + 1 < dataSize,get imgInfo , then setText ande Visibility
                    imgInfo = albumInfo.getImageInfoByIndex(position * 3 + 1);
                    t.addTask(imgInfo.filePath, imageView2, imgInfo.imageId, imgInfo.orientation);
                    allCt2.setVisibility(View.VISIBLE);
                    allCt2.setTag(imgInfo.filePath);
                    allCt2.setTag(R.string.view_tag_key2, imgInfo.orientation);
                    cb2.setTag(position * 3 + 1);
                    if (imgInfo.isChecked) {
                        cb2.setChecked(true);
                        blackBg2.setVisibility(View.VISIBLE);
                        blackBg2.setText(getSizeStr(imgInfo.size, imgInfo.filePath));
                    } else {
                        cb2.setChecked(false);
                        blackBg2.setVisibility(View.GONE);
                    }
                } else {
                    allCt2.setVisibility(View.INVISIBLE);
                }

                if (position * 3 + 2 < dataSize) {  //if position * 3 + 2 < dataSize,get imgInfo , then setText ande Visibility
                    imgInfo = albumInfo.getImageInfoByIndex(position * 3 + 2);
                    t.addTask(imgInfo.filePath, imageView3, imgInfo.imageId, imgInfo.orientation);
                    allCt3.setVisibility(View.VISIBLE);
                    allCt3.setTag(imgInfo.filePath);
                    allCt3.setTag(R.string.view_tag_key2, imgInfo.orientation);
                    cb3.setTag(position * 3 + 2);
                    if (imgInfo.isChecked) {
                        cb3.setChecked(true);
                        blackBg3.setVisibility(View.VISIBLE);
                        blackBg3.setText(getSizeStr(imgInfo.size, imgInfo.filePath));
                    } else {
                        cb3.setChecked(false);
                        blackBg3.setVisibility(View.GONE);
                    }
                } else {
                    allCt3.setVisibility(View.INVISIBLE);
                }

                cb.setOnCheckedChangeListener(onCheck);
                cb2.setOnCheckedChangeListener(onCheck);
                cb3.setOnCheckedChangeListener(onCheck);

            }

            return view;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public void notifyDataSetChanged() {
            t.clearTaskAndCache();
            if (isAlbumMode) {
                size = itemAlbumDatas.size();
            } else {
                size = itemAlbumDatas.get(clickAlbumId).size();
            }
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (size % 3 == 0) {
                return size / 3;
            } else {
                return size / 3 + 1;
            }
        }
    }

    /**
     * Notes code for browsing large jump, because at this stage needs no comment, the function
     */
    private View.OnClickListener onItemClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object o = v.getTag();
            if (o instanceof String) {
                clickItemView = v;
//                String path = (String) o;
//                Intent intent = new Intent(ImagePickerPlusActivity.this, LargePreviewActivity.class);
//                intent.putExtra("filePath", path);
//                String orientation = (String) v.getTag(R.string.view_tag_key2);
//                intent.putExtra("orientation", orientation);
//                intent.putExtra("isJustPreview", false);
//                startActivityForResult(intent, CODE_LARGE_PREVIEW);
            } else if (o instanceof Long) {
                AlbumInfo albumInfo;
                clickItemView = v;
                clickAlbumId = (Long) o;
                isAlbumMode = !isAlbumMode;
                albumInfo = itemAlbumDatas.get(clickAlbumId);
                toolbar.setTitle(albumInfo.albumName);
//                adapter.notifyDataSetChanged();
                if (adapter instanceof MyCursorAdapter) {
                    ((MyCursorAdapter) adapter).changeCursor(getPhotoInfoCursorByAlbumId(clickAlbumId));
                } else {
                    adapter.notifyDataSetChanged();
                }
                if (!isAlbumMode) {
                    lv.setSelection(0);
                }
            }
        }
    };

    /**
     * define CursorAdapter for Providing a bridge for Cursor and ListView connections
     */
    class MyCursorAdapter extends CursorAdapter {
        int size;
        LayoutInflater layoutInflater = getLayoutInflater();
        AlbumInfo albumInfo;

        public MyCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            size = getCursor().getCount();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return layoutInflater.inflate(R.layout.list_grid_item, parent, false);
        }

        /**
         * new View will bind with data using bindView and get the Info
         *
         * @param view
         * @param context
         * @param cursor
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView imageView = (ImageView) view.findViewById(R.id.image_item);
            ImageView imageView2 = (ImageView) view.findViewById(R.id.image_item_2);
            ImageView imageView3 = (ImageView) view.findViewById(R.id.image_item_3);

            setLayoutHeight(imageView);
            setLayoutHeight(imageView2);
            setLayoutHeight(imageView3);

            TextView tv = (TextView) view.findViewById(R.id.tv_info);
            TextView tv2 = (TextView) view.findViewById(R.id.tv_info2);
            TextView tv_2 = (TextView) view.findViewById(R.id.tv_info_2);
            TextView tv2_2 = (TextView) view.findViewById(R.id.tv_info2_2);
            TextView tv_3 = (TextView) view.findViewById(R.id.tv_info_3);
            TextView tv2_3 = (TextView) view.findViewById(R.id.tv_info2_3);

            View bottomCt = view.findViewById(R.id.bottom_ct);
            View bottomCt2 = view.findViewById(R.id.bottom_ct_2);
            View bottomCt3 = view.findViewById(R.id.bottom_ct_3);

            View allCt = view.findViewById(R.id.all_ct);
            View allCt2 = view.findViewById(R.id.all_ct2);
            View allCt3 = view.findViewById(R.id.all_ct3);

            allCt.setOnClickListener(onItemClick);
            allCt2.setOnClickListener(onItemClick);
            allCt3.setOnClickListener(onItemClick);

            CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox);
            CheckBox cb2 = (CheckBox) view.findViewById(R.id.checkbox_2);
            CheckBox cb3 = (CheckBox) view.findViewById(R.id.checkbox_3);

            TextView blackBg = (TextView) view.findViewById(R.id.tv_checked_bg);
            TextView blackBg2 = (TextView) view.findViewById(R.id.tv_checked_bg2);
            TextView blackBg3 = (TextView) view.findViewById(R.id.tv_checked_bg3);

            cb.setTag(R.string.view_tag_key, blackBg);
            cb2.setTag(R.string.view_tag_key, blackBg2);
            cb3.setTag(R.string.view_tag_key, blackBg3);

            allCt.setTag(R.string.view_tag_key, R.id.checkbox);
            allCt2.setTag(R.string.view_tag_key, R.id.checkbox_2);
            allCt3.setTag(R.string.view_tag_key, R.id.checkbox_3);

            if (isAlbumMode) {

                cb.setVisibility(View.GONE);
                cb2.setVisibility(View.GONE);
                cb3.setVisibility(View.GONE);

                blackBg.setVisibility(View.GONE);
                blackBg2.setVisibility(View.GONE);
                blackBg3.setVisibility(View.GONE);

                bottomCt.setVisibility(View.VISIBLE);
                bottomCt2.setVisibility(View.VISIBLE);
                bottomCt3.setVisibility(View.VISIBLE);

                AlbumInfo albumInfo;
                ItemImageInfo imgInfo;

                if (currentPosition * 3 < size) {
                    cursor.move(currentPosition * 2);
                    long albumId = cursor.getLong(3);
                    albumInfo = itemAlbumDatas.get(albumId);
                    if (albumInfo == null) {
                        albumInfo = new AlbumInfo();
                        albumInfo.albumId = cursor.getLong(3);
                        albumInfo.albumName = cursor.getString(4);
                        albumInfo.photoCount = cursor.getInt(7);
                        ItemImageInfo conver = new ItemImageInfo();
                        conver.filePath = cursor.getString(2);
                        conver.uri = Uri.parse(Media.EXTERNAL_CONTENT_URI + "/" + cursor.getInt(0));
                        conver.imageId = cursor.getLong(0);
                        conver.orientation = new String(String.valueOf(cursor.getInt(6)));
                        conver.size = cursor.getLong(1);
                        albumInfo.setConver(conver);
                        itemAlbumDatas.put(albumInfo.albumId, albumInfo);
                    }

                    imgInfo = albumInfo.getConver();
                    t.addTask(imgInfo.filePath, imageView, imgInfo.imageId, imgInfo.orientation);
                    bottomCt.setVisibility(View.VISIBLE);
                    tv.setText(albumInfo.albumName);
                    String photoStr = String.format(getString(R.string.brackets_double), albumInfo.photoCount);  //seize a seat
                    tv2.setText(photoStr);
                    if (albumInfo.choiceCount > 0) {
                        String choiceStr = String.format(getString(R.string.selected_pick), albumInfo.choiceCount);
                        blackBg.setText(choiceStr);
                        blackBg.setVisibility(View.VISIBLE);
                    }
                    allCt.setTag(albumInfo.albumId);
                    allCt.setVisibility(View.VISIBLE);
                } else {
                    allCt.setVisibility(View.INVISIBLE);
                }

                if (currentPosition * 3 + 1 < size) {
                    cursor.moveToNext();
                    long albumId = cursor.getLong(3);
                    albumInfo = itemAlbumDatas.get(albumId);
                    if (albumInfo == null) {
                        albumInfo = new AlbumInfo();
                        albumInfo.albumId = cursor.getLong(3);
                        albumInfo.albumName = cursor.getString(4);
                        albumInfo.photoCount = cursor.getInt(7);
                        ItemImageInfo conver = new ItemImageInfo();
                        conver.filePath = cursor.getString(2);
                        conver.uri = Uri.parse(Media.EXTERNAL_CONTENT_URI + "/" + cursor.getInt(0));
                        conver.imageId = cursor.getLong(0);
                        conver.orientation = new String(String.valueOf(cursor.getInt(6)));
                        conver.size = cursor.getLong(1);
                        albumInfo.setConver(conver);
                        itemAlbumDatas.put(albumInfo.albumId, albumInfo);
                    }

                    imgInfo = albumInfo.getConver();
                    t.addTask(imgInfo.filePath, imageView2, imgInfo.imageId, imgInfo.orientation);
                    bottomCt2.setVisibility(View.VISIBLE);
                    tv_2.setText(albumInfo.albumName);
                    String photoStr = String.format(getString(R.string.brackets_double), albumInfo.photoCount);
                    tv2_2.setText(photoStr);
                    if (albumInfo.choiceCount > 0) {
                        String choiceStr = String.format(getString(R.string.selected_pick), albumInfo.choiceCount);  //seize a seat
                        blackBg2.setText(choiceStr);
                        blackBg2.setVisibility(View.VISIBLE);
                    }
                    allCt2.setTag(albumInfo.albumId);
                    allCt2.setVisibility(View.VISIBLE);
                } else {
                    allCt2.setVisibility(View.INVISIBLE);
                }

                if (currentPosition * 3 + 2 < size) {
                    cursor.moveToNext();
                    long albumId = cursor.getLong(3);
                    albumInfo = itemAlbumDatas.get(albumId);
                    if (albumInfo == null) {
                        albumInfo = new AlbumInfo();
                        albumInfo.albumId = cursor.getLong(3);
                        albumInfo.albumName = cursor.getString(4);
                        albumInfo.photoCount = cursor.getInt(7);
                        ItemImageInfo conver = new ItemImageInfo();
                        conver.filePath = cursor.getString(2);
                        conver.uri = Uri.parse(Media.EXTERNAL_CONTENT_URI + "/" + cursor.getInt(0));
                        conver.imageId = cursor.getLong(0);
                        conver.orientation = new String(String.valueOf(cursor.getInt(6)));
                        conver.size = cursor.getLong(1);
                        albumInfo.setConver(conver);
                        itemAlbumDatas.put(albumInfo.albumId, albumInfo);
                    }

                    imgInfo = albumInfo.getConver();
                    t.addTask(imgInfo.filePath, imageView3, imgInfo.imageId, imgInfo.orientation);
                    bottomCt3.setVisibility(View.VISIBLE);
                    tv_3.setText(albumInfo.albumName);
                    String photoStr = String.format(getString(R.string.brackets_double), albumInfo.photoCount);  //seize a seat
                    tv2_3.setText(photoStr);
                    if (albumInfo.choiceCount > 0) {
                        String choiceStr = String.format(getString(R.string.selected_pick), albumInfo.choiceCount);
                        blackBg3.setText(choiceStr);
                        blackBg3.setVisibility(View.VISIBLE);
                    }
                    allCt3.setTag(albumInfo.albumId);
                    allCt3.setVisibility(View.VISIBLE);
                } else {
                    allCt3.setVisibility(View.INVISIBLE);
                }

            } else {


                if (albumInfo == null) {
                    albumInfo = itemAlbumDatas.get(clickAlbumId);
                }
                ItemImageInfo imgInfo;

                cb.setVisibility(View.VISIBLE);
                cb2.setVisibility(View.VISIBLE);
                cb3.setVisibility(View.VISIBLE);

                cb.setOnCheckedChangeListener(null);
                cb2.setOnCheckedChangeListener(null);
                cb3.setOnCheckedChangeListener(null);

                bottomCt.setVisibility(View.GONE);
                bottomCt2.setVisibility(View.GONE);
                bottomCt3.setVisibility(View.GONE);

                if (currentPosition * 3 < size) {
                    cursor.move(currentPosition * 2);
                    long imgId = cursor.getLong(0);
                    imgInfo = albumInfo.getImageInfoByKey(imgId);
                    if (null == imgInfo) {
                        imgInfo = new ItemImageInfo();
                        imgInfo.orientation = new String(String.valueOf(cursor.getInt(3)));
                        imgInfo.size = cursor.getLong(1);
                        imgInfo.imageId = imgId;
                        imgInfo.filePath = cursor.getString(2);
                        imgInfo.uri = Uri.parse(Media.EXTERNAL_CONTENT_URI + "/" + cursor.getInt(0));
                        albumInfo.addImageInfo(imgInfo);
                    }

                    t.addTask(imgInfo.filePath, imageView, imgInfo.imageId, imgInfo.orientation);
                    allCt.setVisibility(View.VISIBLE);
                    allCt.setTag(imgInfo.filePath);
                    allCt.setTag(R.string.view_tag_key2, imgInfo.orientation);
                    cb.setTag(currentPosition * 3);
                    if (imgInfo.isChecked) {
                        cb.setChecked(true);
                        blackBg.setVisibility(View.VISIBLE);
                        blackBg.setText(getSizeStr(imgInfo.size, imgInfo.filePath));
                    } else {
                        cb.setChecked(false);
                        blackBg.setVisibility(View.GONE);
                    }
                } else {
                    allCt.setVisibility(View.INVISIBLE);
                }

                if (currentPosition * 3 + 1 < size) {
                    cursor.moveToNext();
                    long imgId = cursor.getLong(0);
                    imgInfo = albumInfo.getImageInfoByKey(imgId);
                    if (null == imgInfo) {
                        imgInfo = new ItemImageInfo();
                        imgInfo.orientation = new String(String.valueOf(cursor.getInt(3)));
                        imgInfo.size = cursor.getLong(1);
                        imgInfo.imageId = imgId;
                        imgInfo.filePath = cursor.getString(2);
                        imgInfo.uri = Uri.parse(Media.EXTERNAL_CONTENT_URI + "/" + cursor.getInt(0));
                        albumInfo.addImageInfo(imgInfo);
                    }

                    t.addTask(imgInfo.filePath, imageView2, imgInfo.imageId, imgInfo.orientation);
                    allCt2.setVisibility(View.VISIBLE);
                    allCt2.setTag(imgInfo.filePath);
                    allCt2.setTag(R.string.view_tag_key2, imgInfo.orientation);
                    cb2.setTag(currentPosition * 3 + 1);
                    if (imgInfo.isChecked) {
                        cb2.setChecked(true);
                        blackBg2.setVisibility(View.VISIBLE);
                        blackBg2.setText(getSizeStr(imgInfo.size, imgInfo.filePath));
                    } else {
                        cb2.setChecked(false);
                        blackBg2.setVisibility(View.GONE);
                    }
                } else {
                    allCt2.setVisibility(View.INVISIBLE);
                }

                if (currentPosition * 3 + 2 < size) {
                    cursor.moveToNext();
                    long imgId = cursor.getLong(0);
                    imgInfo = albumInfo.getImageInfoByKey(imgId);
                    if (null == imgInfo) {
                        imgInfo = new ItemImageInfo();
                        imgInfo.orientation = new String(String.valueOf(cursor.getInt(3)));
                        imgInfo.size = cursor.getLong(1);
                        imgInfo.imageId = imgId;
                        imgInfo.filePath = cursor.getString(2);
                        imgInfo.uri = Uri.parse(Media.EXTERNAL_CONTENT_URI + "/" + cursor.getInt(0));
                        albumInfo.addImageInfo(imgInfo);
                    }

                    t.addTask(imgInfo.filePath, imageView3, imgInfo.imageId, imgInfo.orientation);
                    allCt3.setVisibility(View.VISIBLE);
                    allCt3.setTag(imgInfo.filePath);
                    allCt3.setTag(R.string.view_tag_key2, imgInfo.orientation);
                    cb3.setTag(currentPosition * 3 + 2);
                    if (imgInfo.isChecked) {
                        cb3.setChecked(true);
                        blackBg3.setVisibility(View.VISIBLE);
                        blackBg3.setText(getSizeStr(imgInfo.size, imgInfo.filePath));
                    } else {
                        cb3.setChecked(false);
                        blackBg3.setVisibility(View.GONE);
                    }
                } else {
                    allCt3.setVisibility(View.INVISIBLE);
                }

                cb.setOnCheckedChangeListener(onCheck);
                cb2.setOnCheckedChangeListener(onCheck);
                cb3.setOnCheckedChangeListener(onCheck);

            }
        }

        @Override
        public void notifyDataSetChanged() {
            albumInfo = null;
            size = getCursor().getCount();
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (size % 3 == 0) {
                return size / 3;
            } else {
                return size / 3 + 1;
            }
        }

        int currentPosition;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            currentPosition = position;
            return super.getView(position, convertView, parent);
        }

        @Override
        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
        }

    }

    public static String getSizeStr(long size) {
        if (size > 0) {
            String imgSize;
            double temp = Double.valueOf(size);
            temp = temp / 1024.0d; //KB
            if (temp > 1024.0d) { // > 1MB
                temp = temp / 1024.0d; //MB
                String sizeStr = String.valueOf(temp);
                imgSize = sizeStr.substring(0, sizeStr.indexOf(".") + 2) + " MB";
            } else {
                long temp2 = Math.round(temp);
                imgSize = (temp2 > 0 ? temp2 : 1) + " KB";
            }
            return imgSize;
        } else {
            return "";
        }
    }

    public static String getSizeStr(long size, String filePath) {
        String value = getSizeStr(size);
        if (TextUtils.isEmpty(value) && !TextUtils.isEmpty(filePath)) {
            value = getSizeStr(new File(filePath).length());
        }
        return value;
    }

    /**
     * set the OncheckedChangeListener
     */
    private CompoundButton.OnCheckedChangeListener onCheck = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (choicePhotoPaths.size() >= CAN_CHECK_COUNT && isChecked) {
                buttonView.setOnCheckedChangeListener(null);
                buttonView.setChecked(false);
                buttonView.setOnCheckedChangeListener(onCheck);
                String mostStr = String.format(getString(R.string.most_selected), CAN_CHECK_COUNT);
                Toast.makeText(getApplicationContext(), mostStr, Toast.LENGTH_SHORT).show();
                return;
            }
            Integer position = (Integer) buttonView.getTag();
            if (null != position) {
                AlbumInfo albumInfo = itemAlbumDatas.get(clickAlbumId);
                ItemImageInfo imgItemInfo = albumInfo.getImageInfoByIndex(position);
                TextView tvBg = (TextView) buttonView.getTag(R.string.view_tag_key);
                if (isChecked) {
                    tvBg.setVisibility(View.VISIBLE);
                    tvBg.setText(getSizeStr(imgItemInfo.size, imgItemInfo.filePath));
                    choicePhotoPaths.add(imgItemInfo.filePath);
                    datas.add(imgItemInfo);
                    picUriList.add(imgItemInfo.uri);
                    albumInfo.choiceCount++;
                } else {
                    tvBg.setVisibility(View.GONE);
                    choicePhotoPaths.remove(imgItemInfo.filePath);
                    datas.remove(imgItemInfo);
                    picUriList.remove(imgItemInfo.uri);
                    albumInfo.choiceCount--;
                }
                String finishStr = String.format(getString(R.string.finish_percent), choicePhotoPaths.size());  //seize a seat
                menuItem.setTitle(finishStr);
                long choicePhotoSize;
                if ((choicePhotoSize = choicePhotoPaths.size()) > 0) {
                    menuItem.setEnabled(true);
                } else {
                    menuItem.setEnabled(false);
                }
                imgItemInfo.isChecked = isChecked;

                long allFileSize = 0;
                for (int i = 0; i < choicePhotoSize; i++) {
                    allFileSize = allFileSize + new File(choicePhotoPaths.get(i)).length();
                }
                String title = getSizeStr(allFileSize);
                if (!TextUtils.isEmpty(title)) {
                    String picDataStr = String.format(getString(R.string.select_pic_data), title);  //seize a seat
//                    toolbar.setTitle(picDataStr);
                } else {
                    String picStr = getString(R.string.select_pic);
//                    toolbar.setTitle(picStr);
                }
            }
        }
    };

    /**
     * the thread which used in onCreate
     */
    class LoadPhonePhotoThread extends Thread {

        private ConcurrentLinkedQueue<ImageView> imgViews = new ConcurrentLinkedQueue<>();
        private LongSparseArray<String> thumbnailsMap = new LongSparseArray<>();
        private Options options = new Options();
        private android.graphics.Matrix matrix = new android.graphics.Matrix();

        public LoadPhonePhotoThread() {
            super();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }

        public void clearTaskAndCache() {
            imgViews.clear();
        }

        public void addTask(String filePath, ImageView imgView, Long imgId, String orientation) {
            if (imgView != null) {
                synchronized (imgView) {
                    if (null != filePath && null != imgId && null != orientation) {
                        imgView.setTag(imgId);
                        imgView.setTag(R.string.view_tag_key, filePath);
                        imgView.setTag(R.string.view_tag_key2, orientation);
                    }
                }
                if (imgViews.contains(imgView)) {
                    imgViews.remove(imgView);
                }
                imgViews.add(imgView);
            }
        }

        @Override
        public void run() {
            String[] projection = {Thumbnails._ID, Thumbnails.IMAGE_ID, Thumbnails.DATA};
            Cursor cursor = getContentResolver().query(Thumbnails.EXTERNAL_CONTENT_URI, projection,
                    Thumbnails.KIND + "=?", new String[]{String.valueOf(Thumbnails.MINI_KIND)}, null);
            while (cursor.moveToNext()) {
                thumbnailsMap.put(cursor.getLong(1), cursor.getString(2));
            }
            cursor.close();
            while (flag) {
                if (!imgViews.isEmpty()) {
                    ImageView imgView = imgViews.poll();
                    if (imgView == null) {
                        continue;
                    }
                    Long imgId;
                    String tagFilePath;
                    String orientation;
                    synchronized (imgView) {
                        imgId = (Long) imgView.getTag();
                        tagFilePath = (String) imgView.getTag(R.string.view_tag_key);
                        orientation = (String) imgView.getTag(R.string.view_tag_key2);
                    }
                    Bitmap b = null;
                    if (b == null) { //get mini from system diskcache
                        b = getSystemMiniFromSystemDiskCache(imgId);
                    }
                    if (b == null) { //my mini from my diskcache
                        b = getMyMiniFromMyDiskCache(tagFilePath);
                    }
                    //my mini from system ori and save to my diskcache
                    if (b == null) {
                        b = getMyMiniFromSystemOri(tagFilePath);
                    }
                    //gen system mini by system and save to system diskcache、db.
                    if (b == null) {
                        b = getSystemMiniFromSystem(imgId);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putLong("imgId", imgId);
                    if (b != null) {
                        try {
                            int o = Integer.parseInt(orientation);
                            if (o > 0 && o < 360) {
                                matrix.reset();
                                matrix.setRotate(o);
                                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
                            }
                            bundle.putParcelable("bitmap", b);
                        } catch (Exception e) {
                        }
                    } else {
                    }
                    Message msg = mHandler.obtainMessage(0, imgView);
                    msg.setData(bundle);
                    msg.sendToTarget();
                }
            }
            clearTaskAndCache();
        }

        /**
         * get Bitmap form system disk cache
         *
         * @param imgId
         * @return
         */
        private Bitmap getSystemMiniFromSystemDiskCache(long imgId) {
            String thumbnailFilePath = thumbnailsMap.get(imgId);
            if (!TextUtils.isEmpty(thumbnailFilePath)) {
                File miniFile = new File(thumbnailFilePath);
                if (miniFile.exists() && miniFile.length() > 0) {
                    options.inSampleSize = 1;
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(thumbnailFilePath, options);
                    if (options.outWidth > imgViewWidthAndHeight || options.outHeight > imgViewWidthAndHeight) {
                        final float maxBitmapBorder = options.outWidth > options.outHeight ? options.outWidth : options.outHeight;
                        options.inSampleSize = Math.round(maxBitmapBorder / ((float) imgViewWidthAndHeight));
                    }
                    options.inJustDecodeBounds = false;
                    return BitmapUtil.getBitmap(thumbnailFilePath, options);
                } else {
                }
            } else {
            }
            return null;
        }

        private Bitmap getMyMiniFromMyDiskCache(String oriFilePath) {
            if (!TextUtils.isEmpty(diskCachePath) && !TextUtils.isEmpty(oriFilePath)) {
                Bitmap b = BitmapUtil.getBitmap(new File(diskCachePath, new File(oriFilePath).getName().split("\\.")[0]).getAbsolutePath());
                return b;
            }
            return null;
        }

        private Bitmap getSystemMiniFromSystem(long imgId) {
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            Thumbnails.getThumbnail(getContentResolver(), imgId,
                    Thumbnails.MINI_KIND, options);
            if (options.outWidth > imgViewWidthAndHeight || options.outHeight > imgViewWidthAndHeight) {
                final float maxBitmapBorder = options.outWidth > options.outHeight ? options.outWidth : options.outHeight;
                options.inSampleSize = Math.round(maxBitmapBorder / ((float) imgViewWidthAndHeight));
            }
            options.inJustDecodeBounds = false;
            Bitmap bm = Thumbnails.getThumbnail(getContentResolver(), imgId, Thumbnails.MINI_KIND, options);
            if (bm != null && TextUtils.isEmpty(thumbnailsMap.get(imgId))) {
                Cursor c = Thumbnails.queryMiniThumbnail(getContentResolver(), imgId, Thumbnails.MINI_KIND, new String[]{Thumbnails._ID, Thumbnails.DATA});
                if (null != c) {
                    if (c.moveToFirst()) {
                        thumbnailsMap.put(imgId, c.getString(1));
                    } else {
                    }
                    c.close();
                } else {
                }
            }
            return bm;
        }

        private Bitmap getMyMiniFromSystemOri(String filePath) {
            if (!TextUtils.isEmpty(filePath)) {
                options.inJustDecodeBounds = true;
                options.inSampleSize = 1;
                BitmapFactory.decodeFile(filePath, options);
                if (options.outWidth * options.outHeight >= 1600 * 1200) {
                    if (TextUtils.isEmpty(diskCachePath)) {
                        return null;
                    }
                }
                if (options.outWidth > imgViewWidthAndHeight || options.outHeight > imgViewWidthAndHeight) {
                    float maxBitmapBorder = options.outWidth > options.outHeight ? options.outWidth : options.outHeight;
                    options.inSampleSize = Math.round(maxBitmapBorder / ((float) imgViewWidthAndHeight));
                }
                options.inJustDecodeBounds = false;
                Bitmap bm = BitmapUtil.getBitmap(filePath, options);
                if (null != bm) {
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(new File(diskCachePath, new File(filePath).getName().split("\\.")[0]));
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    } catch (Exception e) {
                    } finally {
                        if (null != fos) {
                            try {
                                fos.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
                if (bm == null) {
                }
                return bm;
            } else {
                return null;
            }
        }

    }

    /**
     * get photo info cursor by albumId
     *
     * @param albumId
     * @return
     */
    private Cursor getPhotoInfoCursorByAlbumId(long albumId) {
        String[] projection = {Media._ID, Media.SIZE, Media.DATA, Media.ORIENTATION};
        Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI,
                projection,
                Media.BUCKET_ID + "=?",
                new String[]{String.valueOf(albumId)},
                Media.DATE_MODIFIED + " DESC, " + Media.DATE_ADDED + " DESC, " + Media._ID + " DESC");
        return cursor;
    }

    private Cursor getAlbumnAdapterDatas() throws Exception {
        String[] projection = {Media._ID, Media.SIZE, Media.DATA, Media.BUCKET_ID, Media.BUCKET_DISPLAY_NAME,
                Media.DISPLAY_NAME, Media.ORIENTATION, "COUNT(0) AS count"};
        Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI,
                projection,
                "0==0) GROUP BY (" + Media.BUCKET_ID,
                null,
                Media.DATE_MODIFIED + " DESC, " + Media.DATE_ADDED + " DESC, " + Media._ID + " DESC");
        return cursor;
    }

    /**
     * get itemAlbumDatas
     *
     * @return
     */
    private LongSparseArray<AlbumInfo> getAllAdapterDatas() {
        LongSparseArray<AlbumInfo> itemAlbumDatas = new LongSparseArray<AlbumInfo>();
        String[] projection = {Media._ID, Media.SIZE, Media.DATA, Media.BUCKET_ID, Media.BUCKET_DISPLAY_NAME,
                Media.DISPLAY_NAME, Media.ORIENTATION};
        Cursor cursor2 = getContentResolver().query(Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                Media.DATE_MODIFIED + " DESC, " + Media.DATE_ADDED + " DESC, " + Media._ID + " DESC");
        String filePath;
        while (cursor2.moveToNext()) {
            filePath = cursor2.getString(2);
            if (!new File(filePath).exists()) {
                continue;
            }
            long Media_BUCKET_ID = cursor2.getLong(3);
            AlbumInfo albumInfo = itemAlbumDatas.get(Media_BUCKET_ID);
            if (albumInfo == null) {
                albumInfo = new AlbumInfo();
                albumInfo.albumId = Media_BUCKET_ID;
                albumInfo.albumName = cursor2.getString(4);
                itemAlbumDatas.put(Media_BUCKET_ID, albumInfo);
            }
            ItemImageInfo imageInfo = new ItemImageInfo();
            imageInfo.imageId = cursor2.getLong(0);
            imageInfo.size = cursor2.getLong(1);
            imageInfo.filePath = filePath;
            imageInfo.uri = Uri.parse(Media.EXTERNAL_CONTENT_URI + "/" + cursor2.getInt(0));
            imageInfo.orientation = String.valueOf(cursor2.getInt(6));
            albumInfo.photoCount++;
            albumInfo.addImageInfo(imageInfo);
        }
        cursor2.close();
        return itemAlbumDatas;
    }

}