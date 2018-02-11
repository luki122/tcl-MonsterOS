package com.monster.market.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.RemotableViewMethod;
import android.view.TouchDelegate;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.monster.market.R;
import com.monster.market.utils.SettingUtil;
import com.monster.market.utils.SystemUtil;

import mst.app.dialog.AlertDialog;
import mst.preference.PreferenceManager;

public class ProgressBtn extends LinearLayout {

	public static final int STATUS_NORMAL = 1;
	public static final int STATUS_WAIT_DOWNLOAD = 2;
	public static final int STATUS_PROGRESSING_DOWNLOAD = 3;
	public static final int STATUS_WAIT_INSTALL = 4;
	public static final int STATUS_PROGRESSING_INSTALLING = 5;
	public static final int STATUS_FOUCE = 6;
	public static final int STATUS_FOUCE_NORMAL = 7;

	private int status = STATUS_NORMAL;

	private float startShow = 0.9f;
	private static int progressTime = 500;

	private Button btn;
	private Button btn_backup;
	private Button progress_btn;
	private ToCircleView tcv_toProgress;
	private RoundProgressView round_progress_view;
	private ImageView iv_progress1;
	private ImageView iv_progress2;
	private GlassView agv1;
	private GlassView agv2;
	private ToCircleView tcv_toRect;
	private Button fouceBtn;
	private Button fouceBtn_backup;

	private float textSize;

	private int progress = 0;
	private boolean isRuningStartAnim = false;
	private boolean isRuningEndAnim = false;
	private OnClickListener onButtonClickListener = null;
	private OnClickListener onNormalClickListener = null;
	private OnAnimListener onBeginAnimListener;

	public ProgressBtn(Context context) {
		super(context);
		initView();
	}

	public ProgressBtn(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	@Override
	@RemotableViewMethod
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		if (btn != null) {
			btn.setEnabled(enabled);
		}
		if (btn_backup != null) {
			btn_backup.setEnabled(enabled);
		}
		if (fouceBtn != null) {
			fouceBtn.setEnabled(enabled);
		}
		if (fouceBtn_backup != null) {
			fouceBtn_backup.setEnabled(enabled);
		}
		if( progress_btn != null){
		    progress_btn.setEnabled(enabled);
		}
		    
		
	}

