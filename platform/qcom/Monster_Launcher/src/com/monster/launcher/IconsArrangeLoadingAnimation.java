package com.monster.launcher;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class IconsArrangeLoadingAnimation {

	private AnimatorSet animatorSet;
	
	public void setAnimatorSet(AnimatorSet animatorSet){
		this.animatorSet = animatorSet;
	}
	
	public AnimatorSet setupAnimation(View v1, View v2, View v3) {

		AnimatorSet img1_expand = imageViewProcessExpand(v1);
		img1_expand.setInterpolator(new DecelerateInterpolator());
		img1_expand.setDuration(500);

		AnimatorSet img1_narrow = imageViewProcessNarrow(v1);
		img1_narrow.setInterpolator(new AccelerateInterpolator());
		img1_narrow.setDuration(500);
		img1_narrow.setStartDelay(500);

		AnimatorSet img2_expand = imageViewProcessExpand(v2);
		img2_expand.setInterpolator(new DecelerateInterpolator());
		img2_expand.setDuration(500);
		img1_narrow.setStartDelay(200);

		AnimatorSet img2_narrow = imageViewProcessNarrow(v2);
		img2_narrow.setInterpolator(new AccelerateInterpolator());
		img2_narrow.setDuration(500);
		img2_narrow.setStartDelay(700);

		AnimatorSet img3_expand = imageViewProcessExpand(v3);
		img3_expand.setDuration(500);
		img1_narrow.setStartDelay(400);

		AnimatorSet img3_narrow = imageViewProcessNarrow(v3);
		img3_narrow.setDuration(500);
		img3_narrow.setStartDelay(900);

		AnimatorSet aSet = new AnimatorSet();
		aSet.playTogether(img1_expand, img1_narrow, img2_expand, img2_narrow,
				img3_expand, img3_narrow);

		return aSet;
	}

	private AnimatorSet imageViewProcessExpand(View view) {
		AnimatorSet aSet = new AnimatorSet();

		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0, 1.0f);
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0, 1.0f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0, 1.0f);

		alpha.setRepeatCount(ValueAnimator.INFINITE);
		scaleX.setRepeatCount(ValueAnimator.INFINITE);
		scaleY.setRepeatCount(ValueAnimator.INFINITE);

		alpha.setRepeatMode(ValueAnimator.REVERSE);
		scaleX.setRepeatMode(ValueAnimator.REVERSE);
		scaleY.setRepeatMode(ValueAnimator.REVERSE);

		aSet.playTogether(alpha, scaleX, scaleY);
		return aSet;
	}

	private AnimatorSet imageViewProcessNarrow(View view) {
		AnimatorSet aSet = new AnimatorSet();

		ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0f);
		ObjectAnimator scaleX = ObjectAnimator
				.ofFloat(view, "scaleX", 1.0f, 0f);
		ObjectAnimator scaleY = ObjectAnimator
				.ofFloat(view, "scaleY", 1.0f, 0f);

		alpha.setRepeatCount(ValueAnimator.INFINITE);
		scaleX.setRepeatCount(ValueAnimator.INFINITE);
		scaleY.setRepeatCount(ValueAnimator.INFINITE);

		alpha.setRepeatMode(ValueAnimator.REVERSE);
		scaleX.setRepeatMode(ValueAnimator.REVERSE);
		scaleY.setRepeatMode(ValueAnimator.REVERSE);

		aSet.playTogether(alpha, scaleX, scaleY);
		return aSet;
	}

	public void startAnimation() {
		if (animatorSet != null) {
			if (animatorSet.isRunning()) {
				animatorSet.cancel();
			}
			animatorSet.start();
		}
	}

	public void endAnimation() {
		if (animatorSet != null) {
			if (animatorSet.isStarted() || animatorSet.isRunning()) {
				animatorSet.cancel();
			}
		}
	}
}
