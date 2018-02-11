package cn.tcl.music.model.live;

import java.io.Serializable;

public class SongAllDetailBean extends BaseSong implements Serializable {
    public String song_id;
    public String song_name;
    public String artist_id;
    public String artist_name;
    public String artist_logo;
    public String singers;
    public String album_id;
    public String album_name;
    public String album_logo;
    public int pace;
    public int length;
    public String track;
    public String cd_serial;
    public String music_type;
  //  public int permission;//是否提供服务: 0:正常, 1:不提供服务, 2:需要VIP
    public String lyric_type;
    public String lyric;
    public int rate;
//    public int permission;//是否提供服务: 0:正常, 1:不提供服务, 2:需要VIP
//    public int change;//大于0为上升，小于0为下降;大于1000为new
//    public int sourceType;


    @Override
    public String toString() {
        return "SongDetailBean{" +
                "song_id='" + song_id + '\'' +
                ", song_name='" + song_name + '\'' +
                ", artist_id='" + artist_id + '\'' +
                ", artist_name='" + artist_name + '\'' +
                ", artist_logo='" + artist_logo + '\'' +
                ", singers='" + singers + '\'' +
                ", album_id='" + album_id + '\'' +
                ", album_name='" + album_name + '\'' +
                ", album_logo='" + album_logo + '\'' +
                ", length=" + length +
                ", track='" + track + '\'' +
                ", cd_serial='" + cd_serial + '\'' +
                ", music_type='" + music_type + '\'' +
                ", lyric_type='" + lyric_type + '\'' +
                ", lyric='" + lyric + '\'' +
                '}';
    }
}
