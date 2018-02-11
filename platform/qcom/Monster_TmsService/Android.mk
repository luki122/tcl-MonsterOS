LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libams-1.1.9-mfr_ser:libs/arm64-v8a/libams-1.1.9-mfr.so  \
                       libams-1.1.9-m-mfr_ser:libs/arm64-v8a/libams-1.1.9-m-mfr.so \
                       libbuffalo-1.0.0-mfr_ser:libs/arm64-v8a/libbuffalo-1.0.0-mfr.so \
                       libbumblebee-1.0.4-mfr_ser:libs/arm64-v8a/libbumblebee-1.0.4-mfr.so \
                       libdce-1.1.14-mfr_ser:libs/arm64-v8a/libdce-1.1.14-mfr.so \
                       liboptimus-1.0.0-mfr_ser:libs/arm64-v8a/liboptimus-1.0.0-mfr.so \
                       libQQImageCompare-1.2-mfr_ser:libs/arm64-v8a/libQQImageCompare-1.2-mfr.so \
                      libTmsdk-2.0.9-mfr_ser:libs/arm64-v8a/libTmsdk-2.0.9-mfr.so
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := tms_ser:./libs/tms.jar 
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src)

LOCAL_SRC_FILES += src/com/mst/tms/ITmsService.aidl \
                 src/com/mst/tms/ITrafficCorrectListener.aidl \

LOCAL_PACKAGE_NAME := Monster_TmsService

LOCAL_JAVA_LIBRARIES := telephony-common \
                        org.apache.http.legacy \
                        mst-framework

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-common \
    tms_ser

LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := disabled
LOCAL_JNI_SHARED_LIBRARIES := libams-1.1.9-mfr_ser \
                              libams-1.1.9-m-mfr_ser \
                              libbuffalo-1.0.0-mfr_ser \
                              libbumblebee-1.0.4-mfr_ser \
                              libdce-1.1.14-mfr_ser \
                              liboptimus-1.0.0-mfr_ser \
                              libQQImageCompare-1.2-mfr_ser \
                              libTmsdk-2.0.9-mfr_ser


include $(BUILD_PACKAGE)

