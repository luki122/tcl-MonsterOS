package com.monster.launcher.unread;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.widget.TextView;

import com.monster.launcher.AppInfo;
import com.monster.launcher.DeviceProfile;
import com.monster.launcher.FolderInfo;
import com.monster.launcher.ItemInfo;
import com.monster.launcher.Launcher;
import com.monster.launcher.LauncherModel;
import com.monster.launcher.LauncherSettings;
import com.monster.launcher.Log;
import com.monster.launcher.R;
import com.monster.launcher.ShortcutInfo;
import com.monster.launcher.Workspace;
import com.monster.launcher.compat.UserHandleCompat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * M: This class is a util class, implemented to do the following two things,:
 *
 * 1.Read config xml to get the shortcuts which support displaying unread number,
 * then get the initial value of the unread number of each component and update
 * shortcuts and folders through callbacks implemented in Launcher.
 *
 * 2. Receive unread broadcast sent by application, update shortcuts and folders in
 * workspace, hot seat and update application icons in app customize paged view.
 */
public class MonsterUnreadLoader extends BroadcastReceiver {
    public static final String TAG = "MonsterUnreadLoader";
    public static final String DEBUG_TAG_LAUNCHER = "MonsterUnreadLoader.launcher";
    private static final String TAG_UNREADSHORTCUTS = "unreadshortcuts";
    public static final String UNREAD_SETTINGS_PREFERENCE = "unread_settings";
    public static final String UNREAD_SETTINGS_AllLIST_PREFERENCE = "unread_settings_alllist";
    public static final String UNREAD_SETTINGS_APPS = "unread_apps";
    public static final int UNREAD_NUMBER_ADD = Integer.MAX_VALUE;
    public static final int UNREAD_NUMBER_REMOVE = Integer.MIN_VALUE;
    public static final int UNREAD_NUMBER_REMOVE_WITHKEY = -100;

    public static final String PHONE_PACKAGE_NAME = "com.android.dialer";
    public static final String PHONE_CLASS_NAME = "com.android.dialer.DialtactsActivity";
    public static final String MMS_PACKAGE_NAME = "com.android.mms";
    public static final String MMS_CLASS_NAME = "com.android.mms.ui.ConversationList";
    public static int UNREAD_TYPE_NORMAL = 0;
    public static int UNREAD_TYPE_PHONE = 1;
    public static int UNREAD_TYPE_MMS = 2;
    public static int UNREAD_TYPE_EMAIL = 3;
    public static int UNREAD_TYPE_CALENDAR = 4;

    public static final ArrayList<UnreadSupportShortcut> UNREAD_All_SHORTCUTS =
            new ArrayList<UnreadSupportShortcut>();
    public static final ArrayList<UnreadSupportShortcut> UNREAD_SUPPORT_SHORTCUTS =
            new ArrayList<UnreadSupportShortcut>();



    private static int sUnreadSupportShortcutsNum = 0;
    private static final Object LOG_LOCK = new Object();//to lock UNREAD_SUPPORT_SHORTCUTS
    private Context mContext;
    LauncherModel mLauncherModel;

    private WeakReference<UnreadCallbacks> mCallbacks;

    public static ArrayList<UnreadUpdateListener> sUnreadUpdateListeners = new ArrayList<UnreadUpdateListener>();
    public interface UnreadUpdateListener{
        void onUnreadPackageChange();
    }
    public static void addListener(UnreadUpdateListener l){
        if(!sUnreadUpdateListeners.contains(l)){
            sUnreadUpdateListeners.add(l);
        }
    }

