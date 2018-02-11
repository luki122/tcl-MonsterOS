ifeq ("$(APK_ORIGIN_CONTACTS_SUPPORT)", "yes")
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# We only want this apk build for tests.
LOCAL_MODULE_TAGS := tests
LOCAL_CERTIFICATE := shared

LOCAL_JAVA_LIBRARIES := android.test.runner

src_dirs := src \
    ../../Monster_ContactsCommon/TestCommon/src

# Include all test java files.
LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))

LOCAL_PACKAGE_NAME := ContactsTests

LOCAL_INSTRUMENTATION_FOR := Contacts

include $(BUILD_PACKAGE)
endif
