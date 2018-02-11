LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# Include res dir from chips
chips_dir := ../../../frameworks/opt/chips/res
color_picker_dir := ../../../frameworks/opt/colorpicker/res
datetimepicker_dir := ../../../frameworks/opt/datetimepicker/res
timezonepicker_dir := ../../../frameworks/opt/timezonepicker/res
res_dirs := $(chips_dir) $(color_picker_dir) $(datetimepicker_dir) $(timezonepicker_dir) res
src_dirs := src

LOCAL_EMMA_COVERAGE_FILTER := +com.android.calendar.*

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under,$(src_dirs))

# bundled
#LOCAL_STATIC_JAVA_LIBRARIES += \
#        android-common \
#        libchips \
#        calendar-common

# unbundled
LOCAL_STATIC_JAVA_LIBRARIES := \
        android-common \
        libchips \
        colorpicker \
        android-opt-datetimepicker \
        android-opt-timezonepicker \
        android-support-v4 \
        calendar-common \
        gson

LOCAL_SDK_VERSION := current

LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

LOCAL_PACKAGE_NAME := Monster_Calendar

LOCAL_PROGUARD_FLAG_FILES := proguard.flags \
                             ../../../frameworks/opt/datetimepicker/proguard.flags

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.android.ex.chips
LOCAL_AAPT_FLAGS += --extra-packages com.android.colorpicker
LOCAL_AAPT_FLAGS += --extra-packages com.android.datetimepicker
LOCAL_AAPT_FLAGS += --extra-packages com.android.timezonepicker

LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk

LOCAL_JAVA_LIBRARIES += mst-framework

LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := gson:libs/gson-2.2.4.jar

include $(BUILD_MULTI_PREBUILT)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
