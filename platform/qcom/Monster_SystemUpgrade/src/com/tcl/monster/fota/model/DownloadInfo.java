package com.tcl.monster.fota.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

public class DownloadInfo implements Serializable{

	public List<String> mServers = new ArrayList<String>();
	public List<FileInfo> mFiles = new ArrayList<FileInfo>();
	
	public static class FileInfo implements Serializable{
		public String mUrl ;
		public String mFileId ;
		@Override
		public String toString() {
			return "FileInfo [mUrl=" + mUrl + ", mFileId=" + mFileId + "]";
		}
		
	}

	@Override
	public String toString() {
		return "DownloadInfo [mServers=" + mServers + ", mFiles=" + mFiles
				+ "]";
	}

    public String toJson() {
        return new Gson().toJson(this);
    }
}
