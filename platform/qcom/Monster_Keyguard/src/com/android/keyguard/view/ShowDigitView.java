package com.android.keyguard.view;

import com.android.keyguard.R;
import com.android.keyguard.utils.AnimUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ShowDigitView extends FrameLayout {

    private final static int STATE_EMPTY = 1;
    private final static int STATE_FULL = 2;
    private final static int STATE_ERROR = 3;
    private final static int STATE_UNKONW = -1;

    private ImageView dot_img_1_empty = null;
    private ImageView dot_img_2_empty = null;
    private ImageView dot_img_3_empty = null;
    private ImageView dot_img_4_empty = null;
    private ImageView dot_img_1_full = null;
    private ImageView dot_img_2_full = null;
    private ImageView dot_img_3_full = null;
    private ImageView dot_img_4_full = null;

    public ShowDigitView(Context context) {
        this(context, null);
    }

    public ShowDigitView(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public ShowDigitView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        dot_img_1_empty = ( ImageView ) findViewById(R.id.digit_dot_1_empty);
        dot_img_2_empty = ( ImageView ) findViewById(R.id.digit_dot_2_empty);
        dot_img_3_empty = ( ImageView ) findViewById(R.id.digit_dot_3_empty);
        dot_img_4_empty = ( ImageView ) findViewById(R.id.digit_dot_4_empty);
        dot_img_1_full = ( ImageView ) findViewById(R.id.digit_dot_1_full);
        dot_img_2_full = ( ImageView ) findViewById(R.id.digit_dot_2_full);
        dot_img_3_full = ( ImageView ) findViewById(R.id.digit_dot_3_full);
        dot_img_4_full = ( ImageView ) findViewById(R.id.digit_dot_4_full);
//        setBackgroundResource(R.drawable.digit_password_input_bg);
    }

    public void onTextChange(int count) {
        switch (count) {
            case 0:
                setImageSate(dot_img_1_full, STATE_EMPTY);
                setImageSate(dot_img_2_full, STATE_EMPTY);
                setImageSate(dot_img_3_full, STATE_EMPTY);
                setImageSate(dot_img_4_full, STATE_EMPTY);
                break;
            case 1:
                setImageSate(dot_img_1_full, STATE_FULL);
                setImageSate(dot_img_2_full, STATE_EMPTY);
                setImageSate(dot_img_3_full, STATE_EMPTY);
                setImageSate(dot_img_4_full, STATE_EMPTY);
                break;
            case 2:
                setImageSate(dot_img_1_full, STATE_FULL);
                setImageSate(dot_img_2_full, STATE_FULL);
                setImageSate(dot_img_3_full, STATE_EMPTY);
                setImageSate(dot_img_4_full, STATE_EMPTY);
                break;
            case 3:
                setImageSate(dot_img_1_full, STATE_FULL);
                setImageSate(dot_img_2_full, STATE_FULL);
                setImageSate(dot_img_3_full, STATE_FULL);
                setImageSate(dot_img_4_full, STATE_EMPTY);
                break;
            case 4:
                setImageSate(dot_img_1_full, STATE_FULL);
                setImageSate(dot_img_2_full, STATE_FULL);
                setImageSate(dot_img_3_full, STATE_FULL);
                setImageSate(dot_img_4_full, STATE_FULL);
                break;
            case 5:
                playErrorAnim();
                break;

            default:
                setImageSate(dot_img_1_full, STATE_EMPTY);
                setImageSate(dot_img_2_full, STATE_EMPTY);
                setImageSate(dot_img_3_full, STATE_EMPTY);
                setImageSate(dot_img_4_full, STATE_EMPTY);
                break;
        }
    }

    public void setImageSate(ImageView img, int state) {
        /*switch (state) {
            case STATE_EMPTY:
                img.setImageResource(R.drawable.digit_password_dot_empty);
                break;

            case STATE_FULL:
                img.setImageResource(R.drawable.digit_password_dot_full);
                break;

            case STATE_ERROR:
                img.setImageResource(R.drawable.digit_password_dot_error);
                break;

            default:
                img.setImageResource(R.drawable.digit_password_dot_empty);
                break;
        }*/
        switch (state) {
            case STATE_EMPTY:
                img.setVisibility(View.INVISIBLE);
                break;

            case STATE_FULL:
                img.setAlpha(1.0f);
                img.setVisibility(View.VISIBLE);
                break;

            default:
                img.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public void startInitAnim() {
        dot_img_1_empty.setAlpha(0.0f);
        dot_img_2_empty.setAlpha(0.0f);
        dot_img_3_empty.setAlpha(0.0f);
        dot_img_4_empty.setAlpha(0.0f);
        PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
        PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1.0f);
        PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1.0f);
        ObjectAnimator animator1 = AnimUtils
                .ofPropertyValuesHolder(dot_img_1_empty, pvhAlpha, pvhScaleX, pvhScaleY).setDuration(500);
        ObjectAnimator animator2 = AnimUtils
                .ofPropertyValuesHolder(dot_img_2_empty, pvhAlpha, pvhScaleX, pvhScaleY).setDuration(500);
        ObjectAnimator animator3 = AnimUtils
                .ofPropertyValuesHolder(dot_img_3_empty, pvhAlpha, pvhScaleX, pvhScaleY).setDuration(500);
        ObjectAnimator animator4 = AnimUtils
                .ofPropertyValuesHolder(dot_img_4_empty, pvhAlpha, pvhScaleX, pvhScaleY).setDuration(500);
        animator1.setInterpolator(new OvershootInterpolator());
        animator2.setInterpolator(new OvershootInterpolator());
        animator3.setInterpolator(new OvershootInterpolator());
        animator4.setInterpolator(new OvershootInterpolator());
        AnimatorSet dotImg = new AnimatorSet();
        dotImg.play(animator1);
        dotImg.play(animator2).after(30);
        dotImg.play(animator3).after(60);
        dotImg.play(animator4).after(90);
        dotImg.start();
    }

    private void playErrorAnim(){
        PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
        PropertyValuesHolder pvhTranslationY = PropertyValuesHolder.ofFloat("translationY", 60.0f);
        ObjectAnimator animator1 = AnimUtils
                .ofPropertyValuesHolder(dot_img_1_full, pvhAlpha, pvhTranslationY).setDuration(400);
        ObjectAnimator animator2 = AnimUtils
                .ofPropertyValuesHolder(dot_img_2_full, pvhAlpha, pvhTranslationY).setDuration(400);
        ObjectAnimator animator3 = AnimUtils
                .ofPropertyValuesHolder(dot_img_3_full, pvhAlpha, pvhTranslationY).setDuration(400);
        ObjectAnimator animator4 = AnimUtils
                .ofPropertyValuesHolder(dot_img_4_full, pvhAlpha, pvhTranslationY).setDuration(400);
        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                View view = (View)((ObjectAnimator)animation).getTarget();
                view.setTranslationY(0.0f);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                View view = (View)((ObjectAnimator)animation).getTarget();
                view.setTranslationY(0.0f);
            }
        });
        animator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                View view = (View)((ObjectAnimator)animation).getTarget();
                view.setTranslationY(0.0f);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                View view = (View)((ObjectAnimator)animation).getTarget();
                view.setTranslationY(0.0f);
            }
        });
        animator3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                View view = (View)((ObjectAnimator)animation).getTarget();
                view.setTranslationY(0.0f);
            }
        });
        animator4.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                View view = (View)((ObjectAnimator)animation).getTarget();
                view.setTranslationY(0.0f);
            }
        });
        int []num = getRandomArray();
        AnimatorSet dotImg = new AnimatorSet();
        dotImg.play(animator1).after(num[0]);
        dotImg.play(animator2).after(num[1]);
        dotImg.play(animator3).after(num[2]);
        dotImg.play(animator4).after(num[3]);
        dotImg.start();
    }

    private int[] getRandomArray(){
        int[] arr = new int[4];
        for (int i = 0; i < 4; i++) {
            arr[i] = ( int ) (Math.random() * 150) + 51;
            for (int j = 0; j < i; j++) {
                if (arr[j] == arr[i]) {
                    i--;
                    break;
                }
            }
        }
        return arr;
    }

    /// TCL Monster: kth add for keyguard inversion 20161022 start@{
    public void setDotColor(int RGB) {
        dot_img_1_empty.setColorFilter(RGB);
        dot_img_2_empty.setColorFilter(RGB);
        dot_img_3_empty.setColorFilter(RGB);
        dot_img_4_empty.setColorFilter(RGB);
        dot_img_1_full.setColorFilter(RGB);
        dot_img_2_full.setColorFilter(RGB);
        dot_img_3_full.setColorFilter(RGB);
        dot_img_4_full.setColorFilter(RGB);
    }
    /// end @}
}
