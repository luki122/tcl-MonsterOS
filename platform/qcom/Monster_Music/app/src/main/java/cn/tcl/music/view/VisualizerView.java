package cn.tcl.music.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;
import cn.tcl.music.R;
import cn.tcl.music.util.LogUtil;

public class VisualizerView extends View {
    private static final String TAG = "VisualizerView";
    private static final int LEVELVERSION = 5;
    private static final int HIDEHEIGHT = 15;
    private static final int SCOLLERLEVEL = 30;
    private byte[] mBytes;
    private byte[] mFFTBytes;
    private Rect mRect = new Rect();
    private Visualizer mVisualizer;
    protected float[] mFFTPoints;
    private Resources mRes;

    public VisualizerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VisualizerView(Context context) {
        this(context, null, 0);
    }

    private void init() {
        mFFTBytes = null;
        mRes = getResources();
    }

    /**
     * Links the visualizer to a player
     *
     * @param player
     *            - MediaPlayer instance to link to
     */
    public void link(MediaPlayer player) {
        if (player == null) {
            return;
        }
        // Set previous Visualizer station unable
        if (mVisualizer != null) {
            mVisualizer.setEnabled(false);
        }
        try {
            mVisualizer = new Visualizer(player.getAudioSessionId());
            // Make sure this Visualizer station unable
            mVisualizer.setEnabled(false);
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            // Pass through Visualizer data to VisualizerView
            Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    updateVisualizer(bytes);
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    updateVisualizerFFT(bytes);
                }
            };

            mVisualizer.setDataCaptureListener(captureListener, Visualizer.getMaxCaptureRate() / 2, false, true);
            // Enabled Visualizer and disable when we're done with the stream
            mVisualizer.setEnabled(true);
        } catch (Exception e) {
            LogUtil.d(TAG, "set visualizer view Excetpion: " + e.getMessage());
        }
    }

    public void releaseView() {
        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }
    }
    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }

    public void updateVisualizerFFT(byte[] bytes) {
        mFFTBytes = bytes;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Create canvas once we're ready to draw
        mRect.set(0, 0, getWidth(), getHeight());

        if (mFFTBytes != null) {
            if (mFFTPoints == null || mFFTPoints.length < mFFTBytes.length * 4) {
                mFFTPoints = new float[mFFTBytes.length * 4];
            }
            Paint paint = new Paint();
            paint.setStrokeWidth(mRes.getDimension(R.dimen.dp_2));
            paint.setAntiAlias(true);
            paint.setColor(Color.argb(128, 255, 255, 255));
            for (int i = 0; i < mFFTBytes.length / LEVELVERSION; i++) {
                mFFTPoints[i * 4] = i * 4 * LEVELVERSION;
                mFFTPoints[i * 4 + 2] = i * 4 * LEVELVERSION;
                byte rfk = mFFTBytes[LEVELVERSION * i];
                byte ifk = mFFTBytes[LEVELVERSION * i + 1];
                float magnitude = (rfk * rfk + ifk * ifk);
                int dbValue = (int) (SCOLLERLEVEL * Math.log10(magnitude));

                mFFTPoints[i * 4 + 1] = mRect.height();
                mFFTPoints[i * 4 + 3] = mRect.height() - (dbValue * 2 - HIDEHEIGHT);
            }

            canvas.drawLines(mFFTPoints, paint);
        }
    }
}
