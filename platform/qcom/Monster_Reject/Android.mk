# Copyright 2007-2008 The Android Open Source Project
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_SRC_FILES += src/com/mst/tms/ITmsService.aidl

LOCAL_PACKAGE_NAME := Monster_Reject
LOCAL_CERTIFICATE := shared
LOCAL_STATIC_JAVA_LIBRARIES := \
    telephony-common \
    android-common \
    android-support-v4

# 引用mst-framework的资源
LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk
 
# 引用mst-framework的类
LOCAL_JAVA_LIBRARIES += mst-framework

include $(BUILD_PACKAGE)
include $(call all-makefiles-under,$(LOCAL_PATH))
