package com.monster.market.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.market.MarketApplication;
import com.monster.market.R;
import com.monster.market.bean.AppDetailAnimInfo;
import com.monster.market.bean.AppDetailInfo;
import com.monster.market.bean.AppListInfo;
import com.monster.market.bean.InstalledAppInfo;
import com.monster.market.constants.HttpConstant;
import com.monster.market.constants.WandoujiaDownloadConstant;
import com.monster.market.db.AppDownloadDao;
import com.monster.market.download.AppDownloadData;
import com.monster.market.download.AppDownloadService;
import com.monster.market.download.AppDownloader;
import com.monster.market.download.DownloadUpdateListener;
import com.monster.market.http.DataResponse;
import com.monster.market.http.RequestError;
import com.monster.market.http.RequestHelper;
import com.monster.market.http.data.AppDetailRecommendResultData;
import com.monster.market.install.AppInstallService;
import com.monster.market.install.InstallAppManager;
import com.monster.market.utils.ApkUtil;
import com.monster.market.utils.BitmapUtil;
import com.monster.market.utils.LoadingPageUtil;
import com.monster.market.utils.PicBrowserUtil;
import com.monster.market.utils.ProgressBtnUtil;
import com.monster.market.utils.SettingUtil;
import com.monster.market.utils.SystemUtil;
import com.monster.market.views.CustomAnimCallBack;
import com.monster.market.views.CustomAnimation;
import com.monster.market.views.DetailScrollView;
import com.monster.market.views.ExpandableTextView;
import com.monster.market.views.ProgressBtn;
import com.monster.market.views.SwipeBackLayout;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import mst.app.dialog.AlertDialog;
import mst.widget.FoldProgressBar;
import mst.widget.toolbar.Toolbar;

import static com.monster.market.utils.DensityUtil.dip2px;

/**
 * Created by xiaobin on 16-8-15.
 */
public class AppDetailActivity extends BaseActivity {

    private static final String TAG = "AppDetailActivity";
    public static final String PACKAGE_NAME = "package_name";
    public static final String REPORT_MODULID = "REPORT_MODULID";
    public static final String ANIM_PARAMS = "ANIM_PARAMS";
    public static final String ICON_URL = "ICON_URL";

    private String packageName = "";
    private int reportModulId;

    private LoadingPageUtil loadingPageUtil;
    private ProgressBtnUtil progressBtnUtil;

    private AppDownloadData downloadData;

    private boolean stopFlag = false;
    private int current_status = AppDownloader.STATUS_DEFAULT;

    private LinearLayout ll_toolbar;
    private View view_action_bar_top;

    private DetailScrollView scrollView;
    private boolean scrollable = false;     // ScrollView是否可滚动
    private final int descriptionLine = 5;

    private View view_close;
    private SwipeBackLayout swipeBackLayout;
    private LinearLayout ll_content;
    private LinearLayout ll_content_top;
    private ImageView iv_app_icon;
    private TextView tv_name;
    private RatingBar rb_score;
    private TextView tv_version;
    private ProgressBtn progressBtn;
    private RelativeLayout rl_introduce;
    private TextView tv_itd_version;
    private TextView tv_itd_author;
    private TextView tv_itd_update_time;
    private TextView tv_itd_type;
    private ExpandableTextView tv_description;
    private HorizontalScrollView hsv_pic;
    private LinearLayout mAppPicBrowseLayout = null;
    private FoldProgressBar pb_recommend;
    private LinearLayout ll_recommend_container;

    private FrameLayout layout_bottom;
    private LinearLayout ll_dis_progress;
    private TextView dis_download_text;
    private FrameLayout mDownloadBtnLayout, mDownloadProLayout;
    private ProgressBar mProgressBar;
    private TextView download_text;
    private LinearLayout mDownloadBtnInstall;
    private ImageView mToDownloadManagerBtn;
    private ImageView mCancelDownloadBtn, mDownloadInstallView;
    private TextView mDownloadBtn = null;
    private boolean isOpenAnimal = true;

    private AnimationDrawable animationDrawable;

    private int empty_height = 0;
    private int toolbar_layout_height = 0;
    private int statusBarHeight = 0;
    private int iconViewPaddingRight = 0;       // 移动图标右侧间距
    private int iconViewAfterPaddingTop = 0;    // 移动后图片需要的上间距
    private int picBrowserPaddingTop = 0;       // 移动图片上间距

    // 图片加载工具
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions optionsImage, opIconsImage;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    private boolean closing = false;

    // 上一次点击item项的时间
    private long lastClickItemTime = 0;

    // 点击item的图标url
    private String mIconUrl;
    // 包含动画所需参数的对象
    private AppDetailAnimInfo mAnimInfo;

