package cn.com.xy.sms.sdk.ui.anim;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

public class CardAnimUtil {
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressLint("NewApi")
    public static void applyItemRotation(final View rootView, final View view1, final View view2, long duration1,
            long duration2, AnimatorListener listener) {

        ObjectAnimator animator = ObjectAnimator.ofFloat(view1, "scaleY", 1.0f, 0.0f).setDuration(duration1);
        animator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                view1.setVisibility(View.GONE);
                view2.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view2, "scaleY", 0.0f, 1.0f).setDuration(duration2);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view1, "scaleY", 0.0f, 1.0f).setDuration(1);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(animator);
        animSet.play(animator1).after(animator);
        animSet.play(animator2).after(animator1);
        if (listener != null) {
            animSet.addListener(listener);
        }
        animSet.start();
    }

}
