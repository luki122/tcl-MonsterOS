/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

public interface AutoBrightnessController {
    boolean isAutoBrightnessEnabled();
    boolean setAutoBrightnessEnabled(boolean enabled);
    void addSettingsChangedCallback(AutoBrightnessChangeCallback cb);
    void removeSettingsChangedCallback(AutoBrightnessChangeCallback cb);

    /**
     * A callback for change in AutoBrightness settings (the user has enabled/disabled AutoBrightness).
     */
    public interface AutoBrightnessChangeCallback {
        /**
         * Called whenever AutoBrightness settings change.
         *
         * @param AutoBrightness Enabled A value of true indicates that AutoBrightness
         *                        is enabled in settings.
         */
        void onAutoBrightnessChanged(boolean AutoBrightnessEnabled);
    }
}
