package com.mst.thememanager.entities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.text.TextUtils;
import android.util.Log;

import com.mst.thememanager.utils.Config;
import com.mst.thememanager.utils.FileUtils;
import com.mst.thememanager.utils.TLog;

public class ThemeZip extends ZipFile {

	private static final String TAG = "ThemeZip";

	private HashMap<String, ZipEntry> cache = new HashMap<String, ZipEntry>();
	
	private ArrayList<String> previewCache = new ArrayList<String>();
	
	private ArrayList<String> wallpaperCache = new ArrayList<String>();
	
	private String mDescription;
	private File mFile;

	public ThemeZip(File file) throws ZipException, IOException {
		super(file);
		mFile = file;
		cache.clear();
		// TODO Auto-generated constructor stub
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			Enumeration<ZipEntry> zipEnumeration = (Enumeration<ZipEntry>) zipFile
					.entries();
			ZipEntry entry = null;
			while (zipEnumeration.hasMoreElements()) {
				entry = zipEnumeration.nextElement();
				if (!entry.isDirectory()) {
					String name = entry.getName();
					TLog.d(TAG, name);
					if(name.contains("preview")){
						previewCache.add(name);
					}else if(name.contains("wallpaper")){
						wallpaperCache.add(name);
					}else if(name.contains("description")){
						mDescription = name;
					}
					cache.put(name, entry);
				}
			}

		} catch (Exception ex) {
			Log.d(TAG, "open theme zip file exception-->" + ex);
		} finally {
			if (zipFile != null) {
				zipFile.close();
			}
		}
	}
	
	public void loadInfo(){
		if(!TextUtils.isEmpty(mDescription)){
			try {
				InputStream description = getInputStream(getEntry(mDescription));
				writeInfo(description, getInfoPath(Config.LOCAL_THEME_PKG_INFO_DIR, mDescription));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(previewCache.size() > 0){
			for(String preview:previewCache){
				InputStream previewStream;
				try {
					previewStream = getInputStream(getEntry(preview));
					writeInfo(previewStream, getInfoPath(Config.LOCAL_THEME_PKG_INFO_DIR, preview));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
	
	private String getPreviewPrefix(String preview){
		int lastSplitIndex = preview.lastIndexOf(File.separatorChar);
		if (lastSplitIndex == -1) {
			return null;
		}
		return preview.substring(0, lastSplitIndex);
	}
	
	private String getPreviewImageName(String preview){
		int lastSplitIndex = preview.lastIndexOf(File.separatorChar);
		if (lastSplitIndex == -1) {
			return null;
		}
		return preview.substring(lastSplitIndex+1, preview.length());
	}
	
	private String getInfoPath(String dir, String realName) {
		String themeFilePath = getFilePath();
		StringBuilder builder = new StringBuilder();
		int lastSplitIndex = themeFilePath.lastIndexOf(File.separatorChar);
		if (lastSplitIndex == -1) {
			return null;
		}
		builder.append(dir);
		builder.append(themeFilePath.substring(lastSplitIndex+1,
				themeFilePath.length()));
		builder.append(File.separatorChar);
		builder.append(realName);
		TLog.d(TAG, "info file name-->"+builder.toString());
		return builder.toString();
	}
	
	private void writeInfo(InputStream in,String fileName){
		File outFile = new File(fileName);
		if(!outFile.exists()){
			FileUtils.createFile(fileName);
		}
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outFile);
			int ch;  
            byte[] buffer = new byte[1024];  
            //read (ch) bytes into buffer  
            while ((ch = in.read(buffer)) != -1){  
                // write (ch) byte from buffer at the position 0  
                out.write(buffer, 0, ch);  
                out.flush();  
            }  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
				try {
					if(out != null){
						out.close();
					}
					if(in != null){
						in.close();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	
	public ArrayList<String> getPreviewCache(){
		return previewCache;
	}
	
	public ArrayList<String> getWallpaperCache(){
		return wallpaperCache;
	}

	public String getFilePath(){
		return mFile.getAbsolutePath();
	}
	
	public ZipEntry getEntry(String key) {
		return cache.get(key);
	}

	public int getSize() {
		return cache.size();
	}
	
	public InputStream getFileStream(String key) throws IOException{
		ZipEntry entry = getEntry(key);
		return getInputStream(entry);
	}

}