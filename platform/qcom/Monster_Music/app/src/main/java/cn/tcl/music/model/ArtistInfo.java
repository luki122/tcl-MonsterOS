package cn.tcl.music.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by daisy on 16-10-21.
 */
public class ArtistInfo implements Parcelable {

    public String id;
    public String artist;
    public String artistkey;
    public String artistPortrait;
    public int numberOfAlbums;
    public int numberOfTracks;


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artist);
        dest.writeString(artistkey);
        dest.writeString(artistPortrait);
        dest.writeInt(numberOfAlbums);
        dest.writeInt(numberOfTracks);
    }

    public ArtistInfo() {
    }

    public ArtistInfo(Parcel in) {
        artist = in.readString();
        artistkey = in.readString();
        artistPortrait = in.readString();
        numberOfAlbums = in.readInt();
        numberOfTracks = in.readInt();
    }

    public static final Creator<ArtistInfo> CREATOR = new Creator<ArtistInfo>() {

        public ArtistInfo createFromParcel(Parcel source) {
            return new ArtistInfo(source);
        }

        @Override
        public ArtistInfo[] newArray(int size) {
            return new ArtistInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "MediaInfo{" +
                "artist='" + artist + '\'' +
                ", artistkey=" + artistkey +
                ", artistPortrait=" + artistPortrait +
                ", numberOfAlbums='" + numberOfAlbums + '\'' +
                ", numberOfTracks='" + numberOfTracks + '\'' +
                '}';
    }
}
