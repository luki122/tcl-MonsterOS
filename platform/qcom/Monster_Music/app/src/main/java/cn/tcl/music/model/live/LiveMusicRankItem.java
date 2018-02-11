package cn.tcl.music.model.live;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dongdong.huang on 2015/11/5.
 * 榜单列表元素item
 */
public class LiveMusicRankItem extends BaseSong implements Serializable {
    public String title	= "";//string	是	中国好声音原唱榜	标题
    public String logo	= "";//	string	是	http://img.xiami.net/images/common/uploadpic/16/14044714166072.jpg	logo
    public String logo_middle	= "";//	string	是	http://img.xiami.net/images/common/uploadpic/16/14044714166072.jpg	logo
    public String type	= "";//	string	是	chinavoice_b	类型
    public String cycle_type	= "";//	string	是	daily	周期类型：daily为每日，weekly为每周，none为没有
    public String update_date	= "";//	string	是	每天更新	更新日期
    public List<SongDetailBean> songs;//	array	否

    //榜单详情接口添加
    public String object_id = "";
    public int time;
    public String sign_id = "";
    public String cont = "";
    public int total;
    public String song_changed = "";
    public String more = "";

}
