LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under,src) \
                    src/main/aidl/com/monster/cloud/ICallBack.aidl \
                    src/main/aidl/com/monster/cloud/ProgressConnection.aidl \

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 \
							   AcccountSDK \
							   openqq \
							   qqpim_pro \
							   wup-v3 \
							   gson \
							   universal-imageloader \
                                                           shark


LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk
LOCAL_JAVA_LIBRARIES += mst-framework telephony-common


LOCAL_PACKAGE_NAME := Monster_Cloud

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_CERTIFICATE := platform
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := AcccountSDK:libs/AcccountSDK_V1.2.8.38_20161014_release.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += openqq:libs/openqq.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += qqpim_pro:libs/qqpim_pro.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += wup-v3:libs/wup-v3.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += gson:libs/gson-2.2.4.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += universal-imageloader:libs/universal-image-loader-1.9.5.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += shark:libs/shark1.1.1.jar
include $(BUILD_MULTI_PREBUILT)


include $(CLEAR_VARS)
LOCAL_MODULE        := libSync
LOCAL_MODULE_CLASS  := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE_OWNER  := tct
LOCAL_MODULE_TAGS   := optional
LOCAL_SRC_FILES     := libs/armeabi/libSync.so
LOCAL_MODULE_PATH   := $(PRODUCT_OUT)/system/app/Monster_Cloud/lib/arm/
LOCAL_PROPRIETARY_MODULE := true
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE        := libTmsdk-2.1.1
LOCAL_MODULE_CLASS  := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE_OWNER  := tct
LOCAL_MODULE_TAGS   := optional
LOCAL_SRC_FILES     := libs/armeabi/libTmsdk-2.1.1.so
LOCAL_MODULE_PATH   := $(PRODUCT_OUT)/system/app/Monster_Cloud/lib/arm/
LOCAL_PROPRIETARY_MODULE := true
include $(BUILD_PREBUILT)
include $(call all-makefiles-under,$(LOCAL_PATH))
