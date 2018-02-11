package cn.tcl.music.view.lyric;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.util.LogUtil;

/**
 * LrcView can display LRC file and Seek it.
 *
 * @author
 */
public class LrcView extends View implements ILrcView {


    public final static String TAG = LrcView.class.getSimpleName();

    /**
     * normal display mode
     */
    public final static int DISPLAY_MODE_NORMAL = 0;
    /**
     * seek display mode
     */
    public final static int DISPLAY_MODE_SEEK = 1;
    /**
     * scale display mode ,scale font size
     */
    public final static int DISPLAY_MODE_SCALE = 2;

    private List<LrcRow> mLrcRows;    // all lrc rows of one lrc file
    private int mMinSeekFiredOffset = 10; // min offset for fire seek action, px;
    private int mHignlightRow = 0;   // current singing row , should be highlighted.


    private int mSeekLineTextSize = 15;
    private int mMinSeekLineTextSize = 13;
    private int mMaxSeekLineTextSize = 18;
    private int mSeekLinePaddingX = 0; // Seek line padding x


    private int mDisplayMode = DISPLAY_MODE_NORMAL;
    private LrcViewListener mLrcViewListener;

    private String mLoadingLrcTip;

    private Paint paintAP; //画笔，用于渐隐
    private Paint mPaint;//画笔，用于画不是高亮的歌词
    private Paint paintHL;    //画笔，用于画高亮的歌词，即当前唱到这句歌词

    private static final int DEFAULT_NORMAL_TEXTVIEW_COLOR = Color.BLACK;
    private static final int DEFAULT_HL_TEXTVIEW_COLOR = Color.RED;
    private int normaltextSize;    //非当前行的字体大小
    private int hltextSize;        //当前行的字体大小
    private int normalTextColor;   //非当前行的颜色
    private int hlTextColor;       //当前行的颜色
    private int mLiricMargin = 48;         //歌词行与行之间的间距
    private int mLrcFontSize = 35;    // 歌词大小
    private int mScreenWidth;
    private int mScreenHeight;

    public LrcView(Context context, AttributeSet attr) {
        super(context, attr);
        TypedArray a = context.obtainStyledAttributes(attr, R.styleable.Lyric);
        normaltextSize = a.getDimensionPixelSize(R.styleable.Lyric_normaltextSize, 13);
        mLrcFontSize = normaltextSize;
        hltextSize = a.getDimensionPixelSize(R.styleable.Lyric_hltextSize, 15);
        normalTextColor = a.getColor(R.styleable.Lyric_normalTextColor, DEFAULT_NORMAL_TEXTVIEW_COLOR);
        hlTextColor = a.getColor(R.styleable.Lyric_hlTextColor, DEFAULT_HL_TEXTVIEW_COLOR);
        mLiricMargin = a.getDimensionPixelSize(R.styleable.Lyric_liricMargin, 12);
        a.recycle();
        init();
    }


    public void init() {
        paintAP = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paintAP.setTextAlign(Paint.Align.CENTER);
        paintAP.setColor(normalTextColor);
        paintAP.setTextSize(normaltextSize);
        paintAP.setAlpha(76);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setColor(normalTextColor);
        mPaint.setTextSize(normaltextSize);
        mPaint.setAlpha(76);

        paintHL = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paintHL.setTextAlign(Paint.Align.CENTER);
        paintHL.setColor(hlTextColor);
        paintHL.setTextSize(hltextSize);
        paintHL.setAlpha(217);

        mLoadingLrcTip = getResources().getString(R.string.liric_not_exist);
    }


    public void setListener(LrcViewListener l) {
        mLrcViewListener = l;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mScreenWidth = w;
        mScreenHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mLrcRows == null || mLrcRows.size() == 0) {
            if (mLoadingLrcTip != null) {
                // 没有歌词的时候
                canvas.drawText(mLoadingLrcTip, mScreenWidth / 2, mScreenHeight / 2 - mLrcFontSize, mPaint);
            }
            return;
        }
        if (mHignlightRow < 0 || mHignlightRow >= mLrcRows.size()) {
            return;
        }

        int rowY = 0; // vertical point of each row.
        final int rowX = mScreenWidth / 2;
        int rowNum = 0;
        boolean isSingleLine = true;
        int hlLiricMargin = 8; //当前行比正常行多出的高度

        // 1, draw highlight row at center.
        // 2, draw rows above highlight row.
        // 3, draw rows below highlight row.

        // 1 highlight row
        LrcRow highLightRow = mLrcRows.get(mHignlightRow);
        int highlightRowY = mScreenHeight / 2 - mLrcFontSize;

        if (highLightRow != null) {
            String highlightText = highLightRow.content;
            isSingleLine = true;
            if (highlightText != null && highlightText.getBytes().length / 2 >= mScreenWidth / mLrcFontSize) {
                isSingleLine = false;
                String[] tempText = splitLrc(highlightText);
                canvas.drawText(tempText[0], rowX, highlightRowY, paintHL);
                highlightRowY += mLiricMargin + mLrcFontSize;
                highlightText = tempText[1];
            }
            canvas.drawText(highlightText, rowX, highlightRowY, paintHL);
//			Log.d("xin", "1 highlightRowY : " + highlightRowY);
        }

