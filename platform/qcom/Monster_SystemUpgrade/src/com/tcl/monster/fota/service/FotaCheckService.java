package com.tcl.monster.fota.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;

import com.tcl.monster.fota.FotaApp;
import com.tcl.monster.fota.FotaNotification;
import com.tcl.monster.fota.FotaUIPresenter;
import com.tcl.monster.fota.downloadengine.DownloadEngine;
import com.tcl.monster.fota.downloadengine.DownloadTask;
import com.tcl.monster.fota.listener.OnGotSpopsListener;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.misc.State;
import com.tcl.monster.fota.model.DownloadInfo;
import com.tcl.monster.fota.model.Spop;
import com.tcl.monster.fota.model.UpdatePackageInfo;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaPref;
import com.tcl.monster.fota.utils.FotaUtil;
import com.tcl.monster.fota.utils.ResponseParser;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Fota checking service provide functions like manual check, auto check, install check and get
 * download url from server. A successful checking contains 3 steps without errors:
 * 1) check if there is a firmware update from server.
 * 2) send a post request to get the real download url.
 * 3) create a DownloadTask.
 */
public class FotaCheckService extends IntentService {
	/**
	 * TAG for Log
	 */
    private static final String TAG = "FotaCheckService";

	/**
	 * Request actions
	 */
    public static final String ACTION_CHECK = "com.tcl.ota.action.CHECK";

	/**
	 * Intent extra
	 */
    public static final String EXTRA_CHECK_TYPE = "check_type";

	/**
	 * Check type
	 */
    private static String mCheckType = FotaConstants.FOTA_CHECK_TYPE_VALUE_MANUAL;

	/**
	 * Check url
	 */
    private static String mBaseUrl = FotaConstants.GOTU_URL_1;

	/**
	 * Check again flag
	 */
	private static Boolean mCheckAgain = true;

	/**
	 * Send Location flag
	 */
	private boolean isSendLocation = false;

	/**
	 * Root flag
	 */
	private boolean isRooted = false;

	/**
	 * Full updates or Incremental updates flag
	 */
	private static boolean isFullUpdate = false;

