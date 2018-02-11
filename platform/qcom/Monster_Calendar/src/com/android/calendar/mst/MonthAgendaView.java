package com.android.calendar.mst;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract.Attendees;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.Event;
import com.android.calendar.EventLoader;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.month.MonthByWeekAdapter;
import com.android.calendar.month.MonthByWeekFragment;
import com.android.calendar.month.MonthWeekEventsView;
import com.android.calendar.mst.utils.CalendarUtils;
import com.android.calendar.mst.utils.LunarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonthAgendaView extends LinearLayout implements MonthAgendaListView.OnTouchEventHandleListener {

	private static final String TAG = "MonthAgendaView";
	private static final String DEBUG_TAG = "MonthDay";

	private static int sViewId = 0;
	private int mViewId = -1;

	private static final int DEFAULT_WEEK_COUNT = 6;
	private static final int DAYS_PER_WEEK = 7;

	private Context mContext;
	private ViewSwitcher mViewSwitcher = null;
	private MonthByWeekFragment mParentFragment = null;

	private View mCreateEventView;
	private BaseAdapter mAdapter = null;
	private LayoutInflater mLayoutInflater = null;
	private MonthWeekEventsView[] mWeekViews = null;
	private MonthWeekEventsView mTappedView = null;

	private Time mBaseDate = new Time();
	private int mFirstMonthJulianDay = -1;
	private int mLastMonthJulianDay = -1;

	private int mLastSecondaryDayInFirstWeek = -1;
	private int mFirstSecondaryDayInLastWeek = -1;

	private boolean mHasToday = false;
	private int mTodayJulianDay = -1;
	private boolean mIsTodaySecondary = false;

	private Time mSelectedDate = new Time();
	private Time mPreviousSelectedDate = new Time();
	private int mSelectedJulianDay = -1;

	private int mHighlightWeekIndex = -1;
	private int mHighlightWeekDayIndex = -1;
	private int mActualWeekCount = DEFAULT_WEEK_COUNT;

	private int mPrevHighlightWeekIndex = -2;
	private int mPrevHighlightWeekDayIndex = -2;

	private void resetPrevHighlightIndex() {
		mPrevHighlightWeekIndex = -2;
		mPrevHighlightWeekDayIndex = -2;
	}

	private void resetHighlightIndex() {
		mHighlightWeekIndex = -1;
		mHighlightWeekDayIndex = -1;
	}

	private boolean mShowWeekNum = false;

	private static final int BAR_HEIGHT = 48 + 33; // 55(TOP BAR) + 27(MONTH HEADER) + 48(BOTTOM BAR)
	private static final int FLOATING_VIEW_HALF_HEIGHT = 75;

	private float mScale = 1.0f;
	private float mScaledDensity = 1.0f;

	private int mViewPadding = 0;
	private int mViewWidth = 0;
	private int mViewHeight = 0;

	private int mWeekViewWidth = 0;
	private int mWeekViewHeight = 0;
	private int mWeekViewLeft = 0;
	private int mWeekViewTop = 0;

	private static final int TOUCH_MODE_UNKNOWN = 0;
	private static final int TOUCH_MODE_DOWN = 0x01;
	private static final int TOUCH_MODE_HSCROLL = 0x02;
	private static final int TOUCH_MODE_VSCROLL = 0x04;

	private static final float SCROLL_SENSITIVITY = 1.7f;

	private static final float MIN_VSCROLL_DISTANCE = 37.0f/*60.0f*/;
	private static final float MIN_HSCROLL_VELOCITY = 1300.0f;

	private static final long DURATION_MONTH_SWITCHING = 450;
	private static final Interpolator DEFAULT_INTERPOLATER = new AccelerateDecelerateInterpolator();

	private int mTouchMode = TOUCH_MODE_UNKNOWN;
    private boolean mIsWeekviewMode = false;
    private boolean staticMove = false;
    private static final long DURATION_WEEK_SWITCH = 100;

	private GestureDetector mGestureDetector = null;

	private boolean mIsNewScrolling = false;
	private float mSumScrollX = 0;
	private float mSumScrollY = 0;
	private float mFirstTouchedX = 0;
	private float mFirstTouchedY = 0;

	private EventLoader mEventLoader = null;
	private ArrayList<Event> mEvents = new ArrayList<Event>();
	private ArrayList<Event> mAdapterEvents = new ArrayList<Event>();
    private ArrayList<Event> mBirthdayReminderEvents = new ArrayList<Event>();

	private MonthAgendaAdapter mMonthAgendaAdapter = null;

	private static final int MESSAGE_EVENTS_LOADED = 1;

	private Handler mEventLoaderHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case MESSAGE_EVENTS_LOADED:
					updateEventsListView();
					break;
				default:
					break;
			}
            super.handleMessage(msg);
		}
	};

	private boolean isTodaySelected() {
		if (mSelectedDate == null) return false;

		int selectedJulianDay = Time.getJulianDay(mSelectedDate.toMillis(true), mSelectedDate.gmtoff);

		Time now = new Time();
		now.setToNow();
		now.normalize(true);
		int todayJulianDay = Time.getJulianDay(now.toMillis(true), now.gmtoff);

		return todayJulianDay == selectedJulianDay;
	}

	private void updateEventsListView() {
		Log.d(TAG, "updateEventsListView()");

        if (mAgendaList == null) return;

        ArrayList<Event> events = new ArrayList<Event>();
        events.addAll(mEvents);

        mAdapterEvents.clear();
        mBirthdayReminderEvents.clear();
        for (Event event : events) {
            if (Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME.equals(event.ownerAccount)) {
                mBirthdayReminderEvents.add(event);
            } else {
                mAdapterEvents.add(event);
            }
        }

        boolean doReminderBirthday = Utils.getSharedPreference(mContext,
                CalendarSettingsActivity.KEY_BIRTHDAY_REMINDER, true);
        if (!mBirthdayReminderEvents.isEmpty() && !doReminderBirthday) {
            mBirthdayReminderEvents.clear();
        }

        Log.d(TAG, "mAdapterEvents size = " + mAdapterEvents.size());
		Log.d(TAG, "mEvents size = " + mEvents.size());
		Log.d(TAG, "events size = " + events.size());

        /*if (!mBirthdayReminderEvents.isEmpty()) {
            mAgendaList.setAdapter(null);
            mAgendaList.removeHeaderView(mBirthdayReminderView);
            mAgendaList.addHeaderView(mBirthdayReminderView);

            CharSequence name = mBirthdayReminderEvents.get(0).title;
            if (mBirthdayReminderEvents.size() == 1) {
                mBirthdayReminderContentView.setText(mContext.getString(R.string.birthday_only_one, name));
            } else {
                mBirthdayReminderContentView.setText(mContext.getString(R.string.birthday_have_other, name,
                        mBirthdayReminderEvents.size()));
            }
        } else {
            mAgendaList.removeHeaderView(mBirthdayReminderView);
        }*/

		if (mMonthAgendaAdapter == null) {
			mMonthAgendaAdapter = new MonthAgendaAdapter(mContext, mParentFragment,
					mBirthdayReminderEvents, mAdapterEvents);
		}

		if (mAgendaList.getAdapter() == mMonthAgendaAdapter) {
			mMonthAgendaAdapter.notifyDataSetChanged();
		} else {
			mAgendaList.setAdapter(mMonthAgendaAdapter);
		}

		hideAgendaList(events.isEmpty());
	}

	private Runnable mEventsLoadingFinishedCallback = new Runnable() {
		@Override
		public void run() {
			mEventLoaderHandler.sendEmptyMessage(MESSAGE_EVENTS_LOADED);
		}
	};

	private Runnable mEventsLoadingCanceledCallback = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "events query canceled");
		}
	};

	private static final long DEFAULT_EVENT_ID = -1;

	private void updateActionBarTime(Time targetDate) {
		if (targetDate == null || targetDate.year < 1970 || targetDate.year > 2036) {
			return;
		}

        Log.d(TAG, "2 - update time " + CalendarUtils.printDate(targetDate));

        CalendarController controller = CalendarController.getInstance(mContext);
        controller.sendEvent(mContext, EventType.UPDATE_TITLE, null, null, targetDate,
				DEFAULT_EVENT_ID, ViewType.CURRENT, 0, null, null);
	}

	private Time getTimeOfController() {
		CalendarController controller = CalendarController.getInstance(mContext);

		Time time = new Time();
		time.set(controller.getTime());
		time.normalize(true);

		return time;
	}

	private void setTimeOfController(long millis) {
		CalendarController controller = CalendarController.getInstance(mContext);
		controller.setTime(millis);
	}

	public MonthAgendaView(Context context) {
		super(context);
		initMonthView(context);
	}

	public MonthAgendaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMonthView(context);
	}

	public MonthAgendaView(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
		initMonthView(context);
	}

	private class MonthGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent event) {
			mTouchMode = TOUCH_MODE_DOWN;
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event) {
			selectMonthDay(event.getX(0), event.getY(0));
			return true;
		}

		@Override
		public void onShowPress(MotionEvent event) {
			selectMonthDay(event.getX(0), event.getY(0));
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float deltaX, float deltaY) {
			if (mIsNewScrolling) {
				mSumScrollX = 0;
				mSumScrollY = 0;
				mIsNewScrolling = false;
			}
			mSumScrollX += deltaX;
			mSumScrollY += deltaY;

			if (mTouchMode == TOUCH_MODE_DOWN) {
				float absSumScrollX = Math.abs(mSumScrollX);
				float absSumScrollY = Math.abs(mSumScrollY);

				if (absSumScrollX * SCROLL_SENSITIVITY > absSumScrollY) {
					if (absSumScrollX > MIN_VSCROLL_DISTANCE * mScale) {
                        // if (e1.getY() < mActualWeekCount * mWeekViewHeight) {
                        if ((e1.getY() < mActualWeekCount * mWeekViewHeight && !mIsWeekviewMode) ||
                                (e1.getY() < mWeekViewHeight + 37 * mScale && mIsWeekviewMode)) {
							mTouchMode = TOUCH_MODE_HSCROLL;
						}
					}
				} else if (absSumScrollY > MIN_VSCROLL_DISTANCE * mScale) {
					mTouchMode = TOUCH_MODE_VSCROLL;
				}
			} else if (mTouchMode == TOUCH_MODE_HSCROLL) {
                // handleHScroll();
                if (!mIsWeekviewMode) {
                    handleHScroll();
                } else {
                    handleHorizontalScroll();
                }
            } else if (mTouchMode == TOUCH_MODE_VSCROLL) {
                handleVerticalScroll();
			}

			return true;
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		switch(action) {
			case MotionEvent.ACTION_CANCEL:
				return false;
			case MotionEvent.ACTION_DOWN:
				mIsNewScrolling = true;
				mGestureDetector.onTouchEvent(event);
				return false;
			/*case MotionEvent.ACTION_MOVE:
				mGestureDetector.onTouchEvent(event);
				return false;*/
			case MotionEvent.ACTION_UP:
				mIsNewScrolling = false;
				mGestureDetector.onTouchEvent(event);
				return false;
			default:
				mGestureDetector.onTouchEvent(event);
				return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		switch(action) {
			case MotionEvent.ACTION_CANCEL:
				return true;
			case MotionEvent.ACTION_DOWN:
				return true;
			case MotionEvent.ACTION_MOVE:
				mGestureDetector.onTouchEvent(event);
				return true;
			case MotionEvent.ACTION_UP:
				return true;
			default:
				mGestureDetector.onTouchEvent(event);
				return true;
		}
	}

    public void setInOutAnimation() {
        setInOutAnimation(true, DEFAULT_INTERPOLATER, 0);
    }

	private void setInOutAnimation(boolean gotoFuture, Interpolator interpolator, long duration) {
		float inFromXValue, inToXValue;
		float outFromXValue, outToXValue;
		float progress = 0;

		if (gotoFuture) {
			inFromXValue = 1.0f - progress;
            inToXValue = 0.0f;
            outFromXValue = -progress;
            outToXValue = -1.0f;
		} else {
			inFromXValue = progress - 1.0f;
            inToXValue = 0.0f;
            outFromXValue = progress;
            outToXValue = 1.0f;
		}

		TranslateAnimation inAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, inFromXValue,
                Animation.RELATIVE_TO_SELF, inToXValue,
                Animation.ABSOLUTE, 0.0f,
                Animation.ABSOLUTE, 0.0f);

        TranslateAnimation outAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, outFromXValue,
                Animation.RELATIVE_TO_SELF, outToXValue,
                Animation.ABSOLUTE, 0.0f,
                Animation.ABSOLUTE, 0.0f);

        if (staticMove) {
            staticMove = false;
            duration = 0;
        }

		if (duration < 0) {
			duration = 0;
		}
        inAnimation.setDuration(duration);
        outAnimation.setDuration(duration);

        if (interpolator != null) {
            inAnimation.setInterpolator(interpolator);
            outAnimation.setInterpolator(interpolator);
        }

        inAnimation.setAnimationListener(new AnimationListener() {
        	@Override
        	public void onAnimationEnd(Animation animation) {
        		MonthAgendaView currentView = (MonthAgendaView) mViewSwitcher.getCurrentView();

        		Log.d(DEBUG_TAG, "onAnimationEnd() current view date is " +
        				CalendarUtils.printDate(currentView.getSelectedDate()));
        		mParentFragment.onMonthViewSwitched(currentView.getSelectedDate());
        	}

        	@Override
        	public void onAnimationRepeat(Animation animation) {
        		
        	}

        	@Override
        	public void onAnimationStart(Animation animation) {
                MonthAgendaView currentView = (MonthAgendaView) mViewSwitcher.getCurrentView();
                currentView.initMonthView();
        	}
        });

        mViewSwitcher.setInAnimation(inAnimation);
        mViewSwitcher.setOutAnimation(outAnimation);
	}

	private void handleHScroll() {
		mIsNewScrolling = false;
		mTouchMode = TOUCH_MODE_UNKNOWN;

		Time temp = getTimeOfController();

		boolean gotoNextMonth = (mSumScrollX > 0) ? true : false;
		setInOutAnimation(gotoNextMonth, DEFAULT_INTERPOLATER, DURATION_MONTH_SWITCHING);

		if (gotoNextMonth) {
			temp.month += 1;
		} else {
			temp.month -= 1;
		}
		temp.monthDay = 1;
		temp.normalize(true);

		Log.d(DEBUG_TAG, "handleHScroll() goto date " + CalendarUtils.printDate(temp));

		if (temp.year < CalendarUtils.MIN_YEAR_NUM || temp.year > CalendarUtils.MAX_YEAR_NUM) {
			return;
		}

		MonthAgendaView nextView = (MonthAgendaView) mViewSwitcher.getNextView();
		nextView.setParams(mViewSwitcher, mAdapter, mParentFragment, temp, mEventLoader, mCreateEventView);
		nextView.setRootSize(mRootWidth, mRootHeight);
		nextView.setWeekviewMode(mIsWeekviewMode);
		mViewSwitcher.showNext();
		nextView.highlightToday();
	}

    private void handleHorizontalScroll() {
        mIsNewScrolling = false;
        mTouchMode = TOUCH_MODE_UNKNOWN;

        int selectJulianDay = 0;
        boolean leftScroll = (mSumScrollX > 0) ? true : false;

        if (leftScroll && mHighlightWeekIndex < 5) {
            selectJulianDay = mWeekViews[mHighlightWeekIndex + 1].getFirstJulianDay();
        } else if (leftScroll && mHighlightWeekIndex == 5) {
            int secondaryDayIndex = mWeekViews[mHighlightWeekIndex].getSecondaryDayIndex();
            selectJulianDay = mLastMonthJulianDay - secondaryDayIndex + 8;
        } else if (!leftScroll && mHighlightWeekIndex > 0) {
            selectJulianDay = mWeekViews[mHighlightWeekIndex - 1].getFirstJulianDay();
        } else if (!leftScroll && mHighlightWeekIndex == 0) {
            int secondaryDayIndex = mWeekViews[mHighlightWeekIndex].getSecondaryDayIndex();
            selectJulianDay = mFirstMonthJulianDay - secondaryDayIndex - 8;
        }

        Time selectTime = new Time();
        selectTime.setJulianDay(selectJulianDay);
        selectTime.normalize(true);

        if (selectJulianDay >= mFirstMonthJulianDay && selectJulianDay <= mLastMonthJulianDay) {
            final Time time = selectTime;
        	final MonthWeekEventsView currentWeekView = mWeekViews[mHighlightWeekIndex];
            ObjectAnimator outAnimator = ObjectAnimator.ofFloat(currentWeekView, "x",
                    (leftScroll ? -mViewWidth : mViewWidth));
            outAnimator.setDuration(450);
            outAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    gotoDate(time);
                    ObjectAnimator backAnimator = ObjectAnimator.ofFloat(currentWeekView, "x", 0);
                    backAnimator.setDuration(0);
                    backAnimator.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    
                }
            });

            MonthWeekEventsView nextWeekView = mWeekViews[mHighlightWeekIndex + (leftScroll ? 1 : -1)];

            ObjectAnimator xAnimator = ObjectAnimator.ofFloat(nextWeekView, "x",
                    (leftScroll ? mViewWidth : -mViewHeight));
            xAnimator.setDuration(0);

            ObjectAnimator yAnimator = ObjectAnimator.ofFloat(nextWeekView, "y", 0);
            yAnimator.setDuration(0);

            AnimatorSet initAnimator = new AnimatorSet();
            initAnimator.play(xAnimator).after(yAnimator);

            ObjectAnimator inAnimator = ObjectAnimator.ofFloat(nextWeekView, "x", 0);
            if (leftScroll) {
            	inAnimator.setDuration(450);
            } else {
            	inAnimator.setDuration(400);
            }

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(initAnimator).before(inAnimator);
            animatorSet.play(inAnimator).with(outAnimator);
            animatorSet.start();
        } else {
            gotoDate(selectTime);
        }
    }

    private void handleVerticalScroll() {
        mIsNewScrolling = false;
        mTouchMode = TOUCH_MODE_UNKNOWN;

        if (mHighlightWeekIndex < 0) {
            highlightMonthDate(mSelectedDate);
            if (mHighlightWeekIndex < 0) mHighlightWeekIndex = 0;
        }

        boolean upScroll = (mSumScrollY > 0) ? true : false;
        if (upScroll && !mIsWeekviewMode) {
            mIsWeekviewMode = true;
            for (int i = 0; i < mActualWeekCount; i ++) {
                View weekView = mWeekViews[i];
                ObjectAnimator animator = ObjectAnimator.ofFloat(weekView, "y",
                        (i - mHighlightWeekIndex) * mWeekViewHeight);
                animator.setDuration(mHighlightWeekIndex * DURATION_WEEK_SWITCH);
                animator.start();
            }

            /*ObjectAnimator createEventAnimator = ObjectAnimator.ofFloat(mCreateEventView, "y",
                    mWeekViewHeight + FLOATING_VIEW_HALF_HEIGHT);
            createEventAnimator.setDuration((mActualWeekCount - 1) * DURATION_WEEK_SWITCH);
            createEventAnimator.start();*/

            ObjectAnimator animator = ObjectAnimator.ofFloat(mAgendaField, "y", mWeekViewHeight);
            animator.setDuration((mActualWeekCount - 1) * DURATION_WEEK_SWITCH);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // resetAgendaField();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    updateWeekViews();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    
                }
            });
            animator.start();
        } else if (!upScroll && mIsWeekviewMode) {
            mIsWeekviewMode = false;
            for (int i = 0; i < mActualWeekCount; i++) {
                View weekView = mWeekViews[i];
                ObjectAnimator animator = ObjectAnimator.ofFloat(weekView, "y", i * mWeekViewHeight);
                animator.setDuration(mHighlightWeekIndex * DURATION_WEEK_SWITCH);
                animator.setStartDelay((mActualWeekCount - mHighlightWeekIndex - 1) * DURATION_WEEK_SWITCH); 
                animator.start();
            }

            /*ObjectAnimator createEventAnimator = ObjectAnimator.ofFloat(mCreateEventView, "y",
                    mActualWeekCount * mWeekViewHeight + FLOATING_VIEW_HALF_HEIGHT);
            createEventAnimator.setDuration((mActualWeekCount - 1) * DURATION_WEEK_SWITCH);
            createEventAnimator.start();*/

            ObjectAnimator animator = ObjectAnimator.ofFloat(mAgendaField, "y",
                    mActualWeekCount * mWeekViewHeight);
            animator.setDuration((mActualWeekCount - 1) * DURATION_WEEK_SWITCH);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    updateWeekViews();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // resetAgendaField();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    
                }
            });
            animator.start();
        }
    }

    private void resetAgendaField() {
        int showWeekCount = mIsWeekviewMode ? 1 : mActualWeekCount;
        int wantedHeight = (mRootHeight - mWeekViewHeight * showWeekCount - dp2px(BAR_HEIGHT));
        int agendaFieldWidth = getMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, mViewWidth);
        int agendaFieldHeight = getMeasureSpec(wantedHeight, 0);
        mAgendaField.measure(agendaFieldWidth, agendaFieldHeight);

        int left = 0;
        int right = mViewWidth;
        int top = mWeekViewHeight * mActualWeekCount;
        int bottom = mViewHeight +  mWeekViewHeight * (mIsWeekviewMode ? mActualWeekCount - 1 : 0);
        mAgendaField.layout(left, top, right, bottom);
    }

    public void setWeekviewMode(boolean weekviewMode) {
        mIsWeekviewMode = weekviewMode;
    }

    public boolean getWeekviewMode() {
        return mIsWeekviewMode;
    }

    private void initMonthView() {
        if (mHighlightWeekIndex < 0) {
            highlightMonthDate(mSelectedDate);
            if (mHighlightWeekIndex < 0) mHighlightWeekIndex = 0;
        }

        if (mIsWeekviewMode) {
            for (int i = 0; i < mActualWeekCount; i ++) {
                MonthWeekEventsView weekView = mWeekViews[i];
                ObjectAnimator animator = ObjectAnimator.ofFloat(weekView, "y",
                        (i - mHighlightWeekIndex) * mWeekViewHeight);
                animator.setDuration(0);
                animator.start();
            }

            /*ObjectAnimator createEventAnimator = ObjectAnimator.ofFloat(mCreateEventView, "y",
                    mWeekViewHeight + FLOATING_VIEW_HALF_HEIGHT);
            createEventAnimator.setDuration(0);
            createEventAnimator.start();*/

            ObjectAnimator animator = ObjectAnimator.ofFloat(mAgendaField, "y", mWeekViewHeight);
            animator.setDuration(0);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // resetAgendaField();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    updateWeekViews();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    
                }
            });
            animator.start();
        } else {
            for (int i = 0; i < mActualWeekCount; i++) {
            	MonthWeekEventsView weekView = mWeekViews[i];
                ObjectAnimator animator = ObjectAnimator.ofFloat(weekView, "y", i * mWeekViewHeight);
                animator.setDuration(0);
                animator.start();
            }

            /*ObjectAnimator createEventAnimator = ObjectAnimator.ofFloat(mCreateEventView, "y",
                    mActualWeekCount * mWeekViewHeight + FLOATING_VIEW_HALF_HEIGHT);
            createEventAnimator.setDuration(0);
            createEventAnimator.start();*/

            ObjectAnimator animator = ObjectAnimator.ofFloat(mAgendaField, "y",
                    mActualWeekCount * mWeekViewHeight);
            animator.setDuration(0);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // resetAgendaField();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    
                }
            });
            animator.start();
        }
    }

    private void updateWeekViews() {
    	/*for (int i = 0; i < mActualWeekCount; i++) {
    		MonthWeekEventsView weekView = mWeekViews[i];
            weekView.setWeekMode(mIsWeekviewMode);
            weekView.invalidate();
        }*/

        if (mHighlightWeekIndex < 0 || mHighlightWeekIndex >= mWeekViews.length) return;

        MonthWeekEventsView weekView = mWeekViews[mHighlightWeekIndex];
        weekView.setWeekMode(mIsWeekviewMode);
        weekView.invalidate();
    }

    public MonthWeekEventsView getFirstWeekView() {
        return mWeekViews == null ? null : mWeekViews[0];
    }

	private boolean switchMonth(Time targetDate) {
		int action = CalendarUtils.compareMonth(targetDate, mSelectedDate);

		boolean donotMove = (action == 0);
		if (donotMove) {
			if (!CalendarUtils.isYearInRange(targetDate)) {
				return false;
			}

			MonthAgendaView currView = (MonthAgendaView) mViewSwitcher.getCurrentView();
			currView.highlightMonthDate(targetDate);
		} else {
			if (!CalendarUtils.isYearInRange(targetDate)) {
				return false;
			}

			boolean gotoFuture = (action > 0);
			setInOutAnimation(gotoFuture, DEFAULT_INTERPOLATER, DURATION_MONTH_SWITCHING);

			MonthAgendaView nextView = (MonthAgendaView) mViewSwitcher.getNextView();
			nextView.setParams(mViewSwitcher, mAdapter, 
					mParentFragment, targetDate, mEventLoader, mCreateEventView);
			nextView.setRootSize(mRootWidth, mRootHeight);
			nextView.setWeekviewMode(mIsWeekviewMode);
			mViewSwitcher.showNext();
			nextView.highlightMonthDate(targetDate);
		}

		return !donotMove;
	}

	private void initMonthView(Context context) {
		mViewId = (++sViewId) & 0x1;

		mContext = context;
		mWeekViews = new MonthWeekEventsView[DEFAULT_WEEK_COUNT];

		mSelectedDate = new Time();

        Resources res = context.getResources();
		mScale = res.getDisplayMetrics().density;
		mScaledDensity = res.getDisplayMetrics().scaledDensity;
	}

	public void setMonthWeekAdapter(BaseAdapter adapter) {
		mAdapter = adapter;

		for (int i = 0; i < mActualWeekCount; ++i) {
			((MonthByWeekAdapter) mAdapter).sendEventsToView(mWeekViews[i]);
			mWeekViews[i].invalidate();
		}
	}

	public void setParams(ViewSwitcher switcher, BaseAdapter adapter, MonthByWeekFragment fragment,
			Time selectedDay, EventLoader eventLoader, View createEventView) {

		mViewSwitcher = switcher;
		mAdapter = adapter;
		mParentFragment = fragment;
		mEventLoader = eventLoader;
		mCreateEventView = createEventView;

		if (mGestureDetector == null) {
			mGestureDetector = new GestureDetector(mContext, new MonthGestureDetector());
		}

        if (listGestureDetector == null) {
            listGestureDetector = new GestureDetector(mContext, new ListGestureListenr());
        }

		setBaseDate(selectedDay);
		mSelectedDate.set(selectedDay);
		long millis = mSelectedDate.normalize(true);
		mSelectedJulianDay = Time.getJulianDay(millis, mSelectedDate.gmtoff);

		Log.d(DEBUG_TAG, "update mSelectedDate as " + CalendarUtils.printTime(mSelectedDate));

		initAgendaLayout();

		int weekNum = getWeekNumOfFirstMonthDay(mSelectedDate);
		addWeekViewsByWeekNum(weekNum);

		setSecondaryDayIndex();
		setAgendaListListener(eventLoader);

		resetHighlightIndex();
		resetPrevHighlightIndex();
	}

	private void gotoCreateEvent() {
        CalendarController controller = CalendarController.getInstance(mContext);

        Time t = new Time();
        t.set(controller.getTime());

        Time temp = new Time();
        temp.set(controller.getTime());

        Time now = new Time();
        now.setToNow();
        t.hour = now.hour;
        t.minute = now.minute;
        temp.hour = now.hour;
        temp.minute = now.minute;

        t.second = 0;
        if (t.minute > 30) {
            t.hour++;
            t.minute = 0;
        } else if (t.minute > 0 && t.minute < 30) {
            t.minute = 30;
        }

        controller.sendEventRelatedEvent(
                this, EventType.CREATE_EVENT, -1, t.toMillis(true), 0, 0, 0, temp.toMillis(true));
	}

	private void initAgendaLayout() {
		if (mAgendaField == null) {
			if (mLayoutInflater == null) {
				mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}

			mAgendaField = (ViewGroup) mLayoutInflater.inflate(R.layout.mst_month_agenda_layout, null);
            mNoAgendaPrompt = (TextView) mAgendaField.findViewById(R.id.no_agenda_prompt);

            mAgendaList = (MonthAgendaListView) mAgendaField.findViewById(R.id.month_agenda_list);
            mAgendaList.setDivider(null);
            mAgendaList.setOnTouchEventHandleListener(this);

            View listFooter = mLayoutInflater.inflate(R.layout.mst_month_agenda_footer, null);
            mAgendaList.addFooterView(listFooter);

            /*mBirthdayReminderView = (View) mLayoutInflater.inflate(R.layout.mst_month_birthday_reminder, null);
            mBirthdayReminderContentView = (TextView) mBirthdayReminderView.findViewById(
                    R.id.birthday_reminder_content);
            mBirthdayReminderWishView = (ImageView) mBirthdayReminderView.findViewById(
                    R.id.birthday_reminder_wish);
            mBirthdayReminderWishView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.happy_birthday));
                    intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.happy_birthday));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.wish_birthday)));
                }
            });*/

			mLayoutInflater = null;
		}
	}

    public boolean onTouchEventHandle(MotionEvent event) {
        int action = event.getActionMasked();
        switch(action) {
            case MotionEvent.ACTION_CANCEL:
                return false;
            case MotionEvent.ACTION_DOWN:
                mIsNewScrolling = true;
                listGestureDetector.onTouchEvent(event);
                return false;
            case MotionEvent.ACTION_MOVE:
                return listGestureDetector.onTouchEvent(event);
            case MotionEvent.ACTION_UP:
                mIsNewScrolling = false;

                if (reachListTop && !singleToTop) {
                    singleToTop = true;
                } else {
                    singleToTop = false;
                }
                reachListTop = false;

                return false;
            default:
            	listGestureDetector.onTouchEvent(event);
                return false;
        }
    }

    private GestureDetector listGestureDetector = null;
    private boolean reachListTop = false;
    private boolean singleToTop = false;

    private class ListGestureListenr extends SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            mTouchMode = TOUCH_MODE_DOWN;
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return false;
        }

        @Override
		public void onShowPress(MotionEvent event) {
            
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float deltaX, float deltaY) {
            if (mIsNewScrolling) {
                mSumScrollX = 0;
                mSumScrollY = 0;
                mIsNewScrolling = false;
            }
            mSumScrollX += deltaX;
            mSumScrollY += deltaY;

            if (mTouchMode == TOUCH_MODE_DOWN) {
                float absSumScrollX = Math.abs(mSumScrollX);
                float absSumScrollY = Math.abs(mSumScrollY);

                if (absSumScrollX * SCROLL_SENSITIVITY < absSumScrollY &&
                		absSumScrollY > MIN_VSCROLL_DISTANCE * mScale) {
                    mTouchMode = TOUCH_MODE_VSCROLL;
                }
            } else if (mTouchMode == TOUCH_MODE_VSCROLL) {
                if (!mIsWeekviewMode && mSumScrollY > 0) {
                	handleVerticalScroll();
                } else if (mIsWeekviewMode && mSumScrollY < 0 &&
                		mAgendaList != null && mAgendaList.getFirstVisiblePosition() == 0) {
                	if (mAgendaList.getScrollY() <= 0) {
                        reachListTop = true;
                    } else {
                        reachListTop = false;
                    }

                    if (mAgendaList.getCount() <= 5) {
                    	handleVerticalScroll();
                    } else if (reachListTop && singleToTop) {
                        handleVerticalScroll();
                    }
                }
            }

            return false;
        }
    }

	private void setBaseDate(Time date) {
		if (date == null) return;

		mBaseDate.set(date);
		mBaseDate.normalize(true);

		date.monthDay = 1;
		long millis = date.normalize(true);
		mFirstMonthJulianDay = Time.getJulianDay(millis, date.gmtoff);

		date.month += 1;
		date.monthDay = 1;
		date.monthDay -= 1;
		millis = date.normalize(true);
		mLastMonthJulianDay = Time.getJulianDay(millis, date.gmtoff);

		date.set(mBaseDate);
		date.normalize(true);
	}

	private void setSecondaryDayIndex() {
		Log.d(TAG, "setSecondaryDayIndex() has been invoked");

		if (mActualWeekCount > 0 && mFirstMonthJulianDay > 0 && mLastMonthJulianDay > 0) {
			int firstIndex = mWeekViews[0].getWeekDayIndexByJulianDay(mFirstMonthJulianDay);
			if (firstIndex > 0) {
				mLastSecondaryDayInFirstWeek = (firstIndex - 1);
				mWeekViews[0].setSecondaryIndex(mLastSecondaryDayInFirstWeek, false);
				if (mWeekViews[0].hasToday()) {
					mIsTodaySecondary = mWeekViews[0].isTodaySecondary();
				}
			} else {
				mLastSecondaryDayInFirstWeek = -1;
			}

			/*int lastIndex = mWeekViews[mActualWeekCount - 1].getWeekDayIndexByJulianDay(mLastMonthJulianDay);
			if (lastIndex < (DAYS_PER_WEEK - 1)) {
				mFirstSecondaryDayInLastWeek = (lastIndex + 1);
				mWeekViews[mActualWeekCount - 1].setSecondaryIndex(mFirstSecondaryDayInLastWeek, true);
				if (mWeekViews[mActualWeekCount - 1].hasToday()) {
					mIsTodaySecondary = mWeekViews[mActualWeekCount - 1].isTodaySecondary();
				}
			} else {
				mFirstSecondaryDayInLastWeek = -1;
			}*/
			int index = mWeekViews[4].getWeekDayIndexByJulianDay(mLastMonthJulianDay);
            if (index >= 0 && index < (DAYS_PER_WEEK - 1)) {
                mFirstSecondaryDayInLastWeek = index + 1;
                mWeekViews[4].setSecondaryIndex(mFirstSecondaryDayInLastWeek, true);
                if (mWeekViews[4].hasToday()) {
                    mIsTodaySecondary = mWeekViews[4].isTodaySecondary();
                }
            } else {
                mFirstSecondaryDayInLastWeek = -1;
                if (!mWeekViews[4].hasFocusMonthDay()) {
                    mWeekViews[4].setSecondaryIndex(0, true);
                    if (mWeekViews[4].hasToday()) {
                        mIsTodaySecondary = true;
                    }
                }
            }

            int lastIndex = mWeekViews[5].getWeekDayIndexByJulianDay(mLastMonthJulianDay);
            if (lastIndex >= 0 && lastIndex < (DAYS_PER_WEEK - 1)) {
                mFirstSecondaryDayInLastWeek = lastIndex + 1;
                mWeekViews[5].setSecondaryIndex(mFirstSecondaryDayInLastWeek, true);
                if (mWeekViews[5].hasToday()) {
                    mIsTodaySecondary = mWeekViews[5].isTodaySecondary();
                }
            } else {
                mFirstSecondaryDayInLastWeek = mWeekViews[5].getOffsetByJulianDay(mLastMonthJulianDay) + 1;
                mWeekViews[5].setSecondaryIndex(mFirstSecondaryDayInLastWeek, true);
                if (mWeekViews[5].hasToday()) {
                    mIsTodaySecondary = true;
                }
            }

			Log.d(TAG, "setSecondaryDayIndex() result: " + mLastSecondaryDayInFirstWeek + ", " +
					mFirstSecondaryDayInLastWeek);
		}
	}

	public void setSelectedDate(Time time, boolean viewChanged) {
		if (time == null) return;

		Log.d(TAG, "set mSelectedDate in view " + mViewId);

		mSelectedDate.set(time);
		long millis = mSelectedDate.normalize(true);
		mSelectedJulianDay = Time.getJulianDay(millis, mSelectedDate.gmtoff);

		updateActionBarTime(mSelectedDate);

		if (viewChanged) {
			int weekNum = getWeekNumOfFirstMonthDay(mSelectedDate);
			addWeekViewsByWeekNum(weekNum);
		}
	}

	public void setSelectedDate(int julianDay, boolean viewChanged) {
		if (julianDay < 0) {
			throw new RuntimeException("invalid julian day: " + julianDay);
		}

		Log.d(TAG, "set mSelectedDate in view " + mViewId);

		mSelectedDate.setJulianDay(julianDay);
		mSelectedDate.normalize(true);

		mSelectedJulianDay = julianDay;

		if (viewChanged) {
			int weekNum = getWeekNumOfFirstMonthDay(mSelectedDate);
			addWeekViewsByWeekNum(weekNum);
		}
	}

	public void setSelectedDay(Time selectedDay) {
		// Log.d(DEBUG_TAG, "MonthView.setSelectedDay() has been invoked");
		if (mSelectedDate == null) {
			mSelectedDate = new Time();
		}
		mSelectedDate.set(selectedDay);
		mSelectedDate.normalize(true);
	}
	
	public Time getSelectedDate() {
		return mSelectedDate;
	}

	private int getWeekNumOfFirstMonthDay(Time date) {
		int firstDayOfWeek = Utils.getFirstDayOfWeek(mContext);
		int weekNum = CalendarUtils.getWeekNumOfFirstMonthDay(date, firstDayOfWeek);

		return weekNum;
	}

	public void addWeekViewsByWeekNum(int weekNum) {
		removeAllViews();
		mActualWeekCount = DEFAULT_WEEK_COUNT;

		Time currMonth = CalendarUtils.getJulianMondayTimeFromWeekNum(weekNum + 1);
		((MonthByWeekAdapter) mAdapter).updateFocusMonth(currMonth.month);

		boolean hasToday = false;
		for (int i = 0; i < mWeekViews.length; ++i) {
			mWeekViews[i] = (MonthWeekEventsView) mAdapter.getView(weekNum + i, null, this);

            addView(mWeekViews[i]);

            if (mWeekViews[i].hasToday()) {
                mHasToday = true;
                mTodayJulianDay = mWeekViews[i].getTodayJulianDay();
                hasToday = true;
            }

			mWeekViews[i].setMonthWeekViewIndex(i);
		}

		if (!hasToday) {
			mHasToday = false;
			mTodayJulianDay = -1;
		}

		addView(mAgendaField);
	}

	private int getMeasureSpec(int sizeWanted, int sizeMeasured) {
		int result = -1;
		if (sizeWanted > 0) {
			result = MeasureSpec.makeMeasureSpec(sizeWanted, MeasureSpec.EXACTLY);
		} else if (sizeWanted == ViewGroup.LayoutParams.MATCH_PARENT) {
			result = MeasureSpec.makeMeasureSpec(sizeMeasured, MeasureSpec.EXACTLY);
		} else if (sizeWanted == ViewGroup.LayoutParams.WRAP_CONTENT) {
			result = MeasureSpec.makeMeasureSpec(sizeMeasured, MeasureSpec.AT_MOST);
		}

		return result;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = getMeasuredWidth();
		int measuredHeight = getMeasuredHeight();
		if (mRootView != null) {
			setRootSize(mRootView.getMeasuredWidth(), mRootView.getMeasuredHeight());
		}

		int count = getChildCount();
		if (count > mActualWeekCount) {
			count = mActualWeekCount;
		}

		for (int i = 0; i < count; ++i) {
			View child = getChildAt(i);
			ViewGroup.LayoutParams params = child.getLayoutParams();
			int childWidth = getMeasureSpec(params.width, measuredWidth);
			int childHeight = getMeasureSpec(params.height, measuredHeight);
			child.measure(childWidth, childHeight);
		}

		if (count == mActualWeekCount) {
			int agendaFieldWidth = -1;
			int agendaFieldHeight = -1;

			View weekView = getChildAt(0);
			if (weekView != null) {
				agendaFieldWidth = getMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, mViewWidth);

				// int wantedHeight = (mRootHeight - weekView.getMeasuredHeight() * mActualWeekCount
				// 		- dp2px(BAR_HEIGHT));
                int wantedHeight = (mRootHeight - weekView.getMeasuredHeight() - dp2px(BAR_HEIGHT));

				agendaFieldHeight = getMeasureSpec(wantedHeight, 0);

				mAgendaField.measure(agendaFieldWidth, agendaFieldHeight);
			}
		}

		int wantedTotalHeight = heightMeasureSpec;
		if (count > 0) {
			wantedTotalHeight = count * getChildAt(0).getMeasuredHeight();
			wantedTotalHeight += mAgendaField.getMeasuredHeight();
		}

		setMeasuredDimension(widthMeasureSpec, wantedTotalHeight);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mViewWidth = w;
		mViewHeight = h;
	}

	// must re-layout the child views, or all week views will be
	// displayed on a single line
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int baseLeft = 0;
		int baseTop = 0;

		int count = getChildCount();
		if (count > mActualWeekCount) {
			count = mActualWeekCount;
		}

		View child = null;
		for (int i = 0; i < count; i++) {
			child = getChildAt(i);

			int left   = baseLeft;
			int right  = baseLeft + mViewWidth;
			int top    = baseTop + child.getMeasuredHeight() * (i);
			int bottom = baseTop + child.getMeasuredHeight() * (i + 1);

			child.layout(left, top, right, bottom);
		}

		View firstChild = getChildAt(0);
		mWeekViewWidth = firstChild.getMeasuredWidth();
		mWeekViewHeight = firstChild.getMeasuredHeight();

		setWeekViewLT();

		if (mAgendaField != null) {
			int left = baseLeft;
			int right = baseLeft + mViewWidth;
			int top = baseTop + mWeekViewHeight * mActualWeekCount;
			int bottom = mViewHeight +  mWeekViewHeight * (mIsWeekviewMode ? mActualWeekCount - 1 : 0);;
			Log.d("AgendaField", "layout mAgendaField top/bottom " + top + "/" + bottom);

			mAgendaField.layout(left, top, right, bottom);
		}
	}

	private void setSelectedDateIndex(float x, float y) {
		int totalHeight = mWeekViewHeight * mActualWeekCount;
		if (y < mWeekViewTop || y > totalHeight) {
			Log.d(TAG, "y is out of range");
			return;
		}

		mHighlightWeekIndex = (int) ((y - mWeekViewTop) / mWeekViewHeight);

		if (x < mWeekViewLeft || x > (mViewWidth - mViewPadding)) {
			Log.d(TAG, "x is out of range");
			return;
		}

		mHighlightWeekDayIndex = (int) ((x - mWeekViewLeft) /
				(mViewWidth - mWeekViewLeft - mViewPadding) * DAYS_PER_WEEK);
		Log.d(DEBUG_TAG, "set selected index as " + mHighlightWeekIndex + ", " + mHighlightWeekDayIndex);
	}

	private void setWeekViewLT() {
		mWeekViewLeft = mViewPadding;
		if (mShowWeekNum) {
			mWeekViewLeft += ((float) (mViewWidth - mViewPadding * 2) / (DAYS_PER_WEEK + 1));
		}
		mWeekViewTop = 0;
	}

	private static final int ACTION_STAY = 0;
	private static final int ACTION_NEXT = 1;
	private static final int ACTION_PREVIOUS = -1;

	private void highlightSelectedDate() {
		if (mHighlightWeekIndex >= 0 && mHighlightWeekIndex < mActualWeekCount) {
			mTappedView = mWeekViews[mHighlightWeekIndex];

			int action = ACTION_STAY;
			int secondaryDateIndex = -1;
			// if (mHighlightWeekIndex == 0 || mHighlightWeekIndex == mActualWeekCount - 1) {
			if (mHighlightWeekIndex == 0 || mHighlightWeekIndex == 4) { 
				secondaryDateIndex = mTappedView.getSecondaryDayIndex();
				if (secondaryDateIndex >= 0) {
					if (mTappedView.getSecondaryDayDirection()) {
						if (mHighlightWeekDayIndex >= secondaryDateIndex) {
							action = ACTION_NEXT;
						}
					} else {
						if (mHighlightWeekDayIndex <= secondaryDateIndex) {
							action = ACTION_PREVIOUS;
						}
					}
				}
			}

            if (mHighlightWeekIndex == 5) {
            	secondaryDateIndex = mTappedView.getSecondaryDayIndex();
                if (mHighlightWeekDayIndex >= secondaryDateIndex) {
                    action = ACTION_NEXT;
                }
            }

			if (action == ACTION_STAY) {
				if (mHighlightWeekIndex == mPrevHighlightWeekIndex &&
						mHighlightWeekDayIndex == mPrevHighlightWeekDayIndex) {
					Log.d(DEBUG_TAG, "tapped the same date, return");
					return;
				}

				int julianDay = mTappedView.setAndReturnClickedDay(mHighlightWeekDayIndex);
				mSelectedDate.setJulianDay(julianDay);
				mSelectedDate.normalize(true);

				Log.d(DEBUG_TAG, "1 - update time " + CalendarUtils.printDate(mSelectedDate));

				updateActionBarTime(mSelectedDate);

				setTimeOfController(mSelectedDate.toMillis(false));

				Log.d(DEBUG_TAG, "to invoke loadEventsOfSelectedDay()");
				loadEventsOfSelectedDay();

                if (mIsWeekviewMode) {
                    initMonthView();
                }

				for (int i = 0; i < mActualWeekCount; ++i) {
					if (i != mHighlightWeekIndex) {
						mWeekViews[i].clearClickedDay();
					}
				}
			} else {
				boolean gotoNextMonth = (action == ACTION_NEXT);
				Time targetDate = new Time();
				targetDate.set(mBaseDate);

				int dayOffset = (Math.abs(mHighlightWeekDayIndex - secondaryDateIndex) + 1);
				if (gotoNextMonth) {
					targetDate.month += 1;
					targetDate.monthDay = dayOffset;
				} else {
					targetDate.monthDay = 1;
					targetDate.monthDay -= dayOffset;
				}
				targetDate.normalize(true);
				Log.d(DEBUG_TAG, "switch secondary month: " + CalendarUtils.printDate(targetDate));

                if (targetDate.year < CalendarUtils.MIN_YEAR_NUM ||
                        targetDate.year > CalendarUtils.MAX_YEAR_NUM) {
		            mHighlightWeekIndex = mPrevHighlightWeekIndex;
		            mHighlightWeekDayIndex = mPrevHighlightWeekIndex;
		            return;
		        }

                if (mIsWeekviewMode) {
                    staticMove = true;
                }

				switchMonth(targetDate);
			}
		}
	}

	private void selectMonthDay(float x, float y) {
        if (mIsWeekviewMode) {
            if (y <= mWeekViewHeight) {
                y += mHighlightWeekIndex * mWeekViewHeight;
            } else {
                return;
            }
        } else {
            if (y > mWeekViewHeight * mActualWeekCount) {
                return;
            }
        }

		mFirstTouchedX = x;
		mFirstTouchedY = y;

		Log.d(DEBUG_TAG, "save selected index: " + mPrevHighlightWeekIndex + ", " + mPrevHighlightWeekDayIndex);

		mPrevHighlightWeekIndex = mHighlightWeekIndex;
		mPrevHighlightWeekDayIndex = mHighlightWeekDayIndex;

		mHighlightWeekIndex = -1;
		mHighlightWeekDayIndex = -1;

		setSelectedDateIndex(mFirstTouchedX, mFirstTouchedY);
		highlightSelectedDate();
	}

	protected void highlightMonthDate(Time targetDate) {
		Log.d(TAG, "highlightMonthDate() has been invoked in view " + mViewId);

		int julianDay = Time.getJulianDay(targetDate.normalize(true), targetDate.gmtoff);

		boolean foundHighlightGrid = false;
		for (int i = 0; i < mActualWeekCount; ++i) {
			int index = mWeekViews[i].getWeekDayIndexByJulianDay(julianDay);
			if (index >= 0) {
				setSelectedWeekIndex(i);
				setSelectedWeekDayIndex(index);

				foundHighlightGrid = true;
				highlightSelectedDate();
				break;
			}
		}

		if (!foundHighlightGrid) {
			mHighlightWeekIndex = -1;
			mHighlightWeekDayIndex = -1;
		}
		Log.d(TAG, "DEBUG: not found grid to highlight");
	}

	private boolean mHighlightGivenDay = false;

	public void setHighlightFlag(boolean flag) {
		mHighlightGivenDay = flag;
	}

	public void highlightToday() {
		Log.d(DEBUG_TAG, "highlightToday() mHighlightGivenDay = " + mHighlightGivenDay);
		if (!mHighlightGivenDay) {
			if (mHasToday && !mIsTodaySecondary) {
				Log.d(DEBUG_TAG, "highlightToday() 1");
				mSelectedDate.setJulianDay(mTodayJulianDay);
				mSelectedDate.normalize(true);
			} else {
				Log.d(DEBUG_TAG, "highlightToday() 2");
				mSelectedDate.monthDay = 1;
			}
		}
		mHighlightGivenDay = false;

		Log.d(DEBUG_TAG, "to highlight selected day: " + CalendarUtils.printDate(mSelectedDate));

		highlightMonthDate(mSelectedDate);
	}

	public void setAgendaListListener(EventLoader eventLoader) {
		if (mAgendaList == null) return;

		if (mAgendaList.getOnItemClickListener() == null) {
			mAgendaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				    int headersCount = 0;
                    if (!mBirthdayReminderEvents.isEmpty()) {
                        headersCount += 1;
                        if (position == headersCount - 1) {
                            Event event = mBirthdayReminderEvents.get(0);
                            sendEventForAgendaListItem(EventType.VIEW_BIRTHDAY, event);
                            return;
                        }
                    }

                    if (position < headersCount || position >= mAdapterEvents.size() + headersCount) return;

                    Event event = mAdapterEvents.get(position - headersCount);
					sendEventForAgendaListItem(EventType.VIEW_EVENT, event);
				}
			});
		}

        /*mAgendaList.auroraSetNeedSlideDelete(true);
        mAgendaList.auroraSetAuroraBackOnClickListener(new ListView.AuroraBackOnClickListener() {
            public void auroraOnClick(int position) {
                int headersCount = 0;
                if (!mBirthdayReminderEvents.isEmpty()) {
                    headersCount += 1;
                }

                if (position < headersCount || position >= mAdapterEvents.size() + headersCount) return;

                Event event = mAdapterEvents.get(position - headersCount);
                sendEventForAgendaListItem(EventType.DELETE_EVENT, event);
                mParentFragment.setCanEventChange();
            }

            public void auroraPrepareDraged(int positon) {
                 
            }

            public void auroraDragedSuccess(int position) {
                
            }

            public void auroraDragedUnSuccess(int position) {
                
            }
        });*/
	}

	private void sendEventForAgendaListItem(long eventType, Event event) {
		CalendarController controller = CalendarController.getInstance(mContext);
		Time selectedTime = new Time();
		selectedTime.set(controller.getTime());
		selectedTime.normalize(true);

		controller.sendEventRelatedEventWithExtra(
				mContext,
				eventType,
				event.id,
				event.startMillis,
				event.endMillis,
				0, 0,
				CalendarController.EventInfo.buildViewExtraLong(
					Attendees.ATTENDEE_STATUS_NONE, event.allDay),
					selectedTime.toMillis(true));
	}

	public void loadEventsOfSelectedDay() {
		if (mEventLoader == null) {
			Log.d(DEBUG_TAG, "mEventLoader is null!");
			return;
		}

        mSelectedDate = getTimeOfController();

		int selectedJulianDay = Time.getJulianDay(mSelectedDate.normalize(true), mSelectedDate.gmtoff);

		Log.d(DEBUG_TAG, "load event date " + CalendarUtils.printDate(mSelectedDate));

		if (mEvents == null) {
			mEvents = new ArrayList<Event>();
		} else {
			mEvents.clear();
		}

        mEventLoader.loadEventsInBackground(1, mEvents, selectedJulianDay, 
		 		mEventsLoadingFinishedCallback, mEventsLoadingCanceledCallback);
	}


	private View mRootView = null;
	
	public void setRootView(View root) {
		mRootView = root;
	}

	public void gotoDate(Time targetDate) {
        if (targetDate.year < CalendarUtils.MIN_YEAR_NUM || targetDate.year > CalendarUtils.MAX_YEAR_NUM) {
            showToast();
            return;
        }

        Log.d(DEBUG_TAG, "to highlight date " + CalendarUtils.printDate(targetDate));

		switchMonth(targetDate);

		// no need to check the result of method switchMonth(), because
		// the next view has been switched to the foreground
		/*MonthAgendaView view = (MonthAgendaView) mViewSwitcher.getCurrentView();
		view.setSelectedDate(targetDate, false);
		view.highlightMonthDate(targetDate);*/
	}

    private void showToast() {
        Toast.makeText(mContext, R.string.time_out_of_range, Toast.LENGTH_SHORT).show();
    }

	public boolean hasToday() {
		for (int i = 0; i < mActualWeekCount; ++i) {
			if (mWeekViews[i].hasToday()) {
				return true;
			}
		}

		return false;
	}

    private ViewGroup mAgendaField;

    private TextView mSolarDateView;
    private TextView mLunarDateView;
    private TextView mNoAgendaPrompt;
    private MonthAgendaListView mAgendaList;

    private View mBirthdayReminderView;
    private TextView mBirthdayReminderContentView;
    private ImageView mBirthdayReminderWishView;

	private void hideAgendaList(boolean hide) {
		Log.d(DEBUG_TAG, "hideAgendaList(): " + hide);
		if (hide) {
			mNoAgendaPrompt.setVisibility(View.VISIBLE);
			mAgendaList.setVisibility(View.GONE);
		} else {
			mAgendaList.setVisibility(View.VISIBLE);
			mNoAgendaPrompt.setVisibility(View.GONE);

			// startAgendaListAnim();
		}
	}

	private Animation mAnimAgendaFieldFadeIn = null;
	private static final long ANIM_DURATION_AGENDA_LIST_FADE_IN = 600L;

	private void startAgendaListAnim() {
		if (mHighlightWeekIndex == mPrevHighlightWeekIndex
				&& mHighlightWeekDayIndex == mPrevHighlightWeekDayIndex) {
			// no need run animation multiple times for the same day
			return;
		}

		if (mAnimAgendaFieldFadeIn == null) {
			mAnimAgendaFieldFadeIn = new AlphaAnimation(0.0f, 1.0f);
			mAnimAgendaFieldFadeIn.setDuration(ANIM_DURATION_AGENDA_LIST_FADE_IN);
		}

		mAgendaList.startAnimation(mAnimAgendaFieldFadeIn);
	}

	private int mRootWidth = -1;
	private int mRootHeight = -1;

	private void setRootSize(int w, int h) {
		if (mRootWidth < w) mRootWidth = w;
		if (mRootHeight < h) mRootHeight = h;
	}

	private String[] mWeekDayText = null;

	private int dp2px(int dp) {
		return (int) (dp * mScale + 0.5f);
	}

	private int sp2px(int sp) {
		return (int) (sp * mScaledDensity + 0.5f);
	}

	private void setSelectedWeekIndex(int index) {
		mPrevHighlightWeekIndex = mHighlightWeekIndex;
		mHighlightWeekIndex = index;
	}

	private void setSelectedWeekDayIndex(int index) {
		mPrevHighlightWeekDayIndex = mHighlightWeekDayIndex;
		mHighlightWeekDayIndex = index;
	}

}
