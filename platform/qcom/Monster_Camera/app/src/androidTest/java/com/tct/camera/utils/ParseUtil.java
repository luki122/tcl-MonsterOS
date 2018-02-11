/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

/**
 * @author wenhua.tu
 *
 */
public class ParseUtil {
	private final static String TAG = "ParseUtil";

	public static List<Suite> parseFile(Context context) {
		Log.w(TAG, "parseFile");
		File parseFile = new File(CommonUtil.getTestResultDir(context), CommonUtil.FILE_NAME);
		List<Suite> suiteList = null;
		if (parseFile.exists()) {
			try {
				InputStream inputStream = new FileInputStream(parseFile);
				XmlPullParser parser = Xml.newPullParser();
				parser.setInput(inputStream, "utf-8");
				Suite suite = null;
				Case testCase = null;
				int eventType = parser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					switch (eventType) {
					case XmlPullParser.START_DOCUMENT:
						suiteList = new ArrayList<>();
						break;

					case XmlPullParser.START_TAG:
						if (parser.getName().equals("testsuite")) {
							suite = new Suite();
						} else if (parser.getName().equals("testcase")) {
							testCase = new Case();
//							testCase.setmSuiteName(parser.getAttributeValue(null, "suitename"));
							testCase.setmClassName(parser.getAttributeValue(null, "classname"));
							testCase.setmMethodeName(parser.getAttributeValue(null, "methodname"));
							testCase.setmIsRight(parser.getAttributeValue(null, "right"));
							testCase.setmTime(parser.getAttributeValue(null, "time"));
							testCase.setmMessage(parser.getAttributeValue(null, "message"));
							testCase.setmType(parser.getAttributeValue(null, "type"));
//							testCase.setmStack(parser.getAttributeValue(null, "stack"));
						}
						break;

					case XmlPullParser.END_TAG:
						if (parser.getName().equals("testsuite")) {
							if (suiteList != null && suite != null) {
								suiteList.add(suite);
							}
						} else if (parser.getName().equals("testcase")) {
							if (suite != null && testCase != null) {
								suite.add(testCase);
							}
						}
						break;
					}

					eventType = parser.next();
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return suiteList;
	}
}