    public FotaCheckService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
		super.onCreate();
		mBaseUrl = FotaUtil.getRandomUrl();
		FotaLog.v(TAG, "onCreate -> mBaseUrl = " + mBaseUrl);
    }

    /**
     * This method is called on very startService(Context , FotaCheckService.class).
     * The intent that start the service will pass through here.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
    	FotaLog.v(TAG, "onHandleIntent");
    	if (intent == null) {
    		stopSelf();
    		return;
    	}
    	if (intent.getAction() != ACTION_CHECK) {
    		stopSelf();
    		return ;
    	}

        while (!isDeviceProvisioned()) {
            FotaLog.v(TAG, "wait for Device Provisioned.");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
        }

    	String checkType = intent.getStringExtra(EXTRA_CHECK_TYPE);
		isRooted = FotaUtil.isDeviceRooted();
		if (isRooted) {
			isFullUpdate = true;
		} else {
			isFullUpdate = false;
		}
		FotaLog.d(TAG, "onHandleIntent -> mCheckType = " + checkType + ", isRooted = " + isRooted
				+ ", isFullUpdate = " + isFullUpdate);
		doCheck(checkType);
	}
	
	private boolean isDeviceProvisioned() {
        return 0 != Settings.Global.getInt(
                getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
    }

    /**
     * 1) Network is ok.
     * 2) Intent is not null.
     * 3) Action is ACTION_CHECK.
     * 4) There is no on going check task.
     * Four items above need to check and if you are lucky all passed will go here.
     */
    private void doCheck(String checkType) {
		mCheckType = checkType;
    	if (checkType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_AUTO)) {
            FotaLog.v(TAG, "doCheck -> autocheck currentTime = " + System.currentTimeMillis()
					+ ", frequency = " + PreferenceManager.getDefaultSharedPreferences(FotaCheckService.this)
					.getInt(FotaConstants.UPDATE_CHECK_PREF, FotaUtil.getDefaultAutoCheckVal()));
            PreferenceManager.getDefaultSharedPreferences(FotaCheckService.this).edit()
                    .putLong(FotaConstants.LAST_AUTO_UPDATE_CHECK_PREF, System.currentTimeMillis())
                    .apply();
        }

        List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("id", FotaUtil.IMEI(getApplicationContext())));
        params.add(new BasicNameValuePair("curef", FotaUtil.REF()));
        params.add(new BasicNameValuePair("fv", FotaUtil.VERSION()));
		if (isFullUpdate) {
			params.add(new BasicNameValuePair("mode", "7"));
		} else {
			params.add(new BasicNameValuePair("mode", "2"));
		}
        params.add(new BasicNameValuePair("type", "Firmware"));
        params.add(new BasicNameValuePair("cltp", "10"));
		if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_INSTALL)) {
			params.add(new BasicNameValuePair("cktp", FotaConstants.FOTA_CHECK_TYPE_VALUE_MANUAL));
		} else {
			params.add(new BasicNameValuePair("cktp", mCheckType));
		}
        params.add(new BasicNameValuePair("rtd",
				isRooted ? FotaConstants.FOTA_ROOT_FLAG_VALUE_YES
                        : FotaConstants.FOTA_ROOT_FLAG_VALUE_NO));
        params.add(new BasicNameValuePair("chnl",
                FotaUtil.isWifiOnline(this) ? FotaConstants.FOTA_CONNECT_TYPE_VALUE_WIFI
                        : FotaConstants.FOTA_CONNECT_TYPE_VALUE_3G));
        try {
            String locationinfo = Settings.System.getString(getContentResolver(), "tcl_ota_locationinfo");
            SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
            boolean isFirstTime = prefs.getBoolean(FotaConstants.FIRST_TIME_SEND_LOCATION, true);
            FotaLog.d(TAG, "doCheck -> locationinfo = " + locationinfo + ", isFirstTime = " + isFirstTime);
            if (locationinfo != null && !locationinfo.equals("0_0") && isFirstTime){
                isSendLocation = true;
                params.add(new BasicNameValuePair("lng", locationinfo.split("_")[0]));
                params.add(new BasicNameValuePair("lat", locationinfo.split("_")[1]));
            }
        } catch (Exception e){
            FotaLog.d(TAG, "doCheck -> locationinfo Exception");
            e.printStackTrace();
        }

        String param = URLEncodedUtils.format(params, "UTF-8");

		String baseUrl = "http://" + mBaseUrl + "/check.php";
        FotaLog.v(TAG, "httpGetForCheck -> url = " + baseUrl + "?" + param);
		httpGetForCheck(baseUrl + "?" + param);
    } 

    /**
     * this listener will handle Special Options got from server on every success check.
     */
    private OnGotSpopsListener mOnGotSpopsListener = new OnGotSpopsListener() {
		
    	FotaPref mPref = FotaPref.getInstance(FotaApp.getApp());

        @Override
        public void onGotSpops(List<Spop> spops) {
            for (Spop s : spops) {
                // Log.d("test", s.type + ": " + s.data);
                if (TextUtils.isEmpty(s.data)) {
                    break;
                }
                // TODO: change strange numbers to constants.
                switch (s.type) {
                    case 1:
                        break;
                    case 2:
                        FotaLog.d(TAG, "onGotSpops -> setDefaultCheckFrequency");
    					FotaUtil.setDefaultCheckFrequency(getApplicationContext(),
    							ResponseParser.parseCheckPeriod(s.data));
                        // Log.d("test`", "@@@@"+
                        // ResponseParser.parseCheckPeriod(s.data));
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
//					try {
//						FotaUtil.setCheckWifiOnly(getApplicationContext(),
//								ResponseParser.parseCheckWifiOnly(s.data));
//						// Log.d("test", "@@@@"+
//						// ResponseParser.parseCheckWifiOnly(s.data));
//					} catch (JSONException e) {
//						// not care
//					}
//					try {
//						FotaUtil.setDownloadWifiOnly(getApplicationContext(),
//								ResponseParser.parseDownloadWifiOnly(s.data));
//					} catch (JSONException e) {
//						// not care
//						FotaLog.v(TAG, e.toString());
//					}
					break;
				case 7:
//					try {
//						ResponseParser.parsePriority(s.data);
//					} catch (JSONException e) {
//						// not care
//						FotaLog.v(TAG, e.toString());
//					}
//					try {
//						FotaUtil.setAutoDownload(getApplicationContext(),
//								ResponseParser.parseAutoDownload(s.data));
//						// Log.d("test", "@@@@"+
//						// ResponseParser.parseAutoDownload(s.data));
//					} catch (JSONException e) {
//						// not care
//						// Log.d("test", e.toString());
//					}
//					try {
//						FotaUtil.setAutoInstall(getApplicationContext(),
//								ResponseParser.parseAutoInstall(s.data));
//					} catch (JSONException e) {
//						// not care
//						FotaLog.v(TAG, e.toString());
//					}
					break;
				case 8:
//					FotaUtil.setRemindPeriod(getApplicationContext(),
//							ResponseParser.parseRemindPeriod(s.data));
//					// Log.d("test", "@@@@"+
//					// ResponseParser.parseRemindPeriod(s.data));
//					break;
				case 9:
//					try {
//                            FotaUtil.setRootUpdate(getApplicationContext(),
//                                    ResponseParser.parseRootUpdate(s.data));
//                            // Log.d("test", "@@@@"+
//                            // ResponseParser.parseRootUpdate(s.data));
//					} catch (JSONException e) {
//						//not care
//						FotaLog.v(TAG, e.toString());
//					}
					break;
				case 10:
//					try {
//						ResponseParser.parseCheckTimeBegin(s.data);
//					} catch (JSONException e) {
//						//not care
//						FotaLog.v(TAG, e.toString());
//					}
//					try {
//						ResponseParser.parseCheckTimeEnd(s.data);
//					} catch (JSONException e) {
//						//not care
//						FotaLog.v(TAG, e.toString());
//					}
					break;
				case 11:
//					try {
//						mPref.setClearStatus(ResponseParser.parseClearStatus(s.data));
//						// Log.d("test", "@@@@"+
//						// ResponseParser.parseClearStatus(s.data));
//					} catch (JSONException e) {
//						// not care
//						FotaLog.v(TAG, e.toString());
//					}
					break;
				case 12:
//					try {
//						ResponseParser.parseMaxWifiDownloadSize(s.data);
//						// Log.d("test", "@@@@"+
//						// ResponseParser.parseMaxWifiDownloadSize(s.data));
//						FotaUtil.setMaxWifiDownloadSize(ResponseParser
//								.parseMaxWifiDownloadSize(s.data));
//					} catch (JSONException e) {
//						// not care
//						FotaLog.v(TAG, e.toString());
//					}
					break;
				case 13:
//					try {
//						FotaUtil.setMaxPostponeTimes(getApplicationContext(),
//								ResponseParser.parseRemindCount(s.data));
//						// Log.d("test", "@@@@"+
//						// ResponseParser.parseRemindCount(s.data));
//					} catch (JSONException e) {
//						// not care
//						FotaLog.v(TAG, e.toString());
//					}
					break;
				case 14:
//					try {
//						FotaLog.d(TAG, "Log:" + ResponseParser.parseLogUpload(s.data));
//						// FotaUtil.setMaxPostponeTimes(getApplicationContext(),
//						// ResponseParser.parseLogUpload(s.data));
//					} catch (JSONException e) {
//						// not care
//						FotaLog.v(TAG, "Log:" + e.toString());
//					}
//					break;
                }
            }
        }
    };

    /**
     * We choose http get method to get UpdatePackageInfo.
     */
    private void httpGetForCheck(String realUrl) {

        HttpURLConnection connection = null;
        try {
            URL url = new URL(realUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("User-Agent", FotaUtil.getUserAgentString(this));
            connection.setRequestProperty("Connection", "close");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setReadTimeout(FotaConstants.DEFAULT_GET_TIMEOUT);
            connection.setConnectTimeout(FotaConstants.DEFAULT_GET_TIMEOUT);
            connection.connect();
            int statusCode = connection.getResponseCode();
            FotaLog.v(TAG, "httpGetForCheck -> isFullUpdate = " + isFullUpdate
					+ ", statusCode = " + statusCode);
            switch (statusCode) {
                case HttpURLConnection.HTTP_OK:
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    UpdatePackageInfo info = ResponseParser.parseCheckResponse(in, mOnGotSpopsListener);
                    FotaLog.v(TAG, "httpGetForCheck -> UpdatePackageInfo = " + info);
                    onCheckSuccess(info);
                    if (isSendLocation){
                        updateSendLocationInfoState();
                    }
                    break;
                case HttpURLConnection.HTTP_NO_CONTENT:
                case HttpURLConnection.HTTP_NOT_FOUND:
                    if (isSendLocation){
                        updateSendLocationInfoState();
                    }
                    if (isFullUpdate) {
                        onNoVersionFound();
                    } else {
                        isFullUpdate = true;
                        FotaLog.d(TAG, "httpGetForCheck -> No incremental package, check full updates");
                        doCheck(mCheckType);
                    }
				    break;
                default:
                    FotaLog.d(TAG, "httpGetForCheck -> Error HTTP Code = " + statusCode);
                    onServerException();
                    break;
            }
        } catch (ConnectTimeoutException e) {
            if (mCheckAgain) {
                mCheckAgain = false;
                mBaseUrl = FotaUtil.getRandomUrl();
                FotaLog.d(TAG, "httpGetForCheck -> ConnectTimeoutException, check again");
                doCheck(mCheckType);
            } else {
                mCheckAgain = true;
                onConnectTimeout();
            }
        } catch (Exception e) {
            FotaLog.d(TAG, "httpGetForCheck error happened during check: " + e.toString());
            onError();
            return;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
	}

	/**
	 *  We use http post to get DownloadInfo.
	 */
    private void httpPostForFurtherCheck(UpdatePackageInfo info, String realUrl,
										 String params, boolean isChunck) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(realUrl);
            connection = (HttpURLConnection) url.openConnection();
            byte[] content = params.getBytes();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            if (isChunck) {
                connection.setChunkedStreamingMode(0);
            } else {
                connection.setFixedLengthStreamingMode(content.length);
            }
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("User-Agent", FotaUtil.getUserAgentString(this));
            connection.setRequestProperty("Connection", "close");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setReadTimeout(FotaConstants.DEFAULT_GET_TIMEOUT);
            connection.setConnectTimeout(FotaConstants.DEFAULT_GET_TIMEOUT);
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            FotaLog.v(TAG, "httpPostForFurtherCheck -> " + params);
            out.write(content);
            out.flush();
            out.close();
            int statusCode = connection.getResponseCode();
            FotaLog.d(TAG, "httpPostForFurtherCheck -> statusCode = " + statusCode);
            switch (statusCode) {
                case HttpURLConnection.HTTP_OK:
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    DownloadInfo dInfo = ResponseParser.parsePreDownloadResponse(in);
					FotaLog.v(TAG, "httpPostForFurtherCheck -> DownloadInfo = " + dInfo);
					onPreDownloadSuccess(info, dInfo);
					break;
				case HttpURLConnection.HTTP_NO_CONTENT:
					onNoDownloadInfo(info);
                    break;
                case HttpURLConnection.HTTP_LENGTH_REQUIRED:
                    httpPostForFurtherCheck(info, realUrl, params, false);
                    break;
                default:
					onGetDownloadInfoError(info);
                    break;
            }
        } catch (Exception e) {
            FotaLog.d(TAG, "httpPostForFurtherCheck -> Exception happened during check: " + e.toString());
			onGetDownloadInfoError(info);
            return;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String generateVk(UpdatePackageInfo info, String salt) {

        List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("id", FotaUtil.IMEI(getApplicationContext())));
        params.add(new BasicNameValuePair("salt", salt));
        params.add(new BasicNameValuePair("curef", FotaUtil.REF()));

        params.add(new BasicNameValuePair("fv", info.mFv));
        params.add(new BasicNameValuePair("tv", info.mTv));
        params.add(new BasicNameValuePair("type", "Firmware"));
        params.add(new BasicNameValuePair("fw_id", info.mFirmwareId));
		if (isFullUpdate) {
			params.add(new BasicNameValuePair("mode", "7"));
		} else {
			params.add(new BasicNameValuePair("mode", "2"));
		}
        params.add(new BasicNameValuePair("cltp", "10" + FotaUtil.appendTail()));

        String param = URLEncodedUtils.format(params, "UTF-8");
        return FotaUtil.SHA1(param);
    }

    /**
     * This is step 2.
     *
     * @param info
     */
    private void doAfterCheck(UpdatePackageInfo info) {
        FotaLog.v(TAG, "doAfterCheck");
        String salt = FotaUtil.salt();

        List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("id", FotaUtil.IMEI(getApplicationContext())));
        params.add(new BasicNameValuePair("curef", FotaUtil.REF()));
        params.add(new BasicNameValuePair("vk", generateVk(info, salt)));
        params.add(new BasicNameValuePair("fw_id", info.mFirmwareId));
        params.add(new BasicNameValuePair("fv", info.mFv));
        params.add(new BasicNameValuePair("tv", info.mTv));
        params.add(new BasicNameValuePair("type", "Firmware"));
		if (isFullUpdate) {
			params.add(new BasicNameValuePair("mode", "7"));
		} else {
			params.add(new BasicNameValuePair("mode", "2"));
		}
		params.add(new BasicNameValuePair("cltp", "10"));
		if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_INSTALL)) {
			params.add(new BasicNameValuePair("cktp", FotaConstants.FOTA_CHECK_TYPE_VALUE_MANUAL));
		} else {
			params.add(new BasicNameValuePair("cktp", mCheckType));
		}
        params.add(new BasicNameValuePair("rtd",
				isRooted ? FotaConstants.FOTA_ROOT_FLAG_VALUE_YES
                        : FotaConstants.FOTA_ROOT_FLAG_VALUE_NO));
        params.add(new BasicNameValuePair("chnl",
                FotaUtil.isWifiOnline(this) ? FotaConstants.FOTA_CONNECT_TYPE_VALUE_WIFI
                        : FotaConstants.FOTA_CONNECT_TYPE_VALUE_3G));
		params.add(new BasicNameValuePair("salt", salt));
		
		String param = URLEncodedUtils.format(params, "UTF-8");
		FotaLog.v(TAG, "doAfterCheck -> " + param);

		String baseUrl = "http://" + mBaseUrl + "/download_request.php";
		FotaLog.d(TAG, "httpPostForFurtherCheck -> url = " + baseUrl + "?" + param);
        httpPostForFurtherCheck(info, baseUrl, param, true);
    }
    
    /**
     * This is step 3 .
     *
     * @param uInfo
     * @param dInfo
     */
    private void onPreDownloadSuccess(UpdatePackageInfo uInfo, DownloadInfo dInfo) {
        DownloadTask task = new DownloadTask();
        task.setId(FotaUtil.salt());
        task.setUpdateInfoJson(uInfo.toJson());
        task.setDownlaodInfoJson(dInfo.toJson());
        task.setState(State.CHECKED.name());
        long total = 0;
        for (int i = 0; i < uInfo.mFileCount; i++) {
            total += uInfo.mFiles.get(i).mFileSize;
        }
        task.setTotalBytes(total);

		FotaUIPresenter fotaUIPresenter = FotaUIPresenter.getInstance(this);
        DownloadTask currentTask = fotaUIPresenter.getCurrentDownloadTask();
        FotaLog.d(TAG, "onPreDownloadSuccess -> currentTask = " + currentTask);
        if (currentTask != null && fotaUIPresenter.haveActvieDownloadTask()) {
            String currentTaskTv = currentTask.getUpdateInfo().mTv;
            String checkedTaskTv = task.getUpdateInfo().mTv;
            FotaLog.d(TAG, "onPreDownloadSuccess -> currentTaskTv = " + currentTaskTv + ", "
                            + "checkedTaskTv = " + checkedTaskTv);
			int cmp = currentTaskTv.compareToIgnoreCase(checkedTaskTv);
			if (cmp == 0) {
				onCurrentDownloadIsVaild();
			} else if (cmp > 0) {
				onCurrentDownloadIsDiscard(task);
			} else {
				onCurrentDownloadIsExpired(task);
			}
			return;
        } else {
            updateDownloadTask(task);
        }

        PreferenceManager.getDefaultSharedPreferences(FotaCheckService.this).edit()
                .putBoolean(FotaConstants.DOWNLOAD_IS_FULL_PACKAGE, isFullUpdate).apply();

        if (!FotaUIPresenter.getInstance(this).isMainActivityActive()
				&& mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_AUTO)) {
            // There are updates available
            // The notification should launch the main app
            FotaNotification
                    .updateFotaNotification(this, FotaNotification.TYPE_NEW_VERSION, null);

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			int defaultUpdateFrequency = prefs.getInt(FotaConstants.DEFAULT_UPDATE_CHECK_PREF,
					FotaUtil.getDefaultAutoCheckVal());
			int updateFrequency = prefs.getInt(FotaConstants.UPDATE_CHECK_PREF,
					FotaUtil.getDefaultAutoCheckVal());

			FotaLog.d(TAG, "onPreDownloadSuccess -> defaultUpdateFrequency = " + defaultUpdateFrequency
					+ ", updateFrequency = " + updateFrequency);
			if (updateFrequency != defaultUpdateFrequency) {
				FotaUtil.setCheckFrequency(this, defaultUpdateFrequency);
			} else {
				FotaUtil.setCheckFrequency(this, defaultUpdateFrequency / 2);
			}

            // update last update notification time
            PreferenceManager.getDefaultSharedPreferences(FotaCheckService.this).edit()
                    .putLong(FotaConstants.LAST_UPDATE_NOTIFICATION_TIME, System.currentTimeMillis())
                    .apply();
            FotaUIPresenter.getInstance(this).updateCheckState(State.IDLE);
        } else {
            FotaUIPresenter.getInstance(this)
                    .showCheckResult(FotaUIPresenter.FOTA_CHECK_RESULT_GET_NEW_VERSION);
        }
    }

	/**
	 * Check successful, Server response new version found on Server.
	 * @param info
     */
    private void onCheckSuccess(UpdatePackageInfo info){
    	if(info == null || info.mFirmwareId == null || info.mFv == null || info.mTv == null){
    		onError();
    		return ;
    	}

		FotaUIPresenter fotaUIPresenter = FotaUIPresenter.getInstance(this);
		DownloadTask currentTask = fotaUIPresenter.getCurrentDownloadTask();
		FotaLog.d(TAG, "onCheckSuccess -> currentTask = " + currentTask);
		if (currentTask != null && fotaUIPresenter.haveActvieDownloadTask()) {
			String currentTaskTv = currentTask.getUpdateInfo().mTv;
			String checkTaskTv = info.mTv;
			FotaLog.d(TAG, "onCheckSuccess -> currentTaskTv = " + currentTaskTv + ", "
					+ "checkTaskTv = " + checkTaskTv);
			if (currentTaskTv.compareToIgnoreCase(checkTaskTv) == 0) {
				onCurrentDownloadIsVaild();
				return;
			}
		}

        Intent intent = new Intent("com.tcl.monster.fota.GET_NEW_FIRMWARE");
        intent.putExtra("VERSIONCODE", info.mTv);
        FotaCheckService.this.sendBroadcast(intent);
        doAfterCheck(info);
    }

	/**
	 * Server response version not found on Server.
	 */
	private void onNoVersionFound() {
		if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_AUTO)) {
			FotaLog.d(TAG, "automatic background check, not need notify VersionNotFound");
			return;
		} else if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_INSTALL)) {
			FotaUIPresenter.getInstance(this).scheduleDeleteAfterCheck(null,
					FotaUIPresenter.FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_INVALID);
		} else {
			if (FotaUIPresenter.getInstance(this).haveActvieDownloadTask()) {
				FotaUIPresenter.getInstance(this).scheduleDeleteAfterCheck(null,
						FotaUIPresenter.FOTA_CHECK_RESULT_DOWNLOAD_VERSION_INVALID);
			} else {
				FotaUIPresenter.getInstance(this)
						.showCheckResult(FotaUIPresenter.FOTA_CHECK_RESULT_NO_NEW_VERSION);
			}
		}
	}

	/**
	 * Server exception.
	 *
	 * Exception HTTP Code:
	 * 400 - Bad Request
	 * 408 - Request Timeout
	 * 500 - Internal Server Error
	 * 503 - Service Unavailable
	 */
	private void onServerException(){
		if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_AUTO)) {
			FotaLog.d(TAG, "automatic background check, not need notify ServerException");
			return;
		} else if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_INSTALL)) {
			FotaUIPresenter.getInstance(this)
					.showCheckResult(FotaUIPresenter.FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_VALID);
		} else {
			if (FotaUIPresenter.getInstance(this).haveActvieDownloadTask()) {
				FotaUIPresenter.getInstance(this)
						.showCheckResult(FotaUIPresenter.FOTA_CHECK_RESULT_DOWNLOAD_VERSION_VALID);
			} else {
				FotaUIPresenter.getInstance(this)
						.showCheckResult(FotaUIPresenter.FOTA_CHECK_RESULT_SERVER_EXCEPTION);
			}
		}
	}

	/**
	 * Connect Timeout
	 */
	private void onConnectTimeout(){
		if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_AUTO)) {
			FotaLog.d(TAG, "automatic background check, not need notify ConnectTimeout");
			return;
		} else if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_INSTALL)) {
			FotaUIPresenter.getInstance(this)
					.showCheckResult(FotaUIPresenter.FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_VALID);
		} else {
			if (FotaUIPresenter.getInstance(this).haveActvieDownloadTask()) {
				FotaUIPresenter.getInstance(this)
						.showCheckResult(FotaUIPresenter.FOTA_CHECK_RESULT_DOWNLOAD_VERSION_VALID);
			} else {
				FotaUIPresenter.getInstance(this)
						.showCheckResult(FotaUIPresenter.FOTA_CHECK_RESULT_CONNECT_TIMEOUT);
			}
		}
	}

	/**
	 *  Error happend, we should prompt user that the connection is not stable and please try later.
	 *  TODO: We need collect failed logs here
	 */
	private void onError(){
		if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_AUTO)) {
			FotaLog.d(TAG, "automatic background check, not need notify checkError");
		} else if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_INSTALL)) {
			FotaUIPresenter.getInstance(this)
					.showCheckResult(FotaUIPresenter.FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_VALID);
		} else {
			if (FotaUIPresenter.getInstance(this).haveActvieDownloadTask()) {
				FotaUIPresenter.getInstance(this)
						.showCheckResult(FotaUIPresenter.FOTA_CHECK_RESULT_DOWNLOAD_VERSION_VALID);
			} else {
				FotaUIPresenter.getInstance(this)
						.showCheckResult(FotaUIPresenter.FOTA_CHECK_RESULT_CHECK_ERROR);
			}
		}
	}

	/**
	 * Server response new version found on Server, but not download info.
	 * @param info
     */
	private void onNoDownloadInfo(UpdatePackageInfo info) {
		if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_AUTO)) {
			FotaLog.d(TAG, "automatic background check, not need notify NoDownloadInfo");
			return;
		}

		FotaUIPresenter fotaUIPresenter = FotaUIPresenter.getInstance(this);
		DownloadTask currentTask = fotaUIPresenter.getCurrentDownloadTask();
		FotaLog.d(TAG, "onNoDownloadInfo -> currentTask = " + currentTask);
		if (currentTask != null && fotaUIPresenter.haveActvieDownloadTask()) {
			String currentTaskTv = currentTask.getUpdateInfo().mTv;
			String checkTaskTv = info.mTv;
			FotaLog.d(TAG, "onNoDownloadInfo -> currentTaskTv = " + currentTaskTv + ", "
					+ "checkTaskTv = " + checkTaskTv);
			int cmp = currentTaskTv.compareToIgnoreCase(checkTaskTv);
			if (cmp == 0) {
				onCurrentDownloadIsVaild();
			} else if (cmp > 0) {
				onCurrentDownloadIsDiscard(null);
			} else {
				onCurrentDownloadIsExpired(null);
			}
		} else {
			FotaUIPresenter.getInstance(this)
					.showCheckResult(FotaUIPresenter.FOTA_CHECK_RESULT_NO_NEW_VERSION);
		}
	}

	/**
	 * Server response new version found on Server, but get download info failed
	 * @param info
	 */
	private void onGetDownloadInfoError(UpdatePackageInfo info) {
		if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_AUTO)) {
			FotaLog.d(TAG, "automatic background check, not need notify NoDownloadInfo");
			return;
		}

		FotaUIPresenter fotaUIPresenter = FotaUIPresenter.getInstance(this);
		DownloadTask currentTask = fotaUIPresenter.getCurrentDownloadTask();
		FotaLog.d(TAG, "onGetDownloadInfoError -> currentTask = " + currentTask);
		if (currentTask != null && fotaUIPresenter.haveActvieDownloadTask()) {
			String currentTaskTv = currentTask.getUpdateInfo().mTv;
			String checkTaskTv = info.mTv;
			FotaLog.d(TAG, "onGetDownloadInfoError -> currentTaskTv = " + currentTaskTv + ", "
					+ "checkTaskTv = " + checkTaskTv);
			int cmp = currentTaskTv.compareToIgnoreCase(checkTaskTv);
			if (cmp == 0) {
				onCurrentDownloadIsVaild();
			} else if (cmp > 0) {
				onCurrentDownloadIsDiscard(null);
			} else {
				onCurrentDownloadIsExpired(null);
			}
		} else {
			FotaUIPresenter.getInstance(this)
					.showCheckResult(FotaUIPresenter.FOTA_CHECK_RESULT_CHECK_ERROR);
		}
	}

	/**
	 * Server response new version found on Server, and current download version = server version
	 */
	private void onCurrentDownloadIsVaild() {
		if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_AUTO)) {
			FotaLog.d(TAG, "automatic background check, not need notify CurrentDownloadIsVaild");
			return;
		} else if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_INSTALL)) {
			FotaUIPresenter.getInstance(this)
					.showCheckResult(FotaUIPresenter.FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_VALID);
		} else {
			FotaUIPresenter.getInstance(this)
					.showCheckResult(FotaUIPresenter.FOTA_CHECK_RESULT_DOWNLOAD_VERSION_VALID);
		}
	}

	/**
	 * Server response new version found on Server, and current download version < server version
	 */
	private void onCurrentDownloadIsExpired(DownloadTask task) {
		if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_AUTO)) {
			FotaLog.d(TAG, "automatic background check, not need notify CurrentDownloadIsExpired");
			return;
		}

        saveNewDownloadTask(task);
        if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_INSTALL)) {
			FotaUIPresenter.getInstance(this).scheduleDeleteAfterCheck(task,
					FotaUIPresenter.FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_EXPIRED);
		} else {
			FotaUIPresenter.getInstance(this).scheduleDeleteAfterCheck(task,
					FotaUIPresenter.FOTA_CHECK_RESULT_DOWNLOAD_VERSION_EXPIRED);
		}
	}

	/**
	 * Server response new version found on Server, and current download version > server version
	 */
	private void onCurrentDownloadIsDiscard(DownloadTask task) {
		if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_AUTO)) {
			FotaLog.d(TAG, "automatic background check, not need notify CurrentDownloadIsDiscard");
			return;
		}

        saveNewDownloadTask(task);
        if (mCheckType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_INSTALL)) {
			FotaUIPresenter.getInstance(this).scheduleDeleteAfterCheck(task,
					FotaUIPresenter.FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_DISCARD);
		} else {
			FotaUIPresenter.getInstance(this).scheduleDeleteAfterCheck(task,
					FotaUIPresenter.FOTA_CHECK_RESULT_DOWNLOAD_VERSION_DISCARD);
		}
	}

    private void saveNewDownloadTask(DownloadTask task) {
        if (task != null) {
            DownloadEngine downloadEngine = DownloadEngine.getInstance();
            downloadEngine.init(this);
            downloadEngine.saveDownloadTask(task);
        }
    }

	private void updateDownloadTask(DownloadTask task) {
		if (task != null) {
			DownloadEngine downloadEngine = DownloadEngine.getInstance();
			downloadEngine.init(this);
			downloadEngine.saveDownloadTask(task);
			FotaPref.getInstance(this).setString(FotaConstants.DOWNLOAD_ID, task.getId());
			FotaUIPresenter.getInstance(this).setCurrentDownloadTask(task);
		} else {
			FotaUIPresenter.getInstance(this).setCurrentDownloadTask(null);
		}
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        FotaUIPresenter.getInstance(this).correctCheckingState();
    }

    public void updateSendLocationInfoState(){
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                .putBoolean(FotaConstants.FIRST_TIME_SEND_LOCATION, false)
                .apply();
    }
}