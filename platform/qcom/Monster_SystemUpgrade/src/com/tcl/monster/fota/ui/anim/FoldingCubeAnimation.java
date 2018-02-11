package com.tcl.monster.fota.ui.anim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;

import com.tcl.monster.fota.FotaUIPresenter;

/**
 * Check loading FoldingCube Animation.
 */
public class FoldingCubeAnimation {

    private static final int FOLDING_DURATION = 250;
    private static final int BOX_FALLING_DURATION = 400;
    private static final float BOX_FALLING_LENGTH = 308f;
    private static final int BUTTON_ANIMATION_DURATION = 200;

    private ImageView box;
    private ImageView boxLeftTop;
    private ImageView boxRightTop;
    private ImageView boxLeftBottom;
    private ImageView boxRightBottom;

    private float centerX = 0;
    private float centerY = 0;

    private boolean showGrayBox = false;
    public boolean isPlaying = false;

    private int resultCode = FotaUIPresenter.FOTA_RESULT_TYPE_OK;

    private FoldingCubeAnimationListener mFoldingCubeListener = null;

    public FoldingCubeAnimation(ImageView box, ImageView boxLeftTop, ImageView boxRightTop,
                                ImageView boxLeftBottom, ImageView boxRightBottom) {
        this.box = box;
        this.boxLeftTop = boxLeftTop;
        this.boxRightTop = boxRightTop;
        this.boxLeftBottom = boxLeftBottom;
        this.boxRightBottom = boxRightBottom;

        initAnimation();
    }

    private void initAnimation() {
        box.layout(boxLeftTop.getLeft(), boxLeftTop.getTop(),
                boxLeftTop.getRight(), boxLeftTop.getBottom());
        box.setVisibility(View.VISIBLE);
        boxLeftTop.setVisibility(View.INVISIBLE);
        boxRightTop.setVisibility(View.INVISIBLE);
        boxLeftBottom.setVisibility(View.INVISIBLE);
        boxRightBottom.setVisibility(View.INVISIBLE);

        isPlaying = false;
        showGrayBox = false;
        resultCode = FotaUIPresenter.FOTA_RESULT_TYPE_OK;
    }

    public void start(){
        if (centerX == 0 || centerY == 0) {
            box.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            centerX = box.getWidth() / 2.0f;
                            centerY = box.getHeight() / 2.0f;
                            box.getViewTreeObserver()
                                    .removeGlobalOnLayoutListener(this);
                            play();
                        }
                    });
        } else {
            play();
        }
    }

    public void stop(int result) {
        isPlaying = false;
        resultCode = result;
    }

    private void play() {
        if (isPlaying) {
            return;
        }

        initAnimation();

        final Rotate3DAnimation RotationRightwardAnimation =
                new Rotate3DAnimation(Rotate3DAnimation.ROTATE_Y, 0, 180, centerX * 2, 0, 0);
        RotationRightwardAnimation.setDuration(FOLDING_DURATION);
        RotationRightwardAnimation.setInterpolator(new LinearInterpolator());

        final Rotate3DAnimation RotationDownwardAnimation =
                new Rotate3DAnimation(Rotate3DAnimation.ROTATE_X, 0, -180, 0, centerY * 2, 310f);
        RotationDownwardAnimation.setDuration(FOLDING_DURATION);
        RotationDownwardAnimation.setInterpolator(new LinearInterpolator());

        final Rotate3DAnimation RotationLeftwardAnimation =
                new Rotate3DAnimation(Rotate3DAnimation.ROTATE_Y, 0, -180, 0, 0, 310f);
        RotationLeftwardAnimation.setDuration(FOLDING_DURATION);
        RotationLeftwardAnimation.setInterpolator(new LinearInterpolator());

        final Rotate3DAnimation RotationUpwardAnimation =
                new Rotate3DAnimation(Rotate3DAnimation.ROTATE_X, 0, 180, 0, 0, 310f);
        RotationUpwardAnimation.setDuration(FOLDING_DURATION);
        RotationUpwardAnimation.setInterpolator(new LinearInterpolator());

        RotationRightwardAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                showGrayBox = !showGrayBox;
                if (showGrayBox) {
                    boxLeftTop.setVisibility(View.VISIBLE);
                } else {
                    boxLeftTop.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                box.layout(boxRightTop.getLeft(), 0,
                        boxRightTop.getRight(), boxRightTop.getBottom());
                box.startAnimation(RotationDownwardAnimation);
                if (showGrayBox) {
                    boxRightTop.setVisibility(View.VISIBLE);
                } else {
                    boxRightTop.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        RotationDownwardAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                box.layout(boxRightBottom.getLeft(), boxRightBottom.getTop(),
                        boxRightBottom.getRight(), boxRightBottom.getBottom());
                box.startAnimation(RotationLeftwardAnimation);
                if (showGrayBox) {
                    boxRightBottom.setVisibility(View.VISIBLE);
                } else {
                    boxRightBottom.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        RotationLeftwardAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                box.layout(boxLeftBottom.getLeft(), boxLeftBottom.getTop(),
                        boxLeftBottom.getRight(), boxLeftBottom.getBottom());
                if (!isPlaying && !showGrayBox) {
                    box.clearAnimation();
                    boxLeftBottom.setVisibility(View.INVISIBLE);
                    if (resultCode == FotaUIPresenter.FOTA_CHECK_RESULT_GET_NEW_VERSION) {
                        startBoxFallAnimation();
                    } else {
                        box.setVisibility(View.INVISIBLE);
                        if (mFoldingCubeListener != null) {
                            mFoldingCubeListener.onAnimationEnd();
                        }
                    }
                } else {
                    box.startAnimation(RotationUpwardAnimation);
                    if (showGrayBox) {
                        boxLeftBottom.setVisibility(View.VISIBLE);
                    } else {
                        boxLeftBottom.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        RotationUpwardAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                box.layout(boxLeftTop.getLeft(), boxLeftTop.getTop(),
                        boxLeftTop.getRight(), boxLeftTop.getBottom());
                box.startAnimation(RotationRightwardAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        isPlaying = true;
        box.startAnimation(RotationRightwardAnimation);
    }

    private void startBoxFallAnimation() {
        ObjectAnimator objectAnimator =
                ObjectAnimator.ofFloat(box, "translationY", 0f, BOX_FALLING_LENGTH);
        objectAnimator.setDuration(BOX_FALLING_DURATION);
        objectAnimator.setInterpolator(new PathInterpolator(0.64f, 0.16f, 0.51f, 1.56f));
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                box.clearAnimation();
                startButtonAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        objectAnimator.start();
    }

    private void startButtonAnimation() {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(box, "ScaleX", 1f, 12.7f),
                ObjectAnimator.ofFloat(box, "ScaleY", 1f, 2.64f),
                ObjectAnimator.ofFloat(box, "translationX", 0, 21f));
        set.setDuration(BUTTON_ANIMATION_DURATION);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                box.clearAnimation();
                box.setScaleX(1);
                box.setScaleY(1);
                box.setTranslationX(0);
                box.setTranslationY(0);
                box.layout(boxLeftTop.getLeft(), boxLeftTop.getTop(),
                        boxLeftTop.getRight(), boxLeftTop.getBottom());
                box.setVisibility(View.INVISIBLE);
                if (mFoldingCubeListener != null) {
                    mFoldingCubeListener.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        set.start();
    }

    public void addListener(FoldingCubeAnimationListener listener) {
        mFoldingCubeListener = listener;
    }

    public interface FoldingCubeAnimationListener {
        void onAnimationEnd();
    }
}