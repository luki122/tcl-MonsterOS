package com.android.camera;

public class FyuseAPI {
    public static final String FYUSE_SDK_APPNAME = "com.fyusion.fyuse";
    public static final String FYUSE_PACKAGE_NAME = "com.fyusion.sdk";
    public static final String FYUSE_SDK = "FyuseSDK";

    public static final int VERSION = 2;
    public static final String COMMAND = "command";
    public static final String VERSION_KEY = "version";
    public interface Action {
        String LIST_DIRECTORY = "listDirectory";
        String DELETE_FILE = "deleteFile";
        String START_CAMERA = ".Camera.SDKCameraActivity";
        String RESUME_CAMERA = ".Camera.CameraActivity";
        String OPEN_VIEWER = ".FullScreenActivity";
    }
}
