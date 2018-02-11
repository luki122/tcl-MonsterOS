package mst.widget;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class FoldProgressBar extends SurfaceView implements
		SurfaceHolder.Callback {

	private SurfaceHolder mHolder;
	private AnimationThread mAnimationThread;
	private OnAnimationListener mListener;

	public interface OnAnimationListener {
		public void onAnimation(int currentFrame);

		public void onStop(int currentFrame);

	}

	private static class Frame {
		int frameResId;
		int duration;
	}

	public FoldProgressBar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public FoldProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public FoldProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		init();
	}

	public FoldProgressBar(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
		init();
	}

	public void setAnimationListener(OnAnimationListener listener) {
		mListener = listener;
	}

	private void init() {
		mHolder = getHolder();
		mHolder.addCallback(this);
		setZOrderOnTop(true);
		mHolder.setFormat(PixelFormat.TRANSLUCENT);
		mAnimationThread = new AnimationThread(mHolder, getContext());

	}

	@Override
	public synchronized void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

		mAnimationThread.cancel();
		mAnimationThread.play();
		mAnimationThread.start();
	}

	@Override
	public synchronized void surfaceChanged(SurfaceHolder holder, int format,
			int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mAnimationThread.cancel();
		try {
			mAnimationThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// mAnimationThread = null;
		System.gc();
		mAnimationThread.mFrames = null;
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

        switch (widthMode) {
        case MeasureSpec.AT_MOST:
        	width = getFrameWidth();
            break;
       
        }
        switch (heightMode) {
		case MeasureSpec.AT_MOST:
			height = getFrameHeight();
			break;
		}
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, widthMode), MeasureSpec.makeMeasureSpec(height, heightMode));
	}
	
	private int getFrameWidth(){
		return getResources().getDrawable(com.mst.R.drawable.fold_progress_light, 
				getContext().getTheme()).getIntrinsicWidth();
	}
	
	private int getFrameHeight(){
		return getResources().getDrawable(com.mst.R.drawable.fold_progress_light, 
				getContext().getTheme()).getIntrinsicHeight();
	}

	public void stop() {
		mAnimationThread.cancel();
	}

	private static class AnimationThread extends Thread {
		SurfaceHolder holder;
		WeakReference<Context> context;
		volatile boolean isRunning;
		Paint paint;
		private int mCurrentFrame = -1;
		private Frame[] mFrames;
		private boolean mOneSort = true;

		public AnimationThread(SurfaceHolder surfaceHolder, Context context) {
			// TODO Auto-generated constructor stub
			this.holder = surfaceHolder;
			this.context = new WeakReference<Context>(context);
			isRunning = false;
			paint = new Paint();
			paint.setAntiAlias(true);
		}

		public synchronized void cancel() {
			isRunning = false;
		}

		public synchronized boolean isRunning() {
			return isRunning;
		}

		public synchronized void play() {
			isRunning = true;
			mCurrentFrame = 0;
		}

		private void loadFromXml(final int resourceId) {
			final ArrayList<Frame> myFrames = new ArrayList<Frame>();

			XmlResourceParser parser = context.get().getResources()
					.getXml(resourceId);

			try {
				int eventType = parser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_DOCUMENT) {

					} else if (eventType == XmlPullParser.START_TAG) {
						if (parser.getName().equals("item")) {
							int duration = 50;
							int resId = 0;
							for (int i = 0; i < parser.getAttributeCount(); i++) {
								if (parser.getAttributeName(i).equals(
										"drawable")) {
									resId = Integer.parseInt(parser
											.getAttributeValue(i).substring(1));
								} else if (parser.getAttributeName(i).equals(
										"duration")) {
									duration = parser.getAttributeIntValue(i,
											50);
								}
							}

							Frame myFrame = new Frame();
							myFrame.duration = duration;
							myFrame.frameResId = resId;
							myFrames.add(myFrame);
						} else if ("animation-list".equals(parser.getName())) {
							for (int i = 0; i < parser.getAttributeCount(); i++) {
								if ("oneshot"
										.equals(parser.getAttributeName(i))) {
									mOneSort = parser.getAttributeBooleanValue(
											i, true);
								}
							}
						}

					} else if (eventType == XmlPullParser.END_TAG) {

					} else if (eventType == XmlPullParser.TEXT) {

					}

					eventType = parser.next();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}

			mFrames = new Frame[myFrames.size()];
			for (int i = 0; i < myFrames.size(); i++) {
				mFrames[i] = myFrames.get(i);
			}
		}

		@Override
		public void run() {
			Canvas c = null;
			Date date = null;
			Frame frame = null;
			Bitmap frameBitmap = null;
			mFrames = null;
			loadFromXml(com.mst.R.drawable.fold_progress_light);
			while (isRunning) {
				try {
					synchronized (holder) {
						date = new Date();
						c = holder.lockCanvas();
						frame = mFrames[mCurrentFrame];
						int resId = frame.frameResId;
						if (resId == 0) {
							continue;
						}
						frameBitmap = BitmapFactory.decodeResource(context
								.get().getResources(), resId);
						if (c == null) {
							continue;
						}
						c.drawColor(Color.TRANSPARENT, Mode.CLEAR);
						if (frame != null && frameBitmap != null) {
							c.drawBitmap(frameBitmap, 0, 0, paint);
						}
						mCurrentFrame++;
						if (mCurrentFrame == mFrames.length) {
							mCurrentFrame = 0;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (c != null) {
						holder.unlockCanvasAndPost(c);
					}
					if (frameBitmap != null) {
						frameBitmap.recycle();
						frameBitmap = null;
					}
				}

				try {
					Thread.sleep(Math.max(0,frame.duration - (new Date().getTime() - date.getTime())));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					frame = null;
				}

			}
		}

	}

}
