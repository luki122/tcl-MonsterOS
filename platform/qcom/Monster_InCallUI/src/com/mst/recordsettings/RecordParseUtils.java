package com.mst.recordsettings;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.android.incallui.InCallApp;

public class RecordParseUtils {

	private static final String TAG = "RecordParseUtils";

	static ArrayList<CallRecord> parseRecording(
			String path) {
    	ArrayList<CallRecord> records = new ArrayList<CallRecord>();
		try {
			File file = new File(path);
			if (file.isDirectory()) {
				String[] filesArr = file.list();
				if (filesArr != null) {
					int fileLen = filesArr.length;
					if (fileLen > 0) {
						for (int i = fileLen - 1; i >= 0; i--) {
							CallRecord record = new CallRecord();
							String name = filesArr[i];
							String postfix = getPostfix(name);
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
						"PHONE_NUMBERS_EQUAL(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ", " + trimNumber + ", 0)",
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
}