package com.monster.paymentsecurity;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

/**
 * Created by logic on 16-12-21.
 */
public interface FragmentChangeHandler {
    @IntDef(value = {ACTION_ADD, ACTION_REMOVE, ACTION_HIDE, ACTION_REPLACE})
    @interface action{}
    int ACTION_ADD = 1;
    int ACTION_REPLACE = 2;
    int ACTION_REMOVE = 3;
    int ACTION_HIDE = 4;

    void notifyFragmentChange(@action int action, @NonNull String fragment, Bundle args);
}
