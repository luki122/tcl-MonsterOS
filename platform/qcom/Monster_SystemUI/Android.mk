LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := Monster_SystemUI-proto-tags

LOCAL_SRC_FILES := $(call all-proto-files-under,src) \
    src/com/android/systemui/EventLogTags.logtags
#Mst: zhicang.liu add for wandoujia begin
LOCAL_STATIC_JAVA_LIBRARIES := gson-2.4 \
				rxjava-1.1.0

LOCAL_STATIC_JAVA_AAR_LIBRARIES:= rxandroid-1.1.0 \
				nisdk-core \
				nisdk-updator

#Mst: zhicang.liu add for wandoujia end
LOCAL_PROTOC_OPTIMIZE_TYPE := nano
LOCAL_PROTO_JAVA_OUTPUT_PARAMS := optional_field_style=accessors


include $(BUILD_STATIC_JAVA_LIBRARY)

# ------------------

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-Iaidl-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := \
    Monster_Keyguard \
    android-support-v7-recyclerview \
    android-support-v7-preference \
    android-support-v7-appcompat \
    android-support-v14-preference \
    android-support-v17-leanback \
    framework-protos \
    Monster_SystemUI-proto-tags

LOCAL_JAVA_LIBRARIES := telephony-common
#Mst: zhicang.liu add for mst-framework begin
LOCAL_JAVA_LIBRARIES += mst-framework
#Mst: zhicang.liu add for mst-framework end

LOCAL_PACKAGE_NAME := Monster_SystemUI
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_RESOURCE_DIR := \
    ${LOCAL_PATH}/../Monster_Keyguard/res \
    $(LOCAL_PATH)/res \
    frameworks/support/v7/preference/res \
    frameworks/support/v14/preference/res \
    frameworks/support/v7/appcompat/res \
    frameworks/support/v7/recyclerview/res \
    frameworks/support/v17/leanback/res

LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages com.android.keyguard:android.support.v7.recyclerview:android.support.v7.preference:android.support.v14.preference:android.support.v7.appcompat \
    --extra-packages android.support.v17.leanback
#Mst: zhicang.liu add for wandoujia begin
LOCAL_AAPT_FLAGS += --extra-packages com.wandoujia.notification
#Mst: zhicang.liu add for wandoujia end

#Mst: zhicang.liu add for mst-framework begin
LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk
#Mst: zhicang.liu add for mst-framework end

ifneq ($(SYSTEM_UI_INCREMENTAL_BUILDS),)
    LOCAL_PROGUARD_ENABLED := disabled
    LOCAL_JACK_ENABLED := incremental
endif

include frameworks/base/packages/SettingsLib/common.mk

#begin add by zhicang.liu
LOCAL_MULTILIB := 64
LOCAL_PREBUILT_JNI_LIBS := jni/arm64-v8a/libnicipher.so
#end add by zhicang.liu
include $(BUILD_PACKAGE)

#Mst: tangjun mod for don't do tests Android.mk begin
#ifeq ($(EXCLUDE_SYSTEMUI_TESTS),)
#    include $(call all-makefiles-under,$(LOCAL_PATH))
#endif
#Mst: tangjun mod for don't do tests Android.mk end
#Mst: zhicang.liu add for wandoujia begin
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := gson-2.4:libs/gson-2.4.jar \
					rxjava-1.1.0:libs/rxjava-1.1.0.jar \
					rxandroid-1.1.0:libs/rxandroid-1.1.0.aar \
					nisdk-core:libs/nisdk-core.aar \
					nisdk-updator:libs/nisdk-updator.aar

include $(BUILD_MULTI_PREBUILT)
#Mst: zhicang.liu add for wandoujia end
