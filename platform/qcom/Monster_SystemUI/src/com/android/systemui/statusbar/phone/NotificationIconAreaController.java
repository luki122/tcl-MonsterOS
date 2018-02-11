package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.util.NotificationColorUtil;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.NotificationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A controller for the space in the status bar to the left of the system icons. This area is
 * normally reserved for notifications.
 */
public class NotificationIconAreaController {
    private final NotificationColorUtil mNotificationColorUtil;

    private int mIconSize;
    private int mIconHPadding;
    private int mIconTint = Color.WHITE;

    private PhoneStatusBar mPhoneStatusBar;
    protected View mNotificationIconArea;
    private IconMerger mNotificationIcons;
    private ImageView mMoreIcon,mMoreNotify;
    private final Rect mTintArea = new Rect();
    //add by chenhl start
    private TextView mClock;
    //add by chenhl end

    public NotificationIconAreaController(Context context, PhoneStatusBar phoneStatusBar) {
        mPhoneStatusBar = phoneStatusBar;
        mNotificationColorUtil = NotificationColorUtil.getInstance(context);

        initializeNotificationAreaViews(context);
    }

    protected View inflateIconArea(LayoutInflater inflater) {
        return inflater.inflate(R.layout.notification_icon_area, null);
    }

    /**
     * Initializes the views that will represent the notification area.
     */
    protected void initializeNotificationAreaViews(Context context) {
        reloadDimens(context);

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        mNotificationIconArea = inflateIconArea(layoutInflater);

        mNotificationIcons =
                (IconMerger) mNotificationIconArea.findViewById(R.id.notificationIcons);

        mMoreIcon = (ImageView) mNotificationIconArea.findViewById(R.id.moreIcon);
        if (mMoreIcon != null) {
            mMoreIcon.setImageTintList(ColorStateList.valueOf(mIconTint));
            mNotificationIcons.setOverflowIndicator(mMoreIcon);
        }
        //add by chenhl start
        mMoreNotify = (ImageView) mNotificationIconArea.findViewById(R.id.tcl_more_notify);
        if(mMoreNotify!=null){
            mMoreNotify.setImageTintList(ColorStateList.valueOf(StatusBarIconController.getTint(mTintArea, mMoreNotify, mIconTint)));
        }
        mClock = (TextView) mNotificationIconArea.findViewById(R.id.clock);
        //add by chenhl end
    }

    public void onDensityOrFontScaleChanged(Context context) {
        reloadDimens(context);
        final LinearLayout.LayoutParams params = generateIconLayoutParams();
        for (int i = 0; i < mNotificationIcons.getChildCount(); i++) {
            View child = mNotificationIcons.getChildAt(i);
            child.setLayoutParams(params);
        }
    }

    @NonNull
    private LinearLayout.LayoutParams generateIconLayoutParams() {
        //modify by chenhl start
        /*return new LinearLayout.LayoutParams(
                mIconSize + 2 * mIconHPadding, getHeight());*/
        return new LinearLayout.LayoutParams(
                mIconSize + 2 * mIconHPadding, getHeight());
        //modify by chenhl end
    }

    private void reloadDimens(Context context) {
        Resources res = context.getResources();
        //modify by chenhl start
        //mIconSize = res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_icon_size);
        mIconSize = res.getDimensionPixelSize(R.dimen.tcl_status_bar_icon_size);
        mIconHPadding = res.getDimensionPixelSize(R.dimen.status_bar_icon_padding);
        //modify by chenhl end
    }

    /**
     * Returns the view that represents the notification area.
     */
    public View getNotificationInnerAreaView() {
        return mNotificationIconArea;
    }

    /**
     * See {@link StatusBarIconController#setIconsDarkArea}.
     *
     * @param tintArea the area in which to tint the icons, specified in screen coordinates
     */
    public void setTintArea(Rect tintArea) {
        if (tintArea == null) {
            mTintArea.setEmpty();
        } else {
            mTintArea.set(tintArea);
        }
        //add by chenhl start
        if(mMoreNotify!=null){
            mMoreNotify.setImageTintList(ColorStateList.valueOf(StatusBarIconController.getTint(mTintArea, mMoreNotify, mIconTint)));
        }
        //add by chenhl end
        applyNotificationIconsTint();
    }

    /**
     * Sets the color that should be used to tint any icons in the notification area. If this
     * method is not called, the default tint is {@link Color#WHITE}.
     */
    public void setIconTint(int iconTint) {
        mIconTint = iconTint;
        if (mMoreIcon != null) {
            mMoreIcon.setImageTintList(ColorStateList.valueOf(StatusBarIconController.getTint(mTintArea, mMoreIcon, mIconTint)));
        }
        //add by chenhl start
        if(mMoreNotify!=null){
            mMoreNotify.setImageTintList(ColorStateList.valueOf(StatusBarIconController.getTint(mTintArea, mMoreNotify, mIconTint)));
        }
        //add by chenhl end
        applyNotificationIconsTint();
    }

    protected int getHeight() {
        return mPhoneStatusBar.getStatusBarHeight();
    }

    protected boolean shouldShowNotification(NotificationData.Entry entry,
            NotificationData notificationData) {
        if (notificationData.isAmbient(entry.key)
                && !NotificationData.showNotificationEvenIfUnprovisioned(entry.notification)) {
            return false;
        }
        if (!PhoneStatusBar.isTopLevelChild(entry)) {
            return false;
        }
        if (entry.row.getVisibility() == View.GONE) {
            return false;
        }

        return true;
    }

