package cn.tcl.music.model.live;

import java.io.Serializable;

public class XiamiMemberInfo extends BaseSong implements Serializable{
    public long user_id;
    public String nick_name;
    public String avatar;
    public String gender;
    public String description;
    public long gmt_create;
    public String signature;
    public int fans;
    public int followers;
    public int listens;
    public int collect_count;
    public boolean is_vip;
    public long vip_begin;
    public long vip_finish;
    public boolean is_self;
    public boolean friendship;
}



//        user_id 	int 	是 	9 	用户id, BIGINT类型
//        nick_name 	string 	是 	南瓜 	用户昵称
//        avatar 	string 	是 	http://img.xiami.net/images/avatar_new/288/56/14428011/14428011_1369639103_2.jpg 	用户头像
//        gender 	string 	是 	M 	性别 M:男，F：女，S：保密，N:未知
//        description 	string 	是 	浙江 	省份
//        gmt_create 	int 	是 	1377676603 	创建时间
//        signature 	string 	是 	用户签名 	用户签名
//        fans 	int 	是 	1000 	粉丝数
//        followers 	int 	是 	1000 	关注数
//        listens 	int 	是 	1000 	累计播放数
//        collect_count 	int 	是 	1000 	收藏精选集数量
//        is_vip 	bool 	是 		是否是vip
//        vip_begin 	int 	是 	1408676603 	vip开始时间
//        vip_finish 	int 	是 	1408676603 	vip结束时间
//        is_self 	bool 	否 	false 	是否自己
//        friendship 	int 	否 	1 	好友关系0：未关注，1：互相关注，2对方关注我，3我关注对方