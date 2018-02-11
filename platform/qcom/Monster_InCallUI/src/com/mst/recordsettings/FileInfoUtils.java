package com.mst.recordsettings;

import java.io.File;
import java.text.SimpleDateFormat;

import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.android.incallui.R;


public class FileInfoUtils{
	static void showFileDetail(CallRecord record, Context mContext) {
		if (record == null) {
			return;
		}
		
		AlertDialog dialog = null;
		
		if (null == dialog) {
	    	String path = record.getPath();
	    	if (path == null) {
				return;
			}
	    	
			File file = new File(path);
			long size = 0, pointSize = 0;
			String sizeStr = null;			
			
			if (file.exists()) {
				size = file.length() / 1024;
				pointSize = file.length() % 1024;
				sizeStr = String.valueOf(size) + "." + String.valueOf(pointSize).substring(0, 1);
			} else {
				return;
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String date = sdf.format(record.getEndTime() + record.getDruation());
			
			String message = mContext.getString(R.string.call_record_message_time);
			message += date;
			message += "\n";
			message += mContext.getString(R.string.call_record_message_size);
			message += sizeStr;
			message += "KB";
				message += "\n";
				message += mContext.getString(R.string.call_record_message_path);
				message += record.getFileName();
			
			
			dialog = new AlertDialog.Builder(mContext)
                    .setTitle(R.string.call_record_detail_title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                	dialog.dismiss();
                                	dialog = null;
                                }
                            }).create();
        }

		dialog.show();
	}
}