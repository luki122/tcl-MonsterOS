/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import cn.tcl.note.R;
import cn.tcl.note.util.FileUtils;
import cn.tcl.note.util.NoteLog;

public class ImgCropView extends View {
    private static final int POS_TOP_LEFT = 0;
    private static final int POS_TOP_RIGHT = 1;
    private static final int POS_BOTTOM_LEFT = 2;
    private static final int POS_BOTTOM_RIGHT = 3;
    private static final int POS_TOP = 4;
    private static final int POS_BOTTOM = 5;
    private static final int POS_LEFT = 6;
    private static final int POS_RIGHT = 7;
    private static final int POS_CENTER = 8;
    private final String TAG = ImgCropView.class.getSimpleName();
    private final float BORDER_LINE_WIDTH = 2f;
    private final float BORDER_TOUCH_WIDTH = 30f;
    private final float BORDER_MIN_WIDTH = 2 * BORDER_TOUCH_WIDTH;
    private final float BORDER_DEFAULT_WIDTH = 200f;
    private final float BORDER_DEFAULT_HEIGHT = 200f;
    private final float IMG_LEFT = 0f;
    private final float IMG_TOP = 0f;

    private float mImgWidthRation;
    private float mImgHeightRation;
    //img info
    private Bitmap mNormalImg;
    private String mImgName;
    //rect
    private RectF mBorderRect;
    private RectF mImgRect;//img size
    //paint
    private Paint mImgPaint;
    private Paint mBorderPaint;
    private Paint mBgPaint;
    private Paint mGuideLinePaint;
    private Paint mCornerPaint;
    private float mCornerRadio;
    //touch pos
    private int mTouchPos = -1;
    private boolean isFirst;

    private int mImgScalSize;
    private PointF mLastPoint = new PointF();

    public ImgCropView(Context context) {
        super(context);
        init(context);
    }

    public ImgCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        Resources res = context.getResources();
        mImgPaint = new Paint();
        mImgPaint.setAntiAlias(true);
        mImgPaint.setFilterBitmap(true);

        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(context.getColor(R.color.crop_border_color));
        mBorderPaint.setStrokeWidth(res.getDimension(R.dimen.crop_border_line));

        mBgPaint = new Paint();
        mBgPaint.setColor(context.getColor(R.color.crop_bg_color));

        mGuideLinePaint = new Paint();
        mGuideLinePaint.setColor(context.getColor(R.color.crop_guide_line_color));
        mGuideLinePaint.setStrokeWidth(res.getDimension(R.dimen.crop_guide_line));

