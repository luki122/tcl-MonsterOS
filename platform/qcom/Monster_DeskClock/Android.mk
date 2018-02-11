LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

#LOCAL_RESOURCE_DIR := packages/apps/DeskClock/res
LOCAL_RESOURCE_DIR := ${LOCAL_PATH}/res
LOCAL_RESOURCE_DIR += ${LOCAL_PATH}/datetimepicker/res

#ifeq ($(TARGET_BUILD_APPS),)
LOCAL_RESOURCE_DIR += ${LOCAL_PATH}/appcompat/res
LOCAL_RESOURCE_DIR += ${LOCAL_PATH}/gridlayout/res
#else
#LOCAL_RESOURCE_DIR += prebuilts/sdk/current/support/v7/appcompat/res
#LOCAL_RESOURCE_DIR += prebuilts/sdk/current/support/v7/gridlayout/res
#endif

LOCAL_MODULE_TAGS := optional

LOCAL_PACKAGE_NAME := Monster_DeskClock

LOCAL_CERTIFICATE := platform

LOCAL_OVERRIDES_PACKAGES := AlarmClock

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := android-opt-datetimepicker
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v13
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-appcompat
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-gridlayout
#LOCAL_STATIC_JAVA_LIBRARIES += apache
LOCAL_STATIC_JAVA_LIBRARIES += gson
LOCAL_STATIC_JAVA_LIBRARIES += pinying4j

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.appcompat
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.gridlayout
LOCAL_AAPT_FLAGS += --extra-packages com.android.datetimepicker

LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk

LOCAL_JAVA_LIBRARIES += mst-framework

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

#########################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := gson:libs/gson-2.2.4.jar   pinying4j:libs/pinyin4j-2.5.0.jar


include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))

