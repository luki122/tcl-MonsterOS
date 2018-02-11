package cn.tcl.music.model.live;

import java.io.Serializable;

/**
 * Created by dongdong.huang on 2015/11/5.
 * 艺人bean类
 */
public class ArtistBean extends BaseSong implements Serializable {
    public String artist_id = "";//	int	是	135	艺人id, BIGINT类型
    public String artist_name = "";//	string	是	陈奕迅	艺人名字
    public String artist_logo = "";//	string	是	http://img.xiami.net/images/artistlogo/82/13832967518882.jpg	艺人头像
    public String count_likes = "";//	int	是	100	粉丝数
    public String area = "";//	string	是	大陆	地区
    public String english_name = "";//	string	是	Eason Chan	英文名
    public String recommends = "";//	int	是		分享数
    public String gender = "";//	string	是		性别, M 男性, F 女性, B 乐队
    public String category = "";//	int	是		艺人类别
    public String description = "";//	string	是	liudehua	描述信息
    public int albums_count=0;//可试听专辑数量
    public int songs_count=0;//可视听歌曲数量
    public String sort_title = "A"; //字母排序

    @Override
    public String toString() {
        return "ArtistBean{" +
                "artist_id='" + artist_id + '\'' +
                ", artist_name='" + artist_name + '\'' +
                ", artist_logo='" + artist_logo + '\'' +
                ", count_likes='" + count_likes + '\'' +
                ", area='" + area + '\'' +
                ", english_name='" + english_name + '\'' +
                ", recommends='" + recommends + '\'' +
                ", gender='" + gender + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", albums_count=" + albums_count +
                ", songs_count=" + songs_count +
                ", sort_title=" + sort_title +
                '}';
    }
}
