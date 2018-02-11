package com.monster.launcher.theme;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.monster.launcher.theme.interfaces.IIconGetter;

public class MainActivity extends Activity {
    IconGetterManager iconGetter;
    ImageView imageView1;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;
    Button refrushButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        iconGetter = IconGetterManager.getInstance(this,false);
//        if(iconGetter!=null){
//            iconGetter.setUseMemoryCache(true);
//        }
//        imageView1 = (ImageView) findViewById(R.id.image1);
//        imageView2 = (ImageView) findViewById(R.id.image2);
//        imageView3 = (ImageView) findViewById(R.id.image3);
//        imageView4 = (ImageView) findViewById(R.id.image4);
//        refrushButton = (Button) findViewById(R.id.refrushButton);
//        refrushButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                refrush();
//            }
//        });
//        refrush();
    }

//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//    private void refrush(){
//        iconGetter = IconGetterManager.getInstance(this,false).setUseMemoryCache(true);
//        Log.d("---lj---"," iconGetter : "+iconGetter);
//        long t1 = System.currentTimeMillis();
//        Drawable drawable1 = iconGetter.getIconDrawable("com.monster.launcher");
//        long t11 = System.currentTimeMillis();
//        long t2 = System.currentTimeMillis();
//        Drawable drawable2 = iconGetter.getIconDrawable("com.monster.market");
//        long t22 = System.currentTimeMillis();
//        long t3 = System.currentTimeMillis();
//        Drawable drawable3 = iconGetter.getIconDrawable("com.android.mms");
//        long t33 = System.currentTimeMillis();
//        long t4 = System.currentTimeMillis();
//        Drawable drawable4 = iconGetter.getIconDrawable("com.android.calendar");
//        long t44 = System.currentTimeMillis();
//
//        long s1 = System.currentTimeMillis();
//        drawable1 = iconGetter.getIconDrawable("com.monster.launcher");
//        long s11 = System.currentTimeMillis();
//        long s2 = System.currentTimeMillis();
//        drawable2 = iconGetter.getIconDrawable("com.monster.market");
//        long s22 = System.currentTimeMillis();
//        long s3 = System.currentTimeMillis();
//        drawable3 = iconGetter.getIconDrawable("com.android.mms");
//        long s33 = System.currentTimeMillis();
//        long s4 = System.currentTimeMillis();
//        drawable4 = iconGetter.getIconDrawable("com.android.calendar");
//        long s44 = System.currentTimeMillis();
//
//        Log.d("---lj---"," first : " + (t11-t1) +"," + (t22-t2) +"," + (t33-t3) +"," + (t44-t4));
//        Log.d("---lj---","second : " + (s11-s1) +"," + (s22-s2) +"," + (s33-s3) +"," + (s44-s4));
//
//        imageView1.setImageDrawable(drawable1);
//        imageView2.setImageDrawable(drawable2);
//        imageView3.setImageDrawable(drawable3);
//        imageView4.setImageDrawable(drawable4);
//
//    }
}