    public MonsterUnreadLoader(Context context, LauncherModel launcherModel) {
        mContext = context;
        mLauncherModel = launcherModel;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (Launcher.INTENT_ACTION_UNREAD_CHANGE.equals(action)) {
            ComponentName componentName = (ComponentName) intent.getExtras().get(Launcher.EXTRA_UNREAD_COMPONENT);
            int unreadNum = intent.getIntExtra(Launcher.EXTRA_UNREAD_NUMBER, -1);
            UserHandle user = (UserHandle)intent.getExtras().get(Launcher.EXTRA_UNREAD_USER);
            if (mCallbacks != null && componentName != null) {
                synchronized (LOG_LOCK) {
                    final int index = supportUnreadFeature(componentName, user);
                    Log.e(TAG, "onReceive INTENT_ACTION_UNREAD_CHANGE packageName:" + componentName.getPackageName()
                            + ", unreadNum:" + unreadNum + ", user:" + user + ", index:" + index);
                    if (index >= 0) {
                        if (unreadNum == UNREAD_NUMBER_ADD) {
                        unreadNum = UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum + 1;
//                            unreadNum = 1;
                        } else if (unreadNum == UNREAD_NUMBER_REMOVE) {
//                        unreadNum = UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum - 1;
                            unreadNum = 0;
                        } else if (unreadNum == UNREAD_NUMBER_REMOVE_WITHKEY) {
                            int u = intent.getIntExtra(Launcher.EXTRA_UNREAD_NUMBER_REMOVE, 0);
//                        unreadNum = UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum - u;
                            unreadNum = 0;
                        }
                        componentName = UNREAD_SUPPORT_SHORTCUTS.get(index).component;
                        boolean ret = setUnreadNumberAt(index, unreadNum);
                        boolean needRefresh = ((unreadNum == 0 && UNREAD_SUPPORT_SHORTCUTS.get(index).hadDraw)||(unreadNum > 0 && !UNREAD_SUPPORT_SHORTCUTS.get(index).hadDraw));
                        Log.e(TAG, "onReceive INTENT_ACTION_UNREAD_CHANGE reset : " +ret);
                        if (ret || needRefresh) {
                            final UnreadCallbacks callbacks = mCallbacks.get();
                            if (callbacks != null) {
                                callbacks.bindComponentUnreadChanged(componentName, unreadNum, user);
                            }
                        }
                    }
                }
            }
        }else if (Launcher.INTENT_ACTION_UNREAD_SETTING_CHANGE.equals(action)) {
            Log.d(TAG,"MonsterUnreadLoader onReceive unread settings changed ");
            unReadSettingChanged(context);
            if (mCallbacks != null) {
                UnreadCallbacks callbacks = mCallbacks.get();
                if (callbacks != null) {
                    callbacks.bindUnreadInfoIfNeeded();
                }
            }
        }
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(UnreadCallbacks callbacks) {
        mCallbacks = new WeakReference<UnreadCallbacks>(callbacks);
        if (Log.DEBUG_UNREAD) {
            Log.d(TAG, "initialize: callbacks = " + callbacks
                    + ", mCallbacks = " + mCallbacks);
        }
    }

    /**
     * Load and initialize unread shortcuts.
     *
     */
    public void loadAndInitUnreadShortcuts() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... unused) {
                loadUnreadSupportShortcuts();
                //这里要去重新获取每个应用unread数
                return null;
            }

            @Override
            protected void onPostExecute(final Void result) {
                if (mCallbacks != null) {
                    UnreadCallbacks callbacks = mCallbacks.get();
                    if (callbacks != null) {
                        callbacks.bindUnreadInfoIfNeeded();
                    }
                }
            }
        }.execute();
    }

    /**
     * 初始化，从xml中加载默认的未读角标列表
     */
    private void loadUnreadSupportShortcuts() {
        long start = System.currentTimeMillis();
        if (Log.DEBUG_UNREAD) {
            Log.d(TAG, "loadUnreadSupportShortcuts begin: start = " + start);
        }

        // Clear all previous parsed unread shortcuts.
        synchronized (LOG_LOCK) {
            UNREAD_SUPPORT_SHORTCUTS.clear();
            sUnreadSupportShortcutsNum = 0;
            try {
                XmlResourceParser parser = mContext.getResources().getXml(
                        R.xml.unread_support_shortcuts);
                AttributeSet attrs = Xml.asAttributeSet(parser);
                beginDocument(parser, TAG_UNREADSHORTCUTS);

                final int depth = parser.getDepth();

                int type = -1;
                while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                        && type != XmlPullParser.END_DOCUMENT) {

                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }

                    TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.UnreadShortcut);
                    synchronized (LOG_LOCK) {
                        //外发时需要
                        Bitmap icon = null;
//                    Bitmap icon = Utilities.getAppIcon(mContext,a.getString(R.styleable.UnreadShortcut_unreadPackageName), UserHandleCompat.myUserHandle());
//                    if(icon == null){
//                        icon = PhotoUtils.drawable2bitmap(mContext.getResources().getDrawable(android.R.drawable.sym_def_app_icon));
//                    }
                        addToAllList(new UnreadSupportShortcut(a.getString(R.styleable.UnreadShortcut_unreadPackageName),
                                a.getString(R.styleable.UnreadShortcut_unreadClassName),
                                icon,
                                a.getInt(R.styleable.UnreadShortcut_unreadType, 0), UserHandleCompat.myUserHandle().getUser()));
                    }
                    a.recycle();

                }
            } catch (XmlPullParserException e) {
            } catch (IOException e) {
            }
        }
        if (mLauncherModel != null) {
            mLauncherModel.updateUnread();
        }
        if (Log.DEBUG_UNREAD) {
            Log.d(TAG, "loadUnreadSupportShortcuts end: time used = "
                    + (System.currentTimeMillis() - start) + ",sUnreadSupportShortcutsNum = "
                    + sUnreadSupportShortcutsNum + getUnreadSupportShortcutInfo());
        }
    }

    public static void addToAllList(UnreadSupportShortcut us) {
        final UnreadSupportShortcut uss = getUnreadSupportShortcut(us);
        synchronized (LOG_LOCK) {
            if (uss == null) {
                UNREAD_All_SHORTCUTS.add(us);
            } else {
                us.mUnreadNum = uss.mUnreadNum;
                us.icon = uss.icon;
                UNREAD_All_SHORTCUTS.remove(uss);
                UNREAD_All_SHORTCUTS.add(us);
            }
        }
    }

    public static void addToSupport(UnreadSupportShortcut us) {
        final UnreadSupportShortcut uss = getUnreadSupportShortcut(us);
        synchronized (LOG_LOCK) {
            if (uss == null) {
                UNREAD_SUPPORT_SHORTCUTS.add(us);
            } else {
                us.mUnreadNum = uss.mUnreadNum;
                us.icon = uss.icon;
                UNREAD_SUPPORT_SHORTCUTS.remove(uss);
                UNREAD_SUPPORT_SHORTCUTS.add(us);
            }
            sUnreadSupportShortcutsNum = UNREAD_SUPPORT_SHORTCUTS.size();
        }

    }

    public static void unReadSettingChanged(Context context){
        synchronized (LOG_LOCK) {
            UNREAD_SUPPORT_SHORTCUTS.clear();
            for (UnreadSupportShortcut info : UNREAD_All_SHORTCUTS) {
                if (surportUnreadForTCL(context, info.component.getPackageName())) {
                    addToSupport(info);
                }
            }
        }
        //外发时需要
//        restoreUnreadSupportSettings(context);
    }

