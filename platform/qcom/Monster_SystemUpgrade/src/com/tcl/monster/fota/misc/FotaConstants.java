package com.tcl.monster.fota.misc;

import static android.text.format.DateUtils.SECOND_IN_MILLIS;

/**
 * All the constants for Fota
 * 
 * @author haijun.chen
 *
 */
public class FotaConstants {

    /**
     * Tims should retry for a download.
     */
	public static final int DOWNLOAD_RETRY_TIMES = 3 ;
	
    /**
     * Default time out value of download
     */
    public static final int DEFAULT_TIMEOUT = (int) (20 * SECOND_IN_MILLIS);
    /**
     * Default time out value of connecting to Internet .
     */
    public static final int DEFAULT_GET_TIMEOUT = (int) (25 * SECOND_IN_MILLIS);

    /**
     * The max postpone times of a downloaded package.
     */
    public static final int DEFAULT_MAX_POSTPONE = 3;
    /**
     * The parent dir of update package file.
     */
    public final static String UPDATE_FILE_DIR = ".fotadownload/";

    /**
     * Update file name .
     */
    public final static String UPDATE_FILE_NAME = "update.zip";

    /**
     * Update log file name.
     */
    public final static String UPDATE_LOG_NAME = "update.log";
	
	/**
     * Indicates check type .
     */
    public static final String FOTA_CHECK_TYPE_VALUE_AUTO = "1";
    public static final String FOTA_CHECK_TYPE_VALUE_MANUAL = "2";
    public static final String FOTA_CHECK_TYPE_VALUE_INSTALL = "3";
    public static final String FOTA_ROOT_FLAG = "root_flag";
    public static final String FOTA_ROOT_FLAG_VALUE_NO = "1"; // not be rooted
    public static final String FOTA_ROOT_FLAG_VALUE_YES = "2"; // be rooted
    public static final String FOTA_CONNECT_TYPE = "connect_type";
	public static final String FOTA_CONNECT_TYPE_VALUE_3G = "1"; //connect to sever by 3g net
	public static final String FOTA_CONNECT_TYPE_VALUE_WIFI = "2"; //connect to server by wifi net
    /**
     * Dialog types for DialogActivity
     */
    public static final int DIALOG_TYPE_CONFIRM_DOWNLOAD = 1;
    public static final int DIALOG_TYPE_CONFIRM_INSTALL = 2;

    /**
     * Download ID of the update package.
     */
    public static final String DOWNLOAD_ID = "download_id";

    /**
     * Download ID of the installed update package, for query version detail from DB.
     */
    public static final String INSTALLED_DOWNLOAD_ID = "installed_download_id";

    /**
     * Download package is Full Package or Incremental Package.
     */
    public static final String DOWNLOAD_IS_FULL_PACKAGE = "download_is_full_package";

    /**
     * The path saving the update package . Three possible paths,
     * first is "/cache/delta/.fotadownload/" ,second is "storage/sdcard0/.fotadownload/"
     *  ,third is "storage/sdcard1/.fotadownload/".
     */
    public static final String PATH_SAVING_UPDATE_PACKAGE = "path_saving_update_package";

    /**
     * Preference key for if need show network warn dialog.
     */
    public static final String NETWORK_WARN = "networkwarn";
    public static final String KEY_SLIDING_OPENED = "key_sliding_opened";

    // Preferences
    public static final String UPDATE_CHECK_PREF = "pref_update_check_interval";
    public static final String DEFAULT_UPDATE_CHECK_PREF = "pref_default_update_check_interval";
    public static final String LAST_UPDATE_CHECK_PREF = "pref_last_update_check";
    public static final String LAST_AUTO_UPDATE_CHECK_PREF = "pref_last_auto_update_check";
    public static final String KEY_CHECK_FREQUENCY = "auto_check_frequency";
    public static final String KEY_CHECK_WIFI_ONLY = "check_wifi_only";
    public static final String KEY_DOWNLOAD_WIFI_ONLY = "download_wifi_only";
    public static final String KEY_DELETE_CURRENT_TASK = "delete_current_task";
    public static final String KEY_ADVANCED_MODE = "advanced_mode";
    public static final String KEY_USER_IN_ADVANCED_MODE = "userinadvancedmode";
    public static final String KEY_RESERVE_SPACE_SUPPORT = "reserve_space_support";
	public static final String LAST_UPDATE_NOTIFICATION_TIME ="pref_last_update_notification";
    public static final String KEY_AUTO_CHECK_UPDATE="auto_check_update";//firmwarm udpate tag

    public static final String FIRST_TIME_SEND_LOCATION = "pref_first_time_send_location";
    // Update Check items
    public static final String BOOT_CHECK_COMPLETED = "boot_check_completed";
    public static final int UPDATE_FREQ_AT_BOOT = -1;
    public static final int UPDATE_FREQ_NONE = -2;
    public static final int UPDATE_FREQ_TWICE_DAILY = 43200;
    public static final int UPDATE_FREQ_DAILY = 86400;
    public static final int UPDATE_FREQ_WEEKLY = 604800;
    public static final int UPDATE_FREQ_BI_WEEKLY = 1209600;
    public static final int UPDATE_FREQ_MONTHLY = 2419200;


    // size buffer to install update .
    public static final long SIZE_BUFFER = 20 * 1024 * 1024;

    /**
     * The max size of log file.
     */
    public static final long LOG_FILE_SIZE = 1024 * 1024;

    /**
     * The cache dir size.
     */
    public static final long SIZE_CACHE_NEED = 160 * 1024 * 1024;

    // storage error constants
    public static final String NO_AVAILABLE_STORAGE = "no_available_storage";
    public static final String STORAGE_SPACE_NOT_ENOUGH = "storage_space_not_enough";
    public static final String STORAGE_NOT_AVAILABLE = "storage_not_available";

    /**
     * The reserved space for fota download.
     */
    public static final String PATH_RESERVED_STORAGE_FOR_FOTA = "/cache/delta/";

    public static final String GOTU_URL_1 = "g2master-us-east.tctmobile.com";
    public static final String GOTU_URL_2 = "g2master-us-west.tctmobile.com";
    public static final String GOTU_URL_3 = "g2master-eu-west.tctmobile.com";
    public static final String GOTU_URL_4 = "g2master-ap-south.tctmobile.com";
    public static final String GOTU_URL_5 = "g2master-ap-north.tctmobile.com";
    public static final String GOTU_URL_6 = "g2master-sa-east.tctmobile.com";
    public static final String GOTU_URL_7 = "g2master-cn-north.tctmobile.com";
}
