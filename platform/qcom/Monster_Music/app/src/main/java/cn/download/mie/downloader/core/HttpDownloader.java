package cn.download.mie.downloader.core;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.media.MediaScannerConnection;
import android.os.SystemClock;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import cn.download.mie.downloader.DownloadException;
import cn.download.mie.downloader.DownloadStatus;
import cn.download.mie.downloader.DownloadTask;
import cn.download.mie.util.DBUtils;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.database.OrderUtils;
import cn.tcl.music.database.PathUtils;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.Util;
import cn.tcl.music.view.striptab.DownloadNotification;

public class HttpDownloader implements INetworkDownloader {
    private static final String TAG = HttpDownloader.class.getSimpleName();

    private boolean pictureDownload = false;
    public HttpDownloader() {

    }

    @Override
    public void download(DownloadTask task) throws DownloadException {
        if (task.mStatus == DownloadStatus.PICTURE){
            pictureDownload = true;
            task.mStatus = DownloadStatus.PICTURE;
//            task.getDownloader().getEventCenter().onDownloadStatusChange(task);
        }else if(task.mStatus !=DownloadStatus.STOP){
            pictureDownload = false;
            task.mStatus = DownloadStatus.WAITING;
            task.getDownloader().getEventCenter().onDownloadStatusChange(task);
        }else {
            pictureDownload = false;
        }

        if (!task.checkUrl()) {
            throw new DownloadException(DownloadException.ECODE_URL_CHECK_FAILED);
        }

        //check the dir is already exists
        if (!task.checkDownloadPathAndMkDirs()) {
            throw new DownloadException(DownloadException.ECODE_PATH_NOT_EXIST);
        }

        //check the file is already exists
        task.checkDownloadFileAndDelete();

        long tempLocalLenth = task.getTempFileSize();

        if (task.mFileTotalSize <= 0) {
            //get the file size from server
            long serverLenth = getNetworkFileLength(task);
            task.mFileTotalSize = serverLenth;
        }

        if (task.mFileTotalSize < tempLocalLenth) {
            //download size error
            task.resetTask();
            throw new DownloadException(DownloadException.ECODE_LARGER_THAN_TARGET);
        } else if (task.mFileTotalSize == tempLocalLenth) {
            //download success where size is equal
            if (pictureDownload){
                doDownloadPictureFinish(task);
            }else {
                doDownloadFinish(task);
            }
        }
        downloadContent(task, tempLocalLenth);
    }

