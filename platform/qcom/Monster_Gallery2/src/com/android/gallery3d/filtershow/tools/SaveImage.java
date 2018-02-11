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

package com.android.gallery3d.filtershow.tools;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.widget.Toast;

import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.filtershow.FilterShowActivity;
import com.android.gallery3d.filtershow.cache.ImageLoader;
import com.android.gallery3d.filtershow.filters.FilterRepresentation;
import com.android.gallery3d.filtershow.filters.FiltersManager;
import com.android.gallery3d.filtershow.imageshow.MasterImage;
import com.android.gallery3d.filtershow.pipeline.CachingPipeline;
import com.android.gallery3d.filtershow.pipeline.ImagePreset;
import com.android.gallery3d.filtershow.pipeline.ProcessingService;
//import com.android.gallery3d.picturegrouping.ExifInfoFilter;
import com.android.gallery3d.util.XmpUtilHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
//import java.util.Date;
import java.util.TimeZone;

/**
 * Handles saving edited photo
 */
public class SaveImage {
    private static final String LOGTAG = "SaveImage";

    /**
     * Callback for updates
     */
    public interface Callback {
        void onPreviewSaved(Uri uri);
        void onProgress(int max, int current);
    }

    public interface ContentResolverQueryCallback {
        void onCursorResult(Cursor cursor);
    }

    private static final String TIME_STAMP_NAME = "_yyyyMMdd_HHmmss";
    private static final String PREFIX_PANO = "PANO";
    private static final String PREFIX_IMG = "IMG";
    private static final String POSTFIX_JPG = ".jpg";
    private static final String AUX_DIR_NAME = ".aux";

    private final Context mContext;
    private final Uri mSourceUri;
    private final Callback mCallback;
    private final File mDestinationFile;
    private final Uri mSelectedImageUri;
    private final Bitmap mPreviewImage;

    private int mCurrentProcessingStep = 1;

    public static final int MAX_PROCESSING_STEPS = 6;
    public static final String DEFAULT_SAVE_DIRECTORY = "EditedOnlinePhotos";

    //private static boolean isBurstShotPicture;//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-05,PR1533452

    // In order to support the new edit-save behavior such that user won't see
    // the edited image together with the original image, we are adding a new
    // auxiliary directory for the edited image. Basically, the original image
    // will be hidden in that directory after edit and user will see the edited
    // image only.
    // Note that deletion on the edited image will also cause the deletion of
    // the original image under auxiliary directory.
    //
    // There are several situations we need to consider:
    // 1. User edit local image local01.jpg. A local02.jpg will be created in the
    // same directory, and original image will be moved to auxiliary directory as
    // ./.aux/local02.jpg.
    // If user edit the local02.jpg, local03.jpg will be created in the local
    // directory and ./.aux/local02.jpg will be renamed to ./.aux/local03.jpg
    //
    // 2. User edit remote image remote01.jpg from picassa or other server.
    // remoteSavedLocal01.jpg will be saved under proper local directory.
    // In remoteSavedLocal01.jpg, there will be a reference pointing to the
    // remote01.jpg. There will be no local copy of remote01.jpg.
    // If user edit remoteSavedLocal01.jpg, then a new remoteSavedLocal02.jpg
    // will be generated and still pointing to the remote01.jpg
    //
    // 3. User delete any local image local.jpg.
    // Since the filenames are kept consistent in auxiliary directory, every
    // time a local.jpg get deleted, the files in auxiliary directory whose
    // names starting with "local." will be deleted.
    // This pattern will facilitate the multiple images deletion in the auxiliary
    // directory.

