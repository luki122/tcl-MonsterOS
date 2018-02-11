#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_OVERRIDES_PACKAGES := Calculator
LOCAL_PACKAGE_NAME := Monster_Calculator
LOCAL_CERTIFICATE := platform
LOCAL_SDK_VERSION := current

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk

LOCAL_JAVA_LIBRARIES += mst-framework

LOCAL_STATIC_JAVA_LIBRARIES := apache cr1 android-support-v4

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)
###############################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := apache:libs/org.apache.http.legacy.jar cr1:libs/cr_classes-full-debug.jar

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
