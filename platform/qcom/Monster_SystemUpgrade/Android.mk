LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_PROGUARD_ENABLED := full
LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 \
				libgooglegson \
				libhttpcomponents

LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk
LOCAL_JAVA_LIBRARIES += mst-framework

#LOCAL_DEX_PREOPT := false
   
LOCAL_PACKAGE_NAME := Monster_SystemUpgrade

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libgooglegson:libs/gson-2.7.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += libhttpcomponents:libs/org.apache.httpcomponents.httpclient_4.5.1.jar
include $(BUILD_MULTI_PREBUILT)  
