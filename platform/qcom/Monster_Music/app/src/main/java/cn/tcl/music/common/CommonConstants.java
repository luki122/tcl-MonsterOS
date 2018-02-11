package cn.tcl.music.common;


/**
 * 全局常量文件
 */
public class CommonConstants {
    //bundle key
    /**************** folder begin   *****************/
    /** 文件夹界面文件夹数量 **/
    public static final String KEY_LOCAL_MEDIA_FOLDERS_NUM = "key_local_media_folders_num";
    /** 文件夹界面歌曲数量 **/
    public static final String KEY_LOCAL_MEDIA_FOLDERS_SONGS_NUM = "key_local_media_folders_songs_num";

    public static final String BUNDLE_KEY_FOLDER_ID = "folderId";
    public static final String BUNDLE_KEY_FOLDER_NAME = "folderName";
    public static final String BUNDLE_KEY_FOLDER_SONG_NUM = "songNum";
    public static final String BUNDLE_KEY_FOLDER_IS_SCAM = "is_scan";
    public static final String BUNDLE_KEY_FOLDER_TYPE = "folderType";
    public static final int VALUE_FOLDER_IS_NOT_SCAN = 0;                   //the value of folder is ignore
    public static final int VALUE_FOLDER_IS_SCAN = 1;                       //the value of folder is scan

    public static final int VIRTUAL_ONLINE_FOLDER_ID = 1000000001;          //the virtual id of online folder
    public static final String VIRTUAL_ONLINE_FOLDER_PATH = "/storage/online_virtual";
    public static final String VIRTUAL_ONLINE_FOLDER_NAME = "online_virtual";
    /**************** folder end   *****************/

    /**************** online search begin   *****************/
    public static final int ONLINE_SEARCH_PAGE = 0;
    /**************** online search end   *****************/

    /**************** album begin   *****************/
    public static final String BUNDLE_KEY_ALBUM_ID = "albumId";
    public static final String BUNDLE_KEY_ALBUM_NAME = "albumName";
    public static final String BUNDLE_KEY_ARTWORK = "artwork";
    /**************** album end   *****************/

    /**************** artist begin   *****************/
    public static final String BUNDLE_KEY_ARTIST = "artist";
    public static final String BUNDLE_KEY_ARTIST_ID = "artistId";

    public final static String SECTIONS_NAVIGATION = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
    public final static String LETTER_STRING = "abcdefghijklmnopqrstuvwxyz";
    public static final int TAG_HEAD_VIEW = 1;
    public static final int TAG_CONTENT_VIEW = 2;
    /**************** artist end   *****************/

    /**************** playlist detail begin   *****************/
    public static final String BUNDLE_KEY_PLAYLIST_TITLE = "playlist";
    public static final String BUNDLE_KEY_PLAYLIST_ID = "playlistId";
    public static final String BUNDLE_KEY_PLAYLIST_JUMP_FLAG = "flag";
    public static final String BUNDLE_KEY_PLAYLIST_ADD_FLAG = "addto";
    public static final String BUNDLE_KEY_FAVORITE_ADD_FLAG = "addtofavorite";
    public static final int VALUE_PLAYLIST_FLAG_IMPORT_MEDIA = 1;
    public static final int TYPE_PLAYLIST_RECENTLY_PLAY = 1;        //type for recent
    public static final int TYPE_PLAYLIST_CREATED = 0;              //type for user create
    /**************** playlist detail end   *****************/

    /**************** queue begin   *****************/
    public static final long VALUE_CURRENT_MEDIA_ID_NULL = -1;      //当前无播放歌曲，当前播放歌曲id为-1
    public static final int VALUE_QUEUE_IS_NOT_EFFECTIVE = 0;
    public static final int VALUE_QUEUE_IS_EFFECTIVE = 1;           //the value of item in queue is effective
    public static final int VALUE_QUEUE_IS_AUTO_ADDED = 1;
    public static final int VALUE_QUEUE_IS_NOT_AUTO_ADDED = 0;
    /**************** queue end   *****************/

    /**************** detail begin   *****************/
    /** 详情界面跳转来源类型 **/
    public static final String BUNDLE_KEY_DETAIL_TYPE = "detailType";
    public static final int DETAIL_TYPE_ALBUM = 1;      //专辑
    public static final int DETAIL_TYPE_PLAYLIST = 2;   //歌单
    /**************** detail begin   *****************/

