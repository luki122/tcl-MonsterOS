/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.tcl.filemanager.MountReceiver;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.StorageQueryUtils;
import cn.tcl.filemanager.view.PieChartView;
import mst.widget.toolbar.Toolbar;

public class PieChartActivity extends FileBaseActionbarActivity implements MountReceiver.MountListener {
    private static final String TAG = PieChartActivity.class.getSimpleName();
    private PieChartView mChart;
    private ViewPager mViewPager;
    private MountManager mMountManager;
    private MountReceiver mMountReceiver;
    //TextView of phone storage details
    private TextView mTextTitle, mTextTotal, mTextAvailable, mTextSystem, mTextPicture, mTextMusic, mTextVideo, mTextApk, mTextOther, mAvailableButton;
    private TextView mTextBtn1, mTextBtn2, mTextBtn3;
    //TextView of SD card storage details
    private TextView mTextTitleSd, mTextTotalSd, mTextAvailableSd, mTextSystemSd, mTextPictureSd, mTextMusicSd, mTextVideoSd, mTextApkSd, mTextOtherSd, mAvailableButtonSd;
    private TextView mTextBtnSd1, mTextBtnSd2, mTextBtnSd3;
    //TextView of usb OTG storage details
    private TextView mTextTitleOtg, mTextTotalOtg, mTextAvailableOtg, mTextSystemOtg, mTextPictureOtg, mTextMusicOtg, mTextVideoOtg, mTextApkOtg, mTextOtherOtg, mAvailableButtonOtg;
    private TextView mTextBtnOtg1, mTextBtnOtg2, mTextBtnOtg3;

    private long systemSize;
    private long sdOrOtgSystemSize = 0;
    //The size of phone storage details
    private long pictureSizePhone, musicSizePhone, videoSizePhone, apkSizePhone, otherSizePhone, availableSizePhone, totalSizePhone;
    //The size of SD card storage details
    private long pictureSizeSd, musicSizeSd, videoSizeSd, apkSizeSd, otherSizeSd, availableSizeSd, totalSizeSd;
    //The size of usb OTG storage details
    private long pictureSizeOtg, musicSizeOtg, videoSizeOtg, apkSizeOtg, otherSizeOtg, availableSizeOtg, totalSizeOtg;

    private static final String PHONE = "phone";
    private static final String SD = "sdCard";
    private static final String OTG = "usbOtg";
    private static final int PHONE_VALUE = 0x101;
    private static final int SD_VALUE = 0x102;
    private static final int OTG_VALUE = 0x103;

    private View viewPhone, viewSdCard, viewOtg;

