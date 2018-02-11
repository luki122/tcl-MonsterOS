LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

# Only compile source java files in this apk.
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += \
        src/com/android/providers/contacts/EventLogTags.logtags
LOCAL_SRC_FILES += \
        src/com/mst/tms/ITmsService.aidl

#LOCAL_SRC_FILES += \
#        ../Monster_ContactsCommon/src/com/mst/CallLog.java
#
#LOCAL_SRC_FILES += \
#        ../Monster_ContactsCommon/src/com/mst/ContactsContract.java

LOCAL_JAVA_LIBRARIES := ext telephony-common voip-common

LOCAL_STATIC_JAVA_LIBRARIES += android-common com.android.vcard guava #t9search

# The Emma tool analyzes code coverage when running unit tests on the
# application. This configuration line selects which packages will be analyzed,
# leaving out code which is tested by other means (e.g. static libraries) that
# would dilute the coverage results. These options do not affect regular
# production builds.
LOCAL_EMMA_COVERAGE_FILTER := +com.android.providers.contacts.*

# The Emma tool analyzes code coverage when running unit tests on the
# application. This configuration line selects which packages will be analyzed,
# leaving out code which is tested by other means (e.g. static libraries) that
# would dilute the coverage results. These options do not affect regular
# production builds.
LOCAL_EMMA_COVERAGE_FILTER := +com.android.providers.contacts.*

LOCAL_PACKAGE_NAME := Monster_ContactsProvider
LOCAL_CERTIFICATE := shared
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_ENABLED := full
DISABLE_PROGUARD = false
LOCAL_PROGUARD_FLAG_FILES := proguard.flags


include $(BUILD_PACKAGE)

include $(CLEAR_VARS)  
   
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := t9search:./lib/t9search.jar   
LOCAL_MODULE_TAGS := optional  
include $(BUILD_MULTI_PREBUILT)  

# Use the following include to make our test apk.
#include $(call all-makefiles-under,$(LOCAL_PATH))