    /**************** media begin   *****************/
    public static final int VALUE_MEDIA_IS_FAVORITE = 1;
    public static final int VALUE_MEDIA_IS_NOT_FAVORITE = 0;
    public static final int VALUE_MEDIA_NOT_DOWNLOAD = 0;
    public static final int VALUE_MEDIA_DOWNLOADED = 1;
    /**************** media end   *****************/

    /**************** scenes begin *****************/
    public static final String BUNDLE_KEY_SCENE = "sceneBean";
    public static final int SCENES_RUN_ID = 10000;
    public static final int SCENES_STUDY_ID = 10001;
    public static final int SCENES_WORK_ID = 10002;
    public static final int SCENES_DRIVE_ID = 10003;
    public static final int SCENES_TRAVEL_ID = 10004;
    public static final int SCENES_SLEEP_ID = 10005;
    public static final int SCENES_GATHER_ID = 10006;
    public static final int SCENES_TEA_ID = 10007;
    public static final int SCENES_CLUB_ID = 10008;
    public static final int SCENES_OTHER_ID = 10009;
    /**************** scenes end *****************/


    /**************** select music begin *****************/
    public static final String BUNDLE_KEY_SELECT_SONGS_FROM = "from";           //differ from where to goto select
    public static final int VALUE_SELECT_SONGS_FROM_IMPORT = 1;
    public static final String ADD_SUCCESS_KEY = "add_success";
    public static final String SELECT_SONGS_ADD = "select_songs_add";
    public static final int SELECT_SONGS_TO_PLAYLIST = 1;
    public static final int SELECT_SONGS_TO_FAVORITE = 2;
    /**************** select music end *****************/

    /**************** alpha begin *****************/
    public static final float VIEW_USER_INTERFACE_ENABLE_ALPHA = 1;
    public static final float VIEW_LOCAL_SELECTER_TITLE_ALPHA = 0.86f;
    public static final float VIEW_USER_INTERFACE_DISABLE_ALPHA = 0.3f;
    public static final float VIEW_LOCAL_NO_SELECTER_TITLE_ALPHA  = 0.4f;
    /**************** alpha end *****************/

    /**************** recent play song limit begin*****************/
    public static final int RECENT_PLAY_SONG_SIZE_LIMIT = 100;
    /**************** recent play song limit end*****************/

    /**************** broadcast begin*****************/
    /** 广播--刷新当前播放歌曲的信息 **/
    public static final String BROADCAST_REFRESH_CURRENT_PLAYING_MEDIAINFO = "broadcast_refresh_current_playing_mediainfo";
    /** 广播--删除本地歌曲 **/
    public static final String BROADCAST_DELETE_LOCAL_MEDIA_ITEM = "broadcast_delete_local_media_item";
    /** 广播--文件夹忽略与恢复扫描操作 **/
    public static final String BROADCAST_IGNORE_OR_RECOVER_FOLDER = "broadcast_ignore_or_recover_folder";
    /**************** broadcast end*****************/

    /**************** databse begin*****************/
    public static final String RANDOMIZE = "randomize";                         //call action for queue random
    public static final String BULK_UPDATE_WITH_ID = "bulk_update_with_id";     //call action for update id
    public static final String ADD_NEW_TRACKS = "add_to_last_added";            //call action for add new tracks
    public static final long RECENTLY_PLAYED_PLAYLIST_ID = 2;                   //recent playlist id
    /**************** databse end*****************/

    /**************** music src type begin*****************/
    public final static int SRC_TYPE_LOCAL = 0;
    public final static int SRC_TYPE_MYMIX = 1;
    public final static int SRC_TYPE_RDIO = 2;
    public final static int SRC_TYPE_DEEZER = 3;
    public final static int SRC_TYPE_DEEZERRADIO = 4;
    public final static int SRC_TYPE_DOWNLOADED = 5;//已下载歌曲
    /**************** music src type end *****************/

    /**************** recyclerview type begin*****************/
    public final static int ITEM_TYPE_CONTENT = 0;
    public final static int ITEM_TYPE_HEADER = 1;
    /**************** recyclerview type end *****************/

