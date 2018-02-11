# Copyright 2012, The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
# 引用mst-framework的类
LOCAL_JAVA_LIBRARIES += mst-framework
# 引用mst-framework的资源
LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_RESOURCE_FILES := $(addprefix $(LOCAL_PATH)/, res)

LOCAL_PACKAGE_NAME := PhoneCommon

include $(BUILD_PACKAGE)
include $(CLEAR_VARS)

