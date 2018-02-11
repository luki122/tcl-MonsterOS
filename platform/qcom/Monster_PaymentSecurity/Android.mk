LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under,src)

LOCAL_PACKAGE_NAME := Monster_PaymentSecurity

#LOCAL_STATIC_JAVA_LIBRARIES += \
    android-support-v4 \
    android-support-v7-recyclerview \
    android-support-v7-preference \
    android-support-v7-appcompat \
    android-support-v14-preference \
    android-support-v17-preference-leanback \
    android-support-v17-leanback \

#申明引用的static jar
LOCAL_STATIC_JAVA_LIBRARIES += tms

#申明引用的非static jar
LOCAL_JAVA_LIBRARIES += mst-framework \
    telephony-common \
    android-support-v4

#LOCAL_RESOURCE_DIR := \
    frameworks/support/v17/leanback/res \
    frameworks/support/v7/preference/res \
    frameworks/support/v14/preference/res \
    frameworks/support/v17/preference-leanback/res \
    frameworks/support/v7/appcompat/res \
    frameworks/support/v7/recyclerview/res \
    $(LOCAL_PATH)/res
LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/res \
    frameworks/support/v7/appcompat/res \

#LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages android.support.v17.leanback:android.support.v7.preference:android.support.v14.preference:android.support.v17.preference:android.support.v7.appcompat:android.support.v7.recyclerview
#LOCAL_AAPT_FLAGS +=  --extra-packages it.gmariotti.cardslib.library.recyclerview:it.gmariotti.cardslib.library:it.gmariotti.cardslib.library.cards
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk


LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_MULTILIB := 64

include $(BUILD_PACKAGE)

#static jar prebuilt
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := tms:libs/tms.jar \
include $(BUILD_MULTI_PREBUILT)

#so prebuilt
include $(CLEAR_VARS)
LOCAL_MODULE := libams-1.1.9-m-mfr
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE_OWNER := tct
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := libs/arm64-v8a/libams-1.1.9-m-mfr.so
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/system/app/Monster_PaymentSecurity/lib/arm64
LOCAL_PROPRIETARY_MODULE := true
include $(BUILD_PREBUILT)

#include $(CLEAR_VARS)
#LOCAL_MODULE := libams-1.1.9-mfr
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_MODULE_SUFFIX := .so
#LOCAL_MODULE_OWNER := tct
#LOCAL_MODULE_TAGS := optional
#LOCAL_SRC_FILES := libs/arm64-v8a/libams-1.1.9-mfr.so
#LOCAL_MODULE_PATH := $(PRODUCT_OUT)/system/app/Monster_PaymentSecurity/lib/arm64
#LOCAL_PROPRIETARY_MODULE := true
#include $(BUILD_PREBUILT)


include $(CLEAR_VARS)
LOCAL_MODULE := libbuffalo-1.0.0-mfr
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE_OWNER := tct
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := libs/arm64-v8a/libbuffalo-1.0.0-mfr.so
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/system/app/Monster_PaymentSecurity/lib/arm64
LOCAL_PROPRIETARY_MODULE := true
include $(BUILD_PREBUILT)


#include $(CLEAR_VARS)
#LOCAL_MODULE := libbumblebee-1.0.4-mfr
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_MODULE_SUFFIX := .so
#LOCAL_MODULE_OWNER := tct
#LOCAL_MODULE_TAGS := optional
#LOCAL_SRC_FILES := libs/arm64-v8a/libbumblebee-1.0.4-mfr.so
#LOCAL_MODULE_PATH := $(PRODUCT_OUT)/system/app/Monster_PaymentSecurity/lib/arm64
#LOCAL_PROPRIETARY_MODULE := true
#include $(BUILD_PREBUILT)


include $(CLEAR_VARS)
LOCAL_MODULE := libdce-1.1.14-mfr
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE_OWNER := tct
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := libs/arm64-v8a/libdce-1.1.14-mfr.so
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/system/app/Monster_PaymentSecurity/lib/arm64
LOCAL_PROPRIETARY_MODULE := true
include $(BUILD_PREBUILT)


#include $(CLEAR_VARS)
#LOCAL_MODULE := liboptimus-1.0.0-mfr
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_MODULE_SUFFIX := .so
#LOCAL_MODULE_OWNER := tct
#LOCAL_MODULE_TAGS := optional
#LOCAL_SRC_FILES := libs/arm64-v8a/liboptimus-1.0.0-mfr.so
#LOCAL_MODULE_PATH := $(PRODUCT_OUT)/system/app/Monster_PaymentSecurity/lib/arm64
#LOCAL_PROPRIETARY_MODULE := true
#include $(BUILD_PREBUILT)


#include $(CLEAR_VARS)
#LOCAL_MODULE := libQQImageCompare-1.2-mfr
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_MODULE_SUFFIX := .so
#LOCAL_MODULE_OWNER := tct
#LOCAL_MODULE_TAGS := optional
#LOCAL_SRC_FILES := libs/arm64-v8a/libQQImageCompare-1.2-mfr.so
#LOCAL_MODULE_PATH := $(PRODUCT_OUT)/system/app/Monster_PaymentSecurity/lib/arm64
#LOCAL_PROPRIETARY_MODULE := true
#include $(BUILD_PREBUILT)


include $(CLEAR_VARS)
LOCAL_MODULE := libTmsdk-2.0.9-mfr
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE_OWNER := tct
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := libs/arm64-v8a/libTmsdk-2.0.9-mfr.so
LOCAL_MODULE_PATH := $(PRODUCT_OUT)/system/app/Monster_PaymentSecurity/lib/arm64
LOCAL_PROPRIETARY_MODULE := true
include $(BUILD_PREBUILT)

# additionally, build unit tests in a separate .apk
include $(call all-makefiles-under,$(LOCAL_PATH))

