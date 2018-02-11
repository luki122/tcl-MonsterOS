

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

contacts_common_dir := ../Monster_ContactsCommon
phone_common_dir := ../PhoneCommon
contacts_ext_dir := ../Monster_ContactsCommon/ext
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
            bcr:libs/bcr_open_api_20160324.jar \
            pinyin4j_2:../Monster_ContactsCommon/libs/pinyin4j-2.5.0.jar
            
include $(BUILD_MULTI_PREBUILT)
include $(CLEAR_VARS)
src_dirs := src $(contacts_common_dir)/src $(phone_common_dir)/src $(contacts_ext_dir)/src
res_dirs := res $(contacts_common_dir)/res $(contacts_common_dir)/res_ext $(phone_common_dir)/res

LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
LOCAL_SRC_FILES += src/com/mst/tms/ITmsService.aidl
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs)) \
    frameworks/support/v7/cardview/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res_ext

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.android.contacts.common \
    --extra-packages com.android.phone.common \
    --extra-packages android.support.v7.cardview


LOCAL_JAVA_LIBRARIES := telephony-common voip-common telephony-ext
LOCAL_JAVA_LIBRARIES += framework
# 引用mst-framework的类
#LOCAL_JAVA_LIBRARIES += mediatek-framework
#LOCAL_JAVA_LIBRARIES += mediatek-common
LOCAL_STATIC_JAVA_LIBRARIES := \
    com.android.vcard \
    android-common \
    guava \
    android-support-v13 \
    android-support-v7-cardview \
    android-support-v7-palette \
    android-support-v4 \
    libphonenumber \
    bcr \
    pinyin4j_2

            
# 引用mst-framework的资源

LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk
 

LOCAL_JAVA_LIBRARIES += mst-framework


LOCAL_PACKAGE_NAME := Monster_Contacts
LOCAL_CERTIFICATE := shared
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

#LOCAL_SDK_VERSION := current

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
#include $(call all-makefiles-under,$(LOCAL_PATH))




