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
mst-framework-res-source-path := APPS/mst-framework-res_intermediates/src
# the library
# ============================================================

include $(CLEAR_VARS)

LOCAL_DEX_PREOPT := false

LOCAL_INTERMEDIATE_SOURCES := \
			$(mst-framework-res-source-path)/com/mst/internal/R.java \
			$(mst-framework-res-source-path)/com/mst/R.java \
			$(mst-framework-res-source-path)/com/mst/Manifest.java 

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under)

LOCAL_MODULE := mst-framework


include $(BUILD_JAVA_LIBRARY)


#build monster-framework sdk
include $(CLEAR_VARS)
LOCAL_DEX_PREOPT := false

LOCAL_INTERMEDIATE_SOURCES := \
                        $(mst-framework-res-source-path)/com/mst/internal/R.java \
                        $(mst-framework-res-source-path)/com/mst/R.java \
                        $(mst-framework-res-source-path)/com/mst/Manifest.java

LOCAL_MODULE_TAGS := optional
LOCAL_JACK_ENABLED := disabled

LOCAL_SRC_FILES := $(call all-java-files-under)

LOCAL_MODULE := mst-framework-sdk
include $(BUILD_STATIC_JAVA_LIBRARY)
###############

include $(call first-makefiles-under,$(LOCAL_PATH))

$(info $(LOCAL_INSTALLED_MODULE))
$(LOCAL_INSTALLED_MODULE): | $(PRODUCT_OUT)/system/framework/framework-res.apk
$(info logtest)
$(info $(PRODUCT_OUT))