	private void initView() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.view_progressbtn, this);
		btn = (Button) view.findViewById(R.id.btn);
		btn_backup = (Button) view.findViewById(R.id.btn_backup);
		progress_btn = (Button) view.findViewById(R.id.progress_btn);
		tcv_toProgress = (ToCircleView) view.findViewById(R.id.tcv_toProgress);
		round_progress_view = (RoundProgressView) view.findViewById(R.id.round_progress_view);
		iv_progress1 = (ImageView) view.findViewById(R.id.iv_progress1);
		iv_progress2 = (ImageView) view.findViewById(R.id.iv_progress2);
		agv1 = (GlassView) view.findViewById(R.id.agv1);
		agv2 = (GlassView) view.findViewById(R.id.agv2);
		tcv_toRect = (ToCircleView) view.findViewById(R.id.tcv_toRect);
		fouceBtn = (Button) view.findViewById(R.id.fouceBtn);
		fouceBtn_backup = (Button) view.findViewById(R.id.fouceBtn_backup);

		btn.setBackgroundResource(R.drawable.button_default_selector);
		btn_backup.setBackgroundResource(R.drawable.button_default_selector);
		btn_backup.setBackgroundColor(getResources().getColor(R.color.transparent));

		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (isRuningStartAnim()) {
					return;
				}

				if (!SettingUtil.canDownload(getContext())) {
					final View view = v;

					AlertDialog mWifiConDialog = new AlertDialog.Builder(
							mContext)
							.setTitle(
									getContext().getResources().getString(
											R.string.dialog_prompt))
							.setMessage(
									getContext().getResources().getString(
											R.string.no_wifi_download_message))
							.setNegativeButton(android.R.string.cancel, null)
							.setPositiveButton(android.R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											SharedPreferences sp = PreferenceManager
													.getDefaultSharedPreferences(getContext());
											SharedPreferences.Editor ed = sp.edit();
											ed.putBoolean("wifi_download_key", false);
											ed.commit();
											if (onButtonClickListener != null) {
												onButtonClickListener
														.onClick(view);
											}

											startBeginAnim();

											if (onNormalClickListener != null) {
												onNormalClickListener
														.onClick(view);
											}
										}

									}).create();
					mWifiConDialog.show();

				} else if (!SystemUtil.hasNetwork()) {
					Toast.makeText(getContext(), getContext()
							.getString(R.string.no_network_download_toast), Toast.LENGTH_SHORT).show();
				} else {
					if (onButtonClickListener != null) {
						onButtonClickListener.onClick(v);
					}

					startBeginAnim();

					if (onNormalClickListener != null) {
						onNormalClickListener.onClick(v);
					}
				}
			}
		});
		
		expandViewTouchDelegate(btn);

		textSize = getResources().getDimension(R.dimen.progressBtnTextSize);
	}
	
	/**
	 * @Title: startBeginAnim
	 * @Description: TODO 进度开始前动画
	 * @param
	 * @return void
	 * @throws
	 */
	public void startBeginAnim() {
		btn.setVisibility(View.GONE);
		int width = getResources().getDimensionPixelOffset(R.dimen.app_item_down_btn_width);
		int height = getResources().getDimensionPixelOffset(R.dimen.app_item_down_btn_height);
		tcv_toProgress.setAllViewWidthAndHeight(width, height,
				ToCircleView.TYPE_TO_CIRCLE);
		tcv_toProgress.setVisibility(View.VISIBLE);
		isRuningStartAnim = true;
		tcv_toProgress.startAnim(new CustomAnimCallBack() {
			@Override
			public void callBack(float interpolatedTime, Transformation t) {
				btn_backup.setAlpha(1 - interpolatedTime);
				btn_backup.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize
						- textSize * interpolatedTime);

				// if (interpolatedTime >= startShow) {
				// float alpha = (interpolatedTime - startShow) / (1 -
				// startShow);
				// progress_btn.setAlpha(alpha);
				// progress_btn.setVisibility(View.VISIBLE);
				// }

				if (interpolatedTime == 1) {
					isRuningStartAnim = false;
					setStatus(STATUS_WAIT_DOWNLOAD);

					if (onBeginAnimListener != null) {
						onBeginAnimListener.onEnd(ProgressBtn.this);
					}

					// new Handler().postDelayed(new Runnable() {
					//
					// @Override
					// public void run() {
					// round_progress_view.setProgressAnim(100, 1000);
					// }
					// }, 500);
					// new Handler().postDelayed(new Runnable() {
					// @Override
					// public void run() {
					// startEndAnim();
					// }
					// }, 2000);
				}
			}
		});
		round_progress_view.setProgress(0);
	}

	/**
	 * @Title: startEndAnim
	 * @Description: TODO 结束进度后动画，按钮变为focus
	 * @param
	 * @return void
	 * @throws
	 */
	public void startEndAnim() {
		startEndAnim(true);
	}
	
	public void startEndAnim(final boolean fouce) {
		int width = getResources().getDimensionPixelOffset(R.dimen.app_item_down_btn_width);
		int height = getResources().getDimensionPixelOffset(R.dimen.app_item_down_btn_height);
		tcv_toRect.setAllViewWidthAndHeight(width, height,
				ToCircleView.TYPE_TO_RECT);
		tcv_toRect.setVisibility(View.VISIBLE);
		progress_btn.setVisibility(View.GONE);
		round_progress_view.setVisibility(View.GONE);
		iv_progress1.clearAnimation();
		iv_progress1.setVisibility(View.GONE);
		iv_progress2.clearAnimation();
		iv_progress2.setVisibility(View.GONE);
		agv1.stop();
		agv1.setVisibility(View.GONE);
		agv2.stop();
		agv2.setVisibility(View.GONE);
		fouceBtn_backup.setVisibility(View.VISIBLE);
		isRuningEndAnim = true;
		tcv_toRect.startAnim(new CustomAnimCallBack() {
			@Override
			public void callBack(float interpolatedTime, Transformation t) {
				fouceBtn_backup.setAlpha(interpolatedTime);
				fouceBtn_backup.setTextSize(TypedValue.COMPLEX_UNIT_PX,
						textSize * interpolatedTime);

				if (interpolatedTime >= startShow) {
					float alpha = (interpolatedTime - startShow)
							/ (1 - startShow);
					fouceBtn.setAlpha(alpha);
					fouceBtn.setVisibility(View.VISIBLE);
				}

				if (interpolatedTime == 1) {
					isRuningEndAnim = false;
					if (fouce) {
						setStatus(STATUS_FOUCE);
					} else {
						setStatus(STATUS_FOUCE_NORMAL);
					}
				}
			}
		}, fouce);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		
		if (this.status == status) {
			return;
		}
		
		isRuningStartAnim = false;
		isRuningEndAnim = false;

		iv_progress1.clearAnimation();
		iv_progress2.clearAnimation();
		agv1.stop();
		agv2.stop();
		tcv_toProgress.clearAnimation();
		tcv_toRect.clearAnimation();
		round_progress_view.clearAnimation();
		
		this.status = status;
		
		switch (status) {
		case STATUS_NORMAL:
			btn.setVisibility(View.VISIBLE);
			btn_backup.setVisibility(View.VISIBLE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.GONE);
			fouceBtn_backup.setVisibility(View.GONE);
			
//			restoreViewTouchDelegate(lastDelegate);
			expandViewTouchDelegate(btn);
			
			btn_backup.setAlpha(1);
			btn_backup.setTextSize(TypedValue.COMPLEX_UNIT_PX, btn.getTextSize());
			break;
		case STATUS_WAIT_DOWNLOAD:
			btn.setVisibility(View.GONE);
			btn_backup.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.VISIBLE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.GONE);
			fouceBtn_backup.setVisibility(View.GONE);
			
			restoreViewTouchDelegate(lastDelegate);
			
			agv1.start();
			break;
		case STATUS_PROGRESSING_DOWNLOAD:
			btn.setVisibility(View.GONE);
			btn_backup.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.VISIBLE);
			round_progress_view.setVisibility(View.VISIBLE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.GONE);
			fouceBtn_backup.setVisibility(View.GONE);
			
