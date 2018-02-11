LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform
LOCAL_JAVA_LIBRARIES := \
                telephony-common
                
LOCAL_STATIC_JAVA_LIBRARIES := \
    launcher_recyclerview-v7 \
    android-support-v4  \
    android-support-v7-palette

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    $(call all-java-files-under, monster/src) \
    $(call all-proto-files-under, protos)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res \
    $(LOCAL_PATH)/monster/res \
   $(LOCAL_PATH)/libs/recyclerview/res

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_PROTOC_OPTIMIZE_TYPE := nano
LOCAL_PROTOC_FLAGS := --proto_path=$(LOCAL_PATH)/protos/
LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
#   --extra-packages android.support.v7.recyclerview

LOCAL_SDK_VERSION := current
LOCAL_PACKAGE_NAME := Monster_Launcher
LOCAL_PRIVILEGED_MODULE := true
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
		launcher_recyclerview-v7:libs/recyclerview-v7-23.4.0.jar 
include $(BUILD_MULTI_PREBUILT)   
    
include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-proto-files-under, protos)
LOCAL_PROTOC_OPTIMIZE_TYPE := nano
LOCAL_PROTOC_FLAGS := --proto_path=$(LOCAL_PATH)/protos/
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := launcher_proto_lib
LOCAL_IS_HOST_MODULE := true
LOCAL_STATIC_JAVA_LIBRARIES := host-libprotobuf-java-nano
include $(BUILD_HOST_JAVA_LIBRARY)
include $(call all-makefiles-under,$(LOCAL_PATH))
