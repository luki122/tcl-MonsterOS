package com.mst.thememanager.parser;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.utils.Config;
import com.mst.thememanager.utils.StringUtils;

public class LocalThemeParser implements ThemeParser<Theme,InputStream> {


	private Theme parserTheme(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		// TODO Auto-generated method stub
		final String name = parser.getName();
		if (!name.equals("Theme")) {
			throw new XmlPullParserException(parser.getPositionDescription()
					+ ": invalid Theme  tag " + name);
		}

		return parserThemeInner(parser);
	}

	private Theme parserThemeInner(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		final int innerDepth = parser.getDepth() + 1;
		int depth;
		int type;
		Theme theme = new Theme();
		while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
				&& ((depth = parser.getDepth()) >= innerDepth || type != XmlPullParser.END_TAG)) {
			if (type != XmlPullParser.START_TAG || depth > innerDepth) {
				continue;
			}
			String tag = parser.getName();
			if (Config.ThemeDescription.TAG_DESIGNER.equals(tag)) {
				theme.designer = parser.nextText();
			} else if (Config.ThemeDescription.TAG_DESCRIPTION.equals(tag)) {
				theme.description = StringUtils.trim(parser.nextText());
			} else if (Config.ThemeDescription.TAG_NAME.equals(tag)) {
				theme.name = parser.nextText();
			} else if (Config.ThemeDescription.TAG_VERSION.equals(tag)) {
				theme.version = parser.nextText();
			}else if (Config.ThemeDescription.TAG_SIZE.equals(tag)) {
				theme.size = parser.nextText();
			}else if(Config.ThemeDescription.TAG_WALLPAPER.equals(tag)){
				try{
					theme.systemWallpaperNumber = Integer.valueOf(parser.nextText());
				}catch(Exception e){
					theme.systemWallpaperNumber = -1;
				}
				
			}else if(Config.ThemeDescription.TAG_KEYGUARD_WALLPAPER.equals(tag)){
				theme.systemKeyguardWallpaperName = parser.nextText();
			}
		}
		
		return theme;
	}

	@Override
	public Theme parser(InputStream in) {
		XmlPullParser xmlParser = null;
		final InputStream input = in;
		try {
			xmlParser = XmlPullParserFactory.newInstance().newPullParser();
			xmlParser.setInput(input, ENCODING);
			int type;
			while ((type = xmlParser.next()) != XmlPullParser.START_TAG
					&& type != XmlPullParser.END_DOCUMENT) {
				// Seek parser to start tag.
			}

			if (type != XmlPullParser.START_TAG) {
				throw new XmlPullParserException("No start tag found");
			}
			return parserTheme(xmlParser);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return null;
		
	}

}
