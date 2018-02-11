package cn.tcl.music.network;

/**
 * 2015-11-03
 */
public class DataRequest {
    /**
     * 结果返回状态
     */
    public static class Code {
        public static final int CODE_LOAD_SUCCESS = 0;
        public static final int CODE_LOAD_FAIL = 1;
    }

    /**
     * 网络请求类型
     */
    public static class Type {
        public static final int TYPE_LIVE_BANNER = 0;//banner
        public static final int TYPE_LIVE_LATEST = 1;//新碟首发
        public static final int TYPE_LIVE_RANK = 2;  //排行榜
        public static final int TYPE_LIVE_SINGER = 3;//歌手
        public static final int TYPE_LIVE_RADIO = 4; //电台
        public static final int TYPE_LIVE_RECOMMEND = 5;//热门推荐
        public static final int TYPE_LIVE_ESSENCE = 6;//精选合集
        public static final int TYPE_LIVE_SONG_DETAIL = 7;//歌曲详情
        public static final int TYPE_LIVE_ARTIST_DETAIL = 8;//艺人详情页
        public static final int TYPE_LIVE_ARTIST_HOT_SONGS = 9;//批量获取歌曲基本信息接口
        public static final int TYPE_LIVE_ARTIST_ALBUMS = 10;//艺人的专辑列表
        public static final int TYPE_LIVE_RADIO_CATEGORES = 11;//电台类别
        public static final int TYPE_LIVE_COLLECT_DETAIL = 12;//精选集详情接口
        public static final int TYPE_LIVE_HOT_SEARCH = 13;//搜索热词
        public static final int TYPE_LIVE_RANK_DETAIL = 14;// 排行榜详情
        public static final int TYPE_LIVE_ALL_SEARCH = 15;//综合搜索
        public static final int TYPE_LIVE_SEARCH_SINGER = 16;//搜索歌手
        public static final int TYPE_LIVE_SEARCH_SONG = 17;//搜索歌曲
        public static final int TYPE_LIVE_SEARCH_ALBUM = 18;//搜索专辑
        public static final int TYPE_LIVE_SEARCH_COLLECT = 21;//搜索精选集
        public static final int TYPE_LIVE_ALBUM_DETAIL = 19;//专辑详情页
        public static final int TYPE_LIVE_RADIO_DETAIL = 20;//电台详情
        public static final int TYPE_LIVE_RECOMMEND_DAILY = 21;//每日推荐
        public static final int TYPE_LIVE_HEART_RADIO = 22;//心情电台
        public static final int METHOD_SEARCH_MATCH_SONGS = 23;//匹配歌曲

        public static final int TYPE_LIVE_SCENE_BANNER = 24;
        public static final int TYPE_LIVE_HOT_ALBUM = 25;
        public static final int TYPE_COLLECTION_HOT_TAG = 26;
        public static final int TYPE_LIVE_ADD_COLLECTS = 27;//收藏在线精选集
        public static final int TYPE_LIVE_AUTO_TIPS = 28;
        public static final int TYPE_LIVE_ACCOUNT_LOGIN = 29;
        public static final int TYPE_LIVE_COLLECT_LIST = 30;//我收藏的精选集
        public static final int TYPE_LIVE_REMOVE_COLLECTS = 31;//取消收藏在线的精选集
        public static final int TYPE_LIVE_GET_COLLECTS_FAVORITE = 32;//我收藏的精选集
        public static final int TYPE_GET_MEMBER_INFO = 33;      //获取虾米用户信息
        public static final int TYPE_GUESS_RADIO = 34;//虾米猜电台.
        public static final int TYPE_LIVE_ADD_ALBUMS = 35;//收藏在线专辑
        public static final int TYPE_LIVE_REMOVE_ALBUMS = 36;//取消收藏在线专辑
        public static final int TYPE_LIVE_GET_ALBUMS_FAVORITE = 37;//判断用户收藏的专辑
    }

