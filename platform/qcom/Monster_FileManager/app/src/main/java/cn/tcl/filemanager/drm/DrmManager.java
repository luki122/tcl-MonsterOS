/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.drm;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.drm.DrmManagerClient;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mtk.drm.frameworks.MtkDrmManager;
import com.tct.drm.api.TctDrmManager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.tcl.filemanager.PlfUtils;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.FileUtils;
import cn.tcl.filemanager.utils.LogUtils;

public class DrmManager {

    private static final String TAG = "DrmManager";
    private static final int NO_DRM = -1;
    public static final int MTK_DRM = 10;
    public static final int QCOM_DRM = 20;

    public static final int ACTIONID_NOT_DRM = -1;
    public static final int ACTIONID_INVALID_DRM = -2;

    public static final int DRM_THUMBNAIL_WITH = 500;
    public static final String APP_DRM = "application/vnd.oma.drm";
    public static final String EXT_DRM_CONTENT = "dcf";

    public static final String REMAINING_REPEAT_COUNT = DrmStore.ConstraintsColumns.REMAINING_REPEAT_COUNT;
    public static final String LICENSE_START_TIME = DrmStore.ConstraintsColumns.LICENSE_START_TIME;
    public static final String LICENSE_EXPIRY_TIME = DrmStore.ConstraintsColumns.LICENSE_EXPIRY_TIME;
    public static final String LICENSE_AVAILABLE_TIME = DrmStore.ConstraintsColumns.LICENSE_AVAILABLE_TIME;

    public static int DRM_SCHEME_OMA1_FL;
    public static int DRM_SCHEME_OMA1_CD;
    public static int DRM_SCHEME_OMA1_SD;
    public static String RIGHTS_ISSUER;
    public static String CONSTRAINT_TYPE;
    public static String CONTENT_VENDOR;
    public static String TCT_IS_DRM = TctDrmManager.TCT_IS_DRM;//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
    public static final String TCT_DRM_TYPE = TctDrmManager.TCT_DRM_TYPE;
    public static final String TCT_DRM_RIGHT_TYPE = TctDrmManager.TCT_DRM_RIGHT_TYPE;
    public static final String TCT_DRM_VALID = TctDrmManager.TCT_DRM_VALID;
    public static final String DRM_TIME_OUT_ACTION = TctDrmManager.DRM_TIME_OUT_ACTION;

  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
    public static final String TCT_DRM_METHOD = MtkDrmManager.DRM_METHOD;
    public static int mCurrentDrm = NO_DRM;
  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
    private static boolean isDrmEnable;
    private static DrmManager sInstance;

    private TctDrmManager mTctDrmManager;
    private MtkDrmManager mMtkDrmManager;
    private DrmManagerClient mDrmManagerClient;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    //add for PR863409,PR866854 by yane.wang@jrdcom.com 20141212 begin
    public static int METHOD_FL = 1;
    public static int METHOD_SD = 3;
    private Context mContext;
  //add for PR863409,PR866854 by yane.wang@jrdcom.com 20141212 end

