package cn.tcl.music.util;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public final class FileUtils {

	public static File getApplicationDataDir(Context context)
	{
		if (context == null)
			return null;
        String filePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable())
        {
        	File externalFileDir = context.getExternalFilesDir(null);
        	if (externalFileDir != null)
        		filePath = externalFileDir.getPath();
        }
        
        if (filePath == null)
        {
    		filePath = context.getApplicationInfo().dataDir;
        }
        if (filePath == null)
        	return null;
        return new File(filePath);
	}

	public static String getDirExternal(Context context, String directoryName) {
//		if (!isExternalStorageWritable())
//			return null;
		File extFileDir = FileUtils.getApplicationDataDir(context);
		
    	if (extFileDir == null) {
    		return null;
    	}
		
	    // Get the directory for the app's private directory. 
	    File smpDir = new File(extFileDir, directoryName);
	    if (!smpDir.exists()){
	    	if (!smpDir.mkdirs()) {
	    		Log.e("CrossDJ Dir " + directoryName, "Directory not created");
	    		return "";
	    	}
	    }
	    return smpDir.getAbsolutePath();
	}
	
	public static String generateNonExistentFilename(File directory, String baseName, String extension, int maxIndice)
	{
		int index = 1;
		File fileToGenerate = new File(directory, baseName + (extension != null? extension : ""));
		int parenthesisRightIndex = baseName.lastIndexOf(")");
		if (parenthesisRightIndex == baseName.length() - 1) // Parenthesis at the end
		{
			int parenthesisLeftIndex = baseName.lastIndexOf("(");
			if (parenthesisLeftIndex > 0 && parenthesisLeftIndex + 1 < parenthesisRightIndex)
			{
				String numericStr = baseName.substring(parenthesisLeftIndex + 1, parenthesisRightIndex);
				boolean numeric = numericStr.matches("\\d*");
				
				if (numeric)
				{
					baseName = baseName.substring(0, parenthesisLeftIndex);
				}
			}
		}
		while (fileToGenerate.exists())
		{
			
			String newBaseName = baseName + "(" + index + ")" + (extension != null? extension : "");
			index++;
			fileToGenerate = new File(directory, newBaseName);
			if (maxIndice > 0 && index >= maxIndice) // Max Indice before breaking.
				return null;
		}
		return fileToGenerate.getName();
	}
	
	public static void ClearContent(File fileOrDirectory, boolean removeDirItSelf) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	        	ClearContent(child, true);

	    if (removeDirItSelf)
	    	fileOrDirectory.delete();
	}
	
	public static boolean isRemoteTrack(String trackPath)
	{
		if (trackPath == null)
			return false;
	    return trackPath.startsWith("https://") || trackPath.startsWith("remote-track://") || trackPath.startsWith("http://");
	}
	
}
