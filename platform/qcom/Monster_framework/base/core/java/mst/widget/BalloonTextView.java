package mst.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


/**
 *
 */
public class BalloonTextView extends RelativeLayout{
    private static final String TAG = "BalloonTextView";
    private ArrayList<Balloon> mBalloons;
    private int mDiameter = 240;
    private float mDispersion = 0.8f;
    private final int DEFAULT_COLOR = Color.GRAY;
    private int[] mColors = {Color.parseColor("#ff00CB98"),Color.parseColor("#ff66CCFF"),Color.parseColor("#fff97400")};
    private int mGap = 10;
    private boolean hasFocus = false;
    private int mFocusLayout = -1;
    private int[] mFocusColor = {Color.parseColor("#ff19A8AE"),Color.parseColor("#ffCACCCE")};

    public static final int TYPE_BITMAP = 1;
    public static final int TYPE_TEXT = 0;

    private String mTypeface = "monster-normal";
    private int mTypeStyle = Typeface.NORMAL;

    public BalloonTextView(Context context) {
        super(context);
        init();
    }

    public BalloonTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BalloonTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mBalloons = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setBalloon(int layer, String text){
        Balloon balloon = null;
        setFocus(layer);
        if(layer <= mBalloons.size()-1){
            balloon = mBalloons.get(layer);
            if(balloon != null) {
                if(balloon.type == TYPE_BITMAP){
                    balloon.changed();
                    balloon.type = TYPE_TEXT;
                }
                balloon.color = getColorByLayer(layer);
                balloon.text = text;
                setText(balloon);
            }
        }else if(layer == mBalloons.size()){
            balloon = new Balloon();
            balloon.type = TYPE_TEXT;
            balloon.layer = layer;
            balloon.diameter = getRadiusByLayer(layer);
            balloon.text = text;
            balloon.color = getColorByLayer(layer);
            createView(balloon);
            mBalloons.add(balloon);
        }
    }

    public void setBalloon(int layer, Drawable bitmap){
        bitmap.setTint(Color.WHITE);
        Balloon balloon = null;
        if(layer <= mBalloons.size()-1){
            balloon = mBalloons.get(layer);
            if(balloon != null) {
                if(balloon.type == TYPE_TEXT) {
                    balloon.changed();
                    balloon.type = TYPE_BITMAP;
                }
                balloon.bitmap = bitmap;
                setImageBitmap(balloon);
            }
        }else if(layer == mBalloons.size()){
            balloon = new Balloon();
            balloon.type = TYPE_BITMAP;
            balloon.layer = layer;
            balloon.diameter = getRadiusByLayer(layer);
            balloon.bitmap = bitmap;
            balloon.color = getColorByLayer(layer);
            createView(balloon);
            mBalloons.add(balloon);
        }
    }

    private void createView(Balloon balloon){
        TextView textview = new TextView(getContext());
        balloon.view = textview;
        textview.setGravity(Gravity.CENTER);
        textview.setTextColor(Color.WHITE);
        textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, balloon.diameter / 3f);
        textview.setTypeface(Typeface.create(mTypeface,mTypeStyle));

        Balloon lastBalloon = null;
        if(balloon.layer > 0){
            lastBalloon = mBalloons.get(balloon.layer - 1);
        }

        LayoutParams lp = new LayoutParams(balloon.diameter,balloon.diameter);
        if(lastBalloon != null){
            lp.leftMargin = lastBalloon.left + Math.round(lastBalloon.diameter/2f + lastBalloon.content_wdith/2f) + mGap;
            lp.topMargin = lastBalloon.top + Math.round(lastBalloon.diameter/2f - balloon.diameter/2f);;
        }else{
            lp.leftMargin = 0;
            lp.topMargin = 0;
        }
        balloon.top = lp.topMargin;
        balloon.left = lp.leftMargin;

        balloon.changed();
        if(balloon.type == TYPE_TEXT){
            setText(balloon);
        }else if(balloon.type == TYPE_BITMAP){
            setImageBitmap(balloon);
        }