        if (mDisplayMode == DISPLAY_MODE_SEEK) {
        }

        // 2 above rows
        rowNum = mHignlightRow - 1;
        if (!isSingleLine) {
            highlightRowY -= mLiricMargin + mLrcFontSize;
        }
        rowY = highlightRowY - mLiricMargin - mLrcFontSize - hlLiricMargin;

        for (int i = rowNum; i >= 0; i--) {

            if (rowNum < mLrcRows.size()) {
                LrcRow lrcRow = mLrcRows.get(rowNum);

                if (lrcRow != null) {
                    String aboveText = lrcRow.content;
                    if (aboveText != null && aboveText.getBytes().length / 2 >= mScreenWidth / mLrcFontSize) {
                        String[] tempText = splitLrc(aboveText);
                        if (rowY < mLiricMargin + mLrcFontSize * 2) {
                            canvas.drawText(tempText[1], rowX, rowY, paintAP);
                        } else {
                            canvas.drawText(tempText[1], rowX, rowY, mPaint);
                        }
                        rowY -= mLiricMargin + mLrcFontSize; // MODIFIED by beibei.yang, 2016-05-24,BUG-2004933
                        aboveText = tempText[0];
                    }
                    if (rowY < mLiricMargin + mLrcFontSize * 2) {
                        canvas.drawText(aboveText, rowX, rowY, paintAP);
                    } else {
                        canvas.drawText(aboveText, rowX, rowY, mPaint);
                    }
                }
            }
            rowY -= (mLiricMargin + mLrcFontSize);
            rowNum--;
            if (rowY - mLrcFontSize < 0) {
                break;
            }
        }
        // 3 below rows
        rowNum = mHignlightRow + 1;
        if (!isSingleLine) {
            highlightRowY += mLiricMargin + mLrcFontSize;
        }
        rowY = highlightRowY + mLiricMargin + mLrcFontSize + hlLiricMargin;
        if (mLrcRows != null && mLrcRows.size() != 0) {
            for (int i = rowNum; i < mLrcRows.size(); i++) {
                if (rowNum < mLrcRows.size()) {
                    LrcRow lrcRow = mLrcRows.get(rowNum);
                    if (lrcRow != null) {
                        String belowText = lrcRow.content;
                        if (belowText != null && belowText.getBytes().length / 2 >= mScreenWidth / mLrcFontSize) {
                            String[] tempText = splitLrc(belowText);
                            if (rowY >= mScreenHeight - mLiricMargin - mLrcFontSize) {
                                canvas.drawText(tempText[0], rowX, rowY, paintAP);
                            } else {
                                canvas.drawText(tempText[0], rowX, rowY, mPaint);
                            }
                            rowY += (mLiricMargin + mLrcFontSize); // MODIFIED by beibei.yang, 2016-05-24,BUG-2004933
                            belowText = tempText[1];
                        }
                        if (rowY >= mScreenHeight - mLiricMargin - mLrcFontSize) {
                            canvas.drawText(belowText, rowX, rowY, paintAP);
                        } else {
                            canvas.drawText(belowText, rowX, rowY, mPaint);
                        }
                    }
                }
                rowY += (mLiricMargin + mLrcFontSize);
                rowNum++;
                if (rowY >= mScreenHeight) {
                    break;
                }
            }
        }

    }

    public void seekLrc(int position) {
        if (mLrcRows == null || position < 0 || position > mLrcRows.size()) {
            return;
        }
        LrcRow lrcRow = mLrcRows.get(position);
        mHignlightRow = position;
        invalidate();
        if (mLrcViewListener != null) {
            mLrcViewListener.onLrcSeeked(position, lrcRow);
        }
    }

    private float mLastMotionY;
    private PointF mPointerOneLastMotion = new PointF();
    private PointF mPointerTwoLastMotion = new PointF();
    private boolean mIsFirstMove = false; // whether is first move , some events can't not detected in touch down,
    // such as two pointer touch, so it's good place to detect it in first move

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mLrcRows == null || mLrcRows.size() == 0) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LogUtil.d(TAG, "down,mLastMotionY:" + mLastMotionY);
                mLastMotionY = event.getY();
                mIsFirstMove = true;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:

                if (event.getPointerCount() == 2) {
                    doScale(event);
                    return true;
                }
                // single pointer mode ,seek
                if (mDisplayMode == DISPLAY_MODE_SCALE) {
                    //if scaling but pointer become not two ,do nothing.
                    return true;
                }

                doSeek(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mDisplayMode == DISPLAY_MODE_SEEK) {
                    seekLrc(mHignlightRow);
                }
                mDisplayMode = DISPLAY_MODE_NORMAL;
                invalidate();
                break;
        }
        return true;
    }

    private void doScale(MotionEvent event) {
        if (mDisplayMode == DISPLAY_MODE_SEEK) {
            // if Seeking but pointer become two, become to scale mode
            mDisplayMode = DISPLAY_MODE_SCALE;
            LogUtil.d(TAG, "two move but teaking ...change mode");
            return;
        }
        // two pointer mode , scale font
        if (mIsFirstMove) {
            mDisplayMode = DISPLAY_MODE_SCALE;
            invalidate();
            mIsFirstMove = false;
            setTwoPointerLocation(event);
        }
        int scaleSize = getScale(event);
        LogUtil.d(TAG, "scaleSize:" + scaleSize);
        if (scaleSize != 0) {
            setNewFontSize(scaleSize);
            invalidate();
        }
        setTwoPointerLocation(event);
    }

    private void doSeek(MotionEvent event) {
        float y = event.getY();
        float offsetY = y - mLastMotionY; // touch offset.
        if (Math.abs(offsetY) < mMinSeekFiredOffset) {
            // move to short ,do not fire seek action
            return;
        }
        mDisplayMode = DISPLAY_MODE_SEEK;
        int rowOffset = Math.abs((int) offsetY / mLrcFontSize); // highlight row offset.
        LogUtil.d(TAG, "move new hightlightrow : " + mHignlightRow + " offsetY: " + offsetY + " rowOffset:" + rowOffset);
        if (offsetY < 0) {
            // finger move up
            mHignlightRow += rowOffset;
        } else if (offsetY > 0) {
            // finger move down
            mHignlightRow -= rowOffset;
        }
        mHignlightRow = Math.max(0, mHignlightRow);
        mHignlightRow = Math.min(mHignlightRow, mLrcRows.size() - 1);

        if (rowOffset > 0) {
            mLastMotionY = y;
            invalidate();
        }
    }

    private void setTwoPointerLocation(MotionEvent event) {
        mPointerOneLastMotion.x = event.getX(0);
        mPointerOneLastMotion.y = event.getY(0);
        mPointerTwoLastMotion.x = event.getX(1);
        mPointerTwoLastMotion.y = event.getY(1);
    }

    private void setNewFontSize(int scaleSize) {
        mLrcFontSize += scaleSize;
        mSeekLineTextSize += scaleSize;
        mSeekLineTextSize = Math.max(mSeekLineTextSize, mMinSeekLineTextSize);
        mSeekLineTextSize = Math.min(mSeekLineTextSize, mMaxSeekLineTextSize);
    }

    // get font scale offset
    private int getScale(MotionEvent event) {
        float x0 = event.getX(0);
        float y0 = event.getY(0);
        float x1 = event.getX(1);
        float y1 = event.getY(1);
        float maxOffset = 0; // max offset between x or y axis,used to decide scale size

        boolean zoomin = false;

        float oldXOffset = Math.abs(mPointerOneLastMotion.x - mPointerTwoLastMotion.x);
        float newXoffset = Math.abs(x1 - x0);

        float oldYOffset = Math.abs(mPointerOneLastMotion.y - mPointerTwoLastMotion.y);
        float newYoffset = Math.abs(y1 - y0);

        maxOffset = Math.max(Math.abs(newXoffset - oldXOffset), Math.abs(newYoffset - oldYOffset));
        if (maxOffset == Math.abs(newXoffset - oldXOffset)) {
            zoomin = newXoffset > oldXOffset ? true : false;
        } else {
            zoomin = newYoffset > oldYOffset ? true : false;
        }

        LogUtil.d(TAG, "scaleSize maxOffset:" + maxOffset);

        if (zoomin) {
            return (int) (maxOffset / 10);
        } else {
            return -(int) (maxOffset / 10);
        }
    }

    public void setLrc(List<LrcRow> lrcRows) {
        mLrcRows = lrcRows;
        postInvalidate();
    }

    //[BUGFIX]-MODIFY by yanjia.li, 2016-06-22,BUG-2384922 begin
    public void setLrc(List<LrcRow> lrcRows, int HignlightRow) {
        mHignlightRow = HignlightRow;
        mLrcRows = lrcRows;
        postInvalidate();
    }
    //[BUGFIX]-MODIFY by yanjia.li, 2016-06-22,BUG-2384922 end


    public void seekLrcToTime(long time) {
        if (mLrcRows == null || mLrcRows.size() == 0) {
            return;
        }

        if (mDisplayMode != DISPLAY_MODE_NORMAL) {
            return;
        }
        // find row
        for (int i = 0; i < mLrcRows.size(); i++) {
            LrcRow current = mLrcRows.get(i);
            LrcRow next = i + 1 == mLrcRows.size() ? null : mLrcRows.get(i + 1);

            if ((time >= current.time && next != null && time < next.time)
                    || (time > current.time && next == null)) {
                seekLrc(i);
                return;
            }
        }
    }

    private String[] splitLrc(String lrc) {
        int firstLineSize = lrc.length() * 2 / 3;
        if (lrc.length() * 2 / 3 > mScreenWidth) {
            firstLineSize = lrc.length() / 2;
        }
        String[] temp = new String[2];
        temp[0] = lrc.substring(0, firstLineSize);
        temp[1] = lrc.substring(firstLineSize, lrc.length());
        return temp;
    }
}