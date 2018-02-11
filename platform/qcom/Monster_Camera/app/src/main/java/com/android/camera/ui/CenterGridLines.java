package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

import com.android.camera.debug.Log;
import com.tct.camera.R;

/**
 * GridLines is a view which directly overlays the preview and draws
 * evenly spaced grid lines.
 */
public class CenterGridLines extends View{
    private static final String PHOTO_MODULE_STRING_ID = "CenterGridLines";

    private static final Log.Tag TAG = new Log.Tag(PHOTO_MODULE_STRING_ID);

    private static final int GRIDELINE_SHADOWN_WIGTH = 3;
    Paint mPaint = new Paint();
    Paint mShadowPaint = new Paint();

    public CenterGridLines(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.grid_line_width));
        mPaint.setColor(getResources().getColor(R.color.center_grid_line));
        mShadowPaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.grid_line_width));
        mShadowPaint.setColor(getResources().getColor(R.color.center_grid_shaadow_line));
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw getWidth() : " + getWidth() + " getHeight() : " + getHeight());
            float thirdWidth = getWidth() / 3;
            float thirdHeight = getHeight() / 3;
            for (int i = 1; i < 3; i++) {
                // Draw the vertical lines.
                final float x = thirdWidth * i;
                //public void drawLine(float startX, float startY, float stopX, float stopY,
                canvas.drawLine(x, 0,
                        x, getHeight(), mPaint);
                canvas.drawLine(x + GRIDELINE_SHADOWN_WIGTH, 0,
                        x + GRIDELINE_SHADOWN_WIGTH, getHeight(), mShadowPaint);
                // Draw the horizontal lines.
                final float y = thirdHeight * i;
                canvas.drawLine(0, y,
                        getWidth(),  y, mPaint);
                canvas.drawLine(0, y + GRIDELINE_SHADOWN_WIGTH,
                        getWidth(),  y + GRIDELINE_SHADOWN_WIGTH, mShadowPaint);
            }
        drawDottedLine(canvas);
        drawDottedLineShadow(canvas);
    }

    private void drawDottedLine(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_width));;
        paint.setColor(getResources().getColor(R.color.center_grid_line));
        //DashPathEffect  可以使用DashPathEffect来创建一个虚线的轮廓(短横线/小圆点)，而不是使用实线
        //float[] { 40, 40, 40, 40 }值控制虚线间距，密度
        PathEffect effects = new DashPathEffect(new float[]
                { getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_height),
                        getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_height),
                        getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_height),
                        getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_height)},
                getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow));
        paint.setPathEffect(effects);
        Path path = new Path();
        //top
        //通过moveto，lineto的x，y坐标确定虚线实横，纵，还是倾斜
        path.moveTo(0, 0);//Set the beginning of the next contour to the point (x,y)
        path.lineTo(getWidth(), 0);//Add a line from the last point to the specified point (x,y).
        canvas.drawPath(path, paint);

        //left
        path.moveTo(0, 0);
        path.lineTo(0, getHeight());
        canvas.drawPath(path, paint);

        //bottom
        path.moveTo(0, getHeight());
        path.lineTo(getWidth(), getHeight());
        canvas.drawPath(path, paint);

        //right
        path.moveTo(getWidth() , 0);
        path.lineTo(getWidth(), getHeight());
        canvas.drawPath(path, paint);
    }

    private void drawDottedLineShadow(Canvas canvas) {
        Paint shadowPaint = new Paint();
        shadowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        shadowPaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.grid_line_width));;
        shadowPaint.setColor(getResources().getColor(R.color.center_grid_shaadow_line));
        //DashPathEffect  可以使用DashPathEffect来创建一个虚线的轮廓(短横线/小圆点)，而不是使用实线
        //float[] { 40, 40, 40, 40 }值控制虚线间距，密度
        PathEffect effects = new DashPathEffect(new float[]
                { getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_height),
                        getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_height),
                        getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_height),
                        getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_height)},
                getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow));
        shadowPaint.setPathEffect(effects);
        Path shadowPath = new Path();
        //top shadow
        shadowPath.moveTo(getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow),
                getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow));
        shadowPath.lineTo(getWidth()-getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow),
                getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow));
        canvas.drawPath(shadowPath, shadowPaint);

        //left shadow
        shadowPath.moveTo(getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow),
                getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow));
        shadowPath.lineTo(getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow),
                getHeight()-getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow));
        canvas.drawPath(shadowPath, shadowPaint);

        //bottom shadow
        shadowPath.moveTo(getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow),
                getHeight()-getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow) );
        shadowPath.lineTo(getWidth()-getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow),
                getHeight()-getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow) );
        canvas.drawPath(shadowPath, shadowPaint);

        //right shadow
        shadowPath.moveTo(getWidth()-getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow),
                getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow));
        shadowPath.lineTo(getWidth()-getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow),
                getHeight()-getResources().getDimensionPixelSize(R.dimen.grid_dotted_line_shadow));
        canvas.drawPath(shadowPath, shadowPaint);
    }
}