//    public static void remove(UnreadSupportShortcut us){
//        final UnreadSupportShortcut uss = getUnreadSupportShortcut(us);
//        synchronized (LOG_LOCK) {
//            if (uss != null) {
//                UNREAD_SUPPORT_SHORTCUTS.remove(uss);
//            }
//        }
//        sUnreadSupportShortcutsNum = UNREAD_SUPPORT_SHORTCUTS.size();
//    }

    public static void updateAllUnreadShortcuts(ArrayList<UnreadSupportShortcut> unreadSupport ,Context context) {
        synchronized (LOG_LOCK) {
            UNREAD_All_SHORTCUTS.clear();
            UNREAD_SUPPORT_SHORTCUTS.clear();
            for (UnreadSupportShortcut info : unreadSupport) {
                addToAllList(info);
                if(surportUnreadForTCL(context,info.component.getPackageName())){
                    addToSupport(info);
                }
            }
        }
        //外发时需要
//        restoreUnreadAllListSettings(context);
//        restoreUnreadSupportSettings(context);
//        if(sUnreadUpdateListeners !=null){
//            for (UnreadUpdateListener updateListener : sUnreadUpdateListeners){
//                updateListener.onUnreadPackageChange();
//            }
//        }
    }

    /**
     * Get unread support shortcut information, since the information are stored
     * in an array list, we may query it and modify it at the same time, a lock
     * is needed.
     *
     * @return
     */
    private static String getUnreadSupportShortcutInfo() {
        String info = " Unread support shortcuts are ";
        synchronized (LOG_LOCK) {
            info += UNREAD_SUPPORT_SHORTCUTS.toString();
        }
        return info;
    }

    /**
     * Whether the given component support unread feature.
     *
     * @param component
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static int supportUnreadFeature(ComponentName component, UserHandle user) {
        if (Log.DEBUG_UNREAD) {
            Log.d(TAG, "supportUnreadFeature: component = " + component);
        }
        if (component == null) {
            return -1;
        }
        synchronized (LOG_LOCK) {
            final int size = UNREAD_SUPPORT_SHORTCUTS.size();
            for (int i = 0, sz = size; i < sz; i++) {
                UnreadSupportShortcut uss = UNREAD_SUPPORT_SHORTCUTS.get(i);
                if (uss != null && uss.component != null && uss.component.getPackageName().equals(component.getPackageName())) {
                    if (user == null) {
                        return i;
                    } else if (user.equals(uss.user)) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static int supportUnreadFeature(String pkgName, String clsName, UserHandle user) {
        if (Log.DEBUG_UNREAD) {
            Log.d(TAG, "supportUnreadFeature pkg/cls = " + pkgName + "/" + clsName);
        }
        if (clsName == null || pkgName == null) {
            return -1;
        }
        synchronized (LOG_LOCK) {
            final int size = UNREAD_SUPPORT_SHORTCUTS.size();
            for (int i = 0, sz = size; i < sz; i++) {
                UnreadSupportShortcut uss = UNREAD_SUPPORT_SHORTCUTS.get(i);
                if (uss != null && uss.component != null && uss.component.getPackageName().equals(pkgName) && uss.component.getClassName().equals(pkgName)) {
                    if (user == null) {
                        return i;
                    } else if (user.equals(uss.user)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static UnreadSupportShortcut getUnreadSupportShortcut(UnreadSupportShortcut us) {
        if (Log.DEBUG_UNREAD) {
            Log.d(TAG, "supportUnreadFeature: component = " + us);
        }
        if (us == null || us.component == null) {
            return null;
        }
        synchronized (LOG_LOCK) {
            final int size = UNREAD_SUPPORT_SHORTCUTS.size();
            for (int i = 0, sz = size; i < sz; i++) {
                UnreadSupportShortcut uss = UNREAD_SUPPORT_SHORTCUTS.get(i);
                boolean isCurrentUser = us.user.equals(uss.user);
                if (uss != null && uss.component != null && uss.component.equals(us.component) && isCurrentUser) {
                    return UNREAD_SUPPORT_SHORTCUTS.get(i);
                }
            }
        }

        return null;
    }

    /**
     * Set the unread number of the item in the list with the given unread number.
     *
     * @param index
     * @param unreadNum
     * @return
     */
    static boolean setUnreadNumberAt(int index, int unreadNum) {
        if (index >= 0 || index < sUnreadSupportShortcutsNum) {
            if (Log.DEBUG_UNREAD) {
                Log.d(TAG, "setUnreadNumberAt: index = " + index
                        + ",unreadNum = " + unreadNum + getUnreadSupportShortcutInfo());
            }
            synchronized (LOG_LOCK) {
                if (unreadNum == -1) {
                    UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum++;
                } else {
                    if (UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum != unreadNum) {
                        UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum = unreadNum;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get unread number of application at the given position in the supported
     * shortcut list.
     *
     * @param index
     * @return
     */
    static synchronized int getUnreadNumberAt(int index) {
        if (index < 0 || index >= sUnreadSupportShortcutsNum) {
            return 0;
        }
        if (Log.DEBUG_UNREAD) {
            Log.d(TAG, "getUnreadNumberAt: index = " + index
                    + getUnreadSupportShortcutInfo());
        }
        synchronized (LOG_LOCK) {
            return UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum;
        }
    }

    /**
     * Get unread number for the given component.
     *
     * @param component
     * @return
     */
    public static int getUnreadNumberOfComponent(ComponentName component,UserHandle user) {
        final int index = supportUnreadFeature(component,user);
        return getUnreadNumberAt(index);
    }

    /**
     * Draw unread number for the given icon.
     *
     * @param canvas
     * @param icon
     * @return
     */
//    public static void drawUnreadEventIfNeed(Canvas canvas, View icon) {
//        if(!Launcher.isUnreadEnable)return;
//
//        ItemInfo info = (ItemInfo) icon.getTag();
//        if (info != null && info.unreadNum > 0) {
//            Resources res = icon.getContext().getResources();
//
//            /// M: Meature sufficent width for unread text and background image
//            Paint unreadTextNumberPaint = new Paint();
//            unreadTextNumberPaint.setTextSize(res.getDimension(R.dimen.unread_text_number_size));
//            unreadTextNumberPaint.setTypeface(Typeface.DEFAULT_BOLD);
//            unreadTextNumberPaint.setColor(0xffffffff);
//            unreadTextNumberPaint.setTextAlign(Paint.Align.CENTER);
//
//            Paint unreadTextPlusPaint = new Paint(unreadTextNumberPaint);
//            unreadTextPlusPaint.setTextSize(res.getDimension(R.dimen.unread_text_plus_size));
//
//            String unreadTextNumber;
//            String unreadTextPlus = "+";
//            Rect unreadTextNumberBounds = new Rect(0, 0, 0, 0);
//            Rect unreadTextPlusBounds = new Rect(0, 0, 0, 0);
//            if (info.unreadNum > Launcher.MAX_UNREAD_COUNT) {
//                unreadTextNumber = String.valueOf(Launcher.MAX_UNREAD_COUNT);
//                unreadTextPlusPaint.getTextBounds(unreadTextPlus, 0,
//                        unreadTextPlus.length(), unreadTextPlusBounds);
//            } else {
//                unreadTextNumber = String.valueOf(info.unreadNum);
//            }
//            unreadTextNumberPaint.getTextBounds(unreadTextNumber, 0,
//                    unreadTextNumber.length(), unreadTextNumberBounds);
//            int textHeight = unreadTextNumberBounds.height();
//            int textWidth = unreadTextNumberBounds.width() + unreadTextPlusBounds.width();
//
//            /// M: Draw unread background image.
//            NinePatchDrawable unreadBgNinePatchDrawable =
//                    (NinePatchDrawable) res.getDrawable(R.drawable.ic_newevents_numberindication);
//            int unreadBgWidth = unreadBgNinePatchDrawable.getIntrinsicWidth();
//            int unreadBgHeight = unreadBgNinePatchDrawable.getIntrinsicHeight();
//
//            int unreadMinWidth = (int) res.getDimension(R.dimen.unread_minWidth);
//            if (unreadBgWidth < unreadMinWidth) {
//                unreadBgWidth = unreadMinWidth;
//            }
//            int unreadTextMargin = (int) res.getDimension(R.dimen.unread_text_margin);
//            if (unreadBgWidth < textWidth + unreadTextMargin) {
//                unreadBgWidth = textWidth + unreadTextMargin;
//            }
//            if (unreadBgHeight < textHeight) {
//                unreadBgHeight = textHeight;
//            }
//            Rect unreadBgBounds = new Rect(0, 0, unreadBgWidth, unreadBgHeight);
//            unreadBgNinePatchDrawable.setBounds(unreadBgBounds);
//
//            int unreadMarginTop = 0;
//            int unreadMarginRight = 0;
//            if (info instanceof ShortcutInfo) {
//                if (info.container == (long) LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
//                    unreadMarginTop = (int) res.getDimension(R.dimen.hotseat_unread_margin_top);
//                    unreadMarginRight = (int) res.getDimension(R.dimen.hotseat_unread_margin_right);
//                } else if (info.container == (long) LauncherSettings.Favorites.CONTAINER_DESKTOP) {
//                    unreadMarginTop = (int) res.getDimension(R.dimen.workspace_unread_margin_top);
//                    unreadMarginRight = (int) res.getDimension(
//                            R.dimen.workspace_unread_margin_right);
//                } else {
//                    unreadMarginTop = (int) res.getDimension(R.dimen.folder_unread_margin_top);
//                    unreadMarginRight = (int) res.getDimension(R.dimen.folder_unread_margin_right);
//                }
//            } else if (info instanceof FolderInfo) {
//                if (info.container == (long) LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
//                    unreadMarginTop = (int) res.getDimension(R.dimen.hotseat_unread_margin_top);
//                    unreadMarginRight = (int) res.getDimension(R.dimen.hotseat_unread_margin_right);
//                } else if (info.container == (long) LauncherSettings.Favorites.CONTAINER_DESKTOP) {
//                    unreadMarginTop = (int) res.getDimension(R.dimen.workspace_unread_margin_top);
//                    unreadMarginRight = (int) res.getDimension(
//                            R.dimen.workspace_unread_margin_right);
//                }
//            } else if (info instanceof AppInfo) {
//                unreadMarginTop = (int) res.getDimension(R.dimen.app_list_unread_margin_top);
//                unreadMarginRight = (int) res.getDimension(R.dimen.app_list_unread_margin_right);
//            }
//
//            int unreadBgPosX = icon.getScrollX() + icon.getWidth()
//                    - unreadBgWidth - unreadMarginRight;
//            int unreadBgPosY = icon.getScrollY() + unreadMarginTop;
//
//            canvas.save();
//            canvas.translate(unreadBgPosX, unreadBgPosY);
//
//            if (unreadBgNinePatchDrawable != null) {
//                unreadBgNinePatchDrawable.draw(canvas);
//            } else {
//                if (Log.DEBUG_UNREAD) {
//                    Log.d(TAG, "drawUnreadEventIfNeed: "
//                            + "unreadBgNinePatchDrawable is null pointer");
//                }
//                return;
//            }
//            /// M: Draw unread text.
//            Paint.FontMetrics fontMetrics = unreadTextNumberPaint.getFontMetrics();
//            if (info.unreadNum > Launcher.MAX_UNREAD_COUNT) {
//                canvas.drawText(unreadTextNumber,
//                        (unreadBgWidth - unreadTextPlusBounds.width()) / 2,
//                        (unreadBgHeight + textHeight) / 2,
//                        unreadTextNumberPaint);
//                canvas.drawText(unreadTextPlus,
//                        (unreadBgWidth + unreadTextNumberBounds.width()) / 2,
//                        (unreadBgHeight + textHeight) / 2 + fontMetrics.ascent / 2,
//                        unreadTextPlusPaint);
//            } else {
//                canvas.drawText(unreadTextNumber,
//                        unreadBgWidth / 2,
//                        (unreadBgHeight + textHeight) / 2,
//                        unreadTextNumberPaint);
//            }
//            canvas.drawText(unreadTextNumber,
//                    unreadBgWidth / 2,
//                    (unreadBgHeight + textHeight) / 2,
//                    unreadTextNumberPaint);
//            canvas.restore();
//        }
//    }

    public static void drawUnreadEventIfNeed(Canvas canvas, View icon,Launcher launcher) {
        if(!Launcher.isUnreadEnable)return;

        ItemInfo info = (ItemInfo) icon.getTag();
        if (info != null && info.unreadNum > 0) {
            if (info instanceof AppInfo) return;
            Resources res = icon.getContext().getResources();
            Paint bgPaint = new Paint();
            Paint dotPaint = new Paint();
            float unreadBgRadius = res.getDimension(R.dimen.unread_bg_circle_radius);
            float unreadDotRadius = res.getDimension(R.dimen.unread_inner_circle_radius);
            int bgColor = res.getColor(R.color.unread_bg_color);
            int dotColor = res.getColor(R.color.unread_inner_dot_color);
            bgPaint.setColor(bgColor);
            bgPaint.setAntiAlias(true);
            dotPaint.setColor(dotColor);
            dotPaint.setAntiAlias(true);

            int unreadMarginTop = 0;
            int unreadMarginRight = 0;
//            if (info instanceof ShortcutInfo) {
//                if (info.container == (long) LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
//                    unreadMarginTop = (int) res.getDimension(R.dimen.hotseat_unread_margin_top);
//                    unreadMarginRight = (int) res.getDimension(R.dimen.hotseat_unread_margin_right);
//                } else if (info.container == (long) LauncherSettings.Favorites.CONTAINER_DESKTOP) {
//                    unreadMarginTop = (int) res.getDimension(R.dimen.workspace_unread_margin_top);
//                    unreadMarginRight = (int) res.getDimension(
//                            R.dimen.workspace_unread_margin_right);
//                } else {
//                    unreadMarginTop = (int) res.getDimension(R.dimen.folder_unread_margin_top);
//                    unreadMarginRight = (int) res.getDimension(R.dimen.folder_unread_margin_right);
//                }
//            } else if (info instanceof FolderInfo) {
//                if (info.container == (long) LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
//                    unreadMarginTop = (int) res.getDimension(R.dimen.hotseat_unread_margin_top);
//                    unreadMarginRight = (int) res.getDimension(R.dimen.hotseat_unread_margin_right);
//                } else if (info.container == (long) LauncherSettings.Favorites.CONTAINER_DESKTOP) {
//                    unreadMarginTop = (int) res.getDimension(R.dimen.workspace_unread_margin_top);
//                    unreadMarginRight = (int) res.getDimension(
//                            R.dimen.workspace_unread_margin_right);
//                }
//            } else if (info instanceof AppInfo) {
//                unreadMarginTop = (int) res.getDimension(R.dimen.app_list_unread_margin_top);
//                unreadMarginRight = (int) res.getDimension(R.dimen.app_list_unread_margin_right);
//            }

//            int unreadCenterX = icon.getScrollX() + icon.getWidth()
//                    - (int)unreadBgRadius - unreadMarginRight;
//            int unreadCenterY = icon.getScrollY() + unreadMarginTop + (int)unreadBgRadius;
            int unreadCenterX = 0;
            int unreadCenterY = 0;
            int offsetY = getIconDragingOffsetY(launcher, info) < unreadBgRadius ? (int) unreadBgRadius : getIconDragingOffsetY(launcher, info);
            int offsetX = 0;
            DeviceProfile profile = launcher.getDeviceProfile();
            if (icon instanceof TextView) {
                Drawable d = Workspace.getTextViewIcon((TextView) icon);
                if (d == null) return;
                Rect bounds = Workspace.getDrawableBounds(d);
                offsetX = icon.getWidth() / 2 + bounds.width() / 2 - 16;
            } else {
                offsetX = icon.getWidth() / 2 + profile.folderIconSizePx / 2 - 16;
            }
            unreadCenterX = icon.getScrollX() + offsetX;
            unreadCenterY = icon.getScrollY() + offsetY;
            canvas.save();
            canvas.drawCircle(unreadCenterX,unreadCenterY,unreadBgRadius,bgPaint);
            canvas.drawCircle(unreadCenterX-unreadBgRadius/2,unreadCenterY,unreadDotRadius,dotPaint);
            canvas.drawCircle(unreadCenterX,unreadCenterY,unreadDotRadius,dotPaint);
            canvas.drawCircle(unreadCenterX+unreadBgRadius/2,unreadCenterY,unreadDotRadius,dotPaint);
            canvas.restore();
            updateDrawState(info.getIntent().getComponent(),info.user.getUser(),true);
        }else if (info != null && info.unreadNum == 0 && (info instanceof ShortcutInfo)) {
            Log.e("---lj---","info:"+info);
            updateDrawState(info.getIntent().getComponent(), info.user.getUser(), false);
        }
    }

    private static int getIconDragingOffsetY(Launcher launcher,ItemInfo info) {
        DeviceProfile profile = launcher.getDeviceProfile();
        if(info!=null){
            if (info.container == (long) LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                return profile.getFolderIconOffsetYFromHotseat() + 16;
            } else if (info.container == (long) LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                return profile.getFolderIconOffsetY() + 16;
            }else{
                return profile.getFolderIconOffsetY() + 29;
            }
        }else {
            return profile.getFolderIconOffsetY() + 16;
        }

    }

    public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException {
        int type;
        while ((type = parser.next()) != parser.START_TAG
                && type != parser.END_DOCUMENT) {
            ;
        }

        if (type != parser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }

    public interface UnreadCallbacks {
        /**
         * Bind shortcuts and application icons with the given component, and
         * update folders unread which contains the given component.
         *
         * @param component
         * @param unreadNum
         */
        void bindComponentUnreadChanged(ComponentName component, int unreadNum ,UserHandle user);

        /**
         * Bind unread shortcut information if needed, this call back is used to
         * update shortcuts and folders when launcher first created.
         */
        void bindUnreadInfoIfNeeded();
    }

    /**
     * 获取未读短信数
     * @return
     */
    private int getNewSmsCount() {
        int result = 0;
        Cursor csr = null;
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                if (Log.DEBUG_UNREAD) {
                    Log.e(TAG, "permission denied to getNewSmsCount");
                }
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ((Launcher)mContext).requestPermissions(new String[]{Manifest.permission.READ_SMS},Launcher.REQUEST_PERMISSION_MISS_MMS_UNREAD);
                }else {
                    return 0;
                }
            }
            csr = mContext.getContentResolver().query(Uri.parse("content://sms"), null,
                    "type = 1 and read = 0", null, null);
            if (csr != null) {
                result = csr.getCount();
            }
        } catch (Exception e) {
            if (Log.DEBUG_UNREAD) {
                Log.e(TAG, "getNewSmsCount failed : ", e);
            }
        } finally {
            if (csr != null) {
                csr.close();
            }
        }
        return result;
    }

    /**
     * 获取未读彩信数
     * @return
     */
    private int getNewMmsCount() {
        int result = 0;
        Cursor csr = null;
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                if (Log.DEBUG_UNREAD) {
                    Log.e(TAG, "permission denied to getNewSmsCount");
                }
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ((Launcher)mContext).requestPermissions(new String[]{Manifest.permission.READ_SMS},Launcher.REQUEST_PERMISSION_MISS_MMS_UNREAD);
                }else {
                    return 0;
                }
            }
            csr = mContext.getContentResolver().query(Uri.parse("content://mms/inbox"),
                    null, "read = 0", null, null);
            if (csr != null) {
                result = csr.getCount();
            }
        } catch (Exception e) {
            if (Log.DEBUG_UNREAD) {
                Log.e(TAG, "getNewMmsCount failed : ", e);
            }
        } finally {
            if (csr != null) {
                csr.close();
            }
        }
        return result;
    }

    private ContentObserver newMmsContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            reloadMmsUnread();
        }
    };

    private ContentObserver missCallContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            reloadMissedCall();
        }
    };

    public void reloadMmsUnread(){
        int mNewSmsCount = getNewSmsCount() + getNewMmsCount();
        if (Log.DEBUG_UNREAD) {
            Log.d(TAG, "mNewSmsCount : " + mNewSmsCount);
        }
        synchronized (LOG_LOCK) {
            int index = findShortCutIndexByType(UNREAD_TYPE_MMS);
            if (index < 0) return;
            final UnreadSupportShortcut us = UNREAD_SUPPORT_SHORTCUTS.get(index);
            boolean ret = setUnreadNumberAt(index, mNewSmsCount);
            if (ret) {
                final UnreadCallbacks callbacks = mCallbacks.get();
                if (callbacks != null) {
                    callbacks.bindComponentUnreadChanged(us.component, mNewSmsCount, UserHandleCompat.myUserHandle().getUser());
                }
            }
        }
    }

    public void reloadMissedCall(){
        int mMissCallCount = readMissCall();
        if (Log.DEBUG_UNREAD) {
            Log.e(TAG, "mMissCallCount:" + mMissCallCount);
        }
        synchronized (LOG_LOCK) {
            final int index = findShortCutIndexByType(UNREAD_TYPE_PHONE);
            if (index < 0) return;
            final UnreadSupportShortcut us = UNREAD_SUPPORT_SHORTCUTS.get(index);
            boolean ret = setUnreadNumberAt(index, mMissCallCount);
            if (ret) {
                final UnreadCallbacks callbacks = mCallbacks.get();
                if (callbacks != null) {
                    callbacks.bindComponentUnreadChanged(us.component, mMissCallCount, UserHandleCompat.myUserHandle().getUser());
                }
            }
        }
    }

    public void reloadUnread(){
        if (Log.DEBUG_UNREAD) {
            Log.d(TAG, "reloadUnread");
        }
        reloadMissedCall();
        reloadMmsUnread();
    }

    public synchronized void registerObserver() {
        unregisterObserver();
        try {
            mContext.getContentResolver().registerContentObserver(Uri.parse("content://sms"), true,
                    newMmsContentObserver);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mContext.getContentResolver().registerContentObserver(Telephony.MmsSms.CONTENT_URI, true,
                        newMmsContentObserver);
            }
            mContext.getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true,
                    missCallContentObserver);
        } catch (Exception e) {
            if (Log.DEBUG_UNREAD) {
                Log.e(TAG, "unregisterObserver fail");
            }
        }
    }

    public synchronized void unregisterObserver() {
        try {
            if (newMmsContentObserver != null) {
                mContext.getContentResolver().unregisterContentObserver(newMmsContentObserver);
            }
            if (newMmsContentObserver != null) {
                mContext.getContentResolver().unregisterContentObserver(newMmsContentObserver);
            }
            if (missCallContentObserver != null) {
                mContext.getContentResolver().unregisterContentObserver(missCallContentObserver);
            }
        } catch (Exception e) {
            if (Log.DEBUG_UNREAD) {
                Log.e(TAG, "unregisterObserver fail");
            }
        }
    }

    /**
     * 读取未接电话数
     * @return
     */
    private int readMissCall() {
        int result = 0;
        Cursor csr = null;
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                if (Log.DEBUG_UNREAD) {
                    Log.e(TAG, "permission denied to readMissCall");
                }
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ((Launcher)mContext).requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG},Launcher.REQUEST_PERMISSION_MISS_CALL_UNREAD);
                }else {
                    return 0;
                }
            }
            csr = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{
                    CallLog.Calls.TYPE
            }, " type=? and new=?", new String[]{
                    CallLog.Calls.MISSED_TYPE + "", "1"
            }, "date desc");

            if (csr != null) {
                result = csr.getCount();
            }
        }catch(Exception e){
            if (Log.DEBUG_UNREAD) {
                Log.e(TAG, "readMissCall failed : ", e);
            }
        }finally{
            if(csr != null){
                csr.close();
            }
        }
        return result;
    }

    /**
     *
     * 找到系统中电话的应用，赋值给shortCut，并返回索引
     */
    private int findShortCutIndexByType(int type){
        if(type == UNREAD_TYPE_NORMAL)return -1;//三方应用类型都是UNREAD_TYPE_NORMAL
        synchronized (LOG_LOCK) {
            for (int i = 0; i < UNREAD_SUPPORT_SHORTCUTS.size(); i++) {
                UnreadSupportShortcut us = UNREAD_SUPPORT_SHORTCUTS.get(i);
                if (us.mShortcutType == type) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static boolean surportUnreadForTCL(Context context, String pkg) {
        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(pkg, 0);
            Class<?> c;
            c = Class.forName("android.app.NotificationManager");
            Method m = c.getMethod("getPackageSuperScriptOverride",new Class[] {String.class, int.class });
            Object result = m.invoke(nm,pkg, applicationInfo.uid);
            if (Log.DEBUG_UNREAD) {
                Log.d(TAG, "surportUnreadForTCL pkg : " + pkg + ", result:" + result);
            }
            return (boolean) result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean surportUnreadForOpenMarkert(Context context, String pkg) {
        try {
            SharedPreferences sp = context.getSharedPreferences(UNREAD_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
            synchronized (LOG_LOCK) {
                Set<String> strings = sp.getStringSet(UNREAD_SETTINGS_APPS, null);
                if (strings != null) {
                    Iterator<String> it = strings.iterator();
                    while (it.hasNext()) {
                        String encoded = it.next();
                        if (encoded.contains(pkg)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void addToUnreadQueue(Context context, String pkg) {
        if (pkg.isEmpty()) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(UNREAD_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        synchronized (LOG_LOCK) {
            Set<String> strings = sp.getStringSet(UNREAD_SETTINGS_APPS, null);
            if (strings != null) {
                strings = new HashSet<String>(strings);
                Iterator<String> newStringsIter = strings.iterator();

                while (newStringsIter.hasNext()) {
                    String encoded = newStringsIter.next();
                    if (encoded.contains(pkg)) {
                        newStringsIter.remove();
                    }
                }

            } else {
                strings = new HashSet<String>(1);
            }
            strings.add(pkg);
            sp.edit().putStringSet(UNREAD_SETTINGS_APPS, strings).commit();
        }
    }

    private static void removeFromUnreadQueue(Context context, String pkg) {
        if (pkg.isEmpty()) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(UNREAD_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        synchronized (LOG_LOCK) {
            Set<String> strings = sp.getStringSet(UNREAD_SETTINGS_APPS, null);
            if (strings != null) {
                strings = new HashSet<String>(strings);
                Iterator<String> newStringsIter = strings.iterator();
                while (newStringsIter.hasNext()) {
                    String encoded = newStringsIter.next();
                    if (encoded.contains(pkg)) {
                        newStringsIter.remove();
                    }
                }
                sp.edit().putStringSet(UNREAD_SETTINGS_APPS, strings).commit();
            }
        }
    }

    private static void restoreUnreadSupportSettings(Context context) {
        synchronized (LOG_LOCK) {
            int size = UNREAD_SUPPORT_SHORTCUTS.size();
            SharedPreferences sp = context.getSharedPreferences(UNREAD_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
            if (size <= 0) {
                sp.edit().putStringSet(UNREAD_SETTINGS_APPS, null).commit();
            } else {
                Set<String> strings = new HashSet<String>(size);
                for (Iterator it = UNREAD_SUPPORT_SHORTCUTS.iterator(); it.hasNext(); ) {
                    UnreadSupportShortcut shortcut = (UnreadSupportShortcut) it.next();
                    String component = shortcut.component.getPackageName() + "__" + shortcut.component.getClassName();
                    strings.add(component);
                }
                sp.edit().putStringSet(UNREAD_SETTINGS_APPS, strings).commit();
            }
        }
    }

    private static void restoreUnreadAllListSettings(Context context) {
        synchronized (LOG_LOCK) {
            int size = UNREAD_All_SHORTCUTS.size();
            SharedPreferences sp = context.getSharedPreferences(UNREAD_SETTINGS_AllLIST_PREFERENCE, Context.MODE_PRIVATE);
            if (size <= 0) {
                sp.edit().putStringSet(UNREAD_SETTINGS_APPS, null).commit();
            } else {
                Set<String> strings = new HashSet<String>(size);
                for (Iterator it = UNREAD_All_SHORTCUTS.iterator(); it.hasNext(); ) {
                    UnreadSupportShortcut shortcut = (UnreadSupportShortcut) it.next();
                    String component = shortcut.component.getPackageName() + "__" + shortcut.component.getClassName();
                    strings.add(component);
                }
                sp.edit().putStringSet(UNREAD_SETTINGS_APPS, strings).commit();
            }
        }
    }

    private static void updateDrawState(ComponentName componentName,UserHandle user,boolean hadDraw) {
        if (Log.DEBUG_UNREAD) {
            Log.d(TAG, "updateDrawState componentName : " + componentName + ",user:" + user + ",hadDraw:" + hadDraw);
        }
        synchronized (LOG_LOCK) {
            final int index = supportUnreadFeature(componentName, user);
            if (index >= 0) {
                UNREAD_SUPPORT_SHORTCUTS.get(index).hadDraw = hadDraw;
            }
        }
    }

}
