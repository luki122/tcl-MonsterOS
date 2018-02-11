package com.mst.thememanager.job;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.text.TextUtils;
import android.util.Log;

import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.entities.ThemeZip;
import com.mst.thememanager.job.ThreadPool.Job;
import com.mst.thememanager.job.ThreadPool.JobContext;
import com.mst.thememanager.parser.LocalThemeParser;
import com.mst.thememanager.parser.ThemeParser;
import com.mst.thememanager.utils.Config;

public class LocalThemeJob implements Job<Theme>{

	
	private String mPath;
	private ThemeParser<Theme, InputStream> mThemeParser;
	public LocalThemeJob(String path){
		mPath = path;
		mThemeParser = new LocalThemeParser();
	}
	
	@Override
	public Theme run(JobContext jc) {
		try {
			ThemeZip themeZip = new ThemeZip(new File(mPath));
			InputStream input = null;
			ZipEntry entry = themeZip.getEntry(Config.LOCAL_THEME_DESCRIPTION_FILE_NAME);
			if (entry != null) {
				input = themeZip.getInputStream(entry);
			}
				Theme theme = mThemeParser.parser(input);
				if(theme != null){
					theme.previewArrays = themeZip.getPreviewCache();
					theme.wallpaperArrays = themeZip.getWallpaperCache();
					theme.themeFilePath = themeZip.getFilePath();
					theme.themeZipFile = themeZip;
				}
				themeZip.loadInfo();
				themeZip.close();
				return theme;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
}