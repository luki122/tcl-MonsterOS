/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.android.mms.R;

public class MstEmptyActivity extends Activity{
    private Intent mStoreIntent;
    public static final String EXTRA_KEY_NEW_MESSAGE_NEED_RELOAD = "reload";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStoreIntent = getIntent();
        finish();
    }

    @Override
    protected void onDestroy() {
        if(mStoreIntent != null) {
            String action = mStoreIntent.getStringExtra("new_intent_action");
            boolean extra = mStoreIntent.getBooleanExtra(EXTRA_KEY_NEW_MESSAGE_NEED_RELOAD, false);
            Uri data = mStoreIntent.getData();
            Intent intent = new Intent(action, data);
            intent.putExtra(EXTRA_KEY_NEW_MESSAGE_NEED_RELOAD, extra);
            startActivity(intent);
        }
        super.onDestroy();
    }
}
