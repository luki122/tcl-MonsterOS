package com.mst.wallpaper.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;


public class CropImageView extends ImageView {


    private float mOldX = 0;
    private float mOldY = 0;

    private float mOldX_0 = 0;
    private float mOldY_0 = 0;

    private float mOldX_1 = 0;
    private float mOldY_1 = 0;

    private static final int STATUS_TOUCH_SINGLE = 1;
    private static final int STATUS_TOUCH_MULTI_START = 2;
    private static final int STATUS_TOUCH_MULTI_DRAGING = 3;

    private int mStatus = STATUS_TOUCH_SINGLE;

    // 默认裁剪宽高
    private Context mContext;
    private int mCropWidth = 480;
    private int mCropHeight = 800;

    private float mOriRationHW = 0f;
    private float mScreenRatioWH = 0f;
    private final float mMaxZoomOut = 5.0f;// 最大放大倍数
    private final float mMinZoomIn = 1f;// 最小放大倍数

    private Drawable mDrawable;// 原图
//    private FloatDrawable mFloatDrawable;// 浮层
    private Drawable mFloatDrawable;// 浮层
    private Rect mDrawableSrc = new Rect();// 原图裁剪矩形框
    private Rect mDrawableDst = new Rect();// 裁剪后图片矩形框
    private Rect mDrawableFloat = new Rect();// 裁剪浮层矩形框
    private boolean isFrist = true;
    private int mIsOnePointer;
    private float mRotation = 0;

    private TranslateAnimation trans;

    // 显示/隐藏状态栏
    private int mLastSystemUiVis = 0;
    private boolean isShowSystemUi = false;

    private final static boolean DEBUG = true;
    private final static String TAG = "CropImageView";

    Bitmap mBitmap = null; 

    public boolean mIsSaveEnable = false;

