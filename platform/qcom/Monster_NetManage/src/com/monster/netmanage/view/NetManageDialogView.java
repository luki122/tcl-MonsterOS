package com.monster.netmanage.view;

import com.monster.netmanage.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 后台显示对话框
 * @author zhaolaichao
 *
 */
public class NetManageDialogView extends LinearLayout {

	private Context mContext;
	private TextView mTvMessage;
	private CheckBox mCb;
    private ICheckListener mCheckListener;
    
	public NetManageDialogView(Context context) {
		this(context, null);
	}

	public NetManageDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public NetManageDialogView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NetManageDialogView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		mContext = context;
		initView();
	}
	
	private void initView() {
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.dialog_no_more, this);
		mTvMessage = (TextView) itemView.findViewById(R.id.tv_messsage);
		mCb = (CheckBox) itemView.findViewById(R.id.cb_checked);
		mCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (mCheckListener != null) {
					mCheckListener.setOnCheckListener(buttonView, isChecked);
				}
			}
		});
	}

	public void setMessage(String message) {
		mTvMessage.setText(message);
	}

	public void setMessage(int resId) {
		mTvMessage.setText(resId);
	}
	
	public void setOnCheckListener(ICheckListener checkListener) {
		mCheckListener = checkListener;
	}
	public interface ICheckListener {
		void setOnCheckListener(CompoundButton buttonView, boolean isChecked);
	}
}
