/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.mst.ui;

import java.util.ArrayList;

import com.android.incallui.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import android.util.Log;

public class MstAnswerArrow {
    private static final String TAG = "MstAnswerArrow";
    private float mCenterX;
    private float mCenterY;
    private float mScale = 1.0f;
	private int r;
	
	private Drawable mAnswer;
	private Drawable mReject;
	int drawableHeight;
	int drawableWidth;
	
	
    public WaveManager waveManager = new WaveManager();

    public class WaveManager {
        private float delta = 0f;
        private float delta2 = 0f;
        private float alpha = 1f;

        public void setDeltaY(float r) {
        	delta = r;
        }

        public float getDeltaY() {
            return delta;
        }           
        
        public void setDeltaY2(float r) {
        	delta2 = r;
        }

        public float getDeltaY2() {
            return delta2;
        }             

        public void setAlpha(float a) {
            alpha = a;
        }

        public float getAlpha() {
            return alpha;
        }
    };

    public MstAnswerArrow(Context context) { 
    	r = context.getResources().getDimensionPixelSize(R.dimen.glowpadview_target_placement_radius_mst);
    	
    	mAnswer = context.getResources().getDrawable(R.drawable.arrow_answer);	
    	mReject = context.getResources().getDrawable(R.drawable.arrow_reject);    
    	
    	drawableHeight = mAnswer.getIntrinsicHeight();
		drawableWidth = mAnswer.getIntrinsicWidth();
		mAnswer.setBounds(0, 0, drawableWidth, drawableHeight);
		mReject.setBounds(0, 0, drawableWidth, drawableHeight);
    }

    public void setCenter(float x, float y) {
		Log.i(TAG, "x = " + x);
		Log.i(TAG, "y = " + y);
        mCenterX = x;
        mCenterY = y;
    }    


    public void setScale(float scale) {
        mScale  = scale;
    }

    public float getScale() {
        return mScale;
    }

    public void draw(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(mScale, mScale, mCenterX, mCenterY);
		canvas.translate(mCenterX, mCenterY);
		canvas.translate(0, -r);
		canvas.translate(0, - waveManager.getDeltaY2());
		canvas.translate(-0.5f * drawableWidth, -0.5f * drawableHeight + waveManager.getDeltaY());
		mAnswer.setAlpha((int) Math.round(waveManager.getAlpha() * 255f));
		mAnswer.draw(canvas);
		
		canvas.translate(0, 2 * (r  - waveManager.getDeltaY()));
		mReject.setAlpha((int) Math.round(waveManager.getAlpha() * 255f));
		mReject.draw(canvas);

		canvas.restore();
    }

    private float mAlpha = 1.0f;
    
    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }
    
    public float getAlpha() {
        return mAlpha;
    }
}
