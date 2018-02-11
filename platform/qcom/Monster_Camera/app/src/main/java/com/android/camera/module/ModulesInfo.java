/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.module;

import android.content.Context;

import com.android.camera.ContactsIntentModule;
import com.android.camera.CylindricalPanoramaModule;
import com.android.camera.FilterModule;
import com.android.camera.FyuseModule;
import com.android.camera.MT_BurstPhotoModule;
import com.android.camera.MT_PanoramaModule;
import com.android.camera.ManualModule;
import com.android.camera.MicroVideoModule;
import com.android.camera.NormalPhotoModule;
import com.android.camera.NormalVideoModule;
import com.android.camera.OptimizeBurstPhotoModule;
import com.android.camera.SlowMotionModule;
import com.android.camera.TS_PanoramaGPModule;
import com.android.camera.VideoCaptureIntentModule;
import com.android.camera.VideoFilterModule;
import com.android.camera.app.AppController;
import com.android.camera.app.ModuleManager;
import com.android.camera.debug.DebugPropertyHelper;
import com.android.camera.debug.Log;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.GcamHelper;
import com.android.camera.util.PhotoSphereHelper;
import com.android.camera.util.RefocusHelper;
import com.android.classloader.DynamicClasses;
import com.android.external.plantform.ExtBuild;
import com.tct.camera.R;

/**
 * A class holding the module information and registers them to
 * {@link com.android.camera.app.ModuleManager}.
 */
public class ModulesInfo {
    private static final Log.Tag TAG = new Log.Tag("ModulesInfo");

    /** Selects CaptureModule if true, PhotoModule if false. */
    private static final boolean ENABLE_CAPTURE_MODULE =
            DebugPropertyHelper.isCaptureModuleEnabled();

    public static void setupModules(Context context, ModuleManager moduleManager) {
        int photoModuleId = context.getResources().getInteger(R.integer.camera_mode_photo);
        registerPhotoModule(moduleManager, photoModuleId);
        moduleManager.setDefaultModuleIndex(photoModuleId);
        registerVideoModule(moduleManager, context.getResources()
                .getInteger(R.integer.camera_mode_video));
        registerSlowMotionModule(moduleManager, context.getResources()
                .getInteger(R.integer.camera_mode_slowmotion));
        registerManualModule(moduleManager, context.getResources()
                .getInteger(R.integer.camera_mode_manual));
        registerFilterModule(moduleManager, context.getResources()
                .getInteger(R.integer.camera_mode_filter));
        registerVideoFilterModule(moduleManager, context.getResources()
                .getInteger(R.integer.camera_mode_videofilter));

        registerPanoModule(moduleManager, context.getResources()
                    .getInteger(R.integer.camera_mode_pano));
        register360PhotoModule(moduleManager, context.getResources()
                    .getInteger(R.integer.camera_mode_360_photo));

        registerParallaxModule(moduleManager, context.getResources()
                .getInteger(R.integer.camera_mode_parallax));
        registerMicroVideoModule(moduleManager, context.getResources()
                .getInteger(R.integer.camera_mode_micro_video));
        if (PhotoSphereHelper.hasLightCycleCapture(context)) {
            registerWideAngleModule(moduleManager, context.getResources()
                    .getInteger(R.integer.camera_mode_panorama));
            registerPhotoSphereModule(moduleManager, context.getResources()
                    .getInteger(R.integer.camera_mode_photosphere));
        }
        if (RefocusHelper.hasRefocusCapture(context)) {
            registerRefocusModule(moduleManager, context.getResources()
                    .getInteger(R.integer.camera_mode_refocus));
        }
        if (GcamHelper.hasGcamAsSeparateModule()) {
            registerGcamModule(moduleManager, context.getResources()
                    .getInteger(R.integer.camera_mode_gcam));
        }
    }

    public static void setupPhotoCaptureIntentModules(Context context, ModuleManager moduleManager){
        int photoModuleId = context.getResources().getInteger(R.integer.camera_mode_photo);
        registerPhotoModule(moduleManager, photoModuleId);
        moduleManager.setDefaultModuleIndex(photoModuleId);
    }

    public static void setupPhotoContactsIntentModules(Context context, ModuleManager moduleManager){
        int photoModuleId = context.getResources().getInteger(R.integer.camera_mode_contacts_intent);
        registerContactsModule(moduleManager, photoModuleId);
        moduleManager.setDefaultModuleIndex(photoModuleId);
    }