//			restoreViewTouchDelegate(lastDelegate);
			expandViewTouchDelegate(progress_btn);
			
			break;
		case STATUS_WAIT_INSTALL:
			btn.setVisibility(View.GONE);
			btn_backup.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.VISIBLE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.GONE);
			fouceBtn_backup.setVisibility(View.GONE);
			
			restoreViewTouchDelegate(lastDelegate);
			
			agv2.start();
			break;
		case STATUS_PROGRESSING_INSTALLING:
			btn.setVisibility(View.GONE);
			btn_backup.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.VISIBLE);
			iv_progress2.setVisibility(View.VISIBLE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.GONE);
			fouceBtn_backup.setVisibility(View.GONE);
			
			restoreViewTouchDelegate(lastDelegate);
			
			iv_progress1.postInvalidate();
			iv_progress1.startAnimation(createRotateAnimation(false));
			iv_progress2.postInvalidate();
			iv_progress2.startAnimation(createRotateAnimation(true));
			break;
		case STATUS_FOUCE:
			setFouceStyle();
			
			btn.setVisibility(View.GONE);
			btn_backup.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.VISIBLE);
			fouceBtn_backup.setVisibility(View.GONE);
			
//			restoreViewTouchDelegate(lastDelegate);
			expandViewTouchDelegate(fouceBtn);
			
			break;
		case STATUS_FOUCE_NORMAL:
			setFouceNormalStyle();
			
			btn.setVisibility(View.GONE);
			btn_backup.setVisibility(View.GONE);
			tcv_toProgress.setVisibility(View.GONE);
			progress_btn.setVisibility(View.GONE);
			round_progress_view.setVisibility(View.GONE);
			iv_progress1.setVisibility(View.GONE);
			iv_progress2.setVisibility(View.GONE);
			agv1.setVisibility(View.GONE);
			agv2.setVisibility(View.GONE);
			tcv_toRect.setVisibility(View.GONE);
			fouceBtn.setVisibility(View.VISIBLE);
			fouceBtn_backup.setVisibility(View.GONE);
			
