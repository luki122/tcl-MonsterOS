package cn.tcl.music.model.live;

import java.io.Serializable;

public class SongDetailBean extends BaseSong implements Serializable {
        public String song_id;
        public String song_name;
        public String artist_id;
        public String artist_name;
        public String artist_logo;
        public String singers;
        public String album_id;
        public String album_name;
        public String album_logo;
        public int length;
        public String track;
        public String cd_serial;
        public String music_type;
        public String listen_file;
        public String quality;
        public String expire;
        public String play_volume;
        public String lyric_type;
        public String lyric;
//    public int permission;//是否提供服务: 0:正常, 1:不提供服务, 2:需要VIP
        public int change;//大于0为上升，小于0为下降;大于1000为new
        public int sourceType;


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
                ", listen_file='" + listen_file + '\'' +
                ", quality='" + quality + '\'' +
                ", expire='" + expire + '\'' +
                ", play_volume='" + play_volume + '\'' +
                ", lyric_type='" + lyric_type + '\'' +
                ", lyric='" + lyric + '\'' +
                ", change=" + change +
                ", sourceType=" + sourceType +
                '}';
    }

        @Override
        public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SongDetailBean)) return false;

        SongDetailBean that = (SongDetailBean) o;

        if (length != that.length) return false;
        if (change != that.change) return false;
        if (sourceType != that.sourceType) return false;
        if (song_id != null ? !song_id.equals(that.song_id) : that.song_id != null) return false;
        if (song_name != null ? !song_name.equals(that.song_name) : that.song_name != null)
            return false;
        if (artist_id != null ? !artist_id.equals(that.artist_id) : that.artist_id != null)
            return false;
        if (artist_name != null ? !artist_name.equals(that.artist_name) : that.artist_name != null)
            return false;
        if (artist_logo != null ? !artist_logo.equals(that.artist_logo) : that.artist_logo != null)
            return false;
        if (singers != null ? !singers.equals(that.singers) : that.singers != null) return false;
        if (album_id != null ? !album_id.equals(that.album_id) : that.album_id != null)
            return false;
        if (album_name != null ? !album_name.equals(that.album_name) : that.album_name != null)
            return false;
        if (album_logo != null ? !album_logo.equals(that.album_logo) : that.album_logo != null)
            return false;
        if (track != null ? !track.equals(that.track) : that.track != null) return false;
        if (cd_serial != null ? !cd_serial.equals(that.cd_serial) : that.cd_serial != null)
            return false;
        if (music_type != null ? !music_type.equals(that.music_type) : that.music_type != null)
            return false;
        if (listen_file != null ? !listen_file.equals(that.listen_file) : that.listen_file != null)
            return false;
        if (quality != null ? !quality.equals(that.quality) : that.quality != null) return false;
        if (expire != null ? !expire.equals(that.expire) : that.expire != null) return false;
        if (play_volume != null ? !play_volume.equals(that.play_volume) : that.play_volume != null)
            return false;
        if (lyric_type != null ? !lyric_type.equals(that.lyric_type) : that.lyric_type != null)
            return false;
        return lyric != null ? lyric.equals(that.lyric) : that.lyric == null;

    }

        @Override
        public int hashCode() {
        int result = song_id != null ? song_id.hashCode() : 0;
        result = 31 * result + (song_name != null ? song_name.hashCode() : 0);
        result = 31 * result + (artist_id != null ? artist_id.hashCode() : 0);
        result = 31 * result + (artist_name != null ? artist_name.hashCode() : 0);
        result = 31 * result + (artist_logo != null ? artist_logo.hashCode() : 0);
        result = 31 * result + (singers != null ? singers.hashCode() : 0);
        result = 31 * result + (album_id != null ? album_id.hashCode() : 0);
        result = 31 * result + (album_name != null ? album_name.hashCode() : 0);
        result = 31 * result + (album_logo != null ? album_logo.hashCode() : 0);
        result = 31 * result + length;
        result = 31 * result + (track != null ? track.hashCode() : 0);
        result = 31 * result + (cd_serial != null ? cd_serial.hashCode() : 0);
        result = 31 * result + (music_type != null ? music_type.hashCode() : 0);
        result = 31 * result + (listen_file != null ? listen_file.hashCode() : 0);
        result = 31 * result + (quality != null ? quality.hashCode() : 0);
        result = 31 * result + (expire != null ? expire.hashCode() : 0);
        result = 31 * result + (play_volume != null ? play_volume.hashCode() : 0);
        result = 31 * result + (lyric_type != null ? lyric_type.hashCode() : 0);
        result = 31 * result + (lyric != null ? lyric.hashCode() : 0);
        result = 31 * result + change;
        result = 31 * result + sourceType;
        return result;
    }
}
