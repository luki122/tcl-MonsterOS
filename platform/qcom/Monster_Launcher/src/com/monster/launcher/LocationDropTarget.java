package com.monster.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.monster.launcher.compat.UserHandleCompat;
import com.monster.launcher.util.LongArrayMap;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by antino on 16-6-21.
 */
public class LocationDropTarget  extends  ButtonDropTarget{

    public LocationDropTarget(Context context){
        this(context,null);
    }

    public LocationDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LocationDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    private boolean mIsLocateSuccess; //add by xiangzx

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // Get the hover color
        mHoverColor = getResources().getColor(R.color.location_target_hover_tint);
        //TODO:xiejun, I just apply mdpi picture here,remenber apply other dencenty picture.
        setDrawable(R.drawable.ic_location_launcher_1);
    }


    @Override
    protected boolean supportsDrop(DragSource source, Object info) {
        //modify by xiangzx to repair the condition of locationDrop
       /* Log.i(TAG,"source = "+source);
        return ((info instanceof  ShortcutInfo)||(info instanceof  AppInfo))&&(source instanceof AllAppsContainerView);*/
        return (info instanceof  AppInfo);
    }

    //add by xiangzx
    @Override
    protected void exitDragMode() {
        if(mIsLocateSuccess) {
            mLauncher.showWorkspace(false, null);
        }else{
            Toast.makeText(this.getContext(), R.string.location_failed_hint, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    void completeDrop(DragObject d) {
        mIsLocateSuccess = false;
        //add by xiangzx to locate view
        final AppInfo appInfo = (AppInfo) d.dragInfo;
        ComponentName appCompName = appInfo.getIntent().getComponent();
        UserHandleCompat userHandleCompat  = appInfo.user;
        ArrayList<ItemInfo> sWorkspaceItems = mLauncher.getModel().sBgWorkspaceItems;
        long screenId = -1;
        ShortcutInfo shortcutInfo = null;
        for(ItemInfo workspaceItem : sWorkspaceItems){

            if(workspaceItem instanceof ShortcutInfo){
                ShortcutInfo info = (ShortcutInfo)workspaceItem;
                if(info.getIntent().getComponent() == null){
                    Log.e("locate", "component=null----"+info.title+"---"+"in workspace");
                }else if(info.getIntent().getComponent().equals(appCompName) && (userHandleCompat != null && userHandleCompat.equals(info.user))){
                    screenId = info.screenId;
                    shortcutInfo = info;
                    break;
                }
            }
        }
        //in workspace
        if(screenId != -1){
                mIsLocateSuccess = true;
                snapToLocateView(screenId, shortcutInfo, null, shortcutInfo.container);
        }//in folder
        else{
            long folderScreen = -1;
            FolderInfo folderInfo = null;
            ItemInfo folderInnerItem = null;
            LongArrayMap<FolderInfo> sFolders = mLauncher.getModel().sBgFolders;
            Iterator<FolderInfo> iterator = sFolders.iterator();
            while(iterator.hasNext()){
                FolderInfo fInfo = iterator.next();
                for(ShortcutInfo info : fInfo.contents){
                    if(info.getIntent().getComponent() == null){
                        Log.e("locate", "component=null----"+info.title+"---"+"in folder "+fInfo.toString());
                    }else if(info.getIntent().getComponent().equals(appCompName) && (userHandleCompat != null && userHandleCompat.equals(info.user))){
                        folderScreen = fInfo.screenId;
                        folderInfo = fInfo;
                        folderInnerItem = info;
                        break;
                    }
                }
                if(folderScreen != -1){
                    break;
                }
            }
            if(folderScreen != -1) {
                mIsLocateSuccess = true;
                snapToLocateView(folderScreen, folderInfo, folderInnerItem, folderInfo.container);
            }
        }

    }

    //add by xiangzx
    private void snapToLocateView(final long screenId, final ItemInfo info, final ItemInfo folderInnerItem, final long container){
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                int snapToPage = mLauncher.mWorkspace.getPageIndexForScreenId(screenId);
                boolean snapToCurrentScreen = (container == LauncherSettings.Favorites.CONTAINER_DESKTOP && snapToPage == mLauncher.mWorkspace.mCurrentPage);
                if(info instanceof FolderInfo){
                    final FolderIcon folderIcon = (FolderIcon) mLauncher.mWorkspace.getViewForTag(info);
                    if(container == LauncherSettings.Favorites.CONTAINER_HOTSEAT || snapToCurrentScreen){
                        int folderInnerScreenId = folderInnerItem.rank / folderIcon.mFolder.mContent.itemsPerPage();
                        mLauncher.openFolder(folderIcon);
                        folderIcon.mFolder.mContent.snapToPageImmediately(folderInnerScreenId);
                        startLocateViewAnim(folderIcon, folderInnerItem);
                    }else {
                        mLauncher.mWorkspace.snapToPage(snapToPage, new Runnable() {
                            @Override
                            public void run() {
                                int folderInnerScreenId = folderInnerItem.rank / folderIcon.mFolder.mContent.itemsPerPage();
                                mLauncher.openFolder(folderIcon);
                                folderIcon.mFolder.mContent.snapToPageImmediately(folderInnerScreenId);
                                startLocateViewAnim(folderIcon, folderInnerItem);
                            }
                        });
                    }
                }else if(container == LauncherSettings.Favorites.CONTAINER_HOTSEAT || snapToCurrentScreen){
                    startLocateViewAnim(null, info);
                }else{
                    mLauncher.mWorkspace.snapToPage(snapToPage, new Runnable() {
                        @Override
                        public void run() {
                            startLocateViewAnim(null, info);
                        }
                    });
                }
            }
        }, 100);
    }

    private void startLocateViewAnim(FolderIcon folderIcon, ItemInfo info){
        Animation locateAnim = AnimationUtils.loadAnimation(mLauncher, R.anim.locate_item_anim);
        View view = null;
        if(info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP || info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            view = mLauncher.mWorkspace.getViewForTag(info);
        }else{
            view = folderIcon.mFolder.mContent.findViewWithTag(info);
        }
        if(view != null) {
            view.startAnimation(locateAnim);
        }
    }

}
