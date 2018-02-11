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

package com.android.gallery3d.util;

import java.util.Comparator;

import android.os.Environment;

import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;

public class MediaSetUtils {
    public static final Comparator<MediaSet> NAME_COMPARATOR = new NameComparator();

    // TCL ShenQianfeng Begin on 2016.11.22
    // Original:
    /*
    public static final int CAMERA_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
            + BucketNames.CAMERA);
    public static final int DOWNLOAD_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
            + BucketNames.DOWNLOAD);
    public static final int EDITED_ONLINE_PHOTOS_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
            + BucketNames.EDITED_ONLINE_PHOTOS);
    public static final int IMPORTED_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
            + BucketNames.IMPORTED);
    public static final int SNAPSHOT_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() +
            "/" + BucketNames.SCREENSHOTS);
    */
    // Modify To:
    public static final String EXTERNAL_STORAGE_DIRECTORY = Environment.getExternalStorageDirectory().toString();
    
    public static final String FILE_PATH_CAMERA = EXTERNAL_STORAGE_DIRECTORY + "/" + BucketNames.CAMERA;
    public static final String FILE_PATH_DOWNLOAD = EXTERNAL_STORAGE_DIRECTORY + "/" + BucketNames.DOWNLOAD;
    public static final String FILE_PATH_ONLINE_PHOTOS = EXTERNAL_STORAGE_DIRECTORY + "/" + BucketNames.EDITED_ONLINE_PHOTOS;
    public static final String FILE_PATH_IMPORTED = EXTERNAL_STORAGE_DIRECTORY + "/" + BucketNames.IMPORTED;
    public static final String FILE_PATH_SCREENSHOTS = EXTERNAL_STORAGE_DIRECTORY + "/" + BucketNames.SCREENSHOTS;
    
    public static final int CAMERA_BUCKET_ID = GalleryUtils.getBucketId(FILE_PATH_CAMERA);
    public static final int DOWNLOAD_BUCKET_ID = GalleryUtils.getBucketId(FILE_PATH_DOWNLOAD);
    public static final int EDITED_ONLINE_PHOTOS_BUCKET_ID = GalleryUtils.getBucketId(FILE_PATH_ONLINE_PHOTOS);
    public static final int IMPORTED_BUCKET_ID = GalleryUtils.getBucketId(FILE_PATH_IMPORTED);
    public static final int SCREENSHOTS_BUCKET_ID = GalleryUtils.getBucketId(FILE_PATH_SCREENSHOTS);
    
    
    private static final String [] mFilePaths = { FILE_PATH_CAMERA, FILE_PATH_DOWNLOAD, FILE_PATH_ONLINE_PHOTOS, FILE_PATH_IMPORTED, FILE_PATH_SCREENSHOTS };
    private static final int [] mBucketIds = {CAMERA_BUCKET_ID, DOWNLOAD_BUCKET_ID, EDITED_ONLINE_PHOTOS_BUCKET_ID, IMPORTED_BUCKET_ID, SCREENSHOTS_BUCKET_ID};
    
    // TCL ShenQianfeng End on 2016.11.22

    //TCL ShenQianfeng Begin on 2016.06.12
    /*
    public static final int MST_TEST_BUCKET_ID =  GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() +
            "/" + BucketNames.MST_TEST);
    */
    //TCL ShenQianfeng End on 2016.06.12
    
    // TCL ShenQianfeng Begin on 2016.11.22
    public static String getFilePathByBucketId(int bucketId) {
        for(int i=0; i<mBucketIds.length; i++) {
            if(mBucketIds[i] == bucketId) {
                return mFilePaths[i];
            }
        }
        return "";
    }
    // TCL ShenQianfeng End on 2016.11.22

    private static final Path[] CAMERA_PATHS = {
            Path.fromString("/local/all/" + CAMERA_BUCKET_ID),
            Path.fromString("/local/image/" + CAMERA_BUCKET_ID),
            Path.fromString("/local/video/" + CAMERA_BUCKET_ID)};

    public static boolean isCameraSource(Path path) {
        return CAMERA_PATHS[0] == path || CAMERA_PATHS[1] == path
                || CAMERA_PATHS[2] == path;
    }

    // Sort MediaSets by name
    public static class NameComparator implements Comparator<MediaSet> {
        @Override
        public int compare(MediaSet set1, MediaSet set2) {
            int result = set1.getName().compareToIgnoreCase(set2.getName());
            if (result != 0) return result;
            return set1.getPath().toString().compareTo(set2.getPath().toString());
        }
    }
}
