package com.monster.launcher.effect;

import com.monster.launcher.PagedView;

/**
 * Created by antino on 16-7-12.
 */
public class TransitionEffectFactory {
    private static TransitionEffectFactory mInstance;
    private TransitionEffectFactory(){

    }
    public static TransitionEffectFactory getInstance(){
        if(mInstance==null){
            mInstance = new TransitionEffectFactory();
        }
        return mInstance;
    }

    public TransitionEffect getEffectByName(PagedView pagedView,String name){
        if(TransitionEffect.TRANSITION_EFFECT_ACCORDION.equals(name)){
            return new Accordion(pagedView);
        }else if(TransitionEffect.TRANSITION_EFFECT_CAROUSEL.equals(name)){
            return new Carousel(pagedView);
        }else if(TransitionEffect.TRANSITION_EFFECT_CUBE_IN.equals(name)){
            return new Cube(pagedView,true);
        }else if(TransitionEffect.TRANSITION_EFFECT_CUBE_OUT.equals(name)){
            return new Cube(pagedView,false);
        }else if(TransitionEffect.TRANSITION_EFFECT_CYLINDER_IN.equals(name)){
            return new Cylinder(pagedView,true);
        }else if(TransitionEffect.TRANSITION_EFFECT_CYLINDER_OUT.equals(name)){
            return new Cylinder(pagedView,false);
        }else if(TransitionEffect.TRANSITION_EFFECT_FLIP.equals(name)){
            return new Flip(pagedView);
        }else if(TransitionEffect.TRANSITION_EFFECT_STACK.equals(name)){
            return new Stack(pagedView);
        }else if(TransitionEffect.TRANSITION_EFFECT_OVERVIEW.equals(name)){
            return new Overview(pagedView);
        }else if(TransitionEffect.TRANSITION_EFFECT_ZOOM_IN.equals(name)){
            return new Zoom(pagedView,true);
        }else if(TransitionEffect.TRANSITION_EFFECT_ZOOM_OUT.equals(name)){
            return new Zoom(pagedView,false);
        }else if(TransitionEffect.TRANSITION_EFFECT_ROTATE_UP.equals(name)){
            return new Rotate(pagedView,true);
        }else if(TransitionEffect.TRANSITION_EFFECT_ROTATE_DOWN.equals(name)){
            return new Rotate(pagedView,false);
        }else if(TransitionEffect.TRANSITION_EFFECT_NONE.equals(name)){
            return null;
        }else{
            return null;
        }
    }
}
