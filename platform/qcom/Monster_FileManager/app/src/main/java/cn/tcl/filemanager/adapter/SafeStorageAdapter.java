/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.adapter;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.utils.SafeInfo;
import cn.tcl.filemanager.utils.SharedPreferenceUtils;

/**
 * Created by user on 16-2-29.
 */
public class SafeStorageAdapter extends BaseAdapter{
    private List<SafeInfo> safeInfo;
    private int mCategory;
    private LayoutInflater mlayout;
    private Context context;
    private FileManagerApplication mApplication;
    private MountManager mMountPointManager;

    public static final int CATEGORY_SAFE_BOX_LIST = 1;
    public static final int CATEGORY_STORAGE_LIST = 2;

    public SafeStorageAdapter(Context mContext, FileManagerApplication mApplication, MountManager mMountPointManager) {
        this.safeInfo = new ArrayList<>();
        this.context = mContext;
        this.mApplication = mApplication;
        this.mMountPointManager = mMountPointManager;
        mlayout = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setCategory(int category) {
        this.mCategory = category;
    }

    public void addAll(List<SafeInfo> datas) {
        this.safeInfo.clear();
        this.safeInfo = datas;
    }

    @Override
    public String getItem(int i) {
        return safeInfo.get(i).getSafe_path();
    }

    public void clear() {
         safeInfo.clear();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        String currentSafeName = SharedPreferenceUtils.getCurrentSafeName(context);
        if (view == null) {
            view = mlayout.inflate(R.layout.safe_storage_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) view
                    .findViewById(R.id.safe_storage_name);
            holder.info = (TextView) view
                    .findViewById(R.id.safe_storage_info);
            holder.image = (ImageView) view
                    .findViewById(R.id.storage_item_img);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (mCategory == CATEGORY_STORAGE_LIST) {
            setStorageInfo(safeInfo.get(i).getStorage_name(), holder);
            holder.name.setText(safeInfo.get(i).getStorage_name());
        } else {
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String date = df.format(safeInfo.get(i).getSafe_ct());
            String safeName = safeInfo.get(i).getSafe_name();
            if (safeName == null || safeInfo.get(i).getSafe_name().equals("null")) {
                safeName = Build.MODEL;
            }
            holder.image.setBackgroundResource(R.drawable.category_safe);
            holder.info.setText(date + "   " + context.getResources().getString(R.string.created) + "    " + safeInfo.get(i).getSafe_info());
            Log.d("PATH", "this is path" + safeInfo.get(i).getSafe_path());
            Log.d("PATH", "this is Name" + safeInfo.get(i).getSafe_name());

            String safePath = safeInfo.get(i).getSafe_path();
            if (TextUtils.isEmpty(currentSafeName) && i == 0) {
                holder.name.setText(safeName + "   (" + context.getResources().getString(R.string.in_use) + ")");
                SharedPreferenceUtils.setCurrentSafeName(context, new File(safeInfo.get(i).getSafe_path()).getName());
                SharedPreferenceUtils.setCurrentSafeRoot(context, new File(safeInfo.get(i).getSafe_path()).getParent());
            } else if (!TextUtils.isEmpty(currentSafeName)&&
                    !TextUtils.isEmpty(safePath) &&
                    safePath.contains(currentSafeName)) {
                Log.d("MODEL", "this is Name--111--" + safeInfo.get(i).getSafe_name());
                holder.name.setText(safeName + "   (" + context.getResources().getString(R.string.in_use) + ")");
                SharedPreferenceUtils.setCurrentSafeName(context, new File(safeInfo.get(i).getSafe_path()).getName());
                SharedPreferenceUtils.setCurrentSafeRoot(context, new File(safeInfo.get(i).getSafe_path()).getParent());
            }else {
                holder.name.setText(safeName);
            }
        }
        return view;
    }

    @Override
    public int getCount() {
        return safeInfo.size();
    }

    static class ViewHolder {
        ImageView image;
        TextView name;
        TextView info;

    }

    public void setStorageInfo(String name, ViewHolder holder) {
        if (name.equals(context.getResources().getString(R.string.phone_storage))) {
            holder.image.setBackgroundResource(R.drawable.phone_storage);
            mApplication.mService.startCountStorageSizeTask(holder.info, 0,
                    mMountPointManager.getPhonePath(), context, true);
        } else if (name.equals(context.getResources().getString(R.string.sd_card))) {
            holder.image.setBackgroundResource(R.drawable.sd_storage);
            mApplication.mService.startCountStorageSizeTask(holder.info, 0,
                    mMountPointManager.getSDCardPath(), context, true);
            } else if (name.equals(context.getResources().getString(R.string.usbotg_m))) { // MODIFIED by songlin.qi, 2016-06-08,BUG-2278011
            holder.image.setBackgroundResource(R.drawable.otg_storage);
            mApplication.mService.startCountStorageSizeTask(holder.info, 0,
                    mMountPointManager.getUsbOtgPath(), context, true);
        }

    }
}
