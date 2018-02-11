package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dongdong.huang on 2015/11/5.
 * 精选集bean类
 */
public class CollectionBean extends BaseSong implements Serializable {
    public String list_id = "";// 	int	是	32755005	精选集ID, BIGINT类型
    public String collect_name = "";//	string	是	电子柔情 - 冥想催眠致幻系	精选集名称
    public String collect_logo = "";//	string	是	http://img.xiami.net/images/collect/5/5/32755005_1416305260_pZPV_4.jpeg	精选集LOGO, 没有为空
    public int song_count;//	int	是	100	歌曲总数
    public String user_name = "";//	string	是	念安娜 创作的精选集	用户昵称
    public String author_avatar = "";//	string	是	http://img.xiami.net/images/collect/159/59/7484159_1317343602_1.jpg	头像
    public String play_count;//	int	是	100	播放次数
    public String user_id = "";//	int	是	7674936	用户id, BIGINT类型
    public int gmt_create;//	int	是	7674936	创建时间
    public String description = "";//	string	是	111	描述信息
    public List<String> tag_array;//精选集标签 元素类型为string
    public List<String> tags;//精选集标签 元素类型为string
    public List<SongDetailBean> songs;//歌曲信息 元素类型为object, 查看元素定义
    public int favorite;

    @Override
    public String toString() {

        return  "CollectionBean{" +
                "list_id=" + list_id +
                ", collect_name='" + collect_name + '\'' +
                ", collect_logo='" + collect_logo + '\'' +
                ", song_count='" + song_count + '\'' +
                ", user_name='" + user_name + '\'' +
                ", author_avatar='" + author_avatar + '\'' +
                ", play_count='" + play_count + '\'' +
                ", user_id='" + user_id + '\'' +
                ", gmt_create='" + gmt_create + '\'' +
                ", description='" + description + '\'' +
                ", tags='" + tags + '\'' +
                ", favorite='" + favorite + '\'' +
                ", tag_array=" + tag_array +
                '}';
    }
}
