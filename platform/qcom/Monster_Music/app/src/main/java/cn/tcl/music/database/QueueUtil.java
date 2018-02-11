package cn.tcl.music.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.PlayMode;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;

public class QueueUtil {
    private static final String TAG = QueueUtil.class.getSimpleName();

    /**
     * 添加歌曲到queue表中
     *
     * @param context
     * @param isNeedClearQueue 是否需要情况Queue表
     * @param mediaInfos       歌曲信息集合
     * @return 是否添加成功
     */
    public static int addMediaToQueue(Context context, boolean isNeedClearQueue, ArrayList<MediaInfo> mediaInfos) {
        int result = 0;
        if (isNeedClearQueue) {
            clearQueueTable(context);
        }
        ContentValues[] arrayValues = new ContentValues[mediaInfos.size()];
        for (int i = 0; i < mediaInfos.size(); i++) {
            MediaInfo mediaInfo = mediaInfos.get(i);
            ContentValues contentValues = new ContentValues();
            contentValues.put(MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID, mediaInfo.audioId);
            contentValues.put(MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE, CommonConstants.VALUE_QUEUE_IS_EFFECTIVE);
            contentValues.put(MusicMediaDatabaseHelper.Queue.QueueColumns.TRANSITION, mediaInfo.transitionId);
            contentValues.put(MusicMediaDatabaseHelper.Queue.QueueColumns.AUTO_ADDED, CommonConstants.VALUE_QUEUE_IS_AUTO_ADDED);
            arrayValues[i] = contentValues;
        }
        result = context.getContentResolver().bulkInsert(MusicMediaDatabaseHelper.Queue.CONTENT_URI, arrayValues);
        return result;
    }

    /**
     * 清除queueu表全部数据
     *
     * @param context
     * @return 删除的数量
     */
    public static int clearQueueTable(Context context) {
        return context.getContentResolver().delete(MusicMediaDatabaseHelper.Queue.CONTENT_URI, null, null);
    }

