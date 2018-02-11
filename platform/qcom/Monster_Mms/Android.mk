# Copyright 2007-2008 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
# Include build dir from chips
chips_res_dir := chips/res
chips_src_dir := chips/src

# Include build dir from sdk_tcl_ui
xiaoyuan_dir := sdk_tcl_ui
xy_package := cn.com.xy.sms.sdk.ui

src_dirs := src \
        $(chips_src_dir) \
        $(xiaoyuan_dir)/src

res_dirs := res \
        $(chips_res_dir) \
        $(xiaoyuan_dir)/res

#$(shell rm -rf $(LOCAL_PATH)/chips)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

#add for Tencent service
LOCAL_SRC_FILES += src/com/mst/tms/ITmsService.aidl

LOCAL_PACKAGE_NAME := Monster_Mms
#LOCAL_CERTIFICATE := platform

# Builds against the public SDK
#LOCAL_SDK_VERSION := current

LOCAL_JAVA_LIBRARIES += telephony-common org.apache.http.legacy
LOCAL_STATIC_JAVA_LIBRARIES += android-common jsr305
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4
#LOCAL_STATIC_JAVA_LIBRARIES += libchips
LOCAL_STATIC_JAVA_LIBRARIES += xy_sdk_libs

#LOCAL_STATIC_JAVA_LIBRARIES := \
#    android-common \
#    jsr305 \
#    android-support-v4 \
#    xy_sdk_libs

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.android.ex.chips
#LOCAL_AAPT_FLAGS += --extra-packages cn.com.xy.sms.sdk.ui
LOCAL_AAPT_FLAGS += --extra-packages $(xy_package)
LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk

LOCAL_JAVA_LIBRARIES += mst-framework

LOCAL_REQUIRED_MODULES := SoundRecorder

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_PRIVILEGED_MODULE := true

LOCAL_OVERRIDES_PACKAGES := messaging

include $(BUILD_PACKAGE)

##################################################
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    xy_sdk_libs:$(xiaoyuan_dir)/libs/mplus_sdk_2.43.16101901.jar
include $(BUILD_MULTI_PREBUILT)
##################################################

# This finds and builds the test apk as well, so a single make does both.
# include $(call all-makefiles-under,$(LOCAL_PATH))
