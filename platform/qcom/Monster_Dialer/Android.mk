LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := pinyin4j:../Monster_ContactsCommon/libs/pinyin4j-2.5.0.jar
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

contacts_common_dir := ../Monster_ContactsCommon
phone_common_dir := ../PhoneCommon

src_dirs := src \
    $(contacts_common_dir)/src \
    $(phone_common_dir)/src

# M: Add ContactsCommon ext
src_dirs += $(contacts_common_dir)/ext

res_dirs := res \
    $(contacts_common_dir)/res \
    $(phone_common_dir)/res

# M: Add ext resources
res_dirs += $(contacts_common_dir)/res_ext

LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
LOCAL_SRC_FILES += src/com/mst/tms/ITmsService.aidl
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs)) \
    frameworks/support/v7/cardview/res frameworks/support/v7/recyclerview/res

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages android.support.v7.cardview \
    --extra-packages android.support.v7.recyclerview \
    --extra-packages com.android.contacts.common \
    --extra-packages com.android.phone.common




LOCAL_JAVA_LIBRARIES := telephony-common \
                        ims-common \
                        telephony-ext
                        
# 引用mst-framework的类
LOCAL_JAVA_LIBRARIES += mst-framework

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-common \
    android-support-v13 \
    android-support-v4 \
    android-support-v7-cardview \
    android-support-v7-recyclerview \
    com.android.vcard \
    guava \
    libphonenumber \
    pinyin4j

#    com.android.services.telephony.common \
# 引用mst-framework的资源
LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk
 
LOCAL_PACKAGE_NAME := Monster_Dialer
LOCAL_CERTIFICATE := shared
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

# Uncomment the following line to build against the current SDK
# This is required for building an unbundled app.
#LOCAL_SDK_VERSION := current

include $(BUILD_PACKAGE)

# Use the following include to make our test apk.
#include $(call all-makefiles-under,$(LOCAL_PATH))
