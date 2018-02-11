package com.mst.floatview;

import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.widget.LinearLayout.LayoutParams;

import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.ArrayList;

import javax.crypto.NullCipher;

import com.android.incallui.Call;
import com.android.incallui.CallList;
import com.android.incallui.CallerInfo;
import com.android.incallui.ContactInfoCache;
import com.android.incallui.InCallPresenter;
import com.android.incallui.TelecomAdapter;
import com.android.incallui.ContactInfoCache.ContactCacheEntry;
import com.android.incallui.ContactInfoCache.ContactInfoCacheCallback;
import com.android.incallui.InCallApp;
import com.mst.MstPhoneUtils;
import com.mst.manager.AntiTouchManager;
import com.mst.utils.SubUtils;

import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.view.GestureDetector.OnGestureListener;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.CompoundButton;
import android.provider.ContactsContract.Contacts;
import android.content.ContentUris;
import android.graphics.drawable.Drawable;
import android.graphics.BitmapFactory;
import android.view.animation.*;
import android.animation.*;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.telecom.VideoProfile;
import com.android.incallui.R;
import com.android.dialer.util.TelecomUtil;
import com.mst.tms.MarkResult;

public class MstFloatView extends LinearLayout implements
		View.OnClickListener, OnGestureListener {

	private static final String LOG_TAG = "MstFloatView";

	private static final boolean DBG = true;
	/**
	 * 记录小悬浮窗的宽度
	 */
	public static int viewWidth;

	/**
	 * 记录小悬浮窗的高度
	 */
	public static int viewHeight;

	/**
	 * 记录系统状态栏的高度
	 */
	private static int statusBarHeight;

	/**
	 * 用于更新小悬浮窗的位置
	 */
	private WindowManager windowManager;

	/**
	 * 小悬浮窗的参数
	 */
	private WindowManager.LayoutParams mParams;

	/**
	 * 记录当前手指位置在屏幕上的横坐标值
	 */
	private float xInScreen;

	/**
	 * 记录当前手指位置在屏幕上的纵坐标值
	 */
	private float yInScreen;

	/**
	 * 记录手指按下时在屏幕上的横坐标的值
	 */
	private float xDownInScreen;

	/**
	 * 记录手指按下时在屏幕上的纵坐标的值
	 */
	private float yDownInScreen;

	/**
	 * 记录手指按下时在小悬浮窗的View上的横坐标的值
	 */
	private float xInView;

	/**
	 * 记录手指按下时在小悬浮窗的View上的纵坐标的值
	 */
	private float yInView;

	private TextView mName, mNumber, mArea, mMark;
	private ImageButton mHangup, mAnswer;
	private ImageView mPhoto;
	private GestureDetector gDetector;
	private Context mContext;
	private View mMain;
	private ImageView mSimIcon;

	public MstFloatView(Context context) {
		super(context);
		mContext = context;
		gDetector = new GestureDetector(this);
		windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		LayoutInflater.from(context).inflate(R.layout.incoming_pop, this);
		View view = findViewById(R.id.parent_container);
		viewWidth = view.getLayoutParams().width;
		viewHeight = view.getLayoutParams().height;
		mName = (TextView) findViewById(R.id.name);
		mNumber = (TextView) findViewById(R.id.number);
		mArea = (TextView) findViewById(R.id.area);
		mMark = (TextView) findViewById(R.id.mark);
		mHangup = (ImageButton) findViewById(R.id.hangup);
		mHangup.setOnClickListener(this);
		mAnswer = (ImageButton) findViewById(R.id.answer);
		mAnswer.setOnClickListener(this);
		mPhoto = (ImageView) findViewById(R.id.photo_image);
		mSimIcon = (ImageView) findViewById(R.id.sim_icon);
		mMain = findViewById(R.id.main_content);
		mMain.setOnClickListener(this);
		mDensity = context.getResources().getDisplayMetrics().density;
		updateUI();
		this.setOnKeyListener(null);
		this.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gDetector.onTouchEvent(event);
			}
		});
	}

	public void onClick(View view) {
		int id = view.getId();
		log("onClick(View " + view + ", id " + id + ")...");
    	if (!AntiTouchManager.mIsTouchEnable) {
    		return;
    	} 

		switch (id) {
		case R.id.hangup:
			hangup();
			break;
		case R.id.answer:
			// internalAnswerCall();
			answer();
			break;
		case R.id.main_content:
			InCallApp.getInstance().displayCallScreen();
			break;
		}
		FloatWindowManager.removeWindow(InCallApp.getInstance());
	}

	private void answer() {
		log("answer()...");
		Call call = CallList.getInstance().getIncomingCall();
		if(call == null) {
			return;
		}
		TelecomAdapter.getInstance().answerCall(call.getId(),
				VideoProfile.STATE_AUDIO_ONLY);
//		mHandler.postDelayed(new Runnable(){
//			public void run() {
//				InCallApp.getInstance().displayCallScreen();
//			}
//		}, 1500);
		InCallApp.getInstance().displayCallScreen();
	}

	private void hangup() {
		log("hangup()...");
		Call call = CallList.getInstance().getIncomingCall();
		if(call == null) {
			return;
		}
		TelecomAdapter.getInstance().rejectCall(call.getId(), false, null);
	}

	private void log(String msg) {
		Log.d(LOG_TAG, msg);
	}

	/**
	 * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
	 * 
	 * @param params
	 *            小悬浮窗的参数
	 */
	public void setParams(WindowManager.LayoutParams params) {
		mParams = params;
	}

	/**
	 * 更新小悬浮窗在屏幕中的位置。
	 */
	private void updateViewPosition() {
		mParams.x = (int) (xInScreen - xInView);
		mParams.y = (int) (yInScreen - yInView);
		windowManager.updateViewLayout(this, mParams);
	}

	/**
	 * 用于获取状态栏的高度。
	 * 
	 * @return 返回状态栏高度的像素值。
	 */
	private int getStatusBarHeight() {
		if (statusBarHeight == 0) {
			try {
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field field = c.getField("status_bar_height");
				int x = (Integer) field.get(o);
				statusBarHeight = getResources().getDimensionPixelSize(x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusBarHeight;
	}

	private ContactCacheEntry mContactCacheEntry;

	private void queryInfo() {
		Call call = CallList.getInstance().getIncomingCall();
		if(call == null) {
			FloatWindowManager.removeWindow(InCallApp.getInstance());
			return;
		}
		final ContactInfoCache cache = ContactInfoCache.getInstance(mContext);
		cache.findInfo(call, true, new ContactLookupCallback());
	}

	private class ContactLookupCallback implements ContactInfoCacheCallback {

		public ContactLookupCallback() {
		}

		@Override
		public void onContactInfoComplete(String callId, ContactCacheEntry entry) {
			mContactCacheEntry = entry;
			updateUiInternal();
		}

		@Override
		public void onImageLoadComplete(String callId, ContactCacheEntry entry) {
			mContactCacheEntry = entry;
			updateUiInternal();
		}

        @Override
        public void onContactInteractionsInfoComplete(String callId,
                ContactCacheEntry entry) {
            // TODO Auto-generated method stub
            mContactCacheEntry = entry;
            updateUiInternal();
        }

	}

	void updateUI() {
		mHandler.post(new Runnable(){
			public void run() {
				queryInfo();	
			}
		});
	}

	private void updateUiInternal() {

		log("updateUI start");
		Call call = CallList.getInstance().getIncomingCall();
		if (call != null && mContactCacheEntry != null) {		    
		    
			if (!TextUtils.isEmpty(mContactCacheEntry.namePrimary)) {
				mName.setText(mContactCacheEntry.namePrimary);
				mNumber.setText(mContactCacheEntry.number);
				mNumber.setVisibility(View.VISIBLE);
			} else {	
                mName.setText(mContactCacheEntry.number);
                mNumber.setText("");
                mNumber.setVisibility(View.GONE);	            
			}						
			

			if (!TextUtils.isEmpty(mContactCacheEntry.area)) {
				mArea.setText(mContactCacheEntry.area);
				mArea.setVisibility(View.VISIBLE);				
			} else {
				mArea.setVisibility(View.GONE);				
			}
				         
            MarkResult mark = mContactCacheEntry.mark;
            String markName = "";
            int count = -2;
            if (mark != null) {
                markName = mark.getName();
                count = mark.getTagCount();
            }
			
           if (!TextUtils.isEmpty(markName) && TextUtils.isEmpty(mContactCacheEntry.namePrimary)) {
               String countString = "";
              if(count == -1) {
                   countString = getResources().getString(R.string.mark_by_user);
               } else if(count  >= 0) {
                   countString = getResources().getString(R.string.mark_count, count);
               }
               mMark.setText(markName + " " + countString);
               mMark.setVisibility(View.VISIBLE);
           } else {
               mMark.setText("");
               mMark.setVisibility(View.GONE);
           }

			int slot = SubUtils.getSlotBySubId(call.getSubId());
			if (SubUtils.isValidPhoneId(slot) && SubUtils.isDoubleCardInsert()) {
				mSimIcon.setImageResource(slot > 0 ? R.drawable.sim_icon_2
						: R.drawable.sim_icon_1);
				mSimIcon.setVisibility(View.VISIBLE);
			} else {
				mSimIcon.setVisibility(View.GONE);
			}

			if (mContactCacheEntry.photo != null && mContactCacheEntry.displayPhotoUri != null) {
				mPhoto.setImageDrawable(MstPhoneUtils
						.getDrawableForBitmap(mContactCacheEntry.photo));
				mPhoto.setVisibility(View.VISIBLE);
			} else {
				mPhoto.setVisibility(View.GONE);
			}
		}

		log("updateUI end");

	}

	private Handler mHandler = new Handler();

	private float mDensity = 3.0f;

	/**
	 * Notified when a tap occurs with the down {@link MotionEvent} that
	 * triggered it. This will be triggered immediately for every down event.
	 * All other events should be preceded by this.
	 * 
	 * @param e
	 *            The down motion event.
	 */
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * The user has performed a down {@link MotionEvent} and not performed a
	 * move or up yet. This event is commonly used to provide visual feedback to
	 * the user to let them know that their action has been recognized i.e.
	 * highlight an element.
	 * 
	 * @param e
	 *            The down motion event
	 */
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	/**
	 * Notified when a tap occurs with the up {@link MotionEvent} that triggered
	 * it.
	 * 
	 * @param e
	 *            The up motion event that completed the first tap
	 * @return true if the event is consumed, else false
	 */
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Notified when a scroll occurs with the initial on down
	 * {@link MotionEvent} and the current move {@link MotionEvent}. The
	 * distance in x and y is also supplied for convenience.
	 * 
	 * @param e1
	 *            The first down motion event that started the scrolling.
	 * @param e2
	 *            The move motion event that triggered the current onScroll.
	 * @param distanceX
	 *            The distance along the X axis that has been scrolled since the
	 *            last call to onScroll. This is NOT the distance betweengetContext
	 *            {@code e1} and {@code e2}.
	 * @param distanceY
	 *            The distance along the Y axis that has been scrolled since the
	 *            last call to onScroll. This is NOT the distance between
	 *            {@code e1} and {@code e2}.
	 * @return true if the event is consumed, else false
	 */
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		Log.v("SlideView", "onScroll");
		float y1 = e1.getY();
		float y2 = e2.getY();

		if (Math.abs(y1 - y2) > 50 * mDensity) {
			FloatWindowManager.removeWindow(InCallApp.getInstance());
			// CallNotifier notifier = PhoneGlobals.getInstance().notifier;
			// notifier.silenceRinger();
			// PhoneGlobals.getInstance().notificationMgr.updateInCallNotification();
		    TelecomUtil.silenceRinger(mContext);
			return true;
		}
		return false;
	}

	/**
	 * Notified when a long press occurs with the initial on down
	 * {@link MotionEvent} that trigged it.
	 * 
	 * @param e
	 *            The initial on down motion event that started the longpress.
	 */
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	/**
	 * Notified of a fling event when it occurs with the initial on down
	 * {@link MotionEvent} and the matching up {@link MotionEvent}. The
	 * calculated velocity is supplied along the x and y axis in pixels per
	 * second.
	 * 
	 * @param e1
	 *            The first down motion event that started the fling.
	 * @param e2
	 *            The move motion event that triggered the current onFling.
	 * @param velocityX
	 *            The velocity of this fling measured in pixels per second along
	 *            the x axis.
	 * @param velocityY
	 *            The velocity of this fling measured in pixels per second along
	 *            the y axis.
	 * @return true if the event is consumed, else false
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onFling");
		return false;
	}
	
	
	private ScreenBroadcastReceiver mScreenReceiver = new ScreenBroadcastReceiver();
	 @Override  
	    protected void onAttachedToWindow() {  
	        super.onAttachedToWindow();  
	        IntentFilter filter = new IntentFilter();
	        filter.addAction(Intent.ACTION_SCREEN_ON);
	        mContext.registerReceiver(mScreenReceiver, filter);
	    }
	    
	    @Override  
	    protected void onDetachedFromWindow() {  
	        super.onDetachedFromWindow();  
	        mContext.unregisterReceiver(mScreenReceiver);
	    }
	
	private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
    			InCallApp.getInstance().displayCallScreen();
    			FloatWindowManager.removeWindow(InCallApp.getInstance());
            }
        }
    }

}