    // 用来显示动画的布局
    private RelativeLayout mAnimLayout;
    private ImageView mAnimIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_app_detail);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int flag = getWindow().getDecorView().getSystemUiVisibility();
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | flag);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        getIntentData();
        initImageLoad();
        initViews();
        initData();
        playAnimation();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (empty_height == 0) {
            empty_height = getResources().getDimensionPixelOffset(R.dimen.detail_page_empty_height);

            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);

            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);

            toolbar_layout_height = (int) typedValue.getDimension(outMetrics);
            statusBarHeight = getStatusBarHeight();

            view_action_bar_top.setLayoutParams(new LinearLayout.LayoutParams(0, statusBarHeight));

            iconViewPaddingRight = getResources().getDimensionPixelOffset(R.dimen.detail_icon_after_padding_right);
            iconViewAfterPaddingTop = getResources().getDimensionPixelOffset(R.dimen.detail_icon_after_padding_top);
            picBrowserPaddingTop = getResources().getDimensionPixelOffset(R.dimen.detail_pic_browser_after_padding_top);
        }

        if (stopFlag) {
            updateListener.downloadProgressUpdate();
            stopFlag = false;
        }
        AppDownloadService.registerUpdateListener(updateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopFlag = true;
        AppDownloadService.unRegisterUpdateListener(updateListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PicBrowserUtil.resetImgVContainer();

        if (loadingPageUtil != null) {
            loadingPageUtil.exit();
        }
    }

    @Override
    public void initViews() {
        ll_toolbar = (LinearLayout) findViewById(R.id.ll_toolbar);
        view_action_bar_top = findViewById(R.id.view_action_bar_top);

        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mToolbar.setTitle(getString(R.string.app_detail_title));
        mToolbar.setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);
        mToolbar.inflateMenu(R.menu.toolbar_action_button);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitActivity();
            }
        });
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle().equals("setting")) {
                    Intent i = new Intent(AppDetailActivity.this, ManagerPreferenceActivity.class);
                    startActivity(i);
                }
                return false;
            }
        });

        scrollView = (DetailScrollView) findViewById(R.id.scrollView);
        scrollView.setOnScrollStoppedListener(new DetailScrollView.OnScrollStoppedListener() {
            @Override
            public void onScrollStopped() {
                int Y = scrollView.getScrollY();
                if (Y > 0 && Y < empty_height / 2) {
                    scrollView.smoothScrollTo(0, 0);
                } else if (Y >= empty_height / 2 && Y < empty_height) {
                    scrollView.smoothScrollTo(0, empty_height);
                }
            }
        });

        scrollView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!scrollable) {
                    return true;
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    scrollView.startScrollerTask();
                }

                return false;
            }
        });

        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int x, int y, int oldx, int oldy) {

                int progress = getProgress(y, empty_height);

                if (y >= empty_height) {
                    ll_toolbar.setVisibility(View.VISIBLE);
                    int actionProgress = (int) ((empty_height - y) * 1.0 / statusBarHeight * 100);
                    setActionBarProgress(actionProgress);

                } else {
                    ll_toolbar.setVisibility(View.GONE);
                    setActionBarProgress(0);
                }

                if (scrollView.getLastY() < empty_height && y >= empty_height) {
                    scrollView.scrollTo(0, empty_height);
                }

                setIconProgress(progress);
                setNameProgress(progress);
                setRatingBarProgress(progress);
                setSizeProgress(progress);
                setButtonProgress(progress);
                setPicBrowseProgress(progress);
                setDescriptionProgress(progress);
                setBottomProgress(progress);
            }
        });

        view_close = findViewById(R.id.view_close);
        swipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe_layout);
        ll_content = (LinearLayout) findViewById(R.id.ll_content);
        ll_content_top = (LinearLayout) findViewById(R.id.ll_content_top);
        iv_app_icon = (ImageView) findViewById(R.id.iv_app_icon);
        tv_name = (TextView) findViewById(R.id.tv_name);
        rb_score = (RatingBar) findViewById(R.id.rb_score);
        tv_version = (TextView) findViewById(R.id.tv_version);
        progressBtn = (ProgressBtn) findViewById(R.id.progressBtn);
        rl_introduce = (RelativeLayout) findViewById(R.id.rl_introduce);
        tv_itd_version = (TextView) findViewById(R.id.tv_itd_version);
        tv_itd_author = (TextView) findViewById(R.id.tv_itd_author);
        tv_itd_update_time = (TextView) findViewById(R.id.tv_itd_update_time);
        tv_itd_type = (TextView) findViewById(R.id.tv_itd_type);
        tv_description = (ExpandableTextView) findViewById(R.id.tv_description);
        hsv_pic = (HorizontalScrollView) findViewById(R.id.hsv_pic);
        mAppPicBrowseLayout = (LinearLayout) findViewById(R.id.app_pic_browse_view);
        pb_recommend = (FoldProgressBar) findViewById(R.id.pb_recommend);
        ll_recommend_container = (LinearLayout) findViewById(R.id.ll_recommend_container);
        imageLoader.displayImage(mIconUrl, iv_app_icon, opIconsImage);
        view_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitActivity();
            }
        });

        initLoadingPage();

        layout_bottom = (FrameLayout) findViewById(R.id.layout_bottom);
        mDownloadBtn = (TextView) findViewById(R.id.download_btn);
        ll_dis_progress = (LinearLayout) findViewById(R.id.ll_dis_progress);
        dis_download_text = (TextView) findViewById(R.id.dis_download_text);
        mDownloadBtnLayout = (FrameLayout) findViewById(R.id.download_btn_layout);
        mDownloadBtnInstall = (LinearLayout) findViewById(R.id.download_btn_install);
        mDownloadProLayout = (FrameLayout) findViewById(R.id.download_progress);
        mProgressBar = (ProgressBar) findViewById(R.id.download_progress_rate);
        download_text = (TextView) findViewById(R.id.download_text);
        mCancelDownloadBtn = (ImageView) findViewById(R.id.cancel_download_btn);
        mToDownloadManagerBtn = (ImageView) findViewById(R.id.redirect_download_btn);
        mDownloadInstallView = (ImageView) findViewById(R.id.download_install);

        mDownloadProLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 不做操作, 消费事件
            }
        });

        mCancelDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AppDownloadService.cancelDownload(AppDetailActivity.this, downloadData);
            }
        });

        mToDownloadManagerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent lInt = new Intent(AppDetailActivity.this, DownloadManagerActivity.class);
                startActivity(lInt);
            }
        });

        mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SystemUtil.hasNetwork()) {
                    Toast.makeText(AppDetailActivity.this,
                            getString(R.string.no_network_download_toast),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String dis_text = download_text.getText().toString();

                if (dis_text
                        .equals(getResources().getString(R.string.app_download))
                        || dis_text.equals(getResources().getString(
                        R.string.download_process_update))) {
                    if (!SettingUtil.canDownload(MarketApplication.getInstance())) {

                        AlertDialog mWifiConDialog = new AlertDialog.Builder(
                                AppDetailActivity.this)
                                .setTitle(
                                        getResources().getString(
                                                R.string.dialog_prompt))
                                .setMessage(
                                        getResources().getString(
                                                R.string.no_wifi_download_message))
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {

                                                SettingUtil.setOnlyWifiDownload(MarketApplication.getInstance(), false);
                                                doTheDownOpr();

                                            }

                                        }).create();
                        mWifiConDialog.show();
                    } else {
                        doTheDownOpr();
                    }
                } else if (dis_text.equals(getResources().getString(
                        R.string.app_install))) {
                    mDownloadBtnLayout.setVisibility(view.GONE);
                    mDownloadBtnInstall.setVisibility(view.VISIBLE);
                    animationDrawable = (AnimationDrawable) mDownloadInstallView
                            .getBackground();

                    animationDrawable.start();

                    AppDownloadData tempData = AppDownloadService.getAppDownloadDao()
                            .getAppDownloadData(downloadData.getTaskId());
                    String fileDir = tempData.getFileDir();
                    fileDir = fileDir == null ? "" : fileDir;
                    String fileName = tempData.getFileName();
                    fileName = fileName == null ? "" : fileName;
                    final File file = new File(fileDir, fileName);

                    tempData.setStatus(AppDownloader.STATUS_INSTALL_WAIT);
                    AppDownloadService.getAppDownloadDao().updateStatus(tempData.getTaskId(),
                            AppDownloader.STATUS_INSTALL_WAIT);

                    AppInstallService.startInstall(AppDetailActivity.this, tempData);
                } else if (dis_text.equals(getResources().getString(
                        R.string.item_open))) {
                    ApkUtil.openApp(AppDetailActivity.this,
                            downloadData.getPackageName());
                }
            }
        });

