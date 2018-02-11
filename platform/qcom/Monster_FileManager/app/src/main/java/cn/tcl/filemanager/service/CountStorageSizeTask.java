/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StatFs;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.utils.CommonUtils;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.view.PathProgressLayout;
import cn.tcl.filemanager.view.PathProgressThirdLayout;
import cn.tcl.filemanager.view.PathProgressTwoFirstLayout;
import cn.tcl.filemanager.view.PathProgressTwoSecondLayout;

public class CountStorageSizeTask extends AsyncTask<FileInfo, Integer, String> {
    private static final String TAG = CountStorageSizeTask.class.getSimpleName();
    //    private TextView mTextView;
    private Context mContext;
    private String mFilePath;
    private int percent;
    private PathProgressLayout mPathProgressLayout;
    private PathProgressTwoFirstLayout mPathProgressTwoFirstLayout;
    private PathProgressTwoSecondLayout mPathProgressTwoSecondLayout;
    private PathProgressThirdLayout mPathProgressThirdLayout;
    private TextView mTextView;
    private boolean isSafe;
    private String mFreeSpace;
    private String mFirstStorage;
    private String mSecondStorage;


    public CountStorageSizeTask(PathProgressLayout pathProgressLayout, Context context, String filePath) {
        mContext = context;
        mFilePath = filePath;
        this.mPathProgressLayout = pathProgressLayout;
        LogUtils.i(TAG, "excute CountStorageSizeTask mFilePath->" + mFilePath);
    }

    public CountStorageSizeTask(PathProgressTwoFirstLayout pathProgressLayout, Context context, String filePath) {
        mContext = context;
        mFilePath = filePath;
        this.mPathProgressTwoFirstLayout = pathProgressLayout;
        LogUtils.i(TAG, "excute CountStorageSizeTask mFilePath->" + mFilePath);
    }

    public CountStorageSizeTask(PathProgressTwoSecondLayout pathProgressLayout, Context context, String filePath) {
        mContext = context;
        mFilePath = filePath;
        this.mPathProgressTwoSecondLayout = pathProgressLayout;
        LogUtils.i(TAG, "excute CountStorageSizeTask mFilePath->" + mFilePath);
    }

    public CountStorageSizeTask(PathProgressThirdLayout pathProgressLayout, Context context, String filePath) {
        mContext = context;
        mFilePath = filePath;
        this.mPathProgressThirdLayout = pathProgressLayout;
        LogUtils.i(TAG, "excute CountStorageSizeTask mFilePath->" + mFilePath);
    }

    public CountStorageSizeTask(TextView mTextView, int width, String filePath, Context context,boolean isSafe) {
        mContext = context;
        mFilePath = filePath;
        this.mTextView = mTextView;
        this.isSafe = isSafe;
        LogUtils.i(TAG, "excute CountStorageSizeTask mFilePath->" + mFilePath);
    }

    //    private TextView mPerCenTextView;
   //    private int mWidth;

