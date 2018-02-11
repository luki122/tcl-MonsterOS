LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under,src)


LOCAL_STATIC_JAVA_LIBRARIES := gson1 universal-imageloader
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4

LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk
LOCAL_JAVA_LIBRARIES += mst-framework

LOCAL_PACKAGE_NAME := Monster_Market
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := gson1:libs/gson-2.2.4.jar \
        universal-imageloader:libs/universal-image-loader-1.9.5.jar
include $(BUILD_MULTI_PREBUILT)