    /**
     * @param context
     * @param sourceUri The Uri for the original image, which can be the hidden
     *  image under the auxiliary directory or the same as selectedImageUri.
     * @param selectedImageUri The Uri for the image selected by the user.
     *  In most cases, it is a content Uri for local image or remote image.
     * @param destination Destinaton File, if this is null, a new file will be
     *  created under the same directory as selectedImageUri.
     * @param callback Let the caller know the saving has completed.
     * @return the newSourceUri
     */
    public SaveImage(Context context, Uri sourceUri, Uri selectedImageUri,
                     File destination, Bitmap previewImage, Callback callback)  {
        mContext = context;
        mSourceUri = sourceUri;
        mCallback = callback;
        mPreviewImage = previewImage;
        if (destination == null) {
            mDestinationFile = getNewFile(context, selectedImageUri);
        } else {
            mDestinationFile = destination;
        }

        mSelectedImageUri = selectedImageUri;
    }

    public static File getFinalSaveDirectory(Context context, Uri sourceUri) {
        File saveDirectory = SaveImage.getSaveDirectory(context, sourceUri);
        if ((saveDirectory == null) || !saveDirectory.canWrite()) {
            saveDirectory = new File(Environment.getExternalStorageDirectory(),
                    SaveImage.DEFAULT_SAVE_DIRECTORY);
        }
        // Create the directory if it doesn't exist
        if (!saveDirectory.exists())
            saveDirectory.mkdirs();
        return saveDirectory;
    }

    public static File getNewFile(Context context, Uri sourceUri) {
        File saveDirectory = getFinalSaveDirectory(context, sourceUri);
        String filename = new SimpleDateFormat(TIME_STAMP_NAME).format(new Date(
                System.currentTimeMillis()));
        if (hasPanoPrefix(context, sourceUri)) {
            return new File(saveDirectory, PREFIX_PANO + filename + POSTFIX_JPG);
        }
        return new File(saveDirectory, PREFIX_IMG + filename + POSTFIX_JPG);
    }

    /**
     * Remove the files in the auxiliary directory whose names are the same as
     * the source image.
     * @param contentResolver The application's contentResolver
     * @param srcContentUri The content Uri for the source image.
     */
    public static void deleteAuxFiles(ContentResolver contentResolver,
            Uri srcContentUri) {
        final String[] fullPath = new String[1];
        String[] queryProjection = new String[] { ImageColumns.DATA };
        querySourceFromContentResolver(contentResolver,
                srcContentUri, queryProjection,
                new ContentResolverQueryCallback() {
                    @Override
                    public void onCursorResult(Cursor cursor) {
                        fullPath[0] = cursor.getString(0);
                    }
                }
        );
        if (fullPath[0] != null) {
            // Construct the auxiliary directory given the source file's path.
            // Then select and delete all the files starting with the same name
            // under the auxiliary directory.
            File currentFile = new File(fullPath[0]);

            String filename = currentFile.getName();
            int firstDotPos = filename.indexOf(".");
            final String filenameNoExt = (firstDotPos == -1) ? filename :
                filename.substring(0, firstDotPos);
            File auxDir = getLocalAuxDirectory(currentFile);
            if (auxDir.exists()) {
                FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        if (name.startsWith(filenameNoExt + ".")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                };

                // Delete all auxiliary files whose name is matching the
                // current local image.
                File[] auxFiles = auxDir.listFiles(filter);
                for (File file : auxFiles) {
                    file.delete();
                }
            }
        }
    }

    public Object getPanoramaXMPData(Uri source, ImagePreset preset) {
        Object xmp = null;
        if (preset.isPanoramaSafe()) {
            InputStream is = null;
            try {
                is = mContext.getContentResolver().openInputStream(source);
                xmp = XmpUtilHelper.extractXMPMeta(is);
            } catch (FileNotFoundException e) {
                Log.w(LOGTAG, "Failed to get XMP data from image: ", e);
            } finally {
                Utils.closeSilently(is);
            }
        }
        return xmp;
    }

    public boolean putPanoramaXMPData(File file, Object xmp) {
        if (xmp != null) {
            return XmpUtilHelper.writeXMPMeta(file.getAbsolutePath(), xmp);
        }
        return false;
    }

