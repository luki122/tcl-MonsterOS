package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dongdong.huang on 2015/11/5.
 * 歌碟bean类
 */
public class AlbumBean extends BaseSong implements Serializable {
    public String album_id = "";//	int	是	615067792	歌曲ID, BIGINT类型
    public String album_name = "";//	string	是	战斗吧！	专辑名称
    public String album_logo = "";//	string	是	http://img.xiami.net/images/album/img67/906621467/6150677921416484101_2.png	专辑LOGO
    public String artist_id = "";//	int	是	135	艺人id, BIGINT类型
    public String artist_name = "";//	string	是	陈奕迅	艺人名
    public String artist_logo = "";//	string	是		艺人LOG
    public String gmt_publish;//	int	是	1416384868	发布时间戳
    public int song_count;//	int	是	100	歌曲总数
    public String grade = "";//	number	是	3.2	评分
    public String album_category = "";//	string	是	EP	专辑类型
    public String company = "";//	string	是	BIG Machine	发行公司
    public String language = "";//	string	是	英语	专辑语种
    public int cd_count;//	int	是	1	专辑CD数
    public String description = "";//	string	是
    public List<SongDetailBean> songs;//歌碟详情添加


    @Override
    public String toString() {
        return "AlbumBean{" +
                "album_id='" + album_id + '\'' +
                ", album_name='" + album_name + '\'' +
                ", album_logo='" + album_logo + '\'' +
                ", artist_id='" + artist_id + '\'' +
                ", artist_name='" + artist_name + '\'' +
                ", artist_logo='" + artist_logo + '\'' +
                ", gmt_publish='" + gmt_publish + '\'' +
                ", song_count=" + song_count +
                ", grade='" + grade + '\'' +
                ", album_category='" + album_category + '\'' +
                ", company='" + company + '\'' +
                ", language='" + language + '\'' +
                ", cd_count=" + cd_count +
                ", description='" + description + '\'' +
                '}';
    }
}