    private int mTouchSlop;
    private int mTouchDownX,mTouchDownY;
    private View.OnClickListener mClickListener;
    
    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mIsSaveEnable = false;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
    	// TODO Auto-generated method stub
    	mClickListener = l;
    }
    
    @Override
    public void setImageDrawable(Drawable drawable) {
     
       super.setImageDrawable(drawable);
       if (drawable != null)
       {
       	setDrawable(drawable);
       }
      
    }

    public void setImageDrawable(Drawable drawable, float rotation) {
        if (drawable != null) {
            mRotation = rotation;
            setImageDrawable(drawable);
        }
    }

    public void setDrawable(Drawable drawable) {
        
        mDrawable = drawable;
        mCropWidth = getWidth();
        mCropHeight = getHeight();
        isFrist = true;
        if (mDrawable.getIntrinsicWidth() > 0 || mDrawable.getIntrinsicHeight() > 0) {
            mIsSaveEnable = true;
        }
        if (mRotation > 0 && drawable.getIntrinsicHeight() > 0 && drawable.getIntrinsicWidth() > 0) {
            mDrawable = rotationDrawable(drawable, mRotation);
        }
   
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	if (mDrawable == null) {
            return;
        }

        // nothing to draw (empty bounds)
        if (mDrawable.getIntrinsicWidth() <=0 || mDrawable.getIntrinsicHeight() <= 0) {
            return;
        }
        configureBounds();


        mDrawable.draw(canvas);

        
        canvas.save();
        canvas.clipRect(mDrawableFloat, Region.Op.DIFFERENCE);
        canvas.drawColor(Color.parseColor("#a0000000"));
        canvas.restore();
      
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	
        if (event.getPointerCount() > 1) {
            if (mStatus == STATUS_TOUCH_SINGLE) {
                mStatus = STATUS_TOUCH_MULTI_START;

                mOldX_0 = event.getX(0);
                mOldY_0 = event.getY(0);

                mOldX_1 = event.getX(1);
                mOldY_1 = event.getY(1);
            } else if (mStatus == STATUS_TOUCH_MULTI_START) {
                mStatus = STATUS_TOUCH_MULTI_DRAGING;
            }
        } else {
            if (mStatus == STATUS_TOUCH_MULTI_START || mStatus == STATUS_TOUCH_MULTI_DRAGING) {
                mOldX_0 = 0;
                mOldY_0 = 0;

                mOldX_1 = 0;
                mOldY_1 = 0;

                mOldX = event.getX();
                mOldY = event.getY();
            }

            mStatus = STATUS_TOUCH_SINGLE;
			if (event.getActionMasked() == MotionEvent.ACTION_UP) {
					int newX = (int) event.getX();
					int newY = (int) event.getY();
					Log.d(TAG, "action_up newX:"+newX +"  mTouchDownX"+mTouchDownX+"   y:"+Math.abs(newY - mTouchDownY)+"  slop:"+mTouchSlop);
					if (Math.abs(newX - mTouchDownX) <= mTouchSlop
							&& Math.abs(newY - mTouchDownY) <= mTouchSlop) {
						if (mClickListener != null) {
							mClickListener.onClick(this);
						}
					}
				}else if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
					 	mTouchDownX = (int) event.getX();
		                mTouchDownY = (int) event.getY();
				}
        }


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            	mIsOnePointer = event.getPointerCount();
                mOldX = event.getX();
                mOldY = event.getY();
                //isShowSystemUi = true;
                mIsSaveEnable = false;
                break;

            case MotionEvent.ACTION_UP:
                checkBounds();
                mIsSaveEnable = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                //isShowSystemUi = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mStatus == STATUS_TOUCH_MULTI_DRAGING) {
                    float newx_0 = event.getX(0);
                    float newy_0 = event.getY(0);

                    float newx_1 = event.getX(1);
                    float newy_1 = event.getY(1);

                    float oldWidth = Math.abs(mOldX_1 - mOldX_0);
                    float oldHeight = Math.abs(mOldY_1 - mOldY_0);

                    float newWidth = Math.abs(newx_1 - newx_0);
                    float newHeight = Math.abs(newy_1 - newy_0);

                    boolean isDependHeight = Math.abs(newHeight - oldHeight) > Math.abs(newWidth - oldWidth);

                    float ration = isDependHeight ? ((float) newHeight / (float) oldHeight)
                            : ((float) newWidth / (float) oldWidth);
                    int centerX = mDrawableDst.centerX();
                    int centerY = mDrawableDst.centerY();
                    int _newHeight = (int) (mDrawableDst.height() * ration);
                    int _newWidth = (int) ((float) _newHeight / mOriRationHW);

                    float tmpZoomRation = (float) _newWidth / (float) mDrawableSrc.width();
                    if (tmpZoomRation >= mMaxZoomOut) {
                        _newHeight = (int) (mMaxZoomOut * mDrawableSrc.height());
                        _newWidth = (int) ((float) _newHeight / mOriRationHW);
                    } else if (tmpZoomRation <= mMinZoomIn) {
                        _newHeight = (int) (mMinZoomIn * mDrawableSrc.height());
                        _newWidth = (int) ((float) _newHeight / mOriRationHW);
                    }

                    mDrawableDst.set(centerX - _newWidth / 2, centerY - _newHeight / 2, centerX + _newWidth
                            / 2, centerY + _newHeight / 2);
                    invalidate();


                    mOldX_0 = newx_0;
                    mOldY_0 = newy_0;

                    mOldX_1 = newx_1;
                    mOldY_1 = newy_1;
                } else if (mStatus == STATUS_TOUCH_SINGLE) {
                    int dx = (int) (event.getX() - mOldX);
                    int dy = (int) (event.getY() - mOldY);

                    mOldX = event.getX();
                    mOldY = event.getY();

                    if (!(dx == 0 && dy == 0)) {
                        mDrawableDst.offset((int) dx, (int) dy);
                        invalidate();
                        //isShowSystemUi = false;
                    }
                }
                mIsSaveEnable = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                mIsSaveEnable = false;
                break;
        }

        return true;
    }

    private void configureBounds() {
        if (isFrist) {
            mOriRationHW = ((float) mDrawable.getIntrinsicHeight()) / ((float) mDrawable.getIntrinsicWidth());

            final float scale = mContext.getResources().getDisplayMetrics().density;

            int h = Math.min(getHeight(), (int) (mDrawable.getIntrinsicHeight() * scale + 0.5f));
            //int h = mDrawable.getIntrinsicHeight();
            int w = (int) (h / mOriRationHW);
            //int w = mDrawable.getIntrinsicWidth();

            int left = (getWidth() - w) / 2;
            int top = (getHeight() - h) / 2;
            int right = left + w;
            int bottom = top + h;

            mDrawableSrc.set(left, top, right, bottom);
            mDrawableDst.set(mDrawableSrc);

            mDrawableFloat.set(0, 0, mCropWidth, mCropHeight);

            isFrist = false;
        }
       mDrawable.setBounds(mDrawableDst);
    }

    private void checkBounds() {
        int newLeft = mDrawableDst.left;
        int newTop = mDrawableDst.top;

        boolean isChange = false;
    
        onRebound();


    }

    private void onRebound() {
        int disX = 0, disY = 0;
        if (mDrawableDst.height() < mDrawableFloat.height()) {
            disY = (mDrawableFloat.height() - mDrawableDst.height()) / 2 - mDrawableDst.top;
        } else {
            if (mDrawableDst.top > 0) {
                disY = -mDrawableDst.top;
            }
            if (mDrawableDst.bottom < mDrawableFloat.height()) {
                disY = mDrawableFloat.height() - mDrawableDst.bottom;
            }
        }

        if (mDrawableDst.width() < mDrawableFloat.width()) {
            disX = (mDrawableFloat.width() - mDrawableDst.width()) / 2 - mDrawableDst.left;
        } else {
            if (mDrawableDst.left > 0) {
                disX = -mDrawableDst.left;
            }
            if (mDrawableDst.right < mDrawableFloat.width()) {
                disX = mDrawableFloat.width() - mDrawableDst.right;
            }
        }

        mDrawableDst.offset(disX, disY);
        invalidate();
    }

    private boolean reScale() {
        float scaleX = 1.0f;
        float scaleY = 1.0f;

        return false;
    }

    private void setRect() {

    }

    public Bitmap getCropImage() {
       	final float scale = mContext.getResources().getDisplayMetrics().density;

       	int h = Math.min(getHeight(), (int) (mDrawable.getIntrinsicHeight() * scale + 0.5f));
       	int w = (int) (h / mOriRationHW);

       	int left = (getWidth() - w) / 2;
       	int top = (getHeight() - h) / 2;
       	int right = left + w;
       	int bottom = top + h;
       	
       	       
       
    	//postInvalidate();
        Bitmap tempBitmap = null;
        try {  
            tempBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);	//OutOfMemoryError
        } catch (OutOfMemoryError e) {  
            while(tempBitmap == null) {  
                System.gc();  
                System.runFinalization();  
            }  
        }  
        Canvas canvas = new Canvas(tempBitmap);
        if (mDrawable.getBounds().height() != getHeight() || mDrawable.getBounds().width() != getWidth()) {
			mDrawable.setBounds(left, top, right, bottom);
		} else {
		}
        
        mDrawable.setBounds(mDrawableDst);
        mDrawable.draw(canvas);
        return tempBitmap;
    }

    public int dipTopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 处理移动回弹
     * 
     * @param disX
     * @param disY
     */
    public void rebound(int disX, int disY) {
        trans = new TranslateAnimation(-disX, 0, -disY, 0);
        trans.setInterpolator(new AccelerateInterpolator());
        trans.setDuration(300);
        this.startAnimation(trans);
    }


    private void logD(String title, String body) {
        if (DEBUG) {
            Log.d(TAG, title + ":" + body);
        }
    }

    private Drawable rotationDrawable(Drawable drawable, float rotation) {
        try {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                    : Bitmap.Config.RGB_565;
            Bitmap bitmap = Bitmap.createBitmap(width, height, config);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
            Bitmap newBitmap = adjustPhotoRotation(bitmap, rotation);
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return new BitmapDrawable(newBitmap);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return drawable;
        } catch (Exception e) {
            e.printStackTrace();
            return drawable;
        }
    }

    private Bitmap adjustPhotoRotation(Bitmap bm, final float orientationDegree) {
        try {
            Matrix m = new Matrix();
            int height = bm.getHeight();
            int width = bm.getWidth();
            m.setRotate(orientationDegree, ( float ) width / 2, ( float ) height / 2);
            float targetX = 0;
            float targetY = 0;
            if (orientationDegree == 90 || orientationDegree == 270) {
                if (width > height) {
                    targetX = ( float ) height / 2 - ( float ) width / 2;
                    targetY = 0 - targetX;
                } else {
                    targetY = ( float ) width / 2 - ( float ) height / 2;
                    targetX = 0 - targetY;
                }
            }
            m.postTranslate(targetX, targetY);
            Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
            Paint paint = new Paint();
            Canvas canvas = new Canvas(bm1);
            canvas.drawBitmap(bm, m, paint);
            return bm1;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return bm;
        } catch (Exception e) {
            return bm;
        }
    }

	


}