    /**
     * 根据mediaId删除Queue表中相应的数据
     *
     * @param mediaId 歌曲id
     * @return 删除的数量
     */
    public static int deleteQueueTableWithMediaId(Context context, long mediaId) {
        String where = MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(mediaId)};
        return context.getContentResolver().delete(MusicMediaDatabaseHelper.Queue.CONTENT_URI, where, selectionArgs);

    }

    /**
     * 获取Queue表可以播放的歌曲总数的count
     *
     * @param context
     * @return
     */
    public static int getQueuePlayableCount(Context context) {
        int count = 0;
        String selection = MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE + " = " + CommonConstants.VALUE_QUEUE_IS_EFFECTIVE;
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Queue.CONTENT_URI, null, selection, null, null);
        count = cursor.getCount();
        cursor.close();
        LogUtil.d(TAG, "getQueuePlayableCount and count is " + count);
        return count;
    }

    /**
     * 根据mediaId查询在Queue表中is_effective的状态
     * @param context
     * @param mediaId
     * @return  -1 代表没有找到  0 代表无效  1代表有效
     */
    public static int isMediaEffectiveInQueue(Context context,long mediaId) {
        int result = -1;
        String selection = MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(mediaId)};
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Queue.CONTENT_URI,null,selection,selectionArgs,null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            result = cursor.getInt(cursor.getColumnIndex(MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE));
        }
        cursor.close();
        LogUtil.d(TAG,"result is " + result);
        return result;
    }

    /**
     * 获取下一首可以播放的歌曲信息
     * 首先获取全部可以播放的歌曲的数量，如果可以播放的歌曲数量为0，那么直接返回null
     * <p/>
     * Step1：获取全部可以播放的歌曲的数量，如果为0，那么直接返回null
     * Step2：判断当前的播放模式
     * （1）顺序模式：获取全部实际可以播放的歌曲，考虑队尾找不到要从队头重新找，将数据copy一份放在后面，往后查找
     * （2）随机模式：从可以播放的歌曲里面随机取出一个
     * （3）单曲模式：根据当前的歌曲id，查找信息，直接返回
     *
     * @param context
     * @param queueMode             播放模式
     * @param currentMediaId        当前播放歌曲id，没有传-1
     * @return
     */
    public static MediaInfo getNextPlayableMediaInfo(Context context, int queueMode, long currentMediaId) {
        LogUtil.d(TAG,"queue mode is " + queueMode + " and currentMediaId is " + currentMediaId);
        MediaInfo mediaInfo = null;
        LogUtil.d(TAG,"getPrePlayableMediaInfo and time1 is " + System.currentTimeMillis());
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Queue.CONTENT_URI_QUEUE_MEDIA, null, null, null, MusicMediaDatabaseHelper.Queue.QueueColumns.PLAY_ORDER);
        //完整的queue表数据
        ArrayList<MediaInfo> allQueueMediaInfos = new ArrayList<>();
        //is_effective为1的queue表数据
        ArrayList<MediaInfo> playableQueueMediaInfos = new ArrayList<>();
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                MediaInfo tempMediaInfo = MusicUtil.getMediaInfoFromCursor(cursor);
                allQueueMediaInfos.add(tempMediaInfo);
                if (tempMediaInfo.isEffective == CommonConstants.VALUE_QUEUE_IS_EFFECTIVE) {
                    playableQueueMediaInfos.add(tempMediaInfo);
                }
            }



            //列表循环和单曲模式下的上一曲下一曲是相同的操作
            if (PlayMode.PLAY_MODE_NORMAL == queueMode || PlayMode.PLAY_MODE_REPEAT == queueMode) {
                //考虑到队尾找不到，要从队头重新找的原因，将一份数据copy成两份，如1,2,3，当前播放为1 --》 1,2,3,一,2,3
                ArrayList<MediaInfo> tempInfoList = new ArrayList<MediaInfo>();
                tempInfoList.addAll(allQueueMediaInfos);
                int currentIndex = -1;
                for (int i = 0; i < tempInfoList.size(); i++) {
                    MediaInfo tempMediaInfo = tempInfoList.get(i);
                    LogUtil.d(TAG,"tempMediaInfo audioId is " + tempMediaInfo.audioId);
                    if (currentMediaId == tempMediaInfo.audioId) {
                        currentIndex = i;
                        break;
                    }
                }
                tempInfoList.addAll(allQueueMediaInfos);

                if (currentIndex != -1) {
                    //找到上一曲的标志
                    while (currentIndex < tempInfoList.size() - 1) {
                        currentIndex ++;
                        MediaInfo tempMediaInfo = tempInfoList.get(currentIndex);
                        if (playableQueueMediaInfos.contains(tempMediaInfo)) {
                            mediaInfo = tempMediaInfo;
                            break;
                        }
                    }
                }
            } else {
                Random random = new Random();
                int randomIndex = random.nextInt(playableQueueMediaInfos.size());
                mediaInfo = playableQueueMediaInfos.get(randomIndex);
            }
        }
        cursor.close();
        return mediaInfo;
    }

    /**
     * 获取上一首可以播放的歌曲信息
     * 首先获取全部可以播放的歌曲的数量，如果可以播放的歌曲数量为0，那么直接返回null
     * <p/>
     * Step1：获取全部可以播放的歌曲的数量，如果为0，那么直接返回null
     * Step2：判断当前的播放模式
     * （1）顺序模式：获取全部实际可以播放的歌曲，考虑队尾找不到要从队头重新找，将数据copy一份放在前面，往前查找
     * （2）随机模式：从可以播放的歌曲里面随机取出一个
     * （3）单曲模式：根据当前的歌曲id，查找信息，直接返回
     *
     * @param context
     * @param queueMode             播放模式
     * @param currentMediaId        当前播放歌曲id，没有传-1
     * @return
     */
    public static MediaInfo getPrePlayableMediaInfo(Context context, int queueMode, long currentMediaId) {
        LogUtil.d(TAG,"queue mode is " + queueMode + " and currentMediaId is " + currentMediaId);
        MediaInfo mediaInfo = null;
        LogUtil.d(TAG,"getPrePlayableMediaInfo and time1 is " + System.currentTimeMillis());
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Queue.CONTENT_URI_QUEUE_MEDIA, null, null, null, MusicMediaDatabaseHelper.Queue.QueueColumns.PLAY_ORDER);
        //完整的queue表数据
        ArrayList<MediaInfo> allQueueMediaInfos = new ArrayList<>();
        //is_effective为1的queue表数据
        ArrayList<MediaInfo> playableQueueMediaInfos = new ArrayList<>();
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                MediaInfo tempMediaInfo = MusicUtil.getMediaInfoFromCursor(cursor);
                allQueueMediaInfos.add(tempMediaInfo);
                if (tempMediaInfo.isEffective == CommonConstants.VALUE_QUEUE_IS_EFFECTIVE) {
                    playableQueueMediaInfos.add(tempMediaInfo);
                }
            }

            //列表循环和单曲模式下的上一曲下一曲是相同的操作
            if (PlayMode.PLAY_MODE_NORMAL == queueMode || PlayMode.PLAY_MODE_REPEAT == queueMode) {
                //考虑到队尾找不到，要从队头重新找的原因，将一份数据copy成两份，如1,2,3，当前播放为1 --》 1,2,3,一,2,3
                ArrayList<MediaInfo> tempInfoList = new ArrayList<MediaInfo>();
                tempInfoList.addAll(allQueueMediaInfos);
                int currentIndex = -1;
                for (int i = 0; i < tempInfoList.size(); i++) {
                    MediaInfo tempMediaInfo = tempInfoList.get(i);
                    if (currentMediaId == tempMediaInfo.audioId) {
                        currentIndex = i;
                        currentIndex += tempInfoList.size();
                        break;
                    }
                }
                tempInfoList.addAll(allQueueMediaInfos);

                if (currentIndex != -1) {
                    //找到上一曲的标志
                    while (currentIndex > 0) {
                        currentIndex --;
                        MediaInfo tempMediaInfo = tempInfoList.get(currentIndex);
                        if (playableQueueMediaInfos.contains(tempMediaInfo)) {
                            mediaInfo = tempMediaInfo;
                            break;
                        }
                    }
                }
            } else {
                Random random = new Random();
                int randomIndex = random.nextInt(playableQueueMediaInfos.size());
                mediaInfo = playableQueueMediaInfos.get(randomIndex);
            }
        }
        cursor.close();
        return mediaInfo;
    }

    /**
     * 获取第一个可以播放的文件夹id
     * @param context
     * @return      文件夹id -1代表没有找到
     */
    public static long getFirstPlayableFolder(Context context) {
        long folderId = -1;
        String selection = MusicMediaDatabaseHelper.Folders.FoldersColumns.IS_SCAN + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(CommonConstants.VALUE_FOLDER_IS_SCAN)};
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Folders.CONTENT_URI,null,selection,selectionArgs,null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            folderId = cursor.getLong(cursor.getColumnIndex(MusicMediaDatabaseHelper.Folders.FoldersColumns._ID));
        }
        cursor.close();
        return folderId;
    }

    /**
     * 获取文件夹下面第一首可以播放的歌曲信息
     * @param context
     * @param folderId
     * @return
     */
    public static MediaInfo getFirstPlayableMediaInFolder(Context context,long folderId) {
        MediaInfo mediaInfo = null;
        String selection = MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(folderId)};
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI,null,selection,selectionArgs,null);
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            MediaInfo tempInfo = MusicUtil.getMediaInfoFromCursor(cursor);
            if (!TextUtils.isEmpty(tempInfo.filePath)) {
                File file = new File(tempInfo.filePath);
                if (file.exists()) {
                    mediaInfo = tempInfo;
                    break;
                }
            }
        }
        cursor.close();
        return mediaInfo;
    }

    /**
     * 获取全部的Queue数据
     * @param context
     * @return
     */
    public static ArrayList<MediaInfo> getAllQueueData(Context context) {
        ArrayList<MediaInfo> mediaInfos = new ArrayList<MediaInfo>();
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Queue.CONTENT_URI_QUEUE_MEDIA,null,null,null,null);
        while (cursor.moveToNext()) {
            MediaInfo mediaInfo = MusicUtil.getMediaInfoFromCursor(cursor);
            mediaInfos.add(mediaInfo);
        }
        cursor.close();
        return mediaInfos;
    }

    /**
     * 根据文件夹id，批量设置queue表中相关的数据effective的值为0
     * @param context
     * @param folderId
     * @return
     */
    public static int updateQueueSetIneffectiveByFolderId(Context context,long folderId) {
        int result = 0;
        String selection = MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(folderId)};
        Cursor cursor = context.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI,null,selection,selectionArgs,MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY);
        while (cursor.moveToNext()) {
            ContentValues contentValues = new ContentValues();
            long mediaId = cursor.getLong(cursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID));
            contentValues.put(MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE,CommonConstants.VALUE_QUEUE_IS_NOT_EFFECTIVE);
            String where = MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID + " = ?";
            String[] updateSelectionArgs = new String[]{String.valueOf(mediaId)};
            result += context.getContentResolver().update(MusicMediaDatabaseHelper.Queue.CONTENT_URI,contentValues,where,updateSelectionArgs);
        }
        cursor.close();
        return result;
    }

    public static int insertFolderToQueueByID(Context context, long folder_id) {
        int result = -1;
        if (getQueuePlayableCount(context) > 0) {
            result = clearQueueTable(context);
            if (result == 0) {
                // clear queue fail
                return -1;
            }
        }
        String selection = MusicMediaDatabaseHelper.Media.MediaColumns.SOURCE_TYPE + " <= ? and " + MusicMediaDatabaseHelper.Media.MediaColumns.FOLDER_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(CommonConstants.SRC_TYPE_MYMIX), String.valueOf(folder_id)};
        Cursor c = context.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED,
            DBUtil.MEDIA_FOLDER_COLUMNS,
            selection,
            selectionArgs,
            MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY);
        ArrayList<MediaInfo> mediaInfos = new ArrayList<MediaInfo>();
        if(c != null){
            while (c.moveToNext()) {
                MediaInfo info = MusicUtil.getMediaInfoFromCursor(c);
                if (info != null) {
                    mediaInfos.add(info);
                }
            }
            c.close();
        }
        if(mediaInfos.size() == 0){
            // though we clear queue and but next folder contain no playable song
            return -3;
        }
        result = addMediaToQueue(context, true, mediaInfos);
        if (result == 0) {
            // do clear queue but insert fail
            return -2;
        } else {
            return result;
        }
    }


    public static int clearAllIneffectiveSongInQueue(Context context){
        return context.getContentResolver().delete(MusicMediaDatabaseHelper.Queue.CONTENT_URI, MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE + " = ?",
            new String[]{String.valueOf(CommonConstants.VALUE_QUEUE_IS_NOT_EFFECTIVE)});
    }

    public static MediaInfo getFirstPlayableSongInQueue(Context context){
        Cursor c = context.getContentResolver().query(MusicMediaDatabaseHelper.Queue.CONTENT_URI,null, MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE + " = ?",
            new String[]{String.valueOf(CommonConstants.VALUE_QUEUE_IS_EFFECTIVE)},null);
        MediaInfo info = null;
        if(c != null){
            if(c.moveToFirst()){
               long mediaID = c.getLong(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID));
                info = DBUtil.getMediaInfoWithMediaId(context,mediaID);
            }
            c.close();
        }
        return info;
    }

    public static MediaInfo getNextPlayableMediaInQueueIfCurrentMediaIsIneffective(Context context, long currentMediaID) {
        Cursor c = context.getContentResolver().query(MusicMediaDatabaseHelper.Queue.CONTENT_URI, null, null, null, null);
        int currentIndex = -1;
        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            if (currentMediaID == c.getLong(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID)) &&
                CommonConstants.VALUE_QUEUE_IS_NOT_EFFECTIVE == c.getInt(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE))) {
                currentIndex = i;
                break;
            }
        }
        MediaInfo nextMedia = null;
        if (currentIndex != -1) {
            int nextIndex = currentIndex;
            for (int i = 0; i < c.getCount(); i++) {
                nextIndex++;
                if (nextIndex >= c.getCount()) {
                    nextIndex = 0;
                }
                c.moveToPosition(nextIndex);
                if (CommonConstants.VALUE_QUEUE_IS_EFFECTIVE == c.getInt(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Queue.QueueColumns.IS_EFFECTIVE))) {
                    if (DBUtil.getMediaInfoWithMediaId(context, c.getLong(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID))) != null) {
                        nextMedia = DBUtil.getMediaInfoWithMediaId(context, c.getLong(c.getColumnIndexOrThrow(MusicMediaDatabaseHelper.Queue.QueueColumns.MEDIA_ID)));
                        break;
                    }
                }
            }

        }
        c.close();
        return nextMedia;
    }
}
