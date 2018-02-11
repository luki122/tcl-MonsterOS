package com.android.camera.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
/* MODIFIED-BEGIN by xuan.zhou, 2016-11-10,BUG-3412336*/
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
/* MODIFIED-END by xuan.zhou,BUG-3412336*/
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

import com.android.camera.Thumbnail;
import com.android.camera.debug.Log;
import com.android.camera.util.BitmapPackager;
import com.android.camera.util.Gusterpolator;
import com.tct.camera.R;

public class PeekImageView extends RotateImageView{

    private Log.Tag TAG=new Log.Tag("PeekImageView");
    public static final float STROKE_WIDTH=2f; // ignore, use bottom_bar_peek_thumb_stroke_width instead
    public static final float CIRCLE_OFFSET=0;
    private static final float SCALE_LOWER=0.1f;
    private static final float SCALE_UPPER=1.0f;
    private static final float SCALE_DEFAULT=SCALE_UPPER;
    private static final int ANIMATE_BULB_DURATION=150;

    private float mSpacer;
    private Paint mBitmapPaint;
    private Paint mStrokePaint;
    private float mStrokeWidth;
    private Matrix mMatrix;

    public PeekImageView(Context context){
        super(context);
        init();
    }

    public PeekImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        parseAttributes(context, attrs);

    }

    private void init() {
        Resources res = getContext().getResources();
        mSpacer = res.getDimension(R.dimen.bottom_bar_peek_thumb_spacer);

        mBitmapPaint = new Paint();
        mBitmapPaint.setColor(Color.WHITE);

        mStrokePaint = new Paint();
        mStrokeWidth = res.getDimension(R.dimen.bottom_bar_peek_thumb_stroke_width);
        mStrokePaint.setColor(res.getColor(R.color.bottom_bar_peek_thumb_border_color));
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStrokeWidth(mStrokeWidth);
        mStrokePaint.setStyle(Paint.Style.STROKE);

        mMatrix = new Matrix();
    }

    private void parseAttributes(Context context,AttributeSet attrs){
        TypedArray a=context.getTheme().obtainStyledAttributes(attrs,R.styleable.PeekImageView,0,0);
        int src=a.getResourceId(R.styleable.PeekImageView_src, 0);
        a.recycle();
        setViewImage(src);
    }


    private Drawable mDrawable;
    public void setViewImage(int resId){
        mDrawable=getResources().getDrawable(resId, null);
        mBitmap=new BitmapPackager(BitmapFactory.decodeResource(getResources(), resId));
        invalidate();
    }

    private BitmapPackager mBitmap;
    public void setViewImage(Bitmap bmp){
        mDrawable=new BitmapDrawable(this.getContext().getResources(), bmp);
        mBitmap=new BitmapPackager(bmp);
        invalidate();
    }

    private Uri mUri;
    public void setViewThumb(Thumbnail thumb){
        if (thumb != null) {
            mUri = thumb.getUri();
            setViewImage(thumb.getBitmap());
        } else {
            // if thumb is null, show original drawable
            mUri = null;
            setViewImage(R.drawable.camera_photo_library);
        }
    }

    public void setViewThumbBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            setViewImage(bitmap);
        } else {
            setViewImage(R.drawable.camera_photo_library);
        }
    }


    private ValueAnimator mBulbAnimator;
    private float mAnimatorFraction=SCALE_DEFAULT;
    public void animateThumbBitmap(Bitmap bitmap){
        if(mBulbAnimator==null){
            mBulbAnimator=buildBulbAnimator(bitmap);
        }
        setViewImage(bitmap);
        if(mBulbAnimator.isRunning()){
            mBulbAnimator.cancel();
        }
        mNeedSuperDrawable=false;
        mBulbAnimator.start();
    }

    private ValueAnimator buildBulbAnimator(final Bitmap bitmap){

        ValueAnimator animator=ValueAnimator.ofFloat(SCALE_LOWER,SCALE_UPPER);
        animator.setDuration(ANIMATE_BULB_DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAnimatorFraction = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mAnimatorFraction = 1f;
                mNeedSuperDrawable=true;
                invalidate();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mAnimatorFraction = 1f;
                mNeedSuperDrawable=true;
                invalidate();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        return animator;
    }

    private boolean mNeedSuperDrawable=true;
    @Override
    protected boolean needSuperDrawable() {
        return mNeedSuperDrawable;
    }

    public void setViewThumbUri(Uri peekthumbUri) {
        mUri = peekthumbUri;
    }

    public Uri getUri() {
        return mUri;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if(/*mDrawable==null||*/canvas.getWidth()<=0){
            if(mNeedSuperDrawable) {
                super.onDraw(canvas);
            }
            return ;
        }
        this.setAlpha(mAnimatorFraction);

        float radius = getRadius();
        float x = getCenterPoint();
        float y = getCenterPoint();
        mMatrix.postTranslate(CIRCLE_OFFSET, CIRCLE_OFFSET);
        mMatrix.postScale(mAnimatorFraction, mAnimatorFraction, x, y);
        Bitmap peekOutput = newGeneratePeekBitmap(radius, mSpacer, mBitmap.get()).get(); // MODIFIED by xuan.zhou, 2016-11-10,BUG-3412336
        canvas.drawBitmap(peekOutput, mMatrix, mBitmapPaint);
        mMatrix.reset();

        if(mNeedSuperDrawable) {
            super.onDraw(canvas);
        }

        canvas.drawCircle(x, y, radius - mStrokeWidth / 2, mStrokePaint);
    }

    public float getCenterPoint(){
        return getRadius()+CIRCLE_OFFSET;
    }

    public float getRadius(){
        return getWidth()/2-CIRCLE_OFFSET;
    }


    private Bitmap mSrcBitmap;
    private BitmapPackager mBitmapPackager;
    private BitmapPackager generatePeekBitmap(float radius, float spacer, Bitmap src){
        if(src==mSrcBitmap){
            return mBitmapPackager;
        }
        final int bmpWidth=src.getWidth();
        final int bmpHeight=src.getHeight();


        int scaledWidth=0;
        int scaledHeight=0;
        int transX=0;
        int transY=0;
        int subWidth=0;
        int subHeight=0;
        //Not to make the image stretched , make the long side align with the radius
        if(bmpWidth>bmpHeight){
            scaledWidth=(int)radius*2*bmpWidth/bmpHeight;
            scaledHeight=(int)radius*2;
            transX=(int)((scaledWidth-2*radius)/2)*bmpWidth/scaledWidth;
            transY=0;
            subWidth=subHeight=bmpHeight;
        }else{
            scaledWidth=(int)radius*2;
            scaledHeight=(int)radius*2*bmpHeight/bmpWidth;
            transX=0;
            transY=(int)((scaledHeight-2*radius)/2)*bmpHeight/scaledHeight;
            subWidth=subHeight=bmpWidth;
        }

        //Radius*2 is always the length of shorter border
        Bitmap bmp=Bitmap.createBitmap((int)radius*2, (int)radius*2, Config.ARGB_8888);
        Canvas canvas=new Canvas(bmp);
        Paint paint=new Paint();
        Log.w(TAG,String.format("width is %d height is %d transX is %d transY is %d subWidth is %d subHeight is %d",bmpWidth,bmpHeight,transX,transY,subWidth,subHeight));
        BitmapPackager scaledbitmap=new BitmapPackager(Bitmap.createBitmap(src, transX, transY, subWidth, subHeight));
        Matrix mat=new Matrix();

        mat.setScale(radius*2/(float)subWidth, radius*2/(float)subHeight);
        Path path=new Path();
        path.addCircle(radius, radius, radius - spacer, Direction.CW);
        canvas.clipPath(path);
        canvas.drawBitmap(scaledbitmap.get(), mat, paint);
        mBitmapPackager=new BitmapPackager(bmp);
        mSrcBitmap=src;
        this.setBitmap(bmp);
        return mBitmapPackager;

    }

    /* MODIFIED-BEGIN by xuan.zhou, 2016-11-10,BUG-3412336*/
    private BitmapPackager newGeneratePeekBitmap(float radius, float spacer, Bitmap src) {
        if (src == mSrcBitmap) {
            return mBitmapPackager;
        }
        final int bmpWidth = src.getWidth();
        final int bmpHeight = src.getHeight();

        int scaledWidth = 0;
        int scaledHeight = 0;
        int transX = 0;
        int transY = 0;
        int subWidth = 0;
        int subHeight = 0;
        //Not to make the image stretched , make the long side align with the radius
        if (bmpWidth > bmpHeight) {
            scaledWidth = (int) radius * 2 * bmpWidth / bmpHeight;
            scaledHeight = (int) radius * 2;
            transX = (int) ((scaledWidth - 2 * radius) / 2) * bmpWidth / scaledWidth;
            transY = 0;
            subWidth = subHeight = bmpHeight;
        } else {
            scaledWidth = (int) radius * 2;
            scaledHeight = (int) radius * 2 * bmpHeight / bmpWidth;
            transX = 0;
            transY = (int) ((scaledHeight - 2 * radius) / 2) * bmpHeight / scaledHeight;
            subWidth = subHeight = bmpWidth;
        }

        Log.w(TAG, String.format(
                "width is %d height is %d transX is %d transY is %d subWidth is %d subHeight is %d",
                bmpWidth, bmpHeight, transX, transY, subWidth, subHeight));
        Matrix mat = new Matrix();
        mat.setScale(radius * 2 / (float) subWidth, radius * 2 / (float) subHeight);

        BitmapPackager scaledBitmap = new BitmapPackager(
                Bitmap.createBitmap(src, transX, transY, subWidth, subHeight, mat, false));

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(R.color.bottombar_background_default));

        //Radius*2 is always the length of shorter border
        Bitmap bmp = Bitmap.createBitmap((int) radius * 2, (int) radius * 2, Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(radius, radius, radius - spacer, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledBitmap.get(), 0, 0, paint);

        mBitmapPackager = new BitmapPackager(bmp);
        mSrcBitmap = src;
        this.setBitmap(bmp);
        return mBitmapPackager;
    }
    /* MODIFIED-END by xuan.zhou,BUG-3412336*/

    @Override
    protected boolean needTranslation() {
        return false;
    }
    @Override
    public void setEnabled(boolean enabled) {
        enableFilter(false);
        super.setEnabled(enabled);
    }
}
