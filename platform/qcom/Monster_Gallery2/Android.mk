LOCAL_PATH:= $(call my-dir)
$(info $(LOCAL_PATH))
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13 \
                               com.android.gallery3d.common2 \
                               xmp_toolkit \
                               mp4parser \
                               mst_customlibs_renderscript \
                               mst_customlibs_xmpcore

LOCAL_RENDERSCRIPT_TARGET_API := 18
LOCAL_RENDERSCRIPT_COMPATIBILITY := 18
#LOCAL_RENDERSCRIPT_FLAGS := -rs-package-name=android.support.v8.renderscript

# Keep track of previously compiled RS files too (from bundled GalleryGoogle).
prev_compiled_rs_files := $(call all-renderscript-files-under, src)

# We already have these files from GalleryGoogle, so don't install them.
LOCAL_RENDERSCRIPT_SKIP_INSTALL := $(prev_compiled_rs_files)

LOCAL_SRC_FILES := $(call all-java-files-under, src) $(prev_compiled_rs_files)
LOCAL_SRC_FILES += $(call all-java-files-under, src_pd)

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += -I out/target/common/obj/APPS/mst-framework-res_intermediates/package-export.apk

#LOCAL_PACKAGE_NAME := Gallery2
LOCAL_PACKAGE_NAME := Monster_Gallery2
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_OVERRIDES_PACKAGES := Gallery Gallery3D GalleryNew3D

LOCAL_SDK_VERSION := current

LOCAL_JNI_SHARED_LIBRARIES := libjni_eglfence libjni_filtershow_filters librsjni libjni_jpegstream

#LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_JAVA_LIBRARIES += org.apache.http.legacy
LOCAL_JAVA_LIBRARIES += mst-framework

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := mst_customlibs_renderscript:libs/renderscript-v8.jar \
                                        mst_customlibs_xmpcore:libs/xmpcore-5.1.2.jar
        
include $(BUILD_MULTI_PREBUILT)

ifeq ($(strip $(LOCAL_PACKAGE_OVERRIDES)),)

# Use the following include to make gallery test apk
include $(call all-makefiles-under, $(LOCAL_PATH))

endif