        this.addView(balloon.view,lp);

    }

    private void setImageBitmap(Balloon balloon){
        if(balloon.isChanged()) {
            balloon.content_wdith = balloon.content_height = (int) (balloon.diameter / 3f);
        }
        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.setBounds(0,0,balloon.diameter,balloon.diameter);
        drawable.getPaint().setColor(balloon.color);

        balloon.bitmap.setBounds(0,0,
                balloon.content_wdith,
                balloon.content_height);
        //Bitmap bitmap = Bitmap.createScaledBitmap(balloon.bitmap,balloon.content_wdith,balloon.content_height,false);
        Bitmap finalBitmap = Bitmap.createBitmap(balloon.diameter,balloon.diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(finalBitmap);
        canvas.save();
        drawable.draw(canvas);
        canvas.restore();
        //canvas.drawBitmap(bitmap,balloon.diameter/2f-balloon.content_wdith/2f,balloon.diameter/2f-balloon.content_height/2f,new Paint());
        canvas.save();
        canvas.translate(balloon.diameter/2f-balloon.content_wdith/2f,balloon.diameter/2f-balloon.content_height/2f);
        balloon.bitmap.draw(canvas);
        canvas.restore();
        balloon.view.setText(null);
        balloon.view.setBackground(new BitmapDrawable(getContext().getResources(),finalBitmap));
    }

    private void setText(Balloon balloon){
        if(balloon.isChanged()) {
            Rect textBound = new Rect();
            Paint paint = balloon.view.getPaint();
            paint.getTextBounds(balloon.text, 0, balloon.text.length(), textBound);
            balloon.content_wdith = textBound.width();
            balloon.content_height = textBound.height();
        }
        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.setBounds(0,0,balloon.diameter,balloon.diameter);
        drawable.getPaint().setColor(balloon.color);
        balloon.view.setBackground(drawable);
        balloon.view.setText(balloon.text);
    }

    public void setGap(int gap){
        mGap = gap;
    }

    public void setLayerColor(int... colors){
        if(colors == null) return;
        int layers = colors.length;
        mColors = new int[layers];
        for(int i=0;i<layers;i++){
            mColors[i] = colors[i];
        }
    }

    public void setFocusColor(int... colors){
        if(colors == null) return;
        for(int i=0; i<colors.length;i++){
            boolean avalidate = false;
            if(i == 0 || i == 1){
                avalidate = true;
            }
            if(avalidate){
                mFocusColor[i] = colors[i];
            }
        }
    }

    public void requestColorFocus(boolean focus){
        hasFocus = focus;
    }

    public void clear(){
        setFocus(-1);
        this.removeAllViews();
        mBalloons.clear();
    }

    public void clear(int layer){
        setFocus(layer - 1);
        int size = mBalloons.size();
        if(layer >= 0 && layer < size){
            for(int i=size-1;i>=layer;i--) {
                Balloon balloon = mBalloons.remove(i);
                if (balloon != null) {
                    this.removeView(balloon.view);
                }
            }
        }

    }

    private int getRadiusByLayer(int layer){
        int ret = 0;
        if(layer <= 0){
            ret = mDiameter;
        }else if(layer < 3){
            ret = (int)(mDiameter * Math.pow(mDispersion,layer));
        }else{
            ret = (int)(mDiameter * Math.pow(mDispersion,2));
        }
        return ret;
    }

    private int getColorByLayer(int layer){
        if(hasFocus){
            if(layer == mFocusLayout){
                return mFocusColor[0];
            }else{
                return mFocusColor[1];
            }
        }else {
            if (mColors == null || mColors.length == 0) {
                return 0;
            }
            if (layer < 0) {
                return mColors[0];
            }
            if (layer >= 0 && layer < mColors.length) {
                return mColors[layer];
            } else {
                return mColors[mColors.length - 1];
            }
        }
    }

    public void setFocus(int focus){
        boolean changed = mFocusLayout != focus;
        if(changed){
            focusChanged(mFocusLayout, focus);
        }
        mFocusLayout = focus;
    }

    private void focusChanged(int old, int news){
        Balloon balloon = null;
        if(old > news){
            if(news >= 0 && news < mBalloons.size()){
                balloon = mBalloons.get(news);
                balloon.color = getColorByLayer(old);
            }
        }else{
            if(old >= 0 && old < mBalloons.size()){
                balloon = mBalloons.get(old);
                balloon.color = getColorByLayer(news);
            }
        }
        if(balloon != null){
            if(balloon.type == TYPE_TEXT){
                setText(balloon);
            }else if(balloon.type == TYPE_BITMAP){
                setImageBitmap(balloon);
            }
        }
    }

    public void setColorByLayer(int layer,int color){
        if(mColors == null || mColors.length == 0){
            return;
        }
        if(layer >= 0 && layer < mColors.length){
            mColors[layer] = color;
        }
    }

    public void setDiameter(int d){
        mDiameter = d;
    }

    public void setDispersion(float d){
        mDispersion = d;
    }

    public static class Balloon{
        public int type;
        public String text;
        public Drawable bitmap;
        public int diameter;
        public int left;
        public int top;
        public int color;
        public int layer;
        public int content_wdith;
        public int content_height;
        public TextView view;
        private boolean changed = false;
        public void changed(){
            changed = true;
        }
        public boolean isChanged(){
            if(changed){
                changed = false;
                return true;
            }
            return false;
        }

    }
}