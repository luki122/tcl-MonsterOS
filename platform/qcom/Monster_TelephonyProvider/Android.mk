LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PRIVILEGED_MODULE := true

#merge tct changes
LOCAL_SRC_FILES := $(call all-java-files-under,src)
LOCAL_SRC_FILES += $(call all-java-files-under, tct_src)

#befor ligy modify: TelephonyProvider
LOCAL_PACKAGE_NAME := Monster_TelephonyProvider
LOCAL_CERTIFICATE := platform

LOCAL_JAVA_LIBRARIES += telephony-common
LOCAL_STATIC_JAVA_LIBRARIES += android-common

include $(BUILD_PACKAGE)