    private static void registerContactsModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                return new ContactsIntentModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return false;
            }
        });
    }

    public static void setupVideoCaptureIntentModules(Context context, ModuleManager moduleManager){
        int videoModuleId=context.getResources()
                .getInteger(R.integer.camera_mode_video_capture);
        registVideoCaptureModule(moduleManager, videoModuleId);
        moduleManager.setDefaultModuleIndex(videoModuleId);
    }

    private static void registerPhotoModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                // The PhotoModule requests the old app camere, while the new
                // capture module is using OneCamera. At some point we'll
                // refactor all modules to use OneCamera, then the new module
                // doesn't have to manage it itself.
                // return !ENABLE_CAPTURE_MODULE;
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
                if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
                    return new MT_BurstPhotoModule(app);
                }
                boolean needOptimizeBurst=CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_ENABLE_OPTIMEIZE_SNAPSHOT,false);
//                boolean needOptimizeBurst = true;
                return needOptimizeBurst ? new OptimizeBurstPhotoModule(app) : new NormalPhotoModule(app);
                /* MODIFIED-END by sichao.hu,BUG-2821981*/
            }

            @Override
            public boolean needAddToStrip() {
                return true;
            }
        });
    }


    private static void registerVideoModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                return new NormalVideoModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return true;
            }
        });
    }
    private static void registerFilterModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                return new FilterModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return false; // MODIFIED by yuanxing.tan, 2016-09-08,BUG-2861353
            }
        });
    }

    private static void registerVideoFilterModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                return new VideoFilterModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return false; // MODIFIED by yuanxing.tan, 2016-09-08,BUG-2861353
            }
        });
    }

    private static void registVideoCaptureModule(ModuleManager moduleManager,final int moduleId){
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                return new VideoCaptureIntentModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return true;
            }
        });
    }

    private static void registerSlowMotionModule(ModuleManager moduleManager, final int moduleId){
        if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_SLOW_MOTION_MODULE,false)) {
            return;
        }
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                Log.w(TAG,"create Slow Motion Module");
                return new SlowMotionModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return true;
            }
        });
    }

    private static void registerManualModule(ModuleManager moduleManager, final int moduleId) {
        if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_MANUAL_MODULE,false)) {
            return;
        }
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                return new ManualModule(app);
            }
            @Override
            public boolean needAddToStrip() {
                return true;
            }
        });
    }

    private static void registerPanoModule(ModuleManager moduleManager, final int moduleId) {
        final String panoVendor = CustomUtil.getInstance().getString(CustomFields.DEF_CAMERA_SUPPORT_PANO_VENDOR,
                SettingsUtil.PANO_VENDOR_DEFAULT);
        if (panoVendor.equals(SettingsUtil.PANO_VENDOR_NONE)){
            return;
        }
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                Log.w(TAG,"create Pano Module");
                if (panoVendor.equals(SettingsUtil.PANO_VENDOR_MTK)) {
                    return new MT_PanoramaModule(app);
                }
                return new TS_PanoramaGPModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return true;
            }
        });
    }

    private static void register360PhotoModule(ModuleManager moduleManager, final int moduleId) {
        if (!CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_SUPPORT_CYLINDRICAL_PANORAMA_MODULE, false)) {
            return;
        }
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                Log.d(TAG, "Create 360 Photo module.");
                return new CylindricalPanoramaModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return true;
            }
        });
    }

    private static void registerParallaxModule(ModuleManager moduleManager, final int moduleId){
        if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_PARALLAX_MODULE,false)) {
            return;
        }
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                Log.w(TAG,"create Parallax Module");
                return new FyuseModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return true;
            }
        });
    }

    private static void registerMicroVideoModule(ModuleManager moduleManager, final int moduleId){
        if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_MICRO_VIDEO_MODULE,false)) {
            return;
        }
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                Log.w(TAG,"create Slow MicroVideo Module");
                // MicroVideo is not available yet, use SlowMotionModule here
                return new MicroVideoModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return true;
            }
        });
    }

    private static void registerWideAngleModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                return PhotoSphereHelper.createWideAnglePanoramaModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return false;
            }
        });
    }

    private static void registerPhotoSphereModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                return PhotoSphereHelper.createPanoramaModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return false;
            }
        });
    }

    private static void registerRefocusModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app) {
                return RefocusHelper.createRefocusModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return false;
            }
        });
    }

    private static void registerGcamModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return false;
            }

            @Override
            public ModuleController createModule(AppController app) {
                return GcamHelper.createGcamModule(app);
            }

            @Override
            public boolean needAddToStrip() {
                return false;
            }
        });
    }
}
