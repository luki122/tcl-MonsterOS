package mst.drawable;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.ImageView;



/**
 * Class for play frame animation.
 */
public class MstAnimationDrawable {

	public static class AnimationFrame {
		byte[] bytes;
		int duration;
		Drawable drawable;
		boolean isReady = false;
	}
	private  boolean mOneShot = true;
	private WeakReference<ImageView> mImage;
	private Handler mHandler = new Handler();
	private ActionRunnable mHandlerCallback;
	private boolean mPlay = false;
	private List<MstAnimationDrawable.AnimationFrame> mFrames;
	public MstAnimationDrawable(ImageView imageView,int resourceId){
		mImage = new WeakReference<ImageView>(imageView);
		loadFromXml(resourceId, imageView.getContext(), null);
	}

	public interface OnDrawableLoadedListener {
		public void onDrawableLoaded(List<AnimationFrame> myFrames);
	}

	/**
	 * Start play frame animation in target ImageView.
	 */
	public  void start() {

		final ImageView imageView = mImage.get();
		if(imageView == null){
			return;
		}
		mPlay = true;
		animateRawManually(mFrames, imageView, null);
	}

	private  void loadRaw(final int resourceId, final Context context,
						  final OnDrawableLoadedListener onDrawableLoadedListener) {
		loadFromXml(resourceId, context, onDrawableLoadedListener);
	}

	public List<MstAnimationDrawable.AnimationFrame> getFrames(){
		return mFrames;
	}

	public boolean isOneShot(){
		return mOneShot;
	}
	private  void loadFromXml(final int resourceId,
							  final Context context,
							  final OnDrawableLoadedListener onDrawableLoadedListener) {
		final ArrayList<AnimationFrame> myFrames = new ArrayList<AnimationFrame>();

		XmlResourceParser parser = context.getResources().getXml(
				resourceId);

		try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {

				} else if (eventType == XmlPullParser.START_TAG) {
					if (parser.getName().equals("item")) {
						byte[] bytes = null;
						int duration = 1000;

						for (int i = 0; i < parser.getAttributeCount(); i++) {
							if (parser.getAttributeName(i).equals(
									"drawable")) {
								int resId = Integer.parseInt(parser
										.getAttributeValue(i)
										.substring(1));
								bytes = toByteArray(context.getResources()
										.openRawResource(resId));
							} else if (parser.getAttributeName(i)
									.equals("duration")) {
								duration = parser.getAttributeIntValue(
										i, 1000);
							}
						}

						AnimationFrame myFrame = new AnimationFrame();
						myFrame.bytes = bytes;
						myFrame.duration = duration;
						myFrames.add(myFrame);
					} else if ("animation-list".equals(parser.getName())) {
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							if ("oneshot".equals(parser.getAttributeName(i))) {
								mOneShot = parser.getAttributeBooleanValue(i, true);
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

		mFrames = myFrames;
	}

	private  void animateRawManually(List<AnimationFrame> myFrames,
									 ImageView imageView, Runnable onComplete) {
		if(!mPlay){
			return;
		}

		animateRawManually(myFrames, imageView, onComplete, 0);
	}

	private  void animateRawManually(final List<AnimationFrame> myFrames,
									 final ImageView imageView, final Runnable onComplete,
									 final int frameNumber) {
		final AnimationFrame thisFrame = myFrames.get(frameNumber);

		if (frameNumber == 0) {
			thisFrame.drawable = new BitmapDrawable(imageView.getContext()
					.getResources(), BitmapFactory.decodeByteArray(
					thisFrame.bytes, 0, thisFrame.bytes.length));
		} else {
			AnimationFrame previousFrame = myFrames.get(frameNumber - 1);
			Bitmap bitmap = ((BitmapDrawable) previousFrame.drawable).getBitmap();
			if(bitmap != null){
				bitmap.recycle();
			}
			previousFrame.drawable = null;
			previousFrame.isReady = false;
		}

		imageView.setImageDrawable(thisFrame.drawable);
		if(mHandlerCallback != null) {
			mHandler.removeCallbacks(mHandlerCallback);
			mHandlerCallback = null;
		}
		mHandlerCallback = new ActionRunnable(imageView,this);
		mHandlerCallback.setFrame(thisFrame);
		mHandlerCallback.setFrameNumber(frameNumber);

		mHandler.postDelayed(mHandlerCallback, thisFrame.duration);

		// Load next frame
		if (frameNumber + 1 < myFrames.size()) {
			AnimationFrame nextFrame = myFrames.get(frameNumber + 1);
			nextFrame.drawable = new BitmapDrawable(imageView
					.getContext().getResources(), BitmapFactory.decodeByteArray(nextFrame.bytes, 0,
					nextFrame.bytes.length)
			);
			if (nextFrame.isReady) {
				// Animate next frame
				animateRawManually(myFrames, imageView, onComplete,
						frameNumber + 1);
			} else {
				nextFrame.isReady = true;
			}
		}
	}


	public  byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return output.toByteArray();
	}

	public  int copy(InputStream input, OutputStream output)
			throws IOException {
		long count = copyLarge(input, output);
		if (count > 2147483647L) {
			return -1;
		}
		return (int) count;
	}

	public  long copyLarge(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[4096];
		long count = 0L;
		int n = 0;
		try {
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
				count += n;
			}
		}finally {
			if(input != null){
				input.close();
			}
			if(output != null){
				output.close();
			}
		}
		return count;
	}


	public void recycle(){
		mPlay = false;
		if(mHandlerCallback != null){
			mHandler.removeCallbacks(mHandlerCallback);
		}
		if(mImage.get() != null){
			mImage.get().setBackground(null);
		}
		mImage.clear();
		System.gc();
	}


	private static class ActionRunnable implements Runnable{
		WeakReference<ImageView> mImage;
		MstAnimationDrawable.AnimationFrame thisFrame;
		private int frameNumber;
		private MstAnimationDrawable mDrawable;
		public ActionRunnable(ImageView image,MstAnimationDrawable drawable){
			mImage = new WeakReference<ImageView>(image);
			mDrawable = drawable;
		}

		public void setFrame(MstAnimationDrawable.AnimationFrame thisFrame){
			this.thisFrame = thisFrame;
		}

		public void setFrameNumber(int number){
			this.frameNumber = number;
		}


		@Override
		public void run() {
			ImageView imageView = mImage.get();
			if(imageView == null){
				return;
			}
			// Make sure ImageView hasn't been changed to a different Image
			// in this time
			if (imageView.getDrawable() == thisFrame.drawable) {
				if (frameNumber + 1 < mDrawable.getFrames().size()) {
					AnimationFrame nextFrame = mDrawable.getFrames().get(frameNumber + 1);

					if (nextFrame.isReady) {
						// Animate next frame
						mDrawable.animateRawManually(mDrawable.getFrames(), imageView, null,
								frameNumber + 1);
					} else {
						nextFrame.isReady = true;
					}
				} else {
					if (!mDrawable.isOneShot()) {
						mDrawable.animateRawManually(mDrawable.getFrames(), imageView, null, 0);
					} else {
//                        if (onComplete != null) {
//                            onComplete.run();
//                        }
					}

				}
			}
		}
	}

}