    /**
     * 网络请求接口方法
     */
    public static class Method {
        public static final String METHOD_LIVE_BANNER = "mobile.sdk-image";//banner 在线音乐"song.detail";//music.start-yunos
        public static final String METHOD_LIVE_LATEST = "rank.promotion-albums";//新碟首发 或 song.list
        public static final String METHOD_LIVE_RANK = "rank.list";  //排行榜
        public static final String METHOD_LIVE_SINGER = "artist.wordbook";//歌手
        public static final String METHOD_LIVE_RADIO = "radio.list";//电台
        public static final String METHOD_LIVE_RADIO_DETAIL = "radio.detail-by-type-id";//电台详情
        public static final String METHOD_LIVE_RECOMMEND = "recommend.hot-songs";//热门推荐
        public static final String METHOD_LIVE_ESSENCE = "collect.recommend";//精选合集
        public static final String METHOD_LIVE_SONG_DETAIL = "song.detail";//歌曲详情
        public static final String METHOD_LIVE_ARTIST_DETAIL = "artist.detail";//艺人详情页
        public static final String METHOD_LIVE_ARTIST_HOT_SONGS = "artist.hot-songs";//批量获取歌曲基本信息接口
        public static final String METHOD_LIVE_ARTIST_ALBUMS = "artist.albums";//艺人的专辑列表

        public static final String METHOD_LIVE_RADIO_CATEGORIES = "radio.categories";//电台类别
        public static final String METHOD_LIVE_COLLECT_DETAIL = "collect.detail";//精选集详情接口
        public static final String METHOD_LIVE_SEARCH_HOT_WORDS = "search.hot-words";//	搜索热词
        public static final String METHOD_LIVE_RANK_DETAIL = "rank.detail";// 排行榜详情
        public static final String METHOD_LIVE_SEARCH_ALL = "search.all";//	综合搜索接口
        public static final String METHOD_LIVE_SEARCH_SINGER = "search.artists";//	搜索歌手
        public static final String METHOD_LIVE_SEARCH_SONG = "search.songs";//	搜索	歌曲
        public static final String METHOD_LIVE_SEARCH_ALBUM = "search.albums";//	搜索	专辑
        public static final String METHOD_LIVE_SEARCH_COLLECT = "search.collects";//	搜索	精选集
        public static final String METHOD_LIVE_ALBUM_DETAIL = "album.detail";//专辑详情页
        public static final String METHOD_SEARCH_MATCH_SONGS = "search.match-songs";//精确匹配歌曲
        public static final String SEARCH_AUTO_TIPS = "search.auto-tips";//搜索联想自动提示
        public static final String METHOD_LIVE_RECOMMEND_DAILY = "recommend.daily-songs";//每日推荐歌曲
        public static final String METHOD_LIVE_ADD_COLLECTS = "library.add-collects";//收藏在线精选集
        public static final String METHOD_LIVE_RECOMMEND_SCENE = "radio.scene";//场景音乐列表
        public static final String METHOD_LIVE_ACCOUNT_LOGIN = "account.sdk-login";
        public static final String METHOD_LIVE_COLLECT_LIST = "library.collect-list";//我收藏的精选集
        public static final String METHOD_LIVE_REMOVE_COLLECTS = "library.remove-collects";//取消收藏在线的精选集
        public static final String METHOD_LIVE_GET_COLLECTS_FAVORITE = "library.get-collects-favorite";//判断用户收藏的精选集
        public static final String METHOD_GET_MEMBER_INFO = "member.info";  //获取用户资料
        public static final String METHOD_GUESS_RADIO = "radio.guess";//虾米猜电台
        public static final String METHOD_LIVE_ADD_ALBUMS = "library.add-albums";//收藏在线专辑
        public static final String METHOD_LIVE_REMOVE_ALBUMS = "library.remove-albums";//取消收藏在线专辑
        public static final String METHOD_LIVE_GET_ALBUMS_FAVORITE = "library.get-albums-favorite";//判断用户收藏的专辑
    }
}
