package com.monster.market.http;

import com.monster.market.utils.LogUtil;
import com.monster.market.utils.SystemUtil;

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

public class HttpRequestUtil {

	public static final String TAG = "HttpRequestUtil";

	public static String excute(Request request) throws Exception {
		if (request == null) {
			throw new RequestError(RequestError.ERROR_OTHER, "Request is null");
		}
		if (SystemUtil.hasNetwork()) {
			switch (request.requestMethod) {
				case GET:
					return get(request, true);
				case POST:
					return postJsonRequest(request, true);
				default:
					throw new RequestError(RequestError.ERROR_OTHER,
							"You doesn't define this requestMethod");
			}
		} else {
			throw new RequestError(RequestError.ERROR_NO_NETWORK,
					"no network error");
		}
	}

	public static String get(Request request) throws Exception {
		return get(request, false);
	}

	public static String get(Request request, boolean encrypt) throws Exception {
		URL url = new URL(request.url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {

			String content = "";
			StringBuilder builder = new StringBuilder();
			BufferedReader in = null;

			conn.setRequestMethod("GET");
			conn.setConnectTimeout(request.connectTimeout);
			conn.setReadTimeout(request.connectTimeout);
			conn.connect();

			int status = conn.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK) {
				InputStream inStream = conn.getInputStream();
				in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
				while ((content = in.readLine()) != null) {
					builder.append(content);
				}
				in.close();
				if (encrypt) {
					decrypt(builder.toString());
					LogUtil.i("encrypt: " + decrypt(builder.toString()));
				}
				return builder.toString();
			} else {
				throw new RequestError(RequestError.ERROR_NETWORK, "http response code: " + status);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				throw new RequestError(RequestError.ERROR_TIMEOUT);
			}
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return "";
	}

	public static String postJsonRequest(Request request) throws Exception {
		return postJsonRequest(request, false);
	}

	public static String postJsonRequest(Request request, boolean encrypt) throws Exception {
		URL url = new URL(request.url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {

			String content = "";
			StringBuilder builder = new StringBuilder();
			BufferedReader in = null;

			conn.setRequestMethod("POST");
			conn.setConnectTimeout(request.connectTimeout);
			conn.setReadTimeout(request.connectTimeout);

			LogUtil.i(TAG, "request.getParams(): " + request.getParams());
			// 有params
			if (null != request.getParams() && !"".equals(request.getParams())) {
				String encoding = "UTF-8";
				String params;
				if (encrypt) {
					params = encrypt(request.getParams());

					LogUtil.i("params encrypt: " + decrypt(request.getParams()));
				} else {
					params = request.getParams();
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

			int status = conn.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK) {
				InputStream inStream = conn.getInputStream();
				in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
				int len = 0;
				char[] buff = new char[1024];
				while ((len = in.read(buff)) != -1) {
					builder.append(buff, 0, len);
				}
//				while ((content = in.readLine()) != null) {
//					builder.append(content);
//				}
				in.close();

				if (encrypt) {
//					// 处理加引号的字符串
//					builder.deleteCharAt(0).deleteCharAt(builder.length() - 1);
					return decrypt(builder.toString());
				} else {
					return builder.toString();
				}
			} else {
				throw new RequestError(RequestError.ERROR_NETWORK, "http response code: " + status);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				throw new RequestError(RequestError.ERROR_TIMEOUT);
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
