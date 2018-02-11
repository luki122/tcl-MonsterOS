package com.morpho.utils.multimedia;

import java.io.File;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;

import com.android.camera.util.ApiHelper;


public class MediaProviderUtils {
    
    /**
     * 画像の回転角度
     */
    public static final int ROTATION_NORMAL =   0;
    public static final int ROTATION_90     =  90;
    public static final int ROTATION_180    = 180;
    public static final int ROTATION_270    = 270;
    
    
    /*****************************************************************************************/
    /**  Content URI の取得 
    /*****************************************************************************************/
    /**
     * 外部ストレージに保存してある画像ファイルを対象とし、ファイルパスから "content://～" 形式の URI を取得
     * 
     * @return  null: エラー, それ以外: URI
     */
    public static Uri getExternalImageContentUri(ContentResolver cr, String filePath) {
        return getImageContentUri(cr, filePath, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }
    
    /**
     * 内部ストレージに保存してある画像ファイルを対象とし、ファイルパスから "content://～" 形式の URI を取得
     * 
     * @return  null: エラー, それ以外: URI
     */
    public static Uri getInternalImageContentUri(ContentResolver cr, String filePath) {
        return getImageContentUri(cr, filePath, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
    }
    
    /**
     * 画像ファイルパスから "content://～" 形式の URI を取得
     * @return  null: エラー, それ以外: URI
     */
    private static Uri getImageContentUri(ContentResolver cr, String filePath, Uri storageUri) {
        
        if ((cr == null) || (filePath == null)) {
            return null;
        }

        // DATA が filePath と一致するレコードを検索
        Cursor cursor = cr.query(storageUri,
                                 new String[]{ MediaStore.Images.Media._ID },   // 取得対象 column
                                 MediaStore.Images.Media.DATA + "=?",           // 抽出条件
                                 new String[]{ filePath },                      // 抽出条件
                                 MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        
        if (cursor == null) {
            return null;
        } else if (cursor.getCount() <= 0) {
            return null;
        }
        
        cursor.moveToFirst();
        String id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        String content = storageUri.toString() + File.separator + id;
        
        return Uri.parse(content);
    }
    

    /*****************************************************************************************/
    /**  ファイルパスの取得
    /*****************************************************************************************/
    /**
     * 画像ファイルの Content URI からファイルパスを取得
     * 
     * @return  ファイルパス String
     */
    public static String getImageFilePath(ContentResolver cr, Uri uri) {
        return getContentFilePath(cr, uri, MediaStore.Images.Media.DATA);
    }
    
    /**
     * 動画ファイルの Content URI からファイルパスを取得
     * 
     * @return  ファイルパス String
     */
    public static String getVideoFilePath(ContentResolver cr, Uri uri) {
        return getContentFilePath(cr, uri, MediaStore.Video.Media.DATA);
    }
    
    /**
     * Content URI からファイルパスを取得
     * 
     * @return  ファイルパス String
     */
    private static String getContentFilePath(ContentResolver cr, Uri uri, String colunmName) {

        if ((cr == null) || (uri == null)) {
            return null;
        }

        Cursor cursor = cr.query(uri,
                                 new String[]{ colunmName },    // 取得対象 column
                                 null,
                                 null,
                                 null);
        
        if (cursor == null) {
            return null;
        } else if (cursor.getCount() <= 0) {
            return null;
        }
        
        cursor.moveToFirst();
        String filePath = cursor.getString(cursor.getColumnIndex(colunmName));

        return filePath;
    }
    
    
    /*****************************************************************************************/
    /**  画像ファイルを MediaProvider に追加
    /*****************************************************************************************/
    /**
     * 外部ストレージに保存されている画像ファイルを MediaProvider に追加
     * 
     * @param   cr        ContentResolver
     * @param   filePath  保存ファイルパス
     * @param   mime      MIME TYPE
     * @param   rotation  画像の回転角度 (0° or 90° or 180° or 270°)
     * @return  null: エラー, それ以外: 生成された Content URI
     */
    public static Uri addImageExternal(ContentResolver cr, String filePath, String mime, int rotation) {
        return addImage(cr, filePath, mime, rotation, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    public static Uri addImageExternal(ContentResolver cr, String filePath, String mime, int rotation,
                                       Location location, Rect rect) {
        return addImage(cr, filePath, mime, rotation, location, rect, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    /**
     * 内部ストレージに保存されている画像ファイルを MediaProvider に追加
     * 
     * @param   cr        ContentResolver
     * @param   filePath  保存ファイルパス
     * @param   mime      MIME TYPE
     * @param   rotation  画像の回転角度 (0° or 90° or 180° or 270°)
     * @return  null: エラー, それ以外: 生成された Content URI
     */
    public static Uri addImageInternal(ContentResolver cr, String filePath, String mime, int rotation) {
        return addImage(cr, filePath, mime, rotation, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
    }
    
    /**
     * 画像ファイルを MediaProvider に追加
     * @return  null: エラー, それ以外: 生成された Content URI
     */
    private static Uri addImage(ContentResolver cr, String filePath, String mime, int rotation, Uri storageUri) {
        return addImage(cr, filePath, mime, rotation, null, null, storageUri);
    }

    private static Uri addImage(ContentResolver cr, String filePath, String mime, int rotation,
                                Location location, Rect rect, Uri storageUri) {
        
        if ((cr == null) || (filePath == null) || (mime == null)) {
            return null;
        }

        File file = new File(filePath);
        String fileName = file.getName();
        
        ContentValues contentValues = new ContentValues(9);
        long time = System.currentTimeMillis();

        // Orientation 値のチェック
        if (rotation != ROTATION_NORMAL && rotation != ROTATION_90 &&
            rotation != ROTATION_180    && rotation != ROTATION_270) {
            // 回転角 0/90/180/270 度以外の指定はゼロとして扱う
            rotation = ROTATION_NORMAL;
        }
        
        contentValues.put(MediaStore.Images.Media.TITLE, fileName);             // タイトル名
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);      // タイトル名
        contentValues.put(MediaStore.Images.Media.DATE_TAKEN, time);
        contentValues.put(MediaStore.Images.Media.DATE_ADDED, time/1000);
        contentValues.put(MediaStore.Images.Media.DATE_MODIFIED, time/1000);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, mime);
        contentValues.put(MediaStore.Images.Media.ORIENTATION, rotation);
        contentValues.put(MediaStore.Images.Media.DATA, filePath);              // 画像の保存されたフルパス
        contentValues.put(MediaStore.Images.Media.SIZE, file.length());

        if (ApiHelper.HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT && rect != null) {
            int width = rect.right - rect.left;
            int height = rect.bottom - rect.top;
            contentValues.put(MediaStore.MediaColumns.WIDTH, width);
            contentValues.put(MediaStore.MediaColumns.HEIGHT, height);
        }

        if (location != null) {
            contentValues.put(MediaStore.Images.ImageColumns.LATITUDE, location.getLatitude());
            contentValues.put(MediaStore.Images.ImageColumns.LONGITUDE, location.getLongitude());
        }

        return cr.insert(storageUri, contentValues);
    }
    

    /*****************************************************************************************/
    /**  画像ファイルを MediaProvider から削除
    /*****************************************************************************************/
    /**
     * 外部ストレージに保存されている画像ファイルを MediaProvider から削除
     * 
     * @param cr        ContentResolver
     * @param filePath  削除する画像のファイルパス
     * @return  -1: エラー, 0以上: MediaProvider から削除された row の数
     */
    public static int deleteImageExternal(ContentResolver cr, String filePath) {
        return deleteImage(cr, filePath, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }
    
    /**
     * 内部ストレージに保存されている画像ファイルを MediaProvider から削除
     * 
     * @param cr        ContentResolver
     * @param filePath  削除する画像のファイルパス
     * @return  -1: エラー, 0以上: MediaProvider から削除された row の数
     */
    public static int deleteImageInternal(ContentResolver cr, String filePath) {
        return deleteImage(cr, filePath, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
    }
    
    /**
     * 画像ファイルを MediaProvider から削除
     * @return  -1: エラー, 0以上: MediaProvider から削除された row の数
     */
    private static int deleteImage(ContentResolver cr, String filePath, Uri storageUri) {
        
        int result = -1;
        
        if ((cr == null) || (filePath == null)) {
            return result;
        }
        
        // DATA が filePath と一致するレコードを検索
        Cursor cursor = cr.query(storageUri,
                                 new String[]{ MediaStore.Images.Media._ID },   // 取得対象 column
                                 MediaStore.Images.Media.DATA + "=?",           // 抽出条件
                                 new String[]{ filePath },                      // 抽出条件
                                 MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        
        if (cursor != null) {
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                Uri uri = ContentUris.appendId(storageUri.buildUpon(),
                                               cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))).build();
                result = cr.delete(uri, null, null);
            }
            
            cursor.close();
        }
        
        return result;
    }
    
    
    /*****************************************************************************************/
    /**  動画ファイルを MediaProvider に追加
    /*****************************************************************************************/
    /**
     * 外部ストレージに保存されている画像ファイルを MediaProvider に追加
     * 
     * @param   cr        ContentResolver
     * @param   filePath  保存ファイルパス
     * @param   mime      MIME TYPE
     * @param   duration  総再生時間
     * @return  null: エラー, それ以外: 生成された Content URI
     */
    public static Uri addVideoExternal(ContentResolver cr, String filePath, String mime, long duration) {
        return addVideo(cr, filePath, mime, duration, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
    }
    
    /**
     * 内部ストレージに保存されている画像ファイルを MediaProvider に追加
     * 
     * @param   cr        ContentResolver
     * @param   filePath  保存ファイルパス
     * @param   mime      MIME TYPE
     * @param   duration  総再生時間
     * @return  null: エラー, それ以外: 生成された Content URI
     */
    public static Uri addVideoInternal(ContentResolver cr, String filePath, String mime, long duration) {
        return addVideo(cr, filePath, mime, duration, MediaStore.Video.Media.INTERNAL_CONTENT_URI);
    }
    
    /**
     * 動画ファイルを MediaProvider に追加
     * @return  null: エラー, それ以外: 生成された Content URI
     */
    private static Uri addVideo(ContentResolver cr, String filePath, String mime, long duration, Uri storageUri) {
        
        if ((cr == null) || (filePath == null) || (mime == null)) {
            return null;
        }
        
        File file = new File(filePath);
        String fileName = file.getName();
        
        ContentValues contentValues = new ContentValues(9);
        long time = System.currentTimeMillis();
        
        contentValues.put(MediaStore.Video.Media.TITLE, fileName);
        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Video.Media.DATE_TAKEN, time);
        contentValues.put(MediaStore.Video.Media.DATE_ADDED, time/1000);
        contentValues.put(MediaStore.Video.Media.DATE_MODIFIED, time/1000);
        contentValues.put(MediaStore.Video.Media.MIME_TYPE, mime);
        contentValues.put(MediaStore.Video.Media.DATA, filePath);
        contentValues.put(MediaStore.Video.Media.SIZE, file.length());
        if (duration > 0) {
            contentValues.put(MediaStore.Video.Media.DURATION, Long.toString(duration));
        }

        return cr.insert(storageUri, contentValues);
    }
    
    
    /*****************************************************************************************/
    /**  動画ファイルを MediaProvider から削除
    /*****************************************************************************************/
    /**
     * 外部ストレージに保存されている動画ファイルを MediaProvider から削除
     * 
     * @param cr        ContentResolver
     * @param filePath  削除する動画のファイルパス
     * @return  -1: エラー, 0以上: MediaProvider から削除された row の数
     */
    public static int deleteVideoExternal(ContentResolver cr, String filePath) {
        return deleteVideo(cr, filePath, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
    }
    
    /**
     * 内部ストレージに保存されている動画ファイルを MediaProvider から削除
     * 
     * @param cr        ContentResolver
     * @param filePath  削除する動画のファイルパス
     * @return  -1: エラー, 0以上: MediaProvider から削除された row の数
     */
    public static int deleteVideoInternal(ContentResolver cr, String filePath) {
        return deleteVideo(cr, filePath, MediaStore.Video.Media.INTERNAL_CONTENT_URI);
    }
    
    /**
     * 動画ファイルを MediaProvider から削除
     * @return  -1: エラー, 0以上: MediaProvider から削除された row の数
     */
    private static int deleteVideo(ContentResolver cr, String filePath, Uri storageUri) {
        
        int result = -1;
        
        if ((cr == null) || (filePath == null)) {
            return result;
        }
        
        // DATA が filePath と一致するレコードを検索
        Cursor cursor = cr.query(storageUri,
                                 new String[]{ MediaStore.Video.Media._ID },    // 取得対象 column
                                 MediaStore.Video.Media.DATA + "=?",            // 抽出条件
                                 new String[]{ filePath },                      // 抽出条件
                                 MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        
        if (cursor != null) {
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                Uri uri = ContentUris.appendId(storageUri.buildUpon(),
                                               cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID))).build();
                result = cr.delete(uri, null, null);
            }
            
            cursor.close();
        }
        
        return result;
    }
    
    
    /*****************************************************************************************/
    /**  画像ファイルのサムネイルを Bitmap で取得
    /*****************************************************************************************/
    /**
     * 外部ストレージに保存されている画像のサムネイル取得
     * 
     * @param cr        ContentResolver
     * @param filePath  サムネイル取得対象の画像ファイルパス
     * @param size      サムネイルのサイズ (MediaStore.Images.Thumbnails.MINI_KIND or MICRO_KIND)
     * @return  null: サムネイル取得失敗, null以外: サムネイルのBitmap
     */
    public static Bitmap getImageThumbnailBitmapExternal(ContentResolver cr, String filePath, int size) {
        return getImageThumbnailBitmap(cr, filePath, size, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }
    
    /**
     * 内部ストレージに保存されている画像のサムネイル取得
     * 
     * @param cr        ContentResolver
     * @param filePath  サムネイル取得対象の画像ファイルパス
     * @param size      サムネイルのサイズ (MediaStore.Images.Thumbnails.MINI_KIND or MICRO_KIND)
     * @return  null: サムネイル取得失敗, null以外: サムネイルのBitmap
     */
    public static Bitmap getImageThumbnailBitmapInternal(ContentResolver cr, String filePath, int size) {
        return getImageThumbnailBitmap(cr, filePath, size, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
    }
    
    /**
     * 画像のサムネイルの取得
     * @return  null: サムネイル取得失敗, null以外: サムネイルのBitmap
     */
    private static Bitmap getImageThumbnailBitmap(ContentResolver cr, String filePath, int size, Uri storageUri) {
        Bitmap thumbnail = null;
        
        if ((cr == null) || (filePath == null)) {
            return null;
        }
        
        if ((size != MediaStore.Images.Thumbnails.MINI_KIND) && (size != MediaStore.Images.Thumbnails.MICRO_KIND)) {
            size = MediaStore.Images.Thumbnails.MINI_KIND;
        }
        
        Cursor cursor = cr.query(storageUri,
                                 new String[]{ MediaStore.Images.Media._ID },   // 取得対象 column
                                 MediaStore.Images.Media.DATA + "=?",           // 抽出条件
                                 new String[]{ filePath },                      // 抽出条件
                                 MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        
        if (cursor != null) {
            int count = cursor.getCount();
            int index = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            int[] videoIds = new int[count];
            
            cursor.moveToFirst();
            for (int i = 0; i < count; i++) {
                videoIds[i] = cursor.getInt(index);
                
                // サムネイルの Bitmap 取得
                thumbnail = MediaStore.Images.Thumbnails.getThumbnail(cr, videoIds[i], size, null);
                
                if (thumbnail != null) {
                    break;
                }
                cursor.moveToNext();
            }
            
            cursor.close();
        }
        
        return thumbnail;
    }
    
    
    /*****************************************************************************************/
    /**  動画ファイルのサムネイルを Bitmap で取得
    /*****************************************************************************************/
    /**
     * 外部ストレージに保存されている動画のサムネイル取得
     * 
     * @param cr        ContentResolver
     * @param filePath  サムネイル取得対象の動画ファイルパス
     * @param size      サムネイルのサイズ (MediaStore.Video.Thumbnails.MINI_KIND or MICRO_KIND)
     * @return  null: サムネイル取得失敗, null以外: サムネイルのBitmap
     */
    public static Bitmap getVideoThumbnailBitmapExternal(ContentResolver cr, String filePath, int size) {
        return getVideoThumbnailBitmap(cr, filePath, size, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
    }
    
    /**
     * 内部ストレージに保存されている動画のサムネイル取得
     * 
     * @param cr        ContentResolver
     * @param filePath  サムネイル取得対象の動画ファイルパス
     * @param size      サムネイルのサイズ (MediaStore.Video.Thumbnails.MINI_KIND or MICRO_KIND)
     * @return  null: サムネイル取得失敗, null以外: サムネイルのBitmap
     */
    public static Bitmap getVideoThumbnailBitmapInternal(ContentResolver cr, String filePath, int size) {
        return getVideoThumbnailBitmap(cr, filePath, size, MediaStore.Video.Media.INTERNAL_CONTENT_URI);
    }
    
    /**
     * 動画のサムネイルの取得
     * @return  null: サムネイル取得失敗, null以外: サムネイルのBitmap
     */
    private static Bitmap getVideoThumbnailBitmap(ContentResolver cr, String filePath, int size, Uri storageUri) {
        Bitmap thumbnail = null;
        
        if ((cr == null) || (filePath == null)) {
            return null;
        }
        
        if ((size != MediaStore.Video.Thumbnails.MINI_KIND) && (size != MediaStore.Video.Thumbnails.MICRO_KIND)) {
            size = MediaStore.Video.Thumbnails.MINI_KIND;
        }
        
        Cursor cursor = cr.query(storageUri,
                                 new String[]{ MediaStore.Video.Media._ID },    // 取得対象 column
                                 MediaStore.Video.Media.DATA + "=?",            // 抽出条件
                                 new String[]{ filePath },                      // 抽出条件
                                 MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        
        if (cursor != null) {
            int count = cursor.getCount();
            int index = cursor.getColumnIndex(MediaStore.Video.Media._ID);
            int[] videoIds = new int[count];
            
            cursor.moveToFirst();
            for (int i = 0; i < count; i++) {
                videoIds[i] = cursor.getInt(index);
                
                // サムネイルの Bitmap 取得
                thumbnail = MediaStore.Video.Thumbnails.getThumbnail(cr, videoIds[i], size, null);
                
                if (thumbnail != null) {
                    break;
                }
                cursor.moveToNext();
            }
            
            cursor.close();
        }
        
        return thumbnail;
    }


}
