LOCAL_PATH:= $(call my-dir)

# Build the Telecom service.
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := telephony-common telephony-ext ims-common
LOCAL_STATIC_JAVA_LIBRARIES := ims-ext-common

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += \
        src/org/codeaurora/btmultisim/IBluetoothDsdaService.aidl

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_STATIC_JAVA_LIBRARIES := ims-ext-common

LOCAL_PACKAGE_NAME := Monster_Telecomm

LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

# 引用mst-framework的资源
LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk
 
# 引用mst-framework的类
LOCAL_JAVA_LIBRARIES += mst-framework

include frameworks/base/packages/SettingsLib/common.mk
#[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/11/2016, SOLUTION-2455894
include $(BUILD_PLF)
#[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
include $(BUILD_PACKAGE)

# Build the test package.
include $(call all-makefiles-under,$(LOCAL_PATH))
