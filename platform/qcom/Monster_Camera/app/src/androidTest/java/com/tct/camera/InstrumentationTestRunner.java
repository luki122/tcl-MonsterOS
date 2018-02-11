/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import junit.framework.TestSuite;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.os.Bundle;
import android.test.InstrumentationTestSuite;
import android.util.Log;

import com.tct.camera.testcases.AutoBackTest;
import com.tct.camera.testcases.AutoFrontTest;
import com.tct.camera.testcases.ManualTest;
import com.tct.camera.testcases.MicroVideoBackTest;
import com.tct.camera.testcases.MicroVideoFrontTest;
import com.tct.camera.testcases.PanoramaTest;
import com.tct.camera.testcases.SlomotionTest;
import com.tct.camera.utils.CommonUtil;

/**
 * @author wenhua.tu
 *
 */
public class InstrumentationTestRunner extends android.test.InstrumentationTestRunner {

	private Writer mWriter;
	private XmlSerializer mTestSuiteSerializer;
	private long mTestStarted;
	private static final String TAG = "InstrumentationTestRunner";

	@Override
	public void onStart() {
		Log.w(TAG, "onStart");
		try {
			String filePath = CommonUtil.getTestResultDir(getTargetContext());
			File fileRobo = new File(filePath);
			if (!fileRobo.exists()) {
				fileRobo.mkdir();
			}
			startJUnitOutput(new FileWriter(new File(filePath, CommonUtil.FILE_NAME)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		super.onStart();
	}

	private void startJUnitOutput(Writer writer) {
		try {
			mWriter = writer;
			mTestSuiteSerializer = newSerializer(mWriter);
			mTestSuiteSerializer.startDocument(null, null);
			mTestSuiteSerializer.startTag(null, "testsuites");
			mTestSuiteSerializer.startTag(null, "testsuite");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private XmlSerializer newSerializer(Writer writer) {
		try {
			XmlPullParserFactory pf = XmlPullParserFactory.newInstance();
			XmlSerializer serializer = pf.newSerializer();
			serializer.setOutput(writer);
			return serializer;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void sendStatus(int resultCode, Bundle results) {
		Log.w(TAG, "sendStatus");
		super.sendStatus(resultCode, results);
		switch (resultCode) {
		case REPORT_VALUE_RESULT_ERROR:
		case REPORT_VALUE_RESULT_FAILURE:
		case REPORT_VALUE_RESULT_OK:
			try {
				recordTestResult(resultCode, results);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			break;

		case REPORT_VALUE_RESULT_START:
			recordTestStart(results);

		default:
			break;
		}
	}

	private void recordTestStart(Bundle results) {
		mTestStarted = System.currentTimeMillis();
	}

	private void recordTestResult(int resultCode, Bundle results) throws IOException {
		float time = (System.currentTimeMillis() - mTestStarted) / 1000.0f;
//		String suiteassignment = results.getString("suiteassignment");
		String className = results.getString(REPORT_KEY_NAME_CLASS);
		int classIndex = className.lastIndexOf('.');
		if (classIndex != -1) {
			className = className.substring(classIndex + 1);
		}
		String testMethod = results.getString(REPORT_KEY_NAME_TEST);
		String stack = results.getString(REPORT_KEY_STACK);
		int current = results.getInt(REPORT_KEY_NUM_CURRENT);
		int total = results.getInt(REPORT_KEY_NUM_TOTAL);

		mTestSuiteSerializer.startTag(null, "testcase");
//		mTestSuiteSerializer.attribute(null, "suitename", "");
		mTestSuiteSerializer.attribute(null, "classname", className);
		mTestSuiteSerializer.attribute(null, "methodname", testMethod);
		mTestSuiteSerializer.attribute(null, "time", String.format("%.3f", time));

		if (resultCode != REPORT_VALUE_RESULT_OK) {
			mTestSuiteSerializer.attribute(null, "right", "NO");
			if (stack != null) {
				String reason = stack.substring(0, stack.indexOf('\n'));
				String message = "";
				int index = reason.indexOf(':');
				if (index > -1) {
					message = reason.substring(index + 1);
					reason = reason.substring(0, index);
				}
				mTestSuiteSerializer.attribute(null, "message", message);
				mTestSuiteSerializer.attribute(null, "type", reason);
//				mTestSuiteSerializer.attribute(null, "stack", stack);
			}
		} else {
			mTestSuiteSerializer.attribute(null, "right", "YES");
			mTestSuiteSerializer.attribute(null, "message", "");
			mTestSuiteSerializer.attribute(null, "type", "");
//			mTestSuiteSerializer.attribute(null, "stack", "");
		}

		mTestSuiteSerializer.endTag(null, "testcase");
		if (current == total) {
			mTestSuiteSerializer.endTag(null, "testsuite");
			mTestSuiteSerializer.flush();
		}
	}

	@Override
	public void finish(int resultCode, Bundle results) {
		Log.w(TAG, "finish");
		endTestSuites();
		super.finish(resultCode, results);
	}

	private void endTestSuites() {
		try {
			mTestSuiteSerializer.endTag(null, "testsuites");
			mTestSuiteSerializer.endDocument();
			mTestSuiteSerializer.flush();
			mWriter.flush();
			mWriter.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
