package cn.tcl.music.view;

import cn.tcl.music.R;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class SnackBar extends Dialog {

    private String text;
    private float textSize = 14;
    private String buttonText;
    private View.OnClickListener onClickListener;
    private Activity activity;
    private View view;
    private ButtonFlat button;
    private int backgroundButton = Color.parseColor("#eeff41");

    // With action button
    public SnackBar(Activity activity, String text, String buttonText, View.OnClickListener onClickListener) {
        super(activity, android.R.style.Theme_Translucent);
        this.activity = activity;
        this.text = text;
        this.buttonText = buttonText;
        this.onClickListener = onClickListener;
    }

    @Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.snackbar);
        setCanceledOnTouchOutside(false);
        ((TextView)findViewById(R.id.text)).setText(text);
        ((TextView)findViewById(R.id.text)).setTextSize(textSize); //set textSize
        button = (ButtonFlat) findViewById(R.id.buttonflat);
        if(text == null || onClickListener == null){
            button.setVisibility(View.GONE);
        }else{
            button.setText(buttonText);
            button.setBackgroundColor(backgroundButton);

            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                    onClickListener.onClick(v);
                }
            });
        }
        view = findViewById(R.id.snackbar);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dismiss();
        return activity.dispatchTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void show() {
        super.show();
        view.setVisibility(View.VISIBLE);
        view.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.snackbar_show_animation));
    }

    @Override
    public void dismiss() {
        Animation anim = AnimationUtils.loadAnimation(activity, R.anim.snackbar_hide_animation);
        anim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                SnackBar.super.dismiss();
            }
        });
        view.startAnimation(anim);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK )  {
             dismiss();
         }
        return super.onKeyDown(keyCode, event);
    }

    public void update(String text, View.OnClickListener onClickListener) {
        this.text = text;
        this.onClickListener = onClickListener;
    	
    	((TextView)findViewById(R.id.text)).setText(text);
        button = (ButtonFlat) findViewById(R.id.buttonflat);
        if(text == null || onClickListener == null){
            button.setVisibility(View.GONE);
        }else{
        	button.setVisibility(View.VISIBLE);
        }
    }
    
}