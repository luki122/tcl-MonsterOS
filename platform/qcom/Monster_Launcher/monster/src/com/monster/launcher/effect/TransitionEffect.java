package com.monster.launcher.effect;

import android.view.View;

import com.monster.launcher.PagedView;

/**
 * Created by antino on 16-7-11.
 * this abstract class also a factory
 */
public abstract class TransitionEffect {
    public static final String TRANSITION_EFFECT_NONE = "none";
    public static final String TRANSITION_EFFECT_ZOOM_IN = "zoom-in";
    public static final String TRANSITION_EFFECT_ZOOM_OUT = "zoom-out";
    public static final String TRANSITION_EFFECT_ROTATE_UP = "rotate-up";
    public static final String TRANSITION_EFFECT_ROTATE_DOWN = "rotate-down";
    public static final String TRANSITION_EFFECT_CUBE_IN = "cube-in";
    public static final String TRANSITION_EFFECT_CUBE_OUT = "cube-out";
    public static final String TRANSITION_EFFECT_STACK = "stack";
    public static final String TRANSITION_EFFECT_ACCORDION = "accordion";
    public static final String TRANSITION_EFFECT_FLIP = "flip";
    public static final String TRANSITION_EFFECT_CYLINDER_IN = "cylinder-in";
    public static final String TRANSITION_EFFECT_CYLINDER_OUT = "cylinder-out";
    public static final String TRANSITION_EFFECT_CAROUSEL = "carousel";
    public static final String TRANSITION_EFFECT_OVERVIEW = "overview";
    protected final PagedView mPagedView;
    private final String mName;
    public TransitionEffect(PagedView pagedView,String effectName){
         mPagedView = pagedView;
         mName = effectName;
    }

    public String getmName() {
        return mName;
    }

    public abstract void screenScrolled(View v, int i, float scrollProgress);
}