    public static void setScheme() {
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    DRM_SCHEME_OMA1_FL = MtkDrmManager.DRM_SCHEME_OMA1_FL;
                    DRM_SCHEME_OMA1_CD = MtkDrmManager.DRM_SCHEME_OMA1_CD;
                    DRM_SCHEME_OMA1_SD = MtkDrmManager.DRM_SCHEME_OMA1_SD;
                    RIGHTS_ISSUER = MtkDrmManager.RIGHTS_ISSUER;
                    CONSTRAINT_TYPE = TctDrmManager.CONSTRAINT_TYPE;
                    CONTENT_VENDOR = MtkDrmManager.CONTENT_VENDOR;
                    TCT_IS_DRM = MtkDrmManager.TCT_IS_DRM;
                  //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    break;
                case QCOM_DRM:
                    DRM_SCHEME_OMA1_FL = TctDrmManager.DRM_SCHEME_OMA1_FL;
                    DRM_SCHEME_OMA1_CD = TctDrmManager.DRM_SCHEME_OMA1_CD;
                    DRM_SCHEME_OMA1_SD = TctDrmManager.DRM_SCHEME_OMA1_SD;
                    RIGHTS_ISSUER = TctDrmManager.RIGHTS_ISSUER;
                    CONSTRAINT_TYPE = TctDrmManager.CONSTRAINT_TYPE;
                    CONTENT_VENDOR = TctDrmManager.CONTENT_VENDOR;
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * Constructor for DrmManager.
     */
    public DrmManager(Context context) {
    	mContext = context;
        mCurrentDrm = getDrmPlatform();
        setScheme();

	    if (isDrmEnable) {
	        mDrmManagerClient = new DrmManagerClient(mContext);
	        switch (mCurrentDrm) {
	            case MTK_DRM:
	                mMtkDrmManager = MtkDrmManager.getInstance(mContext);
	                break;
	            case QCOM_DRM:
	                mTctDrmManager = new TctDrmManager(mContext);
	                break;
	            default:
	                break;
	        }
	    }
    }

    /**
     * Get a DrmManager Object.
     *
     * @return a instance of DrmManager.
     */
    public static DrmManager getInstance(Context context) {
    	if (sInstance == null) {
    		sInstance = new DrmManager(context);
    	}
        return sInstance;
    }

    private boolean isMTKDrm() {
        try {
            Class<?> managerClass = Class.forName("com.mediatek.drm.OmaDrmClient");
            if (managerClass.getClass() != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (LinkageError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isQcomDrm() {
        try {
            Class<?> managerClass = Class.forName("com.tct.drm.TctDrmManagerClient");
            if (managerClass.getClass() != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (LinkageError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getDrmPlatform() {
        try {
            LogUtils.d(TAG, "def_DRM_included" + PlfUtils.getBoolean(mContext, "def_DRM_included"));
            // ADD START FOR PR1078727 BY HONGBIN.CHEN 20150906
            if (PlfUtils.getBoolean(mContext, "def_DRM_included")) {
                // ADD END FOR PR1078727 BY HONGBIN.CHEN 20150906
                if (isQcomDrm()) {
                    isDrmEnable = TctDrmManager.isDrmEnabled();
                    return QCOM_DRM;
                } else if (isMTKDrm()) {
                    isDrmEnable = MtkDrmManager.isDrmEnabled();
                    return MTK_DRM;
                }
            }
            isDrmEnable = false;
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
        }
        return NO_DRM;
    }

    /**
     * This method gets Bitmap of DRM file. (Draw a little lock icon at
     * right-down part over original icon)
     *
     * @param resources the resource to use
     * @param path absolute path of the DRM file
     * @param actionId action ID of the file, which is not unique for DRM file
     * @param iconId the ID of background icon, which the new icon draws on
     * @return Bitmap of the DRM file
     */
    public Bitmap overlayDrmIconSkew(Resources resources, String path, int actionId, int iconId) {
        Bitmap bitmap = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    bitmap = mMtkDrmManager.overlayDrmIconSkew(resources, path, actionId, iconId);
                    break;
                case QCOM_DRM:
                    bitmap = TctDrmManager.getDrmThumbnail(path, 48);
                    break;
                default:
                    break;
            }
        }
        return bitmap;
    }

    /**
     * Get original mimeType of a file.
     *
     * @param path The file's path.
     * @return original mimeType of the file.
     */
    public String getOriginalMimeType(String path) {
        if (isDrmEnable) {
        	return mDrmManagerClient.getOriginalMimeType(path);
        }
        return "";
    }

    /**
     * This method check weather the rights-protected content has valid right to
     * transfer.
     *
     * @param path path to the rights-protected content.
     * @return true for having right to transfer, false for not having the
     *         right.
     */
    public boolean canTransfer(String path) {
        boolean flag = false;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    flag = mMtkDrmManager.checkRightsStatus(path, DrmStore.Action.TRANSFER) == DrmStore.RightsStatus.RIGHTS_VALID;//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    break;
                case QCOM_DRM:
                    flag = TctDrmManager.checkRightsStatus(path, DrmStore.Action.TRANSFER)
                            != DrmStore.RightsStatus.RIGHTS_VALID;
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

    /**
     * check weather the rights-protected content has valid right or not
     *
     * @param path path to the rights-protected content.
     * @return true for having valid right, false for invalid right
     */
    public boolean isRightsStatus(String path) {
        boolean flag = false;
		if (isDrmEnable) {
			switch (mCurrentDrm) {
			case MTK_DRM:
				flag = mMtkDrmManager.isRightValid(path);
				break;
			case QCOM_DRM:
				flag = mTctDrmManager.isRightValid(path);
				break;
			default:
				break;
			}
		}
        return flag;
    }

    public static int getAction(String mime) {
        if (mime.startsWith(FileInfo.MIME_HAED_IMAGE)) {
            return DrmStore.Action.DISPLAY;
        } else if (mime.startsWith(FileInfo.MIME_HEAD_AUDIO)
                || mime.startsWith(FileInfo.MIME_HEAD_VIDEO)) {
            return DrmStore.Action.PLAY;
        }

        return DrmStore.Action.PLAY; // otherwise PLAY is returned.
    }

    /**
     * This static method check a file is DRM file, or not.
     *
     * @param fileName the file which need to be checked.
     * @return true for DRM file, false for not DRM file.
     */
    public static boolean isDrmFileExt(String fileName) {
        if (isDrmEnable) {
            String extension = FileUtils.getFileExtension(fileName);
            if (!TextUtils.isEmpty(extension) && (extension.equalsIgnoreCase(EXT_DRM_CONTENT) || extension.equalsIgnoreCase("dm"))) {
                return true; // all drm files cannot be copied
            }
        }
        return false;
    }

    public boolean isDrm(String path) {
        try {
            return executorService.submit(new newThread(path)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private class newThread implements Callable<Boolean> {
        private String path = null;

        public newThread(String path) {
            this.path = path;
        }

        public Boolean call() {
            boolean flag = Boolean.valueOf(false);
            if (isDrmEnable) {
                switch (mCurrentDrm) {
                    case MTK_DRM:
                        flag = mMtkDrmManager.isDrm(path);
                        break;
                    case QCOM_DRM:
                        flag = Boolean.valueOf(mTctDrmManager.isDrm(path));
                        break;
                    default:
                        break;
                }
            }
            return flag;
        }
    }

    public Bitmap getDrmVideoThumbnail(Bitmap bitmap, String filePath, int size) {
        Bitmap b = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                  //there is no getDrmVideoThumbnail fucntion in mtk platform
                    //b = bitmap;//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    break;
                case QCOM_DRM:
                    b = TctDrmManager.getDrmVideoThumbnail(bitmap, filePath, size);
                    break;
                default:
                    break;
            }
        }
        return b;
    }

    public Bitmap getDrmThumbnail(String filePath, int size) {
        Bitmap b = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    b = mMtkDrmManager.getDrmThumbnail(filePath, size);//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    break;
                case QCOM_DRM:
                    b = TctDrmManager.getDrmThumbnail(filePath, size);
                    break;
                default:
                    break;
            }
        }
        return b;
    }

    public Bitmap getDrmRealThumbnail(String filePath, BitmapFactory.Options options, int size) {
        Bitmap b = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    /*Pr 975811 zibin.wang add 2015.11.25 Start*/
                    //b=mMtkDrmManager.getDrmRealThumbnail(filePath, options, size);
                    if (mMtkDrmManager != null) {
                        b = mMtkDrmManager.getDrmThumbnail(filePath, size);//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    }
                    /*Pr 975811 zibin.wang add 2015.11.25 End*/
                    break;
                case QCOM_DRM:
                    b = TctDrmManager.getDrmRealThumbnail(filePath, options, size);
                    break;
                default:
                    break;
            }
        }
        return b;
    }

    public boolean isDrmSDFile(String path) {
        boolean flag = false;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    flag = mMtkDrmManager.isSdType(path);
                    break;
                case QCOM_DRM:
                    flag = mTctDrmManager.isSdType(path);
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

    public boolean isDrmCDFile(String path) {
        boolean flag = false;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    flag = mMtkDrmManager.isCDType(path);//[BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
                    break;
                case QCOM_DRM:
                    flag = mTctDrmManager.isCDType(path);
                    break;
                default:
                    break;
            }
        }
        return flag;
    }
  //[BUGFIX]-Add-BEGIN by TCTNB.ye.chen, 2015/02/15 PR-932969.
    public int getDrmScheme(String path) {
        int flag = TctDrmManager.DRM_SCHEME_OMA1_FL;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    //add for PR863409,PR866854 by yane.wang@jrdcom.com 20141212 begin
                    //flag = getMtkDrmScheme(path);
                    flag = mMtkDrmManager.getDrmScheme(path);
                    //add for PR863409,PR866854 by yane.wang@jrdcom.com 20141212 end
                    break;
                case QCOM_DRM:
                    flag = TctDrmManager.getDrmScheme(path);
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

    public ContentValues getMetadata(String path) {
        ContentValues c = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    c = mMtkDrmManager.getMetadata(path);
                    break;
                case QCOM_DRM:
                    c = TctDrmManager.getMetadata(path);
                    break;
                default:
                    break;
            }
        }
        return c;
    }

    public ContentValues getConstraints(String path, int action) {
        ContentValues c = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    c = mMtkDrmManager.getConstraints(path, action);
                    break;
                case QCOM_DRM:
                    c = TctDrmManager.getConstraints(path, action);
                    break;
                default:
                    break;
            }
        }
        return c;
    }

    public boolean isAllowForward(String path) {
        boolean flag = true;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case MTK_DRM:
                    flag = mMtkDrmManager.isAllowForward(path);
                    break;
                case QCOM_DRM:
                    flag = TctDrmManager.isAllowForward(path);
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

  //add for PR863409,PR866854 by yane.wang@jrdcom.com 20141212 begin
	public int getMtkDrmScheme(String path) {
        Uri uri = Uri.parse(path);
        Object drmObject = null;
        Class<?> reflectClassInfo = null;
        try {
            reflectClassInfo = Class.forName("com.mediatek.drm.OmaDrmClient");
            if (reflectClassInfo != null) {
                Method method = reflectClassInfo.getDeclaredMethod("newInstance",Context.class);
                Object obj = null;
                drmObject = (Object) method.invoke(obj,mContext);
                Method method2 = reflectClassInfo.getDeclaredMethod("getMethod",Uri.class);
                Integer ss = (Integer)method2.invoke(drmObject,uri);
                return ss;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
  //add for PR863409,PR866854 by yane.wang@jrdcom.com 20141212 end
  //[BUGFIX]-Add-BEGIN by NJTS.ye.chen,02/02/2015,PR907464
	public Bitmap getDrmBitmap(String path) {
        Bitmap bitmap = null;
		if (isDrmEnable) {
			switch (mCurrentDrm) {
			case MTK_DRM:
				bitmap = mMtkDrmManager.getDrmThumbnail(path, 200);// [BUGFIX]-Add by TCTNJ,ye.chen 2015-03-10,PR916400
				break;
			case QCOM_DRM:
				bitmap = mTctDrmManager.getDrmBitmap(path);
				break;
			default:
				break;
			}
		}
        return bitmap;
    }
  //[BUGFIX]-Add-BEGIN by NJTS.ye.chen,02/02/2015,PR907464

    //[BUGFIX]-Add-BEGIN by NJTS.Peng.Tian,12/16/2014,PR835313
	public Movie getMovie(Uri uri, Context context) {
        Movie movie = null;
		if (isDrmEnable) {
			switch (mCurrentDrm) {
			case MTK_DRM:
				movie = mMtkDrmManager.getMovie(convertUriToPath(uri, context));// [BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
				break;
			case QCOM_DRM:
				movie = TctDrmManager.getMovie(uri, context);
				break;
			default:
				break;
			}
		}
         return movie;
    }

	public Movie getMovie(String path) {
        Movie movie = null;
		if (isDrmEnable) {
			switch (mCurrentDrm) {
			case MTK_DRM:
				movie = mMtkDrmManager.getMovie(path);// [BUGFIX]-Add by TCTNJ,ye.chen, 2015-03-10,PR916400
				break;
			case QCOM_DRM:
				movie = TctDrmManager.getMovie(path);
				break;
			default:
				break;
			}
		}
        return movie;
    }

   public String convertUriToPath(Uri uri, Context context) {
        String path = null;
        if (null != uri) {
            String scheme = uri.getScheme();
            if (null == scheme || scheme.equals("")
                    || scheme.equals(ContentResolver.SCHEME_FILE)) {
                path = uri.getPath();

            } else if (scheme.equals("http")) {
                path = uri.toString();

            } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                String[] projection = new String[] { MediaStore.MediaColumns.DATA };
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(uri, projection, null, null, null);
                    if (null == cursor || 0 == cursor.getCount()) {
                        return null;
                    }
                    int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    path = cursor.getString(pathIndex);
                } catch (SQLiteException e) {
                	e.printStackTrace();
                    return null;
                    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-06,PR925646 begin
                } catch (IllegalArgumentException e) {
                	e.printStackTrace();
                    return null;
                    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-02-06,PR925646 end
                } finally {
                    if (null != cursor) {
                        cursor.close();
                    }
                }
            } else {
                  Log.e(Uri.class.getSimpleName(), "Given Uri scheme is not supported");
                return null;
            }
        }
        return path;
    }

    //[BUGFIX]-Add-END by NJTS.Peng.Tian
 //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-04-01,PR916400
   public void activateContent(Context context, String filepath) {
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   String drmtoast = context.getResources().getString(R.string.drm_toast_license_expired);
                   mMtkDrmManager.activateContent(context, filepath,drmtoast);
                   break;
               case QCOM_DRM:
                   mTctDrmManager.activateContent(context, filepath);
                   break;
               default:
               break;
           }
       }
   }

   public boolean hasCountConstraint(String filePath) {
       boolean flag = false;
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   flag = mMtkDrmManager.hasCountConstraint(filePath);
                   break;
               case QCOM_DRM:
                   flag = mTctDrmManager.hasCountConstraint(filePath);
                   break;
               default:
                   break;
           }
       }
       return flag;
   }

