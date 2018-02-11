/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.monster.launcher.model;

import android.content.Context;

import com.monster.launcher.AppInfo;
import com.monster.launcher.ItemInfo;
import com.monster.launcher.util.Thunk;

import java.text.Collator;
import java.util.Comparator;

/**
 * Class to manage access to an app name comparator.
 * <p>
 * Used to sort application name in all apps view and widget tray view.
 */
public class AppNameComparator {
    private final Collator mCollator;
    private final AbstractUserComparator<ItemInfo> mAppInfoComparator;
    private final Comparator<String> mSectionNameComparator;

    public AppNameComparator(Context context) {
        mCollator = Collator.getInstance();
        mAppInfoComparator = new AbstractUserComparator<ItemInfo>(context) {

            @Override
            public final int compare(ItemInfo a, ItemInfo b) {
                // Order by the title in the current locale
                int result = compareTitles(a.title.toString(), b.title.toString());
                if (result == 0 && a instanceof AppInfo && b instanceof AppInfo) {
                    AppInfo aAppInfo = (AppInfo) a;
                    AppInfo bAppInfo = (AppInfo) b;
                    // If two apps have the same title, then order by the component name
                    result = aAppInfo.componentName.compareTo(bAppInfo.componentName);
                    if (result == 0) {
                        // If the two apps are the same component, then prioritize by the order that
                        // the app user was created (prioritizing the main user's apps)
                        return super.compare(a, b);
                    }
                }
                return result;
            }
        };
        mSectionNameComparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return compareTitles(o1, o2);
            }
        };
    }

    /**
     * Returns a locale-aware comparator that will alphabetically order a list of applications.
     */
    public Comparator<ItemInfo> getAppInfoComparator() {
        return mAppInfoComparator;
    }

    /**
     * Returns a locale-aware comparator that will alphabetically order a list of section names.
     */
    public Comparator<String> getSectionNameComparator() {
        return mSectionNameComparator;
    }

    //add by xiangzx
    private boolean isChinese(int unicode){
        if(unicode >'\u4e00' && unicode< '\u9fa5'){
            return true;
        }
        return false;
    }

    /**
     * Compares two titles with the same return value semantics as Comparator.
     */
    @Thunk
    int compareTitles(String titleA, String titleB) {
        // Ensure that we de-prioritize any titles that don't start with a linguistic letter or digit
        boolean aStartsWithLetter = (titleA.length() > 0) &&
                Character.isLetter(titleA.codePointAt(0)); //&& !isChinese(titleA.codePointAt(0));
        boolean bStartsWithLetter = (titleB.length() > 0) &&
                Character.isLetter(titleB.codePointAt(0)); //&& !isChinese(titleB.codePointAt(0));
        if (aStartsWithLetter && !bStartsWithLetter) {
            return -1;
            //return 1; //modify by xiangzx
        } else if (!aStartsWithLetter && bStartsWithLetter) {
            return 1;
            //return -1; //modify by xiangzx
        }

        // Order by the title in the current locale
        return mCollator.compare(titleA, titleB);
    }
}
