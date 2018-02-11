package com.monster.market.views;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class CustomAnimation extends Animation {

	private AnimationListener listener;
	private CustomAnimCallBack callBack;

	public CustomAnimation(CustomAnimCallBack callBack) {
		this.callBack = callBack;
	}

	/**
	 * 设置动画监听器
	 */
	public void setAnimationListener(AnimationListener listener) {
		this.listener = listener;
	};

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);
		if (listener != null) {
			if (interpolatedTime == 0) {
				listener.onAnimationStart(this);
			} else if (interpolatedTime == 1) {
				listener.onAnimationEnd(this);
			}
		}

		if (callBack != null) {
			callBack.callBack(interpolatedTime, t);
		}
	}
}
