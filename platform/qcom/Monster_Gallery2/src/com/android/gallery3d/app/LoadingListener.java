/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.gallery3d.app;

public interface LoadingListener {
    public void onLoadingStarted();
    /**
     * Called when loading is complete or no further progress can be made.
     *
     * @param loadingFailed true if data source cannot provide requested data
     */
    public void onLoadingFinished(boolean loadingFailed);
    
    // TCL BaiYuan Begin on 2016.11.14
    // Original:
    /*
    // TCL ShenQianfeng Begin on 2016.08.10
    public void onNotifyEmpty();
    // TCL ShenQianfeng End on 2016.08.10
    // Modify To:
    */
    public void onNotifyEmpty(boolean isEmpty);
    // TCL BaiYuan Begin on 2016.11.14
}