    /**************** application begin*****************/
    public static final int MINUTE_TO_SECOND = 60;
    public static final int SECOND_MILLISECOND = 1000;
    /**************** application end *****************/

    /**************** settings activity begin*****************/
    public static final int NO_OPEN = 0;
    public static final int OPEN = 100;
    public static final int MINUTE_TO_MILLISECOND = 60 * 1000;
    public static final int COUNT_DOWN_TIME_DIALOG_ID = 1;
    /**************** settings activity end *****************/

    /**************** command to control service begin *****************/
    public final static String COMMAND_TO_SERVICE = "cn.tcl.music.command";
    public final static String COMMAND_PLAY = "cn.tcl.music.play";
    public final static String COMMAND_PAUSE ="cn.tcl.music.pause";
    public final static String COMMAND_STOP = "cn.tcl.music.stop";
    public final static String COMMAND_PREV = "cn.tcl.music.pre";
    public final static String COMMAND_NEXT = "cn.tcl.music.next";
    public final static String COMMAND_TOGGLE_PLAY_OR_PAUSE = "cn.tcl.music.toggle.playorpause";
    public final static String COMMAND_PLAY_A_SONG = "cn.tcl.music.playasong";
    public final static String COMMAND_PLAY_A_SONG_MEDIA_ID = "cn.tcl.tcl.play.id";
    public final static String COMMAND_CLOSE_NOTIFICAITON = "cn.tcl.music.closenotification";
    /**************** command to control service end *****************/

    /**************** data in preference to mark last state for service begin *****************/
    public final static String PREFERENCE_SAVED_MEDIA_ID = "cn.tcl.music.media_id";
    public final static String PREFERENCE_SAVED_CURRENT_POSITION = "cn.tcl.music.position";
    /**************** data in preference to mark last state  for service end *****************/


    /**************** the state of media player in service begin *****************/
    public static final int PLAY_STATE_IDLE = 0;
    public static final int PLAY_STATE_PLAYING = 1;
    public static final int PLAY_STATE_PAUSE = 2;
    public static final int PLAY_STATE_STOP = 3;
    public static final int PLAY_STATE_PREPARING = 4;
    public static final int PLAY_STATE_RESET = 5;
    /**************** the state of media player in service end***********/


    /**************** the state of media player in service begin *****************/

    public static final String META_CHANGED = "cn.tcl.music.metachanged";
    public static final String PLAY_STATE_CHANGED = "cn.tcl.music.playstatechanged";
    /**************** the state of media player in service end *****************/

    /**************** url of online xiami music begin *****************/
    public static final String URL_SONG_SHARE = "http://h.xiami.com/song.html?id=";
    public static final String URL_ALBUM_SHARE = "http://h.xiami.com/album_detail.html?id=";
    public static final String URL_ARTIST_SHARE = "http://h.xiami.com/artist_detail.html?id=";
    public static final String URL_COLLECT_SHARE = "http://h.xiami.com/collect_detail.html?id=";
    /**************** url of online xiami music end *****************/

    /**************** Load data status begin *****************/
    public static final int PENDING = 0;
    public static final int RUNNING = 1;
    public static final int SUCCESS = 2;
    public static final int FAIL = 3;
    /**************** Load data status end *****************/

    /**************** the type of live rank begin *****************/
    public static final String LIVE_RANK_CATE_XIAMI_WECHAT = "xiami_wechat";
    public static final String LIVE_RANK_CATE_MUSIC_ORIGINAL = "music_original";
    public static final String LIVE_RANK_CATE_MUSIC_COLLECT = "xiami_collect";
    public static final String LIVE_RANK_CATE_XIAMI_WEIBO = "xiami_weibo";
    /**************** the type of live rank end***********/

    /**************** data saver begin *****************/
    public static final int SHOW_AGAIN = 0;
    public static final int NEVER_SHOW_AGAIN = 1;
    /**************** data saver end *****************/

    /**************** state of onLinePlayList begin *****************/
    public static final String LIST_ID = "list_id";
    public static final String COLLECT_NAME = "collect_name";
    public static final String COLLECT_SONG_COUNT = "collect_song_count";
    /**************** state of onLinePlayList end *****************/
}
