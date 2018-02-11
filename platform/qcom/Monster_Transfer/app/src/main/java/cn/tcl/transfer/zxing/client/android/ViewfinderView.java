/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.transfer.zxing.client.android;

import com.google.zxing.ResultPoint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.transfer.R;
import cn.tcl.transfer.zxing.client.android.camera.CameraManager;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 */
public final class ViewfinderView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 10L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;
    private static final int MIDDLE_LINE_WIDTH = 6;
    private static final int MIDDLE_LINE_PADDING = 5;
    private static final int SPEEN_DISTANCE = 5;
    private static final int TEXT_PADDING_TOP = 20;
    private static final int TEXT_PADDING_FIRST_LINE = 11;
    private static final int TEXT_SIZE = 14;

    private CameraManager cameraManager;
    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private final int laserColor;
    private final int resultPointColor;
    private int scannerAlpha;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;
    private int slideTop;
    private int slideBottom;
    private boolean isFirst;
    private static float density;
    private static float scaledDensity;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
        scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
//    maskColor = resources.getColor(R.color.viewfinder_mask);
//    resultColor = resources.getColor(R.color.result_view);
        maskColor = resources.getColor(R.color.encode_view);
        resultColor = resources.getColor(R.color.encode_view);
        laserColor = resources.getColor(R.color.viewfinder_laser);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        Rect frame = cameraManager.getViewFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        if(!isFirst){
            isFirst = true;
            slideTop = frame.top;
            slideBottom = frame.bottom;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(R.drawable.scan))).getBitmap(), null, frame, paint);
            slideTop += SPEEN_DISTANCE;
            if(slideTop >= frame.bottom){
                slideTop = frame.top;
            }
            Rect lineRect = new Rect();
            lineRect.left = frame.left;
            lineRect.right = frame.right;
            lineRect.top = slideTop;
            lineRect.bottom = slideTop + MIDDLE_LINE_WIDTH;
            canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(R.drawable.scan_line))).getBitmap(), null, lineRect, paint);
            paint.setColor(Color.WHITE);
            paint.setTextSize(TEXT_SIZE * scaledDensity);
            paint.setAlpha(0xff);
            String text = getResources().getString(R.string.text_scan_bottom);
            String text_second = getResources().getString(R.string.text_scan_bottom_second);
            float textWidth = paint.measureText(text);
            float textWidth_second = paint.measureText(text_second);
            canvas.drawText(text, (width - textWidth)/2, (frame.bottom + (float)TEXT_SIZE * scaledDensity +(float)TEXT_PADDING_TOP * density), paint);
            canvas.drawText(text_second, (width - textWidth_second)/2, (frame.bottom + (float)TEXT_SIZE * scaledDensity * 2 + (float)(TEXT_PADDING_TOP + TEXT_PADDING_FIRST_LINE) * density), paint);
            // Request another update at the animation interval, but only repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }

}