//        progressBtn.setOnButtonClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 上报下载
//                reportDownload();
//            }
//        });

        // 下载进度条点击事件
//        ll_dis_progress.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (downloadData != null) {
//                    AppDownloadService.pauseOrContinueDownload(AppDetailActivity.this, downloadData);
//                }
//            }
//        });

    }

    @Override
    public void initData() {
        progressBtnUtil = new ProgressBtnUtil();

        RequestHelper.getAppDetail(this, packageName, new DataResponse<AppDetailInfo>() {
            @Override
            public void onResponse(AppDetailInfo value) {
                if (value != null) {
                    AppDownloadData add = null;
                    AppDownloadDao dao = AppDownloadService.getAppDownloadDao();
                    String taskId = SystemUtil.buildDownloadTaskId(value.getPackageName(), value.getVersionCode());
                    add = dao.getAppDownloadData(taskId);
                    if (add == null) {
                        add = SystemUtil.buildAppDownloadData(value);
                    }
                    downloadData = add;
                    updateListener.downloadProgressUpdate();
                }

                scrollable = true;
                loadingPageUtil.hideLoadPage();

                List<AppDetailInfo.AppImageInfo> infos = value.getAppImageList();
                String[] images = new String[infos.size()];
                for (int i = 0; i < infos.size(); i++) {
                    images[i] = infos.get(i).getNormalPic();
                }
                setupAppDetailDisplay(images);

                // 开始头像图片异步加载
                imageLoader.displayImage(value.getBigAppIcon(), iv_app_icon,
                        opIconsImage);
                iv_app_icon.setVisibility(View.VISIBLE);

                tv_name.setText(value.getAppName());
                String downloadCountStr = String.format(getString(
                        R.string.item_download_count_str), value.getDownloadCountStr());
                tv_version.setText(downloadCountStr + "  " + SystemUtil.bytes2kb(value.getAppSize()));
                int level = value.getApplevel();
                float star = level * 1.0f / 2;
                rb_score.setRating(star);

                tv_itd_version.setText(String.format(getString(R.string.app_version_str), value.getVersionName()));
                tv_itd_author.setText(String.format(getString(R.string.app_author_str), value.getAuthor()));
                tv_itd_update_time.setText(String.format(getString(R.string.app_update_time_str), value.getUpdateTime()));
                tv_itd_type.setText(String.format(getString(R.string.app_type_str), value.getAppType()));

                String description = value.getAppDescription();
                description = description.replace("\r", "").replace("\n\r", "");
                tv_description.setText(Html.fromHtml(description));

                boolean ifFold = false;
                boolean ifHide = false;

                getDetailsRecommend();
            }

            @Override
            public void onErrorResponse(RequestError error) {
                iv_app_icon.setVisibility(View.INVISIBLE);
                if (error.getErrorType() == RequestError.ERROR_NO_NETWORK) {
                    loadingPageUtil.showNoNetWork();
                } else {
                    loadingPageUtil.showNetworkError();
                }
            }
        });

        // 更新右上角显示
        if (AppDownloadService.getDownloadingCountMore() > 0 ||
                SettingUtil.getLastUpdateAppCount(AppDetailActivity.this) > 0) {
            mToolbar.getMenu().findItem(R.id.menu_setting)
                    .setIcon(R.drawable.toolbar_setting_message_normal);
        } else {
            mToolbar.getMenu().findItem(R.id.menu_setting)
                    .setIcon(R.drawable.toolbar_setting_normal);
        }
    }

    private void initLoadingPage() {
        loadingPageUtil = new LoadingPageUtil();

        loadingPageUtil.init(this, findViewById(R.id.frameLayout));
        loadingPageUtil.setOnRetryListener(new LoadingPageUtil.OnRetryListener() {
            @Override
            public void retry() {
                initData();
            }
        });
        loadingPageUtil.setOnShowListener(new LoadingPageUtil.OnShowListener() {
            @Override
            public void onShow() {
//				mListView.setVisibility(View.GONE);
            }
        });
        loadingPageUtil.setOnHideListener(new LoadingPageUtil.OnHideListener() {
            @Override
            public void onHide() {
//				mListView.setVisibility(View.VISIBLE);
            }
        });
        loadingPageUtil.showLoadPage();
        loadingPageUtil.showLoading();
    }

    private void getIntentData() {
        packageName = getIntent().getStringExtra(PACKAGE_NAME);
        reportModulId = getIntent().getIntExtra(REPORT_MODULID, HttpConstant.REPORT_MODULID_HOMEPAGE);
        mAnimInfo = getIntent().getParcelableExtra(ANIM_PARAMS);
        mIconUrl = getIntent().getStringExtra(ICON_URL);
    }

    private void playAnimation() {
        if (mAnimInfo == null) {
            swipeBackLayout.setVisibility(View.VISIBLE);
            return;
        }

        mAnimLayout = (RelativeLayout) findViewById(R.id.anim_layout);
        mAnimIcon = (ImageView) findViewById(R.id.anim_icon);

        int layoutInitHeight = mAnimInfo.getLayoutInitHeight();
        int layoutMarginTop = mAnimInfo.getLayoutMarginTop();
        int iconMarginLeft = mAnimInfo.getIconMarginLeft();
        int iconMarginTop = mAnimInfo.getIconMarginTop();
        int initIconSize = mAnimInfo.getInitIconSize();
        int finalIconSize = mAnimInfo.getFinalIconSize();
        Point coordinate = mAnimInfo.getCoordinate();

        mAnimIcon.getLayoutParams().width = initIconSize;
        mAnimIcon.getLayoutParams().height = initIconSize;
        mAnimLayout.getLayoutParams().height = layoutInitHeight;

        setLayout(mAnimLayout, 0, coordinate.y);
        mAnimLayout.setVisibility(View.VISIBLE);
        setLayout(mAnimIcon, coordinate.x + iconMarginLeft, coordinate.y + iconMarginTop);
        mAnimIcon.setVisibility(View.VISIBLE);
        imageLoader.displayImage(mIconUrl, mAnimIcon, opIconsImage);


        int translationY = coordinate.y - layoutMarginTop;
        // layout向上平移
        ObjectAnimator layoutAnimY = ObjectAnimator.ofFloat(mAnimLayout, View.TRANSLATION_Y, 0, -translationY);

        // layout放大,从初始高度layoutInitHeight放大到 (屏幕高度 - layoutMarginTop)
        ValueAnimator layoutValueAnimator = createValueAnimator(mAnimLayout, getWidth(), getWidth(), layoutInitHeight,
                getHeight() - layoutMarginTop);

        // icon放大, 从initIconSize放大到finalIconSize 例如: 52dp*52dp,放大到 63dp*63dp
        ValueAnimator iconValueAnimator = createValueAnimator(mAnimIcon, initIconSize, finalIconSize, initIconSize, finalIconSize);

        int iconTranslationY = coordinate.y + iconMarginTop - layoutMarginTop;
        // icon位移
        ObjectAnimator iconAnimX = ObjectAnimator.ofFloat(mAnimIcon, View.TRANSLATION_X, 0, getWidth() / 2 - coordinate.x -
                finalIconSize / 2 - iconMarginLeft);
        ObjectAnimator iconAnimY = ObjectAnimator.ofFloat(mAnimIcon, View.TRANSLATION_Y, 0, -iconTranslationY + dip2px(this,
                31));

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.setStartDelay(100);

        if (!mAnimInfo.isDebug()) {
            if (mAnimInfo.getType() == AppDetailAnimInfo.TYPE_BOTTOM_SLIDE_IN) {
                layoutValueAnimator.setDuration(10);
                animatorSet.play(layoutValueAnimator);
                layoutAnimY.setDuration(300);
                animatorSet.play(layoutAnimY).after(10);
            } else {
                animatorSet.setDuration(300);
                animatorSet.play(layoutValueAnimator).with(layoutAnimY);
            }
            animatorSet.play(iconValueAnimator).with(layoutValueAnimator);
            animatorSet.play(iconAnimX).with(layoutValueAnimator);
            animatorSet.play(iconAnimY).with(layoutValueAnimator);
            animatorSet.start();
        }

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mAnimInfo.isDebug()) {
                    swipeBackLayout.setVisibility(View.VISIBLE);
                    mAnimIcon.setVisibility(View.GONE);
                    mAnimLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    // 创建缩放动画
    private ValueAnimator createValueAnimator(final View targetView, final int startX, final int endX, final int startY, final
    int endY) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(1, 100);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private final static String ANIM_TAG = "##Value animator";
            private IntEvaluator mIntEvaluator = new IntEvaluator();

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int currentValue = (Integer) animator.getAnimatedValue();
                Log.d(ANIM_TAG, "current value: " + currentValue);
                // 3
                float fraction = animator.getAnimatedFraction();
                targetView.getLayoutParams().width = mIntEvaluator.evaluate(fraction, startX, endX);
                targetView.getLayoutParams().height = mIntEvaluator.evaluate(fraction, startY, endY);
                targetView.requestLayout();
            }
        });
        return valueAnimator;
    }

    /*
     * 设置控件所在的位置YY，并且不改变宽高，
     * XY为绝对位置
     */
    public static void setLayout(View view, int x, int y) {
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(view.getLayoutParams());
        margin.setMargins(x, y, x + margin.width, y + margin.height);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
        view.setLayoutParams(layoutParams);
    }

    private void initImageLoad() {
        optionsImage = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.icon_detail_default)
                .showImageForEmptyUri(R.drawable.icon_detail_default)
                .showImageOnFail(R.drawable.icon_detail_default).cacheInMemory(true)
                .cacheOnDisc(true).build();

        opIconsImage = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.icon_app_default)
                .showImageForEmptyUri(R.drawable.icon_app_default)
                .displayer(new RoundedBitmapDisplayer(getResources().getDimensionPixelOffset(R.dimen.app_icon_displayer)))
                .showImageOnFail(R.drawable.icon_app_default)
                .cacheInMemory(true).cacheOnDisc(true).build();
    }


    class ShowListener implements View.OnClickListener {
        private boolean ifFold;
        private boolean ifHide;
        private ExpandableTextView content;
        private ImageView show;
        private int lines;

        public ShowListener(boolean ifFold, boolean ifHide,
                            ExpandableTextView content, ImageView show, int lines) {
            this.ifFold = ifFold;
            this.ifHide = ifHide;
            this.content = content;
            this.show = show;
            this.lines = lines;
        }

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
//            if (ifFold) {
//                if (!ifHide) {
//                    show.setVisibility(View.VISIBLE);
//                    moreView.setBackgroundResource(R.drawable.page_content_arrow_down);
//
//                    ifHide = true;
//                    setShowExPandAnimal();
//
//                } else {
//                    show.setVisibility(View.VISIBLE);
//                    content.setLines(lines);
//                    content.setCollapseLines(lines, true);
//                    moreView.setBackgroundResource(R.drawable.page_content_arrow_up);
//                    ifHide = false;
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
//                        }
//                    });
//                }
//            }
        }

    }

    private void setupAppDetailDisplay(final String[] icons) {

        ImageView lImg = null;
        PicBrowserUtil.resetImgVContainer();

        mAppPicBrowseLayout
                .setLayoutParams(new FrameLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, getResources()
                        .getDimensionPixelOffset(
                                R.dimen.app_detail_pic_browser_height)));

        int h = getResources().getDimensionPixelOffset(R.dimen.app_detail_pic_browser_height);
        int w = getResources().getDimensionPixelOffset(R.dimen.app_detail_pic_browser_width);

        if ((icons.length == 0)) {

            for (int i = 0; i < 3; i++) {

                lImg = new ImageView(this);

                lImg.setLayoutParams(new LinearLayout.LayoutParams(w, h));

                lImg.setTag(i);
                lImg.setScaleType(ImageView.ScaleType.FIT_XY);
                lImg.setBackgroundResource(R.drawable.icon_detail_default);

                if (i == 0) {
//                    mAppPicBrowseLayout.addView(addDivider(52));
                } else {
                    mAppPicBrowseLayout.addView(addDivider(36));
                }

                mAppPicBrowseLayout.addView(lImg);

                if (i == 2) {
                    mAppPicBrowseLayout.addView(addDivider(51));
                }

            }
        } else {
            for (int i = 0; i < icons.length; i++) {

                lImg = new ImageView(this);

                lImg.setLayoutParams(new LinearLayout.LayoutParams(w, h));

                lImg.setTag(i);
                lImg.setScaleType(ImageView.ScaleType.FIT_XY);

                lImg.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub

                        String index = v.getTag().toString();

                        Intent intent = new Intent(AppDetailActivity.this,
                                PictureViewActivity.class);

                        intent.putExtra("index", Integer.valueOf(index));
                        intent.putExtra("content", icons);

                        startActivity(intent);
                        overridePendingTransition(0, 0);

                    }
                });

                imageLoader.displayImage(icons[i], lImg, optionsImage,
                        animateFirstListener);

                if (i == 0) {
//                    mAppPicBrowseLayout.addView(addDivider(DensityUtil.dip2px(
//                            this, 17)));
                } else {
                    mAppPicBrowseLayout.addView(addDivider(dip2px(
                            this, 5)));
                }

                mAppPicBrowseLayout.addView(lImg);
                int[] location = new int[2];

                mAppPicBrowseLayout.getLocationOnScreen(location);

                PicBrowserUtil.addImgV(lImg);

                if (i == icons.length - 1) {
                    mAppPicBrowseLayout.addView(addDivider(dip2px(
                            this, 16)));
                }

            }
        }
    }

    private View addDivider(int pDividerLen) {
        View lView = new View(this);
        lView.setBackgroundColor(Color.WHITE);
        lView.setLayoutParams(new LinearLayout.LayoutParams(pDividerLen, 640));

        return lView;
    }

    private class AnimateFirstDisplayListener extends
            SimpleImageLoadingListener {

        final List<String> displayedImages = Collections
                .synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            if (loadedImage != null) {
                final ImageView imageView = (ImageView) view;
                if (loadedImage.getHeight() < loadedImage.getWidth()) {
                    loadedImage = BitmapUtil.rotateBitmap(loadedImage, 90);
                }

                int h = getResources().getDimensionPixelOffset(
                        R.dimen.app_detail_pic_browser_height);
                int w = getResources().getDimensionPixelOffset(
                        R.dimen.app_detail_pic_browser_width);

                imageView.setLayoutParams(new LinearLayout.LayoutParams(w, h));

                imageView.setImageBitmap(loadedImage);
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }

            }
        }

    }

    @Override
    public void onBackPressed() {
        exitActivity();
    }

    private void exitActivity() {
        if (!closing) {
            Animation m = new TranslateAnimation(0, 0, 0, swipeBackLayout.getHeight());
            m.setDuration(200);
            m.setFillAfter(true);
            m.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    closing = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    closing = false;
                    finish();
                    overridePendingTransition(0, android.R.anim.fade_out);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            swipeBackLayout.startAnimation(m);

            Animation m1 = new TranslateAnimation(0, 0, 0, layout_bottom.getHeight());
            m1.setDuration(200);
            m1.setFillAfter(true);
            m1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    closing = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    closing = false;
                    finish();
                    overridePendingTransition(0, android.R.anim.fade_out);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            layout_bottom.startAnimation(m);

            if (ll_toolbar != null && ll_toolbar.getVisibility() == View.VISIBLE) {

                Animation m2 = new TranslateAnimation(0, 0, 0, -ll_toolbar.getHeight());
                m2.setDuration(200);
                m2.setFillAfter(true);
                m2.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        closing = true;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        closing = false;
                        finish();
                        overridePendingTransition(0, android.R.anim.fade_out);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                ll_toolbar.startAnimation(m2);

            }

        }
    }

    private int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }

    /* 获取屏幕高度 */
    public int getHeight() {
        DisplayMetrics displaysMetrics = new DisplayMetrics();
        getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displaysMetrics);
        //得到屏幕高
        return displaysMetrics.heightPixels;
    }

    /* 获取屏幕宽度 */
    public int getWidth() {
        DisplayMetrics displaysMetrics = new DisplayMetrics();
        getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displaysMetrics);
        //得到屏幕宽
        return displaysMetrics.widthPixels;
    }


    private int getProgress(int progress, int max) {
        if (progress > max) {
            return 100;
        } else if (progress <= 0) {
            return 0;
        }
        return (int) (progress * 1.0 / max * 100);
    }

    private void setActionBarProgress(int progress) {
        progressBtn.setAlpha((progress * 1.0f / 100));
    }

    private void setIconProgress(int progress) {
        int left = iv_app_icon.getLeft();
        int parentPaddingLeft = ll_content_top.getPaddingLeft();
        int value = left - parentPaddingLeft;

        int translationX = (int) (value * (progress * 1.0f / 100));
        int translationY = (int) ((toolbar_layout_height + iconViewAfterPaddingTop) * (progress * 1.0f / 100));
        iv_app_icon.setTranslationX(-translationX);
        iv_app_icon.setTranslationY(translationY);
    }

    private void setNameProgress(int progress) {
        int left = tv_name.getLeft();
        int parentPaddingLeft = ll_content_top.getPaddingLeft();
        int imgWidth = iv_app_icon.getWidth();
        int value = left - (parentPaddingLeft + imgWidth + iconViewPaddingRight);

        int translationX = (int) (value * (progress * 1.0f / 100));
        int translationY = (int) ((ll_content_top.getPaddingTop() - iconViewAfterPaddingTop) * (progress * 1.0f / 100));
        tv_name.setTranslationX(-translationX);
        tv_name.setTranslationY(-translationY);
    }

    private void setRatingBarProgress(int progress) {
        int left = rb_score.getLeft();
        int parentPaddingLeft = ll_content_top.getPaddingLeft();
        int imgWidth = iv_app_icon.getWidth();
        int value = left - (parentPaddingLeft + imgWidth + iconViewPaddingRight);

        int translationX = (int) (value * (progress * 1.0f / 100));
        int translationY = (int) ((ll_content_top.getPaddingTop() - iconViewAfterPaddingTop) * (progress * 1.0f / 100));
        rb_score.setTranslationX(-translationX);
        rb_score.setTranslationY(-translationY);
    }

    private void setSizeProgress(int progress) {
        int left = tv_version.getLeft();
        int parentPaddingLeft = ll_content_top.getPaddingLeft();
        int imgWidth = iv_app_icon.getWidth();
        int value = left - (parentPaddingLeft + imgWidth + iconViewPaddingRight);

        int translationX = (int) (value * (progress * 1.0f / 100));
        int translationY = (int) ((ll_content_top.getPaddingTop() - iconViewAfterPaddingTop) * (progress * 1.0f / 100));
        tv_version.setTranslationX(-translationX);
        tv_version.setTranslationY(-translationY);
    }

    private void setButtonProgress(int progress) {
        progressBtn.setAlpha(1 - (progress * 1.0f / 100) * 2);
    }

    private void setPicBrowseProgress(int progress) {
        int buttonHeight = progressBtn.getHeight();
        int value = buttonHeight + picBrowserPaddingTop;

        int translationY = (int) (value * (progress * 1.0f / 100));
        hsv_pic.setTranslationY(-translationY);
    }

    private void setDescriptionProgress(int progress) {
        int buttonHeight = progressBtn.getHeight();
        int value = buttonHeight + picBrowserPaddingTop;

        int translationY = (int) (value * (progress * 1.0f / 100));
        rl_introduce.setTranslationY(-translationY);
    }

    private void setBottomProgress(int progress) {
        layout_bottom.setAlpha((progress * 1.0f / 100));

        int height = layout_bottom.getHeight();

        int translationY = (int) (height * (progress * 1.0f / 100));
        layout_bottom.setTranslationY(-translationY);
    }

    private void getDetailsRecommend() {
        RequestHelper.getDetailsRecommend(this, packageName, new DataResponse<AppDetailRecommendResultData>() {
            @Override
            public void onResponse(AppDetailRecommendResultData value) {
                int size = 0;
                int max = 3;
                final List<AppListInfo> infos = value.getAppList();
                if (infos.size() > max) {
                    size = 3;
                } else {
                    size = infos.size();
                }

                pb_recommend.setVisibility(View.GONE);
                ll_recommend_container.setVisibility(View.VISIBLE);

                LayoutInflater inflater = LayoutInflater.from(AppDetailActivity.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 1);
                View itemView = null;
                ImageView iv_icon;
                TextView tv_name;
                TextView tv_size;
                for (int i = 0; i < size; i++) {
                    itemView = inflater.inflate(R.layout.item_app_detail_recomend, null);
                    iv_icon = (ImageView) itemView.findViewById(R.id.iv_recommend_app);
                    tv_name = (TextView) itemView.findViewById(R.id.tv_recommend_app_name);
                    tv_size = (TextView) itemView.findViewById(R.id.tv_recommend_app_size);

                    imageLoader.displayImage(infos.get(i).getBigAppIcon(), iv_icon,
                            opIconsImage);
                    tv_name.setText(infos.get(i).getAppName());
                    tv_size.setText(SystemUtil.bytes2kb(infos.get(i).getAppSize()));

                    ll_recommend_container.addView(itemView, params);

                    final String pName = infos.get(i).getPackageName();
                    final String icon = infos.get(i).getBigAppIcon();
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (System.currentTimeMillis() - lastClickItemTime > 1000) {

                                Intent intent = new Intent(AppDetailActivity.this, AppDetailActivity.class);
                                intent.putExtra(AppDetailActivity.PACKAGE_NAME, pName);
                                intent.putExtra(AppDetailActivity.ANIM_PARAMS, getAppDetailAnimInfo(view));
                                intent.putExtra(AppDetailActivity.ICON_URL, icon);
                                startActivity(intent);
                                overridePendingTransition(0, 0);

                                lastClickItemTime = System.currentTimeMillis();
                            }
                        }
                    });
                }
            }

            @Override
            public void onErrorResponse(RequestError error) {

            }
        });
    }

    private AppDetailAnimInfo getAppDetailAnimInfo(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        // 为了让icon居中,把背景layout的y坐标上移,然后通过setIconMarginTop让icon的y坐标下移
        Point point = new Point(location[0], location[1] - (view.getHeight() / 2 - dip2px(this, 22)));

        AppDetailAnimInfo animInfo = new AppDetailAnimInfo();

        animInfo.setLayoutInitHeight(view.getHeight())
                .setLayoutMarginTop(dip2px(this, 156))
                .setIconMarginLeft(view.getWidth() / 2 - dip2px(this, 22))
                .setIconMarginTop(view.getHeight() / 2 - dip2px(this, 22))
                .setInitIconSize(dip2px(this, 44))
                .setFinalIconSize(dip2px(this, 63))
                .setCoordinate(point)
                .setDebug(false);
        return animInfo;
    }

    /**
     * 检查应用的状态, 并显示相应布局
     */
    public void checkSoftwareState() {

        // 检测是否安装
        InstalledAppInfo installedAppInfo = InstallAppManager
                .getInstalledAppInfo(this, downloadData.getPackageName());

        // 未安装的情况
        if (installedAppInfo == null) {
            downloadData.setDownload_type(WandoujiaDownloadConstant.TYPE_NORMAL);

            AppDownloader downloader = AppDownloadService.getDownloaders()
                    .get(downloadData.getTaskId());

            // 如果下载器任务存在, 显示各状态信息
            if (downloader != null) {
                int status = downloader.getStatus();
                current_status = status;
                mDownloadBtnLayout.setVisibility(View.GONE);
                mDownloadProLayout.setVisibility(View.VISIBLE);

                long downloadSize = downloader.getDownloadSize();
                long fileSize = downloader.getFileSize();
                double pre = 0;
                if (fileSize != 0) {
                    pre = (downloadSize * 1.0) / fileSize;
                }
                int progress = (int) (pre * 100);
                if (status == AppDownloader.STATUS_DOWNLOADING) {

                    String test;
                    if (progress == 100) {
                        test = String.format(
                                getResources().getString(
                                        R.string.download_process_download_finish), progress)
                                + getString(R.string.download_process_sign);
                    } else {
                        test = String.format(
                                getResources().getString(
                                        R.string.download_process_tip), progress)
                                + getString(R.string.download_process_sign);
                    }

                    dis_download_text.setText(test);
                    mProgressBar.setProgress(progress);
                } else if (status == AppDownloader.STATUS_PAUSE) {

                    String test = String.format(
                            getResources().getString(
                                    R.string.download_process_pause), progress)
                            + getString(R.string.download_process_sign);
                    dis_download_text.setText(test);
                    mProgressBar.setProgress(progress);

                } else if (status == AppDownloader.STATUS_WAIT) {

                    dis_download_text
                            .setText(getString(R.string.download_process_wait));
                    mProgressBar.setProgress(progress);
                } else if (status == AppDownloader.STATUS_CONNECTING) {

                    dis_download_text
                            .setText(getString(R.string.download_process_connecting));
                    mProgressBar.setProgress(progress);
                } else {

                    String test;
                    if (progress == 100) {
                        test = String.format(
                                getResources().getString(
                                        R.string.download_process_download_finish), progress)
                                + getString(R.string.download_process_sign);
                    } else {
                        test = String.format(
                                getResources().getString(
                                        R.string.download_process_tip), progress)
                                + getString(R.string.download_process_sign);
                    }

                    dis_download_text.setText(test);
                    mProgressBar.setProgress(progress);
                }
            } else { // 任务完成或者没有记录
                AppDownloadData tempData = AppDownloadService.getAppDownloadDao()
                        .getAppDownloadData(downloadData.getTaskId());

                if (null == tempData) {
                    mDownloadProLayout.setVisibility(View.GONE);
                    mDownloadBtnLayout.setVisibility(View.VISIBLE);
                    download_text.setText(R.string.app_download);
                } else {
                    int status = tempData.getStatus();

                    // 规避因为延迟收到安装广播，显示错误的问题（待验证可行性）
                    if (current_status == AppDownloader.STATUS_INSTALLING
                            && status == AppDownloader.STATUS_INSTALLED) {
                        return;
                    }

                    current_status = status;
                    String fileDir = tempData.getFileDir();
                    fileDir = fileDir == null ? "" : fileDir;
                    String fileName = tempData.getFileName();
                    fileName = fileName == null ? "" : fileName;
                    final File file = new File(fileDir, fileName);
                    // 查看数据库中该任务状态是否为完成, 并且文件是存在的
                    if (((status == AppDownloader.STATUS_INSTALLFAILED) || (status == AppDownloader.STATUS_INSTALLED))
                            && file.exists()) {

                        if (status == AppDownloader.STATUS_INSTALLED
                                && ((mDownloadProLayout.getVisibility() == View.VISIBLE) || (mDownloadBtnInstall
                                .getVisibility() == View.VISIBLE))) {
                            return;
                        }
                        if (status == AppDownloader.STATUS_INSTALLFAILED) {
                            mDownloadBtnInstall.setVisibility(View.GONE);
                            mDownloadProLayout.setVisibility(View.GONE);
                            mDownloadBtnLayout.setVisibility(View.VISIBLE);
                            download_text.setText(R.string.app_install);
                        } else {
                            mDownloadBtnLayout.setVisibility(View.VISIBLE);
                            mDownloadProLayout.setVisibility(View.GONE);
                            download_text.setText(R.string.app_install);
                        }
                    } else if (status >= AppDownloader.STATUS_INSTALL_WAIT
                            && file.exists()) {

                        mDownloadProLayout.setVisibility(View.GONE);

                        if (status == AppDownloader.STATUS_INSTALLFAILED) {
                            mDownloadBtnInstall.setVisibility(View.GONE);
                            mDownloadBtnLayout.setVisibility(View.VISIBLE);
                            download_text.setText(R.string.app_install);
                        } else {
                            mDownloadBtnLayout.setVisibility(View.GONE);
                            mDownloadBtnInstall.setVisibility(View.VISIBLE);
                        }


                        animationDrawable = (AnimationDrawable) mDownloadInstallView
                                .getBackground();

                        animationDrawable.start();
                    } else { // 条件不符合则显示下载
                        mDownloadProLayout.setVisibility(View.GONE);
                        mDownloadBtnLayout.setVisibility(View.VISIBLE);
                        download_text.setText(R.string.app_download);
                    }
                }
            }
        } else {
            downloadData.setDownload_type(WandoujiaDownloadConstant.TYPE_UPDATE);

            // 这里判断是否为最新版本
            if ((null != animationDrawable) && (animationDrawable.isRunning()))
                animationDrawable.stop();
            if (downloadData.getVersionCode() > installedAppInfo
                    .getVersionCode()) { // 不是最新版本
                AppDownloader downloader = AppDownloadService.getDownloaders()
                        .get(downloadData.getTaskId());
                // 如果下载器任务存在, 显示各状态信息
                if (downloader != null) {
                    mDownloadBtnInstall.setVisibility(View.GONE);
                    mDownloadBtnLayout.setVisibility(View.GONE);
                    mDownloadProLayout.setVisibility(View.VISIBLE);
                    int status = downloader.getStatus();
                    current_status = status;
                    long downloadSize = downloader.getDownloadSize();
                    long fileSize = downloader.getFileSize();
                    double pre = 0;
                    if (fileSize != 0) {
                        pre = (downloadSize * 1.0) / fileSize;
                    }
                    int progress = (int) (pre * 100);
                    if (status == AppDownloader.STATUS_DOWNLOADING) {

                        String test = String.format(
                                getResources().getString(
                                        R.string.download_process_tip),
                                progress)
                                + getString(R.string.download_process_sign);
                        dis_download_text.setText(test);
                        mProgressBar.setProgress(progress);
                    } else if (status == AppDownloader.STATUS_PAUSE) {

                        String test = String.format(
                                getResources().getString(
                                        R.string.download_process_pause), progress)
                                + getString(R.string.download_process_sign);
                        dis_download_text.setText(test);
                        mProgressBar.setProgress(progress);

                    } else {
                        String test = String.format(
                                getResources().getString(
                                        R.string.download_process_pause),
                                progress)
                                + getString(R.string.download_process_sign);
                        dis_download_text.setText(test);
                        mProgressBar.setProgress(progress);
                    }
                } else { // 任务完成或者没有记录
                    AppDownloadData tempData = AppDownloadService
                            .getAppDownloadDao().getAppDownloadData(
                                    downloadData.getTaskId());
                    if (tempData == null) {
                        if ((null != animationDrawable) && (animationDrawable.isRunning()))
                            animationDrawable.stop();
                        mDownloadBtnInstall.setVisibility(View.GONE);
                        mDownloadBtnLayout.setVisibility(View.VISIBLE);
                        mDownloadProLayout.setVisibility(View.GONE);
                        download_text.setText(R.string.download_process_update);
                    } else {
                        int status = tempData.getStatus();

                        current_status = status;
                        String fileDir = tempData.getFileDir();
                        fileDir = fileDir == null ? "" : fileDir;
                        String fileName = tempData.getFileName();
                        fileName = fileName == null ? "" : fileName;
                        final File file = new File(fileDir, fileName);
                        // 查看数据库中该任务状态是否为完成, 并且文件是存在的

                        if (tempData.getVersionCode() == downloadData
                                .getVersionCode()) {

                            if (status == AppDownloader.STATUS_INSTALLING) { // 安装中
                                mDownloadProLayout.setVisibility(View.GONE);
                                mDownloadBtnLayout.setVisibility(View.GONE);
                                mDownloadBtnInstall.setVisibility(View.VISIBLE);
                                animationDrawable = (AnimationDrawable) mDownloadInstallView
                                        .getBackground();

                                animationDrawable.start();
                            } else if (((status == AppDownloader.STATUS_INSTALLFAILED) || (status == AppDownloader.STATUS_INSTALLED))
                                    && file.exists()) {

                                if (status == AppDownloader.STATUS_INSTALLED
                                        && ((mDownloadProLayout.getVisibility() == View.VISIBLE) || (mDownloadBtnInstall
                                        .getVisibility() == View.VISIBLE))) {
                                    return;
                                }

                                if ((null != animationDrawable) && (animationDrawable.isRunning()))
                                    animationDrawable.stop();
                                mDownloadBtnInstall.setVisibility(View.GONE);
                                mDownloadBtnLayout.setVisibility(View.VISIBLE);
                                mDownloadProLayout.setVisibility(View.GONE);
                                download_text.setText(R.string.app_install);
                            } else { // 条件不符合则显示下载
                                if (status >= AppDownloader.STATUS_INSTALL_WAIT
                                        && (mDownloadProLayout.getVisibility() == View.VISIBLE)) {
                                    return;
                                }
                                if ((null != animationDrawable) && (animationDrawable.isRunning()))
                                    animationDrawable.stop();
                                mDownloadBtnInstall.setVisibility(View.GONE);
                                mDownloadBtnLayout.setVisibility(View.VISIBLE);
                                mDownloadProLayout.setVisibility(View.GONE);
                                download_text
                                        .setText(R.string.download_process_update);
                            }
                        } else {
                            if (status >= AppDownloader.STATUS_INSTALL_WAIT
                                    && (mDownloadProLayout.getVisibility() == View.VISIBLE)) {
                                return;
                            }
                            mDownloadBtnLayout.setVisibility(View.VISIBLE);
                            mDownloadProLayout.setVisibility(View.GONE);
                            download_text
                                    .setText(R.string.download_process_update);

                        }
                    }
                }

            } else { // 如果是最新版本
                if ((null != animationDrawable)
                        && (animationDrawable.isRunning()))
                    animationDrawable.stop();
                mDownloadBtnInstall.setVisibility(View.GONE);
                mDownloadBtnLayout.setVisibility(View.VISIBLE);
                mDownloadProLayout.setVisibility(View.GONE);
                download_text.setText(R.string.item_open);


                Animation anim = AnimationUtils.loadAnimation(
                        AppDetailActivity.this, R.anim.scale1);

                anim.setFillAfter(true);
                Animation anim1 = AnimationUtils.loadAnimation(
                        AppDetailActivity.this, R.anim.scale2);
                anim1.setFillAfter(true);
                anim.setAnimationListener(null);


                if ((current_status == AppDownloader.STATUS_INSTALLING)
                        && isOpenAnimal) {
                    mDownloadBtn.startAnimation(anim);

                    download_text.startAnimation(anim1);

                    isOpenAnimal = false;
                }
                current_status = downloadData.getStatus();
            }
        }

    }

    public void setAnimal1() {

        mProgressBar.setProgress(0);

        TranslateAnimation animation = new TranslateAnimation(0, 0,
                -dip2px(AppDetailActivity.this, 5), 0);

        AlphaAnimation animation2 = new AlphaAnimation(0, 1.0f);

        AnimationSet set = new AnimationSet(true);
        set.setDuration(350);
        set.setInterpolator(new DecelerateInterpolator());
        set.addAnimation(animation);
        set.addAnimation(animation2);

        dis_download_text.setText(getString(R.string.download_process_wait));
        dis_download_text.startAnimation(set);

    }

    private void doTheDownOpr() {
        CustomAnimation animation = new CustomAnimation(
                new CustomAnimCallBack() {
                    int isStartAnimal = 0;
                    int isEndAnimal = 0;

                    @Override
                    public void callBack(float interpolatedTime,
                                         Transformation t) {
                        if (isEndAnimal == 1)
                            return;

                        mDownloadBtn.setScaleX(1.0f - 0.239f * interpolatedTime);
                        mDownloadBtn.getBackground().setAlpha(
                                (int) (255 * (1.0f - interpolatedTime)));
                        mDownloadBtn.setScaleY(1.0f - 0.9f * (interpolatedTime));
                        download_text
                                .setTextSize(18 * (1 - 0.4f * (interpolatedTime)));

                        if (interpolatedTime == 1.0f) {
                            isEndAnimal = 1;
                            mDownloadBtnLayout.setVisibility(View.GONE);
                            mHandler.sendEmptyMessageDelayed(0, 200);
                            mDownloadProLayout.setVisibility(View.VISIBLE);

                            Animation anim1 = AnimationUtils.loadAnimation(
                                    AppDetailActivity.this, R.anim.alpha);
                            anim1.setInterpolator(new DecelerateInterpolator());
                            mCancelDownloadBtn.startAnimation(anim1);
                            mToDownloadManagerBtn.startAnimation(anim1);
                            setAnimal1();

                            AppDownloadService.startDownload(
                                    AppDetailActivity.this, downloadData);
                        }

                    }
                });

        animation.setDuration(350);
        animation.setFillAfter(true);
        animation.setInterpolator(new DecelerateInterpolator());

        mDownloadBtn.startAnimation(animation);
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:

                    mDownloadBtn.getBackground().setAlpha(255);
                    mDownloadBtn.setScaleY(1.0f);
                    mDownloadBtn.setScaleX(1.0f);
                    download_text.setTextSize(18);
                    break;
                default:
                    break;
            }
        }
    };

    private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
        @Override
        public void downloadProgressUpdate() {
            if (downloadData != null) {
                progressBtnUtil.updateProgressBtn(progressBtn, downloadData);
                checkSoftwareState();

                // 更新右上角显示
                if (AppDownloadService.getDownloadingCountMore() > 0 ||
                        SettingUtil.getLastUpdateAppCount(AppDetailActivity.this) > 0) {
                    mToolbar.getMenu().findItem(R.id.menu_setting)
                            .setIcon(R.drawable.toolbar_setting_message_normal);
                } else {
                    mToolbar.getMenu().findItem(R.id.menu_setting)
                            .setIcon(R.drawable.toolbar_setting_normal);
                }
            }
        }
    };

}