    public ExifInterface getExifData(Uri source) {
        ExifInterface exif = new ExifInterface();
        String mimeType = mContext.getContentResolver().getType(mSelectedImageUri);
        if (mimeType == null) {
            mimeType = ImageLoader.getMimeType(mSelectedImageUri);
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-01, PR1033187 begin
            if (mimeType == null) {
                return exif;
            }
            //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-07-01, PR1033187 end
        }
        if (mimeType.equals(ImageLoader.JPEG_MIME_TYPE)) {
            InputStream inStream = null;
            try {
                inStream = mContext.getContentResolver().openInputStream(source);
                exif.readExif(inStream);
            } catch (FileNotFoundException e) {
                Log.w(LOGTAG, "Cannot find file: " + source, e);
            } catch (IOException e) {
                Log.w(LOGTAG, "Cannot read exif for: " + source, e);
            } finally {
                Utils.closeSilently(inStream);
            }
        }
        return exif;
    }

    public boolean putExifData(File file, ExifInterface exif, Bitmap image,
            int jpegCompressQuality) {
        boolean ret = false;
        OutputStream s = null;
        try {
            s = exif.getExifWriterStream(file.getAbsolutePath());
            image.compress(Bitmap.CompressFormat.JPEG,
                    (jpegCompressQuality > 0) ? jpegCompressQuality : 1, s);
            s.flush();
            s.close();
            s = null;
            ret = true;
        } catch (FileNotFoundException e) {
            Log.w(LOGTAG, "File not found: " + file.getAbsolutePath(), e);
        } catch (IOException e) {
            Log.w(LOGTAG, "Could not write exif: ", e);
        } finally {
            Utils.closeSilently(s);
        }
        return ret;
    }

    private Uri resetToOriginalImageIfNeeded(ImagePreset preset, boolean doAuxBackup) {
        Uri uri = null;
        if (!preset.hasModifications()) {
            // This can happen only when preset has no modification but save
            // button is enabled, it means the file is loaded with filters in
            // the XMP, then all the filters are removed or restore to default.
            // In this case, when mSourceUri exists, rename it to the
            // destination file.
            File srcFile = getLocalFileFromUri(mContext, mSourceUri);
            // If the source is not a local file, then skip this renaming and
            // create a local copy as usual.
            if (srcFile != null) {
                srcFile.renameTo(mDestinationFile);
/*
                uri = SaveImage.linkNewFileToUri(mContext, mSelectedImageUri,
                        mDestinationFile, System.currentTimeMillis(), doAuxBackup);
*/
                // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 begin
                uri = SaveImage.linkNewFileToUri(mContext, mSelectedImageUri,
                        mDestinationFile, System.currentTimeMillis(), doAuxBackup, null);
                // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 end

            }
        }
        return uri;
    }

    private void resetProgress() {
        mCurrentProcessingStep = 0;
    }

    private void updateProgress() {
        if (mCallback != null) {
            mCallback.onProgress(MAX_PROCESSING_STEPS, ++mCurrentProcessingStep);
        }
    }
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-22,CR979742 begin
    private void updateExifData(ExifInterface exif, long time) {
        // Set tags
        exif.setTag(exif.buildTag(ExifInterface.TAG_ORIENTATION,
                ExifInterface.Orientation.TOP_LEFT));
        // Remove old thumbnail
        exif.removeCompressedThumbnail();
    }
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-22,CR979742 end

