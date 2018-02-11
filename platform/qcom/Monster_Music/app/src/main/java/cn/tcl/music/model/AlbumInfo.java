package cn.tcl.music.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class AlbumInfo implements Parcelable {

    public String artistId;
    public int numberOfTracks;
    public String album;
    public String albumId;
    public String albumKey;
    public String artworkPath;
    public String artist;
    public String artistkey;
    public String artistPortrait;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artistId);
        dest.writeInt(numberOfTracks);
        dest.writeString(album);
        dest.writeString(albumKey);
        dest.writeString(artworkPath);
        dest.writeString(artist);
        dest.writeString(artistkey);
        dest.writeString(artistPortrait);
    }

    public AlbumInfo() {

    }

    public AlbumInfo(Parcel in) {
        artistId = in.readString();
        numberOfTracks = in.readInt();
        album = in.readString();
        albumKey = in.readString();
        artworkPath = in.readString();
        artist = in.readString();
        artistkey = in.readString();
        artistPortrait = in.readString();
    }

    public static final Creator<AlbumInfo> CREATOR = new Creator<AlbumInfo>() {

        public AlbumInfo createFromParcel(Parcel source) {
            return new AlbumInfo(source);
        }

        @Override
        public AlbumInfo[] newArray(int size) {
            return new AlbumInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "MediaInfo{" +
                "artistId='" + artistId + '\'' +
                ", numberOfTracks=" + numberOfTracks +
                ", album=" + album +
                ", albumKey='" + albumKey + '\'' +
                ", artworkPath='" + artworkPath + '\'' +
                ", artist='" + artist + '\'' +
                ", artistkey=" + artistkey +
                ", artistPortrait='" + artistPortrait + '\'' +
                '}';
    }
}