    StorageQueryUtils storageQueryUtils = new StorageQueryUtils(PieChartActivity.this);
    //Get the data from the new open query thread
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PHONE_VALUE:
                    Bundle bundle = (Bundle) msg.obj;
                    long[] arrPhone = bundle.getLongArray(PHONE);
                    initValues(arrPhone, PHONE);
                    init(PHONE);
                    initInternalChart();
                    break;
                case SD_VALUE:
                    Bundle bundleSd = (Bundle) msg.obj;
                    long[] arrSd = bundleSd.getLongArray(SD);
                    initValues(arrSd, SD);
                    init(SD);
                    initSdChart();
                    break;
                case OTG_VALUE:
                    Bundle bundleOtg = (Bundle) msg.obj;
                    long[] arrOtg = bundleOtg.getLongArray(OTG);
                    initValues(arrOtg, OTG);
                    init(OTG);
                    initOtgChart();
                default:
                    break;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LogUtils.timerMark(TAG+" start");
        super.onCreate(savedInstanceState);
        LogUtils.timerMark(TAG+" end");
        setContentView(R.layout.storage_piechart_pager);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        mToolbar.setTitle(R.string.storage_details);
        mToolbar.setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mMountManager = MountManager.getInstance();
        mMountReceiver = MountReceiver.registerMountReceiver(this);
        mMountReceiver.registerMountListener(this);
        refreshPieChart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    /**
     * Check to mount the device and refresh the pie chart
     */
    private void refreshPieChart() {
        mViewPager = (ViewPager) findViewById(R.id.pie_chart_viewpager);
        LayoutInflater inflater = getLayoutInflater();
        final ArrayList<View> viewList = new ArrayList<View>();
        //Add phone ViewPager,must add
        viewPhone = inflater.inflate(R.layout.storage_piechart, null);
        viewList.add(viewPhone);
        //Open a new thread to query the stored data of phone
        new Thread() {
            public void run() {
                systemSize = storageQueryUtils.getSystemSize();
                totalSizePhone = storageQueryUtils.getPhoneTolSize();
                availableSizePhone = storageQueryUtils.getPhoneAvailableSize();
                pictureSizePhone = storageQueryUtils.getPhonePictureSize();
                musicSizePhone = storageQueryUtils.getPhoneAudioSize();
                videoSizePhone = storageQueryUtils.getPhoneVideoSize();
                apkSizePhone = storageQueryUtils.getPhoneApkSize();
                otherSizePhone = storageQueryUtils.getPhoneOtherSize();
                long[] arrPhone = new long[]{systemSize, totalSizePhone, availableSizePhone,
                        pictureSizePhone, musicSizePhone, videoSizePhone, apkSizePhone, otherSizePhone};
                Bundle bundle = new Bundle();
                bundle.putLongArray(PHONE, arrPhone);
                Message msgPhone = new Message();
                msgPhone.what = PHONE_VALUE;
                msgPhone.obj = bundle;
                mHandler.sendMessage(msgPhone);
            }
        }.start();
        if (mMountManager.isSDCardMounted()) {
            //Add sdCard phone ViewPager and Open a new thread to query the SD card stored data
            viewSdCard = inflater.inflate(R.layout.storage_piechart_sd, null);
            viewList.add(viewSdCard);
            new Thread() {
                public void run() {
                    totalSizeSd = storageQueryUtils.getSdTolSize();
                    availableSizeSd = storageQueryUtils.getSdAvailableSize();
                    pictureSizeSd = storageQueryUtils.getSdPictureSize();
                    musicSizeSd = storageQueryUtils.getSdAudioSize();
                    videoSizeSd = storageQueryUtils.getSdVideoSize();
                    apkSizeSd = storageQueryUtils.getSdApkSize();
                    otherSizeSd = storageQueryUtils.getSdOtherSize();
                    long[] arrSd = new long[]{totalSizeSd, availableSizeSd, pictureSizeSd,
                            musicSizeSd, videoSizeSd, apkSizeSd, otherSizeSd};
                    Bundle bundleSd = new Bundle();
                    bundleSd.putLongArray(SD, arrSd);
                    Message msgSd = new Message();
                    msgSd.what = SD_VALUE;
                    msgSd.obj = bundleSd;
                    mHandler.sendMessage(msgSd);
                }
            }.start();
        }
        if (mMountManager.isOtgMounted()) {
            //Add OTG phone ViewPager and Open a new thread to query the OTG stored data
            viewOtg = inflater.inflate(R.layout.storage_piechart_usbotg, null);
            viewList.add(viewOtg);
            new Thread() {
                public void run() {
                    totalSizeOtg = storageQueryUtils.getOtgTolSize();
                    availableSizeOtg = storageQueryUtils.getOtgAvailableSize();
                    pictureSizeOtg = storageQueryUtils.getOtgPictureSize();
                    musicSizeOtg = storageQueryUtils.getOtgAudioSize();
                    videoSizeOtg = storageQueryUtils.getOtgVideoSize();
                    apkSizeOtg = storageQueryUtils.getOtgApkSize();
                    otherSizeOtg = storageQueryUtils.getOtgOtherSize();
                    long[] arrOtg = new long[]{totalSizeOtg, availableSizeOtg, pictureSizeOtg,
                            musicSizeOtg, videoSizeOtg, apkSizeOtg, otherSizeOtg};
                    Bundle bundleOtg = new Bundle();
                    bundleOtg.putLongArray(OTG, arrOtg);
                    Message msgOtg = new Message();
                    msgOtg.what = OTG_VALUE;
                    msgOtg.obj = bundleOtg;
                    mHandler.sendMessage(msgOtg);
                }
            }.start();
        }
        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                // TODO Auto-generated method stub
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return viewList.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                // TODO Auto-generated method stub
                container.removeView(viewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                // TODO Auto-generated method stub
                container.addView(viewList.get(position));


                return viewList.get(position);
            }
        };
        mViewPager.setAdapter(pagerAdapter);
    }

    private void init(String tag) {
        if (PHONE.equals(tag)) {
            mTextTotal = (TextView) viewPhone.findViewById(R.id.total_storage_value);
            mTextAvailable = (TextView) viewPhone.findViewById(R.id.available_storage_center);
            mTextSystem = (TextView) viewPhone.findViewById(R.id.system_storage_value);
            mTextPicture = (TextView) viewPhone.findViewById(R.id.picture_storage_value);
            mTextMusic = (TextView) viewPhone.findViewById(R.id.music_storage_value);
            mTextVideo = (TextView) viewPhone.findViewById(R.id.video_storage_value);
            mTextApk = (TextView) viewPhone.findViewById(R.id.apk_storage_value);
            mTextOther = (TextView) viewPhone.findViewById(R.id.other_storage_value);
            mAvailableButton = (TextView) viewPhone.findViewById(R.id.available_storage_value);
            mTextTitle = (TextView) viewPhone.findViewById(R.id.memory_title);
            mTextBtn1 = (TextView) viewPhone.findViewById(R.id.pie_chart_btn_one);
            mTextBtn2 = (TextView) viewPhone.findViewById(R.id.pie_chart_btn_two);
            mTextBtn3 = (TextView) viewPhone.findViewById(R.id.pie_chart_btn_three);
        }
        if (SD.equals(tag)) {
            mTextTotalSd = (TextView) viewSdCard.findViewById(R.id.total_storage_value_sd);
            mTextAvailableSd = (TextView) viewSdCard.findViewById(R.id.available_storage_center_sd);
            mTextSystemSd = (TextView) viewSdCard.findViewById(R.id.system_storage_value_sd);
            mTextPictureSd = (TextView) viewSdCard.findViewById(R.id.picture_storage_value_sd);
            mTextMusicSd = (TextView) viewSdCard.findViewById(R.id.music_storage_value_sd);
            mTextVideoSd = (TextView) viewSdCard.findViewById(R.id.video_storage_value_sd);
            mTextApkSd = (TextView) viewSdCard.findViewById(R.id.apk_storage_value_sd);
            mTextOtherSd = (TextView) viewSdCard.findViewById(R.id.other_storage_value_sd);
            mAvailableButtonSd = (TextView) viewSdCard.findViewById(R.id.available_storage_value_sd);
            mTextTitleSd = (TextView) viewSdCard.findViewById(R.id.memory_title_sd);
            mTextBtnSd1 = (TextView) viewSdCard.findViewById(R.id.pie_chart_btn_one_sd);
            mTextBtnSd2 = (TextView) viewSdCard.findViewById(R.id.pie_chart_btn_two_sd);
            mTextBtnSd3 = (TextView) viewSdCard.findViewById(R.id.pie_chart_btn_three_sd);
        }
        if (OTG.equals(tag)) {
            mTextTotalOtg = (TextView) viewOtg.findViewById(R.id.total_storage_value_otg);
            mTextAvailableOtg = (TextView) viewOtg.findViewById(R.id.available_storage_center_otg);
            mTextSystemOtg = (TextView) viewOtg.findViewById(R.id.system_storage_value_otg);
            mTextPictureOtg = (TextView) viewOtg.findViewById(R.id.picture_storage_value_otg);
            mTextMusicOtg = (TextView) viewOtg.findViewById(R.id.music_storage_value_otg);
            mTextVideoOtg = (TextView) viewOtg.findViewById(R.id.video_storage_value_otg);
            mTextApkOtg = (TextView) viewOtg.findViewById(R.id.apk_storage_value_otg);
            mTextOtherOtg = (TextView) viewOtg.findViewById(R.id.other_storage_value_otg);
            mAvailableButtonOtg = (TextView) viewOtg.findViewById(R.id.available_storage_value_otg);
            mTextTitleOtg = (TextView) viewOtg.findViewById(R.id.memory_title_otg);
            mTextBtnOtg1 = (TextView) viewOtg.findViewById(R.id.pie_chart_btn_one_otg);
            mTextBtnOtg2 = (TextView) viewOtg.findViewById(R.id.pie_chart_btn_two_otg);
            mTextBtnOtg3 = (TextView) viewOtg.findViewById(R.id.pie_chart_btn_three_otg);
        }
    }

    /**
     * The thread data assigned to the variable
     */
    private void initValues(long[] arr, String tag) {
        if (PHONE.equals(tag) && arr != null) {
            systemSize = arr[0];
            totalSizePhone = arr[1];
            availableSizePhone = arr[2];
            pictureSizePhone = arr[3];
            musicSizePhone = arr[4];
            videoSizePhone = arr[5];
            apkSizePhone = arr[6];
            otherSizePhone = arr[7];
        } else if (SD.equals(tag) && arr != null) {
            totalSizeSd = arr[0];
            availableSizeSd = arr[1];
            pictureSizeSd = arr[2];
            musicSizeSd = arr[3];
            videoSizeSd = arr[4];
            apkSizeSd = arr[5];
            otherSizeSd = arr[6];
        } else if (OTG.equals(tag) && arr != null) {
            totalSizeOtg = arr[0];
            availableSizeOtg = arr[1];
            pictureSizeOtg = arr[2];
            musicSizeOtg = arr[3];
            videoSizeOtg = arr[4];
            apkSizeOtg = arr[5];
            otherSizeOtg = arr[6];
        }
    }

    /**
     * Get the color of each block
     */
    private ArrayList<Integer> getColors() {
        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(getResources().getColor(R.color.system_color));
        colors.add(getResources().getColor(R.color.picture_color));
        colors.add(getResources().getColor(R.color.music_color));
        colors.add(getResources().getColor(R.color.video_color));
        colors.add(getResources().getColor(R.color.apk_color));
        colors.add(getResources().getColor(R.color.other_color));
        colors.add(getResources().getColor(R.color.available_color));
        return colors;
    }

    /**
     * Get the data for each block
     */
    private List<PieChartView.PieMember> getData(HashMap hashMap) {
        List<PieChartView.PieMember> data = new ArrayList<>();
        Iterator<Map.Entry<Integer, Long>> iterator = hashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            PieChartView.PieMember member = new PieChartView.PieMember();
            Map.Entry<Integer, Long> entry = iterator.next();
            long val = entry.getValue();
            member.setNumber(val);
            member.setIndex(entry.getKey());
            data.add(member);
        }
        return data;
    }

    /**
     * Add internal stored data to pie chart
     */
    private void initInternalChart() {
        mTextTitle.setText(R.string.phone_storage_cn);
        mTextTotal.setText(FileUtils.sizeToString(this, totalSizePhone + systemSize));
        mTextAvailable.setText(FileUtils.sizeToString(this, availableSizePhone));
        mTextPicture.setText(FileUtils.sizeToStringPieChart(this, pictureSizePhone));
        mTextMusic.setText(FileUtils.sizeToStringPieChart(this, musicSizePhone));
        mTextVideo.setText(FileUtils.sizeToStringPieChart(this, videoSizePhone));
        mTextApk.setText(FileUtils.sizeToStringPieChart(this, apkSizePhone));
        mTextOther.setText(FileUtils.sizeToStringPieChart(this, otherSizePhone));
        mTextSystem.setText(FileUtils.sizeToStringPieChart(this, systemSize));
        mAvailableButton.setText(FileUtils.sizeToStringPieChart(this, availableSizePhone));
        //Judge the signs below the pie chart
        if (mMountManager.isSDCardMounted() && !(mMountManager.isOtgMounted())) {
            mTextBtn1.setBackgroundColor(getResources().getColor(R.color.btn_open));
            mTextBtn2.setBackgroundColor(getResources().getColor(R.color.btn_close));
            mTextBtn3.setVisibility(View.GONE);
        } else if (!(mMountManager.isSDCardMounted()) && mMountManager.isOtgMounted()) {
            mTextBtn1.setBackgroundColor(getResources().getColor(R.color.btn_open));
            mTextBtn2.setVisibility(View.GONE);
            mTextBtn3.setBackgroundColor(getResources().getColor(R.color.btn_close));
        } else if (mMountManager.isSDCardMounted() && mMountManager.isOtgMounted()) {
            mTextBtn1.setBackgroundColor(getResources().getColor(R.color.btn_open));
            mTextBtn2.setBackgroundColor(getResources().getColor(R.color.btn_close));
            mTextBtn3.setBackgroundColor(getResources().getColor(R.color.btn_close));
        } else {
            mTextBtn1.setVisibility(View.GONE);
            mTextBtn2.setVisibility(View.GONE);
            mTextBtn3.setVisibility(View.GONE);
        }
        mChart = (PieChartView) viewPhone.findViewById(R.id.pie_chart);
        //Add color and data to pie chart
        HashMap hashMap = new HashMap<>();
        hashMap.put(0, systemSize);
        hashMap.put(1, pictureSizePhone);
        hashMap.put(2, musicSizePhone);
        hashMap.put(3, videoSizePhone);
        hashMap.put(4, apkSizePhone);
        hashMap.put(5, otherSizePhone);
        hashMap.put(6, availableSizePhone);
        mChart.setData(getData(hashMap));
        mChart.setColors(getColors());
    }

    /**
     * Add sd card stored data to pie chart
     */
    private void initSdChart() {
        mTextTitleSd.setText(R.string.main_sd_storage);
        mTextTotalSd.setText(FileUtils.sizeToString(this, totalSizeSd));
        mTextAvailableSd.setText(FileUtils.sizeToString(this, availableSizeSd));
        mTextPictureSd.setText(FileUtils.sizeToStringPieChart(this, pictureSizeSd));
        mTextMusicSd.setText(FileUtils.sizeToStringPieChart(this, musicSizeSd));
        mTextVideoSd.setText(FileUtils.sizeToStringPieChart(this, videoSizeSd));
        mTextApkSd.setText(FileUtils.sizeToStringPieChart(this, apkSizeSd));
        mTextOtherSd.setText(FileUtils.sizeToStringPieChart(this, otherSizeSd));
        mTextSystemSd.setText(FileUtils.sizeToStringPieChart(this, sdOrOtgSystemSize));
        mAvailableButtonSd.setText(FileUtils.sizeToStringPieChart(this, availableSizeSd));
        //Judge the signs below the pie chart
        if (!(mMountManager.isOtgMounted())) {
            mTextBtnSd1.setBackgroundColor(getResources().getColor(R.color.btn_close));
            mTextBtnSd2.setBackgroundColor(getResources().getColor(R.color.btn_open));
            mTextBtnSd3.setVisibility(View.GONE);
        } else if (mMountManager.isOtgMounted()) {
            mTextBtnSd1.setBackgroundColor(getResources().getColor(R.color.btn_close));
            mTextBtnSd2.setBackgroundColor(getResources().getColor(R.color.btn_open));
            mTextBtnSd3.setBackgroundColor(getResources().getColor(R.color.btn_close));
        }
        mChart = (PieChartView) viewSdCard.findViewById(R.id.pie_chart_sd);
        //Add color and data to pie chart
        HashMap hashMap = new HashMap<>();
        hashMap.put(0, sdOrOtgSystemSize);
        hashMap.put(1, pictureSizeSd);
        hashMap.put(2, musicSizeSd);
        hashMap.put(3, videoSizeSd);
        hashMap.put(4, apkSizeSd);
        hashMap.put(5, otherSizeSd);
        hashMap.put(6, availableSizeSd);
        mChart.setData(getData(hashMap));
        mChart.setColors(getColors());
    }

    /**
     * Add usb otg stored data to pie chart
     */
    private void initOtgChart() {
        mTextTitleOtg.setText(R.string.usbotg_m);
        mTextTotalOtg.setText(FileUtils.sizeToString(this, totalSizeOtg));
        mTextAvailableOtg.setText(FileUtils.sizeToString(this, availableSizeOtg));
        mTextPictureOtg.setText(FileUtils.sizeToStringPieChart(this, pictureSizeOtg));
        mTextMusicOtg.setText(FileUtils.sizeToStringPieChart(this, musicSizeOtg));
        mTextVideoOtg.setText(FileUtils.sizeToStringPieChart(this, videoSizeOtg));
        mTextApkOtg.setText(FileUtils.sizeToStringPieChart(this, apkSizeOtg));
        mTextOtherOtg.setText(FileUtils.sizeToStringPieChart(this, otherSizeOtg));
        mTextSystemOtg.setText(FileUtils.sizeToStringPieChart(this, sdOrOtgSystemSize));
        mAvailableButtonOtg.setText(FileUtils.sizeToStringPieChart(this, availableSizeOtg));
        //Judge the signs below the pie chart
        if (mMountManager.isSDCardMounted()) {
            mTextBtnOtg1.setBackgroundColor(getResources().getColor(R.color.btn_close));
            mTextBtnOtg2.setBackgroundColor(getResources().getColor(R.color.btn_close));
            mTextBtnOtg3.setBackgroundColor(getResources().getColor(R.color.btn_open));
        } else if (!(mMountManager.isSDCardMounted())) {
            mTextBtnOtg1.setBackgroundColor(getResources().getColor(R.color.btn_close));
            mTextBtnOtg2.setVisibility(View.GONE);
            mTextBtnOtg3.setBackgroundColor(getResources().getColor(R.color.btn_open));
        }
        mChart = (PieChartView) viewOtg.findViewById(R.id.pie_chart_otg);
        //Add color and data to pie chart
        HashMap hashMap = new HashMap<>();
        hashMap.put(0, sdOrOtgSystemSize);
        hashMap.put(1, pictureSizeOtg);
        hashMap.put(2, musicSizeOtg);
        hashMap.put(3, videoSizeOtg);
        hashMap.put(4, apkSizeOtg);
        hashMap.put(5, otherSizeOtg);
        hashMap.put(6, availableSizeOtg);
        mChart.setData(getData(hashMap));
        mChart.setColors(getColors());
    }

    @Override
    public void onMounted() {
        MountManager.getInstance().init(this);
        refreshPieChart();
    }

    @Override
    public void onUnmounted(String mountPoint) {
        MountManager.getInstance().init(this);
        refreshPieChart();
    }

    @Override
    public void onScannerFinished() {

    }

    @Override
    public void onScannerStarted() {

    }

    @Override
    public void onEject() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMountReceiver);
    }
}