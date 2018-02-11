/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.R.integer;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.LocalAlbum;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;

public class SelectionManager {
    @SuppressWarnings("unused")
    private static final String TAG = "SelectionManager";

    public static final int ENTER_SELECTION_MODE = 1;
    public static final int LEAVE_SELECTION_MODE = 2;
    public static final int SELECT_ALL_MODE = 3;

    private Set<Path> mClickedSet;
    private MediaSet mSourceMediaSet;
    private SelectionListener mListener;
    private DataManager mDataManager;
    private boolean mInverseSelection;
    private boolean mIsAlbumSet;
    private boolean mInSelectionMode;
    // TCL ShenQianfeng Begin on 2216.07.22
    // Original:
    //private boolean mAutoLeave = true;
    // Modify To:
    private boolean mAutoLeave = false;
    // TCL ShenQianfeng End on 2216.07.22
    // TCL BaiYuan Begin on 2016.11.01
    private int mTypeImageCount = 0;
    private int mTypeVideoCount = 0;
    private int mType = -1;
    // TCL BaiYuan End on 2016.11.01
    
    private int mTotal;

    public interface SelectionListener {
        public void onSelectionModeChange(int mode);
        public void onSelectionChange(Path path, boolean selected);
    }

    public SelectionManager(AbstractGalleryActivity activity, boolean isAlbumSet) {
        mDataManager = activity.getDataManager();
        mClickedSet = new HashSet<Path>();
        mIsAlbumSet = isAlbumSet;
        mTotal = -1;
    }

    // Whether we will leave selection mode automatically once the number of
    // selected items is down to zero.
    public void setAutoLeaveSelectionMode(boolean enable) {
        mAutoLeave = enable;
    }

    public void setSelectionListener(SelectionListener listener) {
        mListener = listener;
    }

    public void selectAll() {
        mInverseSelection = true;
        mClickedSet.clear();
        // TCL BaiYuan Begin on 2016.11.11
        mTypeImageCount = 0;
        mTypeVideoCount = 0;
        mType = -1;
        // TCL BaiYuan End on 2016.11.11
        mTotal = -1;
        enterSelectionMode();
        if (mListener != null) mListener.onSelectionModeChange(SELECT_ALL_MODE);
    }

    // TCL ShenQianfeng Begin on 2216.07.22
    // Original:
    /*
    public void deSelectAll() {
        leaveSelectionMode();
        mInverseSelection = false;
        mClickedSet.clear();
    }
    */
    // Modify To:
    public void deSelectAll() {
        //leaveSelectionMode();
        mInverseSelection = false;
        mClickedSet.clear();
        // TCL BaiYuan Begin on 2016.11.11
        mTypeImageCount = 0;
        mTypeVideoCount = 0;
        mType = -1;
        // TCL BaiYuan End on 2016.11.11
    }
    // TCL ShenQianfeng End on 2216.07.22
    

    public boolean inSelectAllMode() {
        return mInverseSelection && getSelectedCount() == getTotalCount();
    }

    public boolean inSelectionMode() {
        return mInSelectionMode;
    }

    public void enterSelectionMode() {
        if (mInSelectionMode) return;

        mInSelectionMode = true;
        if (mListener != null) mListener.onSelectionModeChange(ENTER_SELECTION_MODE);
    }

    public void leaveSelectionMode() {
        if (!mInSelectionMode) return;
        // TCL BaiYuan Begin on 2016.11.11
        mTypeImageCount = 0;
        mTypeVideoCount = 0;
        mType = -1;
        // TCL BaiYuan End on 2016.11.11
        mInSelectionMode = false;
        mInverseSelection = false;
        mClickedSet.clear();
        if (mListener != null) mListener.onSelectionModeChange(LEAVE_SELECTION_MODE);
    }

    public boolean isItemSelected(Path itemId) {
        return mInverseSelection ^ mClickedSet.contains(itemId);
    }

    // TCL BaiYuan Begin on 2016.11.03
    // Original:
    /*
    private int getTotalCount() {
    */
    // Modify To:
    public int getTotalCount(){
    // TCL BaiYuan End on 2016.11.03
        if (mSourceMediaSet == null) return -1;

        if (mTotal < 0) {
            mTotal = mIsAlbumSet
                    ? mSourceMediaSet.getSubMediaSetCount()
                    : mSourceMediaSet.getMediaItemCount();
        }
        return mTotal;
    }

    public int getSelectedCount() {
        int count = mClickedSet.size();
        if (mInverseSelection) {
            count = getTotalCount() - count;
        }
        return count;
    }

    // TCL BaiYuan Begin on 2016.11.02
    // Original:
    /*
     public void toggle(Path path) {
        if (mClickedSet.contains(path)) {
            mClickedSet.remove(path);
        } else {
            enterSelectionMode();
            mClickedSet.add(path);
        }
        // Convert to inverse selection mode if everything is selected.
        int count = getSelectedCount();
        if (count == getTotalCount()) {
            selectAll();
        }

        if (mListener != null) mListener.onSelectionChange(path, isItemSelected(path));
        if (count == 0 && mAutoLeave) {
            leaveSelectionMode();
        }
    }
    */
    // Modify To:
    public void toggle(MediaObject item) {
        if (-1 != mType) {
            mType = -1;
        }
        Path path = item.getPath();
        if (mClickedSet.contains(path)) {
            mClickedSet.remove(path);
            if( MediaObject.MEDIA_TYPE_IMAGE == item.getMediaType()) {
                mTypeImageCount--;
            }else if (MediaObject.MEDIA_TYPE_VIDEO == item.getMediaType()) {
                mTypeVideoCount--;
            }
        } else {
            enterSelectionMode();
            mClickedSet.add(path);
            if( MediaObject.MEDIA_TYPE_IMAGE == item.getMediaType()) {
                mTypeImageCount++;
            }else if (MediaObject.MEDIA_TYPE_VIDEO == item.getMediaType()) {
                mTypeVideoCount++;
            }
        }
        // Convert to inverse selection mode if everything is selected.
        int count = getSelectedCount();
        if (count == getTotalCount()) {
            selectAll();
        }
        
        if (mListener != null) mListener.onSelectionChange(path, isItemSelected(path));

        // TCL ShenQianfeng Begin on 2016.11.09
        // Annotated Below:
        /*
        if (count == 0 && mAutoLeave) {
            leaveSelectionMode();
        }
        */
        // TCL ShenQianfeng End on 2016.11.09
    }
    // TCL BaiYuan End on 2016.11.02
    