//			restoreViewTouchDelegate(lastDelegate);
			expandViewTouchDelegate(fouceBtn);
			
			break;
		}
	}

	public boolean isRuningStartAnim() {
		return isRuningStartAnim;
	}

	public boolean isRuningEndAnim() {
		return isRuningEndAnim;
	}

	public void setOnButtonClickListener(OnClickListener onButtonClickListener) {
		this.onButtonClickListener = onButtonClickListener;
	}

	public void setOnNormalClickListener(OnClickListener onNormalClickListener) {
		this.onNormalClickListener = onNormalClickListener;
	}

	public void setBtnText(String text) {
		btn.setText(text);
		btn_backup.setText(text);
		btn.postInvalidate();
		btn_backup.postInvalidate();
	}

	public void setFoucesBtnText(String text) {
		fouceBtn.setText(text);
		fouceBtn_backup.setText(text);
		fouceBtn.postInvalidate();
		fouceBtn_backup.postInvalidate();
	}

	public void setOnFoucsClickListener(OnClickListener onFoucsClickListener) {
		fouceBtn.setOnClickListener(onFoucsClickListener);
	}

	public void setOnProgressClickListener(
			OnClickListener onProgressClickListener) {
		progress_btn.setOnClickListener(onProgressClickListener);
	}

	public void setProgressBackground(int resid) {
		progress_btn.setBackgroundResource(resid);
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
		if (status != STATUS_PROGRESSING_DOWNLOAD) {
			setStatus(STATUS_PROGRESSING_DOWNLOAD);
		}
		round_progress_view.setProgress(progress);
	}

	public void setProgressAnim(int progress) {
		this.progress = progress;
		if (status != STATUS_PROGRESSING_DOWNLOAD) {
			setStatus(STATUS_PROGRESSING_DOWNLOAD);
		}
		round_progress_view.setProgressAnim(progress, progressTime);
	}

	public void setOnBeginAnimListener(OnAnimListener onBeginAnimListener) {
		this.onBeginAnimListener = onBeginAnimListener;
	}
	
	public void setFouceStyle() {
		fouceBtn.setBackgroundResource(R.drawable.button_focus_selector);
		fouceBtn.setTextColor(getResources().getColor(R.color.focus_btn_text_color));
		fouceBtn_backup.setTextColor(getResources().getColor(R.color.focus_btn_text_color));
	}
	
	public void setFouceNormalStyle() {
		fouceBtn.setBackgroundResource(R.drawable.button_default_selector);
		fouceBtn.setTextColor(getResources().getColor(R.color.black));
		fouceBtn_backup.setTextColor(getResources().getColor(R.color.black));
	}

	/**
	 * @Title: createRotateAnimation
	 * @Description: 创建旋转动画
	 * @param @return
	 * @return RotateAnimation
	 * @throws
	 */
	private RotateAnimation createRotateAnimation(boolean reverse) {
		RotateAnimation animation = null;
		if (!reverse) {
			animation = new RotateAnimation(0, 3600, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		} else {
			animation = new RotateAnimation(0, -3600, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		}
		animation.setInterpolator(new LinearInterpolator());
		animation.setFillAfter(true);
		animation.setDuration(10000);
		animation.setStartOffset(0);
		animation.setRepeatCount(1000);
		return animation;
	}
	
	//====================加大按钮点击区域start====================//
	
	private View lastDelegate;
	
	private void expandViewTouchDelegate(final View view) {
		post(new Runnable() {
			@Override
			public void run() {
				Rect bounds = new Rect();
				view.setEnabled(true);
				view.getHitRect(bounds);

		        bounds.top -= 1500;
		        bounds.bottom += 1500;
		        bounds.left -= 1500;
		        bounds.right += 1500;

		        TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

		        if (View.class.isInstance(view.getParent())) {
		            ((View) view.getParent()).setTouchDelegate(touchDelegate);
		        }
		        
		        lastDelegate = view;
			}
		});
	}
	
	private void restoreViewTouchDelegate(final View view) {
        post(new Runnable() {
            @Override
            public void run() {
            	if (view != null) {
            		Rect bounds = new Rect();
            		bounds.setEmpty();
            		TouchDelegate touchDelegate = new TouchDelegate(bounds, view);
            		
            		if (View.class.isInstance(view.getParent())) {
            			((View) view.getParent()).setTouchDelegate(touchDelegate);
            		}
            	}
            	
            }
        });
    }
	
	//====================加大按钮点击区域end====================//
	
	public interface OnAnimListener {
		public void onEnd(ProgressBtn view);
	}

}