   public int checkRightsStatus(String path, int action) {
       int result = -1;
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   result = mMtkDrmManager.checkRightsStatus(path, action);//Task134580 support DRM in mtk platform by fengke at 2015.03.5
                   break;
               case QCOM_DRM:
                   result = TctDrmManager.checkRightsStatus(path, action);
                   break;
               default:
                   break;
           }
       }
       return result;
   }

   public void drmSetWallpaper(Context context, String filepath) {
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   if (!mMtkDrmManager.drmSetAsWallpaper(context, filepath)) {
                       String toastMsg = String.format(context.getResources().getString(R.string.drm_no_crop), filepath);
                       Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
                       return;
                   }
                   if (!TextUtils.isEmpty(filepath) && (new File(filepath)).exists()) {
                       Settings.System.putString(context.getContentResolver(), MtkDrmManager.WALLPAPER_FILEPATH, filepath);
                       mMtkDrmManager.watchingDrmWallpaperStatus(context, filepath);
                   }
                   break;
               case QCOM_DRM:
                   Settings.System.putString(context.getContentResolver(),TctDrmManager.NEW_WALLPAPER_DRMPATH,filepath);
                   break;
               default:
                   break;
           }
       }
   }

	public Bitmap getThumbnailConsume(String path) {
       Bitmap bitmap = null;
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   bitmap = mMtkDrmManager.getThumbnailConsume(path, 640);
                   break;
               case QCOM_DRM:
//                  bitmap = mTctDrmManager.getDrmBitmap(path);
                   break;
               default:
                   break;
             }
        }
       return bitmap;
   }

   public boolean hasRightsToShow(Context context, String filePath) {
       boolean result = false;
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   result = mMtkDrmManager.hasRightsToShow(context, filePath);
                   break;
               case QCOM_DRM:
                   //result = TctDrmManager.checkRightsStatus(path, action);
                   break;
               default:
                   break;
           }
       }
       return result;
   }

   public Dialog showConsumeDialog(Context context,
           DialogInterface.OnClickListener listener,
           DialogInterface.OnDismissListener dismissListener) {
       Dialog result = null;
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   result = MtkDrmManager.showConsumeDialog(context, listener, dismissListener);
                   break;
               case QCOM_DRM:
                   break;
               default:
                   break;
           }
       }
       return result;
   }

   public Dialog showSecureTimerInvalidDialog(Context context,
           DialogInterface.OnClickListener clickListener,
           DialogInterface.OnDismissListener dismissListener) {
       Dialog result = null;
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   result = MtkDrmManager.showSecureTimerInvalidDialog(context, clickListener, dismissListener);
                   break;
               case QCOM_DRM:
                   break;
               default:
                   break;
           }
       }
       return result;
   }



   public Dialog showRefreshLicenseDialog(Context context, String path) {
       Dialog result = null;
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   result = MtkDrmManager.showRefreshLicenseDialog(context, path);
                   break;
               case QCOM_DRM:
                   break;
               default:
                   break;
           }
       }
       return result;
   }

   public int consumeRights(String path, int action) {
       int result = MtkDrmManager.ERROR_UNKNOWN;
       if (isDrmEnable) {
           switch (mCurrentDrm) {
               case MTK_DRM:
                   result = mMtkDrmManager.consumeRights(path, action);
                   break;
               case QCOM_DRM:
                   break;
               default:
                   break;
           }
       }
       return result;
   }
 //[BUGFIX]-Add by TCTNJ,ye.chen, 2015-04-01,PR916400

	public void restoreWallpaper() {
		if (isDrmEnable) {
			if (mCurrentDrm == MTK_DRM) {
	           String filePath = Settings.System.getString(mContext.getContentResolver(), MtkDrmManager.WALLPAPER_FILEPATH);
	           if (!TextUtils.isEmpty(filePath) && !(new File(filePath)).exists()) {
	               mMtkDrmManager.checkDrmWallpaperStatus(mContext, filePath);
	           }
			}
		}
   }

	public void restoreRingtone() {
		if (isDrmEnable) {
			if (mCurrentDrm == MTK_DRM) {
				mMtkDrmManager.checkDrmRingtoneStatus(mContext, null);
			}
		}
	}
}