    /**
     * Updates the notifications with the given list of notifications to display.
     */
    public void updateNotificationIcons(NotificationData notificationData) {
        final LinearLayout.LayoutParams params = generateIconLayoutParams();

        ArrayList<NotificationData.Entry> activeNotifications =
                notificationData.getActiveNotifications();
        final int size = activeNotifications.size();
        ArrayList<StatusBarIconView> toShow = new ArrayList<>(size);
        ArrayList<StatusBarIconView> toCover = new ArrayList<>();//add by chenhl

        // Filter out ambient notifications and notification children.
        for (int i = 0; i < size; i++) {
            NotificationData.Entry ent = activeNotifications.get(i);
            if (shouldShowNotification(ent, notificationData)) {
                //add by chenhl start for statusbar count
                if(isNeedcover(ent.notification)){
                    if(ent.notification.isClearable()){
                        toCover.add(ent.icon);
                    }
                    continue;
                }
                //add by chenhl end

                toShow.add(ent.icon);
            }
        }

        ArrayList<View> toRemove = new ArrayList<>();
        for (int i = 0; i < mNotificationIcons.getChildCount(); i++) {
            View child = mNotificationIcons.getChildAt(i);
            if (!toShow.contains(child)) {
                toRemove.add(child);
            }
        }

        final int toRemoveCount = toRemove.size();
        for (int i = 0; i < toRemoveCount; i++) {
            mNotificationIcons.removeView(toRemove.get(i));
        }

        for (int i = 0; i < toShow.size(); i++) {
            View v = toShow.get(i);
            if (v.getParent() == null) {
                mNotificationIcons.addView(v, i, params);
            }
        }

        // Re-sort notification icons
        final int childCount = mNotificationIcons.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View actual = mNotificationIcons.getChildAt(i);
            StatusBarIconView expected = toShow.get(i);
            if (actual == expected) {
                continue;
            }
            mNotificationIcons.removeView(expected);
            mNotificationIcons.addView(expected, i);
        }

        //add by chenhl start for statusbar count
        updateOverflow(toCover.size(),mMoreNotify);
        //add by chenhl end

        applyNotificationIconsTint();
    }

    /**
     * Applies {@link #mIconTint} to the notification icons.
     */
    private void applyNotificationIconsTint() {
        for (int i = 0; i < mNotificationIcons.getChildCount(); i++) {
            StatusBarIconView v = (StatusBarIconView) mNotificationIcons.getChildAt(i);
            boolean isPreL = Boolean.TRUE.equals(v.getTag(R.id.icon_is_pre_L));
            boolean colorize = !isPreL || NotificationUtils.isGrayscale(v, mNotificationColorUtil);
            if (colorize) {
                v.setImageTintList(ColorStateList.valueOf(
                        StatusBarIconController.getTint(mTintArea, v, mIconTint)));
            }
        }
    }

    //add by chenhl start 2016.7.1 for statusbar count
    private String[] specilApp = new String[]{
            //"com.tencent.mobileqq"
    };
    private String[] systemApp = new String[]{
            "android",
            "com.android.systemui",
            "com.android.providers.downloads",
            "com.android.settings",
            "com.monster.privacymanage",
            "com.android.bluetooth",
            //"com.android.server.telecom"
    };
    private List<String> specilList = Arrays.asList(specilApp);
    private List<String> systemList =Arrays.asList(systemApp);

    private boolean isSpecilThridIcon(String pkgName){

        if(specilList.contains(pkgName)){
            return true;
        }
        return false;
    }

    private boolean isSystemIcon(String pkgName){
        if(systemList.contains(pkgName)){
            return true;
        }
        return false;
    }

    private boolean isNeedcover(StatusBarNotification notification){
        String pkgName = notification.getPackageName();
        if(isSpecilThridIcon(pkgName)||isSystemIcon(pkgName)){
            return false;
        }
        return true;
    }

    private static final int MAXCOUNT=99;
    public void updateOverflow(final int count,final ImageView moreview){
        moreview.post(new Runnable() {
            @Override
            public void run() {
                moreview.setVisibility(count>0 ? View.VISIBLE : View.GONE);
                //moreview.setImageBitmap(createBitmap(count));
            }
        });
    }

    /**
     * create ower bitmap
     */
    private Bitmap mBitmap;
    private Bitmap createBitmap(int count) {
        if(mBitmap!=null&&!mBitmap.isRecycled()){
            mBitmap.recycle();
            mBitmap = null;
        }
        mBitmap = Bitmap.createBitmap(31, 31, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(24f);
        paint.setAntiAlias(true);

        Paint circlePaint = new Paint();
        circlePaint.setColor(Color.BLACK);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(2.5f);
        circlePaint.setAntiAlias(true);

        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        if(count>MAXCOUNT){
            float delt = w/4;
            canvas.drawPoint(delt, h/2, circlePaint);
            canvas.drawPoint(delt*2, h/2, circlePaint);
            canvas.drawPoint(delt*3, h/2, circlePaint);
        }else{
            float textw = paint.measureText(""+count);
            Paint.FontMetrics fm = paint.getFontMetrics();
            float textTop = (h - 0 - fm.bottom - fm.top) / 2;
            canvas.drawText(""+count, w / 2 - textw / 2, textTop, paint);
        }
        canvas.drawCircle(w / 2, h / 2, w / 2-1.5f, circlePaint);
        return mBitmap;
    }

    public TextView getClock(){
        return mClock;
    }
    //add by chenhl end
}