    @Override
    protected String doInBackground(FileInfo... inFileInfo) {
        double real_percent;
        String sizeString = null;
        String totalSpaceString = null;
        String usedSpaceString = null;
        StringBuilder sb = new StringBuilder();
        StringBuilder sdb = new StringBuilder();
        long blocSize = 0;
        long availaBlock = 0;
        long freeSpace = 0;
        long totalSpace = 0;
        long blockCount = 0;

        String sdSizeString = null;
        String sdTotalSpaceString = null;
        String sdUsedSpaceString = null;
        String sdFreeSpaceStr = null;
        long sdBlocSize = 0;
        long sdAvailaBlock = 0;
        long sdFreeSpace = 0;
        long sdTotalSpace = 0;
        long sdBlockCount = 0;
        long sdUsedSpace = 0;

        MountManager mMountManager = MountManager.getInstance();


        // ADD START FOR PR496344 BY HONGBIN.CHEN 20150807
        try {
            while (totalSpace <= 0 && MountManager.getInstance().isMountPoint(mFilePath)) {
                StatFs statfs = new StatFs(mFilePath);
                try {
                    blocSize = statfs.getBlockSizeLong();
                    if (CommonUtils.hasM()) {
                        availaBlock = new File(mFilePath).getFreeSpace();
                    } else {
                        availaBlock = statfs.getAvailableBlocksLong();
                    }
                    blockCount = statfs.getBlockCountLong();
                } catch (NoSuchMethodError e) {
                    blocSize = statfs.getBlockSizeLong();
                    if (CommonUtils.hasM()) {
                        availaBlock = new File(mFilePath).getFreeSpace();
                    } else {
                        availaBlock = statfs.getAvailableBlocksLong();
                    }
                    blockCount = statfs.getBlockCountLong();
                }
                if (!CommonUtils.hasM()) {
                    freeSpace = availaBlock * blocSize;
                }
                totalSpace = blocSize * blockCount;
            }
        } catch (Exception e) {
            freeSpace = 0;
            totalSpace = 0;
            e.printStackTrace();
        }

        if (mMountManager.isSDCardMounted()) {
            try {
                String sdFilePath = mMountManager.getSDCardPath();
                StatFs statfs = new StatFs(sdFilePath);
                try {
                    sdBlocSize = statfs.getBlockSizeLong();
                    if (CommonUtils.hasM()) {
                        sdAvailaBlock = new File(sdFilePath).getFreeSpace();
                    } else {
                        sdAvailaBlock = statfs.getAvailableBlocksLong();
                    }
                    sdBlockCount = statfs.getBlockCountLong();
                } catch (NoSuchMethodError e) {
                    sdBlocSize = statfs.getBlockSizeLong();
                    if (CommonUtils.hasM()) {
                        sdAvailaBlock = new File(sdFilePath).getFreeSpace();
                    } else {
                        sdAvailaBlock = statfs.getAvailableBlocksLong();
                    }
                    sdBlockCount = statfs.getBlockCountLong();
                }
                if (!CommonUtils.hasM()) {
                    sdFreeSpace = sdAvailaBlock * sdBlocSize;
                }
                sdTotalSpace = sdBlocSize * sdBlockCount;
                if (CommonUtils.hasM()) {
                    sdUsedSpace = sdTotalSpace - sdAvailaBlock;
                } else {
                    sdUsedSpace = sdTotalSpace - sdFreeSpace;
                }
            } catch (Exception e) {
                sdFreeSpace = 0;
                sdTotalSpace = 0;
                e.printStackTrace();
            }
        }
        // ADD END FOR PR496344 BY HONGBIN.CHEN 20150807

        totalSpaceString = FileUtils.sizeToGBString(mContext, totalSpace, true);
        if (CommonUtils.hasM()) {
            if(isSafe) {
                usedSpaceString = FileUtils.sizeToGBString(mContext, availaBlock, true);
            } else {
                usedSpaceString = FileUtils.sizeToString(mContext, totalSpace - availaBlock);
            }
            real_percent = (double) (totalSpace - availaBlock) / (double) totalSpace;
        } else {
            if(isSafe) {
                usedSpaceString = FileUtils.sizeToGBString(mContext, freeSpace, true);
            } else {
                usedSpaceString = FileUtils.sizeToString(mContext,totalSpace - freeSpace);
            }
            real_percent = (double) (totalSpace - freeSpace) / (double) totalSpace;
        }

        NumberFormat fmt = NumberFormat.getPercentInstance();
        fmt.setMaximumFractionDigits(2);
        percent = (int) (real_percent * 100);
        //sb.append(usedSpaceString).append("/");
        if (isSafe) {
//            sb.append(mContext.getString(R.string.freeof_m) + " " +usedSpaceString).append("/ ");
            sb.append(mContext.getString(R.string.freeof_m) + " " + usedSpaceString);
        } else {
//            sb.append(usedSpaceString).append("/ ");
            sb.append(usedSpaceString);
        }
//        sb.append(totalSpaceString);
        sizeString = sb.toString();

        sdTotalSpaceString = FileUtils.sizeToGBString(mContext, sdTotalSpace, true);  //sd

    if (CommonUtils.hasM()) {

        if (isSafe) {
            sdUsedSpaceString = FileUtils.sizeToGBString(mContext, sdAvailaBlock, true);
        } else {
            sdUsedSpaceString = FileUtils.sizeToString(mContext, sdTotalSpace - sdAvailaBlock);
        }
        mFreeSpace = usedSpaceString;

    } else {
        if (isSafe) {
            usedSpaceString = FileUtils.sizeToGBString(mContext, freeSpace, true);
        } else {
            usedSpaceString = FileUtils.sizeToString(mContext, totalSpace - freeSpace);
        }

        DecimalFormat df2 = new DecimalFormat("##.00");
        sdFreeSpaceStr = sdUsedSpaceString;

        NumberFormat sfmt = NumberFormat.getPercentInstance();
        sfmt.setMaximumFractionDigits(2);
        if (isSafe) {
//            sdb.append(mContext.getString(R.string.freeof_m) + " " + usedSpaceString).append("/ ");
            sdb.append(mContext.getString(R.string.freeof_m) + " " + usedSpaceString);
        } else {
//            sdb.append(sdUsedSpaceString).append("/ ");
            sdb.append(sdUsedSpaceString);
        }

//        sdb.append(sdTotalSpaceString);
        sdSizeString = sdb.toString();
        mSecondStorage = sdSizeString;
    }

    return sizeString;
}

    private String removeChart(String size) {
        if (size.contains("B")) {
            size = size.replace("B", "");
        }
        if (size.contains("K")) {
            size = size.replace("K", "");
        }
        if (size.contains("M")) {
            size = size.replace("M", "");
        }
        if (size.contains("G")) {
            size = size.replace("G", "");
        }
        if (size.contains("T")) {
            size = size.replace("T", "");
        }
        return size;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        String start[] = result.split("/");
        SpannableString sp = new SpannableString(result);
        sp.setSpan(new TypefaceSpan("sans-serif"), 0, start[0].length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        if(mPathProgressLayout != null){
                mPathProgressLayout.setProgressNew(percent);
        } else {
                if (mTextView != null) {
                    mTextView.setTypeface(CommonUtils.getRobotoMedium());
                    mTextView.setText(sp);
                }
            }
        }
} // MODIFIED by haifeng.tang, 2016-05-06,BUG-2104433
