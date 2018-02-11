/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.camera.ui;

public interface Rotatable {
    public static class RotateEntity {
        public RotateEntity(Rotatable rotatable,boolean needAnimation){
            this.rotatable=rotatable;
            this.animation=needAnimation;
            rotatableHashCode=rotatable.hashCode();
        }
        public Rotatable rotatable;
        public boolean animation;

        public int rotatableHashCode;

        private boolean mOrientationLocked;

        public void setOrientationLocked(boolean locked) {
            mOrientationLocked = locked;
        }

        public boolean isOrientationLocked() {
            return mOrientationLocked;
        }
    }
    // Set parameter 'animation' to true to have animation when rotation.
    public void setOrientation(int orientation, boolean animation);
}