    private void downloadContent(DownloadTask task, long localSize) throws DownloadException {
        Response response = null;
        Call call = null;
        long downloadSize = localSize;
        OutputStream output = null;
        try {
            Map<String, String> headerParam = new HashMap<>(2);
            headerParam.put("RANGE", "bytes=" + localSize + "-");
            headerParam.put("User-Agent", task.getDownloader().getDownloaderConfig().mUA);
            call = HttpNetwork.connect(task.mUrl, headerParam, "GET");
            response = call.execute();
            int resCode = response.code();
            if (resCode == HttpURLConnection.HTTP_OK || resCode == HttpURLConnection.HTTP_PARTIAL) {
                String contentType = response.body().contentType().type();
                if (!task.checkContentType(contentType)) {
                    throw new DownloadException(DownloadException.ECODE_CONTENT_TYPE_NOT_ACCEPTABLE);
                }
                byte[] buf = new byte[1024 * 4];
                int nRead;
                boolean downloadFinish = false;
                InputStream input = response.body().byteStream();
                output = new FileOutputStream(task.getTempFilePath(), true);
                while (!task.isCancel && ((nRead = input.read(buf)) >= 0)) {
                    output.write(buf, 0, nRead);
                    downloadSize += nRead;
                    task.mFileDownloadedSize = downloadSize;
                    downloadFinish = (downloadSize == task.mFileTotalSize);
                    doDownloading(task, downloadSize, downloadFinish);
                    if (downloadFinish) {
                        output.close();
                        break;
                    }
                }
                if (task.isCancel) {
                    output.close();
                    throw new DownloadException(DownloadException.ECODE_PAUSE);
                }
                if (downloadFinish) {
                    output.close();
                    task.mDownloadFinishtime = System.currentTimeMillis();
                    if (pictureDownload){
                        doDownloadPictureFinish(task);
                    }else {
                        doDownloadFinish(task);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new DownloadException(e, DownloadException.ECODE_NETWORK);
        } finally {
            if (output != null) {
                try {
                    output.close();
                }  catch (Exception e) {
                }
            }
            call.cancel();
        }
    }

    private long lastTime = 0;
    private long lastDownloadSize = 0;

    private void doDownloading(DownloadTask task, long downloadSize, boolean force) {
        task.mStatus = DownloadStatus.DOWNLOADING;
        task.mFileDownloadedSize = downloadSize;
        if (lastTime != 0) {
            long timeInterval = SystemClock.elapsedRealtime() - lastTime;
            if (timeInterval > 1000 || force) {
                long space = downloadSize - lastDownloadSize;
                int currentSpeed = (int) ((space) / (timeInterval / 1000.0)); //每秒字节数
                task.getDownloader().getEventCenter().onDownloadProgress(task, downloadSize, task.mFileTotalSize, currentSpeed, 0, 0);
                lastTime = SystemClock.elapsedRealtime();
                lastDownloadSize = downloadSize;
            }

        } else {
            lastTime = SystemClock.elapsedRealtime();
            lastDownloadSize = downloadSize;
        }

    }

    private void doDownloadFinish(final DownloadTask task) throws DownloadException {
        task.mStatus = DownloadStatus.DOWNLOADED;
        task.getDownloader().getEventCenter().onDownloadStatusChange(task);
        if (task.isLyric) {
            try {
                File lyricFile = new File(task.getTempFilePath());
                lyricFile.renameTo(new File(task.getFinalFilePath()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        //set album info
        Util.addExtraMp3Info(task);
        DBUtils.getDownloadTaskManager(MusicApplication.getApp(), null).saveOrUpdate(task);


        File file = new File(task.getFinalFilePath());
        if (file.exists()) {
            LogUtil.d(TAG,"file exists and absolute path is " + file.getAbsolutePath());

            //about download finish,we need to update local database first,because of,if we call MediaScanner scan first,
            //when our app get the MediaScanner's broadcast,will do sync as soon as possible,the wrong result is : there will be two records in local database
            ContentValues contentValues = new ContentValues();
            contentValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.TITLE,task.mFileName);
            contentValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY, OrderUtils.keyFor(task.song_name));
            contentValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.PATH,task.getFinalFilePath());
            contentValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.SIZE,file.length());
            contentValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.DATE_ADD,System.currentTimeMillis());
            contentValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.SUFFIX, PathUtils.getSuffix(task.getFinalFilePath()));
            contentValues.put(MusicMediaDatabaseHelper.Media.MediaColumns.DOWNLOADED,CommonConstants.VALUE_MEDIA_DOWNLOADED);
            ContentResolver contentResolver = MusicApplication.getApp().getContentResolver();
            String where = MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID + " = ?";
            String[] selectionArgs = new String[]{task.mKey};
            contentResolver.update(MusicMediaDatabaseHelper.Media.CONTENT_URI,contentValues,where,selectionArgs);
        } else {
            LogUtil.d(TAG,"file not exists");
        }

        //call media scanner to scan the file
        MediaScannerConnection.scanFile(MusicApplication.getApp(), new String[]{task.getFinalFilePath()}, null, null);
        DownloadNotification.getInstance().setNotification(0, 1, null);
    }

    private  void updateLocalMediaArtwork (final DownloadTask task) {
        ContentResolver resolver = MusicApplication.getApp().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ARTWORK,task.album_logo);
        values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_PORTRAIT,task.artist_logo);
        int rowCount = resolver.update(MusicMediaDatabaseHelper.Media.CONTENT_URI, values, MusicMediaDatabaseHelper.Media.MediaColumns.TITLE + " = ?" +
                        " AND " + MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST + " = ?" +
                        " AND " + MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM + " = ?" +
                        " AND " + MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE + " = ?",
                new String[]{task.mFileName, task.artist_name, task.album_name, String.valueOf(CommonConstants.SRC_TYPE_LOCAL)});
        Log.d(TAG, "task.album_logo : " + task.album_logo + " task.artist_logo : " + task.artist_logo + " task.mKey : " + task.mKey + " rowCount : " + rowCount);
    }

    private void doDownloadPictureFinish(final DownloadTask task) throws DownloadException {
        task.mStatus = DownloadStatus.PICTURE;
        task.getDownloader().getEventCenter().onDownloadStatusChange(task);
        if (task.isLyric) {
            try {
                File lyricFile = new File(task.getTempFilePath());
                lyricFile.renameTo(new File(task.getFinalFilePath()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        //set extra mp3 info
        Util.addExtraMp3Info(task);
        DBUtils.getDownloadTaskManager(MusicApplication.getApp(), null).saveOrUpdate(task);
        MediaScannerConnection.scanFile(MusicApplication.getApp(), new String[]{task.getFinalFilePath()}, null, null);
        DownloadNotification.getInstance().setNotification(0, 1, null);

        if (task != null){
            //update these columns in local database
            ContentResolver resolver = MusicApplication.getApp().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.DOWNLOADED, DownloadStatus.PICTURE);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.SONG_REMOTE_ID,task.mKey);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ARTWORK,task.album_logo);
            values.put(MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST_PORTRAIT,task.artist_logo);
            int rowCount = resolver.update(MusicMediaDatabaseHelper.Media.CONTENT_URI, values, MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID + " = ?", new String[]{task.mKey});
            LogUtil.d(TAG, "song_name = " + task.song_name +", artist_name = " +task.artist_name +",album_name =  " +task.album_name  + ", uniqueId = " + task.mKey +", rowCount = "+rowCount);
        }

    }
    /**
     * get the file information from server
     *
     * @param task
     * @return
     * @throws DownloadException
     */
    private long getNetworkFileLength(DownloadTask task) throws DownloadException {
        Call call = null;
        Response response = null;
        try {
            Map<String, String> headerParam = new HashMap<>(2);
            headerParam.put("User-Agent", task.getDownloader().getDownloaderConfig().mUA);
            call = HttpNetwork.connect(task.mUrl, headerParam, "HEAD");
            response = call.execute();
            if (response.code() != HttpURLConnection.HTTP_OK) {
                throw new DownloadException(DownloadException.ECODE_SERVER);
            }


            String contentType = response.body().contentType().type();
            if (!task.checkContentType(contentType)) {
                throw new DownloadException(DownloadException.ECODE_CONTENT_TYPE_NOT_ACCEPTABLE);
            }
            long length = response.body().contentLength();
            return length;

        } catch (IOException e) {
            e.printStackTrace();
            throw new DownloadException(e, DownloadException.ECODE_NETWORK);
        } finally {
            call.cancel();
        }
    }


}
