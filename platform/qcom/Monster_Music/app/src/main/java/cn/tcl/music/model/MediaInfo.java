package cn.tcl.music.model;

import android.os.Parcel;
import android.os.Parcelable;

import cn.tcl.music.common.CommonConstants;

public class MediaInfo implements Parcelable{

	public long Id;
    public long audioId;
	public long artistId;
	public long albumId;
	public long genreId;
	public long Bpm;
	public long folderId;
	public int folderNum;
	public int sourceType;
	public int transitionId;
	public int tmpQueueIndex;			// used to store data temporarily (not saved in DB)
	public int isEffective;		//Queue中该歌曲是否有效
	public boolean Favorite;
	public double durationMs;
	public String filePath;
    public String remoteImportPath;
	public String title;
	public String artist;
	public String folderName;
	public String album;
	public String genre;
	public String Key;
	public String artworkPath;   //专辑封面
	public String artistPortraitPath;  //艺人肖像
    public String description;
	public String songRemoteId;//保存歌曲网络id

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(Id);
        dest.writeLong(audioId);
		dest.writeLong(artistId);
		dest.writeLong(albumId);
		dest.writeLong(genreId);
		dest.writeLong(Bpm);
		dest.writeLong(folderId);
		dest.writeInt(folderNum);
		dest.writeInt(sourceType);
		dest.writeInt(transitionId);
		dest.writeInt(tmpQueueIndex);
		dest.writeInt(isEffective);
		dest.writeInt(Favorite ? 1 : 0);
		dest.writeDouble(durationMs);
		dest.writeString(filePath);
        dest.writeString(remoteImportPath);
		dest.writeString(title);
		dest.writeString(artist);
		dest.writeString(folderName);
		dest.writeString(album);
		dest.writeString(genre);
		dest.writeString(Key);
		dest.writeString(artworkPath);
		dest.writeString(artistPortraitPath);
        dest.writeString(description);
		dest.writeString(songRemoteId);
	}
	
	public MediaInfo(Parcel in)
	{
		Id = in.readLong();
		audioId = in.readLong();
		artistId = in.readLong();
		albumId = in.readLong();
		genreId = in.readLong();
		Bpm = in.readLong();
		folderId = in.readLong();
		folderNum = in.readInt();
		sourceType = in.readInt();
		transitionId = in.readInt();
		tmpQueueIndex = in.readInt();
		isEffective = in.readInt();
		Favorite = in.readInt() > 0 ? true : false;
		durationMs = in.readDouble();
		filePath = in.readString();
        remoteImportPath = in.readString();
		title = in.readString();
		artist = in.readString();
		folderName = in.readString();
		album = in.readString();
		genre = in.readString();
		Key = in.readString();
		artworkPath =in.readString();
		artistPortraitPath = in.readString();
        description =in.readString();
		songRemoteId = in.readString();
	}
	
	public MediaInfo()
	{
		
	}
	
	@Override
	public boolean equals(Object o) {
	     // Return true if the objects are identical.
	     // (This is just an optimization, not required for correctness.)
	     if (this == o) {
	       return true;
	     }

	     // Return false if the other object has the wrong type.
	     // This type may be an interface depending on the interface's specification.
	     if (!(o instanceof MediaInfo)) {
	       return false;
	     }

	     // Cast to the appropriate type.
	     // This will succeed because of the instanceof, and lets us access private fields.
	     MediaInfo info = (MediaInfo) o;

	     // Check each field. Primitive fields, reference fields, and nullable reference
	     // fields are all treated differently.
	     return audioId == info.audioId;
	}

	public static final Creator<MediaInfo> CREATOR = new Creator<MediaInfo>() {
		
		public MediaInfo createFromParcel(Parcel source) {
			return new MediaInfo(source);
		}

		@Override
		public MediaInfo[] newArray(int size) {
			return new MediaInfo[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
		return "MediaInfo{" +
				"album='" + album + '\'' +
				", Id=" + Id +
				", audioId=" + audioId +
				", title='" + title + '\'' +
				", folderName='" + folderName + '\'' +
				", artistId=" + artistId +
				", artist='" + artist + '\'' +
				", albumId=" + albumId +
				", genreId=" + genreId +
				", genre='" + genre + '\'' +
				", durationMs=" + durationMs +
				", Favorite=" + Favorite +
				", Bpm=" + Bpm +
				", Key='" + Key + '\'' +
				", sourceType=" + sourceType +
				", artworkPath='" + artworkPath + '\'' +
				", artistPortraitPath='" + artistPortraitPath + '\'' +
				", description='" + description + '\'' +
				", transitionId=" + transitionId +
				", tmpQueueIndex=" + tmpQueueIndex +
				", songRemoteId='" + songRemoteId + '\'' +
				", folderId=" + folderId +
				", isEffective=" + isEffective +
				'}';
	}

	public boolean isLocal(){
		int mediaSourceType = sourceType;
		return !(mediaSourceType == CommonConstants.SRC_TYPE_DEEZER || mediaSourceType == CommonConstants.SRC_TYPE_DEEZERRADIO|| mediaSourceType == CommonConstants.SRC_TYPE_RDIO);
	}
}
