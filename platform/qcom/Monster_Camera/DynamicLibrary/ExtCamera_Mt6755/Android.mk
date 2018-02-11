LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := extcamera_mt6755

#LOCAL_MODULE_CLASS := JAVA_LIBRARIES

include $(BUILD_JAVA_LIBRARY)
