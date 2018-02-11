package com.mst.wallpaper.utils;

import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
	public static void closeQuietly(OutputStream stream){
		if(stream == null){
			return;
		}
		try{
			stream.flush();
			stream.close();
			stream = null;
		}catch (Exception e) {
		  //ignore
		}
	}
	
	public static void closeQuietly(InputStream stream){
		if(stream == null){
			return;
		}
		try{
			stream.close();
			stream = null;
		}catch (Exception e) {
		  //ignore
		}
	}
} 