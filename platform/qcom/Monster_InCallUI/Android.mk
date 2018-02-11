LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

dialer_dir := Dialer
contacts_common_dir := ContactsCommon
phone_common_dir := ../PhoneCommon

ifeq ($(TARGET_BUILD_APPS),)
support_library_root_dir := frameworks/support
else
support_library_root_dir := prebuilts/sdk/current/support
endif

src_dirs := src \
    $(dialer_dir)/src \
    $(contacts_common_dir)/src \
    $(phone_common_dir)/src

# M: Add ContactsCommon ext
# src_dirs += $(contacts_common_dir)/ext

res_dirs := res \
    $(dialer_dir)/res \
    $(contacts_common_dir)/res \
    $(phone_common_dir)/res

src_dirs += \
    src-N \
    $(dialer_dir)/src-N \
    $(contacts_common_dir)/src-N \
    $(phone_common_dir)/src-N

# M: Add ext resources
# res_dirs += $(contacts_common_dir)/res_ext

LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs)) \
    $(support_library_root_dir)/v7/cardview/res \
    $(support_library_root_dir)/v7/recyclerview/res \
    $(support_library_root_dir)/v7/appcompat/res \
    $(support_library_root_dir)/design/res


LOCAL_SRC_FILES += \
        src/com/mediatek/telecom/recording/IPhoneRecorder.aidl\
        src/com/mediatek/telecom/recording/IPhoneRecordStateListener.aidl

LOCAL_SRC_FILES += src/com/mst/tms/ITmsService.aidl

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages android.support.v7.cardview \
    --extra-packages android.support.v7.cardview \
    --extra-packages android.support.v7.recyclerview \
    --extra-packages android.support.design \
    --extra-packages com.android.dialer \
    --extra-packages com.android.contacts.common \
    --extra-packages com.android.phone.common

LOCAL_JAVA_LIBRARIES := telephony-common \
                        telephony-ext \
                        ims-common \
                        org.apache.http.legacy


# 引用mst-framework的资源
LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk
 
# 引用mst-framework的类
LOCAL_JAVA_LIBRARIES += mst-framework

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-common \
    android-support-v13 \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-cardview \
    android-support-v7-recyclerview \
    android-support-design \
    com.android.vcard \
    guava \
    libphonenumber \
    ims-ext-common \
#    phonebook_wrapper \
    telephony-common


LOCAL_PACKAGE_NAME := Monster_InCallUI
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

# Uncomment the following line to build against the current SDK
# This is required for building an unbundled app.
#LOCAL_SDK_VERSION := current

include $(BUILD_PACKAGE)


# Use the following include to make our test apk.
# include $(call all-makefiles-under,$(LOCAL_PATH))
