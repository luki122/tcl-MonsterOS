package com.mst.recordsettings;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.incallui.InCallApp;

public class RecordParseUtils {

	private static final String TAG = "RecordParseUtils";

	static ArrayList<CallRecord> parseRecording(
			String path, boolean isPrivacyPath) {
    	ArrayList<CallRecord> records = new ArrayList<CallRecord>();
		try {
			File file = new File(path);
			if (file.isDirectory()) {
				String[] filesArr = file.list();
		        File[] files = file.listFiles();
				if (filesArr != null) {
					int fileLen = filesArr.length;
					if (fileLen > 0) {
						for (int i = fileLen - 1; i >= 0; i--) {
							CallRecord record = new CallRecord();
							String name = filesArr[i];
							String postfix = getPostfix(name);
							if(isPrivacyPath && !name.contains(postfix)) {
					             boolean change = decodeFile(files[i].getPath());
                                 Log.i(TAG, "files[i].getPath():" + files[i].getPath() + "  change:" + change);
                                 if (!change) {
                                     continue;
                                 } else {
                                     name = rename(files[i], path, name);
                                 }
							}
							fillRecord(name, record, postfix, path);
							records.add(record);
							printRecord(record);
						}
						sortRecords(records);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return records;
	}
	
	private static String rename(File f, String path, String name) {
	    String rename = name;
        try {
            rename = new String(Base64.decode(name, Base64.URL_SAFE), "UTF-8");
            boolean result = f.renameTo(new File(path, rename));
            Log.i(TAG, "rename:" + result + "  path:" + path + "  name:" + rename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rename;
	}

	private static String getPostfix(String name) {
		String postfix = ".3gpp";
		if (!TextUtils.isEmpty(name) && name.endsWith(".amr")) {
			postfix = ".amr";
		}
		return postfix;
	}

	private static void fillRecord(String name, CallRecord record,
			String postfix, String path) {
		if (name != null) {
			if (name.length() > 20) {
				String startTime = name.substring(0, 13);
				if (!TextUtils.isEmpty(startTime)) {
					long endTime = 0;
					long durationTime = 0;
					try {
						int durEnd = (name.substring(15, name.length()))
								.indexOf("_");
						durEnd += 15;
						String duration = name.substring(14, durEnd);
						if (!TextUtils.isEmpty(duration)) {
							durationTime = Long.valueOf(duration);
							endTime = Long.valueOf(startTime) + durationTime;
							String number = null;
							number = name.substring(durEnd + 1,
									name.indexOf(postfix));
							if (number != null) {
								number = queryNameByNumber(number);
							} else {
								number = name;
							}

							record.setEndTime(endTime);
							record.setDruation(durationTime);
							record.setName(number);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		processNullName(name, record, postfix, path);

		record.setMimeType("audio/amr");
		record.setPath(path + "/" + name);
		record.setFileName(name);

	}

	private static void processNullName(String name, CallRecord record,
			String postfix, String path) {
		if (record.getName() == null) {
			File fi = new File(path + "/" + name);
			if (fi.exists()) {
				record.setEndTime(fi.lastModified());
			}

			String nameSub = name.substring(0, name.indexOf(postfix));
			if (nameSub == null) {
				nameSub = name;
			}
			record.setName(name);
		}
	}

	private static String queryNameByNumber(String number) {
	    String trimNumber = number.replaceAll(" ", "");
		Cursor nameCursor = InCallApp.getInstance()
				.getContentResolver()
				.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME, },
//						ContactsContract.CommonDataKinds.Phone.NUMBER
//								+ " = '"	// sort
//								+ number
//								+ "'",
						"PHONE_NUMBERS_EQUAL(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ", \"" + trimNumber + "\", 0)",
						null, null);
		if (nameCursor != null) {
			if (nameCursor
					.moveToFirst()) {
				return nameCursor
						.getString(0);
			}

			nameCursor.close();
		}
	
		return number;		
	}

	// sort
	public static class DisplayComparator implements Comparator<CallRecord> {
		private final Collator mCollator = Collator.getInstance();

		public DisplayComparator() {
		}

		private String getDisplay(CallRecord record) {
			long label = record.getEndTime();
			return String.valueOf(label);
		}

		@Override
		public int compare(CallRecord lhs, CallRecord rhs) {
			return mCollator.compare(getDisplay(lhs), getDisplay(rhs));
		}
	}

	private static void sortRecords(ArrayList<CallRecord> records) {
		Log.d(TAG, " sortRecords records.size:" + records.size());
		try {
			Collections.sort(records, new DisplayComparator());
			Collections.reverse(records);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void printRecord(CallRecord record) {
		Log.d(TAG,
				"name:" + record.getName() + "  EndTime:" + record.getEndTime()
						+ " duration:" + record.getDruation() + "  path:"
						+ record.getPath());
	}
	
    private static boolean decodeFile(String file) throws Exception {
        int len = 8;
        java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "rw");
        java.nio.channels.FileChannel channel = raf.getChannel();
        java.nio.MappedByteBuffer buffer = channel.map(
                java.nio.channels.FileChannel.MapMode.READ_WRITE, 0, len);

        for (int i = 0; i < len; i++) {
            byte src = buffer.get(i);
            buffer.put(i, (byte) (src ^ 2));
        }
        buffer.force();
        buffer.clear();
        channel.close();
        raf.close();
        return true;
    }
}