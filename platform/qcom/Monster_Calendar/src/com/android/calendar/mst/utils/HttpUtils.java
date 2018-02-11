package com.android.calendar.mst.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

import android.text.TextUtils;
import android.util.Log;

public class HttpUtils {

	private static final String TAG = "HttpUtils";

	public static String get(String urlStr) throws Exception {
		return get(urlStr, false);
	}

	public static String get(String urlStr, boolean encrypt) throws Exception {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {

			String content = "";
			StringBuilder builder = new StringBuilder();
			BufferedReader in = null;

			conn.setRequestMethod("GET");
			conn.setConnectTimeout(10000);
			conn.connect();

			int stutas = conn.getResponseCode();
			if (stutas == HttpURLConnection.HTTP_OK) {
				InputStream inStream = conn.getInputStream();
				in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
				while ((content = in.readLine()) != null) {
					builder.append(content);
				}
				in.close();
				if (encrypt) {
					decrypt(builder.toString());
					Log.i(TAG, "encrypt: " + decrypt(builder.toString()));
				}
				return builder.toString();
			} else {
				Log.e(TAG, "NETWORK ERROR! http response code: " + stutas);
				return "";
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				Log.e(TAG, "SocketTimeoutException");
			}
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return "";
	}

	public static String postJsonRequest(String urlStr, String paramsStr) throws Exception {
		return postJsonRequest(urlStr, paramsStr, false);
	}

	public static String postJsonRequest(String urlStr, String paramsStr, boolean encrypt) throws Exception {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {

			String content = "";
			StringBuilder builder = new StringBuilder();
			BufferedReader in = null;

			conn.setRequestMethod("POST");
			conn.setConnectTimeout(10000);

			Log.i(TAG, "urlStr: " + urlStr);
			Log.i(TAG, "paramsStr: " + paramsStr);

			if (!TextUtils.isEmpty(paramsStr)) {
				String params;
				if (encrypt) {
					params = encrypt(paramsStr);
					Log.i(TAG, "params encrypt: " + params);
				} else {
					params = paramsStr;
				}

				conn.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
				conn.setDoOutput(true);

				OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
				//发送参数
				writer.write(params);
				//清理当前编辑器的左右缓冲区，并使缓冲区数据写入基础流
				writer.flush();
				writer.close();
			}

			conn.connect();

			int stutas = conn.getResponseCode();
			if (stutas == HttpURLConnection.HTTP_OK) {
				InputStream inStream = conn.getInputStream();
				in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
				while ((content = in.readLine()) != null) {
					builder.append(content);
				}
				in.close();

				if (encrypt) {
//					// 处理加引号的字符串
//					builder.deleteCharAt(0).deleteCharAt(builder.length() - 1);
					return decrypt(builder.toString());
				} else {
					return builder.toString();
				}
			} else {
				Log.e(TAG, "NETWORK ERROR! http response code: " + stutas);
				return "";
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "IOException");
			if (e instanceof SocketTimeoutException) {
				Log.e(TAG, "SocketTimeoutException");
			}
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return "";
	}

	/**
	 * 字符串加密
	 * @param str 待加密字符串
	 * @return 加密后的字符串
	 */
	/*public static String encrypt(String str) {
		String rstStr = "";
		if (str != null && str.length()>0) {
			char[] charArray = str.toCharArray();
			for (int i = 0; i < charArray.length; i++) {
				charArray[i] = (char) (charArray[i] ^ (666 + i));
			}
			rstStr = new String(charArray);
		}

		return rstStr;
	}*/

	public static String encrypt(String str) {
		String rstStr = "";
		if (str != null && str.length() > 0) {
			char[] charArray = str.toCharArray();
			int j = 0;
			for (int i = 0; i < charArray.length; i++) {
				charArray[i] = (char) (charArray[i] ^ (666 + j));
				if (j++ > 10000) {
					j = 0;
				}
			}
			rstStr = new String(charArray);
		}

		return rstStr;
	}

	/**
	 * 字符串解密
	 * @param str 待解密字符串
	 * @return 解密后的字符串
	 */
	public static String decrypt(String str) {
		return encrypt(str);
	}

}
