#this "sdk_tcl_ui/Android.mk" no need be contains to build
#"sdk_tcl_ui/src" and "sdk_tcl_ui/res" have be contains in "Monster_Mms/Android.mk"

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := xysdk
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4
LOCAL_SDK_VERSION := current
LOCAL_SRC_FILES := \
     $(call all-java-files-under, src) \
     $(call all-logtags-files-under, src)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
include $(BUILD_STATIC_JAVA_LIBRARY)

# Build all sub-directories
#include $(call all-makefiles-under,$(LOCAL_PATH))