        mCornerPaint = new Paint();
        mCornerPaint.setColor(context.getColor(R.color.crop_corner_color));
        mCornerRadio = getResources().getDimension(R.dimen.crop_corner_radio);
    }

    public void setImg(String imgName) {
        mImgName = imgName;
        isFirst = true;
        invalidate();
    }

    private void initImg() {
        String imgPath = FileUtils.getPicWholePath(mImgName);
        BitmapFactory.Options bmOptions = getBitmapOptions(imgPath);

        mNormalImg = BitmapFactory.decodeFile(imgPath, bmOptions);

        mImgRect = new RectF();
        float phoneH = getHeight();
        float phoneW = getWidth();
        float imgH = mNormalImg.getHeight();
        float imgW = mNormalImg.getWidth();
        float phoneRation = phoneH / phoneW;
        float imgRation = imgH / imgW;

        //img width fill
        if (phoneRation > imgRation) {
            mImgRect.left = IMG_LEFT;
            mImgRect.right = mImgRect.left + phoneW;

            float imgHeight = imgRation * phoneW;
            mImgRect.top = (phoneH - imgHeight) / 2;
            mImgRect.bottom = mImgRect.top + imgHeight;
        } else {
            //img height fill
            mImgRect.top = IMG_TOP;
            mImgRect.bottom = mImgRect.top + phoneH;

            float imgWidth = (1 / imgRation) * phoneH;
            mImgRect.left = (phoneW - imgWidth) / 2;
            mImgRect.right = mImgRect.left + imgWidth;
        }

        NoteLog.d(TAG, "img.left=" + mImgRect.left + "  img.right=" + mImgRect.right + "img.top=" + mImgRect.top + "  img.bottom=" + mImgRect.bottom +
                "  phoneH=" + phoneH + "  phoneW=" + phoneW + "  imgH=" + imgH + "  imgW=" + imgW + "  phoneRation=" + phoneRation + "  imgRation=" + imgRation);
        //default border
        mBorderRect = new RectF();
        mBorderRect.left = (mImgRect.left + mImgRect.right - BORDER_DEFAULT_WIDTH) / 2;
        mBorderRect.top = (mImgRect.top + mImgRect.bottom - BORDER_DEFAULT_HEIGHT) / 2;
        mBorderRect.right = mBorderRect.left + BORDER_DEFAULT_WIDTH;
        mBorderRect.bottom = mBorderRect.top + BORDER_DEFAULT_HEIGHT;

        mImgHeightRation = 1.0f * mNormalImg.getHeight() / (mImgRect.bottom - mImgRect.top);
        mImgWidthRation = 1.0f * mNormalImg.getWidth() / (mImgRect.right - mImgRect.left);
        invalidate();
    }

    @NonNull
    private BitmapFactory.Options getBitmapOptions(String imgPath) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        float targetW = getWidth();
        float targetH = getHeight();
        mImgScalSize = (int) Math.min(photoW / targetW, photoH / targetH);
        NoteLog.d(TAG, "photoW=" + photoW + "  photoH=" + photoH + "  targetW=" + targetW + "targetH=" + targetH + "  scaleFactor=" + mImgScalSize);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = mImgScalSize;
        return bmOptions;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mImgName != null) {
            if (isFirst) {
                initImg();
                NoteLog.d(TAG, "load img");
                isFirst = false;
            }
            canvas.drawBitmap(mNormalImg, null, mImgRect, mImgPaint);
            canvas.drawRect(mBorderRect, mBorderPaint);
            drawBackground(canvas);
            drawGuideLine(canvas);
            drawBorderCorner(canvas);
        }
    }

    private void drawBorderCorner(Canvas canvas) {
        drawCorner(mBorderRect.left, mBorderRect.top, canvas);
        drawCorner(mBorderRect.left, mBorderRect.bottom, canvas);
        drawCorner(mBorderRect.right, mBorderRect.top, canvas);
        drawCorner(mBorderRect.right, mBorderRect.bottom, canvas);

    }

    private void drawCorner(float x, float y, Canvas canvas) {
        canvas.drawCircle(x, y, mCornerRadio, mCornerPaint);
    }

    private void drawGuideLine(Canvas canvas) {
        //draw vertical line
        float oneThirdWidth = mBorderRect.width() / 3;
        for (int i = 1; i <= 2; i++) {
            float x = mBorderRect.left + oneThirdWidth * i;
            canvas.drawLine(x, mBorderRect.top, x, mBorderRect.bottom, mGuideLinePaint);
        }

        float oneThirdHeight = mBorderRect.height() / 3;
        for (int i = 1; i <= 2; i++) {
            float y = mBorderRect.top + oneThirdHeight * i;
            canvas.drawLine(mBorderRect.left, y, mBorderRect.right, y, mGuideLinePaint);
        }
    }

    private void drawBackground(Canvas canvas) {
        /*-
           -------------------------------------
           |                top                |
           -------------------------------------
           |      |                    |       |<——————————mBmpBound
           |      |                    |       |
           | left |                    | right |
           |      |                    |       |
           |      |                  <─┼───────┼────mBorderBound
           -------------------------------------
           |              bottom               |
           -------------------------------------
          */

        // Draw "top", "bottom", "left", then "right" quadrants.
        // because the border line width is larger than 1f, in order to draw a complete border rect ,
        // i have to change zhe rect coordinate to draw
        float delta = BORDER_LINE_WIDTH / 2;
        float left = mBorderRect.left - delta;
        float top = mBorderRect.top - delta;
        float right = mBorderRect.right + delta;
        float bottom = mBorderRect.bottom + delta;

        canvas.drawRect(mImgRect.left, mImgRect.top, mImgRect.right, top, mBgPaint);
        canvas.drawRect(mImgRect.left, bottom, mImgRect.right, mImgRect.bottom, mBgPaint);
        canvas.drawRect(mImgRect.left, top, left, bottom, mBgPaint);
        canvas.drawRect(right, top, mImgRect.right, bottom, mBgPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setLastPos(event);
                mTouchPos = detectTouchPosition(event.getX(), event.getY());
                NoteLog.d(TAG, "touch pos is " + mTouchPos);
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(event.getX(), event.getY());
                setLastPos(event);
                break;
        }
        return true;
    }

    private void onTouchMove(float x, float y) {
        float moveX = x - mLastPoint.x;
        float moveY = y - mLastPoint.y;
        switch (mTouchPos) {
            case POS_CENTER:
                float width = mBorderRect.width();
                float height = mBorderRect.height();

                mBorderRect.left += moveX;
                if (mBorderRect.left < mImgRect.left) {
                    mBorderRect.left = mImgRect.left;
                } else if (mBorderRect.left > mImgRect.right - width) {
                    mBorderRect.left = mImgRect.right - width;
                }

                mBorderRect.top += moveY;
                if (mBorderRect.top < mImgRect.top) {
                    mBorderRect.top = mImgRect.top;
                } else if (mBorderRect.top > mImgRect.bottom - height) {
                    mBorderRect.top = mImgRect.bottom - height;
                }

                mBorderRect.right = mBorderRect.left + width;
                mBorderRect.bottom = mBorderRect.top + height;
                break;
            case POS_LEFT:
                setMoveLeft(moveX);
                break;
            case POS_RIGHT:
                setMoveRight(moveX);
                break;
            case POS_TOP:
                setMoveTop(moveY);
                break;
            case POS_BOTTOM:
                setMoveBottom(moveY);
                break;
            case POS_TOP_LEFT:
                setMoveLeft(moveX);
                setMoveTop(moveY);
                break;
            case POS_TOP_RIGHT:
                setMoveRight(moveX);
                setMoveTop(moveY);
                break;
            case POS_BOTTOM_LEFT:
                setMoveLeft(moveX);
                setMoveBottom(moveY);
                break;
            case POS_BOTTOM_RIGHT:
                setMoveRight(moveX);
                setMoveBottom(moveY);
                break;
        }
        invalidate();
    }

    private void setMoveLeft(float x) {
        mBorderRect.left += x;
        if (mBorderRect.left < mImgRect.left) {
            mBorderRect.left = mImgRect.left;
        } else if (mBorderRect.left >= mBorderRect.right) {
            mBorderRect.left = mBorderRect.right - BORDER_MIN_WIDTH;
        }
    }

    private void setMoveRight(float x) {
        mBorderRect.right += x;
        if (mBorderRect.right > mImgRect.right) {
            mBorderRect.right = mImgRect.right;
        } else if (mBorderRect.right <= mBorderRect.left) {
            mBorderRect.right = mBorderRect.left + BORDER_MIN_WIDTH;
        }
    }

    private void setMoveTop(float y) {
        mBorderRect.top += y;
        if (mBorderRect.top < mImgRect.top) {
            mBorderRect.top = mImgRect.top;
        } else if (mBorderRect.top >= mBorderRect.bottom) {
            mBorderRect.top = mBorderRect.bottom - BORDER_MIN_WIDTH;
        }
    }

    private void setMoveBottom(float y) {
        mBorderRect.bottom += y;
        if (mBorderRect.bottom > mImgRect.bottom) {
            mBorderRect.bottom = mImgRect.bottom;
        } else if (mBorderRect.bottom <= mBorderRect.top) {
            mBorderRect.bottom = mBorderRect.top + BORDER_MIN_WIDTH;
        }
    }

    private int detectTouchPosition(float x, float y) {
        float left = mBorderRect.left;
        float right = mBorderRect.right;
        float top = mBorderRect.top;
        float bottom = mBorderRect.bottom;
        //center
        if (x > left + BORDER_TOUCH_WIDTH && x < right - BORDER_TOUCH_WIDTH
                && y > top + BORDER_TOUCH_WIDTH && y < bottom - BORDER_TOUCH_WIDTH) {
            return POS_CENTER;
        }
        if (y > top + BORDER_TOUCH_WIDTH && y < bottom - BORDER_TOUCH_WIDTH) {
            if (x > left - BORDER_TOUCH_WIDTH && x < left + BORDER_TOUCH_WIDTH) {
                return POS_LEFT;
            } else if (x > right - BORDER_TOUCH_WIDTH && x < right + BORDER_TOUCH_WIDTH) {
                return POS_RIGHT;
            }
        }
        if (x > left + BORDER_TOUCH_WIDTH && x < right - BORDER_TOUCH_WIDTH) {
            if (y > top - BORDER_TOUCH_WIDTH && y < top + BORDER_TOUCH_WIDTH) {
                return POS_TOP;
            } else if (y > bottom - BORDER_TOUCH_WIDTH && y < bottom + BORDER_TOUCH_WIDTH) {
                return POS_BOTTOM;
            }
        }
        if (y > top - BORDER_TOUCH_WIDTH && y < top + BORDER_TOUCH_WIDTH) {
            if (x > left - BORDER_TOUCH_WIDTH && x < left + BORDER_TOUCH_WIDTH) {
                return POS_TOP_LEFT;
            } else if (x > right - BORDER_TOUCH_WIDTH && x < right + BORDER_TOUCH_WIDTH) {
                return POS_TOP_RIGHT;
            }
        }
        if (y > bottom - BORDER_TOUCH_WIDTH && y < bottom + BORDER_TOUCH_WIDTH) {
            if (x > left - BORDER_TOUCH_WIDTH && x < left + BORDER_TOUCH_WIDTH) {
                return POS_BOTTOM_LEFT;
            } else if (x > right - BORDER_TOUCH_WIDTH && x < right + BORDER_TOUCH_WIDTH) {
                return POS_BOTTOM_RIGHT;
            }
        }
        return -1;
    }

    private void setLastPos(MotionEvent event) {
        mLastPoint.x = event.getX();
        mLastPoint.y = event.getY();
    }

    public Bitmap getCropBitmap() {
        Bitmap mWholeImg = BitmapFactory.decodeFile(FileUtils.getPicWholePath(mImgName));
        int wholeImgW = mWholeImg.getWidth();
        int wholeImgH = mWholeImg.getHeight();
        int x = (int) ((mBorderRect.left - mImgRect.left) * mImgWidthRation / mNormalImg.getWidth() * wholeImgW);
        int y = (int) ((mBorderRect.top - mImgRect.top) * mImgHeightRation / mNormalImg.getHeight() * wholeImgH);
        int width = (int) (mBorderRect.width() * mImgWidthRation / mNormalImg.getWidth() * wholeImgW);
        int height = (int) (mBorderRect.height() * mImgHeightRation / mNormalImg.getHeight() * wholeImgH);
        if (width == 0) {
            if (wholeImgW - x > 0) {
                width = wholeImgW - x;
            } else {
                x = 0;
                width = wholeImgW;
            }
        }
        if (height == 0) {
            if (wholeImgH - y > 0) {
                height = wholeImgH - y;
            } else {
                y = 0;
                height = wholeImgH;
            }
        }
        NoteLog.d(TAG, "crop img x=" + x + " y=" + y + " width=" + width + " height=" + height + " wholeImgW=" + wholeImgW + " wholeImgH=" + wholeImgH);
        Bitmap result = Bitmap.createBitmap(mWholeImg, x, y, width, height);
        mNormalImg.recycle();
        if (result != mWholeImg) {
            mWholeImg.recycle();
        }
        return result;
    }
}