    public Uri processAndSaveImage(ImagePreset preset, boolean flatten,
                                   int quality, float sizeFactor, boolean exit) {

        Uri uri = null;
        if (exit) {
            uri = resetToOriginalImageIfNeeded(preset, !flatten);
        }
        if (uri != null) {
            return null;
        }

        resetProgress();

        boolean noBitmap = true;
        int num_tries = 0;
        int sampleSize = 1;

        // If necessary, move the source file into the auxiliary directory,
        // newSourceUri is then pointing to the new location.
        // If no file is moved, newSourceUri will be the same as mSourceUri.
        Uri newSourceUri = mSourceUri;
        if (!flatten) {
            newSourceUri = moveSrcToAuxIfNeeded(mSourceUri, mDestinationFile);
        }

        Uri savedUri = mSelectedImageUri;
        if (mPreviewImage != null) {
            if (flatten) {
                Object xmp = getPanoramaXMPData(newSourceUri, preset);
                ExifInterface exif = getExifData(newSourceUri);
                long time = System.currentTimeMillis();
                updateExifData(exif, time);
                if (putExifData(mDestinationFile, exif, mPreviewImage, quality)) {
                    putPanoramaXMPData(mDestinationFile, xmp);
                    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 begin
                    ContentValues values = getContentValues(mContext, mSelectedImageUri, mDestinationFile, time, mPreviewImage);
                    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 end
                    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-11,CR915235 begin
                    savedUri = mContext.getContentResolver().insert(
                            Images.Media.EXTERNAL_CONTENT_URI, values);
                    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-11,CR915235 end
                }
            } else {
                Object xmp = getPanoramaXMPData(newSourceUri, preset);
                ExifInterface exif = getExifData(newSourceUri);
                long time = System.currentTimeMillis();
                updateExifData(exif, time);
                // If we succeed in writing the bitmap as a jpeg, return a uri.
                if (putExifData(mDestinationFile, exif, mPreviewImage, quality)) {
                    putPanoramaXMPData(mDestinationFile, xmp);
                    // mDestinationFile will save the newSourceUri info in the XMP.
                    if (!flatten) {
                        XmpPresets.writeFilterXMP(mContext, newSourceUri,
                                mDestinationFile, preset);
                    }
                    // After this call, mSelectedImageUri will be actually
                    // pointing at the new file mDestinationFile.
                    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 begin
                    savedUri = SaveImage.linkNewFileToUri(mContext, mSelectedImageUri,
                            mDestinationFile, time, !flatten, mPreviewImage);
                    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 end
                }
            }
            if (mCallback != null) {
                mCallback.onPreviewSaved(savedUri);
            }
        }

        // Stopgap fix for low-memory devices.
        while (noBitmap) {
            try {
                updateProgress();
                // Try to do bitmap operations, downsample if low-memory
                Bitmap bitmap = ImageLoader.loadOrientedBitmapWithBackouts(mContext, newSourceUri,
                        sampleSize);
                if (bitmap == null) {
                    return null;
                }
                if (sizeFactor != 1f) {
                    // if we have a valid size
                    int w = (int) (bitmap.getWidth() * sizeFactor);
                    int h = (int) (bitmap.getHeight() * sizeFactor);
                    if (w == 0 || h == 0) {
                        w = 1;
                        h = 1;
                    }
                    bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
                }
                updateProgress();
                CachingPipeline pipeline = new CachingPipeline(FiltersManager.getManager(),
                        "Saving");

                bitmap = pipeline.renderFinalImage(bitmap, preset);
                updateProgress();

                Object xmp = getPanoramaXMPData(newSourceUri, preset);
                ExifInterface exif = getExifData(newSourceUri);
                long time = System.currentTimeMillis();
                updateProgress();

                updateExifData(exif, time);
                updateProgress();
                // If we succeed in writing the bitmap as a jpeg, return a uri.
                if (putExifData(mDestinationFile, exif, bitmap, quality)) {
                    putPanoramaXMPData(mDestinationFile, xmp);
                    // mDestinationFile will save the newSourceUri info in the XMP.
                    if (!flatten) {
                        XmpPresets.writeFilterXMP(mContext, newSourceUri,
                                mDestinationFile, preset);
                        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 begin
                        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-05-16,PR1002886 begin
                        uri = updateFile(mContext, savedUri, mDestinationFile, time, bitmap);
                        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-05-16,PR1002886 end

                    } else {
                        ContentValues values = getContentValues(mContext, mSelectedImageUri, mDestinationFile, time, bitmap);
                        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 end
                        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-16, PR950280 begin
                      //[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-17, PR949845 begin
//                        if (exit) {
                            // TCL BaiYuan Begin on 2016.11.02
                        long nowInMs = System.currentTimeMillis();
                        long nowInSec = nowInMs / 1000;
                        values.put(Images.Media.WIDTH, bitmap.getWidth());
                        values.put(Images.Media.HEIGHT, bitmap.getHeight());
                        values.put(Images.Media.DATE_ADDED, nowInSec);
                        values.put(Images.Media.DATE_MODIFIED, nowInSec);
                        values.put(Video.Media.DATE_TAKEN, nowInMs);
                        // TCL BaiYuan End on 2016.11.02
                        if (savedUri != null)
                              mContext.getContentResolver().update(savedUri, values, null, null);
//                        } else {
//                            Object result = mContext.getContentResolver().insert(
//                                    Images.Media.EXTERNAL_CONTENT_URI, values);
//                        }
                        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-03-16, PR950280 end
                          //[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-17, PR949845 begin
                    }
                }
                updateProgress();
                noBitmap = false;
            } catch (OutOfMemoryError e) {
                // Try 5 times before failing for good.
                if (++num_tries >= 5) {
                    throw e;
                }
                System.gc();
                sampleSize *= 2;
                resetProgress();
            }
          //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-08,PR1722503 begin
            catch (SQLiteConstraintException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
          //[BUGFIX]-Add by TCTNJ,xinrong.wang, 2016-03-08,PR1722503 end
        }
        return uri;
    }

    /**
     *  Move the source file to auxiliary directory if needed and return the Uri
     *  pointing to this new source file. If any file error happens, then just
     *  don't move into the auxiliary directory.
     * @param srcUri Uri to the source image.
     * @param dstFile Providing the destination file info to help to build the
     *  auxiliary directory and new source file's name.
     * @return the newSourceUri pointing to the new source image.
     */
    private Uri moveSrcToAuxIfNeeded(Uri srcUri, File dstFile) {
        File srcFile = getLocalFileFromUri(mContext, srcUri);
        if (srcFile == null) {
            Log.d(LOGTAG, "Source file is not a local file, no update.");
            return srcUri;
        }

        // Get the destination directory and create the auxilliary directory
        // if necessary.
        File auxDiretory = getLocalAuxDirectory(dstFile);
        if (!auxDiretory.exists()) {
            boolean success = auxDiretory.mkdirs();
            if (!success) {
                return srcUri;
            }
        }

        // Make sure there is a .nomedia file in the auxiliary directory, such
        // that MediaScanner will not report those files under this directory.
        File noMedia = new File(auxDiretory, ".nomedia");
        if (!noMedia.exists()) {
            try {
                noMedia.createNewFile();
            } catch (IOException e) {
                Log.e(LOGTAG, "Can't create the nomedia");
                return srcUri;
            }
        }
        // We are using the destination file name such that photos sitting in
        // the auxiliary directory are matching the parent directory.
        File newSrcFile = new File(auxDiretory, dstFile.getName());
        // Maintain the suffix during move
        String to = newSrcFile.getName();
        String from = srcFile.getName();

        //[BUGFIX]-Add by TCTNJ,xiangyu.liu, 2016-03-17,Defect:1838560 begin
        try {
            to = to.substring(to.lastIndexOf("."));
            from = from.substring(from.lastIndexOf("."));

            if (!to.equals(from)) {
                String name = dstFile.getName();
                name = name.substring(0, name.lastIndexOf(".")) + from;
                newSrcFile = new File(auxDiretory, name);
            }
        }catch (Exception e) {
            Log.e(LOGTAG, e.toString());
        }
        //[BUGFIX]-Add by TCTNJ,xiangyu.liu, 2016-03-17,Defect:1838560 end

        if (!newSrcFile.exists()) {
            boolean success = srcFile.renameTo(newSrcFile);
            if (!success) {
                return srcUri;
            }
        }

        return Uri.fromFile(newSrcFile);

    }

    private static File getLocalAuxDirectory(File dstFile) {
        File dstDirectory = dstFile.getParentFile();
        File auxDiretory = new File(dstDirectory + "/" + AUX_DIR_NAME);
        return auxDiretory;
    }

    public static Uri makeAndInsertUri(Context context, Uri sourceUri) {
        long time = System.currentTimeMillis();
        String filename = new SimpleDateFormat(TIME_STAMP_NAME).format(new Date(time));
        File saveDirectory = getFinalSaveDirectory(context, sourceUri);
        File file = new File(saveDirectory, filename  + ".JPG");
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 begin
        return linkNewFileToUri(context, sourceUri, file, time, false, null);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 end
    }

    public static void saveImage(ImagePreset preset, final FilterShowActivity filterShowActivity,
            File destination) {
        Uri selectedImageUri = filterShowActivity.getSelectedImageUri();
        Uri sourceImageUri = MasterImage.getImage().getUri();
        boolean flatten = false;
        if (preset.contains(FilterRepresentation.TYPE_TINYPLANET)){
            flatten = true;
        }
        Intent processIntent = ProcessingService.getSaveIntent(filterShowActivity, preset,
                destination, selectedImageUri, sourceImageUri, flatten, 90, 1f, true);

        filterShowActivity.startService(processIntent);

        if (!filterShowActivity.isSimpleEditAction()) {
            String toastMessage = filterShowActivity.getResources().getString(
                    R.string.save_and_processing);
            Toast.makeText(filterShowActivity,
                    toastMessage,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void querySource(Context context, Uri sourceUri, String[] projection,
            ContentResolverQueryCallback callback) {
        ContentResolver contentResolver = context.getContentResolver();
        querySourceFromContentResolver(contentResolver, sourceUri, projection, callback);
    }

    private static void querySourceFromContentResolver(
            ContentResolver contentResolver, Uri sourceUri, String[] projection,
            ContentResolverQueryCallback callback) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(sourceUri, projection, null, null,
                    null);
            if ((cursor != null) && cursor.moveToNext()) {
                callback.onCursorResult(cursor);
            }
        } catch (Exception e) {
            // Ignore error for lacking the data column from the source.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static File getSaveDirectory(Context context, Uri sourceUri) {
        File file = getLocalFileFromUri(context, sourceUri);
        if (file != null) {
            return file.getParentFile();
        } else {
            return null;
        }
    }

    /**
     * Construct a File object based on the srcUri.
     * @return The file object. Return null if srcUri is invalid or not a local
     * file.
     */
  //[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-17, PR949845 begin
    public static File getLocalFileFromUri(Context context, Uri srcUri) {
        if (srcUri == null) {
            Log.e(LOGTAG, "srcUri is null.");
            return null;
        }

        String scheme = srcUri.getScheme();
        if (scheme == null) {
            Log.e(LOGTAG, "scheme is null.");
            return null;
        }

        final File[] file = new File[1];
        // sourceUri can be a file path or a content Uri, it need to be handled
        // differently.
        if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            if (srcUri.getAuthority().equals(MediaStore.AUTHORITY)) {
                querySource(context, srcUri, new String[] {
                        ImageColumns.DATA
                },
                        new ContentResolverQueryCallback() {

                            @Override
                            public void onCursorResult(Cursor cursor) {
                                file[0] = new File(cursor.getString(0));
                            }
                        });
            }
        } else if (scheme.equals(ContentResolver.SCHEME_FILE)) {
            file[0] = new File(srcUri.getPath());
        }
        return file[0];
    }

    /**
     * Gets the actual filename for a Uri from Gallery's ContentProvider.
     */
    private static String getTrueFilename(Context context, Uri src) {
        if (context == null || src == null) {
            return null;
        }
        final String[] trueName = new String[1];
        querySource(context, src, new String[] {
                ImageColumns.DATA
        }, new ContentResolverQueryCallback() {
            @Override
            public void onCursorResult(Cursor cursor) {
                trueName[0] = new File(cursor.getString(0)).getName();
            }
        });
        return trueName[0];
    }

    /**
     * Checks whether the true filename has the panorama image prefix.
     */
    private static boolean hasPanoPrefix(Context context, Uri src) {
        String name = getTrueFilename(context, src);
        return name != null && name.startsWith(PREFIX_PANO);
    }

    /**
     * If the <code>sourceUri</code> is a local content Uri, update the
     * <code>sourceUri</code> to point to the <code>file</code>.
     * At the same time, the old file <code>sourceUri</code> used to point to
     * will be removed if it is local.
     * If the <code>sourceUri</code> is not a local content Uri, then the
     * <code>file</code> will be inserted as a new content Uri.
     * @return the final Uri referring to the <code>file</code>.
     */
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 begin
    public static Uri linkNewFileToUri(Context context, Uri sourceUri,
            File file, long time, boolean deleteOriginal, Bitmap bitmap) {
        File oldSelectedFile = getLocalFileFromUri(context, sourceUri);
        final ContentValues values = getContentValues(context, sourceUri, file, time, bitmap);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 end

        Uri result = sourceUri;

        // In the case of incoming Uri is just a local file Uri (like a cached
        // file), we can't just update the Uri. We have to create a new Uri.
        boolean fileUri = isFileUri(sourceUri);

        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-05,PR1533452 begin
        if (fileUri || oldSelectedFile == null || !deleteOriginal) {
            result = context.getContentResolver().insert(
                    Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            context.getContentResolver().update(sourceUri, values, null, null);
            if (oldSelectedFile.exists()) {
                oldSelectedFile.delete();
            }
        }
        //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-05,PR1533452 end
        return result;
    }

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 begin
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-05-16,PR1002886 begin
    public static Uri updateFile(Context context, Uri sourceUri, File file, long time, Bitmap bitmap) {
        final ContentValues values = getContentValues(context, sourceUri, file, time, bitmap);
        context.getContentResolver().update(sourceUri, values, null, null);
        return sourceUri;
    }
    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 end
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-05-16,PR1002886 end

    // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 begin
  //[BUGFIX]-Modify by TCTNJ, ye.chen, 2015-03-17, PR949845 begin
    public static ContentValues getContentValues(Context context, Uri sourceUri,
                                                  File file, long time, Bitmap bitmap) {
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 end
        final ContentValues values = new ContentValues();

        time /= 1000;
        values.put(Images.Media.TITLE, file.getName());
        values.put(Images.Media.DISPLAY_NAME, file.getName());
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.DATE_TAKEN, time);
        values.put(Images.Media.DATE_MODIFIED, time);
        values.put(Images.Media.DATE_ADDED, time);
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, file.getAbsolutePath());
        values.put(Images.Media.SIZE, file.length());
        // This is a workaround to trigger the MediaProvider to re-generate the
        // thumbnail.
        values.put(Images.Media.MINI_THUMB_MAGIC, 0);
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 begin
        if (bitmap != null) {
            values.put(Images.Media.WIDTH, bitmap.getWidth());
            values.put(Images.Media.HEIGHT, bitmap.getHeight());
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 end

        final String[] projection = new String[] {
                ImageColumns.DATE_TAKEN,
                ImageColumns.LATITUDE, ImageColumns.LONGITUDE,
        };

        SaveImage.querySource(context, sourceUri, projection,
                new ContentResolverQueryCallback() {

                    @Override
                    public void onCursorResult(Cursor cursor) {
                        values.put(Images.Media.DATE_TAKEN, cursor.getLong(0));

                        double latitude = cursor.getDouble(1);
                        double longitude = cursor.getDouble(2);
                        // TODO: Change || to && after the default location
                        // issue is fixed.
                        if ((latitude != 0f) || (longitude != 0f)) {
                            values.put(Images.Media.LATITUDE, latitude);
                            values.put(Images.Media.LONGITUDE, longitude);
                        }
                    }
                });
        return values;
    }

    /**
     * @param sourceUri
     * @return true if the sourceUri is a local file Uri.
     */
    private static boolean isFileUri(Uri sourceUri) {
        String scheme = sourceUri.getScheme();
        if (scheme != null && scheme.equals(ContentResolver.SCHEME_FILE)) {
            return true;
        }
        return false;
    }

}