    // TCL BaiYuan Begin on 2016.11.01
    public int getType(){
        if (-1 != mType) {
            return mType;
        }
        int imageCount = mTypeImageCount;
        int videoCount = mTypeVideoCount;
        if(mInverseSelection){
           int imageTypeTotal = getImageTypeCount(mSourceMediaSet);
           int videoTypeTotal = mSourceMediaSet.getMediaItemCount() - imageTypeTotal;
           imageCount = imageTypeTotal - mTypeImageCount;
           videoCount = videoTypeTotal - mTypeVideoCount;
        }
        int type = 0;
        if (imageCount ==0 && videoCount != 0) {
            type = MediaObject.MEDIA_TYPE_VIDEO;
        }else if(videoCount ==0 && imageCount != 0){
            type = MediaObject.MEDIA_TYPE_IMAGE;
        }else if(videoCount !=0 && imageCount != 0){
            type = MediaObject.MEDIA_TYPE_VIDEO | MediaObject.MEDIA_TYPE_IMAGE;
        }
        mType = type;
        return mType;
    }
    // TCL BaiYuan End on 2016.11.01

    // TCL BaiYuan Begin on 2016.11.14
    private int getImageTypeCount(MediaSet set){
        int count = 0;
        if (set instanceof LocalAlbum) {
            LocalAlbum album = (LocalAlbum) set;
            if (album.isImageAlbum()) {
                count = album.getMediaItemCount();
            }
        }else{
            int totalCount = set.getSubMediaSetCount();
            for (int i = 0; i < totalCount; i++) {
                count += getImageTypeCount(set.getSubMediaSet(i));
            }
        }
        return count;
    }
    
    // TCL BaiYuan Begin on 2016.11.14
    
    private static boolean expandMediaSet(ArrayList<Path> items, MediaSet set, int maxSelection) {
        int subCount = set.getSubMediaSetCount();
        for (int i = 0; i < subCount; i++) {
            if (!expandMediaSet(items, set.getSubMediaSet(i), maxSelection)) {
                return false;
            }
        }
        int total = set.getMediaItemCount();
        int batch = 50;
        int index = 0;

        while (index < total) {
            int count = index + batch < total
                    ? batch
                    : total - index;
            ArrayList<MediaItem> list = set.getMediaItem(index, count);
            if (list != null
                    && list.size() > (maxSelection - items.size())) {
                return false;
            }
            for (MediaItem item : list) {
                items.add(item.getPath());
            }
            index += batch;
        }
        return true;
    }

    public ArrayList<Path> getSelected(boolean expandSet) {
        return getSelected(expandSet, Integer.MAX_VALUE);
    }

    public ArrayList<Path> getSelected(boolean expandSet, int maxSelection) {
        ArrayList<Path> selected = new ArrayList<Path>();
        if (mIsAlbumSet) {
            if (mInverseSelection) {
                int total = getTotalCount();
                for (int i = 0; i < total; i++) {
                    MediaSet set = mSourceMediaSet.getSubMediaSet(i);
                    Path id = set.getPath();
                    if (!mClickedSet.contains(id)) {
                        if (expandSet) {
                            if (!expandMediaSet(selected, set, maxSelection)) {
                                return null;
                            }
                        } else {
                            selected.add(id);
                            if (selected.size() > maxSelection) {
                                return null;
                            }
                        }
                    }
                }
            } else {
                for (Path id : mClickedSet) {
                    if (expandSet) {
                        if (!expandMediaSet(selected, mDataManager.getMediaSet(id),
                                maxSelection)) {
                            return null;
                        }
                    } else {
                        selected.add(id);
                        if (selected.size() > maxSelection) {
                            return null;
                        }
                    }
                }
            }
        } else {
            if (mInverseSelection) {
                int total = getTotalCount();
                int index = 0;
                while (index < total) {
                    int count = Math.min(total - index, MediaSet.MEDIAITEM_BATCH_FETCH_COUNT);
                    ArrayList<MediaItem> list = mSourceMediaSet.getMediaItem(index, count);
                    for (MediaItem item : list) {
                        Path id = item.getPath();
                        if (!mClickedSet.contains(id)) {
                            selected.add(id);
                            if (selected.size() > maxSelection) {
                                return null;
                            }
                        }
                    }
                    index += count;
                }
            } else {
                for (Path id : mClickedSet) {
                    selected.add(id);
                    if (selected.size() > maxSelection) {
                        return null;
                    }
                }
            }
        }
        return selected;
    }

    public void setSourceMediaSet(MediaSet set) {
        mSourceMediaSet = set;
        mTotal = -1;
    }
}
