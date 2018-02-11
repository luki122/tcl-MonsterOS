package cn.tcl.music.model.live;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class RemoteMediaDesc implements Parcelable{ 
	private String downloadID;
	private final String trackUniqueID;
	private Map<String, String> properties;
	private final String name;
	private final String fullpath;
	private final boolean containSubItems;
	private final boolean containSubFolders;
	private final int deezerType;
	
	


	 
	public RemoteMediaDesc()
	{
		downloadID= "";
		trackUniqueID= "";
		name = "";
		this.fullpath = "";
		this.properties = new HashMap<String, String>();
		this.containSubFolders = false;
		this.containSubItems = false;
		this.deezerType = 0;
	}

    public RemoteMediaDesc(String name, String trackUniqueID, Map<String, String> properties)
    {
        downloadID= "";
        this.trackUniqueID= trackUniqueID;
        this.name = name;
        this.fullpath = "";
        this.properties = properties;
        this.containSubFolders = true;
        this.containSubItems = true;
        this.deezerType = 0;
    }
	public RemoteMediaDesc(String name, String downloadID)
	{
		this.name = name;
		this.downloadID = downloadID;
		this.trackUniqueID = "";
		this.fullpath = "";
		this.properties = new HashMap<String, String>();
		this.containSubFolders = false;
		this.containSubItems = false;
		this.deezerType = 0;
	}
	
	public RemoteMediaDesc(String name,
						   String downloadID,
						   String trackUniqueID,
						   String fullpath,
						   boolean mightContainSubItems,
						   boolean mightContainSubFolders,
						   Map<String, String> properties)
	{
		this.name = name;
		this.downloadID = downloadID;
		this.trackUniqueID = trackUniqueID;
		this.fullpath = fullpath;
		this.containSubFolders = mightContainSubFolders;
		this.containSubItems = mightContainSubItems;
		this.properties = properties;
		this.deezerType = 0;
	}

	public RemoteMediaDesc(String name, String downloadID,
						   String trackUniqueID, String fullpath,
						   boolean mightContainSubItems, boolean mightContainSubFolders,
						   Map<String, String> properties, int deezerType) {
		this.name = name;
		this.downloadID = downloadID;
		this.trackUniqueID = trackUniqueID;
		this.fullpath = fullpath;
		this.containSubFolders = mightContainSubFolders;
		this.containSubItems = mightContainSubItems;
		this.properties = properties;
		this.deezerType = deezerType;
	}

	public String getDownloadID() {
		return downloadID;
	}
	
	public String getTrackUniqueID() {
		return trackUniqueID;
	}

	public String getName() {
		return name;
	}
	
	public String getProperty(String key)
	{
		return properties.get(key);
	}

	public String getFullpath() {
		return fullpath;
	}

	public boolean mightContainSubItems() {
		return containSubItems;
	}

	public boolean mightContainSubFolders() {
		return containSubFolders;
	}

	public int getDeezerType() {
		return deezerType;
	}

	@Override
	public int describeContents() 
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeString(downloadID);
		dest.writeString(trackUniqueID);
		
		dest.writeInt(properties.size());
		for(String key : properties.keySet())
		{
			dest.writeString(key);
			dest.writeString(properties.get(key));
		}
		
		dest.writeString(name);
		dest.writeString(fullpath);
		dest.writeInt(containSubItems?1:0);
		dest.writeInt(containSubFolders?1:0);
		dest.writeInt(deezerType);
	}
	
	public static final Creator<RemoteMediaDesc> CREATOR = new Creator<RemoteMediaDesc>()
	{
	    @Override
	    public RemoteMediaDesc createFromParcel(Parcel source)
	    {
	        return new RemoteMediaDesc(source);
	    }

	    @Override
	    public RemoteMediaDesc[] newArray(int size)
	    {
		return new RemoteMediaDesc[size];
	    }
	};

	public RemoteMediaDesc(Parcel in) 
	{
		this.downloadID = in.readString();
		this.trackUniqueID = in.readString();
		
		int size = in.readInt();
		properties = new HashMap<String, String>();
		for(int i = 0; i < size; i++)
		{
		    String key = in.readString();
		    String value = in.readString();
		    properties.put(key,value);
		}
		
		this.name = in.readString();
		this.fullpath = in.readString();
		this.containSubItems = (in.readInt() == 1);
		this.containSubFolders = (in.readInt() == 1);
		this.deezerType = in.readInt();
	}

	
    public void setDownloadID(String Id) {
        downloadID = Id;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }
}
